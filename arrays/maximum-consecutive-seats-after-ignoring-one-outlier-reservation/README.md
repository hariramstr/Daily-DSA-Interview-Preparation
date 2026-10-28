# Maximum Consecutive Seats After Ignoring One Outlier Reservation

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sorting, Hash Set

---

## 🗂 Problem Overview
Given an unsorted integer array of reserved seat numbers, possibly with duplicates, compute the maximum length of any consecutive reserved block after optionally removing all copies of one distinct value. Duplicates do not extend a block, so the problem is really about distinct values. The non-trivial part is scale: with up to `2 * 10^5` entries and values up to `10^9`, any approach that scans gaps naively or tries every deletion candidate is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere noisy identifiers must be normalized before continuity analysis: event-stream deduplication, reservation/inventory reconciliation, time-series gap detection, compiler symbol index cleanup, and search or ranking pipelines that receive repeated sync artifacts. The key engineering issue is separating signal from duplicate or outlier noise without paying quadratic cost. At scale, the wrong approach turns a simple consistency check into a hot-path bottleneck. The right approach—deduplicate, sort, and reason over runs—enables predictable latency, bounded memory growth, and logic that remains robust when upstream systems emit redundant or partially corrupted state.

## 🔍 Problem Statement
You are given an integer array `seats` where:

- `1 <= seats.length <= 2 * 10^5`
- `1 <= seats[i] <= 10^9`

The array is unsorted and may contain duplicates. You may ignore at most one **distinct** seat number, meaning every occurrence of that exact value is removed. After that optional removal, return the maximum size of any consecutive block of reserved seat numbers, where every integer in the block must appear at least once.

Important edge cases:

- Duplicates do not increase block length.
- Removing one value cannot fill a missing number.
- If all values are isolated, the answer is `1`.

Examples:

- `seats = [10, 11, 12, 14, 14, 15, 16]` → distinct values `{10,11,12,14,15,16}` → answer `3`
- `seats = [3, 4, 5, 7, 8, 9, 20]` → answer `3`

The constraint that drives the algorithmic choice is the input size: we need roughly `O(n log n)` or better.

## 🪜 How to Solve This
1. Read the problem → notice duplicates are irrelevant to consecutiveness. That immediately suggests deduplicating first.
2. After deduplication, sort the distinct seat numbers. Once sorted, the problem becomes structural: where do consecutive runs start and end?
3. Now ask what deleting one distinct value can actually do. It cannot create a missing number, so it cannot merge runs separated by a gap larger than `1`.
4. Can deleting a value improve the longest run at all? No. Removing a present value only keeps a run the same elsewhere or shortens the run containing that value.
5. That observation collapses the problem: the answer is simply the longest consecutive run among the sorted distinct values.
6. So the implementation becomes straightforward: build a `HashSet`, convert to array/list, sort, scan once, and track the current run length and best run length.
7. The key mental move is recognizing that “ignore one outlier” is a distractor here because deletion is destructive, not corrective.

## 🧩 Algorithm Walkthrough
1. **Deduplicate with a Hash Set**  
   Insert every seat number into a `HashSet`. This removes repeated sync artifacts and preserves exactly the values that matter for consecutiveness.  
   **Invariant:** each distinct seat value appears once in the working set.

2. **Sort the distinct values**  
   Convert the set into an array and sort ascending. Sorting is the right abstraction because consecutiveness is defined by numeric adjacency, and adjacency is only easy to inspect in sorted order.  
   **Pattern:** **Sort + Linear Scan**. This is preferable to nested comparisons because we only care about local differences after ordering.

3. **Scan for maximal consecutive runs**  
   Initialize `currentRun = 1` and `best = 1`. For each index `i > 0`, compare `sorted[i]` with `sorted[i - 1]`.  
   - If the difference is `1`, extend the current run.  
   - Otherwise, reset `currentRun = 1`.  
   Update `best` after each step.  
   **Invariant:** after processing position `i`, `currentRun` equals the length of the consecutive suffix ending at `sorted[i]`, and `best` is the maximum run seen so far.

4. **Why ignoring one value changes nothing**  
   Removing one distinct value cannot introduce a missing seat number that was absent before. It can only shrink an existing run or leave another run untouched. Therefore the optimal answer is already present in the original distinct sorted sequence.  
   **Correctness argument:** every valid post-deletion consecutive block is also a subset of some original consecutive block, so its length cannot exceed the original maximum.

5. **Return `best`**  
   This is the maximum achievable block length under the problem’s rules.

## 📊 Worked Example
Use `seats = [10, 11, 12, 14, 14, 15, 16]`.

Distinct sorted values: `[10, 11, 12, 14, 15, 16]`

| i | value | prev | diff | currentRun | best |
|---|------:|-----:|-----:|-----------:|-----:|
| 0 | 10 | - | - | 1 | 1 |
| 1 | 11 | 10 | 1 | 2 | 2 |
| 2 | 12 | 11 | 1 | 3 | 3 |
| 3 | 14 | 12 | 2 | 1 | 3 |
| 4 | 15 | 14 | 1 | 2 | 3 |
| 5 | 16 | 15 | 1 | 3 | 3 |

There are two maximal runs: `[10,11,12]` and `[14,15,16]`, each of length `3`. If you remove `14`, the second run shrinks. If you remove `11`, the first run shrinks. If you remove nothing, the best is already `3`. Deletion never bridges the missing `13`, so the final answer is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` in the worst case, where `n` is `seats.length`. Building the hash set is `O(n)` average-case, and sorting up to `n` distinct values dominates. At `10^6` elements this is still practical in most environments; at `10^9`, full in-memory sorting is no longer realistic and the architecture would need external or distributed processing.

### Space Complexity
`O(n)` for the hash set plus the array of distinct values. That storage owns the asymptotic cost. You could reduce duplication by sorting in place after extracting unique values into one buffer, but you cannot avoid linear space entirely if the input is unsorted and duplicate-heavy.

## 💡 Key Takeaways
- If the problem says “duplicates exist, but only presence matters,” first normalize to distinct values before reasoning about structure.
- If the target property is numeric adjacency, sorting plus a single pass is usually the first strong candidate.
- The trap here is overthinking the optional deletion; removing a value cannot repair a missing integer in a consecutive sequence.
- Be careful not to count duplicates as extending a run; deduplication is part of correctness, not just optimization.
- In production systems, normalize noisy upstream state before applying continuity logic; otherwise you optimize the wrong representation.

## 🚀 Variations & Further Practice
- Allow changing or inserting one missing seat instead of deleting one value. The twist is that a single edit can now bridge adjacent runs, so local gap analysis matters.
- Return the actual seat interval, not just its length. The twist is preserving run boundaries and tie-breaking rules during the scan.
- Process seats as a stream with incremental updates. The twist is maintaining longest consecutive ranges online without full re-sort, which pushes toward interval maps or disjoint-set style structures.