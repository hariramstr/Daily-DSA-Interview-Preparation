# Equal Weight Partition Splits

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Prefix Sum &nbsp;|&nbsp; **Tags:** Prefix Sum, Array, Counting

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have a row of boxes, each with a different weight. Your goal is to divide that row into exactly three groups — without rearranging anything — so that every group weighs the same. This problem asks: in how many different ways can you make those two cuts? Each group must contain at least one box, and the order of boxes cannot change.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Fair partitioning is a critical challenge in logistics, cloud computing, and data processing. Amazon and FedEx use similar balancing logic to distribute packages evenly across delivery routes, reducing fuel costs and driver overtime. Cloud providers like AWS use load-balancing algorithms to split workloads equally across servers, preventing bottlenecks and improving response times for customers. Data pipeline tools like Apache Spark partition datasets evenly to maximise parallel processing speed. Getting this balance right translates directly into cost savings, faster delivery, and better user experiences at scale.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a conveyor belt at a warehouse carrying boxes in a fixed order. You need to place exactly two dividers on the belt to create three sections, where each section holds the same total weight. You cannot move the boxes — only choose where to place the dividers. The question is: how many valid divider positions exist? Sometimes there are many options; sometimes there are none at all.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an array `weights` of positive integers, split it into exactly **three non-empty contiguous subarrays** such that all three subarrays have equal sums. Return the total number of valid ways to make this split.

A split is defined by indices `i` and `j` where `0 < i < j < n` (0-based), producing:
- **Left:** `weights[0..i-1]`
- **Middle:** `weights[i..j-1]`
- **Right:** `weights[j..n-1]`

**Constraints:** `3 <= weights.length <= 10^5`, `1 <= weights[i] <= 10^4`

**Examples:**
```
Input: [1, 2, 3, 0, 3]  →  Output: 2
Input: [1, 1, 1, 1, 1, 1]  →  Output: 4
Input: [1, 2, 4]  →  Output: 0
```

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Compute the total sum.** Add up all elements. If the total is not divisible by 3, no equal split is possible — return `0` immediately. This early exit avoids unnecessary work.

2. **Determine the target per partition.** Each of the three subarrays must sum to `total / 3`. Call this value `target`.

3. **Build a prefix sum array.** Create an array where `prefix[k]` holds the sum of `weights[0..k-1]`. This allows any subarray sum to be computed in O(1) time rather than re-scanning the array.

4. **Find valid first-cut positions.** Scan left to right. Every index `i` where `prefix[i] == target` is a valid end of the first subarray. Count these positions and store them in a running counter as you move through the array.

5. **Find valid second-cut positions simultaneously.** For each index `j` where `prefix[j] == 2 * target` AND `j < n` (ensuring the third part is non-empty), add the current count of valid first cuts to the result. This works because any previously recorded first-cut position can pair with this second-cut position to form a valid split.

6. **Return the accumulated result.** The final count represents all valid `(i, j)` pairs.

---

## 📊 Worked Example *(For Developers)*

**Input:** `weights = [1, 2, 3, 0, 3]`, **Expected Output:** `2`

**Total sum** = 9 → **Target per part** = 3

| Step | Index `k` | `prefix[k]` | First-cut count (`== target`) | Second-cut found (`== 2×target`)? | Result added |
|------|-----------|-------------|-------------------------------|-----------------------------------|--------------|
| Init | —         | —           | 0                             | —                                 | 0            |
| 1    | 1         | 1           | 0                             | No                                | 0            |
| 2    | 2         | 3           | 0 → **1**                     | No (prefix ≠ 6)                   | 0            |
| 3    | 3         | 6           | 1                             | **Yes** → add 1                   | 1            |
| 4    | 4         | 6           | 1                             | **Yes** → add 1                   | 2            |

> At index 3, `prefix = 6 = 2 × 3`, so the first cut count (1) is added. At index 4, same condition holds again. Final result = **2**. ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — The algorithm makes a single pass through the array to build prefix sums and a second single pass to count valid splits. Even with 100,000 items (the maximum input size), this runs in microseconds, making it highly scalable for production workloads.

### Space Complexity

**O(n)** for storing the prefix sum array. In practice, this can be reduced to **O(1)** by computing the running sum inline during the counting pass, using only a handful of integer variables regardless of input size.

---

## 💡 Key Takeaways *(For Everyone)*

- **Fair splitting has real business value** — equal partitioning underpins load balancing, logistics routing, and parallel data processing that save companies measurable time and money.
- **Checking divisibility first is a powerful early exit** — eliminating impossible cases before doing any real work is a pattern that speeds up systems at scale.
- **Prefix sums turn repeated range queries from O(n) into O(1)** — precomputing cumulative totals is one of the most reusable tricks in algorithm design.
- **Counting valid left cuts as you scan right avoids a nested loop** — transforming an O(n²) brute-force into O(n) by carrying forward a running count is the core insight here.
- **The third partition is implicitly validated** — because the total is fixed and the first two parts each equal `target`, the third part must also equal `target` by arithmetic, so no extra check is needed.

---

## 🚀 Try It Yourself *(For Developers)*

- **Four-way equal split:** Extend the algorithm to split the array into exactly **four** equal-sum parts — how does the counting logic change, and what is the new time complexity?
- **Weighted tolerance:** Modify the problem so the three parts must be within ±5% of each other in sum rather than exactly equal — how would you adapt the prefix sum approach?
- **Two-dimensional partition:** Given a matrix instead of an array, find the number of ways to make two horizontal cuts such that each row band has the same total element sum — a useful warm-up for image-processing segmentation problems.

---