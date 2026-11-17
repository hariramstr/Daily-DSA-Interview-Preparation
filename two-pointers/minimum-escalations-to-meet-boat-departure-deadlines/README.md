# Minimum Escalations to Meet Boat Departure Deadlines

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given an array of crate weights and a boat weight limit, remove the fewest crates so every remaining crate can be assigned to a boat carrying at most two crates with total weight at most `limit`. Because a crate may ride alone, the only impossible crates are those whose individual weight already exceeds `limit`. The task is to return that minimum escalation count efficiently for up to `200,000` crates, using a sorting-driven two-pointer argument rather than brute-force assignment.

## 🌍 Engineering Impact
This pattern shows up in admission control, batch schedulers, warehouse slotting, and stream-processing compaction: decide whether work items are intrinsically admissible before spending effort on packing or pairing them. In production systems, confusing feasibility with optimization leads to wasted compute, overbuilt matching logic, and brittle heuristics. The right abstraction is often not “find the best grouping” but “separate impossible items from merely inconvenient ones.” At scale, that distinction enables simpler pipelines, predictable latency, and lower operational cost because infeasible units are identified with a monotonic scan instead of expensive combinatorial search.

## 🔍 Problem Statement
You are given:

- `weights[i]`: weight of the `i`-th crate
- `limit`: maximum total weight a boat can carry
- each boat may carry **at most two crates**
- a crate may also be loaded **alone**
- any crate may be **escalated** and removed before assignment, costing `1` escalation

Return the **minimum number of escalations** needed so that all non-escalated crates can be assigned to boats.

Key observation: since a single crate can occupy a boat by itself, pairing is never required for feasibility. A crate forces escalation only if `weights[i] > limit`.

Constraints:

- `1 <= weights.length <= 200000`
- `1 <= weights[i] <= 10^9`
- `1 <= limit <= 10^9`

Examples:

- `weights = [3, 5, 2, 8, 1], limit = 5` → `1`
- `weights = [9, 4, 7, 6, 2, 11], limit = 7` → `2`

The input size rules out quadratic simulation and pushes toward an `O(n log n)` or better scan.

## 🪜 How to Solve This
1. Read the rules carefully → notice the objective is **not** minimizing boats.
2. Ask what makes a crate impossible to ship → only one condition matters: can it fit **alone**?
3. Since a boat can carry one crate, any crate with `weight <= limit` is automatically feasible, regardless of what the other crates are.
4. That collapses the problem from “find a valid pairing arrangement” to “count individually infeasible items.”
5. Why mention sorting and two pointers at all? Because the sorted view makes the feasibility boundary explicit: all valid crates appear on one side, all invalid crates on the other.
6. Sort the array → use a right pointer from the heaviest end.
7. While the current heaviest crate exceeds `limit`, it must be escalated; decrement the pointer and increment the answer.
8. Stop as soon as the heaviest remaining crate fits. By sorted order, everything to its left also fits, so no further escalations are needed.

That is the whole reduction: feasibility is monotonic after sorting.

## 🧩 Algorithm Walkthrough
1. **Sort `weights` in nondecreasing order.**  
   This is the enabling step for the **Two Pointers** pattern. Sorting creates a monotonic structure: once a crate fits alone, every lighter crate also fits alone.

2. **Initialize a pointer at the rightmost index.**  
   The right side contains the heaviest crates, which are the only candidates that can force escalation. The invariant is: everything strictly to the right of the pointer has already been classified as requiring escalation.

3. **Check the heaviest remaining crate.**  
   If `weights[right] > limit`, that crate cannot be placed on any boat, even alone. Escalating it is not a heuristic; it is logically mandatory. Increment the escalation count and move `right` left.

4. **Repeat while the heaviest crate is still too heavy.**  
   This preserves the invariant that all removed crates are exactly the infeasible suffix of the sorted array.

5. **Stop at the first crate with `weights[right] <= limit`.**  
   At this point, every crate in positions `0..right` also satisfies `<= limit` by sorted order. Since any such crate can ride alone, all remaining crates are feasible without further removals.

6. **Return the number of removed crates.**  
   Correctness follows from necessity and sufficiency:
   - every crate above `limit` must be escalated
   - no crate at or below `limit` ever needs escalation for feasibility

This is a two-pointer-style boundary scan, even though only one end moves in practice. The abstraction still matters: sorted order turns global feasibility into a local pointer decision.

## 📊 Worked Example
Take `weights = [9, 4, 7, 6, 2, 11]`, `limit = 7`.

After sorting: `[2, 4, 6, 7, 9, 11]`

| Step | right | weights[right] | `> limit`? | Escalations | Decision |
|---|---:|---:|---:|---:|---|
| 1 | 5 | 11 | Yes | 1 | Must escalate 11 |
| 2 | 4 | 9  | Yes | 2 | Must escalate 9 |
| 3 | 3 | 7  | No  | 2 | Stop |

Why stopping is safe: once `7` fits, all crates to its left (`6, 4, 2`) also fit individually. Since crates may ride alone, no pairing constraint can make them infeasible. One valid assignment is `(2,4)`, `(6)`, `(7)`, but the exact packing is irrelevant to the escalation count. The answer is the size of the infeasible suffix: `2`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`, followed by a linear suffix scan that is `O(n)` in the worst case. Overall: `O(n log n)`. At `10^6` elements this is still practical in native runtimes; at `10^9`, sorting is no longer realistic and the problem becomes an external-memory or distributed-processing concern.

### Space Complexity
`O(1)` auxiliary space beyond the sort if the language/runtime offers in-place sorting of primitive arrays; otherwise `O(log n)` stack space is typical for comparison sort recursion. The scan itself uses constant space and cannot be meaningfully reduced further.

## 💡 Key Takeaways
- If items may be processed alone, first test whether the problem is really about pairing optimization or just individual feasibility.
- When sorting creates a clean valid/invalid boundary, a two-pointer or boundary-scan solution is usually stronger than simulation.
- Do not accidentally solve “minimum boats”; that is a different greedy problem with different correctness conditions.
- Off-by-one trap: after sorting, stop at the first `weights[right] <= limit`; everything left of it is automatically feasible.
- In production systems, separating infeasible work from optimally packable work often removes entire classes of unnecessary matching logic.

## 🚀 Variations & Further Practice
- **Minimum boats after removing at most `k` crates**: now you must jointly optimize removals and pairings, so feasibility and packing are no longer separable.
- **Boats with capacity `m > 2` crates**: the problem shifts toward bin packing; the simple sorted boundary remains useful, but optimal grouping becomes much harder.
- **Online arrivals with dynamic limit changes**: requires maintaining counts of overweight items under updates, pushing the solution toward balanced trees, Fenwick trees, or segment trees.