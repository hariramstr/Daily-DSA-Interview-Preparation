# Minimum Risk to Merge Security Zones

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, prefix-sum

---

## 🗂 Problem Overview
Given an array `risk`, repeatedly merge adjacent groups until one group remains. Each merge costs the sum of risks in the newly formed group, but a merge is legal only if at least one side being merged has total risk `<= T`. Return the minimum total merge cost, or `-1` if no valid full merge sequence exists. The challenge is that local valid merges can still lead to globally impossible or suboptimal outcomes.

## 🌍 Engineering Impact
This pattern shows up in systems that perform constrained hierarchical aggregation: compaction in LSM trees, staged rollout of firewall or IAM policies, compiler IR region folding, and streaming pipeline fusion under safety budgets. The hard part is not combining adjacent units; it is combining them in an order that preserves future feasibility while minimizing cumulative cost. Greedy choices routinely fail because an early merge can create a large intermediate artifact that blocks later consolidation. Interval DP gives a disciplined way to reason about all legal decomposition boundaries without exploding into merge-sequence enumeration.

## 🔍 Problem Statement
You are given `n` security zones in a fixed left-to-right order, where `1 <= n <= 300` and `risk[i]` is the risk value of zone `i` with `1 <= risk[i] <= 10^6`. Initially, each zone is its own group. At each step, you may merge exactly two adjacent groups. If the merged interval has total risk `S`, that merge costs `S`.

A merge between adjacent groups is valid only if at least one of the two groups has total risk `<= T`, where `1 <= T <= 10^12`.

Return the minimum total cost to merge all zones into one group, or `-1` if no valid sequence exists.

Examples:

- `risk = [4,2,7,3], T = 6` → `32`
- `risk = [8,9,5], T = 6` → `-1`

The key constraint is `n <= 300`: too large for brute-force merge-order search, but small enough for cubic interval DP.

## 🪜 How to Solve This
1. Read the problem → this is not arbitrary graph merging; merges are only between **adjacent** groups. That strongly suggests reasoning over subarrays.
2. Notice the cost of the final merge of an interval depends only on the interval sum, not on the internal merge history. That is classic interval-DP structure.
3. Define the subproblem: minimum cost to fully merge `risk[l..r]` into one group. If that interval can be formed at all, its final group sum is fixed: `sum(l, r)`.
4. To build `dp[l][r]`, try every split `k`, meaning left interval `[l..k]` and right interval `[k+1..r]` are each fully merged first.
5. The last merge between those two groups is legal iff `sum(l, k) <= T` or `sum(k+1, r) <= T`.
6. If both subintervals are feasible and the threshold rule holds, total cost is  
   `dp[l][k] + dp[k+1][r] + sum(l, r)`.
7. Take the minimum over all valid `k`. If none work, the interval is impossible.
8. Prefix sums make interval sums O(1), turning the whole approach into a practical O(n^3) DP.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums**  
   Build `prefix[i+1] = prefix[i] + risk[i]`. Then `sum(l, r) = prefix[r+1] - prefix[l]`.  
   Why: every DP transition needs left, right, and whole-interval sums. Without O(1) range sums, the runtime degrades by another factor of `n`.

2. **Define the interval DP state**  
   Let `dp[l][r]` be the minimum cost to merge subarray `risk[l..r]` into one group. Initialize all states to infinity, and set `dp[l][l] = 0`.  
   Invariant: if `dp[l][r]` is finite, then interval `[l..r]` can be legally consolidated into one group.

3. **Process intervals by increasing length**  
   For `len = 2..n`, compute all intervals `[l..r]` of that length.  
   Why: `dp[l][r]` depends only on strictly smaller intervals, so bottom-up evaluation respects dependencies.

4. **Enumerate the final split point**  
   For each `k` in `[l, r-1]`, treat `[l..k]` and `[k+1..r]` as the two groups merged last. Skip if either subinterval is impossible.  
   This is the core **Interval DP** pattern: choose the last operation, not the first.

5. **Apply the threshold validity rule**  
   Compute `leftSum = sum(l, k)` and `rightSum = sum(k+1, r)`. The final merge is legal iff `leftSum <= T || rightSum <= T`.  
   Correctness: every complete merge tree for `[l..r]` has some last split `k`; this condition exactly matches the problem’s legality rule for that last merge.

6. **Relax the DP value**  
   Candidate cost is `dp[l][k] + dp[k+1][r] + sum(l, r)`. Minimize over all valid `k`.  
   Invariant maintained: after all splits are considered, `dp[l][r]` is the minimum cost among all legal ways to fully merge `[l..r]`.

7. **Return the answer**  
   If `dp[0][n-1]` is still infinity, return `-1`; otherwise return that value.

## 📊 Worked Example
Take `risk = [4,2,7,3]`, `T = 6`.

| Interval | Valid split(s) | Cost |
|---|---|---:|
| `[0,0]`,`[1,1]`,`[2,2]`,`[3,3]` | base case | 0 |
| `[0,1] = [4,2]` | `4 <= 6` or `2 <= 6` | `6` |
| `[1,2] = [2,7]` | `2 <= 6` | `9` |
| `[2,3] = [7,3]` | `3 <= 6` | `10` |
| `[0,2] = [4,2,7]` | split at `0`: `0+9+13=22`; split at `1`: `6+0+13=19` | `19` |
| `[1,3] = [2,7,3]` | split at `1`: `0+10+12=22`; split at `2`: `9+0+12=21` | `21` |
| `[0,3] = [4,2,7,3]` | split at `1` valid because left sum `6 <= 6` | `6+10+16=32` |

Result: `dp[0][3] = 32`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n^3)`. There are `O(n^2)` intervals, and each interval tries `O(n)` split points. Prefix sums make each transition O(1). At `n = 300`, this is entirely practical. At `10^6` or `10^9` elements, cubic interval DP is non-starter territory and requires a different problem structure, not micro-optimizations.

### Space Complexity
`O(n^2)` for the DP table, plus `O(n)` for prefix sums. The DP matrix dominates memory. Space reduction is limited because transitions need many previously computed intervals across lengths; compressing aggressively usually destroys the constant-time access pattern that makes the solution viable.

## 💡 Key Takeaways
- Adjacent merges + cost over a whole subarray + “choose where the last merge happened” is a strong signal for interval DP.
- If the legality of combining two solved subproblems depends only on aggregate interval properties, prefix sums usually belong in the design.
- Be careful not to test the threshold rule against the whole interval sum; the rule applies to the two groups being merged at the final split.
- Use a true infinity sentinel and skip impossible subintervals explicitly, or integer overflow / bogus minima will corrupt the DP.
- In production planning problems, feasibility constraints on intermediate states often matter more than final-state cost; model those constraints directly in the state transition, not as an afterthought.

## 🚀 Variations & Further Practice
- Require **both** sides of every merge to have sum `<= T`. Same interval-DP skeleton, but feasibility collapses much faster and many intervals become impossible.
- Allow merging exactly `K` adjacent groups at once, with cost equal to the merged interval sum. This shifts the problem toward higher-dimensional interval DP similar to generalized stone merging.
- Make `T` depend on merge depth or interval length. The conceptual twist is that legality is no longer a pure function of subarray sums, so the DP state must encode additional structural context.