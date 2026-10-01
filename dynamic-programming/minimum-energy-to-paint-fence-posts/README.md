# Minimum Energy to Paint Fence Posts

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given `n` fence posts and a `costs[n][3]` matrix, choose one of three colors for each post so that adjacent posts never share the same color, while minimizing total painting cost. The output is a single integer: the minimum achievable total cost. The problem is non-trivial because each local color choice constrains the next post, so a greedy “pick the cheapest color now” strategy can easily produce a suboptimal global result.

## 🌍 Engineering Impact
This pattern shows up anywhere a sequence of decisions has local incompatibility constraints and additive cost: job scheduling with anti-affinity rules, compiler register assignment under adjacency conflicts, CDN request routing with failover preferences, and streaming pipelines where consecutive stages cannot reuse the same resource class. At small scale, brute force or backtracking works; at production scale, it collapses exponentially. Dynamic programming turns a state-explosion problem into a linear pass by preserving only the minimal frontier of relevant prior decisions. That shift matters architecturally: predictable latency, bounded memory, and a formulation that composes cleanly into larger optimization pipelines.

## 🔍 Problem Statement
You are given a 2D array `costs` where `costs[i][0]`, `costs[i][1]`, and `costs[i][2]` represent the energy required to paint post `i` red, blue, or green. Every post must be painted exactly one color, and no two adjacent posts may have the same color.

Return the minimum total energy required to paint all posts under that constraint.

Constraints:

- `1 <= n <= 1000`
- `costs.length == n`
- `costs[i].length == 3`
- `1 <= costs[i][j] <= 10^4`

If there is only one post, the answer is simply the minimum of its three costs.

Examples:

- `costs = [[1,5,3],[2,9,4]]` → `5`
- `costs = [[7,6,2],[5,8,4],[3,9,1],[6,2,7]]` → `10`

The key constraint driving the algorithm is adjacency: the best choice for post `i` depends only on the best valid choices for post `i - 1`.

## 🪜 How to Solve This
1. Read the constraint carefully → each post only conflicts with its immediate neighbor, not the whole history.

2. That means the full painting sequence does **not** need to be remembered. For post `i`, what matters is only: “what was the cheapest total cost if the previous post ended in red, blue, or green?”

3. This is the signal for **dynamic programming on a linear sequence**:
   - define state by position and ending color
   - transition from the previous position using only allowed colors

4. For each post:
   - cost to end in red = current red cost + min(previous blue, previous green)
   - same logic for blue and green

5. Why this works → if two partial solutions end with the same color at the same index, only the cheaper one matters. The more expensive one can never lead to a better final answer.

6. Because each row depends only on the previous row, you do not need an `n x 3` table. Three running values are enough.

7. After processing all posts, take the minimum of the three final states.

This gives a linear-time solution with constant extra space.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Dynamic Programming on Arrays.**  
   The problem is a sequential optimization problem with local constraints. Each decision affects only the next position, which is exactly the shape where DP is the right abstraction.

2. **Define the state.**  
   Let `dpRed`, `dpBlue`, and `dpGreen` represent the minimum total cost to paint all posts up to the current index, ending with that color.  
   Invariant: after processing post `i`, each variable stores the optimal cost for all valid paintings of posts `0..i` with the specified ending color.

3. **Initialize from the first post.**  
   For post `0`, the optimal cost of ending in each color is just its direct painting cost:
   - `dpRed = costs[0][0]`
   - `dpBlue = costs[0][1]`
   - `dpGreen = costs[0][2]`  
   This is correct because there is no adjacency constraint before the first post.

4. **Process each subsequent post.**  
   For post `i`, compute:
   - `newRed = costs[i][0] + min(dpBlue, dpGreen)`
   - `newBlue = costs[i][1] + min(dpRed, dpGreen)`
   - `newGreen = costs[i][2] + min(dpRed, dpBlue)`  
   Why correct: if post `i` is red, post `i-1` must be blue or green; among those valid predecessors, only the cheaper total matters.

5. **Advance the frontier.**  
   Replace the old state with the new one.  
   Invariant remains preserved: each state still represents the minimum valid total ending in that color at the current index.

6. **Return the best terminal state.**  
   The final answer is `min(dpRed, dpBlue, dpGreen)` because the last post may be any color.

This is optimal because every valid painting is represented by exactly one state path, and every transition keeps only the cheapest equivalent prefix.

## 📊 Worked Example
Use `costs = [[7,6,2],[5,8,4],[3,9,1],[6,2,7]]`.

| Post `i` | Cost Row   | `dpRed` | `dpBlue` | `dpGreen` |
|----------|------------|---------|----------|-----------|
| 0        | `[7,6,2]`  | 7       | 6        | 2         |
| 1        | `[5,8,4]`  | 7       | 10       | 10        |
| 2        | `[3,9,1]`  | 13      | 16       | 8         |
| 3        | `[6,2,7]`  | 14      | 10       | 20        |

Trace:
1. Start with post 0 directly: `(7, 6, 2)`.
2. Post 1:
   - red = `5 + min(6,2) = 7`
   - blue = `8 + min(7,2) = 10`
   - green = `4 + min(7,6) = 10`
3. Post 2:
   - red = `3 + min(10,10) = 13`
   - blue = `9 + min(7,10) = 16`
   - green = `1 + min(7,10) = 8`
4. Post 3:
   - red = `6 + min(16,8) = 14`
   - blue = `2 + min(13,8) = 10`
   - green = `7 + min(13,16) = 20`

Final answer: `min(14, 10, 20) = 10`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because it processes each post exactly once and performs a constant amount of work per row: three transitions and a few `min` operations. At `10^6` rows this is still operationally cheap; at `10^9`, linear time becomes throughput-bound and likely requires partitioning or a different problem framing.

### Space Complexity
The space complexity is `O(1)` extra space when using three rolling state variables. The input matrix dominates total memory. A full `n x 3` DP table would also be valid, but it adds unnecessary storage without improving asymptotic runtime.

## 💡 Key Takeaways
- If a problem asks for a minimum cost over a sequence and each choice only constrains the next item, think dynamic programming with a small rolling state.
- When the state can be summarized as “best cost ending in category X,” you usually do not need to retain the full history.
- Do not update `dpRed`, `dpBlue`, and `dpGreen` in place without temporary variables; later transitions would accidentally read already-overwritten state.
- The base case is the first row itself, not zeros plus transitions; getting that wrong shifts every result.
- At scale, the transferable design insight is state compression: preserve only the minimal information needed for future decisions, and discard dominated history aggressively.

## 🚀 Variations & Further Practice
- **Paint House II / k colors** — same DP pattern, but the color count is variable; the harder twist is avoiding `O(n * k^2)` transitions by tracking the smallest and second-smallest previous costs.
- **Circular fence or houses** — first and last posts are also adjacent; the twist is that the initial choice constrains the terminal state, often requiring multiple DP runs.
- **Forbidden color transitions matrix** — instead of “same color disallowed,” arbitrary transitions may be invalid; the harder part is generalizing the recurrence while keeping the state compact.