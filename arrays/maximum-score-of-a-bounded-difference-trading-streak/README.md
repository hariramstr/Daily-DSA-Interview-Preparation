# Maximum Score of a Bounded-Difference Trading Streak

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Monotonic Queue

---

## 🗂 Problem Overview
Given an integer array `profits` and a non-negative `limit`, find the contiguous subarray whose value range satisfies `max(window) - min(window) <= limit` and whose score `(window length) * (window sum)` is maximized. You must return the best score over all non-empty valid windows. The challenge is that validity depends on dynamic min/max, while the objective depends on both length and sum, so a standard fixed-window or greedy expansion is insufficient.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, market surveillance, telemetry aggregation, and online anomaly detection: maximize a business metric over contiguous time ranges while enforcing a bounded spread or stability constraint. In production, the naive approach degenerates into quadratic rescans for every candidate interval, which collapses under high-cardinality event streams or long retention windows. Monotonic queues let you maintain admissible windows in linear time, while prefix-sum-derived scoring avoids recomputing aggregates. That combination is what makes real-time scoring, alerting, and ranking feasible under tight latency budgets and large in-memory streams.

## 🔍 Problem Statement
You are given an array `profits` where `profits[i]` is the profit or loss on day `i`, and an integer `limit`. A contiguous streak is valid only if the difference between its maximum and minimum element is at most `limit`. For every valid streak, define:

`score = (length of streak) * (sum of values in the streak)`

Return the maximum score among all valid non-empty contiguous streaks.

Constraints:
- `1 <= profits.length <= 2 * 10^5`
- `-10^9 <= profits[i] <= 10^9`
- `0 <= limit <= 10^9`
- Use 64-bit integers for sums and scores

Examples:
- `profits = [3, 1, 2, 2, 4], limit = 2` → `32`
- `profits = [-5, -2, -3, 1], limit = 1` → `1`

The key algorithmic pressure is `n = 2 * 10^5`: any solution that rescans window minima, maxima, or sums per candidate interval will time out.

## 🪜 How to Solve This
1. Start with the validity rule → it depends on the current window’s minimum and maximum, so this immediately suggests a sliding window with dynamic range tracking.
2. Dynamic min/max inside a moving window → use two monotonic deques: one increasing for minima, one decreasing for maxima.
3. Expand the right boundary one element at a time. If `max - min > limit`, move the left boundary until the window becomes valid again.
4. At that point, every step gives you one maximal valid window ending at `right`. But the objective is not just window size — it is `length * sum`, and sums can be negative.
5. So maintain prefix sums to compute any current window sum in O(1). For each valid window `[left..right]`, evaluate its score directly.
6. Why is this enough? Because the sliding window enumerates every maximal valid interval under the range constraint, and every index enters and leaves each deque once, keeping the process linear.
7. The mental model: validity is enforced structurally by monotonic queues; scoring is computed algebraically by prefix sums.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums.**  
   Build `prefix` where `prefix[i + 1] = prefix[i] + profits[i]`. Then the sum of any window `[l..r]` is `prefix[r + 1] - prefix[l]`. This avoids repeated summation and keeps score evaluation O(1).

2. **Maintain two monotonic deques.**  
   Use:
   - `minDeque`: indices with non-decreasing values
   - `maxDeque`: indices with non-increasing values  
   Their fronts always hold the minimum and maximum of the current window. This is the standard **Sliding Window + Monotonic Queue** pattern.

3. **Expand the right pointer.**  
   For each `r`, insert `profits[r]` into both deques, popping dominated elements from the back. This preserves the monotonic invariant and ensures each deque contains only useful candidates.

4. **Shrink from the left until valid.**  
   While `profits[maxDeque.front] - profits[minDeque.front] > limit`, increment `l`. If either deque’s front falls left of `l`, pop it. After this loop, `[l..r]` is the longest valid window ending at `r`.

5. **Compute the current score.**  
   Let `len = r - l + 1` and `sum = prefix[r + 1] - prefix[l]`. Update `answer = max(answer, len * sum)`. Use 64-bit arithmetic throughout.

6. **Why this is correct.**  
   The deques guarantee exact min/max for the active window. The left pointer only moves forward, so total pointer movement is linear. Every valid maximal suffix ending at `r` is considered once, and its score is computed exactly from prefix sums.

## 📊 Worked Example
Example: `profits = [3, 1, 2, 2, 4]`, `limit = 2`

| r | value | l after shrink | window       | min | max | sum | len | score |
|---|-------|----------------|--------------|-----|-----|-----|-----|-------|
| 0 | 3     | 0              | [3]          | 3   | 3   | 3   | 1   | 3     |
| 1 | 1     | 0              | [3,1]        | 1   | 3   | 4   | 2   | 8     |
| 2 | 2     | 0              | [3,1,2]      | 1   | 3   | 6   | 3   | 18    |
| 3 | 2     | 0              | [3,1,2,2]    | 1   | 3   | 8   | 4   | 32    |
| 4 | 4     | 2              | [2,2,4]      | 2   | 4   | 8   | 3   | 24    |

At `r = 4`, the window `[3,1,2,2,4]` becomes invalid because `4 - 1 = 3 > 2`, so `l` advances until the range is valid again. The best score seen is `32` from `[3,1,2,2]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index is pushed and popped at most once from `minDeque` and `maxDeque`, and both pointers move monotonically forward. That makes the dominant work linear. At `10^6` elements this is still practical; at `10^9`, even linear scans become throughput- and memory-bandwidth-bound.

### Space Complexity
`O(n)` if you store prefix sums explicitly, plus `O(n)` worst-case across the deques, though deque occupancy is typically much smaller. You can reduce auxiliary sum storage to `O(1)` with a rolling window sum, but prefix sums keep the implementation simpler and less error-prone.

## 💡 Key Takeaways
- If a subarray constraint is phrased as `max - min <= K`, that is a strong signal for monotonic deques rather than heaps or repeated rescans.
- If the objective combines window size with an aggregate like sum, pair sliding-window validity tracking with prefix sums or another O(1) aggregate structure.
- Be careful to evict deque fronts only when their indices fall strictly left of the current `l`; value-based eviction is wrong with duplicates.
- Use 64-bit integers for both prefix sums and `length * sum`; overflow is easy here even when individual values fit in 32 bits.
- The transferable design insight is to separate concerns: one structure enforces admissibility, another computes the business metric, which scales far better than entangling both in one state machine.

## 🚀 Variations & Further Practice
- Maximize a valid window score where the constraint is `max + min <= K` or `median`-bounded instead of range-bounded; the admissibility structure is no longer a simple pair of monotonic deques.
- Return the top `k` valid streaks without overlap; this adds interval selection on top of online window enumeration.
- Support point updates to `profits` and repeated queries for the best valid streak; this shifts the problem toward segment trees, offline processing, or more complex range-query data structures.