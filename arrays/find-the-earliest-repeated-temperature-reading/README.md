# Find the Earliest Repeated Temperature Reading

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Hash Set, Simulation

---

## 🗂 Problem Overview
Given an integer array `temps`, scan from left to right and return the first temperature whose **second occurrence** is encountered earliest. The output is the repeated value itself, not its index. If no value repeats, return `-1`. The non-trivial part is the input size: with up to `100000` readings, pairwise comparison is unnecessarily expensive, so the solution must detect prior occurrences in near-constant time per element.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect the first repeated event in arrival order: duplicate message detection in streaming pipelines, replay protection in API gateways, repeated symbol detection in compiler passes, and idempotency checks in event-driven systems. At scale, the distinction between “first duplicate encountered” and “most frequent duplicate” matters because downstream actions are often triggered by temporal order, not aggregate counts. Without an O(n) membership structure, latency grows quadratically and quickly becomes unacceptable in hot paths. A hash-backed seen-set enables single-pass detection, predictable performance, and early exit once the first actionable repeat appears.

## 🔍 Problem Statement
You are given an integer array `temps` where `temps[i]` is the temperature recorded at hour `i`. Traverse the array from index `0` to `temps.length - 1` and return the first value whose current appearance is a repeat of a value seen earlier. More precisely, return the temperature whose **second occurrence** appears earliest during the left-to-right scan. If every value is unique, return `-1`.

Constraints:

- `1 <= temps.length <= 100000`
- `-1000000 <= temps[i] <= 1000000`

Examples:

- `temps = [18, 21, 19, 21, 18]` → `21`
- `temps = [5, 7, 8, 9]` → `-1`

Edge cases include arrays of length `1`, negative temperatures, and repeated values appearing many times. The key constraint is array size: it rules out nested scans and pushes toward a linear-time solution.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not asking for the smallest duplicated temperature or the most common one. It asks for the value whose **repeat is encountered first** during a left-to-right scan.

2. That wording implies we should process readings in arrival order and decide immediately, at each position, whether this value has appeared before.

3. To answer “have I seen this value already?” efficiently, we need constant-time membership checks. That points directly to a **Hash Set**.

4. Iterate through `temps` once:
   - If the current temperature is already in the set, we have just reached its second-or-later occurrence.
   - Because we scan left to right, this is the earliest repeated value by definition, so we can return it immediately.

5. Otherwise, add the temperature to the set and continue.

6. If the scan finishes without finding a repeat, return `-1`.

The key insight is that order matters more than frequency. A set captures prior existence, and the single pass preserves the exact arrival semantics the problem asks for.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Hash Set + single-pass simulation.**  
   This problem is a membership-tracking problem over a stream-like array. We do not need counts, sorting, or index maps. We only need to know whether a value has appeared before.

2. **Initialize an empty set `seen`.**  
   `seen` stores every temperature observed in the prefix processed so far.  
   **Invariant:** before processing index `i`, `seen` contains exactly `temps[0..i-1]`.

3. **Scan the array from left to right.**  
   For each `temp` in `temps`, check whether `temp` is already in `seen`.

4. **If `temp` is in `seen`, return `temp` immediately.**  
   This is correct because the current index is the earliest point in the scan where any duplicate has been encountered. Returning later would violate the problem’s ordering rule.

5. **If `temp` is not in `seen`, add it.**  
   This preserves the invariant for the next iteration: after insertion, `seen` matches all values observed up to the current index.

6. **If the loop completes, return `-1`.**  
   At that point, no element was encountered twice, so no repeated temperature exists.

Why this abstraction fits: the problem is fundamentally about **first repeated membership in a sequence**, which is exactly what a hash set optimizes. Sorting would destroy arrival order, and frequency counting does unnecessary work because we can terminate as soon as the first repeat appears.

## 📊 Worked Example
Example: `temps = [18, 21, 19, 21, 18]`

| Step | Current Temp | `seen` Before     | Repeated? | Action              |
|------|--------------|-------------------|-----------|---------------------|
| 1    | 18           | `{}`              | No        | Add `18`            |
| 2    | 21           | `{18}`            | No        | Add `21`            |
| 3    | 19           | `{18, 21}`        | No        | Add `19`            |
| 4    | 21           | `{18, 21, 19}`    | Yes       | Return `21`         |

Trace summary: the first three temperatures are unique, so they are inserted into `seen`. At step 4, `21` is already present, which means this index is the earliest point where any value repeats. Even though `18` also repeats later, its second occurrence happens after `21`’s, so it cannot be the answer.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time, where `n` is `temps.length`. Each element is processed once, and each hash set lookup/insertion is O(1) on average. At `10^6` elements this is still practical; at `10^9`, runtime becomes dominated by raw iteration and memory bandwidth rather than algorithmic overhead.

### Space Complexity
The algorithm uses **O(n)** extra space in the worst case for the hash set, when all temperatures are unique. That space belongs entirely to `seen`. It cannot be reduced to O(1) without sacrificing either correctness under ordering constraints or time complexity.

## 💡 Key Takeaways
- If a problem says “first duplicate encountered while scanning,” think **stream processing + seen-set**, not sorting or frequency aggregation.
- When the question depends on arrival order and asks for early detection, a single left-to-right pass is usually the right mental model.
- Return the **value** when you detect a repeat, not the first value that appears twice after full analysis; early exit is part of correctness here.
- Do not sort the array to simplify duplicate detection: sorting changes temporal order and produces the wrong answer for this problem.
- In production systems, this is the same design move as online deduplication: maintain compact state for prior observations so decisions can be made immediately at ingestion time.

## 🚀 Variations & Further Practice
- Return the **index** of the earliest second occurrence instead of the value; same pattern, but the output contract shifts from identity to event position.
- Find all duplicates in arrival order; harder because you must continue after the first repeat while avoiding duplicate reporting for values seen three or more times.
- Detect the first repeated event within a **sliding time window**; the twist is state eviction, which turns a simple seen-set into a windowed hash structure.