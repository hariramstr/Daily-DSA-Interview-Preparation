# Count Runners Who Improved Their Best Lap

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** arrays, simulation, prefix-minimum

---

## 🗂 Problem Overview
Given an array `laps`, where each value is a runner’s lap time for that day, count how many days produced a strictly better lap than every earlier day. The first element establishes the initial best but never counts as an improvement. The key constraint is scale: with up to `100000` entries, the solution must process the array in one pass, tracking only the smallest lap seen so far.

## 🌍 Engineering Impact
This is the same shape as many production streaming decisions: detect whether an event sets a new record relative to all prior events. You see it in latency monitoring dashboards tracking best-so-far response times, trading systems identifying new intraday lows, search infrastructure recording best ranking scores, and observability pipelines flagging new minima in error budgets or queue depth. At scale, recomputing against all prior history per event is unacceptable; it turns a linear stream into quadratic work. The prefix-minimum pattern enables online processing, bounded memory, and easy composition into larger streaming or batch pipelines.

## 🔍 Problem Statement
You are given an integer array `laps` where `laps[i]` is the runner’s lap time on day `i`. Smaller values are better. A day counts as an improvement only if `laps[i]` is **strictly smaller** than every value in `laps[0..i-1]`. The first day does not count, because there is no earlier lap to compare against.

Return the total number of improvement days.

Constraints:

- `1 <= laps.length <= 100000`
- `1 <= laps[i] <= 1000000000`

Examples:

- `laps = [72, 70, 71, 69, 69, 68]` → `3`
- `laps = [55, 55, 55, 54, 53]` → `2`

Important edge cases:

- Arrays of length `1` always return `0`
- Equal lap times do **not** count
- The input size rules out nested comparisons across all prior days

## 🪜 How to Solve This
1. Read the condition carefully → a day counts only if it beats **all previous** lap times, not just the immediately previous one.

2. “Beats all previous values” should trigger a prefix summary idea → instead of remembering every earlier lap, we only need the **smallest** one seen so far.

3. Start from the first day → it defines the current best lap, but contributes nothing to the answer.

4. Scan left to right once:
   - If the current lap is smaller than the running minimum, increment the improvement count.
   - Then update the running minimum to this new value.

5. If the current lap equals the minimum, do nothing → the problem requires a **strict** improvement.

6. This avoids comparing each day with every prior day. The mental move is: replace “all prior values” with a maintained aggregate, here a prefix minimum.

That gives a linear-time, constant-space solution immediately.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: prefix minimum.**  
   The problem asks whether each element is smaller than all earlier elements. That is exactly the definition of being smaller than the prefix minimum up to the previous index. This makes prefix-minimum tracking the right abstraction.

2. **Initialize state from the first element.**  
   Set `minSoFar = laps[0]` and `count = 0`. This is correct because the first day establishes the baseline best lap, but cannot be an improvement by definition.

3. **Iterate from index `1` to the end.**  
   For each `laps[i]`, compare it with `minSoFar`. This is sufficient because `minSoFar` is the smallest value among all earlier days.

4. **Count only strict improvements.**  
   If `laps[i] < minSoFar`, increment `count`. This is correct because being smaller than the smallest previous lap implies being smaller than every previous lap. If `laps[i] == minSoFar`, it does not count.

5. **Update the invariant.**  
   After processing index `i`, set `minSoFar = min(minSoFar, laps[i])`. The maintained invariant is: after each iteration, `minSoFar` equals the minimum value in `laps[0..i]`.

6. **Return the count.**  
   Since every day is processed once and evaluated against the correct prefix minimum, the final count is exactly the number of improvement days.

This is a single-pass simulation over an array with a rolling aggregate.

## 📊 Worked Example
Example: `laps = [72, 70, 71, 69, 69, 68]`

| Day | Lap | `minSoFar` before | Improvement? | `count` after | `minSoFar` after |
|---|---:|---:|---|---:|---:|
| 0 | 72 | —  | No, first day | 0 | 72 |
| 1 | 70 | 72 | Yes (`70 < 72`) | 1 | 70 |
| 2 | 71 | 70 | No | 1 | 70 |
| 3 | 69 | 70 | Yes (`69 < 70`) | 2 | 69 |
| 4 | 69 | 69 | No, equal is not strict | 2 | 69 |
| 5 | 68 | 69 | Yes (`68 < 69`) | 3 | 68 |

Final answer: `3`.

The important state is just two variables: the best lap seen so far and the number of times that best was broken.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time, where `n` is `laps.length`, because it performs one left-to-right scan and constant work per element. At `10^6` elements this is still routine. At `10^9`, linear work is large but still the only viable asymptotic option; anything quadratic is dead on arrival.

### Space Complexity
The algorithm uses **O(1)** extra space. The only additional state is the running minimum and the improvement counter. There is nothing meaningful to reduce further unless you trade away readability for no practical gain.

## 💡 Key Takeaways
- If a condition says an element must beat **all previous elements**, look for a prefix aggregate rather than repeated backward scans.
- “Record-breaking event” problems are often just running `min` or running `max` with a counter layered on top.
- Do not count index `0`; it establishes the baseline but has no prior value to improve upon.
- Use a **strict** comparison (`<`), not `<=`; equal lap times are ties, not improvements.
- In production systems, maintaining compact rolling summaries is how you turn unbounded history checks into stream-friendly, memory-stable processing.

## 🚀 Variations & Further Practice
- Count days that improve over the **previous `k` days** instead of all prior days; the twist is that prefix minimum no longer works, and you need a sliding-window minimum structure.
- Return the **indices** or values of all improvement days, not just the count; same core pattern, but now output size becomes part of the space discussion.
- Track both new **best** and new **worst** laps in one pass; the twist is maintaining multiple rolling extrema and handling separate event counts correctly.