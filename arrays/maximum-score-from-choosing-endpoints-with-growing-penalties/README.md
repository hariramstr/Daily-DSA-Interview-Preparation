# Maximum Score from Choosing Endpoints with Growing Penalties

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Dynamic Programming, Arrays, Interval DP

---

## 🗂 Problem Overview
Given an array `nums`, remove all elements one by one, choosing only the current leftmost or rightmost value. The `t`-th removal contributes `value * t` to the score, and switching removal side from the previous step costs a fixed penalty `p`. The goal is to maximize total score after all removals. The difficulty is that each decision affects both future multipliers and future switch costs, so local greedy choices fail under `n <= 2000`.

## 🌍 Engineering Impact
This pattern shows up in systems where actions are constrained to boundary choices and the cost of changing strategy is stateful. Examples include storage compaction that can consume from hot/cold ends with seek penalties, streaming schedulers that drain from priority buckets while paying context-switch overhead, and bidirectional rollout/rollback planners where switching direction incurs operational cost. At scale, greedy heuristics overfit to immediate gain and miss global optimum because the value of a choice depends on remaining horizon. Interval DP is what lets you encode “what remains” plus “how you got here” without exploding into exponential search.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n`, where `1 <= n <= 2000`, and a non-negative penalty `p`. At each step `t` from `1` to `n`, remove either the leftmost or rightmost remaining element. If the removed value is `x`, add `x * t` to the score. If the current removal side differs from the previous removal side, subtract `p`. No penalty applies on the first move.

Return the maximum possible total score. Values may be negative, so delaying a negative number can make it more harmful, while delaying a positive number can increase its contribution. That interaction, combined with switch penalties, rules out greedy strategies.

Examples:

- `nums = [4, 2, 9], p = 3` → `35`
- `nums = [8, -5, 7, 3], p = 4` → `43`

The key algorithmic constraint is `n = 2000`: exponential endpoint-choice enumeration is impossible, but `O(n^2)` dynamic programming is feasible.

## 🪜 How to Solve This
1. Start from the brute-force view → every step chooses left or right, so there are `2^n` possible removal sequences. That is immediately too large.

2. Notice what actually defines a subproblem → after some removals, the remaining array is always a contiguous interval `[l, r]`. We do not need the full history, only:
   - which interval remains,
   - what removal number comes next,
   - which side was used last, because that determines whether a penalty applies.

3. The next removal number is not independent state. If `[l, r]` remains, then `removed = n - (r - l + 1)`, so the next time is `t = removed + 1`.

4. That suggests interval DP with an extra bit of state for the last side used. For each interval, compute the best score obtainable after removing everything outside it, ending with the last removal taken from the left or right boundary of the removed region.

5. Transition by extending from smaller intervals to larger removed prefixes/suffixes, or equivalently by shrinking the remaining interval. Each transition adds `nums[l] * t` or `nums[r] * t`, and subtracts `p` only if the side changes.

6. Once that state model is clear, the problem becomes a standard `O(n^2)` interval DP rather than an exponential search tree.

## 🧩 Algorithm Walkthrough
1. **Define the DP state using Interval DP.**  
   Let `dpL[l][r]` be the maximum score achievable after removing everything outside interval `[l, r]`, where the **most recent removal** was from the left side of the then-current interval. Similarly, `dpR[l][r]` tracks states whose most recent removal was from the right. The remaining elements are exactly `nums[l..r]`.

2. **Derive the time multiplier from interval length.**  
   If `[l, r]` remains, then `removed = n - (r - l + 1)`. The next removal happens at time `t = removed + 1`. This invariant eliminates one DP dimension.

3. **Initialize single-element intervals.**  
   When only `nums[i]` remains, it must be removed at time `n`. Both `dpL[i][i]` and `dpR[i][i]` equal `nums[i] * n`, because there is no future side distinction once the process ends.

4. **Expand to larger intervals in reverse order.**  
   For interval `[l, r]`, the first move from this state is either:
   - remove `nums[l]` at time `t`, leaving `[l+1, r]`,
   - or remove `nums[r]` at time `t`, leaving `[l, r-1]`.

5. **Apply side-aware transitions.**  
   If you remove left now, the next state is either `dpL[l+1][r]` with no switch penalty or `dpR[l+1][r] - p` if the next recorded last side differs. Symmetrically for removing right. This maintains the invariant that the DP value already includes all future optimal choices.

6. **Take the best starting move.**  
   The answer is `max(dpL[0][n-1], dpR[0][n-1])`, representing the best full removal plan from the original array. The abstraction is correct because every valid sequence corresponds to exactly one path through shrinking intervals with side-labeled transitions.

A compact recurrence is:

```text
t = n - (r - l)

