# Longest Checkout Line With Limited Coupon Types

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `coupons` and an integer `k`, find the maximum length of a contiguous subarray containing at most `k` distinct coupon types. Repetition is allowed; only the number of unique values matters. Return `0` when `k = 0`. The challenge is scale: with up to `200,000` customers, enumerating all subarrays is too expensive, so the solution must maintain validity incrementally while scanning once.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maintain the longest recent segment under a bounded diversity constraint. Examples include streaming pipelines tracking windows with limited event categories, rate-limiters grouping requests by a small set of principals, observability systems finding stretches with bounded label cardinality, and search or recommendation pipelines constraining feature variety in a candidate band. At scale, brute-force window validation collapses under quadratic behavior and repeated recounting. The sliding-window approach turns a potentially explosive scan into a linear pass with bounded mutable state, which is exactly what production streaming and online analytics systems need.

## 🔍 Problem Statement
You are given an integer array `coupons` where `coupons[i]` is the coupon type used by the `i`-th customer in line, and an integer `k`. Return the length of the longest contiguous block of customers containing at most `k` distinct coupon types.

A block is valid if the number of unique coupon values inside it is `<= k`. Duplicate coupon types may appear any number of times. If `k = 0`, no customer can be included, so the result is `0`.

**Constraints**
- `1 <= coupons.length <= 200000`
- `1 <= coupons[i] <= 1000000000`
- `0 <= k <= coupons.length`

**Examples**
- `coupons = [4, 2, 2, 5, 5, 2, 4, 4], k = 2` → `5`
- `coupons = [1, 3, 1, 3, 2, 2, 2, 4], k = 3` → `7`

The decisive constraint is input size: any `O(n^2)` subarray enumeration will time out.

## 🪜 How to Solve This
1. Read the problem → the word **contiguous** immediately rules out sorting or arbitrary regrouping. Order matters.
2. The condition is **at most `k` distinct values** → this is a classic “window stays valid until diversity exceeds a limit” signal.
3. If we expand a right boundary one customer at a time, we only need to know how many times each coupon type appears in the current window.
4. That suggests a `HashMap<couponType, count>` plus two pointers: `left` and `right`.
5. Move `right` forward, add the new coupon, and track distinct types via the map size.
6. If the window becomes invalid (`distinct > k`), move `left` forward until validity is restored, decrementing counts and removing entries when a count hits zero.
7. After each expansion/shrink cycle, the window is the longest valid one ending at `right`, so update the best length.
8. This works because both pointers only move forward. No element is added or removed more than once, which gives linear time.

## 🧩 Algorithm Walkthrough
1. **Handle the trivial edge case.**  
   If `k == 0`, return `0` immediately. No non-empty window can satisfy the constraint.

2. **Initialize the sliding window.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`, and iterate `right` from `0` to `n - 1`. Maintain a hash map from coupon type to frequency within the current window `[left, right]`.

3. **Expand the window to the right.**  
   For each `coupons[right]`, increment its count in the map. This updates the current window state in `O(1)` average time.

4. **Restore validity when distinct types exceed `k`.**  
   While `map.size() > k`, shrink from the left: decrement `coupons[left]`, remove it from the map if its count becomes zero, then increment `left`.  
   **Invariant:** after this loop, the window `[left, right]` contains at most `k` distinct coupon types.

5. **Record the best valid window.**  
   Once valid, compute `right - left + 1` and update the maximum.  
   This is correct because for a fixed `right`, any window starting left of the current `left` would be invalid, and any starting right of it would be shorter.

6. **Why this abstraction fits.**  
   The problem asks for the longest contiguous region under a monotonic validity rule: adding elements may break validity, and removing from the left can restore it. That is exactly where sliding window dominates brute-force approaches.

## 📊 Worked Example
Example: `coupons = [4, 2, 2, 5, 5, 2, 4, 4]`, `k = 2`

| right | coupon | action | left | freq map | valid? | best |
|---|---:|---|---:|---|---|---:|
| 0 | 4 | add 4 | 0 | {4:1} | yes | 1 |
| 1 | 2 | add 2 | 0 | {4:1, 2:1} | yes | 2 |
| 2 | 2 | add 2 | 0 | {4:1, 2:2} | yes | 3 |
| 3 | 5 | add 5, shrink | 1 | {2:2, 5:1} | yes | 3 |
| 4 | 5 | add 5 | 1 | {2:2, 5:2} | yes | 4 |
| 5 | 2 | add 2 | 1 | {2:3, 5:2} | yes | 5 |
| 6 | 4 | add 4, shrink | 4 | {5:2, 2:1, 4:1} → {5:2, 2:1, 4:1} → {5:2, 2:1, 4:1} → {5:2, 2:1, 4:1} → {5:2, 2:1, 4:1} | no→yes | 5 |
| 7 | 4 | add 4 | 4 | {5:2, 2:1, 4:2} then shrink to {2:1, 4:2} | yes | 5 |

Longest valid block length is `5`: `[2, 2, 5, 5, 2]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time. Each customer enters the window once when `right` advances and leaves at most once when `left` advances. Hash map updates are `O(1)` average, so the dominant cost is a single linear scan. This remains practical at `10^6` elements; at `10^9`, even linear time becomes throughput-bound and requires streaming or distributed partitioning.

### Space Complexity
`O(min(n, k))` auxiliary space, dominated by the frequency map for distinct coupon types currently inside the window. In practice it is `O(k)` once the window is valid. Reducing this further is not realistic without sacrificing constant-time updates or exactness.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under an **“at most K distinct”** constraint, think sliding window immediately.
- When validity depends on counts of values inside a moving range, a hash map plus two pointers is usually the right state model.
- Do not update the answer before shrinking an invalid window; the max must be computed only after `distinct <= k` is restored.
- Remove keys when their frequency reaches zero; forgetting this leaves `map.size()` incorrect and breaks the shrink condition.
- The transferable design insight: maintain incremental state over a moving boundary instead of recomputing global properties for every candidate range.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters** — same pattern, but string-oriented and often used to test whether the abstraction transfers cleanly across domains.
- **Fruit Into Baskets / longest subarray with at most 2 distinct values** — same core idea with a fixed small `k`, which sharpens reasoning about invariants and minimal state.
- **Minimum window substring** — still sliding window, but the objective flips from maximizing a valid window to minimizing one that satisfies a coverage constraint, making correctness subtler.