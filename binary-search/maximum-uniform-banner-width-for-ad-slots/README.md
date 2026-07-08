# Maximum Uniform Banner Width for Ad Slots

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Greedy

---

## 🗂 Problem Overview
Given slot widths and a target number of banners `k`, find the largest integer banner width `w` such that cutting every slot into pieces of width `w` yields at least `k` banners total. Each slot contributes `floor(slots[i] / w)`, and leftover width is discarded. The challenge is scale: slot widths can be up to `10^9`, so checking every possible width is infeasible. The solution depends on exploiting a monotonic feasibility condition.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize a uniform allocation under divisibility and capacity constraints: shard sizing in storage systems, batch sizing in streaming pipelines, chunk sizing for media transcoding, and quota partitioning in distributed rate-limiters. The key production concern is avoiding search over a massive numeric domain when feasibility is monotonic. Without this approach, control loops and planning jobs degrade into brute-force scans that collapse under large capacity ranges. With it, you get predictable latency, simple correctness reasoning, and a reusable decision primitive for capacity planning and resource normalization.

## 🔍 Problem Statement
You are given an integer array `slots` where `slots[i]` is the width of the `i`-th ad slot, and an integer `k` representing the required number of banners. Choose a single integer width `w` for all banners. Each slot may be split into multiple banners of width exactly `w`, contributing `floor(slots[i] / w)` banners; leftover width is wasted and cannot be merged across slots.

Return the maximum valid `w` such that the total number of banners across all slots is at least `k`. If even width `1` cannot produce `k` banners, return `0`.

Constraints:
- `1 <= slots.length <= 100000`
- `1 <= slots[i] <= 1000000000`
- `1 <= k <= 1000000000`

Examples:
- `slots = [9, 7, 5], k = 5` → `3`
- `slots = [2, 3], k = 10` → `0`

The decisive constraint is the search space for `w`: it can be as large as `10^9`, which rules out linear probing over candidate widths.

## 🪜 How to Solve This
1. Read the requirement carefully → we are not minimizing waste or counting exact partitions; we only need the **largest width** that still produces **at least `k` banners**.

2. Ask what happens as `w` changes → if banner width increases, each slot can produce the same number or fewer banners. Total banners is therefore a monotonic non-increasing function of `w`.

3. Monotonic feasibility usually means binary search → define a predicate: “Can width `w` produce at least `k` banners?” If true for some `w`, it will also be true for every smaller width.

4. Establish the search range → the minimum meaningful width is `1`; the maximum possible width is `max(slots)`, since no banner can be wider than the widest slot.

5. For any candidate `mid`, compute `sum(slots[i] / mid)` and compare with `k` → if enough banners are produced, try a larger width; otherwise, shrink the width.

6. The target is the **rightmost feasible value** → this is the standard “maximum valid answer” binary search shape.

## 🧩 Algorithm Walkthrough
1. **Handle impossible cases implicitly via feasibility checks.**  
   Set the binary search range to `low = 1` and `high = max(slots)`. If no width is feasible, the search will never record a valid answer, and the result remains `0`. This avoids a separate pre-pass, though one is also acceptable.

2. **Use the Binary Search on Answer pattern.**  
   The abstraction is correct because feasibility is monotonic: for any width `w`, if `sum(floor(slots[i] / w)) >= k`, then every smaller width is also feasible. That gives an ordered true/false search space.

3. **Pick the midpoint and evaluate feasibility.**  
   For `mid = low + (high - low) / 2`, compute total banners by summing `slots[i] / mid` across all slots. This answers: “Can we produce at least `k` banners at width `mid`?”

4. **Maintain the invariant.**  
   - All widths `<= answer` seen so far are feasible.  
   - All widths `> high` are known infeasible.  
   If `mid` is feasible, record it as the current best and move right with `low = mid + 1`. Otherwise move left with `high = mid - 1`.

5. **Terminate when the search space collapses.**  
   When `low > high`, the last recorded feasible width is the maximum valid width. If none was recorded, return `0`.

6. **Implementation detail that matters at scale.**  
   Accumulate the banner count in 64-bit integer space. With up to `10^5` slots and widths near `1`, the sum can exceed 32-bit bounds.

## 📊 Worked Example
Example: `slots = [9, 7, 5], k = 5`

| Step | low | high | mid | banners from slots `(9,7,5)` | total | feasible? | answer |
|------|-----|------|-----|-------------------------------|-------|-----------|--------|
| 1 | 1 | 9 | 5 | `(1,1,1)` | 3 | No | 0 |
| 2 | 1 | 4 | 2 | `(4,3,2)` | 9 | Yes | 2 |
| 3 | 3 | 4 | 3 | `(3,2,1)` | 6 | Yes | 3 |
| 4 | 4 | 4 | 4 | `(2,1,1)` | 4 | No | 3 |

Trace:
1. Width `5` fails, so the maximum valid width must be smaller.
2. Width `2` works, so try larger widths.
3. Width `3` also works, improving the best answer.
4. Width `4` fails, so the search stops.

Result: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = slots.length` and `M = max(slots)`. Each binary search step scans the array once to evaluate feasibility, and there are `log M` such steps. Even when `M = 10^9`, `log2(M)` is about 30, so the approach scales cleanly while brute-force width scanning does not.

### Space Complexity
`O(1)` auxiliary space beyond the input array. The algorithm stores only scalar search bounds, a running total, and the current best answer. Space cannot be meaningfully reduced further; the only trade-off is using wider integer types for correctness under large sums.

## 💡 Key Takeaways
- If the problem asks for the **maximum/minimum numeric value** satisfying a condition, check whether feasibility over that value is monotonic.
- When the search domain is huge (`10^9`) but each candidate can be validated in linear time, think **binary search on answer**, not direct enumeration.
- Use 64-bit accumulation for the total banner count; `sum(slots[i] / w)` can overflow 32-bit integers.
- This is a **rightmost feasible** binary search: when `mid` works, move `low` right and preserve `mid` as the current answer.
- In production systems, monotonic feasibility transforms expensive planning over large numeric spaces into bounded, predictable decision loops.

## 🚀 Variations & Further Practice
- **Minimum time to produce `k` items across machines**: same binary-search-on-answer pattern, but feasibility is based on time rather than cut width; the twist is reasoning about rate aggregation instead of divisibility.
- **Maximize equal piece length with floating-point precision**: similar cutting problem, but widths are real numbers, so termination depends on epsilon handling and precision guarantees.
- **Allocate workloads across partitions with per-partition overhead**: feasibility remains monotonic, but the validation function becomes more complex because each split has a fixed cost, changing the threshold behavior.