# Count Equivalent Badge Histories Under ID Compression

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Array, Canonical Representation

---

## 🗂 Problem Overview
Given `n` badge histories, each an integer array, count how many unordered pairs are equivalent after replacing each badge ID by the index of its first appearance within that history. Histories may have different lengths, and values can be large or negative. The challenge is to compare structural repetition patterns, not raw values, and to do it in near-linear time over at most `2 * 10^5` total scanned IDs.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need value-independent sequence identity: compiler symbol normalization, log-template mining, query-shape deduplication, event-stream fingerprinting, and search/session behavior analysis. The raw identifiers differ across tenants, shards, or time windows, but the structural pattern is what matters. Without canonicalization, teams fall back to quadratic pairwise comparison or brittle range-based encoding assumptions that collapse under skewed IDs and multi-tenant data. A canonical hashable form enables streaming aggregation, shard-local counting, cacheable fingerprints, and exact grouping at production scale without depending on the original value domain.

## 🔍 Problem Statement
You are given `n` badge histories, where each history is an integer array and histories may have different lengths. Two histories are equivalent if, after compressing badge IDs by first appearance order, they produce the same sequence. Example: `[42,99,42,17] -> [0,1,0,2]` and `[7,3,7,8] -> [0,1,0,2]`, so they match. But `[5,5,8] -> [0,0,1]` does not match `[5,8,5] -> [0,1,0]`.

Return the number of unordered equivalent pairs as a 64-bit integer.

Constraints:
- `1 <= n <= 2 * 10^5`
- `1 <= total scanned IDs across all histories <= 2 * 10^5`
- `-10^9 <= badgeID <= 10^9`
- `1 <= length of each history`

Example 1:
- Input: `[[42,99,42,17],[7,3,7,8],[5,5,8],[8,8,1],[10,11,10,12]]`
- Output: `4`

Example 2:
- Input: `[[1,2,1,2],[4,4,5,5],[9],[3,1,3],[8,6,8,7]]`
- Correct output for this exact list: `0`

The key constraint is that badge IDs are arbitrary integers, so only structure-preserving canonicalization works.

## 🪜 How to Solve This
1. Read the equivalence rule → raw values do not matter; only the reuse pattern does.

2. If values do not matter, each history should be transformed into a canonical form independent of actual badge IDs.

3. How do we build that form? Scan left to right and assign each new badge ID the next unused compressed ID: first unseen value gets `0`, next unseen gets `1`, and so on.

4. That produces a normalized sequence such as `[0,1,0,2]`. Two histories are equivalent iff these normalized sequences are identical.

5. Once every history has a canonical representation, the problem becomes pure grouping: count how many times each pattern appears.

6. Grouping identical keys immediately suggests a hash map from canonical pattern → frequency.

7. For each history, either:
   - increment its pattern count and add the previous frequency to the answer online, or
   - count frequencies first, then sum `f * (f - 1) / 2`.

8. This avoids pairwise comparison entirely. Since total input size is bounded by `2 * 10^5`, one pass to normalize and hash each history is enough.

The mental model is: **canonicalize local structure, then count duplicates globally**.

## 🧩 Algorithm Walkthrough
1. **Use canonical representation + hashing.**  
   The right abstraction is **hash-based grouping by canonical form**. We are not comparing numeric magnitudes or ranges; we are comparing structural equality under renaming. Canonicalization converts an equivalence relation into exact key equality.

2. **Process each history independently.**  
   For one history, maintain a map `badgeID -> compressedID` and a counter `nextID`. This map is local to the history because first-appearance order is defined per sequence, not globally.

3. **Build the compressed pattern left to right.**  
   For each badge:
   - if unseen in this history, assign `compressedID = nextID` and increment `nextID`;
   - otherwise reuse the previously assigned compressed ID.  
   Invariant: after processing position `i`, the generated prefix is the unique first-appearance encoding of the original prefix.

4. **Materialize a hashable key.**  
   Store the compressed sequence as a vector/tuple/string-like key. Histories of different lengths naturally differ because the key length is part of equality.

5. **Count equivalent pairs.**  
   Maintain `freq[key]`. Before incrementing, add `freq[key]` to the answer. This works because each prior history with the same key forms exactly one new unordered pair with the current history.  
   Invariant: after processing `k` histories, `answer` equals the number of equivalent unordered pairs among those `k`.

6. **Return a 64-bit result.**  
   Pair counts can exceed 32-bit range when many histories share one pattern, so the accumulator must be `int64`/`long long`.

This is correct because canonicalization is unique and complete: two histories map to the same key iff they have the same repetition structure.

## 📊 Worked Example
Take `[[42,99,42,17],[7,3,7,8],[5,5,8],[8,8,1],[10,11,10,12]]`.

| History | Local ID map as scanned | Canonical pattern | Previous freq | Pairs added |
|---|---|---:|---:|---:|
| `[42,99,42,17]` | `42→0, 99→1, 17→2` | `[0,1,0,2]` | 0 | 0 |
| `[7,3,7,8]` | `7→0, 3→1, 8→2` | `[0,1,0,2]` | 1 | 1 |
| `[5,5,8]` | `5→0, 8→1` | `[0,0,1]` | 0 | 0 |
| `[8,8,1]` | `8→0, 1→1` | `[0,0,1]` | 1 | 1 |
| `[10,11,10,12]` | `10→0, 11→1, 12→2` | `[0,1,0,2]` | 2 | 2 |

Running total: `0 + 1 + 0 + 1 + 2 = 4`.

The key observation is that actual badge IDs differ, but first-occurrence structure matches. Three histories share `[0,1,0,2]`, contributing `3 choose 2 = 3` pairs; two histories share `[0,0,1]`, contributing `1` pair.

## ⏱ Complexity Analysis
### Time Complexity
`O(T)` average time, where `T` is the total number of scanned IDs across all histories. Each badge ID is inserted/looked up once in a per-history hash map, and each compressed token is appended once. At `10^6` elements this is routine; at `10^9`, even linear work becomes operationally expensive, so the input bound is what makes this feasible.

### Space Complexity
`O(T)` in the worst case. Space is owned by the stored canonical keys across all distinct histories plus the temporary per-history map. You can reduce retained key memory with rolling hashes, but that trades exact structural storage for collision-management complexity.

## 💡 Key Takeaways
- If the problem says “equivalent up to renaming” or “values don’t matter, only repetition structure,” think canonical representation before anything else.
- If the goal is counting equivalent pairs across many objects, convert each object to a stable key and group with a hash map instead of comparing pairs.
- The badge-to-compressed-ID map must be reset for every history; a global map silently corrupts the equivalence definition.
- Length is part of the pattern: `[0,1,0]` and `[0,1,0,2]` are different even though one is a prefix of the other.
- In production, canonicalization is often the boundary between expensive semantic comparison and cheap exact aggregation.

## 🚀 Variations & Further Practice
- Count equivalent **subarrays** within one long event stream; harder because canonicalization must work over sliding windows, not isolated histories.
- Support **online updates** where histories arrive continuously and queries ask for current pair counts; the twist is maintaining exact counts incrementally under streaming constraints.
- Treat two histories as equivalent under **first appearance plus reversal**; harder because each history now has multiple valid canonical forms and you must choose a minimal representative.