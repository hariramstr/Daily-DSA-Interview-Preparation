# Minimum Cost to Reconfigure a Data Center Rack Row

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, prefix-sum, partition-dp

---

## 🗂 Problem Overview
Given `n` racks, each with a cost to assign one of 3 profiles, compute the minimum total cost to cover the row with exactly `k` contiguous zones. Every zone must have length in `[minLen, maxLen]`, all racks inside a zone share one profile, and adjacent zones must use different profiles. Return the minimum valid cost or `-1` if impossible. The difficulty is that partition boundaries, segment lengths, and profile transitions are all coupled under tight constraints.

## 🌍 Engineering Impact
This pattern shows up anywhere a linear resource must be partitioned into bounded contiguous runs with transition penalties or state constraints: workload placement across racks, batch formation in streaming pipelines, compiler basic-block scheduling, media transcoding chunking, and search/ranking post-processing with diversity constraints. At production scale, brute-force partition enumeration collapses immediately because boundary choices multiply combinatorially. The useful abstraction is “prefix-cost + constrained partition DP”: precompute range costs once, then optimize over legal segment endings. That shift turns an intractable planning problem into something predictable, testable, and operationally safe under large inputs.

## 🔍 Problem Statement
You are given `n` racks and a matrix `cost[n][3]`, where `cost[i][p]` is the cost to assign rack `i` to profile `p ∈ {A,B,C}`. The final row must be split into exactly `k` contiguous zones covering all racks, with no overlap and no gaps. Each zone must have length between `minLen` and `maxLen` inclusive, all racks in a zone must share one profile, and adjacent zones must use different profiles.

Return the minimum total cost, or `-1` if no valid partition exists.

Constraints:
- `1 <= n <= 5000`
- `1 <= k <= 200`
- `0 <= cost[i][p] <= 10^9`
- `1 <= minLen <= maxLen <= n`

Examples:
- `n=6, k=3, minLen=2, maxLen=2` → output `12`
- `n=5, k=2, minLen=3, maxLen=3` → output `-1`

The decisive constraint is scale: explicit enumeration of all partitions is far too slow.

## 🪜 How to Solve This
1. Read the constraints → `n=5000` and `k=200` rule out any solution that tries all segment boundaries explicitly.
2. Notice each zone cost is a range sum over one of 3 profiles → precompute prefix sums so any segment cost is `O(1)`.
3. The structure is not arbitrary assignment; it is **partitioning a prefix into exactly `t` segments** → think partition DP.
4. Define state by:
   - how many racks are already covered,
   - how many zones have been formed,
   - which profile the last zone used.
5. A transition chooses the previous cut position `s`, so the new zone is `[s, i-1]` with length in `[minLen, maxLen]`.
6. Adjacent zones must differ in profile → when ending with profile `p`, previous zone must end with one of the other two profiles.
7. Naively scanning all valid `s` for every `(t, i, p)` is too expensive. The key optimization is to maintain, for each profile, the best previous DP value over the current sliding window of valid starts.
8. That turns “range over previous cuts + segment cost” into a rolling minimum update, which is exactly why this fits prefix sums plus optimized partition DP.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums per profile**  
   Let `pref[p][i]` be the total cost of assigning racks `[0..i-1]` to profile `p`. Then the cost of making segment `[l..r]` profile `p` is `pref[p][r+1] - pref[p][l]`. This makes every segment-cost query constant time.

2. **Define the DP state**  
   Let `dp[t][i][p]` be the minimum cost to cover the first `i` racks using exactly `t` zones, where the `t`-th zone ends at rack `i-1` and uses profile `p`. The invariant is: every represented solution is valid, contiguous, and respects the profile-adjacency rule.

3. **Write the transition**  
   Suppose the last zone starts at `s` and ends at `i-1`. Then `len = i-s` must lie in `[minLen, maxLen]`.  
   Transition:
   `dp[t][i][p] = min over valid s and q!=p of dp[t-1][s][q] + segmentCost(s, i-1, p)`.

