# Minimum Pages Per Day to Finish All Books

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview

Given an ordered array of book page counts and a deadline of `d` days, find the minimum daily reading capacity that allows a student to finish all books in order without splitting any single book across days. The non-trivial constraint is the ordering requirement: books cannot be reordered to optimize packing, so the problem reduces to finding the minimum threshold over a monotonic feasibility function — a classic binary search on the answer space.

---

## 🌍 Engineering Impact

This pattern is the backbone of capacity planning under ordering constraints. Kafka partition assignment uses it to balance log segments across brokers while preserving offset order. Build systems like Bazel apply the same logic when distributing ordered compilation units across parallel workers. Rate limiters in API gateways binary-search a token budget to find the minimum window that satisfies SLA throughput. Without this approach, naive linear search over the answer space becomes untenable at scale — a 10^6-element input with page counts up to 10^6 demands logarithmic search over a million-wide range, not brute force.

---

## 🔍 Problem Statement

**Input:** Integer array `pages` (1 ≤ `pages.length` ≤ 10^5, 1 ≤ `pages[i]` ≤ 10^6) and integer `d` (1 ≤ `d` ≤ `pages.length`).

**Output:** Minimum integer `pagesPerDay` such that all books can be read sequentially within `d` days, where no book is split across days.

**Constraints driving the algorithm:**
- Books must be read in order — no reordering.
- `d ≤ pages.length` guarantees at least one book per day is always feasible.
- The lower bound on `pagesPerDay` is `max(pages)` (a single book must fit in one day); the upper bound is `sum(pages)` (all books in one day).

**Examples:**
```
pages = [3, 6, 7, 11], d = 2  →  17
pages = [30, 11, 23, 4, 20], d = 5  →  30
```

The ordering constraint eliminates sorting-based optimizations and forces a greedy feasibility check, which pairs naturally with binary search on the answer.

---

## 🪜 How to Solve This

1. **Observe the answer space has a clear range.** The minimum possible `pagesPerDay` is `max(pages)` — any less and the largest book is unreadable in a single day. The maximum is `sum(pages)` — read everything in one day. The answer lives in `[max(pages), sum(pages)]`.

2. **Notice the feasibility function is monotonic.** If capacity `k` is sufficient to finish in `d` days, then any `k' > k` is also sufficient. This monotonicity is the binary search trigger — you're not searching a sorted array, you're searching a sorted *decision boundary*.

3. **Reduce "is `k` feasible?" to a greedy scan.** Simulate reading greedily: accumulate pages into the current day until adding the next book would exceed `k`, then start a new day. Count days used. If days ≤ `d`, `k` is feasible.

4. **Binary search on `k`.** Narrow the range by testing the midpoint. If feasible, the answer could be lower — move right boundary down. If not, move left boundary up.

5. **Terminate when `lo == hi`.** That value is the minimum feasible capacity.

The insight: you're not searching *in* the input array — you're searching *over a derived answer space* using the input as an oracle.

---

## 🧩 Algorithm Walkthrough

**Pattern: Binary Search on Answer Space + Greedy Feasibility Check**

This is the right abstraction because the answer space is ordered and the feasibility predicate is monotone — two conditions sufficient to guarantee binary search correctness.

**Steps:**

1. **Initialize bounds.** `lo = max(pages)`, `hi = sum(pages)`. These are tight: anything below `lo` is provably infeasible; `hi` is trivially feasible.

2. **Binary search loop.** While `lo < hi`, compute `mid = lo + (hi - lo) / 2` (avoids overflow). Use floor division to bias toward smaller values.

3. **Greedy feasibility check for `mid`.** Initialize `daysNeeded = 1`, `currentLoad = 0`. Iterate through `pages`: if `currentLoad + pages[i] > mid`, increment `daysNeeded` and reset `currentLoad = pages[i]`; otherwise add `pages[i]` to `currentLoad`. This greedy packing is optimal because ordering is fixed — there's no benefit to leaving capacity unused earlier.

4. **Narrow the search space.** If `daysNeeded <= d`, the capacity is sufficient — try lower: `hi = mid`. If `daysNeeded > d`, insufficient — must increase: `lo = mid + 1`.

