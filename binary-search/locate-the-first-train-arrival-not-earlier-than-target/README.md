# Locate the First Train Arrival Not Earlier Than Target

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Lower Bound

---

## 🗂 Problem Overview
Given a sorted array `arrivals`, return the index of the first value that is greater than or equal to `target`. If every value is smaller than `target`, return `-1`. The input is already ordered, which rules out brute-force as the intended approach. The non-trivial part is not finding any matching or larger value, but finding the *leftmost* one efficiently, even when duplicates exist.

## 🌍 Engineering Impact
This is the lower-bound lookup primitive behind booking engines, ad serving, search retrieval, time-series storage, and scheduler queues. Given an ordered set of candidates, systems frequently need the earliest entry that satisfies a minimum threshold: first available slot, first event after a watermark, first posting above a score cutoff. At small scale, linear scan is acceptable; at production scale, repeated scans turn indexed reads into latency cliffs and cache-unfriendly hot paths. Binary search preserves predictable `O(log n)` lookup cost, enables immutable sorted structures, and composes cleanly with B-trees, SSTables, skip lists, and in-memory index layers.

## 🔍 Problem Statement
You are given a sorted integer array `arrivals` where `arrivals[i]` is the scheduled arrival time of the `i`-th train, measured in minutes after midnight. The array is sorted in non-decreasing order, so duplicate arrival times are allowed. You are also given an integer `target`, representing the earliest acceptable boarding time.

Return the index of the first train whose arrival time is **greater than or equal to** `target`. If no train satisfies that condition, return `-1`.

Constraints:
- `1 <= arrivals.length <= 10^5`
- `0 <= arrivals[i] <= 1439`
- `arrivals` is sorted in non-decreasing order
- `0 <= target <= 1439`

Examples:
- `arrivals = [120, 180, 180, 240, 315], target = 181` → `3`
- `arrivals = [60, 90, 150, 150, 210], target = 150` → `2`

The key constraint is that the array is sorted, which makes binary search the correct algorithmic choice over linear scan.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find target,” it is “find the first value `>= target`.”
2. Notice the array is already sorted → whenever the input is ordered and you need a threshold position, binary search should be the first instinct.
3. Duplicates matter → if `target` appears multiple times, returning any matching index is wrong; we need the leftmost valid index.
4. Reframe the problem as a boundary search → find the split point between values `< target` and values `>= target`.
5. Maintain a search window over indices. At each midpoint:
   - If `arrivals[mid] >= target`, the answer could be `mid` or something earlier, so move left.
   - If `arrivals[mid] < target`, everything at or before `mid` is invalid, so move right.
6. Track the best candidate seen so far, or use the final left boundary as the insertion point.
7. If the boundary lands past the end of the array, no valid train exists → return `-1`.

This is the standard lower-bound pattern: search for the first index where a monotonic predicate becomes true.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Lower Bound Binary Search.**  
   The predicate is monotonic: `arrivals[i] >= target` is false for some prefix, then true for the remaining suffix. That monotonic transition is exactly what binary search exploits.

2. **Initialize pointers.**  
   Set `left = 0`, `right = arrivals.length - 1`, and `answer = -1`.  
   Invariant: if a valid answer exists, it is always within the current search range or already stored in `answer`.

3. **Pick the midpoint safely.**  
   Compute `mid = left + (right - left) / 2`.  
   This avoids overflow in languages where `left + right` may exceed integer bounds, even though the current constraints are small.

4. **Evaluate the midpoint against the threshold.**  
   If `arrivals[mid] >= target`, then `mid` is a valid candidate. Record `answer = mid` and shrink the search to the left half with `right = mid - 1`.  
   Why correct: we are looking for the *first* valid index, so any later valid index is inferior.

5. **Discard invalid prefix when midpoint is too small.**  
   If `arrivals[mid] < target`, then every index `<= mid` is also too small because the array is sorted. Move `left = mid + 1`.

6. **Repeat until the window closes.**  
   When `left > right`, the search is complete.  
   Invariant maintained throughout: all indices left of `left` are known invalid, and any recorded `answer` is valid but may not yet be minimal.

7. **Return the result.**  
   If `answer` was never updated, no train arrives at or after `target`, so return `-1`. Otherwise return `answer`.

## 📊 Worked Example
Example: `arrivals = [120, 180, 180, 240, 315]`, `target = 181`

| Step | left | right | mid | arrivals[mid] | Action | answer |
|------|------|-------|-----|---------------|--------|--------|
| 1 | 0 | 4 | 2 | 180 | `< 181`, move right | -1 |
| 2 | 3 | 4 | 3 | 240 | `>= 181`, record and move left | 3 |

Search stops because `left = 3`, `right = 2`.

Trace explanation:
1. Midpoint at index `2` is `180`, which is too early. Because the array is sorted, indices `0..2` cannot contain the answer.
2. Next midpoint is index `3`, value `240`, which satisfies the condition. Record it, then continue left to check whether an earlier valid train exists.
3. The window closes immediately, so index `3` is the first arrival not earlier than `181`.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration halves the remaining search interval, so the dominant operation is the binary search loop. At `10^6` elements this is about 20 comparisons; at `10^9`, about 30. That predictability is why threshold lookups on sorted data remain viable at very large scale.

### Space Complexity
`O(1)`. The algorithm uses only a few scalar variables: pointers, midpoint, and optional answer index. No auxiliary data structure is required. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off is readability versus using a slightly more compact boundary-search formulation.

## 💡 Key Takeaways
- If the input is sorted and the question asks for the first index meeting a threshold, think lower-bound binary search immediately.
- When duplicates exist and the requirement says “first,” “leftmost,” or “earliest valid,” standard equality-based binary search is insufficient.
- The main off-by-one trap is moving `right = mid - 1` after finding a valid candidate; using `right = mid` with this loop shape can stall.
- Another common bug is returning on the first `arrivals[mid] >= target`; that finds a valid index, not necessarily the earliest one.
- At system level, this pattern is the in-memory analogue of index seeks in ordered storage engines: find the boundary, not the exact value.

## 🚀 Variations & Further Practice
- Return the insertion position instead of `-1` when no valid train exists; the conceptual twist is treating the result as a stable lower-bound index rather than a sentinel-based lookup.
- Find the last train arrival `<= target`; same binary-search family, but now you are computing an upper-bound-minus-one boundary.
- Given many passenger queries against the same schedule, optimize for repeated lookups; the twist is shifting from single-query algorithmics to indexed batch processing and cache-aware data layout.