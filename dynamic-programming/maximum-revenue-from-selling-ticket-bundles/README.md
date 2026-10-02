# Maximum Revenue from Selling Ticket Bundles

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, array

---

## 🗂 Problem Overview
You are given `N` consecutive seat sections and an `N x N` matrix where `bundleRevenue[i][j]` is the revenue from selling one promotional bundle covering the contiguous range `[i, j]`. You may select any number of bundles, provided they do not overlap, and you may skip sections entirely. The goal is to return the maximum total revenue. The challenge is that every interval is a candidate, so brute-force subset selection over ranges is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the best non-overlapping spans from a dense set of precomputed interval scores. Examples include ad-slot packaging over contiguous inventory, genomic segment scoring, media chapter monetization, compiler optimization over code regions, and document annotation pipelines selecting non-conflicting spans. At scale, naive enumeration explodes because the candidate set is quadratic. A dynamic-programming formulation turns “evaluate all combinations of intervals” into a linear scan over endpoints with quadratic preprocessing, which is the difference between a usable planning primitive and a combinatorial dead end in production schedulers and pricing engines.

## 🔍 Problem Statement
Given `N` seat sections numbered `0` to `N - 1` and an `N x N` matrix `bundleRevenue`, choose a set of non-overlapping contiguous ranges that maximizes total revenue. If you choose interval `[i, j]`, you earn `bundleRevenue[i][j]`. Intervals may touch but cannot overlap, so `[0,1]` and `[2,4]` are valid together. Leaving sections uncovered is allowed.

Constraints:

- `1 <= N <= 300`
- `bundleRevenue` is `N x N`
- `0 <= bundleRevenue[i][j] <= 10^6` for `0 <= i <= j < N`
- Entries with `i > j` are irrelevant
- Target runtime: `O(N^2)` or `O(N^2 log N)`

Examples:

- Example 1: output `15` from choosing `[0,1] = 9` and `[2,2] = 6`
- Example 2: the stated explanation yields `8 + 12 = 20`, so the correct maximum is `20`, not `14`

The key constraint is that there are `O(N^2)` possible bundles, which rules out exponential search.

## 🪜 How to Solve This
1. Read the problem → notice the decision unit is an interval, not an individual section. That immediately suggests interval DP or weighted interval scheduling.

2. Observe the structure → every valid solution is a set of non-overlapping contiguous ranges. Once you decide the last chosen bundle ending at section `r`, everything before its start is an independent subproblem.

3. Reframe by prefix → let `dp[r]` mean the best revenue using only sections `0..r-1`. This is the standard “best answer for a prefix” state that collapses overlap constraints cleanly.

4. For each right boundary `r`, there are only two meaningful choices:
   - skip section `r-1`, so carry forward `dp[r-1]`
   - end a bundle at `r-1`, choosing some start `l`, which contributes `dp[l] + bundleRevenue[l][r-1]`

5. Take the maximum over all `l` for that endpoint. This works because any bundle ending at `r-1` partitions the problem into a non-overlapping prefix and one final interval.

6. Since there are `N` endpoints and up to `N` starts per endpoint, the full solution is `O(N^2)`, which fits comfortably for `N <= 300`.

## 🧩 Algorithm Walkthrough
1. **Define the DP state using prefix optimality.**  
   Let `dp[k]` be the maximum revenue obtainable from sections `[0, k-1]`. This is a **1D Dynamic Programming / weighted interval scheduling on dense intervals** formulation. The invariant: after computing `dp[k]`, it is the optimal answer for the first `k` sections.

2. **Initialize the empty prefix.**  
   Set `dp[0] = 0`. With no sections, no revenue can be earned. This anchors transitions where a chosen bundle starts at section `0`.

3. **Process prefixes from left to right.**  
   For each `k` from `1` to `N`, first set `dp[k] = dp[k-1]`. This represents skipping section `k-1` entirely. The invariant remains valid because leaving a section uncovered is allowed.

4. **Try every bundle that ends at section `k-1`.**  
   For each start `l` in `[0, k-1]`, consider taking interval `[l, k-1]`. Its value is `dp[l] + bundleRevenue[l][k-1]`.  
   Why correct: `dp[l]` uses only sections before `l`, so it cannot overlap with `[l, k-1]`.

5. **Keep the best transition.**  
   Update `dp[k] = max(dp[k], dp[l] + bundleRevenue[l][k-1])` for all `l`. After this loop, `dp[k]` is the best revenue among all solutions that either skip section `k-1` or end with some bundle at `k-1`.

6. **Return the full-prefix optimum.**  
   The answer is `dp[N]`, the best revenue over all sections. This formulation is preferable to 2D interval DP because the non-overlap constraint is directional and naturally collapses into prefix composition.

## 📊 Worked Example
Use Example 1:

`N = 4`

| k | Sections covered by `dp[k]` | Skip case | Best ending bundle | `dp[k]` |
|---|---|---:|---|---:|
| 0 | none | — | — | 0 |
| 1 | `[0]` | 0 | `[0,0]=5` → `dp[0]+5=5` | 5 |
| 2 | `[0,1]` | 5 | `[0,1]=9` → 9, `[1,1]=4` → `5+4=9` | 9 |
| 3 | `[0,2]` | 9 | `[0,2]=10`, `[1,2]=7` → 12, `[2,2]=6` → `9+6=15` | 15 |
| 4 | `[0,3]` | 15 | `[0,3]=10`, `[1,3]=8` → 13, `[2,3]=9` → 18, `[3,3]=3` → 18 | 18 |

This trace shows the optimal answer is actually `18`, from `[0,1]=9` and `[2,3]=9`. The example explanation’s `15` is valid but not optimal. The DP exposes such inconsistencies immediately because it evaluates every legal final interval for each prefix.

## ⏱ Complexity Analysis
### Time Complexity
`O(N^2)`. For each prefix endpoint `k`, the algorithm scans all possible starts `l` for a bundle ending at `k-1`. With `N <= 300`, this is trivial in practice. At `10^6` or `10^9` elements, quadratic work becomes infeasible, but this problem’s bounded `N` makes dense interval evaluation acceptable.

### Space Complexity
`O(N)` for the `dp` array storing the best revenue for each prefix length. The revenue matrix is part of the input, not auxiliary space. You cannot reduce the DP below linear space without losing prior prefix values needed for transitions.

## 💡 Key Takeaways
- If the input gives a score for every contiguous range and asks for a best non-overlapping subset, think weighted interval scheduling or prefix-based interval DP immediately.
- A strong signal is “choose any number of intervals, no overlap, skipping allowed” — that usually collapses into `dp[right] = max(skip, take-some-interval-ending-here)`.
- Use `dp[k]` for sections `0..k-1`, not `0..k`; this avoids awkward base cases and makes `dp[l] + value[l][k-1]` line up cleanly.
- Intervals that touch are valid: `[a,b]` and `[b+1,c]` do not overlap. Getting this boundary wrong silently undercounts the optimum.
- In production planning systems, dense candidate sets often look combinatorial until you find the right decomposition boundary; prefix optimality is the lever that turns them into tractable optimization passes.

## 🚀 Variations & Further Practice
- Add a limit of at most `K` bundles. Twist: the state becomes `dp[k][t]`, introducing a second optimization dimension for count-constrained selection.
- Charge a fixed activation cost per chosen bundle. Twist: single long intervals may dominate multiple short ones, changing transition economics but preserving the same DP skeleton.
- Allow only bundles whose lengths fall in a permitted set or satisfy business rules. Twist: transition space becomes filtered, which is common in pricing engines with policy constraints.