5. **Return `lo`.** When the loop exits, `lo == hi` and both point to the minimum feasible capacity. The invariant maintained throughout: `lo` is always a candidate answer (feasible or not yet tested), `hi` is always feasible.

**Why greedy is correct here:** books are ordered and indivisible. Packing as many books as possible into each day minimizes days used for any fixed capacity — no rearrangement can do better.

---

## 📊 Worked Example

**Input:** `pages = [3, 6, 7, 11]`, `d = 2`

**Search space:** `lo = 11`, `hi = 27`

| Iteration | lo | hi | mid | daysNeeded | Feasible? | Action     |
|-----------|----|----|-----|------------|-----------|------------|
| 1         | 11 | 27 | 19  | 2          | Yes       | hi = 19    |
| 2         | 11 | 19 | 15  | 3          | No        | lo = 16    |
| 3         | 16 | 19 | 17  | 2          | Yes       | hi = 17    |
| 4         | 16 | 17 | 16  | 3          | No        | lo = 17    |
| Exit      | 17 | 17 | —   | —          | —         | return 17  |

**Feasibility trace for `mid = 17`:** Day 1 accumulates 3 → 9 → 16 (adding 11 would exceed 17, stop). Day 2 takes 11. `daysNeeded = 2 ≤ d`. ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n · log(sum(pages)))** — the binary search runs over a range of at most `n × 10^6` ≈ 10^11, giving ~37 iterations. Each iteration runs an O(n) greedy scan. At n = 10^5, this is roughly 3.7 × 10^6 operations — well within real-time constraints. At n = 10^9 (hypothetical), the log factor stays bounded (~30), but the linear scan becomes the bottleneck.

### Space Complexity

**O(1)** auxiliary space — the greedy feasibility check uses only a fixed number of scalar variables regardless of input size. The input array itself is read-only. No reduction trade-off exists here; this is already optimal.

---

## 💡 Key Takeaways

- **Pattern signal — monotonic feasibility:** When a problem asks for the *minimum value of X such that some condition holds*, and the condition transitions cleanly from false to true as X increases, binary search on the answer space is the right tool — not a search on the input array.
- **Pattern signal — "minimum maximum" or "maximum minimum" phrasing:** Problems framed as minimizing the maximum load, or maximizing the minimum gap, almost always reduce to binary search on the answer with a greedy feasibility oracle.
- **Off-by-one trap — lower bound initialization:** Setting `lo = 0` instead of `max(pages)` is technically correct but wastes log iterations and obscures intent. More critically, a feasibility check with `mid < max(pages)` will always return false, which can mask bugs in the greedy logic during debugging.
- **Off-by-one trap — `hi = mid` vs `hi = mid - 1`:** Because we want the *minimum* feasible value and feasibility at `mid` means the answer could *be* `mid`, use `hi = mid` (not `mid - 1`) when feasible. Using `mid - 1` skips the correct answer.
- **Architectural insight:** This pattern generalizes to any resource-allocation problem with ordering constraints and a monotone cost function — distributed log compaction, shard rebalancing, CI job scheduling. The greedy feasibility check is the domain-specific component; the binary search harness is reusable infrastructure.

---

## 🚀 Variations & Further Practice

- **LeetCode 1011 — Capacity to Ship Packages Within D Days:** Structurally identical problem with different framing (shipping weight vs. reading pages). Useful for confirming the pattern is domain-agnostic and practicing the same harness with a different feasibility oracle.
- **LeetCode 410 — Split Array Largest Sum:** Asks for the minimum possible value of the *maximum subarray sum* when splitting into at most `k` subarrays. The twist: the objective flips from minimizing total days to minimizing the peak partition sum, requiring a subtle change in how the greedy oracle counts splits and what constitutes "feasible."
- **Painter's Partition Problem (multi-painter variant):** Extends the pattern by introducing parallel workers, each constrained to contiguous segments. The feasibility check remains greedy, but the correctness argument must account for work-stealing boundaries — a meaningful step toward real distributed scheduling problems where this pattern appears in production capacity models.