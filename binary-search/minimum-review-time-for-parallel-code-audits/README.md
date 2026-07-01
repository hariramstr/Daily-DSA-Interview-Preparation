# Minimum Review Time for Parallel Code Audits

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Bitmask DP, Scheduling

---

## 🗂 Problem Overview
Given `n` indivisible code changes and `m` reviewers with different review speeds, assign every change to exactly one reviewer to minimize the overall completion time. If reviewer `j` gets total work `W`, their time is `ceil(W / speed[j])`. The challenge is that `n` is huge but `m` is small, so naive partitioning over tasks is impossible; the solution must exploit a feasibility check inside a binary search over time.

## 🌍 Engineering Impact
This pattern shows up in release orchestration, heterogeneous job scheduling, GPU/CPU batch placement, warehouse picking, and distributed ingest pipelines where work units are indivisible but workers have different throughput. At scale, greedy balancing by total load alone fails because worker capacity depends on a candidate deadline and tasks cannot be fractionally split. The binary-search-plus-feasibility framing is what turns an intractable optimization problem into something operationally useful: deadline admission control, capacity planning, and “can we ship by X?” decisions become computable instead of heuristic.

## 🔍 Problem Statement
You are given:

- `reviewWork[i]`: work units required for the `i`-th code change
- `speed[j]`: work units per hour reviewer `j` can process

Each code change must be assigned to exactly one reviewer. A reviewer may process multiple assigned changes sequentially, but a single change cannot be split across reviewers.

If reviewer `j` is assigned total work `W`, completion time is:

`ceil(W / speed[j])`

Return the minimum possible value of the maximum reviewer completion time.

**Constraints**
- `1 <= n <= 2 * 10^5`
- `1 <= m <= 20`
- `1 <= reviewWork[i], speed[j] <= 10^9`
- `m <= n` is not guaranteed

**Examples**
- `reviewWork = [6, 8, 5, 3], speed = [4, 2]` → `4`
- `reviewWork = [9, 9, 9, 9, 9], speed = [3, 3, 3]` → `6`

The decisive constraint is the asymmetry: very large `n`, very small `m`. That rules out assignment DP over tasks and points toward compressing work into at most `m` effective groups.

## 🪜 How to Solve This
1. Read the objective → we are minimizing the maximum completion time. That is the classic signal for **binary search on the answer**.

2. Fix a candidate time `T` → reviewer `j` can process at most `cap[j] = T * speed[j]` total work within `T` hours. The problem becomes: can all tasks be packed into these `m` capacities without splitting any task?

3. Notice the scale mismatch → `n` is up to `2e5`, but `m` is only `20`. That means the expensive part must depend on `m`, not `n`.

4. Sort tasks descending and ignore obviously impossible `T` values early:
   - if any task exceeds every reviewer capacity, infeasible
   - if total work exceeds total capacity, infeasible

5. The key compression: in any feasible assignment, only the aggregate work assigned to each reviewer matters. Since `m` is small, we can reason over reviewer subsets rather than task subsets.

6. For a fixed `T`, use a **bitmask DP / subset-cover feasibility check** over reviewers, driven by capacities, to determine whether all tasks can be placed.

7. Because feasibility is monotone — if `T` works, any larger `T` also works — binary search yields the minimum feasible review time.

## 🧩 Algorithm Walkthrough
1. **Sort reviewers by speed descending and tasks by work descending.**  
   This does not change feasibility, but it improves pruning. Large tasks are the hardest to place, so failing early on them is valuable.

2. **Binary search the answer `T`.**  
   Lower bound: `0` or, tighter, `max(ceil(work_i / maxSpeed))`.  
   Upper bound: `ceil(sum(reviewWork) / min(speed))` or simply `maxWork / minSpeed + sumWork / minSpeed`.  
   Invariant: all times below `lo` are infeasible; all times at or above `hi` are feasible.

3. **For a candidate `T`, compute reviewer capacities.**  
   `cap[j] = T * speed[j]` in 64-bit arithmetic.  
   Immediate rejection:
   - `sum(reviewWork) > sum(cap)`
   - `max(reviewWork) > max(cap)`

