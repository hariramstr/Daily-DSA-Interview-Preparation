# Longest Caption Segment Within Reading Speed

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given a non-negative integer array `words` and an integer `maxWords`, find the maximum length of any contiguous subarray whose sum is at most `maxWords`. The output is a single integer: the longest valid segment length. The challenge is scale: `words.length` can reach `100000`, so checking every possible segment is too slow. The key structural property is that all values are non-negative, which makes a sliding window both valid and optimal.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval under a cumulative budget: video subtitle pacing, API request windows under token limits, streaming pipelines bounded by batch size, and telemetry ingestion constrained by payload quotas. At scale, brute-force interval scans turn linear streams into quadratic work, which is operationally unacceptable for hot paths. The sliding-window formulation exploits monotonicity from non-negative inputs, enabling single-pass processing with constant extra memory. That matters in production because it preserves throughput, reduces latency variance, and keeps memory predictable under sustained high-volume workloads.

## 🔍 Problem Statement
You are given an integer array `words`, where `words[i]` is the number of subtitle words shown during the `i`-th frame interval, and an integer `maxWords`, the viewer’s comfortable reading limit for any contiguous segment.

Return the maximum number of consecutive frame intervals whose total word count is less than or equal to `maxWords`.

Constraints:

- `1 <= words.length <= 100000`
- `0 <= words[i] <= 10000`
- `0 <= maxWords <= 10^9`

Examples:

- `words = [3, 1, 2, 1, 1, 4], maxWords = 5` → `4`
  - Longest valid segment: `[1, 2, 1, 1]`
- `words = [5, 2, 2, 6, 1], maxWords = 7` → `2`
  - Longest valid segments include `[5, 2]` and `[6, 1]`

The algorithmic constraint is decisive: with up to `100000` elements, `O(n^2)` enumeration is too slow. Because all values are non-negative, window sums only increase when expanded, which enables an `O(n)` two-pointer solution.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, not an arbitrary subset. That immediately suggests interval-based techniques rather than sorting or dynamic programming.

2. Notice the objective → maximize window length while keeping the window sum `<= maxWords`. That is the canonical shape of a sliding-window constraint problem.

3. Ask what makes sliding window safe → all `words[i]` are non-negative. When you extend the right boundary, the sum never decreases. When the sum becomes too large, moving the left boundary rightward is the only way to restore validity.

4. That monotonic behavior means each pointer only moves forward. No backtracking, no recomputation of prior sums, no nested scan.

5. Maintain a running sum for the current window `[left, right]`. For each new `right`, add `words[right]`.

6. If the sum exceeds `maxWords`, shrink from the left until the window is valid again.

7. After each adjustment, update the best length seen so far.

The reasoning path is straightforward: contiguous range + upper-bound constraint + non-negative values → sliding window with two pointers.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `currentSum = 0`, and `best = 0`.  
   This represents an empty window before processing any frame. The invariant is: after cleanup, the active window will always satisfy `currentSum <= maxWords`.

2. **Expand the window with the right pointer**  
   Iterate `right` from `0` to `words.length - 1`, adding `words[right]` to `currentSum`.  
   This is the “grow” phase of the **Sliding Window / Two Pointers** pattern: try to include as many consecutive elements as possible.

3. **Restore validity when the budget is exceeded**  
   While `currentSum > maxWords`, subtract `words[left]` and increment `left`.  
   This is correct because all values are non-negative. Removing elements from the left can only reduce or preserve the sum, so once the window is invalid, shrinking is the only productive move.

4. **Record the best valid window**  
   After the shrink loop ends, the window `[left, right]` is valid again. Its length is `right - left + 1`. Update `best` if this length is larger.

5. **Maintain the core invariant**  
   At the end of each iteration, the current window is the longest valid window ending at `right` that can be obtained without reconsidering earlier positions. Since both pointers move only forward, every element is added once and removed at most once.

6. **Return the maximum length found**  
   `best` is the answer because every valid contiguous segment appears as some window during the scan, and the algorithm checks its length exactly when that window becomes valid.

## 📊 Worked Example
Example: `words = [3, 1, 2, 1, 1, 4]`, `maxWords = 5`

| right | words[right] | currentSum after add | left after shrink | valid window     | length | best |
|------:|-------------:|---------------------:|------------------:|------------------|-------:|-----:|
| 0     | 3            | 3                    | 0                 | `[3]`            | 1      | 1    |
| 1     | 1            | 4                    | 0                 | `[3,1]`          | 2      | 2    |
| 2     | 2            | 6                    | 1                 | `[1,2]`          | 2      | 2    |
| 3     | 1            | 4                    | 1                 | `[1,2,1]`        | 3      | 3    |
| 4     | 1            | 5                    | 1                 | `[1,2,1,1]`      | 4      | 4    |
| 5     | 4            | 9                    | 4                 | `[1,4]`          | 2      | 4    |

The longest valid segment is `[1,2,1,1]`, so the answer is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = words.length`. Each element is processed at most twice: once when the right pointer includes it, and once when the left pointer excludes it. This remains practical at `10^6` elements, while `O(n^2)` becomes infeasible far earlier and is completely unrealistic at `10^9` scale.

### Space Complexity
`O(1)` extra space. The algorithm stores only pointer indices, a running sum, and the best length. No auxiliary array or heap is required. Space cannot be meaningfully reduced further without losing the running-sum optimization.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a **sum/budget constraint**, sliding window should be one of the first patterns you test.
- Non-negative input values are the decisive signal that a two-pointer window is valid, because expansion and shrinkage behave monotonically.
- Update the answer **after** shrinking, not before; otherwise you may record an invalid window whose sum exceeds `maxWords`.
- Be careful with window length calculation: for inclusive bounds, it is `right - left + 1`, not `right - left`.
- In production systems, monotonic constraints often let you replace repeated interval scans with single-pass streaming logic, which is the difference between acceptable and pathological throughput.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: harder because the optimization target flips, and with arbitrary integers the simple sliding window no longer works.
- **Longest subarray with at most `K` distinct values**: same two-pointer pattern, but validity depends on frequency state rather than a numeric sum.
- **Maximum average or weighted budget window**: introduces derived metrics where local monotonicity may break, forcing prefix sums, binary search, or more advanced data structures.