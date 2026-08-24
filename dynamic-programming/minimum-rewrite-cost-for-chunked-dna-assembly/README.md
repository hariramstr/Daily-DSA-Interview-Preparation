# Minimum Rewrite Cost for Chunked DNA Assembly

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string-matching, trie

---

## 🗂 Problem Overview
Given a DNA target string, a catalog of reusable fragments, per-fragment rewrite costs, and a penalty for changing fragment length between consecutive placements, compute the minimum cost to assemble the target exactly from left to right. Fragments may be reused arbitrarily but must match the target substring exactly where placed. The challenge is that local cheapest matches are not globally optimal because cost depends on both position and the previous fragment length.

## 🌍 Engineering Impact
This pattern shows up in systems that compose outputs from reusable units under transition penalties: compiler instruction selection with mode-switch costs, media transcoding pipelines with codec/profile changes, search/query planners with operator reconfiguration overhead, and genome assembly or oligo synthesis tooling with setup costs between batch shapes. At scale, naive enumeration explodes because every position can branch into many valid fragments. What matters is separating fast candidate discovery from stateful optimization. That architectural split—index first, optimize second—turns an exponential search space into something operationally predictable under production-sized catalogs.

## 🔍 Problem Statement
You are given `target` of length `n`, arrays `parts` and `cost`, and a non-negative `switchCost`. Starting at index `0`, repeatedly choose any fragment `parts[i]` whose text exactly matches the next uncovered substring of `target`. Add `cost[i]` for that placement. If its length differs from the previously used fragment length, also add `switchCost`; the first placement never pays this penalty.

Return the minimum total cost to cover `target` exactly, or `-1` if impossible.

Constraints:
- `1 <= n <= 10^4`
- `1 <= parts.length <= 2 * 10^4`
- `1 <= parts[i].length <= 50`
- `sum(parts[i].length) <= 2 * 10^5`

Examples:
- `target = "ACGTAC"`, `parts = ["AC","GT","ACG","TAC"]`, `cost = [3,2,5,4]`, `switchCost = 6` → `8`
- `target = "AACGT"`, `parts = ["AA","A","CG","GT"]`, `cost = [4,2,3,3]`, `switchCost = 5` → `-1`

The key constraint is large `n` with many reusable fragments, which rules out brute-force sequence exploration.

## 🪜 How to Solve This
1. Read the problem → the target is consumed left to right, so this is naturally a DP over positions.

2. Notice the switching penalty depends on the **previous fragment length**, not the previous fragment identity. That means the DP state does not need to remember which fragment was used, only what length it had.

3. At each position, we need all fragments that match the target starting there. Doing substring comparisons against every fragment at every index is too expensive, so build a trie over `parts` to enumerate only valid matches from each position.

4. Multiple fragments can have identical text and length but different costs. Keep only the minimum cost per distinct fragment string; worse duplicates are never useful.

5. Define `dp[pos][len]` conceptually as the minimum cost to assemble `target[0:pos]` where the last fragment used had length `len`. From each reachable state, extend using every matching fragment starting at `pos`.

6. Because fragment lengths are bounded by `50`, the “previous length” dimension is tiny. That makes the state space manageable: `O(n * 50)` DP, with trie-guided transitions.

## 🧩 Algorithm Walkthrough
1. **Deduplicate fragment costs by exact string.**  
   If the same fragment text appears multiple times, only the minimum cost matters. This preserves optimality because all future transitions depend only on fragment length and placement cost, not fragment identity.

2. **Build a trie from the unique fragments.**  
   Each terminal node stores the minimum cost of that fragment. The trie is the right abstraction because it turns “which fragments match `target` at position `i`?” into a bounded prefix walk instead of scanning the full catalog.

3. **Define the DP state.**  
   Use dynamic programming with state `dp[i][prevLen]`: minimum cost to cover the prefix ending at index `i`, where the last chosen fragment length is `prevLen`. Also maintain a sentinel `prevLen = 0` for the start state, meaning “no previous fragment yet.”

4. **Enumerate matches from each position via trie traversal.**  
   Starting at `target[i]`, walk forward up to length `50` or until the trie path breaks. Every terminal node yields a valid fragment of length `L` and base cost `c`.

5. **Relax transitions.**  
   From state `(i, prevLen)` to `(i + L, L)`, add `c` plus `switchCost` iff `prevLen != 0` and `prevLen != L`. This is correct because the problem’s only cross-step dependency is whether the length changed.

6. **Maintain the invariant.**  
   After processing position `i`, every stored DP value is the minimum achievable cost for that exact covered prefix and trailing length. Since all transitions move forward, standard left-to-right DP ordering is valid.

7. **Return the best terminal state.**  
   The answer is `min(dp[n][L])` over all lengths `L > 0`; if none are reachable, return `-1`.

## 📊 Worked Example
Use `target = "ACGTAC"`, `parts = ["AC","GT","ACG","TAC"]`, `cost = [3,2,5,4]`, `switchCost = 6`.

| Step | Position | PrevLen | Matching fragments | Transition cost | New state |
|---|---:|---:|---|---:|---|
| Start | 0 | 0 | `"AC"`(2,3), `"ACG"`(3,5) | first piece, no switch | `dp[2][2]=3`, `dp[3][3]=5` |
| 1 | 2 | 2 | `"GT"`(2,2) | `3 + 2 = 5` | `dp[4][2]=5` |
| 2 | 3 | 3 | `"TAC"`(3,4) | `5 + 4 = 9` | `dp[6][3]=9` |
| 3 | 4 | 2 | `"AC"`(2,3) | `5 + 3 = 8` | `dp[6][2]=8` |

Terminal states at `pos = 6` are:
- `dp[6][3] = 9` from `"ACG" + "TAC"`
- `dp[6][2] = 8` from `"AC" + "GT" + "AC"`

Minimum is `8`.

## ⏱ Complexity Analysis
### Time Complexity
Building the trie is `O(sum(parts[i].length))`. The DP processes `n` positions, up to `51` previous-length states, and each trie walk advances at most `50` characters, so the practical bound is `O(n * 50 * 50)`, plus trie construction. This is tractable at `10^4` target length; it would not scale if fragment length were unbounded.

### Space Complexity
`O(sum(parts[i].length) + n * 51)` for the trie and DP table. The DP dominates only linearly in `n` because the previous-length dimension is capped. It can be reduced with sparse maps per position, trading simpler indexing for higher constant factors.

## 💡 Key Takeaways
• If a segmentation problem adds a penalty based only on the previous choice’s small attribute, that attribute is usually the extra DP dimension.  
• If matching candidates are “all dictionary entries that prefix-match here,” a trie is the right signal, not repeated substring scans.  
• The first placement is a special state; model it explicitly with a sentinel previous length to avoid incorrect switch penalties.  
• Deduplicate identical fragment strings by minimum cost before building the trie, or you carry useless transitions through the whole DP.  
• The production-grade insight is to decouple candidate generation from optimization: index the catalog for fast local feasibility, then run DP over the compact state that actually affects global cost.

## 🚀 Variations & Further Practice
- Allow a bounded number of mismatches per fragment placement. The trie alone is no longer enough; you need DP over trie position and edit budget, or automata-based matching.  
- Charge switch penalties based on fragment identity class rather than length. The DP state grows from `50` lengths to potentially many classes, forcing compression or graph-shortest-path formulations.  
- Add per-position rewards/penalties or forbid reusing the same fragment consecutively. This introduces richer transition state and tests whether the current DP dimension is still sufficient.