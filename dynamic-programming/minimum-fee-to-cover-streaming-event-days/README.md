# Minimum Fee to Cover Streaming Event Days

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, memoization

---

## 🗂 Problem Overview
Given a sorted list of event days and fixed prices for 1-day, 7-day, and 30-day reservation passes, compute the minimum total cost needed to cover every required day. The challenge is that a locally cheap choice can block a globally optimal plan: buying several 1-day passes may cost more than one longer pass that also covers future event days. The input is small enough for dynamic programming, but large enough that brute-force pass combinations are unnecessary and error-prone.

## 🌍 Engineering Impact
This pattern shows up anywhere coverage windows must be purchased or allocated against sparse demand: cloud reserved capacity planning, CDN burst shielding, API quota bundles, ad-campaign pacing windows, and workforce scheduling with overlapping shifts. The hard part is not evaluating one interval, but choosing a sequence of intervals whose value depends on future demand density. Without a dynamic-programming view, teams often ship greedy heuristics that look reasonable in dashboards but overpay under clustered workloads. Modeling the problem as “minimum future cost from this demand point” enables deterministic optimization, explainable trade-offs, and straightforward extension to additional pass types or pricing tiers.

## 🔍 Problem Statement
You are given:

- `days`: a strictly increasing integer array of event days
- `costs = [cost1, cost7, cost30]`: prices for 1-day, 7-day, and 30-day passes

A pass bought on day `d` covers day `d` and the next consecutive days within its duration:

- 1-day pass → covers `d`
- 7-day pass → covers `d..d+6`
- 30-day pass → covers `d..d+29`

Return the minimum total fee required to cover every day in `days`.

Constraints:

- `1 <= days.length <= 365`
- `1 <= days[i] <= 365`
- `days` is strictly increasing
- `1 <= costs[i] <= 1000`

Examples:

- `days = [1,4,6,7,8,20]`, `costs = [2,7,15]` → `11`
- `days = [2,3,4,5,6,7,8,9,15,16,17,40]`, `costs = [3,8,20]` → `19`

The key constraint is sparse required days over a bounded calendar. That makes dynamic programming over event indices or calendar days the natural choice.

## 🪜 How to Solve This
1. Read the problem → notice decisions only matter on required event days. Buying coverage for non-event days is fine, but we never need to make a decision there.

2. At any event index `i`, the real question is: **what is the minimum cost to cover `days[i:]`?** That framing immediately suggests dynamic programming.

3. From one event day, there are only three meaningful choices:
   - buy a 1-day pass
   - buy a 7-day pass
   - buy a 30-day pass

4. Each choice jumps forward to the first event day not already covered. That means the problem has optimal substructure: once a pass is chosen, the remaining work is the same problem on a later index.

5. So define `dp(i)` = minimum cost to cover all event days starting from index `i`.

6. Transition:
   - `cost1 + dp(next index after day + 0)`
   - `cost7 + dp(next index after day + 6)`
   - `cost30 + dp(next index after day + 29)`

7. Memoize results so each index is solved once. The bounded input size also allows a calendar-day DP, but index-based DP maps more directly to the sparse-demand structure.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Dynamic Programming + Memoization.**  
   This is the right abstraction because the problem asks for a minimum over repeated future subproblems. The state can be defined by the next uncovered event index.

2. **Define the state.**  
   Let `dp(i)` be the minimum fee needed to cover all event days from `days[i]` onward.  
   Invariant: before evaluating `dp(i)`, all days before index `i` are already covered.

3. **Handle the base case.**  
   If `i == days.length`, there are no remaining event days, so the cost is `0`.  
   This guarantees recursion terminates and anchors the minimization.

4. **Evaluate the three pass options from `days[i]`.**  
   For each duration in `{1, 7, 30}`, compute the first index `j` such that `days[j]` is outside that pass’s coverage window.  
   Then candidate cost = `passCost + dp(j)`.

5. **Take the minimum candidate.**  
   This is correct because any optimal solution must begin with exactly one of the three pass types covering `days[i]`. No other first move exists.

6. **Memoize by index.**  
   Once `dp(i)` is computed, cache it.  
   Invariant: each index is solved once, turning an exponential decision tree into linear subproblem count.

7. **Return `dp(0)`.**  
   That represents the minimum fee to cover the entire schedule starting from the first event day.

This is a classic **top-down DP over sparse positions**. A bottom-up version or calendar-day DP is also valid, but index-based state avoids iterating irrelevant days.

## 📊 Worked Example
Example: `days = [1,4,6,7,8,20]`, `costs = [2,7,15]`

Let `dp(i)` be min cost from index `i`.

| i | day | 1-day leads to | 7-day leads to | 30-day leads to | dp(i) |
|---|-----|----------------|----------------|-----------------|------:|
| 5 | 20  | 2 + dp(6)=2    | 7 + dp(6)=7    | 15 + dp(6)=15   | 2 |
| 4 | 8   | 2 + dp(5)=4    | 7 + dp(5)=9    | 15 + dp(6)=15   | 4 |
| 3 | 7   | 2 + dp(4)=6    | 7 + dp(5)=9    | 15 + dp(6)=15   | 6 |
| 2 | 6   | 2 + dp(3)=8    | 7 + dp(5)=9    | 15 + dp(6)=15   | 8 |
| 1 | 4   | 2 + dp(2)=10   | 7 + dp(5)=9    | 15 + dp(6)=15   | 9 |
| 0 | 1   | 2 + dp(1)=11   | 7 + dp(4)=11   | 15 + dp(6)=15   | 11 |

Answer: `dp(0) = 11`.

## ⏱ Complexity Analysis
### Time Complexity
With memoization, there are `n` states where `n = days.length`. For each state, we advance through at most the remaining days to find the next uncovered index for 3 pass types, giving `O(n^2)` in the straightforward implementation, or `O(n)` with moving pointers / binary search optimizations. At `10^6` or `10^9` elements, only the linear-style formulation is operationally viable.

### Space Complexity
`O(n)` for the memoization table and recursion stack in the top-down version. The dominant structure is the cached result per event index. This can be reduced to iterative bottom-up DP, trading stack usage for slightly less direct code.

## 💡 Key Takeaways
- If the problem asks for a minimum cost over future coverage choices, and each choice jumps to the next uncovered position, think dynamic programming on index/state.
- Sparse required days are a strong signal to model only meaningful positions rather than every calendar day unless bounded-day DP is simpler.
- Coverage is inclusive: a 7-day pass bought on day `d` covers through `d + 6`, not `d + 7`.
- The next state is the first day **outside** the current pass window; getting that boundary wrong shifts every downstream cost.
- In production optimization work, reframing “which option is cheapest now?” into “what is the cheapest remaining plan from this state?” is often the difference between a heuristic and a provably optimal allocator.

## 🚀 Variations & Further Practice
- Add arbitrary pass durations and prices, possibly dozens of SKUs. The twist is scaling the transition logic efficiently while preserving explainability.
- Introduce per-day demand weights or penalties, where uncovered days incur SLA cost instead of being strictly invalid. The twist is optimizing over mixed hard and soft constraints.
- Extend to multi-resource coverage, where one pass type covers only certain event classes or regions. The twist is state explosion and the need for higher-dimensional DP or decomposition.