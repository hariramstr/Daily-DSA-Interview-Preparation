# Maximum Artifact Pairings Within Durability Limit

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Greedy, Sorting

---

## 🗂 Problem Overview
Given an unsorted array `durability` and an integer `limit`, form as many disjoint 2-artifact pairs as possible such that each pair satisfies `|a - b| <= limit`. Artifacts not used in pairs may remain alone, but do not affect the result. The output is the maximum number of valid pairs. The challenge is combinatorial: naive matching explores too many pairings, and the input size requires an `O(n log n)` or better strategy.

## 🌍 Engineering Impact
This pattern shows up anywhere large sets of entities must be matched under a bounded compatibility rule: batching similar jobs in schedulers, co-locating workloads with bounded resource skew, pairing nearby timestamps in stream reconciliation, or grouping adjacent scores in ranking and dedup pipelines. At scale, brute-force matching collapses under quadratic comparisons and unstable heuristics produce globally bad utilization. Sorting plus a linear greedy pass gives deterministic behavior, predictable latency, and a structure that composes well with memory-bound systems, external sort pipelines, and distributed preprocessing stages.

## 🔍 Problem Statement
You are given an integer array `durability` of length `n` and an integer `limit`.

- A crate may contain either one artifact or exactly two artifacts.
- A 2-artifact crate using values `a` and `b` is valid only if `|a - b| <= limit`.
- Each artifact index may be used at most once.
- Leftover artifacts may go alone, but only valid 2-artifact crates count toward the answer.

Return the maximum number of valid pairs.

**Constraints**
- `1 <= durability.length <= 2 * 10^5`
- `0 <= durability[i] <= 10^9`
- `0 <= limit <= 10^9`

**Example 1**
- Input: `durability = [8, 1, 5, 6, 2, 9]`, `limit = 1`
- Output: `2`

**Example 2**
- Input: `durability = [4, 4, 4, 7, 8, 9, 9]`, `limit = 0`
- Output: `2`

The key constraint is `n = 2 * 10^5`: any solution that tries many alternative pairings or uses nested matching will time out.

## 🪜 How to Solve This
1. Start from the constraint, not the examples → `n` is large enough that pairwise search is dead on arrival. We need one expensive global operation at most, then a linear scan.

2. Notice the validity rule depends only on value difference, not original position → that strongly suggests sorting. Once sorted, “who can pair with whom” becomes local instead of global.

3. After sorting, ask what a good pair looks like → if two adjacent values cannot pair, then the smaller one cannot pair with anything later either, because all later values are even larger. That is the key monotonic property.

4. This turns the problem into a left-to-right greedy decision:
   - if `durability[i+1] - durability[i] <= limit`, pair them immediately;
   - otherwise skip `i`, because it is impossible to rescue it later.

5. Why greedy is safe → using the smallest feasible neighbor preserves larger values for later matches and never reduces future options. Any non-adjacent pairing would only make the gap larger or consume an element that could serve another valid pair.

6. That gives a clean plan: sort once, scan with two pointers or one index jumping by 1 or 2, count pairs.

## 🧩 Algorithm Walkthrough
1. **Sort the array in nondecreasing order.**  
   This is the enabling transformation. In sorted order, for any index `i`, all candidates to the right are `>= durability[i+1]`. That means if `durability[i]` cannot pair with its immediate neighbor, it cannot pair with any later element either.

2. **Initialize a scan pointer `i = 0` and `pairs = 0`.**  
   We will process artifacts from smallest to largest without revisiting them. The invariant is: all indices before `i` have been permanently assigned either to a valid pair or to a singleton.

3. **Check adjacent artifacts `durability[i]` and `durability[i+1]`.**  
   This is the core **Two Pointers + Greedy** step. In sorted order, the smallest available artifact should only be tested against the smallest remaining candidate. If that adjacent difference is within `limit`, pairing them is optimal because it consumes the minimum possible values and leaves maximal flexibility for the suffix.

4. **If the pair is valid, count it and advance by 2.**  
   We form one crate, increment `pairs`, and move `i += 2`. The invariant remains: no earlier decision can be improved by reconsidering these two elements.

5. **If the pair is invalid, skip the smaller artifact and advance by 1.**  
   Since `durability[i+1] - durability[i] > limit`, every later artifact differs from `durability[i]` by at least as much, so `durability[i]` is unpairable. Advancing by 1 is therefore forced, not heuristic.

6. **Stop when fewer than two artifacts remain.**  
   At that point no additional pair can be formed. The accumulated count is maximal by the monotonic argument above.

This abstraction is right because sorting creates an ordered search space where local adjacency decisions are globally safe.

## 📊 Worked Example
Take `durability = [8, 1, 5, 6, 2, 9]`, `limit = 1`.

After sorting: `[1, 2, 5, 6, 8, 9]`

| Step | `i` | Values checked | Diff | Action | `pairs` |
|---|---:|---|---:|---|---:|
| 1 | 0 | `(1, 2)` | 1 | valid pair, use both | 1 |
| 2 | 2 | `(5, 6)` | 1 | valid pair, use both | 2 |
| 3 | 4 | `(8, 9)` | 1 | valid pair, use both | 3 |

This trace shows the sorted greedy scan. Under the stated rule `|a-b| <= limit`, all three adjacent pairs are valid, so the maximum is `3`. More generally, the important behavior is the decision rule: adjacent valid pairs are taken immediately; adjacent invalid pairs force the smaller element to remain single because no later match can improve its gap.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` due to sorting; the post-sort scan is `O(n)`. For `n = 2 * 10^5`, this is comfortably practical in memory-resident code. At `10^6`, sort cost dominates but remains standard. At `10^9`, the issue is no longer the algorithmic pattern but storage, I/O, and likely the need for external sorting.

### Space Complexity
`O(1)` auxiliary space beyond the sort implementation if sorting in place is allowed; otherwise the effective space is whatever the language runtime’s sort requires. The scan itself is constant-space. You can reduce memory pressure further only by choosing an in-place sort, usually at the cost of implementation complexity or worse constants.

## 💡 Key Takeaways
- If the constraint depends only on value distance and original order is irrelevant, sorting is usually the first move.
- When a sorted sequence gives a monotonic impossibility rule (“if it fails with the nearest candidate, it fails with all later ones”), greedy pairing is often optimal.
- The correctness hinge is strict: after sorting, compare adjacent unused elements; do not search farther right once the nearest candidate already violates the limit.
- Watch the index update carefully: valid pair → advance by 2, invalid pair → advance by 1. Mixing these cases silently undercounts or reuses elements.
- In production systems, this is the broader lesson that a global reorder can turn an intractable matching problem into a deterministic linear pass with predictable performance.

## 🚀 Variations & Further Practice
- **Return the actual pairs, not just the count.** Same core approach, but now you must preserve pair construction and think about output size, stable ordering, and memory layout.
- **Each crate may hold up to `k` artifacts with `max - min <= limit`.** The conceptual jump is from pair matching to window packing, which changes the greedy invariant and often requires sliding window reasoning.
- **Artifacts have weights and values; maximize total reward of valid pairs.** This breaks the simple local greedy proof and pushes the problem toward dynamic programming or weighted matching on a structured graph.