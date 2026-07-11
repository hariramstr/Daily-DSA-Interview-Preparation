# Find First Day With At Least Target Signups

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, array, lower-bound

---

## 🗂 Problem Overview
Given a non-decreasing array `signups`, return the smallest index `i` such that `signups[i] >= target`. If no such index exists, return `-1`. The input is already sorted because values are cumulative totals by day, which rules out arbitrary search strategies. The non-trivial part is not correctness but meeting the expected `O(log n)` runtime by exploiting monotonic order instead of scanning linearly through up to `100000` entries.

## 🌍 Engineering Impact
This is the exact lower-bound query used in analytics backends, time-series milestone tracking, billing thresholds, SLO burn-rate dashboards, and event-ingestion pipelines. Once metrics are stored as cumulative or monotonic series, teams need fast threshold lookups across many queries. At scale, a linear scan per request turns cheap reads into latency spikes and cache-unfriendly work. Binary search changes the cost model: threshold queries become logarithmic, predictable, and composable. That matters when the same primitive sits under product dashboards, alert evaluation, quota enforcement, or historical reporting APIs serving high fan-out traffic.

## 🔍 Problem Statement
You are given an integer array `signups` of length `n`, where `1 <= n <= 100000`, and `signups` is sorted in non-decreasing order. Each `signups[i]` is the cumulative number of users signed up by the end of day `i`, with `0 <= signups[i] <= 1000000000`. You are also given `target`, where `0 <= target <= 1000000000`.

Return the earliest day index `i` such that `signups[i] >= target`. If no such day exists, return `-1`.

Examples:

- `signups = [3, 5, 5, 9, 14], target = 6` → `3`
- `signups = [1, 2, 4, 4, 7], target = 4` → `2`

Important edge cases:
- `target` may be smaller than or equal to the first element, so answer can be `0`
- duplicates matter; return the first qualifying index, not any qualifying index
- if `target` exceeds the last value, return `-1`

The sorted, monotonic input is the constraint that drives the algorithmic choice: use binary search, specifically a lower-bound search.

## 🪜 How to Solve This
1. Read the problem → the array is sorted and we need the **first** position meeting a condition, not just whether the value exists.

2. Translate the condition → we want the smallest index where `signups[i] >= target`. That is the textbook definition of a **lower bound**.

3. Ask what monotonic property exists → for any index:
   - if `signups[i] >= target`, then every index to the right also satisfies it
   - if `signups[i] < target`, then every index to the left also fails

4. That monotonic true/false boundary suggests binary search immediately. We are not searching for equality; we are searching for the transition point from “too small” to “good enough.”

5. Maintain a search window over possible answers. At each midpoint:
   - if midpoint already satisfies the target, keep it as a candidate and move left
   - otherwise move right, because no earlier index can work

6. When the window closes, either the saved candidate is the earliest valid day, or no candidate was found and the answer is `-1`.

This framing avoids the common mistake of writing “standard binary search for exact match,” which fails when duplicates exist or when the target is absent but a larger value should still qualify.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Binary Search / Lower Bound.**  
   The array is non-decreasing, and the predicate `signups[i] >= target` is monotonic across indices. There is a boundary between failing indices and satisfying indices. Lower-bound search is the right abstraction because we need the first satisfying position.

2. **Initialize search bounds.**  
   Set `left = 0`, `right = n - 1`, and `answer = -1`.  
   `answer` stores the best valid index seen so far. The invariant is: any index smaller than `left` has already been ruled out or processed; any saved `answer` is valid.

3. **Compute midpoint safely.**  
   Use `mid = left + (right - left) // 2`.  
   This avoids overflow in languages with fixed-width integers and is standard binary-search discipline.

4. **Evaluate the midpoint against the predicate.**  
   If `signups[mid] >= target`, then `mid` is a valid candidate. Record `answer = mid` and shrink rightward search to `right = mid - 1` because an earlier valid index may exist.  
   Invariant: `answer`, if set, always points to a valid day.

5. **Discard impossible regions.**  
   If `signups[mid] < target`, then every index at or left of `mid` also fails, due to sorting. Move `left = mid + 1`.  
   Invariant: no discarded index can be the first valid day.

6. **Terminate when bounds cross.**  
   When `left > right`, the search space is exhausted. If `answer != -1`, it is the smallest index satisfying the condition; otherwise the milestone is never reached.

This is correct because each step preserves the boundary-search invariant while halving the remaining search interval.

## 📊 Worked Example
Example: `signups = [3, 5, 5, 9, 14]`, `target = 6`

| Step | left | right | mid | signups[mid] | Action | answer |
|------|------|-------|-----|--------------|--------|--------|
| 1 | 0 | 4 | 2 | 5 | `< 6`, move right: `left = 3` | -1 |
| 2 | 3 | 4 | 3 | 9 | `>= 6`, record candidate, move left: `right = 2` | 3 |

Search stops because `left = 3` and `right = 2`.

Reasoning:
1. Index `2` is too small, so indices `0..2` cannot work.
2. Index `3` satisfies the target, so it is a valid answer.
3. We try to find an earlier valid index by moving left, but the search space is now empty.

Final result: `3`.

This trace shows the key property: once a midpoint satisfies the target, it is not necessarily the answer until we prove no earlier qualifying index exists.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration halves the remaining search interval, and the only non-constant work is comparing `signups[mid]` with `target`. Even at `10^6` elements, this is about 20 comparisons; at `10^9`, about 30. That predictability is why binary search remains a foundational primitive for read-heavy systems.

### Space Complexity
`O(1)`. The algorithm uses a fixed number of variables: bounds, midpoint, and candidate answer. No auxiliary data structure is allocated. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off is readability versus using a library lower-bound primitive.

## 💡 Key Takeaways
- If the input is sorted and the question asks for the **first index** where a condition becomes true, think lower-bound binary search.
- Monotonic predicates like `value >= target` are stronger signals than exact-match wording; they define a boundary, not a lookup.
- Do not stop when you first find a qualifying value; duplicates mean you must continue searching left.
- Be explicit about the “not found” case when all values are smaller than `target`; otherwise off-by-one logic often returns an invalid index.
- In production systems, cumulative or monotonic metrics enable logarithmic threshold queries, which materially changes the scalability of analytics and policy-evaluation paths.

## 🚀 Variations & Further Practice
- Find the **last** day with signups `<= target` or the last occurrence of a value; same pattern, but you search for the upper boundary instead of the lower one.
- Given many milestone queries against the same cumulative array, optimize for batch processing or API design; the conceptual twist is amortization and query-serving strategy rather than single-query correctness.
- Search in a rotated sorted array or an array with unknown monotonic segments; the harder twist is that the global ordering assumption is weakened, so boundary reasoning becomes conditional.