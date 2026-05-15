# Minimum Fuel to Visit All Checkpoints

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Dijkstra, Bitmask DP, Shortest Path

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have a map of cities connected by roads, and each road costs a certain amount of fuel to drive. You start at city zero and must visit a specific set of important cities — in any order you like. The goal is to plan the most fuel-efficient route that guarantees you visit every required city, even if that means passing through other cities along the way.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This problem mirrors challenges faced by logistics companies like FedEx or Amazon every single day. When a delivery driver must visit a fixed set of stops across a city, finding the cheapest route directly reduces fuel costs, driver hours, and carbon emissions. The same logic powers field service scheduling (think telecom engineers visiting multiple repair sites), drone delivery path planning, and even sales territory routing. Shaving even 5–10% off route costs across thousands of daily trips translates into millions of dollars in annual savings and measurable sustainability improvements.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a road trip where you must visit a handful of specific landmarks — say, three national parks — starting from your home. You can take any roads you want, pass through any towns, and visit the parks in any order. Some roads are longer and burn more gas. Your mission: find the travel plan that gets you to every required landmark while spending the least possible fuel overall.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given `n` cities (labeled `0` to `n-1`) connected by bidirectional weighted edges, and a list of `checkpoints` (up to 12 cities), find the minimum total edge-weight cost to visit every checkpoint starting from city `0`. Non-checkpoint cities may be used as intermediaries, and revisiting cities is allowed. Return `-1` if any checkpoint is unreachable.

**Constraints:** `2 ≤ n ≤ 1000`, `0 ≤ roads.length ≤ 5000`, `1 ≤ w ≤ 10^4`, `1 ≤ checkpoints.length ≤ 12`.

**Example 1:**
- Input: `n=5`, `roads=[[0,1,2],[1,2,3],[2,3,1],[3,4,4],[1,4,10]]`, `checkpoints=[2,4]`
- Output: `6`

**Example 2:**
- Input: `n=3`, `roads=[[0,1,1],[1,2,2]]`, `checkpoints=[2]`
- Output: `3`

---

## 🧩 Approach: How We Solve It *(For Developers)*

The solution combines **Dijkstra's shortest-path algorithm** with **Bitmask Dynamic Programming** — a classic technique for small sets of required nodes.

1. **Identify key nodes.** Treat city `0` (start) and all `checkpoints` as the only nodes that matter for state transitions. This gives us at most 13 key nodes.

2. **Run Dijkstra from each key node.** For every key node (start + each checkpoint), run Dijkstra across the full graph to compute the true shortest path to every other key node. This accounts for optimal routing through non-checkpoint intermediary cities. If any checkpoint is unreachable from the start, return `-1` immediately.

3. **Build a reduced distance matrix.** Store pairwise shortest distances between all key nodes in a compact `(k+1) × (k+1)` matrix, where `k = checkpoints.length`.

4. **Apply Bitmask DP.** Define `dp[mask][i]` as the minimum fuel to have visited exactly the checkpoints encoded in `mask`, currently sitting at checkpoint `i`. A bitmask is a compact binary representation — e.g., `mask = 0b101` means checkpoints 0 and 2 have been visited.

5. **Transition.** For each state `(mask, i)`, try extending to an unvisited checkpoint `j`: `dp[mask | (1<<j)][j] = min(..., dp[mask][i] + dist[i][j])`.

6. **Answer.** Return the minimum value across `dp[all_visited][i]` for all `i`.

---

## 📊 Worked Example *(For Developers)*

Using **Example 2**: `n=3`, `roads=[[0,1,1],[1,2,2]]`, `checkpoints=[2]`

| Step | Action | State |
|------|--------|-------|
| 1 | Key nodes: city `0` (start), city `2` (checkpoint index 0) | `k=1` |
| 2 | Dijkstra from city `0` | `dist[0→2] = 3` (via city 1: 1+2) |
| 3 | Dijkstra from city `2` | `dist[2→0] = 3` (symmetric) |
| 4 | Initialize DP | `dp[0b0][start] = 0` (no checkpoints visited, at city 0) |
| 5 | Transition: visit checkpoint 0 (city 2) | `dp[0b1][0] = 0 + dist[start→city2] = 3` |
| 6 | All checkpoints visited (`mask = 0b1`) | Answer = `min(dp[0b1][...]) = 3` ✅ |

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(k × (E + n) log n + k² × 2^k)** where `k` = number of checkpoints (≤ 12), `n` = cities, `E` = roads. Dijkstra runs `k+1` times, and the bitmask DP explores at most `k × 2^k` states. With `k ≤ 12`, the DP portion stays well under 50,000 operations regardless of graph size.

### Space Complexity

**O(k² + k × 2^k)**. The distance matrix holds at most 169 values. The DP table holds at most `12 × 4096 = 49,152` entries — negligible memory even on constrained hardware or embedded routing systems.

---

## 💡 Key Takeaways *(For Everyone)*

- **Real-world routing problems with a small number of mandatory stops are solvable efficiently** — the "small stops" constraint is what makes this tractable rather than computationally explosive.
- **Optimal route planning directly reduces operating costs** — every percentage improvement in fuel efficiency compounds across large fleets or delivery networks.
- **Dijkstra's algorithm is the right tool for finding cheapest paths in weighted graphs** — it guarantees the true shortest path, not just a good guess.
- **Bitmask DP is the key insight for "visit all required nodes" problems** — encoding visited sets as integers lets you compactly represent exponentially many states in a manageable table.
- **The trick of reducing a large graph to a small key-node distance matrix is widely reusable** — whenever `checkpoints ≤ ~20`, this pattern (Dijkstra preprocessing + Bitmask DP) is the go-to approach for minimum-cost coverage problems.

---

## 🚀 Try It Yourself *(For Developers)*

- **Travelling Salesman (return trip):** Modify the problem so you must return to city `0` after visiting all checkpoints — how does the DP transition and final answer change?
- **Mandatory ordering:** What if checkpoints must be visited in a fixed sequence (checkpoint 0 before checkpoint 1, etc.)? Can you adapt the bitmask to enforce ordering constraints?
- **Larger checkpoint sets:** When `checkpoints.length` grows beyond 20, bitmask DP becomes infeasible. Research **branch-and-bound** or **approximation algorithms** (e.g., Christofides' algorithm) as alternatives for that regime.

---