# Minimum Battery Capacity for Delivery Drone Loops

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, array

---

## 🗂 Problem Overview
Given an array `energy` and an integer `maxTrips`, find the smallest battery capacity that lets a drone serve all stops in order using at most `maxTrips` warehouse departures. Each trip must cover one contiguous block of stops, and the total energy of that block cannot exceed the battery capacity. The challenge is that capacity is not chosen directly from the input, so brute-forcing partitions is infeasible at `10^5` stops.

## 🌍 Engineering Impact
This pattern shows up anywhere a fixed-order workload must be split into bounded contiguous batches: Kafka consumer lag catch-up windows, media transcoding chunk assignment, warehouse wave planning, log compaction segments, and CI job sharding with ordering constraints. At scale, naive partition search explodes combinatorially, while overprovisioning wastes cost and capacity. Binary search on the answer converts sizing into a monotonic decision problem: “is this budget enough?” That enables predictable latency, simpler admission control, and tighter infrastructure sizing without exploring every partition layout.

## 🔍 Problem Statement
You are given:

- `energy[i]`: energy required to serve stop `i`
- `maxTrips`: maximum number of trips the drone may start from the warehouse

The drone starts fully charged for each trip, must serve stops from left to right, and each trip must cover a contiguous sequence of one or more stops. Stops cannot be reordered or split. The goal is to compute the minimum integer capacity `C` such that `energy` can be partitioned into at most `maxTrips` contiguous groups, each with sum at most `C`.

Constraints:

- `1 <= energy.length <= 100000`
- `1 <= energy[i] <= 1000000000`
- `1 <= maxTrips <= energy.length`
- Answer fits in signed 64-bit integer

Examples:

- `energy = [7,2,5,10,8], maxTrips = 2` → `18`
- `energy = [4,4,4,4,4], maxTrips = 3` → `8`

The key constraint is `n = 10^5`: partition enumeration is impossible, so the solution must exploit monotonicity.

## 🪜 How to Solve This
1. Read the problem → this is not about reordering or optimizing arbitrary subsets. We must split a fixed array into contiguous groups.

2. Ask what we are minimizing → not the number of trips, but the smallest capacity that makes a valid partition possible.

3. Notice the monotonic property → if capacity `C` is enough, then any larger capacity is also enough. That is the signal for binary search on the answer.

4. Define the feasibility question → “Given capacity `C`, how many trips are required if we pack stops greedily from left to right?”

5. Why greedy works → for a fixed capacity, the best way to minimize trip count is to keep adding stops to the current trip until the next stop would exceed `C`, then start a new trip. Any earlier split only increases trips.

6. Search range becomes obvious:
   - Lower bound = `max(energy)` because every stop must fit alone.
   - Upper bound = `sum(energy)` because one trip could cover everything if allowed.

7. Binary search that range, using the greedy trip counter as the predicate. The first feasible capacity is the answer.

## 🧩 Algorithm Walkthrough
1. **Set bounds for the answer space**  
   Let `low = max(energy)` and `high = sum(energy)`.  
   `low` is mandatory because no capacity smaller than the largest single stop can ever work. `high` is always feasible because it can hold the entire route in one trip.  
   **Invariant:** the true answer lies in `[low, high]`.

2. **Use Binary Search on Answer**  
   This is the explicit pattern: **Binary Search on a Monotonic Predicate**.  
   For a midpoint `mid`, ask whether all stops can be served in at most `maxTrips` trips with capacity `mid`.  
   If yes, search left for a smaller feasible capacity. If no, search right.  
   **Invariant:** feasible capacities form a suffix of the search space.

3. **Feasibility check via greedy scan**  
   Traverse `energy` once, maintaining `currentLoad` and `tripsUsed`. Add each stop to the current trip if it fits; otherwise, start a new trip with that stop.  
   This greedy rule minimizes trips for the proposed capacity because each trip is packed as tightly as possible before splitting.  
   **Invariant:** after processing index `i`, `tripsUsed` is the minimum number of trips needed for `energy[0..i]` under capacity `mid`.

4. **Terminate at first feasible capacity**  
   Continue until `low == high`. That value is the minimum feasible battery capacity.  
   This works because binary search preserves the smallest feasible point, not just any feasible point.

## 📊 Worked Example
Example: `energy = [7, 2, 5, 10, 8]`, `maxTrips = 2`

Initial bounds: `low = 10`, `high = 32`

| mid | Greedy partitioning under `mid` | tripsNeeded | Feasible? |
|---|---|---:|---|
| 21 | `[7,2,5] [10,8]` | 2 | Yes |
| 15 | `[7,2,5] [10] [8]` | 3 | No |
| 18 | `[7,2,5] [10,8]` | 2 | Yes |
| 16 | `[7,2,5] [10] [8]` | 3 | No |
| 17 | `[7,2,5] [10] [8]` | 3 | No |

Binary search narrows as follows:

1. `mid = 21` works → shrink right bound.
2. `mid = 15` fails → raise left bound.
3. `mid = 18` works → try smaller.
4. `mid = 16` fails.
5. `mid = 17` fails.

Now `low = high = 18`, so the minimum required capacity is `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n = energy.length` and `S = sum(energy) - max(energy) + 1` is the answer range. Each binary-search step runs one linear greedy scan. At `10^6` elements this remains practical; at `10^9`, the linear pass itself becomes the limiting factor, not the logarithm.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only running sums, bounds, and a trip counter. No extra arrays or partition structures are required. Space cannot be meaningfully reduced further without changing the input representation.

## 💡 Key Takeaways
- If the problem asks for the minimum possible threshold, budget, or capacity, and feasibility only gets easier as that value grows, think binary search on the answer.
- If items must remain in original order and be split into contiguous groups, greedy packing is often the right feasibility check.
- Use 64-bit arithmetic for `sum(energy)`, bounds, and `mid`; `int` will overflow under the stated constraints.
- The feasibility check should start a new trip only when adding the next stop would exceed capacity; splitting earlier gives the wrong trip count.
- At scale, this pattern is a sizing primitive: reduce an optimization problem to a monotonic yes/no predicate, then search the smallest safe operating point.

## 🚀 Variations & Further Practice
- Minimize the largest subarray sum when splitting into exactly `k` parts; the twist is handling “exactly” versus “at most” and proving equivalence or adjusting the predicate.
- Ship packages within `D` days; same pattern, but framed as capacity planning over ordered loads with day-count feasibility.
- Partition an array to minimize the maximum segment cost when segment cost is not just sum but something richer; the harder twist is losing the simple greedy predicate, which may require DP instead of binary search.