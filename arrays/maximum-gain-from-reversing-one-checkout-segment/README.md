# Maximum Gain from Reversing One Checkout Segment

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane's Algorithm

---

## 🗂 Problem Overview
Given an integer array `items`, compute the maximum subarray sum obtainable after reversing at most one contiguous segment. Reversal changes only order, not values, and you may also skip the reversal. The output is a single 64-bit integer: the best contiguous-run profit in the modified array. What makes this non-trivial is the input size: `2 * 10^5` rules out brute-force enumeration of reversal ranges and post-reversal Kadane recomputation.

## 🌍 Engineering Impact
This pattern shows up anywhere a local reorder can change the best contiguous window score: checkout or clickstream analytics, market microstructure replay, compiler instruction scheduling, media timeline optimization, and ranking pipelines that permit one bounded rewrite before scoring. At scale, naive “try every rewrite and rescore” approaches collapse under quadratic behavior and unstable latency tails. The useful abstraction is not the reversal itself, but how it changes boundary connectivity between profitable prefixes and suffixes. That shift enables linear-time preprocessing, predictable memory use, and a design that survives large arrays, batch scoring, and latency-sensitive services.

## 🔍 Problem Statement
You are given an array `items` of length `n`, where `1 <= n <= 2 * 10^5` and each `items[i]` lies in `[-10^9, 10^9]`. Positive values increase profit; negative values reduce it. You may reverse **at most one** contiguous segment, then choose **any** contiguous subarray in the resulting array. Return the maximum possible subarray sum.

Examples:

- `items = [4, -10, 3, 5]` → `12`
- `items = [-2, 8, -1, -3, 7]` → `14`

Important edge cases:
- all values negative → answer is the largest single element
- reversal may be skipped
- reversing a segment does **not** change total sum inside that segment, only which values become adjacent to the chosen subarray boundaries

The key constraint is `n = 2 * 10^5`: any `O(n^2)` or “reverse and rerun Kadane” strategy is too slow.

## 🪜 How to Solve This
1. Start with ordinary Kadane → without reversal, the problem is standard maximum subarray sum.

2. Ask what reversal can actually improve → it cannot change values, only adjacency. So the gain comes from connecting a strong suffix on one side with a strong prefix on the other side after the middle segment is reversed.

3. Reframe the chosen post-reversal subarray → if it intersects the reversed segment, then in original coordinates it looks like:
   - a suffix ending at the left boundary,
   - plus some internal segment whose order is irrelevant to its sum,
   - plus a prefix starting at the right boundary.

4. That suggests dynamic programming on directional bests:
   - best suffix sums ending at each index
   - best prefix sums starting at each index
   - best subarray entirely inside prefixes/suffixes for the “no-crossing” cases

5. The remaining challenge is combining boundaries efficiently. This is where Kadane-style states matter: we do not need to know the exact reversal, only the best attachable left contribution and right contribution around every split.

6. Once you see reversal as a boundary-rewiring operation rather than an array mutation, a linear-time solution becomes natural.

## 🧩 Algorithm Walkthrough
1. **Compute baseline Kadane result.**  
   Use Kadane’s algorithm to get the best subarray sum with no reversal. This is a lower bound on the answer and handles cases where reversal is useless. Invariant: `best_no_rev` is the maximum subarray sum seen so far.

2. **Build left-to-right attachment states.**  
   For each index `i`, compute:
   - `end_here[i]`: maximum sum of a subarray ending at `i`
   - `best_left[i]`: maximum subarray sum within `items[0..i]`  
   This is standard dynamic programming / Kadane. Invariant: `end_here[i]` is the best suffix attachable to a future boundary on the right.

3. **Build right-to-left symmetric states.**  
   For each index `i`, compute:
   - `start_here[i]`: maximum sum of a subarray starting at `i`
   - `best_right[i]`: maximum subarray sum within `items[i..n-1]`  
   Invariant: `start_here[i]` is the best prefix attachable to a future boundary on the left.

