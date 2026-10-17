# Longest Sleep Tracker Streak With Limited Wake Spikes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given a binary array `states` and an integer `k`, find the maximum length of a contiguous segment containing at most `k` zeros. Those zeros represent awake minutes that may be ignored and treated as sleep. The output is a single integer: the longest valid sleep streak length. The non-trivial part is scale: with up to 200,000 samples, any approach that checks many subarrays explicitly becomes too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere systems tolerate bounded anomalies inside otherwise continuous behavior. Examples include observability pipelines detecting sustained healthy intervals despite brief telemetry gaps, fraud systems finding long “normal” sessions with limited suspicious events, media QoE tracking uninterrupted playback with a few dropped frames, and distributed rate-limit analysis over noisy event streams. At scale, brute-force interval evaluation collapses under quadratic growth. A sliding-window formulation enables single-pass processing, low memory overhead, and straightforward adaptation to streaming or online analytics where data arrives incrementally and decisions must be updated continuously.

## 🔍 Problem Statement
You are given an integer array `states` of length `n` where each element is either `1` or `0`:

- `1` = asleep for that minute
- `0` = awake for that minute

You may convert at most `k` awake minutes (`0`s) into sleep (`1`s). Return the length of the longest contiguous subarray that contains no more than `k` zeros.

Constraints:

- `1 <= states.length <= 200000`
- `states[i] ∈ {0, 1}`
- `0 <= k <= states.length`

Examples:

- `states = [1,1,0,1,0,1,1,1], k = 1` → `5`
- `states = [0,0,1,1,1,0,1,1,0], k = 2` → `7`

Edge cases matter:

- `k = 0`: find the longest run of existing `1`s
- all `1`s: answer is `n`
- `k >= number of zeros`: answer is `n`

The key algorithmic constraint is `n = 200000`, which rules out enumerating all subarrays.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** interval, not arbitrary picks. That immediately suggests a window over the original array.

2. The validity rule is simple: a window is acceptable if it contains at most `k` zeros. So instead of asking “which zeros should I flip?”, ask “what is the largest interval whose zero count stays within budget?”

3. Once the condition depends only on counts inside a moving interval, think **sliding window / two pointers**:
   - expand the right edge to include more minutes
   - track how many zeros are inside
   - if zeros exceed `k`, shrink from the left until the window is valid again

4. Why this works: for each right endpoint, the best valid interval ending there is the widest one you can keep after enforcing `zeros <= k`.

5. This avoids rechecking subarrays from scratch. Each pointer only moves forward, so the whole scan is linear.

6. The result is the maximum window width seen during the pass.

That is the core recognition pattern: contiguous range + bounded “bad” elements + maximize length ⇒ sliding window.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and counters.**  
   Set `left = 0`, `zeroCount = 0`, and `best = 0`.  
   This defines the current window as `states[left..right]`. The invariant we want is: after any necessary shrinking, the window contains at most `k` zeros.

2. **Expand the window with `right`.**  
   Iterate `right` from `0` to `n - 1`. If `states[right] == 0`, increment `zeroCount`.  
   This grows the candidate interval by one minute while maintaining an accurate count of wake spikes inside the window.

3. **Restore validity when the budget is exceeded.**  
   While `zeroCount > k`, move `left` rightward. If `states[left] == 0`, decrement `zeroCount` before advancing `left`.  
   This is the critical Two Pointers step: shrink only as much as needed to reestablish the invariant `zeroCount <= k`.

4. **Record the best valid window.**  
   Once valid, compute `windowLength = right - left + 1` and update `best = max(best, windowLength)`.  
   This is correct because the current window is the longest valid one ending at `right`; any earlier `left` would violate the zero budget.

5. **Finish in one pass.**  
   Each element is added once by `right` and removed at most once by `left`.  
   That gives linear time and constant extra space, which is the right abstraction for large arrays and streaming-style interval analytics.

## 📊 Worked Example
Example: `states = [0,0,1,1,1,0,1,1,0]`, `k = 2`

| right | states[right] | zeroCount | left after shrink | window `[left..right]` | length | best |
|---|---:|---:|---:|---|---:|---:|
| 0 | 0 | 1 | 0 | `[0..0]` | 1 | 1 |
| 1 | 0 | 2 | 0 | `[0..1]` | 2 | 2 |
| 2 | 1 | 2 | 0 | `[0..2]` | 3 | 3 |
| 3 | 1 | 2 | 0 | `[0..3]` | 4 | 4 |
| 4 | 1 | 2 | 0 | `[0..4]` | 5 | 5 |
| 5 | 0 | 3 | 1 | `[1..5]` | 5 | 5 |
| 6 | 1 | 3 | 1 | `[1..6]` | 6 | 6 |
| 7 | 1 | 3 | 1 | `[1..7]` | 7 | 7 |
| 8 | 0 | 4 | 2 | `[2..8]` | 7 | 7 |

At `right = 8`, shrinking removes the zero at index `1`, restoring `zeroCount = 2`. The best valid streak remains `7`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. The dominant work is the single scan with two pointers. `right` advances `n` times, and `left` also advances at most `n` times total, never resetting. At `10^6` elements this is routine; at `10^9`, the algorithm is still asymptotically optimal but runtime becomes constrained by raw I/O and memory bandwidth.

### Space Complexity
`O(1)`. The algorithm stores only a few integers: two pointers, a zero counter, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further; the main trade-off is not memory but whether the input itself is batch-loaded or streamed.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous segment under a bounded count of “bad” values, that is a strong sliding-window signal.
- If validity depends on a simple aggregate inside a range, prefer two pointers over recomputing each subarray.
- Update the answer only after shrinking enough to restore `zeroCount <= k`; recording too early produces invalid lengths.
- Be precise with window length: for inclusive bounds, it is always `right - left + 1`.
- The production-grade insight is to model noisy event streams with tolerance budgets, then maintain maximal valid intervals incrementally instead of rescanning history.

## 🚀 Variations & Further Practice
- **Longest subarray with at most `k` distinct values**: same sliding-window pattern, but validity depends on a frequency map rather than a single zero counter.
- **Max consecutive ones III / binary stream with online updates**: same core problem, but framed as streaming ingestion where the answer must be updated continuously as new samples arrive.
- **Longest substring after replacing at most `k` characters**: similar window expansion, but the harder twist is maintaining the dominant character count while allowing heterogeneous replacements.