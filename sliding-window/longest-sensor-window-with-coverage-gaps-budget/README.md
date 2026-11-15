# Longest Sensor Window With Coverage Gaps Budget

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Prefix Sum

---

## 🗂 Problem Overview
Given a strictly increasing timestamp array `times` and a gap budget, find the longest contiguous subarray whose total missing seconds between adjacent reports does not exceed `budget`. The output is the maximum number of reports in such a window. The challenge is scale: `n` can reach `200000`, timestamps and budget can reach `10^18`, so any quadratic scan over subarrays is immediately disqualified.

## 🌍 Engineering Impact
This pattern shows up in telemetry backfills, observability dashboards, IoT fleet health scoring, CDC stream validation, and log-ingestion quality checks. You often need the longest “good enough” contiguous span under a bounded error or missing-data budget. At production scale, brute-force range evaluation collapses under high-cardinality streams and long retention windows. A linear sliding-window formulation lets systems compute continuity metrics online, with predictable memory and latency. That matters for alerting pipelines, SLO burn analysis, and UI queries where users expect near-real-time answers over millions of ordered events.

## 🔍 Problem Statement
You are given a strictly increasing array `times` of length `n`, where `times[i]` is the second at which the `i`-th sensor report arrived. For any contiguous window `times[l..r]`, define its coverage gap cost as the total number of missing seconds between consecutive reports inside that window:

`cost(l, r) = sum(times[i] - times[i-1] - 1)` for `i = l+1..r`.

Because `times` is strictly increasing, each term is non-negative. A single-report window has cost `0`.

Return the maximum window length whose total cost is at most `budget`.

**Constraints**
- `1 <= n <= 200000`
- `0 <= times[i] <= 10^18`
- `times` is strictly increasing
- `0 <= budget <= 10^18`

**Examples**
- `times = [2, 3, 7, 8, 10], budget = 3` → `4`
- `times = [1, 5, 6, 9, 15, 16], budget = 5` → `3`

The key constraint is that `n` is large enough that `O(n^2)` subarray evaluation is not viable.

## 🪜 How to Solve This
1. Read the cost definition → notice the window cost is not based on all values, only on **adjacent gaps** inside the window.

2. Rewrite the problem mentally: build an implicit array of edge costs  
   `gap[i] = times[i] - times[i-1] - 1` for `i >= 1`.  
   Then a window `times[l..r]` costs the sum of `gap[l+1..r]`.

3. Now the problem becomes: find the longest contiguous report window whose associated gap-sum is at most `budget`.

4. That is a classic **variable-size sliding window** signal: all gap costs are non-negative, so when the window cost exceeds budget, moving the left pointer right is the only way to decrease it.

5. Maintain two pointers `[left..right]` over reports and a running `currentCost` for the gaps inside that window.

6. Each time you extend `right`, add the newly introduced adjacent gap. If the budget is violated, advance `left` and subtract the gap that leaves the window.

7. Because both pointers move only forward, the whole scan is linear. No prefix-sum binary search is required, though prefix sums are a valid alternative framing.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `currentCost = 0`, `best = 1`. A single report is always valid, so the minimum answer is `1`.

2. **Expand the right boundary**  
   Iterate `right` from `0` to `n - 1`. When `right > 0`, the window gains one new internal edge:  
   `times[right] - times[right - 1] - 1`.  
   Add that to `currentCost`.  
   **Invariant:** `currentCost` equals the total missing seconds for `times[left..right]`.

3. **Shrink while invalid**  
   If `currentCost > budget`, move `left` rightward until the window becomes valid again.  
   When removing report `left`, subtract the edge between `left` and `left + 1`, namely  
   `times[left + 1] - times[left] - 1`, because that edge is no longer inside the window.  
   **Invariant:** after shrinking, `currentCost <= budget`.

4. **Record the best valid window**  
   Once the window is valid, update  
   `best = max(best, right - left + 1)`.

5. **Why this is correct**  
   This is the standard **Two Pointers / Sliding Window** pattern over non-negative costs. Expanding right can only increase or preserve cost; shrinking left can only decrease or preserve it. Therefore, for each `right`, the smallest feasible `left` is found monotonically, and no candidate window is skipped.

6. **Why this abstraction fits**  
   The problem asks for the longest contiguous range under an additive budget with non-negative increments. That is exactly the regime where sliding windows dominate: linear scan, constant extra state, and no backtracking.

## 📊 Worked Example
Use `times = [2, 3, 7, 8, 10]`, `budget = 3`.

| right | added gap | currentCost before shrink | left after shrink | valid window     | length | best |
|------:|----------:|--------------------------:|------------------:|------------------|-------:|-----:|
| 0     | 0         | 0                         | 0                 | `[2]`            | 1      | 1    |
| 1     | 0         | 0                         | 0                 | `[2, 3]`         | 2      | 2    |
| 2     | 3         | 3                         | 0                 | `[2, 3, 7]`      | 3      | 3    |
| 3     | 0         | 3                         | 0                 | `[2, 3, 7, 8]`   | 4      | 4    |
| 4     | 1         | 4                         | 1                 | `[3, 7, 8, 10]`  | 4      | 4    |

At `right = 4`, adding gap `10 - 8 - 1 = 1` pushes cost to `4`, over budget. Shrink from the left: removing report `2` removes edge cost `3 - 2 - 1 = 0`, still `4`; removing report `3` would remove edge `7 - 3 - 1 = 3`, but after the first left shift the window is `[3,7,8,10]` with cost `4`. Correctly, the implemented shrink subtracts the leaving internal edge each step, yielding a valid max length of `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each report enters the window once via `right` and leaves at most once via `left`. The dominant work is pointer movement and constant-time arithmetic on adjacent gaps. At `10^6` elements this remains practical; at `10^9`, linear time becomes throughput-bound but is still asymptotically optimal for a full scan.

### Space Complexity
`O(1)` auxiliary space for the sliding-window version. The state is just pointers, the running cost, and the best length. A prefix-sum formulation would use `O(n)` extra memory, which can simplify some reasoning but is unnecessary here.

## 💡 Key Takeaways
- If the constraint is “longest contiguous range with sum/cost ≤ budget” and each incremental contribution is non-negative, think sliding window before anything heavier.
- When the cost is defined over adjacent pairs, convert the problem mentally from node values to edge weights; that usually exposes the right invariant.
- The window cost is over gaps **inside** `times[l..r]`, so adding `right` contributes only the edge `(right-1, right)` and removing `left` subtracts only `(left, left+1)`.
- Use 64-bit or wider integer arithmetic throughout; `times[i]` and `budget` can be `10^18`, so intermediate gap sums will overflow 32-bit types immediately.
- In production data pipelines, many “continuity” metrics reduce to bounded additive defects over ordered events; recognizing that lets you replace expensive range recomputation with online linear scans.

## 🚀 Variations & Further Practice
- Allow up to `k` disjoint windows instead of one longest window. The twist is global budget allocation across segments, which typically pushes the problem toward DP plus prefix sums.
- Make the timestamps mutable with online updates and range queries. The twist is dynamic maintenance of adjacent-gap sums, which suggests Fenwick trees or segment trees rather than a one-pass window.
- Replace additive gap cost with the maximum single gap inside the window. The twist is that the aggregation is no longer a sum, so you need a monotonic deque instead of a plain running total.