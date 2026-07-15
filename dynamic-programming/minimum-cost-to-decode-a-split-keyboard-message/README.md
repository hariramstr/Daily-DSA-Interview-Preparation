# Minimum Cost to Decode a Split Keyboard Message

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, memoization

---

## 🗂 Problem Overview
Given a raw string `s` and a dictionary of words with `decodeCost` and `splitPenalty`, decode all of `s` into a sequence of dictionary words at minimum total cost. A word may match a substring if the substring can be formed by partitioning the word into pieces, with each piece used either forward or reversed. Splitting increases cost by `(pieces - 1) * splitPenalty`. The challenge is efficiently testing many substring/word compatibility cases under tight length bounds.

## 🌍 Engineering Impact
This pattern shows up in OCR correction, keyboard/IME recovery, packet reassembly, noisy protocol decoding, and compiler/token recovery when raw input is partially corrupted but still structurally constrained. At scale, the hard part is not the outer DP; it is the repeated feasibility test between a candidate token and a span of observed bytes or characters. Without precomputed compatibility or memoized matching, systems degrade into quadratic-or-worse rescanning and become unusable in streaming pipelines or low-latency parsers. The right decomposition separates local matching cost from global sequence optimization, which is exactly how production decoders stay tractable.

## 🔍 Problem Statement
You are given a lowercase string `s` with `1 <= |s| <= 400` and a dictionary of up to `120` words, where total dictionary length is at most `2500` and each word length is at most `40`. Each dictionary entry provides `(word, decodeCost, splitPenalty)`.

A word can decode a contiguous substring of `s` if the substring can be partitioned into one or more non-empty pieces whose concatenation is the word, and each piece is matched either in original order or reversed. If `k` pieces are used, total cost for that word application is `decodeCost + (k - 1) * splitPenalty`.

Compute the minimum cost to decode the entire string. If no full decomposition exists, return `-1`.

Example 1:
- `s = "tabeltcode"`
- `dictionary = [["tablet", 5, 2], ["code", 3, 1], ["tab", 4, 1], ["let", 2, 1]]`
- Output: `8`

Example 2:
- `s = "abdc"`
- `dictionary = [["abcd", 6, 5], ["ab", 2, 1], ["cd", 2, 1]]`
- Output: `-1`

The decisive constraint is small word length but many substring checks, which pushes the design toward per-word local DP plus global prefix DP.

## 🪜 How to Solve This
1. Start from the output shape: we need the minimum cost to decode the full prefix of `s` → that strongly suggests prefix DP, `dp[i] = min cost for s[0..i-1]`.

2. For each ending position, we need to know which dictionary words can decode a suffix ending there, and at what minimum cost. So the real subproblem is not “can this word match?” but “what is the cheapest split count for this word against this substring?”

3. A direct brute-force over all partitions of a word is exponential. The structure to exploit is that pieces always preserve the original left-to-right order in the word; only each piece may be reversed.

4. That means matching a word `w` to a same-length substring `t` becomes a segmentation DP on prefixes:
   - either extend with a forward piece where `w[a..b] == t[a..b]`
   - or with a reversed piece where `w[a..b] == reverse(t[a..b])`

5. Precompute, for each word and each relevant substring of equal length, the minimum number of pieces needed to match. Convert that into cost using `decodeCost + (pieces - 1) * splitPenalty`.

6. Feed those local match costs into the outer prefix DP. This separates local compatibility from global optimization and keeps the state space bounded by `|s| <= 400` and `|w| <= 40`.

## 🧩 Algorithm Walkthrough
1. **Define the global DP state.**  
   Let `dp[i]` be the minimum cost to decode prefix `s[0..i-1]`. Initialize `dp[0] = 0`, others to infinity. The invariant is: after processing position `i`, `dp[i]` is the best achievable cost for that prefix.

2. **Iterate over starting positions.**  
   For every `i` with finite `dp[i]`, try every dictionary word `w` of length `m` such that `i + m <= n`. Only equal-length substrings matter because pieces partition the word exactly and preserve total length.

