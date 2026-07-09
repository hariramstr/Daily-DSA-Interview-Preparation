# Count Distinct Folder Paths After Renames

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String, Set

---

## 🗂 Problem Overview
Given absolute folder paths and a sequence of folder rename operations, apply each rename in order and return how many distinct paths remain at the end. A rename affects exactly one folder under one parent, so every path with that exact prefix must be rewritten. The challenge is scale: up to 100k paths and 100k renames, with heavy prefix overlap, so rebuilding strings or traversing a full tree after every operation is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere identifiers are rewritten by scoped namespace changes: object storage key migrations, monorepo package moves, compiler/module refactors, search index document rekeying, and event-stream topic renames. At small scale, repeated string rewriting is fine; at production scale, it explodes into quadratic work, cache churn, and duplicate-state bugs. Hash-based indirection lets you treat renames as logical remaps instead of physical rewrites. That enables high-throughput metadata updates, deterministic deduplication after collisions, and predictable performance under long rename chains where many entities share prefixes.

## 🔍 Problem Statement
You are given:

- `paths`: absolute folder paths such as `"/docs/team/a"`
- `renames`: operations of the form `[oldFolderPath, newFolderName]`

Each rename changes one folder name within its parent. If `oldFolderPath = "/docs/team"` and `newFolderName = "eng"`, then every stored path beginning with `"/docs/team"` becomes a path beginning with `"/docs/eng"`. Only that exact child under that exact parent is renamed; unrelated paths are unchanged.

Return the number of distinct final paths after applying all renames in order. Different original paths may converge to the same final path and must be counted once.

Constraints:

- `1 <= paths.length, renames.length <= 100000`
- Total characters across all inputs `<= 600000`
- Root `"/"` never appears as an input path
- Each rename source exists when applied

Examples:

- `paths = ["/docs/team/a", "/docs/team/b", "/docs/hr/c", "/docs/eng/b"]`
  `renames = [["/docs/team", "eng"]]` → `3`

- `paths = ["/x/a/log", "/x/b/log", "/x/a/tmp", "/y/a/log"]`
  `renames = [["/x/a", "b"], ["/x/b", "c"]]` → `4`

The key constraint is the combination of many renames and shared prefixes: it rules out reprocessing every path for every operation.

## 🪜 How to Solve This
1. Read the rename semantics carefully → this is not arbitrary string replacement. A rename only changes one folder under one parent, which means the problem is really about rewriting path prefixes.

2. If you update every matching path on every rename, worst-case work becomes `O(paths × renames)`, which is dead on arrival at `100k × 100k`.

3. That suggests indirection: instead of mutating all descendants immediately, track how each folder path maps to its current name/location.

4. A path is just a sequence of folder components. If we process components from root to leaf, we can ask at each parent: “what is the current name of this child under this parent?”

5. Renames are scoped by parent, so the natural key is `(current parent identity, child name)`. That points directly to hashing.

6. Build a hash map representing renamed edges. Then, for each original path, walk its components through that map to derive its final canonical path.

7. Final counting is a set problem: hash each canonical final path and count unique results.

The mental model is “lazy namespace remapping plus final canonicalization,” not “simulate filesystem moves.”

## 🧩 Algorithm Walkthrough
1. **Parse paths into components and intern prefixes.**  
   Represent each folder path prefix as a node ID. While scanning all original paths and rename sources, assign IDs to prefixes so `"/docs"` and `"/docs/team"` are addressable entities.  
   **Why:** renames target folder nodes, not arbitrary substrings.  
   **Invariant:** every referenced folder prefix has a stable ID.

2. **Maintain a hash map for renamed children under each parent.**  
   For a rename `oldFolderPath -> newFolderName`, split `oldFolderPath` into `(parentPath, oldName)`. Resolve the current canonical parent ID, then record that under this parent, child `oldName` now maps to `newFolderName`.  
   **Pattern:** hashing with path canonicalization.  
   **Invariant:** for any canonical parent, child-name lookup returns the latest effective name.

