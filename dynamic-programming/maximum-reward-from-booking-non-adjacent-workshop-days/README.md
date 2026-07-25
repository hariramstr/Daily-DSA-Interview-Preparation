# Maximum Reward from Booking Non-Adjacent Workshop Days

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `rewards`, choose workshop days to maximize total reward under one constraint: you cannot book adjacent days. Return only the maximum achievable sum. The challenge is that each decision affects the next one, so locally choosing the largest reward can block a better future combination. With up to `100000` days, the solution must avoid exponential subset exploration and compute the optimum in linear time.

## 🌍 Engineering Impact
This pattern appears anywhere a system must maximize value under local exclusion constraints. Examples include ad-slot scheduling with cooldown windows, batch job placement on shared infrastructure, portfolio selection with mutually exclusive time slots, and stream-processing operators that cannot run in consecutive intervals due to warm-up or recovery costs. At scale, greedy heuristics silently leave money or capacity on the table, while brute force collapses combinatorially. Dynamic programming gives a deterministic optimal answer with predictable latency, which matters when this logic sits inside schedulers, ranking pipelines, or planning services invoked millions of times per day.

## 🔍 Problem Statement
You are given an integer array `rewards` where `rewards[i]` is the reward earned by booking the workshop on day `i`. If you book day `i`, you may not book day `i + 1`. Choose a subset of indices such that no two selected indices are adjacent, and maximize the total reward.

Constraints:

- `1 <= rewards.length <= 100000`
- `0 <= rewards[i] <= 1000000000`
- The answer fits in a 64-bit signed integer

This adjacency restriction is the key constraint: every choice interacts with the immediately previous day, which rules out naive greedy selection and points to dynamic programming.

Examples:

- `rewards = [4, 10, 3, 1, 5]` → `15`
  - Best choice: days `1` and `4` → `10 + 5 = 15`
- `rewards = [2, 7, 9, 3, 1]` → `12`
  - Best choice: days `0`, `2`, and `4` → `2 + 9 + 1 = 12`

## 🪜 How to Solve This
1. Read the constraint carefully → booking day `i` only conflicts with day `i - 1` and `i + 1`. That means each decision is local, not global.
2. Ask what the optimal answer for the first `i` days depends on → only whether we take day `i` or skip it.
3. If we skip day `i`, the best total is whatever was best through day `i - 1`.
4. If we take day `i`, we must skip day `i - 1`, so the total becomes `best through i - 2 + rewards[i]`.
5. That gives the recurrence: `dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])`.
6. Notice we never need the full history, only the previous two states.
7. Collapse the DP table into two running values for O(1) extra space.
8. This works because the problem has optimal substructure: the best plan for prefix `0..i` is built from best plans for smaller prefixes, and overlapping subproblems make recomputation wasteful.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: linear dynamic programming on an array with adjacency exclusion.**  
   This is the canonical “take or skip” DP. The right abstraction is not greedy selection, because a high reward today may block a better pair later.

2. **Define the state on prefixes.**  
   Let `dp[i]` be the maximum reward obtainable from days `0..i`. This state is sufficient because the only relevant conflict for day `i` is day `i - 1`.

3. **Establish base cases.**  
   For one day, `dp[0] = rewards[0]`.  
   For two days, `dp[1] = max(rewards[0], rewards[1])`.  
   These anchor the recurrence and handle the smallest valid inputs.

4. **Derive the transition.**  
   For each day `i >= 2`, there are exactly two valid choices:
   - Skip day `i` → total is `dp[i - 1]`
   - Take day `i` → total is `dp[i - 2] + rewards[i]`  
   Therefore: `dp[i] = max(dp[i - 1], dp[i - 2] + rewards[i])`.

5. **Maintain the invariant.**  
   After processing index `i`, the stored value represents the optimal reward for the prefix `0..i`. This invariant guarantees correctness inductively.

6. **Optimize space.**  
   Since each state depends only on the previous two, keep two variables:
   - `prev2` = best through `i - 2`
   - `prev1` = best through `i - 1`  
   Compute `curr`, then shift forward. This preserves the same invariant with constant extra memory.

7. **Return the final prefix optimum.**  
   After one pass, `prev1` holds the maximum reward for the full array.

## 📊 Worked Example
Use `rewards = [4, 10, 3, 1, 5]`.

| i | rewards[i] | take = prev2 + rewards[i] | skip = prev1 | curr | Meaning |
|---|------------|----------------------------|--------------|------|---------|
| 0 | 4          | —                          | —            | 4    | Best through day 0 |
| 1 | 10         | —                          | —            | 10   | `max(4, 10)` |
| 2 | 3          | 4 + 3 = 7                  | 10           | 10   | Better to skip day 2 |
| 3 | 1          | 10 + 1 = 11                | 10           | 11   | Take day 3 after day 1 |
| 4 | 5          | 10 + 5 = 15                | 11           | 15   | Take day 4 after day 1 |

Trace with rolling state:

- Start: `prev2 = 4`, `prev1 = 10`
- Day 2: `curr = max(10, 4 + 3) = 10`
- Day 3: `curr = max(10, 10 + 1) = 11`
- Day 4: `curr = max(11, 10 + 5) = 15`

Answer: `15`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because it processes each day exactly once and performs constant work per element: one addition and one comparison. This scales comfortably for `10^6` elements in memory-resident workloads, but `10^9` elements would shift the bottleneck to I/O, memory bandwidth, and execution time rather than algorithmic overhead.

### Space Complexity
The optimized implementation uses `O(1)` extra space. The only additional storage is a small fixed set of variables holding the previous two DP states. A full DP array would use `O(n)` space, which is unnecessary unless you need to reconstruct the chosen days rather than only return the maximum sum.

## 💡 Key Takeaways
- If the problem asks for a maximum over an array with a “cannot choose adjacent items” rule, think linear dynamic programming immediately.
- When each choice only conflicts with the immediate neighbor, a prefix-based “take or skip” recurrence is usually the right model.
- Handle `n = 1` and `n = 2` explicitly; most bugs in this problem come from sloppy base-case initialization.
- Use 64-bit arithmetic for the running total; individual values fit in 32 bits, but the accumulated optimum may not.
- In production planning systems, local exclusion constraints often look simple but invalidate greedy heuristics; compact DP gives optimality without sacrificing throughput.

## 🚀 Variations & Further Practice
- **House Robber II**: same recurrence, but the array is circular, so the first and last elements are also adjacent; the twist is splitting into two linear cases.
- **Delete and Earn**: values map into aggregated buckets, then adjacent numeric values conflict; the harder part is transforming the domain before applying the same DP.
- **Weighted interval scheduling**: conflicts are no longer only adjacent by index; the twist is finding the last non-overlapping interval, typically via sorting and binary search.