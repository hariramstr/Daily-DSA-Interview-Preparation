# Minimum Rest Days for a Practice Plan

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, state-transition

---

## 🗂 Problem Overview
Given an array `activities`, choose one action per day—coding, reading, or rest—while minimizing total rest days. Availability varies by day, and the same practice type cannot be repeated on consecutive days. The output is the minimum number of rests across the full schedule. The non-trivial part is that each local choice constrains the next day, so a greedy decision can easily block a better global plan.

## 🌍 Engineering Impact
This pattern shows up anywhere a scheduler must optimize throughput under short-term state constraints: job placement with cooldowns, API clients rotating request classes to avoid rate-limit buckets, streaming systems selecting operators under thermal or resource limits, and workforce scheduling with shift-type adjacency rules. At scale, naive greedy logic overfits to the current slot and creates avoidable idle capacity later. A compact state-transition model prevents that. It enables predictable optimization, bounded memory, and easy extension when the system evolves from binary availability to richer policy constraints.

## 🔍 Problem Statement
You are given an integer array `activities` of length `n` where `1 <= n <= 100` and each value is in `[0, 3]`.

Each day supports:
- `0`: neither coding nor reading; rest is mandatory
- `1`: only coding
- `2`: only reading
- `3`: both coding and reading

You must choose exactly one action per day: coding, reading, or rest. Coding is allowed only if available that day; reading is allowed only if available that day. The same practice type cannot be chosen on consecutive days. Rest is always valid.

Return the minimum number of rest days over all `n` days.

Examples:
- `activities = [1,3,2,0,3]` → `2`
- `activities = [3,3,3]` → `1`

The key constraint is dependency on the previous day’s action. That rules out simple counting or greedy selection and points directly to state-based dynamic programming.

## 🪜 How to Solve This
1. Read the constraint carefully → today’s valid choice depends only on **what happened yesterday**, not on the full history.
2. That immediately suggests a small state machine: after each day, the only thing worth remembering is whether we ended the day with:
   - rest,
   - coding,
   - reading.
3. Once the state is that small, dynamic programming becomes obvious. For each day, compute the minimum rest days needed to end in each of those three states.
4. Why not greedy? Because “take any available non-rest option” can be locally fine but globally suboptimal when a future day has only one activity available.
5. Transition logic is straightforward:
   - rest can always follow anything, with `+1` rest day;
   - coding can follow rest or reading, if coding is available;
   - reading can follow rest or coding, if reading is available.
6. Iterate day by day, updating these three values. The answer is the minimum of the three states after the last day.

This is classic dynamic programming over a tiny finite state space.

## 🧩 Algorithm Walkthrough
1. **Define the DP state.**  
   Use Dynamic Programming with **state transition over the previous action**. Let:
   - `dp[i][0]` = minimum rest days after day `i` if day `i` is a rest day
   - `dp[i][1]` = minimum rest days after day `i` if day `i` ends with coding
   - `dp[i][2]` = minimum rest days after day `i` if day `i` ends with reading  
   This is the right abstraction because future feasibility depends only on the last action.

2. **Initialize day 0.**  
   - Rest is always possible: `dp[0][0] = 1`
   - If coding is available, `dp[0][1] = 0`
   - If reading is available, `dp[0][2] = 0`
   - Otherwise set impossible states to a large sentinel value  
   Invariant: each state stores the best achievable rest count for that exact ending action.

3. **Transition for rest.**  
   `dp[i][0] = min(dp[i-1][0], dp[i-1][1], dp[i-1][2]) + 1`  
   Rest can always follow any prior state, and it increases the rest count by one.

4. **Transition for coding.**  
   If coding is available on day `i`, then  
   `dp[i][1] = min(dp[i-1][0], dp[i-1][2])`  
   Coding cannot follow coding, so exclude `dp[i-1][1]`.

5. **Transition for reading.**  
   If reading is available on day `i`, then  
   `dp[i][2] = min(dp[i-1][0], dp[i-1][1])`  
   Reading cannot follow reading, so exclude `dp[i-1][2]`.

6. **Return the best terminal state.**  
   The answer is `min(dp[n-1][0], dp[n-1][1], dp[n-1][2])`.  
   Correctness follows from exhaustive local transitions over the complete minimal state needed to preserve future validity.

## 📊 Worked Example
Example: `activities = [1,3,2,0,3]`

| Day | Activity | End Rest | End Coding | End Reading |
|---|---:|---:|---:|---:|
| 0 | 1 | 1 | 0 | ∞ |
| 1 | 3 | 1 | 1 | 0 |
| 2 | 2 | 1 | ∞ | 1 |
| 3 | 0 | 2 | ∞ | ∞ |
| 4 | 3 | 3 | 2 | 2 |

Trace:
1. Day 0: only coding exists, so best states are rest=`1`, coding=`0`.
2. Day 1: both exist; reading after coding gives `0`, coding after rest gives `1`.
3. Day 2: only reading exists; it can follow rest or coding, so reading=`1`.
4. Day 3: no activity exists; forced rest, so rest=`2`.
5. Day 4: both exist; either coding or reading after rest yields `2`.

Final answer: `min(3,2,2) = 2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is the number of days. Each day performs a constant number of state transitions across three states. At `10^6` elements this is still linear and operationally cheap; at `10^9`, asymptotics remain optimal but runtime becomes dominated by raw scan cost and execution environment limits.

### Space Complexity
`O(n)` for a full DP table with three columns, or `O(1)` if reduced to only the previous day’s three states. The state space is fixed-size, so rolling-state optimization is the natural production choice unless full trace reconstruction is required.

## 💡 Key Takeaways
- If the problem says “current choice depends only on the previous choice,” look for a tiny DP state keyed by the last action.
- If greedy feels plausible but future availability can invalidate today’s best-looking move, that is a strong signal for state-transition DP.
- Don’t forget that rest is always legal, even when both activities are available; it must remain a valid transition state.
- The activity value `3` means both options are independently available, not a third activity type; transitions must still respect consecutive-repeat rules.
- In production schedulers, compressing history into the smallest sufficient state is what keeps optimization logic fast, testable, and extensible.

## 🚀 Variations & Further Practice
- Add a penalty or reward score per activity instead of minimizing rests; now the DP optimizes weighted utility rather than a simple count.
- Extend the cooldown from 1 day to `k` days; the state must remember a longer activity history or remaining cooldown counters.
- Introduce more activity types with per-type repetition constraints; the conceptual twist is scaling the state space while keeping transitions efficient.