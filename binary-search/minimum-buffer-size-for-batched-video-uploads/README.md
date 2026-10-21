# Minimum Buffer Size for Batched Video Uploads

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array `clips` and an integer `maxBatches`, find the smallest buffer capacity `B` such that the clips can be partitioned, in original order, into at most `maxBatches` contiguous batches where each batch sum is `<= B`. The output is that minimum feasible `B`. What makes this non-trivial is that the search space is numeric and large, while feasibility depends on ordered grouping rather than reordering or local optimization.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must preserve order while minimizing peak resource allocation: upload buffering, Kafka or Pulsar batch sizing, log segment compaction windows, distributed checkpoint chunking, and CI artifact bundling. The core decision is not “can we process everything,” but “what is the minimum capacity that keeps throughput within operational limits.” Without this approach, teams often overprovision memory or network buffers, or fall back to expensive simulation over huge parameter ranges. Binary search over a monotonic feasibility function turns capacity planning from guesswork into a predictable, scalable control loop.

## 🔍 Problem Statement
You are given:

- `clips`, where `clips[i]` is the size of the `i`-th video clip in megabytes
- `maxBatches`, the maximum number of contiguous upload batches allowed

You must partition `clips` into contiguous groups, preserving the original order, such that every clip belongs to exactly one batch and the sum of each batch is at most buffer size `B`. Return the minimum feasible `B`.

Constraints:

- `1 <= clips.length <= 100000`
- `1 <= clips[i] <= 1000000000`
- `1 <= maxBatches <= clips.length`
- Answer fits in signed 64-bit integer

Examples:

- `clips = [8, 3, 5, 7, 2], maxBatches = 3` → `10`
- `clips = [4, 4, 4, 4], maxBatches = 2` → `8`

The key constraint is scale: `n` is up to `1e5`, values are large, and the answer range spans from `max(clips)` to `sum(clips)`, ruling out brute-force search over capacities.

## 🪜 How to Solve This
1. Start with the output shape: we are not asked for the partition itself first, but for the minimum buffer size that makes some partition possible.

2. Notice the decision version is easier: for a given buffer size `B`, can we upload all clips in order using at most `maxBatches` batches?

3. That feasibility check is greedy:
   → scan left to right  
   → keep adding clips to the current batch  
   → when the next clip would exceed `B`, start a new batch

4. Why greedy works: if a clip fits in the current batch, splitting earlier never helps reduce the total number of batches. Packing each batch as much as possible minimizes batch count for that `B`.

5. Now observe monotonicity:
   → if capacity `B` is feasible, any larger capacity is also feasible  
   → if `B` is infeasible, any smaller capacity is also infeasible

6. A monotonic yes/no predicate over a numeric range is a binary-search-on-answer problem.

7. The search bounds are immediate:
   → lower bound = largest single clip  
   → upper bound = sum of all clips

That gives `O(n log(sum))`, which is the intended scale.

## 🧩 Algorithm Walkthrough
1. **Establish the search interval.**  
   Set `low = max(clips)` because no buffer smaller than the largest clip can ever work. Set `high = sum(clips)` because one batch containing everything is always valid when `maxBatches >= 1`. This defines the complete feasible answer range.

2. **Define the feasibility predicate using a Greedy scan.**  
   For a candidate capacity `mid`, walk the array once. Maintain `currentSum` for the active batch and `batchesUsed`, initially `1`. If adding the next clip keeps `currentSum <= mid`, extend the current batch. Otherwise, start a new batch with that clip and increment `batchesUsed`.

3. **Maintain the greedy invariant.**  
   After processing each prefix of the array, the algorithm uses the minimum possible number of batches for that prefix under capacity `mid`. This holds because delaying a split as long as possible cannot increase future flexibility; premature splits only waste capacity.

4. **Classify the candidate.**  
   If the scan finishes with `batchesUsed <= maxBatches`, then `mid` is feasible. Otherwise it is infeasible. This predicate is monotonic, which is the key abstraction: **Binary Search on Answer** over a monotone feasibility function.

5. **Binary search for the first feasible value.**  
   While `low < high`, compute `mid = low + (high - low) / 2`. If feasible, move `high = mid`; otherwise move `low = mid + 1`. This preserves the invariant that the answer always remains inside `[low, high]`.

6. **Return `low`.**  
   When the interval collapses, `low == high` is the smallest feasible buffer size.

## 📊 Worked Example
Example: `clips = [8, 3, 5, 7, 2]`, `maxBatches = 3`

Search range:
- `low = 8`
- `high = 25`

Check `mid = 16`:

| Clip | Current Batch Sum | Action | Batches Used |
|---|---:|---|---:|
| 8 | 8 | add | 1 |
| 3 | 11 | add | 1 |
| 5 | 16 | add | 1 |
| 7 | 7 | new batch | 2 |
| 2 | 9 | add | 2 |

Feasible with 2 batches, so search left: `high = 16`.

Check `mid = 12`:
- `[8,3]` = 11
- `5` starts batch 2
- `7` starts batch 3
- `2` joins batch 3  
Feasible, so `high = 12`.

Check `mid = 10`:
- `[8]`, `[3,5]`, `[7,2]` → exactly 3 batches, feasible.

Check `mid = 9`:
- `[8]`, `[3,5]`, `[7,2]` is impossible because `3+5=8`, `7+2=9`, but ordering forces 4 batches overall. Infeasible.

Minimum feasible buffer size is `10`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n = clips.length` and `S = sum(clips) - max(clips) + 1` is the numeric search range. Each binary search step runs one linear feasibility scan. At `10^6` elements this remains practical; at `10^9` elements the scan itself dominates and the approach becomes I/O-bound rather than algorithmically expensive.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few scalar variables: bounds, running sum, batch count, and midpoint. Space cannot be meaningfully reduced further unless input streaming constraints change the representation of `clips`.

## 💡 Key Takeaways
- If the question asks for the **minimum capacity / maximum load / smallest threshold** and feasibility becomes easier as the value increases, think binary search on answer.
- If the partition must preserve original order and groups are contiguous, a greedy left-to-right feasibility check is often the right first move.
- Use 64-bit arithmetic for `sum(clips)`, `mid`, and running batch sums; 32-bit overflow is easy here.
- The feasibility check should start with `batchesUsed = 1`, not `0`; otherwise exact-boundary cases produce off-by-one errors.
- In production systems, this pattern is a capacity-sizing primitive: optimize the smallest safe resource envelope by combining a monotone predicate with a cheap validator.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core problem, but framed as minimizing the largest subarray sum; useful for recognizing the abstraction independent of domain wording.
- **Capacity To Ship Packages Within D Days**: similar binary-search-on-capacity pattern, but feasibility is constrained by days rather than arbitrary partitions.
- **Allocate tasks with non-contiguous assignment**: harder because the greedy contiguous batching invariant breaks, forcing different optimization machinery such as DP or load-balancing heuristics.