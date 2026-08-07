# Find the First Missing Checkpoint Number

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Hash Set, Simulation

---

## 🗂 Problem Overview
Given an unsorted integer array `checkpoints`, return the smallest positive integer that does not appear in the array. Values `<= 0` are irrelevant, and duplicates do not affect the result. The input is not sorted, so a naive scan for gaps is not enough. The key constraint is finding the first missing positive efficiently without relying on expensive repeated membership checks or sorting unless that trade-off is acceptable.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect the earliest missing identifier in noisy, unordered data: event ingestion pipelines validating sequence continuity, warehouse scanners reconciling package checkpoints, CDC streams detecting dropped offsets, and telemetry systems spotting the first missing sample in a run. At scale, the difference between linear membership tracking and repeated scans becomes material: latency spikes, reconciliation jobs overrun windows, and backfill logic becomes unreliable. A compact presence structure enables deterministic validation, cheap anomaly detection, and predictable performance even when upstream producers emit duplicates, stale records, or malformed values.

## 🔍 Problem Statement
You are given an array `checkpoints` of length `1` to `10^5`, where each element may be negative, zero, positive, duplicated, and unsorted. Return the smallest positive checkpoint ID that does not occur in the array.

Only positive integers matter. Invalid values such as `0` and negatives must be ignored. Duplicate positive IDs count as present once.

Examples:

- `checkpoints = [3, 4, -1, 1]` → `2`
- `checkpoints = [1, 2, 2, 5]` → `3`

The important algorithmic constraint is scale: with up to `10^5` elements, quadratic scans are unnecessary and fragile. The problem is fundamentally a presence-query problem over the positive range, so the right solution should support fast membership checks while handling unsorted input and duplicates cleanly.

## 🪜 How to Solve This
1. Read the problem → we do **not** need ordering of all values; we only need to know which positive IDs exist.
2. Ignore noise → negatives and zero can never be the answer unless `1` is missing, so they should not influence the data structure.
3. Duplicates do not matter → this is a strong signal that we care about **set membership**, not counts.
4. Once you recognize “find the smallest missing positive” in an unsorted array, the natural question becomes: how do I answer “is `1` present? is `2` present? is `3` present?” efficiently?
5. That leads directly to a `HashSet` of valid positive values.
6. Build the set in one pass.
7. Then start from `1` and scan upward until you find the first integer not in the set.
8. Why does this work? Because the answer is defined as the earliest positive gap. If all values `1..k-1` exist and `k` does not, then `k` is exactly the required result.
9. This avoids sorting and nested searches, giving a clean linear-time solution for interview-scale inputs.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Hash Set / Presence Tracking.**  
   This problem is a membership problem over integers, not a sorting problem. The right abstraction is a `HashSet` that records which positive checkpoint IDs are present.

2. **Scan the array once and insert valid values.**  
   For each `id` in `checkpoints`, if `id > 0`, insert it into the set.  
   **Why correct:** only positive values can affect the answer.  
   **Invariant:** after this pass, the set contains exactly the distinct positive IDs from the input.

3. **Start checking from `candidate = 1`.**  
   The smallest missing positive must be at least `1`, so begin there.  
   **Why correct:** any answer smaller than `1` is invalid by definition.

4. **Advance while the candidate exists in the set.**  
   While `candidate` is present, increment it.  
   **Why correct:** if `candidate` exists, it cannot be the first missing positive.  
   **Invariant:** before each increment, all integers in the range `1..candidate` are confirmed present.

5. **Return the first candidate not found.**  
   The first membership failure is the smallest missing positive.  
   **Why correct:** by construction, every smaller positive integer has already been verified present.

This is a classic **simulation with hash-backed membership checks**: simulate the expected positive sequence `1, 2, 3, ...` against observed data, using constant-average-time lookups to keep the scan linear.

## 📊 Worked Example
Example: `checkpoints = [3, 4, -1, 1]`

| Step | Value / Candidate | Action | Set State / Result |
|---|---:|---|---|
| 1 | 3 | positive → insert | `{3}` |
| 2 | 4 | positive → insert | `{3, 4}` |
| 3 | -1 | ignore | `{3, 4}` |
| 4 | 1 | positive → insert | `{1, 3, 4}` |
| 5 | candidate = 1 | present → increment | next `2` |
| 6 | candidate = 2 | not present | return `2` |

Trace summary:
1. Build a set of valid positive IDs: `{1, 3, 4}`.
2. Check from the smallest legal checkpoint ID upward.
3. `1` exists, so the earliest missing value is not `1`.
4. `2` does not exist, so the first missing checkpoint number is `2`.

The key observation is that missing values are discovered by scanning the expected sequence, not by sorting the observed one.

## ⏱ Complexity Analysis
### Time Complexity
Building the `HashSet` takes `O(n)`, and scanning upward for the answer is also `O(n)` in the worst case, so total time is `O(n)` on average. At `10^6` elements this remains practical; at `10^9`, memory bandwidth and hash overhead dominate, so a different storage strategy may be needed.

### Space Complexity
The `HashSet` stores up to all distinct positive values, so space is `O(n)`. That space is owned entirely by the presence-tracking structure. It can be reduced to `O(1)` only with the in-place index-placement variant, at the cost of mutating the input and increasing implementation complexity.

## 💡 Key Takeaways
- If the problem says “smallest missing” in an unsorted array and duplicates do not matter, think **presence tracking**, not sorting.
- If only membership matters and counts are irrelevant, a `HashSet` is usually the first correct abstraction to test.
- Do not waste work storing negatives or zero; they can never be the returned value.
- Start checking from `1`, not `0`, and stop at the first gap; off-by-one mistakes here are the common failure mode.
- In production data validation, separating noisy input normalization from deterministic gap detection keeps reconciliation logic simple and scalable.

## 🚀 Variations & Further Practice
- **First Missing Positive with `O(1)` extra space** — same goal, but now you must rearrange elements in-place using index placement/cyclic positioning.
- **Find all missing numbers in `1..n`** — instead of the first gap, return every missing value; the twist is preserving linear time while handling duplicates.
- **Detect first missing sequence number in a stream** — data arrives incrementally, so the harder part is maintaining the earliest gap online under bounded memory.