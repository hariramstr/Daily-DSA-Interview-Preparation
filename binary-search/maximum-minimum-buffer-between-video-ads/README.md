# Maximum Minimum Buffer Between Video Ads

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given a sorted list of allowed ad insertion times, choose exactly `k` distinct times so that the minimum gap between consecutive chosen ads is as large as possible. Return that maximum achievable minimum gap. The challenge is not selecting ads greedily by local spacing, but optimizing a global bottleneck metric under large input sizes: up to `100000` candidate positions and timestamps up to `10^9`, which rules out combinatorial search or dynamic programming over time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize worst-case separation under discrete placement constraints: ad scheduling in streaming, replica placement across failure domains, rate-limit token refill checkpoints, search result diversification, and batch job staggering in shared clusters. At scale, naive optimization either explodes combinatorially or overfits local choices that degrade tail behavior. The binary-search-on-answer pattern matters because it converts a hard optimization problem into a sequence of cheap feasibility checks. That shift is architecturally important: it gives predictable runtime, clean correctness boundaries, and a reusable decision oracle that can often be instrumented, tested, and evolved independently.

## 🔍 Problem Statement
You are given:

- `candidateTimes`: a strictly increasing sorted array of valid ad start times
- `k`: the exact number of ad breaks to place
- `L`: total video length

You must choose exactly `k` distinct values from `candidateTimes` such that the minimum difference between every pair of adjacent chosen times is maximized. Return that maximum possible minimum distance.

Constraints:

- `2 <= candidateTimes.length <= 100000`
- `0 <= candidateTimes[i] <= 1000000000`
- `candidateTimes` is sorted in strictly increasing order
- `2 <= k <= candidateTimes.length`
- `1 <= L <= 1000000000`

Examples:

- `candidateTimes = [5, 11, 18, 26, 39], k = 3, L = 45` → `13`
- `candidateTimes = [2, 4, 7, 10, 14, 19], k = 4, L = 20` → `5`

The decisive constraint is input size: `O(n^2)` or subset enumeration is infeasible, so the solution must exploit monotonicity and run near `O(n log range)`.

## 🪜 How to Solve This
1. Read the objective carefully → we are not maximizing the average gap or total spread; we are maximizing the **minimum** gap between consecutive chosen times.

2. That phrasing usually suggests a decision version:  
   → “Can I place `k` ads so every adjacent pair is at least `g` apart?”

3. Once you ask that question, a monotonic property appears:  
   → if gap `g` is feasible, then any smaller gap is also feasible  
   → if gap `g` is infeasible, any larger gap is also infeasible

4. Monotonic feasibility means binary search on the answer, not on indices.

5. Now define the feasibility check. For a fixed `g`, the best strategy is greedy:  
   → place the first ad at the earliest candidate time  
   → always place the next ad at the earliest candidate time at least `g` away from the last chosen one

6. Why greedy works: choosing earlier never reduces future options; it only preserves or increases remaining space.

7. So the full approach is:  
   → binary search candidate gap values  
   → for each guess, run one left-to-right greedy pass  
   → keep the largest feasible gap

That gives an efficient and clean solution.

## 🧩 Algorithm Walkthrough
1. **Define the search space.**  
   The minimum gap cannot be negative, and its upper bound is `candidateTimes[n - 1] - candidateTimes[0]`. We binary search this integer range. The pattern is **Binary Search on Answer** because we are searching over possible quality values, not array positions.

2. **Build a feasibility predicate `canPlace(g)`.**  
   Start by choosing the first candidate time. Maintain:
   - `count`: number of ads placed so far
   - `last`: last chosen time  
   Scan left to right. Whenever `candidateTimes[i] - last >= g`, choose it, increment `count`, and update `last`.

3. **Maintain the greedy invariant.**  
   After processing prefix `0..i`, the greedy construction has placed the maximum possible number of ads for gap `g` while keeping the last chosen ad as early as possible. That “earliest valid placement” invariant is what preserves future feasibility.

4. **Use monotonicity.**  
   If `canPlace(g)` returns true, then `g` is achievable and all smaller gaps are also achievable. Move right to try a larger gap. Otherwise move left.

5. **Return the largest feasible value.**  
   Use the standard upper-mid binary search (`mid = lo + (hi - lo + 1) / 2`) to avoid infinite loops when narrowing toward the maximum feasible gap.

This combination of **Binary Search + Greedy Feasibility Check** is the right abstraction because the optimization target is scalar, ordered, and monotone under a cheap decision oracle.

## 📊 Worked Example
Example: `candidateTimes = [5, 11, 18, 26, 39], k = 3`

Binary search over gap `g` in `[0, 34]`.

| Guess `g` | Greedy picks | Count | Feasible? |
|---|---|---:|---|
| 17 | `5, 26` | 2 | No |
| 8  | `5, 18, 26, 39` | 4 | Yes |
| 12 | `5, 18, 39` | 3 | Yes |
| 14 | `5, 26` | 2 | No |
| 13 | `5, 18, 39` | 3 | Yes |

Trace for `g = 13`:
1. Pick `5` → `count = 1`, `last = 5`
2. `11 - 5 = 6` < `13` → skip
3. `18 - 5 = 13` ≥ `13` → pick `18`
4. `26 - 18 = 8` < `13` → skip
5. `39 - 18 = 21` ≥ `13` → pick `39`

We successfully place `3` ads, so `13` is feasible. Since `14` is not feasible, the answer is `13`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log D)`, where `n = candidateTimes.length` and `D = candidateTimes[n - 1] - candidateTimes[0]`. Each binary-search step runs one linear greedy scan. With `n = 10^5`, this is comfortably fast; even if timestamps span `10^9`, `log D` is about 30, so the total work is roughly 30 passes over the array.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few counters and pointers during the feasibility scan; the input array is reused in place. Space cannot be meaningfully reduced further unless the input itself is streamed, which would trade away random access but not improve asymptotic memory.

## 💡 Key Takeaways
- If the problem asks for the **largest minimum** or **smallest maximum**, check whether it can be reframed as a monotone feasibility test plus binary search on the answer.
- When positions are sorted and feasibility depends only on pairwise distance, an earliest-valid greedy placement is often the right decision oracle.
- Use upper-mid binary search when searching for the maximum feasible value; lower-mid can stall on adjacent bounds.
- The feasibility check should count placements `>= k`, not exactly `== k`; if you can place more than `k`, you can always stop at `k`.
- In production systems, separating optimization into a cheap, testable decision predicate plus a search strategy yields better correctness, observability, and reuse than embedding heuristics directly into one opaque routine.

## 🚀 Variations & Further Practice
- **Aggressive Cows / Magnetic Force Between Two Balls**: same core pattern, but framed as placing items in stalls; useful for recognizing the abstraction independent of domain language.
- **Place `k` intervals instead of points**: each ad break has duration, so feasibility must account for occupied ranges rather than single timestamps; the greedy predicate becomes more stateful.
- **Weighted or priority-constrained placements**: some candidate times have penalties or mandatory inclusion rules, which breaks the simple monotone greedy check and may require DP or parametric search with richer feasibility logic.