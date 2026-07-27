# Minimum Reservation Window for Conference Rooms

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Monotonic Predicate, Array

---

## 🗂 Problem Overview
Given provider capacities `blockLength[i]` and a target `k`, find the smallest integer reservation window `W >= 1` such that the total number of rooms `sum(floor(blockLength[i] / W))` is at least `k`. Every room must receive exactly `W` minutes from a single provider. The challenge is scale: `n` is large, capacities reach `10^12`, and scanning every possible `W` is infeasible. The key observation is that feasibility changes monotonically as `W` grows.

## 🌍 Engineering Impact
This pattern shows up anywhere a global threshold controls per-node throughput or partitioning efficiency. Examples include distributed rate-limiters choosing a common token slice, storage systems selecting shard sizes, media pipelines fixing chunk duration, and schedulers deriving a uniform allocation unit from heterogeneous capacity pools. At scale, brute-force threshold scans turn into latency spikes or offline batch jobs that do not converge in time. Recognizing a monotonic feasibility predicate lets you replace linear answer-space exploration with logarithmic search, which is often the difference between an interactive control loop and an operational bottleneck.

## 🔍 Problem Statement
You are given an array `blockLength` of size `n`, where `blockLength[i]` is the total number of reservable minutes available from provider `i`, and an integer `k`, the number of rooms required. A room is valid only if it receives exactly `W` minutes from a single provider, and the same integer `W` must be used for all providers.

Formally, for a chosen `W >= 1`, provider `i` contributes `floor(blockLength[i] / W)` rooms. Return the smallest integer `W` such that:

`sum(floor(blockLength[i] / W)) >= k`

If even `W = 1` cannot produce `k` rooms, return `-1`.

**Constraints**
- `1 <= n <= 2 * 10^5`
- `1 <= blockLength[i] <= 10^12`
- `1 <= k <= 10^12`

**Examples**
- `blockLength = [8, 5, 12], k = 7` → output `1`
- `blockLength = [2, 1], k = 10` → output `-1`

The decisive constraint is the answer space: `W` can range up to `10^12`, so direct iteration is not viable.

## 🪜 How to Solve This
1. Read the formula carefully → for any fixed `W`, checking feasibility is easy: sum `floor(blockLength[i] / W)` across all providers.
2. Ask what happens as `W` changes → increasing `W` can only decrease or preserve each provider’s contribution, so total rooms is monotone non-increasing.
3. Translate that into a predicate → `feasible(W) := totalRooms(W) >= k`.
4. Notice the shape of the answer space → if some `W` is feasible, every smaller positive `W` is also feasible. That means feasible values form a prefix: `[1 ... boundary]`.
5. The problem asks for the minimum feasible `W`. Under the statement as written, if any solution exists, `W = 1` is always feasible. That makes the mathematical answer trivial after one feasibility check.
6. Still, the intended pattern is binary search on a monotonic predicate. In practice, you would binary-search for the boundary if the objective were the largest feasible `W`, or if the predicate direction were inverted.
7. So the real engineering skill here is not coding binary search mechanically; it is validating whether the optimization target matches the monotonic structure.

## 🧩 Algorithm Walkthrough
1. **Define the feasibility predicate.**  
   Let `rooms(W) = sum(blockLength[i] / W)` using integer division. This computes how many rooms can be formed if every room must have exactly `W` minutes. The predicate is `rooms(W) >= k`.

2. **Establish monotonicity.**  
   This is a **Binary Search on Answer / Monotonic Predicate** problem in structure because for larger `W`, each term `floor(blockLength[i] / W)` never increases. Therefore `rooms(W)` is monotone non-increasing over positive integers.

3. **Check the base feasibility at `W = 1`.**  
   Since `W = 1` is the smallest allowed positive integer, if `rooms(1) < k`, no larger `W` can work. Return `-1`. This is both correct and the strongest impossibility test.

4. **Resolve the stated objective.**  
   If `rooms(1) >= k`, then `W = 1` already satisfies the requirement and is the minimum positive integer. Return `1`. This follows directly from the invariant that all valid answers must be at least `1`.

5. **Why mention binary search at all?**  
   Because the underlying predicate is exactly the kind you would binary-search if the objective were “maximum feasible `W`” or “first infeasible `W`.” The invariant in that version is a shrinking search interval preserving the boundary between feasible and infeasible values.

6. **Implementation detail: overflow control.**  
   Accumulate the room count in 64-bit arithmetic and short-circuit once the running sum reaches `k`. That avoids unnecessary work and protects against accidental overflow in languages with narrower defaults.

## 📊 Worked Example
Take `blockLength = [8, 5, 12]`, `k = 7`.

| Step | `W` | Contributions | Total rooms | Decision |
|---|---:|---|---:|---|
| 1 | 1 | `8/1, 5/1, 12/1` → `8, 5, 12` | 25 | `25 >= 7`, feasible |
| 2 | — | Smallest allowed `W` is already feasible | — | Return `1` |

Boundary intuition still matters:

- `W = 3` gives `2 + 1 + 4 = 7`, feasible.
- `W = 4` gives `2 + 1 + 3 = 6`, infeasible.

So the feasibility boundary is between `3` and `4`, which is exactly the shape binary search exploits in the usual “largest feasible `W`” variant. But under the problem’s literal objective—minimum feasible `W`—the answer collapses to checking `W = 1` first. That is the critical reading-comprehension trap.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` for the problem exactly as stated, because only one feasibility pass at `W = 1` is required. The dominant operation is summing provider capacities, with optional early exit once the count reaches `k`. At `10^6` elements this is routine; at `10^9`, even linear scans become infrastructure decisions rather than algorithm exercises.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses only a running 64-bit accumulator and loop variables; the input array owns the memory footprint. Space cannot be meaningfully reduced further unless the data is streamed, in which case the same logic still works with identical asymptotic memory.

## 💡 Key Takeaways
- If a problem defines `sum(floor(a[i] / x))` and asks whether some target can be met, that is a strong signal for a monotonic predicate over the answer space.
- When feasibility only decreases as the candidate value increases, think binary search on the boundary rather than constructing solutions directly.
- Here the wording matters: asking for the **minimum** feasible positive `W` makes the answer trivial once `W = 1` is feasible.
- Use 64-bit arithmetic for both `blockLength[i]` and the running total, and short-circuit once the sum reaches `k` to avoid unnecessary accumulation.
- In production code, the deeper skill is validating that the optimization objective actually matches the monotonic-search pattern before committing to a more complex implementation.

## 🚀 Variations & Further Practice
- Change the objective to **maximum** integer `W` such that at least `k` rooms can be created. Same predicate, but now binary search is essential because the answer is the last feasible point.
- Allow each provider to have a provider-specific setup overhead or minimum chunk size before contributing rooms. The predicate may remain monotonic, but the per-provider contribution function becomes less uniform and easier to implement incorrectly.
- Add a budget constraint where using provider `i` incurs a fixed activation cost plus per-room cost. Now feasibility may require combining binary search with greedy selection or dynamic programming, depending on the cost model.