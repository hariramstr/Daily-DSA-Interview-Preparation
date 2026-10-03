# Maximize Completed Drone Deliveries Before Battery Clash

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given two unsorted integer arrays, `drones` and `batteries`, pair each drone with at most one battery whose charge is at least the drone’s required charge. Each battery is single-use, and pairings may be chosen in any order. The goal is to return the maximum number of valid drone-battery matches. The non-trivial part is scale: with up to `2 * 10^5` items per array, checking arbitrary pairings or backtracking is too expensive, so the solution must exploit ordering.

## 🌍 Engineering Impact
This pattern shows up anywhere scarce resources must be assigned to minimum-capability demands: container placement onto nodes with memory thresholds, ad auctions matching bids to reserve prices, job schedulers assigning workers to tasks, and streaming systems binding throughput budgets to consumers. At small scale, naive matching looks acceptable; at production scale, quadratic scans become latency spikes and capacity waste. Sorting plus greedy two-pointer matching gives a predictable `O(n log n + m log m)` path, preserves optimality for threshold-based assignment, and turns a combinatorial-looking problem into something operationally safe under large batch sizes.

## 🔍 Problem Statement
You are given two integer arrays:

- `drones[i]`: minimum charge required by the `i`-th drone
- `batteries[j]`: available charge in the `j`-th battery pack

A drone can launch only if assigned a battery with charge `>=` its requirement. Each drone can receive at most one battery, and each battery can be used at most once. You may reorder pairings arbitrarily. Return the maximum number of successful assignments.

Constraints:

- `1 <= drones.length, batteries.length <= 2 * 10^5`
- `1 <= drones[i], batteries[j] <= 10^9`

Examples:

- `drones = [4, 2, 7]`, `batteries = [3, 8, 5]` → `2`
- `drones = [1, 3, 3, 6]`, `batteries = [2, 3, 4]` → `3`

The key constraint is input size. With arrays this large, any `O(n * m)` strategy is already disqualified; the solution has to rely on sorting and a linear pass.

## 🪜 How to Solve This
1. Read the problem → this is a maximum matching problem, but only under a simple threshold rule: battery charge must be at least drone demand.

2. Notice what matters → exact original positions do not matter. Only the multiset of requirements and capacities matters, so sorting is immediately useful.

3. Ask what a good greedy choice looks like → if a small battery can satisfy a small drone, spending a larger battery there is usually wasteful because that larger battery might be needed later.

4. Sort both arrays ascending → now the “cheapest feasible assignment” becomes visible.

5. Walk both arrays with two pointers:
   - If the current battery can power the current drone, match them and advance both.
   - Otherwise, the battery is too weak for the smallest remaining drone, so it cannot help any later drone either; discard it and advance only the battery pointer.

6. Why this should work → every successful match uses the smallest battery that can satisfy the smallest remaining drone, preserving stronger batteries for harder drones. That local decision aligns with the global objective of maximizing total matches.

## 🧩 Algorithm Walkthrough
1. **Sort `drones` in non-decreasing order.**  
   This exposes the easiest remaining drone at each step. Any optimal strategy can be transformed into one that considers demands in sorted order without reducing the answer.

2. **Sort `batteries` in non-decreasing order.**  
   This lets us test batteries from weakest to strongest, which is exactly what a resource-preserving greedy strategy wants.

3. **Initialize two pointers:**  
   `i = 0` for `drones`, `j = 0` for `batteries`, and `matched = 0`.  
   Invariant: all elements before `i` and `j` have already been processed, and `matched` is the maximum feasible count for those processed prefixes.

4. **Compare `batteries[j]` with `drones[i]`.**  
   - If `batteries[j] >= drones[i]`, assign this battery to this drone.  
     Why correct: this is the smallest available battery that can satisfy the smallest unmet drone, so using it cannot block a better future assignment.  
     Advance both pointers and increment `matched`.
   - Otherwise, `batteries[j] < drones[i]`.  
     Why correct: this battery cannot satisfy the current smallest drone, so it cannot satisfy any later drone either, since later drones require at least as much charge.  
     Advance `j` only.

5. **Stop when either pointer reaches the end.**  
   At that point, no further valid assignments are possible.

This is the **Two Pointers + Greedy after Sorting** pattern. Two pointers give the linear merge-like scan; the greedy rule ensures each accepted match is minimally sufficient, which is the right abstraction for threshold-based one-to-one assignment.

## 📊 Worked Example
Example: `drones = [4, 2, 7]`, `batteries = [3, 8, 5]`

After sorting:

- `drones = [2, 4, 7]`
- `batteries = [3, 5, 8]`

| Step | `i` | `j` | Drone | Battery | Action | `matched` |
|---|---:|---:|---:|---:|---|---:|
| 1 | 0 | 0 | 2 | 3 | `3 >= 2`, match | 1 |
| 2 | 1 | 1 | 4 | 5 | `5 >= 4`, match | 2 |
| 3 | 2 | 2 | 7 | 8 | `8 >= 7`, match | 3 |

This sorted trace yields `3`, which is the true maximum under the stated rules. The key behavior is that each drone receives the smallest battery that can satisfy it, leaving stronger batteries for larger requirements. If a battery had been too small at any step, it would be discarded immediately because it could not help any remaining drone.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates: `O(n log n + m log m)`, where `n = drones.length` and `m = batteries.length`. The two-pointer scan is linear, `O(n + m)`. At `10^6` scale this is still practical in optimized runtimes; at `10^9`, even reading input is infeasible, so asymptotics stop being the only constraint.

### Space Complexity
`O(1)` auxiliary space if sorting in place is allowed and the language runtime’s sort is treated as implementation detail; otherwise `O(log n + log m)` to `O(n + m)` depending on sort strategy. The main space cost comes from sorting, not from the two-pointer scan itself.

## 💡 Key Takeaways
- If the problem asks for the maximum number of one-to-one assignments under a simple `>=` threshold, sorting both sides and scanning greedily is a strong first candidate.
- When original order is irrelevant and only feasibility matters, that is a signal to replace pairwise search with ordered matching.
- Be precise about pointer movement: on a successful match, advance both pointers; on failure, advance only the battery pointer.
- Do not discard a drone when the current battery is too small; the next stronger battery may still satisfy it.
- In production systems, threshold-constrained allocation often looks combinatorial until you exploit monotonic structure; once exposed, the implementation becomes both faster and easier to reason about.

## 🚀 Variations & Further Practice
- Add **weights or profits** per drone and maximize total value instead of count; the twist is that locally minimal feasible assignment is no longer obviously optimal.
- Allow each battery to power **multiple drones up to a total charge budget**; this shifts the problem toward bin packing or scheduling rather than simple matching.
- Require deciding the **minimum battery upgrade** needed to launch at least `k` drones; this combines greedy matching with binary search on the answer.