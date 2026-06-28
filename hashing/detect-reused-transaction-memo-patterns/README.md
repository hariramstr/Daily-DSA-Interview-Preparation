# Detect Reused Transaction Memo Patterns

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Hash Map, String

---

## 🗂 Problem Overview
Given a list of transaction memos, where each memo is an array of lowercase words, return every memo index that belongs to a repetition-pattern group of size at least two. Two memos match only if they have the same length and the same equality structure across positions. The challenge is avoiding pairwise memo comparison: with up to 100,000 memos, the solution must normalize each memo into a canonical signature and group them efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect structural equivalence while ignoring literal values: fraud pipelines clustering transaction descriptions, log analytics deduplicating templated messages, compiler/interpreter intern pools, and streaming systems grouping event shapes before downstream enrichment. At scale, naive pairwise comparison collapses under quadratic cost and poor cache behavior. Canonicalization plus hashing turns “compare everything to everything” into “compute once, group once.” That shift enables online grouping, bounded-latency ingestion, and simpler partitioning strategies because structural identity becomes a stable key rather than an expensive derived relation.

## 🔍 Problem Statement
Each memo is a list of lowercase words. Two memos belong to the same group if:

- they have the same number of words, and
- the positions of repeated words match exactly.

A memo like `["rent","paid","rent","late"]` normalizes to pattern `[0,1,0,2]`: first new word gets id `0`, next unseen word gets `1`, repeated `"rent"` reuses `0`, and `"late"` gets `2`. Any memo with the same normalized sequence belongs to the same group.

Return all indices whose memo belongs to a group of size at least `2`, sorted increasingly.

**Constraints**
- `1 <= memos.length <= 100000`
- `1 <= memos[i].length <= 100`
- `1 <= memos[i][j].length <= 20`
- total words across all memos `<= 300000`

**Examples**
- `memos = [["rent","paid","rent","late"],["coffee","today","coffee","again"],["taxi","home","taxi","home"],["x","y","x","z"]]`
  → `Output: [0,1,3]`
- `memos = [["a","b"],["c","c"],["dog","cat"],["hi"],["m","n","m"]]`
  → `Output: [0,2]`

The total-word bound is the key signal: the intended solution is linear in total input size, using hashing for grouping.

## 🪜 How to Solve This
1. Read the problem → this is not about matching actual words; it is about matching **repetition structure**.

2. Structural equivalence usually means we need a **canonical representation**. If two memos reduce to the same normalized form, they belong together.

3. For one memo, scan left to right and assign each new word the next integer id:
   - first unseen word → `0`
   - next unseen word → `1`
   - repeated word → reuse its earlier id

4. That turns every memo into a pattern signature such as `[0,1,0,2]`. This signature captures both memo length and repetition layout, so equal signatures imply valid grouping.

5. Once every memo has a signature, the problem becomes standard grouping:
   - `signature -> list of indices`

6. After one pass, collect only groups whose size is at least `2`.

7. Output must be globally sorted. If indices are appended in input order, each group list is already sorted; concatenate qualifying lists and sort once at the end, or collect via a boolean mark array and emit in index order.

The key leap is recognizing that hashing works only after canonicalization. Raw memo content is irrelevant; normalized structure is the real key.

## 🧩 Algorithm Walkthrough
1. **Use the Hash Map + Canonical Encoding pattern.**  
   This is the right abstraction because the task is grouping by an equivalence relation. Hashing is efficient only if equivalent memos map to the same deterministic key.

2. **For each memo, build its canonical pattern signature.**  
   Maintain a local map `word -> first assigned id` and a counter `nextId`. Scan words left to right:
   - if the word is unseen, assign `nextId` and increment it
   - append the assigned id to the signature  
   This is correct because the first occurrence order uniquely defines the repetition structure.

3. **Serialize the signature into a hashable key.**  
   In many languages, a tuple/list of integers is already hashable or can be converted into a delimited string. The invariant is: two memos produce identical keys iff they have identical repetition structure and equal length.

4. **Group memo indices by signature.**  
   Store `groups[signature].append(index)`. Because indices are processed in increasing order, each group preserves sorted order internally.

5. **Identify qualifying groups.**  
   Iterate through the grouped indices and keep only those lists with size `>= 2`. This matches the requirement that singleton patterns are excluded.

6. **Produce sorted output.**  
   Either gather all qualifying indices and sort once, or mark qualifying indices in a boolean array and emit them in a final linear scan. The latter avoids an extra `O(k log k)` sort and preserves strict linear behavior relative to input size.

7. **Why this is optimal enough.**  
   Every word is visited once during normalization, and every memo contributes one group insertion. Given the total-word constraint, this is the intended near-linear solution.

## 📊 Worked Example
Consider:

`[["rent","paid","rent","late"], ["coffee","today","coffee","again"], ["taxi","home","taxi","home"], ["x","y","x","z"]]`

| Index | Memo | Local word→id assignment | Signature | Group state |
|---|---|---|---|---|
| 0 | `["rent","paid","rent","late"]` | `rent→0, paid→1, late→2` | `[0,1,0,2]` | `[0,1,0,2] -> [0]` |
| 1 | `["coffee","today","coffee","again"]` | `coffee→0, today→1, again→2` | `[0,1,0,2]` | `[0,1,0,2] -> [0,1]` |
| 2 | `["taxi","home","taxi","home"]` | `taxi→0, home→1` | `[0,1,0,1]` | `[0,1,0,1] -> [2]` |
| 3 | `["x","y","x","z"]` | `x→0, y→1, z→2` | `[0,1,0,2]` | `[0,1,0,2] -> [0,1,3]` |

Qualifying groups are only those with at least two indices:
- `[0,1,0,2] -> [0,1,3]`

Final output: **`[0,1,3]`**

## ⏱ Complexity Analysis
### Time Complexity
Let `W` be the total number of words across all memos. Building each memo’s signature is linear in its length, so total normalization cost is `O(W)`. Group insertion is amortized `O(1)` per memo. With a final linear emit strategy, overall time is `O(W + n)`. This scales comfortably at `10^6` elements; at `10^9`, memory bandwidth and key materialization dominate.

### Space Complexity
The grouping map stores one entry per distinct pattern plus all memo indices, and each memo needs a temporary local word-to-id map while being processed. Total auxiliary space is `O(W + n)` in the worst case. You can reduce output assembly overhead with boolean marking, trading a small `O(n)` array for avoiding a global sort.

## 💡 Key Takeaways
- If the problem says “same structure, different values,” look for a canonical form before reaching for direct comparison.
- If you need to group many items by equivalence under large input constraints, `HashMap<signature, bucket>` is usually the first scalable design.
- Do not reuse the per-memo `word -> id` map across memos; the normalization must restart from id `0` for every memo.
- Signature encoding must preserve boundaries: naive string concatenation without delimiters can create collisions like `[0,11]` vs `[0,1,1]`.
- In production systems, canonicalization is often the real abstraction boundary: once structure is normalized, indexing, partitioning, caching, and deduplication all become simpler.

## 🚀 Variations & Further Practice
- Return full groups of equivalent memos instead of flat indices; the twist is preserving stable ordering while optimizing memory for large buckets.
- Support streaming ingestion where memos arrive continuously; the harder part is incremental grouping and bounded-memory retention for old signatures.
- Generalize from exact repetition structure to approximate structural similarity; this shifts the problem from deterministic hashing to sketching, locality-sensitive hashing, or edit-distance-style matching.