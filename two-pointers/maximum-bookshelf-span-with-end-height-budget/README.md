# Maximum Bookshelf Span With End-Height Budget

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Array, Greedy

---

## 🗂 Problem Overview
Given an array `heights` and an integer `budget`, choose two different indices `i < j` such that the endpoint height difference satisfies `|heights[i] - heights[j]| <= budget`. Among all valid pairs, maximize the score `(j - i) * min(heights[i], heights[j])`, returning the result as a 64-bit integer. The challenge is that the classic widest-endpoint intuition is constrained by a validity filter on endpoint heights, which invalidates a naive `O(n^2)` scan at `n = 200000`.

## 🌍 Engineering Impact
This pattern shows up in systems that maximize value over distant endpoints under compatibility constraints: matching replicas with bounded latency skew, pairing events in streaming pipelines under watermark tolerance, selecting search candidates under score-distance budgets, or joining telemetry windows where only endpoint metadata matters. At scale, brute-force pair evaluation explodes quadratically and quickly becomes operationally irrelevant. The useful abstraction is not “compare everything,” but “maintain the widest still-feasible frontier while proving discarded endpoints cannot participate in a better result.” That shift is what makes high-throughput matching, ranking, and bounded-join systems tractable.

## 🔍 Problem Statement
You are given:

- `heights`, where `heights[i]` is the height of the `i`-th bookshelf
- `budget`, the maximum allowed absolute difference between the two endpoint heights

For any pair `i < j`, the pair is **stable** if:

- `|heights[i] - heights[j]| <= budget`

Its score is:

- `(j - i) * min(heights[i], heights[j])`

Return the maximum score across all stable pairs. If no stable pair exists, return `0`.

Constraints:

- `2 <= heights.length <= 200000`
- `1 <= heights[i] <= 1000000000`
- `0 <= budget <= 1000000000`

Examples:

- `heights = [5, 1, 4, 6, 3], budget = 2` → `15`
- `heights = [9, 2, 8, 1, 7], budget = 0` → `0`

The decisive constraint is `n = 200000`: any nested-loop endpoint scan is too slow, so the algorithm must aggressively prune impossible winners.

## 🪜 How to Solve This
1. Start from the classic max-area mental model: only the two endpoints matter, and width shrinks as pointers move inward.
2. Then notice the twist: not every pair is legal. The budget adds a validity gate on endpoint heights, so “evaluate current pair and move the shorter side” is no longer sufficient by itself.
3. Still, width only decreases over time, so the only hope of improving the answer after rejecting a pair is to find a taller or more compatible endpoint before width collapses too much.
4. That suggests a **two-pointer scan** from both ends, because it always considers the maximum remaining width first.
5. If the current pair is valid, compute its score. Then move the shorter endpoint: keeping it while reducing width cannot improve `min(heightLeft, heightRight)`.
6. If the pair is invalid, move the taller endpoint. Why? The height gap is too large, and only reducing the taller side can potentially restore feasibility without immediately worsening the bottleneck height.
7. This gives a greedy pruning rule: every move discards an endpoint that cannot be part of a better solution with any interior partner still under consideration.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers**: `left = 0`, `right = n - 1`, and `best = 0`.  
   This sets up the widest possible candidate pair first, which is the core reason Two Pointers is the right abstraction here.

2. **Inspect the endpoint heights**: let `hl = heights[left]`, `hr = heights[right]`.  
   Every decision depends only on these two values because the score uses only width and the smaller endpoint height.

3. **Check budget feasibility**: if `abs(hl - hr) <= budget`, the pair is stable.  
   Compute `score = (right - left) * min(hl, hr)` using 64-bit arithmetic and update `best`.

4. **If the pair is valid, move the shorter side**.  
   Suppose `hl <= hr`. Any future pair using the same `left` but a smaller `right` has strictly smaller width and bottleneck at most `hl`, so it cannot beat the current valid score. Symmetrically for the right side.

5. **If the pair is invalid, move the taller side**.  
   If `hl > hr + budget`, the left endpoint is too tall relative to the right. Moving `right` inward cannot increase `hr` and only shrinks width, so keeping `left` against interior rights cannot repair the violation in a useful way; move `left`. Symmetrically, if `hr > hl + budget`, move `right`.

6. **Maintain the invariant**: after each move, all discarded endpoint configurations are dominated by an already-checked or strictly narrower configuration.  
   That is the greedy pruning argument that keeps the scan linear.

7. **Stop when pointers cross** and return `best`.  
   If no stable pair was ever found, `best` remains `0`.

## 📊 Worked Example
Use `heights = [5, 1, 4, 6, 3]`, `budget = 2`.

| Step | left | right | hl | hr | `|hl-hr|<=2` | score | action |
|---|---:|---:|---:|---:|---:|---:|---|
| 1 | 0 | 4 | 5 | 3 | yes | `4 * 3 = 12` | move `right` (shorter) |
| 2 | 0 | 3 | 5 | 6 | yes | `3 * 5 = 15` | move `left` (shorter) |
| 3 | 1 | 3 | 1 | 6 | no | — | move `right` (taller) |
| 4 | 1 | 2 | 1 | 4 | no | — | move `right` (taller) |

Pointers cross after step 4. The best stable score seen is `15`, from pair `(0, 3)`.

The important behavior is visible here: valid pairs use the classic “discard the shorter wall” rule, while invalid pairs use the budget violation direction to discard the endpoint causing the excessive height gap.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each iteration moves exactly one pointer inward, and each index is visited at most once from each side. There is no sorting, heap maintenance, or nested scan. At `10^6` elements this remains practical; at `10^9`, even linear scans become infrastructure problems rather than algorithm problems.

### Space Complexity
`O(1)`. The algorithm stores only two pointers and the running best score. No auxiliary arrays or index structures are required. You could add tracing or candidate caching for debugging, but that increases memory without improving asymptotic runtime.

## 💡 Key Takeaways
- If the objective depends only on two endpoints and width shrinks monotonically, start by testing whether a two-pointer dominance argument exists.
- When a classic endpoint problem adds a validity constraint, look for a pointer-move rule that removes only endpoints that are provably dominated.
- Use 64-bit multiplication for `(right - left) * min(...)`; 32-bit overflow is easy here.
- Be precise about the invalid-pair move: move the endpoint that is too tall relative to the other, not blindly the shorter side.
- In production systems, the transferable idea is constrained frontier pruning: discard candidates only when you can prove no future narrower configuration can outperform them.

## 🚀 Variations & Further Practice
- Add a requirement that the endpoint heights differ by **at least** `budget` instead of at most `budget`; the pruning direction changes because feasibility now improves when endpoints diverge.
- Return the top `k` stable pairs by score rather than just the maximum; now a single greedy frontier is insufficient and you need structured candidate exploration.
- Allow online updates to `heights` with repeated max-span queries; the hard part becomes preserving endpoint-dominance information under mutation, pushing toward segment trees or indexed search structures.