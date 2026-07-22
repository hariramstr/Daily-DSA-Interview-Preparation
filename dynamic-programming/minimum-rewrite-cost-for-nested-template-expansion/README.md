# Minimum Rewrite Cost for Nested Template Expansion

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, trie, string-matching

---

## 🗂 Problem Overview
Given a target string, a set of reusable templates with normal insertion costs, discounted chaining costs, and a per-character append cost, compute the minimum cost to build the target exactly from left to right. The challenge is that the cheapest way to reach position `i` is not determined by prefix length alone: it also depends on whether the last emitted block was a specific template, because only then can the same template be chained at a discount.

## 🌍 Engineering Impact
This pattern shows up in template/rendering engines, compiler macro expansion, content deduplication pipelines, and storage systems with chunk reuse or delta encoding. The operational issue is stateful reuse: the cost of emitting the next fragment depends on the immediately preceding fragment, not just on current position. At scale, naive substring checks or DP over all templates collapse under `10^5`-length inputs and tens of thousands of patterns. The right design — prefix DP plus indexed multi-pattern matching — turns an otherwise quadratic planner into something deployable in latency-sensitive text, build, or transformation pipelines.

## 🔍 Problem Statement
You must build `target` from an empty string using three operations:

1. append one lowercase letter for `appendCost`,
2. insert any template `templates[i]` for `cost[i]`,
3. if the most recent block used was exactly `templates[i]`, insert the same template again immediately for discounted `chainCost[i]`.

Templates are reusable without limit. Chaining is strict: it only applies to consecutive identical template uses, and any single-character append breaks the chain. Return the minimum total cost to build `target` exactly, or `-1` if impossible.

Constraints are large: `target.length <= 10^5`, `m <= 2 * 10^4`, template lengths up to `50`, and total template length up to `2 * 10^5`. That rules out checking every template at every position.

Examples:

- `target = "ababab"`, `templates = ["ab","aba"]`, `cost = [5,8]`, `chainCost = [2,6]`, `appendCost = 4` → minimum is `9`
- `target = "codecodex"`, `templates = ["code","x"]`, `cost = [7,10]`, `chainCost = [3,1]`, `appendCost = 2` → minimum is `12`

## 🪜 How to Solve This
1. Read the transition rules → notice cost depends on **prefix position + last template used**. That immediately suggests dynamic programming with extra state.

2. Try the obvious DP over `(position, lastTemplate)` → too large if done literally, because `position` is up to `10^5` and templates are up to `2 * 10^4`.

3. Observe what actually matters:
   - `dp[i]`: cheapest cost to build the first `i` characters by any method.
   - `chain[i][t]`: cheapest cost to build the first `i` characters where the **last block** is template `t`, ending exactly at `i`.

4. Now ask: when can template `t` end at position `i`? Only if `templates[t]` matches `target[i-len+1..i]`. That is a string-matching problem, not a DP problem.

5. Preprocess all template matches over the target using a trie/Aho-Corasick-style scan, or a trie with bounded-length checks since template length is at most `50`.

6. Once matches are known, transitions are simple:
   - start template `t` from `dp[start] + cost[t]`
   - chain template `t` from prior end with `chain[start][t] + chainCost[t]`
   - append one character from `dp[i] + appendCost`

The key insight is separation of concerns: use string indexing to find legal transitions, then DP to price them.

## 🧩 Algorithm Walkthrough
1. **Build a trie of templates grouped by terminal template IDs.**  
   This is the **Trie + Dynamic Programming** pattern. The trie gives efficient lookup of which templates match at each ending position in `target`. Because max template length is only `50`, even a bounded backward trie walk is viable. The invariant: every legal template placement is discovered exactly once.

2. **Scan the target and collect matches ending at each position.**  
   For each index `r`, find all templates `t` such that `templates[t]` matches a suffix ending at `r`. Store `(start, t)` in `matchesEndingAt[r]`. The invariant: `matchesEndingAt[r]` fully describes all non-append transitions that can produce prefix `r + 1`.

