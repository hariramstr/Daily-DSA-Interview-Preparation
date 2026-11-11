# Minimum Delay to Assemble a Multi-Stage Rocket

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, prefix-sum

---

## 🗂 Problem Overview
You are given `n` rocket stages in fixed order, with `buildTime[i]` and `stability[i]` for each stage. You must split the stages into exactly `k` non-empty consecutive batches. The cost of a batch `l..r` is the sum of build times in that interval plus the stability spread `max(stability[l..r]) - min(stability[l..r])`. Return the minimum total cost across all valid ordered partitions. The challenge is that local greedy cuts do not compose: every partition choice changes future interval costs.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered work must be partitioned into contiguous execution windows with a per-window aggregation penalty. Examples include streaming pipelines grouped into processing micro-batches, manufacturing scheduling with setup variance, log compaction windows, and query-plan segmentation in compilers or distributed data engines. At scale, brute-force partition search collapses immediately because the number of ordered cuts grows combinatorially. Interval precomputation plus dynamic programming turns an intractable search space into a predictable bounded computation, which is exactly the difference between a planner that works for hundreds of units and one that times out or produces unstable latency under production load.

## 🔍 Problem Statement
Given arrays `buildTime` and `stability`, each of length `n`, partition the stages `1..n` into exactly `k` non-empty contiguous batches while preserving order. For any batch covering indices `l..r`, its cost is:

`sum(buildTime[l..r]) + (max(stability[l..r]) - min(stability[l..r]))`

Return the minimum total cost over all such partitions.

Constraints:
- `1 <= k <= n <= 300`
- `1 <= buildTime[i] <= 10^6`
- `1 <= stability[i] <= 10^9`
- Answer fits in signed 64-bit integer

Examples:

- `buildTime = [4,2,7,3]`, `stability = [8,5,6,10]`, `k = 2` → `18`
- `buildTime = [5,1,4,2,6]`, `stability = [7,7,3,9,4]`, `k = 3` → `23`

Edge cases matter: `k = 1` means one full interval, `k = n` means every stage stands alone, and all arithmetic must use 64-bit types. The key algorithmic constraint is `n <= 300`: small enough for cubic DP, too large for exponential partition enumeration.

## 🪜 How to Solve This
1. Read the partition rule → the batches must be contiguous and preserve order. That is a strong signal for **prefix-based dynamic programming**, not greedy selection.

2. Inspect a single batch cost → it depends only on an interval `l..r`: sum of `buildTime`, max stability, min stability. That means interval cost can be precomputed independently of the partitioning logic.

3. Ask what the final decision looks like → if the last batch starts at `j+1` and ends at `i`, then everything before `j` must already be optimally partitioned into `p-1` batches. That gives the recurrence.

4. So define `dp[p][i]` = minimum cost to partition the first `i` stages into exactly `p` batches.

5. Transition by trying the last cut:
   `dp[p][i] = min(dp[p-1][j] + cost[j+1][i])` for all valid `j`.

6. To make interval lookup O(1), precompute `cost[l][r]`. Prefix sums handle build times; scanning outward while maintaining running min/max handles stability spread.

7. The result is a classic **interval-cost + partition-DP** solution: clean, reliable, and well within bounds for `n = 300`.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums for build times.**  
   Let `prefix[i]` be the sum of the first `i` build times. Then `sum(l..r) = prefix[r] - prefix[l-1]`. This isolates the additive part of any batch cost in O(1). Invariant: prefix sums always represent exact cumulative build time up to each index.

2. **Precompute every interval cost `cost[l][r]`.**  
   For each start `l`, extend `r` from `l` to `n`, maintaining running `mx` and `mn` over `stability[l..r]`. Then:
   `cost[l][r] = (prefix[r] - prefix[l-1]) + (mx - mn)`.  
   This is the **interval DP preprocessing** step. It is correct because batch cost depends only on interval-local aggregates, and those aggregates are updated monotonically as `r` grows.

