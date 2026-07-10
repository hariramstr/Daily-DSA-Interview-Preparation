# Maximum Revenue from Ads with Cooling Gap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `revenue` and an integer `gap`, choose a subset of slots that maximizes total revenue under a spacing constraint: after selecting slot `i`, the next selected slot must satisfy `j > i + gap`. Return the maximum achievable sum. The challenge is that local greedy choices fail: taking a high-value slot can block several future opportunities, so the solution must evaluate include-vs-skip tradeoffs efficiently across up to 200,000 positions.

## 🌍 Engineering Impact
This pattern shows up anywhere a profitable action disables nearby actions for a fixed window. Examples include ad pacing on media timelines, push-notification throttling, campaign budget allocation with cooldowns, CPU job scheduling with recovery intervals, and stream processors that suppress duplicate or correlated events for a retention window. At scale, brute force or backtracking collapses immediately, and greedy heuristics leave money on the table. A dynamic programming formulation turns a combinatorial search into a linear pass, which is the difference between an offline optimizer that can run inside production SLAs and one that cannot.

## 🔍 Problem Statement
You are given:

- `revenue[i]`: revenue earned by placing an ad in slot `i`
- `gap`: the number of immediately following slots that must be skipped after choosing any slot

If you choose slot `i`, the next chosen slot must be at an index strictly greater than `i + gap`. You may skip any slot, including one with positive revenue. Some entries may be `0`.

Return the maximum total revenue over all valid subsets.

Constraints:

- `1 <= revenue.length <= 200000`
- `0 <= revenue[i] <= 1000000000`
- `0 <= gap < revenue.length`

Examples:

- `revenue = [5, 1, 2, 10, 6, 2], gap = 1` → `17`
- `revenue = [4, 7, 3, 9, 2, 8], gap = 2` → `15`

The decisive constraint is input size: `O(n^2)` is not viable, so the solution must be near-linear.

## 🪜 How to Solve This
1. Read the rule carefully → choosing slot `i` blocks a fixed range of future slots, so each decision affects only a bounded future boundary.

2. Ask what the optimal subproblem is → “What is the best revenue achievable considering slots up to index `i`?” That naturally suggests dynamic programming over the timeline.

3. At each slot, there are only two meaningful choices:
   - skip it → revenue stays whatever was best up to `i - 1`
   - take it → add `revenue[i]` plus the best result from the last non-conflicting slot

4. The last non-conflicting slot before `i` is `i - gap - 1`. That fixed jump is the key simplification: no search structure is needed, just indexed lookup.

5. This yields the recurrence:
   - `dp[i] = max(dp[i - 1], revenue[i] + dp[i - gap - 1])`
   - with out-of-bounds DP values treated as `0`

6. Once that recurrence is clear, implementation becomes a linear scan. The problem is not “pick profitable slots”; it is “optimize over include/skip states with exclusion distance,” which is classic 1D DP.

## 🧩 Algorithm Walkthrough
1. **Define the DP state**  
   Use **1D Dynamic Programming** where `dp[i]` means the maximum revenue obtainable using slots in range `[0..i]`. This is the right abstraction because the decision at `i` depends only on previously solved prefixes, not on arbitrary subsets.

2. **Initialize base behavior**  
   For indices before the array starts, treat the best revenue as `0`. This avoids special-case branching when `i - gap - 1 < 0`. The invariant is: every `dp[i]` represents a valid optimal solution for its prefix.

3. **Evaluate the skip option**  
   If slot `i` is skipped, the best value remains `dp[i - 1]`. This preserves optimality because skipping introduces no new constraints and cannot improve on the best prior prefix except by carrying it forward.

4. **Evaluate the take option**  
   If slot `i` is chosen, the previous eligible slot must be at most `i - gap - 1`. So the take value is:
   `revenue[i] + (dp[i - gap - 1] if valid else 0)`.  
   This is correct because any valid solution including `i` must combine `i` with an optimal solution from the largest non-conflicting prefix.

5. **Take the better of the two**  
   Set `dp[i] = max(skip, take)`. The invariant after this step: `dp[i]` is the mathematically optimal revenue for prefix `[0..i]`.

6. **Return the final answer**  
   After one left-to-right pass, `dp[n - 1]` is the global optimum. Since each index is processed once with O(1) work, the algorithm scales linearly and is robust for the upper constraint bound.

## 📊 Worked Example
Use `revenue = [5, 1, 2, 10, 6, 2]`, `gap = 1`.

`prev = i - gap - 1 = i - 2`

| i | revenue[i] | skip = dp[i-1] | take = revenue[i] + dp[i-2] | dp[i] |
|---|------------|----------------|------------------------------|-------|
| 0 | 5          | 0              | 5 + 0 = 5                    | 5     |
| 1 | 1          | 5              | 1 + 0 = 1                    | 5     |
| 2 | 2          | 5              | 2 + 5 = 7                    | 7     |
| 3 | 10         | 7              | 10 + 5 = 15                  | 15    |
| 4 | 6          | 15             | 6 + 7 = 13                   | 15    |
| 5 | 2          | 15             | 2 + 15 = 17                  | 17    |

Result: `17`.

Interpretation: the optimal set is slots `0, 3, 5`, which respects the cooling gap because each chosen index is more than `1` away from the previous chosen slot.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = revenue.length`. Each slot is processed once, and each DP transition does constant work: one prior lookup, one addition, and one `max`. At `10^6` elements this is routine; at `10^9`, even linear time becomes operationally expensive, so the asymptotic improvement over quadratic is necessary but not sufficient.

### Space Complexity
`O(n)` for the DP array storing the best value for each prefix. The array owns essentially all extra space. It can be reduced only if you still preserve access to `dp[i - gap - 1]`; with arbitrary `gap`, that generally means retaining a rolling window or equivalent indexed history.

## 💡 Key Takeaways
- If each choice disables a fixed future range and you are maximizing value, think “include vs skip over prefixes” before thinking greedy.
- When the dependency is “best answer up to the last non-conflicting index,” that is a strong signal for 1D dynamic programming.
- The legality boundary is `i - gap - 1`, not `i - gap`; getting this wrong admits invalid adjacent selections.
- Use a 64-bit integer type for the DP state: `200000 * 10^9` exceeds 32-bit range comfortably.
- In production optimization systems, fixed-window exclusion constraints often collapse from combinatorial search to linear DP once you model the state as an optimal prefix.

## 🚀 Variations & Further Practice
- **Variable cooling gap per slot:** each slot `i` has its own exclusion length, so the previous compatible index is no longer a fixed offset and may require preprocessing or binary search.
- **Circular timeline:** first and last slots also conflict within the gap window, which breaks the simple prefix recurrence and typically requires case-splitting.
- **Select exactly `k` ads:** adds a cardinality constraint, turning the state into `dp[i][k]` or requiring more advanced optimization techniques.