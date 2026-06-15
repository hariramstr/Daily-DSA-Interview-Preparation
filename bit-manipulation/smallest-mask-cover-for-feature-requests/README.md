# Smallest Mask Cover for Feature Requests

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Bitwise OR, Greedy

---

## 🗂 Problem Overview
You are given an array of non-negative integer bitmasks, `requests`. You must choose exactly one existing mask `x` and compute how many additional bits must be turned on so that the upgraded mask covers every request in the array. Covering means every bit required by any request is present in the final mask. The non-trivial part is scale: with up to `100000` masks, pairwise comparison or per-bit reconstruction per candidate is wasteful unless you exploit the global structure.

## 🌍 Engineering Impact
This pattern shows up anywhere capabilities, permissions, feature flags, or resource requirements are encoded as bitsets. Examples include authorization engines, compiler optimization flags, search serving feature gates, stream-processing operator capabilities, and distributed schedulers matching jobs to nodes. At scale, the wrong approach degenerates into repeated subset checks across large populations, turning a simple aggregation problem into avoidable quadratic work. Recognizing that the global OR is the complete requirement set enables a single-pass summary, cheap per-candidate scoring, and a design that remains stable as request volume grows while feature dimensionality stays bounded.

## 🔍 Problem Statement
Each value in `requests` is a bitmask where bit `i` indicates whether feature `i` is required. You must choose one mask `x` from the array, then only turn on additional bits in `x` until it covers all requests. A request `r` is covered by mask `y` when `(r & y) == r`.

Return the minimum number of bits that must be turned on.

Key observation: if `OR` is the bitwise OR of all values in `requests`, then any upgraded mask must contain every bit in `OR`. Since `x` comes from the array, `x` cannot contain bits outside `OR`, so the cost for choosing `x` is exactly `popcount(OR ^ x)`.

**Constraints**
- `1 <= requests.length <= 100000`
- `0 <= requests[i] <= 10^9`

**Examples**
- `requests = [5, 1, 7]` → `OR = 7` → choose `7` → answer `0`
- `requests = [10, 12, 8]` → `OR = 14` → best choice `12` → answer `1`

The input size rules out anything that compares every request against every other request.

## 🪜 How to Solve This
1. Read the requirement carefully → the final upgraded mask must cover **every** request, so it must contain every bit that appears anywhere in the array.
2. That immediately suggests a global summary → compute the bitwise OR of all requests. This OR is the smallest possible mask that covers the full set.
3. Now reframe the problem → for each candidate `x`, you are not searching for some custom upgrade target; the only relevant target is the global OR.
4. Since bits can only be turned on, the missing work for `x` is just “which bits are in `OR` but not in `x`?”
5. That set is `OR ^ x` here, because every `x` from the array is already a subset of `OR`.
6. The cost becomes `popcount(OR ^ x)`.
7. Scan all candidates, keep the minimum popcount.

Why this approach is obvious in hindsight: the problem looks like many coverage checks, but coverage against the whole set collapses into one aggregate mask. Once you see that, the rest is just scoring each candidate by missing bits.

## 🧩 Algorithm Walkthrough
1. **Compute the global requirement mask using Bitwise OR aggregation.**  
   Initialize `target = 0`, then for each `r` in `requests`, set `target |= r`.  
   **Why correct:** any final mask covering all requests must include every bit that appears in any request. `target` is exactly that union.  
   **Invariant:** after processing the first `i` elements, `target` equals the OR of those `i` requests.

2. **Interpret `target` as the minimal universal cover.**  
   No mask smaller than `target` can cover all requests, because any missing bit would fail at least one request.  
   **Why correct:** OR is the least upper bound under bitwise subset ordering.  
   **Invariant:** every valid upgraded mask must be a superset of `target`, and `target` itself is sufficient.

3. **Score each candidate by its missing bits.**  
   For each `x` in `requests`, compute `missing = target ^ x`, then `cost = popcount(missing)`.  
   **Why correct:** because `x` originates from the array, every 1-bit in `x` must also be present in `target`; therefore XOR isolates exactly the bits present in `target` but absent in `x`.  
   **Invariant:** `cost` equals the minimum number of bit activations needed to upgrade `x` to a universal cover.

4. **Take the minimum cost across all candidates.**  
   Track `best = min(best, cost)` while scanning.  
   **Why correct:** the problem asks for the optimal choice of one existing mask.  
   **Pattern:** this is a **bitwise aggregation + greedy selection** pattern. The OR compresses global state; the greedy part is simply choosing the candidate with the fewest missing required bits.

5. **Return `best`.**  
   If some request already equals `target`, the answer is `0`, which naturally falls out of the same logic.

## 📊 Worked Example
Consider `requests = [10, 12, 8]`.

Binary forms:
- `10 = 1010`
- `12 = 1100`
- `8  = 1000`

First compute the global OR:

| Step | Request | Running OR |
|---|---:|---:|
| 1 | 1010 | 1010 |
| 2 | 1100 | 1110 |
| 3 | 1000 | 1110 |

So `target = 14 (1110)`.

Now score each candidate:

| Candidate `x` | `target ^ x` | Missing bits count |
|---|---:|---:|
| 1010 (10) | 0100 | 1 |
| 1100 (12) | 0010 | 1 |
| 1000 (8)  | 0110 | 2 |

Minimum cost is `1`.

Interpretation: the full system requires features `{1,2,3}` in bit positions. Choosing `12` already has bits `2` and `3`; only bit `1` must be enabled. No candidate can do better because none already equals `1110`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = requests.length`. One pass computes the global OR, and one pass computes the minimum popcount difference. Popcount is constant-time for fixed-width integers. At `10^6` elements this is still straightforward linear scan work; at `10^9`, the bottleneck is input throughput, not algorithmic structure.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only the running OR, current candidate score, and best answer. Space cannot be meaningfully reduced below this; any alternative would only trade clarity for no practical gain.

## 💡 Key Takeaways
- If the goal is to cover all bitmask requirements, look for a global bitwise OR before considering pairwise subset checks.
- When candidates are chosen from the same input set, each candidate is automatically a subset of the global OR, which often simplifies set-difference logic.
- Do not overcomplicate the cost formula: here `OR ^ x` works only because `x` has no bits outside `OR`; in a different source set, use `OR & ~x`.
- Be careful not to count numeric difference or Hamming distance to arbitrary masks; the only valid target is the global OR, since bits cannot be turned off.
- In production systems, bitset union is often the right compression primitive: summarize fleet-wide requirements once, then score local choices cheaply and predictably.

## 🚀 Variations & Further Practice
- Allow choosing up to `k` masks from the array before adding bits. The twist is that you now optimize `popcount(OR_all ^ (x1 | x2 | ... | xk))`, which becomes a combinatorial selection problem.
- Minimize weighted activation cost where each bit has a different enablement cost. The conceptual change is replacing plain popcount with a weighted sum over missing bits.
- Support online updates: requests are inserted or removed, and you must answer the minimum cost after each change. The harder part is maintaining the global OR and best candidate efficiently under deletions.