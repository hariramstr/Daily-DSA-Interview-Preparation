# Minimum Oven Temperature for Batch Baking

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Monotonic Predicate

---

## 🗂 Problem Overview
Given an array `batches` and an integer `hours`, find the smallest integer oven temperature `t` such that baking every tray sequentially takes at most `hours` total hours. At temperature `t`, tray `i` requires `ceil(batches[i] / t)` hours. The output is that minimum feasible `t`. The challenge is that `batches[i]` and `hours` are large, so trying temperatures linearly is too slow; the key is recognizing feasibility as a monotonic condition.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the minimum capacity that satisfies a deadline or SLA. Examples include autoscaling worker throughput to clear a backlog, selecting API rate limits to drain queued requests, sizing batch windows in streaming pipelines, and provisioning shard or replica counts in distributed storage. At scale, brute-force tuning across a large parameter range becomes operationally expensive or impossible. Monotonic-predicate search turns “find the smallest configuration that works” into a predictable logarithmic control loop, enabling fast planning, admission control, and capacity right-sizing without overprovisioning.

## 🔍 Problem Statement
You are given:

- `batches`, where `batches[i]` is the number of pastries in the `i`th tray
- `hours`, the total whole hours available

A single integer temperature `t` is used for all trays. Tray `i` takes `ceil(batches[i] / t)` hours, and trays are baked sequentially. Return the minimum integer `t` such that the total baking time is `<= hours`.

Constraints:

- `1 <= batches.length <= 100000`
- `1 <= batches[i] <= 1000000000`
- `batches.length <= hours <= 1000000000`
- `1 <= t <= max(batches)`

This guarantee matters: some feasible `t` always exists, and the answer lies in a bounded integer range. The decisive algorithmic signal is monotonicity: if temperature `t` is sufficient, then any higher temperature is also sufficient.

Examples:

- `batches = [12, 7, 18, 5], hours = 10` → `5`
- `batches = [30, 11, 23, 4, 20], hours = 6` → `23`

## 🪜 How to Solve This
1. Read the formula carefully → total time at temperature `t` is `sum(ceil(batches[i] / t))`.
2. Ask what happens as `t` increases → every term stays the same or decreases, so total time is monotonic non-increasing.
3. Translate the problem into a yes/no question: “Is temperature `t` sufficient to finish within `hours`?”
4. Once feasibility is monotonic, stop thinking about simulation or greedy scheduling. This is binary search on the answer, not on the array.
5. Define the search space:
   - minimum possible temperature is `1`
   - maximum needed temperature is `max(batches)` because any higher value gives no additional benefit per tray
6. For a candidate `mid`, compute required hours using integer ceiling division: `(batch + mid - 1) / mid`.
7. If required hours `<= hours`, `mid` works → try smaller temperatures.
8. Otherwise, `mid` is too low → search higher.
9. Continue until the search converges to the first feasible temperature.

That reasoning is the whole solution: identify monotonic feasibility, then binary search the smallest satisfying value.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer Space.**  
   The array itself is not sorted, but the predicate `canFinish(t)` is monotonic. That is the right abstraction: search over candidate temperatures, not tray orderings or schedules.

2. **Set search bounds.**  
   Initialize `left = 1` and `right = max(batches)`.  
   Why correct: `1` is the smallest legal temperature, and `max(batches)` always works because each tray then takes exactly `1` hour, so total time is `batches.length <= hours`.

3. **Define the feasibility check.**  
   For a candidate temperature `mid`, compute  
   `totalHours = Σ ceil(batches[i] / mid)`  
   using integer arithmetic: `(batches[i] + mid - 1) / mid`.  
   Invariant: after processing each tray, `totalHours` equals the exact cumulative hours required at temperature `mid`.

4. **Use early termination in the check.**  
   If `totalHours` already exceeds `hours`, stop and return false.  
   Why it matters: it avoids unnecessary work on large inputs and preserves the same correctness because exceeding the budget is irreversible.

5. **Shrink the search space.**  
   If `canFinish(mid)` is true, record that a feasible answer exists in `[left, mid]`, so set `right = mid`.  
   Otherwise, the answer must be in `[mid + 1, right]`, so set `left = mid + 1`.  
   Invariant: the true minimum feasible temperature always remains inside `[left, right]`.

6. **Terminate when bounds converge.**  
   When `left == right`, that value is the smallest feasible temperature.  
   Why correct: binary search has eliminated all smaller infeasible values while preserving at least one feasible value.

This is the standard “first true” binary-search template over a monotonic predicate.

## 📊 Worked Example
Example: `batches = [12, 7, 18, 5]`, `hours = 10`

| Step | left | right | mid | Hours at `mid` | Feasible? | Next Range |
|---|---:|---:|---:|---:|---|---|
| 1 | 1 | 18 | 9 | `2+1+2+1=6` | Yes | `[1, 9]` |
| 2 | 1 | 9 | 5 | `3+2+4+1=10` | Yes | `[1, 5]` |
| 3 | 1 | 5 | 3 | `4+3+6+2=15` | No | `[4, 5]` |
| 4 | 4 | 5 | 4 | `3+2+5+2=12` | No | `[5, 5]` |

Binary search converges to `5`.

Interpretation:
- `9` works, so smaller temperatures might still work.
- `5` works exactly, so keep pushing left.
- `3` and `4` are too slow.
- Once the interval collapses to a single value, that value is the minimum feasible temperature.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = batches.length` and `M = max(batches)`. Each binary-search step scans the array once to evaluate feasibility, and there are `log M` such steps. With `M <= 10^9`, that is about 30 iterations, so even for `10^6` elements the approach remains practical; linear scanning over all candidate temperatures would not.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only search bounds and a running hour total; the input array dominates memory usage. Space cannot be meaningfully reduced below constant unless the input representation itself changes, which would not improve the algorithmic trade-off.

## 💡 Key Takeaways
- If the problem asks for the *minimum integer value* that satisfies a condition, check whether that condition becomes permanently true after some threshold.
- When increasing a parameter can only improve feasibility, think “monotonic predicate” and binary search the answer space.
- Use integer ceiling division as `(x + t - 1) / t`; floating-point math is unnecessary and can introduce avoidable precision risk.
- Be precise about binary-search boundaries: for “first feasible value,” use `right = mid` on success, not `mid - 1`, or you can skip the answer.
- In production systems, this pattern is a capacity-planning primitive: find the smallest resource level that meets a deadline instead of overprovisioning by guesswork.

## 🚀 Variations & Further Practice
- **Variable per-tray setup cost:** each tray has a fixed overhead plus `ceil(batches[i] / t)` processing time. The monotonic predicate still holds, but feasibility accounting becomes easier to get wrong.
- **Parallel ovens with shared temperature:** multiple trays can bake concurrently across `k` ovens. The twist is combining monotonic search with a scheduling or load-balancing feasibility check.
- **Minimum speed to arrive on time / ship packages within D days:** same binary-search-on-answer pattern, but the predicate changes from per-item ceiling sums to partitioning or deadline accumulation.