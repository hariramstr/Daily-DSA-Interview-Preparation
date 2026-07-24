# Shortest API Trace Covering Endpoint Quotas

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Table, Sliding Window, Frequency Counting

---

## 🗂 Problem Overview
Given an API trace `trace` and a quota list `(endpoint, count)`, find the length of the shortest contiguous window whose endpoint frequencies meet or exceed every required quota. Duplicate quota entries for the same endpoint must be merged before evaluation. The challenge is scale: both arrays can reach `2 * 10^5`, endpoint names are arbitrary strings, and any solution that rescans windows or uses nested loops will not survive production-sized logs.

## 🌍 Engineering Impact
This pattern shows up in incident reconstruction, security forensics, streaming observability, ad-event attribution, and distributed workflow debugging. You often need the smallest time-local segment that proves a condition: enough retries, enough auth failures, enough calls to a payment path, enough symbol occurrences in a compiler pass, enough feature hits in ranking logs. Without a linear-time sliding window plus hashing, teams fall back to brute-force scans, pre-aggregation that loses locality, or expensive secondary indexes. The efficient formulation preserves contiguity, handles arbitrary string keys, and scales to high-cardinality event streams without exploding latency or memory.

## 🔍 Problem Statement
You are given:

- `trace`: an array of endpoint names, where `trace[i]` is the endpoint called at time `i`
- `quotas`: a list of pairs `[endpoint, count]`

First combine duplicate quota entries by summing counts per endpoint. Then return the length of the shortest contiguous subarray of `trace` whose frequency map covers every required endpoint count. If no such subarray exists, return `-1`.

A window `[l, r]` is valid iff for every required endpoint `x`, the count of `x` inside the window is at least `need[x]`.

Constraints:

- `1 <= trace.length <= 2 * 10^5`
- `1 <= quotas.length <= 2 * 10^5`
- Endpoint names are arbitrary non-empty strings
- Total input string size is bounded, but key comparison and hashing still matter
- Duplicate quota names must be merged

Examples:

- `trace = ["/login","/feed","/cart","/login","/feed","/pay"]`
- `quotas = [["/login",2],["/feed",1],["/pay",1]]`
- Output: `6`

- `trace = ["a","x","b","a","c","b","a"]`
- `quotas = [["a",2],["b",1]]`
- Output: `4`

The key algorithmic driver is the `2 * 10^5` scale: quadratic window checking is not viable.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “contains all distinct endpoints,” it is “contains each endpoint enough times.” That immediately suggests frequency counting, not set membership.

2. Duplicate quota entries can exist → normalize first into a single `need` hash map. If you skip this, correctness becomes fragile and the matching logic gets messy.

3. We need the **shortest contiguous** window → that is the classic signal for a sliding window / two-pointer approach. Sorting is impossible because order defines contiguity.

4. As the right pointer expands, maintain `window[endpoint]`. But checking all quotas after every move would be too expensive. Instead, track how many required endpoints are currently satisfied exactly or beyond quota.

5. Once all required endpoints are satisfied, try shrinking from the left while preserving validity. That greedy contraction is what gives the minimum for each right boundary.

6. Because each pointer only moves forward, the whole scan is near-linear. Hash maps handle arbitrary endpoint strings and high cardinality cleanly.

That chain gets you from raw problem statement to the right abstraction without guessing.

## 🧩 Algorithm Walkthrough
1. **Build the requirement map (`need`) using a hash table.**  
   For each `[endpoint, count]` in `quotas`, accumulate into `need[endpoint] += count`. This is required because quotas may repeat. Invariant: `need` contains the exact minimum frequency required for each endpoint.

2. **Initialize the sliding window state.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`, an empty `window` frequency map, `formed = 0`, and `required = need.size()`. Here, `formed` means how many distinct required endpoints currently meet quota. Invariant: `formed == required` iff the current window is valid.

3. **Expand the right pointer across `trace`.**  
   For each `trace[right]`, if the endpoint is required, increment `window[endpoint]`. If its count just reached `need[endpoint]`, increment `formed`. This avoids rescanning all keys. Invariant: `formed` changes only when a requirement boundary is crossed.

4. **Shrink greedily while the window remains valid.**  
   While `formed == required`, update the best answer with `right - left + 1`, then attempt to remove `trace[left]`. If it is required, decrement its count; if it drops below quota, decrement `formed`. Then advance `left`. This guarantees the smallest valid window for the current `right`.

5. **Return the result.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum length recorded.

Why this is correct: expansion is necessary to satisfy missing quotas; contraction is safe only while validity holds. Since both pointers move monotonically, every element is processed O(1) times amortized.

## 📊 Worked Example
Use `trace = ["a","x","b","a","c","b","a"]`, `quotas = [["a",2],["b",1]]`.

`need = {a:2, b:1}`, `required = 2`

| right | trace[right] | window after add | formed | action |
|---|---|---:|---:|---|
| 0 | a | {a:1} | 0 | not valid |
| 1 | x | {a:1} | 0 | ignore non-required |
| 2 | b | {a:1,b:1} | 1 | `b` satisfied |
| 3 | a | {a:2,b:1} | 2 | valid, answer = 4 |
|   |   | remove `a` at left=0 → {a:1,b:1} | 1 | stop shrinking |
| 4 | c | {a:1,b:1} | 1 | not valid |
| 5 | b | {a:1,b:2} | 1 | extra `b` does not change formed |
| 6 | a | {a:2,b:2} | 2 | valid again |
|   |   | shrink from left=1 (`x`) → still valid | 2 | answer stays 4 |
|   |   | shrink from left=2 (`b`) → {a:2,b:1} | 2 | window length still 4 |
|   |   | shrink from left=3 (`a`) → {a:1,b:1} | 1 | stop |

Minimum length found: `4`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n + q)` expected, where `n = trace.length` and `q = quotas.length`, assuming hash-table operations are O(1) average-case. Each pointer advances at most `n` times, and quota normalization scans `q` entries once. At `10^6` scale this is practical; at `10^9`, throughput and memory locality become the real bottlenecks, not asymptotics.

### Space Complexity
`O(k)`, where `k` is the number of distinct required endpoints. The dominant space is the `need` map plus the active `window` counts. You can reduce constants by storing counts only for required endpoints, but not the asymptotic bound without sacrificing direct lookup efficiency.

## 💡 Key Takeaways
- If the problem asks for the shortest contiguous segment satisfying frequency thresholds, think sliding window plus hash-counts, not sorting or prefix scans alone.
- If requirements are keyed by arbitrary strings and duplicates must be merged, that is a strong signal for a hash map normalization pass before the main algorithm.
- Only increment `formed` when a count reaches the quota exactly; extra occurrences must not inflate satisfaction state.
- When shrinking, update the answer **before** removing `trace[left]`, or you will miss the current valid window by one step.
- The production lesson is to track only state transitions that matter to correctness; boundary-crossing counters scale better than recomputing full validity on every event.

## 🚀 Variations & Further Practice
- Return the actual window indices or subarray, not just the length. Same core algorithm, but you must preserve best boundaries and handle tie-breaking rules explicitly.
- Add per-endpoint maximum quotas as well as minimum quotas. This turns the window into a dual-constraint system where expansion and contraction interact less monotonically.
- Process an online stream with append-only events and repeated queries. The harder twist is amortizing state across queries or using indexed summaries without losing contiguity guarantees.