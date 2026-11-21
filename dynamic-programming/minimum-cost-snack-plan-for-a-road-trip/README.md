# Minimum Cost Snack Plan for a Road Trip

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, state-transition

---

## 🗂 Problem Overview
Given `n` days of snack prices, choose exactly one of three snack types per day and minimize total cost. The constraint is that the same snack cannot be chosen on consecutive days. Input is an `n x 3` cost matrix; output is a single integer: the minimum achievable total cost. The problem is non-trivial because each day’s optimal choice depends on the previous day’s choice, so greedy local minimization is insufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere a sequence of decisions has a small stateful exclusion rule: job scheduling with cooldowns, ad rotation systems that cannot repeat creatives back-to-back, streaming pipelines selecting fallback processors, and search/ranking systems enforcing diversity constraints across adjacent results. At scale, naive enumeration explodes combinatorially, while greedy heuristics violate global optimality. Dynamic programming turns a history-dependent optimization into a bounded-state transition system. That matters operationally: predictable runtime, easy correctness reasoning, and a clean path from prototype logic to production-grade implementations with constant memory and stable latency.

## 🔍 Problem Statement
You are given `costs`, an `n x 3` integer matrix where `costs[i][0]`, `costs[i][1]`, and `costs[i][2]` represent the cost of buying fruit, sandwich, and trail mix on day `i`. For each of the `n` days, you must buy exactly one snack pack. You may not choose the same snack type on two consecutive days.

Return the minimum total cost across all `n` days.

Constraints:

- `1 <= n <= 1000`
- `costs.length == n`
- `costs[i].length == 3`
- `1 <= costs[i][j] <= 1000`

Examples:

- `costs = [[4,2,7],[3,6,1],[8,5,4]]` → `7`
- `costs = [[1,10,3],[10,2,8],[4,7,1],[6,3,9]]` → `7`

The key algorithmic constraint is the adjacency rule: the best choice on day `i` depends on which snack was chosen on day `i-1]`, which makes this a state-transition dynamic programming problem rather than a simple per-row minimum scan.

## 🪜 How to Solve This
1. Read the constraint carefully → the only thing that matters from the past is yesterday’s snack choice, not the full history.

2. That immediately suggests a small state model → for each day, track the minimum total cost if today ends with fruit, sandwich, or trail mix.

3. Define the transition → if today ends with fruit, yesterday must have ended with either sandwich or trail mix. So today’s fruit cost is `costs[i][0] + min(prevSandwich, prevTrailMix)`.

4. Repeat the same logic for the other two snack types → each state depends only on the other two states from the previous day.

5. Initialize from day `0` → if the trip has only one day, the answer is simply the minimum of that row.

6. Observe the memory pattern → each day only needs the previous day’s three values, so a full `n x 3` DP table is optional. Constant-space rolling state is enough.

7. Final answer → after processing all days, take the minimum among the three states for the last day.

This is the standard “sequence optimization with a small forbidden previous state” dynamic programming pattern.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Dynamic Programming with state transition.**  
   The problem has optimal substructure: the cheapest valid plan up to day `i` ending in snack `s` depends on the cheapest valid plan up to day `i-1` ending in a different snack. That makes DP the right abstraction.

2. **Define the state.**  
   Let `dpFruit`, `dpSandwich`, and `dpTrail` represent the minimum total cost after processing the current day, with that specific snack chosen today.  
   Invariant: after day `i`, each variable stores the minimum valid total cost among all plans ending in that snack.

3. **Initialize day 0.**  
   Set:
   - `dpFruit = costs[0][0]`
   - `dpSandwich = costs[0][1]`
   - `dpTrail = costs[0][2]`  
   This is correct because no adjacency constraint applies before the first day.

4. **Process each later day.**  
   For day `i`, compute:
   - `newFruit = costs[i][0] + min(dpSandwich, dpTrail)`
   - `newSandwich = costs[i][1] + min(dpFruit, dpTrail)`
   - `newTrail = costs[i][2] + min(dpFruit, dpSandwich)`  
   Each transition enforces the “no same snack on consecutive days” rule by excluding the matching prior state.

5. **Roll the state forward.**  
   Replace the old DP values with the new ones.  
   Invariant remains preserved: each variable still means “minimum valid total cost ending with this snack on the current day.”

6. **Return the best terminal state.**  
   The trip may end with any snack, so the answer is `min(dpFruit, dpSandwich, dpTrail)`.

This works in linear time because each day performs a constant amount of work over a fixed-size state space.

## 📊 Worked Example
Use `costs = [[1,10,3],[10,2,8],[4,7,1],[6,3,9]]`.

| Day | Fruit | Sandwich | Trail Mix | Explanation |
|---|---:|---:|---:|---|
| 0 | 1 | 10 | 3 | Base costs |
| 1 | 10 + min(10,3) = 13 | 2 + min(1,3) = 3 | 8 + min(1,10) = 9 | Cannot repeat day 0 snack |
| 2 | 4 + min(3,9) = 7 | 7 + min(13,9) = 16 | 1 + min(13,3) = 4 | Best paths ending in each snack |
| 3 | 6 + min(16,4) = 10 | 3 + min(7,4) = 7 | 9 + min(7,16) = 16 | Final transitions |

Final answer: `min(10, 7, 16) = 7`.

One optimal plan is:
- Day 0: fruit (`1`)
- Day 1: sandwich (`2`)
- Day 2: trail mix (`1`)
- Day 3: sandwich (`3`)

Total = `1 + 2 + 1 + 3 = 7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each day performs a fixed number of arithmetic and `min` operations over exactly three states. There is no nested iteration over prior days. At `10^6` rows this is still operationally cheap; at `10^9`, linear work becomes the bottleneck regardless of the constant factor, so throughput and streaming execution would dominate design concerns.

### Space Complexity
`O(1)` auxiliary space with rolling variables for the three prior-state costs. A full DP table would be `O(n)`, but it adds storage without improving asymptotic runtime. The trade-off is debuggability versus memory efficiency.

## 💡 Key Takeaways
- If a sequence problem says “current choice depends only on the previous choice,” look for a small-state dynamic programming transition.
- If each position has a fixed set of options and one local exclusion rule, model DP by “minimum cost ending in option X.”
- Do not update the three state values in place without temporaries; later transitions would accidentally read already-mutated values from the same day.
- Initialize day `0` directly from `costs[0]`; adding a fake previous day often introduces off-by-one bugs or invalid default minima.
- In production systems, bounded-state DP is a useful pattern for converting history-sensitive optimization into predictable, constant-memory streaming computation.

## 🚀 Variations & Further Practice
- Generalize from 3 snack types to `k` types; the twist is reducing the naive `O(n * k^2)` transition cost, often by tracking the smallest and second-smallest previous values.
- Add a penalty or cooldown window of length `m` instead of only forbidding immediate repetition; the harder part is expanding the state to encode more history without blowing up memory.
- Require reconstruction of the actual snack sequence, not just the minimum cost; this adds predecessor tracking and changes the space trade-off.