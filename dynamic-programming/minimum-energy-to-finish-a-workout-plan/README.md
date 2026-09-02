# Minimum Energy to Finish a Workout Plan

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `energy`, compute the minimum total cost required to move from before index `0` to just beyond the last index, where each move advances by either 1 or 2 exercises. You only pay for exercises you land on. The output is a single integer: the minimum achievable energy total. The non-trivial part is that each decision affects future options, so greedy local choices do not reliably produce the global minimum.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose a lowest-cost path through a linear sequence with bounded transition rules. Examples include workflow orchestration with optional stages, media streaming pipelines that can skip expensive transforms, compiler optimization passes with selective execution, and request-processing chains with fallback shortcuts. At scale, brute-force exploration of all paths becomes exponential and operationally useless. Dynamic programming turns that into a predictable linear pass, which matters when this logic sits inside schedulers, admission controllers, or latency-sensitive planning components that must make millions of decisions under tight CPU budgets.

## 🔍 Problem Statement
You are given an integer array `energy` where `energy[i]` is the cost of completing exercise `i`. You start before the first exercise and must finish just beyond the last one. On each move, you may advance by either 1 or 2 exercises. If you land on an exercise, you pay its cost; if you skip over it with a 2-exercise move, you pay nothing for that exercise.

Return the minimum total energy needed to finish the plan.

Constraints:

- `2 <= energy.length <= 1000`
- `0 <= energy[i] <= 999`
- The result fits in a 32-bit integer

Examples:

- `energy = [4, 1, 6, 2]` → `3`
- `energy = [3, 5, 2, 1, 4]` → `6`

The key constraint is the transition rule: each position depends only on the previous one or two positions, which strongly suggests a linear dynamic programming solution.

## 🪜 How to Solve This
1. Read the move rules carefully → from any position, you can arrive only from one step back or two steps back.
2. That means the minimum cost to land on exercise `i` depends on the cheaper of:
   - landing on `i - 1` and stepping once, or
   - landing on `i - 2` and stepping twice.
3. If you define `dp[i]` as the minimum energy required to land on exercise `i`, then the recurrence becomes obvious:  
   `dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])`.
4. Initialize the first two exercises directly, because you can start by landing on either index `0` or index `1`.
5. The finish position is just beyond the array, so you do not pay any extra cost there. You can finish from either of the last two exercises, so the answer is `min(dp[n - 1], dp[n - 2])`.
6. Once that recurrence is visible, the implementation is straightforward: one left-to-right pass, no backtracking, no search tree, no greedy guesswork.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Dynamic Programming on a linear array.**  
   The problem has optimal substructure: the cheapest way to reach exercise `i` is built from the cheapest way to reach `i-1` or `i-2`. That makes DP the right abstraction, not graph search or greedy selection.

2. **Define the state clearly.**  
   Let `dp[i]` be the minimum energy required to land on exercise `i`. This state is sufficient because future decisions only care about the minimum cost accumulated so far, not the exact path shape.

3. **Establish base cases.**  
   `dp[0] = energy[0]` and `dp[1] = energy[1]`.  
   Both are valid because you may start by taking a 1-exercise move to index `0` or a 2-exercise move to index `1`.

4. **Apply the recurrence left to right.**  
   For each `i >= 2`:  
   `dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])`.  
   This is correct because every legal path landing on `i` must come from exactly one of those two prior exercises.

5. **Maintain the invariant.**  
   After processing index `i`, `dp[0..i]` contains the true minimum landing cost for every exercise in that prefix. No future computation can improve those values because all incoming transitions have already been considered.

6. **Compute the finish cost.**  
   The finish is beyond the last index, with no landing cost. You can reach it from either `n-1` or `n-2`, so return `min(dp[n - 1], dp[n - 2])`.

7. **Optimize space if needed.**  
   Because each state depends only on the previous two, the DP array can be reduced to two rolling variables without changing correctness.

## 📊 Worked Example
Take `energy = [4, 1, 6, 2]`.

| i | energy[i] | dp[i] calculation            | dp[i] |
|---|-----------|------------------------------|-------|
| 0 | 4         | base case                    | 4     |
| 1 | 1         | base case                    | 1     |
| 2 | 6         | `6 + min(1, 4)`              | 7     |
| 3 | 2         | `2 + min(7, 1)`              | 3     |

Now compute the finish:

- Finish can be reached from index `2` or index `3`
- Minimum total energy = `min(dp[2], dp[3]) = min(7, 3) = 3`

Optimal path:

- Start → land on exercise `1` (cost `1`)
- Move two exercises → land on exercise `3` (cost `2`)
- Move beyond the array → finish

Total = `1 + 2 = 3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = energy.length`. The dominant operation is a single left-to-right pass, and each index performs constant work: one `min` and one addition. At `10^6` elements this is still operationally cheap; at `10^9`, linear time becomes throughput-bound and likely requires partitioning or a different problem framing.

### Space Complexity
`O(n)` with a full `dp` array, owned entirely by the dynamic programming state. This can be reduced to `O(1)` by keeping only the previous two values, since no older state is ever read again. The trade-off is less inspectability for debugging or path reconstruction.

## 💡 Key Takeaways
- If each position’s optimal value depends only on a fixed number of earlier positions, that is a strong signal for 1D dynamic programming.
- If the problem asks for a minimum total cost over legal moves in a linear sequence, think “state per index + recurrence,” not greedy skipping.
- The finish is not an array index; it is a virtual position beyond the array, so the answer is `min(last, second-last)`, not necessarily `dp[n-1]`.
- Base cases are easy to get wrong: you can start by landing on index `0` or index `1`, so both must be initialized directly.
- The production-grade insight is to model only the minimal state needed for future decisions; bounded dependencies often let you collapse memory from linear to constant without changing behavior.

## 🚀 Variations & Further Practice
- Allow moves of 1, 2, or `k` exercises: same DP pattern, but each state now depends on a wider transition window and may need sliding-window optimization.
- Add a penalty or reward for consecutive move sizes: the state must now include the previous move, turning simple 1D DP into multi-dimensional DP.
- Require reconstruction of the actual optimal path, not just the cost: store predecessor pointers or recompute decisions, introducing a space/debuggability trade-off.