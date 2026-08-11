# Longest Price Feed Window With Limited Direction Reversals

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an integer array `prices` and an integer `k`, find the maximum length contiguous subarray whose non-flat adjacent comparisons switch direction at most `k` times. Each adjacent pair contributes `+1`, `-1`, or `0`; zero means flat and must be ignored when counting reversals. The challenge is that window validity depends on transitions between comparisons, not on element frequencies, so a naive expand-and-recompute approach is too slow for arrays up to `2 * 10^5`.

## 🌍 Engineering Impact
This pattern shows up in market-data processing, telemetry trend analysis, anomaly detection, and stream quality scoring: you care less about raw values than about how often local trend direction flips. In production, recomputing trend segments for every candidate window is quadratic and collapses under sustained feed volume. A linear sliding-window formulation lets you score long streams online, bound memory, and preserve latency under bursty input. The broader architectural lesson is to transform raw events into a compact derivative signal, then maintain window validity over that reduced representation instead of repeatedly rescanning source data.

## 🔍 Problem Statement
You are given `prices`, where `prices[i]` is the asset price at time `i`, and an integer `k`. For any contiguous window `prices[l..r]`, inspect each adjacent pair inside the window:

- increasing if `prices[i] < prices[i+1]`
- decreasing if `prices[i] > prices[i+1]`
- flat if `prices[i] == prices[i+1]`

Flat steps do not count toward direction changes. After removing flats, the remaining direction sequence is valid if it changes between `+` and `-` at most `k` times. Return the maximum valid window length.

Constraints:

- `1 <= prices.length <= 200000`
- `-10^9 <= prices[i] <= 10^9`
- `0 <= k <= prices.length`

Examples:

- `prices = [5,7,9,8,6,6,10,12], k = 1` → `6`
- `prices = [4,4,4,3,2,5,7,6,1], k = 2` → `9`

The key constraint is input size: any solution that re-evaluates reversals per window will time out.

## 🪜 How to Solve This
1. Read the condition carefully → validity is not about values inside the window, but about adjacent **comparisons**.
2. Convert the array into a direction stream of length `n - 1`: `+1`, `-1`, or `0`.
3. Notice what actually matters → not the count of `+` or `-`, but how many times consecutive **non-zero** directions differ.
4. That suggests a sliding window over comparisons, not over raw prices. A price window of length `L` corresponds to a comparison window of length `L - 1`.
5. As the right pointer expands, only one new comparison enters. It can increase the reversal count by at most one: specifically when the new non-zero direction disagrees with the previous non-zero direction already inside the window.
6. When the reversal count exceeds `k`, move the left pointer forward and remove the effect of the outgoing comparison. Again, only a local transition is affected.
7. Track the longest valid comparison window, then convert back to price-window length by adding one.

That is the key mental move: model the problem on the derivative signal, then maintain reversal count incrementally.

## 🧩 Algorithm Walkthrough
1. **Build the direction array** using adjacent prices:  
   `dir[i] = sign(prices[i+1] - prices[i])`, where values are `-1`, `0`, or `1`.  
   This isolates the only information relevant to smoothness.

2. **Define the window on `dir`** with two pointers `[left, right]`.  
   If the direction window has length `m`, the corresponding price window has length `m + 1`.  
   This mapping is exact because each comparison connects two consecutive prices.

3. **Count reversals inside the current direction window.**  
   A reversal occurs at index `i` when `dir[i]` and the previous non-zero direction before `i` are both non-zero and different.  
   Operationally, it is simpler to precompute whether adjacent direction positions form a reversal after ignoring zeros, or maintain this dynamically by tracking nearest non-zero neighbors.

4. **Use a Two Pointers / Sliding Window strategy.**  
   Expand `right`, update the reversal count contributed by the new direction, and keep the invariant:  
   **the current window contains at most `k` non-flat direction reversals.**

5. **Shrink from the left when invalid.**  
   Removing `dir[left]` can only change reversal accounting locally around that boundary. Update the count, increment `left`, and continue until valid again.

6. **Record the best window.**  
   For every valid `[left, right]` in `dir`, candidate answer is `right - left + 2` price elements.  
   Handle `n = 1` separately or naturally via initialization.

Why this abstraction works: sliding windows are effective when validity can be updated incrementally. Here, although the rule sounds global, reversals are induced by local transitions in the compressed sign stream.

## 📊 Worked Example
Take `prices = [5,7,9,8,6,6,10,12]`, `k = 1`.

Directions between adjacent prices:

| i | pair   | dir |
|---|--------|-----|
| 0 | 5→7    | +   |
| 1 | 7→9    | +   |
| 2 | 9→8    | -   |
| 3 | 8→6    | -   |
| 4 | 6→6    | 0   |
| 5 | 6→10   | +   |
| 6 | 10→12  | +   |

Ignoring `0`, the sign stream is `+,+,-,-,+,+`.

Trace the window on directions:

1. Extend through `+,+,-,-` → one reversal (`+` to `-`) → valid.
2. Include `0` → no effect.
3. Include next `+` → second reversal (`-` to `+`) → invalid for `k = 1`.
4. Move `left` rightward until the first `+ → -` transition leaves the window.
5. Best valid direction window spans indices `1..5`, corresponding to prices indices `1..6`:  
   `[7,9,8,6,6,10]`, length `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each comparison enters the sliding window once and leaves once, so both pointers move monotonically across the `n - 1` direction positions. That remains practical at `10^6` elements; at `10^9`, even linear scans become bandwidth-bound and require streaming or distributed partitioning.

### Space Complexity
`O(n)` if you materialize the direction array or auxiliary neighbor metadata. The core logic can be reduced toward `O(1)` extra space by computing directions on the fly, but boundary-update logic becomes less transparent and harder to verify.

## 💡 Key Takeaways
- If a subarray constraint is defined on adjacent relationships rather than values, first transform the input into a comparison or delta stream.
- Sliding window still applies when the validity metric is “number of local transitions,” not just counts or sums, provided updates are incremental.
- Flat steps are ignored, not treated as a third direction; counting them as reversals is the most common correctness bug.
- The answer is a price-window length, but the algorithm runs on `n - 1` comparisons, so the final conversion is `dir_window_length + 1`.
- In production stream processing, deriving a compact state signal first often turns an expensive rescoring problem into a single-pass online algorithm.

## 🚀 Variations & Further Practice
- Allow at most `k` reversals **and** require each monotonic run to have minimum length `m`; harder because validity depends on both transition count and segment sizes.
- Support online updates with append and expire operations over a time-based window; harder because the structure must maintain reversal counts under continuous eviction.
- Replace binary direction with weighted trend classes (strong up, weak up, flat, weak down, strong down) and limit category switches; harder because “ignore flat” becomes a more general state-compression problem.