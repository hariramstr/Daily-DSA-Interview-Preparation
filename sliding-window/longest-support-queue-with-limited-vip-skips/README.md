# Longest Support Queue With Limited VIP Skips

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given a binary array `tickets` and an integer `k`, find the maximum length of any contiguous segment containing at most `k` VIP tickets (`1`s). Regular tickets (`0`s) are unrestricted; only the count of VIPs inside the window matters. The challenge is scale: with up to `100000` elements, enumerating all subarrays is too slow, so the solution must exploit structure in contiguous ranges and maintain validity incrementally.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest continuous operating interval under a bounded exception budget: log streams with limited error tolerance, network traces with capped packet loss, ad-serving sessions with a maximum number of premium reroutes, or streaming pipelines that can absorb only a fixed number of malformed records before rebalancing. At production scale, brute-force range evaluation collapses under quadratic growth and destroys latency budgets. Sliding-window reasoning turns a global search over all intervals into a single pass with local state, enabling online processing, predictable memory use, and straightforward integration into stream processors or observability backends.

## 🔍 Problem Statement
You are given an array `tickets` of length `n`, where each element is either `0` or `1`:

- `0` = regular ticket
- `1` = VIP ticket

You must return the length of the longest contiguous subarray containing at most `k` VIP tickets. In other words, among all continuous queue segments, find the largest one whose count of `1`s does not exceed `k`.

Constraints:

- `1 <= tickets.length <= 100000`
- `tickets[i]` is either `0` or `1`
- `0 <= k <= tickets.length`

Examples:

- `tickets = [0,1,0,0,1,0,0,0], k = 1` → `5`
- `tickets = [1,0,1,0,0,1,0], k = 2` → `6`

Edge cases matter:

- `k = 0` means the answer is the longest all-`0` segment.
- If `k` is at least the total number of VIP tickets, the entire array is valid.

The key constraint is `n = 100000`, which rules out checking all subarrays in `O(n^2)` time.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous** segment, so order matters and sorting is irrelevant.

2. Notice the validity rule depends only on a window-level aggregate: **how many `1`s are inside the current segment**. That is a strong signal for a sliding window.

3. Start with a window `[left, right]` and expand `right` one step at a time. Every new element either keeps the window valid or increases the VIP count.

4. If the window has more than `k` VIP tickets, it cannot be accepted as-is. But discarding the whole window would waste work. Instead, move `left` forward just enough to restore validity.

5. This suggests a classic invariant: maintain the largest possible window ending at `right` such that VIP count `<= k`.

6. Once that invariant holds, the current window length is a candidate answer. Track the maximum over the scan.

7. Why this works: both pointers only move forward, so every element enters and leaves the window at most once. That gives linear time instead of quadratic enumeration.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use the **Sliding Window / Two Pointers** pattern with:
   - `left = 0`
   - `vipCount = 0`
   - `maxLen = 0`  
   The abstraction fits because we are optimizing over contiguous ranges under a monotonic validity constraint: adding elements can violate the rule, and removing from the left can restore it.

2. **Expand the window with `right`**  
   Iterate `right` from `0` to `n - 1`.  
   If `tickets[right] == 1`, increment `vipCount`.  
   This updates the state to reflect the full window `[left, right]`.

3. **Restore validity when needed**  
   While `vipCount > k`, move `left` forward.  
   If `tickets[left] == 1`, decrement `vipCount` before incrementing `left`.  
   This loop maintains the invariant: after it finishes, the current window contains at most `k` VIP tickets.

4. **Record the best valid window**  
   Once valid, compute `right - left + 1` and update `maxLen`.  
   This is correct because `[left, right]` is the longest valid window ending at `right`; any earlier `left` would still violate the constraint.

5. **Finish after one pass**  
   Continue until `right` reaches the end. Return `maxLen`.  
   Correctness follows from examining every possible right boundary and always keeping the maximal valid left boundary for that right.

## 📊 Worked Example
Example: `tickets = [1,0,1,0,0,1,0], k = 2`

| right | tickets[right] | vipCount after add | left after shrink | valid window     | length | maxLen |
|------:|----------------|-------------------:|------------------:|------------------|-------:|-------:|
| 0     | 1              | 1                  | 0                 | `[1]`            | 1      | 1      |
| 1     | 0              | 1                  | 0                 | `[1,0]`          | 2      | 2      |
| 2     | 1              | 2                  | 0                 | `[1,0,1]`        | 3      | 3      |
| 3     | 0              | 2                  | 0                 | `[1,0,1,0]`      | 4      | 4      |
| 4     | 0              | 2                  | 0                 | `[1,0,1,0,0]`    | 5      | 5      |
| 5     | 1              | 3                  | 1                 | `[0,1,0,0,1]`    | 5      | 5      |
| 6     | 0              | 2                  | 1                 | `[0,1,0,0,1,0]`  | 6      | 6      |

At `right = 5`, the window becomes invalid with 3 VIP tickets, so `left` advances until one VIP is removed. The final answer is `6`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each element is processed at most twice: once when `right` includes it, and at most once when `left` excludes it. There is no nested reprocessing of subarrays. At `10^6` elements this is routine; at `10^9`, linear work is still expensive but remains the only viable asymptotic class for a single-machine scan.

### Space Complexity
`O(1)`. The algorithm stores only a few integers: pointer positions, current VIP count, and best length. No auxiliary array or map is required. Space cannot meaningfully be reduced further without losing the ability to track window state.

## 💡 Key Takeaways
- If the problem asks for a longest or shortest **contiguous** segment under a bounded-count constraint, think sliding window before considering anything heavier.
- When validity depends on a window aggregate that can be updated incrementally, Two Pointers usually converts quadratic range search into a linear scan.
- The shrink condition must be `while vipCount > k`, not `if`, because one left shift may not restore validity.
- Window length is `right - left + 1`; missing the `+1` is the most common off-by-one bug in this pattern.
- In production systems, bounded-exception windows are often better modeled as streaming invariants than as batch analytics problems; that shift is what makes online, low-latency evaluation possible.

## 🚀 Variations & Further Practice
- Find the longest subarray with at most `k` zeros after flipping zeros to ones: same pattern, but the semantic interpretation changes from tolerated exceptions to transform budget.
- Count how many subarrays contain at most `k` VIP tickets: same window invariant, but instead of maximizing length, accumulate `right - left + 1` at each step.
- Longest substring with at most `k` distinct characters: same sliding-window core, but validity depends on a frequency map rather than a single counter, which increases state-management complexity.