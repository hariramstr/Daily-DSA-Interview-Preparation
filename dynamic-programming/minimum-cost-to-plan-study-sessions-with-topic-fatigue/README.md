# Minimum Cost to Plan Study Sessions with Topic Fatigue

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, partition-dp

---

## 🗂 Problem Overview
Given two arrays, `time` and `topic`, partition the chapters into contiguous study sessions that preserve the original order. Each session costs the maximum study time in that segment plus the number of topic transitions inside it. The goal is to minimize total cost across all sessions. The non-trivial part is that every split changes future options, so local choices like “cut on topic change” or “group large times together” are not reliably optimal.

## 🌍 Engineering Impact
This is the same optimization shape that shows up in batch formation across ordered workloads: streaming pipelines grouping adjacent events, log compaction windows, compiler pass chunking, media transcoding batches, and warehouse picking routes with setup-switch penalties. Each batch has a dominant fixed cost plus an intra-batch transition cost. At scale, greedy heuristics fragment work or over-aggregate it, both of which waste throughput and inflate tail latency. Prefix dynamic programming gives an exact decision boundary: for every prefix, what is the cheapest way to end the last batch here? That pattern enables predictable planning under ordering constraints.

## 🔍 Problem Statement
You are given `n` chapters in fixed order, where chapter `i` has study time `time[i]` and topic `topic[i]`. You must partition the array into contiguous sessions, covering every chapter exactly once.

For a session spanning indices `j..i`, its cost is:

`max(time[j..i]) + topic_changes(j..i)`

where `topic_changes(j..i)` counts how many times `topic[k] != topic[k-1]` for `k` in `[j+1, i]`.

Return the minimum total cost over all valid partitions.

Constraints:
- `1 <= n <= 1000`
- `1 <= time[i], topic[i] <= 10^6`

Examples:
- `time = [3,1,4,2]`, `topic = [1,1,2,2]` → `6`
- `time = [5,2,6,1,3]`, `topic = [7,7,7,8,7]` → `8`

The key constraint is `n <= 1000`: large enough that brute-force partition enumeration is impossible, but small enough for `O(n^2)` dynamic programming over prefixes.

## 🪜 How to Solve This
1. Read the problem → this is not about reordering or selecting items; it is about **partitioning an ordered array into contiguous groups**.

2. Whenever the task is “split a prefix optimally,” think **prefix DP**. Let `dp[i]` mean the minimum cost to cover the first `i` chapters.

3. To compute `dp[i]`, assume the last session starts at some earlier position `j`. Then the answer is:
   `dp[j] + cost of session [j..i-1]`.

4. That immediately suggests trying all possible `j` for each endpoint `i`. The remaining question is whether session cost can be updated incrementally.

5. Scan backward from `i-1` to `0`, maintaining:
   - the running maximum `time`
   - the running count of topic changes

6. As you extend the session leftward by one chapter, both values are easy to update in `O(1)`. That turns each `dp[i]` computation into a linear backward scan.

7. Total work becomes `O(n^2)`, which is exactly the right fit for `n <= 1000`.

The mental model: “choose where the last cut was,” then evaluate that last segment efficiently.

## 🧩 Algorithm Walkthrough
1. **Define the DP state (Partition DP).**  
   Let `dp[i]` be the minimum total cost to cover chapters `0..i-1`. This is correct because every valid plan for the first `i` chapters ends with exactly one final contiguous session. The invariant is: `dp[i]` stores the optimal cost for that prefix.

2. **Initialize the base case.**  
   Set `dp[0] = 0`, since covering zero chapters costs nothing. This anchors transitions where the first session starts at index `0`.

3. **Enumerate the end of the last session.**  
   For each `i` from `1` to `n`, compute `dp[i]`. We are deciding the best way to end a plan exactly at chapter `i-1`.

4. **Scan backward over all possible session starts.**  
   Let the last session be `j..i-1`, where `j` goes from `i-1` down to `0`. Maintain:
   - `mx = max(time[j..i-1])`
   - `changes = number of topic transitions inside j..i-1`

   When moving from `j+1` to `j`, update:
   - `mx = max(mx, time[j])`
   - if `j < i-1` and `topic[j] != topic[j+1]`, increment `changes`

   This is correct because every internal boundary in the session is counted exactly once.

5. **Relax the DP transition.**  
   Candidate cost is `dp[j] + mx + changes`. Take the minimum over all `j`. The invariant after finishing the backward scan is: `dp[i]` equals the cheapest partition of the first `i` chapters.

6. **Return `dp[n]`.**  
   This is the optimal total cost for the full array. The pattern is classic **partition dynamic programming**: optimize over all valid last segments of a prefix.

## 📊 Worked Example
Use `time = [5,2,6,1,3]`, `topic = [7,7,7,8,7]`.

Let `dp[i]` be min cost for first `i` chapters.

| `i` | Backward session choices `j..i-1` | Best `dp[i]` |
|---|---|---|
| 1 | `[5]`: `0 + 5 + 0 = 5` | 5 |
| 2 | `[2]`: `5+2=7`, `[5,2]`: `0+5+0=5` | 5 |
| 3 | `[6]`: `5+6=11`, `[2,6]`: `5+6=11`, `[5,2,6]`: `0+6+0=6` | 6 |
| 4 | `[1]`: `6+1=7`, `[6,1]`: `5+6+1=12`, `[2,6,1]`: `5+6+1=12`, `[5,2,6,1]`: `0+6+1=7` | 7 |
| 5 | `[3]`: `7+3=10`, `[1,3]`: `6+3+1=10`, `[6,1,3]`: `5+6+2=13`, `[2,6,1,3]`: `5+6+2=13`, `[5,2,6,1,3]`: `0+6+2=8` | 8 |

Final answer: `dp[5] = 8`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)`. For each endpoint `i`, we scan all possible start positions `j` of the last session once, updating the running maximum and topic-change count in `O(1)`. At `n = 1000`, this is about one million state transitions, which is trivial. At `10^6`, quadratic growth becomes completely impractical.

### Space Complexity
`O(n)` for the `dp` array. The session statistics are maintained incrementally with constant extra space during each backward scan. Space can’t be reduced below linear without losing prior prefix optima, unless you accept recomputation that worsens runtime.

## 💡 Key Takeaways
- If the problem says “partition an ordered array into contiguous groups” and asks for a minimum total cost, prefix-based partition DP should be your default hypothesis.
- If each group’s cost can be updated incrementally while expanding or shrinking a boundary, an `O(n^2)` DP is often the intended solution.
- Use `dp[i]` for the first `i` items, not the first `i-1`; this avoids awkward base cases and makes transitions from `dp[j]` to segment `j..i-1` clean.
- When scanning backward, count topic changes using adjacent pairs inside the current segment; the update is triggered by comparing `topic[j]` with `topic[j+1]`, not with the segment end.
- In production planning systems, exact prefix optimization is often the difference between stable batching policy and heuristic drift that silently degrades cost, latency, or utilization.

## 🚀 Variations & Further Practice
- Add a hard cap on total session duration or chapter count. The twist is that not every `j..i` segment is feasible, so transitions must enforce capacity constraints.
- Charge session cost as `max(time) + topic_changes^2` or another non-linear penalty. The harder part is that the segment cost remains incremental, but the optimization landscape becomes less intuitive and greedier heuristics fail harder.
- Allow skipping or reordering a bounded number of chapters. The twist is that contiguity breaks, and the state must now track both prefix position and edit budget, increasing dimensionality.