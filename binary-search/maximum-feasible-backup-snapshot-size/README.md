# Maximum Feasible Backup Snapshot Size

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array `backups` and an integer `k`, split the array into exactly `k` non-empty contiguous bundles while preserving order. The score of a partition is the minimum bundle sum; return the largest possible score. The challenge is that the search space over all contiguous `k`-partitions is combinatorial, so exhaustive partition enumeration is infeasible at `n = 200000`. The key observation is monotonic feasibility: if a minimum bundle sum `S` works, every smaller `S` also works.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered workloads must be partitioned under a fairness or floor constraint: sharding append-only logs across restore windows, batching compaction jobs in storage engines, assigning contiguous video segments to transcoders, or slicing ETL streams into checkpointable stages. At scale, brute-force partitioning collapses under combinatorial growth, and ad hoc heuristics produce unstable guarantees. Binary search on the answer plus a linear feasibility pass gives a predictable envelope: deterministic behavior, bounded runtime, and a clean abstraction for “maximize the minimum acceptable unit,” which is a common operational objective in capacity planning and workload balancing.

## 🔍 Problem Statement
You are given:

- `backups[i]`: size of the `i`-th backup in gigabytes
- `k`: the exact number of contiguous restore bundles

Partition the array into exactly `k` non-empty contiguous parts. Every backup must appear in exactly one part, and original order must be preserved. Let each part’s sum be the total backup size in that bundle. Your goal is to maximize the minimum part sum.

Return that optimal minimum sum.

Constraints:

- `1 <= k <= backups.length <= 200000`
- `1 <= backups[i] <= 1000000000`
- Answer fits in signed 64-bit integer

Examples:

- `backups = [7,2,5,10,8], k = 2` → `14`
- `backups = [4,4,4,4,4,4,4], k = 3` → `8`

The decisive constraint is input size: `O(nk)` or partition DP over all cut positions is too slow. The solution must exploit monotonic feasibility and run near linear time per candidate.

## 🪜 How to Solve This
1. Read the objective carefully → we are not minimizing the largest part; we are maximizing the smallest part sum across exactly `k` contiguous parts.

2. “Maximize a minimum” is a strong signal for binary search on the answer. Ask: if I guess a target minimum sum `S`, can I check whether it is feasible?

3. For a fixed `S`, greedily scan left to right and cut a bundle as soon as its running sum reaches at least `S`. Why? Earlier cuts leave more elements available for later bundles, so this strategy maximizes how many valid bundles we can form.

4. That gives a monotone predicate:
   - if we can form at least `k` bundles with sum `>= S`, then `S` is feasible;
   - if not, any larger `S` is also infeasible.

5. Once monotonicity is established, binary search `S` between `1` and `sum(backups)`.

6. The subtle point is “exactly `k`” versus “at least `k`.” If the greedy pass can form at least `k` valid bundles, we can merge adjacent valid bundles until exactly `k` remain; merged sums only increase, so feasibility is preserved.

That chain gets you from combinatorial partitioning to `O(n log sum)`.

## 🧩 Algorithm Walkthrough
1. **Define the search space using Binary Search on Answer.**  
   Let `low = 1` and `high = sum(backups)`. We are searching for the maximum `S` such that the array can be split into exactly `k` contiguous parts, each with sum at least `S`. This works because feasibility is monotone in `S`.

2. **Build a linear feasibility check using a Greedy scan.**  
   Traverse `backups`, accumulating a running sum. Whenever the sum reaches or exceeds `S`, cut a bundle, increment `count`, and reset the running sum to zero. This greedy rule is correct because cutting later never helps create *more* bundles; earliest valid cuts maximize remaining capacity for future bundles.

3. **Maintain the key invariant.**  
   After processing any prefix, `count` is the maximum number of valid bundles with minimum sum `S` that can be formed from that prefix. The greedy strategy preserves this invariant because every cut is made at the earliest feasible point.

4. **Interpret the result correctly.**  
   If `count >= k`, then `S` is feasible. We may have formed more than `k` bundles, but adjacent valid bundles can always be merged to reduce the count to exactly `k` without violating the minimum-sum threshold.

5. **Run the binary search.**  
   Use the standard upper-mid pattern: `mid = low + (high - low + 1) / 2`. If feasible, move `low = mid`; otherwise move `high = mid - 1`. This avoids infinite loops and converges to the largest feasible `S`.

6. **Return `low`.**  
   At termination, `low == high` and equals the optimal minimum bundle sum.

## 📊 Worked Example
Take `backups = [7,2,5,10,8]`, `k = 2`.

Test `S = 15`:

| Index | Value | Running Sum | Action | Bundles Formed |
|---|---:|---:|---|---:|
| 0 | 7  | 7  | continue | 0 |
| 1 | 2  | 9  | continue | 0 |
| 2 | 5  | 14 | continue | 0 |
| 3 | 10 | 24 | cut bundle `[7,2,5,10]` | 1 |
| 4 | 8  | 8  | end | 1 |

Only 1 valid bundle is formed, so `15` is infeasible.

Test `S = 14`:

| Index | Value | Running Sum | Action | Bundles Formed |
|---|---:|---:|---|---:|
| 0 | 7  | 7  | continue | 0 |
| 1 | 2  | 9  | continue | 0 |
| 2 | 5  | 14 | cut `[7,2,5]` | 1 |
| 3 | 10 | 10 | continue | 1 |
| 4 | 8  | 18 | cut `[10,8]` | 2 |

Now we can form 2 bundles, so `14` is feasible. Binary search therefore keeps `14` and rejects `15`, giving the final answer `14`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log T)`, where `n = backups.length` and `T = sum(backups)`. Each binary-search step runs one linear greedy pass, and the number of steps is `log2(T)`, at most about 48 for 64-bit sums. This scales cleanly to millions of elements; it is completely infeasible to enumerate partitions at billion-scale search spaces.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few counters and a running sum; no DP table or prefix structure is required. Space cannot be meaningfully reduced further, though using 64-bit integers everywhere is mandatory to avoid overflow.

## 💡 Key Takeaways
- If the problem says “maximize the minimum” and asks only for the value, not the partition itself, consider binary search on the answer.
- If feasibility for a guessed threshold can be checked in one pass and smaller thresholds remain feasible, you likely have a monotone predicate.
- The feasibility test must count whether you can form **at least** `k` valid bundles, not exactly `k`; exactness is recovered by merging.
- Use 64-bit arithmetic for `sum`, `mid`, and running totals; `n * backups[i]` easily exceeds 32-bit range.
- In production systems, this pattern turns combinatorial allocation problems into predictable control loops: define a monotone service-level threshold, then search it efficiently.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: dual objective — minimize the maximum subarray sum for exactly `k` parts. Same binary-search pattern, but the feasibility predicate flips direction.
- **Maximize minimum sweetness / load / segment score with cuts**: same structure, but often phrased as making `k` cuts to create `k+1` segments, which changes counting and off-by-one handling.
- **Partition with additional constraints**: require bounded segment length, weighted penalties, or negative values. These break the simple greedy feasibility check and may force DP or more complex monotone optimization.