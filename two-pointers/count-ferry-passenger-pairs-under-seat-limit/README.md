# Count Ferry Passenger Pairs Under Seat Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Array

---

## 🗂 Problem Overview
Given an array of passenger weights and a bench weight limit, count how many distinct index pairs `(i, j)` with `i < j` can sit together without exceeding the limit. The output is the total number of valid pairs, not the pairs themselves. The challenge is scale: with up to 200,000 passengers, checking every pair in `O(n^2)` is too expensive, so the solution must exploit ordering to count many pairs in one step.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must count feasible combinations under a threshold without enumerating all combinations: ad auction candidate pruning, search ranking pairwise eligibility, capacity-aware batching in logistics, and streaming pipelines that form bounded-size groups. At small scale, nested loops are tolerable; at production scale, they become latency spikes, CPU waste, and memory pressure if pairs are materialized. Sorting plus two pointers turns pairwise feasibility into a monotonic scan, which is exactly the kind of structural simplification that keeps high-volume services predictable and cheap under load.

## 🔍 Problem Statement
You are given an integer array `weights` where `weights[i]` is the weight of the `i`-th passenger, and an integer `limit` representing the maximum total weight a bench can support. Count the number of distinct pairs of different passengers `(i, j)` such that `0 <= i < j < n` and `weights[i] + weights[j] <= limit`.

Constraints:

- `2 <= weights.length <= 200000`
- `1 <= weights[i] <= 1000000000`
- `1 <= limit <= 2000000000`
- The result may exceed 32-bit integer range, so use a 64-bit type where needed.

Examples:

- `weights = [2, 3, 4, 5], limit = 7` → `4`
- `weights = [1, 1, 2, 2, 3], limit = 3` → `6`

The key constraint is input size: `O(n^2)` pair enumeration is infeasible, which forces an `O(n log n)` or better counting strategy.

## 🪜 How to Solve This
1. Read the problem → we need a **count of pairs under a sum threshold**, not the actual pair list.
2. Brute force would test every `(i, j)` → that is `O(n^2)`, which fails at `n = 200,000`.
3. When a condition depends only on `weights[i] + weights[j]`, sorting is the first signal. Sorting gives order, and order often creates monotonic behavior.
4. After sorting, fix the lightest remaining passenger at `left` and compare with the heaviest remaining passenger at `right`.
5. If `weights[left] + weights[right] <= limit`, then every passenger between `left+1` and `right` also forms a valid pair with `left`, because they are no heavier than `weights[right]`.
6. That means you can add `right - left` pairs at once instead of checking them individually.
7. If the sum is too large, the heaviest passenger cannot pair with `left`, so move `right` leftward to reduce the sum.
8. Repeat until pointers cross. This is the classic sorted two-pointer counting pattern: use monotonicity to collapse many pair checks into one arithmetic update.

## 🧩 Algorithm Walkthrough
1. **Sort the array in non-decreasing order.**  
   This enables the monotonic property the algorithm depends on: for a fixed `left`, moving `right` left decreases the pair sum. Without sorting, there is no safe way to count ranges of valid partners.

2. **Initialize two pointers:** `left = 0`, `right = n - 1`, and `count = 0`.  
   `left` tracks the lightest unprocessed passenger; `right` tracks the heaviest candidate still under consideration.

3. **Check the pair sum `weights[left] + weights[right]`.**  
   This is the decision point for the Two Pointers pattern. The invariant is that all pairs entirely outside the current `[left, right]` window have already been correctly accounted for or discarded.

4. **If the sum is within the limit, add `right - left` to the answer.**  
   Why this is correct: because the array is sorted, every index `k` with `left < k <= right` satisfies  
   `weights[left] + weights[k] <= weights[left] + weights[right] <= limit`.  
   So `left` forms valid pairs with every remaining passenger up to `right`.

5. **Advance `left++` after counting.**  
   Once all pairs involving `left` are counted, there is no reason to revisit that passenger.

6. **If the sum exceeds the limit, decrement `right--`.**  
   Why this is correct: if the heaviest candidate fails with the lightest candidate, it cannot succeed with any heavier passenger. So `right` cannot participate in a valid pair with the current `left`, and shrinking from the right is the only productive move.

7. **Stop when `left >= right`.**  
   At that point, no distinct pair remains. The final `count` is the total number of valid pairs.

This is the right abstraction because Two Pointers exploits sorted monotonic structure to replace quadratic pair testing with a linear scan after sort.

## 📊 Worked Example
Example: `weights = [2, 3, 4, 5]`, `limit = 7`

Sorted array is already `[2, 3, 4, 5]`.

| Step | left | right | Pair | Sum | Action | Count |
|---|---:|---:|---|---:|---|---:|
| 1 | 0 | 3 | (2,5) | 7 | Valid → add `3` pairs: (2,3), (2,4), (2,5) | 3 |
| 2 | 1 | 3 | (3,5) | 8 | Too large → move `right` left | 3 |
| 3 | 1 | 2 | (3,4) | 7 | Valid → add `1` pair: (3,4) | 4 |

Stop because `left` becomes `2` and `right` is `2`.

Result: `4`.

The important observation is step 1: once `(2,5)` is valid, every passenger between indices `1` and `3` also pairs validly with `2`. That single comparison counts three pairs at once.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` overall. Sorting dominates; the two-pointer scan is `O(n)` because each pointer moves in one direction at most `n` times. At `10^6` elements, this is still practical in optimized environments. At `10^9`, even sorting becomes operationally unrealistic, so the bottleneck shifts from algorithmic complexity to system capacity.

### Space Complexity
`O(1)` auxiliary space beyond the sort if the language/runtime provides in-place sorting; otherwise it is `O(n)` depending on the sorting implementation. The main algorithm itself stores only two pointers and a 64-bit counter. Reducing sort space usually means trading implementation simplicity for lower-level control.

## 💡 Key Takeaways
- If the problem asks for the **number of pairs** satisfying `a[i] + a[j] <= K`, sorting plus two pointers should be the default candidate.
- When one successful comparison implies a whole contiguous range is also valid, you can usually count in bulk instead of enumerating pairs.
- Use a 64-bit accumulator: the number of valid pairs can be on the order of `n(n-1)/2`, which overflows 32-bit integers.
- Be precise about the increment: when a pair is valid, add `right - left`, not `right - left + 1`, because a passenger cannot pair with itself.
- The transferable design insight is to exploit monotonic structure after preprocessing; a one-time ordering cost often converts an intractable pairwise scan into a predictable linear pass.

## 🚀 Variations & Further Practice
- Count pairs whose sum is **strictly less than** a threshold or lies within a range `[L, R]`; the twist is handling inclusive/exclusive bounds cleanly, often via two counting passes.
- Return the **maximum number of disjoint pairs** under the same limit; this changes the problem from counting all valid combinations to greedy matching.
- Extend from pairs to **triplets under a limit**; the same sorting idea applies, but the complexity becomes `O(n^2)` and the counting logic is more subtle.