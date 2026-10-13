# Closest Badge Pair Across Two Sorted Gates

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** two-pointers, array, sorting

---

## 🗂 Problem Overview
Given two sorted integer arrays, `gateA` and `gateB`, find the minimum absolute difference between any pair `(a, b)` where `a` comes from `gateA` and `b` comes from `gateB`. If either array is empty, return `-1`. The challenge is not the comparison itself but doing it efficiently: both arrays may contain up to `2 * 10^5` elements, so checking every cross-pair is too slow. The sorted-order guarantee is the key structural constraint.

## 🌍 Engineering Impact
This pattern shows up anywhere two ordered streams must be reconciled by nearest value: timestamp alignment in streaming pipelines, matching event logs across replicas, correlating metrics from independent collectors, merge phases in search indexing, and diffing sorted symbol tables in compilers or linkers. At scale, the naive Cartesian comparison explodes into latency and cache-unfriendly work. The two-pointer scan turns the problem into a single forward pass, which is exactly what production systems want: predictable memory use, linear throughput, and a design that composes naturally with iterators, paged reads, and external-memory processing.

## 🔍 Problem Statement
You are given two integer arrays, `gateA` and `gateB`, each already sorted in non-decreasing order. Values may be negative, repeated, or identical across arrays. Your task is to return the minimum value of `|a - b|` over all valid cross-array pairs, where `a ∈ gateA` and `b ∈ gateB`.

If either array is empty, no pair can be formed, so return `-1`.

Constraints:
- `0 <= gateA.length, gateB.length <= 2 * 10^5`
- `-10^9 <= gateA[i], gateB[i] <= 10^9`
- Both arrays are sorted in non-decreasing order
- Duplicates are allowed

Examples:
- `gateA = [3, 8, 15, 21]`, `gateB = [1, 9, 18, 30]` → `1`
- `gateA = [-10, -4, 0, 7]`, `gateB = [-6, -3, 2, 11]` → `1`

The key constraint is scale: quadratic comparison is unacceptable, so the sorted input must drive the algorithmic choice.

## 🪜 How to Solve This
1. Start with the brute-force mental model → compare every `gateA[i]` with every `gateB[j]`, track the smallest difference.  
   That works functionally, but `O(n * m)` is dead on arrival for arrays this large.

2. Notice the arrays are already sorted → that means relative ordering gives us directional information.  
   If `gateA[i] < gateB[j]`, then pairing `gateA[i]` with any later value in `gateB` cannot reduce the gap by moving `j` forward alone.

3. That observation suggests a synchronized scan → keep one pointer in each array and compare current values.  
   Record the current absolute difference, then advance the pointer pointing to the smaller value.

4. Why move the smaller side? → because the only way to possibly get closer is to increase the smaller number; moving the larger one forward would usually make the gap worse or leave it non-improving.

5. Continue until one array is exhausted.  
   Every step discards one impossible prefix candidate without missing the optimum, giving a linear pass over both arrays.

## 🧩 Algorithm Walkthrough
1. **Handle the empty-input case first.**  
   If `gateA` or `gateB` is empty, return `-1`. This is required by the contract and avoids undefined pointer access.

2. **Initialize the Two Pointers pattern.**  
   Set `i = 0`, `j = 0`, and `best = +∞`. The abstraction here is explicit: two sorted sequences, each consumed left-to-right, with local comparisons guiding which side can be safely advanced.

3. **Compare the current pair.**  
   At each step, compute `diff = abs(gateA[i] - gateB[j])` and update `best = min(best, diff)`.  
   This preserves the invariant that `best` is the smallest difference seen among all examined pairs.

4. **Early exit on zero.**  
   If `diff == 0`, return `0` immediately. Absolute difference cannot improve beyond zero, so this is globally optimal.

5. **Advance the pointer at the smaller value.**  
   - If `gateA[i] < gateB[j]`, increment `i`
   - Otherwise, increment `j`

   Why this is correct: suppose `gateA[i] < gateB[j]`. Any earlier or current element in `gateB` has already been considered or is not smaller than `gateB[j]`, and any later element in `gateB` is at least as large, so keeping `i` fixed cannot produce a better match than the current comparison. The only remaining chance is to move `i` forward toward `gateB[j]`.

6. **Repeat until one pointer reaches the end.**  
   Once one array is exhausted, no further cross-array pair exists. Return `best`.

The invariant throughout is strong: all skipped pairs are dominated by a comparison already made, so no optimal answer is lost.

## 📊 Worked Example
Use `gateA = [3, 8, 15, 21]` and `gateB = [1, 9, 18, 30]`.

| Step | `i` | `j` | `gateA[i]` | `gateB[j]` | `diff` | `best` | Move |
|---|---:|---:|---:|---:|---:|---:|---|
| 1 | 0 | 0 | 3 | 1 | 2 | 2 | `j++` |
| 2 | 0 | 1 | 3 | 9 | 6 | 2 | `i++` |
| 3 | 1 | 1 | 8 | 9 | 1 | 1 | `i++` |
| 4 | 2 | 1 | 15 | 9 | 6 | 1 | `j++` |
| 5 | 2 | 2 | 15 | 18 | 3 | 1 | `i++` |
| 6 | 3 | 2 | 21 | 18 | 3 | 1 | `j++` |
| 7 | 3 | 3 | 21 | 30 | 9 | 1 | `i++` |

`i` reaches the end, so the scan stops. The minimum difference found is `1`, from the pair `(8, 9)`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = gateA.length` and `m = gateB.length`. Each pointer moves forward at most once per element, so the dominant operation is a single linear scan. At `10^6` total elements this is practical; at `10^9`, runtime becomes I/O-bound but still asymptotically optimal for sequential access.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices and the current best difference; the input arrays own the data footprint. Space cannot be meaningfully reduced further unless the inputs are streamed, in which case the same logic still works with iterator state.

## 💡 Key Takeaways
- If two arrays are already sorted and the objective is based on relative closeness, nearest match, or merge-like comparison, Two Pointers should be the first pattern considered.
- A strong signal is that you only need one global optimum over cross-array pairs, not all pairwise results; sorted order can usually eliminate large parts of the search space.
- Return `-1` before entering the scan if either array is empty; otherwise pointer logic becomes invalid even though the algorithm itself is straightforward.
- Be careful to advance exactly one pointer per step, specifically the one at the smaller value; advancing the wrong side breaks correctness and can skip the optimum.
- In production systems, preserving sorted order upstream often converts expensive reconciliation into a linear streaming pass, which is a major architectural win.

## 🚀 Variations & Further Practice
- Find the actual pair `(a, b)` instead of only the minimum difference; the twist is preserving tie-breaking rules such as smallest `a`, smallest `b`, or first occurrence.
- Solve the unsorted version efficiently; the conceptual twist is deciding whether to sort first (`O(n log n + m log m)`) or use another structure depending on update/query patterns.
- Extend to `k` sorted streams and find the minimum range containing at least one element from each; the harder part is coordinating multiple pointers, typically with a min-heap plus a running maximum.