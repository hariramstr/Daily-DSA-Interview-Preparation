# Generate All Valid Locker Combinations

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Recursion and Backtracking &nbsp;|&nbsp; **Tags:** Recursion, Backtracking, String Generation

---

## 🗂 Problem Overview

Given integers `n` (combination length) and `k` (maximum digit value), generate every `n`-digit string using digits `1..k` where no two adjacent digits are identical, returned in lexicographic order. The output contract is deterministic: a sorted list of all valid strings. The non-trivial constraint is the adjacency restriction — it eliminates a predictable subset of candidates and demands a generation strategy that prunes early rather than filtering after the fact.

---

## 🌍 Engineering Impact

This pattern — constrained sequence enumeration with incremental pruning — appears directly in token generation for language model samplers (no consecutive repeated tokens under certain decoding strategies), test data generators for combinatorial coverage, and configuration validators that enforce alternation rules across pipeline stages. In distributed systems, the same backtracking skeleton drives constraint-satisfying shard-key assignment and replica placement where co-location rules mirror the adjacency constraint. Without early pruning, candidate explosion at scale makes exhaustive post-generation filtering untenable; backtracking keeps the working set bounded to the recursion depth.

---

## 🔍 Problem Statement

**Input:** Two integers `n` (1 ≤ n ≤ 6) and `k` (1 ≤ k ≤ 9).

**Output:** All `n`-digit strings over the alphabet `{1, …, k}` where no two consecutive digits are equal, sorted lexicographically ascending.

**Examples:**

| n | k | Output |
|---|---|--------|
| 2 | 3 | `["12", "13", "21", "23", "31", "32"]` |
| 1 | 4 | `["1", "2", "3", "4"]` |

**Edge cases:** When `n = 1`, no adjacency check is possible — all `k` digits are valid. When `k = 1` and `n > 1`, no valid combination exists because the only digit would always be adjacent to itself.

The constraint driving the algorithmic choice: the adjacency rule creates a dependency between positions, making position-independent enumeration (e.g., nested loops) incorrect without explicit cross-position checks. Backtracking encodes that dependency structurally.

---

## 🪜 How to Solve This

1. **Read the problem → notice position dependency.** Each digit's validity depends on what came immediately before it. This rules out treating each position independently.

2. **Position dependency + build-incrementally → think recursion.** You're constructing a string one character at a time, and the validity of each character is local to the previous character. That's a textbook recursive subproblem.

3. **Recursion with a bad-branch condition → backtracking.** At each position, iterate digits `1..k`. If a digit matches the last placed digit, skip it — prune the branch before descending. This is the backtracking step: you never generate children of an invalid node.

4. **Lexicographic order falls out for free.** Iterating digits `1..k` in ascending order at every level means the recursion tree is traversed left-to-right, producing results already sorted. No post-sort needed.

5. **Base case is clean:** when the current string reaches length `n`, record it. The recursion depth is bounded by `n ≤ 6`, so stack depth is never a concern.

---

## 🧩 Algorithm Walkthrough

**Pattern: Recursive Backtracking** — build the solution incrementally, pruning invalid branches at the point of extension rather than after full construction.

1. **Initialize:** Start with an empty current string and an empty results list. Call `backtrack("", results, n, k)`.

2. **Base case — record and return:** If `current.length() == n`, append `current` to results and return. The invariant at this point: every character in `current` satisfies the adjacency constraint with its predecessor.

3. **Recursive case — extend:** Iterate `digit` from `1` to `k` inclusive. For each digit, check whether it equals the last character of `current`. If it does, skip (prune). This prune is correct because any extension of this branch would carry the same violation forward.

4. **Recurse:** Append `digit` to `current`, recurse, then remove the last character (backtrack). The undo step is what makes this backtracking rather than plain recursion — the shared mutable state (`current`) is restored after each branch, keeping the invariant clean for sibling branches.

