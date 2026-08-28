# Find Insertion Slot for a Sorted Event Timeline

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Lower Bound

---

## 🗂 Problem Overview
Given a sorted array `times` of event start times and a `target` start time, return the index where `target` should be inserted to keep the array sorted. If `target` already exists, return its leftmost occurrence. Formally, find the first index `i` where `times[i] >= target`, or `times.length` if none exists. The non-trivial constraint is the required `O(log n)` runtime, which rules out linear scanning for large inputs.

## 🌍 Engineering Impact
This is the lower-bound lookup primitive behind production systems that maintain ordered state: time-series ingestion buffers, streaming schedulers, compiler symbol tables, search indexes, and distributed rate-limit windows. At scale, the difference between `O(n)` and `O(log n)` is the difference between predictable latency and tail amplification under load. Without this pattern, every insertion-point query degrades with dataset size, forcing premature sharding or caching. With it, ordered collections remain operationally viable, enabling efficient range queries, deduplication boundaries, and deterministic placement in append-heavy or read-heavy systems.

## 🔍 Problem Statement
You are given a sorted integer array `times`, where each value is an event start time in minutes from the beginning of the day. The array is sorted in non-decreasing order, so duplicates are allowed. You are also given an integer `target`, representing a new event start time.

Return the index where `target` should be inserted so the array remains sorted. If `target` already appears, return the first index where it appears. Equivalently, compute the lower bound: the first position `i` such that `times[i] >= target`. If every value is smaller than `target`, return `times.length`.

Constraints:
- `0 <= times.length <= 100000`
- `0 <= times[i] <= 1440`
- `times` is sorted in non-decreasing order
- `0 <= target <= 1440`

Examples:
- `times = [15, 30, 30, 45, 90], target = 30` → `1`
- `times = [10, 20, 40, 80], target = 35` → `2`

The key constraint is the required `O(log n)` runtime, which makes binary search the correct approach.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find exact match or fail.” It asks for the first valid insertion position, even when the value is missing.
2. Notice the array is already sorted → sorted input plus `O(log n)` almost always points to binary search.
3. Translate the condition into a boundary search → we want the first index where `times[i] >= target`, not just any occurrence of `target`.
4. That means we are searching a monotonic predicate: for smaller indices the condition may be false, and once it becomes true, it stays true.
5. Use two pointers, `left` and `right`, to maintain the current search interval. Each midpoint tells us whether the insertion slot is at `mid` or to its right.
6. If `times[mid] >= target`, keep `mid` in play by moving `right = mid`; it could still be the leftmost valid answer.
7. Otherwise, discard the left half with `left = mid + 1`.
8. When `left == right`, the boundary is found. That index is the insertion slot, including the empty-array and “insert at end” cases.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Binary Search for Lower Bound.**  
   This is not a membership test; it is a boundary-finding problem over a sorted array. The right abstraction is **lower bound**: first index where the value is greater than or equal to `target`.

2. **Initialize the search interval.**  
   Set `left = 0` and `right = times.length`. Using a half-open interval `[left, right)` is deliberate: it naturally supports returning `times.length` when the target belongs at the end.

3. **Maintain the invariant.**  
   At every step:
   - all indices `< left` are known to contain values `< target`
   - all indices `>= right` are known to be valid insertion candidates  
   This invariant ensures the answer always remains inside `[left, right)`.

4. **Pick the midpoint.**  
   Compute `mid = left + (right - left) / 2`. This avoids overflow in languages where `left + right` may exceed integer bounds.

5. **Shrink toward the first valid position.**  
   If `times[mid] >= target`, then `mid` could be the answer, so move `right = mid`.  
   Otherwise, `times[mid] < target`, so the answer must be strictly to the right; move `left = mid + 1`.

6. **Terminate at the boundary.**  
   The loop ends when `left == right`. By the invariant, this index is the first position where `times[i] >= target`, or `times.length` if no such index exists.

## 📊 Worked Example
Example: `times = [15, 30, 30, 45, 90]`, `target = 30`

| Step | left | right | mid | times[mid] | Decision |
|---|---:|---:|---:|---:|---|
| 1 | 0 | 5 | 2 | 30 | `30 >= 30`, keep mid, `right = 2` |
| 2 | 0 | 2 | 1 | 30 | `30 >= 30`, keep mid, `right = 1` |
| 3 | 0 | 1 | 0 | 15 | `15 < 30`, discard left half, `left = 1` |
| End | 1 | 1 | — | — | stop; answer is `1` |

The trace shows why this is a lower-bound search rather than a standard equality search. The algorithm does not stop when it first sees `30`; it keeps moving left until it proves there is no earlier valid position. That is exactly what preserves correctness in the presence of duplicates.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration halves the remaining search interval, so the dominant operation is the binary search loop. At `10^6` elements, this is roughly 20 comparisons; at `10^9`, roughly 30. That logarithmic growth is why ordered lookup remains practical at large scale.

### Space Complexity
`O(1)`. The algorithm uses only a fixed number of variables: `left`, `right`, and `mid`. No auxiliary data structure is allocated. Space cannot meaningfully be reduced further without changing the execution model; the trade-off is already optimal for in-memory search.

## 💡 Key Takeaways
- If the input is sorted and the requirement says “first position where condition becomes true,” think lower-bound binary search immediately.
- If duplicates exist and the answer must be the leftmost valid index, a standard exact-match binary search is insufficient.
- Use a half-open interval `[left, right)` to make “insert at end” fall out naturally without special-case logic.
- When `times[mid] >= target`, do **not** move to `mid - 1`; keep `mid` by setting `right = mid`, or you can skip the correct leftmost answer.
- In production code, lower-bound search is a reusable primitive for deterministic placement in ordered structures, not just an interview trick.

## 🚀 Variations & Further Practice
- **Upper bound / rightmost insertion point:** return the first index where `times[i] > target`; the twist is handling duplicates on the opposite boundary.
- **Search in rotated sorted array:** binary search still applies, but the monotonic structure is partially broken and must be re-established each step.
- **First bad version / monotonic predicate search:** generalizes from arrays to any ordered boolean boundary, where the challenge is identifying and preserving the invariant.