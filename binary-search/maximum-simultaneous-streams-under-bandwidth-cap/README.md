# Maximum Simultaneous Streams Under Bandwidth Cap

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Greedy

---

## 🗂 Problem Overview
Given `n` stream encodings, each with a viewer capacity `viewers[i]` and per-viewer bandwidth cost `bitrate[i]`, choose a single target audience size `x >= 1` and select as many encodings as possible such that every selected encoding can serve exactly `x` viewers and total bandwidth stays within budget `B`. Return the maximum selectable count. The difficulty is that `x` is shared across the chosen set, capacities gate eligibility, and brute-forcing all possible `x` is infeasible under `n = 2e5`.

## 🌍 Engineering Impact
This pattern shows up in admission control and resource shaping systems where a uniform service level must be enforced across a chosen cohort: CDN bitrate ladders, multi-tenant batch schedulers, distributed rate-limiters, and cluster placement under CPU or memory budgets. The operational question is rarely “can I place one more item,” but “what is the largest cohort supportable at a common target level.” Without exploiting monotonic feasibility, planners degrade into repeated full scans or combinatorial search. With it, you get deterministic capacity planning, predictable latency, and a design that scales to large fleet snapshots or real-time what-if evaluation.

## 🔍 Problem Statement
You are given two arrays of length `n`: `viewers[i]` is the maximum number of viewers encoding `i` can serve, and `bitrate[i]` is the bandwidth consumed per served viewer on that encoding. You also have a total bandwidth budget `B`.

Pick an integer `x >= 1`. An encoding is eligible only if `viewers[i] >= x`. If selected, it contributes cost `x * bitrate[i]`. You may select any subset of eligible encodings, but every selected encoding must use the same `x`, and total cost must not exceed `B`. Return the maximum possible subset size.

Constraints:
- `1 <= n <= 200000`
- `1 <= viewers[i], bitrate[i] <= 10^9`
- `1 <= B <= 10^18`

Examples:
- `viewers = [5,3,8,6], bitrate = [4,2,1,3], B = 18` → `3`
- `viewers = [10,2,7,4], bitrate = [8,5,2,1], B = 12` → `2`

The key algorithmic pressure is that both `x` and the chosen subset are variable, so naive enumeration over all possible targets is too slow.

## 🪜 How to Solve This
1. Start from the output, not from `x` → we want the maximum number of encodings, call it `k`.
2. Ask a feasibility question instead: “Can I choose at least `k` encodings for some common `x` within budget?”
3. For a fixed `x`, the best way to get `k` encodings is obvious: among encodings with `viewers[i] >= x`, take the `k` smallest `bitrate[i]`, because every chosen encoding is multiplied by the same `x`.
4. That means feasibility for `(k, x)` is  
   `x * (sum of k cheapest eligible bitrates) <= B`.
5. Which `x` values matter? Only capacities present in `viewers`, because eligibility changes only when `x` crosses one of those values.
6. So for a fixed `k`, sweep `x` in descending capacity order, incrementally adding newly eligible bitrates, and track the sum of the `k` cheapest seen so far.
7. If feasibility holds for any `x`, then `k` is achievable.
8. Achievability is monotone in `k`: if `k` works, every smaller count works. That gives binary search on `k`.

This is the key shift: binary search the answer, and make each feasibility check efficient with a greedy data structure.

## 🧩 Algorithm Walkthrough
1. **Binary search on answer `k`**  
   Use the monotonic property: if `k` encodings can be selected, then any `k' < k` can also be selected by dropping some chosen encodings. Search `k` in `[0, n]`.

2. **Pre-sort encodings by `viewers` descending**  
   Represent each encoding as `(viewers[i], bitrate[i])` and sort by capacity descending. This lets us process candidate target values `x` from large to small while maintaining the set of encodings eligible for the current `x`.

