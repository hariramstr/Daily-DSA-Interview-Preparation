# Maximum Consecutive Days After Repairing One Missing Report

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sorting, Hash Set

---

## 🗂 Problem Overview
Given an unsorted array of distinct positive day numbers, compute the longest consecutive streak obtainable after adding at most one missing day not already present. The output is a single integer: the maximum streak length after zero or one repair. The challenge is scale: with up to `2 * 10^5` values and day numbers up to `10^9`, brute-force gap testing or interval expansion around every candidate is too expensive unless the data is organized efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere sparse event timestamps must be converted into contiguous availability windows: health-check ingestion, log completeness validation, CDC pipelines, fraud/session reconstruction, and observability backfill tooling. At scale, the difference between “check every possible repair” and “reason over sorted runs” is the difference between linearithmic preprocessing and quadratic collapse. The useful abstraction is interval stitching: detect maximal consecutive segments, then ask whether one synthetic event can extend a segment or bridge two adjacent segments. That same framing drives production systems that reconcile partial telemetry, repair missing sequence numbers, or infer near-contiguous service uptime from incomplete records.

## 🔍 Problem Statement
You are given `days`, an array of distinct positive integers representing successful report-upload days. The values are unsorted. You may add at most one new day value that is not already present, treating it as a repaired missing report. Return the maximum length of any consecutive streak after that optional repair.

A consecutive streak contains integers with no gaps, e.g. `[10, 11, 12, 13]` has length `4`.

Constraints:

- `1 <= days.length <= 2 * 10^5`
- `1 <= days[i] <= 10^9`
- All `days[i]` are distinct

Examples:

- `days = [8, 4, 5, 10, 6]` → best is repairing `7`, forming `[4, 5, 6, 7, 8]`, answer `5`
- `days = [1, 2, 4, 7, 8]` → repairing `3` forms `[1, 2, 3, 4]`, answer `4`

The key constraint is input size: any approach worse than `O(n log n)` is risky, and value magnitude rules out dense arrays indexed by day number.

## 🪜 How to Solve This
1. Read the problem → the target is not individual numbers, but **consecutive runs**. Once you see “maximum consecutive streak,” think in terms of intervals.
2. Because values are distinct but unsorted, sort first. After sorting, every useful decision is local: only adjacent values determine whether a run continues or a gap appears.
3. Scan the sorted array and compress it into maximal runs like `[4,5,6]` or `[8]`. Each run has a start, end, and length.
4. Now ask what one repair can do:
   - extend a run by `1` at either side if no adjacent value already exists,
   - or bridge two runs if the gap between them is exactly `2` (one missing day in the middle).
5. So the answer is the maximum of:
   - existing run length,
   - run length `+ 1`,
   - left run length `+ 1 +` right run length when two runs are separated by exactly one missing value.
6. This avoids testing arbitrary inserted days. The only meaningful repair positions are at run boundaries or the single hole between two nearly-adjacent runs.

## 🧩 Algorithm Walkthrough
1. **Sort the array** using the standard **Sorting + Interval Compression** pattern.  
   Why: consecutive structure is invisible in arbitrary order. After sorting, every gap is represented by `days[i] - days[i - 1]`.  
   Invariant: processed prefix is in ascending order, so run boundaries are unambiguous.

2. **Build maximal consecutive runs** by scanning once.  
   If `days[i] == days[i - 1] + 1`, stay in the current run; otherwise, close the current run and start a new one.  
   Why: every streak in the final answer is composed of one or two adjacent runs plus possibly one repaired day.  
   Invariant: runs are disjoint, sorted, and maximal.

3. **Initialize the answer with the longest existing run.**  
   Why: repair is optional, and some inputs may already have the optimal streak without insertion.  
   Invariant: `ans` is always a valid achievable streak length.

4. **Consider extending each run by one day.**  
   Any non-empty run can be extended to length `runLen + 1` by inserting `start - 1` or `end + 1`, unless that day already belongs to a neighboring run. But maximal runs guarantee at least one side is free unless the array spans all integers around it, which cannot happen without merging runs.  
   So each run contributes candidate `runLen + 1`.  
   Invariant: single-run repair cases are covered.

5. **Consider bridging adjacent runs with one missing day.**  
   For neighboring runs `[a..b]` and `[c..d]`, if `c - b == 2`, then inserting `b + 1` merges them into one streak of length `(b - a + 1) + 1 + (d - c + 1)`.  
   Why: exactly one hole exists; one repair closes it completely.  
   Invariant: all two-run merge cases are covered, and only exact-gap-2 pairs are eligible.

6. **Return the maximum candidate.**  
   This is correct because any optimal repaired streak must either stay within one run plus one extension, or merge exactly two runs across a single missing value. Larger gaps cannot be fixed with one insertion.

## 📊 Worked Example
Take `days = [1, 2, 4, 7, 8]`.

After sorting: `[1, 2, 4, 7, 8]`

| Step | Current value | Action | Runs so far | Best |
|---|---:|---|---|---:|
| 1 | 1 | start run `[1..1]` | `[1..1]` | 1 |
| 2 | 2 | consecutive, extend | `[1..2]` | 2 |
| 3 | 4 | gap, close previous | `[1..2], [4..4]` | 2 |
| 4 | 7 | gap, close previous | `[1..2], [4..4], [7..7]` | 2 |
| 5 | 8 | consecutive, extend | `[1..2], [4..4], [7..8]` | 2 |

Now evaluate repairs:

- Extend `[1..2]` → length `3`
- Bridge `[1..2]` and `[4..4]` since `4 - 2 = 2` → insert `3`, total `2 + 1 + 1 = 4`
- Extend `[7..8]` → length `3`

Maximum is `4`, achieved by repairing day `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting; the subsequent run-building and candidate evaluation scans are linear. For `10^6` elements, this remains practical in most languages with in-place sort. At `10^9` scale, the issue is no longer algorithmic complexity but memory and machine limits; the asymptotic shape is still the right one.

### Space Complexity
`O(n)` if you materialize all runs explicitly, or `O(1)` auxiliary beyond the sort if you compute run lengths on the fly during a single scan. The owning cost is the sorted array itself. Reducing extra space is straightforward, but usually not worth complicating the implementation.

## 💡 Key Takeaways
- If the problem says “longest consecutive sequence” on unsorted distinct integers, the first signal is to transform the data into ordered runs rather than compare every pair.
- If one modification is allowed, ask whether it can only extend one segment or also bridge two segments; that framing usually collapses the search space.
- The bridge condition is exact: two runs merge only when the gap between their boundaries is `2`, meaning there is exactly one missing integer.
- Do not forget that the repair is optional; initialize from the best existing run before considering any insertion.
- At system scale, sparse event repair is usually an interval-stitching problem, not a point-by-point simulation problem.

## 🚀 Variations & Further Practice
- Allow repairing up to `k` missing days. The twist is that local pairwise bridging no longer suffices; this becomes a sliding-window or gap-budget problem over sorted values.
- Allow duplicate day values in the input. The harder part is normalizing noisy telemetry before interval reasoning, while preserving performance.
- Return the actual repaired day and resulting interval, not just the length. The conceptual shift is from pure optimization to optimization plus deterministic reconstruction.