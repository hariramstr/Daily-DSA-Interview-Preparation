# Longest Shipping Lane With Limited Hazard Labels

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array of package hazard labels and an integer `k`, return the maximum length of any contiguous subarray containing at most `k` distinct labels. The output is a single integer. The challenge is not correctness but scale: with up to `100000` elements, enumerating all subarrays is quadratic and infeasible. The problem demands a linear-time way to maintain a valid window while scanning once from left to right.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must maintain the longest recent span under a bounded diversity constraint. Examples include streaming observability pipelines tracking the longest interval with limited error classes, fraud systems detecting stable transaction sequences with few merchant categories, log processors grouping contiguous events with constrained cardinality, and network telemetry identifying stretches with limited protocol variation. Without a sliding-window approach, implementations degrade into repeated rescans, high tail latency, and poor cache behavior. With it, you get single-pass processing, predictable memory growth, and a design that maps cleanly to online or near-real-time ingestion paths.

## 🔍 Problem Statement
You are given an array of strings `labels`, where `labels[i]` is the hazard category of the `i`-th package on a conveyor belt, and an integer `k`. Find the length of the longest contiguous block whose labels contain at most `k` distinct values.

Constraints:

- `1 <= labels.length <= 100000`
- `1 <= labels[i].length <= 20`
- `labels[i]` contains uppercase letters, digits, or underscores
- `1 <= k <= labels.length`

Examples:

- `labels = ["FLAMMABLE", "CORROSIVE", "FLAMMABLE", "TOXIC", "CORROSIVE", "CORROSIVE"], k = 2` → `3`
- `labels = ["A", "A", "B", "B", "C", "B", "B", "A"], k = 2` → `5`

Edge cases matter: if all labels are identical, the answer is the full array; if `k` is at least the number of distinct labels in the array, the full array is also valid. The key algorithmic constraint is the input size: any approach that checks every subarray is too slow.

## 🪜 How to Solve This
1. Read the requirement carefully → we need a **contiguous** block, so sorting or global frequency analysis is irrelevant.

2. Notice the validity rule depends only on the current block’s **number of distinct labels** → that suggests maintaining a moving window plus a frequency map.

3. Start with two pointers, `left` and `right`, representing the current window. Expand `right` one package at a time and record its label count.

4. After each expansion, check whether the window now contains more than `k` distinct labels. If it does, the window is invalid.

5. To restore validity, move `left` forward, decrementing counts for labels that leave the window. When a count drops to zero, remove that label from the map, reducing the distinct count.

6. At every point where the window is valid, compute its length and update the best answer.

7. Why this works: each element enters the window once and leaves once. That gives linear work instead of re-evaluating overlapping subarrays repeatedly.

This is the standard “longest valid subarray under a bounded constraint” signal: expand greedily, shrink only when invalid.

## 🧩 Algorithm Walkthrough
1. **Initialize window state**  
   Use two pointers: `left = 0`, and iterate `right` from `0` to `n - 1`. Maintain a hash map `freq[label] -> count` and an integer `maxLen = 0`.  
   **Invariant:** `freq` always matches the multiset of labels in `labels[left..right]`.

2. **Expand the window to the right**  
   For each `labels[right]`, increment its count in `freq`.  
   **Why:** We are considering every possible window ending at `right`.  
   **Invariant:** Before shrinking, the window may be invalid, but it accurately reflects the current range.

3. **Check the distinct-label constraint**  
   If `freq.size() <= k`, the window is valid. If `freq.size() > k`, it violates the problem constraint.  
   **Why:** Distinct count is exactly the acceptance rule.

4. **Shrink from the left until valid**  
   While `freq.size() > k`, decrement `freq[labels[left]]`, remove the key if its count becomes zero, and increment `left`.  
   **Why it’s correct:** Any valid window ending at `right` must start at or after the first position that restores the distinct-count bound.  
   **Invariant:** After the loop, `labels[left..right]` is the longest valid window ending at `right`.

5. **Record the best answer**  
   Once valid, compute `right - left + 1` and update `maxLen`.  
   **Why:** Since `left` is minimal for this `right`, this is the maximum valid length ending here.

6. **Pattern fit: Sliding Window / Two Pointers + Hash Map**  
   This abstraction is right because the constraint is monotonic under expansion and repairable by contraction. Adding elements can break validity; removing from the left can restore it without revisiting prior windows.

## 📊 Worked Example
Example: `labels = ["A", "A", "B", "B", "C", "B", "B", "A"]`, `k = 2`

| right | label | action | left | freq | valid? | maxLen |
|---|---|---|---:|---|---|---:|
| 0 | A | add A | 0 | {A:1} | yes | 1 |
| 1 | A | add A | 0 | {A:2} | yes | 2 |
| 2 | B | add B | 0 | {A:2,B:1} | yes | 3 |
| 3 | B | add B | 0 | {A:2,B:2} | yes | 4 |
| 4 | C | add C, shrink | 2 | {B:2,C:1} | yes | 4 |
| 5 | B | add B | 2 | {B:3,C:1} | yes | 4 |
| 6 | B | add B | 2 | {B:4,C:1} | yes | 5 |
| 7 | A | add A, shrink | 4 | {C:1,B:2,A:1} → {C:1,B:2,A:1} → {C:1,B:2,A:1} → {C:1,B:2,A:1} then {C:1,B:2,A:1} invalid until left=4? |

At `right = 6`, the window `["B","B","C","B","B"]` has length `5`, which is the maximum valid answer.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = labels.length`. Each label is added to the window once when `right` advances and removed at most once when `left` advances. Hash map updates are amortized `O(1)`, so the dominant cost is a single linear scan. At `10^6` elements this remains practical; at `10^9`, throughput and memory locality become the operational bottlenecks.

### Space Complexity
`O(min(n, d))`, where `d` is the number of distinct labels in the active window, bounded here by the total distinct labels seen. The hash map owns the extra space. You cannot reduce this below tracking active-label counts without losing constant-time validity updates.

## 💡 Key Takeaways
- If the problem asks for the **longest contiguous range** satisfying “at most / at least / no more than” some property, a sliding window should be your first candidate.
- If validity depends on **counts inside the current range**, not on element order outside it, pair two pointers with a frequency map.
- Remove labels from the map when their count reaches zero; otherwise `freq.size()` overstates the distinct count and breaks correctness.
- Update `maxLen` only after shrinking back to a valid window; recording before repair produces inflated lengths.
- The production-grade insight is to maintain incremental state over a moving boundary rather than recomputing window properties from scratch; that is the difference between scalable stream processing and accidental quadratic behavior.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters**: same pattern, but on a string; the conceptual twist is tighter constant-factor optimization and character-domain assumptions.
- **Longest repeating character replacement**: still sliding window, but validity depends on how many edits are needed, not just distinct count.
- **Minimum window substring**: same two-pointer machinery, but the objective flips from maximizing a valid window to minimizing one that satisfies a coverage constraint.