# Shortest Booth Segment Covering All Payment Methods

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given an array `methods` where each booth exposes one payment method, find the minimum length of any contiguous subarray that contains every distinct payment method appearing anywhere in the full array. Return `0` for an empty array. The challenge is that the target set is defined globally, but the answer must be a local contiguous segment, which rules out simpler counting or sorting-based approaches and pushes toward a linear-time sliding window.

## 🌍 Engineering Impact
This pattern shows up whenever a system must find the smallest contiguous span covering a required feature set: log windows containing all event types, observability traces covering all critical services, ad impression ranges containing all campaign categories, or streaming pipelines detecting minimal intervals with full schema diversity. At scale, brute force window enumeration collapses under quadratic growth and destroys latency budgets. The sliding-window formulation enables single-pass processing, bounded memory proportional to distinct keys, and predictable behavior under high-cardinality streams—exactly the properties needed in real-time analytics, search serving, and online monitoring systems.

## 🔍 Problem Statement
You are given a string array `methods` where `methods[i]` is the payment method accepted by the `i`-th booth in a row. Return the length of the shortest contiguous segment containing every distinct payment method present anywhere in `methods` at least once. Payment methods are case-sensitive, so `"Card"` and `"card"` are different.

Constraints:

- `0 <= methods.length <= 100000`
- `1 <= methods[i].length <= 20`
- `methods[i]` contains letters, digits, underscores, or hyphens
- Expected solution: `O(n)` time using Two Pointers / Sliding Window

Examples:

- `["cash", "card", "voucher", "cash", "qr", "card", "qr"]` → `4`
- `["applepay", "applepay", "cash"]` → `2`

Edge cases matter: an empty array returns `0`; an array with one distinct method returns `1`. The `100000` upper bound makes nested scans impractical and directly motivates a linear-time windowing strategy.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need all valid segments, only the shortest one.
2. Notice the target is “contains every distinct payment method in the whole array” → first compute that global distinct count.
3. Once the target is fixed, the problem becomes: find the smallest contiguous window whose contents cover that target set.
4. “Smallest contiguous window with coverage constraints” is a standard Sliding Window signal → expand right to gain coverage, shrink left to remove waste.
5. Use a hash map for counts inside the current window. A set alone is insufficient because duplicates matter when shrinking.
6. Track how many distinct methods are currently satisfied in the window. When that count equals the global distinct total, the window is valid.
7. Each time the window is valid, try to contract from the left as aggressively as possible while preserving validity. That greedy contraction is what guarantees minimality for each right boundary.
8. Because each pointer only moves forward, the full scan stays linear.

## 🧩 Algorithm Walkthrough
1. **Precompute the target set size.**  
   Build a set from `methods` and store `required = number of distinct methods`. If the array is empty, return `0`. This defines the exact coverage condition every valid window must satisfy.

2. **Initialize the Sliding Window state.**  
   Use two pointers: `left = 0`, and iterate `right` from `0` to `n - 1`. Maintain a hash map `freq` counting payment methods inside the current window `[left, right]`. Also maintain `formed`, the number of distinct methods whose count in `freq` is at least `1`.

3. **Expand the window with `right`.**  
   Add `methods[right]` to `freq`. If its count becomes `1`, this method is newly covered, so increment `formed`. The invariant is: `freq` exactly represents the current window, and `formed` equals the number of distinct covered methods.

4. **Shrink while the window remains valid.**  
   When `formed == required`, the current window covers all payment methods. Record its length. Then remove `methods[left]` and advance `left`. If a count drops to `0`, decrement `formed`; the window is no longer valid, so stop shrinking.

5. **Why this is correct.**  
   This is the canonical **Two Pointers / Sliding Window** pattern for minimum covering subarray. For each `right`, the inner loop computes the smallest valid window ending at `right`. Since every candidate minimal window is considered at the moment it becomes shrinkable, the global minimum is found.

## 📊 Worked Example
Example: `["cash", "card", "voucher", "cash", "qr", "card", "qr"]`  
Distinct methods = 4

| Step | right | Added     | left | formed | Valid? | Best |
|------|-------|-----------|------|--------|--------|------|
| 1    | 0     | cash      | 0    | 1      | No     | ∞    |
| 2    | 1     | card      | 0    | 2      | No     | ∞    |
| 3    | 2     | voucher   | 0    | 3      | No     | ∞    |
| 4    | 3     | cash      | 0    | 3      | No     | ∞    |
| 5    | 4     | qr        | 0    | 4      | Yes    | 5    |
| 6    | shrink| remove cash at 0 | 1 | 4 | Yes | 4 |
| 7    | shrink| remove card at 1 | 2 | 3 | No | 4 |
| 8    | 5     | card      | 2    | 4      | Yes    | 4    |
| 9    | 6     | qr        | 2    | 4      | Yes    | 4    |

A shortest valid segment is indices `[2..5]`: `["voucher", "cash", "qr", "card"]`, length `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Building the distinct set is linear, and the sliding window is also linear because each element is added once by `right` and removed at most once by `left`. At `10^6` elements this remains operationally reasonable; at `10^9`, linear work is still expensive but fundamentally the only viable asymptotic class here.

### Space Complexity
`O(k)`, where `k` is the number of distinct payment methods. The set of all methods and the window frequency map dominate memory. You cannot reduce below distinct-key storage without losing exact coverage tracking; approximate structures would trade correctness for memory.

## 💡 Key Takeaways
- If the problem asks for a **shortest contiguous segment** satisfying a coverage condition, Sliding Window should be one of the first patterns you test.
- If validity depends on **distinct categories present at least once**, pair a global distinct target with a window-local frequency map.
- Do not decrement the “covered distinct count” unless a method’s frequency drops from `1` to `0`; any earlier decrement is a correctness bug.
- Window length for inclusive bounds is `right - left + 1`; this is the most common off-by-one mistake in implementations.
- The production-grade insight is to separate **global requirement discovery** from **local incremental satisfaction**, which is a reusable design pattern in stream processing and online analytics.

## 🚀 Variations & Further Practice
- Return the actual segment indices, and if multiple windows share the same minimum length, choose the earliest one. The twist is tie-breaking without disturbing the core invariant.
- Require each distinct payment method to appear at least `m` times in the window. The harder part is that validity is no longer binary on first occurrence; threshold satisfaction must be tracked per key.
- Solve the streaming version where booths arrive continuously and queries ask for the current shortest suffix covering all methods seen so far. The twist is that the target set itself evolves over time.