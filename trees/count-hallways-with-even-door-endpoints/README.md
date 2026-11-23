# Count Hallways With Even Door Endpoints

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Binary Tree

---

## 🗂 Problem Overview
Given the root of a binary tree, count how many non-leaf nodes form a valid hallway. A node qualifies only if its own value is even and every child that actually exists also has an even value. Missing children are ignored; leaf nodes are excluded because they have no child connections. The output is a single integer count. The only real constraint is structural: you must inspect parent-child relationships across the entire tree without double-counting or missing sparse branches.

## 🌍 Engineering Impact
This pattern shows up anywhere local structural validation must be applied across a hierarchical graph: filesystem policy scans, compiler AST validation, IAM inheritance trees, configuration rollout DAGs simplified to trees, and service dependency health checks. At scale, the important idea is not the parity rule itself but the traversal discipline: evaluate each node once, derive a local predicate from adjacent state, and aggregate globally. Without that approach, teams drift into repeated subtree scans, inflated latency, and brittle logic around missing children. A single-pass traversal keeps validation cost linear, predictable, and easy to compose into larger tree-processing pipelines.

## 🔍 Problem Statement
You are given a binary tree where each node contains an integer room number. A hallway is any non-leaf node together with its direct child links. Count how many such hallways are valid under this rule:

- the parent node’s value is even, and
- every existing child of that parent is also even.

A node with no children does **not** count as a hallway. A node with one child is valid if both the parent and that single child are even.

**Constraints**
- Number of nodes: `1 <= n <= 1000`
- `-10^4 <= Node.val <= 10^4`
- Tree shape is arbitrary binary tree, including sparse trees

**Examples**
- `root = [8,4,6,2,7,null,10]` → `2`
- `root = [5,2,8,4,6]` → `1`

The key constraint is that every node may matter once, but no node requires information beyond itself and its immediate children, which strongly suggests a full-tree traversal with constant-time work per node.

## 🪜 How to Solve This
1. Read the rule carefully → validity is purely **local**. A node only depends on:
   - its own value
   - whether it has children
   - the values of those existing children

2. Since every node may be a candidate hallway, we need to visit the entire tree → that immediately suggests DFS or BFS.

3. Ask whether subtree results are needed → no. We are not comparing depths, balancing, or aggregating descendant properties. Each node can be judged independently once reached.

4. That means the simplest plan is:
   - traverse every node exactly once
   - skip leaves
   - for non-leaf nodes, check parity of the parent and each present child
   - increment a counter when the condition holds

5. Why DFS first? It is the most natural fit for recursive tree inspection and keeps the logic close to the structure: “visit node, evaluate local rule, recurse into children.”

6. BFS would also work, but it adds queue management without improving asymptotics. For this problem, the main insight is not traversal order; it is recognizing a **single-pass local predicate over a tree**.

## 🧩 Algorithm Walkthrough
1. **Use Depth-First Search (DFS)** as the traversal pattern.  
   DFS is the right abstraction because the tree must be fully visited, and each node requires only constant-time local validation. Traversal order does not affect correctness.

2. **At each node, determine whether it is a hallway candidate.**  
   A node is a candidate only if it has at least one child. This preserves the problem invariant that leaves never count, regardless of their value.

3. **Check the parent’s parity first.**  
   If the node value is odd, it cannot be valid. This is an immediate fail and matches the rule exactly.

4. **Check each existing child independently.**  
   If `left` exists, it must be even. If `right` exists, it must be even. Missing children impose no constraint.  
   The invariant here is: after evaluating a node, the algorithm has correctly classified that hallway using only allowed local information.

5. **Increment the answer when all required checks pass.**  
   This count is monotonic: once incremented, it never needs revision because no future traversal step can change a node’s local parent-child relationship.

6. **Recurse into left and right subtrees.**  
   This guarantees every node is examined exactly once. Since each visit performs O(1) work, total runtime is linear in the number of nodes.

7. **Return the accumulated count.**  
   Correctness follows from exhaustive traversal plus exact implementation of the hallway predicate at every non-leaf node.

## 📊 Worked Example
Example: `root = [8,4,6,2,7,null,10]`

| Step | Node | Children Present | Parent Even? | Existing Children Even? | Count |
|---|---:|---|---|---|---:|
| 1 | 8 | 4, 6 | Yes | Yes, Yes | 1 |
| 2 | 4 | 2, 7 | Yes | Yes, **No** | 1 |
| 3 | 2 | none | — | — | 1 |
| 4 | 7 | none | — | — | 1 |
| 5 | 6 | 10 | Yes | Yes | 2 |
| 6 | 10 | none | — | — | 2 |

Trace:
1. Start at `8`: non-leaf, `8`, `4`, and `6` are even → valid.
2. Visit `4`: non-leaf, but child `7` is odd → invalid.
3. Nodes `2` and `7` are leaves → ignored.
4. Visit `6`: one existing child `10`, both even → valid.
5. Node `10` is a leaf → ignored.  
Final answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
The runtime is **O(n)**, where `n` is the number of nodes, because each node is visited exactly once and evaluated with constant-time checks on at most two children. At `10^6` nodes this remains practical as a linear scan; at `10^9`, linear work becomes infrastructure-bound and traversal locality starts dominating execution cost.

### Space Complexity
The auxiliary space is **O(h)** for recursive DFS call stack, where `h` is tree height. In the worst case of a skewed tree, `h = n`; in a balanced tree, `h = log n`. An iterative BFS/DFS can avoid recursion depth risk, trading stack frames for an explicit queue or stack.

## 💡 Key Takeaways
- If a tree problem asks you to evaluate each node using only itself and immediate children, think single-pass DFS/BFS with O(1) work per node.
- When the output is a count of nodes satisfying a local predicate, avoid subtree recomputation; traverse once and accumulate.
- Leaves do not count here, even if their value is even; “no children” means “not a hallway,” not “vacuously valid.”
- Missing children are ignored, not treated as failing conditions; only existing child endpoints must be even.
- The transferable design insight is to separate **local validation logic** from **global traversal mechanics**, which keeps large tree-processing systems linear, composable, and easy to reason about.

## 🚀 Variations & Further Practice
- Count nodes where the parent is even and **all descendants** are even. The twist is that local checks are no longer sufficient; you need subtree summaries.
- Return the **largest valid hallway-connected subtree** instead of a count. The harder part is combining local validity with subtree size aggregation.
- Generalize from binary trees to arbitrary `k`-ary trees or dependency hierarchies. The conceptual shift is handling variable fan-out while preserving linear traversal.