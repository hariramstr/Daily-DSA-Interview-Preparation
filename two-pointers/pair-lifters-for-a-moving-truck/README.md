# Pair Lifters for a Moving Truck

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given an unsorted array `weights` and a per-trip weight `limit`, compute the minimum number of trips needed to move every item exactly once when each trip can carry at most two items. The challenge is not counting items but choosing pairings optimally. A naive search over all possible pairs is too expensive at `10^5` items, so the solution must exploit structure: sort first, then use a two-pointer greedy strategy.

## 🌍 Engineering Impact
This pattern shows up anywhere capacity-constrained batching matters: truck or warehouse load planning, request coalescing under payload limits, stream micro-batching, memory-page packing, and job placement in schedulers. At small scale, brute-force pairing looks acceptable; at production scale it collapses into quadratic behavior, unpredictable latency, and poor utilization. Sorting plus two-pointer greediness gives a deterministic, cache-friendly, easy-to-reason-about plan with strong throughput characteristics. More importantly, it turns a combinatorial pairing problem into an ordered decision process, which is exactly the kind of simplification that keeps operational systems explainable and performant.

## 🔍 Problem Statement
You are given:

- `weights`, where `weights[i]` is the weight of the `i`th furniture item
- `limit`, the maximum total weight allowed on one trip

Rules:

- Each trip carries either one item or two items
- The total weight on a trip must be `<= limit`
- Every item must be moved exactly once

Return the minimum number of trips required.

Constraints:

- `1 <= weights.length <= 100000`
- `1 <= weights[i] <= limit <= 1000000000`

Examples:

- `weights = [4, 2, 3, 5]`, `limit = 6` → `3`  
  Optimal trips: `(4,2)`, `(3)`, `(5)`

- `weights = [1, 2, 2, 3, 4]`, `limit = 4` → `3`  
  Optimal trips: `(1,3)`, `(2,2)`, `(4)`

The key constraint is input size: with up to `10^5` items, any nested-loop or backtracking pairing strategy is too slow. The algorithm must avoid exploring combinations explicitly.

## 🪜 How to Solve This
1. Read the problem → each trip can hold at most two items, so every decision is really about whether the heaviest remaining item can share a trip.

2. Unsorted input hides the useful structure → sort the weights so “lightest” and “heaviest” are easy to access.

3. Once sorted, focus on the heaviest item first → if it cannot pair with the lightest remaining item, it cannot pair with anything else either, because every other candidate is heavier.

4. That observation suggests a greedy rule:
   - Try to pair `lightest + heaviest`
   - If they fit, use one trip and move both pointers
   - If they do not fit, send the heaviest alone and move only the right pointer

5. Repeat until all items are assigned.  
   This works because every step resolves the hardest item to place without sacrificing a better future pairing. Sorting converts a global optimization problem into a sequence of locally safe decisions.

## 🧩 Algorithm Walkthrough
1. **Sort the array in nondecreasing order.**  
   This enables ordered reasoning about feasible pairs. After sorting, if `weights[left] + weights[right]` exceeds `limit`, then `weights[right]` cannot pair with any item between `left` and `right - 1`, because all of them are at least as heavy as `weights[left]`.

2. **Initialize two pointers.**  
   Set `left = 0` for the lightest remaining item and `right = n - 1` for the heaviest remaining item. Also keep a `trips` counter.

3. **Process the heaviest remaining item each iteration.**  
   The heaviest item is the most constrained; delaying its placement does not create new pairing options. This is the core greedy insight.

4. **Check whether the lightest and heaviest can share a trip.**  
   If `weights[left] + weights[right] <= limit`, pair them, increment `left`, decrement `right`, and increment `trips`.  
   Invariant: all items outside `[left, right]` have already been assigned optimally.

5. **Otherwise, send the heaviest alone.**  
   Decrement `right` and increment `trips`.  
   Why correct: if the lightest item cannot fit with the heaviest, no other remaining item can.

6. **Stop when `left > right`.**  
   If `left == right`, one item remains and needs one final trip.

This is the **Two Pointers + Greedy** pattern: sorting creates monotonic structure, and pointer movement encodes the optimal local decision without revisiting prior choices.

## 📊 Worked Example
Example: `weights = [1, 2, 2, 3, 4]`, `limit = 4`

Sorted: `[1, 2, 2, 3, 4]`

| Step | left | right | Pair Checked | Decision | trips |
|---|---:|---:|---|---|---:|
| 1 | 0 (`1`) | 4 (`4`) | `1 + 4 = 5` | `4` goes alone | 1 |
| 2 | 0 (`1`) | 3 (`3`) | `1 + 3 = 4` | pair `(1,3)` | 2 |
| 3 | 1 (`2`) | 2 (`2`) | `2 + 2 = 4` | pair `(2,2)` | 3 |

Now `left = 2`, `right = 1`, so processing stops.

Result: `3` trips.

The important trace property is that every iteration resolves the heaviest remaining item. When `4` cannot pair with `1`, it cannot pair with anything at all, so sending it alone is forced, not merely heuristic.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting; the two-pointer scan afterward is `O(n)`. Sorting dominates. At `10^6` elements, this is still operationally reasonable in most environments; at `10^9`, even linear passes become infrastructure problems, so the bottleneck shifts from algorithm choice to memory, I/O, and distributed execution strategy.

### Space Complexity
`O(1)` auxiliary space if sorting in place is allowed, excluding the sort implementation’s internal overhead. In practice, library sorts may use `O(log n)` stack or more. You can reduce extra allocation by mutating the input array, trading away immutability and sometimes API cleanliness.

## 💡 Key Takeaways
- If the problem says “at most two per group” plus “minimize number of groups under a limit,” that is a strong signal for sorting and opposing pointers.
- When feasibility depends only on the smallest and largest remaining values, look for a monotonic pairing rule rather than combinatorial search.
- The loop condition and pointer updates are easy to get wrong: when a pair fits, move both pointers; when it does not, move only the heavy pointer.
- Handle the single remaining item correctly: `left == right` still requires one trip, so the loop must account for that case.
- The transferable design insight is that ordering often converts expensive global matching into a sequence of locally forced decisions with predictable performance.

## 🚀 Variations & Further Practice
- **Boats to Save People**: same core problem, but framed as rescue boats; useful for recognizing the exact greedy invariant in a different domain.
- **Generalize from 2 items per trip to `k` items per trip**: the problem becomes much harder because the clean two-pointer invariant disappears and bin-packing complexity emerges.
- **Minimize wasted capacity instead of trip count**: changes the objective function, so the current greedy rule is no longer obviously optimal and may require dynamic programming or approximation strategies.