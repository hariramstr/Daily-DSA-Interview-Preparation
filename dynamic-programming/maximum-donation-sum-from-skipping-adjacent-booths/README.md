# Maximum Donation Sum from Skipping Adjacent Booths

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `booths`, choose a subset of booth donation amounts such that no two chosen booths are adjacent, and return the maximum possible sum. The input is a single integer array; the output is one integer. The problem is non-trivial because each local choice affects the next one: taking booth `i` immediately forbids booth `i - 1` and `i + 1`, so greedy selection by largest value does not reliably produce the global optimum.

## 🌍 Engineering Impact
This pattern shows up anywhere adjacent work items are mutually exclusive: scheduling maintenance windows on neighboring network segments, selecting non-overlapping promotions in ranking systems, allocating compute jobs that contend for shared hardware lanes, or activating fraud rules that suppress nearby rules in a decision chain. At small scale, brute force is tolerable; at production scale, combinatorial branching explodes and makes latency unpredictable. Dynamic programming turns a globally constrained optimization into a linear pass with stable runtime, which matters for hot-path services, batch planners, and embedded decision engines where predictability is as important as correctness.

## 🔍 Problem Statement
You are given an integer array `booths` where `booths[i]` is the donation amount available from booth `i`. You may open any set of booths as long as no two opened booths are adjacent. Return the maximum total donation sum obtainable under that constraint.

Constraints:

- `1 <= booths.length <= 100`
- `0 <= booths[i] <= 1000`

Input:

- `booths: number[]`

Output:

- A single integer representing the best non-adjacent sum.

Examples:

- `booths = [5, 1, 2, 10, 6]` → `15`
- `booths = [4, 7, 3, 9]` → `16`

Edge cases matter: arrays of length `1`, arrays containing `0`, and prefixes where skipping a larger-looking current booth preserves a better future total. The key constraint is adjacency exclusion, which makes each decision depend only on prior prefix-optimal results rather than arbitrary subsets.

## 🪜 How to Solve This
1. Read the constraint carefully → choosing booth `i` blocks only `i - 1`, not the entire remaining array. That usually means the problem has a small local dependency and can be solved from left to right.

2. Ask what the optimal answer for a prefix looks like → for booths `0..i`, the best result must be one of two things:
   - skip booth `i`, so the answer is whatever was best for `0..i-1`
   - take booth `i`, so we add `booths[i]` to the best answer for `0..i-2`

3. That immediately gives a recurrence:
   - `best[i] = max(best[i-1], best[i-2] + booths[i])`

4. Notice we never need the full history, only the previous two prefix answers. That reduces the implementation from array DP to constant-space DP.

5. The mental model is simple: at each booth, compare “carry forward the previous optimum” versus “start from the last compatible optimum and include this booth.” Once that framing is clear, the solution is a linear scan with deterministic behavior.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: one-dimensional dynamic programming.**  
   This is the canonical “take or skip” DP over an array. The adjacency rule creates a dependency on only the previous two states, which is exactly why dynamic programming is the right abstraction.

2. **Define the state.**  
   Let `dp[i]` be the maximum donation sum obtainable from booths `0..i`. This state is correct because any valid solution over that prefix either includes booth `i` or excludes it.

3. **Derive the transition.**  
   If we skip booth `i`, the best sum remains `dp[i-1]`. If we take booth `i`, we cannot take `i-1`, so the best compatible total is `dp[i-2] + booths[i]`. Therefore:  
   `dp[i] = max(dp[i-1], dp[i-2] + booths[i])`  
   This preserves the invariant that `dp[i]` is always the optimal answer for prefix `0..i`.

4. **Handle base cases.**  
   For one booth, the answer is `booths[0]`. For two booths, the answer is `max(booths[0], booths[1])`. These anchor the recurrence and prevent invalid indexing.

5. **Optimize space.**  
   Because each state depends only on `i-1` and `i-2`, keep two rolling values instead of a full array. Maintain:
   - `prev2` = best answer up to `i-2`
   - `prev1` = best answer up to `i-1`  
   For each booth, compute `current = max(prev1, prev2 + booths[i])`, then shift the window.

6. **Why this is correct.**  
   At every step, the algorithm compares the only two structurally possible optimal forms for the current prefix. No valid solution can avoid that partition, so the recurrence is exhaustive and mutually exclusive.

## 📊 Worked Example
Example: `booths = [5, 1, 2, 10, 6]`

Using rolling DP state:

| i | booths[i] | take = prev2 + booths[i] | skip = prev1 | current |
|---|-----------|---------------------------|--------------|---------|
| 0 | 5         | 5                         | 0            | 5       |
| 1 | 1         | 1                         | 5            | 5       |
| 2 | 2         | 7                         | 5            | 7       |
| 3 | 10        | 15                        | 7            | 15      |
| 4 | 6         | 13                        | 15           | 15      |

Trace:

1. Start with booth `0`: best is `5`.
2. At booth `1`, skipping is better than taking `1`, so best stays `5`.
3. At booth `2`, taking it with booth `0` gives `7`.
4. At booth `3`, taking `10` plus the best through `1` gives `15`.
5. At booth `4`, taking `6` yields `13`, so keep `15`.

Final answer: `15`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time, where `n` is `booths.length`, because it processes each booth exactly once and performs constant work per element. At `10^6` elements this is still practical in most runtimes; at `10^9`, linear time becomes throughput-bound and requires streaming or distributed execution considerations.

### Space Complexity
The optimized implementation uses `O(1)` extra space. The only additional storage is a few scalar variables holding the previous two DP states. A full `O(n)` DP array is unnecessary unless you need to reconstruct the actual set of chosen booths.

## 💡 Key Takeaways
- If each array position presents a binary choice — take or skip — and taking one invalidates its neighbor, think one-dimensional dynamic programming immediately.
- When the optimal answer for a prefix depends only on the previous one or two prefixes, that is a strong signal for rolling-state DP instead of brute force or greedy logic.
- The most common bug is mis-handling base cases for arrays of length `1` or `2`, which can produce invalid `i-2` access or wrong initialization.
- Another frequent trap is updating rolling variables in the wrong order; compute `current` before shifting `prev2` and `prev1`.
- The production-grade lesson is broader: when local exclusion constraints are fixed-width, global optimization often collapses into a compact state machine with predictable latency.

## 🚀 Variations & Further Practice
- **Circular booths / House Robber II**: first and last booths are also adjacent, so the problem splits into two linear runs and requires careful boundary reasoning.
- **Booth selection with reconstruction**: return the actual booth indices, not just the sum; this adds path recovery and usually requires storing decisions or backtracking metadata.
- **Delete and Earn**: values, not positions, create conflicts; you first aggregate equal values, then solve a transformed adjacent-exclusion DP over the value domain.