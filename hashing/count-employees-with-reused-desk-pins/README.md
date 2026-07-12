# Count Employees With Reused Desk PINs

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Table, Array, Counting

---

## 🗂 Problem Overview
Given an array `pins`, return how many employees belong to a PIN value that appears at least twice. This is not the number of duplicated PIN values; it is the total number of array positions covered by non-unique PINs. The input can contain up to `100000` entries, so pairwise comparison is too expensive. The core task is frequency counting: identify repeated PINs efficiently, then sum their counts.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to detect reuse, collisions, or non-unique identifiers at scale: API key audits, temporary credential issuance, session token validation, event deduplication, compiler symbol counting, and streaming fraud signals. Without hashing, naive duplicate detection becomes quadratic and collapses under large batches or hot partitions. A frequency map gives predictable linear behavior, which matters in ingestion pipelines, access-control audits, and observability backends. More broadly, this is the same architectural move behind cardinality tracking, skew detection, and “group by key, then aggregate” workflows in distributed systems.

## 🔍 Problem Statement
You are given an integer array `pins` where `pins[i]` is the desk PIN used by the `i`-th employee in check-in order. Return the number of employees whose PIN is not unique in the array. Formally, if a PIN value appears `k >= 2` times, all `k` of those employees contribute to the answer.

Constraints:

- `1 <= pins.length <= 100000`
- `0 <= pins[i] <= 1000000000`
- The result fits in a 32-bit integer

Examples:

- `pins = [4312, 9981, 4312, 7777, 9981]` → `4`
- `pins = [12, 34, 56, 78]` → `0`

The key constraint is input size. With `n = 100000`, checking every PIN against every other PIN is unnecessary and too slow. The problem is fundamentally about counting occurrences by value, which points directly to hashing.

## 🪜 How to Solve This
1. Read the requirement carefully → we are not counting distinct duplicated PIN values; we are counting employees attached to duplicated values.

2. That means we need frequency, not just existence. A set can tell us whether we have seen a PIN before, but it cannot tell us how many employees should be counted in the end.

3. Once frequency is the need, the natural abstraction is a hash map:
   - key = PIN value
   - value = number of times that PIN appears

4. Make one pass through `pins` and build the frequency table. This groups equal PINs together conceptually without sorting.

5. Make a second pass over the map values:
   - if a frequency is `1`, ignore it
   - if a frequency is `>= 2`, add the full frequency to the answer

6. Why this approach? Because the problem is “group by exact value, then aggregate by group size.” Hashing is the standard linear-time tool for that pattern.

7. Sorting could also group equal values, but it adds `O(n log n)` work and may mutate input or require copying. A hash map gets the job done directly in expected `O(n)` time.

## 🧩 Algorithm Walkthrough
**Pattern:** Hash-based frequency counting.

1. **Initialize a frequency map and an answer accumulator.**  
   The map stores how many times each PIN has appeared. At this point, the invariant is simple: before processing any element, the map correctly represents counts for the empty prefix.

2. **Scan the array once and update counts.**  
   For each `pin` in `pins`, increment `freq[pin]`. After processing index `i`, the invariant becomes: the map contains exact frequencies for all PINs in `pins[0..i]`. This is correct because each step updates only the count for the current PIN and leaves all previous counts intact.

3. **Iterate over the frequency map values.**  
   For each count:
   - if `count == 1`, that PIN is unique and contributes nothing
   - if `count >= 2`, add `count` to the answer  
   This is correct because the problem asks for the number of employees whose PIN is reused, not the number of repeated PIN values. Every occurrence of a repeated PIN must be counted.

4. **Return the accumulated total.**  
   By construction, every repeated PIN contributes exactly its full frequency once, and every unique PIN contributes zero. The final sum therefore matches the required number of employees with non-unique PINs.

This abstraction is the right fit because the problem is pure equality grouping. No ordering, windowing, or positional relationship matters—only how many times each value occurs.

## 📊 Worked Example
Example: `pins = [4312, 9981, 4312, 7777, 9981]`

| Step | Current PIN | Frequency Map After Step                  |
|------|-------------|-------------------------------------------|
| 1    | 4312        | `{4312: 1}`                               |
| 2    | 9981        | `{4312: 1, 9981: 1}`                      |
| 3    | 4312        | `{4312: 2, 9981: 1}`                      |
| 4    | 7777        | `{4312: 2, 9981: 1, 7777: 1}`             |
| 5    | 9981        | `{4312: 2, 9981: 2, 7777: 1}`             |

Now evaluate frequencies:

- `4312 -> 2`, reused, add `2`
- `9981 -> 2`, reused, add `2`
- `7777 -> 1`, unique, add `0`

Final answer: `2 + 2 = 4`

The important detail is that repeated groups contribute their full size, not just one extra occurrence.

## ⏱ Complexity Analysis

### Time Complexity
Expected **O(n)**, where `n` is `pins.length`. The dominant work is one pass to build the hash table and one pass over at most `n` distinct keys. At `10^6` elements this remains practical in memory-resident workloads; at `10^9`, the algorithm is still linear but system design shifts toward partitioning or streaming due to memory pressure.

### Space Complexity
**O(u)**, where `u` is the number of distinct PINs. The hash map owns the extra space. In the worst case, `u = n`. You can reduce auxiliary space by sorting in place, but that trades memory for `O(n log n)` time and potentially mutates input.

## 💡 Key Takeaways
- If the problem says “count items whose value appears more than once,” that is a frequency-table signal, not a pairwise-comparison problem.
- When equality grouping is the only relationship that matters, reach for a hash map before considering sorting or nested scans.
- Do not return the number of duplicated PIN values; return the sum of frequencies for PINs with count `>= 2`.
- Be careful with one-pass “seen before” logic: it can count only later duplicates unless you explicitly account for the first occurrence too.
- In production systems, this is the same core move as key-based aggregation: group by identifier once, then compute policy from per-key counts.

## 🚀 Variations & Further Practice
- Return the **list of reused PIN values** instead of the employee count; same frequency map, but the output shape changes from scalar aggregation to filtered keys.
- Count employees whose PIN appears **at least `k` times**; the twist is parameterizing the threshold and ensuring the full group count is added only when frequency meets it.
- Process PINs as a **stream with bounded memory**; the harder part is exact vs approximate counting, which pushes the design toward external aggregation, sketches, or partitioned state.