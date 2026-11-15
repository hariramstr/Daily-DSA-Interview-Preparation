# Minimum Reader Speed for Chapter Deadlines

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Search on Answer

---

## 🗂 Problem Overview
Given an array `chapters`, where each value is the page count of a chapter, and an integer `h`, find the minimum integer reading speed `s` such that all chapters can be completed within `h` hours. Each chapter takes `ceil(chapters[i] / s)` hours, and chapters must be finished sequentially. The challenge is that page counts and `h` are large enough that checking every possible speed is infeasible, so the solution must exploit the monotonic relationship between speed and total time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must find the minimum capacity that satisfies a deadline or SLA. Examples include provisioning worker throughput in streaming pipelines, selecting minimum replica counts for rate-limited services, sizing batch windows in ETL systems, and determining minimum CPU or I/O budgets for scheduled jobs. At scale, brute-force tuning across all capacities is operationally expensive or impossible. Monotonic feasibility lets you replace linear exploration with logarithmic search, which is the difference between something usable in control loops, autoscaling logic, or admission-control systems and something that collapses under large input ranges.

## 🔍 Problem Statement
You are given:

- `chapters[i]`: pages in the `i`-th chapter
- `h`: total hours available

A student reads chapters in order. Within one hour, they can work on only one chapter, and they cannot start the next chapter until the current one is fully completed. At reading speed `s` pages/hour, chapter `i` requires `ceil(chapters[i] / s)` hours.

Return the minimum integer speed `s` such that the total reading time across all chapters is at most `h`.

Constraints:

- `1 <= chapters.length <= 100000`
- `1 <= chapters[i] <= 1000000000`
- `chapters.length <= h <= 1000000000`
- The answer fits in a 32-bit signed integer

Examples:

- `chapters = [120, 90, 150], h = 8` → `50`
- `chapters = [300, 200, 400, 100], h = 10` → `100`

The key constraint is the answer space: possible speeds range up to `max(chapters)`, so linear scanning is too slow.

## 🪜 How to Solve This
1. Read the problem → notice the output is not a schedule or count, but the **minimum feasible speed**.
2. Ask what happens if speed increases → total required hours never goes up. That monotonicity is the signal for **binary search on the answer**.
3. Define a feasibility check: for a candidate speed `s`, compute total hours as `sum(ceil(chapter / s))`. If that total is `<= h`, the speed works.
4. Once you can answer “does speed `s` work?” efficiently, search the smallest working `s`.
5. Bound the search space:
   - Minimum speed is `1`
   - Maximum speed is `max(chapters)` because at that speed every chapter finishes in at most one hour
6. Binary search the range:
   - If `mid` works, keep searching left for a smaller valid speed
   - If `mid` fails, search right
7. Stop when the interval collapses. The remaining value is the minimum feasible speed.

This is the standard “optimize a monotonic predicate” playbook.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer.**  
   We are not searching a sorted array directly; we are searching a numeric answer space where feasibility is monotonic. If speed `s` is sufficient, then any speed `> s` is also sufficient.

2. **Set search bounds.**  
   Let `left = 1` and `right = max(chapters)`.  
   - `left` is the slowest meaningful speed.
   - `right` is always feasible because each chapter then takes exactly one hour, and the constraint guarantees `h >= chapters.length`.

3. **Define the feasibility function.**  
   For a candidate speed `s`, compute:
   `hours = Σ ceil(chapters[i] / s)`  
   Use integer math: `(chapters[i] + s - 1) / s`.  
   This preserves the invariant: `hours` is the exact number of whole hours needed at speed `s`.

4. **Run binary search.**  
   While `left < right`:
   - Compute `mid = left + (right - left) / 2`
   - If `mid` is feasible, set `right = mid`
   - Otherwise set `left = mid + 1`

   The invariant is:
   - all speeds `< left` are known infeasible
   - all speeds `>= right` are candidate feasible values

5. **Return `left`.**  
   When the loop ends, `left == right`, and by the invariant this is the smallest feasible speed.

This abstraction is correct because the predicate “can finish within `h` hours at speed `s`” is monotone over integer `s`.

## 📊 Worked Example
Example: `chapters = [120, 90, 150], h = 8`

| Step | left | right | mid | Hours at mid | Feasible? | Next range |
|---|---:|---:|---:|---:|---|---|
| 1 | 1 | 150 | 75 | 2 + 2 + 2 = 6 | Yes | `[1, 75]` |
| 2 | 1 | 75 | 38 | 4 + 3 + 4 = 11 | No | `[39, 75]` |
| 3 | 39 | 75 | 57 | 3 + 2 + 3 = 8 | Yes | `[39, 57]` |
| 4 | 39 | 57 | 48 | 3 + 2 + 4 = 9 | No | `[49, 57]` |
| 5 | 49 | 57 | 53 | 3 + 2 + 3 = 8 | Yes | `[49, 53]` |
| 6 | 49 | 53 | 51 | 3 + 2 + 3 = 8 | Yes | `[49, 51]` |
| 7 | 49 | 51 | 50 | 3 + 2 + 3 = 8 | Yes | `[49, 50]` |
| 8 | 49 | 50 | 49 | 3 + 2 + 4 = 9 | No | `[50, 50]` |

Result: `50`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = chapters.length` and `M = max(chapters)`. Each binary-search step scans the array once to evaluate feasibility, and there are `log M` such steps. Even when `M` is near `10^9`, `log2(M)` is about 30, so the approach remains practical for arrays in the `10^5` to `10^6` range.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few scalar variables and computes total hours on the fly. No additional data structures are required. Space cannot be meaningfully reduced further without changing the execution model; the main trade-off is only between readability and micro-optimizations in the feasibility loop.

## 💡 Key Takeaways
- If the question asks for the **minimum numeric value** that satisfies a constraint, check whether feasibility becomes monotonic as the value increases.
- “Can we finish within `h`?” is often easier to reason about than “what is the exact minimum?”; that shift usually exposes binary search on answer.
- Use integer ceiling safely: `(p + s - 1) / s`; floating-point division is unnecessary and invites precision bugs.
- The binary-search update must preserve the first-valid invariant: feasible `mid` moves `right = mid`, infeasible `mid` moves `left = mid + 1`.
- In production systems, monotonic feasibility predicates are a powerful way to turn capacity sizing and SLA tuning from expensive exploration into deterministic logarithmic search.

## 🚀 Variations & Further Practice
- **Minimum ship capacity within D days**: same search-on-answer pattern, but feasibility simulates packing contiguous loads into day-sized buckets.
- **Koko Eating Bananas / minimum processing rate**: identical monotonic structure, but useful for recognizing the pattern quickly across domains.
- **Split array largest sum**: harder because the objective is minimizing the maximum partition sum, and feasibility depends on partition counting rather than per-item ceiling arithmetic.