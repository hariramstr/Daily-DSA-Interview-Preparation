# Maximum Score from Choosing One Value Per Day Range

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** arrays, dynamic-programming, optimization

---

## 🗂 Problem Overview
Given an integer array `values`, choose a subset of indices such that no two chosen indices are adjacent, and maximize the sum of their values. You may skip any index, including all of them, which matters because values can be negative. Return the maximum achievable total score. The challenge is not selection itself, but making globally optimal choices under local exclusion constraints in better than quadratic time for arrays up to `100000` elements.

## 🌍 Engineering Impact
This pattern shows up anywhere a local action blocks neighboring capacity: job schedulers with cooldown windows, ad-serving systems avoiding adjacent impressions, maintenance planners preventing back-to-back resource contention, and stream processors selecting profitable events under exclusion rules. At small scale, greedy heuristics often look fine; at production scale, they silently leave money or throughput on the table because local maxima are not global maxima. The dynamic-programming formulation gives deterministic optimality in linear time, which is the difference between a toy heuristic and something safe to embed in ranking, planning, or budget-allocation paths.

## 🔍 Problem Statement
You are given an integer array `values` where `values[i]` is the score available on day `i`. Build a schedule by selecting days such that no two selected days are adjacent. If you choose day `i`, you cannot choose `i - 1` or `i + 1`.

Return the maximum total score obtainable.

Key details:
- `1 <= values.length <= 100000`
- `-10000 <= values[i] <= 10000`
- Values may be positive, zero, or negative
- Skipping all days is allowed, so the answer can be `0`

Examples:

- `values = [4, 2, 7, 9, 3]` → `14`  
  Choose days `0, 2, 4` for `4 + 7 + 3 = 14`

- `values = [-5, -1, -8]` → `0`  
  Best choice is to skip every day

The array size rules out `O(n^2)` exploration of all valid subsets, so the solution must exploit overlapping subproblems.

## 🪜 How to Solve This
1. Start with the decision at a single day → for each index, there are only two meaningful choices: take it or skip it.

2. If you **take** day `i`, day `i - 1` becomes unavailable → the best total must be `values[i] + best up to i-2`.

3. If you **skip** day `i`, nothing new is constrained → the best total is simply `best up to i-1`.

4. That gives the recurrence:  
   `dp[i] = max(dp[i-1], dp[i-2] + values[i])`

5. Notice what this means structurally → the optimal answer for a prefix depends only on the previous two prefixes. That is the classic signal for linear dynamic programming over an array.

6. Negative values change the base case, not the pattern → because skipping all days is legal, the running best should never drop below `0`.

7. Once you see “choose element, exclude neighbors, maximize total,” this is the non-adjacent selection DP pattern. No sorting, no backtracking, no nested scans — just one pass maintaining the best answer for the last two positions.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: linear dynamic programming over prefixes.**  
   The problem is equivalent to computing the best score for every prefix of the array. The invariant is: after processing index `i`, we know the maximum score achievable using only days `0..i`.

2. **Define the state.**  
   Let `dp[i]` be the maximum total score from days `0..i` with the non-adjacent constraint. This is correct because any valid schedule over the full array is built from valid schedules over prefixes.

3. **Derive the transition.**  
   For day `i`, either:
   - skip it → score remains `dp[i-1]`
   - take it → score becomes `dp[i-2] + values[i]`  
   So: `dp[i] = max(dp[i-1], dp[i-2] + values[i])`

4. **Handle base cases carefully.**  
   Because skipping everything is allowed:
   - `dp[-1] = 0` conceptually
   - `dp[0] = max(0, values[0])`  
   This prevents negative arrays from producing negative answers.

5. **Compress space.**  
   The recurrence depends only on `i-1` and `i-2`, so a full DP array is unnecessary. Maintain:
   - `prev2` = best up to `i-2`
   - `prev1` = best up to `i-1`

6. **Iterate once left to right.**  
   For each value `v`, compute `current = max(prev1, prev2 + v)`, then shift:
   `prev2 = prev1`, `prev1 = current`  
   The invariant remains: `prev1` is always the best score for the processed prefix.

7. **Return `prev1`.**  
   This is the optimal answer after the final index. The abstraction is **Dynamic Programming with rolling state**, which is the right fit because local choices create overlapping subproblems with constant-width dependency.

## 📊 Worked Example
Example: `values = [4, 2, 7, 9, 3]`

| i | value | take = prev2 + value | skip = prev1 | current | new prev2 | new prev1 |
|---|------:|---------------------:|-------------:|--------:|----------:|----------:|
| 0 | 4     | 0 + 4 = 4            | 0            | 4       | 0         | 4         |
| 1 | 2     | 0 + 2 = 2            | 4            | 4       | 4         | 4         |
| 2 | 7     | 4 + 7 = 11           | 4            | 11      | 4         | 11        |
| 3 | 9     | 4 + 9 = 13           | 11           | 13      | 11        | 13        |
| 4 | 3     | 11 + 3 = 14          | 13           | 14      | 13        | 14        |

Trace interpretation:
- At each step, `take` means include the current day and combine it with the best non-conflicting prefix.
- `skip` means preserve the best schedule already found.
- Final answer is `14`, achieved by choosing days `0, 2, 4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is `values.length`. The algorithm performs a single left-to-right pass, and each iteration does constant work: one addition and one max comparison. At `10^6` elements this is routine; at `10^9`, linear time is still expensive, but fundamentally better than any quadratic alternative, which is completely infeasible.

### Space Complexity
`O(1)` auxiliary space with rolling variables. The only extra memory is the pair of DP states representing the previous two prefixes. A full `O(n)` DP array is possible for debugging or reconstruction, but unnecessary if only the maximum score is required.

## 💡 Key Takeaways
- If the problem says “choose items for maximum total, but choosing one blocks adjacent items,” that is a strong signal for non-adjacent-selection dynamic programming.
- If each decision depends only on a small fixed neighborhood and asks for a global optimum, think prefix DP before considering greedy strategies.
- The base case must allow `0`, otherwise all-negative arrays incorrectly return a negative score.
- When compressing DP state, update order matters: compute `current` before overwriting `prev1` or `prev2`.
- In production planning systems, local exclusion constraints often look greedy but are really DP problems; recognizing that early prevents shipping heuristics that degrade revenue or utilization.

## 🚀 Variations & Further Practice
- **Circular day range:** first and last days are also adjacent. The twist is breaking the cycle into two linear cases: include-first/exclude-last or exclude-first/include-last.
- **Recover the chosen days:** return both maximum score and the actual schedule. The harder part is reconstructing decisions, which usually requires storing predecessor choices or a full DP array.
- **Cooldown of `k` days instead of 1:** choosing day `i` blocks the next `k` days. The recurrence generalizes to `dp[i] = max(dp[i-1], dp[i-k-1] + values[i])`, changing the dependency window and implementation details.