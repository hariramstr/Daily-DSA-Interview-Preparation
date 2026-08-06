# Minimum Fatigue to Tune a Multi-String Instrument

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, State Compression, Knapsack

---

## 🗂 Problem Overview
Given `n <= 8` strings, a target pitch vector, and up to `60` one-time interval tuning operations, compute the minimum total fatigue needed to reach the target exactly from all-zero pitches. Each operation adds a fixed `delta` to every string in `[l, r]` and costs `cost`. The challenge is global coupling: one operation can help several strings while simultaneously overshooting others, so greedy per-string decisions fail.

## 🌍 Engineering Impact
This pattern shows up in systems where each action updates a correlated state vector and exact end-state matters: budget allocation across dependent services, feature-flag rollouts touching overlapping cohorts, compiler optimization passes that rewrite multiple IR regions, and batch scheduling in streaming pipelines with shared resource domains. At scale, local optimization breaks because updates are not independent. State-compressed dynamic programming gives a way to search globally valid combinations while exploiting small state dimensionality. The payoff is predictable optimality under hard constraints, instead of brittle heuristics that pass happy paths and fail under overlap-heavy workloads.

## 🔍 Problem Statement
You are given a target pitch array `target` of length `n` and `m` operations, where operation `j` is `[l_j, r_j, delta_j, cost_j]`. Each operation may be used at most once, in any order. Applying it increases every string in the inclusive range `l_j..r_j` by exactly `delta_j` and adds `cost_j` to total fatigue. All strings start at pitch `0`.

Return the minimum total fatigue required to make every string end at exactly `target[i]`. If no subset of operations produces the target vector exactly, return `-1`.

Constraints:
- `1 <= n <= 8`
- `1 <= m <= 60`
- `0 <= target[i] <= 40`

Examples:
- `target = [3,3]`, `operations = [[0,0,3,4],[1,1,3,5],[0,1,3,6]]` → `6`
- `target = [2,1,2]`, `operations = [[0,1,1,3],[1,2,1,4],[0,2,2,10]]` → `-1`

The key algorithmic driver is small `n` but multi-dimensional exact matching, which makes compact-state DP viable and brute-force subset search unattractive.

## 🪜 How to Solve This
1. Read the constraints → `m` can be `60`, so enumerating all subsets of operations is dead on arrival.

2. Notice `n <= 8` and `target[i] <= 40` → the pitch vector is small enough to encode as a compact DP state.

3. Reframe the problem: each operation is a 0/1 choice that moves us from one pitch vector to another and adds cost. That is a knapsack-style DP, but in multiple dimensions.

4. The only states worth keeping are vectors where every coordinate is `<= target[i]`. Once any string overshoots, that path can never recover because all deltas are non-negative.

5. Encode each pitch vector into a single integer using mixed radix based on `target[i] + 1`. This gives hashable, compact states.

6. Iterate through operations one by one. For each currently reachable state, either skip the operation or apply it once if the resulting vector stays within target.

7. Track the minimum cost per encoded state. The answer is the cost of the encoded target vector, if reachable.

The mental trigger is: “small dimension, exact vector target, one-time updates, non-negative monotone transitions” → state-compressed DP.

## 🧩 Algorithm Walkthrough
1. **Define the DP state using State Compression DP.**  
   Let a state be the current pitch vector across all `n` strings. Because `n` is small, encode the vector into one integer with mixed radix: base for dimension `i` is `target[i] + 1`. This is correct because every valid coordinate lies in `[0, target[i]]`, so the encoding is collision-free.

2. **Initialize the frontier.**  
   Start with the all-zero vector at cost `0`. Maintain a map `dp[state] = min_cost`. The invariant is: after processing the first `k` operations, `dp` contains the cheapest way to reach each valid pitch vector using only those `k` operations.

3. **Process operations as 0/1 transitions.**  
   For each operation `[l, r, delta, cost]`, copy the current map into `next`. For every reachable state, decode or incrementally inspect the vector, apply `delta` to coordinates `l..r`, and reject the transition if any updated coordinate exceeds its target. This pruning is sound because pitches only increase.

4. **Relax the destination state.**  
   If the new vector is valid, encode it and set  
   `next[new_state] = min(next[new_state], dp[state] + cost)`.  
   This is the standard **0/1 Knapsack over compressed multidimensional states** pattern: each operation is either taken once or skipped.

5. **Advance the invariant.**  
   Replace `dp` with `next` and continue. After all `m` operations, `dp[target_state]` is the minimum fatigue among all valid subsets.

6. **Return the result.**  
   If the encoded target vector is absent, return `-1`; otherwise return its stored cost. Correctness follows from exhaustive consideration of every operation’s include/exclude choice over all non-overshooting states.

## 📊 Worked Example
Use `target = [3,3]`, `operations = [[0,0,3,4],[1,1,3,5],[0,1,3,6]]`.

Let state be `(s0, s1)`.

| Step | Operation | Reachable states with min cost |
|---|---|---|
| Start | — | `(0,0): 0` |
| 1 | `[0,0,3,4]` | skip → `(0,0): 0`; take → `(3,0): 4` |
| 2 | `[1,1,3,5]` | from `(0,0)` → `(0,3): 5`; from `(3,0)` → `(3,3): 9` |
| 3 | `[0,1,3,6]` | from `(0,0)` → `(3,3): 6`; from `(3,0)` or `(0,3)` overshoots one string, reject |

Final reachable target state is `(3,3)` with cost `6`, which beats the earlier cost `9`.

The important trace detail is pruning: once an operation would push any coordinate above target, that branch is discarded immediately.

## ⏱ Complexity Analysis
### Time Complexity
Let `S = ∏(target[i] + 1)`, the number of valid pitch vectors. The DP runs in `O(m * S * n)` in the straightforward implementation, since each operation may inspect up to `n` coordinates per state. With `n <= 8`, this is practical only because pruning and sparse-state maps usually keep the reachable frontier far below the full Cartesian product.

### Space Complexity
The DP stores up to `O(S)` states, typically in a hash map from encoded state to minimum cost. Using two maps for rolling transitions keeps auxiliary space linear in the reachable state count. You can reduce constants with array-backed DP only when the full state space is small enough to materialize.

## 💡 Key Takeaways
- If each action updates multiple dimensions at once and exact equality is required, think multidimensional DP rather than greedy or per-dimension optimization.
- Small dimension (`n`) with bounded coordinate values is a strong signal for state compression, even when the number of actions (`m`) is large.
- Mixed-radix encoding must use base `target[i] + 1`; any off-by-one there corrupts state identity.
- Prune immediately on overshoot. Because all deltas are non-negative, an exceeded coordinate can never be repaired later.
- In production systems, this is the same design move as replacing combinatorial action search with compact end-state search when the state vector is small but interactions are dense.

## 🚀 Variations & Further Practice
- Allow negative `delta` operations as well as positive ones. The monotonic overshoot pruning disappears, and the state graph may require shortest-path techniques or bounded-state DP with tighter feasibility checks.
- Add per-operation usage counts instead of 0/1 usage. This turns the problem into bounded or unbounded multidimensional knapsack and changes transition ordering and optimization opportunities.
- Scale `n` upward while keeping `m` moderate. State compression stops working, and the harder question becomes whether meet-in-the-middle, ILP, or approximation is the right architectural trade-off.