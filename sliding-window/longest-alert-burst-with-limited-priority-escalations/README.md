# Longest Alert Burst With Limited Priority Escalations

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, String

---

## 🗂 Problem Overview
Given a string `alerts` of `'L'` and `'H'`, return the length of the longest contiguous substring containing at most `k` high-priority alerts. Those `H` values are the only ones that need “fixing”; if a window has at most `k` of them, it can be treated as entirely low priority. The challenge is scale: with input length up to `200000`, any approach that checks many substrings explicitly will time out.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous segment that violates a policy only within a bounded budget. Examples include log analysis pipelines tolerating a limited number of error events, streaming fraud detectors allowing a capped number of anomalies in a session, QoS monitoring that accepts a few packet drops inside an otherwise healthy interval, and search/session analytics looking for mostly-clean user journeys. At production scale, brute-force substring evaluation collapses under quadratic growth. A sliding-window design enables single-pass processing, predictable memory use, and easy adaptation to online or near-real-time stream processing architectures.

## 🔍 Problem Statement
You are given a string `alerts` where each character is either `'L'` or `'H'`, and an integer `k`. You may choose one contiguous substring and escalate at most `k` occurrences of `'H'` inside it so that those alerts are treated as `'L'`. The task is to return the maximum possible length of such a substring.

Formally, find the longest contiguous window containing at most `k` characters equal to `'H'`.

Constraints:
- `1 <= alerts.length <= 200000`
- `alerts[i]` is either `'L'` or `'H'`
- `0 <= k <= alerts.length`

Examples:
- `alerts = "LLHLLHLLL", k = 1` → `5`
- `alerts = "HHLLLHLH", k = 2` → `6`

Important edge cases:
- `k = 0`: longest run of existing `'L'`
- `k >= count('H')`: entire string is valid
- Large input size rules out quadratic substring enumeration

## 🪜 How to Solve This
1. Read the requirement carefully → we need the **longest contiguous window**, not a count of all valid windows and not a transformed string.
2. The only thing that makes a window invalid is having **more than `k` `'H'` characters**. That means window validity depends on a simple running statistic.
3. When validity depends on a count inside a contiguous range, think **sliding window / two pointers**.
4. Start with both pointers at the left. Expand the right pointer one character at a time, updating how many `'H'` values are inside the current window.
5. If the window exceeds the allowed escalation budget (`highCount > k`), it is invalid. The only way to restore validity while preserving contiguity is to move the left pointer rightward until the count is back within budget.
6. After each expansion and any required shrinking, the current window is the **largest valid window ending at this right boundary**. Record its length.
7. Because each pointer only moves forward, the whole scan is linear. That is exactly the kind of constraint-driven reasoning that makes this pattern the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Two Pointers / Sliding Window.**  
   This problem asks for the longest contiguous region satisfying a budget constraint: at most `k` high-priority alerts. That is the canonical sliding-window shape because validity can be maintained incrementally.

2. **Initialize window state.**  
   Set `left = 0`, `highCount = 0`, and `best = 0`. The window is always `alerts[left..right]`.

3. **Expand the window with `right`.**  
   For each character at index `right`, include it in the window. If `alerts[right] == 'H'`, increment `highCount`.  
   Invariant: `highCount` equals the number of `'H'` characters currently inside the window.

4. **Restore validity when over budget.**  
   While `highCount > k`, move `left` rightward. If `alerts[left] == 'H'`, decrement `highCount` before advancing `left`.  
   Why this is correct: any valid window ending at `right` must start no earlier than the first position that keeps `highCount <= k`.

5. **Record the best valid length.**  
   Once the shrink loop finishes, the current window is valid. Update `best = max(best, right - left + 1)`.  
   Invariant: after each iteration, `alerts[left..right]` is the longest valid window ending at `right`.

6. **Finish after one pass.**  
   Since `left` and `right` each move at most `n` times, the algorithm is `O(n)` with `O(1)` extra space. This is the right abstraction because the problem is not about transforming characters globally; it is about maintaining a locally valid contiguous segment under a bounded violation count.

## 📊 Worked Example
Example: `alerts = "HHLLLHLH"`, `k = 2`

| right | char | highCount after add | left after shrink | valid window | length | best |
|---|---|---:|---:|---|---:|---:|
| 0 | H | 1 | 0 | `H` | 1 | 1 |
| 1 | H | 2 | 0 | `HH` | 2 | 2 |
| 2 | L | 2 | 0 | `HHL` | 3 | 3 |
| 3 | L | 2 | 0 | `HHLL` | 4 | 4 |
| 4 | L | 2 | 0 | `HHLLL` | 5 | 5 |
| 5 | H | 3 | 1 | `HLLLH` | 5 | 5 |
| 6 | L | 2 | 1 | `HLLLHL` | 6 | 6 |
| 7 | H | 3 | 2 | `LLLHLH` | 6 | 6 |

At `right = 5` and `right = 7`, the window exceeds the escalation budget, so `left` advances until only two `'H'` values remain. The maximum valid length observed is `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = alerts.length`. Each character is processed once when `right` expands the window, and at most once again when `left` shrinks it. At `10^6` elements this is routine in memory-resident processing; at `10^9`, the algorithmic shape is still right, but I/O and streaming execution dominate.

### Space Complexity
`O(1)` extra space. The algorithm stores only pointer indices, a counter for `'H'`, and the best length seen so far. Space cannot meaningfully be reduced further; the main trade-off is only whether the input is held in memory or streamed.

## 💡 Key Takeaways
- If the prompt asks for a **longest contiguous segment** under a bounded number of “bad” elements, sliding window should be your first candidate.
- When window validity can be expressed as a simple running count, two pointers usually replace any need for nested substring checks.
- The shrink condition is `while highCount > k`, not `>= k`; windows with exactly `k` high alerts are still valid.
- Be careful to decrement `highCount` only when the character leaving the window is `'H'`, or the invariant silently breaks.
- At scale, this pattern matters because it converts policy-budget checks over streams from quadratic batch work into linear, online-friendly state maintenance.

## 🚀 Variations & Further Practice
- **Longest Repeating Character Replacement**: same window-budget idea, but the budget is measured against the most frequent character in the window rather than a single forbidden symbol.
- **Max Consecutive Ones III**: binary-array version of the same pattern; useful for recognizing the abstraction independent of string framing.
- **Minimum Window Substring**: related two-pointer problem, but harder because the goal flips from maximizing a valid window to minimizing one while satisfying multi-character frequency requirements.