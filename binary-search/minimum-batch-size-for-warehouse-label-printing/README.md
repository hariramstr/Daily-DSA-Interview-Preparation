# Minimum Batch Size for Warehouse Label Printing

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, array, greedy

---

## 🗂 Problem Overview
Given an array `labels` and an integer `d`, split the orders into at most `d` contiguous shifts so that no shift exceeds a chosen batch-size limit. The goal is to return the smallest limit that makes the schedule feasible. The difficulty is that orders must stay in order, cannot be split, and `labels.length` can reach `100000`, which rules out exhaustive partitioning or dynamic programming with quadratic state.

## 🌍 Engineering Impact
This pattern shows up anywhere work must be partitioned sequentially under a hard capacity bound: log segment compaction, streaming micro-batch sizing, warehouse wave planning, tape/archive chunking, and distributed job scheduling where task order is fixed by upstream dependencies. At scale, brute-force partition search collapses under combinatorial growth, while overprovisioning capacity wastes money and throughput. Binary search on a monotonic feasibility predicate gives a clean decision boundary: “is this capacity enough?” That separation matters operationally because it turns an optimization problem into repeated linear validation, which is predictable, testable, and easy to reason about under production load.

## 🔍 Problem Statement
You are given:

- `labels[i]`: number of labels required for the `i-th` order
- `d`: maximum number of shifts available

You must partition `labels` into at most `d` contiguous groups. Each group represents one shift, and the sum of labels in a group cannot exceed the printer batch-size limit. Return the minimum feasible limit.

Constraints:

- `1 <= labels.length <= 100000`
- `1 <= labels[i] <= 1000000000`
- `1 <= d <= labels.length`

Examples:

- `labels = [8, 5, 3, 7, 6], d = 3` → `13`
- `labels = [10, 2, 4, 9, 3], d = 2` → `16`

Important edge conditions:

- The answer can never be smaller than `max(labels)`, because one order cannot be split.
- The answer can never exceed `sum(labels)`, because one shift could process everything if `d = 1`.
- The large input size strongly suggests `O(n log S)` rather than `O(n^2)` or partition DP.

## 🪜 How to Solve This
1. Read the problem → this is not about finding the partition itself first; it is about finding the smallest **capacity** that makes some valid partition possible.

2. Notice the hard constraints:
   - orders stay in original order
   - each shift is contiguous
   - an order cannot be split  
   That means for any fixed batch limit, feasibility can be checked greedily in one pass.

3. Ask the key question: if batch size `x` works, does any larger size also work? Yes. That monotonicity is the signal for **binary search on the answer**.

4. Define the search range:
   - lower bound = largest single order
   - upper bound = total labels across all orders

5. For a candidate limit `mid`, scan left to right:
   - keep adding orders to the current shift
   - if adding the next order exceeds `mid`, start a new shift
   - count how many shifts are needed

6. If the scan needs more than `d` shifts, `mid` is too small. Otherwise, it is feasible, so try smaller.

That reduces an exponential partitioning problem to repeated linear feasibility checks.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer + Greedy Feasibility Check.**  
   The optimization target is the minimum possible maximum group sum. The reason binary search applies is monotonicity: if capacity `x` is sufficient, every `x' > x` is also sufficient.

2. **Set the search bounds.**  
   Let `left = max(labels)` and `right = sum(labels)`.  
   - `left` is mandatory because no order can be split.  
   - `right` is always feasible because one shift can process all orders when capacity is that large.

3. **Pick a midpoint capacity `mid`.**  
   This is the candidate batch-size limit we are testing. The invariant is that the true answer always lies within `[left, right]`.

4. **Run a greedy validation pass.**  
   Traverse `labels` in order, maintaining `currentSum` for the active shift and `shiftsUsed`.  
   - If `currentSum + labels[i] <= mid`, keep the order in the current shift.  
   - Otherwise, start a new shift with `labels[i]`.

   This greedy rule is correct because delaying a split never increases the number of shifts needed for a fixed capacity; packing each shift as much as possible minimizes shift count.

5. **Use the validation result to shrink the search space.**  
   - If `shiftsUsed <= d`, `mid` is feasible, so record it implicitly by moving `right = mid`.  
   - If `shiftsUsed > d`, `mid` is infeasible, so move `left = mid + 1`.

6. **Terminate when `left == right`.**  
   At that point, the interval has collapsed to the minimum feasible capacity. The invariant maintained throughout is: all values below `left` are infeasible, all values at or above `right` are feasible.

## 📊 Worked Example
Example: `labels = [8, 5, 3, 7, 6]`, `d = 3`

Initial bounds: `left = 8`, `right = 29`

| mid | Greedy grouping under `mid` | shifts used | Feasible? |
|---|---|---:|---|
| 18 | `[8,5,3] [7,6]` | 2 | Yes |
| 13 | `[8,5] [3,7] [6]` | 3 | Yes |
| 10 | `[8] [5,3] [7] [6]` | 4 | No |
| 12 | `[8] [5,3] [7] [6]` | 4 | No |

Trace:
1. `mid = 18` works, so search left half.
2. `mid = 13` also works, so try smaller.
3. `mid = 10` fails because greedy needs 4 shifts.
4. `mid = 12` still fails.
5. Bounds converge to `13`.

Answer: `13`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log S)`, where `n = labels.length` and `S = sum(labels) - max(labels) + 1` is the numeric search range. Each binary-search step performs one linear feasibility scan. In practice this is efficient even for `10^6` elements; the logarithmic factor grows with value magnitude, not partition count.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only running sums, bounds, and a shift counter. No extra arrays or DP tables are needed. Space cannot be meaningfully reduced further without changing the execution model, since the input itself dominates memory.

## 💡 Key Takeaways
- If the problem asks for the **minimum capacity / maximum load / threshold** that makes a schedule possible, check whether feasibility is monotonic and binary-search the answer.
- Fixed order + contiguous groups + “at most `d` partitions” is a strong signal that a greedy validation pass will work for each candidate threshold.
- Do not binary-search below `max(labels)`; any smaller value is invalid because a single order cannot be split across shifts.
- Be precise about the feasibility condition: using **at most `d`** shifts is valid, so `shiftsUsed <= d` must count as success.
- In production systems, separating optimization from validation often yields simpler, more testable designs: a monotonic predicate plus search is easier to reason about than direct combinatorial construction.

## 🚀 Variations & Further Practice
- **Split Array Largest Sum**: same core problem, but framed as partitioning an array into `k` subarrays minimizing the largest subarray sum; the conceptual twist is recognizing it despite different domain language.
- **Capacity To Ship Packages Within D Days**: identical binary-search-on-capacity structure, with shipping capacity instead of batch size; the twist is mapping “days” to contiguous processing windows.
- **Allocate Minimum Number of Pages**: similar partitioning under contiguity constraints, but often discussed with students/books; the harder part is handling exact-vs-at-most partition interpretations consistently.