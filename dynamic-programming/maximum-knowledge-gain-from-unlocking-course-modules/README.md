# Maximum Knowledge Gain from Unlocking Course Modules

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, DAG, Knapsack

---

## 🗂 Problem Overview
Given `n` modules with non-negative knowledge gains and prerequisite dependencies forming a DAG, choose at most `k` modules to maximize total gain. A module is selectable only if all of its prerequisites are also selected. The output is the maximum achievable gain under that budget. What makes this hard is that dependencies couple choices: selecting one high-value module may force multiple prerequisite picks, so local greedy decisions are not reliable.

## 🌍 Engineering Impact
This pattern shows up anywhere value-bearing work items are gated by dependency closure and constrained budgets. Examples include compiler optimization passes with prerequisite analyses, feature-flag rollout plans requiring upstream infrastructure, ML pipelines where derived features depend on upstream transforms, and build systems choosing artifacts under time budgets. At scale, naive prioritization by local value fails because the true unit of choice is not a node but its feasible dependency closure. Dynamic programming over dependency structure enables globally optimal planning, capacity-aware scheduling, and predictable trade-offs when resource ceilings are hard constraints rather than soft preferences.

## 🔍 Problem Statement
You are given:

- `n` modules labeled `0 .. n-1`
- `gain[i]`: knowledge gained by completing module `i`
- `prereq[i]`: list of prerequisite modules that must all be completed before `i`
- `k`: maximum number of modules you may complete

The prerequisite graph is a DAG. Each module can be completed at most once, and completing a module consumes one unit of the `k` budget. You may take fewer than `k` modules.

Return the maximum total gain achievable.

Constraints:

- `1 <= n <= 100`
- `0 <= gain[i] <= 10^6`
- `0 <= k <= n`
- `0 <= total prerequisite edges <= 300`

Examples:

- `n=5, gain=[3,4,8,5,10], prereq=[[],[0],[0],[1,2],[]], k=3` → `21`
- `n=6, gain=[5,1,7,4,6,20], prereq=[[],[0],[1],[],[3],[2,4]], k=4` → `17`

The key constraint is dependency closure on a DAG under a cardinality budget, which rules out simple sorting or standard independent-item knapsack.

## 🪜 How to Solve This
1. Read the problem → the first signal is that picking a node forces picking all ancestors. So the decision unit is not an isolated module.

2. Notice the graph is a DAG → that usually means topological order or DAG DP. Cycles would make feasibility more complex; acyclicity gives us a direction to process dependencies safely.

3. Ask whether plain knapsack works → not directly, because items are not independent. The value of taking a module depends on whether its prerequisites are already paid for.

4. Convert the DAG into a rooted forest by adding a super-source pointing to all indegree-zero modules. Then process each subtree as a dependency-constrained group.

5. For each node, compute `dp[u][t]`: maximum gain obtainable by selecting exactly `t` modules from the dependency-closed region rooted at `u`, assuming `u` itself is selected when `t > 0`.

6. Merge children one by one like tree knapsack → this is the core insight. Each child contributes an optional dependency-closed package, and combining packages is a bounded budget allocation problem.

7. The super-source lets us combine all independent components and read the answer as `max(dp[root][0..k])`.

The path to the solution is: DAG structure → dependency closure → rooted composition → tree knapsack DP.

## 🧩 Algorithm Walkthrough
1. **Build graph metadata and topological structure.**  
   Compute indegrees and identify modules with no prerequisites. Add a virtual root `R` with edges to all such modules. This gives one entry point for all feasible plans. The invariant is that every valid selection is represented somewhere under `R`.

2. **Compress the dependency handling into a rooted DP view.**  
   The useful pattern here is **Tree Knapsack DP** applied after rooting the dependency structure. For each node `u`, define `dp[u][t]` as the best gain from a dependency-closed selection of `t` modules within the region unlocked through `u`. If `t > 0`, `u` must be included; otherwise the state is infeasible except for the virtual root.

