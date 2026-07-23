# First Repeated Hashtag in a Campaign Feed

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Table, Array, String

---

## 🗂 Problem Overview
Given an ordered array of hashtag strings, scan from left to right and return the first hashtag whose **second occurrence** appears earliest in that scan. If no hashtag repeats, return an empty string. The key subtlety is that “first repeated” is defined by the earliest repeat event, not by total frequency, first appearance, or lexical order. With up to 100,000 entries, a quadratic comparison strategy is unnecessary and does not scale.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to detect the first duplicate event in an ordered stream: log ingestion pipelines, fraud detection, idempotency-key validation, compiler symbol resolution, search query deduplication, and observability backends flagging repeated labels or dimensions. At scale, the distinction between “first duplicate encountered in stream order” and “most frequent duplicate” matters operationally because downstream actions are often triggered on first reoccurrence. Without a hash-based membership structure, implementations degrade into repeated scans, increasing latency and CPU cost. With hashing, duplicate detection becomes a single-pass streaming primitive that composes well with online processing and bounded-latency architectures.

## 🔍 Problem Statement
You are given an array `hashtags` where each element is a string representing a hashtag in the exact order it appeared in a campaign feed. Return the first hashtag that becomes repeated while scanning from left to right. If no hashtag appears more than once, return `""`.

Comparison is case-sensitive, so `"#Sale"` and `"#sale"` are different values.

Constraints:
- `1 <= hashtags.length <= 100000`
- `1 <= hashtags[i].length <= 50`
- `hashtags[i]` contains letters, digits, underscores, and `#`

Examples:
- Input: `["#launch", "#sale", "#launch", "#summer"]`  
  Output: `"#launch"`
- Input: `["#red", "#blue", "#green", "#blue", "#red"]`  
  Output: `"#blue"`

The constraint that drives the algorithmic choice is the input size: with up to `10^5` strings, nested scans are avoidable, while constant-time membership checks make a linear pass straightforward.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not asking for the most common hashtag or the earliest first appearance among duplicates. It asks for the hashtag whose **second sighting happens first**.
2. That means scan order is the entire problem. Sorting would destroy the only ordering that matters, so any sort-based approach is already suspicious.
3. While scanning, the only question for the current hashtag is: **have I seen this exact string before?**
4. “Have I seen this before?” is the canonical signal for a hash set. Keys are the hashtag strings themselves.
5. Iterate left to right:
   - If the current hashtag is already in the set, return it immediately.
   - Otherwise, add it to the set and continue.
6. Why immediate return works: the first time this condition becomes true is exactly the earliest second occurrence in scan order.
7. If the scan finishes with no match, no hashtag repeated, so return `""`.

This is the standard single-pass hashing pattern: maintain prior state, detect the first violating event, exit early.

## 🧩 Algorithm Walkthrough
1. **Initialize a hash set `seen`.**  
   This set stores every hashtag encountered so far. The invariant is simple: after processing index `i - 1`, `seen` contains exactly the unique hashtags from indices `0..i-1`.

2. **Traverse the array from left to right.**  
   Order matters because the answer is defined by the earliest second occurrence during the scan. Any approach that reorders input breaks correctness.

3. **For each hashtag `tag`, check membership in `seen`.**  
   If `tag` is already present, this is the second-or-later time it has appeared. More importantly, because we are scanning in order, this is the earliest moment any candidate could be returned.

4. **Return immediately on the first membership hit.**  
   This is correct because the first duplicate event encountered in a left-to-right traversal is exactly the problem’s target. No later element can produce an earlier second occurrence.

5. **If not present, insert `tag` into `seen`.**  
   This preserves the invariant for the next iteration: all previously observed unique hashtags remain tracked for constant-time lookup.

6. **If traversal completes, return `""`.**  
   That means no hashtag was ever encountered twice.

The pattern here is **single-pass hashing with a visited set**. It is the right abstraction because the problem is membership detection over a stream, not counting, grouping, or ordering by value.

## 📊 Worked Example
Example: `hashtags = ["#red", "#blue", "#green", "#blue", "#red"]`

| Index | Current Tag | `seen` Before | Repeated? | Action |
|---|---|---|---|---|
| 0 | `#red` | `{}` | No | Add `#red` |
| 1 | `#blue` | `{#red}` | No | Add `#blue` |
| 2 | `#green` | `{#red, #blue}` | No | Add `#green` |
| 3 | `#blue` | `{#red, #blue, #green}` | Yes | Return `#blue` |

Trace summary:
1. `#red` is first seen, so store it.
2. `#blue` is new, so store it.
3. `#green` is new, so store it.
4. `#blue` is already present in `seen`, so this is the first second-occurrence event in the scan.

Even though `#red` also repeats later, its second occurrence happens after `#blue`’s, so it cannot be the answer.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time, where `n` is the number of hashtags. Each element is processed once, and each hash-set lookup/insert is `O(1)` on average. At `10^6` elements this remains practical as a linear scan; at `10^9`, the algorithm is still asymptotically optimal, though memory bandwidth and storage become the real constraints.

### Space Complexity
`O(u)` space, where `u` is the number of unique hashtags seen before termination, worst-case `O(n)`. The hash set owns essentially all auxiliary memory. You cannot generally reduce this without sacrificing constant-time membership checks or accepting slower repeated scans.

## 💡 Key Takeaways
- If the problem says “first duplicate while scanning” or “earliest repeat event,” preserve input order and think hash set, not sorting.
- When the only question per element is “have I seen this exact value before?”, that is a direct signal for single-pass hashing.
- Return on the first repeated lookup; do not continue scanning for a “better” duplicate, because the scan order already defines optimality.
- Case sensitivity matters here: `"#Sale"` and `"#sale"` must hash and compare as distinct strings.
- In production streams, this is the core duplicate-detection primitive: maintain prior state, detect the first reoccurrence, and short-circuit work early.

## 🚀 Variations & Further Practice
- Return the **index** of the first repeated hashtag instead of the string; same pattern, but be explicit about whether you want the first index, second index, or both.
- Find **all** hashtags that repeat, preserving the order in which their second occurrence appears; this extends the set-based scan into ordered duplicate collection.
- Process an **unbounded stream** with memory limits, where exact duplicate detection may require probabilistic structures like Bloom filters; the twist is trading correctness guarantees for bounded space.