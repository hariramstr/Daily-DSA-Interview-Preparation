# Minimum Pair Merges to Clear Duplicate Bit Flags

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Union Find, Graph Connectivity

---

## 🗂 Problem Overview
Given an array of integer bitmasks, repeatedly merge any two masks that share at least one set bit, replacing them with their bitwise OR. The goal is to end with a set of masks that are pairwise bit-disjoint. Return the minimum number of merges required. The non-trivial part is that overlap is transitive through merge sequences, so local pair choices are misleading; the answer depends on connected conflict groups, not greedy pair elimination.

## 🌍 Engineering Impact
This pattern shows up anywhere bitsets encode capabilities, ownership, or resource claims: feature-flag rollups, compiler liveness/interference analysis, search-index shard capability maps, network policy aggregation, and streaming pipeline operator compatibility. At scale, pairwise conflict checks are quadratic and operationally useless. The right abstraction is connectivity over shared attributes, not repeated local reconciliation. That shift enables linear-ish processing, predictable behavior under large fan-in, and clean reasoning about collapse boundaries: which entities must coalesce, which can remain independent, and how to model transitive conflict without simulating every merge sequence.

## 🔍 Problem Statement
You are given an integer array `flags` where each value is a device configuration mask. A `1` bit means that capability is enabled. You may repeatedly choose two different indices `i` and `j` such that `flags[i] & flags[j] != 0`, remove both values, and insert `flags[i] | flags[j]`.

Return the minimum number of such operations needed so that the final array is bit-disjoint: for every remaining pair `a`, `b`, `(a & b) == 0`.

Constraints:
- `1 <= flags.length <= 100000`
- `0 <= flags[i] <= 10^9`
- Answer fits in 32-bit signed integer

Examples:
- `flags = [3, 5, 8]` → `1`
- `flags = [10, 3, 12, 1]` → `2`

Key observation: direct overlap is not enough; masks can belong to the same conflict group through shared bits indirectly. With `n = 1e5`, explicit graph construction over all pairs is too expensive.

## 🪜 How to Solve This
1. Read the operation carefully → a merge is only allowed when two masks overlap on at least one bit.

2. Ask what the final state means → remaining masks must be pairwise disjoint, so any masks that are connected by overlap chains cannot stay in separate final groups if those chains force shared-bit reachability.

3. Reframe the problem as connectivity → each mask is a node; connect two nodes if they share a bit. A sequence of valid merges can collapse any connected component into one mask.

4. Count what a component costs → a connected component with `k` masks needs exactly `k - 1` merges to become one mask.

5. Avoid `O(n^2)` edge building → masks only have up to 30 relevant bits (`flags[i] <= 1e9`), so union masks by shared bit positions instead of comparing every pair.

6. Use Union-Find → for each bit, remember the first index that had it set; every later index with that bit unions into the same component.

7. Sum across components → minimum merges = total nonzero-mask elements minus number of connected components among them. Zero masks are already disjoint and never merge.

## 🧩 Algorithm Walkthrough
1. **Separate the special case `0`.**  
   A zero mask has no set bits, so it cannot overlap with anything and is already disjoint from every value. It never participates in a merge. This preserves the invariant that all work is restricted to masks with at least one bit set.

2. **Model overlap using Union-Find (Disjoint Set Union).**  
   Treat each nonzero mask as a node. Two nodes belong in the same component if they share any set bit, directly or transitively. Union-Find is the right abstraction because we need dynamic component formation, not explicit traversal over a dense graph.

3. **Track ownership per bit position.**  
   Since `flags[i] <= 1e9`, only about 30 bit positions matter. Maintain `firstSeen[bit]`, initialized to `-1`. For each mask, inspect its set bits. If `firstSeen[bit]` is unset, store the current index; otherwise union the current index with the stored one.  
   Invariant: all masks containing a given bit end up in one DSU component.

4. **Rely on transitive closure via repeated unions.**  
   If mask A shares bit 1 with B, and B shares bit 4 with C, then DSU merges all three even if A and C never directly overlap. This matches the problem’s merge semantics: connected conflict groups can be collapsed through valid intermediate merges.

5. **Count connected components among nonzero masks.**  
   Let `m` be the number of nonzero masks and `c` the number of distinct DSU roots among them. Each component can be reduced to one final mask in exactly `size - 1` merges, so total merges are `m - c`.

6. **Why this is minimal.**  
   You cannot split a connected overlap component into multiple final masks if transitive overlap forces them into the same conflict group under valid mergeability. Collapsing each connected component independently is both sufficient and optimal.

## 📊 Worked Example
Take `flags = [10, 3, 12, 1]`.

| Index | Value | Bits Set   | Union Actions              | Components After Step |
|------:|------:|------------|----------------------------|-----------------------|
| 0     | 10    | 1, 3       | `firstSeen[1]=0`, `3=0`    | `{0}`                 |
| 1     | 3     | 0, 1       | `firstSeen[0]=1`, union(1,0) | `{0,1}`             |
| 2     | 12    | 2, 3       | `firstSeen[2]=2`, union(2,0) | `{0,1,2}`           |
| 3     | 1     | 0          | union(3,1)                 | `{0,1,2,3}`          |

Trace:
1. `10` establishes representatives for bits 1 and 3.  
2. `3` shares bit 1 with `10`, so they join.  
3. `12` shares bit 3 with `10`, so it joins the same component.  
4. `1` shares bit 0 with `3`, so it also joins.

There are `4` nonzero masks and `1` connected component, so merges = `4 - 1 = 3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * B * α(n))`, where `B` is the number of relevant bit positions, effectively about 30 here. For each element, we scan its bits and perform near-constant-time DSU operations. This scales comfortably to `10^6` elements; at `10^9`, input size itself is infeasible regardless of algorithm.

### Space Complexity
`O(n + B)`. The DSU parent/rank arrays dominate space, while the per-bit lookup table is constant-sized in practice. You could reduce DSU metadata slightly, but not asymptotically, without giving up efficient component merging and root counting.

## 💡 Key Takeaways
- If the operation allows repeated merges through shared attributes, think connected components before thinking greedy pair selection.
- When pairwise overlap defines edges but the attribute universe is small, union by attribute buckets instead of building all pair edges.
- Do not include `0` masks in DSU merge counting; they are isolated and cannot participate in any valid operation.
- Count components only among nonzero elements actually represented in the DSU, or you will undercount required merges.
- In production systems, collapsing transitive conflict domains is often the real problem; local reconciliation logic hides the actual connectivity structure and fails at scale.

## 🚀 Variations & Further Practice
- Require the actual merge sequence, not just the count. The twist is reconstructing valid pair operations from DSU/group structure while preserving overlap at each step.
- Allow dynamic insert/delete of masks with online queries for current minimum merges. The harder part is maintaining connectivity under deletions, where plain Union-Find no longer suffices.
- Replace bit overlap with shared prime factors or shared labels from a larger universe. The conceptual shift is sparse attribute indexing and component construction beyond fixed-width bitsets.