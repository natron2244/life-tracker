import { readFileSync } from 'fs';

// Load the allow-list from settings.json (sibling of this hooks directory).
const settings = JSON.parse(
  readFileSync(new URL('../settings.json', import.meta.url), 'utf8')
);

// Convert each "Bash(<rule>)" entry into a structured rule object.
// "Bash(git *)"  → { type: 'prefix', value: 'git' }   — allows any git subcommand
// "Bash(git log)"→ { type: 'exact',  value: 'git log' }— allows only that exact call
const bashRules = (settings.permissions?.allow ?? [])
  .filter(r => r.startsWith('Bash(') && r.endsWith(')'))
  .map(r => {
    const inner = r.slice(5, -1);
    if (inner.endsWith(' *')) {
      return { type: 'prefix', value: inner.slice(0, -2) };
    }
    return { type: 'exact', value: inner };
  });

// Quick pre-check: if the command contains any chaining operator we need to
// verify each segment individually rather than trusting a single allow rule.
// Negative lookbehind prevents \| (escaped pipe in grep patterns) from matching.
const CHAIN_PATTERN = /(?<!\\)\|{1,2}|&&|;|`|\$\(/;

// Recursively split a command string into its constituent segments, resolving
// subshell substitutions before splitting on shell operators.
//
// Walk order:
//   1. $(...) — replace each subshell with '' after recursing into its interior
//   2. `...`  — same treatment for backtick subshells
//   3. split on |, ||, &&, ; — the remaining flat operators
//
// Example: "git log | grep foo && echo $(date)"
//   $(...) pass  → recurse("date") → ["date"], inner replaced with ''
//   backtick pass → nothing to replace
//   split pass  → ["git log ", " grep foo ", " echo "]
//   combined    → ["date", "git log", "grep foo", "echo"]
function extractSegments(command) {
  const result = [];

  // Step 1 — strip $(...) subshells, collect their inner segments first so
  // they are checked even though they don't appear in the outer split.
  let remaining = command.replace(/\$\(([^)]*)\)/g, (_, inner) => {
    result.push(...extractSegments(inner));
    return '';
  });

  // Step 2 — same for backtick subshells.
  remaining = remaining.replace(/`([^`]*)`/g, (_, inner) => {
    result.push(...extractSegments(inner));
    return '';
  });

  // Step 3 — split the now-subshell-free string on flat chaining operators.
  // Use negative lookbehind to skip \| (escaped pipes in grep patterns).
  for (const seg of remaining.split(/(?<!\\)\|{1,2}|&&|;/)) {
    const trimmed = seg.trim();
    if (trimmed) result.push(trimmed);
  }

  return result;
}

// Return true if a single segment is covered by an allow rule.
// Strips leading env-var assignments (e.g. "FOO=bar git log") and shell
// redirections (2>&1, >/dev/null) before matching.
function isSegmentAllowed(segment) {
  let cmd = segment.replace(/^([A-Z_][A-Z0-9_]*=\S*\s+)+/, '').trim();
  // Strip redirect tokens: 2>&1, >/dev/null, 2>/dev/null, &>file, >>file, etc.
  cmd = cmd.replace(/\s+\d*[<>][&>]?\S*/g, '').trim();
  if (!cmd) return true;
  return bashRules.some(rule => {
    if (rule.type === 'exact') return cmd === rule.value;
    // prefix rule: "git" matches "git" alone or "git <anything>"
    return cmd === rule.value || cmd.startsWith(rule.value + ' ');
  });
}

// Read the PreToolUse event from stdin and decide allow / ask.
const chunks = [];
process.stdin.on('data', chunk => chunks.push(chunk));
process.stdin.on('end', () => {
  const input = JSON.parse(Buffer.concat(chunks).toString());
  const command = input.tool_input?.command ?? '';

  // Fast path: no chaining operators means the outer Bash permission rules
  // already cover this command — nothing extra to check.
  if (!CHAIN_PATTERN.test(command)) {
    process.exit(0);
  }

  // Slow path: decompose the chain and check every segment individually.
  const segments = extractSegments(command);
  const notAllowed = segments.filter(s => !isSegmentAllowed(s));

  if (notAllowed.length === 0) {
    // Every segment in the chain is explicitly allowed — approve silently.
    process.stdout.write(JSON.stringify({
      hookSpecificOutput: {
        hookEventName: 'PreToolUse',
        permissionDecision: 'allow'
      }
    }));
  } else {
    // At least one segment is not on the allow-list — escalate to the user.
    const names = [...new Set(notAllowed.map(s => s.split(/\s+/)[0]))].join(', ');
    process.stdout.write(JSON.stringify({
      hookSpecificOutput: {
        hookEventName: 'PreToolUse',
        permissionDecision: 'ask',
        permissionDecisionReason: `Chains to non-allowed command(s): ${names} — needs your approval`
      }
    }));
  }

  process.exit(0);
});
