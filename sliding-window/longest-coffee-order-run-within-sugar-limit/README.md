# Longest Coffee Order Run Within Sugar Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `sugars` of non-negative integers and an integer `limit`, find the maximum length of a contiguous subarray whose sum is at most `limit`. The input is the order stream; the output is a single integer: the longest valid run length. What makes this non-trivial is the need to optimize for up to `100000` elements without checking every possible subarray, which would degrade to quadratic time.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must maintain the longest recent span that satisfies a cumulative budget: API rate-limit windows, streaming ingestion bounded by byte or cost quotas, ad-serving sessions constrained by spend, and telemetry pipelines capped by memory or latency budgets. At scale, brute-force rescanning windows causes avoidable CPU amplification and cache-unfriendly behavior. A sliding window turns the problem into a single linear pass with stable memory, which matters in hot paths, online processing, and services that must compute answers incrementally as events arrive rather than batch-recompute from scratch.

## 🔍 Problem Statement
You are given an integer array `sugars` where `sugars[i]` is the sugar packets used in the `i`-th coffee order, and an integer `limit` representing the maximum total sugar allowed in a contiguous run.

Return the length of the longest subarray `sugars[left...right]` such that:

- `sum(sugars[left...right]) <= limit`

Constraints:

- `1 <= sugars.length <= 100000`
- `0 <= sugars[i] <= 10000`
- `0 <= limit <= 1000000000`

Examples:

- `sugars = [1, 2, 1, 1, 3], limit = 4` → `3`
- `sugars = [4, 1, 1, 1, 2], limit = 3` → `3`

Edge cases matter: a single order may exceed `limit`, zeros may appear, and `limit` itself may be zero. The key constraint driving the algorithm is that all values are non-negative. That property makes window expansion and contraction monotonic, enabling a linear-time sliding window instead of backtracking or prefix-sum search.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous segment** under a **sum constraint**. That combination should immediately suggest a window, not sorting or dynamic programming.

2. Notice the critical property → every `sugars[i]` is non-negative. That means when the right side expands, the window sum can only stay the same or increase. When the sum is too large, moving the left side rightward can only decrease or preserve it.

3. That monotonic behavior removes the need to reconsider old left boundaries from scratch. Each pointer only moves forward, which is the signature of a linear two-pointer solution.

4. Maintain a running sum for the current window `[left...right]`. For each new `right`, add `sugars[right]`.

5. If the sum exceeds `limit`, shrink from the left until the window becomes valid again. This restores the invariant that the current window always satisfies the constraint.

6. After revalidating, compute the current window length and update the best answer.

The key insight is not “find all valid subarrays.” It is “maintain the largest valid window seen so far while scanning once.”

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use the **Sliding Window / Two Pointers** pattern with:
   - `left = 0`
   - `windowSum = 0`
   - `maxLen = 0`  
   The abstraction fits because we need a contiguous region and the validity condition depends on an aggregate over that region.

2. **Expand the window with `right`**  
   Iterate `right` from `0` to `n - 1`. Add `sugars[right]` to `windowSum`.  
   Why correct: every candidate answer must end somewhere; scanning all possible right boundaries ensures coverage.

3. **Restore validity when over limit**  
   While `windowSum > limit`, subtract `sugars[left]` and increment `left`.  
   Why correct: because all values are non-negative, removing elements from the left is the only way to reduce the sum while preserving contiguity. No skipped window can become valid later without moving `left`.

4. **Maintain the invariant**  
   After the inner loop, the window `[left...right]` is valid: `windowSum <= limit`.  
   This invariant is central: at every outer-loop iteration, we know the current window is the longest valid window ending at `right`, because `left` is as far left as possible after restoring validity.

5. **Update the answer**  
   Compute `right - left + 1` and update `maxLen`.  
   Why correct: once the window is valid, its length is a legitimate candidate. Taking the maximum across all `right` values yields the global optimum.

6. **Return `maxLen`**  
   Each element enters the window once and leaves at most once, giving linear work overall.

## 📊 Worked Example
Example: `sugars = [1, 2, 1, 1, 3]`, `limit = 4`

| right | sugars[right] | action                         | left | windowSum | window        | maxLen |
|------:|---------------:|--------------------------------|-----:|----------:|---------------|-------:|
| 0     | 1              | add 1                          | 0    | 1         | [1]           | 1      |
| 1     | 2              | add 2                          | 0    | 3         | [1,2]         | 2      |
| 2     | 1              | add 1                          | 0    | 4         | [1,2,1]       | 3      |
| 3     | 1              | add 1, sum=5 > 4, remove left  | 1    | 4         | [2,1,1]       | 3      |
| 4     | 3              | add 3, sum=7 > 4, shrink twice | 3    | 4         | [1,3]         | 3      |

The longest valid windows are `[1,2,1]` and `[2,1,1]`, both length `3`. The important behavior is that `left` never moves backward; once an order is excluded, it never needs to be reconsidered.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = sugars.length`. The dominant work is pointer movement: `right` advances `n` times, and `left` also advances at most `n` times total. At `10^6` elements this remains practical in a single pass; at `10^9`, the algorithm is still asymptotically optimal but runtime becomes bounded by raw scan throughput and I/O.

### Space Complexity
`O(1)`. The algorithm stores only scalar state: two indices, a running sum, and the best length. No auxiliary array or heap is required. Space cannot be meaningfully reduced further unless input streaming replaces in-memory storage, which changes only data access, not algorithmic state.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous subarray** under a threshold and all values are **non-negative**, think sliding window immediately.
- When validity depends on an aggregate like `sum <= limit` and expanding the window changes that aggregate monotonically, two pointers usually beat prefix-sum enumeration.
- Update `maxLen` **after** shrinking, not before; otherwise you may record an invalid window.
- Be careful with window length calculation: for inclusive bounds, it is `right - left + 1`, not `right - left`.
- The production-level insight is that monotonic constraints let you replace repeated recomputation with incremental maintenance, which is exactly how efficient online systems stay linear under load.

## 🚀 Variations & Further Practice
- **Longest subarray with sum exactly `k`**: harder because shrinking greedily no longer works; typically solved with prefix sums plus a hash map.
- **Minimum size subarray sum at least `target`**: same sliding-window core, but the optimization goal flips from maximizing length to minimizing it.
- **Longest subarray with at most `k` distinct values**: still a window, but validity depends on frequency state rather than a simple numeric sum, requiring a hash map and more careful invariant management.