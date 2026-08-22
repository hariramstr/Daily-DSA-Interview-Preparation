# Minimum Peak Load Limit for Batch Servers

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given a fixed-order array of job processing times and `m` servers, partition the array into at most `m` contiguous groups so that every job is assigned exactly once and the largest group sum is minimized. Return that minimum possible peak load. The challenge is that preserving order rules out arbitrary balancing, and brute-forcing all cut positions is exponential. The key property is monotonic feasibility: if a load limit works, any larger limit also works.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered work cannot be reshuffled: log segment compaction, Kafka partition replay windows, video transcoding batches, CI job sharding with dependency order, and storage migration waves. In these systems, the decision is rarely “what is the partition,” but “what capacity threshold guarantees safe execution.” Without exploiting monotonic feasibility, planners degrade into combinatorial search and become unusable at production scale. This approach enables predictable admission control, capacity planning, and rollout safety checks with tight latency even for hundreds of thousands of work units.

## 🔍 Problem Statement
You are given an integer array `jobs` where `jobs[i]` is the processing time of the `i`-th job in arrival order, and an integer `m` representing the maximum number of servers that may be used. Each used server must process one non-empty contiguous block of jobs. Every job must belong to exactly one block, and job order cannot change.

Return the smallest integer `L` such that `jobs` can be partitioned into between `1` and `m` contiguous groups, with each group sum at most `L`.

Constraints:
- `1 <= jobs.length <= 200000`
- `1 <= jobs[i] <= 1000000000`
- `1 <= m <= jobs.length`
- Answer fits in signed 64-bit integer

Examples:
- `jobs = [7,2,5,10,8], m = 2` → `18`
- `jobs = [1,4,4,3,2], m = 3` → `5`

The decisive constraint is input size: `n = 200000` makes partition enumeration infeasible, forcing an `O(n log range)` strategy.

## 🪜 How to Solve This
1. Read the problem → this is not arbitrary load balancing, because jobs must stay in order and each server gets a contiguous block.

2. The output is a minimum possible maximum group sum → that usually suggests “search the answer,” not “construct all partitions.”

3. Ask: for a candidate load limit `L`, can I check feasibility quickly? Yes. Scan left to right and greedily pack jobs into the current server until adding the next job would exceed `L`; then start a new server.

4. Why is greedy enough? Because for a fixed `L`, delaying a split as long as possible minimizes the number of groups used. If even that minimum exceeds `m`, no other partitioning under `L` can succeed.

5. That gives a monotonic predicate:
   - If `L` is feasible, any larger `L` is also feasible.
   - If `L` is infeasible, any smaller `L` is also infeasible.

6. Monotonic predicate + integer answer range → binary search between:
   - lower bound = `max(jobs)`  
   - upper bound = `sum(jobs)`

That yields the minimum valid peak load efficiently.

## 🧩 Algorithm Walkthrough
1. **Establish search bounds using Binary Search on Answer.**  
   The minimum possible load cannot be below the largest single job, since every job must fit somewhere. The maximum possible load is the total sum, corresponding to one server handling everything. This defines a closed search interval `[max(jobs), sum(jobs)]`.

2. **Define the feasibility check using a Greedy scan.**  
   For a candidate limit `L`, iterate through `jobs` left to right, maintaining the current group sum and the number of groups used. Add a job if it keeps the sum `<= L`; otherwise, start a new group with that job. This is the right abstraction because for fixed-order contiguous partitioning, greedily filling each group produces the fewest groups possible under `L`.

3. **Maintain the key invariant.**  
   After processing prefix `jobs[0..i]`, the algorithm has used the minimum number of groups needed for that prefix without any group exceeding `L`. If the greedy scan needs more than `m` groups, then no valid partition exists for `L`.

4. **Binary search for the first feasible limit.**  
   Compute `mid`. If `mid` is feasible, record it as a candidate and search left for a smaller valid limit. If infeasible, search right. Because feasibility is monotonic, this converges to the smallest valid `L`.

5. **Use 64-bit arithmetic.**  
   Group sums and the total sum can exceed 32-bit range. The logic is correct only if accumulation and midpoint computation are done with 64-bit integers.

## 📊 Worked Example
Example: `jobs = [7, 2, 5, 10, 8]`, `m = 2`

Initial bounds: `low = 10`, `high = 32`

| mid | Greedy groups formed under `mid` | groups | Feasible? |
|---|---|---:|---|
| 21 | `[7,2,5]`, `[10,8]` | 2 | Yes |
| 15 | `[7,2,5]`, `[10]`, `[8]` | 3 | No |
| 18 | `[7,2,5]`, `[10,8]` | 2 | Yes |
| 16 | `[7,2,5]`, `[10]`, `[8]` | 3 | No |
| 17 | `[7,2,5]`, `[10]`, `[8]` | 3 | No |

Trace:
1. `mid = 21` works, so search left.
2. `mid = 15` fails because greedy already needs 3 groups.
3. `mid = 18` works.
4. `mid = 16` and `17` fail.
5. Smallest feasible limit is `18`.

The important observation is that the greedy pass is not guessing a partition; it is proving the minimum number of groups required for each candidate limit.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n = jobs.length` and `S = sum(jobs) - max(jobs) + 1` is the search range. Each binary-search step performs one linear feasibility scan. In practice, this stays efficient even for `10^6` elements, while any partition-enumeration strategy collapses long before `10^9` state combinations.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only running sums, counters, and binary-search bounds. Space cannot meaningfully be reduced further; the main trade-off is not memory but ensuring arithmetic uses 64-bit types to avoid overflow.

## 💡 Key Takeaways
- If the problem asks for the minimum possible maximum value and feasibility becomes easier as the limit grows, think binary search on the answer.
- If partitions must be contiguous and order is fixed, a left-to-right greedy feasibility check is often the right companion to binary search.
- The lower bound is `max(jobs)`, not `0` or `min(jobs)`; any smaller value makes the largest job impossible to place.
- In the feasibility pass, split only when adding the next job would exceed the limit; splitting earlier can overcount groups and break correctness.
- At scale, this pattern converts an intractable partition-search problem into a deterministic capacity-threshold computation suitable for schedulers and admission-control systems.

## 🚀 Variations & Further Practice
- Require **exactly `m` groups** instead of at most `m`; the twist is reasoning about whether extra splits can always be introduced without violating the limit.
- Minimize the maximum load when jobs may be assigned in **arbitrary order**; contiguity disappears and the problem shifts toward bin packing / NP-hard territory.
- Add a **per-server startup cost or weighted objective**; feasibility is no longer a simple monotonic count, so the binary-search predicate may need dynamic programming instead of greedy.