# Longest Upload Burst Within Data Cap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `uploads`, where each value is the size of a file upload, and an integer `cap`, find the maximum length of any contiguous subarray whose sum is at most `cap`. Return that length; if every individual upload exceeds the cap, return `0`. The challenge is scale: with up to `200,000` uploads, any brute-force scan of all subarrays is too slow, so the solution must exploit structure in contiguous ranges.

## 🌍 Engineering Impact
This pattern shows up anywhere systems enforce budgets over continuous activity: mobile data throttling, API rate windows, stream ingestion quotas, ad-serving spend guards, and storage write burst control. In production, the question is rarely “what is the total,” but “what is the longest uninterrupted interval that stayed within policy.” Without a linear-time windowing approach, analytics pipelines degrade into quadratic scans, making near-real-time enforcement or observability impractical. The sliding-window model enables bounded-memory processing, predictable latency, and direct adaptation to online streams where events arrive incrementally and decisions must be made without replaying history.

## 🔍 Problem Statement
You are given:

- `uploads`: an integer array of length `1 <= uploads.length <= 200000`
- `cap`: an integer with `0 <= cap <= 1000000000`

Each `uploads[i]` is the size in MB of the `i`-th upload, with `0 <= uploads[i] <= 100000`.

A **burst** is any contiguous sequence of uploads. Return the length of the longest burst whose total size is `<= cap`.

Examples:

- `uploads = [4, 2, 1, 7, 3, 2], cap = 8` → `3`
- `uploads = [9, 1, 2, 1, 1], cap = 4` → `3`

Edge cases matter:

- If `cap = 0`, only runs of zero-sized uploads are valid.
- If all uploads are larger than `cap`, return `0`.
- Contiguity is mandatory; sorting or reordering destroys the problem.

The key algorithmic constraint is input size: `O(n^2)` enumeration of all bursts is not viable.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous segment** under a **sum constraint**. That is a strong signal for a sliding window.

2. Notice all `uploads[i]` are non-negative. That matters more than anything else. With non-negative numbers, expanding the window can only increase or preserve the sum, and shrinking it can only decrease or preserve the sum. That monotonic behavior is what makes two pointers work.

3. Start with a window `[left..right]` and track its running sum.

4. Move `right` forward one upload at a time, adding the new value into the sum. This explores every possible end position exactly once.

5. If the sum exceeds `cap`, the current window is invalid. Because values are non-negative, the only way to restore validity is to move `left` forward, subtracting uploads until the sum is back within budget.

6. After revalidating, the window is the longest valid burst ending at `right`, so update the best length.

7. Repeat until `right` reaches the end. No nested rescans are needed; each pointer only moves forward.

That gives a linear-time solution with constant extra space.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window / Two Pointers.**  
   This problem asks for a longest contiguous range under a threshold. Because all values are non-negative, window sums change monotonically as pointers move. That makes sliding window the correct abstraction; prefix sums alone would still need extra search structure.

2. **Initialize state.**  
   Set `left = 0`, `sum = 0`, and `best = 0`.  
   Invariant: before processing each `right`, `sum` represents the total of the current window `uploads[left..right-1]`, and that window is valid.

3. **Expand the window with `right`.**  
   For each index `right`, add `uploads[right]` to `sum`.  
   This considers every possible burst ending at `right`. The window may now violate the cap.

4. **Shrink until valid.**  
   While `sum > cap`, subtract `uploads[left]` and increment `left`.  
   Why this is correct: with non-negative values, removing items from the left is the only way to reduce the sum while preserving contiguity. Once `sum <= cap`, any earlier `left` would still be invalid.

5. **Record the best valid length.**  
   After shrinking, window `uploads[left..right]` is valid. Its length is `right - left + 1`. Update `best = max(best, right - left + 1)`.  
   Invariant maintained: after each iteration, `best` is the maximum valid burst length seen so far.

6. **Finish in one pass.**  
   Each element is added once and removed at most once. That is why the algorithm is `O(n)` rather than `O(n^2)`.

## 📊 Worked Example
Example: `uploads = [4, 2, 1, 7, 3, 2]`, `cap = 8`

| right | uploads[right] | sum after add | action while sum > 8 | left | valid window | best |
|---|---:|---:|---|---:|---|---:|
| 0 | 4 | 4 | none | 0 | `[4]` | 1 |
| 1 | 2 | 6 | none | 0 | `[4,2]` | 2 |
| 2 | 1 | 7 | none | 0 | `[4,2,1]` | 3 |
| 3 | 7 | 14 | remove 4, 2 → sum = 8 | 2 | `[1,7]` | 3 |
| 4 | 3 | 11 | remove 1, 7 → sum = 3 | 4 | `[3]` | 3 |
| 5 | 2 | 5 | none | 4 | `[3,2]` | 3 |

The maximum valid length observed is `3`, from burst `[4,2,1]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each upload enters the window once when `right` advances and leaves the window at most once when `left` advances. There is no backtracking, sorting, or nested enumeration. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally the right asymptotic shape.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, a running sum, and the best length. No extra arrays or maps are required. Space cannot be meaningfully reduced further without losing the ability to track the current window state.

## 💡 Key Takeaways
- If the problem asks for a longest or shortest **contiguous** range under a threshold, consider sliding window before prefix-sum brute force.
- Non-negative values are the critical signal that two pointers will work; they give the window sum monotonic behavior.
- Update `best` only **after** shrinking the window back to validity, not immediately after expanding.
- Be careful with length calculation: for a valid inclusive window, length is `right - left + 1`, not `right - left`.
- In production systems, this pattern matters because monotonic constraints let you replace replay-heavy analytics with single-pass, bounded-memory decision logic.

## 🚀 Variations & Further Practice
- **Shortest burst with sum at least `target`**: same sliding-window pattern, but the objective flips from maximizing length under a cap to minimizing length over a floor.
- **Longest burst with sum `<= cap` when negatives are allowed**: standard sliding window breaks because sums are no longer monotonic; this requires prefix sums plus an ordered structure or more advanced techniques.
- **Count of all valid bursts instead of longest burst**: still linear with non-negative values, but the aggregation changes from tracking a max to summing the number of valid starts for each end position.