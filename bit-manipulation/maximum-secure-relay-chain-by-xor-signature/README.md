# Maximum Secure Relay Chain by XOR Signature

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Dynamic Programming, Trie

---

## 🗂 Problem Overview
Given an array `signatures`, choose the longest subsequence in original index order such that consecutive XOR values are strictly increasing. If the chosen indices are `p1 < p2 < ... < pk`, then `(a[p1] XOR a[p2]) < (a[p2] XOR a[p3]) < ...`. Return the maximum possible chain length. The challenge is that `n` is up to `2 * 10^5`, so any solution that checks all previous pairs or all subsequences is too slow.

## 🌍 Engineering Impact
This pattern shows up when transitions are constrained by a monotone score derived from pairwise state differences: routing policy upgrades, streaming anomaly chains, compiler optimization passes ordered by profitability, and ranking pipelines that must preserve increasing confidence deltas. At small scale, brute-force pair evaluation works; at production scale, pairwise state explodes to `O(n^2)` and becomes both latency- and memory-prohibitive. The useful lesson is architectural: exploit structure in the scoring function — here, XOR and highest differing bit — so the system indexes transitions by compressed state rather than materializing all edges.

## 🔍 Problem Statement
You are given `n` non-negative integers `signatures[i]`, where `1 <= n <= 2 * 10^5` and `0 <= signatures[i] < 2^30`. Select a subsequence in increasing index order to maximize its length, under this rule: for every consecutive pair of selected nodes, the XOR value must be strictly larger than every XOR used earlier in the same chain.

Formally, if the chosen indices are `p1 < p2 < ... < pk`, then the sequence  
`(signatures[p1] XOR signatures[p2]), (signatures[p2] XOR signatures[p3]), ..., (signatures[p{k-1}] XOR signatures[pk])`  
must be strictly increasing.

A chain of length `1` is always valid.

Examples:

- `signatures = [1, 2, 7, 3]` → `3`
- `signatures = [4, 1, 6, 14, 2]` → `4`

The critical constraint is `n = 2 * 10^5`: this rules out naive DP over all previous pairs and forces a bit-structured optimization.

## 🪜 How to Solve This
1. Start with the obvious DP: let a transition from `j` to `i` be valid if `a[j] XOR a[i]` is larger than the last XOR used to reach `j`. That immediately suggests the state depends on both endpoint and previous threshold.

2. Then notice the threshold is an integer in `[0, 2^30)`, which is too large to track directly. So ask: what matters about comparing XOR values? For XOR, order is determined by the highest bit where two values differ.

3. That highest-differing-bit observation is the compression lever. Instead of storing every exact previous XOR, group states by the most significant bit of the last XOR. Within a fixed bit bucket, lower bits can be handled by trie navigation.

4. Now the problem becomes: for each new value `x`, query the best chain ending at an earlier value `y` whose previous XOR is strictly smaller than `x XOR y`, then update structures so future elements can extend through `x`.

5. A binary trie is the right tool because it lets you compare `x XOR y` against threshold classes bit by bit, while DP values stored on trie nodes summarize the best chain available under that prefix constraint.

## 🧩 Algorithm Walkthrough
1. **Define the DP state.**  
   Let `dp[i][b]` mean the best chain length ending at index `i` where the last XOR used has highest set bit exactly `b` (`0..29`). A length-1 chain is the base case and has no previous XOR. This state is sufficient because strict comparison between XOR values is decided first by highest set bit.

2. **Reframe transitions by bit dominance.**  
   To extend a chain ending at `j` with new node `i`, let `v = a[j] XOR a[i]`. Any prior last XOR with highest bit `< msb(v)` is automatically smaller than `v`. If the prior highest bit equals `msb(v)`, then lower bits decide, so we need a finer comparison inside that bucket. This is the core invariant that makes state compression possible.

3. **Maintain per-bit binary tries.**  
   For each possible highest bit `b`, keep a trie over prior endpoint values `a[j]`. Each trie node stores the maximum chain length for states ending at values passing through that node. This is a **DP + Binary Trie** pattern: trie for fast XOR-order queries, DP for chain length.

4. **Query for each `a[i]`.**  
   For every candidate highest bit `b`, query tries representing smaller previous XOR classes and the equal-`b` trie with lexicographic lower-bit filtering. The query returns the best extendable chain length ending before `i`. Add `1` to append `a[i]`.

5. **Insert new states.**  
   After computing all `dp` values for `i`, insert `a[i]` into the trie corresponding to each realized last-XOR bucket. Updates happen after queries, preserving subsequence order.

6. **Track the global maximum.**  
   The answer is the best value across all computed states, with `1` as the fallback. Correctness follows from the invariant that every trie summarizes all earlier endpoints reachable with a given last-XOR class, and every valid extension is discovered by the XOR comparison logic encoded in trie traversal.

## 📊 Worked Example
Take `signatures = [1, 2, 7, 3]`.

| Step | Current value | Best prior extension | New last XOR | Best chain length |
|---|---:|---|---:|---:|
| 1 | 1 | none | — | 1 |
| 2 | 2 | `[1]` | `1 XOR 2 = 3` | 2 |
| 3 | 7 | extend `[1,2]` since prior last XOR is `3` and `2 XOR 7 = 5 > 3` | 5 | 3 |
| 4 | 3 | from `[1]`, `1 XOR 3 = 2` gives length 2; from `[1,2]`, `2 XOR 3 = 1` fails since `1 <= 3`; from `[1,2,7]`, `7 XOR 3 = 4` fails since `4 <= 5` | varies | 2 |

Trace interpretation:

1. Start with singleton chains.
2. `2` can follow `1`, producing XOR `3`.
3. `7` can follow `2`; new XOR `5` is strictly larger than `3`, so chain length becomes `3`.
4. `3` cannot extend the best length-3 chain because its connecting XOR `4` is not greater than the previous edge XOR `5`.

Answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
With a 30-bit integer domain, each trie query/update touches `O(30)` nodes, and each element performs a constant number of bit-bucket operations. Total time is `O(n * 30 * 30)` in a straightforward implementation, effectively linear for `n = 2 * 10^5`. At `10^6` scale this remains practical; at `10^9`, even linear scans become the bottleneck.

### Space Complexity
Space is `O(n * 30)` in the worst case for trie nodes and DP summaries across bit buckets. The trie owns most of the memory. You can reduce constants with pooled node allocation or compressed sparse tries, trading simpler logic for tighter memory behavior.

## 💡 Key Takeaways
• If a subsequence constraint compares pairwise derived values and those values are XORs, immediately look for a highest-differing-bit reduction instead of exact-value state.  
• When `n` is large and transitions depend on `a[i] XOR a[j]`, “DP + binary trie” is a strong signal: it replaces explicit pair enumeration with indexed bitwise queries.  
• Strictly increasing means `>` not `>=`; equal XOR values cannot appear on adjacent chain edges, which matters when handling same-`msb` transitions.  
• Query-before-update is mandatory; inserting the current element too early silently allows self-use or out-of-order transitions.  
• The transferable design insight is to compress transition state by the comparison primitive the business rule actually uses, not by the raw metric domain.

## 🚀 Variations & Further Practice
- Require the consecutive XOR values to be non-decreasing instead of strictly increasing; the conceptual twist is handling equality correctly in same-prefix trie queries.
- Ask for the number of maximum-length valid chains, not just the length; now the DP state must aggregate counts under duplicate-optimum transitions.
- Generalize from subsequences to paths in a DAG with node labels and XOR edge scores; the harder part is combining topological DP with the same threshold-indexed trie optimization.