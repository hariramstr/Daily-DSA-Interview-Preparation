# Serve Queries by Highest Rated Item

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, hash-map

---

## 🗂 Problem Overview
Design a data structure that maintains items partitioned by category, supports rating updates, and answers queries for the highest-rated item within a category. Ties are broken by smaller `itemId`. The input provides initial `itemId`, `category`, and `rating` arrays plus a sequence of `update` and `top` operations; the output is the result of each `top` query. The challenge is that repeated scans per category are too slow at the given scale.

## 🌍 Engineering Impact
This pattern shows up anywhere a system maintains mutable ranked leaders inside partitions: marketplace recommendations, ad selection per campaign, top content per topic, best endpoint per shard, or search/autocomplete candidates per prefix bucket. At small scale, rescanning a bucket is acceptable; at hundreds of thousands of updates and reads, it becomes a latency amplifier and a cache-unfriendly hotspot. A heap-per-group plus authoritative metadata decouples write frequency from read cost, enabling near-real-time leader queries without rebuilding sorted views on every mutation. The same trade-off appears in streaming aggregators and online ranking services.

## 🔍 Problem Statement
You are given `n` distinct items. Each item belongs to exactly one category and has an integer rating. Then you must process up to `2 * 10^5` operations of two forms:

- `["update", itemId, newRating]`: change the current rating of an existing item
- `["top", category]`: return the `itemId` of the highest-rated item in that category

If multiple items in a category share the highest rating, return the smallest `itemId`.

Constraints are large enough that a linear scan of all items in a category for every `top` query is not viable:

- `1 <= n <= 2 * 10^5`
- `1 <= operations <= 2 * 10^5`
- `itemId` values are distinct, up to `10^9`
- ratings are up to `10^9`
- categories are up to `10^5`

Example:

- Input: `itemId=[10,11,12,13]`, `category=[1,1,2,1]`, `rating=[5,7,9,7]`
- Operations: `["top",1]`, `["update",10,8]`, `["top",1]`, `["update",11,8]`, `["top",1]`, `["top",2]`
- Output: `[11,10,10,12]`

The key constraint is dynamic updates plus repeated max queries within groups.

## 🪜 How to Solve This
1. Read the problem → notice queries are not global; they are scoped by `category`. That immediately suggests partitioning state by category.

2. We need two capabilities at once:
   - find the best item in a category quickly
   - update an item's rating quickly

3. A max-heap per category gives fast access to the current best candidate. But standard heaps do not support efficient arbitrary key updates unless you build indexed heaps or balanced trees.

4. Instead of mutating heap entries in place, keep authoritative current state in a hash map:
   - `itemId -> (category, currentRating)`

5. On every update, push a new heap entry into that item's category heap. Do not remove the old one immediately.

6. On `top(category)`, inspect the heap root. If it matches the authoritative rating for that `itemId`, it is valid. If not, it is stale from an earlier update, so pop it and continue.

7. This is the classic lazy deletion pattern: writes stay simple, reads clean up only the obsolete entries they encounter.

8. Tie-breaking falls out naturally by ordering heap entries as `(-rating, itemId)`.

## 🧩 Algorithm Walkthrough
1. **Initialize authoritative metadata using a hash map.**  
   Store `itemId -> (category, rating)`. This is the source of truth for every item's current state. Correctness depends on never trusting heap entries alone, because heaps will contain historical values after updates.

2. **Build one priority queue per category.**  
   For each initial item, push an entry representing its ranking key into that category’s heap. In Python-style terms, use `(-rating, itemId)` so the heap root corresponds to highest rating, then smallest `itemId`. This encodes the exact query ordering.

3. **Process `update(itemId, newRating)`.**  
   Look up the item’s category from the hash map, overwrite its current rating in the map, and push a new `(-newRating, itemId)` entry into that category heap. We do not search for and remove the old heap node. This is the **heap + lazy deletion** pattern.

4. **Process `top(category)`.**  
   Repeatedly inspect the heap root for that category. Let the root be `(-r, id)`. Compare `r` with the current rating stored in the hash map for `id`. If they match, this entry is live and must be the correct answer because the heap ordering already enforces highest rating and smallest `itemId`. If not, pop it as stale and continue.

5. **Maintain the invariant.**  
   Every category heap may contain stale entries, but at least one valid entry exists for every live item. Therefore, after removing stale roots, the first valid root is always the correct top item for that category.

## 📊 Worked Example
Using Example 1:

| Step | Operation | Category 1 Heap Candidates | Current Ratings | Answer |
|---|---|---|---|---|
| 0 | init | `(7,11), (7,13), (5,10)` | `10→5, 11→7, 13→7` | — |
| 1 | `top(1)` | root = `(7,11)` valid | unchanged | `11` |
| 2 | `update(10,8)` | push `(8,10)` | `10→8, 11→7, 13→7` | — |
| 3 | `top(1)` | root = `(8,10)` valid | unchanged | `10` |
| 4 | `update(11,8)` | push `(8,11)` | `10→8, 11→8, 13→7` | — |
| 5 | `top(1)` | roots tie on rating 8; smaller id wins | unchanged | `10` |
| 6 | `top(2)` | only item `12` with rating `9` | `12→9` | `12` |

Important detail: old entries like `(5,10)` and `(7,11)` remain in heaps. They are ignored later when they reach the root and fail validation against current ratings.

## ⏱ Complexity Analysis
### Time Complexity
Initialization is `O(n log n)` in the worst case if many items land in one category heap. Each `update` is `O(log m)` for that category heap, and each stale entry is popped at most once across all queries, so total processing is `O((n + q) log n)` amortized. This remains practical at `10^6` operations; it is not remotely feasible at `10^9` without sharding or approximation.

### Space Complexity
Space is `O(n + q)` in the worst case because heaps retain stale entries from updates, and the hash map stores one authoritative record per item. You can reduce heap bloat only by using indexed heaps or balanced trees, trading simpler logic for more complex update machinery.

## 💡 Key Takeaways
- If a problem asks for repeated “best-in-group” queries under live updates, think `hash map for ownership/state + heap per group`.
- If arbitrary heap key updates would be awkward, lazy deletion is usually the intended simplification.
- Tie-breaking must be encoded directly into the heap key; bolting it on after extraction produces wrong answers.
- Do not validate a heap entry using category alone; it must be checked against the authoritative current rating for that exact `itemId`.
- In production systems, lazy invalidation often beats eager cleanup because it shifts work off the write path and keeps hot-path mutations cheap.

## 🚀 Variations & Further Practice
- Support `move(itemId, newCategory, newRating)`: now an item can change partitions, so lazy deletion must handle both stale ratings and stale category membership.
- Return the top `k` items per category after updates: the challenge becomes extracting multiple valid leaders without permanently destroying heap state or paying repeated cleanup costs.
- Add deletions and time-based expiration: this turns the problem into a mutable leaderboard with tombstones and potentially multiple invalidation dimensions.