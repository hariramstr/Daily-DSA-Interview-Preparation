# Pair Contestants for a Canoe Ride

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given an array of contestant weights and a canoe weight limit, determine the minimum number of two-person canoes required to transport everyone. Each canoe may carry at most two people, and any pair must have a combined weight no greater than the limit. The challenge is minimizing canoe count efficiently for up to 50,000 contestants, which rules out naive pair-searching or backtracking approaches.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize utilization under a hard capacity constraint with tiny group size: bin-packing approximations in warehouse fulfillment, pairing jobs onto fixed-capacity workers, co-locating lightweight tasks on constrained edge devices, or packing requests into bounded network frames. At scale, brute-force matching collapses under quadratic behavior and unstable latency. The sort-plus-two-pointers approach gives predictable performance, simple correctness reasoning, and low implementation risk. It is especially useful when product requirements value throughput and determinism over globally optimal solutions for more general packing problems.

## 🔍 Problem Statement
You are given:

- `weights[i]`: the weight of the `i`-th contestant
- `limit`: the maximum total weight allowed in a single canoe

Rules:

- Each canoe can carry **at most 2 contestants**
- The sum of weights in one canoe must be `<= limit`
- A contestant may ride alone if no valid partner exists

Return the **minimum number of canoes** needed to carry all contestants.

Constraints:

- `1 <= weights.length <= 50000`
- `1 <= weights[i] <= limit <= 30000`

Examples:

```text
Input:  weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation: (50, 50), (70), (80)
```

```text
Input:  weights = [40, 60, 55, 45], limit = 100
Output: 2
Explanation: (40, 60), (45, 55)
```

The key constraint is input size: with 50,000 elements, checking all possible pairings is too expensive, so the algorithm must avoid quadratic matching.

## 🪜 How to Solve This
1. Read the problem → this is not arbitrary grouping; every canoe holds at most two people. That sharply constrains the search space.

2. We want the **fewest** canoes → equivalently, we want to maximize the number of valid pairs.

3. If the heaviest remaining contestant cannot pair with the lightest remaining contestant, they cannot pair with anyone. Everyone else is heavier than the lightest, so every other pairing would also fail.

4. That observation suggests sorting. Once weights are ordered, the only meaningful decision is whether the current heaviest can share with the current lightest.

5. Use two pointers:
   - left → lightest unassigned contestant
   - right → heaviest unassigned contestant

6. Try to pair them:
   - If `weights[left] + weights[right] <= limit`, pair them and move both pointers.
   - Otherwise, the heaviest must go alone, so move only `right`.

7. In either case, one canoe is consumed per iteration.

This is the kind of greedy choice that becomes obvious only after sorting exposes the monotonic structure.

## 🧩 Algorithm Walkthrough
1. **Sort the weights in nondecreasing order.**  
   This enables ordered reasoning about feasibility. After sorting, if the lightest person cannot fit with the heaviest, no one can fit with the heaviest. That monotonic property is what makes the greedy strategy valid.

2. **Initialize two pointers.**  
   Set `left = 0` and `right = weights.length - 1`. These represent the lightest and heaviest unassigned contestants. Also initialize `canoes = 0`.

3. **Process from the outside inward.**  
   While `left <= right`, allocate exactly one canoe for the contestant at `right`, because the heaviest remaining person must be placed now. This maintains the invariant that all contestants outside `[left, right]` have already been assigned optimally.

4. **Attempt the best possible pairing for the heaviest.**  
   Check whether `weights[left] + weights[right] <= limit`.  
   - If true, pair them and increment `left`. This is optimal because using the lightest feasible partner preserves more capacity among the remaining contestants.
   - Regardless, decrement `right`, since the heaviest contestant has now been assigned.

5. **Increment canoe count each iteration.**  
   Every loop places one canoe, either with one person or two. The interval shrinks monotonically, guaranteeing termination.

This is a classic **Two Pointers + Greedy after Sorting** pattern. Two pointers are the right abstraction because feasibility changes monotonically in sorted order, letting local decisions produce a globally optimal result.

## 📊 Worked Example
Example: `weights = [70, 50, 80, 50]`, `limit = 100`

Sorted weights: `[50, 50, 70, 80]`

| Step | left | right | Pair Checked | Decision | Canoes |
|------|------|-------|--------------|----------|--------|
| 1 | 0 (50) | 3 (80) | 50 + 80 = 130 | Too heavy, 80 goes alone | 1 |
| 2 | 0 (50) | 2 (70) | 50 + 70 = 120 | Too heavy, 70 goes alone | 2 |
| 3 | 0 (50) | 1 (50) | 50 + 50 = 100 | Pair together | 3 |

Final answer: `3`

Trace intuition:
- Start with the heaviest contestant because they are the hardest to place.
- If even the lightest person cannot join them, no pairing is possible.
- Once the two heaviest are forced alone, the remaining two lightest fit exactly into one canoe.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting, followed by an `O(n)` two-pointer scan. Sorting dominates. At `10^6` elements this is still practical in most runtimes; at `10^9`, comparison sorting becomes operationally unrealistic without distribution, approximation, or exploiting bounded weight ranges.

### Space Complexity
`O(1)` auxiliary space if sorting is in place, excluding language/runtime sort internals. In practice, some standard library sorts use stack or buffer space. Space can remain minimal because the algorithm only tracks two indices and a counter.

## 💡 Key Takeaways
- If the problem says “at most 2 per group” and asks for the minimum number of groups under a capacity limit, think sorted greedy with two pointers immediately.
- When feasibility depends on the sum of the smallest and largest remaining values, sorted order often exposes a monotonic decision boundary.
- Use `left <= right`, not `left < right`, or you will miss the final unpaired contestant when one person remains.
- Increment the canoe count on every iteration, not only when a pair is formed; each loop always assigns the heaviest remaining contestant.
- The transferable design insight is to exploit hard structural constraints first; reducing a general packing problem to a monotonic ordered scan is often what makes large-scale systems predictable.

## 🚀 Variations & Further Practice
- **Boats to Save People**: same core problem; useful for reinforcing why the heaviest-person-first greedy proof works.
- **Rescue boats with more than 2 people per boat**: becomes a harder bin-packing variant where the simple two-pointer proof no longer holds.
- **Minimize vehicles with heterogeneous capacities**: adds assignment and scheduling complexity, pushing the solution toward matching, heaps, or approximation algorithms.