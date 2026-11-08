# Maximum Cooling Gap for Server Maintenance

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Graph Coloring

---

## 🗂 Problem Overview
Given strictly increasing rack positions, assign every server to one of two maintenance crews so that within each crew, consecutive assigned servers are separated by at least `D`. The goal is to maximize `D`. For any candidate `D`, this becomes a feasibility question: can the ordered positions be 2-colored without placing two too-close servers in the same color chain? The challenge is scale: `n` is up to `2e5`, so quadratic grouping or backtracking is not viable.

## 🌍 Engineering Impact
This pattern shows up whenever a system must maximize a safety margin under a binary assignment constraint. Examples include scheduling noisy jobs across two maintenance windows, partitioning thermally sensitive hardware across cooling domains, assigning events to replicated pipelines with minimum spacing, or separating retries in distributed rate-limiters to avoid burst coupling. At scale, brute-force search collapses because local assignment choices interact globally. The binary-search-plus-feasibility pattern turns an optimization problem into a monotone decision problem, which is exactly the move that makes large-instance planning tractable and operationally predictable.

## 🔍 Problem Statement
You are given `positions`, a strictly increasing array of length `n` (`2 <= n <= 2 * 10^5`), where `0 <= positions[i] <= 10^18`. Every server must be assigned to exactly one of two crews, `A` or `B`. For a chosen value `D`, each crew is valid only if every pair of consecutive servers assigned to that crew differs by at least `D`.

The task is to return the maximum feasible `D`.

Examples:

- `positions = [1, 2, 4, 8]` → `3`  
  One valid split: `A = [1, 4, 8]`, `B = [2]`. `D = 4` is impossible.

- `positions = [0, 3, 5, 9, 14]` → `5`  
  One valid split: `A = [0, 5, 14]`, `B = [3, 9]`. `D = 6` is impossible.

The key constraint is `n = 2e5`: any approach that explicitly explores partitions is dead on arrival. The solution must exploit monotonicity and linear-time feasibility checking.

## 🪜 How to Solve This
1. Read the objective → we are maximizing a minimum gap. That is a classic signal for **binary search on the answer**.
2. Ask the decision version → for a fixed `D`, can we assign each position to one of two crews so that same-crew consecutive gaps are at least `D`?
3. Reframe the constraint → if two positions are less than `D` apart, they cannot go to the same crew if no same-color server appears between them. On a sorted line, this creates structured local conflicts rather than arbitrary graph complexity.
4. Track only what matters → when processing positions left to right, the future only cares about the most recent server assigned to each crew. Earlier servers are irrelevant once they are far enough away.
5. That suggests a greedy state machine → maintain the latest position used by crew `A` and crew `B`; place each new server into any crew whose last assigned position is at least `D` away.
6. But naive greedy can fail if it makes the wrong choice. The fix is to maintain the reachable pair-state compactly, exploiting order to prune dominated states in linear time.
7. Once feasibility is monotone in `D`, binary search the largest feasible value.

## 🧩 Algorithm Walkthrough
1. **Pattern selection: Binary Search on Answer + Greedy Feasibility DP.**  
   Feasibility is monotone: if distance `D` is achievable, every smaller distance is also achievable. That immediately gives the outer `O(log range)` search.

2. **Define the feasibility state.**  
   Process positions from left to right. After assigning the first `i` servers, a state is the pair `(lastA, lastB)`: the last position assigned to crew `A` and crew `B`. A new position `x` can go to `A` iff `x - lastA >= D`, and similarly for `B`.

3. **Exploit symmetry and dominance.**  
   Sort each state so `lastA <= lastB`. If two states have the same larger endpoint but one has a smaller smaller-endpoint, the latter dominates: it leaves at least as much room for future assignments. This collapses the frontier to constant-size structure rather than exponential branching.

4. **Linear feasibility scan.**  
   For each position `x`, update the frontier by trying to place `x` into either crew wherever legal, then prune dominated states. Because positions are ordered and dominance is total in this 1D setting, the frontier remains `O(1)` in size, so the whole check is `O(n)`.

5. **Invariant.**  
   After processing prefix `0..i`, the frontier contains exactly the non-dominated reachable `(lastA, lastB)` pairs for that prefix. No pruned state can lead to a solution that a retained state cannot also realize.

6. **Binary search bounds.**  
   Lower bound is `0`. Upper bound can be `positions[n-1] - positions[0] + 1` as an exclusive bound. Use upper-mid to search for the maximum feasible `D`.

7. **Why this abstraction is right.**  
   This is not general graph coloring; the sorted line induces interval-like structure. The right mental model is a constrained two-lane packing problem, where dominance pruning makes the decision procedure linear.

## 📊 Worked Example
Take `positions = [1, 2, 4, 8]` and test `D = 3`.

Track reachable non-dominated states as sorted pairs `(last1, last2)`; use `-∞` for an unused crew.

| Step | x | Reachable states after processing x |
|---|---:|---|
| 0 | 1 | `(-∞, 1)` |
| 1 | 2 | `(1, 2)` |
| 2 | 4 | `(2, 4)` from placing `4` with crew of `1`; `(1, 4)` is dominated |
| 3 | 8 | `(4, 8)` and `(2, 8)`; both valid frontier states |

Since the frontier is non-empty at the end, `D = 3` is feasible.

Now test `D = 4`:

- After `1`: `(-∞, 1)`
- After `2`: `(1, 2)`
- After `4`: no legal placement. `4 - 1 < 4` and `4 - 2 < 4`

So `D = 4` is infeasible, and the optimum is `3`.

## ⏱ Complexity Analysis
### Time Complexity
Each feasibility check is `O(n)` because every position updates a constant-size frontier of non-dominated states. Binary search over the answer range adds a `log(maxPosition - minPosition)` factor, so total time is `O(n log R)`, with `R <= 1e18`. At `10^6` elements this remains practical; at `10^9`, even linear scans become the bottleneck.

### Space Complexity
The feasibility check stores only a constant number of frontier states plus loop variables, so auxiliary space is `O(1)`. You cannot asymptotically reduce below that. The trade-off is conceptual complexity: the constant-space frontier relies on a careful dominance argument rather than a more obvious but larger DP table.

## 💡 Key Takeaways
- If a problem asks for the **largest minimum feasible value**, immediately test whether the decision version is monotone; that is the binary-search-on-answer trigger.
- When constraints live on a **sorted 1D domain**, many apparent graph-coloring problems collapse into interval-like greedy or DP structure with strong dominance pruning.
- Use an **exclusive high bound** in binary search and upper-mid calculation to avoid infinite loops when searching for the maximum feasible `D`.
- In the feasibility check, normalize crew state ordering; failing to canonicalize `(lastA, lastB)` vs `(lastB, lastA)` quietly duplicates states and breaks pruning.
- The transferable design insight is to convert expensive global optimization into a cheap monotone predicate plus a tight state summary; that is a recurring scale pattern in schedulers, placement engines, and admission-control systems.

## 🚀 Variations & Further Practice
- Extend from **2 crews to `k` crews**: the monotone search still holds, but the feasibility state no longer stays constant-size; the harder part is finding a compact representation or proving one does not exist.
- Add **capacity constraints per crew**: now assignment must satisfy both spacing and load balancing, turning the feasibility check into a richer DP or flow-like problem.
- Replace the line with a **general graph of conflicts**: the decision problem becomes graph coloring under threshold-induced edges, and the clean 1D dominance structure disappears.