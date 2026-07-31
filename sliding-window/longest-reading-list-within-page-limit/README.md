# Longest Reading List Within Page Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given an array `pages`, return the maximum number of consecutive articles whose total page count does not exceed `maxPages`. The output is a single integer: the longest valid contiguous segment length. The challenge is not correctness on small inputs, but doing it efficiently for arrays up to `100000` elements. Because all page counts are non-negative, the window sum changes monotonically as pointers move, which makes a linear-time sliding window possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous span under a resource budget: API request bursts under rate limits, streaming batches under memory ceilings, ad or content sessions under latency budgets, and log ingestion chunks under payload caps. At scale, brute force is not merely slow; it changes architecture by forcing pre-aggregation, sharding, or approximation. The sliding-window formulation enables single-pass processing with constant extra space, which matters in hot paths, online services, and stream processors where throughput, cache behavior, and predictable latency dominate design choices.

## 🔍 Problem Statement
You are given an integer array `pages` where `pages[i]` is the number of pages in the `i`-th article, and an integer `maxPages` representing the maximum total pages a user can read in one session. Return the maximum number of **consecutive** articles whose total page count is less than or equal to `maxPages`.

Constraints:

- `1 <= pages.length <= 100000`
- `0 <= pages[i] <= 10000`
- `0 <= maxPages <= 1000000000`
- All values are non-negative

Examples:

- `pages = [4, 2, 1, 7, 3, 2]`, `maxPages = 8` → `3`
- `pages = [1, 1, 1, 1, 1]`, `maxPages = 3` → `3`

Edge cases matter: `maxPages` may be `0`, articles may have `0` pages, and a single article may already exceed the limit. The key constraint is non-negative values; that property is what makes a shrinking-and-expanding window correct and efficient.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** group, so sorting or reordering is off the table.

2. Notice the objective → maximize window length while keeping `sum(window) <= maxPages`.

3. Ask what happens when we extend a candidate window to the right. Since all `pages[i]` are non-negative, the sum can only stay the same or increase.

4. That monotonic behavior suggests a sliding window:
   - expand right to include more articles,
   - if the sum becomes too large, move left forward until the window is valid again.

5. Why this works: once a window exceeds the limit, keeping its current left boundary can never help after adding more non-negative values. The only repair is to drop items from the left.

6. Track the largest valid window length seen during the scan.

7. This avoids checking every subarray. Instead of restarting work for each position, both pointers only move forward, which is the signal that an `O(n)` solution exists.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and running state**  
   Set `left = 0`, `windowSum = 0`, and `best = 0`.  
   The invariant we want is: after adjustment, the window `[left, right]` is always valid, meaning `windowSum <= maxPages`.

2. **Expand the window with the right pointer**  
   For each `right` from `0` to `pages.length - 1`, add `pages[right]` to `windowSum`.  
   This is the standard **Sliding Window / Two Pointers** pattern: grow the candidate range incrementally instead of recomputing sums.

3. **Restore validity when the budget is exceeded**  
   While `windowSum > maxPages`, subtract `pages[left]` and increment `left`.  
   This step is correct because all values are non-negative. Removing items from the left is the only way to reduce the sum without skipping candidate contiguous windows.

4. **Record the current valid window length**  
   Once the while-loop finishes, `[left, right]` is the longest valid window ending at `right`, because any earlier left boundary would have made the sum exceed the limit.  
   Update `best = max(best, right - left + 1)`.

5. **Continue until the scan completes**  
   Every element enters the window once and leaves at most once.  
   The maintained invariant is: after each iteration, `windowSum` equals the sum of the current window and that window is valid.

This abstraction is the right fit because the problem asks for an extremal contiguous range under an additive constraint with non-negative values. That combination is exactly where sliding windows dominate.

## 📊 Worked Example
Example: `pages = [4, 2, 1, 7, 3, 2]`, `maxPages = 8`

| right | pages[right] | action                         | left | windowSum | window       | best |
|------:|-------------:|--------------------------------|-----:|----------:|--------------|-----:|
| 0     | 4            | expand                         | 0    | 4         | `[4]`        | 1    |
| 1     | 2            | expand                         | 0    | 6         | `[4,2]`      | 2    |
| 2     | 1            | expand                         | 0    | 7         | `[4,2,1]`    | 3    |
| 3     | 7            | expand → shrink until valid    | 3    | 7         | `[7]`        | 3    |
| 4     | 3            | expand → shrink until valid    | 4    | 3         | `[3]`        | 3    |
| 5     | 2            | expand                         | 4    | 5         | `[3,2]`      | 3    |

At `right = 3`, sum becomes `14`, so we remove `4`, then `2`, then `1` until the window is valid again. The maximum valid length observed is `3`, from `[4, 2, 1]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each article is added to the running sum once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing despite the inner `while` loop. At `10^6` elements this remains practical in a single pass; at `10^9`, the bottleneck becomes I/O and memory locality, not algorithmic blow-up.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a running sum, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further without recomputing sums, which would trade constant memory for worse time complexity.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous range** under a **sum/budget constraint** and all values are non-negative, sliding window should be your first candidate.
- A strong recognition signal is monotonicity: expanding the window never decreases the sum, so pointer movement can be one-way and linear.
- Update the answer **after** shrinking to restore validity; recording length before that introduces invalid windows into the result.
- Be careful with `right - left + 1`; this is the most common off-by-one bug when converting pointer positions into window length.
- In production systems, non-negative bounded-resource streams often permit single-pass windowing, which avoids materialization, reduces latency, and simplifies backpressure-aware designs.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: similar surface area, but the optimization target flips and the standard positive-only window logic changes depending on value constraints.
- **Longest subarray with sum exactly `K`**: harder when negatives are allowed; typically shifts from sliding window to prefix sums plus hash maps.
- **Minimum size subarray sum / fixed-budget batching**: same pattern, but the objective becomes minimizing length or counting windows, which changes when and how state is updated.