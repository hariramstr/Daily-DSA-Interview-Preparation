# Minimum Cost to Place Review Gates in a Dependency Tree

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Tree DP, Depth-First Search

---

## 🗂 Problem Overview
Given a rooted tree of `n` modules, each with gate cost `risk[i]`, choose where to place review gates so every module is protected at minimum total cost. A gate on node `i` protects `i`, its parent, and its direct children. Input is `risk` plus `n - 1` tree edges; output is the minimum achievable cost. The hard part is dependency between parent and child decisions: local cheapest choices can leave ancestors uncovered or force expensive downstream fixes.

## 🌍 Engineering Impact
This pattern shows up in dependency governance, service-mesh policy placement, compiler pass insertion, hierarchical monitoring, and access-control propagation. You are choosing a minimum-cost set of control points on a tree where each control point covers a small neighborhood, not an arbitrary subtree. At production scale, greedy placement overweights local savings and underestimates upstream coverage gaps, leading to policy holes, redundant controls, or inflated rollout cost. Tree DP gives a compositional contract per subtree: each component reports what it costs under precise boundary conditions, enabling predictable optimization, incremental reasoning, and correctness under large fan-out and deep hierarchies.

## 🔍 Problem Statement
You are given a tree with `n` nodes labeled `0..n-1`, rooted at `0`. Each node `i` has non-negative cost `risk[i]`. Placing a gate at node `i` costs `risk[i]` and protects exactly three categories of nodes: `i` itself, its parent, and all of its direct children. Every node in the tree must be protected by at least one placed gate.

Return the minimum total cost.

Constraints:

- `1 <= n <= 100000`
- `0 <= risk[i] <= 1000000000`
- `edges.length == n - 1`
- `edges` forms a valid tree
- Answer fits in signed 64-bit integer

Examples:

- `risk = [5,2,4,6]`, `edges = [[0,1],[1,2],[1,3]]` → `2`
- `risk = [7,3,8,2,5,1]`, `edges = [[0,1],[0,2],[1,3],[1,4],[2,5]]` → `4`

The key constraint is `n = 1e5`: exponential subset search and naive parent/child backtracking are impossible, so the solution must summarize each subtree in constant-size DP states.

## 🪜 How to Solve This
1. Read the coverage rule carefully → a gate affects only distance-1 relatives in the rooted tree, not an entire subtree. That immediately rules out simple subtree-greedy logic.

2. Notice the dependency direction → whether node `u` is valid depends on three possibilities: it has a gate, its parent has a gate, or one of its children has a gate.

3. That suggests a tree DP boundary contract → for each node, compute the minimum cost under each of those protection modes.

4. Define three states:
   - `u` has a gate.
   - `u` has no gate but is protected by its parent.
   - `u` has no gate and must be protected by at least one child.

5. Process children bottom-up with DFS → each child subtree can be solved independently once the parent-state assumption is fixed.

6. The only nontrivial merge is the “must be protected by a child” state → among children, at least one must place a gate. This becomes a standard “all choose cheapest, then force one child into a specific state if needed” transition.

7. Root handling is special → it has no parent, so the final answer cannot use the “protected by parent” state.

## 🧩 Algorithm Walkthrough
1. **Build the rooted tree with DFS/BFS orientation.**  
   Convert the undirected edge list into parent/children relationships rooted at `0`. This removes ambiguity in the coverage rule. Invariant: every non-root node has exactly one parent, and transitions only depend on children.

2. **Use Tree DP with three states per node.**  
   Let:
   - `dp0[u]`: minimum cost if `u` has a gate.
   - `dp1[u]`: minimum cost if `u` has no gate and is protected by its parent.
   - `dp2[u]`: minimum cost if `u` has no gate and must be protected by at least one child.  
   This is the right abstraction because protection can arrive from exactly one level above or below, or from the node itself.

