# Anagram Group Reunion

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, String, Sorting

---

## 🗂 What Is This Problem? *(For Everyone)*

Given a list of words and a series of search terms, find all words that use exactly the same letters as each search term — in any order. The catch: once words are matched and "claimed" by a search term, they disappear from the pool and cannot be claimed again. Each search term sees only what's left after previous searches have taken their share.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This pattern mirrors real inventory and resource allocation systems. Think of an e-commerce warehouse: when a customer orders a product, those units are reserved and removed from available stock so no other order can claim them. The same logic applies to ticket booking systems (seats claimed per transaction), HR platforms matching candidates to job roles (a hired candidate leaves the pool), and content moderation queues where reviewed items are cleared before the next reviewer sees the list. Solving this efficiently reduces processing time, prevents double-allocation errors, and directly improves customer experience and operational accuracy.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Imagine a shared lost-and-found bin of letter tiles. Several people arrive one at a time, each looking for tiles that spell their target word — in any arrangement. The first person takes every matching set of tiles they find. The second person searches the now-smaller bin, and so on. Your job is to record exactly which tiles each person successfully claimed, in the order they appeared in the bin.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an array `words` of `n` strings and an array `queries` of `q` strings, process each query sequentially. For query `i`, find all strings currently remaining in `words` that are anagrams of `queries[i]` — meaning they contain identical characters at identical frequencies. Return those matches in their original relative order, then permanently remove them from `words` before processing query `i+1`.

**Constraints:**
- `1 <= n <= 10^4`, `1 <= q <= 10^3`
- `1 <= words[i].length, queries[i].length <= 20`
- All strings consist of lowercase English letters only.

**Example 1:**
- Input: `words = ["eat","tea","tan","ate","nat","bat"]`, `queries = ["ate","tan"]`
- Output: `[["eat","tea","ate"], ["tan","nat"]]`

**Example 2:**
- Input: `words = ["abc","bca","cab","xyz","zyx"]`, `queries = ["abc","xyz","abc"]`
- Output: `[["abc","bca","cab"], ["xyz","zyx"], []]`

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Compute a canonical key for each word.** Sort the characters of every word alphabetically. For example, `"eat"`, `"tea"`, and `"ate"` all become `"aet"`. This sorted form is the fingerprint that uniquely identifies an anagram group — any two words sharing a fingerprint are anagrams of each other.

2. **Build a hash map from fingerprint → list of (original_index, word).** Pre-processing all words into this map means we never re-scan the entire word list for each query. Lookups become O(1) on average.

3. **Process each query in order.** Sort the query string to get its fingerprint. Look up that fingerprint in the hash map to retrieve all currently stored matching words.

4. **Collect results and remove matched words.** Append the matched words (preserving their original relative order, since we stored them in insertion order) to the result list. Then delete the fingerprint entry from the hash map — this is the "permanent removal" step that prevents future queries from seeing these words.

5. **Handle missing keys gracefully.** If no words remain under a fingerprint (or none ever existed), return an empty list for that query.

6. **Return the accumulated result list** after all queries are processed.

---

## 📊 Worked Example *(For Developers)*

**Input:** `words = ["eat","tea","tan","ate","nat","bat"]`, `queries = ["ate","tan"]`

| Step | Action | Hash Map State | Result So Far |
|---|---|---|---|
| Init | Sort each word to build map | `"aet"→["eat","tea","ate"]`, `"ant"→["tan","nat"]`, `"abt"→["bat"]` | `[]` |
| Query 1: `"ate"` | Sort → `"aet"`; look up key | Found: `["eat","tea","ate"]` | `[["eat","tea","ate"]]` |
| Remove | Delete `"aet"` from map | `"ant"→["tan","nat"]`, `"abt"→["bat"]` | `[["eat","tea","ate"]]` |
| Query 2: `"tan"` | Sort → `"ant"`; look up key | Found: `["tan","nat"]` | `[["eat","tea","ate"],["tan","nat"]]` |
| Remove | Delete `"ant"` from map | `"abt"→["bat"]` | Final output ✓ |

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n · L log L + q · L log L)** where `L` is the maximum string length. Sorting each word or query takes O(L log L). We do this once for all `n` words and once per query. With `n ≤ 10^4`, `q ≤ 10^3`, and `L ≤ 20`, the total operations remain well within millisecond range even at maximum input size.

### Space Complexity

**O(n · L)** for the hash map storing all words and their fingerprints. In practice this is the dominant cost — at most 10,000 strings of length 20, which is roughly 200 KB of memory, trivially manageable on any modern system.

---

## 💡 Key Takeaways *(For Everyone)*

- **Stateful querying has real business value** — modeling "claimed and removed" resources prevents double-booking and inventory conflicts in production systems.
- **Pre-processing data into a lookup structure pays dividends** — building the hash map once means each query is answered in near-constant time rather than scanning the full list repeatedly.
- **Sorted strings are a powerful canonical key** — any two anagrams share the same sorted form, making grouping and comparison trivially O(L log L) without complex character counting.
- **Hash map deletion is the elegantly simple removal mechanism** — rather than rebuilding the word list after each query, a single `del map[key]` achieves permanent removal in O(1).
- **Order preservation is free when you store words in insertion order** — Python dicts and most modern hash map implementations maintain insertion order, so relative ordering requires no extra sorting step.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Frequency count key instead of sorting:** Replace the sorted-string fingerprint with a tuple of 26 character counts. This changes the key-generation step from O(L log L) to O(L) — benchmark both approaches and measure the crossover point where one outperforms the other.
- **Variation 2 — Partial removal:** Instead of removing all matched words after a query, only remove the *first* match found. How does this change the data structure design and the time complexity per query?
- **Variation 3 — Concurrent queries:** What if multiple queries must be answered simultaneously against the same snapshot of `words`, and only then are all matched words removed in bulk? Redesign the algorithm to support this batch-query model.

---