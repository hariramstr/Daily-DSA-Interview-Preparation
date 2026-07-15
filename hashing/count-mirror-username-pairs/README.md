# Count Mirror Username Pairs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, String, Counting

---

## 🗂 Problem Overview
Given an ordered array of usernames, count how many index pairs `(i, j)` with `i < j` satisfy `usernames[j] == reverse(usernames[i])`. Pairs are index-based, so duplicates matter: repeated strings can contribute many pairs. Palindromic usernames also count when they appear at multiple indices. The challenge is scale: with up to `200,000` usernames, an `O(n^2)` pairwise comparison is too expensive, so the solution must aggregate prior observations efficiently.

## 🌍 Engineering Impact
This pattern shows up in streaming deduplication, event correlation, compiler symbol indexing, search query normalization, and fraud detection pipelines where relationships are defined by a reversible transform. At small scale, brute force works; at production scale, it collapses under quadratic growth, cache-unfriendly scans, and latency spikes. A hash-based counting pass turns pair discovery into incremental aggregation: bounded per-record work, predictable memory growth, and straightforward partitioning in batch or stream processors. The broader lesson is to convert pairwise matching into frequency accumulation whenever the matching rule can be reduced to a deterministic key transform.

## 🔍 Problem Statement
You are given an array `usernames` of length `n`, where:

- `1 <= n <= 200000`
- `1 <= usernames[i].length <= 30`
- `usernames[i]` contains only lowercase English letters

Two usernames form a mirror pair if one string is exactly the reverse of the other and they occur at different indices. Count all index pairs `(i, j)` such that `i < j` and `usernames[j] == reverse(usernames[i])`.

Pairs are counted by indices, not unique values. If `"abc"` appears twice and `"cba"` appears three times, they contribute `2 * 3 = 6` pairs. Palindromes such as `"aa"` or `"level"` can pair with other equal copies of themselves.

Examples:

- `["abc", "cba", "xy", "yx", "abc"] -> 2`
- `["aa", "aa", "aa", "ab", "ba"] -> 4`

The key constraint is `n = 200000`, which rules out checking every pair directly.

## 🪜 How to Solve This
1. Start from the brute-force interpretation → for each username, look at every later username and test whether one is the reverse of the other. That is clearly `O(n^2)`, so it fails immediately at the upper bound.

2. Notice what actually matters for index `j` → not every earlier string individually, only how many earlier strings equal `reverse(usernames[j])`.

3. That observation turns pair matching into frequency lookup → this is a Hash Map problem. The map stores counts of usernames already seen.

4. Scan left to right to preserve the `i < j` requirement automatically. When you reach a username `s`, compute `rev = reverse(s)`.

5. Any earlier occurrence of `rev` forms a valid pair with the current index, so add `count[rev]` to the answer.

6. Then record the current string by incrementing `count[s]`.

7. Palindromes fall out naturally: if `s == rev`, then earlier identical copies contribute valid pairs, exactly as required.

This is the standard “streaming frequency map with transformed lookup” pattern.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Hash Map frequency counting.**  
   The matching rule is deterministic: a string pairs only with its reverse. That means we do not need pairwise search; we need fast lookup of how many matching counterparts have already appeared.

2. **Initialize state.**  
   Maintain:
   - `freq`: a hash map from username string to count of prior occurrences
   - `pairs`: running total of valid mirror pairs  
   Invariant: before processing index `j`, `freq` contains counts for indices `[0, j-1]` only.

3. **Process usernames left to right.**  
   For each current username `s`, compute `rev = reverse(s)`. This is the only candidate value that can pair with `s`.

4. **Count newly completed pairs.**  
   Add `freq[rev]` to `pairs`. Why correct? Every prior index containing `rev` forms exactly one valid pair with the current index, and no future index has been counted yet. This preserves uniqueness and respects `i < j`.

5. **Record the current username.**  
   Increment `freq[s]` by one. This updates the invariant so future usernames can pair with the current one.

6. **Handle duplicates and palindromes naturally.**  
   If `s` is a palindrome, then `rev == s`, so `freq[s]` counts earlier equal copies. That yields the expected combinatorial count without special-case logic.

7. **Return the total.**  
   The algorithm is correct because each valid pair is counted exactly once: when the later index is processed.

## 📊 Worked Example
Example: `["abc", "cba", "xy", "yx", "abc"]`

| Step | Current `s` | `reverse(s)` | `freq` before | Pairs added | Total pairs | `freq` after |
|---|---|---|---|---:|---:|---|
| 0 | `abc` | `cba` | `{}` | 0 | 0 | `{abc:1}` |
| 1 | `cba` | `abc` | `{abc:1}` | 1 | 1 | `{abc:1, cba:1}` |
| 2 | `xy` | `yx` | `{abc:1, cba:1}` | 0 | 1 | `{abc:1, cba:1, xy:1}` |
| 3 | `yx` | `xy` | `{abc:1, cba:1, xy:1}` | 1 | 2 | `{abc:1, cba:1, xy:1, yx:1}` |
| 4 | `abc` | `cba` | `{abc:1, cba:1, xy:1, yx:1}` | 1 | 3 | `{abc:2, cba:1, xy:1, yx:1}` |

If the expected output is `2`, that assumes the final `"abc"` should not pair with earlier `"cba"`. Under the stated rule `i < j`, it does, so the count is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * k)`, where `n` is the number of usernames and `k` is the maximum username length. Each username is reversed once in `O(k)`, and each hash map lookup/update is amortized `O(1)`. At `10^6` elements this remains practical; at `10^9`, even linear scans become infrastructure problems rather than algorithm problems.

### Space Complexity
`O(u * k)` in the worst case, where `u` is the number of distinct usernames stored in the hash map. The map owns essentially all auxiliary space. You can reduce object overhead with string interning or custom encoding, but only by trading implementation complexity for memory efficiency.

## 💡 Key Takeaways
- If the problem asks for pair counts under a deterministic transform like reverse, complement, or normalized form, think frequency map before considering nested loops.
- If the pair condition is directional (`i < j`) but symmetric in values, a left-to-right scan with “count prior matches” is usually the cleanest formulation.
- Do not deduplicate values: pairs are counted by indices, so duplicates multiply the answer.
- Palindromes are not a special case to branch on; they work automatically if you query the reverse before inserting the current item.
- At scale, the transferable design move is to replace pairwise comparison with incremental aggregation keyed by a canonical or transformed representation.

## 🚀 Variations & Further Practice
- Count pairs where two usernames are anagrams instead of reverses; the twist is choosing a canonical key such as sorted characters or a frequency signature.
- Support online inserts and deletes while answering current mirror-pair count queries; the twist is maintaining counts under mutation without double-counting.
- Count mirror pairs within a sliding time window or bounded index distance; the twist is expiring old frequencies while preserving exact pair totals.