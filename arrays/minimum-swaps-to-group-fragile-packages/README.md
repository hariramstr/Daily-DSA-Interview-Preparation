# Minimum Swaps to Group Fragile Packages

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Prefix Sum

---

## 🗂 Problem Overview
Given a binary array `packages`, where `1` marks a fragile package, compute the minimum number of swaps needed to make all fragile packages occupy one contiguous block. Swaps may occur between any two positions, so only membership inside the final block matters, not order. The non-trivial part is choosing the best target block efficiently under `n <= 100000`, which rules out trying every rearrangement or using quadratic comparisons.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must minimize disruption while consolidating sparse “hot” elements into a compact region: memory compaction in runtimes, shard rebalancing in distributed storage, log segment cleanup, cache-line packing, and warehouse/bin optimization in fulfillment systems. At scale, brute-force relocation logic becomes operationally expensive because the search space is combinatorial even when the move cost model is simple. The sliding-window view turns a global rearrangement problem into a local density maximization problem, enabling linear-time planning. That shift matters in production systems where placement decisions must be recomputed continuously on large streams or large state snapshots.

## 🔍 Problem Statement
You are given an integer array `packages` of length `n`, where each value is either `0` or `1`. A value of `1` represents a fragile package; `0` represents a non-fragile package. You may swap the contents of any two indices in one operation.

Return the minimum number of swaps required so that all fragile packages become adjacent in one contiguous subarray.

Key observations and edge cases:
- If there are `0` or `1` fragile packages, the answer is `0`.
- Let `k` be the total number of fragile packages. Any valid final arrangement must place all fragile packages inside some window of length `k`.
- Therefore, the problem reduces to finding the length-`k` window containing the most `1`s.

Constraints:
- `1 <= packages.length <= 100000`
- `packages[i] ∈ {0, 1}`

Examples:
- `packages = [1,0,1,0,1]` → `1`
- `packages = [0,0,1,0,1,1,0]` → `1`

## 🪜 How to Solve This
1. Read the problem carefully → notice the operation is an unrestricted swap, not an adjacent swap. That means distance does not matter; only how many misplaced elements exist matters.

2. Count the total number of fragile packages, call it `k` → if all fragile packages must end up together, the final block must have length `k`. There is no other possible block size.

3. Reframe the goal → instead of asking “how do I move all `1`s together?”, ask “which window of size `k` already contains the most `1`s?” That is the same optimization problem.

4. Why does that work? → every `0` inside the chosen window must be swapped with a `1` outside it. So swaps needed for a window = `k - (# of 1s already in the window)`.

5. Now the problem is a standard fixed-size sliding window → compute the number of `1`s in the first window of length `k`, then slide one step at a time, subtracting the outgoing element and adding the incoming one.

6. Track the maximum number of `1`s seen in any window → the answer is `k - maxOnesInWindow`.

This is the shortest path from the original wording to an `O(n)` solution.

## 🧩 Algorithm Walkthrough
1. **Count total fragile packages (`k`)**  
   Scan the array once and sum all values. Since entries are binary, this directly gives the number of fragile packages.  
   **Why correct:** any contiguous grouping of all fragile packages must contain exactly `k` positions.  
   **Invariant:** `k` is fixed for the rest of the algorithm.

2. **Handle trivial cases early**  
   If `k <= 1`, return `0`.  
   **Why correct:** zero or one fragile package is already contiguous by definition.  
   **Invariant:** after this step, `k >= 2`, so a meaningful window exists.

3. **Initialize a fixed-size sliding window of length `k`**  
   Compute the number of `1`s in indices `[0, k-1]`. Store it as `windowOnes`, and initialize `maxOnes = windowOnes`.  
   **Pattern:** Sliding Window over a fixed-length segment.  
   **Why this abstraction fits:** we are evaluating every candidate final block of the same size, and adjacent windows differ by exactly one outgoing and one incoming element.

4. **Slide the window across the array**  
   For each new right boundary `r` from `k` to `n-1`, update:  
   `windowOnes += packages[r] - packages[r-k]`  
   Then update `maxOnes = max(maxOnes, windowOnes)`.  
   **Why correct:** this maintains the exact count of fragile packages in the current window without rescanning it.  
   **Invariant:** after each update, `windowOnes` equals the number of `1`s in the current length-`k` window.

5. **Compute the minimum swaps**  
   Return `k - maxOnes`.  
   **Why correct:** in the best window, every missing fragile package corresponds to one `0` that must be swapped out. Because swaps can target any positions, each such mismatch is resolved in one swap.

An equivalent prefix-sum formulation exists, but sliding window gives the same result with less state and better constant factors.

## 📊 Worked Example
Example: `packages = [1,0,1,0,1]`

Total fragile packages: `k = 3`, so inspect every window of length `3`.

| Window Indices | Window Values | `windowOnes` | `maxOnes` |
|---|---|---:|---:|
| `[0..2]` | `[1,0,1]` | 2 | 2 |
| `[1..3]` | `[0,1,0]` | 1 | 2 |
| `[2..4]` | `[1,0,1]` | 2 | 2 |

Trace:
1. First window `[1,0,1]` contains 2 fragile packages.
2. Slide right once: remove `1`, add `0` → count becomes 1.
3. Slide right again: remove `0`, add `1` → count becomes 2.
4. Best window contains `maxOnes = 2`.

Minimum swaps = `k - maxOnes = 3 - 2 = 1`.

Interpretation: choose a length-3 block that already contains two fragile packages; only one non-fragile package inside that block must be swapped with the remaining fragile package outside it.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass counts total fragile packages, and one pass slides a fixed-size window across the array. Each element is processed a constant number of times. At `10^6` elements this is routine; at `10^9`, asymptotically still optimal, but memory bandwidth and data locality dominate runtime.

### Space Complexity
`O(1)`. The algorithm stores only a few counters: `k`, `windowOnes`, and `maxOnes`. No auxiliary array is required. A prefix-sum version would use `O(n)` space unless computed on demand, which is usually not worth the trade-off here.

## 💡 Key Takeaways
- If the problem asks to make all identical markers contiguous and swaps are unrestricted, look for a fixed-size window equal to the total count of target markers.
- When the cost is “how many items are misplaced,” maximize what is already correct inside a candidate region instead of simulating swaps.
- The window length is `k`, not `k - 1`; off-by-one mistakes here silently corrupt every later count.
- Handle `k = 0` and `k = 1` explicitly before building the initial window, or initialization logic becomes brittle.
- The transferable design insight is to convert global rearrangement problems into local density optimization, which often collapses combinatorial search into a linear scan.

## 🚀 Variations & Further Practice
- **Group all `1`s together in a circular array**: the window can wrap around the end, so you need either array doubling or modular sliding-window logic.
- **Minimum adjacent swaps to group all `1`s**: unrestricted swap counting no longer works; distance matters, and the solution shifts toward median positioning of indices.
- **Weighted grouping or non-uniform move costs**: each misplaced item has a different relocation cost, turning a simple count-maximization problem into a weighted window or prefix-sum optimization.