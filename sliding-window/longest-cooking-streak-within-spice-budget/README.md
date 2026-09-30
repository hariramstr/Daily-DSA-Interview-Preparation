# Longest Cooking Streak Within Spice Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given a non-negative integer array `heat` and an integer `budget`, find the maximum length of any contiguous subarray whose sum is at most `budget`. The output is a single integer: the longest valid streak length. The challenge is scale: `n` can reach `200000`, so brute-force enumeration of all subarrays is too expensive. The key structural property is that all values are non-negative, which makes a linear-time sliding window possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval under a cumulative resource cap: streaming pipelines enforcing byte or latency budgets, rate-limiters finding the largest admissible burst window, ad-serving systems packing impressions under spend constraints, and observability backends grouping events under memory thresholds. At scale, naive range recomputation or nested scans collapse under throughput and tail-latency pressure. The sliding-window formulation turns a quadratic search into a single pass with constant auxiliary space, which is exactly the difference between something that works in a notebook and something that survives production traffic.

## 🔍 Problem Statement
You are given an array `heat` of length `n`, where `heat[i]` is the spice level of the `i`-th dish in cooking order, and an integer `budget`. Return the maximum length of a contiguous subarray whose sum is less than or equal to `budget`.

Constraints:

- `1 <= n <= 200000`
- `0 <= heat[i] <= 100000`
- `0 <= budget <= 10^15`
- The answer fits in a 32-bit signed integer

Examples:

- `heat = [2, 1, 3, 2, 1, 1], budget = 5` → `3`
- `heat = [0, 4, 0, 2, 1, 0, 1], budget = 3` → `4`

Edge cases matter: zeros can extend a valid window without increasing the sum, `budget` can be `0`, and individual elements may already exceed the budget. The decisive constraint is non-negative values: once a window exceeds budget, moving the left boundary right is the only way to reduce the sum predictably.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is about ranges, not subsets or sorting.

2. Notice the objective is “longest window with sum ≤ budget.” That usually suggests either prefix sums + binary search or a sliding window.

3. Check the array values → all `heat[i]` are non-negative. That is the signal that makes sliding window the right first choice:
   - expanding the window to the right never decreases the sum
   - shrinking from the left never increases the sum

4. Start with both pointers at the left and maintain the current window sum.

5. Expand the right pointer one dish at a time. If the sum stays within budget, this window is valid, so update the best length.

6. If the sum exceeds budget, shrink from the left until the window becomes valid again. Because values are non-negative, once valid, every shorter left-trimmed version was necessary to restore feasibility.

7. Each element enters the window once and leaves once, so the total work is linear. That is the core reason this approach scales cleanly.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use the **Sliding Window / Two Pointers** pattern. Set `left = 0`, `currentSum = 0`, and `best = 0`. The active window is always `heat[left..right]`.

2. **Advance the right pointer**  
   For each `right` from `0` to `n - 1`, add `heat[right]` to `currentSum`. This represents extending the contiguous streak by one dish.

3. **Restore validity when over budget**  
   While `currentSum > budget`, subtract `heat[left]` from `currentSum` and increment `left`. This is correct because all values are non-negative: removing items from the left is the only monotonic way to reduce the sum without skipping elements.

4. **Record the current valid window**  
   After the shrink loop, the invariant is: `currentSum <= budget`, and `heat[left..right]` is the longest valid window ending at `right` with the current `left`. Update `best = max(best, right - left + 1)`.

5. **Why the invariant is enough**  
   For every `right`, once the window is valid, any window starting further right would be shorter. So the current window is the best candidate ending at `right`.

6. **Why this abstraction fits**  
   Two Pointers works here because feasibility is monotonic under pointer movement. That property disappears if negative values are allowed; then expanding right could reduce the sum, and the simple window invariant breaks.

7. **Implementation detail**  
   Use a 64-bit accumulator for `currentSum`. With `n = 200000` and `heat[i] = 100000`, the sum can exceed 32-bit integer range even though the final answer does not.

## 📊 Worked Example
Example: `heat = [0, 4, 0, 2, 1, 0, 1]`, `budget = 3`

| right | heat[right] | currentSum after add | action while `> 3` | left | valid window | best |
|---|---:|---:|---|---:|---|---:|
| 0 | 0 | 0 | none | 0 | `[0]` | 1 |
| 1 | 4 | 4 | remove `0`, remove `4` | 2 | `[]` | 1 |
| 2 | 0 | 0 | none | 2 | `[0]` | 1 |
| 3 | 2 | 2 | none | 2 | `[0,2]` | 2 |
| 4 | 1 | 3 | none | 2 | `[0,2,1]` | 3 |
| 5 | 0 | 3 | none | 2 | `[0,2,1,0]` | 4 |
| 6 | 1 | 4 | remove `0`, remove `2` | 4 | `[1,0,1]` | 4 |

The maximum valid streak length is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is added to `currentSum` once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing despite the inner `while` loop. At `10^6` elements this is still practical; at `10^9`, linear scan cost becomes the dominant systems concern.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, the running sum, and the best length. Space cannot be meaningfully reduced further without changing the model; the main trade-off is using a wider integer type for correctness, not extra memory.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous range** under a threshold and all values are **non-negative**, sliding window should be your default candidate.
- The strongest recognition signal is monotonic feasibility: extending right can only worsen or preserve the constraint, and shrinking left can only improve it.
- Use a 64-bit running sum; the answer fits in 32 bits, but the intermediate window sum may not.
- Update the best length only **after** shrinking back to a valid window; doing it before the `while` loop creates invalid-length results.
- In production, this pattern matters because monotonic constraints let you replace repeated range evaluation with a single-pass state machine, which is exactly how high-throughput stream processing stays predictable.

## 🚀 Variations & Further Practice
- **Allow negative numbers in `heat`**: the simple sliding window fails because sum is no longer monotonic; this pushes you toward prefix sums with balanced trees, deques, or binary-searchable structures.
- **Count how many subarrays have sum `<= budget`**: same window idea, but instead of maximizing length, accumulate the number of valid endings at each `right`.
- **Longest subarray with at most `k` distinct values or bounded max-min difference**: still sliding window, but validity now depends on frequency maps or monotonic deques rather than a single running sum.