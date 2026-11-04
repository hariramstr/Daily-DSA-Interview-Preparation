# Minimum Cost to Reorder a Warehouse Picking List with Shelf Jump Fees

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, State Compression, Prefix/Suffix Optimization

---

## 🗂 Problem Overview
Given `positions`, where `positions[i]` lists all shelves that can satisfy the `i`-th request, choose exactly one shelf per request to minimize total cost. Moving from shelf `a` to `b` costs `|a-b|`, and reversing the previous nonzero movement direction adds a turn fee `T`. The first move has no turn fee. The challenge is scale: `n` and the total number of candidate shelves are both up to `2e5`, so naive transitions between consecutive request lists are too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must preserve request order while choosing among multiple execution locations and paying both distance and switching penalties. Examples include warehouse robotics, multi-AZ job placement with migration penalties, compiler instruction selection with mode-switch costs, storage tier routing, and streaming operators that preserve event order while minimizing repartition churn. At small scale, brute-force pairwise transitions appear acceptable; at production scale they collapse under quadratic fanout. The value of this approach is not just asymptotic improvement—it turns a stateful, path-dependent optimization into a linear pass over compressed state, which is exactly what makes schedulers and routing layers predictable under load.

## 🔍 Problem Statement
You are given a non-empty sorted list `positions[i]` for each request `i`, where each value is a valid shelf that contains the requested item. Choose one shelf from every list, in order, minimizing:

- movement cost `|a-b|` between consecutive chosen shelves
- plus turn fee `T` whenever the sign of the current nonzero move differs from the previous nonzero move

Direction `0` means staying on the same shelf. A zero move does not create a turn, and the next nonzero move is compared against the last nonzero direction, not against zero. The first move never pays a turn fee.

Constraints:
- `1 <= n <= 2 * 10^5`
- total entries across all `positions[i]` is `<= 2 * 10^5`
- shelf values up to `1e9`

Examples:

- `positions = [[2,8],[3],[7,10],[6]], T = 5` → `14`
- `positions = [[1,5],[4,6],[2,8],[3]], T = 2` → `6`

The total candidate count forces an `O(total)` or `O(total log total)` style solution, not pairwise DP between adjacent lists.

## 🪜 How to Solve This
1. Start with the obvious DP: for each request and each candidate shelf, track the best cost to end there.  
2. Then notice plain DP is insufficient because future turn fees depend on the **last nonzero direction**, so the state must include direction: `down`, `none`, or `up`.  
3. Next problem: transitioning from every shelf in `positions[i-1]` to every shelf in `positions[i]` is quadratic across adjacent lists. That fails at `2e5` total candidates.  
4. Look at the transition formula. For a target shelf `x`, each previous shelf `y` contributes `dp[y][dir] + |x-y| + maybe(T)`. Because lists are sorted, `|x-y|` splits cleanly into:
   - `x - y` when `y <= x`
   - `y - x` when `y >= x`
5. That means each target can be computed from prefix/suffix minima over the previous list, separately for “arrive by moving up” and “arrive by moving down”.  
6. The direction state is tiny, so compress it. The expensive dimension is shelves, not direction.  
7. Result: dynamic programming with state compression and prefix/suffix optimization, processing each request list in linear time relative to its size and the previous list size.

## 🧩 Algorithm Walkthrough
1. **Define DP state.**  
   For each shelf `p` in the current request list, maintain:
   - `dpUp[p]`: minimum cost ending at `p` where the last nonzero direction is up
   - `dpDown[p]`: same, but down
   - `dpNone[p]`: cost when no nonzero move has occurred yet  
   This state is sufficient because future turn cost depends only on the last nonzero direction.

2. **Initialize the first request.**  
   For every shelf in `positions[0]`, set `dpNone = 0`, `dpUp = dpDown = +∞`.  
   This encodes “the robot may start anywhere on the first picked shelf for free.”

3. **Process one transition at a time.**  
   Let previous shelves be `A`, current shelves be `B`, both sorted. We need best transitions into each `x in B`.

