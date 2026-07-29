# Minimum Channel Switches to Broadcast a Live Event

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, 0-1 BFS, Shortest Path

---

## 🗂 Problem Overview
Given a directed graph where each edge has a channel ID, compute the minimum number of channel switches needed to send a stream from node `0` to node `n - 1`. The first edge does not count as a switch. Return `-1` if the destination is unreachable. The non-trivial part is that path cost depends not just on the current node, but also on the channel used to arrive there, so node-only shortest path logic is insufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere transition cost depends on prior state, not just current location: network routing with protocol changes, streaming pipelines with codec or transport shifts, compiler backends switching register classes, and warehouse/job schedulers paying setup costs between task families. At scale, collapsing state too aggressively produces locally optimal but globally wrong plans. The fix is architectural: model the true state space, then exploit structure in edge weights. Here, switch cost is binary—stay on the same channel or pay one—so 0-1 BFS gives shortest-path correctness with near-linear performance on graphs large enough to matter operationally.

## 🔍 Problem Statement
You are given `n` stations and a directed edge list `edges`, where each edge is `[u, v, c]`: travel from `u` to `v` using channel `c`. A valid route may revisit nodes. The cost of a path is the number of times consecutive edges use different channel IDs. The first selected edge contributes `0`, since no channel is active before transmission starts.

Return the minimum switch count needed to reach node `n - 1` from node `0`, or `-1` if unreachable.

Constraints:
- `2 <= n <= 100000`
- `1 <= edges.length <= 200000`
- `0 <= u, v < n`, `u != v`
- `1 <= c <= 10^9`
- Multiple directed edges between the same nodes may exist.

Examples:
- `n = 5`, `edges = [[0,1,7],[1,2,7],[2,4,7],[0,3,2],[3,4,2],[1,3,5]]` → `0`
- `n = 6`, `edges = [[0,1,1],[1,2,2],[2,5,2],[0,3,3],[3,4,4],[4,5,5],[1,4,1]]` → `1`

The key constraint is that optimality depends on `(node, previous_channel)`, which drives the algorithmic choice.

## 🪜 How to Solve This
1. Read the cost definition → notice the penalty is paid only when the **next edge’s channel differs from the previous edge’s channel**.
2. That immediately breaks ordinary BFS or Dijkstra over just `node`, because reaching the same node on different channels creates different future costs.
3. So redefine the state as **(current node, current channel)**. Now the future cost from that state is well-defined.
4. How do transitions behave? From `(u, ch)` to edge `(u, v, c)`:
   - if `c == ch`, added cost is `0`
   - otherwise, added cost is `1`
5. That is a shortest-path problem with edge weights only in `{0, 1}` → think **0-1 BFS**, not heap-based Dijkstra.
6. Seed the search carefully: the first edge should cost `0`, so initialize from node `0` by pushing every outgoing edge state `(v, c)` with distance `0`.
7. Run 0-1 BFS over these expanded states, keeping the best known switch count for each `(node, channel)`.
8. The answer is the minimum distance among all states whose node is `n - 1`.

Once you see “cost depends on previous label” plus “increment is only 0 or 1,” the solution is almost forced.

## 🧩 Algorithm Walkthrough
1. **Build the adjacency list.**  
   Store outgoing edges for each node as `(nextNode, channel)`. This gives `O(m)` traversal over the original graph and is the base representation for all later state transitions.

2. **Lift the graph into a state graph.**  
   The real shortest-path state is `(node, lastChannel)`, not just `node`. This is the critical modeling step: two arrivals at the same node are not equivalent if they used different channels, because the next edge may cost `0` for one and `1` for the other.

3. **Initialize from node `0` with zero cost.**  
   For every outgoing edge `(0, v, c)`, set distance of state `(v, c)` to `0` and push it into the deque. This encodes the rule that the first chosen edge does not count as a switch. No synthetic “null channel” state is required.

4. **Run 0-1 BFS.**  
   Pop a state `(u, ch)` from the front. For each outgoing edge `(u, v, c)`:
   - transition cost is `0` if `c == ch`
   - otherwise `1`  
   If the new distance improves `dist[(v, c)]`, update it. Push to the **front** for cost `0`, to the **back** for cost `1`.

5. **Maintain the shortest-path invariant.**  
   In 0-1 BFS, deque order guarantees states are processed in nondecreasing distance without a heap. That is why this pattern is the right abstraction: shortest path with binary edge weights.

6. **Extract the answer.**  
   Among all stored states with node `n - 1`, take the minimum distance. If none exist, return `-1`. This is correct because each such state represents a valid completed path with its exact switch count.

## 📊 Worked Example
Use Example 2:

`n = 6`  
`edges = [[0,1,1],[1,2,2],[2,5,2],[0,3,3],[3,4,4],[4,5,5],[1,4,1]]`

| Step | Popped state | Relaxed transition | Added cost | New dist |
|---|---|---|---:|---:|
| Init | — | `(0→1,1)`, `(0→3,3)` | 0 | `dist[(1,1)]=0`, `dist[(3,3)]=0` |
| 1 | `(1,1)` | `(1→2,2)` | 1 | `dist[(2,2)]=1` |
| 1 | `(1,1)` | `(1→4,1)` | 0 | `dist[(4,1)]=0` |
| 2 | `(4,1)` | `(4→5,5)` | 1 | `dist[(5,5)]=1` |
| 3 | `(3,3)` | `(3→4,4)` | 1 | `dist[(4,4)]=1` |
| 4 | `(2,2)` | `(2→5,2)` | 0 | `dist[(5,2)]=1` |

Destination node `5` is reachable with distance `1` via states `(5,5)` and `(5,2)`. Minimum answer: **1**.

## ⏱ Complexity Analysis
### Time Complexity
`O(m + S)`, where `m` is the number of original edges and `S` is the number of reachable `(node, channel)` states and their transitions. In practice this is linear in the expanded graph because each successful relaxation is handled once with deque operations. At `10^6` scale this is operationally reasonable; at `10^9`, graph materialization itself becomes the bottleneck.

### Space Complexity
`O(m + S)` for the adjacency list plus the distance map over `(node, channel)` states. The dominant cost is storing expanded-state distances. You can reduce constants with compact hashing or channel compression, but not the asymptotic bound without sacrificing correctness.

## 💡 Key Takeaways
- If path cost depends on the **previous edge label**, the state is almost certainly `(node, last_label)`, not just `node`.
- If every transition cost is only `0` or `1`, that is a strong signal for **0-1 BFS** instead of heap-based Dijkstra.
- The first edge is a special case: it must start with cost `0`, so careless initialization can add a bogus switch.
- Returning the first time you see node `n - 1` is wrong unless you are processing full `(node, channel)` states in shortest-distance order.
- In production systems, correctness often depends on modeling hidden state explicitly before optimizing the traversal algorithm.

## 🚀 Variations & Further Practice
- Add a per-channel switch penalty matrix instead of uniform cost `1`; this becomes general weighted shortest path and typically requires Dijkstra on the expanded state graph.
- Charge both edge traversal latency and channel-switch cost; now you optimize a mixed metric and must reason about whether 0-1 BFS still applies.
- Allow switching channels only at designated nodes; the conceptual twist is that feasibility now depends on node capabilities as well as prior channel state.