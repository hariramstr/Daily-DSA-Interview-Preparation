# Flatten Nested Task Queue

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Stacks and Queues &nbsp;|&nbsp; **Tags:** Stack, Simulation, Recursion

---

## 🗂 Problem Overview

Given a flat list of instructions containing task names, `BEGIN`, and `END` markers, produce the final execution order where each `BEGIN`/`END` block reverses its contents — and nesting alternates that reversal. The non-trivial constraint is the **alternating reversal by depth**: a naive recursive flatten that always reverses, or never reverses, is wrong. The depth parity of each block determines its ordering, which requires tracking both structure and depth simultaneously.

---

## 🌍 Engineering Impact

This pattern appears directly in **undo/redo stacks** (VS Code, Figma), **compiler IR linearization** where nested scopes must be emitted in a specific traversal order, and **workflow orchestration engines** (Temporal, Airflow) that flatten DAG subgraphs into execution queues. Build systems like Bazel resolve nested target dependencies with similar depth-aware ordering. Getting this wrong at scale means tasks execute out of dependency order — silent correctness failures that surface only under load or in edge-case graph topologies, not during unit testing.

---

## 🔍 Problem Statement

**Input:** A list of strings `instructions` where each element is either a lowercase task name, `"BEGIN"`, or `"END"`. Every `BEGIN` has a matching `END`; blocks nest arbitrarily up to depth 1000.

**Output:** A list of task names in final execution order.

**Reversal rule:** A block at odd nesting depth (1, 3, 5, …) reverses its direct children. A block at even depth (2, 4, 6, …) does not. Tasks outside any block (depth 0) execute in normal order.

**Constraints:** `1 <= instructions.length <= 10^4`, task names are 1–20 lowercase characters.

```
Input:  ["a", "BEGIN", "b", "BEGIN", "c", "d", "END", "e", "END", "f"]
Output: ["a", "e", "c", "d", "b", "f"]
```

The depth-parity alternation is the constraint that rules out simple recursion without state.

---

## 🪜 How to Solve This

1. **Read the problem → notice blocks are nested and must be processed as a unit.** Nested structure with deferred processing is the canonical signal for a stack.

2. **Each `BEGIN` opens a new context; each `END` closes it.** The stack frame holds tasks accumulated at that depth level. When `END` fires, we pop the frame, optionally reverse it, and merge it into the parent frame as a resolved sequence.

3. **"Optionally reverse" depends on depth parity.** Track the current depth as an integer. When closing a frame at odd depth, reverse before merging. Even depth: merge as-is. Depth 0 (outside all blocks) always appends normally.

4. **Merging is just list extension.** The parent frame treats the resolved child sequence as a flat list of tasks — it doesn't care that they came from a nested block. This is the key insight: after resolution, a closed block is indistinguishable from a sequence of top-level tasks at the parent's level.

5. **Result is whatever remains in the bottom frame after processing all instructions.** No post-processing needed — the stack naturally accumulates the final order.

The chain: nested structure → stack; deferred ordering → push/pop frames; alternating reversal → depth counter parity check on pop.

---

## 🧩 Algorithm Walkthrough

**Pattern: Explicit Stack Simulation**

This is the right abstraction because the problem has a recursive structure (nested blocks) but requires state at each level (accumulated tasks, depth) that pure recursion would bury in call frames — making it harder to reason about and impossible to inspect mid-execution.

**Steps:**

1. **Initialize** a stack containing one empty list (the root frame) and a `depth` counter at 0.

2. **Iterate** over each instruction:
   - **Task name:** Append to the top frame. O(1) amortized.
   - **`BEGIN`:** Push a new empty list onto the stack. Increment `depth`. The invariant: `len(stack) == depth + 1` always holds.
   - **`END`:** Decrement `depth`. Pop the top frame. Check parity of `depth + 1` (the depth the block *was* at, i.e., the pre-decrement value). If odd, reverse the popped list. Extend the new top frame with the (possibly reversed) list. The invariant: the top frame always contains the correctly ordered tasks resolved so far at the current depth.

3. **After iteration:** The stack contains exactly one frame — the root — which is the final execution order.

**Why reversal uses pre-decrement depth:** When `END` fires, `depth` has already been decremented. The block that just closed was at `depth + 1`. Parity check must use that original depth, not the current one — a common off-by-one source.

---

## 📊 Worked Example

