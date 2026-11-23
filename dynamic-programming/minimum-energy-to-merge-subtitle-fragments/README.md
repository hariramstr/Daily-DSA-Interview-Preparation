# Minimum Energy to Merge Subtitle Fragments

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Interval DP, Prefix Sum

---

## 🗂 Problem Overview
Given an array `lengths`, merge all subtitle fragments into one final fragment while preserving original order. Each operation may merge only adjacent groups, and the cost of a merge equals the total length of the merged contiguous group. Return the minimum total energy across all valid merge orders. The challenge is that local choices are not reliable: the cost paid now changes future merge costs, so the problem requires global optimization over intervals.

## 🌍 Engineering Impact
This pattern shows up anywhere ordered segments are repeatedly consolidated and each consolidation cost depends on total payload size: subtitle rendering pipelines, media transcoding batches, log compaction, SSTable merge scheduling, compiler token stream reduction, and document chunk aggregation in search or LLM preprocessing. At scale, naive or greedy merge policies inflate total work because every early decision changes downstream recomputation cost. Interval DP matters when adjacency is a hard constraint and merge cost is additive over ranges. It enables predictable optimization under sequencing constraints, which is exactly where heuristic schedulers often fail in production systems.

## 🔍 Problem Statement
You are given `lengths`, where `lengths[i]` is the character count of subtitle fragment `i`. You must merge all fragments into exactly one fragment. A merge can only combine adjacent groups, and the energy cost of that merge is the total number of characters in the resulting contiguous group.

Return the minimum total energy required.

Constraints:

- `1 <= lengths.length <= 300`
- `1 <= lengths[i] <= 10^4`
- Result fits in a 32-bit signed integer

Examples:

- `lengths = [4,2,7]` → `19`
  - Merge `[4,2]` → cost `6`, then `[6,7]` → cost `13`, total `19`
- `lengths = [3,5,1,2]` → `22`
  - Merge `[1,2]` → `3`, then `[5,3]` → `8`, then `[3,8]` → `11`, total `22`

The key constraint is `n <= 300`: large enough to rule out brute-force enumeration of merge orders, but small enough for cubic interval DP.

## 🪜 How to Solve This
1. Read the problem → notice two hard constraints: merges must preserve order, and only adjacent groups can be combined. That immediately rules out Huffman-style greedy merging.

2. Ask what a final merge looks like → every interval `[i..j]` must eventually be split into a left part `[i..k]` and right part `[k+1..j]`, merged after both sides are already internally merged.

3. That suggests subproblems → define the minimum cost to fully merge any subarray into one fragment. If we know that for smaller intervals, we can build larger ones.

4. What is the recurrence?  
   Cost to merge `[i..j]` = best split cost on left + best split cost on right + total length of `[i..j]`.

5. The interval sum appears in every candidate split → precompute prefix sums so range totals are `O(1)` instead of `O(n)`.

6. Fill DP by increasing interval length → smaller intervals must be solved before larger ones. This is the standard interval DP dependency order.

7. Final answer is the cost for the full range `[0..n-1]`.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums**  
   Build `prefix` where `prefix[t]` is the sum of the first `t` fragment lengths. Then `sum(i, j) = prefix[j+1] - prefix[i]`.  
   **Why:** every DP state needs the total size of its interval.  
   **Invariant:** any interval sum is available in `O(1)`.

2. **Define the DP state**  
   Let `dp[i][j]` be the minimum energy to merge fragments from index `i` to `j` into one fragment.  
   **Why:** the problem is naturally about contiguous ranges, so this is a canonical **Interval DP** formulation.  
   **Invariant:** once computed, `dp[i][j]` is optimal for that exact interval.

3. **Initialize base cases**  
   For `i == j`, `dp[i][i] = 0` because one fragment is already merged.  
   **Why:** no operation is needed.  
   **Invariant:** all length-1 intervals are solved.

4. **Process intervals by increasing length**  
   For each interval length from `2` to `n`, evaluate every `[i..j]`.  
   **Why:** `dp[i][j]` depends only on strictly smaller intervals.  
   **Invariant:** when computing `[i..j]`, all required subproblems already exist.

5. **Try every split point**  
   For each `k` in `[i, j-1]`, split into `[i..k]` and `[k+1..j]`. Candidate cost is:  
   `dp[i][k] + dp[k+1][j] + sum(i, j)`  
   Take the minimum over all `k`.  
   **Why:** the final merge of `[i..j]` must combine exactly two adjacent merged subranges. This enumerates all legal final decisions.  
   **Invariant:** after scanning all `k`, `dp[i][j]` is globally optimal.

6. **Return `dp[0][n-1]`**  
   This is the minimum cost to merge the full array.  
   **Why this abstraction fits:** the structure is recursive over contiguous intervals, costs are additive, and adjacency is mandatory. That is textbook Interval DP.

## 📊 Worked Example
Example: `lengths = [3,5,1,2]`

Prefix sums: `[0,3,8,9,11]`

| Interval | Best split | Computation | `dp` |
|---|---:|---|---:|
| `[0,1]` | 0 | `0 + 0 + sum(0,1)=8` | 8 |
| `[1,2]` | 1 | `0 + 0 + sum(1,2)=6` | 6 |
| `[2,3]` | 2 | `0 + 0 + sum(2,3)=3` | 3 |
| `[0,2]` | 0 or 1 | `min(0+6+9, 8+0+9)` | 15 |
| `[1,3]` | 2 | `min(0+3+8, 6+0+8)` | 11 |
| `[0,3]` | 0,1,2 | `min(0+11+11, 8+3+11, 15+0+11)` | 22 |

Trace:
1. Merge `[1,2]` at indices `2..3` → cost `3`
2. Merge resulting last two groups `[5,3]` → cost `8`
3. Merge `[3,8]` → cost `11`

Total = `3 + 8 + 11 = 22`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^3)`. There are `O(n^2)` intervals, and each interval tries `O(n)` split points. Prefix sums keep range-sum lookup at `O(1)`, so the cubic DP dominates. At `n = 300`, this is practical. At `10^6` or `10^9`, cubic growth is completely intractable and demands a different problem structure or optimization theorem.

### Space Complexity
`O(n^2)` for the DP table, plus `O(n)` for prefix sums. The DP matrix owns the memory cost. Space can be reduced only with stronger structural properties; in the general interval-DP form, dropping full 2D state usually breaks dependency access or complicates reconstruction.

## 💡 Key Takeaways
- If the problem says “merge adjacent groups,” “preserve order,” and “cost depends on the whole interval,” think Interval DP immediately.
- If greedy feels tempting but each merge changes future costs, that is a strong signal that local optimization is unsafe and subproblem optimality over ranges is the right model.
- Be precise with interval sums: use `prefix[j+1] - prefix[i]`; most bugs here are off-by-one errors in inclusive range handling.
- Initialize `dp[i][i] = 0` and iterate interval lengths from small to large; any other fill order risks reading uncomputed states.
- In production systems, sequencing constraints often destroy the validity of globally greedy schedulers; explicit dynamic programming restores correctness when adjacency and recomputation costs interact.

## 🚀 Variations & Further Practice
- **Minimum Cost to Cut a Stick** — same interval-DP shape, but the cost comes from cutting an interval rather than merging it; the twist is that cut positions are sparse and must be normalized.
- **Burst Balloons** — interval DP where the last operation in an interval is easier to model than the first; the twist is that boundary values affect local reward.
- **Merge Stones (K-way merges)** — harder because you may merge exactly `K` adjacent piles at a time, which adds feasibility conditions and a more complex DP state.