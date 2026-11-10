# Count Drone Pairs Within Charging Difference

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Array, Sorting

---

## 🗂 Problem Overview
Given a sorted array `charge` and a threshold `maxDiff`, count how many index pairs `(i, j)` with `i < j` satisfy `charge[j] - charge[i] <= maxDiff`. The output is the total number of valid pairs, not the pairs themselves. The challenge is scale: with up to `2 * 10^5` elements, the naive `O(n^2)` pair scan is too slow, so the solution must exploit sorted order to aggregate counts efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere you count compatible pairs inside an ordered metric space: matching requests within latency bands, grouping events within time skew in streaming pipelines, pairing bids/offers within spread limits in trading systems, or finding near-duplicate metrics in observability backends. At scale, brute-force pair enumeration collapses under quadratic growth and burns both CPU and tail latency budgets. The two-pointer formulation turns pairwise validation into a linear scan over a sorted sequence, which is the difference between a batch job that misses its SLA and one that fits comfortably inside a hot path.

## 🔍 Problem Statement
You are given an integer array `charge` sorted in non-decreasing order and an integer `maxDiff`. Count the number of index pairs `(i, j)` such that:

- `0 <= i < j < charge.length`
- `charge[j] - charge[i] <= maxDiff`

Constraints:

- `1 <= charge.length <= 2 * 10^5`
- `0 <= charge[i] <= 10^9`
- `charge` is sorted in non-decreasing order
- `0 <= maxDiff <= 10^9`
- The answer may exceed 32-bit range, so use a 64-bit integer type

Examples:

- `charge = [10, 12, 13, 17], maxDiff = 3` → `3`
- `charge = [4, 4, 5, 8, 9], maxDiff = 1` → `4`

The key constraint is input size. An `O(n^2)` nested loop can require tens of billions of comparisons, so the algorithm must leverage the sorted array to count many pairs at once.

## 🪜 How to Solve This
1. Read the condition carefully → it depends on `charge[j] - charge[i]`, not arbitrary pair logic. Since the array is already sorted, absolute difference simplifies to a one-sided difference for `i < j`.

2. Notice what sorted order buys you → for any fixed left index `i`, if some right index `j` is valid, then every index between `i+1` and `j` is also valid. That means valid partners form a contiguous range.

3. Contiguous valid range → think sliding window / two pointers. Maintain a window `[left, right]` where all elements from `left` to `right` are within `maxDiff` of `charge[left]`.

4. Instead of checking every pair, count them in bulk → when the window is valid, the current `right` can pair with every earlier index inside the window. That contributes `right - left` new pairs immediately.

5. Advance monotonically → move `right` forward once per iteration, and move `left` only when the window becomes invalid. No pointer ever moves backward, which is why the scan is linear after sorting.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers pattern.**  
   Maintain two indices: `left` and `right`. `right` expands the candidate set; `left` shrinks it when the difference constraint is violated. This pattern is the right abstraction because the array is sorted and the validity predicate is monotonic with respect to pointer movement.

2. **Initialize `left = 0`, `count = 0`.**  
   Iterate `right` from `0` to `n - 1`. At each step, treat `charge[right]` as the newest drone entering the current window.

3. **Restore the window invariant.**  
   While `charge[right] - charge[left] > maxDiff`, increment `left`.  
   Why this is correct: if the smallest value in the window is too far from `charge[right]`, no pair involving that `left` and this `right` can be valid. Because the array is sorted, moving `left` rightward is the only way to reduce the difference.

4. **Count new valid pairs ending at `right`.**  
   Once the window is valid, every index `k` in `[left, right - 1]` satisfies `charge[right] - charge[k] <= maxDiff`. That contributes exactly `right - left` pairs.  
   Invariant: after adjustment, the window `[left, right]` is the maximal valid suffix ending at `right`.

5. **Accumulate in 64-bit integer space.**  
   In the worst case, all pairs are valid, so the answer is `n * (n - 1) / 2`, which exceeds 32-bit range for `n = 2 * 10^5`.

6. **Finish after one pass.**  
   Each pointer advances at most `n` times, so the full algorithm is `O(n)` on top of the already-sorted input.

## 📊 Worked Example
Example: `charge = [4, 4, 5, 8, 9]`, `maxDiff = 1`

| right | charge[right] | left after shrink | valid window | new pairs | total |
|------:|---------------:|------------------:|-------------|----------:|------:|
| 0 | 4 | 0 | `[4]` | 0 | 0 |
| 1 | 4 | 0 | `[4, 4]` | 1 | 1 |
| 2 | 5 | 0 | `[4, 4, 5]` | 2 | 3 |
| 3 | 8 | 3 | `[8]` | 0 | 3 |
| 4 | 9 | 3 | `[8, 9]` | 1 | 4 |

Trace:
1. At `right = 2`, `5 - 4 <= 1`, so `5` pairs with both earlier `4`s.
2. At `right = 3`, the window breaks: `8 - 4 > 1`, so `left` advances until only `[8]` remains.
3. At `right = 4`, `9 - 8 <= 1`, so one new pair is added.

Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` because each pointer moves monotonically from left to right at most once. The dominant work is the single scan with occasional `left` increments. At `10^6` elements this remains practical; at `10^9`, even linear scan cost becomes infrastructure-scale, but it is still asymptotically optimal for exact counting.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only indices and the running total; the input array owns the data footprint. Space cannot be meaningfully reduced below constant unless you change the input representation, at which point I/O dominates anyway.

## 💡 Key Takeaways
- If the input is sorted and the validity condition is monotonic across a range, look for a sliding window instead of pairwise enumeration.
- If each new element can form valid pairs with a contiguous block of prior elements, you can usually count `block size` directly rather than listing pairs.
- Use `while`, not `if`, when shrinking the window; multiple left shifts may be required to restore validity.
- Count contribution as `right - left` only after the window is valid; doing it before shrink logic creates classic off-by-one errors.
- In production systems, monotonic-pointer scans are a core technique for converting quadratic compatibility checks into predictable linear passes over ordered data.

## 🚀 Variations & Further Practice
- Count pairs in an **unsorted** array with the same difference constraint: the twist is that you must sort first or use a frequency/range structure, which changes both complexity and data ownership.
- Count the number of **triplets** where `max(window) - min(window) <= K`: same window idea, but combinatorics and validity maintenance become more subtle.
- For each index, return the number of valid partners within distance `K`: same two-pointer core, but now you need per-element attribution instead of one global aggregate.