# Find Employees with Identical Project Portfolios

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, Sorting, Array, String Hashing

---

## 🗂 Problem Overview

Given a flat list of `[employeeId, projectId]` pairs, group employees whose project sets are identical. The input is denormalized — one row per assignment — so the first challenge is aggregation before comparison. The key non-triviality: set equality across variable-length collections requires a canonical representation that is both collision-resistant and cheap to compute, at up to 10⁵ assignments across 10⁴ distinct IDs.

---

## 🌍 Engineering Impact

This pattern is pervasive wherever you need to deduplicate or cluster entities by a multi-valued attribute. Feature flag systems group users by identical flag sets to batch-evaluate rollouts. Recommendation engines cluster users with identical interaction histories to seed collaborative filtering. Schema-registry systems detect duplicate Avro/Protobuf schemas by fingerprinting field sets. Without canonical key construction, you fall back to O(n²) pairwise comparisons — which collapses under production cardinality. The sorted-key-as-hash-map-key pattern is the primitive that makes these systems scale linearly.

---

## 🔍 Problem Statement

**Input:** `assignments: int[][]` where `assignments[i] = [employeeId, projectId]`. Each pair is unique; every employee appears at least once.

**Output:** A list of groups, each containing employee IDs that share an identical project set. Omit singleton groups. Order within groups and between groups is unconstrained.

**Constraints:** `1 ≤ assignments.length ≤ 10⁵`, `1 ≤ employeeId, projectId ≤ 10⁴`.

**Examples:**

```
Input:  [[1,3],[1,5],[2,3],[2,5],[3,1],[3,3],[4,1],[4,3]]
Output: [[1,2],[3,4]]

Input:  [[1,2],[1,4],[2,3],[3,2],[3,4]]
Output: [[1,3]]
```

The critical constraint: project sets are unordered and variable-length, so direct equality comparison is undefined without normalization. That drives the entire algorithmic choice.

---

## 🪜 How to Solve This

1. **Read the problem → notice the input is denormalized.** One row per assignment means you can't compare employees directly — you must first reconstruct each employee's project set.

2. **Reconstruct sets → group assignments by `employeeId` into a `Map<employeeId, List<projectId>>`**. One linear pass is enough.

3. **Need to compare sets for equality → sets need a canonical form.** Sorting each project list and joining into a string (e.g., `"1|3|5"`) produces a deterministic key regardless of insertion order.

4. **Grouping by canonical key → HashMap again.** Map `canonicalKey → List<employeeId>`. Employees that hash to the same key have identical portfolios.

5. **Filter and return.** Emit only groups where the list length ≥ 2.

The "of course" insight: set equality becomes string equality once you sort. Sorting is the normalization step that makes an unordered collection hashable with standard primitives — no custom hash function required.

---

## 🧩 Algorithm Walkthrough

**Pattern: Canonical-Key Grouping via HashMap** — the right abstraction whenever you need equivalence classes over unordered collections.

**Step 1 — Aggregate assignments.**
Iterate `assignments` once. For each `[eId, pId]`, append `pId` to `employeeProjects[eId]`. This reconstructs each employee's raw project list. *Invariant: after this pass, every employee ID maps to its complete, unsorted project list.*

**Step 2 — Canonicalize each employee's project set.**
For each employee, sort their project list and serialize it to a delimited string (e.g., `[1,3]` → `"1,3"`). Sorting is O(k log k) per employee where k is their project count. Across all employees, total work is O(P log P) where P = `assignments.length`, since each project ID is sorted exactly once. *Invariant: two employees have the same canonical key if and only if their project sets are identical.*

**Step 3 — Group by canonical key.**
Insert each `(canonicalKey, employeeId)` into a second HashMap. This is a single O(n) pass over the employee map.

**Step 4 — Filter and collect.**
Iterate the grouping map; emit any value list with size ≥ 2.

No nested loops. No pairwise comparison. The HashMap does the equivalence-class work implicitly.

---

## 📊 Worked Example

Input: `[[1,3],[1,5],[2,3],[2,5],[3,1],[3,3],[4,1],[4,3]]`

**Step 1 — Aggregate:**

| Employee | Raw Projects |
|----------|-------------|
| 1        | [3, 5]      |
| 2        | [3, 5]      |
| 3        | [1, 3]      |
| 4        | [1, 3]      |

**Step 2 — Canonicalize (sort + join):**

| Employee | Canonical Key |
|----------|--------------|
| 1        | `"3,5"`      |
| 2        | `"3,5"`      |
| 3        | `"1,3"`      |
| 4        | `"1,3"`      |

**Step 3 — Group by key:**

| Key     | Employees |
|---------|-----------|
| `"3,5"` | [1, 2]    |
| `"1,3"` | [3, 4]    |

**Step 4 — Filter (size ≥ 2) → Output:** `[[1,2],[3,4]]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(P log P)** where P = `assignments.length`. The dominant cost is sorting each employee's project list; summed across all employees, total comparisons equal P log P (each assignment participates in exactly one sort). HashMap operations are O(P) amortized. At P = 10⁶ this is ~20M operations — comfortably sub-second. At 10⁹ you'd need partitioned/distributed aggregation.

### Space Complexity

**O(P)** — the employee-to-projects map and the canonical-key grouping map together store every assignment exactly twice. No reduction is possible without sacrificing random access; a streaming approach could reduce peak memory at the cost of requiring pre-sorted input.

---

## 💡 Key Takeaways

- **Pattern signal — "group by set equality":** Whenever the grouping predicate is set equivalence over variable-length collections, reach for sort-then-hash. The sorted serialization is the fingerprint.
- **Pattern signal — denormalized input:** A flat list of `[entity, attribute]` pairs almost always requires an aggregation pass before any comparison logic. Skipping it leads to O(n²) pairwise checks.
- **Gotcha — delimiter collisions:** Joining `[1, 23]` and `[12, 3]` both produce `"123"` without a separator. Always use a delimiter that cannot appear in the values (e.g., `","` when IDs are integers).
- **Gotcha — assuming input is pre-grouped:** The input is not sorted by `employeeId`. Iterating and comparing adjacent rows will silently produce wrong results; the aggregation map is mandatory.
- **Architectural insight:** Canonical-key construction is the same primitive used in distributed shuffle/group-by (MapReduce, Spark's `groupByKey`). The sort step is the "map" phase; the HashMap grouping is the "reduce" phase — recognizing this helps you reason about partitioning and skew at scale.

---

## 🚀 Variations & Further Practice

- **Fuzzy portfolio matching:** Instead of exact set equality, find employees whose project sets overlap by ≥ k projects (Jaccard similarity threshold). The canonical-key approach breaks down; you need MinHash LSH or inverted-index intersection — a meaningful jump in complexity.
- **Streaming assignments with deletions:** Employees gain and lose projects over time; groups must update incrementally. The static sort-and-hash no longer works. The twist is maintaining a live canonical key per employee using an order-statistic tree or a rolling hash that supports insertion and deletion in O(log k).
- **Weighted portfolios:** Each `[employeeId, projectId, hoursAllocated]` triple means two employees match only if they share the same projects *and* the same allocation. Extend the canonical key to include sorted `(projectId, hours)` tuples — straightforward, but forces you to think carefully about floating-point serialization and epsilon equality.