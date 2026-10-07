# Maximum Font Scale for Digital Signage

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, arrays, simulation

---

## 🗂 Problem Overview
Given a message, a fixed display width `W`, a sorted list of candidate font scales, and per-character widths for each scale, return the largest scale at which the full message fits on one line. For any chosen scale, total width is the sum of each character’s width at that scale. The non-trivial part is scale count: checking every scale is too expensive when both the message and the number of scales can reach `10^5`, so the monotonic fit property must be exploited.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the largest feasible configuration under a hard capacity limit. Examples include UI layout engines selecting responsive breakpoints, video encoders choosing the highest bitrate that fits bandwidth, search systems tuning result-set size under latency budgets, and distributed schedulers picking the largest batch size that still meets SLA constraints. Without exploiting monotonic feasibility, systems degrade into linear scans over large candidate spaces, wasting CPU on repeated validation. Binary search over a monotone predicate turns “try everything” into a predictable logarithmic control loop, which matters when the validation step itself is already expensive.

## 🔍 Problem Statement
You are given:

- `message`: a string of lowercase English letters, length up to `10^5`
- `W`: maximum allowed rendered width, up to `10^15`
- `scales`: strictly increasing available font scales, length up to `10^5`
- `charWidths[26][m]`: width table where `charWidths[i][j]` is the width of character `('a' + i)` at `scales[j]`

For each scale index `j`, compute:

`sum(charWidths[message[k] - 'a'][j])` for all characters in `message`

Return the largest `scales[j]` whose total is `<= W`. If even the smallest scale does not fit, return `-1`.

Monotonicity is guaranteed: for every character, widths are nondecreasing as scale increases, so if a message fits at some scale, it fits at all smaller scales.

Examples:

- `message = "cab"`, `W = 10`, `scales = [1,2,3]` → totals are `6, 9, 15` → answer `2`
- `message = "zzzz"`, `W = 7`, `scales = [2,4]` → smallest total is `8` → answer `-1`

The key constraint is `message.length * scales.length` can be `10^10`, ruling out brute force.

## 🪜 How to Solve This
1. Read the problem → the obvious check for one scale is easy: sum widths of all characters in `message` at that scale.
2. Then notice the trap → doing that for every scale is `O(n * m)`, which is too large for `10^5 * 10^5`.
3. Look for structure in the candidate space → scales are sorted, and every character width is nondecreasing with scale.
4. That implies a monotone predicate:  
   `fits(j) = totalWidthAtScale(j) <= W`  
   Once `fits(j)` becomes false, it stays false for all larger scales.
5. A monotone boolean over a sorted domain is the standard binary-search signal.
6. So the problem becomes: find the rightmost scale index where `fits(index)` is true.
7. Implement a helper `fits(mid)` that scans the message and accumulates widths for that scale.
8. Use binary search on indices, not scale values, because widths are indexed by position in `scales`.
9. Track the best valid index seen so far; if none exists, return `-1`.
10. Use 64-bit arithmetic for the width sum. With `10^5` characters and width up to `10^9`, totals can reach `10^14`.

That is the whole shape: expensive validation, monotone feasibility, binary search over the answer.

## 🧩 Algorithm Walkthrough
1. **Define the predicate (`fits`)**  
   For a scale index `j`, compute the rendered width of the full message by summing `charWidths[ch - 'a'][j]` for each character `ch` in `message`. Return `true` if the sum is `<= W`, otherwise `false`.  
   **Why correct:** this directly matches the problem definition.  
   **Invariant:** `fits(j)` exactly represents whether `scales[j]` is feasible.

2. **Establish monotonicity**  
   Because each character’s width is nondecreasing across scale indices, the total message width is also nondecreasing. Therefore, if `fits(j)` is false, then `fits(k)` is false for all `k > j`.  
   **Why correct:** sums of nondecreasing sequences remain nondecreasing.  
   **Invariant:** feasible indices form a prefix of the scale array.

3. **Apply the pattern: Binary Search on Answer**  
   Search over index range `[0, scales.length - 1]` to find the rightmost feasible index.  
   If `fits(mid)` is true, record `mid` as a candidate answer and move right (`lo = mid + 1`).  
   Otherwise move left (`hi = mid - 1`).  
   **Why correct:** the feasible region is contiguous from the left.  
   **Invariant:** all indices `<= answer` may still contain the best valid scale; all indices beyond the rejected boundary are known invalid.

4. **Return the scale value, not the index**  
   After search completes, if no feasible index was found, return `-1`; otherwise return `scales[answer]`.  
   **Why correct:** the problem asks for the actual scale value.  
   **Invariant:** `answer` is the largest index with `fits(answer) = true`.

This is a textbook **Binary Search on a Monotone Predicate** problem. The simulation is linear in message length; binary search minimizes how often that simulation runs.

## 📊 Worked Example
Take `message = "cab"`, `W = 10`, `scales = [1,2,3]`.

Relevant widths:

- `a`: `[1,2,4]`
- `b`: `[2,3,5]`
- `c`: `[3,4,6]`

| Step | lo | hi | mid | scale | total width (`c+a+b`) | fits? | best |
|---|---:|---:|---:|---:|---:|---|---:|
| 1 | 0 | 2 | 1 | 2 | `4 + 2 + 3 = 9` | yes | 1 |
| 2 | 2 | 2 | 2 | 3 | `6 + 4 + 5 = 15` | no | 1 |

Search stops because `lo = 2`, `hi = 1`.

Interpretation:

1. Scale `2` fits, so any smaller scale also fits. Try to go larger.
2. Scale `3` does not fit, so any larger scale would also fail.
3. The rightmost feasible index is `1`, so return `scales[1] = 2`.

The trace makes the monotone boundary explicit: valid totals are `[6, 9]`, then invalid totals start at `15`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log m)`, where `n = message.length` and `m = scales.length`. Each `fits(mid)` call scans the full message once, and binary search invokes it `O(log m)` times. At `10^6` elements this is routine; at `10^9`, only the logarithmic candidate search remains viable, not a full scan over all scales.

### Space Complexity
`O(1)` auxiliary space beyond the input arrays. The algorithm stores only indices, the running sum, and the best answer. Space can’t be meaningfully reduced further unless you change the input representation; the main trade-off is precomputing character frequencies to reduce repeated scans.

## 💡 Key Takeaways
- If the problem asks for the maximum feasible value in a sorted candidate set, immediately test whether feasibility is monotone.
- When validation is straightforward but expensive, and candidates are many, “binary search on answer” is usually the right abstraction.
- Use 64-bit accumulation for total width; `int` overflows well before the stated limits.
- Binary-search for the rightmost `true`, not just any `true`; update `best` before moving `lo` right.
- In production systems, monotone-feasibility search is a general pattern for turning repeated capacity checks into bounded, predictable control logic.

## 🚀 Variations & Further Practice
- Allow multiline wrapping with fixed panel height. The predicate becomes “can the message be laid out within `H` lines at scale `s`,” which is still monotone but requires a more involved layout simulation.
- Replace per-character widths with kerning-aware pair widths. Validation now depends on adjacent character pairs, increasing simulation complexity while preserving the same binary-search structure.
- Support dynamic updates to the message and many fit queries. The harder twist is amortization: precompute character frequencies or maintain segment-based aggregates so each `fits` check is sublinear in message length.