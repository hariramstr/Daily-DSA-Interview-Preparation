# Shortest Sorted Window to Merge Daily Rankings

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sliding Window, Array, Merge, Sorted Array

---

## 🗂 Problem Overview
Given two individually sorted arrays, `yesterday` and `today`, treat them as one sorted stream formed by merging them conceptually, not physically. Find the minimum length of any contiguous window in that virtual merged order whose sum is at least `target`. Return `-1` if no such window exists. The non-trivial part is preserving `O(n + m)` behavior without materializing the merged array, which would waste memory and obscure the intended streaming solution.

## 🌍 Engineering Impact
This pattern shows up anywhere multiple ordered sources must be analyzed as one logical stream: search ranking merges, event-time joins in streaming pipelines, compaction scans in LSM trees, market data feeds, and distributed rate-limit windows over sharded counters. At scale, eagerly materializing merged views adds memory pressure, cache misses, and latency spikes. A virtual-merge sliding window lets you compute aggregate properties online, with predictable linear cost and constant extra space. That matters when arrays become million-element runs, or when the same logic is embedded in hot paths such as ranking, observability rollups, or real-time fraud scoring.

## 🔍 Problem Statement
You are given two non-decreasing integer arrays, `yesterday` and `today`, and a positive integer `target`. Imagine merging both arrays into one non-decreasing array, but do not build that array explicitly. In that virtual merged order, find the shortest contiguous subarray whose sum is at least `target`.

Return the minimum window length, or `-1` if no such window exists.

Constraints:
- `1 <= yesterday.length, today.length <= 10^5`
- `1 <= yesterday[i], today[i] <= 10^4`
- `1 <= target <= 10^9`
- Both arrays are already sorted
- All values are positive, which makes sliding-window shrinking valid
- Expected solution: `O(n + m)` time

Examples:
- `yesterday = [1,4,7], today = [2,3,8], target = 11` → merged: `[1,2,3,4,7,8]` → answer: `2`
- `yesterday = [2,2,5], today = [1,6,9], target = 15` → merged: `[1,2,2,5,6,9]` → answer: `2`

The key algorithmic constraint is that contiguity is defined in merged order, not within either source array independently.

## 🪜 How to Solve This
1. Read the problem → notice this is not “merge arrays” and not “subarray in one array.” It is a sliding-window problem over a **virtual sorted stream**.

2. The positivity constraint is the giveaway → when all numbers are positive, expanding the right edge can only increase the sum, and shrinking the left edge can only decrease it. That is exactly when a two-pointer window is optimal.

3. The next question is representation → we need merged-order contiguity, but building the merged array costs unnecessary space. So treat merge as an iterator: at any point, the next element is the smaller head of `yesterday` or `today`.

4. A sliding window needs both ends, not just the next element → so each pointer must know how to fetch the value at its current merged position without storing the full merge. Conceptually, both left and right pointers advance through the same merge order.

5. Maintain a running sum while advancing the right pointer. Whenever the sum reaches `target`, shrink from the left as aggressively as possible and record the shortest valid length.

6. Because each merged element enters the window once and leaves once, total work stays linear.

## 🧩 Algorithm Walkthrough
1. **Model the merged array as a stream.**  
   Use merge logic from sorted-array merge: compare current heads from `yesterday` and `today`, and consume the smaller one. This preserves the invariant that elements are visited in exact merged sorted order.

2. **Run a Two Pointers / Sliding Window over that virtual order.**  
   The right pointer expands the window by consuming the next merged element and adding it to `sum`. The invariant is: `sum` always equals the values currently inside the merged-order window.

3. **Track enough state to remove from the left.**  
   Since we are not storing the full merged array, the left side must also advance in merged order. One practical implementation is to expose a helper that returns the next merged value for each side based on its own `(i, j)` source indices. The left pointer’s state identifies the first element currently inside the window.

4. **Shrink greedily when `sum >= target`.**  
   While the current window is valid, update `answer = min(answer, windowLength)`, then remove the leftmost merged element and advance the left pointer. This is correct because all values are positive: removing more elements is the only way to possibly get a shorter valid window.

5. **Continue until the right pointer exhausts both arrays.**  
   Every merged element is processed at most twice: once when entering from the right, once when leaving from the left. That gives linear time.

6. **Return `-1` if no valid window was found.**  
   This covers the case where the total sum of both arrays is still below `target`.

## 📊 Worked Example
Example: `yesterday = [1,4,7]`, `today = [2,3,8]`, `target = 11`  
Virtual merged order: `[1,2,3,4,7,8]`

| Step | Add | Window Values | Sum | Action | Best |
|---|---:|---|---:|---|---:|
| 1 | 1 | [1] | 1 | expand | ∞ |
| 2 | 2 | [1,2] | 3 | expand | ∞ |
| 3 | 3 | [1,2,3] | 6 | expand | ∞ |
| 4 | 4 | [1,2,3,4] | 10 | expand | ∞ |
| 5 | 7 | [1,2,3,4,7] | 17 | valid, shrink | 5 |
| 5a | remove 1 | [2,3,4,7] | 16 | still valid | 4 |
| 5b | remove 2 | [3,4,7] | 14 | still valid | 3 |
| 5c | remove 3 | [4,7] | 11 | still valid | 2 |
| 5d | remove 4 | [7] | 7 | stop shrinking | 2 |
| 6 | 8 | [7,8] | 15 | valid, shrink | 2 |

Final answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`. Each element from `yesterday` and `today` is consumed once by the right pointer and removed at most once by the left pointer. There is no nested rescanning. At million-element scale this is practical; at billion-element scale, linear scan cost is still high but remains the best possible asymptotically for exact evaluation.

### Space Complexity
`O(1)` extra space if you stream the merge directly and keep only pointer state, running sum, and answer. If an implementation uses recursion or helper abstractions with buffered state, it may drift toward `O(log(n+m))`, but no auxiliary merged array is required.

## 💡 Key Takeaways
- If input arrays are individually sorted and the problem asks about a property of their combined order, think “virtual merge” before “build merged array.”
- If all values are positive and you need the shortest contiguous segment meeting a threshold, that is a strong sliding-window signal.
- Contiguity is in merged order, so shrinking or expanding inside one source array independently is incorrect.
- Be careful with window length accounting: update the answer before removing the leftmost element during the shrink loop.
- The production lesson is broader than this problem: many hot-path analytics can be computed over logical merged views without materializing them, which reduces memory traffic and improves latency predictability.

## 🚀 Variations & Further Practice
- Find the shortest merged-order window with sum **exactly** `target`; positivity still helps, but exact-match handling is less forgiving than threshold-based shrinking.
- Generalize from two sorted arrays to **k sorted streams**; the merge frontier becomes a heap, and preserving window semantics across multiple producers is the harder part.
- Allow **negative values** in the arrays; standard sliding window breaks, and you need prefix sums plus a monotonic deque or another structure for shortest-subarray queries.