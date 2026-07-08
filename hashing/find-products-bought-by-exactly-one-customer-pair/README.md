# Find Products Bought by Exactly One Customer Pair

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Hash Map, Set

---

## 🗂 Problem Overview
Given purchase records `[customerId, productId]`, build a mapping from each customer pair to the products bought by exactly those two customers. Duplicate logs for the same `(customer, product)` must be ignored. For every product, look at its distinct customer set; if that set has size exactly two, assign the product to the normalized pair `(min(a,b), max(a,b))`. Return `[customerA, customerB, sortedProductIds]` entries sorted by customer pair. The scale rules out pairwise comparison or repeated scans.

## 🌍 Engineering Impact
This pattern shows up anywhere raw event logs must be collapsed into exact co-occurrence relationships: retail basket analysis, ad-tech audience overlap, fraud rings, IAM entitlement audits, and streaming pipelines that derive “shared-by-exactly-N principals” features. The operational issue is always the same: logs are noisy, duplicates exist, and naive joins explode combinatorially. A hash-based aggregation model gives deterministic deduplication and bounded linear work over distinct facts. Without it, systems either overcount due to duplicate events or fall back to expensive secondary passes that do not survive high-cardinality product or identity spaces.

## 🔍 Problem Statement
You are given `records`, where each element is `[customerId, productId]`. A record means that customer bought that product at least once. Duplicate records may appear, but repeated purchases of the same product by the same customer count only once.

For each product, compute the set of distinct customers who bought it. If that set contains exactly two customers `a` and `b`, then the product is pair-exclusive for the normalized pair `(min(a,b), max(a,b))`. Group all such products by pair.

Return a list of entries:

- `[customerA, customerB, sortedProductIds]`

sorted by `customerA`, then `customerB`. Each product list must be sorted ascending.

Constraints:

- `1 <= records.length <= 200000`
- `1 <= customerId, productId <= 10^9`
- Duplicate records may exist
- Total distinct `(customerId, productId)` pairs `<= 200000`

Examples:

- `[[1,101],[2,101],[1,102],[3,102],[2,103],[3,103],[2,104],[1,104],[2,104]]`
  → `[[1,2,[101,104]],[1,3,[102]],[2,3,[103]]]`
- `[[5,200],[7,200],[8,200],[5,201],[7,201],[5,202],[5,202],[9,203],[10,203]]`
  → `[[5,7,[201]],[9,10,[203]]]`

The key constraint is the 200k-scale input with duplicates, which strongly favors single-pass hashing over sorting-heavy or quadratic approaches.

## 🪜 How to Solve This
1. Read the requirement carefully → the unit of truth is not each log line, but each distinct `(customer, product)` relationship. That immediately suggests deduplication before any counting logic.

2. Ask what determines inclusion → a product qualifies only if its distinct customer set has size exactly `2`. So the natural first grouping is by `productId`, not by customer pair.

3. Group product → customers using a hash map of sets. This gives two things at once: duplicate suppression and exact customer cardinality per product.

4. Once each product has its distinct customer set, filter to sets of size `2`. Normalize the pair as `(min, max)` so `(2,1)` and `(1,2)` collapse to the same key.

5. Now invert the relationship: pair → list of products. That is the output shape, so build a second hash map keyed by customer pair.

6. Finalize deterministically → sort each product list, then sort pair entries by `(customerA, customerB)`.

The reasoning is straightforward once you separate the problem into two aggregations: first discover exact customer membership per product, then regroup qualifying products by the pair they belong to.

## 🧩 Algorithm Walkthrough
1. **Deduplicate and group by product using a Hash Map + Set pattern.**  
   Maintain `productToCustomers: Map<productId, Set<customerId>>`. For each record `[c, p]`, insert `c` into the set for `p`. This is correct because the problem defines product ownership in terms of distinct customers, not raw event count. Invariant: after processing any prefix of records, each product’s set contains exactly the distinct customers seen so far for that product.

