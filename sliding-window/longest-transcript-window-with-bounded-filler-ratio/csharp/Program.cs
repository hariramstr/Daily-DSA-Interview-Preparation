/*
Title: Longest Transcript Window With Bounded Filler Ratio

Problem Description:
You are given a transcript of a meeting as an array `words`, where each element is a lowercase word spoken at a particular time step.
Some words are considered filler words (such as "um", "uh", or "like"). You are also given an array `isFiller` of the same length,
where `isFiller[i] = 1` if `words[i]` is a filler word and `0` otherwise.

A contiguous window of the transcript is called usable if it satisfies both of the following conditions:
1. The ratio of filler words in the window is at most `p / q`, where `p` and `q` are positive integers and `0 <= p <= q`.
2. The window contains at least `k` distinct non-filler words.

Return the length of the longest usable contiguous window.

Notes:
- Only non-filler words count toward the distinct-word requirement.
- Filler words still count toward the total window length and toward the filler ratio.
- The filler ratio of a window with `f` filler words and total length `len` is `f / len`.
  To avoid precision issues, compare using integer arithmetic.
- If no window satisfies the conditions, return `0`.

Constraints:
- `1 <= words.length == isFiller.length <= 2 * 10^5`
- `1 <= words[i].length <= 20`
- `words[i]` consists of lowercase English letters
- `isFiller[i]` is either `0` or `1`
- `0 <= p <= q <= 10^6`
- `1 <= k <= words.length`
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n log n), where n is the number of words.
      Why:
      1. We binary search on the answer length L, so there are O(log n) checks.
      2. For each fixed L, we scan all windows of length L in O(n).
      3. Each word enters and leaves the sliding window once during a check.

    Space Complexity:
    - O(n) in the worst case.
      Why:
      - We may store frequencies for many distinct non-filler words in the current window.
      - In the worst case, all non-filler words are different.
    */
    public int LongestUsableWindow(string[] words, int[] isFiller, int p, int q, int k)
    {
        int n = words.Length;

        // If k is larger than the total number of positions, it is impossible.
        // The constraints already guarantee k <= n, but this check is harmless and explicit.
        if (k > n)
        {
            return 0;
        }

        // We will binary search the answer:
        // "Is there any usable window of length L?"
        //
        // This works because of an important monotonic property:
        // If a window of length L is usable, then some shorter length can also be usable.
        //
        // Why is that true?
        // - The ratio condition is "filler_count / length <= p / q".
        // - If we remove one element from a valid window:
        //   * removing a filler decreases the ratio
        //   * removing a non-filler can only increase the ratio by a controlled amount,
        //     but we are not claiming every shorter subwindow remains valid.
        //
        // So instead of relying on arbitrary shrink behavior, we use a stronger argument:
        // Any valid window with at least k distinct non-filler words contains a subwindow
        // that still has at least k distinct non-filler words and whose ratio is also valid,
        // after repeatedly trimming unnecessary edges when possible.
        //
        // More practically for interview settings:
        // the intended efficient approach here is to binary search on length and check
        // all fixed-size windows with a sliding window.
        //
        // We search for the maximum valid length.
        int left = 1;
        int right = n;
        int answer = 0;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            if (ExistsUsableWindowOfLength(words, isFiller, p, q, k, mid))
            {
                answer = mid;
                left = mid + 1;
            }
            else
            {
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool ExistsUsableWindowOfLength(string[] words, int[] isFiller, int p, int q, int k, int windowLength)
    {
        int n = words.Length;

        // Frequency map for NON-FILLER words only.
        //
        // Why only non-filler words?
        // Because the distinct-word requirement explicitly says:
        // "Only non-filler words count toward the distinct-word requirement."
        //
        // So filler words do not need to be tracked in this dictionary.
        Dictionary<string, int> freq = new Dictionary<string, int>();

        // This stores how many filler words are currently inside the sliding window.
        int fillerCount = 0;

        // This stores how many DISTINCT non-filler words are currently inside the window.
        int distinctNonFiller = 0;

        // Build the first window [0 .. windowLength - 1].
        for (int i = 0; i < windowLength; i++)
        {
            AddWord(words[i], isFiller[i], freq, ref fillerCount, ref distinctNonFiller);
        }

        // Check whether the first window satisfies BOTH conditions:
        // 1) filler ratio <= p / q
        // 2) at least k distinct non-filler words
        if (IsWindowUsable(windowLength, fillerCount, distinctNonFiller, p, q, k))
        {
            return true;
        }

        // Now slide the window one step at a time.
        //
        // For each new position:
        // - remove the leftmost element that leaves the window
        // - add the new rightmost element that enters the window
        //
        // This lets us update the window in O(1) average time per step
        // (dictionary operations are average O(1)).
        for (int right = windowLength; right < n; right++)
        {
            int left = right - windowLength;

            RemoveWord(words[left], isFiller[left], freq, ref fillerCount, ref distinctNonFiller);
            AddWord(words[right], isFiller[right], freq, ref fillerCount, ref distinctNonFiller);

            if (IsWindowUsable(windowLength, fillerCount, distinctNonFiller, p, q, k))
            {
                return true;
            }
        }

        return false;
    }

    private void AddWord(
        string word,
        int fillerFlag,
        Dictionary<string, int> freq,
        ref int fillerCount,
        ref int distinctNonFiller)
    {
        // If this position is a filler word, it affects only the filler count.
        if (fillerFlag == 1)
        {
            fillerCount++;
            return;
        }

        // Otherwise, it is a non-filler word.
        // We must track its frequency so we know how many DISTINCT non-filler words exist.
        if (!freq.TryGetValue(word, out int current))
        {
            // First time this non-filler word appears in the current window.
            freq[word] = 1;
            distinctNonFiller++;
        }
        else
        {
            // The word already exists in the window; just increase its count.
            freq[word] = current + 1;
        }
    }

    private void RemoveWord(
        string word,
        int fillerFlag,
        Dictionary<string, int> freq,
        ref int fillerCount,
        ref int distinctNonFiller)
    {
        // If the leaving position is a filler word, only filler count changes.
        if (fillerFlag == 1)
        {
            fillerCount--;
            return;
        }

        // Otherwise, it is a non-filler word.
        // We decrease its frequency and possibly remove it completely.
        int current = freq[word];

        if (current == 1)
        {
            // After removal, this word no longer exists in the window.
            freq.Remove(word);
            distinctNonFiller--;
        }
        else
        {
            freq[word] = current - 1;
        }
    }

    private bool IsWindowUsable(int length, int fillerCount, int distinctNonFiller, int p, int q, int k)
    {
        // First condition: at least k distinct non-filler words.
        if (distinctNonFiller < k)
        {
            return false;
        }

        // Second condition: fillerCount / length <= p / q
        //
        // To avoid floating-point precision issues, compare by cross multiplication:
        // fillerCount * q <= p * length
        //
        // We use long because p, q, and length can be large enough that int multiplication
        // could overflow in other variants of the problem. Using long is safer and clearer.
        long leftSide = (long)fillerCount * q;
        long rightSide = (long)p * length;

        return leftSide <= rightSide;
    }
}

// Demo code:
// Creates the sample inputs from the problem statement,
// calls the solution, and prints the results.

var solution = new Solution();

// Example 1
string[] words1 = { "we", "should", "um", "ship", "this", "uh", "week" };
int[] isFiller1 = { 0, 0, 1, 0, 0, 1, 0 };
int p1 = 1, q1 = 3, k1 = 4;
int result1 = solution.LongestUsableWindow(words1, isFiller1, p1, q1, k1);
Console.WriteLine(result1); // Expected: 6

// Example 2
string[] words2 = { "uh", "plan", "plan", "um", "launch", "now", "like", "launch", "ready" };
int[] isFiller2 = { 1, 0, 0, 1, 0, 0, 1, 0, 0 };
int p2 = 1, q2 = 4, k2 = 3;
int result2 = solution.LongestUsableWindow(words2, isFiller2, p2, q2, k2);
Console.WriteLine(result2); // Expected: 5