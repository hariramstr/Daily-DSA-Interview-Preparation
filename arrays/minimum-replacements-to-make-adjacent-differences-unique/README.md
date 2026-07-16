# Minimum Replacements to Make Adjacent Differences Unique

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Hash Map

---

## 🗂 Problem Overview
Given an array `nums`, form `diff[i] = |nums[i] - nums[i + 1]|`. The goal is to make every value in `diff` unique using the fewest element replacements, where each replacement can set one `nums[i]` to any value in `[0, limit]`. The challenge is that changing one element affects one or two adjacent differences, so the optimization is not over values directly, but over which positions to modify to eliminate duplicate-difference conflicts efficiently.

## 🌍 Engineering Impact
This pattern shows up when local edits affect overlapping derived signals: streaming anomaly features, compiler IR rewrites, search-ranking feature deduplication, and telemetry pipelines that enforce uniqueness or collision-free neighboring metrics. At scale, the mistake is treating each duplicate independently, which over-pays because one change can resolve multiple adjacent conflicts. The right abstraction is conflict coverage on a path, not brute-force value search. That distinction matters in production systems where millions of local constraints are derived from neighboring records and every unnecessary mutation increases recomputation, invalidation, or downstream churn.

## 🔍 Problem Statement
You are given `nums` with `2 <= nums.length <= 100000`, `0 <= nums[i] <= 1e9`, and `1 <= limit <= 1e9`. Define the adjacent-difference array:

- `diff[i] = |nums[i] - nums[i + 1]|` for `0 <= i < nums.length - 1`

The array is **difference-distinct** if every value in `diff` appears at most once. In one operation, you may replace any single `nums[i]` with any integer in `[0, limit]`. Return the minimum number of operations required.

Examples:

- `nums = [1,4,7,10], limit = 20` → `diff = [3,3,3]` → answer `1`
- `nums = [5,5,5,5,5], limit = 10` → `diff = [0,0,0,0]` → answer `2`

The key constraint is `n = 1e5`, which rules out trying replacement values or recomputing global validity after each hypothetical edit. The solution must reason structurally about which duplicate differences must be “hit” by at least one modified endpoint.

## 🪜 How to Solve This
1. Read the problem → the real object is not `nums`, but the `diff` array of length `n - 1`.

2. Notice what makes `diff` invalid → only duplicate values matter. If a difference value appears once, it is already fine.

3. Ask what one replacement can do → changing `nums[i]` affects only edges `i - 1` and `i` in `diff`. So each operation can fix at most two neighboring bad difference slots.

4. Reframe the task → for every duplicated difference value, all but at most one of its occurrences must be destroyed. That means we need to choose array indices whose incident diff-edges cover enough duplicate occurrences.

5. This becomes a path-cover problem → each `diff[j]` corresponds to edge `(j, j+1)` in the original array. Replacing `nums[i]` selects vertex `i`, which covers adjacent edges.

6. For a duplicated value appearing on edges `E`, we may leave one edge uncovered and must cover the rest. Across all duplicated values, collect the edges that must be hit.

7. On a path, minimum vertices to cover a set of edges is greedy: scan left to right, and whenever an uncovered required edge appears, pick its right endpoint. That choice covers the current edge and maximizes help for the next one.

## 🧩 Algorithm Walkthrough
1. **Build the adjacent-difference array**  
   Compute `diff[j] = abs(nums[j] - nums[j + 1])` for `j in [0, n-2]`. This is the derived state that determines validity. The invariant is simple: any valid final array must make repeated values in this array disappear except for one surviving occurrence per value.

2. **Group equal differences with a Hash Map**  
   Store, for each difference value, the sorted list of edge indices where it appears. This is the key pattern: **Hash Map + Greedy on a Path**. The map identifies conflict groups in `O(n)` expected time.

3. **Mark required edges to destroy**  
   If a value appears `k <= 1` times, ignore it. If it appears `k >= 2` times, at least `k - 1` of those edges must change. A safe greedy choice is to keep one occurrence and require the others to be hit. Because edges lie on a path and any hit is local, the best global strategy is to maximize overlap between required edges from different groups.

4. **Reduce to minimum vertex cover on selected path edges**  
   Each required diff index `j` is edge `(j, j+1)` in `nums`. Replacing `nums[i]` means selecting vertex `i`, which covers incident required edges. On a path, the minimum number of selected vertices covering a set of edges is obtained greedily.

5. **Greedy cover left to right**  
   Sort required edge indices. Scan them in increasing order. When edge `j` is not already covered by the last chosen vertex, choose vertex `j + 1` (the right endpoint). This is the standard optimal greedy for vertex cover on a path: picking the right endpoint covers the current edge and potentially the next adjacent required edge as well.

6. **Why this is correct**  
   The invariant is: after processing edges up to position `j`, the greedy has used the minimum possible number of replacements to cover all required edges seen so far. Any solution covering edge `j` must choose either `j` or `j+1`; choosing `j+1` is never worse on a left-to-right scan because it dominates `j` for future edges.

## 📊 Worked Example
Take `nums = [1, 4, 7, 10]`, `limit = 20`.

`diff = [3, 3, 3]`

Occurrences by value:

| diff value | edge indices |
|---|---|
| 3 | [0, 1, 2] |

We may keep one occurrence of `3`, so two edges must be destroyed. A best choice is to require edges `0` and `2`, or `0` and `1`, depending on overlap. On a path, picking the middle array index can affect both edge `0` and edge `1`, or edge `1` and edge `2`.

Trace:

1. Required duplicate structure spans consecutive edges `0,1,2`.
2. Scan from left to right.
3. First uncovered bad edge is `0` → choose vertex `1` or `2` strategically.
4. Choosing interior vertex `2` changes edges `1` and `2`; choosing vertex `1` changes edges `0` and `1`.
5. Either way, one interior replacement can eliminate enough repeated `3`s to leave all differences distinct.

Answer: `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Building `diff` is linear, grouping with a hash map is linear, and the final greedy scan over required edges is also linear. This is the right regime for `10^6` elements; anything quadratic is dead on arrival, and even `O(n log n)` starts to matter under repeated batch execution.

### Space Complexity
`O(n)` auxiliary space. The dominant cost is the hash map from difference value to occurrence indices, plus storage for the required-edge set. You can reduce some overhead with streaming or compressed representations, but only by making the implementation more complex and less transparent.

## 💡 Key Takeaways
- If a local edit changes only neighboring derived values, model the problem on a path or graph instead of reasoning directly about raw array values.
- Repeated derived values are a grouping signal: build occurrence lists first, then optimize over conflict structure rather than over candidate replacements.
- The off-by-one trap is that `diff[j]` corresponds to edge `(j, j+1)` in `nums`; replacing `nums[i]` affects only `diff[i-1]` and `diff[i]` when those indices exist.
- Endpoints and interior positions are not symmetric: replacing `nums[0]` or `nums[n-1]` covers one diff edge, while an interior replacement can cover two.
- The transferable design insight is to separate feasibility of local mutation values from combinatorial optimization of mutation locations; at scale, location selection is often the real hard part.

## 🚀 Variations & Further Practice
- Require constructing one valid final array, not just the minimum count. The harder twist is coupling the optimal cover with actual value assignment inside `[0, limit]` without reintroducing collisions.
- Generalize from adjacent differences on a path to arbitrary graph edges derived from node values. The twist is that the easy greedy path cover becomes a genuine minimum vertex cover problem.
- Add weighted replacement costs per index. The twist is moving from unweighted greedy coverage to dynamic programming on a path or interval-style optimization.