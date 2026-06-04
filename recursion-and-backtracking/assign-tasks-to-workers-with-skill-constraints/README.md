# Assign Tasks to Workers with Skill Constraints

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Recursion and Backtracking &nbsp;|&nbsp; **Tags:** Backtracking, Recursion, Array

---

## 🗂 Problem Overview

Given `m` workers (each with a max skill level) and `n` tasks (each with a required skill level, `n <= m`), find every valid way to assign exactly one worker to each task such that the worker's skill meets or exceeds the task's requirement and no worker is used twice. Return all valid assignments as lists of worker indices, one per task, in lexicographic order. The non-trivial constraint: you must enumerate the full assignment space without duplicates, not just verify that one valid assignment exists.

---

## 🌍 Engineering Impact

This pattern — constrained bijective assignment with full enumeration — appears directly in job scheduling engines (Kubernetes pod-to-node binding with resource constraints), compiler register allocation (assigning virtual registers to physical ones under capability constraints), and test infrastructure (distributing test suites across heterogeneous CI workers). At scale, the backtracking structure informs branch-and-bound solvers and constraint propagation engines. Without systematic pruning of invalid branches, naive enumeration explodes; the backtracking skeleton here is the foundation that production solvers like Google OR-Tools build on.

---

## 🔍 Problem Statement

**Input:**
- `workers`: integer array of length `m`, where `workers[i]` is worker `i`'s maximum skill level.
- `tasks`: integer array of length `n`, where `tasks[j]` is task `j`'s required skill level.
- Constraint: `1 <= n <= m <= 8`, `1 <= workers[i], tasks[j] <= 10`.

**Output:** All valid assignments as a list of lists. Each inner list has length `n`; position `i` holds the 0-based index of the worker assigned to task `i`. All worker indices within a single assignment must be distinct. Results must be in lexicographically sorted order.

**Examples:**
```
workers=[3,5,2], tasks=[2,4]  →  [[0,1],[2,1]]
workers=[4,4],   tasks=[3,3]  →  [[0,1],[1,0]]
```

**Edge cases:** Multiple workers with identical skill levels produce distinct assignments because indices differ. `n == m` means every worker must be assigned.

---

## 🪜 How to Solve This

1. **Read the output requirement** → we need *all* valid assignments, not just one. That immediately signals exhaustive search, not greedy.
2. **Notice the structure** → we're assigning tasks sequentially (task 0, then task 1, …). For each task, we choose one unused worker who qualifies. This is a decision tree.
3. **Decision tree + constraints** → classic backtracking. At each level of recursion, we pick a worker for the current task, recurse for the next task, then undo the choice (un-mark the worker as used).
4. **Pruning** → before recursing, check `workers[w] >= tasks[taskIndex]`. Skip workers who can't handle the task — this prunes entire subtrees early.
5. **Lexicographic order** → iterating worker indices from `0` to `m-1` at each level naturally produces results in ascending index order per position, which yields lexicographic output without a post-sort step.
6. **Base case** → when `taskIndex == n`, all tasks are assigned; record the current assignment.

The constraint size (`m <= 8`) confirms exhaustive backtracking is the intended approach — worst case is `8!` paths, which is trivially fast.

---

## 🧩 Algorithm Walkthrough

**Pattern: Backtracking over a permutation space with a feasibility filter.**

1. **Initialize** a boolean `used[m]` array (all false) and an integer `assignment[n]` array to track the current partial assignment.
2. **Recurse** with `taskIndex = 0`. At each call, iterate `w` from `0` to `m-1`:
   - **Feasibility check:** skip if `used[w]` is true or `workers[w] < tasks[taskIndex]`. This is the constraint propagation step — it prunes branches where no valid completion exists for this worker choice.
   - **Choose:** set `assignment[taskIndex] = w`, mark `used[w] = true`.
   - **Recurse:** call with `taskIndex + 1`.
   - **Unchoose:** mark `used[w] = false` (backtrack). The assignment slot will be overwritten on the next iteration, so no explicit reset is needed there.
