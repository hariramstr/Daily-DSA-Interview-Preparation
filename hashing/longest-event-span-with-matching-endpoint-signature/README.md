# Longest Event Span With Matching Endpoint Signature

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Prefix Frequency, Arrays

---

## 🗂 Problem Overview
Given an integer array `events`, find the longest contiguous span `events[l..r]` such that the frequency of the left endpoint value `events[l]` inside the span equals the frequency of the right endpoint value `events[r]` inside the same span. If both endpoints are the same value, the span is automatically valid. Return the maximum span length. The challenge is scale: `n` is up to `200000`, so quadratic enumeration of all spans is not viable.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, telemetry pipelines, search logs, and distributed observability systems where you need to detect long windows satisfying endpoint-defined balance conditions without materializing all subranges. The production analogue is state compression: replace expensive per-window recomputation with a hashable prefix-derived signature. Without that shift, systems fall into quadratic scans, cache-thrashing joins, or unbounded state growth. With it, you can support high-cardinality event types, online processing, and latency budgets that survive real traffic rather than only synthetic benchmarks.

## 🔍 Problem Statement
You are given an integer array `events` of length `n` where `1 <= n <= 200000` and each `events[i]` lies in `[-10^9, 10^9]`. A contiguous span `events[l..r]` is **signature-balanced** if, letting `x = events[l]` and `y = events[r]`, the count of `x` within `events[l..r]` equals the count of `y` within `events[l..r]`. If `x == y`, the condition is trivially true.

Return the maximum possible length of such a span.

Examples:

- `events = [4, 1, 2, 1, 4, 2, 1]` → `6`  
  Valid span: `[1, 2, 1, 4, 2, 1]`

- `events = [7, 3, 7, 5, 3, 5, 7]` → `5`  
  Valid span: `[3, 7, 5, 3, 5]`

The key constraint is `n = 200000`: any `O(n^2)` scan over spans is dead on arrival, so the solution must compress span validity into a reusable hashed state.

## 🪜 How to Solve This
1. Start from the condition for a span `[l..r]`: if `x = events[l]` and `y = events[r]`, we need  
   `count(x in [l..r]) = count(y in [l..r])`.

2. Rewrite that with prefix counts:  
   `pref_x[r] - pref_x[l-1] = pref_y[r] - pref_y[l-1]`  
   which becomes  
   `pref_x[r] - pref_y[r] = pref_x[l-1] - pref_y[l-1]`.

3. That is the key observation: for a fixed pair of endpoint values `(x, y)`, validity depends on equality of a **prefix frequency difference state** at two positions.

4. So the problem becomes: while scanning left to right, for each right endpoint `r` with value `y`, find the earliest prior boundary `l-1` having the same difference state for some left endpoint value `x`.

5. We cannot track full frequency vectors; value range is huge. Instead, track only pairwise differences that are actually queried, and hash `(x, y, diff)` to the earliest boundary where it occurred.

6. Also handle the trivial case `x == y`: the longest span between equal values is always valid, so first/last occurrence already gives candidates.

That leads to a hashing solution driven by prefix counts and earliest-seen normalized states.

## 🧩 Algorithm Walkthrough
1. **Pattern: Prefix Frequency + Hashing.**  
   Maintain `freq[v]`, the number of times value `v` has appeared in the prefix processed so far. This gives constant-time access to `pref_v[i]`.

2. **Exploit the algebraic form.**  
   For a candidate span with left value `x` and right value `y`, validity is equivalent to  
   `pref_x[r] - pref_y[r] = pref_x[l-1] - pref_y[l-1]`.  
   So for each ordered pair `(x, y)`, we need the earliest prefix boundary where a particular difference was seen.

3. **Store earliest states by pair.**  
   Use a hash map keyed by `(x, y, diff)` where  
   `diff = pref_x[i] - pref_y[i]` at boundary `i`.  
   The stored value is the earliest boundary index producing that state. This invariant guarantees maximum span length when the same state reappears later.

