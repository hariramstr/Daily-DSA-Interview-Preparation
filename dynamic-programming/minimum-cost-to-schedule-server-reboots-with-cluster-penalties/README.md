# Minimum Cost to Schedule Server Reboots with Cluster Penalties

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, State Compression, Array

---

## 🗂 Problem Overview
Given `n` servers, assign each one to either an Early or Late reboot window. Each choice has a per-server cost, and adjacent servers incur an extra penalty if they use the same window. The schedule must use **exactly `k` Early assignments**. Return the minimum total cost.

What makes this non-trivial is the combination of a **global cardinality constraint** (`exactly k`) and a **local adjacency interaction** (cost depends on the previous server’s choice), which rules out greedy selection.

## 🌍 Engineering Impact
This pattern shows up in production scheduling and placement systems where each decision has both an individual cost and a neighborhood effect. Examples include maintenance rollout planning across racks, shard placement with anti-affinity penalties, compiler instruction scheduling with transition costs, and streaming pipelines where adjacent stages sharing the same execution lane create contention.

At scale, local heuristics fail because they optimize node cost while ignoring transition cost and global quotas. Dynamic programming is what lets you preserve exact constraints—capacity, budget, rollout percentage—while still modeling pairwise interactions. That enables predictable optimization instead of “mostly good” schedules that collapse under operational edge cases.

## 🔍 Problem Statement
You are given four arrays of length `n`:

- `earlyCost[i]`: cost to reboot server `i` in the Early window
- `lateCost[i]`: cost to reboot server `i` in the Late window
- `earlyPenalty[i]`: penalty if both servers `i-1` and `i` are Early
- `latePenalty[i]`: penalty if both servers `i-1` and `i` are Late

Choose Early or Late for every server such that exactly `k` servers are assigned Early. The total cost is the sum of chosen per-server costs plus same-window penalties for each adjacent pair.

Constraints:

- `1 <= n <= 2000`
- `0 <= k <= n`
- costs and penalties are up to `10^9`
- `earlyPenalty[0]` and `latePenalty[0]` are ignored
- use 64-bit integers

Example 1:
- `n = 4, k = 2`
- `earlyCost = [3, 8, 2, 6]`
- `lateCost = [5, 1, 7, 3]`
- `earlyPenalty = [0, 4, 5, 2]`
- `latePenalty = [0, 6, 1, 3]`
- Output: `9`

Example 2:
- `n = 5, k = 3`
- `earlyCost = [4, 2, 9, 3, 5]`
- `lateCost = [6, 7, 1, 8, 2]`
- `earlyPenalty = [0, 3, 4, 6, 1]`
- `latePenalty = [0, 2, 5, 2, 7]`
- Output: `15`

The key constraint is `n <= 2000`: too large for exponential state search, small enough for quadratic DP.

## 🪜 How to Solve This
1. Read the problem → each server has only two choices, but the cost of choosing one depends on the **previous server’s choice**. That immediately suggests a DP state carrying the last decision.

2. Notice the exact `k` requirement → we also need to track **how many Early assignments have been used so far**. Without that dimension, we cannot enforce the quota.

3. So the natural state becomes: after processing servers `0..i`, with `j` Early assignments used, and with server `i` ending in window `w`, what is the minimum cost?

4. From there, transitions are straightforward:
   - choose Early now → add `earlyCost[i]`, increment `j`, and maybe add `earlyPenalty[i]` if previous was Early
   - choose Late now → add `lateCost[i]`, keep `j`, and maybe add `latePenalty[i]` if previous was Late

5. The adjacency penalty only depends on the immediately previous server, so we do **not** need the full history—just the last window. That is the state compression insight.

6. Since each row only depends on the previous row, we can roll the DP and keep memory at `O(k)` per window state instead of `O(nk)`.

## 🧩 Algorithm Walkthrough
1. **Define the DP pattern: dynamic programming with state compression.**  
   Let `dpE[j]` be the minimum cost after processing the current prefix with exactly `j` Early servers, where the current server is assigned Early. Let `dpL[j]` be the analogous value ending in Late. This is the right abstraction because the only cross-element dependency is adjacency with the immediately previous server.

