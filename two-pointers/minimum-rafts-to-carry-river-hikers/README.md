# Minimum Rafts to Carry River Hikers

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Greedy, Sorting

---

## 🗂 Problem Overview
Given an array of hiker weights and a raft weight limit, compute the minimum number of rafts needed to transport everyone. Each raft can carry at most two hikers, and every hiker must be assigned exactly once. The challenge is not feasibility — each hiker fits alone — but minimizing raft count by choosing pairings optimally. The non-trivial constraint is input size up to 100,000, which rules out brute-force pairing or backtracking.

## 🌍 Engineering Impact
This pattern shows up in bin-packing-like allocation problems across production systems: pairing jobs onto constrained workers, packing requests into fixed-capacity batches, assigning replicas to hosts with headroom limits, or co-locating workloads under memory ceilings. At scale, naive matching explodes combinatorially or leaves capacity underutilized, increasing infrastructure cost and latency. Sorting plus a greedy frontier scan is often the difference between an operationally cheap scheduler and one that burns excess nodes, queue slots, or transport capacity. The broader value is recognizing when a globally optimal result emerges from a locally safe decision once the input is ordered.

## 🔍 Problem Statement
You are given:

- `weights[i]`: the weight of the `i`-th hiker
- `limit`: the maximum total weight a single raft can carry

Rules:

- Each raft carries **at most two** hikers
- The sum of weights on a raft must be `<= limit`
- Every hiker must be assigned to exactly one raft
- Every individual hiker can fit alone

Return the **minimum number of rafts** required.

Constraints:

- `1 <= weights.length <= 100000`
- `1 <= weights[i] <= limit <= 100000`

Examples:

```text
Input:  weights = [3, 5, 3, 4], limit = 5
Output: 4
```

```text
Input:  weights = [1, 2, 2, 3], limit = 3
Output: 3
```

The key constraint is the input size: with up to `10^5` hikers, any approach that tries many pair combinations is too slow. That pushes the solution toward sorting and a linear scan.

## 🪜 How to Solve This
1. Read the problem → we are not just validating pairings; we are minimizing the number of groups under a capacity constraint.

2. Notice each raft holds **at most two** hikers → this is much simpler than general bin packing. For each heaviest remaining hiker, there is only one meaningful question: can they share with the lightest remaining hiker?

3. Why the lightest? → if the heaviest cannot pair with the lightest, they cannot pair with anyone. Everyone else is heavier, so that raft must be used by the heaviest alone.

4. That observation suggests sorting. Once weights are ordered, the best candidate partner for the heaviest remaining hiker is always the lightest remaining one.

5. Use two pointers:
   - left = lightest unassigned
   - right = heaviest unassigned

6. At each step:
   - If `weights[left] + weights[right] <= limit`, pair them and move both pointers.
   - Otherwise, send `weights[right]` alone and move only `right`.

7. Count one raft per iteration. This works because every decision permanently assigns the heaviest remaining hiker in the best possible way.

## 🧩 Algorithm Walkthrough
1. **Sort the weights in non-decreasing order.**  
   This enables ordered reasoning about who can pair with whom. Without sorting, you cannot make a safe greedy choice because “lightest” and “heaviest” are not directly accessible.

2. **Initialize two pointers.**  
   Set `left = 0` and `right = n - 1`, where `left` tracks the lightest unassigned hiker and `right` tracks the heaviest unassigned hiker. Initialize `rafts = 0`.

3. **Process the heaviest remaining hiker first.**  
   This is the core **Two Pointers + Greedy** pattern. The heaviest hiker is the hardest to place, so resolve them immediately.

4. **Check whether the lightest and heaviest can share a raft.**  
   If `weights[left] + weights[right] <= limit`, pair them. This is correct because pairing the heaviest with the lightest preserves maximal room for future pairings. Any heavier partner would only be harder to fit.

5. **If they cannot pair, send the heaviest alone.**  
   This is also correct: if the heaviest cannot fit with the lightest, they cannot fit with any remaining hiker. The sorted order gives the invariant that all candidates between `left` and `right - 1` are at least as heavy as `weights[left]`.

6. **Advance pointers and increment raft count.**  
   Every iteration assigns exactly one raft and removes the heaviest remaining hiker from consideration. The invariant is: all hikers outside `[left, right]` are already optimally assigned.

7. **Stop when `left > right`.**  
   At that point, every hiker has been assigned, and `rafts` is minimal.

## 📊 Worked Example
Example: `weights = [1, 2, 2, 3]`, `limit = 3`

After sorting: `[1, 2, 2, 3]`

| Step | left | right | Pair Checked | Decision | Rafts |
|------|------|-------|--------------|----------|-------|
| 1 | 0 (`1`) | 3 (`3`) | `1 + 3 = 4` | `3` goes alone | 1 |
| 2 | 0 (`1`) | 2 (`2`) | `1 + 2 = 3` | pair `(1,2)` | 2 |
| 3 | 1 (`2`) | 1 (`2`) | single hiker left | `2` goes alone | 3 |

Result: `3`

Why this is optimal:
- The hiker weighing `3` cannot pair with anyone.
- After removing `3`, the best possible remaining pairing is `(1,2)`.
- One `2` remains and must ride alone.

The trace shows the invariant clearly: each iteration finalizes the heaviest remaining hiker with no need to revisit earlier decisions.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting, followed by an `O(n)` two-pointer scan. Sorting dominates. At `10^6` elements this is still practical in most environments; at `10^9`, even reading and sorting the input becomes a system-level problem requiring external-memory or distributed strategies.

### Space Complexity
`O(1)` auxiliary space if sorting in place is allowed, excluding the sort implementation’s internal stack or runtime overhead. If the language’s sort allocates additional memory, practical space may be `O(log n)` or higher. Reducing space further is not meaningful without changing the sorting model.

## 💡 Key Takeaways
- If the problem asks for the minimum number of 2-item groups under a sum limit, sorting plus opposite-end pairing is a strong signal for a two-pointer greedy solution.
- When the hardest-to-place item is the heaviest remaining one, ask whether pairing it with the lightest feasible candidate yields a safe optimal choice.
- Be careful with the termination condition: when `left == right`, one unassigned hiker remains and still needs exactly one raft.
- Only move `left` when a valid pair is formed; moving both pointers on an invalid pair silently drops a hiker and undercounts rafts.
- The transferable design insight is that ordered inputs often convert expensive global matching problems into linear frontier decisions with provable optimality.

## 🚀 Variations & Further Practice
- **Boats to Save People**: same core problem; useful for reinforcing the exact greedy proof and implementation pattern.
- **Generalized boat capacity with up to `k` people per raft**: becomes much harder because the clean two-pointer invariant disappears and the problem moves toward bin packing.
- **Minimize total wasted capacity instead of raft count**: changes the objective function, so the local “pair heaviest with lightest” rule is no longer obviously optimal.