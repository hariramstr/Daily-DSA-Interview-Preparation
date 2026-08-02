# Minimum Pump Rate for Reservoir Refill

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Math

---

## 🗂 Problem Overview
Given an array `volumes` and a deadline `h`, find the smallest integer pump rate `k` such that all reservoirs can be refilled within `h` nights. A reservoir with volume `v` takes `ceil(v / k)` nights because only one reservoir can be worked on per night and unused capacity is lost. The challenge is that `volumes[i]` can be as large as `10^9`, so testing every possible rate is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere you need to compute the minimum capacity that satisfies a deadline under indivisible work units: provisioning batch workers, sizing ingestion throughput for streaming backfills, selecting shard migration bandwidth, or setting compaction/repair rates in storage systems. At scale, brute-force capacity search turns planning into an unbounded simulation problem. The monotonic-feasibility framing changes that into a predictable logarithmic search over the answer space. That matters operationally: it enables fast what-if analysis, tighter autoscaling decisions, and deterministic admission-control thresholds without requiring expensive exhaustive evaluation.

## 🔍 Problem Statement
You are given:

- `volumes[i]`: water required for the `i`-th reservoir
- `h`: total nights available

In one night, the team chooses exactly one reservoir and pumps at rate `k` units for that night. If the reservoir needs less than `k`, the extra capacity is wasted. A reservoir of volume `v` therefore needs `ceil(v / k)` nights.

Return the minimum integer `k` such that:

- all reservoirs are completely refilled
- total nights used is at most `h`

Constraints:

- `1 <= volumes.length <= 100000`
- `1 <= volumes[i] <= 1000000000`
- `volumes.length <= h <= 1000000000`

Examples:

- `volumes = [8, 5, 10, 7], h = 8` → `5`
- `volumes = [30, 11, 23, 4, 20], h = 6` → `23`

The decisive constraint is the rate search space: `k` can range up to `max(volumes)`, which makes linear search too slow.

## 🪜 How to Solve This
1. Read the objective carefully → we are not asked for the total nights for a given rate; we are asked for the **minimum rate** that satisfies a deadline.

2. Ask what happens if the rate increases → every term `ceil(v / k)` stays the same or decreases. Total required nights is therefore monotonic non-increasing in `k`.

3. Monotonic answer space → this is the classic signal for **binary search on the answer**, not binary search on the input array.

4. Define a feasibility check: for a candidate rate `k`, compute  
   `sum(ceil(volumes[i] / k))`.  
   If the sum is `<= h`, the rate is sufficient. Otherwise it is too slow.

5. Bound the search space:
   - minimum possible rate is `1`
   - maximum necessary rate is `max(volumes)` because at that rate every reservoir finishes in one night

6. Binary search for the first feasible rate:
   - if `mid` works, keep searching left for a smaller valid rate
   - if `mid` fails, search right

7. Use integer arithmetic for ceiling division: `(v + k - 1) / k`. That avoids floating-point error and keeps the implementation branch-free.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Binary Search on Answer Space.**  
   The search domain is the integer rate `k`, not the array. This is correct because feasibility is monotonic: once some rate works, all larger rates also work.

2. **Initialize bounds.**  
   Set `left = 1` and `right = max(volumes)`.  
   Invariant: the true answer always lies within `[left, right]`.

3. **Pick the midpoint.**  
   Compute `mid = left + (right - left) / 2`.  
   This avoids overflow in languages with fixed-width integers and preserves the binary search invariant.

4. **Evaluate feasibility at `mid`.**  
   For each reservoir volume `v`, add `(v + mid - 1) / mid` to a running `nights` total.  
   This exactly computes `ceil(v / mid)` using integer math.  
   Invariant during accumulation: `nights` equals the total nights needed for all reservoirs processed so far.

5. **Prune the search space.**  
   - If `nights <= h`, then `mid` is feasible. Record that the answer is at most `mid`, so move `right = mid`.  
   - If `nights > h`, then `mid` is infeasible. All smaller rates also fail, so move `left = mid + 1`.

6. **Terminate when bounds converge.**  
   When `left == right`, the interval contains exactly one rate. By construction, it is the smallest feasible rate.

7. **Why this is correct.**  
   The algorithm maintains two facts: everything below the lower bound is known infeasible, and the answer remains inside the current interval. Monotonicity guarantees no valid answer is discarded.

## 📊 Worked Example
Example: `volumes = [8, 5, 10, 7]`, `h = 8`

| Step | left | right | mid | Nights at mid | Decision |
|---|---:|---:|---:|---:|---|
| 1 | 1 | 10 | 5 | `2+1+2+2 = 7` | feasible → `right = 5` |
| 2 | 1 | 5 | 3 | `3+2+4+3 = 12` | infeasible → `left = 4` |
| 3 | 4 | 5 | 4 | `2+2+3+2 = 9` | infeasible → `left = 5` |

Now `left == right == 5`, so the minimum valid pump rate is `5`.

Why this trace matters: `5` works, but `4` does not. Binary search is specifically finding the boundary between infeasible and feasible rates. The feasibility function over rates looks like a monotonic step function, and the algorithm converges directly to its first valid point.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = volumes.length` and `M = max(volumes)`. Each binary-search step scans the array once to compute required nights, and there are `log M` such steps. Even if `M` is `10^9`, `log2(M)` is about 30, so the algorithm remains practical for arrays in the `10^5` to `10^6` range.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only bounds, a midpoint, and a running sum. No extra data structures scale with input size. Space cannot be meaningfully reduced further unless you change the input representation itself.

## 💡 Key Takeaways
- If the question asks for the **minimum value that satisfies a constraint**, check whether feasibility becomes monotonic as the candidate value increases.
- When the answer range is huge but each candidate can be validated in linear time, binary search on the answer is usually the right move.
- Use integer ceiling division as `(v + k - 1) / k`; floating-point `ceil(v / k)` is unnecessary and can introduce avoidable bugs.
- Be precise about the binary search variant: this is a **first feasible value** search, so on success move `right = mid`, not `mid - 1`.
- In production systems, monotonic-feasibility transforms capacity planning from open-ended simulation into bounded search with predictable latency.

## 🚀 Variations & Further Practice
- **Ship packages within D days**: same binary-search-on-capacity pattern, but feasibility depends on preserving item order and packing multiple items per day.
- **Koko Eating Bananas**: nearly identical structure; the twist is recognizing that the answer space is the rate, not the array contents.
- **Split array largest sum**: binary search over the maximum allowed partition sum, where the harder part is proving the greedy feasibility check is valid.