# Longest Badge Run With Limited Room Changes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an integer array `rooms` representing a time-ordered badge log and an integer `k`, find the maximum length of a contiguous subarray containing at most `k` distinct room IDs. The output is a single integer: the longest valid interval length. The challenge is scale: with up to 200,000 entries, any approach that recomputes distinct counts for many subarrays becomes too slow, so the solution must maintain validity incrementally in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent interval satisfying a bounded-cardinality constraint. Examples include observability pipelines tracking distinct error codes in a time slice, fraud systems measuring merchant or device churn, distributed rate-limiters grouping activity by tenant, and search/session analytics detecting stable user behavior. At production scale, brute-force window rescans collapse under high-throughput streams because distinct counting is expensive when repeated. A sliding window with incremental frequency accounting enables single-pass processing, predictable latency, and memory proportional to active diversity rather than total history.

## 🔍 Problem Statement
You are given an integer array `rooms` where `rooms[i]` is the room ID entered at time `i`, and an integer `k`. Return the length of the longest contiguous subarray containing no more than `k` distinct values.

A contiguous interval must use consecutive log entries. If `k = 0`, no room can be included, so the answer is `0`.

Constraints:

- `1 <= rooms.length <= 200000`
- `0 <= rooms[i] <= 1000000000`
- `0 <= k <= rooms.length`

Examples:

- `rooms = [4, 2, 2, 7, 2, 4, 4, 7], k = 2` → `4`
- `rooms = [9, 9, 1, 3, 1, 1, 3, 9], k = 3` → `8`

The key constraint is the expected `O(n)` runtime. That rules out checking all subarrays or rebuilding distinct sets repeatedly. The algorithm must update the current window’s distinct-room count as it expands and contracts.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous** interval, so sorting is invalid because order matters.

2. The rule is “at most `k` distinct values” → this is a classic **window validity** condition. We want the largest range that stays valid while scanning left to right.

3. If we extend the interval by one room, only one count changes → that suggests maintaining room frequencies in a `HashMap` instead of recomputing distinct values from scratch.

4. Start with two pointers: `left` and `right`. Move `right` forward to grow the window. Each new room increments its frequency.

5. If the window now contains more than `k` distinct rooms, it became invalid → move `left` forward until validity is restored, decrementing counts and removing entries that drop to zero.

6. After every expansion/shrink cycle, the window is the longest valid one ending at `right` → update the best length.

7. Each element enters and leaves the window at most once, which is why this reaches `O(n)` instead of quadratic time.

## 🧩 Algorithm Walkthrough
1. **Handle the degenerate case.**  
   If `k == 0`, return `0` immediately. No non-empty subarray can satisfy the constraint. This avoids unnecessary map work and makes the boundary condition explicit.

2. **Initialize the sliding window.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`, a frequency map, and `best = 0`. The abstraction fits because the validity of a window depends only on counts inside the current contiguous range.

3. **Expand the window with `right`.**  
   For each `rooms[right]`, increment its count in the map. If this room was not previously present, the number of distinct rooms increases by one implicitly via map size.

4. **Restore validity when needed.**  
   While `map.size() > k`, shrink from the left: decrement `rooms[left]`, remove it from the map if its count becomes zero, then advance `left`.  
   **Invariant:** after this loop, the window `[left, right]` contains at most `k` distinct room IDs.

5. **Record the candidate answer.**  
   Once valid, compute `right - left + 1` and update `best`. This is correct because for the current `right`, any valid window starting left of `left` would have violated the distinct-room constraint.

6. **Continue until the array ends.**  
   Every index is processed once by `right` and removed at most once by `left`, giving linear total work. The frequency map is the minimal state needed to know when a room type fully exits the window.

## 📊 Worked Example
Example: `rooms = [4, 2, 2, 7, 2, 4, 4, 7]`, `k = 2`

| right | rooms[right] | Action | left | freq map | best |
|---|---:|---|---:|---|---:|
| 0 | 4 | add 4 | 0 | {4:1} | 1 |
| 1 | 2 | add 2 | 0 | {4:1, 2:1} | 2 |
| 2 | 2 | add 2 | 0 | {4:1, 2:2} | 3 |
| 3 | 7 | add 7, invalid → shrink | 1 | {2:2, 7:1} | 3 |
| 4 | 2 | add 2 | 1 | {2:3, 7:1} | 4 |
| 5 | 4 | add 4, invalid → shrink | 4 | {7:1, 2:1, 4:1} → {2:1, 4:1} | 4 |
| 6 | 4 | add 4 | 4 | {2:1, 4:2} | 4 |
| 7 | 7 | add 7, invalid → shrink | 5 | {4:2, 7:1} | 4 |

The maximum valid length is `4`, from subarray `[2, 2, 7, 2]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each room ID is added to the window once when `right` advances and removed at most once when `left` advances. Hash map updates are `O(1)` average-case, so the dominant cost is a single linear scan. At `10^6` elements this is practical; at `10^9`, throughput and memory locality become the real constraints, not asymptotics.

### Space Complexity
`O(min(n, k))` in practice, bounded by the number of distinct room IDs currently in the window and at most `n` overall. The frequency map owns the space. You cannot reduce this meaningfully without losing constant-time distinct tracking; trading it away usually forces slower rescans.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under an “at most `k` distinct” constraint, think sliding window immediately.
- When validity depends on counts of elements currently inside a range, a frequency map plus two pointers is usually the right state model.
- Remove a room ID from the map when its count reaches zero; keeping zero-count keys breaks the distinct-count invariant.
- Update the answer only after shrinking back to a valid window, and compute length as `right - left + 1` to avoid off-by-one errors.
- The production lesson is broader: incremental state maintenance beats repeated recomputation when processing high-volume ordered events.

## 🚀 Variations & Further Practice
- **Exactly `k` distinct rooms**: compute longest subarray with exactly `k` distinct values, which requires reasoning from “at most `k`” windows or maintaining stricter validity semantics.
- **Count of subarrays with at most / exactly `k` distinct**: same pattern, but instead of maximizing length, you aggregate how many valid windows end at each position.
- **Streaming version with eviction policies**: process an unbounded event stream while maintaining bounded-cardinality windows over time or sequence length, adding operational concerns like expiration and approximate counting.