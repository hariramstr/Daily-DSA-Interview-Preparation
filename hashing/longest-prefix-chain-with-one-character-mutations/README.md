# Longest Prefix Chain with One-Character Mutations

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Dynamic Programming, String

---

## 🗂 Problem Overview
Given a set of distinct lowercase strings, compute the maximum length of a chain where each next word is formed by appending exactly one character and allowing at most one mismatch across the original prefix positions. The output is a single integer: the longest achievable chain length. The challenge is scale: with up to `2 * 10^5` words and total input size `2 * 10^6`, pairwise predecessor checks are prohibitively expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to detect near-neighbor evolution under strict structural constraints: autocomplete candidate expansion, compiler identifier normalization, DNA/protein sequence growth, fuzzy prefix indexes, and streaming entity-resolution pipelines. At production scale, the failure mode is usually accidental quadratic behavior hidden behind “simple” string comparisons. Hash-based grouping plus dynamic programming turns an unbounded comparison graph into a bounded local transition problem. That shift matters operationally: predictable latency, memory proportional to input size, and a design that can be partitioned by length or sharded by hash without changing correctness.

## 🔍 Problem Statement
You are given `words`, an array of distinct lowercase strings.

A word `a` can transition to word `b` iff:

1. `|b| = |a| + 1`
2. Comparing the first `|a|` characters of `b` against `a`, they differ in at most one position
3. The only added character is the final character of `b`

So `b` is an extension of `a` by one trailing character, with up to one mutation allowed in the inherited prefix. Exact prefix extension is also valid.

Return the maximum chain length over all valid chains.

Constraints:

- `1 <= words.length <= 2 * 10^5`
- `1 <= words[i].length <= 30`
- total length of all words `<= 2 * 10^6`

Examples:

- `["a","ab","ac","abc","acc","abca","acca"] -> 5`
- `["cat","bat","bate","bath","batch","catch","cater"] -> 3`

The key constraint is input size: checking all length-adjacent pairs directly is too slow.

## 🪜 How to Solve This
1. Read the transition rule → every edge goes from length `L` to length `L+1`. That immediately suggests dynamic programming by increasing word length.

2. Ask what makes two words compatible → the shorter word must match the longer word’s prefix in all but at most one position. So for a target word of length `L+1`, we need predecessors among words of length `L` that are either:
   - exactly its first `L` characters, or
   - equal after masking out one position.

3. Naively scanning all words of length `L` for every target still explodes. So the real problem is indexing predecessor candidates.

4. For each length bucket, store:
   - exact words → for zero-mutation matches
   - masked signatures of words with one position removed/ignored → for one-mutation matches

5. Then process lengths in order:
   - compute `dp[word] = 1 + best predecessor dp`
   - update the indexes for this length so longer words can query them

6. The reason this works is that word length is tiny (`<= 30`), so generating all masked variants per word is cheap. Hashing converts expensive string-to-string comparisons into O(1)-ish lookups.

## 🧩 Algorithm Walkthrough
1. **Group words by length** using an array or hash map keyed by `len`.  
   This exploits the DAG structure: valid transitions only go from `L` to `L+1`. The invariant is that when processing length `L+1`, all predecessor DP values for length `L` are already finalized.

2. **Maintain DP per word**: `dp[w] = longest chain ending at w`.  
   Base case is `1`, since every word can start a chain. This is standard **Dynamic Programming on a DAG**, where topological order is word length.

3. **Build predecessor indexes for each length `L`** from words of that length:
   - `exactMap[word] = max dp[word]`
   - `maskMap[masked_signature] = max dp[word]` over all words producing that mask  
   The masked signature represents “all characters except one position must match.” This is the core **Hashing** pattern.

4. **For each word `v` of length `L+1`**, derive its prefix `p = v[0:L]`.  
   Query:
   - exact predecessor: `exactMap[p]`
   - one-mutation predecessors: for each position `i` in `p`, compute the mask of `p` with position `i` ignored and query `maskMap`  
   Take the maximum over all hits.

5. **Set `dp[v] = 1 + bestMatch`**, or `1` if no predecessor exists.  
   Correctness follows because any valid predecessor must be either an exact prefix match or differ at exactly one prefix position, and those are exactly the cases indexed.

6. **After finishing all words of one length, publish their hashes for the next layer.**  
   The invariant is monotonic: indexes for length `L` never need updates once length `L+1` processing begins.

## 📊 Worked Example
Use `["a","ab","ac","abc","acc","abca","acca"]`.

| Word | Prefix queried | Best predecessor source | `dp` |
|---|---|---|---:|
| `a` | — | start chain | 1 |
| `ab` | `a` | exact `a` | 2 |
| `ac` | `a` | exact `a` | 2 |
| `abc` | `ab` | exact `ab` | 3 |
| `acc` | `ac` | exact `ac` | 3 |
| `abca` | `abc` | exact `abc` | 4 |
| `acca` | `acc` | exact `acc` | 4 |

The interesting part is masked lookup: when processing a word like `acca`, its prefix is `acc`. Its masks are `_cc`, `a_c`, `ac_`. Any length-3 word sharing one of those masks is a valid one-mutation predecessor. In this input, `abc` and `acc` are both candidates via masks, but `acc` gives the best chain, so `dp["acca"] = 4`. The global maximum is `5` through `a -> ab -> abc -> abca -> acca`.

## ⏱ Complexity Analysis
### Time Complexity
Let `S` be the sum of all word lengths. Each word of length `L` generates `L` masked signatures and performs `L` lookups, with `L <= 30`. So total time is `O(S)` with a small constant, or more precisely `O(sum |w|)`. At million-scale input, this is practical; quadratic pair checks are not.

### Space Complexity
`O(S)` for storing words, DP values, and hash indexes of exact strings plus masked signatures. The dominant owner is the per-length hash state. Space can be reduced by keeping only adjacent length layers, trading simpler implementation for tighter lifecycle management.

## 💡 Key Takeaways
- If transitions only occur between adjacent sizes, treat the input as a DAG ordered by size and reach for dynamic programming.
- If validity is “equal except for a tiny local difference,” build hashable normalized signatures instead of comparing full pairs.
- Be careful to compare only the first `|a|` characters of the longer word; the appended character is always excluded from mutation logic.
- Do not accidentally allow more than one mismatch by combining multiple mask hits; each transition must be validated by exactly one exact-or-single-mask condition.
- The production-grade insight is to replace dense similarity graphs with canonical hashed projections that preserve only the transitions the business rule actually allows.

## 🚀 Variations & Further Practice
- Allow insertion at any position, not just the end. The conceptual twist is that predecessor generation is no longer a single prefix; alignment becomes part of the state.
- Allow up to `k` mutations in the inherited prefix. This pushes the design from single-mask hashing toward combinatorial signature generation or edit-distance-style DP.
- Return the actual longest chain, not just its length. Same DP core, but now you must maintain parent pointers and handle deterministic tie-breaking.