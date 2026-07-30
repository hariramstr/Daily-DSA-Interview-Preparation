# Minimum Merges to Form a Mountain Array

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Interval DP

---

## 🗂 Problem Overview
Given an array `nums`, merge adjacent elements until the resulting array becomes a valid mountain: strictly increasing to one internal peak, then strictly decreasing. Each final element is therefore the sum of a contiguous block of the original array. Return the minimum number of merges, or `-1` if no valid mountain can be formed. The difficulty is that local merge choices change future segment sums, so greedy decisions are unreliable and the search space is combinatorial.

## 🌍 Engineering Impact
This pattern shows up anywhere local aggregation changes downstream shape constraints. Examples include streaming pipelines that coalesce adjacent windows, compiler optimizers that fold instruction ranges, storage engines that compact neighboring segments, and ranking systems that bucket contiguous score bands before enforcing monotonicity constraints. At scale, naive search over all merge sequences explodes because every local combine creates a new state space. Interval DP matters because it reasons over contiguous blocks directly, turning “which merge order?” into “which partitioning is feasible?” That shift is what makes correctness tractable and latency predictable under bounded input sizes.

## 🔍 Problem Statement
You are given an integer array `nums` with `3 <= nums.length <= 200` and `1 <= nums[i] <= 10^6`. In one operation, you may merge any adjacent pair into a single element equal to their sum. After enough merges, each remaining element represents the sum of some contiguous subarray of the original input.

The task is to produce a mountain array with the fewest merges. A valid mountain must have length at least `3`, a peak index `p` with `0 < p < length - 1`, strictly increasing values on the left, and strictly decreasing values on the right.

Return the minimum number of merges required, or `-1` if impossible.

Examples:

- `nums = [1, 2, 1]` → `0`
- `nums = [2, 1, 1, 2]` → `-1`

The key constraint is `n <= 200`: small enough for cubic or quartic dynamic programming, too large for brute-force enumeration of merge sequences.

## 🪜 How to Solve This
1. Read the merge rule → every final value is a sum of a contiguous block. That means the real decision is not merge order, but how to partition the array into contiguous segments.

2. Reframe the target → after partitioning, the segment sums must form a mountain. So we need the partition with the maximum number of segments whose sums are strictly increasing then strictly decreasing.

3. Connect merges to segments → if the final array has `k` elements, we performed exactly `n - k` merges. Minimizing merges is equivalent to maximizing valid mountain length.

4. Notice the structure → segment sums depend on intervals, and the mountain condition depends on relative comparisons between neighboring chosen intervals. That is classic interval DP territory.

5. Split the problem at the peak → for each possible peak interval, compute the longest strictly increasing chain of contiguous segments ending at it from the left, and the longest strictly decreasing chain starting from it on the right.

6. Combine left and right lengths around each peak interval. If both sides exist, we have a valid mountain. Keep the best total segment count, then convert back to merges.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums.**  
   Use `prefix[i+1] = prefix[i] + nums[i]` so any interval sum `sum(l, r)` is `prefix[r+1] - prefix[l]` in `O(1)`. This is mandatory because interval comparisons happen repeatedly. The invariant is that every contiguous block sum is available in constant time.

2. **Define interval DP states.**  
   Let `inc[l][r]` be the maximum number of segments in a strictly increasing partition of subarray `nums[l..r]` where the last segment is exactly interval `[l..r]` when viewed as the current suffix segment. Symmetrically, let `dec[l][r]` capture strictly decreasing partitions starting with interval `[l..r]` as the current prefix segment on the right side. The pattern is **Interval DP** because states are contiguous ranges and transitions split at boundaries.

3. **Compute increasing chains from left to right.**  
   For every interval `[l..r]`, try a previous cut `k < l`. If `sum(k, l-1) < sum(l, r)` and there exists a valid increasing chain ending at `[k..l-1]`, then `[l..r]` can extend it. This maintains the invariant that adjacent segment sums are strictly increasing and segments remain contiguous, non-overlapping, and cover a prefix.

