# Group Anagram Chains by Frequency

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, String, Sorting

---

## 🗂 Problem Overview

Given a list of words, group all anagrams together, then return the groups sorted by descending size. Ties in size are broken by lexicographic order of each group's smallest word. Within each group, words are sorted lexicographically. The non-trivial constraint is the compound sort requirement: you must simultaneously satisfy two ordering criteria across groups while maintaining intra-group ordering — a detail that is easy to get right in isolation and wrong under composition.

---

## 🌍 Engineering Impact

This pattern is the backbone of **canonical-key grouping** — a primitive that appears across domains at scale. Search engines use it to cluster query reformulations and spelling variants. Log aggregation pipelines (Splunk, Datadog) group structurally equivalent log templates by fingerprint. Compiler symbol tables canonicalize identifiers. Genomics tools group DNA k-mers by composition. Distributed deduplication systems in data lakes use sorted-byte-signature keys to detect semantic duplicates across partitions. Without a stable canonical key, grouping degrades to O(n²) pairwise comparison — untenable at 10⁸ records.

---

## 🔍 Problem Statement

**Input:** A list of strings `words` where `1 <= words.length <= 10^4` and `1 <= words[i].length <= 20`, all lowercase English letters. Duplicates are allowed and belong to the same group.

**Output:** A list of groups (each group is a sorted list of strings), where groups are ordered by descending size, with ties broken by ascending lexicographic order of the group's minimum word.

**Examples:**

```
Input:  ["eat", "tea", "tan", "ate", "nat", "bat"]
Output: [["ate","eat","tea"], ["nat","tan"], ["bat"]]

Input:  ["abc", "bca", "xyz", "zyx", "cab", "yzx"]
Output: [["abc","bca","cab"], ["xyz","yzx","zyx"]]
```

**Key constraint driving the algorithm:** Two words are equivalent iff their sorted-character representations are identical. This defines a stable equivalence class — the canonical key — which makes HashMap grouping both correct and efficient.

---

## 🪜 How to Solve This

1. **Read the problem → notice the core task is equivalence-class partitioning**, not sorting or searching. The word "group" is the signal.

2. **Equivalence class → think HashMap.** You need a key that is identical for all members of a class and distinct across classes. What property is shared by all anagrams? Their character multiset — which is exactly what sorting the characters produces.

3. **Choose the canonical key:** sorting each word's characters gives a deterministic, collision-free key in O(k log k) per word. An alternative is a frequency-count tuple (26 integers) — same correctness, avoids the sort, trades string allocation for array allocation.

4. **Single pass to build the map** → O(n · k log k) total. No nested loops needed; HashMap lookup is O(1) amortized.

5. **Post-processing:** sort each group's word list lexicographically, then sort the list of groups by `(-len, min_word)`. This two-key comparator is the entire complexity of the output contract.

6. **Result:** the approach is obvious in hindsight because sorting characters is the minimal transformation that preserves exactly the information anagram-equivalence cares about.

---

## 🧩 Algorithm Walkthrough

**Pattern: Canonical-Key HashMap Grouping**

This is the right abstraction because the problem defines an equivalence relation — reflexive, symmetric, transitive — and we need to partition a set by it. HashMap keyed on a canonical representative is the standard O(n) partition mechanism.

**Steps:**

1. **Initialize** an empty `HashMap<String, List<String>>`. Each entry maps a canonical key to the list of words sharing that key.

2. **Iterate** over every word in `words`. For each word:
   - Compute the canonical key: convert the word to a character array, sort it, convert back to a string. This is O(k log k) where k is word length.
   - Append the original word to `map[key]`. This maintains the invariant that every word in a bucket is an anagram of every other word in that bucket.

3. **Extract** all value lists from the map. At this point, each list is an unordered anagram family.

4. **Sort within each group** lexicographically. This satisfies the intra-group ordering requirement.

5. **Sort the list of groups** by the comparator `(-group.size(), group.min())` — descending size first, then ascending lexicographic order of the smallest element as the tiebreaker. The smallest element of a lexicographically sorted group is always `group[0]`.