4. **Compute transitions that end with an upward move.**  
   Only previous shelves `y <= x` can produce a positive move. Cost is:
   `base(y) + (x-y)`, where  
   `base(y) = min(prevUp[y], prevNone[y], prevDown[y] + T)`  
   because switching from down to up pays `T`, while continuing up or starting first nonzero move does not.  
   Since this is `x + min(base(y)-y)`, a prefix minimum over `A` gives all upward arrivals in linear time.

5. **Compute transitions that end with a downward move.**  
   Symmetrically, for `y >= x`:
   `base(y) + (y-x)`, where  
   `base(y) = min(prevDown[y], prevNone[y], prevUp[y] + T)`  
   This becomes `-x + min(base(y)+y)` over a suffix minimum.

6. **Handle zero moves explicitly.**  
   If a shelf value exists in both `A` and `B`, moving `y -> y` costs `0` and preserves the last nonzero direction. So copy:
   - `currUp[y] = min(currUp[y], prevUp[y])`
   - `currDown[y] = min(currDown[y], prevDown[y])`
   - `currNone[y] = min(currNone[y], prevNone[y])`  
   This is the subtle rule that zero movement does not reset direction.

7. **Roll arrays forward.**  
   Keep only previous and current request states. The invariant after processing request `i` is: each DP entry is the minimum achievable cost for all valid choices up to `i` with the encoded last nonzero direction.

This is classic **Dynamic Programming + State Compression + Prefix/Suffix Optimization**: the path-dependent part is compressed into 3 direction states, and the expensive absolute-value transition is linearized using sorted lists.

## 📊 Worked Example
Use `positions = [[1,5],[4,6],[2,8],[3]]`, `T = 2`.

| Step | Candidate shelves | Best state summary |
|---|---|---|
| `i=0` | `[1,5]` | start anywhere: `none(1)=0`, `none(5)=0` |
| `i=1` | `[4,6]` | to `4`: from `5` is down cost `1`; to `6`: from `5` is up cost `1` |
| `i=2` | `[2,8]` | to `2`: best is `5→4→2`, continue down, total `1+2=3`; to `8`: best is `5→6→8`, continue up, total `1+2=3` |
| `i=3` | `[3]` | from `2→3`: up after down, cost `1 + T = 3`, total `6`; from `8→3`: down after up, cost `5 + T = 7`, total `10` |

Optimal path: `5 → 4 → 2 → 3`  
Movement: `1 + 2 + 1 = 4`  
One direction change before the last move: `+2`  
Answer: `6`

## ⏱ Complexity Analysis
### Time Complexity
`O(S)`, where `S` is the total number of shelf entries across all request lists. Each transition between adjacent lists is processed with a linear merge-style sweep plus prefix/suffix minima, not a Cartesian product. At `10^6` candidates this is still practical; at `10^9`, input size alone is impossible, so asymptotics are already optimal relative to data volume.

### Space Complexity
`O(W)`, where `W` is the combined size of the previous and current request lists, plus constant-sized prefix/suffix helpers. The dominant space owner is the rolling DP arrays. You can reduce constants aggressively, but not the asymptotic bound without recomputing transitions and sacrificing runtime.

## 💡 Key Takeaways
- If a DP transition is “choose one from previous sorted list, pay `|x-y|`”, that is a strong signal to look for prefix/suffix minima instead of pairwise transitions.
- If future cost depends only on a tiny summary of history—here, the last nonzero direction—compress that history into a finite-state DP.
- Zero moves are easy to mishandle: staying on the same shelf does **not** reset direction, and the next nonzero move must still compare against the last nonzero direction.
- The first move is another trap: it has no prior direction, so no turn fee applies when direction becomes up or down for the first time.
- The production lesson is broader than this problem: once path history is reduced to a compact state, sorted structure often lets you replace quadratic routing logic with streaming passes.

## 🚀 Variations & Further Practice
- Add a per-shelf picking cost and a per-request blacklist/whitelist update. The twist is combining local node cost with directional transition cost without losing linear transitions.
- Charge different turn fees for `up→down` and `down→up`, or make the fee depend on the magnitude of the previous move. The harder part is determining whether the compressed direction-only state is still sufficient.
- Extend from 1D shelves to 2D coordinates with Manhattan distance and axis-specific turn penalties. The key challenge is that the clean prefix/suffix decomposition of `|x-y|` no longer applies directly.