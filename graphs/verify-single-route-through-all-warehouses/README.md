# Verify Single Route Through All Warehouses

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, DFS, Connectivity

---

## 🗂 Problem Overview
Given `n` warehouses and an undirected list of roads, determine whether the graph is exactly one simple path covering every warehouse. That means the graph must be connected, must include all nodes, and cannot contain any branch or cycle. The non-trivial part is that connectivity alone is insufficient: the degree distribution must also match a path graph — two endpoints of degree `1`, all internal nodes of degree `2`, with the single-node case handled separately.

## 🌍 Engineering Impact
This pattern shows up anywhere infrastructure must validate a linear topology rather than arbitrary connectivity: supply-chain route verification, workflow engines enforcing single-stage handoff chains, CI/CD pipelines that must remain strictly sequential, and ETL or streaming jobs where each stage should have exactly one predecessor and one successor. At scale, treating “connected” as equivalent to “valid route” causes subtle failures: hidden forks create duplicate processing, cycles create non-terminating traversal, and disconnected nodes create silent data loss. The value of this check is not traversal itself, but enforcing a topology contract before downstream scheduling, optimization, or execution logic assumes linearity.

## 🔍 Problem Statement
You are given an undirected graph with `n` warehouses labeled `0` through `n - 1` and `roads[i] = [a, b]`, where each pair indicates a bidirectional road. Return `true` if the graph forms exactly one simple route visiting all warehouses once; otherwise return `false`.

A valid route must satisfy:

- all warehouses are in one connected component
- if `n > 1`, exactly two warehouses have degree `1`
- every other warehouse has degree `2`
- if `n == 1`, zero roads is valid

Constraints:

- `1 <= n <= 1000`
- `0 <= roads.length <= 1000`
- no self-loops, no duplicate roads

Examples:

- `n = 4, roads = [[0,1],[1,2],[2,3]]` → `true`
- `n = 4, roads = [[0,1],[1,2],[1,3]]` → `false`

The key algorithmic constraint is small enough for full graph traversal, so the right solution is structural validation plus connectivity checking, not brute-force path enumeration.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “is there a path,” it is “is the entire graph itself a path.”
2. A path graph has a very specific shape → for `n > 1`, two endpoints have degree `1`, everything else has degree `2`. Any node with degree `0`, `3`, or more immediately breaks the contract.
3. Degree checks alone are not enough → two separate chains could satisfy local degree rules while still being disconnected.
4. So the problem splits naturally into two validations:
   - local structure: degrees match a path
   - global structure: all nodes belong to one connected component
5. Build an adjacency list and degree counts in one pass over `roads`.
6. Handle the singleton edge case first: `n == 1` is valid only when there are no roads.
7. For larger graphs, count how many nodes have degree `1` and `2`. If the counts do not match the path pattern, return `false`.
8. Then run DFS or BFS from one endpoint. If traversal visits all `n` nodes, the graph is a single path; otherwise, it is fragmented.

This is the obvious approach once you recognize the target object is a path graph, not a generic connected graph.

## 🧩 Algorithm Walkthrough
1. **Build the graph representation**  
   Use an adjacency list for the undirected graph and increment degree counts for both endpoints of every road. This is the standard **Graph Traversal + Degree Validation** pattern. The invariant after this step: `degree[i]` equals the number of incident roads for warehouse `i`.

2. **Handle the singleton case explicitly**  
   If `n == 1`, return `true` only when `roads.length == 0`. This avoids forcing the general “two endpoints” rule onto the only valid one-node graph.

3. **Validate the degree signature of a path graph**  
   Scan all nodes and count how many have degree `1` and how many have degree `2`. For a valid path with `n > 1`, exactly two nodes must have degree `1`, and the remaining `n - 2` nodes must have degree `2`. If any node has another degree, return `false` immediately.  
   Why this is correct: a simple path has exactly two ends and no branching or isolated nodes.

4. **Choose a start node for traversal**  
   Start DFS/BFS from one of the degree-`1` nodes. The invariant: if the graph is truly one path, traversal from one endpoint must reach every node exactly through connected edges.

5. **Check connectivity**  
   Run DFS or BFS and count visited nodes. Return `true` only if the visited count is `n`.  
   Why this completes correctness: degree rules eliminate branches and cycles; connectivity eliminates disconnected chains. Together they characterize exactly one path spanning all nodes.

## 📊 Worked Example
Example: `n = 4`, `roads = [[0,1],[1,2],[2,3]]`

| Step | Action | Degree State | Visited |
|---|---|---|---|
| 1 | Add road `0-1` | `[1,1,0,0]` | — |
| 2 | Add road `1-2` | `[1,2,1,0]` | — |
| 3 | Add road `2-3` | `[1,2,2,1]` | — |
| 4 | Validate degrees | two nodes with `1` (`0,3`), two with `2` (`1,2`) | — |
| 5 | Start DFS from `0` | unchanged | `{0}` |
| 6 | Visit neighbor `1` | unchanged | `{0,1}` |
| 7 | Visit neighbor `2` | unchanged | `{0,1,2}` |
| 8 | Visit neighbor `3` | unchanged | `{0,1,2,3}` |

All four nodes are visited, and the degree pattern matches a path graph. Return `true`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `m = roads.length`. Building the adjacency list and degree array is linear in edges, and DFS/BFS is linear in nodes plus edges. At `10^6` elements this is still practical in memory-resident systems; at `10^9`, even linear scans become infrastructure decisions, not just algorithm choices.

### Space Complexity
`O(n + m)` for the adjacency list, degree array, and visited set. The adjacency list owns most of the space. You can reduce overhead slightly with iterative traversal and compact arrays, but not below graph-storage cost without changing the input model.

## 💡 Key Takeaways
- If a graph problem says “exactly one route through all nodes,” think path graph validation, not generic reachability.
- Degree constraints are often the fastest signal: path graphs have a rigid local signature that immediately rules out branches and cycles.
- Do not stop after checking degrees; two disconnected chains can still satisfy local degree rules.
- Handle `n == 1` separately, or you will incorrectly reject the valid empty graph.
- In production systems, topology validation should encode both local invariants and global connectivity before any scheduler or executor assumes a linear pipeline.

## 🚀 Variations & Further Practice
- Validate whether the graph forms a **cycle through all nodes** instead of a path; the twist is every node must have degree `2`, and connectivity must still hold.
- Given a general graph, determine whether it is a **tree**; the harder part is replacing degree-pattern checks with the acyclic + connected characterization.
- Count how many connected components are themselves valid paths; the twist is moving from whole-graph validation to per-component structural classification.