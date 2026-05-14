# Closest Patient Appointments

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** Heap, Priority Queue, Sorting

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a hospital has a list of scheduled appointment times throughout the day. A doctor becomes available at a specific moment and needs to see the next few patients whose appointment times are closest to that moment. This problem is about efficiently finding those best-matching appointment times from a potentially long list — quickly and accurately.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Efficient appointment matching is critical in healthcare, logistics, and customer service. When a hospital system can instantly surface the most relevant appointments near a given time, staff spend less time searching and more time caring for patients. The same algorithm powers ride-sharing apps like Uber (finding the nearest available driver), e-commerce platforms (surfacing the most relevant products), and airline systems (rebooking passengers on the closest available flights). Faster matching means lower operational costs, reduced patient wait times, and a measurably better experience for both staff and customers.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a train station departures board showing dozens of train times. You arrive at 2:30 PM and want the two trains whose departure times are closest to yours — whether slightly before or after. If two trains are equally close, you prefer the earlier one. This problem asks you to find exactly those best-matching times from a list, then hand them back in chronological order.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an integer array `appointments` of scheduled times (in minutes from midnight), a query time `t`, and an integer `k`, return the `k` appointment times whose absolute difference from `t` is smallest. Ties in distance are broken by preferring the smaller appointment time. Return the result sorted in ascending order.

**Constraints:**
- `1 <= appointments.length <= 10^4`
- `0 <= appointments[i] <= 1440` (minutes in a day, all distinct)
- `0 <= t <= 1440`
- `1 <= k <= appointments.length`

**Examples:**

| Input | t | k | Output |
|---|---|---|---|
| `[30, 120, 200, 450, 800]` | `150` | `2` | `[120, 200]` |
| `[60, 180, 300, 420]` | `240` | `3` | `[60, 180, 300]` |

In Example 1, distances are `[120, 30, 50, 300, 650]`; the two smallest belong to times `120` and `200`.

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use a **max-heap of size k** to efficiently track the `k` closest appointments seen so far.

1. **Define a comparator.** For each appointment, compute its distance from `t` as `abs(appointment - t)`. When two appointments share the same distance, the one with the *larger* time value is considered "worse" (we prefer smaller times on ties). The max-heap will always evict the worst candidate.

2. **Iterate through all appointments.** For each appointment time, push it onto the heap using the tuple `(distance, appointment)` — but negate both values because Python's `heapq` is a min-heap. Storing `(-distance, -appointment)` lets the heap treat the largest distance (worst match) as the highest-priority item to remove.

3. **Maintain heap size k.** After each push, if the heap exceeds size `k`, pop the root — which is the appointment with the greatest distance (or largest time on a tie). This ensures the heap always holds exactly the `k` best candidates.

4. **Extract results.** Once all appointments are processed, extract the appointment values from the heap, reverse the negation applied in step 2, and collect them into a list.

5. **Sort ascending.** Sort the resulting `k` values in ascending order before returning, since heap order does not guarantee sorted output.

This approach avoids sorting the entire array upfront and processes each element in `O(log k)` time.

---

## 📊 Worked Example *(For Developers)*

**Input:** `appointments = [30, 120, 200, 450, 800]`, `t = 150`, `k = 2`

| Step | Appointment | Distance from 150 | Heap (dist, time) | Action |
|---|---|---|---|---|
| 1 | 30 | 120 | `[(120, 30)]` | Push; size ≤ k, no eviction |
| 2 | 120 | 30 | `[(120, 30), (30, 120)]` | Push; size ≤ k, no eviction |
| 3 | 200 | 50 | `[(120, 30), (30, 120), (50, 200)]` | Push; size > k → evict worst: `(120, 30)` |
| 4 | 450 | 300 | `[(300, 450), (30, 120), (50, 200)]` | Push; size > k → evict worst: `(300, 450)` |
| 5 | 800 | 650 | `[(650, 800), (30, 120), (50, 200)]` | Push; size > k → evict worst: `(650, 800)` |

**Remaining heap:** distances `30 → time 120`, `50 → time 200`
**Sorted output:** `[120, 200]` ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n log k)** — We iterate over all `n` appointments once. Each heap push/pop costs `O(log k)`. Since `k ≤ n`, this is significantly faster than a full sort (`O(n log n)`) when `k` is small relative to `n`. At scale with 10,000 appointments and `k = 5`, this is a meaningful saving.

### Space Complexity

**O(k)** — The heap never holds more than `k + 1` elements at any time. Regardless of how large the appointments list grows, our working memory stays bounded by the number of results requested — highly efficient for memory-constrained environments.

---

## 💡 Key Takeaways *(For Everyone)*

- **Real-world scheduling systems** — from hospitals to airlines — rely on exactly this kind of "find the k closest" logic to surface relevant options quickly without overwhelming staff or systems.
- **Smaller k means bigger savings** — when you only need a handful of results from a large dataset, a heap-based approach is dramatically faster and cheaper than sorting everything.
- **A max-heap of size k is the classic pattern** for "keep the k best items seen so far" — master this pattern and it applies across dozens of problem types.
- **Tie-breaking must be encoded into the comparator** — always think carefully about what "equal" means in your domain; here, preferring the earlier time is a deliberate business rule, not an afterthought.
- **Negation trick for Python heaps** — since Python only provides a min-heap via `heapq`, storing negated values `(-distance, -time)` is the idiomatic way to simulate a max-heap without a custom class.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Weighted proximity:** Modify the problem so that appointments *before* `t` are penalised more heavily than those after (e.g., a missed appointment costs twice as much). How does your comparator change?
- **Variation 2 — Streaming appointments:** Instead of a fixed array, appointments arrive one at a time in a live stream. Can you maintain the `k` closest appointments seen so far without reprocessing the entire list on each new arrival?
- **Variation 3 — Multiple query times:** Given a list of query times `[t1, t2, ..., tm]`, efficiently find the single appointment closest to *any* query time. Explore how sorting plus binary search compares to a heap-based approach here.

---