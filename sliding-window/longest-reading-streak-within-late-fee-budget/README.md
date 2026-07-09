# Longest Reading Streak Within Late Fee Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `lateFees` of non-negative daily fees and a budget `B`, find the maximum length of a contiguous block whose total fee is at most `B`. The output is a single integer: the longest valid window size. The non-trivial part is scale: with up to `100000` days, checking every subarray is too expensive. The key structural constraint is that all fees are non-negative, which makes a shrinking sliding window correct and efficient.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval under an additive budget: API rate-limit windows, streaming job backpressure thresholds, ad impression pacing, mobile data usage caps, and log ingestion bounded by cost or latency budgets. At scale, brute-force range evaluation collapses under quadratic growth and destroys tail latency. The sliding-window formulation turns a potentially explosive search into a single pass with stable memory use. Architecturally, that enables online processing, predictable throughput, and easy integration into streaming or edge systems where you cannot afford rescans or large auxiliary indexes.

## 🔍 Problem Statement
You are given an integer array `lateFees` where `lateFees[i]` is the late fee charged on day `i`, and an integer budget `B`. Return the length of the longest contiguous subarray whose sum is less than or equal to `B`.

Constraints:

- `1 <= lateFees.length <= 100000`
- `0 <= lateFees[i] <= 10000`
- `0 <= B <= 1000000000`
- All values in `lateFees` are non-negative

Examples:

- `lateFees = [2, 1, 3, 2, 1], B = 5` → `2`
- `lateFees = [0, 1, 1, 0, 2, 1], B = 3` → `4`

Edge cases matter: `B` can be `0`, fees can contain zeros, and a single day may already exceed budget. The decisive constraint is non-negativity. Because adding a new element can only keep or increase the window sum, and removing from the left can only keep or decrease it, a two-pointer sliding window is both valid and optimal.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** block, not an arbitrary subset. That immediately suggests a window over the array rather than sorting or dynamic programming.

2. Notice the objective → maximize window length while keeping `sum <= B`. That is the classic “expand while valid, shrink when invalid” shape.

3. Check the values → all fees are non-negative. This is the enabling property. If the current window exceeds budget, extending it further cannot fix it; only moving the left boundary right can reduce the sum.

4. Maintain two pointers:
   - `right` expands the candidate window one day at a time.
   - `left` advances only when the budget is violated.

5. Track a running sum instead of recomputing subarray sums. Each element is added once when `right` moves and removed once when `left` moves.

6. After restoring validity, compute the current window length and update the best answer.

That reasoning gets you from “find longest valid contiguous range” to an `O(n)` solution with constant extra space, which is exactly what the constraints demand.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window**  
   Set `left = 0`, `currentSum = 0`, and `best = 0`. The pattern here is **Two Pointers / Sliding Window**: one pointer grows the window, the other contracts it. This abstraction fits because the validity condition depends on a running aggregate over a contiguous range.

2. **Expand the window with `right`**  
   Iterate `right` from `0` to `n - 1`. Add `lateFees[right]` to `currentSum`. This means the active window is always `lateFees[left...right]`.

3. **Restore validity when over budget**  
   While `currentSum > B`, subtract `lateFees[left]` from `currentSum` and increment `left`. This is correct because all values are non-negative: removing items from the left is the only way to reduce the sum, and once a prefix makes the window invalid, keeping that prefix cannot help any longer window ending at the same `right`.

4. **Update the best length**  
   After the shrink loop, the invariant is: `currentSum <= B`, and `left` is the smallest index that makes the current window valid. Now compute `right - left + 1` and update `best`.

5. **Continue until the array ends**  
   Every index moves monotonically rightward. No element re-enters the window after leaving it, which is why the total work is linear.

The maintained invariant is the core correctness argument: after each iteration, the active window is valid and maximal for that `right` under the chosen `left`.

## 📊 Worked Example
Example: `lateFees = [0, 1, 1, 0, 2, 1]`, `B = 3`

| right | fee | action | left | currentSum | window | best |
|---|---:|---|---:|---:|---|---:|
| 0 | 0 | add 0 | 0 | 0 | `[0]` | 1 |
| 1 | 1 | add 1 | 0 | 1 | `[0,1]` | 2 |
| 2 | 1 | add 1 | 0 | 2 | `[0,1,1]` | 3 |
| 3 | 0 | add 0 | 0 | 2 | `[0,1,1,0]` | 4 |
| 4 | 2 | add 2, over budget → shrink | 2 | 3 | `[1,0,2]` | 4 |
| 5 | 1 | add 1, over budget → shrink | 3 | 3 | `[0,2,1]` | 4 |

The longest valid window length is `4`. One such window is indices `0..3`, with fees `[0,1,1,0]` and total `2`. The trace shows the key behavior: expansion is optimistic, shrinking is corrective, and both pointers move only forward.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed at most twice: once when `right` includes it, and once when `left` removes it. There is no nested reprocessing of the same range. At `10^6` elements this is practical in a single pass; at `10^9`, the algorithmic shape is still optimal, but runtime becomes bounded by raw I/O and system throughput.

### Space Complexity
`O(1)`. The algorithm stores only a few scalar variables: two pointers, a running sum, and the best length. No auxiliary arrays, prefix tables, or heaps are required. Space cannot be meaningfully reduced further without sacrificing readability rather than asymptotic cost.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a threshold, start by testing whether sliding window applies.
- The strongest signal for this pattern is a range-sum constraint with **non-negative** values; monotonicity makes one-pass shrinking valid.
- Update the answer only **after** shrinking until `currentSum <= B`; doing it earlier records invalid windows.
- Be careful with window length calculation: for inclusive bounds, it is `right - left + 1`, not `right - left`.
- In production systems, non-negative monotone constraints are a gift: they let you replace rescans and index-heavy designs with streaming, constant-space logic.

## 🚀 Variations & Further Practice
- **Allow negative fees**: standard sliding window breaks because shrinking is no longer monotonic; this pushes you toward prefix sums plus ordered data structures or binary search.
- **Count how many subarrays have sum `<= B`**: same window mechanics, but the output becomes an aggregate count rather than a max length.
- **Minimum-length subarray with sum `>= target`**: same two-pointer pattern, but the optimization direction flips and the shrink condition changes.