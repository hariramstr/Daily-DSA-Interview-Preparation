# Minimum Cost to Assemble a Playlist with Genre Switch Fees

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, sequence-dp, optimization

---

## 🗂 Problem Overview
Given songs in fixed catalog order, choose exactly `k` of them to minimize total playlist cost. Each chosen song contributes `duration[i] - enjoy[i]`, and each genre change between consecutive chosen songs adds switch fee `s`. You may skip songs, but chosen songs must preserve original order. Return the minimum achievable cost, or `-1` if selecting exactly `k` songs is impossible. The non-trivial part is that transition cost depends on the last chosen genre, not just the current song.

## 🌍 Engineering Impact
This pattern shows up in sequence optimization problems where local decisions create stateful transition costs: ad ranking with category fatigue penalties, media recommendation with diversity costs, compiler instruction selection with register-switch penalties, and streaming pipelines where stage changes incur setup overhead. At scale, greedy selection fails because the cheapest next item can make future transitions expensive. Dynamic programming gives a controlled way to preserve order, enforce exact cardinality, and model “cost of switching context” explicitly. That matters when product constraints require globally optimal sequences rather than heuristics that drift under skewed catalogs or long-tail distributions.

## 🔍 Problem Statement
You are given three arrays of length `n`: `duration[i]`, `enjoy[i]`, and `genre[i]`, plus integers `k` and switch fee `s`. Select exactly `k` songs in the given order; skipping is allowed, reordering is not.

The playlist cost is:

- sum of durations of selected songs
- plus `s` for each genre change between consecutive selected songs
- minus sum of enjoyment scores of selected songs

Equivalently, each selected song has base cost `duration[i] - enjoy[i]`, and adjacent selected songs may add a switch penalty.

Return the minimum total cost, or `-1` if no valid playlist of size `k` exists.

Constraints:

- `1 <= n <= 200`
- `1 <= k <= n`
- `1 <= duration[i] <= 10^4`
- `0 <= enjoy[i] <= 10^4`
- `1 <= genre[i] <= 50`
- `0 <= s <= 10^4`

Examples:

- Example 1: `duration=[4,6,3,5], enjoy=[2,5,1,4], genre=[1,2,2,1], k=2, s=3`
- Example 2: `duration=[7,2,6,3,4], enjoy=[1,3,2,5,1], genre=[1,1,3,3,2], k=3, s=4`

The key constraint is that order matters and the transition cost depends on the last chosen genre, which rules out simple greedy or subset DP.

## 🪜 How to Solve This
1. Read the cost formula → split it into two parts: per-song base cost and per-transition switch cost. That immediately suggests a sequential decision process.

2. Notice that skipping songs does not matter directly; only the last chosen song’s genre matters for the next transition. So the full history is unnecessary.

3. We need **exactly `k` picks**, not “up to `k`” and not unconstrained minimum cost. That is a strong signal for DP indexed by count selected.

4. Ask what state is sufficient:
   - how many songs have been processed,
   - how many have been chosen,
   - what the last chosen genre was.

5. Since `genre[i] <= 50`, tracking last genre is cheap. That makes a DP over `(chosen_count, last_genre)` practical.

6. For each song, there are only two actions:
   - skip it → state unchanged
   - take it → add base cost, and maybe add `s` if its genre differs from the previous chosen genre

7. The first chosen song is special: it has no previous song, so no switch fee applies. Handle that explicitly in the transition.

This is classic sequence DP: ordered items, exact selection count, and transition cost depending on previous selected state.

## 🧩 Algorithm Walkthrough
1. **Normalize the per-song contribution.**  
   Compute `base[i] = duration[i] - enjoy[i]`. Then total playlist cost becomes the sum of chosen `base[i]` values plus switch fees. This separates item cost from transition cost and simplifies reasoning.

2. **Define the DP state.**  
   Let `dp[c][g]` be the minimum cost after processing some prefix of songs, having chosen exactly `c` songs, where the last chosen song has genre `g`. Also keep a sentinel state for “no song chosen yet” when `c = 0`.  
   Invariant: every reachable state represents the best cost among all valid subsequences of the processed prefix with that `(count, last_genre)` signature.

