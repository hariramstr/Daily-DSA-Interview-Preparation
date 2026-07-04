# Minimum Processing Rate for Deadline Batches

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Search on Answer

---

## 🗂 Problem Overview
Given batch sizes and a hard deadline of `h` hours, find the smallest integer processing rate `r` such that one worker can finish every batch in time. A batch of size `x` takes `ceil(x / r)` hours because partial final hours still count as full hours. The challenge is not scheduling order — order is irrelevant — but selecting the minimum feasible rate under large constraints, which rules out linear scanning over all possible rates.

## 🌍 Engineering Impact
This pattern shows up in capacity planning for batch ETL, stream backfill jobs, warehouse compaction, search indexing, and ML feature generation pipelines. You are not optimizing a schedule; you are sizing a fixed service rate against a deadline. At production scale, brute-forcing candidate capacities or simulating every configuration is too expensive, especially when the rate domain is large. The monotonic feasibility property enables binary search over capacity, turning what looks like an optimization problem into a fast decision loop. That shift matters in autoscaling controllers, admission planning, and SLO-aware throughput provisioning.

## 🔍 Problem Statement
You are given an integer array `batches`, where `batches[i]` is the number of records in batch `i`, and an integer `h`, the total hours available. A single worker processes at a fixed integer rate `r` records per hour for the entire day. In any hour, the worker can work on only one batch, and finishing a partially filled final hour still consumes the whole hour. Therefore, batch `x` requires `ceil(x / r)` hours.

Return the minimum integer rate `r` such that all batches finish within `h` hours.

Constraints:

- `1 <= batches.length <= 100000`
- `1 <= batches[i] <= 1000000000`
- `batches.length <= h <= 1000000000`
- The answer always exists

Examples:

- `batches = [12, 7, 25, 9], h = 10` → `7`
- `batches = [30, 11, 23, 4, 20], h = 6` → `23`

The key constraint is the search space: batch sizes can reach `10^9`, so testing every possible rate is not viable.

## 🪜 How to Solve This
1. Read the problem → notice the output is not a schedule or ordering, but the **minimum rate** that satisfies a deadline.

2. Ask what happens if we guess a rate `r` → for each batch, required time is `ceil(batch / r)`, so total hours is just the sum across all batches.

3. Observe the monotonic property → if rate `r` is fast enough, then every rate greater than `r` is also fast enough. If `r` is too slow, every smaller rate is also too slow.

4. A monotonic yes/no condition over an integer range is a strong signal for **binary search on answer**.

5. Define the search bounds:
   - Minimum possible rate is `1`
   - Maximum necessary rate is `max(batches)` because any larger rate does not reduce a batch below one hour

6. For each midpoint rate, compute total required hours.
   - If total hours `<= h`, the rate is feasible, so try smaller
   - Otherwise, rate is infeasible, so try larger

7. Continue until the search converges on the smallest feasible rate.

This works because feasibility is cheap to test in one pass, and the answer space is ordered.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Binary Search on Answer**  
   We are not searching inside the array; we are searching the integer rate domain. The predicate is: “Can all batches finish within `h` hours at rate `r`?” This predicate is monotonic, which makes binary search the correct abstraction.

2. **Initialize bounds**  
   Set `left = 1` and `right = max(batches)`.  
   Why correct: rate `1` is the slowest meaningful candidate, and `max(batches)` is always sufficient because each batch then finishes in at most one hour.  
   Invariant: the answer lies within `[left, right]`.

3. **Evaluate a candidate rate**  
   Let `mid = left + (right - left) / 2`. Compute:
   `hours = Σ ceil(batch / mid)`  
   Use integer arithmetic: `(batch + mid - 1) / mid`.  
   Why correct: each batch consumes whole hours, including a partially used final hour.

4. **Shrink the search space**  
   - If `hours <= h`, `mid` is feasible. Record that the answer is at most `mid`, so move `right = mid`.
   - If `hours > h`, `mid` is too slow. Move `left = mid + 1`.  
   Invariant: all rates below `left` are known infeasible; all rates at or above a feasible boundary remain candidates.

5. **Terminate on convergence**  
   Stop when `left == right`. That value is the minimum feasible rate.  
   Why correct: binary search preserves the smallest valid solution by never discarding a feasible lower bound candidate prematurely.

6. **Implementation detail: early exit**  
   While summing hours, if the running total already exceeds `h`, stop early. This does not change correctness, but reduces work on clearly infeasible rates.

## 📊 Worked Example
Example: `batches = [12, 7, 25, 9], h = 10`

| Step | left | right | mid | Hours at `mid` | Feasible? | Next Range |
|---|---:|---:|---:|---:|---|---|
| 1 | 1 | 25 | 13 | `1+1+2+1 = 5` | Yes | `[1, 13]` |
| 2 | 1 | 13 | 7 | `2+1+4+2 = 9` | Yes | `[1, 7]` |
| 3 | 1 | 7 | 4 | `3+2+7+3 = 15` | No | `[5, 7]` |
| 4 | 5 | 7 | 6 | `2+2+5+2 = 11` | No | `[7, 7]` |

Search converges at `7`.

Interpretation:
- Rates `4` and `6` are too slow.
- Rates `7` and `13` are fast enough.
- Because feasibility is monotonic, once `7` works, every larger rate also works. Binary search isolates the boundary between infeasible and feasible rates, which is exactly the minimum valid processing rate.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = batches.length` and `M = max(batches)`. Each binary-search step scans all batches once to compute required hours, and there are `log M` candidate-rate checks. At `10^6` items this is still practical; scanning all rates up to `10^9` is not.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few scalar variables for bounds, midpoint, and accumulated hours. No extra data structures are required. Space cannot be meaningfully reduced further without changing the input representation.

## 💡 Key Takeaways
- If the problem asks for the **minimum integer value** that satisfies a condition, check whether feasibility is monotonic and consider binary search on answer.
- When order does not affect the aggregate cost, reduce the problem to a yes/no predicate over a candidate parameter rather than simulating schedules.
- Use integer ceiling division as `(x + r - 1) / r`; floating-point division is unnecessary and can introduce avoidable precision and performance issues.
- Be careful with binary-search boundaries: when `mid` is feasible, keep it in the search space with `right = mid`, not `mid - 1`, or you can skip the minimum valid answer.
- In production systems, this pattern is a capacity-sizing primitive: convert optimization into repeated feasibility checks over a monotonic control variable.

## 🚀 Variations & Further Practice
- **Ship packages within D days**: same search-on-answer pattern, but feasibility depends on preserving original order and packing contiguous items into daily capacity.
- **Split array largest sum**: minimize the maximum partition sum; the twist is partitioning into at most `k` groups while maintaining sequence order.
- **Koko Eating Bananas / service-rate sizing variants**: same monotonic predicate, but useful for recognizing when a throughput or capacity parameter can be isolated from scheduling details.