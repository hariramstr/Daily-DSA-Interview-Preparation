# Pair Guests for a Tandem Zipline

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given an array of guest weights and a maximum allowed combined weight per tandem ride, compute the minimum number of rides needed to send everyone down the zipline. Each ride can carry at most two guests, and every guest must be assigned exactly once. The non-trivial part is minimizing ride count under a pairing constraint, which rules out naive local choices and makes brute-force pairing infeasible at the input limit of 100,000 guests.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must pack bounded-capacity units efficiently: bin-packing approximations in warehouse fulfillment, pairing jobs onto constrained compute slots, batching requests under payload limits, or assigning passengers to shared transport under safety rules. At scale, brute-force matching explodes combinatorially and destabilizes latency budgets. A sort-plus-two-pointers strategy gives deterministic performance, predictable memory use, and a simple correctness story. That matters in production schedulers and allocation services where throughput, explainability, and worst-case behavior are more important than chasing marginally better but computationally expensive packings.

## 🔍 Problem Statement
You are given:

- `weights[i]`: the weight of the `i`-th guest
- `limit`: the maximum total weight allowed on a single tandem ride

Each ride may carry either:

- one guest alone, or
- two guests together, if their combined weight is `<= limit`

Return the minimum number of rides required so that every guest rides exactly once.

Constraints:

- `1 <= weights.length <= 100000`
- `1 <= weights[i] <= 1000000000`
- `1 <= limit <= 1000000000`
- `weights[i] <= limit` for all `i`, so every guest can always ride alone

Examples:

- `weights = [70, 50, 80, 50], limit = 100` → `3`
- `weights = [40, 60, 55, 45, 80], limit = 100` → `3`

The key constraint is input size: `O(n^2)` pair-search is not viable, so the solution must exploit ordering and make one-pass decisions after sorting.

## 🪜 How to Solve This
1. Read the problem → the objective is not to find *any* valid assignment, but the one using the fewest rides.

2. Each ride holds at most two guests → minimizing rides means maximizing the number of valid pairs.

3. Brute-force pairing is immediately suspect → with up to 100,000 guests, checking many pair combinations is too expensive.

4. Sort the weights → once ordered, the pairing decision becomes structured instead of combinatorial.

5. Look at extremes:
   - If the lightest and heaviest remaining guests can ride together, that is always a good use of capacity.
   - If they cannot, then the heaviest guest cannot pair with anyone else either, because everyone else is heavier than the lightest.

6. That observation suggests two pointers:
   - left pointer at the lightest remaining guest
   - right pointer at the heaviest remaining guest

7. Process one ride per iteration:
   - try to pair `weights[left] + weights[right]`
   - if valid, move both pointers
   - otherwise, send the heaviest alone and move only `right`

8. Count rides until all guests are assigned.

This is the standard greedy + two-pointers shape: sort once, then make locally forced decisions that are globally optimal.

## 🧩 Algorithm Walkthrough
1. **Sort the array in non-decreasing order.**  
   This is what enables the greedy argument. After sorting, the lightest remaining guest is the best candidate to pair with the heaviest remaining guest. Without ordering, you cannot reason locally about whether a heavier guest has any feasible partner.

2. **Initialize two pointers:** `left = 0`, `right = n - 1`, and `rides = 0`.  
   The active interval `[left, right]` represents guests not yet assigned to a ride. This interval shrinks monotonically.

3. **Try to pair the extremes.**  
   If `weights[left] + weights[right] <= limit`, assign them together, increment `left`, decrement `right`, and increment `rides`.  
   Why correct: pairing the heaviest guest with the lightest feasible guest preserves as much room as possible for the remaining guests. Since the lightest works, using any heavier partner would only reduce future flexibility.

4. **Otherwise, send the heaviest guest alone.**  
   If `weights[left] + weights[right] > limit`, then guest `right` cannot pair with anyone in `[left, right - 1]`, because all of them are at least as heavy as `weights[left]`. So decrement `right` and increment `rides`.  
   This is the key invariant: when a pairing fails with the lightest remaining guest, the heaviest remaining guest is forced to ride alone.

5. **Repeat until `left > right`.**  
   Each iteration assigns exactly one ride and removes at least one guest. The algorithm is therefore linear after sorting.

This is a canonical **Two Pointers** greedy pattern: sorted order turns a global matching problem into a sequence of locally forced decisions with a simple invariant over the remaining search space.

## 📊 Worked Example
Example: `weights = [40, 60, 55, 45, 80]`, `limit = 100`

Sorted: `[40, 45, 55, 60, 80]`

| Step | left | right | Pair Checked | Decision | Rides |
|------|------|-------|--------------|----------|-------|
| 1 | 0 (`40`) | 4 (`80`) | `40 + 80 = 120` | `80` rides alone | 1 |
| 2 | 0 (`40`) | 3 (`60`) | `40 + 60 = 100` | pair `(40, 60)` | 2 |
| 3 | 1 (`45`) | 2 (`55`) | `45 + 55 = 100` | pair `(45, 55)` | 3 |

Stop because `left > right`.

Result: `3` rides.

What matters in the trace is the failed first pairing. Once `40 + 80` exceeds the limit, `80` cannot pair with anyone else either, because every other remaining guest is at least `45`, not lighter than `40`. That makes the “send heaviest alone” step not just reasonable, but forced.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting, followed by an `O(n)` two-pointer scan. The sort dominates. At `10^6` elements this is still practical in most production runtimes; at `10^9`, sorting becomes the bottleneck and likely exceeds memory and wall-clock budgets unless external or distributed sorting is used.

### Space Complexity
`O(1)` auxiliary space if sorting is in-place, excluding the sort implementation’s internal stack or runtime overhead. In languages whose sort allocates temporary buffers, effective space may be `O(log n)` or higher. You can reduce overhead only by choosing an in-place sort, with possible trade-offs in implementation complexity.

## 💡 Key Takeaways
- If the problem says “at most two items per group” and asks to minimize the number of groups under a capacity limit, think sorted greedy with two pointers.
- When feasibility depends only on the sum of smallest and largest remaining values, ordering usually collapses a matching problem into a linear scan.
- Use `while left <= right`, not `<`, or you will miss the final unpaired guest when one remains.
- Only advance `left` when a pair is actually formed; advancing both pointers on a failed check silently drops a guest.
- The transferable design insight is that sorted order can turn expensive global allocation into deterministic local decisions with strong invariants and predictable performance.

## 🚀 Variations & Further Practice
- **Boats to Save People / rescue allocation variants** — same core pattern, but useful for reinforcing the proof that a failed lightest+heaviest pairing forces the heaviest to go alone.
- **Generalized capacity packing with more than two items per ride** — becomes much harder because the clean two-pointer invariant breaks; this moves toward bin packing and approximation algorithms.
- **Minimize rides with additional constraints such as compatibility or priority classes** — introduces a second dimension beyond weight, often requiring matching, heaps, or constrained greedy strategies rather than a single sorted pass.