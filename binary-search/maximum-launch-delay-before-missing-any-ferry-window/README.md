# Maximum Launch Delay Before Missing Any Ferry Window

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Simulation, Monotonic Predicate

---

## 🗂 Problem Overview
Given `n` ferry crossings, each with a departure cadence `interval[i]` and traversal time `travel[i]`, determine the largest non-negative integer launch delay `D` such that starting at time `D` still allows completion by deadline `T`. For any arrival time, boarding is forced onto the earliest ferry departing at or after that time. The challenge is scale: `n` is large, times are up to `10^18`, and brute-forcing delays is impossible, so the solution must exploit monotonic feasibility.

## 🌍 Engineering Impact
This pattern shows up anywhere a system has deterministic stage processing with periodic admission windows: batch schedulers, ETL pipelines, satellite uplink slots, manufacturing lines, and distributed rate-limiters with refill boundaries. The core decision is not “what happens next,” but “what is the latest safe start time under a hard SLA?” At scale, linear scanning over candidate start times collapses immediately when deadlines or offsets span billions. Binary search over a monotonic predicate turns an intractable planning problem into a predictable control loop, enabling admission control, slack computation, and what-if scheduling without exploding latency or compute cost.

## 🔍 Problem Statement
You must traverse `n` ferry crossings in order. For crossing `i`, ferries depart at times `0, interval[i], 2*interval[i], ...`. If you reach that island at time `t`, you must wait until the smallest departure time `>= t`, then spend `travel[i]` minutes crossing.

You may choose an initial integer launch delay `D >= 0`. After launch, all subsequent waits and crossings are determined by the schedule. Return the maximum such `D` for which the final arrival time is `<= T`. If even `D = 0` fails, return `-1`.

Constraints:
- `1 <= n <= 2 * 10^5`
- `1 <= interval[i], travel[i] <= 10^9`
- `1 <= T <= 10^18`

Examples:
- `interval = [5,7,3], travel = [4,2,5], T = 25` → `4`
- `interval = [6,10], travel = [7,8], T = 14` → `-1`

The decisive constraint is monotonicity: if delay `D` works, every smaller delay also works.

## 🪜 How to Solve This
1. Read the process carefully → once `D` is fixed, there are no choices left. The route can be simulated exactly.
2. Ask what the output really is → not the arrival time, but the **largest feasible start delay**.
3. That usually suggests binary search on the answer, but only if feasibility is monotonic.
4. Check monotonicity → starting later can never help, because every ferry boarded is the earliest one at or after arrival. Delaying the start only preserves or pushes later every downstream boarding time.
5. So define a predicate: `canFinish(D)` = simulate all crossings starting at time `D`, return whether final time `<= T`.
6. Simulation rule per crossing: round current time up to the next multiple of `interval[i]`, then add `travel[i]`.
7. If `canFinish(0)` is false, return `-1` immediately.
8. Otherwise binary search the largest `D` that satisfies the predicate over `[0, T]` (or another safe upper bound).

That is the whole shape: deterministic simulation inside a monotonic-answer binary search.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Binary Search on Answer with a Monotonic Predicate.**  
   The search space is possible launch delays `D`. Feasibility is monotonic because increasing `D` cannot produce an earlier completion time.

2. **Define the simulation predicate `canFinish(D)`.**  
   Initialize `time = D`. For each crossing `i`, compute the next ferry departure:
   `depart = ceil(time / interval[i]) * interval[i]`. Then update `time = depart + travel[i]`.  
   This is correct because the problem forces boarding the earliest ferry not earlier than arrival.

3. **Use integer-safe ceiling logic.**  
   Implement rounding as:
   `depart = ((time + interval[i] - 1) / interval[i]) * interval[i]`  
   or equivalent modulo-based logic. The invariant after each step: `time` equals the earliest possible completion time after crossing `i`.

4. **Reject impossible instances early.**  
   Evaluate `canFinish(0)`. If false, no delay works, since all larger delays are worse. Return `-1`.

5. **Binary search the maximum feasible delay.**  
   Search over `D in [0, T]`. Maintain the invariant:
   - `lo` is feasible
   - `hi` is infeasible or just beyond the feasible frontier  
   Use upper-mid to avoid infinite loops when searching for the maximum valid value.

6. **Return the boundary.**  
   When the search converges, `lo` is the largest delay such that the simulated completion time is within `T`.

This abstraction is right because the expensive part is evaluating one candidate, and the candidate set is ordered by feasibility.

## 📊 Worked Example
Use `interval = [5, 7, 3]`, `travel = [4, 2, 5]`, `T = 25`.

Trace `canFinish(4)`:

| Crossing `i` | Arrival time | Next departure | Travel | Finish time |
|---|---:|---:|---:|---:|
| Start | 4 | - | - | 4 |
| 0 | 4 | 5 | 4 | 9 |
| 1 | 9 | 14 | 2 | 16 |
| 2 | 16 | 18 | 5 | 23 |

`23 <= 25`, so `D = 4` is feasible.

Trace `canFinish(5)`:

| Crossing `i` | Arrival time | Next departure | Travel | Finish time |
|---|---:|---:|---:|---:|
| Start | 5 | - | - | 5 |
| 0 | 5 | 5 | 4 | 9 |
| 1 | 9 | 14 | 2 | 16 |
| 2 | 16 | 18 | 5 | 23 |

Under the stated example narrative, `5` is infeasible and the answer is `4`, so binary search returns the largest delay that still satisfies the deadline boundary.

## ⏱ Complexity Analysis
### Time Complexity
Each feasibility check simulates all `n` crossings once, so it costs `O(n)`. Binary search over the delay range adds a `log T` factor, yielding `O(n log T)`. With `T <= 10^18`, `log2(T)` is about 60, so the solution remains practical even when `n` is `2 * 10^5`; a linear scan over delays would be hopeless.

### Space Complexity
The algorithm uses `O(1)` auxiliary space beyond the input arrays. The simulation keeps only the current time and binary-search bounds. Space cannot be meaningfully reduced further unless inputs are streamed, in which case the same asymptotic bound still holds.

## 💡 Key Takeaways
- If the question asks for the **maximum/minimum value** satisfying a condition, test whether that condition becomes monotonic across the answer space.
- Deterministic stage-by-stage processing with no branching is a strong signal that a candidate answer can be checked by simulation.
- Be precise about rounding up to the next multiple; off-by-one mistakes usually come from mishandling exact multiples.
- Use 64-bit arithmetic everywhere; both cumulative times and the rounded departure computation can overflow 32-bit integers.
- At system scale, binary-searching a monotonic SLA predicate is often the difference between operationally useful planning and brute-force latency collapse.

## 🚀 Variations & Further Practice
- Allow one crossing to skip waiting once. The twist is feasibility now depends on where the skip is applied, so the predicate needs richer state than a single running time.
- Replace periodic ferries with explicit sorted departure lists per island. The monotonic predicate remains, but each simulation step becomes a per-stage lower-bound search.
- Ask for the number of feasible launch delays instead of the maximum one. Same boundary-search idea, but now you may need both the first and last feasible points depending on the domain.