4. **Compute decreasing chains from right to left.**  
   Similarly, for interval `[l..r]`, try a next cut `k > r`. If `sum(l, r) > sum(r+1, k)` and the suffix beginning at `[r+1..k]` is valid, then `[l..r]` can precede it. This enforces strict decrease away from the peak.

5. **Treat each interval as the peak segment.**  
   For every candidate peak interval `[l..r]`, combine the best increasing partition covering `nums[0..r]` and the best decreasing partition covering `nums[l..n-1]`, aligned so `[l..r]` is shared as the peak. A valid mountain requires at least one segment on each side.

6. **Convert segments to merges.**  
   If the best mountain uses `k` final segments, answer is `n - k`. If no peak yields both sides, return `-1`. This is correct because each merge reduces length by exactly one, so maximizing final mountain length minimizes merges.

## 📊 Worked Example
Take `nums = [1, 3, 2, 1]`.

Prefix sums: `[0, 1, 4, 6, 7]`

Possible useful intervals:

| Interval | Sum |
|---|---:|
| `[0,0]` | 1 |
| `[1,1]` | 3 |
| `[2,2]` | 2 |
| `[3,3]` | 1 |
| `[2,3]` | 3 |

Trace:

1. Left increasing side:
   - `[0,0]` gives chain length `1`
   - `[1,1]` can follow `[0,0]` since `1 < 3` → length `2`
   - `[2,2]` can follow `[0,0]` but not `[1,1]` since `3 < 2` is false

2. Right decreasing side:
   - `[3,3]` gives chain length `1`
   - `[2,2]` can precede `[3,3]` since `2 > 1` → length `2`
   - `[1,1]` can precede `[2,2]` since `3 > 2` → length `3`

3. Peak choice `[1,1] = 3`:
   - left chain ending at peak: `[1, 3]` → length `2`
   - right chain starting at peak: `[3, 2, 1]` → length `3`
   - combined mountain length = `2 + 3 - 1 = 4`

Answer: `4 - 4 = 0` merges.

## ⏱ Complexity Analysis
### Time Complexity
A straightforward interval DP implementation is `O(n^3)` to `O(n^4)`, depending on how transitions are organized. With `n <= 200`, this is acceptable. The dominant cost is testing interval-to-interval transitions under strict sum comparisons. At `10^6` or `10^9` elements, this approach is completely infeasible; the bounded input size is what makes interval DP the right tool here.

### Space Complexity
`O(n^2)` for prefix-accessible interval metadata and DP tables such as `inc` and `dec`. The space is owned by range-based state, not the input itself. It can be reduced only by recomputation or more complex transition ordering, usually trading memory savings for worse constant factors and harder correctness reasoning.

## 💡 Key Takeaways
- If adjacent merges can be repeated and final values are sums of contiguous blocks, reframe the problem as partitioning into intervals rather than simulating merge sequences.
- If the target property is about the ordered values of those blocks, expect interval DP plus prefix sums rather than greedy local merging.
- The mountain must have an internal peak and at least three final segments; forgetting either condition produces false positives.
- Strict inequalities matter on both slopes; equal segment sums invalidate the mountain even if the shape looks unimodal.
- In production systems, the transferable idea is to optimize over canonical end states (partitions) instead of operational histories (merge orders), which collapses state space and simplifies correctness.

## 🚀 Variations & Further Practice
- Allow arbitrary reordering after merges. The interval constraint disappears, and the problem shifts from interval DP to multiset feasibility plus sequence construction.
- Minimize merge cost when merging adjacent elements has weighted cost, not unit cost. Now you must optimize both shape feasibility and merge economics.
- Form a valley array instead of a mountain, or allow `k` peaks. The core partitioning idea remains, but state must encode richer shape structure than a single turning point.