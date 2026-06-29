# Detect Circular Package Dependencies

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Graphs &nbsp;|&nbsp; **Tags:** Graphs, Topological Sort, Cycle Detection

---

## 🗂 Problem Overview
Given `n` packages and directed dependencies `[a, b]` meaning `a` requires `b`, determine whether every package can be installed in some valid order. Return `true` if the dependency graph is acyclic, otherwise `false`. The challenge is not ordering a small set manually, but detecting cycles efficiently under large input bounds: up to `100,000` nodes and `200,000` edges, including isolated packages and duplicate dependency pairs.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must build, deploy, initialize, or evaluate components with prerequisites: package managers, Bazel/Buck build graphs, Terraform resource plans, Airflow or Dagster pipelines, compiler module resolution, service startup orchestration, and database migration frameworks. At scale, missing cycle detection turns into failed deploys, deadlocked initialization, or non-terminating planners. Acyclicity checks are also a governance mechanism: they enforce architectural layering and prevent teams from introducing hidden coupling. Once the graph is known to be a DAG, you unlock deterministic ordering, parallel execution of independent nodes, and safer incremental recomputation.

## 🔍 Problem Statement
You are given `n` packages labeled `0` to `n - 1` and a list of directed dependencies. Each pair `[a, b]` means package `a` depends on package `b`, so `b` must be installed before `a`. Return `true` if all packages can be installed in some order, and `false` if any circular dependency exists.

A package may not appear in the dependency list at all and must still be considered part of the system. Duplicate dependency pairs may exist. Self-dependencies are excluded by constraint (`a != b`).

**Constraints**
- `1 <= n <= 100000`
- `0 <= dependencies.length <= 200000`
- `0 <= a, b < n`

**Examples**
- `n = 4, dependencies = [[1,0],[2,1],[3,2]]` → `true`
- `n = 4, dependencies = [[1,0],[2,1],[0,2],[3,1]]` → `false`

The key algorithmic constraint is scale: nested reachability checks per node are too expensive, so the solution must be linear in nodes plus edges.

## 🪜 How to Solve This
1. Read the dependency rule carefully → `[a, b]` is a directed edge `b -> a` if you want installation order semantics. That immediately suggests a graph problem, not a sorting or set-membership problem.

2. Ask what makes deployment invalid → not “multiple dependencies,” but a cycle. If `x` eventually depends on itself, no valid ordering exists.

3. Connect that to a known graph fact → a directed graph has a valid topological ordering if and only if it is acyclic. So the problem reduces to cycle detection in a directed graph.

4. From there, two standard approaches appear:
   - DFS with three-state visitation (`unvisited`, `visiting`, `visited`) detects a back-edge.
   - Kahn’s algorithm repeatedly removes zero-indegree nodes; if some nodes remain, they are trapped in a cycle.

5. Given the constraints, both are `O(n + m)`. For production-style robustness, indegree-based topological sort is often simpler to reason about iteratively and avoids recursive stack depth issues on long chains.

6. Final check → if you can process all `n` packages, the configuration is valid; otherwise, some cycle prevented progress.

## 🧩 Algorithm Walkthrough
1. **Model the dependencies as a directed graph using Topological Sort / Kahn’s Algorithm.**  
   For each dependency `[a, b]`, add an edge `b -> a` and increment `indegree[a]`. This orientation matches the install rule: prerequisites point to dependents. The invariant is that `indegree[x]` always equals the number of unresolved prerequisites for package `x`.

2. **Initialize a queue with every package whose indegree is zero.**  
   These packages have no unmet dependencies and can be installed immediately. Include isolated packages as well; they are valid nodes with zero indegree and must count toward completion.

3. **Process the queue iteratively.**  
   Pop one package, count it as installed, and traverse its outgoing edges. For each dependent package `v`, decrement `indegree[v]` because one prerequisite has now been satisfied. If `indegree[v]` becomes zero, enqueue it. The invariant is that the queue contains exactly the packages currently installable.

4. **Detect cycles by completion count.**  
   If the graph is acyclic, repeated removal of zero-indegree nodes eventually processes all `n` packages. If a cycle exists, every node in that cycle retains indegree at least one, so the queue empties early. That stalled state is the proof of circular dependency.

5. **Return `processed == n`.**  
   This is correct because topological ordering exists exactly for DAGs. Duplicate edges are harmless if handled consistently: they increase indegree twice and are decremented twice when the source is processed.

## 📊 Worked Example
Example: `n = 4`, `dependencies = [[1,0],[2,1],[0,2],[3,1]]`

Interpret edges as `0->1`, `1->2`, `2->0`, `1->3`.

| Step | Queue | Processed | Indegree `[0,1,2,3]` | Notes |
|---|---|---:|---|---|
| Build graph | `[]` | 0 | `[1,1,1,1]` | Every node has an unmet prerequisite |
| Init queue | `[]` | 0 | `[1,1,1,1]` | No zero-indegree node exists |
| Loop check | `[]` | 0 | `[1,1,1,1]` | Cannot start installation |

Because the queue is empty before any package is processed, there is no valid starting point. That only happens when all remaining nodes are blocked by unresolved prerequisites, which here is caused by the cycle `0 -> 1 -> 2 -> 0`. Package `3` is not itself in the cycle, but it depends on `1`, so it is also blocked. Final result: `processed = 0 != n`, therefore return `false`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n` is the number of packages and `m` is the number of dependency pairs. Building adjacency lists and indegree counts is linear, and each node and edge is processed at most once. This remains practical at `10^6` scale, while anything quadratic becomes operationally irrelevant long before `10^9`.

### Space Complexity
`O(n + m)` for the adjacency list, indegree array, and queue. The graph representation dominates memory. You can trade some structure overhead with more compact storage layouts, but not the asymptotic bound unless you give up linear-time traversal.

## 💡 Key Takeaways
- If the problem asks whether prerequisites allow a valid execution or installation order, think directed graph + topological sort immediately.
- If invalidity is defined by “something eventually depends on itself,” that is cycle detection in a directed graph, not generic reachability.
- Be explicit about edge direction: for `[a, b]`, using `b -> a` makes indegree mean “remaining prerequisites,” which simplifies reasoning.
- Do not forget isolated packages; they still count toward `n` and must be included in initialization and final completion checks.
- In production systems, cycle detection is not just correctness logic; it is an architectural guardrail against hidden coupling and non-deployable dependency graphs.

## 🚀 Variations & Further Practice
- **Return one valid installation order instead of a boolean.** Same topological-sort core, but now you must emit the processed sequence and handle the “no order exists” case cleanly.
- **Return the actual cycle path.** Harder because Kahn’s algorithm only proves a cycle exists; DFS with parent tracking is better when diagnostics matter.
- **Support dynamic dependency updates.** Much harder operationally: incremental cycle detection under edge insertions requires maintaining graph state efficiently rather than recomputing from scratch.