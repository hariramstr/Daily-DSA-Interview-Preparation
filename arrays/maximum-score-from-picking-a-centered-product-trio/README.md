# Maximum Score from Picking a Centered Product Trio

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Minimum, Greedy

---

## 🗂 Problem Overview
Given an integer array `ratings`, choose indices `(l, c, r)` with `l < c < r` such that both side values are strictly smaller than the center: `ratings[l] < ratings[c]` and `ratings[r] < ratings[c]`. The objective is to maximize `ratings[l] + ratings[c] + ratings[r]`. Return that maximum score, or `-1` if no valid trio exists. The challenge is scale: with up to `10^5` elements, enumerating all triples is infeasible, so each index must be evaluated as a center in near-constant time.

## 🌍 Engineering Impact
This pattern shows up anywhere a local candidate must be validated against best-available context on both sides: search ranking windows, time-series anomaly detection, compiler optimization passes over instruction streams, and streaming pipelines that score an event relative to prior and future neighbors. At small scale, nested scans are tolerable; at catalog, log, or telemetry scale, they collapse under quadratic behavior and cache-unfriendly access. The useful architectural move is precomputing directional summaries once, then answering per-position decisions in O(1). That shifts the system from repeated recomputation to indexed context lookup, which is exactly how high-throughput scoring systems stay predictable.

## 🔍 Problem Statement
You are given an array `ratings` where `3 <= ratings.length <= 100000` and `1 <= ratings[i] <= 1000000000`. A valid centered product trio consists of indices `(l, c, r)` such that:

- `l < c < r`
- `ratings[l] < ratings[c]`
- `ratings[r] < ratings[c]`

Its score is:

- `ratings[l] + ratings[c] + ratings[r]`

Return the maximum score among all valid trios. If no such trio exists, return `-1`.

Examples:

- `ratings = [4, 9, 2, 7, 3]` → `16`  
  Best choice: `(0, 1, 4)` → `4 + 9 + 3 = 16`

- `ratings = [1, 2, 3, 4]` → `-1`  
  No index has a smaller value on both sides.

The key constraint is the array size: `10^5` rules out brute-force triple enumeration and forces a linear or near-linear strategy.

## 🪜 How to Solve This
1. Start with the brute-force thought: for every center `c`, scan left for the best valid `l` and right for the best valid `r`.  
   → Correct, but O(n²) overall.

2. Notice what “best” means here. Since the center is fixed, maximizing the trio score means choosing the **largest** value on the left that is still `< ratings[c]`, and the **largest** value on the right that is still `< ratings[c]`.

3. That sounds like a per-index range query, but the constraints suggest something simpler. We do not need arbitrary order statistics if we process each center independently with directional summaries.

4. Reframe the problem: for each position, can we quickly know whether there exists a smaller value on the left and right, and among those, what is the best contribution?

5. The left side can be handled with a prefix structure; the right side with a suffix structure. Since values must be **strictly smaller than the center**, we need a way to query the maximum value below a threshold.

6. That leads to coordinate compression + Fenwick tree / segment tree, processed left-to-right and right-to-left.  
   → Each pass answers: “what is the maximum seen value strictly less than `ratings[i]`?”

7. Once both best-side contributions are known for every index, scan all centers and compute the best valid sum.

## 🧩 Algorithm Walkthrough
1. **Coordinate-compress the values.**  
   Ratings can be as large as `10^9`, but only relative ordering matters for “strictly smaller.” Compressing values to ranks `[1..m]` lets us use indexed trees efficiently.  
   **Invariant:** rank order preserves all `<` relationships.

2. **Compute `bestLeft[i]` with a Fenwick tree for prefix maximums.**  
   Traverse left to right. Before inserting `ratings[i]`, query the maximum value among ranks strictly less than `rank[i]`. That result is the best valid left contribution for center `i`. Then insert `ratings[i]` at its rank.  
   **Why correct:** the tree contains exactly the values from indices `< i`, and querying `< rank[i]` enforces the strict inequality.

3. **Compute `bestRight[i]` symmetrically.**  
   Clear the tree and traverse right to left. Query the maximum value among ranks strictly less than `rank[i]`, then insert `ratings[i]`.  
   **Invariant:** at step `i`, the tree contains exactly values from indices `> i`.

4. **Evaluate every index as a center.**  
   If both `bestLeft[i]` and `bestRight[i]` exist, compute  
   `bestLeft[i] + ratings[i] + bestRight[i]`  
   and track the maximum.

5. **Return the result or `-1`.**  
   If no index has valid contributions on both sides, no centered trio exists.

This is a **prefix/suffix summary + greedy per-center evaluation** problem. The Fenwick tree is the right abstraction because we need repeated “best value below threshold” queries under incremental updates, not full rescans.

## 📊 Worked Example
Use `ratings = [4, 9, 2, 7, 3]`.

| i | rating | bestLeft (< rating) | bestRight (< rating) | valid score |
|---|--------|----------------------|----------------------|-------------|
| 0 | 4      | —                    | 3                    | —           |
| 1 | 9      | 4                    | 7                    | 20          |
| 2 | 2      | —                    | —                    | —           |
| 3 | 7      | 4                    | 3                    | 14          |
| 4 | 3      | 2                    | —                    | —           |

Trace:

1. Left-to-right pass builds prior values and queries the best smaller one.  
   For `9`, best left is `4`. For `7`, best left is also `4`.

2. Right-to-left pass does the same from the other side.  
   For `9`, best right is `7`. For `7`, best right is `3`.

3. Evaluate centers with both sides present:  
   - center `9` → `4 + 9 + 7 = 20`  
   - center `7` → `4 + 7 + 3 = 14`

Maximum score is `20`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. Coordinate compression costs `O(n log n)`, and each Fenwick-tree pass performs one query and one update per element, each `O(log n)`. At `10^6` elements this is still practical; at `10^9`, even linear scans are unrealistic, so the problem becomes a distributed or external-memory concern rather than an algorithmic one.

### Space Complexity
`O(n)` overall: compressed ranks, `bestLeft`, `bestRight`, and the Fenwick tree all scale linearly with the number of distinct values or elements. You can reduce auxiliary storage slightly by computing one side on the fly, but that trades memory for more complex control flow and weaker debuggability.

## 💡 Key Takeaways
- If a problem asks you to score each index using the best eligible value on the left and right, think directional precomputation instead of nested scans.
- “Strictly smaller than current value” is a strong signal for coordinate compression plus Fenwick/segment tree queries over value ranks.
- Query ranks `< currentRank`, not `<= currentRank`; equal values are invalid and silently break correctness.
- Do not insert the current value before querying for that side, or you will allow the center to match against itself.
- The transferable design pattern is to precompute compact directional summaries once, then make local decisions cheaply and predictably at scale.

## 🚀 Variations & Further Practice
- Require the trio to maximize score under `ratings[l] < ratings[c] > ratings[r]` and also minimize index distance. The twist is optimizing over two objectives instead of one.
- Extend from a trio to a length-`k` mountain-shaped subsequence. The harder part is replacing single best-side lookups with dynamic programming over increasing/decreasing states.
- Support online updates to `ratings` and repeated max-trio queries. The twist is moving from static prefix/suffix preprocessing to dynamic range data structures.