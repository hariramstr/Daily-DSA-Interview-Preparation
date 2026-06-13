# Maximum Reward from Skipping Adjacent Milestones

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an ordered array `rewards`, choose a subset of milestone indices with no two adjacent and maximize the sum of selected values. The output is a single integer: the maximum total reward obtainable under that constraint. The non-trivial part is that local greedy choices fail, negative values are allowed, and skipping everything is valid, so the solution must balance current gain against future opportunity in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere adjacent actions compete for a shared resource window: ad-slot selection with cooldowns, batch-job scheduling on constrained workers, alert suppression in streaming pipelines, and portfolio or campaign selection where neighboring decisions cannibalize value. At scale, brute-force search or backtracking collapses immediately, while greedy heuristics silently leave money on the table. The dynamic-programming formulation gives a deterministic linear-time optimizer with constant memory, which matters in hot paths, embedded decision engines, and large-scale evaluators processing millions of ordered candidates without materializing full state histories.

## 🔍 Problem Statement
You are given an integer array `rewards` of length `n`, where `1 <= n <= 100000` and each value is in `[-10000, 10000]`. Milestones are arranged in fixed order, and attending milestone `i` yields `rewards[i]`. You may not attend two adjacent milestones, and you may skip any milestone, including all of them.

Return the maximum total reward achievable by selecting a subset of indices such that no two selected indices are adjacent.

The key edge case is negative input: if every reward is negative, the correct answer is `0`, because attending nothing is allowed.

Examples:

- `rewards = [5, 1, 2, 10, 6]` → `15`
- `rewards = [-4, -2, -7]` → `0`

The constraint that drives the algorithmic choice is `n` up to `100000`, which rules out exponential search and makes `O(n)` the target.

## 🪜 How to Solve This
1. Read the constraint carefully → adjacent selections are forbidden, but order is fixed. That means this is not a sorting or interval-reordering problem.

2. At each milestone, there are only two meaningful decisions:
   - skip it → keep the best reward seen so far
   - take it → add its reward to the best answer that ended two positions earlier

3. That immediately suggests a recurrence on prefixes:
   - best up to `i` depends only on best up to `i-1` and best up to `i-2`

4. Once you see “optimal answer for prefix `i` depends on a constant number of earlier prefixes,” this is classic one-dimensional dynamic programming.

5. Negative values change initialization: the baseline cannot be `rewards[0]`; it must allow “take nothing,” so the running best should never drop below `0`.

6. Because each state only depends on the previous two states, storing the full DP array is optional. Two rolling variables are enough.

7. The result is a single left-to-right pass with a constant-memory recurrence that is both optimal and easy to reason about.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: linear dynamic programming with rolling state.**  
   This is the same abstraction as the classic “House Robber” problem: optimize over a sequence where taking one item blocks its neighbor. The right state is “best reward achievable from the first `i` milestones.”

2. **Define the DP recurrence.**  
   Let `dp[i]` be the maximum reward using milestones `0..i`. Then:
   - skip milestone `i` → `dp[i-1]`
   - take milestone `i` → `dp[i-2] + rewards[i]`  
   So `dp[i] = max(dp[i-1], dp[i-2] + rewards[i])`.

3. **Handle the empty-selection option explicitly.**  
   Since all values may be negative, initialize so the optimum can remain `0`. Conceptually:
   - before processing anything: best is `0`
   - after first element: `max(0, rewards[0])`  
   This preserves the invariant that the running optimum is never worse than skipping everything.

4. **Compress the state to O(1) space.**  
   Only `dp[i-1]` and `dp[i-2]` are needed to compute `dp[i]`. Maintain two variables:
   - `prev2` = best up to `i-2`
   - `prev1` = best up to `i-1`

5. **Iterate left to right.**  
   For each reward `x`, compute `current = max(prev1, prev2 + x)`, then shift:
   - `prev2 = prev1`
   - `prev1 = current`  
   The invariant after each step: `prev1` is the optimal answer for the processed prefix.

6. **Return the final prefix optimum.**  
   After one pass, `prev1` is the maximum total reward under the non-adjacent constraint. Correctness follows from exhaustive local choice over the only two legal actions at each position.

## 📊 Worked Example
Use `rewards = [5, 1, 2, 10, 6]`.

| i | reward | take = prev2 + reward | skip = prev1 | current | new prev2 | new prev1 |
|---|--------|------------------------|--------------|---------|-----------|-----------|
| - | -      | -                      | -            | -       | 0         | 0         |
| 0 | 5      | 0 + 5 = 5              | 0            | 5       | 0         | 5         |
| 1 | 1      | 0 + 1 = 1              | 5            | 5       | 5         | 5         |
| 2 | 2      | 5 + 2 = 7              | 5            | 7       | 5         | 7         |
| 3 | 10     | 5 + 10 = 15            | 7            | 15      | 7         | 15        |
| 4 | 6      | 7 + 6 = 13             | 15           | 15      | 15        | 15        |

Final answer: `15`.

The trace shows the core invariant: at every index, `prev1` is the best achievable reward for the prefix processed so far. The algorithm never commits to a specific subset too early; it only preserves the optimal value.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because it performs one pass over the array and does constant work per element: one addition and one `max` comparison. At `10^6` elements this is routine; at `10^9`, linear time is still expensive, but anything superlinear is operationally unrealistic.

### Space Complexity
The optimized version uses `O(1)` extra space. The only additional storage is two rolling DP variables holding the previous two prefix optima. A full `O(n)` DP array is unnecessary unless you also need to reconstruct which milestones were chosen.

## 💡 Key Takeaways
- If a problem asks for a maximum over an ordered sequence with a “cannot take adjacent items” rule, think one-dimensional dynamic programming immediately.
- When each decision depends only on a fixed number of earlier positions, look for a rolling-state optimization instead of storing the full DP table.
- The base case must allow selecting nothing; otherwise all-negative inputs produce the wrong answer.
- Be careful with index semantics: `take` must use the state from two positions back, not the just-updated previous state.
- The production lesson is broader than this problem: when constraints are local and sequential, prefix-optimal state often gives exact optimization with predictable runtime and minimal memory.

## 🚀 Variations & Further Practice
- **Circular milestones:** first and last milestones are also adjacent. The twist is that one local constraint becomes global, so you solve two linear cases and combine them.
- **Recover the chosen milestones:** return the actual selected indices, not just the maximum reward. The twist is space: reconstruction usually requires storing decisions or replayable state.
- **Skip distance `k` instead of 1:** selecting milestone `i` blocks the next `k` milestones. The twist is generalizing the recurrence from `i-2` to `i-(k+1)` while preserving linear time.