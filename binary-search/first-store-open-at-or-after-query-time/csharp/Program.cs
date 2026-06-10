/*
Title: First Store Open at or After Query Time
Difficulty: Easy
Topic: Binary Search

Problem Description:
You are given a sorted integer array openTimes where openTimes[i] represents the minute of the day
when the i-th store opens. The array is sorted in non-decreasing order, and multiple stores may
open at the same time. You are also given an integer queryTime, representing the time a customer arrives.

Your task is to return the index of the first store whose opening time is greater than or equal to
queryTime. If no such store exists, return -1.

This problem models a common search task in scheduling and availability systems: given a sorted list
of event times, quickly find the earliest event that satisfies a threshold condition. A linear scan works,
but the expected interview solution uses binary search to achieve logarithmic time complexity.

Implement a function that finds the leftmost valid index efficiently.

Constraints:
- 1 <= openTimes.length <= 10^5
- 0 <= openTimes[i] <= 10^9
- openTimes is sorted in non-decreasing order
- 0 <= queryTime <= 10^9

Example 1:
Input: openTimes = [120, 180, 240, 300], queryTime = 200
Output: 2
Explanation: The first opening time that is at least 200 is 240, which is at index 2.

Example 2:
Input: openTimes = [60, 60, 90, 150], queryTime = 60
Output: 0
Explanation: Multiple stores open at 60, and the first such index is 0.

If queryTime is larger than every value in openTimes, the answer should be -1.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the input array is already sorted.
    That sorted property allows us to eliminate half of the remaining search space
    after each comparison, which is much faster than checking every element one by one.
    */
    public int FirstStoreOpenAtOrAfter(int[] openTimes, int queryTime)
    {
        // We will search within the full valid index range of the array.
        // "left" points to the beginning of the current search range.
        // "right" points to the end of the current search range.
        int left = 0;
        int right = openTimes.Length - 1;

        // This variable stores the best answer found so far.
        // We initialize it to -1 because if we never find any opening time
        // that is greater than or equal to queryTime, then the correct answer is -1.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        // The condition left <= right means there is at least one candidate index left.
        while (left <= right)
        {
            // Compute the middle index of the current search range.
            // We use left + (right - left) / 2 instead of (left + right) / 2
            // as a standard safe binary search pattern that avoids integer overflow.
            int mid = left + (right - left) / 2;

            // Read the value at the middle index once so the code is easier to follow.
            int currentOpenTime = openTimes[mid];

            // We now compare the middle value with queryTime.
            // There are two important cases:
            //
            // 1) currentOpenTime >= queryTime
            //    This means index "mid" is a valid candidate answer.
            //    But it may not be the FIRST such index.
            //    Since we need the leftmost valid index, we record mid as a possible answer
            //    and continue searching on the LEFT side to see if an earlier valid index exists.
            //
            // 2) currentOpenTime < queryTime
            //    This means index "mid" is too early and cannot be the answer.
            //    Because the array is sorted, every index to the LEFT of mid is also
            //    less than or equal to currentOpenTime, so those cannot work either.
            //    Therefore, we must search on the RIGHT side.
            if (currentOpenTime >= queryTime)
            {
                // Store this index as the best valid answer found so far.
                answer = mid;

                // Move the right boundary leftward to continue searching
                // for an even earlier valid index.
                right = mid - 1;
            }
            else
            {
                // The current middle value is too small,
                // so discard the left half including mid.
                left = mid + 1;
            }
        }

        // After the loop ends, "answer" is either:
        // - the leftmost index whose value is >= queryTime, or
        // - -1 if no such index exists.
        return answer;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// openTimes = [120, 180, 240, 300], queryTime = 200
// Expected output: 2
int[] openTimes1 = { 120, 180, 240, 300 };
int queryTime1 = 200;
int result1 = solution.FirstStoreOpenAtOrAfter(openTimes1, queryTime1);
Console.WriteLine(result1);

// Example 2:
// openTimes = [60, 60, 90, 150], queryTime = 60
// Expected output: 0
int[] openTimes2 = { 60, 60, 90, 150 };
int queryTime2 = 60;
int result2 = solution.FirstStoreOpenAtOrAfter(openTimes2, queryTime2);
Console.WriteLine(result2);

// Additional example:
// queryTime is larger than every value in openTimes
// Expected output: -1
int[] openTimes3 = { 60, 120, 180 };
int queryTime3 = 500;
int result3 = solution.FirstStoreOpenAtOrAfter(openTimes3, queryTime3);
Console.WriteLine(result3);