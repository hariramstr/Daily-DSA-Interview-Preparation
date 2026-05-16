# Auto-Complete Sentence Builder

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Tries &nbsp;|&nbsp; **Tags:** Trie, String, Prefix Matching

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you start typing a message and your phone or computer suggests how to finish your sentence. This problem asks us to build exactly that feature. Given a list of sentences someone has typed before, and the first few characters they are currently typing, find and return every previous sentence that starts with those characters — in the order they were originally written.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Auto-complete is everywhere — Google Search, Gmail's Smart Compose, IDE code editors, and e-commerce search bars all rely on this technology. A fast, accurate auto-complete directly improves user experience by reducing keystrokes, cutting errors, and keeping users engaged longer. For businesses, this translates to higher conversion rates on search-driven platforms and lower support costs when users find what they need faster. Even a small improvement in search responsiveness can meaningfully increase revenue on high-traffic products.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a physical filing cabinet where every folder is labeled with a full sentence. When someone walks up and says, "I'm looking for anything that starts with 'hel'," your job is to quickly flip through the folders and hand back every one whose label begins with those exact letters — in the same order the folders were originally filed. No guessing, no rearranging, just precise prefix matching in original sequence.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a list of previously typed sentences and a partial input string, return all sentences that begin with the exact characters of the partial input (case-sensitive), preserving original insertion order. Duplicate sentences must each be returned individually. The solution must use a **Trie** data structure for storage and retrieval.

**Constraints:**
- `1 <= sentences.length <= 200`
- `1 <= sentences[i].length <= 100`
- `1 <= partial.length <= 50`
- All characters are printable ASCII (letters, digits, spaces, punctuation)

**Examples:**

| Input | partial | Output |
|---|---|---|
| `["hello world", "hello there", "help me", "goodbye"]` | `"hel"` | `["hello world", "hello there", "help me"]` |
| `["apple pie", "apple juice", "banana split", "apple"]` | `"apple"` | `["apple pie", "apple juice", "apple"]` |

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Build the Trie node structure.** Each node stores a dictionary of child nodes (one per character) and a list of sentence indices that pass through it. Using indices rather than full strings avoids duplicating data at every node.

2. **Insert each sentence into the Trie.** For every sentence, walk character by character from the root. At each node along the path, append the sentence's insertion index to that node's index list. This records "this sentence passes through here," enabling fast prefix lookup later.

3. **Handle spaces and punctuation naturally.** Because Trie nodes use a dictionary keyed by any character, spaces and punctuation are treated identically to letters — no special casing required.

4. **Search using the partial input.** Starting at the root, follow the Trie edges character by character along the partial string. If any character is missing as a child, immediately return an empty list — no sentences can match.

5. **Collect results at the terminal node.** Once all characters of the partial string are consumed, the current node's index list contains the insertion indices of every matching sentence. Because we inserted sentences in order and appended indices sequentially, this list is already in insertion order.

6. **Map indices back to sentences and return.** Use the collected indices to retrieve the original sentence strings and return them.

---

## 📊 Worked Example *(For Developers)*

**Input:** `sentences = ["hello world", "hello there", "help me", "goodbye"]`, `partial = "hel"`

| Step | Action | State |
|---|---|---|
| Insert index 0 | Walk `h→e→l→l→o→ →w→o→r→l→d`, append `0` at each node | Nodes for `h`, `he`, `hel`, … each hold `[0]` |
| Insert index 1 | Walk `h→e→l→l→o→ →t→h→e→r→e`, append `1` | Nodes for `h`, `he`, `hel` now hold `[0, 1]` |
| Insert index 2 | Walk `h→e→l→p→ →m→e`, append `2` | Nodes for `h`, `he`, `hel` now hold `[0, 1, 2]` |
| Insert index 3 | Walk `g→o→o→d→b→y→e`, append `3` | `h` node unaffected; `g` node holds `[3]` |
| Search `"hel"` | Follow `h→e→l`; node exists | Index list at `l` node = `[0, 1, 2]` |
| Return results | Map indices → sentences | `["hello world", "hello there", "help me"]` ✅ |

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

- **Build:** O(N × L) where N is the number of sentences and L is the average sentence length — each character of each sentence is visited once.
- **Search:** O(P + K) where P is the length of the partial string and K is the number of matching sentences.

At scale with hundreds of sentences, search remains nearly instant regardless of how many sentences are stored.

### Space Complexity

O(N × L) for the Trie nodes in the worst case, where every sentence shares no common prefixes. In practice, shared prefixes (e.g., many sentences starting with "the") collapse into shared nodes, significantly reducing memory usage compared to storing sentences in a flat list with repeated prefix characters.

---

## 💡 Key Takeaways *(For Everyone)*

- **Auto-complete is a competitive advantage** — faster, smarter suggestions directly improve user retention and satisfaction in any text-driven product.
- **Prefix matching at scale is a solved problem** — Tries are the industry-standard tool, used in search engines, spell checkers, and command-line tools worldwide.
- **Tries trade memory for speed** — storing sentences in a Trie uses more memory than a plain list, but makes prefix searches dramatically faster as the dataset grows.
- **Insertion order is preserved by design** — appending index numbers sequentially means no sorting step is ever needed, keeping the algorithm efficient.
- **Any character can be a Trie edge** — because nodes use a dictionary, spaces and punctuation require zero special handling, making the structure flexible for real-world text.

---

## 🚀 Try It Yourself *(For Developers)*

- **Frequency-ranked results:** Modify the Trie so each node tracks how many times each sentence was typed, then return matches sorted by frequency (most-typed first) — this is closer to how real auto-complete systems work.
- **Case-insensitive matching:** Normalize characters to lowercase on insert and search, then return the original-cased sentences — explore how this changes node structure and lookup logic.
- **Delete a sentence:** Implement a `delete(sentence)` method that removes a sentence from the Trie without rebuilding the entire structure — this is a classic Trie challenge that deepens understanding of node lifecycle management.

---