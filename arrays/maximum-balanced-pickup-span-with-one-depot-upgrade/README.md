# Maximum Balanced Pickup Span with One Depot Upgrade

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** arrays, sliding-window, monotonic-queue

---

## 🗂 Problem Overview
Given an array `capacities`, find the longest contiguous subarray whose spread can be made at most `limit` after optionally increasing exactly one element by at most `upgrade`. The operation can only raise a low outlier; it cannot reduce a high one. The challenge is that `n` is up to `2 * 10^5`, so checking every window or simulating upgrades per subarray is too expensive. The solution needs near-linear window maintenance over changing minima and maxima.

## 🌍 Engineering Impact
This pattern shows up in systems that must validate a rolling segment under bounded skew while allowing one corrective action: shard throughput smoothing, streaming anomaly suppression, autoscaling windows, and rate-limiter bucket normalization. At scale, brute-force range validation collapses under high-cardinality telemetry or long event streams. What matters is maintaining range statistics incrementally while reasoning about whether one exception can be repaired. The same design pressure appears in observability pipelines and traffic engineering: you need exact answers over moving windows without rescanning history, and you need invariants strong enough to support operational decisions in real time.

## 🔍 Problem Statement
You are given:

- `capacities[i]`: hourly pickup capacity of depot `i`
- `limit`: maximum allowed difference between the largest and smallest value in a chosen contiguous subarray
- `upgrade`: maximum amount by which you may increase one element inside that subarray

Return the maximum subarray length such that, after applying at most one increase to one element, the final subarray satisfies:

`max(subarray) - min(subarray) <= limit`

Constraints:

- `1 <= capacities.length <= 2 * 10^5`
- `0 <= capacities[i], limit, upgrade <= 10^9`

Examples:

- `capacities = [5,3,6,4,7], limit = 2, upgrade = 2` → `4`
- `capacities = [8,2,2,2,8], limit = 3, upgrade = 6` → actual optimal answer `3`

The key constraint is array size: anything worse than roughly `O(n log n)` is unlikely to survive production-sized inputs.

## 🪜 How to Solve This
1. Start with the classic version: longest subarray with `max - min <= limit`. That immediately suggests **sliding window + monotonic deques** for window min and max.

2. Now add one upgrade. Since values can only increase, the only thing we can “repair” is the window minimum side. We cannot do anything about an oversized maximum.

3. Ask what a valid repaired window must look like. After one increase, every element except possibly one already has to fit inside some interval of width `limit`. If multiple low elements are too small, one upgrade is not enough.

4. That reframes the problem: for each window, can we discard the effect of at most one low outlier and make the remaining spread fit within `limit`, while also ensuring that outlier can be raised enough using at most `upgrade`?

5. So we need not only the maximum and minimum, but also the **second minimum** and the count of minima. Those tell us whether there is exactly one problematic low element or a whole cluster.

6. Maintain these statistics online as the right pointer expands and the left pointer contracts. Once validity becomes monotone under shrinking, standard two-pointers gives the maximum length.

## 🧩 Algorithm Walkthrough
1. **Use a sliding window `[l..r]`** and grow `r` from left to right. This is the right abstraction because the target is the longest contiguous segment, and validity is restored by moving `l` forward.

2. **Maintain a monotonic increasing deque for minima** and a monotonic decreasing deque for maxima. Each stores indices, not values.  
   - Front of min deque = current minimum  
   - Front of max deque = current maximum  
   These structures give `O(1)` amortized range extrema updates.

3. **Track enough information to test one-upgrade feasibility.** For the current window, let:
   - `mx = current maximum`
   - `mn = current minimum`
   - `mn2 = second distinct minimum`, if it exists
   - `cntMin = number of occurrences of mn`

4. **Window is immediately valid** if `mx - mn <= limit`. No upgrade needed.

5. Otherwise, the only possible repair is to raise one occurrence of the minimum. That is valid iff:
   - `cntMin == 1` — only one lowest outlier exists
   - `mx - mn2 <= limit` — after raising that outlier above or up to the effective floor, the remaining window already fits
   - Required raise `target - mn <= upgrade`, where `target = mx - limit`  
     This is the minimum value the outlier must reach so that final spread is at most `limit`.

6. **If the window is invalid, advance `l`** and evict expired indices from both deques. Continue until the window becomes valid again. This preserves the invariant: current window is the longest valid window ending at `r`.

7. **Record `maxLen = max(maxLen, r - l + 1)`** after each expansion. Each index enters and leaves each deque once, so the total work stays linear.

## 📊 Worked Example
Take `capacities = [5,3,6,4,7]`, `limit = 2`, `upgrade = 2`.

| r | window        | mn / cntMin / mn2 | mx | valid? | action |
|---|---------------|-------------------|----|--------|--------|
| 0 | `[5]`         | `5 / 1 / -`       | 5  | yes    | `ans=1` |
| 1 | `[5,3]`       | `3 / 1 / 5`       | 5  | yes    | `ans=2` |
| 2 | `[5,3,6]`     | `3 / 1 / 5`       | 6  | yes via upgrade: raise `3→4` | `ans=3` |
| 3 | `[5,3,6,4]`   | `3 / 1 / 4`       | 6  | yes via upgrade: raise `3→4` or `5` | `ans=4` |
| 4 | `[5,3,6,4,7]` | `3 / 1 / 4`       | 7  | no: even after removing lone min, `7-4=3>2` | shrink |

After shrinking past `5`, window `[3,6,4,7]` is still invalid; shrinking past `3` gives `[6,4,7]`, which is valid. Final answer remains `4`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each element is pushed and popped at most once from the min deque and once from the max deque, and each pointer only moves forward. At `10^6` elements this is practical; at `10^9`, even linear scan becomes bandwidth-bound and generally infeasible without partitioning or streaming constraints.

### Space Complexity
`O(n)` in the worst case for the deques, though in practice they hold only active window candidates. The space is owned by the monotonic queues. You cannot reduce this to true `O(1)` while preserving exact online range extrema under arbitrary input order.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous range under a max/min constraint, think **sliding window + monotonic deques** before considering trees or heaps.
- If one modification is allowed, ask whether it repairs the high side, the low side, or exactly one outlier; that usually determines which secondary statistic you must track.
- The upgrade can only fix a **single unique minimum**; if the minimum appears multiple times and is too low, the window is unrecoverable.
- Be careful with the target raise: the upgraded value only needs to reach `max - limit`, not necessarily the current maximum.
- In production systems, “one corrective action allowed” often means you should model the normal case exactly and track just enough structure to isolate a single exception, rather than generalizing to full recomputation.

## 🚀 Variations & Further Practice
- Allow **one arbitrary modification up or down** instead of only increase. The twist is symmetry: now either extreme may be repairable, so you need to reason about both second-min and second-max.
- Allow **up to `k` upgrades**. This becomes a much harder window-feasibility problem: you now need counts of all low outliers relative to a moving threshold, not just the unique minimum.
- Replace the balance rule with **median-based deviation** or total adjustment budget. The monotonic-queue range trick no longer suffices; you typically need order-statistics structures or prefix-sum cost models.