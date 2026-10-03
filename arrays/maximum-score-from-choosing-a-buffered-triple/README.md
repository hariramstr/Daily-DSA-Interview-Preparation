# Maximum Score From Choosing a Buffered Triple

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Maximum, Suffix Maximum

---

## 🗂 Problem Overview
Given an integer array `nums` and an integer `gap`, choose indices `(i, j, k)` such that `i < j < k`, `j - i > gap`, and `k - j > gap`. The objective is to maximize `nums[i] - nums[j] + nums[k]`. Return that maximum score, or `-1` if no valid triple exists. The problem is non-trivial because brute-force enumeration is cubic, and even pairwise optimization is too slow for `n` up to `200000`.

## 🌍 Engineering Impact
This pattern shows up anywhere a decision depends on a current event plus the best admissible state on both sides under spacing constraints. Examples include streaming pipelines with cooldown windows, ad pacing systems that must separate impressions, time-series anomaly scoring with exclusion zones, and search/ranking pipelines that combine prior best, current penalty, and future best. At scale, naive rescans around every candidate middle point collapse under latency and cache pressure. Precomputing prefix and suffix optima turns repeated range-max queries into O(1) lookups, enabling predictable linear passes, simpler capacity planning, and implementations that remain stable as datasets move from thousands to millions of events.

## 🔍 Problem Statement
You are given:

- `nums`, an integer array of length `n`
- `gap`, a non-negative integer

A buffered triple is a choice of indices `(i, j, k)` satisfying:

- `i < j < k`
- `j - i > gap`
- `k - j > gap`

Its score is:

`nums[i] - nums[j] + nums[k]`

Return the maximum score over all valid buffered triples. If no such triple exists, return `-1`.

Constraints:

- `3 <= n <= 200000`
- `-1000000000 <= nums[i] <= 1000000000`
- `0 <= gap < n`

Examples:

- `nums = [5, 1, 9, 2, 7, 3, 8], gap = 1` → `15`
- `nums = [4, -3, 6, -10, 5, 2], gap = 1` → `18`

The key constraint is input size: any O(n²) or O(n³) approach is dead on arrival. The algorithm must evaluate each feasible middle index using precomputed range maxima.

## 🪜 How to Solve This
1. Start from the score formula: `nums[i] - nums[j] + nums[k]`.  
   For a fixed middle index `j`, `nums[j]` is fixed, so the problem becomes: find the best valid `i` on the left and the best valid `k` on the right.

2. Translate the spacing rule into index ranges.  
   If `j` is the middle, then:
   - `i` must lie in `[0, j - gap - 1]`
   - `k` must lie in `[j + gap + 1, n - 1]`

3. That means every valid `j` asks the same question twice:  
   “What is the maximum value in this prefix?” and  
   “What is the maximum value in this suffix?”

4. Repeated range maxima over static arrays is a strong signal for precomputation.  
   Build:
   - `prefixMax[x] = max(nums[0..x])`
   - `suffixMax[x] = max(nums[x..n-1])`

5. Then each middle index can be scored in O(1):  
   `prefixMax[j - gap - 1] - nums[j] + suffixMax[j + gap + 1]`

6. Iterate only over `j` values that have both a legal left side and right side.  
   This yields a linear-time solution with simple invariants and no nested search.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix maxima.**  
   Build an array `prefixMax` where `prefixMax[t]` stores the maximum value seen from index `0` through `t`.  
   **Why correct:** any valid left index for a middle `j` must come from a prefix ending at `j - gap - 1`.  
   **Invariant:** after processing position `t`, `prefixMax[t] = max(nums[0..t])`.

2. **Precompute suffix maxima.**  
   Build `suffixMax` from right to left so that `suffixMax[t]` stores the maximum value in `nums[t..n-1]`.  
   **Why correct:** any valid right index for middle `j` must come from a suffix starting at `j + gap + 1`.  
   **Invariant:** after processing position `t`, `suffixMax[t] = max(nums[t..n-1])`.

3. **Enumerate feasible middle indices.**  
   A middle `j` is feasible only if both sides exist:
   - `j - gap - 1 >= 0`
   - `j + gap + 1 < n`  
   Equivalently, iterate `j` from `gap + 1` through `n - gap - 2`.

4. **Evaluate each middle in O(1).**  
   Compute:
   `leftBest = prefixMax[j - gap - 1]`  
   `rightBest = suffixMax[j + gap + 1]`  
   `score = leftBest - nums[j] + rightBest`

5. **Track the maximum score.**  
   Initialize answer to negative infinity and update on each valid `j`. If no `j` is feasible, return `-1`.

This is a classic **Prefix Maximum + Suffix Maximum** pattern: convert many overlapping range-maximum queries on a static array into two linear precomputations and constant-time evaluation.

## 📊 Worked Example
Example: `nums = [5, 1, 9, 2, 7, 3, 8]`, `gap = 1`

`prefixMax = [5, 5, 9, 9, 9, 9, 9]`  
`suffixMax = [9, 9, 9, 8, 8, 8, 8]`

Valid middle indices are `j = 2, 3, 4`.

| j | nums[j] | left bound = j-gap-1 | leftBest | right bound = j+gap+1 | rightBest | score |
|---|---------|----------------------|----------|------------------------|-----------|-------|
| 2 | 9       | 0                    | 5        | 4                      | 8         | 4     |
| 3 | 2       | 1                    | 5        | 5                      | 8         | 11    |
| 4 | 7       | 2                    | 9        | 6                      | 8         | 10    |

Maximum observed score is `11`.

The mechanics are the point: for each `j`, the legal left and right search spaces collapse to two precomputed lookups. No rescanning, no heap maintenance, no nested iteration.

## ⏱ Complexity Analysis
### Time Complexity
The runtime is **O(n)**: one pass to build `prefixMax`, one pass to build `suffixMax`, and one pass over feasible middle indices. The dominant operation is linear scanning with O(1) work per element. This is practical at `10^6` elements and still structurally correct for much larger inputs, whereas O(n²) becomes unusable long before that.

### Space Complexity
The space usage is **O(n)**, owned by the `prefixMax` and `suffixMax` arrays. It can be reduced to O(n) with only one auxiliary array plus a rolling maximum from the other side, but not to O(1) without sacrificing the constant-time lookup structure that makes the solution clean and linear.

## 💡 Key Takeaways
- If a score for each position depends on the best value in a legal prefix and the best value in a legal suffix, think prefix/suffix precomputation immediately.
- When constraints say “large static array” and each candidate asks repeated range-extremum questions, precompute instead of rescanning.
- The spacing rule is `> gap`, not `>= gap`; the legal left endpoint for `j` is `j - gap - 1`, and the legal right start is `j + gap + 1`.
- Be careful with feasible middle bounds: `j` must satisfy both sides simultaneously, so iterate `j` in `[gap + 1, n - gap - 2]`; otherwise you silently read invalid ranges.
- The transferable design insight is to turn many local optimization queries over immutable data into a small number of global summaries, trading memory for deterministic latency.

## 🚀 Variations & Further Practice
- Extend from a triple to a buffered sequence of `m` picks with alternating `+/-` signs. The twist is moving from simple prefix/suffix maxima to dynamic programming over stages.
- Replace “maximize value in prefix/suffix” with “maximize over a sliding admissible window” where the legal region changes incrementally. The harder abstraction becomes monotonic deque maintenance.
- Support online updates to `nums` between queries. The conceptual jump is from static precomputation to segment trees or Fenwick-like structures for dynamic range extrema.