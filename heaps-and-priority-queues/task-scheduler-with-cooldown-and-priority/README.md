# Task Scheduler with Cooldown and Priority

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** Heap, Priority Queue, Greedy, Simulation

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a single worker who can only handle one job at a time and needs a short break between jobs. Each job becomes available at a different moment and carries a different level of urgency. The question is: in what order should the worker tackle jobs to always pick the most urgent one available? This problem asks us to figure out exactly that sequence.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This scheduling pattern appears everywhere in modern technology. Operating systems use it to decide which program gets CPU time next. Hospital triage systems use priority-based scheduling to treat the most critical patients first. Cloud platforms like AWS and Azure use similar logic to allocate computing resources across thousands of competing jobs, directly reducing idle server time and cutting infrastructure costs. Airlines use priority queues to manage gate assignments. Getting this right means faster response times, lower operational costs, and better user experiences — all measurable business outcomes.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a busy doctor's office with one doctor. Patients arrive at different times and have different levels of urgency. The doctor can only see one patient at a time and needs a short rest between appointments. When the doctor is free, they should always see the most urgent patient currently waiting. We need to record the order in which patients are seen throughout the day.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a list of tasks where `tasks[i] = [releaseTime, priority]` and an integer `cooldown`, simulate CPU scheduling under these rules:

- The CPU processes one task per time unit.
- A task is **available** at any time step `t >= releaseTime`.
- After finishing at time `t`, the CPU cannot start again until `t + cooldown + 1`.
- At each available time step, greedily pick the highest-priority task. Break ties by smallest `releaseTime`, then smallest original index.
- Return the 0-indexed completion order.

**Constraints:** `1 <= tasks.length <= 10^4`, `0 <= releaseTime <= 10^9`, `1 <= priority <= 10^9`, `0 <= cooldown <= 100`.

**Example 1:**
```
Input:  tasks = [[0,3],[0,5],[2,2]], cooldown = 1
Output: [1, 0, 2]
```
**Example 2:**
```
Input:  tasks = [[5,1],[0,2],[0,3]], cooldown = 0
Output: [2, 1, 0]
```

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Sort tasks by release time** — Sort a copy of the task list (keeping original indices) by `releaseTime`. This lets us efficiently discover which tasks have become available as simulated time advances.

2. **Initialize a max-heap** — Use a max-heap (priority queue) keyed on `(-priority, releaseTime, originalIndex)`. Negating priority turns Python's min-heap into a max-heap, so the highest-priority task is always at the top.

3. **Simulate time step by step** — Track `currentTime` (when the CPU next becomes free) and a pointer into the sorted task list. At each scheduling moment:
   - **Advance time smartly**: If the heap is empty and no tasks have arrived yet, jump `currentTime` directly to the next task's release time — avoid simulating idle ticks one by one.
   - **Load available tasks**: Push all tasks whose `releaseTime <= currentTime` onto the heap.
   - **Pick the best task**: Pop the heap to get the highest-priority available task.
   - **Record and advance**: Append its original index to the result list, then set `currentTime = currentTime + cooldown + 1`.

4. **Repeat until all tasks are scheduled** — Continue until the result list contains every task.

5. **Why greedy works here** — Because each task takes exactly 1 unit of time, choosing the locally best (highest-priority) available task at every decision point is globally optimal. There is no benefit to saving a high-priority task for later.

---

## 📊 Worked Example *(For Developers)*

Using **Example 1**: `tasks = [[0,3],[0,5],[2,2]], cooldown = 1`

Sorted tasks (with original indices): `[(0,3,0), (0,5,1), (2,2,2)]`

| Step | `currentTime` | Tasks Loaded into Heap | Heap Contents (–pri, rel, idx) | Task Picked | Result So Far | Next Free Time |
|------|--------------|------------------------|-------------------------------|-------------|---------------|----------------|
| 1 | 0 | Tasks 0 & 1 (release ≤ 0) | `(-5,0,1), (-3,0,0)` | Task 1 (priority 5) | `[1]` | 0+1+1 = **2** |
| 2 | 2 | Task 2 (release 2 ≤ 2) | `(-3,0,0), (-2,2,2)` | Task 0 (priority 3) | `[1,0]` | 2+1+1 = **4** |
| 3 | 4 | *(none new)* | `(-2,2,2)` | Task 2 (priority 2) | `[1,0,2]` | 4+1+1 = **6** |

**Final Output:** `[1, 0, 2]` ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n log n)** — Sorting the tasks costs O(n log n). Each task is pushed onto and popped from the heap exactly once, each operation costing O(log n). The time-jumping trick ensures we never spin through idle cycles, so the total number of scheduling steps equals exactly `n`. This scales comfortably to 10,000 tasks.

### Space Complexity

**O(n)** — The heap holds at most `n` tasks simultaneously, and the sorted task list and result array each require O(n) space. No additional data structures grow with input size, making memory usage predictable and manageable.

---

## 💡 Key Takeaways *(For Everyone)*

- **Greedy priority scheduling is everywhere** — From hospital ERs to cloud computing, always serving the highest-priority available item is a proven, real-world strategy that reduces costly delays.
- **Idle time is a hidden cost** — Jumping directly to the next task's release time (rather than simulating every idle tick) mirrors how real systems avoid wasting compute cycles, saving both time and money.
- **A max-heap is the right tool** — When you repeatedly need "the best item available so far," a heap gives you that in O(log n) per operation, far better than scanning the full list each time (O(n)).
- **Tie-breaking rules matter for correctness** — Real schedulers must define deterministic tie-breaking (by release time, then index here) to produce consistent, reproducible results — critical for debugging and auditing.
- **Time-jumping prevents TLE** — Naively simulating every time unit would be catastrophic when release times reach 10⁹; advancing `currentTime` directly to the next event is a key optimization pattern in simulation problems.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variable task durations** — Modify the problem so each task takes `duration[i]` time units instead of 1. How does this change the heap key and the time-advancement logic? *(Hint: look up the classic CPU Scheduling / Task Scheduler LeetCode #621.)*
- **Multiple CPUs** — Extend the solution to `k` parallel CPUs, each with its own cooldown. How would you manage `k` independent "next free time" values efficiently? *(Hint: use a second min-heap to track CPU availability.)*
- **Dynamic task arrival** — What if new tasks can be added to the queue at runtime (a stream)? Explore how to adapt the simulation for online scheduling where the full task list is not known upfront.

---