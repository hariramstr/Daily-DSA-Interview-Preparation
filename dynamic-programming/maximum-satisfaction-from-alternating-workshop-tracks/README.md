# Maximum Satisfaction from Alternating Workshop Tracks

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, state-transition

---

## 🗂 Problem Overview
Given two arrays, `engineering` and `design`, choose exactly one value from each index to maximize total satisfaction across `N` workshop slots. The constraint is that you cannot attend more than `K` consecutive workshops from the same track. The output is the maximum achievable sum. What makes this non-trivial is that the best choice at slot `i` depends on both the current reward and the length of the current run, so greedy local decisions fail.

## 🌍 Engineering Impact
This pattern shows up in production scheduling and control systems where local optimization is constrained by run-length limits: ad serving with frequency caps, streaming pipelines that must balance hot and cold paths, CPU schedulers avoiding starvation, and recommendation systems enforcing diversity across adjacent impressions. At scale, naive greedy selection over-optimizes immediate gain and violates policy or degrades long-term utility. Dynamic programming gives a compact way to encode “best value so far under operational state,” which is exactly what enables policy-compliant optimization without exploding the search space into full schedule enumeration.

## 🔍 Problem Statement
You are given two integer arrays of length `N`: `engineering[i]` and `design[i]`, representing the satisfaction gained by attending the Engineering or Design track in slot `i`. For every slot, you must choose exactly one track. A valid schedule may not contain more than `K` consecutive choices of the same track.

Compute the maximum total satisfaction over all valid schedules.

**Constraints**
- `1 <= N <= 100000`
- `1 <= K <= N`
- `engineering.length == design.length == N`
- `0 <= engineering[i], design[i] <= 10000`
- At least one valid schedule exists

**Example 1**
- `engineering = [8, 3, 5, 7]`
- `design = [4, 6, 2, 9]`
- `K = 2`
- Output: `28`

**Example 2**
- `engineering = [10, 10, 1, 10, 10]`
- `design = [1, 1, 20, 1, 1]`
- `K = 2`
- Output: `60`

The key algorithmic driver is that `N` is large enough to rule out exponential search, while the validity of a choice depends on bounded history.

## 🪜 How to Solve This
1. Read the constraint carefully → the choice at slot `i` is not independent. You need to know whether you picked Engineering or Design before, and how many times in a row.

2. That immediately suggests **stateful optimization**, not greedy selection. Picking the larger of `engineering[i]` and `design[i]` can trap you into an invalid run later.

3. Ask what minimal history matters → not the full schedule, only:
   - current slot,
   - current track,
   - current consecutive count.

4. That gives a dynamic programming state: “best total up to slot `i` if I end on track `T` with run length `r`.”

5. From there, transitions are straightforward:
   - stay on the same track if `r < K`,
   - switch tracks and reset run length to `1`.

6. Because each state only depends on the previous slot, you do not need an `N x K x 2` table in memory. Keep only the previous layer and build the next one.

7. The result is the maximum value among all valid ending states after processing all `N` slots.

That is the core DP pattern: compress history to the smallest state that preserves future correctness.

## 🧩 Algorithm Walkthrough
1. **Define the DP pattern: finite-state dynamic programming.**  
   Use two arrays for the previous slot:
   - `prevE[r]` = best total ending with Engineering, with exactly `r` consecutive Engineering choices.
   - `prevD[r]` = best total ending with Design, with exactly `r` consecutive Design choices.  
   This is the right abstraction because the constraint is purely about bounded run length.

2. **Initialize slot 0.**  
   Set:
   - `prevE[1] = engineering[0]`
   - `prevD[1] = design[0]`  
   All other states are invalid and should be initialized to negative infinity.  
   Invariant: every stored value represents a valid schedule for processed slots.

3. **Process each subsequent slot `i`.**  
   Create fresh `currE` and `currD`, initialized to negative infinity.

4. **Handle “stay” transitions.**  
   For each `r` from `1` to `K-1`:
   - `currE[r+1] = max(currE[r+1], prevE[r] + engineering[i])`
   - `currD[r+1] = max(currD[r+1], prevD[r] + design[i])`  
   This is correct because continuing the same track only increases the run length by one and remains valid while `r+1 <= K`.

5. **Handle “switch” transitions.**  
   Let `bestPrevD = max(prevD[1..K])` and `bestPrevE = max(prevE[1..K])`. Then:
   - `currE[1] = bestPrevD + engineering[i]`
   - `currD[1] = bestPrevE + design[i]`  
   Switching resets the run length to `1`. The invariant remains: each state is the best valid schedule ending in that exact state.

6. **Roll the arrays forward.**  
   Assign `prevE = currE`, `prevD = currD`, and continue.

7. **Return the best ending state.**  
   Answer is `max(max(prevE[1..K]), max(prevD[1..K]))`.  
   This is complete because every valid schedule must end in one of those states.

## 📊 Worked Example
Take `engineering = [10, 10, 1, 10, 10]`, `design = [1, 1, 20, 1, 1]`, `K = 2`.

| Slot `i` | `engineering[i]` | `design[i]` | `E1` | `E2` | `D1` | `D2` |
|---|---:|---:|---:|---:|---:|---:|
| 0 | 10 | 1  | 10 | -  | 1  | -  |
| 1 | 10 | 1  | 11 | 20 | 11 | 2  |
| 2 | 1  | 20 | 12 | 12 | 40 | 31 |
| 3 | 10 | 1  | 50 | 22 | 13 | 41 |
| 4 | 10 | 1  | 51 | 60 | 51 | 14 |

Interpretation:
- `E2 = 20` at slot 1 means two consecutive Engineering picks: `10 + 10`.
- At slot 2, switching from best Engineering state into Design gives `D1 = 40`.
- At slot 4, `E2 = 60` comes from `Engineering, Engineering, Design, Engineering, Engineering`.

Final answer: `60`.

## ⏱ Complexity Analysis
### Time Complexity
The straightforward state-transition DP runs in `O(N * K)` time. Each slot updates up to `K` run-length states for both tracks, and the dominant work is those transitions. This is practical for moderate `K`, but at `10^6` or `10^9` state updates, constant factors and cache behavior start to matter materially.

### Space Complexity
The rolling-array implementation uses `O(K)` space. The memory is owned by the previous and current state arrays for both tracks. A full `O(N * K)` table is unnecessary unless you need schedule reconstruction; the trade-off is that rolling state gives the value only, not the path.

## 💡 Key Takeaways
- If a choice depends on a bounded amount of recent history, look for a DP state that captures only that history rather than the full sequence.
- “Maximize value under a consecutive-run cap” is a strong signal for state-transition DP with `(last_choice, run_length)` as the core state.
- Initialize unreachable states to negative infinity, not zero; otherwise invalid transitions silently contaminate the result.
- Be careful with run-length indexing: switching resets the count to `1`, while staying increments it to `r + 1` only if `r < K`.
- In production systems, this is the broader pattern of optimizing utility subject to policy-state constraints without enumerating all possible action sequences.

## 🚀 Variations & Further Practice
- Add a third track or `M` tracks. The conceptual twist is that switching now means taking the best among `M-1` alternative ending states, which changes both transition structure and optimization opportunities.
- Require at least `L` and at most `K` consecutive picks per track. The harder part is that both minimum and maximum run-length constraints must be enforced during transitions.
- Add a switching penalty between tracks. The DP state stays similar, but transitions now model trade-offs between diversity and switching cost rather than a pure reset.