# Longest Log Span With Unique Event Signatures

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Sliding Window, String

---

## 🗂 Problem Overview
Given a chronological array of log event signatures, find the maximum length of any contiguous span in which every signature appears at most once. The input is an array of strings; the output is a single integer. The challenge is not correctness but efficiency: with up to 100,000 entries, any approach that rechecks subarrays or uses nested scans will miss the required time bound. This is fundamentally a longest-unique-window problem.

## 🌍 Engineering Impact
This pattern shows up anywhere systems process ordered streams and need the longest conflict-free segment: observability pipelines scanning logs for non-repeating event sequences, fraud systems detecting unique action bursts, compiler/token streams finding maximal distinct spans, and stream processors enforcing temporary uniqueness constraints. At scale, brute-force rescans turn into latency cliffs and memory churn. A sliding-window plus hash-index approach preserves linear throughput, supports online processing, and cleanly maps to architectures where data arrives incrementally rather than as a batch you can repeatedly revisit.

## 🔍 Problem Statement
You are given `events`, an array of strings in chronological order, where `events[i]` is the signature of the `i`-th log entry. Return the length of the longest contiguous subarray containing only unique signatures.

Two entries are equal only if their strings match exactly. The chosen span must be contiguous: if you select indices `l` and `r`, you include every event from `l` through `r`.

**Constraints**
- `1 <= events.length <= 100000`
- `1 <= events[i].length <= 50`
- Signatures contain lowercase letters, digits, underscores, and hyphens
- Full-credit solutions must run in `O(n)` or `O(n log n)`

**Examples**
- `["auth_ok", "cache_miss", "db_retry", "cache_miss", "email_sent"] -> 3`
- `["x1", "x2", "x3", "x2", "x4", "x5"] -> 4`

The key constraint is input size: quadratic subarray checking is not viable.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous** segment, not just the count of distinct values overall. That immediately suggests a moving window rather than global deduplication.

2. Notice the failure mode → a window becomes invalid only when the current event has already appeared inside it. So the only thing that matters is where that signature was last seen.

3. That implies a hash map → map each signature to its most recent index. Hash lookup gives constant-time duplicate detection.

4. Maintain two pointers → `left` is the start of the current valid window, `right` expands one event at a time.

5. On each event:
   - If it has not been seen inside the current window, extend normally.
   - If it was seen at index `j >= left`, move `left` to `j + 1` to exclude the earlier duplicate.

6. Update the last-seen index and compute window length `right - left + 1`.

The key insight: never move `left` backward. Each index enters and leaves the window at most once, which is why the solution is linear.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window + Hash Map pattern.**  
   This problem asks for the longest contiguous region satisfying a uniqueness constraint. That is the canonical signal for a sliding window. The hash map stores `signature -> lastSeenIndex`, which is the minimum state needed to repair the window when a duplicate appears.

2. **Initialize state.**  
   Set `left = 0`, `best = 0`, and an empty map `lastSeen`.  
   Invariant: before processing `events[right]`, the window `events[left..right-1]` contains no duplicates.

3. **Scan from left to right with `right`.**  
   For each `events[right]`, check whether it exists in `lastSeen`. If not, adding it preserves uniqueness.

4. **Handle duplicates only when they are inside the current window.**  
   If `events[right]` was last seen at index `j` and `j >= left`, then the current window would contain two copies of the same signature. Move `left` to `j + 1`.  
   Why this is correct: any valid window ending at `right` must exclude the earlier occurrence, and `j + 1` is the furthest-left valid start.

5. **Update the map with the current index.**  
   Set `lastSeen[events[right]] = right`.  
   Invariant: the map always records the latest index of every signature seen so far.

6. **Compute the current valid window length.**  
   `windowLen = right - left + 1`, then `best = max(best, windowLen)`.  
   This works because after any necessary `left` adjustment, `events[left..right]` is guaranteed duplicate-free.

7. **Return `best`.**  
   The algorithm is optimal because each pointer moves monotonically rightward, avoiding rescans.

## 📊 Worked Example
Example: `events = ["x1", "x2", "x3", "x2", "x4", "x5"]`

| right | event | last seen | left before | left after | window | best |
|---|---|---:|---:|---:|---|---:|
| 0 | x1 | - | 0 | 0 | [x1] | 1 |
| 1 | x2 | - | 0 | 0 | [x1, x2] | 2 |
| 2 | x3 | - | 0 | 0 | [x1, x2, x3] | 3 |
| 3 | x2 | 1 | 0 | 2 | [x3, x2] | 3 |
| 4 | x4 | - | 2 | 2 | [x3, x2, x4] | 3 |
| 5 | x5 | - | 2 | 2 | [x3, x2, x4, x5] | 4 |

At `right = 3`, `x2` repeats and its previous index `1` is still inside the window `[0..2]`, so `left` jumps to `2`. That removes the older `x2` in one step. The final longest valid span is length `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time, where `n` is the number of events. Each event is processed once, and each hash map lookup/update is constant time on average. At `10^6` elements this remains practical in a single pass; at `10^9`, the algorithmic shape is still right, but memory bandwidth and storage dominate.

### Space Complexity
`O(k)` space, where `k` is the number of distinct signatures currently tracked, bounded by the total number of distinct strings seen. The hash map owns the extra space. You cannot reduce this meaningfully without giving up constant-time duplicate checks.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a validity rule that can break when one new item arrives, think sliding window immediately.
- If validity depends on whether an element has appeared before, a hash map of last-seen positions is usually the right companion structure.
- The critical update is `left = max(left, lastSeen[event] + 1)`; without the `max`, you can move `left` backward and corrupt the window.
- Compute window length **after** adjusting `left` for duplicates, or you will overcount invalid spans by one iteration.
- In production stream processing, this pattern matters because it converts repeated rescans into a single-pass state machine with predictable throughput.

## 🚀 Variations & Further Practice
- **Longest substring without repeating characters**: same pattern on characters instead of strings; the conceptual twist is recognizing the exact same abstraction under different data representation.
- **Longest subarray with at most `K` distinct values**: still sliding window, but validity depends on distinct-count cardinality rather than duplicate presence.
- **Smallest window containing all required signatures**: flips the optimization target from maximum valid span to minimum covering span, requiring frequency accounting instead of only last-seen indices.