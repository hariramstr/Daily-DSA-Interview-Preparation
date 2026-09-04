# Longest Transcript Window With Bounded Filler Ratio

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, String

---

## 🗂 Problem Overview
Given a transcript `words` and a parallel binary array `isFiller`, find the maximum-length contiguous window whose filler density is at most `p / q` and that contains at least `k` distinct non-filler words. Return that window length, or `0` if none exists.

The difficulty is that validity depends on two moving properties at once: a ratio over the whole window and a distinct-count over only a subset of elements. Brute-force subarray enumeration is far too slow at `2 * 10^5`.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, speech-processing pipelines, observability backends, and ranking systems where a segment must satisfy both quality and diversity constraints. Examples include transcript chunking for summarization, ad or content windows with bounded low-signal events, and log slices with capped noise ratios but enough unique error signatures.

At scale, naive rescans per candidate window collapse under throughput and latency budgets. A hard sliding window with incremental state turns an otherwise quadratic scan into a linear pass, which is the difference between batch-only feasibility and real-time eligibility checks in production pipelines.

## 🔍 Problem Statement
You are given:

- `words[i]`: the spoken word at time step `i`
- `isFiller[i]`: `1` if `words[i]` is filler, otherwise `0`
- integers `p`, `q`, and `k`

A contiguous window is usable if:

1. Its filler ratio is at most `p / q`
2. It contains at least `k` distinct non-filler words

Only non-filler words contribute to distinctness. Filler words still count toward window length and ratio. Compare ratios with integer arithmetic: for a window with `f` fillers and length `len`, require `f * q <= p * len`.

Constraints:
- `1 <= n <= 2 * 10^5`
- `0 <= p <= q <= 10^6`
- `1 <= k <= n`

Examples:

- `["we","should","um","ship","this","uh","week"]`, `[0,0,1,0,0,1,0]`, `p=1`, `q=3`, `k=4` → `6`
- `["uh","plan","plan","um","launch","now","like","launch","ready"]`, `[1,0,0,1,0,0,1,0,0]`, `p=1`, `q=4`, `k=3` → `5`

The constraint forcing the algorithmic choice is `n = 2 * 10^5`: any `O(n^2)` subarray strategy is dead on arrival.

## 🪜 How to Solve This
1. Read the constraints → contiguous window + longest length usually suggests sliding window, but only if validity behaves monotonically enough to maintain incrementally.

2. Look at the ratio condition → for a fixed right boundary, if the window has too many fillers, moving left rightward can only reduce or preserve filler count and always reduces length. This is exactly the kind of constraint a hard sliding window can enforce.

3. Avoid floating point → rewrite `f / len <= p / q` as `f * q <= p * len`. Now validity is integer-safe and cheap to test.

4. Track the second condition separately → we need at least `k` distinct non-filler words, so maintain a frequency map only for non-filler tokens plus a running `distinctNonFiller` count.

5. Expand right one step at a time → update filler count or word frequency.

6. While the ratio is invalid, shrink from the left until it becomes valid again.

7. Once the ratio is valid, any current window with `distinctNonFiller >= k` is a candidate answer. Record its length.

The key insight: one constraint is enforced by shrinking; the other is observed from maintained state.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers / Sliding Window pattern.**  
   Maintain a window `[left, right]` that always satisfies the filler-ratio constraint after the shrink phase. This is the right abstraction because the ratio constraint is window-local and can be repaired by moving only the left boundary forward.

2. **Track three pieces of mutable state.**  
   - `fillerCount` inside the current window  
   - `freq[word]` for non-filler words only  
   - `distinctNonFiller`, the number of non-filler words with positive frequency  
   This avoids rescanning the window to recompute either condition.

3. **Expand the window by advancing `right`.**  
   If `isFiller[right] == 1`, increment `fillerCount`. Otherwise increment `freq[words[right]]`; if it becomes `1`, increment `distinctNonFiller`.  
   Invariant before shrinking: state exactly matches `[left, right]`.

4. **Repair the ratio constraint by shrinking from the left.**  
   While `fillerCount * q > p * windowLen`, remove `left` from state and increment `left`. If the removed item is filler, decrement `fillerCount`; otherwise decrement its frequency and, if it drops to `0`, decrement `distinctNonFiller`.  
   Invariant after shrinking: `[left, right]` is the longest suffix ending at `right` that satisfies the ratio constraint.

5. **Check the distinctness condition.**  
   If `distinctNonFiller >= k`, update `answer` with the current window length. This is correct because the ratio is already guaranteed valid, and the distinct-count state is exact.

6. **Repeat for all `right`.**  
   Each index enters and leaves the window at most once, giving linear total work.

## 📊 Worked Example
Example: `words = ["we","should","um","ship","this","uh","week"]`  
`isFiller = [0,0,1,0,0,1,0]`, `p=1`, `q=3`, `k=4`

| right | word   | fillerCount | distinctNonFiller | window `[left..right]` | ratio valid? | best |
|------:|--------|-------------|-------------------|-------------------------|--------------|------|
| 0 | we     | 0 | 1 | `[0..0]` len=1 | yes | 0 |
| 1 | should | 0 | 2 | `[0..1]` len=2 | yes | 0 |
| 2 | um     | 1 | 2 | `[0..2]` len=3 | yes (`3 <= 3`) | 0 |
| 3 | ship   | 1 | 3 | `[0..3]` len=4 | yes | 0 |
| 4 | this   | 1 | 4 | `[0..4]` len=5 | yes | 5 |
| 5 | uh     | 2 | 4 | `[0..5]` len=6 | yes (`6 <= 6`) | 6 |
| 6 | week   | 2 | 5 | `[0..6]` len=7 | no (`6 > 7` false? actually `2*3=6 <= 7`, yes) | 7 |

For this input, the full window is valid, so the longest usable length is `7`. The same trace mechanics apply even when shrinking is required.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each element is added to the window once and removed once, and each update to filler count, distinct count, or hash-map frequency is `O(1)` average case. At `10^6` elements this is still operationally reasonable; at `10^9`, even linear scans become infrastructure decisions rather than algorithmic ones.

### Space Complexity
`O(u)` where `u` is the number of distinct non-filler words currently tracked, worst-case `O(n)`. The hash map owns the space. You can reduce constant factors with string interning or ID compression, but not the asymptotic bound without losing exact distinctness tracking.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous segment** under a threshold that can be restored by moving one boundary, think hard sliding window immediately.
- If one condition is a **ratio or average over the whole window**, rewrite it into integer arithmetic and test whether it is monotone enough under shrinking.
- The ratio check must use `fillerCount * q <= p * windowLen`; floating-point comparison is unnecessary and can introduce edge-case bugs.
- Distinct counting applies only to non-fillers, so filler tokens must affect length and ratio but never enter the frequency map.
- In production systems, this pattern is the core move for converting repeated segment rescans into single-pass incremental state maintenance.

## 🚀 Variations & Further Practice
- Require the window to contain **exactly** `k` distinct non-filler words instead of at least `k`; the twist is that maximizing length now interacts more sharply with duplicate eviction.
- Replace the filler ratio with a **weighted noise budget** per token; the twist is maintaining a bounded cumulative cost rather than a simple binary count.
- Ask for the **number of usable windows** instead of the longest one; the twist is turning a feasibility-maintaining window into a counting strategy without double-counting.