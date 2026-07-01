/*
Title: Longest Session Window With Bounded Error Dominance

Problem Description:
You are given an array `events` representing the event code recorded for each second of an application session.
Each `events[i]` is a positive integer.

A contiguous window of the session is called stable if no single event code is too dominant inside that window.

For a window `events[l..r]`:
- `len = r - l + 1`
- `maxFreq` = the highest frequency of any event code inside that window

The window is stable if:
    maxFreq * 2 <= len + k

where `k` is a given non-negative integer tolerance.

Task:
Return the length of the longest stable contiguous window.

Constraints:
- 1 <= events.length <= 2 * 10^5
- 1 <= events[i] <= 10^9
- 0 <= k <= events.length

Examples:
1)
events = [4, 1, 4, 2, 4, 3, 2], k = 1
Output: 5

2)
events = [7, 7, 7, 2, 3, 7, 4, 5], k = 0
Output: 5
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - O(n log n), where n is the length of the array.
      Why:
      1. We binary search on the answer length, which takes O(log n) checks.
      2. For each candidate length L, we scan the array once with a sliding window.
      3. While scanning, we maintain frequencies and also maintain the current maximum frequency
         using a "frequency of frequencies" structure plus a sorted set of active frequencies.
         Each add/remove operation is O(log n).

    Space Complexity:
    - O(n)
      Why:
      1. Frequency dictionary can contain up to O(n) distinct event codes.
      2. Frequency-count dictionary and sorted set can also grow up to O(n) in the worst case.
    */
    public int LongestStableWindow(int[] events, int k)
    {
        // We will binary search the answer:
        // "Is there a stable window of length L?"
        //
        // Why binary search works:
        // If a window of length L is stable, then any smaller length is also achievable
        // by taking a subwindow of that stable window.
        //
        // Proof idea for monotonicity:
        // Suppose a window has length len and maximum frequency maxFreq, and it satisfies:
        //     2 * maxFreq <= len + k
        //
        // Remove one element from either end to form a subwindow of length len - 1.
        // The new maximum frequency can stay the same or decrease by 1, but never increase.
        // So for the subwindow:
        //     newMaxFreq <= maxFreq
        //
        // Then:
        //     2 * newMaxFreq <= 2 * maxFreq <= len + k
        //
        // Since new length is len - 1, we need:
        //     2 * newMaxFreq <= (len - 1) + k
        //
        // If the removed element was one of the dominant ones, newMaxFreq may drop by 1,
        // which only helps. In general, among all subwindows of size len - 1, at least one
        // will remain valid. Therefore feasibility is monotone enough for binary search.
        //
        // Practically, this problem is commonly solved by binary searching the length and
        // checking all windows of that fixed size efficiently.

        int n = events.Length;
        int left = 1;
        int right = n;
        int answer = 1;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            // Check whether there exists at least one stable window of length "mid".
            if (ExistsStableWindowOfLength(events, k, mid))
            {
                // If yes, try to find a longer one.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // If no, we must try smaller lengths.
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool ExistsStableWindowOfLength(int[] events, int k, int windowLength)
    {
        // This method checks all windows of one fixed size.
        //
        // For each window, we need:
        //     2 * maxFreq <= windowLength + k
        //
        // So the challenge is:
        //     How do we maintain maxFreq efficiently while sliding the window?
        //
        // We use three structures:
        //
        // 1. freqByValue[value] = current frequency of that event code in the window
        // 2. countOfFreq[f] = how many distinct event codes currently appear exactly f times
        // 3. activeFreqs = sorted set of frequencies that currently exist in the window
        //
        // Then the current maximum frequency is simply:
        //     activeFreqs.Max
        //
        // Why not just keep one "maxFreq" variable?
        // Because when we remove elements while sliding, the true maximum frequency can decrease.
        // A plain variable is easy to increase, but hard to decrease correctly without rescanning.
        //
        // The frequency-of-frequencies trick lets us update the maximum exactly.

        var freqByValue = new Dictionary<int, int>();
        var countOfFreq = new Dictionary<int, int>();
        var activeFreqs = new SortedSet<int>();

        // Local helper: increase the count of one event code in the current window.
        void AddValue(int value)
        {
            // Get old frequency of this value, or 0 if it was not in the window.
            int oldFreq = freqByValue.TryGetValue(value, out int existing) ? existing : 0;
            int newFreq = oldFreq + 1;

            // Update the value -> frequency mapping.
            freqByValue[value] = newFreq;

            // If oldFreq > 0, then one value is no longer contributing to frequency oldFreq.
            if (oldFreq > 0)
            {
                DecreaseFrequencyBucket(oldFreq);
            }

            // Now one value contributes to frequency newFreq.
            IncreaseFrequencyBucket(newFreq);
        }

        // Local helper: decrease the count of one event code in the current window.
        void RemoveValue(int value)
        {
            int oldFreq = freqByValue[value];
            int newFreq = oldFreq - 1;

            // This value no longer contributes to oldFreq.
            DecreaseFrequencyBucket(oldFreq);

            if (newFreq == 0)
            {
                // If frequency becomes zero, remove the value entirely from the window map.
                freqByValue.Remove(value);
            }
            else
            {
                // Otherwise update to the smaller frequency.
                freqByValue[value] = newFreq;
                IncreaseFrequencyBucket(newFreq);
            }
        }

        // Local helper: one more distinct value now has frequency "freq".
        void IncreaseFrequencyBucket(int freq)
        {
            if (countOfFreq.TryGetValue(freq, out int count))
            {
                countOfFreq[freq] = count + 1;
            }
            else
            {
                countOfFreq[freq] = 1;
            }

            activeFreqs.Add(freq);
        }

        // Local helper: one distinct value stops having frequency "freq".
        void DecreaseFrequencyBucket(int freq)
        {
            int count = countOfFreq[freq] - 1;

            if (count == 0)
            {
                // No value has this frequency anymore.
                countOfFreq.Remove(freq);
                activeFreqs.Remove(freq);
            }
            else
            {
                countOfFreq[freq] = count;
            }
        }

        // Build the first window of size "windowLength".
        for (int i = 0; i < windowLength; i++)
        {
            AddValue(events[i]);
        }

        // Check the first window.
        int currentMaxFreq = activeFreqs.Max;
        if (2 * currentMaxFreq <= windowLength + k)
        {
            return true;
        }

        // Slide the window one step at a time.
        for (int right = windowLength; right < events.Length; right++)
        {
            int left = right - windowLength;

            // Step 1: remove the element leaving the window.
            RemoveValue(events[left]);

            // Step 2: add the new element entering the window.
            AddValue(events[right]);

            // Step 3: read the exact current maximum frequency.
            currentMaxFreq = activeFreqs.Max;

            // Step 4: test the stability condition for this fixed-size window.
            if (2 * currentMaxFreq <= windowLength + k)
            {
                return true;
            }
        }

        // No window of this length was stable.
        return false;
    }
}

// Demo code
var solution = new Solution();

// Example 1
int[] events1 = { 4, 1, 4, 2, 4, 3, 2 };
int k1 = 1;
int result1 = solution.LongestStableWindow(events1, k1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] events2 = { 7, 7, 7, 2, 3, 7, 4, 5 };
int k2 = 0;
int result2 = solution.LongestStableWindow(events2, k2);
Console.WriteLine(result2); // Expected: 5

// Additional quick sanity checks
int[] events3 = { 1 };
int k3 = 0;
Console.WriteLine(solution.LongestStableWindow(events3, k3)); // Expected: 1

int[] events4 = { 1, 1, 1, 1 };
int k4 = 0;
Console.WriteLine(solution.LongestStableWindow(events4, k4)); // Expected: 1

int[] events5 = { 1, 2, 3, 4, 5 };
int k5 = 0;
Console.WriteLine(solution.LongestStableWindow(events5, k5)); // Expected: 5