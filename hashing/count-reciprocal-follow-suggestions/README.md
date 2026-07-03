# Count Reciprocal Follow Suggestions

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** hashing, set, graph

---

## 🗂 Problem Overview
Given up to 200,000 directed follow relationships `[a, b]`, count how many unordered user pairs `{u, v}` have exactly one unique direction present: `u -> v` or `v -> u`, but not both. Duplicate edges must be ignored, and self-follows `[x, x]` do not participate at all. The non-trivial part is deduplicating directed edges while evaluating unordered pairs only once, without falling into an `O(n²)` pairwise comparison.

## 🌍 Engineering Impact
This pattern shows up anywhere systems ingest directed interactions and need pair-level reconciliation: social graph suggestion pipelines, fraud/risk link analysis, messaging reciprocity metrics, ACL diffing, and stream processors that collapse duplicate events before aggregation. At scale, the wrong approach explodes on cardinality: nested comparisons are dead on arrival, and failing to normalize keys causes double counting and inconsistent metrics across shards. Hash-based deduplication plus canonical pair grouping enables linear-time aggregation, predictable memory behavior, and composable downstream logic for recommendation, ranking, and graph analytics.

## 🔍 Problem Statement
You are given `relationships`, where each element is a directed edge `[a, b]` meaning user `a` follows user `b`. Count the number of unordered pairs of distinct users `{u, v}` such that exactly one unique directed edge exists between them after deduplication.

Rules:
- Ignore duplicates: repeated `[a, b]` counts once.
- Ignore self-follows: `[x, x]` contributes nothing.
- Count each unordered pair at most once.
- A pair contributes if it has one direction only, not both.

Constraints:
- `1 <= relationships.length <= 200000`
- `relationships[i].length == 2`
- `1 <= a, b <= 10^9`

Examples:

- `[[1,2],[2,1],[1,3],[4,5],[4,5],[6,7]] -> 3`
- `[[10,20],[20,30],[30,20],[40,40],[50,60],[60,50],[70,80],[80,90]] -> 3`

The key constraint is input size: `200k` edges means the solution must be near-linear and hash-driven.

## 🪜 How to Solve This
1. Read the problem carefully → this is not about users individually; it is about **unordered pairs of users**.
2. Notice the edge cases → duplicates must collapse, and self-loops must disappear before any counting.
3. For each valid directed edge `(a, b)`, ask: what pair does it belong to?  
   → The unordered pair is `(min(a, b), max(a, b))`.
4. But direction still matters. For a normalized pair, there are only two possible unique directions:
   - low → high
   - high → low
5. That means each unordered pair can be represented by a tiny state:
   - one direction seen
   - the other direction seen
   - or both
6. This immediately suggests hashing:
   - use a set to deduplicate directed edges
   - use a map keyed by normalized unordered pair to track direction presence
7. After one pass, count how many pairs have exactly one direction bit set.

The mental move is simple: stop thinking in terms of edges globally, and start thinking in terms of **canonical pair buckets with directional state**.

## 🧩 Algorithm Walkthrough
1. **Deduplicate directed edges with hashing.**  
   Use a hash set of `(a, b)` to ensure repeated follows do not affect the result. This is required because duplicates are semantically a single relationship. Invariant: every directed edge is processed at most once.

2. **Ignore self-follows immediately.**  
   If `a == b`, skip the edge. A self-loop cannot create a reciprocal suggestion and should not allocate state. Invariant: all remaining edges connect two distinct users.

3. **Canonicalize the unordered pair.**  
   For each unique directed edge `(a, b)`, compute `x = min(a, b)` and `y = max(a, b)`. This normalized key ensures both `(a, b)` and `(b, a)` map to the same pair bucket. Invariant: all information for pair `{a, b}` lives in exactly one map entry.

4. **Track direction with a compact bitmask.**  
   If the original edge is `x -> y`, set bit `01`; if it is `y -> x`, set bit `10`. Store this in a hash map from pair key to mask. After processing, each pair has state:
   - `01`: one direction only
   - `10`: the opposite direction only
   - `11`: both directions present  
   This is the core **hashing + canonicalization** pattern.

5. **Count masks with exactly one bit set.**  
   Iterate through the map values and count entries equal to `01` or `10`. Those are exactly the unordered pairs with one unique directed relationship. Invariant: each qualifying pair is counted once, regardless of input duplication or ordering.

This abstraction is correct because every valid input edge contributes to exactly one normalized pair, and the bitmask fully captures the only state that matters: one direction or both.

## 📊 Worked Example
Use `relationships = [[1,2],[2,1],[1,3],[4,5],[4,5],[6,7]]`.

| Step | Edge   | Action                         | Pair Key | Mask After |
|------|--------|--------------------------------|----------|------------|
| 1    | [1,2]  | unique, valid                  | (1,2)    | `01`       |
| 2    | [2,1]  | reverse direction for same pair| (1,2)    | `11`       |
| 3    | [1,3]  | unique, valid                  | (1,3)    | `01`       |
| 4    | [4,5]  | unique, valid                  | (4,5)    | `01`       |
| 5    | [4,5]  | duplicate directed edge, skip  | —        | unchanged  |
| 6    | [6,7]  | unique, valid                  | (6,7)    | `01`       |

Final pair states:
- `(1,2) -> 11` → both directions, do not count
- `(1,3) -> 01` → count
- `(4,5) -> 01` → count
- `(6,7) -> 01` → count

Total reciprocal follow suggestions = **3**.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)` expected time, where `n` is the number of input relationships. Each edge is processed once, with constant-time expected hash set and hash map operations. At `10^6` scale this is still practical; at `10^9`, the algorithmic shape is right but memory and distributed partitioning become the real constraints.

### Space Complexity
`O(u)` space, where `u` is the number of unique non-self directed edges or normalized pairs, whichever dominates. The hash set and pair-state map own the memory. You can reduce space slightly by folding deduplication into the pair-state logic, but only if duplicate handling is encoded carefully.

## 💡 Key Takeaways
- If the problem says “count unordered pairs” but the input is directional, normalize the pair key first and store direction separately.
- If duplicates are explicitly irrelevant, that is a strong signal for a hash set or hash map with canonical keys.
- Forgetting to ignore self-follows will create bogus pair state and inflate counts.
- Counting directed edges instead of unordered pairs will double count cases like `(u, v)` and `(v, u)`.
- Canonicalization plus compact state encoding is a production-grade pattern for collapsing noisy event streams into stable aggregate facts.

## 🚀 Variations & Further Practice
- Count pairs with **at least `k` interactions in one direction** before considering reciprocity; the twist is tracking multiplicity instead of pure presence.
- Process the same logic in a **streaming or windowed system**; the harder part is expiring old edges while preserving deduplication semantics.
- Extend from pair counting to **suggestion generation per user**; now you need adjacency structures and potentially ranking logic on top of the same normalized edge model.