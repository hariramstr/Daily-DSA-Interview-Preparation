# Count Shelf Pairs With Exact Width Sum

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an unsorted integer array `widths` and an integer `targetWidth`, count how many distinct index pairs `(i, j)` with `i < j` satisfy `widths[i] + widths[j] == targetWidth`. The challenge is not detecting one matching pair, but counting all of them efficiently when duplicates are common. A naive nested-loop scan is `O(n^2)`, which is infeasible at `2 * 10^5` elements, so the solution must exploit ordering and duplicate aggregation.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count exact pairwise matches under tight latency or memory budgets: warehouse slotting, ad auction bid matching, fraud detection on transaction deltas, search-ranking feature joins, and telemetry pipelines looking for complementary values. At scale, brute-force pair enumeration explodes quadratically and becomes both a CPU and cost problem. Sorting plus two pointers converts an all-pairs search into a linear scan over ordered data, which is often the difference between a batch job finishing on time and missing an SLA. Handling duplicates correctly matters because real production distributions are rarely uniform.

## 🔍 Problem Statement
You are given an integer array `widths` of length `1` to `2 * 10^5`, where each value may be negative, zero, or positive (`-10^9 <= widths[i] <= 10^9`). You are also given `targetWidth` in the range `[-2 * 10^9, 2 * 10^9]`. Return the number of index pairs `(i, j)` such that `i < j` and `widths[i] + widths[j] == targetWidth`.

Duplicates must be counted by index, not by value. If four shelves have width `2` and the target is `4`, the answer is `C(4, 2) = 6`.

Examples:

- `widths = [1, 5, 3, 3, 2, 4], targetWidth = 6` → `3`
- `widths = [2, 2, 2, 2, 3, 1], targetWidth = 4` → `6`

The key constraint is input size: `O(n^2)` is too slow, so the algorithm must be near `O(n log n)` or better.

## 🪜 How to Solve This
1. Read the problem → we need the **count of all valid pairs**, not the pairs themselves.
2. Notice the input is unsorted, but pair-sum problems become much easier once values are ordered.
3. Sorting suggests the **Two Pointers** pattern: one pointer at the smallest value, one at the largest.
4. After sorting, compare `widths[left] + widths[right]` to `targetWidth`:
   - too small → move `left` rightward
   - too large → move `right` leftward
   - exact match → count pairs
5. The non-obvious part is duplicates. If `widths[left]` appears multiple times and `widths[right]` also appears multiple times, one match may represent many index pairs.
6. So when a match is found, count the full duplicate runs on both sides:
   - different values → `leftCount * rightCount`
   - same value → all elements between pointers are identical, so add `n * (n - 1) / 2`
7. Then skip past the counted block and continue.

That gives an `O(n log n)` solution dominated by sorting, with a single linear pass afterward.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This enables monotonic reasoning: increasing `left` can only increase the sum, and decreasing `right` can only decrease it. That property is what makes **Two Pointers** the right abstraction instead of nested scans.

2. **Initialize `left = 0`, `right = n - 1`, `answer = 0`.**  
   The invariant is that all pairs strictly outside `[left, right]` have already been either counted or ruled out.

3. **Compute `sum = widths[left] + widths[right]`.**  
   - If `sum < targetWidth`, the current smallest value is too small even with the largest remaining partner, so no pair using `left` can work. Increment `left`.
   - If `sum > targetWidth`, the current largest value is too large even with the smallest remaining partner, so no pair using `right` can work. Decrement `right`.

4. **When `sum == targetWidth`, count duplicates carefully.**  
   - If `widths[left] != widths[right]`, count how many equal values are contiguous from `left` (`leftCount`) and from `right` (`rightCount`). Add `leftCount * rightCount`. This is correct because every occurrence on the left can pair with every occurrence on the right.
   - If `widths[left] == widths[right]`, then every element in `[left, right]` has the same value and every pair among them is valid. Let `k = right - left + 1`; add `k * (k - 1) / 2` and terminate.

5. **Advance pointers past counted groups.**  
   This preserves the invariant that no valid pair is counted twice and no candidate pair is skipped.

## 📊 Worked Example
Example: `widths = [1, 5, 3, 3, 2, 4]`, `targetWidth = 6`

Sorted: `[1, 2, 3, 3, 4, 5]`

| Step | left | right | Values | Sum | Action | Added | Total |
|---|---:|---:|---|---:|---|---:|---:|
| 1 | 0 | 5 | 1, 5 | 6 | Match, counts `1 x 1` | 1 | 1 |
| 2 | 1 | 4 | 2, 4 | 6 | Match, counts `1 x 1` | 1 | 2 |
| 3 | 2 | 3 | 3, 3 | 6 | Same value block of size 2 → `C(2,2)` | 1 | 3 |

Trace notes:
1. `(1,5)` contributes one pair.
2. `(2,4)` contributes one pair.
3. The remaining window contains two `3`s. Since both pointers point to the same value and `3 + 3 = 6`, all pairs inside that block are valid. With two elements, that is exactly one pair.

Final answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`, and the two-pointer scan is `O(n)` because each pointer moves inward monotonically and each element is processed at most once after sorting. At `10^6` elements this is still practical; at `10^9`, even linear scans are already operationally unrealistic, so asymptotic improvement beyond quadratic is necessary but not sufficient.

### Space Complexity
If sorting is done in place, auxiliary space is `O(1)` beyond the sort implementation’s internal needs; otherwise it is `O(n)` if a copied array is used. The dominant space owner is the sorted representation. You can reduce extra memory with in-place sort, at the cost of mutating input.

## 💡 Key Takeaways
- If the problem asks for pair sums in an unsorted array and only needs a count, sorting plus two pointers should be one of the first candidate patterns.
- When constraints rule out `O(n^2)` and values can repeat heavily, look for ways to aggregate duplicate runs instead of reasoning per element.
- The biggest correctness trap is undercounting duplicates: one matching value pair may represent many index pairs.
- The other common bug is mishandling the `left == right` value case; when both ends hold the same matching value, use combinations, not repeated pointer movement.
- At production scale, the transferable insight is to convert combinatorial search into ordered traversal with grouped counting; that changes both runtime behavior and system cost profile.

## 🚀 Variations & Further Practice
- Count pairs with sum **less than or equal to** a target. The twist is that one pointer move can validate an entire range, so counting becomes cumulative rather than exact-match based.
- Return the **actual unique value pairs** instead of the number of index pairs. The harder part is deduplication semantics: values versus indices.
- Extend to **3-sum counting**. The conceptual jump is nesting the two-pointer pattern inside an outer loop while still handling duplicates without double-counting.