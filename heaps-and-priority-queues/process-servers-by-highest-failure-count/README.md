# Process Servers by Highest Failure Count

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** Heap, Priority Queue, Hash Map

---

## 🗂 Problem Overview
Given a stream of server failure reports, count how many times each server appears, then return the top `k` server names ordered by repair priority. Higher failure count ranks first; ties break by lexicographically smaller server name. The non-trivial part is scale: sorting every distinct server works, but the intended solution should remain efficient when the number of unique servers is large and `k` is much smaller than that set.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need top-N extraction over high-cardinality event streams: fleet health dashboards, noisy-neighbor detection, search query analytics, abuse detection, compiler symbol frequency analysis, and streaming observability pipelines. At small scale, full sorting is acceptable; at production scale, it becomes unnecessary work and memory churn when only the top `k` matters. The heap-based approach enables bounded-state ranking after aggregation, reduces sort cost from all distinct keys to only the retained frontier, and maps cleanly to batch jobs, streaming windows, and shard-local partial aggregation before global merge.

## 🔍 Problem Statement
You are given an array `reports`, where each element is a server name representing one failure event, and an integer `k`. Count total failures per distinct server, then return up to `k` server names in descending order of failure count. If two servers have the same count, the lexicographically smaller name must come first.

Constraints:

- `1 <= reports.length <= 200000`
- `1 <= reports[i].length <= 30`
- `reports[i]` contains lowercase letters, digits, and hyphens
- `1 <= k <= 200000`

If there are fewer than `k` distinct servers, return all distinct servers in valid priority order.

Examples:

- `reports = ["db-1","api-2","db-1","cache-7","api-2","db-1"], k = 2`
  → `["db-1","api-2"]`

- `reports = ["node-b","node-a","node-b","node-a","node-c"], k = 3`
  → `["node-a","node-b","node-c"]`

The key constraint is the potentially large number of distinct servers: that is what makes a heap preferable to sorting everything.

## 🪜 How to Solve This
1. Read the problem → this is not about event order; it is about aggregated frequency. That immediately suggests counting first.

2. Counting by server name means a `HashMap<String, Int>` keyed by server name. One pass over `reports` gives total failures per server.

3. Now ask: do we need a full ranking of every distinct server? No, only the top `k`. That is the signal for a heap.

4. Use a min-heap of size at most `k` that stores the current best candidates. Define “worst among the kept top-k” at the heap root:
   - lower count is worse
   - for equal count, lexicographically larger name is worse

5. Iterate over the frequency map:
   - push each `(server, count)` into the heap
   - if heap size exceeds `k`, pop the worst element

6. After processing all distinct servers, the heap contains exactly the top `k` servers, but in heap order, not final output order.

7. Extract heap elements and reverse them, or sort the extracted `k` items by final priority. This keeps the expensive ordering work bounded by `k`, not total distinct servers.

## 🧩 Algorithm Walkthrough
1. **Aggregate frequencies with a Hash Map**  
   Traverse `reports` once and increment `freq[server]`.  
   **Why correct:** every report contributes exactly one failure to exactly one server.  
   **Invariant:** after processing index `i`, the map contains correct counts for `reports[0..i]`.

2. **Apply the Top-K via Min-Heap pattern**  
   Create a min-heap ordered so the least desirable retained candidate is at the root:
   - smaller count first
   - if counts tie, lexicographically larger name first  
   This inversion is deliberate: the root is what should be evicted when a better candidate arrives.  
   **Why this abstraction fits:** the problem asks for top `k`, not full ordering; bounded heaps are the standard way to maintain a rolling frontier.

3. **Scan distinct servers and maintain heap size `<= k`**  
   For each `(server, count)` in `freq`, push into the heap. If size exceeds `k`, pop once.  
   **Why correct:** whenever size becomes `k+1`, removing the current worst leaves the best `k` seen so far.  
   **Invariant:** after each iteration, the heap contains the top `k` servers among all processed distinct servers.

4. **Build final output in required order**  
   Pop all heap elements into a list. Since the heap returns worst-to-best among retained elements, reverse the list, or sort those `<= k` elements by:
   - count descending
   - name ascending  
   **Why correct:** this matches the repair priority exactly.

5. **Handle edge conditions naturally**  
   If `k` exceeds the number of distinct servers, no special branch is needed; the heap never grows beyond that count, and all servers are returned.

## 📊 Worked Example
Use `reports = ["db-1","api-2","db-1","cache-7","api-2","db-1"]`, `k = 2`.

### 1) Frequency map
| Report processed | freq |
|---|---|
| db-1 | {db-1: 1} |
| api-2 | {db-1: 1, api-2: 1} |
| db-1 | {db-1: 2, api-2: 1} |
| cache-7 | {db-1: 2, api-2: 1, cache-7: 1} |
| api-2 | {db-1: 2, api-2: 2, cache-7: 1} |
| db-1 | {db-1: 3, api-2: 2, cache-7: 1} |

### 2) Heap maintenance over distinct servers
Assume iteration order: `db-1(3)`, `api-2(2)`, `cache-7(1)`.

1. Push `db-1(3)` → heap = `[db-1(3)]`
2. Push `api-2(2)` → heap = `[api-2(2), db-1(3)]`
3. Push `cache-7(1)` → heap size becomes 3, pop worst → remove `cache-7(1)`

Heap now contains the top 2 servers: `api-2(2)` and `db-1(3)`.  
Pop order is worst-to-best, so extracted list is `[api-2, db-1]`; reverse it to get `["db-1","api-2"]`.

## ⏱ Complexity Analysis
### Time Complexity
Counting reports costs `O(n)`, where `n = reports.length`. Maintaining the heap across `m` distinct servers costs `O(m log k)`. Final extraction costs `O(k log k)` in the worst case if you sort the retained items, or `O(k log k)` implicitly through heap pops. Total: `O(n + m log k)`. At million-scale streams, this avoids sorting all distinct keys; at billion-scale, that difference is operationally material.

### Space Complexity
The frequency map stores `m` distinct servers, and the heap stores at most `k` entries, so space is `O(m + k)`, dominated by the map in most realistic inputs. You cannot avoid `O(m)` exact counting without changing the problem; reducing memory requires approximate heavy-hitter techniques with accuracy trade-offs.

## 💡 Key Takeaways
- If the problem says “top `k` by frequency” over many distinct keys, think `HashMap` for aggregation plus bounded heap for selection.
- If tie-breaking matters, encode it directly into the heap comparator; comparator design is part of the algorithm, not an implementation detail.
- The heap should keep the **worst** of the current top `k` at the root; getting that ordering backwards silently returns the wrong set.
- When counts tie, lexicographically smaller names rank higher, so the min-heap must treat lexicographically larger names as worse for eviction.
- In production systems, this pattern is the exact bridge between exact aggregation and scalable top-N retrieval without paying full-sort cost on every key.

## 🚀 Variations & Further Practice
- **Streaming top-k over sliding windows:** counts expire as events age out, which forces indexed heaps, lazy deletion, or balanced trees instead of one-shot aggregation.
- **Distributed top-k across shards:** each shard computes local heavy hitters, then a global merge reconciles partial counts and tie-breakers; correctness depends on aggregation strategy.
- **Approximate heavy hitters:** replace exact counting with Count-Min Sketch or Space-Saving to bound memory under extreme cardinality, trading exactness for throughput and footprint.