# Count Rescue Boat Pairs Within Safe Weight Range

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** two-pointers, sorting, array

---

## 🗂 Problem Overview
Given an array of passenger weights, count how many index pairs `(i, j)` with `i < j` have a combined weight within `[lowLimit, highLimit]`. You are not constructing an actual boat assignment; you are counting every valid possible pairing. The challenge is scale: with up to `2 * 10^5` passengers, brute-force pair enumeration is too slow, so the solution must avoid `O(n^2)` comparisons and instead exploit ordering plus range counting.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count valid combinations under bounded constraints without materializing all pairs. Examples include ad auction candidate matching under budget bands, fraud detection over transaction pairs within risk thresholds, search/ranking pipelines counting document-score pairs in acceptable ranges, and capacity planning over host-resource combinations. At scale, naive pair scans collapse under quadratic growth and become operationally irrelevant. Sorting plus monotonic pointer movement turns an intractable combinatorial scan into a predictable batch primitive, enabling offline analytics, precomputation jobs, and latency-safe services over large datasets.

## 🔍 Problem Statement
You are given an integer array `weights`, where `weights[i]` is the weight of passenger `i`, and two integers `lowLimit` and `highLimit`. Count the number of distinct index pairs `(i, j)` such that `i < j` and:

- `lowLimit <= weights[i] + weights[j] <= highLimit`

Distinctness is by index, not by value. If multiple passengers share the same weight, each valid index combination counts separately.

Constraints:

- `2 <= weights.length <= 2 * 10^5`
- `1 <= weights[i] <= 10^9`
- `1 <= lowLimit <= highLimit <= 2 * 10^9`

Examples:

- `weights = [2, 3, 5, 6, 8], lowLimit = 7, highLimit = 10` → `5`
- `weights = [1, 1, 4, 4, 7], lowLimit = 5, highLimit = 8` → `7`

The decisive constraint is input size: `O(n^2)` pair checking is not viable, so the solution must leverage sorting and linear-time counting after sort.

## 🪜 How to Solve This
1. Start from the brute-force definition: every pair is valid if its sum falls inside a closed interval. That immediately suggests a range-counting problem over pair sums.

2. Counting directly inside `[lowLimit, highLimit]` is awkward. A cleaner decomposition is:
   - count pairs with sum `<= highLimit`
   - count pairs with sum `< lowLimit`
   - subtract the second from the first

3. Now the problem becomes: how do we count pairs with sum `<= X` faster than quadratic?  
   Sort the weights. Once sorted, if `weights[left] + weights[right] <= X`, then every index between `left+1` and `right` also forms a valid pair with `left`.

4. That observation gives the key optimization: instead of checking each pair individually, count whole blocks at once with two pointers.

5. Move `left` and `right` monotonically:
   - sum too large → decrease `right`
   - sum valid → add `right - left`, then increase `left`

6. Run that helper twice, once for `highLimit` and once for `lowLimit - 1`. The difference is exactly the number of sums inside the target interval.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This enables monotonic reasoning about pair sums. After sorting, increasing the left pointer makes sums larger, while decreasing the right pointer makes sums smaller. Without ordering, no linear scan invariant exists.

2. **Define a helper `countAtMost(limit)`.**  
   It returns the number of pairs `(i, j)` with `i < j` and `weights[i] + weights[j] <= limit`. This is the core reusable primitive.

3. **Initialize two pointers: `left = 0`, `right = n - 1`.**  
   Maintain the invariant that all pairs already counted satisfy the limit, and all discarded pairs are known to exceed it.

4. **Evaluate `weights[left] + weights[right]`.**
   - If the sum is `<= limit`, then every pair `(left, k)` for `k in [left+1, right]` is also `<= limit` because the array is sorted and `weights[k] <= weights[right]`.  
     Add `right - left` to the answer and increment `left`.
   - If the sum is `> limit`, decrement `right` because the current heaviest partner is too large for `left`, and no pair using that `right` with this `left` can work.

5. **Repeat until `left >= right`.**  
   Each pointer moves only inward, so the scan is linear after sorting.

6. **Compute the final answer as:**  
   `countAtMost(highLimit) - countAtMost(lowLimit - 1)`

This is a classic **Two Pointers** pattern: exploit sorted order plus monotonic movement to count a large search space without enumerating it. The abstraction fits because pair validity depends only on the ordered sum relation, not on arbitrary pair-specific state.

## 📊 Worked Example
Use `weights = [2, 3, 5, 6, 8]`, `lowLimit = 7`, `highLimit = 10`.

We compute:

- pairs with sum `<= 10`
- pairs with sum `<= 6`
- subtract

Sorted array is already `[2, 3, 5, 6, 8]`.

| limit | left | right | sum | action | added | total |
|---|---:|---:|---:|---|---:|---:|
| 10 | 0 | 4 | 10 | valid, count `(0,1..4)` | 4 | 4 |
| 10 | 1 | 4 | 11 | too large, `right--` | 0 | 4 |
| 10 | 1 | 3 | 9 | valid, count `(1,2..3)` | 2 | 6 |
| 10 | 2 | 3 | 11 | too large, `right--` | 0 | 6 |

So `countAtMost(10) = 6`.

For `limit = 6`:

- `(2,3)=5` is valid → add `1`
- all other candidate sums exceed `6`

So `countAtMost(6) = 1`.

Final answer: `6 - 1 = 5`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`, and each `countAtMost` pass is `O(n)` because both pointers move monotonically and never reset. Total complexity is `O(n log n)`. At `10^6` elements this is still practical in optimized environments; at `10^9`, even sorting is beyond single-node memory and runtime budgets.

### Space Complexity
If sorting in place is allowed, auxiliary space is `O(1)` beyond the sort implementation. In languages whose standard sort allocates buffers, practical space may be `O(log n)` or higher. You can avoid extra structures entirely; the main trade-off is mutating input order.

## 💡 Key Takeaways
- If the task asks for the **number of pairs** satisfying a sum inequality over a large array, sorted two-pointer counting should be one of the first candidate patterns.
- Closed interval constraints like `[low, high]` often become simpler when rewritten as `count(<= high) - count(< low)`.
- Use a 64-bit accumulator for the answer; the number of valid pairs can exceed 32-bit range even when individual weights fit in `int`.
- Be precise about inclusivity: `lowLimit` is inclusive, so subtract `countAtMost(lowLimit - 1)`, not `countAtMost(lowLimit)`.
- The transferable design insight is to convert expensive exact-range enumeration into two monotonic prefix counts, then compose them algebraically.

## 🚀 Variations & Further Practice
- **Count pairs with absolute difference in a range.** Same sort-plus-count structure, but the monotonic condition is on `|a[i] - a[j]|`, which changes pointer movement and counting logic.
- **Count triplets with sum below a threshold.** Extends the same pattern by fixing one index and running two pointers on the suffix; harder because the outer loop reintroduces `O(n^2)` behavior.
- **Return all valid pairs instead of just the count.** The conceptual twist is output sensitivity: counting is cheap, but materializing pairs can legitimately require `Θ(k)` or `Θ(n^2)` output size.