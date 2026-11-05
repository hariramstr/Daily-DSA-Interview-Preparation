# Maximum Reliability Gain from One Maintenance Swap

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Order Statistics

---

## 🗂 Problem Overview
Given an integer array `reliability`, compute the maximum possible weighted sum `Σ reliability[i] * (i + 1)` after performing at most one swap between two distinct indices. You may also skip the swap. The naive approach checks all `O(n^2)` pairs, but `n` can reach `2 * 10^5`, so the problem is to derive the swap gain algebraically and find the best improving pair in near-linear or `O(n log n)` time.

## 🌍 Engineering Impact
This pattern shows up anywhere a score is a weighted sum over ordered entities and a single reorder operation is allowed: search ranking adjustments, job scheduling in batch pipelines, compiler pass ordering, warehouse pick-sequence optimization, and stream processing priority lanes. At small scale, brute force is acceptable; at production scale, pairwise search collapses under latency and cost budgets. The useful abstraction is not “try swaps” but “optimize a local structural mutation under a global scoring function.” That shift enables deterministic `O(n log n)` planning, predictable tail latency, and implementations that survive large arrays, skewed values, and negative weights without heuristic shortcuts.

## 🔍 Problem Statement
You are given an array `reliability` of length `n`, where `1 <= n <= 2 * 10^5` and each value lies in `[-10^9, 10^9]`. The line score is:

`score = Σ reliability[i] * (i + 1)`

using 0-based indexing. You may perform at most one swap between any two different indices `i` and `j`, after which the score is recomputed. Return the maximum achievable score. If every swap makes the score worse or equal, return the original score.

The key edge cases are:
- `n = 1`, so no swap is possible
- negative values, where moving a more negative number earlier can help
- duplicate values, where many swaps have zero effect
- large magnitudes, requiring 64-bit arithmetic

Examples:

- `reliability = [4, 1, 7, 3]` → `42`
- `reliability = [9, 8, 7]` → `46`

The critical constraint is `n = 2 * 10^5`, which rules out checking all pairs directly.

## 🪜 How to Solve This
1. Start from the weighted sum definition and ask: what changes if only two positions are swapped? Most of the array is unchanged, so the gain must depend only on those two indices and values.

2. Write the delta for swapping `i < j`:
   `gain = a[j]*(i+1) + a[i]*(j+1) - a[i]*(i+1) - a[j]*(j+1)`
   which simplifies to:
   `gain = (j - i) * (a[i] - a[j])`.

3. That immediately gives the condition for improvement: for `i < j`, a swap helps iff `a[i] > a[j]`. So we want a large earlier value, a small later value, and ideally a large distance.

4. Rearrange again:
   `gain = (a[i] - i*a[i]) + (j*a[j] - a[j]) + ...` is messy, but
   `gain = j*a[i] - i*a[i] - j*a[j] + i*a[j]`
   suggests fixing one side and querying an extremum over transformed terms.

5. For each right endpoint `j`, the best left endpoint `i < j` maximizes:
   `a[i] * (j - i) + i * a[j]`.
   Rewrite as:
   `j * a[i] - i * a[i] + i * a[j]`.
   Since both `a[i]` and `i` matter, this becomes an order-statistics problem over lines.

6. Use a Li Chao tree or convex-hull-trick style structure: each index `i` contributes a line `f_i(x) = i*x - i*a[i]`, and for current `j` we query at `x = a[j]` while also adding `j*a[i]` through the symmetric pass. In practice, the clean solution is two directional passes with a Li Chao tree over transformed lines, giving `O(n log C)`.

## 🧩 Algorithm Walkthrough
1. **Compute the base score.**  
   Sum `reliability[i] * (i + 1)` into a 64-bit accumulator. This is the answer if no profitable swap exists. The invariant is simple: `baseScore` never changes; we only search for the best nonnegative gain.

2. **Derive the swap gain formula.**  
   For `i < j`, swapping `a[i]` and `a[j]` changes only two weighted terms, so:
   `gain(i, j) = (j - i) * (a[i] - a[j])`.  
   This is correct by direct expansion and is the entire problem’s leverage point.

