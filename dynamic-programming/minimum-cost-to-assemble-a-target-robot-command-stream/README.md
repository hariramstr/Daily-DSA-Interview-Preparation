# Minimum Cost to Assemble a Target Robot Command Stream

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Trie, String Matching

---

## 🗂 Problem Overview
Given a target string, a list of reusable command macros, and a cost for each macro, compute the minimum total cost to form the target exactly by concatenating macros in order. Macros may be reused arbitrarily, and duplicate macro strings can appear with different costs. Return `-1` if exact assembly is impossible. The challenge is scale: with target length up to `10^5` and total dictionary size up to `2 * 10^5`, checking every macro at every position is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a large stream must be reconstructed from reusable fragments under cost constraints: compiler token expansion, network protocol decoding, speech/text segmentation, DNA motif assembly, and command or workflow synthesis in robotics pipelines. At small scale, brute-force matching is tolerable; at production scale, it collapses under dictionary growth and long inputs. The combination of trie-based prefix matching and dynamic programming turns an otherwise quadratic or worse search into a bounded traversal over valid transitions only. That matters when latency budgets are tight, dictionaries are shared across requests, and cost models encode real resource trade-offs.

## 🔍 Problem Statement
You are given:

- `target`: a non-empty lowercase string
- `macros[i]`: reusable non-empty lowercase strings
- `cost[i]`: cost of appending `macros[i]`

You may use any macro any number of times, but each chosen macro must match the next characters of `target` exactly at the current position. The goal is to assemble the entire `target` with minimum total cost. If no exact concatenation exists, return `-1`.

Constraints:

- `1 <= target.length <= 10^5`
- `1 <= macros.length <= 10^5`
- `1 <= macros[i].length <= 10^5`
- `sum(macros[i].length) <= 2 * 10^5`
- `1 <= cost[i] <= 10^9`

Examples:

- `target = "ababa"`, `macros = ["ab","aba","ba","a"]`, `cost = [4,5,2,10]` → `7`
- `target = "robot"`, `macros = ["ro","bot","obo","t"]`, `cost = [3,4,10,1]` → `-1`

The key constraint is that naive matching across all `(position, macro)` pairs is too slow.

## 🪜 How to Solve This
1. Start from the recurrence: if `dp[i]` is the minimum cost to build `target[0:i]`, then every valid macro that matches starting at `i` creates a transition to `dp[i + len]`.

2. That immediately suggests dynamic programming, but plain DP is not enough. The expensive part is discovering which macros match at each position.

3. Matching every macro against every position is the wrong axis: `O(n * m)` blows up. We need a structure that shares common prefixes across macros.

4. Shared prefixes → trie. Insert all macros into a trie, and at each target index walk forward through the trie character by character. Every terminal node encountered represents a valid macro match starting there.

5. Duplicate macro strings with different costs should collapse to the cheapest terminal cost. There is never a reason to keep a more expensive identical string.

6. Now the algorithm becomes: for each reachable `i`, traverse the trie along `target[i:]` until mismatch; for every terminal hit, relax `dp[end]`.

7. This works because the trie ensures we only explore prefixes that are actually in the dictionary, and DP ensures each prefix of the target keeps only its best known cost.

## 🧩 Algorithm Walkthrough
1. **Preprocess duplicate macros**  
   Build a map from macro string to its minimum cost. This is a correctness-preserving reduction: identical strings induce identical transitions, so only the cheapest one matters. Invariant: each distinct macro appears once with minimal cost.

2. **Build a trie over the reduced dictionary**  
   Insert each macro into a trie. Each terminal node stores the minimum cost of completing that macro. This is the **Trie + Dynamic Programming** pattern: trie for efficient prefix discovery, DP for optimal path cost accumulation.

3. **Initialize DP**  
   Let `n = target.length`. Create `dp[0..n]`, initialized to infinity, with `dp[0] = 0`. Invariant: `dp[i]` is the minimum cost to assemble exactly the first `i` characters, or infinity if unreachable.

4. **Scan target positions left to right**  
   For each index `i`, skip if `dp[i]` is infinity. Otherwise, start at trie root and walk forward over `target[i], target[i+1], ...` until the trie has no matching child. This bounds work to valid dictionary prefixes only.

5. **Relax transitions on terminal nodes**  
   Whenever the current trie node marks the end of a macro of length `L`, update `dp[i + L] = min(dp[i + L], dp[i] + terminalCost)`. Correctness follows from optimal substructure: any optimal assembly ending at `i + L` must come from some optimal assembly of prefix `i` plus one matching macro.

6. **Return the answer**  
   If `dp[n]` is still infinity, return `-1`; otherwise return `dp[n]`. The invariant after processing index `i` is that all optimal costs for prefixes ending at positions reachable from starts `<= i` have been considered.

## 📊 Worked Example
Use `target = "ababa"`, `macros = ["ab","aba","ba","a"]`, `cost = [4,5,2,10]`.

After deduplication, trie terminals are unchanged.

| i | dp[i] | Trie matches from `target[i:]` | Updates |
|---|---:|---|---|
| 0 | 0 | `"a"` cost 10, `"ab"` cost 4, `"aba"` cost 5 | `dp[1]=10`, `dp[2]=4`, `dp[3]=5` |
| 1 | 10 | `"ba"` cost 2 | `dp[3]=min(5,12)=5` |
| 2 | 4 | `"a"` cost 10, `"ab"` cost 4, `"aba"` cost 5 | `dp[3]=5`, `dp[4]=8`, `dp[5]=9` |
| 3 | 5 | `"ba"` cost 2 | `dp[5]=min(9,7)=7` |
| 4 | 8 | `"a"` cost 10 | no better update |

Final state: `dp = [0,10,4,5,8,7]`, so answer is `7`.

The winning path is prefix `0 -> 3` via `"aba"` and `3 -> 5` via `"ba"`.

## ⏱ Complexity Analysis
### Time Complexity
Building the deduplicated dictionary and trie costs `O(sumLen)`, where `sumLen` is the total length of all macros. The DP phase walks the trie from each reachable target index until mismatch, so worst-case time is `O(n * Lmax)` in adversarial inputs, but in practice bounded by valid prefix traversals rather than all macros. At `10^6` scale this is workable; at `10^9`, only heavily pruned traversals survive.

### Space Complexity
`O(sumLen + n)` for the trie plus the DP array. The trie dominates dictionary storage; `dp` dominates target-dependent memory. Space can be reduced only by giving up direct random access to prefix costs, which usually is not worth the complexity or loss of clarity.

## 💡 Key Takeaways
- If the problem says “minimum cost to build a string exactly from reusable words,” think shortest-path-style DP on prefix positions.
- If matching all dictionary entries at every position is obviously too expensive, shared-prefix indexing via a trie is the signal.
- Deduplicate identical macro strings by minimum cost before building the trie; failing to do so wastes both memory and transitions.
- Be precise about DP indexing: `dp[i]` means first `i` characters assembled, so a match ending at target index `j` updates `dp[j + 1]` or `dp[i + len]` depending on representation.
- The production lesson is to separate **state optimization** (DP) from **candidate generation** (trie): most scalable designs come from optimizing both dimensions, not just one.

## 🚀 Variations & Further Practice
- Add wildcard characters inside macros or target segments. The trie is no longer enough by itself; you need automata or augmented matching logic, which changes both transition generation and complexity guarantees.
- Charge cost per macro plus a position-dependent penalty or switching cost between macro types. The DP state must include more context than just the target index.
- Require counting the number of minimum-cost assemblies, not just the minimum cost. Same transition graph, but now DP must track both best cost and multiplicity under tie conditions.