# Maximum Credits from Course Plan with Prerequisite Chains

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, tree-dp, knapsack

---

## 🗂 Problem Overview
Given `n` courses, each course has a credit value and at most one prerequisite. You may take at most `maxCourses` courses, but any chosen course forces you to also choose every prerequisite on its path to a root. The goal is to maximize total credits.

The challenge is that this is not a standard knapsack over independent items. Choices are dependency-closed, so selecting one node may require committing budget to several ancestors first.

## 🌍 Engineering Impact
This pattern shows up anywhere local choices are constrained by dependency closure. Package managers cannot install a leaf package without its dependency chain. Build systems cannot compile a target without upstream artifacts. Workflow engines, streaming DAGs, and feature-flag rollout systems often require enabling prerequisite stages before downstream ones.

At small scale, brute force or greedy heuristics may appear to work. At production scale, they fail because value is attached to subgraphs, not isolated nodes. Tree-DP with knapsack-style merging gives a disciplined way to optimize under hierarchical dependencies and bounded capacity, which is exactly what you need when resource budgets, rollout slots, or execution windows are limited.

## 🔍 Problem Statement
You are given:

- `credits[i]`: credits earned by taking course `i`
- `prereq[i]`: the prerequisite of course `i`, or `-1` if none
- `maxCourses`: maximum number of courses allowed

The prerequisite graph is acyclic, and each node has at most one parent, so the structure is a forest of rooted trees. A valid selection must be closed under prerequisites: if course `u` is selected, every ancestor of `u` must also be selected.

Return the maximum total credits obtainable using at most `maxCourses` courses.

Constraints:

- `1 <= n <= 1000`
- `1 <= credits[i] <= 10^4`
- `-1 <= prereq[i] < n`
- `prereq[i] != i`
- graph is acyclic
- `1 <= maxCourses <= n`

Examples:

- `credits = [3,5,4,8]`, `prereq = [-1,0,1,-1]`, `maxCourses = 3` → `16` via courses `[0,1,3]`
- `credits = [2,7,6,4,5]`, `prereq = [-1,0,0,1,1]`, `maxCourses = 3` → `15` via courses `[0,1,2]`

The key constraint is prerequisite closure over a forest, which rules out flat DP or greedy selection.

## 🪜 How to Solve This
1. Read the dependency rule → notice courses are not independent items. A child is only legal if its entire ancestor chain is already included.

2. Translate the graph shape → since each node has at most one prerequisite and there are no cycles, the structure is a forest. That immediately suggests tree-based reasoning.

3. Ask what a subtree means → if you decide to take a course, all chosen descendants must remain valid under that course. So each subtree can be solved independently, then merged upward.

4. Define the right DP state → let `dp[u][k]` be the maximum credits from the subtree rooted at `u` using exactly `k` selected courses, with the prerequisite rule respected. If `k > 0`, course `u` itself must be included.

5. Handle multiple roots → add a virtual super-root with credit `0` connected to every real root. Then the whole forest becomes one tree, and the final answer is the best value for selecting up to `maxCourses` real courses.

6. Merge children like knapsack → each child contributes a menu of valid subtree choices. Combining those menus is exactly a tree-DP + knapsack merge.

Once you see “hierarchical dependencies + bounded picks,” this pattern becomes the obvious tool.

## 🧩 Algorithm Walkthrough
1. **Build the forest as adjacency lists.**  
   For every course `i`, attach it to `prereq[i]` if present; otherwise attach it to a virtual root. This is correct because each node has only one parent, so the graph is already a forest. The invariant is: every valid global selection corresponds to a valid rooted-subtree selection under the virtual root.

2. **Run DFS bottom-up.**  
   Process children before parents so each child subtree is fully solved before merging. This is the standard **Tree DP** setup: local optimal states are aggregated upward.

3. **Initialize each node’s DP.**  
   Let `dp[u][0] = 0` only for the virtual root. For a real node `u`, initialize `dp[u][1] = credits[u]`. This encodes the prerequisite invariant: selecting anything from `u`’s subtree requires selecting `u` first.

4. **Merge child DP tables using a knapsack convolution.**  
   Suppose current node `u` has partial table `dp[u]`, and child `v` has `dp[v]`. For every feasible pair `(a, b)`, update  
   `newDp[a + b] = max(newDp[a + b], dp[u][a] + dp[v][b])`.  
   This is a **Knapsack-style subtree merge**. It is correct because child selections are independent once the parent is already included.

5. **Track subtree sizes to bound loops.**  
   After processing a child, update the known size of `u`’s solvable subtree. This keeps the merge bounded by actual subtree size and `maxCourses`, not by `n`.

6. **Extract the answer from the virtual root.**  
   The final result is `max(dp[superRoot][k])` for `0 <= k <= maxCourses`. The virtual root contributes no credit and removes the need for special handling across multiple disconnected trees.

## 📊 Worked Example
Example: `credits = [2,7,6,4,5]`, `prereq = [-1,0,0,1,1]`, `maxCourses = 3`

Tree:
- `0`
  - `1`
    - `3`
    - `4`
  - `2`

Bottom-up DP states:

| Node | Valid `dp[node][k]` |
|---|---|
| `3` | `k=1 -> 4` |
| `4` | `k=1 -> 5` |
| `2` | `k=1 -> 6` |
| `1` | start: `1 -> 7`; merge `3`: `2 -> 11`; merge `4`: `2 -> 12`, `3 -> 16` |
| `0` | start: `1 -> 2`; merge `1`: `2 -> 9`, `3 -> 14`; merge `2`: `2 -> 8`, `3 -> 15` |

Interpretation:
- `0 + 1 + 3 = 13`
- `0 + 1 + 4 = 14`
- `0 + 1 + 2 = 15` ← best within 3 courses

So the answer is `15`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * maxCourses^2)` in the standard implementation. Each edge participates in a knapsack-style merge, and each merge considers combinations of selected-course counts up to `maxCourses`. With `n <= 1000`, this is practical. At `10^6` or `10^9` scale, quadratic-in-capacity merging becomes the bottleneck and would require approximation or stronger structural constraints.

### Space Complexity
`O(n * maxCourses)` if you store a DP table per node. The dominant cost is subtree DP state. It can be reduced with more careful in-place merging or by freeing child tables after merge, but that increases implementation complexity and makes debugging harder.

## 💡 Key Takeaways
- If items are only selectable together with all ancestors, you are not solving flat knapsack; you are solving dependency-closed optimization on a tree or forest.
- “Each node has at most one parent” is a strong signal for tree-DP, especially when the objective is constrained by a count or capacity budget.
- For real nodes, `dp[u][0]` should usually be invalid: taking zero from a subtree while still allowing descendants would violate prerequisite closure.
- When merging child tables, iterate counts carefully and cap by both subtree size and `maxCourses`; otherwise you introduce silent overcounting or out-of-bounds states.
- The transferable design insight is to collapse a forest with a virtual root, turning fragmented dependency domains into one uniform optimization surface.

## 🚀 Variations & Further Practice
- Allow each course to have multiple prerequisites, turning the forest into a general DAG. The conceptual jump is that subtree independence disappears, so simple tree merges no longer work.
- Require selecting exactly `maxCourses` instead of at most `maxCourses`, which removes the final max-over-prefix shortcut and makes invalid-state handling more important.
- Add negative credits or per-course workload alongside credits, creating a multi-constraint dependency knapsack where feasibility and optimality interact more sharply.