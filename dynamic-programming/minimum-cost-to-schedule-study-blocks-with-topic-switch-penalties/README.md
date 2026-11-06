# Minimum Cost to Schedule Study Blocks With Topic Switch Penalties

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, matrix-dp, optimization

---

## 🗂 Problem Overview
You must choose exactly one topic per day across `n` days, from `m` possible topics. Each choice has a per-day study cost, and every transition from yesterday’s topic to today’s topic adds a penalty from a full `m x m` switch-cost matrix. The goal is to minimize total cost over the full schedule. The non-trivial part is that each decision depends on both the current day and the previous topic, so greedy local choices fail.

## 🌍 Engineering Impact
This pattern shows up anywhere a system makes sequential choices with stateful transition costs: job placement with warm-cache penalties, compiler pass ordering, ML inference routing with model-switch overhead, ad-serving pipelines with context reload cost, and manufacturing line scheduling with setup times. At small scale, brute force or greedy heuristics appear acceptable; at production scale, they either explode combinatorially or miss globally optimal plans. Dynamic programming gives a deterministic optimization boundary: predictable runtime, explainable behavior, and a clean abstraction for “current action cost + transition from prior state,” which is common in schedulers and orchestration systems.

## 🔍 Problem Statement
Given:

- `cost[i][j]`: the cost of studying topic `j` on day `i`
- `switchCost[a][b]`: the penalty for moving from topic `a` on day `i - 1` to topic `b` on day `i`

choose exactly one topic each day to minimize total cost across all `n` days. There is no switch penalty before day `0`, and you may start with any topic.

Constraints:

- `1 <= n <= 200`
- `1 <= m <= 100`
- `0 <= cost[i][j], switchCost[a][b] <= 10^4`
- Answer fits in 64-bit signed integer

Example 1:

```text
cost = [[4,1,7],[2,3,6],[5,2,4]]
switchCost = [[0,5,2],[3,0,4],[1,2,0]]
Output: 6
```

Example 2:

```text
cost = [[8,3],[6,7],[4,2],[5,9]]
switchCost = [[0,10],[1,0]]
Output: 15
```

The key algorithmic constraint is the dense transition matrix: each state may depend on all `m` prior topics, which naturally leads to `O(n * m * m)` dynamic programming.

## 🪜 How to Solve This
1. Read the problem → each day requires one choice, but today’s cost is not independent of yesterday’s choice.

2. That dependency means the state cannot just be “minimum cost up to day `i`.” It must include the topic chosen on day `i`.

3. Define `dp[i][j]` as the minimum total cost to finish days `0..i` and end day `i` on topic `j`.

4. Base case becomes obvious: on day `0`, there is no previous topic, so `dp[0][j] = cost[0][j]`.

5. For later days, if you want to end on topic `j`, the previous day could have ended on any topic `k`. So:
   `dp[i][j] = cost[i][j] + min(dp[i-1][k] + switchCost[k][j])`.

6. This is the standard dynamic programming pattern: current state cost = local cost + best predecessor transition.

7. Once all days are processed, the answer is the minimum value in the last row, since the final topic is unrestricted.

This approach is natural because the problem has optimal substructure and overlapping subproblems, while the dense transition matrix rules out simpler greedy strategies.

## 🧩 Algorithm Walkthrough
1. **Choose the DP state.**  
   Use **matrix dynamic programming**: `dp[i][j]` stores the minimum cost through day `i` if day `i` uses topic `j`. This is the right abstraction because the only historical detail that affects future cost is the most recent topic. The invariant is: every `dp[i][j]` already represents the globally optimal schedule prefix ending in `j`.

2. **Initialize day 0.**  
   For every topic `j`, set `dp[0][j] = cost[0][j]`. This is correct because there is no incoming transition before the first day. The invariant after initialization: row `0` contains exact optimal costs for all valid starting topics.

3. **Transition day by day.**  
   For each day `i > 0` and each current topic `j`, scan all previous topics `k` and compute:  
   `candidate = dp[i-1][k] + switchCost[k][j] + cost[i][j]`.  
   Take the minimum candidate. This is correct because every valid schedule ending at `(i, j)` must come from exactly one prior topic on day `i-1`.

4. **Preserve only what is needed.**  
   Since row `i` depends only on row `i-1`, you can use two 1D arrays instead of a full `n x m` table. The invariant remains unchanged while reducing memory from `O(nm)` to `O(m)`.

5. **Extract the result.**  
   Return `min(dp[n-1][j])` over all topics `j`. This is correct because the problem does not constrain the final topic.

This is a textbook **state-transition DP over a dense transition matrix**. The `m^2` factor is unavoidable in the straightforward formulation because each destination topic may depend on every source topic.

## 📊 Worked Example
Use Example 1:

```text
cost = [[4,1,7],[2,3,6],[5,2,4]]
switchCost = [[0,5,2],[3,0,4],[1,2,0]]
```

Let `dp[j]` be the best cost ending the current day on topic `j`.

| Day | Topic 0 | Topic 1 | Topic 2 |
|-----|---------|---------|---------|
| 0   | 4       | 1       | 7       |

Day 1:
- topic 0 = `2 + min(4+0, 1+3, 7+1) = 2 + 4 = 6`
- topic 1 = `3 + min(4+5, 1+0, 7+2) = 3 + 1 = 4`
- topic 2 = `6 + min(4+2, 1+4, 7+0) = 6 + 5 = 11`

| Day | Topic 0 | Topic 1 | Topic 2 |
|-----|---------|---------|---------|
| 1   | 6       | 4       | 11      |

Day 2:
- topic 0 = `5 + min(6+0, 4+3, 11+1) = 11`
- topic 1 = `2 + min(6+5, 4+0, 11+2) = 6`
- topic 2 = `4 + min(6+2, 4+4, 11+0) = 12`

Final answer = `min(11, 6, 12) = 6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * m * m)`. For each of `n` days, you compute `m` destination states, and each state scans all `m` prior topics. With the given limits (`n <= 200`, `m <= 100`), this is about 2 million transitions—comfortably small. At `10^6` or `10^9` state transitions, this pattern becomes throughput-sensitive and demands pruning or structure exploitation.

### Space Complexity
`O(m)` with rolling arrays, or `O(n * m)` if you keep the full DP table for debugging or path reconstruction. The dominant structure is the DP state vector. Space can be reduced without affecting correctness, but reconstructing the actual topic sequence then requires storing predecessors separately.

## 💡 Key Takeaways
- If each decision has a local cost plus a penalty based on the immediately previous choice, think DP with state = “position + last choice.”
- A dense transition matrix is a strong signal that each current state may depend on all prior states, which often yields `O(n * m^2)` matrix-style DP.
- Do not apply `switchCost` on day `0`; initialization is just `cost[0][j]`.
- Be careful with the recurrence ordering: `switchCost[k][j]` means previous topic `k` to current topic `j`, not the reverse.
- In production schedulers, separating node cost from transition cost is a durable modeling pattern: it makes trade-offs explicit and optimization strategies composable.

## 🚀 Variations & Further Practice
- Add a constraint that each topic can be used at most `x` times. The twist is expanding state to track usage counts, which increases dimensionality and forces tighter pruning.
- Require reconstruction of the actual optimal topic sequence, not just the minimum cost. The twist is storing predecessor pointers alongside DP state.
- Allow skipping a day or studying multiple topics per day with coupling penalties. The twist is that the state graph becomes richer, often moving from simple row-by-row DP toward shortest-path formulations.