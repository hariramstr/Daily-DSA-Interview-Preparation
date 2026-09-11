# Minimum Router Delay for Sequential Packet Waves

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Math

---

## 🗂 Problem Overview
Given an array `packets`, where each element is the size of a packet wave, and an integer `maxTime`, find the smallest positive integer processing rate `d` such that transmitting all waves sequentially finishes within `maxTime` seconds. Each wave takes `ceil(packets[i] / d)` seconds. The challenge is not simulation but choosing the minimum feasible rate under large constraints: up to `10^5` waves, packet counts up to `10^9`, and `maxTime` up to `10^14`.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the minimum capacity that satisfies a latency or throughput SLO under discrete batching effects. Examples include rate-limited API gateways, storage compaction schedulers, streaming backfill jobs, distributed worker pool sizing, and network shapers that process bursts sequentially. At scale, brute-force tuning is operationally useless: the search space is too large and the cost function is expensive to evaluate. Monotonic feasibility lets you replace iterative guesswork with deterministic capacity planning, bounded runtime, and predictable behavior under changing workloads.

## 🔍 Problem Statement
You are given:

- `packets[i]`: the number of packets in the `i`-th wave
- `maxTime`: the maximum total time allowed to transmit all waves

A router processes at rate `d` packets per second while a wave is active. Since transmission is counted in whole seconds, wave `i` requires:

`ceil(packets[i] / d)`

seconds. Waves are processed strictly in order, one after another. The goal is to return the minimum positive integer `d` such that the sum of all wave times is at most `maxTime`.

Constraints:

- `1 <= packets.length <= 100000`
- `1 <= packets[i] <= 10^9`
- `packets.length <= maxTime <= 10^14`
- The answer always exists

Examples:

- `packets = [8, 4, 10], maxTime = 8` → `4`
- `packets = [30, 11, 23, 4, 20], maxTime = 10` → `11`

The key constraint is the huge search range for `d`, which rules out linear probing and points directly to binary search on the answer.

## 🪜 How to Solve This
1. Read the objective carefully → we are not asked for total time at a fixed rate; we are asked for the **minimum rate** that satisfies a time budget.

2. Ask what happens as `d` changes → if router capacity increases, `ceil(packets[i] / d)` never increases for any wave. Total time is therefore monotonic non-increasing.

3. Monotonic feasibility usually means **binary search on the answer** → define a predicate: “Can all waves finish within `maxTime` if the rate is `d`?”

4. Work out how to test one candidate `d` → scan the array once, summing  
   `ceil(packets[i] / d)` for every wave. If the sum is `<= maxTime`, `d` is feasible.

5. Choose search bounds → the minimum possible rate is `1`; the maximum needed is `max(packets)`, because at that rate every wave finishes in one second, and `maxTime >= packets.length`.

6. Binary search for the first feasible value → if `mid` works, try smaller; if it fails, go larger.

That chain gets you from problem statement to implementation without inventing any custom data structure.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer.**  
   The search space is not array indices but integer capacities `d`. The feasibility function  
   `f(d) = totalTime(d) <= maxTime` is monotonic: once true, it stays true for all larger `d`. That is exactly the abstraction binary search needs.

2. **Set the search interval.**  
   Initialize `left = 1` and `right = max(packets)`.  
   Why this upper bound works: if `d = max(packets)`, each wave takes exactly one second, so total time is `packets.length`, which is guaranteed to be `<= maxTime`.

3. **Evaluate a candidate rate `mid`.**  
   Compute total transmission time as:  
   `sum((p + mid - 1) / mid)` using integer arithmetic.  
   This is the standard ceiling-division identity and avoids floating-point error.

4. **Maintain the binary-search invariant.**  
   Invariant: all values below `left` are known infeasible; all values above `right` are either unknown or feasible depending on the variant, but the search always preserves the existence of an answer in `[left, right]`.  
   If `mid` is feasible, move `right = mid`; otherwise move `left = mid + 1`.

5. **Terminate at the first feasible capacity.**  
   Use the lower-bound form of binary search: loop while `left < right`. When the loop ends, `left == right`, and that value is the smallest feasible `d`.

6. **Short-circuit where useful.**  
   During feasibility checking, if accumulated time already exceeds `maxTime`, stop early. This does not change correctness and improves constant factors on failing candidates.

## 📊 Worked Example
Example: `packets = [30, 11, 23, 4, 20]`, `maxTime = 10`

| Step | left | right | mid | Total Time at `mid` | Feasible? |
|---|---:|---:|---:|---:|---|
| 1 | 1 | 30 | 15 | `2+1+2+1+2 = 8` | Yes |
| 2 | 1 | 15 | 8 | `4+2+3+1+3 = 13` | No |
| 3 | 9 | 15 | 12 | `3+1+2+1+2 = 9` | Yes |
| 4 | 9 | 12 | 10 | `3+2+3+1+2 = 11` | No |
| 5 | 11 | 12 | 11 | `3+1+3+1+2 = 10` | Yes |

Now `left = right = 11`, so the minimum feasible router capacity is `11`.

The search converges because each step removes half the remaining capacity range while preserving the first valid answer.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log M)`, where `n = packets.length` and `M = max(packets)`. Each binary-search step scans the array once to evaluate feasibility, and there are `log M` such steps. In practice, even with `n = 10^5` and `M = 10^9`, this is only about 30 full passes, which is operationally cheap compared with linear search over capacities.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only search bounds and a running time sum; no extra arrays or heap structures are required. Space cannot be meaningfully reduced further unless you trade away readability or integer-safety checks.

## 💡 Key Takeaways
- If the problem asks for the **minimum integer value** that satisfies a constraint, check whether feasibility becomes monotonic as the candidate value increases.
- When the answer range is huge but each candidate can be validated in one pass, think **binary search on the answer**, not brute-force enumeration.
- Use integer ceiling division: `(p + d - 1) / d`; floating-point `ceil(p / d)` is unnecessary and easier to get wrong.
- Use the lower-bound binary-search form (`while left < right`, shrink toward first feasible value) to avoid off-by-one errors around the minimum valid `d`.
- In production capacity planning, monotonic feasibility transforms expensive tuning into a deterministic search problem with predictable runtime and clear correctness guarantees.

## 🚀 Variations & Further Practice
- Allow waves to be split across multiple routers in parallel. The twist is that feasibility may require a scheduling strategy, not just a direct sum, so the predicate becomes more complex.
- Replace whole-second rounding with per-wave setup costs or nonlinear processing penalties. The harder part is proving whether monotonicity still holds and whether binary search remains valid.
- Optimize for the minimum `d` under multiple constraints simultaneously, such as total time and peak energy budget. The challenge shifts from a single monotone predicate to multi-dimensional feasibility.