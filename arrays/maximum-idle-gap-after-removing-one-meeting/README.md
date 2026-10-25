# Maximum Idle Gap After Removing One Meeting

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Prefix/Suffix

---

## 🗂 Problem Overview
Given a workday length and a sorted, non-overlapping list of meetings, compute the largest continuous idle interval obtainable after canceling at most one meeting. The output is a single integer: the maximum free block length anywhere in `[0, dayLength)`. The non-trivial part is scale: with up to `2 * 10^5` meetings, you cannot recompute merged gaps for every possible cancellation using nested scans.

## 🌍 Engineering Impact
This pattern shows up anywhere scheduled occupied intervals constrain throughput: cluster maintenance windows, ad-slot allocation, CPU reservation calendars, streaming pipeline backfill slots, and warehouse dock scheduling. The operational question is often not “what is free now?” but “what is the largest reclaimable contiguous capacity if one reservation is dropped?” At small scale, brute force works; at production scale, repeated interval recomputation becomes a latency and cost problem. The prefix/suffix mindset matters because it turns “evaluate every removal” from repeated global work into constant-time local decisions after one linear preprocessing pass.

## 🔍 Problem Statement
You are given `dayLength` and two arrays `start` and `end` of length `n`, where meeting `i` occupies `[start[i], end[i])`. Meetings are already sorted and non-overlapping, so `end[i] <= start[i + 1]`. You may remove at most one entire meeting, or remove none.

Return the maximum length of any continuous idle interval after that choice. Idle time may occur before the first meeting, between consecutive remaining meetings, or after the last meeting.

Key constraints:

- `1 <= n <= 2 * 10^5`
- `1 <= dayLength <= 10^9`
- `0 <= start[i] < end[i] <= dayLength`

Examples:

- `dayLength = 20, start = [2, 6, 12], end = [4, 9, 15]` → `11`
- `dayLength = 10, start = [1, 3, 7], end = [2, 5, 8]` → `5`

The algorithmic driver is the input size: evaluating each cancellation must be `O(1)` after linear preprocessing.

## 🪜 How to Solve This
1. Read the problem → removing one meeting does not rearrange anything. That means only local structure changes around the removed interval.

2. Ask what cancellation actually does → if you remove meeting `i`, the idle block on its left, the meeting duration itself, and the idle block on its right merge into one continuous interval.

3. So define the adjacent gaps explicitly:
   - left gap before meeting `i`
   - right gap after meeting `i`

4. Then the merged idle block from removing `i` is:
   `leftGap[i] + duration[i] + rightGap[i]`

5. Also remember “remove none” is legal, so the answer must be at least the largest existing idle gap.

6. Because meetings are sorted and non-overlapping, every needed quantity is computable from neighbors only. No search structure, no interval tree, no resorting.

7. This naturally leads to a linear scan:
   - compute all existing gaps
   - evaluate each meeting’s merged gap
   - take the maximum

The key realization is that cancellation affects exactly one contiguous region. Once you see that locality, the problem collapses from “simulate schedules” to “scan adjacent differences.”

## 🧩 Algorithm Walkthrough
1. **Model the free gaps between occupied intervals** using an array perspective.  
   Define the idle gap before meeting `i` as:
   - `start[0]` for `i = 0`
   - `start[i] - end[i - 1]` otherwise  
   Define the idle gap after meeting `i` as:
   - `dayLength - end[n - 1]` for `i = n - 1`
   - `start[i + 1] - end[i]` otherwise  
   This is an **adjacent-difference / prefix-suffix boundary** pattern: sorted intervals let us derive all free space from neighboring endpoints.

2. **Track the best existing idle interval without cancellation.**  
   This handles the “remove none” option and establishes a baseline answer. The invariant is: after scanning boundaries up to index `i`, `best` stores the largest idle gap seen so far.

3. **Evaluate each possible removed meeting in `O(1)`.**  
   For meeting `i`, duration is `end[i] - start[i]`. If removed, the continuous idle block becomes:
   `leftGap + duration + rightGap`  
   This is correct because no other meetings lie inside that merged region; sorted non-overlap guarantees continuity from the previous meeting’s end to the next meeting’s start.

4. **Take the maximum over all meetings and the baseline.**  
   The invariant is global optimality: every legal action is either “remove none” or “remove exactly one meeting,” and each such action is evaluated once.

5. **Why prefix/suffix is still the right abstraction:**  
   Even though the final formula is local, the mental model is the same as prefix/suffix optimization: precompute boundary information so each candidate decision is constant-time. That scales cleanly and keeps the implementation branch-light.

## 📊 Worked Example
Use `dayLength = 20`, `start = [2, 6, 12]`, `end = [4, 9, 15]`.

| i | Meeting   | Left gap | Duration | Right gap | Idle if removed |
|---|-----------|----------|----------|-----------|-----------------|
| 0 | `[2,4)`   | `2`      | `2`      | `2`       | `6`             |
| 1 | `[6,9)`   | `2`      | `3`      | `3`       | `8`             |
| 2 | `[12,15)` | `3`      | `3`      | `5`       | `11`            |

Existing idle gaps without removal are:

- before first: `2`
- between 0 and 1: `2`
- between 1 and 2: `3`
- after last: `5`

Baseline answer is `5`.

Now test each cancellation:
- remove `[2,4)` → merged idle `0..6`, length `6`
- remove `[6,9)` → merged idle `4..12`, length `8`
- remove `[12,15)` → merged idle `9..20`, length `11`

Maximum is `11`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. You scan the meetings once to compute or evaluate adjacent gaps, and each candidate cancellation is processed in constant time. At `10^6` elements this is still practical in most languages; at `10^9`, linear time is already too expensive, so anything superlinear is categorically wrong.

### Space Complexity
`O(1)` auxiliary space if you compute left/right gaps on the fly from neighbors. If you materialize gap arrays for clarity, it becomes `O(n)`. The space can be reduced without changing time complexity; the trade-off is slightly denser boundary logic.

## 💡 Key Takeaways
- If intervals are already sorted and non-overlapping, look for adjacent-difference formulas before reaching for heavier interval data structures.
- “Remove one item to maximize a contiguous region” is a strong signal that each candidate can be evaluated from local left/right context.
- Be precise about half-open intervals: idle lengths come from endpoint subtraction, with no `+1` or inclusive-boundary adjustment.
- Edge meetings need separate boundary handling: before the first meeting and after the last meeting are real idle gaps.
- At scale, the winning design move is to convert repeated global recomputation into one pass of precomputed boundary state plus constant-time candidate evaluation.

## 🚀 Variations & Further Practice
- Allow removing up to `k` meetings. The twist is that local evaluation no longer suffices; you need sliding-window or DP reasoning over merged occupied/free structure.
- Meetings are unsorted and may overlap. The harder part is interval normalization first, then applying the same gap-maximization logic on the merged schedule.
- Instead of canceling, allow moving one meeting anywhere it fits. The twist is feasibility: you must maximize one gap while preserving schedulability elsewhere, which introduces prefix/suffix capacity checks.