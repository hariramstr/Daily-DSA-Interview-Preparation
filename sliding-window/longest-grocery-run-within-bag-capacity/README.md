# Longest Grocery Run Within Bag Capacity

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding-window, two-pointers, array

---

## 🗂 Problem Overview
Given an array `weights` and an integer `capacity`, find the maximum length of any contiguous subarray whose sum is at most `capacity`. The output is a single integer: the longest valid run of consecutive items. The non-trivial constraint is scale: `weights.length` can reach `100000`, so checking every subarray is too slow. The key property is that all weights are non-negative, which makes a linear-time sliding window possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must maximize contiguous throughput under a hard budget. Examples include streaming pipelines batching records under byte-size limits, distributed rate-limiters admitting the longest burst under token capacity, log shippers packing events into transport frames, and storage engines grouping adjacent pages under I/O thresholds. Without a sliding-window approach, implementations degrade into quadratic scans or over-buffering, which collapses under high-volume streams. The pattern enables single-pass decisions, bounded memory, predictable latency, and straightforward online processing where data arrives in order and must be handled incrementally.

## 🔍 Problem Statement
You are given:

- `weights`, where `weights[i]` is the weight of the `i`-th grocery item picked up in order
- `capacity`, the maximum total weight allowed in the bag

Return the length of the longest contiguous sequence of items whose total weight is less than or equal to `capacity`.

Constraints:

- `1 <= weights.length <= 100000`
- `0 <= weights[i] <= 10000`
- `0 <= capacity <= 1000000000`

Examples:

```text
Input:  weights = [2, 1, 3, 2, 1], capacity = 5
Output: 2
Explanation: Longest valid runs include [2,1], [3,2], and [2,1].
```

```text
Input:  weights = [1, 1, 1, 1, 2], capacity = 4
Output: 4
Explanation: [1,1,1,1] fits exactly; any longer run exceeds capacity.
```

The algorithmic choice is driven by one constraint: all values are non-negative. That means expanding the window can only increase or preserve the sum, and shrinking it can only decrease or preserve the sum.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is not subset sum or knapsack. Order matters, and we are choosing a subarray.

2. Notice the objective → maximize window length while keeping `sum <= capacity`. That is the standard shape of a variable-size sliding window problem.

3. Ask what makes sliding window valid → all `weights[i]` are non-negative. If the current window is too heavy, moving the left edge right is the only useful correction because adding more items on the right cannot reduce the sum.

4. Maintain a running window:
   - expand right one item at a time
   - add its weight to the running sum
   - while the sum exceeds `capacity`, shrink from the left

5. After the window becomes valid again, compute its length and update the best answer.

6. Why this works → each index enters the window once and leaves once. No backtracking, no nested rescans, no prefix-sum search structure needed.

This is the simplest possible linear scan because the input monotonicity property does most of the work for us.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and state**  
   Set `left = 0`, `currentSum = 0`, and `maxLen = 0`.  
   The window is always the inclusive range `[left, right]`.  
   Invariant: before recording `maxLen`, the window must satisfy `currentSum <= capacity`.

2. **Expand the window with the right pointer**  
   For each `right` from `0` to `n - 1`, add `weights[right]` to `currentSum`.  
   This represents trying to include the next item in the contiguous run.  
   Pattern: **Two Pointers / Sliding Window**. It is appropriate because the window boundaries move only forward.

3. **Repair invalid windows by shrinking from the left**  
   While `currentSum > capacity`, subtract `weights[left]` and increment `left`.  
   This is correct because all weights are non-negative: removing items from the left is guaranteed not to increase the sum, and there is no reason to move `right` backward.

4. **Record the best valid length**  
   Once the while-loop ends, the window is valid again. Compute `right - left + 1` and update `maxLen`.  
   Invariant maintained: after adjustment, `[left, right]` is the longest valid window ending at `right`, because any earlier left boundary would make the sum exceed capacity.

5. **Finish after one pass**  
   Return `maxLen`.  
   Correctness follows from exhaustive coverage of all possible right endpoints and the maintained minimal valid left boundary for each one.

## 📊 Worked Example
Example: `weights = [2, 1, 3, 2, 1]`, `capacity = 5`

| right | weights[right] | currentSum after add | shrink? | left after shrink | valid window | length | maxLen |
|------:|----------------:|---------------------:|:-------:|------------------:|:------------|-------:|-------:|
| 0 | 2 | 2 | No | 0 | `[2]` | 1 | 1 |
| 1 | 1 | 3 | No | 0 | `[2,1]` | 2 | 2 |
| 2 | 3 | 6 | Yes: remove 2 | 1 | `[1,3]` | 2 | 2 |
| 3 | 2 | 6 | Yes: remove 1, then 3 | 3 | `[2]` | 1 | 2 |
| 4 | 1 | 3 | No | 3 | `[2,1]` | 2 | 2 |

The key observation is that `left` never moves backward. Once a prefix makes the window too heavy, that prefix can never help any future window ending farther right.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is added to `currentSum` once when `right` advances and removed at most once when `left` advances. The dominant work is pointer movement across the array, not nested iteration. At `10^6` elements this is routine; at `10^9`, the algorithm is still linear but input bandwidth becomes the real bottleneck.

### Space Complexity
`O(1)`. The algorithm stores only a few scalar variables: two pointers, a running sum, and the best length. No auxiliary array or map is required. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a threshold and all values are non-negative, sliding window should be your first instinct.
- When expanding a window can only increase the cost metric and shrinking can only decrease it, two pointers usually beats prefix-sum-plus-search in both simplicity and constants.
- Update `maxLen` only **after** shrinking until the window is valid; doing it earlier records illegal windows.
- Be precise about window length: for an inclusive window `[left, right]`, the size is `right - left + 1`, not `right - left`.
- In production systems, monotonic constraints are architectural leverage: they turn expensive global recomputation into cheap incremental maintenance.

## 🚀 Variations & Further Practice
- **Longest subarray with sum exactly equal to `k`** — harder because sliding window no longer works reliably when exact matching matters with arbitrary values; prefix sums plus hashing becomes the right abstraction.
- **Shortest subarray with sum at least `k`** — same domain, different objective; with non-negative values sliding window works, but with negative values it requires prefix sums and a monotonic deque.
- **Maximum consecutive events under dual constraints** — e.g. total bytes and total CPU cost must both stay under limits; the conceptual twist is maintaining validity across multiple resource dimensions.