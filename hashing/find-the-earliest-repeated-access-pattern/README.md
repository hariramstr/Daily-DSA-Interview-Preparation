# Find the Earliest Repeated Access Pattern

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Rolling Hash, Arrays

---

## 🗂 Problem Overview
Given an array of room codes and an integer `windowSize`, examine every contiguous window of exactly that length and find the smallest starting index whose full sequence appears again later. Return that earliest index, or `-1` if no window repeats. The challenge is scale: there can be up to `100000` windows, so comparing every pair directly is too expensive. The intended solution uses hashing to represent windows efficiently and detect repeats in near-linear time.

## 🌍 Engineering Impact
This pattern shows up in security analytics, clickstream/session mining, fraud detection, compiler token-stream deduplication, and streaming observability pipelines. In each case, the system needs to detect repeated contiguous sequences, not just repeated individual events. Without a rolling or incremental hashing strategy, sequence comparison degrades into quadratic behavior or excessive memory churn from materializing many subarrays. At production scale, that means slower anomaly detection, higher GC pressure, and poor tail latency. Hash-based window fingerprints enable single-pass detection, bounded per-window work, and architectures that can operate on large logs or streams without collapsing under combinatorial comparisons.

## 🔍 Problem Statement
You are given `accessLog`, an array of lowercase strings, and an integer `windowSize`. For every contiguous subarray of length `windowSize`, define its access pattern as the ordered sequence of room codes in that window. Return the starting index of the earliest window whose exact pattern occurs again later in the array. If no such repeated window exists, return `-1`.

Two windows match only if they have the same length and identical strings at every position. Overlapping matches are valid.

Constraints:
- `1 <= accessLog.length <= 100000`
- `1 <= windowSize <= accessLog.length`
- `accessLog[i]` contains lowercase English letters
- Total characters across all room codes `<= 200000`

Examples:
- `["lab","hall","vault","lab","hall","vault","exit"]`, `windowSize = 3` → `0`
- `["a","b","a","b","c"]`, `windowSize = 2` → `0`

The key constraint is the input size: naive pairwise window comparison is too slow.

## 🪜 How to Solve This
1. Read the problem → we are not searching for one fixed pattern; we must compare **all length-`windowSize` windows against each other**.
2. Brute force means generating every window and comparing it with every later window → roughly `O(n^2 * windowSize)`, which is immediately disqualified at `100000` elements.
3. The repeated object is an **ordered sequence**, so we need a compact representation of each window that preserves order. That points to hashing.
4. A plain hash per window still sounds expensive if we rebuild it from scratch each time. Since adjacent windows overlap heavily, think **rolling hash**: remove the outgoing room code contribution, add the incoming one.
5. Room codes are strings, so first map each unique string to an integer ID. Then rolling hash operates over integers, not variable-length strings.
6. Scan windows left to right, compute each window hash, and store the earliest index where that hash appeared.
7. On seeing the same hash again, verify the windows if collision safety matters, then update the answer with the earliest stored index.
8. Because we want the **smallest starting index among all repeated windows**, keep scanning even after finding one.

## 🧩 Algorithm Walkthrough
1. **Normalize room codes to integer IDs.**  
   Build a map from each distinct string in `accessLog` to a compact integer. This makes hashing stable and cheap. The invariant is that equal room codes always map to equal IDs, and unequal codes map to different IDs.

2. **Model each window with a rolling hash.**  
   Use the **Rolling Hash** pattern over the ID array. For a window `[i, i + windowSize - 1]`, compute a polynomial-style hash. Precompute `base^(windowSize-1)` so the outgoing element can be removed when sliding. This avoids rebuilding each window hash from scratch.

3. **Initialize the first window hash.**  
   Compute the hash for indices `0..windowSize-1` and record it in a hash map as first seen at index `0`. The invariant: the map stores the earliest starting index for each observed window fingerprint.

4. **Slide one position at a time.**  
   For each next start index `i`, update the hash in O(1): remove the old leftmost ID, shift by multiplying with `base`, and add the new rightmost ID. This preserves the exact ordered sequence semantics.

5. **Detect repeats.**  
   If the current hash already exists, a repeated candidate window has been found. Because hashes can collide, compare the two windows element-by-element if using a single hash. If they match, update `answer = min(answer, firstSeenIndex)`.

6. **Preserve earliest occurrence only.**  
   If a hash is new, store its current index. If it already exists, do not overwrite the earlier index. That invariant is what makes the final answer the earliest repeated start.

7. **Return the result.**  
   After scanning all `n - windowSize + 1` windows, return the smallest repeated start index found, otherwise `-1`.

## 📊 Worked Example
Example: `accessLog = ["lab","hall","vault","lab","hall","vault","exit"]`, `windowSize = 3`

Map strings to IDs: `lab→1, hall→2, vault→3, exit→4`  
ID array: `[1,2,3,1,2,3,4]`

| Window Start | Window IDs | Seen Before? | First Seen Index | Answer |
|---|---|---:|---:|---:|
| 0 | `[1,2,3]` | No | store `0` | `-1` |
| 1 | `[2,3,1]` | No | store `1` | `-1` |
| 2 | `[3,1,2]` | No | store `2` | `-1` |
| 3 | `[1,2,3]` | Yes | `0` | `0` |
| 4 | `[2,3,4]` | No | store `4` | `0` |

Trace:
1. First window hash is inserted with index `0`.
2. Each slide updates the hash in constant time.
3. At start `3`, the fingerprint matches the one from start `0`.
4. Window contents also match exactly, so the earliest repeated start becomes `0`.
5. Continue scanning, but no smaller index can exist.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + C + V)` expected, where `n` is the number of room codes, `C` is total input characters for string-to-ID mapping, and `V` is any collision-verification cost. With good hashing or double hashing, this is effectively linear. At `10^6` scale this is practical; at `10^9`, even linear scans become infrastructure problems rather than algorithm problems.

### Space Complexity
`O(n)` in the worst case. The dominant structures are the string-to-ID map and the hash map of seen window fingerprints. Space can be reduced only by trading away exactness, for example with probabilistic fingerprints or external storage for very large streams.

## 💡 Key Takeaways
- If the problem asks for repeated **contiguous sequences** under large `n`, think rolling hash before considering nested comparisons.
- If adjacent candidates overlap heavily, that is a strong signal that incremental state reuse should replace recomputation.
- The number of windows is `n - windowSize + 1`; off-by-one errors usually come from iterating to `n - windowSize` instead.
- Do not overwrite the first index for a seen hash; the requirement is the earliest repeated starting position, not the latest duplicate pair.
- In production systems, sequence fingerprinting is a standard way to turn expensive structural equality checks into cheap streaming detection with bounded per-event work.

## 🚀 Variations & Further Practice
- Return **all** repeated window start indices, not just the earliest one. The twist is storing multiple occurrences per fingerprint while controlling memory and collision verification cost.
- Detect the **longest** repeated contiguous access pattern. The harder part is combining rolling hash with binary search on window length or suffix-based techniques.
- Process an unbounded event stream with expiration windows. The twist is maintaining rolling fingerprints and duplicate detection under time-based eviction rather than fixed finite arrays.