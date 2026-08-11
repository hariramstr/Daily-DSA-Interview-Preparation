# Find the First Repeated Cart Item

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Hash Set, Simulation

---

## 🗂 Problem Overview
Given an array `items`, scanned in chronological order, return the first product ID whose current occurrence is a repeat of one seen earlier. The result is defined by earliest **second occurrence**, not smallest value or most frequent value. If every ID is unique, return `-1`. The non-trivial constraint is scale: with up to `100000` elements, pairwise comparison is unnecessary overhead, so the solution must detect duplicates in a single left-to-right pass.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect the earliest repeated event in an ordered stream: payment idempotency checks, clickstream deduplication, fraud detection, log ingestion, compiler symbol redefinition checks, and streaming pipelines built on Kafka or Flink. At scale, the distinction between “first repeated by scan order” and “most common duplicate” matters because downstream actions are often triggered by the earliest violation or replay. Without a constant-time membership structure, implementations degrade into quadratic scans or expensive reprocessing. With it, you get predictable latency, one-pass processing, and a clean foundation for online detection rather than batch reconciliation.

## 🔍 Problem Statement
You are given an integer array `items` where each value is a product ID scanned into a cart in exact scan order. Return the first product ID whose scan is a duplicate of a previously seen ID while traversing from left to right. If no item repeats, return `-1`.

Key constraints:

- `1 <= items.length <= 100000`
- `1 <= items[i] <= 1000000000`

Examples:

- `items = [42, 17, 9, 17, 42]` → `17`
- `items = [5, 8, 3, 1]` → `-1`

Important nuance: the answer is the value whose **second appearance occurs earliest**. It is not the smallest repeated ID, and it is not the ID with highest frequency. Because the array can contain up to `100000` elements and values are large, the algorithm should avoid nested scans and instead use an auxiliary structure for fast membership checks.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find any duplicate” and not “count frequencies.” The array order matters, and specifically the earliest **repeat event** matters.

2. Ask what information is needed at position `i` → only whether `items[i]` has appeared before. We do not need total counts up front.

3. That immediately suggests a left-to-right scan with a membership structure → a `HashSet` is the natural fit because lookup and insertion are both expected `O(1)`.

4. Start with an empty set of seen product IDs.

5. For each item:
   - If it is already in `seen`, return it immediately.
   - Otherwise add it to `seen` and continue.

6. Why this works → the first time membership succeeds is exactly the earliest second occurrence in scan order. Returning later would violate the problem definition.

7. If the loop finishes, no ID repeated, so return `-1`.

This is the standard “stream deduplication / first duplicate detection” pattern: one pass, stateful membership tracking, early exit on first violation.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Hash Set + Single Pass Simulation.**  
   This problem is fundamentally an ordered stream check. We process items exactly once in scan order and maintain the set of IDs already observed. A hash set is the right abstraction because the only query we need is: “Have I seen this exact value before?”

2. **Initialize an empty `seen` set.**  
   Before scanning begins, no product IDs have been observed.  
   **Invariant:** after processing indices `0..i-1`, `seen` contains exactly the distinct IDs from that prefix.

3. **Iterate left to right through `items`.**  
   At each element `x`, check whether `x` is already in `seen`. This preserves the stream semantics of the problem: decisions are made in chronological order, not after global analysis.

4. **If `x` is already present, return `x`.**  
   This is correct because the current index is the earliest point at which any duplicate has been encountered. By scanning left to right and exiting immediately, we guarantee the returned value is the one whose second occurrence happens first.

5. **Otherwise insert `x` into `seen`.**  
   This updates the invariant: after insertion, `seen` still matches the distinct IDs in the processed prefix, now extended by the current element.

6. **If the scan completes, return `-1`.**  
   At that point every membership check failed, so no element appeared twice.

This is not a counting problem, sorting problem, or frequency-ranking problem. The decisive property is earliest repeated event detection, and a hash set gives the minimal state needed to solve it efficiently.

## 📊 Worked Example
Example: `items = [42, 17, 9, 17, 42]`

| Index | Current Item | Seen Before? | Seen Set After Step | Return? |
|------:|-------------:|:------------:|---------------------|:-------:|
| 0 | 42 | No | {42} | No |
| 1 | 17 | No | {42, 17} | No |
| 2 | 9  | No | {42, 17, 9} | No |
| 3 | 17 | Yes | {42, 17, 9} | **17** |

Trace:

1. Scan `42` → first occurrence, add it.
2. Scan `17` → first occurrence, add it.
3. Scan `9` → first occurrence, add it.
4. Scan `17` again → already present in `seen`, so this is the first repeated cart item.
5. We stop immediately; the later repeat of `42` is irrelevant because its second occurrence happens after `17`’s second occurrence.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** expected time, where `n` is `items.length`. Each element is processed once, with one hash-set lookup and at most one insertion. At `10^6` elements this remains practical in a single pass; at `10^9`, runtime becomes dominated by raw I/O and memory bandwidth rather than algorithmic inefficiency.

### Space Complexity
The algorithm uses **O(n)** extra space in the worst case for the `seen` hash set, when all items are unique. This space cannot be reduced below linear if you need exact one-pass duplicate detection without mutating or sorting the input.

## 💡 Key Takeaways
- If the prompt says “first duplicate encountered while scanning” or “earliest repeated event,” think ordered stream processing, not frequency counting.
- If you only need prior membership, not counts or positions, a `HashSet` is usually the minimal correct structure.
- Do not sort the array: sorting destroys the original scan order and changes the meaning of “first repeated.”
- Return on the first successful membership check; continuing the scan can produce a logically wrong answer.
- In production event pipelines, exact online deduplication often reduces to maintaining just enough state to answer “have I seen this key before?” with predictable latency.

## 🚀 Variations & Further Practice
- Return the **index** of the first repeated item’s second occurrence instead of the value; same pattern, but the output contract shifts from identity to event position.
- Return **all duplicates in order of their second appearance**; this requires continuing after the first hit and avoiding duplicate reporting for later occurrences.
- Solve the same problem under **memory pressure or approximate deduplication** constraints; this introduces probabilistic structures like Bloom filters and forces a false-positive trade-off.