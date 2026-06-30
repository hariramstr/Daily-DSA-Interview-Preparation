# Maximum Tips from Non-Consecutive Deliveries

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `tips`, choose a subset of delivery requests such that no two chosen requests are adjacent, and return the maximum total tip. The input is a linear sequence, the output is a single integer, and every request is optional. The non-trivial part is that a locally attractive choice can block a better future combination, so greedy selection is unreliable. The solution needs to evaluate prefix-optimal decisions efficiently for up to `100000` requests.

## 🌍 Engineering Impact
This pattern appears anywhere a system must maximize value under local exclusion constraints. Examples include ad-slot selection where adjacent placements conflict, streaming job schedulers with cooldown windows, warehouse pick-path batching where neighboring tasks contend for the same resource, and mobile notification ranking with anti-spam spacing rules. At scale, brute force or backtracking collapses immediately because the decision tree doubles per item. Dynamic programming turns an exponential search into a linear pass, which is the difference between something usable in a hot path and something that times out or forces coarse heuristics.

## 🔍 Problem Statement
You are given an integer array `tips` where `tips[i]` is the tip earned from accepting the `i`-th delivery request. You may accept any number of requests, but you cannot accept two consecutive requests because each accepted delivery imposes a cooldown on the next adjacent request.

Return the maximum total tip obtainable.

Constraints:

- `1 <= tips.length <= 100000`
- `0 <= tips[i] <= 10000`
- The answer fits in a 32-bit signed integer

Examples:

- `tips = [5, 1, 2, 10, 6]` → `15`  
  Accept requests with tips `5` and `10`.

- `tips = [4, 7, 3, 9]` → `16`  
  Accept requests with tips `7` and `9`.

Edge cases matter: a single element returns itself, all zeros return `0`, and the large input bound rules out exponential recursion or any solution that revisits subproblems repeatedly.

## 🪜 How to Solve This
1. Read the constraint carefully → choosing request `i` only conflicts with request `i-1`. That means each decision depends on very little history.

2. For every position, there are only two meaningful choices:
   - skip the current request
   - take the current request, which forces the previous one to be skipped

3. That immediately suggests a prefix-based recurrence:
   - best answer up to `i`
   - derived from answers up to `i-1` and `i-2`

4. Write the choice mathematically:
   - skip `i` → keep the best total from the first `i-1` requests
   - take `i` → add `tips[i]` to the best total from the first `i-2` requests

5. So the transition becomes:
   `dp[i] = max(dp[i-1], dp[i-2] + tips[i])`

6. Once that recurrence is visible, the rest is implementation detail. You do not need search, sorting, or greedy heuristics. This is a one-dimensional dynamic programming problem over an array, and because each state only depends on the previous two, it can be reduced to constant space.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: 1D Dynamic Programming over prefixes.**  
   The problem asks for an optimal value under a local adjacency constraint. That is the classic signal for DP: the best answer for a prefix can be composed from smaller prefixes.

2. **Define the state.**  
   Let `dp[i]` be the maximum tip obtainable from requests `0..i`. This state is sufficient because any valid choice set for prefix `0..i` must either include `i` or exclude it.

3. **Derive the transition.**  
   If request `i` is skipped, the best total remains `dp[i-1]`. If request `i` is accepted, request `i-1` cannot be accepted, so the best compatible total is `dp[i-2] + tips[i]`. Therefore:  
   `dp[i] = max(dp[i-1], dp[i-2] + tips[i])`  
   This is correct because these two cases are exhaustive and mutually exclusive.

4. **Initialize base cases carefully.**  
   `dp[0] = tips[0]`  
   `dp[1] = max(tips[0], tips[1])`  
   These establish the invariant that `dp[i]` is the optimal answer for prefix `0..i`.

5. **Iterate left to right.**  
   For each index from `2` onward, apply the recurrence once. After processing index `i`, the invariant still holds: `dp[i]` is optimal for the first `i+1` requests.

6. **Optimize space if desired.**  
   Since only `dp[i-1]` and `dp[i-2]` are needed, replace the array with two rolling variables. Same recurrence, same correctness, lower memory footprint.

## 📊 Worked Example
Example: `tips = [5, 1, 2, 10, 6]`

| i | tips[i] | skip = best up to i-1 | take = best up to i-2 + tips[i] | dp[i] |
|---|---------|------------------------|----------------------------------|-------|
| 0 | 5       | —                      | —                                | 5     |
| 1 | 1       | 5                      | 1                                | 5     |
| 2 | 2       | 5                      | 5 + 2 = 7                        | 7     |
| 3 | 10      | 7                      | 5 + 10 = 15                      | 15    |
| 4 | 6       | 15                     | 7 + 6 = 13                       | 15    |

Trace:

1. Start with `dp[0] = 5`.
2. At index `1`, better to keep `5` than take `1`.
3. At index `2`, taking `2` with prior best non-adjacent total gives `7`.
4. At index `3`, taking `10` plus `dp[1] = 5` yields `15`.
5. At index `4`, taking `6` only reaches `13`, so keep `15`.

Final answer: `15`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = tips.length`. The algorithm performs a single left-to-right pass, and each position does constant work: one addition and one `max` comparison. At `10^6` elements this is still practical in most runtimes; at `10^9`, linear time becomes throughput-bound and usually infeasible without partitioning or a different problem model.

### Space Complexity
`O(1)` with rolling variables, or `O(n)` if a full DP array is stored for clarity. The only required state is the previous two optimal totals, so the array is unnecessary unless you want easier debugging or state reconstruction.

## 💡 Key Takeaways
- If the problem says “maximize total” with a “cannot take adjacent items” constraint, think 1D dynamic programming over prefixes immediately.
- When each decision only conflicts with the previous item, the recurrence usually depends on `i-1` and `i-2`, not the full history.
- Base cases are the main source of bugs: handle `n = 1` explicitly before reading `dp[1]` or `tips[1]`.
- Be precise about indexing: “take current” combines with `dp[i-2]`, not `dp[i-1]`, or you silently allow adjacent selections.
- In production systems, this pattern is a reminder that local exclusion constraints often collapse to tiny state machines, enabling linear-time optimization in hot paths.

## 🚀 Variations & Further Practice
- **Circular deliveries / House Robber II:** first and last requests are also adjacent, so the linear DP must be run twice over different ranges.
- **Delete and Earn:** choosing a value removes neighboring values numerically, not positionally; the twist is transforming frequency counts into a linear DP domain.
- **Weighted interval scheduling:** requests have arbitrary start/end conflicts instead of simple adjacency, requiring binary search plus DP over compatible predecessors.