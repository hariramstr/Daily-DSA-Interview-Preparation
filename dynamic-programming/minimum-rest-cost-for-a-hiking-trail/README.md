# Minimum Rest Cost for a Hiking Trail

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, 1d-dp

---

## 🗂 Problem Overview
Given an integer array `cost`, where `cost[i]` is the cost paid when landing on checkpoint `i`, compute the minimum total cost required to move from checkpoint `0` to checkpoint `n - 1`. From any checkpoint, you may advance by either 1 or 2 positions. The output is a single integer: the cheapest achievable total. The non-trivial part is that each choice affects future options, so greedy local decisions are not sufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the cheapest path through a linear sequence with limited transition rules: staged rollout pipelines, media transcoding chains, packet forwarding with bounded hops, and workflow engines that can skip optional stages. At scale, brute-force exploration explodes into redundant recomputation because adjacent states share most of their substructure. Dynamic programming collapses that overlap into a single pass, which matters when the same recurrence appears inside larger schedulers, query planners, or cost-based optimizers. The value is not just asymptotic efficiency; it is predictable latency, simpler correctness reasoning, and easy space reduction.

## 🔍 Problem Statement
You are given an array `cost` of length `n`, where checkpoints are numbered from `0` to `n - 1`. You start at checkpoint `0`, and because you are already there, its cost is part of the total. From checkpoint `i`, you may move to `i + 1` or `i + 2` if that checkpoint exists. Every time you land on a checkpoint, you must pay `cost[i]`. Return the minimum total cost needed to reach checkpoint `n - 1`.

Constraints:

- `1 <= n == cost.length <= 1000`
- `0 <= cost[i] <= 1000`
- If `n == 1`, the answer is `cost[0]`

Examples:

- `cost = [4, 2, 7, 3]` → `9`
- `cost = [1, 100, 1, 1, 100, 1]` → `4`

The key constraint is the movement rule: each state depends only on the previous one or two states, which strongly suggests a 1D dynamic programming recurrence.

## 🪜 How to Solve This
1. Read the movement rule → from checkpoint `i`, you can only arrive from `i - 1` or `i - 2`.
2. That means the cheapest way to reach `i` does **not** depend on the full path history, only on the minimum cost to reach those two prior checkpoints.
3. Define a state: “minimum total cost to reach checkpoint `i`.”
4. Base cases come directly from the problem:
   - `dp[0] = cost[0]`
   - `dp[1] = cost[0] + cost[1]` because you start at `0`, so reaching `1` must include both.
5. For every later checkpoint, choose the cheaper predecessor:
   - `dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i]`
6. Compute left to right once, because each state only needs already-computed values.
7. The final answer is `dp[n - 1]`.

This is the standard “linear DP with local transitions” pattern: define the state, derive the recurrence from allowed moves, seed base cases carefully, then iterate.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: 1D Dynamic Programming.**  
   The problem is a shortest-path computation on a linear graph where each node has at most two incoming edges. Dynamic programming is the right abstraction because optimal substructure is explicit: the best path to `i` must extend the best path to `i - 1` or `i - 2`.

2. **Define the DP state.**  
   Let `dp[i]` be the minimum total cost required to reach checkpoint `i`. This state is sufficient because once you know the cheapest way to reach prior checkpoints, no additional history changes the cost of future moves.

3. **Initialize base cases.**  
   - `dp[0] = cost[0]`
   - If `n > 1`, `dp[1] = cost[0] + cost[1]`  
   These are correct because you start on `0`, and the only way to reach `1` is from `0`.

4. **Apply the recurrence.**  
   For each `i` from `2` to `n - 1`:
   - `dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i]`  
   This is correct because every valid path into `i` must come from exactly one of those two checkpoints.

5. **Maintain the invariant.**  
   After processing index `i`, every `dp[k]` for `0 <= k <= i` stores the true minimum cost to reach checkpoint `k`. The recurrence preserves this because it only combines already-correct subproblems.

6. **Return the terminal state.**  
   The problem asks for the minimum cost to reach the final checkpoint, so return `dp[n - 1]`.

7. **Optional optimization.**  
   Since each state depends only on the previous two values, the DP array can be reduced to two rolling variables without changing correctness.

## 📊 Worked Example
Take `cost = [1, 100, 1, 1, 100, 1]`.

| i | cost[i] | dp[i] calculation              | dp[i] |
|---|---------|--------------------------------|-------|
| 0 | 1       | base case                      | 1     |
| 1 | 100     | `1 + 100`                      | 101   |
| 2 | 1       | `min(1, 101) + 1`              | 2     |
| 3 | 1       | `min(101, 2) + 1`              | 3     |
| 4 | 100     | `min(2, 3) + 100`              | 102   |
| 5 | 1       | `min(3, 102) + 1`              | 4     |

Trace:

1. Start at checkpoint `0`, so cost is already `1`.
2. Reaching `1` costs `101`.
3. Reaching `2` is cheaper via `0`, so total becomes `2`.
4. Reaching `3` is cheaper via `2`, so total becomes `3`.
5. Checkpoint `4` is expensive, but still computed normally.
6. Final checkpoint `5` is cheapest via `3`, giving answer `4`.

Optimal path: `0 -> 2 -> 3 -> 5`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because each checkpoint is processed exactly once, and each step does constant work: one `min` and one addition. At `10^6` elements this is still practical in a single pass; at `10^9`, linear work becomes throughput-bound and likely infeasible without partitioning or a different problem structure.

### Space Complexity
The straightforward DP array uses `O(n)` space, owned entirely by `dp`. This can be reduced to `O(1)` by keeping only the previous two DP values, since no earlier state is ever read again after advancing past index `i`.

## 💡 Key Takeaways
- If each position’s optimal answer depends only on a fixed number of earlier positions, think 1D dynamic programming immediately.
- “Choose min(previous valid states) + local cost” is a strong signal for shortest-path-on-a-line DP.
- The starting checkpoint cost counts here; treating checkpoint `0` as free produces the wrong recurrence and wrong examples.
- The base case for `n == 1` must return `cost[0]`; do not assume you can skip the first checkpoint.
- In production systems, this pattern matters because bounded local dependencies let you turn exponential path exploration into deterministic linear evaluation.

## 🚀 Variations & Further Practice
- Allow jumps of up to `k` checkpoints instead of just 1 or 2; the recurrence widens, and efficient implementations may need a monotonic queue to avoid `O(nk)`.
- Add a constraint on the number of 2-step jumps allowed; the state becomes 2D DP because position alone no longer captures feasibility.
- Make each move incur both landing cost and transition cost between checkpoints; this shifts the model toward weighted path DP and tests state design more than recurrence memorization.