# Maximum Matching Distance for Mirrored Billboards

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Hash Map, Index Tracking

---

## 🗂 Problem Overview
Given two equal-length arrays, `top` and `bottom`, compute the largest distance between matching category IDs that appear in both rows. For any category `x`, you may pair one index `i` from `top` where `top[i] = x` with one index `j` from `bottom` where `bottom[j] = x`, and score that category as `|i - j|`. Return the maximum score across all categories, or `0` if no category appears in both arrays. The challenge is avoiding quadratic pair comparisons under large input sizes.

## 🌍 Engineering Impact
This pattern shows up anywhere two indexed streams must be correlated by key while preserving positional information: log reconciliation across replicas, search result alignment between ranking stages, compiler symbol resolution across passes, and streaming joins over partitioned event feeds. At scale, naive pairwise comparison collapses under skewed keys and long tails. The right approach—tracking only the extremal positions per key—turns an apparent cross-product into a single pass with bounded per-key state. That enables predictable latency, memory proportional to cardinality, and straightforward parallelization or shard-local aggregation.

## 🔍 Problem Statement
You are given two arrays, `top` and `bottom`, of equal length `n`, where `1 <= n <= 200000`. Each value is a category ID in the range `1..1e9`.

For a category `x`, define its matching distance as:

- the maximum `|i - j|` such that `top[i] = x` and `bottom[j] = x`

If `x` appears in only one array, it contributes nothing. Return the maximum matching distance over all categories. If no category appears in both arrays, return `0`.

Examples:

- `top = [4, 7, 2, 7, 9]`, `bottom = [8, 7, 4, 2, 7]` → categories in both rows are `4, 7, 2`; best distance is `3` for category `7`
- `top = [5, 1, 5, 3, 1, 6]`, `bottom = [1, 5, 2, 5, 7, 1]` → categories in both rows are `1, 5`; best distance is `4`

The key constraint is `n = 200000`: any solution that compares all matching pairs per category is too slow.

## 🪜 How to Solve This
1. Read the definition carefully → for a fixed category `x`, we do **not** need all matching pairs. We only need the pair with the largest index gap.

2. Ask what maximizes `|i - j|` between two sets of indices → the answer must involve extremes: earliest and latest occurrences across the two rows.

3. For each category, the best candidate is one of:
   - `maxTop[x] - minBottom[x]`
   - `maxBottom[x] - minTop[x]`

4. That means we do not need full index lists. We only need four values per category:
   - first and last index in `top`
   - first and last index in `bottom`

5. Arrays are large and IDs are sparse up to `1e9` → direct indexing is wasteful, so use a hash map keyed by category ID.

6. Scan both arrays once, updating per-category extremes. Then scan the keys and compute the best valid cross-row distance.

7. Categories seen in only one row are ignored automatically. This gives linear time, bounded state per distinct ID, and no nested comparisons.

## 🧩 Algorithm Walkthrough
1. **Use a Hash Map for extremal index tracking.**  
   Pattern: **Hash Map + Index Tracking**. For each category ID, store four fields: `minTop`, `maxTop`, `minBottom`, `maxBottom`. This is the right abstraction because the objective depends only on per-key positional extremes, not on local adjacency or ordering between different keys.

2. **Initialize missing values with sentinels.**  
   For example, set mins to `+∞` and maxes to `-∞`. This lets each update remain constant-time and makes “seen in this row” easy to test later.

3. **Single pass over indices `0..n-1`.**  
   At index `i`, update the record for `top[i]` with `minTop = min(minTop, i)` and `maxTop = i`. Do the symmetric update for `bottom[i]`.  
   Invariant: after processing index `i`, each record contains correct first/last positions for all occurrences seen so far.

4. **Evaluate only categories present in both rows.**  
   A category is valid if both `minTop` and `minBottom` were updated from sentinels. Categories appearing in only one row cannot contribute by definition.

5. **Compute the maximum possible distance for each valid category.**  
   The farthest cross-row pair must use an extreme from one side and an opposite extreme from the other. So compute:  
   `bestX = max(abs(maxTop - minBottom), abs(maxBottom - minTop))`  
   This is sufficient because any interior index can only reduce or match the distance to an opposite-side extreme.

6. **Track the global maximum and return it.**  
   If no category is valid, the answer remains `0`.

## 📊 Worked Example
Example: `top = [4, 7, 2, 7, 9]`, `bottom = [8, 7, 4, 2, 7]`

| i | top[i] | bottom[i] | Updated state |
|---|--------|-----------|---------------|
| 0 | 4 | 8 | `4: top[0,0]`, `8: bottom[0,0]` |
| 1 | 7 | 7 | `7: top[1,1], bottom[1,1]` |
| 2 | 2 | 4 | `2: top[2,2]`, `4: top[0,0], bottom[2,2]` |
| 3 | 7 | 2 | `7: top[1,3], bottom[1,1]`, `2: top[2,2], bottom[3,3]` |
| 4 | 9 | 7 | `9: top[4,4]`, `7: top[1,3], bottom[1,4]` |

Now evaluate shared categories:

- `4`: `max(|0-2|, |2-0|) = 2`
- `2`: `max(|2-3|, |3-2|) = 1`
- `7`: `max(|3-1|, |4-1|) = 3`

Category `9` appears only in `top`, `8` only in `bottom`, so both are ignored. Final answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + k)`, where `n` is array length and `k` is the number of distinct category IDs across both arrays. The dominant work is one linear scan to build extrema and one pass over the map to evaluate candidates. At `10^6` elements this is routine; at `10^9`, only a streaming/distributed variant is realistic.

### Space Complexity
`O(k)` additional space for the hash map storing four integers per distinct category. This is already asymptotically minimal for exact computation over sparse IDs. You can reduce constants with packed structs, but not the order without sacrificing exactness or requiring multiple passes.

## 💡 Key Takeaways
- If the problem asks for a maximum distance between equal values across collections, check whether only per-key **extreme positions** matter instead of all pairs.
- Sparse large-value IDs plus grouping semantics are a strong signal for a **hash map keyed by value with compact per-key state**.
- Do not store every index for a category; that turns a linear problem into unnecessary memory growth with no benefit.
- Be careful with categories that appear in only one row: they must be excluded, not treated as distance `0` candidates during computation.
- In production systems, many expensive correlation problems collapse once you identify the minimal sufficient statistics per key rather than retaining full event history.

## 🚀 Variations & Further Practice
- Return the category ID along with the maximum distance, with deterministic tie-breaking. The twist is preserving enough metadata to resolve equal scores cleanly.
- Extend to `m` parallel rows and ask for the maximum distance between any two rows for the same category. The harder part is maintaining row-aware extrema without blowing up state.
- Support online updates where `top[i]` or `bottom[i]` changes and queries arrive continuously. The twist is moving from one-pass aggregation to dynamic indexed maintenance.