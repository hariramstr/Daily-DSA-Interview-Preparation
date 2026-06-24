# Maximum Coins from Non-Adjacent Arcade Machines

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `coins`, choose a subset of machines such that no two chosen machines are adjacent, and return the maximum total coins collectible. The input is a single integer array; the output is one integer. The non-trivial constraint is local exclusion: taking machine `i` forbids `i-1` and `i+1`, so greedy choices based on the current machine value alone can block a better global total.

## 🌍 Engineering Impact
This pattern shows up anywhere a local action invalidates neighboring actions. In ad serving, selecting one campaign may suppress adjacent inventory slots due to pacing or policy constraints. In streaming pipelines, scheduling one expensive stage can force cooldown windows around nearby tasks. In storage and networking, maintenance windows on one shard or link often exclude adjacent resources in a topology or timeline. Without a dynamic-programming formulation, teams reach for brittle greedy heuristics that look fine in small tests but degrade revenue, throughput, or utilization at scale. The value here is disciplined optimization over prefixes with predictable runtime and straightforward correctness reasoning.

## 🔍 Problem Statement
You are given an integer array `coins` where `coins[i]` is the number of coins in the `i`th arcade machine. If you collect from machine `i`, you cannot collect from machines `i-1` or `i+1` on the same night. Return the maximum total coins collectible without taking two adjacent machines.

Constraints:

- `1 <= coins.length <= 100`
- `0 <= coins[i] <= 1000`

Examples:

- `coins = [4, 2, 7, 9, 3]` → `14`
- `coins = [10, 1, 1, 10]` → `20`

Edge cases matter:

- One machine: answer is that machine’s value.
- Machines may contain `0`, so “take something” is not always useful.
- The key algorithmic constraint is adjacency exclusion, which makes local greedy selection unreliable and naturally leads to dynamic programming over array prefixes.

## 🪜 How to Solve This
1. Read the constraint carefully → choosing one machine blocks its immediate neighbors. That means each position creates a binary decision: **take** or **skip**.

2. Ask what information is needed at index `i` → only the best answer up to earlier positions matters, not the exact set of chosen machines. That is a strong signal for dynamic programming.

3. Define the subproblem → let the best answer for the first `i` machines be the maximum coins collectible from that prefix.

4. For each machine, there are only two valid options:
   - **Skip it** → total stays as the best answer up to the previous machine.
   - **Take it** → add `coins[i]` to the best answer up to `i-2`, because `i-1` becomes unavailable.

5. This gives the recurrence:
   - `best[i] = max(best[i-1], best[i-2] + coins[i])`

6. Notice the recurrence depends on only the previous two states → you do not need a full DP array. Two rolling variables are enough.

That reasoning is the whole solution: optimize over prefixes, carry only the minimal state, and choose the better of take-vs-skip at each step.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Dynamic Programming on a linear array.**  
   This is the canonical “non-adjacent selection” problem. The right abstraction is prefix optimization: compute the best answer for increasingly larger prefixes of the array. That works because the decision at index `i` only interacts with `i-1`.

2. **Define the state.**  
   Let `dp[i]` be the maximum coins collectible from machines `0..i`. This is correct because any optimal solution for prefix `0..i` must either include machine `i` or exclude it.

3. **Write the transition.**  
   - If machine `i` is skipped, the result is `dp[i-1]`.  
   - If machine `i` is taken, machine `i-1` must be skipped, so the result is `dp[i-2] + coins[i]`.  
   Therefore: `dp[i] = max(dp[i-1], dp[i-2] + coins[i])`.  
   The invariant is: after processing index `i`, `dp[i]` is the optimal answer for that prefix.

4. **Handle base cases explicitly.**  
   - `dp[0] = coins[0]`
   - `dp[1] = max(coins[0], coins[1])`  
   These anchor the recurrence and prevent invalid indexing.

5. **Optimize space.**  
   Since each state depends only on the previous two, keep:
   - `prev2 = dp[i-2]`
   - `prev1 = dp[i-1]`  
   For each machine, compute `current = max(prev1, prev2 + coins[i])`, then shift the window forward. The invariant remains the same, but memory drops from `O(n)` to `O(1)`.

6. **Return the last computed optimum.**  
   After the final index, `prev1` holds the maximum coins collectible across the full array.

## 📊 Worked Example
Use `coins = [4, 2, 7, 9, 3]`.

| i | coins[i] | take = prev2 + coins[i] | skip = prev1 | current | Meaning |
|---|----------|--------------------------|--------------|---------|---------|
| 0 | 4        | —                        | —            | 4       | Best for `[4]` |
| 1 | 2        | —                        | —            | 4       | Best for `[4,2]` is `4` |
| 2 | 7        | 4 + 7 = 11               | 4            | 11      | Take `7` with `4` |
| 3 | 9        | 4 + 9 = 13               | 11           | 13      | Better to take `9` with prior non-adjacent best |
| 4 | 3        | 11 + 3 = 14              | 13           | 14      | Take `3` on top of best through index 2 |

Trace with rolling state:

- Start: `prev2 = 4`, `prev1 = 4`
- At `7`: `current = 11` → update to `prev2 = 4`, `prev1 = 11`
- At `9`: `current = 13` → update to `prev2 = 11`, `prev1 = 13`
- At `3`: `current = 14` → update to `prev2 = 13`, `prev1 = 14`

Answer: `14`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = coins.length`, because each machine is processed exactly once and each step performs constant-time arithmetic and comparisons. At `10^6` elements this is still practical in most runtimes; at `10^9`, linear time becomes throughput-bound and data movement dominates, even though the algorithm is asymptotically optimal.

### Space Complexity
`O(1)` with the rolling-state optimization, since only two prior DP values are stored. A full DP array would use `O(n)` space, which can help with debugging or reconstruction of chosen indices, but is unnecessary if only the maximum total is required.

## 💡 Key Takeaways
- If a problem says “choose items for maximum value” and “cannot take adjacent items,” it is almost always a linear dynamic-programming recurrence over prefixes.
- If each decision reduces to “take current” vs “skip current,” look for a two-state dependency on `i-1` and `i-2` rather than backtracking or greedy selection.
- The most common bug is incorrect base-case handling for arrays of length `1` or `2`, especially when converting from DP array form to rolling variables.
- Another frequent trap is updating rolling variables in the wrong order, which silently corrupts the `dp[i-2]` value needed for the take-case.
- The transferable design insight is to compress state aggressively once you prove the recurrence’s dependency boundary; many production optimizers need only a narrow historical window, not full history retention.

## 🚀 Variations & Further Practice
- **House Robber II**: the machines are arranged in a circle, so the first and last are also adjacent; the twist is splitting into two linear DP runs.
- **Delete and Earn**: taking value `x` removes access to `x-1` and `x+1`; the twist is transforming frequency-weighted values into a non-adjacent DP over sorted keys.
- **Maximum sum with no three consecutive elements**: adjacency rules are relaxed but extended; the twist is a larger recurrence with more state and more careful transition design.