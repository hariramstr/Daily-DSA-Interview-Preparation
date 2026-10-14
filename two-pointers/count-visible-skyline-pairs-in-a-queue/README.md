# Count Visible Skyline Pairs in a Queue

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Monotonic Stack, Array

---

## 🗂 Problem Overview
Given an array `heights`, count how many index pairs `(i, j)` with `i < j` are mutually visible. Two buildings are visible to each other if every building between them has height `<= min(heights[i], heights[j])`. Return the total as a 64-bit integer. The challenge is scale: `n` can reach `200000`, so checking all pairs or scanning between endpoints is infeasible. The solution must compress visibility boundaries into a linear-time pass.

## 🌍 Engineering Impact
This pattern shows up anywhere pairwise relationships depend on nearest dominating boundaries rather than full-range scans: market microstructure analysis, skyline/occlusion engines, telemetry peak detection, compiler dominance-like reductions, and stream processors computing local visibility or influence windows. At production scale, naive pair enumeration explodes quadratically in both latency and cost. A monotonic boundary-compression approach turns “compare against everything behind me” into “compare against the only candidates that still matter.” That shift is architectural, not cosmetic: it enables online processing, predictable memory behavior, and throughput that survives bursty or million-element inputs.

## 🔍 Problem Statement
You are given an integer array `heights` of length `n`, where `1 <= n <= 200000` and `1 <= heights[i] <= 10^9`. For every pair `(i, j)` with `i < j`, buildings `i` and `j` are mutually visible if every building strictly between them has height less than or equal to `min(heights[i], heights[j])`. Adjacent buildings are always visible because no buildings lie between them.

Return the total number of visible pairs as a 64-bit integer.

Examples:

- `heights = [5, 3, 4, 2, 6]` → `7`
- `heights = [4, 4, 4]` → `3`

Equal heights matter: they can still see each other if no intermediate building exceeds that shared height. The key constraint is `n = 200000`; any `O(n^2)` pair-checking strategy is too slow, so the algorithm must exploit monotonic structure and process each element a constant number of times.

## 🪜 How to Solve This
1. Read the visibility rule → the shorter endpoint controls whether a pair is valid. That means intermediate buildings only matter if they are taller than some active candidate.
2. Reframe the problem → when scanning left to right, most earlier buildings stop mattering once a taller building appears in front of them. Their future visibility is blocked.
3. That suggests a monotonic structure → keep only buildings that are still “exposed” to future elements. A decreasing stack does exactly that.
4. Handle smaller heights first → if the current building is taller than stack-top buildings, it can see them, and they can never block anything again, so pop them.
5. Equal heights need compression → a run of equal heights behaves as one height level with multiplicity. Store `(height, count)` so equal values contribute multiple pairs without repeated stack entries.
6. After popping smaller heights, if the top has the same height, add its count. If there is a taller building behind that equal-height group, add one more visible pair.
7. If the top is strictly taller, add one pair with that nearest taller building.
8. Each building is pushed once and popped once → linear time.

## 🧩 Algorithm Walkthrough
1. **Use a monotonic decreasing stack of `(height, count)`**.  
   The stack represents buildings to the left that are still potentially visible to future buildings. Heights decrease from bottom to top. Equal heights are merged into one entry with multiplicity.

2. **Process buildings left to right**.  
   For each `h`, initialize `same = 1`. This `same` will absorb an equal-height group if one exists on top of the stack.

3. **Pop all strictly smaller heights**.  
   While `stack.top.height < h`, the current building can see every building in that top group, so add `stack.top.count` to the answer and pop it.  
   **Why correct:** a taller current building is the first blocker for those smaller buildings; once seen now, they will never be relevant again.  
   **Invariant maintained:** remaining stack heights are all `>= h`.

4. **Handle equal height on top**.  
   If `stack.top.height == h`, add `stack.top.count` because the current building sees all equal-height buildings in that group. Set `same += stack.top.count` and pop the group.  
   If the stack is still non-empty afterward, add `1` for the nearest taller building behind the equal group.  
   **Why:** equal heights do not block each other, but a taller building behind them is still visible through the equal-height plateau.

5. **Handle strictly taller top**.  
   If no equal group exists but the stack is non-empty, add `1`. The current building sees exactly the nearest taller surviving building. Anything farther is blocked by that taller one.

6. **Push `(h, same)`**.  
   This preserves the decreasing-stack invariant and compresses equal runs.

This is the standard **Monotonic Stack** formulation of a boundary-compression problem. It is often described as “two-pointers style” because it avoids revisiting interior ranges, but the stack is the right abstraction: it stores exactly the unresolved left boundary candidates.

## 📊 Worked Example
Example: `heights = [5, 3, 4, 2, 6]`

| Step | h | Stack Before | Action | Pairs Added | Stack After | Total |
|---|---:|---|---|---:|---|---:|
| 1 | 5 | `[]` | push `(5,1)` | 0 | `[(5,1)]` | 0 |
| 2 | 3 | `[(5,1)]` | taller top visible | 1 | `[(5,1),(3,1)]` | 1 |
| 3 | 4 | `[(5,1),(3,1)]` | pop `3`, then see `5` | 2 | `[(5,1),(4,1)]` | 3 |
| 4 | 2 | `[(5,1),(4,1)]` | taller top visible | 1 | `[(5,1),(4,1),(2,1)]` | 4 |
| 5 | 6 | `[(5,1),(4,1),(2,1)]` | pop `2`, pop `4`, pop `5` | 3 | `[(6,1)]` | 7 |

Visible pairs counted: `(0,1), (1,2), (0,2), (2,3), (3,4), (2,4), (0,4)`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each building is pushed onto the stack once and popped at most once, so the total number of stack operations is linear. For `10^6` elements this remains practical; for `10^9`, even linear time becomes infrastructure-bound, but the algorithm is still asymptotically optimal for a single pass.

### Space Complexity
`O(n)` in the worst case, owned by the monotonic stack when heights are strictly decreasing. Equal-height compression reduces constant factors but not the worst-case bound. Reducing below `O(n)` would require sacrificing exactness or changing the problem model.

## 💡 Key Takeaways
• If a problem asks for pair visibility under “everything in between must stay below a threshold,” look for a monotonic stack that preserves only unresolved boundary candidates.  
• If earlier elements become permanently irrelevant after a larger element arrives, that is a strong signal for boundary compression rather than nested scans.  
• Equal heights are the main trap: they must be grouped with a count, or you will undercount equal-pair visibility and overcomplicate blocking logic.  
• After consuming an equal-height group, remember the possible extra `+1` for the next taller building behind it; missing that case is a classic off-by-one error.  
• At scale, the real win is not just better Big-O; it is converting repeated range validation into a streaming state machine with bounded per-element work.

## 🚀 Variations & Further Practice
- Count visible pairs in a **circular** skyline. The twist is that blocking wraps around, so you need duplicated traversal or careful pivot selection around global maxima.
- Return, for each building, the number of buildings it can see to the left and right. The twist is directional decomposition and combining two monotonic passes without double-counting.
- Extend visibility to 2D terrain or histogram cells. The twist is that monotonic one-dimensional dominance no longer fully captures occlusion, so interval or geometric structures become necessary.