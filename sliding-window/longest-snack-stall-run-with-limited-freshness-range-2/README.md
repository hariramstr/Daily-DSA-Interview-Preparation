# Longest Snack Stall Run with Limited Freshness Range

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Array

---

## 🗂 Problem Overview
Given an array `freshness` and an integer `limit`, find the maximum length of a contiguous subarray whose internal range stays bounded: `max(window) - min(window) <= limit`. The output is a single integer, the longest valid run length. The challenge is not correctness but scale: with up to `200,000` elements, recomputing min and max for every candidate subarray is too expensive, so the solution must maintain window extrema incrementally.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to enforce bounded variability over moving ranges: streaming anomaly detection, SLO window validation, market data smoothing, sensor fleet quality checks, and search/ranking pipelines that reject unstable segments. In production, the failure mode is usually hidden quadratic work: repeatedly rescanning windows for min/max under sustained throughput. At scale, that turns into latency spikes, cache-unfriendly scans, and backpressure in stream processors. The monotonic-queue sliding window pattern enables deterministic linear-time behavior, making it viable for online processing, large in-memory batches, and low-latency services where bounded spread is a first-class constraint.

## 🔍 Problem Statement
You are given an integer array `freshness` where `freshness[i]` is the freshness score of the `i`-th snack stall, and an integer `limit`. A contiguous run `freshness[l..r]` is valid if:

`max(freshness[l..r]) - min(freshness[l..r]) <= limit`

Return the length of the longest valid contiguous run.

Constraints:

- `1 <= freshness.length <= 200000`
- `0 <= freshness[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `freshness = [8, 6, 7, 9, 10, 5], limit = 3` → `4`
- `freshness = [4, 4, 4, 2, 3, 5, 6], limit = 1` → `3`

Edge cases matter: arrays of length `1`, repeated values, `limit = 0`, and large values near `10^9`. The key algorithmic constraint is the input size: any approach that checks many subarrays explicitly or rescans windows for min/max will time out.

## 🪜 How to Solve This
1. Start with the brute-force instinct → for every left boundary, extend right and track min/max. That works logically, but worst-case it touches too many subarrays.

2. Notice the property is about a **contiguous window** and a condition that becomes invalid only when the window gets too wide in value range. That strongly suggests **sliding window / two pointers**.

3. With sliding window, we can expand the right pointer greedily. But we still need the current window’s minimum and maximum at every step.

4. A plain queue is not enough, and a heap is awkward because removing expired elements from the left is expensive or messy.

5. The right abstraction is **two monotonic deques**:
   - one decreasing deque to expose the current maximum,
   - one increasing deque to expose the current minimum.

6. As each new value arrives, remove weaker candidates from the back, append the new index/value, and keep the front as the window extreme.

7. If `max - min > limit`, shrink from the left until valid again, evicting deque fronts when they fall out of the window.

8. Track the largest valid window length seen. Each element enters and leaves each deque once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Initialize window state**  
   Use `left = 0`, `best = 0`, and two deques:
   - `maxDeque`: indices of elements in decreasing `freshness` order.
   - `minDeque`: indices of elements in increasing `freshness` order.  
   This is the classic **Sliding Window + Monotonic Queue** pattern.

2. **Expand the window with `right`**  
   For each `right` from `0` to `n - 1`, insert `freshness[right]` into both deques:
   - Pop from the back of `maxDeque` while the new value is larger.
   - Pop from the back of `minDeque` while the new value is smaller.  
   This preserves the invariant that deque fronts always hold the max/min of the current window.

3. **Check validity using deque fronts**  
   The current window range is:
   `freshness[maxDeque.front] - freshness[minDeque.front]`  
   If it is within `limit`, the window is valid and no shrink is needed.

4. **Shrink until valid**  
   While the range exceeds `limit`, increment `left`. Before or after moving `left`, remove deque fronts whose indices are now `< left`.  
   Correctness comes from shrinking only as much as needed; once valid, any earlier `left` would still violate the constraint.

5. **Update answer**  
   After restoring validity, compute `right - left + 1` and update `best`.  
   Invariant: after each iteration, `[left, right]` is the longest valid window ending at `right`.

6. **Why this abstraction fits**  
   Two pointers give the contiguous search space; monotonic deques give O(1)-amortized access to window extrema. Together they avoid rescanning and keep the solution O(n), which is exactly what the constraints require.

## 📊 Worked Example
Example: `freshness = [8, 6, 7, 9, 10, 5]`, `limit = 3`

| right | value | window after shrink | max | min | valid | best |
|---|---:|---|---:|---:|---|---:|
| 0 | 8  | `[8]` | 8  | 8 | yes | 1 |
| 1 | 6  | `[8, 6]` | 8  | 6 | yes | 2 |
| 2 | 7  | `[8, 6, 7]` | 8  | 6 | yes | 3 |
| 3 | 9  | `[8, 6, 7, 9]` | 9  | 6 | yes | 4 |
| 4 | 10 | shrink to `[7, 9, 10]` | 10 | 7 | yes | 4 |
| 5 | 5  | shrink to `[5]` | 5  | 5 | yes | 4 |

Trace notes:
- At `right = 4`, range becomes `10 - 6 = 4`, so move `left` forward until the min becomes `7`.
- At `right = 5`, adding `5` makes the range too large against `10`, so repeated shrinking collapses the window to just `[5]`.
- The maximum valid length observed is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` amortized. Each index is inserted into and removed from each deque at most once, so the dominant work is linear deque maintenance plus one pass of the two pointers. At `10^6` elements this remains practical; at `10^9`, linear time is still too large for a single machine pass in many environments, but it is the right asymptotic bound.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when the window is long and values are monotonic. In practice it is bounded by window size. You cannot reduce this to `O(1)` while preserving exact online min/max updates without paying extra time.

## 💡 Key Takeaways
- If the problem asks for the longest/shortest **contiguous** segment under a dynamic constraint, start with two pointers before considering heavier structures.
- If a sliding window needs both current **minimum and maximum**, that is a strong signal for **paired monotonic deques**.
- Store **indices**, not just values, so you can evict elements precisely when they leave the window.
- Shrink while `max - min > limit`, then compute window length as `right - left + 1`; updating in the wrong order causes classic off-by-one bugs.
- The production lesson is broader than this problem: when a range constraint must be enforced continuously over a stream, incremental state maintenance beats repeated recomputation by orders of magnitude.

## 🚀 Variations & Further Practice
- **Sliding Window Maximum / Minimum** — same monotonic deque core, but only one extreme is needed; useful for isolating the data-structure pattern before combining both sides.
- **Shortest subarray satisfying a threshold with monotonic structure** — the twist is optimizing for minimum length, which changes when and how aggressively you shrink.
- **Median or percentile-constrained windows** — harder because monotonic queues no longer suffice; you need balanced trees, indexed heaps, or order-statistics structures to maintain richer window summaries.