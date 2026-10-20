# Longest Translation Draft With Limited Unknown Terms

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Set, Two Pointers

---

## 🗂 Problem Overview
Given a draft translation as an array of words, find the maximum length of any contiguous segment containing at most `k` words not present in an approved dictionary. The approved list should be treated as a set, even if duplicates appear. The output is a single integer: the longest valid window length. The challenge is scale: with up to 200,000 words, any solution that repeatedly rescans subarrays or performs linear dictionary lookups will not hold.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must find the longest acceptable run under a bounded error budget. Examples include log pipelines tolerating a limited number of malformed records, search indexing passes allowing a capped number of unknown tokens, compiler or static-analysis passes scanning spans with unresolved symbols, and moderation/review tooling batching content with limited policy violations. At production scale, brute-force window enumeration collapses under throughput and latency requirements. The sliding-window approach enables single-pass processing, predictable memory use, and straightforward adaptation to streaming or online systems where data arrives incrementally.

## 🔍 Problem Statement
You are given:

- `words`: an array of lowercase strings representing the draft
- `approved`: an array of lowercase strings representing approved dictionary entries
- `k`: the maximum number of unknown words allowed in a contiguous section

Return the length of the longest contiguous subarray of `words` containing at most `k` words not present in `approved`.

Treat `approved` as a set: duplicates do not matter.

Constraints:

- `1 <= words.length <= 200000`
- `1 <= approved.length <= 200000`
- `0 <= k <= words.length`
- `1 <= word length <= 20`

Examples:

- `words = ["hola","mundo","api","seguro","beta","listo"]`
- `approved = ["hola","mundo","seguro","listo"]`
- `k = 1`
- Output: `4`

- `words = ["save","draft","tmp","publish","tmp","done"]`
- `approved = ["save","draft","publish","done"]`
- `k = 0`
- Output: `2`

The key constraint is input size: `O(n^2)` subarray scanning is infeasible, so the algorithm must be near-linear.

## 🪜 How to Solve This
1. Read the problem → we need the **longest contiguous subarray**, so this is immediately a window problem, not a sorting or global counting problem.

2. The validity rule is simple: a window is valid if it contains at most `k` unknown words. That means validity changes incrementally as we expand or shrink the window.

3. Membership in `approved` must be fast → convert `approved` into a `HashSet`. Without that, every lookup could become linear and destroy performance.

4. Start with two pointers, `left` and `right`, representing the current window. Move `right` forward one word at a time, updating the count of unknown words.

5. If the window becomes invalid (`unknownCount > k`), move `left` forward until the window is valid again. While shrinking, decrement `unknownCount` when an unknown word leaves the window.

6. After each expansion/shrink cycle, the window is the longest valid window ending at `right`. Record its length.

7. Because each pointer only moves forward, the full scan is linear after set construction. That is the core reason this approach scales.

## 🧩 Algorithm Walkthrough
1. **Build a hash set from `approved`.**  
   This gives `O(1)` average membership checks. Correctness depends on treating duplicates as irrelevant, which a set naturally enforces.

2. **Initialize the sliding window.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`, `unknownCount = 0`, and `best = 0`. The invariant will be: after adjustment, the window `[left, right]` is always valid.

3. **Expand the window by advancing `right`.**  
   For each `words[right]`, check whether it exists in the approved set. If not, increment `unknownCount`. This updates the window state in constant time.

4. **Restore validity when needed.**  
   If `unknownCount > k`, the window violates the constraint. Move `left` rightward until `unknownCount <= k` again. Each time a word exits the window, if it was unknown, decrement `unknownCount`.

5. **Record the current valid length.**  
   Once the invariant is restored, compute `right - left + 1` and update `best` if larger. This is correct because, for the current `right`, any valid window starting left of `left` would have already violated the unknown-word budget.

6. **Continue until the end of the array.**  
   Every element enters the window once and leaves at most once. That bounded movement is why Sliding Window is the right abstraction: local updates preserve a global optimum over contiguous ranges without rescanning prior state.

## 📊 Worked Example
Example: `words = ["hola","mundo","api","seguro","beta","listo"]`, `approved = ["hola","mundo","seguro","listo"]`, `k = 1`

| right | word    | unknownCount after add | left after shrink | window                         | length | best |
|------:|---------|------------------------:|------------------:|--------------------------------|-------:|-----:|
| 0     | hola    | 0                       | 0                 | ["hola"]                       | 1      | 1    |
| 1     | mundo   | 0                       | 0                 | ["hola","mundo"]               | 2      | 2    |
| 2     | api     | 1                       | 0                 | ["hola","mundo","api"]         | 3      | 3    |
| 3     | seguro  | 1                       | 0                 | ["hola","mundo","api","seguro"]| 4      | 4    |
| 4     | beta    | 2                       | 3                 | ["seguro","beta"]              | 2      | 4    |
| 5     | listo   | 1                       | 3                 | ["seguro","beta","listo"]      | 3      | 4    |

When `beta` enters, unknown count becomes 2, exceeding `k`. Shrink from the left until only one unknown remains. The maximum valid length observed is 4.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = words.length` and `m = approved.length`. Building the hash set costs `O(m)`, and the sliding window scans `words` in `O(n)` because both pointers only move forward. At `10^6` elements this is practical; at `10^9`, it is still linear but likely constrained by memory bandwidth and I/O.

### Space Complexity
`O(m)` for the approved-word hash set. The window itself uses constant extra state. Space can only be reduced by avoiding the set, but that would trade memory for much slower membership checks and usually lose the required performance envelope.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a bounded constraint, Sliding Window should be an immediate candidate.
- If window validity can be updated incrementally when one element enters or leaves, Two Pointers usually beats any prefix-rescan or nested-loop approach.
- Treat `approved` as a set, not a list; forgetting deduplication semantics can lead to unnecessary complexity or incorrect assumptions about counts.
- Be precise about when to update `best`: only after shrinking enough to restore `unknownCount <= k`, otherwise you may record invalid windows.
- The production-grade insight is to convert expensive repeated validation into constant-time incremental state updates, which is the difference between batch-friendly and stream-friendly designs.

## 🚀 Variations & Further Practice
- Allow each unknown word to carry a review cost, and require total cost within budget `k`; same window pattern, but the tracked state becomes a weighted sum instead of a simple count.
- Ask for the longest window containing at most `k` **distinct** unknown words; now you need a frequency map, not just a scalar counter.
- Process an unbounded stream and emit the best window seen so far online; same algorithmic core, but with operational concerns around state retention, checkpointing, and backpressure.