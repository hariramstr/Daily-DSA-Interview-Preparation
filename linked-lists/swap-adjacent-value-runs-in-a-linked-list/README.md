# Swap Adjacent Value Runs in a Linked List

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked List, Two Pointers, Pointer Manipulation

---

## 🗂 Problem Overview
Given the head of a singly linked list, treat each maximal block of equal consecutive values as one run, then swap every adjacent pair of runs by rewiring pointers only. Internal node order within each run must remain unchanged. If there is an odd number of runs, the last run stays in place. The challenge is that run boundaries are value-defined, not size-defined, so you must detect groups dynamically while preserving O(n) time and O(1) extra space.

## 🌍 Engineering Impact
This pattern shows up in streaming pipelines, log compaction, telemetry aggregation, and event-processing systems where adjacent identical records are already coalesced into runs. Reordering at the run level lets you transform structure without materializing new payloads or copying large buffers. At scale, that matters: copying nodes or flattening into arrays increases memory pressure, cache misses, and GC churn. Pointer-only rewrites preserve throughput and latency under high-volume ingestion. The broader lesson is architectural: when data already carries locality or grouping, operate on those natural segments instead of decomposing and rebuilding the stream.

## 🔍 Problem Statement
You are given a singly linked list with `0` to `2 * 10^5` nodes. Each node stores an integer in `[-10^9, 10^9]`. A **run** is a maximal contiguous sequence of nodes with the same value. The task is to swap every two adjacent runs and return the new head of the list, without allocating a copied list or changing node values.

If the runs are `R1, R2, R3, R4, ...`, the output should be `R2, R1, R4, R3, ...`. If the total number of runs is odd, the final run remains unchanged.

Examples:

- Input: `[4,4,1,1,1,3,2,2]`  
  Output: `[1,1,1,4,4,2,2,3]`

- Input: `[7,7,5,6,6,6,9]`  
  Output: `[5,7,7,9,6,6,6]`

The key constraint is `O(n)` time with `O(1)` extra space, which rules out buffering runs in auxiliary arrays.

## 🪜 How to Solve This
1. Read the problem → this is not “swap every two nodes.” The unit of movement is a **run**, where boundaries are discovered from values.

2. Since the list is singly linked, any safe reordering needs explicit control over:
   - the node before the pair of runs,
   - the start/end of the first run,
   - the start/end of the second run,
   - the node after the pair.

3. That immediately suggests a pointer-manipulation approach with a dummy head. The dummy removes head-special-case logic when the first two runs are swapped.

4. For each iteration, scan forward to find the end of the first run. Then scan again to find the end of the second run. If no second run exists, stop: the trailing odd run stays where it is.

5. Once both runs are identified, the relink is local:
   - connect previous segment to run 2,
   - connect run 2 tail to run 1,
   - connect run 1 tail to the remainder.

6. Advance to the tail of the swapped pair and repeat. Each node is visited a constant number of times, so the approach stays linear.

## 🧩 Algorithm Walkthrough
1. **Initialize a dummy node before `head`.**  
   This is the standard **Two Pointers / Pointer Manipulation** pattern for linked-list rewiring. The dummy guarantees there is always a stable predecessor for the pair being swapped, including when the head changes.

2. **Set `prev` to the dummy and `curr` to the current run start.**  
   Invariant: everything before `prev` is already correctly arranged, and `curr` is the first node of the next unsolved suffix.

3. **Identify the first run `[run1Start ... run1End]`.**  
   Move `run1End` forward while the next node has the same value. This preserves the invariant that runs are maximal contiguous equal-value segments.

4. **Check whether a second run exists.**  
   Let `run2Start = run1End.next`. If it is `null`, there is no adjacent run to swap with, so the remaining suffix is already correct.

5. **Identify the second run `[run2Start ... run2End]`.**  
   Advance `run2End` while the next node matches `run2Start.val`. Let `nextPair = run2End.next`. Now the exact local structure is isolated.

6. **Rewire the four boundaries.**  
   Set `prev.next = run2Start`, `run2End.next = run1Start`, and `run1End.next = nextPair`. This swaps whole runs while preserving internal order because nodes inside each run are untouched.

7. **Advance for the next iteration.**  
   Set `prev = run1End` and `curr = nextPair`. Invariant restored: prefix through `prev` is finalized, and the remaining suffix is unprocessed.

## 📊 Worked Example
Example: `4 -> 4 -> 1 -> 1 -> 1 -> 3 -> 2 -> 2`

| Step | Run 1 | Run 2 | Rewired Result So Far | `prev` after swap |
|---|---|---|---|---|
| Start | `[4,4]` | `[1,1,1]` | dummy -> `[1,1,1] -> [4,4] -> 3 -> 2 -> 2` | tail of `[4,4]` |
| Next | `[3]` | `[2,2]` | dummy -> `[1,1,1] -> [4,4] -> [2,2] -> [3]` | tail of `[3]` |
| End | none | none | `[1,1,1,4,4,2,2,3]` | done |

Trace:
1. Scan first run: value `4`, ends at second `4`.
2. Scan second run: value `1`, ends at third `1`.
3. Swap those two run segments.
4. Continue from `3`.
5. Scan `[3]` and `[2,2]`, swap them.
6. No nodes remain; return `dummy.next`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each node is traversed a constant number of times while discovering run boundaries and performing local rewires. There is no nested rescanning over previously processed nodes. At `10^6` nodes this is practical in one pass; at `10^9`, runtime is dominated by raw pointer chasing and memory bandwidth, not algorithmic overhead.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a fixed number of pointers: dummy, predecessor, run starts/ends, and next segment. Space cannot be meaningfully reduced further without sacrificing clarity or making head updates error-prone.

## 💡 Key Takeaways
- If the problem says “maximal contiguous equal values,” the real unit is a run, not an individual node.
- If you must reorder linked-list segments in place with changing head semantics, reach for dummy-head pointer manipulation immediately.
- The most common bug is failing to preserve `run2End.next` before rewiring, which loses the remainder of the list.
- Another trap is misidentifying run boundaries by comparing against a moving pointer instead of the run’s start value.
- In production systems, segment-level transforms outperform element-level rebuilds when the data already has natural locality.

## 🚀 Variations & Further Practice
- Swap adjacent runs only if their lengths differ by at most `k`; the twist is that you must compute run lengths while still doing one-pass in-place rewiring.
- Reverse the order of every `k` consecutive runs instead of swapping pairs; this generalizes local segment rewrites from 2-way to k-way relinking.
- Merge adjacent runs after swapping if the new boundary creates equal neighboring values; the harder part is maintaining maximal-run semantics during mutation.