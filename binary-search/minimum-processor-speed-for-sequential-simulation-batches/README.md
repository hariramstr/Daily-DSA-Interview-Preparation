# Minimum Processor Speed for Sequential Simulation Batches

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Prefix Sum

---

## 🗂 Problem Overview
Given ordered simulation batches with workloads and per-prefix deadlines, compute the smallest positive integer processor speed `S` such that running the batches sequentially finishes every batch by its deadline. Batch `i` takes `ceil(workloads[i] / S)` minutes, and finish times are cumulative. The problem is non-trivial because workloads and deadlines are huge, order is fixed, and brute-forcing speeds is impossible; feasibility must be tested efficiently and monotonically.

## 🌍 Engineering Impact
This pattern shows up anywhere throughput must be provisioned against ordered latency commitments: CI/CD job runners, GPU inference queues, ETL stages, media transcoding pipelines, and single-threaded replay or backfill systems. The operational question is rarely “can we finish eventually?” but “what minimum capacity guarantees every checkpoint SLA?” Without a monotone feasibility model plus binary search on capacity, teams either overprovision permanently or ship brittle heuristics that fail under large inputs. This approach enables deterministic sizing, fast what-if analysis, and safe admission-control decisions at production scale.

## 🔍 Problem Statement
You are given two arrays of length `n`: `workloads[i]` and `deadlines[i]`. Batches must execute in the given order on one processor, with no reordering or preemption. At integer speed `S > 0`, batch `i` requires `ceil(workloads[i] / S)` minutes. Let `finish[i]` be the cumulative time to complete batches `0..i`. A speed is feasible iff `finish[i] <= deadlines[i]` for every `i`.

Return the minimum feasible integer speed, or `-1` if no finite speed works.

Constraints:
- `1 <= n <= 200000`
- `1 <= workloads[i] <= 10^12`
- `1 <= deadlines[i] <= 10^18`

Examples:

- `workloads = [7, 11, 5], deadlines = [4, 8, 10]` → `3`
- `workloads = [9, 9, 9], deadlines = [0, 5, 8]` → `-1`

The key algorithmic driver is the huge search space for `S`: direct simulation across candidate speeds is infeasible.

## 🪜 How to Solve This
1. Read the constraints → `S` can be very large, so searching linearly over speeds is dead on arrival.

2. Inspect feasibility as speed changes → if speed `S` works, any larger speed also works because every term `ceil(workloads[i] / S)` stays the same or decreases. That monotonicity is the signal for binary search on the answer.

3. Define a feasibility check → simulate batches in order, accumulate total time, and fail immediately if any prefix exceeds `deadlines[i]`.

4. Ask whether impossibility can be detected early → yes. Even at “infinite” speed, each batch still takes at least 1 minute, so prefix `i` needs at least `i + 1` minutes. If `deadlines[i] < i + 1` for any `i`, return `-1`.

5. Bound the search space → `1` is the minimum speed. A safe upper bound is `max(workloads)`: at that speed, every batch takes exactly `1` minute, which is the fastest any finite integer speed can achieve.

6. Binary search the smallest feasible speed using the simulation as the predicate. Use 64-bit or wider arithmetic carefully for cumulative time and ceil division.

## 🧩 Algorithm Walkthrough
1. **Pre-check impossible prefixes.**  
   For each index `i`, verify `deadlines[i] >= i + 1`. This is necessary because every batch consumes at least one whole minute for any finite positive integer speed. If this fails, no answer exists.  
   **Invariant:** after this pass, the instance is not trivially impossible.

2. **Choose the search interval.**  
   Set `lo = 1`, `hi = max(workloads)`. At speed `hi`, each batch duration is `ceil(workloads[i] / hi) = 1` since `workloads[i] <= hi`, so total prefix time is exactly `i + 1`. Because the pre-check passed, `hi` is feasible.  
   **Invariant:** the minimum feasible speed lies in `[lo, hi]`.

3. **Use Binary Search on Answer.**  
   Compute `mid = lo + (hi - lo) / 2`. This pattern applies because feasibility is monotone: increasing speed never increases any batch duration.  
   **Invariant:** if `mid` is feasible, the answer is in `[lo, mid]`; otherwise it is in `[mid + 1, hi]`.

4. **Feasibility test via greedy prefix simulation.**  
   Scan batches in order, compute `time_i = (workloads[i] + mid - 1) / mid`, add to `elapsed`, and compare `elapsed` to `deadlines[i]`. Return false on first violation.  
   This is greedy only in the sense that order is fixed and there is no decision to defer; the only correct check is prefix-by-prefix accumulation.  
   **Invariant:** after processing index `i`, `elapsed` equals the exact finish time of batch `i` at speed `mid`.

5. **Terminate at the first feasible point.**  
   Standard lower-bound binary search returns the smallest speed satisfying the predicate.

## 📊 Worked Example
Use `workloads = [7, 11, 5]`, `deadlines = [4, 8, 10]`.

| Speed `S` | Batch times `ceil(w/S)` | Prefix finish times | Feasible? |
|---|---:|---:|---:|
| 1 | `[7, 11, 5]` | `[7, 18, 23]` | No |
| 2 | `[4, 6, 3]` | `[4, 10, 13]` | No |
| 3 | `[3, 4, 2]` | `[3, 7, 9]` | Yes |
| 4 | `[2, 3, 2]` | `[2, 5, 7]` | Yes |

Binary search trace:
1. `lo=1, hi=11` → `mid=6`, feasible.
2. `lo=1, hi=6` → `mid=3`, feasible.
3. `lo=1, hi=3` → `mid=2`, infeasible.
4. `lo=3, hi=3` → stop.

Answer: `3`.

The useful mental model is that each speed induces a deterministic prefix-time profile; binary search finds the first profile entirely under the deadline envelope.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `M = max(workloads)`. Each binary-search step performs one linear feasibility scan, and there are `log M` steps. With `n = 2e5` and `M <= 1e12`, this is roughly `2e5 * 40`, which is operationally cheap. At `10^9` elements, the scan dominates and the approach becomes bandwidth-bound, not search-bound.

### Space Complexity
`O(1)` auxiliary space beyond the input arrays. The algorithm stores a few counters and bounds; no heap structures or prefix arrays are required. You could precompute nothing and still retain full correctness, which is the right trade-off here.

## 💡 Key Takeaways
- If the question asks for the minimum capacity, speed, rate, or threshold that makes all constraints pass, check whether feasibility is monotone and binary-search the answer.
- Prefix deadlines with fixed execution order are a strong signal that the feasibility predicate is a single left-to-right cumulative scan.
- The impossibility check is easy to miss: even infinite speed cannot make a batch take less than 1 minute, so `deadlines[i] < i + 1` means immediate `-1`.
- Use safe ceil division and wide integers: `(w + S - 1) / S` can overflow in narrow types, and cumulative finish times can exceed 32-bit range quickly.
- In production sizing problems, the win is not just asymptotic speed; it is converting capacity planning into a deterministic monotone predicate with a provable minimal answer.

## 🚀 Variations & Further Practice
- Allow reordering of batches to minimize required speed. The twist is that feasibility is no longer a simple prefix scan; scheduling order becomes part of the optimization.
- Replace hard deadlines with a budget on the number of missed deadlines. The predicate changes from universal prefix validity to constrained violations, often requiring DP or more complex greedy structure.
- Extend to `k` identical processors with non-preemptive assignment. Monotonicity may remain, but feasibility becomes a parallel scheduling problem rather than a single cumulative timeline.