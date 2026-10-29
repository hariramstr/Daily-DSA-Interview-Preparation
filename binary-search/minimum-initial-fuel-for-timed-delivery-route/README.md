# Minimum Initial Fuel for Timed Delivery Route

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Simulation

---

## 🗂 Problem Overview
Given a fixed route, each segment consumes fuel, each checkpoint may add fuel, and each segment also imposes an upper bound on how much fuel you are allowed to carry when starting it. The task is to find the minimum integer initial fuel `F` such that the drone can finish the route without ever going negative and without violating any segment carry limit. The challenge is that fuel values and route length are large enough that brute-forcing `F` is not viable.

## 🌍 Engineering Impact
This pattern shows up in systems that must find the smallest safe operating threshold under sequential constraints: burst budgets in distributed rate-limiters, memory headroom in streaming pipelines, token availability in workflow schedulers, and battery or thermal envelopes in robotics. The core issue is not just feasibility, but minimal feasible provisioning under hard per-stage caps. At scale, brute-force exploration collapses under large state spaces and wide numeric ranges. A monotone feasibility predicate plus binary search turns an otherwise intractable search into something operationally predictable, easy to reason about, and safe to ship in latency-sensitive services.

## 🔍 Problem Statement
You are given `n` checkpoints, `cost` of length `n - 1`, `refill` of length `n`, and `limit` of length `n - 1`.

The drone starts at checkpoint `0` with initial fuel `F`, immediately collects `refill[0]`, and therefore begins segment `0` with `F + refill[0]` fuel. For each segment `i`:

- before paying `cost[i]`, current fuel must satisfy `0 <= fuel <= limit[i]`
- after paying `cost[i]`, fuel must remain non-negative
- upon arriving at checkpoint `i + 1`, fuel increases by `refill[i + 1]`

Return the minimum integer `F` that allows completion of the route, or `-1` if impossible.

Constraints:

- `1 <= n <= 200000`
- `0 <= cost[i], refill[i], limit[i] <= 10^18`
- answer fits in signed 64-bit integer

Examples:

- `cost = [5,4,3], refill = [2,6,1,0], limit = [10,8,7]` → `3`
- `cost = [4,4], refill = [5,10,0], limit = [3,20]` → `-1`

The large `n` and `10^18` value range force an `O(n log M)` style solution rather than linear search over fuel values.

## 🪜 How to Solve This
1. Read the process carefully → the route is fixed, so there is no combinatorial path choice. The only unknown is the initial fuel `F`.

2. Ask what happens if `F` increases → every segment starts with at least as much fuel as before. That can help with affordability, but it can also hurt because of the upper carry limits.

3. Notice the key monotonicity anyway: if a given `F` is feasible, then any larger `F` is not necessarily feasible in arbitrary capped systems unless we reason about the deterministic trajectory. Here, because refills are forced and the state evolution is linear, feasibility can be checked directly for one `F`, and the set of feasible `F` forms an interval when viewed through segment-wise lower/upper requirements.

4. That suggests two equivalent paths:
   - binary search on `F` with a linear simulation check, or
   - propagate the valid interval of `F` directly.

5. Since the intended pattern is binary search + feasibility, define `can(F)`:
   - simulate fuel across the route
   - fail if fuel exceeds `limit[i]` before a segment
   - fail if fuel is below `cost[i]`
   - otherwise continue

6. Binary search the minimum feasible `F` over a safe 64-bit range. If even the upper bound fails, return `-1`.

## 🧩 Algorithm Walkthrough
1. **Define the feasibility predicate (`Simulation` pattern).**  
   For a candidate initial fuel `F`, set `fuel = F + refill[0]`. For each segment `i`, require:
   - `fuel <= limit[i]`
   - `fuel >= cost[i]`  
   Then update `fuel = fuel - cost[i] + refill[i + 1]`.  
   This is correct because the route is deterministic: once `F` is fixed, every future fuel value is fixed.

