# Reorder Linked List by Frequency Buckets

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Hash Map, Sorting

---

## 🗂 Problem Overview

Given the head of a singly linked list of positive integers, reorder it so nodes are grouped by value frequency in ascending order. Within a frequency tie, values appear in first-encounter order from the original list. The reordering must be done by relinking existing nodes — no new node allocation. The non-trivial constraint is the tie-breaking rule: it demands you track insertion order during frequency counting, not just counts alone.

---

## 🌍 Engineering Impact

This pattern appears directly in **stream analytics pipelines** (e.g., Kafka consumer lag reporting sorted by event frequency), **search ranking engines** that bucket documents by term frequency before applying secondary sort by recency, and **LFU cache eviction** where the eviction queue is a frequency-ordered linked structure. Without stable, insertion-order-preserving frequency bucketing, secondary sort keys produce non-deterministic output — a correctness bug that surfaces only under specific data distributions, making it expensive to diagnose in production at scale.

---

## 🔍 Problem Statement

**Input:** Head of a singly linked list; each node holds a positive integer (`1 ≤ val ≤ 10^5`). List length: `[1, 10^4]`.

**Output:** The same nodes relinked so values are ordered by ascending frequency. Ties in frequency are broken by first-encounter position in the original list.

**Constraints driving the algorithm:** You cannot allocate new nodes — only relink. This rules out building a fresh list from scratch and forces an in-place pointer manipulation strategy.

**Examples:**

| Input | Output | Reason |
|---|---|---|
| `[4, 2, 4, 1, 2, 4]` | `[1, 2, 2, 4, 4, 4]` | freq(1)=1, freq(2)=2, freq(4)=3 |
| `[3, 1, 3, 2, 1, 3, 2]` | `[1, 1, 2, 2, 3, 3, 3]` | freq(1)=freq(2)=2; 1 first seen at idx 1, 2 at idx 3 |

