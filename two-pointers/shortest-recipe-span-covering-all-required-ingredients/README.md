# Shortest Recipe Span Covering All Required Ingredients

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given an ordered array `ingredients` and a distinct set `required`, find the length of the shortest contiguous subarray containing every required ingredient at least once. Return `-1` if no such span exists. The challenge is scale: with arrays up to `200000` elements, enumerating and validating all subarrays is prohibitively expensive, so the solution must maintain coverage incrementally while scanning once.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the smallest contiguous segment satisfying a coverage constraint. Examples include log analysis windows containing all target event types, search snippets covering all query terms, genomic sequence spans containing required markers, and streaming observability pipelines detecting minimal incident contexts. At scale, brute-force window enumeration explodes quadratically and becomes unusable under latency SLOs. Sliding-window designs enable single-pass processing, bounded state, and predictable memory behavior. Architecturally, this is the difference between an online algorithm that can run in hot paths and an offline batch job that misses operational relevance.

## 🔍 Problem Statement
You are given:

- `ingredients`: an array of non-empty lowercase strings, length `1..200000`
- `required`: an array of distinct non-empty lowercase strings, length `1..200000`

Return the length of the shortest contiguous span in `ingredients` that contains every string in `required` at least once. If no such span exists, return `-1`.

Important details:

- `ingredients` may contain duplicates.
- Order inside the chosen span does not matter.
- The span itself must remain contiguous in the original array.
- `required` contains distinct values.
- `required.length <= ingredients.length`

Examples:

```text
Input:  ingredients = ["salt","oil","garlic","onion","salt","tomato","basil"]
        required    = ["garlic","salt","tomato"]
Output: 4
```

```text
Input:  ingredients = ["flour","egg","milk","butter","egg"]
        required    = ["egg","yeast"]
Output: -1
```

The key constraint is input size: any solution worse than linear or near-linear time will fail at upper bounds.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is not a set-membership problem alone; it is a window problem.

2. We need the **shortest** segment satisfying a condition → that strongly suggests trying to grow a window until valid, then shrink it as much as possible.

3. What makes a window valid? → it must contain every ingredient in `required` at least once. That implies tracking counts of required items currently inside the window.

4. Distinct `required` values simplify the condition → we do not need target multiplicities, only presence/absence. So a hash set for membership and a hash map for in-window counts are enough.

5. Move the right pointer forward → each new ingredient may help satisfy the requirement.

6. Once all required ingredients are covered → move the left pointer forward while preserving validity. This is where the minimum is found.

7. Why this works → every index enters and leaves the window at most once. Instead of rechecking every subarray, we maintain the current state incrementally.

That chain leads directly to a standard sliding-window / two-pointers solution in linear time.

## 🧩 Algorithm Walkthrough
1. **Initialize lookup structures.**  
   Build a hash set from `required` for O(1) membership checks. Maintain a hash map `windowCount` for counts of required ingredients inside the current window. Also track `formed`, the number of required ingredients currently present with count `> 0`.  
   **Invariant:** `formed` accurately reflects how many required tokens are covered by the window.

2. **Expand with the right pointer.**  
   Iterate `right` from `0` to `ingredients.length - 1`. If `ingredients[right]` is required, increment its count in `windowCount`. If its count changes from `0` to `1`, increment `formed`.  
   **Why correct:** adding a token can only improve or preserve coverage; it never invalidates existing coverage.

3. **Check window validity.**  
   Whenever `formed == required.length`, the current window `[left, right]` contains all required ingredients at least once. Record its length as a candidate minimum.  
   **Invariant:** every candidate recorded is valid by construction.

4. **Shrink with the left pointer.**  
   While the window remains valid, advance `left` to remove unnecessary prefix elements. If `ingredients[left]` is not required, discard it immediately. If it is required, decrement its count; if the count drops from `1` to `0`, decrement `formed` and stop shrinking.  
   **Why correct:** this finds the smallest valid window ending at `right`.

5. **Repeat until traversal completes.**  
   Because both pointers only move forward, total work is linear.  
   **Pattern:** this is the canonical **Two Pointers / Sliding Window** abstraction: maintain a contiguous region and update validity incrementally instead of recomputing from scratch.

6. **Return result.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum length recorded.

## 📊 Worked Example
Example: `ingredients = ["salt","oil","garlic","onion","salt","tomato","basil"]`, `required = ["garlic","salt","tomato"]`

| Step | right | token    | left | formed | Valid? | Best |
|------|-------|----------|------|--------|--------|------|
| 1    | 0     | salt     | 0    | 1      | No     | ∞    |
| 2    | 1     | oil      | 0    | 1      | No     | ∞    |
| 3    | 2     | garlic   | 0    | 2      | No     | ∞    |
| 4    | 3     | onion    | 0    | 2      | No     | ∞    |
| 5    | 4     | salt     | 0    | 2      | No     | ∞    |
| 6    | 5     | tomato   | 0    | 3      | Yes    | 6    |
| 7    | shrink| remove salt at 0, still valid | 1 | 3 | Yes | 5 |
| 8    | shrink| remove oil at 1, still valid  | 2 | 3 | Yes | 4 |
| 9    | shrink| remove garlic at 2, invalid   | 3 | 2 | No  | 4 |

Best window is indices `[2..5]`: `["garlic","onion","salt","tomato"]`, length `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = ingredients.length` and `m = required.length`. Building the required set costs `O(m)`, and the sliding window scans `ingredients` once; each element is added and removed at most one time. At `10^6` scale this is practical; at `10^9`, linear time is still expensive but remains the only viable asymptotic class.

### Space Complexity
`O(m)` auxiliary space for the required set and the count map over required ingredients. Space is owned by hash-based lookup structures, not the window itself. It cannot be meaningfully reduced below `O(m)` without sacrificing constant-time membership or count updates.

## 💡 Key Takeaways
- If the problem asks for a **shortest contiguous segment** satisfying a coverage condition, sliding window should be your first candidate.
- If validity depends on **presence/counts of a small tracked subset**, a hash set plus hash map usually replaces nested scans.
- Be careful with window length: for inclusive indices, the span size is `right - left + 1`, not `right - left`.
- Only decrement `formed` when a required ingredient’s count drops from `1` to `0`; decrementing on every removal is a correctness bug.
- The production-grade insight is incremental state maintenance: recompute nothing you can update locally as the window shifts.

## 🚀 Variations & Further Practice
- **Minimum window substring / token span with multiplicities**: required items may need specific counts, not just presence. The twist is tracking exact deficits rather than binary coverage.
- **Smallest span covering all categories in a stream**: inputs may arrive online and windows may need to be emitted continuously under memory constraints.
- **Shortest span after allowing up to `k` missing required items**: validity becomes approximate, forcing a different invariant and more careful shrink logic.