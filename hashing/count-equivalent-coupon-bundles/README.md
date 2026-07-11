# Count Equivalent Coupon Bundles

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String, Counting

---

## 🗂 Problem Overview
Given a list of coupon bundles, count how many unordered index pairs represent the same multiset of coupon codes. Two bundles are equivalent when they contain identical codes with identical frequencies, independent of order. The output is a single integer: the number of matching pairs. The challenge is scale: up to 100,000 bundles, so comparing every pair is infeasible. The solution needs a canonical representation plus hashing to group equivalent bundles efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must deduplicate or aggregate unordered collections with multiplicity: shopping-cart normalization, search query feature bags, compiler dependency fingerprints, event enrichment pipelines, and distributed cache key generation. At small scale, pairwise comparison or ad hoc equality checks look harmless; at production scale, they explode into quadratic work, unstable latency, and poor cache locality. Canonicalization plus hashing turns equivalence into grouping, which enables linear-pass aggregation, shard-friendly partitioning, and deterministic signatures that can be reused across storage, analytics, and online serving paths.

## 🔍 Problem Statement
You are given `bundles`, where each `bundles[i]` is an array of coupon-code strings. Two bundles are equivalent if they contain exactly the same strings with the same counts, regardless of ordering. Return the number of unordered pairs `(i, j)` such that `i < j` and `bundles[i]` is equivalent to `bundles[j]`.

Constraints:
- `1 <= bundles.length <= 100000`
- `1 <= bundles[i].length <= 20`
- `1 <= couponCodes[i].length <= 20`
- Coupon codes contain only uppercase English letters and digits
- Total coupon codes across all bundles `<= 200000`

Examples:

- `bundles = [["SAVE10","FREESHIP"],["FREESHIP","SAVE10"],["BONUS"],["SAVE10","SAVE10","FREESHIP"],["FREESHIP","SAVE10","SAVE10"]]`
  → `2`

- `bundles = [["A","B","A"],["A","A","B"],["B","A"],["C"],["C"],["C","C"]]`
  → `2`

The key algorithmic constraint is avoiding `O(n^2)` pairwise comparison across up to 100,000 bundles.

## 🪜 How to Solve This
1. Read the equivalence rule carefully → order does **not** matter, but frequency **does**. That means each bundle behaves like a multiset, not a sequence.

2. If two bundles are equivalent, they should map to the same key. So the real question becomes: what canonical representation uniquely identifies a multiset of strings?

3. Since each bundle is small (`<= 20` items), sort the coupon codes inside the bundle. Equivalent bundles will produce the same sorted sequence; non-equivalent ones will not.

4. Once every bundle has a canonical key, the problem is no longer “compare bundles.” It becomes “count how many times each key appears.”

5. That immediately suggests a hash map: `key -> frequency`.

6. For each bundle:
   - sort it,
   - serialize the sorted codes into a collision-safe key,
   - look up how many identical bundles have already been seen,
   - add that count to the answer,
   - then increment the key’s frequency.

7. This works because the `k`-th occurrence of a bundle forms exactly `k-1` new pairs with previous identical bundles.

The mental shift is the whole problem: stop thinking about pairwise equality, start thinking about canonicalization plus counting.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: canonicalization + hashing.**  
   This is a grouping/counting problem over an equivalence relation. The right abstraction is to convert each bundle into a deterministic canonical form, then use a hash map to count equal forms. That avoids direct pairwise comparison.

2. **Canonicalize each bundle.**  
   Sort the coupon codes within a bundle lexicographically. Because equivalence ignores order but preserves multiplicity, sorting produces the same ordered list for all and only equivalent bundles. The invariant is: after sorting, equivalent bundles have identical sequences.

3. **Build a collision-safe key.**  
   Join sorted codes with a delimiter that cannot create ambiguity, or store the sorted list as a tuple-like structure depending on language. The invariant is: distinct multisets must not collapse into the same key due to serialization mistakes.

4. **Count incrementally.**  
   Maintain `freq[key] = number of prior bundles with this canonical form`. When processing a new bundle, add `freq[key]` to the answer before incrementing it. This is correct because each prior identical bundle forms one new unordered pair with the current index.

5. **Repeat for all bundles in one pass.**  
   The map accumulates frequencies across the scan. The invariant is: after processing index `i`, `answer` equals the number of equivalent pairs among bundles `0..i`.

6. **Return the accumulated pair count.**  
   If a canonical key appears `m` times, the incremental counting computes `0 + 1 + 2 + ... + (m-1) = m(m-1)/2`, exactly the number of unordered pairs for that group.

## 📊 Worked Example
Use:

`[["A","B","A"],["A","A","B"],["B","A"],["C"],["C"],["C","C"]]`

| Step | Bundle | Sorted form | `freq` before | Pairs added | `freq` after |
|---|---|---|---:|---:|---:|
| 1 | `["A","B","A"]` | `["A","A","B"]` | 0 | 0 | 1 |
| 2 | `["A","A","B"]` | `["A","A","B"]` | 1 | 1 | 2 |
| 3 | `["B","A"]` | `["A","B"]` | 0 | 0 | 1 |
| 4 | `["C"]` | `["C"]` | 0 | 0 | 1 |
| 5 | `["C"]` | `["C"]` | 1 | 1 | 2 |
| 6 | `["C","C"]` | `["C","C"]` | 0 | 0 | 1 |

Final answer: `1 + 1 = 2`.

The trace makes the counting rule obvious: every repeated canonical form contributes as many new pairs as the number of identical bundles already seen.

## ⏱ Complexity Analysis
### Time Complexity
Let `N` be the number of bundles and `K_i` the size of bundle `i`. Sorting each bundle costs `O(K_i log K_i)`, so total time is `O(sum(K_i log K_i))`, plus linear hash-map operations. Since each bundle has at most 20 codes, this is effectively near-linear in total input size. At `10^6` elements this remains practical; `O(n^2)` would not.

### Space Complexity
The hash map stores one entry per distinct canonical bundle, so space is `O(U * K)` in serialized-key terms, where `U` is the number of unique bundle forms. This can be reduced with custom hashing or interned identifiers, but only by trading readability and collision-handling complexity.

## 💡 Key Takeaways
- If equality ignores order but preserves multiplicity, you are almost certainly dealing with a multiset canonicalization problem.
- When the task asks for “number of equivalent pairs,” think “group by normalized key, then count combinations,” not nested comparisons.
- Do not serialize sorted strings naively without an unambiguous delimiter or length-aware encoding; otherwise distinct bundles can collide logically.
- Update the answer **before** incrementing frequency if you use the streaming-count pattern; reversing that introduces an off-by-one error.
- Canonical representations are not just interview tricks; they are a production-grade way to make equivalence deterministic, hashable, and shardable.

## 🚀 Variations & Further Practice
- Count equivalent bundles when each bundle is too large to sort cheaply; the harder twist is designing a frequency-map signature or rolling hash with collision guarantees.
- Group bundles under approximate equivalence, such as allowing one missing or extra coupon; the twist is moving from exact hashing to neighborhood generation or similarity indexing.
- Support online updates and queries in a streaming system; the twist is maintaining pair counts incrementally under inserts, deletes, and possibly distributed state.