# Count Distinct Prefix-Suffix ID Matches

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String Algorithms, Rolling Hash

---

## 🗂 Problem Overview
Given an array of lowercase strings `ids`, count how many distinct index pairs `(i, j)` with `i < j` share at least one common non-empty border. A border is any string that is both a prefix and a suffix of a string, including the full string itself. The challenge is not detecting borders for one string, but counting pairwise compatibility across up to `2 * 10^5` strings and total characters without quadratic comparisons or double-counting pairs that share multiple borders.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to correlate identifiers by structural signatures rather than exact equality: log deduplication, malware signature matching, compiler/token pipelines, DNA or sequence analytics, and search/query normalization. At scale, naive pairwise comparison collapses under cardinality and skew. What matters is extracting compact reusable signatures, then aggregating globally. The production lesson is broader than this problem: when compatibility is defined by shared derived features, the winning design is usually “compute local signatures once, count globally,” not “compare entities against each other.” That shift determines whether the system remains linear in input size or explodes with traffic growth.

## 🔍 Problem Statement
You are given `ids`, an array of lowercase strings. Two strings are compatible if there exists a non-empty string `x` such that `x` is both a prefix and suffix of each string. Equivalently, the two strings share at least one common border. A string is always a border of itself.

Return the number of distinct pairs `(i, j)` where `0 <= i < j < ids.length` and `ids[i]`, `ids[j]` are compatible. Count each pair once even if multiple shared borders exist.

Constraints:

- `1 <= ids.length <= 2 * 10^5`
- `1 <= ids[i].length <= 2 * 10^5`
- `ids[i]` contains only lowercase English letters
- `sum(ids[i].length) <= 2 * 10^5`
- The answer may exceed 32-bit range

Examples:

- `["ababa","aba","xxabaxx","abc","a"]`
- `["aaaa","aa","baab","aabaaa","zz"]`

The decisive constraint is total input size: any `O(n^2)` pair scan is dead on arrival, so the solution must aggregate border signatures in near-linear time.

## 🪜 How to Solve This
1. Read the condition carefully → compatibility is not full-string equality; it is overlap in the set of borders.

2. A naive thought is “for every pair, compare border sets.” That is immediately too expensive because there can be `O(n^2)` pairs.

3. Flip the viewpoint → instead of asking which pairs match, ask which strings contain each border.

4. For one string, all borders can be generated efficiently using a string algorithm such as the prefix-function (KMP failure links). That gives every border length in linear time for that string.

5. Borders are strings, so we need a compact comparable representation. Use hashing or interned substring IDs so equal borders across different strings map to the same key.

6. Now the core counting issue appears: a pair may share multiple borders, but must be counted once. So summing `C(freq(border), 2)` over all borders overcounts.

7. The clean fix is inclusion by first occurrence in processing order: for each new string, count how many prior strings share any border with it. Deduplicate prior matches across multiple borders using a per-string visited marker.

8. That yields a global linear-ish pass: extract borders → map border to prior string indices → count unique prior matches → append current string to each of its border buckets.

## 🧩 Algorithm Walkthrough
1. **Use the prefix-function / failure-chain pattern per string.**  
   For a string `s`, compute `pi[]` in `O(|s|)`. Starting from `len = |s|`, then repeatedly following `len = pi[len - 1]`, enumerate every border length, including the full string. This is correct because the KMP failure chain lists exactly all proper borders, and adding `|s|` includes the string itself.

2. **Assign each border a stable signature.**  
   Use rolling hash on prefixes, or another collision-resistant string key, so the prefix of length `len` can be represented in `O(1)` after preprocessing. Since every border is also a prefix, no substring extraction is needed. The invariant: equal border strings across all `ids` map to the same key.

3. **Maintain `borderToIds: signature -> list of prior indices`.**  
   This is the global inverted index. It answers: which earlier strings had this exact border?

4. **For the current string `i`, gather all compatible prior strings once.**  
   For each border signature of `ids[i]`, scan `borderToIds[signature]`. Mark prior indices in a `seen` array with a generation counter to avoid clearing. Each newly seen prior index contributes exactly one compatible pair `(prior, i)`. This prevents double-counting when two strings share multiple borders.

5. **After counting, publish the current string into all its border buckets.**  
   Append `i` to every `borderToIds[signature]` for its borders. The invariant after processing `i`: every bucket contains exactly the processed strings that own that border.

6. **Why this abstraction fits.**  
   This is an **inverted-index + string-border enumeration** problem. Prefix-function gives local structure; hashing gives canonical keys; the inverted index converts pairwise matching into incremental set union against prior records.

## 📊 Worked Example
Take `ids = ["aaaa","aa","aabaaa","zz"]`.

| `i` | `ids[i]` | Borders | Prior matches found | New pairs |
|---|---|---|---|---|
| 0 | `aaaa` | `a, aa, aaa, aaaa` | none | 0 |
| 1 | `aa` | `a, aa` | from `a`/`aa` → `{0}` | 1 |
| 2 | `aabaaa` | `a, aa, aabaaa` | from `a`/`aa` → `{0,1}` | 2 |
| 3 | `zz` | `z, zz` | none | 0 |

Trace:

1. Process `aaaa` → publish index `0` under its four border signatures.
2. Process `aa` → bucket `a` contains `[0]`, bucket `aa` also contains `[0]`; visited-marking deduplicates index `0`, so add only one pair.
3. Process `aabaaa` → buckets `a` and `aa` expose prior indices `0` and `1`; both counted once.
4. Process `zz` → no overlap with existing buckets.

Total = `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(totalChars + totalBorderOccurrences + totalCandidateHits)` expected with hashing. Prefix-function and border extraction are linear in total input size. Candidate hits are the total lengths of scanned inverted-index buckets; in adversarial cases this can degrade, but under the given total-character bound it is typically near-linear and avoids the impossible `O(n^2)` pair scan. At `10^6` elements, this distinction is the difference between a single pass and billions of comparisons.

### Space Complexity
`O(totalChars + totalBorderOccurrences + n)` for prefix/hash state, border buckets, and the visited-marker array. The dominant owner is the inverted index from border signature to prior string indices. You can reduce memory by storing counts only, but then you lose the ability to deduplicate pairs across multiple shared borders.

## 💡 Key Takeaways
- If compatibility is defined by sharing any derived feature, think inverted index before thinking pairwise comparison.
- Prefix/suffix overlap plus “all borders” is a strong signal for prefix-function, Z-function, or rolling-hash-based border enumeration.
- Do not sum pair counts per border bucket directly; the same pair may share several borders and must be counted once.
- Include the full string as a border explicitly; many implementations only walk proper borders from failure links and miss this case.
- The transferable design pattern is to compute canonical local signatures once, then aggregate through global posting lists instead of comparing entities directly.

## 🚀 Variations & Further Practice
- Count pairs that share a border of length at least `L` or the longest common border per pair; the twist is thresholded or maximal matching rather than existence.
- Support online inserts and queries over a stream of identifiers; the harder part is maintaining border signatures and deduplicated match counts incrementally.
- Extend from exact borders to approximate borders under one edit or wildcard rules; the conceptual jump is from deterministic signatures to fuzzy indexing.