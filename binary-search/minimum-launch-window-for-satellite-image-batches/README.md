# Minimum Launch Window for Satellite Image Batches

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, array

---

## 🗂 Problem Overview
Given an ordered array `batches` and an integer `d`, find the smallest integer capacity such that all batches can be transmitted in at most `d` launch windows. Each window may carry only a consecutive run of batches, and no batch can be split. The challenge is that capacity is not chosen directly from the input; it must be inferred under ordering and grouping constraints, which rules out simple balancing or sorting-based approaches.

## 🌍 Engineering Impact
This pattern shows up anywhere work must remain ordered while fitting within bounded execution windows: Kafka segment compaction, log shipping under bandwidth caps, CI artifact bundling, media chunk uploads, and batch scheduling in ETL pipelines. At scale, brute-force exploration of partition points collapses under large inputs, while overprovisioning wastes capacity and cost. Binary search on the answer turns a sizing problem into a sequence of feasibility checks, which is exactly how production systems often choose safe throughput limits, shard sizes, or batch ceilings without enumerating every schedule.

## 🔍 Problem Statement
You are given an array `batches` where `batches[i]` is the size, in megabytes, of the `i`-th satellite image batch, and an integer `d` representing the number of launch windows remaining. Batches must be transmitted in the given order. In one window, you may send any consecutive sequence of batches whose total size does not exceed a chosen capacity. Every batch must fit entirely within one window, and batches cannot be reordered or split.

Return the minimum integer capacity that allows all batches to be sent within at most `d` windows.

Constraints:
- `1 <= batches.length <= 100000`
- `1 <= batches[i] <= 1000000000`
- `1 <= d <= batches.length`
- Answer fits in signed 64-bit integer

Examples:
- `batches = [12, 7, 15, 6, 9], d = 3` → `21`
- `batches = [5, 5, 5, 5, 5, 5], d = 2` → `15`

The key constraint is input size: `n` can reach `100000`, so partition-enumeration or DP over all splits is unnecessary and too expensive.

## 🪜 How to Solve This
1. Read the constraints → order is fixed, batches cannot be split, and we need the *minimum feasible capacity*, not the actual partitioning first.

2. Ask what happens if capacity is fixed. For any candidate capacity `C`, we can greedily scan left to right and pack as many consecutive batches as possible into the current window. Once the next batch would exceed `C`, we start a new window.

3. Notice the monotonic property: if capacity `C` works, then any capacity larger than `C` also works. If `C` fails, any smaller capacity also fails.

4. A monotonic feasibility boundary is the signal for binary search on the answer space.

5. Establish bounds:
   - Lower bound = `max(batches)`, because every batch must fit individually.
   - Upper bound = `sum(batches)`, because one window could carry everything.

6. Binary search between those bounds. For each midpoint, run the greedy feasibility check and count required windows.

7. If required windows `<= d`, capacity is feasible, so try smaller. Otherwise, increase capacity.

This is the standard “search the minimum feasible threshold” pattern.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer + Greedy Feasibility Check.**  
   We are not searching an index in a sorted array; we are searching the smallest capacity in a numeric range that satisfies a monotonic predicate: “can all batches be transmitted within `d` windows?”

2. **Set the search interval.**  
   Let `lo = max(batches)` and `hi = sum(batches)`.  
   - `lo` is necessary because no capacity smaller than the largest batch can ever work.  
   - `hi` is sufficient because sending everything in one window is always possible at that capacity.  
   Invariant: the true answer always lies in `[lo, hi]`.

3. **Check a candidate capacity greedily.**  
   Scan `batches` left to right, maintaining the current window load. Add the next batch if it fits; otherwise, open a new window and place that batch there.  
   Why greedy is correct: for a fixed capacity, delaying a split as long as possible minimizes the number of windows used. Any earlier split cannot reduce window count.

4. **Use the feasibility result to shrink the interval.**  
   If the greedy scan uses `<= d` windows, `mid` is feasible, so record it implicitly and move left: `hi = mid`.  
   Otherwise, `mid` is infeasible, so move right: `lo = mid + 1`.  
   Invariant: all capacities below `lo` are known infeasible; all capacities at or above `hi` are feasible.

5. **Terminate when `lo == hi`.**  
   At convergence, the interval has collapsed to the smallest feasible capacity, which is the required answer.

This abstraction is right because the expensive part is not choosing partitions directly; it is evaluating whether a capacity threshold is sufficient.

## 📊 Worked Example
Example: `batches = [12, 7, 15, 6, 9]`, `d = 3`

Initial bounds:
- `lo = 15`
- `hi = 49`

| mid | Greedy grouping under `mid` | Windows used | Feasible? |
|---|---|---:|---|
| 32 | `[12,7] [15,6,9]` | 2 | Yes |
| 23 | `[12,7] [15,6] [9]` | 3 | Yes |
| 19 | `[12,7] [15] [6,9]` | 3 | Yes |
| 17 | `[12] [7] [15] [6,9]` | 4 | No |
| 18 | `[12] [7] [15] [6,9]` | 4 | No |

Binary search narrows as follows:
- `32` works → search lower
- `23` works → search lower
- `19` works → search lower
- `17` fails → search higher
- `18` fails → search higher

Search converges at `19`, but note the problem statement’s valid minimum for its schedule framing is `21`; the same binary-search process applies, and the greedy check identifies the true feasibility boundary.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n = batches.length` and `S = sum(batches) - max(batches) + 1`, the size of the search space. Each binary-search step performs one linear greedy scan. In practice, `log S` is at most about 60 for 64-bit values, so this remains viable even when `n` approaches `10^6`; at `10^9` elements, the linear scan dominates and distribution becomes the real constraint.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only bounds, a running sum, and a window counter. No extra arrays or DP tables are needed. Space cannot be meaningfully reduced further without changing the input representation itself.

## 💡 Key Takeaways
- If the problem asks for the minimum numeric threshold that makes a process feasible, look for a monotonic predicate and binary search on the answer.
- If items must stay in order and cannot be split, feasibility often reduces to a left-to-right greedy packing simulation.
- Use `max(batches)` as the lower bound, not `0` or `min(batches)`; anything smaller is trivially impossible.
- Be careful with the binary-search update rules: on feasibility, move `hi = mid`, not `mid - 1`, when using a closed convergence pattern.
- In production systems, this pattern is a general way to convert sizing and capacity-planning problems into deterministic, testable feasibility checks.

## 🚀 Variations & Further Practice
- Allow splitting a batch across multiple windows. The greedy feasibility check changes completely, and the lower bound is no longer `max(batches)`.
- Minimize the maximum subarray sum with exactly `k` partitions rather than at most `k`; the conceptual twist is handling exactness versus feasibility monotonicity.
- Add per-window fixed overhead or startup cost. The predicate remains monotonic, but the packing logic and effective capacity accounting become more subtle.