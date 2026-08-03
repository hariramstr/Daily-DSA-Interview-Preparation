# Longest Call Streak Within Roaming Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an integer array `costs` and an integer `budget`, find the maximum length of a contiguous subarray whose total sum is at most `budget`. The output is a single integer: the longest valid streak length. The problem is non-trivial because `costs.length` can reach `100000`, which rules out checking every possible subarray. The key enabling constraint is that all roaming charges are non-negative, which makes a linear-time sliding window possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest continuous segment under a cumulative resource cap: streaming pipelines enforcing byte or latency budgets, observability systems finding the largest time window under ingest quotas, ad-tech pacing contiguous impression batches, and distributed rate-limiters evaluating sustained request bursts. At scale, brute-force range scans collapse under quadratic behavior and destroy tail latency. The sliding-window approach turns a potentially explosive search space into a single pass with stable memory, which is exactly the kind of algorithmic shift that keeps online systems predictable under high-cardinality, high-throughput workloads.

## 🔍 Problem Statement
You are given an integer array `costs` where `costs[i]` is the roaming charge for the `i`-th phone call, and an integer `budget`. Return the length of the longest contiguous subarray whose sum is less than or equal to `budget`.

Formally, choose indices `l` and `r` such that `0 <= l <= r < costs.length` and:

`costs[l] + costs[l+1] + ... + costs[r] <= budget`

Among all valid choices, maximize `r - l + 1`.

Constraints:

- `1 <= costs.length <= 100000`
- `0 <= costs[i] <= 10000`
- `0 <= budget <= 1000000000`
- All values are integers

Examples:

- `costs = [4, 2, 1, 3, 2], budget = 6` → `3`
- `costs = [7, 1, 2, 1, 1], budget = 4` → `3`

The decisive constraint is non-negative values. Once a window exceeds budget, extending it further cannot reduce the sum, so the left side must move.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so sorting or reordering is invalid.

2. Notice the objective is “longest window with sum <= budget.” That is a strong signal for a **sliding window** when values are non-negative.

3. Ask what happens when we extend the right boundary:
   - the sum stays the same or increases,
   - so if the window becomes invalid, moving `right` further will never fix it.

4. That observation tells us exactly what to do:
   - expand `right` one call at a time,
   - maintain the running sum,
   - while the sum exceeds `budget`, shrink from the left.

5. Why this works:
   - every element enters the window once,
   - every element leaves the window once,
   - so we avoid recomputing sums for overlapping subarrays.

6. Track the maximum window length after restoring validity each time.  
   The mental model is simple: keep the widest possible valid window ending at each `right` index.

This is the canonical “grow until invalid, shrink until valid” pattern.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and a running sum.**  
   Set `left = 0`, `windowSum = 0`, and `maxLen = 0`.  
   This establishes the current candidate window as `costs[left...right]`.

2. **Iterate `right` from left to right across the array.**  
   Add `costs[right]` to `windowSum`.  
   This expands the window by one element and considers all valid streaks ending at `right`.

3. **Restore validity when the budget is exceeded.**  
   While `windowSum > budget`, subtract `costs[left]` and increment `left`.  
   This is the core **Two Pointers / Sliding Window** step. Because all values are non-negative, shrinking from the left is the only way to reduce the sum. That monotonic behavior is why this abstraction fits.

4. **Update the best answer after the window is valid.**  
   Once `windowSum <= budget`, compute the current length as `right - left + 1` and update `maxLen`.  
   Invariant: after the inner loop finishes, the window is always valid.

5. **Why correctness holds.**  
   For each `right`, the algorithm finds the leftmost position that makes the window valid after any necessary shrinking. Any longer valid window ending at `right` would have to start earlier, but earlier starts were eliminated precisely because they exceeded budget.

6. **Why it is efficient.**  
   Each index is visited at most twice: once when added by `right`, once when removed by `left`. No nested rescans occur, so runtime is linear.

## 📊 Worked Example
Example: `costs = [4, 2, 1, 3, 2]`, `budget = 6`

| right | costs[right] | action                          | left | windowSum | current window | maxLen |
|------:|--------------:|----------------------------------|-----:|----------:|----------------|-------:|
| 0     | 4             | add 4                            | 0    | 4         | [4]            | 1      |
| 1     | 2             | add 2                            | 0    | 6         | [4,2]          | 2      |
| 2     | 1             | add 1, sum>6 → remove 4          | 1    | 3         | [2,1]          | 2      |
| 3     | 3             | add 3                            | 1    | 6         | [2,1,3]        | 3      |
| 4     | 2             | add 2, sum>6 → remove 2          | 2    | 6         | [1,3,2]        | 3      |

The longest valid streak length is `3`. Two windows achieve it: `[2,1,3]` and `[1,3,2]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = costs.length`. The dominant work is advancing the two pointers. Each element is added to the running sum once and removed once, so total pointer movement is linear. At `10^6` elements this remains practical; at `10^9`, linear scan cost becomes an infrastructure concern, but the algorithm is still asymptotically optimal.

### Space Complexity
`O(1)`. The algorithm stores only `left`, `right`, `windowSum`, and `maxLen`. No auxiliary arrays, heaps, or prefix-sum structures are required. Space cannot be meaningfully reduced below constant without losing the ability to track the active window state.

## 💡 Key Takeaways
- If the problem asks for a longest or shortest **contiguous** range under a threshold and all values are non-negative, think sliding window immediately.
- “Expand right, shrink left until valid” is the telltale pattern when the window metric is monotonic under non-negative additions.
- Update `maxLen` only after the window has been restored to a valid state; doing it earlier records illegal windows.
- Be careful with the length formula: for inclusive bounds, it is `right - left + 1`, not `right - left`.
- The production-grade insight is exploiting monotonic constraints to replace repeated range evaluation with incremental state maintenance.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: the objective flips, and with negative numbers allowed, standard sliding window breaks; this leads to prefix sums plus a monotonic deque.
- **Maximum consecutive ones after flipping at most `K` zeros**: same window mechanics, but the budget is count-based rather than sum-based.
- **Longest subarray with sum <= `budget` when negatives are allowed**: the monotonic property disappears, so you need more advanced prefix-sum or balanced-structure techniques.