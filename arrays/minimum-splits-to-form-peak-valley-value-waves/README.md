# Minimum Splits to Form Peak-Valley Value Waves

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Greedy

---

## 🗂 Problem Overview
Given an integer array, partition it into the fewest contiguous segments such that each segment forms a strict peak-valley wave: consecutive differences must be non-zero and alternate in sign. Segments of length 1 or 2 are valid unless they contain equal adjacent values. Return the minimum number of segments covering the full array, or `-1` if no valid partition exists. The challenge is doing this in linear time for arrays up to `200000` elements.

## 🌍 Engineering Impact
This pattern shows up in streaming telemetry cleanup, market microstructure analysis, sensor anomaly segmentation, and event-sequence validation in observability pipelines. At scale, you often need to split long streams into maximal locally valid runs under a strict transition rule, then optimize for minimal fragmentation because every split creates downstream cost: more records, more state, more joins, more alerts. A naive backtracking or quadratic DP collapses under sustained ingestion. The useful abstraction here is recognizing when a local validity rule admits a greedy frontier, letting you segment online with bounded memory and predictable latency.

## 🔍 Problem Statement
You are given an array `nums` with `1 <= nums.length <= 200000` and values in `[-10^9, 10^9]`. Partition it into contiguous segments so that every segment is a valid value wave.

A segment is valid if:
- it contains no equal adjacent values, and
- for length `>= 3`, the signs of consecutive differences strictly alternate.

Length-1 segments are always valid. Length-2 segments are valid only if their two values differ. Every element must belong to exactly one segment. Return the minimum number of such segments, or `-1` if no partition exists.

Examples:

- `nums = [3, 1, 4, 2, 5]` → `1`  
  Differences are `[-2, +3, -2, +3]`, so the whole array is valid.

- `nums = [1, 4, 7, 2, 6, 3]` → `2`  
  Differences are `[+3, +3, -5, +4, -3]`; the first two do not alternate, so one optimal split is `[1, 4] | [7, 2, 6, 3]`.

The input size rules out quadratic partition DP.

## 🪜 How to Solve This
1. Read the rule carefully → validity depends only on adjacent differences and whether their signs alternate. That means each segment is defined by a local pattern, not by global ordering.

2. Ask what forces a split → only two things can break a segment:
   - an equal adjacent pair (`diff = 0`)
   - two consecutive differences with the same sign.

3. Equal adjacent values are decisive → no segment may contain them, and a split cannot remove the bad pair because the pair stays adjacent inside some segment. So if any `nums[i] == nums[i-1]`, the answer is immediately `-1`.

4. For non-zero differences, think in terms of the sign array. We want to cover it with the fewest contiguous chunks where signs alternate.

5. If alternation breaks at position `i`, the current segment cannot extend past `i`. To minimize splits, keep each segment as long as possible, then cut exactly when forced.

6. That leads to a greedy scan: start a segment, track the previous difference sign, extend while signs alternate, and split when the next sign repeats.

7. This works because any earlier split only shortens a valid segment without helping future feasibility, so maximal valid extension is optimal.

## 🧩 Algorithm Walkthrough
1. **Pre-validate adjacent equality**  
   Scan `nums` once. If any `nums[i] == nums[i-1]`, return `-1`.  
   **Why correct:** every partition uses contiguous segments, so that equal pair must remain adjacent inside some segment, making that segment invalid.

2. **Handle trivial size**  
   If `n == 1`, return `1`.  
   **Why correct:** a single element is always a valid wave.

3. **Use a Greedy linear scan over differences**  
   This is a **Greedy / one-pass segmentation** pattern. Maintain:
   - `segments = 1`
   - `prevSign = sign(nums[1] - nums[0])`

   The invariant: the current open segment is valid, and `prevSign` is the sign of its last difference.

4. **Process each next difference**  
   For `i` from `2` to `n-1`, compute  
   `currSign = sign(nums[i] - nums[i-1])`.

   Since zeros were already ruled out, `currSign` is `+1` or `-1`.

5. **Decide extend vs split**  
   - If `currSign != prevSign`, extend the current segment and set `prevSign = currSign`.  
   - If `currSign == prevSign`, alternation fails. Split before `nums[i-1]`? No — that would leave the new segment starting with the same invalid pair of signs. The correct cut is between `nums[i-2]` and `nums[i-1]` conceptually, which in streaming form is equivalent to starting a new segment with the last two elements. Increment `segments`, and reset `prevSign = currSign`.

   **Invariant maintained:** after a split, the new segment has length 2, so it is valid regardless of prior alternation history.

6. **Return `segments`**  
   Greedy is optimal because every split is forced exactly at the first point where the current segment becomes invalid, and delaying that split is impossible.

## 📊 Worked Example
Take `nums = [1, 4, 7, 2, 6, 3]`.

| i | pair      | diff sign | prevSign before | action      | segments |
|---|-----------|-----------|-----------------|-------------|----------|
| 1 | 1 → 4     | `+`       | —               | start       | 1        |
| 2 | 4 → 7     | `+`       | `+`             | split       | 2        |
| 3 | 7 → 2     | `-`       | `+`             | extend      | 2        |
| 4 | 2 → 6     | `+`       | `-`             | extend      | 2        |
| 5 | 6 → 3     | `-`       | `+`             | extend      | 2        |

Trace:
1. Start with segment `[1, 4]`.
2. Next diff is also positive, so `[1, 4, 7]` would violate alternation.
3. Split and restart from the last two elements, giving `[7, 2]`.
4. Continue extending because signs alternate: `[7, 2, 6, 3]`.
5. Final partition: `[1, 4] | [7, 2, 6, 3]`, so the minimum is `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. We do one pass to reject equal adjacent values and one pass to count forced splits; these can also be fused into a single scan. The dominant operation is constant-time sign comparison per element. At `10^6` elements this is routine; at `10^9`, throughput and I/O dominate, not algorithmic overhead.

### Space Complexity
`O(1)`. The algorithm stores only the current sign and segment count. No auxiliary arrays or DP tables are required. You could materialize the difference-sign array for debugging or vectorized processing, but that increases space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If segment validity depends only on adjacent transitions and a split cannot repair local violations retroactively, look for a greedy maximal-segment scan.
- When the problem asks for minimum contiguous partitions under a local alternation rule, convert the array into a sign stream and reason on that simpler representation.
- Equal adjacent values are not just a split trigger here; they make the entire instance impossible because contiguity preserves the bad pair.
- On a repeated sign, the reset state is a new length-2 segment ending at the current index; resetting incorrectly by one position produces undercounts.
- In production stream processors, local invariants that admit forced-cut greedy segmentation are valuable because they support online processing with constant memory and stable latency.

## 🚀 Variations & Further Practice
- Allow deleting up to `k` elements before partitioning. The twist is choosing removals that repair zero-diffs or repeated signs globally, which turns the problem into DP over edit budget and local state.
- Minimize total split cost when each cut has a position-dependent penalty. The twist is that greedy maximal extension is no longer always optimal; you need DP or shortest-path reasoning.
- Support circular arrays and find the minimum splits over all rotations. The twist is handling wraparound alternation and choosing a break point that avoids duplicating state.