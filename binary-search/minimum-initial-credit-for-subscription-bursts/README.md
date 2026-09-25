# Minimum Initial Credit for Subscription Bursts

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, prefix-sum

---

## 🗂 Problem Overview
Given a sequence of daily credit deltas, an initial credit `S`, and at most `k` emergency top-ups worth exactly `x` each, determine the smallest `S` that guarantees the running balance never drops below zero. Top-ups may be inserted only immediately before a day, at most once per day.

The challenge is not simulating one strategy, but proving whether *some* strategy exists for a candidate `S`. Large input bounds rule out brute force placement of top-ups or dynamic programming over balances.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maintain a nonnegative resource level under bursty consumption with limited interventions: cloud budget guards, distributed rate-limiters with refill tokens, battery-aware schedulers, streaming backpressure buffers, and prepaid billing ledgers. At scale, the wrong approach explodes into state-space search over intervention points. The right framing turns it into a monotone feasibility problem: “is this reserve enough?” That enables predictable `O(n log A)` behavior, where `A` is the answer range, and gives operators a hard minimum safety buffer instead of heuristic overprovisioning.

## 🔍 Problem Statement
You are given an integer array `transactions` of length up to `200000`, where `transactions[i]` is the net credit change on day `i`. Positive values add credit; negative values consume it. Before processing any day, you may optionally apply one emergency top-up, adding exactly `x` credits, and you may do this on at most `k` distinct days total. Unused top-ups are allowed.

Find the minimum initial credit `S` such that, processing left to right, the balance is never negative after any day.

Key constraints:
- `1 <= n <= 200000`
- `-1e9 <= transactions[i] <= 1e9`
- `0 <= k <= 200000`
- `1 <= x <= 1e9`
- Answer fits in signed 64-bit integer

Examples:
- `transactions = [-4, 3, -6, 2], k = 1, x = 5` → `2`
- `transactions = [-8, -2, 5, -7], k = 2, x = 4` → `6`

The decisive constraint is input size: any solution that explores top-up placements directly is too slow.

## 🪜 How to Solve This
1. Read the problem → the output is the *minimum* initial credit, not the strategy itself. That is a strong signal for binary search on the answer.

2. Ask whether feasibility is monotone → if some `S` works, then any larger initial credit also works. More starting balance cannot hurt. That gives the binary-search predicate.

3. Now define `can(S)` → can we process the array left to right, using at most `k` top-ups, while never letting balance go negative?

4. For `can(S)`, think greedily → if balance would go negative on day `i`, delaying a top-up is impossible because the failure happens *now*. So the only sensible move is to use a top-up immediately before that day, if available.

5. That greedy rule makes the check linear → maintain current balance, and whenever `balance + transactions[i] < 0`, spend one top-up first. If even after adding `x` the day still fails, then `S` is infeasible.

6. Combine both pieces → binary search over `S`, using the linear greedy feasibility check. This avoids combinatorial placement search entirely.

## 🧩 Algorithm Walkthrough
1. **Use Binary Search on the Answer.**  
   The pattern is **Binary Search on Monotone Feasibility**. Define a predicate `can(S)` meaning “there exists a valid top-up schedule with initial credit `S`.” This predicate is monotone: if `can(S)` is true, then `can(S + d)` is also true for any `d >= 0`.

2. **Establish a safe search range.**  
   A lower bound can be `0`. A safe upper bound is obtainable by exponential search: start from `1` and double until `can(high)` becomes true. This avoids hand-deriving a brittle bound and stays within 64-bit guarantees.

3. **Simulate feasibility greedily.**  
   Maintain `balance`, initialized to `S`, and `used`, the number of top-ups consumed. For each day:
   - If `balance + transactions[i] >= 0`, process normally.
   - Otherwise, the day would fail. The only repair available is a top-up *before this day*. If `used == k`, fail.
   - Add `x`, increment `used`, and check again. If `balance + x + transactions[i] < 0`, fail immediately.

4. **Why the greedy choice is correct.**  
   The invariant is: before each day, `balance` is the maximum achievable balance among all strategies that used the same or fewer top-ups so far. Using a top-up earlier than necessary cannot improve future feasibility relative to saving it until the first failing day. So “top up exactly when forced” is optimal for the decision check.

5. **Return the first feasible `S`.**  
   Standard lower-bound binary search returns the minimum initial credit satisfying `can(S)`.

## 📊 Worked Example
Example: `transactions = [-4, 3, -6, 2], k = 1, x = 5`, test `S = 2`.

| Day | Balance Before | Transaction | Need Top-Up? | Balance After |
|---|---:|---:|---|---:|
| 1 | 2 | -4 | Yes, because `2 + (-4) < 0` | `2 + 5 - 4 = 3` |
| 2 | 3 | +3 | No | 6 |
| 3 | 6 | -6 | No | 0 |
| 4 | 0 | +2 | No | 2 |

Used top-ups: `1`, within `k = 1`, so `S = 2` is feasible.

Now test `S = 1`:
- Day 1 requires the only top-up: `1 + 5 - 4 = 2`
- Day 2 → `5`
- Day 3 → `-1`, but no top-ups remain

So `S = 1` is infeasible. Therefore the minimum valid answer is `2`.

## ⏱ Complexity Analysis
### Time Complexity
Each feasibility check scans the array once, so it costs `O(n)`. Binary search performs `O(log A)` checks, where `A` is the answer range in 64-bit space, giving `O(n log A)` overall. In practice `log A <= 63`, so even at `10^6` elements this remains operationally predictable; at `10^9`, the linear scan itself becomes the bottleneck.

### Space Complexity
The algorithm uses `O(1)` auxiliary space beyond the input array: a few 64-bit counters for balance, bounds, and top-up usage. There is no need for prefix arrays or DP tables. Space cannot be meaningfully reduced further without changing the input representation.

## 💡 Key Takeaways
- If the question asks for the minimum starting capacity/budget/credit and larger values only help, think binary search on the answer.
- If interventions are optional but limited, check whether “apply only when forced” yields a greedy feasibility test.
- Use 64-bit arithmetic everywhere: cumulative balances and binary-search bounds can exceed 32-bit even when individual transactions do not.
- The top-up decision happens **before** processing a day, and at most one top-up may be used on that day; allowing repeated same-day top-ups would change the predicate.
- In production systems, many “capacity planning” problems become tractable once reframed as monotone feasibility instead of explicit schedule construction.

## 🚀 Variations & Further Practice
- Allow variable top-up sizes from a set of coupons; feasibility is no longer a simple greedy scan and may require heap-based selection or DP over interventions.
- Charge a cost per top-up and minimize `initial_credit + penalty * topups_used`; the optimization objective becomes two-dimensional and breaks the simple monotone predicate.
- Extend from one account to multiple coupled accounts sharing a global top-up budget; the local greedy choice may fail, pushing the problem toward flow or scheduling formulations.