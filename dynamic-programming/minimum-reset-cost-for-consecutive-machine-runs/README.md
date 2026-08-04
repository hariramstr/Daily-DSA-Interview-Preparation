# Minimum Reset Cost for Consecutive Machine Runs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, sequence-optimization, state-compression

---

## 🗂 Problem Overview
Given a job sequence `modes[0..n-1]`, process jobs in order while paying `resetCost[x]` whenever the machine starts in mode `x` or switches into `x` from a different mode. You may upgrade at most `k` jobs, changing each upgraded position to any mode. The goal is to minimize total start/switch cost. The challenge is that each upgrade changes not just one position, but potentially the switch structure of the entire surrounding sequence, so local greedy choices fail.

## 🌍 Engineering Impact
This pattern shows up anywhere contiguous runs are cheaper than transitions: stream processors minimizing repartitioning, GPU/TPU schedulers reducing kernel reconfiguration, manufacturing lines avoiding tool resets, and storage engines batching by compression or encryption mode. At scale, transition cost dominates per-item work, so optimizing only individual records misses the real bottleneck. The useful abstraction is not “edit a few elements” but “spend a limited budget to reshape run boundaries.” Dynamic programming is what makes that budgeted boundary optimization tractable without exploding into exponential search over all edited sequences.

## 🔍 Problem Statement
You are given:

- `n` jobs processed strictly in order
- `modes[i]`, the required mode for job `i`
- `resetCost[x]`, the cost to start or switch into mode `x`
- an upgrade budget `k`

If two consecutive jobs use the same mode, no extra cost is paid between them. If the mode changes from `a` to `b`, the cost is always `resetCost[b]`, independent of `a`. The first job also costs `resetCost[chosenMode]` because the initial state is undefined.

You may upgrade up to `k` positions, and each upgraded job may be assigned any mode.

Return the minimum achievable total cost.

Constraints:

- `1 <= n <= 2000`
- `1 <= k <= n`
- `1 <= modes[i] <= m`
- `1 <= m <= 100`
- `1 <= resetCost[x] <= 10^4`

Examples:

- `modes = [1,2,2,3], k = 1, resetCost = [0,5,2,7]` → minimum cost `7`
- `modes = [4,1,4,1,4], k = 2, resetCost = [0,3,6,8,2]` → minimum cost `2`

The key constraint is `n = 2000`: too large for exponential search, but small enough for `O(n * k * m)`-style DP.

## 🪜 How to Solve This
1. Read the cost model carefully → cost is paid only when a new run starts, and the amount depends only on the new mode, not the previous one.

2. That means the real optimization target is the sequence of run starts, not individual pairwise transitions.

3. Upgrading a job lets us force its mode to match neighbors, extend an existing run, or start a cheaper run. So the decision at position `i` depends on:
   - how many upgrades we have used
   - what mode the previous processed job ended in

4. That immediately suggests dynamic programming with state `(position, upgradesUsed, currentMode)`.

5. At each job, there are only two meaningful actions:
   - keep its original mode
   - if budget allows, assign it to any mode

6. A naive “assign to any mode” transition is too expensive if done literally for every prior mode and every target mode.

7. The compression insight: for a chosen target mode `x`, the best predecessor is either:
   - already in `x` → no new cost
   - in some other mode → pay `resetCost[x]`
   So for each layer, we only need the best value ending in `x` and the global best value ending in any mode.

8. That reduces the transition to `O(1)` per target mode, giving an efficient DP.

## 🧩 Algorithm Walkthrough
1. **Use Dynamic Programming with state compression.**  
   Let `dp[u][x]` be the minimum cost after processing the current prefix, using exactly `u` upgrades, and ending with the machine in mode `x`. This is the right abstraction because future cost depends only on the current mode and remaining budget, not on the full history.

2. **Initialize the first position through the same transition logic.**  
   Before processing any jobs, treat all states as unreachable. When assigning the first job to mode `x`, the cost is simply `resetCost[x]`, because starting a run in `x` costs that amount.

