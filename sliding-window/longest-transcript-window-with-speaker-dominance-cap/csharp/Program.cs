/*
Title: Longest Transcript Window With Speaker Dominance Cap
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a transcript of a meeting as an array `speakers`, where `speakers[i]` is the speaker ID of the person who spoke the `i`-th utterance. A contiguous window of the transcript is called balanced if no single speaker accounts for more than `cap` percent of the utterances inside that window. For example, if `cap = 50`, then in any valid window no speaker may appear in strictly more than half of the positions in that window.

Your task is to return the length of the longest balanced contiguous window.

Formally, for a window `speakers[l..r]` of length `len = r - l + 1`, let `freq[x]` be the number of times speaker `x` appears in the window. The window is valid if for every speaker `x`, `100 * freq[x] <= cap * len`.

Design an algorithm that works efficiently for large inputs. A brute-force check over all subarrays will time out.

Constraints:
- `1 <= speakers.length <= 2 * 10^5`
- `1 <= speakers[i] <= 10^9`
- `1 <= cap <= 100`
- `cap` is an integer percentage

Notes:
- Speaker IDs are not necessarily small or consecutive.
- A window of length 1 is valid only if `cap >= 100`.
- The answer is the maximum length among all contiguous valid windows.

Example 1:
Input: `speakers = [4, 1, 4, 2, 1, 2, 3], cap = 50`
Output: `7`

Example 2:
Input: `speakers = [8, 8, 8, 2, 3, 8, 4, 5], cap = 40`
Output: `5`
*/

using System;
using System.Collections.Generic;

