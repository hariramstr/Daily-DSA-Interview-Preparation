# Maximum Gain from Reversing One Sales Streak

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane's Algorithm

---

## 🗂 Problem Overview
Given an integer array `nums`, you may reverse at most one contiguous segment, then compute the maximum sum of any contiguous subarray in the resulting array. Return the largest value achievable over all such choices, including doing nothing. The challenge is that reversal changes only ordering, not values, so brute-forcing every segment and rerunning maximum-subarray logic is too expensive. With `n <= 2000`, the target is an `O(n^2)` dynamic-programming solution, not `O(n^3)` or worse.

## 🌍 Engineering Impact
This pattern shows up anywhere local reordering changes downstream aggregation quality: ad-ranking pipelines that can reorder a bounded window, compiler optimization passes that reshuffle instruction blocks, stream processors that can repair one out-of-order burst, or portfolio analytics that evaluate one allowed correction to a time series. At scale, the issue is not computing a single best segment, but reasoning about how one structural edit changes all candidate segments. Without the right DP decomposition, teams ship cubic scans that collapse under batch workloads, interactive tuning, or repeated what-if evaluation. The scalable insight is to separate unaffected prefixes/suffixes from the reordered interior.

## 🔍 Problem Statement
You are given an integer array `nums` where each element represents daily profit impact: positive values are gains, negative values are losses. You may perform **at most one** operation: choose a contiguous subarray and reverse its order. After that optional reversal, compute the maximum sum of any contiguous subarray in the modified array.

Return the largest achievable contiguous-subarray sum.

Constraints:

- `1 <= nums.length <= 2000`
- `-10^4 <= nums[i] <= 10^4`
- Result fits in a 32-bit signed integer

Examples:

- `nums = [4, -10, 3, 5]` → `12`  
  Reverse `[-10, 3, 5]` → `[4, 5, 3, -10]`, best subarray is `[4, 5, 3]`.

- `nums = [-2, 8, -1, 6, -7]` → `13`  
  Best achievable answer is `13`; reversal does not improve it.

The key constraint is that trying all reversals and recomputing Kadane each time is `O(n^3)`, which is unnecessary for `n = 2000`.

## 🪜 How to Solve This
1. Start with the baseline: if no reversal helps, the answer is just standard Kadane on the original array.

2. Ask what a reversal can actually change. It does **not** change the multiset of values inside the chosen window; it only changes which values become adjacent to the left and right outside boundaries.

3. That observation narrows the problem: any improved maximum subarray after reversal must either:
   - lie completely outside the reversed range, or
   - cross the reversed range and benefit from a better arrangement of its endpoints.

4. For a fixed interval `[l..r]` that gets reversed, a subarray crossing it looks like:
   - some suffix ending at `l-1`,
   - plus a subarray inside `[l..r]` but read in reverse order,
   - plus some prefix starting at `r+1`.

5. Reversal preserves sums of contiguous pieces inside `[l..r]`; what changes is which internal prefix/suffix can connect to the outside. That suggests precomputing best prefix/suffix contributions and then evaluating all `(l, r)` pairs.

6. Use dynamic programming around Kadane-style states:
   - best subarray ending at each index,
   - best starting at each index,
   - prefix/suffix best sums for extension,
   - DP over intervals to know the best subarray inside a reversed window.

7. Once those pieces exist, each reversal candidate is combined in `O(1)`, giving total `O(n^2)`.

## 🧩 Algorithm Walkthrough
1. **Compute baseline Kadane states.**  
   Build `endHere[i]`: maximum subarray sum ending at `i`, and `startHere[i]`: maximum subarray sum starting at `i`. Also compute the global no-reversal answer. This is classic **Dynamic Programming / Kadane’s Algorithm**: each state captures the best extend-or-restart choice.

