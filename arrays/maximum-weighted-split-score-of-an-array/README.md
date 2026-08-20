# Maximum Weighted Split Score of an Array

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Monotonic Stack

---

## 🗂 Problem Overview
Given `nums` and `weights`, choose split points `i < j` so the array becomes three non-empty contiguous segments. Each segment contributes `segment_sum * segment_min_weight`, and the goal is to maximize the total contribution. The input size reaches `2 * 10^5`, so naive enumeration of all splits and all segment minima is not viable. The challenge is combining fast range-sum access with efficient reasoning about which element becomes the minimum inside each segment.

## 🌍 Engineering Impact
This pattern shows up anywhere a contiguous range has both additive value and a bottleneck attribute. In streaming pipelines, throughput over a window may be modeled as total work scaled by the slowest stage. In search ranking or ad allocation, a block’s utility can depend on aggregate score constrained by the weakest quality signal. In distributed schedulers, shard groups often have cumulative capacity but are limited by the least healthy node. At production scale, brute-force partitioning collapses under quadratic scans. Prefix aggregates plus monotonic structure let you preserve exactness while staying within latency and memory budgets.

## 🔍 Problem Statement
You are given two arrays of length `n`: `nums` and `weights`, where `3 <= n <= 2 * 10^5`. Choose indices `i` and `j` such that `0 <= i < j < n - 1`, producing three non-empty contiguous subarrays:

- `left = nums[0..i]`
- `middle = nums[i+1..j]`
- `right = nums[j+1..n-1]`

The score is:

`sum(left) * minWeight(left) + sum(middle) * minWeight(middle) + sum(right) * minWeight(right)`

Return the maximum score using 64-bit arithmetic.

Example 1:
- `nums = [4,2,7,3,5]`
- `weights = [6,2,4,1,3]`
- best score = `43`

Example 2:
- `nums = [8,1,6,2,4,3]`
- `weights = [5,3,6,2,7,4]`
- best score = `79`

The decisive constraint is `n = 2 * 10^5`: any `O(n^2)` split enumeration is too slow.

## 🪜 How to Solve This
1. Start with the formula → each segment needs two things: its sum and its minimum weight.  
2. Segment sums are easy → build prefix sums so any range sum is `O(1)`.  
3. Segment minima are the hard part → recomputing minima for every `(i, j)` pair is what kills brute force.  
4. Reframe the problem → instead of asking “for this split, what is the minimum?”, ask “for this index, on which ranges is `weights[k]` the minimum?”  
5. That immediately suggests a **monotonic stack** → compute previous/next smaller elements so each weight owns a maximal interval where it is the segment minimum.  
6. Now each segment score is of the form `minWeight * rangeSum`, but only for ranges constrained to include the chosen minimum index and stay inside its ownership interval.  
7. The remaining challenge is coordinating three segments at once. Treat left, middle, and right as composable DP-style best values over prefixes/suffixes, then combine them around feasible middle intervals.  
8. The winning mental model: prefix sums handle additive state; monotonic stack handles bottleneck state; the solution emerges by making minima explicit instead of rediscovering them per split.

## 🧩 Algorithm Walkthrough
1. **Build prefix sums for `nums`.**  
   Let `pref[t]` be the sum of the first `t` elements. Then any range sum `sum(l..r)` is `pref[r+1] - pref[l]`. This removes all repeated summation work and gives constant-time segment totals.

2. **Compute minimum-ownership intervals with a Monotonic Stack.**  
   For each index `k`, find:
   - `prevLess[k]`: nearest index to the left with strictly smaller weight
   - `nextLessEq[k]`: nearest index to the right with smaller-or-equal weight  
   This tie-breaking avoids double-counting equal minima and gives each index a unique maximal interval where it is the designated minimum.

3. **Model segment contribution by its minimum index.**  
   If `k` is the minimum of a segment `[l..r]`, then `l` must satisfy `prevLess[k] < l <= k`, and `r` must satisfy `k <= r < nextLessEq[k]`. The contribution becomes `weights[k] * (pref[r+1] - pref[l])`.

4. **Precompute best left-segment values by endpoint.**  
   For every possible left endpoint `i`, compute the best score of a segment ending at `i` and starting at `0`. Because the left segment is always a prefix, its minimum can be maintained incrementally: `leftBest[i] = prefixSum(0..i) * minWeight(0..i)`.

