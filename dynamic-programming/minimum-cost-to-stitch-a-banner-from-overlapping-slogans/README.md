# Minimum Cost to Stitch a Banner from Overlapping Slogans

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string-matching, trie

---

## 🗂 Problem Overview
Given a target string, a set of reusable fragments, per-fragment costs, and a maximum allowed overlap `k`, compute the minimum cost to construct the target exactly from left to right. Each placement may overlap up to `k` already-built characters if the fragment still matches the target at that shifted position. Return the minimum total cost, or `-1` if construction is impossible. The difficulty is that overlap creates non-local dependencies, so greedy extension by cheapest or longest fragment fails.

## 🌍 Engineering Impact
This pattern shows up in systems that assemble outputs from reusable weighted pieces under alignment constraints: DNA/read stitching, incremental document reconstruction, compiler token recovery, media segment splicing, and search/snippet generation with cached phrase blocks. At scale, naive recomputation or greedy local matching either explodes combinatorially or misses globally cheaper assemblies. The right model combines dynamic programming with indexed string matching, so candidate transitions are discovered cheaply and evaluated globally. That enables predictable latency, bounded memory, and a design that remains stable when the catalog of reusable fragments grows into the thousands.

## 🔍 Problem Statement
You are given:

- `target`, with `1 <= target.length <= 5000`
- `n` reusable fragments, where `1 <= n <= 2000`
- `fragments[i]`, each of length `1..50`
- `cost[i]`, where `1 <= cost[i] <= 10^9`
- overlap limit `k`, where `0 <= k <= 50`

If the built prefix currently ends at position `p`, you may place a fragment of length `L` so that its first `t` characters overlap the already-built suffix, for any `0 <= t <= min(k, L - 1, p)`. The fragment must match `target[p - t ... p - t + L - 1]`, and the built prefix advances to `max(p, p - t + L)`.

Return the minimum total cost to build all of `target`, else `-1`.

Examples:

- `target = "ababa"`, `fragments = ["aba","baba","ab"]`, `cost = [4,5,2]`, `k = 2` → `6`
- `target = "aaaaab"`, `fragments = ["aaa","aaab","b"]`, `cost = [3,4,10]`, `k = 2` → `7`

The key pressure is `target.length = 5000`: you need indexed matching plus DP, not pairwise backtracking over placements.

## 🪜 How to Solve This
1. Start from the state definition: the only thing that matters after some placements is how far the banner is already built. That suggests DP on prefix length, `dp[p] = minimum cost to build target[0..p-1]`.

2. From a prefix boundary `p`, every legal move is “pick a fragment and an overlap `t`” such that the fragment matches target starting at `s = p - t`. If it matches, it extends coverage to `e = s + L`.

3. Brute-forcing all fragments at every `p` is too expensive. The real subproblem is: for each start index `s`, which fragments match `target[s...]`?

4. That is classic multi-pattern prefix matching. Build a trie of fragments, then scan the target from each start position up to max fragment length (`<= 50`) to enumerate all matching fragments.

5. Once matches are indexed by start position, DP becomes a shortest-path style relaxation: a state `p` can use any match beginning in `[p-k, p]` that still reaches beyond `p`.

6. The overlap bound is tiny (`<= 50`), which is the structural gift. It limits backward lookups and keeps each DP transition set small enough to evaluate directly.

## 🧩 Algorithm Walkthrough
1. **Compress duplicate fragments by minimum cost.**  
   If the same fragment appears multiple times, only the cheapest copy matters. This preserves correctness because every use of that literal string is interchangeable except for cost.

2. **Build a trie over all unique fragments.**  
   This is the string-matching index. Each terminal node stores one or more fragment lengths with their minimum cost. The trie lets us enumerate all fragments matching `target[s...]` in `O(maxLen)` per start, not `O(n * maxLen)`.

