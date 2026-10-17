# Minimum Refills to Keep a Garden Strip Green

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Prefix Sum

---

## 🗂 Problem Overview
Given `waterNeed`, tank capacity `cap`, and a one-time emergency reserve `bonus`, compute the minimum refills needed to water all sections from left to right. Normally, if remaining water is less than the next section’s requirement, you must refill before watering it. Once in the entire run, you may ignore that shortage if the deficit is at most `bonus`, after which the tank becomes `0`. The challenge is deciding where that one-time exception produces the fewest future refills.

## 🌍 Engineering Impact
This pattern shows up in systems that process ordered work under a fixed budget with one limited exception: API gateways with burst credits, streaming pipelines with one spill-to-disk allowance, battery-constrained edge devices with emergency reserve, or schedulers with a single SLA grace event. At scale, brute-forcing the exception point is unacceptable because it turns a linear scan into repeated simulation. The useful abstraction is “sequential resource depletion with one optional override.” Solving it greedily with prefix-style state enables predictable latency, constant memory, and straightforward reasoning about correctness under very large input sizes.

## 🔍 Problem Statement
You are given an array `waterNeed` of length `n`, where `waterNeed[i]` is the water required for section `i`. A cart starts full with `cap` units and waters sections strictly left to right. Watering section `i` consumes `waterNeed[i]` units. If the remaining water is insufficient before a section, the cart must refill to `cap`, and that counts as one refill.

Exactly once, the cart may skip such a refill if the shortage for that section is at most `bonus`. In that case, the section is still watered, the reserve is consumed, and the tank becomes `0` immediately after that section.

Return the minimum number of refills.

Constraints:
- `1 <= n <= 200000`
- `1 <= waterNeed[i] <= cap <= 1000000000`
- `0 <= bonus <= 1000000000`

Examples:
- `waterNeed = [2,4,3,5,2], cap = 6, bonus = 2` → `2`
- `waterNeed = [5,1,4,4], cap = 5, bonus = 0` → `2`

The key constraint is `n = 2e5`, which rules out trying every possible reserve-usage position with full resimulation.

## 🪜 How to Solve This
1. Read the process carefully → without the reserve, the behavior is deterministic. You only refill when the next section does not fit.
2. The only real decision is **where to spend the one-time reserve**. Everything else is forced.
3. Brute force would try each eligible section as the reserve point and simulate the whole walk again. That is `O(n^2)` in the worst case, so it is dead on arrival.
4. Reframe the problem: for any position `i`, if the current tank before watering `i` is `rem < waterNeed[i]` and the deficit `waterNeed[i] - rem <= bonus`, then using the reserve there saves the refill you would otherwise do **right now**.
5. But using it also changes the post-section state to `0`, which can affect later refill boundaries. So “take the first eligible” is not obviously safe unless we reason about future state.
6. The right mental model is dynamic greedy over a linear scan: maintain the best refill count for two states at each step — reserve unused and reserve already used.
7. That gives a one-pass solution with constant memory: each section updates both states based on whether we refill normally or consume the reserve now.

## 🧩 Algorithm Walkthrough
1. **Use a greedy DP over the array.**  
   The pattern is a **linear scan with constant-state dynamic programming**. Greedy alone is insufficient because the one-time reserve changes future tank state; full DP over positions is unnecessary because only two histories matter: reserve unused vs used.

2. **Track two states after processing each section.**  
   For each state, store:
   - minimum refills so far
   - remaining water after watering the current section  
   Call them:
   - `unused = (refills0, rem0)` for paths where reserve is still available
   - `used = (refills1, rem1)` for paths where reserve has already been consumed

3. **Transition the `unused` state normally.**  
   Before section `x`, if `rem0 >= x`, water directly. Otherwise refill once, set tank to `cap`, then water. This is the deterministic no-reserve path.  
   Invariant: `unused` is the best possible state among all ways that have not spent the reserve yet.

4. **Transition the `used` state normally as well.**  
   Since reserve is gone, the only option is the standard refill rule.  
   Invariant: `used` is the best possible state among all ways that already spent the reserve somewhere earlier.

5. **Optionally spend the reserve from the previous `unused` state.**  
   If before section `x`, `rem0 < x` and `x - rem0 <= bonus`, then instead of refilling, consume the reserve here. The resulting state is:
   - refills unchanged
   - remaining water becomes `0`  
   Compare this candidate against the existing `used` transition and keep the better one.

6. **Define “better” carefully.**  
   Prefer fewer refills. If refill counts tie, prefer larger remaining water, because it dominates future decisions.

7. **Return the refill count in the better final state.**  
   Since reserve usage is optional, answer is `min(refills0, refills1)` after the full scan.

This works because the future depends only on current remaining water and whether the reserve is still available.

## 📊 Worked Example
Take `waterNeed = [2,4,3,5,2]`, `cap = 6`, `bonus = 2`.

| i | need | unused `(refills, rem)` | used `(refills, rem)` | note |
|---|---:|---|---|---|
| start | - | `(0, 6)` | impossible | full tank |
| 0 | 2 | `(0, 4)` | impossible | water directly |
| 1 | 4 | `(0, 0)` | impossible | water directly |
| 2 | 3 | `(1, 3)` | impossible | must refill |
| 3 | 5 | `(2, 1)` | `(1, 0)` | from `unused`, deficit `2 <= bonus`, so reserve can be spent here |
| 4 | 2 | `(3, 4)` | `(2, 4)` | both paths need refill before section 4 |

Final answer is `min(3, 2) = 2`.

The important detail is that spending the reserve at section `3` avoids one refill immediately, even though it leaves the tank at `0`. The DP keeps both possibilities alive until the better one is clear.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each section performs a constant amount of work: update the `unused` path, update the `used` path, and possibly create one additional candidate by spending the reserve now. At `10^6` elements this is routine; at `10^9`, linear time is still too large in practice, but asymptotically optimal for a full scan.

### Space Complexity
`O(1)`. The algorithm stores only a few scalar state variables for the two DP states. No auxiliary arrays or prefix tables are required. Space cannot be meaningfully reduced further unless you trade clarity for tightly packed state handling.

## 💡 Key Takeaways
- If a left-to-right process is deterministic except for one optional intervention, that is a strong signal for constant-state DP over “intervention unused / used.”
- When the input must be processed in order and each decision only affects future budget, think greedy scan first, then add minimal state to handle the exception.
- The reserve can only be used when the current tank is insufficient and the deficit is `<= bonus`; using it when water is already sufficient is never beneficial.
- After spending the reserve, the remaining tank is exactly `0`, not negative and not `cap - need`; that state transition is the main implementation trap.
- In production systems, one-shot exception budgets are best modeled as explicit state dimensions, not ad hoc conditionals scattered through the main control flow.

## 🚀 Variations & Further Practice
- Allow the reserve to be used up to `k` times. The conceptual twist is expanding from two states to `k+1` states, turning constant-state DP into small-dimensional budget DP.
- Make refill stations available only at certain indices. Now the scan must reason about reachability windows, not just local insufficiency.
- Charge different refill costs at different positions. The problem shifts from minimizing refill count to minimizing weighted cost, which breaks the simple local intuition and requires richer DP.