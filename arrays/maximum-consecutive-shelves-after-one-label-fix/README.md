# Maximum Consecutive Shelves After One Label Fix

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Prefix/Suffix

---

## 🗂 Problem Overview
Given an array `labels`, find the maximum length of a contiguous segment that can become a strictly consecutive sequence with difference `+1` after changing at most one value. You may replace one element with any positive integer, but the relative order of positions must remain unchanged. The challenge is scale: with up to `200000` elements, checking every subarray or simulating every possible edit is too expensive, so the solution must reason about local consistency in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere a mostly-ordered stream may contain a single corruption: event sequence numbers in streaming pipelines, log offsets in ingestion systems, shard-local IDs in storage engines, packet counters in telemetry, or version chains in replication protocols. At scale, brute-force validation of every interval is not viable; you need incremental structure that detects whether a long run is recoverable with bounded repair. The underlying decision is architectural: do you recompute validity from scratch, or maintain enough local state to extend and merge runs cheaply? The latter is what keeps hot-path validation linear and predictable under large inputs.

## 🔍 Problem Statement
You are given an integer array `labels` where `labels[i]` is the printed label on the `i`-th shelf. A contiguous subarray is considered fixable if, after changing at most one element inside that subarray to any positive integer, the segment becomes:

`[x, x+1, x+2, ..., x+len-1]`

for some positive integer `x`.

Return the maximum possible length of such a contiguous subarray.

Constraints:

- `1 <= labels.length <= 200000`
- `1 <= labels[i] <= 1000000000`
- At most one value may be changed in the chosen subarray
- The replacement must be a positive integer

Examples:

- `labels = [4, 5, 9, 7, 8]` → `5`
- `labels = [10, 11, 13, 14, 20]` → `4`

The key constraint is the input size. Any `O(n^2)` enumeration of subarrays is already too slow, so the solution must exploit local consecutive structure and combine it in `O(n)` time.

## 🪜 How to Solve This
1. Start with the unmodified condition: a segment is already consecutive when every adjacent difference is exactly `1`. That immediately suggests tracking runs where `labels[i] - labels[i-1] == 1`.

2. Now add the “one fix” allowance. A valid segment with one edit can look like:
   - a fully valid run,
   - a valid run plus one bad endpoint we overwrite,
   - or two valid runs separated by exactly one position we overwrite to bridge them.

3. That means the real object to compute is not every subarray, but the length of consecutive runs ending at each index and starting at each index.

4. Build:
   - `left[i]` = length of the longest exact `+1` run ending at `i`
   - `right[i]` = length of the longest exact `+1` run starting at `i`

5. For each index, treat it as the modified shelf. The best segment through that index is:
   - extend from the left run,
   - extend from the right run,
   - or merge both if one replacement value can satisfy both neighbors.

6. The merge condition is forced, not guessed: if replacing `labels[i]` must sit between `labels[i-1]` and `labels[i+1]`, then we need `labels[i+1] - labels[i-1] == 2`, and the implied value must stay positive.

Once you see the problem as “merge exact runs through one repair point,” the linear solution becomes straightforward.

## 🧩 Algorithm Walkthrough
1. **Compute prefix run lengths (`left`)**  
   Use a forward pass. Set `left[0] = 1`. For each `i > 0`, if `labels[i] == labels[i-1] + 1`, then `left[i] = left[i-1] + 1`; otherwise reset to `1`.  
   **Invariant:** `left[i]` is the length of the longest exact consecutive suffix ending at `i`.

2. **Compute suffix run lengths (`right`)**  
   Use a backward pass. Set `right[n-1] = 1`. For each `i < n-1`, if `labels[i+1] == labels[i] + 1`, then `right[i] = right[i+1] + 1`; otherwise reset to `1`.  
   **Invariant:** `right[i]` is the length of the longest exact consecutive prefix starting at `i`.

