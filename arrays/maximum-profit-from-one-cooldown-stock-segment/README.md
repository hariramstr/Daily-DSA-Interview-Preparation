# Maximum Profit from One Cooldown Stock Segment

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, State Compression

---

## 🗂 Problem Overview
Given daily prices, compute the maximum profit when unlimited transactions are allowed, holding at most one unit at a time, with a 1-day cooldown after each sale. The twist: you must choose exactly one contiguous segment where that cooldown is disabled, but only for actions fully inside that segment. The challenge is not stock trading itself; it is combining two different trading regimes over one array under `n <= 200000`, which rules out quadratic boundary testing.

## 🌍 Engineering Impact
This pattern shows up anywhere a system runs under one global policy except for one explicitly scoped override window. Examples include distributed rate-limiters with a temporary burst allowance, streaming pipelines with a relaxed backpressure interval, ad-serving systems with a campaign-specific pacing override, and schedulers with one maintenance window where normal spacing constraints are suspended. At scale, brute-forcing every override interval collapses under latency and cost budgets. The useful abstraction is to precompute optimal prefix/suffix behavior under each regime, then stitch them at boundaries. That turns a combinatorial policy-placement problem into a linear scan with clear invariants and predictable performance.

## 🔍 Problem Statement
You are given an integer array `prices` where `prices[i]` is the commodity price on day `i`. You may buy and sell any number of times, but can hold at most one unit, cannot buy and sell on the same day, and every buy must occur before its matching sell. Normally, after selling, you must wait one full day before buying again.

You must choose exactly one contiguous segment `[l, r]` as a promotion window. Inside that window only, the cooldown is disabled, so selling on day `i` and buying again on day `i+1` is allowed if both days lie within `[l, r]`. Outside the window, normal cooldown rules apply.

Return the maximum achievable profit.

- `1 <= prices.length <= 200000`
- `1 <= prices[i] <= 1000000000`

Examples:

- `prices = [1,3,2,5,4,7]` → `8`
- `prices = [5,4,3,2,1]` → `0`

The `O(n)` / `O(n log n)` requirement is the key constraint: trying all promotion windows is infeasible.

## 🪜 How to Solve This
1. Start with the baseline stock DP: with cooldown, each day has a small number of states — holding, free to buy, or in cooldown. That already gives an `O(n)` solution for the standard problem.

2. Now add the promotion window. The immediate trap is to think “try every `[l, r]` and rerun DP.” That is `O(n^2)` windows times `O(n)` work — dead on arrival.

3. Reframe the problem → the array has three regions:
   - prefix before `l`: normal cooldown,
   - middle `[l, r]`: no cooldown,
   - suffix after `r`: normal cooldown again.

4. That suggests decomposition. If we can precompute:
   - best profit for every prefix under normal cooldown,
   - best profit for every suffix under normal cooldown,
   - best tradable value for every no-cooldown middle segment,
   then we can combine them at boundaries.

5. The no-cooldown middle has a simpler structure: with unlimited transactions and no cooldown, optimal profit on a segment is just the sum of positive day-to-day increases.

6. The only subtlety is boundary state compatibility: whether we enter or leave the middle while holding or not. So the right solution is state-compressed DP on prefixes and suffixes, plus a linear accumulation for the promotion gain, then a scan over split points.

## 🧩 Algorithm Walkthrough
1. **Model the normal regime with state-compressed dynamic programming.**  
   Use the classic three states for each day:
   - `free[i]`: max profit by end of day `i`, not holding, allowed to buy next day.
   - `hold[i]`: max profit by end of day `i`, currently holding.
   - `cool[i]`: max profit by end of day `i`, not holding, but next day is blocked due to a sale today.  
   This is the correct abstraction because cooldown is a local state machine; only yesterday’s state matters.

2. **Compute prefix-optimal values under cooldown.**  
   Scan left to right and store, for each boundary `i`, the best achievable profit up to day `i` in each relevant state.  
   Invariant: prefix DP fully captures every legal trading history ending at or before `i`.

