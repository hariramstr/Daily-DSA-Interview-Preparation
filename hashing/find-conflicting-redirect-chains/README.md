# Find Conflicting Redirect Chains

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Graph, DFS

---

## 🗂 Problem Overview
You are given redirect edges `[fromUrl, toUrl]` where each `fromUrl` has at most one outgoing edge. For every valid starting URL — only nodes appearing as `fromUrl` — follow redirects until reaching either a URL with no outgoing redirect or a cycle. URLs conflict when they resolve to the same terminal result, where cycles are represented by their lexicographically smallest member. Return the number of unordered conflicting start pairs. The challenge is doing this at scale without comparing every pair or recomputing chains repeatedly.

## 🌍 Engineering Impact
This pattern shows up anywhere a system resolves pointer-like references through a mostly functional graph: URL canonicalization, identity/account merges, package dependency aliasing, compiler symbol indirection, workflow forwarding rules, and service routing tables. At production scale, naive repeated traversal creates hot paths with quadratic behavior, duplicate work, and inconsistent cycle handling. A memoized graph-resolution approach gives deterministic canonical endpoints, efficient grouping, and a clean abstraction for downstream analytics. It enables bulk conflict detection, deduplication, and policy enforcement over hundreds of thousands of mappings without turning resolution into an operational bottleneck.

## 🔍 Problem Statement
Given `redirects`, an array of unique `[fromUrl, toUrl]` pairs, treat each `fromUrl` as a valid starting URL. Repeatedly follow redirects until either:

1. the current URL has no outgoing redirect, or
2. the traversal revisits a URL already seen in the current path, forming a cycle.

Each starting URL resolves to a terminal result:
- a sink URL with no outgoing redirect, or
- the canonical cycle representative: the lexicographically smallest URL in that cycle.

Count how many unordered pairs of distinct starting URLs resolve to the same terminal result.

Constraints:
- `1 <= redirects.length <= 2 * 10^5`
- unique `fromUrl`
- total URL text length `<= 4 * 10^5`

Example 1:
`[["/a","/b"],["/b","/final"],["/c","/final"],["/d","/e"],["/e","/final"]]`  
All five starting URLs resolve to `/final`, so the answer is `C(5,2) = 10`.

Example 2:
`[["/p","/q"],["/q","/p"],["/x","/q"],["/m","/n"],["/n","/end"],["/z","/end"]]`  
`/p,/q,/x` resolve to cycle rep `/p`; `/m,/n,/z` resolve to `/end`; answer `= 3 + 3 = 6`.

The key constraint is `2 * 10^5` edges: pairwise comparison is not viable.

## 🪜 How to Solve This
1. Read the structure carefully → every node has at most one outgoing edge. That means this is not a general graph problem; it is a **functional graph** problem.

2. What do we actually need? → not the full path, only the **terminal result** for each starting URL. Once two starts share the same resolved endpoint, they conflict.

3. That suggests a two-phase view:
   - resolve each starting URL to a canonical terminal,
   - group equal terminals and count pairs.

4. How do we resolve efficiently? → if we traverse from every start independently, long shared suffixes get recomputed many times. So cache the resolved terminal for each visited URL.

5. What about cycles? → while walking one chain, keep a map from URL to its index in the current path. If a URL repeats, the suffix from first occurrence to now is the cycle. Compute its lexicographically smallest URL once and assign that representative to every node in the cycle.

6. After resolution, use a hash map `terminal -> count`, then sum `count * (count - 1) / 2`.

That gives linear-time graph resolution plus linear-time grouping.

## 🧩 Algorithm Walkthrough
1. **Build the redirect map**  
   Store `next[fromUrl] = toUrl` in a hash map. Also record the set of valid starting URLs as exactly the map keys.  
   **Invariant:** every start node has one known outgoing edge; non-keys are sinks.

2. **Maintain global memoization**  
   Keep `resolved[url] = terminalRepresentative` for any URL whose final result is already known.  
   **Why:** in a functional graph, many starts merge into the same suffix. Memoization collapses repeated work.  
   **Pattern:** **DFS with memoization on a functional graph**.

