# Merge Live Rankings from Trending Feeds

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, k-way-merge

---

## 🗂 Problem Overview
You are given `k` regional feeds, each already sorted by descending score, where every entry is `[postId, score]`. A post can appear in multiple feeds, and its global score is the maximum score seen across all feeds. Return the top `m` **unique** `postId`s ordered by global score descending, breaking ties by smaller `postId`.

The challenge is that flattening and sorting all entries wastes the structure already present in each feed and becomes expensive when total input size is large.

## 🌍 Engineering Impact
This pattern shows up anywhere multiple pre-ranked sources must be merged into one global view: federated search, regional recommendation systems, ad auctions, news aggregation, leaderboard consolidation, and distributed observability pipelines. Each shard or region can rank locally, but product behavior depends on a globally consistent top-N.

Without a heap-based merge, systems often over-materialize intermediate results, driving up latency, memory pressure, and network transfer. The right approach lets you exploit local ordering, stream candidates incrementally, and stop once enough globally best unique items are known. That is the difference between a ranking service that scales with shard count and one that degrades with total corpus size.

## 🔍 Problem Statement
Given `feeds`, a list of `k` arrays, where each feed contains records `[postId, score]` sorted in descending `score`, compute the top `m` unique posts under this rule: a post’s global score is the **maximum** score it receives in any feed. Return only the ordered list of `postId`s, sorted by global score descending; if scores tie, smaller `postId` comes first.

Constraints:
- `1 <= k <= 10^4`
- `1 <= total entries across all feeds <= 2 * 10^5`
- `1 <= postId, score <= 10^9`
- `1 <= m <= number of distinct postIds`

Example 1:
```text
feeds = [
  [[101,95],[102,90],[103,80]],
  [[104,99],[101,97],[105,70]],
  [[102,96],[106,88]]
], m = 4

Output: [104,101,102,106]
```

Example 2:
```text
feeds = [
  [[7,50],[8,40],[9,30]],
  [[8,60],[10,55]],
  [[7,60],[11,20]]
], m = 3

Output: [7,8,10]
```

The key constraint is large total input size with each feed already sorted, which strongly suggests a k-way merge instead of global re-sorting.

## 🪜 How to Solve This
1. Read the problem → two requirements jump out:  
   - merge multiple already-sorted lists  
   - deduplicate by `postId` using the **maximum** score

2. “Multiple sorted lists” should immediately suggest **k-way merge with a heap**. Instead of touching everything up front, keep only the current best unseen item from each feed in a max-heap.

3. But we cannot emit a post the first time we see its `postId` unless we know that score is already its maximum across all feeds. Why is that safe here? Because feeds are descending, and the heap always exposes the highest remaining score globally. The first time a `postId` is popped, no future occurrence can have a higher score.

4. So the flow becomes: pop best candidate → if this `postId` has not been emitted, add it to the answer → push the next item from the same feed.

5. Continue until `m` unique posts are collected. This exploits sorted structure, avoids flattening, and naturally handles duplicates while preserving each post’s maximum score.

## 🧩 Algorithm Walkthrough
1. **Initialize a max-heap with the first item from every non-empty feed.**  
   Store `(score, postId, feedIndex, itemIndex)`. In languages with only min-heaps, negate `score` and `postId` appropriately to simulate ordering by highest score, then smallest `postId`.  
   **Invariant:** the heap contains the best unprocessed candidate from each active feed.

2. **Maintain a `seen` set of emitted `postId`s and an output list.**  
   We only care about the first time a post is selected globally.  
   **Why correct:** because of descending feed order and max-heap extraction, the first popped occurrence of a `postId` is its maximum score across all feeds.

3. **Pop the heap’s top candidate.**  
   This is the highest-score remaining record among all feeds; ties are resolved by smaller `postId`.  
   **Invariant:** no unprocessed record anywhere has a better ranking key than the popped item.

4. **If `postId` is not in `seen`, emit it.**  
   Add it to `seen` and append to the answer. If already seen, skip it.  
   **Why correct:** later duplicates can only have equal or lower score, so they cannot improve the global score for that post.

5. **Advance within the same feed.**  
   If the popped record came from position `j`, push position `j + 1` from that feed if it exists.  
   This is the standard **k-way merge** pattern: each pop reveals the next candidate from that source.

6. **Stop after collecting `m` unique posts.**  
   Since `m` is guaranteed valid, termination is safe.  
   Pattern: **Heap-based k-way merge with deduplication on first global encounter**. This abstraction fits because ordering is distributed across feeds, not within one flat array.

## 📊 Worked Example
Using Example 1:

| Step | Heap Top Popped | Seen Before? | Output | Next Pushed |
|---|---|---:|---|---|
| Init | `(99,104,F1)`, `(95,101,F0)`, `(96,102,F2)` | — | `[]` | — |
| 1 | `104,99` | No | `[104]` | `101,97` from `F1` |
| 2 | `101,97` | No | `[104,101]` | `105,70` from `F1` |
| 3 | `102,96` | No | `[104,101,102]` | `106,88` from `F2` |
| 4 | `101,95` | Yes | `[104,101,102]` | `102,90` from `F0` |
| 5 | `102,90` | Yes | `[104,101,102]` | `103,80` from `F0` |
| 6 | `106,88` | No | `[104,101,102,106]` | none |

We stop at 4 unique posts. The first time `101` appears is with score `97`, and the first time `102` appears is with score `96`; later duplicates are lower and safely ignored.

## ⏱ Complexity Analysis
### Time Complexity
`O((k + t) log k)`, where `t` is the number of popped entries until `m` unique posts are found; in the worst case, `t` is the total number of feed entries `N`, so `O(N log k)`. The dominant cost is heap push/pop. At million-scale inputs this remains practical when `k` is moderate; a full `O(N log N)` sort scales noticeably worse.

### Space Complexity
`O(k + u)`, where `k` is the heap size and `u` is the number of unique emitted posts tracked in `seen` (up to all distinct `postId`s visited before termination). You could reduce bookkeeping only by sacrificing duplicate detection correctness or reprocessing work.

## 💡 Key Takeaways
- If the input is “many individually sorted lists” and you need a single ordered stream, think **k-way merge with a heap** before thinking global sort.
- If duplicates must be consolidated by “best value wins,” ask whether sorted order makes the **first global encounter** sufficient to finalize that key.
- Tie-breaking must be encoded directly in heap ordering; bolting it on after extraction produces unstable or incorrect results.
- Do not mark a `postId` as seen when pushing into the heap; mark it only when popping, or you may discard a better occurrence from another feed.
- In production ranking systems, exploiting shard-local order lets you stream top candidates incrementally instead of materializing and sorting the full cross-shard corpus.

## 🚀 Variations & Further Practice
- Return the top `m` posts when the global score is the **sum** of scores across feeds, not the max. Twist: first encounter is no longer final, so you need full aggregation before ranking.
- Support **online feed updates** where new entries arrive continuously. Twist: the merge becomes incremental and may require mutable priority queues plus versioning or watermark logic.
- Add a constraint that each feed may contain the same `postId` multiple times. Twist: you now need per-feed duplicate handling and stronger invariants around when a post’s global score is finalized.