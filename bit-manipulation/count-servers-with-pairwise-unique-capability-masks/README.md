# Count Servers With Pairwise-Unique Capability Masks

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Bitmask, Counting, SOS DP

---

## 🗂 Problem Overview
Given up to 200,000 server capability masks, count how many unordered pairs of distinct servers have no overlapping enabled bit, i.e. `(masks[i] & masks[j]) == 0`. The result must be returned as a 64-bit integer. The challenge is that naive pairwise comparison is `O(n^2)`, which is too expensive at this scale, while the mask universe is small: only 20 possible bits, so `2^20` states.

## 🌍 Engineering Impact
This pattern shows up anywhere entities are represented as compact feature flags and compatibility means disjointness: scheduler placement constraints, CPU instruction capability matching, ad-target exclusion sets, search ranking feature gating, and streaming pipeline operator compatibility. At production scale, `O(n^2)` pair checks collapse quickly under bursty workloads or large fleet snapshots. The bitmask-domain view changes the architecture: instead of comparing entities, you aggregate by state and answer compatibility queries over the state space. That enables predictable latency, bounded memory, and batch-friendly implementations that remain viable as cardinality grows while feature width stays fixed.

## 🔍 Problem Statement
You are given an integer array `masks` where each value is a 20-bit capability mask for one server. Count the number of unordered pairs `(i, j)` with `i < j` such that the two servers share no enabled capability bit:

`(masks[i] & masks[j]) == 0`

Constraints:

- `1 <= masks.length <= 200000`
- `0 <= masks[i] < 2^20`
- Bits range from `0` to `19`
- Duplicate mask values are allowed
- Return the answer as a 64-bit integer

Examples:

- `masks = [1, 2, 3, 4]` → `4`
- `masks = [0, 1, 1, 2, 6]` → `6`

Important edge cases:

- Mask `0` is compatible with every mask, including another `0`
- Duplicate masks contribute multiplicity to the count
- The small mask width, not the array length, is the key constraint driving the algorithmic choice

## 🪜 How to Solve This
1. Start with the condition: two masks are valid together when their bitwise AND is zero. That immediately suggests this is not about ordering or adjacency; it is about mask relationships.

2. A brute-force scan checks every pair of servers. With `n = 200000`, that is about `2e10` comparisons in the worst case, so it is dead on arrival.

3. Notice the asymmetry in the constraints: `n` is large, but masks live in a tiny fixed universe of size `2^20`. That means we should count how many servers have each exact mask, then reason over mask frequencies instead of raw rows.

4. For a given mask `m`, any compatible partner must be a submask of `~m` within 20 bits. So the real subproblem is: for every mask, how many observed masks are submasks of some target mask?

5. That is the classic use case for **SOS DP**: precompute, for every bitmask `s`, the total frequency of all submasks of `s`.

6. Once that table exists, each server with mask `m` can query how many servers are compatible via `submaskCount[fullMask ^ m]`. Sum those counts, subtract self-pair artifacts, then divide by two because each unordered pair is seen twice.

## 🧩 Algorithm Walkthrough
1. **Count exact mask frequencies.**  
   Build an array `freq` of size `2^20`, where `freq[x]` is the number of servers whose capability mask equals `x`.  
   **Why correct:** all later counting depends only on how many times each mask occurs, not on element positions.  
   **Invariant:** after this step, total servers equal `sum(freq)`.

2. **Initialize the SOS DP table.**  
   Copy `freq` into `dp`, where initially `dp[s] = freq[s]`.  
   **Why:** we want `dp[s]` to become the number of observed masks that are submasks of `s`. Starting from exact counts is the base case.  
   **Invariant:** before transitions, `dp[s]` counts only the exact mask `s`.

3. **Run Sum Over Subsets DP.**  
   For each bit `b` from `0` to `19`, and each mask `s`, if bit `b` is set in `s`, add `dp[s ^ (1 << b)]` into `dp[s]`.  
   **Pattern:** **SOS DP / subset DP**.  
   **Why this abstraction fits:** compatibility reduces to counting submasks of a complement mask, and SOS DP computes all such submask sums in `O(B * 2^B)` instead of enumerating submasks per query.  
   **Invariant:** after processing the first `k` bits, `dp[s]` contains frequencies of all submasks differing only in those `k` processed dimensions.

