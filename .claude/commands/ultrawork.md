# /ultrawork — Parallel Work Orchestrator

Orchestrate up to 3 sub-agents working in parallel on isolated git worktrees. You are the orchestrator: surface all questions to the user, detect file conflicts, enforce dependency order, and produce a safe merge report.

## Step 1 — Gather Tasks

If `$ARGUMENTS` is non-empty, parse it as a semicolon-separated or newline-separated list of tasks.

If `$ARGUMENTS` is empty or blank, ask the user for their task list now before proceeding.

Assign each task a short ID: `task-1`, `task-2`, `task-3`, etc. Show the user the numbered list before continuing.

---

## Step 2 — Planning Phase (Workflow tool)

Call the Workflow tool with the script below. Pass this as `args`:

```json
{ "tasks": [{ "id": "task-1", "description": "..." }, ...] }
```

**Planning workflow script** (pass as the `script` parameter):

```javascript
export const meta = {
  name: 'ultrawork-plan',
  description: 'Collect implementation plans from parallel sub-agents — no code changes',
  phases: [{ title: 'Planning' }],
}

const PLAN_SCHEMA = {
  type: 'object',
  properties: {
    taskId: { type: 'string' },
    taskSummary: { type: 'string' },
    filesToModify: { type: 'array', items: { type: 'string' } },
    filesToCreate: { type: 'array', items: { type: 'string' } },
    dependsOnTaskIds: {
      type: 'array',
      items: { type: 'string' },
      description: 'IDs of other tasks whose changes must exist before this one can be implemented',
    },
    questions: {
      type: 'array',
      items: { type: 'string' },
      description: 'Clarifying questions that must be answered before implementation',
    },
    implementationSteps: { type: 'array', items: { type: 'string' } },
  },
  required: ['taskId', 'taskSummary', 'filesToModify', 'filesToCreate', 'dependsOnTaskIds', 'questions', 'implementationSteps'],
}

const { tasks } = args

phase('Planning')
log(`Collecting plans for ${tasks.length} tasks in parallel...`)

const otherTasksSummary = tasks.map(t => `- ${t.id}: ${t.description}`).join('\n')

const plans = await parallel(tasks.map(t => () =>
  agent(
    `You are a planning agent. Analyze the codebase for task "${t.id}".

Task: ${t.description}

IMPORTANT: Do NOT write or modify any code. Read and analyze only.

Your job:
1. Identify every file that would need to be modified or created to complete this task.
2. Identify which of the other tasks your implementation depends on — list their IDs in dependsOnTaskIds. A dependency means your code requires that task's changes to already be in place before you can implement yours.
3. List any questions that must be answered before implementation can begin.
4. Write clear, ordered implementation steps.

Other tasks running in parallel (for dependency identification):
${otherTasksSummary}

Return your structured plan.`,
    { schema: PLAN_SCHEMA, label: `plan:${t.id}`, phase: 'Planning' }
  )
))

return { plans: plans.filter(Boolean) }
```

---

## Step 3 — Orchestrator Review (you handle this — no workflow)

After the planning workflow returns `{ plans }`:

### 3a. Surface questions to the user
Collect all `questions` arrays from every plan. If any questions exist, call `AskUserQuestion` to get answers (group related questions, max 4 per call). Record answers as a map `{ "question text": "answer text" }` keyed per task.

### 3b. Detect file conflicts and augment dependencies
For each pair of plans A and B, compute:
- `overlap = (A.filesToModify ∪ A.filesToCreate) ∩ (B.filesToModify ∪ B.filesToCreate)`

If overlap is non-empty, determine which is more foundational (creates the structure the other builds on) and add the foundational task's ID to the other's `dependsOnTaskIds`. Note the conflicting files.

### 3c. Topological sort into execution batches
1. Build a directed dependency graph.
2. Batch 0: tasks with no dependencies.
3. Batch N: tasks whose dependencies are all satisfied by earlier batches.
4. If there is a cycle (A depends on B, B depends on A), ask the user to break the tie.

### 3d. Show pre-execution summary to user
Present this before executing and wait for confirmation:

```
Planning complete. Execution plan:

Batch 0 (parallel): task-1, task-3
  task-1 → [app/Foo.kt]
  task-3 → [app/Bar.kt]

Batch 1 (after Batch 0): task-2
  task-2 → [app/Foo.kt, app/Baz.kt] — depends on task-1 (shared: app/Foo.kt)

File conflict notes:
  app/Foo.kt: task-1 and task-2 both modify it → task-1 runs first

Ready to execute?
```

