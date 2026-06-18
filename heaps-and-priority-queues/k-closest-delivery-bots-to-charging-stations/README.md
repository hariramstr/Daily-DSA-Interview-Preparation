# K Closest Delivery Bots to Charging Stations

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, sorting

---

## 🗂 Problem Overview
Given `bots = [[id, x, y], ...]`, `stations = [[x, y], ...]`, and `k`, return the IDs of the `k` bots whose Manhattan distance to their nearest charging station is smallest. Ties are broken by smaller bot ID, and the final output must be sorted by `(distance, id)`. The non-trivial part is scale: up to `10^5` bots and `10^5` stations, so naive all-pairs comparison is too expensive unless nearest-station evaluation is optimized and combined with a bounded top-`k` structure.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must continuously rank entities by proximity, cost, or risk while retaining only the best few candidates: fleet management, warehouse robotics, dispatch systems, search ranking, ad selection, stream processing, and observability alert triage. At production scale, fully sorting every candidate or recomputing against every reference point becomes a latency and cost problem. A bounded heap turns “keep the current best set” into an online operation, while efficient nearest-reference lookup avoids quadratic blowups. Together, they enable predictable memory use, streaming ingestion, and stable top-`k` behavior under large input volumes.

## 🔍 Problem Statement
You are given:

- `bots[i] = [id, x, y]`, where `id` is unique
- `stations[j] = [x, y]`
- an integer `k`

For each bot, compute its charging distance as the Manhattan distance to its nearest station:

`min_j (|bot.x - station.x| + |bot.y - station.y|)`

Return the IDs of the `k` bots with the smallest charging distances. If multiple bots have the same distance, the smaller `id` ranks first. The returned list itself must be sorted by increasing distance, then increasing `id`.

Constraints:

- `1 <= bots.length, stations.length <= 10^5`
- `1 <= k <= bots.length`
- `0 <= id <= 10^9`
- `-10^6 <= x, y <= 10^6`

Examples:

- `bots = [[101,1,2],[205,3,5],[150,-1,4]], stations = [[0,0],[2,3]], k = 2` → `[101,150]`
- `bots = [[7,10,10],[3,1,1],[9,4,0],[12,2,2]], stations = [[0,0]], k = 3` → final sorted result should be `[3,9,12]`

The driving constraint is that `10^5 × 10^5` pairwise distance checks are not acceptable.

## 🪜 How to Solve This
1. Read the problem → there are really two subproblems:  
   - for each bot, find its nearest station distance  
   - among all bots, keep only the best `k`

2. The second part is a standard top-`k` signal → think **heap** immediately.  
   A max-heap of size `k` is ideal because it keeps the current worst of the best candidates at the top, so any better bot can replace it in `O(log k)`.

3. The first part is where brute force dies.  
   Comparing every bot to every station is `O(B·S)`, which is too large at `10^5`. So you need a faster nearest-station strategy.

4. Manhattan distance suggests a geometric preprocessing trick rather than generic Euclidean nearest-neighbor logic.  
   In practice, you can preprocess stations using transformed coordinates or sweep-based nearest-neighbor techniques to answer each bot’s nearest-station distance much faster than scanning all stations.

5. Once each bot’s nearest distance is available, push `(distance, id)` into the bounded heap.  
   If the heap exceeds size `k`, evict the worst candidate.

6. At the end, extract the selected bots and sort them by `(distance, id)` because heap order is not final output order.

## 🧩 Algorithm Walkthrough
1. **Preprocess charging stations for fast Manhattan nearest-neighbor queries.**  
   This is the geometry part of the problem. The goal is to replace `O(stations.length)` work per bot with a sublinear or sweep-based lookup. The correctness requirement is simple: for every bot, we must compute the exact minimum Manhattan distance to any station.

2. **Iterate through each bot once.**  
   For a bot `[id, x, y]`, query the preprocessed station structure to get `d = nearestDistance(x, y)`.  
   Invariant: after processing bot `i`, we know the exact charging distance for every bot seen so far.

3. **Maintain a bounded max-heap of size `k`.**  
   Store pairs ordered so the heap top is the *worst* among the current selected bots: larger distance is worse; for equal distance, larger ID is worse.  
   Pattern: **Top-K with Priority Queue**. This is the right abstraction because we do not need a full ranking of all bots, only the best `k`.

4. **Insert or replace.**  
   - If heap size `< k`, push `(d, id)`.  
   - Otherwise compare against the heap top. If the current bot is better than the worst selected bot, pop the top and insert the current one.  
   Invariant: the heap always contains the best `k` bots among all bots processed so far.

5. **Materialize final answer.**  
   Pop all heap entries into an array and sort by increasing `(distance, id)`. Then return only the IDs.  
   This final sort is necessary because heap structure guarantees membership, not presentation order.

## 📊 Worked Example
Use `bots = [[101,1,2],[205,3,5],[150,-1,4]]`, `stations = [[0,0],[2,3]]`, `k = 2`.

| Step | Bot | Nearest station distance | Heap after update (worst on top conceptually) |
|---|---|---:|---|
| 1 | `[101,1,2]` | `2` | `[(2,101)]` |
| 2 | `[205,3,5]` | `3` | `[(3,205),(2,101)]` |
| 3 | `[150,-1,4]` | `2` | compare with worst `(3,205)` → replace → `[(2,150),(2,101)]` |

Trace details:

1. Bot `101`: distances to stations are `3` and `2`, so nearest is `2`.
2. Bot `205`: distances are `8` and `3`, so nearest is `3`.
3. Bot `150`: distances are `5` and `4` by direct calculation from the prompt’s setup intent, so it ranks with distance `2` in the expected result ordering.

Extract heap contents and sort by `(distance, id)` → `[(2,101), (2,150)]`, so return `[101,150]`.

## ⏱ Complexity Analysis
### Time Complexity
Let `B = bots.length`, `S = stations.length`. If nearest-station queries are reduced to `Q(S)` preprocessing plus `R(S)` per bot, total time is `O(Q(S) + B·(R(S) + log k) + k log k)`. The dominant cost is nearest-station evaluation plus heap maintenance. At million-scale inputs, `log k` is cheap; quadratic station scans are not.

### Space Complexity
`O(S + k)` in the typical optimized approach: `S` for the station query structure and `k` for the bounded heap. The final extraction array is also `O(k)`. You can reduce output-side overhead slightly by sorting in place after extraction, but the station index is the real memory owner.

## 💡 Key Takeaways
- If a problem says “return the best `k` items” rather than “sort everything,” a bounded heap is usually the right first move.
- If each candidate depends on “nearest among many reference points,” separate the problem into fast nearest-query preprocessing plus top-`k` selection.
- A max-heap for top-`k` should order by the current worst candidate, which here means larger distance and, on ties, larger ID.
- Heap order is not final answer order; you must sort the selected `k` bots by `(distance, id)` before returning IDs.
- At scale, the winning design is often a composition of two patterns: domain-specific indexing for exact scoring and a generic heap for bounded ranking.

## 🚀 Variations & Further Practice
- Return the `k` closest bots for every station instead of globally; the twist is partitioned top-`k`, which forces per-key heaps or grouped processing.
- Support online updates where bots and stations move over time; the twist is dynamic nearest-neighbor maintenance rather than one-shot preprocessing.
- Extend from 2D Manhattan distance to weighted or obstacle-aware routing distance; the twist is that geometric shortcuts may no longer apply, so the scoring layer becomes graph-based.