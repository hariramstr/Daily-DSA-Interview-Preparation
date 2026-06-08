# Minimum Capacity Shipping Containers Over D Days

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview

Given a fixed-order sequence of container weights and a day budget `D`, find the minimum ship capacity that allows all containers to be shipped within `D` days. Containers cannot be reordered — the ship loads greedily from left to right each day until adding the next container would exceed capacity. The non-trivial constraint: capacity is a continuous integer space you must search, not a value directly derivable from the input.

---

## 🌍 Engineering Impact

This pattern — binary searching on the *answer* rather than the input — appears wherever you need to minimize a resource bound subject to a feasibility check. Rate-limiter configuration (minimum token bucket size to drain a burst queue within a deadline), Kubernetes bin-packing (minimum node memory to schedule a pod sequence), video transcoding pipelines (minimum chunk size to meet SLA across N workers), and database partition sizing (minimum shard capacity to balance write throughput) all reduce to the same shape: monotone feasibility function over an integer range. Without this framing, engineers reach for simulation loops or heuristic tuning, both of which break under scale or adversarial inputs.

---

## 🔍 Problem Statement

**Input:** `weights: int[]` — ordered container weights; `D: int` — available shipping days.
**Output:** Minimum integer capacity such that a greedy left-to-right packing fits all containers into at most `D` trips.

**Constraints:**
- `1 <= D <= weights.length <= 50,000`
- `1 <= weights[i] <= 500`

**Examples:**

| `weights` | `D` | Output | Packing |
|---|---|---|---|
| `[3,2,2,4,1,4]` | `3` | `6` | `[3,2] / [2,4] / [1,4]` |
| `[1,2,3,4,5]` | `2` | `9` | `[1,2,3] / [4,5]` |

**Key constraint driving the algorithm:** The answer space `[max(weights), sum(weights)]` is monotone — if capacity `C` is feasible, so is every `C' > C`. That monotonicity is the binary search trigger.

---

## 🪜 How to Solve This

1. **Reframe the question.** "What is the minimum capacity?" is hard to answer directly. Flip it: "Is capacity `C` sufficient?" — that's a simple greedy simulation and easy to answer in O(n).

2. **Identify monotonicity.** If `C` works, `C+1` also works. If `C` fails, `C-1` also fails. The feasibility function is monotone over the capacity axis — that's the binary search signal.

3. **Bound the search space.** The lower bound is `max(weights)` — any single container must fit. The upper bound is `sum(weights)` — one ship, one day, everything fits. The answer lives in `[lo, hi]`.

4. **Binary search on the answer.** Pick `mid = (lo + hi) / 2`, simulate greedy packing, count days. If days `<= D`, the capacity is sufficient — try smaller (`hi = mid`). If days `> D`, too tight — go larger (`lo = mid + 1`).

5. **Terminate.** When `lo == hi`, you've converged on the minimum feasible capacity. No off-by-one risk if you use the half-open `lo < hi` loop with `hi = mid` / `lo = mid + 1`.

---

## 🧩 Algorithm Walkthrough

**Pattern: Binary Search on Answer + Greedy Feasibility Check**

This is the right abstraction because the answer space has a total order and a monotone predicate — the two requirements for binary search. The greedy check is correct because containers are ordered and we always want to pack as many as possible into the current day before starting a new one (any other packing strategy can only use more days).

**Steps:**

1. **Initialize bounds.** `lo = max(weights)`, `hi = sum(weights)`. These are the tightest valid bounds — anything below `lo` is immediately infeasible; `hi` is always feasible.

2. **Binary search loop** (`while lo < hi`). Compute `mid = lo + (hi - lo) / 2` (avoids overflow in languages with fixed-width integers).

3. **Greedy feasibility check** for capacity `mid`: iterate `weights` left to right, accumulate a running `load`. When `load + weights[i] > mid`, close the current day (increment `days`, reset `load = 0`), then add `weights[i]` to the new day. Always start with `days = 1`.

4. **Branch.** If `days <= D`: `mid` is feasible, but a smaller value might also work → `hi = mid`. If `days > D`: `mid` is too small → `lo = mid + 1`.

