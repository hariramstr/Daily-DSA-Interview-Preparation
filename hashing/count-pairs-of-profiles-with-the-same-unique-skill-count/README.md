# Count Pairs of Profiles with the Same Unique Skill Count

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Hash Map, Set

---

## 🗂 Problem Overview
Given a list of employee profiles, where each profile is a list of skill names that may contain duplicates, compute how many unordered profile pairs share the same number of distinct skills. For each profile, first deduplicate its skills, then compare only the resulting unique-count value. The challenge is scale: profiles can be numerous, individual profiles can be large, and a quadratic pairwise comparison across profiles is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere noisy records must be normalized, reduced to a compact signature, and grouped efficiently: identity resolution pipelines, search indexing, feature-store ingestion, compiler symbol extraction, telemetry aggregation, and entitlement systems. In production, the expensive mistake is comparing raw records pairwise instead of projecting them into a stable key and counting frequencies. Without this approach, CPU and memory blow up under high-cardinality streams. With it, you get a linear pass, bounded state per equivalence class, and a design that composes cleanly with batch jobs, stream processors, and map-reduce style aggregation.

## 🔍 Problem Statement
You are given `profiles`, where `profiles[i]` is a list of lowercase skill names. A skill may appear multiple times within the same profile due to data duplication. Define the **unique skill count** of a profile as the number of distinct skill names in that profile.

Return the number of unordered pairs `(i, j)` with `i < j` such that profiles `i` and `j` have the same unique skill count.

Constraints:

- `1 <= profiles.length <= 100000`
- `1 <= profiles[i].length <= 100000`
- Sum of all profile lengths `<= 200000`
- Skill names contain lowercase English letters, length `1..20`

Examples:

- `profiles = [["java","sql","java"],["go","python"],["aws","aws","linux"],["c++"],["html","css","js"]]`
  - unique counts: `[2, 2, 2, 1, 3]`
  - answer: `3`

- `profiles = [["ml","ml","ml"],["sql"],["go","rust"],["a","b","c"],["x","y"],["k"]]`
  - unique counts: `[1, 1, 2, 3, 2, 1]`
  - answer: `4`

The key constraint is that total input size is large enough that `O(n^2)` profile comparisons are unacceptable.

## 🪜 How to Solve This
1. Read the problem → the actual skill names across different profiles do **not** need to match. Only the number of distinct skills per profile matters.

2. That means each profile can be reduced to a single integer:  
   `distinct_count = size(set(profile))`.

3. Once every profile becomes an integer, the problem changes from “compare profiles” to “count how many times each integer occurs.”

4. Counting equal values efficiently is a textbook **Hash Map frequency aggregation** problem:
   - key = unique skill count
   - value = number of profiles seen with that count

5. As you process each profile, compute its distinct count with a **Hash Set**. Then either:
   - increment the map and compute pairs later with `f * (f - 1) / 2`, or
   - add the current frequency to the answer immediately before incrementing.

6. This avoids nested comparisons entirely. You touch each skill once to build per-profile uniqueness, then touch each profile once to aggregate counts.

The core insight: normalize noisy input locally, then group globally by the normalized signature.

## 🧩 Algorithm Walkthrough
1. **Use the Hashing pattern: local deduplication + global frequency counting.**  
   For each profile, create a hash set of its skills. This removes duplicate skills within that profile and gives the correct unique skill count. This is the right abstraction because the problem defines equivalence by a derived key, not by raw array equality.

2. **Compute the profile’s signature.**  
   Let `k = number of elements in the set`. This integer is the only value that matters for pairing.  
   Invariant: after processing a profile, `k` exactly equals its distinct skill count, regardless of duplicate noise in the original list.

3. **Track how many prior profiles had the same `k`.**  
   Maintain a hash map `freq`, where `freq[k]` is the number of already-processed profiles with unique count `k`.  
   Invariant: before processing the current profile, `freq[k]` represents all valid earlier partners for this profile.

4. **Accumulate pairs incrementally.**  
   Add `freq[k]` to the answer, then increment `freq[k]`. If `freq[k]` was 3, the current profile forms 3 new unordered pairs with those earlier profiles.  
   This is correct because every valid pair is counted exactly once, when the later profile is processed.

5. **Return the total answer.**  
   No post-processing is required with the incremental method. Alternatively, you could build the full frequency map first and sum `f * (f - 1) / 2` over all counts. Both are equivalent.

This yields a linear scan over total skills plus profiles, which matches the input-size constraint.

## 📊 Worked Example
Take:

`profiles = [["java","sql","java"],["go","python"],["aws","aws","linux"],["c++"],["html","css","js"]]`

| Index | Raw Profile                  | Distinct Set            | `k` | `freq` before | Pairs added | `freq` after |
|------:|------------------------------|-------------------------|----:|--------------:|------------:|-------------:|
| 0     | `["java","sql","java"]`      | `{java, sql}`           | 2   | 0             | 0           | 1            |
| 1     | `["go","python"]`            | `{go, python}`          | 2   | 1             | 1           | 2            |
| 2     | `["aws","aws","linux"]`      | `{aws, linux}`          | 2   | 2             | 2           | 3            |
| 3     | `["c++"]`                    | `{c++}`                 | 1   | 0             | 0           | 1            |
| 4     | `["html","css","js"]`        | `{html, css, js}`       | 3   | 0             | 0           | 1            |

Running total of pairs: `0 + 1 + 2 + 0 + 0 = 3`.

The valid pairs are `(0,1)`, `(0,2)`, and `(1,2)`.

## ⏱ Complexity Analysis

### Time Complexity
`O(S)`, where `S` is the total number of skill entries across all profiles. Each skill is inserted into a per-profile hash set once, and each profile contributes one hash map update. At `10^6` elements this is routine; at `10^9`, even linear work becomes infrastructure-sensitive and likely requires distributed processing.

### Space Complexity
`O(U + C)`, where `U` is the maximum number of distinct skills in a single profile’s temporary set and `C` is the number of distinct unique-count values stored in the map. The temporary set can be reused per profile; reducing it further would trade memory for slower deduplication.

## 💡 Key Takeaways
- If the problem asks for pairs of items that are equivalent under some derived property, reduce each item to that property first, then count frequencies.
- When duplicates matter only within each record, think “local `Set`, global `Map`” rather than sorting or pairwise comparison.
- Do not compare raw profile lengths; duplicates inside a profile mean length and distinct-count are different metrics.
- If you use the combinatorial formula `f * (f - 1) / 2`, make sure the answer type can hold large values.
- The production pattern is normalize noisy records into compact signatures, then aggregate on the signature instead of carrying full payloads through the pipeline.

## 🚀 Variations & Further Practice
- Count pairs where profiles have the exact same **set** of skills, not just the same distinct-count. Twist: the hash key becomes a canonicalized set representation rather than an integer.
- Count pairs whose unique skill counts differ by at most `k`. Twist: frequency aggregation alone is insufficient; you need ordered counting or prefix-sum style range aggregation.
- Process profiles in a streaming system with sliding windows. Twist: you must support both insertion and expiration of frequency counts while preserving pair totals incrementally.