3. **Track canonical node transitions lazily.**  
   When a folder is renamed, descendants are not rewritten individually. Instead, the renamed folder’s node becomes reachable through a different edge name under the same parent.  
   **Why correct:** all descendants inherit the new prefix automatically when later traversed from root.

4. **Canonicalize each original path after all renames.**  
   Start at root. For each component, use the current parent context and hash lookup to determine the effective child name, then advance to the corresponding child node. Build a canonical final path signature from the effective component sequence.  
   **Invariant:** after processing `i` components, the constructed prefix equals the true final prefix after all renames.

5. **Deduplicate final paths with a hash set.**  
   Insert each canonical final path string, tuple, or rolling hash into a set and return the set size.  
   **Why:** multiple originals can collapse after renames, and uniqueness is the required output.

This is the right abstraction because the expensive operation is repeated prefix rewriting; hashing converts it into constant-time edge remapping and one final pass.

## 📊 Worked Example
Use:

- `paths = ["/x/a/log", "/x/b/log", "/x/a/tmp", "/y/a/log"]`
- `renames = [["/x/a", "b"], ["/x/b", "c"]]`

| Step | Action | Effective mapping | Distinct final paths if materialized |
|---|---|---|---|
| 1 | Initial state | none | `/x/a/log`, `/x/b/log`, `/x/a/tmp`, `/y/a/log` |
| 2 | Rename `/x/a -> b` | under parent `/x`, `a → b` | `/x/b/log`, `/x/b/log`, `/x/b/tmp`, `/y/a/log` |
| 3 | Rename `/x/b -> c` | under parent `/x`, latest canonical child `b → c` | `/x/c/log`, `/x/c/log`, `/x/c/tmp`, `/y/a/log` |
| 4 | Deduplicate | final set | `/x/c/log`, `/x/c/tmp`, `/y/a/log` plus the remaining unique canonical branch result |

Trace intuition:

1. The first rename merges `/x/a/*` into `/x/b/*`.
2. The second rename moves the whole canonical `/x/b/*` branch to `/x/c/*`.
3. Because renames are applied to the current state, chained remaps must resolve through canonical parents, not original strings.
4. Final counting is set cardinality after canonicalization.

## ⏱ Complexity Analysis
### Time Complexity
`O(totalChars + totalComponents)` expected, assuming hash map and hash set operations are `O(1)` on average. Parsing inputs, recording renames, and canonicalizing each path are all linear in input size. At `10^6` scale this is routine; at `10^9`, input size and memory bandwidth dominate long before algorithmic overhead does.

### Space Complexity
`O(totalChars + totalComponents)` for interned prefixes, rename maps, and the final deduplication set. The main owner is path-prefix indexing plus canonical path signatures. You can reduce memory by storing rolling hashes instead of full canonical strings, trading simpler correctness/debuggability for collision handling complexity.

## 💡 Key Takeaways
- If operations rewrite many items by shared prefix, think indirection plus hashing before considering per-item mutation.
- When updates are scoped by `(parent, childName)`, that pair is usually the real key, not the full raw string path.
- The rename target is a single folder name, not a full path; accidentally treating it as a full replacement breaks parent scoping.
- Renames apply to the current filesystem state, so chained operations must resolve through canonical parents rather than original input paths.
- At scale, namespace changes are cheaper as metadata remaps with late materialization than as eager rewrites of every descendant key.

## 🚀 Variations & Further Practice
- Support online queries between renames, asking for the current canonical path of arbitrary inputs. The twist is maintaining fast incremental resolution instead of one final pass.
- Allow subtree moves to a different parent, not just sibling renames. The harder part is preserving canonical ancestry when both parent identity and child name change.
- Count distinct final folders and files separately with mixed rename/delete operations. The twist is combining lazy remapping with tombstones and collision-aware deduplication.