# Minimum Cost to Paint a Street of Shops with Neighborhood Targets

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, state-compression

---

## 🗂 Problem Overview
Given `n` shops in a line, each must end with one of `m` colors. Some shops are fixed; others can be painted at a per-color cost. The goal is to minimize total painting cost while producing exactly `target` neighborhoods, where a neighborhood is a maximal contiguous run of the same color. The difficulty is that each decision affects both cost and future neighborhood count, so local cheapest choices are often globally wrong.

## 🌍 Engineering Impact
This pattern shows up anywhere local decisions change segment structure over time: log compaction with run boundaries, video/frame encoding with scene segmentation costs, compiler instruction selection with transition penalties, and streaming pipelines that batch adjacent compatible records. At scale, greedy heuristics fail because transition count is itself a constrained resource. Dynamic programming gives a bounded-state search over cost and boundary formation, which is exactly what production systems need when optimizing under hard structural constraints. Without it, teams either overpay with brute force or ship heuristics that degrade badly on adversarial inputs and edge-heavy workloads.

## 🔍 Problem Statement
You are given:

- `shops[i] = 0` if shop `i` is unpainted, otherwise `shops[i] ∈ [1, m]`
- `cost[i][c - 1]` = cost to paint shop `i` with color `c` when unpainted
- `target` = required number of neighborhoods after all shops are painted

A neighborhood is a maximal contiguous block of equal colors. The task is to return the minimum total cost to paint all shops so that the final street has exactly `target` neighborhoods. If no valid assignment exists, return `-1`.

Constraints:

- `1 <= n <= 100`
- `1 <= m <= 20`
- `1 <= target <= n`
- `cost[i][j] <= 10^4`

Examples:

- `shops = [0,0,0,0]`, `cost = [[1,5],[4,1],[1,3],[2,1]]`, `target = 2` → `4`
- `shops = [1,0,2,0,0]`, `target = 3` → compute the true minimum from the arrays, not the narrative text

The key constraint is exact neighborhood count, which forces stateful optimization rather than greedy painting.

## 🪜 How to Solve This
1. Read the problem → notice cost alone is not enough; we must also control how many color segments are formed.

2. A shop only interacts with the immediately previous shop for neighborhood creation. That strongly suggests the state should include the **previous color**.

3. We also need to know how many neighborhoods have already been formed. So the natural DP state becomes:  
   `position i`, `neighborhoods formed k`, `last color c`.

4. For each shop, try every allowed color:
   - if it matches the previous color, neighborhood count stays the same
   - otherwise, it increases by one

5. Pre-painted shops reduce branching: they allow exactly one color transition.

6. Since only the previous row matters, compress the `i` dimension and keep DP over `(k, color)`.

7. Use a large sentinel `INF` for impossible states and take the minimum over all ending colors with exactly `target` neighborhoods.

Once you see “optimize cost subject to exact number of contiguous groups,” this DP formulation is the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Define the DP pattern: Dynamic Programming with state compression.**  
   Let `dp[k][c]` be the minimum cost after processing shops up to the current index, ending with color `c`, having formed exactly `k` neighborhoods. This abstraction is right because future decisions only depend on the last color and current neighborhood count.

2. **Initialize the first shop.**  
   If shop `0` is pre-painted with color `c`, then `dp[1][c] = 0`. If unpainted, set `dp[1][c] = cost[0][c-1]` for every color `c`.  
   Invariant: after initialization, every valid state represents exactly one painted shop and one neighborhood.

3. **Process shops left to right.**  
   For each next shop, build `next[k][c]` from prior states. This preserves the left-to-right dependency and avoids revisiting earlier choices.

4. **Enumerate allowed current colors.**  
   If the shop is pre-painted, only one color is legal. Otherwise, all `m` colors are candidates, each with its own add-on painting cost.

5. **Transition from every previous color.**  
   From prior state `(k, prevColor)` to current color `c`:
   - if `c == prevColor`, stay at `k`
   - else move to `k + 1`  
   Update with minimum total cost.  
   Invariant: every DP entry is the cheapest way to realize that exact `(k, c)` state.

6. **Prune impossible states.**  
   Ignore transitions where `k > target` or prior cost is `INF`. This keeps work bounded and avoids contaminating minima.

7. **Return the answer.**  
   After the last shop, take `min(dp[target][c])` over all colors. If all are `INF`, return `-1`.  
   Correctness follows from exhaustive enumeration of all legal colorings under a state representation that captures every factor affecting future cost.

## 📊 Worked Example
Use `shops = [0,0,0,0]`, `cost = [[1,5],[4,1],[1,3],[2,1]]`, `target = 2`.

| Shop i | Allowed colors | Key best states after processing shop |
|---|---|---|
| 0 | 1, 2 | `(1,1)=1`, `(1,2)=5` |
| 1 | 1, 2 | From prior states: `(1,1)=5`, `(1,2)=6`, `(2,1)=9`, `(2,2)=2` |
| 2 | 1, 2 | Best useful states: `(2,1)=3`, `(2,2)=5` |
| 3 | 1, 2 | Best target states: `(2,1)=7`, `(2,2)=4` |

Trace intuition:

1. Paint shop `0` as color `1` for cost `1`.
2. Paint shop `1` as color `2` for cost `1`, creating neighborhood `2`.
3. Paint shop `2` as color `2` for cost `3`? Not optimal from all paths; DP keeps cheaper alternatives.
4. Paint shop `3` as color `2` for cost `1`.

Optimal final coloring is `[1,2,2,2]` with exactly 2 neighborhoods and total cost `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * target * m^2)`. For each of `n` shops, we evaluate every neighborhood count, every current color, and every previous color. With `n <= 100` and `m <= 20`, this is practical. At `10^6` or `10^9` scale it would not be; the bounded constraints are what make this DP viable.

### Space Complexity
`O(target * m)` with rolling arrays. The space is owned by the compressed DP tables for current and next shop states. A full 3D DP would take `O(n * target * m)` and is easier to debug, but unnecessary unless reconstructing the actual coloring path.

## 💡 Key Takeaways
- If the objective depends on both cumulative cost and the number of contiguous segments formed so far, expect DP with state `(index, groups, lastValue)`.
- When only the immediately previous choice affects whether a new group starts, “last color” is the critical state dimension.
- The most common bug is miscounting the first neighborhood: the first painted shop always starts neighborhood `1`, not `0`.
- Another frequent trap is handling pre-painted shops incorrectly; they still participate in transitions but contribute zero painting cost and only one legal color.
- The transferable design insight is that exact structural constraints usually require carrying boundary-state explicitly rather than hoping a local optimizer will preserve global shape.

## 🚀 Variations & Further Practice
- Add a requirement to reconstruct one optimal coloring, not just its cost. The twist is storing parent pointers while preserving compressed-state efficiency.
- Penalize color changes instead of constraining neighborhood count. This shifts the problem from exact-feasibility DP to transition-cost sequence optimization.
- Extend from a line to a grid or tree of shops. The hard part becomes that “neighborhood” is no longer captured by a single previous color, so the state space grows dramatically.