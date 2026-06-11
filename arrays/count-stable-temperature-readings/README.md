# Count Stable Temperature Readings

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Math

---

## 🗂 Problem Overview
Given an integer array `readings`, count how many interior indices `i` satisfy `readings[i] * 2 == readings[i - 1] + readings[i + 1]`. The first and last elements are never eligible because they do not have two neighbors. The input size can reach `100000`, so the solution must avoid nested scans and operate in linear time with constant extra space. The output is a single integer: the number of stable readings.

## 🌍 Engineering Impact
This pattern shows up in telemetry pipelines, IoT sensor validation, observability backends, and market-data smoothing checks, where each sample is validated against a local neighborhood rather than a global model. At scale, the important design choice is streaming-friendly, constant-space evaluation: each record can be classified using only adjacent values. Without that property, data-quality checks become batch-heavy, memory-intensive, and harder to parallelize. The broader lesson is architectural: when a rule depends only on local context, exploit that locality to keep validation cheap, composable, and deployable in hot paths.

## 🔍 Problem Statement
You are given an integer array `readings` where `readings[i]` is the temperature recorded on day `i`. A reading is **stable** if it is exactly the average of its immediate neighbors. Formally, an index `i` is stable when `1 <= i <= n - 2` and:

`readings[i] * 2 == readings[i - 1] + readings[i + 1]`

Return the total number of such indices.

Constraints:
- `1 <= readings.length <= 100000`
- `-100000 <= readings[i] <= 100000`
- Target complexity: `O(n)` time
- Extra space: `O(1)`

If the array has fewer than 3 elements, the answer is `0`, since no element has both neighbors.

Examples:
- `readings = [4, 6, 8, 7, 6]` → `2`
- `readings = [5, 5, 5, 5]` → `2`

The key constraint is array length up to `100000`, which rules out any approach that compares each element against more than its immediate neighbors.

## 🪜 How to Solve This
1. Read the condition carefully → each index depends only on three values: left neighbor, current value, right neighbor.

2. Translate “current equals average of neighbors” into integer arithmetic:
   `readings[i] == (readings[i - 1] + readings[i + 1]) / 2`
   becomes
   `readings[i] * 2 == readings[i - 1] + readings[i + 1]`.
   This avoids floating-point work and removes ambiguity around exact equality.

3. Notice locality → no index depends on any other index’s result. That means every candidate position can be checked independently in a single pass.

4. Identify valid positions → only indices `1` through `n - 2` can qualify. The endpoints are excluded by definition.

5. Iterate once through the interior of the array, evaluate the condition, and increment a counter when it holds.

6. Stop there → no preprocessing, sorting, prefix structure, or auxiliary buffer helps because the rule is already constant-time per index.

This is the standard “single linear scan over local neighborhood constraints” pattern: when each decision uses fixed-width context, the optimal solution is usually one pass and a counter.

## 🧩 Algorithm Walkthrough
1. **Handle the structural boundary condition.**  
   If `n < 3`, return `0`. No element has both a left and right neighbor, so there are no valid candidate indices. This establishes the first invariant: all later checks operate only on legal positions.

2. **Use a single-pass linear scan.**  
   Iterate `i` from `1` to `n - 2`. This is the full set of indices that can possibly be stable. The pattern here is **Array Simulation / Linear Scan**: evaluate a local predicate at each position without storing intermediate state.

3. **Evaluate the stability predicate using integer arithmetic.**  
   For each `i`, check whether:
   `readings[i] * 2 == readings[i - 1] + readings[i + 1]`  
   This is correct because it is algebraically equivalent to “the current value equals the average of its neighbors,” but avoids division and floating-point precision concerns.

4. **Maintain a running count.**  
   If the predicate is true, increment `count`. The invariant after processing index `i` is: `count` equals the number of stable readings among indices `1..i`.

5. **Return the final count.**  
   After the scan completes, every eligible index has been examined exactly once, and no ineligible index has been considered. That gives both correctness and optimal asymptotic cost.

This abstraction is the right one because the problem is not about global structure; it is about repeated evaluation of a fixed local relation.

## 📊 Worked Example
Use `readings = [4, 6, 8, 7, 6]`.

| i | left | current | right | check                          | stable? | count |
|---|------|---------|-------|--------------------------------|---------|-------|
| 1 | 4    | 6       | 8     | `6 * 2 == 4 + 8` → `12 == 12`  | yes     | 1     |
| 2 | 6    | 8       | 7     | `8 * 2 == 6 + 7` → `16 == 13`  | no      | 1     |
| 3 | 8    | 7       | 6     | `7 * 2 == 8 + 6` → `14 == 14`  | yes     | 2     |

Trace:
1. Skip index `0` and `4` because endpoints lack two neighbors.
2. Check each interior index exactly once.
3. Stable positions are `1` and `3`.
4. Final answer: `2`.

The useful mental model is that each step consumes a width-3 window, but only the center index is being classified.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. The algorithm performs one pass over indices `1` through `n - 2`, and each iteration does constant-time arithmetic and comparison. At `10^6` elements this is trivial for modern systems; at `10^9`, linear cost is still large but remains the only viable asymptotic class for a full exact scan.

### Space Complexity
`O(1)`. The only extra state is a loop index and a counter. No auxiliary arrays, hash tables, or window buffers are required. This cannot be meaningfully reduced further unless the input is streamed, in which case the same constant-space property still holds.

## 💡 Key Takeaways
- If each decision depends only on a fixed-size local neighborhood, look for a single linear scan rather than preprocessing or nested loops.
- “Equals the average of neighbors” is a strong signal to rewrite the condition algebraically and check it per index.
- The valid scan range is `1` to `n - 2`; including endpoints is the most common off-by-one bug here.
- Prefer `2 * readings[i] == readings[i - 1] + readings[i + 1]` over division to avoid precision issues and accidental truncation semantics.
- In production data-validation paths, locality-aware rules are valuable because they enable streaming evaluation with predictable memory and throughput.

## 🚀 Variations & Further Practice
- Count indices where a reading equals the average of the previous `k` and next `k` readings; the twist is moving from fixed-width local checks to sliding-window sums.
- Return all maximal stable segments instead of just the count; the twist is shifting from point classification to interval construction.
- In a streaming system, emit stable readings online as records arrive; the twist is handling partial windows and delayed eligibility for the newest samples.