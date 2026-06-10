# First Store Open at or After Query Time

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, arrays, search

---

## 🗂 Problem Overview
Given a sorted array `openTimes` and a `queryTime`, return the index of the first element whose value is greater than or equal to the query. If every opening time is smaller than the query, return `-1`. The non-trivial part is not checking existence, but finding the **leftmost** valid index efficiently, even when duplicates exist. Because the array is already sorted and can contain up to `10^5` elements, binary search is the intended logarithmic solution.

## 🌍 Engineering Impact
This is the same access pattern used in scheduling systems, calendar backends, ad-serving windows, time-series retention lookups, and search infrastructure that needs the first record at or beyond a threshold. In production, the difference between linear scan and binary search compounds quickly: repeated threshold queries over large sorted datasets turn O(n) latency into a bottleneck, especially under fan-out or high QPS. The sorted-order invariant enables predictable logarithmic lookup, which is foundational for index design, cache-friendly query paths, and systems that must answer “next available slot” or “first eligible event” without scanning entire partitions.

## 🔍 Problem Statement
You are given a sorted integer array `openTimes` where `openTimes[i]` is the minute of day when the `i`-th store opens. The array is sorted in non-decreasing order, so equal values may appear multiple times. You are also given an integer `queryTime`.

Return the index of the first store whose opening time is **greater than or equal to** `queryTime`. If no such store exists, return `-1`.

Constraints:

- `1 <= openTimes.length <= 10^5`
- `0 <= openTimes[i] <= 10^9`
- `openTimes` is sorted in non-decreasing order
- `0 <= queryTime <= 10^9`

Examples:

- `openTimes = [120, 180, 240, 300], queryTime = 200` → `2`
- `openTimes = [60, 60, 90, 150], queryTime = 60` → `0`

The key constraint is sorted order. That immediately suggests binary search, because a linear scan is correct but wastes the structure already provided.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find `queryTime` exactly.” It is “find the first value `>= queryTime`.”
2. Notice the array is sorted → whenever a middle value is large enough, everything to its right is also large enough, so the answer could be at `mid` or earlier.
3. That monotonic condition is the binary-search signal: values before the answer are “too small,” values at or after the answer are “valid.”
4. Maintain a search window over indices. At each step:
   - If `openTimes[mid] < queryTime`, the answer must be to the right.
   - Otherwise, `mid` is a candidate answer, but there may be an earlier valid index, so move left.
5. Keep track of the best candidate seen so far.
6. When the window closes, either you found the leftmost valid index or no valid value exists, in which case return `-1`.

This is the classic **lower bound** search. Once you recognize “first position satisfying a threshold in sorted data,” the solution becomes mechanical.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search / Lower Bound**  
   The array is sorted, and the predicate `openTimes[i] >= queryTime` is monotonic across indices: once true, it stays true. That makes lower-bound binary search the correct abstraction.

2. **Initialize the search window**  
   Set `left = 0`, `right = openTimes.length - 1`, and `answer = -1`.  
   Invariant: if a valid answer exists, it is always within the current window or already stored in `answer`.

3. **Compute the midpoint safely**  
   Use `mid = left + (right - left) / 2`.  
   This avoids overflow in languages where `left + right` could exceed integer bounds, even though the current constraints are modest.

4. **Check whether `mid` is valid**  
   If `openTimes[mid] >= queryTime`, then `mid` is a candidate. Record `answer = mid` and continue searching left with `right = mid - 1`.  
   Why correct: we want the **first** valid index, so any later valid index is inferior.

5. **Discard invalid left segments**  
   If `openTimes[mid] < queryTime`, then every index `<= mid` is also invalid because the array is sorted. Move right with `left = mid + 1`.

6. **Terminate when the window is exhausted**  
   When `left > right`, all possibilities have been resolved.  
   Invariant at termination: `answer` is the smallest index seen with value `>= queryTime`, or `-1` if no such index exists.

7. **Return the result**  
   This handles duplicates naturally, because the algorithm keeps pushing left after finding a valid match.

## 📊 Worked Example
Example: `openTimes = [120, 180, 240, 300]`, `queryTime = 200`

| Step | left | right | mid | openTimes[mid] | Action | answer |
|------|------|-------|-----|----------------|--------|--------|
| 1 | 0 | 3 | 1 | 180 | Too small, search right | -1 |
| 2 | 2 | 3 | 2 | 240 | Valid candidate, search left | 2 |
| 3 | 2 | 1 | - | - | Stop (`left > right`) | 2 |

Trace explanation:

1. Start with the full array.
2. Index `1` has value `180`, which is less than `200`, so indices `0..1` cannot contain the answer.
3. Move to the right half. Index `2` has value `240`, which satisfies the condition, so record `2`.
4. Search left of `2` to see whether an earlier valid index exists. The window becomes empty immediately.
5. Return `2`, the first store opening at or after the query time.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(log n)** time because each iteration halves the remaining search space. The dominant operation is the binary-search loop over the sorted array. At `10^6` elements this is roughly 20 comparisons; at `10^9`, roughly 30. That difference is why this pattern scales cleanly for repeated threshold queries.

### Space Complexity
The algorithm uses **O(1)** extra space. It stores only a few index variables and an optional `answer` candidate. No auxiliary data structures are required. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off would be recursion, which would increase stack usage.

## 💡 Key Takeaways
- If the input is sorted and the question asks for the **first** index meeting a condition like `>= x`, think **lower-bound binary search** immediately.
- A monotonic predicate over indices — false before some boundary, true after it — is the strongest signal that binary search applies.
- When `openTimes[mid] >= queryTime`, do not stop; record `mid` and continue left, or you will miss earlier duplicates.
- Be explicit about the “not found” case: if every value is smaller than `queryTime`, the correct return is `-1`, not `left`.
- In production systems, preserving sorted order turns threshold lookups from scans into index-style queries with predictable logarithmic latency.

## 🚀 Variations & Further Practice
- Return the **count** of stores open at or after `queryTime`; the twist is converting the lower-bound index into an aggregate result without scanning.
- Given many queries, answer each efficiently against the same sorted array; the twist is recognizing when preprocessing is unnecessary because repeated binary search is already optimal.
- Find the first store opening in a time range `[start, end]`; the twist is combining two boundary searches (`lower_bound(start)` and `upper_bound(end)`).