3. **Run a local segmentation DP for `(w, s[i..i+m-1])`.**  
   This is the key pattern: **Dynamic Programming with memoized substring compatibility**. Let `pieceDp[p]` be the minimum number of pieces needed to match the first `p` characters of `w` against the first `p` characters of the target substring. Transition from `q` to `p` if segment `w[q..p-1]` matches the target segment either forward or reversed.

4. **Test segment compatibility efficiently.**  
   For each candidate segment `[q, p)`, check:
   - forward: `w[q + k] == t[q + k]`
   - reversed: `w[q + k] == t[p - 1 - k]`  
   Since `m <= 40`, an `O(m^3)` local DP per word/substring is acceptable. The invariant is: `pieceDp[p]` stores the minimum piece count over all valid partitions ending at `p`.

5. **Convert piece count to monetary cost.**  
   If `pieceDp[m] = k`, then this word decodes the substring with cost `decodeCost + (k - 1) * splitPenalty`. Update `dp[i + m]`.

6. **Return the result.**  
   If `dp[n]` is still infinity, return `-1`; otherwise return `dp[n]`. Correctness follows from optimal substructure: every optimal full decoding ends with one valid word application on a suffix, and the prefix before it must also be optimally decoded.

## 📊 Worked Example
Take `s = "tabeltcode"` and word `"tablet"` with `(decodeCost=5, splitPenalty=2)` against substring `"tabelt"`.

| Prefix length `p` | Best pieces for `"tablet"[0..p)` vs `"tabelt"[0..p)` |
|---|---:|
| 0 | 0 |
| 1 (`t`) | 1 |
| 2 (`ta`) | 1 |
| 3 (`tab`) | 1 |
| 4 (`tabl`) | impossible |
| 5 (`table`) | impossible |
| 6 (`tablet`) | 2 |

Trace:
1. `pieceDp[0] = 0`.
2. Segment `[0,3)` matches forward: `"tab"` vs `"tab"` → `pieceDp[3] = 1`.
3. Segment `[3,6)` matches reversed: `"let"` vs `"elt"` reversed → valid, so `pieceDp[6] = 2`.
4. Cost for this application is `5 + (2 - 1) * 2 = 7`.

Then the outer DP tries `"code"` on the remaining suffix. Competing decompositions such as `"tab" + "let" + "code"` are evaluated the same way, and `dp` keeps only the cheapest prefix cost at each boundary.

## ⏱ Complexity Analysis
### Time Complexity
Let `n = |s|`, `D` be the number of dictionary words, and `L` the maximum word length (`<= 40`). For each start index and each candidate word, we run an `O(L^3)` local segmentation DP in the straightforward implementation. Total time is `O(n * D * L^3)`, which is practical here because `L` is capped at `40`. At `10^6`-scale inputs this would fail; bounded token length is what makes the approach viable.

### Space Complexity
Global DP uses `O(n)`. Each local word/substring check uses `O(L)` or `O(L^2)` depending on whether segment compatibility is checked on demand or precomputed. The dominant space is therefore `O(n + L^2)`, and can be reduced by recomputing compatibility at the cost of more CPU.

## 💡 Key Takeaways
- If the problem asks for minimum cost over a full string decomposition, prefix DP is the default starting point.
- If each token has an internal “can match in multiple structured ways” rule, separate local feasibility DP from global sequence DP.
- Do not forget that split penalty depends on the number of pieces, not the number of cut positions considered during search.
- The substring matched by a word must have exactly the same total length as the word; allowing shorter or longer spans is a silent correctness bug.
- The production-grade insight is to factor expensive local matching into a reusable compatibility layer, then compose it with a much simpler global optimizer.

## 🚀 Variations & Further Practice
- Allow arbitrary permutation of pieces rather than preserving piece order. The local matcher stops being simple segmentation DP and starts looking like subset DP or constrained matching.
- Add per-piece reversal cost instead of a flat split penalty. Now the local DP must optimize both piece count and orientation choices.
- Decode a stream online where `s` arrives incrementally. The challenge shifts from batch DP to maintaining rolling compatibility and amortized updates under latency constraints.