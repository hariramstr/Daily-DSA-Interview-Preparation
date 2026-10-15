# Minimum Inspection Radius for Pipeline Leak Sensors

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Intervals, Greedy

---

## 🗂 Problem Overview
Given sensor positions on a line and a set of critical closed intervals, compute the smallest integer radius `R` such that the union of sensor coverage intervals `[x - R, x + R]` fully contains every critical zone. The challenge is scale: up to `2 * 10^5` sensors and zones, with coordinates up to `10^18`. That rules out pointwise simulation and pushes the solution toward monotonic search plus interval-based feasibility checking.

## 🌍 Engineering Impact
This pattern shows up anywhere fixed resources must guarantee continuous coverage over required ranges: cellular tower planning, CDN edge reachability windows, camera or LiDAR corridor coverage, maintenance scheduling over rail/pipeline segments, and time-window validation in streaming systems. At scale, brute-force validation collapses under large coordinate spaces and dense overlaps. The useful abstraction is not “check every point,” but “reason over merged intervals under a monotone parameter.” That shift enables predictable `O((n + m) log U)` behavior, where `U` is the answer range, and turns an otherwise intractable coverage problem into a production-safe decision procedure.

## 🔍 Problem Statement
You are given:

- `sensors`: integer positions of fixed leak sensors, `1 <= sensors.length <= 2 * 10^5`
- `zones`: critical closed intervals `[start, end]`, `1 <= zones.length <= 2 * 10^5`
- coordinates in `[0, 10^18]`

A sensor at position `x` covers `[x - R, x + R]`, where `R` is the same integer for all sensors. Find the minimum integer `R` such that every critical zone is fully covered by the union of all sensor coverage intervals.

Important details:

- Zones may overlap, touch, or be nested.
- Only listed critical zones matter; uncovered non-critical pipeline sections are irrelevant.
- The answer fits in signed 64-bit.

Examples:

- `sensors = [2, 8, 14]`, `zones = [[1, 4], [6, 10], [13, 15]]` → `2`
- `sensors = [5, 11]`, `zones = [[0, 3], [4, 8], [10, 14]]` → `5`

The key constraint is the `10^18` coordinate range: any algorithm proportional to distance or point count is dead on arrival.

## 🪜 How to Solve This
1. Read the problem → coverage radius is the unknown, and the question is “what is the minimum value that makes the condition true?”
2. That wording is a strong binary-search signal: if radius `R` works, then any larger radius also works. Feasibility is monotone.
3. So the problem reduces to: given a candidate `R`, can we verify coverage of all critical zones efficiently?
4. Raw zones may overlap or nest. Since every point in their union must be covered, first merge them into disjoint required intervals. That shrinks the state space and removes redundant checks.
5. For a fixed `R`, each sensor induces an interval. We do not need to materialize all covered points; we only need to know whether the union of sensor intervals covers each merged zone.
6. Sort sensors once. Then sweep sensors and merged zones left to right. Greedily extend the rightmost covered point using any sensor whose coverage starts at or before the next uncovered position.
7. Binary search the smallest `R` whose feasibility check succeeds.

That chain gives a clean `sort + merge + greedy check + binary search` solution.

## 🧩 Algorithm Walkthrough
1. **Sort sensors and zones.**  
   Sort `sensors` ascending. Sort `zones` by start, then merge overlapping or touching intervals into disjoint required segments.  
   **Why correct:** overlapping critical zones do not create new obligations beyond their union.  
   **Invariant:** merged zones are disjoint and represent exactly the required coverage set.

2. **Define the monotone predicate `canCover(R)`.**  
   For a candidate radius `R`, sensor `x` covers `[x - R, x + R]`. We must decide whether these intervals cover every merged zone.  
   **Why correct:** if `canCover(R)` is true, then `canCover(R + 1)` is also true because every sensor interval only expands.  
   **Pattern:** **Binary Search on Answer**.

