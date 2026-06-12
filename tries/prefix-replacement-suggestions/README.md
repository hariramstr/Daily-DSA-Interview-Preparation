# Prefix Replacement Suggestions

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Tries &nbsp;|&nbsp; **Tags:** Trie, String, Prefix Matching

---

## 🗂 Problem Overview
Given a dictionary of root words and a list of query words, replace each query with the shortest root that is its prefix. If no root matches, keep the original word. Return both the transformed list and the number of words replaced. The non-trivial part is doing prefix lookup efficiently across up to `2 * 10^5` total characters, while always preferring the shortest valid match and ignoring duplicate roots.

## 🌍 Engineering Impact
This pattern shows up in search normalization, query rewriting, autocomplete backends, lexical analyzers, routing tables, and policy engines. The core requirement is low-latency prefix resolution over a shared vocabulary, often on hot paths. A naive scan across all roots per query collapses under scale because lookup cost grows with dictionary size. A Trie changes the cost model: work becomes proportional to query length, not corpus size. That enables predictable latency, early termination on the first valid root, and cleaner extension to ranked suggestions, token pipelines, and online dictionary updates.

## 🔍 Problem Statement
You are given two arrays of lowercase strings: `roots` and `queries`. For each word in `queries`, find whether any dictionary root is a prefix of that word. If multiple roots match, choose the **shortest** matching root. Replace the query with that root; otherwise leave it unchanged. Return both the transformed array and the number of replacements performed.

Constraints:
- `1 <= roots.length, queries.length <= 2 * 10^4`
- `1 <= roots[i].length, queries[i].length <= 100`
- Total characters across all strings `<= 2 * 10^5`
- Roots may contain duplicates

Examples:

- `roots = ["cat", "bat", "rat"]`
- `queries = ["cattle", "battery", "rattle", "dog"]`
- Result: `["cat", "bat", "rat", "dog"]`, `3`

- `roots = ["a", "ab", "abc", "bcd"]`
- `queries = ["abcde", "abacus", "bcdx", "zzz"]`
- Result: `["a", "a", "bcd", "zzz"]`, `3`

The key constraint is repeated prefix matching over many strings. That rules out nested scans and points directly to a Trie.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not general substring search; it is **prefix-only** matching.
2. Notice the tie-break rule → if several roots match, we need the **shortest** one, not the longest or arbitrary.
3. Ask what data structure supports repeated prefix traversal efficiently → a Trie is the natural fit because each edge corresponds to the next character.
4. Insert every root into the Trie, marking terminal nodes. Duplicates do not matter because marking the same terminal twice changes nothing.
5. For each query, walk the Trie character by character:
   - If traversal breaks, no root matches → keep the original word.
   - If you hit a terminal node, stop immediately → that is the shortest matching root by construction.
6. Count replacements only when the returned prefix differs from the original query.
7. The mental model is simple: build once, then answer each lookup in time proportional to the query length, with early exit as soon as correctness is guaranteed.

## 🧩 Algorithm Walkthrough
1. **Build a Trie from `roots`**  
   Use the standard **Trie / Prefix Tree** pattern. Each node stores child pointers for letters and a terminal flag indicating that a root ends here. This is correct because every root maps to exactly one path from the root node.

2. **Deduplicate implicitly during insertion**  
   If the same root appears multiple times, insertion traverses the same path and sets the same terminal flag. The invariant is that each terminal node represents the existence of a root, not its multiplicity.

3. **Process each query with prefix traversal**  
   Start at the Trie root and scan characters left to right. If the next child does not exist, no dictionary root is a prefix of this query. The invariant is that the current Trie node corresponds to the prefix consumed so far.

4. **Stop on the first terminal node**  
   The moment traversal reaches a terminal node, return the consumed prefix. This is the key correctness property: because traversal is left to right, the first terminal encountered is the shortest matching root.

5. **Fallback when traversal fails or finishes without terminal**  
   If no terminal node is reached, return the original query unchanged. This preserves semantics for words with no approved root.

6. **Accumulate results and replacement count**  
   Append the chosen string to the output array. Increment `replacedCount` only if a root was found and used. The final invariant is that the output array is positionally aligned with `queries`, and the count equals the number of successful prefix substitutions.

## 📊 Worked Example
Example: `roots = ["a", "ab", "abc", "bcd"]`, `queries = ["abcde", "abacus", "bcdx", "zzz"]`

| Query    | Traversal path | First terminal hit | Output | Replaced? |
|----------|----------------|--------------------|--------|-----------|
| `abcde`  | `a` → `b` → `c` | `a`                | `a`    | Yes       |
| `abacus` | `a` → `b`       | `a`                | `a`    | Yes       |
| `bcdx`   | `b` → `c` → `d` | `bcd`              | `bcd`  | Yes       |
| `zzz`    | `z` missing      | none               | `zzz`  | No        |

Trace:
1. Insert all roots into the Trie.
2. For `abcde`, stop immediately at terminal node `a`; do not continue to `ab` or `abc`.
3. For `abacus`, same logic: shortest match wins.
4. For `bcdx`, first terminal appears at `bcd`.
5. For `zzz`, traversal fails at the first character.

Final result: transformed `["a", "a", "bcd", "zzz"]`, `replacedCount = 3`.

## ⏱ Complexity Analysis
### Time Complexity
Building the Trie costs `O(sum(len(root)))`. Query processing costs `O(sum(len(query)))`, with early termination when a terminal root is found or traversal fails. Overall: `O(total characters)`. At `10^6` characters this is comfortably linear; at `10^9`, linear still dominates, so memory layout and cache behavior become the real constraints.

### Space Complexity
The Trie uses `O(sum(len(root)))` space in the worst case, bounded by the total number of inserted characters. This can be reduced with compressed tries or hash-based child maps, but that trades simpler traversal and predictable constant factors for lower memory density.

## 💡 Key Takeaways
- If the problem says “find a prefix among many dictionary entries” and lookup repeats across many inputs, think Trie before considering nested scans.
- If the rule says “shortest matching prefix,” that is a strong signal that early termination on the first terminal Trie node is the core invariant.
- Do not keep traversing after reaching a terminal node; that would bias toward longer matches and violate the problem contract.
- Count replacements only when a prefix match exists; identical strings are still replacements if they came from a terminal root equal to the full query.
- In production systems, Tries matter because they convert vocabulary-size-dependent lookup into input-length-dependent lookup, which is the difference between scalable normalization and latency cliffs.

## 🚀 Variations & Further Practice
- Return the **longest** matching root instead of the shortest; same Trie, different stopping rule, and correctness now depends on scanning as far as possible while remembering the last terminal.
- Support dynamic inserts and deletes of roots in an online service; the harder part is maintaining correctness and memory efficiency under concurrent updates.
- Extend from exact prefix replacement to ranked suggestions or fuzzy prefix matching; now the Trie is only the index, and scoring / edit-distance logic becomes the dominant complexity.