3. **Traverse each unresolved start**  
   For a start not yet in `resolved`, walk forward iteratively. Track:
   - `path`: URLs visited in order for this traversal
   - `indexInPath[url]`: first index in `path`  
   Stop when one of three things happens:
   - current URL is already in `resolved`
   - current URL has no outgoing edge
   - current URL appears in `indexInPath` again, meaning a cycle

4. **Handle a known suffix or sink**  
   If you hit a memoized URL, inherit its resolved terminal. If you hit a URL with no outgoing redirect, that URL itself is the terminal.  
   Then assign that terminal backward to all nodes in `path` not yet assigned.  
   **Invariant:** every processed node now points to the correct final terminal.

5. **Handle a cycle**  
   If traversal re-enters the current path, extract the cycle segment from `path[cycleStart:]`. Compute the lexicographically smallest URL in that segment; this is the canonical representative. Assign it to every node in the cycle, then also to all nodes before the cycle in the current path.  
   **Why correct:** every node entering that cycle must resolve to the same canonical cycle identity.

6. **Count conflicts**  
   Iterate only over valid starting URLs, increment `freq[resolved[start]]`, then sum `freq[t] * (freq[t] - 1) / 2` across groups.  
   **Invariant:** each unordered conflicting pair is counted exactly once.

## 📊 Worked Example
Example: `["/p"->"/q", "/q"->"/p", "/x"->"/q", "/m"->"/n", "/n"->"/end", "/z"->"/end"]`

| Start | Traversal | Stop Condition | Resolved Terminal |
|---|---|---|---|
| `/p` | `/p -> /q -> /p` | cycle detected | `/p` |
| `/q` | already memoized via `/p` traversal | cached | `/p` |
| `/x` | `/x -> /q` | hits memoized `/q` | `/p` |
| `/m` | `/m -> /n -> /end` | `/end` has no outgoing edge | `/end` |
| `/n` | already memoized via `/m` traversal | cached | `/end` |
| `/z` | `/z -> /end` | `/end` has no outgoing edge | `/end` |

Cycle `{/p,/q}` gets canonical representative `/p` because it is lexicographically smaller.  
Group counts:
- `/p` → 3 starts: `/p,/q,/x`
- `/end` → 3 starts: `/m,/n,/z`

Total conflicts = `C(3,2) + C(3,2) = 3 + 3 = 6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(N + L)` where `N` is the number of redirect pairs and `L` is the total URL text length involved in hashing/comparisons. Each starting node is resolved once, each edge is traversed at most once before memoization, and each cycle node participates in one lexicographic-min scan. This remains practical at `10^6` scale; `10^9` would require distributed storage and streaming, not a different asymptotic algorithm.

### Space Complexity
`O(N + L)` for the redirect map, memoized terminal map, frequency map, and per-traversal path/index structures. The dominant owner is the hash-based storage of URLs. You could compress URLs to integer IDs to reduce overhead, trading implementation complexity for lower memory pressure and faster hashing.

## 💡 Key Takeaways
- If each node has at most one outgoing edge and the task asks where each start “eventually ends up,” think functional graph resolution, not generic graph traversal.
- If the final answer depends on “how many items share the same resolved result,” the shape is resolve first, then group with a hash map.
- Only URLs appearing as `fromUrl` are valid starting points; sink-only URLs affect resolution but must not be counted as starts.
- Cycle handling is not just detection: you must assign one canonical representative, and here that representative is the lexicographically smallest URL in the cycle.
- In production systems, canonicalizing pointer chains once and memoizing the result is the difference between stable linear throughput and repeated-path amplification under skewed traffic.

## 🚀 Variations & Further Practice
- Return the actual conflicting groups, not just the pair count; the harder part is preserving efficient grouping while emitting members in deterministic order.
- Support online updates to redirects and repeated conflict queries; the twist is that memoized terminals become invalid under mutation, pushing you toward dynamic graph maintenance.
- Generalize from out-degree `<= 1` to arbitrary directed graphs; now terminal equivalence becomes SCC condensation plus sink/cycle canonicalization rather than simple functional-graph DFS.