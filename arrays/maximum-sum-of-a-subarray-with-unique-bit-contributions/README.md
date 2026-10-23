# Maximum Sum of a Subarray With Unique Bit Contributions

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Bit Manipulation

---

## 🗂 Problem Overview
Given an array of non-negative integers, find the maximum sum over all contiguous subarrays where no bit position is used by more than one element. Equivalently, every pair of elements inside the chosen window must satisfy `a & b == 0`. The output is that maximum sum. The challenge is that `n` can reach `2 * 10^5`, so checking all subarrays or all element pairs inside each window is computationally infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a rolling selection must satisfy non-overlapping resource claims: feature-flag masks in serving paths, CPU capability masks in schedulers, compiler register allocation windows, search/ranking pipelines enforcing mutually exclusive signals, and streaming systems deduplicating bit-encoded attributes. At scale, brute-force validation of every candidate range collapses under quadratic behavior and repeated recomputation. The useful abstraction is incremental constraint maintenance: keep a moving window valid as data arrives, update state in constant time per shift, and preserve enough structure to answer optimization questions online rather than through expensive rescans.

## 🔍 Problem Statement
You are given `nums`, an array of length `n` where `1 <= n <= 2 * 10^5` and `0 <= nums[i] <= 10^9`. A contiguous subarray is **clean** if for every distinct pair of indices `i, j` inside that subarray, `nums[i] & nums[j] == 0`. Return the maximum possible sum of values among all clean contiguous subarrays. The result fits in a signed 64-bit integer.

This is stricter than “all values distinct.” Two different numbers may still conflict if they share any set bit, and duplicate nonzero values can never coexist in a clean subarray.

Examples:

- `nums = [5,2,8,1]` → `11`  
  Best clean subarray: `[2,8,1]`

- `nums = [3,8,48,10]` → `59`  
  Best clean subarray: `[3,8,48]`

The key constraint is `n = 2 * 10^5`: the algorithm must avoid enumerating all subarrays.

## 🪜 How to Solve This
1. Start from the condition, not the sum: a valid subarray requires that no bit appears twice. That immediately suggests maintaining aggregate bit usage for a window.

2. Because the subarray must be contiguous, sorting is illegal. This is a sliding-window problem, not a set-selection problem.

3. Ask what state is sufficient to test whether a new value can enter the current window. If the window is already clean, then each set bit appears exactly once, so a single bitmask representing the OR of the window is enough.

4. When extending right with `nums[r]`, check `windowMask & nums[r]`.  
   → zero means no conflict, so the window stays clean.  
   → nonzero means some bit is duplicated, so shrink from the left until the conflict disappears.

5. While shrinking, remove `nums[l]` from both the running sum and the bitmask. Because the window invariant guarantees unique bit ownership, removal can be done with XOR.

6. Track the running sum of the current clean window and update the maximum after each successful extension. This yields a linear pass with constant-time state updates.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` that is always clean. This is the right abstraction because the constraint is local to a contiguous range and can be repaired incrementally by advancing `left`.

2. **Track two pieces of state:**  
   `windowMask` = bitwise OR of all values currently in the window,  
   `windowSum` = sum of all values currently in the window.  
   Correctness relies on the invariant that the window is clean, so each set bit in `windowMask` belongs to exactly one element.

3. **Try to add `nums[right]`.**  
   If `(windowMask & nums[right]) == 0`, the new value shares no bit with the current window. Add it directly:  
   `windowMask |= nums[right]`, `windowSum += nums[right]`.

4. **If there is a conflict, shrink from the left.**  
   While `(windowMask & nums[right]) != 0`, remove `nums[left]`:  
   `windowMask ^= nums[left]`, `windowSum -= nums[left]`, `left++`.  
   XOR removal is correct only because no bit is duplicated inside a valid window.

5. **After conflicts are cleared, add `nums[right]`.**  
   The window is clean again, and the invariant is restored.

6. **Update the answer after each right expansion.**  
   `best = max(best, windowSum)` captures the maximum sum over all clean windows encountered.

7. **Why this is linear:**  
   Each element enters the window once and leaves once. No pointer moves backward, so the total work is `O(n)`.

## 📊 Worked Example
Example: `nums = [5,2,8,1]`

| right | nums[right] | action | left | windowMask (bin) | windowSum | best |
|---|---:|---|---:|---|---:|---:|
| 0 | 5 | add | 0 | `0101` | 5 | 5 |
| 1 | 2 | no conflict, add | 0 | `0111` | 7 | 7 |
| 2 | 8 | no conflict, add | 0 | `1111` | 15 | 15 |
| 3 | 1 | conflict with mask (`1111 & 0001`) | 0 | `1111` | 15 | 15 |
| 3 | 1 | remove `5` | 1 | `1010` | 10 | 15 |
| 3 | 1 | add `1` | 1 | `1011` | 11 | 15 |

The full prefix `[5,2,8]` is actually clean, so its sum is `15`. Adding `1` conflicts with `5` because both use bit 0. After removing `5`, the window `[2,8,1]` becomes clean with sum `11`. The maximum remains `15`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed when the right pointer includes it and at most once more when the left pointer removes it. The dominant work is the constant-time bitwise conflict check and update per pointer movement. This scales comfortably to `10^6` elements; at `10^9`, even linear scans become throughput-bound and require partitioned or streaming execution.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a running sum, the current bitmask, and the best answer. Space cannot be meaningfully reduced further without losing the incremental validity check; expanding to per-bit counters is possible but unnecessary for this constraint set.

## 💡 Key Takeaways
- If the problem asks for a **contiguous** range with a constraint that can be repaired by dropping items from the left, think sliding window before considering DP or brute force.
- If pairwise validity can be summarized by a compact aggregate state (`windowMask` here), that state is usually the key to getting from quadratic checks to linear time.
- XOR-based removal is only correct because the maintained window invariant guarantees each bit is owned by at most one element; if duplicates were allowed, this would silently break.
- Update the answer after the window is restored and the new right element is added; doing it during conflict resolution produces invalid-window maxima.
- The transferable design insight is incremental constraint maintenance: preserve a compact, composable representation of validity so online optimization does not require rescanning historical data.

## 🚀 Variations & Further Practice
- Allow up to `k` overlaps per bit instead of zero. The conceptual twist is that a single OR mask is no longer sufficient; you need per-bit frequency counts and a different validity predicate.
- Return the longest clean subarray rather than the maximum-sum one. Same window mechanics, but the optimization target changes from additive state to window length.
- Make the array circular. The harder part is handling wraparound without double-counting and preserving linear-time behavior under a duplicated-array or modular-window formulation.