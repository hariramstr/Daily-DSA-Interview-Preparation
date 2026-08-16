# Minimum Cost to Restore a Merged Manuscript

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, String, Trie

---

## 🗂 Problem Overview
Given a target string, choose reusable fragments with associated placement costs to reconstruct the target exactly from left to right. A fragment can be placed only if it matches the next substring at the current position, and fragments may be reused unlimited times. Return the minimum total cost, or `-1` if reconstruction is impossible. The non-trivial part is that greedy local choices fail: the cheapest matching fragment now may block or increase the optimal completion later.

## 🌍 Engineering Impact
This pattern shows up anywhere a system assembles a canonical output from reusable components under cost constraints: compiler token expansion, OCR/post-processing correction pipelines, DNA sequence assembly from known motifs, template-driven document generation, and search query rewriting. At scale, naive backtracking explodes combinatorially, while a prefix-DP model gives deterministic latency and predictable memory. Adding a Trie matters when the candidate dictionary is large and heavily shared by prefixes: it converts repeated substring scans into structured prefix traversal, which is exactly the kind of optimization that separates toy implementations from production-grade text and sequence systems.

## 🔍 Problem Statement
You are given:

- `target`: a lowercase string, `1 <= target.length <= 5000`
- `fragments`: up to `1000` non-empty lowercase strings
- `cost[i]`: placement cost for `fragments[i]`, with `1 <= cost[i] <= 10^6`

You may use any fragment any number of times. To build `target`, you must cover it from index `0` to `n - 1` without gaps or reordering. A chosen fragment must exactly match the substring starting at the current position. The goal is to minimize total cost.

Return the minimum total cost to form the full string, or `-1` if no exact reconstruction exists.

Examples:

- `target = "abracadabra"`
- `fragments = ["ab", "ra", "cad", "a", "bra"]`
- `cost = [4, 2, 5, 1, 3]`
- Output: `12`

- `target = "applepenapple"`
- `fragments = ["apple", "pen", "app", "lepen"]`
- `cost = [5, 2, 3, 10]`
- Output: `12`

The key constraint is `target.length = 5000`: large enough that exponential search is impossible, but small enough for prefix DP with efficient matching.

## 🪜 How to Solve This
1. Read the problem → we are building the string strictly left to right, so every decision depends only on the current prefix already completed.

2. “Minimum cost to reach position `i`” is the natural state → define `dp[i]` as the minimum cost to build `target[0:i]`.

3. From any reachable position `i`, try every fragment that matches starting at `i` → if fragment `f` of length `L` matches, update `dp[i + L] = min(dp[i + L], dp[i] + cost)`.

4. That already gives the recurrence, but matching every fragment at every position is wasteful → many fragments share prefixes, so organize them in a Trie.

5. With a Trie, start from `target[i]` and walk forward character by character. Every time the Trie reaches a terminal node, you found a valid fragment ending there and can relax the next DP state.

6. This works because the problem is over prefixes, costs are additive, and reuse is unlimited. Once `dp[i]` is optimal, every transition out of `i` is independent of how that prefix was formed.

## 🧩 Algorithm Walkthrough
1. **Build a Trie of fragments**  
   Insert every fragment into a Trie. At each terminal node, store the minimum cost among fragments ending there. This is correct because identical fragment text with higher cost is never better than the cheaper equivalent. Invariant: every root-to-node path represents a fragment prefix.

2. **Define the DP state**  
   Let `dp[i]` be the minimum cost to form the first `i` characters of `target`. Initialize `dp[0] = 0` and all other entries to infinity. Invariant: before processing transitions from `i`, `dp[i]` is the best known cost for prefix `target[0:i]`.

3. **Skip unreachable prefixes**  
   If `dp[i]` is infinity, no valid reconstruction reaches position `i`, so no outgoing transitions matter. This prevents useless Trie traversals.

4. **Traverse the Trie from each reachable index**  
   Starting at target position `i`, walk forward through `target[j]` while following Trie edges. Stop when no edge exists or the maximum fragment length is exceeded. This enumerates exactly the fragments matching `target[i:j]`.

5. **Relax DP transitions at terminal Trie nodes**  
   Whenever the current Trie node marks a complete fragment with cost `c`, update `dp[j + 1] = min(dp[j + 1], dp[i] + c)`. This is the standard **Dynamic Programming over prefixes**, with the Trie acting as an efficient transition generator.

6. **Return the final answer**  
   If `dp[n]` is still infinity, return `-1`; otherwise return `dp[n]`. Invariant at completion: `dp[k]` is optimal for every prefix length `k`, so `dp[n]` is the minimum total reconstruction cost.

## 📊 Worked Example
Use `target = "applepenapple"` with fragments:

| Fragment | Cost |
|---|---:|
| `apple` | 5 |
| `pen` | 2 |
| `app` | 3 |
| `lepen` | 10 |

Let `dp[i]` be min cost for first `i` chars.

1. `dp[0] = 0`
2. From index `0`, Trie matches:
   - `app` → `dp[3] = 3`
   - `apple` → `dp[5] = 5`
3. From index `3`, remaining string starts with `"le..."`:
   - `lepen` matches → `dp[8] = 13`
4. From index `5`, remaining string starts with `"pen..."`:
   - `pen` matches → `dp[8] = min(13, 5 + 2) = 7`
5. From index `8`, remaining string starts with `"apple"`:
   - `app` → `dp[11] = 10`
   - `apple` → `dp[13] = 12`

Final state: `dp[13] = 12`, so the answer is `12`.

## ⏱ Complexity Analysis
### Time Complexity
Building the Trie costs `O(sum(len(fragments)))`. The DP phase is `O(n * L)` where `n = target.length` and `L` is the maximum fragment length, because each reachable index walks forward at most `L` Trie edges. With constraints here, that is roughly `5000 * 50`, comfortably bounded. At `10^6` or `10^9` scale, this pattern only remains viable if transition fanout stays tightly capped.

### Space Complexity
The DP array uses `O(n)` space, and the Trie uses `O(sum(len(fragments)))`. Total space is linear in target length plus total fragment characters. You could reduce constant factors with compressed Trie structures, but usually at the cost of implementation complexity and slower updates.

## 💡 Key Takeaways
- If the problem asks for a minimum cost to build a string prefix-by-prefix with exact matching, prefix DP should be your first instinct.
- If many candidate strings are tested repeatedly against many start positions, shared-prefix structure is a strong signal to introduce a Trie.
- Store the minimum cost per terminal fragment text in the Trie; duplicate strings with higher cost only add noise.
- Be careful with indices: `dp[i]` represents the first `i` characters, so a match ending at target index `j` updates `dp[j + 1]`.
- The transferable design insight is to separate state optimization from transition generation: DP finds the optimal state progression, Trie makes candidate enumeration cheap.

## 🚀 Variations & Further Practice
- Allow replacing, deleting, or inserting characters with penalties instead of exact fragment matches; this turns the problem into a hybrid of segmentation and edit-distance DP.
- Limit each fragment to a bounded number of uses; the unlimited-reuse assumption disappears, and the state must encode inventory or counts.
- Return not only the minimum cost but also the number of distinct minimum-cost reconstructions; same DP skeleton, but now you maintain both optimal value and path multiplicity.