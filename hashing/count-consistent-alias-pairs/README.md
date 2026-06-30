# Count Consistent Alias Pairs

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Bitmask, Counting

---

## 🗂 Problem Overview
Given an array of lowercase alias strings, count how many index pairs `(i, j)` with `i < j` have exactly the same set of distinct characters. Character order and frequency do not matter; only membership in the set matters. The challenge is scale: with up to `100000` aliases, comparing every pair is too expensive. The key is to map each alias to a canonical representation of its character set, then count equal representations efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to collapse high-cardinality text inputs into equivalence classes. Examples include compiler symbol normalization, search-query feature extraction, spam or abuse detection pipelines, streaming deduplication, and distributed rate-limiters keyed by capability sets or permission fingerprints. Without canonical hashing, teams fall back to pairwise comparison or repeated set construction, which does not survive production traffic. A compact signature enables linear scans, cheap aggregation, shard-friendly counting, and predictable memory behavior. The broader lesson is to convert semantic equivalence into a stable key early, then let standard counting infrastructure do the rest.

## 🔍 Problem Statement
You are given an array `aliases` where:

- `1 <= aliases.length <= 100000`
- `1 <= aliases[i].length <= 100`
- each `aliases[i]` contains only lowercase English letters

Two aliases are consistent if they contain exactly the same distinct characters. Repetition does not matter, and order does not matter.

Return the number of pairs `(i, j)` such that:

- `0 <= i < j < aliases.length`
- `aliases[i]` and `aliases[j]` are consistent

Examples:

- `["abbca", "cba", "aaaa", "a", "bac", "xy", "yx"] -> 5`
- `["abc", "de", "eed", "fff", "fed", "cab", "xyz"] -> 2`

The decisive constraint is `aliases.length = 100000`: an `O(n^2)` pairwise comparison is infeasible. The solution must reduce each string to a canonical key and count matches in near-linear time.

## 🪜 How to Solve This
1. Read the equivalence rule carefully → two strings match if their **distinct-character sets** match. Counts are irrelevant.

2. That means each alias should be reduced to a canonical signature representing membership of `'a'` through `'z'`.

3. Since the alphabet is fixed to 26 lowercase letters, the natural signature is a **26-bit bitmask**:
   - bit `0` means `'a'` appears
   - bit `1` means `'b'` appears
   - and so on

4. Once every alias becomes a mask, the problem is no longer about strings. It becomes: **how many pairs of equal integers exist?**

5. Equal-key pair counting is a standard hashing pattern:
   - scan left to right
   - for the current mask, every previous occurrence forms one new valid pair
   - add that count to the answer
   - then increment the stored frequency for this mask

6. This avoids nested comparisons entirely. We do one pass over aliases, one small pass over each string’s characters, and constant-time hash updates.

The key mental move is recognizing that the problem is really **canonicalization + frequency counting**, not string comparison.

## 🧩 Algorithm Walkthrough
1. **Choose the abstraction: hashing with bitmask canonicalization.**  
   The right pattern is **Hash Map + Bitmask Encoding**. Bitmasking works because the character universe is fixed and small: 26 lowercase letters. A mask uniquely identifies the set of distinct characters in an alias.

2. **Convert each alias into its mask.**  
   Initialize `mask = 0`. For each character `c` in the alias, compute `bit = c - 'a'` and set `mask |= (1 << bit)`.  
   This is correct because repeated characters set the same bit again and do not change the mask, exactly matching the problem’s “distinct characters only” rule.

3. **Use a frequency map from mask to count of prior aliases.**  
   Maintain `freq[mask] = number of aliases already seen with this exact character set`.  
   Invariant: before processing alias `i`, `freq` contains counts only for indices `< i`.

4. **Count new pairs incrementally.**  
   For the current alias mask, add `freq[mask]` to the answer. Every prior alias with the same mask forms one valid pair `(previousIndex, i)`.  
   This is correct because consistency is equivalent to mask equality.

5. **Update the map after counting.**  
   Increment `freq[mask]` by 1.  
   Invariant after update: `freq` now reflects all aliases up to and including the current one.

6. **Return the accumulated answer.**  
   This one-pass counting avoids double-counting because each pair is created exactly once, when the later index is processed.

An equivalent alternative is to count all masks first and sum `count * (count - 1) / 2`, but the streaming version is often cleaner and more useful in production pipelines.

## 📊 Worked Example
Example: `aliases = ["abbca", "cba", "aaaa", "a", "bac", "xy", "yx"]`

| Step | Alias   | Mask / Set   | `freq` before | Pairs added | Total |
|------|---------|--------------|---------------|-------------|-------|
| 1    | abbca   | `{a,b,c}`    | 0             | 0           | 0     |
| 2    | cba     | `{a,b,c}`    | 1             | 1           | 1     |
| 3    | aaaa    | `{a}`        | 0             | 0           | 1     |
| 4    | a       | `{a}`        | 1             | 1           | 2     |
| 5    | bac     | `{a,b,c}`    | 2             | 2           | 4     |
| 6    | xy      | `{x,y}`      | 0             | 0           | 4     |
| 7    | yx      | `{x,y}`      | 1             | 1           | 5     |

Final answer: `5`.

The important state is not the original strings but the frequency of each canonical mask seen so far.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * k)`, where `n` is the number of aliases and `k` is the average alias length. The dominant work is scanning characters to build each 26-bit mask, plus `O(1)` average hash-map operations. At `10^6` elements this remains practical; at `10^9`, even linear scans become infrastructure problems rather than algorithm problems.

### Space Complexity
`O(m)`, where `m` is the number of distinct masks observed. The frequency map owns the space. Since there are at most `2^26` possible masks, the theoretical bound is capped, though real input usually uses far fewer. You can trade memory for a fixed-size array if the environment tolerates it.

## 💡 Key Takeaways
- If the problem says “same characters regardless of order or repetition,” look for a canonical set representation rather than direct string comparison.
- A fixed small alphabet is a strong signal that bitmasking may be better than sorting or explicit set objects.
- Do not count after incrementing the frequency map, or you will incorrectly count self-pairs.
- Use a 64-bit accumulator for the answer; with `100000` aliases, pair counts can exceed 32-bit integer limits.
- The transferable design insight is to reduce semantic equivalence to a stable compact key early, then leverage standard aggregation primitives at scale.

## 🚀 Variations & Further Practice
- Count pairs where aliases differ by exactly one distinct character. Twist: exact equality becomes near-neighbor matching across masks, so you enumerate single-bit flips.
- Group strings by anagram class instead of distinct-character set. Twist: frequency matters now, so a presence bitmask is insufficient; you need a richer canonical signature.
- Support full Unicode instead of lowercase English letters. Twist: the fixed-width bitmask disappears, forcing different canonicalization and memory trade-offs.