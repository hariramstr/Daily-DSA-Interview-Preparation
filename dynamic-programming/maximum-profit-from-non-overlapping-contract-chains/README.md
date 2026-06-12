# Maximum Profit from Non-Overlapping Contract Chains

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Scheduling, State Compression

---

## 🗂 Problem Overview
Given `N` day-indexed contracts, each startable only on its exact day, choose a non-overlapping subset maximizing total profit. A chosen contract on day `i` consumes `duration[i]` days and yields `profit[i]`, but only if it finishes within the horizon. The complication is historical state: consecutive accepted contracts cannot belong to the same `group`. Skipped days do not clear that constraint, so the decision depends on both time compatibility and the group of the last selected contract.

## 🌍 Engineering Impact
This pattern shows up in ad-serving campaign selection, batch job admission control, cloud reservation planning, and marketplace matching where decisions are time-exclusive but also constrained by the last accepted category, tenant, or policy domain. At scale, naive interval scheduling with extra state collapses into quadratic scans or per-group recomputation. The right formulation turns “best prior compatible choice except same class” into a constant-time query backed by compressed state. That enables online planning over hundreds of thousands of intervals without exploding latency, memory, or coordination complexity in schedulers and optimization services.

## 🔍 Problem Statement
You are given three arrays of length `N`: `duration`, `profit`, and `group`. Contract `i` may be started only on day `i`, occupies days `i` through `i + duration[i] - 1`, and is valid only if it finishes by day `N - 1`. You may skip any day. Among all chosen contracts, no two may overlap. Additionally, if two chosen contracts are consecutive in the final schedule, their `group` values must differ; skipped days do not reset this rule.

Return the maximum total profit.

Constraints:
- `1 <= N <= 2 * 10^5`
- `1 <= duration[i] <= N`
- `1 <= profit[i] <= 10^9`
- `1 <= group[i] <= 2 * 10^5`
- Answer fits in signed 64-bit

Examples:
- `duration=[2,1,2,1], profit=[50,10,40,70], group=[1,1,2,1]` → `90`
- `duration=[1,2,1,1,2], profit=[8,20,7,15,30], group=[1,2,1,3,2]` → `53`

The scale rules out any DP that scans all previous days or all previous groups per contract.

## 🪜 How to Solve This
1. Start with classic weighted interval scheduling: for each day, either skip it or take the contract starting there and jump to its end day. That would normally be a 1D DP over time.

2. Then notice the extra rule breaks plain 1D DP. Taking contract `i` is not determined only by “best profit before day `i`”; it depends on the group of the last accepted contract.

3. A naive fix is `dp[day][lastGroup]`, but `N` and group IDs are both up to `2e5`, so that state space is dead on arrival.

4. Reframe the transition: to take contract `i` with group `g`, you need the best completed schedule before day `i` whose last group is **not** `g`.

5. That suggests maintaining, for every day boundary, not all groups, but only the **top two** completed DP states: the best total profit and its ending group, plus the second-best. Then “best except group `g`” is immediate.

6. Process days left to right. Carry forward skip decisions, and when a contract ends on day `e`, publish a candidate state for boundary `e + 1`.

7. This is dynamic programming with state compression: preserve exactly the information needed for future transitions, nothing more.

## 🧩 Algorithm Walkthrough
1. **Model DP on day boundaries.**  
   Let `bestAt[t]` represent the best completed schedule available before day `t` (i.e., after processing days `< t`). We process boundaries `0..N`. This is the standard scheduling-DP abstraction because a contract starting at day `i` can only depend on schedules completed by boundary `i`.

2. **Compress state to top-two `(profit, lastGroup)` pairs per boundary.**  
   For each boundary `t`, store:
   - `first[t] = (maxProfit, groupOfLastContract)`
   - `second[t] = (secondMaxProfit, groupOfLastContract)`  
   Why two? Because when evaluating a contract of group `g`, we need the best prior schedule whose last group is not `g`. If `first.group != g`, use `first`; otherwise use `second`. This is the key state-compression invariant.

