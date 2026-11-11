# Longest Toll Segment With Exact Pass Usage

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Two Pointers

---

## 🗂 Problem Overview
Given an array `roads`, find the maximum length of a contiguous subarray containing exactly `k` values `<= passLimit`. Every element in the chosen window either qualifies for pass usage or does not, and the count of qualifying elements must be exact, not bounded. The challenge is scale: with up to `100000` elements, brute-force enumeration of all subarrays is too slow, so the solution must exploit contiguous structure and maintain counts incrementally.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, fraud detection, observability pipelines, and traffic-shaping systems: find the longest interval where a threshold-triggered event occurs exactly `k` times. Examples include the longest request span with exactly `k` throttled calls, the widest log window with exactly `k` error-class events, or the largest ranking segment with exactly `k` low-confidence results. At production scale, nested scans collapse under throughput and latency requirements. A linear sliding-window formulation enables online processing, bounded memory, and predictable performance under sustained high-volume streams.

## 🔍 Problem Statement
You are given:

- `roads`: an integer array where `roads[i]` is the toll cost of the `i`-th road segment
- `passLimit`: a segment qualifies for pass usage if `roads[i] <= passLimit`
- `k`: the chosen contiguous subarray must contain exactly `k` qualifying segments

Return the maximum length of any contiguous subarray satisfying that condition. If none exists, return `0`.

Constraints:

- `1 <= roads.length <= 100000`
- `0 <= roads[i] <= 1000000000`
- `0 <= passLimit <= 1000000000`
- `0 <= k <= roads.length`

Examples:

- `roads = [8, 2, 5, 1, 9, 3, 7], passLimit = 3, k = 2` → `5`
- `roads = [4, 6, 8, 10], passLimit = 5, k = 1` → `4`

The key constraint is input size: `O(n^2)` subarray enumeration is not viable, so the algorithm must run in linear time.

## 🪜 How to Solve This
1. Read the condition carefully → the actual values do not matter beyond whether `roads[i] <= passLimit`.
2. That means each element can be reduced to a binary signal: qualifying or non-qualifying.
3. We need the **longest contiguous window** with **exactly `k` qualifying elements** → this is a classic sliding-window signal.
4. Exact-count window problems are often easier if reframed as:
   - longest window with **at most `k`** qualifiers
   - minus the restriction for **at most `k - 1`** if counting subarrays  
   But here we need a maximum length, not a count, so we can solve directly.
5. Use two pointers:
   - expand the right pointer
   - track how many qualifying elements are in the current window
   - if the count exceeds `k`, move the left pointer until the window is valid again
6. Whenever the count is exactly `k`, the current window is a candidate answer.
7. Because both pointers only move forward, the scan is linear.

The core insight is that contiguity plus a monotone constraint (`> k` is invalid, shrinking fixes it) makes Two Pointers the right tool.

## 🧩 Algorithm Walkthrough
1. **Classify each element on the fly.**  
   Treat `roads[i] <= passLimit` as a qualifying segment. We do not need a second array; a boolean check during traversal is enough. This keeps the state minimal.

2. **Initialize a sliding window.**  
   Maintain:
   - `left`: start of current window
   - `right`: end of current window as we iterate
   - `qualifyingCount`: number of values `<= passLimit` inside `[left, right]`
   - `best`: maximum valid length seen so far

3. **Expand the window by moving `right`.**  
   For each new element, if it qualifies, increment `qualifyingCount`. This preserves the invariant that `qualifyingCount` matches the current window contents.

4. **Restore validity when the window exceeds the budget.**  
   If `qualifyingCount > k`, advance `left` until `qualifyingCount <= k`. Each time a qualifying element leaves the window, decrement the count. This is the critical Two Pointers invariant: after shrinking, the window is the longest suffix ending at `right` that does not exceed `k`.

5. **Record answers only for exact matches.**  
   When `qualifyingCount == k`, update `best = max(best, right - left + 1)`. This is correct because the current window is valid and as wide as possible for this `right` under the maintained invariant.

6. **Handle edge cases explicitly.**  
   If `k == 0`, the algorithm still works: the valid windows are maximal stretches containing no qualifying elements. If no window ever reaches exactly `k`, `best` remains `0`.

This is a standard **Sliding Window / Two Pointers** pattern because the validity condition depends on a count that changes predictably as the window expands or contracts.

## 📊 Worked Example
Example: `roads = [8, 2, 5, 1, 9, 3, 7]`, `passLimit = 3`, `k = 2`

| right | roads[right] | qualifies (`<=3`) | left | qualifyingCount | valid? | best |
|------:|--------------:|------------------:|-----:|----------------:|:------:|-----:|
| 0 | 8 | No  | 0 | 0 | No  | 0 |
| 1 | 2 | Yes | 0 | 1 | No  | 0 |
| 2 | 5 | No  | 0 | 1 | No  | 0 |
| 3 | 1 | Yes | 0 | 2 | Yes | 4 |
| 4 | 9 | No  | 0 | 2 | Yes | 5 |
| 5 | 3 | Yes | 0 | 3 | No  | 5 |

Now shrink:
- remove `8` → count still `3`, `left = 1`
- remove `2` → count becomes `2`, `left = 2`

Current window is `[5, 1, 9, 3]`, length `4`, so `best` stays `5`.

| right | roads[right] | qualifies | left | qualifyingCount | valid? | best |
|------:|--------------:|----------:|-----:|----------------:|:------:|-----:|
| 6 | 7 | No | 2 | 2 | Yes | 5 |

Final answer: `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed once when `right` expands the window, and at most once again when `left` contracts it. There is no nested rescanning. At `10^6` elements this is practical in memory-resident workloads; at `10^9`, the algorithmic shape is still optimal, but I/O and streaming constraints dominate.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a qualifying counter, and the best length. No auxiliary arrays or hash structures are required. Space cannot be meaningfully reduced further without losing the ability to track the current window state.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous region** under a threshold-based count constraint, Sliding Window / Two Pointers should be your first candidate.
- When element magnitudes are irrelevant except for a predicate like `<= passLimit`, reduce the problem mentally to a binary stream and reason about counts.
- Be careful to update the answer only when the window has **exactly** `k` qualifying elements; `<= k` is only an intermediate validity condition.
- The shrink loop must run while `qualifyingCount > k`, not `>= k`, or you will discard valid exact-match windows.
- In production systems, this pattern matters because it converts expensive interval searches into single-pass, bounded-state scans suitable for online processing.

## 🚀 Variations & Further Practice
- **Count all subarrays with exactly `k` qualifying elements**: same predicate reduction, but now use `atMost(k) - atMost(k - 1)`; the twist is counting all windows rather than maximizing one.
- **Longest subarray where the sum of qualifying weights is exactly `k`**: harder because the window state is no longer binary-count based, and monotonicity may break if weights vary.
- **2D version on a grid or matrix**: find the largest rectangle with exactly `k` qualifying cells; the twist is collapsing rows and combining prefix sums with subarray techniques.