3. **Compute suffix-optimal values under cooldown.**  
   Run the symmetric DP right to left, so for each start boundary `i`, you know the best profit obtainable on `i..n-1` given whether you start free to buy or in a forced cooldown-like restriction.  
   Invariant: suffix DP summarizes all legal futures independent of the exact prefix path.

4. **Precompute no-cooldown segment profit.**  
   Inside the promotion window, the optimal strategy is the standard unlimited-transactions result: accumulate `max(0, prices[i] - prices[i-1])`.  
   This works because without cooldown and with one-unit holding, every rising edge can be harvested independently.

5. **Track best middle contribution for every boundary pair implicitly.**  
   Rather than enumerating all `[l, r]`, maintain a running best “enter promotion here” value while scanning the right endpoint. This is equivalent to a Kadane-style optimization over transformed boundary values.

6. **Combine prefix + middle + suffix.**  
   For each possible end of the promotion window, merge:
   - legal prefix state before entering the window,
   - profit captured inside the no-cooldown segment,
   - legal suffix state after leaving it.  
   Correctness comes from optimal substructure: once the entry/exit states are fixed, the three regions are independent.

7. **Take the global maximum.**  
   This yields an `O(n)` solution using **Dynamic Programming + State Compression + Prefix/Suffix decomposition**, which is the right pattern whenever one contiguous interval changes the transition rules.

## 📊 Worked Example
Take `prices = [1, 3, 2, 5, 4, 7]`.

Without promotion, cooldown DP yields profit `6`.

Inside a no-cooldown segment, profit is the sum of positive adjacent gains:

| Day pair | Delta | Gain used in promotion? |
|---|---:|---:|
| 1 → 3 | +2 | 2 |
| 3 → 2 | -1 | 0 |
| 2 → 5 | +3 | 3 |
| 5 → 4 | -1 | 0 |
| 4 → 7 | +3 | 3 |

If we choose promotion window `[1, 5]` in 0-based indexing over prices `[3,2,5,4,7]`, the middle contributes `3 + 3 = 6`, but the best full plan is to start one day earlier:

1. Buy at `1`, sell at `3` → `+2`
2. Buy at `2`, sell at `5` → `+3`
3. Buy at `4`, sell at `7` → `+3`

Total = `8`.

The prefix/suffix DP ensures this stitching is legal: the first sale happens before the cooldown-free region starts affecting re-entry, and all rapid rebuys occur inside the chosen window.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each DP pass over prefixes and suffixes is linear, and the promotion-window optimization is another linear scan with constant-time state updates. This scales comfortably to `10^6` elements in memory-resident workloads; at `10^9`, the issue is no longer algorithmic complexity but storage and streaming constraints.

### Space Complexity
`O(n)` if you materialize prefix and suffix state arrays for clean boundary composition. The dominant space is those DP buffers. It can be reduced toward `O(1)` auxiliary state with tighter fusion of passes, but at the cost of more brittle logic and harder correctness reasoning.

## 💡 Key Takeaways
- If a problem says “one contiguous segment changes the rules,” think prefix/suffix decomposition plus a scan over boundaries, not interval brute force.
- If trading constraints depend only on the previous day’s action, model them as a tiny state machine and compress the DP state aggressively.
- The promotion window only disables cooldown for actions fully inside it; boundary transitions still need normal-regime legality checks.
- Be careful with off-by-one semantics around “wait exactly one full day” — selling on day `i` blocks buying on day `i+1`, not day `i+2`.
- The transferable design insight is to isolate a local policy override as a composable subproblem, then stitch it to globally consistent prefix/suffix summaries.

## 🚀 Variations & Further Practice
- Allow **up to `k` promotion windows** instead of exactly one. The twist is interval budgeting: you now need DP over both day index and number of overrides used.
- Add a **transaction fee** alongside cooldown and one no-cooldown segment. The harder part is composing multiple orthogonal transition costs without exploding state count.
- Make the promotion window disable cooldown only for **at most `m` transactions** inside it. This turns the middle from a simple greedy sum into another constrained DP.