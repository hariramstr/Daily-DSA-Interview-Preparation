# Minimum Energy to Process a Sensor Queue

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `load` and a non-negative batching `penalty`, compute the minimum energy required to process all packets in order. Each step can process either one packet for `load[i]` or two consecutive packets for `max(load[i], load[i+1]) + penalty`. Every packet must be covered exactly once, with no overlap. The non-trivial part is that each local choice affects the remaining suffix, so locally attractive batching decisions can produce globally suboptimal totals.

## 🌍 Engineering Impact
This pattern shows up in streaming systems, packet schedulers, storage compaction, and micro-batching pipelines where adjacent work items can be merged with a fixed coordination cost. Examples include Kafka-like consumer batching, GPU kernel launch amortization, log shipping, and edge-device telemetry processing. At scale, greedy heuristics leave money on the table because fixed overhead interacts with uneven item costs. Dynamic programming gives a predictable global optimum over ordered workloads, which matters when cost models drive autoscaling, battery life, throughput targets, or SLO-aware admission control. The core architectural lesson: once work must remain ordered, optimization usually becomes a prefix-state problem, not a local ranking problem.

## 🔍 Problem Statement
You are given an integer array `load` where `load[i]` is the energy cost of processing packet `i` individually, and a non-negative integer `penalty` representing the overhead of batching. The device must process packets strictly in queue order, covering every packet exactly once.

Allowed operations:

- Process packet `i` alone for cost `load[i]`
- Process packets `i` and `i+1` together for cost `max(load[i], load[i+1]) + penalty`

Batches cannot overlap, and no packet may be skipped or processed twice.

Return the minimum total energy required.

Constraints:

- `1 <= load.length <= 100000`
- `0 <= load[i] <= 1000000000`
- `0 <= penalty <= 1000000000`
- The answer fits in a signed 64-bit integer

Examples:

- `load = [4, 7, 2, 9], penalty = 1` → `19`
- `load = [5, 1, 5, 1], penalty = 0` → `10`

The key constraint is the input size: `n` can be `100000`, so exponential search or quadratic DP is not viable.

## 🪜 How to Solve This
1. Read the problem → notice the queue order is fixed. You are not selecting arbitrary pairs; you are deciding how to partition the array into blocks of size 1 or 2.

2. Once the problem becomes “partition a prefix optimally,” dynamic programming is the natural fit. The cost of finishing at position `i` depends only on whether the last decision used one packet or two.

3. Define the state on prefixes: let `dp[i]` be the minimum energy to process the first `i` packets. That immediately gives a clean recurrence.

4. For packet `i-1`, there are only two valid endings:
   - process it alone → come from `dp[i-1]`
   - batch packets `i-2` and `i-1` → come from `dp[i-2]`

5. That yields:
   - `dp[i] = min(dp[i-1] + load[i-1], dp[i-2] + max(load[i-2], load[i-1]) + penalty)`

6. Because each state depends only on the previous two states, you can compute left to right in one pass and even compress space to `O(1)`.

The reason this is the right approach is simple: the problem has optimal substructure over prefixes and only constant-width transitions.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: 1D Dynamic Programming over prefixes.**  
   The queue must remain in order, and each decision consumes either one or two consecutive packets. That is the classic signal for prefix DP: solve the best answer for the first `i` items, then extend.

2. **Define the state.**  
   Let `dp[i]` be the minimum energy needed to process the first `i` packets, meaning indices `[0, i-1]`. This indexing avoids awkward boundary handling when considering the last one or two packets.

3. **Initialize base cases.**  
   `dp[0] = 0` because processing zero packets costs nothing.  
   `dp[1] = load[0]` because a single packet can only be processed alone.  
   These base cases anchor all later transitions.

4. **Apply the recurrence for each prefix `i >= 2`.**  
   Two valid ways to end the first `i` packets:
   - Last packet alone: `dp[i-1] + load[i-1]`
   - Last two packets batched: `dp[i-2] + max(load[i-2], load[i-1]) + penalty`

   So:
   `dp[i] = min(...)`

5. **Maintain the invariant.**  
   After computing `dp[i]`, it is the true minimum for the first `i` packets. This holds because every legal plan must end in exactly one of the two allowed terminal shapes, and both predecessors are already optimal by induction.

6. **Optimize space if desired.**  
   Since `dp[i]` depends only on `dp[i-1]` and `dp[i-2]`, store just two rolling values. The abstraction remains the same; only the memory footprint changes.

This is the right abstraction because the problem is not about choosing packets globally; it is about choosing the cheapest legal suffix extension of an already-optimized prefix.

## 📊 Worked Example
Take `load = [4, 7, 2, 9]`, `penalty = 1`.

Let `dp[i]` = minimum cost for first `i` packets.

| i | Prefix covered | Alone option | Batch option | dp[i] |
|---|----------------|--------------|--------------|-------|
| 0 | `[]`           | —            | —            | 0     |
| 1 | `[4]`          | `0 + 4`      | —            | 4     |
| 2 | `[4,7]`        | `4 + 7 = 11` | `0 + max(4,7)+1 = 8` | 8 |
| 3 | `[4,7,2]`      | `8 + 2 = 10` | `4 + max(7,2)+1 = 12` | 10 |
| 4 | `[4,7,2,9]`    | `10 + 9 = 19`| `8 + max(2,9)+1 = 18` | 18 |

So the minimum is `18`, achieved by batching `(0,1)` for `8` and `(2,3)` for `10`.

This trace makes the DP intuition explicit: each row asks only how the final one or two packets should be handled.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = load.length`. Each packet participates in a constant amount of work: one individual-cost transition and, from the second packet onward, one batch-cost transition. At `10^6` elements this is routine in production code; at `10^9`, even linear scans become infrastructure decisions rather than algorithmic details.

### Space Complexity
`O(n)` with a full DP array, or `O(1)` with rolling state because each transition uses only the previous two results. The reducible space belongs entirely to the DP state; the trade-off is losing the full table, which only matters if you need to reconstruct the actual batching plan.

## 💡 Key Takeaways
- If choices consume adjacent items in fixed-size chunks and the order cannot change, think prefix dynamic programming immediately.
- If a greedy local merge rule sounds plausible but the problem asks for a global minimum over the full sequence, assume DP until proven otherwise.
- Be precise about indexing: `dp[i]` should represent the first `i` packets, not packet index `i`, or the recurrence becomes error-prone.
- Use 64-bit arithmetic throughout; `load[i]`, `penalty`, and cumulative sums can exceed 32-bit range easily.
- In production systems, fixed-overhead batching problems are usually sequence-partitioning problems; optimizing them correctly requires modeling the boundary cost, not just ranking local opportunities.

## 🚀 Variations & Further Practice
- Allow batches of size up to `k` instead of only 2. The twist is the transition fan-out grows, and you need a more general recurrence over the last `1..k` packets.
- Add a limit on the number of batches allowed. This turns the problem into DP over both prefix length and batch count.
- Make the batch cost depend on aggregate statistics such as `sum`, `max - min`, or a nonlinear penalty. The harder part is preserving fast transitions while the cost model becomes more expressive.