# Longest Transcript Window With Speaker Dominance Cap

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given an array `speakers` and an integer percentage `cap`, find the maximum length of a contiguous subarray where no speaker appears in more than `cap%` of that window. Formally, for every speaker frequency `freq[x]` in a window of length `len`, the condition is `100 * freq[x] <= cap * len`. The challenge is that checking every subarray is quadratic, which is infeasible at `2 * 10^5` elements.

## 🌍 Engineering Impact
This pattern shows up anywhere a rolling segment must satisfy a dominance or concentration bound: abuse detection in event streams, fairness checks in call-center routing, tenant isolation in multi-tenant queues, shard hot-spot detection, and content diversity constraints in ranking pipelines. At scale, brute-force rescans collapse under throughput and latency targets because every new event would trigger repeated recomputation over overlapping ranges. A sliding-window formulation turns an expensive global search into incremental state maintenance, enabling online processing, bounded memory growth, and predictable performance under sustained high-cardinality traffic.

## 🔍 Problem Statement
You are given `speakers`, where `speakers[i]` is the speaker ID for the `i`-th utterance, and an integer `cap` in `[1, 100]`. Return the length of the longest contiguous window such that for every speaker `x` inside the window, `100 * freq[x] <= cap * len`, where `len` is the window length.

Constraints:
- `1 <= speakers.length <= 2 * 10^5`
- `1 <= speakers[i] <= 10^9`
- `1 <= cap <= 100`

Important details:
- Speaker IDs are sparse, so array indexing by ID is not viable.
- A length-1 window is valid only when `cap >= 100`.
- We need the longest valid **contiguous** window.

Examples:
- `speakers = [4,1,4,2,1,2,3], cap = 50` → `7`
- `speakers = [8,8,8,2,3,8,4,5], cap = 40` → `5`

The decisive constraint is `n = 2 * 10^5`: any `O(n^2)` enumeration of windows will time out.

## 🪜 How to Solve This
1. Read the condition carefully → validity depends only on the **maximum frequency** in the current window, because if the most frequent speaker satisfies the cap, every other speaker does too.
2. That reframes the problem as: maintain a window where `100 * maxFreq <= cap * windowLen`.
3. We want the **longest contiguous** valid region → this is a classic signal for a Two Pointers / Sliding Window approach.
4. Expand the right pointer one utterance at a time, updating that speaker’s count.
5. If the window becomes invalid, shrink from the left until validity is restored.
6. To do this efficiently, we need:
   - a hash map for speaker counts, since IDs are large and sparse;
   - a way to track the current maximum frequency without rescanning the whole map each time.
7. Use a frequency-of-frequencies structure so increments and decrements can update `maxFreq` in constant amortized time.
8. Once the window is valid again, record its length and continue.

The key idea is incremental maintenance: never recompute window statistics from scratch.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window pattern with two pointers `l` and `r`.**  
   `r` expands the candidate window; `l` contracts it only when the dominance cap is violated. This is the right abstraction because we need the longest contiguous segment under a monotone validity rule.

2. **Maintain `count[speaker]`.**  
   When `speakers[r]` enters the window, increment its count. When `speakers[l]` leaves, decrement its count. This gives exact frequencies for the current window.

3. **Maintain `freqCount[f] = number of speakers currently appearing exactly f times`.**  
   On every count change from `old` to `new`, decrement `freqCount[old]` and increment `freqCount[new]`. This avoids rescanning all speakers to find the maximum count.

4. **Track `maxFreq`.**  
   When a speaker’s count increases to `new`, update `maxFreq = max(maxFreq, new)`. During left-shrinks, if `freqCount[maxFreq]` becomes zero, decrement `maxFreq` until it points to a non-empty frequency bucket.  
   Invariant: `maxFreq` is always the true maximum frequency in the current window.

5. **Check validity using one inequality.**  
   The window `[l..r]` is valid iff `100 * maxFreq <= cap * (r - l + 1)`. This is correct because every other speaker frequency is `<= maxFreq`.

6. **Shrink until valid, then update answer.**  
   Since `l` and `r` each move at most `n` times, total work is linear apart from hash-map overhead.

## 📊 Worked Example
Example: `speakers = [8,8,8,2,3,8,4,5]`, `cap = 40`

| Step | `r` | Add | Window | Counts | `maxFreq` | Valid? |
|---|---:|---:|---|---|---:|---|
| 1 | 0 | 8 | `[8]` | `{8:1}` | 1 | `100 <= 40` → no |
| 2 | 1 | 8 | `[8,8]` | `{8:2}` | 2 | `200 <= 80` → no |
| 3 | 2 | 8 | `[8,8,8]` | `{8:3}` | 3 | `300 <= 120` → no |
| shrink |  | remove 8s | until `[8]` | `{8:1}` | 1 | still no |
| 4 | 3 | 2 | `[8,2]` | `{8:1,2:1}` | 1 | `100 <= 80` → no |
| 5 | 4 | 3 | `[8,2,3]` | `{8:1,2:1,3:1}` | 1 | `100 <= 120` → yes |
| 6 | 5 | 8 | `[8,2,3,8]` | `{8:2,2:1,3:1}` | 2 | `200 <= 160` → no |
| shrink |  | remove left 8 | `[2,3,8]` | `{8:1,2:1,3:1}` | 1 | yes |
| 7 | 6 | 4 | `[2,3,8,4]` | all 1s | 1 | yes |
| 8 | 7 | 5 | `[2,3,8,4,5]` | all 1s | 1 | yes |

Best length: `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each element enters the window once and leaves once, so both pointers advance at most `n` times. Hash-map updates and frequency-bucket maintenance are constant amortized work. This is practical at `10^6` scale; anything quadratic is dead on arrival long before `10^9`.

### Space Complexity
`O(k + n)` in the straightforward implementation, where `k` is the number of distinct speakers in the current transcript and `n` bounds the frequency-bucket array/map. The dominant structures are `count` and `freqCount`. You can compress `freqCount` with a sparse map, trading lower memory for slightly higher constant factors.

## 💡 Key Takeaways
- If a window constraint says “for all categories, frequency must stay under a threshold,” check whether the condition collapses to tracking only the **maximum frequency**.
- “Longest contiguous subarray satisfying a monotone validity rule” is a strong signal for Two Pointers / Sliding Window.
- Do not use floating-point percentages; compare `100 * maxFreq` and `cap * len` as integers to avoid precision bugs.
- Be careful when shrinking: if the last speaker at `maxFreq` drops, you must lower `maxFreq` until it matches a non-empty bucket.
- The transferable design insight is to maintain just enough incremental state to answer a global window predicate without rescanning overlapping data.

## 🚀 Variations & Further Practice
- **Per-speaker caps instead of one global `cap`:** validity becomes `100 * freq[x] <= cap[x] * len`, which removes the single-`maxFreq` shortcut and forces more complex violation tracking.
- **Streaming version with online queries:** process an unbounded event stream and report the longest valid suffix or rolling maximum under memory and latency constraints.
- **Allow up to `k` cap violations inside a window:** introduces a second-order budget, requiring you to track how many speakers currently exceed the threshold rather than enforcing strict validity.