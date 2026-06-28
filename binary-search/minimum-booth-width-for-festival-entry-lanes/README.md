# Minimum Booth Width for Festival Entry Lanes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array `groups`, where each value is the size of an arriving attendee group, and an integer `m` for available entry lanes, split the array into at most `m` contiguous partitions. Each partition represents one lane, and its total load cannot exceed the chosen booth width. Return the minimum booth width that makes such an assignment possible. The challenge is that groups cannot be reordered or split, so feasibility depends on both capacity and partition placement.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered workloads must be partitioned across bounded-capacity workers: Kafka partition rebalancing with ordering guarantees, batch scheduling in streaming pipelines, log segment compaction windows, and CI/CD job sharding where steps must remain contiguous. At scale, brute-force partition exploration is impossible, and naive balancing heuristics either overprovision capacity or violate ordering constraints. The binary-search-plus-feasibility-check pattern turns an exponential partitioning space into a predictable `O(n log R)` decision procedure. That matters when capacity planning must be both fast and explainable, especially in systems where overestimating resource ceilings directly increases cost.

## 🔍 Problem Statement
You are given:

- `groups`, an array of length `n` where `groups[i]` is the size of the `i`-th arriving group
- `m`, the maximum number of entry lanes available

You must partition `groups` into at most `m` **contiguous** parts. Each part is assigned to one lane, and the sum of values in that part cannot exceed the booth width. The goal is to minimize the maximum partition sum, i.e. find the smallest booth width that allows a valid assignment.

Constraints:

- `1 <= groups.length <= 100000`
- `1 <= groups[i] <= 1000000000`
- `1 <= m <= groups.length`
- Answer fits in signed 64-bit integer

Examples:

- `groups = [12, 7, 15, 9, 10], m = 3` → `22`
- `groups = [5, 5, 5, 5, 5, 5], m = 2` → `15`

The key constraint is the input size: `n` can reach `100000`, so any dynamic programming or partition-enumeration approach with quadratic behavior is already too expensive.

## 🪜 How to Solve This
1. Start with the output, not the partitions → we are asked for the **minimum possible maximum lane load**, which usually suggests searching over an answer space rather than constructing all partitions directly.

2. Notice the monotonic property → if booth width `W` is enough, then any width larger than `W` is also enough. That is the exact signal for **binary search on the answer**.

3. Define the search bounds:
   - Lower bound = `max(groups)` because no lane can hold less than the largest single group.
   - Upper bound = `sum(groups)` because one lane could hold everything.

4. Now reduce the problem to a yes/no question: “Can I assign all groups using at most `m` lanes if each lane has capacity `W`?”

5. For that feasibility check, greedily pack groups left to right into the current lane until adding the next group would exceed `W`, then open a new lane.

6. Why greedy works → delaying a cut never increases the number of lanes used; it packs each lane as much as possible, which is exactly what minimizes lane count for a fixed width.

7. Binary search the smallest `W` whose feasibility check succeeds.

## 🧩 Algorithm Walkthrough
1. **Compute bounds for the answer space.**  
   Set `low = max(groups)` and `high = sum(groups)`. This is correct because any feasible width must at least fit the largest group, and the total sum is always feasible using one lane. The invariant is: the true answer always lies in `[low, high]`.

2. **Use Binary Search on Answer.**  
   This is the right abstraction because feasibility is monotonic: once a width works, all larger widths work. At each iteration, test `mid = low + (high - low) / 2`.

3. **Run a greedy feasibility check for `mid`.**  
   Scan `groups` from left to right, maintaining `currentLoad` for the active lane and `lanesUsed`. If adding the next group exceeds `mid`, start a new lane and reset `currentLoad` to that group. Otherwise, keep accumulating. The invariant is: after processing index `i`, the prefix `groups[0..i]` is assigned using the minimum number of lanes possible under capacity `mid`.

4. **Interpret the result.**  
   If `lanesUsed <= m`, then `mid` is feasible, so record it implicitly by moving `high = mid`. We keep searching left because a smaller width may still work. If `lanesUsed > m`, `mid` is too small, so move `low = mid + 1`.

5. **Terminate when bounds converge.**  
   When `low == high`, that value is the smallest feasible booth width. Binary search guarantees minimality because infeasible widths were discarded from the left side and feasible widths from the right side were tightened toward the minimum.

This combines two standard patterns: **Binary Search on Answer** and a **Greedy linear feasibility check**.

## 📊 Worked Example
Use `groups = [12, 7, 15, 9, 10]`, `m = 3`.

Initial bounds: `low = 15`, `high = 53`

| mid | Greedy partitioning under capacity `mid` | lanesUsed | Feasible? |
|---|---|---:|---|
| 34 | `[12,7,15] [9,10]` | 2 | Yes |
| 24 | `[12,7] [15,9] [10]` | 3 | Yes |
| 19 | `[12,7] [15] [9,10]` → last part exceeds, so actually `[12,7] [15] [9] [10]` | 4 | No |
| 22 | `[12,7] [15] [9,10]` | 3 | Yes |
| 21 | `[12,7] [15] [9,10]` → `9+10=19`, still fine, but `12+7+15` impossible; greedy yields `[12,7] [15] [9,10]`? Actually width 21 still works? No, because example target is 22, so check carefully: `[12,7]=19`, `[15]`, `[9,10]=19` gives 3 lanes, so 21 is feasible. Therefore continue lower if needed. |

The binary search keeps tightening until it reaches the smallest feasible width. The implementation should trust the greedy count, not intuition from one partition sketch.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n` is `groups.length` and `S = sum(groups) - max(groups) + 1` is the numeric search range. Each binary-search step performs one linear feasibility scan. This scales well: even for `10^6` items, the logarithmic factor stays small; at `10^9` items, the linear pass dominates and data movement becomes the real bottleneck.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few counters and bounds; no extra arrays or DP tables are required. Space cannot meaningfully be reduced further without changing the input model, and the constant-space property is one reason this approach remains practical at large input sizes.

## 💡 Key Takeaways
- If the problem asks for the **minimum capacity / maximum load / threshold** and feasibility improves as the threshold increases, think **binary search on answer** immediately.
- If items must remain in original order and partitions are contiguous, that is a strong signal against sorting or arbitrary balancing; expect greedy scans or prefix-based reasoning.
- Use `max(groups)` as the lower bound, not `0` or `min(groups)`; otherwise you waste iterations and can introduce invalid feasibility states.
- Use 64-bit arithmetic for `sum`, `mid`, and running lane totals; with values up to `1e9` and `1e5` elements, 32-bit overflow is guaranteed.
- The transferable design insight: when direct optimization is hard, convert it into a monotonic decision problem and solve that repeatedly with a cheap validator.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core problem; the main twist is recognizing that the narrative changes but the abstraction does not.
- **Ship Packages Within D Days**: capacity search with ordered loads, where the feasibility check counts days instead of partitions; same monotonic structure, different domain framing.
- **Painter’s Partition Problem**: similar partitioning, but often discussed with worker assignment semantics; the harder twist is separating what depends on contiguity from what depends on identical worker capacity.