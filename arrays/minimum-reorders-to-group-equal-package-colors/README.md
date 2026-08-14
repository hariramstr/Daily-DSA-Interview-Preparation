# Minimum Reorders to Group Equal Package Colors

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Hash Map

---

## 🗂 Problem Overview
Given an array `colors`, move the fewest elements so that every distinct value appears in exactly one contiguous block. A move removes one element and reinserts it anywhere. The block order is unconstrained, which means the problem is not sorting by value; it is maximizing how many elements can remain in-place under some valid block ordering. With `n` up to `2 * 10^5`, brute-force over permutations of colors or interval rearrangements is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere records must be re-clustered by key while minimizing churn: log compaction by tenant, warehouse wave planning, stream partition repair, columnar storage re-segmentation, and search-index posting list maintenance. At scale, the difference between “rebuild everything” and “preserve the largest already-valid subsequence” is the difference between bounded write amplification and operational pain. The useful abstraction is not physical movement but maximal retained structure under a relaxed target ordering. That enables near-linear planning, predictable memory use, and incremental repair strategies instead of full reshuffles.

## 🔍 Problem Statement
You are given an array `colors` where `colors[i]` is the color of the `i`th package. In one operation, you may remove one package from its current position and insert it anywhere else. Return the minimum number of operations required so that, in the final array, each distinct color appears in one contiguous segment.

Equivalent view: keep the largest subsequence already compatible with some ordering of color blocks, and move everything else.

Constraints:

- `1 <= colors.length <= 2 * 10^5`
- `1 <= colors[i] <= 10^5`
- Up to `10^5` distinct colors

Examples:

- `colors = [3, 1, 3, 2, 1, 2]` → `3`
- `colors = [4, 4, 2, 2, 3, 3]` → `0`

The key constraint is the input size: any approach that tries block-order permutations, pairwise interval checks, or quadratic DP will fail.

## 🪜 How to Solve This
1. Read the operation carefully → removing and reinserting elements means relative order among the elements you keep does not change. So the real objective is: **what is the longest subsequence we can leave untouched?**

2. Ask what a valid untouched subsequence looks like → once a color appears, all kept occurrences of that color must be in one contiguous run inside the subsequence. That means the subsequence corresponds to some order of color blocks.

3. Compress the problem by color → for each color, only its first and last positions matter structurally. If color `b` starts after color `a` ends, then block `a` can appear before block `b` without conflict.

4. That turns the problem into DP on colors ordered by positions → if intervals do not overlap, their full counts can chain. If they do overlap, you can only extend through local adjacency in the original array.

5. Maintain the best valid subsequence ending at each color using a hash map → answer is `n - bestKept`.

This is a dynamic-programming-on-intervals problem disguised as array reordering.

## 🧩 Algorithm Walkthrough
1. **Collect per-color metadata using a Hash Map.**  
   For each color, compute:
   - `count[color]`
   - `first[color]`
   - `last[color]`  
   This models each color as an interval `[first, last]` plus weight `count`. The invariant is that every color block in any valid kept subsequence must respect some left-to-right interval order.

2. **Sort distinct colors by `first` position.**  
   This gives the only meaningful order for deciding which color blocks can precede others. If `last[a] < first[b]`, then all kept copies of `a` can appear before all kept copies of `b` with no positional contradiction.

3. **Run weighted interval DP for non-overlapping colors.**  
   Let `dp[color]` be the maximum kept subsequence length ending with that color as the last block.  
   Base case: keep all occurrences of that color, so start from `count[color]`.  
   Transition: extend from the best color whose interval ends before this color starts.

4. **Add adjacency-based extension for overlapping intervals.**  
   If `colors[i] = x` and `colors[i-1] = y` with `x != y`, then a subsequence ending in block `y` can transition into block `x` by keeping this occurrence of `x`. Repeating this across occurrences accumulates the best contiguous block-order-compatible subsequence even when intervals overlap globally.  
   Invariant: `bestEnd[color]` always stores the best valid subsequence whose last kept element belongs to `color`.

5. **Take the maximum kept length and subtract from `n`.**  
   Every element not in that subsequence must be moved. This is correct because insertion can realize any block order while preserving the kept subsequence exactly.

Pattern: **Dynamic Programming + Hash Map state compression**. The hash map tracks best subsequence values per color; the DP captures “keep vs move” under block-order constraints.

## 📊 Worked Example
Take `colors = [3, 1, 3, 2, 1, 2]`.

| i | colors[i] | best ending at color after processing `i` |
|---|---:|---|
| 0 | 3 | `dp[3] = 1` |
| 1 | 1 | start new block after `3` → `dp[1] = 1` |
| 2 | 3 | extend existing `3` block or restart after `1` → `dp[3] = 2` |
| 3 | 2 | transition from best prior block → `dp[2] = 2` |
| 4 | 1 | extend `1` using prior best before this occurrence → `dp[1] = 2` |
| 5 | 2 | extend `2` → `dp[2] = 3` |

The best valid kept subsequence has length `3`, for example one compatible with block order `1 -> 2` or `3 -> 2`. Since `n = 6`, minimum moves = `6 - 3 = 3`.

The important observation is that we are not keeping a sorted subsequence; we are keeping the longest subsequence consistent with **some** contiguous color-block ordering.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + k log k)` or `O(n log k)` depending on the exact DP implementation, where `k` is the number of distinct colors. The dominant work is building per-color metadata and ordering color states. This is comfortable at `10^6` scale, but anything quadratic is dead long before `10^9`.

### Space Complexity
`O(k)` auxiliary space for the hash maps storing counts, interval endpoints, and DP state per distinct color. This is already close to minimal because the algorithm must at least remember one state per color. Reducing it further usually trades away clarity without changing asymptotics.

## 💡 Key Takeaways
- If an operation lets you remove items and reinsert them arbitrarily, reframe the problem as maximizing the subsequence you can keep unchanged.
- When values must end up in contiguous groups but group order is free, think “DP over value blocks,” not “sorting” or “windowing.”
- Do not confuse total frequency with keepable frequency; overlapping color intervals can prevent chaining whole color counts.
- Be careful with transitions between colors: the DP state is about the **last block in the kept subsequence**, not the last index processed.
- The production-grade insight is to optimize for preserved structure under a flexible target layout; that often beats explicit reconstruction in both throughput and write amplification.

## 🚀 Variations & Further Practice
- Require the final color blocks to appear in increasing color order. Twist: the free block-order choice disappears, so the DP collapses into a stricter ordered subsequence problem.
- Assign a different move cost per package. Twist: maximize retained total weight instead of retained count, turning the state into weighted DP.
- Allow up to `m` contiguous segments per color instead of exactly one. Twist: state must track segment budget, which increases complexity from per-color DP to multi-state optimization.