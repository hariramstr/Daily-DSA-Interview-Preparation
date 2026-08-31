# Merge Sensor Streams by Freshest Reading

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, k-way-merge

---

## 🗂 Problem Overview
You are given `k` timestamp-sorted sensor streams and must emit one merged sequence of readings. Each output item is `[timestamp, value, streamIndex]`, chosen by repeatedly selecting the unread reading with the smallest timestamp; ties are broken by larger value, then smaller stream index. The challenge is scale: with up to `10^4` streams and `2 * 10^5` total readings, repeatedly scanning all stream heads is too expensive. The right solution maintains only the current candidate from each stream.

## 🌍 Engineering Impact
This is the same shape as merging ordered event logs, CDC feeds, telemetry shards, Kafka partitions, search result streams, or distributed scheduler queues. In production, the issue is not correctness alone but deterministic ordering under tie conditions and predictable cost as shard count grows. A naive implementation that rescans every source on each emission turns fan-in into a latency amplifier. A heap-based frontier keeps merge cost proportional to active sources, enabling scalable ingest, replay, and downstream processing while preserving a stable, auditable ordering contract across independently produced streams.

## 🔍 Problem Statement
Given `k` streams, where each stream is a list of `[timestamp, value]` pairs sorted by nondecreasing timestamp, return a merged list of `[timestamp, value, streamIndex]`. At each step, select the unread reading with the smallest timestamp. If timestamps tie, select the larger value first. If both timestamp and value tie, select the smaller stream index first. Some streams may be empty.

Constraints:
- `1 <= k <= 10^4`
- `0 <= total readings <= 2 * 10^5`
- `0 <= timestamp <= 10^9`
- `-10^9 <= value <= 10^9`

Example 1:
`[[[1,5],[4,2]], [[1,7],[3,1]], [[2,9]]]`
→ `[[1,7,1],[1,5,0],[2,9,2],[3,1,1],[4,2,0]]`

Example 2:
`[[], [[2,4],[2,3],[5,8]], [[2,4]], [[1,10],[6,0]]]`
→ `[[1,10,3],[2,4,1],[2,4,2],[2,3,1],[5,8,1],[6,0,3]]`

The key constraint is large `k`: it rules out repeated linear scans across stream heads.

## 🪜 How to Solve This
1. Read the selection rule carefully → this is not a global sort over all readings from scratch; each stream is already sorted, so we should exploit that structure.

2. Notice what is actually “available” at any moment → only the next unread reading from each stream can possibly be chosen. Everything behind it is blocked by stream order.

3. That means the decision frontier has size at most `k` → one candidate per stream, not all `n` readings.

4. We need to repeatedly extract the best candidate under a custom ordering:
   - smaller timestamp first
   - for equal timestamp, larger value first
   - for equal timestamp and value, smaller stream index first

5. Repeated “get best, then replace from same source” is the classic k-way merge pattern → use a priority queue keyed by that ordering.

6. Initialize the heap with the first reading from every non-empty stream.

7. Pop the best reading, append it to the result, then push the next reading from that same stream.

8. Continue until the heap is empty. This avoids rescanning all streams and gives `O(n log k)` instead of `O(nk)`.

## 🧩 Algorithm Walkthrough
1. **Model the problem as a k-way merge.**  
   The pattern is **Heap / Priority Queue-based k-way merge**. Each stream is already sorted by timestamp, so the only relevant unread element from a stream is its current head. This reduces the candidate set from all unread readings to at most one per stream.

2. **Define the heap key to match the exact ordering rule.**  
   Store entries like `(timestamp, -value, streamIndex, positionInStream)`.  
   Why: min-heap semantics naturally give smallest timestamp first; negating value makes larger values rank earlier; stream index resolves final ties deterministically.

3. **Seed the heap with the first reading from each non-empty stream.**  
   This establishes the invariant: the heap contains exactly the next unread reading for every stream that still has data.

4. **Pop the top heap entry and append it to the output.**  
   This is correct because the heap order encodes the global selection rule across all currently eligible readings.

5. **Advance only the stream that produced the popped reading.**  
   Push its next reading, if one exists.  
   Why: within a stream, later readings cannot become eligible until earlier ones are emitted.

6. **Maintain the invariant after every pop/push.**  
   The heap always represents the current frontier of unread stream heads. Therefore, the next pop is always the correct next output.

7. **Stop when the heap is empty.**  
   At that point, every reading from every stream has been emitted exactly once, in deterministic priority order.

## 📊 Worked Example
Using Example 1:

Streams:
- `0: [[1,5],[4,2]]`
- `1: [[1,7],[3,1]]`
- `2: [[2,9]]`

Heap key is `(timestamp, -value, streamIndex, pos)`.

| Step | Heap Top Chosen | Output So Far | Next Pushed |
|---|---|---|---|
| Init | `(1,-7,1,0)` among heads | `[]` | — |
| 1 | `[1,7,1]` | `[[1,7,1]]` | stream 1 pos 1 → `[3,1]` |
| 2 | `[1,5,0]` | `[[1,7,1],[1,5,0]]` | stream 0 pos 1 → `[4,2]` |
| 3 | `[2,9,2]` | `...,[2,9,2]` | none |
| 4 | `[3,1,1]` | `...,[3,1,1]` | none |
| 5 | `[4,2,0]` | `...,[4,2,0]` | none |

At step 1, stream 1 beats stream 0 because both timestamps are `1`, but value `7` is larger than `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log k)`, where `n` is the total number of readings. Each reading is pushed into and popped from the heap once, and each heap operation costs `O(log k)` because the heap holds at most one active reading per stream. At million-scale inputs this remains practical; at billion-scale, the bottleneck becomes I/O and memory bandwidth, not comparison count alone.

### Space Complexity
`O(k)` auxiliary space for the heap, plus `O(n)` for the output required by the problem. The heap owns the working set. Auxiliary space cannot be reduced below `O(k)` without giving up efficient frontier selection and falling back to repeated scans.

## 💡 Key Takeaways
- If multiple sorted sources must be merged while repeatedly choosing the current global minimum, think **k-way merge with a heap**, not repeated linear search.
- If tie-breaking is deterministic and multi-field, encode the full ordering directly into the heap key instead of patching behavior after extraction.
- Be careful with the value tie-break: the problem wants **larger** value first, which means using `-value` in a min-heap.
- Do not push all readings into the heap upfront; only the current head of each stream belongs there, or you lose the `O(n log k)` advantage.
- The production lesson is to maintain a bounded frontier over preordered shards rather than re-evaluating entire sources on every scheduling decision.

## 🚀 Variations & Further Practice
- Merge streams where each source can arrive incrementally over time rather than being fully materialized upfront; the harder part is preserving ordering while handling backpressure and incomplete inputs.
- Return only the top `m` merged readings under the same ordering; the twist is early stopping without consuming every stream.
- Add deduplication by `(timestamp, value)` across streams during merge; the harder part is combining heap-based ordering with cross-stream state tracking.