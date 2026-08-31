# Find Players With Reordered Card Histories

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Arrays, Frequency Counting

---

## 🗂 Problem Overview
Given a list of player card histories, return every player ID whose history is equivalent to at least one other after ignoring draw order but preserving card frequencies. Two histories match only if they represent the same multiset of card IDs. The output must be sorted by player ID. The challenge is scale: up to 100,000 players, so direct pairwise comparison is too expensive and requires a canonical, hashable representation of each history.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect equivalence under reordering: deduplicating event batches in streaming pipelines, grouping semantically identical compiler symbol usages, identifying repeated request payloads in API gateways, or collapsing search-query feature bags in ranking systems. At small scale, pairwise comparison is tolerable; at platform scale, it explodes into quadratic work and cache-unfriendly scans. Canonicalization plus hashing turns “compare everything to everything” into “normalize once, group once.” That shift is what enables predictable latency, bounded memory growth, and horizontally scalable grouping logic in distributed data-processing paths.

## 🔍 Problem Statement
You are given `histories`, where `histories[i]` is the ordered list of card IDs drawn by player `i`. Two players are reordered-equivalent if their histories contain exactly the same card IDs with the same frequencies, regardless of order. Histories may have different lengths, including zero.

Return all player IDs that belong to at least one reordered-equivalent group, sorted in ascending order.

Constraints:
- `1 <= histories.length <= 100000`
- `0 <= histories[i].length <= 100000`
- `0 <= card IDs <= 1000000000`
- Total number of cards across all histories is at most `200000`

Examples:

- `[[4,9,4,2],[2,4,9,4],[7,7,1],[1,7,7],[3,5]] -> [0,1,2,3]`
- `[[1,2,3],[3,2,1,1],[],[5,5],[]] -> [2,4]`

The key constraint is the input size: `O(n^2)` history comparison is not viable, so the solution must build a canonical signature per history and group by hash.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** care about order, but we **do** care about multiplicity. That means this is not sequence equality; it is multiset equality.

2. Multiset equality suggests a **frequency profile**. For each history, count how many times each card ID appears.

3. Now ask the real question: how do we compare many frequency profiles efficiently? We need a **canonical signature** so equivalent histories produce the same key.

4. Since card IDs are arbitrary and sparse, use a hash map to count frequencies, then convert that map into a deterministic representation, typically a sorted list of `(cardID, count)` pairs.

5. Use another hash map: `signature -> list of player IDs`. As each player is processed, append its ID to the bucket for that signature.

6. After one pass, any bucket with size greater than 1 represents a reordered-equivalent group. Collect all IDs from those buckets.

7. The final output must be sorted. If players are appended in input order and buckets are scanned arbitrarily, sort the result once at the end.

This avoids nested comparisons entirely and scales with total input size, not with the square of player count.

## 🧩 Algorithm Walkthrough
1. **Build a per-history frequency map**  
   For each player `i`, scan `histories[i]` and count occurrences of each card ID in a local hash map. This captures exactly the information needed for multiset equality and discards irrelevant ordering.  
   **Invariant:** after this step, the map represents the exact card-frequency profile of player `i`.

2. **Canonicalize the frequency map into a signature**  
   Convert the map into a list of `(cardID, count)` pairs and sort by `cardID`. Serialize that sorted list into a stable key, such as tuples or a delimited string. Sorting is required because hash map iteration order is not deterministic.  
   **Invariant:** two histories produce identical signatures if and only if they have identical multisets.

3. **Group players by signature using hashing**  
   Maintain a global hash map from `signature` to the list of player IDs sharing it. Append `i` to the corresponding bucket. This is the core **Hashing + Canonical Representation** pattern: normalize first, then group in constant expected time per item.  
   **Invariant:** every bucket contains exactly the players with the same frequency profile.

4. **Extract only duplicated groups**  
   Iterate through all buckets. If a bucket size is at least 2, every player in it belongs to a reordered-equivalent group, so add all IDs to the answer.

5. **Sort the final answer**  
   Because the problem requires ascending player IDs, sort the collected IDs before returning. This is correct regardless of hash map bucket traversal order.

This abstraction is right because the problem is fundamentally about equivalence classes under permutation, and hashing canonical forms is the standard way to materialize those classes efficiently.

## 📊 Worked Example
Consider `histories = [[4,9,4,2],[2,4,9,4],[7,7,1],[1,7,7],[3,5]]`.

| Player ID | History        | Frequency Map         | Canonical Signature        | Group State |
|-----------|----------------|-----------------------|----------------------------|-------------|
| 0         | [4,9,4,2]      | {4:2, 9:1, 2:1}       | [(2,1),(4,2),(9,1)]       | sigA -> [0] |
| 1         | [2,4,9,4]      | {2:1, 4:2, 9:1}       | [(2,1),(4,2),(9,1)]       | sigA -> [0,1] |
| 2         | [7,7,1]        | {7:2, 1:1}            | [(1,1),(7,2)]             | sigB -> [2] |
| 3         | [1,7,7]        | {1:1, 7:2}            | [(1,1),(7,2)]             | sigB -> [2,3] |
| 4         | [3,5]          | {3:1, 5:1}            | [(3,1),(5,1)]             | sigC -> [4] |

Buckets with size greater than 1 are `sigA` and `sigB`, so collect `[0,1,2,3]`. Sorting keeps the output in ascending order.

## ⏱ Complexity Analysis
### Time Complexity
Let `T` be the total number of cards across all histories, and let `u_i` be the number of distinct card IDs in history `i`. Building frequency maps costs `O(T)`. Canonicalizing each history costs `O(u_i log u_i)` due to sorting distinct IDs. Total time is `O(T + Σ u_i log u_i + r log r)` for final result sort. This remains practical at million-scale inputs; quadratic comparison does not.

### Space Complexity
Space is `O(T)` in the worst case, dominated by per-history frequency maps during processing, canonical signatures, and the grouping hash map. It can be reduced slightly by streaming one history at a time and storing only signatures plus grouped IDs, trading some implementation simplicity for lower peak memory.

## 💡 Key Takeaways
• If order is irrelevant but multiplicity matters, think “multiset equality,” not sequence comparison.  
• If the task says “find all items that belong to some equivalent group,” that is a strong signal for canonicalization plus hash-based grouping.  
• Empty histories are valid and all share the same signature; they must group together correctly.  
• Do not use raw hash map iteration as the signature source; without sorting keys first, equivalent histories may serialize differently.  
• The production-grade insight is to normalize data into deterministic identities before grouping, deduplicating, or caching at scale.

## 🚀 Variations & Further Practice
- Return the grouped player IDs as clusters instead of a flat sorted list; the twist is preserving deterministic group ordering while still using hash-based grouping efficiently.
- Support streaming updates where histories arrive incrementally; the harder part is maintaining signatures online without rebuilding full frequency profiles each time.
- Find near-equivalent histories where one card count may differ by at most 1; the twist is moving from exact hashing to approximate matching or neighborhood search.