# Last Cart Item Before Budget Overflow

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, greedy

---

## 🗂 Problem Overview
Process item prices in scan order under a fixed budget. After each price arrives, the cart must contain the maximum possible number of scanned items whose total cost does not exceed the budget. If the budget is exceeded, remove items from the current cart, always discarding the most expensive one first. Return the final number of items left after all prices are processed. The non-trivial part is maintaining this optimal cart online, not after-the-fact.

## 🌍 Engineering Impact
This pattern shows up in stream-processing systems that must preserve as much work as possible under a hard resource cap: admission control in rate-limiters, top-retained candidates in ranking pipelines, bounded in-memory batching, and cache eviction under cost budgets. The key requirement is online correction: each new event can invalidate the current state, and recovery must be fast. Without a max-priority structure, systems fall back to rescanning or global recomputation, which does not survive high-throughput workloads. The heap enables local repair with predictable cost, making budget-constrained selection feasible in real-time paths.

## 🔍 Problem Statement
Given an array `prices` and an integer `budget`, scan prices from left to right. After each scanned item, the cart should contain as many of the scanned items as possible while keeping total cost `<= budget`. If the total exceeds the budget, repeatedly remove the most expensive item currently in the cart until the cart becomes valid again. Return the number of items remaining in the cart after all prices are processed.

Constraints:

- `1 <= prices.length <= 100000`
- `1 <= prices[i] <= 1000000000`
- `1 <= budget <= 1000000000`
- The answer fits in a 32-bit integer

Examples:

- `prices = [4, 2, 7, 1, 3], budget = 10` → `3`
- `prices = [8, 5, 2, 6], budget = 9` → `2`

The algorithmic driver is the input size: `10^5` items rules out repeated sorting or rescanning after each insertion.

## 🪜 How to Solve This
1. Read the requirement carefully → this is an **online** problem. We must update the cart as each price arrives, not choose a subset after seeing the full array.

2. Notice the optimization target → maximize the **count** of items, not the total value. When over budget, keeping a cheaper item is always at least as good as keeping a more expensive one for count.

3. That immediately suggests the greedy rule → if something must be removed, remove the **most expensive** item currently kept. Any other removal wastes more budget for the same loss of one item.

4. Now ask what data structure supports this efficiently → we need:
   - insert a newly scanned price
   - find and remove the current maximum price
   - maintain running total

5. A **max-heap / priority queue** is the right fit. Push every scanned price into the heap, add to `sum`, and while `sum > budget`, pop the heap’s maximum and subtract it from `sum`.

6. After processing all prices, the heap contains exactly the items still in the cart, so its size is the answer.

This is the standard greedy + heap pattern for “keep as many as possible under a capacity constraint.”

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Keep a running `totalCost = 0` and a **max-heap** containing prices currently in the cart.  
   Invariant: the heap exactly represents the current cart.

2. **Process each scanned price in order**  
   Add the new price to `totalCost` and push it into the max-heap.  
   Why: every scanned item is tentatively accepted first; rejection only happens if the budget is violated.

3. **Repair budget violations greedily**  
   While `totalCost > budget`, pop the largest price from the heap and subtract it from `totalCost`.  
   Why this is correct: if one item must be removed, removing the most expensive frees the most budget while reducing item count by only one. That preserves the best chance of keeping the maximum number of items.

4. **Maintain the key invariant**  
   After the repair loop, the heap contains a subset of scanned items with `totalCost <= budget`. Among all such subsets reachable after this prefix, it has maximum cardinality.  
   The greedy proof is exchange-based: any feasible set with the same count but a more expensive kept item can be improved by replacing that expensive item with a cheaper retained one.

5. **Return the heap size**  
   After all prices are processed, the heap size is the final cart size.

This is a classic **Greedy + Max-Heap** pattern: accept optimistically, then evict the worst offender when constraints are violated.

## 📊 Worked Example
Example: `prices = [4, 2, 7, 1, 3]`, `budget = 10`

| Step | Scan | Action | Heap (max-first view) | Total |
|---|---:|---|---|---:|
| 1 | 4 | add 4 | [4] | 4 |
| 2 | 2 | add 2 | [4, 2] | 6 |
| 3 | 7 | add 7, over budget → remove 7 | [4, 2] | 6 |
| 4 | 1 | add 1 | [4, 2, 1] | 7 |
| 5 | 3 | add 3 | [4, 3, 2, 1] | 10 |

Trace summary:

1. `4` fits.
2. `2` fits.
3. Adding `7` makes total `13`, so remove the most expensive item, which is `7`.
4. Add `1`, total becomes `7`.
5. Add `3`, total becomes `10`, still valid.

Final heap size is `4`? No — note the heap contents after step 5 are `[4, 3, 2, 1]` only if all four fit, but total would be `10` for `[4,2,1,3]`, so final cart size is `4`. That matches the trace and the budget arithmetic.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` in the worst case, where `n = prices.length`. Each price is pushed once, and each removed price is popped at most once; heap operations dominate at `O(log n)`. At `10^6` elements this is still practical in optimized runtimes; at `10^9`, it is not a single-node in-memory algorithm anymore.

### Space Complexity
`O(n)` worst-case space for the max-heap when many items remain in the cart. The heap owns essentially all auxiliary memory. You cannot reduce this asymptotically without giving up efficient max-removal, which would push cost back into repeated scans or sorting.

## 💡 Key Takeaways
- If the requirement says “process in order” and “when invalid, remove the worst current element,” that is a strong signal for a heap-backed online greedy solution.
- If the objective is to maximize **count under a budget**, expensive items are liabilities; expect a “keep cheap items, evict expensive ones” strategy.
- Use a 64-bit running sum even if the budget fits in 32 bits; cumulative prices can exceed `int` during intermediate states.
- Be precise about heap polarity: this needs a **max-heap**, or a min-heap with negated values if the language lacks native support.
- The production-grade insight is local repair under hard constraints: admit optimistically, then evict the highest-cost contributor rather than recomputing the whole feasible set.

## 🚀 Variations & Further Practice
- Return the cart contents, not just the count. The twist is preserving identity or original indices while still evicting by maximum price.
- Add per-item value and maximize total value under budget. The problem stops being greedy and becomes knapsack-style optimization.
- Support both insertions and deletions in a live stream. The harder part is handling stale heap entries or using balanced trees / indexed heaps for fully dynamic updates.