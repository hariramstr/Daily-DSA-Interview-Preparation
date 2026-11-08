# Longest Snack Stall Run Within Spending Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `costs` of positive integers and an integer `budget`, find the maximum length of any contiguous subarray whose sum is at most `budget`. The output is a single integer: the longest affordable run of adjacent stalls. The non-trivial constraint is scale: `costs.length` can reach `100000`, so checking every possible segment with nested loops is too slow and requires a linear-time or near-linear strategy.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous span under a cumulative constraint. Examples include streaming pipelines finding the largest event burst under memory limits, ad-tech pacing windows under spend caps, rate-limit enforcement over request sequences, and storage engines compacting adjacent records while staying within I/O budgets. At scale, brute-force range evaluation collapses under quadratic behavior and destroys latency predictability. A sliding window turns the problem into a single pass with bounded state, which is exactly what production systems need for online processing, backpressure control, and predictable resource usage.

## 🔍 Problem Statement
You are given an array `costs` where `costs[i]` is the price of the snack at stall `i`, and an integer `budget`. You may buy snacks only from one contiguous block of stalls. Return the maximum number of consecutive stalls whose total cost is less than or equal to `budget`.

All values are positive, which is the key structural constraint:  
- `1 <= costs.length <= 100000`  
- `1 <= costs[i] <= 10000`  
- `1 <= budget <= 1000000000`

If every individual stall costs more than `budget`, return `0`.

Examples:

- `costs = [2, 1, 3, 2, 1], budget = 5` → `2`
- `costs = [4, 2, 1, 1, 3], budget = 6` → `3`

The positivity constraint drives the algorithmic choice. Because adding a new stall can only increase the running sum, and removing from the left can only decrease it, a sliding window is both valid and optimal.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is not sorting, prefix grouping, or arbitrary subset selection.

2. Notice the objective → maximize window length subject to `sum <= budget`.

3. Check the constraints → `n` is up to `100000`, so an `O(n^2)` scan of all subarrays is unnecessary and too expensive.

4. Look at the data property → all `costs[i]` are **positive**. That matters more than the small difficulty label. Positive numbers mean:
   - expanding the window to the right never decreases the sum
   - shrinking from the left never increases the sum

5. That monotonic behavior suggests **Two Pointers / Sliding Window**:
   - grow the right pointer to include more stalls
   - if the sum exceeds `budget`, move the left pointer forward until the window is valid again

6. At every step, once the window is valid, its length is a candidate answer.

7. Because each pointer only moves forward, the whole scan is linear. That is the reason this approach scales cleanly and is the obvious fit once you notice the positivity constraint.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window**  
   Set `left = 0`, `currentSum = 0`, and `maxLen = 0`.  
   This window represents the current contiguous segment `[left, right]`.

2. **Expand with the right pointer**  
   Iterate `right` from `0` to `n - 1`. Add `costs[right]` to `currentSum`.  
   Why this is correct: every candidate segment ending at `right` must include that value if we want to consider windows anchored at this endpoint.

3. **Restore validity when over budget**  
   While `currentSum > budget`, subtract `costs[left]` and increment `left`.  
   Invariant maintained: after this loop, the window sum is always `<= budget`.  
   This works because all values are positive, so removing items from the left strictly moves the sum toward feasibility.

4. **Record the best valid window**  
   Once valid, compute `windowLen = right - left + 1` and update `maxLen`.  
   Why this is sufficient: for a fixed `right`, the current `left` is the smallest index that makes the window valid after shrinking. Any earlier left boundary would be invalid.

5. **Repeat until the scan completes**  
   This is the classic **Two Pointers / Sliding Window** pattern: one pointer expands search space, the other contracts it just enough to preserve the constraint.

6. **Return `maxLen`**  
   If no stall is affordable, every single-element window will be shrunk away immediately, and `maxLen` remains `0`.

The core invariant is simple and powerful: after each iteration, `[left, right]` is the longest valid window ending at `right` that can be maintained without exceeding the budget.

## 📊 Worked Example
Example: `costs = [4, 2, 1, 1, 3]`, `budget = 6`

| right | costs[right] | action                              | left | currentSum | maxLen |
|------:|--------------:|-------------------------------------|-----:|-----------:|-------:|
| 0     | 4             | add 4                               | 0    | 4          | 1      |
| 1     | 2             | add 2                               | 0    | 6          | 2      |
| 2     | 1             | add 1 → sum 7, shrink by removing 4 | 1    | 3          | 2      |
| 3     | 1             | add 1                               | 1    | 4          | 3      |
| 4     | 3             | add 3 → sum 7, shrink by removing 2 | 2    | 5          | 3      |

Trace summary:
1. `[4]` is valid.
2. `[4,2]` reaches budget exactly.
3. Adding `1` breaks the budget, so move `left` once.
4. Window `[2,1,1]` is valid and length `3`.
5. Adding `3` breaks the budget again; shrink to `[1,1,3]`, also length `3`.

Final answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is added to the running sum once when the right pointer advances and removed at most once when the left pointer advances. There is no nested reprocessing. At `10^6` elements this remains practical in a single pass; at `10^9`, the bottleneck becomes I/O and memory bandwidth, not algorithmic shape.

### Space Complexity
`O(1)`. The algorithm stores only a few scalar variables: two pointers, a running sum, and the best length. No auxiliary array or heap is required. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a threshold and all values are positive, sliding window should be your first candidate.
- “Expand until invalid, then shrink until valid” is the signature pattern when the constraint is monotonic under pointer movement.
- Update the answer **after** restoring `sum <= budget`; recording before shrinking produces invalid lengths.
- Be careful with window length calculation: for inclusive pointers, it is `right - left + 1`, not `right - left`.
- In production systems, positive-only cumulative constraints often enable online single-pass algorithms that replace expensive retrospective range scans.

## 🚀 Variations & Further Practice
- **Longest subarray with sum at most `k` when negatives are allowed** — sliding window breaks because the sum is no longer monotonic; typically requires prefix sums plus a more advanced data structure or binary search technique.
- **Minimum size subarray with sum at least `target`** — same two-pointer pattern, but the optimization goal flips from maximizing length to minimizing it, which changes when you update the answer.
- **Maximum average or weighted window under a budget-like constraint** — introduces non-uniform scoring, often requiring prefix sums, binary search on the answer, or deque-based optimizations.