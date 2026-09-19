# Maximum Score from Choosing a Guarded Middle Segment

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Monotonic Stack, Sorting

---

## 🗂 Problem Overview
Given `nums` and a non-negative `penalty`, choose a non-empty contiguous subarray maximizing:

`min(nums[l..r]) * (r - l + 1) - penalty * (# outside elements < min(nums[l..r]))`.

The challenge is that a segment’s minimum determines both its best expandable span and its global penalty contribution. With `n` up to `2e5`, enumerating all subarrays is infeasible. The solution must convert “try every segment” into “evaluate each possible minimum efficiently.”

## 🌍 Engineering Impact
This pattern shows up anywhere a local window score depends on both in-window structure and global distribution. Examples include search ranking windows penalized by stronger out-of-window candidates, streaming anomaly segments scored by weakest confidence with external suppressors, and capacity planning intervals where the bottleneck resource defines throughput while lower-capacity nodes outside the interval reduce viability. At scale, brute-force interval evaluation collapses under quadratic growth. The useful shift is to index decisions by the controlling value—the bottleneck/minimum—then compute its maximal influence region and global count effects once. That enables predictable `O(n log n)` or better behavior under production-sized inputs.

## 🔍 Problem Statement
Given an integer array `nums` of length `n` and an integer `penalty >= 0`, choose one non-empty contiguous subarray `nums[l..r]`. Its score is:

- `minimum(nums[l..r]) * length(nums[l..r])`
- minus `penalty * count(outside elements strictly smaller than that minimum)`.

“Outside” means indices `< l` or `> r`. Return the maximum score over all valid subarrays.

Constraints:

- `1 <= n <= 200000`
- `1 <= nums[i] <= 1000000000`
- `0 <= penalty <= 1000000000`
- Answer fits in signed 64-bit integer

Examples:

- `nums = [5,2,4,3], penalty = 2` → `6`
- `nums = [7,1,6,5,2], penalty = 3` → `10`

The central constraint is `n = 2e5`: `O(n^2)` subarray enumeration is not viable, so the algorithm must exploit structure around subarray minima.

## 🪜 How to Solve This
1. Start from the score formula → the segment is completely characterized by its minimum value and how far that value can remain the minimum.

2. Fix an index `i` as the position of the chosen minimum → what is the best segment whose minimum is `nums[i]`?  
   Obviously, you want the **largest span** around `i` containing no value smaller than `nums[i]`, because larger length always increases the base term and does not increase the count of globally smaller values.

3. That immediately suggests a monotonic-stack view → for each index, find the nearest strictly smaller element on the left and right. Those boundaries define the maximal segment where `nums[i]` can serve as minimum.

4. Now handle the penalty term → for a fixed minimum value `x`, the penalty depends only on how many array elements are globally `< x`, minus those already inside the chosen maximal span.

5. But inside that maximal span, by construction, there are **no** elements `< x`. So every globally smaller element lies outside automatically.

6. Therefore each candidate reduces to:  
   `x * maximal_span_length - penalty * global_count_less_than_x`.

7. Compute span lengths with a monotonic stack, compute `count_less_than` via sorting/compression, evaluate every index, take the maximum.

## 🧩 Algorithm Walkthrough
1. **Compute previous strictly smaller index for every position** using a **monotonic increasing stack**.  
   Pop while `stack.top().value >= nums[i]`, then the new top is the nearest index with value `< nums[i]`.  
   This maintains the invariant that stack values are strictly increasing, so the top is the closest valid smaller boundary.

2. **Compute next strictly smaller index** with a reverse pass and the same rule.  
   Again pop while `>= nums[i]`. The resulting left/right boundaries ensure the open interval `(prevSmaller[i], nextSmaller[i])` contains no element smaller than `nums[i]`.

3. **Derive maximal valid span length** for index `i`:  
   `len = nextSmaller[i] - prevSmaller[i] - 1`.  
   Any subarray containing `i` and staying within these boundaries has minimum at least `nums[i]`; the maximal one is optimal because the penalty term is unchanged and the base term grows with length.

4. **Count globally smaller elements for each value.**  
   Sort a copy of `nums`, deduplicate, and for each `nums[i]` binary-search the first position of that value. That index equals the number of elements strictly smaller than `nums[i]` in the whole array.

5. **Evaluate each index as the controlling minimum**:  
   `score[i] = 1LL * nums[i] * len - 1LL * penalty * lessCount(nums[i])`.  
   This is correct because the maximal span contains no element `< nums[i]`, so every globally smaller element must be outside the segment.

6. **Take the maximum over all indices.**  
   Multiple indices with equal value may produce different span lengths; evaluating all positions preserves correctness.

This is the right abstraction because the problem is not “find a good interval” directly; it is “for each possible bottleneck, find its maximal legal influence region.”

## 📊 Worked Example
Take `nums = [5,2,4,3]`, `penalty = 2`.

| i | nums[i] | prev smaller | next smaller | max len | # global `< nums[i]` | score |
|---|---------|--------------|--------------|---------|----------------------|-------|
| 0 | 5 | -1 | 1 | 1 | 3 | `5*1 - 2*3 = -1` |
| 1 | 2 | -1 | 4 | 4 | 0 | `2*4 - 0 = 8` |
| 2 | 4 | 1 | 3 | 1 | 2 | `4*1 - 4 = 0` |
| 3 | 3 | 1 | 4 | 2 | 1 | `3*2 - 2 = 4` |

Trace notes:

1. For `2` at index `1`, there is no smaller value anywhere, so its maximal span is the whole array.
2. For `3` at index `3`, the previous smaller value is `2`, so the best span is `[4,3]`.
3. The maximum computed score is `8`, from the whole array with minimum `2`.

This trace also exposes a consistency issue in the provided example text: under the stated scoring rule, the whole array scores higher than `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. The two monotonic-stack passes are `O(n)` total because each index is pushed and popped at most once. Counting globally smaller elements via sorting and binary search costs `O(n log n)`, which dominates. At `10^6` elements this is still practical in optimized languages; at `10^9`, neither memory nor sort cost is realistic on a single node.

### Space Complexity
`O(n)`. Space is owned by the left/right boundary arrays, the stack, and the sorted copy used for rank/count queries. You can reduce constants by reusing buffers, but asymptotically it stays linear unless you accept destructive transforms or external-memory techniques.

## 💡 Key Takeaways
- If a subarray score is controlled by its minimum, immediately ask whether each index can be treated as the “chosen minimum” and expanded to a maximal legal span.
- “Nearest smaller on both sides” is a strong signal for a monotonic-stack solution, especially when brute-force interval enumeration is impossible.
- Use **strictly smaller** boundaries; popping on the wrong comparison (`>` vs `>=`) breaks duplicate handling and span ownership.
- The penalty count is global, not local; do not recompute it per segment or accidentally subtract inside-span elements that cannot exist in the maximal span anyway.
- The transferable design insight is to reframe expensive interval search around the bottleneck variable that determines both feasibility and objective contribution.

## 🚀 Variations & Further Practice
- Replace the penalty with `penalty * (# outside elements <= minimum)`. The conceptual twist is duplicate handling: boundary rules and global counts must align exactly on strict vs non-strict comparisons.
- Ask for the top `k` scoring segments instead of just the maximum. The harder part is deduplicating overlapping candidate spans and managing equal-minimum ownership.
- Make the score depend on `minimum * sum(segment)` minus a global penalty. This combines monotonic-stack span logic with prefix sums, and the optimal span is no longer trivially the maximal one for each minimum.