3. **Precompute all matches by start position.**  
   For each `s` in `0..m-1`, walk the trie along `target[s], target[s+1], ...` for at most `50` characters or until the path breaks. Every terminal hit yields a candidate interval `(s, e, cost)` where `e = s + length`.  
   Invariant: `matches[s]` contains exactly the fragments that can legally be anchored at `s`.

4. **Run dynamic programming on built prefix length.**  
   Let `dp[p]` be min cost to build exactly up to `p`. Initialize `dp[0] = 0`, others to infinity.

5. **Relax transitions using bounded overlap.**  
   From state `p`, valid next placements can start at any `s` in `[max(0, p-k), p]`. For each match `(s, e, c)` with `e > p`, update `dp[e] = min(dp[e], dp[p] + c)`.  
   Why correct: overlap means the fragment may start before `p`, but only within `k`, and it must extend coverage beyond `p`; otherwise it adds cost without progress.

6. **Return `dp[m]` if finite, else `-1`.**  
   This is a **Dynamic Programming + Trie-based multi-pattern matching** solution. DP handles global optimality; the trie makes transition discovery cheap enough for the input bounds.

## 📊 Worked Example
Use `target = "aaaaab"`, `fragments = ["aaa","aaab","b"]`, `cost = [3,4,10]`, `k = 2`.

Precomputed matches:

| start `s` | matching fragments | intervals |
|---|---|---|
| 0 | `aaa` | `(0,3,3)` |
| 1 | `aaa`, `aaab` | `(1,4,3)`, `(1,5,4)` |
| 2 | `aaa`, `aaab` | `(2,5,3)`, `(2,6,4)` |
| 3 | `b` only fails until index 5 | none useful |
| 5 | `b` | `(5,6,10)` |

DP trace:

1. `dp[0] = 0`
2. From `p = 0`, only `s = 0` is allowed → take `(0,3,3)` → `dp[3] = 3`
3. From `p = 3`, allowed starts are `s in [1,3]`
4. At `s = 1`, `(1,5,4)` reaches `5`; `(1,4,3)` reaches `4`
5. At `s = 2`, `(2,6,4)` is valid because overlap is `1` and it extends past `3`
6. Update `dp[6] = min(inf, dp[3] + 4) = 7`

Answer: `7`.

## ⏱ Complexity Analysis
### Time Complexity
Building the trie is `O(total fragment length)`, at most `O(n * 50)`. Match precomputation scans at most `50` characters from each target index, so `O(m * 50)`. DP checks at most `k + 1 <= 51` start positions per state, and each start has at most `50` matches in the worst case, giving `O(m * k * 50)`. This is comfortably practical at `m = 5000`; it is nowhere near viable for `10^9`-scale inputs without a different streaming model.

### Space Complexity
`O(total fragment length + total matches + m)` for the trie, precomputed match lists, and DP array. With max fragment length `50`, total matches remain bounded enough for memory safety. You can trade space for recomputation by generating matches on demand, but that increases constant factors and complicates implementation.

## 💡 Key Takeaways
- If the state is “how much prefix is already valid” and moves can reach backward a bounded amount, think DP on prefix length with constrained transitions.
- If many short patterns must be matched against every target offset, a trie is usually the right indexing structure before considering heavier automata.
- The overlap parameter is easy to misapply: legal starts are `s = p - t`, so `s` must lie in `[p-k, p]`, not `[p, p+k]`.
- Reject placements that do not extend coverage (`e <= p`); they are syntactically valid overlaps but useless and can create incorrect self-relaxations.
- In production systems, bounded local rollback or overlap often turns an intractable global assembly problem into a stable DP with predictable performance envelopes.

## 🚀 Variations & Further Practice
- Allow negative or zero-cost fragments and ask for minimum cost with cycle detection. The twist is that non-progressing or overlapping placements can create shortest-path pathologies.
- Replace exact matching with wildcard or edit-distance-tolerant fragments. The hard part becomes combining approximate string matching with DP state transitions efficiently.
- Make `k` unbounded and scale fragment lengths much larger. This pushes the solution toward automata, interval optimization, or graph shortest-path formulations with more aggressive indexing.