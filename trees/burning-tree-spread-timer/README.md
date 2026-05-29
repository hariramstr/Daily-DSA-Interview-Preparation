# Burning Tree Spread Timer

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Tree, BFS, DFS, Graph Traversal

---

## 🗂 Problem Overview

Given a binary tree and a start node, simulate fire spreading simultaneously to all adjacent nodes (parent, left child, right child) each second. Return the total seconds until every node is burning. The non-trivial constraint: fire travels *upward* through parent links, which a standard binary tree doesn't store. This forces a graph conversion or a two-pass strategy — you can't solve it with a naive top-down traversal.

---

## 🌍 Engineering Impact

This pattern — converting a hierarchical structure into a bidirectional graph for multi-source propagation — appears directly in epidemic/failure modeling across distributed systems. Think cascading failure analysis in service meshes (how quickly does a single unhealthy pod poison dependent services?), blast-radius estimation in infrastructure dependency graphs, and network topology flood-fill in BGP route propagation. Without the parent-pointer augmentation, you undercount propagation paths and produce incorrect time-to-full-spread estimates, which breaks SLO modeling and capacity planning at scale.

---

## 🔍 Problem Statement

**Input:** Root of a binary tree (1–10⁴ nodes, all values unique, 1 ≤ val ≤ 10⁴) and an integer `start` identifying the ignition node.

**Output:** Minimum integer seconds for all nodes to be burning.

**Spread rule:** Each second, fire expands simultaneously from every burning node to all unvisited neighbors — left child, right child, and parent.

**Examples:**

```
Input: root = [1,2,3,4,5,null,6], start = 2
Output: 3
# t=0: {2} | t=1: {1,4,5} | t=2: {3} | t=3: {6}

Input: root = [1,2,3], start = 1
Output: 1
# t=0: {1} | t=1: {2,3}
```

**Key constraint driving the algorithm:** Parent links don't exist natively. The fire must propagate upward, so the tree must be treated as an undirected graph before BFS can correctly model simultaneous multi-directional spread.

---

## 🪜 How to Solve This

1. **Read the spread rule → fire moves in all directions, including upward.** A binary tree only gives you downward edges. Upward traversal is impossible without extra bookkeeping.

2. **Upward traversal needed → augment the structure.** Two options: store a `parent` map via DFS, or convert the entire tree to an adjacency list. The adjacency list is cleaner and decouples the graph traversal from tree-specific logic.

3. **Simultaneous spread from multiple sources → BFS, not DFS.** BFS naturally models wave-front propagation layer by layer. DFS would require backtracking and merging depths — far more complex. The BFS level counter *is* the elapsed time.

4. **Find the start node during the DFS → kill two birds.** While building the adjacency list, record the node reference matching `start`. This avoids a second traversal.

5. **Run BFS from the start node → count levels.** Each BFS level = one second. The answer is the total number of levels minus one (since the initial enqueue of `start` is t=0, not t=1).

The chain: tree has no parent links → convert to undirected graph → multi-source simultaneous spread → BFS level-order traversal → level count = answer.

---

## 🧩 Algorithm Walkthrough

**Pattern: Graph Conversion + BFS Level-Order Traversal**

The tree's implicit directionality is the core obstacle. BFS on the raw tree structure can't propagate upward, so the first step is flattening the tree into an undirected adjacency list — this is the key abstraction that makes the problem tractable.

**Step 1 — DFS to build adjacency list and locate start node.**
Traverse the tree once. For each parent-child edge, add both directions to a `HashMap<Integer, List<Integer>>`. Record the node whose value equals `start`. This runs in O(n) and establishes the invariant that every node has all its true neighbors registered.

**Step 2 — BFS from start node.**
Initialize a queue with the start node's value and a `visited` set. Process level by level. For each level, dequeue all current nodes, enqueue their unvisited neighbors, and increment a timer counter. The timer increments *after* enqueueing, so t=0 correctly represents the start node burning alone.