3. **For each next job, build `next[u][x]`.**  
   Suppose we want the current job to end in mode `x`. The best predecessor is:
   - stay in `x`: cost `dp[uPrev][x]`
   - switch from any other mode: `bestAny[uPrev] + resetCost[x]`
   More precisely, use `min(dp[uPrev][x], bestAny[uPrev] + resetCost[x])`, where `bestAny[uPrev]` is the minimum over all ending modes. This works because switch cost depends only on the destination.

4. **Handle the two assignment types.**
   - **No upgrade:** only allowed for `x = modes[i]`
   - **Upgrade used:** allowed for any `x`, consuming one budget unit

5. **Maintain the invariant.**  
   After processing position `i`, `dp[u][x]` is the minimum cost for every valid prefix ending in mode `x` with exactly `u` upgrades used. No cheaper history is omitted because every legal assignment either preserves the original mode or spends one upgrade to choose another.

6. **Compress by layers.**  
   Only the previous prefix is needed, so keep two `k+1 by m` tables or equivalent rolling arrays.

7. **Answer extraction.**  
   After all jobs, take the minimum over all `dp[u][x]` for `0 <= u <= k` and `1 <= x <= m`.

## 📊 Worked Example
Take `modes = [1,2,2,3]`, `k = 1`, `resetCost = [0,5,2,7]`.

We track `dp[u][mode] = min cost`.

| Step | Job | Upgrades used | End mode 1 | End mode 2 | End mode 3 |
|---|---:|---:|---:|---:|---:|
| 1 | mode 1 | 0 | 5 | ∞ | ∞ |
| 1 | mode 1 | 1 | 5 | 2 | 7 |
| 2 | mode 2 | 0 | ∞ | 7 | ∞ |
| 2 | mode 2 | 1 | 10 | 2 | 12 |
| 3 | mode 2 | 0 | ∞ | 7 | ∞ |
| 3 | mode 2 | 1 | 12 | 2 | 14 |
| 4 | mode 3 | 0 | ∞ | ∞ | 14 |
| 4 | mode 3 | 1 | 12 | 7 | 9 |

Interpretation of the last row:
- ending in mode `3` with one upgrade gives cost `9`
- ending in mode `2` with one upgrade gives cost `7`

That `7` corresponds to upgrading the last job from `3` to `2`, producing `[1,2,2,2]`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n * k * m)`, assuming each DP layer precomputes the best value across modes for every upgrade count. The dominant work is evaluating every `(position, upgradesUsed, targetMode)` state once. This is fine for the given bounds, but at `10^6` or `10^9` elements, even linear-in-`n` DP would require stronger structural compression or streaming approximations.

### Space Complexity
`O(k * m)` with rolling arrays. The space is owned by the current and next DP layers, plus a small `bestAny` helper array. Keeping full `O(n * k * m)` history is unnecessary unless you also need to reconstruct the edited sequence.

## 💡 Key Takeaways
- If the cost is paid only when a run starts and depends only on the destination state, think “DP over prefix + current mode,” not greedy local edits.
- When a limited number of elements can be reassigned arbitrarily, that is a strong signal for budgeted sequence DP with state compression.
- The first job is not a special free case; starting in mode `x` costs `resetCost[x]`, and forgetting that shifts every answer.
- “At most `k` upgrades” is easiest to implement as DP over “exactly `u` used so far,” then minimize over `u <= k` at the end.
- In production systems, transition-dominated workloads are often best modeled around boundary costs rather than per-item costs; that reframing is what makes the optimization tractable.

## 🚀 Variations & Further Practice
- Allow switching cost to depend on both source and destination, `cost[a][b]`. The compression via global minimum breaks, and the DP becomes a full transition matrix problem.
- Add a per-upgrade penalty instead of a hard budget. This turns the problem into cost-augmented run optimization where the trade-off is continuous rather than capacity-constrained.
- Require reconstruction of the actual edited sequence and chosen modes. Same DP core, but now you need parent pointers or a compressed backtracking strategy.