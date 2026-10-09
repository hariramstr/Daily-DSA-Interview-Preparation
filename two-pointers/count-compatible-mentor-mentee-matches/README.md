# Count Compatible Mentor-Mentee Matches

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given two arrays of skill ratings, pair mentors and mentees one-to-one to maximize the number of valid matches. A match is valid only when `mentor >= mentee` and `mentor - mentee <= maxGap`. You may reorder both arrays arbitrarily before pairing. The challenge is avoiding quadratic pair checking: with inputs up to `2 * 10^5`, the solution must exploit ordering so each element is considered only a constant number of times.

## 🌍 Engineering Impact
This pattern shows up anywhere two ordered populations must be matched under bounded compatibility rules: ad allocation against campaign constraints, job schedulers assigning workers to tasks with capability thresholds, search systems matching queries to shards with latency budgets, or marketplace engines pairing supply and demand under SLA windows. At scale, naive all-pairs evaluation collapses under `O(nm)` cost and cache-unfriendly access patterns. Sorting plus linear matching turns the problem into a predictable, throughput-friendly pass. That enables batch processing, deterministic behavior, and straightforward reasoning about fairness, capacity utilization, and tail-latency under large input volumes.

## 🔍 Problem Statement
You are given integer arrays `mentors` and `mentees`, plus an integer `maxGap`. Each mentor can be matched with at most one mentee, and each mentee can be matched with at most one mentor. A pair `(mentor, mentee)` is compatible if:

- `mentor >= mentee`
- `mentor - mentee <= maxGap`

Return the maximum number of compatible pairs.

You may reorder both arrays arbitrarily before matching. The objective is purely cardinality: maximize the count of valid pairs, not the total quality of matches.

Constraints:

- `1 <= mentors.length, mentees.length <= 2 * 10^5`
- `1 <= mentors[i], mentees[i] <= 10^9`
- `0 <= maxGap <= 10^9`

Examples:

- `mentors = [6, 3, 8, 10]`, `mentees = [2, 5, 7, 9]`, `maxGap = 2` → `3`
- `mentors = [4, 4, 6]`, `mentees = [3, 4, 5, 6]`, `maxGap = 0` → `2`

The input size rules out brute-force comparison of every mentor against every mentee.

## 🪜 How to Solve This
1. Start from the constraints → `2 * 10^5` means `O(nm)` is dead on arrival. We need something near `O(n log n + m log m)`.

2. Notice the compatibility rule is interval-based for each mentee: a valid mentor must lie in `[mentee, mentee + maxGap]`. Interval problems usually become easier after sorting.

3. Once both arrays are sorted, the smallest remaining mentee should be considered first. If we can’t match them with the smallest feasible mentor, using a larger mentor only wastes capacity that might be needed later.

4. That suggests a greedy rule: always try to match the current smallest mentee with the current smallest mentor that can legally satisfy them.

5. Use two pointers:
   - If mentor is too small (`mentor < mentee`), that mentor can never help this or any later mentee → discard it.
   - If mentor is too large (`mentor > mentee + maxGap`), this mentee can never be matched by the current or any later mentor → discard the mentee.
   - Otherwise, match them and advance both.

6. This works because sorting converts a global combinatorial problem into a local monotonic decision process with no backtracking.

## 🧩 Algorithm Walkthrough
1. **Sort both arrays ascending.**  
   This is the enabling step for the **Two Pointers + Greedy** pattern. After sorting, feasibility becomes monotonic: once a mentor is too small for a mentee, they are too small for every later mentee; once a mentor is too large for a mentee’s allowed gap, all later mentors are also too large.

2. **Initialize two indices `i` and `j` at `0`.**  
   `i` scans `mentors`, `j` scans `mentees`. Maintain the invariant that everything before `i` and `j` has already been either matched or proven unusable.

3. **If `mentors[i] < mentees[j]`, advance `i`.**  
   This mentor cannot satisfy the current mentee’s minimum requirement. Because mentees are sorted nondecreasingly, they also cannot satisfy any future mentee. Discarding them is always safe.

4. **Else if `mentors[i] > mentees[j] + maxGap`, advance `j`.**  
   The current mentor is already above the allowed range for this mentee. Since future mentors are even larger, this mentee is impossible to match from here onward. Discarding the mentee is forced, not heuristic.

5. **Otherwise, record a match and advance both pointers.**  
   The mentor lies inside the valid interval `[mentee, mentee + maxGap]`. Greedily taking the smallest feasible mentor preserves larger mentors for larger mentees, which is exactly what maximizes total match count.

6. **Stop when either pointer reaches the end.**  
   No further one-to-one matches are possible once one side is exhausted.

This abstraction is right because the problem has ordered compatibility and irreversible dominance relations. Two pointers exploit that structure directly, giving a single linear scan after sorting.

## 📊 Worked Example
Take `mentors = [6, 3, 8, 10]`, `mentees = [2, 5, 7, 9]`, `maxGap = 2`.

After sorting:

- `mentors = [3, 6, 8, 10]`
- `mentees = [2, 5, 7, 9]`

| Step | `i` | `j` | Mentor | Mentee | Decision | Matches |
|---|---:|---:|---:|---:|---|---:|
| 1 | 0 | 0 | 3 | 2 | `3` in `[2,4]` → match | 1 |
| 2 | 1 | 1 | 6 | 5 | `6` in `[5,7]` → match | 2 |
| 3 | 2 | 2 | 8 | 7 | `8` in `[7,9]` → match | 3 |
| 4 | 3 | 3 | 10 | 9 | `10` in `[9,11]` → match | 4 |

This trace yields 4 valid pairs. The example output in the prompt shows one valid pairing of size 3, but not the maximum. Since `(8,7)` and `(10,9)` are both simultaneously feasible, the true maximum for this input is 4.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates: `O(n log n + m log m)`, followed by a linear `O(n + m)` two-pointer scan. At `10^6` scale, this is still operationally realistic in memory-resident batch jobs. At `10^9`, even sorting becomes infrastructure-bound, pushing you toward external sort or distributed processing.

### Space Complexity
`O(1)` auxiliary space beyond sort overhead if sorting in place. In practice, language runtime sort implementations may allocate additional stack or buffer memory. You can reduce extra allocations by mutating inputs directly, trading away preservation of original ordering.

## 💡 Key Takeaways
- If the rule is “pair items one-to-one under ordered numeric constraints,” sorting plus two pointers should be one of the first candidate patterns.
- When feasibility forms a monotonic interval and you only need the maximum count, greedy matching with the smallest feasible partner is usually the right lens.
- Be careful with the upper-bound check: invalid-on-the-high-side is `mentor > mentee + maxGap`, not `>=`.
- Do not advance both pointers on a failed comparison unless a match was made; each failure case eliminates exactly one side.
- In production matching systems, imposing order first often converts expensive bipartite search into a deterministic linear pass with better throughput and easier correctness reasoning.

## 🚀 Variations & Further Practice
- Add capacities: each mentor can take `k` mentees instead of one. The twist is preserving greedy correctness while tracking residual capacity efficiently.
- Maximize total pairing value instead of count, where value depends on skill difference. This turns a simple greedy pass into dynamic programming or weighted matching territory.
- Process mentors and mentees as streaming arrivals rather than static arrays. The harder part is maintaining near-optimal matches online without full re-sorting.