# Maximum Comfort from Skipping Adjacent Hotel Nights

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `comfort`, where `comfort[i]` is the comfort score of booking the hotel on night `i`, compute the maximum total comfort obtainable by choosing nights such that no two chosen nights are adjacent. The output is a single integer: the best achievable sum. The problem is non-trivial because each local choice affects the next one, so greedy selection by largest value does not reliably produce the global optimum.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize value under local exclusion constraints. Examples include ad scheduling where adjacent slots cannot both be sold to the same campaign, workload placement where neighboring time windows must remain idle for cooling or maintenance, and streaming pipelines that suppress consecutive expensive enrichments to stay within latency budgets. At scale, brute-force exploration becomes impossible because the choice graph grows exponentially. Dynamic programming turns that into a linear pass with predictable memory, which matters for hot paths, embedded schedulers, and services processing millions of decisions per second.

## 🔍 Problem Statement
You are given an integer array `comfort` with `1 <= comfort.length <= 100` and `0 <= comfort[i] <= 1000`. Each element represents the comfort score of staying at a hotel on that night of a road trip. You may choose any subset of nights, but you cannot choose two consecutive nights.

Return the maximum possible total comfort score.

Examples:

- `comfort = [6, 7, 1, 30, 8, 2, 4]` → `41`  
  One optimal choice is `7 + 30 + 4 = 41`.

- `comfort = [5, 1, 1, 5]` → `10`  
  Choose the first and last nights.

The key constraint is adjacency: taking night `i` blocks night `i - 1` and `i + 1`. That dependency makes this a classic one-dimensional dynamic programming problem rather than a simple summation or greedy selection task.

## 🪜 How to Solve This
1. Read the constraint carefully → the only thing that matters about a chosen night is that it forbids its immediate neighbor. That usually signals a linear DP over prefixes.

2. Focus on one night at a time → for night `i`, there are only two meaningful decisions:
   - skip it, so the best total stays whatever was best through `i - 1`
   - take it, so you add `comfort[i]` to the best total through `i - 2`

3. That gives the recurrence immediately:  
   `best[i] = max(best[i - 1], comfort[i] + best[i - 2])`

4. Why this works → every valid solution for prefix `0..i` must either include `i` or exclude it. There is no third case, and those two cases reduce cleanly to smaller subproblems.

5. Notice the recurrence only depends on the previous two states → you do not need a full DP array unless you want traceability. Two rolling variables are enough.

6. The result is a single left-to-right pass, which is optimal for the required `O(n)` runtime and easy to reason about in code review.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: linear dynamic programming with rolling state.**  
   This is the same abstraction as the classic House Robber problem: maximize cumulative value under a “no adjacent picks” constraint. The right mental model is “best answer for every prefix.”

2. **Define the state.**  
   Let `dp[i]` be the maximum comfort obtainable using nights `0..i`. This state is sufficient because future decisions only need the best totals up to the previous one or two positions.

3. **Write the transition.**  
   For each index `i`, either:
   - skip night `i`, yielding `dp[i - 1]`
   - take night `i`, yielding `comfort[i] + dp[i - 2]`  
   Therefore: `dp[i] = max(dp[i - 1], comfort[i] + dp[i - 2])`

4. **Establish base cases.**  
   For one night, `dp[0] = comfort[0]`.  
   For two nights, `dp[1] = max(comfort[0], comfort[1])`.  
   These anchor the recurrence and handle the smallest valid inputs.

5. **Maintain the invariant during iteration.**  
   After processing index `i`, `dp[i]` is the optimal answer for the prefix `0..i`. This invariant holds because the recurrence exhausts all valid possibilities for the current night.

6. **Optimize space.**  
   Since `dp[i]` depends only on `dp[i - 1]` and `dp[i - 2]`, keep two variables, often named `prev1` and `prev2`. After each step, shift them forward. This preserves correctness while reducing extra space from `O(n)` to `O(1)`.

## 📊 Worked Example
Use `comfort = [6, 7, 1, 30, 8, 2, 4]`.

| i | comfort[i] | take = comfort[i] + prev2 | skip = prev1 | current |
|---|------------|----------------------------|--------------|---------|
| 0 | 6          | 6                          | 0            | 6       |
| 1 | 7          | 7                          | 6            | 7       |
| 2 | 1          | 7                          | 7            | 7       |
| 3 | 30         | 37                         | 7            | 37      |
| 4 | 8          | 15                         | 37           | 37      |
| 5 | 2          | 39                         | 37           | 39      |
| 6 | 4          | 41                         | 39           | 41      |

Trace:
- After night 3, taking `30` plus the best through night 1 gives `37`.
- Night 4 is skipped because `37` is better than `8 + 7 = 15`.
- Night 6 is taken because `4 + 37 = 41` beats skipping with `39`.

Final answer: `41`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because it processes each night exactly once and performs constant work per element: one addition and one `max`. At `10^6` elements this is still practical in a tight loop; at `10^9`, linear time becomes throughput-bound and requires distributed or streaming considerations, but no asymptotically faster exact solution exists for arbitrary input.

### Space Complexity
The optimized version uses `O(1)` extra space, owned entirely by two rolling DP variables. A full DP array would use `O(n)` space and can help with debugging or reconstructing decisions, but it is unnecessary if only the maximum score is required.

## 💡 Key Takeaways
- If a problem asks for a maximum total over a sequence with a “cannot choose adjacent items” rule, think one-dimensional dynamic programming over prefixes.
- When each decision at index `i` reduces to “take current” vs. “skip current,” the recurrence often depends only on the previous one or two states.
- Be careful with base cases for arrays of length `1` and `2`; most bugs in this problem come from incorrect initialization rather than the recurrence.
- In the rolling-state version, update variables in the right order; overwriting `prev2` too early corrupts the `take` calculation.
- The production-grade insight is that local exclusion constraints often collapse exponential search into a compact state machine, enabling deterministic latency and memory usage.

## 🚀 Variations & Further Practice
- **House Robber II**: same recurrence, but the array is circular, so the first and last elements are also adjacent; this forces decomposition into two linear runs.
- **Delete and Earn**: values map into buckets, and choosing one value excludes neighboring values numerically rather than by index; the twist is transforming the input before applying the same DP pattern.
- **Paint House / Task scheduling with cooldowns**: the exclusion rule expands beyond immediate adjacency or introduces multiple state dimensions, requiring richer DP state design.