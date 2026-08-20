# Minimum Energy to Decode a Beacon Stream

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, trie

---

## 🗂 Problem Overview
Given a string `s`, a reusable dictionary `patterns`, and a discount `d`, split `s` into dictionary words so the total decoding energy is minimized. Each block costs `len^2`, except consecutive blocks with the same starting letter give the later block a discount of `d`, floored at zero. Return the minimum total cost to decode all of `s`, or `-1` if no exact segmentation exists. The non-trivial part is that the cost of the current block depends on the previous block’s starting letter.

## 🌍 Engineering Impact
This pattern shows up in streaming parsers, packet/frame decoders, tokenization pipelines, and compiler front-ends where local decisions affect downstream cost. The structure is classic: segment a stream using a finite dictionary, but carry just enough state from the previous segment to price the next one correctly. At scale, naive substring checks and exponential backtracking collapse under long inputs and large vocabularies. A DP plus trie design turns an otherwise combinatorial search into a bounded left-to-right pass, enabling predictable latency, controllable memory, and straightforward production hardening around dictionary updates and throughput.

## 🔍 Problem Statement
You are given a lowercase string `s` of length up to `5000`, a list `patterns` of up to `2000` valid blocks, and an integer discount `d` where `0 <= d <= 2500`. Each pattern may be reused any number of times. You must partition `s` into a sequence of patterns whose concatenation is exactly `s`.

If the chosen blocks are `b1, b2, ..., bk`, then:
- `cost(b1) = len(b1)^2`
- for `i > 1`, if `bi` and `b(i-1)` start with the same letter,  
  `cost(bi) = max(0, len(bi)^2 - d)`, otherwise `len(bi)^2`

Return the minimum total energy, or `-1` if no exact partition exists.

Examples:
- `s = "ababa"`, `patterns = ["a", "ab", "ba"]`, `d = 2` → `5`
- `s = "cable"`, `patterns = ["ca", "ble", "cab"]`, `d = 3` → `-1`

The key constraint is `|s| = 5000`: too large for brute-force segmentation, but small enough for dynamic programming with efficient pattern matching.

## 🪜 How to Solve This
1. Read the problem → this is not just “can I segment the string?” but “what is the cheapest segmentation?” That immediately suggests dynamic programming over string positions.

2. Look at the discount rule → the current block’s cost depends only on one property of the previous block: its starting letter. That means the DP state does **not** need the full previous word, only a compact summary.

3. Define the state → let `dp[i][c]` be the minimum energy to decode prefix `s[0:i]` where the last chosen block starts with letter `c`. Also track a special “no previous block” case for the start.

4. Generate transitions efficiently → from each position `i`, enumerate all dictionary patterns that match `s[i:]`. Doing this by scanning every pattern each time is wasteful; a trie lets you walk forward through `s` once per start position and discover all matching patterns.

5. Update the next state → for every matched block, compute its base cost `len^2`, apply the discount only if its starting letter equals the previous state letter, and relax the DP at the ending position.

That chain gets you from exponential search to bounded DP with fast prefix matching.

## 🧩 Algorithm Walkthrough
1. **Build a trie from `patterns`.**  
   Each root-to-terminal path represents a valid block. This is the right abstraction because every transition starts at some index `i` and asks: “which patterns match `s[i:]`?” A trie answers that in one forward walk instead of `O(#patterns)` substring checks.

2. **Define DP state as `dp[pos][prevStart]`.**  
   `pos` is the next unread index in `s`. `prevStart` is the starting letter of the previously chosen block, or a sentinel for “no previous block yet.” This is correct because the future cost depends only on where we are and whether the next block’s first letter matches the previous block’s first letter.

3. **Initialize the base case.**  
   Set `dp[0][NONE] = 0`; all other states start at infinity. Invariant: every finite state represents an exact decoding of `s[0:pos]`.

4. **Enumerate outgoing transitions from each reachable position.**  
   Starting at `pos`, walk the trie along `s[pos], s[pos+1], ...` until the path breaks. Every terminal trie node yields a valid block ending at `nextPos`. Its starting letter is simply `s[pos]`.

5. **Compute transition cost.**  
   Let `L = nextPos - pos`, `base = L * L`. If `prevStart == s[pos]`, use `max(0, base - d)`; otherwise use `base`. Relax `dp[nextPos][currStart]`.

6. **Maintain the invariant.**  
   After processing all transitions from all reachable states at `pos`, every finite value in later positions is the minimum known cost for an exact segmentation ending with that starting letter. Since all transitions move forward, standard left-to-right DP is sufficient.

7. **Extract the answer.**  
   Take the minimum over all `dp[n][*]`. If all are infinite, return `-1`.

This is a **Dynamic Programming + Trie** solution: DP handles optimal substructure; the trie removes repeated dictionary scans.

## 📊 Worked Example
Use `s = "ababa"`, `patterns = ["a", "ab", "ba"]`, `d = 2`.

Let state be `(pos, prevStart) -> cost`.

| Step | From state | Matched block | To state | Added cost | New total |
|---|---|---|---|---:|---:|
| 1 | `(0, NONE)=0` | `"a"` | `(1, 'a')` | `1` | `1` |
| 2 | `(0, NONE)=0` | `"ab"` | `(2, 'a')` | `4` | `4` |
| 3 | `(1, 'a')=1` | `"ba"` | `(3, 'b')` | `4` | `5` |
| 4 | `(2, 'a')=4` | `"ba"` | `(4, 'b')` | `4` | `8` |
| 5 | `(3, 'b')=5` | `"ba"` | `(5, 'b')` | `max(0, 4-2)=2` | `7` |
| 6 | `(4, 'b')=8` | `"a"` | `(5, 'a')` | `1` | `9` |
| 7 | `(2, 'a')=4` | later path via `"ba"` then `"a"` improves end | `(5, 'a')` | `+1` after different start | `5` |

Minimum over end states at position `5` is `5`.

## ⏱ Complexity Analysis

### Time Complexity
Building the trie costs `O(sum(pattern lengths))`. The DP explores each string position and walks forward through at most the maximum pattern length, so the dominant work is `O(n * A * L)`, where `A = 27` DP states per position and `L <= 50`. With fixed alphabet/state size, this is effectively `O(nL)`, which is practical at `n = 5000` but the difference is decisive once repeated scans approach `10^6`–`10^9` checks.

### Space Complexity
The trie uses `O(sum(pattern lengths))`, and the DP table uses `O(n * A)` with `A = 27`. Space is therefore linear in input size. You can compress DP by processing only reachable states per position, but the constant-state table is usually simpler and already cheap.

## 💡 Key Takeaways
- If segmentation cost depends on a small summary of the previous segment, extend the DP state with that summary instead of carrying the full history.
- If every DP transition asks “which dictionary words match here?”, that is a strong signal for trie-backed matching rather than repeated pattern scans.
- Be careful with the first block: it has no previous block, so the discount must not apply from an uninitialized state.
- The discount compares **starting letters of consecutive blocks**, not ending letters and not full-string equality; this is the easiest rule to misread.
- In production systems, this is the standard move when local context affects scoring: keep the context state minimal, and make candidate generation sublinear with an index structure.

## 🚀 Variations & Further Practice
- Allow discounts to depend on the full previous pattern, not just its first letter. The conceptual twist is state explosion: DP now needs pattern identity or a compressed automaton state.
- Replace the static dictionary with online pattern updates. The harder part is maintaining fast matching under mutation, pushing you toward incremental trie structures or automata rebuild strategies.
- Add a cap on the number of blocks used. This turns the problem into multi-dimensional DP over position, previous-start state, and segment count.