# Minimum Cost to Archive Photos with Album Switching Fee

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, state-compression, sequence-optimization

---

## 🗂 Problem Overview
You process a photo sequence left to right and must assign each photo to Album A or Album B. Within each album, original order must be preserved. Appending a photo incurs a cost based only on the previous category already placed in that same album; empty albums cost nothing. The goal is to minimize total cost. The challenge is that local greedy choices fail, while tracking full album histories is too expensive for `n <= 2000`.

## 🌍 Engineering Impact
This pattern shows up in streaming systems that must partition ordered events across a small number of lanes while minimizing transition penalties: log compaction pipelines, media transcoding queues, packet classification paths, and compiler or ML preprocessing stages that preserve per-lane order. At scale, the wrong state model explodes combinatorially because full history is irrelevant but tempting to track. The right abstraction—retain only the last item per lane—turns an intractable assignment problem into a practical DP. That enables predictable latency, bounded memory, and implementation simplicity when processing long ordered streams under asymmetric transition costs.

## 🔍 Problem Statement
Given `photos` of length `n` and an `m x m` matrix `switchCost`, assign each photo to one of two albums while processing from left to right. Order inside each album must match the original sequence. If a photo of category `x` is appended to an empty album, cost is `0`; otherwise, if the previous category in that album was `y`, the added cost is `switchCost[y][x]`. Return the minimum total cost.

Constraints:
- `1 <= n <= 2000`
- `1 <= m <= 50`
- `1 <= photos[i] <= m`
- `0 <= switchCost[a][b] <= 10^6`
- Answer fits in signed 64-bit integer

Examples:
- `photos = [1,2,1]`, `switchCost = [[0,5],[4,0]]` → `0`
- `photos = [1,3,2,3]`, `switchCost = [[0,7,2],[6,0,5],[4,1,0]]` → `1`

The key constraint is `n = 2000`: too large for exponential assignment search, small enough for `O(n^2)` dynamic programming.

## 🪜 How to Solve This
1. Read the problem → each decision places the current photo into one of two ordered containers.
2. Notice what affects future cost → not the full contents of each album, only the most recent category in each album.
3. But tracking both last categories directly still feels awkward because albums are built from the same prefix of photos.
4. Reframe the state around the processed prefix: after placing photo `i`, one album must end with `photos[i]`; the other album ends with some earlier photo `j`, or is empty.
5. That gives a compact DP state: `dp[i][j]` = minimum cost after processing up to `i`, where one album’s last photo is `i` and the other album’s last photo is `j`.
6. From there, only two actions exist for photo `i+1`:
   - append it to the album that currently ends at `i`
   - append it to the album that currently ends at `j`
7. Each transition updates one “last photo” pointer and adds exactly one directional switch cost.
8. The result is a classic state-compressed sequence DP: preserve order, keep only frontier state, avoid history explosion.

## 🧩 Algorithm Walkthrough
1. **Define the pattern: dynamic programming with state compression.**  
   We process the sequence prefix by prefix. The full assignment history is irrelevant once we know the last photo in each album, because future costs depend only on those last categories.

2. **Choose the state.**  
   Let `dp[i][j]` be the minimum cost after processing photos `0..i`, where:
   - one album’s most recent photo is `photos[i]`
   - the other album’s most recent photo is `photos[j]`
   - `j = -1` means the other album is still empty  
   In implementation, use `j` in `0..i-1` plus a sentinel for empty.

3. **Initialize the base case.**  
   After placing the first photo, one album ends at index `0` and the other is empty. Cost is `0`.

4. **Transition 1: append `photos[i+1]` to the album ending at `i`.**  
   The other album still ends at `j`. Added cost is `switchCost[photos[i]][photos[i+1]]`.  
   This maintains the invariant that after processing `i+1`, one album ends at `i+1`, the other at `j`.

5. **Transition 2: append `photos[i+1]` to the album ending at `j`.**  
   Then the album previously ending at `i` becomes the “other” album. New state is `dp[i+1][i]`.  
   Added cost is `0` if `j` is empty, else `switchCost[photos[j]][photos[i+1]]`.

6. **Take minima over all predecessor states.**  
   Every valid assignment of the prefix maps to exactly one DP state, and every transition corresponds to one legal album choice. That gives optimal substructure and correctness.

7. **Return the minimum value in the last DP row.**  
   After all photos are processed, either album may hold the final photo, but the state representation already normalizes that.

## 📊 Worked Example
Use `photos = [1, 3, 2, 3]`.

Let `dp[i][j]` mean: after processing index `i`, one album ends at `i`, the other at `j` (`E` = empty).

| Step | State | Cost |
|---|---|---:|
| Start | `dp[0][E]` | 0 |
| Add photo `3` to same album as `1` | `dp[1][E]` | `0 + cost(1→3)=2` |
| Add photo `3` to empty other album | `dp[1][0]` | `0` |
| From `dp[1][0]`, add `2` after `3` | `dp[2][0]` | `0 + cost(3→2)=1` |
| From `dp[1][0]`, add `2` after `1` | `dp[2][1]` | `0 + cost(1→2)=7` |
| From `dp[2][0]`, add final `3` after `2` | `dp[3][0]` | `1 + cost(2→3)=0 = 1` |
| From `dp[2][0]`, add final `3` after `1` | `dp[3][2]` | `1 + cost(1→3)=2 = 3` |

Minimum final cost is `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)`. There are `O(n^2)` DP states because for each `i`, the second album’s last index `j` ranges over prior positions plus empty. Each state has `O(1)` transitions. At `n = 2000`, this is practical; at `10^6`, it is not, and a different structural assumption would be required.

### Space Complexity
`O(n^2)` for the full DP table, or `O(n)` with rolling arrays because each row depends only on the previous row plus one aggregated update target. The DP structure owns the space. Rolling space reduces memory but makes reconstruction/debugging less convenient.

## 💡 Key Takeaways
- If a sequence assignment problem preserves order and transition cost depends only on the previous item in each lane, think DP over “last used positions,” not full partitions.
- When there are only two containers, a strong signal is that one current item can be treated as fixed in one container, leaving the other container’s frontier as the real state dimension.
- The empty-album case is a real state, not a special afterthought; model it explicitly or you will mischarge the first placement into an album.
- `switchCost[a][b]` is directional; reusing symmetric intuition will silently produce wrong answers on valid inputs.
- The transferable design insight is state minimization: preserve exactly the information future decisions depend on, and discard everything else.

## 🚀 Variations & Further Practice
- Extend from 2 albums to `k` albums. The conceptual twist is state dimensionality explosion; you need stronger compression, symmetry reduction, or approximation.
- Allow reordering within a bounded window instead of strict global order. Now the problem mixes scheduling with transition-cost optimization and loses the clean prefix-DP structure.
- Add capacity constraints or per-album penalties. This introduces another resource dimension, forcing multi-constraint DP or shortest-path formulations over a larger state graph.