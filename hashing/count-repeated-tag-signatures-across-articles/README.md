# Count Repeated Tag Signatures Across Articles

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Hash Map, Set Canonicalization

---

## 🗂 Problem Overview
Given `articles`, where each article is a list of string tags, count how many unordered article pairs reduce to the same distinct-tag set. Order does not matter, and repeated tags inside one article must be ignored. The output is a single 64-bit integer. The challenge is scale: with up to `100000` articles, pairwise comparison is too expensive, so the solution must canonicalize each article and aggregate counts efficiently with hashing.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to detect semantic equivalence after normalization: deduplicating event labels in streaming pipelines, collapsing permission scopes in IAM systems, grouping compiler symbol attributes, or identifying equivalent document facets in search indexing. At scale, naive pairwise comparison turns into quadratic latency and memory pressure. Canonical signatures plus hash-based aggregation convert an all-to-all comparison problem into a linear scan. That shift matters operationally: it enables online counting, bounded per-record work, and predictable performance under bursty workloads where millions of normalized entities must be grouped or deduplicated quickly.

## 🔍 Problem Statement
You are given `articles`, a list of articles, where each article is a list of lowercase string tags. Two articles share the same tag signature if, after removing duplicate tags within each article and ignoring order, the resulting sets are identical.

Return the number of unordered pairs of articles with the same normalized signature.

Constraints:
- `1 <= articles.length <= 100000`
- `1 <= articles[i].length <= 20`
- `1 <= tags[i][j].length <= 20`
- Tags contain lowercase English letters only
- Use 64-bit arithmetic for the answer

Examples:

- `articles = [["ai","cloud","ai"],["cloud","ai"],["ml"],["ml","ml"],["cloud"]]`
- Output: `2`

- `articles = [["news","sports"],["sports","news","sports"],["finance"],["news"],["finance","finance"],["sports","news"]]`
- Output: `4`

The key constraint is article count: `100000` rules out comparing every article pair directly.

## 🪜 How to Solve This
1. Read the problem → the real task is not comparing raw lists, but grouping articles by an equivalence relation: “same set of distinct tags.”
2. Once the problem is about grouping equivalent items, a `HashMap` should be the default instinct. The missing piece is the key.
3. Raw tag lists cannot be keys directly because order is irrelevant and duplicates inside one article do not matter.
4. So each article needs a canonical representation:
   - remove duplicates within the article
   - sort the remaining tags
   - serialize that sorted unique list into a stable signature
5. Now every article with the same normalized tag set produces exactly the same signature string or tuple.
6. Scan the articles once. For each signature, look up how many times it has already appeared and add that count to the answer. Then increment the stored count.
7. This works because when the current article is the `k`-th occurrence of a signature, it forms exactly `k-1` new unordered pairs with prior matching articles.
8. Result: no nested article comparisons, just canonicalization plus hash-based counting.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + canonicalization pattern.**  
   The abstraction is: convert each equivalence class member into one deterministic key, then count frequencies in a hash map. This is the right fit because equality is defined structurally, not by raw input order.

2. **Normalize one article at a time.**  
   For the current article, insert its tags into a local set to remove duplicates. This enforces the invariant that repeated tags inside a single article do not affect the signature.

3. **Build a canonical signature.**  
   Convert the deduplicated set into a list, sort it lexicographically, then join with a delimiter or store as an immutable tuple/vector key. Sorting guarantees that logically identical sets produce identical keys regardless of original tag order.

4. **Count pairs incrementally.**  
   Maintain `freq[signature] = number of prior articles with this signature`. Before incrementing, add `freq[signature]` to the answer. This is correct because each prior matching article forms one new unordered pair with the current article.

5. **Update the frequency map.**  
   Increment `freq[signature]`. The invariant after processing index `i` is: the map contains exact counts for articles `0..i`, and the answer equals the number of valid pairs among those processed articles.

6. **Use 64-bit arithmetic for the answer.**  
   In the worst case, all articles share one signature, yielding `n(n-1)/2`, which exceeds 32-bit range when `n` is large.

## 📊 Worked Example
Example: `articles = [["news","sports"],["sports","news","sports"],["finance"],["news"],["finance","finance"],["sports","news"]]`

| Step | Raw article | Normalized signature | Prior count | Pairs added | Total |
|---|---|---|---:|---:|---:|
| 1 | `["news","sports"]` | `["news","sports"]` | 0 | 0 | 0 |
| 2 | `["sports","news","sports"]` | `["news","sports"]` | 1 | 1 | 1 |
| 3 | `["finance"]` | `["finance"]` | 0 | 0 | 1 |
| 4 | `["news"]` | `["news"]` | 0 | 0 | 1 |
| 5 | `["finance","finance"]` | `["finance"]` | 1 | 1 | 2 |
| 6 | `["sports","news"]` | `["news","sports"]` | 2 | 2 | 4 |

Final answer: `4`.

The important state transition is that `prior count` always means “matching normalized articles already seen,” so adding it directly counts new unordered pairs exactly once.

## ⏱ Complexity Analysis
### Time Complexity
For each article with `m` tags, deduplication is `O(m)` average-case and sorting unique tags is `O(u log u)`, where `u <= m <= 20`. Total complexity is `O(n * (m + u log u))`, effectively linear in article count. At `10^6` records this remains practical; at `10^9`, the bottleneck becomes I/O and distributed aggregation, not asymptotic shape.

### Space Complexity
`O(k * u)` for the hash map, where `k` is the number of distinct signatures stored and `u` is signature size. The map dominates memory. You can reduce key size with hashed fingerprints, but that trades exactness for collision handling complexity unless you use collision-safe composite keys.

## 💡 Key Takeaways
- If equality ignores order and duplicates, the problem is usually “canonicalize first, then hash/group.”
- When asked for number of equivalent pairs, think frequency map plus incremental pair counting instead of explicit pair generation.
- Do not forget to deduplicate tags within each article before building the signature; sorting alone is insufficient.
- Use 64-bit integers for the answer, since `n(n-1)/2` can overflow 32-bit even when each article is tiny.
- In production systems, normalization is often the real data model; once you make it explicit, counting, deduplication, and caching all become straightforward.

## 🚀 Variations & Further Practice
- Count repeated signatures when tags have multiplicity, so `["a","a","b"]` differs from `["a","b"]`; the twist is canonicalizing multisets rather than sets.
- Support online updates and deletions of articles; the harder part is maintaining pair counts incrementally as signature frequencies both increase and decrease.
- Group near-matching articles where signatures may differ by at most one tag; this shifts from exact hashing to locality-sensitive indexing or combinational neighborhood generation.