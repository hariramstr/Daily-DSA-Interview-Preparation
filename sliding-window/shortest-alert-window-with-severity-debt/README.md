# Shortest Alert Window With Severity Debt

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding-window, hash-map, multiset

---

## 🗂 Problem Overview
Given `alerts[i] = [serviceId, severity]`, find the minimum-length contiguous window containing alerts from at least `m` distinct services whose severity debt is within `budget`. For a window, let `peak` be its maximum severity, and for each service use only that service’s maximum severity inside the window; debt is the sum of `peak - serviceMax` over distinct services. The difficulty is that both `peak` and per-service maxima change dynamically as the window expands and shrinks.

## 🌍 Engineering Impact
This pattern shows up in streaming observability, fraud detection, SIEM correlation, and multi-tenant SLO enforcement: you need the smallest time slice that is both broad enough across entities and internally consistent under a budgeted score. At scale, recomputing maxima and aggregate penalties per window is prohibitive. The right design combines incremental state, ordered structures, and shrink-when-valid semantics. Without that, systems fall back to quadratic scans, high tail latency, or coarse batch approximations. With it, you can support online alert triage, bounded-memory stream processors, and low-latency policy evaluation over millions of events.

## 🔍 Problem Statement
You are given an array `alerts` of length `n`, where each element is `[serviceId, severity]`. Return the length of the shortest contiguous window such that:

1. The window contains alerts from at least `m` distinct services.
2. If `peak` is the maximum severity in the window, and `serviceMax[s]` is the maximum severity for service `s` within that window, then:

`debt = Σ (peak - serviceMax[s])` over all distinct services in the window

The window is valid iff `debt <= budget`.

If no window satisfies both conditions, return `-1`.

Constraints:

- `1 <= n <= 2 * 10^5`
- `1 <= serviceId <= 2 * 10^5`
- `1 <= severity <= 10^9`
- `1 <= m <= n`
- `0 <= budget <= 10^14`

Examples:

- `alerts = [[1,4],[2,2],[1,6],[3,5]], m = 3, budget = 5` → `4`
- `alerts = [[1,8],[2,7],[2,3],[3,8],[1,5]], m = 3, budget = 1` → `4`

The key constraint is `n = 2e5`: any solution that recomputes window maxima or per-service maxima from scratch during contraction will time out.

## 🪜 How to Solve This
1. Start with the obvious requirement: shortest **contiguous** subarray → think sliding window / two pointers.

2. Then inspect validity. We need:
   - number of distinct services in the window
   - global maximum severity in the window
   - per-service maximum severities
   - aggregate debt derived from those values

3. A naive window check is too expensive because shrinking the left edge can invalidate a service’s maximum or even the global maximum. That means we need data structures that support incremental insert/remove and fast max queries.

4. Rewrite the debt formula:
   - `debt = distinctCount * peak - sum(serviceMax over services)`
   This is the key simplification. Instead of summing gaps every time, maintain:
   - `distinctCount`
   - `peak`
   - `sumServiceMax`

5. For each service, maintain a multiset of severities currently in the window so its current maximum is available after both insertions and deletions.

6. Also maintain a global multiset of all severities in the window to get `peak`.

7. Expand `right`, update all structures, and once the window is valid, repeatedly advance `left` to minimize length while preserving validity.

That gives the standard shortest-valid-window pattern, but with richer state maintenance.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers / Sliding Window pattern.**  
   Maintain a window `[left, right]` and grow `right` from `0` to `n-1`. This is the correct abstraction because the target is the shortest contiguous segment satisfying a monotone-enough validity condition under incremental updates.

2. **Track per-service membership and maxima.**  
   For each `serviceId`, keep a multiset of severities currently inside the window. Its maximum is that service’s contribution to `sumServiceMax`.  
   **Invariant:** for every service present in the window, the stored max equals the true maximum among its in-window alerts.

