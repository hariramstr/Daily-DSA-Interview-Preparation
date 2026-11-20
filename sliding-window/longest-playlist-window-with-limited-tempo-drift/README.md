# Longest Playlist Window With Limited Tempo Drift

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Array

---

## 🗂 Problem Overview
Given an array `tempo` and a non-negative integer `limit`, find the maximum length of a contiguous subarray whose maximum BPM minus minimum BPM is at most `limit`. The output is only the length. The challenge is maintaining window min/max efficiently while scanning up to `200000` songs. Recomputing min and max for every candidate window turns this into quadratic work, which is too slow for large playlists.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need a longest stable interval under bounded variance: telemetry smoothing in streaming pipelines, fraud detection windows over transaction values, QoS monitoring for latency drift, and adaptive bitrate selection over recent network samples. At scale, naive rescans of each window collapse under high-cardinality streams and tight latency budgets. The monotonic-queue sliding-window approach gives deterministic linear throughput, bounded per-element work, and predictable memory behavior. That matters in production because it enables online processing, avoids heap-heavy structures, and keeps tail latency stable even when input distributions are adversarial.

## 🔍 Problem Statement
You are given an integer array `tempo` where `tempo[i]` is the BPM of the `i`-th song in playlist order, and an integer `limit`. Return the length of the longest contiguous subarray `tempo[l..r]` such that:

`max(tempo[l..r]) - min(tempo[l..r]) <= limit`

Constraints:

- `1 <= tempo.length <= 200000`
- `1 <= tempo[i] <= 1000000000`
- `0 <= limit <= 1000000000`

Examples:

- `tempo = [98, 101, 100, 108, 107, 103], limit = 5` → `3`
- `tempo = [120, 124, 122, 121, 119, 130, 118], limit = 4` → `4`

Edge cases matter: `limit = 0` means all values in the window must be identical; arrays of length `1` always return `1`. The key constraint is input size: any approach worse than near-linear time will not scale.

## 🪜 How to Solve This
1. Read the requirement carefully → this is about the **longest contiguous window**, so sorting is irrelevant because order must be preserved.

2. Ask what makes a window valid → only one condition matters: current `max - min <= limit`.

3. If we expand the right boundary one song at a time, we need the current min and max immediately. Recomputing them by scanning the whole window each time is too expensive.

4. That suggests a sliding window with two pointers: grow `right`, and when the window becomes invalid, move `left` until it becomes valid again.

5. The remaining problem is data structure choice → we need a structure that supports:
   - pushing a new value,
   - removing expired values from the left,
   - reading current min and max in O(1).

6. Monotonic deques fit exactly:
   - one decreasing deque for the maximum,
   - one increasing deque for the minimum.

7. Each element enters and leaves each deque at most once, so the whole scan is linear. That is the key observation that turns an apparent O(n²) search into O(n).

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` and extend `right` across the array. This is the right abstraction because validity depends on a contiguous range, and invalid windows can be repaired only by shrinking from the left.

2. **Track candidates for maximum with a decreasing deque.**  
   Before appending `tempo[right]`, pop smaller values from the back. The deque then stays in non-increasing order, so its front is always the maximum of the current window.

3. **Track candidates for minimum with an increasing deque.**  
   Before appending `tempo[right]`, pop larger values from the back. The deque stays in non-decreasing order, so its front is always the minimum of the current window.

4. **Check validity after each expansion.**  
   If `maxDeque.front - minDeque.front > limit`, the window is invalid. Shrink from the left until the constraint is restored.

5. **Expire values leaving the window.**  
   When moving `left`, if `tempo[left]` equals the front of either deque, pop that front. This preserves the invariant that both deque fronts belong to the active window.

6. **Update the answer only when the window is valid.**  
   After all required shrinking, `[left, right]` is the longest valid window ending at `right`. Update `best = max(best, right - left + 1)`.

7. **Why this is correct.**  
   The deques always expose the true min and max of the current window, and `left` only moves forward. Since every element is inserted and removed at most once per deque, the algorithm is linear and exact.

## 📊 Worked Example
Example: `tempo = [98, 101, 100, 108, 107, 103]`, `limit = 5`

| right | value | maxDeque | minDeque | left | valid? | best |
|---|---:|---|---|---:|---|---:|
| 0 | 98  | [98] | [98] | 0 | yes | 1 |
| 1 | 101 | [101] | [98, 101] | 0 | yes (`101-98=3`) | 2 |
| 2 | 100 | [101, 100] | [98, 100] | 0 | yes (`101-98=3`) | 3 |
| 3 | 108 | [108] | [98, 100, 108] | 0 | no (`108-98=10`) | 3 |
|   | shrink | [108] | [100, 108] | 1 | no | 3 |
|   | shrink | [108] | [100, 108] | 2 | no (`108-100=8`) | 3 |
|   | shrink | [108] | [108] | 3 | yes | 3 |
| 4 | 107 | [108, 107] | [107] | 3 | yes (`108-107=1`) | 3 |
| 5 | 103 | [108, 107, 103] | [103] | 3 | yes (`108-103=5`) | 3 |

Answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each song is pushed into and popped from each deque at most once, and both pointers move only forward. There is no nested rescanning of windows. At `10^6` elements this remains practical; at `10^9`, linear time is still expensive, but anything superlinear is immediately infeasible.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when the window is strictly monotonic and large. In practice it is bounded by window size. You cannot reduce this to `O(1)` while preserving exact online min/max updates without paying more per operation.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous range** under a threshold involving current min/max, think sliding window before considering brute force or sorting.
- If a window needs **constant-time access to both min and max under insert/expire**, monotonic deques are the signal, not heaps alone.
- Shrink while the window is invalid, not just once; a single new element can require multiple left moves.
- Be precise about expiring deque fronts: remove them only when the outgoing `tempo[left]` matches the front value.
- In production stream processing, the broader lesson is to maintain just enough ordered state to support online decisions, rather than recomputing summaries over every moving range.

## 🚀 Variations & Further Practice
- Return the actual window boundaries, or all maximal windows, instead of just the length; the core algorithm stays the same, but result tracking becomes more subtle under ties.
- Replace `max - min <= limit` with a rolling median or percentile constraint; this breaks the monotonic-deque trick and typically requires balanced trees or indexed heaps.
- Support updates to arbitrary positions between queries; this shifts the problem from online scanning to dynamic range queries, where segment trees or Fenwick-tree-adjacent designs become relevant.