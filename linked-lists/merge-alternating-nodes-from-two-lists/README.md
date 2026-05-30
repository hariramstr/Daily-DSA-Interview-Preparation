# Merge Alternating Nodes from Two Lists

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked List, Two Pointers, In-Place Manipulation

---

## 🗂 Problem Overview

Given two singly linked lists, interleave their nodes in alternating order — one from `list1`, one from `list2`, repeat — without allocating new nodes. The core contract: return the head of the merged list, built entirely by rewiring `.next` pointers in-place. The non-trivial constraint is handling unequal lengths cleanly: when one list runs out, the remainder of the other must be appended without losing any nodes or creating cycles.

---

## 🌍 Engineering Impact

This pattern surfaces wherever two ordered streams must be interleaved without buffering. In **merge-based storage engines** (LevelDB, RocksDB), compaction merges sorted SSTable runs by alternating reads from two iterators — the same pointer-advancement logic applies. **Round-robin load balancers** cycle across backend pools using the same structural invariant. **Streaming join operators** in systems like Apache Flink interleave event streams from two partitions. In all these cases, allocating intermediate nodes under high throughput is unacceptable; in-place pointer manipulation is what keeps memory pressure flat at scale.

---

## 🔍 Problem Statement

Given heads of two singly linked lists `list1` and `list2`, merge them by alternating nodes: `list1[0] → list2[0] → list1[1] → list2[1] → ...`. When one list is exhausted, append the remaining nodes of the longer list. Return the head of the merged list. No new nodes may be created — only `.next` pointer reassignment is permitted.

**Constraints:** Each list has `[0, 1000]` nodes; node values in `[-10^4, 10^4]`; either list may be `null`.

**Examples:**

| Input | Output |
|---|---|
| `list1 = [1,3,5]`, `list2 = [2,4,6]` | `[1,2,3,4,5,6]` |
| `list1 = [1,3]`, `list2 = [2,4,6,8]` | `[1,2,3,4,6,8]` |

**Edge cases to handle:** either list is `null` (return the other), lists of length 1, and lists with large length disparity.

---

## 🪜 How to Solve This

1. **Read the problem → notice the output is a single linked list built from existing nodes.** No new allocation means the solution is entirely about rewiring `.next` pointers.

2. **Alternating selection → two pointers advancing in lockstep.** One pointer (`p1`) walks `list1`, another (`p2`) walks `list2`. At each step we need to splice `p2` between `p1` and `p1.next`.

3. **Splicing one node in → requires saving `p1.next` and `p2.next` before overwriting them.** This is the classic "save before you clobber" pattern in linked list manipulation.

4. **Termination condition → loop while both pointers are non-null.** The moment either is exhausted, the remaining chain from the other is already correctly linked — no extra append step needed if we wire it in during the last splice.

5. **Null inputs → if `list1` is null, return `list2` immediately, and vice versa.** This collapses the edge case before the main loop and keeps the loop invariant clean.

The result feels obvious in hindsight: two pointers, one splice operation per iteration, save-then-rewire.

---

## 🧩 Algorithm Walkthrough

**Pattern: Two Pointers with In-Place Pointer Rewiring**

This is the right abstraction because both lists are traversed linearly and simultaneously, and each step consumes exactly one node from each — a textbook two-pointer advance.

**Steps:**

1. **Guard clause.** If `list1` is `null`, return `list2`. If `list2` is `null`, return `list1`. The merged head is always `list1`'s head when both are non-null (since `list1` goes first).

2. **Initialize pointers.** Set `p1 = list1` and `p2 = list2`. These are the current insertion points in each list.

3. **Loop while both `p1` and `p2` are non-null.** Each iteration splices one `p2` node after `p1`.

4. **Save next pointers.** `p1Next = p1.next`, `p2Next = p2.next`. This is mandatory — we're about to overwrite both `.next` fields.

