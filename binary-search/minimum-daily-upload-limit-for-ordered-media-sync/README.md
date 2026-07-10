# Minimum Daily Upload Limit for Ordered Media Sync

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array `uploadSizes` and an integer `d`, find the smallest daily upload limit `L` such that all segments can be uploaded in order within at most `d` days. Each segment must stay intact and each day gets a contiguous block of segments. The challenge is that naive search over all possible limits is too slow at scale, so the solution must exploit the monotonic relationship between limit size and schedule feasibility.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered work must be partitioned under a capacity budget: media ingestion pipelines, log compaction batches, warehouse wave planning, backup windows, distributed replication, and CI/CD artifact promotion. In production, the wrong approach either overprovisions capacity or burns CPU exploring impossible configurations. Recognizing monotonic feasibility lets you replace brute-force tuning with a predictable search over a bounded answer space. That matters when arrays are large, item sizes are huge, and the decision sits on a hot planning path or inside a control loop that must react quickly.

## 🔍 Problem Statement
You are given `uploadSizes`, where `uploadSizes[i]` is the bandwidth required to upload segment `i`, and an integer `d`, the maximum number of days available. Segments must be uploaded in the original order, cannot be split across days, and each day receives a contiguous sequence of segments. The goal is to compute the minimum daily limit `L` such that all segments can be uploaded in at most `d` days.

Constraints:

- `1 <= uploadSizes.length <= 200000`
- `1 <= uploadSizes[i] <= 1000000000`
- `1 <= d <= uploadSizes.length`
- Answer fits in signed 64-bit integer

Examples:

- `uploadSizes = [7,2,5,10,8], d = 2` → `18`
- `uploadSizes = [4,4,4,4,4,4,4], d = 3` → `12`

The key algorithmic constraint is input size: `n` can be 200k and values can be large, so scanning all candidate limits directly is not viable.

## 🪜 How to Solve This
1. Read the constraints → brute-forcing every possible daily limit is immediately suspect because the answer range can be enormous.
2. Ask what changes when the limit grows → if a limit `L` works, then any larger limit also works. That is a monotonic feasibility condition.
3. Once feasibility is monotonic, think binary search on the answer, not on indices.
4. Define the search bounds:
   - Lower bound = largest single segment, because no day can hold less than that.
   - Upper bound = sum of all segments, meaning everything uploads in one day.
5. Now you need a fast feasibility test for a candidate `L`.
6. Because order is fixed and segments are indivisible, the greedy check is obvious: keep adding segments to the current day until the next one would exceed `L`, then start a new day.
7. This greedy pass computes the minimum days needed for `L`. If that count is `<= d`, the limit is feasible; otherwise it is not.
8. Binary search the smallest feasible `L`.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer + Greedy Feasibility Check.**  
   The search space is not array indices but possible daily limits. Feasibility is monotonic: increasing the limit never makes scheduling harder.

2. **Set the lower bound `lo` to `max(uploadSizes)`.**  
   Any smaller value fails immediately because at least one segment would not fit in any day. This maintains the invariant that every feasible answer is `>= lo`.

3. **Set the upper bound `hi` to `sum(uploadSizes)`.**  
   This always works by placing all segments in one day. This maintains the invariant that at least one feasible answer exists in `[lo, hi]`.

4. **For a candidate limit `mid`, run a greedy scan.**  
   Accumulate segment sizes into the current day. If adding the next segment would exceed `mid`, start a new day and continue. This is correct because delaying a split never increases the number of days used; the greedy packing minimizes days for that fixed limit.

5. **Interpret the scan result.**  
   If the greedy pass uses `<= d` days, `mid` is feasible, so shrink the search to `[lo, mid]`. Otherwise, `mid` is infeasible, so move to `[mid + 1, hi]`.

6. **Terminate when `lo == hi`.**  
   At that point, the range has collapsed to the minimum feasible daily limit. The invariant throughout is: all values below `lo` are infeasible, and at least one value in `[lo, hi]` is feasible.

## 📊 Worked Example
Example: `uploadSizes = [7,2,5,10,8]`, `d = 2`

| Step | lo | hi | mid | Days needed with `mid` | Decision |
|---|---:|---:|---:|---:|---|
| Init | 10 | 32 | - | - | bounds from max and sum |
| 1 | 10 | 32 | 21 | 2 (`[7,2,5]`, `[10,8]`) | feasible → `hi = 21` |
| 2 | 10 | 21 | 15 | 3 (`[7,2,5]`, `[10]`, `[8]`) | infeasible → `lo = 16` |
| 3 | 16 | 21 | 18 | 2 (`[7,2,5]`, `[10,8]`) | feasible → `hi = 18` |
| 4 | 16 | 18 | 17 | 3 (`[7,2,5]`, `[10]`, `[8]`) | infeasible → `lo = 18` |

Search ends with `lo = hi = 18`, so the minimum daily upload limit is `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n` is the number of segments and `S = sum(uploadSizes) - max(uploadSizes) + 1` is the answer range. Each binary-search step performs one linear feasibility scan. This scales well: even for very large ranges, `log S` stays small, so the dominant cost is repeated sequential passes over the array.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only bounds, a running sum, and a day counter. Space cannot be meaningfully reduced further; the only trade-off is using wider integer types to avoid overflow in `sum` and `mid`.

## 💡 Key Takeaways
- If the question asks for the minimum capacity, rate, or limit that makes an ordered process feasible, check for monotonic feasibility and consider binary search on the answer.
- If items must remain in order and cannot be split, a greedy left-to-right packing pass is often the right feasibility oracle.
- Use `max(uploadSizes)` as the lower bound and `sum(uploadSizes)` as the upper bound; anything else usually introduces correctness bugs or wasted iterations.
- Count days carefully: initialize with one active day during the feasibility scan, and start a new day only when the next segment would exceed the candidate limit.
- In production planning systems, this pattern turns expensive capacity tuning into a deterministic search over a provably correct decision boundary.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core problem, but framed as partitioning an array into `k` subarrays minimizing the maximum subarray sum; the abstraction is identical.
- **Capacity To Ship Packages Within D Days**: same binary-search-on-answer pattern, but with shipping semantics; useful for reinforcing the monotonic feasibility model.
- **Allow reordering or parallel lanes**: harder because the greedy contiguous partition check no longer applies cleanly; the problem shifts toward bin packing or multi-machine scheduling, where monotonicity may remain but feasibility becomes much more complex.