# Count Matching License Plates by Character Multiset

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Counting, String

---

## 🗂 Problem Overview
Given an array of uppercase alphanumeric license plates, count how many unordered index pairs represent the same character multiset: identical characters with identical frequencies, ignoring order. The output is a single integer pair count. The challenge is scale: with up to 100,000 plates, comparing every pair is too expensive, so the solution must reduce each plate to a canonical hashable signature and count matches efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need order-insensitive identity. Examples include log deduplication, compiler symbol normalization, search-query canonicalization, fraud detection on tokenized identifiers, and streaming pipelines that aggregate semantically equivalent events. At production scale, pairwise comparison collapses under quadratic cost and cache-unfriendly behavior. Canonical signatures plus hash-based counting turn equivalence detection into a linear pass with predictable memory use. That shift matters architecturally: it enables online aggregation, partitionable processing, and stable performance under bursty workloads where naive comparison would blow latency budgets or force premature sharding.

## 🔍 Problem Statement
You are given an array `plates` where each element is a string of length `1..20`, containing only `'A'..'Z'` and `'0'..'9'`. Two plates match if they contain exactly the same characters with the same frequencies, regardless of order. Return the number of unordered pairs `(i, j)` such that `i < j` and `plates[i]` matches `plates[j]`.

Constraints:
- `1 <= plates.length <= 100000`
- `1 <= plates[i].length <= 20`
- characters are limited to 36 possible symbols
- result may exceed 32-bit integer range

Examples:

- `["A1B1", "1AB1", "AB12", "B2A1", "XYZ", "ZYX"] -> 3`
- `["AA11", "1A1A", "A11A", "BB2", "2BB", "B2B", "C3"] -> 6`

The key constraint is input size: `O(n^2)` pair checking is infeasible, so the algorithm must group equivalent plates in near-linear time.

## 🪜 How to Solve This
1. Read the matching rule carefully → order does **not** matter, only character counts do. That means each plate belongs to an equivalence class defined by its frequency vector.

2. Once you see equivalence classes, think grouping → grouping at scale means a hash map.

3. The next question is the map key → the raw string is wrong because `"A1B1"` and `"1AB1"` differ. We need a canonical representation that is identical for all permutations of the same multiset.

4. Because the alphabet is fixed and small (26 letters + 10 digits), build a 36-slot frequency signature for each plate. That is more direct than sorting and avoids `k log k` work.

5. Iterate through `plates` once. For each signature, the number of new matching pairs equals how many times that signature has already appeared.

6. Add that count to the answer, then increment the signature’s frequency in the map.

7. This works because every valid pair is counted exactly once: when the later plate in the pair is processed.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + frequency-signature pattern.**  
   The problem is not about comparing strings directly; it is about counting members of the same equivalence class. Hashing is the right abstraction because we need fast lookup of “how many previous items are equivalent to this one?”

2. **Define a canonical signature for each plate.**  
   Create an array of length 36, where each slot stores the count of one symbol: `0-9` and `A-Z`. For every character in the plate, increment the corresponding slot. Two plates match if and only if their 36-count vectors are identical. This is correct because the vector fully captures multiplicity and ignores order.

3. **Serialize or otherwise hash the signature.**  
   Convert the 36 counts into a stable key usable in a hash map. A tuple, fixed-size array wrapper, or delimited string all work. The invariant is: equal multisets produce equal keys; unequal multisets produce different keys.

4. **Process plates in one pass.**  
   For each plate, compute its key and look up how many times that key has already appeared. If the map says `c`, then this plate forms exactly `c` new pairs with prior plates. Add `c` to the running answer.

5. **Update the map after counting.**  
   Increment the stored count for that key. This preserves the invariant that the map always contains counts of signatures seen strictly before the current index.

6. **Use a 64-bit accumulator.**  
   In the worst case, all plates match, producing `n * (n - 1) / 2` pairs. With `n = 100000`, that exceeds 32-bit signed range.

## 📊 Worked Example
Example: `["AA11", "1A1A", "A11A", "BB2", "2BB", "B2B", "C3"]`

| Step | Plate | Signature Summary | Previous Count | New Pairs | Total |
|---|---|---|---:|---:|---:|
| 1 | `AA11` | `A:2, 1:2` | 0 | 0 | 0 |
| 2 | `1A1A` | `A:2, 1:2` | 1 | 1 | 1 |
| 3 | `A11A` | `A:2, 1:2` | 2 | 2 | 3 |
| 4 | `BB2` | `B:2, 2:1` | 0 | 0 | 3 |
| 5 | `2BB` | `B:2, 2:1` | 1 | 1 | 4 |
| 6 | `B2B` | `B:2, 2:1` | 2 | 2 | 6 |
| 7 | `C3` | `C:1, 3:1` | 0 | 0 | 6 |

The running invariant is simple: before each row, the map stores counts for all earlier plates. The current plate contributes one pair for each earlier plate with the same signature.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * k)`, where `n` is the number of plates and `k` is the maximum plate length, plus `O(36)` per signature construction, which is constant. Since `k <= 20`, this is effectively linear in input size. At `10^6` items this remains practical; at `10^9`, memory bandwidth and distributed partitioning dominate.

### Space Complexity
`O(u)`, where `u` is the number of distinct signatures stored in the hash map. The signature representation owns most of the space. You can reduce overhead with compact binary keys, but that trades readability and implementation simplicity for tighter memory behavior.

## 💡 Key Takeaways
- If the problem says “same elements/frequencies regardless of order,” you are almost certainly looking for a canonical representation plus hashing.
- If you need the number of matching pairs across a large collection, think “count equivalence classes,” not “compare every pair.”
- Do not use a 32-bit integer for the answer; `100000 * 99999 / 2` overflows signed `int`.
- Be careful with signature encoding: ambiguous string concatenation without separators can cause collisions.
- In production systems, canonicalization is often the real design move: once identity is normalized, counting, deduplication, partitioning, and caching all become cheap.

## 🚀 Variations & Further Practice
- Count matching pairs in a stream with time-window expiration; the harder part is maintaining signature counts under insertions and deletions.
- Group plates by “almost matching,” where one character count may differ by ±1; the twist is generating neighboring signatures efficiently.
- Extend to arbitrary Unicode identifiers; the harder part is signature design when the alphabet is no longer fixed and compact.