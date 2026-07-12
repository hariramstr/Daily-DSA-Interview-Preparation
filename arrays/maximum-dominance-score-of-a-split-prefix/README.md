# Maximum Dominance Score of a Split Prefix

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane's Algorithm

---

## 🗂 Problem Overview
Given an integer array, choose a split point so both sides are non-empty. For each split, compare the best positive excursion on one side with the worst negative excursion on the other: specifically, `|leftMax - rightMin|` and `|leftMin - rightMax|`. The split’s score is the larger of the two, and the goal is the maximum score across all splits. The challenge is scale: with `n` up to `200,000`, recomputing subarray extrema per split is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must evaluate the strongest upside and downside over every partition of a sequence. In streaming anomaly detection, you may compare worst historical degradation before a cut with best recovery after it. In trading or risk systems, this resembles evaluating maximum drawdown versus rebound across time windows. In ranking and telemetry pipelines, brute-force rescans per boundary create quadratic behavior, which collapses under high-cardinality streams. Precomputing prefix/suffix extrema turns repeated boundary evaluation into a linear pass, enabling predictable latency and memory behavior under production load.

## 🔍 Problem Statement
You are given `nums`, an integer array of length `n`, where `2 <= n <= 200000` and `-10^9 <= nums[i] <= 10^9`. For every split index `i` with `0 <= i < n - 1`, define:

- left part: `nums[0..i]`
- right part: `nums[i+1..n-1]`

For that split, compute:

- `|maxSubarray(left) - minSubarray(right)|`
- `|minSubarray(left) - maxSubarray(right)|`

The split score is the larger of those two values. Return the maximum split score over all valid splits. Subarrays must be non-empty and contiguous, and the chosen subarray on each side can be anywhere within that side.

Examples:

- `nums = [2, -5, 4, -1, 3]` → `12`
- `nums = [7, -2, -6, 5, -1, 4]` → `18`

The key constraint is the number of split points: there are `O(n)` of them, so any `O(n)` or `O(n log n)` work per split is too slow.

## 🪜 How to Solve This
1. Read the definition carefully → every split asks for four values: best and worst subarray sums on the left, and best and worst subarray sums on the right.

2. Notice the repeated work → adjacent splits differ by one element, but a naive solution would recompute max/min subarray sums from scratch for each side. That is `O(n^2)` or worse.

3. Ask what each split really needs → not the exact subarray, only the best prefix-wide answer up to `i` and the best suffix-wide answer from `i+1`.

4. That suggests precomputation → for every index:
   - `leftMax[i]`: maximum subarray sum anywhere in `nums[0..i]`
   - `leftMin[i]`: minimum subarray sum anywhere in `nums[0..i]`
   - `rightMax[i]`: maximum subarray sum anywhere in `nums[i..n-1]`
   - `rightMin[i]`: minimum subarray sum anywhere in `nums[i..n-1]`

5. How to build those arrays efficiently → Kadane’s algorithm already gives maximum subarray sum ending at each position. Its mirrored form gives suffix values, and sign-flipped logic gives minimum subarray sums.

6. Once those four arrays exist, each split is constant-time: compare the two candidate absolute differences and keep the global maximum.

## 🧩 Algorithm Walkthrough
1. **Use dynamic programming with Kadane-style state for prefix maxima.**  
   Maintain `currMaxEndingHere`, the maximum subarray sum that must end at index `i`. Update it as `max(nums[i], currMaxEndingHere + nums[i])`. Then set `leftMax[i] = max(leftMax[i-1], currMaxEndingHere)`.  
   **Invariant:** `leftMax[i]` is the maximum subarray sum anywhere in `nums[0..i]`.

2. **Repeat the same idea for prefix minima.**  
   Maintain `currMinEndingHere = min(nums[i], currMinEndingHere + nums[i])`, then `leftMin[i] = min(leftMin[i-1], currMinEndingHere)`.  
   **Invariant:** `leftMin[i]` is the minimum subarray sum anywhere in `nums[0..i]`.

