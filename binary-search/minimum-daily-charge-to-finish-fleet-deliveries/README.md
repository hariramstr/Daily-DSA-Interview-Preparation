# Minimum Daily Charge to Finish Fleet Deliveries

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Math

---

## 🗂 Problem Overview
Given an array `deliveryDistance` and a deadline `maxDays`, find the smallest positive integer daily charge budget `X` such that the total days required across all vans, `sum(ceil(deliveryDistance[i] / X))`, is at most `maxDays`. The challenge is not computing days for one candidate `X`, but finding the minimum valid `X` efficiently under large constraints: up to `10^5` vans, distances up to `10^9`, and `maxDays` up to `10^12`.

## 🌍 Engineering Impact
This pattern shows up whenever you need the minimum capacity, quota, or throughput that satisfies a global SLA. Examples include sizing batch windows in streaming pipelines, choosing shard throughput in distributed queues, setting API rate budgets, provisioning worker concurrency, or determining minimum network bandwidth for replication jobs. At scale, brute-force tuning is not viable because the search space is huge and each feasibility check already touches large datasets. Recognizing monotonic feasibility lets you replace iterative guesswork with a deterministic logarithmic search, which is the difference between a control loop that converges quickly and one that burns compute or misses deadlines.

## 🔍 Problem Statement
Each van `i` must complete `deliveryDistance[i]` kilometers. If the system provides a daily charge budget of `X` kilometers, that van finishes in `ceil(deliveryDistance[i] / X)` days. All vans operate independently, and the total required days is:

`ceil(d1 / X) + ceil(d2 / X) + ... + ceil(dn / X)`

You must return the smallest positive integer `X` such that this total is `<= maxDays`.

Constraints:

- `1 <= deliveryDistance.length <= 100000`
- `1 <= deliveryDistance[i] <= 1000000000`
- `deliveryDistance.length <= maxDays <= 1000000000000`

Examples:

- `deliveryDistance = [12, 7, 25, 9], maxDays = 10` → `7`
- `deliveryDistance = [3, 6, 14], maxDays = 8` → `3`

The key observation is that as `X` increases, total required days never increases. That monotonic property drives the algorithmic choice.

## 🪜 How to Solve This
1. Read the formula carefully → for any fixed `X`, we can compute total days directly as `sum(ceil(distance / X))`.

2. Ask what happens when `X` grows → each term `ceil(distance / X)` stays the same or decreases. So the total days function is monotonic non-increasing.

3. Monotonic answer space usually means binary search on the answer, not on the array.

4. Define the search range:
   - Minimum possible `X` is `1`
   - Maximum useful `X` is `max(deliveryDistance)`, because then every van finishes in exactly one day

5. For a candidate `mid`, compute total required days:
   - If total days `<= maxDays`, `mid` is feasible, and maybe we can do better with a smaller `X`
   - Otherwise `mid` is too small, so move right

6. Keep shrinking the range until `left == right`. That value is the minimum feasible budget.

7. Use integer arithmetic for ceiling division:  
   `ceil(a / b) = (a + b - 1) / b`

This is the standard “minimum feasible value under a monotonic predicate” template.

## 🧩 Algorithm Walkthrough
1. **Model the feasibility predicate**  
   Define `canFinish(X)` as whether `sum(ceil(deliveryDistance[i] / X)) <= maxDays`. This predicate is monotonic: if `canFinish(X)` is true, then `canFinish(Y)` is also true for every `Y > X`. That is the core invariant enabling **Binary Search on Answer**.

2. **Initialize bounds**  
   Set `left = 1` and `right = max(deliveryDistance)`.  
   Why this is correct:
   - `1` is the smallest legal positive budget
   - `max(deliveryDistance)` is always feasible because each van then needs exactly one day, so total days equals the number of vans, which is guaranteed `<= maxDays`

3. **Pick the midpoint**  
   Compute `mid = left + (right - left) / 2` to avoid overflow in fixed-width integer environments. `mid` is the candidate daily budget to test.

4. **Evaluate feasibility in one pass**  
   For each distance `d`, add `(d + mid - 1) / mid` to a running total. This computes `ceil(d / mid)` using integer math.  
   Invariant: after processing the first `k` vans, the running total equals the exact days required for those `k` vans under budget `mid`.

5. **Prune the search space**  
   - If total days `<= maxDays`, `mid` is feasible, so the minimum answer lies in `[left, mid]`
   - Otherwise, the answer lies in `[mid + 1, right]`

6. **Terminate when bounds converge**  
   When `left == right`, every smaller value has been ruled out and the current value is feasible. That makes it the minimum valid `X`.

This abstraction is correct because binary search is operating over a sorted boolean space: invalid budgets first, then valid budgets.

## 📊 Worked Example
Use `deliveryDistance = [12, 7, 25, 9]`, `maxDays = 10`.

| Step | left | right | mid | Days Calculation | totalDays | Feasible? |
|---|---:|---:|---:|---|---:|---|
| 1 | 1 | 25 | 13 | 1 + 1 + 2 + 1 | 5 | Yes |
| 2 | 1 | 13 | 7 | 2 + 1 + 4 + 2 | 9 | Yes |
| 3 | 1 | 7 | 4 | 3 + 2 + 7 + 3 | 15 | No |
| 4 | 5 | 7 | 6 | 2 + 2 + 5 + 2 | 11 | No |
| 5 | 7 | 7 | 7 | done | — | answer |

Trace:
1. `mid = 13` works, so search left half.
2. `mid = 7` also works, so try smaller.
3. `mid = 4` fails, so move right.
4. `mid = 6` fails too.
5. Bounds converge at `7`, the minimum feasible daily charge budget.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n` is the number of vans and `M = max(deliveryDistance)`. Each binary-search step scans the array once, and there are `log M` steps. With `n = 10^5` and `M ≈ 10^9`, this is roughly 30 full passes, which is practical; brute-forcing all `X` values is not.

### Space Complexity
`O(1)` auxiliary space beyond the input array. The algorithm stores only search bounds and a running sum. Space cannot be meaningfully reduced further; the main trade-off is using wider integer types for safety when accumulating totals.

## 💡 Key Takeaways
- If the question asks for the **minimum integer parameter** that makes a condition true, check whether feasibility is monotonic and search the answer space directly.
- When increasing a capacity, rate, or budget can only improve feasibility, that is a strong binary-search-on-answer signal.
- Use integer ceiling division as `(d + x - 1) / x`; floating-point division is unnecessary and risks precision issues.
- The search update must preserve “first true” semantics: feasible → `right = mid`, infeasible → `left = mid + 1`.
- In production systems, this pattern is a capacity-sizing primitive: encode the SLA as a monotonic predicate, then solve for the smallest safe provisioning level deterministically.

## 🚀 Variations & Further Practice
- **Koko Eating Bananas / minimum processing rate**: same pattern, but framed as rate selection under a time budget; the twist is recognizing the hidden monotonic predicate quickly.
- **Ship packages within D days**: binary search on capacity, but feasibility requires a sequential greedy pass rather than independent per-item ceiling math.
- **Minimum limit of balls in a bag / split operations**: still binary search on answer, but the predicate is based on allowed transformation operations, not direct time aggregation.