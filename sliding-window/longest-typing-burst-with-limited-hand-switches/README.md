# Longest Typing Burst With Limited Hand Switches

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, String

---

## 🗂 Problem Overview
Given a lowercase string `s`, an integer `k`, and a 26-character `handMap`, find the maximum length of a contiguous substring whose adjacent characters cause at most `k` hand switches. Each letter maps to either left or right hand, and only transitions between neighboring characters inside the substring count. The challenge is scale: with `s.length` up to `2 * 10^5`, enumerating all substrings is infeasible, so the solution must maintain validity incrementally.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maximize a contiguous interval under a bounded transition budget. Examples include streaming pipelines limiting schema changes across adjacent records, search ranking windows constrained by category flips, network telemetry segments with bounded protocol transitions, and user-session analytics detecting stable behavior bursts. At production scale, brute-force interval scans collapse under quadratic growth and destroy latency budgets. A sliding-window design enables single-pass processing, predictable memory, and composability with online ingestion, which matters when the input is a live stream rather than a static array.

## 🔍 Problem Statement
You are given:

- `s`: a string of lowercase English letters
- `k`: maximum allowed number of hand switches inside a chosen substring
- `handMap`: a length-26 string where `handMap[i]` is `'L'` or `'R'`, defining which hand types `('a' + i)`

A substring is valid if the number of adjacent positions where the hand changes is at most `k`. Return the length of the longest valid substring.

Key constraints:

- `1 <= s.length <= 2 * 10^5`
- `0 <= k < s.length`
- `handMap.length == 26`

Examples:

- `s = "abacabad"`, `k = 2`, `handMap = "LLRLRRLLRLRLRLRLRLRLRLRLRL"` → `5`
- `s = "zzxyyx"`, `k = 1`, `handMap = "LRLRLRLRLRLRLRLRLRLRLRLRLR"` → `4`

The decisive constraint is input size: any `O(n^2)` substring scan is too slow, so the algorithm must update the answer while moving through the string once.

## 🪜 How to Solve This
1. Read the problem → the validity of a substring depends only on adjacent pairs inside it, not on global counts of characters.
2. That means extending a window by one character changes the switch count in only one place: the new boundary between `s[right - 1]` and `s[right]`.
3. Shrinking from the left is similarly local: when `left` moves forward, only the old boundary between `s[left]` and `s[left + 1]` leaves the window.
4. Local add/remove effects are a strong signal for **Sliding Window / Two Pointers**.
5. Precompute or derive each character’s hand in `O(1)` using `handMap`.
6. Expand `right`, add the new boundary’s contribution, and if switches exceed `k`, advance `left` until the window is valid again.
7. After every valid expansion, update the best length.

The key realization is that we are not counting something over all characters in the window; we are counting over the `window_length - 1` adjacent edges. Once you model the problem as “maintain a budget of bad edges,” the linear solution becomes straightforward.

## 🧩 Algorithm Walkthrough
1. **Map characters to hands.**  
   For any character `c`, compute its hand as `handMap[c - 'a']`. This gives constant-time access to whether a position is typed with `'L'` or `'R'`.

2. **Initialize a sliding window.**  
   Use two pointers: `left = 0`, and iterate `right` from `0` to `n - 1`. Maintain `switches`, the number of hand changes across adjacent pairs fully inside `s[left..right]`.

3. **Expand the window by moving `right`.**  
   When `right > 0`, inspect the new adjacent pair `(right - 1, right)`. If their hands differ, increment `switches`.  
   **Why correct:** this is the only new edge introduced by extending the window.

4. **Restore validity by moving `left`.**  
   While `switches > k`, remove the contribution of the edge `(left, left + 1)` before incrementing `left`. If those two hands differ, decrement `switches`.  
   **Invariant:** after adjustment, `switches` equals the exact number of hand-switch edges inside the current window.

5. **Record the best answer.**  
   Once the window is valid, update `best = max(best, right - left + 1)`.  
   **Why this works:** for each `right`, the maintained `left` is the smallest valid boundary after necessary shrinking, so the current valid window is the longest one ending at `right`.

6. **Return `best`.**  
   This is the standard **Two Pointers / Sliding Window** pattern: grow optimistically, shrink only when constraints are violated, and rely on monotonic pointer movement for linear time.

## 📊 Worked Example
Example: `s = "zzxyyx"`, `k = 1`  
Assume `z -> R`, `x -> R`, `y -> L`, so hand sequence is `R R R L L R`.

| right | char | new edge added | switches | left after shrink | window | best |
|---|---:|---|---:|---:|---|---:|
| 0 | z | — | 0 | 0 | `z` | 1 |
| 1 | z | `R->R` no | 0 | 0 | `zz` | 2 |
| 2 | x | `R->R` no | 0 | 0 | `zzx` | 3 |
| 3 | y | `R->L` yes | 1 | 0 | `zzxy` | 4 |
| 4 | y | `L->L` no | 1 | 0 | `zzxyy` | 5 |
| 5 | x | `L->R` yes | 2 | shrink to 3 | `yyx` | 5 |

At `right = 5`, the window exceeds the switch budget. Advancing `left` removes edges `(0,1)`, `(1,2)`, then `(2,3)`, and only the last removal decreases `switches` from 2 to 1. The longest valid burst found is length `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = s.length`. Each pointer moves monotonically from left to right, and every adjacent edge is added once and removed at most once. At `10^6` elements this remains practical in a single pass; at `10^9`, the algorithm is still asymptotically optimal but runtime becomes dominated by raw scan cost and I/O.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores a few integers and performs constant-time hand lookups from the fixed-size `handMap`. Space cannot meaningfully be reduced further unless you trade readability for direct inline character comparisons.

## 💡 Key Takeaways
- If a substring constraint depends on adjacent transitions rather than full-window frequency counts, think in terms of maintaining edge contributions inside a sliding window.
- When adding or removing one element changes validity only locally, Two Pointers is usually the right abstraction.
- The switch count belongs to edges between characters, not to characters themselves; window length is `right - left + 1`, but tracked transitions are one fewer.
- On left-shrink, remove the edge `(left, left + 1)` before incrementing `left`; reversing that order produces subtle off-by-one bugs.
- The production-grade insight is to model bounded instability as a budget over local state transitions, which enables streaming, single-pass enforcement instead of expensive interval recomputation.

## 🚀 Variations & Further Practice
- Allow up to `k` switches **per hand type** or weighted switch costs; the twist is that validity is no longer a single scalar budget.
- Return the actual substring and break ties lexicographically or by earliest start; the twist is preserving deterministic selection while maintaining linear time.
- Generalize from binary hands to arbitrary categories and ask for the longest window with at most `k` category transitions; same pattern, but the abstraction becomes “bounded adjacent state changes” rather than keyboard-specific logic.