3. **Transition for `dp0[u]`.**  
   If `u` has a gate, each child is already protected by its parent. A child may still place its own gate or defer to its children. So for each child `v`, add `min(dp0[v], dp1[v], dp2[v])`. Invariant: every child subtree remains fully covered.

4. **Transition for `dp1[u]`.**  
   If `u` is protected by its parent and has no gate, then no child gate is required to cover `u`. But each child cannot assume protection from `u`, since `u` has no gate. Therefore each child must be either gated itself or protected by one of its children: add `min(dp0[v], dp2[v])`.

5. **Transition for `dp2[u]`.**  
   Now `u` has no gate and no parent protection, so at least one child must have a gate. For each child, baseline choice is `min(dp0[v], dp2[v])`. Track the extra cost to force a child into `dp0[v]` instead of its baseline. Sum baselines, then add the smallest forcing delta if no child naturally chose `dp0`. This maintains the invariant that `u` becomes protected from below.

6. **Handle leaves correctly.**  
   For a leaf:
   - `dp0 = risk[leaf]`
   - `dp1 = 0` because parent’s gate protects it
   - `dp2 = INF` because no child exists to protect it  
   This base case is what makes the parent/child contracts consistent.

7. **Return the root answer.**  
   Root cannot be protected by a parent, so answer is `min(dp0[root], dp2[root])`.

## 📊 Worked Example
Use `risk = [7,3,8,2,5,1]`, `edges = [[0,1],[0,2],[1,3],[1,4],[2,5]]`.

Bottom-up DP:

| Node | Children | `dp0` | `dp1` | `dp2` |
|---|---|---:|---:|---:|
| 3 | - | 2 | 0 | INF |
| 4 | - | 5 | 0 | INF |
| 5 | - | 1 | 0 | INF |
| 1 | 3,4 | 3 + 0 + 0 = 3 | 2 + 5 = 7 | force one child gate → 7 |
| 2 | 5 | 8 + 0 = 8 | 1 | 1 |
| 0 | 1,2 | 7 + min(3,7,7) + min(8,1,1) = 11 | invalid for final | baseline `min(3,7)+min(8,1)=4`, already includes child 1 gated |

Final answer: `min(dp0[0], dp2[0]) = min(11, 4) = 4`.

Interpretation: gate at `1` protects `0,1,3,4`; gate at `5` protects `2,5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is visited once, and each edge is processed a constant number of times while building the rooted tree and merging child DP states. At `10^6` nodes this is still linear and practical with iterative traversal; at `10^9`, even linear time is operationally infeasible in memory and wall-clock terms.

### Space Complexity
`O(n)`. Space is owned by the adjacency list, parent/children representation, and three DP arrays. It can be reduced slightly by reusing storage during postorder traversal, but asymptotically remains linear because the tree itself must be represented.

## 💡 Key Takeaways
- If a tree problem says a node can be satisfied by itself, its parent, or one of its children, that is a strong signal for constant-state Tree DP rather than greedy traversal.
- When subtree decisions depend on a small boundary contract with the parent, define DP states around that contract instead of around raw “covered/uncovered” booleans.
- The root is a special case: it cannot use any state that assumes parent coverage, so final-state selection must exclude that branch.
- In the “must be covered by a child” transition, forgetting to enforce “at least one child actually has a gate” produces subtly invalid minima.
- The transferable design insight is to summarize each subtree by the minimal interface the rest of the system needs, which is exactly how scalable optimization and modular reasoning work in production architectures.

## 🚀 Variations & Further Practice
- Require every node to be protected by at least **two** gates for redundancy. The twist is multiplicity: DP must track coverage count contributions from parent, self, and children.
- Change coverage from distance `1` to distance `k` on the tree. The harder part is that the parent/child boundary is no longer constant-size unless states encode remaining coverage radius.
- Add forbidden nodes where gates cannot be placed. This turns some previously valid states into impossible ones and stresses correctness around `INF` propagation and root feasibility.