3. **Base case:** when `taskIndex == n`, copy `assignment` into the results list. Copying is critical — the same array is mutated throughout.
4. **Invariant maintained:** at every recursive level, `used` accurately reflects exactly which workers are committed in the current partial assignment above this level.
5. **Lexicographic correctness:** because worker indices are iterated in ascending order at every level, the first complete assignment found is lexicographically smallest, and the natural DFS order produces the full sorted output.

---

## 📊 Worked Example

**Input:** `workers = [3, 5, 2]`, `tasks = [2, 4]`

| Call depth | taskIndex | Worker tried | Feasible? | assignment | used       | Action              |
|------------|-----------|--------------|-----------|------------|------------|---------------------|
| 0          | 0         | w=0 (skill 3)| ✅ 3≥2    | [0, _]     | [T,F,F]    | recurse             |
| 1          | 1         | w=0 (skill 3)| ❌ used   | —          | —          | skip                |
| 1          | 1         | w=1 (skill 5)| ✅ 5≥4    | [0, 1]     | [T,T,F]    | **record [0,1]**    |
| 1          | 1         | w=2 (skill 2)| ❌ 2<4    | —          | —          | skip                |
| 0          | 0         | w=1 (skill 5)| ✅ 5≥2    | [1, _]     | [F,T,F]    | recurse             |
| 1          | 1         | w=1 (skill 5)| ❌ used   | —          | —          | skip                |
| 1          | 1         | w=0,w=2      | ❌ 3<4,2<4| —          | —          | skip both           |
| 0          | 0         | w=2 (skill 2)| ✅ 2≥2    | [2, _]     | [F,F,T]    | recurse             |
| 1          | 1         | w=1 (skill 5)| ✅ 5≥4    | [2, 1]     | [F,T,T]    | **record [2,1]**    |

**Result:** `[[0,1],[2,1]]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(m! / (m-n)! · n)** in the worst case — equivalent to `P(m,n)` permutations, each taking O(n) to copy into results. With `m=n=8` that's `8! = 40,320` paths. Skill constraints prune this significantly in practice. This does not scale to `m > ~12`; at that point, Hungarian algorithm or ILP solvers are the right tools.

### Space Complexity

**O(n + m)** auxiliary space: the recursion stack reaches depth `n`, the `used` array is O(m), and the `assignment` array is O(n). Output storage is O(results · n) and is unavoidable. Stack depth is bounded by `n <= 8`, so stack overflow is not a concern here.

---

## 💡 Key Takeaways

- **Pattern signal #1:** "Return *all* valid configurations" is the canonical trigger for backtracking — any problem asking for exhaustive enumeration under constraints maps to this skeleton.
- **Pattern signal #2:** When the problem involves assigning items from one set to another with a one-to-one constraint and per-item feasibility checks, you're looking at a constrained permutation problem, not a combination problem.
- **Implementation gotcha #1:** Always copy the `assignment` array when recording a result — storing a reference to the mutable array means every recorded result will reflect the final (backtracked) state.
- **Implementation gotcha #2:** Iterating workers from index `0` to `m-1` at every level is what guarantees lexicographic output; any reordering (e.g., sorting workers by skill first) breaks this property and requires a post-sort.
- **Architectural insight:** The `used[]` boolean array is a compact bitmask in disguise — in production constraint solvers, this becomes a bitset enabling O(1) propagation checks and efficient cloning for parallel search branches.

---

## 🚀 Variations & Further Practice

- **Weighted optimal assignment (Hungarian Algorithm):** Add a cost matrix `cost[worker][task]` and find the minimum-cost valid assignment instead of all assignments. The twist: exhaustive backtracking becomes intractable; you need the O(n³) Hungarian algorithm or dynamic programming over bitmask states (`dp[mask]` = min cost to assign tasks corresponding to set bits).
- **Multiple tasks per worker with capacity constraints:** Allow each worker to handle up to `k` tasks. This breaks the simple `used[]` flag into a capacity counter, dramatically expanding the search space and requiring tighter pruning heuristics or a flow-network formulation (min-cost max-flow).
- **Online assignment with worker availability windows:** Workers and tasks arrive in a stream with deadlines. The static backtracking model breaks entirely; this becomes a real-time scheduling problem requiring priority queues, greedy approximations, or online bipartite matching algorithms.