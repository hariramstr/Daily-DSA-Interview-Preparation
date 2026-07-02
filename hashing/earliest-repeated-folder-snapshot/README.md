# Earliest Repeated Folder Snapshot

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Array, Set Normalization

---

## 🗂 Problem Overview
You are given folder snapshots in time order, where each snapshot is an array of distinct file IDs and element order does not matter. Two snapshots match if they contain the same IDs as a set. Return the earliest repeated pair `[i, j]` with `i < j`, minimizing `j` first and then `i`. The challenge is avoiding pairwise comparison across up to `100000` snapshots, so each snapshot must be normalized into a canonical hashable form efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems compare unordered state: cache-key derivation for build artifacts, compiler dependency fingerprints, deduplicating object manifests in blob stores, stream-processing state compaction, and search/indexing pipelines that canonicalize token sets. At scale, failing to normalize equivalent states means duplicate work, missed cache hits, and unstable identity semantics across services. Hash-based canonicalization turns expensive structural equality checks into constant-time lookups, enabling online detection of repeated state, incremental processing, and deterministic behavior across distributed components that may emit logically identical data in different orders.

## 🔍 Problem Statement
Given `snapshots`, a chronological list of folder states, return the earliest pair of indices `[i, j]` such that `i < j` and `snapshots[i]` and `snapshots[j]` contain exactly the same file IDs, regardless of order. Each snapshot contains no duplicate IDs internally. If multiple repeated states exist, choose the pair with the smallest `j`; if multiple pairs share that `j`, choose the smallest `i`. If no repeated state exists, return `[-1, -1]`.

Constraints:

- `1 <= snapshots.length <= 100000`
- `0 <= snapshots[i].length <= 1000`
- `0 <= fileID <= 1000000000`
- Total number of file IDs across all snapshots is at most `200000`

Examples:

- `[[5,1,9],[3,4],[9,5,1],[4,3],[7]] -> [0,2]`
- `[[],[8],[2,6],[6,2],[]] -> [2,3]`

The key constraint is input size: quadratic snapshot comparison is not viable, so each snapshot must be converted into a canonical representation and checked via hashing.

## 🪜 How to Solve This
1. Read the equality rule → order inside each snapshot is irrelevant, so raw arrays are not directly comparable.

2. If order does not matter, normalize each snapshot into something order-independent. The simplest canonical form is the sorted snapshot.

3. Once a snapshot is normalized, the problem becomes: “have I seen this exact canonical state before?” That is a textbook hash map lookup.

4. Scan snapshots left to right in chronological order. For each snapshot:
   - sort it,
   - convert it into a hashable key,
   - check whether that key already exists in the map.

5. If the key already exists, return `[firstSeenIndex, currentIndex]` immediately. Because we process in increasing `j`, the first repeat encountered automatically has the smallest possible `j`.

6. Store only the first index for each normalized state. That preserves the smallest `i` for any later tie on the same repeated state.

7. No repeat found after one pass → return `[-1, -1]`.

This is the standard **hashing + canonicalization** pattern: normalize equivalent structures, then use a map for constant-time identity checks.

## 🧩 Algorithm Walkthrough
1. **Choose the abstraction: Hashing with canonicalization.**  
   The snapshots are arrays, but equality is set-based. Hashing only works if logically equal snapshots produce identical keys, so we first canonicalize each snapshot by sorting its file IDs.

2. **Iterate snapshots in chronological order.**  
   Let `j` be the current index. Processing left to right matters because the problem prioritizes the smallest repeated `j`. The first duplicate we detect is therefore globally optimal on the second index.

3. **Normalize the current snapshot.**  
   Sort the file IDs and build a delimiter-safe key, such as a tuple or `"1#5#9"`. This guarantees that `[5,1,9]` and `[9,5,1]` map to the same key, while different sets do not collide at the representation layer.

4. **Check the hash map.**  
   Maintain `firstSeen[key] = earliest index where this normalized state appeared`.  
   - If `key` exists, return `[firstSeen[key], j]`.  
   - If not, store `firstSeen[key] = j`.

5. **Maintain the invariant.**  
   After processing index `j`, the map contains every unique normalized snapshot from indices `0..j`, each mapped to its earliest occurrence. That invariant ensures correctness for both tie-breakers: earliest `j` comes from scan order, earliest `i` comes from only storing the first occurrence.

6. **Finish the scan.**  
   If no duplicate key appears, no repeated folder state exists, so return `[-1, -1]`.

This is the right pattern because the expensive part is equivalence under permutation, not searching itself. Canonicalization reduces structural equality to exact key equality.

## 📊 Worked Example
Example: `snapshots = [[5,1,9],[3,4],[9,5,1],[4,3],[7]]`

| j | snapshot   | normalized key | firstSeen before | action |
|---|------------|----------------|------------------|--------|
| 0 | [5,1,9]    | 1#5#9          | absent           | store `1#5#9 -> 0` |
| 1 | [3,4]      | 3#4            | absent           | store `3#4 -> 1` |
| 2 | [9,5,1]    | 1#5#9          | present at `0`   | return `[0,2]` |

Trace:

1. At `j = 0`, normalize `[5,1,9]` to `1#5#9`; map becomes `{1#5#9: 0}`.
2. At `j = 1`, normalize `[3,4]` to `3#4`; map becomes `{1#5#9: 0, 3#4: 1}`.
3. At `j = 2`, normalize `[9,5,1]` to `1#5#9`; key already exists at index `0`.
4. Return `[0,2]` immediately.  
   We do not continue, because any later repeat would have larger `j` and therefore lose by the problem’s ordering rule.

## ⏱ Complexity Analysis

### Time Complexity
Let `m` be the number of snapshots and `K` the total number of file IDs across all snapshots. Sorting each snapshot of length `k` costs `O(k log k)`, so total time is `O(sum(k log k))`, bounded by the input distribution. Hash lookups are `O(1)` average. At million-scale inputs this remains practical; at billion-scale, normalization cost dominates and streaming or stronger fingerprints may be needed.

### Space Complexity
The hash map stores one canonical key per distinct snapshot state, so space is `O(U + K')`, where `U` is the number of unique snapshots and `K'` is the total size of stored normalized representations. Space can be reduced with compact hashes, at the cost of collision handling complexity.

## 💡 Key Takeaways
- If equality ignores order, that is a strong signal to normalize first and hash second.
- When the problem asks for the earliest repeated occurrence in a sequence, a left-to-right first-seen map is usually the right shape.
- Return immediately on the first duplicate encountered; continuing can only worsen `j`.
- Store the first index for each normalized state, not the latest, or you will break the smallest-`i` tie rule.
- In production systems, canonical representations are often more important than the hash table itself; stable identity semantics unlock caching, deduplication, and deterministic behavior.

## 🚀 Variations & Further Practice
- Detect repeated snapshots when each snapshot may contain duplicate file IDs internally; the twist is multiset equality rather than set equality, so normalization must preserve counts.
- Return all repeated snapshot groups instead of the earliest pair; the harder part is balancing memory usage with complete grouping output.
- Support online updates where files are added or removed between snapshots; the twist is maintaining an incremental fingerprint instead of re-sorting each full state.