# Minimum Cost to Curate a Museum Exhibit Path

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Array, Optimization

---

## 🗂 Problem Overview
Given two arrays, `cost` and `premium`, compute the minimum total cost to travel from room `0` to room `n - 1`, moving only by `+1` or `+2` steps. Entering a room adds its curation cost, and every consecutive visited pair of premium rooms adds an extra penalty. The challenge is that each decision depends not just on position, but also on the immediately previous visited room, which rules out greedy local choices.

## 🌍 Engineering Impact
This pattern shows up anywhere a linear workflow allows bounded skips while transition cost depends on adjacent chosen states. Examples include streaming pipelines with optional stages, compiler optimization passes with pass-to-pass invalidation costs, warehouse picking routes with setup penalties, and media transcoding chains where consecutive premium codecs trigger expensive resets. At scale, greedy heuristics misprice transitions and produce globally suboptimal plans. Dynamic programming gives deterministic optimality in one pass, which matters when these decisions are embedded inside schedulers, planners, or cost-based optimizers serving high-throughput systems.

## 🔍 Problem Statement
You are given:

- `cost[i]`: the cost paid if room `i` is visited
- `premium[i]`: `1` if room `i` is premium, otherwise `0`
- `penalty`: extra fee charged whenever two consecutive visited rooms are both premium

The guide must start at room `0` and end at room `n - 1`. From room `i`, the next visited room may only be `i + 1` or `i + 2`, if in bounds. The total cost is the sum of all visited room costs plus the penalty for each consecutive visited pair where both endpoints are premium, even when the move is a skip of length 2.

Constraints:

- `2 <= n <= 100000`
- `0 <= cost[i] <= 100000`
- `premium[i] ∈ {0,1}`
- `0 <= penalty <= 100000`

Example 1:
- Input: `cost = [4,2,7,3,5]`, `premium = [1,0,1,1,0]`, `penalty = 6`
- Output: `14`

Example 2:
- Input: `cost = [1,9,2,8,1,3]`, `premium = [1,1,0,1,1,1]`, `penalty = 4`
- Output: `11`

The `n = 100000` upper bound eliminates exponential path exploration and pushes the solution toward linear-time DP.

## 🪜 How to Solve This
1. Read the movement rule → from any room, you can only arrive from the previous one or two rooms. That is the classic signal for 1D dynamic programming.

2. Define the state carefully → let `dp[i]` be the minimum total cost to reach room `i` while paying for every visited room and every premium-to-premium transition already taken.

3. Ask what the last move could have been → to reach `i`, the previous visited room must be either `i - 1` or `i - 2`. There are no other possibilities.

4. Price each transition exactly once → when moving from `j` to `i`, add:
   - `cost[i]`
   - plus `penalty` only if `premium[j] == 1` and `premium[i] == 1`

5. Take the cheaper predecessor →  
   `dp[i] = min(dp[i-1] + transition(i-1, i), dp[i-2] + transition(i-2, i))`

6. Initialize the base cases → `dp[0] = cost[0]`, and `dp[1]` comes only from room `0`.

7. Notice the dependency width is constant → each state depends on only two prior states, so the solution is `O(n)` time and can be reduced to `O(1)` extra space.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: linear dynamic programming.**  
   The graph is an implicit DAG over indices `0..n-1`, with edges from `i` to `i+1` and `i+2`. Because edges only move forward, the optimal path to a node depends only on optimal paths to earlier nodes.

2. **Define the DP state.**  
   Let `dp[i]` be the minimum total cost to reach room `i`. This is sufficient because the only transition-dependent information needed at step `i` is the identity of the immediate predecessor, and the recurrence enumerates both legal predecessors directly.

3. **Establish the base cases.**  
   `dp[0] = cost[0]` since the path must start there.  
   `dp[1] = dp[0] + cost[1] + premiumPenalty(0, 1)` because room `1` can only be reached from room `0`.

4. **Compute transition cost.**  
   For any legal move `j -> i`, transition cost is:  
   `cost[i] + (premium[j] == 1 && premium[i] == 1 ? penalty : 0)`  
   This is correct because room cost is paid on entry, and the premium fee applies per consecutive visited pair.

5. **Apply the recurrence.**  
   For `i >= 2`:  
   `dp[i] = min(dp[i-1] + trans(i-1, i), dp[i-2] + trans(i-2, i))`  
   Invariant: after processing index `i`, `dp[k]` is optimal for every `k <= i`.

6. **Return the terminal state.**  
   The answer is `dp[n-1]`, since every valid path must end at the last room.

7. **Optimize space if needed.**  
   Since only `dp[i-1]` and `dp[i-2]` are required, retain two rolling values instead of the full array. Same correctness, lower memory footprint.

## 📊 Worked Example
Use Example 2:

- `cost = [1,9,2,8,1,3]`
- `premium = [1,1,0,1,1,1]`
- `penalty = 4`

| i | From `i-1` | From `i-2` | `dp[i]` |
|---|---:|---:|---:|
| 0 | — | — | 1 |
| 1 | `1 + 9 + 4 = 14` | — | 14 |
| 2 | `14 + 2 = 16` | `1 + 2 = 3` | 3 |
| 3 | `3 + 8 = 11` | `14 + 8 + 4 = 26` | 11 |
| 4 | `11 + 1 + 4 = 16` | `3 + 1 = 4` | 4 |
| 5 | `4 + 3 + 4 = 11` | `11 + 3 + 4 = 18` | 11 |

Optimal path reconstruction from the winning predecessors gives `0 -> 2 -> 4 -> 5`.

Costs paid:
- Rooms: `1 + 2 + 1 + 3 = 7`
- Premium transition: `(4,5)` adds `4`

Total = `11`.

## ⏱ Complexity Analysis

### Time Complexity
The algorithm runs in `O(n)` time because each room is processed once and each state performs constant work: two candidate transitions and one minimum. At `10^6` elements this is routine in production. At `10^9`, linear time becomes operationally expensive, but it remains asymptotically optimal for exact evaluation of every state.

### Space Complexity
The full DP array uses `O(n)` space. That storage is owned entirely by the memoized state values. Because each state depends on only the previous two, space can be reduced to `O(1)` with rolling variables, at the cost of losing straightforward path reconstruction unless predecessor data is stored separately.

## 💡 Key Takeaways
- If movement is constrained to a small forward window like `+1` or `+2`, and you need a minimum total cost, think linear DP before considering graph-heavy formulations.
- If edge cost depends on the relationship between consecutive chosen states, not just the destination, model the transition explicitly in the recurrence.
- Do not forget that a skip from `i` to `i+2` still creates a consecutive visited pair for penalty purposes.
- Initialize `dp[1]` from `dp[0]` only; treating it like the general recurrence introduces an invalid `i-2` predecessor.
- In production planning systems, local step cost plus transition cost is a strong signal that optimal decisions require stateful cost accumulation rather than greedy selection.

## 🚀 Variations & Further Practice
- Allow moves of up to `k` rooms instead of only `+1` or `+2`; the harder part is optimizing the recurrence when predecessor choice expands from constant width to a sliding window.
- Add a constraint on the maximum number of premium rooms visited; this turns the state into `dp[i][usedPremium]` and forces a trade-off between path cost and resource budget.
- Make the penalty depend on the last two visited room types rather than one predecessor; this increases state dimensionality and tests whether the transition history can still be compressed efficiently.