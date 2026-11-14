/*
Title: Longest Auction Window With Limited Bidder Dominance
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given an array bids where bids[i] is the bidder ID that placed the i-th bid in a live auction feed, and an integer limit.
A contiguous window of bids is called fair if no single bidder appears more than limit times more than all other bidders combined
inside that window.

Formally, for a window of length L, if the maximum frequency of any bidder in the window is maxFreq, then the window is fair when:

    maxFreq <= (L - maxFreq) + limit

Your task is to return the length of the longest fair contiguous window.

This models a fraud-detection rule for auction systems: a bidder may be active, but cannot dominate the stream too heavily relative
to everyone else unless the configured tolerance limit allows it.

A valid solution is expected to run efficiently on large inputs. Since bidder IDs may be large integers, your algorithm should not
assume a small fixed value range.

Constraints:
- 1 <= bids.length <= 200000
- 1 <= bids[i] <= 10^9
- 0 <= limit <= bids.length
- The answer must be computed for a contiguous subarray

Example 1:
Input: bids = [4, 7, 4, 4, 8, 7, 4, 9], limit = 1
Output: 5

Example 2:
Input: bids = [5, 5, 5, 2, 3, 5, 2, 2, 2], limit = 0
Output: 6
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n log n), where n is the number of bids.

    Why:
    - Each bid enters the sliding window once and leaves once.
    - We update a frequency dictionary in O(1) average time.
    - We also maintain a sorted structure of "how many bidders currently have frequency f".
      Updating that structure takes O(log n) because we may add/remove frequencies from a SortedSet.
    - Therefore the total work across the whole array is O(n log n).

    Space Complexity:
    O(n)

    Why:
    - In the worst case, all bidder IDs are different, so the frequency dictionary can hold O(n) entries.
    - The frequency-count dictionary and sorted set can also together hold O(n) distinct frequencies.
    */
    public int LongestFairWindow(int[] bids, int limit)
    {
        // This dictionary stores the current frequency of each bidder ID inside the active sliding window.
        // Key   = bidder ID
        // Value = how many times that bidder appears in the current window
        //
        // We use Dictionary<int, int> because bidder IDs can be as large as 1e9,
        // so we cannot use a fixed-size array indexed by bidder ID.
        var freqByBidder = new Dictionary<int, int>();

        // This dictionary stores how many bidders currently have a given frequency.
        //
        // Example:
        // if frequencyCount[3] = 2, that means exactly two different bidders currently appear 3 times in the window.
        //
        // Why do we need this?
        // Because the fairness condition depends on the maximum frequency in the current window.
        // When the window grows or shrinks, bidder frequencies change.
        // We need an efficient way to know the current maximum frequency at all times.
        var bidderCountByFrequency = new Dictionary<int, int>();

        // This sorted set stores all frequencies that currently exist in the window.
        // Its largest element is the current maximum bidder frequency.
        //
        // Example:
        // if frequencies present are {1, 2, 4}, then max frequency is 4.
        //
        // We use SortedSet<int> so that:
        // - adding a frequency is efficient
        // - removing a frequency is efficient
        // - getting the maximum frequency is efficient via Max
        var activeFrequencies = new SortedSet<int>();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int best = 0;

        // We expand the right boundary one step at a time.
        for (int right = 0; right < bids.Length; right++)
        {
            int bidder = bids[right];

            // STEP 1: Add the new rightmost bid into the window.
            //
            // We first read the old frequency of this bidder.
            // If the bidder is not yet in the window, old frequency is 0.
            int oldFreq = freqByBidder.TryGetValue(bidder, out int existingFreq) ? existingFreq : 0;
            int newFreq = oldFreq + 1;

            // Update the bidder's frequency in the main frequency dictionary.
            freqByBidder[bidder] = newFreq;

            // Because this bidder's frequency changed from oldFreq -> newFreq,
            // we must also update our "how many bidders have each frequency" structure.
            //
            // First, remove one bidder from oldFreq, but only if oldFreq > 0.
            // Frequency 0 is not stored because it means "not present in the window".
            if (oldFreq > 0)
            {
                DecreaseFrequencyBucket(oldFreq, bidderCountByFrequency, activeFrequencies);
            }

            // Then add one bidder to newFreq.
            IncreaseFrequencyBucket(newFreq, bidderCountByFrequency, activeFrequencies);

            // STEP 2: Shrink the window from the left while it is not fair.
            //
            // The fairness rule is:
            // maxFreq <= (windowLength - maxFreq) + limit
            //
            // Rearranging:
            // 2 * maxFreq <= windowLength + limit
            //
            // As long as this is false, the current window is invalid,
            // so we must move the left pointer rightward until the window becomes valid again.
            while (!IsFair(left, right, limit, activeFrequencies))
            {
                int leftBidder = bids[left];

                // Read the current frequency of the bidder that is leaving the window.
                int leftOldFreq = freqByBidder[leftBidder];
                int leftNewFreq = leftOldFreq - 1;

                // Update the bidder's frequency-count bucket:
                // one bidder no longer has frequency leftOldFreq.
                DecreaseFrequencyBucket(leftOldFreq, bidderCountByFrequency, activeFrequencies);

                if (leftNewFreq == 0)
                {
                    // If the bidder frequency becomes zero, remove the bidder entirely from the window map.
                    freqByBidder.Remove(leftBidder);
                }
                else
                {
                    // Otherwise, store the reduced frequency and add one bidder to that new frequency bucket.
                    freqByBidder[leftBidder] = leftNewFreq;
                    IncreaseFrequencyBucket(leftNewFreq, bidderCountByFrequency, activeFrequencies);
                }

                // Finally move the left boundary forward by one.
                left++;
            }

            // STEP 3: At this point, the window [left..right] is guaranteed to be fair.
            // So we can safely use its length to update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }

    private static bool IsFair(int left, int right, int limit, SortedSet<int> activeFrequencies)
    {
        // The current window length.
        int length = right - left + 1;

        // If the window is non-empty, the maximum bidder frequency is simply the largest active frequency.
        // If somehow the set were empty (which should only correspond to an empty window),
        // we treat max frequency as 0.
        int maxFreq = activeFrequencies.Count == 0 ? 0 : activeFrequencies.Max;

        // Apply the exact fairness condition from the problem statement:
        // maxFreq <= (length - maxFreq) + limit
        //
        // This means:
        // the most frequent bidder cannot exceed all other bids combined by more than "limit".
        return maxFreq <= (length - maxFreq) + limit;
    }

    private static void IncreaseFrequencyBucket(
        int frequency,
        Dictionary<int, int> bidderCountByFrequency,
        SortedSet<int> activeFrequencies)
    {
        // We are saying:
        // "One more bidder now has this exact frequency."

        if (bidderCountByFrequency.TryGetValue(frequency, out int count))
        {
            bidderCountByFrequency[frequency] = count + 1;
        }
        else
        {
            bidderCountByFrequency[frequency] = 1;
        }

        // Make sure this frequency is marked as active.
        activeFrequencies.Add(frequency);
    }

    private static void DecreaseFrequencyBucket(
        int frequency,
        Dictionary<int, int> bidderCountByFrequency,
        SortedSet<int> activeFrequencies)
    {
        // We are saying:
        // "One bidder no longer has this exact frequency."

        int count = bidderCountByFrequency[frequency];

        if (count == 1)
        {
            // If this was the last bidder with that frequency,
            // remove the frequency bucket entirely.
            bidderCountByFrequency.Remove(frequency);

            // Also remove it from the sorted set of active frequencies,
            // because this frequency no longer exists in the current window.
            activeFrequencies.Remove(frequency);
        }
        else
        {
            bidderCountByFrequency[frequency] = count - 1;
        }
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] bids1 = { 4, 7, 4, 4, 8, 7, 4, 9 };
int limit1 = 1;
int result1 = solution.LongestFairWindow(bids1, limit1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] bids2 = { 5, 5, 5, 2, 3, 5, 2, 2, 2 };
int limit2 = 0;
int result2 = solution.LongestFairWindow(bids2, limit2);
Console.WriteLine(result2); // Expected: 6

// Additional quick sanity checks
int[] bids3 = { 1 };
int limit3 = 0;
Console.WriteLine(solution.LongestFairWindow(bids3, limit3)); // Expected: 1

int[] bids4 = { 1, 1, 1, 1 };
int limit4 = 0;
Console.WriteLine(solution.LongestFairWindow(bids4, limit4)); // Expected: 1

int[] bids5 = { 1, 2, 1, 2, 1, 2 };
int limit5 = 0;
Console.WriteLine(solution.LongestFairWindow(bids5, limit5)); // Expected: 6