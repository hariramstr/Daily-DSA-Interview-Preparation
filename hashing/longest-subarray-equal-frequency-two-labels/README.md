# Find Longest Subarray with Equal Frequency of Two Labels

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Prefix Sum, Array

---

## 🗂 Problem Overview

Given a binary-labeled array of `'A'`s and `'B'`s, find the length of the longest contiguous subarray where both labels appear with equal frequency. The input/output contract is simple: one array in, one integer out. The non-trivial constraint is that you must do this in a single pass — any brute-force enumeration of subarrays is O(n²) and fails at n = 10⁵. The key insight is recognizing this as a prefix-sum problem disguised as a counting problem.

---

## 🌍 Engineering Impact

This pattern is pervasive in production systems that track balance or parity across a stream. Feature flag audit systems use it to find the longest window where two code paths were exercised equally — critical for A/B test validity. Log processors use it to detect balanced request/response pairs in distributed traces. Load balancers apply the same prefix-delta technique to identify the longest interval where two backend pools received equal traffic. Without this O(n) approach, real-time stream processors would miss SLA windows or require expensive recomputation over sliding windows at scale.

---

## 🔍 Problem Statement

**Input:** An array `labels` of strings where every element is either `'A'` or `'B'`.  
**Output:** An integer — the length of the longest contiguous subarray with equal counts of `'A'` and `'B'`.

**Constraints:**
- `1 <= labels.length <= 10^5`
- `labels[i] ∈ { 'A', 'B' }`

**Examples:**

| Input | Output | Explanation |
|---|---|---|
| `["A","B","B","A","A","B","A"]` | `6` | `labels[0..5]` has 3 `'A'`s and 3 `'B'`s |
| `["A","A","A","B"]` | `2` | `labels[2..3]` is the only balanced pair |

**Edge cases:** All-same label → output `0`. Array of length 1 → output `0`. Entire array balanced → output `n`.

The constraint that drives the algorithmic choice: you need the *longest* such subarray, which means you must track the *first* time each balance state was seen — not the last.

---

## 🪜 How to Solve This

1. **Read the problem** → you need the longest subarray where `count('A') == count('B')`. Reframe: you want `count('A') - count('B') == 0` within some window.

2. **Reframe as a running sum** → assign `+1` for `'A'` and `-1` for `'B'`. Now the problem becomes: find the longest subarray with sum `0`. This is a classic prefix-sum structure.

3. **Prefix sum with sum `0`** → if `prefix[i] == prefix[j]` for `i < j`, then the subarray `(i, j]` has sum `0`. You want to maximize `j - i`.

4. **Maximize the window** → to maximize `j - i`, you need the *earliest* index where each prefix sum value was first seen. A hash map from `prefix_sum → first_index` gives you O(1) lookup per step.

5. **Seed the map with `{0: -1}`** → this handles the case where the balanced subarray starts at index `0`. Without this sentinel, you'd miss subarrays that begin at the array's start.

6. **Single pass** → for each index, compute the running sum, check the map, update the max length, and record the first occurrence if not already present.

The "of course" moment: once you see `count(A) - count(B)` as a running delta, the hash-map-on-first-occurrence pattern falls out naturally.

---

## 🧩 Algorithm Walkthrough

**Pattern:** Prefix Sum + Hash Map (First Occurrence Index)

This is the right abstraction because the problem asks for a range property (subarray sum equals zero), and prefix sums convert range queries into point lookups. Storing the *first* occurrence ensures maximality.

**Steps:**

1. **Initialize** a hash map `first_seen = {0: -1}`. The sentinel `0 → -1` represents the state before any element is processed, enabling subarrays that start at index `0` to be measured correctly.

2. **Initialize** `balance = 0` and `max_len = 0`.

3. **Iterate** over `labels` with index `i`:
   - If `labels[i] == 'A'`, increment `balance`. If `'B'`, decrement `balance`. This maintains the invariant that `balance` equals `count('A') - count('B')` over `labels[0..i]`.

