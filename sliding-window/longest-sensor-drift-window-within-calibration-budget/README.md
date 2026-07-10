# Longest Sensor Drift Window Within Calibration Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Array

---

## 🗂 Problem Overview
Given an integer array `readings` and an integer `budget`, find the maximum length of a contiguous subarray whose maximum and minimum values differ by at most `budget`. The output is a single integer: the longest valid window size. The challenge is maintaining window `max` and `min` efficiently while expanding and shrinking the window, because checking them from scratch for every subarray leads to quadratic time and fails at `n = 200000`.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest stable segment under bounded variance: telemetry smoothing, anomaly suppression in streaming pipelines, market data batching, autoscaling signal aggregation, and distributed rate-limiters that tolerate short-lived jitter. At scale, recomputing extrema for every candidate interval turns a linear scan into a latency amplifier and often forces premature sharding or sampling. The monotonic-queue sliding-window approach preserves exactness while keeping throughput predictable, which matters when windows are evaluated continuously over high-volume event streams or time-series partitions.

## 🔍 Problem Statement
You are given:

- `readings`: an integer array of length `1 <= readings.length <= 200000`
- `budget`: an integer with `0 <= budget <= 10^9`

Each contiguous window `readings[l..r]` is valid if:

- `max(readings[l..r]) - min(readings[l..r]) <= budget`

Return the maximum length among all valid contiguous subarrays.

Values may be large and negative:

- `-10^9 <= readings[i] <= 10^9`

Examples:

- `readings = [8, 10, 9, 12, 7, 8], budget = 3` → `3`
- `readings = [4, 4, 5, 6, 6, 3, 4], budget = 2` → `5`

Edge cases include all-equal arrays, `budget = 0`, strictly increasing or decreasing sequences, and windows invalidated by a single outlier. The key constraint is array size: `O(n^2)` enumeration is not viable, so the algorithm must update window validity incrementally in near-constant time per element.

## 🪜 How to Solve This
1. Read the condition carefully → validity depends only on the current window’s `max` and `min`, not on every pair inside it.

2. That immediately suggests a sliding window: expand the right boundary while the window stays valid, and shrink the left boundary only when it breaks.

3. The hard part is not the two pointers; it is answering “what are the current max and min?” after every move without rescanning the window.

4. If we used a balanced tree, we could track extrema in `O(log n)` per update. Good enough, but not optimal.

5. Because the window only moves forward, we can do better with two monotonic deques:
   - one decreasing deque for candidate maxima
   - one increasing deque for candidate minima

6. When a new reading arrives, remove weaker candidates from the back, then append it. The front always holds the current extreme.

7. If `max - min > budget`, advance the left pointer and evict deque fronts when they fall out of the window.

8. Record the largest valid width seen. Every index enters and leaves each deque once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use two pointers: `left = 0`, iterate `right` from `0` to `n - 1`. Maintain:
   - a decreasing deque of indices for maximum candidates
   - an increasing deque of indices for minimum candidates  
   This is the standard **Sliding Window + Monotonic Queue** pattern.

2. **Insert the new element into the max deque**  
   While the deque’s back points to a value smaller than `readings[right]`, pop it. Then push `right`.  
   Why correct: any smaller value behind a larger, newer value can never become the max for a future window containing both.

3. **Insert the new element into the min deque**  
   While the deque’s back points to a value larger than `readings[right]`, pop it. Then push `right`.  
   Symmetric reasoning: larger, older values are dominated for future minimum queries.

4. **Check window validity**  
   The current max is `readings[maxDeque.front]`; the current min is `readings[minDeque.front]`.  
   If their difference exceeds `budget`, the window is invalid.

5. **Shrink from the left until valid**  
   While invalid, increment `left`. Before or after incrementing, remove deque fronts whose indices are now `< left`.  
   Invariant maintained: both deques contain only indices inside the current window, and their fronts are the true extrema.

6. **Update the answer**  
   Once valid, compute `right - left + 1` and maximize the result.  
   This is correct because for each `right`, the algorithm finds the leftmost valid boundary after all necessary shrinking.

7. **Why linear time holds**  
   Each index is pushed once and popped at most once from each deque. No index re-enters. That amortizes all deque maintenance to `O(n)` total.

## 📊 Worked Example
Example: `readings = [8, 10, 9, 12, 7, 8]`, `budget = 3`

| right | value | maxDeque values | minDeque values | left | valid? | best |
|---|---:|---|---|---:|---|---:|
| 0 | 8  | [8]       | [8]       | 0 | yes (`8-8=0`)  | 1 |
| 1 | 10 | [10]      | [8,10]    | 0 | yes (`10-8=2`) | 2 |
| 2 | 9  | [10,9]    | [8,9]     | 0 | yes (`10-8=2`) | 3 |
| 3 | 12 | [12]      | [8,9,12]  | 1→2→3 | after shrinking, yes (`12-12=0`) | 3 |
| 4 | 7  | [12,7]    | [7]       | 4 | yes (`7-7=0`)  | 3 |
| 5 | 8  | [8]       | [7,8]     | 4 | yes (`8-7=1`)  | 3 |

At `right = 3`, adding `12` breaks the budget. Shrinking removes `8`, then `10`, then `9`, until only `[12]` remains. The best valid length seen is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each reading is appended once to each deque and removed at most once, so deque maintenance is amortized constant time per element. At `10^6` elements this remains practical in a single pass; at `10^9`, the bottleneck becomes memory bandwidth and I/O rather than algorithmic overhead.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when the window is monotonic and many indices remain candidates. This cannot be reduced to `O(1)` while preserving exact online max/min updates; the trade-off is using a tree for `O(log n)` updates with similar asymptotic space.

## 💡 Key Takeaways
- If a contiguous-window problem asks for the longest range under a constraint involving current `max` and `min`, think sliding window plus an auxiliary structure for extrema.
- If the window only moves forward and you need repeated range extrema, monotonic deques are usually a stronger fit than heaps or rescanning.
- Store indices, not values, so you can evict elements precisely when they fall out of the left side of the window.
- Be careful with the shrink loop: update deque fronts when their indices are outside the window, or stale extrema will make valid windows look invalid.
- The production lesson is broader than this problem: incremental maintenance of a small set of decision-critical statistics often converts unstable quadratic behavior into predictable streaming throughput.

## 🚀 Variations & Further Practice
- **Sliding Window Maximum / Minimum** — same monotonic deque machinery, but the twist is fixed-size windows instead of a validity constraint that drives shrinking.
- **Shortest subarray with sum at least K** — also uses a monotonic deque, but over prefix sums; the harder part is reasoning about optimal candidates rather than direct window extrema.
- **Longest continuous subarray with absolute diff less than or equal to limit using a balanced tree** — same problem family, but compare `O(n)` monotonic queues with `O(n log n)` ordered-map implementations and their operational trade-offs.