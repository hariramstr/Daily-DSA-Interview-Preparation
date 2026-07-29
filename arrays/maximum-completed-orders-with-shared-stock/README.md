# Maximum Completed Orders with Shared Stock

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Binary Search, Greedy

---

## 🗂 Problem Overview
Given an array `stock`, choose a single uniform order size `x` such that at least `k` customer orders can be fulfilled. Each order must come entirely from one product type, but a product type may supply multiple orders, contributing `floor(stock[i] / x)` orders. Return the maximum feasible `x`, or `0` if even size `1` cannot produce `k` orders. The challenge is that `stock[i]` and `k` are large, so scanning all possible sizes is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a fixed service unit must be derived from fragmented capacity: warehouse cartonization, ad-budget slicing across campaigns, batch sizing in streaming pipelines, shard-level rate allocation, and GPU job packing where work units cannot span devices. At scale, brute-force sizing decisions collapse under large cardinalities and high capacity ranges. The key architectural move is exploiting monotonic feasibility: if a given unit size works, every smaller size also works. That enables binary search over the answer space, turning an otherwise impractical search into a predictable, low-latency decision primitive suitable for schedulers, allocators, and admission-control paths.

## 🔍 Problem Statement
You are given:

- `stock[i]`: available units of product type `i`
- `k`: required number of customer orders

Every fulfilled order must:

- use units from exactly one product type
- have the same size `x`
- allow leftover units to remain unused

A product type with `stock[i]` contributes `floor(stock[i] / x)` orders of size `x`. The goal is to find the largest `x` such that the total number of orders across all product types is at least `k`.

If `sum(stock) < k`, then even order size `1` is impossible, so return `0`.

**Constraints**
- `1 <= stock.length <= 100000`
- `1 <= stock[i] <= 1000000000`
- `1 <= k <= 1000000000000`

**Examples**
- `stock = [8, 5, 6], k = 5` → `3`
- `stock = [2, 3], k = 6` → `0`

The decisive constraint is the huge search space for `x`: it can range up to `10^9`, which rules out linear probing over candidate sizes.

## 🪜 How to Solve This
1. Read the requirement carefully → we are not assigning specific orders; we are only asking whether a candidate order size `x` is feasible.

2. For any fixed `x`, feasibility is easy to compute:
   - each product type contributes `stock[i] // x` orders
   - total feasible orders is `sum(stock[i] // x)`

3. Notice the monotonic property:
   - if size `x` works, then every smaller size also works
   - if size `x` fails, every larger size also fails

4. A monotonic yes/no condition over an integer range is a binary-search-on-answer problem.

5. Search `x` between `1` and `max(stock)`.
   - Midpoint `m` asks: “Can we produce at least `k` orders of size `m`?”
   - If yes, try larger sizes.
   - If no, shrink the range.

6. Add one early rejection:
   - if total units across all types is less than `k`, return `0`

7. The greedy aspect is implicit: for a chosen size, taking all possible full orders from each type is always optimal because partial leftovers cannot help any other type.

## 🧩 Algorithm Walkthrough
1. **Check global feasibility at size `1`.**  
   Compute `totalUnits = sum(stock)`. If `totalUnits < k`, return `0`. This is correct because size `1` is the smallest possible order; if that fails, no larger size can succeed. Invariant: after this check, at least one candidate size may be feasible.

2. **Define the feasibility function.**  
   For a candidate size `x`, compute `orders = sum(stock[i] // x)`. Return whether `orders >= k`. This works because each product type is independent and orders cannot mix types. Invariant: feasibility depends only on aggregate full-order counts, not on assignment order.

3. **Recognize the pattern: Binary Search on Answer.**  
   The predicate `feasible(x)` is monotonic decreasing as `x` grows. Larger order sizes can only reduce or preserve `stock[i] // x`, never increase it. This monotonicity is exactly why binary search is the right abstraction.

4. **Initialize bounds.**  
   Set `lo = 1`, `hi = max(stock)`, `best = 0`. No order size can exceed the largest stock bucket, since one order must come from a single type.

5. **Binary search for the maximum feasible size.**  
   While `lo <= hi`, test `mid = lo + (hi - lo) // 2`.  
   - If `feasible(mid)` is true, record `best = mid` and move right: `lo = mid + 1`.  
   - Otherwise move left: `hi = mid - 1`.  
   Invariant: `best` is always feasible; the remaining search interval contains only sizes that may improve it.

6. **Use early termination inside feasibility.**  
   While summing `stock[i] // x`, stop once the running total reaches `k`. This avoids unnecessary work and protects against oversized accumulation in fixed-width integer environments.

## 📊 Worked Example
Take `stock = [8, 5, 6]`, `k = 5`.

| Step | `lo` | `hi` | `mid` | Orders from `[8,5,6]` | Total | Feasible? | Action |
|---|---:|---:|---:|---|---:|---|---|
| Start | 1 | 8 | - | - | - | - | Search begins |
| 1 | 1 | 8 | 4 | `[2,1,1]` | 4 | No | `hi = 3` |
| 2 | 1 | 3 | 2 | `[4,2,3]` | 9 | Yes | `best = 2`, `lo = 3` |
| 3 | 3 | 3 | 3 | `[2,1,2]` | 5 | Yes | `best = 3`, `lo = 4` |

Loop ends because `lo > hi`.

Result: `3`.

Why this is maximal:
- size `3` yields exactly `5` orders, so it works
- size `4` yields only `4` orders, so every larger size also fails by monotonicity

That “last true” structure is the core binary-search target.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = stock.length` and `M = max(stock)`. Each binary-search step scans the array once to evaluate feasibility, and there are `log M` such steps. With `M <= 10^9`, that is about 30 iterations, so the approach remains practical even when `n` reaches `10^5` or higher-scale analogs.

### Space Complexity
`O(1)` auxiliary space beyond the input array. The algorithm stores only bounds, counters, and the running answer. Space cannot meaningfully be reduced further; the main trade-off is using wider integer types for correctness rather than additional memory structures.

## 💡 Key Takeaways
- If the problem asks for the **maximum feasible uniform value** and feasibility can be checked independently, look for binary search on the answer.
- If a candidate value creates a **monotonic yes/no boundary**—works for all smaller values, fails for all larger ones—you likely have the right search structure.
- Use 64-bit arithmetic for `k`, total units, and running order counts; `int` overflows easily under the given constraints.
- Be careful with the binary-search variant: this is a **find the last true** problem, so on success move right and preserve `best`.
- In production allocation systems, monotonic feasibility predicates are valuable because they separate policy search from capacity evaluation, making scaling behavior predictable and testable.

## 🚀 Variations & Further Practice
- Allow each order to combine units from multiple product types. The monotonic predicate still exists, but the feasibility function changes fundamentally because fragmentation no longer matters.
- Require **exactly** `k` orders instead of at least `k`. This introduces edge cases where leftover capacity and divisibility matter, and feasibility may no longer be a simple monotonic count check.
- Add per-product-type caps or costs for opening a type. Now the problem mixes sizing with constrained selection, pushing it toward knapsack-style optimization or parametric search with richer state.