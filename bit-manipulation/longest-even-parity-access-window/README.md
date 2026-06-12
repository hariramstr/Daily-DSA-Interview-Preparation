# Longest Even-Parity Access Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Prefix Mask, Hash Map

---

## 🗂 Problem Overview
Given an array `events` of badge categories in `[0, 19]`, find the maximum length of a contiguous subarray where every category appears an even number of times. Return that length, or `0` if no non-empty balanced window exists. The challenge is scale: `events.length` can reach `200000`, so checking all subarrays is infeasible. The key constraint is the fixed alphabet of 20 categories, which makes parity compression into a bitmask practical.

## 🌍 Engineering Impact
This pattern shows up anywhere a stream must be segmented by state equivalence rather than exact counts: log anomaly detection, streaming fraud signals, compiler token-state tracking, telemetry pipelines, and distributed access-control auditing. At scale, naive window enumeration or per-window frequency maps collapse under quadratic cost and cache-unfriendly memory churn. Prefix-state compression turns an unbounded counting problem into a fixed-width state machine. That enables single-pass processing, predictable memory use, and easy adaptation to online or partitioned systems where you need to reason about segment properties from compact summaries instead of replaying raw events.

## 🔍 Problem Statement
You are given an integer array `events` where each value is a badge category from `0` to `19`. A contiguous window is **balanced** if every category appears an even number of times within that window. The task is to return the length of the longest such window.

Constraints:
- `1 <= events.length <= 200000`
- `0 <= events[i] < 20`
- Expected time: `O(n)`
- Expected extra space: `O(min(n, 2^20))`

Examples:

- `events = [3, 5, 3, 5, 7, 7]` → `6`
- `events = [1, 2, 1, 4, 2, 4, 4]` → `6`

Edge cases matter:
- A single event can never form a balanced non-empty window.
- The answer may be `0` if no repeated prefix parity state yields a non-empty valid subarray.
- The bounded category range is the reason an `O(n)` prefix-mask solution is viable.

## 🪜 How to Solve This
1. Read the requirement carefully → the window is valid when **every count is even**, not when counts match some target.
2. “Even vs odd” is a parity question → exact frequencies are unnecessary. For each category, only one bit matters.
3. While scanning left to right, maintain a 20-bit mask where bit `k` is `1` iff category `k` has appeared an odd number of times so far.
4. When you see category `x`, flip bit `x` using XOR. That updates parity in `O(1)`.
5. Now the key observation: if the same mask appears at positions `i` and `j`, then the subarray `(i+1..j)` has zero parity difference, meaning every category occurs an even number of times there.
6. So the problem becomes: for each prefix mask, remember the earliest index where it appeared, and maximize the distance to later occurrences.
7. Seed mask `0` at index `-1` so a prefix from `0` to `i` is handled naturally when it is already balanced.

Once you see “fixed small alphabet + parity + longest subarray,” prefix mask plus earliest-occurrence map is the obvious tool.

## 🧩 Algorithm Walkthrough
1. **Use the Prefix Mask pattern with a Hash Map.**  
   Maintain `mask`, a 20-bit integer representing parity of counts in the prefix ending at the current index. This is the right abstraction because the validity condition depends only on parity, and parity composes cleanly under XOR.

2. **Initialize base state.**  
   Set `mask = 0`, `best = 0`, and store `firstSeen[0] = -1`. This encodes the empty prefix. The invariant is: `firstSeen[m]` holds the earliest index where parity state `m` occurred.

3. **Scan the array once.**  
   For each index `i`, read `events[i] = c` and update `mask ^= (1 << c)`. This flips only the parity of category `c`, preserving all others. After this step, `mask` exactly represents parity for prefix `events[0..i]`.

4. **Check whether this state was seen before.**  
   - If `mask` already exists in `firstSeen`, then the subarray from `firstSeen[mask] + 1` to `i` is balanced. Why: equal prefix states imply their XOR difference is zero, so every category changed parity an even number of times.
   - Update `best = max(best, i - firstSeen[mask])`.

5. **Otherwise record the earliest occurrence.**  
   If `mask` is new, store `firstSeen[mask] = i`. Earliest occurrence matters because it maximizes future subarray lengths for the same mask.

6. **Return `best`.**  
   If no balanced non-empty window exists, `best` remains `0`, which matches the specification.

This works in linear time because each event causes one mask flip and one constant-time map lookup/update.

## 📊 Worked Example
Take `events = [1, 2, 1, 4, 2, 4, 4]`.

| i | event | mask after flip | first seen? | balanced window length | best |
|---|-------|-----------------|-------------|------------------------|------|
| -1 | — | `00000` | store `0 -> -1` | — | 0 |
| 0 | 1 | `00010` | no, store at 0 | — | 0 |
| 1 | 2 | `00110` | no, store at 1 | — | 0 |
| 2 | 1 | `00100` | no, store at 2 | — | 0 |
| 3 | 4 | `10100` | no, store at 3 | — | 0 |
| 4 | 2 | `10000` | no, store at 4 | — | 0 |
| 5 | 4 | `00000` | yes, at -1 | `5 - (-1) = 6` | 6 |
| 6 | 4 | `10000` | yes, at 4 | `6 - 4 = 2` | 6 |

The longest balanced window is indices `0..5`: `[1, 2, 1, 4, 2, 4]`, length `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each event triggers one XOR bit flip and one hash-map lookup/update, both constant-time on average. For `10^6` elements this remains practical in a single pass. At `10^9`, the asymptotics still hold, but runtime becomes dominated by memory bandwidth, map overhead, and system-level streaming constraints.

### Space Complexity
`O(min(n, 2^20))`. The space is owned by the earliest-occurrence table for prefix masks. Because only `2^20` distinct masks exist, memory is capped. It can be reduced further with a fixed array of size `2^20`, trading hash-map flexibility for denser, faster storage.

## 💡 Key Takeaways
- If a subarray condition depends only on **even/odd frequency**, think parity compression instead of full counting.
- If the domain of values is small and fixed, a **prefix bitmask state** is often the right replacement for a frequency map per window.
- Seed the empty-prefix state (`mask = 0` at index `-1`) or you will miss balanced windows that start at index `0`.
- Store the **earliest** index for each mask, not the latest; overwriting it silently loses the longest answer.
- In production systems, compact prefix-state representations let you reason about large streams with fixed-width summaries instead of replaying or materializing expensive per-segment counts.

## 🚀 Variations & Further Practice
- Longest subarray where **at most one** category has odd frequency: same prefix-mask idea, but for each position also probe masks differing by one bit.
- Generalize from 20 categories to a larger alphabet: the conceptual twist is that fixed-width bitmasks stop fitting comfortably, forcing sparse state representations or different invariants.
- Count the **number** of balanced subarrays instead of the longest one: same prefix states, but now you aggregate frequencies of masks rather than earliest positions.