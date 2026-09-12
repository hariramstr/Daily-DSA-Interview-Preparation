# Verify Unique Employee Extension Mapping

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** hashing, hash-map, data-validation

---

## 🗂 Problem Overview
Given two equal-length arrays, `names` and `extensions`, determine whether they define a valid bijection between employee names and phone extensions. The output is `true` only if every repeated name always maps to the same extension and no extension is reused by a different name. The non-trivial part is enforcing both directions of uniqueness efficiently under input sizes up to `10^5`, which rules out pairwise comparison and pushes toward constant-time lookup structures.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must validate one-to-one identity relationships under noisy or duplicated input: HR sync pipelines, IAM account linking, compiler symbol resolution, payment token ownership, device-to-user registration, and stream-processing deduplication. At scale, failing to enforce bidirectional uniqueness creates silent corruption: ambiguous ownership, non-idempotent updates, broken joins, and inconsistent downstream caches. Hash-based validation enables single-pass integrity checks during ingestion, before bad state propagates into warehouses, search indexes, or operational databases where cleanup becomes expensive and often non-deterministic.

## 🔍 Problem Statement
You are given two arrays of equal length, `names` and `extensions`, where record `i` associates `names[i]` with `extensions[i]`. Return `true` if these records describe a valid one-to-one mapping; otherwise return `false`.

A valid mapping must satisfy both conditions:

1. If the same employee name appears multiple times, it must always have the same extension.
2. If two records have the same extension, they must belong to the same employee name.

Constraints:

- `1 <= names.length == extensions.length <= 10^5`
- `1 <= names[i].length <= 50`
- `names[i]` contains English letters, spaces, and underscores
- `1 <= extensions[i] <= 10^9`

Examples:

- `names = ["Alice","Bob","Alice","Cara"]`, `extensions = [101,202,101,303]` → `true`
- `names = ["Alice","Bob","Alice"]`, `extensions = [101,101,202]` → `false`

The `10^5` bound is the key constraint: the solution should be linear, not quadratic.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not just “does each name have one extension.” It is also “does each extension belong to one name.” That means we need to validate consistency in both directions.

2. Repeated lookups with consistency checks → think hash maps. We want constant-time access to “what extension have I already seen for this name?” and “what name have I already seen for this extension?”

3. Iterate through the arrays once, record by record. For each `(name, extension)` pair:
   - If `name` was seen before with a different extension, fail immediately.
   - If `extension` was seen before with a different name, fail immediately.

4. If neither conflict exists, store both associations and continue.

5. Finish the pass without contradiction → the mapping is valid.

Why this approach is natural: the problem is fundamentally about maintaining equivalence constraints over repeated observations. Hash maps are the direct tool for incremental constraint checking. Sorting is unnecessary, and nested scans would be too slow at the given input size.

## 🧩 Algorithm Walkthrough
1. **Use the Hash Map / Bidirectional Consistency pattern.**  
   Maintain two maps:
   - `nameToExt: name -> extension`
   - `extToName: extension -> name`  
   This is the right abstraction because the problem defines a bijection, and bijections must be checked in both directions.

2. **Process records left to right.**  
   For each index `i`, read `name = names[i]` and `ext = extensions[i]`.  
   Why: each record is an independent constraint on the global mapping.

3. **Validate the forward mapping (`name -> extension`).**  
   If `nameToExt` already contains `name`, its stored extension must equal `ext`. If not, return `false`.  
   Invariant maintained: every seen name maps to exactly one extension.

4. **Validate the reverse mapping (`extension -> name`).**  
   If `extToName` already contains `ext`, its stored name must equal `name`. If not, return `false`.  
   Invariant maintained: every seen extension belongs to exactly one name.

5. **Record the association if consistent.**  
   Set `nameToExt[name] = ext` and `extToName[ext] = name`.  
   This is safe because any conflicting prior state has already been rejected.

6. **Return `true` after the loop.**  
   If the scan completes, every observed constraint is mutually consistent, so the full dataset defines a valid one-to-one mapping.

This works because every possible invalid case is exactly one of the two contradictions above: one key mapping to multiple values, or one value mapping to multiple keys.

## 📊 Worked Example
Consider:

`names = ["Alice","Bob","Alice","Cara"]`  
`extensions = [101,202,101,303]`

| Step | name   | ext | `nameToExt` after step                  | `extToName` after step                  | Valid? |
|------|--------|-----|------------------------------------------|------------------------------------------|--------|
| 1    | Alice  | 101 | `{Alice: 101}`                           | `{101: "Alice"}`                         | Yes    |
| 2    | Bob    | 202 | `{Alice: 101, Bob: 202}`                 | `{101: "Alice", 202: "Bob"}`             | Yes    |
| 3    | Alice  | 101 | unchanged; existing mapping matches      | unchanged; existing mapping matches      | Yes    |
| 4    | Cara   | 303 | `{Alice: 101, Bob: 202, Cara: 303}`      | `{101: "Alice", 202: "Bob", 303: "Cara"}` | Yes    |

At step 3, the repeated name is not a problem because it repeats the same extension. No extension is reused by a different employee, so the final answer is `true`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = names.length`. Each record is processed once, and each step performs a constant number of hash-map lookups and writes. At `10^6` elements this is still practical in memory-resident systems; at `10^9`, the algorithm remains linear but becomes constrained by memory bandwidth, heap size, and likely requires partitioned or streaming execution.

### Space Complexity
`O(n)` in the worst case, owned by the two hash maps storing distinct names and extensions. You cannot generally reduce this below linear while preserving single-pass validation; lower memory would require trade-offs such as sorting, external storage, or probabilistic checks that lose exactness.

## 💡 Key Takeaways
- If a problem says “same key must always map to same value,” that is a direct signal for a hash map consistency check.
- If it also says “different keys cannot share a value,” you are not checking a function; you are checking a bijection, so you need reverse-state tracking too.
- A common bug is validating only `name -> extension` and forgetting `extension -> name`, which incorrectly accepts shared extensions.
- Another easy mistake is overwriting an existing map entry before checking for conflict, which destroys the evidence needed to reject invalid input.
- In production pipelines, bidirectional validation is a cheap way to enforce referential integrity at ingestion time before ambiguity contaminates downstream systems.

## 🚀 Variations & Further Practice
- Validate many-to-one instead of one-to-one: multiple employees may share a department code, but each employee must still map consistently. The twist is dropping reverse uniqueness while preserving forward consistency.
- Detect and report all conflicts rather than returning a boolean. The harder part is collecting contradictory records without short-circuiting and designing useful diagnostics.
- Validate mappings across distributed shards or streaming windows. The conceptual twist is state partitioning and merge correctness when the same key may appear in different partitions.