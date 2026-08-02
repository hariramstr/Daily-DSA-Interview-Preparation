# Longest Stream Window With Pairwise Bitwise Overlap Budget

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Bit Manipulation, Hashing

---

## 🗂 Problem Overview
Given an array of non-negative integers, treat each value as a bitmask and define a conflict between two elements when their bitwise AND is non-zero. For any contiguous subarray, its cost is the number of conflicting pairs inside it. The task is to return the maximum window length whose total conflict count is at most `k`. The challenge is that inserting one element can create many new conflicts across the current window, so naive pairwise recomputation is too expensive.

## 🌍 Engineering Impact
This pattern shows up in streaming systems where each event carries a feature set, capability mask, shard affinity, or policy tag, and you need the longest recent segment that stays under an interaction budget. Examples include online fraud pipelines, search/ranking feature co-activation windows, observability streams with overlapping labels, and schedulers avoiding resource-contention bursts. At scale, brute-force pair counting collapses under quadratic behavior and unstable latency. The right approach enables bounded-memory online processing, predictable throughput, and incremental maintenance of a global pair metric without replaying the whole window on every update.

## 🔍 Problem Statement
You are given `nums`, an array of length `n`, where `0 <= nums[i] < 2^20`, and an integer `k`. For a contiguous window `nums[l..r]`, define its overlap cost as the number of index pairs `(i, j)` with `l <= i < j <= r` such that `(nums[i] & nums[j]) != 0`. Return the maximum length of any contiguous subarray whose overlap cost is at most `k`.

Constraints:

- `1 <= n <= 2 * 10^5`
- `0 <= nums[i] < 2^20`
- `0 <= k <= n * (n - 1) / 2`
- Duplicates are allowed

Examples:

- `nums = [1, 2, 3, 8, 10], k = 2` → `4`
- `nums = [5, 1, 4, 2, 8, 3], k = 1` → `3`

The key constraint is `n = 2e5`: any solution that recomputes pair conflicts per window or checks all pairs is immediately non-viable.

## 🪜 How to Solve This
1. Read the problem → this is a longest-valid-subarray question, so start with sliding window / two pointers.
2. Ask what makes a window valid → not a sum over elements, but a sum over *pairs*. Adding one number affects many prior elements.
3. Reframe the update → when appending `x`, the new cost increase is exactly the number of existing window elements that share at least one bit with `x`.
4. That suggests maintaining enough state to answer: “how many current values overlap this mask?”
5. Directly tracking only per-bit counts is not enough, because summing counts over set bits double-counts values sharing multiple bits with `x`.
6. Since each number is only 20 bits wide, use inclusion–exclusion over the set bits of `x`: count how many existing masks contain at least one of those bits.
7. Maintain a frequency table over all bit-subsets seen in the current window. Then each add/remove updates all non-empty subsets of that value’s set bits.
8. Expand right, accumulate new conflicts, and while cost exceeds `k`, shrink left and subtract that element’s contribution against the remaining window.

Once you see “dynamic pair count” plus “small bit-width,” the approach becomes mechanical.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers pattern.**  
   Maintain a window `[l..r]` and a running `cost`, the number of conflicting pairs currently inside the window. This is the right abstraction because validity is monotone under shrinking: if a window exceeds `k`, moving `l` right can only remove pairs.

2. **Represent overlap queries with subset frequencies.**  
   For each non-empty bit subset `s`, store `freq[s]`: how many window elements contain all bits in `s`. Because `nums[i] < 2^20`, each value has at most 20 set bits, so enumerating its non-empty subsets is feasible when bit density is moderate and still bounded by `2^20` in the worst case.

3. **Query how many current elements conflict with `x`.**  
   Let the set bits of `x` define subsets `s`. By inclusion–exclusion, the number of window elements sharing at least one bit with `x` is  
   `sum((-1)^(|s|+1) * freq[s])` over all non-empty subsets `s` of `x`’s set bits.  
   This counts each prior element once iff it intersects `x`.

4. **Expand right.**  
   Before inserting `nums[r]`, compute its conflict count against the current window and add that to `cost`. Then update `freq` by incrementing every non-empty subset of `nums[r]`. Invariant: after insertion, `cost` equals the exact pair count for `[l..r]`.

5. **Shrink while invalid.**  
   To remove `nums[l]`, first decrement all its subset frequencies so the structure represents `[l+1..r]`. Then recompute how many remaining elements conflict with that removed value and subtract that from `cost`. This removes exactly the pairs involving the old left endpoint.

6. **Track the best length.**  
   After restoring `cost <= k`, update `ans = max(ans, r - l + 1)`. Since each index enters and leaves once, the window mechanics stay linear in pointer movement; the extra work is subset enumeration per element.

## 📊 Worked Example
Take `nums = [1, 2, 3, 8, 10]`, `k = 2`.

| Step | `r` | Value | New conflicts added | `cost` after add | Action | Window |
|---|---:|---:|---:|---:|---|---|
| 1 | 0 | 1 (`0001`) | 0 | 0 | keep | `[1]` |
| 2 | 1 | 2 (`0010`) | 0 | 0 | keep | `[1,2]` |
| 3 | 2 | 3 (`0011`) | 2 (`1&3`, `2&3`) | 2 | keep | `[1,2,3]` |
| 4 | 3 | 8 (`1000`) | 0 | 2 | keep | `[1,2,3,8]` |
| 5 | 4 | 10 (`1010`) | 3 (`2&10`, `3&10`, `8&10`) | 5 | shrink | invalid |

Now remove from the left:

- Remove `1`: it conflicts only with `3`, so `cost = 4`
- Remove `2`: it conflicts with `3` and `10`, so `cost = 2`

Window becomes `[3,8,10]`, valid again, length `3`. Best length seen was `4`.

## ⏱ Complexity Analysis
### Time Complexity
Let `b_i` be the number of set bits in `nums[i]`. Each add/remove enumerates all non-empty subsets of that value, so total time is `O(sum 2^{b_i})`. With the given 20-bit cap, this is bounded by `O(n · 2^20)` worst-case, but in practice behaves like `O(n · 2^b)` for small effective bit density. At `10^6` elements, this is only viable if masks are sparse; at `10^9`, you need stronger domain constraints or approximation.

### Space Complexity
`O(2^20)` for the subset-frequency table, plus `O(1)` window metadata. The table dominates memory and is fixed by bit-width, not by `n`. You can reduce space only by switching to sparse hash storage, trading lower memory for higher constant factors and less predictable latency.

## 💡 Key Takeaways
- If the question asks for the longest contiguous region under a budget and validity can be restored by only moving the left edge, think sliding window immediately.
- If adding one element changes a metric against *all prior elements*, look for an incremental way to count its marginal contribution instead of recomputing the whole window.
- Per-bit frequency alone is incorrect here; it double-counts prior values that share multiple bits with the inserted mask.
- On removal, update the frequency structure before subtracting that element’s contribution, or you will count a self-interaction that does not exist.
- Small fixed-width state spaces often justify precomputed or direct-index counting structures that would be impossible if the domain scaled with input size.

## 🚀 Variations & Further Practice
- Replace the condition `(a & b) != 0` with “shared bit count is at least `t`.” The twist is that inclusion–exclusion no longer directly gives the marginal count; you need richer subset statistics.
- Ask for the number of valid subarrays instead of the maximum length. Same window core, but now every valid right endpoint contributes `r - l + 1`.
- Move from contiguous windows to arbitrary subsequences or offline range queries. The monotone two-pointer property disappears, pushing you toward Mo’s algorithm, divide-and-conquer, or bitset-based offline processing.