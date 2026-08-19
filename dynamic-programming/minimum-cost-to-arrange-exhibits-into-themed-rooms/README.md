# Minimum Cost to Arrange Exhibits into Themed Rooms

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, partition-dp, array

---

## 🗂 Problem Overview
Given an array `themes` of length `n`, split it into exactly `k` non-empty contiguous rooms while preserving order. The cost of a room is the number of equal-theme exhibit pairs inside that segment, and the goal is to minimize the total cost across all rooms. The challenge is that segment costs are not additive per element; each split decision changes future duplicate-pair counts, which makes greedy strategies fail and pushes the problem into partition dynamic programming.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered data must be segmented under a non-linear intra-segment cost: log batching in streaming pipelines, query-plan chunking in compilers, shard-local dedup windows, sessionization in analytics, and document block segmentation in search indexing. At scale, naive recomputation of segment cost for every candidate split collapses under quadratic or cubic behavior. Partition DP with efficient segment-cost handling gives predictable latency and makes exact optimization feasible under bounded partition counts. The broader lesson is architectural: once cost depends on interactions within a contiguous block, the system needs explicit boundary optimization, not local heuristics.

## 🔍 Problem Statement
You are given an integer array `themes` where `themes[i]` is the theme label of the `i`-th exhibit in a fixed left-to-right hallway. Partition the array into exactly `k` contiguous, non-empty rooms. The cost of one room is the number of unordered pairs of exhibits in that room with equal theme labels. The total arrangement cost is the sum of room costs. Return the minimum possible total cost.

Constraints:

- `1 <= n <= 1000`
- `1 <= k <= min(n, 50)`
- `1 <= themes[i] <= 10^5`

Examples:

- `themes = [1, 2, 1, 2, 1], k = 2` → `1`
- `themes = [4, 4, 4, 5, 5], k = 2` → `4`

Edge cases matter: `k = n` forces every exhibit into its own room with cost `0`; `k = 1` means the entire array is one room; repeated values concentrated in one region can make split placement highly non-obvious. The key algorithmic constraint is that `n` is moderate but large enough that trying all partitions with on-the-fly segment recomputation is too expensive.

## 🪜 How to Solve This
1. Start from the output shape: we need **exactly `k` contiguous partitions**. That is the classic signal for **partition DP**.

2. Define the subproblem: let `dp[p][i]` be the minimum cost to split the first `i` exhibits into exactly `p` rooms. Then the last room must be some segment `[j..i]`, so  
   `dp[p][i] = min(dp[p-1][j-1] + cost(j, i))`.

3. That immediately shifts attention to `cost(j, i)`: if we recompute duplicate pairs inside every segment from scratch, the DP becomes too slow.

4. Observe the room cost depends only on frequency counts inside a contiguous subarray. For a fixed start `j`, extending the segment rightward by one exhibit increases cost by the number of previous occurrences of that theme already in the segment.

5. So precompute `cost(j, i)` for all `1 <= j <= i <= n` in `O(n^2)` using a frequency map per starting position.

6. Once segment costs are available in `O(1)`, the DP transition is straightforward: try every valid previous cut. With `n <= 1000` and `k <= 50`, `O(k * n^2)` is comfortably feasible.

## 🧩 Algorithm Walkthrough
1. **Precompute segment costs** using a frequency table per left boundary.  
   For each `l` from `1` to `n`, walk `r` from `l` to `n`, maintain `freq[themes[r]]`, and update  
   `cost[l][r] = cost[l][r-1] + freq[themes[r]]` before incrementing the frequency.  
   **Why correct:** when adding a value already seen `f` times, it forms exactly `f` new equal pairs.  
   **Invariant:** after processing `r`, `cost[l][r]` equals the duplicate-pair count in segment `[l..r]`.

2. **Define the DP state** for the partition problem.  
   Let `dp[p][i]` be the minimum cost to partition the first `i` exhibits into exactly `p` rooms.  
   **Why correct:** every valid solution over a prefix ends with one last contiguous room.

