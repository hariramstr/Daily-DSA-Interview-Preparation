# Pair Kayaks for a Calm River

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given an array of tourist weights and a kayak weight limit, assign every tourist to a one- or two-person kayak while minimizing the total number of kayaks used. The output is a single integer: the minimum kayak count. The non-trivial part is finding globally optimal pairings efficiently at up to 50,000 tourists; brute-force pairing or local ad hoc choices can easily waste capacity and produce unnecessary kayaks.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must pack bounded-capacity units with minimal resource usage: batching requests under payload limits, assigning jobs to worker slots, grouping records into network frames, or packing shipments into containers. At scale, naive matching strategies inflate fleet size, queue depth, memory pressure, or cloud cost. The sort-plus-two-pointers approach works because the constraint is simple and ordered: once items are sorted, feasibility becomes monotonic. That enables deterministic, low-overhead allocation logic instead of combinatorial search, which is the difference between predictable throughput and operational instability in high-volume scheduling paths.

## 🔍 Problem Statement
You are given:

- `weights[i]`: the weight of the `i`th tourist
- `limit`: the maximum total weight a kayak can carry

Each kayak can carry either one tourist or two tourists, and every tourist must be assigned to exactly one kayak. A two-person kayak is valid only if the combined weight does not exceed `limit`. Return the minimum number of kayaks required.

Constraints:

- `1 <= weights.length <= 50000`
- `1 <= weights[i] <= limit <= 100000`
- Every tourist can always ride alone if needed

Examples:

- `weights = [70, 50, 80, 50]`, `limit = 100` → `3`
- `weights = [40, 60, 55, 45, 30]`, `limit = 90` → `3`

The key constraint is scale: with 50,000 elements, quadratic pair testing is unnecessary and too expensive. The structure of the capacity rule strongly suggests sorting, then making greedy decisions from both ends.

## 🪜 How to Solve This
1. Read the problem → we are minimizing the number of groups, where each group can contain at most two people under a sum constraint.

2. Notice what actually matters → for any heavy tourist, there is only one useful question: can they share with the lightest remaining tourist? If not, they cannot share with anyone else, because everyone else is heavier.

3. That observation suggests sorting → once weights are ordered, feasibility becomes monotonic instead of arbitrary.

4. Use two pointers:
   - left = lightest remaining tourist
   - right = heaviest remaining tourist

5. Try the best possible rescue for the heaviest tourist:
   - If `weights[left] + weights[right] <= limit`, pair them and move both pointers.
   - Otherwise, the heaviest tourist must go alone, so move only `right`.

6. In both cases, one kayak is consumed per iteration.

7. Why this is enough → every step makes the locally optimal choice that cannot hurt future options. Pairing the heaviest with the lightest preserves as much remaining capacity flexibility as possible.

This is the standard “sort, then greedily consume extremes” shape.

## 🧩 Algorithm Walkthrough
1. **Sort the weights in non-decreasing order.**  
   This is the enabling step. After sorting, if the lightest remaining tourist cannot pair with the heaviest, then no one can pair with the heaviest. That monotonic property is what makes the greedy strategy correct.

2. **Initialize two pointers:**
   - `left = 0`
   - `right = weights.length - 1`  
   Also initialize `kayaks = 0`.

3. **Process while `left <= right`.**  
   Each iteration assigns the heaviest remaining tourist at `right` to exactly one kayak. This is the core invariant: after every step, all tourists outside `[left, right]` are fully assigned, and `kayaks` is the minimum needed for those assigned tourists.

4. **Check whether the lightest and heaviest can share.**  
   If `weights[left] + weights[right] <= limit`, place them together. Increment `left` and decrement `right`.  
   Why correct: pairing the heaviest with the lightest is the least restrictive successful pairing. Any heavier partner would only reduce future pairing options.

5. **Otherwise, send the heaviest alone.**  
   Decrement `right` only.  
   Why correct: if the lightest cannot fit with the heaviest, no other remaining tourist can, since all others are at least as heavy as `weights[left]`.

6. **Increment `kayaks` in both cases.**  
   Exactly one kayak is used per iteration, whether it carries one or two tourists.

This is a textbook **Two Pointers + Greedy after Sorting** solution: ordered data turns a pairing problem into a linear scan over extremes.

## 📊 Worked Example
Example: `weights = [40, 60, 55, 45, 30]`, `limit = 90`

Sorted: `[30, 40, 45, 55, 60]`

| Step | left | right | Pair Checked | Decision | Kayaks |
|---|---:|---:|---|---|---:|
| 1 | 0 (`30`) | 4 (`60`) | `30 + 60 = 90` | Pair them | 1 |
| 2 | 1 (`40`) | 3 (`55`) | `40 + 55 = 95` | `55` goes alone | 2 |
| 3 | 1 (`40`) | 2 (`45`) | `40 + 45 = 85` | Pair them | 3 |

Pointers cross, so all tourists are assigned.

Final grouping:
- `(30, 60)`
- `(55)`
- `(40, 45)`

Result: `3`

The important behavior is step 2: once `40` cannot pair with `55`, nothing heavier could either, so sending `55` alone is forced, not merely greedy.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting; the two-pointer scan afterward is `O(n)`. For `10^6` elements, this is still practical in optimized runtimes because sort dominates but remains manageable. At `10^9`, even reading and sorting the data becomes a systems problem requiring external-memory or distributed strategies.

### Space Complexity
`O(1)` auxiliary space if sorting is in place, excluding the sort implementation’s internal stack or runtime overhead. In languages with non-in-place sort behavior, effective space may be `O(n)`. Reducing that depends on the sorting primitive, not the two-pointer logic.

## 💡 Key Takeaways
- If the problem asks for minimizing containers/groups with a simple capacity rule and at most two items per group, sorting plus two pointers should be an immediate candidate.
- A strong signal is monotonic feasibility after sorting: if the lightest item cannot pair with the heaviest, nothing can.
- Use `left <= right`, not `left < right`, or you will miss the final unpaired tourist.
- Increment the kayak count on every iteration, even when a pair is formed; the loop assigns one kayak per pass.
- In production allocation paths, ordered greedy strategies are valuable when they convert expensive global matching into predictable linear scans over sorted state.

## 🚀 Variations & Further Practice
- **Boats to Save People** — same core problem; useful for recognizing this exact greedy proof pattern under different domain language.
- **Minimum number of bins with arbitrary bin capacity and more than two items per bin** — becomes true bin packing, where this greedy shortcut no longer guarantees optimality.
- **Pairing under multiple constraints (e.g., weight + skill compatibility)** — sorting on one dimension is no longer sufficient; the harder part is preserving feasibility across competing constraints.