3. **Reframe as a maximum-query problem.**  
   Expand:
   `gain(i, j) = j*a[i] - i*a[i] + i*a[j] - j*a[j]`.  
   For fixed `j`, the last term `-j*a[j]` is constant, so we need the maximum of:
   `j*a[i] + i*a[j] - i*a[i]` over all `i < j`.  
   That is a family of linear functions evaluated at `x = a[j]`.

4. **Apply the pattern: Li Chao Tree / dynamic line container.**  
   Insert one line per prior index `i`:  
   `f_i(x) = i*x + (-i*a[i]) + j-dependent slope contribution handled by querying with parameterized form`.  
   A standard implementation stores lines of the form `m*x + b`; with the algebra arranged consistently, each query returns the best compatible left endpoint. This is the right abstraction because we need online max-over-lines under arbitrary signed coordinates.

5. **Sweep left to right.**  
   Before processing `j`, the structure contains exactly the candidate indices `i < j`. Query at `x = a[j]`, combine with `-j*a[j]` and the remaining `j*a[i]` term from the chosen line formulation, and update `bestGain`. Then insert the line generated by index `j`. The invariant is that every valid pair is considered exactly once.

6. **Return `baseScore + max(0, bestGain)`.**  
   This preserves the “at most one swap” requirement. If all gains are negative, the original ordering is already optimal.

## 📊 Worked Example
Take `reliability = [4, 1, 7, 3]`.

Base score:

| i | a[i] | weight | contribution |
|---|------|--------|--------------|
| 0 | 4    | 1      | 4            |
| 1 | 1    | 2      | 2            |
| 2 | 7    | 3      | 21           |
| 3 | 3    | 4      | 12           |

`baseScore = 39`

Now evaluate gains using `gain(i, j) = (j - i)(a[i] - a[j])`:

1. `(0,1)` → `1 * (4 - 1) = 3`
2. `(0,2)` → `2 * (4 - 7) = -6`
3. `(0,3)` → `3 * (4 - 3) = 3`
4. `(1,2)` → `1 * (1 - 7) = -6`
5. `(1,3)` → `2 * (1 - 3) = -4`
6. `(2,3)` → `1 * (7 - 3) = 4`

Best gain is `4`, from swapping indices `2` and `3`, producing `[4,1,3,7]` with score `43`.

The important trace insight is not the brute-force enumeration; it is that every candidate pair is scored by a closed-form delta depending only on two values and their distance.

## ⏱ Complexity Analysis
### Time Complexity
With a Li Chao tree or equivalent dynamic max-line structure, the sweep runs in `O(n log C)`, where `C` is the coordinate range of `reliability[i]`. In practice this behaves like `O(n log 1e9)`, which is effectively linear for `2 * 10^5` elements. At `10^6`, this is still operationally reasonable; `O(n^2)` is not. At `10^9`, neither is feasible in-memory, so the issue becomes system design, not algorithm choice.

### Space Complexity
The auxiliary space is `O(n)` in the worst case, owned by the line container plus recursion or node storage for the Li Chao tree. You can reduce constants with coordinate compression and iterative structures, but not the asymptotic bound without sacrificing query capability.

## 💡 Key Takeaways
- If a problem allows one local mutation to a global weighted sum, first derive the exact delta; many “hard” array problems collapse after that algebra.
- When the delta becomes “maximize a linear expression over prior elements,” think order statistics, convex hull trick, or Li Chao tree instead of nested loops.
- Use 64-bit arithmetic everywhere: both the base score and the swap gain can exceed 32-bit range easily under the given constraints.
- Be careful with indexing: the score uses `(i + 1)`, but the simplified gain formula uses raw zero-based positions after cancellation.
- The transferable design insight is to optimize state transitions by modeling their marginal effect directly, rather than recomputing the whole objective for every candidate action.

## 🚀 Variations & Further Practice
- Allow up to `k` swaps instead of one. The conceptual jump is from single-step marginal optimization to combinatorial interaction between swaps; greedy local gain is no longer sufficient.
- Replace swap with “move one element to any position.” The delta affects an entire interval, turning the problem into range contribution accounting rather than a two-point update.
- Maximize a weighted sum where weights are arbitrary, not `1..n`. The same delta-first mindset applies, but the line structure changes because distance is no longer the only positional factor.