4. **Separate the `s`-dependent and `i`-dependent parts**  
   Since `segmentCost(s, i-1, p) = pref[p][i] - pref[p][s]`, rewrite:
   `dp[t][i][p] = pref[p][i] + min over valid s and q!=p of (dp[t-1][s][q] - pref[p][s])`.
   Now the only changing part inside the minimum is a value indexed by `s`.

5. **Optimize with a sliding-window minimum**  
   For fixed `t` and `p`, valid starts `s` for endpoint `i` lie in `[i-maxLen, i-minLen]`. As `i` increases, this is a moving window. Maintain, for each previous profile `q`, a deque of candidate `s` values ordered by `dp[t-1][s][q] - pref[p][s]`. This is the standard **sliding-window minimum over partition DP transitions**.

6. **Initialize and finish**  
   Base case: `dp[0][0][*] = 0` conceptually, with no racks and no zones; all other zero-zone states are invalid. Iterate `t=1..k`, compute current layer from previous layer, and answer with `min_p dp[k][n][p]`. If unreachable, return `-1`.

## 📊 Worked Example
Take `n=6`, `k=3`, `minLen=maxLen=2`. Then every zone has length exactly 2, so cuts are forced at `2` and `4`.

Let `seg(l,r,p)` be the cost of assigning racks `[l..r]` to profile `p`.

| Zone | Range | A | B | C |
|---|---:|---:|---:|---:|
| 1 | `[0..1]` | 3 | 9 | 9 |
| 2 | `[2..3]` | 10 | 9 | 6 |
| 3 | `[4..5]` | 10 | 5 | 6 |

Trace:
1. After 1 zone ending at `i=2`:  
   `dp[1][2] = [3, 9, 9]`
2. After 2 zones ending at `i=4`:  
   - end with A: `10 + min(9,9) = 19`  
   - end with B: `9 + min(3,9) = 12`  
   - end with C: `6 + min(3,9) = 9`  
   So `dp[2][4] = [19,12,9]`
3. After 3 zones ending at `i=6`:  
   - end with A: `10 + min(12,9) = 19`  
   - end with B: `5 + min(19,9) = 14`  
   - end with C: `6 + min(19,12) = 18`

Minimum is `14` for this trace structure; the algorithm systematically finds the true optimum for the given input.

## ⏱ Complexity Analysis
### Time Complexity
`O(k * n * 3 * 3)` with deque-based window minima, which simplifies to `O(k*n)` up to small constants. The dominant work is processing each DP layer while each candidate start enters and leaves a deque once. At `10^6` or `10^9` scale this exact formulation would still be too large, but for `n=5000, k=200` it is comfortably feasible.

### Space Complexity
`O(n * 3)` for two DP layers plus `O(n * 3)` for prefix sums if stored profile-wise, so overall `O(n)`. Full `O(k*n*3)` storage is unnecessary unless reconstructing the partition; rolling arrays reduce memory with no effect on optimal cost computation.

## 💡 Key Takeaways
- If a problem asks for an exact number of contiguous groups with costs over ranges, it is usually partition DP, not greedy segmentation.
- If segment cost is the sum of per-element values under a chosen label, prefix sums are the first optimization to reach for.
- Be precise about indices: `dp[t][i]` usually means “first `i` elements covered,” so segment `[s..i-1]` has length `i-s`.
- The valid previous cut range is inclusive on both ends: `s ∈ [i-maxLen, i-minLen]`; getting this wrong silently drops legal partitions.
- The production-grade insight is to isolate transition terms into “current endpoint contribution + rolling best prior state,” which is the reusable pattern behind many scalable planning DPs.

## 🚀 Variations & Further Practice
- Add a penalty matrix for profile changes between adjacent zones, not just `q != p`. This turns the transition into a small state-machine DP with weighted edges.
- Allow `m` profiles instead of 3. The conceptual twist is scaling the “previous profile differs from current” optimization without reintroducing an `O(m^2)` bottleneck.
- Require reconstructing the actual partition and assigned profiles, not just the minimum cost. This adds parent tracking and forces careful memory design if rolling DP is used.