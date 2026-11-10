# Find the Nearest Open Seat in a Venue Tree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Breadth-First Search, Hash Map

---

## 🗂 Problem Overview
Given a binary tree of unique section ids, a target section id, and a set of closed sections, return the open section with minimum edge distance from the target. Movement is allowed to left child, right child, and parent, so the tree must be searched like an undirected graph. If multiple open sections are equally near, choose the smallest id. Return `-1` if the target does not exist or all sections are closed.

## 🌍 Engineering Impact
This pattern shows up anywhere a hierarchy must be queried by nearest reachable healthy node rather than by ancestry alone: service dependency graphs, failover routing in cluster topologies, nearest available warehouse/bin lookup, filesystem or DOM traversal with disabled subtrees, and org/resource trees with maintenance windows. What breaks without this approach is correctness under bidirectional navigation: a pure downward traversal misses valid nearest candidates through ancestors and sibling branches. Recording parent links and then running BFS gives deterministic shortest-path behavior, bounded work, and a clean tie-breaking surface that scales to large mutable topologies.

## 🔍 Problem Statement
A venue seating layout is represented as a binary tree with `n` nodes, where `1 <= n <= 100000`. Each node stores a unique integer section id in `[1, 10^9]`. Given `root`, an integer `target`, and a set `closedSections`, return the id of the nearest open section measured by number of tree edges from the target section.

Traversal may move downward to children or upward to parents. If the target section is open, it is a valid answer with distance `0`. If several open sections share the same minimum distance, return the smallest id. If `target` is not present in the tree, return `-1`. If every section is closed, return `-1`.

Examples:

- `root = [8,4,12,2,6,10,14], target = 4, closedSections = [4,2]` → `6`
- `root = [5,3,9,1,null,7,11], target = 3, closedSections = [1,3,5]` → `7`

The key constraint is `O(n)` time and `O(n)` extra space, which rules out repeated path searches from the target to every candidate.

## 🪜 How to Solve This
1. Read the problem → shortest distance in a tree, but movement is allowed both down and up. That means this is not a normal root-to-leaf tree traversal anymore; it is an unweighted graph shortest-path problem.

2. Trees do not store parent pointers by default → to move upward, first traverse the tree once and record each node’s parent in a hash map. During the same pass, locate the node whose id equals `target`.

3. If the target node does not exist, stop immediately with `-1`.

4. From the target, run Breadth-First Search. BFS is the right tool because every edge has equal cost, so the first layer containing an open node gives the minimum distance.

5. At each BFS level, inspect all nodes in that layer. Collect open candidates from that layer only. If any exist, return the smallest id among them. That handles the tie-break correctly without exploring deeper levels.

6. Use a visited set because once parent links are added, the tree becomes an undirected graph and can cycle between child and parent.

## 🧩 Algorithm Walkthrough
1. **Build parent references and find the target node**  
   Perform a traversal from `root`. For each child, store `parent[child] = current`. Also compare each node’s id with `target`.  
   **Why correct:** this creates the missing reverse edges needed for upward movement.  
   **Invariant:** after traversal, every reachable node has a correct parent mapping except the root, and target existence is known.

2. **Handle missing target early**  
   If no node with id `target` was found, return `-1`.  
   **Why correct:** BFS cannot start from a nonexistent node, and the contract explicitly requires `-1`.

3. **Initialize graph-style BFS from the target**  
   Use a queue seeded with the target node and a visited set containing it.  
   **Pattern:** **Breadth-First Search on an implicit undirected graph**.  
   **Why this abstraction fits:** tree edges become bidirectional once parent traversal is allowed, and BFS guarantees shortest path in unweighted graphs.

4. **Process one distance layer at a time**  
   For all nodes currently in the queue, check whether each node’s id is not in `closedSections`. Track the minimum id among open nodes in this layer.  
   **Why correct:** all nodes in the same BFS layer are at identical distance from target.  
   **Invariant:** before expanding to the next layer, every node in the current layer has the minimum unprocessed distance.

5. **Return on first layer with an open candidate**  
   If at least one open node exists in the current layer, return the smallest id found.  
   **Why correct:** any deeper node would be farther away, so the current layer contains the globally nearest valid answers.

6. **Expand neighbors safely**  
   Add `left`, `right`, and `parent[node]` if present and not visited.  
   **Why correct:** this enumerates exactly the legal moves.  
   **Invariant:** each node enters the queue at most once, preserving `O(n)` time.

7. **Exhaustion case**  
   If BFS finishes without finding an open node, return `-1`.  
   **Why correct:** every reachable section was closed.

## 📊 Worked Example
Example: `root = [5,3,9,1,null,7,11]`, `target = 3`, `closedSections = {1,3,5}`

| BFS Distance | Queue at Start | Open Nodes in Layer | Action |
|---|---|---:|---|
| 0 | `[3]` | none (`3` closed) | expand to `1, 5` |
| 1 | `[1, 5]` | none (`1`, `5` closed) | expand to `9, 7` |
| 2 | `[9, 7]` | `9, 7` | return smaller id = `7` |

Trace:
1. Build parent map: `parent[3]=5`, `parent[9]=5`, `parent[1]=3`, `parent[7]=9`, `parent[11]=9`.
2. Start BFS from node `3`.
3. Distance `0`: node `3` is closed.
4. Distance `1`: nodes `1` and `5` are both closed.
5. Distance `2`: nodes `9` and `7` are both open. Same minimum distance, so tie-break by smaller id.
6. Final answer: `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One traversal builds the parent map and finds the target; one BFS visits each node at most once. Hash-set membership for `closedSections` and `visited` is `O(1)` average-case. At `10^6` nodes this is still linear and practical; at `10^9`, memory and traversal time become the real bottlenecks, not asymptotics.

### Space Complexity
`O(n)`. The parent map, visited set, and BFS queue together dominate memory usage. It can only be reduced by changing the representation or accepting repeated traversals, which would violate the required linear-time bound.

## 💡 Key Takeaways
- If a tree problem allows movement to both children and parent, stop thinking “tree traversal” and start thinking “implicit undirected graph.”
- If the question asks for minimum number of edges in an unweighted structure, BFS is the default shortest-path primitive.
- The tie-break is per BFS layer, not globally across all discovered nodes; return the smallest open id in the first valid layer only.
- Once parent pointers are introduced, failing to track `visited` creates child-parent-child cycles and invalid repeated work.
- In production systems, augmenting a hierarchical model with reverse edges often turns a structurally simple tree into the graph you actually need to query correctly.

## 🚀 Variations & Further Practice
- Return the actual path to the nearest open section, not just its id. The twist is preserving predecessor information while still handling tie-breaks deterministically.
- Support many target queries against the same mostly static tree. The twist is preprocessing or indexing to avoid rebuilding parent state and rerunning full BFS per query.
- Generalize from a binary tree to an arbitrary graph with blocked nodes and weighted edges. The twist is replacing BFS with Dijkstra and handling cycles as a first-class concern.