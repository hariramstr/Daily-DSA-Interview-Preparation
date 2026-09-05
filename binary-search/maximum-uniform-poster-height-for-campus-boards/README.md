# Maximum Uniform Poster Height for Campus Boards

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Greedy

---

## 🗂 Problem Overview
Given strip heights `boards[]` and a required poster count `k`, find the largest integer height `h` such that cutting every strip into pieces of height `h` yields at least `k` posters in total. A strip of height `x` contributes `floor(x / h)` posters, and pieces cannot be combined across strips. The challenge is scale: `boards.length` is up to `200,000`, strip heights up to `10^9`, and brute-forcing all candidate heights is infeasible.

## 🌍 Engineering Impact
This pattern shows up whenever a system must maximize a uniform allocation size under hard capacity constraints. Examples include distributed rate-limiters choosing the largest per-tenant quota that still satisfies global fairness, media pipelines selecting chunk sizes that fit throughput budgets, storage systems determining maximum block sizes from fragmented extents, and schedulers computing the largest task quantum that still yields enough parallel work units. Without exploiting monotonic feasibility, implementations degrade into linear scans over huge answer spaces or expensive simulation loops. Binary search on the answer converts a capacity-planning problem into a predictable, scalable decision procedure.

## 🔍 Problem Statement
You are given an integer array `boards` where `boards[i]` is the height of the `i`-th strip, and an integer `k` representing the minimum number of posters required. Each poster must have the same integer height `h`. A strip may be cut into multiple posters, but each poster must come entirely from one strip; joining leftover pieces is not allowed.

For a candidate height `h`, the total posters produced is:

`sum(floor(boards[i] / h))`

Return the maximum integer `h` such that this total is at least `k`. If even height `1` cannot produce `k` posters, return `0`.

Constraints:
- `1 <= boards.length <= 200000`
- `1 <= boards[i] <= 1000000000`
- `1 <= k <= 1000000000`

Examples:
- `boards = [8, 5, 8], k = 5` → `4`
- `boards = [2, 3], k = 10` → `0`

The key constraint is the answer space: heights range up to `10^9`, so direct enumeration is not viable.

## 🪜 How to Solve This
1. Read the objective → we are not asked to construct the cuts, only to find the maximum valid height.
2. For any fixed height `h`, feasibility is easy to test: sum `floor(board / h)` across all strips and check whether the total is at least `k`.
3. Notice the monotonic property → if height `h` works, then every smaller positive height also works, because decreasing `h` can only increase or preserve the number of posters.
4. A monotonic yes/no predicate over an integer range is the standard signal for binary search on the answer.
5. Define the search space:
   - minimum candidate height = `1`
   - maximum candidate height = `max(boards)`
6. Before searching, observe the impossible case naturally falls out: if height `1` is not feasible, no larger height can be feasible either, so the result is `0`.
7. Binary search for the largest feasible `h`:
   - if `mid` works, record it and move right
   - if `mid` fails, move left
8. The result is the last feasible height seen.

This is efficient because feasibility is linear in `n`, and the answer space shrinks logarithmically.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer.**  
   The candidate answer is an integer height `h`, and feasibility is monotonic. This is the right abstraction because we are optimizing a value, not searching for an index in sorted data.

2. **Set bounds.**  
   Let `low = 1` and `high = max(boards)`. No valid poster height can exceed the tallest strip. The invariant is that the true answer, if it exists, lies within this range.

3. **Define the feasibility check.**  
   For a candidate `mid`, compute `count = Σ floor(boards[i] / mid)`. If `count >= k`, then `mid` is feasible. This is correct because each strip independently contributes the maximum number of posters of height `mid`, and contributions are additive.

4. **Use early termination in the check.**  
   While summing, stop once `count >= k`. This preserves correctness and avoids unnecessary work on large inputs. The invariant is that once feasibility is established, extra contributions do not change the binary-search decision.

5. **Run the search for the maximum feasible value.**  
   While `low <= high`, compute `mid`.  
   - If feasible, store `answer = mid` and move `low = mid + 1` to search for a larger valid height.  
   - Otherwise move `high = mid - 1`.

6. **Return the recorded answer.**  
   If no candidate was feasible, `answer` remains `0`. This handles the “cannot make `k` posters even at height 1” case without special branching.

7. **Maintain numeric safety.**  
   Use 64-bit arithmetic for the poster count and midpoint calculation. With up to `200,000` strips and heights up to `10^9`, intermediate sums can exceed 32-bit range.

## 📊 Worked Example
Example: `boards = [8, 5, 8]`, `k = 5`

| Step | low | high | mid | posters from [8,5,8] | total | feasible? | action |
|---|---:|---:|---:|---|---:|---|---|
| 1 | 1 | 8 | 4 | `[2,1,2]` | 5 | yes | `answer=4`, move right |
| 2 | 5 | 8 | 6 | `[1,0,1]` | 2 | no | move left |
| 3 | 5 | 5 | 5 | `[1,1,1]` | 3 | no | move left |

Search stops when `low > high`. The largest feasible height seen was `4`.

Why this trace matters: the search never evaluates every height. It only probes enough candidates to isolate the boundary between feasible and infeasible values. That boundary is exactly the maximum uniform poster height.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = boards.length` and `M = max(boards)`. Each binary-search step scans the array once for feasibility, and there are `log2(M)` steps. With `M <= 10^9`, that is about 31 passes. This remains practical even when `n` approaches `10^6`; a linear scan over all heights would not.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few scalar variables for bounds, midpoint, running count, and answer. No extra data structures are required. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off is only numeric width, where 64-bit counters are required for safety.

## 💡 Key Takeaways
- If the problem asks for the **maximum/minimum integer value** satisfying a condition, check whether that condition is monotonic; that is the strongest signal for binary search on the answer.
- When feasibility for a candidate can be computed by a single pass with additive contributions, expect an `O(n log answer_space)` solution rather than simulation or greedy construction.
- The classic off-by-one trap is returning `low` or `high` blindly; store the last feasible value or use a carefully proven boundary convention.
- Use 64-bit arithmetic for accumulated poster counts and midpoint computation; 32-bit overflow is easy to trigger under the stated constraints.
- At scale, this pattern is a capacity-boundary search: convert optimization into repeated feasibility checks, then isolate the boundary efficiently instead of enumerating the space.

## 🚀 Variations & Further Practice
- **Minimize the maximum load after splitting resources**: same binary-search-on-answer pattern, but the predicate flips from “can produce at least `k` units” to “can keep every partition under threshold `T`”.
- **Allow at most `c` cuts total**: feasibility now depends on both produced pieces and cut budget, adding a second resource constraint to the predicate.
- **2D uniform cutting problems**: maximize square or rectangle size from sheets or boards, where each item contributes `floor(w / x) * floor(h / y)` or similar; the harder twist is multi-dimensional feasibility.