3. **Initialize base cases.**  
   Set `dp[0][0] = 0` and all other states to infinity.  
   **Invariant:** impossible states remain unreachable, preventing invalid transitions such as empty rooms.

4. **Apply the partition transition.**  
   For each room count `p` from `1` to `k`, and each prefix length `i` from `p` to `n`, try every last-room start `j` from `p` to `i`:  
   `dp[p][i] = min(dp[p][i], dp[p-1][j-1] + cost[j][i])`.  
   **Why correct:** this enumerates every legal final cut exactly once.

5. **Return `dp[k][n]`.**  
   This is the minimum total cost for the full array using exactly `k` non-empty contiguous rooms.

This is a textbook **Partition Dynamic Programming** pattern: optimize over all possible final boundaries while using precomputed interval costs to make transitions cheap.

## 📊 Worked Example
Take `themes = [1, 2, 1, 2, 1]`, `k = 2`.

First compute key segment costs:

| Segment | Cost |
|---|---:|
| `[1..1] = [1]` | 0 |
| `[1..2] = [1,2]` | 0 |
| `[1..3] = [1,2,1]` | 1 |
| `[1..4] = [1,2,1,2]` | 2 |
| `[1..5] = [1,2,1,2,1]` | 4 |
| `[2..5] = [2,1,2,1]` | 2 |
| `[3..5] = [1,2,1]` | 1 |
| `[4..5] = [2,1]` | 0 |

Now DP:

- `dp[1][i] = cost[1][i]` → `[0, 0, 1, 2, 4]`
- For `dp[2][5]`, try last room starts:
  - `j=2`: `dp[1][1] + cost[2][5] = 0 + 2 = 2`
  - `j=3`: `dp[1][2] + cost[3][5] = 0 + 1 = 1`
  - `j=4`: `dp[1][3] + cost[4][5] = 1 + 0 = 1`
  - `j=5`: `dp[1][4] + cost[5][5] = 2 + 0 = 2`

Minimum is `1`, achieved by splitting after index `2` or `3`.

## ⏱ Complexity Analysis
### Time Complexity
Precomputing all segment costs takes `O(n^2)`. The DP has `k` layers, each evaluating `O(n^2)` transitions, so total time is `O(n^2 + k*n^2) = O(k*n^2)`. For `n = 1000`, this is practical. At `10^6` or `10^9` scale, this exact approach is no longer viable without stronger structure or approximation.

### Space Complexity
The cost table uses `O(n^2)` space, and the DP table uses `O(k*n)`, so total space is `O(n^2 + k*n)`. DP can be reduced to two rows, but the interval-cost table still dominates unless costs are computed online with a more complex optimization strategy.

## 💡 Key Takeaways
- If the problem asks for **exactly `k` contiguous groups** and the objective is a sum over segment costs, think partition DP immediately.
- If a segment’s cost depends on **interactions among elements inside the segment** rather than simple sums, precomputing interval cost is often the first unlock.
- Be careful with indexing: `dp[p-1][j-1] + cost[j][i]` assumes 1-based segment boundaries and non-empty rooms.
- Initialize impossible states to infinity; otherwise, invalid transitions can silently allow fewer than `k` rooms or empty partitions.
- The transferable design insight is to separate **boundary optimization** from **segment scoring**: precompute or incrementally maintain local block cost, then optimize global cuts.

## 🚀 Variations & Further Practice
- Add a weighted penalty per room or per split. The DP shape stays similar, but the trade-off between fragmentation and duplicate suppression becomes explicit.
- Replace duplicate-pair cost with a harder segment metric such as distinct-count penalty, inversion count, or mode frequency. The conceptual twist is whether interval cost remains precomputable in `O(1)` lookup after preprocessing.
- Scale `n` far beyond `1000` and ask for near-linear or `O(k*n log n)` behavior. This pushes the problem toward divide-and-conquer DP optimization or Mo-style cost maintenance, if the cost function supports it.