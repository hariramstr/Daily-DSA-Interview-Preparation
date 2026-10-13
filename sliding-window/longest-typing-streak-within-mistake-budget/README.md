# Longest Typing Streak Within Mistake Budget

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given a binary array `mistakes` and an integer `k`, find the maximum length of a contiguous subarray containing at most `k` ones. Here, `1` represents a mistaken word and `0` a correct one. The output is a single integer: the longest acceptable streak length. The problem becomes non-trivial because `mistakes.length` can reach `200000`, which rules out brute-force enumeration of all subarrays.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest continuous segment under a bounded error budget. Examples include observability pipelines finding the longest interval with at most `k` failed probes, video streaming systems measuring the longest playback span with limited dropped frames, and search or recommendation systems tolerating a bounded number of low-quality results in a session window. At scale, brute force collapses under quadratic scans and excessive cache-unfriendly work. A sliding-window formulation enables single-pass processing, predictable latency, and easy adaptation to streaming or online evaluation where data arrives incrementally.

## 🔍 Problem Statement
You are given a binary array `mistakes` where `mistakes[i]` is either `0` or `1`, and an integer `k`. A contiguous subarray is considered acceptable if it contains at most `k` values equal to `1`. Return the maximum possible length of such a subarray.

Constraints:

- `1 <= mistakes.length <= 200000`
- `mistakes[i]` is either `0` or `1`
- `0 <= k <= mistakes.length`

Examples:

- `mistakes = [0,1,0,0,1,0,0,0], k = 1` → `5`
- `mistakes = [1,1,0,0,0,1,0,0,1,0], k = 2` → `8`

Edge cases matter: `k = 0` means the answer is the longest all-zero run; if `k` is at least the total number of ones, the whole array is valid. The key algorithmic constraint is array size: checking every subarray is too slow, so the solution must be linear.

## 🪜 How to Solve This
1. Read the requirement carefully → we need the **longest contiguous block** satisfying a constraint, not all blocks and not a reordered answer.
2. The constraint is local to a window: “number of `1`s inside this range must be `<= k`.” That is a classic signal for **sliding window / two pointers**.
3. Start with a window `[left, right]` and expand `right` one step at a time. Track how many mistakes (`1`s) are currently inside.
4. If the window is still valid (`ones <= k`), great — update the best length.
5. If it becomes invalid (`ones > k`), don’t restart. Shrink from the left until the window is valid again.
6. Why this works: for each `right`, the smallest valid `left` gives the longest valid window ending at `right`. Any earlier `left` is invalid; any later `left` is shorter.
7. Because each pointer only moves forward, total work is linear. That is the key leap from brute force to scalable.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and counters.**  
   Set `left = 0`, `ones = 0`, and `best = 0`. The window is the inclusive range `[left, right]`. This is the standard **Two Pointers / Sliding Window** pattern because validity depends on aggregate state inside a contiguous range.

2. **Expand the right boundary.**  
   Iterate `right` from `0` to `n - 1`. If `mistakes[right] == 1`, increment `ones`.  
   Invariant after this step: the window reflects exactly the elements between `left` and `right`.

3. **Restore validity when the budget is exceeded.**  
   While `ones > k`, move `left` rightward. If `mistakes[left] == 1`, decrement `ones` before incrementing `left`.  
   Why correct: any window with the current `right` and an earlier `left` is invalid, so shrinking is necessary, not optional.

4. **Update the answer once valid.**  
   After the shrink loop, the window satisfies `ones <= k`. Compute `right - left + 1` and update `best` if larger.  
   Invariant: after each iteration, `best` is the maximum valid window length seen so far.

5. **Why the abstraction fits.**  
   The problem has a monotonic property: expanding can only increase or preserve the count of ones, and shrinking can only decrease or preserve it. That monotonicity is exactly what makes sliding window optimal here.

6. **Termination and result.**  
   When `right` reaches the end, `best` is the longest contiguous acceptable streak. No candidate window is missed because every maximal valid window ending at each `right` is considered.

## 📊 Worked Example
Example: `mistakes = [0,1,0,0,1,0,0,0], k = 1`

| right | mistakes[right] | ones | left after shrink | valid window      | length | best |
|------:|-----------------:|-----:|------------------:|-------------------|-------:|-----:|
| 0     | 0                | 0    | 0                 | `[0]`             | 1      | 1    |
| 1     | 1                | 1    | 0                 | `[0,1]`           | 2      | 2    |
| 2     | 0                | 1    | 0                 | `[0,1,0]`         | 3      | 3    |
| 3     | 0                | 1    | 0                 | `[0,1,0,0]`       | 4      | 4    |
| 4     | 1                | 2    | 2                 | `[0,0,1]`         | 3      | 4    |
| 5     | 0                | 1    | 2                 | `[0,0,1,0]`       | 4      | 4    |
| 6     | 0                | 1    | 2                 | `[0,0,1,0,0]`     | 5      | 5    |
| 7     | 0                | 1    | 2                 | `[0,0,1,0,0,0]`   | 6      | 6    |

The longest valid streak is length `6`, from index `2` to `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = mistakes.length`. Each element is processed at most twice: once when `right` includes it, and once when `left` excludes it. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, linear time is still expensive but remains the only viable asymptotic shape for a single-machine scan.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a count of ones, and the best length. No auxiliary array or hash structure is required. Space cannot be meaningfully reduced further without losing the ability to track the current window state.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a bounded-count constraint, think sliding window immediately.
- Binary arrays with conditions like “at most `k` bad items” are especially strong signals for a two-pointer solution.
- Be careful to shrink **while** `ones > k`, not just once; a single left move may not restore validity.
- Window length is `right - left + 1`; forgetting the `+1` is the most common off-by-one bug here.
- The production-level insight is to exploit monotonic constraints so range validation becomes incremental instead of recomputed from scratch.

## 🚀 Variations & Further Practice
- **Max Consecutive Ones III**: same core problem, framed as flipping at most `k` zeros; the conceptual twist is recognizing it is structurally identical after remapping.
- **Longest Repeating Character Replacement**: sliding window with a replacement budget, but validity depends on the most frequent character in the window rather than a simple binary count.
- **Minimum Size Subarray Sum / shortest valid window variants**: same two-pointer mechanics, but optimization direction flips from maximizing to minimizing, which changes when and how results are updated.