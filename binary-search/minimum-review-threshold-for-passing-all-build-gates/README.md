# Minimum Review Threshold for Passing All Build Gates

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Prefix Sum, Monotonic Function

---

## 🗂 Problem Overview
Given gate requirements and review batches, compute the smallest integer threshold `T` such that after capping every batch to `min(reviews[j], T)`, the pooled capped total can satisfy the sum of all gate requirements. If the uncapped total reviews are still insufficient, return `-1`. The challenge is scale: values are large, inputs are up to `2 * 10^5`, and scanning thresholds linearly is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere per-source contribution is globally capped before aggregation: distributed rate-limiters, ad-budget pacing, shard-level query quotas, fairness controls in ranking systems, and resource admission in multi-tenant schedulers. The core issue is deciding the minimum cap that makes aggregate capacity cross a target. At small scale, brute force works; at production scale, it collapses under large cardinality and wide numeric ranges. Recognizing monotonic aggregate functions lets you replace threshold sweeps with logarithmic search, which is the difference between an interactive control loop and an offline batch computation.

## 🔍 Problem Statement
You are given `n` build gates with required review points `requirements[i]` and `m` review batches `reviews[j]`. A single global threshold `T >= 0` is chosen. Each batch contributes at most `T`, so its usable value becomes `min(reviews[j], T)`. All capped contributions are pooled and can be distributed arbitrarily across gates, so only the total matters.

Find the minimum integer `T` such that:

`sum(min(reviews[j], T)) >= sum(requirements[i])`

If `sum(reviews) < sum(requirements)`, return `-1`.

Constraints:
- `1 <= n, m <= 2 * 10^5`
- `1 <= requirements[i], reviews[j] <= 10^12`
- Answer fits in signed 64-bit range

Examples:
- `requirements = [5, 7, 4]`, `reviews = [3, 10, 8]` → output `7`
- `requirements = [9, 6]`, `reviews = [4, 3, 5]` → output `-1`

The decisive constraint is the threshold search space: values are huge, so the solution must exploit monotonicity.

## 🪜 How to Solve This
1. Sum all gate requirements. Since capped review points can be redistributed arbitrarily, gate order is irrelevant; only the total required matters.

2. Sum all review batches. If even the uncapped total is below the required total, stop immediately with `-1`.

3. Now focus on the function `f(T) = sum(min(reviews[j], T))`. As `T` increases, `f(T)` never decreases. That monotonicity is the signal for binary search.

4. Ask the decision question: “For this threshold `T`, is the capped total at least the required total?” If yes, maybe a smaller `T` also works. If no, we must increase `T`.

5. To evaluate `f(T)` efficiently across many binary-search probes, sort `reviews` and build prefix sums. Then for any `T`, split batches into:
   - values `<= T`, contributing their full value
   - values `> T`, each contributing exactly `T`

6. That turns each feasibility check into a binary search over the sorted array plus O(1) arithmetic, which is fast enough for large inputs.

## 🧩 Algorithm Walkthrough
1. **Reduce the problem to totals.**  
   Compute `required = sum(requirements)` and `available = sum(reviews)`. This is correct because capped review points are fully fungible across gates. Invariant: feasibility depends only on aggregate supply versus aggregate demand.

2. **Early impossibility check.**  
   If `available < required`, return `-1`. No threshold can increase total supply beyond the uncapped sum. Invariant: all later work assumes at least one feasible threshold exists.

3. **Sort reviews and build prefix sums.**  
   Let `sortedReviews` be ascending. Build `prefix[i] = sum(sortedReviews[0..i-1])`. This prepares fast evaluation of capped totals. Pattern: **Binary Search + Prefix Sum over a Monotonic Function**.

4. **Define the feasibility function.**  
   For a candidate `T`, find the first index `idx` where `sortedReviews[idx] > T` using upper bound. Then:
   - batches before `idx` contribute `prefix[idx]`
   - remaining `m - idx` batches contribute `(m - idx) * T`  
   So `f(T) = prefix[idx] + (m - idx) * T`. Invariant: this exactly equals `sum(min(reviews[j], T))`.

5. **Binary search the minimum valid threshold.**  
   Search `T` in `[0, max(reviews)]`. If `f(mid) >= required`, record `mid` as a candidate and move left. Otherwise move right. Invariant: search space always preserves the smallest feasible threshold.

6. **Return the leftmost feasible value.**  
   Because feasibility is monotonic, the first `T` that passes is the minimum answer.

## 📊 Worked Example
Use `requirements = [5, 7, 4]`, `reviews = [3, 10, 8]`.

`required = 16`  
Sorted reviews: `[3, 8, 10]`  
Prefix sums: `[0, 3, 11, 21]`

| `T` | first `> T` index | full contribution | capped contribution | total | feasible |
|---|---:|---:|---:|---:|---|
| 5 | 1 | `prefix[1] = 3` | `2 * 5 = 10` | 13 | no |
| 8 | 2 | `prefix[2] = 11` | `1 * 8 = 8` | 19 | yes |
| 6 | 1 | `3` | `2 * 6 = 12` | 15 | no |
| 7 | 1 | `3` | `2 * 7 = 14` | 17 | yes |

Binary search progression:
1. Search range `[0, 10]`
2. Probe `5` → infeasible, move right
3. Probe `8` → feasible, move left
4. Probe `6` → infeasible, move right
5. Probe `7` → feasible, move left and terminate

Minimum valid threshold is `7`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting `reviews` costs `O(m log m)`. Each feasibility check is `O(log m)` for upper bound, and binary search over threshold values costs `O(log V)`, where `V = max(reviews)`. Total: `O(m log m + log V * log m)`. At million-scale arrays this is practical; linear threshold scans over `10^12`-sized domains are not.

### Space Complexity
`O(m)` for the sorted array and prefix sums. The prefix array owns most of the extra memory. It can be reduced if checks are recomputed by scanning, but that trades memory for unacceptable `O(m)` per probe time.

## 💡 Key Takeaways
- If the problem asks for the minimum numeric parameter that makes a condition true, check whether the condition is monotonic; that is a binary-search signal.
- If contributions look like `min(x, T)` or `max(x, T)`, sorting plus prefix sums often turns repeated aggregate checks into logarithmic-time queries.
- The binary search should target the leftmost feasible `T`; using the wrong loop invariant is the easiest way to return a non-minimal threshold.
- Use 64-bit arithmetic everywhere for totals and products like `(count * T)`; this problem overflows 32-bit types immediately.
- The production-grade insight is to separate optimization from feasibility: define a monotonic decision function first, then search the parameter space around it.

## 🚀 Variations & Further Practice
- Require each gate to receive reviews from a restricted subset of batches instead of a global pool; the monotonic threshold remains, but feasibility becomes a flow or matching problem.
- Allow different thresholds per batch class with a global fairness budget; now the search space becomes multidimensional and usually needs greedy structure or convex optimization.
- Support online updates to `reviews` and repeated threshold queries; the conceptual twist is replacing static prefix sums with Fenwick trees or segment trees over compressed values.