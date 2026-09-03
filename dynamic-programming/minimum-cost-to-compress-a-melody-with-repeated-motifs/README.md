# Minimum Cost to Compress a Melody with Repeated Motifs

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, interval-dp, string-matching

---

## 🗂 Problem Overview
Given an integer array `notes` and a fixed repeat penalty `P`, compute the minimum cost to encode the full melody. Any subarray can be stored raw, split into consecutive blocks, or encoded as repeated copies of a shorter identical pattern plus `P`. The difficulty is that motif blocks are recursive: the repeated unit may itself be compressed optimally. With `n <= 200`, brute-force enumeration of all partitions and all candidate motifs is too expensive without interval DP and fast repetition checks.

## 🌍 Engineering Impact
This pattern shows up anywhere structured sequences need compact representations under recursive reuse: grammar-based compression, media deduplication, compiler IR canonicalization, binary delta encoding, telemetry rollups, and repeated-fragment detection in log or trace pipelines. At scale, the difference between naive segmentation and structure-aware compression is not cosmetic; it changes storage cost, cache residency, transfer latency, and downstream query performance. Without the right DP formulation, teams either miss profitable reuse or ship quadratic-plus behavior hidden inside “optimization” passes. The core architectural lesson is to separate segmentation cost from repeated-structure detection and optimize both explicitly.

## 🔍 Problem Statement
You are given an array `notes` of length `n` and an integer penalty `P`. For any subarray `notes[i..j]`, the encoding cost is the minimum of:

- storing it raw: `j - i + 1`
- splitting it into two non-empty consecutive parts
- encoding it as repeated copies of a shorter pattern whose length divides the subarray length, if every copy is identical; that cost is `cost(pattern) + P`

The pattern can itself be recursively compressed, so repeated blocks and splits can nest arbitrarily.

Return the minimum cost to encode the entire array.

**Constraints**
- `1 <= n <= 200`
- `1 <= notes[i] <= 10^9`
- `1 <= P <= 200`

**Examples**
- `notes = [4,7,4,7,4,7], P = 2` → `4`
- `notes = [5,5,5,8,5,5,5,8], P = 3` → `7`

The decisive constraint is not `n` alone, but the need to evaluate all intervals while detecting exact repetition efficiently.

## 🪜 How to Solve This
1. Read the operations carefully → every valid encoding is defined on a contiguous subarray. That is the classic signal for **interval DP**.
2. For each interval `notes[i..j]`, ask: what are all structurally different ways to encode it? There are only three: raw, split, or repeated motif.
3. Raw is trivial: cost = length.
4. Split means trying every cut `k` between `i` and `j`, so `dp[i][j] = min(dp[i][k] + dp[k+1][j])`.
5. The non-obvious part is motif compression: if the interval is `t` repeats of a shorter block of length `d`, then its cost is not `d`; it is `dp[i][i+d-1] + P`, because the base pattern may also compress.
6. That means the real subproblem is: for each interval length `L`, which divisors `d < L` produce exact repetition?
7. Once repetition detection is available, fill DP by increasing interval length so every smaller pattern and every split result is already known.
8. The resulting mental model is: **optimize over interval structure, then reuse recursively compressed representatives of periodic intervals**.

## 🧩 Algorithm Walkthrough
1. **Define the state with Interval DP.**  
   Let `dp[i][j]` be the minimum encoding cost for subarray `notes[i..j]`. This is correct because every legal encoding of a contiguous region decomposes into legal encodings of smaller contiguous regions. Invariant: when computing length `L`, all shorter intervals are finalized.

2. **Initialize the raw baseline.**  
   For every interval, start with `dp[i][j] = j - i + 1`. This guarantees a valid upper bound even when no split or repetition helps.

3. **Apply split transitions.**  
   For each cut `k` in `[i, j-1]`, update  
   `dp[i][j] = min(dp[i][j], dp[i][k] + dp[k+1][j])`.  
   This captures arbitrary partitioning into blocks. Invariant: after processing all cuts, `dp[i][j]` is optimal among all non-repetitive segmentations.

4. **Apply repeated-motif transitions.**  
   Let `L = j - i + 1`. For every divisor `d` of `L` with `d < L`, test whether `notes[i..j]` consists of `L / d` identical copies of `notes[i..i+d-1]`. If true, update  
   `dp[i][j] = min(dp[i][j], dp[i][i+d-1] + P)`.  
   This is correct because motif encoding stores one compressed representative plus the fixed repetition penalty.

5. **Use a fast repetition check.**  
   A direct element-by-element comparison per candidate can still pass at `n = 200`, but a cleaner scalable approach precomputes longest common prefixes between suffixes or an equality table over intervals. Then “is this interval periodic with base length `d`?” becomes cheap. This is where the **string-matching** tag matters: the DP is interval-based, but performance depends on efficient structural equality tests.

6. **Return `dp[0][n-1]`.**  
   By construction, it is the minimum across raw storage, all recursive splits, and all valid repeated motifs.

## 📊 Worked Example
Take `notes = [4,7,4,7,4,7]`, `P = 2`.

| Interval | Raw | Best split | Repetition check | Final |
|---|---:|---:|---|---:|
| `[4]`, `[7]` | 1 | — | — | 1 |
| `[4,7]` | 2 | `1+1=2` | no shorter divisor | 2 |
| `[4,7,4,7]` | 4 | `2+2=4` | 2 repeats of `[4,7]` → `dp([4,7])+2 = 4` | 4 |
| `[4,7,4,7,4,7]` | 6 | `2+4=6`, `4+2=6` | 3 repeats of `[4,7]` → `2+2 = 4` | 4 |

Trace:
1. Length 1 intervals cost 1.
2. Length 2 interval `[4,7]` stays raw at cost 2.
3. Length 4 interval is periodic with base length 2, but motif cost ties raw/split at 4.
4. Full length 6 interval is periodic with the same base `[4,7]`.
5. Compress the base raw for cost 2, add penalty 2, total 4.

## ⏱ Complexity Analysis
### Time Complexity
With interval DP over all `O(n^2)` subarrays, `O(n)` split points per interval, and divisor-based repetition checks accelerated by precomputed equality/LCP information, the full solution runs in about `O(n^3)`. That is practical for `n = 200`, but the same shape becomes infeasible at `10^6` or `10^9`, where interval enumeration itself is impossible.

### Space Complexity
`O(n^2)` for the DP table, plus `O(n^2)` if you precompute LCP or interval-equality support. The dominant owner is the 2D interval state. Space can be reduced only by sacrificing constant-time access to prior intervals, which usually hurts clarity and runtime more than it helps here.

## 💡 Key Takeaways
- If the problem asks for an optimum over every contiguous subarray with split decisions, it is almost certainly interval DP.
- If a subarray may be replaced by a shorter repeated unit, look for periodicity detection plus recursive reuse of the base pattern’s optimal cost.
- The motif transition uses `dp` of the base block, not the raw base length; missing that loses recursive compression entirely.
- Be careful with divisors: only proper divisors of the interval length are valid candidate pattern lengths, and every repeated copy must match exactly.
- In production systems, the transferable design insight is to decouple structural detection from optimization state: fast equivalence checks make higher-level dynamic decisions tractable.

## 🚀 Variations & Further Practice
- Allow motif blocks with variable penalties based on repeat count; harder because the repetition transition depends on both base length and multiplicity, not just periodicity.
- Permit approximate repeats with up to `k` mismatches; this shifts the problem from exact string matching to edit/error-tolerant structure detection.
- Add a dictionary budget for reusing motifs across non-contiguous intervals; this turns local interval DP into a global compression/planning problem with shared state.