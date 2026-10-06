# Maximum Gain from Swapping One Price Dip and Peak

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix/Suffix, Greedy

---

## 🗂 Problem Overview
Given an array `prices`, you may swap at most one pair of positions, then execute exactly one buy-before-sell transaction on the modified array. The goal is to maximize `prices[s] - prices[b]` with `b < s`, returning `0` if no profit exists. The difficulty is positional: you cannot reorder freely, only relocate two values once. That forces reasoning about how one swap can improve either the best prefix minimum before a sell point or the best suffix maximum after a buy point.

## 🌍 Engineering Impact
This pattern shows up anywhere one bounded mutation is allowed before an optimization pass. In trading simulators, it models correcting one delayed quote before computing best execution. In streaming pipelines, it resembles fixing one out-of-order event to recover a downstream max-delta metric. In ranking and scheduling systems, a single item promotion/demotion can materially change the best feasible pair under ordering constraints. At scale, brute-force “try every swap, then recompute” collapses under quadratic cost. Prefix/suffix summaries let you evaluate local mutations against global optima without reprocessing the full sequence per candidate.

## 🔍 Problem Statement
You are given an integer array `prices` where `prices[i]` is the asset price on day `i`. You may swap at most one pair of distinct indices `i != j`. After that optional swap, choose one buy day `b` and one later sell day `s` such that `b < s`. The profit is `prices[s] - prices[b]`. Return the maximum achievable profit, or `0` if every valid transaction is non-profitable.

Constraints:

- `2 <= prices.length <= 2 * 10^5`
- `0 <= prices[i] <= 10^9`

Examples:

- `prices = [8, 3, 6, 1, 9]` → `8`
- `prices = [10, 7, 4, 6, 2]` → `5`

The key constraint is the array size: `O(n^2)` swap enumeration is already too expensive, and `O(n^3)` recomputation is completely infeasible. The solution must exploit positional summaries, not free sorting.

## 🪜 How to Solve This
1. Start with the no-swap version → classic “best time to buy and sell stock”: maintain the minimum seen so far and maximize sell price minus that minimum.

2. Now add one swap → a swap can help in only two structural ways:
   - move a **smaller value earlier**, improving the buy side for later sells;
   - move a **larger value later**, improving the sell side for earlier buys.

3. That suggests prefix/suffix thinking → for every position, ask:
   - what is the smallest value available if I’m allowed to import one value from the suffix?
   - what is the largest value available if I’m allowed to import one value from the prefix?

4. Precompute the best suffix minimums and suffix maximums, plus prefix minimums and prefix maximums, with indices.

5. Evaluate each day as a potential sell using the best buy achievable before it after at most one swap, and symmetrically each day as a potential buy using the best sell achievable after it.

6. The core insight: one swap only changes two positions, so the optimal transaction can be analyzed through boundary summaries rather than explicit simulation of every modified array.

## 🧩 Algorithm Walkthrough
1. **Compute baseline profit with a Prefix Minimum scan.**  
   Pattern: **Greedy + Prefix Summary**.  
   Scan left to right, maintain the minimum price seen so far, and update the best profit. This gives the no-swap answer and establishes the invariant: at day `i`, we know the best buy available in `[0..i-1]`.

2. **Build prefix extrema arrays.**  
   For each index `i`, store:
   - minimum value and its earliest index in `[0..i]`
   - maximum value and its index in `[0..i]`  
   This lets us answer: “what is the best buy or sell candidate strictly before a boundary?”

3. **Build suffix extrema arrays.**  
   For each index `i`, store:
   - minimum value and its index in `[i..n-1]`
   - maximum value and its latest index in `[i..n-1]`  
   This supports the symmetric question on the right side of a boundary.

4. **Evaluate improving the buy side.**  
   For each sell day `s`, consider swapping the value at some earlier buy position with the minimum value from the suffix segment before `s` can still remain earlier than `s` after the swap. Operationally, the best achievable buy before `s` after one swap is the minimum of:
   - the original prefix minimum before `s`
   - a suffix minimum from some position `< s` moved into an earlier slot.  
   The invariant is that the buy day must still be strictly before `s` after the swap.

5. **Evaluate improving the sell side.**  
   Symmetrically, for each buy day `b`, consider whether a larger value from the prefix can be moved to a later position, or whether a suffix maximum already gives the best sell. This captures swaps that improve the right endpoint rather than the left.

6. **Take the global maximum over baseline and both one-swap improvements.**  
   The abstraction is **Prefix/Suffix extrema under one bounded mutation**. It is the right model because the swap budget is one, and the objective depends only on one left endpoint and one right endpoint under order constraints.

## 📊 Worked Example
Take `prices = [10, 7, 4, 6, 2]`.

| Day | Price | Prefix Min | Baseline Best | Suffix Max |
|---|---:|---:|---:|---:|
| 0 | 10 | 10 | 0 | 10 |
| 1 | 7  | 7  | 0 | 7  |
| 2 | 4  | 4  | 0 | 6  |
| 3 | 6  | 4  | 2 | 6  |
| 4 | 2  | 2  | 2 | 2  |

Baseline profit is `2` from buy `4` on day `2`, sell `6` on day `3`.

Now consider one swap. The suffix minimum is `2` at day `4`. Swapping day `1` (`7`) with day `4` (`2`) yields:

`[10, 2, 4, 6, 7]`

Now:
- best buy before later days is `2` at day `1`
- best sell after that is `7` at day `4`

Profit becomes `7 - 2 = 5`, which beats the baseline. The swap improved both feasibility and value without violating `buy < sell`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each prefix and suffix summary is built in a linear scan, and each candidate buy/sell improvement is evaluated in another linear pass. There is no nested swap enumeration. At `10^6` elements this remains practical; at `10^9`, even linear scans become bandwidth-bound and require distributed or external-memory treatment.

### Space Complexity
`O(n)`. The space is owned by prefix/suffix extrema arrays storing values and sometimes indices. It can be reduced toward `O(1)` only if you derive a tighter one-pass formulation, but that usually increases proof complexity and implementation risk.

## 💡 Key Takeaways
- If a problem allows only one local mutation but asks for a global optimum, think prefix/suffix summaries before considering brute-force simulation.
- If order matters after the mutation, free sorting is the wrong mental model; preserve positional feasibility throughout the design.
- The buy day and sell day are chosen **after** the swap on the modified array; validating against the original positions is a common logic bug.
- Be explicit about strict inequality `b < s`; extrema arrays must respect boundaries and not accidentally reuse the same position on both sides.
- In production systems, bounded-repair optimization often reduces to “precompute global summaries, then score each allowed mutation in O(1),” which is the scalable alternative to recomputation.

## 🚀 Variations & Further Practice
- Allow up to `k` swaps before one transaction. The twist is that local endpoint improvement becomes a combinatorial reordering problem and typically needs DP or state compression.
- Allow one swap and up to `k` buy/sell transactions. The harder part is that a swap changes the state space for every interval, so simple stock DP no longer composes cleanly.
- Process prices as a stream with late-arriving corrections. The challenge is maintaining prefix/suffix-like summaries incrementally under bounded out-of-order updates.