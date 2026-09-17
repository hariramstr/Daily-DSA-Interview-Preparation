# Maximum Tip Total from Choosing Non-Consecutive Tables

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `tips`, choose a subset of table values such that no two chosen indices are adjacent, and return the maximum possible sum. The input is a single integer array; the output is one integer: the best achievable total tip amount. The non-trivial part is the adjacency constraint: taking a locally large value can block another profitable choice nearby, so independent greedy decisions are not reliable.

## 🌍 Engineering Impact
This pattern shows up anywhere selecting one unit suppresses adjacent or temporally neighboring units: ad-slot allocation with cooldown windows, batch job scheduling on shared resources, stream processors avoiding back-to-back hot partitions, and warehouse picking routes where adjacent tasks contend for the same worker. At small scale, brute force or ad hoc heuristics may pass. At production scale, those approaches either explode combinatorially or leave measurable value on the table. Dynamic programming gives a deterministic optimum, predictable runtime, and a compact state model that can be embedded in schedulers, optimizers, and admission-control layers without introducing operational complexity.

## 🔍 Problem Statement
You are given an integer array `tips` where `tips[i]` is the tip amount earned if a premium server is assigned table `i`. Tables are arranged linearly, and the server cannot be assigned two adjacent tables. Return the maximum total tips obtainable from any valid subset.

Constraints:

- `1 <= tips.length <= 100`
- `0 <= tips[i] <= 1000`
- Result fits in a 32-bit signed integer

Examples:

- `tips = [5, 1, 8, 4, 7]` → `20`  
  Choose indices `0, 2, 4` → `5 + 8 + 7 = 20`

- `tips = [10, 3, 2, 9]` → `19`  
  Choose indices `0, 3` → `10 + 9 = 19`

The key constraint is the non-adjacent rule. That dependency between neighboring decisions is exactly what rules out a simple greedy strategy and points to dynamic programming.

## 🪜 How to Solve This
1. Read the problem → every table creates a binary choice: take it or skip it.

2. Notice the dependency → if you take table `i`, you are forced to skip `i - 1`. That means the best answer at position `i` depends on earlier optimal answers, not just on `tips[i]`.

3. Try the two possibilities for each index:
   - Skip `i` → total stays whatever was best through `i - 1`
   - Take `i` → add `tips[i]` to whatever was best through `i - 2`

4. That immediately suggests a recurrence:  
   `best[i] = max(best[i - 1], best[i - 2] + tips[i])`

5. Once the recurrence is clear, the rest is mechanical:
   - define base cases for the first one or two tables
   - iterate left to right
   - keep only the previous two states if you want constant space

6. Why this is the right mental model: each decision compresses the full history into just two prior optimal states. That is the hallmark of 1D dynamic programming.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: 1D Dynamic Programming.**  
   This is the classic “pick or skip with adjacency exclusion” structure. The right abstraction is DP because each position has overlapping subproblems and optimal substructure.

2. **Define the state.**  
   Let `dp[i]` be the maximum total tips obtainable from tables `0..i`.  
   Invariant: after computing `dp[i]`, it represents the optimal answer for that prefix under the non-adjacent constraint.

3. **Set base cases.**  
   - `dp[0] = tips[0]`
   - `dp[1] = max(tips[0], tips[1])`  
   Why correct: with one table, either take it or not; with two tables, you can take at most one.

4. **Transition for each later table.**  
   For `i >= 2`:  
   `dp[i] = max(dp[i - 1], dp[i - 2] + tips[i])`  
   Why correct:
   - `dp[i - 1]` covers all optimal solutions that skip table `i`
   - `dp[i - 2] + tips[i]` covers all optimal solutions that take table `i`

5. **Maintain the invariant left to right.**  
   Each step only depends on already-correct prior states, so induction holds: if `dp[i - 1]` and `dp[i - 2]` are optimal, then `dp[i]` is optimal.

6. **Return the final prefix optimum.**  
   The answer is `dp[n - 1]`.

7. **Space optimization.**  
   Because only `i - 1` and `i - 2` are needed, replace the array with two rolling variables. Same correctness, lower memory, slightly less traceability.

## 📊 Worked Example
Use `tips = [5, 1, 8, 4, 7]`.

| i | tips[i] | take = dp[i-2] + tips[i] | skip = dp[i-1] | dp[i] |
|---|---------|---------------------------|----------------|-------|
| 0 | 5       | —                         | —              | 5     |
| 1 | 1       | —                         | —              | 5     |
| 2 | 8       | 5 + 8 = 13                | 5              | 13    |
| 3 | 4       | 5 + 4 = 9                 | 13             | 13    |
| 4 | 7       | 13 + 7 = 20               | 13             | 20    |

Trace:

1. Start with `dp[0] = 5`.
2. For the first two tables, best is `max(5, 1) = 5`.
3. At index `2`, taking `8` with `dp[0]` beats skipping it.
4. At index `3`, `4` does not improve the total.
5. At index `4`, taking `7` plus `dp[2] = 13` gives `20`.

Final answer: `20`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is `tips.length`, because each table is processed once and each step does constant work: one addition and one `max`. At `10^6` elements this is still operationally cheap. At `10^9`, linear time becomes throughput-bound, but the algorithm remains asymptotically optimal for a full scan.

### Space Complexity
`O(n)` with a DP array, owned entirely by the prefix-state table. This can be reduced to `O(1)` by keeping only the previous two DP values, trading away full intermediate-state visibility, which is sometimes useful for debugging or reconstructing choices.

## 💡 Key Takeaways
- If each element creates a **take-or-skip** decision and taking one blocks its neighbor, think 1D dynamic programming immediately.
- If the optimal answer for index `i` can be expressed from `i-1` and `i-2`, you are looking at a prefix DP recurrence, not a greedy problem.
- Handle `n = 1` and `n = 2` explicitly; most bugs here come from assuming `dp[1]` exists without guarding array bounds.
- Do not write the transition as `dp[i - 1] + tips[i]`; that illegally combines adjacent selections and silently overcounts.
- The transferable design insight is state compression: many production optimizers only need a tiny rolling frontier of prior decisions, not the full history.

## 🚀 Variations & Further Practice
- **Circular tables variant**: first and last tables are also adjacent. The twist is splitting the problem into two linear runs: exclude first or exclude last.
- **Recover the chosen tables, not just the total**: same DP core, but now you need parent reconstruction or reverse traversal logic.
- **Distance-`k` exclusion**: choosing table `i` blocks the next `k` tables. The recurrence generalizes, but indexing and base-case handling become more error-prone.