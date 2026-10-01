# Maximum Free Days After Canceling One Booking Block

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Greedy

---

## 🗂 Problem Overview
Given a binary array `booked`, return the longest contiguous stretch of `0`s obtainable after canceling at most one contiguous subarray consisting only of `1`s. You may cancel an entire booked run or any sub-block inside it, or skip cancellation entirely. The challenge is that `n` can reach `200000`, so brute-force enumeration of all cancelable intervals is too expensive; the solution must exploit run structure and local adjacency.

## 🌍 Engineering Impact
This pattern shows up anywhere a binary timeline models capacity, availability, or suppression windows: reservation systems, ad-serving blackout periods, maintenance windows in SRE tooling, CPU scheduling gaps, and streaming backfill planners. At scale, the wrong approach degenerates into quadratic scans over intervals or repeated recomputation of contiguous capacity. The right abstraction compresses the problem into run boundaries and local merge effects, enabling linear-time planning over large horizons. That matters when these computations sit inside interactive schedulers, optimization loops, or admission-control systems where latency and predictability are as important as correctness.

## 🔍 Problem Statement
You are given `n` days, labeled `1..n`, and a binary array `booked` of length `n`. `booked[i] = 1` means day `i + 1` is booked; `0` means free. You may cancel at most one contiguous block `booked[l..r]`, but only if every element in that block is `1`. After optionally turning that block into `0`s, return the maximum length of any contiguous run of free days.

Key details:
- `1 <= n <= 200000`
- `booked[i] ∈ {0, 1}`
- The canceled block must lie within a single existing run of `1`s
- Canceling only part of a run is allowed, though not always useful
- You may choose not to cancel anything

Examples:
- `booked = [0,1,1,0,0,1,0]` → `5`
- `booked = [1,0,1,1,1,0,1]` → `5`

The input size rules out trying every booked interval; the algorithm must be linear or close to it.

## 🪜 How to Solve This
1. Read the operation carefully → you can only cancel inside one existing run of `1`s. That means the array’s behavior is determined by alternating runs of `0`s and `1`s, not by arbitrary subarrays.

2. Ask what cancellation actually changes → it converts some consecutive `1`s into `0`s. If you cancel inside a run of `1`s`, the new free interval can connect the free run on the left, the canceled segment, and the free run on the right.

3. Notice the optimization target → we care about the longest final run of `0`s, not the number of bookings removed. So for any chosen `1`-run, canceling more is never worse; canceling the entire run maximizes the bridge between adjacent zero-runs.

4. That reduces the problem to: for each maximal run of `1`s`, compute  
   `leftZeroRun + oneRunLength + rightZeroRun`.

5. Also handle the “do nothing” case → if the array already has the best free run, or there are no `1`s, the answer is just the longest existing zero-run.

6. This suggests a single pass over runs, maintaining lengths of neighboring zero-runs and evaluating each `1`-run in O(1). Total cost: O(n).

## 🧩 Algorithm Walkthrough
1. **Compress the array into runs**  
   Use a linear scan to identify maximal contiguous runs of equal values. For each run, record its value (`0` or `1`) and length. This is the right abstraction because the operation cannot cross run boundaries; only run lengths matter, not individual positions.

2. **Track the baseline answer**  
   While building runs, update the longest existing `0`-run. This preserves the invariant that the answer is valid even if we choose not to cancel anything.

3. **Evaluate each `1`-run as a cancellation candidate**  
   For a run of `1`s at position `i` in the run list, inspect adjacent runs:
   - `left = runs[i-1].length` if `i-1` exists and is a `0`-run, else `0`
   - `right = runs[i+1].length` if `i+1` exists and is a `0`-run, else `0`
   - candidate free run = `left + runs[i].length + right`

   This is correct because canceling the full `1`-run turns the entire middle segment into free days, merging with both neighboring free runs if present.

4. **Why full cancellation is always optimal**  
   Any partial cancellation inside the same `1`-run creates a free segment no longer than canceling the entire run. Since adjacent zero-runs are fixed, maximizing the canceled middle length maximizes the merged free interval.

5. **Return the maximum over all candidates and baseline**  
   The invariant after processing each run is: `ans` equals the best achievable longest free interval considering all runs seen so far.

This is a **run-length encoding + greedy local merge** pattern. Greedy is valid because each `1`-run can be optimized independently, and the best local choice for that run is trivially to cancel all of it.

## 📊 Worked Example
Take `booked = [0,1,1,0,0,1,0]`.

Run decomposition:

| Run Index | Value | Length | Left 0-run | Right 0-run | Candidate |
|---|---:|---:|---:|---:|---:|
| 0 | 0 | 1 | - | - | baseline = 1 |
| 1 | 1 | 2 | 1 | 2 | 1 + 2 + 2 = 5 |
| 2 | 0 | 2 | - | - | baseline = 2 |
| 3 | 1 | 1 | 2 | 1 | 2 + 1 + 1 = 4 |
| 4 | 0 | 1 | - | - | baseline = 2 |

Trace:
1. Longest existing free run is `2`.
2. Cancel run 1 (`[1,1]`) → merges left `0`, canceled `11`, right `00` into `00000`, length `5`.
3. Cancel run 3 (`[1]`) → merges `00`, `1`, `0` into length `4`.
4. Best result is `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. The dominant work is one linear scan to build or process runs, with O(1) work per element and per run. At `10^6` elements this is routine in memory-resident code; at `10^9`, linear time is still expensive but remains the only viable asymptotic class for batch processing.

### Space Complexity
`O(n)` in the explicit run-list implementation, owned by the run-length encoding structure. This can be reduced to `O(1)` auxiliary space by streaming runs and keeping only neighboring run lengths, at the cost of slightly more stateful logic.

## 💡 Key Takeaways
• If an operation is constrained to contiguous equal-valued segments, stop thinking in raw indices and start thinking in runs.  
• When the objective is the longest merged interval after one edit, look for “left contribution + edited segment + right contribution.”  
• Do not overcomplicate partial cancellation: within a single `1`-run, canceling less can never produce a longer free interval than canceling all of it.  
• Be careful at boundaries: missing left or right zero-runs contribute `0`, not an invalid access or negative length.  
• In production systems, interval optimization often becomes tractable once you compress state transitions and reason over segment boundaries instead of individual events.

## 🚀 Variations & Further Practice
- Allow canceling up to `k` booked blocks instead of one. The twist is that local independence disappears; you now need DP or sliding-window logic over runs.
- Assign each booked day a cancellation cost and maximize free-run length under a budget. The harder part is balancing interval length against weighted feasibility.
- Make the timeline circular rather than linear. The conceptual challenge is handling wraparound runs and merged boundary segments without double-counting.