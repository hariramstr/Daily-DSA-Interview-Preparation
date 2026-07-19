# Count Products With a Unique Reviewer Set

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Set, Array

---

## 🗂 Problem Overview
Given review records `[productId, userId]`, build the distinct reviewer set for every product that appears in the input, then count how many products have a reviewer set that no other product shares. Duplicate reviews by the same user for the same product must be ignored. The challenge is not per-product aggregation alone, but hashing reviewer sets efficiently across up to `2 * 10^5` records without falling into quadratic pairwise comparisons between products.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must canonicalize entity membership and then compare entities by that canonical form: deduplicating audience segments in ad-tech, grouping identical ACLs in authorization systems, collapsing equivalent dependency sets in build graphs, or detecting identical feature-user cohorts in experimentation platforms. At scale, naive comparison of every product against every other product is operationally dead on arrival. Canonical hashing enables near-linear grouping, predictable memory use, and a clean separation between local deduplication and global equivalence classification. That same design scales from batch ETL jobs to streaming aggregation pipelines and stateful services.

## 🔍 Problem Statement
You are given `reviews`, where each element is a pair `[productId, userId]`. A product may appear multiple times, and the same user may review the same product multiple times. For this problem, only the **set of distinct users per product** matters.

Two products are equivalent if their reviewer sets are exactly equal. Count how many products belong to an equivalence class of size `1`.

Constraints:

- `1 <= reviews.length <= 2 * 10^5`
- `1 <= productId, userId <= 10^9`
- Only products appearing in `reviews` are considered
- The solution must run in near-linear time

Examples:

- `[[101,1],[101,2],[102,2],[102,1],[103,3],[103,3],[104,4]]` → `2`
- `[[10,7],[10,8],[11,7],[12,8],[12,8],[13,9],[13,10],[14,9],[14,10]]` → `3`

The key constraint is the input size: comparing reviewer sets pairwise across products is too expensive, so the solution must use hashing and canonicalization.

## 🪜 How to Solve This
1. Read the problem carefully → there are **two deduplication layers**. First, repeated `(productId, userId)` pairs should not matter. Second, different products with the same final reviewer set should collapse into the same group.

2. Per-product reviewer collection immediately suggests a `HashMap<productId, Set<userId>>`. That gives the exact reviewer set for each product while naturally removing duplicate reviews.

3. Now ask: how do we compare sets across products efficiently? Raw sets are not stable hash keys in many languages, so each set needs a **canonical representation**.

4. The simplest canonical form is: convert each product’s reviewer set to a sorted list, then serialize it into a key. If two products have the same reviewer set, they produce the same canonical key.

5. Count how many products map to each canonical key using another hash map.

6. Finally, sum the groups whose frequency is `1`.

The core thought process is: **aggregate locally, canonicalize globally, then count equivalence classes**. Once you see “same set regardless of order and duplicates,” hashing plus canonical representation is the obvious tool.

## 🧩 Algorithm Walkthrough
1. **Build reviewer sets per product using hashing.**  
   Use a hash map from `productId` to a hash set of `userId`s. For each review `[p, u]`, insert `u` into the set for `p`.  
   **Why correct:** sets remove duplicate reviews automatically.  
   **Invariant:** after processing any prefix of input, each product maps to exactly the distinct reviewers seen so far.

2. **Canonicalize each reviewer set.**  
   For every product in the map, extract its reviewer set, sort the user IDs, and serialize the sorted list into a string or tuple-like key.  
   **Why correct:** two sets are equal iff their sorted unique elements are identical.  
   **Invariant:** equivalent reviewer sets produce identical canonical keys; non-equivalent sets do not.

3. **Count products per canonical reviewer-set key.**  
   Maintain a second hash map from canonical key to frequency. Increment once per product.  
   **Why correct:** this transforms “which products share the same reviewer set?” into a standard frequency-counting problem.  
   **Invariant:** the frequency for a key equals the number of products whose reviewer set canonicalizes to that key.

4. **Compute the answer.**  
   Iterate through the products again, or through the key-frequency map with product counts tracked, and count how many products belong to keys with frequency `1`.  
   **Why correct:** a product is unique exactly when no other product shares its reviewer-set key.

5. **Pattern name: Hashing + Canonicalization.**  
   This is the right abstraction because the equivalence relation is on unordered sets with duplicates ignored. Hashing handles grouping; canonicalization converts structural equality into key equality without pairwise comparisons.

## 📊 Worked Example
Use `reviews = [[101,1],[101,2],[102,2],[102,1],[103,3],[103,3],[104,4]]`.

| Step | Review      | productToUsers state                          |
|------|-------------|-----------------------------------------------|
| 1    | `[101,1]`   | `101 -> {1}`                                  |
| 2    | `[101,2]`   | `101 -> {1,2}`                                |
| 3    | `[102,2]`   | `101 -> {1,2}`, `102 -> {2}`                  |
| 4    | `[102,1]`   | `101 -> {1,2}`, `102 -> {1,2}`                |
| 5    | `[103,3]`   | `...`, `103 -> {3}`                           |
| 6    | `[103,3]`   | unchanged, duplicate ignored                  |
| 7    | `[104,4]`   | `...`, `104 -> {4}`                           |

Canonical keys:

- `101 -> "1,2"`
- `102 -> "1,2"`
- `103 -> "3"`
- `104 -> "4"`

Frequency map:

- `"1,2" -> 2`
- `"3" -> 1`
- `"4" -> 1`

Only products `103` and `104` belong to singleton groups, so the answer is `2`.

## ⏱ Complexity Analysis
### Time Complexity
Building per-product reviewer sets is `O(n)` average-case over `n = reviews.length`. Canonicalization costs `Σ k_i log k_i`, where `k_i` is the number of distinct reviewers for product `i`, due to sorting each reviewer set. Overall: `O(n + Σ k_i log k_i)`, which is near-linear in practice. This scales comfortably to `10^6` records, but not to `10^9` without distributed partitioning.

### Space Complexity
Space is `O(U)`, where `U` is the total number of distinct `(productId, userId)` memberships retained across all product sets, plus canonical-key bookkeeping. This can be reduced only by using probabilistic fingerprints or external storage, trading exactness or latency for memory.

## 💡 Key Takeaways
- If the problem says “same members, order irrelevant, duplicates irrelevant,” think **set canonicalization + hashing**, not pairwise comparison.
- If you must count entities by structural equivalence, look for a way to turn each structure into a stable hashable key.
- Do not count raw review rows; duplicate `(productId, userId)` pairs must be removed before comparing products.
- Be careful with canonical keys: iterating a hash set directly is not deterministic, so unsorted serialization is wrong.
- In production systems, this is the standard move for collapsing high-cardinality relational data into canonical identities that can be grouped, cached, and compared cheaply.

## 🚀 Variations & Further Practice
- Count reviewer-set groups with size at least `k` instead of singleton groups; same pattern, but now you care about distribution shape rather than uniqueness.
- Support online updates and queries as reviews stream in; harder because canonical keys must be maintained incrementally under mutable sets.
- Treat two products as equivalent if their reviewer sets are equal up to a threshold or Jaccard similarity; the exact-hash solution breaks, pushing you toward MinHash or approximate set-signature techniques.