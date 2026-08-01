# Find the Earliest Duplicate Custom Alias

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String, Simulation

---

## 🗂 Problem Overview
Given aliases in creation order, return the first index whose normalized form matches any earlier alias. Normalization lowercases letters, removes `-` and `_`, and preserves every other character, including `.` and digits. If no normalized alias repeats, return `-1`. The challenge is scale: with up to 200,000 aliases, pairwise comparison is too expensive, so the solution must detect duplicates in a single left-to-right pass using hashing.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must enforce uniqueness over canonicalized identifiers: messaging channel aliases, email local-part normalization, compiler symbol tables, API gateway route registration, search indexing, and streaming dedup pipelines. At small scale, ad hoc scans work; at production scale, they collapse under quadratic behavior and inconsistent normalization logic. A hash-based canonicalization pass gives deterministic duplicate detection, predictable latency, and a clean separation between raw user input and the internal identity key. That separation matters operationally: it prevents drift between services, makes validation reproducible, and keeps ingestion paths linear in total input size.

## 🔍 Problem Statement
You are given an array `aliases` where `aliases[i]` is the `i`th alias created. Two aliases are equivalent if their normalized forms are identical.

Normalization rules:
- convert uppercase letters to lowercase
- remove every `'-'` and `'_'`
- keep all other characters unchanged

Return the smallest index `i` such that the normalized form of `aliases[i]` already appeared among `aliases[0...i-1]`. If no such index exists, return `-1`.

Constraints:
- `1 <= aliases.length <= 200000`
- `1 <= aliases[i].length <= 100`
- characters are English letters, digits, `-`, `_`, and `.`

Examples:
- `["Team-Chat", "alerts", "team_chat", "team.chat"] -> 2`
- `["build.v1", "build_v1", "BUILD-V2", "buildv2"] -> 3`

The key constraint is input size: the expected solution must run in `O(total input size)` time using hashing, which rules out nested comparisons or repeated rescans.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need all duplicates or duplicate groups. We need the **first position in creation order** whose canonical form was seen before.

2. “Equivalent after normalization” means the raw string is not the identity. The identity is the **normalized alias**. Once that is clear, the problem becomes:  
   **scan left to right and detect the first repeated key**.

3. Repeated-key detection in one pass is a standard hashing problem.  
   Use a `HashSet` of normalized aliases seen so far.

4. For each alias, compute its normalized form by:
   - lowercasing letters
   - skipping `-` and `_`
   - appending everything else

5. After normalization:
   - if the normalized string is already in the set, return the current index immediately
   - otherwise insert it and continue

6. Why this is enough: left-to-right scanning guarantees the first duplicate **by creation time** is exactly the first index where membership succeeds.

7. Why not sort? Sorting would destroy creation order, which is the thing we are asked to preserve.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: hashing with canonicalization.**  
   The problem is not about comparing raw strings; it is about comparing strings under an equivalence rule. The right abstraction is to map every alias to a canonical key, then use a hash set for seen keys.

2. **Initialize an empty hash set `seen`.**  
   Invariant: after processing index `i - 1`, `seen` contains exactly the normalized forms of `aliases[0...i-1]`.

3. **Iterate through `aliases` from left to right.**  
   This ordering is essential because the answer is the earliest duplicate by creation time, not the first original alias in a duplicate class.

4. **Normalize the current alias.**  
   Build a new string character by character:
   - if the character is `'-'` or `'_'`, skip it
   - if it is uppercase, convert to lowercase
   - otherwise append as-is  
   This is correct because it applies the equivalence definition exactly and preserves characters such as `.` and digits.

5. **Check membership in `seen`.**  
   - If present, return the current index.  
   - If absent, insert it into `seen`.  
   This maintains the invariant and guarantees correctness: the first successful lookup is the earliest index whose normalized alias occurred before.

6. **If the loop finishes, return `-1`.**  
   At that point no normalized form repeated, so no valid duplicate exists.

This is a classic **Hash Set / one-pass deduplication** pattern. It fits because we need expected `O(1)` membership checks while preserving stream order.

## 📊 Worked Example
Use `aliases = ["Team-Chat", "alerts", "team_chat", "team.chat"]`.

| i | alias        | normalized  | seen before step              | duplicate? |
|---|--------------|-------------|-------------------------------|------------|
| 0 | Team-Chat    | `teamchat`  | `{}`                          | No         |
| 1 | alerts       | `alerts`    | `{teamchat}`                  | No         |
| 2 | team_chat    | `teamchat`  | `{teamchat, alerts}`          | Yes        |

Trace:
1. Start with empty `seen`.
2. Index `0`: normalize `"Team-Chat"` → `"teamchat"`. Not present, insert it.
3. Index `1`: normalize `"alerts"` → `"alerts"`. Not present, insert it.
4. Index `2`: normalize `"team_chat"` → `"teamchat"`. This key already exists from index `0`, so return `2`.
5. We stop immediately; `"team.chat"` is never processed because the earliest duplicate has already been found.

## ⏱ Complexity Analysis
### Time Complexity
`O(total input size)` expected time. Each alias is scanned once during normalization, and each normalized string is inserted/looked up once in the hash set. For `10^6` characters total, this is comfortably linear; for `10^9`, it is still the right asymptotic shape, though memory bandwidth and allocation behavior become the practical bottlenecks.

### Space Complexity
`O(u)` normalized strings, where `u` is the number of distinct normalized aliases seen before termination, bounded by `O(total input size)`. The hash set owns the space. You can reduce allocation overhead with interning or custom hashing, but only by trading implementation complexity and portability.

## 💡 Key Takeaways
- If the problem says “equivalent after transformation” and asks for duplicate detection, first ask what the canonical key is.
- If the answer must preserve original arrival order, prefer a one-pass hash set over sorting or grouping.
- Do not remove `.` or digits during normalization; only `-` and `_` are stripped.
- Return the current index on the first repeated normalized form; do not return the earlier matching index.
- In production systems, canonicalization should be explicit and centralized; duplicate detection is only as correct as the normalization contract.

## 🚀 Variations & Further Practice
- Return all duplicate indices or group aliases by normalized form. The twist is preserving both canonical grouping and original order efficiently.
- Support online updates and deletions in a long-lived registry. The harder part is maintaining canonical uniqueness under mutable state.
- Detect collisions under richer normalization rules, such as Unicode case folding or locale-sensitive transforms. The complexity shifts from hashing to defining a stable canonical form correctly.