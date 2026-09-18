# Longest Moderation Queue With Bounded Toxicity Spread

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Deque

---

## 🗂 Problem Overview
Given an array `scores` and an integer `limit`, find the maximum length of a contiguous subarray whose internal spread stays bounded: `max(window) - min(window) <= limit`. The output is a single integer: the longest valid window length. The challenge is that the window is variable-sized and can start anywhere, while `scores.length` can reach `200000`, making repeated min/max recomputation across candidate windows too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must maintain a rolling segment under bounded variance: moderation pipelines, streaming anomaly detection, market data smoothing, search-ranking freshness windows, and autoscaling signals. At scale, the difference between recomputing extrema per window and maintaining them incrementally is the difference between linear throughput and collapse under bursty traffic. Monotonic queues enable exact online decisions with predictable latency, which matters in event-driven systems, real-time dashboards, and ingestion services where backpressure, tail latency, and memory growth are architectural concerns rather than implementation details.

## 🔍 Problem Statement
You are given an integer array `scores`, where `scores[i]` is the toxicity score of the `i`-th comment in chronological order, and an integer `limit`. A contiguous block is reviewable if the difference between its maximum and minimum score is at most `limit`.

Return the maximum possible length of such a contiguous block.

Formally, find the largest `(r - l + 1)` such that:

- `0 <= l <= r < scores.length`
- `max(scores[l..r]) - min(scores[l..r]) <= limit`

Constraints:

- `1 <= scores.length <= 200000`
- `0 <= scores[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `scores = [4, 7, 5, 6, 8, 3, 4], limit = 3` → `4`
- `scores = [10, 10, 10, 1, 2, 3, 4], limit = 2` → `3`

The key constraint is input size: any approach that scans a window to recompute min or max during left/right movement will time out.

## 🪜 How to Solve This
1. Read the requirement → we need the **longest contiguous window** satisfying a condition. That is a sliding-window signal.
2. Check whether the validity condition is monotonic under expansion/shrinking → adding a new score can only worsen the max/min spread, while removing from the left can restore validity. That means two pointers can work.
3. Ask what makes validity expensive → for each window, we need fast access to both current minimum and maximum.
4. A heap is tempting, but deletions from the left become awkward or require lazy cleanup. We want something tighter.
5. Monotonic deques solve exactly this:  
   → one deque keeps candidates for the maximum in decreasing order  
   → one deque keeps candidates for the minimum in increasing order
6. As the right pointer advances, push the new value into both deques while removing dominated values from the back.
7. If `max - min > limit`, move the left pointer right until the window is valid again, evicting deque fronts when they fall out of range.
8. Track the largest valid window seen. Each element enters and leaves each deque once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use two pointers: `l = 0`, and iterate `r` from `0` to `n - 1`. Maintain two deques of indices:  
   - `maxDeque`: scores in decreasing order  
   - `minDeque`: scores in increasing order  
   This is the **Sliding Window + Monotonic Queue** pattern.

2. **Extend the window to the right**  
   For index `r`, remove indices from the back of `maxDeque` while their scores are `< scores[r]`, then append `r`. Do the symmetric operation for `minDeque`, removing from the back while scores are `> scores[r]`.  
   Why correct: any removed index is dominated by `r` and can never become the max/min of a future window containing `r`.

3. **Check validity using deque fronts**  
   The current maximum is `scores[maxDeque[0]]`; the current minimum is `scores[minDeque[0]]`. If their difference exceeds `limit`, the window is invalid.

4. **Shrink from the left until valid**  
   While invalid, increment `l`. Before or after incrementing, remove deque fronts equal to the old `l`, because those indices have left the window.  
   Invariant: after shrinking, both deque fronts always lie within `[l, r]`, and their values are the true max/min of the current window.

5. **Update the answer**  
   Once valid, compute `r - l + 1` and update the best length.  
   Why this works: for each `r`, the algorithm finds the leftmost valid boundary after necessary shrinking, so every maximal valid window ending at `r` is considered.

## 📊 Worked Example
Using `scores = [4, 7, 5, 6, 8, 3, 4]`, `limit = 3`.

| r | score | l after shrink | maxDeque front | minDeque front | valid window | best |
|---|------:|---------------:|---------------:|---------------:|--------------|-----:|
| 0 | 4 | 0 | 4 | 4 | `[4]` | 1 |
| 1 | 7 | 0 | 7 | 4 | `[4,7]` spread 3 | 2 |
| 2 | 5 | 0 | 7 | 4 | `[4,7,5]` spread 3 | 3 |
| 3 | 6 | 0 | 7 | 4 | `[4,7,5,6]` spread 3 | 4 |
| 4 | 8 | 2 | 8 | 5 | `[5,6,8]` spread 3 | 4 |
| 5 | 3 | 5 | 3 | 3 | `[3]` | 4 |
| 6 | 4 | 5 | 4 | 3 | `[3,4]` spread 1 | 4 |

At `r = 4`, adding `8` makes spread `8 - 4 = 4`, so the window must shrink. Advancing `l` to `2` removes `4` and then `7`, restoring validity.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each index is appended to and removed from each deque at most once, and both pointers move only forward. There is no nested rescanning of the window. At `10^6` elements this remains practical; at `10^9`, linear work is still enormous, but the algorithm is asymptotically optimal.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when the window grows large and values remain monotonic. In practice it is bounded by window size. Reducing below this generally requires sacrificing exactness or accepting slower min/max maintenance.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous range under a constraint involving both current minimum and maximum, think sliding window plus auxiliary extrema maintenance.
- If sorting would destroy chronology and heaps make left-edge eviction awkward, monotonic deques are usually the right abstraction.
- Store indices, not just values; you must know when an extremum candidate falls out of the window.
- Be precise about the shrink condition: invalidate on `max - min > limit`, not `>= limit`, or you will reject exact-boundary windows.
- The transferable design lesson is incremental state maintenance: preserve just enough ordered structure to answer window validity in constant time as the stream advances.

## 🚀 Variations & Further Practice
- Return the actual longest subarray boundaries, or all maximal valid windows, which adds result-shaping without changing the core deque invariant.
- Replace `max - min <= limit` with a median- or percentile-based constraint; the sliding window remains, but monotonic queues no longer suffice and you need balanced trees or indexed heaps.
- Process an unbounded event stream with time-based expiration instead of index-based windows; same pattern, but eviction is driven by timestamps and operational memory limits matter more.