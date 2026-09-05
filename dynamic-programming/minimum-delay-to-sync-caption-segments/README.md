# Minimum Delay to Sync Caption Segments

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, prefix-frequency

---

## 🗂 Problem Overview
Given a lowercase string `s`, partition it into contiguous segments whose lengths are all between `minLen` and `maxLen`. Each segment has a normalization cost: the fewest character replacements needed to make every character in that segment identical. Return the minimum total cost across all valid partitions, or `-1` if no full partition exists. The challenge is that partition boundaries and per-segment costs interact, making naive enumeration of all splits too expensive.

## 🌍 Engineering Impact
This pattern shows up in streaming pipelines, speech or caption post-processing, log compaction, packet batching, and storage block normalization: you must choose chunk boundaries while each chunk has an internal cleanup cost. At small scale, brute-force partition search is tolerable; at production scale, it explodes because local segment quality and global packing constraints are coupled. Dynamic programming with cheap segment scoring enables predictable latency, bounded memory, and deterministic optimization under strict window-size constraints. Without it, systems either overfit to greedy cuts or pay unacceptable recomputation costs as input size grows.

## 🔍 Problem Statement
You are given a string `s` of length up to `5000`, containing only lowercase English letters, and two integers `minLen` and `maxLen` where `1 <= minLen <= maxLen <= 100`. Partition the full string into contiguous segments such that every segment length is in `[minLen, maxLen]`.

For a segment, its cost is the minimum number of character changes needed to make all characters in that segment equal. Equivalently, if the segment length is `L` and its most frequent character appears `f` times, the cost is `L - f`.

Return the minimum total cost over all valid partitions, or `-1` if no valid partition covers the entire string.

Examples:

- `s = "abacbc", minLen = 2, maxLen = 3` → `2`
- `s = "aaabbbcc", minLen = 3, maxLen = 3` → `-1`

The key constraint is `n = 5000`: too large for exhaustive partition enumeration, but small enough for `O(n * maxLen * alphabet)` dynamic programming.

## 🪜 How to Solve This
1. Read the problem → we are not just evaluating substrings; we must cover the entire string with valid-length segments. That is a partition DP signal.
2. For any segment, cost depends only on character frequencies inside that segment: `length - maxFrequency`. So the real subproblem is: if the last segment ends at position `i`, what is the cheapest valid previous cut?
3. Define `dp[i]` as the minimum cost to partition the prefix `s[0..i)`. Then every valid last segment has some length `L` in `[minLen, maxLen]`, starting at `i - L`.
4. That gives the recurrence: `dp[i] = min(dp[i-L] + cost(i-L, i-1))` over all valid `L`.
5. The remaining issue is computing segment cost fast enough. Since `maxLen <= 100` and alphabet size is 26, we can build frequencies incrementally while extending the segment backward from `i`.
6. This avoids precomputing all substring costs and keeps the implementation tight: for each end index, scan backward at most 100 characters, maintain counts and current max frequency, and relax `dp[i]`.

## 🧩 Algorithm Walkthrough
1. **Use Dynamic Programming over prefixes.**  
   Let `dp[i]` be the minimum synchronization delay for the first `i` characters. `dp[0] = 0`, and all other states start as infinity. This is correct because every valid solution for prefix `i` must end with exactly one final segment.

2. **Enumerate the final segment ending at each position.**  
   For each `i` from `1` to `n`, consider segment lengths `L` from `1` to `maxLen`, stopping when `i - L < 0`. Only lengths in `[minLen, maxLen]` are eligible for transition. The invariant is: while processing `i`, every candidate last segment is examined exactly once.

3. **Maintain frequency counts incrementally.**  
   As you extend backward from `s[i-1]` to `s[i-L]`, update a 26-element frequency array and track `maxFreq` for that segment. Then `segmentCost = L - maxFreq`. This is the **prefix-DP + rolling frequency window** pattern: local segment scoring is computed on the fly instead of recomputed from scratch.

4. **Relax DP transitions.**  
   If `dp[i-L]` is reachable, update `dp[i] = min(dp[i], dp[i-L] + segmentCost)`. This is correct because concatenating an optimal partition of the prefix with one valid final segment yields a valid partition of the larger prefix.

5. **Handle impossible states explicitly.**  
   If no valid transition reaches `dp[n]`, return `-1`. This captures cases where the total length cannot be composed from allowed segment sizes, regardless of segment costs.

6. **Why this abstraction fits.**  
   The problem is not greedy: a locally cheap segment can force an impossible or expensive suffix. Prefix DP is the right abstraction because cut decisions have forward consequences, while segment cost is purely local and efficiently maintainable.

## 📊 Worked Example
Take `s = "abacbc"`, `minLen = 2`, `maxLen = 3`.

| `i` | Prefix | Candidate last segment | Segment cost | Previous `dp` | New `dp[i]` |
|---|---|---|---:|---:|---:|
| 0 | `""` | — | — | — | 0 |
| 2 | `ab` | `ab` | 1 | `dp[0]=0` | 1 |
| 3 | `aba` | `aba` | 1 | `dp[0]=0` | 1 |
| 4 | `abac` | `ac` / `bac` | 1 / 2 | `dp[2]=1`, `dp[1]=∞` | 2 |
| 5 | `abacb` | `cb` / `acb` | 1 / 2 | `dp[3]=1`, `dp[2]=1` | 2 |
| 6 | `abacbc` | `bc` / `cbc` | 1 / 1 | `dp[4]=2`, `dp[3]=1` | 2 |

Best final transition is `dp[3] + cost("cbc") = 1 + 1 = 2`, corresponding to `"aba" + "cbc"`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * maxLen * 1)` for the backward scan, with `O(1)` updates per extension because the alphabet is fixed at 26. More explicitly, it is `O(n * maxLen)`, bounded here by about `5000 * 100 = 5e5` segment extensions. That is trivial at `10^6`-scale operations and still far from `10^9`, where architecture and cache behavior start dominating.

### Space Complexity
`O(n + 26)` = `O(n)`. The `dp` array owns the asymptotic space; the frequency array is constant-sized. You could not reduce below `O(n)` cleanly without sacrificing straightforward state access, since each prefix depends on many earlier prefixes within the allowed length window.

## 💡 Key Takeaways
- If a problem asks for a minimum cost to partition a sequence into valid contiguous chunks, think prefix dynamic programming immediately.
- If each chunk’s score depends on frequency or majority count inside a bounded window, incremental counting often removes the need for substring precomputation.
- Be careful with indexing: `dp[i]` should represent the first `i` characters, so a segment of length `L` ends at `i` and starts at `i - L`.
- Do not treat unreachable prefixes as zero-cost; initialize them to infinity and guard transitions, or impossible partitions will silently look valid.
- In production systems, bounded local recomputation plus global DP is a common pattern for turning combinatorial search into deterministic, latency-safe optimization.

## 🚀 Variations & Further Practice
- Allow arbitrary alphabets or Unicode categories; the conceptual twist is that segment scoring may no longer be constant-time per extension unless you compress symbols or change data structures.
- Add a penalty per cut or a target number of segments; now the DP state must track both prefix position and segmentation count, increasing dimensionality.
- Permit segment-specific rules, such as different allowed lengths by character class or weighted replacement costs; the harder part becomes redefining local cost while preserving efficient transitions.