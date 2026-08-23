# Minimum Cost to Reorder Containers Through Two Staging Lanes

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, sequence-partitioning, subsequence

---

## 🗂 Problem Overview
Given an array `weights`, assign each element to exactly one of two order-preserving lanes, then merge the two lane fronts into a single nondecreasing loading sequence. If consecutive loaded weights are `p, w`, the cost contribution is `|w - p|`; the first item is free. Return the minimum total cost, or `-1` if no such merge exists. The hard part is that feasibility and optimal cost are coupled: the partition must support a valid merge, not just a low local transition cost.

## 🌍 Engineering Impact
This pattern shows up whenever a fixed arrival stream must be split across a tiny number of ordered buffers and later reassembled under monotonicity constraints. Examples include packet resequencing with bounded staging queues, storage compaction pipelines that preserve per-shard order, compiler or ETL passes that interleave two stable streams, and warehouse/port automation with parallel staging lanes. At scale, brute-force partitioning explodes, while greedy local placement fails because future mergeability depends on both active subsequences. The dynamic-programming view turns an exponential scheduling/search problem into a predictable `O(n^2)` optimization with explicit feasibility guarantees.

## 🔍 Problem Statement
You are given an array `weights` of length `n` where `1 <= n <= 3000` and `1 <= weights[i] <= 10^9`. Each arriving container must be assigned to lane `A` or lane `B`. Within each lane, original order must be preserved. After assignment, the final loading order is formed by repeatedly taking the front element from either lane until both are empty.

The resulting loaded sequence must be nondecreasing. Its cost is the sum of absolute differences between adjacent loaded weights; the first loaded container contributes `0`.

Return the minimum possible total cost, or `-1` if no valid partition-and-merge exists.

Examples:

- `weights = [4, 1, 3, 2]` → `3`
- `weights = [3, 1, 2, 1]` → `-1`

The key constraint is `n <= 3000`: too large for exponential partition search, but small enough for carefully designed quadratic dynamic programming.

## 🪜 How to Solve This
1. Start with feasibility, not cost → the final sequence must be nondecreasing, so each lane must itself be nondecreasing. Otherwise that lane alone would force an inversion during the merge.

2. Reframe the problem → we are partitioning the original array into two nondecreasing subsequences. That is the exact structural constraint; the merge is then determined by always taking the smallest available front compatible with monotonic output.

3. Notice what cost really depends on → once the final loaded order is nondecreasing, `|w - p|` becomes `w - p`. The total cost telescopes to `last_loaded - first_loaded`.

4. That means optimization is simpler than it looks → among all feasible two-lane partitions, minimize `max(weights) - chosen_first_loaded_weight)`; equivalently, maximize the smallest element that can appear first in some feasible partition.

5. To know whether a choice is feasible, local greedy decisions are insufficient → assigning one item to lane `A` changes what future items can still fit in `B`.

6. This suggests DP on lane tails → process items left to right, track the last value placed in one lane and the index of the last item in the other. That captures exactly the future feasibility state in `O(n^2)`.

## 🧩 Algorithm Walkthrough
1. **Core pattern: Dynamic Programming over sequence partition state.**  
   Process containers in arrival order. After placing the first `i` items, a state records which item was placed last into one lane and what the tail value of the other lane is. This is the right abstraction because future placements only care whether each lane remains nondecreasing.

2. **Define the DP state compactly.**  
   Let `dp[i][j]` be the minimum loading cost for a feasible assignment of the prefix ending at position `i`, where item `i` is the tail of the lane that just received the latest item, and item `j` is the tail of the other lane (`j = 0` can represent an empty lane with sentinel `-∞`). This avoids storing full subsequences.

3. **Transition by placing the next item `k = max(i, j) + 1`.**  
   You have two choices: append `weights[k]` to the lane ending at `i`, or append it to the lane ending at `j`. A transition is valid only if the chosen lane tail is `<= weights[k]`, preserving nondecreasing order within that lane.

4. **Accumulate cost only when the loaded sequence boundary changes.**  
   Because any feasible final merge is globally nondecreasing, the total adjacent-difference cost telescopes. In DP form, this means the incremental effect can be modeled from the current smallest possible front / first-loaded choice, or equivalently by tracking the best achievable starting value for each feasible state.

5. **Use feasibility-first DP, then derive cost.**  
   A practical formulation is: compute all feasible two-lane partitions of the prefix and, for each state, maximize the minimum first-loadable value. Once feasibility reaches all `n` items, answer is `max(weights) - best_first`, since the final nondecreasing merge must end at the global maximum.

6. **Why this is correct.**  
   The invariant is: every DP state represents exactly the information needed to decide whether future items can still be appended without violating lane monotonicity. Since lane internals are order-preserving and nondecreasing, any reachable terminal state corresponds to a valid merge; maximizing the first loaded value minimizes the telescoping total cost.

## 📊 Worked Example
Take `weights = [4, 1, 3, 2]`.

A feasible partition is:

- Lane A: `[4]`
- Lane B: `[1, 3, 2]` → invalid because `3 > 2`
- So try:
  - Lane A: `[4]`
  - Lane B: `[1, 2]`
  - Remaining `3` must go to A → Lane A becomes `[4, 3]` → also invalid

The valid partition is:

| Step | Item | Lane A | Lane B | Feasible? |
|---|---:|---|---|---|
| 1 | 4 | [4] | [] | Yes |
| 2 | 1 | [4] | [1] | Yes |
| 3 | 3 | [4] | [1, 3] | Yes |
| 4 | 2 | [4] | [1, 2] and move 3 to A earlier | N/A |

Better assignment:

- Lane A: `[4, 3]`
- Lane B: `[1, 2]`

Both lanes preserve original order. Merge as `[1, 2, 3, 4]`, which is nondecreasing. Cost is:

- `|2-1| + |3-2| + |4-3| = 1 + 1 + 1 = 3`

So the answer is `3`.

## ⏱ Complexity Analysis

### Time Complexity
The expected solution is `O(n^2)`. There are `O(n^2)` DP states representing pairs of lane tails, and each state performs `O(1)` transition work. At `n = 3000`, this is practical; at `10^6`, quadratic DP is already prohibitive, and at `10^9` it is completely infeasible without a different structural reduction.

### Space Complexity
Space is `O(n^2)` for the full DP table, or `O(n)` to `O(n^2)` depending on the exact state compression strategy. The dominant cost is storing feasibility/optimality per pair of tails. Some rolling-array reductions are possible, but they complicate reconstruction and state interpretation.

## 💡 Key Takeaways
• If a problem says “split one sequence into a fixed small number of order-preserving subsequences,” think sequence-partition DP, not sorting or greedy scheduling.  
• If feasibility depends on the tails of multiple partially built subsequences, that is a strong signal that pair-state DP is the right model.  
• The biggest trap is forgetting that each lane must itself be nondecreasing; preserving original order alone is not enough.  
• Sentinel handling matters: representing an empty lane cleanly avoids special-case bugs in transitions and first-element logic.  
• The production lesson is broader than this puzzle: bounded-buffer reordering problems often become tractable only after reducing full history to a minimal state boundary.

## 🚀 Variations & Further Practice
- Generalize from two lanes to `k` lanes: feasibility becomes partition into `k` nondecreasing subsequences, and the state space grows combinatorially unless you exploit stronger structure.
- Change the objective from adjacent-difference cost to arbitrary pairwise transition penalties: telescoping disappears, so feasibility and optimization can no longer be separated cleanly.
- Allow online assignment with no lookahead: the problem becomes adversarial/streaming, where competitive analysis replaces exact offline DP.