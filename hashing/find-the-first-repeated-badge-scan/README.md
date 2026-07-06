# Find the First Repeated Badge Scan

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Table, Array, String

---

## 🗂 Problem Overview
Given an ordered list of badge scan IDs, return the first ID whose second occurrence is encountered while scanning left to right. If every ID is unique, return an empty string. The output is the repeated ID itself, not its position. The non-trivial part is preserving stream order while detecting duplicates efficiently: a naive pairwise comparison is quadratic, which does not scale to the input limit of 100,000 scans.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect repeats in an ordered event stream with low latency: API idempotency keys, fraud detection on transaction feeds, duplicate log suppression, message deduplication in Kafka consumers, and identity/access systems processing badge or token events. At small scale, nested scans are tolerable; at production scale, they collapse under throughput and tail-latency pressure. A hash-backed seen set turns duplicate detection into a single-pass streaming operation, enabling predictable performance, bounded per-event work, and straightforward integration into ingestion pipelines, gateways, and online decision systems.

## 🔍 Problem Statement
You are given an array `scans` of length `1..100000`, where each element is a case-sensitive alphanumeric string of length `1..30`. Process the array from left to right and return the first scan ID that appears more than once. “First” refers to the earliest duplicate event encountered during that left-to-right pass. If no ID repeats, return `""`.

Because comparison is case-sensitive, `"A12"` and `"a12"` are different IDs.

Examples:

- `scans = ["E45", "B12", "C77", "B12", "E45"]` → `"B12"`
- `scans = ["AA1", "BB2", "CC3", "DD4"]` → `""`

The key constraint is `scans.length <= 100000`. That rules out an `O(n^2)` nested-loop approach for any implementation expected to behave well under realistic load.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** want the most frequent ID or the earliest ID that appears multiple times globally. We want the first duplicate event encountered during a left-to-right scan.

2. That wording implies a streaming view → at each position, the only question is: **have I seen this ID before?**

3. “Have I seen this before?” is the canonical signal for hashing → use a `HashSet` keyed by the scan ID string.

4. Iterate once through `scans`:
   - If the current ID is already in the set, return it immediately.
   - Otherwise, add it to the set and continue.

5. Why this works → the first time membership succeeds is exactly the earliest second occurrence in arrival order.

6. Why not sort? Sorting groups duplicates, but destroys the original order, which is the entire point of the problem.

7. Why not count frequencies first? A frequency map tells you what repeats, but not which duplicate event appears first unless you still do an ordered pass. A set is the minimal structure needed.

## 🧩 Algorithm Walkthrough
**Pattern:** Hash Set / streaming duplicate detection.

1. Initialize an empty hash set `seen`.
   - **What happens:** `seen` stores every unique scan ID encountered so far.
   - **Why correct:** membership in `seen` answers whether the current scan has appeared earlier.
   - **Invariant:** after processing index `i - 1`, `seen` contains exactly the IDs from `scans[0..i-1]`.

2. Traverse the array from left to right.
   - **What happens:** inspect each `scanId` in arrival order.
   - **Why correct:** the problem defines “first repeated” relative to this exact order.
   - **Invariant:** before handling `scanId`, `seen` reflects all prior events and no future ones.

3. For each `scanId`, check whether it is already in `seen`.
   - **If yes:** return `scanId`.
   - **Why correct:** this is the earliest point at which a duplicate event has been observed. Any later duplicate is, by definition, not first.
   - **Invariant:** the first successful membership test corresponds to the answer.

4. If `scanId` is not in `seen`, insert it.
   - **What happens:** the current ID becomes part of the historical state for future checks.
   - **Why correct:** future occurrences must recognize this ID as previously seen.
   - **Invariant:** `seen` remains the set of all distinct IDs processed so far.

5. If the loop completes without returning, return `""`.
   - **Why correct:** no membership test ever succeeded, so no ID repeated in the stream.

This abstraction is the right one because the problem is fundamentally online: each decision depends only on prior observations, and hashing gives expected `O(1)` membership and insert operations.

## 📊 Worked Example
Consider `scans = ["E45", "B12", "C77", "B12", "E45"]`.

| Step | Current ID | Seen Before? | `seen` After Step         | Return |
|------|------------|--------------|---------------------------|--------|
| 1    | `E45`      | No           | `{E45}`                   | —      |
| 2    | `B12`      | No           | `{E45, B12}`              | —      |
| 3    | `C77`      | No           | `{E45, B12, C77}`         | —      |
| 4    | `B12`      | Yes          | unchanged                 | `B12`  |

Trace reasoning:
1. `E45` is new, so add it.
2. `B12` is also new, so add it.
3. `C77` is new, so add it.
4. `B12` is already present in `seen`, which means this is the first duplicate event encountered while scanning left to right. Return immediately; there is no need to inspect the final `E45`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** expected time, where `n` is the number of scans. Each element performs at most one hash lookup and one hash insertion. At `10^6` elements this is still practical in memory-resident systems; at `10^9`, linear work is only viable in streaming or distributed settings, but still fundamentally better than quadratic collapse.

### Space Complexity
The space complexity is **O(u)**, where `u` is the number of unique scan IDs seen before termination, worst-case **O(n)**. The hash set owns this memory. You cannot generally reduce it without sacrificing correctness or accepting probabilistic structures such as Bloom filters, which introduce false positives.

## 💡 Key Takeaways
- If the prompt says “first duplicate while scanning left to right,” think streaming state plus constant-time membership checks, not sorting or post-processing.
- If the core question is “have I seen this exact value before?”, a hash set is usually the minimal correct abstraction.
- Do not confuse “first value that repeats somewhere” with “first second-occurrence encountered”; order semantics matter.
- Return the scan ID immediately on the first repeat; continuing the loop can accidentally bias toward later duplicates.
- In production pipelines, this is the same design move as online deduplication: preserve event order, keep compact state, and make per-record decisions in one pass.

## 🚀 Variations & Further Practice
- Return the **index** of the first repeated scan instead of the ID; same pattern, but the output contract changes and indexing mistakes become the main trap.
- Find **all** duplicate scan IDs while preserving first-duplicate encounter order; requires separating “seen once” from “already reported.”
- Process an **unbounded stream** with memory limits; the twist is state eviction or approximate membership, which introduces correctness versus resource trade-offs.