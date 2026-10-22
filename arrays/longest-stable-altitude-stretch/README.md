# Longest Stable Altitude Stretch

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Monotonic Queue

---

## 🗂 Problem Overview
Given an integer array `altitudes` and an integer `limit`, find the maximum length of a contiguous segment where the difference between the segment’s maximum and minimum altitude is at most `limit`. The output is a single integer: the longest valid subarray length. The challenge is maintaining window min/max efficiently as the window expands and contracts; checking every subarray is quadratic and fails at `10^5` elements.

## 🌍 Engineering Impact
This pattern shows up in telemetry pipelines, anomaly detection, market data smoothing, sensor fusion, and autoscaling control loops: “find the longest recent interval where variation stayed bounded.” In production, the issue is rarely the predicate itself; it is maintaining rolling extrema under strict latency and memory budgets. Without the right structure, teams fall back to rescanning windows or rebuilding heaps, which collapses under high-throughput streams. A monotonic-queue sliding window gives deterministic linear behavior, predictable cache-friendly access, and a clean foundation for online processing where each event is consumed once and expired once.

## 🔍 Problem Statement
You are given an integer array `altitudes` where `altitudes[i]` is the drone’s altitude at second `i`, and an integer `limit`. A contiguous segment is stable if:

`max(segment) - min(segment) <= limit`

Return the length of the longest stable contiguous segment.

Constraints:

- `1 <= altitudes.length <= 100000`
- `-1000000000 <= altitudes[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `altitudes = [30, 32, 31, 35, 34, 33], limit = 3` → `3`
- `altitudes = [100, 100, 100, 100], limit = 0` → `4`

Important edge cases:

- Negative values are valid.
- `limit = 0` means all values in the segment must be identical.
- The segment must be contiguous; sorting is irrelevant because it destroys order.
- The key constraint is `n = 100000`, which rules out brute-force enumeration of subarrays and pushes toward `O(n log n)` or `O(n)`.

## 🪜 How to Solve This
1. Start from the condition itself: every valid segment depends only on its current minimum and maximum. That immediately suggests a sliding window, because if a window is valid, extending it may break validity, and shrinking from the left may restore it.

2. A plain sliding window is not enough unless min and max are cheap to query. Recomputing them after every move would turn the solution back into quadratic behavior.

3. So ask: what data structure supports:
   - append on the right,
   - remove expired elements from the left,
   - read current min and max in `O(1)`?

4. The answer is two monotonic deques:
   - one decreasing deque for the current maximum,
   - one increasing deque for the current minimum.

5. As each new altitude enters, remove weaker candidates from the back of each deque. As the left boundary advances, remove indices that fall out of the window from the front.

6. Now the window validity check becomes trivial: `altitudes[maxDeque.front] - altitudes[minDeque.front] <= limit`.

7. Expand right once per element, shrink left only when necessary, and track the best window length. Each index enters and leaves each deque at most once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window + Monotonic Queue pattern.**  
   Maintain a window `[left, right]` over the array. This is the right abstraction because the validity condition is local to a contiguous range and can be restored by moving `left` forward.

2. **Track candidate maxima with a decreasing deque of indices.**  
   Before inserting `right`, pop from the back while `altitudes[back] < altitudes[right]`. Those smaller values can never become the maximum while the new value remains in the window. The front always holds the index of the current maximum.

3. **Track candidate minima with an increasing deque of indices.**  
   Before inserting `right`, pop from the back while `altitudes[back] > altitudes[right]`. Those larger values are dominated for future minimum queries. The front always holds the current minimum.

4. **Expand the window by adding `right` to both deques.**  
   At this point, both deques contain only indices inside the current suffix ending at `right`, ordered so their fronts are extremal values.

5. **Restore validity by shrinking from the left while needed.**  
   While `altitudes[maxDeque.front] - altitudes[minDeque.front] > limit`, increment `left`. If either deque’s front index is now left of the window, pop it.  
   Invariant: after this loop, `[left, right]` is the longest valid window ending at `right`.

6. **Update the answer with `right - left + 1`.**  
   This is correct because every valid window ending at `right` and starting before `left` is impossible by construction; we already shrank just enough to restore validity.

7. **Why linear time holds.**  
   Each index is pushed once into each deque and popped at most once from each end. No index is revisited, so total work is `O(n)`.

## 📊 Worked Example
Example: `altitudes = [30, 32, 31, 35, 34, 33]`, `limit = 3`

| right | value | maxDeque values | minDeque values | left | valid? | best |
|---|---:|---|---|---:|---|---:|
| 0 | 30 | [30] | [30] | 0 | yes | 1 |
| 1 | 32 | [32] | [30, 32] | 0 | yes (`32-30=2`) | 2 |
| 2 | 31 | [32, 31] | [30, 31] | 0 | yes (`32-30=2`) | 3 |
| 3 | 35 | [35] | [30, 31, 35] | 0 | no (`35-30=5`) | 3 |
|   |    | [35] | [31, 35] | 1 | no (`35-31=4`) | 3 |
|   |    | [35] | [35] | 3 | yes | 3 |
| 4 | 34 | [35, 34] | [34] | 3 | yes (`35-34=1`) | 3 |
| 5 | 33 | [35, 34, 33] | [33] | 3 | yes (`35-33=2`) | 3 |

Longest stable stretch length is `3`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each altitude index is inserted into the max deque once and removed once; the same is true for the min deque. The dominant cost is these amortized constant-time deque operations. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally better than any rescanning approach.

### Space Complexity
`O(n)` in the worst case, owned by the two deques storing candidate indices. In many inputs the live window is smaller, but asymptotically the bound remains linear. You cannot reduce this to constant space without giving up efficient dynamic min/max maintenance.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous range satisfying a bound on `max - min`, think sliding window immediately.
- If a sliding window needs fast rolling extrema under insert-right/remove-left semantics, monotonic deques are the signal.
- Store indices, not just values; expiration is based on window position, not equality of readings.
- Shrink while the window is invalid, then compute `right - left + 1`; updating too early causes off-by-one errors.
- In production stream processing, monotonic queues are the right tool when you need exact rolling bounds with predictable linear behavior and no heap cleanup overhead.

## 🚀 Variations & Further Practice
- Return the actual `[l, r]` interval, not just its length; the twist is handling ties deterministically while preserving the same invariants.
- Solve the online streaming version where readings arrive continuously and you must emit the current longest valid suffix; the twist is incremental processing without batch rescans.
- Generalize from `max - min <= limit` to window median or percentile constraints; the harder part is that monotonic queues no longer suffice, pushing you toward balanced trees or indexed heaps.