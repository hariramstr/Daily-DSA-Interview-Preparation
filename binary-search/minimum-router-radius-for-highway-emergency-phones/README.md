# Minimum Router Radius for Highway Emergency Phones

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Array

---

## 🗂 Problem Overview
Given unsorted positions of emergency phones and router installation points on a 1D highway, compute the minimum integer radius needed so every phone is within range of at least one router. The output is a single radius: the worst-case distance from any phone to its nearest router. The non-trivial part is scale: with up to `2 * 10^5` phones and routers, comparing every phone against every router is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere you need nearest-neighbor lookup over ordered coordinates: CDN edge assignment by geography, warehouse-to-customer service radius checks, cellular tower coverage validation, and time-based event matching in streaming systems. At small scale, brute force is acceptable; at operational scale, it explodes into quadratic latency and cache-hostile scans. Sorting once and answering each query with binary search turns an intractable batch job into a predictable `O((n + m) log m)` pipeline. The broader architectural lesson is standard: pay one preprocessing cost to make repeated proximity queries cheap, composable, and easy to reason about.

## 🔍 Problem Statement
You are given two integer arrays:

- `phones`, where `phones[i]` is the position of an emergency phone
- `routers`, where `routers[j]` is the position of a router

A router with radius `r` covers every phone whose absolute distance from that router is at most `r`. Return the minimum integer `r` such that every phone is covered by at least one router.

Key details:

- Arrays are not sorted
- Positions may be negative
- Duplicate phone or router positions are allowed
- `1 <= phones.length, routers.length <= 2 * 10^5`
- `-10^9 <= phones[i], routers[j] <= 10^9`
- Result fits in 32-bit signed integer

Examples:

- `phones = [2, 10, 15]`, `routers = [1, 5, 14]` → `4`
- `phones = [-8, -3, 0, 7]`, `routers = [-10, 2]` → `5`

The algorithmic driver is the input size: `O(n * m)` nearest-router checks are not viable.

## 🪜 How to Solve This
1. Read the problem → notice the answer for one phone depends only on its **closest** router, not all routers.

2. Closest element in an unsorted array is expensive to query repeatedly → sort `routers` first so positional relationships become searchable.

3. For a given phone position `p`, the nearest router must be either:
   - the first router `>= p`, or
   - the router immediately before it.

4. That observation screams **binary search on a sorted array**. Find the insertion point of `p` in `routers`.

5. Compute distance to the right candidate and left candidate if they exist. The smaller of those two is the minimum radius needed for that phone.

6. Repeat for every phone and track the maximum of these per-phone minimum distances.

7. Why maximum? Because one global radius must cover **all** phones. So the tightest valid radius is determined by the hardest phone to cover.

This is the standard “preprocess ordered reference points, then answer nearest-neighbor queries efficiently” pattern.

## 🧩 Algorithm Walkthrough
1. **Sort the router positions**  
   This is the preprocessing step. Once sorted, routers define ordered coverage boundaries on the number line. The invariant after this step: for any phone position, its nearest router must lie at or adjacent to its insertion index.

2. **For each phone, binary search the insertion point in `routers`**  
   Use lower bound: the first index `i` such that `routers[i] >= phone`. This is the canonical **Binary Search on Sorted Array** pattern. It avoids scanning and gives `O(log m)` query time per phone.

3. **Check the right-side candidate**  
   If `i < routers.length`, then `routers[i]` is the nearest router on the right. Its distance is `abs(routers[i] - phone)`.

4. **Check the left-side candidate**  
   If `i > 0`, then `routers[i - 1]` is the nearest router on the left. Its distance is `abs(phone - routers[i - 1])`.

5. **Take the smaller of the two distances**  
   This is correct because in a sorted array, any router farther left than `i - 1` or farther right than `i` is necessarily at least as far away. The invariant: after processing each phone, you know its exact nearest-router distance.

6. **Track the maximum over all phones**  
   The global radius must satisfy every phone, so the answer is the maximum of all per-phone minima. The invariant: after processing `k` phones, `answer` is the minimum radius sufficient for those `k` phones.

7. **Return the final maximum**  
   This is the smallest feasible radius for the full set. No larger radius is necessary, and no smaller radius can cover the worst-case phone.

## 📊 Worked Example
Use `phones = [-8, -3, 0, 7]`, `routers = [-10, 2]`.

Sorted routers: `[-10, 2]`

| Phone | Lower Bound Index | Left Router | Right Router | Min Distance | Running Answer |
|---|---:|---:|---:|---:|---:|
| -8 | 1 | -10 | 2 | `min(2, 10) = 2` | 2 |
| -3 | 1 | -10 | 2 | `min(7, 5) = 5` | 5 |
| 0 | 1 | -10 | 2 | `min(10, 2) = 2` | 5 |
| 7 | 2 | 2 | — | `5` | 5 |

Trace:
1. For `-8`, insertion point is before `2`; nearest is `-10`.
2. For `-3`, both sides exist; nearest is `2`.
3. For `0`, nearest is again `2`.
4. For `7`, no right router exists; only left candidate `2` matters.

Final answer: `5`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting routers costs `O(m log m)`. Then each of `n` phones performs one binary search over routers, costing `O(log m)` each, for total `O(n log m)`. Overall: `O(m log m + n log m)`. At million-scale inputs this is practical; quadratic work is not. At billion-scale, even sorting becomes a system-level concern.

### Space Complexity
If sorting is done in place, auxiliary space is `O(1)` beyond the sort implementation’s stack/runtime overhead. If the language’s sort allocates, practical space is `O(log m)` to `O(m)` depending on implementation. You can reduce copies by sorting the router array directly.

## 💡 Key Takeaways
- If the problem asks for the nearest value among many repeated queries, sort once and binary-search each query instead of rescanning.
- When the answer for each item depends only on its predecessor/successor in sorted order, think lower bound immediately.
- Be careful with boundary cases: insertion index `0` means no left router; insertion index `routers.length` means no right router.
- The answer is the **maximum of per-phone minimum distances**, not the minimum or sum; that aggregation is the core correctness condition.
- At scale, this is a classic preprocessing trade-off: invest in ordered structure construction to make repeated proximity queries predictable and cheap.

## 🚀 Variations & Further Practice
- **Routers with variable radii**: each router has its own coverage range; nearest position is no longer sufficient, and interval coverage logic becomes the real abstraction.
- **Minimize number of routers for a fixed radius**: invert the problem into greedy placement or feasibility checking with binary search on the answer.
- **2D nearest facility coverage**: phones and routers become points on a plane; binary search on a line no longer works, pushing you toward spatial indexes or sweep-line structures.