5. **Rewire.** Set `p1.next = p2` (insert `p2` after `p1`), then `p2.next = p1Next` (reconnect to the rest of `list1`).

6. **Advance both pointers.** `p1 = p1Next`, `p2 = p2Next`. Both move forward by one node in their respective original lists.

7. **Termination.** When the loop exits, either `p1` or `p2` is `null`. Because we wired `p2.next = p1Next` in the last iteration, the remainder of whichever list is longer is already attached. No additional append is needed.

**Invariant maintained:** After each iteration, the merged prefix ending at the current `p2` node is correctly interleaved.

---

## 📊 Worked Example

**Input:** `list1 = [1, 3, 5]`, `list2 = [2, 4, 6]`

| Iteration | p1 | p2 | p1Next | p2Next | List state after rewire |
|---|---|---|---|---|---|
| Start | 1 | 2 | — | — | `1→3→5`, `2→4→6` |
| 1 | 1 | 2 | 3 | 4 | `1→2→3→5`, `4→6` |
| 2 | 3 | 4 | 5 | 6 | `1→2→3→4→5`, `6` |
| 3 | 5 | 6 | null | null | `1→2→3→4→5→6` |
| Exit | null | null | — | — | Loop terminates |

After iteration 3, `p1 = null` and `p2 = null`. The merged list `1→2→3→4→5→6` is fully wired. Head is `list1`'s original head: node `1`.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(min(n, m))** where `n` and `m` are the lengths of `list1` and `list2`. The loop runs exactly `min(n, m)` iterations — one per node consumed from the shorter list. At 10^6 elements this is a single linear pass; at 10^9 it remains tractable but cache locality of the node allocations becomes the dominant performance factor.

### Space Complexity

**O(1).** Only two pointer variables (`p1`, `p2`) and two temporaries (`p1Next`, `p2Next`) are used regardless of input size. No auxiliary structures. Space cannot be reduced further — the four variables are the irreducible minimum for safe in-place rewiring.

---

## 💡 Key Takeaways

- **Pattern signal — "in-place" + "two sequences" + "interleave":** whenever a problem forbids allocation and asks you to merge or interleave two linear structures, two-pointer rewiring is the default starting point.
- **Pattern signal — pointer manipulation problems where you lose nodes:** if a naive implementation could accidentally drop nodes or create cycles, the fix is almost always "save `.next` before overwriting it."
- **Gotcha — pointer advance order matters:** advancing `p1` and `p2` before completing the splice will lose your place in one of the lists; always save both `next` pointers at the top of the loop body before any rewiring.
- **Gotcha — the final append is implicit, not explicit:** a common mistake is adding a post-loop `if p2 != null: p1.next = p2` — this is unnecessary and can corrupt the list if the last splice already wired `p2.next` to the remaining `list1` tail.
- **Architectural insight:** the "save, rewire, advance" micro-pattern is the atomic unit of in-place linked list surgery; it composes directly into more complex operations (list reversal, k-group rotation, merge sort on lists) and is worth internalizing as a reusable primitive rather than re-deriving each time.

---

## 🚀 Variations & Further Practice

- **Merge K Sorted Lists (LeetCode #23):** extends the two-pointer merge to K lists simultaneously; the conceptual twist is that a min-heap (or tournament tree) replaces the simple two-pointer advance, changing the per-step cost from O(1) to O(log K) and making the data structure choice the crux of the problem.
- **Reverse Nodes in K-Group (LeetCode #25):** instead of interleaving one node at a time, you reverse fixed-size segments in-place; the same "save before clobber" discipline applies, but now you must track group boundaries and handle a partial final group, adding a counting invariant on top of the pointer rewiring.
- **Reorder List (LeetCode #143):** combines this interleaving pattern with list splitting and in-place reversal — find the midpoint, reverse the second half, then merge alternately; the difficulty is that all three sub-operations must be composed correctly without any intermediate allocation.