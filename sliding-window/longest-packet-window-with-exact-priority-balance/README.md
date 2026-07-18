# Longest Packet Window With Exact Priority Balance

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given a packet priority stream `priorities` and an exact frequency profile `target` over priorities `1..m`, find the longest contiguous window whose counts match that profile exactly. Any value `> m` is corrupted and invalidates the entire window. The challenge is that this is not a “frequency at most” problem: the window must contain every required count exactly, no extras, and corrupted packets split the stream into independent candidate regions.

## 🌍 Engineering Impact
This pattern shows up in streaming observability, intrusion detection, and event-pipeline validation where a segment must match an exact signature, not just satisfy loose thresholds. Think packet classifiers, fraud-rule engines, log correlation, or search-query feature windows. At scale, brute-force subarray enumeration collapses under high-cardinality streams and long retention buffers. A linear sliding-window design lets you process millions of events with bounded memory, isolate corruption boundaries cheaply, and preserve deterministic latency. The broader value is architectural: exact-profile matching often requires maintaining a deficit/excess invariant rather than recomputing full histograms per candidate window.

## 🔍 Problem Statement
You are given:

- `priorities`, an integer array of length up to `200000`
- `target`, an integer array of length `m` where `1 <= m <= 100000`
- valid priorities are exactly `1..m`
- any `priorities[i] > m` is corrupted and cannot appear in a valid window

A window is balanced iff for every priority `p in [1, m]`, its count in the window equals `target[p - 1]` exactly. Return the length of the longest balanced contiguous window, or `0` if none exists.

Important edge cases:

- `target[i]` may be `0`, so some valid priorities must be absent
- the sum of `target` may be `0`
- corrupted values split the array into valid regions
- exact matching means even one extra valid-priority packet makes the window invalid

Examples:

- `priorities = [1,2,1,3,2,1,2]`, `target = [2,2,1]` → `5`
- `priorities = [4,1,2,1,3,2,5,1,2,3]`, `target = [1,1,1]` → `3`

The input size rules out quadratic scanning.

## 🪜 How to Solve This
1. Read the requirement carefully → the target profile is exact, so any valid window must have length equal to `sum(target)`. That immediately kills the idea of “expand and shrink until valid” over arbitrary lengths.

2. Notice corrupted values `> m` → they act as hard barriers. No valid window can cross them, so the stream naturally decomposes into independent valid-priority segments.

3. Inside a clean segment, we only need to test windows of one fixed length `L = sum(target)` → that strongly suggests a fixed-size sliding window rather than a variable-size one.

4. Exact frequency matching across `m` priorities sounds expensive if we compare full histograms for every shift → instead maintain incremental counts and a single mismatch metric.

5. Track, for each priority, whether the current window count equals the target count. When one element enters and one leaves, only two priorities can change status. That gives O(1) update per shift.

6. Scan each clean segment once, update counts incrementally, and record any window whose mismatch count is zero. Since every valid window has the same length, the “longest” answer is either `L` or `0`, but the scan still proves existence correctly.

## 🧩 Algorithm Walkthrough
1. **Precompute the required window length.**  
   Let `L = sum(target)`. Any balanced window must contain exactly that many packets because the target counts are exact. If `L == 0`, the only balanced window is the empty one; under standard non-empty subarray interpretation, return `0`.

2. **Use the Sliding Window pattern with fixed size.**  
   This is not a generic Two Pointers problem where both ends move freely. The exact-count constraint fixes the window size, so the right abstraction is a **fixed-size sliding window with frequency deltas**.

3. **Treat corrupted packets as reset points.**  
   While scanning `priorities`, if `priorities[r] > m`, clear the current window state and restart after `r`. This is correct because no valid window can include that element.

4. **Maintain counts only for the current clean region.**  
   Keep `window[p]` for priorities `1..m`. Also maintain `mismatchCount`: how many priorities currently have `window[p] != target[p]`. Initialize it from `target`.

5. **Update mismatch status incrementally.**  
   When adding or removing a priority `x`, check whether `window[x] == target[x]` before the update and after the update. If equality status flips, adjust `mismatchCount`. This preserves the invariant that `mismatchCount == 0` iff the current window matches the target profile exactly.

6. **Enforce size `L`.**  
   Expand right. If the clean-window length exceeds `L`, remove from the left until length is `L`. Once length is exactly `L`, test `mismatchCount == 0`. If true, return `L` immediately or record it as the answer.

7. **Why this is optimal.**  
   Every element enters and leaves the window at most once per clean segment. No histogram is recomputed from scratch. The algorithm is linear in `n` with O(m) auxiliary storage.

## 📊 Worked Example
Use `priorities = [4,1,2,1,3,2,5,1,2,3]`, `target = [1,1,1]`.

Here `m = 3`, so valid priorities are `1,2,3`, and `L = 3`.

| Step | r | value | Action | Window | Counts (1,2,3) | Valid? |
|---|---:|---:|---|---|---|---|
| 1 | 0 | 4 | corrupted → reset | `[]` | `(0,0,0)` | no |
| 2 | 1 | 1 | add | `[1]` | `(1,0,0)` | no |
| 3 | 2 | 2 | add | `[1,2]` | `(1,1,0)` | no |
| 4 | 3 | 1 | add, size=3 | `[1,2,1]` | `(2,1,0)` | no |
| 5 | 4 | 3 | add, shrink left | `[2,1,3]` | `(1,1,1)` | yes |
| 6 | 5 | 2 | add, shrink left | `[1,3,2]` | `(1,1,1)` | yes |
| 7 | 6 | 5 | corrupted → reset | `[]` | `(0,0,0)` | no |
| 8 | 7..9 | 1,2,3 | build size=3 | `[1,2,3]` | `(1,1,1)` | yes |

A balanced window exists, so the answer is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`. Computing `L` costs `O(m)`, and the stream scan is `O(n)` because each packet is added once and removed once from the fixed-size window. At `10^6` elements this is routine; at `10^9`, the asymptotics are still right, but throughput, cache behavior, and streaming I/O dominate.

### Space Complexity
`O(m)` for the target and current-window frequency arrays or maps. This is the irreducible state needed to represent exact counts across all valid priorities. You can sparsify storage when many target entries are zero, trading simpler constant-time array access for hash-map overhead.

## 💡 Key Takeaways
- If a subarray must match a full frequency profile exactly, first check whether that fixes the window length; that often converts a hard variable-window problem into a fixed-size sliding window.
- Corrupted or forbidden values are a strong signal to split the scan into independent regions rather than trying to carry state across invalid boundaries.
- The main bug source is forgetting that priorities with `target[i] = 0` still matter: their presence inside the window makes it invalid.
- Another common trap is updating counts without updating the equality/mismatch status before and after the change, which silently breaks correctness.
- In production stream processing, maintaining a compact invariant like “number of mismatched keys” is far cheaper and more predictable than revalidating full state on every shift.

## 🚀 Variations & Further Practice
- Allow windows whose counts are **at least** the target instead of exactly equal. The fixed-length shortcut disappears, and the problem becomes a true variable-size sliding window with surplus management.
- Allow up to `k` corrupted packets inside a window. Now invalid elements are no longer hard reset points; you need a mixed constraint over exact frequencies plus bounded noise.
- Ask for the **number** of balanced windows rather than existence/maximum length. Same core invariant, but correctness now depends on counting every matching shift across all clean segments.