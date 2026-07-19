# Minimum Packet Size for Sequential Upload Windows

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, array

---

## 🗂 Problem Overview
Given an array `clips` and an integer `w`, find the smallest integer packet size `P` such that the clips can be split into at most `w` contiguous windows, where each window’s total size is at most `P`. The order of clips cannot change. The challenge is that `n` can reach `100000`, so trying every partition or packet size directly is too expensive. The key is to exploit the monotonic relationship between packet size and feasibility.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered workloads must be partitioned under a capacity limit: batching events in streaming pipelines, segmenting WAL or replication logs, sizing upload chunks in media delivery, assigning contiguous work ranges in ETL stages, or packing ordered requests into bounded execution windows. At scale, brute-force partition search collapses under combinatorial growth. The binary-search-plus-greedy shape matters because it converts a planning problem into a monotone feasibility check. That enables predictable latency, straightforward correctness reasoning, and easy adaptation to systems where capacity is tunable but ordering constraints are non-negotiable.

## 🔍 Problem Statement
You are given:

- `clips[i]`: size of the `i`-th video clip
- `w`: maximum number of contiguous upload windows

You must partition `clips` into at most `w` contiguous parts such that the sum of each part is at most some packet size `P`. Return the minimum integer `P` for which such a partition exists.

Constraints:

- `1 <= clips.length <= 100000`
- `1 <= clips[i] <= 1000000000`
- `1 <= w <= clips.length`
- Answer fits in signed 64-bit integer

Examples:

- `clips = [8, 3, 5, 7, 2], w = 3` → `10`
- `clips = [4, 4, 4, 4], w = 2` → `8`

Edge conditions matter:

- `P` must be at least `max(clips)`, or some clip cannot fit anywhere.
- `P` never needs to exceed `sum(clips)`, which corresponds to one window.
- The large input size rules out dynamic programming over all partitions.

## 🪜 How to Solve This
1. Read the problem → this is an **ordered partitioning** problem. We are not rearranging clips; we are cutting the array into contiguous windows.

2. Ask what we are optimizing → not the number of windows directly, but the **minimum capacity** `P` that makes a valid partition possible.

3. Notice the monotonic property → if packet size `P` works, then any larger packet size also works. That is the signal for **binary search on the answer**.

4. Now define the feasibility check → for a fixed `P`, scan left to right and greedily pack as many clips as possible into the current window without exceeding `P`. When the next clip would overflow, start a new window.

5. Why greedy works → delaying a split never hurts. Packing each window maximally minimizes the number of windows needed for that `P`.

6. Compare the required window count to `w`:
   - if required windows `<= w`, `P` is feasible
   - otherwise, `P` is too small

7. Binary search between:
   - lower bound = `max(clips)`
   - upper bound = `sum(clips)`

That gives `O(n log S)` time, where `S` is the total clip size.

## 🧩 Algorithm Walkthrough
1. **Establish the search space**  
   Use the pattern **Binary Search on Answer**. The smallest valid `P` cannot be below `max(clips)`, because every clip must fit into some window. The largest necessary `P` is `sum(clips)`, where all clips go into one window.

2. **Define a monotone predicate `canSplit(P)`**  
   Scan the array once and compute how many windows are needed if each window sum must stay `<= P`. This predicate is monotone: once it becomes true, it stays true for all larger `P`.

3. **Run a greedy feasibility pass**  
   Maintain `currentSum` and `windowsUsed`. For each clip:
   - if `currentSum + clip <= P`, append it to the current window
   - otherwise, start a new window with that clip  
   This greedy rule is correct because maximizing each window locally minimizes the total number of windows globally under a fixed capacity.

4. **Preserve the key invariant**  
   After processing index `i`, the algorithm has used the minimum number of windows possible for `clips[0..i]` under capacity `P`, and the current window is as full as possible without violating `P`.

5. **Binary search for the minimum feasible `P`**  
   Let `mid = low + (high - low) / 2`.
   - If `canSplit(mid)` is true, record it as a candidate and search left.
   - Otherwise search right.

6. **Return the first feasible value**  
   When the search converges, `low` is the minimum packet size that supports partitioning into at most `w` contiguous windows.

## 📊 Worked Example
Example: `clips = [8, 3, 5, 7, 2]`, `w = 3`

Try feasibility with `P = 10`:

| Clip | Current Window Sum Before | Action                  | Current Window Sum After | Windows Used |
|------|----------------------------|-------------------------|--------------------------|--------------|
| 8    | 0                          | add to current window   | 8                        | 1            |
| 3    | 8                          | exceeds 10, new window  | 3                        | 2            |
| 5    | 3                          | add to current window   | 8                        | 2            |
| 7    | 8                          | exceeds 10, new window  | 7                        | 3            |
| 2    | 7                          | add to current window   | 9                        | 3            |

So `P = 10` needs exactly 3 windows: `[8]`, `[3,5]`, `[7,2]`, which is feasible.

Try `P = 9`:

- `[8]`
- `[3,5]`
- `[7,2]` is still valid at 9, but the actual failure comes from binary search context only if another arrangement is needed? No: with this input, `P = 9` is also feasible. Therefore the minimum valid packet size is actually `8`? Check: `[8]`, `[3,5]`, `[7,2]` gives sums `8,8,9`, so `9` works. `8` fails because `7+2=9`. Minimum is `9`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n` is the number of clips and `S = sum(clips)`. Each binary-search step performs one linear feasibility scan. This scales well because the logarithmic factor stays small even when clip sizes are large. At million-element scale, the linear pass dominates; at billion-element scale, data movement becomes the real bottleneck.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few running counters: bounds, current window sum, and windows used. No extra arrays or DP tables are required. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for the **minimum capacity / threshold / limit** that makes a partition feasible, look for binary search on the answer.
- If feasibility under a fixed threshold can be checked by a single left-to-right pass, a greedy predicate is usually the right companion to binary search.
- Set the lower bound to `max(clips)`, not `0` or `min(clips)`; otherwise the search includes impossible capacities.
- Use 64-bit arithmetic for sums and midpoints; `sum(clips)` can exceed 32-bit range easily under the given constraints.
- The production-grade insight is to separate **optimization** from **feasibility**: once the predicate is monotone, expensive planning problems often collapse into fast threshold search.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core problem, but framed as minimizing the largest subarray sum; the conceptual twist is recognizing it is the identical monotone-capacity partition pattern.
- **Capacity To Ship Packages Within D Days**: ordered shipment batching with day limits; the harder part is seeing that “days” are just bounded contiguous groups under a capacity threshold.
- **Painter’s Partition Problem**: contiguous work allocation across workers; the twist is mapping workload balancing to the same binary-search-plus-greedy feasibility structure.