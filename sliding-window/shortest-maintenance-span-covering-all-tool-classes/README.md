# Shortest Maintenance Span Covering All Tool Classes

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given a tool-usage sequence `tools` and a multiset of required tool classes `required`, find the length of the shortest contiguous subarray of `tools` that contains every required class with at least the required multiplicity. Duplicates in `required` matter. Return `-1` if no such span exists. The challenge is scale: with arrays up to 200,000 elements, enumerating subarrays is infeasible, so the solution must update counts incrementally while scanning once.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must detect the smallest interval satisfying a multiset constraint: log pipelines finding the minimal event span covering required signatures, search/query engines locating the shortest document window containing all query terms with frequency thresholds, fraud detection over transaction streams, and observability systems isolating the smallest trace segment containing all required markers. At production scale, brute-force interval checks collapse under quadratic behavior and cache-unfriendly rescans. A sliding-window plus frequency-map design turns the problem into a linear pass with bounded mutable state, which is the difference between an online algorithm and an offline batch bottleneck.

## 🔍 Problem Statement
You are given two integer arrays:

- `tools`, where `tools[i]` is the tool class used at position `i`
- `required`, where each value is a tool class that must appear in a valid contiguous span

`required` is a multiset, not a set. If a class appears multiple times in `required`, the chosen span must contain at least that many occurrences.

Return the length of the shortest contiguous subarray of `tools` satisfying all requirements, or `-1` if none exists.

Constraints:

- `1 <= tools.length <= 200000`
- `1 <= required.length <= 200000`
- `1 <= tools[i], required[i] <= 10^9`
- Answer fits in 32-bit signed integer

Examples:

- `tools = [7,2,3,2,5,2,1,5]`, `required = [2,5,2]` → `3`
- `tools = [4,1,4,3,6,1,3]`, `required = [1,3,3]` → `-1`

The key constraint is input size: any approach that checks many subarrays explicitly will time out.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “contains all distinct values,” it is “contains all required counts.” That immediately suggests frequency tracking, not sorting or prefix sums alone.

2. We need a **contiguous** span and we want the **shortest** one → that is the standard signal for a sliding window / two-pointer approach.

3. Build a frequency map for `required` → this tells us the exact quota for each tool class.

4. Expand the right pointer through `tools` → as each tool enters the window, update its count. If that tool was still below its required quota, we just made progress toward validity.

5. Once the window satisfies all quotas, shrink from the left as aggressively as possible → this is how we guarantee minimality for the current right boundary.

6. Record the best length every time the window is valid before shrinking breaks validity.

7. Why this works: each pointer only moves forward, and counts are updated incrementally. That avoids rescanning subarrays and gives linear-time behavior even with duplicate requirements.

## 🧩 Algorithm Walkthrough
1. **Count the requirement multiset.**  
   Build `need[class] = required frequency`. Also track `missing = required.length`, meaning how many total required occurrences are still unmet. This is stronger than counting distinct classes because duplicates matter.

2. **Initialize a sliding window.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`. Maintain `window[class]` for counts inside the current interval `[left, right]`.

3. **Expand the right boundary.**  
   For each `right` from `0` to `tools.length - 1`, add `tools[right]` to `window`. If `window[x] <= need[x]`, then this occurrence satisfies one still-missing requirement, so decrement `missing`.

4. **Detect validity.**  
   The window is valid exactly when `missing == 0`. This invariant means every required occurrence across all classes is covered by the current window.

5. **Shrink greedily from the left.**  
   While the window remains valid, update the best answer with `right - left + 1`, then remove `tools[left]` and increment `left`. If removing that value causes `window[y] < need[y]`, increment `missing`; the window is no longer valid, so stop shrinking.

6. **Return the result.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum recorded length.

Why this abstraction fits: the problem asks for a shortest contiguous region under additive membership constraints. Sliding window is the right model because adding/removing one element changes validity locally and can be tracked with O(1) map updates.

## 📊 Worked Example
Example: `tools = [7,2,3,2,5,2,1,5]`, `required = [2,5,2]`  
Need: `{2:2, 5:1}`, `missing = 3`

| right | tools[right] | window after add | missing | action |
|---|---:|---|---:|---|
| 0 | 7 | {7:1} | 3 | not valid |
| 1 | 2 | {7:1,2:1} | 2 | not valid |
| 2 | 3 | {7:1,2:1,3:1} | 2 | not valid |
| 3 | 2 | {7:1,2:2,3:1} | 1 | not valid |
| 4 | 5 | {7:1,2:2,3:1,5:1} | 0 | valid, try shrink |

Shrink phase at `right = 4`:
- `[0..4]` length 5 → remove `7`, still valid
- `[1..4]` length 4 → remove first `2`, now `2` count drops below need, invalid

Continue:
- `right = 5`, add `2` → valid again for window `[2..5] = [3,2,5,2]`
- Shrink: length 4, remove `3`, still valid
- `[3..5] = [2,5,2]`, length 3 → best
- Remove left `2`, invalid

Answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = tools.length` and `m = required.length`. Building the requirement map costs `O(m)`. The sliding window moves each pointer forward at most `n` times, so the dominant work is linear. At million-scale input this is practical; at billion-scale, only streaming or distributed partitioning makes sense.

### Space Complexity
`O(k)`, where `k` is the number of distinct tool classes appearing in `required` and the active window map. The frequency maps own the space. You can reduce constants by only storing counts for required classes, trading a small branch on each update for lower memory usage.

## 💡 Key Takeaways
- If the problem asks for the **shortest contiguous segment** satisfying inclusion constraints, sliding window should be your first candidate.
- If duplicates in the target matter, model the target as a **multiset frequency map**, not a set membership check.
- The validity condition is not “all keys seen”; it is “every key’s window count meets required count,” or equivalently `missing == 0`.
- Be careful when shrinking: update the answer **before** removing `tools[left]`, otherwise you can miss the current minimal valid window.
- The production-grade insight is to convert repeated global validation into a local invariant maintained under incremental updates; that is the core scalability move.

## 🚀 Variations & Further Practice
- **Minimum window substring / token span:** same pattern, but over characters or tokens; harder when alphabets are large or input is Unicode/tokenized text.
- **Shortest span with weighted requirements:** each class contributes a weight or score rather than unit count; validity becomes threshold-based instead of exact frequency coverage.
- **Streaming version with expirations or time windows:** events arrive online and old events expire by timestamp, forcing the window to satisfy both multiset coverage and temporal bounds simultaneously.