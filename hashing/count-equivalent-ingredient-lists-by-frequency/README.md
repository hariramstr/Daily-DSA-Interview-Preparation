# Count Equivalent Ingredient Lists by Frequency

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Frequency Map, String Canonicalization

---

## 🗂 Problem Overview
Given `recipes`, where each recipe is a non-empty list of lowercase ingredient strings, count how many unordered index pairs `(i, j)` represent equivalent recipes. Two recipes are equivalent when they contain the same ingredients with the same frequencies, regardless of order. The challenge is scale: with up to `100000` recipes, comparing every pair is infeasible. The core task is to derive a canonical signature per recipe so equivalent multisets map to the same key.

## 🌍 Engineering Impact
This pattern shows up anywhere unordered structured data must be deduplicated or grouped by semantic equality. Examples include event normalization in streaming pipelines, compiler symbol aggregation, cache-key generation for query planners, product catalog deduplication, and security analytics over token bags or feature sets. At scale, pairwise comparison explodes quadratically and becomes operationally irrelevant. Canonicalization plus hashing turns equivalence detection into a linear pass with bounded per-record work. That shift enables partitionable pipelines, stable aggregation keys, and predictable memory behavior under high-cardinality workloads.

## 🔍 Problem Statement
You are given `recipes`, an array where `recipes[i]` is a non-empty array of lowercase ingredient names. Return the number of unordered pairs `(i, j)` such that `0 <= i < j < recipes.length` and the two recipes contain exactly the same multiset of ingredients. Order does not matter; frequency does.

Constraints:
- `1 <= recipes.length <= 100000`
- `1 <= recipes[i].length <= 20`
- `1 <= ingredient.length <= 20`
- Ingredients contain only lowercase English letters
- Total ingredient strings across all recipes do not exceed `300000`

Examples:

- `recipes = [["egg","milk","egg"],["milk","egg","egg"],["egg","milk"],["flour"],["flour"]]` → `2`
- `recipes = [["tomato","basil","tomato"],["basil","tomato","tomato"],["tomato","basil"],["cheese","tomato"],["tomato","cheese"]]` → `2`

The decisive constraint is input size: `O(n^2)` recipe comparison is too slow, so the solution must group recipes by a canonical representation.

## 🪜 How to Solve This
1. Read the equivalence rule → this is not sequence equality, it is **multiset equality**. Order is irrelevant, counts are not.
2. If order is irrelevant, each recipe needs a canonical form. Equivalent recipes must produce the same key; non-equivalent ones must not.
3. For a recipe, count ingredient frequencies with a small map. That captures exactly what defines equivalence.
4. A map itself is not a safe hash key unless serialized deterministically. So convert the frequency map into a stable signature, such as sorted `(ingredient, count)` pairs joined into a string.
5. Now the problem becomes grouping identical signatures. Use a hash map from `signature -> how many times seen`.
6. As each new recipe arrives, if its signature has already appeared `k` times, it forms `k` new pairs with prior equivalent recipes. Add `k` to the answer, then increment the stored count.
7. This avoids nested comparisons entirely: one pass over recipes, bounded work per recipe, and exact counting by construction.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + canonicalization pattern.**  
   The right abstraction is: transform each unordered collection into a deterministic key, then count equal keys with a hash map. This is the standard move when equality is semantic rather than positional.

2. **Build a frequency map for one recipe.**  
   Iterate its ingredient list and count occurrences: `ingredient -> count`. This is correct because recipe equivalence is defined exactly by ingredient multiplicities.

3. **Canonicalize the frequency map.**  
   Extract the unique ingredients, sort them lexicographically, and serialize as a stable signature such as `"egg#2|milk#1"`. Sorting is necessary because hash map iteration order is not stable; deterministic ordering is the invariant that makes equal multisets produce equal keys.

4. **Look up the signature in a global hash map.**  
   Let `seen[key]` be the number of prior recipes with the same signature. Every one of those forms a valid pair with the current recipe, so add `seen[key]` to the answer.

5. **Update the global count.**  
   Increment `seen[key]`. The maintained invariant is: after processing index `i`, `seen` contains exact counts for all signatures among recipes `0..i`, and `answer` equals the number of equivalent pairs within that prefix.

6. **Repeat for all recipes.**  
   Because each recipe is processed independently and contributes pairs only with prior matching signatures, every unordered pair is counted once and only once.

## 📊 Worked Example
Example: `recipes = [["egg","milk","egg"],["milk","egg","egg"],["egg","milk"],["flour"],["flour"]]`

| Step | Recipe | Frequency Map | Signature | seen before | Pairs added | Total |
|---|---|---|---|---:|---:|---:|
| 0 | `["egg","milk","egg"]` | `{egg:2, milk:1}` | `egg#2|milk#1` | 0 | 0 | 0 |
| 1 | `["milk","egg","egg"]` | `{milk:1, egg:2}` | `egg#2|milk#1` | 1 | 1 | 1 |
| 2 | `["egg","milk"]` | `{egg:1, milk:1}` | `egg#1|milk#1` | 0 | 0 | 1 |
| 3 | `["flour"]` | `{flour:1}` | `flour#1` | 0 | 0 | 1 |
| 4 | `["flour"]` | `{flour:1}` | `flour#1` | 1 | 1 | 2 |

Final answer: `2`.

The key observation is that recipes `0` and `1` collapse to the same canonical signature, as do `3` and `4`.

## ⏱ Complexity Analysis
### Time Complexity
For each recipe with `m` ingredients and `u` unique ingredients, building the frequency map is `O(m)` and sorting unique ingredient names is `O(u log u)`. Total complexity is `O(totalIngredients + Σ u log u)`. Given `recipes[i].length <= 20`, per-recipe work is tightly bounded. This remains practical at million-scale records; quadratic comparison does not.

### Space Complexity
`O(U)` for the global hash map of canonical signatures, where `U` is the number of distinct recipe signatures, plus `O(u)` temporary space per recipe for its local frequency map and sorted keys. Space can be reduced only by using more compact signature encoding, trading readability and implementation simplicity.

## 💡 Key Takeaways
- If the problem says “same elements regardless of order, but counts matter,” think **multiset canonicalization + hashing**, not pairwise comparison.
- If you need the number of equivalent pairs, a frequency map of canonical keys usually lets you count incrementally with `answer += seen[key]`.
- Do not use an unordered map’s iteration order directly as a key; serialize after sorting ingredients, or equal recipes may produce different signatures.
- Be careful to count frequencies, not just distinct ingredients; `["egg","milk","egg"]` and `["egg","milk"]` are not equivalent.
- In production systems, canonical keys are what make deduplication, aggregation, and partitioning composable across distributed boundaries.

## 🚀 Variations & Further Practice
- Count equivalent ingredient lists when ingredient names are large objects or IDs and signature construction cost dominates; the twist is designing a compact, collision-safe canonical encoding.
- Group recipes by equivalence class and return the largest class size or all grouped indices; the twist is moving from pair counting to materializing clusters efficiently.
- Support online updates where recipes are inserted and deleted over time; the harder part is maintaining pair counts incrementally under mutable state.