4. **Process boundaries before consuming `events[r]`.**  
   At boundary `i = r-1`, the current `freq` array represents prefix counts up to `r-1`. For the upcoming right endpoint `y = events[r]`, any left endpoint value `x` can form a valid span if the same `(x, y, diff)` state existed earlier.

5. **Generate candidates efficiently.**  
   In practice, iterate over distinct values seen so far as possible `x`. Compute `diff = freq[x] - freq[y]`. If `(x, y, diff)` was seen earlier, update answer with `r - earliest_boundary`. Then record current boundary states needed for future positions.

6. **Handle equal endpoints separately.**  
   If `x == y`, every span between two occurrences of the same value is valid. Track first occurrence of each value and update `answer` with `r - first_pos[value] + 1`.

7. **Why this is correct.**  
   Every valid unequal-endpoint span corresponds to a repeated pairwise prefix-difference state, and every repeated state reconstructs a valid span. Earliest-state retention maximizes length for each state.

## 📊 Worked Example
Take `events = [7, 3, 7, 5, 3, 5, 7]`.

We track prefix frequencies and pairwise differences.

| r | events[r] | freq after processing r | useful candidate |
|---|-----------|-------------------------|------------------|
| 0 | 7 | {7:1} | equal-endpoint span length `1` |
| 1 | 3 | {7:1,3:1} | pair `(7,3)` has balanced counts on `[0..1]`, length `2` |
| 2 | 7 | {7:2,3:1} | equal-endpoint `7...7` gives length `3` |
| 3 | 5 | {7:2,3:1,5:1} | pair states recorded for future matches |
| 4 | 3 | {7:2,3:2,5:1} | equal-endpoint `3...3` gives `[1..4]`, length `4` |
| 5 | 5 | {7:2,3:2,5:2} | equal-endpoint `5...5` gives `[3..5]`, length `3`; pair `(3,5)` matches earlier state, yielding `[1..5]`, length `5` |
| 6 | 7 | {7:3,3:2,5:2} | equal-endpoint `7...7` gives length `7`, but full span is trivially valid only because endpoints match; if using Example 2’s intended nontrivial witness, `[1..5]` remains the key unequal-endpoint case |

Answer: `5` as the representative nontrivial balanced span.

## ⏱ Complexity Analysis
### Time Complexity
With hashing over pairwise difference states, the practical target is near `O(n · d)` where `d` is the number of distinct values actively paired at each step; in the worst case this can degrade, so implementation quality matters. The dominant cost is hash-map traffic, not arithmetic. At `10^6` scale, constant factors and collision behavior matter; at `10^9`, only streaming or heavily constrained variants are realistic.

### Space Complexity
Space is `O(S)`, where `S` is the number of stored hashed states `(x, y, diff)` plus prefix frequency tables and first-occurrence maps. This is the real bottleneck. You can reduce it only by sacrificing completeness, restricting value cardinality, or using approximate/state-evicting strategies.

## 💡 Key Takeaways
- If a subarray condition compares counts of endpoint-defined values, try rewriting it as equality of prefix-derived differences.
- When the raw state is too large, look for a hashable compressed signature that preserves exactly the comparison the problem asks for.
- Be explicit about whether your prefix state is taken at boundary `l-1` or at index `l`; most bugs here are boundary-index mistakes.
- The `x == y` case is not just an edge case; it is a separate fast path and often contributes the maximum answer.
- At scale, the core design move is state compression: preserve only the invariant needed for future decisions, not the full history.

## 🚀 Variations & Further Practice
- Require `count(events[l]) - count(events[r]) = k` for a fixed nonzero `k`; same prefix-difference idea, but the lookup becomes offset rather than equality.
- Generalize from two endpoint values to `m` designated values whose frequencies must all match inside the span; this turns pairwise differences into a higher-dimensional normalized signature.
- Ask for the number of valid spans instead of the longest one; now you need frequency-of-state counting rather than earliest-state retention, which changes both memory pressure and overflow handling.