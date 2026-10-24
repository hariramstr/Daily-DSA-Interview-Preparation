# Find the Earliest Fully Stocked Shelf

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** arrays, prefix, simulation

---

## 🗂 Problem Overview
You process a permutation of shelf slots, where `deliveries[i]` tells which slot is filled at minute `i + 1`. After each minute, consider the largest slot number seen so far. The shelf is “fully stocked” if every slot from `1` through that maximum has already been filled. Return the earliest minute when this condition first holds. The challenge is to detect this online in one pass without rescanning previously seen slots.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must detect when a contiguous prefix of work is complete while events arrive out of order. Typical examples include streaming ingestion offsets, replicated log catch-up, compaction checkpoints, build pipelines waiting for all prior artifacts, and search indexing stages that can only publish once all lower sequence IDs are present. At scale, repeatedly checking “do we now have everything up to X?” by scanning state is wasteful and latency-heavy. The right abstraction turns an apparent set-completeness problem into a constant-space prefix-progress check, which matters for hot paths and high-throughput event processors.

## 🔍 Problem Statement
Given an array `deliveries` of length `n`, where each value is a distinct integer from `1` to `n`, minute `t` fills slot `deliveries[t - 1]`. You must return the first minute when the filled slots form a complete prefix from `1` to the maximum slot number seen so far.

Formally, after processing minute `t`, let `maxSeen` be the largest slot number delivered in the first `t` minutes. If all slots `1..maxSeen` have been filled, return `t`. If this does not happen earlier, it must happen by minute `n`.

Constraints:
- `1 <= n <= 100000`
- `deliveries.length == n`
- `1 <= deliveries[i] <= n`
- All values are distinct

Examples:
- `deliveries = [2,1,3,5,4]` → `2`
- `deliveries = [3,1,2,4]` → `3`

The key constraint is that `n` is large enough that any repeated prefix validation would be the wrong algorithmic choice.

## 🪜 How to Solve This
1. Read the condition carefully → after minute `t`, we only care about two facts: how many deliveries have happened, and the largest slot number seen so far.
2. Ask what it means for slots `1..maxSeen` to all be present. Because every slot appears exactly once, if we have processed `t` deliveries and `maxSeen == t`, then those `t` delivered slots must be exactly `{1,2,...,t}`.
3. That observation removes the need for a set, bitmap, or prefix scan. We do not need to track every missing slot.
4. So iterate once through the array, maintaining `maxSeen = max(maxSeen, deliveries[i])`.
5. At minute `i + 1`, check whether `maxSeen == i + 1`.
   - If true, the first `i + 1` deliveries form a complete prefix.
   - If false, there is still at least one gap below `maxSeen`.
6. Return the first minute where equality holds.

The core insight is that uniqueness converts a membership problem into a counting invariant.

## 🧩 Algorithm Walkthrough
1. **Initialize running state**  
   Set `maxSeen = 0`. This tracks the largest slot number delivered so far. The pattern here is a **prefix simulation with a running aggregate**: process the stream once, preserving only the state needed to answer the prefix-completeness question.

2. **Scan deliveries left to right**  
   For each index `i` from `0` to `n - 1`, update `maxSeen = max(maxSeen, deliveries[i])`.  
   Why this matters: `maxSeen` defines the right boundary of the prefix we would need to have fully filled at this moment.

3. **Check the prefix-completion invariant**  
   After processing minute `i + 1`, compare `maxSeen` with `i + 1`.  
   If `maxSeen == i + 1`, then among `i + 1` distinct delivered slots, none can be larger than `i + 1`, and all values are unique. Therefore those slots must be exactly `1..i+1`. The prefix is complete.

4. **Return the earliest valid minute**  
   The first time the equality holds is the answer, because the problem asks for the earliest such minute.

5. **Why correctness holds globally**  
   If `maxSeen > i + 1`, then at least one slot in `1..maxSeen` is missing: you have only `i + 1` distinct deliveries but need `maxSeen` distinct slots to cover that prefix. So equality is both necessary and sufficient.

6. **Fallback case**  
   If no earlier minute qualifies, return `n`. By the final minute, all slots `1..n` are filled, so the condition must hold.

## 📊 Worked Example
Take `deliveries = [2,1,3,5,4]`.

| Minute | Delivered Slot | maxSeen | Deliveries So Far | `maxSeen == minute`? | Result |
|---|---:|---:|---|---|---|
| 1 | 2 | 2 | {2} | No | gap at 1 |
| 2 | 1 | 2 | {1,2} | Yes | return 2 |
| 3 | 3 | 3 | {1,2,3} | Yes | later than earliest |
| 4 | 5 | 5 | {1,2,3,5} | No | gap at 4 |
| 5 | 4 | 5 | {1,2,3,4,5} | Yes | final prefix complete |

The decisive moment is minute 2. After two deliveries, the largest slot seen is `2`, and there have been exactly two distinct deliveries. Since values are unique and both must lie within `1..2`, they must be slots `1` and `2`. That is the earliest fully stocked prefix.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. The algorithm performs one left-to-right pass and does constant work per element: one `max` update and one equality check. At `10^6` elements this is trivial in most environments; at `10^9`, linear time is still expensive but remains the best possible for a full-input streaming check.

### Space Complexity
`O(1)`. The only extra state is the running maximum and loop index. This cannot be meaningfully reduced further. Any alternative that stores seen slots uses more memory without improving asymptotic runtime for this specific constraint set.

## 💡 Key Takeaways
- If the input is a permutation and the condition asks whether seen values form `1..k`, look for a counting invariant instead of explicit membership tracking.
- “Earliest prefix-complete moment” is a strong signal for one-pass simulation with a running aggregate, not sorting or nested scans.
- The minute is `i + 1`, not `i`; this is the most common off-by-one mistake in the check.
- Compare `maxSeen` to the number of processed elements, not to `deliveries[i]` or array length.
- In production systems, uniqueness and ordering guarantees often let you replace expensive completeness checks with a compact invariant over prefix progress.

## 🚀 Variations & Further Practice
- Allow duplicate deliveries or missing slots: now `maxSeen == count` is no longer sufficient, so you need explicit frequency/state tracking.
- Return every minute when the prefix is complete, not just the first: same invariant, but collect all matching indices instead of early exit.
- Generalize from prefix completeness to arbitrary interval completeness in a stream: this requires richer structures such as Fenwick trees, segment trees, or gap counters.