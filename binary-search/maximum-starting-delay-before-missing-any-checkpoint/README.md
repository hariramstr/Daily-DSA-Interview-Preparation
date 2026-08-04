# Maximum Starting Delay Before Missing Any Checkpoint

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Prefix Sum, Greedy

---

## 🗂 Problem Overview
Given ordered checkpoint travel times and per-checkpoint deadlines, compute the largest integer delay `x` you can wait before starting while still reaching every checkpoint on time. Arrival at checkpoint `i` is `x + prefix[i]`, where `prefix[i]` is cumulative travel time up to `i`. Return the maximum feasible `x`, or `-1` if even starting immediately misses some deadline. The non-trivial part is scale: `n` is large and values require 64-bit arithmetic.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the latest safe start time under cumulative latency budgets: streaming pipelines with stage SLAs, distributed job orchestration with per-hop deadlines, request routing through service chains, and logistics or fulfillment planning with checkpoint cutoffs. At scale, brute-force simulation over candidate delays is operationally useless because latency budgets and path lengths are large, and deadline checks happen continuously. The monotonic-feasibility framing enables predictable `O(n log M)` behavior, which matters when these checks sit inside schedulers, admission controllers, or planning services that must make fast decisions under heavy load.

## 🔍 Problem Statement
You are given two arrays of length `n`:

- `travel[i]`: time required to move from checkpoint `i - 1` to checkpoint `i`
- `deadline[i]`: latest allowed arrival time at checkpoint `i`

You may wait an integer number of minutes `x >= 0` before starting. After that, checkpoints must be visited in order, without skipping or reordering. Let `prefix[i] = travel[0] + ... + travel[i]`. Then checkpoint `i` is feasible iff:

`x + prefix[i] <= deadline[i]`

Your task is to return the maximum integer `x` satisfying all checkpoints, or `-1` if no such `x` exists even when `x = 0`.

Constraints:

- `1 <= n <= 2 * 10^5`
- `1 <= travel[i] <= 10^9`
- `1 <= deadline[i] <= 10^18`

Examples:

- `travel = [3, 2, 4], deadline = [5, 8, 12]` → `3`
- `travel = [4, 4, 4], deadline = [3, 10, 15]` → `-1`

The key algorithmic signal is monotonic feasibility: if delay `x` works, every smaller delay also works.

## 🪜 How to Solve This
1. Read the condition carefully → each checkpoint imposes an upper bound on the starting delay: `x <= deadline[i] - prefix[i]`.

2. That means the route is feasible only if `x` is no larger than **all** of those bounds. So the true answer is the minimum slack across checkpoints.

3. Why is binary search still natural here? Because the problem explicitly gives a monotonic predicate:
   - if `x` is feasible, any smaller `x` is feasible;
   - if `x` is infeasible, any larger `x` is also infeasible.

4. Once you see monotonicity, think:
   - define `can(x)` = “all arrivals meet deadlines”
   - binary search the largest `x` where `can(x)` is true

5. To evaluate `can(x)`, scan once, maintain cumulative travel time, and verify `x + prefix <= deadline` at every step.

6. Before searching, note the impossible case falls out naturally: if `can(0)` is false, return `-1`.

7. The mental model is “latest safe launch time under cumulative constraints.” Each checkpoint tightens the allowable start window; the tightest checkpoint determines the answer.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Binary Search on Answer.**  
   The search space is the integer starting delay `x`. Feasibility is monotonic: once a delay fails, every larger delay fails too. That makes binary search the right abstraction.

2. **Define the feasibility check `can(x)`.**  
   Scan checkpoints from left to right, accumulating `prefix += travel[i]`. For each checkpoint, test whether `x + prefix <= deadline[i]`. If any checkpoint violates this, return `false`; otherwise return `true`.  
   **Invariant:** after processing index `i`, all checkpoints `0..i` are on time under delay `x`.

3. **Handle the impossible case early.**  
   Run `can(0)`. If it fails, no non-negative delay can work, because any larger `x` only increases every arrival time. Return `-1`.

4. **Establish binary search bounds.**  
   Use `low = 0`. A safe `high` can be `min(deadline)` or `10^18`, since the answer is guaranteed to fit in signed 64-bit range. The tighter the upper bound, the fewer iterations, but either is acceptable.

5. **Binary search for the maximum feasible delay.**  
   While `low <= high`, compute `mid`. If `can(mid)` is true, record it and move right (`low = mid + 1`) to search for a larger valid delay. Otherwise move left (`high = mid - 1`).  
   **Invariant:** all values `<= answer` are feasible; all values `> answer` in the rejected region are infeasible.

6. **Return the best feasible value found.**  
   This is the largest integer delay satisfying every checkpoint deadline.

This combines **Prefix Sum** for cumulative travel, **Greedy feasibility checking** for a fixed candidate, and **Binary Search** to exploit monotonicity.

## 📊 Worked Example
Take `travel = [3, 2, 4]`, `deadline = [6, 8, 12]`.

We test candidate delays with `can(x)`:

| Checkpoint `i` | `travel[i]` | `prefix` | `deadline[i]` | Arrival with `x=3` | Feasible? |
|---|---:|---:|---:|---:|---:|
| 0 | 3 | 3 | 6  | 6  | Yes |
| 1 | 2 | 5 | 8  | 8  | Yes |
| 2 | 4 | 9 | 12 | 12 | Yes |

So `can(3) = true`.

Now try `x = 4`:

| Checkpoint `i` | `prefix` | `deadline[i]` | Arrival with `x=4` | Feasible? |
|---|---:|---:|---:|---:|
| 0 | 3 | 6  | 7  | No |

The first checkpoint already fails, so `can(4) = false`.

Binary search therefore converges to `3`, the largest feasible starting delay. The important state is just cumulative travel plus the current candidate delay; no dynamic programming or backtracking is needed.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `M` is the size of the delay search range, typically up to `10^18`. Each feasibility check is a single linear scan over `n` checkpoints, and binary search performs about 60 iterations in the worst 64-bit case. This remains practical for `10^6`-scale scans, but not for `10^9` elements.

### Space Complexity
`O(1)` auxiliary space if prefix sums are accumulated on the fly. The only extra state is a few 64-bit counters and binary search bounds. You could precompute a prefix array, but that raises space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If the problem asks for the maximum value that still satisfies a condition, check whether feasibility is monotonic; that is the strongest signal for binary search on answer.
- When constraints are cumulative and ordered, prefix sums plus a one-pass validator often turn a global condition into a cheap local scan.
- Use 64-bit arithmetic for cumulative travel, candidate delay, and `x + prefix`; overflow is easy here and silently breaks feasibility checks.
- Be explicit about the binary search variant: this is “find the last true,” so update `low = mid + 1` on success and retain the current answer.
- In production systems, this pattern generalizes to “latest safe admission point under cumulative budgets,” a useful framing for schedulers, pipelines, and deadline-aware routing.

## 🚀 Variations & Further Practice
- Allow optional waiting at intermediate checkpoints, not just before the start. The twist is that feasibility is no longer a single global start offset; local slack distribution becomes part of the decision.
- Add multiple routes and ask for the route that maximizes starting delay. The harder part is combining per-route feasibility with route selection efficiently.
- Replace fixed deadlines with checkpoint windows `[open[i], close[i]]`. Now you must reason about both earliest and latest feasible arrivals, turning the validator into interval propagation rather than a simple upper-bound check.