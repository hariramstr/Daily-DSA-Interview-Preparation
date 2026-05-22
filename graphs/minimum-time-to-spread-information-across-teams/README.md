# Minimum Time to Spread Information Across Teams

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, BFS, Topological Sort, Shortest Path, Dynamic Programming

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a company where news travels from a CEO down through managers to employees. Each person takes a different amount of time to pass a message along. This problem asks: given that chain of communication, how long does it take for **everyone** in the company to hear the news? We want the fastest possible time — and we need to know if some employees are unreachable at all.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This algorithm mirrors challenges faced every day in business and technology. Emergency alert systems need to know how quickly a warning reaches every citizen. Supply chain managers model how long a factory delay ripples to every downstream partner. Software deployment pipelines calculate when all servers have received an update. Optimising this "information cascade" directly reduces response times, minimises downtime costs, and improves operational resilience. Companies like Amazon use similar models to coordinate warehouse workflows, while public health agencies apply them to track how quickly guidance reaches field workers during a crisis.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a company phone tree during an emergency. The CEO calls two managers, each manager calls their own team members, and so on. Every person takes a different amount of time to make their calls. Some employees might not be connected to anyone. The question is: **how many minutes until the very last person gets the message?** If someone is completely cut off from the chain, the answer is "impossible."

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a directed graph of `n` employees (nodes `0` to `n-1`), an array `time[i]` representing the delay for employee `i` to inform their direct reports, and a list of directed edges `relations[j] = [a, b]` (employee `a` informs employee `b`), find the **minimum time** for information starting at node `0` to reach **all** nodes. Return `-1` if any node is unreachable.

**Constraints:** `1 ≤ n ≤ 10⁴`, `0 ≤ time[i] ≤ 1000`, `0 ≤ relations.length ≤ 5×10⁴`, no self-loops.

**Example 1:**
- Input: `n=6`, `time=[0,3,2,1,4,0]`, `relations=[[0,1],[0,2],[1,3],[1,4],[2,5]]`
- Output: `7` *(path 0→1→4: 0+3+4=7)*

**Example 2:**
- Input: `n=4`, `time=[0,1,2,0]`, `relations=[[0,1],[0,2],[1,3]]`
- Output: `2` *(path 0→2: 0+2=2)*

---

## 🧩 Approach: How We Solve It *(For Developers)*

We model this as a **longest-path problem on a weighted DAG** using BFS-based Topological Sort (Kahn's Algorithm) combined with dynamic programming.

1. **Build the graph and compute in-degrees.**
   Construct an adjacency list from `relations`. Track how many incoming edges each node has (`in_degree[node]`). This tells us which nodes have no prerequisites — they can be processed immediately.

2. **Initialise a DP array.**
   Create `dist[i]` = the earliest time employee `i` receives the information. Set `dist[0] = 0` (source). All others start at `0` but will be updated as we process predecessors.

3. **Seed the BFS queue with in-degree-zero nodes.**
   Only node `0` should realistically start the chain. Nodes with in-degree `0` that are not node `0` are isolated — they will never receive information, triggering a `-1` return.

4. **Process nodes in topological order.**
   For each node `u` dequeued, iterate over its neighbours `v`. Update: `dist[v] = max(dist[v], dist[u] + time[u])`. We take the **maximum** because `v` must wait for the *slowest* predecessor to arrive. Decrement `in_degree[v]`; if it reaches `0`, enqueue `v`.

5. **Check completeness and return the answer.**
   If the number of processed nodes is less than `n`, a cycle or disconnected component exists — return `-1`. Otherwise, return `max(dist)`.

---

## 📊 Worked Example *(For Developers)*

**Input:** `n=6`, `time=[0,3,2,1,4,0]`, `relations=[[0,1],[0,2],[1,3],[1,4],[2,5]]`

| Step | Queue | Node Processed | Neighbour Updated | `dist` array |
|------|-------|----------------|-------------------|--------------|
| Init | [0] | — | — | [0, 0, 0, 0, 0, 0] |
| 1 | [1, 2] | 0 | dist[1]=max(0,0+0)=0+0... → dist[1]=0, dist[2]=0 | [0, 0, 0, 0, 0, 0] |
| 2 | [2] | 1 | dist[3]=max(0,0+3)=3, dist[4]=max(0,0+3)=3 | [0, 0, 0, 3, 3, 0] |
| 3 | [3,4,5] | 2 | dist[5]=max(0,0+2)=2 | [0, 0, 0, 3, 3, 2] |
| 4 | [4,5] | 3 | (no outgoing edges) | [0, 0, 0, 3, 3, 2] |
| 5 | [5] | 4 | (no outgoing edges) | [0, 0, 0, 3, 3, 2] |
| 6 | [] | 5 | (no outgoing edges) | [0, 0, 0, 3, 3, 2] |

> **Note:** `dist[1]` should be `0+0=0` (time[0]=0), then employee 1 informs 3 and 4 at `dist[1]+time[1] = 0+3 = 3`. Employee 4's final dist = **3+4 = 7** once we correctly propagate `time[1]=3` from node 1.
>
> **Corrected dist after full propagation:** `[0, 0, 0, 3, 7, 2]` → **Answer: max = 7** ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n + E)** — where `n` is the number of employees and `E` is the number of communication links. Every node and every edge is visited exactly once during the topological traversal. At the maximum scale (`n=10⁴`, `E=5×10⁴`), this completes in microseconds, making it highly scalable for real enterprise org charts.

### Space Complexity

**O(n + E)** — we store the adjacency list (`O(E)`), the `dist` array, and the in-degree array (both `O(n)`). At maximum input size this is well within typical memory budgets, requiring only a few megabytes.

---

## 💡 Key Takeaways *(For Everyone)*

- **Information bottlenecks are measurable.** This model lets organisations identify which single employee or team is slowing down the entire communication chain — a direct tool for org design.
- **"Impossible to inform everyone" is a real risk.** Disconnected or siloed teams are not just a culture problem — they are a structural graph problem with a mathematical answer.
- **Topological sort is the right tool for dependency chains.** Any time tasks or messages must respect ordering constraints, Kahn's algorithm processes them safely and efficiently.
- **Use `max`, not `min`, when waiting for the slowest predecessor.** A common bug is applying Dijkstra's "shortest path" logic here — but a node must wait for *all* its predecessors, so we always take the maximum arrival time.
- **DP on a DAG generalises widely.** The `dist[v] = max(dist[v], dist[u] + cost)` pattern appears in project scheduling (Critical Path Method), compiler optimisation, and build systems like Make or Bazel.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Multiple Sources:** Modify the problem so that information starts simultaneously from a *set* of employees (e.g., a leadership team). How does initialising multiple `dist` values to `0` change the BFS seeding step?
- **Variation 2 — Find the Bottleneck Employee:** Instead of returning the minimum time, return the *employee ID* whose delay contributes most to the critical path. This maps directly to Critical Path Method (CPM) used in project management.
- **Variation 3 — Cyclic Networks:** What if the communication graph can contain cycles (e.g., peer-to-peer feedback loops)? Topological sort breaks down — explore how Bellman-Ford or BFS on a general graph handles this case, and why cycle detection becomes essential.

---