6. **Return** the sorted list of groups.

**Invariant maintained throughout:** the canonical key is a pure function of the character multiset, so two words map to the same key if and only if they are anagrams.

---

## 📊 Worked Example

Input: `["eat", "tea", "tan", "ate", "nat", "bat"]`

| Word | Sorted Key | Map State (key → group)                          |
|------|------------|--------------------------------------------------|
| eat  | `aet`      | `{aet: ["eat"]}`                                 |
| tea  | `aet`      | `{aet: ["eat","tea"]}`                           |
| tan  | `ant`      | `{aet: ["eat","tea"], ant: ["tan"]}`             |
| ate  | `aet`      | `{aet: ["eat","tea","ate"], ant: ["tan"]}`       |
| nat  | `ant`      | `{aet: ["eat","tea","ate"], ant: ["tan","nat"]}` |
| bat  | `abt`      | `{aet: [...], ant: [...], abt: ["bat"]}`         |

**Sort within groups:** `["ate","eat","tea"]`, `["nat","tan"]`, `["bat"]`

**Sort groups by `(-size, min_word)`:** sizes are 3, 2, 1 → already descending. No tie to break.

**Output:** `[["ate","eat","tea"], ["nat","tan"], ["bat"]]`

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n · k log k)** where n is the number of words and k is the maximum word length. The dominant operation is sorting each word's characters. The final group sort is O(n log n) in the worst case but is bounded by the per-word cost since k ≤ 20. At n = 10⁶ this is comfortably sub-second; at n = 10⁹ you need distributed partitioning — the canonical key maps cleanly to a partition key in a shuffle step.

### Space Complexity

**O(n · k)** — the HashMap stores every word once plus its canonical key. The keys add at most a constant factor over the input size. This cannot be meaningfully reduced without sacrificing O(1) lookup; the trade-off would be recomputing keys on demand at the cost of time.

---

## 💡 Key Takeaways

- **Pattern signal — "group by equivalence":** any time the problem asks you to cluster items where membership is defined by a shared property (not a range or threshold), reach for canonical-key HashMap partitioning before considering sorting or graph approaches.
- **Pattern signal — "same characters, different order":** sorted characters as a HashMap key is the idiomatic anagram fingerprint; a frequency-count tuple (e.g., `[2,0,0,...,1,...]`) is the O(k) alternative worth knowing when k is large.
- **Implementation gotcha — compound sort key:** the tiebreaker uses the group's *minimum* word, which is `group[0]` only after the intra-group sort is complete. Sorting groups before sorting their contents produces a wrong tiebreaker.
- **Implementation gotcha — duplicates:** the problem explicitly allows duplicate words. They hash to the same key and land in the same bucket naturally — no special handling needed, but failing to account for them in your mental model leads to off-by-one reasoning about group sizes.
- **Architectural insight:** the canonical-key pattern generalizes directly to distributed systems — sorted-character keys (or any deterministic fingerprint) are stable partition keys for shuffle-based grouping in MapReduce, Spark, or Flink, enabling embarrassingly parallel anagram-family aggregation without cross-partition communication.

---

## 🚀 Variations & Further Practice

- **Anagram substring detection:** given a pattern p and a string s, find all starting indices in s where a substring is an anagram of p. The twist is that the "group" is a sliding window, so the canonical key must be maintained incrementally using a frequency-count array with O(1) updates — sorting per window is too slow.
- **Anagram families with edit distance tolerance:** group words that are anagrams *or* differ by at most one character substitution. This breaks the clean equivalence relation (transitivity no longer holds trivially), forcing a graph-based union-find approach where the HashMap is only the first layer of a two-phase algorithm.
- **Top-K largest anagram groups under a stream:** words arrive in a stream and you must maintain the top-K groups by size at all times. The canonical-key HashMap remains, but the output layer requires a min-heap of size K, turning the final sort into an online maintenance problem with O(log K) per insertion.