5. **Lexicographic guarantee:** Because digits are appended in ascending order `1..k` at every depth level, the DFS traversal order directly produces lexicographic output. No sorting step is required.

6. **Termination:** The recursion tree has depth exactly `n` and branching factor at most `k-1` per node (one digit is always excluded after the first level). It always terminates.

---

## 📊 Worked Example

**Input:** `n = 2`, `k = 3`

| Call depth | `current` | Digit tried | Last char | Pruned? | Action |
|---|---|---|---|---|---|
| 0 | `""` | 1 | none | No | recurse → `"1"` |
| 1 | `"1"` | 1 | `'1'` | **Yes** | skip |
| 1 | `"1"` | 2 | `'1'` | No | recurse → `"12"` ✓ |
| 1 | `"1"` | 3 | `'1'` | No | recurse → `"13"` ✓ |
| 0 | `""` | 2 | none | No | recurse → `"2"` |
| 1 | `"2"` | 1 | `'2'` | No | recurse → `"21"` ✓ |
| 1 | `"2"` | 2 | `'2'` | **Yes** | skip |
| 1 | `"2"` | 3 | `'2'` | No | recurse → `"23"` ✓ |

Pattern continues symmetrically for digit `3`, yielding `"31"`, `"32"`. Final output: `["12","13","21","23","31","32"]`.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(k · (k−1)^(n−1))** — the root has `k` branches; every subsequent level has at most `k−1` (one digit is always excluded). Each leaf requires O(n) work to record the string. At the problem's maximum (`n=6, k=9`), this yields at most 9 · 8^5 = 294,912 nodes — trivially fast. The formula does not scale to 10^6 inputs because `n` and `k` are bounded by the constraints, not the input size.

### Space Complexity

**O(n)** for the recursion stack and the mutable current-string buffer, both bounded by the combination length. The results list holds O(k · (k−1)^(n−1)) strings each of length `n`, which is the unavoidable output space. The working memory cannot be reduced further without lazy/iterator-based generation, which trades memory for call-site complexity.

---

## 💡 Key Takeaways

- **Pattern signal — position-dependent validity:** Whenever a character's legality depends on its immediate predecessor (or neighbor), that dependency is best encoded as a recursive parameter, not a post-generation filter.
- **Pattern signal — "generate all valid X":** Any problem asking for exhaustive enumeration under local constraints is a backtracking candidate; the constraint defines the prune condition, not a separate validation pass.
- **Implementation gotcha — prune before recursing, not after:** Checking the adjacency condition *before* the recursive call avoids allocating and immediately discarding invalid partial strings, keeping the invariant clean at every frame.
- **Off-by-one trap — digit range is 1-indexed:** The digits run `1..k` inclusive, not `0..k-1`. Using zero-based indexing silently produces wrong output that passes casual inspection because the structure looks correct.
- **Architectural insight:** The backtracking skeleton (extend → recurse → undo) is a reusable template. In production constraint-satisfaction systems (scheduler placement, config generators), separating the *constraint check* from the *traversal logic* into distinct functions makes the same skeleton adaptable to arbitrary rule sets without restructuring the core algorithm.

---

## 🚀 Variations & Further Practice

- **Non-adjacent constraint across a window:** Instead of prohibiting equal *adjacent* digits, prohibit any digit that appeared in the last `w` positions. The twist: the prune condition must inspect a sliding window of the current path, not just the last character — the state passed through recursion grows from a single character to a bounded queue.
- **Weighted enumeration — return combinations ranked by digit-sum:** The same backtracking structure applies, but results must be emitted into a priority queue rather than a list. The conceptual twist is that DFS traversal order no longer aligns with the desired output order, so lexicographic-for-free no longer holds.
- **Combinations on a graph alphabet:** Replace the linear digit range with a graph where edges define which digits may follow which (a generalization of the adjacency rule). The prune condition becomes an adjacency-list lookup, and the problem reduces to enumerating all paths of length `n` in a directed graph — connecting this pattern directly to DFS-based path enumeration.