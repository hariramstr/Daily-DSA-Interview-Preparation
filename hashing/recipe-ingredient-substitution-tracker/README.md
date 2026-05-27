# Recipe Ingredient Substitution Tracker

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Graph, String Matching

---

## 🗂 Problem Overview

Given a directed substitution graph (A → B means A can be replaced by B), a pantry set, and a list of named recipes, determine which recipes are fully satisfiable. An ingredient is satisfiable if it exists in the pantry directly, or if exactly one substitution hop reaches a pantry ingredient. The depth-one constraint — no chaining through substitutions — is what makes naïve transitive-closure approaches incorrect and forces precise graph traversal bounded to a single edge.

---

## 🌍 Engineering Impact

This pattern is the core of dependency resolution under aliasing constraints — it appears in package managers (npm, Cargo) resolving peer dependency aliases, in compiler symbol tables mapping identifiers across import namespaces, and in feature-flag systems where one flag can stand in for another within a single evaluation pass. At scale, the "at most one hop" constraint mirrors real policy enforcement: a single level of indirection is auditable; unbounded chains are not. Systems that ignore this boundary — allowing transitive aliasing without a depth budget — produce resolution graphs that are exponentially harder to debug and audit.

---

## 🔍 Problem Statement

**Input:**
- `substitutions`: list of `[A, B]` pairs meaning ingredient A can be replaced by B (directed, no cycles, unique pairs)
- `pantry`: list of ingredient strings the user has available
- `recipes`: list of `[name, ingredients[]]` pairs

**Output:** Lexicographically sorted list of recipe names the user can fully prepare.

**Satisfiability rule:** An ingredient `X` is satisfiable if `X ∈ pantry` OR there exists a direct substitution `X → Y` where `Y ∈ pantry`. Chains of length > 1 are **not** allowed — `X → Y → Z` does not satisfy `X` even if `Z ∈ pantry`.

**Constraints:** Up to 500 substitution rules, 200 recipes of up to 20 ingredients each, 300 pantry items. All names are lowercase strings of length 1–20.

**Edge cases:** An ingredient with no substitution rule must be in the pantry directly. An ingredient that substitutes to something not in the pantry is unsatisfied.

---

## 🪜 How to Solve This

1. **Read the constraint carefully** → "at most one substitution per ingredient" eliminates transitive closure (BFS/DFS to arbitrary depth). This is a bounded single-hop lookup, not a reachability problem.

2. **Single-hop lookup → HashMap** → Build `sub_map: {A: B}` from the substitution list. For any ingredient, the answer is: is it in the pantry, or does `sub_map[ingredient]` exist and land in the pantry?

3. **Pantry as a HashSet** → Every satisfiability check is O(1). Without this, each ingredient check scans the pantry list — O(300) per check, multiplied across all recipe ingredients.

4. **Per-ingredient check is independent** → A recipe is makeable if and only if every ingredient passes the check. Short-circuit on the first failure.

5. **Collect passing recipe names, sort lexicographically** → Straightforward post-filter sort. The sort is not the bottleneck; correctness of the satisfiability predicate is.

The entire solution reduces to: build two lookup structures, then evaluate a predicate per ingredient per recipe. No graph traversal needed once you recognize the depth bound.

---

## 🧩 Algorithm Walkthrough

**Pattern: Hash Map + Hash Set lookup with bounded graph traversal (depth-1)**

**Step 1 — Build pantry set**
Convert `pantry` list to a `HashSet<String>`. This makes every membership check O(1) instead of O(n). Invariant: `pantry_set` never mutates after construction.

**Step 2 — Build substitution map**
Iterate `substitutions`, populate `sub_map: HashMap<String, String>` where `sub_map[A] = B`. Since pairs are unique and acyclic, no collision handling or cycle detection is needed. Invariant: each key appears at most once.

**Step 3 — Define satisfiability predicate**
For ingredient `X`:
- If `X ∈ pantry_set` → satisfied ✓
- Else if `sub_map.contains(X)` AND `sub_map[X] ∈ pantry_set` → satisfied ✓
- Otherwise → not satisfied ✗

This is the critical step. The depth-one constraint means we do not recurse or loop — we check exactly one level of indirection.

