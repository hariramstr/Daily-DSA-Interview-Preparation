# Minimum Warehouse Lift Strength for Stacked Crates

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array `weights` and an integer `maxTrips`, compute the smallest lift strength `S` that allows all crates to be moved left-to-right in order using at most `maxTrips` trips. Each trip must contain a contiguous block of crates whose total weight does not exceed `S`. The challenge is that crates cannot be reordered, skipped, or split, so the search space is over valid contiguous partitions rather than arbitrary grouping.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must preserve order while minimizing peak capacity: batching events into Kafka fetch windows, assigning video segments to transcoding workers, packing requests into rate-limited API calls, or splitting ETL workloads across bounded-memory executors. At scale, guessing capacity or using linear search is operationally expensive: you either overprovision infrastructure or violate throughput/SLA constraints. The binary-search-on-answer pattern converts a hard optimization problem into a sequence of cheap feasibility checks, which is exactly how production schedulers, admission controllers, and capacity planners make bounded decisions under monotonic constraints.

## 🔍 Problem Statement
You are given:

- `weights[i]`: the weight of the `i`-th crate
- `maxTrips`: the maximum number of trips allowed

The lift processes crates strictly from left to right. In one trip, it may take any consecutive sequence of crates as long as their total weight is at most some strength limit `S`. Every crate must be moved exactly once, and trips must partition the array into contiguous groups.

Return the minimum `S` such that all crates can be moved in at most `maxTrips` trips.

Constraints:

- `1 <= weights.length <= 100000`
- `1 <= weights[i] <= 1000000000`
- `1 <= maxTrips <= weights.length`
- The answer fits in a 64-bit signed integer

Examples:

- `weights = [7,2,5,10,8], maxTrips = 2` → `18`
- `weights = [4,4,4,4,4], maxTrips = 3` → `8`

The key constraint is input size: `n` can be `100000`, so enumerating partitions is infeasible.

## 🪜 How to Solve This
1. Read the problem → notice we are minimizing a maximum value, not constructing the partition itself.
2. Ask: if someone handed us a candidate strength `S`, can we quickly verify whether it works? Yes: scan left to right and greedily pack each trip until adding the next crate would exceed `S`, then start a new trip.
3. Why is greedy enough? Because with fixed `S`, delaying a split never hurts. Packing each trip as full as possible minimizes the number of trips used for that `S`.
4. Now observe monotonicity → if strength `S` is sufficient, then any larger strength is also sufficient. If `S` fails, any smaller strength must fail too.
5. Monotonic feasibility over an integer range is the signal for binary search on the answer.
6. Establish bounds:
   - Lower bound = `max(weights)` because every crate must fit alone.
   - Upper bound = `sum(weights)` because one trip can carry everything if allowed.
7. Binary search that range, using the greedy feasibility check to decide whether to move left or right.

That gives an `O(n log sum(weights))` solution with constant extra space.

## 🧩 Algorithm Walkthrough
1. **Define the search space**  
   Use **Binary Search on Answer**. The minimum feasible strength must lie between:
   - `low = max(weights)`
   - `high = sum(weights)`  
   This is correct because no solution can be smaller than the heaviest crate, and the total sum always works as a single-trip capacity.

2. **Implement a feasibility check for a candidate `S`**  
   Use a single left-to-right pass with a **Greedy partitioning** rule:
   - Keep a running trip sum.
   - If adding `weights[i]` stays `<= S`, extend the current trip.
   - Otherwise, start a new trip with `weights[i]`.  
   Invariant: after processing index `i`, the algorithm has used the fewest trips possible for prefix `weights[0..i]` under capacity `S`.

3. **Why the greedy check is correct**  
   For a fixed `S`, splitting earlier than necessary can only increase or preserve the trip count. Therefore, the fullest-prefix strategy minimizes trips. If even this strategy needs more than `maxTrips`, then no valid partition under `S` exists.

4. **Exploit monotonicity**  
   If `canMove(S)` is true, then `canMove(S+1)` is also true. That monotone predicate is exactly what binary search requires.

5. **Binary search for the first feasible value**  
   Compute `mid`.  
   - If `canMove(mid)` is true, record it implicitly by moving `high = mid`.
   - Otherwise move `low = mid + 1`.  
   Invariant: the answer always remains inside `[low, high]`.

6. **Return `low`**  
   When the search converges, `low == high` is the smallest feasible strength.

## 📊 Worked Example
Example: `weights = [7,2,5,10,8]`, `maxTrips = 2`

Initial bounds: `low = 10`, `high = 32`

| mid | Greedy trips formed | trips used | feasible? |
|---|---|---:|---|
| 21 | `[7,2,5]`, `[10,8]` | 2 | yes |
| 15 | `[7,2,5]`, `[10]`, `[8]` | 3 | no |
| 18 | `[7,2,5]`, `[10,8]` | 2 | yes |
| 17 | `[7,2,5]`, `[10]`, `[8]` | 3 | no |

Trace:
1. `mid = 21` works, so shrink right bound to `21`.
2. `mid = 15` fails, so raise left bound to `16`.
3. `mid = 18` works, so shrink right bound to `18`.
4. `mid = 17` fails, so raise left bound to `18`.

Now `low == high == 18`, so the minimum required strength is `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log R)`, where `n = weights.length` and `R = sum(weights) - max(weights) + 1`. Each binary-search step runs one linear feasibility scan. In practice this is fast even for large arrays because `log R` is at most about 50 for 64-bit sums. At million-scale inputs, the linear pass dominates; at billion-scale, input size itself becomes the bottleneck.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only scalar counters: bounds, running sum, and trip count. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off is using 64-bit integers everywhere to avoid overflow.

## 💡 Key Takeaways
- If a problem asks for the minimum possible capacity, threshold, or maximum load, try turning it into a yes/no feasibility check.
- When feasibility becomes easier as the candidate value increases, that monotonicity is a strong signal for binary search on the answer.
- The lower bound must be `max(weights)`, not `0` or `min(weights)`, because every crate must fit in some trip by itself.
- Use 64-bit arithmetic for `sum`, `mid`, and running trip totals; 32-bit overflow is easy here with `10^5` items of size `10^9`.
- In production systems, this pattern is a capacity-planning primitive: optimize a hard threshold by repeatedly evaluating a cheap monotone predicate instead of exploring combinatorial partitions.

## 🚀 Variations & Further Practice
- Allow exactly `k` trips instead of at most `k`; the twist is reasoning about whether unused capacity can always be converted into extra valid partitions.
- Minimize the maximum subarray sum when negative numbers are allowed; the monotonic structure weakens, and the greedy feasibility check no longer works unchanged.
- Add per-trip fixed overhead or startup cost; feasibility must account for both payload sum and trip cost, which changes the partitioning rule and the search bounds.