# Minimum Heater Time for Factory Rods

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Math

---

## 🗂 Problem Overview
Given rod lengths and a fixed budget of total machine-minutes, find the smallest positive integer heating time `t` such that processing every rod in chunks of size `t` requires at most `machines` total machine-minutes. Each rod contributes `ceil(length / t)` minutes. The challenge is that `t` ranges up to the maximum rod length, so naive linear search is too slow. The key property is monotonicity: if some `t` works, every larger `t` also works.

## 🌍 Engineering Impact
This pattern shows up anywhere you need the minimum capacity, batch size, timeout, or shard width that satisfies a global budget constraint. Examples include distributed rate-limiters choosing refill quanta, streaming pipelines sizing micro-batches to fit latency budgets, storage systems selecting chunk sizes to stay under I/O limits, and schedulers tuning task granularity against worker capacity. Without exploiting monotonic feasibility, systems fall back to exhaustive search or ad hoc tuning, which collapses under large search spaces. Binary search over the answer turns an operational sizing problem into a predictable, scalable decision procedure with clear correctness guarantees.

## 🔍 Problem Statement
You are given:

- `rods`, where `rods[i]` is the length of the `i`-th rod
- `machines`, the total machine-minutes available across all identical heating machines

If the factory chooses heating time `t`, then a rod of length `L` needs `ceil(L / t)` machine-minutes, because it can be processed in equal-sized segments over time. The goal is to return the smallest positive integer `t` such that:

`sum(ceil(rods[i] / t)) <= machines`

Constraints:

- `1 <= rods.length <= 100000`
- `1 <= rods[i] <= 1000000000`
- `rods.length <= machines <= 1000000000`

Examples:

- `rods = [8, 5, 10], machines = 7` → `4`
- `rods = [12, 15, 6], machines = 6` → `6`

The algorithmic driver is the search range: `t` can be as large as `10^9`, so scanning all candidates is not viable.

## 🪜 How to Solve This
1. Read the formula carefully → for any fixed `t`, we can compute the total required machine-minutes directly as `sum(ceil(rods[i] / t))`.

2. Ask the key question: what changes as `t` increases?  
   Larger heating time means each rod needs fewer segments, so total required machine-minutes never increases.

3. That gives a monotonic predicate:  
   `feasible(t) = total_minutes(t) <= machines`  
   Once this becomes true, it stays true for all larger `t`.

4. Monotonic predicate over an integer range → think binary search on the answer, not binary search in an array.

5. Set bounds:  
   - Minimum possible `t` is `1`  
   - Maximum needed `t` is `max(rods)` because at that point each rod takes exactly one minute

6. For each midpoint, compute required machine-minutes.  
   If it fits, try smaller `t`; if it does not, move right.

7. The first feasible value is the answer.  
   This is the standard “leftmost true” binary search pattern.

## 🧩 Algorithm Walkthrough
1. **Define the search space**  
   Use `low = 1` and `high = max(rods)`.  
   This is correct because `t` must be positive, and `t = max(rods)` always works: every rod contributes exactly `1`, so the total is `rods.length`, and the constraints guarantee `rods.length <= machines`.

2. **Define the monotonic feasibility check**  
   For a candidate `t`, compute:  
   `required = sum((L + t - 1) / t)` using integer arithmetic.  
   This is the standard ceiling-division identity and avoids floating-point error.

3. **Maintain the binary-search invariant**  
   - Values `< low` are known infeasible  
   - Values `>= high` contain at least one feasible answer  
   This is the classic **binary search on answer** pattern over a monotonic predicate.

4. **Evaluate the midpoint**  
   Let `mid = low + (high - low) / 2`.  
   If `required <= machines`, then `mid` is feasible, so the minimum feasible value is in `[low, mid]`; set `high = mid`.  
   Otherwise, the answer must be in `[mid + 1, high]`; set `low = mid + 1`.

5. **Terminate when bounds converge**  
   When `low == high`, the search has isolated the leftmost feasible `t`. That is exactly the minimum heating time.

6. **Use wide enough arithmetic**  
   The running sum can exceed 32-bit integer range in intermediate states, especially with `10^5` rods. Use 64-bit accumulation.

## 📊 Worked Example
Take `rods = [8, 5, 10]`, `machines = 7`.

| Step | low | high | mid | Required machine-minutes | Feasible? |
|---|---:|---:|---:|---:|---|
| Start | 1 | 10 | 5 | `ceil(8/5)+ceil(5/5)+ceil(10/5)=2+1+2=5` | Yes |
| 1 | 1 | 5 | 3 | `3+2+4=9` | No |
| 2 | 4 | 5 | 4 | `2+2+3=7` | Yes |

Trace:

1. `mid = 5` works, so search left for a smaller feasible value.
2. `mid = 3` fails, so everything at or below `3` is too small.
3. `mid = 4` works, and bounds converge to `4`.

Result: `t = 4`, the smallest heating time that stays within the machine-minute budget.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = rods.length` and `M = max(rods)`. Each binary-search step scans all rods once, and there are `log M` steps. With `M <= 10^9`, that is about 30 iterations, so even for `10^5` or `10^6` elements this remains practical; a linear scan over `10^9` candidate answers would not.

### Space Complexity
`O(1)` auxiliary space beyond the input. The algorithm stores only search bounds, a midpoint, and a running total. Space cannot be meaningfully reduced further unless you change the input representation itself; the main trade-off here is arithmetic width, not memory.

## 💡 Key Takeaways
- If the problem asks for the minimum integer parameter that satisfies a global constraint, check whether feasibility is monotonic and binary-search the answer.
- When increasing a candidate value can only make the constraint easier to satisfy, you are likely looking at a leftmost-true search pattern.
- Use integer ceiling division as `(x + t - 1) / t`; floating-point `ceil(x / t)` is unnecessary and can introduce precision bugs.
- Set the upper bound to `max(rods)`, not an arbitrary large constant; it is both sufficient and simplifies correctness reasoning.
- At scale, this pattern replaces tuning-by-enumeration with a deterministic capacity-sizing primitive that composes well in schedulers, storage systems, and throughput controllers.

## 🚀 Variations & Further Practice
- **Koko Eating Bananas / minimum processing rate problems**: same binary-search-on-answer structure, but framed as rate selection under a time budget.
- **Split array to minimize largest sum**: still binary search on a monotonic answer, but the feasibility check is greedy partitioning rather than ceiling-division math.
- **Minimum capacity to ship packages within D days**: similar pattern, with ordering constraints that make the feasibility function stateful across the scan.