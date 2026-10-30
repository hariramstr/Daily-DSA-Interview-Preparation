# Shortest Sensor Window Covering Required Device Types

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given a chronological array of sensor event types and a map of required per-type quotas, find the length of the shortest contiguous window whose counts meet or exceed every quota. Return `-1` if no such window exists. The challenge is not membership but multiplicity: multiple device types may require multiple occurrences, event names are arbitrary strings, and the input can be large enough that any nested-window scan is immediately non-viable.

## 🌍 Engineering Impact
This pattern shows up in stream processing, observability pipelines, fraud detection, and log analytics: find the smallest time-ordered segment that satisfies a multi-signal condition. Think Kafka consumers detecting correlated events, SIEM systems searching for attack signatures, or ranking pipelines requiring a minimum mix of feature hits before scoring. At scale, brute-force window enumeration collapses under cardinality and throughput. A linear sliding-window design enables online processing, bounded state, and predictable latency. Without it, systems either overprovision heavily or fall back to batch scans that destroy freshness and operational cost efficiency.

## 🔍 Problem Statement
You are given:

- `events`: an array of strings, where `events[i]` is the device type of the `i`-th event
- `required`: a map where `required[t]` is the minimum count of type `t` that must appear in a valid contiguous window

Return the minimum length of a contiguous subarray whose frequency map covers all quotas in `required`. If no window can satisfy every required type, return `-1`.

Constraints:

- `1 <= events.length <= 200000`
- `1 <= required.size() <= 50000`
- device names are arbitrary lowercase strings, digits, or underscores
- only keys present in `required` affect validity
- the sum of required counts may exceed `events.length`, making failure possible

Examples:

- `events = ["cam","temp","cam","door","temp","cam","door"]`, `required = {"cam":2,"door":1}` → `4`
- `events = ["a","b","c","a","b","a","d"]`, `required = {"a":2,"b":2,"d":1}` → `6`

The key algorithmic constraint is input size: anything worse than near-linear time is the wrong shape.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so sorting or regrouping destroys the only structure that matters.
2. Notice validity depends on **counts inside a moving range**, not just presence → this suggests a frequency map over a window.
3. Ask what changes when the window expands or shrinks → only one event enters or leaves at a time, so counts can be updated incrementally.
4. That leads directly to **Two Pointers / Sliding Window**: move the right pointer to accumulate events until all quotas are satisfied.
5. Once the window is valid, the left pointer becomes actionable → shrink aggressively to remove unnecessary prefix events while preserving validity.
6. Track not total matched events, but how many required device types currently meet their quota. This avoids rescanning the entire `required` map after each pointer move.
7. Ignore non-required event types except as window length contributors; they should not inflate bookkeeping cost.
8. The result is a single pass where each pointer moves at most `n` times, which is exactly the shape demanded by the constraints.

## 🧩 Algorithm Walkthrough
1. **Initialize requirement state.**  
   Store `required` as a hash map. Maintain another map `windowCount` for counts inside the current window. Also track `formed`, the number of required device types whose current count has reached the needed quota. The target is `need = required.size()`.

2. **Expand with the right pointer.**  
   Iterate `right` from `0` to `events.length - 1`. If `events[right]` is not in `required`, it does not affect validity, so only the window length changes. If it is required, increment its count in `windowCount`.

3. **Update satisfaction state.**  
   When a required type’s count becomes exactly equal to its quota, increment `formed`. This is the critical invariant: `formed == need` iff the current window satisfies all quotas.

4. **Shrink with the left pointer while valid.**  
   Once `formed == need`, record the current window length as a candidate answer. Then move `left` rightward to minimize the window. For each removed event, decrement its count if it is required. If a count drops below its quota, decrement `formed` and stop shrinking.

5. **Preserve the minimal-valid-window invariant.**  
   At every point where the inner loop exits, the window is the smallest invalid-after-removal boundary for the current `right`. That guarantees no shorter valid window ending at this `right` was skipped.

6. **Return the result.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum recorded length.

This is the canonical **Sliding Window + Hash Map + Two Pointers** pattern because the problem asks for a shortest contiguous range under incremental frequency constraints.

## 📊 Worked Example
Use `events = ["cam","temp","cam","door","temp","cam","door"]`, `required = {"cam":2,"door":1}`.

| Step | right | event  | window counts (`cam`,`door`) | formed | left | valid? | best |
|---|---:|---|---|---:|---:|---|---:|
| 1 | 0 | cam  | (1,0) | 0 | 0 | no  | ∞ |
| 2 | 1 | temp | (1,0) | 0 | 0 | no  | ∞ |
| 3 | 2 | cam  | (2,0) | 1 | 0 | no  | ∞ |
| 4 | 3 | door | (2,1) | 2 | 0 | yes | 4 |
| 5 | shrink | remove cam | (1,1) | 1 | 1 | no  | 4 |
| 6 | 4 | temp | (1,1) | 1 | 1 | no  | 4 |
| 7 | 5 | cam  | (2,1) | 2 | 1 | yes | 4 |
| 8 | shrink temp | (2,1) | 2 | 2 | yes | 4 |
| 9 | shrink cam | (1,1) | 1 | 3 | no  | 4 |

Shortest valid window is length `4`, e.g. `events[2..5] = ["cam","door","temp","cam"]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average-case, where `n = events.length`, assuming hash map operations are `O(1)` amortized. Each event is processed once when `right` expands and at most once when `left` contracts. At `10^6` elements this is practical; at `10^9`, the algorithmic shape is still correct but I/O and memory locality dominate system behavior.

### Space Complexity
`O(k)`, where `k` is the number of distinct required device types actually tracked in the maps. The space is owned by `required` and `windowCount`. It can only be reduced by sacrificing direct lookup efficiency, which is usually the wrong trade-off for large streams.

## 💡 Key Takeaways
- If the problem asks for the **shortest contiguous range** satisfying per-key frequency constraints, think sliding window before considering any global reordering.
- If validity depends on **meeting quotas across multiple categories**, track “how many categories are satisfied,” not repeated full-map comparisons.
- Increment `formed` only when a count becomes **exactly equal** to its required quota; incrementing on `>` is a correctness bug.
- When shrinking, update the answer **before** removing `events[left]`, otherwise you can miss the current minimal valid window.
- In production streams, this pattern is the difference between bounded incremental state and repeated rescans that turn a real-time problem into a batch system.

## 🚀 Variations & Further Practice
- Return the actual window indices or subarray, not just length; same pattern, but now correctness depends on preserving boundaries alongside the best length.
- Add weighted quotas or per-event costs and ask for the minimum-cost valid window; the conceptual twist is that shortest length no longer aligns with optimality.
- Process an unbounded stream with expirations by timestamp instead of index; the harder part is maintaining validity under time-based eviction rather than simple pointer movement.