# Minimum Time Gap to Place K Exam Sessions

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Sorting

---

## 🗂 Problem Overview
Given a set of distinct reserved classroom start times and an integer `k`, choose exactly `k` of those times so that the minimum gap between consecutive chosen times is as large as possible. Return that maximum achievable gap. The challenge is not selecting any valid subset, but optimizing the smallest pairwise spacing under large input constraints, which rules out combinatorial search and pushes toward a monotonic decision procedure plus binary search.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize minimum separation under fixed candidate positions: scheduling maintenance windows, placing replicas across failure domains, spacing ads in ranked feeds, allocating batch jobs across time slots, or selecting sampling points in telemetry pipelines. At scale, brute force collapses because the search space is exponential while the feasibility check is cheap and monotonic. Recognizing that structure enables a clean architecture: preprocess once, then run a fast decision function repeatedly. That trade-off is common in placement engines, capacity planners, and admission-control systems where “best feasible threshold” matters more than exact arrangement enumeration.

## 🔍 Problem Statement
You are given an integer array `times` of distinct reserved classroom start times and an integer `k`, where `2 <= k <= times.length <= 100000` and `0 <= times[i] <= 1000000000`. You must choose exactly `k` distinct times such that the difference between every pair of consecutive chosen times is at least `d`, and return the largest possible `d`.

The key constraint is input size: `10^5` elements makes subset enumeration impossible, so the solution must be near `O(n log M)` where `M` is the answer range.

Examples:

- `times = [30, 0, 90, 60, 120], k = 3` → `60`  
  Sorted: `[0, 30, 60, 90, 120]`, choose `0, 60, 120`.

- `times = [5, 18, 1, 9, 14], k = 4` → `4`  
  Sorted: `[1, 5, 9, 14, 18]`, choose `1, 5, 9, 14`.

Edge cases are mostly about bounds: small `k`, dense times, or very large time values.

## 🪜 How to Solve This
1. Read the objective carefully → we are maximizing a minimum gap, not minimizing total distance or constructing all subsets.

2. Distinct times with ordering-sensitive gaps → sort first. Once sorted, any valid placement can be reasoned about left to right.

3. Ask the standard optimization question: if I guess a gap `d`, can I check whether it is feasible? Yes.

4. For a fixed `d`, greedily place the first session at the earliest time, then always place the next session at the earliest time at least `d` minutes later. This is optimal for feasibility because taking an earlier valid slot leaves maximal room for the remaining sessions.

5. That gives a yes/no predicate: “Can I place at least `k` sessions with gap `d`?”

6. Notice the monotonicity: if gap `d` is feasible, every smaller gap is also feasible; if `d` is infeasible, every larger gap is infeasible.

7. Monotonic predicate over an integer answer space → binary search the maximum feasible `d`.

This is the classic “binary search on answer + greedy feasibility check” pattern.

## 🧩 Algorithm Walkthrough
1. **Sort `times` ascending.**  
   This converts the problem into ordered placement on a line. After sorting, only consecutive distances matter for a greedy feasibility pass. Invariant: all future decisions are made in chronological order.

2. **Define the search space for the answer.**  
   The minimum gap `d` lies between `0` and `times[n - 1] - times[0]`. We binary search this integer range. Invariant: the true answer always remains inside the current `[lo, hi]`.

3. **Implement a greedy feasibility check `canPlace(d)`.**  
   Place the first session at `times[0]`, then scan left to right and place another session whenever `times[i] - lastPlaced >= d`. Count placements until either `k` sessions are placed or the array ends.  
   Why correct: choosing the earliest valid slot never hurts future feasibility; any later choice only reduces remaining space.

4. **Use the monotonic property.**  
   If `canPlace(d)` is true, then `canPlace(d - 1)` must also be true. If `canPlace(d)` is false, then `canPlace(d + 1)` must also be false. This is exactly the abstraction needed for **Binary Search on Answer**.

5. **Bias binary search upward to find the maximum feasible value.**  
   Use `mid = lo + (hi - lo + 1) / 2`. If feasible, set `lo = mid`; otherwise set `hi = mid - 1`.  
   Invariant: `lo` is always feasible, `hi` is always an upper bound, and the interval shrinks without infinite loops.

6. **Return `lo`.**  
   At termination, `lo == hi`, and by construction it is the largest feasible minimum gap.

## 📊 Worked Example
Example: `times = [30, 0, 90, 60, 120], k = 3`

Sorted times: `[0, 30, 60, 90, 120]`  
Search range: `lo = 0`, `hi = 120`

| Step | lo | hi  | mid | Feasible? | Greedy placement |
|------|----|-----|-----|-----------|------------------|
| 1 | 0 | 120 | 60 | Yes | `0, 60, 120` |
| 2 | 60 | 120 | 90 | No | `0, 90` only |
| 3 | 60 | 89 | 75 | No | `0, 90` only |
| 4 | 60 | 74 | 67 | No | `0, 90` only |
| 5 | 60 | 66 | 63 | No | `0, 90` only |
| 6 | 60 | 62 | 61 | No | `0, 90` only |

Now `lo = hi = 60`, so the answer is `60`.

The important observation is that feasibility flips exactly once: all gaps up to `60` work, all larger gaps fail.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)`. Each binary-search iteration runs a linear greedy scan in `O(n)`, and there are `O(log R)` iterations where `R = max(times) - min(times)`. Total: `O(n log n + n log R)`. This scales comfortably to `10^5` elements; at `10^6`, it is still practical, while quadratic approaches are not.

### Space Complexity
If sorting in place, auxiliary space is `O(1)` beyond the sort implementation’s internal requirements; otherwise `O(n)` if a copied array is used. The greedy check itself is constant space. You can reduce memory by sorting in place, at the cost of mutating input.

## 💡 Key Takeaways
- If a problem asks for the **largest minimum** value and a guessed answer can be checked with a yes/no pass, look for binary search on the answer.
- If candidate positions are one-dimensional and ordered, feasibility often collapses to a greedy left-to-right placement strategy.
- Use an **upper-mid** binary search (`(lo + hi + 1) / 2` pattern) when searching for the maximum feasible value; otherwise you can stall.
- The greedy check should compare against the **last chosen time**, not the previous array element; confusing those breaks correctness.
- In production systems, this pattern is valuable because it separates optimization from feasibility: a cheap monotonic validator can power scalable threshold tuning without enumerating placements.

## 🚀 Variations & Further Practice
- Add a requirement to return the actual chosen `k` times, not just the maximum gap. Same feasibility logic, but now you must preserve reconstruction details cleanly.
- Allow some reserved times to have weights or costs, and maximize minimum gap under a budget. The monotonic check becomes more complex and may no longer be purely greedy.
- Extend from a 1D timeline to 2D placement or graph distances. The “sort + greedy” structure disappears, forcing different feasibility machinery or approximation strategies.