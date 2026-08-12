# Longest Fruit Basket Refill Under Weight Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding-window, array, two-pointers

---

## 🗂 Problem Overview
Given an array of positive fruit weights and a basket capacity `maxWeight`, find the maximum length of any contiguous subarray whose total weight does not exceed the limit. The output is only the length, not the subarray itself. The non-trivial part is scale: with up to `100000` elements, brute-force enumeration of all subarrays is too slow, so the solution must exploit the positivity constraint to stay linear.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest valid contiguous span under a cumulative budget: stream processors batching events under payload limits, network stacks coalescing packets under MTU-like constraints, ad servers selecting contiguous impression windows under spend caps, and log ingestion systems grouping records under memory thresholds. At scale, naive rescanning creates quadratic behavior, cache churn, and latency spikes. The sliding-window formulation turns a potentially explosive search space into a single pass with predictable memory use, which matters for hot-path services and high-throughput data pipelines.

## 🔍 Problem Statement
You are given an integer array `weights` where `weights[i]` is the weight of the `i`th fruit on a conveyor belt, and an integer `maxWeight` representing the basket’s capacity. You must choose one contiguous group of fruits, preserving order and without skipping elements, such that the total weight is at most `maxWeight`. Return the maximum possible length of such a group.

Constraints:

- `1 <= weights.length <= 100000`
- `1 <= weights[i] <= 10000`
- `1 <= maxWeight <= 1000000000`

Examples:

- `weights = [2, 1, 3, 2, 1]`, `maxWeight = 5` → `2`
- `weights = [1, 1, 1, 1, 2]`, `maxWeight = 4` → `4`

The decisive constraint is that all weights are positive integers. That monotonicity makes it safe to expand a window and shrink it only when the sum exceeds the limit, enabling a linear-time solution.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is immediately a windowing problem, not subset selection or prefix-sum lookup alone.

2. Notice the objective → maximize window length while keeping `sum <= maxWeight`. That usually suggests trying to grow a range as far as possible.

3. Check the data property → all weights are **positive**. This is the key unlock. If adding a new fruit makes the sum too large, adding even more fruits will never fix it; the only repair is to remove items from the left.

4. That leads directly to a **sliding window / two-pointer** approach:
   - move the right pointer forward one step at a time,
   - add the new weight to the running sum,
   - while the sum is too large, move the left pointer forward and subtract weights,
   - after revalidating the window, update the best length.

5. Why this is efficient → each element enters the window once and leaves once. No nested rescans, no recomputing subarray sums, no quadratic explosion.

6. The positivity constraint is what makes this “obvious” in hindsight. Without it, shrinking the left side would not be enough to restore monotonic behavior.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `currentSum = 0`, and `best = 0`.  
   This window `[left, right]` will always represent the current candidate segment.

2. **Expand with the right pointer**  
   Iterate `right` from `0` to `weights.length - 1`. Add `weights[right]` to `currentSum`.  
   This is the “grow” phase of the **Sliding Window / Two Pointers** pattern.

3. **Restore validity when overweight**  
   While `currentSum > maxWeight`, subtract `weights[left]` from `currentSum` and increment `left`.  
   This is correct because all values are positive: removing items from the left is the only way to reduce the sum, and once the window is valid again, any earlier `left` would still be invalid.

4. **Update the answer after revalidation**  
   Once `currentSum <= maxWeight`, compute the current window length as `right - left + 1` and update `best` if larger.  
   The invariant here is: after the shrink loop, `[left, right]` is the longest valid window ending at `right`.

5. **Finish after one pass**  
   Return `best`.  
   The abstraction fits because the feasibility condition is monotonic under positive numbers: expanding can only increase sum, shrinking can only decrease it. That monotonicity is exactly what makes two pointers the right tool rather than prefix-sum plus binary search or brute force.

## 📊 Worked Example
Example: `weights = [2, 1, 3, 2, 1]`, `maxWeight = 5`

| right | weights[right] | currentSum after add | shrink? | left after shrink | valid window | length | best |
|---|---:|---:|---|---:|---|---:|---:|
| 0 | 2 | 2 | no | 0 | `[2]` | 1 | 1 |
| 1 | 1 | 3 | no | 0 | `[2,1]` | 2 | 2 |
| 2 | 3 | 6 | yes: remove 2 | 1 | `[1,3]` | 2 | 2 |
| 3 | 2 | 6 | yes: remove 1, then 3 | 3 | `[2]` | 1 | 2 |
| 4 | 1 | 3 | no | 3 | `[2,1]` | 2 | 2 |

The important detail is that shrinking continues until the window is valid again. After each `right` expansion, the algorithm keeps the longest valid suffix ending at that index. The maximum length seen across all such suffixes is the answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = weights.length`. The right pointer advances `n` times, and the left pointer also advances at most `n` times total, so each element is processed a constant number of times. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally better than any quadratic alternative.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, a running sum, and the best length. No extra arrays or maps are required. Space cannot be meaningfully reduced further without changing the input model or sacrificing single-pass behavior.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a cumulative constraint, sliding window should be your first check.
- Positive-only values are a strong signal that a two-pointer window can maintain validity monotonically.
- Update the answer **after** shrinking to a valid state; doing it before revalidation records illegal windows.
- Be precise about window length: for inclusive pointers, it is `right - left + 1`, not `right - left`.
- In production systems, monotonic constraints are leverage: they turn expensive global search into stable single-pass streaming logic.

## 🚀 Variations & Further Practice
- **Longest subarray with sum exactly `k`**: harder because positivity may not be enough, and with negative numbers you typically switch to prefix sums plus a hash map.
- **Minimum-size subarray with sum at least `target`**: same sliding-window pattern, but the optimization objective flips from maximizing valid length to minimizing it.
- **Longest subarray with at most `k` distinct values**: still sliding window, but validity depends on frequency state rather than a scalar sum, requiring a hash map and careful eviction logic.