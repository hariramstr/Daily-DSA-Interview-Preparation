# Minimum Energy to Read a Shelf of Books

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, optimization

---

## 🗂 Problem Overview
Given an array `energy`, each value is the cost of reading that book. You start before index `0` and may move to `i + 1` or `i + 2` on each step, paying only for books you actually read. The task is to compute the minimum total energy required to finish on the last book. The non-trivial part is that each choice affects future options, so greedy local decisions are not reliable.

## 🌍 Engineering Impact
This pattern shows up anywhere a system advances through ordered states with limited transition choices and additive cost: workflow engines choosing retry paths, streaming pipelines skipping expensive transforms, compiler passes selecting staged optimizations, or storage systems deciding checkpoint intervals. At scale, brute-force path enumeration explodes exponentially, while a dynamic programming formulation collapses repeated subproblems into linear work. That shift matters operationally: predictable latency, bounded memory, and simpler correctness reasoning. The broader value is recognizing when a sequential optimization problem is really “best cost to reach state `i` depends on a small fixed predecessor set.”

## 🔍 Problem Statement
You are given an integer array `energy` where `energy[i]` is the energy cost to read book `i`. You begin before the shelf, effectively at a virtual position before index `0`. From any current position, you may move forward by one or two books, meaning your first read can be book `0` or book `1` if it exists.

Each time you land on a book, you must pay its energy cost. You must finish exactly on the last book, and the goal is to minimize total energy spent.

Constraints:
- `1 <= energy.length <= 1000`
- `0 <= energy[i] <= 10^4`
- You must end on the last book

Examples:
- `energy = [4, 2, 7, 3]` → `5`
- `energy = [5, 1, 2, 10, 1]` → `4`

The key algorithmic constraint is the required linear-time solution: each position depends only on the previous one or two positions.

## 🪜 How to Solve This
1. Start from the destination, not the path. We do not need to enumerate all reading sequences; we only need the cheapest way to reach each book.

2. Ask the right subproblem: what is the minimum energy needed to land on book `i`? Once that is defined, the final answer is just the value for the last index.

3. Notice the transition rule. To reach book `i`, the previous position must have been either `i - 1` or `i - 2`. So the best cost to reach `i` is:
   `energy[i] + min(cost[i - 1], cost[i - 2])`.

4. Establish base cases carefully. Since you start before the array, reaching book `0` costs `energy[0]`, and reaching book `1` costs `energy[1]` because you may jump there directly.

5. Iterate left to right, filling the recurrence once per index. That gives linear time and avoids recomputing overlapping subproblems.

6. Since each state depends on only two prior states, the DP array is optional. A constant-space rolling formulation is enough if you only need the final answer.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Dynamic Programming over a linear sequence.**  
   This is the right abstraction because the problem asks for an optimal cumulative cost, and each state depends on a fixed, local predecessor set. That combination is a classic DP signal.

2. **Define the state.**  
   Let `dp[i]` be the minimum energy required to land on book `i`. This state is sufficient because once you know the cheapest cost to reach `i`, the path details no longer matter for future decisions.

3. **Set base cases.**  
   `dp[0] = energy[0]`.  
   If `n > 1`, `dp[1] = energy[1]`.  
   This is correct because the start position allows a direct jump to either of the first two books.

4. **Apply the recurrence.**  
   For every `i >= 2`:  
   `dp[i] = energy[i] + min(dp[i - 1], dp[i - 2])`.  
   Why correct: any valid path to `i` must come from exactly one of those two predecessor books. Choosing the cheaper predecessor preserves optimality.

5. **Maintain the invariant.**  
   After processing index `i`, `dp[0..i]` contains the true minimum cost to reach each of those books. This holds inductively because each new value is built from already-correct prior states.

6. **Return the target state.**  
   The problem requires ending on the last book, so return `dp[n - 1]`.

7. **Optimize space if needed.**  
   Because only `dp[i - 1]` and `dp[i - 2]` are needed, replace the array with two rolling variables. Same correctness, lower memory footprint, slightly less debuggability.

## 📊 Worked Example
Use `energy = [5, 1, 2, 10, 1]`.

| i | energy[i] | Computation | dp[i] |
|---|-----------|-------------|-------|
| 0 | 5 | base case | 5 |
| 1 | 1 | base case | 1 |
| 2 | 2 | `2 + min(1, 5)` | 3 |
| 3 | 10 | `10 + min(3, 1)` | 11 |
| 4 | 1 | `1 + min(11, 3)` | 4 |

Trace:
1. Reading book `0` costs `5`.
2. Reading book `1` directly from the start costs `1`.
3. Best way to reach book `2` is via book `1`, total `3`.
4. Book `3` is expensive, so even the best path there costs `11`.
5. To finish at book `4`, come from book `2`, giving `1 + 3 = 4`.

Answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time because each book is processed exactly once, and each state transition does constant work: one addition and one `min` comparison. At `10^6` elements this is still practical in most environments; at `10^9`, linear time becomes throughput-bound and likely requires partitioning or a different problem framing.

### Space Complexity
Using a DP array costs `O(n)` space, owned entirely by the `dp` structure. This can be reduced to `O(1)` by keeping only the previous two states, since the recurrence has width two. The trade-off is less visibility into intermediate states during debugging.

## 💡 Key Takeaways
- If a problem asks for a minimum cumulative cost over a sequence and each position depends on a small fixed set of earlier positions, think one-dimensional dynamic programming.
- “Can move by 1 or 2 steps” is a strong signal that the recurrence will depend on the previous two states.
- The start position is virtual, so `dp[1]` is not `energy[1] + dp[0]`; you may jump directly to book `1`.
- For `n = 1`, return `energy[0]` immediately; base-case handling is where most off-by-one bugs appear.
- In production systems, this pattern generalizes to shortest-path-on-a-line problems where state compression turns exponential decision trees into predictable linear execution.

## 🚀 Variations & Further Practice
- Allow jumps of up to `k` books instead of just 1 or 2; the twist is the recurrence widens from two predecessors to a sliding minimum over `k` states.
- Add a penalty for skipping a book; now the transition cost depends on the move taken, not just the destination state.
- Count the number of distinct minimum-energy paths in addition to the minimum cost; this requires tracking both optimal value and multiplicity.