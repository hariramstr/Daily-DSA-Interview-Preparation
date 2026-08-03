# Longest Run of Pairwise Disjoint Feature Masks

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Sliding Window, Two Pointers

---

## 🗂 Problem Overview
Given an array of non-negative integers, find the longest contiguous subarray where no bit position is set in more than one element. Equivalently, every pair of values inside the window must satisfy `a & b == 0`. Return only the maximum window length. The challenge is enforcing this pairwise constraint efficiently under `10^5` elements; naive pairwise checking per window quickly degenerates into quadratic work.

## 🌍 Engineering Impact
This pattern shows up anywhere a stream of entities carries compact capability or resource masks and adjacent batches must remain conflict-free: scheduler slot assignment, feature-flag rollout validation, compiler register/live-range packing, search-serving request coalescing, and streaming pipelines that merge mutually exclusive attributes. At scale, brute-force overlap checks create latency spikes and force smaller batch sizes. A sliding-window bitmask approach enables linear scans, predictable memory use, and online processing, which matters when decisions must be made incrementally rather than after materializing and rechecking large candidate windows.

## 🔍 Problem Statement
You are given `masks`, where `masks[i]` is a 32-bit non-negative integer representing enabled feature bits for the `i`-th build. A contiguous run is compatible if each bit appears in at most one value in that run. Formally, for any distinct indices `a` and `b` in the run, `masks[a] & masks[b] == 0`.

Return the length of the longest compatible contiguous run.

Constraints:
- `1 <= masks.length <= 100000`
- `0 <= masks[i] <= 10^9`
- values fit in a 32-bit signed integer

Examples:
- `masks = [1, 2, 4, 3, 8]` → `3`
- `masks = [5, 1, 2, 8, 4]` → `4`

Important nuance: checking whether the bitwise AND of the whole window is zero is insufficient. Validity requires pairwise disjointness across the entire window. The input size rules out recomputing pairwise overlaps for every candidate subarray.

## 🪜 How to Solve This
1. Read the constraint carefully → the subarray must be contiguous, so this is a window problem, not a subset problem.

2. Notice what invalidates a window → adding one new mask only causes trouble if it shares at least one set bit with bits already present in the current window.

3. That suggests maintaining aggregate state for the window → a single bitmask `used` representing all bits currently occupied.

4. Why is one aggregate mask enough? Because the window invariant is stronger than “AND is zero”: if the window is valid, each bit appears at most once, so `used` is exactly the bitwise OR of the window.

5. For each new element, test conflict with `used` using `used & masks[right]`. If nonzero, the new build overlaps with something already inside.

6. Shrink from the left until the conflict disappears. Since the window was valid before insertion, removing `masks[left]` can safely clear its bits from `used`.

7. Expand, update the best length, and continue. Each element enters and leaves the window at most once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` that is always compatible. This abstraction fits because we need the longest contiguous segment satisfying a monotonic validity condition: if a window is invalid after adding `right`, moving `left` forward can restore validity.

2. **Track occupied bits with a running mask `used`.**  
   `used` stores the OR of all values currently in the window. This is correct only because we maintain the invariant that no bit appears twice. Under that invariant, each set bit in `used` belongs to exactly one element.

3. **Attempt to extend the window with `masks[right]`.**  
   Compute `used & masks[right]`. If it is zero, the new value shares no bits with the current window, so extension is safe.

4. **If there is overlap, shrink from the left.**  
   While `used & masks[right] != 0`, remove `masks[left]` from the window and advance `left`. Because bits are unique inside a valid window, removal can be done with `used ^= masks[left]` (or equivalently `used &= ~masks[left]`).

5. **Add the new value once conflict-free.**  
   Set `used |= masks[right]`. The invariant is restored: every bit in the window remains unique.

6. **Update the answer with `right - left + 1`.**  
   This is the largest compatible window ending at `right`. Taking the max over all `right` yields the global optimum.

7. **Why linear time holds.**  
   `right` advances `n` times, and `left` also advances at most `n` times total. No element is revisited after removal, so the total work is `O(n)`.

## 📊 Worked Example
Using `masks = [1, 2, 4, 3, 8]`:

| right | masks[right] | used before | conflict? | left after shrink | used after add | window | best |
|---|---:|---:|---|---:|---:|---|---:|
| 0 | 1 | 0 | no | 0 | 1 | `[1]` | 1 |
| 1 | 2 | 1 | no | 0 | 3 | `[1,2]` | 2 |
| 2 | 4 | 3 | no | 0 | 7 | `[1,2,4]` | 3 |
| 3 | 3 | 7 | yes (`7&3`) | 2 | 7 | `[4,3]` | 3 |
| 4 | 8 | 7 | no | 2 | 15 | `[4,3,8]` | 3 |

Trace for `right = 3`: `3` conflicts with `1` and `2`. Remove `1` → `used = 6`, still conflicts. Remove `2` → `used = 4`, conflict cleared. Add `3` → `used = 7`. Longest compatible run remains length `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = masks.length`. Each element is added to the window once and removed at most once; the dominant work is constant-time bitwise operations during pointer movement. At `10^6` elements this remains practical; at `10^9`, even linear scan cost becomes the system bottleneck regardless of algorithmic optimality.

### Space Complexity
`O(1)`. The algorithm stores only a few integers: two pointers, the current answer, and the aggregate bitmask. Space cannot be meaningfully reduced further. A frequency-per-bit variant also stays `O(1)` for fixed-width integers, but adds implementation complexity without improving asymptotics here.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** region under a validity rule that can be restored by dropping elements from the left, think Sliding Window / Two Pointers immediately.
- If conflicts are defined by shared bits, look for a compressed state representation using bitwise OR/AND instead of pairwise comparisons.
- The removal step works with `XOR` only because the window invariant guarantees each bit appears at most once; without that invariant, `XOR` would corrupt state.
- Do not confuse “pairwise disjoint bits” with “bitwise AND of the whole window is zero”; those are different conditions.
- In production systems, compact conflict state plus incremental repair often beats recomputation, especially for online validation over ordered streams.

## 🚀 Variations & Further Practice
- Allow each bit to appear up to `k` times in a window instead of once. The twist is that a single OR mask is no longer sufficient; you need per-bit counts.
- Find the longest **non-contiguous** subset of pairwise disjoint masks. The twist is that contiguity disappears, so sliding window no longer applies and the problem shifts toward combinatorial selection.
- Process an append-only stream with interleaved queries for current longest suffix or longest-so-far compatible run. The twist is online state management and query semantics under continuous ingestion.