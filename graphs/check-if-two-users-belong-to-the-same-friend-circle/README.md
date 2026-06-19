# Check if Two Users Belong to the Same Friend Circle

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graph, BFS, DFS

---

## 🗂 Problem Overview
Given `n` users and an undirected list of friendship pairs, determine whether two users `source` and `target` are in the same connected component. The output is a single boolean: `true` if some path exists between them, otherwise `false`. The problem is simple conceptually, but non-trivial because direct friendships are incomplete information; reachability must be inferred transitively by traversing the graph rather than checking only immediate neighbors.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to answer connectivity queries over sparse relationships: social graph features, fraud-ring detection, service dependency analysis, network reachability, compiler dependency graphs, and identity/account-linking systems. At production scale, the difference between scanning raw edges and traversing an indexed graph is the difference between interactive latency and unusable endpoints. Modeling relationships as connected components enables fast reasoning about isolation, blast radius, trust boundaries, and propagation paths. Even when the immediate requirement is a single boolean query, the underlying abstraction is foundational for graph-backed product and infrastructure systems.

## 🔍 Problem Statement
You are given an undirected graph of `n` users labeled `0` through `n - 1`, and a list `friendships` where each pair `[u, v]` means users `u` and `v` know each other directly. Because friendship is mutual, each edge can be traversed in both directions.

Return `true` if `source` can reach `target` through zero or more friendship links; otherwise return `false`.

Constraints:

- `1 <= n <= 10^4`
- `0 <= friendships.length <= 2 * 10^4`
- `friendships[i].length == 2`
- `0 <= u, v < n`
- `u != v`
- No duplicate friendship pairs
- `0 <= source, target < n`

Examples:

- `n = 5, friendships = [[0,1],[1,2],[3,4]], source = 0, target = 2` → `true`
- `n = 6, friendships = [[0,1],[2,3],[3,4]], source = 1, target = 5` → `false`

The key constraint is that reachability must be determined over the full connected component, which makes graph traversal the natural choice.

## 🪜 How to Solve This
1. Read the problem → this is not about counting edges or checking direct friendship; it asks whether two nodes are connected through any chain. That is a reachability question.

2. Reachability in an undirected graph → think traversal. The standard tools are BFS or DFS. Either works because we only need existence of a path, not the shortest weighted path.

3. Raw edge pairs are inconvenient for traversal → build an adjacency list so each user can efficiently enumerate neighbors.

4. Start from `source` → explore outward, marking visited users so cycles do not cause repeated work or infinite loops.

5. During traversal, if you ever encounter `target`, return `true` immediately. That early exit matters because the problem asks for one query, not a full graph analysis.

6. If traversal finishes without seeing `target`, then `source` and `target` are in different connected components, so return `false`.

7. Notice the shortcut: if `source == target`, the answer is trivially `true` even before building or traversing anything.

This is the basic connected-component membership pattern dressed as a social graph problem.

## 🧩 Algorithm Walkthrough
1. **Model the graph as an adjacency list**  
   Create a list of neighbors for each user. For every friendship `[u, v]`, append `v` to `u`’s list and `u` to `v`’s list.  
   **Why correct:** the graph is undirected, so both directions must exist.  
   **Invariant:** after processing all edges, every direct friendship is represented exactly twice.

2. **Handle the trivial case early**  
   If `source == target`, return `true`.  
   **Why correct:** a node is always reachable from itself by a path of length zero.  
   **Invariant:** no traversal is needed for self-connectivity.

3. **Initialize traversal state**  
   Use either **BFS** with a queue or **DFS** with a stack/recursion. Mark `source` as visited before pushing/enqueuing it.  
   **Why this pattern:** this is a standard **graph traversal / connected-component search** problem. We need exhaustive exploration of one component, not ordering or optimization.

4. **Explore neighbors iteratively**  
   Repeatedly remove one node from the worklist, inspect its neighbors, and visit any unvisited neighbor.  
   **Why correct:** traversal expands exactly along reachable edges from `source`.  
   **Invariant:** every visited node is reachable from `source`; every node in the worklist has been discovered but not fully processed.

5. **Check for the target during discovery**  
   If a neighbor equals `target`, return `true` immediately.  
   **Why correct:** discovering `target` proves a valid path exists.  
   **Invariant:** early termination does not miss a better answer because the output is boolean, not path length.

6. **Finish traversal**  
   If the queue/stack becomes empty, all nodes reachable from `source` have been explored. If `target` was never found, return `false`.  
   **Why correct:** no unexplored reachable nodes remain.  
   **Invariant:** visited now equals the full connected component containing `source`.

## 📊 Worked Example
Example: `n = 5`, `friendships = [[0,1],[1,2],[3,4]]`, `source = 0`, `target = 2`

Adjacency list:
- `0: [1]`
- `1: [0,2]`
- `2: [1]`
- `3: [4]`
- `4: [3]`

Using BFS:

| Step | Queue   | Visited     | Current | Action |
|------|---------|-------------|---------|--------|
| 1    | `[0]`   | `{0}`       | —       | Initialize from `source` |
| 2    | `[]`    | `{0}`       | `0`     | Pop `0`, inspect neighbor `1` |
| 3    | `[1]`   | `{0,1}`     | —       | Enqueue `1` |
| 4    | `[]`    | `{0,1}`     | `1`     | Pop `1`, inspect neighbors `0`, `2` |
| 5    | —       | `{0,1,2}`   | —       | Discover `2` = `target`, return `true` |

The traversal never touches users `3` or `4` because they are in a different connected component.

## ⏱ Complexity Analysis
### Time Complexity
Building the adjacency list takes `O(E)`, where `E = friendships.length`. Traversal visits each reachable node and edge at most once, so total time is `O(V + E)`. At `10^6` elements this is still linear and practical; at `10^9`, even linear scans become infrastructure-level work and require partitioning or precomputation.

### Space Complexity
The adjacency list dominates space at `O(V + E)`, plus `O(V)` for the visited set and BFS/DFS worklist in the worst case. You can avoid storing the full graph only by repeatedly scanning edges during traversal, which reduces memory but degrades time significantly.

## 💡 Key Takeaways
- If the prompt asks whether two entities are connected “through one or more links,” it is almost always a graph reachability problem.
- If relationships are mutual and transitive membership matters, think connected components and BFS/DFS before considering anything more complex.
- In an undirected graph, forgetting to add both directions to the adjacency list silently breaks connectivity.
- Mark nodes as visited when enqueuing/pushing them, not after popping, to avoid duplicate work and inflated queue/stack growth.
- The production lesson is broader than this toy problem: once relationships become first-class data, explicit graph modeling turns expensive repeated scans into predictable indexed traversal.

## 🚀 Variations & Further Practice
- **Multiple connectivity queries:** answer many `source`/`target` checks efficiently using Union-Find; the twist is shifting work from per-query traversal to upfront component construction.
- **Shortest friendship chain:** return the minimum number of hops instead of a boolean; the twist is that BFS becomes semantically necessary, not just convenient.
- **Dynamic friendships:** support edge additions/removals over time; the twist is maintaining connectivity incrementally, which is much harder than one-shot traversal.