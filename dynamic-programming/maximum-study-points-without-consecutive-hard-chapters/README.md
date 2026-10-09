# Maximum Study Points Without Consecutive Hard Chapters

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `points`, where `points[i]` is the reward for reviewing chapter `i`, compute the maximum total study points obtainable when no two chosen chapters are adjacent. You may skip any chapter, and the goal is to maximize the sum, not the number of chapters studied. The problem is non-trivial because each choice affects the next valid choices, so greedy local decisions can block a better global total.

## 🌍 Engineering Impact
This pattern appears anywhere a system must maximize value under local exclusion constraints. Examples include ad-slot selection where adjacent placements cannot both be shown, job scheduling with cooldown windows, storage compaction where neighboring segments cannot be rewritten together, and streaming pipelines that must avoid back-to-back expensive operators on the same shard. At scale, naive greedy selection fails because local maxima create globally suboptimal plans. Dynamic programming gives deterministic optimality, predictable runtime, and a compact state model that can be embedded in planners, schedulers, and resource allocators without combinatorial explosion.

## 🔍 Problem Statement
You are given an integer array `points` of length `n`, where `1 <= n <= 100` and `0 <= points[i] <= 1000`. Each element represents the study points earned by reviewing that chapter. Chapters must be considered in order, and you may not review two consecutive chapters in the same session.

Return the maximum total study points you can earn.

This is a classic optimization problem over a linear sequence: at each index, taking the current chapter excludes the previous one, while skipping it preserves prior options. That adjacency constraint is what drives the dynamic programming approach.

Examples:

- `Input: points = [3, 2, 5, 10, 7]`
  `Output: 15`
  Review chapters with points `3, 5, 7`.

- `Input: points = [2, 1, 4, 9]`
  `Output: 11`
  Review chapters with points `2, 9`.

Edge cases include arrays of length `1`, all-zero arrays, and cases where skipping a large local value enables a better total later.

## 🪜 How to Solve This
1. Read the constraint carefully → the only restriction is **no adjacent picks**. That means the decision at index `i` depends only on what happened at `i - 1` and `i - 2`.

2. Ask what the optimal subproblem looks like → define it as: “maximum points achievable considering chapters up to index `i`.”

3. At each chapter, there are only two meaningful choices:
   - **Skip it** → total stays whatever was best through `i - 1`.
   - **Take it** → add `points[i]` to the best total through `i - 2`.

4. That immediately gives the recurrence:
   `dp[i] = max(dp[i - 1], dp[i - 2] + points[i])`.

5. Why this works → every valid solution ending at `i` either excludes chapter `i` or includes it. There is no third case, and those two cases cover all legal outcomes.

6. Notice the recurrence only looks back two positions → full DP array is optional. Two rolling variables are enough.

7. This is the standard “pick-or-skip on a line” dynamic programming pattern. Once you see local exclusion plus additive reward, this formulation becomes the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Dynamic Programming on a linear array.**  
   The problem has optimal substructure: the best answer for prefix `0..i` can be built from best answers on smaller prefixes. The adjacency rule creates a fixed dependency window of size two, which is exactly what makes DP the right abstraction.

2. **Define state.**  
   Let `dp[i]` be the maximum study points obtainable from chapters `0..i`.  
   Invariant: after computing `dp[i]`, it represents the optimal value for that prefix under the non-adjacent constraint.

3. **Establish base cases.**  
   - `dp[0] = points[0]`
   - `dp[1] = max(points[0], points[1])`  
   These are correct because with one chapter, you either take it or not; with two chapters, you can take at most one.

4. **Apply the transition.**  
   For each `i >= 2`:
   - Skip chapter `i` → value is `dp[i - 1]`
   - Take chapter `i` → value is `dp[i - 2] + points[i]`  
   So `dp[i] = max(dp[i - 1], dp[i - 2] + points[i])`.

5. **Why the transition is complete.**  
   Any valid solution for prefix `0..i` must either include chapter `i` or exclude it. If it includes `i`, then `i - 1` is forbidden, so the remaining best prefix is `dp[i - 2]`. This partitions the search space cleanly with no overlap errors.

6. **Optimize space if desired.**  
   Since each state depends only on the previous two, track:
   - `prev2 = dp[i - 2]`
   - `prev1 = dp[i - 1]`  
   Update them iteratively.  
   Invariant: before processing `i`, `prev2` and `prev1` hold the optimal values for the two prior prefixes.

7. **Return the final optimum.**  
   The answer is `dp[n - 1]` or the final rolling value. This is globally optimal because every prefix was solved optimally using the recurrence.

## 📊 Worked Example
Use `points = [3, 2, 5, 10, 7]`.

| i | points[i] | skip = dp[i-1] | take = dp[i-2] + points[i] | dp[i] |
|---|-----------|----------------|-----------------------------|-------|
| 0 | 3         | —              | —                           | 3     |
| 1 | 2         | —              | —                           | 3     |
| 2 | 5         | 3              | 3 + 5 = 8                   | 8     |
| 3 | 10        | 8              | 3 + 10 = 13                 | 13    |
| 4 | 7         | 13             | 8 + 7 = 15                  | 15    |

Trace:
1. Start with `dp[0] = 3`.
2. For the first two chapters, best is `max(3, 2) = 3`.
3. At index `2`, taking `5` plus `dp[0]` beats skipping: `8`.
4. At index `3`, taking `10` plus `dp[1]` gives `13`.
5. At index `4`, taking `7` plus `dp[2]` gives `15`, which beats `13`.

Final answer: `15`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because each chapter is processed once and each step does constant work: one addition and one comparison. At `10^6` elements this is still operationally cheap in most runtimes; at `10^9`, linear time becomes throughput-bound and only feasible in highly optimized streaming or distributed settings.

### Space Complexity
Using a full DP array costs `O(n)` space, owned by the `dp` table. Since each state depends only on the previous two, space can be reduced to `O(1)` with rolling variables, trading away the ability to inspect or reconstruct all intermediate states directly.

## 💡 Key Takeaways
- If the input is a linear sequence and choosing one item forbids its immediate neighbor, think “pick-or-skip DP” before considering greedy logic.
- When the optimal answer for index `i` depends only on `i - 1` and `i - 2`, that is a strong signal for one-dimensional dynamic programming with rolling state.
- The most common bug is incorrect base-case handling for arrays of length `1` or `2`; define those explicitly before writing the loop.
- Another frequent trap is mixing up `dp[i - 1] + points[i]` with `dp[i - 2] + points[i]`; taking the current item must exclude the previous one.
- The production-grade insight is that local exclusion constraints often collapse an exponential search space into a tiny state machine, enabling exact optimization with predictable cost.

## 🚀 Variations & Further Practice
- **House Robber II**: same recurrence, but the array is circular, so the first and last elements are also adjacent; the twist is splitting into two linear DP runs.
- **Delete and Earn**: values rather than positions create the exclusion rule; the harder part is transforming frequency-weighted values into a linear DP domain.
- **Maximum sum with no three consecutive elements**: expands the local constraint window, requiring a richer state definition than the simple two-state recurrence here.