# Longest Chat Streak With At Most One Silent Minute

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given a binary array `messages`, return the maximum length of any contiguous subarray containing at most one `0`. Here, `1` means the agent was active during that minute, and `0` means silence. The output is a single integer: the longest valid chat streak. The problem is easy to state but non-trivial at scale because `messages.length` can reach `100000`, which rules out checking every possible subarray.

## 🌍 Engineering Impact
This pattern shows up anywhere systems tolerate a bounded number of anomalies inside an otherwise continuous run: sessionization in clickstreams, uptime windows with brief packet loss, fraud detection over event streams, and observability pipelines that smooth single-sample gaps. At production scale, brute-force window enumeration collapses under high-volume logs or real-time ingestion. Sliding window logic enables single-pass processing with constant auxiliary state, which matters for stream processors, edge analytics, and latency-sensitive dashboards. More importantly, it encodes a business rule—“one brief interruption is acceptable”—without materializing every candidate segment.

## 🔍 Problem Statement
You are given a binary array `messages` where each element is either `1` or `0`:

- `1` → the support agent sent at least one message during that minute
- `0` → that minute was silent

A valid chat streak is any contiguous block of minutes containing **at most one** silent minute. Return the length of the longest such block.

Constraints:

- `1 <= messages.length <= 100000`
- `messages[i]` is either `0` or `1`

Examples:

- `messages = [1,1,0,1,1,1,0,1]` → `6`
- `messages = [0,1,1,1,0,1,1]` → `5`

Edge cases matter:

- all `1`s → the whole array is valid
- all `0`s → the answer is `1`, since a single `0` is allowed
- multiple `0`s close together force the window to shrink immediately

The key constraint is array size: an `O(n^2)` scan over all subarrays is unnecessary and too expensive.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need the longest run of `1`s. We need the longest **contiguous window** with at most one `0`.
2. “Contiguous” plus “at most K bad elements” is a strong sliding-window signal. Here, `K = 1`.
3. Start with a window `[left, right]` and expand `right` one step at a time. Track how many silent minutes (`0`s) are currently inside the window.
4. As long as the window has at most one `0`, it is valid, so update the best length.
5. If adding a new element makes the zero count exceed one, the window is no longer valid. The only fix is to move `left` forward until the window contains at most one `0` again.
6. This works because every index enters the window once and leaves once. No backtracking, no nested rescans.
7. The mental model: maintain the largest valid suffix ending at each `right`, and keep the global maximum over all such suffixes.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window / Two Pointers.**  
   The constraint is local: a window is valid if it contains at most one `0`. That makes this a classic variable-size sliding window problem, where validity can be restored by advancing the left boundary.

2. **Initialize state.**  
   Keep `left = 0`, `zeroCount = 0`, and `best = 0`.  
   Invariant: before recording `best`, the current window `[left, right]` must contain at most one `0`.

3. **Expand the window with `right`.**  
   For each `right` from `0` to `n - 1`, inspect `messages[right]`. If it is `0`, increment `zeroCount`.  
   Why this is correct: the window now reflects exactly the elements between `left` and `right`.

4. **Restore validity when needed.**  
   If `zeroCount > 1`, move `left` rightward until the window becomes valid again. Each time `messages[left] == 0`, decrement `zeroCount` before incrementing `left`.  
   Invariant restored: after shrinking, the window again contains at most one `0`.

5. **Update the answer.**  
   Once valid, compute `right - left + 1` and update `best`.  
   Why this is sufficient: for each `right`, the maintained window is the longest valid window ending at `right`, because `left` only moves when forced.

6. **Return `best`.**  
   Total work is linear because both pointers move monotonically. This is the right abstraction because the problem asks for a longest contiguous region under a bounded violation budget.

## 📊 Worked Example
Example: `messages = [1,1,0,1,1,1,0,1]`

| right | messages[right] | left | zeroCount | valid window         | best |
|------:|-----------------:|-----:|----------:|----------------------|-----:|
| 0     | 1                | 0    | 0         | `[1]`                | 1    |
| 1     | 1                | 0    | 0         | `[1,1]`              | 2    |
| 2     | 0                | 0    | 1         | `[1,1,0]`            | 3    |
| 3     | 1                | 0    | 1         | `[1,1,0,1]`          | 4    |
| 4     | 1                | 0    | 1         | `[1,1,0,1,1]`        | 5    |
| 5     | 1                | 0    | 1         | `[1,1,0,1,1,1]`      | 6    |
| 6     | 0                | 3    | 1         | `[1,1,1,0]`          | 6    |
| 7     | 1                | 3    | 1         | `[1,1,1,0,1]`        | 6    |

At `right = 6`, the second `0` makes the window invalid. Shrink from the left until the first `0` is removed. The maximum valid length remains `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed at most twice: once when `right` includes it, and once when `left` excludes it. There is no nested rescan over the same range. At `10^6` elements this is routine; at `10^9`, linear work is still expensive but remains the only viable asymptotic class for a single-machine pass.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, a zero counter, and the best length. No auxiliary arrays or hash structures are required. Space cannot be meaningfully reduced further without losing the ability to track window validity.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** segment satisfying “at most K violations,” think variable-size sliding window immediately.
- Binary arrays with a budget like “flip/ignore/delete at most one value” are strong pattern-recognition signals for Two Pointers.
- Update the answer only **after** shrinking invalid windows; otherwise you may record lengths containing two `0`s.
- Be careful with `right - left + 1`: this is inclusive window length, and it is the most common off-by-one failure here.
- In production analytics, bounded-error windows are a clean way to encode tolerance rules without sacrificing single-pass throughput.

## 🚀 Variations & Further Practice
- Allow at most `k` silent minutes instead of one. Same pattern, but the budget becomes a parameter and tests whether the implementation generalizes cleanly.
- Find the longest streak after deleting exactly one element. The conceptual twist is that the answer may need to exclude one position even when the array is all `1`s.
- Process an unbounded event stream online and emit the current best streak continuously. The harder part is preserving the same invariant under streaming and memory constraints.