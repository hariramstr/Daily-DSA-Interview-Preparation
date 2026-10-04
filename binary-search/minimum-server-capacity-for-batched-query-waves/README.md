# Minimum Server Capacity for Batched Query Waves

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given a chronological array `requests`, split it into at most `k` contiguous waves so that each wave’s total queries do not exceed some server capacity `C`. The goal is to return the minimum integer `C` that makes such a partition possible. The difficulty is that minutes cannot be reordered or split, so this is not a balancing problem; it is a constrained partitioning problem over contiguous ranges with a large search space.

## 🌍 Engineering Impact
This pattern shows up in systems that batch ordered work under hard per-batch limits: streaming ingestion windows, log compaction segments, search indexing shards, CI job bundling, and deployment wave planning. The operational question is rarely “can we process everything?” but “what is the minimum safe capacity that preserves ordering and limits coordination overhead?” Without the right approach, teams either overprovision capacity or run expensive simulation loops over huge ranges. The binary-search-on-answer pattern turns capacity planning into a predictable decision procedure, which matters when timelines are large, traffic spikes are uneven, and recomputation must stay cheap enough for planning loops and autoscaling controls.

## 🔍 Problem Statement
You are given an array `requests` where `requests[i]` is the number of queries arriving in minute `i`, and an integer `k` representing the maximum number of contiguous deployment waves allowed. Each wave must cover a contiguous block of minutes, and the sum of requests assigned to a wave must be at most capacity `C`. A minute cannot be split across waves.

Return the smallest integer `C` such that the full array can be partitioned into at most `k` valid waves.

Constraints:
- `1 <= requests.length <= 200000`
- `1 <= requests[i] <= 1000000000`
- `1 <= k <= requests.length`
- Answer fits in signed 64-bit integer

Examples:
- `requests = [7,2,5,10,8], k = 2` → `18`
- `requests = [1,4,4,3,2], k = 3` → `5`

The key constraint is scale: with up to 200k elements and large values, enumerating capacities or trying all partitions is infeasible.

## 🪜 How to Solve This
1. Read the problem → notice the partition must preserve order and use contiguous groups. That rules out sorting or arbitrary balancing.

2. Ask what makes a capacity valid → for a fixed `C`, the question becomes: can I scan left to right and form waves whose sums stay `<= C`, using at most `k` waves?

3. Notice the greedy check is forced → if adding the next minute would exceed `C`, you must start a new wave. Delaying that split is impossible, and splitting earlier never helps reduce wave count.

4. Observe monotonicity → if capacity `C` works, any larger capacity also works. If `C` fails, any smaller capacity fails. That gives a sorted true/false search space.

5. Once the answer space is monotonic, binary search the minimum feasible `C`.

6. Bound the search range tightly:
   - lower bound = `max(requests)` because every minute must fit somewhere
   - upper bound = `sum(requests)` because one wave can hold everything

7. Each feasibility check is linear, and binary search over a 64-bit numeric range is logarithmic, yielding the expected `O(n log S)` solution.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Binary Search on Answer + Greedy Feasibility Check.**  
   We are not searching an index; we are searching the minimum numeric capacity that satisfies a monotonic predicate. That is the defining signal for binary search on answer.

2. **Initialize search bounds.**  
   Set `lo = max(requests)` and `hi = sum(requests)`.  
   Why correct: any valid capacity must fit the largest single minute, and total sum is always sufficient with one wave.  
   Invariant: the true answer lies in `[lo, hi]`.

3. **Define `canFit(C)`.**  
   Scan `requests` left to right, maintaining `currentSum` for the active wave and `wavesUsed`. If `currentSum + requests[i] <= C`, extend the current wave; otherwise start a new wave with that minute.  
   Why correct: for fixed `C`, this greedy strategy minimizes the number of waves because it packs each wave as much as possible before splitting.  
   Invariant: after processing prefix `0..i`, `wavesUsed` is the minimum waves needed for that prefix under capacity `C`.

4. **Use the predicate in binary search.**  
   Compute `mid`. If `canFit(mid)` uses at most `k` waves, record it as feasible and move left (`hi = mid`). Otherwise move right (`lo = mid + 1`).  
   Why correct: feasibility is monotonic in `C`.

5. **Terminate when `lo == hi`.**  
   At convergence, the interval has collapsed to the smallest feasible capacity, which is the required answer.

## 📊 Worked Example
Example: `requests = [7,2,5,10,8]`, `k = 2`

Binary search range:
- `lo = 10`
- `hi = 32`

Check `C = 21`:

| Minute | Request | Current Wave Sum | Waves Used | Action |
|---|---:|---:|---:|---|
| 0 | 7  | 7  | 1 | add to current wave |
| 1 | 2  | 9  | 1 | add |
| 2 | 5  | 14 | 1 | add |
| 3 | 10 | 10 | 2 | exceed 21, start new wave |
| 4 | 8  | 18 | 2 | add |

`21` is feasible.

Check `C = 15`:
- `[7,2,5] = 14`
- next `10` starts wave 2
- `8` cannot join `10`, so wave 3 is required  
Fails because `3 > k`.

Continuing binary search:
- `18` works
- `17` fails

Therefore the minimum feasible capacity is `18`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `n = requests.length` and `S = sum(requests) - max(requests) + 1` is the numeric search range. Each binary-search step runs one linear feasibility scan. In practice this scales well: even for very large arrays, the logarithmic factor stays small, unlike partition DP or brute-force capacity enumeration.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few running counters and search bounds; no extra arrays, heaps, or DP tables are required. Space cannot be meaningfully reduced further without changing the input representation.

## 💡 Key Takeaways
- If the problem asks for the **minimum numeric limit** such that a partitioning or scheduling constraint becomes possible, check for a monotonic feasibility predicate.
- If items must remain **contiguous and in order**, greedy packing during validation is often the right first test before considering DP.
- Use `max(requests)` as the lower bound, not `0` or `min(requests)`; otherwise the search includes impossible capacities and obscures correctness.
- Count waves carefully: initialize with one active wave during the scan, or handle the empty/current-wave transition consistently to avoid off-by-one errors.
- In production planning systems, this pattern is a strong fit when you need the smallest safe capacity under ordering constraints without paying the cost of exhaustive simulation.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core problem; useful for recognizing the canonical binary-search-on-answer formulation.
- **Ship Packages Within D Days**: similar feasibility check, but the constraint is days instead of waves; the conceptual twist is operational framing rather than algorithmic structure.
- **Minimize the maximum subarray sum with additional constraints**: e.g., exactly `k` waves, per-wave minimum size, or penalties for cuts; these variants often break the simple greedy predicate and push the solution toward DP or parametric optimization.