# Minimum Playback Rate for Training Videos

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Math

---

## 🗂 Problem Overview
Given an array `videos`, where each value is a video length in minutes, and an integer `h`, the total available hours, find the minimum integer playback rate `r` such that all videos finish within `h` hours. Each video consumes `ceil(videos[i] / r)` hours because any partial hour still occupies a full scheduling block. The challenge is that video lengths can be very large, so scanning every possible rate is too expensive; the solution depends on the monotonic relationship between rate and total required hours.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must find the minimum capacity that satisfies a deadline or SLO. Examples include provisioning worker throughput for batch pipelines, sizing API rate limits to clear backlogs, choosing replication bandwidth for data migration windows, and tuning ingestion capacity in streaming systems. At scale, brute-force capacity scans waste compute and delay planning loops. Monotonic-search framing turns “try every configuration” into “search the feasible boundary,” which is exactly how production schedulers, autoscaling controllers, and admission-control systems make bounded decisions under large numeric ranges.

## 🔍 Problem Statement
You are given:

- `videos[i]`: length of the `i`-th training video in minutes
- `h`: total number of hours available

Choose a single integer playback rate `r` in minutes per hour, applied uniformly to every video. A video of length `x` requires `ceil(x / r)` hours, since any partially used final hour still counts as a full scheduled hour. Videos are watched one by one in order.

Return the minimum integer rate `r` such that the sum of required hours across all videos is at most `h`. If `h < videos.length`, return `-1`, because even an arbitrarily large rate cannot reduce any video below one hour.

Constraints:

- `1 <= videos.length <= 100000`
- `1 <= videos[i] <= 1000000000`
- `1 <= h <= 1000000000`

Examples:

- `videos = [90, 120, 75], h = 6` → `60`
- `videos = [30, 11, 23, 4, 20], h = 5` → `30`

The key algorithmic pressure is the large search space for possible rates.

## 🪜 How to Solve This
1. Start from the scheduling formula: for a fixed rate `r`, total time is  
   `ceil(v1 / r) + ceil(v2 / r) + ... + ceil(vn / r)`.

2. Notice the critical property: as `r` increases, required hours never increase. They either stay the same or decrease. That means feasibility is monotonic.

3. Monotonic feasibility immediately suggests binary search, not over array indices, but over the answer space: possible playback rates.

4. Define the search bounds:
   - Lowest possible rate is `1`
   - Highest useful rate is `max(videos)`, because any higher rate still makes each video take at least one hour, so nothing improves beyond that

5. For each candidate rate `mid`, compute total required hours.
   - If total hours `<= h`, `mid` is feasible, so try smaller rates
   - Otherwise, `mid` is too slow, so search higher rates

6. Handle the impossible case early: if `h < number of videos`, return `-1`.

This is the standard “minimum feasible value under a monotonic predicate” pattern.

## 🧩 Algorithm Walkthrough
1. **Check impossibility upfront**  
   If `h < videos.length`, return `-1`. Even with infinite playback rate, each video still consumes at least one hour block due to the ceiling rule. This establishes a hard lower bound on total hours.

2. **Set binary search bounds**  
   Use `left = 1` and `right = max(videos)`.  
   Why `max(videos)`? At that rate, every video fits within one hour, so total time becomes exactly `videos.length`, the minimum achievable total. The invariant is that the answer, if it exists, lies within `[left, right]`.

3. **Pick a candidate rate**  
   Compute `mid = left + (right - left) / 2` to avoid overflow in fixed-width integer languages. This is the current playback rate being tested.

4. **Evaluate feasibility at `mid`**  
   Sum `ceil(videos[i] / mid)` across all videos. Use integer math:  
   `ceil(x / r) = (x + r - 1) / r`.  
   This preserves correctness without floating-point error. The invariant here is that the computed total exactly matches the scheduling semantics.

5. **Shrink the search space**  
   - If total hours `<= h`, `mid` is feasible. Record it implicitly by moving `right = mid`; the minimum feasible rate is at `mid` or lower.
   - If total hours `> h`, `mid` is infeasible. Move `left = mid + 1`.

6. **Terminate when bounds converge**  
   When `left == right`, you have the smallest feasible rate. This works because binary search maintains the boundary between infeasible and feasible rates.

This is a classic **binary search on answer space** problem: the abstraction fits because feasibility is monotonic over integer rates.

## 📊 Worked Example
Take `videos = [90, 120, 75]`, `h = 6`.

| Step | left | right | mid | Hours at mid | Feasible? |
|---|---:|---:|---:|---:|---|
| 1 | 1 | 120 | 60 | `2 + 2 + 2 = 6` | Yes |
| 2 | 1 | 60 | 30 | `3 + 4 + 3 = 10` | No |
| 3 | 31 | 60 | 45 | `2 + 3 + 2 = 7` | No |
| 4 | 46 | 60 | 53 | `2 + 3 + 2 = 7` | No |
| 5 | 54 | 60 | 57 | `2 + 3 + 2 = 7` | No |
| 6 | 58 | 60 | 59 | `2 + 3 + 2 = 7` | No |

Now `left = 60`, `right = 60`, so stop.  
Rate `60` is the minimum feasible playback rate. The trace shows the boundary behavior clearly: all rates below `60` are infeasible, and `60` is the first rate that satisfies the deadline.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n` is the number of videos and `M = max(videos)`. Each binary-search step scans the array once to compute required hours, and there are `log M` such steps. This scales well even when `M` is `10^9`; the logarithmic factor stays small, while the array scan remains the dominant cost.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few scalar variables for bounds and the running hour total; no extra data structures proportional to input size are required. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for the **minimum value** that satisfies a constraint, check whether feasibility becomes monotonic as the candidate value increases.
- When the search domain is a numeric range rather than array positions, think **binary search on the answer**.
- The impossible case here is easy to miss: if `h < videos.length`, no rate works because each video costs at least one hour.
- Use integer ceiling safely as `(x + r - 1) / r`; floating-point division invites precision bugs and unnecessary overhead.
- At scale, this pattern is really about finding the smallest capacity that meets a deadline without exhaustively simulating every provisioning level.

## 🚀 Variations & Further Practice
- **Weighted processing windows:** each item has a different per-hour cost or priority, so feasibility is still monotonic but the validation function becomes more complex.
- **Continuous-rate optimization:** allow non-integer rates and ask for a precision threshold; the twist is switching from integer binary search to real-valued search with epsilon handling.
- **Multi-resource deadline fitting:** each job consumes two constrained resources, making feasibility no longer one-dimensional and breaking simple binary search on a single answer space.