3. **Define the DP state.**  
   Let `dp[p][i]` be the minimum cost to partition the first `i` stages into exactly `p` non-empty consecutive batches. Initialize all states to infinity. Set `dp[0][0] = 0`. Invariant: unreachable states remain infinity; reachable states encode optimal prefix partitions only.

4. **Apply the partition transition.**  
   For each batch count `p` from `1..k`, and each prefix length `i` from `p..n`, try every valid previous cut `j` from `p-1..i-1`:
   `dp[p][i] = min(dp[p][i], dp[p-1][j] + cost[j+1][i])`.  
   This is standard **partition DP**: the last batch is an interval, and the prefix before it must already be optimal.

5. **Return `dp[k][n]`.**  
   This is correct by optimal substructure: every valid `k`-batch partition of the first `n` stages has some final cut, and the recurrence enumerates all such cuts exactly once.

## 📊 Worked Example
Use `buildTime = [5,1,4,2,6]`, `stability = [7,7,3,9,4]`, `k = 3`.

Prefix sums: `[0,5,6,10,12,18]`

Selected interval costs:

| Interval | Build Sum | Max-Min | Cost |
|---|---:|---:|---:|
| `[1,1]` | 5 | 0 | 5 |
| `[1,2]` | 6 | 0 | 6 |
| `[3,3]` | 4 | 0 | 4 |
| `[4,5]` | 8 | 5 | 13 |
| `[3,5]` | 12 | 6 | 18 |

DP trace:

1. `dp[1][1..5] = [5, 6, 14, 18, 24]`
2. For `p = 2`:
   - `dp[2][2] = dp[1][1] + cost[2,2] = 5 + 1 = 6`
   - `dp[2][3] = min(5 + cost[2,3], 6 + cost[3,3]) = min(10, 10) = 10`
   - `dp[2][5] = 19`
3. For `p = 3`:
   - `dp[3][5] = min(dp[2][2] + cost[3,5], dp[2][3] + cost[4,5], dp[2][4] + cost[5,5])`
   - `= min(6+18, 10+13, 17+6) = 23`

Answer: `23`.

## ⏱ Complexity Analysis
### Time Complexity
Precomputing all interval costs takes `O(n^2)`. The DP has `k * n` states, and each state scans up to `n` previous cut positions, so the dominant cost is `O(k * n^2)`. Total complexity is `O(n^2 + k * n^2) = O(k * n^2)`, which is practical for `n <= 300` but not remotely viable at million-scale inputs.

### Space Complexity
The interval cost table uses `O(n^2)` space, and the DP table also uses `O(k * n)` or `O(n)` with rolling arrays. The dominant owner is usually `cost[l][r]`. You can reduce DP memory, but removing interval storage trades space for repeated recomputation and worse runtime.

## 💡 Key Takeaways
- If the problem says “split an ordered array into exactly `k` contiguous groups,” think partition DP before considering greedy heuristics.
- If each group cost depends only on interval-local aggregates, precomputing interval cost is usually the unlock.
- Be precise about indexing: `dp[p][i]` over first `i` items pairs naturally with interval `cost[j+1][i]`, not `cost[j][i]`.
- Enforce non-empty batches by limiting transitions to `j >= p-1` and iterating `i >= p`; otherwise you silently admit invalid states.
- The production lesson is to separate expensive local scoring from global optimization: precompute reusable interval metrics once, then run a planner over those cached costs.

## 🚀 Variations & Further Practice
- Add a fixed per-batch setup penalty or a nonlinear penalty on batch length; the DP structure survives, but the optimal partition shape changes and may affect optimization opportunities.
- Replace `max-min` with a harder interval statistic such as median deviation or distinct-count penalty; now interval precomputation becomes the real bottleneck.
- Allow at most `k` batches instead of exactly `k`, or permit skipping stages with penalties; this introduces extra state and changes the transition boundary conditions.