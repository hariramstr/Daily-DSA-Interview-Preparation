# Minimum Signal Delay to Sync Stage Lights

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Arrays

---

## 🗂 Problem Overview
You must pick exactly one allowed start time from each light’s sorted list and minimize the spread between the earliest and latest chosen times. Formally, minimize `max(chosenTimes) - min(chosenTimes)` across all lights. The challenge is scale: there can be up to `100000` lights and `200000` total candidate times, so brute-forcing combinations or comparing every cross-list pairing is infeasible. The key observation is that feasibility for a delay `D` is monotonic.

## 🌍 Engineering Impact
This pattern shows up anywhere multiple independent components must align within a bounded tolerance: distributed job schedulers choosing execution slots, media pipelines synchronizing audio/video segment boundaries, fleet orchestration aligning rollout windows, and stream processors coordinating watermark advancement across partitions. At small scale, ad hoc pairwise checks seem fine; at production scale they collapse under combinatorial explosion. Binary search on the answer converts an optimization problem into repeated feasibility checks, which is often the difference between an elegant service-level objective computation and a system that times out under peak load. It also forces a clean separation between “can we satisfy this bound?” and “what is the smallest bound?”

## 🔍 Problem Statement
Given `positions` of length `n` and a 2D array `times`, choose one value from each `times[i]` so that the synchronization delay is minimized:

`delay = max(chosenTimes) - min(chosenTimes)`

Return the minimum possible delay.

`positions[i]` identifies the light but does not affect the calculation. Each `times[i]` is sorted in non-decreasing order and contains at least one value, so a valid selection always exists.

**Constraints**
- `1 <= n <= 100000`
- `positions.length == n`
- `times.length == n`
- `1 <= times[i].length <= 100000`
- `sum(times[i].length) <= 200000`
- `0 <= positions[i], times[i][j] <= 10^9`

**Examples**
- `positions = [2,5,9]`, `times = [[1,4,8],[2,6],[5,7,10]]` → `2`
- `positions = [0,3,8,12]`, `times = [[3],[1,4,9],[5,6],[2,8,11]]` → `3`

The decisive constraint is the total input size: large enough to rule out combinatorial search, but structured enough to support binary search over the answer.

## 🪜 How to Solve This
1. Start from the objective: minimize `max - min`. That usually suggests searching over a candidate range width rather than directly constructing the optimal set.

2. Reframe the question: for a proposed delay `D`, can all chosen times fit inside **some** interval `[L, L + D]`?

3. Once phrased that way, each light contributes a simple requirement: its list must contain at least one value inside that interval.

4. The crucial insight is monotonicity: if delay `D` is feasible, then any larger delay is also feasible. That is the signature for **binary search on the answer**.

5. Now the real work is the feasibility check. We need to know whether there exists a window of length `D` that hits every list at least once.

6. A practical way to do that is to flatten all candidate times into `(time, lightId)` pairs, sort by time, and run a sliding window. If a window spans at most `D` and covers all `n` light IDs, then `D` works.

7. Binary search the smallest feasible `D`. The result is optimal because feasibility transitions exactly once from false to true.

## 🧩 Algorithm Walkthrough
1. **Flatten the input into events.**  
   Build an array of pairs `(time, lightIndex)` for every allowed time in every list. This converts “many sorted lists” into one globally ordered timeline. The invariant we want later is: every contiguous segment in this sorted array corresponds to a real time interval.

2. **Sort all events by time.**  
   This enables the **Sliding Window / Two Pointers** pattern. Once sorted, any candidate interval `[events[left].time, events[right].time]` is represented by a contiguous subarray.

3. **Binary search the answer `D`.**  
   Search between `0` and `maxTime - minTime`. The predicate is `feasible(D)`: does some interval of width at most `D` contain at least one event from every light? Because larger windows can only include more events, `feasible(D)` is monotonic.

4. **Feasibility check with a sliding window.**  
   Maintain `left`, `right`, a frequency array `count[lightIndex]`, and `coveredLights`. Expand `right`, adding the current event’s light. While the window width exceeds `D`, advance `left` and decrement counts.  
   Invariant: after shrinking, the window width is always `<= D`, and `coveredLights` equals the number of distinct lights represented in the current window.

5. **Detect success.**  
   If at any point `coveredLights == n`, then every light has at least one allowed time inside the current interval, so delay `D` is feasible.

6. **Return the minimum feasible `D`.**  
   Standard lower-bound binary search gives the smallest delay for which the predicate holds, which is exactly the optimal answer.

This is the right abstraction because the optimization target is scalar and monotonic, while the feasibility condition reduces cleanly to range coverage over a sorted sequence.

## 📊 Worked Example
Use `positions = [2,5,9]`, `times = [[1,4,8],[2,6],[5,7,10]]`.

Flattened and sorted events:

| idx | time | light |
|---:|---:|---:|
| 0 | 1 | 0 |
| 1 | 2 | 1 |
| 2 | 4 | 0 |
| 3 | 5 | 2 |
| 4 | 6 | 1 |
| 5 | 7 | 2 |
| 6 | 8 | 0 |
| 7 | 10 | 2 |

Check `D = 2`:

1. Window `[1,2]` covers lights `{0,1}` only.
2. Extend to time `4`: width becomes `3`, so move `left` from `1` to `2`. Window `[2,4]` still covers `{0,1}`.
3. Extend to time `5`: window `[4,5]` covers `{0,2}`.
4. Extend to time `6`: window `[4,6]` has width `2` and covers `{0,1,2}`.

So `D = 2` is feasible.  
If you test `D = 1`, no window ever covers all three lights. Therefore the minimum delay is `2`.

## ⏱ Complexity Analysis
### Time Complexity
Let `M = sum(len(times[i]))`, with `M <= 200000`. Sorting flattened events costs `O(M log M)`. Each feasibility check is `O(M)` via sliding window, and binary search performs `O(log R)` checks where `R <= 10^9`. Total complexity is `O(M log M + M log R)`, which is practical at this input size but not at naive `10^9`-scale scans.

### Space Complexity
The dominant space is the flattened event array plus per-light frequency tracking: `O(M + n)`. You can’t meaningfully remove the event storage if you want efficient repeated feasibility checks; trading it away would force recomputation or more complex multi-list traversal logic.

## 💡 Key Takeaways
- If the problem asks for a minimum possible maximum gap, threshold, or tolerance, check whether “can we do it with value `X`?” is monotonic; that is a strong binary-search-on-answer signal.
- When feasibility is “does some interval contain at least one item from every group?”, think sorted events plus sliding window rather than pairwise comparisons across groups.
- Be careful with the window condition: feasibility is `maxTime - minTime <= D`, so shrink only while the width is `> D`, not `>= D`.
- Distinct-group coverage must be tracked with counts, not booleans alone; removing the left edge can drop a light from the window only when its count reaches zero.
- In production systems, this pattern is valuable because it separates optimization from validation, letting you scale decision logic through a monotone predicate instead of exploding the search space.

## 🚀 Variations & Further Practice
- Require choosing one time per light **and** preserving the order of lights along the truss; the twist is that feasibility now depends on sequence constraints, not just set coverage.
- Minimize the delay when each light can contribute **k choices** instead of one; the harder part is that coverage becomes multiplicity-aware rather than simple distinct presence.
- Extend to 2D or multidimensional synchronization windows, where each candidate has multiple attributes; the monotone answer may remain, but the feasibility check is no longer a simple one-dimensional sliding window.