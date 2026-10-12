# Maximum Stability Score from Choosing Daily Work Modes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Array, State Transition

---

## 🗂 Problem Overview
Given an `n x 3` array `score`, choose exactly one work mode per day and maximize total stability points. The catch is local but binding: the same mode cannot be selected on consecutive days. Return the maximum achievable sum across all days. The problem is non-trivial because naive enumeration explores `3^n` schedules, while the optimal choice for day `i` depends only on which mode was used on day `i - 1`.

## 🌍 Engineering Impact
This pattern shows up anywhere a system makes repeated local choices under adjacency or cooldown constraints: autoscaling policies that should not oscillate between states, streaming pipelines choosing execution modes without repeating expensive transitions, ad-serving or ranking systems enforcing diversity across adjacent slots, and workforce schedulers avoiding repeated assignments. At scale, brute-force search is dead on arrival; the state space explodes while the real dependency remains tiny. Dynamic programming turns an exponential planning problem into a linear pass, enabling predictable latency, bounded memory, and straightforward reasoning about correctness under production-sized inputs.

## 🔍 Problem Statement
You are given `score`, a 2D integer array of size `n x 3`, where `score[i][0]`, `score[i][1]`, and `score[i][2]` are the points earned on day `i` for choosing Focus, Meeting, and Recovery. For each day, exactly one mode must be chosen. The same mode cannot be chosen on two adjacent days.

Return the maximum total score over all `n` days.

Constraints:
- `1 <= n <= 100000`
- `score.length == n`
- `score[i].length == 3`
- `0 <= score[i][m] <= 10000`

Examples:
- `score = [[8,5,6],[7,9,4],[3,8,10],[9,2,7]]` → `36`
- `score = [[4,4,7],[6,1,5],[2,9,3]]` → `22`

The key constraint is `n` up to `100000`: exponential search is impossible, so the solution must exploit the fact that each day depends only on the previous day’s chosen mode.

## 🪜 How to Solve This
1. Read the constraint → `n` is large, so trying all schedules is immediately ruled out.
2. Notice the restriction is only about adjacent days → the legality of today’s choice depends only on yesterday’s mode, not the full history.
3. That dependency shape is a classic dynamic programming signal: define state by **day + last chosen mode**.
4. Ask the right subproblem: “What is the best total score up to day `i` if day `i` ends in Focus / Meeting / Recovery?”
5. For each mode today, the previous day can only be one of the other two modes → each state transition is just a max over two values.
6. Initialize day `0` directly from `score[0]` because there is no previous day.
7. Iterate forward, updating the best totals for the three possible ending modes.
8. The answer is the maximum of the three states on the last day.

Once framed this way, the problem collapses from exponential search to a linear scan with constant state.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Dynamic Programming with state transition.**  
   The decision on day `i` is constrained only by day `i - 1`. That makes this a small-state DP, not a graph search or combinatorial backtracking problem.

2. **Define the state.**  
   Let `dp[i][m]` be the maximum total score from day `0` through day `i` if mode `m` is chosen on day `i`.  
   This is correct because it captures exactly the information needed for future transitions: the current day and the last mode used.

3. **Initialize the base case.**  
   For day `0`, `dp[0][0] = score[0][0]`, `dp[0][1] = score[0][1]`, `dp[0][2] = score[0][2]`.  
   Invariant: after initialization, each state represents the best valid schedule ending in that mode on day `0`.

4. **Apply the transition.**  
   For each later day `i`:
   - `dp[i][0] = score[i][0] + max(dp[i-1][1], dp[i-1][2])`
   - `dp[i][1] = score[i][1] + max(dp[i-1][0], dp[i-1][2])`
   - `dp[i][2] = score[i][2] + max(dp[i-1][0], dp[i-1][1])`  
   Why correct: choosing mode `m` today forbids only mode `m` yesterday, so the best predecessor is the maximum of the other two states.

5. **Maintain only the previous row.**  
   Since `dp[i]` depends only on `dp[i-1]`, store three running values instead of the full table.  
   Invariant: before processing day `i`, the three variables hold the optimal totals for day `i - 1`.

6. **Return the best terminal state.**  
   The final schedule may end in any mode, so return `max(lastFocus, lastMeeting, lastRecovery)`.

## 📊 Worked Example
Using `score = [[8,5,6],[7,9,4],[3,8,10],[9,2,7]]`:

| Day | Focus | Meeting | Recovery |
|-----|------:|--------:|---------:|
| 0 | 8 | 5 | 6 |
| 1 | 7 + max(5,6) = 13 | 9 + max(8,6) = 17 | 4 + max(8,5) = 12 |
| 2 | 3 + max(17,12) = 20 | 8 + max(13,12) = 21 | 10 + max(13,17) = 27 |
| 3 | 9 + max(21,27) = 36 | 2 + max(20,27) = 29 | 7 + max(20,21) = 28 |

Trace:
1. Day 0 is just the raw score values.
2. Day 1 ending in Meeting must come from prior Focus or Recovery, so it becomes `9 + max(8,6) = 17`.
3. Day 2 ending in Recovery becomes `10 + max(13,17) = 27`.
4. Day 3 ending in Focus becomes `9 + max(21,27) = 36`.

Final answer: `max(36, 29, 28) = 36`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each day performs a constant amount of work: three state updates, each taking a max over two values. This scales comfortably to `10^6` elements in a single pass. At `10^9`, linear time is still too large in practice, but this is asymptotically optimal because every input row must be read.

### Space Complexity
`O(1)` auxiliary space if you keep only the previous day’s three DP values. A full `O(n)` table is unnecessary unless you also need to reconstruct the exact sequence of chosen modes rather than only the maximum score.

## 💡 Key Takeaways
- If a choice sequence has a “cannot repeat adjacent state” rule, model the DP state as “best result ending in state `m` at position `i`.”
- When the transition depends only on the immediately previous step, that is a strong signal to replace brute-force recursion with rolling-state dynamic programming.
- The base case is easy to get wrong: day `0` has no predecessor, so initialize directly from `score[0]` rather than applying the recurrence.
- Do not update the three rolling states in place without temporary variables; later transitions in the same iteration must read the previous day, not partially updated current values.
- In production systems, many “global optimization” problems become tractable once you identify that the true dependency horizon is local and the state can be compressed.

## 🚀 Variations & Further Practice
- Generalize from 3 modes to `k` modes: same DP idea, but transitions become `O(k^2)` unless you optimize with prefix/suffix maxima or track the top two previous states.
- Add a cooldown of `c` days before a mode can repeat: the state must now encode a larger history window or remaining cooldown counters, increasing state dimensionality.
- Require reconstruction of the actual optimal schedule, not just the score: store parent pointers or a full DP table, trading constant memory for recoverability.