# Maximum Consecutive Days After One Schedule Swap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Prefix Sum

---

## 🗂 Problem Overview
Given a binary array `days`, return the longest consecutive run of `1`s obtainable after at most one swap between any `1` and any `0`. The swap preserves the total number of working days, so the challenge is local grouping under a global capacity constraint: you can merge or extend a window, but never create more `1`s than already exist. The target solution must handle arrays up to `200000` elements in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system tries to maximize contiguous useful capacity under a fixed global budget: CPU scheduling windows, ad-serving availability slots, warehouse staffing calendars, media buffering, and streaming pipeline backpressure smoothing. The key issue is not just finding a dense local segment, but doing so while respecting a conserved global resource. At scale, naive recomputation across all candidate swaps becomes quadratic and collapses under large timelines or event streams. A linear sliding-window formulation enables online reasoning, predictable latency, and efficient integration into schedulers, optimizers, and planning services.

## 🔍 Problem Statement
You are given a binary array `days` where `days[i] = 1` means an employee works on day `i`, and `days[i] = 0` means they are off. You may perform at most one swap between any position containing `1` and any position containing `0`. After that optional swap, return the maximum possible length of a contiguous block of `1`s.

Important edge cases:
- If the array contains no `0`, the answer is `days.length`.
- If the array contains no `1`, the answer is `0`.
- Because only one swap is allowed, the resulting block can contain at most all existing `1`s.

Constraints:
- `1 <= days.length <= 200000`
- `days[i] ∈ {0, 1}`

Examples:
- `days = [1,1,0,1,1,1,0,1]` → `6`
- `days = [1,0,1,0,1]` → `3`

The array size rules out trying every swap; the algorithm must be `O(n)`.

## 🪜 How to Solve This
1. Read the problem → the swap can move one `1` anywhere, so what matters is not exact swap simulation but whether a window can be made all `1`s with at most one internal `0`.

2. Reframe it → after one swap, any valid target segment is effectively a subarray containing at most one `0`. That `0` can be replaced by some `1` from outside the window.

3. Notice the global constraint → you cannot fill a window longer than the total number of `1`s in the entire array. This is the subtle part many implementations miss.

4. That suggests a sliding window → maintain the longest window with at most one `0`.

5. For each such window, the achievable consecutive `1`s is `min(window_length, total_ones)`.  
   Why? If the window already has all `1`s, great. If it has one `0`, you need an extra `1` from outside; if none exists, you cannot fully realize the window.

6. So the problem becomes: count total `1`s once, scan once with two pointers, keep at most one `0` in the window, and maximize the capped window length.

## 🧩 Algorithm Walkthrough
1. **Count total working days (`totalOnes`)**  
   This is the global budget of `1`s available anywhere in the array. No answer can exceed it. This invariant prevents overestimating windows like `[1,1,1]` when there is no spare `1` to swap in.

2. **Use the Sliding Window / Two Pointers pattern**  
   Maintain a window `[left, right]` such that it contains at most one `0`. This is the right abstraction because one swap can repair at most one bad position inside a candidate contiguous block.

3. **Expand `right` one step at a time**  
   When `days[right] == 0`, increment `zeroCount`. The window now represents a candidate segment that is either already all `1`s or can be fixed by one swap.

4. **Shrink from the left while `zeroCount > 1`**  
   If the window contains two or more `0`s, one swap is insufficient to make it fully consecutive. Move `left` forward until the invariant “window has at most one zero” is restored.

5. **Evaluate the current window**  
   Let `windowLen = right - left + 1`. The realizable consecutive block is `min(windowLen, totalOnes)`.  
   - If the window has one `0` and there is at least one `1` outside, the swap can fill it.  
   - If the window has no `0`, you may still be limited by `totalOnes`, though in practice this cap mainly matters when considering windows larger than the number of available `1`s.

6. **Track the maximum over all windows**  
   Every valid answer corresponds to some window with at most one `0`, and the scan visits all such windows in linear time. The invariant is: after each iteration, `[left, right]` is the longest suffix ending at `right` that needs at most one swap.

## 📊 Worked Example
Example: `days = [1,1,0,1,1,1,0,1]`  
`totalOnes = 6`

| right | days[right] | left | zeroCount | window `[left..right]` | windowLen | candidate = `min(windowLen, 6)` |
|------:|-------------:|-----:|----------:|------------------------|----------:|--------------------------------:|
| 0 | 1 | 0 | 0 | `[1]` | 1 | 1 |
| 1 | 1 | 0 | 0 | `[1,1]` | 2 | 2 |
| 2 | 0 | 0 | 1 | `[1,1,0]` | 3 | 3 |
| 3 | 1 | 0 | 1 | `[1,1,0,1]` | 4 | 4 |
| 4 | 1 | 0 | 1 | `[1,1,0,1,1]` | 5 | 5 |
| 5 | 1 | 0 | 1 | `[1,1,0,1,1,1]` | 6 | 6 |
| 6 | 0 | shrink | 2→1 | valid window restored | 4 | 4 |
| 7 | 1 | 3 | 1 | `[1,1,1,0,1]` | 5 | 5 |

Best answer is `6`, achieved by filling the zero at index `2` using the `1` at index `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass counts total `1`s, and one sliding-window pass moves each pointer at most `n` times. There is no nested rescanning. At `10^6` elements this is routine; at `10^9`, the algorithm is still asymptotically right but would require streaming/distributed handling due to memory and wall-clock constraints.

### Space Complexity
`O(1)`. The algorithm stores a few counters and pointer indices; no auxiliary arrays are required. Space cannot be meaningfully reduced below constant, though a prefix-sum variant would increase memory to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If a binary-array problem allows fixing or replacing at most one bad element, think sliding window with a bounded count of violations.
- If the operation preserves the total number of `1`s, always track the global resource count; local window logic alone is insufficient.
- Do not return raw window length blindly: cap by `totalOnes`, or you may claim a block longer than the number of available working days.
- Be careful when shrinking the window after the second `0`; update `zeroCount` exactly when `left` crosses a zero, or the invariant breaks.
- The transferable design insight is to separate **local feasibility** (window has at most one defect) from **global capacity** (total available `1`s), a common pattern in schedulers and resource allocators.

## 🚀 Variations & Further Practice
- Allow up to `k` swaps instead of one. The conceptual twist is that the window may contain up to `k` zeros, but the result is still capped by the total number of `1`s.
- Make swaps adjacent only. Now the cost depends on distance, turning a simple feasibility window into a movement-cost optimization problem.
- Support online updates where days flip between `0` and `1`, and queries ask for the current best answer. The harder part is maintaining segment structure and global counts under mutation.