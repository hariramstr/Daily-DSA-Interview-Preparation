# Count Stores With a Unique Payment Method Mix

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Sets, String Normalization

---

## 🗂 Problem Overview
You are given `methodsUsed`, where each row lists payment methods observed at one store. Repeated method names within a store do not matter; only the distinct set matters. Two stores are equivalent if those distinct sets match exactly, regardless of order or duplicates. Return how many stores belong to a payment-method mix that appears exactly once. The non-trivial part is scale: up to `100000` stores, so pairwise set comparison is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must deduplicate entities by normalized feature sets: fraud detection on merchant capabilities, analytics pipelines grouping user event attributes, search indexing over unordered token bags, compiler symbol-table canonicalization, and stream processors aggregating logically equivalent records. At production scale, failing to normalize before grouping creates false distinctions, inflated cardinality, and expensive downstream joins. Hash-based canonicalization enables linear-time aggregation, compact signatures, and stable equivalence classes that can be counted, cached, partitioned, or shipped across services without re-computing set equality repeatedly.

## 🔍 Problem Statement
Given a 2D array `methodsUsed`, where `methodsUsed[i]` contains the payment method names recorded for store `i`, count how many stores have a **distinct-method set** that appears exactly once across all stores.

A store may list the same method multiple times, but duplicates must be ignored. Order also does not matter. So `["cash","card","cash"]` and `["card","cash"]` represent the same mix.

Constraints:

- `1 <= methodsUsed.length <= 100000`
- `0 <= methodsUsed[i].length <= 100`
- `1 <= total number of method entries across all stores <= 200000`
- Method names use lowercase English letters, length `1..20`

Examples:

- `methodsUsed = [["cash","card","cash"],["wallet"],["card","cash"],["gift","wallet"],["wallet","gift"],["bank"]]` → `2`
- `methodsUsed = [["cash"],[],["card","wallet"],["wallet","card","wallet"],[]]` → `1`

The key constraint is the number of stores: you need near-linear grouping, not repeated set comparisons between stores.

## 🪜 How to Solve This
1. Read the problem carefully → the comparison is not between raw lists, but between **sets of distinct strings**.
2. Once duplicates and order do not matter, each store needs a **canonical form**. Without that, logically identical mixes like `["card","cash"]` and `["cash","card","cash"]` hash differently.
3. How do you canonicalize a set of strings?  
   → Deduplicate within the store  
   → Sort the distinct method names  
   → Join them into a stable signature such as `"card#cash"`
4. Now the problem becomes: count how many times each signature appears across stores.
5. That immediately suggests a `HashMap<signature, frequency>`.
6. Make one pass over stores to build frequencies.
7. Make a second pass over those frequencies and sum the counts whose frequency is exactly `1`.

The core insight is that hashing only works if equivalent inputs normalize to the same key. Once you build that key correctly, the rest is standard frequency counting in linear time relative to input size.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + canonicalization pattern.**  
   The right abstraction is: convert each store’s unordered, duplicate-filled list into a deterministic representation, then count equal representations with a hash map. This avoids expensive set-to-set comparisons across stores.

2. **For each store, build its distinct method set.**  
   Insert all method names from `methodsUsed[i]` into a temporary hash set.  
   Why correct: duplicates within one store are explicitly irrelevant.  
   Invariant: after this step, the temporary set contains exactly the distinct methods for that store.

3. **Convert the set into a canonical signature.**  
   Copy the set into a list, sort lexicographically, then join with a delimiter that cannot create ambiguity.  
   Why correct: sorting removes order dependence; joining creates a stable hashable key.  
   Invariant: two stores produce the same signature iff their distinct method sets are identical.

4. **Count signatures globally.**  
   Increment `freq[signature]` in a hash map.  
   Why correct: all equivalent stores collide intentionally into the same bucket.  
   Invariant: after processing `k` stores, `freq` holds exact occurrence counts for signatures seen so far.

5. **Compute the answer.**  
   Iterate over all stores again, or over signature counts, and count stores whose signature frequency is `1`.  
   Why correct: the problem asks for stores belonging to a unique mix, not the number of unique mixes. Here those values coincide only because each qualifying mix contributes exactly one store.

6. **Handle empty stores naturally.**  
   An empty list normalizes to the empty signature. Multiple empty stores therefore collapse into the same group, which matches the definition.

## 📊 Worked Example
Using:

`[["cash","card","cash"],["wallet"],["card","cash"],["gift","wallet"],["wallet","gift"],["bank"]]`

| Store Index | Raw Methods | Distinct Set | Canonical Signature | Frequency After Insert |
|---|---|---|---|---|
| 0 | `["cash","card","cash"]` | `{cash, card}` | `card#cash` | 1 |
| 1 | `["wallet"]` | `{wallet}` | `wallet` | 1 |
| 2 | `["card","cash"]` | `{card, cash}` | `card#cash` | 2 |
| 3 | `["gift","wallet"]` | `{gift, wallet}` | `gift#wallet` | 1 |
| 4 | `["wallet","gift"]` | `{wallet, gift}` | `gift#wallet` | 2 |
| 5 | `["bank"]` | `{bank}` | `bank` | 1 |

Final frequency map:

- `card#cash -> 2`
- `wallet -> 1`
- `gift#wallet -> 2`
- `bank -> 1`

Only signatures with frequency `1` are `wallet` and `bank`, so exactly `2` stores have a unique payment method mix.

## ⏱ Complexity Analysis
### Time Complexity
For each store, deduplication is linear in that store’s length, and sorting costs `O(d log d)` where `d` is the number of distinct methods in that store. Overall complexity is `O(totalEntries + Σ d log d)`, bounded well by the input limits. At `10^6` elements this remains practical; at `10^9`, signature construction and memory movement dominate and require distributed processing.

### Space Complexity
`O(U + S)` where `U` is temporary per-store distinct methods and `S` is the total number of unique normalized signatures stored in the frequency map. The map owns most of the space. You can reduce retained memory with streaming or hashed fingerprints, but that trades away debuggability and collision safety.

## 💡 Key Takeaways
- If equivalence ignores order and duplicates, the problem is usually “normalize first, then hash/count,” not “compare every pair.”
- When the output depends on how many times an equivalence class appears, a frequency map over canonical signatures is the default move.
- Do not use the raw list as the key; `["cash","card"]` and `["card","cash","cash"]` must collapse to the same signature.
- Empty lists are valid stores and must normalize consistently; multiple empty stores belong to the same mix.
- Canonicalization is an architectural primitive: once you define a stable representation for logical identity, grouping, caching, partitioning, and deduplication all get simpler.

## 🚀 Variations & Further Practice
- Count how many **distinct** payment mixes exist instead of how many stores are uniquely mixed; same normalization, different aggregation target.
- Support online updates where methods arrive as a stream per store; the harder part is maintaining canonical signatures incrementally without rebuilding from scratch.
- Treat method names case-insensitively or with synonym mapping (`"cc" -> "card"`); the twist is adding a normalization layer before set construction, which changes correctness boundaries.