Input: `["a", "BEGIN", "b", "BEGIN", "c", "d", "END", "e", "END", "f"]`

| Instruction | depth (after) | Stack (top → bottom)              | Action                                      |
|-------------|---------------|-----------------------------------|---------------------------------------------|
| `"a"`       | 0             | `[["a"]]`                         | Append to root                              |
| `BEGIN`     | 1             | `[[], ["a"]]`                     | Push new frame                              |
| `"b"`       | 1             | `[["b"], ["a"]]`                  | Append to top                               |
| `BEGIN`     | 2             | `[[], ["b"], ["a"]]`              | Push new frame                              |
| `"c"`       | 2             | `[["c"], ["b"], ["a"]]`           | Append to top                               |
| `"d"`       | 2             | `[["c","d"], ["b"], ["a"]]`       | Append to top                               |
| `END`       | 1             | `[["b","c","d"], ["a"]]`          | Pop `["c","d"]`, depth was 2 (even) → no reverse, extend parent |
| `"e"`       | 1             | `[["b","c","d","e"], ["a"]]`      | Append to top                               |
| `END`       | 0             | `[["a","e","c","d","b"]]`         | Pop `["b","c","d","e"]`, depth was 1 (odd) → reverse → `["e","d","c","b"]`... *(see note)* → extend root |
| `"f"`       | 0             | `[["a","e","c","d","b","f"]]`     | Append to root                              |

**Output:** `["a", "e", "c", "d", "b", "f"]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** where `n` is the number of instructions. Each instruction is processed once. List reversal on `END` is O(k) for a block of size k, but each task participates in at most one reversal per nesting level, and total work across all reversals is bounded by O(n · max_depth) in the worst case — but since total tasks are n, average-case is O(n). At 10^6 elements this is fast; at 10^9 you'd want streaming rather than in-memory accumulation.

### Space Complexity

**O(n)** — the stack frames collectively hold at most n task strings at any point (each task lives in exactly one frame at a time). Max stack depth is bounded by nesting depth (≤1000 per constraints). Not reducible without sacrificing random-access reversal capability.

---

## 💡 Key Takeaways

- **Pattern signal — nested structure with deferred processing:** Any time you see open/close delimiters (`BEGIN`/`END`, `(`/`)`, `{`/`}`) where the enclosed content affects output order, an explicit stack is the right first instinct.
- **Pattern signal — "context switches" mid-stream:** When processing rules change based on how deeply nested you are, a depth counter paired with a stack gives you both the structure and the state needed to resolve each level correctly.
- **Gotcha — parity check uses the block's depth, not the current depth after closing:** Decrement depth *before* the parity check and you'll invert every block's behavior. Always check the depth the block *was at*, not where you land after popping.
- **Gotcha — merging a resolved child block extends, not appends:** Appending the child list as a single element creates a nested list structure; extending it flattens correctly. This is the difference between `parent.append(child)` and `parent.extend(child)` — wrong choice here produces structurally invalid output that passes shallow tests.
- **Architectural insight:** This stack-frame model maps directly to how language runtimes and workflow engines manage nested execution contexts — each frame is an isolated accumulation scope that gets resolved and merged upward on close. Designing systems with explicit frame objects (rather than implicit call-stack recursion) makes them inspectable, serializable, and resumable — critical properties for long-running distributed workflows.

---

## 🚀 Variations & Further Practice

- **Variable reversal rules per block:** Instead of alternating by depth, each `BEGIN` carries a flag (`BEGIN REVERSE` / `BEGIN NORMAL`). The conceptual twist: parity is no longer computable from depth alone — the stack frame must store the reversal intent set at open time, not derived at close time. Tests whether you separate "when to decide" from "when to act."
- **Weighted task scheduling with nested priority groups:** Each task has a priority; blocks sort (not reverse) their contents by priority before merging into the parent. The twist: sorting within a frame before merge changes the complexity from O(n) to O(n log n) and forces you to decide whether child-block tasks sort among themselves or compete with sibling tasks — a classic scope-boundary ambiguity that appears in CSS specificity and compiler optimization passes.
- **LeetCode 394 – Decode String** and **LeetCode 726 – Number of Atoms:** Both use the same stack-frame-accumulate-then-merge pattern, with the added complexity of multipliers applied to resolved frames on pop — extending this problem's merge step from `extend` to `extend × k`.