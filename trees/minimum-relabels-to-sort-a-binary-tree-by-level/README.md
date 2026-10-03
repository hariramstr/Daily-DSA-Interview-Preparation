# Minimum Relabels to Sort a Binary Tree by Level

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Breadth-First Search, Sorting

---

## 🗂 Problem Overview
Given a binary tree, compute the minimum number of label swaps needed so that each depth level is sorted from left to right in nondecreasing order. Swaps are allowed only between nodes on the same level; the tree shape is fixed. The output is the sum of minimum swaps across all levels. The challenge is scale: with up to 100000 nodes, brute-force per-level rearrangement or repeated swapping is too slow, so the solution must combine level-order traversal with efficient minimum-swap computation.

## 🌍 Engineering Impact
This pattern shows up anywhere data is partitioned into independent buckets that can be reordered locally but not globally: search-ranking stages grouped by shard, compiler passes normalizing symbols per scope, streaming pipelines batching events by window, or distributed schedulers reordering tasks within priority bands. The key architectural move is decomposition: process each partition independently, then aggregate cost. Without that framing, teams reach for global sorting or cross-bucket movement that violates constraints, inflates latency, and complicates correctness. At scale, recognizing “independent local reorder + minimum transformation cost” is what keeps solutions linearithmic and operationally predictable.

## 🔍 Problem Statement
You are given the root of a binary tree with `1 <= n <= 100000` nodes. Each node stores an integer label in the range `[-10^9, 10^9]`. In one operation, you may swap the labels of any two nodes at the same depth level. You may not move labels across levels, and you may not change the tree structure.

Return the minimum total number of swaps required so that every level, read left to right, is sorted in nondecreasing order.

Examples:

- `root = [5,4,3,7,6,8,9]` → `1`
- `root = [10,1,8,7,6,5,4]` → `2`

Edge cases matter: a single-node tree returns `0`; sparse or unbalanced trees still define levels normally under BFS; duplicate values may appear, so the minimum-swap logic must handle repeated labels correctly. The `O(n log n)` expectation rules out quadratic per-level strategies.

## 🪜 How to Solve This
1. Read the constraint carefully → swaps are allowed only within the same depth. That immediately suggests the problem decomposes by level.
2. If levels are independent, the tree problem becomes:  
   **BFS to collect each level** → then solve **minimum swaps to sort an array** for that level.
3. BFS is the natural traversal because it already exposes nodes level by level in left-to-right order. No extra indexing scheme is needed.
4. For one level’s values, sorting tells us the target arrangement. The real question is not “how to sort” but “how many swaps are minimally necessary.”
5. Minimum swaps to transform an array into a target order is a permutation-cycle problem: every element belongs somewhere, and each cycle of length `k` costs `k - 1` swaps.
6. Because values may repeat, avoid naive value-to-index mapping. Instead, sort `(value, originalIndex)` pairs or otherwise preserve identity so duplicates are distinguished.
7. Sum the swap counts from all levels. Since total nodes across all levels is `n`, the overall cost stays within `O(n log n)`.

## 🧩 Algorithm Walkthrough
1. **Traverse the tree with Breadth-First Search.**  
   This is the explicit pattern here: **BFS / level-order traversal**. At each iteration, the queue contains exactly one tree level. That invariant guarantees the extracted values are already in the left-to-right order the problem cares about.

2. **Materialize the current level’s labels into an array `vals`.**  
   This isolates one independent subproblem. Since labels cannot cross levels, any optimal global solution must be the sum of optimal per-level solutions.

3. **Build sortable identities for the level.**  
   Create pairs `(value, originalIndex)` and sort them by `value`, then by `originalIndex`. This produces the exact target ordering while preserving uniqueness even when values repeat. The invariant is: each original position maps to one unique destination.

4. **Construct the permutation from current positions to sorted positions.**  
   After sorting, each element knows where it should end up. This reduces the level to a permutation graph over indices.

5. **Count cycles in the permutation.**  
   For each unvisited index, follow the permutation until it loops. A cycle of length `k` requires `k - 1` swaps, which is minimal. This is the standard **minimum swaps via cycle decomposition** pattern.

6. **Accumulate the swap count and continue BFS.**  
   Every node is processed once by BFS, and every level is sorted once conceptually. Correctness follows from level independence plus the optimality of cycle-based swap counting for permutations.

## 📊 Worked Example
Take `root = [10,1,8,7,6,5,4]`.

| Level | Values        | Sorted Target | Permutation Cycles         | Swaps |
|------:|---------------|---------------|----------------------------|------:|
| 0     | `[10]`        | `[10]`        | `(0)`                      | 0     |
| 1     | `[1,8]`       | `[1,8]`       | `(0) (1)`                  | 0     |
| 2     | `[7,6,5,4]`   | `[4,5,6,7]`   | `0→3→0`, `1→2→1`           | 2     |

Trace for level 2:

1. Original indexed values: `[(7,0), (6,1), (5,2), (4,3)]`
2. Sorted indexed values: `[(4,3), (5,2), (6,1), (7,0)]`
3. Position mapping becomes `[3,2,1,0]`
4. Cycles:
   - `0 -> 3 -> 0` has length 2 → 1 swap
   - `1 -> 2 -> 1` has length 2 → 1 swap

Total = `0 + 0 + 2 = 2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` overall. BFS itself is `O(n)`, and for each level of width `w`, sorting costs `O(w log w)`. Summed across levels, the worst case is bounded by `O(n log n)`. At `10^6` elements this is still practical; at `10^9`, even linear scans become infrastructure decisions, so partitioning and external-memory strategies matter.

### Space Complexity
`O(w)` auxiliary space, where `w` is the maximum width of the tree. The queue, level array, sorted pairs, and visited markers are all level-scoped. You can reduce some temporary allocations with buffer reuse, but not without trading away clarity and implementation safety.

## 💡 Key Takeaways
- If a tree problem says “operate independently per depth” or “compare nodes level by level,” that is a strong signal for BFS plus per-level post-processing.
- If the objective is the minimum number of swaps to reach a sorted arrangement, think permutation cycles rather than simulating swaps greedily.
- Duplicates break naive `value -> index` mappings; preserve identity with original indices or a stable positional strategy.
- Be precise about level boundaries in BFS: capture `queue.size()` before expanding children, or you will mix levels and corrupt the cost.
- The transferable design insight is decomposition by constrained mobility: when elements can move only within partitions, solve each partition optimally and aggregate.

## 🚀 Variations & Further Practice
- Allow swaps between any nodes in the tree, not just within a level. The twist is that levels are no longer independent, so the problem becomes a global permutation over all nodes.
- Require each level to be sorted in zigzag order or according to a custom comparator. The traversal stays the same, but the target permutation changes per level.
- Minimize total swap cost when swapping positions has weights or distances. The harder part is that cycle length alone is no longer sufficient; you need weighted transformation logic.