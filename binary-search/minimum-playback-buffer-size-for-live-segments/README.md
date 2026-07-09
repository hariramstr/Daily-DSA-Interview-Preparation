# Minimum Playback Buffer Size for Live Segments

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an array of segment sizes and a limit of at most `k` contiguous playback sessions, compute the smallest fixed buffer size that can support the full stream in order. Each session is a contiguous block whose total size must not exceed that buffer. The challenge is that `n` can reach `100000`, so brute-force partitioning is not viable; the solution must exploit structure in the search space.

## 🌍 Engineering Impact
This pattern shows up anywhere a sequential workload must be partitioned under a fixed capacity budget: live video chunking, batch sizing in streaming pipelines, log compaction windows, shard rebalancing with locality constraints, and CI/test scheduling where jobs must remain ordered. At scale, naive trial partitioning explodes combinatorially, while overprovisioning wastes memory, bandwidth, or compute reservation. The binary-search-on-answer pattern turns a hard optimization problem into repeated linear feasibility checks, which is exactly the kind of trade-off production systems rely on: predictable runtime, monotonic decision boundaries, and easy reasoning about worst-case behavior.

## 🔍 Problem Statement
You are given `segments`, an array of `n` positive integers where `segments[i]` is the size of the `i`th live segment in megabytes, and an integer `k`. Partition the array into at most `k` contiguous parts such that the largest part sum is as small as possible. Return that minimum possible maximum sum, interpreted here as the minimum fixed playback buffer size.

Constraints:
- `1 <= n <= 100000`
- `1 <= segments[i] <= 1000000000`
- `1 <= k <= n`
- Answer fits in signed 64-bit integer

Examples:
- `segments = [8, 3, 5, 7, 2], k = 3` → `9`
- `segments = [4, 4, 4, 4], k = 2` → `8`

Edge conditions matter: a buffer smaller than `max(segments)` is impossible, and with large values the total sum requires 64-bit arithmetic. The `100000` upper bound rules out dynamic programming over all partitions.

## 🪜 How to Solve This
1. Read the objective carefully → we are not asked for the partition itself first; we are asked for the minimum possible maximum session sum.

2. Notice the decision version is easier → “Can buffer size `x` support the stream in at most `k` sessions?” That is much simpler than directly minimizing.

3. Observe monotonicity → if buffer `x` works, then any larger buffer also works. If `x` fails, any smaller buffer fails. That immediately suggests binary search on the answer.

4. Define the search range:
   - Lower bound = largest single segment, because every segment must fit.
   - Upper bound = total sum, because one session containing everything is always valid.

5. For a candidate buffer `x`, greedily scan left to right:
   - Keep adding segments to the current session while the sum stays `<= x`.
   - When the next segment would exceed `x`, start a new session.

6. Why greedy works → for a fixed `x`, packing each session as full as possible minimizes the number of sessions needed. If even this strategy needs more than `k`, then `x` is infeasible.

7. Binary search the smallest feasible `x`.

## 🧩 Algorithm Walkthrough
1. **Establish bounds using array properties.**  
   Set `low = max(segments)` and `high = sum(segments)`. This is correct because no valid solution can be below the largest element, and the total sum is always feasible as one contiguous session. The invariant is: the answer always lies in `[low, high]`.

2. **Apply the pattern: Binary Search on Answer.**  
   Instead of searching indices, search candidate buffer sizes. At each step compute `mid = low + (high - low) / 2`. The abstraction fits because feasibility is monotonic: larger capacities never make the problem harder.

3. **Run a greedy feasibility check for `mid`.**  
   Scan the array once, maintaining `currentSum` for the active session and `sessionsUsed`. Add the next segment if it fits; otherwise start a new session with that segment. This maintains the invariant that every completed session is as full as possible without exceeding `mid`.

4. **Why the greedy check is correct.**  
   For a fixed capacity, delaying a split cannot increase the number of sessions; splitting earlier only wastes remaining capacity. Therefore the greedy pass yields the minimum sessions required under that capacity. If greedy needs `> k` sessions, no other contiguous partitioning can do better for the same `mid`.

5. **Shrink the search space.**  
   If `mid` is feasible, record that any answer is `<= mid`, so move `high = mid`. Otherwise move `low = mid + 1`. The invariant becomes: `low` is always infeasible-or-unknown below answer, `high` is always feasible.

6. **Terminate when bounds converge.**  
   When `low == high`, you have the smallest feasible buffer size. Use 64-bit integers throughout because both the sum and midpoint can exceed 32-bit range.

## 📊 Worked Example
Example: `segments = [8, 3, 5, 7, 2]`, `k = 3`

Initial bounds: `low = 8`, `high = 25`

| mid | Greedy sessions formed | Sessions count | Feasible? |
|---|---|---:|---|
| 16 | `[8,3,5]`, `[7,2]` | 2 | Yes |
| 12 | `[8,3]`, `[5,7]`, `[2]` | 3 | Yes |
| 10 | `[8]`, `[3,5]`, `[7,2]` | 3 | Yes |
| 9  | `[8]`, `[3,5]`, `[7,2]` | 3 | Yes |
| 8  | `[8]`, `[3,5]`, `[7]`, `[2]` | 4 | No |

Binary search narrows as follows:
- `16` works → search left
- `12` works → search left
- `10` works → search left
- `9` works → search left
- `8` fails → search right

Converged answer: `9`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log S)`, where `S = sum(segments) - max(segments) + 1` is the numeric search space. Each binary-search step runs one linear greedy pass. In practice this is efficient even for `n = 10^6`; the logarithmic factor stays small. At `10^9` elements, the linear scan dominates and data movement becomes the real bottleneck.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only running totals, bounds, and a session counter; no extra arrays or DP tables are needed. Space cannot meaningfully be reduced further without changing the input model, though streaming the input would trade random access for one-pass processing constraints.

## 💡 Key Takeaways
• If the problem asks for a minimum feasible capacity and feasibility becomes easier as capacity grows, think binary search on the answer.  
• If partitions must remain contiguous and you only need to test a candidate threshold, a greedy left-to-right packing pass is a strong signal.  
• The lower bound is `max(segments)`, not `0` or `min(segments)`; starting lower only adds useless iterations.  
• Use 64-bit arithmetic for `sum`, `mid`, and running session totals; 32-bit overflow silently breaks correctness on valid inputs.  
• In production systems, this pattern is valuable because it separates optimization from validation: a monotonic feasibility oracle plus binary search often beats bespoke heuristics in both predictability and operability.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core problem, but framed generically; useful for recognizing the pattern independent of domain language.
- **Capacity To Ship Packages Within D Days**: same binary-search-on-capacity idea, but feasibility is expressed as days rather than partitions; the twist is mapping domain constraints to the same monotonic predicate.
- **Allocate Minimum Number of Pages / Painter’s Partition**: contiguous assignment under a minimized maximum load; the harder part is seeing that different narratives collapse to the same greedy feasibility structure.