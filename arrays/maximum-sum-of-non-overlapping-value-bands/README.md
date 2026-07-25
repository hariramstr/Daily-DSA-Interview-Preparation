# Maximum Sum of Non-Overlapping Value Bands

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** arrays, dynamic-programming, sliding-window

---

## 🗂 Problem Overview
Given an integer array `nums` and threshold `k`, select any set of pairwise non-overlapping contiguous subarrays whose internal `max - min <= k`. Each chosen subarray contributes its element sum, and elements may be skipped entirely. Return the maximum total score. The challenge is not validating one window, but optimizing across all windows under overlap constraints at `n <= 2 * 10^5`, where naive enumeration of subarrays or interval DP is too slow.

## 🌍 Engineering Impact
This pattern shows up in segmentation problems over ordered data: selecting profitable stretches of market ticks whose volatility stays bounded, carving stable latency regions from observability streams, extracting low-drift spans in sensor pipelines, or choosing admissible token spans in ranking and parsing systems. At scale, the failure mode is usually quadratic interval exploration or repeated rescans for min/max. The useful abstraction is: maintain a validity window online, then run a weighted non-overlapping selection over those candidate endings. That combination enables single-pass throughput, predictable memory, and production-safe behavior on long streams or large batch jobs.

## 🔍 Problem Statement
You are given `nums` with `1 <= nums.length <= 2 * 10^5`, values in `[-10^9, 10^9]`, and `0 <= k <= 10^9`. A value band is any contiguous subarray `nums[l..r]` where the difference between the maximum and minimum element in that subarray is at most `k`. You may select any number of such bands, but selected bands cannot overlap. The score of a band is the sum of its elements, and the goal is to maximize total score. Length-1 bands are always valid, but negative-sum valid bands should usually be skipped.

Examples:

- `nums = [4, 2, 3, 7, 6, 5], k = 2` → `27`
- `nums = [5, -4, 6, 6, -2, 7], k = 1` → `24`

The decisive constraint is `n = 2e5`: checking all subarrays or all valid intervals is infeasible, so the algorithm must process each index near-constantly.

## 🪜 How to Solve This
1. Start from the validity rule: a subarray is allowed only if its `max - min <= k`. That immediately suggests a sliding window with data structures that can report current min and max in O(1) amortized time.

2. But finding valid windows is only half the problem. We are not asked for the longest or count of valid windows; we need the best **sum of non-overlapping** valid windows. That is weighted interval scheduling on an array.

3. For each right endpoint `r`, ask: among all valid bands ending at `r`, which one gives the best total if appended after an optimal solution ending before its left boundary?

4. Write that as  
   `dp[r+1] = max(dp[r], max over valid l..r of dp[l] + sum(l..r))`.

5. Rearrange the band score using prefix sums:  
   `dp[l] + prefix[r+1] - prefix[l] = prefix[r+1] + (dp[l] - prefix[l])`.

6. Now the problem becomes: over all currently valid left boundaries `l`, maintain the maximum value of `dp[l] - prefix[l]`. As the sliding window advances, invalid left indices expire. A monotonic deque over these candidate values gives O(1) amortized updates and queries.

## 🧩 Algorithm Walkthrough
1. **Prefix sums for O(1) band sums.**  
   Build `prefix[i+1] = prefix[i] + nums[i]`. Then any band sum is `prefix[r+1] - prefix[l]`. This converts interval scoring into a form that can be combined with DP state.

2. **DP over array prefixes.**  
   Let `dp[i]` be the maximum score obtainable using only `nums[0..i-1]`. The invariant is standard: `dp[i]` already accounts for all non-overlapping choices entirely inside that prefix.

3. **Sliding window for validity.**  
   Maintain a left pointer `L` and two monotonic deques over indices: one decreasing by value for the window maximum, one increasing for the minimum. After inserting `r`, advance `L` until `nums[maxDeque.front] - nums[minDeque.front] <= k`. Then every start `l >= L` yields a valid band `l..r`, and every `l < L` is invalid.

