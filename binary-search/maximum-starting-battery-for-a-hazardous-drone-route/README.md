# Maximum Starting Battery for a Hazardous Drone Route

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Prefix Sum, Monotonic Predicate

---

## 🗂 Problem Overview
Given an integer array `delta` and a battery `capacity`, find the largest integer starting battery `B` in `[0, capacity]` such that after applying each checkpoint update in order, every intermediate battery level remains within `[0, capacity]`. Return `-1` if no start value works. The non-trivial part is that feasibility depends on the entire prefix history, not just the final sum, and the search space for `B` can be as large as `10^18`.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the largest safe initial budget under cumulative drift: distributed rate-limiters with burst credits, streaming pipelines with bounded buffers, warehouse robotics with energy envelopes, and admission control in schedulers with headroom constraints. At scale, brute-forcing candidate starting states is impossible, and local checks are misleading because failures are caused by prefix excursions, not terminal state. The combination of a monotonic feasibility predicate and prefix accumulation enables predictable latency, bounded memory, and a clean separation between simulation logic and search strategy.

## 🔍 Problem Statement
A drone starts with an integer battery `B` before checkpoint `0`. At checkpoint `i`, its battery changes by `delta[i]`, where values may be positive or negative. After processing every checkpoint, the battery must remain in the inclusive safe range `[0, capacity]`. If it drops below `0`, the drone crashes; if it exceeds `capacity`, the controller fails.

Compute the **maximum** feasible starting battery `B`. If no integer `B` in `[0, capacity]` allows the full route to complete safely, return `-1`.

**Constraints**
- `1 <= delta.length <= 2 * 10^5`
- `-10^9 <= delta[i] <= 10^9`
- `0 <= capacity <= 10^18`

The key constraint is the huge answer range: scanning all possible starts is impossible, so the solution must exploit monotonicity.

**Examples**
- `delta = [4, -7, 3, -2], capacity = 8` → `4`
- `delta = [-3, 5, -4, 1], capacity = 6` → `3`

## 🪜 How to Solve This
1. Read the requirement carefully → we are not checking one starting battery; we need the **largest** valid one.
2. A candidate `B` is feasible if every prefix-adjusted battery stays in range: `B + prefixSum[i]` must be between `0` and `capacity`.
3. Notice the monotonic structure → if some `B` is feasible, then any smaller `B` may avoid overflow but may fail on underflow. More directly, feasibility can be checked against prefix bounds, and the valid `B` values form an interval.
4. That immediately suggests two routes:
   - derive the interval directly from prefix minima/maxima, or
   - use **binary search on the answer** with a feasibility check.
5. Since the problem explicitly expects binary search, define `can(B)` by simulating the route and rejecting on the first out-of-range battery.
6. Because increasing `B` only shifts every prefix level upward, once starts become too large they stay too large. That gives the monotonic predicate binary search needs.
7. Search for the maximum feasible `B` in `[0, capacity]`, track the best valid value, and return `-1` if none exists.

## 🧩 Algorithm Walkthrough
1. **Define the predicate `can(B)`**  
   Simulate the route from starting battery `B`. After each checkpoint, update `battery += delta[i]`. If `battery < 0` or `battery > capacity`, return `false`; otherwise continue. If all checkpoints pass, return `true`.  
   **Invariant:** after processing checkpoint `i`, `battery` equals `B + prefixSum[i]`.

2. **Recognize the pattern: Binary Search on Answer + Monotonic Predicate**  
   Feasibility changes monotonically with respect to overflow pressure: as `B` increases, every intermediate battery level increases by the same amount. That means there is a highest feasible starting value, and all larger values are impossible. This is exactly the abstraction binary search needs.

3. **Initialize the search space**  
   Set `lo = 0`, `hi = capacity`, and `ans = -1`. We only search legal starting batteries, so no extra validation range is needed.

4. **Binary search for the rightmost feasible value**  
   While `lo <= hi`, compute `mid = lo + (hi - lo) / 2`.  
   - If `can(mid)` is true, record `ans = mid` and move right with `lo = mid + 1`.  
   - Otherwise move left with `hi = mid - 1`.  
   **Invariant:** `ans` is always the largest feasible value seen so far.

5. **Return the result**  
   If no candidate passed, `ans` remains `-1`. Otherwise it is the maximum feasible start.

A stronger observation exists: feasible `B` must satisfy `-minPrefix <= B <= capacity - maxPrefix`. That yields an `O(n)` direct solution. But the requested pattern is binary search, and it still meets the target complexity.

## 📊 Worked Example
Take `delta = [4, -7, 3, -2]`, `capacity = 8`.

Binary search over `B ∈ [0, 8]`:

| mid | Battery trace after checkpoints | Feasible? | Action |
|---:|---|---|---|
| 4 | `8, 1, 4, 2` | Yes | `ans = 4`, search right |
| 6 | `10, 3, 6, 4` | No, exceeds `8` at first step | search left |
| 5 | `9, 2, 5, 3` | No, exceeds `8` at first step | search left |

Search terminates with `ans = 4`.

Equivalent prefix view:
- Prefix sums: `4, -3, 0, -2`
- Minimum prefix = `-3` → need `B >= 3`
- Maximum prefix = `4` → need `B <= 8 - 4 = 4`

So feasible starts are exactly `3` and `4`, and the maximum is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`can(B)` scans `n` checkpoints, so it costs `O(n)`. Binary search performs `O(log capacity)` checks, giving total time `O(n log capacity)`. With `capacity <= 10^18`, `log2(capacity)` is at most about `60`, so runtime is effectively around `60n`, which is practical even when `n` approaches `2 * 10^5`.

### Space Complexity
`O(1)` auxiliary space for the binary-search version if feasibility is checked by streaming through `delta` without storing prefix sums. If you precompute prefixes to derive bounds directly, space becomes `O(1)` or `O(n)` depending on whether you retain the full prefix array for debugging or analysis.

## 💡 Key Takeaways
- If the problem asks for the maximum or minimum numeric answer and gives a fast yes/no validator, look for **binary search on the answer**.
- If each candidate value shifts all prefix states uniformly, that is a strong signal for a **monotonic predicate** over the search space.
- Use `mid = lo + (hi - lo) / 2`; with `capacity` up to `10^18`, naive midpoint arithmetic can overflow in fixed-width integer languages.
- Be precise about the search direction: on a feasible `mid`, move right to find the **largest** valid start, not just any valid one.
- In production systems, many “largest safe configuration” problems reduce to finding the boundary of a monotone failure region; separating boundary search from state validation keeps the design testable and scalable.

## 🚀 Variations & Further Practice
- Allow checkpoint reordering or optional skips: the problem stops being pure boundary search and becomes a scheduling/selection problem over feasible prefixes.
- Replace fixed `capacity` with time-varying per-checkpoint bounds: feasibility still depends on prefixes, but the valid interval for `B` is constrained by heterogeneous upper and lower envelopes.
- Optimize for the number of feasible starting batteries instead of the maximum one: same prefix-bound core, but now you compute interval size rather than just its right endpoint.