3. **Maintain `distinctCount` and `sumServiceMax`.**  
   On insertion/removal of an alert, if a service’s maximum changes, subtract the old max and add the new one. If a service enters or leaves the window entirely, update `distinctCount`.  
   **Invariant:** `sumServiceMax` is always the sum of current per-service maxima.

4. **Track the global peak with another ordered multiset.**  
   Insert every severity when expanding and erase it when shrinking. The largest key is `peak`.  
   **Invariant:** `peak` is the maximum severity over all alerts currently in the window.

5. **Compute validity in O(1) from maintained aggregates.**  
   The window is valid iff:
   - `distinctCount >= m`
   - `distinctCount * peak - sumServiceMax <= budget`  
   This is correct by algebraic equivalence to the original debt definition.

6. **Shrink greedily while valid.**  
   Once valid, record `right - left + 1`, then remove `alerts[left]` and advance `left`. Continue until the window becomes invalid. This preserves the standard minimality invariant: for each fixed `right`, you test the shortest valid window ending at `right`.

7. **Return the best length or `-1`.**  
   Every alert enters once and leaves once; the cost is dominated by ordered multiset operations.

## 📊 Worked Example
Use `alerts = [[1,8],[2,7],[2,3],[3,8],[1,5]], m = 3, budget = 1`.

| Step | Window | Distinct | Peak | Service Maxima | SumMax | Debt | Valid |
|---|---|---:|---:|---|---:|---:|---|
| r=0 | `[1,8]` | 1 | 8 | `{1:8}` | 8 | 0 | No |
| r=1 | `[1,8],[2,7]` | 2 | 8 | `{1:8,2:7}` | 15 | 1 | No |
| r=2 | `...,[2,3]` | 2 | 8 | `{1:8,2:7}` | 15 | 1 | No |
| r=3 | `...,[3,8]` | 3 | 8 | `{1:8,2:7,3:8}` | 23 | 1 | Yes |
| shrink | remove `[1,8]` | 2 | 8 | `{2:7,3:8}` | 15 | 1 | No |
| r=4 | `[2,7],[2,3],[3,8],[1,5]` | 3 | 8 | `{1:5,2:7,3:8}` | 20 | 4 | No |

The first valid window is indices `[0..3]`, length `4`. After removing the leftmost alert, service `1` disappears, so distinct count drops below `m`. Extending to `r=4` restores three services, but the debt rises above budget. Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` in the general ordered-multiset implementation. Each alert is inserted once and removed once from the global multiset and one per-service multiset; each such operation is `O(log n)`. At `10^6` elements this is still practical in optimized languages; at `10^9`, you need distributed streaming or approximation, not in-memory exact evaluation.

### Space Complexity
`O(n)` in the worst case. Space is owned by the global severity multiset plus all per-service multisets storing the alerts currently in the window. You can reduce constants with specialized heaps plus lazy deletion, but not the asymptotic bound without giving up exact deletions.

## 💡 Key Takeaways
- If the problem asks for the shortest contiguous segment and validity can be maintained incrementally, default to two pointers before considering heavier machinery.
- When a window score depends on a global extreme and grouped per-key extremes, look for an algebraic rewrite into maintained aggregates like `count * peak - sumOfGroupMax`.
- The main trap is left-edge removal: deleting one alert can change both a service-local maximum and the global maximum, so plain counters are insufficient.
- Be careful to update `sumServiceMax` using the old service maximum before mutating that service’s multiset, then add back the new maximum if the service remains present.
- The transferable design insight is to convert expensive recomputation into local state transitions with explicit invariants; that is the difference between toy window logic and production-grade online evaluation.

## 🚀 Variations & Further Practice
- Require **exactly** `m` distinct services instead of at least `m`; the conceptual twist is that shrinking can overshoot validity in both directions, so the window condition is less monotone.
- Replace debt with the sum of gaps to the **k-th highest** severity in the window; now both the reference value and grouped maxima depend on order statistics, not just a single max.
- Make the stream **online with expirations by timestamp** rather than array index; same core pattern, but now window movement is driven by time and late/out-of-order events complicate state maintenance.