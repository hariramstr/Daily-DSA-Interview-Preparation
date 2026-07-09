# Count Days with a New Highest Step Total

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Prefix Maximum

---

## 🗂 Problem Overview
Given an integer array `steps`, count how many days establish a new all-time high step total. Day `i` is a record day if `steps[i]` is strictly greater than every value in `steps[0..i-1]`. The first day always qualifies. The input is a single array; the output is one integer count. The only real constraint is scale: with up to `100000` entries, the solution should be a single linear scan with constant extra state.

## 🌍 Engineering Impact
This is the same pattern used in streaming metrics, observability backends, ranking pipelines, and user analytics systems whenever you need to detect new highs without retaining full history. Examples include peak QPS tracking in API gateways, best-so-far latency regressions in performance dashboards, and leader updates in event-driven scoring systems. At scale, recomputing “max so far” from scratch per event turns a trivial streaming problem into quadratic work. Maintaining a prefix maximum enables one-pass processing, low memory usage, and straightforward online operation over append-only data or Kafka-style event streams.

## 🔍 Problem Statement
You are given an integer array `steps` where `steps[i]` is the number of steps walked on day `i`. A day is a record day if its value is strictly greater than every earlier day's value. Day `0` is always a record day because there is no prior data.

Return the total number of record days.

Constraints:

- `1 <= steps.length <= 100000`
- `0 <= steps[i] <= 1000000`

Examples:

- `steps = [3000, 4500, 4200, 5000, 5000, 6200]` → `4`  
  Record days: indices `0, 1, 3, 5`

- `steps = [8000, 7000, 7000, 6500]` → `1`  
  Only the first day is a record day

The key constraint is array length: this rules out any approach that compares each day against all previous days. The intended solution is a linear pass that maintains the highest value seen so far.

## 🪜 How to Solve This
1. Read the condition carefully → a day qualifies only if it beats **all previous days**, not just the immediately previous one.

2. “Greater than every earlier element” is a prefix property → for each position, the only historical fact that matters is the maximum seen so far.

3. That observation collapses the problem state dramatically → instead of storing prior days or rescanning them, keep one running maximum and one counter.

4. Scan left to right:
   - If the current value is greater than the running maximum, this day is a new record.
   - Increment the count.
   - Update the running maximum.

5. Why this is enough → once you know the largest prior value, comparing against anything smaller is irrelevant. The full history compresses into a single number.

6. Why this is the right first-pass instinct → whenever a problem asks whether each element beats all previous elements, think **prefix maximum**. That usually implies an online, one-pass solution with `O(n)` time and `O(1)` extra space.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Prefix Maximum / Streaming Scan**  
   This is a classic prefix-aggregate problem. For each index, we need a property of the prefix `steps[0..i-1]`: specifically, its maximum. Prefix maximum is the right abstraction because it summarizes exactly the information needed for the next decision.

2. **Initialize running state**  
   Keep:
   - `maxSoFar`: highest step total seen up to the current point
   - `recordDays`: number of record-setting days found so far  
   You can initialize from the first element or use a sentinel smaller than any valid step count. Since day `0` is always a record day, initializing from the first element is clean and avoids special-case comparisons later.

3. **Iterate from left to right**  
   For each day after the first, compare `steps[i]` with `maxSoFar`.

4. **Decide whether the day is a record**  
   If `steps[i] > maxSoFar`, then by definition it exceeds every prior day, because `maxSoFar` is the greatest prior value. Increment `recordDays`.

5. **Update the invariant**  
   After processing day `i`, set `maxSoFar = max(maxSoFar, steps[i])`.  
   Invariant: after each iteration, `maxSoFar` equals the maximum of `steps[0..i]`, and `recordDays` equals the number of indices in that prefix that introduced a new maximum.

6. **Return the count**  
   The scan is complete once every element has been processed. Because the invariant holds at each step, the final count is correct for the entire array.

## 📊 Worked Example
Use `steps = [3000, 4500, 4200, 5000, 5000, 6200]`.

| Day | Steps | `maxSoFar` before | Record Day? | `maxSoFar` after | Count |
|---|---:|---:|---|---:|---:|
| 0 | 3000 | — | Yes | 3000 | 1 |
| 1 | 4500 | 3000 | Yes | 4500 | 2 |
| 2 | 4200 | 4500 | No | 4500 | 2 |
| 3 | 5000 | 4500 | Yes | 5000 | 3 |
| 4 | 5000 | 5000 | No | 5000 | 3 |
| 5 | 6200 | 5000 | Yes | 6200 | 4 |

The important detail is day `4`: equality does not count. A record requires a **strictly greater** value, so the second `5000` is ignored. The final answer is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = steps.length`. Each element is visited exactly once, and each visit performs a constant amount of work: one comparison, maybe one increment, and maybe one assignment. At `10^6` elements this is trivial; at `10^9`, linear time is still expensive but remains the only viable exact single-machine approach.

### Space Complexity
`O(1)` extra space. The algorithm stores only two scalars: the running maximum and the record-day count. No auxiliary arrays or hash structures are required. Space cannot be meaningfully reduced further unless input is streamed and not materialized, which changes I/O handling rather than algorithmic state.

## 💡 Key Takeaways
- If a condition says an element must beat **all previous elements**, that is a strong signal for a prefix maximum scan.
- If the output depends on sequential history but only through one aggregate fact, look for a streaming `O(1)`-state solution instead of nested comparisons.
- The comparison must be `>` rather than `>=`; equal values do not create a new record.
- Be careful with initialization: either count day `0` up front or use a sentinel and let the first element qualify naturally, but do not do both.
- In production systems, prefix aggregates are a core technique for converting historical queries into online, append-friendly computations.

## 🚀 Variations & Further Practice
- Count days that are higher than the previous `k` days instead of all previous days; the twist is maintaining a sliding-window maximum rather than a prefix maximum.
- Return the indices of record days and the margin by which each record exceeded the previous best; same scan, but richer output and more careful state handling.
- Process a live event stream with out-of-order arrivals; the harder part is no longer the scan itself but preserving correctness under reordering and late data.