dpL[l][r] = nums[l] * t + max(
    dpL[l+1][r],
    dpR[l+1][r] - p
)

dpR[l][r] = nums[r] * t + max(
    dpR[l][r-1],
    dpL[l][r-1] - p
)
```

with base case:

```text
dpL[i][i] = dpR[i][i] = nums[i] * n
```

## 📊 Worked Example
Use `nums = [4, 2, 9]`, `p = 3`.

Let interval length grow from `1` to `3`.

| Interval | t | dpL | dpR |
|---|---:|---:|---:|
| `[2,2]` (`9`) | 3 | 27 | 27 |
| `[1,1]` (`2`) | 3 | 6 | 6 |
| `[0,0]` (`4`) | 3 | 12 | 12 |
| `[1,2]` (`2,9`) | 2 | `2*2 + max(27, 27-3)=31` | `9*2 + max(6, 6-3)=24` |
| `[0,1]` (`4,2`) | 2 | `4*2 + max(6, 6-3)=14` | `2*2 + max(12, 12-3)=16` |
| `[0,2]` (`4,2,9`) | 1 | `4 + max(31, 24-3)=35` | `9 + max(16, 14-3)=25` |

Final answer: `max(35, 25) = 35`.

Interpretation: the optimal plan starts by removing left `4`, then continues optimally on `[1,2]` without paying unnecessary switch penalties, yielding the all-left sequence.

## ⏱ Complexity Analysis

### Time Complexity
`O(n^2)`. There are `O(n^2)` intervals, and each interval computes `dpL` and `dpR` in constant time from adjacent smaller intervals. For `n = 2000`, this is practical. At `10^6` elements, quadratic DP is already infeasible; at `10^9`, it is completely out of scope without additional structure.

### Space Complexity
`O(n^2)` for the two DP tables `dpL` and `dpR`, which dominate memory usage. This can be reduced to `O(n)` with diagonal rolling arrays because each state depends only on the previous interval length, but that trades away some clarity and debugging convenience.

## 💡 Key Takeaways
- If choices are restricted to array endpoints and the remaining state is always a contiguous interval, think interval DP before considering search or greedy.
- If transition cost depends on the previous action, add that action as a small DP state dimension instead of encoding full history.
- The time multiplier is easy to off-by-one: for remaining interval `[l, r]`, the next removal time is `n - (r - l + 1) + 1`, which simplifies to `n - (r - l)`.
- Use 64-bit arithmetic everywhere: `nums[i]` and `p` can both be `1e9`, and weighted sums easily exceed 32-bit range.
- In production optimization problems, the winning model often comes from identifying the minimal sufficient state, not from improving the search heuristic.

## 🚀 Variations & Further Practice
- Add a side-dependent penalty matrix, where switching `L→R` and `R→L` have different costs, or even staying on the same side has a cost. The twist is that transition logic becomes asymmetric.
- Allow removing up to `k` elements from either end per step. The interval abstraction survives, but each state now has up to `2k` transitions instead of two.
- Make the multiplier depend on remaining length or a non-linear scoring function. The harder part is proving whether the same DP state remains sufficient or whether additional dimensions are required.