3. **Observe the only relevant `x` values**  
   Feasibility changes only when eligibility changes, which happens at capacities appearing in the input. Between two consecutive distinct capacities, the eligible set is unchanged while cost scales linearly with `x`, so the smallest `x` in that interval is already one of those capacities. Checking distinct `viewers` values is sufficient.

4. **Feasibility check for a fixed `k`: Greedy + heap / balanced multiset**  
   Sweep distinct capacity levels downward. As encodings become eligible, insert their `bitrate` into a structure that maintains exactly the `k` smallest bitrates seen so far and their sum.  
   Invariant: after processing all encodings with `viewers >= x`, the maintained sum is the minimum possible bitrate sum for any `k` eligible encodings.

5. **Test budget at each capacity level**  
   Whenever at least `k` encodings are eligible, compute `cost = x * sumK`. If `cost <= B`, then `k` is feasible. This is correct because any other `k`-subset has bitrate sum at least `sumK`.

6. **Data structure choice**  
   The pattern is **Binary Search on Answer + Greedy selection under a moving eligibility frontier**. A max-heap of chosen bitrates works well: push each eligible bitrate, keep only the `k` smallest by evicting the largest when size exceeds `k`, and track the running sum.

## 📊 Worked Example
Use `viewers = [5, 3, 8, 6]`, `bitrate = [4, 2, 1, 3]`, `B = 18`.

Check whether `k = 3` is feasible.

Sorted by `viewers` descending:

| x level | Newly eligible bitrate(s) | Heap of 3 smallest bitrates | sumK | Cost `x * sumK` | Feasible |
|---|---:|---|---:|---:|---|
| 8 | 1 | [1] | — | — | need 3 items |
| 6 | 3 | [3,1] | — | — | need 3 items |
| 5 | 4 | [4,1,3] | 8 | 40 | no |
| 3 | 2 | keep 3 smallest → [3,1,2] | 6 | 18 | yes |

At `x = 3`, three encodings are eligible with cheapest bitrates `1, 2, 3`. Their total bandwidth is `3 * (1 + 2 + 3) = 18`, exactly the budget. So `k = 3` works. Binary search would then test whether `k = 4` works; it does not, so the answer is `3`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting costs `O(n log n)` once. Each feasibility check processes all encodings and performs heap operations in `O(log k)`, so `O(n log k)`, bounded by `O(n log n)`. Binary searching `k` adds another `log n` factor, for total `O(n log^2 n)`. This is practical at `2e5`; brute-force over target values or subset sizes is not.

### Space Complexity
`O(n)` for the sorted encoding list, plus `O(k)` auxiliary heap space during a feasibility check. The heap is the only dynamic structure. You could reduce some allocation churn by reusing buffers across checks, but asymptotically the footprint remains linear.

## 💡 Key Takeaways
- If the problem asks for a maximum count and feasibility for count `k` implies feasibility for all smaller counts, binary search on the answer is usually the right first move.
- If every selected item pays the same multiplicative factor `x`, the greedy core often becomes “among eligible items, take the `k` cheapest base costs.”
- Only distinct `viewers` values matter as candidate targets; checking arbitrary intermediate `x` values wastes work and obscures the monotonic structure.
- Use 64-bit arithmetic everywhere: both `sumK` and `x * sumK` can exceed 32-bit range long before hitting the stated constraints.
- At scale, this is a standard planning pattern: separate a monotone control variable from a fast incremental feasibility oracle instead of recomputing global optima from scratch.

## 🚀 Variations & Further Practice
- Allow each encoding to have a minimum and maximum supported viewer range, not just an upper bound. The twist is that eligibility becomes an interval, so the sweep must handle both insertions and removals.
- Replace uniform target `x` with selecting exactly `k` encodings and maximizing `x`. This flips the outer binary search variable and turns the problem into direct threshold feasibility.
- Add a second budget, such as total GPU transcode slots or regional capacity. The twist is moving from one-dimensional greedy selection to multi-constraint feasibility, where heaps alone are no longer sufficient.