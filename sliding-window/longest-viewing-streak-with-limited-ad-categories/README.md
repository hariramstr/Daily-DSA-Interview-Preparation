# Longest Viewing Streak With Limited Ad Categories

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an integer array `ads` and an integer `k`, find the maximum length of any contiguous subarray containing at most `k` distinct category IDs. Repeated IDs do not increase the distinct-count budget; only unique categories matter. The output is a single integer: the longest valid streak length. The challenge is scale: with up to `200000` elements, enumerating all subarrays is infeasible, so the solution must update state incrementally in near-linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent interval satisfying a bounded-cardinality constraint: ad-tech session analytics, streaming observability windows, fraud detection over event types, search query reformulation spans, and distributed rate-limiters tracking unique principals per interval. At production scale, brute-force window validation collapses under quadratic behavior and excessive cache-unfriendly rescans. A sliding-window design turns the problem into a single pass with bounded mutable state, which is exactly what enables online processing, predictable latency, and efficient memory use in ingestion pipelines or real-time decision services.

## 🔍 Problem Statement
You are given an array `ads` where `ads[i]` is the ad category shown at minute `i`, and an integer `k`. A viewing streak is any contiguous subarray. The task is to return the maximum streak length such that the number of distinct category IDs in that subarray is at most `k`.

Constraints:

- `1 <= ads.length <= 200000`
- `1 <= ads[i] <= 1000000000`
- `1 <= k <= ads.length`

Examples:

- `ads = [4, 2, 2, 5, 5, 2, 4], k = 2` → `5`  
  Valid longest streak: `[2, 2, 5, 5, 2]`

- `ads = [1, 3, 1, 3, 2, 2, 4, 2], k = 3` → `6`  
  Valid longest streaks include `[1, 3, 1, 3, 2, 2]`

The key constraint is input size: checking every subarray would be `O(n²)`, which is too slow. That forces a linear or near-linear approach.

## 🪜 How to Solve This
1. Read the problem → it asks for a **contiguous** segment, so this is immediately a window problem, not a set or sorting problem.

2. The validity rule depends on the number of **distinct** values in the current range → we need a frequency structure keyed by category ID. That suggests a `HashMap<category, count>`.

3. We want the **longest** valid window, not all valid windows → expand greedily to the right, and only shrink when the constraint is violated.

4. Start with two pointers: `left` and `right`. Move `right` forward one step at a time, adding `ads[right]` into the frequency map.

5. If the map now contains more than `k` distinct categories, the window is invalid. Move `left` forward until validity is restored, decrementing counts and removing keys when a count reaches zero.

6. After each expansion/shrink cycle, the window `[left, right]` is the longest valid window ending at `right`, so update the global maximum.

7. Why this works: each element enters the window once and leaves once. No rescanning, no nested enumeration, just incremental state maintenance.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window with Two Pointers.**  
   The problem asks for the longest contiguous region under a constraint that can be updated incrementally. That is the canonical signal for a variable-size sliding window.

2. **Maintain window state with a hash map.**  
   Store `count[category] = frequency within current window`. This gives constant-average-time updates when expanding or shrinking the window.

3. **Expand the right pointer.**  
   For each `right` from `0` to `n - 1`, add `ads[right]` to the map. If it is a new category, the distinct count increases implicitly via map size.

4. **Restore validity when distinct categories exceed `k`.**  
   While `map.size() > k`, increment `left` and decrement the count of `ads[left]`. If a count becomes zero, remove that key.  
   **Invariant:** after this loop, the current window contains at most `k` distinct categories.

5. **Record the best valid window.**  
   Once valid, compute `right - left + 1` and compare against the running maximum.  
   **Why correct:** for a fixed `right`, any valid window starting left of the current `left` would have already violated the distinct-category constraint, so the maintained window is the longest valid one ending at `right`.

6. **Finish after one pass.**  
   Each pointer moves monotonically forward. No pointer ever retreats, which is why the total work is linear despite the nested `while` loop.

## 📊 Worked Example
Example: `ads = [4, 2, 2, 5, 5, 2, 4]`, `k = 2`

| right | ads[right] | action | left | window | distinct | max |
|---|---:|---|---:|---|---:|---:|
| 0 | 4 | add 4 | 0 | `[4]` | 1 | 1 |
| 1 | 2 | add 2 | 0 | `[4,2]` | 2 | 2 |
| 2 | 2 | increment 2 | 0 | `[4,2,2]` | 2 | 3 |
| 3 | 5 | add 5, invalid → shrink remove 4 | 1 | `[2,2,5]` | 2 | 3 |
| 4 | 5 | increment 5 | 1 | `[2,2,5,5]` | 2 | 4 |
| 5 | 2 | increment 2 | 1 | `[2,2,5,5,2]` | 2 | 5 |
| 6 | 4 | add 4, invalid → shrink past first 2, second 2, 5, 5 | 5 | `[2,4]` | 2 | 5 |

Answer: `5`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)` average time. Each element is added to the window once by `right` and removed at most once by `left`, so the dominant work is linear hash map updates. At `10^6` elements this is practical in-memory; at `10^9`, the algorithm is still asymptotically right but becomes constrained by I/O, memory locality, and runtime budget.

### Space Complexity
`O(min(n, k))` auxiliary space, owned by the frequency hash map for categories currently in the window. In the worst valid window it holds at most `k` keys. You cannot meaningfully reduce this without losing constant-time distinct tracking; alternatives trade memory for slower updates.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a dynamically checkable constraint, think variable-size sliding window.
- If validity depends on the number of **distinct values** in the current range, pair the window with a frequency hash map.
- Remove a category from the map exactly when its count drops to zero; otherwise `map.size()` overstates distinct categories.
- Update the answer only after shrinking back to a valid window, and compute length as `right - left + 1` to avoid off-by-one errors.
- The production lesson is broader: incremental state maintenance beats recomputation when processing high-volume streams with local window constraints.

## 🚀 Variations & Further Practice
- **Exactly `k` distinct categories**: similar window mechanics, but you count windows or maximize length under an equality constraint rather than `<= k`.
- **Longest substring with at most `k` distinct characters**: same pattern on strings; the twist is recognizing the abstraction independent of domain vocabulary.
- **Minimum window containing all required categories**: still two pointers, but the optimization direction flips from maximizing a valid window to minimizing one while satisfying coverage constraints.