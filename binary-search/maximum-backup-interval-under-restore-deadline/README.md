# Maximum Backup Interval Under Restore Deadline

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, prefix-sum, array

---

## 🗂 Problem Overview
Given an array `changes` and a restore limit `L`, choose the largest positive backup interval `k` such that splitting the timeline into consecutive blocks of size `k` produces block sums all `<= L`. The last block may be shorter. Return that maximum `k`, or `0` if even `k = 1` fails. The challenge is scale: `n` can reach `200000`, so checking every `k` with naive range summation is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere a system batches contiguous work under a hard per-batch limit: backup compaction windows, streaming micro-batch sizing, storage segment rollups, checkpoint intervals, and log shipping. At scale, brute-force evaluation of every candidate interval burns both latency and compute budget, especially when validation itself scans large ranges repeatedly. The useful abstraction is monotone feasibility: once a larger batch size fails, all larger ones fail. Exploiting that property with binary search turns an operational tuning problem into a predictable decision procedure, which is exactly what you want in schedulers, controllers, and capacity-management loops.

## 🔍 Problem Statement
You are given:

- `changes[i]`: amount of changed data on day `i`
- `L`: maximum restoreable size for any single backup

For a chosen interval `k`, the array is partitioned into consecutive chunks of size `k` starting at index `0`: `[0..k-1]`, `[k..2k-1]`, and so on; the final chunk may be shorter. Interval `k` is feasible if every chunk sum is at most `L`. Return the maximum feasible `k`. If no positive `k` works, return `0`.

Constraints:

- `1 <= n == changes.length <= 200000`
- `0 <= changes[i] <= 1000000000`
- `0 <= L <= 1000000000000000000`

Examples:

- `changes = [2,1,3,2,2], L = 6` → output `3`
- `changes = [7,1,2], L = 6` → output `0`

The decisive constraint is `n = 200000`: evaluating all `k` values with repeated summation is not viable.

## 🪜 How to Solve This
1. Read the partition rule carefully → blocks are not arbitrary subarrays; they are fixed consecutive chunks starting at `0`. That removes combinatorial complexity and turns validation into deterministic range checks.

2. Ask what happens as `k` grows → each block becomes longer, so some block sums can only stay the same or increase. If a given `k` is infeasible, any larger `k` is also infeasible. That is the monotonic signal.

3. Monotonic feasibility usually means binary search on the answer → search `k` in `[1, n]` for the largest feasible value.

4. Now make feasibility cheap → for one candidate `k`, we need sums of blocks `[0, k-1]`, `[k, 2k-1]`, etc. Prefix sums give each block sum in `O(1)`.

5. Total check cost for one `k` becomes `O(n / k)` blocks, each constant time. Combined with binary search, this is fast enough in practice and straightforward to reason about.

6. Handle the failure baseline first → if even `k = 1` fails, return `0`.

## 🧩 Algorithm Walkthrough
1. **Build a prefix-sum array**  
   Use `prefix[i + 1] = prefix[i] + changes[i]`. Then any block sum from `l` to `r` is `prefix[r + 1] - prefix[l]`.  
   **Why correct:** prefix sums encode cumulative totals, so subtraction isolates a contiguous range.  
   **Invariant:** `prefix[t]` always equals the sum of `changes[0..t-1]`.

2. **Define a feasibility check for interval `k`**  
   Iterate block starts `s = 0, k, 2k, ...`. For each, compute `e = min(s + k, n)` and block sum `prefix[e] - prefix[s]`. If any sum exceeds `L`, return `false`; otherwise `true`.  
   **Why correct:** the problem fixes exactly these chunk boundaries.  
   **Invariant:** all previously scanned blocks satisfy the limit.

3. **Observe the monotone property**  
   If `k` is feasible, smaller `k` values are also feasible. Splitting a valid block into smaller consecutive pieces cannot increase any piece sum because all `changes[i] >= 0`.  
   **Why this matters:** feasibility forms a prefix of valid answers, which is the exact condition for **Binary Search on Answer**.

4. **Binary search the maximum feasible `k`**  
   Search over `[1, n]`. On feasible `mid`, store it and move right; on infeasible `mid`, move left.  
   **Invariant:** all values `<= answer` found feasible remain candidates; all values above the current infeasible boundary are discarded.

5. **Return `0` when necessary**  
   If no feasible `k` is found, return `0`. This covers cases where some single day already exceeds `L`.

This combines two standard patterns: **Prefix Sum** for constant-time range queries and **Binary Search on Monotone Predicate** for answer optimization.

## 📊 Worked Example
Take `changes = [2,1,3,2,2]`, `L = 6`.

Prefix sums:

| i | 0 | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|---|
| prefix[i] | 0 | 2 | 3 | 6 | 8 | 10 |

Binary search trace:

1. Search range `[1,5]`, `mid = 3`  
   Blocks: `[0,3) = 3`, `[3,5) = 4`? No — careful: `[0,3)` is `2+1+3 = 6`, `[3,5)` is `2+2 = 4`. All `<= 6` → feasible. Best = `3`.

2. Search range `[4,5]`, `mid = 4`  
   Blocks: `[0,4) = 8`, `[4,5) = 2`. First block exceeds `6` → infeasible.

3. Search range ends. Return `3`.

The key state is not individual elements but block boundaries and their sums. Prefix sums make each validation constant-time, so the search only pays for the number of blocks inspected.

## ⏱ Complexity Analysis
### Time Complexity
Building the prefix sum is `O(n)`. Each feasibility check scans `O(n / k)` blocks, and binary search performs `O(log n)` checks, so the worst-case bound is `O(n log n)`. For `10^6` elements this is still practical; for `10^9`, even linear scans become the real bottleneck, not the search strategy.

### Space Complexity
The prefix-sum array uses `O(n)` space and dominates memory usage. You could remove it and sum blocks on the fly, but then each feasibility check becomes more expensive or harder to reason about. The extra array is the right trade-off here.

## 💡 Key Takeaways
- If the question asks for the largest or smallest parameter making a condition true, check immediately whether the condition is monotone and therefore binary-searchable.
- Fixed contiguous chunking plus repeated range-sum validation is a strong signal for prefix sums rather than nested loops.
- The monotonic argument depends on `changes[i] >= 0`; with negative values, larger chunks would not necessarily be harder to satisfy.
- Be precise about chunk boundaries: blocks start at `0, k, 2k, ...`, and the last block is `[start, min(start + k, n))`.
- In production systems, monotone feasibility turns expensive tuning loops into deterministic control logic with predictable runtime and simpler failure envelopes.

## 🚀 Variations & Further Practice
- Allow arbitrary partitioning into segments of length at most `k` instead of fixed chunk boundaries. The monotone property may still hold, but feasibility becomes a dynamic programming or greedy proof problem.
- Minimize the restore limit `L` for a fixed `k`. Same prefix-sum foundation, but now the answer is the maximum chunk sum induced by that interval.
- Introduce online updates to `changes[i]` and repeated queries for maximum feasible `k`. Prefix sums are no longer enough; you need a Fenwick tree or segment tree plus a different validation strategy.