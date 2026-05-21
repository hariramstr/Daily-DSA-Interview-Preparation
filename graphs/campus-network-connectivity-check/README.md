# Campus Network Connectivity Check

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, BFS, Union-Find, Connected Components

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a university campus where buildings are connected by network cables. Some buildings are fully linked together, while others are isolated or only connected to a small group. This problem asks a simple question: how many separate, self-contained network islands exist on campus? A "cluster" is any group of buildings that can communicate with each other, directly or through other buildings.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Identifying isolated network clusters is a foundational task in IT infrastructure management, telecommunications, and logistics. Internet service providers use this technique to detect outages — if a segment of the network becomes disconnected, engineers need to know immediately. Ride-sharing companies like Uber use similar logic to group service zones. Supply chain managers use it to find isolated warehouses cut off from distribution hubs. Catching these disconnections early prevents costly downtime, improves service reliability, and helps teams prioritise where to invest in new infrastructure connections.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of campus buildings as islands, and network cables as bridges between them. If you can walk from Island A to Island B — even by crossing several other islands — they belong to the same group. Your job is simply to count how many completely separate island groups exist. An island with no bridges at all still counts as its own lonely group of one.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an integer `n` representing buildings numbered `0` to `n - 1`, and a list `cables` where each entry `[a, b]` represents a bidirectional edge between nodes `a` and `b`, determine the total number of **connected components** in the undirected graph.

**Constraints:**
- `1 <= n <= 1000`
- `0 <= cables.length <= 5000`
- No self-loops; no duplicate edges
- `0 <= cables[i][0], cables[i][1] < n`

**Examples:**

```
Input:  n = 6, cables = [[0,1],[1,2],[3,4]]
Output: 3
Explanation: {0,1,2}, {3,4}, {5} → 3 clusters

Input:  n = 4, cables = [[0,1],[1,2],[2,3]]
Output: 1
Explanation: {0,1,2,3} → all connected, 1 cluster
```

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use **Union-Find (Disjoint Set Union)** — an efficient data structure purpose-built for grouping and merging connected components.

**Step-by-step:**

1. **Initialise:** Create a `parent` array of size `n` where every building starts as its own cluster — `parent[i] = i`. Also maintain a `rank` array for optimisation.

2. **Define `find(x)`:** Recursively locate the "root" representative of building `x`. Apply **path compression** — point every visited node directly to the root, flattening future lookups.

3. **Define `union(a, b)`:** Find the roots of both buildings. If they differ, merge the smaller-ranked tree under the larger one (**union by rank**). This keeps the tree shallow and operations fast.

4. **Process cables:** For every cable `[a, b]`, call `union(a, b)`. This merges the two clusters those buildings belong to.

5. **Count clusters:** After processing all cables, count how many buildings are their own root (`parent[i] == i`). Each such building is the representative of a unique cluster.

6. **Return the count** — this is the number of isolated network clusters.

Path compression and union by rank together ensure near-constant time per operation, making this approach highly scalable.

---

## 📊 Worked Example *(For Developers)*

**Input:** `n = 6`, `cables = [[0,1],[1,2],[3,4]]`

| Step | Operation | Parent Array (index → parent) | Cluster Count |
|------|-----------|-------------------------------|---------------|
| Init | — | `[0, 1, 2, 3, 4, 5]` | 6 |
| Cable `[0,1]` | union(0,1) → merge root 0 & 1 | `[0, 0, 2, 3, 4, 5]` | 5 |
| Cable `[1,2]` | union(1,2) → find(1)=0, merge 0 & 2 | `[0, 0, 0, 3, 4, 5]` | 4 |
| Cable `[3,4]` | union(3,4) → merge root 3 & 4 | `[0, 0, 0, 3, 3, 5]` | 3 |
| Count roots | nodes where `parent[i]==i`: {0, 3, 5} | — | **3** |

**Output:** `3` ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n + E · α(n))** — where `E` is the number of cables and `α` is the inverse Ackermann function, which grows so slowly it is effectively constant. In practice, this means even a campus with thousands of buildings and cables is processed nearly instantaneously. The algorithm scales effortlessly within the given constraints.

### Space Complexity

**O(n)** — we store two arrays (`parent` and `rank`) each of size `n`. No adjacency list or matrix is needed. For `n = 1000`, this is negligible memory — roughly a few kilobytes.

---

## 💡 Key Takeaways *(For Everyone)*

- **Network outage detection depends on this:** Telecom and IT teams use connected-component analysis daily to identify isolated segments before users notice problems.
- **Infrastructure investment decisions:** Counting clusters reveals exactly how many separate network islands need to be bridged — directly informing budget and planning.
- **Union-Find is the right tool for dynamic grouping:** When elements are repeatedly merged into groups, Union-Find outperforms BFS/DFS by avoiding full graph traversals.
- **Path compression + union by rank is the secret:** These two optimisations together reduce each operation to near O(1) amortised time — always apply both.
- **Isolated nodes are valid clusters:** Never forget buildings with zero cables — they each count as their own cluster and are easy to miss without explicit initialisation.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Minimum cables to fully connect:** Given the cluster count, how many additional cables are needed to merge all clusters into one? *(Answer: clusters − 1)*
- **Variation 2 — Dynamic connectivity:** Extend the problem so cables can be added one at a time and you must report the cluster count after each insertion. Explore how Union-Find handles this incrementally.
- **Variation 3 — BFS/DFS alternative:** Solve the same problem using Breadth-First Search with an explicit adjacency list and a `visited` array — then benchmark both approaches against each other on large inputs.

---