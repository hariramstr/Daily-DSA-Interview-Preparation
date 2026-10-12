# Maximum Consecutive Seats After Blocking One Broken Chair

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Two Pointers

---

## 🗂 Problem Overview
Given a binary array `seats`, find the length of the longest contiguous segment that can be made entirely usable if you convert at most one broken seat (`0`) into a usable seat (`1`). The output is a single integer: the maximum achievable run length. The challenge is scale: with `n` up to `200000`, brute-force testing every broken seat or every subarray is unnecessary and too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize a contiguous “healthy” region while tolerating a bounded number of failures. Examples include log ingestion pipelines allowing one malformed record in a batch, streaming analytics tolerating one dropped event in a window, packet-processing systems accepting one corrupt frame before resetting a run, and storage scanners finding the longest nearly-valid extent. At scale, the difference between rescanning windows and maintaining a rolling validity budget is material: it determines whether the solution remains linear under sustained throughput, predictable under bursty inputs, and simple enough to reason about in hot paths.

## 🔍 Problem Statement
You are given an array `seats` of length `n` where each value is either `1` (usable) or `0` (broken). You may change **at most one** `0` into `1`. Return the maximum length of a contiguous subarray containing only `1`s after applying that optional change.

Constraints:

- `1 <= n <= 200000`
- `seats[i] ∈ {0, 1}`

Examples:

```text
Input:  seats = [1,1,0,1,1,1,0,1]
Output: 6
Explanation: Flip index 2 → [1,1,1,1,1,1,0,1], longest run = 6
```

```text
Input:  seats = [0,0,1,1,0,1]
Output: 4
Explanation: Flip index 4 → [0,0,1,1,1,1], longest run = 4
```

Edge cases matter: all `1`s should return `n`; all `0`s should return `1` because one seat may be covered. The key algorithmic driver is the input size: quadratic scans are not acceptable.

## 🪜 How to Solve This
1. Read the problem → we need the longest **contiguous** region, so order matters. This immediately rules out counting total `1`s or using sorting.

2. Notice the operation budget → we are allowed **at most one bad element** inside the segment. That reframes the problem as: find the longest window containing at most one `0`.

3. Longest subarray with a bounded number of “invalid” elements is a classic **sliding window / two pointers** signal. We do not need to test each flip explicitly.

4. Maintain a window `[left, right]` and track how many broken seats (`0`s) are inside it.

5. Expand `right` one step at a time. If the window now contains more than one `0`, shrink from the left until the constraint is restored.

6. At every step, the current window is the longest valid window ending at `right`, so update the best length.

7. This works because each pointer only moves forward. No rescans, no nested loops, no auxiliary arrays. The “flip” is conceptual: any valid window with one `0` can be made all `1`s by covering that seat.

## 🧩 Algorithm Walkthrough
1. **Choose the abstraction: Sliding Window / Two Pointers.**  
   We want the longest contiguous range satisfying a local constraint: “contains at most one `0`.” That is exactly the shape sliding windows solve efficiently.

2. **Initialize state.**  
   Set `left = 0`, `zeroCount = 0`, and `best = 0`. The window is inclusive: `[left, right]`.

3. **Expand the window with `right`.**  
   For each index `right` from `0` to `n - 1`, inspect `seats[right]`. If it is `0`, increment `zeroCount`.  
   Invariant after expansion: the window reflects all elements between `left` and `right`.

4. **Repair invalid windows.**  
   If `zeroCount > 1`, the window violates the “at most one cover” budget. Move `left` rightward until `zeroCount <= 1`. Whenever `seats[left]` is `0`, decrement `zeroCount` before advancing.  
   Invariant after shrinking: the current window is valid again.

5. **Record the best valid length.**  
   Once valid, compute `right - left + 1` and update `best`. This is correct because after shrinking, `[left, right]` is the longest valid window ending at `right`; any earlier `left` would still leave more than one `0`.

6. **Return `best`.**  
   This naturally handles all edge cases:
   - all `1`s → window never shrinks, result is `n`
   - one or more `0`s → best valid span includes at most one `0`
   - all `0`s → any valid window has length `1`

The core invariant is simple and strong: **the active window always contains at most one broken seat**.

## 📊 Worked Example
Use `seats = [1,1,0,1,1,1,0,1]`.

| right | seats[right] | zeroCount | left after shrink | window `[left..right]` | length | best |
|------:|--------------:|----------:|------------------:|------------------------|-------:|-----:|
| 0 | 1 | 0 | 0 | `[0..0]` | 1 | 1 |
| 1 | 1 | 0 | 0 | `[0..1]` | 2 | 2 |
| 2 | 0 | 1 | 0 | `[0..2]` | 3 | 3 |
| 3 | 1 | 1 | 0 | `[0..3]` | 4 | 4 |
| 4 | 1 | 1 | 0 | `[0..4]` | 5 | 5 |
| 5 | 1 | 1 | 0 | `[0..5]` | 6 | 6 |
| 6 | 0 | 2 | 3 | `[3..6]` | 4 | 6 |
| 7 | 1 | 1 | 3 | `[3..7]` | 5 | 6 |

At `right = 6`, the second `0` breaks validity, so `left` advances past the first `0` at index `2`. The maximum valid window seen remains length `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed at most twice: once when `right` includes it, and once when `left` excludes it. There is no backtracking. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, linear time is still expensive but remains the only viable asymptotic shape for a single-pass scan.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a zero counter, and the best length. No auxiliary arrays or prefix structures are required. Space cannot meaningfully be reduced further; the trade-off is already optimal for a streaming-style pass.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous segment with at most `k` violations, think sliding window before considering DP or brute force.
- “Change at most one element” is often equivalent to “maintain a window with at most one bad value,” which is the key reframing here.
- Be precise about `at most one`, not `exactly one`: arrays of all `1`s must return `n`, not `n - 1`.
- The shrink condition is `while zeroCount > 1`, not `>= 1`; otherwise you incorrectly discard valid windows containing one flippable `0`.
- In production systems, bounded-error windows are a useful design pattern: enforce a local defect budget incrementally instead of recomputing global validity.

## 🚀 Variations & Further Practice
- Allow flipping at most `k` broken seats instead of one. Same sliding-window pattern, but the budget becomes configurable and the shrink condition changes to `zeroCount > k`.
- Return the indices of the best segment and which broken seat to cover. Same core algorithm, but now tie-breaking and state capture matter.
- Solve the circular version where seats wrap around. The conceptual twist is handling windows across the boundary without double-counting or violating linear-time guarantees.