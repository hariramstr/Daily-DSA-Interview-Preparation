# Wildcard Query Frequency in Log Stream

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Tries &nbsp;|&nbsp; **Tags:** Tries, Backtracking, Design, String Matching

---

## 🗂 Problem Overview

Build a data structure over a live log stream that supports two operations: `insert(word)` to record a new log entry, and `query(pattern)` to return both the count of all inserted words matching a wildcard pattern and the insertion index of the lexicographically smallest match. The `?` wildcard matches exactly one character, and patterns must match full words. The non-trivial constraint is that queries arrive interleaved with inserts, so you cannot precompute matches offline — the index space grows dynamically.

---

## 🌍 Engineering Impact

This pattern is the backbone of log aggregation systems like Elasticsearch's wildcard queries, Splunk's field-pattern matching, and API gateway route matching (e.g., AWS API Gateway, Nginx `location` blocks). At scale, naive linear scan over a growing corpus breaks down: a 50K-entry log stream with 50K queries is 2.5B character comparisons in the worst case. Trie-based structures reduce per-query traversal to `O(26^d · L)` where `d` is wildcard count, enabling sub-millisecond p99 latency on pattern-heavy workloads that would otherwise saturate CPU under load.

---

## 🔍 Problem Statement

Given a stream of lowercase alphabetic strings, implement `LogStream` with:

- `insert(word)`: Records the word with a 0-based insertion index (duplicates allowed).
- `query(pattern)`: Returns `[count, index]` — count of all inserted words matching the pattern, and the insertion index of the lexicographically smallest match (`-1` if none).

`?` matches exactly one lowercase letter; patterns match entire words, not substrings.

**Constraints:** word/pattern length ≤ 20; ≤ 5×10⁴ combined operations; all word characters are lowercase a–z; patterns may contain `?`.

**Key driver:** The combination of wildcard branching mid-pattern, full-word matching, and the requirement to track both frequency and lex-minimum index simultaneously rules out hash maps and demands a traversal-based structure.

**Example:**
```
insert("apple")→0, insert("apply")→1, insert("apt")→2
query("ap?le") → [2, 0]   // "apple","apply" match; "apple" is lex smallest at index 0
query("a??")   → [1, 2]   // only "apt" matches; index 2
```

---

## 🪜 How to Solve This

1. **Read the constraints** → word length ≤ 20, up to 5×10⁴ ops. Brute force (scan all inserted words per query) is O(N·L) per query → up to 10⁸ ops total. Tight but possibly acceptable — however, the wildcard matching per word is itself O(L) with backtracking, so real cost is worse. Look for a smarter structure.

2. **Pattern matching over strings with wildcards** → think Trie. A Trie encodes all inserted words as shared prefix paths, so a single traversal can match many words simultaneously without re-examining each word independently.

3. **Wildcard `?` at a node** → instead of following one child edge, follow all 26. This is the branching point. Depth-first traversal with backtracking naturally handles this — at each `?`, recurse into every non-null child.

4. **Need count AND lex-minimum index** → store both at each terminal node. Count is straightforward. For lex-minimum: since Trie children are ordered (array index 0='a' to 25='z'), a left-to-right DFS naturally visits words in lexicographic order — the first terminal hit during DFS is the lex smallest match.

5. **Duplicate words** → each insertion gets its own index. Terminal nodes must store a list (or count + min-index) rather than a single value.

The insight: **the Trie turns a per-word scan into a shared-prefix traversal**, and wildcard branching is just "recurse into all children" — backtracking handles the combinatorics cleanly.

---

## 🧩 Algorithm Walkthrough

**Pattern: Trie with DFS Backtracking**

**Trie Node structure:**
- `children[26]`: child pointers indexed by character offset.
- `count`: number of words terminating here (handles duplicates).
- `minIndex`: smallest insertion index among all words terminating here (`Integer.MAX_VALUE` if none).

**`insert(word)`:**
1. Walk the Trie character by character, creating nodes as needed.
2. At the terminal node, increment `count` and update `minIndex = min(minIndex, currentInsertionIndex)`.
3. Increment the global insertion counter.

**`query(pattern)`:**
1. Launch a recursive DFS: `dfs(node, patternIndex)` returning `[count, minIndex]`.
2. **Base case:** `patternIndex == pattern.length` → if `node.count > 0`, return `[node.count, node.minIndex]`; else `[0, -1]`.
3. **Literal character:** follow the single matching child (if non-null), recurse.
4. **`?` wildcard:** iterate all 26 children; for each non-null child, recurse and accumulate: `totalCount += childResult[0]`, `globalMin = min(globalMin, childResult[1])`.
5. Return accumulated `[totalCount, globalMin == MAX_VALUE ? -1 : globalMin]`.

