# Count Valid Song Duos Under Stage Time

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an array of song durations and a stage time limit, count how many distinct index pairs `(i, j)` with `i < j` have `durations[i] + durations[j] <= stageLimit`. Equal values at different indices still form distinct pairs. The challenge is scale: with up to 200,000 songs, checking all `O(n^2)` pairs is too slow. The goal is an efficient counting strategy that avoids enumerating every candidate duo.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to count feasible pairings under a threshold without materializing all combinations. Examples include ad-serving candidate joins under latency budgets, search ranking feature pairing under score caps, warehouse batching under weight limits, and streaming pipelines that count event pairs within bounded cost. At production scale, brute force collapses under quadratic growth: CPU spikes, cache behavior degrades, and latency becomes non-linear. Sorting plus two pointers converts pairwise feasibility into a monotonic scan, enabling predictable throughput, simpler capacity planning, and a design that remains viable as datasets move from thousands to millions of records.

## 🔍 Problem Statement
You are given:

- `durations`, an integer array where `durations[i]` is the duration of the `i`-th song
- `stageLimit`, the maximum allowed combined duration for a duo

Return the number of distinct pairs of indices `(i, j)` such that:

- `0 <= i < j < n`
- `durations[i] + durations[j] <= stageLimit`

Constraints:

- `2 <= durations.length <= 200000`
- `1 <= durations[i] <= 1000000000`
- `1 <= stageLimit <= 2000000000`
- The result fits in a signed 64-bit integer

Examples:

- `durations = [120, 90, 150, 60, 80], stageLimit = 210` → `7`
- `durations = [200, 40, 40, 170, 30], stageLimit = 210` → `5`

The key constraint is input size. `O(n^2)` pair enumeration is not acceptable at 200,000 elements, so the solution must exploit ordering and monotonicity.

## 🪜 How to Solve This
1. Start from the brute-force interpretation → for each song, test every later song. That is obviously correct, but `n = 200,000` makes quadratic work infeasible.

2. Notice the condition is only about the **sum** of two values relative to a threshold. That usually suggests sorting, because sorted order turns “which partners are valid?” into a contiguous range question.

3. After sorting, fix the smallest remaining value on the left and compare it with the largest remaining value on the right.

4. If `durations[left] + durations[right] <= stageLimit`, then not just that pair works — **every index between `left+1` and `right` also works with `left`**, because those values are no larger than `durations[right]`.

5. That means you can count `right - left` pairs at once instead of checking them individually.

6. If the sum is too large, the right value is the only thing that can be reduced while preserving all smaller left-side options, so move `right` inward.

7. This gives a linear scan after sorting: one pass, no backtracking, no missed pairs, no double counting.

## 🧩 Algorithm Walkthrough
1. **Sort the array in non-decreasing order.**  
   This is what makes the problem tractable. Once sorted, pair feasibility becomes monotonic: decreasing the right pointer can only decrease the sum, and increasing the left pointer can only increase it.

2. **Initialize two pointers:** `left = 0`, `right = n - 1`, and a 64-bit counter `count = 0`.  
   The invariant is that all pairs outside the current `[left, right]` window have already been fully accounted for or ruled out.

3. **Check the pair `(left, right)`.**  
   Compute `durations[left] + durations[right]`.

4. **If the sum is within `stageLimit`, count a whole block.**  
   Because the array is sorted, every index `k` where `left < k <= right` satisfies  
   `durations[left] + durations[k] <= durations[left] + durations[right] <= stageLimit`.  
   So all `right - left` pairs starting at `left` are valid. Add that quantity to `count`, then increment `left`.

5. **If the sum exceeds `stageLimit`, shrink from the right.**  
   The current largest value is too large to pair with `durations[left]`. Since any larger left would only increase the sum, no pair involving this `right` and current `left` works. Decrement `right`.

6. **Repeat until `left >= right`.**  
   Each move strictly shrinks the search space, so the scan is linear after sorting.

This is the canonical **Two Pointers** pattern: use sorted order plus a monotonic predicate to count ranges in aggregate rather than evaluating pairs one by one.

## 📊 Worked Example
Example: `durations = [120, 90, 150, 60, 80]`, `stageLimit = 210`

Sorted: `[60, 80, 90, 120, 150]`

| Step | left | right | Values      | Sum | Action | Count |
|------|------|-------|-------------|-----|--------|-------|
| 1 | 0 | 4 | 60, 150 | 210 | valid → add `4` | 4 |
| 2 | 1 | 4 | 80, 150 | 230 | too large → `right--` | 4 |
| 3 | 1 | 3 | 80, 120 | 200 | valid → add `2` | 6 |
| 4 | 2 | 3 | 90, 120 | 210 | valid → add `1` | 7 |

Stop when `left == right`.

Why step 1 adds 4: once `60 + 150` fits, `60` also pairs with `120`, `90`, and `80`. Sorting guarantees all values between `left` and `right` are no larger than `150`, so they are all valid with `60`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by a single `O(n)` two-pointer scan. Overall complexity is `O(n log n)`. At `10^6` elements this is still operationally realistic in optimized runtimes; at `10^9`, even sorting becomes infrastructure-scale and requires distributed or external-memory strategies.

### Space Complexity
The scan itself is `O(1)` extra space beyond the sort. Total space is typically `O(log n)` to `O(n)` depending on the language’s sorting implementation. You can only reduce this by using an in-place sort, trading away stability and sometimes implementation simplicity.

## 💡 Key Takeaways
- If the problem asks for counting pairs under a threshold and brute force is too large, sorting plus two pointers should be one of the first patterns you test.
- A strong signal is monotonic feasibility: after sorting, if one extreme pair works or fails, that result tells you something about an entire range of neighboring pairs.
- When `durations[left] + durations[right] <= stageLimit`, add `right - left`, not `1`; missing this turns the algorithm back into near-brute-force thinking.
- Use a 64-bit accumulator for the answer, and be careful to stop at `left < right` so you never pair an element with itself.
- The transferable design insight is to exploit ordering so a local comparison lets you aggregate whole classes of valid combinations instead of materializing them individually.

## 🚀 Variations & Further Practice
- Count pairs whose sum is **strictly less than** a target or lies within a range `[L, R]`; the twist is translating the same scan into inclusive/exclusive boundary counting.
- Return the **actual pair values or indices** instead of just the count; the harder part is output size, which can become quadratic even when counting is efficient.
- Extend from pairs to **triplets under a threshold**; the same sorted two-pointer idea applies inside an outer loop, raising complexity to `O(n^2)` and making duplicate handling more subtle.