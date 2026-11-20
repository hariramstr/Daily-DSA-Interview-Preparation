# Maximum Reward from Choosing Rested Training Days

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an integer array `rewards`, choose a subset of indices such that no two chosen days are adjacent, and the sum of their values is maximized. Return that maximum total reward. Negative values matter because training can be harmful on some days, and the athlete may skip all days, making `0` a valid answer. The non-trivial part is balancing local choices against future opportunities under the no-consecutive-days constraint.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize value under local exclusion constraints. Examples include ad serving with cooldown windows, job schedulers that avoid back-to-back resource contention, portfolio rebalancing with lockout periods, and streaming pipelines that suppress adjacent expensive operations. At scale, greedy heuristics fail because a locally strong choice can block a better downstream combination. Dynamic programming gives deterministic optimality in one pass, which matters when planners run repeatedly inside larger optimization loops, recommendation systems, or operational scheduling services with tight latency budgets.

## 🔍 Problem Statement
You are given an integer array `rewards` where `rewards[i]` is the benefit of training on day `i`. You may train on any subset of days, but cannot train on two consecutive days because each training day requires a recovery day immediately after.

Return the maximum total reward achievable.

Constraints:

- `1 <= rewards.length <= 100000`
- `-10000 <= rewards[i] <= 10000`
- Target runtime: `O(n)`
- Prefer `O(1)` extra space

If all values are negative, the athlete may choose no days, so the correct answer is `0`.

Examples:

- `rewards = [5, 1, 1, 5]` → `10`
  - Train on days `0` and `3`
- `rewards = [4, -2, 7, 3, -1, 8]` → `19`
  - One optimal plan is days `0`, `2`, and `5`

The array can be large enough that quadratic search is not viable, which is the constraint that forces a linear-state dynamic programming approach.

## 🪜 How to Solve This
1. Read the constraint carefully → choosing day `i` forbids only day `i - 1`, not all earlier days. That means the decision at each position depends on a very small amount of prior state.

2. Ask what “best up to index `i`” means → for each day, there are only two meaningful options:
   - skip this day and keep the best total from yesterday
   - take this day and add its reward to the best total from two days ago

3. That naturally suggests a recurrence:
   - `best[i] = max(best[i - 1], best[i - 2] + rewards[i])`

4. Handle negatives correctly → because skipping all days is allowed, the running best should never be forced below `0`.

5. Notice the recurrence only needs the previous two results → no need for a full DP array unless you want traceability. Two rolling variables are enough.

6. The reason this works is structural: every valid plan ending at `i` either includes day `i` or excludes it. Those cases are mutually exclusive and cover all possibilities, so the recurrence is complete.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Dynamic Programming on a linear array with adjacency exclusion.**  
   This is the classic “take or skip” formulation. The right abstraction is DP because each position has an optimal substructure: the best answer for a prefix can be built from smaller prefixes.

2. **Define the state.**  
   Let `dp[i]` be the maximum reward obtainable from days `0..i`. This state is sufficient because the future only cares about the best value so far, not the exact chosen set.

3. **Write the transition.**  
   For day `i`, either:
   - skip it → total stays `dp[i - 1]`
   - take it → total becomes `dp[i - 2] + rewards[i]`  
   So:
   `dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])`

4. **Set base conditions to preserve the “skip everything” option.**  
   Use:
   - `dp[-1] = 0`
   - `dp[0] = max(0, rewards[0])`  
   This ensures negative prefixes do not contaminate later results.

5. **Compress space.**  
   Maintain two rolling values:
   - `prev2` = best up to `i - 2`
   - `prev1` = best up to `i - 1`  
   For each reward `x`, compute `curr = max(prev1, prev2 + x)`, then shift forward.

6. **Maintain the invariant.**  
   After processing index `i`, `prev1` equals the optimal reward for prefix `0..i`, and `prev2` equals the optimal reward for prefix `0..i-1`. Because each update considers all valid plans that either include or exclude the current day, correctness follows by induction over the array.

## 📊 Worked Example
Use `rewards = [4, -2, 7, 3, -1, 8]`.

| Day `i` | Reward | `prev2` | `prev1` | `curr = max(prev1, prev2 + reward)` |
|---|---:|---:|---:|---:|
| 0 | 4  | 0 | 0 | 4 |
| 1 | -2 | 0 | 4 | 4 |
| 2 | 7  | 4 | 4 | 11 |
| 3 | 3  | 4 | 11 | 11 |
| 4 | -1 | 11 | 11 | 11 |
| 5 | 8  | 11 | 11 | 19 |

Trace:

1. Day 0: take `4` → best is `4`.
2. Day 1: `-2` is harmful, so skip → best stays `4`.
3. Day 2: take `7` plus best through day 0 (`4`) → `11`.
4. Day 3: taking `3` would give `7`, worse than `11`, so skip.
5. Day 4: negative, skip.
6. Day 5: take `8` plus best through day 3 (`11`) → `19`.

Final answer: `19`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` because each day is processed exactly once, with constant-time work per element: one addition and one `max`. This scales comfortably to `10^6` elements in typical in-memory workloads. At `10^9`, the asymptotics remain optimal, but throughput, memory bandwidth, and batch-processing architecture become the limiting factors.

### Space Complexity
`O(1)` extra space with the rolling-state formulation. The only additional storage is a few scalar variables holding prior DP states. A full `O(n)` DP array is unnecessary unless you need reconstruction of the chosen days or debugging visibility into intermediate states.

## 💡 Key Takeaways
- If a problem says “choose elements for maximum value” and “cannot take adjacent items,” think one-dimensional DP with a take/skip recurrence immediately.
- If each decision only blocks a fixed local neighborhood, that is a strong signal that optimal prefix state can be summarized in constant space.
- The base case must preserve the option to choose nothing; otherwise all-negative inputs produce an incorrect negative answer.
- Be careful when initializing the first one or two states: off-by-one mistakes around `i - 2` are the most common bug in this pattern.
- The production lesson is broader than this problem: when local constraints create overlapping subproblems, encode the minimal state boundary and stream the optimization in one pass.

## 🚀 Variations & Further Practice
- **Circular days constraint:** first and last days are also adjacent. The twist is breaking the cycle into two linear DP runs: exclude first day or exclude last day.
- **Choose days with a cooldown of `k` days:** taking day `i` blocks the next `k` days. The recurrence generalizes from `i - 2` to `i - k - 1`.
- **Reconstruct the chosen schedule, not just the value:** same DP core, but now you must retain enough history to backtrack the actual selected indices.