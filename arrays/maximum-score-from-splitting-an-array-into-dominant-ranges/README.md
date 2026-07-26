# Maximum Score from Splitting an Array into Dominant Ranges

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Frequency Counting

---

## 🗂 Problem Overview
Given an array `nums` and an integer `k`, split the array into exactly `k` non-empty contiguous subarrays to maximize total score. A subarray’s score is `(maximum frequency of any value inside it) × (subarray length)`. Return the best possible sum across all `k` parts. The difficulty is that segment quality is non-local: extending a range changes both its dominant frequency and its length, so greedy cut placement fails.

## 🌍 Engineering Impact
This pattern shows up anywhere a stream or sequence must be partitioned into contiguous windows while optimizing a segment-local objective: log compaction, streaming analytics, query/session segmentation, compiler token grouping, and ranking pipelines that batch adjacent items under a dominant label. At scale, brute-force partition enumeration collapses immediately because the search space is combinatorial. The useful architectural move is to separate expensive segment scoring from global partition optimization: precompute reusable range costs, then run dynamic programming over cut positions. That decomposition is broadly applicable in schedulers, storage layout tuning, and adaptive batching systems.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n` and an integer `k`, where `1 <= n <= 350` and `1 <= k <= min(n, 50)`. Partition the entire array into exactly `k` non-empty contiguous subarrays. For any subarray `nums[l..r]`, let `f` be the highest frequency of any value in that range. Its contribution is:

`score(l, r) = f * (r - l + 1)`

The total score is the sum across all chosen subarrays. Return the maximum achievable total.

Examples:

- `nums = [1,2,2,1,2], k = 2` → `13`
- `nums = [4,4,3,3,3,2,2], k = 3` → `23`

Edge conditions matter: every part must be non-empty, all elements must be used exactly once, and ties in dominant value do not matter because only the maximum frequency value is used. The key constraint is `n <= 350`: small enough for `O(n^3)`-class DP, too large for enumerating all partitions.

## 🪜 How to Solve This
1. Read the objective carefully → each segment score depends only on that segment, but the partition must cover the array with exactly `k` cuts. That strongly suggests **partition DP**.

2. Ask what makes the problem hard → the score of `nums[j..i-1]` is not a simple sum or prefix-difference function. It depends on the most frequent value inside that exact range.

3. Separate concerns:
   - First, compute the score for every possible subarray.
   - Then, choose the best way to chain exactly `k` such subarrays together.

4. For subarray scoring, expand each left boundary `l` to the right:
   - maintain a frequency map,
   - maintain the current maximum frequency,
   - compute `cost[l][r] = maxFreq * (r - l + 1)`.

5. Once all range costs are known, define DP:
   - `dp[p][i]` = best score for splitting the first `i` elements into exactly `p` parts.

6. Transition by placing the last cut at `j`:
   - previous `p-1` parts cover `nums[0..j-1]`,
   - last part is `nums[j..i-1]`.

7. Try all valid `j`, take the maximum. With `n=350`, this is comfortably feasible.

## 🧩 Algorithm Walkthrough
1. **Precompute range scores using incremental frequency counting.**  
   For each start index `l`, walk `r` from `l` to `n-1`. Maintain a hash map `freq[value]` and a scalar `maxFreq`. After inserting `nums[r]`, set `cost[l][r] = maxFreq * (r - l + 1)`.  
   **Why correct:** the map always reflects counts in `nums[l..r]`, and `maxFreq` is the dominant frequency for that exact range.  
   **Invariant:** after processing `r`, `cost[l][r]` is exact.

2. **Define the DP state.**  
   Let `dp[p][i]` be the maximum score for partitioning the prefix of length `i` (`nums[0..i-1]`) into exactly `p` non-empty contiguous parts.  
   **Why this state:** it captures both the coverage requirement and the exact number of segments.

3. **Initialize base cases.**  
   Set `dp[0][0] = 0` and all other states to negative infinity.  
   **Invariant:** impossible states remain impossible; only valid partitions contribute.

4. **Transition by choosing the last segment.**  
   For each `p` from `1` to `k`, and each `i` from `p` to `n`, try every `j` from `p-1` to `i-1`:  
   `dp[p][i] = max(dp[p][i], dp[p-1][j] + cost[j][i-1])`  
   **Why correct:** every valid `p`-partition of the first `i` elements ends with some last segment `nums[j..i-1]`.

5. **Return `dp[k][n]`.**  
   This is the best score using all elements in exactly `k` contiguous parts.

This is a classic **Dynamic Programming on Partitions** pattern, with **frequency-count-based range precomputation** to make each transition constant-time apart from the cut scan.

## 📊 Worked Example
Example: `nums = [1,2,2,1,2]`, `k = 2`

First compute a few range scores:

| Range | Freqs | maxFreq | len | score |
|---|---|---:|---:|---:|
| `[0..0] = [1]` | `{1:1}` | 1 | 1 | 1 |
| `[0..2] = [1,2,2]` | `{1:1,2:2}` | 2 | 3 | 6 |
| `[1..4] = [2,2,1,2]` | `{2:3,1:1}` | 3 | 4 | 12 |
| `[3..4] = [1,2]` | `{1:1,2:1}` | 1 | 2 | 2 |

Now DP:

- `dp[1][1] = cost[0][0] = 1`
- `dp[1][5] = cost[0][4] = 9` because whole array has `2` occurring `3` times, length `5`

For `p = 2`, `i = 5`, try last cut `j`:

- `j=1`: `dp[1][1] + cost[1][4] = 1 + 12 = 13`
- `j=2`: `cost[0][1] + cost[2][4] = 2 + 4 = 6`
- `j=3`: `6 + 2 = 8`
- `j=4`: `8 + 1 = 9`

Best is `13`, from split `[1] | [2,2,1,2]`.

## ⏱ Complexity Analysis
### Time Complexity
Precomputing all `cost[l][r]` values takes `O(n^2)` expected time with hash-map frequency updates. The DP has `k` layers, `n` end positions, and up to `n` cut positions per state, so `O(k * n^2)`. Overall: `O(n^2 + k*n^2) = O(k*n^2)`. At `n=350`, this is trivial; at `10^6`, it is completely infeasible without stronger structure.

### Space Complexity
`O(n^2 + k*n)` if storing the full `cost` table and DP table. The `cost` matrix dominates. DP can be reduced to `O(n)` per layer with rolling arrays, but `cost` remains `O(n^2)` unless recomputed on demand, which would increase runtime.

## 💡 Key Takeaways
- If a problem asks for splitting an array into exactly `k` contiguous parts with a segment-local objective, default to partition DP before considering greedy strategies.
- If segment score is expensive but depends only on subarray contents, precompute all range costs once and keep the DP transition simple.
- Be precise about indexing: `dp[p][i]` usually means first `i` elements, so the last segment is `nums[j..i-1]`, not `nums[j..i]`.
- Enforce non-empty parts by limiting transitions to `j >= p-1` and `i >= p`; otherwise invalid states silently leak into the answer.
- The transferable design insight is to decompose optimization into reusable local scoring plus global composition, which is the same move used in schedulers, batching engines, and layout optimizers.

## 🚀 Variations & Further Practice
- Replace `max frequency * length` with a more complex segment statistic, such as `sum of top-2 frequencies * length`; the harder part is maintaining richer range state efficiently.
- Allow up to `k` parts instead of exactly `k`; this changes the DP objective and introduces a decision about whether creating another segment is beneficial.
- Scale `n` to `10^5+` and ask for near-linear or `O(n log n)` performance; the conceptual twist is that full range precomputation is no longer viable, so you need additional structure or approximation.