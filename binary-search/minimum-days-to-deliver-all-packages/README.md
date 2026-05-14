# Minimum Days to Deliver All Packages

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Prefix Sum, Arrays

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a delivery truck that must ship packages in a fixed order. Each day, the truck can carry only a limited number of packages and a limited total weight. The goal is to figure out the **fewest number of days** needed to deliver every single package, given those two real-world constraints working together.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This problem mirrors challenges faced daily by logistics companies like FedEx, UPS, and Amazon. When routing deliveries, planners must balance truck capacity (weight limits) and route constraints (consecutive stops) to minimise fleet operating days — directly reducing fuel costs, driver overtime, and customer wait times. The same optimisation logic powers warehouse dispatch systems, manufacturing assembly lines, and even content delivery pipelines where batching tasks efficiently translates into measurable cost savings and faster service-level agreement (SLA) compliance.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a conveyor belt at a warehouse with packages lined up in a strict order. A truck arrives each morning and loads packages from the front of the belt — but it can only take so many packages at once, and the combined weight must stay under a limit. You cannot skip a heavy package and grab a lighter one further back. How many truck trips (days) do you need to clear the entire belt?

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an integer array `packages` of length `n`, an integer `k`, and an integer `capacity`, determine the **minimum number of days** to deliver all packages under these rules:

- Each day, the truck delivers a **contiguous subarray** of packages starting where the previous day ended.
- At most **`k` packages** may be loaded per day.
- The **sum of weights** loaded per day must not exceed `capacity`.
- Every individual package weight is guaranteed to be ≤ `capacity`.

**Example 1:**
- Input: `packages = [3,2,4,1,5,2,3]`, `k = 3`, `capacity = 7`
- Output: `4`

**Example 2:**
- Input: `packages = [1,2,3,4,5]`, `k = 2`, `capacity = 6`
- Output: `3`

**Constraints:** `1 ≤ n ≤ 10⁵`, `1 ≤ packages[i] ≤ 500`, `1 ≤ k ≤ n`, `1 ≤ capacity ≤ 10⁷`

---

## 🧩 Approach: How We Solve It *(For Developers)*

The key insight is to **binary search on the answer** (number of days `d`) rather than simulate every possible grouping directly.

1. **Establish the search space.** The minimum possible days is `1` (if everything fits in one trip) and the maximum is `n` (one package per day). Our answer lies somewhere in this range.

2. **Binary search on `d`.** Pick the midpoint `mid` of the current range. Ask: *"Can we deliver all packages in exactly `mid` days?"* This transforms an optimisation problem into a yes/no decision problem, which is much easier to evaluate.

3. **Greedy feasibility check (`canDeliver(d)`).** Simulate delivery greedily: starting from the first package, load as many consecutive packages as possible into today's truck without exceeding `k` packages or `capacity` weight. When either limit is hit, start a new day. Count the total days used.

4. **Use prefix sums for fast range-sum queries.** Pre-compute a prefix sum array so that the weight of any subarray `[i, j]` can be computed in O(1) instead of O(n), keeping the feasibility check efficient.

5. **Narrow the binary search.** If `canDeliver(mid)` returns `true`, the answer might be even smaller — search the left half. If `false`, we need more days — search the right half. Repeat until the range collapses to the minimum valid `d`.

---

## 📊 Worked Example *(For Developers)*

**Input:** `packages = [1,2,3,4,5]`, `k = 2`, `capacity = 6`

**Prefix sums:** `[0, 1, 3, 6, 10, 15]`

| Binary Search Step | `lo` | `hi` | `mid` | `canDeliver(mid)`? | Reasoning |
|--------------------|------|------|-------|--------------------|-----------|
| 1 | 1 | 5 | 3 | ✅ Yes | Day 1: [1,2]=3; Day 2: [3]=3 (4 would exceed cap); Day 3: [4,5]=9 > 6 → [4]=4; Day 4: [5]=5 → 4 days needed, not 3. Actually 3 days: [1,2],[3],[4,5]? [4,5]=9>6 → No. Days=[1,2],[3],[4],[5]=4. Re-check mid=3: false → search right. |
| 2 | 4 | 5 | 4 | ✅ Yes | [1,2],[3],[4],[5] = 4 days ≤ 4 → true |
| 3 | 4 | 4 | — | — | Converged |

**Answer:** `4`

> **Note:** Example 2's stated output of `3` requires re-examining constraints; greedy simulation with `k=2, capacity=6` yields **4 days**: `[1,2]`, `[3]`, `[4]`, `[5]`.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n log n)** — The binary search runs O(log n) iterations over the answer range. Each iteration performs a greedy O(n) scan of all packages. Pre-building the prefix sum array is O(n). At 100,000 packages, this executes in microseconds, making it production-safe at scale.

### Space Complexity

**O(n)** — We allocate one prefix sum array of length `n + 1`. No recursive call stack or additional data structures are needed, so memory usage grows linearly and predictably with input size.

---

## 💡 Key Takeaways *(For Everyone)*

- **Minimising delivery days directly reduces operational costs** — fewer days means less fuel, fewer driver hours, and faster customer fulfilment.
- **Two simultaneous constraints (count + weight) are common in real logistics** — any scheduling or batching system faces this dual-limit challenge.
- **Binary search on the answer is a powerful pattern** — when the answer is monotonic (more days always makes delivery easier), you can search for the minimum valid value rather than enumerate all possibilities.
- **Greedy feasibility checks pair naturally with binary search** — the greedy "load as much as possible each day" strategy gives the tightest packing, ensuring the feasibility check is both correct and efficient.
- **Prefix sums convert repeated range-sum queries from O(n) to O(1)** — a small O(n) pre-computation investment that pays dividends across every binary search iteration.

---

## 🚀 Try It Yourself *(For Developers)*

- **Classic prerequisite — Ship Within D Days** (LeetCode #1011): Binary search on capacity instead of days; a simpler single-constraint version that builds the same muscle memory.
- **Add a third constraint** — modify the problem so the truck also has a maximum number of *trips per week*, and find the minimum number of weeks. This forces you to nest feasibility checks.
- **Reverse the problem** — given a fixed number of days `d`, find the **minimum capacity** needed to deliver all packages within `d` days using at most `k` packages per day, and compare how the binary search bounds shift.

---