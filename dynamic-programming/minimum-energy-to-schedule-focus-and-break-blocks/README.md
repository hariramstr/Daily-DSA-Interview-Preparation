# Minimum Energy to Schedule Focus and Break Blocks

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, state-compression, array

---

## 🗂 Problem Overview
Given two cost arrays, choose for each task either **Focus** or **Break** so total energy is minimized. The catch is global: you may not schedule more than `k` consecutive Focus tasks. Input is `focusCost[]`, `breakCost[]`, and `k`; output is the minimum valid total energy across all `n` tasks. The problem is non-trivial because each local choice affects future legality through the current Focus streak length, so greedy selection is insufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere a system optimizes per-item cost under a bounded-run constraint. Examples include distributed rate-limiters that allow bursts up to a cap before requiring cooldown, streaming pipelines that batch expensive operators but enforce fairness checkpoints, CPU schedulers that limit uninterrupted high-power execution, and ad-delivery systems that cap repeated premium placements. At scale, naive local optimization overuses the cheaper mode and violates operational policy. Dynamic programming with compressed state turns a policy-constrained sequence optimization into a linear pass, which is the difference between something deployable on large event streams and something that collapses under latency or memory pressure.

## 🔍 Problem Statement
You are given integer arrays `focusCost` and `breakCost`, both of length `n`, and an integer `k`. For each task `i`, you must choose exactly one mode:

- **Focus** → pay `focusCost[i]`
- **Break** → pay `breakCost[i]`

The final schedule must satisfy: every maximal consecutive run of Focus tasks has length at most `k`.

Return the minimum total energy.

Constraints:

- `1 <= n <= 100000`
- `1 <= k <= n`
- `focusCost.length == breakCost.length == n`
- `1 <= focusCost[i], breakCost[i] <= 1000000000`

Examples:

- `focusCost = [3,2,5,1]`, `breakCost = [4,6,1,7]`, `k = 2` → `7`
- `focusCost = [1,1,1,1]`, `breakCost = [10,10,10,10]`, `k = 2` → `13`

The key constraint is `n = 1e5`: any `O(nk)` or quadratic DP is risky or unacceptable when `k` is large.

## 🪜 How to Solve This
1. Read the rule carefully → legality depends only on the **current streak length of Focus**, not the full history.
2. That immediately suggests DP state compression: after processing task `i`, the only relevant state is how many consecutive Focus tasks end at `i`.
3. Define DP over streak length:
   - choosing **Break** resets streak to `0`
   - choosing **Focus** moves streak `s -> s+1`, only if `s < k`
4. A direct DP with states `0..k` works conceptually, but `O(nk)` is too slow when both `n` and `k` can be `1e5`.
5. Look at transitions:
   - `dpNext[0] = min(dp[*]) + breakCost[i]`
   - `dpNext[s+1] = dp[s] + focusCost[i]`
6. The expensive part is repeatedly computing `min(dp[*])`. Maintain that minimum incrementally.
7. Now each task does constant work per state shift if implemented naively, but we can reformulate further:
   - let Break positions partition the array into Focus runs of length at most `k`
   - equivalently, start from all-Break cost and decide which tasks to convert to Focus using gain `breakCost[i] - focusCost[i]`, with chosen indices grouped into runs of length at most `k`
8. That becomes a linear DP with prefix minima / sliding-window optimization.

## 🧩 Algorithm Walkthrough
1. **Transform the objective.**  
   Start with `base = sum(breakCost)`, meaning every task is scheduled as Break. If task `i` is switched to Focus, total cost changes by `delta[i] = focusCost[i] - breakCost[i]`. Now the problem is: choose positions to pay `delta[i]`, with chosen positions forming Focus runs of length at most `k`. This is equivalent and isolates the constraint.

2. **Define the DP pattern: dynamic programming on sequence prefixes.**  
   Let `dp[i]` be the minimum total cost for the first `i` tasks. To end at task `i`, there are two possibilities:
   - task `i` is Break: `dp[i] = dp[i-1] + breakCost[i-1]`
   - task `i` ends a Focus run of length `len` where `1 <= len <= k`