**Step 3 — Termination.**
BFS ends when the queue is empty — all reachable nodes (the entire tree, since it's connected) have been visited. The timer value at termination is the answer.

**Why BFS is correct here:** BFS guarantees that all nodes at distance `d` from the source are processed before any node at distance `d+1`. This exactly mirrors simultaneous fire spread — no node at a greater distance can burn before all closer nodes have burned.

---

## 📊 Worked Example

**Input:** `root = [1,2,3,4,5,null,6], start = 2`

Adjacency list after DFS:
`{1:[2,3], 2:[1,4,5], 3:[1,6], 4:[2], 5:[2], 6:[3]}`

| Second (t) | Queue (dequeued) | Newly Enqueued | Visited Set            |
|------------|------------------|----------------|------------------------|
| 0          | —                | [2]            | {2}                    |
| 1          | [2]              | [1, 4, 5]      | {2, 1, 4, 5}           |
| 2          | [1, 4, 5]        | [3]            | {2, 1, 4, 5, 3}        |
| 3          | [3]              | [6]            | {2, 1, 4, 5, 3, 6}     |
| done       | [6]              | []             | all nodes visited      |

Queue empties after processing level 3. **Answer: 3.**

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — DFS visits each node once to build the adjacency list; BFS visits each node once during propagation. Both passes are linear in the number of nodes. At 10⁴ nodes (the constraint ceiling) this is trivially fast. At 10⁶ nodes it remains practical; at 10⁹ it would require distributed BFS since the adjacency list itself wouldn't fit in memory.

### Space Complexity

**O(n)** — dominated by the adjacency list storing 2n directed edges (each undirected edge stored twice) and the BFS queue which holds at most O(n) nodes in the worst case (a star graph). The visited set is also O(n). No meaningful reduction is possible without sacrificing either correctness or the ability to traverse upward.

---

## 💡 Key Takeaways

- **Pattern signal — bidirectional traversal on a unidirectional structure:** Whenever a problem requires moving against the natural direction of a tree or DAG (upward, reverse topological), the first instinct should be graph conversion or parent-pointer augmentation before any traversal logic.
- **Pattern signal — simultaneous multi-source propagation with a time dimension:** "Spreads at the same time" is the canonical BFS trigger. If the problem asks for *when* something reaches all nodes, BFS level count is the answer; DFS will require non-trivial post-processing.
- **Implementation gotcha — timer off-by-one:** The BFS loop structure matters. Incrementing the timer *per level* (not per node dequeue) and initializing at 0 for the start node is critical. A common mistake is returning `levels - 1` or `levels + 1` due to confusion about when the counter increments relative to the initial enqueue.
- **Implementation gotcha — visited set must be checked before enqueue, not after dequeue:** Checking on dequeue allows duplicate entries in the queue, which corrupts the level count and can cause incorrect timing results on dense graphs.
- **Architectural insight:** The graph-conversion step is a deliberate separation of concerns — decouple *structure normalization* (making the graph traversable) from *algorithm execution* (BFS). This maps directly to production patterns like building an internal dependency graph from a service registry before running reachability or blast-radius analysis.

---

## 🚀 Variations & Further Practice

- **Variable spread rates per node:** Assign each node a burn delay (e.g., node `v` takes `w(v)` seconds to ignite after a neighbor catches fire). This converts the problem to a weighted shortest-path problem (Dijkstra's from the start node on the undirected graph), breaking the uniform BFS level assumption entirely.
- **Multiple simultaneous ignition points:** Fire starts at a *set* of nodes at t=0. The conceptual twist is multi-source BFS — initialize the queue with all start nodes simultaneously. The harder extension asks: given a budget of `k` fire-starts, which placement minimizes total burn time? This becomes a combinatorial optimization problem (NP-hard in general graphs, tractable on trees via centroid decomposition).
- **LeetCode 863 — All Nodes Distance K in Binary Tree:** The same graph-conversion prerequisite, but instead of timing, you return all nodes at exactly distance `k` from a target. Reinforces the parent-augmentation pattern without the BFS timing layer.