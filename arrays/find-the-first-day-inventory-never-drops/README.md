# Find the First Day Inventory Never Drops

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Simulation

---

## 🗂 Problem Overview
Given an array `stock`, return the smallest index `i` such that the suffix `stock[i...n-1]` is non-decreasing. Equivalently, from day `i` onward, inventory never drops compared with the previous day. If the entire array is already non-decreasing, return `0`.

The challenge is not correctness but efficiency: with up to `100000` elements, checking every suffix independently would degrade to quadratic time. The goal is a linear scan that identifies the earliest valid starting point.

## 🌍 Engineering Impact
This pattern shows up anywhere teams need to identify the earliest point after which a metric remains monotonic: backlog recovery in job queues, error-rate stabilization in observability pipelines, inventory reconciliation in retail systems, and watermark progression in streaming platforms. At scale, the difference between rescanning all suffixes and maintaining a single pass is the difference between interactive dashboards and batch-only diagnostics.

Architecturally, this is a suffix-validity problem. Recognizing that shape lets you replace repeated validation with one backward pass, which reduces CPU, improves cache behavior, and keeps the implementation predictable under large input sizes and high-frequency recomputation.

## 🔍 Problem Statement
You are given an integer array `stock` where `stock[i]` is the inventory level at the end of day `i`. A day is stable if its inventory is greater than or equal to the previous day’s inventory. Return the smallest index `i` such that for every `j` with `i < j < n`, `stock[j] >= stock[j - 1]`. In other words, the suffix starting at `i` must be non-decreasing.

Constraints:

- `1 <= stock.length <= 100000`
- `-1000000000 <= stock[i] <= 1000000000`
- A valid answer always exists in `[0, n - 1]`

Examples:

- `stock = [9, 7, 8, 8, 10]` → `1`
- `stock = [5, 6, 4, 7, 9]` → `2`

Edge cases matter: a single-element array returns `0`, equal adjacent values are allowed, and the linear-size constraint rules out checking each starting position independently.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need the longest non-decreasing subarray or the number of drops. We need the **earliest index whose entire suffix is valid**.

2. Reframe the condition → index `i` works if every adjacent pair from `i` onward satisfies `stock[j] >= stock[j - 1]`. That means one bad drop anywhere to the right invalidates all earlier starts before that drop.

3. This suggests scanning from right to left → when moving backward, we can ask: “Is the suffix starting at `i + 1` already non-decreasing?” If yes, then `i` is valid exactly when `stock[i] <= stock[i + 1]`.

4. Maintain the earliest valid suffix start seen so far. Initialize it to the last index, since a one-element suffix is always non-decreasing.

5. Walk backward once. Whenever `stock[i] <= stock[i + 1]`, extend the valid suffix leftward by setting the answer to `i`. If not, stop extending; earlier indices cannot bypass this drop unless they start after it.

This is a greedy suffix-extension scan: once a suffix is known valid, only one adjacent comparison is needed to decide whether it can grow left.

## 🧩 Algorithm Walkthrough
1. **Initialize the suffix boundary**  
   Set `answer = n - 1`. The suffix containing only the last element is trivially non-decreasing.  
   **Invariant:** `stock[answer...n-1]` is non-decreasing.

2. **Scan from right to left**  
   For each index `i` from `n - 2` down to `0`, compare `stock[i]` with `stock[i + 1]`.  
   This is a **Greedy + Reverse Scan** pattern: we greedily extend the known-good suffix as far left as possible.

3. **Extend when the local pair is valid**  
   If `stock[i] <= stock[i + 1]`, then prepending `stock[i]` to an already non-decreasing suffix preserves non-decreasing order. Update `answer = i`.  
   **Why correct:** the only new constraint introduced by adding `i` is the pair `(i, i + 1)`, and that pair now satisfies the rule.

4. **Reset on a drop**  
   If `stock[i] > stock[i + 1]`, then the suffix starting at `i` is invalid. Do not update `answer`.  
   **Why this matters:** this drop is a hard boundary. Any valid answer must be strictly after `i`.

5. **Return the leftmost successful extension**  
   After the scan, `answer` is the smallest index whose suffix is non-decreasing.  
   **Invariant preserved throughout:** after processing each `i`, `answer` marks the earliest start of a valid suffix among positions already examined.

This abstraction is right because the property is suffix-based and local: once the tail is valid, extending it requires checking exactly one adjacent relation.

## 📊 Worked Example
Take `stock = [9, 7, 8, 8, 10]`.

| i | stock[i] | stock[i+1] | `stock[i] <= stock[i+1]` | answer |
|---|----------|------------|---------------------------|--------|
| - | -        | -          | initialize                | 4      |
| 3 | 8        | 10         | yes                       | 3      |
| 2 | 8        | 8          | yes                       | 2      |
| 1 | 7        | 8          | yes                       | 1      |
| 0 | 9        | 7          | no                        | 1      |

Trace:

1. Start with `answer = 4`; last element alone is valid.
2. At `i = 3`, `8 <= 10`, so suffix `[8, 10]` is valid.
3. At `i = 2`, `8 <= 8`, so suffix `[8, 8, 10]` is valid.
4. At `i = 1`, `7 <= 8`, so suffix `[7, 8, 8, 10]` is valid.
5. At `i = 0`, `9 > 7`, so the suffix cannot extend further left.

Return `1`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because it performs a single backward pass and does one constant-time comparison per index. For `10^6` elements, this is still operationally cheap and cache-friendly. At `10^9`, linear work is still large, but it remains the best possible asymptotic bound for reading the full input.

### Space Complexity
The algorithm uses `O(1)` extra space. No auxiliary arrays, stacks, or maps are needed; the only additional state is the answer index and loop variables. Space cannot be meaningfully reduced further without changing the input representation itself.

## 💡 Key Takeaways
- If the problem asks for the earliest index whose **suffix** satisfies a monotonic property, think reverse scan before considering nested checks.
- When validity of a larger range depends only on a known-valid suffix plus one new boundary condition, a greedy extension is usually enough.
- The comparison must allow equality: use `<=`, not `<`, because non-decreasing permits repeated values.
- Initialize the answer to `n - 1`, not `0`; the last element is the only suffix guaranteed valid without inspection.
- In production code, suffix-validity problems often collapse from repeated recomputation to one pass once you model the invariant explicitly.

## 🚀 Variations & Further Practice
- Return the length of the longest non-decreasing suffix instead of its starting index; same scan, different output shape.
- Find all indices whose suffix is non-decreasing after allowing at most one violation; the twist is tracking limited error budget, not pure monotonicity.
- Generalize to streaming updates where `stock[i]` changes online; the harder part is maintaining suffix monotonicity incrementally rather than recomputing from scratch.