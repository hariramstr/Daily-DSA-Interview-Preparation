# Longest Trading Window With Limited Price Peaks

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `prices` and an integer `k`, find the maximum length of a contiguous window whose number of internal peak indices is at most `k`. An index counts as a peak only if it lies strictly inside the current window and is greater than both adjacent values inside that same window. The challenge is boundary dependence: expanding or shrinking the window can create or destroy peaks at the edges, so static precomputation alone is insufficient.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, market-data surveillance, observability pipelines, and anomaly detection over rolling windows. The hard part is not counting events, but counting context-dependent events whose validity changes when window boundaries move. Without an incremental window strategy, systems fall back to recomputation per range, which collapses under high-throughput feeds or long retention intervals. A correct two-pointer design enables online processing, bounded memory, and predictable latency, which matters in real-time scoring, alert suppression, and any service that must maintain rolling constraints over mutable local structure.

## 🔍 Problem Statement
You are given an integer array `prices` of length up to `200000`, where `prices[i]` is the stock price at minute `i`, and an integer `k`. A minute `i` is a peak inside window `[l, r]` only if `l < i < r` and:

- `prices[i] > prices[i - 1]`
- `prices[i] > prices[i + 1]`

Return the maximum length of a contiguous window containing at most `k` such peaks.

Key edge case: peak status depends on the current window. An index that is a peak in one window may stop being a peak after moving to the boundary, because boundary elements are never internal peaks.

Examples:

- `prices = [5, 1, 4, 2, 6, 3, 7], k = 2` → `6`
- `prices = [2, 9, 1, 8, 3, 7, 4], k = 1` → `4`

The constraint `n <= 200000` rules out recomputing peaks for every candidate window.

## 🪜 How to Solve This
1. Read the definition carefully → peak status is local: index `i` depends only on `i-1`, `i`, `i+1`.
2. Then notice the boundary wrinkle → only indices strictly inside `[l, r]` can count, so the active peak set for a window is exactly the globally peak-shaped indices that fall in `[l+1, r-1]`.
3. That observation simplifies the problem: first mark every index `i` in the full array that satisfies `prices[i] > prices[i-1] && prices[i] > prices[i+1]`.
4. Now a window is valid iff the sum of those marked indices over its interior is at most `k`.
5. Since we need the longest contiguous valid window and the constraint is monotone under shrinking, think sliding window / two pointers.
6. Expand the right pointer one step at a time. When `r` moves, only index `r-1` newly becomes interior, so only that position can start contributing.
7. If peak count exceeds `k`, move the left pointer right. When `l` moves, only index `l+1` stops being interior, so only that position can stop contributing.
8. Maintain the invariant: current `peakCount` equals the number of marked peak indices strictly inside `[l, r]`.

## 🧩 Algorithm Walkthrough
1. **Precompute local peaks.**  
   Build a boolean or `0/1` array `isPeak` for indices `1..n-2`, where `isPeak[i] = 1` iff `prices[i] > prices[i-1] && prices[i] > prices[i+1]`. This is correct because peak-ness depends only on immediate neighbors in the original array; window boundaries only determine whether that index is interior.

2. **Initialize a Two Pointers window.**  
   Start with `l = 0`, `peakCount = 0`, `best = 1`. The abstraction is standard sliding window: maintain the longest range satisfying a monotone constraint, here “interior peak count <= k”.

3. **Expand the right boundary.**  
   For each `r` from `0` to `n-1`, add the contribution of index `r-1` if `r - 1 > l`. More simply, when `r >= 2`, `r-1` has just become an interior index of `[l, r]`, so add `isPeak[r-1]`.

4. **Restore validity by shrinking from the left.**  
   While `peakCount > k`, move `l` right by one. Before incrementing `l`, remove `isPeak[l+1]` if `l + 1 < r`, because that index ceases to be interior after the shift. This preserves the invariant that `peakCount` matches peaks strictly inside the current window.

5. **Update the answer.**  
   After each expansion and any required shrinking, `[l, r]` is valid. Update `best = max(best, r - l + 1)`.

6. **Why this is linear.**  
   Each pointer advances at most `n` times. No index is added to or removed from the interior more than once per pointer movement, so total work is `O(n)`.

## 📊 Worked Example
Take `prices = [5, 1, 4, 2, 6, 3, 7]`, `k = 2`.

Global peaks: `isPeak = [0, 0, 1, 0, 1, 0, 0]` at indices `2` and `4`.

| r | New interior index added | peakCount after add | Shrink? | l | Window | best |
|---|---:|---:|---|---:|---|---:|
| 0 | - | 0 | no | 0 | `[0,0]` | 1 |
| 1 | - | 0 | no | 0 | `[0,1]` | 2 |
| 2 | 1 | 0 | no | 0 | `[0,2]` | 3 |
| 3 | 2 | 1 | no | 0 | `[0,3]` | 4 |
| 4 | 3 | 1 | no | 0 | `[0,4]` | 5 |
| 5 | 4 | 2 | no | 0 | `[0,5]` | 6 |
| 6 | 5 | 2 | no | 0 | `[0,6]` | 7 |

For this exact definition, the full window has only peaks `4` and `6` at indices `2` and `4`; index `6` is boundary, so not counted. The maximum valid length is `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass builds `isPeak`, and one sliding-window pass advances `r` from left to right while `l` also moves forward at most `n` times total. At `10^6` elements this remains practical in-memory; at `10^9`, the algorithm is still optimal asymptotically but becomes constrained by I/O and memory bandwidth.

### Space Complexity
`O(n)` if you materialize `isPeak`. That storage is owned by the auxiliary peak-marker array. It can be reduced to `O(1)` extra space by recomputing local peak checks on boundary transitions, but the implementation becomes easier to get wrong and less transparent.

## 💡 Key Takeaways
- If a problem asks for the longest contiguous range under a count-based constraint that becomes valid again when the left edge moves right, it is a sliding-window signal.
- If the counted property is local to neighboring elements, first ask whether it can be converted into a per-index marker plus an interior-range accounting rule.
- The main trap is boundary semantics: window endpoints are never peaks, even if they look like peaks globally.
- When `r` expands, only `r-1` can newly enter the interior; when `l` advances, only `l+1` can leave it. Updating anything else is an off-by-one bug.
- At scale, the win comes from maintaining a moving invariant over a stream instead of recomputing contextual state for every candidate range.

## 🚀 Variations & Further Practice
- Allow both peaks and valleys, with separate budgets for each. The twist is maintaining two interior event counts under the same moving boundaries.
- Ask for the number of valid windows instead of the longest one. The twist is converting the same invariant into a counting argument over all right endpoints.
- Make the peak definition depend on a wider neighborhood, such as being greater than the previous and next `d` elements. The twist is that local `O(1)` updates may require auxiliary range-max structures.