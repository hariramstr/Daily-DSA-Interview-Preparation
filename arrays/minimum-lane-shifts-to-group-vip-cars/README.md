# Minimum Lane Shifts to Group VIP Cars

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Median

---

## 🗂 Problem Overview
Given a binary array `lanes`, where `1` marks a VIP car, compute the minimum number of adjacent swaps needed so all VIP cars occupy one contiguous block. Each swap moves a VIP car by one position through neighboring cars, costing `1`. If there are fewer than two VIP cars, the answer is `0`. The challenge is scale: trying every possible target block is too slow for `n` up to `100000`, so the solution must exploit structure in the VIP positions.

## 🌍 Engineering Impact
This pattern shows up anywhere sparse “special” items must be compacted with minimum movement cost: log compaction in storage engines, shard rebalancing in distributed queues, packet reordering in network schedulers, and seat or slot consolidation in reservation systems. At small scale, brute force is acceptable; at production scale, it becomes a latency and cost amplifier. The useful abstraction is not the raw array but the positions of the marked elements and the fact that minimizing total absolute movement collapses to a median-based decision. That shift turns an intractable search over placements into a linear pass with predictable memory and runtime.

## 🔍 Problem Statement
You are given an array `lanes` of length `1 <= n <= 100000`, where each element is either `0` or `1`. A `1` represents a VIP car and a `0` represents a regular car. In one operation, you may swap a VIP car with an adjacent car to its left or right, with cost `1` per adjacent swap.

Return the minimum total number of adjacent swaps required to make all VIP cars occupy consecutive positions somewhere in the array. The final block can start anywhere. If the array contains `0` or `1` VIP car, return `0`. The result fits in a 64-bit integer.

Examples:

- `lanes = [1,0,0,1,0,1]` → `3`
- `lanes = [0,1,0,1,0,0,1,0]` → `4`

The key constraint is that enumerating every possible contiguous target block and summing movements would be too slow; the algorithm must run near linearly.

## 🪜 How to Solve This
1. Read the problem → adjacent swaps on a binary array usually mean positions matter more than values.
2. Extract the indices of all VIP cars. Ignore regular cars except as distance between VIPs.
3. We do **not** care which exact block they end up in yet. We care about minimizing total movement.
4. If VIP cars end up consecutive, then after ordering them left to right, their targets look like `x, x+1, x+2, ...`.
5. For a VIP currently at position `pos[i]`, moving it to `x+i` costs `|pos[i] - (x+i)|`.
6. Rearrange that as `|(pos[i] - i) - x|`. Now the problem becomes: choose `x` minimizing the sum of absolute deviations.
7. That is the classic median property: the sum of absolute distances is minimized at the median.
8. So build `adjusted[i] = pos[i] - i`, choose its median, and sum distances to that median.
9. Because `pos` is increasing, `adjusted` is also non-decreasing, so the median is available directly without sorting.
10. Edge case: `0` or `1` VIP car means no grouping work, so answer is `0`.

## 🧩 Algorithm Walkthrough
1. **Collect VIP positions**  
   Scan `lanes` once and store every index `i` where `lanes[i] == 1` in `pos`.  
   Why correct: adjacent swaps only affect how far each VIP travels; the zeros are just empty distance.  
   Invariant: `pos` is strictly increasing.

2. **Handle trivial cases**  
   If `pos.size() <= 1`, return `0`.  
   Why correct: zero or one VIP is already contiguous by definition.  
   Invariant: remaining logic assumes at least two VIP cars.

3. **Normalize away the consecutive target offsets**  
   Build conceptual values `adjusted[i] = pos[i] - i`.  
   Why correct: if the final contiguous block starts at `x`, then VIP `i` must land at `x + i`, so its cost is `|pos[i] - (x+i)| = |adjusted[i] - x|`.  
   Invariant: minimizing swaps is now equivalent to minimizing total absolute deviation from a single scalar `x`.

4. **Choose the median**  
   Let `m = adjusted[k/2]`, where `k = pos.size()`.  
   Pattern: **Median minimization over absolute distances**, often paired with **prefix-sum-friendly position transforms**.  
   Why correct: the median minimizes the sum of absolute differences. Since `pos` is sorted, `adjusted` is sorted too, so no extra sort is needed.  
   Invariant: `m` defines an optimal starting index for the final VIP block.

5. **Accumulate total movement**  
   Sum `abs(adjusted[i] - m)` across all VIPs using 64-bit arithmetic.  
   Why correct: this is exactly the total adjacent swap count required to move each VIP into the optimal contiguous block while preserving left-to-right order implied by swaps.  
   Invariant: after processing index `i`, the partial sum equals the minimum cost contribution of the first `i+1` VIP cars.

## 📊 Worked Example
Take `lanes = [1,0,0,1,0,1]`.

VIP positions are `pos = [0,3,5]`, so `k = 3`.

| i | pos[i] | adjusted[i] = pos[i] - i |
|---|--------|---------------------------|
| 0 | 0      | 0                         |
| 1 | 3      | 2                         |
| 2 | 5      | 3                         |

Median of `adjusted` is `2`, so the optimal block starts at index `x = 2`.

That means target VIP positions are:

- VIP 0: `2`
- VIP 1: `3`
- VIP 2: `4`

Movement cost:

- `|0 - 2| = 2`
- `|3 - 3| = 0`
- `|5 - 4| = 1`

Total = `2 + 0 + 1 = 3`.

This matches the minimum adjacent swaps. The normalization step is the key: instead of searching all blocks, it converts the problem into “pick the best scalar center,” which is solved by the median.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is `lanes.length`. One pass collects VIP positions, and one pass computes the movement sum. There is no nested scan over candidate blocks and no sort beyond the natural order from the input scan. At `10^6` elements this is routine; at `10^9`, linear work is still expensive but remains the only viable asymptotic shape.

### Space Complexity
`O(k)`, where `k` is the number of VIP cars, due to storing their positions. This can be reduced only slightly by streaming into a compact structure or doing multiple passes, but you still need enough state to identify the median or equivalent pivot.

## 💡 Key Takeaways
- If a binary-array problem asks to make all marked elements contiguous with adjacent swaps, extract positions first; the raw array is usually the wrong representation.
- When target locations form a consecutive sequence, try subtracting the element rank (`pos[i] - i`) to reduce the problem to median minimization.
- Do not sum distances to raw VIP positions; you must normalize by index, or you will overcount the spacing that disappears in the final contiguous block.
- Use 64-bit arithmetic for the total cost; individual moves fit in `int`, but aggregate movement can exceed 32-bit range.
- The transferable design insight: many “search over all placements” problems collapse once you transform state into a monotone coordinate system and optimize with a robust statistic like the median.

## 🚀 Variations & Further Practice
- Allow at most `k` non-VIP cars inside the final block instead of requiring perfect contiguity; the twist is that the objective becomes a sliding-window optimization rather than direct median minimization.
- Minimize swaps to group exactly `m` out of all VIP cars; the harder part is choosing the best subset/window of VIP positions before applying the same normalization.
- Extend from 1D positions to 2D parking slots with Manhattan movement; the conceptual twist is separability across axes and handling independent medians per dimension.