4. **Transform the recurrence.**  
   For fixed `r`,  
   `bestEndingAtR = prefix[r+1] + max_{l in [L..r]}(dp[l] - prefix[l])`.  
   So we need the maximum transformed candidate among currently valid starts.

5. **Monotonic deque over candidate starts.**  
   Maintain another deque of indices `l` in the active range `[L..r]`, ordered by decreasing `dp[l] - prefix[l]`. Expire indices `< L`. Before processing `r`, add candidate index `r` for future windows and ensure the deque remains monotonic. The front always holds the best start for any valid band ending at the current `r`.

6. **State transition.**  
   Compute  
   `take = prefix[r+1] + value(frontCandidate)` and  
   `skip = dp[r]`.  
   Then `dp[r+1] = max(skip, take)`. This preserves the invariant that each prefix optimum either ignores `nums[r]` or ends with one valid final band.

7. **Why this abstraction fits.**  
   This is a fusion of **Sliding Window + Monotonic Queues + Prefix-DP**. Sliding window enforces the local admissibility constraint; DP handles non-overlap globally; the monotonic candidate deque removes the need to scan all valid starts per endpoint.

## 📊 Worked Example
Use `nums = [4, 2, 3, 7, 6, 5]`, `k = 2`.

`prefix = [0, 4, 6, 9, 16, 22, 27]`

| r | nums[r] | valid start range | best `dp[l]-prefix[l]` | take | skip | `dp[r+1]` |
|---|---:|---|---:|---:|---:|---:|
| 0 | 4 | `[0..0]` | `0` | `4` | `0` | `4` |
| 1 | 2 | `[0..1]` | `0` | `6` | `4` | `6` |
| 2 | 3 | `[0..2]` | `0` | `9` | `6` | `9` |
| 3 | 7 | `[3..3]` | `dp[3]-prefix[3]=0` | `16` | `9` | `16` |
| 4 | 6 | `[3..4]` | `0` | `22` | `16` | `22` |
| 5 | 5 | `[3..5]` | `0` | `27` | `22` | `27` |

At `r = 3`, the old window `[0..3]` becomes invalid because `7 - 2 > 2`, so starts before `3` expire. The DP then naturally stitches `[4,2,3]` and `[7,6,5]` into the optimal total `27`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` amortized. Each index is inserted and removed at most once from the max deque, min deque, and candidate deque. There is no nested scan over starts or intervals. At `10^6` elements this remains practical in a compiled language; at `10^9`, even linear time becomes a systems problem rather than an algorithm problem.

### Space Complexity
`O(n)` for `dp` and prefix sums, plus `O(n)` worst-case across the deques, though each deque individually holds at most `n` indices. Space can be reduced to `O(n)` without change, but not to `O(1)` because future transitions need historical `dp[l] - prefix[l]` values.

## 💡 Key Takeaways
- If the problem says “contiguous subarray with bounded max/min” and asks for efficient processing, think sliding window with monotonic deques immediately.
- If it also says “choose non-overlapping segments for maximum total score,” that is a strong signal to combine interval validity with prefix DP.
- The active candidate set is starts `l` in the current valid window, not arbitrary prior indices; expiring stale starts is the main correctness trap.
- Be careful about DP indexing: `dp[i]` should represent the first `i` elements, so a band `l..r` transitions from `dp[l]` to `dp[r+1]`.
- The transferable design insight is to separate local feasibility maintenance from global optimization state; this decomposition is what makes streaming-scale interval decisions tractable.

## 🚀 Variations & Further Practice
- Allow at most `m` bands instead of unlimited selection. The twist is adding a second DP dimension for band count while preserving efficient window-based candidate maintenance.
- Replace the validity rule `max - min <= k` with a cost function over the window, such as variance or median deviation. The harder part is that min/max deques no longer suffice, so the local-feasibility structure changes.
- Make the array circular. Now non-overlap and validity interact across the wrap boundary, requiring either duplication plus case splitting or a more careful interval scheduling formulation.