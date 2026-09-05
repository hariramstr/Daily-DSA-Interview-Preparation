# Minimum Lane Fixes to Make Traffic Speeds Nondecreasing

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Simulation

---

## 🗂 Problem Overview
Given an integer array `speeds`, compute the minimum total amount you must add so the final sequence becomes nondecreasing from left to right. You may increase any element by any positive amount, but you may never decrease one. The output is the sum of all increases, not the transformed array itself. The non-trivial part is finding the global minimum under a one-way operation constraint, in linear time, while safely handling totals that exceed 32-bit range.

## 🌍 Engineering Impact
This pattern shows up anywhere a stream must be repaired to satisfy a monotonic invariant without rewriting prior state: event-time watermark correction in streaming pipelines, cumulative quota enforcement in distributed rate-limiters, monotonically increasing version vectors, and search or ranking systems that post-process noisy scores into stable ordered outputs. At scale, the wrong approach introduces backtracking, buffering, or quadratic repair passes. The right greedy pass enables online correction, predictable memory use, and simple operational semantics: once a prefix is fixed, it never needs revisiting. That matters in high-throughput telemetry, ETL normalization, and append-only data processing systems.

## 🔍 Problem Statement
You are given an array `speeds` of length `n`, where `1 <= n <= 100000` and `0 <= speeds[i] <= 1000000000`. Each value represents a measured traffic speed at a highway segment, ordered west to east.

You may perform any number of lane fixes. In one fix, choose a single index and increase `speeds[i]` by any positive amount. Decreasing values is forbidden. Your goal is to make the final array nondecreasing, meaning `final[i] <= final[i + 1]` for every valid `i`, while minimizing the total added amount.

Return that minimum total increase. Use 64-bit arithmetic because the answer may exceed 32-bit range.

Examples:

- `speeds = [5, 3, 3, 7, 2]` → `9`
- `speeds = [1, 2, 4, 4, 6]` → `0`

The key constraint is `n = 100000`, which rules out repair strategies that repeatedly rescan or compare many pairs.

## 🪜 How to Solve This
1. Read the constraint carefully → we can only increase values, never decrease them. That means every decision is constrained by what came before.

2. Ask what the smallest valid value at position `i` can be → it must be at least the final value of position `i - 1`. Any larger increase is unnecessary and only makes later positions harder.

3. That observation suggests a greedy rule: process left to right, maintain the maximum value the current element must reach, and only raise elements that fall below it.

4. If `speeds[i]` is already at least that running requirement, do nothing and update the requirement to `speeds[i]`.

5. If `speeds[i]` is smaller, the cheapest valid repair is to raise it exactly to the running requirement. Add the difference to the answer.

6. Why this works → once a prefix is nondecreasing, the only information the next position cares about is the final previous value. There is no benefit in revisiting earlier choices.

7. That gives a single-pass simulation with a greedy invariant, which is exactly what the input size wants.

## 🧩 Algorithm Walkthrough
1. **Pattern: Greedy single-pass simulation over an array.**  
   This is the right abstraction because each element’s minimum legal final value depends only on the final value immediately before it. There is no branching state worth storing.

2. **Initialize a running threshold.**  
   Let `prev` be the final value of the previous segment. Start with `prev = speeds[0]`. Also initialize `total = 0` using 64-bit arithmetic.  
   **Invariant:** the processed prefix can already be made nondecreasing with total increase `total`, and `prev` equals the last value in that repaired prefix.

3. **Scan from left to right starting at index 1.**  
   For each `speeds[i]`, compare it with `prev`.

4. **Case 1: `speeds[i] >= prev`.**  
   No increase is needed. Set `prev = speeds[i]`.  
   **Why correct:** keeping the value unchanged is the cheapest valid choice, and a larger value would only tighten constraints for later elements.

5. **Case 2: `speeds[i] < prev`.**  
   Increase this element to exactly `prev`. Add `prev - speeds[i]` to `total`. Keep `prev` unchanged.  
   **Why correct:** any value below `prev` violates monotonicity; any value above `prev` adds unnecessary cost and cannot improve future decisions.

6. **Finish after one pass.**  
   The invariant guarantees the whole array is repairable with cost `total`, and the greedy choice at each step is locally minimal and globally optimal because future positions only inherit the current final value, not the original one.

## 📊 Worked Example
Use `speeds = [5, 3, 3, 7, 2]`.

| i | original | prev before | increase needed | total after | prev after |
|---|----------|-------------|-----------------|-------------|------------|
| 0 | 5        | —           | 0               | 0           | 5          |
| 1 | 3        | 5           | 2               | 2           | 5          |
| 2 | 3        | 5           | 2               | 4           | 5          |
| 3 | 7        | 5           | 0               | 4           | 7          |
| 4 | 2        | 7           | 5               | 9           | 7          |

Trace:

1. Start with `prev = 5`.
2. `3 < 5`, so raise it by `2`; total becomes `2`.
3. Next `3 < 5`, raise again by `2`; total becomes `4`.
4. `7 >= 5`, keep it; now `prev = 7`.
5. `2 < 7`, raise by `5`; total becomes `9`.

Minimum total added speed is `9`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because it performs one left-to-right pass and constant work per element. There are no nested scans, sorting steps, or backtracking decisions. At `10^6` elements this is routine; at `10^9`, runtime becomes bandwidth-bound, but the asymptotic shape remains optimal.

### Space Complexity
The algorithm uses `O(1)` extra space. The only additional state is the running previous value and the 64-bit accumulated total. Space cannot be meaningfully reduced further unless input mutation or streaming input constraints change the interface.

## 💡 Key Takeaways
- If you can only increase values and need a nondecreasing array, that is a strong signal for a left-to-right greedy pass with a running floor.
- When each position depends only on the repaired previous position, think simulation with an invariant, not dynamic programming or sorting.
- Use 64-bit accumulation: individual fixes fit in 32 bits, but the total across `100000` elements may not.
- Do not update `prev` incorrectly after a repair; once an element is raised to `prev`, the repaired value is still `prev`, not the original input.
- In production data repair pipelines, monotonic constraints often admit online greedy correction, which avoids buffering and makes behavior composable under streaming load.

## 🚀 Variations & Further Practice
- **Make the array strictly increasing instead of nondecreasing.** The twist is that each repaired value must be at least `prev + 1`, which changes the running threshold logic.
- **Allow both increments and decrements with minimum total adjustment.** This becomes a different optimization problem, often tied to isotonic regression or median-based cost models.
- **Return the repaired array, not just the cost, under streaming or in-place constraints.** Same greedy core, but now mutation strategy, memory ownership, and output interface matter.