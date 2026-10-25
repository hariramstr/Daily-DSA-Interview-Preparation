# Minimum Oven Capacity for Batch Cookies

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array `trays`, where each value is the size of an order in fixed arrival order, determine the smallest oven capacity that lets you process all orders in at most `d` baking sessions. Each session must take a contiguous block of orders, and no order can be split or reordered. The non-trivial part is optimizing over possible capacities, not constructing arbitrary partitions, because feasibility changes monotonically as capacity increases.

## 🌍 Engineering Impact
This pattern shows up anywhere work must be partitioned into ordered batches under a hard per-batch limit: log segment compaction, streaming micro-batch sizing, warehouse wave picking, media transcoding queues, and request shaping in rate-limited downstream systems. The key architectural property is monotonic feasibility: if a batch size works, any larger one also works. Without exploiting that, teams often fall back to quadratic partitioning or repeated simulation that collapses under large input sizes. This approach enables predictable scaling, bounded latency in planning, and simpler admission-control decisions over massive ordered workloads.

## 🔍 Problem Statement
You are given:

- `trays[i]`: the number of cookie trays required by the `i`-th order
- `d`: the maximum number of baking sessions available

You must process orders in the original sequence. A single baking session may contain one or more **contiguous** orders, but the total trays assigned to that session cannot exceed the oven capacity. Orders cannot be reordered, skipped, or split across sessions.

Return the **minimum integer oven capacity** that allows all orders to be completed in at most `d` sessions.

Constraints:

- `1 <= trays.length <= 100000`
- `1 <= trays[i] <= 1000000`
- `1 <= d <= trays.length`
- Answer fits in a 64-bit signed integer

Examples:

- `trays = [8, 3, 5, 7, 2], d = 3` → `10`
- `trays = [4, 4, 4, 4], d = 2` → `8`

The decisive constraint is input size: `n` can reach `100000`, which rules out exhaustive partitioning and points to searching the answer space instead.

## 🪜 How to Solve This
1. Read the problem → notice we are not asked for the partition itself first; we are asked for the **minimum capacity** that makes some partition possible.

2. Ask what happens if capacity increases → feasibility only improves. If capacity `C` can finish within `d` sessions, then any capacity `> C` can too. That monotonic property is the signal for **binary search on the answer**.

3. Define the search bounds:
   - Lower bound = largest single order, because every order must fit somewhere.
   - Upper bound = sum of all orders, because one giant session would fit everything.

4. For a candidate capacity, simulate greedily:
   - Keep adding contiguous orders to the current session.
   - When the next order would exceed capacity, start a new session.
   - Count sessions used.

5. Why greedy works → for a fixed capacity, delaying a split as long as possible minimizes the number of sessions. Any earlier split cannot improve feasibility.

6. Binary search the smallest capacity whose greedy check uses `<= d` sessions. That gives the minimum feasible oven size in `O(n log S)` where `S` is the numeric search range.

## 🧩 Algorithm Walkthrough
1. **Establish numeric bounds**  
   Compute `low = max(trays)` and `high = sum(trays)`.  
   This is correct because any valid capacity must fit the largest single order, and the total sum is always sufficient using one session.  
   **Invariant:** the answer always lies in `[low, high]`.

2. **Use the Binary Search on Answer pattern**  
   Repeatedly choose `mid = low + (high - low) / 2` as a candidate oven capacity.  
   This pattern is appropriate because the predicate “can all orders be baked in at most `d` sessions with capacity `mid`?” is monotonic.

3. **Run a greedy feasibility check**  
   Scan `trays` left to right, maintaining:
   - `currentLoad`: trays assigned to the current session
   - `sessions`: number of sessions opened so far  
   If adding the next order stays within `mid`, keep it in the current session. Otherwise, start a new session with that order.  
   **Invariant:** after processing index `i`, the scan has used the minimum possible number of sessions for prefix `trays[0..i]` under capacity `mid`.

4. **Interpret the result**  
   If `sessions <= d`, then `mid` is feasible, so try smaller capacities by setting `high = mid`.  
   Otherwise, `mid` is too small, so set `low = mid + 1`.

5. **Terminate at the first feasible value**  
   The loop ends when `low == high`. That value is the smallest feasible capacity because binary search has eliminated all smaller infeasible capacities while preserving at least one feasible candidate.

## 📊 Worked Example
Example: `trays = [8, 3, 5, 7, 2]`, `d = 3`

Binary search starts with:

- `low = 8`
- `high = 25`

Check `mid = 16`:

| Order | Current Load | Action | Sessions |
|---|---:|---|---:|
| 8 | 8 | add to current | 1 |
| 3 | 11 | add to current | 1 |
| 5 | 16 | add to current | 1 |
| 7 | 7 | new session | 2 |
| 2 | 9 | add to current | 2 |

`16` is feasible, so search left.

Check `mid = 12` → sessions become `[8,3]`, `[5,7]`, `[2]` → `3`, still feasible.

Check `mid = 10` → sessions become `[8]`, `[3,5]`, `[7,2]` → `3`, feasible.

Check `mid = 9` → sessions become `[8]`, `[3,5]`, `[7]`, `[2]` → `4`, infeasible.

Smallest feasible capacity is `10`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log(sum(trays) - max(trays) + 1))`. Each binary-search step performs one linear greedy scan over `n` orders, and the number of steps is logarithmic in the numeric capacity range. At million-scale inputs this remains practical; at billion-scale inputs, the linear scan dominates and data movement becomes the real cost center.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few counters and bounds; it does not build partition structures or DP tables. Space cannot be meaningfully reduced further without changing the input model, though streaming the array is possible if random access is unnecessary.

## 💡 Key Takeaways
- If the problem asks for the **minimum/maximum value** that makes a schedule or allocation feasible, check whether feasibility is monotonic and therefore searchable.
- Ordered batching with contiguous groups and a hard per-group limit is a strong signal for **binary search on answer + greedy feasibility**.
- Set the lower bound to `max(trays)`, not `0` or `min(trays)`; otherwise the search admits impossible capacities.
- In the feasibility scan, start a new session only when `currentLoad + trays[i] > capacity`; using `>=` is a classic off-by-one bug.
- At scale, separating a monotonic decision predicate from optimization logic is a reusable design move for capacity planning, admission control, and batch sizing.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core pattern, but framed as minimizing the largest subarray sum across exactly `k` partitions; the conceptual twist is recognizing it as the same monotonic partitioning problem.
- **Capacity To Ship Packages Within D Days**: identical search-on-capacity structure, but with shipping semantics; useful for reinforcing the abstraction independent of domain wording.
- **Allow reordering of orders before batching**: much harder because contiguity disappears and the problem shifts toward bin packing, where greedy feasibility is no longer sufficient and the monotonic check becomes substantially more complex.