3. **Initialize each node locally.**  
   For a real module `u`, start with `dp[u][1] = gain[u]`. This is correct because selecting `u` alone is the minimal closed set before considering descendants. For the virtual root, initialize `dp[R][0] = 0`.

4. **Merge children bottom-up.**  
   Process nodes in reverse topological / postorder fashion. For each child `v` of `u`, merge `dp[v]` into `dp[u]` using knapsack convolution: distribute a total budget `t` between what is already chosen under `u` and what is additionally taken from `v`. The invariant is that after merging the first `m` children, `dp[u][t]` is optimal over exactly those children.

5. **Respect prerequisite closure.**  
   Because children are only merged into a parent state where the parent is already selected, every constructed state remains valid. This is why the abstraction works: feasibility is encoded structurally, not checked repeatedly.

6. **Extract the answer.**  
   The result is `max(dp[R][0], dp[R][1], ..., dp[R][k])`. Using “at most `k`” instead of “exactly `k`” matters because gains are non-negative but feasibility may prevent filling the budget.

## 📊 Worked Example
Use Example 1:

`gain = [3,4,8,5,10]`  
`prereq = [[], [0], [0], [1,2], []]`  
`k = 3`

Dependency structure: `R -> {0,4}`, `0 -> {1,2}`, `{1,2} -> 3`

| Node / Merge Step | DP states `(modules -> gain)` |
|---|---|
| `4` | `{1: 10}` |
| `1` | `{1: 4}` |
| `2` | `{1: 8}` |
| `3` | `{1: 5}` but unusable unless both `1` and `2` are chosen upstream |
| `0` start | `{1: 3}` |
| merge child `1` into `0` | `{1: 3, 2: 7}` |
| merge child `2` into `0` | `{1: 3, 2: 11, 3: 15}` |
| root `R` start | `{0: 0}` |
| merge subtree `0` | `{0: 0, 1: 3, 2: 11, 3: 15}` |
| merge subtree `4` | `{0: 0, 1: 10, 2: 13, 3: 21}` |

Best value for at most `3` modules is `21`, achieved by taking modules `{0,2,4}`.

## ⏱ Complexity Analysis
### Time Complexity
The dominant cost is knapsack-style merging across edges and budgets, giving `O(E * k^2)` in the practical tree-merge formulation, or `O(n * k^2)` when each node is merged once into its parent structure. With `n <= 100`, this is easily tractable. At `10^6` or `10^9` nodes, quadratic-in-budget DP is only viable when `k` is very small and aggressively bounded.

### Space Complexity
Space is `O(n * k)` for storing DP tables per node. The main owner is the subtree DP state. It can be reduced with destructive postorder merging or rolling arrays, but that trades away debuggability and often complicates correctness around child-by-child transitions.

## 💡 Key Takeaways
- If selecting an item requires selecting all ancestors and the graph is acyclic, think dependency-closure DP rather than greedy ranking.
- If the problem asks for “best value under a pick budget” and choices are structurally coupled, that is a strong signal for knapsack-style state composition.
- Be explicit about whether `dp[u][t]` means “exactly `t` picks” or “at most `t` picks”; mixing them silently corrupts merge logic.
- The virtual root is not cosmetic: without it, combining multiple independent prerequisite components becomes error-prone and special-cased.
- In production planning systems, the transferable insight is that the schedulable unit is often a closure over dependencies, not an individual task, feature, or artifact.

## 🚀 Variations & Further Practice
- **Weighted budget instead of module count:** each module has a study-time cost, and the budget is total time. Same dependency closure, but knapsack capacity is no longer bounded by `n`.
- **Require exactly `k` modules:** removes the ability to stop early, which changes base cases and makes infeasible states more important to model correctly.
- **General DAG with shared descendants counted once:** harder because tree-style decomposition breaks when multiple paths reach the same prerequisite; you need DAG-specific state management or subset compression on small-width graphs.