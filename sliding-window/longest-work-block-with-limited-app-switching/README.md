# Longest Work Block With Limited App Switching

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `apps`, where each element is the focused application for one minute, return the length of the longest contiguous block containing at most `k` distinct app names. The output is a single integer. The challenge is not correctness on small inputs, but doing it efficiently for sessions up to `200,000` minutes long, where brute-force enumeration of all subarrays is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent span under a bounded diversity constraint: desktop telemetry, observability pipelines, clickstream analysis, API consumer tracking, fraud detection, and streaming QoS enforcement. In production, the difference between rescanning windows and maintaining incremental state is the difference between linear throughput and collapse under sustained volume. Sliding-window state lets systems process event streams online, with predictable memory and latency, instead of materializing large intermediate ranges or repeatedly recomputing distinct counts from scratch.

## 🔍 Problem Statement
You are given an array `apps` of length `n`, where `apps[i]` is the application name active during minute `i`, and a non-negative integer `k`. Find the maximum length of a contiguous subarray containing no more than `k` distinct strings.

Return `0` if `apps` is empty. If `k = 0`, no non-empty subarray is valid, so return `0`.

Constraints:
- `0 <= apps.length <= 200000`
- `0 <= k <= apps.length`
- `1 <= apps[i].length <= 20`
- `apps[i]` contains lowercase letters, digits, underscores, or hyphens

Examples:
- `apps = ["mail","docs","mail","chat","docs","docs"], k = 2` → `3`
- `apps = ["ide","ide","browser","terminal","browser","terminal","music"], k = 3` → `6`

The key constraint is input size: `O(n^2)` subarray enumeration is not viable, so the algorithm must process the session in essentially one pass.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** block, so sorting or grouping globally is irrelevant. This is a window problem.
2. The validity rule is “at most `k` distinct apps” → that suggests maintaining a running frequency map for the current window.
3. Start with two pointers: `left` and `right`. Expand `right` one minute at a time, adding `apps[right]` into the map.
4. If the window now contains more than `k` distinct apps, it became invalid → move `left` forward until the window is valid again, decrementing counts and removing apps whose count drops to zero.
5. At every step where the window is valid, compute its length and update the best answer.
6. Why this works: every element enters the window once and leaves once. We never restart work for each subarray; we incrementally repair the window.
7. That gives the right shape immediately: hash map for counts, two pointers for the moving boundary, linear scan for scale.

## 🧩 Algorithm Walkthrough
1. **Handle trivial cases early.**  
   If `apps` is empty or `k == 0`, return `0`. This avoids special-case behavior later and matches the problem contract.

2. **Initialize the sliding window.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`, a hash map `freq`, and `best = 0`. The window is always `apps[left..right]`.

3. **Expand the window to the right.**  
   For each `right` from `0` to `n - 1`, increment `freq[apps[right]]`. This updates the current block with one new minute of activity.

4. **Restore validity when distinct count exceeds `k`.**  
   If `freq.size() > k`, the window violates the constraint. Move `left` forward, decrementing `freq[apps[left]]`. When a count reaches zero, remove that app from the map. Continue until `freq.size() <= k`.

5. **Record the best valid window.**  
   Once the invariant is restored, `apps[left..right]` is the longest valid window ending at `right`, because any earlier `left` would still be invalid. Update `best = max(best, right - left + 1)`.

6. **Why the abstraction fits.**  
   The problem asks for an extremal contiguous range under a monotone constraint: adding elements can break validity, removing from the left can restore it. That is exactly when sliding window dominates brute force.

7. **Invariant maintained throughout.**  
   After each iteration, the window contains at most `k` distinct apps, and `freq` exactly matches the counts inside that window. Correctness follows from never discarding a potentially better valid suffix ending at `right`.

## 📊 Worked Example
Example: `apps = ["mail","docs","mail","chat","docs","docs"]`, `k = 2`

| right | app added | left after shrink | window                           | distinct | best |
|------:|-----------|-------------------|----------------------------------|---------:|-----:|
| 0     | mail      | 0                 | [mail]                           | 1        | 1    |
| 1     | docs      | 0                 | [mail, docs]                     | 2        | 2    |
| 2     | mail      | 0                 | [mail, docs, mail]               | 2        | 3    |
| 3     | chat      | 2                 | [mail, chat]                     | 2        | 3    |
| 4     | docs      | 3                 | [chat, docs]                     | 2        | 3    |
| 5     | docs      | 3                 | [chat, docs, docs]               | 2        | 3    |

Trace notes:
- At `right = 3`, adding `chat` creates 3 distinct apps, so shrink from the left until only 2 remain.
- The same repair happens at `right = 4`.
- The maximum valid length observed is `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time, where `n = apps.length`. Each app is added to the window once and removed at most once, so pointer movement is linear. Hash map updates dominate but are constant-time on average. At `10^6` elements this remains practical; at `10^9`, linear time is still expensive but fundamentally the best possible for exact single-pass processing.

### Space Complexity
`O(min(n, k))` auxiliary space for the frequency map, more precisely `O(number of distinct apps in the current window)`. That state is necessary to know when distinct count changes. You can’t reduce it meaningfully without losing exactness or paying with rescans.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under an “at most `k` distinct” constraint, think sliding window immediately.
- When validity can be restored by moving only the left boundary forward, the constraint is monotone and two pointers are usually the right abstraction.
- Remove keys from the frequency map when their count reaches zero; keeping zero-count entries breaks the distinct-count invariant.
- Update the answer only after shrinking back to a valid window, and compute length as `right - left + 1` to avoid classic off-by-one errors.
- In production stream processing, incremental window state turns repeated recomputation into bounded per-event work, which is the core scalability win.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters**: same pattern on strings; the twist is character-level indexing and often tighter constant-factor requirements.
- **Longest repeating character replacement**: still a sliding window, but validity depends on window size minus max-frequency, not distinct count.
- **Subarrays with exactly `k` distinct elements**: harder because “exactly” is not directly monotone; typically solved as `atMost(k) - atMost(k - 1)`.