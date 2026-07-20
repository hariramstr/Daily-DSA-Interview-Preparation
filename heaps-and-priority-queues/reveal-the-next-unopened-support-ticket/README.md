# Reveal the Next Unopened Support Ticket

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, hash-set

---

## 🗂 Problem Overview
Given two arrays, `tickets` and `opened`, return the smallest ticket ID that exists in `tickets` but not in `opened`. If no such ticket exists, return `-1`. The challenge is not correctness alone but choosing a structure that lets you efficiently ask for the minimum remaining candidate while also testing membership quickly. That combination naturally points to a min-heap for ordering and a hash set for constant-time exclusion checks.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must repeatedly surface the smallest eligible item from a larger pool while excluding already-processed entries: support queues, job schedulers, retry pipelines, compaction candidates, and event replayers. At scale, the failure mode is usually not wrong ordering but expensive rescans of large candidate sets. A heap plus exclusion set turns “find next valid item” from repeated full scans into incremental selection. That matters when queues are large, workers are concurrent, and eligibility changes independently from insertion order or original storage layout.

## 🔍 Problem Statement
You are given two arrays of distinct positive integers:

- `tickets`: all created ticket IDs
- `opened`: ticket IDs already opened by agents

Every value in `opened` is guaranteed to appear in `tickets`. Return the smallest value from `tickets` that does **not** appear in `opened`. If all tickets are already opened, return `-1`.

Constraints:

- `1 <= tickets.length <= 10^5`
- `0 <= opened.length <= tickets.length`
- `1 <= tickets[i], opened[i] <= 10^9`
- All values in `tickets` are distinct
- All values in `opened` are distinct

Examples:

- `tickets = [42, 17, 90, 23]`, `opened = [17, 42]` → `23`
- `tickets = [8, 3, 11]`, `opened = [3, 8, 11]` → `-1`

The key constraint is the need to return the **smallest unopened** ID efficiently, which drives the choice of an ordered structure rather than a plain set alone.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need all unopened tickets, only the smallest one.

2. That immediately suggests we need fast access to the current minimum candidate → think **min-heap / priority queue**.

3. We also need to know whether a candidate has already been opened → think **hash set** for `O(1)` membership checks.

4. Build a set from `opened` so we can instantly reject any ticket that is already processed.

5. Push all values from `tickets` into a min-heap. Now the heap gives candidates in ascending order, regardless of input order.

6. Repeatedly pop the smallest ticket:
   - if it is in `openedSet`, discard it
   - otherwise, it is the smallest unopened ticket, so return it immediately

7. If the heap becomes empty, every ticket was opened, so return `-1`.

This approach is easy to reason about because it separates concerns cleanly: the heap handles ordering, and the set handles exclusion.

## 🧩 Algorithm Walkthrough
**Pattern:** **Min-Heap + Hash Set**

This is the right abstraction because the problem combines two operations with different access patterns: “give me the smallest remaining ID” and “tell me whether this ID is already opened.” No single primitive handles both efficiently; the heap and set complement each other.

1. **Create a hash set from `opened`.**  
   This gives constant-time membership checks. The invariant is: `openedSet` always represents exactly the IDs that must be skipped.

2. **Insert every ticket ID into a min-heap.**  
   The heap maintains the ordering invariant: the root is always the smallest unprocessed ticket among those still in the heap.

3. **Pop the heap while it is non-empty.**  
   Each pop yields the smallest candidate not yet examined. This guarantees we inspect ticket IDs in strictly increasing order.

4. **Check whether the popped ID is in `openedSet`.**  
   - If yes, discard it and continue.  
   - If no, return it immediately.  
   This is correct because any smaller ticket would already have been popped earlier. Therefore the first candidate not in `openedSet` is the smallest unopened ticket.

5. **If the heap is exhausted, return `-1`.**  
   At that point every ticket from `tickets` was found in `openedSet`, so no unopened ticket exists.

This solution is straightforward, deterministic, and aligns directly with the problem’s intended heap-oriented framing.

## 📊 Worked Example
Take `tickets = [42, 17, 90, 23]` and `opened = [17, 42]`.

| Step | Heap top / popped | `openedSet` | Action | Result |
|---|---:|---|---|---|
| 1 | build heap from all tickets | `{17, 42}` | min-heap contains `17, 23, 42, 90` | — |
| 2 | pop `17` | `{17, 42}` | already opened, discard | — |
| 3 | pop `23` | `{17, 42}` | not opened, return immediately | `23` |

Trace reasoning:

1. The heap ensures we inspect IDs in sorted order without sorting the original array explicitly.
2. `17` is the smallest ticket, but it is already in `openedSet`, so it cannot be the answer.
3. The next smallest is `23`, and it is not opened.
4. Because all smaller IDs have already been checked, `23` is provably the smallest unopened ticket.

## ⏱ Complexity Analysis

### Time Complexity
Building the `opened` set takes `O(m)`, and inserting all `n` tickets into the heap takes `O(n log n)` if pushed one by one. In the worst case, we may pop all `n` elements, adding another `O(n log n)`. Overall: `O(n log n + m)`. At million-scale inputs this is still practical; at billion-scale, heap-based in-memory processing becomes the bottleneck.

### Space Complexity
The heap stores up to `n` ticket IDs and the set stores up to `m` opened IDs, so total auxiliary space is `O(n + m)`. This can be reduced by sorting `tickets` in place and scanning, but that trades heap semantics for mutation or extra sort cost.

## 💡 Key Takeaways
- If a problem asks for the **smallest valid remaining item** while validity is checked separately, think **min-heap + hash set**.
- When input order is irrelevant but repeated minimum extraction matters, a priority queue is usually a stronger fit than repeated scans.
- Do not return the first ticket not in `opened` from the original array; the answer depends on numeric minimum, not insertion order.
- Handle the all-opened case explicitly: if every heap element is discarded, the correct result is `-1`, not an uninitialized value.
- In production systems, separating **ordering** from **eligibility filtering** often yields simpler and more scalable queue-processing designs.

## 🚀 Variations & Further Practice
- Return the **k-th smallest unopened** ticket instead of the first; same pattern, but you must continue popping valid candidates and count only those not in the exclusion set.
- Support **online updates** where tickets and opened IDs arrive continuously; this introduces synchronization, stale heap entries, and lazy deletion concerns.
- Find the next unopened ticket across **multiple shards or queues**; the harder twist is merging local minima while preserving global ordering and exclusion correctness.