**Step 4 — Evaluate recipes**
For each recipe `(name, ingredients)`, apply the predicate to every ingredient. If all pass, add `name` to the result list. Short-circuit on first failure to avoid unnecessary checks.

**Step 5 — Sort and return**
Sort the result list lexicographically. Return.

Total structure: two hash lookups per ingredient, one linear pass per recipe. Clean, cache-friendly, and trivially parallelizable per recipe if needed.

---

## 📊 Worked Example

Using **Example 1**: `pantry = {coconut-oil, oat-milk, flour, sugar}`, substitutions: `butter→margarine`, `margarine→coconut-oil`, `milk→oat-milk`

| Recipe | Ingredient | In Pantry? | sub_map hit? | Sub in Pantry? | Satisfied? |
|--------|------------|------------|--------------|----------------|------------|
| cake | butter | ✗ | margarine | ✗ | ✗ → recipe fails |
| bread | butter | ✗ | margarine | ✗ | ✗ → recipe fails |
| cookies | butter | ✗ | margarine | ✗ | ✗ → recipe fails |

Wait — per the corrected problem semantics, `butter→margarine` (one hop), `margarine` not in pantry → butter unsatisfied. `bread` and `cake` both fail on butter. Only `milk→oat-milk` succeeds. No recipe passes all ingredients. Output: `[]`.

> **Note:** The example output in the problem description contains an internal contradiction. The algorithm above is correct per the stated "at most one substitution" rule. Always trust the constraint specification over the example explanation when they conflict.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(R × I + S + P)** where R = recipes, I = max ingredients per recipe, S = substitutions, P = pantry size. HashMap and HashSet construction is O(S) and O(P). Recipe evaluation is O(R × I) with O(1) lookups. At 10⁶ recipes × 20 ingredients, this is 2×10⁷ hash lookups — well within a single-second budget. At 10⁹ scale, partition recipes across workers; the lookup structures are read-only and trivially shareable.

### Space Complexity

**O(S + P)** for the substitution map and pantry set, both bounded by input size. The result list is O(R) in the worst case. No reduction is possible without sacrificing O(1) lookup — trading space for time here would reintroduce O(P) linear scans per ingredient check.

---

## 💡 Key Takeaways

- **Pattern signal — bounded indirection:** When a problem says "at most one substitution/alias/redirect," it's a single-hop hash lookup, not a graph traversal. Recognize this before reaching for BFS/DFS.
- **Pattern signal — membership under aliasing:** Any time you need "does X or any of its known aliases exist in set S," the structure is always `HashMap<alias, canonical>` + `HashSet<canonical>`.
- **Gotcha — chain depth off-by-one:** The problem allows depth-1 substitution, not depth-0 (exact match only) and not depth-∞ (full transitivity). Misreading this in either direction produces wrong answers; re-read the constraint before coding.
- **Gotcha — contradictory examples:** This problem's provided example output conflicts with its own constraint explanation. In interviews and production specs alike, when an example contradicts a stated rule, raise it explicitly — don't silently implement the example behavior.
- **Architectural insight:** Depth-bounded aliasing is a deliberate policy choice in production systems (one level of indirection is auditable, traceable, and reversible). Encoding the depth limit as a constant in your lookup logic — rather than a loop termination condition — makes the policy explicit, testable, and easy to change when requirements evolve.

---

## 🚀 Variations & Further Practice

- **Unbounded substitution chains (full transitivity):** Remove the one-hop constraint and allow `A → B → C → ...` chains of arbitrary length. Now you need transitive closure — either precompute reachability via BFS/DFS per ingredient, or use Union-Find if substitution becomes bidirectional. The challenge is doing this efficiently when the substitution graph is sparse but deep.
- **Weighted substitutions with preference ranking:** Each substitution carries a cost or quality score, and you want the *best* satisfying substitution (not just any). This introduces a shortest-path or priority-queue layer on top of the lookup structure, and forces you to handle the case where multiple substitution paths exist with different costs.
- **Dynamic pantry updates with recipe re-evaluation:** The pantry changes incrementally (items added/removed), and you must maintain the set of currently makeable recipes without re-evaluating all recipes from scratch. This is a classic incremental maintenance problem — the right structure is an inverted index from ingredient to recipes, with a per-recipe unsatisfied-ingredient counter that you decrement/increment on pantry mutations.