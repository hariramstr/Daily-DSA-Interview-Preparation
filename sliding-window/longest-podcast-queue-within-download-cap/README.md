# Longest Podcast Queue Within Download Cap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `sizes` and a storage limit `cap`, find the maximum length of a contiguous block of episodes whose total download size is at most `cap`. Return only that length. The challenge is not correctness but efficiency: with up to `200000` episodes, checking every subarray is too expensive. The key observation is that all sizes are positive, which makes a sliding window viable and lets us maintain validity incrementally.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize contiguous work under a hard budget: media prefetch queues, CDN edge caching, Kafka consumer batching, mobile offline sync, and API request coalescing under payload caps. At scale, brute-force range evaluation collapses under quadratic growth and turns a simple admission-control problem into a latency or cost hotspot. The sliding-window approach enables single-pass decision-making with predictable memory use, which matters in hot paths, stream processors, and client-side code where both throughput and battery/storage constraints are real architectural limits.

## 🔍 Problem Statement
You are given an integer array `sizes`, where `sizes[i]` is the download size in MB of the `i`-th podcast episode, and an integer `cap`, the maximum total storage available. Return the length of the longest contiguous subarray whose sum is `<= cap`.

Constraints:

- `1 <= sizes.length <= 200000`
- `1 <= sizes[i] <= 100000`
- `1 <= cap <= 10^15`
- If no single episode fits within `cap`, return `0`

Examples:

- `sizes = [40, 20, 30, 50, 10, 10]`, `cap = 70` → `3`
- `sizes = [90, 30, 20, 40]`, `cap = 60` → `2`

The decisive constraint is input size: an `O(n^2)` scan over all subarrays is not acceptable. Also, because every `sizes[i]` is positive, growing the window can only increase the sum, and shrinking it can only decrease the sum. That monotonic behavior is what makes a linear-time solution possible.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** block, so this is about ranges, not arbitrary subsets.

2. Notice all episode sizes are positive → that is the strongest signal here. Positive values mean if a window is too large, moving its left edge right will strictly reduce the sum.

3. Ask what brute force does → for every start index, try every end index and compute sums. Even with prefix sums, that still leaves too many candidate ranges.

4. Reframe the task → instead of recomputing every range, keep one active window `[left..right]` and maintain its current total.

5. Expand right → each new episode is tentatively included.

6. If the sum exceeds `cap`, shrink from the left until the window becomes valid again. Because values are positive, once valid, any further shrinking would only make the window shorter and unnecessary.

7. After restoring validity, record the current window length as a candidate answer.

8. Repeat once across the array → each index enters and leaves the window at most once, giving linear time.

This is the classic “longest valid contiguous segment under additive budget” pattern.

## 🧩 Algorithm Walkthrough
1. **Initialize window state**  
   Set `left = 0`, `currentSum = 0`, and `best = 0`. The window is empty initially. The invariant is: before evaluating `best`, the active window will be made valid such that `currentSum <= cap`.

2. **Iterate `right` from left to right**  
   For each episode at index `right`, add `sizes[right]` to `currentSum`. This expands the candidate contiguous block by one element. In the Two Pointers / Sliding Window pattern, `right` only moves forward.

3. **Repair invalid windows by advancing `left`**  
   While `currentSum > cap` and `left <= right`, subtract `sizes[left]` and increment `left`. This is correct because all values are positive: removing elements from the left is the only way to reduce the sum, and once the sum is back within budget, the resulting window is the longest valid window ending at `right`.

4. **Update the best length**  
   If the window is valid, compute `right - left + 1` and update `best = max(best, windowLength)`. The maintained invariant is that `[left..right]` is the maximum-length valid window ending at `right`.

5. **Why this abstraction fits**  
   Sliding Window is the right model because the validity condition depends on an aggregate over a contiguous range, and positivity gives monotonic behavior. Without positive numbers, shrinking would not reliably move the sum in one direction, and this linear strategy would break.

6. **Termination and result**  
   After one pass, return `best`. If no episode ever fits, `best` remains `0`, which matches the required behavior.

## 📊 Worked Example
Take `sizes = [40, 20, 30, 50, 10, 10]`, `cap = 70`.

| right | added | currentSum before shrink | left after shrink | currentSum after shrink | window        | best |
|------:|------:|-------------------------:|------------------:|------------------------:|---------------|-----:|
| 0     | 40    | 40                       | 0                 | 40                      | `[40]`        | 1    |
| 1     | 20    | 60                       | 0                 | 60                      | `[40,20]`     | 2    |
| 2     | 30    | 90                       | 1                 | 50                      | `[20,30]`     | 2    |
| 3     | 50    | 100                      | 3                 | 50                      | `[50]`        | 2    |
| 4     | 10    | 60                       | 3                 | 60                      | `[50,10]`     | 2    |
| 5     | 10    | 70                       | 3                 | 70                      | `[50,10,10]`  | 3    |

The best valid contiguous block has length `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each episode is added to the window once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing of the same elements. At `10^6` elements this remains practical; at `10^9`, linear time is still expensive but fundamentally better than any quadratic alternative.

### Space Complexity
`O(1)`. The algorithm stores only a few scalar variables: two pointers, a running sum, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further without losing the ability to track the active window state.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a sum constraint, Sliding Window should be your first check.
- Positive-only values are the signal that makes Two Pointers work: expanding increases the sum, shrinking decreases it.
- Use a 64-bit integer for the running sum; `cap` can be as large as `10^15`, so 32-bit accumulation is unsafe.
- Update the answer only after shrinking the window back to validity; recording length too early produces invalid results.
- In production systems, this pattern is a one-pass admission-control primitive: maximize useful contiguous work while staying inside a hard resource budget.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: similar range-sum reasoning, but the optimization target flips and often requires prefix sums plus a monotonic deque.
- **Longest subarray with sum `<= cap` when negatives are allowed**: positivity disappears, so standard sliding window breaks; you need prefix-sum ordering or more advanced data structures.
- **Maximum number of requests in a batch under payload and per-item constraints**: same window pattern, but validity depends on multiple conditions, not just one running sum.