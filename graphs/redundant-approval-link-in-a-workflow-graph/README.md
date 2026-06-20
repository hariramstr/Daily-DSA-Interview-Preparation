# Redundant Approval Link in a Workflow Graph

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Union Find, Directed Graph

---

## 🗂 Problem Overview
You are given `n` directed edges over nodes `1..n`, where the intended structure is a rooted workflow with exactly `n - 1` edges. One extra edge was added, so the graph now has either a node with two parents, a directed cycle, or both. Return the single edge whose removal restores a valid rooted workflow. The non-trivial part is that this is a directed graph, but the most efficient cycle check uses Union-Find, so you must combine indegree analysis with connectivity reasoning.

## 🌍 Engineering Impact
This pattern shows up anywhere a system expects a tree-shaped dependency model but receives one accidental extra relation: workflow engines, CI/CD stage graphs, IAM approval chains, compiler pass scheduling, orchestration DAG validators, and metadata lineage systems. At scale, a single redundant edge can create deadlocks, duplicate execution, invalid topological order, or ambiguous ownership. The useful insight is not just cycle detection; it is reconciling local structural violations like “two parents” with global graph validity. That combination is what production validators need when ingesting partially trusted configuration or user-authored dependency data.

## 🔍 Problem Statement
Given `edges`, an array of length `n`, where each element is a directed edge `[u, v]`, find the one edge to remove so the remaining graph is a valid rooted workflow over the same `n` nodes.

A valid rooted workflow must have:
- exactly one node with indegree `0`
- every other node with indegree `1`
- no directed cycle
- full reachability from the root

Constraints:
- `2 <= n <= 100000`
- `edges.length == n`
- `1 <= u, v <= n`
- `u != v`

Examples:

```text
Input:  [[1,2],[1,3],[2,3]]
Output: [2,3]
```

```text
Input:  [[1,2],[2,3],[3,4],[4,1],[1,5]]
Output: [4,1]
```

The key constraint is `n = 100000`, which rules out repeated graph reconstruction or per-edge DFS checks. You need near-linear time, which strongly points to Union-Find plus a single pass for indegree conflict detection.

## 🪜 How to Solve This
1. Start from the target shape: a rooted tree in directed form means every node except one has exactly one parent.  
2. With one extra edge, only two structural failures are possible:
   - some node gets **two parents**
   - a **cycle** appears  
   Sometimes both happen together.

3. A node with two parents is the first thing to detect, because it gives you exactly two candidate edges: the earlier parent edge and the later parent edge.

4. Then ask: if I temporarily ignore the later conflicting edge, does the rest of the graph still contain a cycle?
   - If yes, the earlier edge is the real problem.
   - If no, the later edge is the one to remove.

5. If no node has two parents, the problem reduces to classic cycle detection in a graph that should otherwise be a tree. That is exactly where Union-Find is efficient.

6. The mental model is: **indegree analysis identifies candidate bad edges; Union-Find decides which candidate actually breaks tree validity**. That combination gives an `O(n)` solution without expensive directed-graph traversals from every possibility.

## 🧩 Algorithm Walkthrough
1. **Scan for indegree violations using a parent array.**  
   For each edge `[u, v]`, record the first parent of `v`. If `v` already has a parent, then `v` has two incoming edges. Save:
   - `candidate1 = [firstParent, v]`
   - `candidate2 = [u, v]`  
   This is correct because in a valid rooted tree, no node can have indegree `2`.

2. **Run Union-Find over the edges, optionally skipping `candidate2`.**  
   Pattern: **Union-Find (Disjoint Set Union)** for cycle detection in an almost-tree.  
   If a two-parent conflict exists, skip the later edge `candidate2` during union. This tests whether removing that edge is sufficient.

3. **For each processed edge `[u, v]`, try to union `u` and `v`.**  
   If `find(u) == find(v)`, adding this edge closes a cycle.  
   Invariant: after each successful union, the processed edges remain acyclic in the undirected sense needed for this tree-restoration test.

4. **Resolve by case.**
   - **No two-parent conflict:** the edge that triggers the cycle is the answer.
   - **Two-parent conflict and no cycle when skipping `candidate2`:** remove `candidate2`.
   - **Two-parent conflict and cycle still exists when skipping `candidate2`:** remove `candidate1`.  
   Why: if skipping the later edge does not eliminate the cycle, the earlier parent edge must be participating in the invalid structure.

5. **Return the edge in input-order preference automatically.**  
   The construction respects the “return the last valid choice” rule because `candidate2` is the later incoming edge, and in the pure-cycle case the cycle-forming edge encountered last is returned.

## 📊 Worked Example
Use `edges = [[1,2],[1,3],[2,3]]`.

| Step | Edge   | parent[3] | candidate1 | candidate2 | Union-Find action |
|------|--------|-----------|------------|------------|-------------------|
| 1    | [1,2]  | —         | —          | —          | union(1,2)        |
| 2    | [1,3]  | 1         | —          | —          | union(1,3) later  |
| 3    | [2,3]  | already 1 | [1,3]      | [2,3]      | mark conflict     |

Now rerun Union-Find while skipping `candidate2 = [2,3]`:

1. Process `[1,2]` → union succeeds.  
2. Process `[1,3]` → union succeeds.  
3. Skip `[2,3]`.

No cycle appears, so removing the later conflicting edge is enough. Final answer: `[2,3]`.

This trace shows the core decision rule: when a node has two parents, test whether dropping the later one restores tree structure. If yes, that later edge is redundant.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n * α(n))`, which is effectively linear because each edge is scanned once for indegree analysis and once for Union-Find operations. At `10^6` elements this is still practical; at `10^9`, the bottleneck becomes memory bandwidth and input size rather than asymptotic DSU cost.

### Space Complexity
Space is `O(n)` for the parent array plus Union-Find parent/rank structures. That is the irreducible cost of tracking node ownership and component state. You could compress some metadata, but not without sacrificing clarity or making path-compression logic harder to maintain.

## 💡 Key Takeaways
- If a directed graph is “almost a rooted tree” and has exactly one extra edge, immediately check for the two canonical failures: a node with two parents and a cycle.
- When the graph is directed but the target structure is tree-like, Union-Find is often still the right cycle detector once indegree constraints narrow the cases.
- The subtle trap is handling the node with two parents: you must preserve both candidate edges and defer the final choice until after the cycle test.
- Another common bug is unioning all edges blindly after detecting a two-parent conflict; you must skip the later candidate during validation or you lose the ability to distinguish the cases.
- In production graph validation, local invariants and global invariants should be checked together; neither indegree rules nor cycle detection alone is sufficient for structural correctness.

## 🚀 Variations & Further Practice
- **Redundant Connection (undirected):** same Union-Find core, but without directed indegree constraints; the twist is that cycle detection alone is sufficient.
- **Course Schedule / topological validation:** moves from “remove one bad edge” to validating arbitrary directed acyclic dependencies; the harder part is full DAG reasoning rather than almost-tree repair.
- **Arborescence validation with multiple corrupt edges:** extends this problem from one extra edge to arbitrary invalid workflow edits, requiring stronger graph algorithms and explicit root/reachability verification.