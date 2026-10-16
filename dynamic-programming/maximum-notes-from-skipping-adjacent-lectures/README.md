# Maximum Notes from Skipping Adjacent Lectures

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an integer array `notes`, choose a subset of lecture values such that no two chosen lectures are adjacent, and return the maximum possible sum. You are allowed to skip every lecture, so the result can be `0` when all values are negative. The non-trivial part is the adjacency constraint: each decision affects the next index, which rules out simple greedy selection and points to a stateful optimization approach.

## 🌍 Engineering Impact
This pattern shows up anywhere local choices create exclusion windows: ad-slot selection with cooldowns, batch job scheduling on constrained workers, portfolio picks under conflict rules, and stream processing where adjacent events cannot both be promoted. At production scale, greedy heuristics fail because a strong local choice can block a better downstream plan. Dynamic programming gives a deterministic linear-time optimizer with predictable memory behavior. That matters when this logic sits inside ranking pipelines, schedulers, or edge services processing millions of items, where quadratic exploration is operationally unacceptable and unstable decision quality compounds across requests.

## 🔍 Problem Statement
You are given an integer array `notes` of length `n`, where `notes[i]` is the value of attending the `i`-th lecture. You may attend any set of lectures as long as no two attended lectures are adjacent. Return the maximum total note value obtainable.

You may also choose to attend no lectures, so the answer must never be negative.

**Constraints**
- `1 <= notes.length <= 100000`
- `-1000 <= notes[i] <= 1000`

These bounds make exhaustive search infeasible: there are exponentially many subsets, so the solution must run in linear time.

**Examples**
- `notes = [5, 1, 6, 2, 4]` → `15`  
  Attend `5`, `6`, and `4`.
- `notes = [4, -2, 3, 7]` → `11`  
  Attend `4` and `7`.

Edge cases matter:
- single-element arrays
- all-negative arrays, where answer is `0`
- alternating high/low values, where local comparisons can be misleading

## 🪜 How to Solve This
1. Start from the decision at one index: for lecture `i`, there are only two valid choices → skip it, or attend it.
2. If you skip lecture `i`, your best total is whatever the best answer was up to `i - 1`.
3. If you attend lecture `i`, you cannot attend `i - 1`, so the best total becomes `notes[i] + best up to i - 2`.
4. That gives the recurrence:  
   `best[i] = max(best[i - 1], best[i - 2] + notes[i])`
5. Now notice the recurrence only depends on the previous two states. You do not need a full DP array unless you want traceability.
6. Initialize carefully so skipping everything remains legal. That means the running best should never be forced below `0`.
7. Iterate once through the array, updating:
   - previous best excluding current adjacency conflict
   - current best overall
8. This is the standard **Dynamic Programming on a linear array with exclusion constraints** pattern: each position compresses the optimal history into a constant-size state.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Linear Dynamic Programming.**  
   The array is processed left to right, and each position depends only on earlier optimal subproblems. The adjacency rule creates a one-step exclusion window, which is exactly what DP handles well.

2. **Define the state.**  
   Let `dp[i]` be the maximum sum achievable using lectures from index `0` to `i`, with no adjacent selections. This state is sufficient because only the prefix matters; future choices do not change the optimal value already computed for that prefix.

3. **Write the transition.**  
   For lecture `i`, either:
   - skip it → value stays `dp[i - 1]`
   - take it → value becomes `dp[i - 2] + notes[i]`  
   Therefore:  
   `dp[i] = max(dp[i - 1], dp[i - 2] + notes[i])`

4. **Handle base cases correctly.**  
   Since attending nothing is allowed:
   - `dp[-1]` can be treated as `0`
   - `dp[0] = max(0, notes[0])`  
   This prevents negative totals from propagating.

5. **Maintain the invariant.**  
   After processing index `i`, the current state stores the optimal non-adjacent sum for the prefix `notes[0..i]`. This invariant guarantees correctness because every valid solution for the prefix either includes `i` or does not.

6. **Compress space.**  
   Only `dp[i - 1]` and `dp[i - 2]` are needed to compute `dp[i]`. Replace the array with two variables, often called `prev1` and `prev2`, and update them in one pass.

7. **Return the final best value.**  
   After the last index, the running optimum is the answer. This yields linear time and constant auxiliary space, which is the right trade-off for `n` up to `100000`.

## 📊 Worked Example
Example: `notes = [5, 1, 6, 2, 4]`

Let:
- `prev2` = best up to `i - 2`
- `prev1` = best up to `i - 1`

| i | notes[i] | take = prev2 + notes[i] | skip = prev1 | curr | New prev2 | New prev1 |
|---|----------|--------------------------|--------------|------|-----------|-----------|
| 0 | 5        | 0 + 5 = 5                | 0            | 5    | 0         | 5         |
| 1 | 1        | 0 + 1 = 1                | 5            | 5    | 5         | 5         |
| 2 | 6        | 5 + 6 = 11               | 5            | 11   | 5         | 11        |
| 3 | 2        | 5 + 2 = 7                | 11           | 11   | 11        | 11        |
| 4 | 4        | 11 + 4 = 15              | 11           | 15   | 11        | 15        |

Final answer: `15`

Chosen lectures correspond to values `5`, `6`, and `4`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time because each lecture is processed exactly once, and each step does constant work: one addition and one comparison. At `10^6` elements this is routine in a single pass; at `10^9`, linear time is still expensive but remains the only viable asymptotic class for exact evaluation.

### Space Complexity
The optimized version uses **O(1)** auxiliary space. The only extra memory is a few scalar variables holding prior DP states. A full DP array would raise this to **O(n)**, which can help debugging or reconstruction but is unnecessary for the final sum.

## 💡 Key Takeaways
- If a problem asks for a maximum or minimum over an array with a “cannot take adjacent items” rule, it is a strong signal for linear dynamic programming.
- When each choice only conflicts with a fixed local neighborhood, look for a recurrence that compares “take current” versus “skip current.”
- The base case must allow `0`; otherwise all-negative inputs incorrectly return a negative value instead of “attend nothing.”
- Be careful updating rolling variables: compute `curr` before overwriting the state needed for the next transition.
- The transferable design insight is that exclusion constraints often collapse into a tiny state machine, letting you replace combinatorial search with a streaming optimizer.

## 🚀 Variations & Further Practice
- **House Robber II**: same recurrence, but the array is circular, so the first and last elements also conflict; this forces decomposition into two linear runs.
- **Delete and Earn**: values must be aggregated by number, then adjacent numeric values conflict; the harder part is transforming the domain before applying the same DP pattern.
- **Maximum sum with distance `k` exclusion**: taking one item blocks the next `k` items, generalizing the recurrence and increasing the state dependency window.