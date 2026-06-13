# Reverse Nodes in Even-Length ID Groups

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked List, Two Pointers, In-Place Reversal

---

## 🗂 Problem Overview
Given the head of a singly linked list, partition nodes into consecutive groups of intended sizes 1, 2, 3, 4, ... until the list ends. For each group, compute its actual length; if that length is even, reverse the nodes in place, otherwise leave them unchanged. Return the modified head. The challenge is pointer surgery under strict constraints: linear time, constant extra space, and no rebuilding from copied values.

## 🌍 Engineering Impact
This pattern shows up anywhere records are processed in variable-sized batches over pointer-based structures: streaming pipelines with chunked transforms, log compaction chains, memory allocators managing free-list segments, and storage engines rewriting linked extents. At scale, the distinction between mutating links in place versus materializing a second structure is not cosmetic; it determines cache behavior, memory pressure, and tail latency. The broader lesson is operational: when data arrives as a chain and grouping is positional, you need local rewiring with bounded state, not value extraction plus reconstruction.

## 🔍 Problem Statement
You are given a singly linked list with `n` nodes, where `1 <= n <= 100000`. Starting at the head, split the list into consecutive groups whose intended sizes are `1, 2, 3, 4, ...`. The final group may be shorter if the list runs out.

For each group, use its **actual** length, not its intended length. If that actual length is even, reverse the nodes in that group. If it is odd, keep the group unchanged. Groups must stay in the same overall sequence; only internal node order may change.

Examples:

- `head = [5,8,3,9,1,4]`
  - Groups: `[5]`, `[8,3]`, `[9,1,4]`
  - Output: `[5,3,8,9,1,4]`

- `head = [1,2,3,4,5,6,7,8,9]`
  - Groups: `[1]`, `[2,3]`, `[4,5,6]`, `[7,8,9]`
  - Output: `[1,3,2,4,5,6,9,8,7]`

The key constraint is `O(n)` time with `O(1)` extra space, which rules out collecting groups into arrays and rebuilding.

## 🪜 How to Solve This
1. Read the problem → the list is not being reordered globally; it is being processed in **contiguous positional groups**.
2. Group sizes are deterministic: first 1 node, then 2, then 3, and so on. So we can walk once while tracking the current target group size.
3. For each group, we first need its **actual** length, because the tail group may be shorter than intended. That means probing ahead up to `k` nodes, not assuming `k` exists.
4. Once we know the actual length:
   - odd length → do nothing, just advance pointers
   - even length → reverse exactly that segment in place
5. Reversal in a singly linked list is a local pointer operation. The only hard part is reconnecting:
   - node before the group
   - new head of the reversed group
   - tail of the reversed group to the next segment
6. This naturally suggests a dummy head plus two-pointer traversal:
   - `prevGroupTail` anchors the already-processed prefix
   - `groupHead` starts the current segment
7. Every node is visited a constant number of times, so the approach stays linear and in-place.

## 🧩 Algorithm Walkthrough
1. **Initialize a dummy node and traversal pointers.**  
   Use a dummy before `head` so the first real group is handled the same as later groups. Maintain `prevGroupTail`, the tail of the fully processed prefix. This invariant simplifies reconnection after optional reversal.

2. **Track the intended group size `k`, starting at 1.**  
   For each iteration, let `groupHead = prevGroupTail.next`. This is the first node of the next unprocessed group.

3. **Measure the actual group length.**  
   Walk forward from `groupHead` up to `k` nodes or until `null`. Count how many nodes exist and keep a pointer to `nextGroupHead`, the node after the group. This is the critical correctness step: the last group may be shorter than `k`, and parity must use actual length.

4. **If the actual length is odd, skip reversal.**  
   Advance `prevGroupTail` to the last node of this group. The invariant remains: everything before `prevGroupTail` is finalized and correctly linked.

5. **If the actual length is even, reverse exactly that segment.**  
   Apply the standard **in-place linked-list reversal** pattern over `length` nodes, stopping at `nextGroupHead`. After reversal:
   - the old `groupHead` becomes the tail
   - the last node in the segment becomes the new head  
   Reconnect `prevGroupTail.next` to the new head and connect the new tail to `nextGroupHead`.

6. **Advance to the next group and increment `k`.**  
   After either branch, move `prevGroupTail` to the tail of the processed group, increment `k`, and continue until no nodes remain.

This is a **Two Pointers + In-Place Reversal** problem: one pointer anchors processed structure, the other scans and optionally rewires a bounded segment.

## 📊 Worked Example
Example: `head = [5,8,3,9,1,4]`

| Step | Intended Size | Actual Group | Even? | Action | List After Step |
|---|---:|---|---|---|---|
| 1 | 1 | `[5]` | No | Keep as is | `[5,8,3,9,1,4]` |
| 2 | 2 | `[8,3]` | Yes | Reverse to `[3,8]` | `[5,3,8,9,1,4]` |
| 3 | 3 | `[9,1,4]` | No | Keep as is | `[5,3,8,9,1,4]` |

Trace:
1. Start with dummy → `prevGroupTail` points before `5`.
2. Count 1 node: group `[5]`, odd, so just move `prevGroupTail` to `5`.
3. Count next 2 nodes: `[8,3]`, even, reverse links inside that segment, reconnect `5 -> 3 -> 8`.
4. Count next 3 nodes: `[9,1,4]`, odd, leave unchanged.
5. End of list reached; return `dummy.next`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is counted once as part of group sizing and participates in at most one reversal, with only constant-time pointer updates around it. At `10^6` nodes this is operationally routine; at `10^9`, the bottleneck is traversal and memory locality, not asymptotic overhead.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores a fixed number of pointers and counters; no arrays, stacks, or copied node buffers are required. You could express reversal recursively, but that would trade clarity for `O(group_size)` stack usage and violate the intended space bound.

## 💡 Key Takeaways
• If a linked-list problem says “process contiguous groups of sizes 1, 2, 3...” and mutate only some groups, think sequential segmentation plus local pointer rewiring.  
• If the decision depends on the group’s realized size rather than a declared size, expect a probe/count phase before mutation.  
• The last group is the trap: parity must use actual remaining nodes, not the intended group size.  
• After reversing a group, the original group head becomes the new tail; forgetting to reconnect it to the next segment is the classic bug.  
• In production code, in-place segment transforms are the right tool when throughput and memory ceilings matter more than value-level convenience.

## 🚀 Variations & Further Practice
- Reverse nodes in groups of `k`: same in-place reversal core, but fixed-size segmentation removes the “actual length vs intended length” wrinkle.
- Reverse odd-length groups instead: identical traversal structure, but parity condition flips; useful for validating that the implementation cleanly separates grouping from mutation.
- Apply a predicate-based transform per group, such as reversing only if the group sum is even: harder because the decision depends on aggregate computation plus safe segment rewiring without extra storage.