---

## Step 4 — Execution Phase (Workflow tool, one call per batch)

Because worktree branch names are only known after execution, run each batch as a **separate Workflow call** and wire branch names between calls.

For each batch (in order):

1. Build the args for this batch. For each task, include `dependsOnTaskIds` and the **resolved branch names** from completed tasks (from prior batch results).
2. Call the Workflow tool with the execution script below.
3. After the workflow returns, record each task's `branch` name for use in the next batch.

**Execution workflow script** (pass as the `script` parameter for each batch call):

```javascript
export const meta = {
  name: 'ultrawork-execute',
  description: 'Execute one batch of approved plans in parallel worktrees',
  phases: [{ title: 'Execute' }],
}

const RESULT_SCHEMA = {
  type: 'object',
  properties: {
    taskId: { type: 'string' },
    success: { type: 'boolean' },
    branch: { type: 'string' },
    filesChanged: { type: 'array', items: { type: 'string' } },
    summary: { type: 'string' },
    testInstructions: { type: 'string' },
  },
  required: ['taskId', 'success', 'branch', 'filesChanged', 'summary', 'testInstructions'],
}

const { tasks } = args
// tasks: Array<{ taskId, description, plan, answers, dependsOnBranches }>
// dependsOnBranches: branch names from prior batches (resolved by orchestrator)

phase('Execute')
log(`Executing ${tasks.length} tasks in parallel on isolated worktrees...`)

const results = await parallel(tasks.map(t => () => {
  const mergeBlock = (t.dependsOnBranches && t.dependsOnBranches.length > 0)
    ? `IMPORTANT — before making any code changes, merge your dependencies:\n${t.dependsOnBranches.map(b => `  git merge ${b}`).join('\n')}\nThis brings in the changes your task builds on.\n\n`
    : ''

  const answersBlock = (t.answers && Object.keys(t.answers).length > 0)
    ? `Answers to your questions:\n${Object.entries(t.answers).map(([q, a]) => `Q: ${q}\nA: ${a}`).join('\n\n')}\n\n`
    : ''

  return agent(
    `Implement task "${t.taskId}": ${t.description}

${mergeBlock}${answersBlock}Approved plan:
Files to modify: ${(t.plan.filesToModify || []).join(', ') || 'none'}
Files to create: ${(t.plan.filesToCreate || []).join(', ') || 'none'}
Steps:
${(t.plan.implementationSteps || []).map((s, i) => `${i + 1}. ${s}`).join('\n')}

After completing all changes:
- Run "git branch --show-current" and include the branch name in your response
- List every file you actually modified or created
- Write clear test instructions for someone reviewing this worktree before merging`,
    {
      schema: RESULT_SCHEMA,
      label: `exec:${t.taskId}`,
      phase: 'Execute',
      isolation: 'worktree',
    }
  )
}))

return { results: results.filter(Boolean) }
```

**Wiring branches between batches**: After each batch workflow returns, build a lookup `{ taskId → branch }` from the results. For the next batch's tasks, populate `dependsOnBranches` by resolving each task's `dependsOnTaskIds` through that lookup.

---

## Step 5 — Final Report

After all batches complete, format and present this report to the user:

```
## /ultrawork Complete

### Worktrees
| Task | Description | Branch | Files Changed |
|------|-------------|--------|---------------|
| task-1 | Add dark mode | claude/worktree-abc | Theme.kt |
| task-3 | Fix crash | claude/worktree-xyz | GameViewModel.kt |
| task-2 | Refactor nav | claude/worktree-def | AppNavigation.kt, Theme.kt |

### Recommended Merge Order
Merge in this order to avoid conflicts:

1. task-1 (claude/worktree-abc)
   Test: <test instructions>
   git merge claude/worktree-abc

2. task-3 (claude/worktree-xyz) — independent, merge any time
   Test: <test instructions>
   git merge claude/worktree-xyz

3. task-2 (claude/worktree-def) — merge task-1 first
   Test: <test instructions>
   git merge claude/worktree-def

### File Conflict Notes
- Theme.kt: modified by task-1 and task-2. task-1 merged first so task-2's worktree already includes those changes. No manual resolution expected.
- Any other shared files: note them here with guidance.

### Failed Tasks
List any tasks where success=false, with their error summaries.
```
