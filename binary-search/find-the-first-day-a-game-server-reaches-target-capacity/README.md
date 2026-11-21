# Find the First Day a Game Server Reaches Target Capacity

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Lower Bound

---

## 🗂 Problem Overview
Given a non-decreasing array `players`, return the smallest index `i` where `players[i] >= target`. If no day reaches the required capacity, return `-1`. The challenge is not correctness by brute force, but using the sorted property to avoid a linear scan. This is a classic lower-bound lookup: find the first position where a monotonic condition becomes true.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need threshold crossing on ordered data: time-series alerting, autoscaling triggers, rate-limit bucket lookup, storage tier transitions, and search/index segment boundaries. In production, the difference between linear scan and logarithmic lookup is not academic; it determines whether hot-path queries remain cheap as datasets grow from thousands to millions of entries. Binary search enables predictable latency, better cache behavior, and simpler scaling assumptions. Without it, threshold checks embedded in schedulers, control planes, or analytics services become hidden O(n) costs that accumulate into tail-latency and throughput problems.

## 🔍 Problem Statement
You are given an integer array `players` of length `n`, where `1 <= n <= 100000`, and `players` is sorted in non-decreasing order. Each `players[i]` represents the active player count recorded on day `i`. You are also given an integer `target`.

Return the smallest index `i` such that `players[i] >= target`. If no such index exists, return `-1`.

The sorted-order guarantee is the key constraint: once some index satisfies the condition, every index to its right also satisfies it. That monotonicity makes binary search the correct algorithmic choice.

Examples:

- `players = [120, 150, 150, 210, 260]`, `target = 151` → `3`
- `players = [40, 80, 80, 95]`, `target = 100` → `-1`

Edge cases include all values below `target`, `target` smaller than the first element, and repeated values where the first qualifying index must be returned.

## 🪜 How to Solve This
1. Read the problem → notice the array is already sorted. That immediately rules out any need for preprocessing.
2. The request is not “find `target` exactly” → it is “find the first value at least `target`.” That is a lower-bound query, not a standard equality search.
3. Translate the condition into a monotonic predicate:  
   `players[i] >= target` is false for some prefix, then true for the rest.
4. A false-then-true boundary in a sorted structure is the canonical signal for binary search.
5. Keep a search window `[left, right]` and inspect `mid`.
6. If `players[mid] >= target`, the answer could be `mid` or something earlier → move left by setting `right = mid - 1`, while recording `mid` as a candidate.
7. If `players[mid] < target`, everything at or left of `mid` is unusable → move right with `left = mid + 1`.
8. Continue until the window closes. If a candidate was found, return it; otherwise return `-1`.

The key mental shift is searching for the first valid position, not just any valid position.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Lower Bound via Binary Search.**  
   The array is sorted, and the predicate `players[i] >= target` is monotonic. That means there is a boundary between invalid indices and valid indices. Binary search is the right abstraction because it finds boundaries in O(log n).

2. **Initialize pointers and candidate state.**  
   Set `left = 0`, `right = players.length - 1`, and `answer = -1`.  
   Invariant: if an answer exists, it is always within the current search range or already stored in `answer`.

3. **Compute the midpoint safely.**  
   Use `mid = left + (right - left) / 2`.  
   This avoids overflow in languages where `left + right` may exceed integer bounds, even though the given constraints are modest.

4. **Check whether `mid` satisfies the threshold.**  
   If `players[mid] >= target`, then `mid` is a valid candidate. Record `answer = mid`.  
   Why correct: the first qualifying day could still be earlier, so we must continue searching the left half.

5. **Discard half the search space.**  
   - If `players[mid] >= target`, set `right = mid - 1`.  
   - Otherwise set `left = mid + 1`.  
   Invariant: discarded elements cannot contain a better answer than the remaining range plus `answer`.

6. **Terminate when `left > right`.**  
   At that point, every possible earlier candidate has been ruled out. `answer` is either the smallest valid index found or remains `-1` if the threshold is never reached.

This is not “binary search for equality”; it is binary search for the leftmost true position.

## 📊 Worked Example
Example: `players = [120, 150, 150, 210, 260]`, `target = 151`

| Step | left | right | mid | players[mid] | Action | answer |
|------|------|-------|-----|--------------|--------|--------|
| 1 | 0 | 4 | 2 | 150 | `< target`, move right | -1 |
| 2 | 3 | 4 | 3 | 210 | `>= target`, record and move left | 3 |
| 3 | 3 | 2 | — | — | stop (`left > right`) | 3 |

Trace explanation:

1. Midpoint `2` has value `150`, which is too small. Because the array is sorted, indices `0..2` cannot be answers.
2. Search continues in `3..4`. Midpoint `3` has value `210`, which satisfies the condition. Record `3`, but keep searching left in case an earlier day also works.
3. The search window closes immediately, so `3` is confirmed as the first day reaching or exceeding the target.

## ⏱ Complexity Analysis
### Time Complexity
Binary search examines at most `log2(n)` positions, so the runtime is **O(log n)**. The dominant operation is repeated midpoint evaluation and pointer adjustment. At `10^6` elements this is about 20 comparisons; at `10^9`, about 30. That predictability is why this pattern scales cleanly in latency-sensitive paths.

### Space Complexity
The algorithm uses **O(1)** extra space. It stores only a few indices and an optional answer variable; no auxiliary arrays or recursion are required. Space cannot be meaningfully reduced further without changing the execution model, and there is no trade-off worth making here.

## 💡 Key Takeaways
- If the input is sorted and the condition changes from false to true exactly once, think lower-bound binary search immediately.
- If the problem asks for the *first* index meeting a threshold, standard “find exact match” binary search is the wrong mental model.
- When `players[mid] >= target`, do not return immediately; record the candidate and continue left.
- Be careful with loop bounds and updates: `right = mid - 1` and `left = mid + 1` are what prevent infinite loops and off-by-one errors.
- At scale, threshold lookup on ordered data should be modeled as boundary search, because it turns hidden linear work into predictable logarithmic cost.

## 🚀 Variations & Further Practice
- **Find the last day below capacity**: return the largest index `i` such that `players[i] < target`. Same pattern, but now you are searching for the rightmost false boundary.
- **Search in a rotated sorted array**: the monotonic structure is partially broken, so you must first reason about which half remains ordered before applying binary search.
- **First bad version / first true in a monotonic predicate**: the array disappears, and the abstraction becomes pure boundary search over an implicit ordered domain.