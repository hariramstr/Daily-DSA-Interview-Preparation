# Minimum Relay Upgrades to Stabilize a Signal Tree

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Tree DP, DFS, GCD

---

## 🗂 Problem Overview
Given a rooted tree, each node has a noise value, and every root-to-node path must end up with path GCD exactly `1`. One operation upgrades a node by replacing its value with any positive integer. Because a changed node affects every path through its subtree, local choices have global consequences. The task is to compute the minimum number of upgraded nodes needed to make all root-to-node paths stable under constraints up to `2 * 10^5` nodes.

## 🌍 Engineering Impact
This pattern shows up whenever a hierarchical dependency carries an accumulated state and a mutation at one point rewrites the state for an entire downstream region. Examples include distributed policy inheritance, compiler scope resolution, streaming DAG sanitization, certificate-chain validation, and search-ranking feature propagation. At scale, brute-force recomputation per path is dead on arrival: too many overlapping prefixes, too much repeated work. The useful abstraction is “tree traversal with compressed ancestor state,” where equivalent upstream states are merged. That enables predictable latency, bounded memory growth, and decisions that remain explainable under large fanout and deep hierarchies.

## 🔍 Problem Statement
You are given a tree with `n` nodes, rooted at `0`, where `noise[i]` is the value at station `i`. A root-to-node path is **stable** if the GCD of all values on that path is exactly `1`. In one upgrade, you may replace any node’s value with any positive integer. The new value applies to every path passing through that node. Return the minimum number of upgraded nodes required so that **every** root-to-node path, including the path containing only node `0`, is stable.

Constraints:

- `1 <= n <= 2 * 10^5`
- `1 <= noise[i] <= 10^9`
- `edges.length == n - 1`
- The graph is a tree

Examples:

- `n = 5, edges = [[0,1],[0,2],[1,3],[1,4]], noise = [6,10,15,9,25]` → `1`
- `n = 4, edges = [[0,1],[1,2],[1,3]], noise = [6,10,7,15]` → `2`

The algorithmic pressure comes from subtree-wide side effects plus a large state space if ancestor-path GCDs are tracked naively.

## 🪜 How to Solve This
1. Start from the requirement, not the operation: every root-to-node path must have GCD `1`.
2. Notice the root path is just `[0]` → if `noise[0] != 1`, the root must be upgraded. That immediately removes one degree of freedom.
3. For any non-root node, whether its path is already stable depends only on the GCD accumulated from its ancestors and its own value.
4. That suggests DFS with a carried state: current path GCD before entering this node.
5. But a node upgrade is special: if you set this node to `1`, every descendant path through it becomes stable immediately, regardless of previous GCD.
6. So each node has two meaningful choices:
   - keep its value and continue with `gcd(parentGcd, noise[u])`
   - upgrade it to `1`, pay `1`, and terminate all constraints in its subtree
7. This turns the problem into tree DP with memoization on `(node, incomingGcd)`.
8. The key optimization is that path GCD values collapse aggressively: along any path, the number of distinct GCDs is small because each new GCD must divide the previous one.

## 🧩 Algorithm Walkthrough
1. **Build the rooted tree with DFS/BFS orientation.**  
   Convert the undirected adjacency list into parent/children relationships rooted at `0`. This avoids revisiting parents and gives a clean subtree DP structure.

2. **Define the DP state: `dp(u, g)`** where `g` is the GCD of values on the path from the root to `u`’s parent after all decisions above `u`.  
   This is the right abstraction because the future only depends on the compressed ancestor effect, not the full path.

3. **At node `u`, evaluate two transitions.**  
   - **Keep `u` unchanged:** new GCD is `g' = gcd(g, noise[u])` (or `noise[u]` if `u` is root). This is valid only if all descendants can be fixed under that new incoming state.  
   - **Upgrade `u` to `1`:** cost `1`, and the path GCD to `u` becomes `1`. Since every descendant path includes `u`, all paths in the subtree are already stable; no further upgrades are needed below. Subtree cost becomes exactly `1`.

4. **Base correctness invariant.**  
   `dp(u, g)` equals the minimum upgrades needed so every root-to-node path ending inside `u`’s subtree is stable, assuming ancestor path GCD before `u` is `g`.

5. **Subtree aggregation for the keep case.**  
   If `g' == 1`, then every path to every descendant already contains a prefix with GCD `1`, and extending a path preserves GCD `1`. Therefore the entire subtree needs `0` additional upgrades.  
   If `g' > 1`, each child must be solved independently with incoming GCD `g'`, so cost is `sum(dp(child, g'))`.

6. **Memoize by `(u, g)`.**  
   This is the core **Tree DP + compressed GCD state** pattern. Distinct `g` values per node remain small in practice and are bounded by the divisor-chain behavior of repeated GCD reduction.

7. **Answer is `dp(0, 0)`** using `0` as the neutral “no prior value” sentinel, since `gcd(0, x) = x`.

## 📊 Worked Example
Use `n = 5`, `edges = [[0,1],[0,2],[1,3],[1,4]]`, `noise = [6,10,15,9,25]`.

| Step | Node | Incoming GCD | Keep Result | Upgrade Result | Best |
|---|---:|---:|---:|---:|---:|
| 1 | 0 | 0 | `gcd(0,6)=6` | set to `1` → cost `1` | ? |
| 2 | 1 | 6 | `gcd(6,10)=2` | cost `1` for subtree | depends |
| 3 | 3 | 2 | `gcd(2,9)=1` → subtree done | `1` | `0` |
| 4 | 4 | 2 | `gcd(2,25)=1` → subtree done | `1` | `0` |
| 5 | 2 | 6 | `gcd(6,15)=3` and leaf unstable | `1` | `1` |

If we keep node `0`, child costs are:
- subtree at `1`: `0`
- subtree at `2`: `1`

Total = `1`.  
If we upgrade node `0` to `1`, every path already has GCD `1`, so total = `1`.  
Minimum answer: `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * s)`, where `s` is the number of distinct incoming GCD states memoized per node. In practice `s` is small because GCD values shrink along divisor chains, so this is near-linear. At `10^6` nodes this remains feasible with iterative traversal and tight maps; `10^9` is irrelevant here because value magnitude affects GCD cost, not state explosion directly.

### Space Complexity
`O(n * s)` for the memo table plus `O(n)` for the adjacency list and rooted tree structure. Space is owned primarily by per-node cached GCD states. It can be reduced with on-the-fly state merging, but that usually trades memory for more recomputation and implementation complexity.

## 💡 Key Takeaways
- If a tree problem carries an accumulated path property and subtree decisions depend only on that compressed property, think tree DP over ancestor state.
- If the path aggregate is GCD/AND/min-like, expect strong state compression because many prefixes collapse to the same value.
- The root is an edge case with real semantic weight here: the path `[0]` must itself have GCD `1`, so node `0` may be forced.
- Upgrading a node to `1` is not just a local fix; it short-circuits the entire subtree because every descendant path includes that node.
- In production systems, this is the same optimization pattern as pushing a canonicalizing rewrite high in a dependency tree to eliminate downstream remediation work.

## 🚀 Variations & Further Practice
- Require every root-to-node path GCD to belong to a set `S` instead of exactly `1`; the harder part is that “upgrade to `1` solves everything” no longer holds, so subtree termination disappears.
- Add weighted upgrade costs per node; same DP shape, but now the optimal cut point in the tree depends on heterogeneous economics rather than pure count.
- Support online updates to `noise[i]` with repeated queries; this shifts the problem toward heavy-light decomposition or dynamic tree structures with cached GCD-state summaries.