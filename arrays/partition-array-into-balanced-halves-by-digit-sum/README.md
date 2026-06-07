# Partition Array into Balanced Halves by Digit Sum

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Meet in the Middle, Dynamic Programming, Bit Manipulation

---

## 🗂 Problem Overview

Given an even-length integer array, partition it into two equal-sized halves minimizing the absolute difference between each half's total digit sum. The digit sum transform reduces each element to a scalar, making this a **balanced subset sum** problem — find a subset of exactly `n/2` elements whose summed digit values is as close as possible to half the total digit sum. The even-split cardinality constraint is what elevates this beyond a standard subset-sum.

---

## 🌍 Engineering Impact

This pattern appears anywhere you need to **balance load across two partitions with a derived weight function** rather than raw values. Distributed database sharding balances query cost (not row count); compiler register allocation splits live ranges by spill cost; A/B test assignment engines split cohorts by engagement score proxies. Without balanced partitioning, you get skewed shards, hot replicas, or statistically invalid experiment groups. Meet-in-the-Middle specifically matters when the partition space is too large for DP but too structured for greedy — a common regime in scheduling and bin-packing subsystems.

---

## 🔍 Problem Statement

**Input:** Integer array `nums` of even length `n` (`2 ≤ n ≤ 40`), where `1 ≤ nums[i] ≤ 10^5`.  
**Output:** Minimum absolute difference between the total digit sums of two groups, each containing exactly `n/2` elements.

**Constraints driving the algorithm:** `n ≤ 40` rules out O(2ⁿ) brute force (2⁴⁰ ≈ 10¹²) but is too small for polynomial DP to be the only option. This is the canonical Meet-in-the-Middle signal.

| Example | Input | Digit Sums | Output |
|---------|-------|------------|--------|
| 1 | `[14, 21, 35, 9]` | `[5, 3, 8, 9]` | `1` |
| 2 | `[10, 22, 33, 45, 50, 67]` | `[1, 4, 6, 9, 5, 13]` | `0` |

**Edge cases:** All digit sums equal (difference = 0); total digit sum is odd (minimum difference ≥ 1); all weight concentrated in one element.

---

## 🪜 How to Solve This

1. **Transform first** → digit sums are what matter, not raw values. Reduce `nums` to a `ds[]` array immediately. Now the problem is purely numeric.

2. **Recognize subset sum with cardinality constraint** → we need a subset of exactly `n/2` elements summing as close to `total/2` as possible. Standard 0/1 knapsack handles this in O(n · total_sum · n) — feasible, but Meet-in-the-Middle is more elegant given `n ≤ 40`.

3. **Notice n ≤ 40** → this is the canonical Meet-in-the-Middle bound. Split the array in half; enumerate all `2^20` subsets of each half. For each subset, record `(digit_sum, count_chosen)`.

4. **Match across halves** → for each subset of the left half choosing `k` elements with sum `s`, we need a right-half subset choosing exactly `n/2 - k` elements with sum as close to `total/2 - s` as possible. Sort right-half states by sum; binary search for the complement.

5. **The cardinality constraint is the hard part** → group right-half subsets by count chosen, sort each group by sum, then binary search within the correct group. This is the non-obvious implementation step that separates a working solution from a buggy one.

---

## 🧩 Algorithm Walkthrough

**Pattern: Meet in the Middle**

This is the right abstraction because the search space is 2ⁿ but splits cleanly into two independent halves of size 2^(n/2), each enumerable in ~10⁶ operations.

1. **Digit-sum transform:** Compute `ds[i]` for each element. Compute `total = sum(ds)`. Target for one group: `total / 2`.

2. **Split:** Divide `ds` into `left` (first `n/2` elements) and `right` (last `n/2` elements).

3. **Enumerate left subsets:** For each of the `2^(n/2)` subsets of `left`, record `(subset_sum, elements_chosen)` as a pair. Store in a list `left_states`.

4. **Enumerate right subsets:** Same for `right`. Group `right_states` by `elements_chosen` count into a map: `right_by_count[k]` = sorted list of sums for subsets of size `k`.

