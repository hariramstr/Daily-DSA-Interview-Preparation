# Count Pairs of Orders With the Same Item Set

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Arrays, Set Canonicalization

---

## 🗂 Problem Overview
Given a list of orders, where each order is a list of item IDs, count how many unordered pairs of orders are equivalent after ignoring duplicate items within an order and ignoring item order. Each order must be reduced to its distinct-item set, then grouped by that normalized form. The challenge is scale: up to 100,000 orders and 200,000 total item IDs make pairwise comparison infeasible, so the solution must canonicalize and hash efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must detect semantic equivalence after normalization: deduplicating shopping carts, canonicalizing ACLs, collapsing feature-flag sets, compiler symbol-set comparisons, and stream processing pipelines that group events by unordered attributes. At scale, failing to canonicalize before hashing leads to false fragmentation: logically identical records land in different buckets, inflating storage, cache misses, and downstream compute. The right approach enables efficient aggregation, exact deduplication, and stable grouping keys that can be partitioned, cached, or persisted across services without expensive pairwise reconciliation.

## 🔍 Problem Statement
You are given `orders`, where each `orders[i]` is a non-empty list of item IDs. An item ID may appear multiple times in the same order, but equivalence is defined only by the set of distinct item IDs in that order. Two orders are equivalent if, after removing duplicates and ignoring order, they contain exactly the same item IDs.

Return the number of unordered pairs `(i, j)` such that `i < j` and `orders[i]` is equivalent to `orders[j]`.

Constraints:

- `1 <= orders.length <= 100000`
- `1 <= total number of item IDs across all orders <= 200000`
- `1 <= item IDs <= 1000000000`
- Each order contains at least 1 item

Examples:

- `[[1,2,2,3],[3,1,2],[4,4],[4,5],[5,4,4]] -> 2`
- `[[8],[8,8],[1,2],[2,1],[1,1,2,2],[3]] -> 4`

The key constraint is that `orders.length` is large enough that `O(n^2)` pair checking is not viable.

## 🪜 How to Solve This
1. Read the equivalence rule carefully → multiplicity inside one order does **not** matter, so each order is really a set.

2. If we need to count equivalent orders, we are really grouping orders by some normalized identity → that immediately suggests a hash map.

3. What should the key be? Not the raw order, because `[3,1,2]` and `[1,2,2,3]` must match. So first remove duplicates, then impose a deterministic order, typically by sorting.

4. That gives a canonical representation such as `(1,2,3)` for every order equivalent to `{1,2,3}`.

5. Once each order maps to a canonical key, counting pairs becomes a frequency problem: if a key appears `f` times, it contributes `f * (f - 1) / 2` unordered pairs.

6. You can either build all frequencies first and sum combinations at the end, or update the answer online: when you see the same key again, it forms a new pair with every prior occurrence of that key.

7. This avoids nested comparisons entirely and scales with total input size plus normalization cost per order.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + canonicalization pattern.**  
   The core abstraction is: convert every equivalence class into one stable key, then count equal keys. This is the right pattern because the problem asks for grouping under a custom equality relation, not direct comparison of raw arrays.

2. **Process each order independently.**  
   For a given order, remove duplicate item IDs by inserting them into a set. This enforces the problem’s rule that multiplicity is irrelevant.  
   **Invariant:** after this step, the collection contains exactly the distinct items of the order.

3. **Create a canonical representation.**  
   Convert the distinct items to a list and sort it, then use the sorted sequence as the hash key (for example, a tuple or joined string).  
   **Why correct:** two orders are equivalent iff their distinct-item sets are equal, and equal sets produce identical sorted sequences.

4. **Count occurrences with a hash map.**  
   Maintain `freq[key]`, the number of prior orders with the same canonical form. Before incrementing it, add `freq[key]` to the answer.  
   **Invariant:** after processing `k` orders, `answer` equals the number of equivalent unordered pairs among those `k` orders.

5. **Return the accumulated answer.**  
   This online counting works because the current order forms one new pair with each previous order in the same group, and none with orders in other groups.

## 📊 Worked Example
Example: `orders = [[1,2,2,3],[3,1,2],[4,4],[4,5],[5,4,4]]`

| Step | Raw Order     | Distinct Set | Canonical Key | freq before | Pairs added | Total |
|------|---------------|--------------|---------------|-------------|-------------|-------|
| 1    | `[1,2,2,3]`   | `{1,2,3}`    | `(1,2,3)`     | 0           | 0           | 0     |
| 2    | `[3,1,2]`     | `{1,2,3}`    | `(1,2,3)`     | 1           | 1           | 1     |
| 3    | `[4,4]`       | `{4}`        | `(4)`         | 0           | 0           | 1     |
| 4    | `[4,5]`       | `{4,5}`      | `(4,5)`       | 0           | 0           | 1     |
| 5    | `[5,4,4]`     | `{4,5}`      | `(4,5)`       | 1           | 1           | 2     |

Final answer: `2`.

The trace makes the counting rule clear: each repeated canonical key contributes as many new pairs as its prior frequency.

## ⏱ Complexity Analysis
### Time Complexity
Let `M` be the total number of item IDs across all orders. For each order of size `k`, we deduplicate and then sort its distinct items, costing `O(k + d log d)` where `d <= k`. Summed across all orders, this is `O(M + Σ d log d)`, typically written as `O(M log K)` in the worst case. At million-scale inputs this is practical; at billion-scale, canonicalization cost dominates and may require distributed partitioning.

### Space Complexity
The main space cost is the hash map of canonical keys plus temporary per-order deduplication storage. In the worst case, this is `O(M)` across all distinct canonicalized content. You can reduce constant factors with compact key encoding, but only by trading readability and implementation simplicity.

## 💡 Key Takeaways
- If the problem says “same elements regardless of order” and duplicates inside a group do not matter, think **canonicalize to a set-like key, then hash**.
- If you need the number of equivalent pairs across many records, avoid pairwise comparison; convert it into **frequency counting over normalized representations**.
- Do not forget to remove duplicates **before** sorting; sorting the raw order would incorrectly distinguish `[8]` from `[8,8]`.
- Count unordered pairs carefully: when processing online, add the current frequency before incrementing; reversing that introduces an off-by-one error.
- In production systems, stable canonical forms are the bridge between domain-specific equality and scalable infrastructure primitives like hashing, partitioning, caching, and deduplication.

## 🚀 Variations & Further Practice
- Count pairs where orders are equivalent as **multisets**, not sets. The twist is that multiplicity now matters, so canonicalization must preserve counts rather than deduplicating.
- Group orders by **subset/superset** relationships instead of exact equality. The harder part is that hashing exact canonical keys no longer solves the problem directly.
- Support a streaming system with **incremental updates and deletions** of orders. The conceptual twist is maintaining pair counts correctly under both insert and remove operations.