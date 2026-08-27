# Maximum Floor Height Under Elevator Trip Limits

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Monotonic Predicate, Math

---

## 🗂 Problem Overview
Given `n` sealed delivery batches, where batch `i` has `boxes[i]` boxes, choose a single integer elevator capacity `cap` for all batches. Batch `i` then needs `ceil(boxes[i] / cap)` trips, and total trips must not exceed `maxTrips`. Return the largest feasible `cap` in `[1, max(boxes)]`, or `-1` if even the best case is impossible. The challenge is scale: `n` is large and values reach `10^12`–`10^18`, ruling out linear search over capacities.

## 🌍 Engineering Impact
This pattern shows up anywhere a global configuration parameter must satisfy an aggregate budget under monotonic behavior. Examples include shard sizing under request fan-out limits, batch size tuning in streaming pipelines, chunk sizing for object storage uploads, and concurrency caps in distributed workers. At scale, brute-force tuning is operationally useless because the search space is huge and each feasibility check is already expensive. Recognizing a monotonic predicate lets you replace parameter sweeps with logarithmic search, turning an otherwise impractical control-plane decision into something deterministic, fast, and safe to run online.

## 🔍 Problem Statement
You are given an array `boxes` of length `n`, where `boxes[i]` is the number of identical boxes in batch `i`, and an integer `maxTrips`. You must choose one integer capacity `cap >= 1` for the elevator. A batch may be split across multiple trips, but batches cannot be mixed, so batch `i` requires `ceil(boxes[i] / cap)` trips.

Find the largest integer `cap` in the bounded search range `[1, max(boxes)]` such that:

`sum(ceil(boxes[i] / cap)) <= maxTrips`

If no such `cap` exists, return `-1`.

Constraints:
- `1 <= n <= 200000`
- `1 <= boxes[i] <= 10^12`
- `1 <= maxTrips <= 10^18`
- Answer fits in signed 64-bit integer

Examples:
- `boxes = [8, 5, 13], maxTrips = 8` → `13`
- `boxes = [4, 4, 4], maxTrips = 2` → `-1`

The key algorithmic driver is that feasibility is monotonic in `cap`, while the numeric range is far too large for direct iteration.

## 🪜 How to Solve This
1. Read the formula carefully → total trips is `sum(ceil(boxes[i] / cap))`.
2. Notice what happens when `cap` increases → each term stays the same or decreases, never increases.
3. That gives a monotonic predicate:  
   `feasible(cap) := totalTrips(cap) <= maxTrips`.
4. Monotonic predicate over an integer range usually means binary search, not simulation over all capacities.
5. Bound the search space: the problem explicitly defines meaningful capacities as `[1, max(boxes)]`. Above that, every batch already takes exactly one trip, so behavior stops changing.
6. Before searching, check impossibility: if even `cap = max(boxes)` needs more than `maxTrips`, no answer exists. Since that capacity minimizes trips within the allowed range, return `-1`.
7. Then binary search for the **largest** feasible value, not the first one. That changes the update rule: when `mid` is feasible, keep it and move right.
8. During feasibility checks, compute `ceil(a / b)` as `(a + b - 1) / b` and stop early if the running sum already exceeds `maxTrips`.

This is the standard “binary search on answer” path once you spot monotonicity.

## 🧩 Algorithm Walkthrough
1. **Define the predicate (`Binary Search on Answer`)**  
   For a candidate capacity `cap`, compute  
   `trips = Σ ceil(boxes[i] / cap)`.  
   If `trips <= maxTrips`, the candidate is feasible. This is the right abstraction because the problem does not ask for trips directly; it asks for the maximum parameter value satisfying a monotonic constraint.

2. **Establish monotonicity**  
   If `cap1 < cap2`, then for every batch, `ceil(boxes[i] / cap2) <= ceil(boxes[i] / cap1)`. Therefore total trips is non-increasing as capacity grows. The invariant is: once a capacity is feasible, every larger capacity in the bounded range is also feasible.

