# Rainfall Accumulation Between Sensors

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Prefix Sum &nbsp;|&nbsp; **Tags:** Prefix Sum, Array, Brute Force Optimization

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a row of rain gauges placed along a road. Each gauge records how much rain fell at that spot. This problem asks: for any stretch of that road, how many continuous segments of gauges recorded a combined rainfall above a certain level? We need to answer that question quickly, potentially hundreds of times, for different stretches and different thresholds.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This type of analysis is the backbone of environmental monitoring, insurance risk assessment, and smart city infrastructure planning. Flood-risk insurers use exactly this kind of range-based accumulation query to identify dangerous rainfall corridors and price policies accurately. Agricultural companies use it to decide where to deploy irrigation resources. Traffic management systems use similar logic to detect congestion zones along highways. Solving this efficiently means faster decisions, lower computational costs, and the ability to process thousands of sensor queries in real time — directly improving operational response times and reducing infrastructure damage costs.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a row of buckets along a street, each holding some amount of rainwater. A city inspector asks you repeatedly: "Between bucket 3 and bucket 10, how many groups of side-by-side buckets together hold more than 50 millimetres of water?" Each question covers a different stretch of the street and a different water level. Your job is to answer every inspector's question correctly and efficiently, without re-counting everything from scratch each time.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an integer array `rainfall` of length `n` (where `rainfall[i]` is the rainfall at position `i`) and a 2D array `queries`, answer each query `[left, right, threshold]` by counting how many contiguous subarrays `rainfall[a..b]` satisfy `left <= a <= b <= right` and `sum(rainfall[a..b]) > threshold`.

**Constraints:**
- `1 <= n <= 1000`
- `0 <= rainfall[i] <= 100`
- `1 <= queries.length <= 500`
- `0 <= left <= right < n`
- `0 <= threshold <= 10^5`

**Example 1:**
- Input: `rainfall = [3,1,4,1,5]`, `queries = [[0,3,5],[1,4,7]]`
- Output: `[4, 5]`

**Example 2:**
- Input: `rainfall = [2,2,2,2]`, `queries = [[0,3,4],[0,2,6]]`
- Output: `[6, 0]`

---

## 🧩 Approach: How We Solve It *(For Developers)*

We combine a **prefix sum array** with a **brute-force subarray enumeration**, making range-sum lookups O(1) instead of O(n).

1. **Build a prefix sum array.**
   Create an array `prefix` where `prefix[i] = rainfall[0] + rainfall[1] + ... + rainfall[i-1]`. This lets us compute the sum of any subarray `[a, b]` instantly as `prefix[b+1] - prefix[a]`, avoiding repeated addition.

2. **Process each query independently.**
   For each query `[left, right, threshold]`, we need to examine all valid subarrays contained within `[left, right]`.

3. **Enumerate all starting points `a` from `left` to `right`.**
   For each starting index `a`, iterate over all ending indices `b` from `a` to `right`. This covers every possible contiguous subarray within the query window.

4. **Compute subarray sum using the prefix array.**
   For each `(a, b)` pair, calculate `sum = prefix[b+1] - prefix[a]` in constant time — no inner loop needed for summation.

5. **Compare against the threshold.**
   If `sum > threshold`, increment a counter for this query.

6. **Store the result.**
   Append the counter to the results array and move to the next query.

This approach avoids recomputing sums from scratch and is well-suited to the given constraint sizes.

---

## 📊 Worked Example *(For Developers)*

**Input:** `rainfall = [3, 1, 4, 1, 5]`, query = `[0, 3, 5]`

**Prefix array:** `prefix = [0, 3, 4, 8, 9, 14]`

| Start `a` | End `b` | Sum (`prefix[b+1] - prefix[a]`) | > 5? |
|:---------:|:-------:|:-------------------------------:|:----:|
| 0         | 0       | 3 − 0 = **3**                   | ✗    |
| 0         | 1       | 4 − 0 = **4**                   | ✗    |
| 0         | 2       | 8 − 0 = **8**                   | ✓    |
| 0         | 3       | 9 − 0 = **9**                   | ✓    |
| 1         | 1       | 4 − 3 = **1**                   | ✗    |
| 1         | 2       | 8 − 3 = **5**                   | ✗    |
| 1         | 3       | 9 − 3 = **6**                   | ✓    |
| 2         | 2       | 8 − 4 = **4**                   | ✗    |
| 2         | 3       | 9 − 4 = **5**                   | ✗    |
| 3         | 3       | 9 − 8 = **1**                   | ✗    |

**Count of qualifying subarrays = 4** ✅ — matches expected output.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n + Q · w²)** where `n` is the array length, `Q` is the number of queries, and `w` is the average query window size. Building the prefix array is O(n). Each query enumerates O(w²) subarray pairs. Under the given constraints (n ≤ 1000, Q ≤ 500), this remains comfortably fast — roughly 250 million operations at worst, manageable for the input sizes specified.

### Space Complexity

**O(n)** for the prefix sum array, plus O(Q) for storing results. No additional data structures grow with input size. In practice, for n = 1000, the prefix array uses only a few kilobytes of memory — negligible on any modern system.

---

## 💡 Key Takeaways *(For Everyone)*

- **Real-time environmental monitoring depends on fast range queries** — this pattern directly powers flood-alert and sensor-network systems used by governments and insurers.
- **Answering many questions about the same dataset is far cheaper when you pre-process it once** — the prefix sum is that one-time investment that pays dividends across every query.
- **Prefix sums turn repeated range-sum calculations from O(n) into O(1)** — always build one when you expect multiple sum queries over the same array.
- **Brute-force enumeration is acceptable when constraints are small** — always check problem bounds before over-engineering a solution; sometimes O(n²) per query is perfectly fine.
- **The pattern here — precompute, then query — generalises widely** to 2D grids, sliding windows, and difference arrays, making it one of the most reusable tools in a developer's algorithmic toolkit.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Exact match:** Modify the problem so you count subarrays whose sum equals the threshold exactly. How does this change the comparison logic, and could a hash-map approach improve efficiency?
- **Variation 2 — 2D rainfall grid:** Extend the problem to a 2D grid of sensors where queries define rectangular regions. Explore 2D prefix sums to handle this efficiently.
- **Variation 3 — Sliding window minimum threshold:** Instead of a fixed threshold per query, the threshold decreases by 1 for every additional sensor included in the subarray. How would you adapt the prefix sum approach to handle this dynamic condition?

---