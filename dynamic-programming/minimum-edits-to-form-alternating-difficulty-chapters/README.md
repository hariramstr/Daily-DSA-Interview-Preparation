# Minimum Edits to Form Alternating Difficulty Chapters

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, partitioning

---

## 🗂 Problem Overview
Given a string `chapters` of `'E'` and `'H'`, split it into exactly `k` non-empty contiguous groups. Each group must be made uniform by editing characters, and adjacent groups must alternate labels. Return the minimum number of edits required, or `-1` if no such partition exists. The non-trivial part is optimizing both the partition boundaries and each group’s target label under `n <= 2000`, where brute-force partition enumeration is too expensive.

## 🌍 Engineering Impact
This pattern shows up in systems that segment ordered data into constrained runs: log compaction with alternating storage classes, streaming pipelines that batch records into mode-specific stages, genome or signal segmentation, and ranking pipelines that enforce alternating content classes. At scale, naive boundary search explodes because each cut decision interacts with future states. Dynamic programming matters when local edits are cheap but global structure is constrained. It enables predictable optimization over long sequences, supports exact trade-off computation, and avoids heuristic partitioning that becomes unstable under adversarial or highly skewed inputs.

## 🔍 Problem Statement
You are given a string `chapters` of length `n`, where each character is either `'E'` or `'H'`. You must partition the string into exactly `k` contiguous, non-empty groups. After partitioning, every character inside a group must be identical, and adjacent groups must alternate between `'E'` and `'H'`.

You may edit any character, flipping `'E'` to `'H'` or `'H'` to `'E'`, at cost `1` per edit. Return the minimum total edit cost needed to produce a valid partition. If no valid partition exists, return `-1`.

Constraints:
- `1 <= n <= 2000`
- `1 <= k <= n`

Examples:
- `chapters = "EEHHE", k = 3` → `0` using `EE | HH | E`
- `chapters = "EHEEEH", k = 4` → `1`

The key algorithmic constraint is `n = 2000`: large enough to rule out exponential partition search, small enough for quadratic dynamic programming.

## 🪜 How to Solve This
1. Read the problem → this is not just “edit a string”; it is “partition a sequence into exactly `k` segments with structural constraints.”
2. Each segment must be uniform → for any substring, the cost to make it all `'E'` or all `'H'` is easy to compute if you know prefix counts.
3. Adjacent groups must alternate → once you choose the label of one group, the next label is forced. That means the state only needs to know how many groups have been formed and what label the current group ends with.
4. Exact number of groups + contiguous partitioning → classic dynamic programming over prefixes.
5. Define `dp[g][i][c]` as the minimum cost to partition the first `i` chapters into `g` groups, where the `g`-th group is uniform character `c`.
6. Transition by choosing the start of the last group `j`: previous state must end with the opposite label, and the substring `chapters[j..i-1]` is converted to `c`.
7. Precompute substring conversion costs with prefix counts so each transition is O(1), keeping the overall solution polynomial.

That chain leads directly to DP with prefix sums.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix counts**  
   Build `prefE[i]` = number of `'E'` characters in `chapters[0..i-1]`. Then for any substring `[l, r]`, its length is `r - l + 1`, number of `'E'` is `prefE[r+1] - prefE[l]`, and number of `'H'` follows from length.  
   **Why correct:** prefix sums give exact character counts for any contiguous range in O(1).

2. **Derive segment conversion cost**  
   Cost to make substring `[l, r]` all `'E'` is `countH`; cost to make it all `'H'` is `countE`.  
   **Invariant:** every segment cost is computed independently and exactly.

3. **Define the DP state**  
   Let `dp[g][i][t]` be the minimum edits to convert the first `i` characters into exactly `g` alternating uniform groups, where the last group has type `t` (`0` for `'E'`, `1` for `'H'`).  
   **Pattern:** Dynamic Programming on prefixes + partitioning.

4. **Initialize base cases**  
   For one group, `dp[1][i][E]` is cost to make `chapters[0..i-1]` all `'E'`; similarly for `'H'`.  
   **Why correct:** one group consumes the whole prefix.

5. **Transition over last cut**  
   For `g >= 2`, choose previous cut position `j`, where the last group is `chapters[j..i-1]`. Then  
   `dp[g][i][E] = min(dp[g-1][j][H] + cost(j, i-1, E))`  
   `dp[g][i][H] = min(dp[g-1][j][E] + cost(j, i-1, H))`  
   for all valid `j` with at least one character per group.  
   **Invariant:** alternation is enforced by only transitioning from the opposite label.

6. **Return the best final label**  
   Answer is `min(dp[k][n][E], dp[k][n][H])`, unless unreachable.  
   Since `k <= n`, feasibility is automatic; `-1` is only needed if an implementation uses unreachable sentinels and no state is filled.

## 📊 Worked Example
Take `chapters = "EEHHE"`, `k = 3`.

Let `E=0`, `H=1`.

| State | Value | Reason |
|---|---:|---|
| `dp[1][2][E]` | 0 | `"EE"` already uniform `E` |
| `dp[1][2][H]` | 2 | flip both chars |
| `dp[2][4][H]` | 0 | cut at `j=2`: `dp[1][2][E]=0` + cost(`"HH"`→`H`)=0 |
| `dp[2][4][E]` | 2 | best previous `H` state plus cost to force last group to `E` |
| `dp[3][5][E]` | 0 | cut at `j=4`: `dp[2][4][H]=0` + cost(`"E"`→`E`)=0 |
| `dp[3][5][H]` | 1 | last char would need one flip |

Trace:
1. First group over prefix `"EE"` is cheapest as `E`.
2. Second group over `"HH"` must alternate, so `H` is forced and costs `0`.
3. Final group is `"E"`, alternating back to `E`, also cost `0`.

Answer: `min(dp[3][5][E], dp[3][5][H]) = 0`.

## ⏱ Complexity Analysis
### Time Complexity
The straightforward DP is `O(k * n^2)`: for each group count `g` and prefix end `i`, scan all possible previous cut positions `j`. Segment cost lookup is O(1) via prefix sums, so the transition loop dominates. At `n = 2000`, this is practical; at `10^6` or `10^9`, it is completely infeasible without stronger structure or optimization.

### Space Complexity
Space is `O(k * n)` for the DP table, plus `O(n)` for prefix counts. The DP dominates memory. It can be reduced to `O(n)` by keeping only the previous and current group layers, since transitions depend only on `g-1`, at the cost of losing full reconstruction data.

## 💡 Key Takeaways
- If a problem asks for an exact number of contiguous groups over a sequence, expect prefix-based partition DP rather than greedy splitting.
- If adjacent segments must alternate between a small set of labels, encode the ending label in the DP state; that usually collapses global constraints into local transitions.
- The valid cut range is easy to get wrong: after forming `g` groups over `i` characters, both the prefix before the cut and the last segment must remain non-empty.
- Base cases matter: `dp[1][i][t]` covers the entire prefix as one segment, not “first character only” or “any segment ending at `i`.”
- The transferable design insight is to separate local transformation cost precomputation from global structure optimization; that decomposition is common in production segmentation and scheduling systems.

## 🚀 Variations & Further Practice
- Allow three labels instead of two, with the rule that adjacent groups must differ. The twist is that the next label is no longer forced, so each transition fans out across multiple prior label states.
- Add per-cut penalties or per-group weights. This turns pure edit minimization into joint segmentation and cost modeling, closer to real pipeline batching or storage-tier assignment.
- Ask for reconstruction of the actual partition boundaries and final labels, not just the minimum cost. The conceptual extension is maintaining parent pointers while preserving space efficiency.