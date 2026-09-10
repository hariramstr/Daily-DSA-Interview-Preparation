# Track Top K Hashtags in a Sliding Event Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, sliding-window

---

## 🗂 Problem Overview
Process a timestamp-ordered stream of hashtag events and, after each event, return the current top `k` hashtags seen within the last `windowSize` seconds. The active window is inclusive: `[currentTime - windowSize + 1, currentTime]`. Ranking is by descending frequency, then lexicographically ascending hashtag. The challenge is dynamic expiration: counts rise when events arrive and fall when old events leave, so a one-time sort or full recomputation per step is too expensive for large streams.

## 🌍 Engineering Impact
This pattern shows up in real-time analytics, trending-topic detection, ad click aggregation, abuse detection, rate-limiter hot-key tracking, and observability systems computing top error signatures over rolling windows. The hard part is not counting; it is maintaining a ranked view while data both enters and expires. Without an incremental design, systems degrade into repeated full scans, blowing latency budgets and cache locality as cardinality grows. A queue-plus-heap design enables bounded per-event work, predictable streaming behavior, and a clean separation between event-time eviction, aggregate state, and ranked retrieval under sustained throughput.

## 🔍 Problem Statement
You are given two arrays of length `n`: `timestamps` and `hashtags`, where `timestamps` is sorted in non-decreasing order and each event contributes exactly one hashtag at its timestamp. After processing each event, return the top `k` distinct hashtags whose events fall inside the inclusive window `[currentTime - windowSize + 1, currentTime]`.

Ranking rules:
- Higher frequency ranks first.
- If frequencies tie, lexicographically smaller hashtag ranks first.

If fewer than `k` distinct hashtags are active, return all of them in ranked order.

Constraints:
- `1 <= n <= 100000`
- `1 <= windowSize <= 10^9`
- `1 <= k <= 100`
- `1 <= timestamps[i] <= 10^9`
- `timestamps` is sorted
- hashtags are lowercase strings of length `1..20`

Example:
- `timestamps = [1,2,3,6,7]`
- `hashtags = ["ai","ml","ai","db","ai"]`
- `windowSize = 5, k = 2`
- Output: `[["ai"],["ai","ml"],["ai","ml"],["ai","db"],["ai","db"]]`

The key algorithmic constraint is that counts must support both increments and decrements efficiently as the window slides.

## 🪜 How to Solve This
1. Read the problem → this is not static top-`k`; rankings change after every event because old events expire.
2. Expiration depends on time order → since timestamps are already sorted, use a FIFO queue to hold active events and evict from the front when they fall before `currentTime - windowSize + 1`.
3. We need current counts per hashtag → use a hash map: `count[tag]`.
4. We also need fast access to highest-ranked hashtags → think heap, ordered by `(-frequency, hashtag)`.
5. But heap entries become stale whenever a hashtag count changes, and counts change in both directions → use lazy deletion instead of trying to update heap entries in place.
6. For every increment or decrement, push a fresh snapshot `(currentCount, tag)` into the heap.
7. When reading top results, repeatedly discard heap entries whose stored count no longer matches `count[tag]`.
8. Because we need the top `k` after every event, pop valid entries temporarily, record them, then push them back so the heap remains usable for the next step.

That chain leads naturally to: queue for window membership, hash map for exact counts, heap for ranking, lazy deletion for stale state.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window pattern over event time.**  
   Maintain a queue of active events `(timestamp, hashtag)`. For each incoming event at time `t`, the valid window start is `t - windowSize + 1`. Evict from the queue while `front.timestamp < windowStart`. This is correct because timestamps are non-decreasing, so once an event expires it never becomes valid again.

2. **Maintain exact frequencies with a hash map.**  
   When an event enters, increment `count[tag]`. When an event expires, decrement `count[tag]`, and remove the key if the count reaches zero. The invariant is: after eviction and insertion, `count` exactly matches the multiset of hashtags currently in the queue.