**Invariant maintained:** Every path from root to a terminal node represents exactly one distinct word-form; duplicate insertions are collapsed into `count` and `minIndex` at the terminal, keeping the Trie structure clean and traversal cost independent of duplicate frequency.

---

## 📊 Worked Example

Using Example 2: inserts `"bat"`(idx 0), `"bad"`(idx 1), then `query("b?d")`, then insert `"bed"`(idx 3), then `query("b?d")`.

**Trie after inserting "bat", "bad":**

| Node Path | count | minIndex |
|-----------|-------|----------|
| root→b→a→t | 1 | 0 |
| root→b→a→d | 1 | 1 |

**`query("b?d")` — DFS trace:**

| Step | patternIdx | node | action | result |
|------|-----------|------|--------|--------|
| 1 | 0 (`b`) | root | follow `children['b'-'a']` | → b-node |
| 2 | 1 (`?`) | b-node | iterate all 26 children; only `a` non-null | → recurse into a-node |
| 3 | 2 (`d`) | a-node | follow `children['d'-'a']` | → ad-node |
| 4 | 3 (end) | ad-node | count=1, minIndex=1 | return [1, 1] |

**Result:** `[1, 1]` ✓. After inserting `"bed"` (idx 3), the `b→e→d` path gets count=1, minIndex=3. Re-running `query("b?d")` now accumulates `[1,1]` from `b→a→d` and `[1,3]` from `b→e→d` → **`[2, 1]`** ✓.

---

## ⏱ Complexity Analysis

### Time Complexity

**Insert:** O(L) — single root-to-leaf walk, L ≤ 20, effectively O(1).  
**Query:** O(26^w · L) where `w` is the number of `?` wildcards in the pattern. Worst case: pattern is all `?`s of length 20 → 26^20, but in practice `w` is small and the Trie is sparse. At 5×10⁴ queries with typical `w ≤ 3`, this is well within budget.

### Space Complexity

O(N · L · 26) for the Trie — each of N inserted words contributes at most L new nodes, each holding a 26-pointer array. At N=5×10⁴, L=20, this is ~26M pointers. Replacing the fixed array with a `HashMap<Character, Node>` per node reduces space proportional to actual alphabet usage at the cost of ~2–3× slower child lookup.

---

## 💡 Key Takeaways

- **Signal — "match a pattern against a dynamic set of strings":** When you see wildcard or prefix matching over a corpus that grows at runtime, a Trie is almost always the right first instinct; hash maps can't handle structural pattern matching without enumerating candidates.
- **Signal — "return aggregate stats over all matches":** When a query must aggregate (count, min, max) across all matching entries rather than just confirm existence, augment Trie nodes with the aggregate state rather than collecting matches and post-processing.
- **Gotcha — duplicate insertions:** The problem explicitly allows non-unique words. A naive Trie that stores a single terminal flag will silently under-count. Terminal nodes must store `count` (not a boolean) and `minIndex` (not the current index, since a later duplicate has a higher index and must not overwrite).
- **Gotcha — `minIndex` initialization:** Initialize `minIndex` to `Integer.MAX_VALUE`, not `-1`. Using `-1` as a sentinel in `min()` comparisons will corrupt the result since `-1 < 0 < any valid index`. Only convert to `-1` at the final return boundary.
- **Architectural insight:** Augmenting Trie nodes with pre-aggregated metadata (count, min-index, frequency histograms) is the same pattern used in production autocomplete engines and search indexes — push computation into the write path so the read path stays fast, trading insert overhead for query throughput.

---

## 🚀 Variations & Further Practice

- **Multi-character wildcard (`*`):** Replace `?` (exactly one char) with `*` (zero or more chars), as in shell glob or regex `.*`. This introduces the possibility of matching across variable-depth Trie levels, requiring memoization or dynamic programming over `(nodePointer, patternIndex)` pairs to avoid exponential re-traversal — the same state-space explosion that makes regex NFA simulation non-trivial.
- **Top-K lexicographic matches instead of just the minimum:** Return the K smallest matching words per query. Each Trie node must maintain a sorted structure (e.g., a bounded min-heap of size K) of terminal indices in its subtree, updated on every insert — this forces you to reason about maintaining order invariants through a tree structure under concurrent mutation, a common problem in ranked autocomplete systems.
- **Pattern frequency with expiry (sliding window):** Extend the design so that words inserted more than T operations ago no longer count toward query results. This invalidates static `count`/`minIndex` aggregates and requires either a time-ordered auxiliary structure (e.g., a deque per terminal node) or a segment-tree-augmented Trie — the core challenge in streaming analytics systems with retention windows.