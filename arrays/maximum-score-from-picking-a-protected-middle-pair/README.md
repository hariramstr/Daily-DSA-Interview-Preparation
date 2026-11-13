# Maximum Score from Picking a Protected Middle Pair

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Monotonic Stack, Prefix/Suffix

---

## 🗂 Problem Overview
Given an integer array `nums`, choose endpoints `i < j` with at least one element between them (`j - i >= 2`). The score is `nums[i] + nums[j] + min(nums[i+1..j-1])`. Return the maximum score over all valid pairs.

The challenge is that the interior minimum couples both endpoints to the whole interval. A naive `O(n^2)` scan over all pairs is too slow for `n` up to `200000`, so the solution must exploit interval structure.

## 🌍 Engineering Impact
This pattern shows up anywhere a boundary decision is gated by the weakest interior signal: search ranking with span penalties, streaming anomaly windows with guard-band thresholds, portfolio/risk bands where endpoints matter but the worst in-window metric constrains viability, or network path scoring where edge nodes are attractive only if the path bottleneck is acceptable.

At scale, brute-force interval evaluation collapses under quadratic growth and becomes operationally irrelevant for real-time systems. The useful abstraction is to let each element act as the bottleneck for a maximal region, then optimize endpoints within that region. That shift turns pairwise enumeration into linear-time structural decomposition.

## 🔍 Problem Statement
You are given an integer array `nums` where `3 <= nums.length <= 200000` and `-10^9 <= nums[i] <= 10^9`.

Choose indices `i` and `j` such that:
- `i < j`
- `j - i >= 2` so the middle segment is non-empty

Define the score as:

`nums[i] + nums[j] + min(nums[i+1..j-1])`

Return the maximum possible score among all valid pairs.

Examples:

- `nums = [5, 1, 4, 3]` → `10`  
  Best pair: `(0, 2)` → `5 + 4 + min([1]) = 10`

- `nums = [2, 7, -3, 6, 1]` → `10`  
  Best pair: `(1, 3)` → `7 + 6 + min([-3]) = 10`

Negative values matter: the best answer is not simply the two largest endpoints. The key constraint is `n = 200000`, which rules out checking all intervals or all endpoint pairs.

## 🪜 How to Solve This
1. Start from the hard part: the score depends on the **minimum inside the interval**, not on the endpoints alone. That usually means the interval should be analyzed from the perspective of its minimum element.

2. Reframe the problem → suppose index `k` is the interior minimum used in the score. Then both endpoints must lie on opposite sides of `k`, and every element between them must be `>= nums[k]`.

3. That immediately suggests a **maximal span** around each `k` where `nums[k]` remains the minimum. This is classic monotonic-stack territory: find the nearest strictly smaller element on the left and right.

4. Inside that span, if `k` is the minimum, the best score using `k` is just:
   `nums[k] + (best endpoint on the left) + (best endpoint on the right)`.

5. So the problem decomposes cleanly:
   - for each index, find its valid left/right boundary where it can serve as the protected minimum
   - within those boundaries, query the maximum value on each side

6. Range maxima over many intervals suggest prefix/suffix precomputation or a segment tree. Since the intervals are arbitrary, a segment tree gives the clean `O(n log n)` solution with straightforward correctness.

## 🧩 Algorithm Walkthrough
1. **Treat each index as the candidate protected minimum.**  
   If index `k` is the minimum of the interior segment, then any valid pair must choose one endpoint left of `k` and one right of `k`. This converts interval scoring into “best left endpoint + best right endpoint + nums[k]”.

2. **Compute nearest strictly smaller elements with a monotonic increasing stack.**  
   For each `k`, find:
   - `L[k]`: nearest index left of `k` with `nums[L[k]] < nums[k]`
   - `R[k]`: nearest index right of `k` with `nums[R[k]] < nums[k]`  
   Then `nums[k]` is minimal throughout the open interval `(L[k], R[k])`. This is the maximal region where `k` can be the protected minimum.

3. **Derive valid endpoint ranges.**  
   Endpoints must exclude `k` itself and keep `k` strictly inside:
   - left endpoint range: `[L[k] + 1, k - 1]`
   - right endpoint range: `[k + 1, R[k] - 1]`  
   If either side is empty, `k` cannot be the interior minimum of any valid pair.

4. **Query the best endpoint on each side.**  
   Build a **segment tree for range maximum** over `nums`. For each `k`, query:
   - `leftMax = max(nums[L[k]+1 .. k-1])`
   - `rightMax = max(nums[k+1 .. R[k]-1])`

5. **Form the candidate score.**  
   `candidate = nums[k] + leftMax + rightMax`  
   This is correct because once `k` is fixed as the minimum, the endpoints contribute independently, so choosing the maximum on each side is optimal.

6. **Take the global maximum.**  
   Iterate all `k`, evaluate valid candidates, and return the largest.  
   The invariant is: every valid pair has at least one interior index that attains the interval minimum, and when that index is processed, the pair lies inside its admissible span.

## 📊 Worked Example
Use `nums = [2, 7, -3, 6, 1]`.

| k | nums[k] | L[k] | R[k] | left range | right range | leftMax | rightMax | score |
|---|---:|---:|---:|---|---|---:|---:|---:|
| 1 | 7  | 0 | 2 | empty | empty | — | — | invalid |
| 2 | -3 | -1 | 5 | `[0..1]` | `[3..4]` | 7 | 6 | 10 |
| 3 | 6  | 2 | 4 | empty | empty | — | — | invalid |

Trace:
1. For `k = 2`, nearest smaller on the left/right does not exist because `-3` is the smallest element, so its span covers the whole array.
2. Any valid pair using `k = 2` as the interior minimum must pick one endpoint from indices `0..1` and one from `3..4`.
3. Best left endpoint is `7` at index `1`; best right endpoint is `6` at index `3`.
4. Score becomes `-3 + 7 + 6 = 10`, which is optimal.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. The monotonic-stack passes are linear, `O(n)`. Then each index performs up to two range-maximum queries on the segment tree, each `O(log n)`, for total `O(n log n)`. At `10^6` elements this is still practical; at `10^9`, no in-memory exact scan is realistic without distribution or approximation.

### Space Complexity
`O(n)`. Space is owned by the left/right boundary arrays, the stack, and the segment tree. You can swap the segment tree for a sparse table if queries are static, but that increases constant factors and preprocessing footprint rather than reducing asymptotic space.

## 💡 Key Takeaways
- If a score depends on the **minimum inside a subarray**, treat each element as a candidate bottleneck and ask where it remains the controlling minimum.
- Nearest-smaller boundaries are a strong signal for **monotonic stack** decomposition: they turn interval enumeration into per-index maximal spans.
- Be precise about strictness: use **strictly smaller** boundaries so equal values do not incorrectly exclude valid spans where the same minimum can appear multiple times.
- Endpoint ranges are `[L[k]+1, k-1]` and `[k+1, R[k]-1]`; including `k` or the boundary indices is the easiest off-by-one bug in this problem.
- The transferable design move is to replace quadratic pair evaluation with a **structural ownership model**: assign each interval to a bottleneck element, then optimize locally within that ownership region.

## 🚀 Variations & Further Practice
- Return the maximum score for every index as the protected minimum, not just the global best. The twist is exposing per-index interval ownership and endpoint selection efficiently.
- Replace `min(interior)` with `max(interior)` or with `min + max`. The harder part is that the clean single-bottleneck decomposition breaks, forcing different range-query or dual-structure reasoning.
- Support online updates to `nums` and repeated score queries. The conceptual jump is from static preprocessing to dynamic interval maintenance with segment trees or more specialized balanced structures.