# Find the First Broken Sequence Number

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Hash Set, Simulation

---

## 🗂 Problem Overview
Given an unsorted integer array, return the smallest positive integer that does not appear in it. The expected sequence starts at `1` and increases without gaps, but the input may contain corrupted values such as negatives, zero, duplicates, or numbers far outside the useful range. The key constraint is that only values in `[1, n]` can affect the answer for an array of length `n`, so the solution should focus on membership, not ordering.

## 🌍 Engineering Impact
This pattern shows up anywhere systems expect dense identifiers or contiguous progress markers: event ingestion pipelines validating offsets, job schedulers detecting the first missing task index, storage engines checking SSTable segment numbering, and compiler/linker pipelines verifying symbol or relocation slots. At scale, the important decision is to ignore irrelevant data early and track only the bounded domain that can influence the result. Without that discipline, teams over-sort, over-scan, or retain unnecessary state. The right approach turns a noisy batch into a bounded membership problem, which is cheaper, easier to reason about, and safer under corrupted input.

## 🔍 Problem Statement
You are given an integer array `nums` with length `n` where sequence numbers are supposed to cover positive integers starting from `1` with no gaps. One or more records may be corrupted: values can be unsorted, duplicated, zero, negative, or much larger than `n`. Return the smallest positive integer that does not appear in the array.

The critical observation is that the answer must lie in `[1, n + 1]`. If every value from `1` through `n` is present, then the first missing positive is `n + 1`. Anything less than `1` or greater than `n` cannot change that result.

Examples:

- `nums = [3, 4, -1, 1]` → `2`
- `nums = [1, 2, 0]` → `3`

Constraints:

- `1 <= nums.length <= 100000`
- `-1000000000 <= nums[i] <= 1000000000`

That bounded answer range is what drives the algorithmic choice.

## 🪜 How to Solve This
1. Read the problem → notice we do **not** need the array sorted; we only need to know whether certain positive integers exist.

2. Ask which values can matter → for an array of length `n`, only numbers in `[1, n]` can block the answer.  
   - `<= 0` is irrelevant because we want a positive result.  
   - `> n` is irrelevant because even if all `1..n` exist, the answer becomes `n + 1`.

3. That reframes the task as a **bounded membership check** over `1..n`, not a general search problem.

4. Once the problem is “which values from a small range are present?”, a `HashSet` is the straightforward tool.  
   - One pass: insert every valid value `1 <= x <= n`.  
   - Second pass: scan `1` through `n` and return the first value not in the set.

5. If all `1..n` are present, return `n + 1`.

This is the easy-version solution because it is linear, simple to implement, and robust under noisy input. The main mental shift is to stop caring about order and start caring about presence within a constrained domain.

## 🧩 Algorithm Walkthrough
**Pattern:** Hash Set / bounded membership tracking.

1. **Compute `n = nums.length`.**  
   This establishes the only range that matters: `1..n`, with `n + 1` as the fallback answer.  
   **Invariant:** the final answer must be in `[1, n + 1]`.

2. **Create an empty hash set `seen`.**  
   We use it to record which relevant positive integers are present.  
   **Why this abstraction fits:** the problem asks repeated “does value `k` exist?” queries, and a hash set gives expected `O(1)` membership checks.

3. **Scan the array once.**  
   For each `x` in `nums`, insert it into `seen` only if `1 <= x <= n`.  
   Ignore negatives, zero, and values greater than `n` because they cannot affect the smallest missing positive.  
   **Invariant:** after this pass, `seen` contains exactly the useful values from the input.

4. **Scan integers from `1` to `n`.**  
   For each candidate `k`, check whether `k` is in `seen`. The first missing one is the answer.  
   **Why it is correct:** by scanning in increasing order, the first absent value is by definition the smallest missing positive.

5. **If every value `1..n` is present, return `n + 1`.**  
   This is the only remaining possibility under the bounded-range argument.

This solution is correct because it reduces the problem to complete coverage of a finite interval and tests that coverage directly.

## 📊 Worked Example
Example: `nums = [3, 4, -1, 1]`, so `n = 4`

| Step | Value | Action | `seen` after step |
|---|---:|---|---|
| 1 | 3 | valid (`1..4`), insert | `{3}` |
| 2 | 4 | valid, insert | `{3, 4}` |
| 3 | -1 | ignore | `{3, 4}` |
| 4 | 1 | valid, insert | `{1, 3, 4}` |

Now scan candidates from `1` to `4`:

| Candidate | In `seen`? | Result |
|---|---|---|
| 1 | yes | continue |
| 2 | no | return `2` |

Why this works: the set tells us exactly which useful positive numbers exist. We do not care that the original array was unsorted or contained `-1`. Once `1` is confirmed present and `2` is absent, no larger number can be the first missing positive.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. One pass builds the hash set, and one pass scans candidates from `1` to `n`. The dominant operations are hash insert and lookup. At `10^6` elements this is still practical in memory-resident workloads; at `10^9`, the asymptotics remain linear but memory bandwidth and allocation behavior dominate.

### Space Complexity
`O(n)` extra space in the worst case, owned by the hash set storing up to `n` relevant values. It can be reduced to `O(1)` with the in-place index-placement variant, but that increases implementation complexity and mutates the input array.

## 💡 Key Takeaways
- If the prompt asks for the “smallest missing” positive integer, look for a bounded answer range like `[1, n + 1]` before considering sorting.
- When order is irrelevant and the real question is “which values from a constrained domain are present?”, a hash set is usually the right first move.
- Do not insert every number blindly; values `<= 0` and `> n` are noise for this problem and should be ignored.
- The fallback is `n + 1`, not `n`; if all values `1..n` exist, the next positive integer is the answer.
- In production code, aggressively shrinking the problem to the subset of state that can influence the result is often the difference between a scalable design and unnecessary work.

## 🚀 Variations & Further Practice
- **First Missing Positive in `O(1)` extra space** — same problem, but now you must use in-place index placement or cyclic swapping; the conceptual twist is encoding membership inside the input array itself.
- **Find all missing numbers in `[1, n]`** — instead of one answer, return every absent value; the harder part is preserving linear time while handling duplicates cleanly.
- **Streaming missing-ID detection across batches** — values arrive incrementally rather than in one array; the twist is maintaining compact state and answering the current first gap online.