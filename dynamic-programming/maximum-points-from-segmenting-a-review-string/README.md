# Maximum Points from Segmenting a Review String

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, String, Trie

---

## 🗂 Problem Overview
Given a lowercase string `s` and a dictionary of unique phrases with integer scores, compute the maximum total score obtainable by partitioning **all** of `s` into contiguous, non-overlapping dictionary phrases. Every character must be covered exactly once, and phrases may be reused arbitrarily. Return the best score, or `-1` if no full segmentation exists. The non-trivial part is that local matches are not enough: scores may be negative, so only a global optimization over valid suffixes works.

## 🌍 Engineering Impact
This pattern shows up anywhere a raw token stream must be fully explained by a weighted vocabulary: search query rewriting, ad keyword normalization, compiler tokenization with costs, OCR post-processing, speech lattice rescoring, and rule-based entity extraction. At small scale, brute-force substring checks are tolerable; at production scale, they collapse under repeated prefix matching and ambiguous segmentations. Dynamic programming gives correctness under competing local choices, while trie-based matching removes redundant scans. The combination matters when latency budgets are tight, dictionaries are large, and scoring semantics are not monotonic, making greedy heuristics operationally unsafe.

## 🔍 Problem Statement
You are given a string `s` (`1 <= |s| <= 5000`) and a list of unique phrases, each paired with an integer score. Each phrase length is at most `50`, the number of phrases is at most `2000`, and the total phrase length is at most `20000`. Scores may be negative, zero, or positive.

Split `s` into a sequence of dictionary phrases such that:

- every character belongs to exactly one phrase,
- phrases are contiguous and non-overlapping,
- each chosen phrase matches the corresponding substring exactly.

Return the maximum total score among all valid full segmentations. If no full segmentation exists, return `-1`.

Example 1:
- `s = "applepieapple"`
- `phrases = [["apple", 5], ["pie", 3], ["app", 2], ["lepie", 4]]`
- Output: `13`

Example 2:
- `s = "catsandog"`
- `phrases = [["cat", 4], ["cats", 7], ["and", 3], ["sand", 5], ["dog", 6]]`
- Output: `-1`

The key constraint is not just string length; it is the need to evaluate many overlapping phrase matches without recomputing suffix decisions.

## 🪜 How to Solve This
1. Start from the requirement that the **entire** string must be covered exactly once. That immediately suggests a prefix/suffix decomposition problem, not a greedy matching problem.

2. At any index `i`, the only meaningful question is: **what is the best score for segmenting `s[i:]`?** That naturally defines `dp[i]`.

3. If a phrase matches starting at `i` and has score `w`, then choosing it leaves the subproblem at `i + len(phrase)`. So the transition is:
   `dp[i] = max(w + dp[i + len])` over all matching phrases.

4. Invalid suffixes must stay invalid. Use a sentinel like negative infinity so unreachable states do not contaminate valid scores.

5. Compute from right to left because `dp[i]` depends only on later positions.

6. The remaining question is match efficiency. Naively testing every phrase at every index is avoidable. Since all transitions begin from the current character, a trie lets you walk forward once per index and enumerate only phrases that actually match that prefix.

7. Result: `dp[0]` if reachable, otherwise `-1`.

## 🧩 Algorithm Walkthrough
1. **Build a trie over all phrases**  
   Store children by character and, at terminal nodes, the phrase score. This converts “which phrases match `s[i:]`?” from scanning the whole dictionary into a bounded prefix walk. The pattern is **Dynamic Programming on Strings + Trie-based prefix enumeration**.

2. **Define the DP state**  
   Let `dp[i]` be the maximum score for fully segmenting the suffix `s[i:]`. Set `dp[n] = 0` because the empty suffix is already fully segmented with zero additional score. Initialize all other states to negative infinity to represent “unreachable.”

3. **Iterate indices from right to left**  
   For each `i`, walk the trie using characters `s[i], s[i+1], ...` until either the trie path breaks or the max phrase length is exceeded. This maintains the invariant that every trie node visited corresponds exactly to a phrase prefix matching `s[i:j]`.

4. **Apply transitions at terminal nodes**  
   Whenever the current trie node marks a complete phrase with score `w`, check whether `dp[j+1]` is reachable. If so, update `dp[i] = max(dp[i], w + dp[j+1])`. This is correct because any valid segmentation starting at `i` must choose some first phrase, and the remainder is optimally captured by `dp[j+1]`.

5. **Return the final answer**  
   If `dp[0]` is still unreachable, return `-1`; otherwise return `dp[0]`. The invariant at completion is that every `dp[i]` equals the optimal score for `s[i:]`, or unreachable if no exact segmentation exists.

## 📊 Worked Example
Use `s = "applepieapple"` and phrases `("apple",5)`, `("pie",3)`, `("app",2)`, `("lepie",4)`.

Let `n = 13`, `dp[13] = 0`.

| i  | Matching phrases from `s[i:]` | Transition(s) | dp[i] |
|----|-------------------------------|---------------|-------|
| 8  | `apple`                       | `5 + dp[13]`  | 5     |
| 5  | `pie`                         | `3 + dp[8]`   | 8     |
| 3  | `lepie`                       | `4 + dp[8]`   | 9     |
| 0  | `app`, `apple`                | `2 + dp[3] = 11`, `5 + dp[5] = 13` | 13 |

Trace:
1. Suffix `"apple"` at index `8` is directly segmentable, so `dp[8] = 5`.
2. At index `5`, `"pie"` leads to the known optimal suffix at `8`, giving `8`.
3. At index `3`, `"lepie"` is valid and reaches index `8`, giving `9`.
4. At index `0`, both `"app"` and `"apple"` match, but `"apple"` yields the better future state. Final answer: `13`.

## ⏱ Complexity Analysis
### Time Complexity
With a trie, each index walks forward only along matching characters, bounded by the maximum phrase length `L <= 50`. Total time is `O(n * L + totalPhraseLength)`, effectively `O(n * 50)` after trie construction. At million-scale strings this remains linear in input size; an `O(n * phrases)` approach does not.

### Space Complexity
`O(n + totalPhraseLength)`: `dp` uses `O(n)`, and the trie uses space proportional to the total number of phrase characters. You can reduce DP to less than `O(n)` only with more complex state management, but random suffix access makes the full array the pragmatic choice.

## 💡 Key Takeaways
- If the problem asks for the best way to cover an entire string with dictionary words, think suffix DP immediately.
- If transitions depend on “all words matching at this position,” combine DP with a prefix index such as a trie.
- `dp[n] = 0` is the only valid base case; every other state should start unreachable, not zero.
- Negative scores make “take any match” incorrect; unreachable and low-score states must be distinguished explicitly.
- In production systems, this is the standard pattern for exact coverage with weighted local choices: separate global optimization (DP) from efficient candidate generation (trie/index).

## 🚀 Variations & Further Practice
- Return the actual segmentation, not just the score: same DP core, but now you must store back-pointers and handle tie-breaking deterministically.
- Allow skipping characters with a penalty: turns exact coverage into weighted alignment, closer to sequence decoding than pure word break.
- Support phrase updates and many online queries: the harder part becomes data structure design for mutable dictionaries and amortized matching cost.