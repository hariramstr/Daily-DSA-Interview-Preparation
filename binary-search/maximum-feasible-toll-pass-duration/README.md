# Maximum Feasible Toll Pass Duration

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, greedy, interval-covering

---

## 🗂 Problem Overview
Given sorted toll gate positions `gates`, route length `L`, and a limit of `K` reusable passes, find the largest integer duration `D` such that all gates can be covered by at most `K` intervals of length `D`. Each pass covers a continuous interval `[x, x + D]`, where `x` may be any real value. The non-trivial part is scale: `gates.length` is up to `2 * 10^5`, while `L` can reach `10^18`, so brute-forcing durations is impossible.

## 🌍 Engineering Impact
This pattern shows up whenever you need to maximize a service window under a hard budget on the number of windows: CDN cache prewarming ranges, retention compaction segments in storage engines, maintenance windows over event timelines, and spectrum/time-slot allocation in networking. The core issue is not just coverage, but coverage under monotonic feasibility. Without exploiting that monotonic structure, systems fall back to exhaustive search or dynamic programming that collapses at production cardinalities. Binary search plus greedy feasibility turns an intractable planning problem into a predictable, scalable decision procedure with clean operational bounds.

## 🔍 Problem Statement
You are given a strictly increasing array `gates`, where `gates[i]` is the mile marker of the `i`-th toll gate, plus integers `L` and `K`. A pass of duration `D` covers every gate in some interval `[x, x + D]`, where `x` can be any real number. You may use at most `K` passes, and passes may overlap. Return the largest integer `D` for which all gates can be covered.

Constraints:

- `1 <= gates.length <= 2 * 10^5`
- `1 <= K <= gates.length`
- `1 <= gates[i] <= L <= 10^18`
- `gates` is sorted in strictly increasing order
- answer is an integer in `[0, L]`

Examples:

- `gates = [2, 5, 6, 11, 14], L = 20, K = 2`
- `gates = [1, 4, 8, 9, 15], L = 20, K = 3`

The key algorithmic constraint is the huge search space for `D`: up to `10^18`, which strongly suggests binary search over the answer.

## 🪜 How to Solve This
1. Read the objective carefully → we are not asked to construct all intervals, only to find the maximum feasible integer `D`.

2. Ask whether feasibility changes monotonically with `D` → if a duration `D` works, then any smaller duration is not necessarily easier here; instead, larger intervals cover more points, so feasibility is monotone non-decreasing with `D`. That means there is a threshold.

3. Threshold over a huge numeric domain → think binary search on the answer, not on indices.

4. For a fixed `D`, reduce the problem to: how many length-`D` intervals are needed to cover all sorted gate positions?

5. Sorted points plus fixed-length intervals → greedy is the natural check. Start from the leftmost uncovered gate, place an interval beginning there, and let it extend as far right as possible.

6. Why that greedy works → any feasible solution covering the leftmost uncovered gate can shift its interval left endpoint to that gate without losing coverage to the right, so starting there is optimal locally and never increases pass count.

7. Combine both ideas → binary search `D`, run greedy coverage in `O(n)`, and keep the largest feasible value.

## 🧩 Algorithm Walkthrough
1. **Use the Binary Search on Answer pattern.**  
   Search `D` in the integer range `[0, L]`. For each midpoint `mid`, ask: can all gates be covered with at most `K` intervals of length `mid`? This is the right abstraction because the feasibility predicate is monotonic.

2. **Define `feasible(D)` with a greedy sweep.**  
   Maintain an index `i` pointing to the leftmost uncovered gate and a counter `used` for passes consumed. While `i < n`, open one pass to cover the interval `[gates[i], gates[i] + D]`.

3. **Advance through all gates covered by that pass.**  
   Move `i` right while `gates[i] <= gates[start] + D`. This greedily maximizes coverage from the current pass. The invariant: after each pass, every gate before `i` is covered, and the number of passes used is minimal for that prefix.

4. **Stop early if `used > K`.**  
   Once the greedy sweep exceeds `K`, `D` is infeasible. If the sweep finishes within `K`, `D` is feasible.

5. **Why the greedy check is correct.**  
   For the leftmost uncovered gate `g`, any valid interval covering `g` can be shifted so its left endpoint is exactly `g`, producing coverage `[g, g + D]`, which is at least as good for future gates. Therefore the greedy choice never hurts and minimizes the number of intervals.

6. **Update binary search bounds.**  
   If `feasible(mid)` is true, store `mid` and search higher. Otherwise search lower. The invariant: all values below the lower bound are known feasible, and all above the upper bound are known infeasible.

## 📊 Worked Example
Take `gates = [2, 5, 6, 11, 14]`, `K = 2`.

Check `D = 4`:

| Pass | Start gate | Covered interval | Gates covered | Next index | Used |
|---|---:|---|---|---:|---:|
| 1 | 2 | `[2, 6]` | 2, 5, 6 | 3 | 1 |
| 2 | 11 | `[11, 15]` | 11, 14 | 5 | 2 |

All gates are covered with `2` passes, so `feasible(4) = true`.

Check `D = 3`:

| Pass | Start gate | Covered interval | Gates covered | Next index | Used |
|---|---:|---|---|---:|---:|
| 1 | 2 | `[2, 5]` | 2, 5 | 2 | 1 |
| 2 | 6 | `[6, 9]` | 6 | 3 | 2 |
| 3 | 11 | `[11, 14]` | 11, 14 | 5 | 3 |

This needs `3` passes, so `feasible(3) = false`.

Binary search therefore keeps `4` and rejects smaller infeasible candidates below the threshold.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log L)`, where `n = gates.length`. Each feasibility check is a single linear sweep over the gates, and binary search performs `O(log L)` checks. With `L <= 10^18`, that is about 60 iterations, so even at million-scale inputs the dominant cost remains sequential scanning, not the numeric search space.

### Space Complexity
`O(1)` auxiliary space beyond the input array. The greedy feasibility check uses only a few counters and indices. You could not meaningfully reduce this further; the only trade-off would be preprocessing structures, which would increase space without improving asymptotic runtime here.

## 💡 Key Takeaways
- If the problem asks for the maximum or minimum numeric value satisfying a condition, check whether feasibility is monotonic; that is the strongest signal for binary search on the answer.
- If inputs are sorted points on a line and you need the fewest fixed-length segments to cover them, greedy left-to-right packing is usually the right primitive.
- Be precise about interval inclusivity: coverage is `[x, x + D]`, so gates exactly at `x + D` are covered.
- Binary search over integers needs a stable convention for updating bounds and recording the best feasible answer; otherwise off-by-one bugs are easy.
- At scale, the win comes from separating optimization from decision: turn “find the best value” into repeated cheap feasibility checks.

## 🚀 Variations & Further Practice
- Allow each pass to have a different cost and ask for the minimum total cost to cover all gates; the monotonic predicate disappears, pushing the problem toward DP or shortest-path formulations.
- Extend from points to intervals of required coverage on the highway; greedy coverage becomes more subtle because each demand item has width, not just a position.
- Make passes two-dimensional, covering toll checkpoints on a plane with axis-aligned rectangles of fixed size; the clean 1D greedy invariant breaks, and the problem becomes significantly harder.