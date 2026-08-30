# Minimum Cost to Reach the Office With 1-Step or 2-Step Elevators

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `cost` of length `n`, where `cost[i]` is the fee for landing on floor `i + 1`, compute the minimum total fee required to reach exactly floor `n`. From any position, you may move up either 1 floor or 2 floors, and you pay only when you land. The non-trivial part is that each decision affects future options, so a locally cheap move does not always produce the globally cheapest path.

## 🌍 Engineering Impact
This pattern shows up anywhere a system advances through ordered states with local transition costs: workflow engines choosing retry paths, streaming pipelines minimizing recomputation across stages, compilers selecting cheapest intermediate transformations, and routing layers evaluating hop-by-hop penalties. At scale, brute-force exploration of all paths becomes combinatorial noise. Dynamic programming turns repeated subproblems into a linear pass with deterministic memory use. That matters operationally: predictable latency, simpler correctness reasoning, and a clean path to space optimization when the state transition depends only on a fixed-size history window.

## 🔍 Problem Statement
You start in the lobby, before floor 1, and want to reach exactly floor `n`, where `n = cost.length`. From floor `i`, you may move to `i + 1` or `i + 2`. Landing on floor `k` incurs fee `cost[k - 1]`. No fee is paid at the start.

Return the minimum total fee needed to reach floor `n`.

Constraints:

- `1 <= cost.length <= 1000`
- `1 <= cost[i] <= 1000`
- `n = cost.length`

Examples:

- `cost = [4, 2, 7, 3]` → `5`  
  Optimal path: lobby → floor 2 → floor 4, cost = `2 + 3 = 5`

- `cost = [1, 100, 1, 1, 100, 1]` → `4`  
  Optimal path: lobby → floor 1 → floor 3 → floor 4 → floor 6, cost = `1 + 1 + 1 + 1 = 4`

The key constraint is the restricted transition set: each floor depends only on the previous one or two floors, which strongly suggests dynamic programming.

## 🪜 How to Solve This
1. Read the problem → notice this is not a path-search problem over an arbitrary graph. Each floor has only two incoming possibilities: from one floor below or two floors below.

2. Ask what the minimum information is to decide floor `i` → only the cheapest way to reach `i - 1` and `i - 2`. Nothing earlier matters directly.

3. Define a state: `dp[i] = minimum cost to reach floor i` → once that state is available, reaching the next floor is just a local choice.

4. Write the recurrence → to land on floor `i`, you either came from `i - 1` or `i - 2`, then paid the fee for floor `i`. So take the cheaper prior state and add the current landing cost.

5. Handle the base cases carefully → floor 1 costs `cost[0]`; floor 2 can be reached directly from the lobby for `cost[1]` or through floor 1, but direct landing is allowed and cheaper to model explicitly.

6. Compute left to right → every state depends only on already-computed earlier states, so one linear pass solves the problem.

This is the classic dynamic programming signal: optimal answer for position `i` is built from optimal answers to smaller positions.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Dynamic Programming on a linear sequence.**  
   The state space is ordered by floor number, and each state depends on a constant number of earlier states. This is the right abstraction because the problem asks for a minimum over overlapping subproblems.

2. **Define the DP state.**  
   Let `dp[i]` be the minimum fee required to reach floor `i`, using 1-based floor indexing. This is correct because the problem asks for the cheapest cost to each exact destination, not just whether it is reachable.

3. **Initialize base cases.**  
   `dp[1] = cost[0]` and `dp[2] = cost[1]`.  
   Why: from the lobby, you may land directly on floor 1 or floor 2, paying only that floor’s fee. The invariant after initialization is that the minimum cost for the first two reachable floors is exact.

4. **Apply the recurrence for floors 3 through `n`.**  
   `dp[i] = min(dp[i - 1], dp[i - 2]) + cost[i - 1]`  
   This is correct because any valid path to floor `i` must come from exactly one of those two predecessor floors. The invariant maintained is: after computing `dp[i]`, all values `dp[1..i]` are optimal.

5. **Return `dp[n]`.**  
   Since the objective is to reach exactly floor `n`, the final DP state is the answer.

6. **Optional optimization.**  
   Because each state depends only on the previous two, the array can be reduced to two rolling variables. Same recurrence, lower space, slightly less debuggability.

## 📊 Worked Example
Example: `cost = [4, 2, 7, 3]`

Let `dp[i]` be the minimum cost to reach floor `i`.

| Floor `i` | Landing fee | Formula | `dp[i]` |
|---|---:|---|---:|
| 1 | 4 | direct from lobby | 4 |
| 2 | 2 | direct from lobby | 2 |
| 3 | 7 | `min(dp[2], dp[1]) + 7 = min(2, 4) + 7` | 9 |
| 4 | 3 | `min(dp[3], dp[2]) + 3 = min(9, 2) + 3` | 5 |

Trace:

1. Reach floor 1 for cost `4`.
2. Reach floor 2 directly for cost `2`.
3. Floor 3 is cheaper via floor 2 than floor 1, so cost becomes `9`.
4. Floor 4 is cheaper via floor 2 than floor 3, so cost becomes `5`.

Answer: `dp[4] = 5`, corresponding to lobby → floor 2 → floor 4.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because each floor is processed once, and each step performs a constant-time minimum and addition. At `10^6` elements this is still operationally cheap in most environments; at `10^9`, linear time becomes throughput-bound and likely infeasible without changing the problem model.

### Space Complexity
The straightforward DP array uses `O(n)` space, owned entirely by `dp`. This can be reduced to `O(1)` because only the previous two states are needed. The trade-off is reduced traceability during debugging and less visibility into intermediate state.

## 💡 Key Takeaways
- If a problem asks for a minimum cost to reach position `i`, and `i` depends on a small fixed set of earlier positions, think linear dynamic programming immediately.
- “Choose 1 step or 2 steps” is a strong pattern-recognition signal that the recurrence will depend on the previous one or two states.
- The indexing trap is that `cost[i]` maps to floor `i + 1`, so `dp[i]` must add `cost[i - 1]` if floors are 1-based.
- Floor 2 is reachable directly from the lobby, so initializing it from `cost[1]` is important; do not force all paths through floor 1.
- In production systems, this pattern generalizes to any sequential optimization where local transitions are constrained and overlapping subproblems make brute-force search wasteful.

## 🚀 Variations & Further Practice
- Allow jumps of up to `k` floors instead of just 1 or 2; the twist is that each state now depends on a sliding window of previous states.
- Add forbidden floors or penalties for specific transitions; the twist is that reachability and cost minimization must be handled together.
- Return both the minimum cost and the actual path; the twist is storing predecessor information without losing the DP simplicity.