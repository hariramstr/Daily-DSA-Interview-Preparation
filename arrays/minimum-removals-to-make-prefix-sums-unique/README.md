# Minimum Removals to Make Prefix Sums Unique

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** arrays, dynamic-programming, hash-map

---

## 🗂 Problem Overview
Given an array `nums`, remove the fewest elements so the remaining subsequence has pairwise distinct prefix sums. Order must be preserved, but elements can be skipped arbitrarily. Equivalently, maximize the length of a subsequence whose running totals never repeat. The difficulty is that local choices are misleading: keeping an element changes every later prefix sum, so this is not a simple duplicate-detection or sliding-window problem. With `n` up to `200000`, quadratic subsequence DP is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a cumulative state must avoid collisions under selective retention. In streaming pipelines, deduplication over running aggregates can require dropping events while preserving order. In distributed rate-limiters and quota ledgers, repeated cumulative balances can indicate logically equivalent states that must be avoided for auditability or replay correctness. In compilers and incremental build systems, selective dependency retention under state-uniqueness constraints appears in optimization passes. At scale, brute-force backtracking collapses under long histories; the value of this approach is turning a global subsequence constraint into a linear pass with state compression keyed by cumulative sums.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n`, where `1 <= n <= 200000` and `-10^9 <= nums[i] <= 10^9`. You may remove any elements, but the remaining elements must stay in their original relative order. Let the kept subsequence be `b`. Its prefix sums are:

- `pref[0] = b[0]`
- `pref[1] = b[0] + b[1]`
- ...
- `pref[m - 1] = b[0] + ... + b[m - 1]`

The goal is to minimize removals so that all prefix sums in `b` are distinct.

Examples:

- `nums = [2, -2, 3, 1, -1]` → `1`
- `nums = [1, -1, 1, -1, 1]` → `2`

Negative values and zeros matter: repeated prefix sums can arise far apart, and the valid result is a subsequence, not a subarray. The `O(n^2)` state space of naive subsequence DP is the core constraint forcing a compressed dynamic-programming approach.

## 🪜 How to Solve This
1. Start from the condition itself → repeated prefix sums are forbidden in the kept subsequence.

2. For a kept subsequence with current total `S`, appending `x` creates new prefix sum `S + x`. That append is valid only if `S + x` has not appeared earlier in that subsequence.

3. A naive DP would track the full set of seen prefix sums for every subsequence state. That is hopeless at `n = 200000`.

4. The key observation: we do not need the whole history explicitly. We need the best subsequence length achievable for each current total. Let `dp[s]` mean: maximum kept length of a valid subsequence whose final prefix sum is `s`.

5. To append `x` and end at `t`, we must come from some prior total `p = t - x`, but only if `t` was not already used inside that subsequence. The trick is to encode this exclusion using a “best so far except forbidden sum” transition.

6. Process left to right, maintain best valid lengths indexed by current total, and update through a hash map. The answer is `n - maxKeptLength`.

7. This is dynamic programming over subsequences with hash-based state compression: preserve order, compress by cumulative sum, avoid quadratic pairwise comparisons.

## 🧩 Algorithm Walkthrough
1. **Reframe the objective as maximization.**  
   Instead of minimizing removals directly, maximize the length of a valid kept subsequence. Final answer is `n - best`. This is standard subsequence optimization and removes one degree of bookkeeping.

2. **Define the DP state.**  
   Let `best[s]` be the maximum length of a valid subsequence whose last prefix sum equals `s`. Also treat the empty subsequence as having cumulative sum `0` before any element is chosen. The invariant: every subsequence represented by `best[s]` has all prefix sums distinct.

3. **Track the best predecessor length available before a forbidden sum repeats.**  
   When considering value `x`, appending it to a subsequence ending at total `p` creates new total `t = p + x`. This is valid only if `t` is not already one of that subsequence’s prefix sums. To support this efficiently, maintain for each total the best subsequence length that can transition without reusing that total. This is the hash-map DP compression step.

4. **Process elements in order.**  
   For each `x`, compute candidate updates from previously stored states only; do not chain within the same iteration. This preserves subsequence order and prevents using one element multiple times.

5. **Apply max updates per resulting sum.**  
   If appending `x` yields a better valid subsequence ending at total `t`, update `best[t]`. The invariant after processing index `i`: `best` contains optimal valid subsequences drawn only from `nums[0..i]`.

6. **Use hash maps because sums are sparse and large.**  
   Prefix totals can be as large as `±2e14`, so array indexing is impossible. Hash maps give expected `O(1)` updates and lookups.

7. **Return removals.**  
   Let `L` be the maximum value in `best`. The minimum removals is `n - L`.

Pattern: **Dynamic Programming + Hash Map state compression**. It is the right abstraction because the problem is globally constrained, order-sensitive, and driven by sparse cumulative-state transitions rather than local window structure.

## 📊 Worked Example
Take `nums = [2, -2, 3, 1, -1]`.

| Step | x  | Best valid subsequence ending here | Prefix sums | Length |
|------|----|------------------------------------|-------------|--------|
| 1 | 2  | `[2]` | `[2]` | 1 |
| 2 | -2 | `[2, -2]` | `[2, 0]` | 2 |
| 3 | 3  | `[2, -2, 3]` | `[2, 0, 3]` | 3 |
| 4 | 1  | `[2, -2, 3, 1]` | `[2, 0, 3, 4]` | 4 |
| 5 | -1 | appending gives total `3` again | `[2, 0, 3, 4, 3]` | invalid |

At the last step, the new prefix sum would be `3`, which already appeared after keeping the third element. So any subsequence that keeps all five elements is invalid. The best valid kept length is `4`, achieved by dropping the final `-1`. Therefore minimum removals = `5 - 4 = 1`.

## ⏱ Complexity Analysis
### Time Complexity
Expected `O(n)` with hash-map-backed DP updates, assuming constant-time average lookup and insert. Each element contributes a bounded number of state transitions and max-updates. At `10^6` elements this is practical in optimized languages; at `10^9`, even linear time is too large, so the bottleneck becomes I/O and memory bandwidth rather than algorithm design.

### Space Complexity
`O(k)`, where `k` is the number of distinct cumulative sums stored in the hash maps, worst-case `O(n)`. The space is owned by sparse DP state keyed by sums. It cannot be meaningfully reduced without losing constant-time transitions; compression would trade memory for slower lookups or offline coordinate remapping.

## 💡 Key Takeaways
- If the problem says “keep order, remove arbitrary elements, optimize a global property,” think subsequence DP before considering windows or greedy.
- If validity depends on cumulative totals that can be large, sparse, and repeated, a hash map keyed by prefix sum is usually the right state representation.
- Do not update DP states in-place in a way that lets the current element feed another transition in the same iteration.
- Be explicit about the empty subsequence baseline sum `0`; many off-by-one errors come from mishandling the first kept element.
- The production-grade insight is state compression: represent only the cumulative states that matter, not the full combinatorial history.

## 🚀 Variations & Further Practice
- Require all prefix sums to be distinct **and non-negative**. The extra feasibility constraint couples transition validity with state value, making pruning and dominance rules more subtle.
- Assign a removal cost per element instead of unit cost. Now the objective is maximum retained weight or minimum deletion cost, which turns the DP into weighted subsequence optimization.
- Generalize from “no repeated prefix sum” to “no prefix sum may appear more than `k` times.” The state must track bounded multiplicities, which breaks the simple one-value-per-sum compression.