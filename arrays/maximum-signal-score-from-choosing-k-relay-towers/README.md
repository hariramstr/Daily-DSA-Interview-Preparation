# Maximum Signal Score from Choosing K Relay Towers

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Monotonic Stack

---

## 🗂 Problem Overview
Given `heights` and an integer `k`, choose exactly `k` towers in original order to maximize the sum of `min` values across each adjacent chosen pair. Formally, for chosen indices `i1 < i2 < ... < ik`, maximize `Σ min(heights[it], heights[i(t+1)]))`. The output is a 64-bit integer. The difficulty is that choices are non-contiguous and globally coupled: selecting one tower changes the contribution structure of both its left and right neighbors, so greedy local decisions fail.

## 🌍 Engineering Impact
This pattern shows up in sequence-constrained optimization where pairwise edge value is capped by the weaker endpoint: network path provisioning, replica placement across heterogeneous nodes, streaming stage chaining, ad-slot sequencing, and search/ranking pipelines with compatibility ceilings. At scale, brute-force subset search is dead on arrival, and naive `O(n^2 k)` dynamic programming collapses under production-sized inputs. The useful architectural lesson is recognizing when a pairwise transition can be decomposed by dominance structure. Here, monotonic-stack preprocessing converts an intractable transition fanout into a bounded set of meaningful predecessors, enabling predictable performance under large `n` and moderate `k`.

## 🔍 Problem Statement
You are given an integer array `heights` of length `n`, where `heights[i]` is the elevation of the `i`-th relay tower on a line. Choose exactly `k` towers while preserving left-to-right order. If the chosen indices are `i1 < i2 < ... < ik`, the score is:

`min(heights[i1], heights[i2]) + min(heights[i2], heights[i3]) + ... + min(heights[i(k-1)], heights[ik])`.

Return the maximum possible score as a 64-bit integer.

Constraints:

- `2 <= n <= 200000`
- `1 <= heights[i] <= 1e9`
- `2 <= k <= min(n, 200)`

Examples:

- `heights = [5,1,4,6,3], k = 3` → `8`
- `heights = [2,7,3,9,5,8], k = 4` → `17`

The key constraint is the combination of very large `n` and moderate `k`: it rules out quadratic predecessor scans and pushes toward DP with transition compression.

## 🪜 How to Solve This
1. Start with the obvious DP: let `dp[t][i]` be the best score for choosing exactly `t` towers and ending at index `i`.
2. The transition is immediate:  
   `dp[t][i] = max over j < i of (dp[t-1][j] + min(heights[j], heights[i]))`.
3. Then notice the bottleneck: scanning all `j < i` gives `O(n^2 k)`, impossible for `n = 2e5`.
4. Split the transition by height relation:  
   - if `heights[j] <= heights[i]`, contribution is `dp[t-1][j] + heights[j]`  
   - otherwise contribution is `dp[t-1][j] + heights[i]`
5. That suggests two different queries while sweeping left to right:
   - best value among prior towers with height `<= current`
   - best `dp[t-1][j]` among prior towers with height `> current`, then add `current`
6. Heights are large, so compress them.
7. Use Fenwick/segment trees for those two prefix/suffix max queries.
8. The monotonic-stack angle helps reason about dominance: only height order matters for which side of `min` is active, and monotone structure is the right mental model for pruning irrelevant predecessors.
9. With one left-to-right pass per `t`, the problem becomes tractable.

## 🧩 Algorithm Walkthrough
1. **Define the DP state.**  
   Let `prev[i]` be the best score for selecting `t-1` towers ending at `i`, and `cur[i]` for `t` towers ending at `i`. Base case: for `t = 1`, score is `0` for every `i`, because no adjacent pair exists yet.  
   **Invariant:** `prev[i]` always represents a valid ordered selection ending at `i`.

2. **Rewrite the transition by cases.**  
   For current endpoint `i`, every predecessor `j < i` contributes `prev[j] + min(h[j], h[i])`. Split by whether `h[j] <= h[i]` or `h[j] > h[i]`.  
   **Why correct:** `min` collapses to one endpoint depending only on relative height.

