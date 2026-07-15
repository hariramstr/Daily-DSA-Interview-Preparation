# Maximum Starter Batch for Subscription Trials

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given warehouse inventories `kits[]` and a required number of customer batches `m`, determine the largest integer batch size such that at least `m` equal-sized, non-empty batches can be produced. Each batch must come entirely from one warehouse, but a warehouse may be split into multiple batches. The challenge is scale: inventories are large, `m` can reach `10^12`, and testing every possible batch size is too expensive. The key is that feasibility changes monotonically as batch size grows.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize a uniform allocation under fragmented capacity: distributed rate-limiters assigning equal token budgets, media/CDN systems slicing bandwidth into fixed delivery units, warehouse and fulfillment planning, and multi-tenant schedulers carving identical resource bundles from heterogeneous nodes. At small scale, brute force is tolerable; at production scale, it collapses under large search spaces and repeated feasibility checks. Recognizing monotonic feasibility enables binary search over the answer space, turning an intractable scan into a predictable, bounded decision procedure that composes well with admission control, capacity planning, and autoscaling logic.

## 🔍 Problem Statement
You are given an integer array `kits` where `kits[i]` is the number of starter kits available in warehouse `i`, and an integer `m` representing how many customer batches must be created.

Each batch must:
- contain exactly the same number of kits,
- be non-empty,
- be assembled from a single warehouse only.

A warehouse may be split into multiple batches, as long as the total kits used from that warehouse does not exceed its inventory.

Return the maximum possible number of kits per batch. If creating `m` non-empty batches is impossible, return `0`.

**Constraints**
- `1 <= kits.length <= 100000`
- `1 <= kits[i] <= 1000000000`
- `1 <= m <= 1000000000000`
- Answer fits in a 32-bit signed integer

**Examples**
- `kits = [9, 7, 5], m = 5` → `3`
- `kits = [2, 4, 6], m = 7` → `1`

The decisive constraint is the huge answer space: candidate batch sizes can be as large as `10^9`, so linear probing over sizes is not viable.

## 🪜 How to Solve This
1. Start from the direct question: “What is the largest batch size `x` such that we can still make at least `m` batches?”
2. For any fixed `x`, the number of batches warehouse `i` can produce is `kits[i] / x` using integer division. Summing those values tells us whether `x` is feasible.
3. Notice the monotonic behavior: if size `x` works, then every smaller size also works; if `x` fails, every larger size fails.
4. Monotonic feasibility is the signal for **binary search on the answer**, not binary search on the array.
5. Define a search range:
   - low = `1`
   - high = `max(kits)` or tighter, `sum(kits) / m`
6. Repeatedly test the midpoint:
   - enough batches → midpoint is valid, try larger
   - not enough → midpoint is too large, try smaller
7. Keep the best valid size seen.
8. Handle the impossible case early: if total kits `< m`, you cannot form `m` non-empty batches, so return `0`.

This is the standard “maximize a value under a monotone predicate” pattern.

## 🧩 Algorithm Walkthrough
1. **Check global feasibility.**  
   Compute `total = sum(kits)`. If `total < m`, even batch size `1` cannot produce `m` non-empty batches, so return `0`. This avoids unnecessary search and establishes the baseline invariant that a solution may exist only when total capacity is sufficient.

2. **Define the search space.**  
   Use **Binary Search on Answer** over batch sizes. Set `left = 1` and `right = min(max(kits), total / m)` if you want a tighter upper bound. Any valid answer must lie in this interval.

3. **Evaluate a candidate size.**  
   For a midpoint `mid`, compute  
   `count = Σ floor(kits[i] / mid)`.  
   This is the maximum number of batches of size `mid` obtainable without mixing warehouses. The invariant is: `count >= m` means `mid` is feasible.

4. **Exploit monotonicity.**  
   If `mid` is feasible, record it and move right: larger sizes might still work. If infeasible, move left: any larger size will also fail. This preserves the binary-search invariant that all feasible values are on one side of the boundary and all infeasible values on the other.

5. **Terminate at the maximum feasible size.**  
   Continue until `left > right`. The stored best value, or equivalently the final `right`, is the maximum valid batch size.

6. **Implementation detail that matters at scale.**  
   Use 64-bit arithmetic for `total`, `m`, and the running batch count. Inputs fit in 32-bit individually, but aggregate sums and counts do not.

## 📊 Worked Example
Take `kits = [9, 7, 5]`, `m = 5`.

| Step | left | right | mid | batches from 9,7,5 | total batches | feasible? |
|------|------|-------|-----|--------------------|---------------|-----------|
| 1 | 1 | 4 | 2 | 4, 3, 2 | 9 | yes |
| 2 | 3 | 4 | 3 | 3, 2, 1 | 6 | yes |
| 3 | 4 | 4 | 4 | 2, 1, 1 | 4 | no |

Why is `right = 4` initially? `total = 21`, so `total / m = 4`, which is a tighter upper bound than `max(kits) = 9`.

Trace:
1. `mid = 2` works, so search larger sizes.
2. `mid = 3` also works, so search larger again.
3. `mid = 4` fails, so move left.
4. Search ends with maximum feasible size `3`.

The boundary is clear: sizes `1..3` are feasible, `4+` are not.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log U)`, where `n = kits.length` and `U` is the search upper bound, typically `max(kits)` or `sum(kits)/m`. Each binary-search step scans the array once to count batches. In practice, `log U` is at most about 31 for 32-bit-sized answers, so this remains stable even when `n` reaches `10^6`; a brute-force scan over `10^9` candidate sizes would be non-starter.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few scalar variables for bounds, totals, and the running count. Space cannot be meaningfully reduced further without changing the execution model; the trade-off surface is in time, not memory.

## 💡 Key Takeaways
- If the problem asks for the **maximum uniform value** and feasibility for a candidate value can be checked independently, look for binary search on the answer.
- A monotone predicate is the tell: “if size `x` works, all smaller sizes work” is the exact signal to stop thinking brute force.
- Use 64-bit integers for `sum(kits)`, `m`, and accumulated batch counts; overflow is easy here even though the final answer fits in 32 bits.
- Be explicit about the search invariant and midpoint updates; the common bug is returning the first failing value instead of the last successful one.
- In production systems, this pattern matters because it separates **capacity evaluation** from **search strategy**, letting you optimize the predicate while keeping the decision framework unchanged.

## 🚀 Variations & Further Practice
- Allow batches to combine kits from multiple warehouses. The monotone search still applies, but the feasibility function changes fundamentally because per-warehouse floor division is no longer the right model.
- Add a per-warehouse setup cost or cap on the number of batches each warehouse may emit. The predicate remains monotone, but feasibility becomes a constrained allocation problem rather than a pure sum of quotients.
- Optimize for the minimum number of warehouses used while still producing `m` batches of size `x`. This introduces a second objective and often requires sorting, greedy selection, or nested search.