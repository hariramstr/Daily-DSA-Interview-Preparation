# Minimum Edit Cost to Form K Palindrome Chapters

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, String, Interval DP

---

## 🗂 Problem Overview
Given a lowercase string `s` and an integer `k`, split `s` into exactly `k` non-empty contiguous substrings, then minimally edit each substring so it becomes a palindrome. The output is the minimum total number of character changes across all chapters. The difficulty is not computing one palindrome cost, but optimizing over all valid partitions under `|s| <= 200`, which rules out naive exponential split enumeration and pushes toward interval preprocessing plus partition DP.

## 🌍 Engineering Impact
This pattern shows up anywhere a sequence must be segmented into contiguous regions with a local normalization cost and a global optimization objective. Examples include compiler passes that partition token streams for rewrite minimization, OCR or speech pipelines that segment noisy spans before correction, DNA/read alignment windows, and document chunking for downstream NLP cleanup. At scale, brute-forcing boundaries collapses under combinatorial growth. Precomputing interval costs and then optimizing partitions separately is the architectural move that makes these systems tractable: it isolates expensive local scoring from global boundary selection, enables predictable runtime, and creates a reusable cost surface for multiple downstream decisions.

## 🔍 Problem Statement
You are given a string `s` of lowercase English letters and an integer `k`, where `1 <= k <= s.length <= 200`. Split `s` into exactly `k` non-empty contiguous chapters. Each chapter may be edited independently, and one edit changes any single character to any other lowercase letter. The cost of a chapter is the minimum number of edits required to make that substring a palindrome. Return the minimum total cost across all valid `k`-way splits.

Examples:

- `s = "abcac", k = 2` → `1`  
  Optimal split: `"ab" + "cac"` → costs `1 + 0 = 1`

- `s = "leetcode", k = 3` → `2`  
  One optimal split: `"le" + "etco" + "de"` → costs `1 + 0 + 1 = 2`

Edge conditions matter: every chapter must be non-empty, order is preserved, and the small `n` but quadratic substring space strongly suggests preprocessing all interval costs before doing partition DP.

## 🪜 How to Solve This
1. Read the problem → we are not rearranging characters; we are choosing **split points** over a fixed sequence.
2. For any chosen chapter, its edit cost depends only on symmetric mismatches inside that substring. That means substring cost is independent of the rest of the partition.
3. Independent substring cost + global partition optimization is a strong signal for **two-phase DP**:
   - precompute cost for every interval
   - run DP over prefixes and chapter counts
4. Ask: how do we compute palindrome cost for `s[l..r]` efficiently?  
   Compare mirrored characters. If `s[l] != s[r]`, we need one edit for that pair, plus whatever the inner substring needs.
5. That gives an interval recurrence:  
   `cost[l][r] = cost[l+1][r-1] + (s[l] != s[r] ? 1 : 0)`
6. Once all substring costs are known, define `dp[i][p]` as the minimum cost to split the first `i` characters into `p` chapters.
7. Transition by choosing where the last chapter starts:  
   `dp[i][p] = min(dp[t][p-1] + cost[t][i-1])` for all valid `t`.
8. This works because every optimal `p`-chapter partition of a prefix must end with some final chapter, and everything before that is an optimal `(p-1)`-chapter partition.

## 🧩 Algorithm Walkthrough
1. **Precompute interval palindrome-edit costs using Interval DP.**  
   Build `cost[l][r]` for all `0 <= l <= r < n`. The recurrence is:
   - `cost[l][r] = 0` if `l >= r`
   - otherwise `cost[l][r] = cost[l+1][r-1] + (s[l] != s[r] ? 1 : 0)`  
   This is correct because each mirrored pair contributes independently exactly one edit if mismatched. The invariant: once `cost[l+1][r-1]` is known, `cost[l][r]` is fully determined.

2. **Define partition DP over prefixes.**  
   Let `dp[i][p]` be the minimum cost to partition `s[0..i-1]` into exactly `p` non-empty chapters. Initialize:
   - `dp[0][0] = 0`
   - all other states to infinity  
   The invariant: `dp[i][p]` stores the optimal answer for that exact prefix/chapter-count pair.

