# Minimum Capacity Courier Fleet for Weighted Deliveries

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Dynamic Programming

---

## 🗂 Problem Overview

Given `n` packages in a fixed order with associated weights, partition them into exactly `k` contiguous, non-empty segments — one per courier — such that no segment exceeds `maxPkgs` packages in count. The objective is to minimize the maximum weight-sum across all segments. The non-trivial constraint is the dual-axis feasibility check: a valid partition must simultaneously satisfy a count bound (packages per courier) and a weight bound (the capacity we're optimizing), making pure greedy insufficient without a search framework.

---

## 🌍 Engineering Impact

This pattern is the backbone of load-balancing decisions in distributed systems. Kafka partition assignment, MapReduce shard splitting, and database horizontal sharding all reduce to variants of this problem: divide an ordered sequence into `k` chunks while minimizing the hottest chunk. Without a binary-search-on-answer approach, naive enumeration collapses under scale — a 10^5-element input with thousands of workers makes brute-force infeasible. The dual constraint (count + weight) mirrors real-world SLAs where both throughput limits and payload size caps must be respected simultaneously.

---

## 🔍 Problem Statement

**Input:** Integer array `weights` of length `n`, integer `k` (number of couriers), integer `maxPkgs` (max packages per courier).

**Output:** Minimum possible value of the maximum load among all `k` couriers, or `-1` if no valid partition exists.

**Constraints:**
- `1 ≤ n ≤ 10^5`, `1 ≤ k ≤ n`, `1 ≤ maxPkgs ≤ n`, `1 ≤ weights[i] ≤ 10^4`

**Examples:**

| Input | Output |
|---|---|
| `weights=[3,2,8,5,1,7,4]`, `k=3`, `maxPkgs=4` | `13` |
| `weights=[10,10,10,10,10]`, `k=3`, `maxPkgs=1` | `-1` |

**Key constraint driving the algorithm:** feasibility of a given capacity cap is monotonic — if capacity `C` works, so does `C+1`. That monotonicity is the binary search entry point. The `maxPkgs` bound adds a second feasibility dimension that the greedy checker must enforce independently.

---

## 🪜 How to Solve This

1. **Recognize the optimization shape** → we're minimizing a maximum value over a partition space. "Minimize the maximum" is a canonical signal for binary search on the answer.

2. **Establish the search space** → the answer lies between `max(weights)` (minimum possible capacity for any valid assignment) and `sum(weights)` (one courier takes everything). Both bounds are computable in O(n).

3. **Reframe as a decision problem** → instead of "find the minimum capacity," ask "given capacity `C`, can we partition into at most `k` segments each satisfying both the weight cap and the `maxPkgs` count cap?" Decision problems are easier to solve and compose.

4. **Build the greedy checker** → scan left to right, greedily extending the current segment as long as neither constraint is violated. Count the segments needed. If segments needed ≤ `k`, capacity `C` is feasible.

5. **Verify monotonicity** → if `C` is feasible, `C+1` is trivially feasible (relaxing the weight cap never breaks a valid partition). Binary search is valid.

6. **Handle impossibility upfront** → if `k * maxPkgs < n`, there aren't enough slots to cover all packages regardless of capacity. Return `-1` immediately.

---

## 🧩 Algorithm Walkthrough

**Pattern: Binary Search on Answer + Greedy Feasibility Check**

This is the right abstraction because the answer space is ordered and the feasibility predicate is monotone — two necessary and sufficient conditions for binary search applicability.

**Step 1 — Impossibility pre-check:**
If `k * maxPkgs < n`, return `-1`. Even with infinite capacity, we can't assign all packages without violating the count constraint on at least one courier.

**Step 2 — Define search bounds:**
- `lo = max(weights)` — any valid capacity must accommodate the heaviest single package.
- `hi = sum(weights)` — trivially feasible upper bound.

**Step 3 — Binary search loop:**
While `lo < hi`, compute `mid = lo + (hi - lo) / 2`. Call the feasibility checker with capacity `mid`.

**Step 4 — Greedy feasibility checker (`canDeliver(C)`):**
Initialize `segments = 1`, `currentLoad = 0`, `currentCount = 0`. Iterate through `weights`:
- If adding `weights[i]` would exceed `C` (weight cap) or `currentCount + 1 > maxPkgs` (count cap), start a new segment, incrementing `segments`. If `segments > k`, return `false`.
- Otherwise, accumulate `weights[i]` into `currentLoad` and increment `currentCount`.
Return `segments ≤ k`.

**Step 5 — Narrow the search:**
If `canDeliver(mid)` is true, set `hi = mid` (mid might be optimal). Otherwise, set `lo = mid + 1`.

**Step 6 — Return `lo`** — the minimum feasible capacity.

**Invariant maintained:** At every iteration, the answer lies within `[lo, hi]`.

---

## 📊 Worked Example

**Input:** `weights = [3, 2, 8, 5, 1, 7, 4]`, `k = 3`, `maxPkgs = 4`

**Pre-check:** `3 * 4 = 12 ≥ 7` ✓ | `lo = 8`, `hi = 30`

| Iteration | `lo` | `hi` | `mid` | `canDeliver(mid)` | Action |
|---|---|---|---|---|---|
| 1 | 8 | 30 | 19 | `[3,2,8,5,1] \| [7,4]` → 2 segments ≤ 3 ✓ | `hi = 19` |
| 2 | 8 | 19 | 13 | `[3,2,8] \| [5,1,7] \| [4]` → 3 segments ≤ 3 ✓ | `hi = 13` |
| 3 | 8 | 13 | 10 | `[3,2,8]`=13 > 10, try `[3,2]`\|`[8]`\|`[5,1]`\|`[7,4]` → 4 > 3 ✗ | `lo = 11` |
| 4 | 11 | 13 | 12 | `[3,2,8]`=13 > 12 → forces split → 4 segments > 3 ✗ | `lo = 13` |
| 5 | 13 | 13 | — | Loop exits | **Return 13** |

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n log S)** where `S = sum(weights)` (bounded by `n * max(weight) = 10^5 * 10^4 = 10^9`). The binary search runs `O(log S) ≈ 30` iterations; each feasibility check is O(n). At `n = 10^5`, this is roughly 3 × 10^6 operations — well within real-time constraints. At `n = 10^9` (hypothetical), the log factor keeps it tractable where O(n^2) would not be.

