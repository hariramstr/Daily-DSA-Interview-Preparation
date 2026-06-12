# Minimum Adjustments to Create K Rising Price Blocks

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Partition DP

---

## 🗂 Problem Overview
Given an array `prices` and an integer `k`, split the array into exactly `k` non-empty contiguous blocks. You may edit any value, paying absolute-difference cost, so that each block becomes non-decreasing after modification. Return the minimum total edit cost across all blocks. The difficulty is that you must optimize two coupled decisions at once: where to place block boundaries and how to minimally reshape each chosen subarray into a non-decreasing sequence.

## 🌍 Engineering Impact
This pattern shows up in systems that segment noisy ordered data into locally monotone regimes: pricing pipelines, telemetry smoothing, time-series compression, ranking score calibration, and piecewise trend fitting in forecasting services. At scale, brute-forcing segment boundaries or local repairs explodes combinatorially and blocks online or batch SLAs. The useful abstraction is to precompute the optimal repair cost for every interval, then run partition DP over those interval costs. That separation of concerns turns an intractable joint optimization into a predictable dynamic program with clear memory and latency bounds.

## 🔍 Problem Statement
You are given an integer array `prices` of length `n` and an integer `k`, where `1 <= n <= 200`, `1 <= k <= n`, and `-10^9 <= prices[i] <= 10^9`. You may change `prices[i]` to any integer `x` at cost `|prices[i] - x|`. Partition the array into exactly `k` non-empty contiguous blocks so that, after modification, every block is non-decreasing. Return the minimum total adjustment cost.

A block of length `1` always satisfies the constraint. Blocks are independent after partitioning: values in one block do not constrain neighboring blocks.

Examples:

- `prices = [5, 1, 4, 2], k = 2` → `1`
- `prices = [7, 3, 6, 3, 8], k = 3` → `3`

The key constraint is not `n` alone, but the combination of arbitrary edits plus exact `k` contiguous partitions. That rules out greedy splitting and points directly to interval preprocessing plus partition DP.

## 🪜 How to Solve This
1. Read the objective carefully → this is not just partitioning, and not just sequence repair. We must jointly choose boundaries and edited values.

2. Separate the concerns → if we knew the cost to convert any subarray `prices[l..r]` into a non-decreasing sequence, then the global problem becomes: pick `k` intervals whose costs sum to minimum.

3. That immediately suggests **Partition DP** → `dp[i][b]` = minimum cost to cover the first `i` elements using exactly `b` blocks.

4. The real work is interval cost preprocessing → for each subarray, compute the minimum `L1` cost to fit a non-decreasing sequence. This is an instance of isotonic regression under absolute loss.

5. Because the edited values can be assumed to come from the set of observed values in the interval, we can discretize candidate heights and solve each interval with DP:
   - choose a value for each position,
   - enforce non-decreasing order,
   - minimize total absolute deviation.

6. Once every `cost[l][r]` is known, the outer DP is standard: try every previous cut `p < i`, and transition from `dp[p][b-1] + cost[p][i-1]`.

That decomposition is the whole idea: expensive local optimization once, cheap global composition afterward.

## 🧩 Algorithm Walkthrough
1. **Precompute interval repair costs (`cost[l][r]`)** using **interval DP + value discretization**.  
   For a fixed subarray `prices[l..r]`, collect and sort its values into `vals`. Under `L1` loss, an optimal non-decreasing fitted sequence can be chosen from these candidate values. Define `f[pos][t]` as the minimum cost to fit `prices[l..pos]` where the fitted value at `pos` is `vals[t]`. The transition is:
   `f[pos][t] = |prices[pos] - vals[t]| + min(f[pos-1][u]) for all u <= t`.
   Prefix minima make this efficient.  
   **Invariant:** fitted values never decrease because transitions only come from `u <= t`.

2. **Fill all intervals.**  
   Run the above for every `l`, extending `r` from `l` to `n-1`. Store the minimum over the last row as `cost[l][r]`.  
   **Why correct:** each interval is solved independently for the exact local constraint “make this block non-decreasing at minimum absolute adjustment cost.”

3. **Run partition DP** with the explicit pattern **Partition DP**.  
   Let `dp[i][b]` be the minimum cost to partition the prefix `prices[0..i-1]` into exactly `b` valid blocks. Initialize `dp[0][0] = 0`, others to infinity.

4. **Transition over the last cut.**  
   For each `i` and `b`, try every `p` with `b-1 <= p < i`:
   `dp[i][b] = min(dp[i][b], dp[p][b-1] + cost[p][i-1])`.  
   **Invariant:** after processing, `dp[i][b]` is the optimal cost for the first `i` elements using exactly `b` contiguous blocks.

5. **Return `dp[n][k]`.**  
   This is globally optimal because every feasible solution has a last block, and the DP enumerates all such last-block choices over exact prefixes.

## 📊 Worked Example
Take `prices = [7, 3, 6, 3, 8]`, `k = 3`.

First compute a few interval costs:

| Interval | Best non-decreasing fit | Cost |
|---|---|---:|
| `[7]` | `[7]` | 0 |
| `[3,6,3]` | `[3,4,4]` | 3 |
| `[6,3]` | `[3,3]` or `[6,6]` | 3 |
| `[8]` | `[8]` | 0 |

Then partition DP:

1. `dp[1][1] = cost[0][0] = 0`
2. `dp[4][2]` considers splits of `[7,3,6,3]` into 2 blocks. Best is `p = 1`:  
   `dp[1][1] + cost[1][3] = 0 + 3 = 3`
3. `dp[5][3]` considers final block ending at index `4`. Best is `p = 4`:  
   `dp[4][2] + cost[4][4] = 3 + 0 = 3`

So the optimal partition is `[7] | [3,6,3] | [8]`, total cost `3`.

## ⏱ Complexity Analysis
### Time Complexity
A practical formulation is `O(n^4)` overall: `O(n^3)` interval preprocessing when each subarray DP is run over its length-sized candidate value set with prefix minima, plus `O(k n^2)` for partition DP. With `n <= 200`, this is acceptable. At `10^6` or `10^9` scale, this class of exact dynamic programming is not viable without stronger structure or approximation.

### Space Complexity
`O(n^2)` for the interval cost table plus `O(nk)` for the partition DP table. Temporary per-interval DP can be kept to `O(n)` or `O(n^2)` depending on implementation style. Space can be reduced in the outer DP with rolling arrays, but the `cost[l][r]` table is usually worth retaining.

## 💡 Key Takeaways
- If a problem asks for **exactly `k` contiguous groups** and each group has an independent optimization cost, it is a strong Partition DP signal.
- If each segment must be transformed to satisfy an order property at minimum edit cost, look for **interval preprocessing** before global partitioning.
- Be precise about indexing: `dp[i][b]` usually means first `i` elements, while `cost[l][r]` is inclusive; mixing half-open and closed intervals is the fastest way to ship a wrong answer.
- Enforce non-empty blocks in transitions: the last cut `p` must satisfy `p < i`, and valid states require at least `b` elements to form `b` blocks.
- The transferable design insight is to decouple expensive local normalization from global segmentation, then compose them with a second-stage optimizer.

## 🚀 Variations & Further Practice
- Require each block to be **strictly increasing** instead of non-decreasing; the local interval solver changes because equal fitted values are no longer legal.
- Add a **fixed penalty per block** and ask for the unconstrained optimum number of blocks; this turns exact-`k` partition DP into cost-regularized segmentation.
- Replace absolute deviation with **squared error**; the interval subproblem becomes isotonic regression under `L2`, which admits different optimization structure and faster specialized solvers.

---