4. **Query compatible counts per server.**  
   Let `FULL = (1 << 20) - 1`. For each original mask `m`, the masks compatible with it are exactly the submasks of `FULL ^ m`. Add `dp[FULL ^ m]` to the answer accumulator.  
   **Why correct:** any bit set in `m` must be absent from the partner, so the partner must lie entirely inside the complement.

5. **Remove double counting and self handling.**  
   Every valid unordered pair is counted twice: once from each endpoint. Self-pairs only appear for mask `0`, because `0 & 0 == 0`; those are not allowed since indices must be distinct. The clean way is to sum over all servers, subtract `n` contributions only for masks that counted themselves? Better: subtract `1` per server from its compatible count when `m == 0`, then divide the final total by `2`.  
   Equivalent implementation: accumulate `dp[FULL ^ m] - (m == 0 ? 1 : 0)` for each server, then return `ans / 2`.  
   **Invariant:** final total equals the number of unordered distinct compatible pairs.

## 📊 Worked Example
Take `masks = [0, 1, 1, 2, 6]`.

| Server mask `m` | Complement `FULL ^ m` | `dp[complement]` | Self-adjust | Contribution |
|---|---:|---:|---:|---:|
| 0 | all 20 bits set | 5 | -1 | 4 |
| 1 | all bits except bit 0 | 2 | 0 | 2 |
| 1 | all bits except bit 0 | 2 | 0 | 2 |
| 2 | all bits except bit 1 | 3 | 0 | 3 |
| 6 | all bits except bits 1,2 | 1 | 0 | 1 |

Explanation of the `dp` values:

- Compatible with `0`: every mask, so `5`
- Compatible with `1`: only masks with bit `0` unset → `{0, 2}` → `2`
- Compatible with `2`: masks with bit `1` unset → `{0, 1, 1}` → `3`
- Compatible with `6` (`110`): masks using neither bit `1` nor `2` → `{0}` → `1`

Total adjusted sum = `4 + 2 + 2 + 3 + 1 = 12`  
Each unordered pair appears twice, so answer = `12 / 2 = 6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + B * 2^B)`, where `B = 20`. That is `O(n + 20 * 2^20)`, dominated by the SOS DP pass over the mask space. This is practical because `2^20` is about one million. At `10^6` input scale with fixed `B`, runtime remains bounded by the mask universe; at `10^9`, only the initial frequency accumulation becomes infeasible.

### Space Complexity
`O(2^B)` for the frequency and DP arrays, or about one million entries each for `B = 20`. This is the core memory cost. You can reuse one array in place after frequency initialization, reducing constants, but not the asymptotic bound without giving up the precomputed submask-sum capability.

## 💡 Key Takeaways
- If `n` is large but the feature space is a small fixed-width bitmask, stop thinking in terms of element pairs and start thinking in terms of state aggregation.
- “Count masks compatible with this mask” is a strong signal for subset/superset transforms, especially SOS DP over `2^B` states.
- The complement must be taken within exactly 20 bits: use `FULL ^ m`, not language-level bitwise NOT, which introduces high bits and breaks indexing.
- Self-pair correction matters only for mask `0`; if you forget it, the result is off by `count(0) / 2` after division.
- The production lesson is to trade row-wise computation for domain-wise precomputation when the state space is bounded and query volume is high.

## 🚀 Variations & Further Practice
- Count ordered pairs or pairs under a threshold on overlapping bits, e.g. `(a & b)` has at most `k` set bits. The twist is that exact disjointness becomes a combinatorial overlap-count problem.
- Support online updates: insert/delete masks and answer compatibility queries in real time. The harder part is replacing one-shot SOS DP with a dynamic data structure or batched rebuild strategy.
- Generalize from pair counting to maximum compatible subset or matching under disjointness constraints. The twist is moving from counting to optimization over an intersection graph.