# Count Late Arrivals After the Daily Cutoff

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Counting, Simulation

---

## 🗂 Problem Overview
Given an unsorted array of employee arrival times in minutes after midnight and a cutoff time, return how many arrivals are strictly later than the cutoff. The input is a single-day snapshot; the output is one integer count. The only subtlety is the inequality: arrivals equal to the cutoff are on time, not late. With up to 100,000 entries, the right solution is a single linear scan rather than sorting or any heavier aggregation.

## 🌍 Engineering Impact
This pattern shows up anywhere systems classify events against a threshold: SLA breach counting in observability pipelines, late-job detection in schedulers, fraud or anomaly counters in stream processors, and policy enforcement in access-control or rate-limiting systems. At scale, the difference between “scan once and increment a counter” versus “sort first” is not cosmetic; it determines whether the logic can run inline on hot paths, inside batch windows, or on streaming partitions. The broader lesson is to match the algorithm to the question being asked: if you only need a count above a threshold, preserve throughput and avoid unnecessary reordering or state.

## 🔍 Problem Statement
You are given an integer array `arrivalTimes`, where each value is an employee’s arrival time in minutes after midnight for one day, and an integer `cutoff`, the latest arrival still considered on time. Return the number of employees whose arrival time is **strictly greater than** `cutoff`.

Constraints:

- `1 <= arrivalTimes.length <= 100000`
- `0 <= arrivalTimes[i] <= 1439`
- `0 <= cutoff <= 1439`
- The result fits in a 32-bit integer

The array is not sorted, and duplicate arrival times may appear. Equality matters: `arrivalTimes[i] == cutoff` is on time.

Examples:

- `arrivalTimes = [525, 541, 540, 600, 539], cutoff = 540` → `2`
- `arrivalTimes = [480, 480, 481, 479], cutoff = 480` → `1`

The key constraint is input size: with up to 100,000 elements, the natural choice is an `O(n)` pass with constant extra space.

## 🪜 How to Solve This
1. Read the problem carefully → the output is not the latest arrival, not the list of late employees, and not a sorted schedule. It is just a **count**.

2. Notice the decision rule is local per element → for each arrival time, we only need to answer one question: is it `> cutoff`?

3. Because each element can be classified independently, there is no dependency between positions → that rules out the need for sorting, prefix structures, or nested comparisons.

4. Translate that into a scan → initialize a counter to zero, iterate through the array once, and increment the counter whenever an arrival is strictly greater than the cutoff.

5. Be precise about the boundary → `cutoff` itself is on time, so the condition must be `>` and not `>=`.

6. Validate against edge cases → all on time should return `0`; all late should return `arrivalTimes.length`; duplicates should be counted normally.

This is a classic linear counting pass over an array. The right instinct is: if the question is “how many items satisfy a predicate,” scan once and count.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Linear Scan + Counting Predicate.**  
   This is not a search, sort, or two-pointer problem. The correct abstraction is a single-pass counting pattern: evaluate each element against a boolean condition and accumulate the number of matches.

2. **Initialize a running counter.**  
   Start with `lateCount = 0`. This variable represents the number of processed arrivals that are already known to be late.  
   **Invariant:** after processing the first `i` elements, `lateCount` equals the number of values greater than `cutoff` among those `i` elements.

3. **Iterate through `arrivalTimes` once.**  
   For each `time` in the array, compare it to `cutoff`. No preprocessing is needed because order is irrelevant to the final count.  
   **Why this is correct:** each employee contributes independently: either 1 if late, or 0 if on time.

4. **Apply the strict inequality.**  
   If `time > cutoff`, increment `lateCount`. If `time == cutoff` or `time < cutoff`, do nothing.  
   **Critical detail:** the problem defines “late” as strictly after the cutoff. Equality must not be counted.

5. **Return the counter after the loop completes.**  
   Once every element has been examined exactly once, the invariant implies `lateCount` is the total number of late arrivals in the full array.

This approach is optimal for the stated problem because it minimizes both work and memory: one pass, one integer accumulator, no auxiliary structures.

## 📊 Worked Example
Example: `arrivalTimes = [525, 541, 540, 600, 539]`, `cutoff = 540`

| Index | Arrival Time | `time > cutoff` | `lateCount` after step |
|------:|-------------:|:---------------:|-----------------------:|
| 0 | 525 | No  | 0 |
| 1 | 541 | Yes | 1 |
| 2 | 540 | No  | 1 |
| 3 | 600 | Yes | 2 |
| 4 | 539 | No  | 2 |

Trace:

1. Start with `lateCount = 0`.
2. `525` is before `540` → still `0`.
3. `541` is after `540` → increment to `1`.
4. `540` equals the cutoff → still on time, remain at `1`.
5. `600` is after the cutoff → increment to `2`.
6. `539` is before the cutoff → no change.

Final answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time, where `n` is `arrivalTimes.length`, because it performs one comparison per element and a constant-time increment when needed. At `10^6` elements this is still routine for in-memory processing; at `10^9`, the bottleneck becomes data movement and I/O, not the comparison itself.

### Space Complexity
The algorithm uses `O(1)` extra space. The only additional state is the running counter and loop variable; no auxiliary array, map, or sorted copy is required. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the prompt asks for the number of elements satisfying a simple condition, default to a single-pass counting scan before considering sorting or indexing.
- When element order does not affect the answer, treat the array as an unordered stream and look for an `O(n)` accumulator pattern.
- The core off-by-one trap is using `>= cutoff`; the problem requires `> cutoff` because arrivals exactly at the cutoff are on time.
- Duplicate times do not need deduplication; each array entry represents one employee and must be counted independently.
- In production code, threshold classification is often throughput-sensitive; choosing the minimal-state linear pass keeps hot-path logic predictable and scalable.

## 🚀 Variations & Further Practice
- Return the **indices or records** of late arrivals instead of just the count; same predicate, but now output size varies and memory usage becomes part of the design.
- Process **multiple cutoff queries** against the same arrival array; the harder twist is choosing between repeated linear scans, frequency buckets over `0..1439`, or sorted preprocessing.
- Extend to **streaming arrivals over time windows** and maintain rolling late counts; the conceptual jump is from batch counting to incremental state management and window eviction.