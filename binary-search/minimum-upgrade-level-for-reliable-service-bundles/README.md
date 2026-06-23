# Minimum Upgrade Level for Reliable Service Bundles

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Prefix Sum, Fenwick Tree

---

## 🗂 Problem Overview
Given `levels`, a threshold `target`, and a required count `k`, choose the smallest integer upgrade level `X` such that after replacing every `levels[i] < X` with `X`, at least `k` contiguous subarrays have sum at least `target`. Return that minimum `X`, or `0` if the original array already works. The challenge is scale: there are `O(n^2)` bundles, so brute-force counting is infeasible for `n` up to `2 * 10^5`.

## 🌍 Engineering Impact
This pattern shows up whenever a global control knob changes many local metrics and you need the minimum setting that makes enough windows, ranges, or cohorts pass a service-level threshold. Examples include distributed rate-limit floors, minimum replica quality in storage tiers, streaming pipeline backpressure tuning, and search/ranking score normalization. At production scale, the failure mode is obvious: naive recomputation over all ranges explodes quadratically. The binary-search-plus-fast-feasibility pattern turns an impossible tuning problem into a predictable control loop, which is exactly what you want for capacity planning, rollout safety checks, and automated policy calibration.

## 🔍 Problem Statement
You are given an array `levels` of length `n`, where `0 <= levels[i] <= 10^9`. For an integer policy strength `X`, define:

- `final[i] = max(levels[i], X)`

A contiguous bundle `(l, r)` is reliable if:

- `sum(final[l..r]) >= target`

Your task is to find the minimum integer `X` in `[0, 10^9]` such that the number of reliable contiguous bundles is at least `k`. If the original array already yields at least `k` reliable bundles, return `0`.

Constraints:

- `1 <= n <= 2 * 10^5`
- `1 <= target <= 10^18`
- `1 <= k <= n * (n + 1) / 2`

Examples:

- `levels = [1,3,2], target = 5, k = 4` → `2`
- `levels = [0,0,4,1], target = 4, k = 8` → `3`

The decisive constraint is `n = 2 * 10^5`: counting all subarrays directly is not viable, so each feasibility check must be near `O(n log n)`.

## 🪜 How to Solve This
1. Read the objective → we are not asked to maximize anything over subarrays directly; we are asked for the minimum `X` that makes a monotone condition true.
2. Monotone condition → if some `X` works, any larger `X` also works, because `max(levels[i], X)` never decreases. That immediately suggests binary search on the answer.
3. Now reduce the problem → for a fixed `X`, we only need to count how many subarrays have sum at least `target` in the transformed array.
4. Counting subarrays by brute force is still too slow → think prefix sums. A subarray sum `prefix[r+1] - prefix[l] >= target` becomes `prefix[l] <= prefix[r+1] - target`.
5. So for each right endpoint, we need the number of earlier prefix sums below a threshold. That is an order-statistics query over a dynamic set.
6. Prefix sums are large and not bounded tightly → coordinate-compress them, then use a Fenwick Tree to maintain counts of seen prefix sums.
7. Combine both layers: binary search over `X`, and for each candidate, run one `O(n log n)` prefix-sum counting pass.

## 🧩 Algorithm Walkthrough
1. **Exploit monotonicity with Binary Search on Answer.**  
   Define `works(X)` = “after upgrading to `X`, at least `k` subarrays have sum `>= target`.” Since increasing `X` only increases or preserves every element, `works(X)` is monotone. This invariant justifies binary search over `X` in `[0, 10^9]`.

2. **Build the transformed prefix sums for a fixed `X`.**  
   Compute `final[i] = max(levels[i], X)` and prefix sums `P[0] = 0`, `P[i+1] = P[i] + final[i]`. Then each subarray `(l, r)` has sum `P[r+1] - P[l]`.

3. **Rewrite the counting condition.**  
   For each `j = r+1`, we need the number of prior indices `i < j` such that `P[i] <= P[j] - target`. This is now a prefix-count query, not a range-sum problem.

4. **Use Prefix Sum + Fenwick Tree.**  
   Collect all prefix sums `P[*]`, sort unique values, and coordinate-compress them. As you scan `j` from left to right, query how many seen prefix sums are `<= P[j] - target`, then insert `P[j]` into the Fenwick Tree. The invariant: the tree always stores counts of prefix sums from indices `< j`.

5. **Short-circuit when possible.**  
   If the running count reaches `k`, stop early. Feasibility only needs to know whether the count is at least `k`, not the exact total.

6. **Return the smallest valid `X`.**  
   First check `works(0)`; if true, return `0`. Otherwise binary search for the leftmost `X` with `works(X) = true`.

This is the right abstraction because the problem is two nested monotone/counting patterns: **Binary Search on Answer** outside, **Prefix Sums + Fenwick Tree** inside.

## 📊 Worked Example
Take `levels = [1,3,2]`, `target = 5`, `k = 4`, and test `X = 2`.

Transformed array: `final = [2,3,2]`  
Prefix sums: `P = [0,2,5,7]`

We scan prefix sums and count prior values `<= P[j] - 5`.

| j | P[j] | Need prior `<=` | Seen prefix sums | Count added |
|---|------|------------------|------------------|-------------|
| 0 | 0    | -5               | {}               | 0 |
| 1 | 2    | -3               | {0}              | 0 |
| 2 | 5    | 0                | {0,2}            | 1 |
| 3 | 7    | 2                | {0,2,5}          | 2 |

Total so far is `3`, but note the scan includes all valid `(l, r)` pairs through prefix indexing: the valid subarrays are `[2,3]`, `[3]`, `[3,2]`, `[2,3,2]`, so total `4`. Therefore `works(2)` is true. Testing `X = 1` yields only `3`, so the minimum valid answer is `2`.

## ⏱ Complexity Analysis
### Time Complexity
Each feasibility check for a fixed `X` is `O(n log n)`: `O(n)` to build transformed prefix sums, plus `O(n log n)` for coordinate compression and Fenwick queries/updates. Binary search over `X` adds a factor of about `log2(1e9) ≈ 30`, giving `O(n log n log V)`. This is practical at `2 * 10^5`, but impossible at `10^6` without tighter constants.

### Space Complexity
`O(n)` auxiliary space. The dominant structures are the prefix-sum array, compressed coordinate list, and Fenwick Tree. You can stream some parts to reduce temporary allocations, but asymptotically it stays linear unless you replace the counting structure with a more complex online ordered map.

## 💡 Key Takeaways
- If the question asks for the minimum parameter value that makes a condition true, check for monotonicity first; that is the strongest signal for binary search on the answer.
- If a subarray condition is about sums crossing a threshold, rewrite it with prefix sums before considering any data structure.
- Be careful with prefix indexing: subarray `(l, r)` maps to `P[r+1] - P[l]`, so the scan runs over `n + 1` prefix sums, not `n` elements.
- The Fenwick query must count prefix sums `<= P[j] - target`, which usually means coordinate-compressing both exact prefix values and using upper-bound lookup for the threshold.
- At scale, the winning design is often “global monotone search + fast local feasibility,” not trying to solve the full optimization directly.

## 🚀 Variations & Further Practice
- Require the number of reliable bundles to be **exactly** `k` instead of at least `k`; monotonicity breaks, so binary search no longer applies cleanly.
- Replace the global floor `max(levels[i], X)` with a **budgeted upgrade** where raising service `i` costs `X - levels[i]`; now feasibility couples elements and becomes an optimization problem.
- Ask for the minimum `X` such that at least `k` bundles have **average** reliability at least `target`; this introduces length-normalized constraints and changes the prefix-sum transformation.