3. **Write the Focus-run transition.**  
   If tasks `[j..i]` are Focus (`len = i-j+1`), then task `j-1` must be Break unless `j = 1`. Cost is:
   `dp[j-1] + sumFocus(j..i)`, except that when `j > 1`, `dp[j-1]` already includes task `j-1` in some valid mode, so no extra handling is needed. We only need fast range sums of `focusCost`.

4. **Use prefix sums to make run costs O(1).**  
   Build `prefFocus`, where `prefFocus[i]` is the sum of the first `i` focus costs. Then:
   `sumFocus(j..i) = prefFocus[i] - prefFocus[j-1]`.

5. **Rearrange for sliding-window minimum.**  
   For each `i`, Focus transitions are:
   `dp[i] = min(dp[i], prefFocus[i] + min_{j in [i-k+1, i]}(dp[j-1] - prefFocus[j-1]))`
   This is the critical optimization. The inner expression depends on `j` only through `dp[j-1] - prefFocus[j-1]`.

6. **Maintain that minimum with a monotonic deque.**  
   As `i` advances, valid `j` values form a sliding window of size `k`. Store candidate indices `t = j-1` with value `dp[t] - prefFocus[t]` in increasing order. Pop expired indices from the front; pop dominated candidates from the back. This yields the best Focus-run start in `O(1)` amortized time.

7. **Invariant.**  
   After processing position `i`, `dp[i]` is the minimum valid cost for the first `i` tasks, and the deque contains exactly the best candidates for starting a Focus run that ends at future positions while respecting the maximum run length `k`.

## 📊 Worked Example
Example: `focus = [3,2,5,1]`, `break = [4,6,1,7]`, `k = 2`

Let `dp[0] = 0`, `prefFocus = [0,3,5,10,11]`.

| i | Break transition | Best Focus window candidate | Focus transition | dp[i] |
|---|---:|---:|---:|---:|
| 1 | `dp[0]+4 = 4` | `dp[0]-pref[0]=0` | `pref[1]+0 = 3` | 3 |
| 2 | `dp[1]+6 = 9` | min of `{0, dp[1]-pref[1]=0}` | `pref[2]+0 = 5` | 5 |
| 3 | `dp[2]+1 = 6` | window allows starts at 2..3, best is `dp[2]-pref[2]=0` | `pref[3]+0 = 10` | 6 |
| 4 | `dp[3]+7 = 13` | window allows starts at 3..4, best is `dp[3]-pref[3]=-4` | `pref[4]-4 = 7` | 7 |

Result: `7`, achieved by `Focus, Focus, Break, Focus`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each task performs constant-time DP updates plus amortized `O(1)` deque maintenance. Every index enters and leaves the deque at most once. At `10^6` elements this remains practical in optimized runtimes; at `10^9`, even linear scans become throughput-bound and require distributed or streaming assumptions beyond this problem.

### Space Complexity
`O(n)` if you store full `dp` and prefix sums. The dominant structures are the DP array, prefix array, and deque. `dp` can be reduced if needed, but because deque candidates reference historical states, you still need enough retained values to evaluate `dp[t] - prefFocus[t]`.

## 💡 Key Takeaways
- If a sequence problem says “optimize cost” plus “no more than `k` consecutive X,” the DP state is usually the current run length or an equivalent run-boundary formulation.
- When a DP transition scans all run lengths `1..k`, look for prefix sums plus a sliding-window minimum to collapse `O(nk)` into `O(n)`.
- Be precise about indexing: `dp[i]` over first `i` tasks and `pref[i]` over first `i` costs avoids off-by-one errors in run sum calculations.
- The valid Focus-run starts for position `i` are constrained to `j in [i-k+1, i]`; expiring stale deque entries one step too late silently permits illegal runs.
- In production optimization systems, policy constraints often become tractable once you convert “history-dependent legality” into a compact state and then optimize the expensive transition with the right data structure.

## 🚀 Variations & Further Practice
- Allow a penalty or bonus for **switching modes** between adjacent tasks. This adds boundary cost, forcing the DP to track both run length and previous mode.
- Require both **max Focus run ≤ k** and **max Break run ≤ m**. Now both modes have bounded streaks, so the state space becomes two-sided rather than reset-on-break.
- Generalize from binary choice to **multiple execution modes** with one constrained mode and several unconstrained alternatives; the challenge becomes combining per-mode transitions without blowing up state count.