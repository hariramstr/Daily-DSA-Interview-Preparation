# Maximum Net Gain from One Detour Swap

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Prefix Sum

---

## 🗂 Problem Overview
Given an integer array `gain`, choose at most one swap of two non-overlapping contiguous subarrays, preserving internal order inside each block and the relative order of everything else. After that single edit, compute the largest possible contiguous-subarray sum in the resulting array.

The challenge is combinatorial: there are `O(n^4)` candidate block pairs, and even evaluating each swap naively is too expensive. With `n` up to `2 * 10^5`, the solution must compress the swap effect into reusable prefix/suffix dynamic-programming state.

## 🌍 Engineering Impact
This pattern shows up anywhere a mostly fixed sequence can tolerate one structural rewrite to improve a downstream objective. Examples: reordering stages in streaming pipelines to isolate expensive or lossy operators, moving a toxic shard range away from hot key ranges in storage engines, compiler pass scheduling where one pass invalidates locality for another, or search-ranking feature pipelines where one harmful segment suppresses an otherwise strong score run.

At scale, brute-force “try every rewrite” approaches collapse under latency and cost constraints. The useful abstraction is not the literal swap; it is precomputing enough prefix/suffix state to evaluate a local structural change without rebuilding the whole system view.

## 🔍 Problem Statement
You are given an array `gain` of length `n`, where `1 <= n <= 2 * 10^5` and `-10^9 <= gain[i] <= 10^9`. You may perform **at most one** operation: choose indices `l1 <= r1 < l2 <= r2`, swap the contiguous blocks `gain[l1..r1]` and `gain[l2..r2]`, and keep the order inside each block unchanged.

After the swap, compute the maximum sum over all contiguous subarrays of the new array. You may also skip the swap entirely. Return the largest value achievable.

Examples:

- `gain = [5, -100, 4, 3]` → `12`
- `gain = [-2, 7, -3, 6, -10, 5]` → `18`

Edge cases matter: all values may be negative, the best answer may use no swap, and the optimal swap may either remove a bad middle block or splice a profitable block into another profitable region. The `2 * 10^5` bound rules out anything cubic or even quadratic.

## 🪜 How to Solve This
1. Start from the target metric, not the operation: we only care about the **best subarray after one swap**, not the full post-swap array.

2. A maximum subarray is defined by how it enters and exits a region. That immediately suggests Kadane-style DP: best prefix-ending-here, suffix-starting-here, and best internal subarray.

3. Ask what a swap can actually improve. It cannot change values; it only changes **adjacency**. So the win comes from creating a new concatenation:
   - profitable left part
   - moved-in profitable block
   - profitable right part  
   or from ejecting a harmful block out of the middle.

4. That means we should precompute reusable summaries for every prefix/suffix: total sum, best prefix, best suffix, best subarray.

5. Once each interval can be summarized as a small state object, swapping two blocks becomes a question of composing segment summaries in a different order.

6. The remaining problem is search: enumerate split structure, not all swaps. Use prefix sums plus dynamic programming / segment-composition logic so each candidate boundary is evaluated from precomputed best states, yielding near-linear or `O(n log n)` time instead of brute force.

## 🧩 Algorithm Walkthrough
1. **Model each segment by four values** using the classic maximum-subarray monoid:
   - `sum`: total segment sum
   - `pref`: best prefix sum
   - `suff`: best suffix sum
   - `best`: best subarray sum  
   This is the right abstraction because concatenating two segments can be evaluated in `O(1)`:
   `sum = A.sum + B.sum`,  
   `pref = max(A.pref, A.sum + B.pref)`,  
   `suff = max(B.suff, B.sum + A.suff)`,  
   `best = max(A.best, B.best, A.suff + B.pref)`.

2. **Build prefix and suffix DP summaries** so any fixed left or right remainder is available instantly. This maintains the invariant that every processed prefix/suffix is represented exactly by its four-value summary.

3. **Reframe a swap**. If the original array is split as  
   `P | A | M | B | S`,  
   then swapping `A` and `B` yields  
   `P | B | M | A | S`.  
   The only changed region is the concatenation order inside that five-part decomposition.

4. **Search over middle boundaries, not raw intervals.** For each separator structure, maintain the best possible left block summary that could be moved across the middle, and symmetrically the best right block summary. This is where prefix sums and DP interact: you are optimizing over interval summaries, not element-by-element swaps.

5. **Use a segment tree or equivalent range-summary structure** to query any interval summary in `O(log n)` and combine it with maintained best-left / best-right candidates. This keeps the invariant that every candidate reordered region is evaluated exactly as a concatenation of monoid summaries.

6. **Take the maximum over all constructions**, including the no-swap Kadane answer. Correctness follows because every valid swap corresponds to some `P, A, M, B, S` decomposition, and the monoid composition computes the exact maximum subarray value of the reordered sequence.

## 📊 Worked Example
Take `gain = [5, -100, 4, 3]`.

We compare the no-swap baseline with the best reordered structure.

| Step | Sequence / Segment View | `best` max-subarray |
|---|---|---:|
| 1 | Original `[5, -100, 4, 3]` | `7` |
| 2 | Choose `A = [-100]`, `B = [4, 3]` | — |
| 3 | Decompose as `P=[5]`, `A=[-100]`, `M=[]`, `B=[4,3]`, `S=[]` | — |
| 4 | After swap: `P | B | M | A | S = [5, 4, 3, -100]` | — |
| 5 | Compose summaries: `[5] + [4,3]` gives prefix/suffix/best `12` before the trailing `-100` | `12` |
| 6 | Final array best subarray remains `[5,4,3]` | `12` |

The key observation is that the swap improves adjacency: `[5]` can now connect directly to `[4,3]`, while the harmful block is pushed out of the profitable run.

## ⏱ Complexity Analysis
### Time Complexity
With range-summary composition plus a logarithmic-time structure for interval queries and candidate updates, the solution runs in `O(n log n)`. The dominant cost is evaluating all split positions while querying/combining segment summaries. At `10^6` elements this is still practical; at `10^9`, even linear scans become infeasible without distribution or approximation.

### Space Complexity
The solution uses `O(n)` space for prefix/suffix DP arrays and the range-summary structure. That space is owned by stored segment summaries, not raw duplication of the array. Some DP pieces can be streamed, but reducing memory usually complicates symmetric left/right evaluation.

## 💡 Key Takeaways
- If a problem allows one structural edit but asks for an aggregate over the final sequence, look for a segment-summary algebra rather than simulating edits.
- “Maximum subarray after one rearrangement” is a strong signal for Kadane-state composition: total, best prefix, best suffix, best internal.
- The swap indices are easy to mis-handle: `r1 < l2` is strict, so the two blocks cannot overlap or touch in the wrong order.
- Empty middle `M` is valid, but empty swapped blocks are not; the no-swap option must be handled separately.
- In production systems, this is the same design move as evaluating local rewrites from cached summaries instead of rebuilding global state after every candidate mutation.

## 🚀 Variations & Further Practice
- Allow up to `k` swaps instead of one. The twist is state explosion: the DP must track how many reorder operations have been spent and how many open structural choices remain.
- Allow reversing either swapped block in addition to swapping. This breaks the simple segment summary unless you also maintain reverse-direction prefix/suffix summaries.
- Maximize a different objective after one swap, such as maximum circular subarray sum or maximum product subarray. The harder part is that the composition algebra is no longer the standard Kadane monoid.