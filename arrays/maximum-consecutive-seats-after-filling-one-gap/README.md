# Maximum Consecutive Seats After Filling One Gap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Two Pointers

---

## 🗂 Problem Overview
Given a binary array `seats`, find the maximum length of a contiguous block of occupied seats after changing at most one `0` to `1`. If the array already contains only `1`s, return its full length. The challenge is not the transformation itself but computing the best possible block efficiently under large input sizes, where trying every empty seat independently would degrade to quadratic behavior.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the longest contiguous segment under a bounded number of defects, gaps, or exceptions. Examples include log ingestion pipelines tolerating one malformed record, search ranking runs with one missing feature, network telemetry detecting near-continuous uptime with one dropped sample, and storage systems scanning mostly-healthy block ranges with limited corruption. At scale, brute-force rescans per candidate gap multiply I/O and CPU cost catastrophically. A sliding-window formulation turns the problem into a single linear pass, which is the difference between an online metric and an offline batch repair job.

## 🔍 Problem Statement
You are given an array `seats` of length `1 <= n <= 200000`, where each element is either `0` or `1`. A `1` represents an occupied seat and a `0` represents an empty seat. You may change at most one `0` into a `1`, and must return the maximum possible number of consecutive `1`s anywhere in the array after that change.

If the array is already fully occupied, the answer is `n`.

Examples:

- `seats = [1,1,0,1,1,1,0,1]` → `6`
- `seats = [0,0,1,0,1,1,0]` → `4`

Edge cases matter: arrays of length `1`, arrays with no zero, arrays with all zeros, and zeros at either boundary. The key constraint is `n = 200000`, which rules out checking every zero by rescanning left and right each time.

## 🪜 How to Solve This
1. Read the problem → we are not asked to flip a specific seat; we are asked for the longest contiguous region achievable with at most one gap inside it.
2. Reframe it → any valid answer is a window containing at most one `0`. If a window has two zeros, one flip is no longer enough.
3. That immediately suggests a sliding window / two-pointer approach: expand the right edge, count zeros, and shrink the left edge whenever the window becomes invalid.
4. Why this works → every time the window contains at most one zero, it represents a contiguous segment that can be made fully occupied by flipping that zero, or left unchanged if there is no zero.
5. So the problem reduces to maintaining the longest valid window under the invariant “zero count <= 1”.
6. This avoids per-zero rescans. Each pointer only moves forward, so the whole array is processed in linear time.
7. The fully occupied case falls out naturally: if there are no zeros, the longest valid window is the entire array.

## 🧩 Algorithm Walkthrough
1. **Initialize a sliding window** with `left = 0`, `zeroCount = 0`, and `best = 0`.  
   This window represents the current candidate segment. The invariant we want is simple: the window must contain at most one zero.

2. **Scan with `right` from left to right.**  
   For each `seats[right]`, if it is `0`, increment `zeroCount`.  
   This extends the candidate segment by one element while tracking whether one flip is still sufficient.

3. **Restore validity when needed.**  
   While `zeroCount > 1`, move `left` forward. If `seats[left]` is `0`, decrement `zeroCount` before advancing.  
   This is the core Two Pointers / Sliding Window pattern: grow optimistically, then contract until the constraint is satisfied again.

4. **Update the best answer** after the window is valid.  
   The current window length is `right - left + 1`. Since it contains at most one zero, it can be converted into all `1`s with at most one change, so it is a legitimate candidate.

5. **Why it is correct.**  
   At every step, the window is the longest suffix ending at `right` that satisfies `zeroCount <= 1`. Any longer suffix would contain more than one zero and be invalid. Taking the maximum over all `right` positions therefore captures the global optimum.

6. **Why this abstraction fits.**  
   The problem is not about values individually; it is about the longest contiguous range under a bounded violation count. That is exactly what sliding windows are built for.

## 📊 Worked Example
Example: `seats = [1,1,0,1,1,1,0,1]`

| right | seats[right] | zeroCount | left after shrink | window `[left..right]` | length | best |
|------:|--------------:|----------:|------------------:|------------------------|-------:|-----:|
| 0 | 1 | 0 | 0 | `[1]` | 1 | 1 |
| 1 | 1 | 0 | 0 | `[1,1]` | 2 | 2 |
| 2 | 0 | 1 | 0 | `[1,1,0]` | 3 | 3 |
| 3 | 1 | 1 | 0 | `[1,1,0,1]` | 4 | 4 |
| 4 | 1 | 1 | 0 | `[1,1,0,1,1]` | 5 | 5 |
| 5 | 1 | 1 | 0 | `[1,1,0,1,1,1]` | 6 | 6 |
| 6 | 0 | 2 | 3 | `[1,1,1,0]` | 4 | 6 |
| 7 | 1 | 1 | 3 | `[1,1,1,0,1]` | 5 | 6 |

Best window length is `6`, achieved by flipping the zero at index `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is visited at most twice: once when `right` expands the window and once when `left` contracts it. There is no nested rescanning. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, linear time is still expensive but remains the only viable asymptotic shape for a single-pass scan.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a zero counter, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further; the trade-off is already optimal for a streaming-style pass.

## 💡 Key Takeaways
- If the prompt asks for the longest contiguous segment after allowing a small number of “bad” elements, think sliding window with a bounded violation counter.
- When the operation is “change at most one value,” translate that into a window constraint like “contains at most one zero.”
- Be careful to shrink while `zeroCount > 1`, not `>= 1`; one zero is still valid because it is the seat you may fill.
- Update the answer only after restoring the window invariant, otherwise you may record an invalid segment containing two zeros.
- The transferable design insight is to model limited repairs, faults, or exceptions as window-validity constraints, enabling single-pass online computation instead of repeated local recomputation.

## 🚀 Variations & Further Practice
- Allow flipping **up to `k` zeros** instead of one. Same sliding-window pattern, but the invariant becomes `zeroCount <= k`, which tests whether the abstraction generalizes cleanly.
- Return the **indices of the seat to fill** and the resulting segment boundaries. Same core scan, but now you must preserve enough state to reconstruct the optimal decision.
- Extend from binary occupancy to **weighted penalties per gap**, where a window is valid if total repair cost stays under budget. The conceptual twist is replacing a simple count with an accumulated constraint.