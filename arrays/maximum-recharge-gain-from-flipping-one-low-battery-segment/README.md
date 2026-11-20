# Maximum Recharge Gain from Flipping One Low-Battery Segment

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Kadane's Algorithm, Dynamic Programming

---

## 🗂 Problem Overview
Given a binary array `status`, flip exactly one non-empty contiguous segment and return the maximum possible count of `1`s afterward. Flipping turns `0 -> 1` and `1 -> 0`, so the chosen segment can both help and hurt. The non-trivial part is the mandatory flip: even an all-`1` array must lose at least one healthy minute. With `n` up to `2 * 10^5`, brute-force enumeration of all subarrays is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a single contiguous intervention changes a metric with mixed positive and negative local effects: retry-window tuning in streaming pipelines, temporary rule inversion in rate-limiters, feature-flag rollback windows, or post-processing spans in telemetry repair. At scale, the difference between evaluating every interval and reducing the problem to a linear scan is the difference between an online decision and an offline batch job. The key architectural move is converting a state mutation into a gain/loss array, then optimizing over that derived representation with a stable, single-pass primitive.

## 🔍 Problem Statement
You are given a binary array `status` of length `n`, where `1 <= n <= 2 * 10^5` and each element is either `0` or `1`. `status[i] = 1` means minute `i` was healthy; `status[i] = 0` means low-battery. You must apply exactly one recovery patch to one non-empty contiguous subarray, flipping every bit in that segment.

Return the maximum possible number of healthy minutes after the flip.

Key edge case: if the array is all `1`s, you still must flip something, so the answer is `n - 1`.

Examples:

- `status = [1,0,0,1,0,1]` → `5`
- `status = [1,1,1,1]` → `3`

The constraint that drives the algorithmic choice is the input size: `O(n^2)` subarray enumeration is too slow, so the solution must be linear or close to it.

## 🪜 How to Solve This
1. Start from what the flip changes, not from the final array. A flipped `0` is good: it adds one healthy minute. A flipped `1` is bad: it removes one healthy minute.

2. Translate each position into a gain value:
   - `0` becomes `+1`
   - `1` becomes `-1`

3. Now the problem is simpler: choose one non-empty contiguous segment whose total gain is as large as possible.

4. That is exactly the maximum subarray problem. Once you see “contiguous segment” plus “maximize total contribution,” Kadane’s algorithm should be the default candidate.

5. Count the original number of `1`s once. Then compute the best possible gain from one required flip. Final answer = `originalOnes + maxGain`.

6. The “exactly once” requirement matters: we are not allowed to skip the operation. So unlike some Kadane variants, we must select a non-empty subarray even if every gain is negative. That means initialization must preserve the possibility of an all-negative best segment.

## 🧩 Algorithm Walkthrough
1. **Count baseline healthy minutes.**  
   Compute `base = number of 1s in status`. This is the result before any patch. Every valid answer is this baseline plus the net effect of the chosen flip.

2. **Map the array to gains.**  
   For each element:
   - if `status[i] == 0`, gain is `+1`
   - if `status[i] == 1`, gain is `-1`  
   This transformation is correct because flipping affects only the healthy-minute count, and each position contributes independently to that delta.

3. **Run Kadane’s algorithm on the gain stream.**  
   Maintain:
   - `current`: best gain of a subarray ending at the current index
   - `best`: best gain seen anywhere so far  
   Update with `current = max(gain, current + gain)`, then `best = max(best, current)`.

4. **Preserve the non-empty-subarray invariant.**  
   Initialize `current` and `best` from the first transformed value, not `0`. This is the critical detail that enforces “flip exactly once.” If all gains are `-1`, the algorithm returns `-1`, meaning flip one `1` and lose one healthy minute.

5. **Return `base + best`.**  
   This is optimal because Kadane finds the contiguous segment with maximum total gain, and every possible flip corresponds to exactly one contiguous gain subarray.

This is a classic **Kadane’s Algorithm / Dynamic Programming** reduction: local per-element effect plus contiguous optimization in one pass.

## 📊 Worked Example
Take `status = [1,0,0,1,0,1]`.

Baseline healthy minutes: `base = 3`  
Gain array: `[-1, +1, +1, -1, +1, -1]`

| i | status[i] | gain | current = max(gain, current+gain) | best |
|---|-----------|------|-----------------------------------|------|
| 0 | 1 | -1 | -1 | -1 |
| 1 | 0 | +1 | max(1, 0) = 1 | 1 |
| 2 | 0 | +1 | max(1, 2) = 2 | 2 |
| 3 | 1 | -1 | max(-1, 1) = 1 | 2 |
| 4 | 0 | +1 | max(1, 2) = 2 | 2 |
| 5 | 1 | -1 | max(-1, 1) = 1 | 2 |

Best gain is `2`, so final answer is `base + best = 3 + 2 = 5`.

One optimal segment is indices `1..4`: flipping `[0,0,1,0]` yields `[1,1,1,0,1,1]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. We do one pass to count baseline `1`s and one pass-equivalent Kadane scan over the same elements; these can even be fused into a single traversal. At `10^6` elements this is routine. At `10^9`, linear work is still expensive but remains the only realistic asymptotic class for exact evaluation.

### Space Complexity
`O(1)`. The algorithm needs only a few scalar variables: baseline count, current gain, and best gain. No auxiliary array is required if gains are computed on the fly. You can materialize the gain array for debugging, but that trades constant space for observability, not correctness.

## 💡 Key Takeaways
- If a problem asks for one contiguous modification and each element contributes an independent delta, convert the operation into a gain/loss array first.
- “Find the best contiguous segment” after such a transformation is a strong signal for Kadane’s algorithm rather than interval enumeration.
- The flip is mandatory, so do not initialize Kadane with `0`; that would incorrectly allow choosing no segment.
- In an all-`1` array, the answer is `n - 1`, not `n`; this is the easiest edge case to get wrong in review.
- The transferable design insight is to optimize over the effect surface of an action, not over the raw state mutation itself.

## 🚀 Variations & Further Practice
- Allow flipping **at most once** instead of exactly once. The twist is that the empty operation becomes legal, so Kadane initialization and final clamping behavior change.
- Allow flipping **up to `k` disjoint segments**. This becomes a segmented dynamic programming problem rather than a single maximum-subarray scan.
- In a 2D grid of battery states, flip one **rectangular region** to maximize healthy cells. The same gain transformation applies, but the optimization becomes maximum-sum submatrix.