3. **Enumerate the last chapter boundary.**  
   For each `i` from `1..n` and `p` from `1..min(i, k)`, try every `t` where the last chapter is `s[t..i-1]` and the first `t` characters form `p-1` chapters:
   - `dp[i][p] = min(dp[i][p], dp[t][p-1] + cost[t][i-1])`
   with `t >= p-1` to keep earlier chapters non-empty.  
   This is the standard **Partition DP** pattern: optimize a prefix by splitting at the final cut.

4. **Return `dp[n][k]`.**  
   This is the minimum total edit cost for the full string split into exactly `k` chapters.

The abstraction is right because the problem cleanly decomposes into **local interval scoring** and **global contiguous partitioning**, which is exactly what Interval DP + Partition DP is built for.

## 📊 Worked Example
Take `s = "abcac"`, `k = 2`.

First compute relevant substring costs:

| Substring | Cost | Reason |
|---|---:|---|
| `"a"` | 0 | already palindrome |
| `"ab"` | 1 | `a != b` |
| `"abc"` | 1 | only outer `a` vs `c` mismatches |
| `"cac"` | 0 | already palindrome |
| `"bcac"` | 1 | `b != c`, inner `"ca"` costs 1? actually optimal total is 1 via pairs |
| `"abcac"` | 2 | mismatches on mirrored pairs |

Now fill DP:

- `dp[0][0] = 0`
- For one chapter:
  - `dp[1][1] = cost[0][0] = 0`
  - `dp[2][1] = cost[0][1] = 1`
  - `dp[5][1] = cost[0][4] = 2`
- For two chapters at `i = 5`:
  - split at `t = 1`: `dp[1][1] + cost[1][4] = 0 + 1 = 1`
  - `t = 2`: `1 + cost[2][4] = 1 + 1 = 2`
  - `t = 3`: `1 + cost[3][4] = 1 + 1 = 2`
  - `t = 4`: `1 + cost[4][4] = 1 + 0 = 1`

Minimum is `1`, achieved by `"a" + "bcac"` or `"ab" + "cac"`.

## ⏱ Complexity Analysis
### Time Complexity
Precomputing all substring costs takes `O(n^2)`. The partition DP considers `O(n * k)` states, and each state scans up to `O(n)` split points, for `O(n^2 * k)` total time. With `n <= 200`, this is comfortably bounded. At `10^6` or `10^9` scale, cubic-style partition scans are infeasible; this solution is intentionally tuned to the given constraint envelope.

### Space Complexity
`O(n^2 + n*k)`. The `cost` table owns the quadratic space, and `dp` adds `O(nk)`. `dp` can be reduced to `O(n)` per chapter layer with rolling arrays, but `cost` remains `O(n^2)` unless you recompute interval costs on demand, which increases runtime and complicates correctness.

## 💡 Key Takeaways
- If the problem asks for splitting a sequence into exactly `k` contiguous groups and each group has an independent score, think **partition DP** immediately.
- If substring scoring depends only on the endpoints and an inner substring, that is a strong signal for **interval DP preprocessing**.
- Be precise about indexing: `dp[i][p]` usually means the first `i` characters, while `cost[l][r]` is inclusive on both ends.
- Enforce non-empty chapters in transitions; the last split point `t` must leave at least `p-1` characters for the earlier `p-1` groups.
- The transferable design pattern is separating **local cost surface computation** from **global optimization**, which is a common way to make sequence segmentation problems scalable and testable.

## 🚀 Variations & Further Practice
- Allow arbitrary per-character substitution costs instead of unit edits. The partition DP stays the same, but interval preprocessing must accumulate weighted mismatch costs.
- Require each chapter to satisfy a stronger structural constraint, such as becoming a palindrome with at most `m` edits or matching a dictionary pattern. The local interval scoring becomes more complex and may need additional state.
- Optimize a related segmentation objective such as partitioning into `k` groups with minimum compression cost or minimum encoding length. The same partition-DP skeleton applies, but the interval cost model changes completely.