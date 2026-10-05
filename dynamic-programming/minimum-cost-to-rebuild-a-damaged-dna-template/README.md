# Minimum Cost to Rebuild a Damaged DNA Template

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string-matching, trie

---

## 🗂 Problem Overview
Given a DNA target string and a set of reusable fragments with per-use costs, compute the minimum total cost to assemble the target exactly by concatenating matching fragments. A fragment can be placed only where it matches the target substring at that position. Return the minimum cost, or `-1` if reconstruction is impossible. The challenge is scale: `target.length` is up to `10^5`, so naive substring checks across all fragments at every position are too expensive.

## 🌍 Engineering Impact
This pattern shows up in production systems that reconstruct or segment long sequences under weighted rules: genome assembly pipelines, compiler tokenization with weighted lexicons, search query rewriting, packet signature matching, and log parsing over large streams. The core issue is not just matching but choosing the cheapest valid composition globally. At scale, brute-force substring scans collapse under input size, and greedy choices produce unstable or wrong results. A trie-backed dynamic program turns repeated prefix checks into shared work, enabling predictable latency, bounded memory growth, and a design that remains viable as dictionaries and input streams grow.

## 🔍 Problem Statement
You are given a DNA string `target` over `{A, C, G, T}`, an array `fragments`, and an array `cost` where `cost[i]` is the price of using `fragments[i]` once. Each fragment may be reused any number of times. A valid reconstruction partitions `target` into contiguous pieces such that each piece equals some fragment used at that position. The goal is to minimize total cost.

Return the minimum cost to form the entire string, or `-1` if no exact partition exists.

Constraints:
- `1 <= target.length <= 10^5`
- `1 <= fragments.length <= 10^4`
- `1 <= fragments[i].length <= 200`
- `sum(fragments[i].length) <= 2 * 10^5`
- `1 <= cost[i] <= 10^9`

Examples:
- `target = "ACGTAC"`, `fragments = ["AC","CGT","GT","AC"]`, `cost = [4,5,3,2]` → `7`
- `target = "AAGT"`, `fragments = ["AA","AG","GT"]`, `cost = [3,4,2]` → `5`

The decisive constraint is `target.length = 10^5`: any `O(n * fragments * avgLen)` approach is non-viable.

## 🪜 How to Solve This
1. Start from the output shape → we need the cheapest way to build every prefix of `target`. That is classic dynamic programming: `dp[i] = min cost to build target[0:i]`.

2. Then ask: from position `i`, which fragments can start here? A direct scan over all fragments is too slow because `n` is large and fragments can total `2 * 10^5` characters.

3. Shared prefixes in fragments suggest a trie. Insert every fragment into a trie, and at terminal nodes keep the minimum cost for that exact fragment string. Duplicate fragment strings with different costs collapse naturally.

4. For each reachable `i`, walk forward through the trie using `target[i], target[i+1], ...` until the path breaks. Every terminal node encountered means a fragment matches starting at `i`, so update `dp[end]`.

5. This works because fragment length is capped at `200`, so each trie walk is short. The DP handles global optimality; the trie makes candidate generation cheap enough.

## 🧩 Algorithm Walkthrough
1. **Preprocess fragments into a Trie**  
   Build a trie over the alphabet `{A,C,G,T}`. Each node has up to four children and optionally stores `terminalCost`, the minimum cost of any fragment ending there.  
   **Why correct:** if the same fragment appears multiple times, only the cheapest one matters.  
   **Invariant:** every trie path from root to a terminal node represents a valid fragment with its minimum usable cost.

2. **Define the DP state**  
   Let `dp[i]` be the minimum cost to reconstruct the prefix `target[0:i)`. Initialize `dp[0] = 0`, all others to infinity.  
   **Why correct:** any exact reconstruction is a sequence of fragment placements covering prefixes.  
   **Invariant:** when processing index `i`, `dp[i]` is the best known cost to reach that boundary exactly.

3. **Transition by trie-guided matching**  
   For each `i` where `dp[i]` is finite, start at trie root and scan `target[j]` for `j = i .. min(n-1, i+199)`. If the trie has no child for `target[j]`, stop. Otherwise advance. Whenever the current trie node is terminal, update  
   `dp[j+1] = min(dp[j+1], dp[i] + terminalCost)`.  
   **Why correct:** every update corresponds to appending one valid matching fragment to an already optimal prefix.  
   **Invariant:** all reachable fragment matches starting at `i` are considered exactly once along the trie walk.

4. **Return the answer**  
   If `dp[n]` is still infinity, return `-1`; otherwise return `dp[n]`.  
   This is a **Dynamic Programming + Trie** pattern: DP chooses the cheapest composition, trie compresses repeated prefix matching across many strings.

## 📊 Worked Example
Use `target = "AAGT"`, `fragments = ["AA", "AG", "GT"]`, `cost = [3, 4, 2]`.

Trie terminals:
- `"AA"` → `3`
- `"AG"` → `4`
- `"GT"` → `2`

`dp = [0, inf, inf, inf, inf]`

| i | reachable? | trie matches from `i` | updates | dp after step |
|---|---:|---|---|---|
| 0 | yes | `"A"` → continue, `"AA"` terminal | `dp[2] = min(inf, 0+3)=3` | `[0, inf, 3, inf, inf]` |
| 1 | no | skip | none | `[0, inf, 3, inf, inf]` |
| 2 | yes | `"G"` → continue, `"GT"` terminal | `dp[4] = min(inf, 3+2)=5` | `[0, inf, 3, inf, 5]` |
| 3 | no | skip | none | `[0, inf, 3, inf, 5]` |

Result: `dp[4] = 5`.

Interpretation: build prefix `"AA"` for cost `3`, then suffix `"GT"` for cost `2`. No cheaper exact partition exists.

## ⏱ Complexity Analysis
### Time Complexity
Building the trie costs `O(sum(len(fragments)))`. The DP phase visits each target position once, and from each reachable position walks at most the maximum fragment length (`<= 200`) through the trie, so total time is `O(n * 200 + totalFragmentChars)`, effectively linear in `n`. At million-scale targets this remains practical; quadratic scans do not.

### Space Complexity
The trie stores one node per distinct fragment prefix, so space is `O(totalFragmentChars)`. The DP array adds `O(n)`. You can reduce DP memory only if you change the state model, but for exact prefix-cost reconstruction the full `O(n)` array is the simplest and most reliable trade-off.

## 💡 Key Takeaways
- If the problem asks for the cheapest exact construction of a string from reusable pieces, think prefix DP immediately.
- If transitions require checking many candidate strings that share prefixes, a trie is usually the right acceleration structure.
- Deduplicate identical fragment strings by keeping only the minimum terminal cost; failing to do this wastes work and can obscure correctness.
- Be precise about DP indexing: `dp[i]` should mean cost for `target[0:i)`, and a match ending at `j` updates `dp[j+1]`.
- The production lesson is broader than this problem: separate global optimization logic (DP) from candidate generation (trie/index), and both correctness and scalability improve.

## 🚀 Variations & Further Practice
- Allow a fragment to match with up to `k` mismatches. The trie alone is no longer enough; transitions become approximate matching with a larger DP state.
- Add a limit on how many times each fragment may be used. This turns an unbounded composition problem into a constrained optimization problem with additional state.
- Charge both fragment cost and position-dependent penalties. The segmentation DP remains, but the transition cost now depends on context, not just the fragment.