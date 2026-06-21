# Count Inventory Plateaus

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Counting

---

## 🗂 Problem Overview
Given an integer array `stock`, treat each maximal contiguous run of equal values as one inventory plateau. The task is to count how many such plateaus have length at least `k`. Input is the array and threshold `k`; output is a single integer count. The only real challenge is doing this in one pass over up to `100000` elements without overcomplicating grouping logic or missing boundary cases at run transitions.

## 🌍 Engineering Impact
This pattern shows up anywhere systems compress or reason about contiguous identical states rather than individual events. In streaming pipelines, repeated sensor values or status codes are often processed as runs to reduce downstream work. In observability systems, alert-state timelines are summarized into stable intervals. In warehouse, pricing, and capacity planning systems, consecutive unchanged values matter more than raw samples. At scale, failing to detect runs incrementally leads to unnecessary allocations, poor cache behavior, and avoidable quadratic scans. A linear run-length traversal enables online aggregation, compact summaries, and predictable performance under sustained high-volume input.

## 🔍 Problem Statement
You are given an integer array `stock` where `stock[i]` is the inventory count at hour `i`, and an integer `k`. A plateau is a maximal contiguous block of equal values: all elements in the block are the same, and extending the block left or right would change the value.

Return the number of plateaus whose length is at least `k`.

Constraints:

- `1 <= stock.length <= 100000`
- `0 <= stock[i] <= 1000000000`
- `1 <= k <= stock.length`

Examples:

- `stock = [4, 4, 4, 2, 2, 9, 1, 1], k = 2` → `3`
- `stock = [6, 3, 3, 3, 5, 5, 8], k = 3` → `1`

Edge cases matter: a single-element array is one plateau; every value distinct means all plateau lengths are `1`; the final run must be counted even though no later value forces a transition. The `100000` upper bound rules out any nested rescanning approach.

## 🪜 How to Solve This
1. Read the problem → this is not about frequencies across the whole array; it is about **contiguous grouping**. Equal values separated by a different value belong to different plateaus.

2. Once “maximal contiguous run” is clear, the natural model is: scan left to right and track the current run length.

3. At each new element, ask one question: does it continue the current plateau or start a new one?  
   → If equal to the previous value, extend the run.  
   → Otherwise, the previous plateau is complete, so evaluate its length against `k`, count it if eligible, and reset.

4. After the loop, handle the last run explicitly. This is the classic boundary condition because the array ends without a value change to flush the final plateau.

5. Why this approach? Because every element only affects its local run boundary. No sorting, hashing, or extra storage helps: sorting would destroy contiguity, and a map would answer the wrong question.

6. The result is a single linear pass with constant memory, which is exactly what the constraints and problem shape suggest.

## 🧩 Algorithm Walkthrough
1. **Use a linear scan with a run-length counter**. This is a **Two Pointers / run-length encoding traversal** pattern in its simplest form: one implicit pointer marks the start of the current plateau, while the loop index advances through the array. This abstraction fits because plateaus are contiguous and disjoint.

2. **Initialize state**: set `count = 0` and `runLength = 1`. The invariant is that before processing `stock[i]`, `runLength` equals the length of the current plateau ending at `i - 1`.

3. **Iterate from index `1` to `n - 1`**. Compare `stock[i]` with `stock[i - 1]`.
   - If equal, increment `runLength`.  
     Why correct: the current plateau continues by one element.
   - If different, the previous plateau has ended. Check whether `runLength >= k`; if so, increment `count`. Then reset `runLength = 1` for the new plateau starting at `i`.

4. **Maintain the invariant** after each iteration: `runLength` always describes the active plateau ending at the current index.

5. **Flush the final plateau after the loop**. If `runLength >= k`, increment `count`. This is necessary because the last plateau never encounters a “different next value” to trigger counting inside the loop.

6. **Return `count`**. Correctness follows from partitioning the array into maximal equal-value runs exactly once, evaluating each run once, and never merging non-contiguous equal values.

## 📊 Worked Example
Example: `stock = [4, 4, 4, 2, 2, 9, 1, 1]`, `k = 2`

| i | stock[i] | Compare to previous | runLength after step | count |
|---|----------|---------------------|----------------------|-------|
| 0 | 4        | start               | 1                    | 0     |
| 1 | 4        | equal               | 2                    | 0     |
| 2 | 4        | equal               | 3                    | 0     |
| 3 | 2        | different           | reset to 1           | 1     |
| 4 | 2        | equal               | 2                    | 1     |
| 5 | 9        | different           | reset to 1           | 2     |
| 6 | 1        | different           | reset to 1           | 2     |
| 7 | 1        | equal               | 2                    | 2     |

Loop ends with `runLength = 2`, so flush the final plateau and increment `count` to `3`.

Plateaus found: `[4,4,4]`, `[2,2]`, `[9]`, `[1,1]`.  
Eligible plateaus: lengths `3`, `2`, and `2` → answer `3`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time, where `n = stock.length`, because each element is visited once and each visit performs only a constant-time comparison and possible counter update. At `10^6` elements this is trivial in practice; at `10^9`, linear work is still expensive but remains the best possible asymptotic bound for exact single-pass evaluation.

### Space Complexity
The algorithm uses **O(1)** extra space. Only scalar state is maintained: the current run length and the result counter. There is no auxiliary array, map, or stack. Space cannot be meaningfully reduced further unless input traversal itself is changed to a streaming interface.

## 💡 Key Takeaways
- If the problem says “contiguous,” “maximal block,” or “adjacent equal values,” think run-length traversal before considering maps or sorting.
- If equal values separated by other values must remain distinct, this is a segmentation problem, not a global counting problem.
- The most common bug is forgetting to count the final run after the loop ends.
- Another easy off-by-one trap is initializing `runLength` incorrectly for arrays of length `1` or starting iteration at index `0`.
- In production code, recognizing compressible contiguous state lets you replace event-by-event processing with interval-level summaries, which improves throughput and simplifies downstream logic.

## 🚀 Variations & Further Practice
- Count plateaus whose values are within a tolerance band instead of exactly equal; the twist is that run membership becomes predicate-based rather than equality-based.
- Return the start and end indices of all qualifying plateaus; the twist is preserving boundary metadata while still streaming in one pass.
- Support online updates and repeated queries over changing inventory; the twist is moving from static linear scan to a dynamic interval or segment-tree style representation.