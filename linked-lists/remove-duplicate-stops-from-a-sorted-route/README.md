# Remove Duplicate Stops from a Sorted Route

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked List, Two Pointers, In-Place

---

## 🗂 Problem Overview
You are given the head of a sorted singly linked list and must remove duplicate nodes in place so each value appears exactly once. The function returns the original head of the deduplicated list, or `null` for an empty list. The non-trivial constraint is that the solution must use `O(1)` extra space, which rules out auxiliary sets and forces direct pointer rewiring while preserving the first occurrence of each value.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered records must be compacted without reallocating full structures: log ingestion pipelines collapsing repeated event IDs, search indexing systems deduplicating sorted posting lists, storage engines compacting sorted key runs, and telemetry processors removing adjacent duplicate samples after merge steps. At scale, the distinction matters: rebuilding structures wastes memory bandwidth, increases GC pressure, and breaks streaming behavior. In-place compaction over sorted input enables single-pass cleanup, predictable memory use, and better cache behavior, which is exactly what production data-path code needs under sustained throughput.

## 🔍 Problem Statement
Given the head of a singly linked list whose node values are sorted in non-decreasing order, remove duplicate nodes so that each distinct value appears once. Keep the first node in each run of equal values and bypass the rest by updating `next` pointers in place. Return the head of the modified list.

Constraints:
- Number of nodes: `[0, 300]`
- `-1000 <= Node.val <= 1000`
- List is sorted in non-decreasing order
- Extra space must be `O(1)`

Edge cases matter:
- Empty list → return `null`
- Single node → return unchanged
- All nodes identical → collapse to one node
- No duplicates → return original structure unchanged

Examples:
- `head = [4,4,7,7,7,9,12,12]` → `[4,7,9,12]`
- `head = [1,2,2,3,5,5,8]` → `[1,2,3,5,8]`

The sorted-input constraint drives the algorithm: duplicates are adjacent, so one linear pass is enough.

## 🪜 How to Solve This
1. Read the problem → notice the list is already sorted. That immediately changes the search space: duplicates cannot appear in arbitrary positions, only in contiguous runs.

2. Since duplicates are adjacent, you do not need a hash set or any global memory of previously seen values. You only need to compare the current node with the next node.

3. Think in terms of list compaction, not deletion. The goal is to preserve one representative per value and skip the rest by rewiring `next`.

4. Start from the head and walk forward with one moving pointer. At each node:
   - If `current.val == current.next.val`, the next node is redundant, so point `current.next` to `current.next.next`.
   - Otherwise, advance `current`.

5. Why not always advance? Because after removing one duplicate, the new `current.next` might still hold the same value. Staying on the same node lets you collapse an entire run.

6. This gives a single-pass, in-place solution with constant extra space. The sorted property is doing all the heavy lifting.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Two Pointers / in-place linked-list compaction.**  
   This is not a general deduplication problem. Because the list is sorted, local comparison is sufficient. The right abstraction is a traversal pointer that rewires links when it detects adjacent duplicates.

2. **Handle trivial structure implicitly.**  
   If `head` is `null`, there is nothing to process. If the list has one node, the loop simply never executes and the node is returned unchanged. No special branching is required beyond safe loop conditions.

3. **Initialize `current = head`.**  
   `current` always points to the last node kept in the cleaned prefix. That prefix is guaranteed to contain no duplicates.

4. **Traverse while `current` and `current.next` exist.**  
   The comparison target is `current.next`, so the loop condition must guarantee it is valid.

5. **If `current.val == current.next.val`, remove the duplicate.**  
   Set `current.next = current.next.next`. This skips exactly one redundant node.  
   **Why correct:** the list is sorted, so equal adjacent values belong to the same duplicate run. Keeping the first and bypassing later ones preserves the required output.

6. **If values differ, advance `current = current.next`.**  
   This extends the cleaned prefix by one distinct value.  
   **Invariant maintained:** all nodes from `head` through `current` are deduplicated, in original sorted order.

7. **Return `head`.**  
   The head never changes because the first occurrence of the first value is always retained.

## 📊 Worked Example
Example: `head = [4,4,7,7,7,9,12,12]`

| Step | `current.val` | `current.next.val` | Action | List after step |
|---|---:|---:|---|---|
| 1 | 4 | 4 | duplicate → skip next | `[4,7,7,7,9,12,12]` |
| 2 | 4 | 7 | distinct → advance | `[4,7,7,7,9,12,12]` |
| 3 | 7 | 7 | duplicate → skip next | `[4,7,7,9,12,12]` |
| 4 | 7 | 7 | duplicate → skip next | `[4,7,9,12,12]` |
| 5 | 7 | 9 | distinct → advance | `[4,7,9,12,12]` |
| 6 | 9 | 12 | distinct → advance | `[4,7,9,12,12]` |
| 7 | 12 | 12 | duplicate → skip next | `[4,7,9,12]` |

Stop when `current.next` is `null`. Final result: `[4,7,9,12]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of nodes. Each comparison examines the current node and its successor, and each duplicate node is removed at most once. At `10^6` nodes this remains a straightforward linear scan; at `10^9`, runtime is dominated by pointer traversal and memory access latency, not algorithmic overhead.

### Space Complexity
`O(1)` extra space. The algorithm uses a single traversal pointer and rewires the existing list in place. There is no auxiliary structure owning proportional memory. You could copy into a new list, but that would increase space to `O(n)` with no algorithmic benefit here.

## 💡 Key Takeaways
- Sorted linked list + “remove duplicates” is a strong signal that adjacent comparison is enough; you should not reach for a hash set first.
- If the requirement says “in place” and “keep first occurrence,” think list compaction by pointer rewiring rather than node deletion bookkeeping.
- The main trap is advancing after removing a duplicate; doing so can leave later duplicates in the same run unprocessed.
- The loop condition must protect `current.next` access; `while current and current.next` is the safe boundary.
- In production data paths, sorted input often converts global-state problems into local linear compaction, which reduces memory pressure and simplifies streaming execution.

## 🚀 Variations & Further Practice
- **Remove all duplicates from a sorted list**: instead of keeping one copy, remove every value that appears more than once. The twist is that duplicate runs must be deleted entirely, which often requires a dummy head.
- **Remove duplicates from an unsorted linked list**: adjacency no longer helps, so you need a hash set for `O(n)` time or accept `O(n^2)` time for constant extra space.
- **Merge two sorted linked lists, then deduplicate**: combines stable merge logic with in-place compaction, forcing you to reason about pointer ownership across two phases.