# Interleave Linked Lists by Prime and Composite Positions

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Number Theory, Two Pointers

---

## 🗂 Problem Overview

Given a singly linked list, restructure it in-place so that: position 1 (neither prime nor composite) leads the list, followed by an alternating interleave of prime-indexed and composite-indexed nodes, with any remainder appended after the shorter group is exhausted. The non-trivial constraint is the combination of in-place O(1) space restructuring with real-time primality classification during a single O(n) traversal — no pre-sorting, no auxiliary arrays.

---

## 🌍 Engineering Impact

This pattern surfaces directly in **streaming data pipelines** where records must be partitioned and re-sequenced by a computed property without buffering the full dataset — think Kafka consumer rebalancing or priority-lane merging in network packet schedulers. In **search ranking systems**, interleaving results from heterogeneous sources (ads vs. organic, personalized vs. trending) by a computed slot rule is structurally identical. Getting the in-place constraint wrong at scale means O(n) auxiliary memory allocations that blow heap budgets under high-throughput conditions and introduce GC pressure that degrades p99 latency.

---

## 🔍 Problem Statement

Given the head of a singly linked list of integers (length 1–10⁵, values −10⁹ to 10⁹), restructure the list **in-place** according to the following positional classification (1-indexed):

- **Position 1**: neither prime nor composite — always the head of the result.
- **Prime positions** (2, 3, 5, 7, 11, …): form the first interleave group.
- **Composite positions** (4, 6, 8, 9, …): form the second interleave group.

The output order is: `[pos1] → [prime₁] → [composite₁] → [prime₂] → [composite₂] → … → [remaining]`

**Example 1:**
```
Input:  [10, 20, 30, 40, 50, 60, 70]
Output: [10, 20, 40, 30, 60, 50, 70]
```

**Example 2:**
```
Input:  [5, 3, 8, 1, 9]
Output: [5, 3, 1, 8, 9]
```

The O(1) space constraint eliminates any approach that collects nodes into auxiliary lists before relinking — the pointer surgery must happen during traversal.

---

## 🪜 How to Solve This

1. **Read the output contract** → the result is an interleave of two subsequences extracted from the original list by a positional predicate. This immediately signals a **partition-then-merge** pattern, not a sort.

2. **Partition by position, not value** → you don't know which nodes belong to which group until you've walked the list. Walk once, classify each position as prime or composite on the fly, and route each node's pointer into one of two separate chains.

3. **Primality in O(1) per node up to 10⁵** → a precomputed Sieve of Eratosthenes up to 10⁵ gives O(n log log n) preprocessing and O(1) lookup per position. Alternatively, trial division up to √pos works per-node but adds a constant factor. Sieve wins cleanly here.

4. **Build two sublists with tail pointers** → maintain `primeHead/primeTail` and `compHead/compTail`. As you traverse, splice each node into the correct sublist by updating `tail.next`. This keeps the partition O(n) with no extra allocations.

5. **Interleave the two chains** → standard two-pointer merge: alternate taking one node from each chain until one is exhausted, then append the remainder. The position-1 node is a degenerate case — prepend it before the merge begins.

---

## 🧩 Algorithm Walkthrough

**Pattern: Two-Pass Pointer Manipulation (Partition + Interleave)**

**Step 1 — Sieve preprocessing.**
Run the Sieve of Eratosthenes up to `n = 10⁵`. This gives an O(1) `isPrime[pos]` lookup for every position encountered during traversal. The sieve cost is O(n log log n), dominated by the traversal.

**Step 2 — Single-pass partition.**
Walk the list with a position counter starting at 1. For position 1, save the node as `anchor`. For each subsequent node, check `isPrime[pos]`: if true, append to the prime sublist via `primeTail.next`; otherwise append to the composite sublist via `compTail.next`. Null-terminate both sublists after the loop. **Invariant**: every node is visited exactly once, and each node's `next` pointer is overwritten, so no original linkage leaks into the new structure.

**Step 3 — Interleave merge.**
Use two pointers `p = primeHead`, `c = compHead`. Alternate: take from `p`, advance `p`; take from `c`, advance `c`. When either pointer hits null, break and append the non-null pointer's remaining chain. **Invariant**: at every step the output chain is a valid, non-cyclic linked list.

