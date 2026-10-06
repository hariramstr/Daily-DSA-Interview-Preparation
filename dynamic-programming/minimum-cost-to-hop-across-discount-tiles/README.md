# Minimum Cost to Hop Across Discount Tiles

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, bottom-up-dp

---

## 🗂 Problem Overview
Given an integer array `cost`, compute the minimum total fee required to move from before index `0` to beyond the last index. From any position, you may hop forward by 1 or 2 tiles, and you pay only for tiles you land on. The non-trivial part is the starting rule: you may begin by landing on tile `0` or tile `1`, and the optimal answer depends on choosing the cheaper sequence of paid landings across overlapping subproblems.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose a lowest-cost path through a linear pipeline with bounded local transitions. Examples include packet processing stages with optional fast paths, workflow engines skipping expensive validation steps, compiler optimization passes selecting adjacent or skipped transforms, and media transcoding pipelines choosing between full and incremental processing. At scale, brute-force exploration explodes combinatorially, while greedy choices fail because a locally cheap hop can force an expensive downstream state. Bottom-up dynamic programming gives deterministic latency, predictable memory, and a structure that can often be reduced to constant-space execution in hot paths.

## 🔍 Problem Statement
You are given an array `cost` where `cost[i]` is the fee paid when landing on tile `i`. A player starts before the hallway and wants to move past the final tile. Each move may advance by either 1 tile or 2 tiles. The player may initially land on tile `0` or tile `1`, and from then on every landed tile must be paid.

Return the minimum total cost required to move beyond the last tile.

**Constraints**
- `2 <= cost.length <= 1000`
- `0 <= cost[i] <= 999`

**Examples**
- `cost = [4, 2, 7, 3]` → `5`  
  Start on tile `1` (pay `2`), hop to tile `3` (pay `3`), then exit.

- `cost = [1, 100, 1, 1, 100, 1]` → `3`  
  One optimal route is tile `0` → tile `2` → tile `5` → beyond, paying `1 + 1 + 1 = 3`.

The key constraint is that each state depends only on the previous one or two states, which strongly suggests dynamic programming rather than search.

## 🪜 How to Solve This
1. Read the movement rule → each position can be reached only from the previous tile or the one before that.
2. That immediately creates overlapping subproblems: the cheapest way to reach tile `i` depends on the cheapest ways to reach `i-1` and `i-2`.
3. Define the subproblem clearly: “What is the minimum cost to land on tile `i`?”
4. If you land on `i`, you must pay `cost[i]`. So the best way to reach `i` is:  
   `cost[i] + min(best to reach i-1, best to reach i-2)`.
5. Initialize the first two states carefully. Landing on tile `0` costs `cost[0]`; landing on tile `1` costs `cost[1]`. The “free start” means there is no extra entry cost before those landings.
6. The destination is not an actual tile; moving beyond the last tile is free. So the final answer is the cheaper of ending on the last tile or the second-to-last tile, then hopping out.
7. Because each state depends on only two earlier states, this is classic bottom-up DP and can be implemented in either `O(n)` space or optimized to `O(1)` space.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Bottom-Up Dynamic Programming.**  
   This is the right abstraction because the problem asks for an optimal cumulative cost over a linear structure, and each state has a fixed, local dependency window of size two.

2. **Define the DP state.**  
   Let `dp[i]` be the minimum total cost required to land on tile `i`. This state is sufficient because future decisions only care about the cheapest cost to stand on a given tile, not the exact path taken.

3. **Initialize base cases.**  
   `dp[0] = cost[0]` and `dp[1] = cost[1]`.  
   This is correct because the player may start by landing directly on either tile, with no extra fee before that move.

4. **Build the recurrence.**  
   For every `i >= 2`:  
   `dp[i] = cost[i] + min(dp[i - 1], dp[i - 2])`  
   Why it works: any valid landing on `i` must come from `i-1` or `i-2`. Taking the cheaper of those two optimal prefixes preserves optimality.

5. **Maintain the invariant.**  
   After computing `dp[i]`, every `dp[k]` for `0 <= k <= i` stores the true minimum cost to land on tile `k`. This invariant holds by induction from the base cases and recurrence.

6. **Compute the exit cost.**  
   The goal is to move beyond the last tile, not to land on it necessarily. The final move can come from either the last tile or the second-to-last tile, so the answer is:  
   `min(dp[n - 1], dp[n - 2])`.

7. **Optional space optimization.**  
   Since only the previous two states are needed, replace the array with two rolling variables. Same recurrence, lower memory footprint, better cache behavior.

## 📊 Worked Example
Use `cost = [1, 100, 1, 1, 100, 1]`.

| i | cost[i] | dp[i] calculation                  | dp[i] |
|---|---------|------------------------------------|------:|
| 0 | 1       | base case                          | 1     |
| 1 | 100     | base case                          | 100   |
| 2 | 1       | `1 + min(100, 1)`                  | 2     |
| 3 | 1       | `1 + min(2, 100)`                  | 3     |
| 4 | 100     | `100 + min(3, 2)`                  | 102   |
| 5 | 1       | `1 + min(102, 3)`                  | 4     |

Now compute the exit:

- End on tile `5`, then hop out → total `4`
- End on tile `4`, then hop out → total `102`

So the minimum is `min(4, 102) = 4`.

For this input, the optimal path is `0 -> 2 -> 3 -> 5 -> beyond`, paying `1 + 1 + 1 + 1 = 4`. Despite the prompt’s narrative, `3` is not achievable under the stated rules.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = cost.length`. The dominant operation is a single left-to-right pass, doing constant work per tile: one `min` and one addition. At `10^6` elements this is still routine on modern hardware; at `10^9`, linear time becomes throughput-bound and likely requires streaming, partitioning, or tighter memory locality assumptions.

### Space Complexity
`O(n)` for the explicit DP array, owned entirely by `dp`. This can be reduced to `O(1)` because each state depends only on the previous two values. The trade-off is losing the full state history, which matters only if you also need to reconstruct the chosen path.

## 💡 Key Takeaways
- If the cheapest result at index `i` depends only on optimal results at `i-1` and `i-2`, you are in classic linear dynamic programming territory.
- “Choose 1 step or 2 steps, minimize cumulative cost” is a strong pattern-recognition signal for bottom-up DP with a small recurrence window.
- The exit is beyond the array, so the answer is not necessarily `dp[n-1]`; it is `min(dp[n-1], dp[n-2])`.
- The free-start rule does not mean tile `0` or `1` is free; it means there is no cost before landing there, so base cases are `cost[0]` and `cost[1]`.
- The production-grade lesson is to model only the state boundary that future decisions actually depend on; that is what makes both DP and system pipelines scalable.

## 🚀 Variations & Further Practice
- Allow hops of up to `k` tiles instead of only 1 or 2. The twist is widening the dependency window, which may require a deque or optimized range-min tracking for large `k`.
- Add a constraint that exactly one tile may be skipped without paying even if landed on. The twist is introducing an extra DP dimension for “promotion used vs not used.”
- Return not just the minimum cost but also the actual path taken. The twist is storing predecessor information or reconstructing decisions while preserving the same recurrence.