# Minimum Cost to Stabilize a Multi-Stage Assembly Line

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, State Compression, Sequence Optimization

---

## 🗂 Problem Overview
Choose one mode for each of `n` sequential assembly stages. Selecting mode `j` at stage `i` costs `costs[i][j]`, and each adjacent pair of chosen modes adds `switchPenalty[prev][curr]`. The sequence must contain exactly `k` mode-blocks, where a new block starts only when the mode changes. Return the minimum total cost, or `-1` if no sequence can realize exactly `k` blocks. The non-trivial part is optimizing both cost and block count under adjacency-dependent transitions.

## 🌍 Engineering Impact
This pattern shows up anywhere a sequential plan has both per-step execution cost and transition cost: compiler instruction selection with pipeline hazards, ML inference graph placement across heterogeneous devices, manufacturing recipe scheduling, CDN routing policy changes, and streaming job operator reconfiguration. At scale, greedy local choices fail because transition penalties accumulate globally and structural constraints like “exactly k segments” matter for stability, latency, or operational budgets. Dynamic programming is what lets you preserve global optimality while keeping the state compact enough to run within production latency and memory limits.

## 🔍 Problem Statement
Given `n` stages and `m` possible modes per stage, choose exactly one mode for every stage. The total cost is:

- the sum of `costs[i][chosenMode]` for all stages, plus
- the sum of `switchPenalty[mode[i-1]][mode[i]]` for every adjacent pair.

A mode-block is a maximal contiguous run of equal modes. The chosen sequence must contain exactly `k` such blocks. A block count increases only when `mode[i] != mode[i-1]`, even if `switchPenalty[a][a]` is nonzero.

Constraints:
- `1 <= n <= 100`
- `1 <= m <= 50`
- `1 <= k <= n`
- costs and penalties are up to `10^6`

Examples:

- `n=4, m=3, k=2` → output `8`
- `n=3, m=2, k=3`, `costs=[[1,10],[10,1],[1,10]]`, `switchPenalty=[[0,5],[5,0]]` → valid alternating sequences force 3 blocks; minimum total is `13`

The key algorithmic pressure point is that adjacency penalties and exact block count rule out independent per-stage optimization.

## 🪜 How to Solve This
1. Read the objective carefully → this is not just “pick the cheapest mode per stage.” The cost of stage `i` depends on what happened at stage `i-1`.
2. Notice the structural constraint → “exactly `k` mode-blocks” means we must track how many times the mode changes, not just total cost.
3. That immediately suggests a DP state with three dimensions: current stage, blocks formed so far, and current mode.
4. Why current mode? Because the next transition cost depends on it, and whether the next choice starts a new block also depends on it.
5. Define `dp[i][b][c]` as the minimum cost after processing stages `0..i`, ending in mode `c`, with exactly `b` blocks formed.
6. Transition from every previous mode `p`:
   - if `p == c`, block count stays `b`
   - otherwise, block count becomes `b + 1`
   - always add `costs[i][c] + switchPenalty[p][c]`
7. Initialize stage `0` with one block for every mode.
8. The answer is the minimum over all ending modes at `dp[n-1][k][*]`; if all are unreachable, return `-1`.

Once you see “sequence + adjacency cost + exact number of segments,” this DP is the natural model.

## 🧩 Algorithm Walkthrough
1. **Model the problem as Dynamic Programming over sequence state.**  
   Use the pattern explicitly: **Dynamic Programming on sequences with state compression by last choice**. The right abstraction is “prefix optimality”: once you know the best cost for a prefix ending in mode `p` with `b` blocks, the earlier details no longer matter.

2. **Define the state.**  
   Let `dp[b][c]` represent the minimum cost after processing the current stage, ending in mode `c`, with exactly `b` blocks. This compresses away the stage dimension by rolling arrays across stages. Invariant: after stage `i`, `dp[b][c]` is the optimal cost for all valid assignments of stages `0..i`.

