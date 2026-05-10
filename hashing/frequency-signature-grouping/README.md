# Frequency Signature Grouping

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String, Grouping

---

## 🗂 What Is This Problem? *(For Everyone)*

Given a list of words, we need to sort them into groups where every word in a group has the exact same "fingerprint" — meaning each word uses its letters with the same counts. For instance, "eat" and "tea" both use one 'e', one 'a', and one 't', so they belong together. Words that look completely different can still share the same fingerprint and be grouped.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This grouping technique powers real systems across many industries. Search engines use it to cluster similar queries and surface better autocomplete suggestions. Cybersecurity teams apply it to detect duplicate or disguised malware signatures that share the same underlying pattern. E-commerce platforms use similar fingerprinting to deduplicate product listings that describe the same item in different ways. By grouping items that share a hidden structural identity — rather than looking only at surface appearance — businesses reduce redundancy, improve search quality, and cut storage costs, all without requiring human review of every individual record.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Imagine you run a post office and receive hundreds of parcels. Instead of sorting by label, you weigh each parcel and count its dimensions. Any two parcels with the exact same weight-and-size profile get stacked together, regardless of what's written on the outside. This problem works the same way: we ignore what letters a word contains and instead ask, "how many of each letter does it use?" — then stack matching words into the same pile.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a list of strings `words`, group all strings that share the same **frequency signature**. Two strings share a frequency signature when their character-to-count mappings are identical — meaning the same characters each appear the same number of times.

**Constraints:**
- `1 <= words.length <= 10^4`
- `1 <= words[i].length <= 100`
- `words[i]` consists of lowercase English letters only.

**Example 1:**
```
Input:  words = ["eat", "tea", "tan", "ate", "nat", "bat"]
Output: [["eat","tea","ate"], ["tan","nat"], ["bat"]]
```

**Example 2:**
```
Input:  words = ["aab", "bba", "bbc", "aac", "xyz"]
Output: [["aab","aac"], ["bba","bbc"], ["xyz"]]
```

The order of groups and the order of strings within each group does not matter.

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Initialize a hash map.** Create a dictionary where each key is a frequency signature and each value is a list of words sharing that signature. A hash map gives us O(1) average-case lookups, which keeps the overall solution fast.

2. **Count character frequencies for each word.** For every word, use Python's `Counter` (or equivalent) to produce a character-to-count mapping. For example, `"aab"` produces `{'a': 2, 'b': 1}`.

3. **Convert the frequency map into a hashable key.** Dictionaries cannot be used directly as hash map keys. Instead, sort the character-count pairs alphabetically and convert them to a tuple — e.g., `(('a', 2), ('b', 1))`. Sorting ensures that two words with the same character-frequency pairs always produce the identical key, regardless of internal dictionary ordering.

4. **Insert each word into the correct group.** Use the tuple key to look up the group in the hash map. If the key doesn't exist yet, create a new list. Append the current word to the matching list.

5. **Return all groups.** Extract and return the dictionary's values as a list of lists.

This approach is clean, deterministic, and handles edge cases like single-character words and words with all unique characters naturally.

---

## 📊 Worked Example *(For Developers)*

**Input:** `["aab", "bba", "bbc", "aac", "xyz"]`

| Step | Word  | `Counter` Result        | Sorted Tuple Key              | Group Map (after insertion)                              |
|------|-------|-------------------------|-------------------------------|----------------------------------------------------------|
| 1    | `aab` | `{a:2, b:1}`            | `(('a',2),('b',1))`           | `{(('a',2),('b',1)): ["aab"]}`                           |
| 2    | `bba` | `{b:2, a:1}`            | `(('a',1),('b',2))`           | `{..., (('a',1),('b',2)): ["bba"]}`                      |
| 3    | `bbc` | `{b:2, c:1}`            | `(('b',2),('c',1))`           | `{..., (('b',2),('c',1)): ["bbc"]}`                      |
| 4    | `aac` | `{a:2, c:1}`            | `(('a',2),('c',1))`           | `{..., (('a',2),('c',1)): ["aac"]}`                      |
| 5    | `xyz` | `{x:1, y:1, z:1}`       | `(('x',1),('y',1),('z',1))`   | `{..., (('x',1),('y',1),('z',1)): ["xyz"]}`              |

**Final output:** `[["aab"], ["bba"], ["bbc"], ["aac"], ["xyz"]]`

> Note: `"aab"` and `"aac"` land in different groups here because their keys differ (`b` vs `c`). This correctly reflects that the full character-frequency pair — not just the count values — defines the signature.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n · k)**, where `n` is the number of words and `k` is the maximum word length. For each word we count its characters in O(k) time and sort at most 26 pairs (constant), making the total work linear in the size of the input. This scales comfortably to tens of thousands of words.

### Space Complexity

**O(n · k)** for storing all words and their keys in the hash map. In the worst case every word is in its own group, so memory grows linearly with input size — predictable and manageable even at scale.

---

## 💡 Key Takeaways *(For Everyone)*

- **Fingerprinting unlocks hidden structure.** Items that look completely different on the surface can share a deep identity — and grouping by that identity is enormously useful in search, deduplication, and fraud detection.
- **Grouping by signature saves human effort.** Automating structural matching means teams can process millions of records without manual review, directly reducing operational cost.
- **Hash maps are the right tool for grouping problems.** Whenever you need to bucket items by a shared property, a dictionary gives you O(1) average-case insertion and lookup.
- **Keys must be hashable and canonical.** Converting a frequency map to a sorted tuple ensures two equivalent signatures always produce the exact same key — a critical correctness requirement.
- **Sorting 26 pairs is effectively constant time.** Because the alphabet is fixed in size, the sorting step doesn't add meaningful overhead, keeping the algorithm linear overall.

---

## 🚀 Try It Yourself *(For Developers)*

- **Anagram grouping variant:** Modify the key to be the *sorted characters* of each word (e.g., `"eat"` → `"aet"`). How does this differ from frequency signature grouping, and when would each approach be more appropriate?
- **Frequency-only grouping:** Instead of using character-frequency pairs as the key, use only the *sorted list of frequency values* (e.g., `[2, 1]`). Which additional words get merged together, and what real-world scenario might justify this looser grouping?
- **Scale challenge:** Adapt the solution to process a stream of one million words efficiently, grouping on the fly without storing all words in memory at once. What data structure changes would you make?

---