**Step 4 — Reconnect anchor.**
Set `anchor.next = mergedHead`. Return `anchor` as the new head.

The explicit null-termination of sublists before merging is the most common source of bugs — stale `next` pointers create cycles that are hard to detect without a cycle-check pass.

---

## 📊 Worked Example

Input: `[10, 20, 30, 40, 50, 60, 70]` (n = 7)

**Partition pass:**

| Position | Node Value | Classification | Prime Chain      | Composite Chain |
|----------|------------|----------------|------------------|-----------------|
| 1        | 10         | neither        | —                | —               |
| 2        | 20         | prime          | 20               | —               |
| 3        | 30         | prime          | 20 → 30          | —               |
| 4        | 40         | composite      | 20 → 30          | 40              |
| 5        | 50         | prime          | 20 → 30 → 50     | 40              |
| 6        | 60         | composite      | 20 → 30 → 50     | 40 → 60         |
| 7        | 70         | prime          | 20 → 30 → 50 → 70 | 40 → 60       |

**Interleave merge:** `20 → 40 → 30 → 60 → 50 → 70` (prime exhausted last, 70 appended)

**Prepend anchor:** `10 → 20 → 40 → 30 → 60 → 50 → 70` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** overall. The sieve runs in O(n log log n) but with n ≤ 10⁵ this is effectively constant. The partition and interleave passes are each O(n). At 10⁶ elements this is comfortably sub-millisecond; at 10⁹ elements, the linear bound holds but memory bandwidth becomes the bottleneck, not CPU cycles.

### Space Complexity

**O(n)** for the sieve array (bounded by max list length, not input values). The pointer manipulation itself is O(1). The sieve can be replaced with per-node trial division to achieve true O(1) auxiliary space at the cost of an O(√n) factor per node — a meaningful trade-off only when memory is severely constrained.

---

## 💡 Key Takeaways

- **Pattern signal — positional predicate on a sequence**: any problem that partitions a linear structure by a computed property of *index* (not value) and then merges the partitions is a partition-then-interleave problem, not a sorting problem.
- **Pattern signal — "in-place" + "two groups"**: when you see in-place restructuring into two interleaved subsequences, reach for dual tail-pointer partition before considering any swap-based approach; swaps create O(n²) pointer chasing.
- **Gotcha — stale `next` pointers**: after partitioning, both sublists still contain nodes whose `next` pointers point into the original list. Failing to null-terminate `primeTail.next` and `compTail.next` before the merge pass creates cycles that manifest as infinite loops, not crashes.
- **Gotcha — position-1 special case**: the anchor node must be extracted *before* the partition loop, not treated as a degenerate prime or composite. Folding it into the general case with a conditional inside the loop is a subtle off-by-one that corrupts the interleave order.
- **Architectural insight**: the sieve-as-lookup-table pattern generalizes to any scenario where a stream of items must be classified by a property that is expensive to compute per-item but cheap to precompute over the domain — rate-limit tier assignment, shard routing by key hash range, and feature-flag bucketing all follow this exact structure.

---

## 🚀 Variations & Further Practice

- **Generalized k-way interleave by arbitrary positional predicate**: instead of prime/composite, partition into k groups by `pos % k` or a custom classifier, then perform a k-way merge. The conceptual twist is that a two-pointer merge becomes a min-heap merge, pushing the complexity to O(n log k) and requiring you to reason about heap-based pointer management on linked list nodes.
- **Streaming variant with unknown length**: the sieve requires knowing n upfront. Reformulate for a stream where n is unknown — you must switch to per-node trial division or maintain a dynamic prime sieve (e.g., a segmented sieve with a sliding window), which forces a space-time trade-off analysis under memory pressure.
- **In-place interleave with stability under node-value constraints**: extend the problem so that within each group (prime-indexed, composite-indexed), nodes must remain in sorted order by value without auxiliary storage — this combines the partition logic with an in-place linked list merge sort, compounding the pointer invariant complexity significantly.