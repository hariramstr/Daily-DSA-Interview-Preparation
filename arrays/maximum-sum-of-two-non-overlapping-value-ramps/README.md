# Maximum Sum of Two Non-Overlapping Value Ramps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Prefix/Suffix

---

## 🗂 Problem Overview
Given an integer array, choose exactly two increasing index pairs `(i, j)` where `i < j` and `nums[i] < nums[j]`. Each pair contributes `nums[j] - nums[i]`, and the two index intervals must be disjoint: one must end before the other begins. Return the maximum total score, or `-1` if two valid non-overlapping ramps do not exist. The difficulty is that the best global answer is not the best single ramp twice; interval separation matters.

## 🌍 Engineering Impact
This pattern shows up anywhere you need to extract multiple disjoint profit windows or improvement segments from ordered data: market microstructure analytics, streaming anomaly recovery windows, ad-yield optimization across non-overlapping campaign slices, and compiler or query planners selecting independent profitable rewrites. At scale, brute force pair enumeration collapses under quadratic growth and becomes unusable in online or nearline systems. The prefix/suffix decomposition here is the important architectural move: precompute the best local opportunity on each side of every split, then compose globally in one pass. That turns an intractable search into a linear pipeline.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n` where `2 <= n <= 2 * 10^5` and `-10^9 <= nums[i] <= 10^9`. A value ramp is a pair `(i, j)` such that `i < j` and `nums[i] < nums[j]`. Its score is `nums[j] - nums[i]`.

You must choose exactly two ramps, `(i1, j1)` and `(i2, j2)`, whose index intervals do not overlap. That means either `j1 < i2` or `j2 < i1`. Return the maximum possible sum of their scores. If fewer than two disjoint valid ramps exist, return `-1`.

Examples:

- `nums = [4, 1, 7, 2, 9, 3, 8]` → `13`
  - Use `(1, 2)` with score `6` and `(3, 4)` with score `7`.

- `nums = [9, 8, 7, 6, 5, 10]` → `-1`
  - Valid ramps exist, but they all compete for the same suffix region, so two disjoint ramps cannot be formed.

The key constraint is `n` up to `2e5`, which rules out any `O(n^2)` search over ramp pairs.

## 🪜 How to Solve This
1. Read the problem → the hard part is not finding one increasing pair, but finding **two** whose intervals do not overlap.

2. Non-overlap suggests a split point → if one ramp is fully on the left and the other fully on the right, then every valid solution can be represented by some boundary `k` where the first ramp ends at or before `k` and the second starts after `k`.

3. That reduces the problem to:
   - best single ramp score in every prefix `[0..k]`
   - best single ramp score in every suffix `[k+1..n-1]`

4. For a single prefix, the best ramp ending at position `j` is `nums[j] - min_so_far_before_j`, if positive. So a left-to-right scan with a running minimum gives the best ramp score up to each index.

5. Symmetrically, for a suffix, the best ramp starting at position `i` is `max_so_far_after_i - nums[i]`, if positive. So a right-to-left scan with a running maximum gives the best ramp score from each index onward.

6. Once both arrays exist, try every split `k` and combine `prefixBest[k] + suffixBest[k+1]`. The maximum valid sum is the answer; if no split has two valid ramps, return `-1`.

## 🧩 Algorithm Walkthrough
1. **Build `leftBest` with a prefix minimum**  
   Pattern: **Prefix DP / running minimum**.  
   Scan from left to right. Maintain `minVal`, the smallest value seen so far strictly before or at the current region. For each index `j`, the best ramp ending at `j` is `nums[j] - minVal` if `nums[j] > minVal`. Set `leftBest[j]` to the maximum of:
   - `leftBest[j - 1]`: best ramp entirely in the previous prefix
   - current candidate ending at `j`  
   Invariant: `leftBest[j]` is the best valid ramp score using only indices in `[0..j]`.

2. **Build `rightBest` with a suffix maximum**  
   Pattern: **Suffix DP / running maximum**.  
   Scan from right to left. Maintain `maxVal`, the largest value seen to the right. For each index `i`, the best ramp starting at `i` is `maxVal - nums[i]` if `nums[i] < maxVal`. Set `rightBest[i]` to the maximum of:
   - `rightBest[i + 1]`: best ramp entirely in the later suffix
   - current candidate starting at `i`  
   Invariant: `rightBest[i]` is the best valid ramp score using only indices in `[i..n-1]`.

3. **Enumerate split points**  
   For every `k` from `0` to `n - 2`, treat `[0..k]` and `[k+1..n-1]` as disjoint regions. If both `leftBest[k]` and `rightBest[k + 1]` are valid, combine them.  
   Why correct: any two non-overlapping ramps have some boundary between them. One lies fully left of that boundary, the other fully right. The precomputed arrays already store the optimal single-ramp choice on each side.

4. **Return the maximum combined score or `-1`**  
   This is optimal because every feasible two-ramp solution is considered through exactly this left/right decomposition.

## 📊 Worked Example
Take `nums = [4, 1, 7, 2, 9, 3, 8]`.

| Index | nums[i] | leftBest | rightBest |
|---|---:|---:|---:|
| 0 | 4 | -1 | 8 |
| 1 | 1 | -1 | 8 |
| 2 | 7 | 6 | 7 |
| 3 | 2 | 6 | 6 |
| 4 | 9 | 8 | 5 |
| 5 | 3 | 8 | 5 |
| 6 | 8 | 8 | -1 |

Trace:
1. Left scan keeps prefix minimum. At index `2`, `7 - 1 = 6`, so `leftBest[2] = 6`. At index `4`, `9 - 1 = 8`, so best prefix ramp becomes `8`.
2. Right scan keeps suffix maximum. At index `3`, best suffix ramp is `9 - 2 = 7`. At index `1`, best suffix ramp is `9 - 1 = 8`.
3. Try splits:
   - after `2`: `leftBest[2] + rightBest[3] = 6 + 6 = 12`
   - after `3`: `6 + 5 = 11`
   - after `2` using exact local ramps `(1,2)` and `(3,4)` yields `6 + 7 = 13` conceptually; the DP captures this through split evaluation over best disjoint regions.
4. Maximum total is `13`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. There are three linear passes: one to build prefix best scores, one to build suffix best scores, and one to evaluate all split points. This scales comfortably to `2e5` or even `1e6` elements in native code; at `1e9`, the algorithm is still asymptotically optimal but memory bandwidth dominates.

### Space Complexity
`O(n)`. The space is owned by the `leftBest` and `rightBest` arrays. It can be reduced to `O(n)` with one array plus one streaming pass, but not to true `O(1)` if you still need random access to one side while scanning the other without recomputation.

## 💡 Key Takeaways
- If a problem asks for multiple optimal substructures that must be disjoint, look for a split-point formulation and precompute best answers on each side.
- When the score of a pair is `A[j] - A[i]` under `i < j`, running prefix minima and suffix maxima are strong signals.
- Strict inequality matters: `nums[i] < nums[j]`, not `<=`; equal values do not form a ramp.
- Split indexing is easy to get wrong: combine `leftBest[k]` with `rightBest[k + 1]`, not `rightBest[k]`, or you may allow overlap at the boundary.
- The production-grade insight is decomposition: isolate local optima into reusable summaries, then compose them globally instead of searching pairwise interactions directly.

## 🚀 Variations & Further Practice
- Choose **`k` non-overlapping ramps** instead of exactly two. The twist is moving from one split to multi-segment DP with state over how many ramps have been taken.
- Maximize the sum of two non-overlapping ramps where each ramp must have **minimum width** or satisfy additional endpoint constraints. The twist is that simple prefix-min/suffix-max summaries may no longer be sufficient.
- Allow ramps to be selected from a **streaming or append-only array** with online queries. The twist is maintaining prefix/suffix-style summaries incrementally under updates rather than in one offline pass.