# Minimum Fatigue to Encode a Morse Broadcast

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, interval-dp

---

## 🗂 Problem Overview
Given a lowercase string `s`, encode each letter with standard Morse code and minimize total transmission fatigue. Fatigue depends on adjacent Morse symbols: the first symbol costs `startCost`, then each next symbol costs `sameCost` if it matches the previous symbol, otherwise `switchCost`. Before encoding, you may partition `s` into contiguous non-empty segments and independently reverse each segment. Return the minimum possible fatigue. The challenge is that local reversal choices change global Morse-boundary transitions.

## 🌍 Engineering Impact
This pattern shows up in systems where local reorderings are allowed but global stream cost is what matters: compiler instruction scheduling, packet batching in streaming pipelines, storage layout optimization, and DNA/protein sequence transforms in bioinformatics tooling. At scale, naive enumeration of all partitions and orientations explodes combinatorially. The useful abstraction is to compress each interval into a small boundary summary plus internal cost, then compose intervals with dynamic programming. That enables predictable `O(n^3)` planning instead of exponential search, which is the difference between a deployable optimizer and an academic brute-force prototype.

## 🔍 Problem Statement
You are given a string `s` of length `1..300`, containing only lowercase English letters, and three positive integers: `startCost`, `sameCost`, and `switchCost` up to `10^6`. Each letter maps to its standard Morse code representation. The final transmitted stream is the concatenation of Morse codes of the chosen letter order.

You may split `s` into any number of contiguous non-empty groups. For each group, you may either keep the letters as-is or reverse that group once. Groups remain in original group order; only letters inside each group may flip.

Fatigue rules:
- first Morse symbol of the whole transmission costs `startCost`
- every later symbol costs `sameCost` if equal to the previous symbol
- otherwise it costs `switchCost`

Return the minimum total fatigue.

Examples:
- `s = "cab", startCost = 3, sameCost = 1, switchCost = 4` → `16`
- `s = "azaz", startCost = 2, sameCost = 5, switchCost = 1` → `14`

The key constraint is `n <= 300`: too large for exponential partition search, small enough for interval precomputation plus cubic DP.

## 🪜 How to Solve This
1. Read the operation carefully → reversing arbitrary segments means the final letter order is not arbitrary; it is built by concatenating interval choices.
2. Notice what affects cost → for any chosen segment orientation, most fatigue is internal to that Morse stream. Only the first symbol, last symbol, and internal cost matter when joining segments.
3. That suggests interval compression → for every substring `s[l..r]`, compute:
   - cost if transmitted forward
   - cost if transmitted reversed
   - first Morse symbol
   - last Morse symbol
4. Once an interval is summarized, joining two chosen pieces is easy: total cost is left cost + right cost, except the right piece’s first-symbol start charge must be replaced by a boundary transition from the left piece’s last symbol.
5. Now the problem becomes segmentation DP → `dp[i][lastSymbol]` or, more directly, `dp[i] = min cost for prefix ending at i`, trying every previous cut `j` and both orientations of `s[j..i]`.
6. The reason this works is optimal substructure: once a prefix is fixed, only its ending Morse symbol matters for how the next segment attaches.

## 🧩 Algorithm Walkthrough
1. **Precompute Morse metadata per letter**  
   Store each letter’s Morse string, length, first symbol, last symbol, and internal fatigue when started fresh. This gives constant-time access to per-letter boundaries.

2. **Build interval summaries for every `s[l..r]` in forward order**  
   Pattern: **interval DP / substring precomputation**.  
   For each interval, compute:
   - `firstF[l][r]`: first Morse symbol of `s[l..r]`
   - `lastF[l][r]`: last Morse symbol
   - `costF[l][r]`: fatigue of transmitting that interval alone, including `startCost`  
   Extend from `r-1` to `r` by adding the new letter’s internal cost and one boundary transition between the previous interval’s last symbol and the new letter’s first symbol. The invariant is: `costF[l][r]` is exact for the forward letter order.