3. **Maintain ranking candidates with a max-heap via lazy deletion.**  
   Push a snapshot whenever a tag’s count changes: conceptually `(-count[tag], tag)`. This avoids expensive in-place heap updates. Some entries become stale later; that is expected.

4. **Validate heap entries on access.**  
   Before using the heap top, compare the snapshot count against `count[tag]`. If they differ, discard the entry and continue. If `count[tag]` is zero or missing, it is also stale. The invariant is: every accepted heap entry reflects the current active-window count.

5. **Extract the current top `k`.**  
   Pop valid entries until you collect `k` hashtags or the heap is exhausted. Store popped valid entries temporarily, append their tags to the answer for this step, then push them back. This preserves heap state while producing a ranked snapshot.

6. **Why this abstraction fits.**  
   This is a **Sliding Window + Heap with Lazy Deletion** problem. The queue solves time-based eviction, the map solves mutable aggregation, and the heap solves repeated top-`k` retrieval without rescanning all distinct hashtags each step.

## 📊 Worked Example
Example: `timestamps=[4,4,5,8,8,9]`, `hashtags=["red","blue","red","green","blue","blue"]`, `windowSize=3`, `k=3`

| Step | Event | Active Window | Queue After Eviction+Insert | Counts | Top k |
|---|---|---|---|---|---|
| 1 | (4, red) | [2..4] | [(4, red)] | red=1 | [red] |
| 2 | (4, blue) | [2..4] | [(4, red), (4, blue)] | red=1, blue=1 | [blue, red] |
| 3 | (5, red) | [3..5] | [(4, red), (4, blue), (5, red)] | red=2, blue=1 | [red, blue] |
| 4 | (8, green) | [6..8] | [(8, green)] | green=1 | [green] |
| 5 | (8, blue) | [6..8] | [(8, green), (8, blue)] | green=1, blue=1 | [blue, green] |
| 6 | (9, blue) | [7..9] | [(8, green), (8, blue), (9, blue)] | blue=2, green=1 | [blue, green] |

The important transition is step 4: when time reaches `8`, every event at `4` and `5` expires because the new valid range starts at `6`.

## ⏱ Complexity Analysis
### Time Complexity
Each event is inserted into the queue once and evicted once, so window maintenance is `O(n)`. Each count change pushes one heap snapshot, and stale entries are discarded at most once, giving amortized `O(n log n)` heap work overall. Per-step top-`k` extraction adds roughly `O(k log n)`, acceptable for `k <= 100` even when streams reach millions of events.

### Space Complexity
`O(n)` in the worst case: the queue may hold all events in a large window, the count map holds distinct active hashtags, and the heap accumulates current plus stale snapshots. You can reduce heap growth only by using a more complex indexed heap or balanced tree with explicit updates.

## 💡 Key Takeaways
- If the prompt says “top `k` over the last X units” and data arrives in sorted time order, think sliding window plus incremental state, not repeated recomputation.
- If ranking depends on mutable counts that can both increase and decrease, a heap with lazy deletion is usually simpler than trying to update heap nodes in place.
- The window is inclusive: valid timestamps are `>= currentTime - windowSize + 1`; using `currentTime - windowSize` is the classic off-by-one bug.
- When extracting top results, do not permanently remove valid heap entries unless you rebuild ranking state; pop temporarily, record, then restore.
- In production streaming systems, separating membership (queue), truth state (map), and ranking view (heap) keeps correctness local and operational behavior predictable.

## 🚀 Variations & Further Practice
- Return top `k` for arbitrary query times, not just after each event. Twist: queries may arrive between events, so you need independent time advancement and eviction.
- Track top `k` over multiple concurrent window sizes. Twist: one stream now feeds several rolling aggregations, forcing careful state sharing or per-window isolation.
- Support event deletions or out-of-order timestamps. Twist: FIFO eviction no longer works, so you need indexed structures, event-time buffering, or balanced trees instead of a simple queue.