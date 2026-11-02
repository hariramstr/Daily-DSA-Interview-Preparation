# Maximum Charge Saved by Skipping One Noisy Sensor Block

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Kadane's Algorithm, Prefix Sum

---

## 🗂 Problem Overview
Given an integer array `readings`, compute the maximum possible sum of elements that remain after optionally removing exactly one contiguous block. Removing nothing is allowed; removing the entire array is also valid. The output is the best achievable remaining total. The challenge is scale: with up to `200000` elements, any brute-force enumeration of all removable intervals is too slow, so the solution must exploit array structure and run in linear time.

## 🌍 Engineering Impact
This pattern shows up in streaming telemetry, observability pipelines, sensor fusion, and financial event processing where a contiguous segment is known or suspected to be corrupted, noisy, duplicated, or adversarial. In production, you rarely want “best subarray kept”; you want “best total after excising one bad window.” Without a linear-time approach, remediation logic becomes too expensive for high-volume streams or online scoring paths. Reframing the problem as total sum minus worst contiguous contribution enables bounded-latency processing, predictable memory use, and clean integration into batch and streaming architectures.

## 🔍 Problem Statement
You are given an integer array `readings` where `readings[i]` is the battery impact of processing the `i`-th sensor reading. Positive values add charge; negative values consume it. You must process the stream in order, but you may skip one contiguous block `readings[l...r]`, removing its contribution entirely. Skipping nothing is allowed, and skipping the full array is also allowed.

Return the maximum possible sum of the remaining elements.

Constraints:

- `1 <= readings.length <= 200000`
- `-1000000000 <= readings[i] <= 1000000000`
- Result fits in signed 64-bit integer

Examples:

- `readings = [4, -7, 3, 6, -2, 5]` → `16`
- `readings = [-5, -2, -8]` → `0`

The key constraint is input size: `O(n^2)` interval enumeration is infeasible, so the algorithm must run in `O(n)` time.

## 🪜 How to Solve This
1. Start from what the result actually is: sum of everything **outside** one removed block.
2. That means  
   `remaining_sum = total_sum - removed_block_sum`.
3. If `total_sum` is fixed, maximizing the remaining sum is equivalent to minimizing the sum of the block we remove.
4. So the problem is not “what should we keep?” but “what is the minimum-sum contiguous subarray we should delete?”
5. That immediately suggests a Kadane-style scan, but inverted: instead of tracking the maximum subarray sum, track the minimum subarray sum ending at each position.
6. Then compare two choices:
   - remove nothing → keep `total_sum`
   - remove the worst block → keep `total_sum - min_subarray_sum`
7. This also handles deleting the full array. If all values are negative, the minimum-sum subarray is the whole array, so the answer becomes `0`.
8. The mental model is simple: one pass to compute the total, one pass to find the most harmful contiguous segment, then subtract it if beneficial.

## 🧩 Algorithm Walkthrough
1. **Compute the total sum of the array.**  
   This establishes the baseline where no block is removed. Use 64-bit accumulation because `n * value` can exceed 32-bit range.

2. **Run a minimum-subarray variant of Kadane’s Algorithm.**  
   Maintain:
   - `minEndingHere`: minimum sum of a contiguous subarray that must end at the current index
   - `minSoFar`: minimum sum of any contiguous subarray seen so far

   Transition:
   - `minEndingHere = min(readings[i], minEndingHere + readings[i])`
   - `minSoFar = min(minSoFar, minEndingHere)`

   This is the same dynamic programming pattern as Kadane’s Algorithm, but inverted from max to min.

3. **Why this is correct.**  
   At each index, the minimum-sum subarray ending there either:
   - starts fresh at the current element, or
   - extends the previous minimum-sum suffix  
   That invariant guarantees every contiguous block is considered exactly once through its ending position.

4. **Convert removed-block cost into kept total.**  
   Candidate answers:
   - keep everything: `totalSum`
   - remove the worst block: `totalSum - minSoFar`

5. **Return the larger of the two.**  
   If `minSoFar` is positive, removing any block would reduce the result, so keeping everything wins. If `minSoFar` is negative, removing it increases the remaining total. If the whole array is negative, `minSoFar == totalSum`, so the result is `0`.

This is the right abstraction because the problem is fundamentally **prefix-local dynamic programming over contiguous ranges**, not combinatorial search.

## 📊 Worked Example
Example: `readings = [4, -7, 3, 6, -2, 5]`

| i | value | totalSum | minEndingHere | minSoFar |
|---|------:|---------:|--------------:|---------:|
| 0 | 4     | 4        | 4             | 4        |
| 1 | -7    | -3       | -7            | -7       |
| 2 | 3     | 0        | -4            | -7       |
| 3 | 6     | 6        | 2             | -7       |
| 4 | -2    | 4        | -2            | -7       |
| 5 | 5     | 9        | 3             | -7       |

Trace:
1. Total array sum is `9`.
2. The minimum-sum contiguous block found is `[-7]`, so `minSoFar = -7`.
3. Removing that block yields `9 - (-7) = 16`.
4. Compare with removing nothing: `max(9, 16) = 16`.

Answer: `16`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. The dominant work is a single linear scan that updates constant-sized state per element. This scales comfortably to `10^6` elements in memory-resident workloads. At `10^9` elements, the algorithmic shape is still optimal, but throughput becomes an I/O and streaming-systems concern rather than a CPU one.

### Space Complexity
`O(1)`. The algorithm stores only running totals and Kadane state; no prefix array is required. You could express it with explicit prefix sums, but that increases memory to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If a problem asks for the best result after removing one contiguous block, translate it to `total sum - worst block`; that is a strong signal for min-subarray reasoning.
- “Optionally skip one segment” usually means Kadane’s Algorithm or prefix-sum optimization is nearby, even when the prompt is not phrased as a subarray problem.
- Do not force removal: compare against the baseline `totalSum`, or you will return the wrong answer when every removable block has positive sum.
- Use 64-bit arithmetic throughout; with `200000` elements and values up to `1e9`, 32-bit accumulation will overflow.
- In production, this is a recurring design move: optimize a global objective by isolating and excising the single worst contiguous anomaly instead of rebuilding the whole computation.

## 🚀 Variations & Further Practice
- **Remove up to `k` disjoint contiguous blocks**: the twist is interval budgeting; the problem becomes dynamic programming over segment count rather than a single Kadane pass.
- **Remove exactly one block of length at least `L` or at most `L`**: now the minimum-subarray search must respect window constraints, typically requiring prefix sums plus a monotonic structure.
- **Maximum subarray sum with one deletion**: related but different; you are optimizing a kept contiguous region, not the total outside a removed region, so the DP state and invariants change.