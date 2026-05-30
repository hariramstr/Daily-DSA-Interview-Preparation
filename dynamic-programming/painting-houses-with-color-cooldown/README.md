# Painting Houses with Color Cooldown

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Array, State Machine

---

## 🗂 Problem Overview

Given `n` houses and `k` colors, assign each house a color minimizing total painting cost, where `cost[i][j]` is the cost of painting house `i` with color `j`. The non-trivial constraint: once a color is used, it cannot be reused for the next `cooldown - 1` houses. This cooldown window transforms a simple greedy selection into a state-dependent decision where each choice forecloses future options across a sliding horizon. Return `-1` if no valid assignment exists.

---

## 🌍 Engineering Impact

This pattern is the core of **rate-limiting with per-resource cooldowns** — think API throttling where endpoint categories can't be hit within a reuse window, or GPU kernel scheduling where memory-bank conflicts enforce re-issue delays. In **task scheduling systems** (Kubernetes job queues, LeetCode's own task scheduler), the same DP structure governs slot assignment under cooldown. Without it, greedy approaches produce locally optimal but globally infeasible schedules. The DP formulation scales gracefully and makes constraint violations structurally impossible rather than reactively detected.

---

## 🔍 Problem Statement

**Input:** A 2D array `cost[n][k]` and an integer `cooldown`.
**Output:** Minimum total cost to paint all `n` houses, or `-1` if impossible.

**Constraint driving the algorithm:** Color `j` used at house `i` cannot be reused at houses `i+1` through `i + cooldown - 1`. This creates a dependency window of up to `cooldown` prior decisions, ruling out greedy and demanding full state tracking.

**Examples:**

| Input | cooldown | Output |
|---|---|---|
| `[[1,5,3],[2,9,4],[8,1,6]]` | 2 | `6` |
| `[[1,2],[1,2]]` | 2 | `3` |

**Edge cases:** `cooldown >= k` with only 2 colors makes many configurations infeasible. `cooldown = 1` degenerates to unconstrained minimum-cost selection per house.

**Hard constraints:** `1 ≤ n ≤ 100`, `1 ≤ k ≤ 20`, `1 ≤ cooldown ≤ n`.

---

## 🪜 How to Solve This

1. **Observe the dependency structure** → the cost of painting house `i` with color `j` depends on which colors were used in the previous `cooldown - 1` houses. This is not a local constraint — it's a window.

2. **Window dependency → DP state** → if choices only depended on the immediately prior house, a simple 2D DP suffices. A cooldown window means the state must encode *which color was last used and when*, or equivalently, which colors are currently "blocked."

3. **State definition** → `dp[i][j]` = minimum cost to paint houses `0..i` where house `i` is painted color `j`. Transition: `dp[i][j] = cost[i][j] + min(dp[i-1][c])` for all `c` where `|c - j|` isn't the issue — rather, color `j` must not have been used at any of houses `i - cooldown + 1` through `i - 1`.

4. **Feasibility check** → if `k < cooldown` is not necessarily infeasible (colors repeat after the window), but if at any house all colors are blocked, return `-1`. Track this explicitly during transitions.

5. **Final answer** → `min(dp[n-1][j])` over all `j`.

---

## 🧩 Algorithm Walkthrough

**Pattern: Interval-Constrained DP (State Machine over a sliding forbidden set)**

1. **Initialize** `dp[0][j] = cost[0][j]` for all colors `j`. The first house has no prior constraints.

2. **For each house `i` from `1` to `n-1`:** iterate over each candidate color `j`. Color `j` is **forbidden** if it was used at any house in the range `[i - cooldown + 1, i - 1]`. Equivalently, `j` is forbidden if there exists a prior house `p` in that range where the DP chose color `j` — but since we track all color assignments in `dp`, we check: color `j` is forbidden at house `i` if `i - p < cooldown` for the most recent `p` where `j` was used.

3. **Transition:** `dp[i][j] = cost[i][j] + min(dp[i-1][c])` for all `c ≠ j` *and* where `j` was not used at house `i-1` through `i-cooldown+1`. Concretely: scan `dp[i-1][c]` for all `c` not in the forbidden set for position `i`.

4. **Forbidden set construction:** for house `i`, color `j` is forbidden if it appears as the chosen color at any of the `cooldown - 1` preceding houses. Since we carry full `dp` rows (not just the argmin), we mark `j` as forbidden by checking whether `dp[i - t][j]` was reachable for `t` in `1..cooldown-1`. A cleaner implementation: for each `(i, j)`, iterate over all `c ≠ j` for the immediate prior row, but also propagate the forbidden window by carrying a `last_used` matrix or by iterating back `cooldown` steps.

