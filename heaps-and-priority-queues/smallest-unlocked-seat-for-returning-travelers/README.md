# Smallest Unlocked Seat for Returning Travelers

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, sorting

---

## 🗂 Problem Overview
Given arrival and departure times for `n` travelers, assign seats so each arriving traveler gets the smallest currently unoccupied seat number. Seats freed at time `t` can be reused immediately by arrivals at `t`. Return the seat assigned to `targetTraveler`. The challenge is scale: with up to `100000` travelers, repeatedly scanning seats to find the smallest free one is too slow. The solution needs efficient reuse of released seat numbers while processing arrivals in chronological order.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must allocate the lowest available resource identifier under churn: connection slot assignment in proxies, worker-slot reuse in schedulers, room or gate assignment in transportation systems, and compact ID reuse in memory managers or container orchestrators. At small scale, linear scans are acceptable; at production scale, they create latency spikes and poor cache behavior under bursty arrivals and releases. The heap-based design separates “currently occupied until time t” from “available for immediate reuse,” enabling predictable `O(log n)` updates, stable throughput, and deterministic minimal-ID allocation without global rescans.

## 🔍 Problem Statement
You are given two arrays, `arrivals` and `departures`, where traveler `i` arrives at `arrivals[i]` and leaves at `departures[i]`. All arrival times are distinct, `arrivals[i] < departures[i]`, and `1 <= n <= 100000`. When a traveler arrives, assign the smallest seat number not currently occupied. If no released seat exists, allocate the next unused seat number. If someone leaves at time `t`, that seat is immediately available for another traveler arriving at `t`.

Return the seat number assigned to `targetTraveler`.

Examples:

- `arrivals = [1,4,2]`, `departures = [5,6,3]`, `targetTraveler = 1` → `1`
- `arrivals = [3,8,5,6]`, `departures = [10,9,7,11]`, `targetTraveler = 3` → `2`

The key constraint is `n = 100000`: any approach that scans all seats on each arrival degrades to quadratic behavior and will not hold up.

## 🪜 How to Solve This
1. Read the problem → the assignment order is driven by arrival time, not traveler index. So the first move is to sort travelers by arrival.
2. Notice two independent questions happen at each arrival:
   - Which occupied seats have become free by now?
   - Among all free seats, what is the smallest number?
3. “Who becomes free next?” suggests a min-heap ordered by departure time.
4. “What is the smallest reusable seat?” suggests another min-heap ordered by seat number.
5. Process travelers in arrival order:
   - Before seating the current traveler, release every occupied seat whose departure time is `<= currentArrival`.
   - Releasing means removing it from the occupied heap and pushing its seat number into the free-seat heap.
6. Then assign a seat:
   - If a free seat exists, pop the smallest one.
   - Otherwise, issue the next new seat number.
7. The moment you process `targetTraveler`, return that seat immediately. No need to finish the simulation.

The core insight is that sorting gives time order, and the two heaps each answer one “minimum” query efficiently.

## 🧩 Algorithm Walkthrough
1. **Preprocess travelers by arrival time.**  
   Build tuples `(arrival, departure, travelerIndex)` and sort by `arrival`. This is correct because seat assignment depends only on chronological arrivals. The invariant after sorting: processing order matches the lounge timeline.

2. **Maintain an occupied-seat min-heap keyed by departure time.**  
   Each heap entry is `(departureTime, seatNumber)`. This lets us efficiently find the next seat to become available. Invariant: every currently occupied seat appears exactly once here.

3. **Maintain an available-seat min-heap keyed by seat number.**  
   When a traveler leaves, their seat number is pushed here. This encodes the rule “assign the smallest unoccupied seat.” Invariant: this heap contains exactly the reusable seats not currently occupied.

4. **Track `nextUnusedSeat`.**  
   If no reusable seat exists, assign `nextUnusedSeat` and increment it. This avoids preallocating seat IDs and guarantees compact numbering.

5. **For each arriving traveler, release all seats with `departure <= arrival`.**  
   Pop from the occupied heap until its minimum departure exceeds the current arrival. Each popped seat is now free and must be pushed into the available-seat heap. This is correct because seats become reusable immediately at equal timestamps.

6. **Assign the seat.**  
   If the available-seat heap is non-empty, pop its minimum; otherwise use `nextUnusedSeat`. Then push `(departure, assignedSeat)` into the occupied heap. The invariant is restored: occupied seats are tracked by leave time, free seats by seat number.

7. **Return early for `targetTraveler`.**  
   As soon as the target traveler is assigned a seat, return it. The simulation state up to that event is sufficient; later arrivals cannot change the already assigned seat.

This is the classic **dual min-heap simulation** pattern: one heap for resource release order, one for smallest reusable resource selection.

## 📊 Worked Example
Example: `arrivals = [1,4,2]`, `departures = [5,6,3]`, `targetTraveler = 1`

Sorted travelers by arrival:

| Step | Traveler | Arrive | Release before seating | Free seats | Assigned | Occupied after step |
|---|---:|---:|---|---|---:|---|
| 1 | 0 | 1 | none | `[]` | 0 | `[(5,0)]` |
| 2 | 2 | 2 | none | `[]` | 1 | `[(3,1),(5,0)]` |
| 3 | 1 | 4 | release `(3,1)` since `3 <= 4` | `[1]` | 1 | `[(5,0),(6,1)]` |

Trace:
1. Traveler 0 arrives first, no free seats exist, so seat `0` is created.
2. Traveler 2 arrives at `2`, seat `0` is still occupied, so seat `1` is created.
3. Before traveler 1 arrives at `4`, traveler 2 has already left at `3`, freeing seat `1`.
4. The smallest available seat is now `1`, so traveler 1 gets seat `1`.

Answer: `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)`. Sorting travelers costs `O(n log n)`, and each traveler is inserted into and removed from heaps at most once, adding another `O(n log n)`. This scales well for `10^6` elements in optimized environments; at `10^9`, even sorting alone becomes operationally impractical without distribution or external-memory techniques.

### Space Complexity
`O(n)`. The sorted traveler list plus the two heaps together hold at most `n` entries. This can’t be meaningfully reduced below linear without sacrificing either correctness or the ability to answer “next release” and “smallest free seat” efficiently.

## 💡 Key Takeaways
- If a problem says “smallest available resource” under dynamic release/reuse, think min-heap of reusable IDs immediately.
- If events happen over time and releases affect future allocations, sort events and simulate in chronological order.
- Use `departure <= arrival`, not `<`, because seats freed at time `t` are reusable by arrivals at the same time.
- Keep two heaps with different orderings; trying to make one structure answer both “earliest release” and “smallest free ID” usually leads to bugs or worse complexity.
- The transferable design insight is separation of concerns: one structure models lifecycle timing, another models allocation policy.

## 🚀 Variations & Further Practice
- **Return seat assignments for all travelers, not just one.** Same pattern, but you must persist each assignment and finish the full simulation.
- **Support online arrivals and departures without pre-sorting.** Harder because events arrive incrementally; you need a live event stream model rather than an offline sorted pass.
- **Allocate the smallest available resource across multiple lounges or zones with constraints.** The twist is constrained eligibility, which often requires indexed heaps, balanced trees, or segment trees instead of a single global free-seat heap.