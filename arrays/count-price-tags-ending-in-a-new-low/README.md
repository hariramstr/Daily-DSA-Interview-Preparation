# Count Price Tags Ending in a New Low

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Prefix Minimum

---

## 🗂 Problem Overview
Given an integer array `prices`, count how many elements are strictly smaller than every value that appeared earlier in the array. The first element always counts because it has no predecessors. The output is a single integer: the number of “new low” price tags encountered during a left-to-right scan. The key constraint is scale: with up to `100000` elements, the solution must avoid rechecking all prior values for each position.

## 🌍 Engineering Impact
This is the same shape as many streaming “record event” problems in production systems: detecting new latency minima in observability pipelines, tracking cheapest quote seen so far in market data feeds, identifying lowest resource cost across scheduler decisions, or flagging best-so-far scores in ranking systems. At scale, the difference between rescanning history and maintaining a running aggregate is the difference between linear throughput and collapse under load. The prefix-minimum pattern enables single-pass processing, bounded memory, and easy composition into stream processors, log consumers, and online analytics paths where historical replay is expensive or impossible.

## 🔍 Problem Statement
You are given an integer array `prices` where `prices[i]` is the price tag assigned to the `i`-th item in labeling order. A price ends in a new low if it is **strictly smaller** than every earlier price. Return the total count of such items.

Constraints:

- `1 <= prices.length <= 100000`
- `-1000000000 <= prices[i] <= 1000000000`
- Duplicates may appear

Important edge cases:

- The first element always contributes `1`
- A value equal to the smallest seen so far does **not** count
- Negative values are valid, so initialization should not assume non-negative input

Examples:

- `prices = [12, 10, 11, 9, 9, 7]` → `4`
- `prices = [5, 5, 5, 4, 6, 3]` → `3`

The algorithmic driver is the input size: nested comparisons would degrade to `O(n^2)`, which is unnecessary because only the smallest prior value matters.

## 🪜 How to Solve This
1. Read the condition carefully → an element qualifies only if it is smaller than **all previous elements**.
2. Replace “all previous elements” with the only fact that matters: the **minimum seen so far**.
3. That observation collapses the problem from “compare against a prefix” to “compare against one running value.”
4. Start from the left because the definition depends on earlier elements, not later ones.
5. Initialize the first element as both:
   - the first counted new low, and
   - the current running minimum.
6. For each later price:
   - if it is `< currentMin`, increment the answer and update `currentMin`
   - otherwise ignore it
7. Notice why duplicates do not count: the rule is strictly smaller, so `== currentMin` must be skipped.
8. This is the classic **prefix minimum / streaming aggregate** pattern: one pass, constant state, no need to store or revisit prior elements.

Once you see that the entire prefix can be summarized by one scalar, the linear solution becomes the obvious implementation.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Prefix Minimum.**  
   This problem asks whether each element beats every earlier element. The right abstraction is a running prefix minimum, because the minimum fully summarizes whether the current value is a new record low.

2. **Initialize state from the first element.**  
   Set `count = 1` and `minSoFar = prices[0]`. This is correct because the first item has no prior values, so it always qualifies.  
   **Invariant:** after processing index `i`, `minSoFar` is the minimum of `prices[0..i]`, and `count` is the number of new lows in that prefix.

3. **Scan left to right from index 1.**  
   For each `prices[i]`, compare it with `minSoFar`. No other historical information is needed because if `prices[i] < minSoFar`, it is automatically smaller than every prior value.

4. **Handle the qualifying case.**  
   If `prices[i] < minSoFar`, increment `count` and assign `minSoFar = prices[i]`.  
   **Why correct:** the current value establishes a strictly lower minimum than any earlier value, so it must be counted exactly once.

5. **Handle the non-qualifying case.**  
   If `prices[i] >= minSoFar`, do nothing. Equal values are not new lows because the condition is strict. Larger values obviously fail as well.

6. **Return `count` after the scan.**  
   The invariant guarantees correctness for the full array once the traversal completes.

This is the right abstraction because it converts repeated prefix comparisons into a single streaming state update, yielding `O(n)` time and `O(1)` extra space.

## 📊 Worked Example
Example: `prices = [12, 10, 11, 9, 9, 7]`

| i | price | minSoFar before | New low? | minSoFar after | count |
|---|------:|----------------:|:--------:|---------------:|------:|
| 0 | 12    | —               | Yes      | 12             | 1 |
| 1 | 10    | 12              | Yes      | 10             | 2 |
| 2 | 11    | 10              | No       | 10             | 2 |
| 3 | 9     | 10              | Yes      | 9              | 3 |
| 4 | 9     | 9               | No       | 9              | 3 |
| 5 | 7     | 9               | Yes      | 7              | 4 |

The trace shows the entire decision process. At each step, only one comparison matters: current value versus the smallest value seen earlier. The second `9` is rejected because equality does not satisfy the strict-lower requirement. Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = prices.length`. The dominant operation is a single comparison per element during one left-to-right pass. At `10^6` elements this is routine and cache-friendly; at `10^9`, it is still linear but now dominated by memory bandwidth and I/O rather than algorithmic overhead.

### Space Complexity
`O(1)` extra space. The algorithm stores only two scalars: the running minimum and the count. This cannot be meaningfully reduced further without losing required state; any alternative with stored prefixes would only increase memory with no benefit.

## 💡 Key Takeaways
- If a condition says “smaller than all previous values,” look for a **prefix minimum** rather than repeated scans of prior elements.
- Streaming, left-to-right record detection is a strong signal that a single running aggregate can replace historical storage.
- The comparison must be **strictly `<`**, not `<=`; duplicates of the current minimum do not count.
- Do not forget the first element contributes `1`; starting `count` at `0` without special handling is the common off-by-one bug.
- In production systems, many online decisions become tractable once a full history can be summarized by a compact invariant updated per event.

## 🚀 Variations & Further Practice
- Count elements that are greater than all previous elements instead of smaller: same pattern, but with a **prefix maximum** and reversed comparison.
- Return the indices or values of all new lows, not just the count: same scan, but now output collection size grows with the number of record events.
- For each position, compute whether it is smaller than everything to its left **and** everything to its right: requires combining prefix minima with suffix minima, introducing bidirectional preprocessing.