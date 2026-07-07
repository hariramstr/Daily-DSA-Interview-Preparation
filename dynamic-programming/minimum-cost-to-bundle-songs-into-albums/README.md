# Minimum Cost to Bundle Songs into Albums

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, partition-dp

---

## 🗂 Problem Overview
Given an array `durations`, split it into contiguous albums so every song appears exactly once and album order matches the original sequence. Each album may contain at most `maxSongs` songs, and its cost is `(maximum duration in the album) * (album size)`. Return the minimum total cost across all valid partitions. The problem is non-trivial because each partition choice changes both album size and album maximum, so local greedy decisions do not compose into a global optimum.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered workloads must be partitioned into bounded batches with non-linear batch cost: media packaging, stream micro-batching, warehouse pick-wave planning, compiler pass chunking, and distributed job scheduling with setup amplification. The key issue is that batch cost depends on the worst item in the batch, not just the count. At scale, greedy batching either over-packs expensive outliers or fragments work into too many small units. Partition DP gives a reliable way to optimize globally under ordering constraints, which is exactly what production planners, schedulers, and release pipelines need when “keep sequence, choose boundaries” is the core decision.

## 🔍 Problem Statement
You are given:

- `durations[i]`: duration of the `i`th song
- `maxSongs`: maximum number of consecutive songs allowed in one album

You must partition the full array into contiguous groups such that:

- every song belongs to exactly one album
- song order is preserved
- each album has between `1` and `maxSongs` songs

The cost of one album is:

`max(duration in album) * number of songs in album`

Return the minimum total cost over all valid partitions.

Constraints:

- `1 <= durations.length <= 2000`
- `1 <= durations[i] <= 10^6`
- `1 <= maxSongs <= durations.length`

Examples:

- `durations = [3,1,4,2], maxSongs = 2` → `10`
- `durations = [5,2,2,6,3], maxSongs = 3` → `18`

The critical constraint is `n <= 2000`: large enough to rule out exponential partition search, but small enough for quadratic dynamic programming.

## 🪜 How to Solve This
1. Read the problem → this is not reordering, selection, or interval merging. It is **partitioning an ordered array into contiguous groups**.

2. Notice the objective → the total cost is the sum of per-group costs, and each group cost depends only on:
   - where the group starts and ends
   - the maximum value inside that group

3. That structure strongly suggests **partition DP**:
   - define the best answer for the first `i` songs
   - try every valid last album ending at `i`

4. For a fixed ending position `i`, the last album can have length `1..maxSongs`, as long as it stays in bounds.  
   If that last album starts at `j`, then:
   - previous cost is the optimum for songs before `j`
   - last album cost is `max(durations[j..i-1]) * (i - j)`

5. Compute these candidates while scanning backward from `i - 1`, updating the running maximum incrementally. That avoids recomputing maxima for every subarray.

6. Take the minimum over all valid last-album choices.  
   Once you see “ordered contiguous partition + optimize total cost from local segment cost,” the DP becomes the obvious model.

## 🧩 Algorithm Walkthrough
1. **Define the DP state.**  
   Let `dp[i]` be the minimum cost to bundle the first `i` songs, where `dp[0] = 0`.  
   This is the standard **Partition Dynamic Programming** pattern: solve a prefix optimally by choosing the final segment.

2. **Iterate over album endpoints.**  
   For each `i` from `1` to `n`, compute `dp[i]`.  
   The invariant is: before processing `dp[i]`, all `dp[0..i-1]` already contain optimal costs for their prefixes.

3. **Enumerate valid last album lengths.**  
   The last album ending at song `i - 1` can contain at most `maxSongs` songs, so consider starts `j` from `i - 1` down to `max(0, i - maxSongs)`.  
   This guarantees every candidate partition is valid and contiguous.

4. **Maintain the segment maximum incrementally.**  
   While moving `j` backward, update `currentMax = max(currentMax, durations[j])`.  
   Then the candidate cost is `dp[j] + currentMax * (i - j)`.  
   This is correct because `dp[j]` optimally covers the prefix before the last album, and the last album cost depends only on `durations[j..i-1]`.

5. **Take the minimum candidate.**  
   Set `dp[i]` to the smallest candidate over all valid `j`.  
   The invariant after this step: `dp[i]` is the optimal cost for the first `i` songs.

6. **Return `dp[n]`.**  
   The algorithm works because every legal partition has a unique final album, and the recurrence exhaustively evaluates all such endings without overlap errors.  
   This abstraction is right because the problem is fundamentally “choose cut points in order,” which is exactly what partition DP models.

## 📊 Worked Example
Use `durations = [3,1,4,2]`, `maxSongs = 2`.

Let `dp[i]` = minimum cost for first `i` songs.

| i | Candidate last album(s) | Computation | `dp[i]` |
|---|---|---|---|
| 0 | none | base case | 0 |
| 1 | `[3]` | `dp[0] + 3*1 = 3` | 3 |
| 2 | `[1]`, `[3,1]` | `dp[1]+1=4`, `dp[0]+3*2=6` | 4 |
| 3 | `[4]`, `[1,4]` | `dp[2]+4=8`, `dp[1]+4*2=11` | 8 |
| 4 | `[2]`, `[4,2]` | `dp[3]+2=10`, `dp[2]+4*2=12` | 10 |

Trace intuition:

1. Best for first two songs is splitting them: cost `4`.
2. For the first three, keeping `4` alone is cheaper than pairing it with `1`.
3. For all four, keeping `2` alone gives `dp[3] + 2 = 10`, which beats pairing `[4,2]`.

Answer: `10`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * maxSongs)` time. For each endpoint `i`, we scan backward over at most `maxSongs` candidate starts while maintaining the running maximum in `O(1)` per step. With `n <= 2000`, this is easily practical. At `10^6` or `10^9` scale, this quadratic-style prefix expansion would be unacceptable without stronger structure or approximation.

### Space Complexity
`O(n)` space for the `dp` array storing the optimal cost for each prefix length. The running maximum and candidate cost use constant extra space. Space cannot be reduced below `O(n)` cleanly because each `dp[j]` may be needed by many later states.

## 💡 Key Takeaways
- If the array order is fixed and you are choosing where to cut contiguous groups, think **partition DP** before considering greedy heuristics.
- When total cost is the sum of segment costs and each segment cost depends only on that segment, a prefix-based recurrence is usually the right abstraction.
- Be explicit about indexing: `dp[i]` should mean “first `i` songs,” which makes the last segment `[j .. i-1]` and avoids fencepost bugs.
- While scanning candidate starts backward, update the segment maximum incrementally; recomputing `max(durations[j..i-1])` each time silently degrades performance.
- In production planning systems, globally optimal boundary placement often matters more than locally efficient batches because worst-item amplification makes greedy packing systematically unstable.

## 🚀 Variations & Further Practice
- **Add a fixed per-album setup cost.** Same partition DP, but each segment cost becomes `setup + max * length`; this changes the trade-off between many small albums and fewer large ones.
- **Require exactly `k` albums.** Introduces a second DP dimension for partition count, turning a 1D prefix optimization into constrained partition DP.
- **Replace `max * length` with a richer segment cost** such as `max + sum` or `max - min`; the twist is whether the segment statistic can still be maintained incrementally or needs heavier preprocessing.