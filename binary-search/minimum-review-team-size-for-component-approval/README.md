# Minimum Review Team Size for Component Approval

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Difference Array

---

## 🗂 Problem Overview
Given an array `reviews` and an integer `span`, find the minimum number of engineers needed so every component `i` receives at least `reviews[i]` review contributions. Each engineer contributes `1` to every component in one contiguous block of length at most `span`, and blocks may overlap. The output is the smallest team size that makes all requirements feasible. The challenge is scale: `n` is up to `2 * 10^5`, so enumerating assignments or simulating all placements is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a bounded action affects a contiguous range and you need the minimum capacity to satisfy per-position demand. Examples include CDN edge cache warming over URL ranges, distributed rate-limit budget allocation across time windows, ad-delivery smoothing over impression intervals, and batch resource boosts in streaming pipelines. At scale, brute-force placement explodes because local choices interact through overlap. The binary-search-plus-feasibility pattern turns an intractable combinatorial search into a predictable `O(n log A)` decision process, which is the difference between a planner that works in production and one that collapses under release-sized inputs.

## 🔍 Problem Statement
You are given:

- `reviews[i]`: minimum review contributions required for component `i`
- `span`: maximum length of a contiguous block one engineer can cover
- `n = reviews.length`, with `1 <= n <= 2 * 10^5`
- `1 <= span <= n`
- `0 <= reviews[i] <= 10^9`

An engineer may be assigned to any contiguous block of length at most `span`, contributing exactly `1` review to every component in that block. Multiple engineers may overlap. A component is approved when its total received reviews are at least its requirement.

Return the minimum number of engineers needed.

Examples:

- `reviews = [1,2,2,1], span = 2` → `3`
- `reviews = [3,0,1,4,2], span = 3` → `6`

The key constraint is the combination of large `n` and large review counts. That rules out explicit assignment search and pushes toward a monotonic optimization strategy with a linear-time feasibility check.

## 🪜 How to Solve This
1. Start from the question being asked: not “find one assignment,” but “find the minimum team size.” That is a strong signal for binary search on the answer.

2. Ask whether feasibility is monotonic. If `k` engineers are enough, then any `k + 1` engineers are also enough, because extra engineers can always be placed without hurting validity. That gives a yes/no predicate suitable for binary search.

3. Now reduce the problem to: given `k`, can we approve all components using at most `k` engineers?

4. For feasibility, scan left to right. At component `i`, only one thing matters: how many active review contributions already cover `i`. If that is below `reviews[i]`, the deficit must be filled immediately; delaying would leave component `i` permanently under-covered.

5. Where should those new engineers start? Greedy says: start them at `i` and extend as far right as possible, up to `span`. That helps current and future components maximally.

6. To apply many overlapping range additions efficiently, use a difference array / sweep-line idea to track when previously added engineers stop contributing.

That chain gets you to binary search outside, greedy feasibility inside.

## 🧩 Algorithm Walkthrough
1. **Binary search the answer space.**  
   The minimum team size lies between `0` and `sum(reviews)` inclusive. `0` is valid when all requirements are zero; `sum(reviews)` is always a safe upper bound because each engineer can satisfy at least one unit somewhere. The predicate is: “Can we satisfy all components with at most `mid` engineers?”

2. **Use a greedy left-to-right feasibility check.**  
   Maintain `active`, the number of engineers whose assigned blocks currently cover index `i`. Also maintain a difference array `end` where `end[j]` means that many contributions expire before processing index `j`. Before handling `i`, subtract `end[i]` from `active`.

3. **At each component, fill only the deficit.**  
   If `active >= reviews[i]`, do nothing. Otherwise, the deficit is `need = reviews[i] - active`. These `need` engineers must start at `i`; no later start can help component `i`. This is the greedy invariant: after processing index `i`, component `i` is exactly satisfied with the fewest additional engineers forced by the prefix.

4. **Extend each new engineer as far right as allowed.**  
   Assign the `need` engineers to the block `[i, min(n - 1, i + span - 1)]`. This maximizes future benefit without changing current feasibility. Update `active += need`, `used += need`, and schedule expiration with `end[i + span] += need` if that index is in bounds.

5. **Early exit on budget overflow.**  
   If `used > mid`, feasibility fails immediately. If the scan finishes within budget, `mid` is feasible.

This combines **Binary Search on Answer** with a **Greedy + Difference Array** feasibility pass. The abstraction is right because the optimization target is monotonic, and the range effect of each decision must be aggregated in `O(1)` amortized time.

## 📊 Worked Example
Example: `reviews = [1,2,2,1]`, `span = 2`

Check feasibility for `k = 3`.

| i | expire at i | active before fill | required | add `need` | active after fill | used |
|---|-------------|--------------------|----------|------------|-------------------|------|
| 0 | 0           | 0                  | 1        | 1          | 1                 | 1    |
| 1 | 0           | 1                  | 2        | 1          | 2                 | 2    |
| 2 | 1           | 1                  | 2        | 1          | 2                 | 3    |
| 3 | 1           | 1                  | 1        | 0          | 1                 | 3    |

Trace:
1. At `i = 0`, component 0 is short by 1, so start 1 engineer on `[0,1]`.
2. At `i = 1`, active coverage is 1, still short by 1, so start another on `[1,2]`.
3. At `i = 2`, the first engineer expires; active drops to 1. Start 1 more on `[2,3]`.
4. At `i = 3`, the second engineer expires; active is still 1 from the third engineer, enough.

So `3` works. Trying `k = 2` fails at index `2`, proving the answer is `3`.

## ⏱ Complexity Analysis
### Time Complexity
The feasibility check is `O(n)`: one left-to-right pass, with constant-time updates to `active` and the difference array. Binary search runs over the answer range, so total time is `O(n log S)`, where `S = sum(reviews)`. For `n = 10^6`, linear passes are still practical; anything quadratic is dead on arrival. At `10^9`-scale values, the logarithmic factor stays manageable.

### Space Complexity
The algorithm uses `O(n)` extra space for the difference array that tracks when active contributions expire. The rest is constant space. You could not realistically reduce this below linear without replacing direct expiration indexing with a more complex event structure, which adds overhead without improving asymptotics.

## 💡 Key Takeaways
- If the problem asks for a minimum resource count and “`k` works implies `k+1` works,” think binary search on the answer immediately.
- If each action affects a contiguous range and you only need aggregate coverage, think greedy placement plus a difference array rather than explicit interval simulation.
- The expiration index is `i + span`, not `i + span - 1`; contributions remain active through `i + span - 1` and disappear before the next index.
- In the feasibility pass, deficits must be filled at the current index; postponing even one unit breaks correctness because past components cannot be repaired later.
- The production lesson is to separate optimization from execution: use a monotonic decision oracle for planning, then wrap it with binary search instead of searching the full assignment space.

## 🚀 Variations & Further Practice
- Allow each engineer to contribute a weight greater than 1, with per-engineer costs. The twist is that feasibility becomes a budgeted covering problem and the greedy step may need a priority structure or convex optimization logic.
- Require each assigned block to have length exactly `span` instead of at most `span`. The edge handling changes materially near the right boundary, and some instances become infeasible for small `k`.
- Extend from a line to a 2D grid where one action covers a `span x span` submatrix. The same intuition survives, but the difference-array machinery becomes two-dimensional and implementation complexity rises sharply.