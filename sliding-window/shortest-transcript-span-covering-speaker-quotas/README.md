# Shortest Transcript Span Covering Speaker Quotas

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given a transcript array `speakers` and quota requirements `[speakerId, minCount]`, find the shortest contiguous subarray whose speaker frequencies satisfy every quota. Return `[start, end]` using 0-based indices, breaking ties by smaller `start`. If no valid span exists, return `[-1, -1]`. The problem is non-trivial because transcript length can reach `2 * 10^5`, speaker IDs are sparse and large, and brute-force checking all subarrays is infeasible.

## 🌍 Engineering Impact
This pattern shows up in log analytics, search snippet extraction, streaming observability, ad delivery pacing, and meeting intelligence systems. You need the smallest interval that satisfies a set of frequency constraints over a high-cardinality event stream. At scale, naive rescans or per-window recomputation collapse under throughput and latency targets. A sliding-window design enables single-pass processing, bounded state over only relevant keys, and deterministic tie-breaking. That matters in production when building online extractors, compliance detectors, or ranking features where minimal qualifying spans reduce downstream storage, review cost, and model input size.

## 🔍 Problem Statement
You are given:

- `speakers`, where `speakers[i]` is the speaker ID for the `i`-th utterance
- `requirements`, where each pair `[speakerId, minCount]` specifies a minimum quota

Find the shortest contiguous span `[start, end]` such that, within `speakers[start..end]`, every required speaker appears at least the requested number of times. If multiple spans have the same length, choose the one with the smallest `start`. If no span satisfies all quotas, return `[-1, -1]`.

Constraints:

- `1 <= speakers.length <= 2 * 10^5`
- `1 <= requirements.length <= 10^5`
- `1 <= speakerId <= 10^9`
- `1 <= minCount <= speakers.length`
- Requirement speaker IDs are distinct

Examples:

- `speakers = [4,2,7,2,4,2,9,7,4]`, `requirements = [[2,2],[4,2],[7,1]]` → `[0,4]`
- `speakers = [5,1,5,3,1,5,2,3]`, `requirements = [[1,2],[3,2],[2,1]]` → `[3,7]`

The key constraint is input size: anything worse than near-linear time is not viable.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so sorting or global counting destroys the structure we care about.

2. The condition is frequency-based: “at least `minCount` for selected speaker IDs.” That immediately suggests a frequency map keyed by speaker ID.

3. Brute force would try every start/end pair and recheck quotas repeatedly. With `n = 2 * 10^5`, `O(n^2)` is dead on arrival.

4. Because the constraint is monotonic with expansion — adding utterances can only help satisfy quotas — think **sliding window / two pointers**.

5. Maintain a window `[left, right]` and counts only for required speakers. Track how many requirements are currently satisfied.

6. Expand `right` until all quotas are met. Then shrink `left` as much as possible while preserving validity. That gives the shortest valid window ending at `right`.

7. Repeat for all `right`. Since each pointer moves forward at most `n` times, the whole process is linear aside from hash-map operations.

8. Tie-breaking by smallest start index falls out naturally if you update the answer only when a window is shorter, or equally short but earlier.

## 🧩 Algorithm Walkthrough
1. **Build the requirement map.**  
   Store `need[speakerId] = minCount`. This isolates the constrained speakers and avoids tracking irrelevant IDs. Also compute `requiredKinds = need.size()`.

2. **Initialize sliding-window state.**  
   Use two pointers: `left = 0`, iterate `right` from `0` to `n - 1`. Maintain `have[speakerId]` for required speakers only, and `formed`, the number of speakers whose current count has reached their quota.  
   Invariant: `formed` equals the number of requirement keys currently satisfied in the window.

3. **Expand the window with `right`.**  
   When `speakers[right]` is required, increment `have[id]`. If `have[id]` becomes exactly `need[id]`, increment `formed`.  
   Why exact equality? Because a requirement should count as newly satisfied once, not on every excess occurrence.

4. **Shrink while valid.**  
   When `formed == requiredKinds`, the current window satisfies all quotas. Record it if it is shorter than the best seen, or equal length with smaller `left`. Then try to move `left` rightward.  
   If `speakers[left]` is required, decrement its count. If it drops from `need[id]` to `need[id] - 1`, decrement `formed`.  
   Invariant: after each shrink step, the window is either still valid and minimality can be improved further, or it becomes invalid and expansion must resume.

5. **Return the best answer.**  
   If no valid window was ever found, return `[-1, -1]`; otherwise return the recorded indices.

This is the canonical **Two Pointers + Hash Map sliding window** pattern: dynamic frequency tracking over a contiguous range with monotonic pointer movement.

## 📊 Worked Example
Use `speakers = [4,2,7,2,4,2,9,7,4]`, `requirements = [[2,2],[4,2],[7,1]]`.

Need: `{2:2, 4:2, 7:1}`, `requiredKinds = 3`

| right | speaker | window `[left..right]` | relevant counts `(2,4,7)` | formed | action |
|---|---:|---|---|---:|---|
| 0 | 4 | `[0..0]` | `(0,1,0)` | 0 | expand |
| 1 | 2 | `[0..1]` | `(1,1,0)` | 0 | expand |
| 2 | 7 | `[0..2]` | `(1,1,1)` | 1 | expand |
| 3 | 2 | `[0..3]` | `(2,1,1)` | 2 | expand |
| 4 | 4 | `[0..4]` | `(2,2,1)` | 3 | valid → best = `[0,4]` |
| 5 | 2 | `[0..5]` | `(3,2,1)` | 3 | shrink? removing `4` breaks quota later |
| 6 | 9 | irrelevant | `(3,2,1)` | 3 | shrink attempts still cannot beat length 5 |
| 7 | 7 | extra `7` | `(3,2,2)` | 3 | continue |
| 8 | 4 | extra `4` | `(3,3,2)` | 3 | continue |

The first valid window `[0,4]` has length 5, and no shorter valid span appears later.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + r)`, where `n = speakers.length` and `r = requirements.length`. Building the requirement map costs `O(r)`. The sliding window is `O(n)` because each pointer moves forward at most once across the array. At `10^6` elements this is practical; at `10^9`, even linear scans become infrastructure decisions rather than algorithmic details.

### Space Complexity
`O(r)` auxiliary space for the requirement map and current window counts over required speakers only. This is already close to minimal for exact quota tracking. You could compress IDs, but that changes constants, not asymptotics, and adds preprocessing complexity.

## 💡 Key Takeaways
- If the problem asks for the **shortest contiguous range** satisfying frequency thresholds, that is a strong sliding-window signal.
- If only a subset of high-cardinality IDs matters, track counts for constrained keys only; that usually turns an impossible scan into a linear one.
- Increment the “satisfied requirements” counter only when a count reaches the quota exactly; excess occurrences must not double-count.
- When shrinking, the invalidation boundary is precise: dropping from `need[id]` to `need[id] - 1` means the window just became invalid.
- The production-grade insight is to maintain incremental state over the active window instead of recomputing aggregate predicates for every candidate interval.

## 🚀 Variations & Further Practice
- Require **exact** counts instead of minimum counts. The monotonicity weakens, so shrinking logic becomes stricter and some standard sliding-window shortcuts no longer apply cleanly.
- Add per-utterance weights or timestamps and ask for the minimum-**cost** valid span instead of minimum length. Now the optimization target differs from pointer distance.
- Process the transcript as an unbounded stream and continuously emit the best valid suffix/window so far. The harder part is online state management and eviction under memory constraints.