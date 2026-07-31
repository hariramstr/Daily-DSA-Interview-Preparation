# Maximum Product Score from a Fixed-Length Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Prefix Product

---

## 🗂 Problem Overview
Given an integer array `nums` and a fixed window size `k`, find the maximum product among all contiguous subarrays of length exactly `k` that contain no zero. Any window containing a zero is invalid. Return `0` if no valid window exists. The challenge is efficiency: with up to `100000` elements, recomputing each window product from scratch is too expensive, especially when signs flip due to negative values and zeros break multiplicative continuity.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, ranking pipelines, and telemetry systems where a fixed-size rolling score must be maintained under strict latency budgets. Think fraud detection windows, signal quality scoring, or feature extraction in online inference paths. A naive recomputation model turns an `O(n)` pass into `O(nk)`, which collapses under sustained throughput or large window sizes. The sliding-product approach enables single-pass processing, bounded memory, and predictable performance. Handling zeros as hard invalidation points mirrors production realities where missing, corrupt, or sentinel values force segment resets rather than incremental updates.

## 🔍 Problem Statement
You are given an integer array `nums` and an integer `k`. A contiguous window of length exactly `k` is valid only if none of its elements is zero. For each valid window, compute the product of its `k` elements and return the maximum such product as a signed 64-bit integer. If no valid window exists, return `0`.

Constraints:

- `1 <= nums.length <= 100000`
- `-10 <= nums[i] <= 10`
- `1 <= k <= nums.length`
- The maximum valid product fits in signed 64-bit range

Examples:

- `nums = [2, -3, 4, -1, 5], k = 3` → `12`
  - Windows: `[2,-3,4] = -24`, `[-3,4,-1] = 12`, `[4,-1,5] = -20`
- `nums = [0, -2, -3, 4, 0, 5], k = 2` → `6`
  - Valid windows: `[-2,-3] = 6`, `[-3,4] = -12`

The key constraint is scale: evaluating every length-`k` product independently is too slow.

## 🪜 How to Solve This
1. Read the problem → the window length is fixed, so this is immediately a sliding-window problem, not a general subarray search.

2. Notice what makes products tricky → unlike sums, products are not easy to update if a zero appears, because division by zero is impossible and any zero invalidates the whole window.

3. Split the array mentally into zero-free segments → inside such a segment, a window product can be updated incrementally:
   - multiply by the incoming value
   - divide by the outgoing value

4. That suggests the right state:
   - current rolling product
   - count of zeros currently inside the window

5. If zero count is nonzero, the window is invalid and must not contribute to the answer.

6. If zero count is zero and the window size is `k`, the rolling product is exactly the window product, so compare it against the best answer.

7. This avoids recomputing `k` multiplications per window. You pay constant work per element, which is the only viable path at `10^5` scale.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window pattern with a rolling product.**  
   Maintain a window `[left, right]`, a `product` of all non-zero values currently represented in the window, and a `zeroCount` for how many zeros are inside it. This is the right abstraction because the window size is fixed and moves one step at a time.

2. **Expand the window by adding `nums[right]`.**  
   If the incoming value is zero, increment `zeroCount`. Otherwise multiply it into `product`.  
   **Invariant:** `product` equals the product of all non-zero elements currently in the window; zeros are tracked separately.

3. **Shrink when the window exceeds size `k`.**  
   Let `out = nums[left]`, then advance `left`. If `out` is zero, decrement `zeroCount`; otherwise divide `product` by `out`.  
   This is correct because within a valid zero-free window, every outgoing non-zero factor was previously multiplied in exactly once.

4. **Evaluate only windows of exact size `k`.**  
   When `right - left + 1 == k`, the window is fully formed. If `zeroCount == 0`, it is valid and `product` is the exact product score for that window.

5. **Track the maximum valid product.**  
   Initialize the answer as “not found yet.” Update it whenever a valid window appears. If no valid window is ever seen, return `0`.

6. **Why this works at scale.**  
   Each element enters and leaves the window once. No nested recomputation, no prefix-division edge cases across zeros, and no need to materialize segment products separately.

## 📊 Worked Example
Example: `nums = [0, -2, -3, 4, 0, 5], k = 2`

| right | in  | left | window      | product | zeroCount | valid? | best |
|------:|----:|-----:|-------------|--------:|----------:|:------:|-----:|
| 0     | 0   | 0    | `[0]`       | 1       | 1         | No     | —    |
| 1     | -2  | 0    | `[0,-2]`    | -2      | 1         | No     | —    |
| 2     | -3  | 1    | `[-2,-3]`   | 6       | 0         | Yes    | 6    |
| 3     | 4   | 2    | `[-3,4]`    | -12     | 0         | Yes    | 6    |
| 4     | 0   | 3    | `[4,0]`     | 4       | 1         | No     | 6    |
| 5     | 5   | 4    | `[0,5]`     | 5       | 1         | No     | 6    |

The important detail is that `product` only tracks non-zero factors, while `zeroCount` determines whether the current length-`k` window is usable. That separation avoids division-by-zero and makes zero invalidation explicit.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = nums.length`. Each element is processed at most twice: once when it enters the window and once when it leaves. That linear behavior is the dominant cost. At `10^6` elements this remains practical; at `10^9`, the algorithm is still asymptotically optimal but throughput and I/O become the real bottlenecks.

### Space Complexity
`O(1)`. The algorithm stores only scalar state: two pointers, a rolling product, a zero counter, and the best answer. Space cannot be meaningfully reduced further without recomputing products, which would trade memory efficiency for unacceptable time cost.

## 💡 Key Takeaways
- Fixed-size contiguous range + aggregate metric + “move one step at a time” is a strong signal for a sliding-window solution.
- If the aggregate is multiplicative, ask whether you can maintain it incrementally with multiply-on-enter and divide-on-exit.
- Do not divide out an element before checking whether it was zero; zero must be handled through a separate counter.
- Be precise about when the window is evaluated: only after enforcing `windowSize == k`, not while it is still growing or after it has exceeded size `k`.
- In production systems, sentinel values like zero often invalidate incremental state; tracking invalidation explicitly is usually safer than forcing the aggregate structure to encode every edge case.

## 🚀 Variations & Further Practice
- **Maximum product of any subarray with length at most `k`:** harder because the window is no longer fixed, so simple divide-on-exit logic is insufficient.
- **Return the maximum product and the window indices across arrays with many zeros:** same core pattern, but now segment boundaries and tie-breaking rules matter.
- **2D version over a matrix with fixed-size rectangles:** extends the idea into rolling products across rows and columns, with zero handling becoming substantially more complex.