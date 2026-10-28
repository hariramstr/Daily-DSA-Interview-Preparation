# Minimum Penalty to Build a Monotone API Rollout Plan

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, prefix-minimum, matrix-dp

---

## 🗂 Problem Overview
Given an `N x M` penalty matrix, choose exactly one rollout level `1..M` for each of `N` regions such that the chosen levels form a nondecreasing sequence. The objective is to minimize total penalty. The challenge is that each region’s locally cheapest level may make later choices invalid or expensive. Brute-forcing all assignments is impossible; the key is exploiting the monotonicity constraint to compress transitions in a dynamic programming formulation.

## 🌍 Engineering Impact
This pattern shows up anywhere decisions must remain monotone across an ordered surface: progressive API rollouts by geography, staged feature flags by customer tier, compiler optimization levels across pipeline phases, search-ranking thresholds across latency tiers, or bandwidth caps across edge regions. Without the right DP, teams fall back to greedy heuristics or expensive backtracking, both of which fail under scale or produce unstable plans. The prefix-minimum optimization matters because it converts a quadratic state transition into a linear scan per row, making globally optimal planning feasible for matrices with millions of cells.

## 🔍 Problem Statement
You are given `cost`, where `cost[i][j]` is the penalty of assigning rollout level `j + 1` to region `i`. For each region `i` in `0..N-1`, choose exactly one level in `1..M`. The chosen sequence must be nondecreasing:

`level[0] <= level[1] <= ... <= level[N-1]`

Return the minimum possible total penalty.

Constraints:

- `1 <= N <= 2000`
- `1 <= M <= 2000`
- `1 <= N * M <= 4 * 10^6`
- `0 <= cost[i][j] <= 10^9`
- Use 64-bit integers for the answer

Examples:

- `cost = [[5,1,4],[6,2,3],[7,3,1]]` → `4`
  - Optimal levels: `[2,2,3]`
- `cost = [[1,10,100],[100,1,10],[100,10,1],[1000,1,1]]` → `4`
  - One optimal plan: `[1,2,3,3]`

The decisive constraint is `N * M` up to `4 * 10^6`: `O(N * M^2)` is too slow, so transitions must be optimized.

## 🪜 How to Solve This
1. Read the monotonicity rule → if region `i` uses level `j`, then region `i-1` must have used some level `<= j`. That immediately suggests a DP over `(region, ending level)`.

2. Define the state: let `dp[i][j]` be the minimum total penalty for regions `0..i` if region `i` ends at level `j`. Now the answer is the minimum value in the last row.

3. Write the recurrence:  
   `dp[i][j] = cost[i][j] + min(dp[i-1][0..j])`  
   because the previous level can be anything not exceeding `j`.

4. Notice the bottleneck: naively computing `min(dp[i-1][0..j])` for every `j` costs `O(M)` per state, giving `O(N * M^2)`.

5. Exploit structure: as `j` increases, the needed range only expands by one element. So while scanning left to right, maintain a running prefix minimum of the previous row.

6. This turns each row into one linear pass: compute prefix minima of `prev`, then build `curr[j] = cost[i][j] + prefixMin[j]`.

7. Since each row depends only on the previous one, collapse space to two 1D arrays.

## 🧩 Algorithm Walkthrough
1. **Choose the DP abstraction: matrix DP with prefix minima.**  
   The state space is naturally a grid: rows are regions, columns are rollout levels. This is the right abstraction because the constraint relates adjacent decisions in order, not arbitrary pairs.

2. **Initialize the first row.**  
   For region `0`, there is no prior constraint. So:  
   `prev[j] = cost[0][j]`  
   Invariant: `prev[j]` is the minimum penalty for processing regions `0..0` and ending at level `j+1`.

3. **For each subsequent region, compute prefix minima over the previous row.**  
   Scan `j = 0..M-1`, maintaining:  
   `best = min(best, prev[j])`  
   This `best` equals `min(prev[0..j])`.  
   Invariant: after processing column `j`, `best` is exactly the cheapest valid prior ending level for current level `j`.

4. **Build the current DP row.**  
   Set:  
   `curr[j] = cost[i][j] + best`  
   This is correct because any valid plan ending at current level `j+1` must come from a previous level at most `j+1`, and `best` already stores the cheapest such predecessor.

5. **Roll arrays forward.**  
   After finishing the row, swap `prev` and `curr`.  
   Invariant: `prev` always represents the fully computed optimal costs for the latest processed region.

6. **Return the minimum of the last row.**  
   The final plan may end at any level, so the answer is `min(prev[j])`.

This is a classic **Dynamic Programming + Prefix Minimum** optimization: the monotone constraint converts arbitrary transitions into prefix-range minima, which can be maintained in linear time per row.

## 📊 Worked Example
Use `cost = [[5,1,4],[6,2,3],[7,3,1]]`.

| Region `i` | Raw cost row | `prev` before row | Prefix min of `prev` | `curr` result |
|---|---:|---:|---:|---:|
| 0 | `[5,1,4]` | — | — | `[5,1,4]` |
| 1 | `[6,2,3]` | `[5,1,4]` | `[5,1,1]` | `[11,3,4]` |
| 2 | `[7,3,1]` | `[11,3,4]` | `[11,3,3]` | `[18,6,4]` |

Trace:

1. Start with region 0: choosing levels 1, 2, 3 costs `[5,1,4]`.
2. For region 1, valid predecessors for each level are all earlier levels up to that column. Prefix minima of `[5,1,4]` are `[5,1,1]`, so new costs become `[6+5, 2+1, 3+1] = [11,3,4]`.
3. For region 2, prefix minima of `[11,3,4]` are `[11,3,3]`, so costs become `[7+11, 3+3, 1+3] = [18,6,4]`.

Minimum final value is `4`, achieved by levels `[2,2,3]`.

## ⏱ Complexity Analysis

### Time Complexity
`O(N * M)`. Each of the `N` rows is processed with a single left-to-right pass over `M` levels, and each column does only constant work: update a running minimum and compute one DP cell. At `4 * 10^6` cells, this is practical; `O(N * M^2)` is not.

### Space Complexity
`O(M)` with rolling arrays. The dominant structures are the previous and current DP rows; no full `N x M` table is required. You can also compute prefix minima on the fly, so there is no need for an additional `M`-sized prefix array unless it improves code clarity.

## 💡 Key Takeaways
- If a DP transition looks like “current state depends on any previous state in a prefix/range,” look for prefix minima or prefix sums before accepting a quadratic inner loop.
- Ordered decisions plus a nondecreasing/nonincreasing constraint are strong signals for matrix DP over `(position, chosen value)`.
- Be explicit about indexing: problem levels are `1..M`, but array columns are `0..M-1`; mixing them is the easiest way to corrupt the recurrence.
- Use 64-bit integers end to end; with penalties up to `10^9` across thousands of regions, 32-bit overflow is guaranteed.
- The transferable design insight is that monotonic business constraints often create exploitable structure; the right abstraction turns a combinatorial search problem into a linear scan over compressed state.

## 🚀 Variations & Further Practice
- Allow both nondecreasing and nonincreasing plans, and choose the cheaper one. The twist is recognizing that the second case can be solved by reversing level order or transforming the matrix.
- Add a switching penalty `abs(level[i] - level[i-1])` or arbitrary transition costs. The harder part is that transitions are no longer prefix minima; you need a richer DP optimization or accept higher complexity.
- Require exactly `K` distinct levels in the final plan. This adds another DP dimension and forces reasoning about when a new level segment begins.