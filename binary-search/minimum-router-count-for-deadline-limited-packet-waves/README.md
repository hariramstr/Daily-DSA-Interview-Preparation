# Minimum Router Count for Deadline-Limited Packet Waves

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Scheduling

---

## 🗂 Problem Overview
Given packet counts `packets[i]` arriving at time `i` and non-decreasing deadlines `deadline[i]`, compute the minimum number of identical routers needed to finish every wave on time. Each router processes one packet per unit time, processing is preemptive, and no work can start before arrival. The challenge is not total work alone, but whether enough capacity exists inside every deadline-bounded time window. Feasibility is monotonic in `k`, which makes binary search viable.

## 🌍 Engineering Impact
This pattern shows up in deadline-constrained compute admission: stream processors draining micro-batches, NIC or firewall pipelines handling bursty traffic, warehouse job schedulers, and multi-tenant background workers with SLA cutoffs. At scale, aggregate throughput is a misleading metric; the real failure mode is local overload inside tight windows after release times. Without a feasibility test over cumulative demand versus time-bounded capacity, systems either overprovision everywhere or miss deadlines under bursty arrivals. This approach enables exact capacity sizing, predictable SLA enforcement, and fast “what-if” planning under changing workloads.

## 🔍 Problem Statement
You are given two arrays of length `n`:

- `packets[i]`: packets in wave `i`
- `deadline[i]`: latest time by which wave `i` must be fully processed

Wave `i` arrives at time `i`, so its packets cannot be processed before then. There are `k` identical routers, each processing exactly `1` packet per unit time. Routers are preemptive and may switch waves arbitrarily, but total processing rate at any time is at most `k`.

Find the minimum integer `k` such that all waves can be completed by their deadlines.

Key constraints:

- `1 <= n <= 2 * 10^5`
- `1 <= packets[i] <= 10^9`
- `i <= deadline[i] <= 10^9`
- `deadline` is non-decreasing

Examples:

- `packets = [3,2,4], deadline = [2,3,5]` → `2`
- `packets = [5,6,4], deadline = [1,2,2]` → `8`

The decisive constraint is large `n` plus huge values, which rules out time simulation and points to binary search on the answer with a linear feasibility check.

## 🪜 How to Solve This
1. Start from the monotonicity signal: if `k` routers can meet all deadlines, then `k+1` routers can too. That immediately suggests binary search on the minimum feasible `k`.

2. Now ask what “feasible for a fixed `k`” really means. Because processing is preemptive, exact wave-to-router assignment does not matter; only cumulative capacity over time matters.

3. For any deadline `d`, all waves `0..i` with `deadline[i] = d` must be finished by time `d`. So by that time, required work is the prefix sum of packets, and available capacity is not just `k * (d + 1)`.

4. Release times matter. Work from wave `j` only becomes available at time `j`, so within prefix `0..i`, the maximum processable work by time `deadline[i]` is `k * Σ(max(0, deadline[i] - j + 1))` over `j <= i`.

5. Because `deadline` is non-decreasing, that sum simplifies for prefix `0..i`: every wave `j <= i` is available by `deadline[i]`, so capacity becomes `k * ((i + 1) * (deadline[i] + 1) - Σj)`.

6. Rearranging gives a per-prefix lower bound on `k`. The answer is the maximum such bound across all prefixes, so binary search is optional; direct computation is enough. Still, the intended hard pattern is binary search + greedy feasibility.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix work.**  
   Maintain `work += packets[i]` while scanning left to right. This is the total demand that must be completed for waves `0..i` by time `deadline[i]`.  
   **Invariant:** after index `i`, `work` equals required cumulative processing for the current prefix.

2. **Model prefix capacity under release times.**  
   For prefix `0..i`, wave `j` can only run during times `j..deadline[i]`, giving `deadline[i] - j + 1` time units per router. Summing over all `j <= i` yields per-router capacity:
   `cap1 = Σ(deadline[i] - j + 1) = (i + 1)(deadline[i] + 1) - i(i + 1)/2`.  
   **Why correct:** preemption makes only aggregate available processing slots matter.

3. **Feasibility check for fixed `k` (Greedy capacity test).**  
   Prefix `0..i` is feasible iff `work <= k * cap1`. If any prefix fails, no schedule exists for this `k`.  
   **Invariant:** every checked prefix satisfies Hall-like cumulative demand ≤ cumulative service capacity.

4. **Exploit monotonicity (Binary Search pattern).**  
   Since feasibility only improves as `k` increases, binary search over `k` is valid. Use `1` as low bound and a doubling strategy for the high bound until feasible, then binary search.  
   **Why this abstraction fits:** the search space is numeric and monotone; the check is linear.

5. **Direct bound interpretation.**  
   Each prefix implies `k >= ceil(work / cap1)`. The minimum feasible `k` is the maximum of these bounds. This collapses the binary search into one pass, but the binary-search framing remains the reusable pattern for less structured variants.

## 📊 Worked Example
Take `packets = [5, 6, 4]`, `deadline = [1, 2, 2]`.

| i | packets[i] | deadline[i] | prefix work | per-router capacity by deadline | required `k` |
|---|------------|-------------|-------------|----------------------------------|--------------|
| 0 | 5          | 1           | 5           | `(1)*(2) - 0 = 2`                | `ceil(5/2)=3` |
| 1 | 6          | 2           | 11          | `(2)*(3) - 1 = 5`                | `ceil(11/5)=3` |
| 2 | 4          | 2           | 15          | `(3)*(3) - 3 = 6`                | `ceil(15/6)=3` |

Trace:

1. By time `1`, one router can process wave `0` for times `0,1` → `2` units.
2. By time `2`, one router has `2` slots for wave `0`, `2` for wave `1`, and `1` for wave `2`? No — because time is shared globally, the correct aggregate is `3 + 2 + 1 = 6` slots across release windows.
3. Maximum lower bound is `3`, so `k = 3`.

## ⏱ Complexity Analysis
### Time Complexity
With binary search, time is `O(n log A)`, where `A` is the answer range, because each feasibility check scans all waves once. With the closed-form prefix bound, it becomes `O(n)`. At `10^6` scale this is practical; anything quadratic is dead on arrival, and time simulation over `10^9` deadlines is impossible.

### Space Complexity
Space is `O(1)` beyond the input if you stream prefix sums and compute capacities on the fly. No heap, DP table, or schedule reconstruction is required. You could store prefix arrays for debugging or proofs, but that only adds `O(n)` without improving asymptotics.

## 💡 Key Takeaways
- Monotone “minimum resource to satisfy all constraints” problems are strong binary-search-on-answer candidates.
- When processing is preemptive, stop thinking about assignments and start thinking about cumulative demand versus cumulative capacity.
- The main trap is using `k * (deadline[i] + 1)` and forgetting release times; early capacity cannot process future arrivals.
- Be precise about inclusive time bounds: arrival at `i` and deadline `d` gives `d - i + 1` usable unit intervals, not `d - i`.
- In production schedulers, exact capacity planning often reduces to proving every critical prefix or window is serviceable, not to constructing the runtime schedule itself.

## 🚀 Variations & Further Practice
- Add per-wave weights or priorities and ask for the minimum weighted deadline violation under fixed `k`; feasibility becomes optimization, not pure monotone checking.
- Allow arbitrary, non-monotone deadlines; now prefixes are insufficient and the feasibility test needs a stronger EDF-style or interval-demand argument.
- Introduce heterogeneous routers with different rates or availability windows; binary search may still apply, but the check becomes a resource-allocation problem rather than a simple cumulative inequality.