3. **Derive reversed-interval summaries**  
   Reversing letters in `s[l..r]` is equivalent to reading the same substring from `r` down to `l`. Precompute `costR[l][r]`, `firstR[l][r]`, and `lastR[l][r]` similarly, or reuse forward tables on reversed indexing. The invariant is identical: each reversed summary fully describes that oriented segment.

4. **Run segmentation DP over prefixes**  
   Let `dp[i][b]` be the minimum fatigue for prefix `s[0..i-1]` whose transmitted Morse stream ends with symbol `b` (`0='.'`, `1='-'`).  
   For every cut position `j < i`, try segment `s[j..i-1]` in both orientations. If the prefix before `j` ends with symbol `x`, joining adds:
   `segmentCost - startCost + transition(x, segmentFirst)`  
   because the segment’s standalone first-symbol charge is replaced by the real boundary cost. For `j = 0`, use the segment cost directly.

5. **Take the best terminal state**  
   Answer is `min(dp[n][0], dp[n][1])`. Correctness follows from optimal substructure: any optimal full solution ends with some last segment and some final Morse symbol, both covered by the transition.

## 📊 Worked Example
Use `s = "cab"`, `startCost = 3`, `sameCost = 1`, `switchCost = 4`.

Morse:
- `c = -.-.`
- `a = .-`
- `b = -...`

Representative interval summaries:

| Interval | Orientation | First | Last | Standalone Cost |
|---|---|---:|---:|---:|
| `c` | forward | `-` | `.` | 12 |
| `a` | forward | `.` | `-` | 7 |
| `b` | forward | `-` | `.` | 9 |
| `cab` | forward | `-` | `.` | 19 |
| `cab` | reversed = `bac` | `-` | `.` | 16 |

Prefix DP trace:
1. Prefix `c`: best = `12`, ending with `.`
2. Prefix `ca`:
   - split as `c | a` → `12 + (7 - 3 + switch('.', '.')) = 20`
   - whole `ca` → `16`
3. Prefix `cab`:
   - whole forward `cab` → `19`
   - whole reversed `bac` → `16`
   - other splits are worse

Minimum fatigue is `16`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^3)` in the straightforward formulation: `O(n^2)` interval summaries, then `O(n^2)` prefix/segment transitions with constant orientation and boundary work, or `O(n^3)` if interval costs are built by direct substring accumulation instead of incremental reuse. At `n = 300`, cubic work is practical; at `10^6` or `10^9`, it is completely infeasible and demands a different structure.

### Space Complexity
`O(n^2)` for forward and reversed interval cost/boundary tables, plus `O(n)` or `O(n·2)` for the prefix DP. Space is owned by substring summaries. You can reduce constants by storing only needed boundary metadata, but not the asymptotic bound without recomputing intervals and paying more time.

## 💡 Key Takeaways
- If a problem allows arbitrary partitioning of a string into contiguous intervals with per-interval transformations, think interval summaries plus segmentation DP.
- If composition cost depends only on boundary state, compress each interval to internal cost + first/last symbols instead of carrying full content.
- The main trap is double-counting the segment’s `startCost` when attaching it after an existing prefix; replace it with the actual boundary transition.
- Reversed substring cost is about reversed **letter order**, not reversed Morse symbols; do not reverse dots and dashes inside a letter.
- The production-grade insight is to summarize expensive local structure into a small composable interface; that is the core move behind scalable planners and optimizers.

## 🚀 Variations & Further Practice
- Allow each segment to be either kept, reversed, or cyclically rotated. The twist is that interval state is no longer just two orientations; you now need richer boundary enumeration or more expensive preprocessing.
- Charge fatigue based on runs of equal Morse symbols with nonlinear cost. The twist is that boundary composition must track run lengths, not just end symbols.
- Permit reordering of segments after partitioning. The twist turns segmentation DP into a much harder combinatorial ordering problem, closer to TSP-style state explosion or minimum-cost arrangement.