# Pair Hikers for a Narrow Bridge

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given an array of hiker weights and a bridge weight limit, determine the minimum number of crossings needed when each crossing can carry at most two hikers whose combined weight does not exceed the limit. Every hiker must cross exactly once. The challenge is not feasibility but optimal pairing: a naive strategy can waste crossings, while the right approach exploits sorted order to pair the heaviest remaining hiker with the lightest feasible partner.

## 🌍 Engineering Impact
This pattern shows up in capacity-constrained batching across production systems: packing requests into rate-limited API calls, assigning jobs to fixed-resource workers, co-locating containers under memory caps, or grouping records into transport frames. At small scale, brute force pairing is tolerable; at fleet or stream scale, it becomes quadratic and operationally irrelevant. The sort-plus-two-pointer approach gives a predictable, low-complexity decision rule that preserves throughput while respecting hard constraints. Architecturally, it enables deterministic scheduling behavior, simpler reasoning about worst-case load, and efficient implementations in latency-sensitive paths.

## 🔍 Problem Statement
You are given:

- `weights[i]`: the weight of the `i`-th hiker
- `limit`: the maximum total weight allowed in a single crossing

Rules:

- Each crossing can contain either one or two hikers
- The total weight of two hikers sharing a crossing must be `<= limit`
- Every hiker must be assigned to exactly one crossing

Return the minimum number of crossings required.

Constraints:

- `1 <= weights.length <= 50000`
- `1 <= weights[i] <= limit <= 100000`

Examples:

```text
Input: weights = [70, 50, 80, 50], limit = 100
Output: 3
Explanation: [50, 50], [70], [80]
```

```text
Input: weights = [40, 60, 60, 40, 30], limit = 90
Output: 3
Explanation: [30, 60], [40, 40], [60]
```

The key constraint is minimizing crossings under a pairwise capacity limit. With up to 50,000 hikers, checking all pairings is too expensive, which pushes the solution toward sorting plus a linear scan.

## 🪜 How to Solve This
1. Read the problem → each crossing can take at most two hikers, so this is a pairing optimization problem under a hard capacity constraint.

2. Ask what makes a crossing “expensive” → every unpaired hiker consumes an entire crossing alone. So the real objective is to maximize valid pairs.

3. Notice that the heaviest remaining hiker is the hardest to place. If they cannot pair with the lightest remaining hiker, they cannot pair with anyone else either, because everyone else is heavier.

4. That observation suggests sorting. Once weights are ordered, the only meaningful question for the current heaviest hiker is: can the current lightest hiker fit with them?

5. Use two pointers:
   - left → lightest remaining hiker
   - right → heaviest remaining hiker

6. If `weights[left] + weights[right] <= limit`, pair them and move both pointers.
   Otherwise, send the heaviest alone and move only `right`.

7. Count one crossing per iteration. This greedy choice is safe because every step resolves the most constrained hiker optimally without blocking a better future pairing.

## 🧩 Algorithm Walkthrough
1. **Sort the weights in non-decreasing order.**  
   This creates structure in the search space. After sorting, feasibility becomes monotonic: if the lightest remaining hiker cannot pair with the heaviest, no other remaining hiker can.

2. **Initialize two pointers.**  
   Set `left = 0` and `right = n - 1`, plus `crossings = 0`.  
   Invariant: all hikers outside `[left, right]` have already been assigned to valid crossings.

3. **Process the heaviest remaining hiker first.**  
   The hiker at `right` is the most constrained because they have the fewest possible partners. Any optimal solution must decide their crossing now or equivalently later with the same feasibility boundary.

4. **Check whether the lightest and heaviest can share a crossing.**  
   If `weights[left] + weights[right] <= limit`, pair them. Increment `left` and decrement `right`.  
   Why correct: pairing the heaviest with the lightest feasible candidate preserves heavier candidates for other pairings and never reduces future options.

5. **Otherwise, send the heaviest alone.**  
   If the lightest cannot fit with the heaviest, nobody can. Decrement `right` only.  
   Invariant maintained: the removed hiker has been assigned in the only valid way.

6. **Increment the crossing count on every iteration.**  
   Each loop resolves exactly one crossing, either a pair or a solo.

7. **Stop when `left > right`.**  
   If `left == right`, one hiker remains and must cross alone, which the loop naturally counts.

This is a classic **Two Pointers** greedy pattern: sort to impose order, then use opposing pointers to make locally optimal decisions that are globally correct.

## 📊 Worked Example
Example: `weights = [40, 60, 60, 40, 30]`, `limit = 90`

Sorted weights: `[30, 40, 40, 60, 60]`

| Step | left | right | Pair Checked | Decision        | Crossings |
|------|------|-------|--------------|-----------------|-----------|
| 1    | 0    | 4     | 30 + 60 = 90 | Pair them       | 1         |
| 2    | 1    | 3     | 40 + 60 = 100| 60 goes alone   | 2         |
| 3    | 1    | 2     | 40 + 40 = 80 | Pair them       | 3         |

Trace summary:

1. The heaviest `60` can share with the lightest `30`, so that is always worth taking.
2. The remaining heaviest `60` cannot share with `40`, so it must cross alone.
3. The final two `40`s fit together.

All hikers are assigned in 3 crossings, which is minimal.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting, followed by an `O(n)` two-pointer scan. Sorting dominates. At `10^6` elements this is still practical in most in-memory systems; at `10^9`, full sorting becomes infrastructure-scale work and likely requires external or distributed strategies rather than an in-process solution.

### Space Complexity
`O(1)` auxiliary space if sorting is done in place, excluding language/runtime sort overhead. The two-pointer scan itself uses constant extra memory. If immutability is required and a copied array is used, space becomes `O(n)` in exchange for preserving the input.

## 💡 Key Takeaways
- If the problem says “at most two items per group” and asks for the minimum number of groups under a sum limit, think sorted greedy with two pointers immediately.
- When the heaviest remaining item is the hardest to place, sorting often turns a combinatorial pairing problem into a linear decision process.
- Be careful with the loop condition: `left <= right`, not just `<`, because one unpaired hiker may remain and still needs a crossing.
- Only advance `left` when a valid pair is formed; advancing both pointers on an invalid sum silently drops a hiker and undercounts crossings.
- The transferable design insight is to resolve the most constrained unit first using ordered state, which reduces global optimization problems to cheap local decisions.

## 🚀 Variations & Further Practice
- **Boats to Save People** — same core problem; the twist is recognizing that maximizing valid pairs is equivalent to minimizing boats.
- **Minimum number of platforms / meeting rooms** — still a two-pointer scheduling pattern, but the constraint shifts from pairwise capacity to overlapping intervals.
- **Bin packing with more than two items per container** — the conceptual jump is that greedy two-pointer logic is no longer sufficient; the problem becomes much harder and often only admits heuristics or approximation algorithms.