# Minimum Energy to Cross a Toll Bridge Path

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Array, Optimization

---

## 🗂 Problem Overview
Given an array `cost`, where `cost[i]` is the energy paid when landing on section `i`, compute the minimum total energy required to move past the last section. The robot can begin before index `0` and jump either 1 or 2 sections at a time. The challenge is that each decision depends on the cheapest way to reach the prior one or two positions, which makes greedy local choices unreliable and points directly to dynamic programming.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the cheapest path through a linear sequence with bounded transition rules: staged data pipelines, packet forwarding across constrained hops, workflow engines with skip/retry semantics, and compiler optimization passes that select among adjacent transformations. At scale, brute-force exploration explodes into repeated recomputation, while greedy heuristics miss globally optimal paths. Dynamic programming converts an exponential decision tree into a linear scan with stable memory usage. That matters in production when the same optimization runs millions of times per second inside schedulers, routing layers, or cost-based planners.

## 🔍 Problem Statement
You are given an integer array `cost` with `2 <= cost.length <= 1000` and `0 <= cost[i] <= 999`. If the robot lands on section `i`, it pays `cost[i]` exactly once. From any position, it may jump forward by either 1 or 2 sections. It starts before the path and finishes when it moves beyond the last index.

Return the minimum total energy needed to reach the destination.

Examples:

- `cost = [4, 2, 7, 3]` → `5`  
  Start before the path, land on section `1` (pay `2`), then section `3` (pay `3`), then move past the end.

- `cost = [1, 100, 1, 1, 100, 1]` → `3`  
  An optimal route lands on sections `0`, `2`, and `5`, paying `1 + 1 + 1 = 3`.

The key constraint is the transition rule: each state depends only on the previous two states, enabling linear-time DP.

## 🪜 How to Solve This
1. Read the movement rule → from any point, you can only come from 1 step back or 2 steps back. That immediately suggests a recurrence over adjacent states.

2. Define the subproblem → let the minimum cost to land on section `i` be the cost of that section plus the cheaper of the two ways to reach it.

3. Write the recurrence →  
   `dp[i] = cost[i] + min(dp[i-1], dp[i-2])`

4. Handle the start correctly → the robot may begin before section `0`, so landing on section `0` costs `cost[0]`, and landing on section `1` costs `cost[1]`. No prior payment exists.

5. Define the finish condition → the destination is beyond the last section, so the final move can come from either of the last two sections. The answer is `min(dp[n-1], dp[n-2])`.

6. Notice the dependency width is constant → each state only needs the previous two values, so the DP array is optional. You can keep two rolling variables and get the same result in `O(1)` extra space.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Dynamic Programming with rolling state.**  
   This is a shortest-path problem on a linear DAG where each node has edges from the previous one or two nodes. DP is the right abstraction because optimal substructure is explicit: the cheapest way to reach `i` must extend the cheapest way to reach `i-1` or `i-2`.

2. **Initialize the base states.**  
   Let `prev2 = cost[0]` and `prev1 = cost[1]`. These represent the minimum energy required to land on sections `0` and `1`. This is correct because the robot may start before the array and jump directly to either section.

3. **Iterate from section `2` to `n-1`.**  
   For each section `i`, compute  
   `curr = cost[i] + min(prev1, prev2)`.  
   This works because any valid landing on `i` must come from exactly one of those two earlier sections.

4. **Advance the rolling window.**  
   Update `prev2 = prev1` and `prev1 = curr`.  
   The invariant is: after processing index `i`, `prev1` holds the minimum cost to land on `i`, and `prev2` holds the minimum cost to land on `i-1`.

5. **Return the cheaper exit path.**  
   The robot finishes by stepping beyond the array, which can be done from either of the last two sections. Therefore return `min(prev1, prev2)`.

This preserves correctness at every step while reducing memory from `O(n)` to `O(1)`.

## 📊 Worked Example
Example: `cost = [4, 2, 7, 3]`

| Step | Section | Computation | prev2 | prev1 |
|------|---------|-------------|-------|-------|
| Init | 0, 1    | `prev2 = 4`, `prev1 = 2` | 4 | 2 |
| 1    | 2       | `curr = 7 + min(2, 4) = 9` | 2 | 9 |
| 2    | 3       | `curr = 3 + min(9, 2) = 5` | 9 | 5 |

After processing all sections, the robot can exit from section `2` or section `3`.  
Return `min(9, 5) = 5`.

Interpretation:

- Land on section `1` → pay `2`
- Jump to section `3` → pay `3`
- Move past the end → pay nothing

Total energy = `5`.

The trace also shows why a local choice on section `2` is irrelevant; what matters is preserving the minimum cumulative cost to each reachable landing point.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = cost.length`. The algorithm performs a single left-to-right pass, and each section requires constant work: one `min`, one addition, and a few assignments. At `10^6` elements this is routine; at `10^9`, linear time is still likely dominated by memory bandwidth and becomes impractical without partitioning or streaming constraints.

### Space Complexity
`O(1)` extra space with the rolling-state version. The only additional memory is two or three scalar variables holding the last DP states. A full DP array uses `O(n)` space, which may help debugging or path reconstruction but is unnecessary for the minimum-cost value alone.

## 💡 Key Takeaways
- If each position’s optimal answer depends only on a fixed number of earlier positions, suspect 1D dynamic programming.
- If the problem asks for a minimum total cost over sequential choices with limited jumps, model it as a recurrence instead of searching paths explicitly.
- The answer is not `dp[n-1]`; the destination is beyond the array, so you must return `min` of the last two reachable landing states.
- Base cases are easy to get wrong: starting before the array means section `0` and section `1` are both valid first landings.
- In production systems, bounded-state DP is valuable because it turns repeated local optimization over streams or stages into predictable linear work with constant memory.

## 🚀 Variations & Further Practice
- **Climbing stairs with arbitrary jump sizes `k`:** same DP idea, but each state depends on the previous `k` states, forcing a different rolling-window strategy.
- **Minimum path sum in a grid:** extends from 1D to 2D DP; the conceptual twist is managing two-dimensional state transitions and boundary initialization.
- **House Robber:** also linear DP with local dependencies, but the constraint changes from movement cost to mutually exclusive selections across adjacent positions.