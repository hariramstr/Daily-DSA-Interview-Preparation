# Longest Whiteboard Streak Within Marker Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `inkUse` of non-negative integers and an integer `maxInk`, find the maximum length of any contiguous subarray whose total sum is at most `maxInk`. The output is a single integer: the longest valid uninterrupted writing streak. The non-trivial constraint is that the array can be large, so checking every possible subarray is too expensive. The fact that all values are non-negative is what makes a linear-time sliding window possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous span under a cumulative budget: API rate-limit windows, streaming job batch sizing, log ingestion under memory caps, media buffering, and query planning under cost thresholds. At scale, brute-force range evaluation collapses under quadratic growth and creates avoidable latency spikes. The sliding-window formulation turns a potentially expensive search into a single pass with stable memory, which matters in hot paths and online systems. More importantly, it relies on monotonicity from non-negative costs, a property that often determines whether a design can be incremental or requires heavier indexing structures.

## 🔍 Problem Statement
You are given:

- `inkUse`, an array where `inkUse[i]` is the marker ink required for section `i`
- `maxInk`, the maximum ink available for one continuous writing session

Return the length of the longest contiguous subarray whose sum is `<= maxInk`.

Constraints:

- `1 <= inkUse.length <= 100000`
- `0 <= inkUse[i] <= 10000`
- `0 <= maxInk <= 1000000000`
- All values are non-negative integers

Examples:

- `inkUse = [2, 1, 3, 2, 1]`, `maxInk = 5` → `2`
- `inkUse = [1, 0, 2, 1, 1, 0, 1]`, `maxInk = 4` → `5`

Edge cases matter:

- `maxInk = 0` means only all-zero windows are valid
- Single-element arrays must still work
- Zero values can extend a valid window without increasing the sum

The algorithmic choice is driven by one critical constraint: all costs are non-negative, so expanding the window never decreases the sum.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** group, so this is about ranges, not subsets or sorting.

2. Notice the condition is based on the **sum of the current range** being `<= maxInk`. That suggests maintaining a running total instead of recomputing sums repeatedly.

3. Ask what happens when we expand a range to the right. Because all `inkUse[i]` values are non-negative, the sum can only stay the same or increase. That monotonic behavior is the key signal for a sliding window.

4. Start with a window `[left...right]` and keep a running `sum`. Move `right` forward one step at a time, adding the new value.

5. If the window becomes invalid (`sum > maxInk`), shrink it from the left until it becomes valid again. Since removing non-negative values can only decrease the sum, this converges immediately.

6. After each adjustment, the current window is the longest valid window ending at `right`, so update the best length.

7. This avoids nested scans: each element enters the window once and leaves once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window (Two Pointers).**  
   Set `left = 0`, `sum = 0`, and `best = 0`. The window is represented by indices `[left, right]`. The invariant is: after rebalancing, `sum` always equals the sum of the current window.

2. **Expand the window by advancing `right`.**  
   For each index `right`, add `inkUse[right]` to `sum`. This tests whether the current contiguous range can be extended. Because values are non-negative, extension never hides an overflow; if the sum is too large now, it will not fix itself without shrinking.

3. **Restore validity by advancing `left` while needed.**  
   While `sum > maxInk`, subtract `inkUse[left]` from `sum` and increment `left`. This is correct because every invalid window containing the current left boundary is too expensive, so keeping those elements cannot help. The invariant after this loop: `sum <= maxInk`, and `[left, right]` is the smallest valid window ending at `right`.

4. **Record the best valid length.**  
   Once the window is valid, compute `right - left + 1` and update `best` if larger. This works because every valid window ending at `right` and starting before `left` was already ruled out as over budget.

5. **Finish after one pass.**  
   Each pointer only moves forward. That is why the Two Pointers / Sliding Window pattern is the right abstraction here: monotonic input costs let us maintain a valid contiguous region incrementally instead of recomputing or backtracking.

## 📊 Worked Example
Example: `inkUse = [1, 0, 2, 1, 1, 0, 1]`, `maxInk = 4`

| right | inkUse[right] | sum after add | action while `sum > 4` | left | valid window        | length | best |
|------:|---------------:|--------------:|-------------------------|-----:|---------------------|-------:|-----:|
| 0     | 1              | 1             | none                    | 0    | `[1]`               | 1      | 1    |
| 1     | 0              | 1             | none                    | 0    | `[1,0]`             | 2      | 2    |
| 2     | 2              | 3             | none                    | 0    | `[1,0,2]`           | 3      | 3    |
| 3     | 1              | 4             | none                    | 0    | `[1,0,2,1]`         | 4      | 4    |
| 4     | 1              | 5             | remove `1`              | 1    | `[0,2,1,1]`         | 4      | 4    |
| 5     | 0              | 4             | none                    | 1    | `[0,2,1,1,0]`       | 5      | 5    |
| 6     | 1              | 5             | remove `0`, remove `2`  | 3    | `[1,1,0,1]`         | 4      | 5    |

Answer: `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = inkUse.length`. Each element is added to the running sum once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing. This scales comfortably to `10^6` elements; at `10^9`, linear time is still expensive but remains the only viable class for online processing.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few scalar variables: `left`, `right`, `sum`, and `best`. No extra data structure grows with input size. You cannot meaningfully reduce this further without losing the ability to track the active window sum incrementally.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous range** under a threshold and all values are non-negative, sliding window should be your first candidate.
- The strongest recognition signal is **monotonicity**: expanding the window only increases or preserves the running sum, so shrinking from the left is sufficient.
- Update the answer **after** shrinking, not before; otherwise you may record an invalid over-budget window.
- Be careful with window length calculation: for an inclusive window `[left, right]`, the size is `right - left + 1`.
- In production systems, this pattern is valuable because monotonic constraints often let you replace repeated range evaluation with a single streaming pass and constant memory.

## 🚀 Variations & Further Practice
- **Minimum size subarray sum**: instead of maximizing length under a budget, minimize length reaching at least a target; same pattern, but the optimization direction changes.
- **Longest subarray with sum exactly `k` when negatives are allowed**: sliding window breaks because monotonicity disappears; prefix sums plus hashing become necessary.
- **Maximum consecutive ones III / longest repeating character replacement**: still sliding window, but the validity condition depends on frequency state rather than a simple running sum.