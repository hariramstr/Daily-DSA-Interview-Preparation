# Count Pairs of Receipts With the Same Tax Breakdown

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Counting, Canonical Representation

---

## 🗂 Problem Overview
Given a list of receipts, where each receipt contains line items as `[category, amount]`, count how many unordered pairs of receipts are equivalent after aggregating amounts by category. Line-item order is irrelevant, and repeated categories within a receipt must be merged before comparison. The challenge is avoiding pairwise receipt comparison: with up to `100000` receipts and `200000` total line items, only a near-linear hashing-based approach is practical.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to detect semantic equality after normalization: invoice deduplication, event aggregation in streaming pipelines, compiler IR canonicalization, search query normalization, and distributed rate-limiter key construction. At scale, comparing raw structures directly is too expensive and too fragile because equivalent inputs may differ syntactically. Canonical representation plus hashing turns structural equality into key equality, enabling efficient grouping, deduplication, caching, and join operations. Without it, systems drift toward quadratic comparisons, inconsistent cache hits, and expensive downstream reconciliation logic.

## 🔍 Problem Statement
Each receipt is a list of line items, where every item is `[category, amount]`. A receipt reduces to a mapping from category to total amount by summing all amounts for the same category. Two receipts are tax-equivalent iff these reduced mappings are exactly identical.

Return the number of unordered equivalent receipt pairs.

Constraints:
- `1 <= receipts.length <= 100000`
- `1 <= total line items across all receipts <= 200000`
- `1 <= category.length <= 20`
- categories are lowercase English strings
- `1 <= amount <= 100000`
- result may exceed 32-bit range, so use 64-bit arithmetic

Example 1:
- `[[["food",200],["book",500],["food",300]], [["book",500],["food",500]], [["food",200],["book",400]], [["book",500],["food",500],["toy",100]]]`
- Output: `1`

Example 2:
- `[[["a",10],["b",5],["a",5]], [["b",5],["a",15]], [["a",15]], [["c",7]], [["c",7]], [["b",5],["a",15]]]`
- Output: `4`

The key algorithmic constraint is scale: nested receipt comparisons are infeasible.

## 🪜 How to Solve This
1. Read the equivalence rule carefully → receipts are not compared as raw lists; they are compared after **aggregation by category**.

2. That means each receipt must first be normalized. If a receipt contains `["food",200]` and `["food",300]`, those are really one logical entry: `"food" -> 500`.

3. Once normalized, the problem becomes: how many receipts produce the same reduced mapping?

4. Grouping equal objects efficiently suggests a `HashMap`. The real question is the key: maps are not directly hashable in many languages, and insertion order may differ.

5. So build a **canonical signature** for each receipt:
   - aggregate totals by category,
   - sort categories,
   - serialize as something deterministic like `"book#500|food#500"`.

6. Now equivalent receipts generate identical signatures, regardless of original line-item order or duplicates.

7. Count frequencies of signatures. If a signature appears `f` times, it contributes `f * (f - 1) / 2` unordered pairs.

8. This avoids quadratic comparison entirely: normalize once, hash once, count once.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + canonical representation pattern.**  
   The core abstraction is: convert each complex structure into a deterministic normalized key, then count equal keys. This is the right pattern because equivalence depends on semantic content, not original ordering.

2. **Process receipts one at a time.**  
   For each receipt, create a temporary map `totals[category] += amount`.  
   This is correct because the problem defines equivalence on aggregated category totals.  
   Invariant: after scanning a receipt, `totals` exactly matches its reduced tax breakdown.

3. **Canonicalize the reduced mapping.**  
   Extract the categories, sort them lexicographically, and serialize `(category, total)` pairs into a single string or tuple sequence.  
   Sorting is necessary because hash maps do not preserve a reliable comparison order across receipts.  
   Invariant: two receipts produce the same signature iff their reduced mappings are identical.

4. **Count signatures globally.**  
   Maintain `freq[signature]`. After generating a receipt’s signature, increment its count.  
   This groups all equivalent receipts together without direct comparison.

5. **Compute the number of unordered pairs.**  
   For each signature with frequency `f`, add `f * (f - 1) / 2` to the answer, using 64-bit arithmetic.  
   This is correct because every distinct pair inside the same equivalence class is valid exactly once.

6. **Return the accumulated total.**  
   The algorithm is efficient because work is proportional to total line items plus per-receipt category sorting, not receipt-pair comparisons.

## 📊 Worked Example
Use Example 2:

| Receipt Index | Raw Receipt | Aggregated Map | Canonical Signature | Freq After |
|---|---|---|---|---|
| 0 | `[["a",10],["b",5],["a",5]]` | `{a:15,b:5}` | `a#15|b#5` | 1 |
| 1 | `[["b",5],["a",15]]` | `{a:15,b:5}` | `a#15|b#5` | 2 |
| 2 | `[["a",15]]` | `{a:15}` | `a#15` | 1 |
| 3 | `[["c",7]]` | `{c:7}` | `c#7` | 1 |
| 4 | `[["c",7]]` | `{c:7}` | `c#7` | 2 |
| 5 | `[["b",5],["a",15]]` | `{a:15,b:5}` | `a#15|b#5` | 3 |

Final frequencies:
- `a#15|b#5 -> 3` gives `3 * 2 / 2 = 3` pairs
- `c#7 -> 2` gives `2 * 1 / 2 = 1` pair
- `a#15 -> 1` gives `0` pairs

Total answer: `3 + 1 = 4`.

## ⏱ Complexity Analysis
### Time Complexity
Let `L` be the total number of line items across all receipts, and let `k` be the number of distinct categories in a receipt. Aggregation is `O(L)`. Canonicalization adds `O(k log k)` per receipt for sorting distinct categories. Overall: `O(L + Σ k log k)`. At million-scale inputs this is practical; quadratic receipt comparison is not.  

### Space Complexity
`O(U + M)`, where `U` is the maximum number of distinct categories in one receipt for the temporary aggregation map, and `M` is the number of distinct canonical signatures stored globally. Space is dominated by the frequency table; reducing it would require trading exactness for probabilistic hashing or external storage.

## 💡 Key Takeaways
- If equality is defined after normalization or aggregation, think “canonical form + hash map,” not direct comparison.
- When the problem asks for the number of equivalent pairs across many objects, frequency counting is usually the right reduction.
- Do not build the signature from raw line-item order; receipts with identical totals but different ordering must collide intentionally.
- Use 64-bit arithmetic for the pair count: `f * (f - 1) / 2` can overflow 32-bit even when inputs are valid.
- Canonicalization is a production-grade design move: normalize once at system boundaries, then make grouping, caching, and deduplication cheap everywhere else.

## 🚀 Variations & Further Practice
- Count equivalent receipts in a streaming system with bounded memory: the twist is approximate counting or windowed state management instead of exact global frequency maps.
- Treat categories as hierarchical tax codes and consider receipts equivalent under subtree rollups: the harder part is canonicalizing after ontology-aware aggregation.
- Find near-equivalent receipts where one category total may differ by at most `d`: exact hashing no longer works directly, so you need locality-sensitive bucketing or indexed neighborhood search.