3. **Initialize.**  
   Set all states to infinity. Set the empty state cost to `0`. This encodes that before choosing any song, cost is zero and there is no last genre.

4. **Process songs left to right.**  
   For each song `i`, create a fresh next-DP initialized from current DP to represent the skip option. This preserves all existing best subsequences.

5. **Apply the take transition.**  
   For every reachable state `(c, g)`:
   - if `c == 0`, taking song `i` creates state `(1, genre[i])` with added cost `base[i]`
   - otherwise, transition to `(c+1, genre[i])` with added cost `base[i] + (g != genre[i] ? s : 0)`

   This is correct because switch cost depends only on the previous chosen genre, which the state already stores.

6. **Bound the count dimension.**  
   Only process `c` up to `k-1` for take transitions. This keeps the DP compact and aligned with the exact-cardinality requirement.

7. **Extract the answer.**  
   After all songs are processed, the answer is the minimum `dp[k][g]` over all genres `g`. If all are unreachable, return `-1`.

This is **sequence dynamic programming** with a compressed “last selected attribute” state. It is the right abstraction because order is fixed, choices are binary, and transition cost is stateful but low-cardinality.

## 📊 Worked Example
Take `duration=[7,2,6,3,4]`, `enjoy=[1,3,2,5,1]`, `genre=[1,1,3,3,2]`, `k=3`, `s=4`.

`base = [6,-1,4,-2,3]`

| Song | Genre | Base | Key DP updates |
|---|---:|---:|---|
| start | - | - | `dp[0][none] = 0` |
| 1 | 1 | 6 | take → `dp[1][1] = 6` |
| 2 | 1 | -1 | from `dp[0]`: `dp[1][1] = -1`; from `dp[1][1]=6`: `dp[2][1] = 5` |
| 3 | 3 | 4 | from `dp[1][1]=-1`: `dp[2][3] = 7` (`+4+4` switch); from `dp[2][1]=5`: `dp[3][3] = 13` |
| 4 | 3 | -2 | from `dp[2][1]=5`: `dp[3][3] = 7` (`+(-2)+4`); from `dp[2][3]=7`: `dp[3][3] = 5` |
| 5 | 2 | 3 | candidate `dp[3][2]` values are worse than `5` |

Best final state is `dp[3][3] = 5`, achieved by songs `2,3,4`: base sum `-1 + 4 - 2 = 1`, one genre switch `+4`, total `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * k * G)` where `G` is the number of possible genres, at most `50`. For each of `n` songs, we scan all selection counts up to `k` and all last-genre states. With `n <= 200`, this is comfortably small. At `10^6` or `10^9` items, this exact DP would be infeasible without stronger structure or approximation.

### Space Complexity
`O(k * G)` using rolling arrays over the song index, since each step depends only on the previous prefix. The space is owned by the DP table. You could keep full `O(n * k * G)` state for reconstruction, but that trades memory for easier path recovery.

## 💡 Key Takeaways
- If the input order is fixed and you must choose an exact number of items, think sequence DP before considering greedy or sorting.
- If the cost of taking an item depends only on a small summary of the previous chosen item, that summary is usually the DP state.
- The first selected song must not pay a switch fee; treating it like a normal transition is a common bug.
- Use a fresh next-layer DP per song; in-place updates can accidentally reuse the same song multiple times in one iteration.
- In production systems, this pattern is the clean way to model context-switch penalties without exploding state to the full decision history.

## 🚀 Variations & Further Practice
- Add a maximum total duration budget in addition to choosing exactly `k` songs. The twist is a second resource dimension, turning the problem into multi-constraint DP.
- Make switch fee depend on the ordered pair of genres, not a constant `s`. The harder part is that transitions become a full genre-to-genre cost matrix.
- Ask for the actual playlist, not just the minimum cost. The conceptual extension is storing parent pointers or reconstructing decisions from retained DP history.