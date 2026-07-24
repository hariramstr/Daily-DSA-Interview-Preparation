# Shortest Market Span Covering All Ad Campaigns

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Frequency Counting

---

## 🗂 Problem Overview
Given a chronological array `visits` of campaign IDs and an array `required` describing minimum counts per campaign, find the length of the shortest contiguous window whose campaign frequencies satisfy every requirement. Return `-1` if no such window exists. The challenge is that requirements are multi-dimensional: campaigns may need repeated occurrences, some need none, and `visits.length` can reach `200000`, ruling out brute-force enumeration of subarrays.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the smallest interval satisfying a multi-key quota. Examples include streaming analytics windows that must contain enough events from each source, ad attribution spans covering required campaign impressions, log pipelines searching for the minimal incident slice containing all signal types, and search/ranking systems enforcing diversity constraints over contiguous result segments. At scale, naive rescans or per-start recomputation collapse under throughput and latency budgets. A correct sliding-window design turns an otherwise quadratic scan into a single-pass, cache-friendly routine that can run inline in hot paths or streaming consumers.

## 🔍 Problem Statement
You are given:

- `visits`, where `visits[i]` is the campaign ID influencing the `i`-th visit
- `required`, where `required[c]` is the minimum number of times campaign `c` must appear in a valid window

Campaign IDs are in `[0, m - 1]`, where `m = required.length`. You must return the minimum length of a contiguous subarray whose frequency counts satisfy `windowCount[c] >= required[c]` for every campaign `c`. If even the full array cannot satisfy all requirements, return `-1`.

Constraints:

- `1 <= visits.length <= 200000`
- `1 <= m <= 100000`
- `0 <= visits[i] < m`
- `0 <= required[c] <= visits.length`

Examples:

- `visits = [2,0,1,2,0,1,2,1]`, `required = [1,2,2]` → `5`
- `visits = [3,1,3,2,1,0,2,3]`, `required = [1,1,2,1]` → `6`

The key constraint is input size: checking every subarray is infeasible, so the solution must be near-linear.

## 🪜 How to Solve This
1. Start by reframing the problem: we do **not** need all valid windows, only the shortest one. That usually suggests trying to grow a window until it becomes valid, then shrink it aggressively.

2. Notice validity depends only on frequency counts inside a contiguous range. That is a classic **sliding window / two pointers** signal: maintain counts incrementally instead of recomputing them for every subarray.

3. Track, for each campaign, how many times it appears in the current window. But checking all `m` campaigns after every move would be too expensive.

4. Compress validity into one scalar: how many campaigns currently meet their required count. If a campaign transitions from `required[c]-1` to `required[c]`, mark it satisfied. If shrinking drops it below requirement, mark it unsatisfied.

5. Expand the right pointer until all required campaigns are satisfied. Then shrink the left pointer while the window remains valid, updating the best length each time.

6. Because each pointer only moves forward, the full scan is linear in `visits.length`, which is exactly what the constraints demand.

## 🧩 Algorithm Walkthrough
1. **Precompute feasibility and target dimension.**  
   Count total occurrences of each campaign in `visits`. If any `required[c]` exceeds the total available count, return `-1` immediately. Also count how many campaigns actually matter: `need = number of c where required[c] > 0`. This avoids treating zero-requirement campaigns as constraints.

2. **Initialize the sliding window state.**  
   Use two pointers `left = 0`, `right = 0`, an array `windowCount[m]`, and an integer `formed = 0`. `formed` means how many required campaigns are currently satisfied. This is the key invariant:  
   `formed == need` iff the current window is valid.

3. **Expand with the right pointer.**  
   For each `visits[right] = c`, increment `windowCount[c]`. If `required[c] > 0` and `windowCount[c]` just became exactly `required[c]`, increment `formed`. This captures the moment campaign `c` becomes satisfied.

4. **Shrink with the left pointer while valid.**  
   When `formed == need`, the window covers all requirements. Record its length. Then remove `visits[left] = d` and advance `left`. If `required[d] > 0` and `windowCount[d]` falls from `required[d]` to `required[d]-1`, decrement `formed`. This preserves correctness: once `formed < need`, the window is no longer valid.

5. **Repeat until the scan ends.**  
   This is the standard **Two Pointers / Sliding Window** pattern: right expands to gain feasibility, left contracts to restore minimality. Each element enters and leaves the window at most once, which is why the algorithm is linear.

## 📊 Worked Example
Example: `visits = [2,0,1,2,0,1,2,1]`, `required = [1,2,2]`

Need campaigns: `0,1,2`, so `need = 3`.

| Step | right | added | window counts (0,1,2) | formed | action |
|---|---:|---:|---|---:|---|
| 1 | 0 | 2 | (0,0,1) | 0 | expand |
| 2 | 1 | 0 | (1,0,1) | 1 | expand |
| 3 | 2 | 1 | (1,1,1) | 1 | expand |
| 4 | 3 | 2 | (1,1,2) | 2 | expand |
| 5 | 4 | 0 | (2,1,2) | 2 | expand |
| 6 | 5 | 1 | (2,2,2) | 3 | valid, length 6 |
| 7 | shrink left=0 remove 2 | (2,2,1) | 2 | invalid |
| 8 | 6 | 2 | (2,2,2) | 3 | valid, length 6 |
| 9 | shrink left=1 remove 0 | (1,2,2) | 3 | valid, length 5 |
| 10 | shrink left=2 remove 1 | (1,1,2) | 2 | stop shrinking |

Best length found: `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = visits.length` and `m = required.length`. We do one feasibility pass, then a single sliding-window scan where each pointer advances at most `n` times. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally better than any quadratic alternative.

### Space Complexity
`O(m)` for the frequency arrays storing total counts and current window counts. That space is owned by campaign-indexed counting structures. It can be reduced with sparse hash maps when campaign usage is sparse, but that trades predictable array access for higher constant factors.

## 💡 Key Takeaways
- If the problem asks for the **shortest contiguous range** satisfying frequency thresholds, think sliding window before considering prefix-sum or brute-force approaches.
- When validity is “all categories meet minimum counts,” collapse the multidimensional check into a single `formed vs need` invariant.
- Campaigns with `required[c] == 0` must not inflate the satisfaction target, or the window-validity logic becomes subtly wrong.
- Update `formed` only on threshold crossings: exactly when a count reaches `required[c]`, and exactly when it drops below during shrink.
- The production lesson is to replace repeated global validation with incremental state transitions; that is the difference between quadratic rescans and streaming-grade throughput.

## 🚀 Variations & Further Practice
- Return the actual window indices, and break ties by earliest start or lexicographically smallest span; the twist is preserving deterministic selection while shrinking.
- Support online updates where `required` changes between queries; the harder part is that the satisfaction invariant is no longer fixed across scans.
- Find the shortest window covering a multiset when campaign IDs are large or sparse strings instead of dense integers; the conceptual twist is swapping array counting for hash-based frequency tracking.