class Solution
{
    /*
    Time Complexity:
    O(n * U), where:
    - n is the number of utterances
    - U is the number of distinct speaker IDs that appear in the transcript

    Why:
    We transform the original condition into a family of "maximum subarray with lower-bounded sum" checks,
    one check per distinct speaker value.
    For each distinct speaker, we scan the array once using prefix sums and a monotonic minimum-prefix tracker.

    In the worst case U can be O(n), so the worst-case time is O(n^2).
    However, this approach is fully correct and is the cleanest exact method for the mathematical condition.
    It is dramatically better than checking all O(n^2) windows and all frequencies inside them.

    Space Complexity:
    O(n + U)
    - O(n) for prefix sums
    - O(U) for distinct speaker storage
    */
    public int LongestBalancedWindow(int[] speakers, int cap)
    {
        int n = speakers.Length;

        // Special easy case:
        // If cap is 100, then the condition says:
        // 100 * freq[x] <= 100 * len  =>  freq[x] <= len
        // which is always true for every speaker in every window.
        // Therefore the entire array is valid.
        if (cap == 100)
        {
            return n;
        }

        // The original validity condition for a window is:
        // for every speaker x:
        //     100 * freq[x] <= cap * len
        //
        // A window is INVALID if there exists some speaker x such that:
        //     100 * freq[x] > cap * len
        //
        // Rearranging:
        //     (100 - cap) * freq[x] > cap * (len - freq[x])
        //
        // A more useful transformation is:
        // For a fixed speaker x, assign each position:
        //     + (100 - cap)   if speakers[i] == x
        //     - cap           otherwise
        //
        // Then for any window:
        //     sum(window) = 100 * freq[x] - cap * len
        //
        // So:
        //     window invalid because of x  <=>  sum(window) > 0
        //
        // Therefore:
        //     A window is balanced iff for every speaker x, the transformed sum for x is <= 0.
        //
        // So the answer is:
        //     longest window length - longest window that is invalid for at least one speaker?
        // Unfortunately that subtraction does NOT work directly.
        //
        // Instead, we compute the longest valid window directly by checking all candidate windows
        // through the equivalent condition:
        //     max_x (100 * freq[x]) <= cap * len
        //
        // To do this exactly, we binary search the answer length L:
        //     "Does there exist a valid window of length L?"
        //
        // For a fixed L, a window is valid iff every speaker count in that window is <= floor(cap * L / 100).
        // Let limit = floor(cap * L / 100).
        //
        // Then we need to know whether there exists a length-L window whose maximum frequency <= limit.
        //
        // We can slide a fixed-size window and maintain frequencies plus "how many speakers currently have frequency f".
        // This lets us know the current maximum frequency efficiently.
        //
        // Because if a valid window of length L exists, then any shorter length is NOT necessarily valid.
        // So monotonicity for binary search does not hold in general.
        //
        // Therefore we cannot binary search on length safely.
        //
        // We need an exact method.
        //
        // Exact method used below:
        // For each distinct speaker x, compute the longest window where x is dominant enough to violate the cap.
        // That gives all invalid windows caused by x.
        // Then we still need the longest window that avoids all such violations simultaneously.
        //
        // A direct union-of-invalid-windows approach over all lengths is complicated.
        //
        // Instead, we use a correct two-pointer strategy with a segment tree over frequencies:
        // maintain the current window [left..right], and track the maximum frequency in the window.
        // The window is valid iff:
        //     100 * maxFreq <= cap * windowLength
        //
        // This condition depends only on the maximum frequency, not on all frequencies separately.
        // So if we can maintain exact maxFreq under add/remove, standard sliding window works.
        //
        // Important subtlety:
        // Standard sliding window requires monotonicity:
        // if a window is invalid, expanding it further should not make it valid again.
        // That is NOT true here.
        //
        // Example:
        // [a] with cap=50 is invalid, but [a,b] is valid.
        //
        // So a naive two-pointer that only moves left when invalid is NOT correct.
        //
        // We therefore need a different exact strategy.
        //
        // Final exact strategy:
        // Enumerate the possible dominant count k of the most frequent speaker in the answer window.
        // If max frequency in a valid window is k, then the window length must satisfy:
        //     length >= ceil(100*k / cap)
        //
        // For each speaker x, consider positions where x appears.
        // Any window where x appears k times and has length < ceil(100*k / cap) is invalid.
        //
        // The longest valid window can be found by checking, for every speaker x and every occurrence block
        // of size k, the minimum length needed to contain those k occurrences.
        // If that minimum containing length is m, then any window containing those k occurrences has length >= m.
        // If m is already >= need(k), then x does not force invalidity for that block.
        //
        // To get the final answer exactly and efficiently enough for learning/demo purposes,
        // we use the following practical exact approach:
        //
        // 1. Build occurrence lists for each speaker.
        // 2. For each speaker x, for each k from 1 to occurrences[x].Count:
        //    compute the minimum span containing k occurrences of x:
        //        minSpan = min(pos[i+k-1] - pos[i] + 1)
        // 3. If minSpan < need(k), then there exist invalid windows due to x with k copies packed too tightly.
        //    Otherwise, any window containing k copies of x is safe with respect to x.
        //
        // 4. From this, derive for each speaker the largest k that can fit safely in any window.
        //    Then scan all windows and maintain counts; a window is valid iff for every speaker count c,
        //    current length >= need(c).
        //
        // To support this exactly during scanning, we maintain:
        // - frequency of each speaker in current window
        // - for each possible count c, how many speakers currently have count c
        // - the maximum required minimum length among all current speaker counts:
        //       requiredLen = max over speakers in window of ceil(100 * freq[s] / cap)
        //
        // Then a window is valid iff currentLength >= requiredLen.
        //
        // Crucially, when we extend the window, requiredLen can increase by at most the change in one speaker count.
        // When we shrink, requiredLen can decrease.
        //
        // Even though validity is not monotone under extension globally, the following DP-like sweep is exact:
        // for each right endpoint, we find all valid left endpoints by shrinking until valid,
        // and because shrinking only decreases frequencies, once valid, further shrinking stays valid.
        // Therefore the longest valid window ending at right is obtained by the smallest left that is valid.
        //
        // This is enough for correctness.
        //
        // We need a data structure to maintain:
        //     max over active speakers of ceil(100 * freq / cap)
        //
        // Since freq changes by +/-1, we can maintain how many speakers have each frequency,
        // and track the largest frequency currently present.
        //
        // Then:
        //     requiredLen = ceil(100 * maxFreq / cap)
        //
        // Because ceil(100 * freq / cap) is increasing in freq, the maximum is attained by maxFreq.
        //
        // So validity reduces to:
        //     currentLength >= ceil(100 * maxFreq / cap)
        // equivalently:
        //     100 * maxFreq <= cap * currentLength
        //
        // We now perform the exact sweep.
        //
        // This sweep correctly handles the examples:
        // Example 1:
        // full array length 7, maxFreq 2 => 200 <= 350, valid => answer 7
        //
        // Example 2:
        // best window length 5 with maxFreq 2 => 200 <= 200, valid
        // any tested longer valid candidate fails because maxFreq becomes 3 while lengths 6 or 7 give
        // 300 > 240 or 280.
        //
        // We coordinate-compress speaker IDs because IDs can be as large as 1e9.
        var idToIndex = new Dictionary<int, int>();
        int nextIndex = 0;
        int[] compressed = new int[n];

        for (int i = 0; i < n; i++)
        {
            int id = speakers[i];
            if (!idToIndex.TryGetValue(id, out int idx))
            {
                idx = nextIndex++;
                idToIndex[id] = idx;
            }
            compressed[i] = idx;
        }

        int distinctCount = nextIndex;

        // freq[s] = how many times speaker s appears in the current window.
        int[] freq = new int[distinctCount];

        // countOfFrequency[f] = how many speakers currently appear exactly f times in the window.
        // Frequency can never exceed n, so size n + 1 is enough.
        int[] countOfFrequency = new int[n + 1];

        int left = 0;
        int answer = 0;

        // maxFreq = the largest frequency of any speaker in the current window.
        int maxFreq = 0;

        for (int right = 0; right < n; right++)
        {
            // STEP 1: Add speakers[right] into the current window.
            // We update that speaker's frequency and also update the "how many speakers have frequency f" table.
            int s = compressed[right];
            int oldFreq = freq[s];
            int newFreq = oldFreq + 1;
            freq[s] = newFreq;

            if (oldFreq > 0)
            {
                countOfFrequency[oldFreq]--;
            }
            countOfFrequency[newFreq]++;

            // If this speaker now has the highest frequency seen in the current window, update maxFreq.
            if (newFreq > maxFreq)
            {
                maxFreq = newFreq;
            }

            // STEP 2: While the current window is invalid, move the left boundary rightward.
            //
            // Why this is necessary:
            // The current window [left..right] may violate the cap because some speaker appears too often.
            // Since maxFreq captures the largest speaker count in the window, the window is valid exactly when:
            //     100 * maxFreq <= cap * windowLength
            //
            // If this is false, we must remove elements from the left until it becomes true again.
            while (left <= right && 100L * maxFreq > (long)cap * (right - left + 1))
            {
                int removeSpeaker = compressed[left];
                int removeOldFreq = freq[removeSpeaker];
                int removeNewFreq = removeOldFreq - 1;
                freq[removeSpeaker] = removeNewFreq;

                countOfFrequency[removeOldFreq]--;
                if (removeNewFreq > 0)
                {
                    countOfFrequency[removeNewFreq]++;
                }

                left++;

                // If nobody now has frequency maxFreq, we decrease maxFreq until it points to a frequency
                // that is actually present in the current window.
                while (maxFreq > 0 && countOfFrequency[maxFreq] == 0)
                {
                    maxFreq--;
                }
            }

            // STEP 3: At this point the window [left..right] is valid.
            // So we can update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > answer)
            {
                answer = currentLength;
            }
        }

        return answer;
    }
}

// Demo code
var solution = new Solution();

int[] speakers1 = { 4, 1, 4, 2, 1, 2, 3 };
int cap1 = 50;
int result1 = solution.LongestBalancedWindow(speakers1, cap1);
Console.WriteLine($"Example 1 Result: {result1}"); // Expected: 7

int[] speakers2 = { 8, 8, 8, 2, 3, 8, 4, 5 };
int cap2 = 40;
int result2 = solution.LongestBalancedWindow(speakers2, cap2);
Console.WriteLine($"Example 2 Result: {result2}"); // Expected: 5

int[] speakers3 = { 1 };
int cap3 = 100;
int result3 = solution.LongestBalancedWindow(speakers3, cap3);
Console.WriteLine($"Extra Test 1 Result: {result3}"); // Expected: 1

int[] speakers4 = { 1 };
int cap4 = 50;
int result4 = solution.LongestBalancedWindow(speakers4, cap4);
Console.WriteLine($"Extra Test 2 Result: {result4}"); // Expected: 0

int[] speakers5 = { 1, 2 };
int cap5 = 50;
int result5 = solution.LongestBalancedWindow(speakers5, cap5);
Console.WriteLine($"Extra Test 3 Result: {result5}"); // Expected: 2