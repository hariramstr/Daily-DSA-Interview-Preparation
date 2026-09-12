# Maximum Points from Skipping Adjacent Museum Rooms

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an integer array `rooms`, choose a subset of room scores such that no two chosen indices are adjacent, and return the maximum total score. You may skip any room, including all rooms, so the result must never be negative. The challenge is that each decision depends on the previous one: taking room `i` forbids room `i - 1`, which rules out greedy local choices and pushes the problem toward dynamic programming.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize value under local exclusion constraints. Examples include ad-slot selection where adjacent placements cannibalize engagement, streaming schedulers that avoid colocating noisy jobs, search ranking with diversity penalties between neighboring results, and maintenance planners that cannot take adjacent nodes offline. At scale, naive backtracking explodes exponentially and greedy heuristics silently lose value. The dynamic programming formulation gives deterministic optimality, linear throughput, and constant-memory execution, which matters when this logic sits inside hot paths, batch scoring pipelines, or policy engines processing millions of candidates.

## 🔍 Problem Statement
You are given an integer array `rooms` where `rooms[i]` is the visitor-point value of including room `i` in a museum tour. Select a subset of rooms such that no two selected rooms are adjacent, and return the maximum total points.

You may skip any room, and if all values are non-positive, choosing no rooms is valid. Therefore, the answer is always at least `0`.

**Constraints**
- `1 <= rooms.length <= 100000`
- `-1000 <= rooms[i] <= 1000`

**Examples**
- `rooms = [4, 2, 7, 9, 3]` → `13`
  - Best valid choice: `4 + 9 = 13`
- `rooms = [5, 1, 1, 5]` → `10`
  - Choose the first and last rooms

The key constraint is adjacency: each index creates a dependency on the previous one, which makes this a linear dynamic programming problem rather than a simple summation or greedy scan.

## 🪜 How to Solve This
1. Read the constraint carefully → the only thing that blocks choosing room `i` is whether room `i - 1` was chosen.
2. That means the full history does **not** matter → only the best answer up to the previous room and the one before that.
3. For each room, there are only two meaningful choices:
   - **Skip it** → keep the best total seen so far.
   - **Take it** → add its score to the best total from two positions back.
4. So the recurrence becomes intuitive:  
   `best[i] = max(best[i - 1], best[i - 2] + rooms[i])`
5. Because skipping all rooms is allowed, initialize the running best so it never drops below `0`.
6. Notice we do not need the whole DP array to compute the next state → only the last two values matter.
7. That gives a linear scan, constant extra space, and an implementation that is both optimal and operationally simple.

This is the standard “include current vs exclude current” dynamic programming pattern.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Dynamic Programming on a linear array.**  
   This is the classic “non-adjacent maximum sum” problem. The right abstraction is DP because each position has overlapping subproblems and a binary decision: include or exclude.

2. **Define the state.**  
   Let `dp[i]` be the maximum score obtainable from rooms `0..i` with no adjacent selections. This state is sufficient because the optimal answer for prefix `0..i` depends only on smaller prefixes.

3. **Write the transition.**  
   For room `i`, either:
   - skip it, yielding `dp[i - 1]`
   - take it, yielding `dp[i - 2] + rooms[i]`  
   So: `dp[i] = max(dp[i - 1], dp[i - 2] + rooms[i])`

4. **Handle the non-negative-answer rule.**  
   Since choosing no rooms is valid, base values should never force a negative result. Conceptually:
   - `dp[-1] = 0`
   - `dp[0] = max(0, rooms[0])`

5. **Compress the state.**  
   The recurrence only reads `dp[i - 1]` and `dp[i - 2]`, so store two variables:
   - `prev1` = best up to previous room
   - `prev2` = best up to the room before that

6. **Maintain the invariant during iteration.**  
   After processing index `i`, `prev1` equals the optimal answer for prefix `0..i`, and `prev2` equals the optimal answer for prefix `0..i-1`.

7. **Return the final best.**  
   After one pass, `prev1` is the maximum achievable score for the full array. This is correct by induction over the prefix length.

## 📊 Worked Example
Example: `rooms = [4, 2, 7, 9, 3]`

| i | rooms[i] | take = prev2 + rooms[i] | skip = prev1 | current | new prev2 | new prev1 |
|---|----------|--------------------------|--------------|---------|-----------|-----------|
| 0 | 4        | 0 + 4 = 4                | 0            | 4       | 0         | 4         |
| 1 | 2        | 0 + 2 = 2                | 4            | 4       | 4         | 4         |
| 2 | 7        | 4 + 7 = 11               | 4            | 11      | 4         | 11        |
| 3 | 9        | 4 + 9 = 13               | 11           | 13      | 11        | 13        |
| 4 | 3        | 11 + 3 = 14              | 13           | 14      | 13        | 14        |

This trace shows the core invariant: at each step, the algorithm compares “take current, so use the best from two back” against “skip current, so keep the best so far.” For this input, the optimal total is `14`, achieved by choosing rooms with scores `4`, `7`, and `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = rooms.length`, because each room is processed exactly once and each iteration does constant work. At `10^6` elements this is still practical in a single pass. At `10^9`, the algorithm remains asymptotically optimal, but runtime becomes dominated by raw scan cost and memory bandwidth.

### Space Complexity
`O(1)` extra space when using two rolling variables. No auxiliary array is required unless you want full DP state for debugging or reconstruction of chosen indices, which increases space to `O(n)`.

## 💡 Key Takeaways
- If the problem says “maximize value” with a rule like “cannot pick adjacent items,” that is a strong signal for linear dynamic programming.
- When each choice depends only on a small local exclusion window, look for an “include vs exclude” recurrence before considering greedy strategies.
- The answer must never be negative here; initialize base cases so “choose nothing” remains a valid outcome.
- Be careful with examples and traces: the recurrence may produce a better result than an informal explanation if the explanation overlooks a valid non-adjacent combination.
- The production-grade insight is state compression: many DP problems need only the last few states, which turns an optimal algorithm into one that is also cache-friendly and operationally cheap.

## 🚀 Variations & Further Practice
- **House Robber II**: same recurrence, but the array is circular, so the first and last elements are also adjacent; you must solve two linear cases and combine them.
- **Delete and Earn**: values interact by numeric adjacency rather than index adjacency; the twist is preprocessing into aggregated buckets before applying the same DP pattern.
- **Maximum sum with no `k` consecutive picks**: the exclusion rule expands from adjacent items to a larger local window, increasing state dimensionality and transition complexity.