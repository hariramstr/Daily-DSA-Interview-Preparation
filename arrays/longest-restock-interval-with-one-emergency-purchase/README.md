# Longest Restock Interval with One Emergency Purchase

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Binary Search

---

## 🗂 Problem Overview
Given an integer array `changes`, find the longest contiguous subarray whose running balances can be made all non-negative after applying at most one unlimited positive adjustment to a single element inside that subarray. The adjustment affects only prefixes ending at or after that position, so it cannot repair earlier deficits. The challenge is to evaluate this condition efficiently for up to `2 * 10^5` elements, where brute-force subarray checks are far too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere a local intervention can repair only future state, not past state: inventory correction in supply systems, backfill events in streaming pipelines, burst credits in rate-limiters, and compensating transactions in ledgers. At scale, the key question is not whether a window ever dips negative, but whether one bounded intervention point can restore feasibility for the suffix of that window. Without the right prefix-sum formulation, teams fall into quadratic window validation, which collapses under large event streams. With it, you get predictable latency, composable invariants, and a clean path to binary-searching the optimal interval length.

## 🔍 Problem Statement
You are given an integer array `changes` where positive values add inventory and negative values remove it. For any contiguous interval `changes[l..r]`, consider its running balances:

`changes[l]`, `changes[l] + changes[l+1]`, ..., `sum(changes[l..r])`.

The interval is stable if, after optionally increasing exactly one element inside the interval by any non-negative amount, every running balance becomes at least `0`.

Return the maximum stable interval length.

Constraints:

- `1 <= changes.length <= 2 * 10^5`
- `-10^9 <= changes[i] <= 10^9`

Examples:

- `changes = [4, -6, 3, 2]` → `4`
- `changes = [-3, 5, -2, 1, -4]` → `4`

Critical observation: one purchase at index `k` can only affect prefixes that include `k`. If a prefix before `k` is already negative, that interval cannot be repaired by choosing `k` later. That directional constraint is what forces a prefix-sum-based algorithm rather than a generic sliding window.

## 🪜 How to Solve This
1. Read the repair rule carefully → one modification only helps prefixes at or after the modified day.
2. Rephrase a subarray `l..r` using global prefix sums `P` → every local running balance is `P[i] - P[l-1]`.
3. Ask when one intervention is enough:
   - Prefixes before the chosen repair point must already be non-negative.
   - Prefixes from the repair point onward may dip, because one large enough purchase can lift all of them together.
4. That means a valid interval has a split point `k` such that:
   - `min(P[l..k-1]) >= P[l-1]`
   - no condition is needed after `k`, because an unbounded increase at `k` can raise every later prefix by the same amount.
5. So the real question becomes: does `l..r` contain some index `k` where all earlier local prefixes are non-negative?
6. Equivalently, once the running balance first goes negative, you must repair exactly there or earlier — but earlier repair cannot fix that first negative if it occurs before the repair point.
7. This reduces to finding the longest subarray whose prefix-minimum sequence has at most one violation region, which can be checked efficiently with prefix minima structure and binary search on answer length.

## 🧩 Algorithm Walkthrough
1. **Build global prefix sums.**  
   Let `P[0] = 0`, `P[i+1] = P[i] + changes[i]`. For subarray `l..r`, its local running balances are `P[l+1]-P[l]`, `P[l+2]-P[l]`, ..., `P[r+1]-P[l]`. This converts every subarray check into comparisons against one baseline `P[l]`.

2. **Characterize repairability.**  
   Suppose we place the emergency purchase at position `k` inside `l..r`. Every local prefix ending before `k` is unchanged, so all of them must already be `>= 0`. Every local prefix ending at or after `k` gets the same additive boost, and because the boost is unbounded, we can always choose it large enough. Therefore, `l..r` is valid iff there exists some `k` such that the minimum prefix sum over `l..k-1` is at least `P[l]`.

3. **Reduce to earliest failure.**  
   Scan local prefixes from left to right. If they never go negative, the interval is already stable. If they first go negative at offset `t`, then the only possible repair point is at or before that failing element, and all earlier prefixes must have been non-negative. So the interval is valid iff its first negative local prefix exists at some position that can serve as the repair point — which is always true as long as the interval does not start with a negative prefix before any repairable element. Concretely, the first element can always be repaired, but once a negative prefix occurs before the chosen repair index, the interval is impossible.

4. **Use prefix minima + binary search on length.**  
   For a fixed length `L`, check whether some window of length `L` is stable. This can be done by querying the earliest position in the window where the local prefix drops below zero. Range minimum queries over prefix sums support this efficiently. Binary search over `L` because feasibility is monotone: if a length `L` works, every shorter length also works.

5. **Pattern choice: Prefix Sum + Binary Search.**  
   This is the right abstraction because the constraint is about all prefixes of a subarray, not just its total sum. Prefix sums expose those balances; binary search exploits monotonicity of feasible length.

## 📊 Worked Example
Take `changes = [4, -6, 3, 2]`.

Prefix sums: `P = [0, 4, -2, 1, 3]`

Check window `l = 0, r = 3`:

| Local prefix end | Value | Running balance |
|---|---:|---:|
| 0 | `P[1] - P[0]` | `4` |
| 1 | `P[2] - P[0]` | `-2` |
| 2 | `P[3] - P[0]` | `1` |
| 3 | `P[4] - P[0]` | `3` |

Trace:

1. First running balance is `4` → already non-negative.
2. Second running balance is `-2` → first failure occurs here.
3. Repair at index `1` (value `-6`).
4. Add `2` to that day.
5. Every prefix ending at or after index `1` increases by `2`.

Adjusted running balances become `[4, 0, 3, 5]`, all non-negative.

So length `4` is feasible, and since this is the full array, the answer is `4`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log n)` with binary search over answer length and `O(n)` or `O(n log n)` feasibility checks depending on the range-minimum implementation. The dominant cost is repeated window validation on prefix sums. This is practical for `10^6`-scale input, but anything quadratic is dead on arrival; at `10^9`, only streaming or distributed approximations are realistic.

### Space Complexity
`O(n)` for prefix sums and any auxiliary structure used for range minimum queries or window validation. The prefix array owns most of the memory. You can trade some space for slower checks, but reducing below linear space usually costs enough extra time to lose the benefit.

## 💡 Key Takeaways
- If a problem asks whether all prefixes of a subarray satisfy a bound, think prefix sums immediately; totals alone are insufficient.
- If one operation can repair only future state, model the exact boundary where past violations become irreversible.
- Off-by-one errors usually come from mixing array indices with prefix-sum indices; keep `P[i]` as sum of the first `i` elements.
- The repair affects prefixes ending at or after the chosen index, not the entire subarray uniformly; that directional effect is the core trap.
- In production systems, this is the same design move as isolating what a compensating action can and cannot retroactively fix before choosing a scalable validation strategy.

## 🚀 Variations & Further Practice
- Allow up to `k` emergency purchases instead of one. The twist is that feasibility is no longer a single split point; you need DP, greedy segmentation, or a more complex monotone predicate.
- Bound the purchase amount by `B`. Now suffix repair is no longer automatic, so you must track how deep the minimum suffix deficit goes after the repair point.
- Make the interval circular instead of contiguous on a line. The conceptual jump is handling wraparound windows while preserving prefix-based reasoning.