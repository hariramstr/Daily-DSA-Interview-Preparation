# Maximum Inherited Budget After One Department Freeze

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Prefix/Suffix Maximum

---

## 🗂 Problem Overview
Given a rooted tree, compute each node’s root-to-node budget sum, then choose at most one subtree to remove so the maximum inherited budget among the remaining nodes is as large as possible. The CEO node cannot be removed, so at least one node always survives. The challenge is not computing path sums, but answering: for every possible frozen subtree, what is the maximum path sum outside that subtree — in subquadratic time for `n` up to `200000`.

## 🌍 Engineering Impact
This pattern shows up whenever a hierarchical structure must support “exclude one region, then query the best remaining candidate.” Examples include disabling a service shard in a dependency tree, suppressing a compiler scope while preserving symbol visibility elsewhere, removing a bad branch in a search/ranking taxonomy, or quarantining a subtree in an org/resource graph. At scale, recomputing the best surviving value per excluded subtree is the failure mode: naive traversal becomes `O(n^2)` and collapses under large trees. The right approach converts subtree exclusion into interval exclusion, enabling linear preprocessing and predictable latency.

## 🔍 Problem Statement
You are given a rooted tree with `n` departments, root `0`, directed conceptually from parent to child. Each node `i` has an integer `budget[i]`, possibly negative. For any node `u`, its inherited budget is the sum of `budget` values along the path from node `0` to `u`, inclusive.

You may freeze at most one department `x`, where `x != 0`. Freezing removes the entire subtree rooted at `x`. Among all still-active nodes, return the maximum inherited budget. You may also choose not to freeze any subtree.

Constraints:
- `1 <= n <= 200000`
- `edges.length == n - 1`
- `-10^9 <= budget[i] <= 10^9`
- Answer fits in signed 64-bit integer

Example 1:
- `edges = [[0,1],[0,2],[1,3],[1,4]]`
- `budget = [4,-2,3,5,-1]`
- Output: `7`

Example 2:
- `edges = [[0,1],[0,2],[1,3],[2,4],[2,5]]`
- `budget = [5,4,-10,8,20,1]`
- Output: `17`

The key constraint is `n = 200000`: trying every freeze and rescanning the tree will time out.

## 🪜 How to Solve This
1. Read the problem → the inherited budget of each node is just a root-to-node prefix sum on a tree. That part is standard DFS.

2. The real question is: if subtree `x` is removed, what is the maximum path sum among nodes **not** in that subtree?

3. “Subtree exclusion” on trees is a strong signal to flatten the tree with an Euler tour. In Euler order, every subtree becomes one contiguous interval `[tin[x], tout[x]]`.

4. After flattening, the problem becomes: for each interval, find the maximum value outside it. That is no longer a tree problem; it is an array problem.

5. Maximum outside an interval suggests prefix/suffix maxima:
   - best before `tin[x]`
   - best after `tout[x]`

6. So the plan is:
   - DFS once to compute path sums and Euler positions
   - build an array of path sums in Euler order
   - precompute prefix max and suffix max
   - for every non-root node, evaluate freezing its subtree in `O(1)`

7. Also compare against “freeze nothing,” which is simply the global maximum path sum.

That gives linear time overall.

## 🧩 Algorithm Walkthrough
1. **Build the rooted adjacency list.**  
   Store children for each node from the given edges. The invariant is simple: traversal from `0` reaches every node exactly once because the input is a valid rooted tree.

2. **Run DFS to compute path sums and Euler tour indices.**  
   For each node `u`, compute  
   `pathSum[u] = pathSum[parent] + budget[u]`.  
   Assign entry time `tin[u]` when first visited, and exit boundary `tout[u]` after processing its subtree. In Euler preorder, every subtree of `u` occupies a contiguous segment `[tin[u], tout[u]]`.

3. **Materialize path sums in Euler order.**  
   Create `flat[tin[u]] = pathSum[u]`.  
   Now subtree removal is equivalent to deleting one contiguous subarray from `flat`.

