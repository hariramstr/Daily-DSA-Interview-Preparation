# Minimum Fatigue to Plan Alternating Workout Intensities

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Array, State Transition

---

## 🗂 Problem Overview
Given `n` workout days, each day has three fatigue costs: low, medium, and high intensity. You must choose exactly one intensity per day, with the restriction that the same intensity cannot be used on consecutive days. The goal is to return the minimum total fatigue across all days. The problem is non-trivial because the cheapest choice for one day can block a better sequence later, so local greedy decisions are not reliable.

## 🌍 Engineering Impact
This pattern shows up anywhere a system makes sequential choices under adjacency constraints: traffic shaping across network paths, job scheduling across heterogeneous workers, search ranking diversification, ad serving rotation, and power-state management in mobile or embedded systems. The operational issue is the same: a locally optimal action can degrade the next decision boundary. At scale, brute-force exploration is impossible and greedy heuristics silently accumulate cost or instability. Dynamic programming gives a bounded-state model: preserve only the minimal cost of ending in each valid state, enabling linear-time planning with predictable memory and no combinatorial explosion.

## 🔍 Problem Statement
You are given a 2D array `costs` where `costs[i][0]`, `costs[i][1]`, and `costs[i][2]` represent the fatigue cost of performing day `i` at low, medium, and high intensity respectively.

Choose one intensity for every day such that no two consecutive days use the same intensity. Return the minimum possible total fatigue.

**Constraints**
- `1 <= n == costs.length <= 100000`
- `costs[i].length == 3`
- `1 <= costs[i][j] <= 10^4`
- Answer fits in a 32-bit signed integer

**Examples**
- `costs = [[5,3,4],[6,2,7],[3,8,1]]` → minimum total fatigue is `6`
- `costs = [[1,10,100],[10,1,100],[100,10,1],[1,100,10]]` → `4`

The key constraint is `n` up to `100000`: exponential search over all valid intensity sequences is infeasible, and even quadratic work is unnecessary. The structure strongly suggests a linear scan with constant-sized state.

## 🪜 How to Solve This
1. Read the rule carefully → today’s valid choices depend only on yesterday’s intensity, not the full history.
2. That dependency is the signal for dynamic programming → define the best total fatigue up to day `i` for each possible ending intensity.
3. For a given day and intensity, the previous day can only be one of the other two intensities → each state transition is just a `min` over two prior states.
4. Because there are only three intensities, the DP state is tiny and fixed-size → no need for an `n x 3` table unless you want easier debugging.
5. Initialize day `0` directly from `costs[0]` → the minimum cost to end in low/medium/high on the first day is just that day’s cost.
6. Iterate forward, updating the three states for each day.
7. After the last day, take the minimum of the three ending states.

The core insight: this is not “pick the cheapest valid option each day.” It is “carry forward the cheapest way to arrive at each ending mode,” which is exactly the dynamic programming mental model.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Dynamic Programming with state transition.**  
   The decision for day `i` depends only on which intensity was chosen on day `i-1`. That makes this a classic finite-state DP: three states per day, one for each ending intensity.

2. **Define the state.**  
   Let:
   - `dpLow` = minimum total fatigue up to the current day if today is low
   - `dpMed` = minimum total fatigue up to the current day if today is medium
   - `dpHigh` = minimum total fatigue up to the current day if today is high  
   Invariant: after processing day `i`, each variable stores the optimal cost among all valid plans ending in that intensity.

3. **Initialize from day 0.**  
   For the first day, there is no previous constraint:
   - `dpLow = costs[0][0]`
   - `dpMed = costs[0][1]`
   - `dpHigh = costs[0][2]`  
   This is correct because any intensity is allowed on day 0.

4. **Transition for each later day.**  
   For day `i`:
   - `newLow = costs[i][0] + min(dpMed, dpHigh)`
   - `newMed = costs[i][1] + min(dpLow, dpHigh)`
   - `newHigh = costs[i][2] + min(dpLow, dpMed)`  
   Each transition excludes the same intensity from the previous day, exactly matching the rule.

5. **Roll the state forward.**  
   Replace the old values with `newLow`, `newMed`, `newHigh`.  
   This preserves the invariant while using constant extra space.

6. **Return the final answer.**  
   The optimal full plan can end in any intensity, so return `min(dpLow, dpMed, dpHigh)`.

This abstraction is right because the problem has a small, fixed number of mutually exclusive states and local transition rules between adjacent positions.

## 📊 Worked Example
Use `costs = [[1,10,100],[10,1,100],[100,10,1],[1,100,10]]`.

| Day | Costs (L,M,H) | dpLow | dpMed | dpHigh |
|---|---:|---:|---:|---:|
| 0 | (1,10,100) | 1 | 10 | 100 |
| 1 | (10,1,100) | 10 + min(10,100) = 20 | 1 + min(1,100) = 2 | 100 + min(1,10) = 101 |
| 2 | (100,10,1) | 100 + min(2,101) = 102 | 10 + min(20,101) = 30 | 1 + min(20,2) = 3 |
| 3 | (1,100,10) | 1 + min(30,3) = 4 | 100 + min(102,3) = 103 | 10 + min(102,30) = 40 |

Final answer is `min(4, 103, 40) = 4`.

One optimal sequence is:
- Day 0: low
- Day 1: medium
- Day 2: high
- Day 3: low

Total fatigue = `1 + 1 + 1 + 1 = 4`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each day performs a constant amount of work: three state updates, each using a `min` over two values. For `10^6` days this is still practical as a single linear pass; for `10^9`, the algorithmic form is optimal but runtime becomes constrained by raw CPU and memory bandwidth.

### Space Complexity
`O(1)` extra space. The algorithm only keeps three running DP values and three temporary next-state values. If desired, an `O(n)` DP table can be used for traceability or path reconstruction, but it is unnecessary for computing the minimum cost alone.

## 💡 Key Takeaways
- If each position has a small fixed set of choices and validity depends only on the previous choice, think state-transition dynamic programming immediately.
- When the objective is a global minimum under local adjacency constraints, greedy selection is a red flag; carry forward best costs per ending state instead.
- Initialize day `0` directly from the input; introducing a fake previous day often creates incorrect transitions or unnecessary branching.
- Compute next-state values before overwriting current ones; in-place updates without temporaries will mix day `i-1` and day `i` state.
- The production-grade insight is state compression: when only the previous frontier matters, retain just that frontier and eliminate table growth without losing optimality.

## 🚀 Variations & Further Practice
- **`k` intensity modes instead of 3**: same DP idea, but transitions become “best previous state excluding current mode,” which pushes you toward tracking the smallest and second-smallest prior values for `O(nk)` or better.
- **Circular schedule**: first and last day also cannot use the same intensity. This breaks the simple linear boundary condition and typically requires fixing the first choice and solving multiple constrained passes.
- **Recover the actual plan, not just the minimum cost**: requires storing predecessor choices or reconstructible metadata, trading `O(1)` space for path reconstruction capability.