2. **Initialize the first server.**  
   For server `0`, there is no adjacency penalty. So:
   - `dpE[1] = earlyCost[0]`
   - `dpL[0] = lateCost[0]`  
   All other states are infinity.  
   Invariant: after initialization, each state represents the optimal cost for prefix `[0]`.

3. **Process servers from left to right.**  
   For each server `i > 0`, build new arrays `nextE` and `nextL` from the previous `dpE` and `dpL`.

4. **Transition into Early.**  
   For each feasible `j`:
   - from previous Early: `dpE[j-1] + earlyCost[i] + earlyPenalty[i]`
   - from previous Late: `dpL[j-1] + earlyCost[i]`  
   Take the minimum and store in `nextE[j]`.  
   Invariant: `nextE[j]` is optimal for prefix `[0..i]`, exactly `j` Early servers, ending in Early.

5. **Transition into Late.**  
   For each feasible `j`:
   - from previous Early: `dpE[j] + lateCost[i]`
   - from previous Late: `dpL[j] + lateCost[i] + latePenalty[i]`  
   Store the minimum in `nextL[j]`.

6. **Roll the arrays.**  
   Replace `dpE, dpL` with `nextE, nextL`. This preserves correctness because row `i` depends only on row `i-1`.

7. **Return the final answer.**  
   After processing all servers, the result is `min(dpE[k], dpL[k])`. That guarantees exactly `k` Early assignments and the minimum total cost over both possible ending windows.

## 📊 Worked Example
Use Example 1:

- `k = 2`
- `earlyCost = [3,8,2,6]`
- `lateCost = [5,1,7,3]`
- `earlyPenalty = [0,4,5,2]`
- `latePenalty = [0,6,1,3]`

| i | Server choice state | `j=0` | `j=1` | `j=2` |
|---|---|---:|---:|---:|
| 0 | end Late (`dpL`) | 5 | ∞ | ∞ |
| 0 | end Early (`dpE`) | ∞ | 3 | ∞ |
| 1 | `dpL` | 12 | 4 | ∞ |
| 1 | `dpE` | ∞ | 13 | 11 |
| 2 | `dpL` | 20 | 11 | 18 |
| 2 | `dpE` | ∞ | 14 | 6 |
| 3 | `dpL` | 26 | 17 | 9 |
| 3 | `dpE` | ∞ | 26 | 17 |

Interpretation:
- After server 3, with exactly 2 Early assignments, the best ending states are:
  - end Late: `9`
  - end Early: `17`

Answer: `min(9, 17) = 9`, achieved by `[Early, Late, Early, Late]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * k)`. For each of `n` servers, we iterate over all feasible counts of Early assignments from `0..k`, updating two states (`end Early`, `end Late`) in constant time. At `n = 2000`, this is practical. At `10^6` or `10^9` scale, quadratic dependence on one dimension becomes the limiting factor.

### Space Complexity
`O(k)`. The memory is owned by two rolling DP arrays for the previous row and two for the current row. A full `O(n * k)` table is unnecessary because transitions only depend on the immediately previous server. The trade-off is reduced debuggability versus much better memory efficiency.

## 💡 Key Takeaways
- If a sequence problem has an **exact count constraint** plus **cost depending on the previous choice**, expect DP with dimensions for count and last state.
- When pairwise interaction is only between neighbors, you usually need to remember **just the previous decision**, not the full assignment history.
- `earlyPenalty[0]` and `latePenalty[0]` are ignored; applying them at initialization is a classic off-by-one bug.
- Be careful with transition indices: moving into Early consumes one Early slot, so transitions use `j-1`; moving into Late does not.
- The transferable design insight is that many production optimizers become tractable once you separate **global quota tracking** from **local transition state** and compress history to the minimum sufficient context.

## 🚀 Variations & Further Practice
- Add a third maintenance window. The conceptual twist is that the “last state” dimension grows from 2 to 3, increasing transition fan-out while preserving the same DP structure.
- Replace adjacency penalties with penalties for runs of length up to `r`. Now the state must encode more history than just the previous choice, turning simple state compression into bounded-history DP.
- Allow at most `k` Early assignments and a budget cap on total penalty. This introduces a second resource dimension and forces a trade-off between exact optimization and pseudo-polynomial DP.