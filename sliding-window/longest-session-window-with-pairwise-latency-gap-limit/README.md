# Longest Session Window With Pairwise Latency Gap Limit

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Deque

---

## 🗂 Problem Overview
Given an array `latency` and an integer `limit`, find the maximum length of a contiguous subarray whose values stay within `limit` of each other. The condition is equivalent to requiring `max(window) - min(window) <= limit`. The challenge is scale: with up to `200000` elements, any approach that recomputes min/max for many overlapping windows degrades too quickly. The solution needs to maintain window extrema incrementally while moving through the array once.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to detect the longest stable interval in a noisy stream: API latency monitoring, market data smoothing, sensor anomaly detection, stream processing, and autoscaling control loops. At production scale, the difference between rescanning each window and maintaining rolling extrema is the difference between linear throughput and collapse under bursty traffic. Without this approach, online analytics pipelines either miss SLO windows or require expensive pre-aggregation. With it, you can evaluate stability constraints in real time, on unbounded streams, with predictable memory and latency behavior.

## 🔍 Problem Statement
You are given an integer array `latency` where `latency[i]` is the response time of the `i`-th request, and an integer `limit`. A contiguous window `[l, r]` is stable if every pair of values inside it differs by at most `limit`. That pairwise condition reduces to a simpler test:

`max(latency[l..r]) - min(latency[l..r]) <= limit`

Return the length of the longest stable contiguous window.

Constraints:
- `1 <= latency.length <= 200000`
- `0 <= latency[i] <= 10^9`
- `0 <= limit <= 10^9`

Examples:
- `latency = [8, 2, 4, 7], limit = 4` → `2`
- `latency = [10, 1, 2, 4, 7, 2], limit = 5` → `4`

The key constraint is input size. Enumerating subarrays is quadratic, and even a linear scan per candidate window is too slow. The algorithm must support fast updates to both current minimum and maximum as the window grows and shrinks.

## 🪜 How to Solve This
1. Read the condition → “every pair differs by at most `limit`” looks expensive, but it collapses to one check: only the current `min` and `max` matter.
2. We need a contiguous block → that strongly suggests a sliding window with two pointers, expanding `right` and advancing `left` only when the window becomes invalid.
3. But a plain sliding window is not enough → when `right` moves, we need updated min and max; when `left` moves, we may need to discard stale extrema.
4. Recomputing min/max from the whole window each time would turn this into quadratic behavior on adversarial inputs.
5. So maintain two deques:
   - one monotonic decreasing deque for the maximum
   - one monotonic increasing deque for the minimum
6. As each new value enters, remove dominated values from the back. As `left` advances, remove expired values from the front if they fall out of the window.
7. At every step, if `max - min > limit`, shrink until valid again, then record window length.

That gives a single-pass solution with amortized constant work per element.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and two monotonic deques.**  
   Use `left = 0`, iterate `right` from `0` to `n - 1`. Maintain:
   - `maxDeque`: indices whose values are in decreasing order
   - `minDeque`: indices whose values are in increasing order  
   This is the core **Sliding Window + Monotonic Queue** pattern.

2. **Insert the new element at `right`.**  
   Before appending `right`:
   - pop from the back of `maxDeque` while `latency[back] < latency[right]`
   - pop from the back of `minDeque` while `latency[back] > latency[right]`  
   This preserves the invariant that deque fronts always hold the current window max and min.

3. **Check whether the window is still stable.**  
   The window `[left, right]` is valid iff  
   `latency[maxDeque.front] - latency[minDeque.front] <= limit`.  
   If not, the current window violates the pairwise bound.

4. **Shrink from the left until valid.**  
   While invalid, increment `left`. Before or after incrementing, remove deque fronts whose index is now `< left`.  
   This guarantees both deques contain only indices inside the active window.

5. **Update the answer.**  
   Once valid, compute `right - left + 1` and update the best length.  
   Correctness follows from the invariant: after shrinking, `[left, right]` is the longest valid window ending at `right`.

6. **Why linear time holds.**  
   Each index is inserted into each deque once and removed once. No element can be popped repeatedly beyond that. The total deque work is therefore `O(n)`.

## 📊 Worked Example
Example: `latency = [10, 1, 2, 4, 7, 2]`, `limit = 5`

| right | value | left after shrink | maxDeque values | minDeque values | valid window | best |
|---|---:|---:|---|---|---|---:|
| 0 | 10 | 0 | [10] | [10] | [10] | 1 |
| 1 | 1  | 1 | [1] | [1] | [1] | 1 |
| 2 | 2  | 1 | [2] | [1,2] | [1,2] | 2 |
| 3 | 4  | 1 | [4] | [1,2,4] | [1,2,4] | 3 |
| 4 | 7  | 2 | [7] | [2,4,7] | [2,4,7] | 3 |
| 5 | 2  | 2 | [7,2] | [2] | [2,4,7,2] | 4 |

At `right = 4`, the window `[1,2,4,7]` is invalid because `7 - 1 = 6`, so `left` advances to drop `1`. At `right = 5`, the window `[2,4,7,2]` has `max = 7`, `min = 2`, difference `5`, so it is valid and sets the final answer to `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is pushed into `maxDeque` and `minDeque` once, and popped from each at most once. The dominant work is deque maintenance during the single left/right scan. At `10^6` elements this remains practical; at `10^9`, linear time is still the lower bound, but memory and I/O become the real bottlenecks.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when the input is strictly monotonic. In practice, they often stay much smaller. You cannot reduce this to `O(1)` while preserving exact rolling min/max under arbitrary shrink operations without giving up linear-time guarantees.

## 💡 Key Takeaways
- If the rule is “all values in a window must stay within a bound,” translate it immediately to `max - min <= limit`.
- If the problem asks for the longest valid contiguous range under a dynamic constraint, think sliding window before considering heavier structures.
- Store indices in the deques, not raw values, so you can evict elements that fall out of the window correctly.
- Be precise about when stale front elements are removed as `left` advances; this is the most common source of subtle bugs.
- In production stream processing, monotonic queues are the right tool when you need exact rolling extrema with predictable linear behavior under high-volume input.

## 🚀 Variations & Further Practice
- **Sliding Window Maximum / Minimum** — same monotonic deque core, but only one extremum is needed; useful for isolating the data-structure pattern.
- **Shortest subarray satisfying a bound after arbitrary violations** — same two-pointer instinct, but the objective flips and often changes the shrink logic.
- **Median- or percentile-bounded windows** — harder because monotonic queues no longer suffice; you need balanced trees, heaps with lazy deletion, or order-statistics structures.