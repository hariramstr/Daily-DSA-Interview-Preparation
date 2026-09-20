# Maximum Net Gain from One Interior Refund Block

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Kadane's Algorithm, Prefix Sum

---

## 🗂 Problem Overview
Given an integer array `transactions` and a fixed `baseGain`, choose exactly one contiguous subarray that lies strictly inside the array—its start cannot be `0`, and its end cannot be `n - 1`. That chosen block is refunded, so its sum is flipped from `+S` to `-S`, changing the final result to `baseGain + totalSum - 2*S`. To maximize the result, you must find the minimum-sum interior subarray efficiently under `n` up to `200,000`.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must apply one reversible correction over a constrained window: financial ledgers with locked opening/closing entries, streaming pipelines with immutable boundary checkpoints, audit rollback in event-sourced systems, and ad-tech attribution windows where only interior events are mutable. At scale, brute-force enumeration of all candidate windows collapses under quadratic cost and poor cache behavior. Recognizing that “maximize final score after flipping one segment” reduces to “find the minimum valid subarray” enables linear-time processing, predictable latency, and straightforward integration into batch or streaming analytics paths.

## 🔍 Problem Statement
You are given:

- `transactions`, an integer array where positive values are profits and negative values are losses
- `baseGain`, a fixed integer always included in the final result

You must choose **exactly one** contiguous refunded block such that:

- the block has length at least `1`
- it is an **interior** block: it cannot start at index `0`
- it cannot end at index `n - 1`

If the chosen block has sum `S`, the final result is:

`baseGain + sum(transactions) - 2 * S`

Return the maximum possible final result.

Constraints:

- `3 <= transactions.length <= 200000`
- `-10^9 <= transactions[i] <= 10^9`
- `-10^9 <= baseGain <= 10^9`
- answer fits in signed 64-bit integer

Examples:

- `transactions = [4, -7, 3, -2, 5], baseGain = 10` → `27`
- `transactions = [8, 2, 6], baseGain = -5` → `7`

The decisive constraint is array size: checking all interior subarrays is `O(n^2)`, which is too slow.

## 🪜 How to Solve This
1. Start from the formula:  
   `baseGain + totalSum - 2 * refundedSum`.

2. `baseGain` and `totalSum` are fixed no matter which block you choose.  
   So maximizing the final result means minimizing `refundedSum`.

3. That reframes the problem completely:  
   find the **minimum-sum contiguous subarray**, but only among subarrays fully inside indices `1..n-2`.

4. Once you see “minimum subarray sum on a contiguous range,” Kadane’s algorithm should come to mind. Standard Kadane finds a maximum subarray; the same recurrence works for minimum by replacing `max` with `min`.

5. The interior constraint is not a post-processing detail. It changes the search domain.  
   Instead of scanning the whole array and filtering later, run Kadane only on `transactions[1..n-2]`.

6. After finding `minInteriorSum`, plug it back into the formula:  
   `answer = baseGain + totalSum - 2 * minInteriorSum`.

7. Use 64-bit arithmetic throughout. With `200,000` elements and values near `10^9`, intermediate sums exceed 32-bit range easily.

## 🧩 Algorithm Walkthrough
1. **Compute the total array sum.**  
   Let `totalSum = sum(transactions)`. This is part of every candidate result, so we compute it once.  
   Invariant: after this pass, `totalSum` is exact and independent of the refunded block choice.

2. **Restrict the search to the interior segment.**  
   Valid refunded blocks must lie entirely within indices `1` through `n - 2`.  
   This converts the problem into a standard contiguous-subarray optimization over a smaller range.  
   Invariant: every subarray considered from now on is valid by construction.

3. **Run minimum-subarray Kadane on `transactions[1..n-2]`.**  
   Pattern: **Kadane’s Algorithm**, adapted for minimum sum.  
   Maintain:
   - `currentMin`: minimum-sum subarray ending at the current index
   - `bestMin`: minimum-sum subarray seen anywhere so far

   Recurrence:
   - `currentMin = min(transactions[i], currentMin + transactions[i])`
   - `bestMin = min(bestMin, currentMin)`

   Why correct: any minimum-sum subarray ending at `i` either starts fresh at `i` or extends the minimum-sum subarray ending at `i-1`.

4. **Translate the minimum refunded sum into the maximum final gain.**  
   Since refunding subtracts the block twice relative to the original total, compute:  
   `answer = baseGain + totalSum - 2 * bestMin`.

5. **Return the result as 64-bit.**  
   Invariant at termination: `bestMin` is the minimum valid interior block sum, so the derived final value is globally optimal.

## 📊 Worked Example
Take `transactions = [4, -7, 3, -2, 5]`, `baseGain = 10`.

- `totalSum = 4 + (-7) + 3 + (-2) + 5 = 3`
- Interior range is indices `1..3` → `[-7, 3, -2]`

| i | value | currentMin = min(value, currentMin + value) | bestMin |
|---|------:|---------------------------------------------:|--------:|
| 1 |   -7  | -7                                           | -7      |
| 2 |    3  | min(3, -7 + 3) = -4                          | -7      |
| 3 |   -2  | min(-2, -4 + -2) = -6                        | -7      |

The minimum interior subarray sum is `-7`, achieved by `[-7]`.

Now compute the final result:

`answer = baseGain + totalSum - 2 * bestMin`  
`= 10 + 3 - 2 * (-7)`  
`= 13 + 14`  
`= 27`

So the optimal refunded block is `[-7]`, and the maximum final result is `27`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass computes `totalSum`, and one pass over indices `1..n-2` runs minimum Kadane. The dominant work is linear scanning with constant-time updates per element. At `10^6` elements this is routine; at `10^9`, even linear time becomes operationally expensive, but still asymptotically optimal for exact evaluation.

### Space Complexity
`O(1)`. The algorithm stores only running sums and minima; no auxiliary arrays are required. Space is owned entirely by a handful of scalar variables. You cannot reduce asymptotically below constant space without changing the execution model, though streaming input would still support the same bound.

## 💡 Key Takeaways
- If the objective looks like “original total plus/minus twice a chosen segment,” rewrite it so the optimization becomes a min- or max-subarray problem.
- When the problem says “exactly one contiguous block” and `n` is large, Kadane or prefix-sum reasoning should be your first pattern check.
- The refunded block must be strictly interior, so the Kadane scan must run on `1..n-2`, not the full array.
- Use 64-bit arithmetic for `totalSum`, running minima, and the final expression; 32-bit overflow is easy here.
- In production systems, boundary immutability often turns a generic optimization into a constrained one; encode those constraints directly into the search space instead of filtering invalid candidates afterward.

## 🚀 Variations & Further Practice
- Allow the refunded block to be optional instead of mandatory. The twist is comparing “no refund” against the best interior refund, which changes the minimum-subarray logic slightly.
- Refund up to `k` disjoint interior blocks. The harder part is moving from single-segment Kadane to DP over multiple non-overlapping subarrays.
- Support online updates to `transactions` with repeated queries for the best refund. The conceptual jump is from one-pass scanning to segment trees storing total, min-prefix, min-suffix, and min-subarray metadata.