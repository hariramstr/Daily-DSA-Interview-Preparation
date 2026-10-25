# Longest Parcel Belt Stretch With Limited Fragile Items

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Hash Set

---

## 🗂 Problem Overview
Given an array `parcels`, a set-like list `fragileTypes`, and a limit `k`, find the maximum length of any contiguous subarray containing at most `k` fragile parcels. A parcel is fragile if its value appears in `fragileTypes`. The challenge is scale: with up to `200000` parcels, enumerating all subarrays is infeasible. The solution must evaluate validity incrementally while scanning once, not recompute fragile counts for every candidate segment.

## 🌍 Engineering Impact
This pattern shows up in streaming systems, warehouse control software, network traffic shaping, and observability pipelines: find the longest recent span that stays within a bounded “bad event” budget. Examples include longest request window with at most `k` throttled calls, longest log segment with limited error-class events, or longest batch with bounded manual-review items. At scale, brute force collapses under quadratic scans and repeated membership checks. A sliding window plus constant-time classification enables online processing, predictable latency, and memory bounded by metadata rather than input history.

## 🔍 Problem Statement
You are given:

- `parcels`: an integer array of parcel type codes
- `fragileTypes`: an integer array of distinct type codes considered fragile
- `k`: the maximum number of fragile parcels allowed in a valid contiguous stretch

Return the length of the longest contiguous subarray of `parcels` containing at most `k` values present in `fragileTypes`.

Constraints:

- `1 <= parcels.length <= 200000`
- `0 <= parcels[i] <= 10^9`
- `0 <= fragileTypes.length <= 200000`
- `fragileTypes` contains distinct values
- `0 <= fragileTypes[i] <= 10^9`
- `0 <= k <= parcels.length`

Examples:

- `parcels = [4,7,2,7,9,3,7,5], fragileTypes = [7,9], k = 2` → `4`
- `parcels = [1,8,6,8,2,4,6], fragileTypes = [8,4], k = 1` → `3`

The key constraint is input size: checking every subarray would be `O(n^2)`, which is too slow.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment with a bounded count of “special” elements. That is a strong sliding-window signal.

2. Notice what makes a segment valid: not sum, not distinct count, just **how many elements belong to a predefined set**. That means each new parcel can be classified in `O(1)` if `fragileTypes` is stored in a `HashSet`.

3. Ask what happens when a window becomes invalid. If adding `parcels[right]` pushes fragile count above `k`, no prefix extension helps; the only fix is to move the left boundary rightward until the count is back within budget.

4. That monotonic behavior is why two pointers work: `right` only moves forward, and `left` only moves forward. No backtracking, no nested rescans.

5. Track the maximum valid window length after every adjustment. Since each element enters and leaves the window at most once, the full scan is linear after set construction.

This is the standard “longest subarray with at most K bad items” template.

## 🧩 Algorithm Walkthrough
1. **Build a `HashSet` of fragile types.**  
   This gives constant-time membership checks for each parcel. Without it, checking whether a parcel is fragile could degrade to `O(m)` per element, where `m = fragileTypes.length`.

2. **Initialize the sliding window.**  
   Use two pointers: `left = 0`, and iterate `right` from `0` to `n - 1`. Maintain `fragileCount`, the number of fragile parcels inside the current window `[left, right]`.

3. **Expand the window by moving `right`.**  
   For each `parcels[right]`, check whether it is in the fragile set. If yes, increment `fragileCount`.  
   Invariant after expansion: the window reflects exactly the elements between `left` and `right`.

4. **Shrink while invalid.**  
   If `fragileCount > k`, move `left` rightward until the window is valid again. Each time `parcels[left]` is fragile, decrement `fragileCount` before advancing `left`.  
   Invariant after shrinking: `[left, right]` is the smallest valid window ending at `right`.

5. **Update the answer.**  
   Once the window is valid, compute `right - left + 1` and maximize the result. This is correct because for a fixed `right`, the longest valid window ending there is the leftmost valid one after shrinking.

6. **Why this pattern fits.**  
   This is a classic **Sliding Window / Two Pointers** problem: contiguous range, monotonic validity restoration, and a local metric (`fragileCount`) that can be updated incrementally.

## 📊 Worked Example
Use `parcels = [4,7,2,7,9,3,7,5]`, `fragileTypes = {7,9}`, `k = 2`.

| right | parcels[right] | fragileCount after add | left after shrink | window        | len | best |
|------:|----------------|------------------------|-------------------|---------------|----:|-----:|
| 0     | 4              | 0                      | 0                 | [4]           | 1   | 1    |
| 1     | 7              | 1                      | 0                 | [4,7]         | 2   | 2    |
| 2     | 2              | 1                      | 0                 | [4,7,2]       | 3   | 3    |
| 3     | 7              | 2                      | 0                 | [4,7,2,7]     | 4   | 4    |
| 4     | 9              | 3                      | 2                 | [2,7,9]       | 3   | 4    |
| 5     | 3              | 2                      | 2                 | [2,7,9,3]     | 4   | 4    |
| 6     | 7              | 3                      | 4                 | [9,3,7]       | 3   | 4    |
| 7     | 5              | 2                      | 4                 | [9,3,7,5]     | 4   | 4    |

The longest valid stretch has length `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = parcels.length` and `m = fragileTypes.length`. Building the hash set costs `O(m)`, and the sliding window costs `O(n)` because each pointer advances at most `n` times. This remains practical at `10^6` scale; at `10^9`, even linear scans become operationally expensive.

### Space Complexity
`O(m)` for the `HashSet` storing fragile types. The window itself uses constant extra state. Space can only be reduced by replacing the set with slower membership checks, which trades memory efficiency for unacceptable time overhead on large inputs.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under an **at most K violations** rule, think sliding window immediately.
- If validity depends on whether elements belong to a predefined collection, convert that collection to a `HashSet` before scanning.
- Be careful to decrement `fragileCount` only when the element leaving the window is actually fragile.
- Update the answer **after** restoring validity; measuring while `fragileCount > k` produces inflated lengths.
- The production analogue is budget-constrained stream processing: maintain a valid rolling region by updating local state incrementally instead of recomputing global properties.

## 🚀 Variations & Further Practice
- Return the actual subarray indices, not just the length; same pattern, but now tie-breaking rules matter when multiple windows share the maximum length.
- Replace the binary fragile/non-fragile rule with per-type weights and require total fragility cost `<= budget`; this becomes a weighted sliding-window constraint.
- Allow offline queries with different `k` values over the same parcel stream; the harder part is preprocessing or indexing so repeated scans are avoided.