3. **Coordinate-compress heights.**  
   Map each distinct height to rank `1..m`. This makes prefix/suffix range-max queries feasible in `O(log m)`.  
   **Invariant:** height ordering is preserved exactly; only magnitudes are remapped.

4. **Maintain two max-query structures while sweeping left to right.**  
   - Structure A stores `prev[j] + h[j]` at rank `rank(h[j])`; query prefix `<= rank(h[i])`.  
   - Structure B stores `prev[j]` at rank `rank(h[j])`; query suffix `> rank(h[i])`, then add `h[i]`.  
   This is the core DP + monotone-order decomposition.

5. **Compute each `cur[i]`.**  
   `cur[i] = max(prefixBest, suffixBest + h[i])`, using only already-seen indices `j < i`. If no predecessor exists, value is negative infinity.  
   **Invariant:** transitions never violate ordering because updates happen after queries for index `i`.

6. **Roll arrays across selection count.**  
   Repeat for `t = 2..k`, swapping `prev` and `cur`. The answer is `max(prev[i])` after the final round.  
   **Why this abstraction fits:** the problem is dynamic programming over ordered subsequences, and the monotonic-height split turns a dense predecessor graph into logarithmic range maxima.

## 📊 Worked Example
Use `heights = [5,1,4,6,3]`, `k = 3`.

For `t = 1`: `prev = [0,0,0,0,0]`

For `t = 2`, each value is the best single pair ending at `i`:

| i | h[i] | best predecessor contribution | cur[i] |
|---|------|-------------------------------|--------|
| 0 | 5 | none | -inf |
| 1 | 1 | `0 + min(5,1)` | 1 |
| 2 | 4 | max of `(0+4 from h=1)`, `(0+4 from h=5?) no, min=4` | 4 |
| 3 | 6 | best prior `min` is with tower 0 or 2 | 5 |
| 4 | 3 | best prior `min` is 3 | 3 |

So after `t = 2`: `prev = [-inf,1,4,5,3]`.

For `t = 3`:

- `i = 2 (h=4)`: from `j=1`, score `1 + min(1,4) = 2`
- `i = 3 (h=6)`: best is from `j=2`, score `4 + min(4,6) = 8`
- `i = 4 (h=3)`: best is from `j=3`, score `5 + min(6,3) = 8`

Maximum is `8`, achieved by indices `[0,2,3]` with heights `[5,4,6]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(k * n * log n)` after coordinate compression. Each of the `k-1` DP rounds performs one left-to-right sweep, and each index does a constant number of range-max queries and point updates. At `n = 1e6`, this is still expensive but structurally viable; at `n = 1e9`, no exact in-memory approach is realistic without distribution or approximation.

### Space Complexity
`O(n + m)` where `n` is for the rolling DP arrays and `m <= n` is for compressed-height query structures. In practice this is `O(n)`. It can be reduced slightly with tighter tree implementations, but not below linear without sacrificing exactness or update/query efficiency.

## 💡 Key Takeaways
- If the transition is `max over j < i` with a `min(a[j], a[i])` term, look for a case split on value ordering rather than scanning all predecessors.
- When `n` is huge and `k` is small-to-moderate, ordered-subsequence DP is often viable only after compressing transitions into range queries.
- Base case is easy to get wrong: selecting one tower yields score `0`, not its height, because no adjacent pair exists yet.
- Query-before-update matters: updating structures with index `i` before computing `cur[i]` incorrectly allows self-transitions.
- The transferable design insight is to separate combinatorial state progression from value-domain indexing; once transitions are expressed as monotone range aggregates, scale follows.

## 🚀 Variations & Further Practice
- **Add reconstruction of the chosen indices.** Same DP, but now store predecessor pointers under compressed transitions; the harder part is preserving argmax information through range-max structures.
- **Replace `min` with `max` or `|a-b|`.** The transition decomposition changes completely; `max` remains structured, while absolute difference usually needs different envelopes or Li Chao/convex-hull style machinery.
- **Allow up to `k` towers instead of exactly `k`.** Simpler objective surface, but now termination and state dominance matter because shorter selections may dominate longer partial states.