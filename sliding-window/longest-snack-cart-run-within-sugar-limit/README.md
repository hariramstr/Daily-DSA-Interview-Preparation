# Longest Snack Cart Run Within Sugar Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given a non-negative integer array `sugars` and an integer `limit`, return the maximum length of any contiguous subarray whose sum is at most `limit`. The challenge is not computing sums, but doing so efficiently across up to `100000` elements. The key constraint is that all values are non-negative, which makes a shrinking-and-expanding sliding window valid and eliminates the need for backtracking or prefix-sum search structures.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must maintain the longest valid contiguous run under an additive budget while processing data in order: streaming pipelines enforcing byte or event quotas, distributed rate-limiters tracking bursts within a rolling batch, ad-tech pacing systems, log ingestion buffers, and observability agents capping memory or payload size. Without the sliding-window approach, teams often fall back to quadratic scans or repeated recomputation, which collapses under sustained throughput. The non-negative-input property enables single-pass admission control: advance optimistically, shrink only when over budget, and preserve linear latency under load.

## 🔍 Problem Statement
You are given an integer array `sugars` where `sugars[i]` is the sugar content of the `i`-th snack, and an integer `limit` representing the maximum allowed total sugar for one consecutive selection. Return the length of the longest contiguous segment whose sum is `<= limit`.

Constraints:

- `1 <= sugars.length <= 100000`
- `0 <= sugars[i] <= 100000`
- `0 <= limit <= 1000000000`
- All values in `sugars` are non-negative integers

Examples:

- `sugars = [2, 1, 3, 2, 1, 1]`, `limit = 5` → `3`
- `sugars = [6, 2, 2, 1]`, `limit = 4` → correct answer is `2`, from `[2, 2]` or `[2, 1]`

Edge cases matter: `limit = 0`, arrays containing zeros, and cases where every positive element exceeds the limit. The non-negative constraint is what makes an `O(n)` sliding window possible.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so sorting or reordering is invalid.

2. Notice all values are **non-negative** → this is the signal that a sliding window may work. When you extend the window rightward, the sum can only stay the same or increase. When you move the left edge rightward, the sum can only stay the same or decrease.

3. That monotonic behavior removes ambiguity → once a window exceeds `limit`, the only way to make it valid again is to shrink it from the left. There is no reason to revisit earlier left positions after they are removed.

4. Maintain two pointers, `left` and `right`, plus a running sum. Expand by moving `right`, add `sugars[right]`, and while the sum is too large, subtract `sugars[left]` and increment `left`.

5. After restoring validity, the current window is the longest valid window ending at `right`. Update the best length.

6. Repeat once across the array. No nested rescans are needed because each element enters and leaves the window at most once.

That reasoning leads directly to a linear-time solution.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `windowSum = 0`, and `best = 0`.  
   This defines an empty window `[left..right]` before iteration starts.

2. **Expand the window with the right pointer**  
   For each index `right` from `0` to `n - 1`, add `sugars[right]` to `windowSum`.  
   Why this works: we are exploring every possible contiguous segment as a right-anchored extension of the current window.

3. **Restore validity by shrinking from the left**  
   While `windowSum > limit`, subtract `sugars[left]` and increment `left`.  
   Invariant maintained: after this loop, the window `[left..right]` is valid, meaning its sum is `<= limit`.  
   This is correct only because all values are non-negative. Removing left elements cannot make a valid window invalid, and keeping an oversized window is never useful.

4. **Record the best valid length**  
   Compute `right - left + 1` and update `best` if larger.  
   Why this is sufficient: after shrinking, `[left..right]` is the longest valid window ending at `right`, because any earlier `left` would have produced a larger sum and remained invalid.

5. **Continue until the scan completes**  
   Each element is added once and removed at most once.  
   Pattern: **Sliding Window / Two Pointers**. This is the right abstraction because the feasibility condition is additive and monotonic under non-negative inputs.

6. **Return `best`**  
   If no snack fits, `best` remains `0`, which matches the required behavior.

## 📊 Worked Example
Example: `sugars = [2, 1, 3, 2, 1, 1]`, `limit = 5`

| right | sugars[right] | action after add/shrink | left | windowSum | valid window | best |
|------:|--------------:|-------------------------|-----:|----------:|--------------|-----:|
| 0 | 2 | add 2 | 0 | 2 | `[2]` | 1 |
| 1 | 1 | add 1 | 0 | 3 | `[2,1]` | 2 |
| 2 | 3 | add 3 → sum 6, shrink remove 2 | 1 | 4 | `[1,3]` | 2 |
| 3 | 2 | add 2 → sum 6, shrink remove 1 | 2 | 5 | `[3,2]` | 2 |
| 4 | 1 | add 1 → sum 6, shrink remove 3 | 3 | 3 | `[2,1]` | 2 |
| 5 | 1 | add 1 | 3 | 4 | `[2,1,1]` | 3 |

Final answer: `3`.

The important observation is that once the sum exceeds `5`, advancing `left` is the only repair action needed.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. The dominant work is the single left-to-right scan, plus left-pointer movement during shrink operations. Each element is added to the window once and removed once, so total pointer movement is linear. At `10^6` elements this is routine; at `10^9`, linear is still expensive but fundamentally viable in streaming or chunked systems, unlike quadratic alternatives.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a running sum, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further; the trade-off is already optimal for this problem class.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** segment under a sum constraint, sliding window should be your first candidate.
- The decisive signal here is **non-negative values**; that monotonicity makes two pointers correct and linear.
- Be careful with examples containing invalid explanations: `[2,2,1]` sums to `5`, so for `limit = 4` the answer is `2`, not `3`.
- Update the best length only **after** shrinking until the window is valid; doing it earlier records illegal windows.
- In production systems, monotonic constraints are architectural leverage: they let you replace recomputation-heavy scans with single-pass state machines.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: similar surface area, but the objective flips and often requires prefix sums plus a monotonic deque.
- **Longest subarray with sum at most `K` when negatives are allowed**: sliding window breaks because the sum is no longer monotonic; prefix-sum ordering or balanced structures become necessary.
- **Count of subarrays with sum at most `K`**: same window mechanics, but instead of tracking max length, accumulate how many valid subarrays end at each `right`.