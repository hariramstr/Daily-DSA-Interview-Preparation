# Employee Shift Overlap Detector

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, Interval Overlap, String Parsing

---

## 🗂 What Is This Problem? *(For Everyone)*

Given a list of employee clock-in and clock-out records, find every pair of employees who were both at work at the same time — even briefly. Each record is a simple log entry with a name, an action (in or out), and a time. The goal is to return every pair of employees whose working periods overlapped at any point during the day.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Shift overlap detection is a practical, everyday need across many industries. Hospitals must ensure enough nurses are on the floor simultaneously. Warehouses track whether two workers share a forklift zone, creating a safety hazard. Payroll systems flag double-billing when contractors log overlapping hours on multiple projects. Retail managers use overlap data to avoid overstaffing during slow periods, directly cutting labour costs. Even ride-sharing platforms like Uber use interval logic to detect when two drivers are serving the same zone — optimising coverage and reducing idle time.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Imagine a shared office with a sign-in sheet at the door. Each person writes their name and the time they arrive, then signs out when they leave. At the end of the day, the manager wants to know: which pairs of people were ever in the office at the same time? Think of it like checking whether two guests' hotel stays overlapped — if one checks in before the other checks out, they were there together.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a list of log strings in the format `"employeeId action timestamp"` — where `action` is `"in"` or `"out"` and `timestamp` is an integer (minutes since midnight, `0 ≤ timestamp ≤ 1440`) — reconstruct each employee's work intervals and return all unique pairs `[id1, id2]` (with `id1 < id2` lexicographically) whose intervals overlap at any point.

Two intervals `[a, b]` and `[c, d]` overlap if and only if `a < d && c < b` (strict inequality, so touching endpoints do not count as overlap).

**Constraints:** `1 ≤ logs.length ≤ 1000`, employee IDs are lowercase strings up to length 10, no two entries share the same timestamp for the same employee, and every check-in is always followed by a check-out before the next check-in.

**Example 1:**
- Input: `["alice in 100", "bob in 120", "alice out 200", "bob out 250"]`
- Output: `[["alice", "bob"]]`

**Example 2:**
- Input: `["alice in 50", "alice out 90", "bob in 100", "bob out 150"]`
- Output: `[]`

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Parse the logs into a hash map.** Iterate through every log entry and split each string by spaces to extract `employeeId`, `action`, and `timestamp`. Store each employee's data in a hash map keyed by their ID. This gives O(1) lookup per employee.

2. **Reconstruct intervals per employee.** For each employee, pair their `"in"` timestamps with the corresponding `"out"` timestamps to form intervals `[checkIn, checkOut]`. Since the problem guarantees every check-in is followed by a check-out, we can use a temporary variable to hold the open check-in time until its matching check-out arrives.

3. **Compare every pair of employees.** Once all intervals are built, iterate over every unique pair of employees (using a nested loop over the hash map keys). For each pair, check all combinations of their intervals for overlap.

4. **Apply the overlap condition.** Two intervals `[a, b]` and `[c, d]` overlap if `a < d && c < b`. This single condition elegantly handles all cases — partial overlap, one interval containing the other, etc. Touching endpoints (e.g., one leaves at 90, another arrives at 90) are treated as non-overlapping.

5. **Collect and return results.** If any interval pair overlaps, add `[min(id1,id2), max(id1,id2)]` to the result set. Using a set prevents duplicate pairs when employees have multiple overlapping intervals.

---

## 📊 Worked Example *(For Developers)*

**Input:** `["alice in 100", "bob in 120", "alice out 200", "bob out 250"]`

| Step | Action | State |
|------|--------|-------|
| Parse log 1 | `alice in 100` | `map = { alice: {openIn: 100} }` |
| Parse log 2 | `bob in 120` | `map = { alice: {openIn: 100}, bob: {openIn: 120} }` |
| Parse log 3 | `alice out 200` | `alice intervals = [[100, 200]]` |
| Parse log 4 | `bob out 250` | `bob intervals = [[120, 250]]` |
| Compare pair | `alice [100,200]` vs `bob [120,250]` | Check: `100 < 250` ✅ AND `120 < 200` ✅ → **Overlap!** |
| Output | Add pair | `result = [["alice", "bob"]]` |

Alice's interval ends at 200, Bob's starts at 120 — they share the window 120–200, confirming the overlap.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n + E²·I²)** where `n` is the number of log entries, `E` is the number of unique employees, and `I` is the maximum number of intervals per employee. In practice, with the given constraints (≤1000 logs), this is effectively **O(n²)** at worst — fast enough for thousands of employees without any noticeable delay.

### Space Complexity

**O(n)** extra memory is used to store the parsed intervals in the hash map. The result list is at most O(E²) pairs. With at most 1000 log entries, memory usage stays well within practical limits on any modern device.

---

## 💡 Key Takeaways *(For Everyone)*

- **Overlap detection is a universal business tool** — from scheduling nurses to auditing contractor timesheets, knowing who was "present" simultaneously has direct cost and safety implications.
- **Touching endpoints are not overlaps** — if Alice leaves at 90 and Bob arrives at 90, they never shared a moment; this distinction matters in payroll and compliance systems.
- **Hash maps make grouping fast** — by keying intervals to employee IDs, we avoid repeated scans of the entire log and achieve O(1) per-employee lookup.
- **The two-condition overlap test (`a < d && c < b`) is a classic pattern** — memorise it; it elegantly handles every overlap case including containment and partial overlap in a single line.
- **Sorting logs by timestamp first** is an optional optimisation that enables early-exit strategies and is worth considering when the number of employees or intervals grows large.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Count overlapping minutes:** Instead of just detecting *whether* two employees overlap, calculate the *total number of minutes* they were simultaneously checked in. This requires computing interval intersection lengths.
- **Variation 2 — Multi-day schedules:** Extend the problem so timestamps span multiple days (e.g., `dayNumber * 1440 + minutesSinceMidnight`). How does your parsing and interval comparison logic need to change?
- **Variation 3 — Find the busiest moment:** Rather than pairs, find the single minute during the day when the most employees were simultaneously checked in — a classic "meeting rooms" sweep-line problem.

---