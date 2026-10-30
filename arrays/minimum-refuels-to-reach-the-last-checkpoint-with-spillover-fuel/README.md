# Minimum Refuels to Reach the Last Checkpoint with Spillover Fuel

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Heap

---

## 🗂 Problem Overview
Given a target distance, initial fuel, and sorted gas stations `[position, fuel]`, compute the minimum number of stations needed to reach the target. Fuel consumption is 1 liter per mile, and stopping at a station adds all of that station’s fuel to whatever remains in the tank. The challenge is choosing stops optimally under large input sizes: up to `10^5` stations and positions/fuel up to `10^9`, which rules out dynamic programming over distance.

## 🌍 Engineering Impact
This pattern shows up in systems that must defer commitment until resource shortfall is proven: streaming backpressure control, distributed job schedulers consuming burst capacity, cloud autoscaling with reserved fallback pools, and query planners selecting expensive operators only when cheaper paths fail. The core decision is not “what is available now?” but “what previously reachable option gives the largest recovery when needed?” Without this approach, systems either over-commit early, wasting scarce capacity, or make locally valid but globally expensive choices. The heap-backed greedy strategy enables online decision-making with bounded state and predictable performance at production scale.

## 🔍 Problem Statement
You need to reach mile `target` starting from mile `0` with `startFuel` liters. The car burns exactly 1 liter per mile. Each station is `stations[i] = [position_i, fuel_i]`, sorted by increasing position, where `position_i < target`. If you stop at a station, you must take all its fuel; any fuel already in the tank remains, so the new fuel is simply added.

Return the minimum number of stops required to reach `target`, or `-1` if unreachable.

Constraints:
- `1 <= target <= 10^9`
- `0 <= startFuel <= 10^9`
- `0 <= stations.length <= 10^5`
- `stations` sorted by strictly increasing position

Examples:
- `target = 100, startFuel = 25, stations = [[10,30],[20,20],[40,40],[70,25]]` → `3`
- `target = 120, startFuel = 50, stations = [[25,25],[60,30],[90,20]]` → `2`

The decisive constraint is `10^5` stations: quadratic exploration or state-by-distance DP is not viable.

## 🪜 How to Solve This
1. Start from the physical constraint: you can only refuel at stations you have already reached. That means decisions are gated by reachability, not by total global fuel.
2. Ask what causes failure → you fail only when current fuel is insufficient to reach the next checkpoint or the target.
3. At that failure point, which prior station should have been used? Since every stop costs exactly one refuel count, the best retroactive choice is the reachable station with the most fuel.
4. That immediately suggests a max-heap: as you pass stations, store their fuel as candidates. Do not commit to stopping yet.
5. Move forward in order of position. Whenever fuel is too low to reach the next position, repeatedly “activate” the largest previously seen station from the heap.
6. If the heap is empty when you need fuel, the trip is impossible.
7. This is greedy, but the right kind: delay refueling until necessary, then take the largest available refill to minimize the number of stops.

That chain leads naturally to a single pass plus a priority queue.

## 🧩 Algorithm Walkthrough
1. **Model the destination as one final checkpoint.**  
   Treat `target` like a station at position `target` with `0` fuel. This unifies the logic: every iteration asks, “Can I reach the next checkpoint?”

2. **Track current reachable distance via fuel.**  
   Let `fuel` represent how far you can still travel from your current progress model. Initially `fuel = startFuel`. Maintain `prev = 0` for the previous checkpoint position.

3. **Process checkpoints in increasing order.**  
   For each station `(pos, gas)` and finally `(target, 0)`, subtract the travel cost: `fuel -= pos - prev`. This simulates driving from the last checkpoint to the current one.

4. **If fuel drops below zero, repair using past options.**  
   While `fuel < 0`, pop the largest fuel amount from a **max-heap** of previously passed stations, add it to `fuel`, and increment `stops`.  
   **Invariant:** the heap contains exactly the fuels of all reachable-but-not-yet-used stations behind you.

5. **Fail fast if no repair exists.**  
   If `fuel < 0` and the heap is empty, no reachable station can rescue the route, so return `-1`.

6. **After reaching the checkpoint, add its fuel as a future option.**  
   Push `gas` into the heap. You are not forced to use it now; you merely record that it is now reachable.  
   This is the key greedy abstraction: **deferred choice with a max-heap**.

7. **Why this is correct.**  
   Every refuel has equal cost: one stop. When extra fuel is needed, choosing any smaller prior station before a larger one can never reduce stop count. Replacing a smaller chosen refill with a larger available one weakly improves reach with the same number of stops. That exchange argument justifies the greedy selection.

## 📊 Worked Example
Example: `target = 100`, `startFuel = 25`, `stations = [[10,30],[20,20],[40,40],[70,25]]`

| Checkpoint | Distance Traveled | Fuel After Travel | Heap Before Refuel | Action | Stops |
|---|---:|---:|---|---|---:|
| 10 | 10 | 15 | `[]` | push 30 | 0 |
| 20 | 10 | 5 | `[30]` | push 20 | 0 |
| 40 | 20 | -15 | `[30,20]` | pop 30 → fuel 15, push 40 | 1 |
| 70 | 30 | -15 | `[40,20]` | pop 40 → fuel 25, push 25 | 2 |
| 100 | 30 | -5 | `[25,20]` | pop 25 → fuel 20 | 3 |

Trace summary:
1. Reach 10 and 20 without refueling.
2. To reach 40, current fuel is insufficient, so use the largest previously reachable station: 30.
3. To reach 70, again use the largest available prior station: 40.
4. To reach 100, use 25.
5. Total minimum stops = `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` where `n = stations.length`. Each station is pushed into the heap once and popped at most once, and each heap operation costs `O(log n)`. At `10^6` elements this is still practical in optimized runtimes; at `10^9`, no per-element algorithm is feasible, so input size is the real bound.

### Space Complexity
`O(n)` for the max-heap storing fuels of passed but unused stations. This is the dominant auxiliary structure. It cannot be reduced asymptotically without giving up the ability to retrieve the best prior refill in sublinear time.

## 💡 Key Takeaways
- If the problem asks for the **minimum number of actions** and each action has equal cost, look for a greedy rule that maximizes benefit per action.
- If choices become available over time and can be selected later, that is a strong signal for a **heap of deferred candidates**.
- Do not refuel when you arrive at a station by default; push its fuel into the heap and decide only when you are forced to.
- Include the destination as a final checkpoint, or you will miss the last stretch and undercount failures/refuels.
- The broader design lesson: defer irreversible decisions until constraint violation, then resolve using the best previously admissible option.

## 🚀 Variations & Further Practice
- **Finite tank capacity:** you can no longer always take all fuel without overflow, which breaks the simple exchange argument and changes the state space.
- **Stations not sorted or arriving as a stream:** now preprocessing or online ordering guarantees matter, and correctness depends on how reachability is maintained.
- **Different stop costs or partial refuels allowed:** minimizing stop count becomes minimizing weighted cost, often requiring dynamic programming or shortest-path formulations instead of pure greedy.