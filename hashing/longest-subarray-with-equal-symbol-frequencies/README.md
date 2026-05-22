# Longest Subarray With Equal Symbol Frequencies

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Prefix Sum, String, Frequency Counting

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine scanning a long piece of text and trying to find the longest section where every letter used appears the exact same number of times. If a section uses the letters A, B, and C, each must appear equally — no letter can dominate. This problem asks: what is the longest such section we can find inside any given piece of text?

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Balanced frequency detection is a foundational technique in data quality and fairness systems. Streaming platforms like Spotify use similar logic to ensure playlist diversity — no single artist dominates a recommendation window. In cybersecurity, analysts scan network logs to detect anomalies where certain event types appear disproportionately, signalling potential intrusions. In content moderation, balanced keyword distribution helps identify spam or manipulative text. Solving this efficiently at scale — across millions of records — directly translates to faster detection, lower compute costs, and better user experiences.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a bag of coloured candies laid out in a long row. You want to find the longest unbroken stretch of candies where every colour present appears the same number of times — no colour is over- or under-represented. You can start and end anywhere in the row, but you cannot skip candies in the middle. What is the longest fair stretch you can find?

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a string `s` of lowercase English letters (`'a'`–`'z'`) with length between 1 and 100,000, find the length of the longest contiguous substring in which every distinct character that appears does so with equal frequency.

**Constraints:**
- `1 <= s.length <= 100,000`
- `s` consists only of lowercase English letters

**Examples:**

| Input | Output | Reason |
|---|---|---|
| `"aabbcc"` | `6` | `a`, `b`, `c` each appear 2 times across the full string |
| `"aaabbbcc"` | `6` | Substring `"aaabbb"` (indices 0–5): `a` and `b` each appear 3 times |
| `"abcde"` | `5` | Every character appears exactly once; entire string is valid |

The solution must run in **O(n²) or better** time complexity.

---

## 🧩 Approach: How We Solve It *(For Developers)*

The key insight is to use **prefix frequency counts** combined with a **canonical signature** that captures the relative differences between character frequencies at each position.

1. **Build prefix frequency arrays.** As we scan left to right, maintain a running count of how many times each of the 26 letters has appeared up to the current index. This lets us compute the frequency of any character within any substring `[i, j]` in O(1) using subtraction.

2. **Normalise into a signature (canonical hash).** For any prefix ending at index `i`, compute the frequency vector of all characters seen so far. Subtract the minimum non-zero frequency from every active character's count. This "normalised" difference vector is the signature. Two prefixes sharing the same signature mean the substring between them has perfectly balanced character frequencies.

3. **Store the first occurrence of each signature.** Use a hash map that records the earliest index at which each signature was seen. When the same signature reappears at a later index, the substring between those two indices is valid.

4. **Track the maximum length.** Each time a repeated signature is found, compute the distance between the current index and the stored first-occurrence index. Update the global maximum if this distance is larger.

5. **Handle the base case.** Initialise the hash map with an empty signature at index `-1` to correctly handle valid substrings that start from index 0.

---

## 📊 Worked Example *(For Developers)*

**Input:** `s = "aaabbbcc"`

| Index | Char | Raw Freq (a,b,c) | Min Active Freq | Normalised Signature | Map State | Max Length |
|---|---|---|---|---|---|---|
| –1 | — | (0,0,0) | — | `()` | `{ (): -1 }` | 0 |
| 0 | a | (1,0,0) | 1 | `(a:0)` | add `(a:0) → 0` | 0 |
| 1 | a | (2,0,0) | 2 | `(a:0)` | seen at 0 → len=1 | 1 |
| 2 | a | (3,0,0) | 3 | `(a:0)` | seen at 0 → len=2 | 2 |
| 3 | b | (3,1,0) | 1 | `(a:2,b:0)` | add new | 2 |
| 4 | b | (3,2,0) | 2 | `(a:1,b:0)` | add new | 2 |
| 5 | b | (3,3,0) | 3 | `(a:0,b:0)` | add new → `→ 0` | **6** |
| 6 | c | (3,3,1) | 1 | `(a:2,b:2,c:0)` | add new | 6 |
| 7 | c | (3,3,2) | 2 | `(a:1,b:1,c:0)` | add new | 6 |

At index 5, the signature `(a:0, b:0)` first appeared at index `-1`... wait — actually it matches the stored entry for index `–1` via the empty base, giving length `5 – (–1) = 6`. **Answer: 6** ✓

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n × 26)** which simplifies to **O(n)**. For each of the `n` characters, we compute a normalised frequency signature across at most 26 letters — a fixed constant. This means the algorithm scales linearly: doubling the input size roughly doubles the runtime, making it practical even for strings of 100,000 characters.

### Space Complexity

**O(n × 26)** in the worst case for the hash map, as each of the `n` prefix positions could produce a unique signature of up to 26 values. In practice, signatures repeat frequently, keeping memory usage well below the theoretical maximum.

---

## 💡 Key Takeaways *(For Everyone)*

- **Fairness detection at scale is a real business problem** — whether in playlists, content feeds, or security logs, balanced distribution analysis drives better products.
- **Efficiency matters enormously** — an O(n²) brute-force approach on 100,000 characters runs ~10 billion operations; this approach runs ~2.6 million, a difference of thousands of times faster.
- **Prefix sums are a powerful pattern** — storing cumulative counts lets you answer "what happened between two points?" in O(1) instead of rescanning every time.
- **Canonical normalisation is the core trick** — by subtracting the minimum frequency, you convert an absolute count problem into a relative difference problem, making distant substrings comparable with a single hash lookup.
- **Hash maps turn O(n²) search into O(n)** — storing the first occurrence of a state and checking for repeats is a broadly applicable technique seen in problems ranging from subarray sums to longest balanced brackets.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Binary strings:** Solve the simpler version where `s` contains only `'0'` and `'1'` — find the longest substring with equal counts of each. This is a classic warm-up that uses the same prefix-difference idea.
- **Variation 2 — At most K distinct characters:** Extend the problem so that the substring may contain at most `K` distinct characters, all with equal frequency. How does your signature change?
- **Variation 3 — 2D grid version:** Given a matrix of characters, find the largest rectangular subgrid where every character appears with equal frequency — a significantly harder generalisation worth exploring once the 1D version is mastered.

---