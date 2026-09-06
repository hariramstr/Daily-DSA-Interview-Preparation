# Merge Release Streams by Highest Current Severity

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, k-way-merge

---

## 🗂 Problem Overview
You are given `k` release streams, each already sorted in non-increasing severity order, and must produce one global list in the same order. At any point, only the current head of each stream is eligible. Ties are resolved by smaller stream index, then earlier position within that stream. The challenge is scale: with up to `10^5` streams, repeatedly scanning all heads to find the next maximum is too slow.

## 🌍 Engineering Impact
This is the same shape as merging ordered inputs in log aggregation, search result federation, event replay, distributed schedulers, and multi-shard feed assembly. Each source is locally ordered, but the platform needs a globally ordered view without re-sorting everything from scratch. At small scale, a linear scan over sources is acceptable; at shard counts in the thousands, it becomes the bottleneck. A heap-based k-way merge turns “check every source each time” into “track only current winners,” which is exactly what makes fan-in pipelines, ranking services, and incident-management backends predictable under load.

## 🔍 Problem Statement
Given `streams: number[][]`, where each `streams[i]` is sorted in non-increasing order, return a single array containing all severity scores in non-increasing order.

Rules:
- At each step, you may take only the next unprocessed element from a stream.
- If multiple available elements have the same severity, choose the one from the smaller stream index.
- If severity and stream index are both the same, the earlier position in that stream comes first.

Constraints:
- `1 <= streams.length <= 10^5`
- `0 <= streams[i].length <= 10^5`
- `0 <= severity <= 10^9`
- Total number of reports across all streams `<= 2 * 10^5`

Examples:
- `[[9,7,3],[10,6],[8,8,1]] -> [10,9,8,8,7,6,3,1]`
- `[[5,5,2],[],[5,4],[6]] -> [6,5,5,5,4,2]`

The decisive constraint is large `k`: scanning every stream head on every output element would degrade to `O(Nk)`.

## 🪜 How to Solve This
1. Read the problem → each stream is already sorted, so the next global answer must come from one of the current heads.

2. That means we do **not** need to compare against every remaining element, only against at most one candidate per stream.

3. If we repeatedly need the “largest current candidate,” that is a priority queue / heap signal immediately.

4. Initialize the heap with the first element from every non-empty stream. Each heap entry must carry:
   - severity
   - stream index
   - position within that stream

5. Pop the best candidate:
   - highest severity first
   - for equal severity, smaller stream index first
   - position is naturally preserved because only one item per stream is active at a time

6. After popping from stream `i` at position `j`, push `streams[i][j+1]` if it exists. That restores the invariant that the heap contains the next available item from every still-active stream.

7. Repeat until the heap is empty. This gives `O(N log k)` instead of `O(Nk)`, which is the difference between viable and non-viable when `k` is large.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: k-way merge with a priority queue.**  
   Each input stream is already ordered. The problem is not general sorting; it is merging multiple sorted sequences while preserving a global order. A heap is the right abstraction because we repeatedly need the best among current frontiers.

2. **Seed the heap with one candidate per non-empty stream.**  
   For each stream `i`, push `(severity=streams[i][0], stream=i, index=0)`.  
   This is correct because only the first unprocessed item in each stream is eligible at the start.

3. **Define heap ordering carefully.**  
   Since we want non-increasing severity, the heap must return the largest severity first. For ties, smaller `stream` wins. The in-stream position tie-breaker is already enforced by the “one active item per stream” rule: you cannot expose `index=1` until `index=0` has been consumed.

4. **Pop the top entry and append its severity to the result.**  
   This is globally correct because the heap contains every currently legal candidate, and the comparator exactly matches the problem’s ordering rule.

5. **Advance only the stream you popped from.**  
   If the popped item came from `(stream=i, index=j)`, push `(streams[i][j+1], i, j+1)` when it exists.  
   This maintains the invariant: the heap always contains exactly the next unprocessed item from each stream that still has remaining elements.

6. **Continue until exhaustion.**  
   When the heap is empty, every stream has been fully consumed, so the merged output is complete.

The key invariant is: **the heap represents the current frontier across all streams**. That invariant is what makes each greedy choice safe.

## 📊 Worked Example
Use `streams = [[9,7,3],[10,6],[8,8,1]]`.

| Step | Heap Top Chosen | Result So Far | Next Pushed |
|---|---|---|---|
| Init | `10@s1`, `9@s0`, `8@s2` | `[]` | — |
| 1 | `10@s1` | `[10]` | push `6@s1` |
| 2 | `9@s0` | `[10,9]` | push `7@s0` |
| 3 | `8@s2` | `[10,9,8]` | push `8@s2` |
| 4 | `8@s2` | `[10,9,8,8]` | push `1@s2` |
| 5 | `7@s0` | `[10,9,8,8,7]` | push `3@s0` |
| 6 | `6@s1` | `[10,9,8,8,7,6]` | none |
| 7 | `3@s0` | `[10,9,8,8,7,6,3]` | none |
| 8 | `1@s2` | `[10,9,8,8,7,6,3,1]` | none |

At every step, the heap contains exactly one available candidate per active stream, so the chosen value is always the correct next output.

## ⏱ Complexity Analysis
### Time Complexity
`O(N log k)`, where `N` is the total number of reports and `k` is the number of streams. Each report is pushed and popped from the heap at most once, and each heap operation costs `O(log k)`. At million-scale inputs this remains practical; `O(Nk)` does not. At billion-scale, even optimal asymptotics become constrained by memory and I/O.

### Space Complexity
`O(k)` auxiliary space for the heap, plus `O(N)` for the output array. The heap owns the working-set overhead because it stores one frontier item per active stream. You cannot reduce output space unless streaming results incrementally instead of materializing the merged array.

## 💡 Key Takeaways
- If multiple inputs are already individually sorted and you need one global ordering, think **k-way merge**, not full re-sort.
- If the next answer always comes from the current head of one source, that is a strong **priority queue / heap** signal.
- Comparator bugs are the main failure mode: severity must sort descending, but stream index must sort ascending on ties.
- Do not push an entire stream into the heap; push only the current frontier element, then advance lazily after each pop.
- In production fan-in systems, maintaining a small frontier over ordered sources is the scalable alternative to repeated global scans.

## 🚀 Variations & Further Practice
- Merge streams where reports arrive online over time; the twist is maintaining global order under partial visibility and backpressure.
- Return the top `m` reports only; the twist is early termination and avoiding unnecessary advancement of low-value streams.
- Merge by composite ranking such as `(severity, timestamp, source-priority)`; the twist is designing a stable comparator that matches business semantics exactly.