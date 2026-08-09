# Shortest Segment With Target XOR

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Prefix XOR, Hash Map

---

## 🗂 Problem Overview
Given an array of non-negative integers `nums` and an integer `target`, find the length of the shortest non-empty contiguous segment whose XOR equals `target`. Return `-1` if no such segment exists. The challenge is scale: with up to `2 * 10^5` elements, checking all subarrays is too slow. The key is to turn subarray XOR into a prefix-XOR lookup and optimize specifically for minimum segment length.

## 🌍 Engineering Impact
This pattern shows up in streaming telemetry, packet inspection, storage integrity checks, and event-processing systems where cumulative bitwise state is queried over contiguous windows. In practice, brute-force range evaluation collapses under high-throughput streams or large batch jobs because it turns linear ingestion into quadratic work. Prefix-state indexing converts repeated range computation into constant-time lookups, which is the difference between an online algorithm and an offline bottleneck. The deeper architectural lesson is familiar: when a range aggregate has an invertible prefix form, persist the right prefix summary and answer segment queries without rescanning history.

## 🔍 Problem Statement
You are given:

- `nums`, an array of length `n`
- `target`, an integer

A contiguous segment `nums[l..r]` is valid if:

```text
nums[l] ^ nums[l+1] ^ ... ^ nums[r] == target
```

Return the length of the shortest valid segment, or `-1` if none exists. A segment must contain at least one element.

Constraints:

- `1 <= nums.length <= 2 * 10^5`
- `0 <= nums[i] <= 10^9`
- `0 <= target <= 10^9`

Examples:

```text
Input: nums = [5, 1, 2, 1, 5], target = 3
Output: 2
Explanation: [1, 2] has XOR 3.
```

```text
Input: nums = [4, 7, 4, 7], target = 0
Output: 4
Explanation: The full segment XOR is 0, and no shorter contiguous segment works.
```

The decisive constraint is `n = 2 * 10^5`: any `O(n^2)` subarray enumeration is infeasible, so the solution must be near-linear.

## 🪜 How to Solve This
1. Start with the brute-force thought: every subarray has an XOR, so maybe try all `l, r`.  
   → That is immediately too slow at `O(n^2)`.

2. Ask what makes XOR range queries special.  
   → Prefix XOR behaves like prefix sum, except the inverse is also XOR:
   `xor(l..r) = prefix[r+1] ^ prefix[l]`.

3. Rewrite the condition:
   `prefix[r+1] ^ prefix[l] = target`  
   → therefore `prefix[l] = prefix[r+1] ^ target`.

4. Now the problem becomes: while scanning left to right, for the current prefix value, have we seen the matching prior prefix that would make a valid segment?

5. But this is not just existence; we need the **shortest** segment.  
   → For a fixed right endpoint, the shortest segment comes from the **largest** valid `l`, i.e. the most recent occurrence of the needed prefix value.

6. That tells us exactly what to store in the hash map: for each prefix XOR, keep its latest index.

7. Scan once, update answer when a match exists, then overwrite the current prefix index.  
   → Single pass, hash lookups, linear time.

## 🧩 Algorithm Walkthrough
1. **Use the Prefix XOR + Hash Map pattern.**  
   Define `prefix[i]` as the XOR of the first `i` elements, with `prefix[0] = 0`. Then the XOR of `nums[l..r]` is `prefix[r+1] ^ prefix[l]`. This is the right abstraction because it converts a range computation into a relation between two prefix states.

2. **Transform the target condition into a lookup.**  
   For each position `i` while scanning elements, let `curr` be `prefix[i+1]`. A segment ending at index `i` has XOR `target` iff there exists some earlier prefix value `need = curr ^ target`. If `need` appeared at prefix index `j`, then the segment length is `(i + 1) - j`.

3. **Store the latest index for each prefix XOR.**  
   Since we want the shortest segment, for a fixed `i` we want the largest possible `j` satisfying `prefix[j] = need`. Therefore the map should record the most recent index of each prefix XOR, not the earliest. Invariant: after processing position `i`, the map contains the latest prefix index for every prefix value seen so far.

4. **Initialize correctly.**  
   Seed the map with `{0: 0}` because a segment starting at index `0` and ending at `i` is valid when `prefix[i+1] == target`.

5. **Scan once and update answer.**  
   For each element:
   - update `curr ^= nums[i]`
   - compute `need = curr ^ target`
   - if `need` exists in the map, update the minimum length
   - store `curr` at prefix index `i + 1`

6. **Return the result.**  
   If no match was ever found, return `-1`; otherwise return the minimum length.

## 📊 Worked Example
Use `nums = [5, 1, 2, 1, 5]`, `target = 3`.

| i | nums[i] | curr prefix XOR | need = curr ^ 3 | latest index of need | candidate length | best |
|---|---------|------------------|-----------------|----------------------|------------------|------|
| - | -       | 0                | -               | map = `{0: 0}`       | -                | inf  |
| 0 | 5       | 5                | 6               | none                 | -                | inf  |
| 1 | 1       | 4                | 7               | none                 | -                | inf  |
| 2 | 2       | 6                | 5               | 1                    | 3 - 1 = 2        | 2    |
| 3 | 1       | 7                | 4               | 2                    | 4 - 2 = 2        | 2    |
| 4 | 5       | 2                | 1               | none                 | -                | 2    |

Interpretation:

- At `i = 2`, current prefix is `6`.
- We need prior prefix `5`, which was last seen at prefix index `1`.
- So segment `nums[1..2] = [1, 2]` has XOR `3` and length `2`.

No length-1 segment works, so the answer is `2`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)` expected time. Each element performs constant work: one XOR update, one hash lookup, and one hash write. At `10^6` elements this remains practical in memory-resident systems; at `10^9`, linear time is still the lower bound, but I/O and memory locality dominate implementation viability.

### Space Complexity
`O(n)` in the worst case, owned by the hash map of distinct prefix XOR values. This cannot generally be reduced without losing the ability to answer lookups in constant time; trading space down usually pushes the solution back toward superlinear time.

## 💡 Key Takeaways
- If a problem asks for a contiguous subarray with a target XOR, prefix XOR should be the first mental move; range XOR becomes a difference-of-prefixes lookup.
- If the requirement is shortest or closest segment, do not just detect matching prefixes—reason about which occurrence to retain.
- Seed the map with prefix XOR `0` at index `0`, or you will miss valid segments that start at the first element.
- Store prefix indices in prefix space (`i + 1`), not array-index space, or segment lengths will drift by one.
- At scale, the transferable design pattern is to cache invertible cumulative state so range queries become point lookups instead of rescans.

## 🚀 Variations & Further Practice
- **Count all subarrays with XOR equal to `target`**: same prefix-XOR identity, but the map stores frequencies instead of latest indices.
- **Longest subarray with XOR equal to `target`**: same transformation, but now you keep the earliest index for each prefix XOR rather than the latest.
- **2D submatrix XOR equals target**: compress rows or columns and apply the 1D prefix-XOR technique repeatedly; the harder twist is managing the extra dimension without exploding runtime.