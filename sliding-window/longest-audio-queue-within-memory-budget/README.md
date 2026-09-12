# Longest Audio Queue Within Memory Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `memory`, find the maximum length of a contiguous subarray whose sum is at most `budget`. The clips must remain in original order, so reordering is not allowed. Return only the length, not the subarray itself. The challenge is scale: with up to 200,000 clips, enumerating all subarrays is too slow, so the solution must maintain validity incrementally as the range moves.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the largest contiguous workload that fits under a hard resource cap: media buffering, TCP receive windows, stream processor batch sizing, log ingestion chunks, and memory-bounded prefetch in storage engines. At scale, brute-force range evaluation turns linear data scans into quadratic latency spikes, which is unacceptable in hot paths. A sliding window converts repeated recomputation into incremental accounting. That enables predictable throughput, lower tail latency, and simpler admission-control logic when workloads arrive in order and only contiguous segments are valid operational units.

## 🔍 Problem Statement
You are given:

- `memory`: an array where `memory[i]` is the RAM required by the `i`-th audio clip
- `budget`: the maximum total RAM available for buffering

Return the length of the longest contiguous block of clips whose total memory usage is `<= budget`.

Constraints:

- `1 <= memory.length <= 200000`
- `1 <= memory[i] <= 1000000000`
- `1 <= budget <= 100000000000000`
- Result fits in a 32-bit signed integer

Examples:

- `memory = [4, 2, 1, 7, 3, 2], budget = 8` → `3`
- `memory = [5, 1, 1, 1, 5], budget = 7` → `3`

Key edge conditions:

- Every value is positive, which is what makes the sliding-window approach valid.
- A single clip may exceed `budget`; then any valid window must exclude it.
- The input size rules out `O(n^2)` subarray checks.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous** range under a **sum constraint**. That combination should immediately suggest a sliding window.

2. Check the array properties → all `memory[i]` values are positive. That matters because when the window sum gets too large, moving the left boundary rightward can only decrease the sum. Without positivity, this logic breaks.

3. Start expanding from left to right → treat each new clip as a candidate to include. Keep a running sum for the current window.

4. If the sum exceeds `budget`, the current window is invalid → shrink from the left until the sum is valid again. There is no reason to discard anything from the right, because we are trying to maximize length while preserving contiguity.

5. After restoring validity, record the current window length. Since every index enters and leaves the window at most once, the process is linear.

6. The mental model is simple: maintain the largest valid suffix ending at each position, and keep the best length seen globally.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and a running sum.**  
   Let `left = 0`, `sum = 0`, and `best = 0`. The window is the inclusive range `[left, right]`. The invariant is: after adjustment, this window is always valid, meaning `sum <= budget`.

2. **Expand the window with `right`.**  
   Iterate `right` from `0` to `n - 1`. Add `memory[right]` to `sum`. This represents trying to include the next clip while preserving order.

3. **Repair invalid windows by advancing `left`.**  
   While `sum > budget`, subtract `memory[left]` from `sum` and increment `left`. This is the core **Sliding Window / Two Pointers** step. It is correct because all values are positive: removing elements from the left is the only monotonic way to reduce the sum while keeping the window contiguous and ending at `right`.

4. **Update the best answer once valid.**  
   After the shrink loop, `[left, right]` is the longest valid window ending at `right`, because any earlier `left` would make the sum exceed `budget`. Compute `right - left + 1` and update `best`.

5. **Repeat until the scan completes.**  
   Each element is added once when `right` advances and removed at most once when `left` advances. That gives linear time.

6. **Why this abstraction fits.**  
   This is a canonical variable-size sliding window: optimize window length subject to a monotonic validity condition (`sum <= budget`). The positivity constraint turns local adjustments into globally correct progress.

## 📊 Worked Example
Use `memory = [4, 2, 1, 7, 3, 2]`, `budget = 8`.

| Step | right | Added | sum before shrink | left after shrink | valid window | length | best |
|---|---:|---:|---:|---:|---|---:|---:|
| 1 | 0 | 4 | 4 | 0 | `[4]` | 1 | 1 |
| 2 | 1 | 2 | 6 | 0 | `[4,2]` | 2 | 2 |
| 3 | 2 | 1 | 7 | 0 | `[4,2,1]` | 3 | 3 |
| 4 | 3 | 7 | 14 | 3 | `[7]` | 1 | 3 |
| 5 | 4 | 3 | 10 | 4 | `[3]` | 1 | 3 |
| 6 | 5 | 2 | 5 | 4 | `[3,2]` | 2 | 3 |

At `right = 3`, adding `7` breaks the budget, so the window shrinks repeatedly: remove `4`, then `2`, then `1`, leaving only `[7]`. The maximum valid length observed anywhere in the scan is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each clip is processed at most twice: once when the right pointer includes it, and once when the left pointer excludes it. There is no nested re-scan of the array. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally the best possible for exact single-pass evaluation.

### Space Complexity
`O(1)`. The algorithm stores only scalar state: two indices, a running sum, and the best length. No auxiliary array or heap is required. Space cannot be meaningfully reduced further without losing the ability to track the active window sum.

## 💡 Key Takeaways
- If the problem asks for a **longest/shortest contiguous subarray** under a threshold, check whether a sliding window applies before considering prefix-sum or DP approaches.
- Positive-only values are the strongest signal that a variable-size two-pointer window will be both correct and linear.
- Use a 64-bit accumulator for the running sum; `memory[i]` and `budget` exceed 32-bit range in aggregate.
- Update the answer only **after** shrinking to restore `sum <= budget`; doing it earlier records invalid windows.
- In production systems, this pattern is incremental resource admission: maintain a maximal valid frontier instead of recomputing feasibility from scratch.

## 🚀 Variations & Further Practice
- **Longest subarray with sum exactly equal to `k`**: harder because the validity condition is no longer monotonic; prefix sums plus hashing replace the sliding window.
- **Shortest subarray with sum at least `k`**: the optimization direction flips, and with arbitrary integers the right tool becomes prefix sums plus a monotonic deque.
- **Maximum average or weighted contiguous block under multiple constraints**: introduces competing budgets or non-additive scoring, often requiring binary search on the answer or more advanced window bookkeeping.