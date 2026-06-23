# Combine Smallest File Chunks

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** heap, priority-queue, greedy

---

## 🗂 Problem Overview
Given an array of chunk sizes, repeatedly merge the two smallest chunks until one chunk remains. Each merge costs the sum of the two chosen sizes, and that merged size goes back into the pool for future merges. Return the minimum possible total cost. The non-trivial part is that every merge creates a new candidate that must be efficiently reinserted in sorted order, so a one-time sort is insufficient.

## 🌍 Engineering Impact
This pattern shows up anywhere incremental aggregation has compounding cost: LSM-tree compaction, external merge sort, Huffman-style encoding, media transcoding pipelines, backup/archive assembly, and distributed shuffle or spill-file consolidation. At small scale, naive repeated sorting or linear scans are tolerable; at production scale, they become a latency and CPU tax on critical paths. The heap-based approach preserves optimal merge order while keeping insertion and extraction cheap. Architecturally, it enables predictable throughput under large fan-in workloads where intermediate artifacts continuously re-enter the scheduling set.

## 🔍 Problem Statement
You are given an integer array `chunks` where `chunks[i]` is the size of the `i-th` file chunk. In one operation, select the two smallest available chunks, merge them, and pay a cost equal to their sum. The merged chunk is added back to the available set. Continue until exactly one chunk remains, and return the total cost of all merges.

Constraints:

- `1 <= chunks.length <= 100000`
- `1 <= chunks[i] <= 1000000000`
- The result may exceed 32-bit integer range, so use 64-bit arithmetic.

Examples:

- `chunks = [4, 3, 2, 6]` → `29`
- `chunks = [10]` → `0`

Edge case: if there is only one chunk, no merge happens, so the cost is zero. The key constraint is dynamic reordering after every merge: each new combined chunk must be placed back among the remaining values efficiently, which drives the choice of a min-heap.

## 🪜 How to Solve This
1. Read the operation carefully → the cost depends on **which pair is merged first**, because merged chunks come back and may be charged again later.

2. Ask what minimizes repeated future cost → if a chunk will participate in later merges, making it small for as long as possible is beneficial. That strongly suggests always merging the two smallest chunks first.

3. Notice why sorting once is not enough → after merging `a` and `b`, the new chunk `a + b` must be compared against all remaining chunks. A static sorted array no longer stays valid.

4. Translate that need into a data structure → we need repeated:
   - extract-min
   - extract-min
   - insert

5. That is exactly the `min-heap / priority queue` pattern → each operation is `O(log n)`, and heap construction is `O(n)` or `O(n log n)` depending on the API.

6. Stop condition is simple → when only one chunk remains, all required merges are complete.

7. Use 64-bit accumulation → individual chunk sizes fit in 32 bits, but total merge cost may not.

## 🧩 Algorithm Walkthrough
1. **Initialize a min-heap with all chunk sizes.**  
   This is the core abstraction: a **greedy algorithm implemented with a min-heap / priority queue**. The heap maintains the invariant that the smallest available chunk can always be removed efficiently.

2. **Handle the trivial case implicitly or explicitly.**  
   If the heap has size `1`, no merge is needed and the answer is `0`. This matches the problem definition and avoids unnecessary work.

3. **Repeatedly extract the two smallest chunks.**  
   Pop `x`, then pop `y`. By the greedy choice, these are the cheapest chunks to combine now, and delaying either would only increase future cost because larger values would be propagated earlier.

4. **Compute the merge cost and accumulate it.**  
   Let `merged = x + y`. Add `merged` to `totalCost`. The invariant after each iteration: `totalCost` equals the sum of all merge operations performed so far.

5. **Push the merged chunk back into the heap.**  
   This preserves the set of currently available chunks. Conceptually, two chunks have been replaced by their combined artifact, exactly matching the problem’s state transition.

6. **Continue until one chunk remains.**  
   Each iteration reduces the number of available chunks by one, so the process terminates after `n - 1` merges.

7. **Return `totalCost` as a 64-bit value.**  
   Correctness comes from the standard optimal-merge / Huffman-style greedy argument: always combining the two smallest items minimizes total weighted recombination cost.

## 📊 Worked Example
Example: `chunks = [4, 3, 2, 6]`

| Step | Heap before      | Pop two | Merged | Total cost | Heap after   |
|------|------------------|---------|--------|------------|--------------|
| 0    | `[2, 3, 4, 6]`   | —       | —      | `0`        | `[2, 3, 4, 6]` |
| 1    | `[2, 3, 4, 6]`   | `2, 3`  | `5`    | `5`        | `[4, 5, 6]`  |
| 2    | `[4, 5, 6]`      | `4, 5`  | `9`    | `14`       | `[6, 9]`     |
| 3    | `[6, 9]`         | `6, 9`  | `15`   | `29`       | `[15]`       |

Trace summary:

- First merge the smallest pair: `2 + 3 = 5`
- Reinsert `5`, then merge `4 + 5 = 9`
- Reinsert `9`, then merge `6 + 9 = 15`

Only one chunk remains, so the process stops. Final answer: `29`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log n)` overall. Building the heap is `O(n)` in optimal implementations, then each of the `n - 1` merges performs two extractions and one insertion, each `O(log n)`. At `10^6` elements this is still practical; at `10^9`, even optimal asymptotics are dominated by memory and I/O constraints.

### Space Complexity
`O(n)` extra space for the min-heap holding all active chunks. That storage is fundamental to efficient repeated minimum extraction. You could reduce auxiliary structure only by giving up heap efficiency, which would worsen runtime to repeated scans or re-sorts.

## 💡 Key Takeaways
- If a problem says “repeatedly take the two smallest/largest and put the result back,” think priority queue immediately.
- If each operation creates a new value that must rejoin the candidate set, a one-time sort is usually the wrong abstraction.
- Use 64-bit integers for both `merged` and `totalCost`; overflow is easy when values are up to `1e9` and there are many merges.
- The loop condition is `while heap.size() > 1`; using `> 0` causes an invalid final extraction.
- In production systems, this is the optimal-merge pattern: local greedy scheduling can minimize total downstream recomputation cost when intermediate artifacts re-enter the workflow.

## 🚀 Variations & Further Practice
- **Connect Ropes / Minimum Cost to Connect Sticks** — same heap pattern, different domain framing; useful for recognizing the abstraction independent of wording.
- **Huffman Coding** — same greedy merge idea, but the merged structure is a tree and the objective is minimizing weighted path length, not just reporting total merge cost.
- **Optimal File Merge with ordered constraints** — harder variant where only adjacent files can be merged; the heap greedy strategy no longer works, and dynamic programming becomes necessary.