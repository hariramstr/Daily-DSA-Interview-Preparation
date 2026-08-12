# Minimum Latency to Decode a Layered Signal Tape

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, string

---

## 🗂 Problem Overview
Given a string `s`, compute the minimum total cost to decode all characters when one operation may resolve the two endpoints of any contiguous segment `[l, r]` whose endpoint characters are equal. That operation costs `r - l + 1`. The challenge is that decisions interact recursively across nested substrings: choosing a wide matching pair may reduce future work or force expensive structure, so local greedy choices are not reliable.

## 🌍 Engineering Impact
This pattern shows up in systems that optimize hierarchical work under structural constraints: parser recovery over nested tokens, compiler IR rewrites across matched delimiters, genomic or signal post-processing over paired markers, and storage compaction where boundary-aligned merges affect downstream cost. At scale, naive local heuristics overfit immediate savings and miss globally cheaper decompositions. Interval DP matters when work on one range changes the feasible plans inside adjacent ranges. The value is not just correctness; it gives a decision framework for when nested batching beats independent processing, which directly affects latency, throughput, and resource scheduling.

## 🔍 Problem Statement
You are given an uppercase string `s` of length `n` (`1 <= n <= 400`). Every character must be decoded. In one pass, you may choose a contiguous substring `[l, r]` whose current leftmost and rightmost undecoded characters are equal, pay cost `r - l + 1`, and decode those two endpoints. Interior characters may be decoded before, after, or split across other passes.

Return the minimum total latency needed to decode the full string. The answer fits in a 32-bit signed integer.

Examples:

- `s = "ABCA"` → `6`  
  Decode `A...A` for `4`, then `B` and `C` individually for `1 + 1`.

- `s = "ABBA"` → `6`  
  Decode outer `A...A` for `4`, then inner `B...B` for `2`.

The key constraint is `n <= 400`: large enough that exponential recursion is impossible, small enough for cubic interval DP.

## 🪜 How to Solve This
1. Read the operation carefully → it always resolves endpoints of an interval, so the problem is fundamentally about substrings, not prefixes or counts.

2. Ask what a subproblem should represent → for any interval `s[l..r]`, define the minimum cost to fully decode just that interval. That immediately suggests interval DP.

3. Start from the simplest action → decode `s[l]` alone. That costs `1`, plus whatever it takes to decode `s[l+1..r]`.

4. Then look for ways to do better → if some `k > l` has `s[l] == s[k]`, then `s[l]` can be paired with `s[k]` inside one valid recursive structure instead of being handled alone.

5. That split creates two independent regions: the inside `s[l+1..k-1]` and the remainder `s[k+1..r]`. Their optimal costs can be combined.

6. Because every useful choice is “pair left endpoint with some matching position” or “leave it alone,” the recurrence is complete.

7. Compute intervals from short to long so every dependency is already solved when needed.

## 🧩 Algorithm Walkthrough
1. **Define the DP state — Interval DP.**  
   Let `dp[l][r]` be the minimum latency to fully decode substring `s[l..r]`. This is the right abstraction because every legal operation is anchored on interval endpoints, and once a pairing decision is made, the remaining work splits into smaller intervals.

2. **Initialize base cases.**  
   For a single character, `dp[i][i] = 1`. A length-1 interval can only be decoded by itself. This establishes the invariant that solved states represent fully decoded substrings.

3. **Default transition: decode `s[l]` alone.**  
   Set `dp[l][r] = 1 + dp[l+1][r]`. This is always valid and gives an upper bound. It means we spend one pass on the leftmost character and optimally solve the rest.

4. **Merge transition: pair `s[l]` with a matching `s[k]`.**  
   For each `k` in `(l+1..r)` where `s[l] == s[k]`, consider:
   - `dp[l+1][k-1]` for the interior,
   - `dp[k+1][r]` for the suffix,
   - plus the cost to resolve the pair `(l, k)`, which is `k - l + 1`.

   So candidate cost is  
   `dp[l+1][k-1] + (k - l + 1) + dp[k+1][r]`,  
   with empty intervals treated as `0`.

   This is correct because the operation on `[l, k]` only requires matching endpoints; the interior can be decoded in any order around it.

5. **Fill by increasing interval length.**  
   Iterate `len = 1..n`, then all `l`, with `r = l + len - 1`. This maintains the invariant that every referenced smaller interval is already computed.

6. **Return `dp[0][n-1]`.**  
   That is the optimal cost for the full tape.

## 📊 Worked Example
Take `s = "ABBA"`.

| Interval | Best choice | Cost |
|---|---|---:|
| `dp[0][0] = "A"` | single char | 1 |
| `dp[1][1] = "B"` | single char | 1 |
| `dp[2][2] = "B"` | single char | 1 |
| `dp[3][3] = "A"` | single char | 1 |
| `dp[1][2] = "BB"` | pair endpoints `[1,2]` | 2 |
| `dp[2][3] = "BA"` | decode `B` alone, then `A` | 2 |
| `dp[0][1] = "AB"` | decode `A` alone, then `B` | 2 |
| `dp[0][2] = "ABB"` | `A` alone + `dp[1][2]` | 3 |
| `dp[1][3] = "BBA"` | `B` alone + `dp[2][3]` | 3 |
| `dp[0][3] = "ABBA"` | pair `A` with `A` at `k=3` | `dp[1][2] + 4 = 2 + 4 = 6` |

Final answer: `dp[0][3] = 6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^3)`. There are `O(n^2)` intervals, and for each interval `[l, r]` we may scan all `k` in `[l+1, r]` looking for matching endpoints. With `n <= 400`, this is practical. At `10^6` or `10^9` scale, cubic growth is completely infeasible, so this approach depends on the bounded input size.

### Space Complexity
`O(n^2)` for the DP table storing every interval result. That dominates memory usage. Space cannot be reduced to linear without losing random access to smaller subintervals required by the recurrence; compression would require recomputation and usually destroys the runtime bound.

## 💡 Key Takeaways
- If legal moves are defined on substring boundaries and choices split the problem into left/inside/right regions, suspect interval DP immediately.
- When greedy “take the nearest match” or “take the widest match” feels plausible but unprovable, that is often a signal that global structure matters more than local savings.
- Be explicit about empty intervals: `dp[l+1][k-1]` or `dp[k+1][r]` may vanish, and treating them as zero avoids boundary bugs.
- Fill intervals by increasing length; any other order risks reading uninitialized subproblems and silently producing wrong minima.
- The transferable design insight is that nested batching decisions often require modeling future interaction cost, not just immediate operation cost.

## 🚀 Variations & Further Practice
- Allow one pass to decode any number of equal characters across a segment, not just the two endpoints. The twist is that merging opportunities become many-to-one, changing the recurrence shape.
- Add per-operation fixed overhead plus segment-length cost. The harder part is balancing fewer large passes against many small ones under the same interval structure.
- Restrict the number of passes or require reconstruction of an optimal decode plan. This turns pure optimization into optimization plus schedule recovery or feasibility under an additional resource bound.