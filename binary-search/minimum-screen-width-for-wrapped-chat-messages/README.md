# Minimum Screen Width for Wrapped Chat Messages

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Strings, Greedy

---

## 🗂 Problem Overview
Given an array of chat messages and a limit `maxLines`, compute the smallest screen width `W` such that every message can be rendered in at most `maxLines` wrapped lines. Wrapping must preserve whole words, use single spaces between adjacent words on the same line, and never split a word. The challenge is that one width must work for all messages, and the search space is large enough that testing widths one by one is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a global capacity must satisfy many independent workloads: UI layout engines, log viewers, email preview panes, terminal renderers, PDF pagination, and notification systems. At scale, brute-forcing candidate widths or repeatedly reflowing text becomes expensive, especially when rendering is on the critical path. The important architectural idea is monotonic feasibility: once a width works, any larger width also works. That property enables binary search over configuration space instead of linear exploration, which is exactly the kind of shift that keeps latency predictable under large input sets and frequent recomputation.

## 🔍 Problem Statement
You are given `messages`, where each string contains lowercase words separated by single spaces, with no leading or trailing spaces, and an integer `maxLines`. A message is displayed using standard word wrapping: each line contains one or more complete words, adjacent words on the same line contribute one separating space, and words cannot be split.

Return the minimum integer width `W` such that **every** message can be displayed using at most `maxLines` lines under width `W`.

Constraints:
- `1 <= messages.length <= 100000`
- `1 <= messages[i].length <= 100000`
- Total length across all messages `<= 200000`
- `1 <= maxLines <= 100000`

Examples:
- `messages = ["chat app design", "binary search works"], maxLines = 2` → `12`
- `messages = ["a bb ccc", "dddd e"], maxLines = 3` → `4`

The key algorithmic constraint is the large input size combined with a numeric answer space, which strongly suggests binary search on width plus a linear feasibility check.

## 🪜 How to Solve This
1. Start from the output: we need the **minimum width**, not the wrapped layout itself. That usually means “search over answers.”
2. Ask whether feasibility is monotonic: if width `W` works for all messages, does any width `W+1` also work? Yes. Extra width can only keep the same line count or reduce it.
3. Once monotonicity is clear, binary search becomes the natural outer strategy.
4. Define a checker for a candidate width:
   - Scan each message greedily from left to right.
   - Pack as many whole words as possible into the current line.
   - Start a new line only when the next word would exceed `W`.
5. Why greedy works: for a fixed width, delaying a line break never increases the number of lines. Packing each line maximally minimizes line count for that width.
6. Establish bounds:
   - Lower bound = longest word across all messages.
   - Upper bound = longest full message length, since any message fits on one line at that width.
7. Binary search those bounds until the smallest feasible width is found.

## 🧩 Algorithm Walkthrough
1. **Precompute search bounds.**  
   Parse all messages to find:
   - `lo`: maximum word length across every message.
   - `hi`: maximum raw message length.  
   This is correct because no valid width can be smaller than the longest word, and any message trivially fits unwrapped at its own full length.

2. **Use the Binary Search on Answer pattern.**  
   While `lo < hi`, test `mid = lo + (hi - lo) / 2`. The invariant is:
   - every width `< lo` is known infeasible,
   - every width `>= hi` is known feasible.

3. **Feasibility check for one width.**  
   For each message, split into words and simulate wrapping greedily:
   - if the current line is empty, place the word,
   - otherwise add it if `currentLen + 1 + wordLen <= mid`,
   - else start a new line with that word and increment line count.  
   If any message exceeds `maxLines`, `mid` is infeasible.

4. **Why greedy is optimal.**  
   For fixed width, ending a line earlier than necessary cannot reduce the number of lines later. Maximally filling each line therefore yields the minimum possible line count for that message under that width.

5. **Shrink the search space.**  
   If `mid` is feasible, keep the left half by setting `hi = mid`; otherwise set `lo = mid + 1`. When the loop ends, `lo == hi` is the minimum feasible width.

This combines a monotonic predicate with a linear checker, which is the right abstraction because the optimization target is numeric and feasibility is ordered.

## 📊 Worked Example
Take `messages = ["chat app design", "binary search works"]`, `maxLines = 2`.

| Step | `lo` | `hi` | `mid` | Feasible? | Reason |
|---|---:|---:|---:|---|---|
| Init | 6 | 19 | — | — | longest word = `binary` = 6, longest message length = 19 |
| 1 | 6 | 19 | 12 | Yes | `"chat app" / "design"` and `"binary" / "search works"` |
| 2 | 6 | 12 | 9 | No | second message becomes `"binary" / "search" / "works"` |
| 3 | 10 | 12 | 11 | No | `"search works"` length is 12, so still 3 lines |
| 4 | 12 | 12 | — | Done | minimum feasible width found |

Checker trace at `W = 12` for `"binary search works"`:
1. Start line with `binary` → length 6  
2. `search` does not fit (`6 + 1 + 6 = 13`) → new line  
3. Add `works` to second line (`6 + 1 + 5 = 12`)  
Total lines = 2, within limit.

## ⏱ Complexity Analysis
### Time Complexity
Let `T` be the total number of characters across all messages. Each feasibility check scans all messages once, so it costs `O(T)`. Binary search runs over widths from longest word to longest message, giving `O(log M)` checks where `M` is max message length. Total complexity is `O(T log M)`, which remains practical even when input volume approaches millions of characters.

### Space Complexity
Extra space is `O(1)` beyond input storage if messages are scanned in place and words are parsed on the fly. If you pre-split all messages into word arrays, space rises to `O(T)`. That can simplify implementation, but it trades memory locality and footprint for convenience.

## 💡 Key Takeaways
• If the problem asks for the minimum numeric value that satisfies a condition, check whether feasibility is monotonic; that is the strongest signal for binary search on answer.  
• When a fixed capacity must work for many independent items, think “global threshold + per-item validator” rather than constructing the final layout directly.  
• The lower bound must be the longest individual word, not the shortest failing width observed during wrapping experiments.  
• Space accounting on a line is easy to get wrong: every added word after the first costs `1 + wordLen`, while the first word costs only `wordLen`.  
• At the system level, this is a reusable pattern for replacing expensive configuration sweeps with logarithmic search over a monotone feasibility function.

## 🚀 Variations & Further Practice
- Allow hyphenation or character-level splitting inside words. The monotonic search still holds, but the feasibility checker changes from word packing to finer-grained segmentation.
- Minimize the maximum number of lines across messages under a fixed width budget, or solve the dual problem: given width, compute the worst-case line count. The optimization target shifts, but the monotone predicate remains central.
- Add per-message formatting rules such as indentation, variable font widths, or forbidden breakpoints. The binary search survives, but the checker becomes a richer layout engine rather than simple greedy word packing.