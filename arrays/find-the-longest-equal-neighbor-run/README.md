# Find the Longest Equal Neighbor Run

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Linear Scan

---

## 🗂 Problem Overview
Given an integer array `nums`, return the length of the longest contiguous run where adjacent elements are equal. A run is valid if every neighboring pair inside that segment matches. If the array is empty, return `0`; if no adjacent values match, the answer is `1`. The key constraint is scale: with up to `100000` elements, the solution should avoid nested comparisons and compute the result in a single linear pass.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need dwell-time or state-stability measurement over ordered events: IoT telemetry, device health monitoring, clickstream session analysis, log compaction, and stream processors such as Flink or Kafka Streams. At scale, the difference between a single pass and repeated rescans is the difference between constant-memory streaming and avoidable latency amplification. Without this approach, teams often over-engineer with grouping, buffering, or window materialization. The linear-scan formulation enables online processing, low memory pressure, and straightforward integration into ingestion pipelines where events arrive already ordered by time.

## 🔍 Problem Statement
You are given an integer array `nums` with `0 <= nums.length <= 100000` and values in the range `[-10^9, 10^9]`. A stable run is a contiguous subarray in which every adjacent pair is equal. The task is to return the maximum length of any such run.

Important edge cases:
- Empty array → return `0`
- Single-element array → return `1`
- All values distinct → return `1`
- Entire array equal → return `nums.length`

Examples:

- `nums = [4,4,4,2,2,7,7,7,7,1]` → `4`  
  Longest run is `[7,7,7,7]`

- `nums = [5,3,8,1]` → `1`  
  No equal neighbors, so each element forms a run of length `1`

The constraint that drives the algorithmic choice is the array length: this should be solved in `O(n)` time with a single scan, not by checking every subarray.

## 🪜 How to Solve This
1. Read the problem → notice it asks for a **contiguous** segment, not a frequency count. That immediately rules out sorting or hash-based aggregation because order matters.

2. Focus on what defines the segment → a run continues only while `nums[i] == nums[i - 1]`. That means each element either:
   - extends the current run, or
   - starts a new run.

3. That observation suggests a streaming state machine:
   - keep `currentRun`, the length of the run ending at the current index
   - keep `bestRun`, the maximum seen so far

4. Scan left to right once:
   - if current value matches the previous one, increment `currentRun`
   - otherwise reset `currentRun` to `1`

5. After each step, update `bestRun`. This works because every maximal run is discovered exactly once at its right boundary.

6. Handle the empty array explicitly before the scan. Everything else falls out naturally from the same logic.

This is the classic linear-scan / simulation pattern: minimal state, no backtracking, no auxiliary structure, and correctness driven by a simple invariant about the current suffix.

## 🧩 Algorithm Walkthrough
1. **Handle the empty input**
   - If `nums.length == 0`, return `0`.
   - This is correct because there is no run in an empty sequence.
   - Invariant: after this check, the array has at least one element, so a run length of `1` is always valid.

2. **Initialize tracking state**
   - Set `currentRun = 1` and `bestRun = 1`.
   - `currentRun` represents the length of the equal-neighbor run ending at the current index.
   - `bestRun` represents the maximum run length seen in the prefix processed so far.

3. **Scan from left to right**
   - For each index `i` from `1` to `n - 1`, compare `nums[i]` with `nums[i - 1]`.
   - If they are equal, increment `currentRun`; otherwise reset it to `1`.
   - Why this is correct: any run ending at `i` must either extend the run ending at `i - 1` or begin fresh at `i`.

4. **Update the global maximum**
   - After processing each element, set `bestRun = max(bestRun, currentRun)`.
   - This preserves the invariant that `bestRun` is the longest run in `nums[0..i]`.

5. **Return `bestRun`**
   - After the final index, every run has been accounted for exactly once.
   - The pattern here is **Linear Scan / Simulation**. It is the right abstraction because the problem depends only on local adjacency and can be solved with constant state over a single ordered pass.

## 📊 Worked Example
Example: `nums = [4,4,4,2,2,7,7,7,7,1]`

| i | nums[i] | nums[i-1] | Equal? | currentRun | bestRun |
|---|---------|-----------|--------|------------|---------|
| 0 | 4       | —         | —      | 1          | 1       |
| 1 | 4       | 4         | Yes    | 2          | 2       |
| 2 | 4       | 4         | Yes    | 3          | 3       |
| 3 | 2       | 4         | No     | 1          | 3       |
| 4 | 2       | 2         | Yes    | 2          | 3       |
| 5 | 7       | 2         | No     | 1          | 3       |
| 6 | 7       | 7         | Yes    | 2          | 3       |
| 7 | 7       | 7         | Yes    | 3          | 3       |
| 8 | 7       | 7         | Yes    | 4          | 4       |
| 9 | 1       | 7         | No     | 1          | 4       |

The longest run is discovered when the scan reaches index `8`. No extra storage or second pass is needed because `currentRun` always describes the run ending at the current position.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n` is `nums.length`. The dominant operation is one equality check per element after the first, plus constant-time updates to two counters. At `10^6` elements this is trivial in practice; at `10^9`, the algorithm remains optimal asymptotically, though runtime becomes bounded by raw scan throughput and memory bandwidth.

### Space Complexity
`O(1)`. The algorithm stores only a few scalar variables: the current run length, the best run length, and the loop index. There is no auxiliary data structure. Space cannot be meaningfully reduced further unless the input itself is streamed rather than materialized.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** region defined by equality between neighbors, think linear scan before considering maps, sorting, or DP.
- When each element only depends on the immediately previous element, that is a strong signal that constant-state simulation is sufficient.
- The empty-array case is the main boundary condition; forgetting it usually leads to incorrect initialization or out-of-bounds access.
- Reset `currentRun` to `1`, not `0`, when the value changes, because the current element starts a new valid run by itself.
- In production pipelines, many “state duration” metrics reduce to maintaining a current streak and a best streak over ordered events, which is exactly the same design pattern.

## 🚀 Variations & Further Practice
- Return the **start and end indices** of the longest equal run, not just its length. The twist is carrying boundary metadata while preserving the same invariant.
- Process a **stream of events online** and answer the longest run seen so far after each append. The harder part is designing the state interface for incremental updates.
- Find the longest run where adjacent values differ by at most `k` instead of being exactly equal. The conceptual shift is redefining the continuation condition while keeping the same scan pattern.