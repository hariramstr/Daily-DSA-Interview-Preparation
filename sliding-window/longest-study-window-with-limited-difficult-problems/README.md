# Longest Study Window With Limited Difficult Problems

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding-window, two-pointers, array

---

## 🗂 Problem Overview
Given an array of problem difficulties, find the maximum length of a contiguous segment containing at most `k` difficult problems, where “difficult” means `problems[i] >= threshold`. The output is a single integer: the longest valid window length. The challenge is scale: with up to `200,000` elements, checking every possible subarray is too slow, so the solution must exploit structure in contiguous ranges and update state incrementally.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest continuous interval satisfying a bounded-violation rule. Examples include streaming pipelines tolerating up to `k` malformed events, SRE alert windows allowing limited error spikes, search/ranking sessions with capped low-quality results, and rate-limiters tracking bursts with bounded exceptions. At scale, brute-force interval scans collapse under quadratic behavior and destroy latency budgets. A sliding-window design turns the problem into a single pass with constant auxiliary state, which is exactly the difference between something usable in hot paths and something that only works in offline analysis.

## 🔍 Problem Statement
You are given an integer array `problems`, plus integers `threshold` and `k`. A problem is classified as difficult if its difficulty is greater than or equal to `threshold`. Your task is to return the length of the longest contiguous subarray containing at most `k` difficult elements.

Constraints:

- `1 <= problems.length <= 200000`
- `0 <= problems[i] <= 1000000000`
- `0 <= k <= problems.length`
- `0 <= threshold <= 1000000000`

Examples:

- `problems = [2, 7, 3, 9, 4, 8, 1], threshold = 7, k = 2` → `5`
- `problems = [10, 1, 1, 10, 1, 10, 1, 1], threshold = 10, k = 1` → `4`

Edge cases matter: `k = 0` means no difficult problems are allowed, and `threshold = 0` means every element is difficult. The decisive constraint is array length: `O(n^2)` subarray enumeration will not pass.

## 🪜 How to Solve This
1. Read the problem → the word **contiguous** is the signal. That usually rules out sorting or arbitrary selection and points toward a window over the original order.

2. The validity rule is simple: a window is valid if the count of values `>= threshold` is at most `k`. That means we do not need the full contents of the window, only one aggregate: `difficultCount`.

3. Start expanding a right pointer across the array. Each new element either increases `difficultCount` by one or leaves it unchanged.

4. If the window becomes invalid (`difficultCount > k`), do not restart. Instead, shrink from the left until the window is valid again. This preserves contiguity and avoids reprocessing subarrays.

5. At every step where the window is valid, update the best length. The key realization is monotonic movement: both pointers only move forward, so each element enters and leaves the window at most once.

6. That gives a linear-time sliding window: exactly the right fit when the constraint is “longest contiguous segment with at most K bad items.”

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` over the array. This is the right abstraction because validity depends on a contiguous range and a count that can be updated incrementally.

2. **Expand the window by moving `right` one step at a time.**  
   For each `problems[right]`, check whether it is difficult (`>= threshold`). If so, increment `difficultCount`.  
   **Invariant:** before any shrinking, the window reflects exactly the elements from `left` to `right`.

3. **Restore validity when the constraint is violated.**  
   While `difficultCount > k`, move `left` forward. If the element leaving the window was difficult, decrement `difficultCount`.  
   **Why correct:** shrinking is necessary because any window containing more than `k` difficult elements is invalid. Moving `left` is the only way to remove elements while preserving contiguity.  
   **Invariant:** after the loop, the window is valid and contains at most `k` difficult problems.

4. **Record the best valid length.**  
   Once valid, compute `right - left + 1` and update the maximum.  
   **Why correct:** for each `right`, the maintained `left` is the leftmost position after restoring validity, so this is the longest valid window ending at `right`.

5. **Finish after one pass.**  
   Since `left` and `right` each move at most `n` times, total work is linear. No nested rescans occur, which is exactly why this scales to the upper constraint bound.

## 📊 Worked Example
Example: `problems = [2, 7, 3, 9, 4, 8, 1]`, `threshold = 7`, `k = 2`

| right | value | difficult? | left after shrink | difficultCount | window length | best |
|------:|------:|:----------:|------------------:|---------------:|--------------:|-----:|
| 0 | 2 | No  | 0 | 0 | 1 | 1 |
| 1 | 7 | Yes | 0 | 1 | 2 | 2 |
| 2 | 3 | No  | 0 | 1 | 3 | 3 |
| 3 | 9 | Yes | 0 | 2 | 4 | 4 |
| 4 | 4 | No  | 0 | 2 | 5 | 5 |
| 5 | 8 | Yes | 2 | 2 | 4 | 5 |
| 6 | 1 | No  | 2 | 2 | 5 | 5 |

At `right = 5`, the window temporarily has three difficult problems: `7, 9, 8`. Shrink from the left: remove `2` first, still invalid; remove `7`, and `difficultCount` drops back to `2`. The valid window becomes `[3, 9, 4, 8]`. Final answer: `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed once when `right` expands the window and at most once again when `left` shrinks it. There is no quadratic subarray enumeration. At `10^6` elements this is still practical; at `10^9`, linear scan cost dominates and distribution/external-memory concerns become the real bottleneck.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a difficult-element counter, and the best length seen so far. No auxiliary array or map is required. Space cannot be meaningfully reduced further without changing the execution model; the trade-off is already optimal for in-memory streaming.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** segment under a bounded-count constraint, think sliding window before considering heavier structures.
- When validity depends on a window-local aggregate like “at most `k` items matching a predicate,” two pointers usually convert brute-force subarray search into a single pass.
- Be precise about the predicate: difficult means `>= threshold`, not `> threshold`; that boundary changes correctness on equal values.
- Update the answer only after shrinking back to a valid window, and compute length as `right - left + 1` to avoid classic off-by-one errors.
- In production systems, this pattern matters because bounded-violation interval detection is often a streaming problem, and constant-state linear scans are what keep hot-path analytics feasible.

## 🚀 Variations & Further Practice
- **Longest window with at most `k` distinct values** — same sliding-window skeleton, but validity depends on a frequency map rather than a single counter.
- **Longest subarray after flipping at most `k` zeros** — equivalent binary formulation; the twist is recognizing that “bad items” can be modeled as a predicate over values.
- **Shortest subarray with sum at least `target`** — still window-based, but the optimization direction flips and correctness depends on monotonicity of positive sums.