# Longest Quiet Study Stretch

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given a non-negative integer array `noise` and an integer `maxNoise`, find the maximum length of any contiguous subarray whose sum is at most `maxNoise`. The output is a single integer: the longest valid study stretch. The problem is non-trivial because brute force checks all subarrays in quadratic time, which fails at `noise.length = 100000`. The key enabling constraint is that all values are non-negative, which makes a shrinking sliding window correct.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval under a cumulative budget: API rate-limit windows, streaming pipelines bounded by per-batch cost, ad-tech impression pacing, mobile telemetry buffering, and storage compaction throttling. At scale, the difference between recomputing every candidate interval and maintaining a rolling window is the difference between online processing and backlog growth. Non-negative metrics let you use a monotonic budget model: once a window exceeds the limit, extending it only makes things worse. That property enables single-pass implementations with predictable latency, low memory pressure, and straightforward operational behavior under sustained load.

## 🔍 Problem Statement
You are given an array `noise` where `noise[i]` is the noise level recorded at minute `i`, and an integer `maxNoise` representing the maximum total noise a student can tolerate during one continuous study session.

Return the length of the longest contiguous subarray whose sum is `<= maxNoise`.

Constraints:

- `1 <= noise.length <= 100000`
- `0 <= noise[i] <= 10000`
- `0 <= maxNoise <= 1000000000`

Examples:

- `noise = [2, 1, 3, 2, 1, 1], maxNoise = 5` → `3`
- `noise = [6, 2, 1], maxNoise = 5` → `2`

Edge cases matter:

- If every single element is greater than `maxNoise`, return `0`.
- Zero-valued minutes are valid and can extend a window without increasing its sum.

The algorithmic choice is driven by one critical constraint: all `noise[i]` values are non-negative. That makes sliding window valid because window sums only increase when expanding right.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, not an arbitrary subset. That immediately suggests interval-based techniques rather than DP over combinations.

2. Notice the condition is on the **sum of the current interval**. If we had to evaluate every start/end pair, that would be `O(n^2)`, which is too slow for `100000` elements.

3. Look at the data constraint → all `noise[i]` are non-negative. That is the signal for a sliding window:
   - moving the right pointer forward can only keep the sum the same or increase it
   - if the sum becomes too large, moving the left pointer forward is the only way to recover validity

4. Maintain a window `[left, right]` and its running sum.
   - Expand right one minute at a time
   - While the sum exceeds `maxNoise`, shrink from the left
   - After restoring validity, record the current window length

5. Why this works → for each `right`, the smallest valid `left` is enough. Any earlier `left` would violate the budget, and any later `left` would only make the window shorter.

That gives a linear scan with constant extra space.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window state**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`, `windowSum = 0`, and `best = 0`. The window always represents the current contiguous candidate interval. The invariant we want is: after adjustment, `windowSum <= maxNoise`.

2. **Extend the window to the right**  
   For each index `right` from `0` to `n - 1`, add `noise[right]` to `windowSum`. This considers every possible ending position exactly once. Because values are non-negative, extending right never decreases the sum.

3. **Restore validity by shrinking from the left**  
   While `windowSum > maxNoise`, subtract `noise[left]` and increment `left`. This is correct because once the budget is exceeded, no window starting at the current `left` and ending at `right` can be valid. Removing elements from the left is the only way to reduce the sum.

4. **Record the best valid length**  
   After the shrink loop, the window `[left, right]` is valid again. Update `best = max(best, right - left + 1)`. This captures the longest valid window ending at `right`.

5. **Why the invariant is enough**  
   At every iteration, the maintained window is the longest valid window ending at the current `right`. Any longer one would start earlier and would have already violated the sum constraint. That is why no candidate is missed.

6. **Return the result**  
   If no element can fit, `best` remains `0`, which matches the required behavior.

## 📊 Worked Example
Example: `noise = [2, 1, 3, 2, 1, 1]`, `maxNoise = 5`

| right | noise[right] | windowSum after add | action while sum > 5 | left | valid window | best |
|------:|-------------:|--------------------:|----------------------|-----:|--------------|-----:|
| 0 | 2 | 2 | none | 0 | `[2]` | 1 |
| 1 | 1 | 3 | none | 0 | `[2,1]` | 2 |
| 2 | 3 | 6 | remove `2` | 1 | `[1,3]` | 2 |
| 3 | 2 | 6 | remove `1` | 2 | `[3,2]` | 2 |
| 4 | 1 | 6 | remove `3` | 3 | `[2,1]` | 2 |
| 5 | 1 | 4 | none | 3 | `[2,1,1]` | 3 |

The longest valid stretch is length `3`, from indices `3..5`. The key behavior is visible at each overflow: because all values are non-negative, shrinking from the left is sufficient to restore validity without reconsidering earlier starts.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is added to the running sum once when the right pointer advances and removed at most once when the left pointer advances. There is no nested reprocessing of the array. At `10^6` elements this remains practical in a single pass; at `10^9`, the bottleneck becomes I/O and memory bandwidth, not algorithmic shape.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, the running sum, and the best length. No prefix array or extra data structure is required. You could trade this for prefix sums, but that would increase memory without improving asymptotic runtime for this non-negative case.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a threshold and all values are non-negative, sliding window should be your first candidate.
- The strongest recognition signal is monotonicity: expanding the window cannot improve an over-budget sum, so shrinking from the left becomes safe and complete.
- Update `best` only **after** the shrink loop; recording length before restoring validity produces incorrect answers.
- Be careful with `right - left + 1`; once `left` moves past `right` on an oversized element, the current valid length can legitimately become `0`.
- In production systems, non-negative budget metrics enable single-pass admission-control logic with stable latency and no need for backtracking or expensive recomputation.

## 🚀 Variations & Further Practice
- **Minimum Size Subarray Sum**: find the shortest contiguous subarray with sum at least `target`; same pattern, but the optimization objective flips and changes when you update the answer.
- **Longest Subarray with Sum at Most K with negative numbers allowed**: sliding window breaks because sums are no longer monotonic; requires prefix sums plus a more advanced structure or offline processing.
- **Count subarrays with product less than K**: still a sliding window on positive values, but the maintained aggregate is multiplicative rather than additive, which changes overflow handling and edge cases around zero.