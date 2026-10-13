# Minimum Launch Day for Staggered Marketing Campaigns

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Greedy

---

## 🗂 Problem Overview
Given an array `readyDay`, return the earliest day when at least `m` non-overlapping launch groups can be formed, where each group contains exactly `k` consecutive campaigns by index and every campaign in the group must be ready by that day. If no such day exists, return `-1`. The challenge is that readiness evolves over time, adjacency is positional rather than value-based, and `n` is large enough that simulating day-by-day or trying combinations is infeasible.

## 🌍 Engineering Impact
This pattern shows up in rollout orchestration, batch scheduling, manufacturing readiness gates, and streaming pipelines where contiguous capacity segments must be activated only after all dependencies are available. The key production concern is finding the earliest feasible threshold under a monotonic condition without scanning the entire time domain. Without this approach, systems degrade into repeated full recomputation, poor tail latency, or brittle heuristics. Binary search on the answer converts “find the earliest safe activation point” into a scalable decision problem, which is exactly how large systems reason about capacity windows, deployment waves, and SLA-safe cutovers.

## 🔍 Problem Statement
You are given `readyDay[i]`, the day the `i`-th campaign becomes ready. After that day, it remains ready permanently. You must form at least `m` launch groups, each consisting of exactly `k` consecutive campaigns in the original array. A campaign can be used in at most one group.

Return the minimum day when this is possible, or `-1` if impossible.

Constraints:
- `1 <= n <= 200000`
- `1 <= readyDay[i] <= 1000000000`
- `1 <= m <= 100000`
- `1 <= k <= n`
- `n == readyDay.length`

Immediate impossibility check: if `m * k > n`, there are not enough campaigns even if all are ready.

Examples:
- `readyDay = [5, 2, 4, 6, 3, 7], m = 2, k = 2` → `6`
- `readyDay = [1, 10, 3, 10, 2], m = 3, k = 1` → `3`

The algorithmic driver is the input size: `n` is large, and day values are huge, so the solution must avoid brute force over days or groupings.

## 🪜 How to Solve This
1. Read the requirement carefully → we are not asked to construct the groups, only to find the minimum day when enough groups become possible.

2. Notice the hidden search space → the answer is a day, not an index. Candidate days range from `min(readyDay)` to `max(readyDay)`.

3. Ask the key question: if day `D` works, does any later day also work? → yes. More campaigns become ready over time, never fewer. That gives a monotonic feasibility condition.

4. Monotonic answer space usually means binary search on the answer → instead of testing every day, search the smallest feasible `D`.

5. Now define the feasibility check for a fixed day `D` → scan left to right, count consecutive campaigns with `readyDay[i] <= D`, and every time the streak reaches `k`, form one group and reset the streak because groups cannot overlap.

6. If the scan can form at least `m` groups, `D` is feasible; otherwise it is not.

7. Add the early guard `m * k > n` → this prevents wasted work and avoids false hope on impossible inputs.

This is the standard pattern: binary search over a monotonic threshold, with a linear greedy validator.

## 🧩 Algorithm Walkthrough
1. **Check structural impossibility.**  
   If `m * k > n`, return `-1` immediately. This is correct because even with every campaign ready, non-overlapping groups of size `k` need `m * k` total campaigns. The invariant is simple: no algorithm can overcome insufficient cardinality.

2. **Define the search bounds.**  
   Let `low = min(readyDay)` and `high = max(readyDay)`. The answer, if it exists, must lie in this closed interval. This is the classic **Binary Search on Answer** pattern: search a value domain, not an array index.

3. **Implement a feasibility function `canMake(day)`.**  
   Scan the array once. If `readyDay[i] <= day`, extend the current consecutive-ready streak. Otherwise reset the streak to zero. Whenever the streak reaches `k`, increment the group count and reset the streak to zero to enforce non-overlap.  
   Why greedy reset is correct: using the earliest available `k` campaigns in a ready streak never reduces the number of groups obtainable from that streak; a streak of length `L` yields exactly `floor(L / k)` groups.

4. **Binary search for the minimum feasible day.**  
   Compute `mid`. If `canMake(mid)` is true, record that feasibility exists at `mid` and move `high = mid - 1` to search for an earlier day. Otherwise move `low = mid + 1`.

5. **Return the leftmost feasible day.**  
   The maintained invariant is: all days below `low` are known infeasible, and any recorded feasible answer is a valid upper bound. When the loop ends, `low` is the minimum feasible day.

This combination of **Binary Search + Greedy Linear Check** is the right abstraction because the state transition over days is monotonic, while the local grouping decision is optimal with a single pass.

## 📊 Worked Example
Example: `readyDay = [5, 2, 4, 6, 3, 7], m = 2, k = 2`

Binary search over days `2..7`.

| Day | Ready mask (`<= day`)         | Greedy groups formed | Feasible? |
|-----|-------------------------------|----------------------|-----------|
| 4   | `[F, T, T, F, T, F]`          | 1 from `[1,2]`       | No        |
| 5   | `[T, T, T, F, T, F]`          | 1 from `[0,1]`       | No        |
| 6   | `[T, T, T, T, T, F]`          | 2 from `[0,1]`, `[2,3]` | Yes   |

Trace for `day = 6`:
1. Index 0 ready → streak = 1  
2. Index 1 ready → streak = 2 → form group 1, reset streak = 0  
3. Index 2 ready → streak = 1  
4. Index 3 ready → streak = 2 → form group 2, reset streak = 0  
5. We already have `m = 2`, so day 6 works.

Since day 5 fails and day 6 succeeds, the minimum launch day is `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log R)`, where `R = max(readyDay) - min(readyDay) + 1`. Each binary-search step runs one linear feasibility scan over `n` campaigns. In practice, `log R` is at most about 31 for 32-bit day values, so this scales well even when `n` approaches `10^6`; a brute-force day scan over a `10^9` range would be completely intractable.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few counters and search bounds; no extra arrays or heaps are required. Space cannot be meaningfully reduced below constant, and increasing it does not improve asymptotic runtime for this formulation.

## 💡 Key Takeaways
- If the question asks for the *minimum threshold* where a condition becomes true, check whether feasibility is monotonic; that is the strongest signal for binary search on the answer.
- If validation for a candidate threshold can be done in one pass with local decisions, pair binary search with a greedy checker instead of searching combinatorial groupings.
- The impossibility guard is `m * k > n`, and it should be computed in a wide enough integer type to avoid overflow in languages with fixed-width integers.
- Reset the consecutive-ready counter after forming a group; otherwise you accidentally allow overlap and overcount groups inside the same streak.
- In production systems, threshold search plus a monotonic validator is a reusable design: it turns expensive optimization into repeated cheap feasibility checks with predictable scaling.

## 🚀 Variations & Further Practice
- Allow each group to contain campaigns within a sliding window of size `k`, not necessarily all ready on the same day; the twist is that feasibility is no longer a simple consecutive streak count.
- Minimize the maximum readiness gap inside each chosen group instead of the launch day; this shifts the monotonic predicate from absolute thresholding to interval quality.
- Support dynamic updates to `readyDay` with repeated queries for different `m` and `k`; the harder part is maintaining feasibility efficiently under online changes rather than rescanning the full array each time.