3. **Maintain `dp[i]` = minimum cost to build prefix length `i`.**  
   Initialize `dp[0] = 0`, others to infinity. Always allow single-character append:  
   `dp[i + 1] = min(dp[i + 1], dp[i] + appendCost)`.  
   This guarantees reachability for every prefix if appends are allowed.

4. **Maintain chain state only at valid template endings.**  
   Let `endCost[r][t]` be the minimum cost to build prefix `r + 1` with template `t` as the last block. For a match of `t` spanning `[l..r]`:
   - **fresh use:** `endCost[r][t] = min(endCost[r][t], dp[l] + cost[t])`
   - **chained use:** if `l > 0` and prefix `l` already ended with template `t`, then  
     `endCost[r][t] = min(endCost[r][t], endCost[l - 1][t] + chainCost[t])`

   The invariant: `endCost[r][t]` is the best cost among all constructions ending exactly with template `t`.

5. **Fold template-ending states back into prefix DP.**  
   After processing all matches ending at `r`, update `dp[r + 1]` with the minimum over all `endCost[r][t]`. This keeps `dp` as the global best regardless of last operation.

6. **Use sparse storage for chain states.**  
   Most `(position, template)` pairs never occur. Store only matched endings, e.g. hash maps or vectors keyed by ending position. This is what keeps the solution within memory bounds while preserving correctness.

## 📊 Worked Example
Take `target = "ababab"`, `templates = ["ab","aba"]`.

Let template `0 = "ab"` and `1 = "aba"`.

| Prefix length | Action / match | Derived cost |
|---|---|---:|
| `0` | start | `dp[0] = 0` |
| `1` | append `'a'` | `dp[1] = 4` |
| `2` | match `"ab"` at `[0..1]` | fresh: `dp[0] + 5 = 5`, so `endCost[1][0] = 5`, `dp[2] = 5` |
| `3` | match `"aba"` at `[0..2]` | fresh: `dp[0] + 8 = 8`, so `dp[3] = 8` |
| `4` | match `"ab"` at `[2..3]` | fresh: `dp[2] + 5 = 10`; chain from prior `"ab"` ending at `1`: `5 + 2 = 7` |
| `4` | best prefix | `dp[4] = 7` |
| `6` | match `"ab"` at `[4..5]` | fresh: `dp[4] + 5 = 12`; chain from prior `"ab"` ending at `3`: `7 + 2 = 9` |
| `6` | final | `dp[6] = 9` |

The optimal plan is `"ab"` once, then chain `"ab"` twice: `5 + 2 + 2 = 9`.

## ⏱ Complexity Analysis
### Time Complexity
Building the trie is `O(sumTemplateLengths)`. Matching is `O(n * L)` with bounded template length `L <= 50`, or near-linear with Aho-Corasick. DP processing is proportional to the number of matches. Overall: `O(sumTemplateLengths + n * L + matches)`, which is practical for `10^5` target length but not for `10^9`-scale streams without chunking or online indexing.

### Space Complexity
`O(sumTemplateLengths + matches)` for the trie plus sparse match/chain state. The dominant cost is storing valid template endings, not the DP array itself. You can reduce memory by streaming matches directly into DP, at the cost of tighter coupling between matching and transition logic.

## 💡 Key Takeaways
• If transition cost depends on the immediately previous emitted block, plain prefix DP is insufficient; look for a compressed “last action” state.  
• If many substrings must be checked against a dictionary of patterns, that is a trie/string-matching signal, not a nested-loop DP signal.  
• Chaining only applies when the previous block is the same template and adjacent; any character append must invalidate that state explicitly.  
• Be careful with indices: `dp[i]` is prefix length `i`, while a template match is usually tracked by inclusive end index `r`; most bugs come from mixing those conventions.  
• The production lesson is to separate legality discovery from cost optimization: indexed matching finds possible transitions, DP prices them.

## 🚀 Variations & Further Practice
- Allow chaining from any template to any other with a transition matrix; this turns the problem into DP over prefix plus automaton state with much denser transitions.
- Permit wildcard characters inside templates; matching now requires automata or rolling-hash verification, and the number of legal transitions can grow sharply.
- Add a budget on the number of single-character appends; the DP becomes multi-dimensional and forces explicit trade-offs between exact matching and expensive fallback operations.