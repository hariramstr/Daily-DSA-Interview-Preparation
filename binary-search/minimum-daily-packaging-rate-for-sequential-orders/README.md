# Minimum Daily Packaging Rate for Sequential Orders

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array `orders` and an integer `d`, compute the smallest integer daily packaging rate `R` that allows all orders to be completed within `d` days. Orders must be processed in original order, each day handles one contiguous suffix segment of remaining orders, and no order may be split across days. The non-trivial constraint is that partition boundaries are constrained by both contiguity and a per-day capacity, which makes direct optimization hard but feasibility monotonic.

## 🌍 Engineering Impact
This pattern shows up anywhere work must be processed in-order under bounded per-interval capacity: fulfillment batching, Kafka consumer catch-up windows, compaction scheduling in LSM storage, video transcoding queues, and ETL micro-batch sizing. At scale, the wrong approach either overprovisions throughput or performs expensive search over partitionings. The key architectural move is to separate optimization from validation: use a cheap linear feasibility check for a candidate capacity, then binary search the answer. That turns an intractable partition-selection problem into a predictable, production-safe control loop with clear latency and capacity bounds.

## 🔍 Problem Statement
You are given `orders`, where `orders[i]` is the size of the `i`-th order, and an integer `d` representing the number of available days. Each day must process a contiguous block of remaining orders from left to right. The total items processed in a day cannot exceed rate `R`, and a single order cannot be split across days.

Return the minimum integer `R` such that all orders are completed within `d` days.

Constraints:

- `1 <= orders.length <= 2 * 10^5`
- `1 <= orders[i] <= 10^9`
- `1 <= d <= 10^9`

Examples:

- `orders = [7, 2, 5, 10, 8], d = 2` → `18`
- `orders = [3, 6, 7, 11], d = 4` → `11`

The algorithmic driver is monotonic feasibility: if rate `R` works, every `R' > R` also works. That property makes binary search the right optimization strategy.

## 🪜 How to Solve This
1. Read the constraints → brute-force partitioning is dead on arrival. There are exponentially many ways to split an array into day-sized chunks.

2. Notice what is actually being optimized → not the partition itself, but the minimum capacity that makes *some* valid partition possible.

3. Ask the key binary-search question: for a fixed rate `R`, can we decide feasibility efficiently? Yes.

4. For a candidate `R`, scan left to right greedily:
   - keep adding orders to the current day while the sum stays `<= R`
   - when the next order would exceed `R`, start a new day
   - if any single order is `> R`, fail immediately

5. Why is greedy enough here? Because for a fixed capacity, packing each day as much as possible minimizes the number of days used. Any other valid schedule cannot use fewer days than this scan.

6. Once feasibility becomes a monotonic yes/no predicate, the search space is obvious:
   - lower bound = `max(orders)`
   - upper bound = `sum(orders)`

7. Binary search that range to find the smallest feasible `R`.

## 🧩 Algorithm Walkthrough
1. **Establish bounds**  
   Use the **Binary Search on Answer** pattern. The minimum possible rate is `max(orders)` because no order can be split. The maximum is `sum(orders)` because one day could process everything if allowed. This bounds the search over all valid answers.

2. **Define the feasibility predicate**  
   Implement `canFinish(R)` using a single left-to-right pass. Maintain:
   - `daysUsed`, initially `1`
   - `currentLoad`, initially `0`  
   For each order:
   - if `currentLoad + order <= R`, append it to the current day
   - otherwise, start a new day with that order and increment `daysUsed`

3. **Why the greedy scan is correct**  
   For fixed `R`, delaying a split as long as possible is optimal. Starting a new day earlier never helps reduce total days, because all future work remains in the same order and capacity is unchanged. Invariant: after processing index `i`, the scan has used the minimum possible days for prefix `orders[0..i]` under rate `R`.

4. **Exploit monotonicity**  
   If `canFinish(R)` is true, then `canFinish(R+1)` is also true. Larger capacity cannot invalidate an existing schedule. This monotonic predicate is exactly what binary search requires.

5. **Binary search for the first feasible rate**  
   While `low < high`, compute `mid`. If `canFinish(mid)` is true, keep the left half including `mid`; otherwise search right. The invariant is:
   - all values `< low` are infeasible
   - all values `>= high` are feasible

6. **Return `low`**  
   When the interval collapses, `low` is the smallest feasible daily rate.

## 📊 Worked Example
Example: `orders = [7, 2, 5, 10, 8]`, `d = 2`

Initial bounds: `low = 10`, `high = 32`

| Step | mid | Feasibility trace | daysUsed | Result |
|---|---:|---|---:|---|
| 1 | 21 | `[7,2,5] = 14`, add `10` → new day, day2=`10,8` | 2 | feasible |
| 2 | 15 | day1=`7,2,5` = 14, `10` starts day2, `8` needs day3 | 3 | infeasible |
| 3 | 18 | day1=`7,2,5` = 14, `10,8` = 18 on day2 | 2 | feasible |
| 4 | 17 | day1=`7,2,5` = 14, `10` day2, `8` day3 | 3 | infeasible |

Binary search narrows as:
- feasible at `21` → search left
- infeasible at `15` → search right
- feasible at `18` → search left
- infeasible at `17` → search right

Converged answer: `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n = orders.length` and `S = sum(orders) - max(orders) + 1` is the search range. Each binary-search step runs one linear feasibility scan. In practice this is efficient even for `n = 10^6`; the logarithmic factor stays small. At `10^9` elements, the array scan dominates and data movement becomes the real bottleneck.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only running totals, bounds, and a day counter. No extra arrays or DP tables are needed. Space cannot meaningfully be reduced further without changing the input model; the main trade-off is using 64-bit arithmetic to avoid overflow.

## 💡 Key Takeaways
- If the problem asks for a minimum capacity, speed, rate, or threshold and feasibility only improves as that value increases, think binary search on answer.
- If items must remain in order and a candidate threshold can be validated with one pass, look for a greedy feasibility check rather than explicit partition enumeration.
- Set the lower bound to `max(orders)`, not `0` or `min(orders)`; any smaller value is invalid because orders cannot be split.
- Use 64-bit integers for `sum`, `mid`, and running loads; with `2 * 10^5` elements of size `10^9`, 32-bit arithmetic overflows immediately.
- The transferable design pattern is to decouple optimization from scheduling: validate a candidate budget cheaply, then search the budget space instead of constructing globally optimal partitions directly.

## 🚀 Variations & Further Practice
- Allow splitting an order across days. The lower bound and feasibility logic change completely; the problem becomes throughput allocation rather than contiguous partitioning.
- Minimize the maximum daily load when exactly `d` non-empty groups must be formed. The exact-day requirement introduces edge handling beyond simple “within `d` days” feasibility.
- Add per-day setup costs or non-uniform day capacities. Feasibility may stop being monotonic, which breaks binary search on answer and forces DP or more complex optimization.