**Edge cases:** Single node (trivially sorted), all nodes identical (output equals input), all nodes unique (sorted by value's single occurrence, tie-broken by position — which is just original order).

---

## 🪜 How to Solve This

1. **Read the problem → notice two distinct sub-problems:** counting frequencies AND preserving a secondary ordering. These are separable concerns — solve them independently.

2. **Frequency counting → HashMap.** One pass through the list gives `val → count`. Natural choice; O(n) time and space.

3. **Tie-breaking by first encounter → track insertion order during the same pass.** A second map `val → first_seen_index` costs nothing extra in traversal. Alternatively, use an insertion-ordered structure (Python `dict` preserves insertion order since 3.7).

4. **Reordering → sort unique values.** You have a small set of unique values (at most 10^4). Sort them by `(frequency, first_seen_index)`. This gives the canonical output order for values.

5. **Reconstruct by relinking.** Iterate the sorted value list; for each value, walk the original node-chain (stored per-value) and link them in sequence. The "no new nodes" constraint is satisfied because you're stitching together existing node groups.

6. **Why not bucket sort?** Frequencies range `[1, n]` — bucket sort is viable (O(n) time) and worth mentioning, but the sort over unique values is at most O(u log u) where u ≤ n, so both are acceptable here.

---

## 🧩 Algorithm Walkthrough

**Pattern: HashMap + Stable Sort + Pointer Stitching**

**Step 1 — Single-pass metadata collection.**
Traverse the list once. For each node: increment `freq[val]`, and if `val` is not yet in `first_seen`, record `first_seen[val] = current_index`. Also append the node to a per-value node list `groups[val]`. This single pass maintains the invariant that `groups[val]` holds all nodes for that value in their original relative order.

**Step 2 — Sort unique values.**
Extract the set of unique values and sort by the composite key `(freq[val], first_seen[val])`. This is a stable sort over at most `u` unique values. The sort encodes both the frequency requirement and the tie-breaking rule in one operation, keeping concerns clean.

**Step 3 — Relink nodes.**
Iterate through the sorted value list. For each value, take its node chain from `groups[val]` and append it to a running tail pointer. Connect the tail of the previous group to the head of the current group. After processing all groups, set `tail.next = None` to terminate the list.

**Step 4 — Return new head.**
The head of the first group in sorted order is the new list head.

**Why this abstraction works:** Separating "grouping" (HashMap), "ordering" (sort), and "relinking" (pointer stitching) into three independent phases makes each step verifiable in isolation — a property that matters both in interviews and in production code review.

---

## 📊 Worked Example

Input: `[3, 1, 3, 2, 1, 3, 2]`

**Pass 1 — Build metadata:**

| Index | Val | freq | first_seen | groups (node refs) |
|---|---|---|---|---|
| 0 | 3 | 1 | 3→0 | groups[3]=[n0] |
| 1 | 1 | 1 | 1→1 | groups[1]=[n1] |
| 2 | 3 | 2 | — | groups[3]=[n0,n2] |
| 3 | 2 | 1 | 2→3 | groups[2]=[n3] |
| 4 | 1 | 2 | — | groups[1]=[n1,n4] |
| 5 | 3 | 3 | — | groups[3]=[n0,n2,n5] |
| 6 | 2 | 2 | — | groups[2]=[n3,n6] |

**Sort key per value:** `1→(2,1)`, `2→(2,3)`, `3→(3,0)`

**Sorted order:** `[1, 2, 3]`

**Relink:** `n1→n4→n3→n6→n0→n2→n5→None`

**Output:** `[1, 1, 2, 2, 3, 3, 3]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n + u log u)** where `n` is list length and `u` is the number of unique values (`u ≤ n`). The single traversal is O(n); sorting unique values is O(u log u); relinking is O(n). Dominant term is O(n log n) worst case (all unique values). At 10^6 elements this is ~20M operations — comfortably sub-second. At 10^9, memory pressure dominates before CPU does.

### Space Complexity

**O(n)** for the `groups`, `freq`, and `first_seen` maps. The `groups` map holds references to all n nodes distributed across u keys — it owns the dominant space. This cannot be reduced to O(1) without multiple passes and in-place value-based partitioning, which trades constant factors for algorithmic complexity.

---

## 💡 Key Takeaways

- **Pattern signal — "group then order":** Whenever a problem asks you to reorder elements based on an aggregate property of their value (frequency, sum, count), the shape is always: one-pass aggregation → sort on aggregate → reconstruct. Recognize this two-phase structure immediately.
- **Pattern signal — tie-breaking on secondary key:** Any time a sort has a tie-breaking rule referencing original position, you need to capture `first_seen` index explicitly during traversal — do not rely on sort stability alone unless the input structure already encodes order.
- **Implementation gotcha — forgetting to null-terminate:** After stitching the last group's tail into the new list, `tail.next` must be set to `None`. The last node in `groups[last_val]` still holds its original `next` pointer, which points into the middle of the old list.
- **Implementation gotcha — single-value lists:** If all nodes share one value, `groups` has one entry, the sort is a no-op, and the relink loop runs once. Verify your pointer stitching handles the case where there is no "previous tail" to connect.
- **Architectural insight:** The separation of aggregation, ordering, and mutation into three distinct, stateless phases is directly applicable to stream processing jobs (e.g., a Flink or Spark stage): collect → sort/rank → emit. Conflating these phases into a single mutable loop creates ordering bugs that are hard to test and harder to parallelize.

---

## 🚀 Variations & Further Practice

- **Descending frequency with LRU-style eviction:** Reorder by descending frequency, and among ties, place the *most recently seen* value first. The twist: "most recently seen" requires tracking last-encounter index rather than first, and the sort key flips — this maps directly to LFU cache implementation where you need both frequency and recency to break ties correctly.
- **Frequency bucketing on a doubly linked list with O(1) reorder:** Given a doubly linked list where you must maintain frequency order dynamically as new nodes are appended (not just a one-time reorder), static sorting is no longer viable. This forces a bucket-of-buckets structure (the core of an O(1) LFU cache) — a significant jump in pointer bookkeeping complexity.
- **Streaming variant — fixed memory window:** Reorder a sliding window of the last `k` nodes by frequency as each new node arrives, with O(log k) per insertion. The conceptual twist is that evicting the oldest node requires decrementing its frequency and potentially re-bucketing, turning a batch problem into an incremental one with non-trivial invalidation logic.