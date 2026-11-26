# Maximum Reward from Adjacent Booth Visits

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an integer array `rewards`, choose a subset of booth rewards such that no two chosen booths are adjacent, and return the maximum total reward. The input is a single array; the output is one integer: the best achievable sum under the adjacency constraint. What makes this non-trivial is that each local choice affects the next position, so greedy selection by largest value does not work reliably.

## 🌍 Engineering Impact
This pattern shows up whenever adjacent selections conflict: ad-slot pacing across neighboring inventory buckets, batch scheduling where consecutive jobs contend for the same resource, edge-device polling windows, and stream-processing stages that cannot execute back-to-back due to cooldown or overlap constraints. At small scale, brute force is tolerable; at production scale, it explodes combinatorially and obscures correctness. The dynamic programming formulation turns a globally constrained optimization into a linear scan with stable memory behavior. That matters in hot paths, embedded runtimes, and services where predictable latency and simple invariants are more valuable than clever but fragile heuristics.

## 🔍 Problem Statement
You are given an array `rewards` where `rewards[i]` is the reward for visiting booth `i`. You may visit any set of booths as long as no two visited booths are adjacent. Return the maximum total reward possible.

Constraints:

- `1 <= rewards.length <= 100`
- `0 <= rewards[i] <= 1000`

Examples:

- `rewards = [4, 2, 7, 9, 3]` → `13`
- `rewards = [2, 1, 1, 2]` → `4`

Edge cases matter even in an easy DP:
- One booth: answer is that booth’s reward.
- All zeros: answer is `0`.
- Large values do not change the strategy, only the totals.

The key constraint is adjacency: taking booth `i` forbids booth `i - 1`. That dependency on the immediately previous state is exactly what drives the dynamic programming approach and makes an `O(n)` solution the natural fit.

## 🪜 How to Solve This
1. Read the constraint carefully → the only thing that matters about booth `i` is whether booth `i - 1` was taken.
2. That means each position has two conceptual options:
   - **Skip booth `i`** → best total stays whatever was best through `i - 1`.
   - **Take booth `i`** → add `rewards[i]` to the best total through `i - 2`.
3. So the problem is not “which booths look large?” but “what is the best answer up to each index?”
4. Define a running best: `dp[i] = max reward using booths[0..i]`.
5. Then the recurrence becomes obvious:
   - `dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])`
6. This works because every valid solution ending at `i` must either include `i` or exclude it. There is no third case.
7. Once that recurrence is clear, implementation is straightforward:
   - use a DP array for clarity, or
   - keep only the previous two states for `O(1)` extra space.

This is classic one-dimensional dynamic programming: local decision, overlapping subproblems, linear dependency chain.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: 1D Dynamic Programming.**  
   The problem has a linear structure and a local exclusion rule: choosing index `i` blocks only `i - 1`. That makes DP the right abstraction because the optimal answer at each position depends on a constant-size history.

2. **Define the state.**  
   Let `dp[i]` be the maximum reward obtainable from booths `0..i`.  
   Invariant: after computing `dp[i]`, it represents the optimal valid total for that prefix.

3. **Establish base cases.**  
   - `dp[0] = rewards[0]`
   - `dp[1] = max(rewards[0], rewards[1])`  
   Why correct: with one booth, either take it or not; with two booths, adjacency prevents taking both.

4. **Apply the recurrence for each `i >= 2`.**  
   Compute:
   - `skip = dp[i - 1]`
   - `take = dp[i - 2] + rewards[i]`
   - `dp[i] = max(skip, take)`  
   Why correct: every optimal solution for prefix `0..i` either excludes booth `i` or includes it. If it includes `i`, the best compatible prefix ends at `i - 2`.

5. **Maintain the invariant through the scan.**  
   After each update, `dp[i]` is optimal for the prefix because both exhaustive legal choices were considered and the better one was retained.

6. **Return the final state.**  
   `dp[n - 1]` is the optimal answer for the full array.

7. **Optimize space if needed.**  
   Since each state depends only on the previous two, replace the array with two rolling variables. Same recurrence, same correctness, lower memory footprint.

## 📊 Worked Example
Example: `rewards = [4, 2, 7, 9, 3]`

| i | rewards[i] | take = dp[i-2] + rewards[i] | skip = dp[i-1] | dp[i] |
|---|------------|------------------------------|----------------|-------|
| 0 | 4          | —                            | —              | 4     |
| 1 | 2          | —                            | —              | 4     |
| 2 | 7          | 4 + 7 = 11                   | 4              | 11    |
| 3 | 9          | 4 + 9 = 13                   | 11             | 13    |
| 4 | 3          | 11 + 3 = 14                  | 13             | 14    |

Trace:
1. Start with `dp[0] = 4`.
2. For index `1`, best is `max(4, 2) = 4`.
3. At index `2`, taking `7` with booth `0` gives `11`, better than skipping.
4. At index `3`, taking `9` with best through `1` gives `13`.
5. At index `4`, taking `3` with best through `2` gives `14`.

Final answer: `14`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because each booth is processed once, and each step does constant work: one addition and one max comparison. At `10^6` elements this is still practical in a single pass; at `10^9`, linear time becomes throughput-bound and input scanning dominates.

### Space Complexity
The DP-array version uses `O(n)` space to store one result per index. This can be reduced to `O(1)` by keeping only the previous two states, since the recurrence depends only on `i - 1` and `i - 2`. The trade-off is slightly less inspectable intermediate state.

## 💡 Key Takeaways
- If a problem asks for an optimal subset from a linear array with a “cannot take adjacent elements” rule, think 1D dynamic programming immediately.
- If each decision depends only on a fixed-size prefix history, look for a recurrence over indices rather than a greedy rule.
- The most common bug is incorrect base-case handling for arrays of length `1` or `2`.
- Another common trap is misreading the recurrence and combining `rewards[i]` with `dp[i - 1]`, which illegally allows adjacent selections.
- In production code, this pattern is a reminder that local exclusion constraints often collapse into compact state machines with predictable runtime and memory behavior.

## 🚀 Variations & Further Practice
- **House Robber II**: same recurrence, but the array is circular, so the first and last elements are also adjacent; this forces decomposition into two linear cases.
- **Delete and Earn**: values, not positions, create conflicts; you first aggregate equal numbers, then run the same adjacency DP over the value domain.
- **Paint House / Min Cost Climbing Stairs**: similar linear DP structure, but the state transition minimizes cost or tracks multiple prior states instead of a single exclusion rule.