4. **Precompute prefix and suffix maxima.**  
   `pref[i] = max(flat[0..i])` and `suff[i] = max(flat[i..n-1])`.  
   This is the **Prefix/Suffix Maximum** pattern: constant-time “best outside interval” queries after linear preprocessing.

5. **Evaluate each possible freeze.**  
   For every node `x != 0`, the best surviving inherited budget is  
   `max(pref[tin[x]-1], suff[tout[x]+1])`, handling boundaries carefully.  
   This is correct because every active node lies either before or after the subtree interval in Euler order.

6. **Include the no-freeze option.**  
   The answer may already be optimal without removing anything, so compare all freeze candidates with `pref[n-1]`, the global maximum.

7. **Return the maximum over all choices.**  
   The invariant throughout is: each candidate represents the exact maximum inherited budget among nodes outside one excluded subtree.

## 📊 Worked Example
Use Example 1:

- `edges = [[0,1],[0,2],[1,3],[1,4]]`
- `budget = [4,-2,3,5,-1]`

DFS preorder: `0, 1, 3, 4, 2`

| Node | Path Sum | tin | tout |
|---|---:|---:|---:|
| 0 | 4 | 0 | 4 |
| 1 | 2 | 1 | 3 |
| 3 | 7 | 2 | 2 |
| 4 | 1 | 3 | 3 |
| 2 | 7 | 4 | 4 |

So:

- `flat = [4, 2, 7, 1, 7]`
- `pref = [4, 4, 7, 7, 7]`
- `suff = [7, 7, 7, 7, 7]`

Evaluate freezes:

1. Freeze `1` → remove interval `[1,3]`  
   Remaining max = `max(pref[0], suff[4]) = max(4, 7) = 7`

2. Freeze `3` → remove `[2,2]`  
   Remaining max = `max(pref[1], suff[3]) = max(4, 7) = 7`

3. Freeze `4` → remove `[3,3]`  
   Remaining max = `max(pref[2], suff[4]) = max(7, 7) = 7`

4. Freeze `2` → remove `[4,4]`  
   Remaining max = `pref[3] = 7`

No freeze also gives `7`, so the answer is `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One DFS computes path sums and Euler indices, one linear pass builds prefix maxima, one builds suffix maxima, and one evaluates all freeze candidates. At `10^6` nodes this is still practical in systems languages; at `10^9`, even linear scans become infeasible, so the asymptotic improvement from `O(n^2)` is necessary but not sufficient.

### Space Complexity
`O(n)`. The adjacency list, Euler-order array, path sums, and prefix/suffix max arrays dominate memory. You can reduce some duplication by writing directly into Euler-order storage during DFS, but asymptotically it remains linear; the trade-off is tighter implementation coupling and reduced readability.

## 💡 Key Takeaways
• If a tree problem asks about removing or querying an entire subtree, Euler tour flattening should be one of the first transformations you consider.  
• If the post-transformation question becomes “best value outside an interval,” prefix/suffix maxima are usually the right constant-time query primitive.  
• Be precise about subtree interval boundaries: in preorder flattening, the subtree is the inclusive range `[tin[u], tout[u]]`, not half-open unless you define it that way consistently.  
• Use 64-bit integers for path sums; node values are `1e9` and depth can be `2e5`, so 32-bit arithmetic will overflow immediately.  
• The transferable design insight is to turn hierarchical exclusion into contiguous-range exclusion, then solve it with cheap array precomputation instead of repeated structural traversal.

## 🚀 Variations & Further Practice
- Allow freezing **up to k subtrees** instead of one. The conceptual jump is from single-interval exclusion to selecting multiple disjoint intervals, which pushes the solution toward DP on Euler order or tree DP with merge states.
- Support **online updates** to budgets and repeated freeze queries. The hard part becomes maintaining root-to-node sums and outside-subtree maxima dynamically, which typically requires heavy-light decomposition, Fenwick/segment trees, or reroot-aware data structures.
- Ask for the **node achieving** the best surviving inherited budget, with deterministic tie-breaking. The twist is carrying argmax metadata through prefix/suffix preprocessing rather than just scalar maxima.