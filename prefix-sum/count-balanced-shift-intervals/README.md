# Count Balanced Shift Intervals

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Prefix Sum &nbsp;|&nbsp; **Tags:** Prefix Sum, Hash Map, Array

---

## 🗂 Problem Overview
Given an array `hours` and a `threshold`, classify each time block as heavy (`hours[i] >= threshold`) or light (`hours[i] < threshold`). Count how many contiguous intervals contain exactly the same number of heavy and light blocks. The output is a single integer: the number of balanced subarrays. The challenge is scale: with up to `2 * 10^5` elements, enumerating and validating all subarrays is too slow, so the solution must avoid quadratic work.

## 🌍 Engineering Impact
This pattern shows up anywhere a stream is reduced into binary signals and you need to count neutral spans efficiently: trading systems detecting balanced buy/sell pressure windows, observability pipelines measuring equal error/success bursts, workforce analytics over shift intensity, and network telemetry tracking overloaded vs normal intervals. At production scale, brute-force interval scans collapse under volume and latency budgets. Prefix-sum frequency counting turns a pairwise comparison problem into a single-pass aggregation problem. That shift matters architecturally: it enables online processing, bounded per-event work, and predictable performance under high-cardinality event streams.

## 🔍 Problem Statement
You are given an integer array `hours` of length `n` and an integer `threshold`.

For each index:
- `hours[i] >= threshold` means the block is **heavy**
- `hours[i] < threshold` means the block is **light**

A subarray `hours[l...r]` is **balanced** if it contains the same number of heavy and light blocks. Return the total number of such subarrays.

Constraints:
- `1 <= n == hours.length <= 2 * 10^5`
- `0 <= hours[i] <= 10^9`
- `0 <= threshold <= 10^9`

Examples:

- `hours = [6, 3, 8, 2, 7], threshold = 5` → `4`
- `hours = [4, 4, 9, 1], threshold = 4` → `2`

The key constraint is `n = 2 * 10^5`: any `O(n^2)` approach that checks every interval is infeasible. The right solution must count valid subarrays indirectly, using a linear scan plus an auxiliary structure.

## 🪜 How to Solve This
1. Read the condition carefully → “same number of heavy and light” is a balance condition, not a raw sum over `hours`.
2. Convert the array into contributions:
   - heavy → `+1`
   - light → `-1`
   Now a balanced interval is exactly a subarray whose transformed sum is `0`.
3. Once the problem becomes “count subarrays with sum `0`,” the standard move is prefix sums.
4. Let `prefix[i]` be the transformed sum up to index `i`. A subarray `l...r` has sum `0` when:
   `prefix[r] == prefix[l - 1]`.
5. That means every time the same prefix sum appears again, it forms new balanced intervals with all earlier occurrences of that same sum.
6. So the task reduces to: as you scan left to right, keep a frequency map of prefix sums seen so far.
7. For each new prefix sum:
   - add its previous frequency to the answer
   - then increment its frequency
8. Seed the map with prefix sum `0` seen once, so intervals starting at index `0` are counted correctly.

That is the whole leap: transform balance into zero-sum, then count equal prefixes.

## 🧩 Algorithm Walkthrough
1. **Apply the Prefix Sum + Hash Map pattern.**  
   Transform each `hours[i]` into `+1` if it is heavy, otherwise `-1`. This is the right abstraction because the original requirement is about equal counts of two categories, which naturally maps to a zero-sum condition.

2. **Initialize state.**  
   Set `prefix = 0`, `answer = 0`, and a hash map `freq` with `freq[0] = 1`.  
   This invariant means: before processing any element, we have seen one prefix sum equal to zero, representing the empty prefix.

3. **Scan the array once from left to right.**  
   For each element:
   - update `prefix += +1` or `-1`
   - look up how many times this exact `prefix` has appeared before

4. **Count new balanced intervals.**  
   If the current prefix sum has been seen `k` times, then there are `k` earlier positions with the same prefix. Each one defines a subarray ending at the current index whose transformed sum is zero. Add `k` to `answer`.

5. **Record the current prefix.**  
   Increment `freq[prefix]`.  
   The invariant after each step: `freq[x]` equals the number of processed prefixes with value `x`, and `answer` equals the number of balanced subarrays ending at or before the current index.

6. **Return the accumulated answer.**  
   Correctness follows from prefix-difference identity: a subarray sum is zero iff the prefix sums at its boundaries are equal. Since every index is processed once and every valid interval is counted exactly when its right endpoint is reached, there is no double counting and no omission.

## 📊 Worked Example
Take `hours = [6, 3, 8, 2, 7]`, `threshold = 5`.

Transformed array: `[+1, -1, +1, -1, +1]`

| i | hours[i] | val | prefix | freq[prefix] before | new intervals | answer |
|---|----------|-----|--------|---------------------|---------------|--------|
| - | -        | -   | 0      | 1                   | -             | 0      |
| 0 | 6        | +1  | 1      | 0                   | 0             | 0      |
| 1 | 3        | -1  | 0      | 1                   | 1             | 1      |
| 2 | 8        | +1  | 1      | 1                   | 1             | 2      |
| 3 | 2        | -1  | 0      | 2                   | 2             | 4      |
| 4 | 7        | +1  | 1      | 2                   | 2             | 6      |

Using the stated transformation, the count is `6`, corresponding to zero-sum subarrays in `[+1, -1, +1, -1, +1]`. The mechanism is the important part: repeated prefix sums identify balanced intervals immediately.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` time. Each element is transformed once, each prefix sum is updated once, and each hash map operation is amortized `O(1)`. At `10^6` elements this remains practical in a single pass; at `10^9`, even linear time becomes a systems problem dominated by I/O, memory bandwidth, and streaming architecture.

### Space Complexity
`O(n)` space in the worst case for the prefix-frequency map, when all prefix sums are distinct. The map owns essentially all auxiliary memory. You cannot generally reduce this to `O(1)` without losing the ability to count prior equal prefixes, unless stronger constraints on the input are available.

## 💡 Key Takeaways
- If a problem asks for subarrays where two categories occur equally often, try converting them to `+1` and `-1`; equality-of-counts often becomes zero-sum.
- If you need to count many subarrays efficiently, repeated prefix sums are a strong signal that a hash map over prefix frequencies is the right tool.
- Seed `freq[0] = 1` before the scan; otherwise, balanced intervals starting at index `0` are silently missed.
- Update order matters: add `freq[prefix]` to the answer before incrementing `freq[prefix]`, or you will count zero-length intervals implicitly.
- The production lesson is broader than this problem: once you can encode a domain condition as an additive invariant, expensive pairwise scans often collapse into a linear streaming aggregation.

## 🚀 Variations & Further Practice
- Count subarrays where heavy blocks exceed light blocks by exactly `k`; same prefix-sum idea, but now you count prior prefixes equal to `current - k`.
- Count the longest balanced interval instead of the total number; store the first occurrence of each prefix sum rather than its frequency.
- Extend from binary classification to multiple categories with equal counts; the twist is moving from a scalar prefix sum to a vector-difference signature, which changes both representation and hash-key design.