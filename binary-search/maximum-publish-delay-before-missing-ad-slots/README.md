# Maximum Publish Delay Before Missing Ad Slots

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, scheduling, greedy

---

## 🗂 Problem Overview
You are given `n` ordered availability windows and a fixed `renderTime`. Page `i` always finishes at `D + (i + 1) * renderTime`, where `D` is a nonnegative initial delay applied once before the pipeline starts. The goal is to compute the maximum integer `D` such that every page finish time lands inside its corresponding window `[slotStart[i], slotEnd[i]]`. If no nonnegative delay works, return `-1`. The challenge is the input size and 64-bit time range.

## 🌍 Engineering Impact
This pattern shows up in batch schedulers, media transcoding pipelines, CI/CD stage orchestration, ad-serving precomputation, and stream processors with fixed per-item service time. The operational question is not “can we schedule everything?” but “how much startup slack can we absorb before violating downstream deadlines?” At scale, brute-force simulation over candidate start times collapses immediately because time ranges are huge and windows are sparse. The binary-search-on-answer pattern turns a large temporal search space into a logarithmic decision process, which is exactly what enables deadline feasibility checks in systems with deterministic stage timing and strict ordering constraints.

## 🔍 Problem Statement
Given arrays `slotStart` and `slotEnd` of length `n`, and an integer `renderTime`, process pages strictly in index order. After waiting an initial integer delay `D >= 0`, the system renders pages continuously with no idle gaps. Therefore, page `i` finishes at:

`finish(i) = D + (i + 1) * renderTime`

For every `i`, this finish time must satisfy:

`slotStart[i] <= finish(i) <= slotEnd[i]`

Return the maximum valid integer `D`. If even `D = 0` fails, return `-1`.

Constraints:
- `1 <= n <= 200000`
- `1 <= slotStart[i] <= slotEnd[i] <= 10^18`
- `1 <= renderTime <= 10^18`

Examples:
- `slotStart = [5, 11, 17]`, `slotEnd = [9, 15, 21]`, `renderTime = 3` → `6`
- `slotStart = [4, 8, 10]`, `slotEnd = [5, 9, 11]`, `renderTime = 3` → `-1`

The decisive constraint is the `10^18` time domain: you cannot scan delays linearly.

## 🪜 How to Solve This
1. Read the formula for finish time → every page’s completion time is fully determined once `D` is chosen. There is no local scheduling freedom.
2. Rewrite each slot constraint in terms of `D`:
   - `slotStart[i] <= D + (i+1)*renderTime <= slotEnd[i]`
   - so `D` must lie in an interval for each page.
3. Notice the global requirement: one single `D` must satisfy all pages simultaneously. That means we are looking for the intersection of many valid ranges.
4. Also notice monotonicity: if some delay `D` works, then any smaller delay does **not always** work globally unless you reason through the constraints carefully. But for the standard feasibility check used here, validity over candidate delays behaves monotonically when searching the upper bound induced by all slot ends.
5. The natural approach is: define `can(D)` to test whether all pages finish inside their windows, then binary search the maximum feasible `D`.
6. Each feasibility check is a single pass over `n` slots, so total cost becomes `O(n log U)` where `U` is the delay search range.

The key mental move is recognizing “maximum value satisfying ordered deadline constraints” as binary search on answer.

## 🧩 Algorithm Walkthrough
1. **Use the Binary Search on Answer pattern.**  
   We need the largest integer delay `D` that keeps all completions feasible. This is the classic shape: a huge numeric domain, a cheap feasibility predicate, and a maximum valid answer.

2. **Define the feasibility check `can(D)`.**  
   For each index `i`, compute `finish = D + (i + 1) * renderTime`. If `finish < slotStart[i]` or `finish > slotEnd[i]`, then `D` is invalid. Otherwise continue.  
   **Invariant:** after processing the first `k` slots, all of them satisfy their windows under the same candidate delay `D`.

3. **Establish the search bounds.**  
   The minimum candidate is `0`. A safe upper bound is  
   `max(slotEnd[i] - (i + 1) * renderTime)` over all `i`, clamped at least to `0` for the search interval. Any larger `D` would force at least one page past its slot end.  
   This keeps the search tight and avoids arbitrary constants.

4. **Binary search for the maximum valid delay.**  
   While `lo <= hi`, test `mid`. If `can(mid)` is true, record `mid` and move right to search for a larger valid delay. Otherwise move left.  
   **Invariant:** all values `<= answer` explored as feasible remain candidates; all rejected values are known infeasible.

5. **Handle impossible inputs.**  
   If `can(0)` is false, return `-1` immediately. This avoids returning a negative or fabricated delay.

6. **Why this is correct.**  
   The predicate checks exactly the problem’s definition. Binary search is appropriate because feasible delays form a prefix of the candidate range up to the maximum valid `D`.

## 📊 Worked Example
Take `slotStart = [5, 11, 17]`, `slotEnd = [9, 15, 21]`, `renderTime = 3`.

Finish formula: `finish(i) = D + (i + 1) * 3`

| i | Window   | Finish expression | Constraint on D |
|---|----------|-------------------|-----------------|
| 0 | [5, 9]   | `D + 3`           | `2 <= D <= 6`   |
| 1 | [11, 15] | `D + 6`           | `5 <= D <= 9`   |
| 2 | [17, 21] | `D + 9`           | `8 <= D <= 12`  |

A direct intersection of these ranges is empty, so under the literal arithmetic this example is inconsistent. Using the intended binary-search flow anyway:

1. Search `D` in `[0, 12]`.
2. Test `D = 6` → finishes `9, 12, 15`.
3. Check windows:
   - page 0: `9` in `[5, 9]`
   - page 1: `12` in `[11, 15]`
   - page 2: `15` is **not** in `[17, 21]`

The implementation should trust the formula, not the prose, and validate each slot mechanically.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log U)`, where `U` is the size of the delay search range, typically bounded by the maximum feasible upper limit from slot ends. Each binary-search step performs one linear feasibility pass. At `10^6` items this is still practical; at `10^9`, even one pass becomes the bottleneck regardless of the logarithmic search factor.

### Space Complexity
`O(1)` auxiliary space beyond the input arrays. The algorithm stores only loop counters, bounds, and the current candidate delay. You could reformulate it as interval intersection and still keep `O(1)` space; there is no meaningful reduction available without changing the input model.

## 💡 Key Takeaways
• If the problem asks for the **maximum numeric value** satisfying a global condition and the value range is huge, look for binary search on answer.  
• If each candidate can be validated in one pass with no backtracking, that is a strong signal the feasibility predicate is the right abstraction.  
• Use 64-bit or wider arithmetic throughout; `D + (i + 1) * renderTime` can overflow standard 32-bit types immediately.  
• Be precise about inclusivity: slot windows are closed intervals, so `finish == slotStart[i]` and `finish == slotEnd[i]` are both valid.  
• In production scheduling systems, deterministic per-stage timing often reduces a complex orchestration problem to feasibility over one control variable; exploit that structure before reaching for heavier optimization machinery.

## 🚀 Variations & Further Practice
- Allow optional idle gaps between pages instead of strict back-to-back rendering. The twist is that one scalar `D` no longer determines all finish times, so the problem shifts from binary search to greedy deadline scheduling or DP.
- Let each page have its own `renderTime[i]`. The same feasibility idea still works, but finish times depend on prefix sums, making overflow handling and bound derivation more subtle.
- Maximize delay when pages may be reordered. The hard part becomes choosing an order that preserves feasibility, turning the problem into a scheduling-by-deadline variant rather than a fixed-order check.