2. **Precompute attachable boundary gains.**  
   Let `leftGain[i]` be the best suffix sum ending exactly at `i`, and `rightGain[i]` the best prefix sum starting exactly at `i`. These are the only outside pieces that can connect to a reversed block. The invariant: any crossing subarray touching the left boundary must use a suffix of the left side; similarly on the right it must use a prefix.

3. **Build interval DP for internal reversed contribution.**  
   Define `mid[l][r]` as the maximum subarray sum obtainable within `nums[l..r]` after that interval is reversed, under the condition that the chosen subarray may touch either boundary as needed for outside concatenation. Because reversing `[l..r]` maps prefixes to suffixes and vice versa, this DP can be derived from prefix/suffix sums over the original interval.

4. **Enumerate all reversal windows `[l..r]`.**  
   For each pair, evaluate candidates:
   - best entirely outside the window,
   - best entirely inside the reversed window,
   - best crossing from left into reversed block,
   - best crossing from reversed block into right,
   - best crossing both sides.  
   Each is assembled from precomputed left/right gains and interval prefix/suffix totals.

5. **Take the maximum over all cases.**  
   Correctness follows from exhaustive structural decomposition: every optimal post-reversal subarray either avoids the reversed window or intersects it in one contiguous piece. Since the reversal only changes order inside one interval, these precomputed boundary-compatible summaries are sufficient.

## 📊 Worked Example
Use `nums = [4, -10, 3, 5]`.

| Step | State |
|---|---|
| Original Kadane | best = `8` from `[3, 5]` |
| Try reversal `[1..3]` | segment `[-10, 3, 5]` becomes `[5, 3, -10]` |
| New array | `[4, 5, 3, -10]` |
| Best subarray after reversal | `[4, 5, 3]` = `12` |

Trace by decomposition:

1. Left side before reversal window: index `0`, best suffix ending there is `4`.
2. Reversed block contributes best prefix after reversal: `[5, 3]` = `8`.
3. Right side after index `3` is empty, so contribution is `0`.
4. Combined crossing sum = `4 + 8 = 12`.

Other reversals are weaker:

- reverse `[0..1]` → `[-10, 4, 3, 5]`, best = `12`? No, best is `4 + 3 + 5 = 12`, same.
- reverse `[0..3]` → `[5, 3, -10, 4]`, best = `8`.

Maximum achievable answer is `12`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)`. The dominant work is evaluating all reversal intervals `(l, r)`, of which there are `O(n^2)`, while each candidate is scored using precomputed DP summaries in `O(1)`. At `10^6` elements this is already too large; at `10^9` it is completely infeasible, so this approach is appropriate specifically because `n <= 2000`.

### Space Complexity
`O(n^2)` if you materialize interval DP tables, plus `O(n)` for Kadane-style prefix/suffix arrays. The interval table owns the space. It can be reduced toward `O(n)` only with more intricate rolling-state derivations, usually at the cost of readability and implementation risk.

## 💡 Key Takeaways
- If a problem allows one local reorder but asks for a global optimum afterward, look for a decomposition into unaffected regions plus a boundary-sensitive middle.
- “At most one edit” plus “maximum contiguous subarray” is a strong signal for Kadane-style DP augmented with extra state, not brute-force simulation.
- Reversal preserves sums of contiguous pieces inside the chosen window; the only real leverage is how prefixes and suffixes reconnect across its boundaries.
- Be careful not to double-count the middle interval when combining left suffix, reversed-window contribution, and right prefix.
- In production systems, bounded structural edits are often tractable only when you summarize regions with composable boundary states rather than recomputing whole pipelines after each edit.

## 🚀 Variations & Further Practice
- Allow **up to `k` reversals** instead of one. The twist is state explosion: you now need DP over edit count and interval interactions, not a single boundary merge.
- Replace reversal with **one deletion or one replacement**. The harder part is that values change, not just order, so interval invariants differ from the reversal case.
- Ask for the **maximum circular subarray sum after one reversal**. The twist is combining wrap-around Kadane logic with a local reorder, which creates two interacting boundary systems.