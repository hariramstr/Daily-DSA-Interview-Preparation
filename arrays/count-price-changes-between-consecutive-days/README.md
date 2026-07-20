# Count Price Changes Between Consecutive Days

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Counting

---

## 🗂 Problem Overview
Given an integer array `prices`, count how many adjacent day-to-day transitions represent a price change. For every index `i > 0`, compare `prices[i]` with `prices[i - 1]`; if they differ, increment the answer. Return the total count of such transitions. The only real constraint is scale: with up to `100000` elements, the solution should be a single linear pass rather than anything involving nested comparisons or unnecessary auxiliary structures.

## 🌍 Engineering Impact
This pattern appears in telemetry pipelines, market-data summarization, feature-flag rollout analytics, and observability dashboards where the metric is transition count rather than magnitude. In streaming systems, you often care about state flips, config churn, or status changes across consecutive events. At scale, the difference between a one-pass adjacency scan and heavier state reconstruction matters: dashboards need predictable latency, low memory pressure, and easy incremental updates. This approach enables online processing, works naturally on append-only event streams, and avoids over-modeling when only transition frequency is needed.

## 🔍 Problem Statement
You are given an integer array `prices` where `prices[i]` is the product price on day `i`. Count how many times the price changed relative to the previous day. A change occurs only when two consecutive values are different.

Return the number of indices `i` such that `1 <= i < prices.length` and `prices[i] != prices[i - 1]`.

Constraints:

- `0 <= prices.length <= 100000`
- `-1000000000 <= prices[i] <= 1000000000`

Edge cases matter:

- Empty array → `0`
- Single-element array → `0`
- All equal values → `0`

Examples:

- `prices = [10, 10, 12, 12, 9, 9, 11]` → `3`
- `prices = [4, 4, 4, 4]` → `0`

The key algorithmic constraint is input size: the array is large enough that the correct solution should inspect each consecutive pair exactly once.

## 🪜 How to Solve This
1. Read the problem → notice we are not asked for the size of each change, only whether a transition happened.

2. “Compared with the previous day” is the key phrase → this is an adjacency problem, not a global counting or sorting problem.

3. Once the problem is framed as checking consecutive pairs, the brute-force shape disappears. There is exactly one useful comparison per element after the first: `prices[i]` vs. `prices[i - 1]`.

4. That suggests a linear scan:
   - start from index `1`
   - compare current value with previous value
   - increment a counter when they differ

5. Why this is enough:
   - every valid price change is defined by one adjacent pair
   - no pair needs to be revisited
   - no extra state beyond the running count and previous element is required

6. Edge cases fall out naturally:
   - arrays of length `0` or `1` have no adjacent pairs
   - repeated runs like `[5, 5, 5]` contribute nothing until the value changes

This is a textbook single-pass simulation/counting problem over an array.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Single-Pass Array Scan / Simulation.**  
   The problem is about local transitions between consecutive elements. That makes a linear adjacency scan the right abstraction. No hashing, sorting, or dynamic programming is needed because each decision depends only on the current and previous values.

2. **Initialize a counter to zero.**  
   This counter represents the number of detected price changes so far. The invariant is: after processing index `i`, the counter equals the number of differing adjacent pairs in the prefix `prices[0..i]`.

3. **Start iterating from index `1`.**  
   Index `0` has no previous day, so no comparison is possible there. Starting at `1` avoids special-case logic inside the loop and aligns directly with the problem definition.

4. **Compare `prices[i]` with `prices[i - 1]`.**  
   If they are different, increment the counter. This is correct because each adjacent pair contributes either exactly one change or zero changes—never more.

5. **Maintain the invariant after each step.**  
   For every processed index, exactly one new adjacent pair has been evaluated. If the pair differs, the counter increases by one; otherwise it stays unchanged. The counter therefore remains accurate for the processed prefix.

6. **Return the counter after the loop ends.**  
   At termination, all consecutive pairs in the array have been examined exactly once, so the counter is the final answer.

This algorithm is optimal for the stated contract: one pass, constant extra space, and no redundant work.

## 📊 Worked Example
Use `prices = [10, 10, 12, 12, 9, 9, 11]`.

| i | prices[i - 1] | prices[i] | Different? | changes |
|---|---------------|-----------|------------|---------|
| 1 | 10            | 10        | No         | 0       |
| 2 | 10            | 12        | Yes        | 1       |
| 3 | 12            | 12        | No         | 1       |
| 4 | 12            | 9         | Yes        | 2       |
| 5 | 9             | 9         | No         | 2       |
| 6 | 9             | 11        | Yes        | 3       |

Trace summary:

1. Start with `changes = 0`.
2. Compare each day with the previous day.
3. Increment only on transitions `10 → 12`, `12 → 9`, and `9 → 11`.
4. Equal runs do not matter; they simply preserve the current count.

Final answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time, where `n` is `prices.length`, because it performs exactly one comparison for each element after the first. At `10^6` elements this is still a straightforward in-memory scan; at `10^9`, the bottleneck becomes I/O and data movement rather than the comparison logic itself.

### Space Complexity
The algorithm uses **O(1)** extra space. The only additional storage is the running counter and loop state; no auxiliary array or map is required. Space cannot be meaningfully reduced further unless the input is streamed, in which case the same constant-space logic still applies.

## 💡 Key Takeaways
- If the problem says “compare with previous” or “count transitions between consecutive elements,” think single-pass adjacency scan immediately.
- When the output depends only on local neighboring relationships, global structures like sorting or hashing are usually unnecessary.
- Start the loop at index `1`; index `0` has no previous element, and forcing it into the loop often creates off-by-one bugs.
- Do not count equal consecutive values; repeated runs contribute zero until the value actually changes.
- In production analytics, transition counting is a low-cost way to quantify churn or instability without retaining full event history.

## 🚀 Variations & Further Practice
- Count **upward** and **downward** price changes separately; the twist is classifying each transition into multiple categories instead of a single binary count.
- Process prices as a **stream** and answer incrementally after each new event; the twist is maintaining only the previous value and cumulative state online.
- Count changes within a **sliding window** of days; the twist is that transitions entering and leaving the window must both be updated correctly.