# Longest Delivery Streak With Limited Cold Packages

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, String

---

## 🗂 Problem Overview
Given a string `deliveries` containing only `'N'` and `'C'`, return the length of the longest contiguous substring with at most `k` occurrences of `'C'`. The substring must preserve original order and be one continuous block. The challenge is scale: with input length up to `200000`, enumerating all substrings is too expensive, so the solution must maintain validity incrementally while scanning once.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest valid contiguous segment under a bounded “bad event” budget: streaming pipelines tolerating limited malformed records, observability systems finding the longest interval with at most `k` error spikes, ad-serving or ranking pipelines enforcing bounded expensive candidates, and warehouse or logistics schedulers limiting special-handling items per batch. At scale, brute-force window evaluation collapses under quadratic cost and destroys latency budgets. Sliding-window reasoning enables single-pass admission control, online processing, and predictable memory usage, which matters in hot paths, stream processors, and large-volume operational planning systems.

## 🔍 Problem Statement
You are given:

- a string `deliveries` of length `n`, where each character is either `'N'` or `'C'`
- an integer `k`

Return the length of the longest contiguous substring containing at most `k` occurrences of `'C'`.

A valid block must remain contiguous and preserve the original order. You cannot reorder deliveries or pick multiple disjoint segments.

**Constraints**
- `1 <= deliveries.length <= 200000`
- `deliveries[i]` is either `'N'` or `'C'`
- `0 <= k <= deliveries.length`

**Examples**

```text
Input: deliveries = "NCCNNNCN", k = 2
Output: 6
Explanation: "NCCNNN" and "CNNNCN" are valid length-6 windows.
```

```text
Input: deliveries = "CCCCNNC", k = 1
Output: 3
Explanation: A longest valid window is "CNN".
```

The key constraint is `n` up to `200000`: any approach that checks every substring is too slow, which forces a linear-time strategy.

## 🪜 How to Solve This
1. Read the problem → the word **contiguous** is the signal. We are not choosing arbitrary deliveries; we are choosing one continuous block.
2. Notice the validity rule depends only on a running count: a window is valid if it contains at most `k` cold packages. That is exactly the kind of condition that can be updated incrementally.
3. If we extend a candidate block one character at a time, only one thing can break validity: the count of `'C'` becomes `k + 1`.
4. When that happens, there is no reason to restart from scratch. We can shrink the left side until the window becomes valid again.
5. This suggests the **sliding window / two pointers** pattern: one pointer expands the window, the other pointer repairs it.
6. While scanning, every valid window gives a candidate answer, so we track the maximum width seen.
7. The reason this works efficiently is that each character enters the window once and leaves once. No nested rescanning, no substring generation, no auxiliary indexing required.

Once you see “longest contiguous segment with at most `k` violations,” sliding window is the default mental model.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and counters.**  
   Set `left = 0`, `coldCount = 0`, and `best = 0`. The window is `deliveries[left...right]`.  
   **Invariant:** before updating `best`, the window is valid or will be repaired immediately.

2. **Expand the window with `right`.**  
   Iterate `right` from `0` to `n - 1`. If `deliveries[right] == 'C'`, increment `coldCount`.  
   This is the “grow” phase of the **Two Pointers / Sliding Window** pattern.

3. **Repair invalid windows by moving `left`.**  
   While `coldCount > k`, move `left` rightward. If `deliveries[left] == 'C'`, decrement `coldCount` before advancing.  
   **Why correct:** any window containing more than `k` cold packages is invalid, and the only way to restore validity while keeping contiguity is to discard items from the left.

4. **Record the best valid width.**  
   After the repair loop, the window satisfies `coldCount <= k`. Update  
   `best = max(best, right - left + 1)`.  
   **Invariant:** `best` is always the maximum length of any valid window seen so far.

5. **Finish in one pass.**  
   Each index is visited by `right` once and by `left` at most once. That gives linear time.  
   This abstraction is the right fit because the validity predicate is monotonic under left-shrinking: once a window has too many `'C'`, removing elements from the left eventually restores validity without missing any better candidate ending at `right`.

## 📊 Worked Example
Example: `deliveries = "NCCNNNCN"`, `k = 2`

| right | char | coldCount after add | left after shrink | valid window | length | best |
|------:|:----:|--------------------:|------------------:|:------------|-------:|-----:|
| 0 | N | 0 | 0 | `N` | 1 | 1 |
| 1 | C | 1 | 0 | `NC` | 2 | 2 |
| 2 | C | 2 | 0 | `NCC` | 3 | 3 |
| 3 | N | 2 | 0 | `NCCN` | 4 | 4 |
| 4 | N | 2 | 0 | `NCCNN` | 5 | 5 |
| 5 | N | 2 | 0 | `NCCNNN` | 6 | 6 |
| 6 | C | 3 | 2 | `CNNNC` | 5 | 6 |
| 7 | N | 2 | 2 | `CNNNCN` | 6 | 6 |

At `right = 6`, the window becomes invalid with three `'C'` values. Shrinking from the left removes `N` first, then `C`, restoring validity. The maximum valid length remains `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = deliveries.length`. The dominant work is the single left-to-right scan, plus left-pointer movement across the string at most once. At `10^6` elements this is still operationally cheap; at `10^9`, linear work is large but still the only viable asymptotic class for a single-machine pass.

### Space Complexity
`O(1)`. The algorithm stores only a few integers: two pointers, the current cold-package count, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous segment** satisfying “at most `k` bad items,” think sliding window before considering anything more complex.
- When validity depends on a count that can be updated incrementally, two pointers usually beat prefix-heavy or brute-force formulations.
- Update the answer only **after** shrinking invalid windows; recording too early produces lengths for illegal substrings.
- Be careful with `right - left + 1`; this is the most common off-by-one error when converting pointer positions into window length.
- The production-grade insight: bounded-violation windows are often best handled as online admission-control problems, not as offline enumeration problems.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters** — same window pattern, but validity depends on a frequency map rather than one scalar counter.
- **Max consecutive ones III / flip at most `k` zeros** — structurally identical problem, useful for recognizing the binary-window template quickly.
- **Minimum window substring** — same two-pointer machinery, but the objective flips from maximizing a valid window to minimizing one that satisfies a coverage constraint.