2. **Filter products to the pair-exclusive subset.**  
   Iterate through `productToCustomers`. If a product’s customer set size is not `2`, ignore it. This is correct because the definition excludes products bought by one customer or by three or more customers. Invariant: every surviving product belongs to exactly one normalized customer pair.

3. **Normalize the pair key.**  
   Extract the two customers, compute `(a, b) = (min(x, y), max(x, y))`, and use that ordered pair as the key. This avoids duplicate representations of the same pair and guarantees stable output semantics.

4. **Regroup into pair → products.**  
   Maintain `pairToProducts: Map<(a,b), List<productId>>` and append the current product. This inversion matches the required output contract exactly.

5. **Sort product lists.**  
   For each pair, sort its product list ascending. This satisfies output requirements and preserves deterministic behavior independent of input order.

6. **Sort final entries by pair.**  
   Convert the map into `[a, b, products]` entries and sort lexicographically by `(a, b)`. The dominant abstraction here is **hash-based grouping with set-backed deduplication**, which is the right fit because membership and cardinality matter more than sequence.

## 📊 Worked Example
Using `[[1,101],[2,101],[1,102],[3,102],[2,103],[3,103],[2,104],[1,104],[2,104]]`:

| Step | Record | `productToCustomers` update |
|---|---|---|
| 1 | `[1,101]` | `101 -> {1}` |
| 2 | `[2,101]` | `101 -> {1,2}` |
| 3 | `[1,102]` | `102 -> {1}` |
| 4 | `[3,102]` | `102 -> {1,3}` |
| 5 | `[2,103]` | `103 -> {2}` |
| 6 | `[3,103]` | `103 -> {2,3}` |
| 7 | `[2,104]` | `104 -> {2}` |
| 8 | `[1,104]` | `104 -> {1,2}` |
| 9 | `[2,104]` | unchanged; duplicate |

Now scan products:

- `101 -> {1,2}` → pair `(1,2)` gets `101`
- `102 -> {1,3}` → pair `(1,3)` gets `102`
- `103 -> {2,3}` → pair `(2,3)` gets `103`
- `104 -> {1,2}` → pair `(1,2)` gets `104`

After sorting:
`[[1,2,[101,104]],[1,3,[102]],[2,3,[103]]]`

## ⏱ Complexity Analysis
### Time Complexity
`O(D + P log P + Σ k_i log k_i)` where `D` is the number of distinct `(customer, product)` pairs, `P` is the number of output pairs, and `k_i` is products per pair. The hash aggregation is linear; sorting output dominates only at the end. At `10^6` elements this is routine; at `10^9`, only streaming/distributed partitioning makes it viable.

### Space Complexity
`O(D)` in the worst case, owned primarily by `productToCustomers` and then `pairToProducts`. You can reduce peak memory by replacing full customer sets with capped state per product, but only if you carefully preserve duplicate suppression semantics.

## 💡 Key Takeaways
- If the problem says “exactly these distinct entities” and duplicates must not count, think `HashMap<Key, Set<Value>>` before anything else.
- If output groups are defined by a derived relationship, first aggregate by the source entity, then invert into the required grouping.
- Do not count raw records; duplicate `(customer, product)` logs will corrupt cardinality unless you deduplicate per product.
- Always normalize the customer pair as `(min, max)`; otherwise the same logical pair splits across two keys.
- In production data pipelines, exactness usually comes from choosing the right aggregation boundary early, not from adding cleanup logic later.

## 🚀 Variations & Further Practice
- Return products bought by **exactly `k` distinct customers** and group by the full sorted customer tuple; the harder part is representing and hashing variable-length identity sets efficiently.
- Process records as a **stream with bounded memory**, emitting pair-exclusive products incrementally; the twist is late-arriving duplicates and products whose customer count can grow past two.
- Extend to **weighted purchases** where repeated purchases matter for ranking but not membership; the challenge is maintaining both deduplicated membership and aggregate metrics simultaneously.