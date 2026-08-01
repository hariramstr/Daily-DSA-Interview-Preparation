# Longest Meeting Stretch With Limited Late Arrivals

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given a binary array `arrivals` and an integer `k`, return the length of the longest contiguous subarray containing at most `k` zeros. Here, `1` means on-time and `0` means late, but the domain story is irrelevant to the core task: maximize window length under a bounded violation count. The challenge is scale: with up to `200000` elements, enumerating all subarrays is too slow, so the solution must exploit the structure of contiguous ranges.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval that tolerates a bounded number of failures: streaming pipelines allowing a few malformed events, SLO monitoring that permits limited error bursts, fraud/risk scoring over event sequences, and observability systems identifying the longest “healthy enough” service window. At scale, brute-force interval scans collapse under quadratic behavior and become unusable in hot paths or real-time analytics. Sliding-window formulations turn these into single-pass computations, enabling online processing, predictable latency, and memory-stable implementations that fit both batch jobs and continuously evaluated streams.

## 🔍 Problem Statement
You are given a binary array `arrivals` where `arrivals[i] ∈ {0,1}` and an integer `k`. Find the maximum length of a contiguous subarray containing at most `k` zeros. Equivalently, you may “excuse” up to `k` late arrivals within a chosen contiguous block, and you want the longest such block.

Constraints:

- `1 <= arrivals.length <= 200000`
- `arrivals[i]` is either `0` or `1`
- `0 <= k <= arrivals.length`

Examples:

- `arrivals = [1,1,0,1,0,1,1,1], k = 1` → `5`
- `arrivals = [0,0,1,1,1,0,1,1], k = 2` → `7`

Edge cases matter: `k = 0` means the answer is the longest run of `1`s; `k >= number_of_zeros` means the entire array is valid. The key algorithmic constraint is input size: checking every subarray would be `O(n²)`, which is not acceptable here.

## 🪜 How to Solve This
1. Read the problem → the word “contiguous” should immediately rule out sorting, prefix grouping, or arbitrary selection. We are choosing a window in-place.

2. Notice the rule is not about exact composition, only a bounded count: “at most `k` zeros.” That is a classic sliding-window signal because validity can be tracked incrementally.

3. Start with a window `[left, right]` and expand `right` one slot at a time. Every time you include a `0`, increase a `zeroCount`.

4. If `zeroCount <= k`, the current window is valid, so it is a candidate answer.

5. If `zeroCount > k`, the window became invalid. Do not restart from scratch. Instead, move `left` forward until the window is valid again, decrementing `zeroCount` when a zero leaves the window.

6. This works because once a window violates the constraint, any larger window with the same `left` also violates it. Shrinking is the only productive move.

7. Since each pointer only moves forward, the whole scan is linear.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use the **Sliding Window / Two Pointers** pattern with `left = 0`, `zeroCount = 0`, and `maxLen = 0`. The active window is always `arrivals[left..right]`.

2. **Expand the right boundary**  
   Iterate `right` from `0` to `n - 1`. When `arrivals[right] == 0`, increment `zeroCount`. This updates the violation count for the newly expanded window.

3. **Restore validity when needed**  
   If `zeroCount > k`, the current window is invalid. Move `left` rightward until `zeroCount <= k` again. Whenever `arrivals[left] == 0`, decrement `zeroCount` before advancing `left`.  
   **Invariant:** after this shrink phase, the window contains at most `k` zeros.

4. **Record the best valid window**  
   Once valid, compute `currentLen = right - left + 1` and update `maxLen = max(maxLen, currentLen)`. This is correct because every valid window ending at `right` and starting before `left` would have been invalid; the current `left` is the earliest valid start after repair.

5. **Why this abstraction fits**  
   The problem asks for the longest contiguous region under a monotone constraint: adding elements can only increase or preserve the zero count, and shrinking from the left can only decrease or preserve it. That monotonicity is exactly why two pointers work.

6. **Termination and correctness**  
   Each element enters the window once via `right` and leaves at most once via `left`. No candidate window is missed because every maximal valid window is observed when `right` reaches its endpoint and `left` has been minimally advanced to restore validity.

## 📊 Worked Example
Example: `arrivals = [1,1,0,1,0,1,1,1], k = 1`

| right | arrivals[right] | zeroCount after add | left after shrink | valid window        | length | maxLen |
|------:|-----------------:|--------------------:|------------------:|---------------------|-------:|-------:|
| 0     | 1                | 0                   | 0                 | `[1]`               | 1      | 1      |
| 1     | 1                | 0                   | 0                 | `[1,1]`             | 2      | 2      |
| 2     | 0                | 1                   | 0                 | `[1,1,0]`           | 3      | 3      |
| 3     | 1                | 1                   | 0                 | `[1,1,0,1]`         | 4      | 4      |
| 4     | 0                | 2                   | 3                 | `[1,0]`             | 2      | 4      |
| 5     | 1                | 1                   | 3                 | `[1,0,1]`           | 3      | 4      |
| 6     | 1                | 1                   | 3                 | `[1,0,1,1]`         | 4      | 4      |
| 7     | 1                | 1                   | 3                 | `[1,0,1,1,1]`       | 5      | 5      |

Answer: `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed at most twice: once when `right` includes it, and at most once when `left` excludes it. There are no nested rescans. At `10^6` elements this remains practical in a single pass; at `10^9`, the algorithm is still asymptotically optimal, though runtime becomes dominated by raw I/O and memory bandwidth.

### Space Complexity
`O(1)`. The algorithm stores only a few integers: `left`, `right`, `zeroCount`, and `maxLen`. No auxiliary array or map is required. Space cannot be meaningfully reduced further without losing the ability to track window validity and best length.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under an **“at most K bad items”** constraint, think sliding window immediately.
- When the validity metric can be updated incrementally as the window expands and shrinks, two pointers usually beat prefix-based or brute-force approaches.
- Shrink only while `zeroCount > k`; shrinking earlier loses valid length and can hide the true maximum.
- Be careful with window length: for inclusive bounds, it is always `right - left + 1`, not `right - left`.
- The transferable design insight: bounded-error interval detection often becomes linear once you model the constraint as a monotone window invariant.

## 🚀 Variations & Further Practice
- **Longest Repeating Character Replacement** — same sliding-window core, but the constraint depends on window size and the frequency of the dominant character rather than a direct zero count.
- **Max Consecutive Ones III** — identical structure with different framing; useful for recognizing the pattern independent of domain language.
- **Minimum Size Subarray Sum / shortest valid window variants** — same two-pointer machinery, but optimization flips from maximizing to minimizing, which changes when you record answers and how aggressively you shrink.