4. **Check the map:** if `balance` is already in `first_seen`, then `labels[first_seen[balance]+1 .. i]` is a balanced subarray. Compute its length as `i - first_seen[balance]` and update `max_len`.

5. **Record first occurrence:** if `balance` is *not* in `first_seen`, store `first_seen[balance] = i`. Never overwrite — earlier indices yield longer windows.

6. **Return** `max_len` after the full pass.

**Invariant maintained:** At every step, `first_seen[b]` holds the smallest index at which balance `b` was first observed, guaranteeing that any window computed is the longest possible for that balance value.

---

## 📊 Worked Example

Input: `["A", "B", "B", "A", "A", "B", "A"]`

| i | label | balance | first_seen map | max_len |
|---|---|---|---|---|
| — | — | 0 | `{0: -1}` | 0 |
| 0 | A | +1 | `{0:-1, 1:0}` | 0 |
| 1 | B | 0 | `{0:-1, 1:0}` | **2** (0 − (−1)) |
| 2 | B | −1 | `{0:-1, 1:0, -1:2}` | 2 |
| 3 | A | 0 | `{0:-1, 1:0, -1:2}` | **4** (3 − (−1)) |
| 4 | A | +1 | `{0:-1, 1:0, -1:2}` | **5** (4 − 0) |
| 5 | B | 0 | `{0:-1, 1:0, -1:2}` | **6** (5 − (−1)) |
| 6 | A | +1 | `{0:-1, 1:0, -1:2}` | 6 (4−0=4, no update) |

**Output:** `6` — subarray `labels[0..5]`.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n).** Each element is visited exactly once. Hash map insertion and lookup are O(1) average. At 10⁶ elements this is comfortably sub-millisecond; at 10⁹ it remains linear but memory pressure on the hash map becomes the practical bottleneck, not CPU cycles.

### Space Complexity

**O(n).** The hash map holds at most `2n + 1` distinct balance values (range `[−n, n]`). This cannot be reduced to O(1) without losing the ability to answer arbitrary first-occurrence queries — the map is load-bearing.

---

## 💡 Key Takeaways

- **Pattern signal #1:** Any problem asking for the *longest subarray* satisfying a sum or count equality is a strong indicator for prefix sum + first-occurrence hash map — the word "longest" is the trigger.
- **Pattern signal #2:** When a two-class counting problem can be rewritten as `count(X) - count(Y) == 0`, you've reduced it to a zero-sum subarray problem, which is a solved pattern.
- **Gotcha #1:** Seed the map with `{0: -1}` before iteration. Forgetting this sentinel causes subarrays starting at index `0` to be silently skipped, producing wrong answers that pass most tests but fail on inputs like `["A","B"]`.
- **Gotcha #2:** Never overwrite an existing entry in `first_seen`. The map must record the *earliest* index for each balance value; overwriting with a later index shrinks the window and breaks maximality.
- **Architectural insight:** The first-occurrence hash map is a general streaming primitive — it converts "find the longest window with property P" into a stateful O(1)-per-element scan, which composes cleanly into stream processors and avoids any need for reprocessing historical data.

---

## 🚀 Variations & Further Practice

- **k distinct labels with equal frequency** (the follow-up): With `k` labels, you track a balance vector of size `k−1` (differences relative to one reference label) and hash the entire vector as the map key. The conceptual twist is that your "balance" is now multi-dimensional, and hash collisions on tuple keys require careful implementation.
- **Longest subarray with at most one more `'A'` than `'B'`**: Instead of looking for `balance == 0`, you query for `balance ∈ {0, 1}`. This breaks the single-lookup pattern and requires either two map lookups per step or a reformulation — a useful exercise in recognizing when the clean reduction no longer holds.
- **LeetCode 525 — Contiguous Array**: The canonical binary (0/1) version of this exact problem. Solving it confirms the pattern transfers directly when the label encoding changes, and exposes whether you've internalized the abstraction or just memorized the solution.