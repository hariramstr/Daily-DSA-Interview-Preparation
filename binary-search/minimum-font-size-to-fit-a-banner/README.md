# Minimum Font Size to Fit a Banner

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Simulation

---

## 🗂 Problem Overview
Given an ordered list of words and a banner of width `W` and height `H`, find the smallest positive integer font size `s` that allows the full message to fit. At size `s`, every character and every inter-word space consumes `s` width, and each rendered line consumes `s` height. Wrapping is allowed only between words. The challenge is that feasibility depends on both horizontal packing and vertical line count, and the search space for `s` is large.

## 🌍 Engineering Impact
This pattern shows up in layout engines, PDF/rendering pipelines, subtitle placement, dashboard widgets, and ad-tech creative fitting. The core issue is not text rendering itself; it is decision search over a monotonic feasibility function under strict resource limits. At scale, brute-force sizing or repeated full re-layout across many candidates becomes expensive, especially when banners are generated per request or per device profile. A monotonic search turns an unbounded tuning problem into a predictable control loop: fast enough for online serving, simple enough to reason about, and robust under large input ranges and skewed content distributions.

## 🔍 Problem Statement
You are given `words`, `W`, and `H`. For an integer font size `s > 0`, each word of length `L` occupies `L * s` width, each space between adjacent words on the same line occupies `s` width, and each line consumes `s` height. Words must remain in order and cannot be split. A layout is valid if every line width is at most `W` and total height is at most `H`.

Return the minimum integer `s` such that the entire message fits, or `-1` if no positive size works.

Constraints:
- `1 <= words.length <= 10^5`
- `1 <= words[i].length <= 10^5`
- Sum of word lengths `<= 2 * 10^5`
- `1 <= W, H <= 10^9`

Examples:
- `words = ["team","sync","works"], W = 20, H = 8` → `2`
- `words = ["a","bb","ccc"], W = 6, H = 6` → `1`

The key algorithmic signal is monotonic feasibility across a very large integer answer space.

## 🪜 How to Solve This
1. Read the problem → this is not asking for the layout itself first; it asks for the **minimum size** that satisfies a yes/no condition.

2. For any fixed font size `s`, the question becomes: **can I greedily place words line by line without exceeding width, and finish within the available number of lines?**  
   Available lines = `H / s`.

3. That feasibility check is straightforward:
   - If any single word width `len(word) * s` exceeds `W`, fail immediately.
   - Otherwise, pack words greedily onto the current line.
   - If the next word plus one required space does not fit, start a new line.

4. Why greedy works → once word order is fixed and words cannot be split, delaying a wrap never hurts. Packing each line as much as possible minimizes the number of lines used.

5. Now notice monotonicity:
   - If size `s` fits, every smaller size also fits.
   - If size `s` does not fit, every larger size also fails.

6. Monotonic yes/no over integers → binary search the answer.  
   Search `s` in `[1, min(W, H)]`, because a positive size larger than either dimension cannot fit even one line safely.

## 🧩 Algorithm Walkthrough
1. **Define the pattern: binary search on answer + greedy simulation.**  
   The answer is an integer size, and feasibility is monotonic. That makes binary search the right abstraction; the layout check is the decision oracle.

2. **Establish the search bounds.**  
   Lower bound is `1`. Upper bound can be `min(W, H)`: a line height of `s` must fit inside `H`, and even a one-character word needs width `s <= W`. This keeps the search finite and tight.

3. **Implement `canFit(s)`.**  
   Compute `maxLines = H / s`. If `maxLines == 0`, return false. Then scan words in order, maintaining:
   - `linesUsed`
   - `currentLineWidth`  
   For each word, compute `wordWidth = len(word) * s`. If `wordWidth > W`, fail immediately.

4. **Greedily place each word.**  
   If the current line is empty, place the word directly. Otherwise, try `currentLineWidth + s + wordWidth` to account for the mandatory space. If it fits, extend the line; if not, start a new line with that word.

5. **Maintain the invariant.**  
   After processing each word, the current partial layout uses the minimum possible number of lines for the processed prefix. This is why the greedy simulation is correct.

6. **Binary search for the minimum feasible size.**  
   Standard lower-bound search:
   - If `canFit(mid)` is true, record `mid` and search left.
   - Otherwise search right.  
   Return the best recorded size, or `-1` if even `1` fails.

## 📊 Worked Example
Use `words = ["team","sync","works"]`, `W = 20`, `H = 8`.

Check `s = 2`:
- `maxLines = 8 / 2 = 4`
- Word widths: `team=8`, `sync=8`, `works=10`

| Step | Word   | Current Line Width | Action                  | Lines Used |
|------|--------|--------------------|-------------------------|------------|
| 1    | team   | 0                  | place on line 1         | 1          |
| 2    | sync   | 8                  | `8 + 2 + 8 = 18` fits   | 1          |
| 3    | works  | 18                 | `18 + 2 + 10 = 30` no   | 2          |

Result: 2 lines used, within `maxLines = 4`, so `s = 2` fits.

Check `s = 3`:
- `maxLines = 8 / 3 = 2`
- Widths: `12, 12, 15`
- `team` on line 1, `sync` must wrap to line 2, `works` must wrap to line 3  
That exceeds available lines, so `s = 3` fails.

Minimum fitting size is `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log U)`, where `n` is the number of words and `U = min(W, H)`. Each binary-search step runs one linear feasibility scan. With `n = 10^5` and `log U <= 30`, this is comfortably practical. A linear scan over `10^9` candidate sizes would be impossible; the logarithmic search is the entire win.

### Space Complexity
`O(1)` auxiliary space beyond the input. The feasibility check keeps only counters and current line state. You could precompute word lengths, but that only trades a small constant-factor speedup for `O(n)` extra memory.

## 💡 Key Takeaways
- If the problem asks for the minimum or maximum integer satisfying a yes/no condition, check whether feasibility is monotonic; that is the strongest signal for binary search on answer.
- When order is fixed and splitting is forbidden, “pack as much as possible before wrapping” is a strong signal that a greedy simulation may be optimal.
- Be careful with the objective: this problem asks for the **minimum** fitting size, not the maximum readable size, which is the more common variant.
- The width test for a non-empty line must include exactly one extra `s` for the inter-word space; missing that is the most common off-by-one-style bug here.
- In production systems, separating a monotonic decision oracle from the search strategy gives you a reusable design: the simulator encodes business rules, and the search layer optimizes under large parameter ranges.

## 🚀 Variations & Further Practice
- **Maximum font size that fits**: same feasibility oracle, but now search for the largest valid `s`; the conceptual twist is changing lower-bound to upper-bound binary search.
- **Variable-width glyphs and kerning pairs**: each character or pair has different width, so the simulation remains greedy but the width model becomes data-driven and harder to cache efficiently.
- **Justified text with a line limit**: instead of simple wrapping, distribute spaces or minimize raggedness under a fixed number of lines; this breaks the pure greedy structure and pushes toward DP or cost-based optimization.