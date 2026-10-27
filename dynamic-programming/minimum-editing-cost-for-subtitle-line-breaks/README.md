# Minimum Editing Cost for Subtitle Line Breaks

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, String, Array

---

## 🗂 Problem Overview
Given an ordered array of transcript words and a maximum subtitle width, split the words into valid lines while preserving order. Each line may contain multiple words separated by single spaces, and its total length must not exceed `maxWidth`. Every non-final line incurs a penalty equal to the square of its unused width; the final line costs `0`. Return the minimum total penalty across all valid line-break placements. The challenge is combinatorial: every break decision changes the optimal cost of the remaining suffix.

## 🌍 Engineering Impact
This pattern appears anywhere content must be segmented under hard display constraints while optimizing a quality metric: subtitle rendering, document layout engines, e-book pagination, PDF generation, terminal UIs, chat message wrapping, and ad or notification formatting. At small scale, greedy wrapping looks acceptable; at production scale, it creates visibly poor layouts and inconsistent user experience. Dynamic programming matters because local choices do not compose: a line that looks efficient now can force expensive downstream breaks. The same decision model shows up in scheduling, packet framing, and batch construction where suffix cost determines the globally optimal split.

## 🔍 Problem Statement
You are given `words`, an array of `n` strings, and an integer `maxWidth`. Split the transcript into consecutive subtitle lines without reordering words. If words `i...j` are placed on one line, its used length is:

`sum(length(words[k])) for k in [i..j] + (j - i)`

The `(j - i)` term accounts for one space between adjacent words. A line is valid only if this total is `<= maxWidth`.

For every non-final line, cost is:

`(maxWidth - usedLength)^2`

The final line has cost `0` if it fits.

Return the minimum total cost over all valid splits.

Constraints:
- `1 <= words.length <= 500`
- `1 <= words[i].length <= maxWidth <= 1000`

Examples:
- `["AI","helps","teams","ship","faster"], maxWidth = 10`
- `["code","review","needs","clear","notes"], maxWidth = 12`

The key constraint is not input size alone; it is that each break choice affects the optimal formatting of the remaining suffix, which rules out simple greedy strategies.

## 🪜 How to Solve This
1. Read the problem → notice this is not about constructing one valid layout; it is about minimizing total cost across all valid layouts.

2. Focus on the first line → if you decide the first line ends at word `j`, then everything after `j` is the same problem on a smaller suffix.

3. That recursive structure is the signal for dynamic programming:
   - `dp[i]` = minimum cost to format words starting at index `i`.

4. For each `i`, try extending the current line word by word:
   - keep a running line length,
   - stop as soon as it exceeds `maxWidth`,
   - compute the line cost plus `dp[j + 1]`.

5. Handle the last line specially:
   - if `j` is the final word and the line fits, cost is `0`, not squared slack.

6. Take the minimum over all valid `j`.

This is the standard “optimal partition of a sequence” pattern. The reason it works is that once the first line is fixed, the remaining decision space is independent except for the starting index. That gives overlapping subproblems and a clean suffix DP.

## 🧩 Algorithm Walkthrough
1. **Define the DP state.**  
   Use the **Dynamic Programming** pattern for sequence partitioning. Let `dp[i]` be the minimum total cost to format the suffix starting at word `i`. The invariant: once `dp[i]` is computed, it is the globally optimal answer for that suffix.

2. **Initialize the base case.**  
   Set `dp[n] = 0`, where `n = words.length`. This means formatting an empty suffix costs nothing. It anchors transitions from the last valid line.

3. **Process indices from right to left.**  
   For each `i` from `n - 1` down to `0`, evaluate every valid line starting at `i`. Right-to-left order ensures `dp[j + 1]` is already known when considering words `i...j`.

4. **Expand the line incrementally.**  
   Maintain `lineLength` while extending `j` from `i` forward. Add `words[j].length`, plus one extra character for a space when `j > i`. If `lineLength > maxWidth`, break immediately. The invariant here is that every examined `(i, j)` is a valid single-line candidate.

5. **Compute transition cost.**  
   - If `j == n - 1`, this is the last line, so candidate cost is `0`.
   - Otherwise, candidate cost is `(maxWidth - lineLength)^2 + dp[j + 1]`.

6. **Take the minimum candidate.**  
   Assign `dp[i]` to the smallest candidate over all valid `j`. This is correct because every legal formatting of suffix `i` begins with exactly one valid first line `i...j`.

7. **Return `dp[0]`.**  
   That value is the minimum total editing cost for the full transcript.

This abstraction is the right fit because the problem is not local packing; it is optimal partitioning with a suffix-dependent objective.

## 📊 Worked Example
Use `words = ["code","review","needs","clear","notes"]`, `maxWidth = 12`.

| i | Valid first line(s) | Line length | Immediate cost | Next dp | dp[i] |
|---|---|---:|---:|---:|---:|
| 5 | empty suffix | 0 | 0 | — | 0 |
| 4 | `"notes"` | 5 | 0 (last line) | 0 | 0 |
| 3 | `"clear"` | 5 | 49 | 0 | 49 |
| 3 | `"clear notes"` | 11 | 0 (last line) | 0 | 0 |
| 2 | `"needs"` | 5 | 49 | 0 | 49 |
| 2 | `"needs clear"` | 11 | 1 | 0 | 1 |
| 1 | `"review"` | 6 | 36 | 1 | 37 |
| 0 | `"code"` | 4 | 64 | 37 | 101 |
| 0 | `"code review"` | 11 | 1 | 1 | 2 |

So `dp[0] = 2`, achieved by `["code review", "needs clear", "notes"]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)` in the worst case, where `n = words.length`. For each starting index, you may extend the line across many following words until width is exceeded. At `n = 500`, this is trivial. At `10^6` elements, quadratic work is infeasible; at `10^9`, it is completely out of scope without stronger structure.

### Space Complexity
`O(n)` for the DP array storing one value per suffix. The input dominates no additional asymptotic space beyond that. Space cannot be reduced below linear if you keep arbitrary suffix optima, though top-down memoization trades array simplicity for recursion overhead and stack depth.

## 💡 Key Takeaways
- If a problem asks for an optimal way to split an ordered sequence into contiguous groups, think sequence DP before considering greedy heuristics.
- If the cost of the current group combines with the optimal cost of the remaining suffix, the natural state is usually `dp[i]` over starting index `i`.
- The last line is a special case with cost `0`; forgetting that changes both transitions and final answers.
- Line length is `sum(word lengths) + number of gaps`, so spaces count as `(j - i)`, not `j - i + 1`.
- In production systems, this is the same design move as optimal batching: model local packing decisions with a suffix cost instead of assuming locally efficient choices stay globally efficient.

## 🚀 Variations & Further Practice
- **Text justification with exact spacing distribution:** harder because you must both choose line breaks and distribute extra spaces across gaps, not just score leftover width.
- **Word wrap with nonzero last-line penalty:** removes the special-case final line, which changes transitions and often exposes hidden assumptions in implementations.
- **Partition array for minimum cost with arbitrary segment cost function:** same DP skeleton, but the conceptual twist is deriving fast segment-cost evaluation instead of simple width tracking.