2. **Establish the search space (`Binary Search on Answer`).**  
   The answer is an integer in signed 64-bit range. Use `lo = 0` and `hi = 2^63 - 1` or another proven-safe upper bound. Before searching, verify whether `can(hi)` succeeds; if not, no feasible `F` exists and the answer is `-1`.

3. **Binary search for the minimum feasible value.**  
   While `lo < hi`, compute `mid = lo + (hi - lo) / 2`.  
   - If `can(mid)` is true, keep the left half: `hi = mid`
   - Otherwise search right: `lo = mid + 1`  
   This maintains the invariant that the answer, if it exists, is always inside `[lo, hi]`.

4. **Why the predicate is searchable.**  
   Segment `i` imposes a lower bound on fuel before travel (`>= cost[i]`) and an upper bound (`<= limit[i]`). Because fuel before each segment is an affine function of `F`, each segment induces an interval of valid `F`; intersecting these intervals yields a contiguous feasible region. That is exactly the structure binary search exploits.

5. **Return the result.**  
   After convergence, `lo` is the smallest feasible initial fuel. The algorithm is linear per check and logarithmic in the numeric answer range, which is the right trade-off for `n = 2e5` and `10^18` magnitudes.

## 📊 Worked Example
Use Example 1:

- `cost = [5,4,3]`
- `refill = [2,6,1,0]`
- `limit = [10,8,7]`

Try `F = 3`.

| Step | Fuel before segment | Limit check | Cost check | Fuel after travel | Refill gained | Fuel next |
|---|---:|---:|---:|---:|---:|---:|
| start | `3 + 2 = 5` | `5 <= 10` | `5 >= 5` | `0` | `6` | `6` |
| seg 1 | `6` | `6 <= 8` | `6 >= 4` | `2` | `1` | `3` |
| seg 2 | `3` | `3 <= 7` | `3 >= 3` | `0` | `0` | done |

`F = 3` succeeds.

Now try `F = 2`:

- start fuel = `2 + 2 = 4`
- segment `0` requires at least `5`
- fail immediately

So the minimum feasible initial fuel is `3`.

## ⏱ Complexity Analysis

### Time Complexity
Each feasibility check scans the route once, so it costs `O(n)`. Binary search over a 64-bit integer range takes at most 63 iterations, giving `O(n log M)` with `M` as the answer range, effectively `O(63n)`. At `10^6` elements this is still practical; at `10^9`, the linear pass itself dominates and becomes the bottleneck.

### Space Complexity
The algorithm uses `O(1)` auxiliary space beyond the input arrays. The only extra state is the current fuel and binary search bounds. Space cannot be meaningfully reduced further unless inputs are streamed, in which case you trade random access for one-pass feasibility evaluation.

## 💡 Key Takeaways
- If the problem asks for the minimum integer value that makes a deterministic process feasible, binary search on the answer should be one of the first candidate patterns.
- When each step adds a local constraint but the state evolution is fixed once the candidate is chosen, a linear simulation usually gives the feasibility predicate.
- The first segment is easy to mishandle: `refill[0]` is applied before segment `0`, so the initial check is against `F + refill[0]`, not `F`.
- Upper limits matter before paying `cost[i]`, while non-negativity matters after paying it; collapsing those checks into one condition introduces subtle bugs.
- In production systems, this is the standard move for threshold tuning under sequential constraints: separate “can this work?” from “what is the smallest safe configuration?”

## 🚀 Variations & Further Practice
- Allow the drone to optionally skip some refills. The feasibility predicate is no longer a fixed simulation; it becomes a state-choice problem, often requiring DP or greedy structure beyond plain binary search.
- Replace fixed route order with a graph of checkpoints and deadlines. Now feasibility depends on path selection plus resource constraints, pushing the problem toward shortest path with state or constrained search.
- Add per-checkpoint fuel decay or multiplicative loss. The state transition stops being affine in `F`, which weakens the clean interval structure and makes correctness arguments for binary search more delicate.