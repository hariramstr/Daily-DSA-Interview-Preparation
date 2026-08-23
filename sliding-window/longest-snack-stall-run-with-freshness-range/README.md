# Longest Snack Stall Run With Freshness Range

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Array

---

## 🗂 Problem Overview
Given an array `freshness` and an integer `limit`, find the longest contiguous subarray whose maximum and minimum values differ by at most `limit`. The output is a single integer: the maximum valid window length. The challenge is maintaining the current window’s min and max efficiently as the window moves. With up to `200000` elements, recomputing extrema for every candidate subarray is too slow, so the solution must update them incrementally in near-constant time.

## 🌍 Engineering Impact
This pattern shows up anywhere systems enforce bounded variation over a moving range: streaming anomaly detection, market data smoothing, QoS enforcement, sensor fleet monitoring, and ranking pipelines that reject unstable batches. In production, the issue is rarely “find a subarray” and usually “maintain validity over a rolling window under high throughput.” Without monotonic structures, teams fall back to rescanning windows or using heavier balanced trees, which increases latency, memory churn, and implementation complexity. The monotonic-queue approach enables linear-time processing, predictable performance, and simpler reasoning about correctness under sustained load.

## 🔍 Problem Statement
You are given an integer array `freshness` where `freshness[i]` is the freshness score of the `i`-th snack stall, and an integer `limit`. A contiguous run of stalls is valid if:

`max(freshness[l..r]) - min(freshness[l..r]) <= limit`

Return the maximum length of any valid contiguous subarray.

Constraints:
- `1 <= freshness.length <= 200000`
- `0 <= freshness[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `freshness = [4, 7, 6, 8, 5, 9], limit = 3` → `4`
- `freshness = [10, 1, 2, 4, 7, 2], limit = 5` → `4`

Edge cases matter:
- Single-element arrays are always valid.
- `limit = 0` means all values in the window must be identical.
- Large values require comparisons that remain correct without overflow assumptions.

The key constraint is array size: `O(n^2)` enumeration is not viable.

## 🪜 How to Solve This
1. Read the condition carefully → validity depends only on the current window’s **minimum** and **maximum**.
2. That immediately suggests a **sliding window**: expand the right boundary, and if the window becomes invalid, move the left boundary until it is valid again.
3. The blocker is obvious: after each move, how do we know the window’s min and max without rescanning the whole window?
4. A heap can give extrema, but removing stale elements gets awkward. A balanced tree works, but it is `O(log n)` per update and heavier than necessary.
5. The right abstraction is a pair of **monotonic queues**:
   - one decreasing queue for the current maximum,
   - one increasing queue for the current minimum.
6. As each new value enters, discard worse candidates from the back because they can never become the max or min while the new value remains in the window.
7. If `max - min > limit`, advance the left pointer and evict indices that fell out of the window.
8. After every adjustment, the window is the longest valid one ending at `right`, so update the answer.

Once seen, the structure is straightforward: dynamic window + constant-time extrema maintenance.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and two deques.**  
   Use `left = 0`, iterate `right` from `0` to `n - 1`. Maintain:
   - a decreasing deque of indices whose values are candidates for the window maximum,
   - an increasing deque of indices whose values are candidates for the window minimum.  
   This is the classic **Sliding Window + Monotonic Queue** pattern.

2. **Insert the new element at `right`.**  
   For the max deque, pop from the back while `freshness[back] < freshness[right]`.  
   For the min deque, pop from the back while `freshness[back] > freshness[right]`.  
   Then append `right` to both.  
   Why correct: any removed index is dominated by the new value and can never become the relevant extreme before it leaves the window.

3. **Check window validity.**  
   The current maximum is `freshness[maxDeque.front]`; the current minimum is `freshness[minDeque.front]`.  
   If their difference exceeds `limit`, the window is invalid.

4. **Shrink from the left until valid.**  
   While invalid, increment `left`. Before or after incrementing, remove deque fronts equal to the old left index, since those elements are no longer inside the window.  
   Invariant: both deque fronts always point to indices within `[left, right]`.

5. **Record the best answer.**  
   Once valid, `right - left + 1` is the longest valid window ending at `right`, because `left` was advanced only as much as necessary. Update the global maximum.

6. **Why this is optimal.**  
   Each index is pushed and popped from each deque at most once. That gives linear total work while preserving exact min/max for every window.

## 📊 Worked Example
Example: `freshness = [10, 1, 2, 4, 7, 2]`, `limit = 5`

| right | value | max deque values | min deque values | left after shrink | valid window |
|---|---:|---|---|---:|---|
| 0 | 10 | [10] | [10] | 0 | [10] |
| 1 | 1 | [10, 1] | [1] | 1 | [1] |
| 2 | 2 | [2] | [1, 2] | 1 | [1, 2] |
| 3 | 4 | [4] | [1, 2, 4] | 1 | [1, 2, 4] |
| 4 | 7 | [7] | [1, 2, 4, 7] | 2 | [2, 4, 7] |
| 5 | 2 | [7, 2] | [2] | 2 | [2, 4, 7, 2] |

Trace highlights:
1. At `right = 1`, window `[10, 1]` is invalid because `10 - 1 = 9`, so `left` moves to `1`.
2. At `right = 4`, window `[1, 2, 4, 7]` is invalid because `7 - 1 = 6`, so `left` advances past `1`.
3. At `right = 5`, the window `[2, 4, 7, 2]` is valid with range `7 - 2 = 5`, giving answer `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index is inserted into and removed from each monotonic deque at most once, so the dominant work is linear over the array. At `10^6` elements this remains practical; at `10^9`, linear time is still expensive but fundamentally better than any rescanning or quadratic approach.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when the window is strictly monotonic and many indices remain candidates. In practice it is often smaller. Reducing this to `O(1)` would require giving up efficient exact min/max maintenance.

## 💡 Key Takeaways
- If a contiguous-window problem asks for a constraint based on current `min` and `max`, think sliding window plus a structure that maintains extrema incrementally.
- If the window expands one step at a time and invalidity is repaired by moving only the left boundary, monotonic queues are often the simplest linear-time fit.
- Store **indices**, not just values, so you can evict elements precisely when they leave the window.
- Be careful with the shrink loop: update deques for expired indices and continue shrinking until `max - min <= limit`, not just once.
- The production-grade insight is to preserve only decision-relevant candidates; monotonic structures are a general way to turn repeated global recomputation into amortized constant-time maintenance.

## 🚀 Variations & Further Practice
- **Sliding Window Maximum / Minimum**: same monotonic-queue core, but fixed-size windows instead of a validity constraint; the twist is emitting extrema for every window position.
- **Longest subarray with absolute difference less than or equal to limit using balanced BST / multiset**: same problem, but compare the trade-off between `O(n)` monotonic queues and `O(n log n)` ordered structures that are more flexible for generalized queries.
- **2D window range constraints over matrices or streams with expiration by timestamp**: extends the same idea, but the harder twist is maintaining extrema across multiple dimensions or non-uniform window boundaries.