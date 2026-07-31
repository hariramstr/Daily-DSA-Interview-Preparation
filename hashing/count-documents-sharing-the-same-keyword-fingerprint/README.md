# Count Documents Sharing the Same Keyword Fingerprint

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Frequency Counting, Canonical Representation

---

## 🗂 Problem Overview
Given up to `10^5` documents, each represented as a list of keywords with repeats, count how many unordered document pairs share the same keyword-frequency fingerprint. A fingerprint is the sorted multiset of per-keyword occurrence counts, ignoring the keyword strings themselves. The challenge is not frequency counting inside one document; it is building a canonical, hashable representation per document so equivalent documents can be grouped efficiently without pairwise comparison.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to compare structural equivalence while discarding labels: search-query normalization, log-template clustering, compiler IR deduplication, document similarity pipelines, and feature-bucket aggregation in ranking systems. At scale, naive pairwise comparison collapses immediately: `O(n^2)` comparisons across `10^5` items is operationally irrelevant because it never finishes. Canonicalization plus hashing turns an equivalence problem into a grouping problem. That shift enables streaming aggregation, distributed partitioning by normalized keys, cache-friendly counting, and stable semantics across heterogeneous upstream producers that emit different tokens but identical frequency shapes.

## 🔍 Problem Statement
You are given `documents`, where each document is a non-empty list of lowercase keywords. Keywords may repeat within a document. For each document, count occurrences of each distinct keyword, discard the keyword names, sort the resulting counts, and treat that sorted list as the document’s fingerprint.

Two documents match if their fingerprints are identical. Return the number of unordered matching pairs across all documents.

Constraints:

- `1 <= documents.length <= 10^5`
- `1 <= total number of keywords across all documents <= 3 * 10^5`
- `1 <= keyword.length <= 20`
- Each document contains at least one keyword

Examples:

- `["red","red","blue","green","green"] -> [1,2,2]`
- `["cat","cat","dog","fox","fox"] -> [1,2,2]`

So those two documents match. The key constraint is total input size at scale: the solution must avoid comparing every document against every other document.

## 🪜 How to Solve This
1. Read the matching rule carefully → documents are equivalent based on **frequency shape**, not keyword identity.
2. That means each document must be reduced to a canonical form. Raw keyword lists are useless because order and names do not matter.
3. For one document, first count keyword occurrences with a frequency map.
4. Extract just the counts from that map. Now the keyword labels are gone, which is exactly what the definition requires.
5. Sort those counts. Without sorting, `[2,1,2]` and `[1,2,2]` would be treated differently even though they represent the same multiset.
6. Use the sorted count list as a hashable key: tuple, delimited string, or immutable vector depending on language.
7. Group documents by this canonical key in a global hash map.
8. Once grouped, each bucket of size `k` contributes `k * (k - 1) / 2` unordered pairs.
9. This avoids nested document comparisons entirely. The problem is really “normalize then count collisions,” which is a standard hashing pattern.

## 🧩 Algorithm Walkthrough
1. **Apply the canonical representation + hashing pattern.**  
   The right abstraction is: convert every document into a deterministic normalized key, then count equal keys with a hash map. This is the same pattern used in anagram grouping and structural deduplication.

2. **For each document, build a local frequency map.**  
   Scan its keywords and count occurrences per distinct term. This is correct because the fingerprint is defined from per-keyword multiplicities.  
   **Invariant:** after processing a document, the local map contains exact counts for every keyword in that document.

3. **Discard keyword identities and extract only the counts.**  
   The problem explicitly ignores actual strings once frequencies are computed. Keeping them in the key would over-distinguish equivalent documents.  
   **Invariant:** the extracted list represents the document’s multiset of keyword frequencies.

4. **Sort the count list to canonicalize the multiset.**  
   Multisets have no order, but arrays do. Sorting ensures every equivalent multiset maps to exactly one representation.  
   **Invariant:** any two documents with the same fingerprint produce identical sorted count lists.

5. **Serialize or store the sorted list in a hashable form.**  
   Examples: tuple `(1,2,2)` or string `"1#2#2"`. The representation must be collision-safe at the application level, not just hash-function level.

6. **Accumulate global frequencies of fingerprints.**  
   Increment `fingerprintCount[key]` for each document.  
   **Invariant:** after processing `i` documents, the map stores exact bucket sizes for the first `i` fingerprints.

7. **Compute pair counts from bucket sizes.**  
   For each bucket size `k`, add `k * (k - 1) / 2`. This counts unordered pairs exactly once and is equivalent to summing combinations `C(k,2)`.

## 📊 Worked Example
Use Example 1:

| Doc Index | Document | Local Keyword Counts | Fingerprint Key | Global Count After Insert |
|---|---|---|---|---|
| 0 | `["red","red","blue","green","green"]` | `red:2, blue:1, green:2` | `(1,2,2)` | 1 |
| 1 | `["cat","cat","dog","fox","fox"]` | `cat:2, dog:1, fox:2` | `(1,2,2)` | 2 |
| 2 | `["a","b","b","c"]` | `a:1, b:2, c:1` | `(1,1,2)` | 1 |
| 3 | `["m","m","n","n","p"]` | `m:2, n:2, p:1` | `(1,2,2)` | 3 |
| 4 | `["z"]` | `z:1` | `(1)` | 1 |

Final buckets:

- `(1,2,2) -> 3` documents → `3 * 2 / 2 = 3` pairs
- `(1,1,2) -> 1` document → `0` pairs
- `(1) -> 1` document → `0` pairs

Total = `3`.

## ⏱ Complexity Analysis
### Time Complexity
Let `T` be the total number of keywords across all documents, and let each document have `u_i` distinct keywords. Building local frequency maps costs `O(T)`. Sorting each document’s count list costs `O(sum(u_i log u_i))`. Total time is `O(T + sum(u_i log u_i))`, which is effectively near-linear under the given `3 * 10^5` total-keyword bound and remains viable where `O(n^2)` does not.

### Space Complexity
Space is `O(T)` in the worst case: local per-document frequency maps plus the global hash map of canonical fingerprints. The dominant owner is the set of stored fingerprint keys. You can reduce constant factors with compact tuple encoding or integer packing, but not the asymptotic need to store group counts.

## 💡 Key Takeaways
- If the problem says “same structure, different labels,” look for a canonical representation that removes irrelevant identity before comparison.
- If you need counts of equivalent objects across a large collection, think “hash normalized keys,” not pairwise matching.
- Do not use raw frequency-map iteration order as part of the key; maps are unordered, so equivalent documents may serialize differently.
- Be careful to sort the **counts**, not the keywords; sorting tokens solves a different problem and gives the wrong equivalence relation.
- In production systems, normalization is often the real algorithm: once a stable canonical form exists, grouping, caching, partitioning, and deduplication become trivial.

## 🚀 Variations & Further Practice
- Count documents that match after allowing one frequency edit, e.g. fingerprints within one insertion/deletion of a count; the twist is approximate canonical matching instead of exact hashing.
- Group documents by full keyword-frequency maps rather than frequency multisets; now keyword identity matters, so the canonical key must preserve labeled counts.
- Support online updates where keywords are appended to documents and pair counts must be maintained incrementally; the hard part is dynamic re-keying of documents across fingerprint buckets.