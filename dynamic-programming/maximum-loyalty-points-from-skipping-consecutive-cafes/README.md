# Maximum Loyalty Points from Skipping Consecutive Cafes

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `points`, choose a subset of cafes that maximizes total loyalty points, with one restriction: no two selected cafes can be adjacent. The function returns the largest achievable sum under that rule. The problem is non-trivial because each local choice affects the next one — taking cafe `i` blocks cafe `i + 1`, so a greedy pick based only on the current value can easily miss the global optimum.

## 🌍 Engineering Impact
This pattern shows up anywhere adjacent selections conflict: ad-slot allocation with spacing constraints, maintenance scheduling on neighboring infrastructure, stream-processing jobs that cannot run back-to-back on the same constrained resource, and ranking systems that enforce diversity between nearby results. At scale, brute-force enumeration collapses immediately because the valid combinations grow exponentially. The dynamic programming framing converts a combinatorial search into a linear pass with stable memory usage. That matters operationally: predictable latency, simpler correctness reasoning, and a reusable recurrence that can be embedded in planners, schedulers, and optimization services without introducing search-heavy failure modes.

## 🔍 Problem Statement
You are given an integer array `points` where `points[i]` is the loyalty points available at the `i`-th cafe. Compute the maximum total points obtainable by selecting cafes such that no two selected cafes are adjacent.

Constraints:

- `1 <= points.length <= 100`
- `0 <= points[i] <= 1000`

Examples:

- `points = [5, 1, 2, 10]` → `15`  
  Best choice: cafes `0` and `3`, total `5 + 10 = 15`.

- `points = [2, 7, 9, 3, 1]` → `12`  
  Best choice: cafes `0`, `2`, and `4`, total `2 + 9 + 1 = 12`.

Edge cases matter even in this small input range: a single cafe, all zeros, or arrays where skipping a large-looking local option enables a better total later. The key constraint is adjacency exclusion, which makes each position depend on the best result from earlier positions rather than on simple accumulation.

## 🪜 How to Solve This
1. Read the rule carefully → selecting cafe `i` forbids selecting `i - 1` and `i + 1`. That means each decision has a dependency boundary of one position.

2. Ask what the best answer “up to index `i`” looks like → for every cafe, there are only two meaningful choices:
   - skip it, so the best total stays whatever it was at `i - 1`
   - take it, so the total becomes `points[i] + best up to i - 2`

3. That immediately suggests dynamic programming → the problem has optimal substructure because the best answer at `i` is built from best answers to smaller prefixes.

4. Define a recurrence:  
   `dp[i] = max(dp[i - 1], dp[i - 2] + points[i])`

5. Handle the base cases first → with one cafe, answer is `points[0]`; with two cafes, answer is `max(points[0], points[1])`.

6. Notice the recurrence only needs the previous two states → you can keep the full DP array for clarity or compress to constant space for production-quality efficiency.

This is the standard “take-or-skip” DP pattern on a linear array with exclusion constraints.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Dynamic Programming on a linear sequence.**  
   This is the right abstraction because each index has a binary decision with overlapping subproblems. The optimal answer for a prefix depends only on smaller prefixes, not on the full decision history.

2. **Define the state.**  
   Let `dp[i]` be the maximum points collectible from cafes `0..i`. This state is sufficient because once we know the best total for a prefix, we do not need to remember which exact cafes produced it.

3. **Write the transition.**  
   For cafe `i`, either:
   - skip it → total remains `dp[i - 1]`
   - take it → total becomes `dp[i - 2] + points[i]`  
   Therefore: `dp[i] = max(dp[i - 1], dp[i - 2] + points[i])`

4. **Initialize base cases correctly.**  
   `dp[0] = points[0]`  
   `dp[1] = max(points[0], points[1])`  
   These establish the invariant that `dp[i]` always stores the optimal non-adjacent sum for the prefix ending at `i`.

5. **Iterate left to right.**  
   For each `i >= 2`, compute `dp[i]` from already-finalized prior states. The invariant is preserved because both candidate values are themselves optimal for their respective prefixes.

6. **Return the final state.**  
   `dp[n - 1]` is the optimal answer for the whole array.

7. **Optional space optimization.**  
   Since each step uses only `dp[i - 1]` and `dp[i - 2]`, replace the array with two rolling variables. Correctness is unchanged because no older state is referenced after the transition.

## 📊 Worked Example
Example: `points = [2, 7, 9, 3, 1]`

| i | points[i] | skip = dp[i-1] | take = dp[i-2] + points[i] | dp[i] |
|---|-----------|----------------|-----------------------------|-------|
| 0 | 2         | —              | —                           | 2     |
| 1 | 7         | 2              | 7                           | 7     |
| 2 | 9         | 7              | 2 + 9 = 11                  | 11    |
| 3 | 3         | 11             | 7 + 3 = 10                  | 11    |
| 4 | 1         | 11             | 11 + 1 = 12                 | 12    |

Trace:

1. Start with `dp[0] = 2`
2. For index `1`, choose max of first two cafes → `7`
3. At index `2`, taking `9` plus `dp[0]` beats skipping → `11`
4. At index `3`, skipping preserves the better total → `11`
5. At index `4`, taking `1` plus `dp[2]` gives `12`

Final answer: `12`

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = points.length`. Each cafe is processed once, and each step performs constant-time work: one addition and one max comparison. At `10^6` elements this remains practical as a single linear scan; at `10^9`, asymptotically it is still optimal, but runtime becomes constrained by memory bandwidth and execution environment rather than algorithmic shape.

### Space Complexity
`O(n)` with a DP array, owned entirely by the prefix-results table. This can be reduced to `O(1)` by storing only the previous two DP states, trading away full intermediate history but preserving the final answer and correctness.

## 💡 Key Takeaways
- If a problem says “maximize value from a line of items” and “cannot take adjacent items,” it is almost always a take-or-skip dynamic programming recurrence.
- If each decision only blocks a fixed local neighborhood, look for a prefix-optimal state like `best up to i` rather than exploring combinations explicitly.
- The most common bug is misinitializing the first two states, especially when `n == 1`.
- Another frequent trap is using `dp[i - 1] + points[i]`, which illegally allows adjacent selections; the take case must come from `i - 2`.
- In production optimization code, local exclusion constraints often collapse exponential search into a linear recurrence if the state boundary is chosen correctly.

## 🚀 Variations & Further Practice
- **Circular cafes / House Robber II**: first and last cafes are also adjacent, so the linear recurrence must be run twice over different ranges.
- **Delete and Earn**: selecting a value removes neighboring values numerically, not positionally; the twist is transforming frequency aggregation into the same non-adjacent DP.
- **Weighted interval scheduling**: conflicts are no longer only adjacent positions; the harder part is finding the last compatible choice efficiently before applying DP.