3. **Initialize the empty schedule.**  
   At boundary `0`, the best profit is `0` with a sentinel group such as `-1`. This allows the first chosen contract to come from any group.

4. **Carry forward skip decisions.**  
   As we move from boundary `t` to `t + 1`, propagate `first[t]` and `second[t]` into `t + 1`. This encodes “do nothing on day `t`” and preserves monotonic availability of completed schedules.

5. **Evaluate the contract starting on day `i`.**  
   If `i + duration[i] <= N`, let `end = i + duration[i]`. Query boundary `i` for the best prior schedule excluding `group[i]`. Add `profit[i]` to that value to form a candidate schedule ending at boundary `end` with last group `group[i]`.

6. **Insert the candidate into boundary `end`.**  
   Update the top-two states for `end`, deduplicating by group: if a candidate improves the stored value for its group, keep the better one. Maintain the invariant that `first` and `second` are the two highest-profit distinct-group states known at that boundary.

7. **Return the best profit at boundary `N`.**  
   Because skip propagation makes each boundary accumulate all earlier completions, `first[N].profit` is the global optimum.

This is dynamic programming over time with state compression by “best two distinct last-group summaries,” which is exactly the right abstraction because the transition only asks for “best except this group.”

## 📊 Worked Example
Use `duration=[2,1,2,1]`, `profit=[50,10,40,70]`, `group=[1,1,2,1]`.

| Day `i` | Best before `i` | Contract `(dur,p,g)` | Prior best excluding `g` | Candidate ends at | Result |
|---|---:|---|---:|---:|---:|
| 0 | 0, last=-1 | (2,50,1) | 0 | 2 | profit 50, last=1 |
| 1 | 0, last=-1 | (1,10,1) | 0 | 2 | profit 10, last=1 |
| 2 | 50, last=1 | (2,40,2) | 50 | 4 | profit 90, last=2 |
| 3 | 50, last=1 | (1,70,1) | 0 | 4 | profit 70, last=1 |

Boundary propagation matters:
- At boundary `2`, best completed schedule is `(50, group 1)`.
- For day `2`, group `2` differs, so we can extend to `90`.
- For day `3`, group `1` matches the best prior group, so we must fall back to the second-best prior state, which is the empty schedule with profit `0`.

Final answer at boundary `4` is `90`.

## ⏱ Complexity Analysis
### Time Complexity
`O(N)`. Each day performs constant work: carry forward two states, compute at most one candidate contract, and update one boundary’s top-two summaries. There is no per-group scan and no predecessor search. At `10^6` scale this remains practical; at `10^9`, even linear time is too large, so the bottleneck becomes input size, not algorithmic overhead.

### Space Complexity
`O(N)`. You store two compressed DP states per day boundary, plus input arrays. The dominant space owner is the boundary DP table. It can be reduced only with more complex event buffering or in-place propagation, but the asymptotic bound stays linear because future end boundaries must still be updated.

## 💡 Key Takeaways
- If a scheduling problem says “take non-overlapping intervals” and “the next choice depends on the previous chosen category,” think weighted interval scheduling plus compressed historical state.
- If the transition is “best previous result except one forbidden class,” that is a strong signal that top-`k` summaries, not full per-class DP, may be sufficient.
- Use day **boundaries** (`end = start + duration`) rather than inclusive day ranges; most off-by-one bugs come from mixing “last occupied day” with “next available day.”
- Skipped days do **not** reset the last accepted group; treating idle time as a state reset produces invalid schedules and inflated answers.
- In production optimizers, the winning move is often not a better search structure but identifying the minimal state future decisions actually need.

## 🚀 Variations & Further Practice
- Allow up to `K` consecutive contracts from the same group with a penalty or cap. The twist is that last-group state becomes `(group, streakLength)`, forcing a richer compressed DP.
- Add a cooldown between contracts or release times independent of start day. The twist is combining interval compatibility with delayed availability, often requiring predecessor indexing or event queues.
- Replace “different from previous group” with “group transitions have arbitrary costs.” The twist turns the top-two trick into a max-plus transition problem over groups, which may require segment trees, sparse optimization, or matrix-style DP.

---