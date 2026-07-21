# Maximum Visible Booths Between Taller Buildings

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Monotonic Stack, Nearest Greater Element

---

## 🗂 Problem Overview
Given an array `heights`, place a booth on any building `i` and ask whether it is enclosed by a strictly taller building on both sides. If so, its visibility score is the span between those nearest taller boundaries: `R - L - 1`. If either boundary is missing, the score is `0`. Return the maximum score across all buildings. The challenge is scale: with up to `200000` buildings, per-index left/right scanning is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs nearest-dominating context on both sides of an event or entity. Examples include skyline occlusion in rendering pipelines, nearest stronger signal windows in telemetry streams, price-bar containment in trading systems, and dominance boundaries in compiler or query-planning passes. At scale, the difference between `O(n^2)` scans and `O(n)` monotonic-stack passes is the difference between interactive latency and batch-only feasibility. The approach also generalizes cleanly to streaming-friendly preprocessing, bounded-memory scans, and reusable “nearest greater/smaller” primitives in array-heavy services.

## 🔍 Problem Statement
You are given an integer array `heights` where `heights[i]` is the height of the `i`-th building in a line. For each building `i`, define:

- `L`: the closest index `< i` such that `heights[L] > heights[i]`
- `R`: the closest index `> i` such that `heights[R] > heights[i]`

If both exist, the building is enclosed and its score is `R - L - 1`. Otherwise, its score is `0`. Return the maximum score over all indices.

Constraints:
- `1 <= heights.length <= 200000`
- `1 <= heights[i] <= 1000000000`

Examples:
- `heights = [5, 2, 4, 3, 6]` → output `3`
- `heights = [7, 1, 5, 2, 4, 8]` → output `4`

Equal heights do **not** count as taller. The key constraint is input size: any nested scanning approach is unacceptable.

## 🪜 How to Solve This
1. Read the definition carefully → each score depends on the **nearest strictly greater** value on the left and on the right.
2. Nearest-greater queries across an array are a standard signal for a **monotonic stack**. Why? Because we need local dominance, not global maxima.
3. Brute force would scan left and right from every index → `O(n^2)` in the worst case, which fails at `200000`.
4. Instead, compute nearest taller on the left in one left-to-right pass:
   - Maintain a stack of indices with heights in **strictly decreasing** order.
   - Pop anything `<= current`, because it cannot be the nearest taller for this or future shorter elements.
5. Compute nearest taller on the right symmetrically in one right-to-left pass.
6. Once `left[i]` and `right[i]` are known, the score formula is immediate:
   - both exist → `right[i] - left[i] - 1`
   - otherwise → `0`
7. Track the maximum while evaluating scores. Two linear passes plus one linear aggregation gives `O(n)` total.

## 🧩 Algorithm Walkthrough
1. **Initialize storage for boundaries.**  
   Create arrays `left` and `right`, filled with `-1`. `-1` means “no taller boundary exists on that side.” This gives a clean sentinel for later score computation.

2. **Left-to-right pass for nearest greater on the left.**  
   Use a **monotonic decreasing stack** of indices. For each index `i`, pop while `heights[stack.top] <= heights[i]`. After popping, the remaining top—if any—is the nearest index to the left with strictly greater height, so set `left[i] = stack.top`. Then push `i`.  
   **Invariant:** stack indices are ordered by position, and their heights are strictly decreasing from bottom to top.

3. **Right-to-left pass for nearest greater on the right.**  
   Clear the stack and repeat symmetrically from `n - 1` down to `0`. Pop while `heights[stack.top] <= heights[i]`. The remaining top is the nearest taller building on the right, so set `right[i] = stack.top`, then push `i`.  
   **Invariant:** same monotonic property holds relative to the reverse scan direction.

4. **Compute scores.**  
   For each `i`, if `left[i] != -1` and `right[i] != -1`, compute `right[i] - left[i] - 1`; otherwise use `0`. Take the maximum.

5. **Why this is correct.**  
   The stack removes all candidates that are not strictly taller or are blocked by a closer candidate. What survives on top is exactly the nearest valid greater element. This is the canonical nearest-greater-element application of a monotonic stack.

## 📊 Worked Example
Example: `heights = [5, 2, 4, 3, 6]`

| i | h | left pass stack after step | left[i] | right[i] | score |
|---|---|----------------------------|---------|----------|-------|
| 0 | 5 | [0]                        | -1      | 4        | 0     |
| 1 | 2 | [0,1]                      | 0       | 2        | 1     |
| 2 | 4 | [0,2]                      | 0       | 4        | 3     |
| 3 | 3 | [0,2,3]                    | 2       | 4        | 1     |
| 4 | 6 | [4]                        | -1      | -1       | 0     |

Right pass details:
- At index `4`, no taller building exists to the right.
- At index `3`, nearest taller right is `4`.
- At index `2`, nearest taller right is `4`.
- At index `1`, nearest taller right is `2`.
- At index `0`, nearest taller right is `4`.

Maximum score is at index `2`: `4 - 0 - 1 = 3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index is pushed onto the stack once and popped at most once in each pass, so the dominant work is linear. At `10^6` elements this remains practical; at `10^9`, even linear time becomes infrastructure-bound, but it is still the best asymptotic class available.

### Space Complexity
`O(n)`. The `left` and `right` arrays plus the stack dominate memory usage. You can reduce auxiliary storage by computing one side on the fly and folding score calculation into the second pass, but that trades simplicity for tighter implementation coupling.

## 💡 Key Takeaways
- If a problem asks for the **nearest strictly greater/smaller element** on one or both sides for every index, a monotonic stack should be your first candidate.
- When the output depends on **local enclosing boundaries**, not global extrema, sorting is usually the wrong abstraction; adjacency-aware linear scans are the right one.
- Use `<=` in the pop condition, not `<`, because equal heights are not valid taller boundaries and must be discarded.
- The score is `R - L - 1`, not `R - L`; the boundaries themselves are excluded from the visible span.
- At production scale, precomputing nearest-dominance structure in linear time is often the difference between a reusable primitive and a hotspot that forces sharding or offline processing.

## 🚀 Variations & Further Practice
- Return the score for **every** building, not just the maximum; same core pattern, but now boundary arrays become part of the output contract.
- Replace “strictly taller” with “taller or equal”; the conceptual twist is that the stack pop condition changes, which alters duplicate-handling semantics.
- Extend to **2D grids** or skyline visibility; the harder part is that nearest-dominance is no longer captured by a single linear ordering, so the 1D monotonic-stack trick does not transfer directly.