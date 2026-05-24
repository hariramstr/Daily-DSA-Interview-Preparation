# Minimum Bandwidth Allocation for K Data Streams

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Prefix Sum

---

## 🗂 Problem Overview

Given `n` streams with bandwidth requirements and exactly `k` channels, assign streams to channels in **contiguous order** to minimize the maximum channel load. The contiguity constraint is what makes this non-trivial — you cannot reorder or cherry-pick streams across channels. Input is an integer array `streams` and integer `k`; output is a single integer representing the minimized maximum load. The challenge is finding the optimal partition boundary placement across an exponential search space.

---

## 🌍 Engineering Impact

This pattern appears directly in **Kafka partition assignment**, where log segments must be distributed across brokers contiguously to preserve ordering guarantees. It also governs **video transcoding pipeline sharding** (splitting frame ranges across worker nodes), **database horizontal partitioning** (range-based sharding to balance query load), and **compiler instruction scheduling** across execution units. Without this approach, naive assignment creates hotspot channels that become throughput bottlenecks, causing cascading backpressure. Binary search on the answer converts an NP-hard-looking partition problem into a tractable O(n log S) decision problem.

---

## 🔍 Problem Statement

**Input:** Integer array `streams` of length `n` (1 ≤ n ≤ 10⁵, 1 ≤ streams[i] ≤ 10⁹) and integer `k` (1 ≤ k ≤ n).

**Output:** Minimum possible value of the maximum channel load when streams are partitioned into exactly `k` contiguous groups.

**Constraints driving the algorithm:** Streams must be assigned in contiguous blocks — no reordering. This eliminates sorting-based greedy approaches and makes the partition boundary placement the sole degree of freedom.

**Examples:**

```
streams = [10, 20, 30, 40, 50], k = 3  →  60
  Split: [10,20,30] | [40] | [50]  →  loads: 60, 40, 50

streams = [7, 2, 5, 10, 8], k = 2  →  18
  Split: [7,2,5] | [10,8]  →  loads: 14, 18
```

**Edge cases:** k = 1 (answer is total sum), k = n (answer is max single element), streams containing a single very large value that dominates any partition.

---

## 🪜 How to Solve This

1. **Read the problem → notice the output is a minimum of a maximum.** "Minimize the maximum" is a canonical signal for binary search on the answer, not direct optimization.

2. **Reframe the question →** Instead of asking "where do I place partition boundaries?", ask "given a maximum load cap `M`, can I partition the array into at most `k` channels without exceeding `M`?"

3. **This new question is a decision problem →** It has a monotonic property: if cap `M` is feasible, then any `M' > M` is also feasible. Monotonicity is the prerequisite for binary search.

4. **Define the search space →** Lower bound is `max(streams)` (a single stream must fit in one channel). Upper bound is `sum(streams)` (one channel holds everything). The answer lives in this range.

5. **Write a greedy feasibility check →** Greedily assign streams to the current channel until adding the next stream would exceed `M`; open a new channel. Count channels used. If count ≤ k, `M` is feasible.

6. **Binary search over `[lo, hi]` →** Converge on the smallest feasible `M`. Each feasibility check is O(n), binary search runs O(log S) iterations where S = sum(streams).

The insight crystallizes: partition placement is hard, but *validating a placement bound* is linear.

---

## 🧩 Algorithm Walkthrough

**Pattern: Binary Search on the Answer + Greedy Feasibility Validator**

This is the right abstraction because the answer space is ordered and the feasibility predicate is monotonic — classic conditions for binary search on a value rather than an index.

**Steps:**

1. **Compute bounds.** `lo = max(streams)` — no channel can have a load less than the largest single stream. `hi = sum(streams)` — the trivially feasible upper bound (one channel). Both are computed in O(n).

2. **Binary search loop.** While `lo < hi`, compute `mid = lo + (hi - lo) / 2`. Use integer midpoint to avoid overflow.

3. **Feasibility check (`canAllocate(mid)`).**
   - Initialize `channels = 1`, `currentLoad = 0`.
   - Iterate through each stream. If `currentLoad + stream > mid`, increment `channels` and reset `currentLoad = 0`.
   - Add stream to `currentLoad`.
   - If `channels > k` at any point, return `false`.
   - Return `true` if loop completes with `channels ≤ k`.
   - **Invariant:** At each step, `currentLoad` is the minimum possible load for the current channel given greedy left-to-right packing.

