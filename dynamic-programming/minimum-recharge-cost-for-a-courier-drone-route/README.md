# Minimum Recharge Cost for a Courier Drone Route

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Array, Shortest Path

---

## 🗂 Problem Overview
Given two arrays, `cost` and `jump`, compute the minimum total amount the drone must pay to travel from checkpoint `0` to checkpoint `n - 1`. Buying at checkpoint `i` costs `cost[i]` and enables a forward move to any checkpoint in `(i, i + jump[i]]`. The challenge is that local cheapest choices are not globally optimal, and reachability depends on prior purchases. If the last checkpoint cannot be reached, return `-1`.

## 🌍 Engineering Impact
This pattern appears in staged execution systems where each decision unlocks a bounded future range at some cost: workflow schedulers choosing expensive-but-broad fanout stages, WAN routing with paid transit hops, cloud pipelines selecting intermediate storage or compute tiers, and compilers choosing optimization passes with downstream effects. At scale, greedy local minimization creates pathological global plans: lower immediate cost, higher total spend, or dead-end execution. Dynamic programming matters here because it converts a combinatorial path search into deterministic state propagation, making correctness auditable and latency predictable under large input sizes.

## 🔍 Problem Statement
You are given `n` checkpoints in fixed order, indexed `0` to `n - 1`. At checkpoint `i`, if the drone lands there, it may buy an energy pack for `cost[i]`. That purchase allows a jump to any later checkpoint `j` where `i < j <= i + jump[i]`. The drone starts at checkpoint `0` and must reach checkpoint `n - 1`.

Return the minimum total recharge cost required to reach the final checkpoint. If no sequence of purchases and jumps can reach it, return `-1`.

Constraints:

- `1 <= n <= 5000`
- `cost.length == jump.length == n`
- `0 <= cost[i] <= 10^9`
- `0 <= jump[i] <= n`
- Answer fits in signed 64-bit if reachable

Examples:

- `cost = [5, 1, 4, 2]`, `jump = [2, 2, 1, 0]` → `6`
- `cost = [3, 2, 7, 1, 5]`, `jump = [1, 0, 2, 1, 0]` → `-1`

The key constraint is `n <= 5000`: small enough for quadratic DP, large enough that brute-force path enumeration is unjustifiable.

## 🪜 How to Solve This
1. Read the move rule → every purchase at checkpoint `i` creates directed edges from `i` to a contiguous forward range.
2. Translate the story into graph terms → each checkpoint is a node, buying at `i` pays `cost[i]`, then you may move to any reachable `j`.
3. Notice the graph is acyclic → all edges go forward, so we do not need general shortest-path machinery with cycles.
4. Ask what state matters → the minimum cost to *arrive at* each checkpoint. Call it `dp[i]`.
5. From a checkpoint with known best cost, propagate forward: if `dp[i]` is known, then every `j` in its reachable range can be improved by `dp[i] + cost[i]`.
6. This is dynamic programming over a DAG, or equivalently shortest path on a 1D acyclic graph.
7. Greedy fails because a cheap pack can force landing on expensive intermediates, while a slightly pricier earlier pack may unlock a cheaper suffix.
8. With `n = 5000`, an `O(n^2)` transition scan is acceptable, simple, and robust. That is the right trade-off unless the problem explicitly demands subquadratic optimization.

## 🧩 Algorithm Walkthrough
1. **Define the DP state.**  
   Let `dp[i]` be the minimum total cost required to land on checkpoint `i`. Initialize all entries to infinity except `dp[0] = 0`, because the drone starts at checkpoint `0` without having paid yet.  
   **Invariant:** before processing transitions from `i`, `dp[i]` is the best known cost to reach `i`.

2. **Process checkpoints left to right.**  
   Since all moves go strictly forward, this is a **Dynamic Programming on a DAG / shortest path in topological order** pattern. Left-to-right order is already a valid topological order.  
   **Why correct:** no later node can improve an earlier node.

3. **Skip unreachable states.**  
   If `dp[i]` is infinity, checkpoint `i` cannot be reached, so buying there is impossible and no transitions should be emitted.  
   **Invariant:** only reachable checkpoints generate candidate updates.

4. **Relax forward transitions.**  
   From checkpoint `i`, buying a pack costs `cost[i]` and allows movement to every `j` with `i < j <= min(n - 1, i + jump[i])`. For each such `j`, update:  
   `dp[j] = min(dp[j], dp[i] + cost[i])`.  
   **Why correct:** any valid path to `j` through `i` must pay the best cost to reach `i`, then the purchase cost at `i`.

5. **Interpret the terminal state carefully.**  
   Reaching checkpoint `n - 1` means landing there; no purchase is required at the destination. Therefore the answer is simply `dp[n - 1]` if finite, otherwise `-1`.

6. **Complexity rationale.**  
   Each checkpoint may relax up to `O(n)` forward edges, yielding `O(n^2)` time and `O(n)` space. Given the input bound, this is operationally safe and implementation-simple.

## 📊 Worked Example
Use `cost = [5, 1, 4, 2]`, `jump = [2, 2, 1, 0]`.

Let `dp[i]` = minimum cost to land on checkpoint `i`.

| Step | i | dp before | Reachable next checkpoints | Update cost | dp after |
|---|---:|---|---|---|---|
| Init | - | `[0, inf, inf, inf]` | - | - | `[0, inf, inf, inf]` |
| 1 | 0 | `[0, inf, inf, inf]` | `1, 2` | `0 + 5 = 5` | `[0, 5, 5, inf]` |
| 2 | 1 | `[0, 5, 5, inf]` | `2, 3` | `5 + 1 = 6` | `[0, 5, 5, 6]` |
| 3 | 2 | `[0, 5, 5, 6]` | `3` | `5 + 4 = 9` | `[0, 5, 5, 6]` |
| 4 | 3 | destination | none | - | `[0, 5, 5, 6]` |

Result: `dp[3] = 6`.

The optimal route is: start at `0`, buy for `5`, jump to `1`; buy at `1` for `1`, jump to `3`. Total cost = `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)` in the worst case. For each checkpoint `i`, the algorithm may scan and relax up to `jump[i]` forward positions, bounded by `n`. At `10^6` or `10^9` elements this would be too slow, but at `n <= 5000` it is entirely practical and keeps the implementation straightforward.

### Space Complexity
`O(n)` for the `dp` array storing the minimum cost to reach each checkpoint. This is already minimal for the direct DP formulation. Space cannot be meaningfully reduced without sacrificing readability or requiring a more specialized transition-optimization structure.

## 💡 Key Takeaways
- If each position unlocks a forward range and you need a minimum aggregate cost, think DP on indices or shortest path on a DAG.
- If greedy “take the cheapest current option” feels tempting but future reachability changes the decision surface, it is usually a dynamic programming signal.
- `dp[0]` must start at `0`, not `cost[0]`; you only pay `cost[i]` when using checkpoint `i` to depart.
- The destination does not require a purchase after arrival; charging `cost[n - 1]` is a common bug.
- In production planning systems, bounded forward dependencies often let you replace general graph search with ordered state propagation, improving both correctness reasoning and operational predictability.

## 🚀 Variations & Further Practice
- Add a limit on the number of purchases allowed. This turns the state into `dp[i][k]` and introduces a second optimization dimension.
- Allow backward jumps or arbitrary edges. The graph is no longer acyclic, so the solution shifts from DP to Dijkstra or Bellman-Ford depending on edge properties.
- Charge per-edge movement cost in addition to per-checkpoint purchase cost. The transition function becomes richer, and range-based optimization may require segment trees or monotonic structures.