# Earliest Day to Activate K Sensor Corridors

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Arrays

---

## 🗂 Problem Overview
Given `activationDay[i]`, determine the minimum day `D` such that at least `k` non-overlapping contiguous sensor corridors can be chosen, where every sensor in each corridor is active by day `D` and each corridor length lies in `[minLen, maxLen]`. Return `-1` if impossible. The challenge is scale: `n` can reach `200000`, so trying every day or enumerating corridor combinations is not viable.

## 🌍 Engineering Impact
This pattern shows up in systems that need the earliest feasible rollout point under local contiguity constraints: shard activation in distributed storage, contiguous capacity windows in schedulers, radio spectrum block allocation, and streaming pipelines that require adjacent healthy partitions before promotion. At scale, brute-force feasibility checks collapse under repeated recomputation. The binary-search-on-answer pattern converts a large temporal search space into logarithmically many validation passes, while the greedy feasibility check turns a combinatorial packing problem into a linear scan. That combination is what makes SLA-sensitive planning and admission decisions tractable on large fleets.

## 🔍 Problem Statement
You are given an array `activationDay` of length `n`, where sensor `i` becomes operational on day `activationDay[i]`. A valid corridor is a contiguous block of sensors whose length is between `minLen` and `maxLen`, inclusive, and where every sensor in the block is active by day `D`.

The task is to return the smallest day `D` such that at least `k` non-overlapping valid corridors can be selected. Corridors may have different lengths as long as each lies in `[minLen, maxLen]`. If no such day exists even after all sensors are active, return `-1`.

Constraints:
- `1 <= n <= 200000`
- `1 <= activationDay[i] <= 1000000000`
- `1 <= k <= n`
- `1 <= minLen <= maxLen <= n`

Examples:
- `activationDay = [4,2,5,3,3,6,1], k = 2, minLen = 2, maxLen = 3` → `4`
- `activationDay = [7,7,7], k = 2, minLen = 2, maxLen = 2` → `-1`

The key algorithmic pressure is that both the day range and the search space of corridor selections are large.

## 🪜 How to Solve This
1. Read the problem → the output is the *earliest day*, not the corridors themselves. That is a strong signal for binary search on the answer.
2. Ask whether feasibility is monotonic → if day `D` works, any later day also works because more sensors become active, never fewer.
3. Reduce the problem for a fixed day `D` → each sensor is just active/inactive. Now the question becomes: can we extract at least `k` disjoint active segments with lengths in `[minLen, maxLen]`?
4. Observe that active sensors form maximal runs. Corridors cannot cross inactive sensors, so each run can be processed independently.
5. Inside one active run of length `L`, to maximize the number of non-overlapping corridors, always take the shortest allowed length `minLen`. Any longer choice consumes more sensors and cannot increase the count.
6. So a run contributes `floor(L / minLen)` corridors, provided `L >= minLen`. Summing across runs gives the maximum number of corridors possible on day `D`.
7. Binary search the minimum day whose feasibility check reaches at least `k`.

Once that monotonicity and run-level greedy fact are clear, the implementation is straightforward.

## 🧩 Algorithm Walkthrough
1. **Early impossibility check**  
   If `k * minLen > n`, return `-1` immediately. Even with every sensor active, there are not enough positions to host `k` disjoint corridors. This avoids unnecessary search.

2. **Define the monotone predicate `can(D)`**  
   Scan `activationDay` left to right and treat `activationDay[i] <= D` as active. Maintain the current active run length `run`. When an inactive sensor appears, finalize that run and add `run / minLen` to the corridor count. Reset `run = 0`. After the scan, finalize the last run as well.

3. **Why the greedy count is correct**  
   The pattern is **Binary Search on Answer + Greedy Feasibility Check**. For a run of length `L`, any valid corridor uses at least `minLen` sensors. Therefore no solution can extract more than `floor(L / minLen)` disjoint corridors from that run. This upper bound is achievable by partitioning greedily into consecutive blocks of size `minLen`, ignoring leftover sensors. `maxLen` does not constrain this construction because `minLen <= maxLen`.

4. **Maintain the invariant during scanning**  
   After processing index `i`, `count` equals the maximum number of valid non-overlapping corridors obtainable from all completed runs strictly before or ending at `i`, and `run` is the length of the current unfinished active suffix.

5. **Binary search the day**  
   Search between `min(activationDay)` and `max(activationDay)`. If `can(mid)` is true, record `mid` as a candidate and move left; otherwise move right. The first feasible day is the answer.

## 📊 Worked Example
Use `activationDay = [4,2,5,3,3,6,1]`, `k = 2`, `minLen = 2`, `maxLen = 3`.

Check day `D = 4`:

| i | activationDay[i] | active by day 4? | run | completed corridors |
|---|---:|:---:|---:|---:|
| 0 | 4 | yes | 1 | 0 |
| 1 | 2 | yes | 2 | 0 |
| 2 | 5 | no  | 0 | 1 (`2/2`) |
| 3 | 3 | yes | 1 | 1 |
| 4 | 3 | yes | 2 | 1 |
| 5 | 6 | no  | 0 | 2 (`2/2`) |
| 6 | 1 | yes | 1 | 2 |

Finalize trailing run: `1/2 = 0`. Total corridors = `2`, so day `4` is feasible.

Check day `D = 3`: active pattern is `[F,T,F,T,T,F,T]`. Runs have lengths `1,2,1`, contributing `0 + 1 + 0 = 1`. Not feasible.

Binary search therefore converges to `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log U)`, where `U = max(activationDay) - min(activationDay) + 1`, or equivalently `O(n log M)` for `M = max(activationDay)`. Each feasibility check is a single linear scan, and binary search performs about 31 iterations for 32-bit day values. This remains practical for `10^6` elements; `10^9` elements would be dominated by scan cost, not the logarithmic search.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only counters, bounds, and the current run length. Space cannot meaningfully be reduced further; any alternative that materializes active/inactive arrays would only increase memory footprint without improving asymptotic time.

## 💡 Key Takeaways
- If the question asks for the minimum threshold where a condition becomes possible, test for monotonicity first; that is the classic trigger for binary search on the answer.
- If feasibility depends on packing disjoint segments inside contiguous valid runs, try reducing each run to an independent counting problem.
- Do not overcomplicate `maxLen`: when maximizing the number of corridors, choosing length `minLen` is always optimal because longer segments only waste capacity.
- Be careful to finalize the last active run after the scan; missing the tail run is the most common off-by-one bug here.
- In production systems, separating a monotone decision predicate from the optimization search is a powerful design pattern: it yields simpler proofs, better testability, and predictable scaling.

## 🚀 Variations & Further Practice
- Require each chosen corridor to have length **exactly** `x` instead of a range. The feasibility check changes from `run / minLen` to `run / x`, and the problem becomes a stricter packing variant.
- Add a constraint that adjacent corridors must be separated by at least `g` inactive or unused sensors. The greedy counting logic must now account for spacing, not just raw run length.
- Allow corridor value weights and ask for the earliest day where total selected weight reaches a target. This breaks the simple per-run division and pushes the feasibility check toward DP or interval optimization.