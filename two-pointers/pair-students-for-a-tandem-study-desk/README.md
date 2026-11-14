# Pair Students for a Tandem Study Desk

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
Given an array of backpack sizes and a maximum allowed combined size per tandem desk, assign every student to a desk while minimizing the total number of desks used. Each desk can hold one or two students, and any paired students must have a sum less than or equal to `deskLimit`. The non-trivial part is finding the globally optimal pairing arrangement efficiently under `n <= 50,000`, where brute-force matching or repeated local choices becomes too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize packing efficiency under a hard capacity constraint: bin-packing approximations in warehouse fulfillment, pairing workloads onto compute slots, batching requests under payload limits, or assigning jobs to constrained hardware lanes. At scale, naive matching strategies waste capacity, inflate infrastructure cost, and create uneven utilization. Sorting plus a two-pointer greedy pass gives a predictable, low-overhead decision procedure that is easy to reason about, easy to test, and cheap enough to run in hot paths or large scheduling batches without introducing combinatorial complexity.

## 🔍 Problem Statement
You are given:

- `backpackSizes[i]`: size of the `i`-th student's backpack
- `deskLimit`: maximum total backpack size allowed at a tandem desk

Return the minimum number of desks needed so that every student is assigned to exactly one desk. A desk may seat either one student or two students. If two students share a desk, their combined backpack sizes must be `<= deskLimit`. Students may be rearranged arbitrarily before assignment.

Constraints:

- `1 <= backpackSizes.length <= 50000`
- `1 <= backpackSizes[i] <= deskLimit <= 100000`

Examples:

- `backpackSizes = [2, 3, 4, 5], deskLimit = 6` → `3`
- `backpackSizes = [1, 2, 2, 3], deskLimit = 3` → `3`

The key constraint is the input size: `50,000` elements rules out quadratic pairing logic and pushes toward an `O(n log n)` or better approach.

## 🪜 How to Solve This
1. Read the objective → we are not checking whether a pairing exists; we are minimizing the number of desks. That means we want to maximize valid pairs.

2. Notice rearrangement is allowed → original order is irrelevant, so sorting is immediately a candidate.

3. After sorting, ask: who is hardest to place? → the student with the largest backpack. If they cannot pair with the smallest remaining backpack, they cannot pair with anyone, so they must sit alone.

4. That observation suggests a greedy strategy:
   - try to pair the heaviest remaining student with the lightest remaining student
   - if they fit, use one desk and move both pointers
   - if they do not fit, the heaviest student must take a desk alone

5. Why this works → every decision removes the largest remaining backpack safely. We never lose a better future option by pairing it with the smallest feasible partner, because any larger partner would only make the constraint harder to satisfy.

6. The implementation naturally becomes: sort once, then sweep inward with two pointers.

## 🧩 Algorithm Walkthrough
1. **Sort the array in non-decreasing order.**  
   This creates structure: the smallest remaining backpack is always at the left pointer, and the largest remaining backpack is always at the right pointer. Without sorting, there is no reliable local decision rule.

2. **Initialize two pointers:**  
   `left = 0` for the lightest unassigned student, `right = n - 1` for the heaviest unassigned student. Also initialize `desks = 0`.

3. **Process while `left <= right`.**  
   Each iteration assigns exactly one desk, so `desks` increments once per loop. The invariant is: all students outside `[left, right]` have already been assigned optimally relative to the greedy choices made so far.

4. **Check whether `backpackSizes[left] + backpackSizes[right] <= deskLimit`.**  
   - If true, pair them and move both pointers inward.  
   - If false, assign the heaviest student alone and decrement `right`.

5. **Why the greedy choice is correct.**  
   This is a classic **Two Pointers + Greedy** pattern. If the heaviest student cannot pair with the lightest, they cannot pair with anyone else, because every other remaining student is heavier than the lightest. So seating them alone is forced, not just convenient.

6. **If they can pair, pairing with the lightest is optimal.**  
   Using a heavier partner for the heaviest student would only consume more capacity and reduce future pairing options. The lightest feasible partner preserves maximal flexibility for the remaining students.

7. **Terminate when pointers cross.**  
   At that point, every student has been assigned, and `desks` is the minimum possible.

## 📊 Worked Example
Example: `backpackSizes = [2, 3, 4, 5]`, `deskLimit = 6`

Sorted array: `[2, 3, 4, 5]`

| Step | left | right | Students considered | Sum | Action | Desks |
|---|---:|---:|---|---:|---|---:|
| 1 | 0 | 3 | 2 and 5 | 7 | Too large, 5 sits alone | 1 |
| 2 | 0 | 2 | 2 and 4 | 6 | Pair them | 2 |
| 3 | 1 | 1 | 3 alone | — | Single student gets desk | 3 |

Result: `3`

The important part is step 1. Once `2 + 5 > 6`, student `5` cannot pair with anyone, because every remaining student besides `2` is at least as large as `3`. That makes the “seat the largest alone” decision provably safe, not heuristic.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting, followed by an `O(n)` two-pointer scan. Sorting dominates. At `10^6` elements this is still practical in most environments; at `10^9`, full in-memory sorting becomes the real bottleneck and likely requires external-memory or distributed processing.

### Space Complexity
`O(1)` auxiliary space if sorting is done in place, excluding the sort implementation’s internal stack or runtime overhead. If the language’s sort allocates additional memory, effective space may be `O(log n)` or higher. Reducing space further usually means accepting implementation-specific trade-offs.

## 💡 Key Takeaways
- If the problem says “rearrange freely” and asks to minimize containers/pairs under a capacity limit, sorting is usually the first useful move.
- When the decision depends on combining the smallest and largest remaining values, that is a strong signal for a two-pointer greedy pass.
- Use `left <= right`, not `left < right`, or you will miss the final unpaired student when one remains.
- Increment the desk count once per iteration, regardless of whether you formed a pair or seated one student alone.
- The transferable design insight is that imposing order on an otherwise combinatorial search space often turns matching problems into cheap, deterministic linear scans after one upfront indexing step.

## 🚀 Variations & Further Practice
- **Boats to Save People** — same core pattern, but framed as rescue capacity; useful for recognizing the abstraction independent of domain wording.
- **Minimum number of gondolas / elevator rides variants** — similar pairing under weight limits, but some versions add multiple riders or dynamic constraints that break the simple two-pointer proof.
- **General bin packing** — desks can hold more than two students; this becomes substantially harder because the clean “largest with smallest” invariant no longer guarantees optimality.