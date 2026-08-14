# Longest Recipe Prep Window Under Ingredient Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array of recipe ingredient categories and an integer `k`, find the maximum length of any contiguous segment containing at most `k` distinct categories. The output is a single integer: the size of the longest valid window. The challenge is scale: with up to `100000` recipes, enumerating all subarrays is too expensive, so the solution must maintain validity incrementally while scanning the array once.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the longest recent span under a bounded diversity constraint. Examples include streaming analytics over event types, sessionization in clickstreams, log pipelines tracking windows with limited service IDs, and cache admission logic constrained by tenant cardinality. Without an incremental windowing approach, implementations degrade into repeated rescans or per-range recomputation, which collapses under high-throughput streams. The sliding-window plus frequency-map pattern enables linear-time processing, predictable memory growth, and online operation where decisions must be made as data arrives rather than after full batch materialization.

## 🔍 Problem Statement
You are given:

- `recipes`: an array of strings where `recipes[i]` is the ingredient category for the `i`-th recipe
- `k`: the maximum number of distinct categories allowed in a contiguous window

Return the length of the longest contiguous subarray containing at most `k` distinct strings.

Constraints:

- `1 <= recipes.length <= 100000`
- `1 <= recipes[i].length <= 20`
- `recipes[i]` contains lowercase English letters
- `1 <= k <= recipes.length`

Examples:

```text
Input:  recipes = ["dairy","grain","dairy","spice","grain","grain"], k = 2
Output: 3
```

```text
Input:  recipes = ["meat","meat","veg","veg","sauce","veg","veg"], k = 2
Output: 4
```

Edge cases matter: repeated categories should extend the window cheaply, `k = 1` reduces to the longest run with one category, and if `k` is at least the number of distinct categories in the array, the answer is the full array length. The `100000` upper bound rules out quadratic subarray checks.

## 🪜 How to Solve This
1. Read the problem → the word *contiguous* immediately suggests a window, not a subset or grouping problem.

2. Notice the constraint is about the number of **distinct** categories inside that window → we need a structure that can tell us how many unique values are currently present. That points to a `HashMap<String, Int>` storing frequencies.

3. Start with a window `[left, right]` and expand `right` one step at a time. Each new recipe either increases the count of an existing category or introduces a new one.

4. If the window now has more than `k` distinct categories, it became invalid. Since the window must stay contiguous, the only legal repair is to move `left` forward until the distinct count drops back to `k` or less.

5. Track the maximum valid window size after each expansion. This works because every index enters the window once and leaves once; we never need to restart or re-evaluate old ranges.

6. The core insight: when a constraint is monotonic under expansion and repairable by shrinking from one side, use a sliding window with two pointers and counts.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window state.**  
   Use two pointers: `left = 0` and `right` iterating from `0` to `n - 1`. Maintain a hash map `freq` from category name to count, plus `maxLen = 0`.  
   **Invariant:** `freq` always reflects counts for the current window `[left, right]`.

2. **Expand the window to include `recipes[right]`.**  
   Increment `freq[recipes[right]]`. If this category was absent before, the number of distinct categories increases implicitly through the map size.  
   **Why correct:** the window definition changes only by adding one element on the right.

3. **Repair invalid windows by shrinking from the left.**  
   While `freq.size > k`, decrement `freq[recipes[left]]`, and if a count reaches zero, remove that key from the map. Then increment `left`.  
   **Invariant:** after the loop, the window is valid again with at most `k` distinct categories.  
   **Why correct:** any valid subarray ending at `right` and starting before the current `left` would still contain too many distinct categories, so shrinking is necessary.

4. **Update the best answer.**  
   Once valid, compute `right - left + 1` and update `maxLen`.  
   **Why correct:** this is the longest valid window ending at `right`, because `left` is the smallest index that keeps the window valid.

5. **Return `maxLen`.**  
   This is the standard **Sliding Window / Two Pointers** pattern: one pass, local repairs, and a frequency map to maintain distinctness without rescanning the window.

## 📊 Worked Example
Example: `recipes = ["dairy","grain","dairy","spice","grain","grain"]`, `k = 2`

| right | recipe  | action                         | left | freq                          | valid | maxLen |
|------:|---------|--------------------------------|-----:|-------------------------------|:-----:|------:|
| 0     | dairy   | add dairy                      | 0    | {dairy:1}                     | yes   | 1 |
| 1     | grain   | add grain                      | 0    | {dairy:1, grain:1}            | yes   | 2 |
| 2     | dairy   | increment dairy                | 0    | {dairy:2, grain:1}            | yes   | 3 |
| 3     | spice   | add spice → 3 distinct         | 0    | {dairy:2, grain:1, spice:1}   | no    | 3 |
| 3     | spice   | shrink: remove dairy at left   | 1    | {dairy:1, grain:1, spice:1}   | no    | 3 |
| 3     | spice   | shrink: remove grain at left   | 2    | {dairy:1, spice:1}            | yes   | 3 |
| 4     | grain   | add grain → 3 distinct         | 2    | {dairy:1, spice:1, grain:1}   | no    | 3 |
| 4     | grain   | shrink: remove dairy           | 3    | {spice:1, grain:1}            | yes   | 3 |
| 5     | grain   | increment grain                | 3    | {spice:1, grain:2}            | yes   | 3 |

Answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = recipes.length`. Each recipe is added to the window once by advancing `right`, and removed at most once by advancing `left`. Hash map updates are amortized `O(1)`, so the dominant cost is a single linear scan. At `10^6` elements this remains practical; at `10^9`, throughput and memory locality become the real bottlenecks, not asymptotic behavior.

### Space Complexity
`O(min(n, d))`, where `d` is the number of distinct categories seen in the active window; in the worst case this is `O(n)`. The hash map owns the space. You cannot reduce this asymptotically without losing constant-time distinct-count maintenance and falling back to rescans or approximate tracking.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a bounded condition, sliding window should be your first candidate.
- If validity depends on the number of **distinct** values in the current range, pair two pointers with a frequency map.
- Remove keys from the map when their count reaches zero; leaving zero-count entries breaks the distinct-count invariant.
- Update the answer only after restoring the window to a valid state, or you will count invalid ranges and get off-by-one errors.
- The production-grade insight is incremental state maintenance: preserve enough window metadata to make each new event cheap, instead of recomputing range properties from scratch.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters**: same pattern on strings; the twist is tighter constant-factor sensitivity and often Unicode handling in production implementations.
- **Minimum window substring**: still sliding window, but now the goal is the shortest valid range satisfying required counts, which changes when and how you shrink.
- **Subarrays with exactly `k` distinct values**: harder because “exactly” is not directly monotonic; typically solved via `atMost(k) - atMost(k - 1)`.