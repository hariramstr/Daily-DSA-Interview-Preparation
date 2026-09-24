# Longest Workout Plan Within Heart Rate Drift

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Array

---

## 🗂 Problem Overview
Given an array `heartRate` and an integer `limit`, find the maximum length of a contiguous segment whose internal range stays bounded: `max(segment) - min(segment) <= limit`. The output is a single integer: the longest valid window length. The challenge is maintaining window validity efficiently as the segment expands and contracts. With up to `100000` readings, recomputing min and max for every candidate subarray is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent interval whose variability stays within a tolerance band: telemetry smoothing, anomaly suppression in observability pipelines, market-data stability windows, autoscaling signal qualification, and sensor fusion streams. At scale, the failure mode is obvious: naive rescans of every window turn linear ingestion into quadratic latency. The monotonic-queue sliding-window approach preserves exact min/max under continuous updates, enabling real-time decisioning on high-volume streams without introducing approximation error, expensive tree structures, or batch-only processing constraints.

## 🔍 Problem Statement
You are given an integer array `heartRate` where `heartRate[i]` is the athlete’s heart rate during minute `i`, and an integer `limit`. A contiguous workout segment is stable if the difference between its maximum and minimum heart rate is at most `limit`.

Return the length of the longest stable contiguous segment.

Constraints:

- `1 <= heartRate.length <= 100000`
- `1 <= heartRate[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `heartRate = [120, 123, 121, 126, 124, 122], limit = 4` → `3`
- `heartRate = [98, 100, 101, 99, 102, 100, 99], limit = 3` → `5`

Important edge cases:
- Single-element arrays are always valid.
- `limit = 0` means every valid segment must have identical values.
- Large value ranges rule out counting-based tricks.
- The `100000` length bound forces an `O(n)` or `O(n log n)` solution; `O(n^2)` will not pass.

## 🪜 How to Solve This
1. Read the condition carefully → validity depends on the **current window’s min and max**, not on adjacent differences or averages.

2. We need the **longest contiguous** segment → that strongly suggests a sliding window with two pointers, because once a window becomes invalid, we can try shrinking it from the left instead of restarting.

3. The blocker is obvious: in a moving window, how do we get min and max fast? Recomputing them on every shift would make the algorithm quadratic.

4. That points to **monotonic queues**:
   - one deque keeps candidates for the maximum in decreasing order,
   - one deque keeps candidates for the minimum in increasing order.

5. As we extend the right pointer, we insert the new value while removing dominated elements from the back. The deque fronts always hold the current window’s max and min.

6. If `max - min > limit`, move the left pointer rightward until the window becomes valid again, evicting deque fronts when they fall out of range.

7. Track the largest valid window length seen during this process. Each element enters and leaves each deque at most once, so the whole scan stays linear.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and two monotonic deques.**  
   Use `left = 0`, iterate `right` from `0` to `n - 1`. Maintain:
   - `maxDeque`: decreasing values, so front is current maximum.
   - `minDeque`: increasing values, so front is current minimum.  
   This is the classic **Sliding Window + Monotonic Queue** pattern.

2. **Insert the new right-side value into both deques.**  
   For `maxDeque`, pop from the back while elements are smaller than the new value.  
   For `minDeque`, pop from the back while elements are larger than the new value.  
   This preserves the invariant that each deque remains monotonic and contains only useful candidates.

3. **Check window validity using deque fronts.**  
   The current window `[left, right]` is valid iff  
   `maxDeque.front() - minDeque.front() <= limit`.  
   This is correct because the fronts are exactly the maximum and minimum of the active window.

4. **Shrink from the left while invalid.**  
   If the range exceeds `limit`, increment `left`. Before or after incrementing, remove the outgoing value from deque fronts if it matches them.  
   The invariant after shrinking: both deque fronts still belong to the current window, and the window is valid again.

5. **Update the answer after restoring validity.**  
   Compute `right - left + 1` and maximize the result.  
   This works because for each `right`, the algorithm finds the leftmost valid boundary after necessary shrinking, giving the largest valid window ending at `right`.

6. **Why this abstraction is right.**  
   A heap can give min/max, but deleting stale interior elements is awkward without lazy cleanup. A balanced tree works in `O(log n)`. Monotonic deques exploit the one-directional movement of the window to achieve `O(1)` amortized updates and exact range tracking.

## 📊 Worked Example
Example: `heartRate = [98, 100, 101, 99, 102, 100, 99]`, `limit = 3`

| right | value | maxDeque | minDeque | left | valid? | best |
|---|---:|---|---|---:|---|---:|
| 0 | 98  | [98] | [98] | 0 | yes | 1 |
| 1 | 100 | [100] | [98,100] | 0 | yes | 2 |
| 2 | 101 | [101] | [98,100,101] | 0 | yes | 3 |
| 3 | 99  | [101,99] | [98,99] | 0 | yes | 4 |
| 4 | 102 | [102] | [98,99,102] | 1 | yes after shrink | 4 |
| 5 | 100 | [102,100] | [99,100] | 1 | yes | 5 |
| 6 | 99  | [102,100,99] | [99] | 1 | yes | 6 |

At `right = 4`, range becomes `102 - 98 = 4`, so the window is invalid. Move `left` from `0` to `1`, evict `98` from `minDeque`, and the range becomes `102 - 99 = 3`. The longest valid segment is length `6`: `[100, 101, 99, 102, 100, 99]`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each heart-rate reading is pushed into and popped from each deque at most once. There is no nested rescan of the window; the dominant work is the single left-to-right pass plus amortized deque maintenance. This remains practical at `10^6` elements, while `O(n^2)` becomes completely infeasible far before that, and impossible at `10^9`.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when the window is highly monotonic and many candidates remain active. In practice it is often smaller. Reducing below this while keeping exact online min/max is not realistic without accepting slower updates, such as `O(log n)` tree-based structures.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous subarray under a condition involving the window’s current min and max, think sliding window plus a data structure that supports dynamic extrema.
- If the window only moves forward and stale elements expire from the left, monotonic deques are usually a better fit than heaps or repeated rescans.
- Be precise about eviction: remove from deque fronts only when the outgoing left-side value matches the current front candidate.
- Update the answer only after shrinking until the window is valid; otherwise you will count invalid ranges and overstate the result.
- The production lesson is broader than the puzzle: when constraints depend on rolling extrema, preserving incremental state is the difference between stream-native systems and accidental batch algorithms.

## 🚀 Variations & Further Practice
- Return the actual subarray indices, not just the length. Same core pattern, but now tie-breaking and state retention matter.
- Count how many subarrays satisfy `max - min <= limit`. The twist is converting a longest-window invariant into a counting argument over all valid suffixes.
- Replace the fixed `limit` with per-position thresholds or weighted drift rules. The harder part is that validity may no longer be expressible using only raw min/max, so the monotonic-queue abstraction may need to be generalized or abandoned.