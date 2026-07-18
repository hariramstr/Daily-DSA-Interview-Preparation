# Longest Shelf Span With Limited Height Adjustments

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Prefix Sum

---

## 🗂 Problem Overview
Given an array `heights` and an adjustment budget, find the longest **contiguous** subarray whose elements can all be increased to a single common height without exceeding the budget. Because decreases are forbidden, the target height for any chosen span must be the maximum value already inside that span. The challenge is scale: with up to `100000` shelves, checking every subarray is infeasible, so the solution must evaluate span feasibility incrementally.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maintain the largest feasible contiguous region under an additive normalization cost. Examples include smoothing time-series buckets in observability pipelines, equalizing shard capacity over adjacent partitions, normalizing contiguous media segments in streaming systems, and enforcing bounded correction windows in manufacturing telemetry. At scale, brute-force window evaluation collapses under quadratic growth and destroys latency budgets. The right approach enables linear-time admission checks, predictable memory use, and a clean separation between local state maintenance and global optimization — exactly what matters in hot-path services and large batch pipelines.

## 🔍 Problem Statement
You are given:

- `heights`: an integer array where `heights[i]` is the stack height on shelf `i`
- `budget`: the maximum total number of unit increases allowed

Return the maximum length of a **contiguous** subarray that can be made uniform by only increasing elements.

For any chosen span `[l..r]`, the cheapest valid target height is the maximum value already in that span. The cost is:

`max(heights[l..r]) * window_length - sum(heights[l..r])`

Constraints:

- `1 <= heights.length <= 100000`
- `1 <= heights[i] <= 1000000000`
- `0 <= budget <= 100000000000000`

Examples:

- `heights = [3,1,2,2,4], budget = 3` → `3`
- `heights = [5,5,5,5], budget = 0` → `4`

The key constraint is contiguity plus large `n`: any `O(n^2)` subarray enumeration will time out.

## 🪜 How to Solve This
1. Start with the cost formula for a window: if all values must become the window maximum, cost is `max * len - sum`.  
2. That immediately suggests two maintained quantities per window: the **sum** and the **maximum**. Sum is easy with a running total or prefix sums. Maximum is harder because the window moves.  
3. Since the subarray must stay contiguous, think **sliding window** instead of sorting. But a normal sliding window only works if feasibility can be updated efficiently.  
4. To keep the current maximum under insertions and deletions, use a **monotonic deque** storing indices in decreasing height order. Its front is always the window maximum.  
5. Expand the right pointer one step at a time, update sum and deque, and compute the cost of equalizing the current window.  
6. If cost exceeds `budget`, shrink from the left until the window becomes feasible again.  
7. Every index enters and leaves the window once, so the whole process stays linear.  

The key mental move is recognizing that this is not “try all subarrays,” but “maintain the largest feasible contiguous region under a window cost function.”

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Maintain `left`, `sum`, `best`, and a **monotonic decreasing deque** of indices. The deque invariant is: heights at stored indices are in non-increasing order, and all indices lie inside the current window.

2. **Extend the window to the right**  
   For each `right`, add `heights[right]` to `sum`. Before pushing `right` into the deque, pop from the back while `heights[back] <= heights[right]`. This preserves decreasing order, so the deque front remains the maximum.

3. **Compute current equalization cost**  
   Let `maxHeight = heights[deque.front()]`. For window length `right - left + 1`, the cost is:  
   `maxHeight * windowLength - sum`  
   This is correct because every element must be raised to the current maximum, and raising is the only allowed operation.

4. **Shrink until feasible**  
   While cost exceeds `budget`, remove `heights[left]` from `sum`. If `left` equals `deque.front()`, pop the front because that index leaves the window. Increment `left` and recompute cost.  
   The invariant after shrinking: the window is feasible and the deque still reflects its maximum.

5. **Record the answer**  
   Once feasible, update `best = max(best, windowLength)`. This is safe because for each `right`, the algorithm keeps the longest valid window ending at `right`.

6. **Why this abstraction fits**  
   This is a textbook combination of **Sliding Window + Monotonic Queue + Running Sum**. Sliding window handles contiguity, the deque gives `O(1)` amortized max queries under movement, and the running sum makes cost evaluation constant-time.

## 📊 Worked Example
Example: `heights = [3,1,2,2,4]`, `budget = 3`

| right | value | window `[left..right]` | max | sum | cost = max*len-sum | action | best |
|---|---:|---|---:|---:|---:|---|---:|
| 0 | 3 | `[0..0]` → `[3]` | 3 | 3 | 0 | valid | 1 |
| 1 | 1 | `[0..1]` → `[3,1]` | 3 | 4 | 2 | valid | 2 |
| 2 | 2 | `[0..2]` → `[3,1,2]` | 3 | 6 | 3 | valid | 3 |
| 3 | 2 | `[0..3]` → `[3,1,2,2]` | 3 | 8 | 4 | shrink left | 3 |
| 3 | 2 | `[1..3]` → `[1,2,2]` | 2 | 5 | 1 | valid | 3 |
| 4 | 4 | `[1..4]` → `[1,2,2,4]` | 4 | 9 | 7 | shrink left | 3 |
| 4 | 4 | `[2..4]` → `[2,2,4]` | 4 | 8 | 4 | shrink left | 3 |
| 4 | 4 | `[3..4]` → `[2,4]` | 4 | 6 | 2 | valid | 3 |

Final answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index is pushed into the deque once and popped at most once; the left and right pointers each move monotonically across the array. The dominant work is therefore linear. At `10^6` elements this is still practical; at `10^9`, even linear scan cost becomes a systems problem rather than an algorithm problem.

### Space Complexity
`O(n)` in the worst case for the monotonic deque, though typical occupancy is smaller. The running sum and pointers are constant-space. You cannot reduce below window-state storage if you need exact max under arbitrary left-edge movement without paying extra time.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** region under a budgeted transformation cost, start with sliding window before considering DP or brute force.
- If feasibility depends on both a window aggregate and the current extreme value, look for a pairing of prefix/running sums with a monotonic deque.
- Use `max * len - sum`, not pairwise differences; the target is the window maximum because decreases are disallowed.
- Be careful to evict deque indices when `left` passes them; stale maxima silently corrupt the cost calculation.
- In production code, this pattern is a strong example of maintaining just enough incremental state to turn repeated global recomputation into a single linear pass.

## 🚀 Variations & Further Practice
- Allow both increments and decrements, minimizing cost to make a window equal: the target becomes the median, shifting the problem toward order statistics and balanced data structures.
- Ask for the number of feasible subarrays instead of the maximum length: same window mechanics, but now every valid right endpoint contributes a count.
- Replace “window maximum” with a dynamic target chosen from an external policy or capped threshold: feasibility is no longer tied to a monotonic extreme, so the state model becomes more complex.