4. **Model one reversal as joining two attachable sides.**  
   If we reverse a segment `[l..r]`, a post-reversal subarray can take:
   - a left suffix ending at `l-1`
   - then some subarray inside `[l..r]`
   - then a right prefix starting at `r+1`  
   Since reversal preserves sums of internal contiguous choices, the key gain is from boundary reconnection. The best crossing candidate around a split is therefore driven by `end_here` on the left and `start_here` on the right.

5. **Precompute best internal contribution.**  
   The internal reversed segment can contribute any contiguous sum from the original interval. To support this efficiently, maintain prefix sums and the best subarray values over ranges via Kadane-derived scans. This lets each boundary combination be evaluated in `O(1)` amortized after linear preprocessing.

6. **Scan all valid boundary pairs implicitly.**  
   Rather than enumerating every `[l, r]`, maintain the best left contribution seen so far and combine it with right-start states while advancing. This is the dynamic-programming compression that removes the quadratic factor. Invariant: at position `r`, the maintained state represents the best achievable value for any reversal ending at or before `r`.

7. **Take the maximum over all forms.**  
   The answer is the max of:
   - no reversal
   - subarray entirely left of the reversed zone
   - entirely right of the reversed zone
   - crossing through the reversed zone with newly improved adjacency

The governing pattern is **Dynamic Programming with Kadane-style directional states**. Kadane gives optimal attachable prefixes/suffixes; the reversal only changes how those states can be connected.

## 📊 Worked Example
Take `items = [4, -10, 3, 5]`.

| i | items[i] | end_here[i] | best_left[i] | start_here[i] | best_right[i] |
|---|---:|---:|---:|---:|---:|
| 0 | 4   | 4  | 4  | 4  | 12 |
| 1 | -10 | -6 | 4  | -2 | 8  |
| 2 | 3   | 3  | 4  | 8  | 8  |
| 3 | 5   | 8  | 8  | 5  | 5  |

Trace:
1. Baseline Kadane gives `8` from subarray `[3, 5]`.
2. Consider reversing `[1..3] = [-10, 3, 5]` → array becomes `[4, 5, 3, -10]`.
3. Now the best contiguous run is `[4, 5, 3] = 12`.
4. Why this works: reversal moved the profitable suffix `[3, 5]` next to the profitable left prefix `[4]`, eliminating the `-10` barrier between them.
5. No other reversal creates a better left-right connection, so the answer is `12`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each Kadane-style DP pass is linear, and the reversal-aware combination step also scans the array once while maintaining compressed state instead of enumerating all reversal intervals. At `10^6` elements this is practical in native code; at `10^9`, even linear scans become throughput-bound and require distributed or streaming reformulation.

### Space Complexity
`O(n)` for directional DP arrays such as `end_here`, `start_here`, `best_left`, and `best_right`. Some states can be reduced to `O(1)` if you only need the final value, but keeping arrays simplifies boundary composition and reduces implementation risk.

## 💡 Key Takeaways
- If an operation only reorders a contiguous region, ask how it changes **adjacency at boundaries** rather than simulating the mutation.
- “Maximum subarray after one structural edit” is usually a signal for **Kadane plus directional DP**, not brute-force interval testing.
- Use 64-bit arithmetic throughout; with `n = 2e5` and values near `1e9`, 32-bit sums overflow immediately.
- Be precise about contiguity after reversal: many intuitive examples accidentally stitch together elements that are no longer adjacent.
- The transferable design insight is to replace expensive what-if rewrites with precomputed attachable states, then optimize the composition layer.

## 🚀 Variations & Further Practice
- Allow **up to `k` reversals**. The twist is state explosion: you now need DP over edit count and boundary attachment, not a single Kadane-style composition.
- Replace reversal with **one swap of arbitrary elements**. Harder because the edit is non-local; adjacency changes at two distant points instead of one segment boundary pair.
- Ask for the **actual reversal interval and resulting subarray**, not just the value. Same core DP, but now you must carry predecessor metadata and resolve ties consistently.