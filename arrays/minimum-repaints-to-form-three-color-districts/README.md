# Minimum Repaints to Form Three Color Districts

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Prefix Sum

---

## 🗂 Problem Overview
Given a string `colors` of length `n`, repaint the fewest buildings so the entire row becomes exactly three contiguous non-empty districts: all `R`, then all `G`, then all `B`. You may repaint any building to any color at unit cost. The output is the minimum total repaint count. The challenge is that both split points are unknown, and checking all `O(n^2)` district boundaries is too slow for `n` up to `200000`.

## 🌍 Engineering Impact
This pattern shows up whenever a sequence must be partitioned into ordered phases with minimal correction cost: log pipelines split into ingest/transform/serve stages, genome or signal segmentation into labeled regions, compiler token streams forced into valid state transitions, or ranking pipelines enforcing monotone business rules across buckets. At small scale, brute-force boundary enumeration works; at production scale, it collapses under quadratic scans and cache-unfriendly recomputation. Prefix-cost and DP formulations turn “search over all boundaries” into linear-time optimization, enabling predictable latency, streaming-friendly implementations, and straightforward extension to more phases or weighted rewrite costs.

## 🔍 Problem Statement
You are given a string `colors` where each character is one of `'R'`, `'G'`, or `'B'`, and `3 <= n <= 200000`. Repaint operations change any single building to any color, each costing `1`. The goal is to transform the full string into three contiguous non-empty districts in left-to-right order: a red block, followed by a green block, followed by a blue block.

Formally, choose split points `i < j` so that:
- `colors[0..i]` becomes all `R`
- `colors[i+1..j]` becomes all `G`
- `colors[j+1..n-1]` becomes all `B`

Return the minimum repaint cost over all valid splits.

Examples:
- `colors = "RGRBB"` → `1`
- `colors = "BBRGRG"` → `3`

The critical constraint is `n = 200000`: evaluating every pair of split points is `O(n^2)` and not viable.

## 🪜 How to Solve This
1. Start with the brute-force framing → there are two split points, so a naive solution tries every `(i, j)` and computes repaint cost for three ranges.
2. Notice what makes brute force expensive → recomputing “how many characters in this range are not `R`/`G`/`B`” over and over.
3. That suggests prefix sums immediately → if we know counts of each color up to every index, then repaint cost for any interval and target color becomes `interval_length - count_of_target_color`.
4. Now the search space is still all `(i, j)` pairs. Better cost queries do not fix the quadratic number of candidate splits.
5. Reframe the problem as staged optimization → for every position, track the cheapest way to make the prefix end in the `R` phase, then the `RG` phases, then the `RGB` phases.
6. This is dynamic programming on prefixes with fixed phase order. Each new building either stays in the current phase or starts the next one.
7. Because districts must be non-empty, initialize carefully so phase transitions cannot happen too early.
8. Result: one left-to-right pass, constant work per character, no nested boundary search.

## 🧩 Algorithm Walkthrough
1. **Choose the abstraction: prefix DP over ordered phases.**  
   This is a dynamic programming problem on arrays, with an ordered-state machine: phase 1 is `R`, phase 2 is `G`, phase 3 is `B`. The invariant is that after processing index `i`, each DP state stores the minimum repaint cost for `colors[0..i]` under a valid prefix ending in that phase.

2. **Define the states.**  
   Let:
   - `dpR[i]` = min cost to make `colors[0..i]` all `R`
   - `dpG[i]` = min cost to make `colors[0..i]` into non-empty `R+G`
   - `dpB[i]` = min cost to make `colors[0..i]` into non-empty `R+G+B`

   This enforces order directly; no invalid permutations are representable.

3. **Compute local repaint costs.**  
   For character `c = colors[i]`:
   - `costR = 0 if c == 'R' else 1`
   - `costG = 0 if c == 'G' else 1`
   - `costB = 0 if c == 'B' else 1`

   Each state transition adds exactly one of these costs.

4. **Write the transitions.**  
   - `dpR[i] = dpR[i-1] + costR`  
     Still in the red district.
   - `dpG[i] = min(dpR[i-1], dpG[i-1]) + costG`  
     Either start green here after a non-empty red prefix, or extend green.
   - `dpB[i] = min(dpG[i-1], dpB[i-1]) + costB`  
     Either start blue here after a non-empty green prefix, or extend blue.

   The invariant is preserved: every state corresponds to a valid partition with all required earlier districts present.

5. **Handle initialization to enforce non-empty districts.**  
   At `i = 0`, only `dpR` is valid. `dpG` and `dpB` must start as infinity because green and blue cannot begin before red exists. Similarly, `dpB` only becomes valid once at least one green position has been formed.

6. **Compress space.**  
   Only the previous index is needed, so keep three scalars instead of arrays. This yields `O(n)` time and `O(1)` extra space while preserving the same recurrence.

## 📊 Worked Example
Take `colors = "RGRBB"`.

| i | c | costR | costG | costB | dpR | dpG | dpB |
|---|---|------:|------:|------:|----:|----:|----:|
| 0 | R | 0 | 1 | 1 | 0 | ∞ | ∞ |
| 1 | G | 1 | 0 | 1 | 1 | 0 | ∞ |
| 2 | R | 0 | 1 | 1 | 1 | 1 | 1 |
| 3 | B | 1 | 1 | 0 | 2 | 2 | 1 |
| 4 | B | 1 | 1 | 0 | 3 | 3 | 1 |

Trace:
1. Index `0` must belong to the red district, so only `dpR` is valid.
2. At index `1`, we can start the green district with zero repaint because the character is already `G`.
3. At index `2`, we can start the blue district from the best valid `RG` prefix, but since the character is `R`, that costs `1`.
4. Extending through the final two `B`s keeps `dpB` at `1`.

Answer: `1`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time: one left-to-right pass, constant-time state updates per building. That is practical for `2 * 10^5`, still fine at `10^6`, and remains predictable under large-scale workloads where `O(n^2)` boundary enumeration would be completely infeasible.

### Space Complexity
The space complexity is `O(1)` extra space if the DP is state-compressed into three scalars. If full DP arrays are kept for debugging or reconstruction, space becomes `O(n)`. The reduction trades observability for tighter memory and better cache behavior.

## 💡 Key Takeaways
- If a sequence must be transformed into a fixed left-to-right phase order, think prefix DP before considering explicit split enumeration.
- If range costs are “number of mismatches to a target label,” prefix sums or per-element transition costs usually eliminate repeated recomputation.
- The non-empty district requirement is the main correctness trap; invalid early transitions must be blocked with proper initialization.
- Off-by-one errors usually come from confusing “start new phase at index `i`” with “transition from a valid prefix ending at `i-1`.”
- At scale, the real win is reframing boundary search as stateful streaming optimization: same correctness, radically better latency and memory locality.

## 🚀 Variations & Further Practice
- Generalize from three districts to `k` ordered districts with a target color sequence like `C1, C2, ..., Ck`; the twist is extending the DP from 3 states to `k` while preserving `O(nk)` complexity.
- Add weighted repaint costs per building or per target color; the recurrence still works, but local mismatch cost becomes heterogeneous instead of binary.
- Require recovering the actual split points, not just the minimum cost; this adds parent tracking or reconstruction logic on top of the DP states.