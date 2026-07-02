# Minimum Training Score to Unlock Project Groups

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, prefix-sum, monotonic-function

---

## 🗂 Problem Overview
Given two arrays, `tasks` and `req`, each project group contributes `tasks[i]` tasks and requires training score `req[i]`. An employee may complete only a prefix of groups, in order, stopping at the first group whose requirement exceeds score `S`. Return the minimum integer `S` such that the completed prefix contains at least `target` tasks, or `-1` if even all groups together are insufficient. The challenge is scale: `n` is up to 200,000, so brute-forcing scores or repeatedly simulating prefixes is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a capability threshold unlocks an ordered workload: feature gates in rollout systems, compiler passes enabled by optimization level, streaming pipelines gated by schema/version compatibility, or search/ranking stages unlocked by latency budget. At scale, the wrong approach repeatedly re-evaluates the same prefix state and burns CPU on threshold checks that are structurally monotonic. Binary search over a monotonic feasibility function turns an unbounded “try every level” process into a predictable logarithmic decision loop, which matters when thresholds are large, inputs are hot, and this check sits on a critical request or scheduling path.

## 🔍 Problem Statement
You are given `tasks[i]` and `req[i]` for `n` project groups, where `1 <= n <= 200000`. Group `i` contributes `tasks[i]` tasks and can be completed only if training score `S >= req[i]`. Groups must be completed strictly in order, so a score unlocks a prefix ending just before the first unmet requirement. Find the smallest integer `S` such that the total tasks in that prefix is at least `target`. If the sum of all groups is still below `target`, return `-1`.

Constraints are large: `tasks[i]` and `req[i]` can each reach `1e9`, and `target` can reach `1e14`, so 64-bit arithmetic is required.

Examples:

- `tasks = [4,3,5,2], req = [2,6,6,9], target = 10` → `6`
- `tasks = [2,1,4], req = [5,3,8], target = 6` → `8`

The key algorithmic signal is monotonicity: if score `S` works, any larger score also works.

## 🪜 How to Solve This
1. Read the problem → the employee does **not** choose arbitrary groups; they complete a **prefix** only. That means for any score `S`, the outcome is determined by the first index where `req[i] > S`.

2. Ask what changes as `S` increases → the completable prefix can only stay the same or extend. It never shrinks. That is a monotonic feasibility function.

3. Monotonic feasibility usually means binary search over the answer space, not over the array indices. Search `S` in `[1, max(req)]`.

4. Now define `can(S)` → “does score `S` allow at least `target` tasks?” To answer it efficiently, find the first blocked group and sum tasks before it.

5. Recomputing that sum every time would be wasteful, so precompute prefix sums of `tasks`.

6. To find the first blocked group quickly, precompute prefix maxima of `req`. Then the employee can complete up to the last index where `prefixMaxReq[i] <= S`, which is itself a binary-searchable boundary.

7. Combine both layers: binary search score `S`, and inside each check, binary search the reachable prefix length. That gives an efficient `O(n + log^2 M)` solution after preprocessing.

## 🧩 Algorithm Walkthrough
1. **Precompute total tasks and fail fast.**  
   Build a 64-bit prefix sum array `prefixTasks`, where `prefixTasks[i]` is total tasks through index `i`. If `prefixTasks[n-1] < target`, return `-1` immediately. This is correct because no score can unlock more than the full array.

2. **Precompute prefix maxima of requirements.**  
   Build `prefixMaxReq[i] = max(req[0..i])`. This captures the true requirement to complete the first `i+1` groups in order. Even if `req[i]` is small, a larger earlier requirement still blocks progress. The invariant is: score `S` can complete prefix ending at `i` iff `prefixMaxReq[i] <= S`.

3. **Define the monotonic predicate `can(S)`.**  
   Using binary search on `prefixMaxReq`, find the largest index `j` such that `prefixMaxReq[j] <= S`. Then the completed task count is `prefixTasks[j]` (or `0` if no such `j`). Return whether that count is at least `target`. This is monotonic because increasing `S` can only increase `j`.

4. **Binary search the minimum valid score.**  
   Search `S` over `[1, max(req)]`. For midpoint `mid`, evaluate `can(mid)`. If true, keep the left half because a smaller valid score may exist; otherwise move right. Maintain the invariant that the answer, if it exists, always remains inside the current interval.

5. **Return the leftmost feasible score.**  
   When the search converges, the remaining value is the smallest score satisfying the predicate. This is the standard **Binary Search on Answer** pattern, powered by a monotonic function and accelerated by **prefix sums** plus a second binary search over the reachable prefix.

## 📊 Worked Example
Use `tasks = [4,3,5,2]`, `req = [2,6,6,9]`, `target = 10`.

Precompute:

| i | tasks[i] | req[i] | prefixTasks | prefixMaxReq |
|---|----------|--------|-------------|--------------|
| 0 | 4        | 2      | 4           | 2            |
| 1 | 3        | 6      | 7           | 6            |
| 2 | 5        | 6      | 12          | 6            |
| 3 | 2        | 9      | 14          | 9            |

Binary search score in `[1, 9]`:

1. `mid = 5`  
   Largest `prefixMaxReq <= 5` is index `0` → reachable tasks `4` → not enough.

2. `mid = 7`  
   Largest `prefixMaxReq <= 7` is index `2` → reachable tasks `12` → enough.

3. Narrow to `[6, 7]`, then `mid = 6`  
   Largest reachable index is still `2` → tasks `12` → enough.

Now left boundary is `6`, so the minimum valid score is `6`.

## ⏱ Complexity Analysis
### Time Complexity
Preprocessing prefix sums and prefix maxima costs `O(n)`. Each feasibility check performs a binary search on `prefixMaxReq`, and the outer search over score range costs `O(log M)`, where `M = max(req)`. Total time is `O(n + log n · log M)`, effectively linear for this input size. At million-scale arrays this remains practical; at billion-scale arrays, preprocessing and memory bandwidth dominate.

### Space Complexity
`O(n)` extra space for `prefixTasks` and `prefixMaxReq`. That space is what enables fast repeated feasibility checks. You can reduce auxiliary storage by computing one structure in place or combining logic, but usually at the cost of code clarity or more complex indexing.

## 💡 Key Takeaways
- If the prompt asks for the minimum threshold/value that makes a condition true, check immediately whether the condition is monotonic; that is the strongest signal for binary search on answer.
- When progress is constrained to an ordered prefix, convert per-element requirements into a prefix-level requirement, often via prefix max/min rather than reasoning about raw values directly.
- Do not binary search directly on `req[i]` without accounting for earlier blockers; the reachable prefix depends on `max(req[0..i])`, not on `req[i]` alone.
- Use 64-bit integers for task totals and `target`; `tasks[i]` is up to `1e9` and `n` is up to `2e5`, so 32-bit accumulation overflows immediately.
- In production systems, this pattern is a reminder to separate expensive state construction from cheap repeated threshold checks; preprocessing often turns an operational bottleneck into a logarithmic control loop.

## 🚀 Variations & Further Practice
- Allow completing groups in any order instead of only as a prefix. The monotonicity remains, but the feasibility check changes from prefix reachability to subset selection or sorting by requirement.
- Add per-group score consumption or score growth after completion. The predicate may stop being monotonic, forcing dynamic programming or greedy proofs instead of binary search.
- Support many target queries against the same `tasks` and `req`. The challenge shifts from single-query optimization to building reusable indices or offline query processing.