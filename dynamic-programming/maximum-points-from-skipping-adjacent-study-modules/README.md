# Maximum Points from Skipping Adjacent Study Modules

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `points`, choose a subset of modules to maximize total score, with one hard constraint: you cannot take two adjacent modules. Return the maximum achievable sum, or `0` for an empty array. The problem is non-trivial because each local choice affects the next one: taking module `i` immediately forbids `i + 1`, so greedy selection by largest value does not reliably produce the global optimum.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize value under local exclusion constraints. Examples include ad-slot selection where adjacent placements cannibalize click-through, batch job schedulers that avoid back-to-back resource spikes, streaming pipelines that skip neighboring expensive transforms under latency budgets, and portfolio-style ranking where nearby candidates are mutually suppressive. At scale, naive greedy logic creates unstable or suboptimal allocations because it ignores downstream opportunity cost. Dynamic programming gives deterministic optimality with linear cost, which matters when this decision is embedded in hot paths, recommendation loops, or repeated planning jobs across millions of entities.

## 🔍 Problem Statement
You are given an integer array `points` where `points[i]` is the score earned by completing the `i`-th study module. You may complete any subset of modules, but you cannot complete both module `i` and module `i + 1`. The goal is to return the maximum total points obtainable under that rule.

If the array is empty, return `0`.

Constraints:

- `0 <= points.length <= 100`
- `0 <= points[i] <= 1000`

Examples:

- `points = [4, 2, 7, 3, 9]` → `20`  
  Complete modules with scores `4`, `7`, and `9`.

- `points = [5, 1, 1, 5]` → `10`  
  Complete the first and last modules.

The key constraint is adjacency exclusion. That dependency between neighboring choices rules out simple greedy strategies and points directly to dynamic programming over prefixes of the array.

## 🪜 How to Solve This
1. Read the constraint carefully → the decision at index `i` depends only on whether we took `i - 1`. That is a strong signal for prefix-based dynamic programming.

2. Reframe the problem → instead of asking “should I take this module?”, ask “what is the best score achievable up to this index?”

3. At each module, there are only two meaningful choices:
   - **Skip it** → keep the best score from the previous module.
   - **Take it** → add `points[i]` to the best score from two modules earlier.

4. That gives the recurrence:  
   `best[i] = max(best[i - 1], best[i - 2] + points[i])`

5. Base cases matter:
   - Empty array → `0`
   - One element → that element
   - Two elements → the larger of the two

6. Notice we only ever need the previous two results, not the full table. That lets us compress space to constant memory while keeping the same logic.

This is the standard “include or exclude current item under adjacency constraints” dynamic programming pattern.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Dynamic Programming on a linear array.**  
   The problem has optimal substructure: the best answer for prefix `0..i` can be built from optimal answers to smaller prefixes. The adjacency rule creates a one-step dependency, which is exactly what DP handles well.

2. **Define the state.**  
   Let `dp[i]` be the maximum points obtainable from modules `0` through `i`.  
   Invariant: after computing `dp[i]`, it represents the globally optimal score for that prefix, not just a locally good choice.

3. **Write the transition.**  
   For module `i`, either:
   - skip it, yielding `dp[i - 1]`
   - take it, yielding `dp[i - 2] + points[i]`  
   So: `dp[i] = max(dp[i - 1], dp[i - 2] + points[i])`  
   This is correct because every valid solution for prefix `i` must fall into exactly one of those two cases.

4. **Handle base cases explicitly.**  
   - `n = 0` → `0`
   - `n = 1` → `points[0]`
   - `dp[0] = points[0]`
   - `dp[1] = max(points[0], points[1])`  
   These anchor the recurrence and prevent invalid indexing.

5. **Optimize space.**  
   Since `dp[i]` depends only on `dp[i - 1]` and `dp[i - 2]`, keep two rolling values instead of an array.  
   Invariant: before processing index `i`, the two variables store the optimal results for prefixes ending at `i - 2` and `i - 1`.

6. **Return the final prefix optimum.**  
   After the last index, the maintained value is the maximum achievable score for the full array. This yields linear time, constant extra space, and deterministic optimality.

## 📊 Worked Example
Take `points = [4, 2, 7, 3, 9]`.

| i | points[i] | skip = best up to i-1 | take = best up to i-2 + points[i] | best |
|---|-----------|------------------------|------------------------------------|------|
| 0 | 4         | —                      | —                                  | 4    |
| 1 | 2         | 4                      | 2                                  | 4    |
| 2 | 7         | 4                      | 4 + 7 = 11                         | 11   |
| 3 | 3         | 11                     | 4 + 3 = 7                          | 11   |
| 4 | 9         | 11                     | 11 + 9 = 20                        | 20   |

Trace:

1. Start with module `0`: best is `4`.
2. At module `1`, taking `2` is worse than keeping `4`.
3. At module `2`, taking `7` plus prior non-adjacent best (`4`) gives `11`.
4. At module `3`, skipping preserves `11`.
5. At module `4`, take `9` plus best through module `2` (`11`) to reach `20`.

Final answer: `20`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because it processes each module once and performs constant work per index: one addition and one `max` comparison. At `10^6` elements this is still practical in a hot loop; at `10^9`, linear time becomes throughput-bound and requires partitioning or a different problem formulation.

### Space Complexity
The full DP table uses `O(n)` space, owned by the `dp` array. This can be reduced to `O(1)` by storing only the previous two states, since no older values are needed. The trade-off is losing the full decision history unless you reconstruct choices separately.

## 💡 Key Takeaways
- If a problem asks for a maximum over a linear sequence with a “cannot take adjacent items” rule, think one-dimensional dynamic programming immediately.
- When each choice is binary — take current or skip current — and affects only nearby positions, prefix DP with a small recurrence is usually the right abstraction.
- The most common bug is mishandling arrays of length `0`, `1`, or `2`; define those cases before writing the recurrence.
- Another frequent trap is indexing `i - 2` without guarding early positions, especially when converting between full-table and rolling-state implementations.
- The production lesson is broader than this toy problem: local exclusion constraints often look greedy but are really small-state optimization problems, and modeling the state explicitly prevents unstable heuristics.

## 🚀 Variations & Further Practice
- **House Robber II**: same recurrence, but the array is circular, so the first and last elements are also adjacent; the twist is splitting into two linear DP runs.
- **Delete and Earn**: values map into a frequency-weighted number line where taking value `x` blocks `x - 1` and `x + 1`; the harder part is transforming the input before applying the same DP pattern.
- **Paint House / Min Cost Climbing variants**: adjacent decisions remain coupled, but the state expands beyond binary include/exclude into multiple mutually exclusive choices per position.