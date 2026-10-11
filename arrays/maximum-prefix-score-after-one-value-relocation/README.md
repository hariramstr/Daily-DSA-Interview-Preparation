# Maximum Prefix Score After One Value Relocation

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Greedy

---

## 🗂 Problem Overview
Given an integer array, count how many prefix sums are strictly positive. You may relocate at most one element: remove `nums[j]` and insert it anywhere else while preserving the order of all remaining elements. The goal is to maximize that count.

The challenge is that one move changes an entire contiguous range of prefix sums, not just one position. With `n` up to `2 * 10^5`, trying every relocation and recomputing scores is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere one localized reorder changes cumulative state over a long suffix: streaming pipelines with backlog debt, distributed rate-limiters with burst credits, ad-serving budget pacing, and job schedulers with one expensive task moved across a queue. The operational question is rarely “what is the best local swap,” but “which single intervention improves the most downstream checkpoints.”

At scale, brute-force replay is unacceptable because every candidate move invalidates many cumulative metrics. The useful abstraction is to reason about range effects on prefix state, then choose the one relocation whose impact dominates globally.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n`, where `1 <= n <= 2 * 10^5` and `-10^9 <= nums[i] <= 10^9`. Define `prefix[i] = nums[0] + ... + nums[i]`. The prefix score is the number of indices `i` such that `prefix[i] > 0`.

You may perform at most one relocation: choose one element `nums[j]`, remove it, and insert it at any position, including the front or the back. All other elements must remain in their original relative order.

Return the maximum possible prefix score after at most one such move.

Examples:

- `nums = [4, -5, 2, 1]` → `4`  
  Move `-5` to the end: `[4, 2, 1, -5]`, prefix sums `[4, 6, 7, 2]`.

- `nums = [-2, 3, -1, 2]` → `3`  
  Move `-2` to index `2`: `[3, -1, -2, 2]`, prefix sums `[3, 2, 0, 2]`.

The key constraint is `n = 2 * 10^5`: any `O(n^2)` relocation search is dead on arrival.

## 🪜 How to Solve This
1. Start from prefix sums. A relocation does not arbitrarily change the array; it shifts one value across a contiguous block, so many prefix sums move by the same amount.

2. Split the move types by sign:
   - Moving a **negative** value later increases every prefix between its old and new positions.
   - Moving a **positive** value earlier increases every prefix between its new and old positions.
   - Moving a value in the opposite direction hurts prefixes, so it is never useful for maximizing score.

3. That means each candidate move is really: choose a range of prefixes and add a constant `x` to all of them.

4. For a fixed moved value, the best target position is obvious:
   - negative `x`: move it as far right as possible;
   - positive `x`: move it as far left as possible.
   Any shorter range helps fewer prefixes.

5. So the problem reduces to two one-sided scans:
   - For each negative element, how many currently non-positive prefixes after it become positive if we add `-x`?
   - For each positive element, how many currently non-positive prefixes before it become positive if we add `x`?

6. Precompute prefix sums, then answer those “how many values in a range lie in `[-x+1, 0]`?” queries efficiently with offline sorting and a Fenwick tree.

## 🧩 Algorithm Walkthrough
1. **Compute prefix sums and the base score.**  
   Let `pref[i]` be the sum through index `i`. The current answer starts as the count of `pref[i] > 0`. This is the invariant baseline: every relocation is measured as an incremental gain over this score.

2. **Characterize useful relocations greedily.**  
   If `nums[j] < 0`, moving it right increases `pref[k]` by `-nums[j]` for every `k` in `[j, newPos-1]`. Best choice: move it to the end, maximizing the helped range.  
   If `nums[j] > 0`, moving it left increases `pref[k]` by `nums[j]` for every `k` in `[newPos, j-1]`. Best choice: move it to the front.  
   This is the core **Greedy + Prefix Sum** insight: once sign is fixed, the optimal destination is extremal.

3. **Translate gain into counting thresholds.**  
   A prefix contributes extra score only if it was previously `<= 0` and becomes `> 0`.  
   - For negative `x = nums[j] < 0`, gain is the count of `pref[k]` with `k >= j` and `0 >= pref[k] > x`.  
   - For positive `x = nums[j] > 0`, gain is the count of `pref[k]` with `k < j` and `-x < pref[k] <= 0`.

4. **Answer range-threshold counts offline.**  
   Build queries of the form: in prefix-index range `[L, R]`, count values `pref[k] <= T`. Then each gain becomes  
   `count(<= 0) - count(<= bound)` for an appropriate lower bound.  
   Sort prefix sums by value, sort queries by threshold, and process with a **Fenwick Tree** over indices. The tree maintains how many prefix positions with value `<= currentThreshold` have been activated.

5. **Evaluate all candidates and take the maximum.**  
   For each index, compute the gain from moving that element in its beneficial direction, add it to the base score, and keep the best result. The invariant is monotonic: every candidate is evaluated against the same original prefix array, because only one relocation is allowed.

## 📊 Worked Example
Take `nums = [4, -5, 2, 1]`.

| i | nums[i] | pref[i] | pref[i] > 0 |
|---|---------|---------|-------------|
| 0 | 4       | 4       | yes         |
| 1 | -5      | -1      | no          |
| 2 | 2       | 1       | yes         |
| 3 | 1       | 2       | yes         |

Base score = `3`.

Now test useful relocations:

1. Move `-5` right to the end.  
   Prefix indices affected: `[1..3]`. Each gains `5`.  
   Old affected prefixes: `[-1, 1, 2]` → new: `[4, 6, 7]`.  
   Newly positive count = `1` (`-1` becomes `4`). Total = `4`.

2. Move `4` left: impossible, already at front.

3. Move `2` left to front.  
   Affected prefixes before index `2`: `[4, -1]` gain `2` → `[6, 1]`.  
   Newly positive count = `1`, but moving `2` also changes array shape; total still reaches `4`? No, because the relocated element itself shifts positions and the exact resulting prefixes do not beat the previous best.

Best result is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. Computing prefix sums is linear; the dominant work is offline query processing with sorting plus Fenwick tree updates and range-count queries. This is practical for `2 * 10^5` and still viable around `10^6` with careful constants. At `10^9`, even linear scans are infeasible, so the problem becomes a distributed or external-memory exercise.

### Space Complexity
`O(n)`. Space is owned by the prefix array, candidate queries, sorted event lists, and the Fenwick tree. You can reduce some auxiliary allocations by streaming query construction, but asymptotically the working set remains linear unless you accept more complex in-place coordination.

## 💡 Key Takeaways
- If one move changes many downstream cumulative values by the same constant, think prefix sums plus range-effect reasoning, not simulation.
- When a relocation preserves relative order of all other elements, useful moves often collapse to extremal destinations via a greedy argument.
- The strict inequality matters: prefixes equal to `0` do **not** count, so threshold ranges must be handled as `> 0`, not `>= 0`.
- For a moved negative at index `j`, the affected prefix range starts at `j`, not `j + 1`; that off-by-one is easy to miss.
- In production systems, the transferable idea is to optimize interventions by modeling their suffix-wide impact once, then selecting the highest-leverage action without replaying the whole system.

## 🚀 Variations & Further Practice
- Allow up to `k` relocations instead of one. The twist is interaction: gains are no longer independent because one move changes the prefix landscape seen by the next.
- Maximize the number of prefixes above an arbitrary threshold `T` rather than `0`. Same machinery applies, but every inequality shifts and edge handling gets easier to get wrong.
- Support online updates and queries: modify one element, then ask for the best one-relocation score. The hard part is maintaining prefix-order statistics dynamically rather than solving offline once.