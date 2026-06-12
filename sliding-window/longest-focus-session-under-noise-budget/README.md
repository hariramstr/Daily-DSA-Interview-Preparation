# Longest Focus Session Under Noise Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `noise` of non-negative integers and an integer `budget`, find the maximum length of a contiguous subarray whose sum is at most `budget`. The output is a single integer: the longest valid study window. The challenge is efficiency: `noise.length` can reach `100000`, so brute-force enumeration of all subarrays is too slow. The non-negative values are the key structural constraint that makes a linear sliding-window solution possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval under a cumulative resource cap: API request bursts under rate budgets, streaming jobs bounded by memory or latency envelopes, media buffering under bandwidth constraints, and observability pipelines limiting per-tenant ingestion over time windows. At scale, naive range enumeration collapses under quadratic behavior and destroys tail latency. The sliding-window formulation exploits monotonicity from non-negative inputs, enabling single-pass processing with constant auxiliary space. That matters operationally: predictable throughput, cache-friendly scans, and simpler online processing where data arrives incrementally rather than as a fully materialized batch.

## 🔍 Problem Statement
You are given:

- `noise`, where `noise[i]` is the noise level during minute `i`
- `budget`, the maximum total noise the student can tolerate in one uninterrupted study block

Return the maximum number of consecutive minutes such that the sum of the chosen contiguous block is `<= budget`.

Constraints:

- `1 <= noise.length <= 100000`
- `0 <= noise[i] <= 10000`
- `0 <= budget <= 1000000000`

Examples:

- `noise = [2, 1, 3, 2, 1], budget = 5` → `2`
- `noise = [0, 2, 0, 1, 1, 0], budget = 3` → `4`

Edge cases matter:

- If every single value exceeds `budget`, return `0`
- Zero-valued minutes can extend a valid window without increasing the sum
- The decisive constraint is that all `noise[i]` are non-negative, which makes shrinking the window monotonic and enables a linear-time sliding-window approach

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous block**, so this is immediately about subarrays, not sorting or arbitrary selection.

2. Notice the condition is on the **sum of the current block**: sum must stay `<= budget`. That suggests maintaining a running window rather than recomputing sums repeatedly.

3. Check the value domain → all `noise[i]` are **non-negative**. That is the unlock. If expanding the window makes the sum too large, moving the left boundary rightward can only decrease or preserve the sum. No backtracking is needed.

4. That monotonic behavior points directly to **sliding window / two pointers**:
   - expand right to include more minutes
   - while the sum is too large, shrink from the left
   - after revalidating, record the current window length

5. Why not prefix sums plus binary search? Possible, but unnecessary. With non-negative values, a single pass is simpler and strictly better in constant factors.

6. The mental model: maintain the largest valid window ending at each right index. Because left only moves forward, every element is added once and removed once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and state**  
   Set `left = 0`, `windowSum = 0`, and `maxLen = 0`.  
   This defines the current window as `noise[left..right]`. The invariant is: after adjustment, this window is always valid, meaning `windowSum <= budget`.

2. **Expand the window with the right pointer**  
   For each `right` from `0` to `n - 1`, add `noise[right]` to `windowSum`.  
   This explores every possible window ending at `right`. We deliberately over-expand first, then repair if needed.

3. **Shrink while invalid**  
   While `windowSum > budget`, subtract `noise[left]` and increment `left`.  
   This is correct because all values are non-negative. Removing elements from the left is the only operation needed to restore validity; once valid, any earlier `left` would still violate the budget.

4. **Record the best valid length**  
   After shrinking, the current window `left..right` is the longest valid window ending at `right`, because we only moved `left` as far as necessary. Update `maxLen = max(maxLen, right - left + 1)`.

5. **Return the result**  
   When the scan completes, `maxLen` is the maximum valid contiguous length over the entire array.

This is the **Sliding Window / Two Pointers** pattern. It is the right abstraction because the feasibility of a window changes monotonically under expansion and contraction when all elements are non-negative.

## 📊 Worked Example
Example: `noise = [0, 2, 0, 1, 1, 0]`, `budget = 3`

| right | noise[right] | windowSum after add | left after shrink | valid window     | length | maxLen |
|------:|-------------:|--------------------:|------------------:|------------------|-------:|-------:|
| 0     | 0            | 0                   | 0                 | `[0]`            | 1      | 1      |
| 1     | 2            | 2                   | 0                 | `[0,2]`          | 2      | 2      |
| 2     | 0            | 2                   | 0                 | `[0,2,0]`        | 3      | 3      |
| 3     | 1            | 3                   | 0                 | `[0,2,0,1]`      | 4      | 4      |
| 4     | 1            | 4                   | 2                 | `[0,1,1]`        | 3      | 4      |
| 5     | 0            | 2                   | 2                 | `[0,1,1,0]`      | 4      | 4      |

At `right = 4`, the sum exceeds the budget, so we shrink from the left: remove `0`, still `4`; remove `2`, now `2`. The window becomes valid again. Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = noise.length`. Each element is added to the window once by the right pointer and removed at most once by the left pointer. There is no nested rescanning. At `10^6` elements this remains practical; at `10^9`, the algorithm is still optimal asymptotically but likely constrained by I/O and memory bandwidth.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, a running sum, and the best length. No extra arrays or maps are required. Space cannot be meaningfully reduced further without changing the execution model; the trade-off is already minimal.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous subarray** under a threshold and all values are non-negative, sliding window should be your first candidate.
- When feasibility is restored by only moving one boundary forward, that monotonicity is the signal that two pointers will beat prefix-sum brute force.
- Update `maxLen` only **after** shrinking the window back to validity; doing it before that records illegal windows.
- Be careful with the window length formula: for inclusive boundaries it is `right - left + 1`, not `right - left`.
- In production systems, this pattern is valuable because monotonic constraints often let you replace quadratic range search with stable single-pass online processing.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: similar range-sum reasoning, but the optimization target flips and often requires prefix sums plus a monotonic deque.
- **Longest subarray with at most `K` distinct values**: still sliding window, but validity depends on frequency state rather than a scalar sum.
- **Longest subarray under budget with negative values allowed**: the monotonic property breaks, so standard sliding window no longer works; this pushes you toward prefix-sum data structures or more advanced techniques.