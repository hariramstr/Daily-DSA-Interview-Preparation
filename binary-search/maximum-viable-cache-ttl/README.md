# Maximum Viable Cache TTL

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Hash Map, Monotonic Predicate

---

## 🗂 Problem Overview
Given chronologically ordered requests `(key, time, cost)` and a recomputation budget `B`, find the maximum integer cache TTL `T` such that total recomputation cost stays within budget. A request is a hit only if the previous request for the same key occurred at most `T` seconds earlier; otherwise it is a miss and pays full cost. The challenge is scale: `n` is up to `200,000`, times are large, and naive simulation across all candidate TTLs is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a global freshness window controls cost or load: CDN/object caches, API response memoization, feature-store serving, search result caching, stream deduplication, and stateful event processors. At scale, the question is rarely “does caching help?” but “what freshness threshold keeps backend spend under a hard budget?” Without a monotonic-search formulation, teams end up brute-forcing policy values, overprovisioning compute, or shipping brittle heuristics. The binary-search-plus-linear-check approach turns policy tuning into a deterministic capacity-planning primitive: predictable, explainable, and cheap enough to run repeatedly in control loops or offline optimization jobs.

## 🔍 Problem Statement
You are given three equal-length arrays `keys`, `times`, and `costs`, sorted by nondecreasing `times`, plus a budget `B`. For request `i`, `keys[i]` identifies the resource, `times[i]` is its arrival time, and `costs[i]` is the recomputation cost if the request is a cache miss.

A single global TTL `T` applies to all keys. For a fixed key, a request is a hit iff the immediately previous request for that same key occurred within `T` seconds, i.e. `current_time - previous_time <= T`. Hits cost `0`; misses cost `costs[i]` and refresh the cached value.

Return the maximum integer `T` such that total recomputation cost is at most `B`. If no `T` works, return `-1`.

Examples:
- `keys=[1,2,1,1,2], times=[1,2,4,8,10], costs=[5,7,5,5,7], B=17`
- `keys=[3,3,3,4], times=[5,9,15,20], costs=[4,4,4,10], B=13`

The key algorithmic constraint is `n <= 200000`: this rules out evaluating many TTLs with anything slower than linear-time feasibility.

## 🪜 How to Solve This
1. Read the cost rule carefully → for any fixed `T`, total cost is easy to simulate in one left-to-right pass if you remember the last timestamp seen for each key.

2. Notice the monotonicity → increasing `T` can only convert misses into hits, never the reverse. So total recomputation cost is non-increasing as `T` grows.

3. Once you see “find maximum `T` such that predicate holds” and the predicate is monotonic, think binary search on the answer.

4. Define the predicate: `feasible(T) = (total_cost_with_T <= B)`.

5. Implement `feasible(T)` with a hash map:
   - if key not seen before → miss
   - else compare current time with last time for that key
   - gap `<= T` → hit
   - gap `> T` → miss
   - always update last seen time to current time

6. Search space matters. The answer only changes when `T` crosses some observed same-key gap, plus `0`. So binary search over integer TTLs from `0` to `max(times) - min(times)` is fine, or more tightly over induced gaps after precomputing them.

7. Edge case → if even an effectively infinite TTL still exceeds `B`, return `-1`.

## 🧩 Algorithm Walkthrough
1. **Precompute an upper bound for search.**  
   Use the maximum same-key gap, or more simply `times[n-1] - times[0]`. No TTL above that changes hit/miss outcomes. This bounds the binary search domain.

2. **Define the monotonic predicate `feasible(T)`.**  
   Scan requests from left to right while maintaining `last_seen[key] = most recent timestamp for that key`. This is the **Hash Map + Monotonic Predicate** pattern: the map gives `O(1)` state access per request, and the predicate supports binary search.

3. **Evaluate each request under TTL `T`.**  
   If the key has no prior request, it is a miss. Otherwise compute `gap = times[i] - last_seen[key]`. If `gap <= T`, it is a hit and adds `0`; else it is a miss and adds `costs[i]`. Then update `last_seen[key] = times[i]`.

4. **Maintain the invariant during the scan.**  
   After processing index `i`, `last_seen` stores the latest timestamp for every key among requests `0..i`, and `running_cost` equals the exact recomputation cost for that prefix under TTL `T`.

5. **Short-circuit when possible.**  
   If `running_cost > B`, return `false` immediately. This does not change correctness and improves practical performance on infeasible TTLs.

6. **Binary search for the maximum feasible TTL.**  
   Since `feasible(T)` is monotone non-decreasing with larger TTLs, use upper-mid binary search:
   - if `feasible(mid)` is true, move right
   - else move left

7. **Handle impossibility.**  
   First test the largest relevant TTL. If it is still infeasible, no TTL can satisfy the budget, so return `-1`.

## 📊 Worked Example
Take `keys=[3,3,3,4]`, `times=[5,9,15,20]`, `costs=[4,4,4,10]`, `B=14`.

Check `T = 6`:

| i | key | time | last seen before | gap | hit/miss | added cost | total |
|---|-----|------|------------------|-----|----------|------------|-------|
| 0 | 3 | 5  | —  | — | miss | 4  | 4  |
| 1 | 3 | 9  | 5  | 4 | hit  | 0  | 4  |
| 2 | 3 | 15 | 9  | 6 | hit  | 0  | 4  |
| 3 | 4 | 20 | —  | — | miss | 10 | 14 |

`feasible(6) = true`.

Now check `T = 5`:
- request 0: miss → `4`
- request 1: gap `4 <= 5`, hit → `4`
- request 2: gap `6 > 5`, miss → `8`
- request 3: miss → `18`

So `feasible(5) = false`. The boundary is exactly `T = 6`, which is the maximum valid TTL.

## ⏱ Complexity Analysis

### Time Complexity
Each feasibility check is `O(n)` because every request performs one hash map lookup/update and constant-time arithmetic. Binary search adds a `log U` factor, where `U` is the TTL search range, so total time is `O(n log U)`. With `n = 2e5` and `U` up to `1e18`, this is still practical because `log2(1e18) ≈ 60`.

### Space Complexity
The hash map stores the most recent timestamp per distinct key, so space is `O(k)`, where `k` is the number of unique keys and `k <= n`. This is already minimal for single-pass exact evaluation; reducing it would require sacrificing correctness or adding extra passes.

## 💡 Key Takeaways
• If the problem asks for the maximum parameter value satisfying a budget/capacity constraint, check whether the feasibility predicate is monotonic and binary-searchable.  
• If each decision depends only on the most recent occurrence of an entity, a hash map of last-seen state is usually the right linear-time primitive.  
• The hit condition is `gap <= T`, not `< T`; that boundary determines correctness at exact threshold values.  
• Always update `last_seen[key]` on every request, including hits; the cache window chains through consecutive accesses.  
• In production, this is the same move as turning a policy-tuning problem into a monotone optimization problem with a cheap simulator.

## 🚀 Variations & Further Practice
- **Per-key TTL budgets:** each key may choose its own TTL under a global memory or staleness budget; the monotonicity becomes multi-dimensional and simple binary search no longer applies directly.
- **TTL with finite cache capacity:** add eviction alongside expiration; now hit/miss depends on both time and replacement policy, pushing the problem toward simulation with ordered structures.
- **Online adaptation:** requests arrive as a stream and you must adjust TTL continuously from observed miss cost; the twist is moving from offline exact optimization to control-loop or bandit-style estimation.