5. **Search for complement:** For each `(s, k)` in `left_states`, look up `right_by_count[n/2 - k]`. Binary search for the value closest to `total/2 - s`. Compute the resulting total group-1 sum as `s + best_right`, then `abs(2 * group1_sum - total)`.

6. **Track minimum:** Update global minimum across all valid pairings.

7. **Invariant maintained:** Every evaluated pairing represents a valid partition of exactly `n/2` elements into group 1, so the cardinality constraint is always satisfied.

---

## 📊 Worked Example

**Input:** `nums = [14, 21, 35, 9]` → `ds = [5, 3, 8, 9]`, `total = 25`, target ≈ `12`  
**Split:** `left = [5, 3]`, `right = [8, 9]`, need exactly 2 elements total in group 1.

| Left subset | sum | count | Right complement needed (count=2-k) | Right options (sum) | Best right | Group1 sum | \|diff\| |
|-------------|-----|-------|--------------------------------------|---------------------|------------|------------|---------|
| `{}` | 0 | 0 | count=2: {8,9}→17 | 17 | 17 | 17 | 9 |
| `{5}` | 5 | 1 | count=1: {8}→8, {9}→9 | closest to 7 → 8 | 8 | 13 | **1** |
| `{3}` | 3 | 1 | count=1: closest to 9 → 9 | 9 | 9 | 12 | 1 |
| `{5,3}` | 8 | 2 | count=0: {}→0 | 0 | 0 | 8 | 9 |

Minimum `|diff|` = **1**. ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(2^(n/2) · n/2)** — enumerating subsets of each half is O(2^(n/2)); the sort of right-half states per count bucket is O(2^(n/2) · log(2^(n/2))); binary search per left state is O(log(2^(n/2))). At `n=40` this is ~20M operations — comfortably tractable. Pure brute force at `n=40` would be 10¹² operations, completely infeasible.

### Space Complexity

**O(2^(n/2))** — dominated by storing right-half subset states (~1M entries at `n=40`). The count-grouped map holds the same data reorganized. No reduction is possible without sacrificing the binary search; you could stream left states to cut left-half storage to O(1) at no performance cost.

---

## 💡 Key Takeaways

- **Pattern signal #1:** Array length bounded at 40 with an exponential search space is the canonical Meet-in-the-Middle fingerprint — any time you see `n ≤ 40` with subset enumeration, reach for this pattern before DP.
- **Pattern signal #2:** "Partition into two equal-sized groups minimizing some aggregate difference" is balanced subset sum with a cardinality constraint — not a sorting or greedy problem, regardless of how it's phrased.
- **Gotcha #1:** The cardinality constraint (`exactly n/2` elements per group) is easy to drop. Forgetting to group right-half states by count and binary-searching only within the matching count bucket produces wrong answers that pass many test cases.
- **Gotcha #2:** When `total` is odd, the minimum achievable difference is at least 1 — don't short-circuit to 0 when `total % 2 == 0` without verifying a valid partition actually exists with that sum split.
- **Architectural insight:** The digit-sum transform is an instance of a **cost projection** — replacing raw values with derived weights before partitioning. In production systems (sharding, scheduling), always identify the correct cost metric before solving the partition problem; optimizing on the wrong metric (e.g., row count instead of query cost) produces balanced-looking but operationally skewed partitions.

---

## 🚀 Variations & Further Practice

- **Multi-way partition (k > 2 groups):** Extend to partitioning into `k` equal groups minimizing the max pairwise difference. Meet-in-the-Middle no longer applies directly; this becomes a multi-dimensional DP or ILP problem, and the state space grows as O(n^(k-1)) — the twist is managing exponential state growth with memoization over partial assignments.
- **Weighted cardinality constraint:** Each element carries both a digit-sum and a "slot cost"; groups must balance digit sums while each group's total slot cost stays within a budget. This couples two knapsack dimensions, requiring 2D DP or constrained Meet-in-the-Middle with two-key sorting and range queries instead of binary search.
- **Online / streaming variant:** Elements arrive in a stream and must be assigned to a group irrevocably on arrival, minimizing final imbalance. The offline Meet-in-the-Middle approach collapses entirely; this requires online algorithms (greedy with regret bounds or reservoir-sampling-based approaches), connecting to the broader literature on online load balancing.