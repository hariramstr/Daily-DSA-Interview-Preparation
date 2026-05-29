# First Spoiled Item on Sorted Shelf

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Two Pointers

---

## 🗂 Problem Overview

Given a sorted array of integers representing item expiration dates (days from today), find the index of the first element strictly greater than 0. Items with expiration ≤ 0 are spoiled. Return -1 if all items are spoiled, 0 if none are. The non-trivial constraint: you must do this in **O(log n)**, ruling out a simple left-to-right scan and requiring binary search on a sorted monotonic predicate boundary.

---

## 🌍 Engineering Impact

This pattern — finding the first element satisfying a monotonic predicate in a sorted sequence — is pervasive at scale. Time-series databases (InfluxDB, TimescaleDB) use it to locate the earliest valid record in a retention window. Rate limiters binary-search sorted timestamp rings to find the first non-expired token. Log compaction systems in Kafka-style brokers identify the first offset past a retention boundary. Without O(log n) here, a linear scan across 10⁸ records per query collapses throughput; binary search keeps latency flat as data grows.

---

## 🔍 Problem Statement

**Input:** A non-decreasing integer array `expirations` where each value represents days until expiration. Values ≤ 0 mean the item is already spoiled.

**Output:** The index of the first element with value > 0. Return 0 if no items are spoiled; return -1 if all are spoiled.

**Constraints:**
- `1 <= expirations.length <= 10^5`
- `-10^4 <= expirations[i] <= 10^4`
- Array is sorted in non-decreasing order
- Required time complexity: **O(log n)**

**Examples:**

| Input | Output | Reason |
|---|---|---|
| `[-5, -3, -1, 0, 2, 4, 7]` | `4` | First value > 0 is `2` at index 4 |
| `[1, 3, 5, 8]` | `0` | All values > 0; boundary is at index 0 |
| `[-4, -2, 0]` | `-1` | All values ≤ 0; no valid item exists |

The O(log n) requirement is the forcing function — it eliminates linear scan and mandates binary search.

---

## 🪜 How to Solve This

1. **Read the constraint** → O(log n) on a sorted array. That's binary search, full stop. No other algorithm achieves this.

2. **Reframe the question** → You're not searching for a specific value; you're searching for a **boundary** — the transition point where elements flip from ≤ 0 to > 0. This is the classic "find first true" binary search variant.

3. **Model the predicate** → Define `f(i) = expirations[i] > 0`. Because the array is sorted, once `f(i)` is true, it stays true for all `j > i`. The predicate is monotonic: `[false, false, ..., true, true, ...]`. Binary search finds the leftmost `true`.

4. **Handle the edge cases first** → If `expirations[0] > 0`, return 0 immediately. If `expirations[last] <= 0`, return -1. These O(1) checks eliminate the degenerate cases before the search loop runs.

5. **Recognize the result** → The answer is the leftmost index where the predicate holds — a standard lower-bound search. The implementation follows directly from the mental model.

---

## 🧩 Algorithm Walkthrough

**Pattern: Binary Search on a Monotonic Predicate (Lower Bound)**

This is the "find first true" template — not a value search, but a boundary search. The sorted order guarantees the predicate `expirations[i] > 0` is monotonically non-decreasing (false then true), which is the necessary condition for binary search correctness.

**Steps:**

1. **Edge case — no spoiled items:** If `expirations[0] > 0`, every item is fresh. Return 0.

2. **Edge case — all spoiled:** If `expirations[n-1] <= 0`, no fresh item exists. Return -1.

3. **Initialize pointers:** Set `left = 0`, `right = n - 1`, `result = -1`.

4. **Binary search loop:** While `left <= right`:
   - Compute `mid = left + (right - left) / 2` (avoids integer overflow).
   - If `expirations[mid] > 0`: this index is a candidate. Record `result = mid`, then search left (`right = mid - 1`) to find an earlier valid index.
   - If `expirations[mid] <= 0`: the boundary is to the right. Set `left = mid + 1`.

5. **Invariant maintained:** At every step, `result` holds the leftmost valid index seen so far. The search space halves each iteration.

6. **Return `result`:** Contains the leftmost index where `expirations[i] > 0`, or -1 if none found.

---

## 📊 Worked Example

Input: `expirations = [-5, -3, -1, 0, 2, 4, 7]` → Expected output: `4`

| Iteration | `left` | `right` | `mid` | `expirations[mid]` | `> 0?` | Action | `result` |
|---|---|---|---|---|---|---|---|
| Initial | 0 | 6 | — | — | — | Edge checks pass | -1 |
| 1 | 0 | 6 | 3 | 0 | No | `left = 4` | -1 |
| 2 | 4 | 6 | 5 | 4 | Yes | `result = 5`, `right = 4` | 5 |
| 3 | 4 | 4 | 4 | 2 | Yes | `result = 4`, `right = 3` | 4 |
| Exit | 4 | 3 | — | — | — | `left > right` | **4** |

Each time we find a valid index, we record it and keep searching left to find an earlier one. The loop terminates with the leftmost valid index.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(log n).** Each iteration halves the search space; the loop executes at most ⌈log₂ n⌉ times. At 10⁶ elements: ~20 iterations. At 10⁹ elements: ~30 iterations. This is the difference between sub-millisecond and multi-second latency at data warehouse scale.

### Space Complexity

**O(1).** Only three integer variables (`left`, `right`, `result`, `mid`) are allocated regardless of input size. No auxiliary data structures. Space cannot be reduced further — this is already optimal.

---

## 💡 Key Takeaways

- **Pattern signal — sorted array + O(log n) requirement:** Any time you see these two together, binary search is the answer. The question is only which variant: exact match, lower bound, or upper bound.
- **Pattern signal — monotonic predicate over an index range:** When you can express the problem as "find the first index where condition flips from false to true," you have a lower-bound binary search, not a value search.
- **Gotcha — off-by-one on the boundary condition:** The predicate here is strictly `> 0`, not `>= 0`. Items expiring exactly today (value = 0) are spoiled. Confusing `>` with `>=` shifts the boundary by one and produces wrong results on inputs containing zeros.
- **Gotcha — mid computation:** Always use `mid = left + (right - left) / 2`, not `(left + right) / 2`. The latter overflows when both pointers are near `INT_MAX` — a real bug in production systems handling large offset ranges.
- **Architectural insight:** Encode the predicate as a pure function separate from the binary search scaffold. This makes the search logic reusable and the business rule (what "spoiled" means) independently testable — critical when retention policies change without notice.

---

## 🚀 Variations & Further Practice

- **Find the last spoiled item (upper bound):** Instead of the first index where `f(i)` is true, find the last index where `f(i)` is false. The twist: you must flip the search direction and track a `result` that updates when the predicate is false, not true. Forces you to internalize both lower-bound and upper-bound templates.
- **Rotated sorted array with spoilage threshold:** Apply the same predicate search on an array that has been rotated at an unknown pivot (LeetCode 33/81 variant). The twist: sorted order is broken globally but preserved locally, requiring a two-phase approach — first find the rotation point, then binary search the correct half.
- **2D shelf (matrix) with row-sorted expirations:** Extend to an `m × n` matrix where each row is sorted. Find the first non-spoiled item across the entire matrix in O(m log n). The twist: you must decide whether to binary search each row independently or exploit inter-row monotonicity for a smarter traversal.