4. **Compress tasks into prefix sums.**  
   After sorting tasks descending, build `prefix[i] = total work of first i tasks`. This lets us ask: how many largest remaining tasks can a subset of reviewers cover?

5. **Precompute subset capacities for all reviewer masks.**  
   For each mask in `[0, 2^m)`, compute `subsetCap[mask] = sum(cap[j] for j in mask)`. This is the core structure enabling subset DP.

6. **Run bitmask DP over reviewer subsets.**  
   Let `dp[mask]` be the maximum number of largest tasks that can be assigned using exactly the reviewers in `mask`.  
   Transition: for each `mask`, try adding one reviewer or one submask block; advance as far as the new total capacity can cover in prefix sums.  
   Invariant: if `dp[mask] = k`, then the first `k` largest tasks can be feasibly packed into reviewers in `mask`.

7. **Accept if `dp[(1<<m)-1] == n`.**  
   This proves every task can be assigned within `T`. Because the feasibility predicate is monotone, binary search is correct.

This is a **Binary Search + Bitmask DP** solution: binary search handles optimization; subset DP solves the fixed-deadline packing decision.

## 📊 Worked Example
Use `reviewWork = [6, 8, 5, 3]`, `speed = [4, 2]`.

Sort work descending: `[8, 6, 5, 3]`  
Prefix sums: `[0, 8, 14, 19, 22]`

Try `T = 3`:
- capacities = `[12, 6]`
- total capacity = `18 < 22` → infeasible immediately

Try `T = 4`:
- capacities = `[16, 8]`
- total capacity = `24`

Subset capacities:

| mask | reviewers | capacity | max prefix covered |
|---|---:|---:|---:|
| `00` | none | 0 | 0 |
| `01` | speed 4 | 16 | 2 tasks (`8+6=14`, next would be 19) |
| `10` | speed 2 | 8 | 1 task (`8`) |
| `11` | both | 24 | 4 tasks |

DP interpretation:
1. With reviewer `01`, cover first 2 tasks.
2. Remaining tasks are `5, 3`, which reviewer `10` can cover within capacity `8`.
3. All 4 tasks fit, so `T = 4` is feasible.

Since `3` fails and `4` works, the minimum review time is `4`.

## ⏱ Complexity Analysis

### Time Complexity
Sorting tasks costs `O(n log n)`. Each feasibility check costs `O(2^m * poly(m))` depending on the exact subset transition strategy, with `m <= 20`, and binary search adds a `log U` factor where `U` is the time range. In practice: `O(n log n + 2^m log U)`, which is viable because the exponential term depends only on reviewers, not tasks.

### Space Complexity
`O(n + 2^m)`. The `n` term comes from sorted work and prefix sums; the `2^m` term comes from subset capacities and DP state. Space can be reduced slightly by reusing arrays, but the subset tables are the essential cost of making the small-`m` strategy fast.

## 💡 Key Takeaways
- If the problem asks for the **minimum possible maximum time/load**, binary search on the answer should be one of the first candidate patterns.
- If one dimension is huge (`n`) and another is tiny (`m <= 20`), look for a state space exponential in the small dimension rather than the large one.
- Use 64-bit arithmetic for `T * speed[j]`, prefix sums, and total capacities; 32-bit overflow will silently invalidate feasibility.
- Be careful with lower bounds and `ceil(W / speed)` reasoning: the feasibility check should compare work against `T * speed`, not repeatedly compute per-task rounded times.
- The transferable design insight is to separate **optimization** from **admission control**: solve “can we meet deadline `T`?” efficiently, then wrap it in a monotone search.

## 🚀 Variations & Further Practice
- **Reviewer-task compatibility constraints:** each reviewer can audit only certain changes. The twist is that subset capacity alone is no longer sufficient; feasibility becomes constrained bipartite packing.
- **Minimize weighted completion penalty instead of makespan:** binary search no longer applies directly because the objective is not a monotone threshold decision.
- **Allow splitting a code change across reviewers:** the problem collapses from indivisible scheduling toward flow / continuous load balancing, changing both the lower bounds and the feasibility structure.