3. **Initialize answer with exact runs**  
   Even without any modification, any exact run is valid. So the maximum value in `left` is a lower bound.

4. **Treat each index as the edited position**  
   This is the core **Prefix/Suffix run merging** pattern. For index `i`, consider three cases:
   - replace `labels[i]` and keep only the left side → length `left[i-1] + 1`
   - replace `labels[i]` and keep only the right side → length `1 + right[i+1]`
   - replace `labels[i]` to bridge both sides → possible only if `labels[i+1] - labels[i-1] == 2`; then length is `left[i-1] + 1 + right[i+1]`

5. **Handle boundaries carefully**  
   At `i = 0` or `i = n-1`, only one side exists. Replacing an endpoint can always extend the adjacent exact run by one, subject to array bounds.

6. **Respect positivity implicitly**  
   The bridge value is `labels[i-1] + 1`. Since all labels are positive, any bridge between two positive neighbors remains positive. Endpoint replacements can always choose a positive value.

This works because any fixable segment with one edit has at most one “broken” position; everything else must already form exact `+1` runs on each side.

## 📊 Worked Example
Take `labels = [10, 11, 13, 14, 20]`.

| i | labels[i] | left[i] | right[i] |
|---|-----------|---------|----------|
| 0 | 10        | 1       | 2        |
| 1 | 11        | 2       | 1        |
| 2 | 13        | 1       | 2        |
| 3 | 14        | 2       | 1        |
| 4 | 20        | 1       | 1        |

Trace candidate edits:

1. `i = 0`: replace `10` to extend right run → `1 + right[1] = 2`
2. `i = 1`: left=`1`, right=`right[2]=2`; bridge needs `13 - 10 = 3`, so no full merge. Best = `max(2, 3) = 3`
3. `i = 2`: left=`left[1]=2`, right=`right[3]=1`; bridge needs `14 - 11 = 3`, so no merge. Best = `max(3, 2) = 3`
4. `i = 3`: left=`1`, right=`1`; bridge needs `20 - 13 = 7`, no merge
5. `i = 4`: replace endpoint → `left[3] + 1 = 3`

Also consider exact runs and endpoint extension around `[10,11,13,14]`: changing `13 -> 12` yields length `4`, which is the maximum.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. There are three linear passes: one to build `left`, one to build `right`, and one to evaluate each index as the potential repaired position. No nested scanning occurs. At `10^6` elements this remains practical; at `10^9`, even linear time becomes a systems problem dominated by memory bandwidth and input handling.

### Space Complexity
`O(n)` for the `left` and `right` arrays. That space is owned entirely by prefix/suffix run metadata. It can be reduced with more careful streaming logic, but doing so usually increases implementation complexity and makes correctness around merge cases harder to audit.

## 💡 Key Takeaways
- If a problem asks for the longest contiguous segment that becomes valid after one local repair, look for “prefix valid run + repaired point + suffix valid run.”
- When the validity rule is defined by adjacent relationships, precomputing maximal runs from both directions is often cleaner than maintaining a complex sliding-window state.
- The bridge condition is exact: merging both sides through index `i` requires `labels[i+1] - labels[i-1] == 2`, not merely “close enough.”
- Endpoint edits are easy to undercount; replacing the first or last element can extend an adjacent exact run by one even when no two-sided merge exists.
- At scale, the transferable design pattern is to convert expensive interval validation into constant-time composition over precomputed local invariants.

## 🚀 Variations & Further Practice
- Allow up to `k` edits instead of one. The twist is that simple prefix/suffix merging no longer suffices; you need a window over “bad transitions” or DP over repair budget.
- Require the replacement value to come from a bounded range or from values already present. The harder part is that a structurally fixable bridge may become infeasible under domain constraints.
- Generalize from step `+1` to arbitrary fixed difference `d` or even per-query `d`. The conceptual shift is from exact consecutive runs to affine runs, which changes the merge predicate and preprocessing strategy.