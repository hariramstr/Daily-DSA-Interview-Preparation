# Longest Pantry Window With At Most K Expired Items

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given a binary array `items`, return the maximum length of a contiguous subarray containing at most `k` expired items (`1`s). Fresh items are `0`s and do not constrain the window. The challenge is not correctness but efficiency: with up to `100000` elements, enumerating all subarrays is too expensive. The problem asks for a linear-time scan that continuously maintains the longest valid window as the right boundary advances.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent span satisfying a bounded-error or bounded-anomaly constraint. Examples include streaming observability pipelines tolerating up to `k` malformed events, fraud systems finding the longest transaction run with limited risk flags, and rate-limiters tracking the largest burst window with at most `k` violations. At scale, brute-force window evaluation collapses under quadratic growth and destroys latency budgets. Sliding window logic enables single-pass processing, predictable memory use, and direct adaptation to online or near-real-time streams where data cannot be repeatedly rescanned.

## 🔍 Problem Statement
You are given an array `items` of length `n` where each value is binary:

- `items[i] = 0` → fresh
- `items[i] = 1` → expired

You are also given an integer `k`. Your goal is to find the maximum length of any contiguous segment whose number of expired items is at most `k`.

Constraints:

- `1 <= items.length <= 100000`
- `items[i]` is either `0` or `1`
- `0 <= k <= items.length`

Examples:

- `items = [0,1,0,0,1,0,0], k = 1` → `4`
- `items = [1,1,0,0,0,1,0], k = 2` → `6`

Edge cases matter:

- `k = 0` means the window may contain no expired items.
- If `k >= total number of 1s`, the whole array is valid.
- The key constraint is input size: checking every subarray is `O(n^2)`, which is unnecessary and too slow.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is immediately a window problem, not a counting or sorting problem.

2. Notice the rule is **at most `k` expired items** → validity depends only on how many `1`s are inside the current window.

3. If we expand a window to the right, the only thing that can make it invalid is adding too many `1`s. That suggests maintaining a running count of expired items.

4. Once the window becomes invalid (`expiredCount > k`), there is no reason to discard everything and restart. We only need to move the left boundary rightward until the window becomes valid again.

5. This gives the classic **sliding window / two pointers** shape:
   - right pointer expands the candidate window
   - left pointer repairs it when constraints are violated

6. While doing this, every valid window is considered exactly once in its maximal repaired form, so we can update the best length during the scan.

7. The result is a single pass, constant extra space, and no nested subarray enumeration.

## 🧩 Algorithm Walkthrough
1. **Initialize pointers and counters.**  
   Set `left = 0`, `expiredCount = 0`, and `maxLen = 0`.  
   Invariant: the active window is `items[left...right]` once `right` starts moving.

2. **Expand the window with `right`.**  
   For each index `right` from `0` to `n - 1`, include `items[right]` in the window. If it is `1`, increment `expiredCount`.  
   Why this works: every contiguous segment can be represented by some pair of boundaries; we enumerate them implicitly by growing the right edge.

3. **Repair invalid windows.**  
   While `expiredCount > k`, move `left` rightward. If `items[left]` was `1`, decrement `expiredCount` before advancing.  
   Invariant maintained: after the loop, the window again contains at most `k` expired items.

4. **Record the best valid window.**  
   Once repaired, compute `right - left + 1` and update `maxLen`.  
   Why this is correct: for the current `right`, the repaired `left` is the smallest valid left boundary reachable by shrinking, so this is the longest valid window ending at `right`.

5. **Return `maxLen`.**  
   The pattern is **Sliding Window / Two Pointers** because the validity condition is monotonic under expansion and shrinkage: adding elements can break the constraint, and removing from the left can restore it without revisiting prior work.

## 📊 Worked Example
Example: `items = [0,1,0,0,1,0,0], k = 1`

| right | items[right] | expiredCount | left after repair | window        | length | maxLen |
|------:|--------------:|-------------:|------------------:|---------------|-------:|-------:|
| 0     | 0             | 0            | 0                 | `[0]`         | 1      | 1      |
| 1     | 1             | 1            | 0                 | `[0,1]`       | 2      | 2      |
| 2     | 0             | 1            | 0                 | `[0,1,0]`     | 3      | 3      |
| 3     | 0             | 1            | 0                 | `[0,1,0,0]`   | 4      | 4      |
| 4     | 1             | 2            | 2                 | `[0,0,1]`     | 3      | 4      |
| 5     | 0             | 1            | 2                 | `[0,0,1,0]`   | 4      | 4      |
| 6     | 0             | 1            | 2                 | `[0,0,1,0,0]` | 5      | 5      |

Final answer: `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed at most twice: once when `right` includes it and once when `left` excludes it. There is no nested rescanning. At `10^6` elements this remains practical in a single pass; at `10^9`, the algorithm is still asymptotically optimal, though throughput and I/O dominate.

### Space Complexity
`O(1)`. The algorithm stores only a few integers: two pointers, the expired-item count, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous region satisfying an **“at most K bad elements”** constraint, think sliding window immediately.
- If validity can be tracked with a small rolling statistic like a count of `1`s, two pointers usually beat any prefix-sum-plus-search alternative.
- The repair condition is `while expiredCount > k`, not `if`, because one left shift may not be enough to restore validity.
- Window length is `right - left + 1`; forgetting the `+1` is the most common off-by-one error here.
- In production systems, bounded-violation windows are a strong fit for online processing because they preserve linear work and constant memory under continuous input.

## 🚀 Variations & Further Practice
- **Max Consecutive Ones III**: same core problem, but framed as flipping at most `k` zeros; the conceptual twist is recognizing equivalence under value inversion.
- **Longest Substring with At Most K Distinct Characters**: the window constraint shifts from a single counter to a frequency map, making state maintenance more complex.
- **Minimum Size Subarray Sum / shortest valid window problems**: same two-pointer mechanics, but optimization flips from maximizing to minimizing, which changes when results are recorded.