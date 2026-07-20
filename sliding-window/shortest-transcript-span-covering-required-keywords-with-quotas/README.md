# Shortest Transcript Span Covering Required Keywords with Quotas

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given a transcript `words` and a set of required keywords with minimum quotas, find the length of the shortest contiguous span whose word frequencies satisfy every quota. Return `-1` if no such span exists. The difficulty is not membership but multiplicity: each keyword may need to appear several times, and `words` can be as large as 200,000 tokens, which rules out rescanning candidate spans or using quadratic substring checks.

## 🌍 Engineering Impact
This pattern shows up in log analytics, alert correlation, ad-query matching, compliance scanning, and streaming NLP pipelines where you need the smallest time or token window satisfying a set of conditions. Think incident timelines containing enough occurrences of specific error classes, or transcript spans matching moderation rules with repeated triggers. At scale, naive rescans explode CPU and cache miss rates, especially in online systems. A quota-aware sliding window turns the problem into a single pass with bounded state, enabling low-latency processing on large streams and predictable memory behavior under production traffic.

## 🔍 Problem Statement
You are given:

- `words`: an array of lowercase strings, length up to `200000`
- `required`: distinct lowercase keywords
- `need`: minimum counts for each corresponding keyword

Find the minimum length of a contiguous subarray `words[l...r]` such that every `required[j]` appears at least `need[j]` times inside that span. If no such span exists, return `-1`.

Key constraints:

- `1 <= required.length == need.length <= 100000`
- `1 <= sum(need) <= 200000`
- all `required[j]` are distinct

Examples:

- `words = ["api","error","db","api","timeout","error","api"]`
- `required = ["api","error"]`, `need = [2,1]`
- Output: `4`

- `words = ["login","cache","login","queue","cache","queue"]`
- `required = ["login","queue","cache"]`, `need = [2,1,2]`
- Output: `5`

The algorithmic driver is scale: with 200k tokens, only near-linear solutions are viable.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so sorting or global counting is irrelevant. This is a window problem.
2. Notice the condition is “at least these counts” → not just presence/absence. That suggests a frequency map for required words and another for the current window.
3. Ask what makes a window valid → every keyword must meet its quota. Instead of checking all keywords after every move, track a single summary value: how many required keywords currently satisfy their quota.
4. Expand the right pointer → add the new word if it matters. When a word’s count reaches its required threshold, increment the satisfied-keyword counter.
5. Once all quotas are satisfied → shrink from the left as aggressively as possible while preserving validity. This is where the minimum span emerges.
6. If removing a left word would drop some keyword below quota, stop shrinking and continue expanding right.
7. Because each pointer only moves forward, the whole process is linear after map setup.

This is the standard “minimum valid window” pattern, adapted from binary satisfaction to quota satisfaction.

## 🧩 Algorithm Walkthrough
1. **Build the requirement map.**  
   Store `target[word] = required quota`. Let `k = required.length`. This defines the exact constraints the window must satisfy.

2. **Initialize sliding-window state.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`. Maintain `window[word]` only for required words. Also track `formed`, the number of keywords whose current count is at least their target quota. The invariant is: `formed == k` iff the current window is valid.

3. **Expand the window with `right`.**  
   For each `words[right]`, if it is not required, it does not affect validity. If it is required, increment its count in `window`. If the count becomes exactly equal to `target[word]`, increment `formed`. This transition matters because only threshold crossings change validity.

4. **Shrink while valid.**  
   While `formed == k`, the window satisfies all quotas. Record `right - left + 1` as a candidate answer. Then try removing `words[left]`. If it is required, decrement its count; if the count falls from `target[word]` to `target[word] - 1`, decrement `formed` because the window just became invalid. Increment `left` and continue.

5. **Why this is correct.**  
   Every valid window is considered when `right` reaches its end. For each such `right`, the inner loop finds the smallest valid left boundary. Therefore the minimum over all recorded candidates is the global optimum.

6. **Termination and failure case.**  
   If no window ever reaches `formed == k`, return `-1`. Otherwise return the best recorded length.

## 📊 Worked Example
Example: `words = ["api","error","db","api","timeout","error","api"]`  
`target = {api:2, error:1}`

| right | word    | window counts         | formed | left moves | best |
|------:|---------|-----------------------|:------:|------------|:----:|
| 0     | api     | api=1                 | 0      | no         | inf  |
| 1     | error   | api=1,error=1         | 1      | no         | inf  |
| 2     | db      | api=1,error=1         | 1      | no         | inf  |
| 3     | api     | api=2,error=1         | 2      | valid      | 4    |
|       | shrink  | remove `api` at left  | 1      | left=1     | 4    |
| 4     | timeout | api=1,error=1         | 1      | no         | 4    |
| 5     | error   | api=1,error=2         | 1      | no         | 4    |
| 6     | api     | api=2,error=2         | 2      | shrink     | 4    |

At `right = 3`, the first valid window is `[0..3]`, length `4`. Later windows are valid but not shorter. Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = words.length` and `m = required.length`. Building the target map is `O(m)`, and the sliding window is `O(n)` because each token is added once by `right` and removed at most once by `left`. At million-scale input this remains practical; at billion-scale, the bottleneck becomes I/O and memory locality, not asymptotic behavior.

### Space Complexity
`O(m)` for the requirement map and current window counts over required keywords. Space is owned by the hash maps keyed by distinct required words. You can reduce constant factors with integer ID compression, but not the asymptotic bound without sacrificing direct lookup simplicity.

## 💡 Key Takeaways
- If the problem asks for the shortest contiguous segment satisfying frequency constraints, it is a sliding-window problem, not a prefix-sum or sorting problem.
- When validity depends on many keys, track threshold crossings with a single `formed` counter instead of rechecking the whole map each step.
- Only increment `formed` when a count becomes exactly equal to its quota; extra occurrences must not double-count satisfaction.
- When shrinking, invalidate the window only when a count drops from quota to quota minus one; this is the common off-by-one bug.
- In production systems, this pattern is a general template for single-pass constraint satisfaction over streams with bounded mutable state.

## 🚀 Variations & Further Practice
- Return the actual span indices, and break ties by earliest start or lexicographically smallest span; the twist is preserving deterministic tie-handling during shrink.
- Support dynamic updates to `required` and `need` during stream processing; the harder part is maintaining validity under changing quotas without restarting the scan.
- Allow at most `K` non-required words or weighted token costs inside the window; now the window must satisfy both lower-bound quotas and an additional budget constraint.