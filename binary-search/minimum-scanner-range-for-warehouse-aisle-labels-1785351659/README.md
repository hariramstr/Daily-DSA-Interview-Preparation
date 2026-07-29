# Minimum Scanner Range for Warehouse Aisle Labels

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given sorted aisle-label positions and `k` scanners, find the smallest integer range `R` so all labels are covered. A scanner placed at any real position covers every label within distance `R`, i.e. an interval of length `2R`. The output is the minimum feasible integer `R`. The problem is non-trivial because `labels.length` can reach `100000`, so brute-forcing placements or trying every grouping strategy is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere you must minimize a uniform service radius under a fixed resource budget: edge cache placement, Wi-Fi or sensor coverage, CDN POP reachability, robotics docking zones, and warehouse automation. At scale, the wrong approach degenerates into combinatorial search over placements or partitions. The binary-search-on-answer pattern works because feasibility is monotonic: if radius `R` works, any larger radius works. That enables predictable performance, bounded latency, and straightforward capacity planning. The greedy feasibility pass also mirrors production admission checks where you need a fast “can current resources cover this workload?” decision.

## 🔍 Problem Statement
You are given a sorted integer array `labels`, where `labels[i]` is the position of the `i`-th aisle label on a straight corridor, and an integer `k` representing the number of available scanners. Each scanner may be placed at any real-valued position and covers all labels within distance `R`, producing an interval `[x - R, x + R]`. All scanners share the same range.

Return the minimum integer `R` such that all labels can be covered using at most `k` scanners.

Constraints:

- `1 <= labels.length <= 100000`
- `0 <= labels[i] <= 1000000000`
- `labels` is sorted in non-decreasing order
- `1 <= k <= labels.length`

Examples:

- `labels = [1, 2, 8, 12, 17], k = 2` → `4`
- `labels = [0, 5, 6, 7, 20], k = 3` → `1`

The key constraint is input size: any approach worse than roughly `O(n log M)` is risky, where `M` is the coordinate range.

## 🪜 How to Solve This
1. Read the problem → we are not asked for scanner positions directly; we are asked for the minimum feasible range. That is a strong signal for binary search on the answer.

2. Ask whether feasibility is monotonic → if range `R` can cover all labels with `k` scanners, then any `R' > R` also works. Once true, always true. That gives a sorted search space over possible answers.

3. Reduce the problem to a decision check → for a fixed `R`, can we cover all labels using at most `k` scanners?

4. Notice each scanner covers a continuous interval of width `2R` → to maximize coverage, start from the leftmost uncovered label and place a scanner as far right as possible while still covering it. That scanner then covers every label up to `leftmost + 2R`.

5. Repeat greedily → each scanner consumes the largest possible prefix of remaining labels. If this process needs `<= k` scanners, `R` is feasible.

6. Binary search the smallest feasible `R` between `0` and `labels[n-1] - labels[0]`.

This is the standard combination: binary search over a monotonic answer space, greedy for the feasibility predicate.

## 🧩 Algorithm Walkthrough
1. **Define the search space.**  
   The minimum possible range is `0`. A safe upper bound is `labels[n-1] - labels[0]`, which is enough for one scanner to cover everything if `k >= 1`. We binary search this integer interval.

2. **Build a feasibility function `canCover(R)`.**  
   This is the core pattern: **Binary Search on Answer + Greedy Covering**. For a candidate range `R`, scan labels left to right. When you encounter the first uncovered label at position `p`, spend one scanner and conceptually place it at `p + R`. That covers the interval `[p, p + 2R]`.

3. **Greedily skip all labels inside that interval.**  
   Advance the index while `labels[i] <= p + 2R`. This is correct because any scanner that must cover `p` cannot extend farther right than `p + 2R`. The greedy choice therefore maximizes coverage for that scanner.

4. **Count scanners used.**  
   Each iteration covers one maximal contiguous block of labels reachable from the current leftmost uncovered label. The invariant is: all labels before index `i` are covered using the minimum number of scanners induced by this left-to-right strategy.

5. **Use the feasibility result in binary search.**  
   If `canCover(mid)` is true, record `mid` as a candidate answer and search left for a smaller feasible range. Otherwise search right.

6. **Return the first feasible value.**  
   Binary search terminates at the smallest integer `R` for which the greedy check succeeds.

## 📊 Worked Example
Example: `labels = [1, 2, 8, 12, 17]`, `k = 2`

Check `R = 4`:

| Step | Leftmost uncovered | Scanner covers up to | Labels covered | Scanners used |
|------|---------------------|----------------------|----------------|---------------|
| 1 | `1` | `1 + 2*4 = 9` | `1, 2, 8` | 1 |
| 2 | `12` | `12 + 2*4 = 20` | `12, 17` | 2 |

All labels are covered with `2` scanners, so `R = 4` is feasible.

Check `R = 3`:

| Step | Leftmost uncovered | Scanner covers up to | Labels covered | Scanners used |
|------|---------------------|----------------------|----------------|---------------|
| 1 | `1` | `7` | `1, 2` | 1 |
| 2 | `8` | `14` | `8, 12` | 2 |
| 3 | `17` | `23` | `17` | 3 |

This needs `3` scanners, which exceeds `k`. Therefore `R = 3` is infeasible, and the minimum answer is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log D)`, where `n = labels.length` and `D = labels[n-1] - labels[0]`. Each feasibility check is a single linear pass, and binary search performs `O(log D)` checks. With coordinates up to `10^9`, `log D` is about 30, so the solution remains practical even when `n` approaches `10^6`.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few indices and counters; the input array dominates storage. Space cannot be meaningfully reduced below constant without changing the input representation, and there is no trade-off worth making here.

## 💡 Key Takeaways
- If the question asks for the minimum value that makes a condition possible, check whether feasibility becomes permanently true after some threshold; that is the binary-search-on-answer signal.
- If coverage happens on a line with sorted coordinates, a left-to-right greedy sweep is often the right feasibility check.
- The scanner covers labels up to `start + 2R`, not `start + R`; the scanner is placed at `start + R`, not at the label itself.
- Use `<=` when skipping covered labels, since labels exactly at the interval boundary are covered.
- In production systems, separating optimization from feasibility is powerful: binary search handles the objective, while a cheap monotonic predicate handles scale.

## 🚀 Variations & Further Practice
- Labels are unsorted and scanners have different per-device ranges or costs; now preprocessing and the feasibility model become more complex, often requiring sorting plus DP or parametric search.
- Minimize the number of scanners needed for a fixed range `R`; same greedy core, but the optimization axis is inverted and useful for capacity planning.
- Extend from a line to 2D points with circular coverage; the monotonicity remains, but the feasibility check is no longer a simple greedy sweep and often becomes NP-hard or approximation-driven.