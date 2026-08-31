# Minimum Lane Changes to Collect Ordered Checkpoints

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Simulation

---

## 🗂 Problem Overview
Given two length-`n` arrays over a 3-lane road, compute the minimum number of lane changes needed to move from position `0` to `n - 1`, starting in lane `2`, while collecting every required checkpoint token and never occupying a blocked lane. Forward movement is free; only lane switches cost `1`. The challenge is that local choices interact across positions, so greedy switching can easily fail even though the state space is small.

## 🌍 Engineering Impact
This pattern shows up in constrained path selection over a tiny state space with massive input length: stream processors choosing execution lanes under per-stage constraints, packet schedulers rerouting across a few queues, compiler backends selecting among a small number of register classes, or workflow engines advancing through ordered gates with transient exclusions. At scale, brute-force path enumeration collapses immediately, while per-position dynamic programming stays linear and predictable. The important architectural lesson is that when the number of modes is fixed and tiny, you optimize over time by carrying forward only the best cost per mode, not the full history.

## 🔍 Problem Statement
You are given:

- `checkpoints[i]`: `0` if position `i` has no token, otherwise the lane `1..3` whose token must be collected there.
- `blocked[i]`: `0` if no lane is blocked at position `i`, otherwise the lane `1..3` that cannot be occupied there.

The vehicle starts at position `0` in lane `2` and moves right one position at a time until `n - 1`. Before entering the next position, it may stay in the same lane or switch lanes; each switch costs `1`, and forward movement costs `0`. A token is collected only if the vehicle is in the required lane at that position. Return the minimum lane changes needed to finish while collecting all tokens, or `-1` if impossible.

Constraints: `1 <= n <= 100000`, arrays have equal length, values are in `{0,1,2,3}`, `checkpoints[i] != blocked[i]` when a token exists, and `blocked[0] != 2`.

Examples:

- `checkpoints = [0,0,1,0,3]`, `blocked = [0,3,0,2,0]` → `2`
- `checkpoints = [0,2,0,1]`, `blocked = [0,0,2,1]` → `-1`

The key constraint is `n` up to `100000`: you need a linear-time solution.

## 🪜 How to Solve This
1. Read the problem → notice the road is long, but the number of lanes is fixed at exactly `3`. That usually means the useful state is “best cost to be in each lane now,” not the full path.

2. At any position, the past matters only through the minimum cost of arriving in lane `1`, `2`, or `3`. That is classic dynamic programming over a tiny state space.

3. For each position, first invalidate lanes blocked there. Then, if a checkpoint exists, invalidate every lane except the required one. This converts the problem into “which lanes are legal at this position?”

4. Once legality is known, update the cost for each legal lane:
   - stay in the same lane for free, or
   - switch from one of the other two lanes for cost `+1`.

5. Because there are only three lanes, each transition is constant work. No heap, no graph search, no recursion.

6. If all three lane states become unreachable at any position, return `-1`. Otherwise, after processing all positions, take the minimum reachable cost.

The reason this approach is natural is that the road is large, but the choice set per step is tiny and fixed.

## 🧩 Algorithm Walkthrough
1. **Model the state with Dynamic Programming over fixed-width lanes.**  
   Maintain `dp[1..3]`, where `dp[lane]` is the minimum lane changes needed to be at the current position in that lane. This is the right abstraction because the future depends only on current lane and accumulated cost, not on the exact sequence of prior moves.

2. **Initialize the starting position correctly.**  
   At position `0`, set lane `2` to cost `0`. Lanes `1` and `3` are reachable with one immediate side jump at position `0`, so they start at `1`, unless blocked or disallowed by a checkpoint at `0`. This preserves the invariant: `dp[l]` is the best valid cost after fully processing position `0`.

3. **For each position `i`, compute legal lanes.**  
   A lane is legal if it is not `blocked[i]`. If `checkpoints[i] != 0`, then only that checkpoint lane remains legal. This enforces both constraints at the exact position where they matter.

4. **Invalidate illegal states.**  
   For any illegal lane, set `dp[lane] = INF`. After this step, every finite entry corresponds to a valid occupancy at position `i`.

5. **Relax side jumps among legal lanes.**  
   For each legal lane `l`, update  
   `newDp[l] = min(dp[l], min(dp[other legal lanes]) + 1)`.  
   This captures either staying put or switching from another lane before entering this position. With only three lanes, checking all alternatives is constant time.

6. **Advance the invariant.**  
   Replace `dp` with `newDp`. Now `dp` again means “minimum valid cost at the current position for each lane.”

7. **Detect impossibility early.**  
   If all three entries are `INF` at any step, no valid route exists; return `-1`.

8. **Return the best finishing lane.**  
   The answer is `min(dp[1], dp[2], dp[3])` after processing `n - 1`, since the destination does not require a specific final lane unless constrained by that position itself.

## 📊 Worked Example
Use `checkpoints = [0,0,1,0,3]`, `blocked = [0,3,0,2,0]`.

Let `dp = [lane1, lane2, lane3]`, with `INF` meaning unreachable.

| Position `i` | `blocked[i]` | `checkpoints[i]` | Legal lanes | `dp` after processing |
|---|---:|---:|---|---|
| 0 | 0 | 0 | 1,2,3 | `[1,0,1]` |
| 1 | 3 | 0 | 1,2 | `[1,0,INF]` |
| 2 | 0 | 1 | 1 | `[1,INF,INF]` |
| 3 | 2 | 0 | 1,3 | `[1,INF,2]` |
| 4 | 0 | 3 | 3 | `[INF,INF,2]` |

Trace:
1. Start in lane 2, but lanes 1 and 3 are reachable at cost 1 via an immediate switch.
2. Position 1 blocks lane 3, so lane 3 becomes invalid.
3. Position 2 requires lane 1, forcing the vehicle there with total cost 1.
4. Position 3 blocks lane 2; lane 1 remains valid, and lane 3 is reachable by switching from lane 1 for cost 2.
5. Position 4 requires lane 3, so final answer is `2`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each position processes exactly three lanes and compares against at most two alternatives per lane, so the dominant work is constant per index. At `10^6` positions this is routine; at `10^9`, linear time is still the limiting factor, but anything superlinear is already non-viable.

### Space Complexity
`O(1)`. The algorithm stores only the current and next cost for three lanes, plus a few scalars. Space is owned entirely by the fixed-size DP state. You cannot asymptotically reduce below this without recomputing state, which would only worsen clarity, not memory.

## 💡 Key Takeaways
- If the input length is large but the number of operational modes is tiny and fixed, think DP over modes rather than path search over history.
- When transitions are local in time and cost depends only on current mode, “best cost per state at index `i`” is the interview signal for rolling dynamic programming.
- Position `0` is easy to mishandle: lane `2` starts at cost `0`, but lanes `1` and `3` may be reachable at cost `1` immediately unless blocked or checkpoint-constrained.
- Apply both constraints at each position before relaxing transitions; otherwise you can accidentally switch through a lane that is illegal at that position.
- In production systems, this is the reusable pattern for long sequential optimization with a tiny state space: compress history aggressively and carry only frontier costs.

## 🚀 Variations & Further Practice
- Extend from 3 lanes to `k` lanes with arbitrary switch costs between lanes; the twist is that naive per-position transitions become `O(k^2)` and may require optimization.
- Add penalties or rewards on positions, not just lane changes; now the DP must optimize mixed movement and occupancy costs under the same legality constraints.
- Allow blocked intervals and checkpoint windows instead of single positions; the harder part is reasoning about feasibility across ranges rather than pointwise constraints.