5. **Precompute best right-segment values by startpoint.**  
   Symmetrically, for every `s`, compute the score of suffix segment `[s..n-1]`: `rightBest[s] = suffixSum(s..n-1) * suffixMinWeight(s..n-1)`.

6. **Evaluate all feasible middle segments efficiently.**  
   For a middle segment `[i+1..j]`, its score is `sum(i+1..j) * minWeight(i+1..j)`. Using the ownership intervals from the stack, each index `k` contributes as the minimum for all middle ranges containing `k` and staying inside its valid interval. The combination target is:  
   `leftBest[i] + middleScore(i+1, j) + rightBest[j+1]`.

7. **Use range-max style aggregation over valid boundaries.**  
   For each minimum index `k`, the valid left split `i` and right split `j` ranges are constrained by the ownership interval and by non-empty segment rules. The expression separates into terms dependent on `i` and `j`, enabling offline maximization with Fenwick/segment-tree style range maxima or equivalent sweep logic.

8. **Maintain the invariant:** every candidate split is considered exactly once through the index that owns the middle minimum, and every score uses `O(1)` sum lookup. That is why the approach scales while preserving exactness.

## 📊 Worked Example
Use `nums = [4,2,7,3,5]`, `weights = [6,2,4,1,3]`.

Prefix sums: `pref = [0,4,6,13,16,21]`

Possible split `(i, j)` means:
- left = `[0..i]`
- middle = `[i+1..j]`
- right = `[j+1..4]`

| `i` | `j` | left score | middle score | right score | total |
|---|---:|---:|---:|---:|---:|
| 0 | 1 | `4*6=24` | `2*2=4` | `(7+3+5)*1=15` | `43` |
| 0 | 2 | `24` | `(2+7)*2=18` | `(3+5)*1=8` | `50` |
| 0 | 3 | `24` | `(2+7+3)*1=12` | `5*3=15` | `51` |
| 1 | 2 | `(4+2)*2=12` | `7*4=28` | `8` | `48` |
| 1 | 3 | `12` | `(7+3)*1=10` | `15` | `37` |
| 2 | 3 | `(4+2+7)*2=26` | `3*1=3` | `15` | `44` |

Best score is `51` at `(i=0, j=3)`. This also highlights why exact range minima matter: the minimum can shift abruptly as boundaries move.

## ⏱ Complexity Analysis

### Time Complexity
With prefix sums, monotonic stack preprocessing, and an efficient offline/range-max combination step, the target complexity is `O(n log n)` or better depending on the chosen aggregation structure. That is practical for `2 * 10^5` elements, still reasonable near `10^6`, and fundamentally different from `O(n^2)`, which is already unusable far below `10^9`.

### Space Complexity
`O(n)` auxiliary space. The dominant structures are prefix sums, stack boundary arrays, and the range-max support structure used during combination. You can’t reduce this meaningfully without giving up either constant-time range sums or linear-time minimum-ownership preprocessing.

## 💡 Key Takeaways
- If a scoring function is `range sum * range minimum`, that is a strong signal to combine **prefix sums** with a structure that assigns minima to intervals.
- When the problem asks for optimal contiguous partitions under a bottleneck metric, think in terms of “which index owns the minimum?” rather than recomputing minima per candidate split.
- The non-empty three-part split constraint means valid boundaries are `0 <= i < j < n - 1`; off-by-one bugs usually come from mixing segment endpoints with split indices.
- Equal weights require deliberate tie-breaking in the monotonic stack (`<` on one side, `<=` on the other) or you will double-count or miss valid intervals.
- At scale, the transferable design lesson is to decompose mixed metrics into orthogonal precomputations: additive state via prefix aggregates, limiting state via monotone structure, then combine offline.

## 🚀 Variations & Further Practice
- Maximize the same score over **`k` contiguous segments** instead of exactly three. The twist is turning the middle-segment reasoning into a higher-dimensional DP with bottleneck-aware transitions.
- Support **online updates** to `nums` or `weights` with repeated max-score queries. The hard part is that both sums and minimum-ownership intervals become dynamic, pushing toward segment trees or more advanced range structures.
- Replace `min(weights in segment)` with **second minimum** or another order statistic. The conceptual jump is that monotonic-stack ownership no longer captures the segment bottleneck with a single index.