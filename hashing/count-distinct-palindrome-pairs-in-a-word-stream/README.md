# Count Distinct Palindrome Pairs in a Word Stream

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String, Palindrome

---

## 🗂 Problem Overview
Given an array of lowercase strings, count how many distinct **ordered** index pairs `(i, j)` satisfy `i != j` and `words[i] + words[j]` is a palindrome. Indices, not string values, define uniqueness, so duplicates must be counted separately. The challenge is avoiding the obvious `O(n^2)` pair scan: with up to `10^5` words, only an approach that uses hashing plus palindrome-aware splits is viable.

## 🌍 Engineering Impact
This pattern shows up in streaming text analytics, search query normalization, compiler token pipelines, and log-processing systems where pairwise compatibility must be detected under strict latency budgets. The core issue is joining records by a derived structural property rather than exact equality. At small scale, brute-force cross-products hide the problem; at production scale, they explode CPU, cache miss rates, and tail latency. Hash-indexing transformed representations—here, reversed strings plus palindrome-compatible prefixes/suffixes—turns an intractable all-pairs comparison into bounded local lookups. That shift is the difference between a batch-only solution and something usable inside real-time ingestion or ranking paths.

## 🔍 Problem Statement
You are given `words`, an array of lowercase strings representing tokens from a live stream. Count the number of distinct ordered pairs `(i, j)` such that:

- `i != j`
- `words[i] + words[j]` is a palindrome

Important details:

- Order matters: `(i, j)` and `(j, i)` are different pairs.
- Equal string values at different indices are distinct candidates.
- `(i, i)` is never allowed.
- Empty strings are valid inputs.
- `1 <= words.length <= 10^5`
- `0 <= words[i].length <= 100`
- Total character count across all words is at most `3 * 10^5`

Examples:

- `["bat", "tab", "cat"] -> 2`  
  Valid pairs: `(0,1)`, `(1,0)`

- `["", "aba", "xy", "yx", "a"] -> 8`  
  Empty string pairs with standalone palindromes, and reverse matches like `"xy"` / `"yx"` contribute both directions.

The total-length constraint is what rules out quadratic pair checking and pushes toward hashing over reversed words and split positions.

## 🪜 How to Solve This
1. Start with the brute-force definition: every pair `(i, j)` is valid if the concatenation is a palindrome. That is immediately too expensive at `10^5` words.

2. Ask what must be true for `a + b` to be a palindrome. If you split `a` at some position, one side must already be a palindrome, and the other side must be matched by the reverse of `b`.

3. That suggests a hash-based lookup structure: store every word by value, but indexed so reverse-compatible matches are cheap to find.

4. For each word, try every split position `k` from `0` to `len(word)`:
   - If the prefix is a palindrome, then some other word equal to `reverse(suffix)` can go in front.
   - If the suffix is a palindrome, then some other word equal to `reverse(prefix)` can go after.

5. Because duplicates are counted by index, the hash map cannot store just one index; it must store all indices for a word.

6. Exclude self-pairs, count ordered pairs, and be careful not to double-count the same construction at the same split boundary.

Once you see “all-pairs check, but compatibility is determined by reversible structure,” the HashMap + palindrome-split approach is the natural reduction.

## 🧩 Algorithm Walkthrough
1. **Build a hash index of exact word values to all indices**.  
   Pattern: **Hashing / inverted index**.  
   Why: duplicate strings must contribute multiple ordered pairs, so each word maps to a list of indices, not a single position.  
   Invariant: after preprocessing, every exact string lookup returns all candidate indices in `O(1)` average time.

2. **Iterate through each word and enumerate every split position** from `0` to `m`, where `m` is the word length.  
   Why: every valid palindrome pair can be characterized by some split where one side of the current word is already palindromic.

3. **Check the prefix-palindrome case**.  
   If `word[0:k]` is a palindrome, then any word equal to `reverse(word[k:m])` can be prepended.  
   Correctness: the prepended reverse mirrors the suffix, while the palindromic prefix occupies the center safely.

4. **Check the suffix-palindrome case**.  
   If `word[k:m]` is a palindrome, then any word equal to `reverse(word[0:k])` can be appended.  
   Correctness: symmetric argument; the appended reverse mirrors the prefix.

5. **Count all matching indices except the current one**.  
   Why: `(i, i)` is forbidden even when the concatenation would be palindromic.

6. **Avoid duplicate counting at degenerate splits**.  
   In practice, the suffix case at `k = m` or equivalent empty-side scenarios can reproduce pairs already counted elsewhere. Guard that branch carefully.

This works because every valid ordered pair must expose a split where one side is palindromic and the other side is exactly the reverse of the partner word.

## 📊 Worked Example
Example: `words = ["", "aba", "xy", "yx", "a"]`

| i | word | split | palindrome side | needed reverse | matches | pairs added |
|---|------|-------|-----------------|----------------|---------|-------------|
| 0 | `""` | 0 | prefix/suffix | `""` | self only | none |
| 1 | `aba` | 0 | suffix `aba` | `""` | index 0 | `(1,0)` |
| 1 | `aba` | 3 | prefix `aba` | `""` | index 0 | `(0,1)` |
| 2 | `xy` | 0 | prefix `""` | `yx` | index 3 | `(3,2)` |
| 2 | `xy` | 2 | suffix `""` | `yx` | index 3 | `(2,3)` |
| 3 | `yx` | 0 | prefix `""` | `xy` | index 2 | `(2,3)` handled symmetrically |
| 3 | `yx` | 2 | suffix `""` | `xy` | index 2 | `(3,2)` handled symmetrically |
| 4 | `a` | 0/1 | palindromic side | `""` | index 0 | `(4,0)`, `(0,4)` |

The running count comes from reverse matches plus the empty string pairing with standalone palindromes. Self-matches are discarded.

## ⏱ Complexity Analysis
### Time Complexity
Let `L` be the total number of characters across all words. For each word of length `m`, we try `m + 1` splits and perform palindrome checks on substrings, giving `O(sum(m^2))` in the straightforward implementation. With `m <= 100`, this is acceptable under the total-length bound. At million-scale records, bounded token length keeps the approach practical; at billion-scale, you would need stronger substring-palindrome preprocessing.

### Space Complexity
`O(L + n)` for the hash map storing each distinct word and the list of its indices. The dominant owner is the inverted index over input strings. Space can be reduced by compressing duplicates into counts where index identity is irrelevant, but not in this problem because ordered index pairs must be counted exactly.

## 💡 Key Takeaways
- If a string-pair problem asks whether `a + b` satisfies a symmetry property, look for split points where one side is self-valid and the other side can be hash-matched.
- When order matters and duplicates are separate by index, a map from value to **all positions** is the right mental model, not a set or single-index map.
- Empty strings are a special case that naturally falls out of the split logic, but they often create accidental double counts if both branches are unchecked.
- The two palindrome conditions are asymmetric: prefix-palindrome enables prepending, suffix-palindrome enables appending; swapping them silently drops valid ordered pairs.
- In production systems, the broader lesson is to replace cross-product validation with indexing on a transformed key that captures structural compatibility, not literal equality.

## 🚀 Variations & Further Practice
- Count distinct palindrome pairs where each word can be used at most once globally; the twist is combining compatibility indexing with matching or flow constraints.
- Return all valid pairs online as words arrive in a stream; the harder part is maintaining reverse-compatible state incrementally under low-latency updates.
- Extend from exact palindromes to “near-palindromes” allowing one mismatch; this breaks simple reverse lookup and requires richer state or approximate matching structures.