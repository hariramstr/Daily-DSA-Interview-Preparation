# Minimum Fatigue to Decode a Corrupted Beacon Stream

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, trie

---

## 🗂 Problem Overview
Given a corrupted beacon stream `s`, partition it into contiguous pieces so every piece length matches some dictionary code length. Each piece is decoded by exactly one dictionary code of the same length, with cost `fatigue(code) + character mismatches`. Compute the minimum total cost over the full stream, or `-1` if no valid length-based partition exists. The challenge is not the DP recurrence itself, but evaluating many candidate words efficiently under tight input bounds.

## 🌍 Engineering Impact
This pattern shows up in noisy protocol decoding, OCR/post-correction pipelines, DNA/read alignment heuristics, compiler recovery after token corruption, and streaming telemetry normalization. At small scale, brute-force substring-to-dictionary comparison is acceptable; at production scale, it collapses under combinatorial candidate checks and repeated prefix work. The right design separates global optimization from local matching cost: dynamic programming chooses segmentation, while a trie or length-indexed structure makes candidate evaluation tractable. That decomposition enables predictable latency, bounded memory growth, and clean extension paths when dictionaries become large, mutable, or tenant-specific.

## 🔍 Problem Statement
You are given a string `s` of length `n` (`1 <= n <= 5000`) and a dictionary of up to `2000` lowercase codes. Each code has a non-negative fatigue cost, code length at most `50`, and total dictionary character count at most `50000`.

You must partition `s` into contiguous pieces. For each piece, choose exactly one dictionary code of the same length. The cost of decoding that piece is:

`fatigue(code) + mismatch_count(piece, code)`

where `mismatch_count` is the number of positions with different characters. Return the minimum total fatigue to decode all of `s`, or `-1` if no partition uses only lengths present in the dictionary.

Example 1:
- `s = "abxdab"`
- `dictionary = [["ab",1],["ax",2],["xd",1],["dab",3],["zz",0]]`
- Output: `3`

Example 2:
- `s = "abcde"`
- `dictionary = [["ab",4],["c",2],["de",1],["xyz",0]]`
- Output: `7`

The decisive constraint is that naive DP plus full dictionary scans per position is too expensive.

## 🪜 How to Solve This
1. Start with the obvious recurrence: if `dp[i]` is the minimum cost to decode the first `i` characters, then every valid last piece ending at `i` gives  
   `dp[i] = min(dp[i-len] + best_cost(s[i-len:i], len))`.

2. That immediately exposes the real problem: computing `best_cost(substring, len)` fast. A naive scan over all words of that length for every `i` is too slow.

3. Notice the maximum code length is only `50`. That means each DP state only needs to consider at most `50` suffix lengths, which makes prefix DP viable.

4. Group dictionary codes by length. For a fixed end position and length, we only compare against codes that could legally match.

5. To avoid repeated substring slicing and to support efficient character-by-character traversal, store each length bucket in a trie. Walking the trie against the current suffix lets us accumulate mismatch counts incrementally.

6. For each `i`, traverse backward lengths `1..50` where possible, query the corresponding trie for the minimum `fatigue + mismatches`, and relax `dp[i]`.

7. If `dp[n]` stays unreachable, return `-1`; otherwise return it.

## 🧩 Algorithm Walkthrough
1. **Preprocess the dictionary by code length.**  
   Build one trie per distinct length. Insert every code into its length-specific trie and store the minimum fatigue at terminal nodes. This is the **Dynamic Programming + Trie** pattern: DP handles optimal segmentation; trie compresses candidate comparisons.

2. **Define DP state.**  
   Let `dp[i]` be the minimum cost to decode prefix `s[0:i]`. Initialize `dp[0] = 0`, all others to infinity.  
   Invariant: after processing position `i`, `dp[i]` is the optimal cost for that exact prefix.

3. **Enumerate possible last piece lengths.**  
   For each end index `i` from `1` to `n`, consider every dictionary length `L` with `L <= i`. If `dp[i-L]` is unreachable, skip.  
   Why correct: every valid decoding of `s[0:i]` must end with one legal piece of some dictionary length.

4. **Query the trie for length `L`.**  
   Traverse the trie using substring `s[i-L:i]`, but at each depth allow mismatch accumulation against all child edges. The query returns the minimum over all length-`L` codes of `fatigue(code) + mismatches`.  
   Invariant: query result is the exact local optimum for that substring and length bucket.

5. **Relax the DP transition.**  
   Update `dp[i] = min(dp[i], dp[i-L] + queryCost)`. This composes an optimal prefix solution with an optimal final-piece choice.

6. **Finish and report.**  
   Return `dp[n]` if finite, else `-1`. Unreachability means no sequence of dictionary lengths can cover the stream.

## 📊 Worked Example
Use `s = "abxdab"` and dictionary `{ "ab":1, "ax":2, "xd":1, "dab":3, "zz":0 }`.

| `i` | Prefix | Candidate lengths | Best transition | `dp[i]` |
|---|---|---:|---|---:|
| 0 | `""` | — | base case | 0 |
| 1 | `a` | none | no length-1 code | ∞ |
| 2 | `ab` | 2 | `dp[0] + cost("ab","ab") = 0 + 1` | 1 |
| 3 | `abx` | 3 | `dp[0] + cost("abx","dab") = 3 + 2` | 5 |
| 4 | `abxd` | 2, 3 | `dp[2] + cost("xd","xd") = 1 + 1` | 2 |
| 5 | `abxda` | 2, 3 | best is `dp[2] + cost("xda","dab") = 1 + 5` | 6 |
| 6 | `abxdab` | 2, 3 | `dp[4] + cost("ab","ab") = 2 + 1` | 3 |

Answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
Let `D` be total dictionary characters and `Lmax = 50`. Building the tries costs `O(D)`. The DP has `n` states, each considering at most `Lmax` lengths; each trie query is bounded by the code length, so total time is effectively `O(n * Lmax^2)` in a straightforward implementation, with small constants because `Lmax` is capped at 50. That is practical at this input scale; a quadratic scan over all dictionary words is not.

### Space Complexity
Space is `O(D + n)`: the tries store all dictionary characters once, and the DP array stores `n + 1` costs. You can reduce DP to a rolling window only if reconstruction is unnecessary, but full-prefix storage is usually the cleaner trade-off.

## 💡 Key Takeaways
- If the problem asks for a minimum cost over all partitions of a string, prefix DP should be your first mental model.
- If each DP transition requires searching many candidate strings with shared prefixes, a trie is a strong signal instead of repeated flat scans.
- Be precise about indexing: `dp[i]` should represent `s[0:i]`, so the last piece is always `s[i-L:i]`.
- Do not confuse “cannot decode” with “high cost”: unreachable states must stay at infinity and only become `-1` at the end.
- The scalable design is to decouple global optimization from local scoring: DP chooses structure, specialized indexes make each transition cheap.

## 🚀 Variations & Further Practice
- Allow insertions and deletions, not just substitutions. The local cost becomes edit distance, so each DP transition embeds another DP.
- Make the dictionary mutable with online inserts/deletes. The challenge shifts from static preprocessing to maintaining low-latency indexed matching under updates.
- Require reconstruction of the chosen partition and codes, or the top-`k` decodings. Same recurrence, but now predecessor tracking and tie-handling matter.