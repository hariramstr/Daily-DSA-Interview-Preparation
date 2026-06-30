# Maximum Median Quality After Budgeted Increments

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Binary Search, Greedy

---

## 🗂 Problem Overview
Given an integer array `quality` and a total increment budget, maximize the array’s median after applying at most `budget` `+1` operations across any elements. The median is the element at index `n / 2` after sorting, using 0-based indexing. The challenge is that increasing one candidate median value often requires raising other elements in the upper half as well, and the input size rules out any per-operation or brute-force simulation.

## 🌍 Engineering Impact
This pattern shows up anywhere a percentile-like threshold must be raised under a constrained improvement budget: search ranking floors, SLO uplift planning across service shards, warehouse inventory quality normalization, and distributed rate-limiter token balancing. At scale, the wrong approach burns cycles simulating local changes that do not affect the global order statistic. The right abstraction treats the target threshold as a monotonic feasibility problem: “can we make the median at least `x`?” That enables predictable `O(n log V)` behavior, clean capacity planning, and implementations that remain stable under six-figure array sizes and trillion-scale budgets.

## 🔍 Problem Statement
You are given an integer array `quality` where `quality[i]` is the quality score of the `i`-th part, and an integer `budget` representing how many total `+1` operations may be applied. In one operation, any single element may be increased by `1`. Return the maximum possible median after at most `budget` operations.

Sort only for evaluation. For an array of length `n`, the median is the element at index `n / 2` in sorted order. Constraints are large: `1 <= n <= 200000`, `1 <= quality[i] <= 1e9`, `0 <= budget <= 1e12`. That immediately eliminates brute force.

Examples:

- `quality = [1, 3, 5], budget = 4` → `7`
- `quality = [2, 2, 8, 9, 9], budget = 6` → `10`

Edge cases include `budget = 0`, duplicate-heavy arrays, and even-length arrays where the median is the upper-middle element.

## 🪜 How to Solve This
1. Sort the array → the median position becomes fixed at `mid = n / 2`. Anything left of `mid` is irrelevant for increasing the median, because those elements can stay smaller without affecting the target index.

2. Ask the right question → not “where should I spend each increment?” but “can I make the median at least `x`?” That reframes the problem as a monotonic feasibility check.

3. For a target `x`, only the suffix `quality[mid...n-1]` matters. Every value in that suffix that is below `x` must be raised to `x`; larger values need nothing.

4. The required cost is `sum(max(0, x - quality[i]))` over the suffix. If that cost is within `budget`, then `x` is achievable.

5. Feasibility is monotonic: if `x` works, every smaller value works. That is the signal for binary search on the answer.

6. Search between the current median and `currentMedian + budget`. The upper bound is safe because one increment can raise the median by at most one in aggregate.

This is the cleanest path from problem statement to scalable solution.

## 🧩 Algorithm Walkthrough
1. **Sort the array.**  
   This is the enabling step for all order-statistic reasoning. After sorting, the median index `mid = n / 2` is fixed, and any attempt to increase the median only needs to consider the suffix from `mid` onward. Invariant: the sorted order defines exactly which elements can block a higher median.

2. **Define a feasibility function `canReach(target)`.**  
   For each index `i` from `mid` to `n - 1`, add `target - quality[i]` to the cost only if `quality[i] < target`. This computes the minimum budget needed to ensure every blocking element in the upper half is at least `target`. Invariant: after paying this cost, the sorted element at index `mid` is guaranteed to be at least `target`.

3. **Use Binary Search on the Answer.**  
   This is the core pattern: **Binary Search on Monotonic Feasibility**. If a target median is achievable, all smaller targets are also achievable. That monotonicity makes binary search the right abstraction, not a heuristic.

4. **Set bounds carefully.**  
   Lower bound is the current median. Upper bound can be `quality[mid] + budget + 1` as an exclusive bound, or `quality[mid] + budget` as inclusive. This avoids overflow-prone guesswork and keeps the search space tight.

5. **Iterate until convergence.**  
   On each midpoint, run `canReach(midValue)`. If feasible, move right to try a larger median; otherwise move left. Invariant: the search interval always contains the optimal answer.

6. **Return the largest feasible value.**  
   Correctness follows from monotonicity plus exact cost computation over the suffix. No local greedy allocation is needed once feasibility is expressed correctly.

## 📊 Worked Example
Example: `quality = [2, 2, 8, 9, 9]`, `budget = 6`

Sorted array is already `[2, 2, 8, 9, 9]`, so `mid = 2`, current median = `8`.

| Target | Needed on suffix `[8,9,9]` | Total Cost | Feasible? |
|---|---:|---:|---|
| 10 | `(10-8) + (10-9) + (10-9)` | 4 | Yes |
| 11 | `3 + 2 + 2` | 7 | No |

Binary search narrows between feasible `10` and infeasible `11`, so answer is `10`.

Trace:
1. Lower bound = `8`, upper bound ≈ `14`.
2. Test `11` → cost `7` > budget, reject.
3. Test `9` → cost `1`, accept.
4. Test `10` → cost `4`, accept.
5. Search converges on largest feasible target: `10`.

The key observation is that raising only the current median is insufficient once larger targets are blocked by nearby values in the upper half.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n + (n/2) log budget)` in the straightforward implementation: sorting dominates once, and each binary-search step scans the upper half to compute feasibility. In practice this is written as `O(n log n + n log budget)`. At `10^6` elements it is still tractable; at `10^9`, neither sorting nor full scans are realistic without distribution or approximation.

### Space Complexity
`O(1)` auxiliary space beyond the sort, or `O(log n)` depending on the language’s sorting implementation. The array itself owns the dominant memory. You can reduce copies by sorting in place; the trade-off is mutating input, which may be unacceptable in some codebases.

## 💡 Key Takeaways
- If the problem asks for a maximum threshold under a limited budget and feasibility becomes easier for smaller thresholds, think binary search on the answer.
- If only an order statistic matters after sorting, inspect which side of the array can actually influence that statistic; here, the left half is irrelevant.
- For even `n`, the median is the element at index `n / 2`, not the average of two middle values; using the wrong definition breaks the entire solution.
- Use `long`/`int64` for both `budget` and accumulated cost; the constraints make 32-bit overflow guaranteed in realistic cases.
- The transferable design insight is to replace incremental simulation with a monotonic feasibility model, then search the decision space instead of the state space.

## 🚀 Variations & Further Practice
- Allow both increments and decrements with different per-unit costs, then maximize the median under a total cost budget. The twist is that feasibility is still monotonic, but cost modeling becomes asymmetric.
- Support online updates to `quality` and repeated “maximum median under budget” queries. The harder part is replacing full rescans with Fenwick trees, segment trees, or order-statistic structures.
- Maximize the `k`-th percentile instead of the median. The abstraction is the same, but the affected suffix size changes and exposes whether the implementation truly generalizes beyond one fixed order statistic.