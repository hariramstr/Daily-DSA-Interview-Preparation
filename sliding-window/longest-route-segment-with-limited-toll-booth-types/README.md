# Longest Route Segment With Limited Toll Booth Types

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding-window, hash-map, two-pointers

---

## 🗂 Problem Overview
Given an array `booths` and an integer `k`, find the maximum length of any contiguous subarray containing at most `k` distinct booth types. The output is a single integer: that longest valid segment length. The challenge is not correctness on small inputs, but doing it efficiently under `booths.length <= 200000`, where brute-force enumeration of all subarrays becomes prohibitively expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent span satisfying a bounded-cardinality constraint: streaming analytics over event types, fraud detection over merchant categories, observability pipelines tracking service IDs, and search/session analysis over user actions. At scale, naive rescans or recomputation per position collapse under throughput and latency targets. The sliding-window approach enables single-pass processing with bounded incremental state, which matters for online systems, memory-sensitive stream processors, and services that must compute windowed metrics continuously rather than batch-rebuild them from scratch.

## 🔍 Problem Statement
You are given an integer array `booths`, where `booths[i]` is the toll booth type encountered at position `i`, and an integer `k`. Return the length of the longest contiguous subarray containing at most `k` distinct values.

Constraints:

- `1 <= booths.length <= 200000`
- `1 <= booths[i] <= 1000000000`
- `0 <= k <= booths.length`

If `k = 0`, no booth type may appear in the segment, so the answer is `0`.

Examples:

- `booths = [4, 7, 4, 4, 9, 7, 9, 9], k = 2` → `4`
- `booths = [5, 5, 1, 2, 1, 2, 3], k = 3` → `6`

The key constraint is input size: `O(n^2)` subarray enumeration is not viable. The solution must maintain validity incrementally while scanning once, which strongly suggests a sliding-window design backed by frequency tracking.

## 🪜 How to Solve This
1. Read the problem → the word **contiguous** is the first signal. That usually rules out sorting and points toward a moving range over the original array.

2. The constraint is **at most `k` distinct values** → we do not care about order inside the segment, only how many unique booth types are currently present. That means we need counts, not positions alone.

3. If we extend a segment one element at a time, validity changes locally → adding one booth either keeps the distinct count unchanged or increases it by one. That is exactly the kind of state a hash map can maintain efficiently.

4. Once a window becomes invalid (`distinct > k`), the only repair is to shrink it from the left until it becomes valid again. Shrinking from the right would skip candidate answers and break contiguity.

5. This gives the full pattern: two pointers define a window, a hash map stores frequencies, and we update the best length whenever the window is valid.

6. The reason this is linear is that each pointer only moves forward. No element is added or removed more than once.

## 🧩 Algorithm Walkthrough
1. **Handle the trivial edge case.**  
   If `k == 0`, return `0` immediately. No non-empty segment can satisfy the constraint.

2. **Initialize the sliding window.**  
   Use the **Two Pointers / Sliding Window** pattern: `left = 0`, iterate `right` from `0` to `n - 1`. Maintain a hash map `freq` from booth type to count within the current window.

3. **Expand the window to include `booths[right]`.**  
   Increment its count in `freq`. If this booth type was not previously present, the number of distinct types increases implicitly because the map gains a new key.

4. **Restore validity when needed.**  
   While `freq.size() > k`, move `left` rightward. Decrement `freq[booths[left]]`; if its count reaches zero, remove that key from the map.  
   This is correct because the only invalidity is excess distinct types, and removing items from the left is the minimal way to recover a valid contiguous window ending at `right`.

5. **Record the best answer.**  
   After the shrink loop, the window `[left, right]` is valid and maximal for this `right` under the current `left` constraint. Update `best = max(best, right - left + 1)`.

6. **Maintain the invariant.**  
   At every iteration end, the window is contiguous, valid (`<= k` distinct), and represented exactly by `freq`. Because `left` and `right` each advance monotonically, the total work is linear.

## 📊 Worked Example
Example: `booths = [4, 7, 4, 4, 9, 7, 9, 9]`, `k = 2`

| right | booths[right] | action | left | freq | best |
|---|---:|---|---:|---|---:|
| 0 | 4 | add 4 | 0 | {4:1} | 1 |
| 1 | 7 | add 7 | 0 | {4:1, 7:1} | 2 |
| 2 | 4 | add 4 | 0 | {4:2, 7:1} | 3 |
| 3 | 4 | add 4 | 0 | {4:3, 7:1} | 4 |
| 4 | 9 | add 9, invalid → shrink | 2 | {4:2, 9:1} | 4 |
| 5 | 7 | add 7, invalid → shrink | 4 | {9:1, 7:1} | 4 |
| 6 | 9 | add 9 | 4 | {9:2, 7:1} | 4 |
| 7 | 9 | add 9 | 4 | {9:3, 7:1} | 4 |

The longest valid window length is `4`. The important observation is that after every expansion, shrinking continues only until the distinct-count constraint is restored.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time, assuming `O(1)` average hash map operations. Each booth enters the window once when `right` advances and leaves at most once when `left` advances. At `10^6` elements this remains practical in a single pass; at `10^9`, runtime becomes dominated by raw scan cost and system-level I/O constraints.

### Space Complexity
`O(min(n, k))` to store frequencies of booth types currently in the window, bounded by the number of distinct values present there. This cannot be meaningfully reduced without losing constant-time updates; alternatives trade memory for slower lookups or recomputation.

## 💡 Key Takeaways
- If the problem asks for a **longest/shortest contiguous segment** under a running constraint, sliding window should be your first candidate.
- If validity depends on **counts of distinct values**, pair the window with a hash map of frequencies rather than recomputing uniqueness repeatedly.
- The most common bug is forgetting to **remove a key when its count drops to zero**, which makes the distinct count wrong.
- Another common trap is updating the answer **before** shrinking invalid windows; only valid windows should contribute to `best`.
- The production-level insight is incremental state maintenance: when constraints evolve locally as data streams in, avoid global recomputation and preserve correctness through explicit invariants.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters**: same pattern, but over strings; the conceptual twist is character-domain assumptions and Unicode handling in real systems.
- **Minimum window substring**: still sliding window plus counts, but now the goal is the shortest valid window satisfying required frequencies, which makes the shrink condition more subtle.
- **Subarrays with exactly `k` distinct integers**: harder because “exactly” is awkward directly; the standard trick computes `atMost(k) - atMost(k - 1)`.