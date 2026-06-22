# Longest Compliance Window with Forbidden Pair Threshold

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given an integer array `events`, a deduplicated set of unordered `forbiddenPairs`, and a threshold `limit`, find the longest contiguous subarray whose total number of conflicting index pairs is at most `limit`. A conflict is created whenever two values inside the window match a forbidden rule, including self-pairs like `[x, x]`. The hard part is dynamic pair counting: adding or removing one event can change the conflict total by many pairs, so naive recomputation per window is infeasible.

## 🌍 Engineering Impact
This pattern shows up in streaming risk engines, abuse detection, SIEM correlation, ad-serving policy enforcement, and distributed rate-limiters where co-occurrence rules matter within bounded time windows. The operational challenge is not membership testing but maintaining aggregate interaction counts under continuous insertions and evictions. Without an incremental windowed approach, systems degrade into repeated full-window scans, blowing latency budgets and forcing smaller retention windows. With the right counting model, you can enforce pairwise constraints online, preserve throughput under skewed distributions, and keep memory proportional to active keys rather than historical volume.

## 🔍 Problem Statement
You are given:

- `events`, where `1 <= events.length <= 200000`
- `forbiddenPairs`, where `1 <= forbiddenPairs.length <= 200000`
- `limit`, where `0 <= limit <= 10^15`

Each `forbiddenPairs[i] = [a, b]` defines an unordered forbidden rule between groups `a` and `b`. Duplicates in `forbiddenPairs` count once. For any contiguous window, its conflict count is the number of index pairs `(i, j)` inside the window with `i < j` such that `events[i]` and `events[j]` form a forbidden pair. If the rule is `[x, x]`, then every equal-value pair of `x` contributes.

Return the maximum window length whose conflict count is at most `limit`.

Examples:

- `events = [1,2,1,3,2,1]`, `forbiddenPairs = [[1,2],[2,3]]`, `limit = 2` → `4`
- `events = [4,4,4,2,4]`, `forbiddenPairs = [[4,4],[2,4]]`, `limit = 3` → `3`

The key constraint is scale: `O(n^2)` or recomputing pair counts per window is not viable.

## 🪜 How to Solve This
1. Read the problem → this is a longest-valid-subarray question, so start with a sliding window.
2. The obstacle appears immediately: validity is not based on a simple sum or distinct count. One new event can create many new conflicts with existing values in the window.
3. That suggests an incremental counting model. Ask: when I append value `x`, how many new forbidden pairs does it create? Exactly the number of existing occurrences of every value `y` such that `{x, y}` is forbidden.
4. So maintain:
   - frequency of each value in the current window
   - adjacency list of forbidden partners for each value
   - current total conflict count
5. On right expansion with `x`, add `freq[y]` for every forbidden neighbor `y` of `x`. For self-rule `[x, x]`, this naturally adds the current `freq[x]`, which is the number of new equal pairs formed.
6. If conflicts exceed `limit`, shrink from the left. Removing value `x` should subtract the number of pairs that event participated in with the remaining window, so decrement `freq[x]` first, then subtract `freq[y]` for each forbidden neighbor `y`.
7. Because each pointer only moves forward, this yields a linear scan plus graph-neighbor work.

## 🧩 Algorithm Walkthrough
1. **Normalize forbidden rules.**  
   Treat `[a, b]` and `[b, a]` as the same rule by storing each pair in canonical order `(min(a,b), max(a,b))`. Deduplicate before building adjacency lists. This prevents double-counting from duplicate input rules.

2. **Build a forbidden-neighbor map.**  
   For each unique rule `(a, b)`, add `b` to `adj[a]`. If `a != b`, also add `a` to `adj[b]`; if `a == b`, keep a single self-edge. This gives the exact set of values that can form new conflicts with a value entering or leaving the window.

3. **Use the Two Pointers / Sliding Window pattern.**  
   Maintain `left`, iterate `right`, and keep `freq[value]` for the active window. The invariant is: `conflicts` equals the total forbidden-pair count for `events[left..right]`.

