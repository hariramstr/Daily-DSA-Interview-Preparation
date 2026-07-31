# Minimum Fatigue to Type a Macro Script

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, suffix-array

---

## 🗂 Problem Overview
Given a string `s`, per-character typing costs, and a fixed macro-use cost, compute the minimum fatigue required to produce `s` left to right. At each position, you either type one character or emit a whole substring as a macro, but only if that exact substring already occurred entirely inside the previously produced prefix. The challenge is not the DP itself; it is efficiently determining, for every start position, which substring lengths are legally reusable under the prefix-only constraint.

## 🌍 Engineering Impact
This pattern shows up anywhere systems trade one-time discovery against repeated cheap reuse: compiler backends emitting repeated instruction sequences, text editors and IDEs with snippet expansion, dictionary-based compression, deduplicated log shipping, and storage engines exploiting repeated byte ranges. At small scale, naive substring checks are tolerable; at production scale they collapse into quadratic or cubic scans over hot paths. The important architectural move is separating **reuse eligibility discovery** from **cost optimization**. That decomposition enables predictable performance, cleaner invariants, and easier substitution of stronger indexing structures such as suffix arrays, suffix automata, or rolling-hash-backed match tables.

## 🔍 Problem Statement
You must generate a lowercase string `s` exactly from left to right.

Allowed actions:

- `Type(c)`: append `c`, paying `typeCost[c]`.
- `Define(l, r)`: free, but only if `s[l..r]` already appeared as a contiguous substring fully inside `s[0..l-1]`.
- `Use(l, r)`: append that macro, paying `macroCost`, then advance to `r + 1`.

Definitions are only useful after the corresponding text has already appeared in the produced prefix. Equivalent text from different positions is the same macro content.

Goal: return the minimum total fatigue.

Constraints:

- `1 <= |s| <= 2000`
- lowercase English letters only
- `typeCost.length == 26`
- `1 <= typeCost[k], macroCost <= 10^6`

Example:

- `s = "ababa"`, cheap `a/b`, `macroCost = 2` → answer `5`
- `s = "aaaaaa"`, expensive typing, `macroCost = 4` → compute the true optimum from the formal rules, not from informal guessing

The decisive constraint is `|s| <= 2000`: large enough that repeated substring scans are too slow, small enough for `O(n^2)` preprocessing plus `O(n^2)` DP.

## 🪜 How to Solve This
1. Read the transition carefully → the state is just “how much prefix have I already produced?” So a prefix DP is natural: `dp[i] = min fatigue to produce s[0..i-1]`.

2. From position `i`, typing is trivial: go to `i + 1` with cost `typeCost[s[i]]`.

3. The hard part is macro use → from `i`, we need every `j` such that `s[i..j]` appeared somewhere fully inside `s[0..i-1]`.

4. That is a substring-reuse query constrained by the current prefix boundary, not by global existence. So we need a precomputation that tells us the **maximum reusable length starting at each position `i`**.

5. Once we know `reuseLen[i]`, the DP becomes obvious: for any `1 <= len <= reuseLen[i]`, transition from `i` to `i + len` with cost `macroCost`.

6. How do we compute `reuseLen[i]`? Compare suffixes. For each earlier start `p < i`, the longest common prefix of suffixes `s[p..]` and `s[i..]` tells us how much text matches. But the earlier occurrence must end before `i`, so usable length is capped by `i - p`.

7. Therefore `reuseLen[i] = max over p < i of min(LCP(p, i), i - p)`. Precompute all pairwise LCP values in `O(n^2)`, then run the DP.

## 🧩 Algorithm Walkthrough
1. **Precompute suffix LCP table** using dynamic programming on suffixes.  
   Let `lcp[i][j]` be the length of the longest common prefix of suffixes starting at `i` and `j`. Fill from the end:
   - if `s[i] == s[j]`, then `lcp[i][j] = 1 + lcp[i+1][j+1]`
   - else `0`  
   This works because each state depends only on the diagonally next state. Invariant: after processing `(i, j)`, `lcp[i][j]` is exact.

