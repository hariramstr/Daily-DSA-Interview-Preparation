# Next Available Parking Spot

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Heaps and Priority Queues &nbsp;|&nbsp; **Tags:** Heap, Priority Queue, Simulation

---

## 🗂 Problem Overview
Maintain a set of parking spots numbered `1..n`, where smaller numbers are preferred. Some spots start occupied, and each incoming operation either parks a car in the smallest currently free spot or frees a previously occupied spot. Return the assigned spot number for every `"park"` request, or `-1` if none are available. The non-trivial part is supporting up to `100000` dynamic allocate/free operations without rescanning all spots each time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must repeatedly allocate the lowest-cost available resource under churn: seat assignment, port allocation, job-slot scheduling, shard ID reuse, VM host selection, and compiler/register allocation heuristics. At small scale, linear scans are acceptable; at sustained high request rates they become latency amplifiers and contention hotspots. A min-heap turns “find next available” from repeated global search into incremental maintenance of a frontier. That changes the architecture from scan-heavy state management to event-driven resource recycling with predictable `O(log n)` updates and `O(1)` access to the current best candidate.

## 🔍 Problem Statement
You are given:

- An integer `n`, representing parking spots numbered from `1` to `n`
- An array `occupied`, listing spots already taken initially
- An array `operations`, where each operation is either:
  - `["park"]`: assign the smallest free spot, or return `-1` if none exist
  - `["leave", x]`: mark spot `x` as free again

Return an array containing the result of each `"park"` operation in order.

Constraints:

- `1 <= n <= 100000`
- `0 <= occupied.length <= n`
- `occupied` contains distinct values in `1..n`
- `1 <= operations.length <= 100000`
- Every `["leave", x]` is valid: `x` is occupied at that moment

Example 1:
`n = 5, occupied = [2, 4], operations = [["park"], ["park"], ["leave", 2], ["park"], ["park"]]`  
Output: `[1, 3, 2, 5]`

Example 2:
`n = 3, occupied = [1, 2, 3], operations = [["park"], ["leave", 2], ["park"], ["park"]]`  
Output: `[-1, 2, -1]`

The key constraint is dynamic reuse under many operations, which rules out repeated full scans.

## 🪜 How to Solve This
1. Read the problem → the required action is always “give me the smallest currently free spot.” That immediately suggests we need fast access to the minimum free value.

2. Ask what changes over time → spots are removed when parked and reinserted when cars leave. So this is not a one-time sort; it is a dynamic ordered set problem.

3. Consider the simplest structure that supports both operations efficiently:
   - get smallest free spot
   - add a freed spot back  
   A min-heap does exactly that.

4. Build the initial heap from all spots not listed in `occupied`. That gives the starting free inventory.

5. For each operation:
   - `"park"` → pop from the heap if possible, otherwise return `-1`
   - `"leave", x` → push `x` back into the heap

6. Why this works → the heap always contains exactly the free spots, and its root is always the smallest one. That invariant makes each decision local and efficient, instead of repeatedly searching global state.

## 🧩 Algorithm Walkthrough
1. **Initialize occupied membership**
   Create a hash set from `occupied`. This lets us determine in `O(1)` average time whether each spot from `1` to `n` starts free or taken.  
   **Invariant:** the set represents initial non-free spots only; it is used to build the heap correctly.

2. **Build the min-heap of free spots**
   Iterate from `1` to `n`. For every spot not in the occupied set, push it into a min-heap.  
   **Why correct:** every initially free spot is inserted exactly once, and no occupied spot is inserted.  
   **Invariant:** after initialization, the heap contains all and only currently free spots.

3. **Process each operation in order**
   This is a **Heap / Priority Queue simulation** pattern: maintain the best available candidate under incremental updates.  
   - If the operation is `["park"]`:
     - If the heap is empty, append `-1`
     - Otherwise pop the minimum spot and append it
   **Why correct:** the heap root is the smallest free spot by min-heap ordering. Removing it marks that spot occupied.
   **Invariant:** after popping, the heap still contains exactly the remaining free spots.

4. **Handle departures**
   If the operation is `["leave", x]`, push `x` into the heap.  
   **Why correct:** the problem guarantees `x` is occupied at that moment, so reinserting it cannot create duplicates.  
   **Invariant:** the heap is restored to containing exactly the free spots after the leave event.

5. **Return collected park results**
   Only `"park"` operations produce output, so maintain a result array and return it at the end.

## 📊 Worked Example
Use `n = 5`, `occupied = [2, 4]`, `operations = [["park"], ["park"], ["leave", 2], ["park"], ["park"]]`.

Initial free spots: `1, 3, 5` → heap top is `1`.

| Step | Operation      | Heap Before | Action                  | Output |
|------|----------------|-------------|-------------------------|--------|
| 1    | `["park"]`     | `[1,3,5]`   | pop `1`                 | `1`    |
| 2    | `["park"]`     | `[3,5]`     | pop `3`                 | `3`    |
| 3    | `["leave", 2]` | `[5]`       | push `2`                | —      |
| 4    | `["park"]`     | `[2,5]`     | pop `2`                 | `2`    |
| 5    | `["park"]`     | `[5]`       | pop `5`                 | `5`    |

Final result: `[1, 3, 2, 5]`.

The critical state is the heap: it always mirrors the current free inventory, ordered so the next assignment is immediate.

## ⏱ Complexity Analysis
### Time Complexity
Building the initial heap takes `O(n)` if heapified from a list, or `O(n log n)` if inserted one-by-one. Each operation is `O(log n)` in the worst case due to heap push/pop, while empty checks are `O(1)`. At `10^6` operations this remains practical; at `10^9`, even logarithmic factors become operationally significant.

### Space Complexity
`O(n)` space for the free-spot heap, plus `O(k)` for the initial occupied set where `k = occupied.length`. This can be reduced only by trading away fast initialization or membership checks, which usually worsens total runtime.

## 💡 Key Takeaways
- If a problem repeatedly asks for the smallest available item while items are removed and later re-added, think min-heap immediately.
- “Dynamic ordered availability” is the signal: this is not sorting once, it is maintaining a best candidate under updates.
- Be careful to initialize the heap with free spots, not occupied ones; inverting that set is the whole setup.
- On `"park"`, popping from an empty heap must return `-1` rather than failing or inventing a spot.
- The production lesson is broader than parking: maintain reusable resources as an incrementally updated priority structure instead of rescanning global state on every request.

## 🚀 Variations & Further Practice
- Support `"parkPreferred", x"`: assign the smallest free spot greater than or equal to `x`, otherwise wrap or fail. This pushes the problem from heap-only into balanced BST / ordered set territory.
- Add spot classes or costs: choose the cheapest free spot within a zone or capability set. The harder part is maintaining multiple indexed priority views consistently.
- Handle invalid or duplicate leave events in an untrusted stream. This introduces state validation and often requires a separate occupancy set alongside the heap.