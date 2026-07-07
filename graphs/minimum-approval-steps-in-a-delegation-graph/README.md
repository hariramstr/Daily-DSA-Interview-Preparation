# Minimum Approval Steps in a Delegation Graph

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Breadth-First Search, Shortest Path

---

## 🗂 Problem Overview
Given a directed graph of `n` employees, a set of valid starting employees, and a set of acceptable target employees, compute the minimum number of delegation edges needed to reach any target from any start. Return `-1` if no such path exists. The non-trivial part is that both the source and destination are sets, not single nodes, and the graph is large enough that per-start shortest-path searches are too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere reachability and minimum-hop routing matter across many admissible entry and exit points: workflow engines, IAM delegation chains, service dependency graphs, message-routing systems, and approval or escalation pipelines. At scale, the wrong approach becomes repeated graph traversal from each candidate source, which explodes latency and infrastructure cost. Multi-source BFS collapses many equivalent searches into one linear pass, giving predictable performance and a clean operational model. It enables fast policy evaluation, shortest escalation discovery, and bounded-time path checks in large sparse graphs.

## 🔍 Problem Statement
You are given a directed graph with `n` nodes labeled `0` to `n - 1` and edges `edges[i] = [u, v]`, meaning `u` can reach `v` in one delegation step. You are also given two distinct-node lists: `starts` and `targets`.

Return the minimum number of edges in any directed path that begins at a node in `starts` and ends at a node in `targets`. If no target is reachable from any start, return `-1`.

Constraints:

- `1 <= n <= 2 * 10^5`
- `0 <= edges.length <= 3 * 10^5`
- `1 <= starts.length <= n`
- `1 <= targets.length <= n`

Examples:

- `n = 7, edges = [[0,1],[1,3],[2,3],[3,4],[4,6],[2,5]], starts = [0,2], targets = [6,5]` → `1`
- `n = 6, edges = [[0,1],[1,2],[2,3],[4,5]], starts = [0,4], targets = [3]` → `3`

The key constraint is graph size: anything worse than linear or near-linear traversal will not scale.

## 🪜 How to Solve This
1. Read the problem → this is shortest path in an unweighted directed graph, because every delegation edge costs exactly `1`.

2. Notice the twist → there are multiple valid starts and multiple valid ends. Running BFS from each start would repeat work and can degrade toward `O(|starts| * (n + m))`.

3. Ask what BFS actually needs → an initial frontier. BFS does not require a single source; it only requires all distance-`0` nodes to be seeded first.

4. So initialize the queue with every node in `starts` at distance `0`. That models a virtual super-source connected to all starts with zero-cost edges.

5. Convert `targets` to a hash set for `O(1)` membership checks. During BFS, the first target dequeued is guaranteed to have the minimum number of steps from any start.

6. Why does that work? BFS explores nodes in nondecreasing distance order in unweighted graphs. Once a target appears, no undiscovered path can be shorter.

7. Edge cases fall out naturally: if a start is already a target, answer is `0`; if traversal finishes without hitting a target, answer is `-1`.

## 🧩 Algorithm Walkthrough
1. **Build the adjacency list**  
   Represent the directed graph as `adj[u] -> list of v`. This gives `O(1)` amortized edge iteration and keeps traversal linear in `n + m`. The pattern here is **Breadth-First Search on an unweighted graph**.

2. **Materialize targets as a set**  
   Store all target nodes in a hash set. This makes “is this node an acceptable destination?” constant time, which matters because the check happens on every dequeue or discovery.

3. **Initialize multi-source BFS**  
   Push every node in `starts` into the queue with distance `0`, and mark each as visited immediately. The invariant is: every queued node has already been assigned its shortest known distance, and no node is enqueued twice.

4. **Handle zero-step success**  
   Before or during initialization, if any start belongs to `targets`, return `0`. This avoids unnecessary traversal and correctly handles overlap between the two sets.

5. **Process the queue level by level**  
   Pop a node `u`, then inspect all outgoing neighbors `v`. If `v` is unvisited, mark it visited, assign `dist[v] = dist[u] + 1`, and enqueue it. The invariant remains: nodes leave the queue in nondecreasing distance order.

6. **Stop at the first target reached**  
   As soon as a dequeued or newly discovered node is in `targets`, return its distance. This is correct because BFS guarantees the first time a node is reached is via the shortest path in an unweighted graph.

7. **Return `-1` if exhausted**  
   If the queue empties without reaching any target, then no valid start can reach any valid target.

## 📊 Worked Example
Use Example 1:

`n = 7`  
`edges = [[0,1],[1,3],[2,3],[3,4],[4,6],[2,5]]`  
`starts = [0,2]`, `targets = [6,5]`

| Step | Queue     | Visited        | Action |
|------|-----------|----------------|--------|
| Init | `[0,2]`   | `{0,2}`        | Seed all starts at distance `0` |
| 1    | `[2,1]`   | `{0,1,2}`      | Pop `0`, discover `1` at distance `1` |
| 2    | `[1,3,5]` | `{0,1,2,3,5}`  | Pop `2`, discover `3` and `5` at distance `1` |
| 3    | —         | —              | `5` is a target, so answer is `1` |

Trace intuition:

1. Multi-source BFS starts from both `0` and `2` simultaneously.
2. That means the search does not prefer one start over another.
3. Node `5` is reached directly from `2` in one edge.
4. Even though `6` is also reachable, it requires more steps.
5. Because BFS explores by increasing distance, the first target found gives the global minimum.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `m = edges.length`. Building the adjacency list touches every edge once, and BFS visits each node and edge at most once. At `10^6` scale this is still practical in memory-aware implementations; at `10^9`, even linear scans become infrastructure problems rather than algorithm problems.

### Space Complexity
`O(n + m)`. The adjacency list dominates edge storage, while the queue, visited structure, and target set add `O(n)`. You can reduce some overhead with bitsets or packed arrays, but only by trading readability and implementation simplicity for tighter memory control.

## 💡 Key Takeaways
- If the graph is unweighted and the question asks for the fewest edges, BFS should be your default shortest-path candidate.
- If there are many valid sources, think multi-source BFS instead of repeating single-source BFS from each start.
- Do not forget the `starts ∩ targets` case; the correct answer there is `0`, not `1`.
- Mark nodes visited when enqueuing, not when dequeuing, or duplicates can inflate work and complicate distance guarantees.
- In production systems, collapsing many equivalent traversals into one frontier-based pass is a recurring scale pattern: same correctness, far better operational cost.

## 🚀 Variations & Further Practice
- Reverse the graph and run multi-source BFS from all `targets`; this is useful when you need the minimum distance from every node to any acceptable sink, not just one query.
- Add edge weights to represent variable approval latency; BFS no longer applies, and the conceptual shift is to Dijkstra or 0-1 BFS depending on weight structure.
- Ask for the number of distinct shortest approval chains, not just the minimum length; now you must track both distance and path counts without double-counting longer routes.