# Minimum Swaps to Sort Values Within Each Tree Level

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Breadth-First Search, Sorting

---

## 🗂 Problem Overview
Given a binary tree, compute the minimum number of value swaps needed so that each depth level is strictly increasing from left to right. Swaps are allowed only between nodes on the same level; the tree shape is fixed. The output is the sum of the minimum swaps across all levels. The non-trivial part is that this is not a global tree-reordering problem: each level is an independent minimum-swap-to-sort array problem embedded inside a breadth-first traversal.

## 🌍 Engineering Impact
This pattern shows up whenever hierarchical data is processed in level batches and each batch must be normalized with minimal mutation cost. Examples include execution layers in query planners, breadth-wise compaction in storage engines, ranking corrections in search result tiers, and stage-local reorder minimization in streaming pipelines. At scale, the wrong approach degenerates into repeated ad hoc sorting and mutation logic with poor observability and unpredictable cost. The right decomposition—level-order traversal plus per-batch permutation analysis—gives bounded behavior, clean isolation of concerns, and an implementation that remains explainable under large fan-out and high node counts.

## 🔍 Problem Statement
You are given the root of a binary tree with up to `100000` nodes. Each node stores an integer in the range `[-10^9, 10^9]`. For any single operation, you may swap the values of any two nodes that lie on the same depth level. You may not change parent/child pointers.

Return the minimum total number of swaps required so that, for every level independently, node values appear in strictly increasing order from left to right. Values on the same level are guaranteed to be distinct, which means the target sorted order is unique.

Examples:

- `root = [5,3,4,7,6,8,9]` → `1`
- `root = [1,7,5,4,6,8,9]` → `1`

Edge cases matter:
- A single-node tree returns `0`.
- A level of size `1` always contributes `0`.
- Large trees require an `O(n log n)`-class solution; quadratic per-level reordering is not viable.

## 🪜 How to Solve This
1. Read the problem → notice the tree structure itself never changes. That immediately suggests we should traverse the tree only to *group values by depth*.

2. Grouping by depth in a tree → breadth-first search is the natural fit. BFS already processes nodes level by level, so it gives exactly the batches we need.

3. For each level, ask: what is the minimum number of swaps to transform the current value order into sorted order? That is a classic array permutation problem.

4. Distinct values on each level simplify everything → once the level is sorted, every value has exactly one target position. No ambiguity, no duplicate-handling logic.

5. Minimum swaps to realize a permutation comes from cycle decomposition. A cycle of length `k` needs `k - 1` swaps; summing over all cycles gives the optimum.

6. So the full solution is: BFS over the tree, extract one level’s values, compute minimum swaps to sort that array, add to the answer, continue.

That decomposition is the key insight: tree traversal is just batch extraction; the real work is permutation analysis per batch.

## 🧩 Algorithm Walkthrough
1. **Traverse the tree with Breadth-First Search (BFS).**  
   Use a queue initialized with `root`. At each iteration, capture the current queue size; that size is exactly the number of nodes on the current level. This maintains the invariant that one loop iteration processes one complete depth level.

2. **Extract the current level’s values in left-to-right order.**  
   Pop exactly `levelSize` nodes, append their values to an array, and enqueue their children. This preserves the required ordering because BFS naturally visits siblings in left-to-right sequence.

3. **Convert the level into a sorting permutation.**  
   Create a sorted copy of the level values. Build a mapping from value to its target index in the sorted array. Because values on a level are distinct, this mapping is one-to-one and stable.

4. **Compute minimum swaps using cycle decomposition.**  
   For each index `i`, determine where the element currently at `i` must go. This defines a permutation over indices. Visit each unprocessed index and follow the cycle until it closes. A cycle of length `k` contributes `k - 1` swaps.  
   This is the standard **minimum swaps to sort via permutation cycles** pattern, and it is correct because each swap can place at most one new element into its final position within a cycle.

5. **Accumulate across levels.**  
   Add the swap count for the current level to the global answer. Since swaps cannot cross levels, levels are independent subproblems, and summing their optima is globally optimal.

## 📊 Worked Example
Take `root = [1,7,5,4,6,8,9]`.

| Level | Values       | Sorted       | Cycles                          | Swaps |
|------:|--------------|--------------|----------------------------------|------:|
| 0     | `[1]`        | `[1]`        | `(0)`                            | 0     |
| 1     | `[7,5]`      | `[5,7]`      | `0 → 1 → 0`                      | 1     |
| 2     | `[4,6,8,9]`  | `[4,6,8,9]`  | all fixed points                 | 0     |

Trace:
1. BFS starts with root `1` → level 0 contributes `0`.
2. Next queue contains `7, 5` → sorted target is `[5, 7]`.
3. Index mapping is: `7` should go to `1`, `5` should go to `0`.
4. That forms one cycle of length `2`, so swaps = `2 - 1 = 1`.
5. Final level is already increasing, so no swaps are needed.

Total = `0 + 1 + 0 = 1`.

## ⏱ Complexity Analysis
### Time Complexity
Overall time is `O(n log n)` in the worst case. BFS visits each node once, contributing `O(n)`. For each level of width `w`, sorting costs `O(w log w)`, and the sum across levels is bounded by `O(n log n)` in the worst shape. At `10^6` elements this is practical; at `10^9`, it is not memory- or latency-feasible in a single-process setting.

### Space Complexity
Space is `O(w)` for the BFS queue plus `O(w)` for the current level’s arrays, where `w` is the maximum tree width. The auxiliary permutation bookkeeping is also level-local. You can reduce constant factors, but not the asymptotic bound, without sacrificing clarity or making traversal state harder to reason about.

## 💡 Key Takeaways
- If a tree problem says “do something independently for each depth,” that is a strong signal for BFS-level batching.
- If the operation is “minimum swaps to reach sorted order,” think permutation cycles, not simulation of arbitrary swaps.
- Be careful not to mix levels: swaps are legal only within the same depth, so global sorting logic is invalid.
- The example text is inconsistent in one place; implement against the stated rule, not the narrative typo.
- The transferable design insight is decomposition: separate structural traversal from per-batch optimization, and each part becomes simpler, testable, and scalable.

## 🚀 Variations & Further Practice
- Allow duplicate values on a level. The harder part is that the target sorted arrangement is no longer uniquely determined, so the permutation construction needs duplicate-aware indexing.
- Replace binary trees with general `k`-ary trees or DAG layers. The traversal generalizes, but width can grow sharply, making memory behavior and batching strategy more important.
- Minimize cost when swaps have weights or distance penalties instead of unit cost. The problem stops being plain cycle counting and becomes a weighted reordering optimization problem.