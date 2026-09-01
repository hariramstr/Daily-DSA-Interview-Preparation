# Count Mirror Inventory Code Pairs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String, Canonical Form

---

## 🗂 Problem Overview
Given up to 100,000 lowercase product codes, count how many unordered pairs are equivalent under this rule: reverse one string, then rotate it by any number of positions. Only strings of equal length can match. The challenge is avoiding pairwise comparison across all codes, which would be quadratic. The scalable approach is to compute a canonical signature for each code so all mirror-equivalent strings collapse into the same hash bucket.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to deduplicate or join records under non-trivial equivalence, not exact equality: compiler symbol normalization, DNA/circular-sequence matching, log aggregation, search indexing, and streaming entity resolution. At small scale, ad hoc pairwise checks pass tests; at warehouse, telemetry, or ingestion scale, they collapse under cardinality and latency pressure. Canonicalization plus hashing turns an expensive relation into a cheap grouping problem. That shift matters architecturally: it enables single-pass aggregation, partition-friendly processing, bounded per-record work, and predictable memory behavior in distributed pipelines and online services.

## 🔍 Problem Statement
You are given an array `codes` where each element is a non-empty lowercase string. Two indices `(i, j)` form a valid unordered mirror pair if `i < j` and `codes[j]` can be obtained by taking `codes[i]`, reversing it, and then applying any cyclic rotation, including zero rotations.

Only strings of the same length can ever match. Constraints are large enough to rule out naive all-pairs comparison:

- `1 <= codes.length <= 100000`
- `1 <= codes[i].length <= 50`
- `codes[i]` contains only lowercase English letters

Examples:

- `["abca", "cbaa", "zz", "zz", "aacb"] -> 3`
- `["abc", "cab", "bca", "xy", "yx", "aa"] -> 2`

The key algorithmic pressure is this: equivalence is not plain equality, but the input is large enough that we must reduce each string to a canonical representative and count matches via hashing.

## 🪜 How to Solve This
1. Read the relation carefully → it is not “all rotations are equal,” but “a string matches any rotation of its reverse.”

2. Reframe the problem → we do not need to compare every pair; we need to **group strings by equivalence class** under this mirror rule.

3. Ask what stays invariant across the class → if `t` is a rotation of `reverse(s)`, then `reverse(t)` is a rotation of `s`. So every member of the class is tied together by the same set of cyclic rotations, just viewed through reversal.

4. That suggests a canonical form → for any string `s`, compute `minRotation(s)` and `minRotation(reverse(s))`. A mirror-equivalence class is represented by the lexicographically smaller of those two.

5. Why this works → direct rotations share `minRotation(s)`. Mirror-related strings share `minRotation(reverse(s))`. Taking the minimum of both collapses both directions into one stable signature.

6. Once each code has a signature, the rest is standard hashing → count frequencies in a map, then sum `freq * (freq - 1) / 2` across buckets.

7. Because max length is only 50, even an `O(m^2)` per-string canonicalization is acceptable; the real win is eliminating the `O(n^2)` pair scan.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + canonical form pattern.**  
   The core abstraction is: convert an equivalence relation into exact-key grouping. Here, equality under “reverse then rotate” is awkward to test pairwise, but easy to count once every string maps to the same canonical signature.

2. **For each code `s`, compute its reverse `r`.**  
   This explicitly models the mirror transformation. Any valid partner of `s` must be some cyclic rotation of `r`.

3. **Compute the lexicographically minimal rotation of `s`.**  
   Call this `rotS`. It is identical for all cyclic rotations of `s`, so it canonically represents the rotation class of `s`.

4. **Compute the lexicographically minimal rotation of `r`.**  
   Call this `rotR`. Any string reachable from `s` by reverse-then-rotate belongs to the rotation class represented by `rotR`.

5. **Define the signature as `min(rotS, rotR)`.**  
   This is the critical invariant: if two strings are related either by rotation or by mirror-rotation, both will produce the same unordered pair of rotation-class representatives, hence the same minimum. This collapses the full equivalence class into one key.

6. **Insert the signature into a hash map and increment its count.**  
   The map maintains: after processing the first `k` strings, each bucket contains exactly the number of previously seen strings in that equivalence class.

7. **Compute the answer from frequencies.**  
   For each signature with count `f`, add `f * (f - 1) / 2`. This counts unordered index pairs without double-counting.

Because `m <= 50`, minimal rotation can be implemented by checking all rotations directly. In production or for larger strings, Booth’s algorithm reduces that step to linear time per string.

## 📊 Worked Example
Example: `codes = ["abca", "cbaa", "zz", "zz", "aacb"]`

| idx | code | reverse | minRotation(code) | minRotation(reverse) | signature | freq after |
|---:|---|---|---|---|---|---:|
| 0 | abca | acba | aabc | aacb | aabc | 1 |
| 1 | cbaa | aabc | aabc | aabc | aabc | 2 |
| 2 | zz | zz | zz | zz | zz | 1 |
| 3 | zz | zz | zz | zz | zz | 2 |
| 4 | aacb | bcaa | aacb | aabc | aabc | 3 |

Now count pairs per signature:

1. Signature `aabc` has frequency `3` → `3 * 2 / 2 = 3` pairs  
2. Signature `zz` has frequency `2` → `2 * 1 / 2 = 1` pair

That table exposes an important subtlety: strings in the same class do not need to share the same minimal rotation directly; one may align through the reversed side. The final signature handles both.

## ⏱ Complexity Analysis
### Time Complexity
Let `n` be the number of codes and `m` the maximum code length. With a simple minimal-rotation routine that checks all rotations, each string costs `O(m^2)`, so total time is `O(n * m^2)`. Given `m <= 50`, this is effectively linear in `n`. At million-record scale it remains practical; at billion-record scale, constant factors and distribution strategy dominate.

### Space Complexity
`O(n * m)` in the worst case for the hash map keys and counts, assuming many distinct signatures. The map owns almost all space. You can reduce key overhead with rolling hashes or interned IDs, trading readability and collision-handling complexity for lower memory pressure.

## 💡 Key Takeaways
- If a problem asks for pair counts under a non-trivial equivalence rule, the first instinct should be canonicalization plus hashing, not pairwise comparison.
- “Reverse, rotate, normalize” is a strong signal that the real object is an equivalence class, and the implementation hinge is choosing the right class representative.
- Do not group only by `minRotation(reverse(s))`; that misses strings whose own orientation is the canonical side of the same class.
- Only same-length strings can match, so any implementation that accidentally mixes lengths in a shared signature space is wrong unless length is implicit in the key.
- At scale, canonical forms are an architectural tool: they turn expensive relational checks into stable keys that support indexing, partitioning, and one-pass aggregation.

## 🚀 Variations & Further Practice
- Count pairs where strings are equivalent under **rotation, reversal, or both**, and support online inserts/deletes; the harder twist is maintaining counts under dynamic updates.
- Extend the alphabet and allow **up to one character substitution** after mirror-rotation; the twist is combining canonicalization with approximate matching.
- Return the **largest equivalence class** and one representative path of transformations; the twist is moving from counting to reconstructing witnesses while preserving efficient grouping.