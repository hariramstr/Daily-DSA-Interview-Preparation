# Maximum Audience Gain from One Seating Block Swap

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Prefix Sum

---

## 🗂 Problem Overview
Given an array `seats`, compute the maximum score obtainable after performing at most one swap of two indices. The score is the sum of `seats[i]` where `i = 0` always contributes, and every later `seats[i]` contributes only if it is strictly greater than `seats[i-1]` in the final array. The challenge is scale: with `n` up to `100000`, trying all `O(n^2)` swaps and rescoring each array in `O(n)` is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a global metric is defined by local adjacency rules and a tiny mutation budget. Examples include search-ranking pipelines where one document move changes pairwise dominance signals, streaming anomaly detectors built from neighbor comparisons, compiler instruction scheduling with local gain heuristics, and warehouse slotting or seating optimization systems with limited rearrangement cost. At scale, brute-force recomputation turns a local update into a full-array re-evaluation, which destroys latency budgets. The right approach isolates the small region whose contribution can change, enabling predictable performance and making one-shot optimization viable inside larger planning or serving systems.

## 🔍 Problem Statement
You are given an integer array `seats` where `1 <= seats.length <= 100000` and `1 <= seats[i] <= 1000000000`. You may perform **at most one swap** between two different indices, or choose not to swap. After that, evaluate the final array left to right:

- `seats[0]` always contributes to the score.
- For every `i > 0`, add `seats[i]` only if `seats[i] > seats[i-1]`.

Return the maximum possible score.

Examples:

- `seats = [5, 2, 8, 3]` → `16`
- `seats = [4, 4, 4]` → `4`

Edge cases matter: arrays of length `1`, repeated values, swaps involving adjacent indices, and cases where the best answer is obtained by making no swap. The key constraint is `n = 100000`, which rules out enumerating all swaps and recomputing the full score each time.

## 🪜 How to Solve This
1. Start with the scoring rule → every element’s contribution depends only on itself and its immediate left neighbor. That is the first signal that a swap has **local impact**, not global impact.

2. Write the score as independent per-index contributions:
   - `contrib(0) = seats[0]`
   - `contrib(i) = seats[i]` if `seats[i] > seats[i-1]`, else `0`

3. Now ask: if we swap positions `i` and `j`, which contributions can change? Only indices whose left-neighbor relationship touches `i` or `j`: specifically `i`, `i+1`, `j`, and `j+1` (with bounds handling). Everything else is unchanged.

4. That means each candidate swap can be evaluated by subtracting the old contribution of a tiny index set and adding the new contribution after the swap.

5. The remaining problem is candidate generation. We still cannot try all `O(n^2)` pairs, so we need a greedy reduction: only swaps that move a large value into a position where it can newly contribute, or remove a blocking predecessor, are worth considering.

6. Prefix/suffix maxima are the natural tool here: they let us quickly find strong swap partners on either side without scanning the whole array each time.

## 🧩 Algorithm Walkthrough
1. **Model the score as local contributions.**  
   Define `gain(i, a)` as the contribution of index `i` in array `a`: `a[0]` for `i = 0`, otherwise `a[i]` if `a[i] > a[i-1]`, else `0`.  
   **Invariant:** total score is `sum(gain(i))`.

2. **Observe the swap locality.**  
   Swapping indices `i` and `j` changes only comparisons that involve those positions or their right neighbors. The affected contribution set is `{i, i+1, j, j+1}` after deduplication and bounds checks.  
   **Why correct:** every other index keeps the same value and the same left neighbor.

3. **Precompute the baseline score.**  
   One pass computes all `gain(i)` values and their sum.  
   **Invariant:** we can evaluate any swap as `base - oldLocal + newLocal`.

4. **Use a Greedy + Prefix/Suffix candidate filter.**  
   For each position, the only interesting swap partners are extreme values that can materially improve local comparisons: typically the best larger value to the left/right or the smallest blocker to move away. Prefix maxima/minima and suffix maxima/minima provide these candidates in `O(1)` lookup per index after `O(n)` preprocessing.  
   **Why this abstraction fits:** the objective is local, but the best partner search is directional over an array, which is exactly what prefix/suffix summaries compress.

5. **Evaluate each retained candidate in constant time.**  
   For every index, test a small constant number of swap partners derived from those summaries. Recompute only the affected local gains before and after the hypothetical swap.  
   **Invariant:** each tested swap is scored exactly; no full rescoring is needed.

6. **Track the maximum over all tested swaps and the no-swap baseline.**  
   This yields an `O(n)` or near-`O(n)` solution depending on the exact candidate set design, which is the only viable regime for `n = 100000`.

## 📊 Worked Example
Take `seats = [5, 2, 8, 3]`.

Baseline contributions:

| i | value | left neighbor | contributes? | gain |
|---|-------|---------------|--------------|------|
| 0 | 5     | —             | yes          | 5    |
| 1 | 2     | 5             | no           | 0    |
| 2 | 8     | 2             | yes          | 8    |
| 3 | 3     | 8             | no           | 0    |

Baseline score = `13`.

Try swap `(1, 2)` → array becomes `[5, 8, 2, 3]`.  
Affected indices are `{1, 2, 3}`.

Recomputed local gains:

- `i=1`: `8 > 5` → `8`
- `i=2`: `2 > 8` → `0`
- `i=3`: `3 > 2` → `3`

New score = baseline `13` minus old affected gains `(0 + 8 + 0)` plus new affected gains `(8 + 0 + 3)` = `16`.

That is the maximum for this example. The important point is not the arithmetic; it is that the swap is evaluated from a constant-size neighborhood, not by rescanning the array.

## ⏱ Complexity Analysis
### Time Complexity
With baseline scoring plus prefix/suffix preprocessing, the dominant work is linear passes over the array and constant-time evaluation of a bounded candidate set per index, giving `O(n)`. That is practical for `10^6` elements and still conceptually scalable; `O(n^2)` is already impossible, and `O(n^3)` is dead on arrival.

### Space Complexity
`O(n)` auxiliary space if you store prefix/suffix extrema and per-index baseline gains. That space is owned by the directional summary arrays. It can be reduced toward `O(1)` only by sacrificing candidate lookup power or requiring multiple rescans, which usually worsens runtime.

## 💡 Key Takeaways
- If a score is defined by adjacent comparisons, first ask whether a mutation changes only a constant-size neighborhood.
- “One swap” plus “maximize a derived score” is a strong signal to decompose the score into per-index contributions and evaluate deltas, not full recomputation.
- Adjacent swaps and non-adjacent swaps share the same idea, but the affected index set must be deduplicated carefully when `j = i+1`.
- Index `0` is special: it always contributes, so treating it with the generic `a[i] > a[i-1]` rule causes subtle off-by-one bugs.
- The transferable design insight is to convert global rescoring problems into local delta updates backed by directional summaries; that is the same move used in low-latency planners, rankers, and schedulers.

## 🚀 Variations & Further Practice
- Allow up to `k` swaps instead of one. The local-delta trick still helps, but the search space becomes combinatorial and usually needs DP or beam-search-style pruning.
- Replace the scoring rule with contribution based on a wider window, such as `seats[i] > max(seats[i-w..i-1])`. The locality shrinks less cleanly, pushing the solution toward segment trees or monotonic structures.
- Support online updates and repeated “best one swap now?” queries. The hard part becomes maintaining local contributions and candidate summaries under mutation, which turns this into a dynamic data structure problem.