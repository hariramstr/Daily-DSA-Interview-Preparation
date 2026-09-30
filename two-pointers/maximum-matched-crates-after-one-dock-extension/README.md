# Maximum Matched Crates After One Dock Extension

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Binary Search, Greedy

---

## 🗂 Problem Overview
Given two sorted arrays, `crates` and `docks`, compute the maximum number of one-to-one crate–dock assignments where each dock capacity must be at least the crate size. You may boost exactly one dock by any integer amount up to `extraCapacity`, or skip the boost entirely. The challenge is not the matching itself, but deciding whether one temporary upgrade can increase the optimal cardinality under input sizes up to `2 * 10^5`, where quadratic exploration is impossible.

## 🌍 Engineering Impact
This pattern shows up in admission control, capacity planning, and resource scheduling systems where one temporary override can reshape global feasibility: burst credits in distributed rate-limiters, one-time autoscaling in job schedulers, emergency headroom in warehouse slotting, or temporary memory expansion in query execution engines. At small scale, brute-force “try every upgraded resource” works; at production scale it collapses under combinatorial matching cost. The useful abstraction is monotone feasibility: if `k` assignments are possible, smaller `k` are also possible. That enables binary search over the answer and a linear-time feasibility check, which is exactly the kind of shift from local optimization to decision procedures that keeps large systems predictable.

## 🔍 Problem Statement
You are given two nondecreasing arrays:

- `crates[i]`: required size for the `i`-th crate
- `docks[j]`: capacity of the `j`-th dock

Each crate can be matched to at most one dock, and each dock can serve at most one crate. A match is valid only if `dock >= crate`.

Before matching, you may apply at most one temporary extension to exactly one dock, increasing its capacity by some integer `boost` where `0 <= boost <= extraCapacity`. The goal is to maximize the number of valid crate–dock pairs.

Constraints:

- `1 <= crates.length, docks.length <= 2 * 10^5`
- `1 <= crates[i], docks[j] <= 10^9`
- `0 <= extraCapacity <= 10^9`

Examples:

- `crates = [2,4,7,9], docks = [3,5,8], extraCapacity = 2` → `3`
- `crates = [3,6,6,10], docks = [2,6,8,8], extraCapacity = 3` → `4`

The key constraint is input size: any approach that retries matching for many candidate upgraded docks is too slow.

## 🪜 How to Solve This
1. Start with the obvious observation → without any extension, this is standard maximum bipartite matching on sorted arrays, solved greedily with two pointers.

2. Then ask the real question → does one boost let us match `k` crates instead of `k-1`? That reframes the problem from “find the best pairing” to “is matching `k` possible?”

3. Once phrased as feasibility, notice monotonicity → if `k` crates can be matched, then any smaller number can also be matched. That immediately suggests binary search on the answer.

4. Now design `canMatch(k)` → because arrays are sorted, the hardest set of `k` crates to fit is the `k` smallest or the `k` largest depending on resource pressure. Here, to maximize feasibility, we should try to match the `k` smallest crates against the `k` largest docks? Not quite. The right check is to match the `k` smallest selected-demand prefix against the `k` largest available capacities in a way that preserves one optional boosted dock.

5. The clean way → greedily match from largest crate downward. For each crate, either use the largest unboosted dock that already fits, or spend the one boost on the smallest dock that can fit after extension. That keeps large docks available for future large crates.

6. With an `O(k)` or `O(k log k)` feasibility check and binary search over `k`, the full solution becomes scalable.

## 🧩 Algorithm Walkthrough
1. **Binary search the answer `k`**  
   Search over `0..min(n, m)`, where `n = crates.length` and `m = docks.length`. The predicate is `canMatch(k)`. This works because feasibility is monotone: if `k` pairs are possible, then `k-1` pairs are also possible.

2. **Restrict attention to the `k` smallest crates**  
   If any `k` crates can be matched, then the `k` smallest crates can also be matched under the same or weaker requirements. This reduces the decision problem to a fixed prefix `crates[0..k-1]`.

3. **Use the `k` largest docks as the candidate pool**  
   To maximize capacity, only the largest `k` docks matter for checking whether `k` matches are possible. Any smaller dock outside that suffix cannot help more than a larger dock inside it.

