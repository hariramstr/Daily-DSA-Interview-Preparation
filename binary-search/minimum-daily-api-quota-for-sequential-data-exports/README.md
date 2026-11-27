# Minimum Daily API Quota for Sequential Data Exports

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array `requests`, where each value is the API cost to export one client in order, and an integer `d`, compute the smallest daily quota that lets all exports finish in at most `d` days. Each day must process a contiguous sequence of clients, and no client can be split across days. The difficulty is that preserving order rules out rebalancing arbitrarily, so the problem becomes finding the minimum feasible capacity under sequential packing constraints.

## 🌍 Engineering Impact
This pattern shows up anywhere work must be processed in-order under bounded per-window capacity: batch export systems, Kafka consumer catch-up windows, ETL partition sizing, log compaction passes, CI job sharding with ordering constraints, and cloud rate-limited backfills. At scale, naive trial of every possible quota is infeasible because the search space can span billions. What matters architecturally is recognizing monotonic feasibility: once a quota works, any larger quota also works. That turns a potentially huge optimization space into a logarithmic search over capacities, enabling predictable planning and admission control without simulating every configuration.

## 🔍 Problem Statement
You are given:

- `requests[i]`: API calls required to complete the `i`-th client export
- `d`: maximum number of days allowed

You must process clients in the given order. Each day handles one contiguous block of clients, and a single client cannot be split across multiple days. Return the minimum daily API quota that allows all exports to complete within at most `d` days.

Constraints:

- `1 <= requests.length <= 100000`
- `1 <= requests[i] <= 1000000000`
- `1 <= d <= requests.length`

Examples:

```text
Input:  requests = [7, 2, 5, 10, 8], d = 2
Output: 18
Explanation: [7, 2, 5] | [10, 8]
```

```text
Input:  requests = [4, 4, 4, 4, 4], d = 3
Output: 8
Explanation: [4, 4] | [4, 4] | [4]
```

The key constraint is the value range: quotas may lie between `max(requests)` and `sum(requests)`, which can be very large, so linear search over capacities is not viable.

## 🪜 How to Solve This
1. Read the constraints carefully → exports must stay in order, and each day is a contiguous block. That means this is not arbitrary partitioning; it is sequential packing.

2. Ask the decision question first → if someone gives you a daily quota `Q`, can you determine whether all clients fit within `d` days? Yes: scan left to right, greedily filling the current day until adding the next client would exceed `Q`, then start a new day.

3. Notice the monotonic property → if quota `Q` works, any quota larger than `Q` also works. If `Q` fails, any smaller quota also fails. That is the signal for binary search on the answer.

4. Establish the search bounds:
   - Lower bound = `max(requests)` because one client cannot be split.
   - Upper bound = `sum(requests)` because one day can process everything.

5. Binary search that range → for each midpoint, run the feasibility check. If it fits in `d` days, try smaller; otherwise try larger.

6. The result is the smallest feasible quota, not just any working quota. That distinction drives the `left/right` update logic.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Binary Search on Answer + Greedy Feasibility Check.**  
   We are not searching an index in a sorted array; we are searching a numeric answer space with a monotonic predicate: “can all exports finish within `d` days using quota `Q`?”

2. **Set the lower bound to the largest single request.**  
   Any quota below `max(requests)` is impossible because one client cannot be split. This invariant guarantees every candidate quota is at least individually valid for each item.

3. **Set the upper bound to the total sum of requests.**  
   With quota equal to `sum(requests)`, everything fits in one day. This guarantees the search interval contains at least one feasible answer.

4. **For a candidate quota `mid`, simulate the schedule greedily.**  
   Traverse `requests` left to right, maintaining the current day’s accumulated load. If adding the next request exceeds `mid`, start a new day and place that request there. This is correct because delaying a split never helps; packing each day as much as possible minimizes the number of days used for that quota.

5. **Count days and evaluate feasibility.**  
   If the greedy pass uses `<= d` days, `mid` is feasible, so search the smaller half to find a tighter minimum. Otherwise, `mid` is too small, so search the larger half.

6. **Terminate when bounds converge.**  
   When `left == right`, all smaller quotas have been ruled out and this value is the minimum feasible quota. The invariant maintained throughout is: infeasible answers lie left of the boundary, feasible answers lie at or to the right.

## 📊 Worked Example
Example: `requests = [7, 2, 5, 10, 8]`, `d = 2`

Initial bounds:

- `left = max = 10`
- `right = sum = 32`

| mid | Greedy grouping under `mid` | days used | feasible? |
|---|---|---:|---|
| 21 | `[7,2,5] [10,8]` | 2 | yes |
| 15 | `[7,2,5] [10] [8]` | 3 | no |
| 18 | `[7,2,5] [10,8]` | 2 | yes |
| 16 | `[7,2,5] [10] [8]` | 3 | no |
| 17 | `[7,2,5] [10] [8]` | 3 | no |

Trace:

1. `21` works, so shrink right to `21`.
2. `15` fails, so raise left to `16`.
3. `18` works, so shrink right to `18`.
4. `16` fails, so raise left to `17`.
5. `17` fails, so raise left to `18`.

Now `left == right == 18`, so the minimum daily quota is `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n` is `requests.length` and `S = sum(requests) - max(requests) + 1` is the answer range. Each binary-search step scans the array once. In practice this scales well even when request values approach `10^9`, because the logarithmic factor stays small while the dominant cost remains sequential array passes.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few counters and running totals; no extra arrays or heaps are required. Space cannot meaningfully be reduced further, though using wider integer types may be necessary to avoid overflow when summing large inputs.

## 💡 Key Takeaways
- If the problem asks for the **minimum capacity / rate / quota** and feasibility for a candidate value is easy to test, suspect binary search on the answer.
- If work must remain **in original order** and be split into contiguous groups, a greedy left-to-right packing check is often the right feasibility predicate.
- The lower bound is not `0` or `min(requests)`; it must be `max(requests)` because a single client cannot span days.
- Use 64-bit arithmetic for `sum`, `mid`, and running totals; with `100000` elements up to `10^9`, 32-bit integers overflow.
- In production systems, monotonic feasibility is a powerful optimization lever: once identified, it converts expensive configuration search into deterministic logarithmic planning.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum** — same core pattern, but framed as minimizing the largest subarray sum across exactly `k` partitions; the conceptual twist is recognizing it as the same contiguous-capacity problem.
- **Capacity To Ship Packages Within D Days** — operationally identical, but useful for reinforcing the binary-search-on-answer template under shipping semantics.
- **Allocate Minimum Number of Pages** — similar partitioning problem where each student gets contiguous books; the harder part is spotting that “contiguous assignment” preserves the same monotonic feasibility structure.