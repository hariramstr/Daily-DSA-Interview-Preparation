# Minimum Refuels to Reach the Final Checkpoint

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Heap

---

## 🗂 Problem Overview
Given a target distance, initial fuel, and a sorted list of fuel stations, determine the minimum number of refuels required to reach the target. Each station can be used at most once, and if you stop, you take all available fuel. The challenge is not reachability alone, but minimizing stops under large input sizes. Local choices are misleading, so the solution needs a greedy strategy with efficient access to the best previously passed station.

## 🌍 Engineering Impact
This pattern shows up in systems that defer expensive decisions until they become necessary while preserving optimality. Examples include streaming pipelines that buffer candidate work until backpressure forces admission, search/ranking systems that keep top-k fallback candidates, and schedulers that postpone resource allocation until capacity is tight. At scale, eager commitment wastes scarce resources; delayed commitment without the right data structure becomes too slow. The heap-backed greedy approach enables “choose the best option among everything seen so far” in logarithmic time, which is exactly what high-throughput planners, allocators, and admission controllers need.

## 🔍 Problem Statement
You need to travel `target` miles, starting with `startFuel` liters. Fuel consumption is fixed at 1 liter per mile. Stations are given as `stations[i] = [position, fuel]`, sorted by strictly increasing `position`, where each station lies before the target. When you reach a station, you may either skip it or refuel once and take all its fuel. The goal is to return the minimum number of refuels needed to reach the target, or `-1` if the trip is impossible.

Constraints are large: `stations.length` can reach `10^5`, and distances/fuel values can reach `10^9`, so quadratic exploration is not viable.

Examples:

- `target = 100, startFuel = 25, stations = [[25,25],[50,25],[75,25]]` → `3`
- `target = 120, startFuel = 50, stations = [[25,30],[40,20],[70,40],[95,30]]` → `2`

Edge cases include zero stations, zero starting fuel, and being able to reach the target without any stop.

## 🪜 How to Solve This
1. Read the problem → the objective is not “maximize remaining fuel,” but “minimize number of stops.”
2. Notice the key freedom → you do **not** need to decide at a station whether it was the right stop immediately. You only need that decision when you are about to run out of reachable distance.
3. That suggests delayed choice → as you pass stations, record their fuel as options you could have taken.
4. When you can’t reach the next checkpoint (next station or target), the best move is to retroactively choose the largest fuel amount among all stations already passed.
5. Why largest? If a refuel is unavoidable, taking the biggest available fuel extends reach the most and cannot increase the number of future stops.
6. To repeatedly retrieve the largest passed station efficiently, use a max-heap.
7. Iterate through stations in order, plus treat the target as a final checkpoint with zero fuel.
8. If at any point you need fuel but the heap is empty, the trip is impossible.

That chain leads directly to a greedy + heap solution.

## 🧩 Algorithm Walkthrough
1. **Use the Greedy + Max-Heap pattern.**  
   Greedy is appropriate because the only meaningful decision point is when current fuel is insufficient to reach the next checkpoint. The heap is the right abstraction because among all passed stations, we need the maximum fuel quickly.

2. **Track the farthest reachable distance as `fuel`.**  
   Interpret current fuel as “how far from the start I can still reach.” Initially, `fuel = startFuel`. This avoids simulating mile-by-mile movement.

3. **Process stations in increasing position, then process the target as a final station with zero fuel.**  
   For each checkpoint at `position`, first ensure it is reachable. This keeps the invariant: before handling checkpoint `i`, all stations before it have either been skipped or stored as refuel options.

4. **While `fuel < position`, refuel from the best previously passed station.**  
   Pop the maximum fuel from the heap, add it to `fuel`, and increment the stop count. This is correct because if a stop is necessary, choosing any smaller fuel first cannot reduce the total number of stops.

5. **If the heap is empty while `fuel < position`, return `-1`.**  
   No previously reachable station can extend the trip, so the target is impossible.

6. **Once the checkpoint is reachable, push its fuel into the heap.**  
   This preserves the invariant that the heap contains exactly the fuels of all reachable-but-not-yet-used stations.

7. **After the target checkpoint is processed, return the stop count.**  
   The algorithm is optimal because it refuels only when necessary and always takes the largest deferred option available.

## 📊 Worked Example
Example: `target = 120`, `startFuel = 50`, `stations = [[25,30],[40,20],[70,40],[95,30]]`

| Step | Checkpoint | Fuel Before | Heap Before | Action | Fuel After | Stops |
|---|---:|---:|---|---|---:|---:|
| 1 | 25 | 50 | [] | Reachable, push 30 | 50 | 0 |
| 2 | 40 | 50 | [30] | Reachable, push 20 | 50 | 0 |
| 3 | 70 | 50 | [30,20] | Not reachable → pop 30 | 80 | 1 |
| 4 | 70 | 80 | [20] | Reachable, push 40 | 80 | 1 |
| 5 | 95 | 80 | [40,20] | Not reachable → pop 40 | 120 | 2 |
| 6 | 95 | 120 | [20] | Reachable, push 30 | 120 | 2 |
| 7 | 120 | 120 | [30,20] | Reach target | 120 | 2 |

Key observation: we passed mile 25 before deciding to “use” it. The heap lets us defer that decision until it becomes necessary.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` where `n = stations.length`. Each station is pushed into the heap once and popped at most once, and heap operations dominate. At `10^6` scale this is still practical in optimized environments; at `10^9`, only streaming or external-memory formulations would be feasible, not in-memory heap processing.

### Space Complexity
`O(n)` in the worst case for the max-heap holding fuels from all passed but unused stations. This is owned entirely by the deferred-choice structure. It cannot be reduced asymptotically without sacrificing fast access to the best prior station.

## 💡 Key Takeaways
- If the input is ordered by position and the decision is “pick the best among everything seen so far when forced,” think greedy plus heap.
- If the objective is minimizing interventions rather than maximizing immediate gain, delayed commitment is often the right mental model.
- Treat the target as a final station with zero fuel; this removes special-case termination logic.
- Refuel only while the next checkpoint is unreachable, not whenever a station is encountered; eager refueling overcounts stops.
- In production systems, deferring commitment while maintaining a priority structure is a scalable pattern for optimal fallback selection under evolving constraints.

## 🚀 Variations & Further Practice
- **Cheapest Refueling / Minimum Cost to Reach Destination:** instead of minimizing stop count, minimize total fuel cost; the twist is that “largest fuel so far” is no longer optimal, and price-aware planning changes the greedy criterion.
- **Finite Tank Capacity:** stations still provide fuel, but the tank has a maximum size; the twist is that deferred selection interacts with capacity constraints, so simple retroactive accumulation no longer works unchanged.
- **Jump Game II / Course Scheduling with Resources:** related greedy reachability problems where the challenge is choosing when to commit to an extension versus preserving future optionality.