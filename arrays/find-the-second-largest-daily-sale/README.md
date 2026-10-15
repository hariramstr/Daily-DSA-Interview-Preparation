# Find the Second Largest Daily Sale

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, One Pass, Simulation

---

## 🗂 Problem Overview
Given an integer array `sales`, return the second largest **distinct** value. Distinct matters: repeated occurrences of the maximum do not qualify as second largest. If the array contains fewer than two unique values, return `-1`. The key constraint is not correctness but efficiency: with up to `10^5` elements and an explicit `O(n)` / `O(1)` target, sorting or auxiliary sets are avoidable overhead. The intended solution is a single linear scan maintaining the top two distinct values seen so far.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need top-k summaries from streams without materializing or sorting the full dataset: ad-tech bid ranking, search result scoring, observability pipelines tracking highest latency buckets, fraud systems keeping top transaction amounts, or real-time sales dashboards. At scale, sorting every batch or storing all distinct values burns memory bandwidth and increases tail latency. A one-pass, constant-space approach enables streaming execution, predictable resource usage, and easy composition into larger pipelines. It also generalizes cleanly to bounded top-k tracking, which is a common primitive in ranking and telemetry systems.

## 🔍 Problem Statement
You are given an integer array `sales` where `sales[i]` is the total sales for day `i`. Return the second largest **distinct** value in the array. If no such value exists, return `-1`.

Constraints:

- `1 <= sales.length <= 10^5`
- `-10^9 <= sales[i] <= 10^9`
- Required time complexity: `O(n)`
- Required extra space: `O(1)`

Important edge cases:

- Arrays of length `1` must return `-1`
- Duplicate maximum values do not count toward second largest
- Negative numbers are valid and must be handled correctly
- Arrays with only one distinct value return `-1`

Examples:

- `sales = [12, 7, 19, 19, 5, 14]` → `14`
- `sales = [8, 8, 8]` → `-1`

The algorithmic constraint that matters is the `O(n)` time and `O(1)` extra space requirement, which rules out sort-first approaches as the intended solution.

## 🪜 How to Solve This
1. Read the requirement carefully → it is not “second element after sorting,” it is “second largest **distinct** value.”
2. Distinctness means duplicates of the maximum should be ignored, so simply counting positions is wrong.
3. Since we only need the top two distinct values, storing the full array state is unnecessary.
4. That suggests maintaining two running values while scanning once:
   - `largest`
   - `secondLargest`
5. For each sale:
   - If it is greater than `largest`, promote the old `largest` into `secondLargest`, then update `largest`
   - Else if it is smaller than `largest` but greater than `secondLargest`, update `secondLargest`
   - Else ignore it, because it is either a duplicate or too small to matter
6. The reason this works is that every element can only affect the top two distinct positions; anything below them is irrelevant.
7. At the end, if `secondLargest` was never assigned a valid distinct value, return `-1`.

This is the natural “streaming top-2” mental model: process each item once, preserve only the minimal state needed for the answer.

## 🧩 Algorithm Walkthrough
1. **Use the One-Pass Top-Two Tracking pattern.**  
   This problem is a small instance of streaming top-k selection with `k = 2`. That abstraction is the right fit because the output depends only on the two highest distinct values, not on global ordering.

2. **Initialize two sentinels:** `largest` and `secondLargest`.  
   They represent the best and second-best distinct values seen so far. Initialization must allow negative inputs, so use a sentinel smaller than any valid input or an explicit “unset” state.

3. **Scan the array from left to right once.**  
   Each element is evaluated exactly once, satisfying the linear-time requirement.

4. **If `sale > largest`, shift the current maximum down.**  
   Set `secondLargest = largest`, then `largest = sale`.  
   This is correct because any previous maximum becomes the best candidate for second place once a new maximum appears.  
   **Invariant:** after this update, `largest` is the greatest distinct value seen so far.

5. **Else if `sale < largest` and `sale > secondLargest`, update second place.**  
   The strict `< largest` check enforces distinctness, preventing duplicates of the maximum from polluting second place.  
   **Invariant:** `secondLargest` is always the greatest value strictly less than `largest` among processed elements.

6. **Otherwise ignore the value.**  
   It is either a duplicate of `largest`, equal to `secondLargest`, or too small to change the answer.

7. **Return the result.**  
   If `secondLargest` is still unset, there was no second distinct value, so return `-1`.

The core correctness argument is the maintained invariant: after processing any prefix, the pair (`largest`, `secondLargest`) is exactly the top two distinct values in that prefix.

## 📊 Worked Example
Example: `sales = [12, 7, 19, 19, 5, 14]`

| Step | sale | largest | secondLargest | Action |
|------|------|---------|---------------|--------|
| 1 | 12 | 12 | unset | First maximum found |
| 2 | 7  | 12 | 7 | New second largest |
| 3 | 19 | 19 | 12 | New maximum; old maximum shifts down |
| 4 | 19 | 19 | 12 | Ignore duplicate maximum |
| 5 | 5  | 19 | 12 | Ignore; too small |
| 6 | 14 | 19 | 14 | Better second largest |

Final result: `14`.

What matters in the trace is the invariant, not the local comparisons. After each step, `largest` and `secondLargest` are the top two distinct values seen so far. Notice that the duplicate `19` does nothing, which is exactly the distinctness rule. Also notice that `14` can still improve the answer late in the scan even though it is not a new maximum.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, because the array is scanned once and each element triggers only constant-time comparisons and assignments. There is no nested iteration, sorting, or auxiliary lookup structure. At `10^6` elements this remains trivial on modern hardware; at `10^9`, linear cost is still large but is the best possible for exact selection from an unsorted stream.

### Space Complexity
`O(1)` extra space. The algorithm stores only two running values plus optional sentinel or state flags. No heap, set, or copied array is required. Space cannot be meaningfully reduced further without losing the ability to represent both top distinct candidates during the scan.

## 💡 Key Takeaways
- If a problem asks for the largest or second-largest value under strict space bounds, think streaming top-k rather than sorting.
- “Distinct” is the signal that equality checks matter; duplicates must be filtered during the scan, not cleaned up afterward.
- Do not update `secondLargest` when `sale == largest`; that incorrectly treats duplicate maxima as distinct values.
- Be careful with initialization when negatives are allowed; naive defaults like `0` break correctness on all-negative arrays.
- The production-grade insight is that bounded ranking problems often need only summary state, which enables low-latency, memory-stable stream processing.

## 🚀 Variations & Further Practice
- Return the **third** largest distinct sale: same pattern, but now maintain top-3 state and reason carefully about ordered updates.
- Return the top-`k` distinct sales from a stream: introduces a bounded heap or ordered structure, trading `O(1)` state for configurable ranking depth.
- Find the second largest sale per region or store: same local logic, but composed with grouping, which turns the problem into per-key streaming aggregation.