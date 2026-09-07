# Find the First Local Peak Day

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Linear Scan

---

## 🗂 Problem Overview
Given an integer array `visitors`, return the index of the first day whose value is strictly greater than both adjacent days. Only indices `1` through `n - 2` are eligible, since endpoints do not have two neighbors. If no such index exists, return `-1`. The problem is simple in shape but requires disciplined boundary handling and strict comparison semantics, making a single left-to-right linear scan the right solution.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the earliest local anomaly or turning point in ordered telemetry: traffic spikes in observability pipelines, first latency regressions in time-series monitoring, local maxima in clickstream analysis, or burst detection in rate-limiter dashboards. At scale, the requirement is rarely “find all peaks”; it is “detect the first actionable one and stop.” That changes the implementation choice. A linear scan with early exit minimizes latency, avoids unnecessary memory, and composes well inside streaming or batch jobs where millions of sequences are evaluated continuously.

## 🔍 Problem Statement
You are given an integer array `visitors` where `visitors[i]` is the number of store visitors on day `i`. A day `i` is a local peak if:

- `visitors[i] > visitors[i - 1]`
- `visitors[i] > visitors[i + 1]`

Return the index of the **first** local peak. If no local peak exists, return `-1`.

Important constraints and edge cases:

- `3 <= visitors.length <= 10^5`
- `0 <= visitors[i] <= 10^6`
- The first and last indices can never be peaks
- Comparison is **strict**, so equal adjacent values do not qualify

Examples:

- `visitors = [12, 18, 15, 20, 19]` → `1`
- `visitors = [5, 7, 7, 6, 4]` → `-1`

The key constraint is that only neighboring values matter, which rules out heavier techniques and points directly to a linear scan.

## 🪜 How to Solve This
1. Read the condition carefully → a peak depends only on `i - 1`, `i`, and `i + 1`. That means each decision is local, not global.

2. Notice the output asks for the **first** valid index → scanning from left to right is enough. The moment a peak is found, we can return immediately.

3. Exclude impossible positions up front → index `0` and index `n - 1` cannot be peaks because they do not have two neighbors.

4. For each index `i` from `1` to `n - 2`, compare the current value with both adjacent values.

5. Use strict comparisons exactly as stated → `>` on both sides. If either neighbor is equal or larger, this index is not a peak.

6. If a peak is found, return `i` immediately. If the scan finishes without finding one, return `-1`.

Why this approach is obvious in hindsight: the problem is a local predicate over a contiguous array, with no need to reorder, cache history, or compute aggregates. That is the signature of a linear scan.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Linear Scan / Simulation.**  
   This is the right abstraction because each candidate index can be validated using constant-time checks against adjacent elements. There is no dependency on distant values, so more complex structures would add cost without improving correctness.

2. **Initialize iteration at index `1`.**  
   We skip index `0` because it lacks a previous element. This preserves the invariant that every inspected index has both required neighbors.

3. **Iterate through index `n - 2`.**  
   We stop before the last element for the symmetric reason: index `n - 1` lacks a next element. The invariant remains: every examined position is eligible to be a peak.

4. **At each index `i`, evaluate the local peak predicate.**  
   Check whether `visitors[i] > visitors[i - 1]` and `visitors[i] > visitors[i + 1]`. This is correct because the definition of a local peak is exactly this two-sided strict inequality.

5. **Return immediately on the first match.**  
   Since the scan proceeds left to right, the first valid index encountered is guaranteed to be the earliest local peak. Early exit is both correct and optimal for best-case latency.

6. **Return `-1` if the loop completes.**  
   At that point, every eligible index has been checked and none satisfied the predicate. Therefore no local peak exists.

This algorithm maintains a simple invariant: before each iteration, all earlier eligible indices have already been proven not to be peaks. That makes the first success final.

## 📊 Worked Example
Consider `visitors = [12, 18, 15, 20, 19]`.

| i | visitors[i-1] | visitors[i] | visitors[i+1] | Peak? |
|---|---:|---:|---:|---|
| 1 | 12 | 18 | 15 | Yes |
| 2 | 18 | 15 | 20 | No |
| 3 | 15 | 20 | 19 | Yes |

Trace:

1. Start at `i = 1`, the first eligible index.
2. Compare `18` with neighbors `12` and `15`.
3. `18 > 12` and `18 > 15`, so index `1` is a local peak.
4. Return `1` immediately.
5. Although index `3` is also a peak, it is irrelevant because the requirement is to return the **first** one.

This example shows why left-to-right traversal plus early exit is sufficient: once a valid peak appears, later candidates cannot change the answer.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time, where `n` is the length of `visitors`. Each eligible index is checked once, and each check is constant time. At `10^6` elements this is trivial for modern hardware; at `10^9`, linear cost becomes material but still remains the optimal asymptotic bound for arbitrary input.

### Space Complexity
The algorithm uses **O(1)** extra space. It stores only the loop index and performs direct comparisons against the input array. There is no auxiliary data structure to reduce further; this is already space-optimal unless the input representation itself changes.

## 💡 Key Takeaways
- If a condition depends only on adjacent elements in an array, think linear scan before considering heavier machinery.
- If the problem asks for the **first** match, left-to-right traversal with early exit is usually the dominant simplification.
- Do not evaluate index `0` or `n - 1`; they can never satisfy a two-neighbor predicate.
- Use strict `>` comparisons on both sides; equal neighbors invalidate a peak.
- In production code, local predicates plus early termination are a strong pattern for low-latency anomaly detection over ordered data.

## 🚀 Variations & Further Practice
- Return **all** local peak indices instead of the first one; same scan, but no early exit and output size now affects total work.
- Find a peak in a much larger array using **binary search** assumptions; the twist is exploiting shape properties to beat linear scan.
- Extend to streaming data where values arrive incrementally; the harder part is maintaining enough local context to emit peaks online without rescanning.