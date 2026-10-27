# Pair Gondolas for Minimum Height Imbalance

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given an even-length array `heights`, pair every visitor into exactly one 2-person gondola and minimize the largest height difference among all formed pairs. Return that minimum possible worst-case difference. The challenge is global, not local: a pairing that looks good for one gondola can force a much worse gap elsewhere. With up to `200000` visitors, brute-force pairing or backtracking is infeasible, so the solution must exploit structure.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must form fixed-size pairs while minimizing worst-case imbalance: task co-location in schedulers, shard rebalancing, GPU batch packing, ride-sharing seat assignment, and warehouse bin pairing. At scale, arbitrary matching creates pathological tails: one overloaded shard, one slow batch, one unfair allocation. The relevant design goal is minimizing the maximum deviation, not average deviation. Sorting plus greedy local decisions often converts an exponential assignment space into a deterministic `O(n log n)` pipeline, which is exactly the kind of reduction that keeps placement and balancing logic operational under production load.

## 🔍 Problem Statement
You are given an integer array `heights` where `heights[i]` is the height of the `i`-th visitor. The array length is always even, and every visitor must be assigned to exactly one gondola containing exactly two people.

For a given complete pairing, each gondola contributes a height difference equal to the absolute difference between its two riders. The quality of that pairing is the maximum such difference across all gondolas. Your task is to return the minimum possible quality over all valid pairings.

Constraints:

- `2 <= heights.length <= 200000`
- `heights.length` is even
- `1 <= heights[i] <= 1000000000`

Examples:

- `heights = [150, 160, 155, 168]` → `8`
- `heights = [172, 180, 169, 171, 175, 178]` → `3`

The key constraint is input size: enumerating pairings is combinatorial, so the algorithm must be near-linear after sorting.

## 🪜 How to Solve This
1. Read the objective carefully → we are not minimizing the sum of pair differences; we are minimizing the **largest** pair difference.

2. When the objective is a worst-case gap over numeric values, sorting is usually the first move. Once heights are ordered, the notion of “close together” becomes explicit.

3. After sorting, ask what kind of pairing could avoid large gaps. If you skip over a nearby height and pair someone with a farther person, that larger gap has to appear somewhere.

4. That suggests a greedy structure: pair neighbors in sorted order. Adjacent values produce the smallest available local gaps.

5. Why not cross pairs? Because crossing forces at least one pair to span a wider interval than some adjacent alternative. For a minimax objective, that is exactly what you want to avoid.

6. So the problem reduces to: sort, scan in steps of two, compute each adjacent difference, and track the maximum. That maximum is the best possible answer.

## 🧩 Algorithm Walkthrough
1. **Sort the array in nondecreasing order.**  
   This is the enabling step. Before sorting, “best partner” is undefined globally. After sorting, nearby values are contiguous, and any non-adjacent pairing visibly spans over intermediate candidates.

2. **Apply the greedy pairing rule: pair `heights[0]` with `heights[1]`, `heights[2]` with `heights[3]`, and so on.**  
   This is a **Two Pointers / ordered greedy** pattern, though the pointers advance together rather than from opposite ends. The abstraction fits because we consume the sorted sequence in fixed-size windows while preserving a structural invariant.

3. **For each pair, compute the difference and update the running maximum.**  
   The invariant is: after processing the first `2k` elements, we have formed the optimal pairing for that prefix under the no-crossing structure induced by sorting.

4. **Why is adjacent pairing correct?**  
   In a sorted array, any pairing with crossings can be uncrossed without increasing the maximum pair difference. Repeatedly uncrossing transforms any optimal solution into one that pairs adjacent elements. Therefore, an optimal solution exists in this form.

5. **Return the maximum adjacent difference encountered.**  
   That value is the minimum achievable worst gondola imbalance.

## 📊 Worked Example
Take `heights = [172, 180, 169, 171, 175, 178]`.

After sorting: `[169, 171, 172, 175, 178, 180]`

| Step | Pair Indices | Pair Values   | Difference | Running Max |
|------|--------------|---------------|------------|-------------|
| 1    | `(0, 1)`     | `(169, 171)`  | `2`        | `2`         |
| 2    | `(2, 3)`     | `(172, 175)`  | `3`        | `3`         |
| 3    | `(4, 5)`     | `(178, 180)`  | `2`        | `3`         |

Result: `3`

Why this works: every pair uses adjacent sorted values, so no gondola spans over a closer candidate. If we tried a non-adjacent pairing like `(169, 175)`, that already creates a gap of `6`, which is worse than the final answer. The scan confirms the optimal worst-case difference is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting; the post-sort scan is `O(n)`. For `10^6` elements, this is still practical in optimized runtimes because the sort dominates but remains manageable. At `10^9` elements, in-memory sorting is no longer realistic; the bottleneck becomes system architecture, not algorithmic elegance.

### Space Complexity
`O(1)` auxiliary space if sorting in place, excluding the sort implementation's internal stack or runtime overhead. In languages with non-in-place sort behavior, effective space may be `O(n)`. Reducing space usually means choosing an in-place sort, with possible trade-offs in stability or implementation complexity.

## 💡 Key Takeaways
- If the objective is to minimize the **maximum pairwise gap** over numeric values, sorting is a strong signal that adjacency may define the optimal structure.
- When arbitrary pairings explode combinatorially, look for an exchange argument that converts the search space into a canonical greedy form.
- Iterate in steps of `2` after sorting; incrementing by `1` accidentally creates overlapping pairs and invalid reuse of visitors.
- Track the **maximum** pair difference, not the sum or minimum; this is a minimax problem, and using the wrong aggregate silently produces incorrect logic.
- In production allocation systems, minimizing tail imbalance often matters more than optimizing average quality; greedy-on-sorted-order is a common way to control that tail deterministically.

## 🚀 Variations & Further Practice
- **Minimize the maximum pair sum under capacity constraints:** same pairing structure, but the objective shifts from difference to sum feasibility, often leading to opposite-end two-pointer reasoning.
- **Pair elements to minimize total absolute difference:** easier objective, but useful contrast; it reinforces why adjacent pairing also emerges under a sum-based cost function.
- **Form groups of size `k` minimizing the maximum intra-group spread:** the conceptual twist is moving from pairs to windows/clusters, which often requires binary search on the answer plus greedy feasibility checking.