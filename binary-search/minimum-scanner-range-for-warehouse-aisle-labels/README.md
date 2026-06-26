# Minimum Scanner Range for Warehouse Aisle Labels

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Greedy

---

## 🗂 Problem Overview
Given positions of fixed scanners and aisle labels on a number line, compute the smallest integer scanner range `r` such that every label lies within distance `r` of at least one scanner. Inputs may be unsorted and contain duplicates. The challenge is scale: with up to `2 * 10^5` elements in each array, checking every scanner-label pair is infeasible, so the solution must exploit ordering and monotonicity.

## 🌍 Engineering Impact
This pattern shows up anywhere fixed infrastructure must cover dynamic demand points with minimum tolerance: cellular tower radius planning, CDN edge reachability, warehouse robotics sensing, geofenced alerting, and latency-budget validation between clients and serving regions. At scale, brute-force nearest-neighbor checks collapse under quadratic behavior and poor cache locality. Sorting plus monotone feasibility testing turns an expensive all-pairs coverage problem into a predictable, production-friendly pipeline. That matters when coverage thresholds drive provisioning, SLA enforcement, or rollout safety checks, where you need deterministic performance and a proof that the chosen minimum is actually sufficient.

## 🔍 Problem Statement
You are given two integer arrays:

- `scanners[i]`: position of the `i`-th fixed scanner
- `labels[j]`: position of the `j`-th label that must be readable

A scanner with range `r` covers every label at position `x` where `|x - scanner| <= r`. Return the minimum integer `r` such that every label is covered by at least one scanner.

Constraints:

- `1 <= scanners.length, labels.length <= 2 * 10^5`
- `0 <= scanners[i], labels[j] <= 10^9`
- Positions may be unsorted and may contain duplicates
- The answer fits in a 32-bit signed integer

Examples:

- `scanners = [2, 10], labels = [1, 5, 11]` → `3`
- `scanners = [15, 4, 20], labels = [3, 8, 14, 21]` → `4`

The key constraint is input size. Any `O(n * m)` strategy is too slow, which pushes the solution toward sorting and a logarithmic search over the answer space.

## 🪜 How to Solve This
1. Read the problem → notice we are not asked which scanner covers which label, only the minimum range that makes full coverage possible.

2. “Minimum value such that condition becomes true” is a strong binary-search-on-answer signal. If range `r` works, then any larger range also works. That monotonicity is the core lever.

3. To test a candidate `r`, brute force is still too expensive. So sort both arrays and sweep left to right.

4. Once sorted, each scanner covers a continuous interval `[scanner - r, scanner + r]`. Coverage along a line is naturally greedy: advance through labels while the current scanner can cover them, then move to the next scanner.

5. The feasibility check becomes linear after sorting. That gives:
   - sort once
   - binary search over `r`
   - use a linear greedy pass for each midpoint

6. The alternative mental model is nearest distance per label after sorting scanners. That also works, but the binary-search framing is useful because it generalizes to many “minimum sufficient threshold” problems.

## 🧩 Algorithm Walkthrough
1. **Sort both arrays**  
   Sort `scanners` and `labels` in nondecreasing order. This converts arbitrary positions into an ordered 1D coverage problem. The invariant after sorting is simple: when scanning left to right, any uncovered label must be at or to the right of the current label pointer.

2. **Define the monotone predicate `canCover(r)`**  
   For a fixed range `r`, determine whether all labels can be covered. This is the **Greedy Sweep** pattern over sorted intervals. Each scanner induces interval `[s - r, s + r]`.

3. **Run a two-pointer feasibility scan**  
   Maintain pointer `j` to the first uncovered label. For each scanner `s`, advance `j` while `labels[j] < s - r` only if those labels are already impossible to cover by this scanner; in practice, if the current uncovered label is left of the scanner’s coverage start, feasibility fails unless a previous scanner covered it. A cleaner implementation is: while `j < labels.length` and `labels[j] <= s + r`, advance `j` only after ensuring `labels[j] >= s - r` or that earlier scanners already handled prior labels. Operationally, the simplest invariant is that `j` always points to the first label not yet covered by previous scanners; then for scanner `s`, cover every label with `labels[j] <= s + r` starting from that point if `labels[j] >= s - r`.

4. **Binary search the answer**  
   Search `r` in `[0, max(|label - scanner|)]`, safely bounded by `10^9`. If `canCover(mid)` is true, shrink right bound; otherwise grow left bound. The invariant is standard binary search: left is infeasible, right is feasible, or equivalently maintain the smallest known feasible range.

5. **Return the first feasible `r`**  
   Correctness follows from two facts: the predicate is monotone, and the greedy scan never wastes scanner coverage because on a line, covering the earliest uncovered labels first is always optimal.

## 📊 Worked Example
Example: `scanners = [2, 10]`, `labels = [1, 5, 11]`

Sorted inputs are unchanged. Test `r = 3`.

| Step | Scanner | Coverage Interval | First Uncovered Label | Labels Covered | Next `j` |
|---|---:|---|---:|---|---:|
| 1 | 2 | `[-1, 5]` | 1 | 1, 5 | 2 |
| 2 | 10 | `[7, 13]` | 11 | 11 | 3 |

`j = 3` means all labels are covered, so `r = 3` is feasible.

Now test `r = 2`.

| Step | Scanner | Coverage Interval | First Uncovered Label | Labels Covered | Next `j` |
|---|---:|---|---:|---|---:|
| 1 | 2 | `[0, 4]` | 1 | 1 | 1 |
| 2 | 10 | `[8, 12]` | 5 | none | 1 |

Label `5` remains uncovered, so `r = 2` is infeasible. Therefore the minimum feasible range is `3`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates the one-time preprocessing cost: `O(S log S + L log L)` for `S = scanners.length` and `L = labels.length`. Each feasibility check is `O(S + L)`, and binary search over the answer adds about `log 1e9 ≈ 31` iterations. Total: `O(S log S + L log L + (S + L) log M)`. This is practical at `10^6` scale, impossible at `10^9`.

### Space Complexity
Space is `O(1)` auxiliary beyond the sort implementation, or `O(log S + log L)` if counting recursion/stack used by the language’s sorting routine. The main memory cost is the input arrays themselves. You can’t meaningfully reduce that without giving up sorted access.

## 💡 Key Takeaways
- If the question asks for the minimum threshold that makes a global condition true, check for monotonicity and consider binary search on the answer.
- If entities live on a 1D line and coverage is interval-based, sorting plus a greedy sweep is usually the right abstraction.
- Be careful with inclusive boundaries: a label at exactly `scanner ± r` is covered.
- Duplicates do not change the algorithm, but they do expose pointer bugs if you accidentally skip equal positions incorrectly.
- In production systems, threshold search plus linear feasibility checks is a scalable pattern for turning expensive optimization problems into deterministic decision procedures.

## 🚀 Variations & Further Practice
- Allow adding at most `k` new scanners and ask for the minimum range. The twist is combining feasibility with placement strategy, not just validating fixed infrastructure.
- Move from 1D positions to 2D coordinates. The nearest-cover check stops being a simple sweep and typically requires spatial indexing or different geometry.
- Instead of minimizing range, minimize the number of scanners needed for a fixed range. This flips the optimization target and becomes a classic interval covering problem.