3. **Initialize stage 0.**  
   For every mode `c`, set `dp[1][c] = costs[0][c]`. There is exactly one block at the first stage regardless of mode. All other states are unreachable and should be initialized to `INF`.

4. **Process each next stage `i`.**  
   Build `next[b][c]` from prior `dp`. For every previous block count `b` and previous mode `p`, try choosing current mode `c`:
   - `newBlocks = b + (p == c ? 0 : 1)`
   - `candidate = dp[b][p] + costs[i][c] + switchPenalty[p][c]`
   Relax `next[newBlocks][c]`.

5. **Respect feasibility bounds.**  
   Skip transitions where `newBlocks > k`. Also note that after processing stage `i`, block count cannot exceed `i + 1`, which naturally bounds work.

6. **Extract the result.**  
   After all `n` stages, answer is `min(dp[k][c])` over all modes `c`. If every value is `INF`, return `-1`.

7. **Why this is correct.**  
   Every valid sequence has a unique path through these states, and every transition adds exactly the incremental stage cost and adjacency penalty while updating block count correctly. Since the recurrence enumerates all previous modes, it cannot miss an optimal sequence.

## 📊 Worked Example
Take `n=3, m=2, k=3`:

- `costs = [[1,10],[10,1],[1,10]]`
- `switchPenalty = [[0,5],[5,0]]`

Let `dp[b][mode]` after each stage:

| Stage | State updates |
|---|---|
| `i=0` | `dp[1][0]=1`, `dp[1][1]=10` |
| `i=1` | From mode `0`: stay `0` → `dp[1][0]=11`, switch to `1` → `dp[2][1]=7` |
|  | From mode `1`: switch to `0` → `dp[2][0]=25`, stay `1` → `dp[1][1]=11` |
| `i=2` | From `dp[2][1]=7`: switch to `0` → `dp[3][0]=13`, stay `1` → `dp[2][1]=17` |
|  | Other transitions are worse for `b=3` |

Final answer: `min(dp[3][0], dp[3][1]) = 13`.

The optimal sequence is `[0,1,0]`:
- stage costs = `1 + 1 + 1 = 3`
- penalties = `5 + 5 = 10`
- total = `13`

## ⏱ Complexity Analysis
### Time Complexity
The straightforward DP runs in `O(n * k * m^2)`: for each stage, each block count, and each previous mode, we try every current mode. With `n<=100`, `k<=100`, and `m<=50`, that is at most about `25,000,000` transitions, which is practical. This is nowhere near `10^9`-scale work, so a direct implementation is acceptable.

### Space Complexity
Using rolling arrays, space is `O(k * m)`, since only the previous and current stage layers are needed. A full 3D table would cost `O(n * k * m)` and is unnecessary unless you need path reconstruction for the actual mode sequence.

## 💡 Key Takeaways
- If a sequence problem has per-position cost, adjacency-dependent transition cost, and an exact segment/group count, think DP with state `(position, groups, lastChoice)`.
- “Exactly `k` blocks” is a strong signal that the state must track structural events like mode changes, not just cumulative cost.
- The first stage always starts block count at `1`; initializing it as `0` is the most common off-by-one bug here.
- `switchPenalty[a][a]` may be nonzero, but staying in the same mode must **not** create a new block; cost transition and block transition are separate concerns.
- In production systems, this pattern generalizes to optimizing plans under both execution cost and reconfiguration cost, where preserving the right boundary state is what makes global optimization tractable.

## 🚀 Variations & Further Practice
- Add **forbidden modes per stage** or preassigned modes for some stages; same DP shape, but feasibility pruning becomes part of the transition logic.
- Require **at most `k` blocks** or charge a per-block creation penalty; this changes the objective from exact structural matching to constrained or regularized segmentation.
- Extend penalties to depend on the **previous two modes** instead of one; the state becomes higher-order Markov, increasing complexity from tracking one boundary decision to tracking a mode pair.