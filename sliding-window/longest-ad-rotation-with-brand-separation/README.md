# Longest Ad Rotation With Brand Separation

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `brands` and an integer `gap`, find the maximum length of a contiguous segment where repeated occurrences of the same brand are sufficiently spaced apart. For any brand appearing multiple times inside the segment, consecutive appearances must have at least `gap` other ads between them. The output is a single integer: the longest valid segment length. The challenge is scale: with up to `200000` ads, any quadratic scan over candidate subarrays is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere a stream must respect per-key cooldowns while maximizing throughput or measuring compliant windows. Ad-serving and recommendation systems use it to enforce brand or creator spacing constraints. Distributed rate-limiters apply the same idea to repeated requests from the same tenant or token. Streaming observability pipelines use similar logic to detect longest compliant spans under deduplication or suppression rules. Without an incremental windowing approach, systems fall back to repeated rescans, which collapse under high-cardinality streams. The sliding-window plus last-seen-index pattern turns a potentially quadratic validation problem into a single-pass decision process.

## 🔍 Problem Statement
You are given an integer array `brands`, where `brands[i]` is the brand ID of the `i`-th ad in chronological order, and an integer `gap`. A contiguous segment is valid if, for every brand appearing in that segment, any two consecutive occurrences of that brand are at least `gap + 1` positions apart. Equivalently, repeated appearances of the same brand must have at least `gap` other ads between them.

Return the length of the longest valid contiguous segment.

Constraints:

- `1 <= brands.length <= 200000`
- `1 <= brands[i] <= 1000000000`
- `0 <= gap <= brands.length`

Edge cases:

- A segment of length `1` is always valid.
- If `gap = 0`, every segment is valid.

Examples:

- `brands = [4, 1, 2, 4, 3, 1, 5], gap = 2` → `7`
- `brands = [7, 2, 7, 3, 4, 7], gap = 2` → `4`

The key constraint is input size: this forces an `O(n)` or near-linear solution.

## 🪜 How to Solve This
1. Read the rule carefully → validity depends only on distances between repeated occurrences of the **same** brand inside a contiguous window.
2. That immediately suggests we do **not** need to compare every pair of positions. For each brand, only its **most recent occurrence** matters when we extend the window.
3. Think in terms of a growing window `[left, right]`. When we add `brands[right]`, either it fits the cooldown rule or it violates it because the same brand appeared too recently.
4. If it violates, the fix is deterministic: move `left` just past that previous conflicting occurrence. Anything earlier cannot repair the violation.
5. To do that in constant time, store `lastSeen[brand] = most recent index`.
6. As `right` advances once, `left` only moves forward, never backward. That gives the standard Two Pointers / Sliding Window shape.
7. After each step, the window is the longest valid segment ending at `right`, so update the best length.
8. This avoids rescanning subarrays and reduces the problem to one pass plus hash lookups.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` that is always valid after processing each `right`. This is the right abstraction because the problem asks for the longest **contiguous** segment under a local repeat constraint.

2. **Track the last index of each brand with a hash map.**  
   Let `lastSeen[brand]` store the most recent position where that brand appeared. This is sufficient because only the nearest previous occurrence can violate the spacing rule for the current `right`.

3. **Expand the window one element at a time.**  
   For each `right`, read `brand = brands[right]`. If the brand has not been seen before, no conflict exists; the current window remains valid.

4. **Detect a violation using index distance.**  
   If `brand` was last seen at `prev`, then the number of ads between `prev` and `right` is `right - prev - 1`. A violation occurs when `right - prev - 1 < gap`, equivalently `right - prev <= gap`.

5. **Repair by moving `left` forward.**  
   Set `left = max(left, prev + 1)`. This removes the conflicting earlier occurrence from the window. Using `max` is critical because `left` must never move backward.

6. **Update state and answer.**  
   Record `lastSeen[brand] = right`, then compute window length `right - left + 1` and update the maximum.

7. **Invariant maintained:**  
   After each iteration, `[left, right]` is valid, and `left` is the smallest index that makes it valid for this `right`. That guarantees correctness and ensures each pointer advances at most `n` times overall.

## 📊 Worked Example
Example: `brands = [7, 2, 7, 3, 4, 7]`, `gap = 2`

A repeat is invalid when `right - prev <= 2`.

| right | brand | prev | action on left | window `[left,right]` | length | best |
|---|---:|---:|---|---|---:|---:|
| 0 | 7 | — | none | `[0,0]` | 1 | 1 |
| 1 | 2 | — | none | `[0,1]` | 2 | 2 |
| 2 | 7 | 0 | `2 - 0 <= 2`, so `left = 1` | `[1,2]` | 2 | 2 |
| 3 | 3 | — | none | `[1,3]` | 3 | 3 |
| 4 | 4 | — | none | `[1,4]` | 4 | 4 |
| 5 | 7 | 2 | `5 - 2 > 2`, valid | `[1,5]` | 5 | 5 |

The longest valid segment is indices `1..5`: `[2, 7, 3, 4, 7]`, length `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each index is processed once by the `right` pointer, and `left` only moves forward. Hash map reads and writes are `O(1)` on average, so the dominant cost is a single linear scan. At `10^6` elements this is routine; at `10^9`, the bottleneck becomes memory bandwidth and storage, not algorithmic shape.

### Space Complexity
`O(k)` where `k` is the number of distinct brands in the input, owned by the `lastSeen` hash map. In the worst case, `k = n`. Space cannot be meaningfully reduced without sacrificing constant-time conflict checks and falling back to rescans.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** region under a per-key validity rule, start with Sliding Window / Two Pointers.
- If validity for a new element depends only on its most recent matching occurrence, a hash map of `lastSeen` indices is usually enough.
- The violation test is `right - prev <= gap`, not `right - prev - 1 <= gap`; mixing these forms is a common off-by-one source.
- When repairing the window, use `left = max(left, prev + 1)`; assigning `left = prev + 1` directly can move the window backward and corrupt correctness.
- In production stream processing, this pattern is valuable because it converts repeated compliance checks from retrospective scans into incremental state updates.

## 🚀 Variations & Further Practice
- Allow up to `k` spacing violations inside a window. The twist is that validity is no longer binary per insertion; you need to count conflicts and shrink until the violation budget is restored.
- Replace a global `gap` with a per-brand cooldown map. The core pattern survives, but the violation threshold becomes key-dependent and state handling gets less uniform.
- Ask for the number of valid subarrays instead of the longest one. The twist is turning a max-window problem into a counting problem while preserving linear-time window movement.