# Minimum Reorders to Group Expiring Coupons

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Bitmask

---

## 🗂 Problem Overview
Given an array `coupons`, compute the minimum number of single-element insert operations needed so that every distinct value appears in exactly one contiguous block. You may move any coupon to any position, and block order is unconstrained. The challenge is choosing the best block ordering so the maximum number of existing elements can stay where they are. With `n <= 200` but values bounded by `20`, the problem is small in length but combinatorial in group ordering.

## 🌍 Engineering Impact
This pattern shows up anywhere records must be reorganized into contiguous equivalence classes while minimizing physical movement: log compaction by tenant, warehouse batch picking by SKU, columnar storage re-clustering, compiler IR passes that group symbols by phase, or streaming pipelines that co-locate keys for downstream operators. At scale, the difference between “rebuild from scratch” and “maximize what stays in place” is write amplification, cache churn, and operational latency. The subset-DP framing matters because local greedy moves look plausible but fail once global block ordering determines how much existing structure can be preserved.

## 🔍 Problem Statement
You are given an integer array `coupons` where `1 <= coupons.length <= 200` and `1 <= coupons[i] <= 20`. Each number represents an expiration day. You want a final arrangement in which all equal values are contiguous, with blocks allowed in any order and internal ordering within a block irrelevant.

In one operation, pick a single coupon from its current position and insert it anywhere else. Return the minimum number of such operations required.

Examples:

- `coupons = [3, 1, 3, 2, 1]` → `2`
- `coupons = [4, 4, 2, 2, 3]` → `0`

The key constraint is not `n`; it is the small value domain. Distinct expiration days are at most `20`, which makes dynamic programming over subsets feasible. The right objective is to maximize how many coupons can remain in place under some block ordering, then subtract from `n`.

## 🪜 How to Solve This
1. Read the problem → notice this is not sorting. Equal values only need to be grouped, and block order is free.

2. Reframe the operation cost → moving one coupon means that coupon does not stay in its original “useful” position. So instead of simulating moves, maximize how many coupons can remain.

3. Fix a candidate order of value-blocks → once block order is chosen, the final array is partitioned into fixed segments whose lengths are the frequencies of each value.

4. Ask what can stay for that order → inside the segment reserved for value `v`, every existing `v` already in that segment can remain. Everything else must be moved.

5. Precompute segment scores fast → for each value and each interval, count how many occurrences of that value already lie there using prefix sums.

6. Optimize over block orders → the number of distinct values is small, so use bitmask DP over subsets. Each state says which value-blocks have already been placed; the next block starts at a deterministically known offset.

7. Final step → `answer = n - maxKept`. That converts a hard sequence-edit problem into a compact subset optimization problem.

## 🧩 Algorithm Walkthrough
1. **Compress distinct values.**  
   Map each expiration day present in `coupons` to an index `0..m-1`, where `m <= 20`. Record `freq[i]`, the count of each value. This reduces the DP dimension to the number of distinct groups, not array length.

2. **Build prefix counts.**  
   For each compressed value `i`, build `prefix[i][p] = number of occurrences of value i in coupons[0..p-1]`. This lets us query how many `i`s already lie in any interval `[l, r)` in `O(1)`.

3. **Define the DP state.**  
   Use **Bitmask Dynamic Programming**: `dp[mask] = maximum number of coupons that can stay in place after placing exactly the blocks in mask first`. This is the right abstraction because block order is the only global decision, and the set of already placed blocks fully determines the next segment start.

4. **Track segment boundaries implicitly.**  
   For each `mask`, compute `len(mask) = sum(freq[i] for i in mask)`. That is the starting index of the next block in the final arrangement. Invariant: the first `len(mask)` positions are fully assigned to the values in `mask`.

5. **Transition by placing one more block.**  
   For every value `j` not in `mask`, place its block next in interval `[start, start + freq[j])`, where `start = len(mask)`. The number of coupons of value `j` that can remain is  
   `keep = prefix[j][start + freq[j]] - prefix[j][start]`.  
   Then update  
   `dp[mask | (1 << j)] = max(dp[mask | (1 << j)], dp[mask] + keep)`.

6. **Extract the answer.**  
   `dp[(1 << m) - 1]` is the maximum number of coupons that can stay under the best block ordering. The minimum moves are `n - dp[fullMask]`. This is correct because every coupon either already lies inside its assigned final block or must be moved exactly once by an insert operation.

## 📊 Worked Example
Take `coupons = [3, 1, 3, 2, 1]`.

Compress values as: `3 -> 0`, `1 -> 1`, `2 -> 2`  
Frequencies: `freq = [2, 2, 1]`

| Mask | Placed blocks | Start | Try next block | Target interval | Keep | New dp |
|---|---|---:|---|---|---:|---:|
| `000` | none | 0 | `3` | `[0,2)` | 1 | 1 |
| `000` | none | 0 | `1` | `[0,2)` | 1 | 1 |
| `000` | none | 0 | `2` | `[0,1)` | 0 | 0 |
| `001` | `3` | 2 | `1` | `[2,4)` | 1 | 2 |
| `001` | `3` | 2 | `2` | `[2,3)` | 0 | 1 |
| `011` | `3,1` | 4 | `2` | `[4,5)` | 0 | 2 |

Best full-mask value is `3`, meaning three coupons can stay where they are under the best block order. Therefore:

`minimum moves = 5 - 3 = 2`

One valid final grouping is `[3, 3, 1, 1, 2]`.

## ⏱ Complexity Analysis
### Time Complexity
Let `m` be the number of distinct coupon values. The DP has `2^m` states, and each state tries up to `m` next blocks, so the dominant cost is `O(m * 2^m)`, plus `O(mn)` for prefix counts. With `m <= 20`, this is practical; at `10^6` or `10^9` distinct groups it is completely infeasible.

### Space Complexity
The main space is `O(2^m)` for the DP table and `O(mn)` for prefix counts. Space can be trimmed slightly by computing interval counts differently, but the exponential DP is the irreducible cost of exploring block orders.

## 💡 Key Takeaways
- If values must become contiguous and block order is unconstrained, think “choose an order of groups,” not “simulate moves.”
- When the value domain is small but ordering choices are global, subset DP is often the right tool even if the array itself is longer.
- The DP transition depends on the cumulative length of already placed blocks; off-by-one errors in interval boundaries `[start, start + freq)` are the common failure mode.
- Compress only values that actually appear; iterating over the full value range is harmless here but often introduces unnecessary states and bugs.
- The transferable design insight is to optimize for preserved structure first; minimizing edits is often equivalent to maximizing what can remain untouched under a better global layout.

## 🚀 Variations & Further Practice
- Require blocks to appear in increasing expiration-day order. The subset DP disappears, but the problem becomes a fixed-layout optimization over predetermined segments.
- Charge different move costs per coupon or per distance moved. Now “keep vs move” is no longer binary, and the objective shifts toward weighted interval placement or min-cost flow.
- Allow splitting a value across at most two blocks instead of exactly one. This breaks the simple subset-state model and forces richer state to track partially placed groups.