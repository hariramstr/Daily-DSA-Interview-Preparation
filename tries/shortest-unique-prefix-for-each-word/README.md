# Shortest Unique Prefix for Each Word

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Tries &nbsp;|&nbsp; **Tags:** Trie, Hash Map, String

---

## 🗂 What Is This Problem? *(For Everyone)*

Given a list of words, find the shortest starting fragment of each word that belongs to that word alone. For example, if your list contains "dog" and "done," typing just "d" is not enough to tell them apart — but "dog" and "do" are. The goal is to find the minimum number of letters needed to uniquely identify every word in the list.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This algorithm powers the autocomplete and tab-completion features found in command-line tools, search engines, and mobile keyboards. When a user types into a terminal or search bar, the system must quickly determine the shortest input that narrows results to a single option. Faster identification means fewer keystrokes, reduced user frustration, and a smoother experience. Tools like Git, Linux shells, and IDE code editors rely on exactly this logic to save developers time every single day — directly boosting productivity and reducing errors caused by mistyped commands.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Imagine a library where every book has a unique title. A librarian wants to create the shortest possible shorthand for each title so that no two books share the same shorthand. For "The Great Gatsby" and "The Grapes of Wrath," just "The" is not enough — but "The Great" versus "The Grapes" works perfectly. This problem asks you to find that minimal shorthand for every word in a given list.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a list of **distinct** lowercase words, return an array where the `i-th` element is the shortest prefix of `words[i]` such that no other word in the list shares that prefix.

**Constraints:**
- `1 <= words.length <= 1000`
- `1 <= words[i].length <= 100`
- All words consist of lowercase English letters only.
- All words are distinct; no word is a prefix of another.

**Examples:**

```
Input:  words = ["dog", "cat", "car", "card", "done"]
Output: ["dog", "cat", "car", "card", "do"]

Input:  words = ["apple", "banana", "cherry"]
Output: ["a", "b", "c"]
```

In Example 1, `"do"` uniquely identifies `"done"` because no other word starts with `"do"`, even though `"d"` is shared with `"dog"`.

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use a **Trie** (prefix tree) — a tree structure where each node represents one character, and shared prefixes share the same path.

1. **Build the Trie:** Insert every word into the Trie character by character. At each node, maintain a counter (`count`) tracking how many words pass through that node. This tells us whether a prefix is shared.

2. **Why count?** A prefix is unique when exactly one word passes through its final node — meaning `count == 1` at that node. This is the key insight: we don't need to compare words directly; the count does it for us.

3. **Find the shortest unique prefix:** For each word, traverse the Trie again character by character. Stop at the first node where `count == 1`. The characters traversed up to and including that node form the shortest unique prefix.

4. **Why Trie over brute force?** A brute-force approach comparing every pair of words costs O(n² × L). The Trie reduces this to a single build pass and a single query pass, each linear in total character count.

5. **Return results:** Collect the prefix found for each word and return them in the original input order.

---

## 📊 Worked Example *(For Developers)*

**Input:** `["dog", "cat", "car", "card", "done"]`

**Step 1 — Build Trie (tracking pass-through counts):**

| Path | Count | Shared By |
|------|-------|-----------|
| `d` | 2 | dog, done |
| `d→o` | 2 | dog, done |
| `d→o→g` | 1 | dog only |
| `d→o→n` | 1 | done only |
| `c` | 3 | cat, car, card |
| `c→a` | 3 | cat, car, card |
| `c→a→t` | 1 | cat only |
| `c→a→r` | 2 | car, card |
| `c→a→r→d` | 1 | card only |

**Step 2 — Query each word (stop at first node with count == 1):**

| Word | Traversal | First count=1 node | Prefix |
|------|-----------|--------------------|--------|
| dog | d(2)→o(2)→g(1) | `g` | `"dog"` |
| cat | c(3)→a(3)→t(1) | `t` | `"cat"` |
| car | c(3)→a(3)→r(2) | never 1 until `r` at depth 3 | `"car"` |
| card | c(3)→a(3)→r(2)→d(1) | `d` | `"card"` |
| done | d(2)→o(2)→n(1) | `n` | `"do"` → stops at `n`, prefix is `"do"` |

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(N × L)** — where N is the number of words and L is the average word length. We make two passes over all characters: one to build the Trie and one to query it. Even with thousands of words, this scales linearly with total input size, making it highly efficient.

### Space Complexity

**O(N × L)** — the Trie stores at most one node per character across all words. In the worst case (no shared prefixes), this equals the total number of characters in the input. Practically, shared prefixes reduce actual memory usage significantly.

---

## 💡 Key Takeaways *(For Everyone)*

- **Autocomplete is everywhere:** Every time a search bar or terminal suggests a completion after one or two keystrokes, an algorithm like this is running behind the scenes.
- **Fewer keystrokes = real business value:** Reducing the characters a user must type directly improves speed, reduces errors, and enhances product experience — measurable outcomes.
- **Tries are built for prefix problems:** Whenever a problem involves shared beginnings of strings, a Trie is almost always the right data structure to reach for first.
- **Pass-through counts are the key trick:** Storing how many words pass through each node transforms a complex comparison problem into a simple threshold check (`count == 1`).
- **Two-pass pattern:** Build first, query second — this separation of concerns makes Trie solutions clean, testable, and easy to extend (e.g., adding deletion or frequency ranking later).

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Allow prefix words:** Modify the solution to handle cases where one word *is* a prefix of another (e.g., `["car", "card"]`). How does your Trie node need to change to mark word endings?
- **Variation 2 — Weighted by frequency:** Extend the problem so that more frequently typed words get priority — if two prefixes are equally short, prefer the one for the more common word. How would you store frequency data in the Trie?
- **Variation 3 — Real-time insertion:** Design a system where words are inserted one at a time and the shortest unique prefix for every existing word is updated dynamically after each insertion.

---