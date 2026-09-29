# Maximum Score from Picking a Protected Triple

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Binary Search, Prefix Maximum

---

## 🗂 Problem Overview
Given an integer array `nums`, choose indices `i < j < k` to maximize:

`(nums[i] + nums[j] + nums[k]) * min(k - j, j - i)`

Return the maximum score as a 64-bit integer, or `0` if fewer than three elements exist. The difficulty is that the objective couples value selection with distance symmetry around `j`: the best triple is not just the three largest numbers, and brute-force enumeration over all triples is infeasible at `n` up to `2 * 10^5`.

## 🌍 Engineering Impact
This pattern shows up whenever a score combines payload quality with a bottleneck distance or protection radius. Examples include search ranking with left/right context windows, streaming anomaly detection where a center event needs support on both sides, compiler optimization passes that score instruction groups by value and spacing, and distributed rate-limiters that depend on nearest surrounding checkpoints. At scale, naive enumeration collapses under combinatorial growth. The useful abstraction is to separate value maximization from distance feasibility, then answer many “best candidate within a radius” queries efficiently. That shift turns an impossible cubic scan into something deployable on production-sized inputs.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n` and must choose three indices `i < j < k`. The score of a triple is:

`(nums[i] + nums[j] + nums[k]) * min(k - j, j - i)`

The `min(...)` term is the protection radius of the middle index `j`: the smaller side limits the usable spacing. Return the maximum score over all valid triples. If `n < 3`, return `0`.

Constraints:

- `3 <= n <= 2 * 10^5`
- `-10^9 <= nums[i] <= 10^9`
- The answer may be negative
- Use 64-bit arithmetic

Examples:

- `nums = [5, 1, 4, 2, 6]` → best triple `(0, 2, 4)`, score `30`
- `nums = [-3, 7, -2, 8, -1]` → best triple `(1, 3, 4)`, score `14`

The key constraint is `n = 2 * 10^5`: `O(n^2)` and `O(n^3)` are dead on arrival.

## 🪜 How to Solve This
1. Start from the formula → the awkward part is `min(k - j, j - i)`. That suggests introducing a radius `d`, where both sides must be at least `d`: `i <= j - d` and `k >= j + d`.

2. For a fixed middle `j` and fixed radius `d`, the score becomes:

   `d * (nums[j] + bestLeft(j, d) + bestRight(j, d))`

   where `bestLeft(j, d)` is the maximum value in `nums[0..j-d]`, and `bestRight(j, d)` is the maximum value in `nums[j+d..n-1]`.

3. That immediately suggests prefix and suffix maxima. Once those are built, any fixed `(j, d)` can be evaluated in `O(1)`.

4. The remaining problem is avoiding all `O(n^2)` `(j, d)` pairs. Notice that for fixed `j`, the feasible `d` range is `1..min(j, n-1-j)`, and the score is a product of `d` and a non-increasing envelope of available left/right maxima.

5. Use binary search on breakpoints induced by prefix/suffix maxima changes: the best candidate only changes when `j-d` crosses a prefix-max update or `j+d` crosses a suffix-max update. Evaluate only those critical radii, not every radius.

That is the core reduction: turn triple search into radius queries over precomputed maxima.

## 🧩 Algorithm Walkthrough
1. **Build prefix maximum values and their change points**  
   Compute `prefMax[x] = max(nums[0..x])`. Also record indices where the prefix maximum strictly increases. This supports “best left value available up to `j-d`”. The invariant is: `prefMax[x]` is the optimal `nums[i]` for any `i <= x`.

2. **Build suffix maximum values and their change points**  
   Compute `sufMax[x] = max(nums[x..n-1])`, plus indices where the suffix maximum strictly increases when scanning from right to left. This supports “best right value available from `j+d` onward”. The invariant is: `sufMax[x]` is the optimal `nums[k]` for any `k >= x`.

3. **Fix the middle index `j`**  
   The feasible protection radius is `1 <= d <= limit`, where `limit = min(j, n-1-j)`. For any such `d`, score is  
   `d * (nums[j] + prefMax[j-d] + sufMax[j+d])`.

4. **Exploit piecewise-constant maxima**  
   As `d` grows, `j-d` moves left and `j+d` moves right. `prefMax[j-d]` changes only when `j-d` crosses a prefix-max breakpoint; `sufMax[j+d]` changes only when `j+d` crosses a suffix-max breakpoint. Between two consecutive breakpoints, the sum term is constant, so the best score in that interval is attained at the largest feasible `d`.

5. **Enumerate only critical radii**  
   For each `j`, use binary search over the breakpoint arrays to identify all interval boundaries induced by left and right changes within `[1, limit]`. Evaluate the score at the right endpoint of each interval. This is the key pattern: **Prefix/Suffix Maximum + Binary Search over change points**. It is the right abstraction because the expensive part is not querying maxima, but knowing where the query result can change.

6. **Track the global maximum in 64-bit arithmetic**  
   Initialize with a very small `long long` value, not `0`, because all valid triples may be negative. Return the best score found.

## 📊 Worked Example
Take `nums = [5, 1, 4, 2, 6]`.

Prefix maxima: `[5, 5, 5, 5, 6]`  
Suffix maxima: `[6, 6, 6, 6, 6]`

Consider each middle index:

| `j` | `nums[j]` | `limit` | candidate `d` | left max `prefMax[j-d]` | right max `sufMax[j+d]` | score |
|---|---:|---:|---:|---:|---:|---:|
| 1 | 1 | 1 | 1 | 5 | 6 | 12 |
| 2 | 4 | 2 | 1 | 5 | 6 | 15 |
| 2 | 4 | 2 | 2 | 5 | 6 | 30 |
| 3 | 2 | 1 | 1 | 5 | 6 | 13 |

For `j = 2`, both `prefMax[j-d]` and `sufMax[j+d]` stay constant as `d` grows from `1` to `2`, so the larger radius dominates. The best triple is `(0, 2, 4)` with score `(5 + 4 + 6) * 2 = 30`.

## ⏱ Complexity Analysis
### Time Complexity
With prefix/suffix maxima precomputed in `O(n)`, each middle index `j` is processed by binary-searching breakpoint arrays and evaluating only critical radii. Total complexity is `O(n log n)` in the practical implementation. That is routine at `10^6` scale and still fundamentally different from the impossible `O(n^2)` or `O(n^3)` regimes that explode toward `10^9` operations.

### Space Complexity
`O(n)` space for prefix maxima, suffix maxima, and breakpoint arrays. That is the dominant memory cost. You can reduce some auxiliary storage by recomputing or compressing change points, but the trade-off is more complex control flow and usually worse constant factors.

## 💡 Key Takeaways
- If a score contains `min(distanceLeft, distanceRight)`, introduce a radius `d` and rewrite the problem as a feasibility window on both sides of a center.
- If repeated queries ask for “best value up to index” or “best value from index onward,” prefix/suffix maxima are usually the first useful reduction.
- Do not initialize the answer to `0`; valid triples can all be negative, and the problem explicitly allows a negative optimum.
- Be careful with radius bounds: `d` must satisfy both `j-d >= 0` and `j+d < n`, so the upper bound is exactly `min(j, n-1-j)`.
- The transferable design insight is to identify where a query result can change, then search only those breakpoints instead of scanning every feasible parameter value.

## 🚀 Variations & Further Practice
- Replace `min(k-j, j-i)` with `max(k-j, j-i)`: the radius reduction no longer yields a clean symmetric feasibility window, so the breakpoint structure changes substantially.
- Allow online updates to `nums` between queries: prefix/suffix maxima are no longer static, pushing the solution toward segment trees or Fenwick-tree-like range-max structures.
- Generalize from triples to protected `m`-tuples around one or more centers: the challenge becomes composing multiple distance constraints with range-maximum queries without reintroducing quadratic state.