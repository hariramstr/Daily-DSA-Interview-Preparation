# Maximum Score from Choosing a Stable Product Trio

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix/Suffix, Greedy

---

## 🗂 Problem Overview
Given an array `ratings`, choose exactly three indices `left < middle < right` such that `ratings[left] < ratings[middle] < ratings[right]`. Among all such strictly increasing trios, return the maximum possible sum of their values. If no valid trio exists, return `-1`.

The challenge is not the condition itself but the scale: with up to `2 * 10^5` elements, enumerating all triples is infeasible. The solution must exploit structure in the array to evaluate each position efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose an ordered progression under monotonic constraints while optimizing value: search ranking pipelines selecting diversified result ladders, pricing engines constructing upgrade paths, recommendation systems surfacing entry/mid/premium bundles, and event-stream analytics detecting profitable increasing sequences.

At scale, brute-force combinatorics collapse immediately. What matters is decomposing a global optimization into per-position summaries: “best valid predecessor” and “best valid successor.” That shift enables linear or near-linear passes, predictable memory use, and implementations that remain viable inside latency-sensitive services, batch scoring jobs, or streaming enrichment stages.

## 🔍 Problem Statement
You are given an integer array `ratings` where `3 <= ratings.length <= 2 * 10^5` and `1 <= ratings[i] <= 10^9`.

Choose indices `left < middle < right` such that:

- `ratings[left] < ratings[middle]`
- `ratings[middle] < ratings[right]`

The trio must therefore be strictly increasing in both index order and value. Its score is:

`ratings[left] + ratings[middle] + ratings[right]`

Return the maximum score over all valid trios, or `-1` if none exist.

Examples:

- `ratings = [4, 2, 6, 3, 8]` → `18`  
  Optimal trio: indices `(0, 2, 4)` with values `(4, 6, 8)`

- `ratings = [9, 7, 5, 4]` → `-1`  
  No increasing trio exists

The key constraint is `n = 2 * 10^5`: any `O(n^2)` or `O(n^3)` approach is too slow, so the algorithm must summarize left and right candidates efficiently.

## 🪜 How to Solve This
1. Start from the middle element perspective → every valid trio is defined by some `middle`.
2. For a fixed `middle`, the problem becomes:  
   - find the best `left` value smaller than `ratings[middle]`
   - find the best `right` value greater than `ratings[middle]`
3. “Best” here means maximizing the final sum, so:
   - on the left, we want the **largest value strictly less than** `ratings[middle]`
   - on the right, we want the **largest value strictly greater than** `ratings[middle]`
4. The right side is easier to precompute with a suffix maximum array: for every index, know the maximum value to its right.
5. The left side is dynamic: as we scan left to right, we need fast queries for the largest seen value below the current one. That suggests an ordered structure, or coordinate compression plus Fenwick/segment tree.
6. For each position, combine:
   - best smaller-on-left
   - current value
   - best larger-on-right
7. If either side does not exist, that index cannot be the middle.
8. This converts a cubic search into a near-linear or `O(n log n)` optimization.

## 🧩 Algorithm Walkthrough
1. **Precompute suffix maxima**  
   Build `suffixMax[i] = max(ratings[i], suffixMax[i + 1])` from right to left.  
   Why it works: for any middle index `i`, the best possible right contribution is the maximum value in `ratings[i+1..n-1]`.  
   Invariant: after processing position `i`, `suffixMax[i]` stores the maximum value from `i` to the end.

2. **Coordinate-compress ratings**  
   Since values can be up to `10^9`, compress distinct ratings into ranks `1..m`.  
   Why it matters: Fenwick trees and segment trees operate on dense index ranges efficiently.  
   Invariant: value ordering is preserved exactly under compression.

3. **Maintain best left candidate with a Fenwick tree (Prefix Maximum pattern)**  
   Scan from left to right. Before processing `ratings[i]`, query the Fenwick tree for the maximum seen value with compressed rank `< rank(ratings[i])`.  
   Why it works: this returns the largest valid `left` value strictly smaller than the current middle.  
   Invariant: the tree stores the maximum rating observed so far for every prefix of ranks.

4. **Validate the right candidate using suffix maximum**  
   Let `rightBest = suffixMax[i + 1]`. It is usable only if `rightBest > ratings[i]`.  
   Why it works: the suffix maximum is the best sum contributor, but strict inequality must still hold.

5. **Compute candidate score**  
   If both `leftBest` and `rightBest` exist, update  
   `answer = max(answer, leftBest + ratings[i] + rightBest)`.  
   This is correct because for a fixed middle, independently maximizing valid left and right values maximizes the trio sum.

6. **Insert current value into the Fenwick tree**  
   Update the tree at `rank(ratings[i])` with `ratings[i]`.  
   Invariant: after index `i`, the structure represents all possible left candidates for future middles.

This is a **Prefix/Suffix + Greedy maximization** solution, with a Fenwick tree providing ordered prefix maxima in `O(log n)`.

## 📊 Worked Example
Use `ratings = [4, 2, 6, 3, 8]`.

Suffix maxima:

| i | ratings[i] | suffixMax[i] |
|---|------------|--------------|
| 4 | 8          | 8            |
| 3 | 3          | 8            |
| 2 | 6          | 8            |
| 1 | 2          | 8            |
| 0 | 4          | 8            |

Left-to-right trace:

| i | val | best smaller on left | best greater on right | candidate |
|---|-----|----------------------|------------------------|-----------|
| 0 | 4   | none                 | 8                      | invalid    |
| 1 | 2   | none                 | 8                      | invalid    |
| 2 | 6   | 4                    | 8                      | 18         |
| 3 | 3   | 2                    | 8                      | 13         |
| 4 | 8   | 6                    | none                   | invalid    |

Maximum valid score is `18`, from trio `(4, 6, 8)` at indices `(0, 2, 4)`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. Building the suffix maximum array is `O(n)`. Coordinate compression costs `O(n log n)` due to sorting distinct values. Each Fenwick tree query and update is `O(log n)`, performed once per element. At `10^6` elements this is still practical; at `10^9`, even linear scans are no longer realistic in memory-bound systems.

### Space Complexity
`O(n)` for the suffix maximum array, compressed value mapping, and Fenwick tree. The dominant owner is the auxiliary indexing state, not the answer computation itself. Space can be tuned slightly, but removing these structures usually forces slower queries or more complex trade-offs.

## 💡 Key Takeaways
- If the problem says “choose a middle element” with constraints on both sides, think in terms of left-summary and right-summary arrays or data structures.
- If values are large but only relative ordering matters, coordinate compression is usually the right bridge to efficient indexed structures.
- Strict inequalities matter: querying `<= current` instead of `< current` silently accepts invalid trios with equal values.
- `suffixMax[i + 1]` is the right-side candidate; using `suffixMax[i]` incorrectly allows reusing the middle element as the right endpoint.
- The transferable design insight is to decompose global combinatorial search into per-position optimal summaries, which is the same move used in ranking, stream processing, and cost-based optimization systems.

## 🚀 Variations & Further Practice
- Find the maximum score of a strictly increasing subsequence of length `k`; the twist is that local left/right summaries are no longer sufficient, so you need DP with Fenwick/segment trees.
- Count the number of valid increasing trios instead of maximizing sum; the twist is replacing max-aggregation with frequency aggregation while preserving strict ordering.
- Support online updates to `ratings` and repeated queries; the twist is moving from one-pass preprocessing to dynamic range data structures with update/query trade-offs.