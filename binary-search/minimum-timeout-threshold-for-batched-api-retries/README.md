# Minimum Timeout Threshold for Batched API Retries

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an ordered array `requestTimes` and a limit `maxBatches`, find the smallest integer timeout threshold `T` such that the calls can be partitioned into at most `maxBatches` contiguous batches, where each batch sum is `<= T`. Order cannot change, and every call must be assigned exactly once. The non-trivial part is that the search space for `T` is large, but feasibility is monotonic: once a threshold works, every larger threshold also works.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered work must be chunked under a hard budget: API retry windows, Kafka consumer micro-batching, distributed log shipping, compaction planning, media segmenting, and CI job sharding. At scale, picking the threshold too low explodes coordination overhead; too high increases tail latency and failure blast radius. The value here is not just solving a toy partition problem — it is recognizing a monotonic control parameter and replacing expensive search over partitionings with a cheap feasibility oracle plus binary search. That shift is often the difference between operational tuning and brute-force collapse.

## 🔍 Problem Statement
You are given:

- `requestTimes`, where `requestTimes[i]` is the execution time of the `i`-th API call
- `maxBatches`, the maximum number of contiguous batches allowed

Calls must remain in original order. A batch contains consecutive calls, and the sum of times inside a batch must not exceed timeout threshold `T`. If adding the next call would exceed `T`, the current batch closes and a new one begins. Every call must belong to exactly one batch.

Return the minimum integer `T` such that all calls fit in at most `maxBatches` batches.

Constraints:

- `1 <= requestTimes.length <= 100000`
- `1 <= requestTimes[i] <= 1000000000`
- `1 <= maxBatches <= requestTimes.length`
- Answer fits in 64-bit signed integer

Examples:

- `requestTimes = [7,2,5,10,8], maxBatches = 2` → `18`
- `requestTimes = [4,4,4,4], maxBatches = 3` → `8`

The key constraint is input size: `n` is large enough that enumerating partitions is infeasible.

## 🪜 How to Solve This
1. Read the problem → this is not arbitrary partitioning; batches must be **contiguous** and preserve order. That removes combinatorial freedom and suggests a linear scan can validate a candidate answer.

2. Ask what the output really is → not the partition itself, but the **minimum feasible threshold** `T`.

3. Notice the monotonicity → if threshold `T` works, then any `T' > T` also works, because larger batch budgets can only reduce or preserve the number of required batches.

4. Once you see “minimum value that satisfies a monotonic predicate,” think **binary search on the answer**.

5. Define the predicate: “Can I process all calls in at most `maxBatches` batches with threshold `T`?”  
   That can be checked greedily: keep adding calls to the current batch until the next one would exceed `T`, then start a new batch.

6. Why is greedy enough? Because for a fixed `T`, delaying a split as long as possible minimizes the number of batches. Any earlier split cannot help.

7. Set bounds correctly → lower bound is `max(requestTimes)`, upper bound is `sum(requestTimes)`. Binary search that range and return the first feasible threshold.

## 🧩 Algorithm Walkthrough
1. **Establish search bounds.**  
   Let `low = max(requestTimes)` and `high = sum(requestTimes)`.  
   `low` is mandatory because no threshold below the largest single request can ever work. `high` is always feasible because one batch containing everything is valid when allowed by threshold.

2. **Use Binary Search on Answer Space.**  
   This is the core pattern: search over integer thresholds rather than array indices. At each iteration, compute `mid = low + (high - low) / 2` to avoid overflow.

3. **Run a greedy feasibility check for `mid`.**  
   Scan `requestTimes` left to right, maintaining:
   - `currentBatchSum`
   - `batchesUsed`  
   Add each request to the current batch if it fits. Otherwise, start a new batch with that request and increment `batchesUsed`.

4. **Maintain the key invariant.**  
   During the scan, each constructed batch has sum `<= mid`, and the algorithm uses the **minimum number of batches possible** for that threshold because it only splits when forced. This is the Greedy part.

5. **Interpret the result.**  
   If `batchesUsed <= maxBatches`, then `mid` is feasible, so search left: set `high = mid`. Otherwise, `mid` is too small, so search right: set `low = mid + 1`.

6. **Terminate on convergence.**  
   When `low == high`, that value is the smallest feasible threshold. Binary search correctness follows from the monotonic predicate: infeasible values lie on the left, feasible values on the right.

This combination of **Binary Search + Greedy Feasibility Check** is the right abstraction because the optimization target is scalar, ordered, and monotonic.

## 📊 Worked Example
Example: `requestTimes = [7, 2, 5, 10, 8]`, `maxBatches = 2`

Initial bounds: `low = 10`, `high = 32`

| mid | Greedy batching under mid | batchesUsed | Feasible? |
|---|---|---:|---|
| 21 | `[7,2,5] [10,8]` | 2 | Yes |
| 15 | `[7,2,5] [10] [8]` | 3 | No |
| 18 | `[7,2,5] [10,8]` | 2 | Yes |
| 17 | `[7,2,5] [10] [8]` | 3 | No |

Trace:
1. `mid = 21` works, so tighten right bound to search for a smaller threshold.
2. `mid = 15` fails because `10 + 8 > 15`, forcing 3 batches.
3. `mid = 18` works.
4. `mid = 17` fails.

Bounds converge at `18`, which is the minimum feasible timeout threshold.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n` is the number of calls and `S = sum(requestTimes) - max(requestTimes) + 1` is the answer range. Each binary-search step performs one linear feasibility scan. In practice this scales well: even for very large numeric ranges, `log S` stays small, while the dominant cost remains sequential memory-friendly iteration.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few counters and bounds; no extra arrays or partition structures are required. You could materialize the actual batching, but that would increase space to `O(maxBatches)` or `O(n)` depending on representation.

## 💡 Key Takeaways
- If the problem asks for the **minimum numeric threshold** that makes a process possible, check whether feasibility is monotonic and binary-search the answer.
- If validation requires partitioning an ordered sequence into contiguous groups under a cap, a left-to-right greedy scan is often the correct feasibility oracle.
- Set the lower bound to `max(requestTimes)`, not `0` or `min(requestTimes)`; otherwise you waste search space or admit impossible thresholds.
- In the feasibility check, start a new batch only when `currentSum + value > T`; using `>=` is a classic off-by-one bug that rejects exact fits.
- At production scale, this pattern is a control-plane optimization: search over policy parameters, not over explicit partition configurations.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core problem; the twist is framing it as minimizing the maximum subarray sum rather than batching under a timeout budget.
- **Ship Packages Within D Days**: binary search on capacity, but the feasibility model is day-based scheduling; same monotonic predicate, different domain semantics.
- **Minimize the maximum batch cost with per-batch fixed overhead**: harder because each split changes total cost structure, so the feasibility predicate may need adjustment or dynamic programming.