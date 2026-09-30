# Maximum Consecutive Seats After One Reservation Move

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Greedy, Prefix Sum

---

## 🗂 Problem Overview
Given a binary array `seats`, move at most one `1` to any `0` and maximize the length of the longest contiguous run of `1`s. The move preserves the total number of reserved seats, so every gain comes from relocating existing capacity rather than creating new capacity. The non-trivial part is that filling a gap can also destroy another block, so local merges are only valid when there is an extra reserved seat available elsewhere.

## 🌍 Engineering Impact
This pattern shows up in systems that rebalance scarce discrete allocations under a single mutation budget: seat maps, shard placement, CPU pinning, contiguous memory/page compaction, and time-slot scheduling. The core question is whether one relocation can increase usable contiguous capacity without reducing another critical region below value. At scale, brute-force “try every move” logic collapses under high-cardinality arrays or hot-path scheduling loops. A linear pass with precomputed segment information enables deterministic latency, predictable memory behavior, and easier reasoning about correctness under operational constraints where one bad relocation can fragment the system instead of improving it.

## 🔍 Problem Statement
You are given a binary array `seats` of length up to `2 * 10^5`, where `1` means reserved and `0` means empty. You may perform at most one move: pick one reserved seat, clear it, and place that reservation into any empty seat. Return the maximum possible length of consecutive `1`s after that move, or after doing nothing.

Key edge case: a zero between two runs of `1`s is not always mergeable into `left + 1 + right`. That only works if there exists at least one reserved seat outside those two runs to move into the gap; otherwise the move must come from one of the runs and shrinks what you are trying to build.

Examples:

- `seats = [1,1,0,1,0,1,1,1]` → `4`
- `seats = [1,0,1,1,0,1]` → `3`

The array size rules out quadratic simulation of all possible moves.

## 🪜 How to Solve This
1. Read the move carefully → this is not “flip one zero to one.” Total count of `1`s stays fixed, so every improvement must borrow a reservation from somewhere else.

2. Think in runs, not individual seats → the only places worth considering are zeros adjacent to existing `1` blocks, because isolated fills cannot beat extending or merging runs.

3. For any zero, ask two questions:
   - how many consecutive `1`s are immediately on its left?
   - how many consecutive `1`s are immediately on its right?

4. If you fill that zero, the tempting candidate is `left + 1 + right`. But that is only achievable if there is at least one extra `1` outside those adjacent runs. Otherwise you must steal from one of them, and the best you can realize is just `left + right`.

5. That suggests precomputing contiguous-run lengths around every index in linear time, plus the total number of `1`s.

6. Then scan each zero once, compute the best achievable block centered there, and take the maximum against the existing longest run. This is a greedy local evaluation backed by global count information.

## 🧩 Algorithm Walkthrough
1. **Count total reserved seats.**  
   Let `ones = sum(seats)`. This gives the global budget. No answer can exceed `ones`, because a move does not create reservations.

2. **Build left-run lengths using a prefix-style pass.**  
   Create `left[i] = number of consecutive 1s ending at i`.  
   If `seats[i] == 1`, then `left[i] = left[i-1] + 1`; otherwise `0`.  
   Invariant: after processing `i`, `left[i]` exactly captures the contiguous block touching `i` from the left.

3. **Build right-run lengths using a suffix-style pass.**  
   Create `right[i] = number of consecutive 1s starting at i`.  
   If `seats[i] == 1`, then `right[i] = right[i+1] + 1`; otherwise `0`.  
   Invariant: after processing `i`, `right[i]` captures the contiguous block touching `i` from the right.

4. **Track the current best without any move.**  
   The maximum value in `left` is the longest existing run. This handles “do nothing” and all-ones arrays correctly.

5. **Evaluate each zero as a candidate insertion point.**  
   For zero at `i`, let:
   - `L = (i > 0) ? left[i-1] : 0`
   - `R = (i + 1 < n) ? right[i+1] : 0`

   Filling this zero could connect or extend adjacent runs.

6. **Apply the move-feasibility rule.**  
   If `L + R < ones`, there exists at least one reserved seat outside those adjacent runs, so candidate length is `L + 1 + R`.  
   Otherwise all reserved seats already belong to those runs, so any inserted `1` must be stolen from them; candidate length is `L + R`.

7. **Take the maximum over all candidates.**  
   This is a **greedy + prefix/suffix run-length** pattern: each zero is evaluated independently using local structure and one global invariant (`ones`). That abstraction is correct because a single move can only affect one insertion point and one source seat.

## 📊 Worked Example
Use `seats = [1,1,0,1,0,1,1,1]`.

| i | seats[i] | left[i] | right[i] |
|---|---:|---:|---:|
| 0 | 1 | 1 | 2 |
| 1 | 1 | 2 | 1 |
| 2 | 0 | 0 | 0 |
| 3 | 1 | 1 | 1 |
| 4 | 0 | 0 | 0 |
| 5 | 1 | 1 | 3 |
| 6 | 1 | 2 | 2 |
| 7 | 1 | 3 | 1 |

`ones = 6`. Existing best run is `3`.

Now inspect zeros:

1. `i = 2`  
   `L = left[1] = 2`, `R = right[3] = 1`  
   `L + R = 3 < 6`, so there is an extra `1` elsewhere.  
   Candidate = `2 + 1 + 1 = 4`.

2. `i = 4`  
   `L = left[3] = 1`, `R = right[5] = 3`  
   `L + R = 4 < 6`, so candidate = `1 + 1 + 3 = 5` seems possible locally, but moving the only `1` from index `3` would break the left side. The formula still holds because the extra `1` can come from outside these runs only if one exists; here it does: indices `0` or `1`. Resulting best block is `4` contiguous on the right after valid relocation.

Answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass counts `1`s, one pass builds left-run lengths, one pass builds right-run lengths, and one pass evaluates zeros. The dominant operation is linear scanning with constant-time work per index. At `10^6` elements this is routine; at `10^9`, the issue becomes memory bandwidth and storage, not asymptotic behavior.

### Space Complexity
`O(n)` for the `left` and `right` arrays storing run lengths. This can be reduced with more careful run-based processing or by storing segment boundaries instead of per-index state, but the straightforward version is simpler and less error-prone.

## 💡 Key Takeaways
- If the problem says “move one existing item” rather than “flip one bit,” treat total resource count as a hard invariant.
- When maximizing contiguous structure in a binary array, convert the array into run-length information before considering local edits.
- The merge formula is not always `left + 1 + right`; it depends on whether an extra `1` exists outside the adjacent runs.
- Boundary handling is easy to get wrong: for a zero at either end, one of `L` or `R` must be `0`, not an out-of-bounds read.
- In production allocation systems, local optimization is only valid when checked against global capacity constraints; otherwise “improvements” just move fragmentation elsewhere.

## 🚀 Variations & Further Practice
- Allow up to `k` reservation moves instead of one. The twist is that local zero evaluation no longer suffices; you need a sliding-window or budgeted-gap formulation.
- Generalize from binary seats to weighted reservations where moving a reservation has cost or priority. The harder part is optimizing contiguous value, not just contiguous count.
- Extend to 2D seating blocks or memory pages. The conceptual jump is from 1D run merging to connected-component growth under relocation constraints.