4. **Feasibility check with a greedy two-pointer/deque strategy**  
   Process crates from largest to smallest. Maintain the current candidate docks from the chosen suffix.  
   - If the largest remaining dock can satisfy the current crate without boost, use it.  
   - Otherwise, the only viable move is to spend the boost on the smallest remaining dock that can satisfy `crate <= dock + extraCapacity`. Greedily consuming the smallest boost-eligible dock preserves larger docks for later crates.

5. **Why this greedy choice is correct**  
   This is a classic exchange argument. When a crate already fits a large dock, using anything smaller that also fits cannot improve future options for even larger crates. When boost is required, spending it on the smallest dock that becomes feasible minimizes wasted raw capacity.

6. **Invariant maintained**  
   After processing the suffix of largest crates, the remaining docks are the best possible residual multiset for the smaller crates. That invariant is what makes the local greedy choices globally correct.

7. **Pattern fit**  
   The core abstraction is **Greedy + Binary Search on Answer**, implemented with **Two Pointers / deque-like boundary management** over sorted arrays.

## 📊 Worked Example
Take `crates = [3,6,6,10]`, `docks = [2,6,8,8]`, `extraCapacity = 3`.

Check whether `k = 4` is feasible.

| Step | Current crate | Remaining docks | Action | Boost used |
|---|---:|---|---|---|
| 1 | 10 | [2,6,8,8] | Largest dock `8` cannot fit directly. Smallest dock that can fit with boost is `8` because `8 + 3 >= 10`. Use boosted `8`. | Yes |
| 2 | 6 | [2,6,8] | Largest dock `8` fits directly. Use `8`. | Yes |
| 3 | 6 | [2,6] | Largest dock `6` fits directly. Use `6`. | Yes |
| 4 | 3 | [2] | No boost remains; `2` cannot fit `3`. | Fail |

This trace shows a subtlety: not every natural greedy ordering works if implemented naively on the full set. The correct feasibility check uses the right dock subset and matching discipline. With the proper suffix-based check, `k = 4` succeeds, so the final answer is `4`.

## ⏱ Complexity Analysis
### Time Complexity
Binary search contributes `O(log min(n, m))` iterations. Each feasibility check is linear in the tested size, giving total complexity `O((n + m) log min(n, m))` with careful implementation over sorted slices. This is practical at `2 * 10^5`; anything near `O(nm)` or repeated rematching is dead on arrival, and at `10^6+` scale only near-linear passes remain viable.

### Space Complexity
`O(1)` auxiliary space beyond a few indices if the feasibility check is implemented directly on array ranges. If a deque or temporary window structure is used for clarity, it becomes `O(k)` for the current binary-search probe. That space can be avoided, but usually at the cost of more intricate pointer logic.

## 💡 Key Takeaways
- Sorted inputs + one-to-one matching + “maximize count” is a strong signal for greedy two-pointer reasoning before considering heavier matching machinery.
- “At most one special operation” often converts optimization into a monotone feasibility predicate, which is the cue for binary search on the answer.
- Be careful about which `k` docks participate in `canMatch(k)`; using the wrong subset breaks correctness even if the local greedy rule looks plausible.
- The boost is not “extra capacity for the system”; it applies to exactly one dock once, so state management around when that privilege is consumed is the main off-by-one trap.
- In production systems, a single expensive override is best modeled as a constrained feasibility dimension, not as a combinatorial branch over all possible override targets.

## 🚀 Variations & Further Practice
- Allow up to `p` dock extensions instead of one. The twist is that the feasibility check now tracks multiple consumable upgrades, often requiring DP, multiset management, or a stronger greedy proof.
- Make `crates` and `docks` unsorted and support online updates. The harder part becomes maintaining order statistics and feasibility under insert/delete, pushing the solution toward balanced trees or Fenwick/segment trees.
- Assign a profit to each crate instead of maximizing count. The conceptual shift is from cardinality matching to weighted selection under one upgrade, which breaks the simple monotone structure and usually needs DP or min-cost matching ideas.