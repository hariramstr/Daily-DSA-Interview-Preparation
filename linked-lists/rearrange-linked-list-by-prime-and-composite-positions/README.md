# Rearrange Linked List by Prime and Composite Positions

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Math, Prime Numbers, In-place Algorithm

---

## 🗂 Problem Overview

Given a singly linked list of `n` nodes with 1-based position indices, rearrange it so nodes at prime positions come first (original order), followed by nodes at composite positions (original order), with the position-1 node appended last — all in-place with O(1) extra space. The non-trivial constraint is the simultaneous requirement of positional classification, stable relative ordering within each group, and zero auxiliary allocation.

---

## 🌍 Engineering Impact

This pattern — partitioning a sequence by a positional or computed predicate while preserving intra-group order — appears in streaming pipeline stage routing, where records must be bucketed by priority class without buffering the full stream. Search ranking engines apply equivalent logic when re-ordering result sets by tier (sponsored, organic, related) in a single pass. Memory-constrained embedded systems and kernel-level linked list management (e.g., Linux's `list_head`) demand exactly this kind of pointer-surgery approach; allocating auxiliary nodes is either prohibited or prohibitively expensive at the scale of millions of list operations per second.

---

## 🔍 Problem Statement

Given the head of a singly linked list with `n` nodes (1 ≤ n ≤ 10⁵, 1 ≤ `Node.val` ≤ 10⁹), rearrange nodes by the category of their 1-based position index:

- **Prime positions** (2, 3, 5, 7, …) → front of the output list, original relative order preserved.
- **Composite positions** (4, 6, 8, 9, …) → middle section, original relative order preserved.
- **Position 1** (neither prime nor composite) → appended at the very end.

No new `ListNode` objects may be allocated; only `next` pointer reassignment is permitted.

**Example 1:**
```
Input:  [10, 20, 30, 40, 50, 60, 70]
Output: [20, 30, 50, 70, 40, 60, 10]
```

**Example 2:**
```
Input:  [5, 15, 25]
Output: [15, 25, 5]
```

**Edge cases:** single node (output is the node itself), all positions prime (no composite section), n = 2 (position 1 and one prime).

---

## 🪜 How to Solve This

1. **Read the problem → notice three disjoint categories**, not two. The instinct to use a single "odd/even"-style split (as in LeetCode 328) is close but wrong — position 1 is its own bucket, not prime.

2. **Three buckets, stable order → three separate sub-lists built via tail pointers.** You don't need to know the full list length upfront; traverse once, classify each node by position, and splice it onto the appropriate sub-list's tail.

3. **Primality check per position → positions only go up to 10⁵**, so a simple trial-division check (or a precomputed sieve) is cheap. A sieve up to 10⁵ costs O(n log log n) time and O(n) space — but since we're already O(1) space-constrained, inline trial division per node keeps space at O(1) with negligible practical overhead at this scale.

4. **Connect the three sub-lists at the end** → prime tail → composite head, composite tail → position-1 node, position-1 node's `next` = `null`. Order of stitching matters; get it wrong and you corrupt the list.

5. **Handle empty sub-lists defensively** — if no composite nodes exist, prime tail connects directly to the position-1 node.

---

## 🧩 Algorithm Walkthrough

**Pattern: Multi-bucket In-place Partitioning with Tail Pointer Splicing**

This is the right abstraction because we need O(1) space, stable ordering, and multiple destination buckets — tail pointers let us append in O(1) per node without reversing or re-scanning.

1. **Initialize three dummy head nodes** (`primeDummy`, `compositeDummy`, `oneDummy`) and corresponding tail pointers pointing at each dummy. Dummies avoid special-casing empty buckets on first insertion.

2. **Traverse the original list**, maintaining a `position` counter starting at 1. For each node, sever its `next` pointer (set to `null`) to avoid dangling links, then classify:
   - `position == 1` → append to `oneDummy` sub-list.
   - `isPrime(position)` → append to `primeDummy` sub-list.
   - Otherwise → append to `compositeDummy` sub-list.
   Advance the relevant tail pointer after each append.

3. **Primality test (`isPrime`):** Return `false` for values < 2. Trial-divide from 2 to √position; if any divisor found, return `false`, else `true`. At n ≤ 10⁵, √n ≤ 317 — this is negligible per call.

4. **Stitch the three sub-lists:** Connect `primeTail.next` to `compositeDummy.next` (the composite head, which may be `null`). Connect `compositeTail.next` to `oneDummy.next` (the position-1 node, which may be `null` if n = 0). Ensure the final node's `next` is `null`.

5. **Return `primeDummy.next`.** If no prime-position nodes exist, fall back to `compositeDummy.next`, then `oneDummy.next`.

**Invariant maintained:** At every step, each sub-list contains only nodes of its category, in original relative order, with no cycles.

---

## 📊 Worked Example

Input: `[10, 20, 30, 40, 50, 60, 70]` — positions 1 through 7.

| Step | Position | Value | isPrime? | Prime List       | Composite List | One List |
|------|----------|-------|----------|------------------|----------------|----------|
| 1    | 1        | 10    | No (pos1)| —                | —              | 10       |
| 2    | 2        | 20    | Yes      | 20               | —              | 10       |
| 3    | 3        | 30    | Yes      | 20→30            | —              | 10       |
| 4    | 4        | 40    | No       | 20→30            | 40             | 10       |
| 5    | 5        | 50    | Yes      | 20→30→50         | 40             | 10       |
| 6    | 6        | 60    | No       | 20→30→50         | 40→60          | 10       |
| 7    | 7        | 70    | Yes      | 20→30→50→70      | 40→60          | 10       |

**Stitch:** `primeTail(70).next = 40`, `compositeTail(60).next = 10`, `10.next = null`.

**Output:** `[20, 30, 50, 70, 40, 60, 10]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n · √n)** — the single traversal is O(n), and each node incurs an `isPrime` check costing O(√position) ≤ O(√n). At n = 10⁵ this is ~3.2 × 10⁷ operations — fast. At n = 10⁶ it grows to ~10⁹, where a precomputed sieve becomes preferable. At n = 10⁹ a sieve is mandatory.

### Space Complexity

**O(1)** — the three dummy nodes are fixed-count stack allocations regardless of input size. No auxiliary arrays, no recursion stack. If a Sieve of Eratosthenes replaces trial division, space becomes O(n), which violates the problem constraint; trial division preserves the O(1) guarantee at the cost of a small constant-factor time increase.

---

## 💡 Key Takeaways

- **Pattern signal — "stable partition into k groups":** Whenever a problem asks for in-place grouping with preserved relative order across multiple categories, reach for k dummy-head + tail-pointer sub-lists before considering any sort-based or index-swap approach.
- **Pattern signal — positional predicate on a linked list:** If the classification depends on *index* rather than *value*, you must traverse sequentially and cannot use random access; this immediately rules out divide-and-conquer and points toward a single-pass accumulator pattern.
- **Implementation gotcha — sever `next` before appending:** Failing to null out each node's `next` before splicing it onto a sub-list risks creating cycles, especially for the last node of the original list, which may retain a stale pointer.
- **Implementation gotcha — stitching order and null sub-lists:** If the composite bucket is empty, `primeTail.next` must point to the position-1 node directly, not to `compositeDummy.next` (which is `null`). Always check each dummy's `next` before wiring; a single missed null-guard produces a silently truncated list.
- **Architectural insight:** The dummy-head pattern is the linked list equivalent of a sentinel node in a skip list or a virtual head in a queue — it eliminates conditional branching on empty-bucket first-insertion and makes the stitching logic uniform. In production list-management code (kernel drivers, allocator free-lists), this pattern reduces branch mispredictions and simplifies correctness proofs.

---

## 🚀 Variations & Further Practice

- **Generalize to k buckets by arbitrary predicate:** Instead of three fixed categories, partition the list into k groups defined by a function `f(position) → bucket_id`. The conceptual twist is that k may not be known until traversal completes (e.g., bucket by `position mod p` for a runtime-supplied prime `p`), requiring a dynamic map of dummy heads and a final merge pass over k sub-lists in sorted bucket order.
- **Rearrange by value-based primality (not position-based):** Classify nodes by whether `Node.val` is prime. This decouples classification from traversal order and introduces the challenge that `Node.val` can reach 10⁹ — trial division still works but costs O(√val) ≈ O(31623) per node, making a segmented sieve or Miller-Rabin primality test the production-grade choice.
- **In-place stable sort of a linked list by multiple keys (merge sort variant):** Extend the partitioning idea to a full ordering: sort nodes first by bucket (prime < composite < position-1), then by original value within each bucket. The twist is that merge sort on linked lists is O(n log n) with O(log n) stack space for recursion — achieving true O(1) space requires a bottom-up iterative merge, a significantly harder implementation challenge.