# Minimum Cost to Reserve Compute Blocks with Fragmentation Penalty

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, optimization

---

## 🗂 Problem Overview
Given hourly compute demand, choose any number of contiguous reservation blocks so that the sum of block capacities at each hour matches demand exactly. Each block has a fixed setup fee, linear capacity-time cost, and a fragmentation penalty when its capacity exceeds the minimum demand inside its interval. Return the minimum total cost. The challenge is that blocks overlap and interact globally, so local greedy choices fail; the search space over all decompositions is exponential without interval DP.

## 🌍 Engineering Impact
This pattern shows up anywhere a time-varying requirement is implemented as layered contiguous reservations: cloud capacity commitments, CDN bandwidth shaping, reserved IOPS windows, ad-budget pacing, and batch scheduler slot allocation. The operational issue is fragmentation: long-lived allocations are cheaper operationally, but crossing low-demand valleys makes them inefficient or penalized. At scale, naive per-hour provisioning explodes control-plane churn, while greedy interval merging leaves money on the table. Interval DP gives a principled way to trade setup overhead against fragmentation, enabling predictable optimization under exact-capacity constraints rather than heuristic overbooking.

## 🔍 Problem Statement
You are given `n` hours (`1 <= n <= 200`), an array `demand[i]` (`0 <= demand[i] <= 10^6`), a `setupCost`, and a `fragmentationPenalty`. A reservation block is a contiguous interval `[l, r]` with fixed capacity `c`, contributing exactly `c` units to every hour in that interval. Multiple blocks may overlap, but for every hour the total contributed capacity must equal `demand[i]` exactly.

A block costs:

`setupCost + c * (r - l + 1) + fragmentationPenalty * max(0, c - min(demand[l..r]))`

Compute the minimum total cost; the answer fits in signed 64-bit.

Examples:

- `demand = [3,1,3], setupCost = 2, fragmentationPenalty = 4` → `13`
- `demand = [2,2,2,2], setupCost = 5, fragmentationPenalty = 3` → `13`

The decisive constraint is `n <= 200`: too large for exponential partition search, small enough for cubic or quartic interval DP.

## 🪜 How to Solve This
1. Read the block definition → each block adds a constant “layer” across a contiguous interval. That strongly suggests decomposing the demand histogram into horizontal strips, not vertical per-hour purchases.

2. Ask what the first block on an interval can look like → if we solve some subarray `[l, r]`, one natural move is to place a base layer of height `m = min(demand[l..r])` across the whole interval. Any optimal solution can be rearranged into layers without changing feasibility.

3. Why is that useful? → subtracting the interval minimum splits the remaining demand into independent positive segments. Once the base layer is paid for, hours that drop to zero disconnect the problem.

4. What about paying the base as one block versus several smaller ones? → because the layer spans exactly `[l, r]` at capacity `m`, its penalty is zero. The only remaining decision is where to recurse after subtraction.

5. This becomes interval DP → for every interval, either solve by splitting at some midpoint, or take the full-width minimum layer and recurse on the residual segments. That captures the global trade-off between extra setup fees and wider shared blocks.

## 🧩 Algorithm Walkthrough
1. **Define the DP state (Interval DP).**  
   Let `dp[l][r]` be the minimum cost to satisfy `demand[l..r]` exactly, assuming hours outside the interval are irrelevant. This is the right abstraction because every block is contiguous, and subtracting a full-width layer preserves contiguity inside subsegments.

2. **Precompute interval minima.**  
   For every interval `[l, r]`, compute `mn[l][r] = min(demand[l..r])`. This lets us price a full-width base block in O(1) during DP transitions.

3. **Baseline transition: split the interval.**  
   For every `k` in `[l, r-1]`, consider  
   `dp[l][r] = min(dp[l][k] + dp[k+1][r])`.  
   This maintains the invariant that `dp[l][r]` is at most the best partitioned solution.

4. **Layering transition: take the interval minimum as one block.**  
   Buy one block `[l, r]` with capacity `mn[l][r]`. Its cost is  
   `setupCost + mn[l][r] * (r-l+1)`  
   because the fragmentation term is zero: capacity equals the interval minimum. Subtracting this layer leaves residual demand `demand[i] - mn[l][r]`.

5. **Recurse only on positive residual segments.**  
   After subtraction, positions where residual becomes zero break dependence. Sum `dp` over maximal subsegments with positive residual. This is correct because no future block can cross a zero-residual hour without overprovisioning that hour.

6. **Fill by increasing interval length.**  
   Short intervals are solved first, then used to solve larger ones. The invariant is standard interval-DP closure: every referenced subinterval is already optimal when computing `dp[l][r]`.

7. **Result.**  
   `dp[0][n-1]` is the minimum total cost.

## 📊 Worked Example
Take `demand = [3,1,3]`, `setupCost = 2`, `fragmentationPenalty = 4`.

| Interval | `min` | Full-width block cost | Residual after subtracting `min` | Residual segments | Candidate total |
|---|---:|---:|---|---|---:|
| `[0,0]` | 3 | `2 + 3*1 = 5` | `[0]` | none | 5 |
| `[1,1]` | 1 | `2 + 1*1 = 3` | `[0]` | none | 3 |
| `[2,2]` | 3 | `2 + 3*1 = 5` | `[0]` | none | 5 |
| `[0,2]` | 1 | `2 + 1*3 = 5` | `[2,0,2]` | `[0,0]`, `[2,2]` | `5 + 5 + 5 = 15` |

Now compare with splits:

- `[0,0] + [1,2] = 5 + 8 = 13`
- `[0,1] + [2,2] = 8 + 5 = 13`

So `dp[0][2] = 13`. The optimal plan is one width-3 base layer of capacity 1 plus two single-hour top-up blocks of capacity 2.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^3)` with standard interval DP plus O(1) interval-min lookup and O(n) split enumeration per state. With `n <= 200`, this is comfortably practical. At `10^6` or `10^9` elements, cubic interval DP is completely infeasible; this solution relies on the deliberately small interval bound.

### Space Complexity
`O(n^2)` for the DP table and interval-min table. The space is owned by dense interval state, not recursion depth. You can reduce some auxiliary storage by computing minima on the fly, but that trades memory for extra time and usually worsens total runtime.

## 💡 Key Takeaways
- If the problem lets you apply a constant contribution over a contiguous range and demands exact reconstruction, think in terms of horizontal layers and interval DP.
- When subtracting an interval minimum causes zero-valued cut points that isolate subproblems, that is a strong signal the interval decomposition is structurally correct.
- The fragmentation penalty is easy to overthink here: for the canonical minimum-layer transition, it vanishes because the chosen capacity equals the interval minimum.
- Be careful about residual segmentation after subtraction; only maximal positive runs recurse, and crossing a zero creates invalid overprovisioning.
- In production optimization, fixed setup costs plus contiguous allocations usually imply that “merge everything” and “split everything” are both wrong; the scalable design insight is to model the merge/split frontier explicitly.

## 🚀 Variations & Further Practice
- Allow block capacity to vary by hour within a block, but charge a smoothness penalty between adjacent hours; this breaks the clean layer decomposition and pushes the problem toward sequence DP or min-cost flow.
- Add a limit on the total number of blocks; now the state must track both interval cost and block count, introducing another DP dimension and harder trade-offs.
- Replace the fragmentation term with a penalty based on `max(demand[l..r]) - min(demand[l..r])`; this couples interval shape directly into block pricing and weakens the zero-penalty minimum-layer simplification.