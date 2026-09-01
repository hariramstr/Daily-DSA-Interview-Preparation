# Maximum Bonus from Merging Sprint Reports

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Prefix Sum, Array

---

## 🗂 Problem Overview
Given an integer array `reports`, partition it into contiguous, non-empty blocks covering the full array. Each block contributes `sum(block) * length(block)`, and the goal is to maximize the total contribution across all blocks. The output is that maximum total bonus. The problem is non-trivial because every cut changes both the local block score and the remaining optimization space, so greedy choices fail and the best answer for a prefix depends on all earlier partition points.

## 🌍 Engineering Impact
This pattern shows up anywhere raw event streams are compressed into contiguous aggregates with a nonlinear scoring function: sprint rollups, streaming pipeline micro-batching, ad-delivery sessionization, log compaction windows, and time-series summarization for anomaly review. At scale, naive heuristics over-segment or over-merge, producing unstable downstream metrics and poor operator decisions. Dynamic programming matters when local merge quality is not additive across boundaries. It enables globally optimal segmentation under strict ordering constraints, which is exactly what many production systems need when reordering is illegal and every event must be assigned once.

## 🔍 Problem Statement
You are given an array `reports` of length `n` where `1 <= n <= 2000` and `-10^4 <= reports[i] <= 10^4`. Partition the array into one or more contiguous, non-empty subarrays. For any chosen block `reports[l..r]`, its bonus is:

`sum(reports[l..r]) * (r - l + 1)`

Return the maximum possible total bonus over all valid partitions. Every index must belong to exactly one block, and blocks must preserve original order.

Negative values are allowed, so splitting into many small blocks is not always optimal; sometimes merging a negative region with adjacent positives increases the total.

Examples:

- `reports = [3, -1, 2]` → `12`
- `reports = [4, -5, 6, 1]` → `24`

The key constraint is `n <= 2000`: large enough that exponential partition search is impossible, but small enough for an `O(n^2)` dynamic programming solution.

## 🪜 How to Solve This
1. Read the scoring rule → each partition is made of contiguous blocks, and the score of a block depends only on its sum and length.
2. Notice the structure → if the last block ends at position `i`, then everything before that is just “best answer for a smaller prefix.”
3. That immediately suggests prefix DP: define the best total bonus for the first `i` elements.
4. To compute `dp[i]`, try every possible start `j` of the final block ending at `i - 1`.
5. For each `j`, combine:
   - the best score for the prefix before `j`, and
   - the score of block `reports[j..i-1]`.
6. Block sums must be fast, otherwise each candidate block becomes expensive. Use a prefix-sum array so any subarray sum is `O(1)`.
7. This turns the problem into checking all `O(n^2)` partition boundaries, which is acceptable for `n = 2000`.
8. The reason this works is optimal substructure: once the last block is fixed, the earlier prefix can be solved independently and optimally.

## 🧩 Algorithm Walkthrough
1. **Build prefix sums**  
   Let `prefix[i]` be the sum of the first `i` elements, with `prefix[0] = 0`. Then the sum of `reports[j..i-1]` is `prefix[i] - prefix[j]`.  
   **Why:** subarray sums become constant-time lookups.  
   **Invariant:** `prefix[i]` always equals `reports[0] + ... + reports[i-1]`.

2. **Define the DP state**  
   Let `dp[i]` be the maximum bonus obtainable from the first `i` elements. The answer is `dp[n]`.  
   **Why:** the problem asks for an optimal partition of the full prefix ending at each position.  
   **Invariant:** after processing `i`, `dp[i]` is globally optimal for that prefix.

3. **Enumerate the last block**  
   For each `i` from `1` to `n`, try every `j` in `[0, i-1]` as the start of the last block. That block is `reports[j..i-1]`.  
   **Why:** every valid partition of the first `i` elements has exactly one final block.  
   **Invariant:** all candidate final cuts for prefix `i` are considered exactly once.

4. **Compute transition**  
   `blockSum = prefix[i] - prefix[j]`  
   `blockLen = i - j`  
   `dp[i] = max(dp[i], dp[j] + blockSum * blockLen)`  
   **Why:** `dp[j]` is the best score before the last block, and the last block contributes independently.  
   **Pattern:** this is classic **Dynamic Programming over prefixes**, accelerated by **Prefix Sum**.

5. **Handle negatives safely**  
   Initialize `dp` with very small values except `dp[0] = 0`.  
   **Why:** block scores may be negative, so defaulting to zero would incorrectly prefer impossible states.  
   **Invariant:** every `dp[i]` comes from a valid partition, even when all totals are negative.

## 📊 Worked Example
Take `reports = [4, -5, 6, 1]`.

Prefix sums:

| i | prefix[i] |
|---|-----------|
| 0 | 0 |
| 1 | 4 |
| 2 | -1 |
| 3 | 5 |
| 4 | 6 |

Now fill `dp`:

| i | Best choice for last block | Computation | dp[i] |
|---|----------------------------|-------------|-------|
| 0 | — | base case | 0 |
| 1 | `[4]` | `0 + 4*1` | 4 |
| 2 | `[4,-5]` | `0 + (-1)*2 = -2`; `[ -5 ]` gives `4-5=-1` | -1 |
| 3 | `[4,-5,6]` | `0 + 5*3 = 15` | 15 |
| 4 | `[4,-5,6,1]` | `0 + 6*4 = 24` | 24 |

So the optimal answer is `24`, achieved by taking the whole array as one block.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)`. For each endpoint `i`, the algorithm tries every possible start `j` of the last block, and each transition is `O(1)` using prefix sums. At `n = 2000`, this is about two million transitions. At `10^6` or `10^9`, quadratic work is completely infeasible.

### Space Complexity
`O(n)`. The space is owned by the prefix-sum array and the DP array. It can be reduced slightly if prefix sums are computed differently, but you still need linear storage for DP unless you change the problem structure or accept recomputation.

## 💡 Key Takeaways
- If a problem asks for partitioning an array into contiguous blocks and optimizing a total score, prefix-based dynamic programming should be your first candidate.
- When each candidate block score depends on a subarray sum, pair DP with prefix sums to avoid hidden `O(n^3)` behavior.
- Use `dp[0] = 0` and initialize all other states to negative infinity; negative-valued inputs make zero-initialization incorrect.
- Be explicit about indexing: `dp[i]` usually means “first `i` elements,” so the last block is `j..i-1`, not `j..i`.
- In production systems, globally optimal segmentation often beats local heuristics when boundaries affect downstream cost models nonlinearly.

## 🚀 Variations & Further Practice
- Add a constraint of exactly `k` sprint summaries. Twist: DP state becomes two-dimensional, `dp[i][k]`, and you must optimize both prefix length and partition count.
- Change the block score to include a fixed per-block penalty or reward. Twist: segmentation now trades off block quality against boundary cost, which mirrors real batching and compaction systems.
- Allow online updates to `reports` and repeated maximum-bonus queries. Twist: static prefix-DP is no longer enough; you need data structures or offline batching strategies to manage recomputation.