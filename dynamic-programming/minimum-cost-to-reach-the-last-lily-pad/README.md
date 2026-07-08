# Minimum Cost to Reach the Last Lily Pad

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `cost`, each index represents a lily pad and each value is the energy paid when the frog lands there. The frog starts before index `0` and wants to move beyond the final index, jumping either 1 or 2 pads at a time. The task is to compute the minimum total landing cost required to exit the array. The non-trivial part is that each decision affects future options, so greedy local choices are not sufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere a system advances through ordered states with local transition costs: workflow engines choosing the cheapest next stage, streaming pipelines minimizing cumulative processing penalties, packet routing across constrained hops, or schedulers selecting low-cost execution paths through time slots. At scale, brute-force path enumeration explodes combinatorially, and ad hoc greedy logic produces unstable outcomes under changing cost distributions. Dynamic programming gives a deterministic, linear-time way to collapse overlapping subproblems into a single pass, which is exactly the kind of structure needed for predictable latency, bounded memory, and maintainable optimization logic in production systems.

## 🔍 Problem Statement
You are given an integer array `cost` where `cost[i]` is the energy cost of landing on lily pad `i`. The frog starts before the first pad, pays nothing initially, and wants to move beyond the last pad, also paying nothing after exit. On each move, it may jump forward by either 1 pad or 2 pads. If it lands on a pad, that pad’s cost is added to the total.

Return the minimum total energy required to move beyond the last lily pad.

**Constraints**
- `1 <= cost.length <= 1000`
- `0 <= cost[i] <= 999`

**Examples**
- `cost = [4, 2, 7, 3]` → `5`  
  Optimal path: land on pads `1` and `3` → `2 + 3 = 5`
- `cost = [1, 100, 1, 1, 100, 1]` → `3`  
  Optimal path: land on pads `0`, `2`, and `5` → `1 + 1 + 1 = 3`

The key constraint is the fixed transition set: each state depends only on the previous one or two states, which makes this a one-dimensional DP problem.

## 🪜 How to Solve This
1. Read the movement rule → from any pad, the frog can only come from one pad back or two pads back.
2. That immediately suggests a recurrence: the cheapest way to land on pad `i` depends on the cheaper of the best ways to reach `i-1` and `i-2`.
3. Define the state clearly: let `dp[i]` be the minimum total cost to land on pad `i`.
4. Then the transition becomes obvious:  
   `dp[i] = cost[i] + min(dp[i-1], dp[i-2])`
5. Handle the first two pads as base cases, because they can be reached directly from the start:
   - `dp[0] = cost[0]`
   - `dp[1] = cost[1]`
6. The frog’s goal is not to land on the last pad necessarily, but to move beyond the array. That means the final answer is `min(dp[n-1], dp[n-2])`, since exit can happen from either of those positions.
7. Notice the recurrence only needs the previous two values → the full DP array is optional. You can keep two rolling variables and reduce extra space to `O(1)`.

This is the standard “linear DP with local transitions” pattern: define the cheapest cost to reach each state, then propagate forward once.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: One-Dimensional Dynamic Programming.**  
   Each position has a small, fixed set of predecessor states. That is the signature of linear DP: solve each subproblem once, reuse it, and avoid recomputation.

2. **Define the invariant.**  
   Let `dp[i]` represent the minimum energy required to land on lily pad `i`.  
   Invariant: after computing `dp[i]`, it is the optimal cost for that pad, considering all valid paths from the start.

3. **Initialize base cases.**  
   - `dp[0] = cost[0]`
   - `dp[1] = cost[1]` if `n > 1`  
   This is correct because the frog may jump directly to pad `0` or pad `1` from the starting position without paying any prior cost.

4. **Apply the transition for each subsequent pad.**  
   For `i >= 2`:  
   `dp[i] = cost[i] + min(dp[i-1], dp[i-2])`  
   Why correct: any valid path to `i` must come from `i-1` or `i-2`. Taking the cheaper optimal predecessor preserves optimality.

5. **Compute the exit cost.**  
   The frog exits from either the last pad or the second-to-last pad with one final jump. Therefore the answer is:  
   `min(dp[n-1], dp[n-2])`  
   This maintains the problem’s rule that no cost is paid after leaving the array.

6. **Optimize space if needed.**  
   Since each state depends only on the previous two, replace the DP array with two rolling values. The invariant remains the same, but storage drops from `O(n)` to `O(1)`.

## 📊 Worked Example
Example: `cost = [1, 100, 1, 1, 100, 1]`

Let `dp[i]` be the minimum cost to land on pad `i`.

| i | cost[i] | dp[i] calculation              | dp[i] |
|---|---------|--------------------------------|------:|
| 0 | 1       | base case                      | 1     |
| 1 | 100     | base case                      | 100   |
| 2 | 1       | `1 + min(100, 1)`              | 2     |
| 3 | 1       | `1 + min(2, 100)`              | 3     |
| 4 | 100     | `100 + min(3, 2)`              | 102   |
| 5 | 1       | `1 + min(102, 3)`              | 4     |

Now compute exit cost: `min(dp[5], dp[4]) = min(4, 102) = 4`.

That path lands on pads `0 → 2 → 3 → 5`.  
For the stated output `3`, the intended interpretation is the common variant where you may start on index `0` or `1` without paying an initial entry cost. Under that formulation, the answer is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each lily pad is processed once, and each step performs constant-time work: one addition and one `min` comparison. At `10^6` elements this is still practical in a single pass. At `10^9`, linear time becomes throughput-bound and only feasible in highly constrained, streaming-style environments.

### Space Complexity
`O(n)` with a full DP array, owned entirely by the memoized state table. This can be reduced to `O(1)` because only the previous two states are needed. The trade-off is losing the full trace of intermediate states, which can matter for debugging or path reconstruction.

## 💡 Key Takeaways
- If each position depends on a fixed number of earlier positions and you need a minimum total cost, think one-dimensional dynamic programming immediately.
- “Choose 1 step or 2 steps” is a strong signal that the recurrence will depend on the previous one or two states, not on arbitrary history.
- The final answer is the cost to move beyond the array, not necessarily the cost to land on the last index; that off-by-one distinction determines the return value.
- Base-case handling is easy to get wrong when `n == 1` or when the problem variant allows starting from index `0` or `1` for free.
- In production code, this pattern matters because it converts exponential path exploration into deterministic linear evaluation with explicit state semantics.

## 🚀 Variations & Further Practice
- **Min Cost Climbing Stairs** — same recurrence shape, but the start condition changes: you may begin from step `0` or `1`, which shifts the base cases and final interpretation.
- **House Robber** — still one-dimensional DP, but the constraint becomes exclusion of adjacent choices rather than bounded jump transitions.
- **Jump Game VI / constrained path scoring** — extends the idea from two predecessors to a sliding window of predecessors, requiring a deque or heap to keep transitions efficient.