2. **Compute maximum reusable macro length per start position.**  
   For each `i`, scan all earlier positions `p < i`. The substring starting at `i` can reuse at most:
   - `lcp[p][i]` characters by textual equality
   - `i - p` characters because the earlier occurrence must lie entirely in `s[0..i-1]`  
   So candidate length is `min(lcp[p][i], i - p)`. Take the maximum over all `p`.  
   Invariant: `reuseLen[i]` is the longest legal macro starting at `i`.

3. **Run prefix DP** — this is classic **Dynamic Programming on prefixes**.  
   Initialize `dp[0] = 0`, others to infinity. For each position `i`:
   - type one character: `dp[i+1] = min(dp[i+1], dp[i] + typeCost[s[i]])`
   - use any legal macro length `len in [1, reuseLen[i]]`:  
     `dp[i+len] = min(dp[i+len], dp[i] + macroCost)`

4. **Why this is correct.**  
   Every valid construction of the first `i` characters ends with either:
   - typing the last character, or
   - one macro use covering some suffix of that prefix  
   The preprocessing guarantees exactly the legal macro lengths, and the DP tries all of them. By optimal substructure, the minimum over these transitions is the true optimum.

## 📊 Worked Example
Take `s = "ababa"`, `typeCost[a]=1`, `typeCost[b]=1`, `macroCost=2`.

First compute reusable lengths:

| `i` | suffix | earlier starts `p` | best legal match | `reuseLen[i]` |
|---|---|---:|---:|---:|
| 0 | `ababa` | none | 0 | 0 |
| 1 | `baba`  | 0 | `min(LCP(0,1)=0,1)=0` | 0 |
| 2 | `aba`   | 0,1 | from `p=0`: `min(3,2)=2` | 2 |
| 3 | `ba`    | 0,1,2 | from `p=1`: `min(2,2)=2` | 2 |
| 4 | `a`     | 0..3 | from `p=2`: `min(1,2)=1` | 1 |

Now DP:

- `dp[0]=0`
- from `0`: type `a` → `dp[1]=1`
- from `1`: type `b` → `dp[2]=2`
- from `2`: type `a` → `dp[3]=3`; macro length `1..2` → `dp[4]=4`, `dp[5]=4`
- from `3`: no better result than existing
- final answer: `dp[5]=4`? Not valid for this trace unless costs permit exact path; with this setup, `dp[5]=5` via type `a`, type `b`, macro `"ab"`, type `a`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)` to build the LCP table, `O(n^2)` to compute `reuseLen`, and `O(n^2)` in the worst case for DP transitions over all reusable lengths. Overall: `O(n^2)`. At `n = 2000`, this is comfortable. At `10^6`, it is impossible; you would need compressed indexing and subquadratic transition handling.

### Space Complexity
`O(n^2)` for the LCP table, which dominates memory. `dp` and `reuseLen` are only `O(n)`. Space can be reduced only by replacing the full LCP matrix with a stronger string index structure, but that increases implementation complexity substantially.

## 💡 Key Takeaways
- If a string DP asks “from position `i`, what previously seen substring can I reuse?”, the real problem is usually substring-eligibility preprocessing, not the DP recurrence.
- Prefix-only legality is a strong signal that you need a state like “best reusable length starting at `i`” derived from suffix comparisons or a suffix-based index.
- The earlier occurrence must end before `i`; forgetting the cap `i - p` incorrectly allows overlapping self-reference.
- `reuseLen[i] = L` means **all** lengths `1..L` are legal macro uses, not just length `L`; missing that loses optimal transitions.
- The transferable design pattern is to decouple expensive structural discovery from cheap optimization, which is exactly how scalable compilers, compressors, and editors keep hot-path decisions simple.

## 🚀 Variations & Further Practice
- Allow macro definition to have a nonzero cost or a bounded macro table size. The problem becomes DP with resource management, not just prefix optimization.
- Permit reuse of any previously defined macro even if its source occurrence overlaps current output via earlier macro expansions. This breaks the simple prefix-occurrence invariant and changes the reachability model.
- Replace exact substring equality with approximate matching or weighted edit distance. The suffix/LCP preprocessing no longer applies directly; you need a different indexing or automaton strategy.