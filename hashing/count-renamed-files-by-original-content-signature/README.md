# Count Renamed Files by Original Content Signature

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, Hash Set, Data Deduplication

---

## 🗂 Problem Overview
Given file records of the form `[name, signature]`, count how many signatures are associated with at least two distinct file names. The signature is a stable content hash, so it defines file identity by contents rather than by name. Exact duplicate records must be ignored. The non-trivial part is scale: with up to 200,000 records, pairwise comparison or repeated scans is too expensive, so grouping and deduplication must happen in near-linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere systems reconcile identity across unstable labels: object storage migrations, backup deduplication, media asset pipelines, package registries, and content-addressed stores. The operational question is rarely “how many rows exist”; it is “how many logical objects map to multiple external identifiers.” Without hash-based grouping, implementations devolve into quadratic joins or expensive sorts, which break under large migrations and replay-heavy event streams. With the right structure, you can cheaply detect renames, aliases, duplicate ingestion, and metadata drift while preserving throughput and making downstream reconciliation deterministic.

## 🔍 Problem Statement
You are given `records`, where each element is a pair `[name, signature]`. `name` is the current file name, and `signature` is a stable hash of the file contents. Multiple records may share the same signature, meaning they represent identical content. Some of those records may use different names because the file was renamed during migration.

Return the number of signatures that are associated with at least two unique file names.

Constraints:
- `1 <= records.length <= 200000`
- Each record is `[name, signature]`
- `1 <= name.length, signature.length <= 100`
- Names and signatures use lowercase letters, digits, `.`, `_`, `-`
- Duplicate pairs may appear and should count once

Examples:

- `records = [["report_v1.pdf","h1"],["report_final.pdf","h1"],["notes.txt","h2"],["notes.txt","h2"],["summary.txt","h3"]]` → `1`
- `records = [["img001.png","x9"],["vacation.png","x9"],["draft.doc","a1"],["draft_v2.doc","a1"],["draft.doc","a1"],["todo.md","b7"],["todo_backup.md","b7"]]` → `3`

The key constraint is input size: near-linear processing is expected.

## 🪜 How to Solve This
1. Read the requirement carefully → we are not counting records, and we are not counting names globally. We are counting **signatures whose associated name set has size at least 2**.

2. That immediately suggests grouping → each signature defines a bucket, so a `HashMap<signature, ...>` is the natural top-level structure.

3. Inside each bucket, duplicates matter → the same `[name, signature]` pair can appear multiple times, but should only contribute one name. That means each signature needs a **set of names**, not a counter.

4. Process each record once:
   - look up the signature
   - insert the name into that signature’s set
   - duplicates collapse automatically

5. After the scan, inspect each signature bucket and count how many sets have size `>= 2`.

6. Why this approach is obvious in hindsight:
   - “same signature” means equivalence class
   - “distinct names” means deduplication
   - equivalence classes + deduplication is the standard `HashMap + HashSet` pattern

This avoids nested comparisons entirely and matches the near-linear expectation.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Hash-based grouping with per-group deduplication.**  
   The problem is fundamentally about partitioning records by `signature` and then measuring the number of unique `name` values in each partition. A hash map gives constant-average-time access to each partition; a hash set removes duplicate `(name, signature)` records naturally.

2. **Initialize a map from signature to set of names.**  
   Conceptually: `groups[signature] = {all unique names seen for this signature}`.  
   Invariant: after processing the first `i` records, every map entry contains exactly the distinct names observed so far for that signature.

3. **Scan the input once.**  
   For each `[name, signature]`:
   - create the set if this is the first time the signature appears
   - insert `name` into that set  
   Why correct: insertion into a set is idempotent, so repeated identical records do not inflate the count.

4. **Count qualifying signatures.**  
   Iterate over all map entries and increment the answer when the set size is at least 2.  
   Why correct: the problem asks whether a signature appears under two or more distinct names, not how many extra names it has. Each qualifying signature contributes exactly once.

5. **Maintain the right abstraction boundary.**  
   Do not track global name frequencies or pair frequencies unless optimizing for memory in a specialized way. The correctness condition is local to each signature bucket.

6. **Result.**  
   Return the number of signature groups whose deduplicated name set size is `>= 2`.  
   This is the minimal direct implementation of the grouping + deduplication pattern.

## 📊 Worked Example
Use:

`[["img001.png","x9"],["vacation.png","x9"],["draft.doc","a1"],["draft_v2.doc","a1"],["draft.doc","a1"],["todo.md","b7"],["todo_backup.md","b7"]]`

| Step | Record | Map State |
|---|---|---|
| 1 | `["img001.png","x9"]` | `x9 -> {img001.png}` |
| 2 | `["vacation.png","x9"]` | `x9 -> {img001.png, vacation.png}` |
| 3 | `["draft.doc","a1"]` | `a1 -> {draft.doc}`, `x9 -> {...}` |
| 4 | `["draft_v2.doc","a1"]` | `a1 -> {draft.doc, draft_v2.doc}` |
| 5 | `["draft.doc","a1"]` | unchanged; duplicate pair |
| 6 | `["todo.md","b7"]` | `b7 -> {todo.md}` |
| 7 | `["todo_backup.md","b7"]` | `b7 -> {todo.md, todo_backup.md}` |

Final set sizes:
- `x9`: 2
- `a1`: 2
- `b7`: 2

All three signatures have at least two distinct names, so the answer is `3`.

## ⏱ Complexity Analysis
### Time Complexity
Average-case **O(n)**, where `n` is the number of records. Each record performs one hash map lookup and one hash set insertion, both constant time on average. At `10^6` records this remains practical; at `10^9`, the bottleneck becomes memory bandwidth and storage layout rather than asymptotic behavior.

### Space Complexity
**O(u)**, where `u` is the number of unique `(signature, name)` associations retained across all sets. The map and its nested sets own the space. You can reduce memory by storing only up to two names per signature, but that trades generality for a problem-specific optimization.

## 💡 Key Takeaways
- If the prompt says “group by X, then count distinct Y per group,” think `HashMap<X, HashSet<Y>>` before considering sorting or nested scans.
- If duplicate records exist but should not affect the result, a set is usually part of the core model, not an afterthought.
- Do not count raw record frequency per signature; repeated identical pairs must not make a signature qualify.
- Do not increment the answer every time you see a second record for a signature; the threshold is based on the second **distinct name**, not the second occurrence.
- In production systems, stable content identifiers let you separate logical identity from mutable labels, which is the basis of reliable deduplication and reconciliation.

## 🚀 Variations & Further Practice
- Return the actual signatures or grouped file names instead of just the count; same pattern, but now output size and ordering requirements matter.
- Process the records as a stream with bounded memory; harder because you may need approximate distinct counting or early-threshold tracking instead of full sets.
- Generalize from “at least two names” to “top-k signatures by number of distinct names”; this adds ranking and may require heap-based post-processing.