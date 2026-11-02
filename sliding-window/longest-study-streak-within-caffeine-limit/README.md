# Longest Study Streak Within Caffeine Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `cups` of non-negative integers and a safety threshold `limit`, find the maximum length of any contiguous subarray whose sum is at most `limit`. The output is a single integer: the longest valid run of consecutive study sessions. The problem is non-trivial because brute force checks all subarrays in quadratic time, which is too slow for arrays up to `100000` elements. The non-negative input constraint is what makes a linear sliding-window solution possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest valid contiguous interval under a cumulative budget: API rate-limit windows, streaming ingestion bounded by memory or byte quotas, mobile telemetry batching under payload caps, and ad-serving session analysis under spend thresholds. At scale, brute-force interval evaluation collapses under high-cardinality streams and long retention windows. The sliding-window formulation turns repeated recomputation into incremental maintenance, which is the difference between per-request latency stability and pathological tail behavior. More broadly, it enables online processing: you can update state as events arrive rather than materializing and rescanning historical slices.

## 🔍 Problem Statement
You are given an integer array `cups` where `cups[i]` is the caffeine consumed in the `i`-th study session, and an integer `limit` representing the maximum safe total caffeine across a consecutive stretch of sessions. Return the length of the longest contiguous subarray whose sum is less than or equal to `limit`.

Constraints:
- `1 <= cups.length <= 100000`
- `0 <= cups[i] <= 10000`
- `0 <= limit <= 1000000000`

If no individual session fits within the limit, return `0`.

Examples:
- `cups = [2, 1, 3, 2, 1]`, `limit = 5` → `2`
- `cups = [0, 1, 1, 0, 2]`, `limit = 2` → `4`

The key algorithmic constraint is that all values are non-negative. That means when a window sum becomes too large, moving the left boundary rightward can only decrease or preserve the sum, so a two-pointer sliding window is sufficient in `O(n)` time.

## 🪜 How to Solve This
1. Read the requirement carefully → we need the **longest contiguous** segment, not any subset and not a sorted rearrangement. That immediately suggests interval reasoning rather than combinatorics.

2. Notice the objective is constrained by a running sum: total caffeine in the current segment must stay `<= limit`. Recomputing sums for every possible interval would be too expensive.

3. Check the data property that matters most: all `cups[i]` are non-negative. That is the signal for a sliding window. Why? Because extending the window to the right never decreases the sum, and shrinking from the left never increases it.

4. Maintain a current window `[left, right]` and its sum. Expand `right` one step at a time. If the sum exceeds `limit`, advance `left` until the window becomes valid again.

5. After each adjustment, the window is the longest valid window ending at `right`, so its length is a candidate answer.

6. This works in one pass because each index enters the window once and leaves once. No backtracking, no nested rescans, no prefix-sum search structure needed.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `windowSum = 0`, and `best = 0`.  
   This defines an empty sliding window. The invariant is: before processing each new `right`, `windowSum` equals the sum of `cups[left..right-1]`.

2. **Expand the window to the right**  
   For each index `right` from `0` to `n - 1`, add `cups[right]` to `windowSum`.  
   This is the standard **Two Pointers / Sliding Window** expansion step. We tentatively include the new session and then check validity.

3. **Restore validity when over budget**  
   While `windowSum > limit`, subtract `cups[left]` from `windowSum` and increment `left`.  
   This step is correct because all values are non-negative. Once the sum is too large, the only way to make the current contiguous window valid is to discard items from the left. There is no benefit in keeping an earlier left boundary.

4. **Record the current valid window**  
   After the while-loop, `windowSum <= limit`, so `[left, right]` is valid. Update `best = max(best, right - left + 1)`.  
   The invariant now is: the current window is valid, and `best` is the maximum valid length seen so far.

5. **Why this abstraction fits**  
   Sliding window is the right model because the validity predicate depends on a cumulative metric over a contiguous range, and the non-negative constraint guarantees monotonic behavior under pointer movement. Without that monotonicity, shrinking the left side would not reliably restore feasibility, and this approach would fail.

## 📊 Worked Example
Take `cups = [0, 1, 1, 0, 2]`, `limit = 2`.

| right | cups[right] | windowSum after add | left after shrink | valid window     | length | best |
|------:|------------:|--------------------:|------------------:|------------------|-------:|-----:|
| 0     | 0           | 0                   | 0                 | `[0]`            | 1      | 1    |
| 1     | 1           | 1                   | 0                 | `[0,1]`          | 2      | 2    |
| 2     | 1           | 2                   | 0                 | `[0,1,1]`        | 3      | 3    |
| 3     | 0           | 2                   | 0                 | `[0,1,1,0]`      | 4      | 4    |
| 4     | 2           | 4                   | 3                 | `[0,2]`          | 2      | 4    |

At `right = 4`, adding `2` breaks the limit. Shrink from the left: remove `0`, then `1`, then `1`, leaving sum `2` and `left = 3`. The best answer remains `4`, from window `[0,1,1,0]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is added to the window once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing despite the inner `while` loop. At `10^6` elements this is practical in a single pass; at `10^9`, runtime becomes dominated by raw scan cost and I/O bandwidth rather than algorithmic overhead.

### Space Complexity
`O(1)`. The algorithm stores only scalar state: two pointers, a running sum, and the best length. No auxiliary array or heap is required. Space cannot be meaningfully reduced further without losing the ability to track the current window sum incrementally.

## 💡 Key Takeaways
- If the problem asks for a longest or shortest **contiguous** range under a sum constraint, sliding window should be one of the first patterns you test.
- The decisive signal here is **non-negative values**; that monotonicity is what makes two pointers correct and linear.
- Update `best` only **after** shrinking the window back to validity; doing it earlier records illegal windows.
- Be careful with the returned length formula: for a valid inclusive window, it is `right - left + 1`, not `right - left`.
- In production systems, incremental window maintenance is often the difference between online, bounded-cost processing and repeated full-range recomputation.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: harder because the optimization direction flips; with arbitrary values, this typically needs prefix sums plus a monotonic deque.
- **Longest subarray with sum exactly `K`**: harder when negatives are allowed; sliding window breaks, so prefix sums with a hash map become the right tool.
- **At most `K` distinct elements in a window**: same two-pointer skeleton, but validity depends on frequency state rather than a numeric sum, introducing map maintenance and eviction logic.