4. **Narrow the search.** If `canAllocate(mid)` is true, the answer could be `mid` or lower: set `hi = mid`. Otherwise, set `lo = mid + 1`.

5. **Termination.** When `lo == hi`, that value is the minimum feasible maximum load. Return `lo`.

---

## 📊 Worked Example

`streams = [7, 2, 5, 10, 8]`, `k = 2`

**Bounds:** `lo = max = 10`, `hi = sum = 32`

| Iteration | lo | hi | mid | canAllocate(mid) | Reasoning |
|-----------|----|----|-----|-----------------|-----------|
| 1 | 10 | 32 | 21 | true | [7,2,5,10] load=24 > 21 → split → ch1=14, ch2=18 ≤ 21 ✓ |
| 2 | 10 | 21 | 15 | false | [7,2,5] load=14, add 10 → 24 > 15 → ch2, add 8 → 18 > 15 → ch3 > k ✗ |
| 3 | 16 | 21 | 18 | true | [7,2,5] load=14, add 10 → 24 > 18 → ch2=[10,8]=18 ≤ 18 ✓ |
| 4 | 16 | 18 | 17 | false | [7,2,5]=14, [10]=10, [8]=8 → 3 channels > k ✗ |
| 5 | 18 | 18 | — | — | lo == hi → **return 18** |

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n log S)** where S = `sum(streams)` (up to 10¹⁴ for n=10⁵, streams[i]=10⁹). Binary search runs at most ~47 iterations over log₂(10¹⁴); each iteration scans all n streams. At n=10⁶ this is ~60M operations — comfortably sub-second. At n=10⁹ you'd need streaming/chunked evaluation.

### Space Complexity

**O(1)** auxiliary space. The feasibility check uses only scalar counters; no prefix sum array is required. A prefix sum array could accelerate range-sum queries in a more complex variant but adds O(n) space with no benefit here since the greedy scan is already linear.

---

## 💡 Key Takeaways

- **Pattern signal — "minimize the maximum" or "maximize the minimum":** These phrases almost always indicate binary search on the answer. The moment you see this phrasing, ask whether a feasibility check on the answer value is easier than direct optimization.
- **Pattern signal — contiguous grouping with an optimization objective:** When elements cannot be reordered and you're partitioning into groups, greedy validators combined with binary search consistently outperform DP for large n (DP would be O(n·k) here, which is 10¹⁰ at scale).
- **Gotcha — lower bound must be `max(streams)`, not 0 or 1:** Setting `lo = 0` wastes iterations and risks incorrect convergence if all streams are equal. The tightest valid lower bound is the maximum single element.
- **Gotcha — use `lo + (hi - lo) / 2`, not `(lo + hi) / 2`:** With `hi = sum(streams)` potentially reaching 10¹⁴, naive midpoint overflows 32-bit integers and silently corrupts results in languages without arbitrary precision.
- **Architectural insight:** This binary-search-on-answer + greedy-validator decomposition is a reusable template for resource allocation problems at scale — rate limiter configuration, shard sizing, and worker pool partitioning all reduce to the same structure when the feasibility predicate is monotonic and cheap to evaluate.

---

## 🚀 Variations & Further Practice

- **Weighted channels with capacity limits:** Each channel has a maximum capacity constraint in addition to minimizing the max load. The feasibility check gains an extra termination condition, but the harder twist is that the lower bound calculation must account for mandatory splits around oversized segments — the greedy validator's correctness proof changes.
- **2D stream allocation (streams × priority classes):** Partition `n` streams across `k` channels where each channel also has a priority-class budget. This extends to binary search over a 2D feasibility space, requiring a more complex validator and raising the question of whether the feasibility region remains convex — a prerequisite for binary search to remain correct.
- **LeetCode 410 — Split Array Largest Sum:** The canonical form of this exact problem. Solving it first, then generalizing to weighted costs or multi-dimensional constraints, is the standard progression for internalizing the binary-search-on-answer template.