5. **Return `lo`.** Loop exits when `lo == hi`, which is the minimum feasible capacity. The invariant maintained throughout: `lo` is always a candidate that *might* be the answer; `hi` is always feasible.

---

## 📊 Worked Example

`weights = [3, 2, 2, 4, 1, 4]`, `D = 3`. Bounds: `lo = 4`, `hi = 16`.

| Iteration | `lo` | `hi` | `mid` | Greedy days | Decision |
|---|---|---|---|---|---|
| 1 | 4 | 16 | 10 | 2 (`[3,2,2,4] / [1,4]`) | `hi = 10` |
| 2 | 4 | 10 | 7 | 3 (`[3,2,2] / [4,1] / [4]`) | `hi = 7` |
| 3 | 4 | 7 | 5 | 4 (`[3,2] / [2] / [4,1] / [4]`) | `lo = 6` |
| 4 | 6 | 7 | 6 | 3 (`[3,2] / [2,4] / [1,4]`) | `hi = 6` |
| 5 | 6 | 6 | — | loop exits | **return 6** |

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n log S)** where `S = sum(weights) - max(weights)`. The binary search runs `O(log S)` iterations; each iteration runs the O(n) greedy check. With `n = 50,000` and `S ≤ 500 × 50,000 = 25,000,000`, that's roughly `50,000 × 25 ≈ 1.25M` operations — well within budget. At `n = 10^6` this stays tractable; at `n = 10^9` the linear scan per iteration becomes the bottleneck.

### Space Complexity

**O(1)** auxiliary space. The greedy check uses only scalar accumulators (`load`, `days`). The input array is read-only. No reduction trade-off exists here — the algorithm is already optimal on space without sacrificing time.

---

## 💡 Key Takeaways

- **Pattern signal — "minimize the maximum":** Any problem asking for the minimum value of a resource bound (capacity, speed, cost) where feasibility is monotone is a binary-search-on-answer candidate. The phrase "minimum X such that Y is possible" is the trigger.
- **Pattern signal — greedy feasibility check:** When the feasibility check for a fixed parameter reduces to a single left-to-right greedy scan with no backtracking, binary search on that parameter is almost always the intended solution.
- **Gotcha — lower bound must be `max(weights)`, not `1`:** Initializing `lo = 1` is logically correct but wastes `O(log max(weights))` iterations on provably infeasible candidates. More critically, it signals a weak grasp of the problem's invariants.
- **Gotcha — loop variant `hi = mid` vs `hi = mid - 1`:** Because you're searching for the *minimum feasible* value, use `hi = mid` (not `mid - 1`) when feasible, and `lo = mid + 1` when infeasible. Mixing these up produces an off-by-one that's hard to catch without a carefully chosen test case like `D = weights.length`.
- **Architectural insight:** The separation of *search strategy* (binary search) from *feasibility oracle* (greedy simulation) is a composable design. In production, the oracle can be swapped — e.g., replaced with a database query or a distributed computation — without touching the search logic. This is the same inversion-of-control principle behind configurable schedulers and capacity planners.

---

## 🚀 Variations & Further Practice

- **[LeetCode 1011 — Capacity To Ship Packages Within D Days](https://leetcode.com/problems/capacity-to-ship-packages-within-d-days/):** This problem verbatim. Solve it first to cement the pattern before moving to variants.
- **[LeetCode 410 — Split Array Largest Sum](https://leetcode.com/problems/split-array-largest-sum/):** Identical structure — minimize the maximum subarray sum across `k` partitions. The conceptual twist: the feasibility check is the same greedy scan, but the framing as a "split" problem obscures the binary-search-on-answer shape, making it harder to recognize under interview pressure.
- **[LeetCode 875 — Koko Eating Bananas](https://leetcode.com/problems/koko-eating-bananas/):** Binary search on eating speed rather than capacity. The twist: the feasibility check involves ceiling division per pile rather than a running sum, introducing a subtle integer arithmetic trap (`math.ceil(pile / speed)` vs `(pile + speed - 1) // speed`) that catches engineers who pattern-match too quickly without re-deriving the oracle.