# Minimum Cost to Stabilize a Conveyor Belt with Optional Buffer Pads

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
You must cover an array of `n` conveyor segments exactly once using either single-segment repairs or valid adjacent two-segment buffered repairs. Each chosen operation may optionally consume one of at most `k` coupons, reducing its cost by `discount` but never below zero. The goal is to minimize total repair cost. The problem is non-trivial because local choices interact: pairing one segment changes future pairing availability, and coupon placement must be optimized globally across the full left-to-right covering.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must tile a linear resource with mutually exclusive actions under budget modifiers: packet batching in streaming pipelines, instruction bundling in compilers, storage block repair scheduling, and ad-slot packing with promotional credits. At scale, greedy decisions fail because a cheap local merge can block a better downstream arrangement or waste a limited discount on the wrong unit of work. Dynamic programming is what turns an exponential search over coverings and coupon assignments into a predictable, bounded optimization pass, which is exactly what production schedulers and planners need when correctness and cost efficiency both matter.

## 🔍 Problem Statement
Given:

- `instability[0..n-1]`
- `singleCost[0..n-1]`
- `pairCost[0..n-2]`
- `limit`, `k`, and `discount`

cover every segment exactly once using:

1. **Single repair** on segment `i` with cost `singleCost[i]`
2. **Buffered repair** on segments `i` and `i+1` with cost `pairCost[i]`, allowed only when  
   `abs(instability[i] - instability[i+1]) <= limit`

A coupon may be applied to any chosen operation, reducing its cost to `max(0, cost - discount)`. At most `k` coupons may be used, and using fewer is allowed.

Return the minimum total cost.

Constraints force an efficient solution:

- `1 <= n <= 2000`
- `0 <= k <= 2000`
- costs and values up to `10^9`

Examples:

- `instability=[4,7,6,6], singleCost=[8,5,7,4], pairCost=[9,20,6], limit=2, k=1, discount=3` → `16`
- `instability=[3,3,10,11,12], singleCost=[6,6,8,5,5], pairCost=[7,50,6,9], limit=1, k=2, discount=4` → `10`

The key algorithmic driver is the `n, k <= 2000` bound: exponential covering choices are impossible, but `O(nk)` dynamic programming is feasible.

## 🪜 How to Solve This
1. Read the problem → this is a **cover the array exactly once** problem, so decisions are naturally made on prefixes.
2. Each position can be handled in only two structural ways:
   - cover segment `i` alone
   - cover segments `i` and `i+1` together, if valid
3. That immediately suggests DP on “how much of the array is already covered.”
4. Coupons create a second dimension: the best answer for a prefix depends on **how many coupons have already been spent**, not just the prefix length.
5. So define a state like: minimum cost to cover the first `i` segments using exactly `c` coupons.
6. From that state, try all legal next operations:
   - single without coupon
   - single with coupon
   - pair without coupon
   - pair with coupon
7. Because every operation consumes either 1 or 2 segments and at most 1 coupon, transitions are constant-time.
8. The result is the minimum over all states covering all `n` segments with coupon count `0..k`.

This is the standard mental model for interval tiling with resource-constrained discounts: prefix DP plus a budget dimension.

## 🧩 Algorithm Walkthrough
1. **Define the DP pattern: prefix dynamic programming with resource tracking.**  
   Let `dp[i][c]` be the minimum cost to cover the first `i` segments (`0..i-1`) using exactly `c` coupons. This is the right abstraction because every valid solution is a left-to-right tiling of the array.

2. **Initialize the base case.**  
   Set `dp[0][0] = 0` and all other states to infinity. The invariant is: if `dp[i][c]` is finite, it represents a valid exact cover of the prefix of length `i`.

3. **Transition with a single repair.**  
   From state `(i, c)`, if `i < n`, repair segment `i` alone:
   - no coupon → `dp[i+1][c] = min(dp[i+1][c], dp[i][c] + singleCost[i])`
   - with coupon, if `c < k` → add `max(0, singleCost[i] - discount)`

   This preserves exact coverage because segment `i` is consumed once.

4. **Transition with a buffered repair.**  
   If `i + 1 < n` and `abs(instability[i] - instability[i+1]) <= limit`, then segments `i` and `i+1` may be covered together:
   - no coupon → update `dp[i+2][c]`
   - with coupon, if `c < k` → add `max(0, pairCost[i] - discount)` and update `dp[i+2][c+1]`

   The invariant remains intact: the next uncovered segment is always exactly `i` before the move and advances to `i+2` after a valid pair.

5. **Take the best completed state.**  
   The answer is `min(dp[n][c])` for `0 <= c <= k`, since coupons are optional.

6. **Why this is correct.**  
   Every legal solution decomposes into a sequence of single/pair operations on the next uncovered segment. The DP enumerates exactly those choices and records the cheapest cost for each prefix/coupon combination. Optimal substructure holds because once a prefix is fixed, the remaining suffix cost depends only on the next index and coupons used so far.

## 📊 Worked Example
Use Example 2:

`instability=[3,3,10,11,12]`  
`single=[6,6,8,5,5]`, `pair=[7,50,6,9]`, `limit=1`, `k=2`, `discount=4`

| State | Decision | New State | Added Cost | Total |
|---|---|---:|---:|---:|
| `dp[0][0]=0` | pair `(0,1)` with coupon | `dp[2][1]` | `max(0,7-4)=3` | `3` |
| `dp[2][1]=3` | pair `(2,3)` with coupon | `dp[4][2]` | `max(0,6-4)=2` | `5` |
| `dp[4][2]=5` | single `4` without coupon | `dp[5][2]` | `5` | `10` |

Why not pair `(3,4)` instead? Because choosing `(2,3)` blocks `(3,4)`, and DP evaluates both coverings.  
Why not spend a coupon on segment `4`? No coupons remain in this path, and even if one did, earlier use on the cheaper pair structure is what enables the global optimum. Final answer: `10`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * k)`. There are `(n + 1) * (k + 1)` states, and each state performs a constant number of transitions: single with/without coupon, and pair with/without coupon when valid. At `n, k <= 2000`, this is about 4 million states, which is practical. At `10^6` scale it would not be; at `10^9`, completely infeasible without stronger structure.

### Space Complexity
`O(n * k)` for the full DP table. The table owns essentially all memory. This can be reduced to `O(k)` with rolling arrays because transitions only go from `i` to `i+1` or `i+2`, but implementation becomes more delicate due to two-step updates.

## 💡 Key Takeaways
- If a problem says “cover the array exactly once” with local choices consuming 1 or 2 items, think prefix DP before considering anything more exotic.
- If optimization depends on both position and a bounded consumable resource like coupons, promotions, or edits, add that resource as a second DP dimension.
- Be careful with indexing: `pairCost[i]` corresponds to segments `i` and `i+1`, so pair transitions are only valid when `i + 1 < n`.
- Coupons apply per operation, not per segment; a paired repair consumes one coupon total, not two.
- The production-grade lesson is that constrained tiling plus limited incentives is rarely greedily composable; explicit state modeling is what prevents local savings from degrading global cost.

## 🚀 Variations & Further Practice
- Allow buffered repairs of length 2 or 3, each with different validity rules and costs. The twist is a larger transition set and more opportunities for subtle blocking between local groupings.
- Make coupon discounts heterogeneous, where coupon `j` has value `discount[j]` and can be used once. This turns the second dimension from “count used” into a more complex allocation problem.
- Add a penalty for switching between repair types across adjacent operations. The twist is that DP state must now also track the previous operation type, not just prefix length and coupons used.