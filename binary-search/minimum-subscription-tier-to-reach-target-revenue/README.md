# Minimum Subscription Tier to Reach Target Revenue

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Array

---

## 🗂 Problem Overview
Given customer spending limits and a target revenue, find the smallest integer subscription price `p` such that charging `p` to every customer willing to pay at least `p` produces revenue `>= targetRevenue`. Revenue is `p * count(limits[i] >= p)`. Return `-1` if no price works. The challenge is scale: limits can be as large as `10^9`, so scanning every possible price is infeasible even though the answer space is numeric.

## 🌍 Engineering Impact
This pattern shows up anywhere a global threshold controls participation and aggregate yield: pricing engines, ad auction reserve tuning, API plan gating, autoscaling cutoffs, fraud-score thresholds, and search/ranking score filters. At production scale, brute-forcing thresholds across a huge numeric domain is wasted work because only boundary changes matter. Sorting plus binary search turns a large continuous-looking search space into a small set of decision points with predictable latency. That matters in services where threshold recomputation sits on hot paths, batch optimization jobs, or control loops that must remain stable under large input cardinality and wide value ranges.

## 🔍 Problem Statement
You are given an integer array `limits` where `limits[i]` is the highest subscription tier customer `i` will accept, and an integer `targetRevenue`. If the company sets a global price `p`, then every customer with `limits[i] >= p` subscribes. Total revenue is:

`revenue(p) = p * number_of_customers_with_limit_at_least_p`

Return the minimum integer price `p` such that `revenue(p) >= targetRevenue`. If no such integer price exists, return `-1`.

Constraints:
- `1 <= limits.length <= 2 * 10^5`
- `1 <= limits[i] <= 10^9`
- `1 <= targetRevenue <= 10^18`

Examples:
- `limits = [3, 8, 6, 6, 10], targetRevenue = 18` → `5`
- `limits = [2, 2, 2], targetRevenue = 10` → `-1`

The key constraint is the price domain: values up to `10^9` rule out linear scans over all possible prices. Also, revenue can exceed 32-bit range, so arithmetic must use 64-bit integers.

## 🪜 How to Solve This
1. Start from the revenue formula: for a chosen price `p`, only the number of customers with `limit >= p` matters. That immediately suggests sorting, because sorted thresholds let us count eligible customers quickly.

2. Notice the search space is numeric and huge (`1..10^9`). Brute force over prices is dead on arrival. That is the first signal for binary search over the answer.

3. Ask whether the predicate is monotonic: “does there exist enough revenue at price `p`?” The raw revenue function is not globally monotonic, so binary-searching directly on `revenue(p)` is unsafe.

4. Shift perspective: between adjacent distinct customer limits, the paying customer count is constant. Within such an interval, revenue increases linearly with `p`, so the minimum valid price for that fixed customer count is easy to compute.

5. After sorting, if exactly the suffix starting at index `i` can pay, then `count = n - i`, and the smallest price that reaches the target is `ceil(targetRevenue / count)`. That candidate is valid only if it lies above the previous limit and at most the current limit.

6. Scan all suffix counts once and return the smallest valid candidate. Sorting gives the structure; the per-position check gives the answer efficiently.

## 🧩 Algorithm Walkthrough
1. **Sort the limits array in nondecreasing order.**  
   This is the enabling step for a **sorting + threshold search** pattern. After sorting, every feasible customer set for a price `p` is a suffix of the array. That converts an arbitrary subset-counting problem into indexed suffix arithmetic.

2. **Interpret each index `i` as a participation boundary.**  
   If `p <= limits[i]` and `p > limits[i - 1]` (or `i = 0`), then exactly customers `i..n-1` subscribe. The invariant is: for any price inside this interval, the subscriber count is fixed at `count = n - i`.

3. **Compute the minimum price needed for this fixed count.**  
   For `count = n - i`, the smallest price that can hit the target is  
   `needed = ceil(targetRevenue / count)`.  
   This is correct because with fixed `count`, revenue is linear: `p * count`.

4. **Validate whether `needed` belongs to this interval.**  
   It must satisfy:
   - `needed <= limits[i]` so these `count` customers still buy.
   - `needed > limits[i - 1]` for `i > 0`, otherwise more customers would also qualify and this boundary assumption would be wrong.  
   This preserves the interval invariant.

5. **Return the first valid `needed` while scanning left to right.**  
   Earlier indices mean larger customer counts, which generally allow smaller feasible prices. Scanning in sorted order and checking each boundary yields the minimum valid integer price.

6. **If no boundary admits a valid price, return `-1`.**  
   This means even the best achievable revenue across all suffixes is below target.

## 📊 Worked Example
Use `limits = [3, 8, 6, 6, 10]`, `targetRevenue = 18`.

Sorted: `[3, 6, 6, 8, 10]`

| i | Active customers (`n-i`) | Interval for `p` | `needed = ceil(18 / count)` | Valid? |
|---|---:|---|---:|---|
| 0 | 5 | `1..3` | 4 | No, `4 > 3` |
| 1 | 4 | `4..6` | 5 | Yes |
| 2 | 3 | `7..6` | 6 | Invalid interval |
| 3 | 2 | `7..8` | 9 | No, `9 > 8` |
| 4 | 1 | `9..10` | 18 | No, `18 > 10` |

At `i = 1`, exactly four customers can pay for any price in `[4, 6]`. The minimum price that reaches the target with four customers is `ceil(18/4) = 5`, and `5` lies in that interval. Revenue is `5 * 4 = 20`, so the answer is `5`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by a single `O(n)` scan over suffix boundaries. With `n = 2 * 10^5`, this is comfortably practical. At `10^6`, it is still routine in most backends. A scan over the numeric price domain up to `10^9` would be categorically non-viable.

### Space Complexity
`O(1)` auxiliary space beyond the sort, if sorting in place is allowed; otherwise it depends on the language runtime’s sorting implementation. The main memory cost is the sorted array itself. You can avoid extra structures entirely, at the cost of mutating input.

## 💡 Key Takeaways
- If the input defines “who qualifies above a threshold,” sort first and think in suffixes or prefixes rather than individual prices.
- When the numeric answer range is huge but behavior changes only at data boundaries, search over structural breakpoints, not the full domain.
- The revenue function itself is not globally monotonic; binary-searching directly on `p -> revenue(p) >= target` is a trap.
- Use 64-bit arithmetic for both `targetRevenue` and `p * count`; this problem will overflow 32-bit integers in valid test cases.
- In production systems, threshold optimization becomes tractable when you collapse continuous-looking parameter spaces into a finite set of state transitions induced by sorted inputs.

## 🚀 Variations & Further Practice
- **Maximum revenue instead of minimum valid price:** find the price that maximizes `p * count`. The twist is optimization over breakpoints rather than feasibility against a target.
- **Per-customer weights or seat counts:** each customer contributes different revenue weight, so suffix counts become suffix sums and the invariant changes from cardinality to aggregate capacity.
- **Online updates to limits with repeated queries:** support inserts/deletes and threshold queries efficiently. The twist is moving from one-time sorting to balanced trees, Fenwick trees, or segment trees over compressed coordinates.