3. **Mirror both passes from right to left for suffix extrema.**  
   Build `rightMax[i]` and `rightMin[i]` using the same recurrence, but traversing backward so each state refers to subarrays starting at or after `i`.  
   **Invariant:** `rightMax[i]` and `rightMin[i]` summarize all subarrays fully contained in `nums[i..n-1]`.

4. **Scan all split points.**  
   For split after `i`, left side is summarized by `leftMax[i]` and `leftMin[i]`; right side by `rightMax[i+1]` and `rightMin[i+1]`. Compute:
   - `a = abs(leftMax[i] - rightMin[i+1])`
   - `b = abs(leftMin[i] - rightMax[i+1])`
   Update answer with `max(answer, a, b)`.

5. **Why this abstraction fits.**  
   This is classic **Dynamic Programming + Kadane’s Algorithm**: local “best ending here” states roll up into global prefix/suffix extrema. The split scan becomes trivial because all expensive subarray reasoning has already been amortized into four linear passes.

## 📊 Worked Example
Take `nums = [7, -2, -6, 5, -1, 4]`.

| i | leftMax[i] | leftMin[i] | rightMax[i] | rightMin[i] |
|---|------------|------------|-------------|-------------|
| 0 | 7  | 7   | 8  | -8 |
| 1 | 7  | -2  | 8  | -8 |
| 2 | 7  | -8  | 8  | -6 |
| 3 | 7  | -8  | 8  | -1 |
| 4 | 7  | -8  | 4  | -1 |
| 5 | 7  | -8  | 4  | 4  |

Now evaluate splits:

1. After `0`:  
   `|leftMax[0] - rightMin[1]| = |7 - (-8)| = 15`  
   `|leftMin[0] - rightMax[1]| = |7 - 8| = 1`

2. After `2`:  
   `|leftMax[2] - rightMin[3]| = |7 - (-1)| = 8`  
   `|leftMin[2] - rightMax[3]| = |-8 - 8| = 16`

3. After `4`:  
   `|leftMax[4] - rightMin[5]| = |7 - 4| = 3`  
   `|leftMin[4] - rightMax[5]| = |-8 - 4| = 12`

The maximum observed score is `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each of the four DP arrays is built in a single linear pass, and split evaluation is another linear pass. The dominant operation is constant work per element. This scales comfortably to `10^6` elements; at `10^9`, even linear scans become infrastructure-bound and require distributed or external-memory treatment.

### Space Complexity
`O(n)`. The four prefix/suffix arrays own the memory footprint, plus constant-size running state. It can be reduced only partially: you can fuse some passes or stream one side, but you still need enough retained suffix or prefix information to evaluate every split correctly.

## 💡 Key Takeaways
- If every partition asks for an extremum over the entire prefix and suffix, that is a strong signal to precompute prefix/suffix DP summaries rather than rescan.
- When the problem asks for both maximum and minimum subarray sums, think “Kadane twice”: once in max form, once in min form.
- The split is after `i`, so the right side must use index `i + 1`; mixing `right[*][i]` with `left[*][i]` silently allows empty overlap.
- Use 64-bit arithmetic throughout: subarray sums can exceed 32-bit range long before the final answer does.
- The production pattern is broader than arrays: precompute reusable boundary summaries so repeated cut-point evaluation becomes constant-time instead of quadratic.

## 🚀 Variations & Further Practice
- **Circular array split dominance:** allow subarrays to wrap around within each side; the twist is combining standard Kadane with total-sum transforms and careful exclusion logic.
- **Online updates with split queries:** support point updates and repeated dominance queries; the harder part is replacing static prefix/suffix DP with segment trees that carry max/min subarray metadata.
- **k-way partition optimization:** maximize aggregate dominance across multiple cuts; the twist is moving from single-split linear DP to partition DP with significantly larger state space.