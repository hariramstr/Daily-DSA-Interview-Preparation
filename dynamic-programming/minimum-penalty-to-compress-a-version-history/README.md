# Minimum Penalty to Compress a Version History

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, partition-dp, prefix-sums

---

## 🗂 Problem Overview
Given an array `sizes` of revision sizes, partition it into exactly `k` non-empty contiguous archive blocks so the total padding penalty is minimized. A block’s penalty is the gap between storing every version at that block’s maximum size and the block’s true summed size. Return the minimum total penalty, or `-1` if `k > n`. The challenge is that every split decision changes future costs, so greedy local choices fail.

## 🌍 Engineering Impact
This pattern shows up anywhere a sequential history must be coalesced into bounded segments under a lossy normalization rule: log compaction, SSTable tiering, video GOP packing, checkpoint batching, columnar page encoding, and time-window aggregation in streaming systems. The operational problem is always the same: segment boundaries determine both storage overhead and downstream read behavior. Without a principled partitioning strategy, teams ship heuristics that look fine on small traces but explode under skewed distributions. Partition DP turns an exponential search over all boundary placements into a predictable optimization pass, which is exactly what you want in offline planning, compaction scheduling, and build-time layout decisions.

## 🔍 Problem Statement
You are given an integer array `sizes` with `n = sizes.length`, where `1 <= n <= 400`, and an integer `k` with `1 <= k <= 400`. Partition the array into exactly `k` contiguous, non-empty blocks.

For any block covering indices `l..r`, its penalty is:

`penalty(l, r) = max(sizes[l..r]) * (r - l + 1) - sum(sizes[l..r])`

The goal is to minimize the sum of penalties across all `k` blocks. If `k > n`, no valid partition exists, so return `-1`.

Examples:

- `sizes = [5, 2, 4, 6, 3], k = 2` → `5`
- `sizes = [8, 8, 8, 8], k = 3` → `0`

The key constraint is small enough for `O(n^2)` interval precomputation and `O(k * n^2)` DP, but too large for brute-force enumeration of all partitions.

## 🪜 How to Solve This
1. Read the objective → we must split a sequence into exactly `k` contiguous groups. That is a strong signal for **partition DP**.

2. Inspect the block cost → `penalty(l, r)` depends only on the subarray `sizes[l..r]`, not on anything outside it. That means if we can compute every interval cost once, later DP transitions become cheap.

3. Ask what state matters → for a prefix ending at position `i`, and using exactly `b` blocks, we only care about the minimum penalty so far. That gives a natural state: `dp[b][i]`.

4. Define the transition → if the last block starts at `j`, then the previous `b-1` blocks must cover the prefix before `j`. So:
   `dp[b][i] = min(dp[b-1][j] + cost(j, i-1))`.

5. Precompute interval costs → scan every start index outward, maintaining running max and prefix sums. This avoids recomputing `max` and `sum` inside the DP.

6. Validate edge conditions early → `k > n` is impossible, and `dp[0][0] = 0` is the only valid zero-block base case.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums.**  
   Build `prefix[i+1] = sum(sizes[0..i])`. This lets you query any interval sum in `O(1)`. The invariant is: `sum(l, r) = prefix[r+1] - prefix[l]`.

2. **Precompute all interval penalties.**  
   For each start `l`, expand `r` from `l` to `n-1`, maintain `runningMax = max(sizes[l..r])`, and compute  
   `cost[l][r] = runningMax * (r - l + 1) - (prefix[r+1] - prefix[l])`.  
   This is correct because the block representation is fully determined by its maximum and length. The invariant is: after processing `(l, r)`, `runningMax` equals the maximum on that exact interval.

3. **Define the DP state.**  
   Let `dp[b][i]` be the minimum penalty to partition the first `i` elements (`sizes[0..i-1]`) into exactly `b` non-empty blocks. This is the standard **partition DP** abstraction over prefixes.

4. **Initialize base cases.**  
   Set all values to infinity except `dp[0][0] = 0`. No elements with zero blocks is valid; any other zero-block prefix is impossible.

5. **Fill transitions.**  
   For each block count `b` from `1..k`, and prefix length `i` from `b..n`, try every last-cut position `j` from `b-1..i-1`:  
   `dp[b][i] = min(dp[b][i], dp[b-1][j] + cost[j][i-1])`.  
   This is correct because every valid `b`-block partition has a unique final block `j..i-1`.

6. **Return the answer.**  
   If `k > n`, return `-1`; otherwise return `dp[k][n]`. The final invariant is that every feasible partition of the full array into `k` blocks has been considered exactly once through its last boundary.

## 📊 Worked Example
Take `sizes = [5, 2, 4, 6, 3]`, `k = 2`.

First compute a few interval costs:

| Interval | max | sum | penalty |
|---|---:|---:|---:|
| `[0..0]` = `[5]` | 5 | 5 | 0 |
| `[0..1]` = `[5,2]` | 5 | 7 | 3 |
| `[0..2]` = `[5,2,4]` | 5 | 11 | 4 |
| `[1..4]` = `[2,4,6,3]` | 6 | 15 | 9 |
| `[2..4]` = `[4,6,3]` | 6 | 13 | 5 |
| `[4..4]` = `[3]` | 3 | 3 | 0 |

Now DP:

- `dp[1][1..5] = [0, 3, 4, 7, 10]` using one block over each prefix.
- For `dp[2][5]`, try last block starts:
  - `j=1`: `dp[1][1] + cost[1][4] = 0 + 9 = 9`
  - `j=2`: `dp[1][2] + cost[2][4] = 3 + 5 = 8`
  - `j=3`: `dp[1][3] + cost[3][4] = 4 + 3 = 7`
  - `j=4`: `dp[1][4] + cost[4][4] = 7 + 0 = 7`

Minimum is `7`.

## ⏱ Complexity Analysis
### Time Complexity
Precomputing all interval costs takes `O(n^2)`. The DP has `k` layers, `n` prefix states per layer, and up to `n` transition points per state, so total time is `O(n^2 + k * n^2) = O(k * n^2)`. At `n = 400`, this is practical; at `10^6`, it is not remotely viable without stronger structure.

### Space Complexity
The interval cost table uses `O(n^2)` space, and the full DP table uses `O(k * n)`. You can reduce DP storage to `O(n)` with rolling arrays because each layer depends only on the previous one, but the `O(n^2)` cost matrix still dominates unless you recompute costs on demand.

## 💡 Key Takeaways
- If a problem asks for exactly `k` contiguous groups over an array prefix, think partition DP before trying greedy or sliding-window heuristics.
- If segment cost depends only on the interval itself, precomputing all interval costs is usually the move that makes the DP tractable.
- Use prefix length `i` to mean “first `i` elements,” then the last block is naturally `j..i-1`; this avoids most index confusion.
- Enforce non-empty blocks by iterating `i >= b` and `j >= b-1`; otherwise you silently allow impossible states.
- In production, this is the same design pattern as offline layout optimization: separate expensive local cost modeling from global boundary optimization.

## 🚀 Variations & Further Practice
- Add a fixed per-block archival overhead, so the optimizer trades off fewer blocks against padding cost; the twist is that `k` may become optional rather than fixed.
- Replace `max`-based padding with a different block representative such as median or rounded bucket size; the twist is that interval cost may no longer be incrementally maintainable with a single running statistic.
- Allow only blocks of length within `[L, R]`; the twist is that feasibility constraints prune transitions and can change which DP optimizations are available.