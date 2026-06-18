# Longest Upgrade Window With One Rollback

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Two Pointers

---

## 🗂 Problem Overview
Given a binary array `status`, find the maximum length of a contiguous subarray that can be made entirely `1`s by changing at most one `0` to `1`. Return only that maximum length. The challenge is not correctness on small inputs, but doing it efficiently under `status.length <= 100000`. Exhaustively checking all subarrays is quadratic and does not scale, so the solution must exploit structure in contiguous windows.

## 🌍 Engineering Impact
This pattern shows up anywhere you need the longest healthy run while tolerating a bounded number of faults: streaming pipeline uptime windows, flaky test streak analysis in CI, packet-loss smoothing in network telemetry, or search/indexing jobs with one retry budget. At production scale, brute-force segment evaluation collapses under high-cardinality event streams and long retention windows. A sliding-window formulation turns the problem into online state maintenance: constant extra memory, linear scan, and predictable latency. That matters in systems that process millions of events per minute, where per-window rescans translate directly into CPU waste and tail-latency spikes.

## 🔍 Problem Statement
You are given an integer array `status` of length `n`, where each element is either `1` or `0`. A `1` means the upgrade at that minute succeeded; a `0` means it failed. You may perform at most one rollback operation, which converts a single `0` into a `1`.

Your task is to return the length of the longest **contiguous** subarray that can contain only `1`s after applying at most one rollback.

Constraints:

- `1 <= status.length <= 100000`
- `status[i]` is either `0` or `1`

Examples:

- `status = [1,1,0,1,1,1,0,1]` → `6`
- `status = [0,1,1,0,1,0,1,1]` → `4`

Edge cases matter:

- If the array already contains all `1`s, the full length is valid.
- If there are many `0`s, only one can be absorbed into the chosen window.
- The contiguity requirement rules out counting scattered successes.

The input bound eliminates any `O(n^2)` subarray enumeration approach.

## 🪜 How to Solve This
1. Read the problem → the phrase “longest contiguous window” should immediately suggest a windowing technique, not DP or brute force.

2. Notice the tolerance rule → the window is valid as long as it contains **at most one `0`**. That is the real condition to maintain.

3. Once validity is expressed as “count of bad items inside a moving range,” the natural abstraction is a sliding window with two pointers.

4. Expand the right pointer one step at a time → include each new element in the current candidate window.

5. If the window now contains more than one `0`, it violates the rollback budget → move the left pointer rightward until the window becomes valid again.

6. At every step, the current window is the longest valid window ending at the current right boundary. Record its length.

7. Because each pointer only moves forward, the whole scan is linear. No rescanning, no nested loops, no auxiliary arrays.

This is the key mental move: stop thinking about “which `0` should I flip?” and instead maintain “what is the largest range containing at most one `0`?”

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` over `status`. This pattern fits because we need the longest contiguous segment satisfying a local constraint: at most one failure inside the segment.

2. **Track the number of failures in the current window.**  
   Keep an integer `zeroCount`. When `status[right] == 0`, increment it. This is the only state needed to determine whether the window is still feasible under one rollback.

3. **Expand the window greedily.**  
   Move `right` from `0` to `n - 1`. After adding each element, the window represents the largest candidate ending at `right` before any repair for invalidity.

4. **Restore validity when the rollback budget is exceeded.**  
   If `zeroCount > 1`, move `left` forward until `zeroCount <= 1` again. Whenever `status[left] == 0`, decrement `zeroCount` before advancing.  
   **Invariant:** after this shrinking phase, `[left, right]` is always a valid window containing at most one `0`.

5. **Update the answer from the valid window size.**  
   Compute `right - left + 1` and maximize the result. This is correct because, for a fixed `right`, any valid window starting left of `left` would contain more than one `0` and therefore be invalid.

6. **Why this is optimal.**  
   Every element enters the window once and leaves once. The algorithm never misses a better candidate because it evaluates the maximal valid window for every right boundary. That is exactly what the objective asks for.

## 📊 Worked Example
Take `status = [1,1,0,1,1,1,0,1]`.

| right | status[right] | left | zeroCount | window `[left..right]` | length | best |
|------:|---------------:|-----:|----------:|------------------------|-------:|-----:|
| 0 | 1 | 0 | 0 | `[1]` | 1 | 1 |
| 1 | 1 | 0 | 0 | `[1,1]` | 2 | 2 |
| 2 | 0 | 0 | 1 | `[1,1,0]` | 3 | 3 |
| 3 | 1 | 0 | 1 | `[1,1,0,1]` | 4 | 4 |
| 4 | 1 | 0 | 1 | `[1,1,0,1,1]` | 5 | 5 |
| 5 | 1 | 0 | 1 | `[1,1,0,1,1,1]` | 6 | 6 |
| 6 | 0 | 3 | 1 | shrink past first `0` | 4 | 6 |
| 7 | 1 | 3 | 1 | `[1,1,1,0,1]` | 5 | 6 |

At `right = 6`, the second `0` makes the window invalid, so `left` advances until only one `0` remains. The maximum valid length observed is `6`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each element is processed at most twice: once when `right` includes it and once when `left` excludes it. There is no nested rescanning. At `10^6` elements this remains practical in a single pass; at `10^9`, linear time is still expensive but fundamentally better than any quadratic alternative.

### Space Complexity
`O(1)`. The algorithm stores only a few integers: `left`, `right`, `zeroCount`, and `best`. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced below constant without losing the ability to maintain window validity online.

## 💡 Key Takeaways
- If a problem asks for the longest contiguous segment under a bounded number of “bad” elements, think sliding window immediately.
- “At most one modification” often translates into “window may contain at most one violating item,” which is the signal for two pointers.
- Be careful to shrink while `zeroCount > 1`, not `>= 1`; one `0` is still valid because it can be rolled back.
- The window length is `right - left + 1`; this off-by-one is the most common implementation bug in pointer-based scans.
- In production systems, bounded-error windows are best handled as streaming invariants, not repeated recomputation over historical ranges.

## 🚀 Variations & Further Practice
- Allow up to `k` rollbacks instead of one. Same sliding-window pattern, but the invariant becomes “at most `k` zeros,” which generalizes the budgeted-error model.
- Require deleting exactly one element rather than flipping one `0`. The conceptual twist is that a valid all-ones run may need to exclude one position even if it is already `1`.
- Move from binary values to weighted failures, where each failure has a rollback cost and the window must stay under a total budget. The harder part is maintaining a sum constraint rather than a simple count.