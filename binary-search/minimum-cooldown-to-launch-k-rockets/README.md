# Minimum Cooldown to Launch K Rockets

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given a sorted array of launch pad positions, choose exactly `k` distinct pad indices so that the minimum distance between any two consecutive chosen pads is as large as possible. Return that maximum feasible distance. The challenge is scale: with up to `200000` pads and coordinates up to `10^18`, enumerating subsets or trying all placements directly is infeasible. The key is to exploit the monotonic feasibility of a candidate cooldown distance.

## 🌍 Engineering Impact
This pattern shows up anywhere you need to maximize a safety margin, spacing, or cooldown under hard capacity constraints. Examples include placing replicas across failure domains, scheduling jobs with thermal or power separation, assigning radio channels with interference bounds, and selecting cache refresh windows to avoid thundering herds. At scale, brute-force placement logic collapses under combinatorial growth. The binary-search-on-answer pattern turns an optimization problem into repeated feasibility checks, which is often the difference between something operationally usable and something that times out, stalls planners, or forces weaker heuristics into production.

## 🔍 Problem Statement
You are given a sorted array `pads` of length `n`, where `pads[i]` is the position of the `i`-th launch pad on a line. You must choose exactly `k` distinct pad indices such that every pair of consecutive chosen pads is at least `d` units apart. The goal is to maximize `d`.

Constraints:
- `2 <= n <= 200000`
- `2 <= k <= n`
- `0 <= pads[i] <= 10^18`
- `pads` is sorted in non-decreasing order
- Duplicate positions are allowed, but each index can be used at most once

Examples:

- `pads = [1, 2, 8, 12, 17], k = 3` → `7`
- `pads = [0, 0, 4, 9, 13, 18], k = 4` → `4`

The decisive constraint is input size: any subset-based or dynamic-programming approach over combinations is dead on arrival. The solution must be near-linear or `O(n log M)` where `M` is the coordinate range.

## 🪜 How to Solve This
1. Read the objective → we are not asked to construct all valid schedules, only the best minimum spacing. That usually suggests searching over the answer itself.

2. Ask the critical question: if a cooldown distance `d` is feasible, what about smaller values? They are also feasible. If `d` is infeasible, larger values are also infeasible. That monotonicity is the signal for binary search.

3. Now reduce the problem to a decision check: for a fixed `d`, can we place `k` launches? Since pads are sorted, the best strategy is greedy: take the earliest possible pad, then always take the next pad whose position is at least `d` away from the last chosen one.

4. Why greedy works → choosing earlier never hurts future choices; it leaves maximal remaining space to place the rest.

5. Binary search the largest feasible `d` over `[0, pads[n-1] - pads[0]]`.

6. Combine both pieces: `O(n)` feasibility check inside `O(log M)` search gives an efficient solution even with `10^18` coordinates.

## 🧩 Algorithm Walkthrough
1. **Define the search space.**  
   The minimum cooldown distance `d` can range from `0` to `pads[n-1] - pads[0]`. This upper bound is tight: no two chosen pads can be farther apart than the total span. This frames the optimization as **Binary Search on Answer**.

2. **Implement a feasibility check for a candidate `d`.**  
   Start by choosing the first pad. Then scan left to right and greedily choose the next pad whose position is at least `d` away from the last chosen position. Count how many pads are selected.  
   **Invariant:** after processing index `i`, the chosen set is the maximum-size feasible prefix selection ending within `pads[0..i]` under distance `d`.

3. **Why the greedy check is correct.**  
   This is a standard earliest-placement argument. If you can place a launch at some valid pad, taking the earliest such pad cannot reduce the number of future placements; any later choice only consumes more space. Therefore, if greedy cannot place `k` launches, no other strategy can.

4. **Binary search for the maximum feasible `d`.**  
   For midpoint `mid`, run the feasibility check. If `mid` works, record it and search higher; otherwise search lower.  
   **Invariant:** the answer always lies within the current search interval, and all values on one side of the feasibility boundary are known.

5. **Return the best feasible value found.**  
   This yields the largest cooldown distance that still allows exactly `k` chosen pads. Since choosing more than `k` also implies we can choose exactly `k`, the feasibility condition is `count >= k`.

## 📊 Worked Example
Example: `pads = [1, 2, 8, 12, 17], k = 3`

| Step | low | high | mid | Greedy picks for `mid` | count | Feasible |
|------|-----|------|-----|-------------------------|-------|----------|
| 1 | 0 | 16 | 8 | `1, 12` | 2 | No |
| 2 | 0 | 7 | 3 | `1, 8, 12, 17` | 4 | Yes |
| 3 | 4 | 7 | 5 | `1, 8, 17` | 3 | Yes |
| 4 | 6 | 7 | 6 | `1, 8, 17` | 3 | Yes |
| 5 | 7 | 7 | 7 | `1, 8, 17` | 3 | Yes |

Binary search stops after confirming `7` is feasible and no larger value remains in range.  
The greedy trace for `d = 7` is straightforward: choose `1`, skip `2`, choose `8`, skip `12` because `12 - 8 < 7`, then choose `17`. We placed exactly `3` launches, so `7` is valid.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `M = pads[n-1] - pads[0]`. Each binary-search step runs one linear greedy scan over `n` pads, and there are `O(log M)` steps. With `M` up to `10^18`, `log2(M)` is about 60, so this remains practical even for hundreds of thousands of elements; it would not be for `10^9` elements without distribution or streaming constraints.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few counters and position trackers; the input array dominates memory. Space cannot meaningfully be reduced below this unless the input itself is streamed, which would preserve the same asymptotic auxiliary cost but constrain access patterns.

## 💡 Key Takeaways
- If the problem asks for the maximum minimum distance, threshold, or capacity, check whether feasibility is monotonic; that is the classic trigger for binary search on the answer.
- When the input is already sorted and a fixed-threshold feasibility check asks “can we place/select enough items?”, greedy earliest placement is often the right proof shape.
- Use `count >= k`, not `count == k`, in the feasibility check; if you can place more than `k`, you can always stop at exactly `k`.
- Be careful with binary-search boundaries and midpoint updates when searching for the maximum feasible value; off-by-one errors here are the common failure mode.
- At scale, many optimization problems become tractable only after separating them into a monotone decision predicate plus a fast verifier.

## 🚀 Variations & Further Practice
- **Unsorted pad positions:** require an initial sort, changing the full complexity to `O(n log n + n log M)` and forcing you to distinguish preprocessing cost from search cost.
- **2D or graph-based placement:** maximize minimum distance among `k` selected sites when distance is Euclidean or shortest-path based; the feasibility check is no longer a simple greedy scan.
- **Weighted launches or forbidden pads:** each pad has value or eligibility constraints, so feasibility becomes combinatorial and may require DP, matching, or parametric search with a more complex predicate.