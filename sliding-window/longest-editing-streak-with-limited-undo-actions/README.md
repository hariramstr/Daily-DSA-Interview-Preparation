# Longest Editing Streak With Limited Undo Actions

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding-window, two-pointers, array

---

## 🗂 Problem Overview
Given a binary array `events` and an integer `k`, find the maximum length of a contiguous subarray containing at most `k` zeros. Here, `1` represents productive editing time and `0` represents undo or non-productive activity. The output is a single integer: the longest valid streak length. The challenge is scale: with up to `200000` elements, enumerating all subarrays is too slow, so the solution must exploit structure in contiguous ranges.

## 🌍 Engineering Impact
This pattern shows up in telemetry and stream analytics where systems must summarize noisy behavior while tolerating bounded anomalies: editor activity timelines, session quality scoring, fraud-event bursts, observability windows with limited error spikes, or rate-limit enforcement with temporary exceptions. At scale, brute-force range evaluation collapses under quadratic growth and becomes unusable in online pipelines or interactive dashboards. The sliding-window approach enables single-pass processing, bounded memory, and predictable latency, which matters for real-time metrics, edge processing, and high-volume event ingestion where every extra pass multiplies infrastructure cost.

## 🔍 Problem Statement
You are given an array `events` of length `n`, where each value is either `0` or `1`, and an integer `k`. A contiguous streak is considered valid if it contains at most `k` zeros. The task is to return the maximum possible length of any such contiguous subarray.

Constraints:
- `1 <= events.length <= 200000`
- `0 <= k <= events.length`
- `events[i] ∈ {0, 1}`

Examples:

- `events = [1,1,0,1,0,1,1,1], k = 1` → `4`
- `events = [0,1,1,0,1,1,0,1], k = 2` → `7`

Edge cases matter:
- `k = 0` means the answer is the longest run of consecutive `1`s.
- If `k` is at least the total number of zeros, the whole array is valid.
- Single-element arrays should return `1` regardless of value if `k >= 1`, otherwise only `1` when the element is `1`.

The key algorithmic constraint is input size: any `O(n^2)` subarray scan is too slow.

## 🪜 How to Solve This
1. Read the problem → we need the longest **contiguous** segment, so this is a range problem, not counting or sorting.
2. Notice the rule is “at most `k` zeros” → validity depends on a simple property of a window: how many zeros it contains.
3. When a subarray condition can be updated incrementally as boundaries move, think **sliding window / two pointers**.
4. Start with both pointers at the left. Expand the right pointer one step at a time, updating the zero count.
5. If the window becomes invalid (`zeroCount > k`), move the left pointer right until the window is valid again.
6. At every step, once the window is valid, its length is a candidate answer.
7. Why this works: for each right boundary, the best valid window ending there is the widest one after shrinking just enough. There is no reason to reconsider earlier left positions once they have been ruled out.
8. This gives a single linear pass: every element enters the window once and leaves once, which is exactly the efficiency the constraints demand.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` that always represents the current contiguous streak under consideration. This pattern fits because the validity rule depends only on the count of zeros inside the current range.

2. **Expand the window by moving `right` from `0` to `n - 1`.**  
   For each new element, include `events[right]` in the window. If it is `0`, increment `zeroCount`. This updates the state in `O(1)` time.

3. **Restore validity when the constraint is violated.**  
   If `zeroCount > k`, the window is invalid. Move `left` forward until `zeroCount <= k` again. Whenever `events[left]` is `0`, decrement `zeroCount` before advancing.  
   **Invariant:** after shrinking, the window always contains at most `k` zeros.

4. **Record the best valid length.**  
   Once the invariant holds, compute `right - left + 1` and update the maximum. This is correct because the current window is the widest valid window ending at `right`; shrinking any less would violate the constraint, and shrinking more would only reduce length.

5. **Finish after one pass.**  
   Each pointer moves monotonically to the right and never retreats. That guarantees linear runtime and constant auxiliary space.

This abstraction is the right one because the problem asks for an optimal contiguous range under a bounded-violation constraint, which is exactly what sliding windows are designed to solve efficiently.

## 📊 Worked Example
Example: `events = [0,1,1,0,1,1,0,1], k = 2`

| right | events[right] | zeroCount after add | left after shrink | window `[left..right]` | length | best |
|---|---:|---:|---:|---|---:|---:|
| 0 | 0 | 1 | 0 | `[0..0]` | 1 | 1 |
| 1 | 1 | 1 | 0 | `[0..1]` | 2 | 2 |
| 2 | 1 | 1 | 0 | `[0..2]` | 3 | 3 |
| 3 | 0 | 2 | 0 | `[0..3]` | 4 | 4 |
| 4 | 1 | 2 | 0 | `[0..4]` | 5 | 5 |
| 5 | 1 | 2 | 0 | `[0..5]` | 6 | 6 |
| 6 | 0 | 3 | 1 | `[1..6]` | 6 | 6 |
| 7 | 1 | 2 | 1 | `[1..7]` | 7 | 7 |

At `right = 6`, the third zero makes the window invalid, so `left` advances past index `0`, removing one zero. The final best window is `[1..7]`, length `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. The dominant work is advancing the two pointers across the array. `right` visits each element once, and `left` also advances at most `n` times total, so the total number of pointer moves is linear. At `10^6` elements this is routine; at `10^9`, linear time is still expensive but remains the only viable asymptotic class for a single-machine scan.

### Space Complexity
`O(1)`. The algorithm stores only a few integers: `left`, `right`, `zeroCount`, and `best`. No auxiliary array or hash structure is required. Space cannot be meaningfully reduced below constant without losing the ability to track window state, and there is no trade-off worth making here.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under an “at most / at least” constraint, sliding window should be one of the first patterns you test.
- When window validity depends on a count that can be updated incrementally, two pointers usually beat any prefix-sum-plus-nested-scan approach.
- Be careful to shrink only while `zeroCount > k`; using `>= k` incorrectly discards windows that are still valid.
- Window length is `right - left + 1`; missing the `+1` is the most common off-by-one error in this pattern.
- In production stream processing, bounded-noise summarization is often a window-maintenance problem; the right abstraction turns an expensive retrospective computation into an online linear pass.

## 🚀 Variations & Further Practice
- **Longest subarray with at most `k` distinct values**: same sliding-window skeleton, but replace a zero counter with a frequency map and a distinct-count invariant.
- **Max consecutive ones after flipping exactly `k` zeros**: similar mechanics, but the “exactly” requirement changes result handling and edge-case logic.
- **Shortest subarray with sum at least `target`**: still a window problem, but the optimization direction flips from maximizing a valid window to minimizing one after reaching feasibility.