5. **Infeasibility:** if `dp[i][j] = ∞` for all `j` at any row `i`, return `-1`.

6. **Result:** `min(dp[n-1])`, filtering out `∞`.

---

## 📊 Worked Example

**Input:** `cost = [[1,5,3],[2,9,4],[8,1,6]]`, `cooldown = 2`

**Initialization (house 0):**

| Color | 0 | 1 | 2 |
|---|---|---|---|
| `dp[0]` | 1 | 5 | 3 |

**House 1** — cooldown=2 means color used at house 0 is forbidden:

| Color j | Forbidden? | Best prior `dp[0][c]` (c≠j, c not forbidden for j) | `dp[1][j]` |
|---|---|---|---|
| 0 | Yes (used at house 0) | — | ∞ |
| 1 | No | min(dp[0][0], dp[0][2]) = min(1,3) = 1 | 1+9 = **10** |
| 2 | No | min(dp[0][0], dp[0][1]) = min(1,5) = 1 | 1+4 = **5** |

**House 2** — color used at house 1 is forbidden:

| Color j | Forbidden at house 2? | Best prior `dp[1][c]` | `dp[2][j]` |
|---|---|---|---|
| 0 | No | min(dp[1][1], dp[1][2]) = min(10,5) = 5 | 5+8 = **13** |
| 1 | No | min(dp[1][2]) = 5 (dp[1][0]=∞) | 5+1 = **6** |
| 2 | No | min(dp[1][1]) = 10 | 10+6 = **16** |

**Answer:** `min(13, 6, 16) = 6` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n · k²)** — for each of the `n` houses and `k` candidate colors, we scan up to `k` prior colors to find the valid minimum. With `n=100` and `k=20`, this is 40,000 operations — trivial. At `n=10⁶`, `k=20`, it reaches 400M operations and warrants optimizing the inner loop to O(1) using a precomputed min-excluding-j structure, reducing to **O(n · k)**.

### Space Complexity

**O(n · k)** for the full DP table, reducible to **O(cooldown · k)** by retaining only the last `cooldown` rows — the transition never looks further back. The trade-off is code complexity against memory; at `n=100, k=20` the savings are negligible, but the rolling-window pattern is worth knowing for larger inputs.

---

## 💡 Key Takeaways

- **Pattern signal — window-based dependency:** when a constraint says "the last `W` decisions restrict the current one," that's a DP with a sliding forbidden set, not a greedy or simple recurrence. The window width `W` directly shapes your state.
- **Pattern signal — "return -1 if impossible":** infeasibility detection in DP means tracking `∞` (or sentinel) states and propagating them correctly — a signal that the state space has dead ends, not just suboptimal paths.
- **Gotcha — forbidden set scope:** the cooldown applies to the *window* `[i - cooldown + 1, i - 1]`, not just the immediately prior house. Off-by-one on the window boundary (`cooldown - 1` vs `cooldown` houses back) produces wrong answers that pass most tests but fail edge cases where `cooldown = n`.
- **Gotcha — `cooldown = 1` degeneration:** when `cooldown = 1`, no color is ever forbidden (the window is empty), so every house independently picks its cheapest color. Ensure your forbidden-set logic produces an empty set here, not an accidentally populated one.
- **Architectural insight:** the cooldown DP is structurally identical to a **finite state machine with timed re-entry** — a pattern that appears in circuit breakers, retry policies, and resource lease management. Encoding "blocked until time T" as DP state is more robust than imperative cooldown tracking because it makes all valid states explicit and infeasibility provable.

---

## 🚀 Variations & Further Practice

- **[LeetCode 621 — Task Scheduler](https://leetcode.com/problems/task-scheduler/):** The same cooldown constraint, but you minimize *total time slots* rather than cost, and idle slots are permitted. The twist: you must decide whether to insert idles or reorder tasks, shifting the problem from pure DP to a greedy frequency-based argument — the cost function changes the optimal strategy entirely.
- **Variable cooldown per color:** instead of a global `cooldown`, each color `j` has its own `cooldown[j]`. The forbidden-set construction becomes color-specific, and the state must track the last-used index per color — expanding state space to O(n · k) tracked positions and making the transition O(k²) per house with no easy optimization.
- **Circular house arrangement:** houses form a ring, so house `n-1` and house `0` are neighbors. This adds a dependency between the first and last decisions, requiring you to fix the color of house `0`, solve the linear subproblem for houses `1..n-1` under that constraint, and iterate over all `k` starting colors — a standard "run DP twice" technique that appears in circular scheduling and ring-buffer allocation problems.