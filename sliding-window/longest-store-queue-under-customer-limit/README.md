# Longest Store Queue Under Customer Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `customers`, where each value is the number of customers arriving in a minute, return the maximum length of a contiguous subarray whose sum is at most `limit`. The output is just that length. The non-trivial constraint is scale: `customers.length` can reach `100000`, so checking every possible subarray is too expensive. Because all values are non-negative, the window sum changes monotonically as pointers move, enabling a linear-time sliding window solution.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest continuous interval that stays under a cumulative budget: API rate-limit windows, burst analysis in streaming pipelines, CPU or memory budget enforcement over rolling intervals, and observability systems looking for sustained “healthy” periods. At scale, brute-force interval scans collapse under quadratic behavior and become unusable for real-time monitoring or online decisioning. The sliding-window formulation exploits monotonicity from non-negative inputs, reducing both latency and implementation complexity. That matters in production because it enables single-pass processing, bounded memory, and predictable behavior on high-volume event streams.

## 🔍 Problem Statement
You are given:

- `customers`: an integer array of length `1 <= n <= 100000`
- `limit`: an integer where `0 <= limit <= 1000000000`

Each `customers[i]` represents how many new customers joined the checkout line during minute `i`, with `0 <= customers[i] <= 10000`.

Find the maximum length of a contiguous block of minutes such that the sum of values in that block is less than or equal to `limit`. Return only the length.

Because all values are non-negative, once a window exceeds `limit`, extending it further cannot fix it; this constraint drives the algorithmic choice.

Examples:

- `customers = [2,1,3,2,1], limit = 5` → `2`
- `customers = [1,0,1,1,0,1], limit = 3` → `5`

Edge case: if every individual value is greater than `limit`, no valid minute exists, so return `0`.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** block, so this is about subarrays, not sorting or arbitrary selection.

2. Notice all values are **non-negative** → that is the key signal. If you expand a window to the right, the sum never decreases. If the sum becomes too large, the only way to recover is to move the left edge rightward.

3. That monotonic behavior suggests a **sliding window / two pointers** approach:
   - grow the window by advancing `right`
   - keep a running sum
   - while the sum exceeds `limit`, shrink from the left

4. After restoring validity, the current window is the longest valid window ending at `right`, so compare its length against the best answer seen so far.

5. Why this works better than brute force:
   - brute force checks `O(n^2)` windows
   - sliding window moves each pointer at most `n` times
   - total work becomes `O(n)`

Once you see “longest contiguous segment under threshold” plus “non-negative values,” this pattern is the default choice.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `sum = 0`, and `maxLen = 0`.  
   This represents an empty window `[left..right]` before iteration starts.

2. **Expand the window with `right`**  
   For each index `right`, add `customers[right]` to `sum`.  
   This grows the current contiguous block by one minute.

3. **Restore validity when over limit**  
   While `sum > limit`, subtract `customers[left]` from `sum` and increment `left`.  
   This is the core **Two Pointers / Sliding Window** step. It is correct because all values are non-negative: removing elements from the left is the only way to reduce the sum, and moving `right` further would only keep or increase it.

4. **Record the best valid window**  
   After the shrink loop ends, the invariant is:  
   **`sum <= limit` and the window `[left..right]` is valid.**  
   Now compute `right - left + 1` and update `maxLen` if larger.

5. **Continue until the array ends**  
   Each element enters the window once and leaves once.  
   The invariant maintained throughout is that after each iteration, the active window is the longest valid window ending at the current `right` that can be obtained without violating `sum <= limit`.

6. **Return `maxLen`**  
   If no single element fits, the window repeatedly collapses and `maxLen` remains `0`, which matches the required behavior.

This abstraction is the right fit because the problem asks for an extremal contiguous range under a cumulative constraint with non-negative values.

## 📊 Worked Example
Example: `customers = [1,0,1,1,0,1]`, `limit = 3`

| right | customers[right] | sum after add | action while sum > 3 | left | valid window     | length | maxLen |
|------:|------------------:|--------------:|----------------------|-----:|------------------|-------:|-------:|
| 0     | 1                 | 1             | none                 | 0    | `[1]`            | 1      | 1      |
| 1     | 0                 | 1             | none                 | 0    | `[1,0]`          | 2      | 2      |
| 2     | 1                 | 2             | none                 | 0    | `[1,0,1]`        | 3      | 3      |
| 3     | 1                 | 3             | none                 | 0    | `[1,0,1,1]`      | 4      | 4      |
| 4     | 0                 | 3             | none                 | 0    | `[1,0,1,1,0]`    | 5      | 5      |
| 5     | 1                 | 4             | remove `1` at left   | 1    | `[0,1,1,0,1]`    | 5      | 5      |

The longest valid contiguous block has length `5`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. The dominant work is pointer movement: `right` advances `n` times, and `left` also advances at most `n` times total across the full run. This remains practical at `10^6` elements; at `10^9`, linear time is still expensive but fundamentally better than any quadratic scan.

### Space Complexity
`O(1)`. The algorithm stores only a few scalar variables: two pointers, a running sum, and the best length. No auxiliary array or map is required. Space cannot be meaningfully reduced further without losing the running-sum state needed for single-pass processing.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous subarray** under a threshold and all values are **non-negative**, think sliding window immediately.
- “Expand until invalid, then shrink until valid” is the signature recognition pattern for two-pointer range problems with monotonic sums.
- Update the answer **after** shrinking, not before; otherwise you may record an invalid window.
- Be careful with window length calculation: for inclusive bounds, it is `right - left + 1`, not `right - left`.
- In production systems, non-negative monotonic metrics often allow single-pass budget enforcement, replacing expensive interval enumeration with predictable streaming logic.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: similar range reasoning, but the objective flips and the optimal structure may require prefix sums plus a monotonic deque.
- **Longest subarray with at most `K` distinct values**: still sliding window, but validity depends on frequency state rather than a numeric sum.
- **Arrays with negative numbers allowed**: the monotonic property breaks, so standard sliding window no longer works; prefix-sum-based techniques become necessary.