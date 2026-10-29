# Match Guests to Spare Umbrellas

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sorting, Greedy

---

## 🗂 Problem Overview
You are given two arrays: `guests`, where each value is the minimum umbrella size a guest will accept, and `umbrellas`, where each value is the size of an available spare umbrella. Each guest can receive at most one umbrella, and each umbrella can be used once. The goal is to return the maximum number of valid guest–umbrella assignments. The problem is non-trivial because local assignment choices can block future matches unless you process both sets in a globally ordered way.

## 🌍 Engineering Impact
This pattern shows up anywhere two constrained resource pools must be matched for maximum throughput: assigning compute slots to jobs with minimum memory requirements, placing requests onto servers with capacity floors, fitting ad impressions to eligibility thresholds, or matching riders to vehicles with minimum seat counts. At scale, naive pairwise comparison becomes quadratic and operationally useless. Sorting plus linear scan turns an allocation problem into a predictable, cache-friendly pass with clear failure behavior. That matters in schedulers, marketplace engines, and stream processors where matching quality and latency both directly affect system efficiency.

## 🔍 Problem Statement
Given two integer arrays, `guests` and `umbrellas`, return the maximum number of guests who can be assigned a suitable umbrella.

A guest `i` can only use an umbrella whose size is at least `guests[i]`. Each guest can receive at most one umbrella, and each umbrella can be assigned at most once.

Constraints:
- `1 <= guests.length, umbrellas.length <= 100000`
- `1 <= guests[i], umbrellas[i] <= 1000000000`

Examples:

- `guests = [2, 3, 1]`, `umbrellas = [1, 1, 3]` → `2`
- `guests = [4, 2, 5]`, `umbrellas = [2, 3, 4, 6]` → `3`

Important edge cases:
- More guests than umbrellas
- More umbrellas than guests
- Many duplicate sizes
- No umbrella can satisfy any guest
- Every guest can be satisfied

The key constraint is input size up to `10^5`, which rules out nested matching strategies and pushes toward `O(n log n + m log m)` or better.

## 🪜 How to Solve This
1. Read the problem → this is not about finding the “best” umbrella for one guest in isolation; it is about maximizing the total number of valid assignments.

2. Notice the compatibility rule is one-dimensional: umbrella size must be `>=` guest requirement. That usually suggests sorting, because once both sides are ordered, feasibility becomes monotonic.

3. Ask what greedy choice is safe → if the smallest available umbrella can satisfy the smallest unmet guest, using it is optimal. Giving that guest a larger umbrella only reduces future options.

4. If the current umbrella is too small, it is useless for that guest and for every later guest as well, because later guests require the same or larger size. So skipping that umbrella is also safe.

5. That reasoning gives the full strategy: sort both arrays, keep one pointer on guests and one on umbrellas, and advance through them once.

6. Every pointer move either consumes a valid match or discards an umbrella that can never help. No backtracking is needed, which is the signal that a greedy two-pointer scan is the right tool.

## 🧩 Algorithm Walkthrough
1. **Sort both arrays in non-decreasing order.**  
   This converts arbitrary input order into a structure where the smallest remaining guest and smallest remaining umbrella are always visible. The relevant pattern is **Two Pointers with Greedy Matching**: process both sorted sequences from left to right.

2. **Initialize two pointers:** `g = 0` for `guests`, `u = 0` for `umbrellas`, plus `matched = 0`.  
   Invariant: all indices before `g` are already matched; all indices before `u` are already consumed or discarded.

3. **Compare `umbrellas[u]` with `guests[g]`.**  
   - If `umbrellas[u] >= guests[g]`, assign that umbrella to that guest. Increment `matched`, `g`, and `u`.  
   Why correct: this uses the smallest umbrella that can satisfy the smallest unmet guest, preserving larger umbrellas for stricter guests.
   - Otherwise, increment `u` only.  
   Why correct: an umbrella too small for the current smallest guest cannot satisfy any later guest either.

4. **Continue until one pointer reaches the end.**  
   If guests are exhausted, all possible useful matches are already made. If umbrellas are exhausted, no further assignments are possible.

5. **Return `matched`.**  
   The invariant throughout is that the algorithm never wastes a feasible smaller umbrella on a guest who could have waited for a larger one. That is exactly why the greedy choice is globally optimal here.

## 📊 Worked Example
Example: `guests = [4, 2, 5]`, `umbrellas = [2, 3, 4, 6]`

After sorting:
- `guests = [2, 4, 5]`
- `umbrellas = [2, 3, 4, 6]`

| Step | `g` | `u` | Guest need | Umbrella size | Action | Matched |
|---|---:|---:|---:|---:|---|---:|
| 1 | 0 | 0 | 2 | 2 | Match | 1 |
| 2 | 1 | 1 | 4 | 3 | Too small, skip umbrella | 1 |
| 3 | 1 | 2 | 4 | 4 | Match | 2 |
| 4 | 2 | 3 | 5 | 6 | Match | 3 |

Pointers stop because all guests are matched.

Result: `3`

The key observation is step 2: once umbrella `3` fails guest `4`, it is guaranteed to fail every remaining guest, so skipping it is lossless.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates: `O(n log n + m log m)`, where `n = guests.length` and `m = umbrellas.length`. The two-pointer scan is linear, `O(n + m)`. At `10^6` elements this is still practical with optimized sorting; at `10^9`, full in-memory sorting becomes the real bottleneck and would require external or distributed strategies.

### Space Complexity
`O(1)` auxiliary space if sorting in place is allowed, excluding sort implementation overhead. In practice, language runtimes may allocate temporary memory during sort. Space can be reduced only marginally; the main trade-off is between in-place mutation and preserving original input order.

## 💡 Key Takeaways
- If two collections have a one-sided compatibility rule like `a >= b` and each item can be used once, think sorted greedy matching with two pointers.
- If rejecting a candidate for the current smallest requirement implies it will fail all future requirements, you likely have a monotonic structure worth exploiting.
- Sort both arrays in ascending order; reversing one side breaks the greedy invariant and can reduce the match count.
- Be careful to advance only the umbrella pointer on a failed comparison; advancing both pointers incorrectly drops an unmatched guest.
- The transferable design insight is that ordered scans often turn expensive bipartite-style matching into a deterministic linear pass when compatibility is monotonic.

## 🚀 Variations & Further Practice
- **Assign Cookies / resource-to-demand matching:** same core pattern, but useful for recognizing the greedy proof shape in different domain language.
- **Minimum number of boats / bins with capacity:** still greedy after sorting, but now each resource may serve multiple items under a sum constraint, which changes pointer movement.
- **Weighted or cost-aware matching:** maximize value or minimize waste instead of just count; the monotonic greedy scan may no longer be sufficient, forcing DP, heaps, or flow-based formulations.