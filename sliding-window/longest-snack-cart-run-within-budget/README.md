# Longest Snack Cart Run Within Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding-window, array, two-pointers

---

## 🗂 Problem Overview
Given an array `prices` of positive integers and an integer `budget`, return the maximum length of a contiguous segment whose total cost is at most `budget`. The output is a single integer: the longest affordable run of consecutive carts. The non-trivial part is scale: `prices.length` can reach `100000`, so checking every possible subarray is too slow. The positivity of all prices is the key property that makes a linear-time sliding window possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous span under a cumulative constraint. In streaming pipelines, it is the largest event window that fits within memory or latency budgets. In distributed rate-limiters, it models the longest burst that stays within token capacity. In media delivery and ad serving, it appears when selecting the longest consecutive segment under bandwidth or spend limits. Without a sliding-window approach, implementations degrade to quadratic scans, which collapse under high-throughput workloads. With it, you get predictable linear behavior, low memory overhead, and a clean primitive for online processing.

## 🔍 Problem Statement
You are given:

- `prices`, an integer array where `prices[i]` is the cost at the `i`-th snack cart
- `budget`, the maximum total amount that can be spent

Find the length of the longest contiguous subarray whose sum is less than or equal to `budget`.

Constraints:

- `1 <= prices.length <= 100000`
- `1 <= prices[i] <= 10000`
- `1 <= budget <= 1000000000`

Examples:

- `prices = [2, 1, 3, 2, 1], budget = 6` → `3`
- `prices = [5, 2, 2, 1, 4], budget = 5` → `2`

If no single cart can be afforded, return `0`.

The decisive constraint is that all `prices[i]` are positive. That means when a window exceeds `budget`, advancing the left boundary can only decrease the sum. That monotonic behavior is exactly what makes a two-pointer sliding window the right algorithmic choice.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so reordering is illegal. That immediately rules out sorting or greedy selection by value.

2. Notice the objective → maximize window length subject to `sum <= budget`. This is a classic “expand while valid, shrink when invalid” shape.

3. Check the element constraints → every `prices[i]` is positive. That matters more than the size limits. With positive numbers, adding a new element can only increase the sum, and removing from the left can only decrease it.

4. That monotonicity suggests a sliding window with two pointers:
   - move the right pointer forward to grow the candidate segment
   - if the sum exceeds `budget`, move the left pointer forward until the segment is valid again

5. After each adjustment, the current window is the longest valid window ending at `right`, so update the best length.

6. This avoids recomputing subarray sums from scratch and turns what would be an `O(n^2)` search into a single linear pass.

## 🧩 Algorithm Walkthrough
1. **Initialize the window state.**  
   Set `left = 0`, `currentSum = 0`, and `maxLen = 0`. The window is the inclusive range `[left, right]` as `right` advances. Invariant: `currentSum` always equals the sum of elements currently inside the window.

2. **Expand the window from the right.**  
   For each index `right`, add `prices[right]` to `currentSum`. This considers every possible subarray ending at `right`. Pattern: **Sliding Window / Two Pointers**. It is the right abstraction because the validity condition depends on a running aggregate over a contiguous range.

3. **Restore validity when over budget.**  
   While `currentSum > budget`, subtract `prices[left]` and increment `left`. This is correct because all prices are positive, so shrinking from the left strictly reduces or preserves the sum and is the only way to make the current window affordable again.

4. **Record the best valid window.**  
   Once the while-loop ends, the window `[left, right]` is valid: `currentSum <= budget`. Its length is `right - left + 1`. Update `maxLen` if this is larger than any previously seen valid window.

5. **Maintain the core invariant.**  
   After processing each `right`, the window is the longest valid suffix ending at `right` that can be reached by only advancing `left`. Because both pointers move forward at most `n` times, the total work is linear.

6. **Return `maxLen`.**  
   If every individual price exceeds `budget`, the window always shrinks to empty and `maxLen` remains `0`, which matches the required behavior.

## 📊 Worked Example
Example: `prices = [2, 1, 3, 2, 1]`, `budget = 6`

| Step | right | prices[right] | currentSum after add | Action while sum > 6 | left | Valid window | maxLen |
|------|-------|---------------|----------------------|----------------------|------|--------------|--------|
| 1 | 0 | 2 | 2 | none | 0 | `[2]` | 1 |
| 2 | 1 | 1 | 3 | none | 0 | `[2,1]` | 2 |
| 3 | 2 | 3 | 6 | none | 0 | `[2,1,3]` | 3 |
| 4 | 3 | 2 | 8 | remove `2` at left | 1 | `[1,3,2]` | 3 |
| 5 | 4 | 1 | 7 | remove `1` at left | 2 | `[3,2,1]` | 3 |

The longest valid window length is `3`. Multiple windows achieve it; the algorithm only needs the maximum length, not the segment itself.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = prices.length`. Each element is added to the window once when `right` advances and removed at most once when `left` advances. There is no nested rescanning. At `10^6` elements this is still practical; at `10^9`, the bottleneck becomes I/O and memory bandwidth rather than algorithmic overhead.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only scalar state: two pointers, a running sum, and the best length. No prefix array or extra data structure is required. You cannot asymptotically reduce below constant extra space without changing the input model.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a threshold and all values are positive, sliding window should be your first candidate.
- “Expand right, shrink left until valid” is the signature pattern when the constraint is monotonic under window contraction.
- Update `maxLen` only **after** restoring `currentSum <= budget`; doing it earlier records invalid windows.
- Be precise about window length: for inclusive pointers, it is `right - left + 1`, not `right - left`.
- In production systems, positivity or monotonicity constraints are what let you replace quadratic search with single-pass online algorithms.

## 🚀 Variations & Further Practice
- **Longest subarray with sum exactly equal to `k`**: harder because the window is no longer monotonic in the same way; often solved with prefix sums and a hash map.
- **Shortest subarray with sum at least `k`**: the objective flips, and with arbitrary integers the solution may require prefix sums plus a monotonic deque.
- **Maximum consecutive requests under weighted capacity with negatives allowed**: the sliding window breaks because shrinking does not reliably improve validity; this forces different state structures and reasoning.