# Minimum Cost to Encode a Message with Reusable Dictionary Blocks

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Trie, String Matching

---

## 🗂 Problem Overview
Given a target string and a reusable dictionary of words with costs, compute the minimum total cost to cover the target exactly from left to right. At each position, you may place any dictionary word that matches the target starting there, then advance by that word’s length. Words can be reused arbitrarily. Return the minimum cost, or `-1` if no exact cover exists. The challenge is scale: brute-force substring matching and naive DP both blow up under `10^5`-sized inputs.

## 🌍 Engineering Impact
This pattern shows up in production compression pipelines, tokenizer design, compiler front-ends, packet classification, and search/query rewriting systems. You have a long stream, a reusable vocabulary, and a cost model that is local per token but globally optimized over the full sequence. At small scale, nested scans are acceptable; at platform scale, they become latency cliffs and memory amplifiers. The right approach separates concerns: fast match discovery via trie/automaton and optimal assembly via dynamic programming. That decomposition is what makes large dictionaries, repeated queries, and predictable tail latency feasible.

## 🔍 Problem Statement
You are given:

- a lowercase string `target`, length up to `10^5`
- an array `words`, up to `10^5` entries
- a parallel array `costs`, where `costs[i]` is the non-negative cost of using `words[i]`

From position `p`, you may use `words[i]` only if it exactly matches `target[p..p+len(words[i])-1]`. If used, you pay `costs[i]` and move to the next uncovered position. You must cover the entire target exactly, with no gaps or overlaps. Words may be reused any number of times. Duplicate dictionary strings may appear; only the cheapest effective cost for that string matters.

Return the minimum total cost, or `-1` if full coverage is impossible.

Examples:

- `target = "ababa"`, `words = ["ab","aba","ba","a"]`, `costs = [4,5,2,10]` → `9`
- `target = "codecode"`, `words = ["co","code","de","odec"]`, `costs = [3,8,4,5]` → `14`

The decisive constraint is the input size: `O(n * number_of_words)` matching is not viable.

## 🪜 How to Solve This
1. Read the problem → it is a shortest-path / minimum-cost segmentation problem on a string.  
2. The natural DP is `dp[i] = minimum cost to encode suffix starting at i` or prefix ending at `i`. That part is straightforward.  
3. The hard part is not DP state design; it is finding all words that match at each position without scanning the full dictionary every time.  
4. That immediately suggests a string-indexing structure: trie, Aho-Corasick, or grouped rolling hashes by length.  
5. A trie is the most direct fit here: from each target position, walk forward character by character and emit every dictionary word that ends on that path.  
6. Then DP becomes: for every match `target[i..j]`, relax `dp[j+1] = min(dp[j+1], dp[i] + cost(word))`.  
7. To handle duplicates, compress identical words to their minimum cost before building the trie.  
8. The result is a clean two-layer design: efficient match enumeration + optimal cost accumulation. That is the scalable mental model.

## 🧩 Algorithm Walkthrough
1. **Deduplicate dictionary entries by string**  
   Keep the minimum cost for each distinct word. This is correct because two identical strings create identical transitions in the DP graph; only the cheaper edge can ever matter. Invariant: each unique word appears once with its best cost.

2. **Build a trie over the unique words**  
   Each node stores child pointers and, if a word ends there, the minimum cost of that word. This is the **Trie + Dynamic Programming** pattern: the trie indexes all legal next moves from any target position.

3. **Define DP over target prefixes**  
   Let `dp[i]` be the minimum cost to encode `target[0..i-1]`. Initialize `dp[0] = 0`, all others to infinity. Invariant: when processing position `i`, `dp[i]` is the best known cost to reach that boundary exactly.

4. **Enumerate matches starting at each reachable position**  
   If `dp[i]` is infinity, skip it. Otherwise, walk the trie while scanning `target[i], target[i+1], ...` until the path breaks. Every terminal trie node at position `j` means a dictionary word matches `target[i..j]`.

5. **Relax transitions**  
   For each match ending at `j`, update `dp[j+1] = min(dp[j+1], dp[i] + wordCost)`. This is correct because every valid encoding is a sequence of such exact-match transitions, and DP takes the minimum over all predecessors.

6. **Return the terminal state**  
   If `dp[n]` is still infinity, no exact cover exists; return `-1`. Otherwise return `dp[n]`. The abstraction is right because the problem is a DAG shortest path over string boundaries, and trie traversal makes edge discovery efficient.

## 📊 Worked Example
Use `target = "ababa"` with deduplicated dictionary:

| Word | Cost |
|---|---:|
| `ab` | 4 |
| `aba` | 5 |
| `ba` | 2 |
| `a` | 10 |

Let `dp[i]` = min cost to cover first `i` chars.

| i | reachable cost | matches from `i` | updates |
|---|---:|---|---|
| 0 | 0 | `a`, `ab`, `aba` | `dp[1]=10`, `dp[2]=4`, `dp[3]=5` |
| 1 | 10 | `ba` | `dp[3]=min(5,12)=5` |
| 2 | 4 | `a`, `ab`, `aba` | `dp[3]=5`, `dp[4]=8`, `dp[5]=9` |
| 3 | 5 | `ba` | `dp[5]=min(9,7)=7`? No — from index 3, substring is `"ba"`, so this is valid only if positions align. Here it does, yielding full cover of length 5. |
| 4 | 8 | `a` | `dp[5]=min(7,18)=7` |

The minimum valid full cover is obtained by exact boundary transitions discovered during trie walks; DP keeps the cheapest one.

## ⏱ Complexity Analysis
### Time Complexity
`O(U + M)` to build the trie, where `U` is the number of unique words and `M` is the total length of those words, plus the total trie-walk work across target positions. In the common trie-based formulation this is `O(n * L)` in the worst case, where `L` is the maximum matched depth before failure. At `10^5` scale, this is practical only because total dictionary length is capped at `2 * 10^5`.

### Space Complexity
`O(M + n)` for the trie plus the DP array. The trie owns most of the structural memory; `dp` is linear in target length. Space can be reduced only marginally unless you replace the trie with a more compact automaton or hashed-length index, trading implementation complexity for memory locality.

## 💡 Key Takeaways
- If the problem says “minimum cost to cover a sequence exactly” and local choices can block future feasibility, think DP over boundaries, not greedy.
- If DP transitions depend on “which dictionary entries match here,” pair DP with a string-indexing structure such as a trie or Aho-Corasick.
- Deduplicate identical words by minimum cost before building the matcher; failing to do so wastes both trie nodes and transition work.
- Be precise about DP indexing: `dp[i]` as cost for the first `i` characters avoids off-by-one errors when a match ends at `j` and updates `dp[j+1]`.
- The transferable design insight is separation of concerns: one component enumerates valid transitions fast, another computes the global optimum over those transitions.

## 🚀 Variations & Further Practice
- Allow per-use penalties that depend on the previous chosen word. This turns plain DP into DP over `(position, previous-token-state)` and stresses state explosion control.
- Support many target queries against the same dictionary. This pushes the design toward Aho-Corasick or other reusable automata to amortize preprocessing.
- Permit wildcard characters or approximate matches in dictionary blocks. The conceptual twist is that transition discovery is no longer exact string matching, so both the matcher and the DP state become more complex.