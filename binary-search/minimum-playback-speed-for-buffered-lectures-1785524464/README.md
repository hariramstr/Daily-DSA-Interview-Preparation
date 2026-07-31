# Minimum Playback Speed for Buffered Lectures

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Math

---

## 🗂 Problem Overview
Given lecture lengths in minutes and a fixed study window of `H` hours, compute the smallest positive integer playback speed `s` that allows all lectures to finish within `H * 60` minutes. At speed `s`, each lecture costs `ceil(length / s)` minutes, so time decreases as speed increases, but only in discrete steps. The challenge is recognizing that this creates a monotonic feasibility condition over a large answer space, making brute-force search unacceptable.

## 🌍 Engineering Impact
This pattern shows up anywhere you need the minimum capacity that satisfies a deadline under monotonic behavior: provisioning worker throughput for batch pipelines, sizing rate limits for API gateways, selecting shard fanout in search systems, or tuning concurrency in media transcoding queues. At scale, linear probing over candidate capacities is operationally useless because the search space can be enormous while feasibility checks are relatively cheap. Binary search over the answer converts “find the smallest safe configuration” into a predictable, bounded decision procedure. That matters in production because capacity planning, autoscaling thresholds, and SLA enforcement often depend on exactly this shape of problem.

## 🔍 Problem Statement
You are given an integer array `lectures` where `lectures[i]` is the length of the `i`th lecture in minutes, and an integer `H` representing available hours before the exam. All lectures must be watched in order using one shared integer playback speed `s > 0`.

At speed `s`, a lecture of length `x` consumes `ceil(x / s)` minutes. Total watch time is therefore:

`sum(ceil(lectures[i] / s))`

Return the minimum integer `s` such that total time is at most `H * 60`. If no integer speed can satisfy the deadline, return `-1`.

Key constraints:
- `1 <= lectures.length <= 100000`
- `1 <= lectures[i] <= 10^9`
- `1 <= H <= 10^9` in the stated constraints, though the examples include `H = 0`
- Each lecture costs at least 1 minute, even at arbitrarily large speed

Examples:
- `lectures = [30, 11, 23, 4, 20], H = 1` → `2`
- `lectures = [100, 200, 300], H = 0` → `-1`

The large value range rules out scanning speeds one by one.

## 🪜 How to Solve This
1. Read the cost formula carefully → `ceil(x / s)` means faster speed never increases total time. That is the key signal: feasibility is monotonic.

2. Define the decision question first → “If speed is `s`, can I finish within `H * 60` minutes?” This is much easier than directly computing the minimum valid speed.

3. Notice the shape of answers:
   - If some speed `s` works, then every larger speed also works.
   - If some speed `s` fails, then every smaller speed also fails.

4. Monotonic yes/no over integers → binary search on the answer space.

5. Establish bounds:
   - Lower bound is `1`.
   - Upper bound can be `max(lectures)`, because at that speed every lecture takes at most 1 minute.

6. Handle impossibility up front → if `lectures.length > H * 60`, return `-1`, since each lecture costs at least one minute regardless of speed.

7. For each midpoint, compute total required minutes using integer ceiling arithmetic. If it fits, try smaller speeds; otherwise, go larger.

That is the whole mental model: convert optimization into repeated feasibility checks over a monotonic domain.

## 🧩 Algorithm Walkthrough
1. **Convert hours to minutes.**  
   Compute `limit = H * 60`. This is the actual budget used by the feasibility test. Use a wide integer type because `H` can be large, and multiplication must not overflow.

2. **Apply the impossibility guard.**  
   If `lectures.length > limit`, return `-1`. This is correct because `ceil(x / s) >= 1` for every positive lecture length and every positive integer speed. The invariant is simple: no solution exists if the minimum possible total time already exceeds the budget.

3. **Choose binary-search bounds.**  
   Set `lo = 1`, `hi = max(lectures)`. This is sufficient because at `max(lectures)`, every lecture takes exactly 1 minute, so total time is `lectures.length`, the theoretical minimum.

4. **Run binary search on speed.**  
   This is the classic **binary search on answer** pattern. At each step, let `mid = lo + (hi - lo) / 2` and evaluate whether speed `mid` is feasible.

5. **Evaluate feasibility in one pass.**  
   Sum `ceil(x / mid)` across all lectures using integer math: `(x + mid - 1) / mid`. If the running total exceeds `limit`, stop early; the exact remainder no longer matters. The invariant is that the accumulated sum equals the total planner minutes required for the processed prefix.

6. **Shrink the search space.**  
   If `mid` is feasible, record it as a candidate and move left: `hi = mid - 1`. Otherwise move right: `lo = mid + 1`. This preserves the invariant that all speeds below `lo` are known invalid and all recorded candidates are valid.

7. **Return the minimum valid speed.**  
   When the loop ends, `lo` is the smallest feasible speed if a solution exists. Because feasibility is monotonic, binary search is both correct and optimal for this constraint profile.

## 📊 Worked Example
Example: `lectures = [30, 11, 23, 4, 20]`, `H = 1`  
`limit = 60`, `lo = 1`, `hi = 30`

| Step | mid | Required minutes | Feasible? | Next range |
|---|---:|---:|---|---|
| 1 | 15 | `2+1+2+1+2 = 8` | Yes | `1..14` |
| 2 | 7  | `5+2+4+1+3 = 15` | Yes | `1..6` |
| 3 | 3  | `10+4+8+2+7 = 31` | Yes | `1..2` |
| 4 | 1  | `30+11+23+4+20 = 88` | No | `2..2` |
| 5 | 2  | `15+6+12+2+10 = 45` | Yes | done |

Binary search works because the predicate “total minutes `<= 60`” flips only once: speed `1` fails, speed `2` works, and every larger speed also works. The minimum feasible speed is therefore `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = lectures.length` and `M = max(lectures)`. Each binary-search step scans the array once to evaluate feasibility, and there are `log M` such steps. For `n = 10^6`, this is still practical; for `M` near `10^9`, `log2(M)` is only about 30.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only scalar bounds, the running total, and the current midpoint. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off is using wider integer types to avoid overflow.

## 💡 Key Takeaways
- If the problem asks for the minimum integer parameter that makes a condition true, check whether the condition becomes permanently true after some threshold.
- When a direct optimization target is awkward, rewrite it as a yes/no feasibility function and search the answer space instead.
- The impossibility check here is structural, not incidental: each lecture costs at least one minute, so `lectures.length > H * 60` immediately rules out any solution.
- Use integer ceiling safely as `(x + s - 1) / s`, and use wide arithmetic for both `H * 60` and the accumulated total.
- In production systems, this is the core pattern behind finding the smallest capacity that satisfies an SLA without exhaustively testing every configuration.

## 🚀 Variations & Further Practice
- Allow per-lecture playback speeds with a global penalty budget for speed changes. The monotonic predicate may still exist, but the feasibility check becomes dynamic programming instead of a single pass.
- Replace the per-lecture ceiling rule with contiguous batching or partitioning constraints. This shifts the problem toward “split array largest sum” style binary search, where the predicate depends on grouping decisions.
- Optimize for fractional speeds with precision requirements. The monotonic structure remains, but integer binary search becomes floating-point search with termination and numerical-stability concerns.