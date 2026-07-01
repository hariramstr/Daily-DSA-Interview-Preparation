# Nearest Exit Gate in an Office Floor

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Breadth-First Search, Shortest Path

---

## 🗂 Problem Overview
Given an undirected graph of `n` rooms, a list of hallways, a set of exit rooms, and a starting room, compute the minimum number of edges needed to reach any exit. Return `0` if the start is already an exit, and `-1` if no exit is reachable. The non-trivial part is scale: up to `10^5` nodes and `2 * 10^5` edges rules out repeated searches or path enumeration, so the solution must exploit the graph being unweighted.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the nearest reachable target in an unweighted network: failover routing to the closest healthy node, warehouse robotics finding the nearest charging dock, service meshes locating the nearest egress gateway, or game/server simulations computing shortest movement in uniform-cost maps. At scale, the wrong approach degrades into repeated graph scans, high tail latency, and unnecessary memory churn. Breadth-First Search gives deterministic shortest paths in hop-count space, enabling predictable latency, bounded work per edge, and straightforward reasoning about reachability in partially disconnected topologies.

## 🔍 Problem Statement
You are given:

- `n` rooms labeled `0` to `n - 1`
- `edges`, where each hallway is an undirected pair `[u, v]`
- `exits`, the rooms containing emergency exit gates
- `start`, the employee’s current room

Return the minimum number of hallways required to reach any exit room.

Edge cases matter:

- If `start` is in `exits`, return `0`
- If no exit is reachable from `start`, return `-1`
- The graph may be disconnected
- All edges have equal cost, so shortest path means minimum edge count

Constraints:

- `1 <= n <= 10^5`
- `0 <= edges.length <= 2 * 10^5`
- No duplicate hallways

Examples:

- `n=7, edges=[[0,1],[1,2],[2,3],[1,4],[4,5],[5,6]], exits=[3,6], start=0` → `3`
- `n=5, edges=[[0,1],[1,2],[3,4]], exits=[4], start=0` → `-1`

The key constraint is uniform edge weight: that makes BFS the correct shortest-path algorithm.

## 🪜 How to Solve This
1. Read the problem → we need the *minimum number of edges* in an *unweighted* undirected graph. That is the classic signal for BFS.

2. Notice there are multiple valid targets (`exits`) → we do **not** need the shortest path to every node, only the first exit reached in increasing distance order.

3. Since BFS explores nodes level by level, the first time we encounter any exit, we have found the minimum hallway count. No later path can be shorter.

4. To run BFS efficiently, first build an adjacency list from `edges`. An adjacency matrix would waste space at this input size.

5. Track visited rooms so each room is processed once. Without this, cycles would cause repeated work or infinite traversal.

6. Initialize the queue with `start` at distance `0`. If `start` is already an exit, return immediately.

7. Expand neighbors layer by layer until:
   - an exit is found → return its distance
   - the queue is exhausted → return `-1`

The reasoning is simple: unweighted shortest path + large sparse graph + earliest reachable target = BFS.

## 🧩 Algorithm Walkthrough
1. **Build the graph as an adjacency list.**  
   For each hallway `[u, v]`, append `v` to `u`’s neighbor list and `u` to `v`’s. This is correct because hallways are bidirectional. The invariant is that adjacency fully represents every reachable move in `O(n + m)` space.

2. **Convert `exits` into a hash set.**  
   Exit membership must be checked repeatedly during traversal. A set gives `O(1)` average lookup. The invariant is that exit detection does not depend on scanning the full exits list.

3. **Handle the trivial base case.**  
   If `start` is an exit, return `0`. This preserves correctness for the zero-length path case and avoids unnecessary graph traversal.

4. **Run Breadth-First Search (BFS) from `start`.**  
   Use a queue storing `(room, distance)` or process the queue level by level. BFS is the right abstraction because this is an **unweighted shortest-path** problem: every edge adds exactly one step.

5. **Mark rooms visited when enqueuing, not when dequeuing.**  
   This prevents duplicate insertions from multiple parents and guarantees each room enters the queue at most once. The invariant is that the first discovered distance for a room is its shortest possible distance.

6. **For each dequeued room, inspect its neighbors.**  
   If an unvisited neighbor is an exit, return `distance + 1`. This is correct because BFS explores all nodes at distance `d` before any node at distance `d + 1`.

7. **If BFS finishes without finding an exit, return `-1`.**  
   At that point, every reachable room has been explored, so no exit exists in the start room’s connected component.

## 📊 Worked Example
Example: `n=7`, `edges=[[0,1],[1,2],[2,3],[1,4],[4,5],[5,6]]`, `exits=[3,6]`, `start=0`

| Step | Queue              | Visited                | Action |
|------|--------------------|------------------------|--------|
| 0    | `[(0,0)]`          | `{0}`                  | Start at room 0 |
| 1    | `[(1,1)]`          | `{0,1}`                | From 0, enqueue 1 |
| 2    | `[(2,2),(4,2)]`    | `{0,1,2,4}`            | From 1, enqueue 2 and 4 |
| 3    | `[(4,2),(3,3)]`    | `{0,1,2,4,3}`          | From 2, enqueue 3 |
| 4    | —                  | —                      | Room 3 is an exit → return 3 |

Trace intuition:

1. BFS explores by distance layers: `0`, then `1`, then `2`, then `3`.
2. Exit `6` exists, but it is deeper in the graph.
3. The first exit discovered is `3` at distance `3`, so that is the optimal answer.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `m = edges.length`. Building the adjacency list touches each edge once, and BFS visits each node and edge at most once. This is the right asymptotic shape for sparse graphs: workable at `10^6` total graph elements, but anything superlinear becomes expensive quickly and is infeasible near `10^9`.

### Space Complexity
`O(n + m)`. The adjacency list dominates, with additional `O(n)` for the visited structure, queue, and exit set. You cannot materially reduce this without changing representation; the trade-off would usually be slower neighbor access or repeated scans.

## 💡 Key Takeaways
- If the problem asks for the minimum number of edges in an unweighted graph, BFS should be the default candidate.
- Multiple targets do not change the core pattern: BFS still works because the first target reached is the nearest.
- Mark nodes visited when you enqueue them, not after you pop them, or you risk duplicate work in cyclic graphs.
- Handle `start in exits` before traversal; otherwise you can return an incorrect positive distance instead of `0`.
- In production systems, shortest-hop search is often less about pathfinding and more about bounded, predictable work under partial connectivity.

## 🚀 Variations & Further Practice
- **Weighted hallways:** edges have non-uniform costs, so BFS no longer works; switch to Dijkstra’s algorithm.
- **Nearest exit for every room:** compute minimum distance to any exit for all nodes using **multi-source BFS** seeded with all exits at distance `0`.
- **Dynamic hallways or exits:** support updates over time, which introduces incremental graph maintenance and invalidates one-shot traversal assumptions.