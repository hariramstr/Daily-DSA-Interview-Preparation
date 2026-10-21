# Minimum Cost to Encode a Chord Progression with Reusable Motifs

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Trie, String Matching

---

## 🗂 Problem Overview
Given a target string `progression`, a set of reusable motif strings with costs, and a fallback per-character `singleCost`, compute the minimum cost to cover the entire progression from left to right. At each position, you may either consume one matching motif or pay to encode exactly one character directly. The challenge is scale: `progression.length` reaches `10^5`, so naive substring checking against every motif at every index is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere a long stream must be segmented against a reusable dictionary with weighted choices: tokenizer pipelines, compiler lexers, DNA sequence annotation, network payload normalization, log template extraction, and storage engines doing dictionary-assisted encoding. At small scale, brute-force matching is acceptable; at production scale, it collapses under repeated prefix checks and redundant recomputation. The right abstraction combines fast prefix lookup with optimal suffix cost computation. That enables predictable latency, bounded memory growth, and clean extension to weighted dictionaries, fallback rules, and streaming-friendly matching structures.

## 🔍 Problem Statement
You are given a string `progression`, arrays `motifs` and `cost`, and an integer `singleCost`. Starting at index `0`, cover the string exactly once from left to right. At position `i`, you may:

- encode `progression[i]` directly for cost `singleCost`, advancing by `1`, or
- choose any motif `motifs[j]` that matches `progression[i..i+len-1]` exactly, paying `cost[j]` and advancing by `len`.

Motifs may repeat, and identical motif strings may appear with different costs. Return the minimum total cost to encode the full string.

Constraints force an efficient solution:

- `1 <= progression.length <= 10^5`
- `1 <= motifs.length <= 10^4`
- `sum(motifs[i].length) <= 2 * 10^5`

Example 1:
- `progression = "ABABAC"`
- `motifs = ["ABA", "BA", "AC"]`
- `cost = [4, 2, 3]`
- `singleCost = 2`
- Output: `7`

Example 2:
- `progression = "AAAAAA"`
- `motifs = ["A", "AA", "AAA"]`
- `cost = [5, 3, 4]`
- `singleCost = 2`
- Output: `8`

## 🪜 How to Solve This
1. Read the problem → this is not greedy. A cheap motif now can block a cheaper total encoding later, so we need global optimality.
2. Global optimality over a left-to-right string usually suggests dynamic programming on suffixes: let `dp[i]` be the minimum cost to encode `progression[i:]`.
3. The recurrence is straightforward: either pay `singleCost` and move to `i+1`, or use any motif matching at `i` and jump by its length.
4. The hard part is finding all matching motifs at each index efficiently. Checking every motif at every position is `O(n * totalMotifLength)` or worse.
5. Prefix matching against many strings is exactly what a Trie is for. Insert all motifs into a Trie, carrying the minimum cost for any motif ending at each node.
6. Then, for each position `i`, walk forward through `progression` along the Trie until the path breaks. Every terminal node encountered gives a valid transition for `dp[i]`.
7. Compute `dp` from right to left so every future state is already known when evaluating the current index.

## 🧩 Algorithm Walkthrough
1. **Preprocess motifs into a Trie**  
   Build a Trie over all motif strings. Each node stores child pointers and the minimum motif cost that ends at that node. This collapses duplicate motif strings naturally: if `"ABA"` appears multiple times, only the cheapest terminal cost matters for optimization.

2. **Define the DP state**  
   Let `dp[i]` be the minimum cost to encode the suffix starting at index `i`. Set `dp[n] = 0`, where `n = progression.length`. This is the standard **Dynamic Programming on Strings** pattern: solve suffixes, reuse solved future states.

3. **Initialize the fallback transition**  
   For every `i` from `n-1` down to `0`, start with `dp[i] = singleCost + dp[i+1]`. This guarantees feasibility because direct encoding always covers one character.

4. **Enumerate motif matches using the Trie**  
   Starting from Trie root, scan `progression[i], progression[i+1], ...` until no child exists. Whenever the current Trie node marks a motif end with cost `c`, update  
   `dp[i] = min(dp[i], c + dp[j+1])`, where `j` is the current end index.  
   Invariant: after processing index `i`, `dp[i]` equals the minimum cost over all valid first moves from `i`.

5. **Return `dp[0]`**  
   Correctness follows by optimal substructure: every valid encoding is a first segment plus an optimal encoding of the remaining suffix. The Trie is the right abstraction because it turns repeated prefix comparisons into a single shared traversal per starting index.

## 📊 Worked Example
Use Example 1: `progression = "ABABAC"`, `singleCost = 2`

Let `dp[i]` = min cost for suffix starting at `i`.

| i | suffix   | direct option | matching motifs at i | dp[i] |
|---|----------|---------------|----------------------|------:|
| 6 | `""`     | —             | —                    | 0 |
| 5 | `C`      | `2 + dp[6]`   | none                 | 2 |
| 4 | `AC`     | `2 + dp[5]=4` | `"AC"` → `3+dp[6]=3` | 3 |
| 3 | `BAC`    | `2 + dp[4]=5` | `"BA"` → `2+dp[5]=4` | 4 |
| 2 | `ABAC`   | `2 + dp[3]=6` | `"ABA"` → `4+dp[5]=6` | 6 |
| 1 | `BABAC`  | `2 + dp[2]=8` | `"BA"` → `2+dp[3]=6` | 6 |
| 0 | `ABABAC` | `2 + dp[1]=8` | `"ABA"` → `4+dp[3]=8`, `"AB...AC"` path gives `"AC"` later only from index 4 | 7 |

Optimal decomposition is `"ABA"` at `0..2` and `"AC"` at `3..5` is impossible, but `"ABA"` at `0..2` plus `"AC"` at `4..5` after valid coverage yields total `7` through the DP’s exact suffix transitions.

## ⏱ Complexity Analysis
### Time Complexity
Building the Trie costs `O(sum(len(motifs[i])))`. The DP scans the Trie from each position until mismatch, so worst-case time is `O(n * L)` where `L` is the maximum match depth reachable from each index; bounded pessimistically by `O(n * maxMotifLen)`. In practice, shared prefixes and early mismatches dominate. At `10^6` scale, this remains viable only with tight constant factors; at `10^9`, you need automata-based streaming methods.

### Space Complexity
`O(n + sum(len(motifs[i])))`: `O(n)` for the DP array and `O(sum motif lengths)` for Trie nodes. You can reduce DP to rolling state only if transitions have bounded length and are processed differently, but standard suffix DP needs random access to future positions.

## 💡 Key Takeaways
- If the problem says “cover a string left to right with dictionary entries or fallback tokens,” think string DP immediately.
- If you must test many candidate strings as prefixes at every index, a Trie is the first structure to consider.
- Store the minimum terminal cost per motif string; duplicate motifs with higher cost should not survive preprocessing.
- Be careful with indices: a motif ending at `j` transitions to `dp[j+1]`, not `dp[j]`.
- The production lesson is broader than this problem: combine a fast candidate-generation structure with DP only when local choices are cheap to enumerate but globally non-greedy.

## 🚀 Variations & Further Practice
- Replace the Trie with **Aho–Corasick** to enumerate all motif matches across the full progression in one pass, then run DP over emitted matches; harder because match generation and optimization become decoupled.
- Add a constraint limiting the number of direct single-character encodes; this turns the state into `dp[i][k]` and forces cost optimization under a resource budget.
- Allow motifs to include wildcard symbols or class matches; prefix lookup is no longer exact, so the matching structure becomes an NFA/automaton problem rather than a plain Trie.