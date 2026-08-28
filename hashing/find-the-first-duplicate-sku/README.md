# Find the First Duplicate SKU

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Array, String

---

## 🗂 Problem Overview
Given an ordered list of scanned SKU strings, return the first SKU whose **second occurrence** appears earliest while scanning left to right. If no SKU repeats, return an empty string. The challenge is not correctness but efficiency: with up to 100,000 SKUs, pairwise comparison is wasteful. The right solution tracks previously seen values in constant expected time using a hash-based set, preserving scan order and enabling a single pass.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect the earliest repeated event in an ordered stream: warehouse receiving pipelines, idempotency-key validation, fraud detection, log deduplication, compiler symbol resolution, and stream processors such as Kafka consumers or Flink jobs. At scale, the wrong approach turns linear ingestion into quadratic work, which destroys throughput and increases tail latency. A hash-backed membership check enables real-time duplicate detection with predictable behavior, low implementation complexity, and a clean path from in-memory processing to distributed stateful stream operators when the event volume outgrows a single process.

## 🔍 Problem Statement
You are given an array `skus` where each element is a SKU code scanned at a warehouse receiving station, in exact scan order. Each SKU is a case-sensitive string containing English letters, digits, or hyphens. Return the first SKU that appears more than once, where “first” is defined by the earliest **second appearance** encountered during a left-to-right scan. If no SKU repeats, return `""`.

Constraints:

- `1 <= skus.length <= 100000`
- `1 <= skus[i].length <= 50`
- `skus[i]` contains only letters, digits, and `-`
- Comparison is case-sensitive

Examples:

- `["BX-12", "A7", "Q9", "A7", "BX-12"]` → `"A7"`
- `["P1", "R2", "S3", "T4"]` → `""`

The key constraint is input size: `100000` elements rules out nested scans and strongly suggests an `O(n)` hashing approach.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find any duplicate” and not “find the smallest repeated value.” It is specifically the first value whose **repeat is observed earliest** in scan order.

2. That wording implies we should process the array exactly as the warehouse scanned it: left to right. Any reordering, such as sorting, would destroy the meaning of “first duplicate.”

3. For each SKU, we need one question answered fast: **have I seen this exact string before?** That is a membership test, which is the canonical use case for a hash set.

4. Start with an empty set called `seen`.

5. Iterate through `skus`:
   - If the current SKU is already in `seen`, return it immediately. Its second appearance is the earliest one encountered so far by construction.
   - Otherwise, add it to `seen` and continue.

6. If the loop finishes, no SKU repeated, so return `""`.

This is the standard single-pass hashing pattern: preserve order through iteration, preserve history through a set, and stop at the first repeated observation.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Hash Set / Single-Pass Membership Check.**  
   The problem asks for repeated detection in an ordered sequence, with no need to count frequencies in advance. A hash set is the right abstraction because it answers “seen before?” in expected `O(1)` time.

2. **Initialize an empty set `seen`.**  
   This set represents the invariant: after processing index `i - 1`, `seen` contains every distinct SKU from positions `0..i-1`.

3. **Scan the array from left to right.**  
   Order matters because “first duplicate” is defined by the earliest second occurrence, not by lexical order or first repeated value globally.

4. **For each `sku`, test membership in `seen`.**  
   If `sku` is already present, return it immediately. This is correct because the current index is the first point in the scan where a duplicate has been observed before any later element could qualify.

5. **If not present, insert `sku` into `seen`.**  
   This preserves the invariant for the next iteration: all SKUs encountered so far are now recorded.

6. **If the loop completes, return `""`.**  
   That means no membership check ever succeeded, so no SKU appeared twice.

Why this is correct: the algorithm returns at the earliest index `j` such that `skus[j]` appeared at some earlier index `i < j`. Since the scan is left to right, no duplicate with a smaller second-occurrence index can be skipped. That exactly matches the problem definition.

## 📊 Worked Example
Use `skus = ["BX-12", "A7", "Q9", "A7", "BX-12"]`.

| Step | Current SKU | `seen` before check        | Duplicate? | Action              |
|------|-------------|----------------------------|------------|---------------------|
| 1    | `BX-12`     | `{}`                       | No         | Add `BX-12`         |
| 2    | `A7`        | `{BX-12}`                  | No         | Add `A7`            |
| 3    | `Q9`        | `{BX-12, A7}`              | No         | Add `Q9`            |
| 4    | `A7`        | `{BX-12, A7, Q9}`          | Yes        | Return `A7`         |

Trace reasoning:

- `BX-12` is first seen, so it cannot be a duplicate yet.
- `A7` is also new.
- `Q9` is also new.
- The next `A7` is already in `seen`, so its second occurrence happens here.
- Even though `BX-12` also repeats later, its second appearance occurs after `A7`, so it is not the answer.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time, where `n` is the number of SKUs. Each element is processed once, and each hash-set lookup/insert is expected constant time. At `10^6` elements this remains practical in-memory; at `10^9`, the algorithmic shape is still right, but memory distribution and streaming/state partitioning become the real constraints.

### Space Complexity
`O(k)` space, where `k` is the number of distinct SKUs seen before termination, worst-case `O(n)`. The hash set owns nearly all extra memory. You can reduce space only by trading away exactness or ordering guarantees, for example with probabilistic structures that admit false positives.

## 💡 Key Takeaways
- If the problem says “first repeated while scanning left to right,” preserve input order and think single-pass state, not sorting.
- If each step asks “have I seen this exact value before?”, a hash set is usually the default primitive.
- Return on the first successful membership check; do not keep scanning for “more duplicates,” or you risk solving the wrong problem.
- The duplicate is defined by the earliest **second occurrence**, not the earliest first occurrence among values that eventually repeat.
- In production systems, this is the core shape of exact online deduplication: ordered ingestion plus constant-time state lookup.

## 🚀 Variations & Further Practice
- Return the index of the first duplicate’s second occurrence instead of the SKU itself; same pattern, but forces precision about what “first” means.
- Find all duplicate SKUs with counts, preserving first-seen order; this extends the set into a hash map and separates detection from aggregation.
- Detect duplicates within a sliding window of the last `k` scans; the twist is bounded state, requiring a hash set plus queue/deque or indexed eviction logic.