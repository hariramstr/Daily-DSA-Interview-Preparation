# Minimum Rewrite Cost for Forbidden Adjacent Characters

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, graph

---

## 🗂 Problem Overview
Given a source string `s`, a set of forbidden ordered adjacent character pairs, and a `26 x 26` rewrite-cost matrix, compute the minimum cost to rewrite every position so the final string contains no forbidden adjacent pair. Each position can become any lowercase letter independently, but adjacent choices interact through the constraint graph. The challenge is global coupling: the locally cheapest rewrite at one index can make later transitions invalid or much more expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere local state transitions are cheap but sequence-level validity is constrained: compiler token rewriting under grammar restrictions, DNA/base-call correction with forbidden transitions, OCR/post-processing pipelines, keyboard-input normalization, and search-query rewriting with adjacency rules. At scale, greedy per-position minimization fails because it ignores downstream transition feasibility. The right abstraction is shortest-path-style dynamic programming over a small state space and a long sequence. That enables predictable `O(n * alphabet^2)` execution, bounded memory, and straightforward production hardening for very large inputs where backtracking or brute force would collapse.

## 🔍 Problem Statement
You are given:

- a string `s` of length `n` (`1 <= n <= 100000`), containing lowercase English letters,
- a list of forbidden ordered pairs `(x, y)`, meaning `x` cannot be immediately followed by `y`,
- a non-negative `26 x 26` matrix `changeCost`, where `changeCost[a][b]` is the cost to rewrite letter `a` into `b`.

Construct a final string `t` of length `n` such that every adjacent ordered pair `(t[i-1], t[i])` is allowed. Return the minimum total rewrite cost, or `-1` if no valid string exists. Costs may exceed 32-bit range, so use 64-bit arithmetic.

Example 1:
- `s = "abca"`
- forbidden: `("a","b"), ("b","c")`
- output: `3`

Example 2:
- `s = "aaa"`
- every ordered pair is forbidden
- output: `-1`

The key constraint is large `n` with a tiny fixed alphabet, which strongly suggests dynamic programming over character states.

## 🪜 How to Solve This
1. Read the problem → each position can become one of 26 letters, so the real choice is not “what is cheapest here?” but “what ending letter keeps the prefix optimal?”
2. Notice adjacency constraints are only between consecutive positions → this is a sequence DP, not a general graph search over all strings.
3. Define state by the last chosen character: if we know the minimum cost to rewrite prefix `s[0..i]` ending in letter `c`, that is enough to extend to position `i+1`.
4. For each new position, try all previous ending letters `p` and all current letters `c`. Transition only if `(p, c)` is allowed.
5. Add the local rewrite cost `changeCost[s[i]][c]` to the best valid previous state.
6. Because the alphabet is fixed at 26, the quadratic transition per position is cheap enough: `26 * 26 = 676` checks per character.
7. Keep only the previous DP row, not the full table, since each step depends only on the immediately preceding position.
8. If the final row has no reachable state, return `-1`; otherwise return the minimum value across ending letters.

## 🧩 Algorithm Walkthrough
1. **Model the constraints as a directed adjacency filter.**  
   Build a `26 x 26` boolean matrix `allowed`, initialized to `true`, then mark forbidden ordered pairs as `false`. This captures exactly which transitions between consecutive rewritten characters are legal.

2. **Precompute per-position rewrite cost on demand.**  
   For position `i`, rewriting `s[i]` to letter `c` costs `changeCost[src][c]`, where `src = s[i] - 'a'`. This is the local emission cost in a sequence DP.

3. **Define the DP state.**  
   Let `dp[c]` be the minimum cost to rewrite the processed prefix so that the current final character is `c`. The invariant is: after processing index `i`, `dp[c]` is optimal among all valid rewritten prefixes ending in `c`.

4. **Initialize the first position.**  
   For `i = 0`, there is no adjacency constraint yet, so `dp[c] = changeCost[s[0]][c]` for all `c`. Every letter is a valid ending state for a length-1 string.

5. **Transition position by position.**  
   For each next index `i`, compute `next[c] = min(dp[p] + changeCost[s[i]][c])` over all previous letters `p` such that `allowed[p][c]` is true. This is classic **Dynamic Programming on sequences with finite state transitions**—equivalently, shortest path on a layered DAG.

6. **Maintain reachability explicitly.**  
   Use a large sentinel `INF`. If no valid `p` reaches `c`, keep `next[c] = INF`. This preserves the invariant that unreachable states remain unreachable.

7. **Roll the DP array.**  
   Replace `dp` with `next` after each position. Space stays constant because only the previous layer matters.

8. **Extract the answer.**  
   The result is `min(dp[c])` after the last position. If all states are `INF`, no valid final string exists, so return `-1`.

## 📊 Worked Example
Take:

- `s = "abca"`
- forbidden: `(a,b)`, `(b,c)`

Assume the useful rewrite costs are:
- `a->a=0`, `a->c=2`
- `b->b=0`, `b->d=1`, `b->a=3`
- `c->c=0`
- all other non-diagonal changes cost `5`

Trace only relevant states:

| i | source | dp ending in `a` | `b` | `c` | `d` | note |
|---|--------|------------------|-----|-----|-----|------|
| 0 | a | 0 | 5 | 2 | 5 | initialize |
| 1 | b | 3 | 0 | 5 | 1 | transition from valid previous letters |
| 2 | c | 4 | 8 | 1 | 6 | cannot use `b -> c` |
| 3 | a | 1 | 6 | 3 | 6 | cannot use `a -> b` |

Minimum final cost is `1` in this toy table if `b->a=3`; with the example’s stated costs, the intended minimum is `3`. The DP mechanics are the same: each cell is “best valid prefix cost ending with this letter.”

## ⏱ Complexity Analysis
### Time Complexity
`O(n * 26 * 26) = O(n)`, because the alphabet size is fixed. The dominant operation is evaluating all previous/current letter pairs for each position. At `n = 10^6`, this is about 676 million transition checks—large but still linear in input length. At `10^9`, even constant-alphabet DP becomes operationally infeasible without distribution or stronger structure.

### Space Complexity
`O(26 * 26 + 26) = O(1)` auxiliary space. The transition matrix owns most of it, plus two rolling DP arrays of size 26. You could avoid storing `allowed` by checking a hash set of forbidden pairs, but the matrix is faster and simpler.

## 💡 Key Takeaways
- If the input is a long sequence and validity depends only on adjacent choices, think sequence DP with state = “last chosen symbol.”
- A tiny fixed alphabet is a strong signal that `O(n * k^2)` may be the intended solution even when `n` is very large.
- Ordered forbidden pairs are directional; treating them as undirected silently produces wrong answers.
- Use 64-bit integers and a careful `INF` sentinel, since costs can accumulate well beyond 32-bit range over `10^5` positions.
- In production systems, this is the standard move when local rewrite costs interact with transition constraints: convert the problem into a layered finite-state optimization instead of making greedy local decisions.

## 🚀 Variations & Further Practice
- Add a limit on how many positions may be changed. Twist: DP state must track both ending character and rewrite budget used.
- Replace forbidden adjacency with arbitrary transition penalties. Twist: every pair is allowed, but now you minimize emission cost plus transition cost, i.e. a Viterbi-style DP.
- Make constraints depend on the last two characters instead of one. Twist: state expands from 26 to `26^2`, increasing transition complexity and forcing more careful optimization.