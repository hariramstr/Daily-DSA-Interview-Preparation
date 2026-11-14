# Maximum Consecutive Days After Rebooking One Hotel Stay

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Prefix/Suffix

---

## 🗂 Problem Overview
Given a strictly increasing array `bookedDays`, each value is a reserved calendar day. You may move at most one reservation to any other positive day not already present. The goal is to maximize the length of the longest consecutive run in the final set. The challenge is deciding whether a single moved day should extend an existing run, bridge two runs separated by one gap, or be left unchanged. With up to `2 * 10^5` elements, quadratic gap testing is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a mostly ordered set must be improved with one corrective mutation: compaction windows in storage engines, sequence repair in event streams, sparse time-series normalization, and shard-range healing in distributed schedulers. At scale, the wrong approach degenerates into repeated local recomputation over long sorted spans, which is exactly how latency cliffs appear in control planes and analytics backfills. The useful abstraction is not “try every move,” but “reason over contiguous segments and gaps.” That shift enables linear scans, predictable memory use, and logic that remains stable as cardinality grows from thousands to millions.

## 🔍 Problem Statement
You are given a strictly increasing integer array `bookedDays` where:

- `1 <= bookedDays.length <= 200000`
- `1 <= bookedDays[i] <= 1000000000`

Each value is a unique reserved day. You may rebook at most one reservation: remove one existing day and insert a different positive integer day not already in the set. After this optional move, return the maximum possible length of any consecutive-day streak.

Examples:

- `bookedDays = [2, 4, 5, 7]` → `4`  
  Move `7 -> 3`, producing `{2,3,4,5}`.

- `bookedDays = [10, 11, 14, 15, 16, 20]` → `4`  
  Move `20 -> 13`, producing `{10,11,13,14,15,16}`; best streak is `13..16`.

The key constraint is input size. Since the array is already sorted and unique, the real problem is analyzing consecutive segments and the gaps between them in `O(n)` or `O(n log n)`, not simulating every possible rebooking.

## 🪜 How to Solve This
1. Read the array as **runs of consecutive days**, not individual values.  
   If `bookedDays[i] == bookedDays[i-1] + 1`, the current streak continues; otherwise a new streak starts.

2. Once you think in runs, only a few useful moves remain:  
   → extend a run by inserting one missing adjacent day  
   → bridge two runs if the gap is exactly one missing day  
   → avoid breaking the run you stole a day from unless that run has spare length

3. That immediately suggests precomputing streak lengths around each index.  
   A prefix-style pass gives the length of the consecutive run ending at `i`; a suffix-style pass gives the run starting at `i`.

4. For each gap between `i` and `i+1`, inspect its size:  
   - gap `== 1` means already consecutive  
   - gap `== 2` means one inserted day can merge both sides  
   - gap `> 2` means you can only extend one side by one

5. The subtle part is donor availability. If you move a day from elsewhere, some other reservation must exist outside the merged segment, or you must take from a run of length greater than one so the target streak survives as computed.

6. That yields a linear greedy scan over gaps, backed by prefix/suffix run lengths.

## 🧩 Algorithm Walkthrough
1. **Compute consecutive-run lengths from the left.**  
   Let `left[i]` be the length of the consecutive streak ending at index `i`.  
   If `bookedDays[i] == bookedDays[i-1] + 1`, then `left[i] = left[i-1] + 1`; otherwise `1`.  
   Invariant: `left[i]` always equals the exact size of the maximal suffix-run ending at `i`.

2. **Compute consecutive-run lengths from the right.**  
   Let `right[i]` be the length of the consecutive streak starting at index `i`.  
   If `bookedDays[i+1] == bookedDays[i] + 1`, then `right[i] = right[i+1] + 1`; otherwise `1`.  
   Invariant: `right[i]` is the exact size of the maximal prefix-run starting at `i`.

3. **Initialize the answer with the best existing run.**  
   Without any move, the answer is `max(left[i])`. This handles already-optimal inputs and `n = 1`.

4. **Evaluate each boundary between `i` and `i+1`.**  
   Let `gap = bookedDays[i+1] - bookedDays[i]`.  
   This is the core **prefix/suffix + greedy gap analysis** pattern: every useful move is localized around a gap.

5. **If `gap == 2`, one missing day bridges both runs.**  
   Candidate merged length is `left[i] + right[i+1]`.  
   This is valid because inserting the single missing day creates one continuous streak across the boundary.

6. **If `gap > 2`, you cannot merge both sides with one move.**  
   You can only extend either side by one:  
   `left[i] + 1` or `right[i+1] + 1`.  
   This captures placing the moved reservation adjacent to an existing run.

7. **Cap by total element count.**  
   Since the final set still contains exactly `n` reservations, every candidate is bounded by `n`.  
   This also avoids overcounting in cases where a conceptual “borrow from elsewhere” argument would exceed available days.

8. **Return the maximum candidate seen.**  
   Correctness follows because any optimal single move must either bridge one one-day hole or extend one run at one edge; no other move can create a longer consecutive block than those local transformations.

## 📊 Worked Example
Take `bookedDays = [2, 4, 5, 7]`.

| i | day | left[i] | right[i] |
|---|-----|---------|----------|
| 0 | 2   | 1       | 1        |
| 1 | 4   | 1       | 2        |
| 2 | 5   | 2       | 1        |
| 3 | 7   | 1       | 1        |

Now inspect gaps:

1. Between `2` and `4`: `gap = 2`  
   One missing day (`3`) bridges the runs.  
   Candidate = `left[0] + right[1] + 1?`  
   In set terms, inserting `3` joins `{2}` with `{4,5}` into `{2,3,4,5}` → length `4`.

2. Between `5` and `7`: `gap = 2`  
   Insert `6` to join `{4,5}` with `{7}` → `{4,5,6,7}` → length `4`.

Best answer: `4`.

The useful mental model is that each `gap == 2` is a one-day hole; each larger gap is too wide to bridge with one rebooking.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One left-to-right pass builds prefix run lengths, one right-to-left pass builds suffix run lengths, and one final pass evaluates every gap once. There is no nested exploration of moves. At `10^6` elements this remains practical; at `10^9`, the algorithmic form is still optimal but memory and input transport dominate.

### Space Complexity
`O(n)` for the `left` and `right` arrays storing run lengths. This can be reduced with more stateful scanning, but the code becomes less transparent and easier to get wrong around bridge cases and boundary handling.

## 💡 Key Takeaways
- If the input is already sorted and unique, look for a representation in terms of contiguous runs and gaps rather than element-by-element simulation.
- Prefix/suffix arrays are a strong signal when each decision depends on the maximal structure immediately to the left and right of a boundary.
- The main off-by-one trap is distinguishing `gap == 2` from `gap > 2`: only the former is a single missing day that can truly bridge two runs.
- Be careful not to overcount merged length when reasoning about “moving one day”; the final set size is still exactly `n`.
- In production systems, local boundary analysis often beats global recomputation: model mutations as changes to segment structure, not arbitrary edits to the whole dataset.

## 🚀 Variations & Further Practice
- Allow rebooking up to `k` reservations instead of one. The twist is that isolated gap decisions become a budget-allocation problem across many segments.
- Make the input unsorted with possible duplicates. The harder part is normalizing into a unique ordered set before applying the same gap logic.
- Return the actual rebooking operation, not just the maximum streak length. This adds reconstruction and tie-breaking requirements on top of the optimization pass.