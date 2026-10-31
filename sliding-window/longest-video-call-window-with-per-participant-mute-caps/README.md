# Longest Video Call Window With Per-Participant Mute Caps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `participants`, where each element is the speaker ID at a given second, and an array `muteCap`, where `muteCap[id]` is the maximum allowed occurrences of that participant inside a contiguous window, find the longest valid subarray. A window is valid only if every participant count stays within its own cap. The challenge is that caps differ per ID, may be zero, and validating the whole frequency map after each update is too expensive for large inputs.

## 🌍 Engineering Impact
This pattern shows up in streaming systems, rate-limit enforcement, observability pipelines, and real-time session analytics. Examples include per-tenant quota windows, per-key event burst controls, and ingestion filters where each entity has its own threshold. At scale, the difference between incremental validity tracking and repeated full-state validation is the difference between linear throughput and collapse under cardinality. The core architectural idea is maintaining a compact violation signal as state mutates, rather than recomputing global correctness. That enables predictable latency, bounded memory growth, and straightforward online processing over unbounded streams.

## 🔍 Problem Statement
You are given:

- `participants`, an array of length `n` where `participants[i]` is the participant ID speaking during second `i`
- `muteCap`, an array of length `m` where `muteCap[id]` is the maximum allowed count of participant `id` inside a valid contiguous window
- Participant IDs are in `[0, m - 1]`

Return the maximum length of a contiguous subarray whose per-participant frequencies all satisfy:

`count[id] <= muteCap[id]`

Constraints:

- `1 <= participants.length <= 200000`
- `1 <= m <= 200000`
- `0 <= participants[i] < m`
- `0 <= muteCap[id] <= participants.length`

Examples:

- `participants = [1,2,1,3,2,1,2]`, `muteCap = [0,2,2,1]` → `5`
- `participants = [4,4,2,4,1,2,1,1,3]`, `muteCap = [0,1,2,2,1]` → `4`

The decisive constraint is input size: any approach that rescans all participant counts whenever the window changes will time out.

## 🪜 How to Solve This
1. Read the condition carefully → validity is local to a window, but defined by frequency counts. That immediately suggests a sliding window rather than prefix sums or sorting.

2. Notice what makes it hard → each participant has a different cap. You cannot track a single global limit like “at most `k` distinct” or “max frequency <= k”.

3. Ask what changes when the window moves → only two counts ever change: the ID added on the right and the ID removed on the left. So validity should also be updated incrementally.

4. That leads to the key idea → maintain a frequency map for the current window plus a small piece of state saying whether any participant currently exceeds its cap.

5. Expand right greedily → add the next participant, update its count, and detect whether that specific ID became invalid.

6. If the window is invalid, shrink from the left until all violations disappear. Because left and right only move forward, total work stays linear.

7. Track the maximum valid width seen during this process. The result is the longest stable window.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers / Sliding Window pattern.**  
   Maintain a window `[left, right]` over `participants`. This is the right abstraction because we need the longest **contiguous** segment satisfying a constraint that can be updated as elements enter and leave.

2. **Track frequencies inside the current window.**  
   Use an array of size `m` or a hash map keyed by participant ID. When `participants[right] = id` enters the window, increment `freq[id]`.

3. **Track violations incrementally, not by rescanning all IDs.**  
   Maintain an integer `overLimit`, the number of participant IDs whose frequency currently exceeds `muteCap[id]`.  
   - If after incrementing `freq[id]`, it becomes `muteCap[id] + 1`, then that ID just crossed from valid to invalid, so increment `overLimit`.

4. **Shrink while invalid.**  
   While `overLimit > 0`, remove `participants[left] = x` from the window and advance `left`.  
   - Before or after decrementing, detect whether `x` transitions from invalid back to valid. Concretely, if `freq[x] == muteCap[x] + 1` before decrement, then decrementing restores validity for that ID, so decrement `overLimit`.

5. **Maintain the invariant.**  
   After the inner shrink loop finishes, the current window satisfies `freq[id] <= muteCap[id]` for every ID. That invariant is enough to safely update the answer with `right - left + 1`.

6. **Why this is correct.**  
   For each `right`, the algorithm keeps the leftmost position that makes the window valid after necessary shrinking. Any longer window ending at `right` would include the invalid prefix we already removed. Therefore every maximal valid suffix is considered, and the global maximum is found.

## 📊 Worked Example
Take `participants = [1,2,1,3,2,1,2]`, `muteCap = [0,2,2,1]`.

| right | added | freq snapshot (1,2,3) | overLimit | left after shrink | valid window | best |
|---|---:|---|---:|---:|---|---:|
| 0 | 1 | (1,0,0) | 0 | 0 | `[1]` | 1 |
| 1 | 2 | (1,1,0) | 0 | 0 | `[1,2]` | 2 |
| 2 | 1 | (2,1,0) | 0 | 0 | `[1,2,1]` | 3 |
| 3 | 3 | (2,1,1) | 0 | 0 | `[1,2,1,3]` | 4 |
| 4 | 2 | (2,2,1) | 0 | 0 | `[1,2,1,3,2]` | 5 |
| 5 | 1 | (3,2,1) | 1 | 1 | `[2,1,3,2,1]` | 5 |
| 6 | 2 | (2,3,1) | 1 | 2 | `[1,3,2,1,2]` | 5 |

At `right = 5`, participant `1` exceeds cap `2`, so we shrink until its count returns to `2`. Same idea at `right = 6` for participant `2`. Maximum valid length remains `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = participants.length`. Each element is added to the window once and removed at most once, so the dominant work is pointer movement plus constant-time frequency updates. This remains practical at `10^6` elements; at `10^9`, the algorithm is still asymptotically right but becomes constrained by memory bandwidth and I/O.

### Space Complexity
`O(m)` with an array-backed frequency table, or `O(d)` with a hash map where `d` is the number of distinct participant IDs currently tracked. The space is owned by frequency storage. Using a hash map reduces sparse-ID memory usage but adds higher constant factors and less predictable cache behavior.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous region under mutable frequency constraints, default to Sliding Window before considering heavier machinery.
- When validity changes only because one item enters or leaves, look for a constant-time violation counter instead of rescanning the whole state.
- The main trap is updating `overLimit` only on threshold crossings; incrementing or decrementing it on every count change is wrong.
- Be precise about the shrink condition: keep shrinking while `overLimit > 0`, then compute window length as `right - left + 1`.
- The transferable design insight is to maintain a minimal incremental correctness signal for dynamic state, rather than recomputing global validity after each mutation.

## 🚀 Variations & Further Practice
- Allow up to `k` total cap violations across the window instead of zero. The twist is that validity becomes a budgeted aggregate constraint, not a strict per-ID feasibility check.
- Make `muteCap` depend on time or window position, such as per-participant caps that change by minute. The harder part is that adding/removing elements is no longer enough; the constraint itself mutates.
- Ask for the number of valid subarrays instead of the longest one. Same sliding-window core, but now every valid right boundary contributes multiple subarrays and counting logic replaces max tracking.