3. **Set search bounds**  
   Use `lo = 1`, `hi = max(boxes)`. This matches the problem’s bounded definition of the answer. At `hi`, each batch needs exactly one trip, so total trips is `n`. If `n > maxTrips`, return `-1` immediately.

4. **Run upper-bound style binary search**  
   While `lo <= hi`, take `mid = lo + (hi - lo) / 2`.  
   - If `mid` is feasible, record `mid` as current best and search right (`lo = mid + 1`) to find a larger feasible capacity.  
   - Otherwise search left (`hi = mid - 1`).

5. **Compute trips safely**  
   Use integer arithmetic: `ceil(x / cap) = (x + cap - 1) / cap`. Accumulate into 64-bit or wider integer. Maintain the invariant that if the running total already exceeds `maxTrips`, the predicate is false and the scan can terminate early.

6. **Return the best feasible capacity**  
   The stored answer is the maximum feasible `cap` in the allowed range. Correctness follows from the monotonic predicate and the binary search invariant that the feasible region is contiguous.

## 📊 Worked Example
Take `boxes = [8, 5, 13]`, `maxTrips = 8`.

| Step | lo | hi | mid | trips(mid) | Feasible? | best |
|---|---:|---:|---:|---:|---|---:|
| Start | 1 | 13 | 7 | `2 + 1 + 2 = 5` | Yes | 7 |
| Move right | 8 | 13 | 10 | `1 + 1 + 2 = 4` | Yes | 10 |
| Move right | 11 | 13 | 12 | `1 + 1 + 2 = 4` | Yes | 12 |
| Move right | 13 | 13 | 13 | `1 + 1 + 1 = 3` | Yes | 13 |
| End | 14 | 13 | — | — | — | 13 |

Trace notes:
1. `hi = max(boxes) = 13`.
2. Since `cap = 13` gives one trip per batch, total minimum trips in-range is `3`, so the instance is feasible.
3. Every tested midpoint is feasible, so the search keeps pushing right.
4. Final answer is `13`, the largest feasible capacity in the bounded domain.

## ⏱ Complexity Analysis
### Time Complexity
Each feasibility check scans all `n` batches, so it costs `O(n)`. Binary search performs `O(log M)` checks where `M = max(boxes)`. Total complexity is `O(n log M)`. With `n = 2e5` and `M` up to `1e12`, this is practical; linear scanning over all capacities is not remotely viable at `10^6` or `10^9` scale.

### Space Complexity
`O(1)` auxiliary space beyond the input array. The algorithm stores only search bounds, a running trip count, and the current best answer. Space cannot be meaningfully reduced further without changing the input representation; the main trade-off is using wider integer types to avoid overflow.

## 💡 Key Takeaways
- If the question asks for the largest or smallest parameter value that satisfies a constraint, check immediately for a monotonic predicate and consider binary search on the answer.
- When increasing a candidate can only improve or only worsen feasibility, the valid region is contiguous; that is the signal that turns a huge numeric domain into `log range` decisions.
- The bounded domain matters here: without restricting `cap` to `[1, max(boxes)]`, the “largest feasible” answer would be unbounded whenever any feasible solution exists.
- The main implementation trap is searching for the **maximum feasible** value: on success, move right and retain `mid`; otherwise move left.
- In production systems, this pattern is a control-plane optimization technique: model a tunable parameter with a monotonic budget check, then solve configuration selection deterministically instead of by expensive trial sweeps.

## 🚀 Variations & Further Practice
- Add a per-trip fixed overhead and ask for the minimum total cost under a trip budget; the twist is combining monotonic feasibility with a secondary optimization objective.
- Allow different capacities per elevator type with a limited fleet count; the harder part is that the predicate may require greedy allocation or DP inside each binary-search check.
- Replace sealed batches with mixable items and ask for the minimum capacity to finish within `k` trips; same monotonic search pattern, but the packing rule changes the feasibility function substantially.