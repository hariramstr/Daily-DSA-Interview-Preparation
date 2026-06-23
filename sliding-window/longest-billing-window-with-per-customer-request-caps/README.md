# Longest Billing Window With Per-Customer Request Caps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given a chronologically ordered array `requests`, where each entry is a customer ID, find the longest contiguous window in which no customer appears more than `limit` times. Return only that maximum window length. The difficulty is not the rule itself but the scale: up to 200,000 requests, large customer ID values, and a requirement for linear-time processing. That rules out recomputing frequencies for each candidate window and points directly to dynamic frequency tracking.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to enforce or analyze bounded activity over contiguous event streams: API gateway rate-limit auditing, tenant-isolation checks in shared infrastructure, fraud detection over transaction sequences, and streaming observability pipelines that detect burst concentration by principal. At scale, brute-force window validation collapses under cardinality and throughput. A sliding-window design with incremental state turns an otherwise quadratic scan into a single pass, which is the difference between offline batch pain and real-time operability. The same idea underpins efficient admission control, fairness enforcement, and bounded-resource scheduling in multi-tenant systems.

## 🔍 Problem Statement
You are given an integer array `requests` in chronological order, where `requests[i]` is the customer ID for the `i`-th API request, and an integer `limit`. A contiguous window `[left, right]` is valid if every customer ID appears at most `limit` times within that window. Return the maximum possible window length.

Constraints:

- `1 <= requests.length <= 200000`
- `1 <= requests[i] <= 1000000000`
- `1 <= limit <= requests.length`

Examples:

- `requests = [4, 7, 4, 2, 7, 4, 7], limit = 2` → `5`
- `requests = [9, 9, 9, 3, 3, 8, 8, 8, 8], limit = 2` → `4`

Edge cases matter: all IDs may be distinct, all may be identical, and IDs are too large for array-indexed counting. The decisive constraint is the input size: any approach that rescans windows or rebuilds counts repeatedly will be too slow. The target is `O(n)` time with dynamic frequency maintenance.

## 🪜 How to Solve This
1. Read the requirement carefully → we need the **longest contiguous subarray** satisfying a per-value frequency cap. That is a classic sliding-window signal.

2. Ask what makes a window invalid → only one thing: after adding `requests[right]`, that customer’s count may become `limit + 1`.

3. That suggests incremental bookkeeping → maintain a hash map `count[id]` for the current window, expand the right boundary one step at a time, and update only the affected customer count.

4. When the window becomes invalid, do not restart or recompute → shrink from the left until the violating customer is back within the cap. Because only one new element was added, repeated left moves are enough to restore validity.

5. Track the best window length after each valid expansion. Both pointers move only forward, so total work stays linear.

6. Why this is the right mental model: the problem is not “find all frequencies” but “maintain a valid interval under local updates.” That is exactly what sliding window plus a hash map is built for.

## 🧩 Algorithm Walkthrough
1. **Initialize the sliding window state**  
   Use the **Two Pointers / Sliding Window** pattern. Set `left = 0`, `best = 0`, and create a hash map `freq` from customer ID to count in the current window. The invariant we want is: after any repair step, the window `[left, right]` is valid.

2. **Expand the window with `right`**  
   Iterate `right` from `0` to `n - 1`. For each `requests[right]`, increment `freq[requests[right]]`. This is the only count that changed, so only this customer can have broken the constraint.

3. **Repair invalidity by moving `left`**  
   While `freq[requests[right]] > limit`, decrement `freq[requests[left]]` and advance `left`. This works because removing elements from the left monotonically reduces counts in the window. Once the violating customer’s count is back to `limit`, the whole window is valid again.

4. **Record the best valid window**  
   After repair, compute `right - left + 1` and update `best`. This is correct because every window considered at this point satisfies the cap for all customers.

5. **Why the invariant holds**  
   Before adding `right`, the window was valid. Adding one element can only increase one frequency by one. Shrinking until that single over-limit count is fixed restores global validity. No other customer count can increase during shrinking.

6. **Why this abstraction is right**  
   The problem asks for an optimal contiguous range under a constraint that can be updated incrementally. That is the defining use case for sliding windows: local state updates, monotonic pointer movement, and no repeated full-window scans.

## 📊 Worked Example
Example: `requests = [4, 7, 4, 2, 7, 4, 7]`, `limit = 2`

| right | requests[right] | action | left | freq after repair | window | best |
|---|---:|---|---:|---|---|---:|
| 0 | 4 | add 4 | 0 | {4:1} | [4] | 1 |
| 1 | 7 | add 7 | 0 | {4:1,7:1} | [4,7] | 2 |
| 2 | 4 | add 4 | 0 | {4:2,7:1} | [4,7,4] | 3 |
| 3 | 2 | add 2 | 0 | {4:2,7:1,2:1} | [4,7,4,2] | 4 |
| 4 | 7 | add 7 | 0 | {4:2,7:2,2:1} | [4,7,4,2,7] | 5 |
| 5 | 4 | add 4, invalid | 1 | {4:2,7:2,2:1} | [7,4,2,7,4] | 5 |
| 6 | 7 | add 7, invalid | 2 | {4:2,7:2,2:1} | [4,2,7,4,7] | 5 |

At `right = 5`, customer `4` reaches count `3`, so we move `left` once and drop the first `4`. At `right = 6`, customer `7` reaches count `3`, so we drop the first `7`. The maximum valid length remains `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each request is added to the window once by advancing `right`, and removed at most once by advancing `left`. Hash map updates are `O(1)` average-case, so the dominant cost is a single linear pass. At `10^6` elements this is routine; at `10^9`, the algorithm is still asymptotically right but memory bandwidth and runtime become the practical limits.

### Space Complexity
`O(k)`, where `k` is the number of distinct customer IDs present in the current window, bounded by `n`. The hash map owns the space. It cannot be meaningfully reduced without sacrificing constant-time frequency updates; replacing it with rescans would destroy the linear-time guarantee.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** region under a dynamically checkable constraint, sliding window should be your first candidate.
- If validity depends on per-value counts and IDs are sparse or large, pair the window with a hash map rather than sorting or array indexing.
- Update `best` only **after** repairing the window; doing it before the shrink step records invalid ranges.
- The shrink condition is tied to the newly added value: use `while freq[current] > limit`, not a single `if`, or repeated duplicates will break correctness.
- In production systems, incremental state maintenance is the difference between stream-time enforcement and prohibitively expensive retrospective recomputation.

## 🚀 Variations & Further Practice
- Allow each customer to have its own cap, e.g. `limitByCustomer[id]`; same pattern, but validity now depends on per-key thresholds rather than one global limit.
- Find the longest window where **at most `m` customers** violate the cap; this adds a second layer of state and turns a single-condition repair loop into multi-constraint window management.
- Process an unbounded stream with evictions by timestamp instead of index contiguity; same counting idea, but now the window is time-based and must expire old events online.