4. **Expand right by one event `x`.**  
   Before incrementing `freq[x]`, add to `conflicts` the number of existing window elements that conflict with `x`: `sum(freq[y] for y in adj[x])`. This is correct because the new event only creates pairs with elements already in the window. Then increment `freq[x]`.

5. **Shrink while invalid.**  
   If `conflicts > limit`, repeatedly remove `events[left] = x`. First decrement `freq[x]`, then subtract `sum(freq[y] for y in adj[x])`, because after decrement, `freq[y]` counts exactly the remaining partners of the removed event. Advance `left`.

6. **Track the best length.**  
   After restoring validity, update `best = max(best, right - left + 1)`. Since every valid window ending at `right` is considered after minimal shrinking, the maximum is found.

This abstraction works because pair creation and pair removal are both local to the entering or exiting value; no full-window recomputation is needed.

## 📊 Worked Example
Example: `events = [4,4,4,2,4]`, `forbiddenPairs = [[4,4],[2,4]]`, `limit = 3`

Rules: `adj[4] = {4,2}`, `adj[2] = {4}`

| Step | Action | Window | freq(4) | freq(2) | conflicts |
|---|---|---|---:|---:|---:|
| 1 | add 4 | `[4]` | 1 | 0 | 0 |
| 2 | add 4, add `freq[4]=1` | `[4,4]` | 2 | 0 | 1 |
| 3 | add 4, add `freq[4]=2` | `[4,4,4]` | 3 | 0 | 3 |
| 4 | add 2, add `freq[4]=3` | `[4,4,4,2]` | 3 | 1 | 6 |
| 5 | shrink remove 4: dec `freq[4]`, subtract `freq[4]+freq[2]=2+1` | `[4,4,2]` | 2 | 1 | 3 |
| 6 | add 4, add `freq[4]+freq[2]=2+1` | `[4,4,2,4]` | 3 | 1 | 6 |
| 7 | shrink remove 4 → subtract 3 | `[4,2,4]` | 2 | 1 | 3 |

Best valid length is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + U + Σ deg(events[i])))` in adjacency-list terms, commonly described as `O(n + m + window_updates_by_neighbor_scan)`, where `m` is the number of unique forbidden rules. Each pointer moves at most `n` times, but each add/remove scans the forbidden neighbors of that value. This is practical when rule degree is moderate; at `10^6` scale it remains viable, while quadratic rescans do not.

### Space Complexity
`O(m + k)`, where `m` is the number of unique forbidden rules and `k` is the number of distinct values currently tracked in `freq` or adjacency. The adjacency map owns most of the space. You can compress with arrays instead of hash maps for bounded value ranges, trading flexibility for lower constant factors.

## 💡 Key Takeaways
- If the question asks for the longest contiguous region under a threshold and validity changes incrementally as the window moves, think sliding window before considering heavier machinery.
- If adding one element affects the score by “how many matching things already exist,” frequency counting plus incremental pair accounting is the signal.
- Deduplicate forbidden rules up front; otherwise duplicate input edges silently inflate conflict counts.
- On window shrink, decrement the outgoing value’s frequency before subtracting its contribution, or self-pairs and cross-pairs will be off by one.
- The transferable design insight is to model expensive global metrics as sums of local delta updates at insertion/eviction boundaries.

## 🚀 Variations & Further Practice
- **Weighted forbidden pairs:** each rule `[a, b]` contributes weight `w(a,b)` per matching index pair. Same window pattern, but deltas become weighted frequency sums.
- **Online updates to the rule set:** forbidden pairs can be added or removed during processing. Harder because the window score must be adjusted against current frequencies without rescanning history.
- **2D or time-bucketed windows:** events are constrained by both contiguity and timestamp range. The conceptual twist is maintaining pair counts while evicting by time rather than just index.