### Space Complexity

**O(1)** auxiliary space. The greedy checker uses only scalar accumulators regardless of input size. The input array itself is O(n) but not owned by the algorithm. No reduction trade-off exists here — the approach is already optimal on space.

---

## 💡 Key Takeaways

- **Pattern signal #1:** "Minimize the maximum" or "maximize the minimum" over a partition of an ordered sequence almost always maps to binary search on the answer — the monotonicity of feasibility is the tell.
- **Pattern signal #2:** When a problem has two independent constraints (here: weight cap AND count cap), check whether each can be enforced independently in a single greedy pass. If yes, the feasibility checker composes cleanly.
- **Gotcha #1:** The lower bound of the search space must be `max(weights)`, not `0` or `1`. Starting lower wastes iterations and can cause incorrect feasibility results if a single package exceeds the tested capacity.
- **Gotcha #2:** The impossibility check `k * maxPkgs < n` must happen before the binary search. Without it, the greedy checker may silently return a wrong answer when the search space itself is vacuous.
- **Architectural insight:** The binary-search-on-answer + greedy-checker decomposition is a reusable template for capacity planning problems in infrastructure: treat the capacity parameter as a continuous dial, define a cheap feasibility oracle, and let binary search find the threshold. This separates policy (what's optimal) from mechanism (what's feasible) — a clean boundary for testing and future constraint changes.

---

## 🚀 Variations & Further Practice

- **Variable courier counts with cost:** Extend the problem so that using more than `k` couriers incurs a per-courier cost, and you must minimize `max_load + α * num_couriers`. The monotonicity breaks in two dimensions simultaneously, requiring a Lagrangian relaxation (aliens trick / lambda optimization) layered on top of the binary search — a significantly harder composition.
- **Non-contiguous assignment with dependencies:** Remove the contiguity requirement but add a DAG of package dependencies (package `i` must be delivered before package `j`). The greedy checker becomes a topological-sort-aware DP, and the binary search shell remains intact — isolating where the complexity lives.
- **LeetCode 410 — Split Array Largest Sum:** The direct predecessor problem without the `maxPkgs` constraint. Solving it first isolates the binary-search-on-answer pattern before the dual-constraint complexity is introduced.