3. **Sweep merged zones left to right.**  
   Maintain a pointer `i` into sorted sensors. For each merged zone `[a, b]`, set `need = a`, the leftmost point not yet covered in this zone.  
   **Invariant:** everything left of `need` in the current zone is already covered.

4. **Greedily extend coverage.**  
   While `need <= b`, consume all sensors with `sensors[i] - R <= need`. Among them, track the farthest right endpoint `best = max(best, sensors[i] + R)`.  
   If no such sensor exists or `best < need`, coverage breaks and `canCover(R)` is false. Otherwise set `need = best + 1` conceptually, or for interval arithmetic on integers/closed intervals, continue while `best < b` and treat `[need, best]` as covered.  
   **Why correct:** any sensor starting after `need` cannot cover `need`; choosing the farthest reachable right endpoint is the standard greedy interval-cover argument.  
   **Pattern:** **Greedy interval covering**.

5. **Binary search the minimum feasible radius.**  
   Search `R` over `[0, hi]`, where `hi` can safely be `10^18` or a tighter bound from extremes. Use mid-point arithmetic that avoids overflow.  
   **Invariant:** search space maintains `lo` infeasible boundary and `hi` feasible boundary until convergence.

This combination is the right abstraction because the hard part is not geometry; it is exploiting monotonicity and interval union structure.

## 📊 Worked Example
Use `sensors = [2, 8, 14]`, `zones = [[1, 4], [6, 10], [13, 15]]`.

Merged zones are unchanged.

Check `R = 2`:

| Zone | `need` start | Eligible sensors (`x - R <= need`) | Farthest `best` | Result |
|---|---:|---|---:|---|
| `[1,4]` | 1 | sensor `2` covers `[0,4]` | 4 | zone covered |
| `[6,10]` | 6 | sensor `8` covers `[6,10]` | 10 | zone covered |
| `[13,15]` | 13 | sensor `14` covers `[12,16]` | 16 | zone covered |

So `canCover(2) = true`.

Check `R = 1`:

- Zone `[1,4]`: sensor `2` covers `[1,3]`, so farthest `best = 3`.
- Next uncovered point is `4`.
- Sensor `8` starts at `7`, which is too far right to cover `4`.
- Coverage fails.

Therefore the minimum radius is `2`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting sensors and zones costs `O(n log n + m log m)`. Each feasibility check is linear, `O(n + k)`, where `k` is the number of merged zones, because sensor and zone pointers only move forward. Binary search over a 64-bit radius adds a factor of about 60, so total time is `O(n log n + m log m + (n + m) log U)` with `U <= 10^18`. This remains practical at million-scale inputs; it is the only viable class once coordinates approach `10^9+`.

### Space Complexity
Space is `O(m)` in the worst case for merged zones, plus sorting overhead depending on language/runtime. The feasibility check itself is `O(1)` extra beyond pointers and scalar state. You could reduce retained space by merging zones in place after sort, trading readability for tighter memory behavior.

## 💡 Key Takeaways
- If the question asks for the minimum value that makes a condition true, first test whether the condition is monotone; that is the strongest signal for binary search on the answer.
- When requirements are unions of intervals, merge them early; reducing overlapping obligations usually simplifies both correctness and runtime.
- Closed-interval semantics matter: touching zones should merge, and coverage checks must not accidentally treat endpoints as open.
- Use overflow-safe arithmetic for `mid`, `x - R`, and `x + R` when coordinates are near `10^18`; 64-bit is enough only if every operation is disciplined.
- The transferable design insight is to convert huge coordinate spaces into ordered interval events; production systems scale when they reason over structure, not individual points.

## 🚀 Variations & Further Practice
- Allow each sensor to have its own activation cost and ask for the minimum total cost to cover all zones for a fixed radius; the twist is combining interval coverage with weighted selection.
- Make zones dynamic with online insert/delete queries and repeated radius checks; the harder part is maintaining merged required intervals incrementally.
- Extend from 1D pipeline coverage to 2D corridor or polygon coverage; monotonicity may survive, but the feasibility check stops being a simple greedy interval sweep.