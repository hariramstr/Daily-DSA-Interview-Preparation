# Minimum Charging Stops for Deadline-Limited Robots

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, greedy, sorting, simulation

---

## 🗂 Problem Overview
A robot must travel from `0` to `target` with initial battery `startCharge`, spending one unit of charge per unit distance. Along the way, stations offer extra charge, but only if the robot reaches them no later than each station’s expiry time. The goal is to return the minimum number of charging stops needed to reach `target`, or `-1` if impossible. The difficulty is that reachability is constrained by both distance and time, so “seen” stations can become useless if not taken immediately.

## 🌍 Engineering Impact
This pattern shows up in systems that must defer expensive choices while preserving future optionality under deadlines: streaming schedulers consuming expiring credits, distributed rate-limiters with time-bounded burst tokens, ad-serving or search pipelines selecting best remaining candidates before SLA cutoffs, and fleet routing with per-node availability windows. At scale, naive local decisions either over-consume scarce resources or miss deadlines despite globally feasible plans. A heap-backed greedy strategy enables online selection of the highest-value previously reachable option exactly when progress stalls, which is the difference between predictable throughput and deadline-driven collapse.

## 🔍 Problem Statement
Given `target`, `startCharge`, and `stations`, where each station is `[position_i, charge_i, expiry_i]`, determine the minimum number of charging stops required for the robot to reach `target`. Moving distance `d` consumes `d` battery, time equals total distance traveled, charging is instantaneous, and the robot cannot move backward. A station can provide its full charge only if the robot arrives at its position at time `t <= expiry_i`; otherwise it contributes nothing.

Constraints:

- `1 <= target <= 10^9`
- `0 <= startCharge <= 10^9`
- `1 <= stations.length <= 2 * 10^5`
- `0 < position_i < target`
- `1 <= charge_i <= 10^9`
- `0 <= expiry_i <= 10^9`

Examples:

- `target = 25, startCharge = 10, stations = [[5,8,7],[9,7,12],[14,10,20]]` → `2`
- `target = 30, startCharge = 8, stations = [[6,5,5],[7,20,6],[10,10,15]]` → `-1`

The key constraint is `2 * 10^5` stations with large coordinates, which rules out simulation by battery unit or repeated rescans.

## 🪜 How to Solve This
1. Start with the classic minimum-refuel-stops mental model → when you cannot move farther, retroactively take the largest charge among stations you have already passed. That immediately suggests a max-heap.

2. Now add expiry → a passed station is not automatically usable later. Since time equals current traveled distance, once your reachable frontier exceeds a station’s expiry, that station is dead forever.

3. Sort stations by position → as the frontier expands, add every station whose position is now reachable. But only add stations that were valid when reached, i.e. `position <= expiry`. If that condition fails, the station was already expired at arrival and can never help.

4. Maintain a max-heap of charges from all passed-and-valid stations not yet used.

5. If current charge cannot reach the next milestone, pop the largest available charge. This greedy choice minimizes stop count because every stop should buy the maximum possible extension.

6. Repeat until the frontier reaches `target`, or the heap is empty while progress is still required → impossible.

The key realization is subtle: expiry is checked at arrival time, and arrival time at a station is fixed by its position, not by when you later decide to activate it.

## 🧩 Algorithm Walkthrough
1. **Sort by position**.  
   Use a greedy + max-heap pattern. Sorting gives the stations in the order the robot encounters them on the line. This is necessary because time is identical to distance traveled, so station eligibility is determined at first pass.

2. **Track the farthest reachable position**.  
   Initialize `reach = startCharge`. This is both remaining movement budget from the origin and the current time horizon. Invariant: everything at `position <= reach` is physically reachable from prior decisions.

3. **Scan and enqueue newly reachable stations**.  
   While the next station has `position <= reach`, process it. If `position <= expiry`, push `charge` into a max-heap; otherwise ignore it permanently. Correctness: arrival time at that station is exactly its position, so validity is immutable once known.

4. **If target is already within reach, stop**.  
   If `reach >= target`, return the number of stops taken. Invariant: the algorithm never takes an unnecessary stop after the target becomes reachable.

5. **If stuck, consume the best deferred option**.  
   If no more stations can be added and `reach < target`, pop the maximum charge from the heap, add it to `reach`, and increment stops. This is the core greedy step: among all valid passed stations, taking the largest charge maximizes future reach per stop, which is exactly the objective.

6. **Detect impossibility**.  
   If `reach < target` and the heap is empty, return `-1`. There is no valid previously passed station left to extend the frontier, so no future station can ever be reached.

This is the right abstraction because it is an **online greedy selection over a growing candidate set**, which is exactly what priority queues are for.

## 📊 Worked Example
Example: `target = 25`, `startCharge = 10`, `stations = [[5,8,7],[9,7,12],[14,10,20]]`

| Step | `reach` | Newly reachable stations | Max-heap | Action | Stops |
|---|---:|---|---|---|---:|
| Start | 10 | `(5,8,7)`, `(9,7,12)` valid | `[8,7]` | none | 0 |
| 1 | 10 | no more | `[8,7]` | pop `8`, `reach = 18` | 1 |
| 2 | 18 | `(14,10,20)` valid | `[10,7]` | none | 1 |
| 3 | 18 | no more | `[10,7]` | pop `10`, `reach = 28` | 2 |
| End | 28 | target reached | — | return `2` | 2 |

Why this works: the robot first defers charging decisions, then only spends a stop when progress stalls. At `reach = 10`, both stations at 5 and 9 are already reachable and valid, so the best stop is the larger charge `8`. After extending to 18, station 14 becomes available, and taking `10` next reaches the target with the fewest stops.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` overall: `O(n log n)` to sort stations by position, then each station is pushed into the heap at most once and popped at most once, each heap operation costing `O(log n)`. This scales cleanly to `2 * 10^5` inputs; anything quadratic would fail well before million-scale workloads.

### Space Complexity
`O(n)` in the worst case for the max-heap, when many reachable stations remain deferred. The heap owns essentially all auxiliary space. You cannot reduce this asymptotically without giving up the ability to choose the best prior station online.

## 💡 Key Takeaways
- If the problem says “minimum number of stops/actions” and you may choose from previously encountered candidates when progress stalls, think greedy + max-heap.
- If candidates become available in sorted traversal order but selection order should be by value, that is a strong priority-queue signal.
- The expiry check is `position <= expiry`, not `currentReach <= expiry`; arrival time at a station is fixed when you pass it.
- Do not eagerly “charge” at every valid station. Stops are the optimization target, so defer usage until you are forced to extend reach.
- The transferable systems insight: separate **candidate discovery** from **candidate activation**, then use a heap to make the highest-leverage deferred decision at the latest safe point.

## 🚀 Variations & Further Practice
- Add **finite battery capacity**: now the largest charge is not always optimal because overflow wastes energy, so the greedy proof changes.
- Allow **partial charging** or per-station activation costs: selection becomes a value-density or multi-criteria optimization problem rather than pure max-charge choice.
- Extend to **2D routing with time windows**: reachability is no longer a single monotonic frontier, so the simple sorted scan breaks and graph shortest-path/state-space methods take over.