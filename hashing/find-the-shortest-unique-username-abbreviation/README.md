# Find the Shortest Unique Username Abbreviation

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String, Prefix Counting

---

## 🗂 Problem Overview
Given a list of distinct lowercase usernames and an index `p`, return the shortest abbreviation of `usernames[p]` that no other username can produce. An abbreviation is any non-empty prefix followed by `*`, or the full username itself. The challenge is scale: with up to `2 * 10^5` total characters, repeatedly comparing the target against every other string and every possible prefix is too expensive. The right solution counts prefix collisions once, then answers the target directly.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need compact but unambiguous identifiers: UI display names, CLI symbol shortening, IDE autocomplete labels, compiler symbol tables, log stream labels, and search result disambiguation. At small scale, pairwise prefix checks look harmless; at fleet scale, they become latency spikes and memory churn. Prefix-frequency indexing turns repeated ambiguity checks into a single linear preprocessing pass. That enables deterministic label generation, predictable performance under large dictionaries, and clean separation between ingestion-time indexing and query-time lookup. The same trade-off appears in routing tables, observability pipelines, and metadata catalogs.

## 🔍 Problem Statement
You are given an array `usernames` of distinct lowercase strings and an index `p`. For the target `usernames[p]`, find the shortest valid abbreviation that is unique across the array.

A valid abbreviation keeps a non-empty prefix and replaces the remaining suffix with `*`. The full username without `*` is also valid. Two abbreviations conflict only if their final strings are exactly equal.

Examples:

- `usernames = ["marina","mark","mason","mila"], p = 0` → `"mari*"`
- `usernames = ["zoe","zora","zack","amy"], p = 3` → `"a*"`

Constraints:
- `1 <= usernames.length <= 2 * 10^5`
- `1 <= usernames[i].length <= 10^5`
- Sum of all username lengths `<= 2 * 10^5`
- All usernames are distinct
- `0 <= p < usernames.length`

The key constraint is total input size: any algorithm that rechecks many prefixes across many strings will degrade toward quadratic behavior and fail at the upper bound.

## 🪜 How to Solve This
1. Start from the definition of a conflict: `k`-prefix abbreviation of the target is unique iff no other username shares its first `k` characters.  
2. That immediately reframes the problem from “generate abbreviations” to “count how many usernames have each prefix.”  
3. If we had a frequency map for every prefix in the dataset, the target answer would be the first prefix length whose count is `1`.  
4. Why is that enough? Because every shorter abbreviation collides by definition, and the first unique prefix gives the shortest valid answer.  
5. What about the full username? Since all usernames are distinct, the full string is always unique, so an answer always exists even if every shorter starred form collides.  
6. Implementation path → scan every username once, emit all prefixes, increment counts in a hash map.  
7. Then scan prefixes of `usernames[p]` from shortest to longest and return the first with count `1`, formatted as `prefix + "*"`. If none works, return the full username.  

This is a classic prefix-counting + hashing problem: preprocess global ambiguity once, answer locally in linear time in the target length.

## 🧩 Algorithm Walkthrough
1. **Build a prefix frequency table using hashing.**  
   For each username, generate every non-empty proper prefix and increment its count in a hash map. The key is the prefix string; the value is how many usernames share it. We do not need to store the full word because the unstarred full username is always unique under the distinctness constraint.

2. **Maintain the counting invariant.**  
   After preprocessing, `freq[s]` equals the number of usernames whose prefix exactly matches `s`. This invariant is sufficient because abbreviation uniqueness depends only on exact prefix-string equality before appending `*`.

3. **Scan the target from shortest prefix to longest proper prefix.**  
   For `k = 1 .. len(target) - 1`, check whether `freq[target[:k]] == 1`. The first such `k` is the shortest unique starred abbreviation. Correctness follows from monotonic search over candidate lengths: once we examine prefixes in increasing order, the first unique one is minimal.

4. **Fallback to the full username.**  
   If every proper prefix has frequency greater than `1`, return the full target string. This is valid because usernames are distinct, so no other username can equal it.

5. **Why hashing is the right abstraction.**  
   The core pattern is **prefix counting with a hash map**. We are not comparing strings pairwise; we are indexing ambiguity classes. That reduces repeated conflict checks into O(1)-average lookups after a single linear pass over total characters.

## 📊 Worked Example
Example: `usernames = ["marina","mark","mason","mila"]`, `p = 0`

Target = `"marina"`

| Prefix | Count after preprocessing | Unique? |
|---|---:|---|
| `m` | 4 | No |
| `ma` | 3 | No |
| `mar` | 2 | No |
| `mari` | 1 | Yes |

Trace:
1. Build counts from all usernames’ proper prefixes.  
   - `"marina"` contributes `m, ma, mar, mari, marin`  
   - `"mark"` contributes `m, ma, mar`  
   - `"mason"` contributes `m, ma, mas, maso`  
   - `"mila"` contributes `m, mi, mil`
2. Query target prefixes in order.  
3. `m*` collides with all four usernames.  
4. `ma*` collides with `"marina"`, `"mark"`, `"mason"`.  
5. `mar*` collides with `"marina"` and `"mark"`.  
6. `mari*` is unique, so return `"mari*"`.

## ⏱ Complexity Analysis
### Time Complexity
`O(S)`, where `S` is the sum of all username lengths. Each character participates in prefix generation once during preprocessing, and the target scan is linear in its length. At `10^6` characters this is routine; at `10^9`, even linear passes become infrastructure decisions around memory bandwidth and sharding.

### Space Complexity
`O(S)` in the worst case for the prefix hash map, since distinct prefixes across all usernames must be stored. This can be reduced with a trie or rolling-hash encoding, but only by trading implementation complexity, constant factors, or collision handling.

## 💡 Key Takeaways
- If uniqueness depends on “how many items share this prefix/key,” think counting index first, not pairwise comparison.
- When the input bound is on total character count, linear scans over prefixes are usually acceptable; nested per-string prefix checks are not.
- Proper prefixes produce starred abbreviations; the full username is a separate valid fallback and should not be forced into the `*` logic.
- Off-by-one errors are easy here: prefix length must be non-empty, and the starred form only applies when some suffix remains.
- In production systems, precomputing ambiguity classes once is often the difference between predictable query latency and repeated hot-path string work.

## 🚀 Variations & Further Practice
- Return the shortest unique abbreviation for **every** username, not just `usernames[p]`; same prefix-counting core, but now query all strings efficiently after one preprocessing pass.
- Support **dynamic inserts/deletes** of usernames; the harder twist is maintaining prefix counts incrementally without rebuilding the entire index.
- Generalize abbreviations beyond prefixes, such as keeping first and last segments or allowing numeric compression; the twist is that the equivalence key is no longer a simple prefix and may require richer hashing or trie state.