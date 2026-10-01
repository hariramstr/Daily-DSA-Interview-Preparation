# Longest DNA Read Window With Bounded GC Imbalance

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, String

---

## 🗂 Problem Overview
Given a DNA string `s` and thresholds `k` and `m`, find the maximum length of a contiguous substring where two constraints hold simultaneously: `|count('G') - count('C')| <= k` and `count('A') + count('T') <= m`. Return the longest valid non-empty window, or `0` if none exists. The challenge is scale: with `|s|` up to `2 * 10^5`, enumerating and validating all substrings is computationally infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere you need the longest contiguous region satisfying bounded aggregate constraints: genomic read QC, streaming anomaly detection, log segmentation, search-session analysis, and network traffic windows with multiple policy limits. At production scale, brute-force substring or interval validation collapses under quadratic growth, especially in pipelines processing millions of records or long event streams. A sliding-window design turns repeated recomputation into incremental state maintenance, which reduces latency, improves cache behavior, and makes online processing feasible. The broader architectural value is recognizing when a local validity predicate can be maintained as a moving frontier instead of recomputed from scratch.

## 🔍 Problem Statement
You are given a string `s` of length `1` to `2 * 10^5`, containing only `'A'`, `'C'`, `'G'`, and `'T'`, plus integers `k` and `m` where `0 <= k, m <= s.length`. A contiguous segment is valid if:

- `|#G - #C| <= k`
- `#A + #T <= m`

Return the length of the longest valid contiguous non-empty substring. If no non-empty substring satisfies both constraints, return `0`.

Examples:

- `s = "GCGATCGG", k = 1, m = 2` → `6`  
  Valid longest window: `"CGATCG"`

- `s = "ATATGGCCG", k = 0, m = 1` → `5`  
  Valid longest window: `"TGGCC"`

The key constraint is input size. Any approach that checks all substrings, or even re-counts characters for many candidate windows, will time out. The solution must update window state incrementally in linear time.

## 🪜 How to Solve This
1. Read the problem → the output is about a **contiguous** segment, not an arbitrary subsequence. That immediately suggests a window-based approach.

2. Notice the validation rule depends only on **counts inside the current interval**: number of `G`, `C`, and total `A/T`. Counts are additive, so they can be updated when the window expands or shrinks.

3. Ask the key question: if a window becomes invalid, can moving the left boundary restore validity without revisiting old work? Yes. Removing characters only decreases `A/T`, and adjusts the `G-C` imbalance predictably.

4. That gives the standard two-pointer shape:  
   expand right → update counts → while invalid, advance left → record best valid length.

5. Why this works: every character enters the window once and leaves once. No nested rescans, no substring materialization, no prefix-sum search over all pairs.

6. The mental model is not “find all valid substrings.” It is “maintain the maximal valid suffix ending at each right index.” Once that clicks, the linear solution becomes the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers / Sliding Window pattern.**  
   Maintain a window `[left, right]` over `s`. Track three pieces of state: `gCount`, `cCount`, and `atCount` where `atCount = count('A') + count('T')`. This abstraction is right because validity depends only on aggregate counts over a contiguous range.

2. **Expand the window one character at a time.**  
   For each `right`, incorporate `s[right]` into the counters. This gives the current candidate segment ending at `right`.

3. **Check validity after each expansion.**  
   The window is valid iff `abs(gCount - cCount) <= k` and `atCount <= m`. If both hold, the current window is admissible.

4. **Shrink from the left while invalid.**  
   If either constraint is violated, repeatedly remove `s[left]` from the counters and increment `left`. This is correct because any valid window ending at `right` must start at or after the first position that restores both constraints.

5. **Maintain the invariant.**  
   After the shrink loop finishes, `[left, right]` is the longest valid window ending at `right` with the current leftmost feasible boundary. The invariant is: all counts exactly match the current window, and the window is valid.

6. **Update the answer.**  
   Record `maxLen = max(maxLen, right - left + 1)` after restoring validity. Since every valid endpoint is considered, the global maximum is found.

7. **Why linear time holds.**  
   `right` advances `n` times. `left` also advances at most `n` times total. Each character is added once and removed once, so the algorithm is `O(n)`.

## 📊 Worked Example
Example: `s = "ATATGGCCG"`, `k = 0`, `m = 1`

| right | char | Window after shrink | G | C | A/T | Valid? | best |
|------:|------|---------------------|--:|--:|----:|:------:|-----:|
| 0 | A | `A` | 0 | 0 | 1 | yes | 1 |
| 1 | T | `T` | 0 | 0 | 1 | yes | 1 |
| 2 | A | `A` | 0 | 0 | 1 | yes | 1 |
| 3 | T | `T` | 0 | 0 | 1 | yes | 1 |
| 4 | G | `TG` | 1 | 0 | 1 | no | 1 |
| 5 | G | `TGG` | 2 | 0 | 1 | no | 1 |
| 6 | C | `TGGC` | 2 | 1 | 1 | no | 1 |
| 7 | C | `TGGCC` | 2 | 2 | 1 | yes | 5 |
| 8 | G | `GGCCG` | 3 | 2 | 0 | no | 5 |

The important behavior is at `right = 7`: once `G` and `C` rebalance and `A/T` stays within budget, the window jumps to length `5`. The shrink loop ensures the left boundary is always the earliest position that preserves validity.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = s.length`. Each character is processed when the right pointer enters it and at most once more when the left pointer removes it. That constant-work-per-move property is what keeps the solution viable at `10^6` scale; a quadratic approach is already unusable far below `10^9`.

### Space Complexity
`O(1)`. The algorithm stores a few counters and pointer indices, independent of input length. Space is owned entirely by scalar window state. You could generalize to a frequency map for larger alphabets, but that trades fixed-size counters for slightly higher constant overhead.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** region under count-based constraints, sliding window should be your first hypothesis.
- When validity can be updated incrementally as elements enter and leave a window, that is a strong signal that two pointers can replace brute-force substring checks.
- Be careful to shrink in a `while` loop, not a single `if`; one left move may not be enough to restore both constraints.
- Update the answer only after the window has been restored to a valid state, or you will record illegal lengths.
- The production lesson is broader than this string problem: incremental state maintenance beats repeated full recomputation when constraints are local and stream-shaped.

## 🚀 Variations & Further Practice
- Require **exactly** `m` occurrences from `{A, T}` instead of at most `m`; the twist is that validity is no longer monotone in the same way, so standard shrinking logic needs refinement.
- Add a third bounded constraint, such as `count('G') + count('C') <= p`; this tests whether you can maintain multiple interacting window invariants without breaking linear time.
- Ask for the **number** of valid substrings instead of the longest one; the conceptual shift is from maximizing a window to counting all valid suffixes induced by each right boundary.