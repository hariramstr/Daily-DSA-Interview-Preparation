# Maximum Recharge Gain from One Idle Block

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Kadane's Algorithm, Prefix Sum

---

## 🗂 Problem Overview
Given an integer array `changes`, choose exactly one non-empty contiguous block and flip the sign of every value in that block. Return the maximum possible total array sum after that single operation. The challenge is that flipping a block changes the final total by `-2 * blockSum`, so the optimal block is the one with the minimum subarray sum, not the maximum. With up to `200000` elements, brute-force enumeration of all blocks is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a single contiguous intervention changes aggregate system cost: suppressing one noisy interval in observability pipelines, reweighting one time window in search or ranking signals, reversing one burst in power or thermal telemetry, or applying one rollback window in streaming corrections. At production scale, the difference between `O(n)` and `O(n^2)` is the difference between online analysis and batch-only processing. The useful abstraction is not “find the best segment” in isolation, but “optimize a global metric by applying one contiguous transformation,” which recurs in schedulers, anomaly correction, and financial time-series adjustments.

## 🔍 Problem Statement
You are given an integer array `changes` where `changes[i]` is the net battery change during minute `i`. Positive values mean recharge; negative values mean drain. You must choose exactly one non-empty contiguous block and invert the sign of every element in that block. After this one operation, return the maximum possible total battery change.

Key constraints:

- `1 <= changes.length <= 200000`
- `-100000 <= changes[i] <= 100000`
- Result fits in signed 64-bit integer

Examples:

- `changes = [4, -7, 3, -2]` → `12`
  - Original total = `-2`
  - Flip `[-7]` → `[4, 7, 3, -2]` → total `12`

- `changes = [5, 2, 4]` → `3`
  - Original total = `11`
  - Must flip something
  - Best is flipping `[4]` → `[5, 2, -4]` → total `3`

The scale rules out checking every subarray, which would take quadratic time.

## 🪜 How to Solve This
1. Start with the total sum of the array. That is the baseline before any flip.

2. Ask what flipping a block actually does. If a block has sum `S`, then every `x` becomes `-x`, so the block contribution changes from `S` to `-S`. Net effect on the total is `-2S`.

3. That means maximizing the final total is equivalent to maximizing `total - 2S`. Since `total` is fixed, we need the smallest possible subarray sum.

4. Once the problem becomes “find the minimum-sum contiguous subarray,” the pattern should look familiar: this is Kadane’s algorithm, but inverted. Standard Kadane finds a maximum subarray sum; here we track the minimum ending at each position.

5. Why this works: for each index, the best minimum-sum subarray ending there either extends the previous minimum-ending subarray or starts fresh at the current value.

6. Compute:
   - `totalSum`
   - `minSubarraySum`
   - answer = `totalSum - 2 * minSubarraySum`

7. The “must flip exactly one non-empty block” constraint matters. Even if all numbers are positive, you still must choose one block, so the minimum subarray is simply the smallest element.

## 🧩 Algorithm Walkthrough
1. **Compute the total sum**  
   Traverse the array once and accumulate `totalSum`. This is the unmodified battery change. We need it because the final answer is derived from the original total plus the gain from one flip.

2. **Reframe the optimization**  
   Flipping a contiguous block with sum `S` changes the total by `-2S`. Therefore, the best flip is the block with the **minimum subarray sum**. This is the key reduction: the original problem is an array transformation problem, but the optimization target is a minimum-subarray problem.

3. **Apply Kadane’s Algorithm for minimum subarray**  
   Maintain:
   - `currentMin`: minimum subarray sum ending at the current index
   - `bestMin`: minimum subarray sum seen anywhere so far

   Transition:
   - `currentMin = min(changes[i], currentMin + changes[i])`
   - `bestMin = min(bestMin, currentMin)`

   Invariant: after processing index `i`, `currentMin` is the minimum sum of any non-empty subarray ending at `i`, and `bestMin` is the minimum over all prefixes processed so far.

4. **Derive the final answer**  
   Once `bestMin` is known, compute:
   `answer = totalSum - 2 * bestMin`

   This is correct because flipping the minimum-sum block yields the largest possible increase in total. If `bestMin` is positive, the answer decreases, but that is still required because one non-empty block must be flipped.

5. **Why this abstraction fits**  
   This is **Kadane’s Algorithm** over a transformed objective. The pattern is right because the choice is contiguous, local extension is sufficient, and global optimality can be maintained with a rolling invariant in linear time.

## 📊 Worked Example
Use `changes = [4, -7, 3, -2]`.

| i | value | totalSum | currentMin = min(x, currentMin + x) | bestMin |
|---|------:|---------:|------------------------------------:|--------:|
| 0 | 4     | 4        | 4                                   | 4       |
| 1 | -7    | -3       | min(-7, 4 + -7) = -7               | -7      |
| 2 | 3     | 0        | min(3, -7 + 3) = -4                | -7      |
| 3 | -2    | -2       | min(-2, -4 + -2) = -6              | -7      |

Trace interpretation:

1. Baseline total is `-2`.
2. The minimum subarray sum found is `-7`, corresponding to the block `[-7]`.
3. Flipping that block changes the total by `-2 * (-7) = +14`.
4. Final answer = `-2 + 14 = 12`.

Notice that `[-7, 3, -2]` has sum `-6`, which is good but not optimal. The algorithm correctly keeps the most negative contiguous block seen so far.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. One pass is enough to compute both the total sum and the minimum subarray sum, with constant work per element. At `10^6` elements this is routine in memory-resident processing; at `10^9`, linear time is still expensive but remains the only realistic exact strategy in a streaming or chunked execution model.

### Space Complexity
`O(1)`. The algorithm stores only a few running scalars: total sum, current minimum-ending sum, and global minimum. No auxiliary arrays or prefix buffers are required. You could express it with prefix sums, but that would increase memory without improving asymptotic runtime.

## 💡 Key Takeaways
- If one contiguous block is transformed and the objective is a global sum, first derive how that transformation changes the total algebraically.
- “Choose one block to maximize final score” is often reducible to max/min subarray, especially when the block contributes additively to a global metric.
- The operation is mandatory: do not return the original total when all values are positive; you still must flip one non-empty block.
- Use 64-bit arithmetic for the running total and final answer; `n * value` can exceed 32-bit range comfortably.
- The production-grade insight is to optimize the **delta of an intervention**, not the intervention directly; once the delta is isolated, a generic linear-time primitive often appears.

## 🚀 Variations & Further Practice
- Allow flipping **up to one** contiguous block instead of exactly one. The twist is that the empty choice becomes valid, so the answer is `max(totalSum, totalSum - 2 * minSubarraySum)`.
- Allow flipping **two disjoint** contiguous blocks. The harder part is composing left/right best minima or using DP to track multiple segment choices.
- Replace sign inversion with multiplying one block by an arbitrary factor `k`. The conceptual twist is deriving the new delta formula and then solving the corresponding weighted subarray optimization.