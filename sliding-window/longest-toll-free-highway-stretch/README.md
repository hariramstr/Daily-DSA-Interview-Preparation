# Longest Toll-Free Highway Stretch

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `costs` of non-negative toll fees and an integer `budget`, find the maximum length of a contiguous subarray whose total sum does not exceed `budget`. The output is a single integer: the longest valid stretch of checkpoints. The non-trivial part is efficiency: with up to `100000` elements, brute-force enumeration of all subarrays is too slow, so the solution must exploit the fact that all costs are non-negative.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize a contiguous span under a cumulative resource cap: streaming pipelines bounded by memory, API gateways enforcing rolling request-cost budgets, ad-serving systems choosing the longest eligible impression run, and observability backends scanning the largest time window under byte or latency limits. At scale, a naive recomputation of every interval becomes quadratic and collapses under sustained throughput. The sliding-window approach turns a global search into a single pass with stable memory, which is exactly what enables online processing, predictable latency, and straightforward horizontal scaling.

## 🔍 Problem Statement
You are given an integer array `costs` where `costs[i]` is the toll charged at checkpoint `i`, and an integer `budget` representing the maximum total amount the traveler can spend on one continuous stretch of the trip.

Return the length of the longest contiguous subarray whose sum is less than or equal to `budget`.

Constraints:
- `1 <= costs.length <= 100000`
- `0 <= costs[i] <= 10000`
- `0 <= budget <= 1000000000`

Examples:
- `costs = [4, 2, 1, 3, 2]`, `budget = 6` → `3`
- `costs = [1, 1, 1, 1, 1]`, `budget = 3` → `3`

Edge cases matter:
- `budget = 0` means only stretches made entirely of zero-cost checkpoints are valid.
- Large arrays rule out nested-loop enumeration.
- The key algorithmic constraint is that all values are non-negative, which makes a shrinking window safe and monotonic.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous** region, so this is immediately about windows, not sorting or arbitrary subset selection.

2. Notice the constraint on values → every `costs[i]` is non-negative. That matters because when you expand a window to the right, the sum can only stay the same or increase. When you move the left edge rightward, the sum can only stay the same or decrease.

3. That monotonic behavior suggests a **sliding window / two pointers** strategy. Maintain a current window `[left, right]` and its running sum.

4. Expand `right` one step at a time, adding each new toll to the running sum.

5. If the sum exceeds `budget`, the current window is invalid. Because values are non-negative, the only way to restore validity is to shrink from the left until the sum is back within budget.

6. After each adjustment, the window is the longest valid window ending at `right`, so update the best length seen so far.

7. This avoids recomputing sums for overlapping subarrays and guarantees linear time because each pointer only moves forward.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `windowSum = 0`, and `maxLen = 0`.  
   This defines an empty sliding window. The invariant is: before processing each result update, `windowSum` equals the sum of `costs[left..right]`.

2. **Iterate `right` from left to right**  
   For each checkpoint, add `costs[right]` to `windowSum`.  
   This grows the candidate window by one element. The pattern here is **Two Pointers / Sliding Window**, appropriate because the validity condition depends on a cumulative metric over a contiguous range.

3. **Restore validity when over budget**  
   While `windowSum > budget`, subtract `costs[left]` from `windowSum` and increment `left`.  
   This is correct only because all tolls are non-negative: removing elements from the left cannot increase the sum, so repeated shrinking must eventually restore validity.

4. **Record the best valid window**  
   Once the while-loop finishes, the current window `[left, right]` satisfies `windowSum <= budget`. Its length is `right - left + 1`. Update `maxLen` if this length is larger.

5. **Maintain the key invariant**  
   After each iteration, the window is valid and is the longest valid suffix ending at `right`. Any earlier `left` would have produced a sum above budget, so no longer valid window ending at this `right` was missed.

6. **Return `maxLen`**  
   Since every right boundary is processed exactly once and every left boundary advances at most once, the algorithm is complete and linear.

## 📊 Worked Example
Example: `costs = [4, 2, 1, 3, 2]`, `budget = 6`

| right | costs[right] | action                         | left | windowSum | valid window   | maxLen |
|------:|-------------:|--------------------------------|-----:|----------:|----------------|-------:|
| 0     | 4            | add 4                          | 0    | 4         | `[4]`          | 1      |
| 1     | 2            | add 2                          | 0    | 6         | `[4,2]`        | 2      |
| 2     | 1            | add 1, sum=7 > 6, remove 4     | 1    | 3         | `[2,1]`        | 2      |
| 3     | 3            | add 3                          | 1    | 6         | `[2,1,3]`      | 3      |
| 4     | 2            | add 2, sum=8 > 6, remove 2     | 2    | 6         | `[1,3,2]`      | 3      |

The longest valid stretches are `[2,1,3]` and `[1,3,2]`, both length `3`. The algorithm returns `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = costs.length`. Each element is added to the window once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing of ranges. At `10^6` elements this remains practical in a single pass; at `10^9`, the bottleneck becomes I/O and memory locality rather than algorithmic complexity.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, a running sum, and the best length. No prefix array or extra data structure is required. You could trade this for prefix sums, but that would increase memory without improving asymptotic runtime here.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** region under a threshold and values are non-negative, sliding window should be your first candidate.
- “Expand until invalid, then shrink until valid” is the recognition pattern for two-pointer problems with monotonic window behavior.
- Update the answer **after** shrinking, not before; otherwise you may record an invalid window.
- Be careful with window length calculation: for an inclusive range `[left, right]`, the length is `right - left + 1`.
- In production systems, this pattern matters because non-negative monotonic constraints let you replace repeated interval scans with a single online pass.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `target`**: same window mechanics, but the optimization goal flips from maximizing length under a cap to minimizing length over a floor.
- **Longest subarray with at most `k` distinct values**: still sliding window, but validity depends on frequency state rather than a scalar sum, so you need a hash map.
- **Subarray sum with negative numbers allowed**: standard sliding window breaks because the sum is no longer monotonic; this pushes you toward prefix sums plus deque, binary search, or balanced structures.