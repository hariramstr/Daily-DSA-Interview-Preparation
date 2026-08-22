# Find the First Repeated Poll Vote

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Array, String

---

## 🗂 Problem Overview
Given a stream-ordered array of vote strings, return the first value that repeats as you scan from left to right. “First” is defined by the earliest second occurrence, not by lexical order or first appearance. If no value repeats, return `""`. The non-trivial part is preserving stream order while detecting duplicates efficiently under input sizes up to `100000`, which rules out quadratic pairwise comparison and makes one-pass membership tracking the right model.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect the earliest duplicate event in a live stream: fraud and abuse detection, idempotency-key validation, telemetry deduplication, clickstream processing, and message replay detection in event-driven systems. In polling or voting systems, the requirement is not just “does a duplicate exist,” but “which duplicate is observed first in arrival order.” Without an O(1)-average membership structure, throughput collapses under high-cardinality streams and latency grows with history size. A hash-backed seen set enables single-pass processing, bounded per-event work, and straightforward promotion from in-memory logic to distributed stream operators.

## 🔍 Problem Statement
You are given an array `votes` of length `1` to `100000`, where each element is a string of length `1` to `30` containing lowercase English letters, digits, or underscores. Each string represents a vote received in order.

Return the first vote value whose second occurrence is encountered earliest during a left-to-right scan. Equivalently, iterate through `votes` and return the first value that has already been seen. If every vote is unique, return an empty string `""`.

Examples:

- `votes = ["red", "blue", "green", "blue", "red"]` → `"blue"`
- `votes = ["north", "south", "east", "west"]` → `""`

Important edge cases:
- A duplicate may occur immediately, e.g. `["x", "x"]`
- Multiple values may repeat, but only the earliest second occurrence matters
- No repetition should produce `""`

The key constraint is scale: `100000` elements makes nested scans unnecessary and avoidable.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not asking for all duplicates or the most frequent vote. It asks for the first value that is seen twice during a left-to-right scan.

2. That wording suggests a streaming mindset → at each position, the only question is: “Have I seen this vote before?”

3. “Have I seen this before?” is a membership query → the natural structure is a hash set, because average-case lookup and insertion are O(1).

4. Scan once from left to right:
   - If the current vote is already in the set, return it immediately.
   - Otherwise insert it and continue.

5. Why immediate return works → the first duplicate encountered during the scan is, by definition, the vote whose second occurrence appears earliest. No later element can invalidate that.

6. If the scan finishes without a hit, then no vote repeated, so return `""`.

This is the standard one-pass hashing pattern: maintain a compact summary of prior state, answer each event locally, and stop as soon as the target condition is satisfied.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Hash Set / One-Pass Streaming Detection.**  
   The problem is fundamentally about duplicate detection with order preservation. Sorting would destroy arrival order, and nested loops would be O(n²). A hash set preserves the only state we need: which vote values have appeared so far.

2. **Initialize an empty set `seen`.**  
   This set represents the invariant: after processing index `i - 1`, `seen` contains exactly the distinct votes from positions `0..i-1`.

3. **Iterate through `votes` from left to right.**  
   For each vote `v`, first check whether `v` is already in `seen`.

4. **If `v` is in `seen`, return `v` immediately.**  
   This is correct because the current position is the earliest point at which any second occurrence has been observed so far. Since we scan in order, the first duplicate we encounter is the required answer.

5. **If `v` is not in `seen`, insert it.**  
   This restores the invariant for the next iteration: all distinct votes seen up to the current index are now recorded.

6. **If the loop completes, return `""`.**  
   At that point, every vote was inserted exactly once and no membership check ever succeeded, so no repeated vote exists.

This abstraction is the right one because the problem does not require counts, frequencies, or positions beyond “seen before.” A set is strictly sufficient state.

## 📊 Worked Example
Example: `votes = ["red", "blue", "green", "blue", "red"]`

| Index | Vote    | `seen` before check         | Repeated? | Action                  |
|------:|---------|-----------------------------|-----------|-------------------------|
| 0     | `red`   | `{}`                        | No        | Add `red`               |
| 1     | `blue`  | `{red}`                     | No        | Add `blue`              |
| 2     | `green` | `{red, blue}`               | No        | Add `green`             |
| 3     | `blue`  | `{red, blue, green}`        | Yes       | Return `"blue"`         |

Trace summary:
1. `red` is new, so store it.
2. `blue` is new, so store it.
3. `green` is new, so store it.
4. `blue` is already present, meaning this is its second occurrence.
5. We stop immediately; `red` also repeats later, but its second occurrence happens after `blue`’s, so it is not the answer.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** average time, where `n` is the number of votes. Each element is processed once, with one hash lookup and at most one hash insertion. At `10^6` elements this remains practical in-memory; at `10^9`, the algorithmic shape is still right, but memory distribution and streaming infrastructure become the real constraints.

### Space Complexity
The algorithm uses **O(u)** space, where `u` is the number of distinct vote values seen before termination, worst-case **O(n)**. The hash set owns essentially all auxiliary space. You cannot reduce this to constant space without sacrificing one-pass behavior or degrading lookup time.

## 💡 Key Takeaways
- If the prompt says “first duplicate while scanning left to right,” think streaming membership test, not sorting or frequency counting.
- If the only state you need is “seen before or not,” a hash set is usually the minimal correct abstraction.
- Check for membership **before** inserting the current vote; reversing that order makes every element look duplicated.
- “First repeated” means earliest **second occurrence**, not the element with the earliest first appearance among all duplicates.
- In production systems, this pattern is the in-memory form of online deduplication: maintain just enough state to make a per-event decision without rescanning history.

## 🚀 Variations & Further Practice
- Return the **index** of the first repeated vote instead of the value; same pattern, but the output contract shifts from identity to position.
- Return **all** repeated votes in order of their second occurrence; this extends the one-pass set approach but requires continued scanning after the first hit.
- Find the first vote whose count reaches **k** instead of 2; the conceptual twist is moving from membership tracking to hash-based counting.