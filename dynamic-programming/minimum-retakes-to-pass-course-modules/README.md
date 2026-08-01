# Minimum Retakes to Pass Course Modules

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, state-transition

---

## 🗂 Problem Overview
Given an array `modules` and a fixed increment `d`, process scores left to right and decide for each module whether to keep its score or retake it once to add exactly `d`. The final chosen scores must form a non-decreasing sequence. Return the minimum number of retakes required, or `-1` if no valid sequence exists. The challenge is that each position has only two possible final values, but local choices can block all future feasibility.

## 🌍 Engineering Impact
This pattern shows up in streaming normalization and constrained correction pipelines, where events arrive in order and each item has a small set of allowed adjustments. Examples include ranking-score calibration, telemetry repair, packet timestamp correction, and workflow state reconciliation. At scale, greedy local repair often fails because preserving short-term feasibility can increase future cost or make the stream unrecoverable. A compact dynamic-programming state model lets you process large ordered datasets in one pass, preserve strict sequencing guarantees, and reason explicitly about feasibility versus optimization under limited per-item transformations.

## 🔍 Problem Statement
You are given:

- An integer array `modules` of length `n`
- An integer `d`

For each index `i`, you must choose exactly one final score:

- `modules[i]`
- `modules[i] + d` if you spend one retake on that module

Each module can be retaken at most once, and modules must remain in their original order. The chosen final sequence must be non-decreasing.

Return the minimum number of retakes needed to make this possible. If no valid sequence exists, return `-1`.

Constraints:

- `1 <= n <= 100000`
- `0 <= modules[i] <= 1000000000`
- `0 <= d <= 1000000000`

Examples:

- `modules = [4, 2, 5, 5], d = 3` → `1`
- `modules = [7, 3, 2], d = 4` → `-1`

The key constraint is `n = 1e5`, which rules out exponential branching and pushes toward a linear-time state-transition DP.

## 🪜 How to Solve This
1. Read the problem → each module has only two possible final values: original or boosted. That immediately suggests a tiny state space per index.

2. Notice the dependency → whether module `i` can end at a value depends only on what value module `i-1` ended at. This is classic sequential state-transition DP.

3. Define the states → for every position, track the minimum retakes needed if this module ends in:
   - state `0`: `modules[i]`
   - state `1`: `modules[i] + d`

4. Transition from previous states → a transition is valid only if the previous chosen value is `<=` the current chosen value. Among all valid predecessors, keep the minimum retake count.

5. Add the cost of choosing boosted state → moving into state `1` adds one retake; state `0` adds zero.

6. If both states become unreachable at some index, stop early and return `-1`.

7. At the end, take the minimum of the two final states.

The reason this works is that the future only cares about two things: the current final value and how many retakes were spent to get there.

## 🧩 Algorithm Walkthrough
1. **Use Dynamic Programming with two states per index.**  
   This is a **state-transition DP on an array**. For index `i`, define:
   - `dp0`: minimum retakes if module `i` ends as `modules[i]`
   - `dp1`: minimum retakes if module `i` ends as `modules[i] + d`  
   This abstraction is correct because each module has exactly two legal outcomes.

2. **Initialize the first module.**  
   For `i = 0`:
   - ending at `modules[0]` costs `0`
   - ending at `modules[0] + d` costs `1`  
   Invariant: each DP state stores the minimum retakes among all valid sequences ending at that exact value.

3. **For each next module, compute its two candidate values.**  
   Let:
   - `cur0 = modules[i]`
   - `cur1 = modules[i] + d`  
   Also keep previous values:
   - `prev0 = modules[i-1]`
   - `prev1 = modules[i-1] + d`

4. **Try all four transitions.**  
   From each reachable previous state, transition to each current state if the sequence remains non-decreasing:
   - `prev0 <= cur0`
   - `prev0 <= cur1`
   - `prev1 <= cur0`
   - `prev1 <= cur1`  
   For each valid transition, minimize the target DP value.

5. **Apply retake cost only when entering boosted state.**  
   Transitioning into `cur1` adds `1`; transitioning into `cur0` adds `0`. This preserves optimality because every valid sequence ending in a state is compared by total retake count.

6. **Roll the DP forward in O(1) space.**  
   Only the previous index matters, so store two values and update them per step.

7. **Detect impossibility.**  
   If both current states are unreachable, no future choice can recover feasibility. Return `-1`.

## 📊 Worked Example
Example: `modules = [4, 2, 5, 5]`, `d = 3`

| i | values        | dp0 (keep) | dp1 (retake) | Notes |
|---|---------------|------------|--------------|-------|
| 0 | 4 / 7         | 0          | 1            | Start: keep or retake first |
| 1 | 2 / 5         | INF        | 1            | `2` is too small after `4` or `7`; `5` works after `4` |
| 2 | 5 / 8         | 1          | 2            | From previous `5`, both `5` and `8` are valid |
| 3 | 5 / 8         | 1          | 2            | Again both valid; keeping `5` preserves min cost |

Trace:
1. At index `0`, either end at `4` with cost `0`, or `7` with cost `1`.
2. At index `1`, value `2` cannot follow `4` or `7`, so keep-state is unreachable.
3. Retaken value `5` can follow `4`, giving cost `1`.
4. Remaining modules can stay at `5`, so the minimum total retakes is `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index evaluates a constant number of transitions: four predecessor-to-current checks across two states. That remains practical for `10^6` elements and is the only viable direction once `n` approaches `10^5+`. Anything quadratic collapses immediately; anything exponential is irrelevant even at small scales.

### Space Complexity
`O(1)` auxiliary space. The algorithm only needs the previous index’s two DP values and the current two values. You could store full DP tables for debugging or reconstruction, but that increases memory to `O(n)` without improving the minimum-retakes computation.

## 💡 Key Takeaways
- If each position has a tiny fixed set of legal outcomes and order must be preserved, think state-transition DP rather than greedy repair.
- If feasibility depends only on the previous chosen value, that is a strong signal for rolling DP over an array.
- Treat unreachable states explicitly with `INF`; silently using default zero values will produce false valid paths.
- Be careful when `d = 0`: the two candidate values are identical, but the retake costs differ, so the minimum must still be chosen correctly.
- In production pipelines, bounded per-item correction plus global monotonicity is rarely solvable by local heuristics; compact DP gives predictable correctness under streaming constraints.

## 🚀 Variations & Further Practice
- Allow each module to be retaken up to `k` times, adding `d` each time. The twist is the state space grows from 2 states per index to `k+1`, forcing more careful optimization.
- Allow a different increment `d[i]` per module. The DP shape stays similar, but uniform reasoning about transitions disappears and edge-case handling gets sharper.
- Minimize total retake cost when each module has a custom retake penalty instead of unit cost. The feasibility condition is unchanged, but the optimization target becomes weighted rather than count-based.