# Minimum Cost to Deploy Feature Flags Across Release Stages

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, State Compression, Matrix Transitions

---

## 🗂 Problem Overview
Choose one of `m` configurations for each of `n` sequential release stages. Each choice has a per-stage base cost, and every adjacent pair adds a transition cost based on the previous and current configuration. Some stages are pinned to a required configuration, and the final sequence must contain exactly `k` contiguous blocks of equal configurations. Return the minimum total cost, or `-1` if no valid plan exists. The difficulty is that local cheapest choices can easily destroy the global block-count constraint.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must optimize a sequential plan under switching penalties and segmentation constraints: rollout strategies in CI/CD pipelines, model selection across streaming inference stages, query-plan operator selection, compiler pass scheduling, and multi-region traffic steering with migration overhead. At scale, greedy selection fails because transition costs and block-count targets create path-dependent state. Without a structured DP, teams either overpay operationally or encode brittle heuristics that collapse under new constraints. The value of this approach is explicit state modeling: it makes cost, continuity, and policy constraints composable instead of implicit.

## 🔍 Problem Statement
You are given:

- `n` stages, processed in order from `0` to `n - 1`
- `m` possible configurations
- `cost[i][j]`: base cost of deploying configuration `j` at stage `i`
- `transition[a][b]`: extra cost when stage `i - 1` uses `a` and stage `i` uses `b`
- `required[i]`: either `-1` or a fixed configuration that stage `i` must use
- target `k`: the final plan must contain exactly `k` contiguous blocks

A block is a maximal consecutive run of the same configuration. If the chosen configuration changes between adjacent stages, block count increases by one; otherwise it stays the same.

Return the minimum total cost satisfying all constraints, or `-1` if impossible.

Examples:

- `n=4, m=3, k=2` → output `7`
- `n=5, m=2, k=3`, `required=[0,-1,-1,1,-1]` → output `12`

The key constraint is combinatorial coupling: `n <= 100`, `m <= 50`, and block count depends on the full prefix, so brute force over `m^n` assignments is infeasible.

## 🪜 How to Solve This
1. Read the problem → notice the cost of choosing configuration `c` at stage `i` depends on the previous configuration, not just on `i`.
2. Also notice the objective is not only minimum cost; we must end with exactly `k` blocks. That means the state must remember how many blocks have already been formed.
3. A prefix-based DP becomes the natural fit: after processing stage `i`, what is the cheapest way to end with configuration `c` and exactly `b` blocks?
4. Define state `dp[i][b][c]` = minimum cost for stages `0..i`, using `b` blocks, ending in configuration `c`.
5. Transition logic is binary:
   - same configuration as previous → block count unchanged
   - different configuration → block count increases by one
6. Add base cost for stage `i`, plus transition cost from previous configuration.
7. Respect `required[i]` by only allowing the pinned configuration when present.
8. Initialize stage `0` carefully: any allowed configuration starts with exactly one block.
9. Since each layer depends only on the previous stage, compress `i` away and keep two `k x m` layers.

That line of reasoning gets you to the correct abstraction before thinking about code details.

## 🧩 Algorithm Walkthrough
1. **Use Dynamic Programming with state compression.**  
   The right pattern is **Dynamic Programming over sequence position + last choice + segment count**. This is the correct abstraction because future cost depends on the immediately previous configuration and on how many blocks have already been created.

2. **Define the DP state.**  
   Let `prev[b][c]` be the minimum cost after processing the previous stage, forming exactly `b` blocks, and ending with configuration `c`. This invariant means every stored value already includes all base costs and all transition costs up to that stage.

3. **Initialize stage `0`.**  
   For every allowed configuration `c` at stage `0`, set `prev[1][c] = cost[0][c]`. The first chosen configuration always creates the first block, and there is no incoming transition before stage `0`.

4. **Process each next stage `i`.**  
   Build `curr` from `prev`. For each block count `b`, previous configuration `p`, and allowed current configuration `c`, compute:
   - `nb = b` if `c == p`
   - `nb = b + 1` if `c != p`
   Then relax  
   `curr[nb][c] = min(curr[nb][c], prev[b][p] + transition[p][c] + cost[i][c])`.

5. **Enforce milestone constraints.**  
   If `required[i] != -1`, only that configuration is considered for stage `i`. This prunes invalid states early and preserves correctness.

6. **Bound the state space.**  
   Never consider `b > i + 1` or `b > k`; you cannot have more blocks than processed stages. This keeps the DP tight.

7. **Extract the answer.**  
   After stage `n - 1`, the result is `min_c prev[k][c]`. If all values are infinity, return `-1`.

This is correct because every valid plan corresponds to exactly one DP path, and every DP transition models exactly one legal extension of a prefix.

## 📊 Worked Example
Consider `n=5, m=2, k=3`, `required=[0,-1,-1,1,-1]`.

Let configurations be `0` and `1`.

| Stage `i` | Allowed configs | Key DP states after processing stage |
|---|---|---|
| 0 | `{0}` | `dp[1][0] = cost[0][0] = 3` |
| 1 | `{0,1}` | From `(1,0)`: stay `0` → `dp[1][0] = 3 + t[0][0] + 5`; switch `1` → `dp[2][1] = 3 + t[0][1] + 1` |
| 2 | `{0,1}` | Extend each prior state; staying keeps block count, switching increments it |
| 3 | `{1}` | Forced to config `1`; all states ending in `0` must transition or die |
| 4 | `{0,1}` | Final transitions produce candidates with 1, 2, or 3 blocks |

The important trace is structural, not numeric: each row carries forward only `(blocks, lastConfig)` summaries. By stage `4`, the minimum among states with exactly `3` blocks is the answer. Any path with fewer or more than `3` blocks is discarded even if its raw cost is lower.

## ⏱ Complexity Analysis
### Time Complexity
The straightforward DP is `O(n * k * m^2)`: for each stage, for each block count, try every previous configuration and every current configuration. With `n <= 100`, `k <= 100`, `m <= 50`, this is about 25 million transitions, which is practical. At `10^6` or `10^9` scale, this shape would be completely infeasible without stronger structure or matrix/min-plus optimization.

### Space Complexity
Using full `dp[i][b][c]` costs `O(n * k * m)`, but only the previous stage is needed, so it compresses to `O(k * m)`. The dominant structure is the two rolling DP layers. This reduction trades easier debugging for materially lower memory and better cache behavior.

## 💡 Key Takeaways
- If a sequence problem asks for a minimum cost with both adjacency-dependent penalties and an exact number of groups/segments, expect DP state of the form `(index, groups, lastChoice)`.
- “Exactly `k` contiguous blocks” is a strong signal for segment-count DP, not greedy selection or shortest-path over positions alone.
- The first stage is a special case: it starts with exactly one block, and there is no incoming transition cost.
- When `required[i]` is fixed, do not compute all `m` choices and filter later; restrict transitions up front or you will both waste work and invite invalid-state bugs.
- In production systems, this pattern generalizes to policy-aware planning: encode operational constraints as explicit state dimensions rather than burying them in heuristics.

## 🚀 Variations & Further Practice
- Add a hard cap on how many times each configuration may be used. The twist is introducing per-configuration usage counts, which explodes state unless you redesign the formulation.
- Replace exact `k` blocks with a penalty per new block and ask for global minimum. This removes one DP dimension but changes the optimization objective and pruning strategy.
- Make transition cost depend on both stage and pair, `transition[i][a][b]`. The conceptual twist is time-varying edge weights, which preserves the DP shape but removes any chance of precomputing reusable transitions.