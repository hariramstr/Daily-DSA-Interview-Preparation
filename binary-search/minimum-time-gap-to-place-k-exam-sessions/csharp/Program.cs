/*
Title: Minimum Time Gap to Place K Exam Sessions
Difficulty: Medium
Topic: Binary Search

Problem Description:
A university has already reserved several classrooms at specific start times during a day.
The administration wants to schedule exactly k proctored exam sessions using these reserved
start times, with at most one exam session per reserved time. To reduce staffing conflicts
and hallway congestion, they want the chosen exam sessions to be as far apart as possible.

You are given an integer array times, where times[i] is the start time of the i-th reserved
classroom slot in minutes from the beginning of the day, and an integer k. Return the largest
possible value d such that it is possible to choose exactly k distinct start times from times
where the difference between every pair of consecutive chosen start times is at least d minutes.

If multiple selections are possible, you only need to return the maximum achievable minimum gap d.

This problem should be solved efficiently for large inputs. A common approach is to sort the
times and use binary search on the answer. For a candidate gap d, greedily place exam sessions
from left to right and check whether k sessions can be scheduled.

Constraints:
- 2 <= times.length <= 100000
- 0 <= times[i] <= 1000000000
- All values in times are distinct
- 2 <= k <= times.length

Example 1:
Input: times = [30, 0, 90, 60, 120], k = 3
Output: 60
Explanation: After sorting, times = [0, 30, 60, 90, 120].
Choosing exam sessions at 0, 60, and 120 gives gaps of 60 and 60.
It is impossible to choose 3 sessions with a minimum gap greater than 60, so the answer is 60.

Example 2:
Input: times = [5, 18, 1, 9, 14], k = 4
Output: 4
Explanation: After sorting, times = [1, 5, 9, 14, 18].
One valid choice is 1, 5, 9, and 14, whose consecutive gaps are 4, 4, and 5.
A minimum gap of 5 is not possible for 4 sessions, so the answer is 4.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the array takes O(n log n)
    - Each feasibility check takes O(n)
    - Binary search runs for O(log R), where R = max(times) - min(times)
    - Total: O(n log n + n log R)

    Space Complexity:
    - O(1) extra space beyond the sorting implementation details
    - We sort the input array in place
    */
    public int MaximizeMinimumGap(int[] times, int k)
    {
        // Step 1:
        // Sort the reserved start times in ascending order.
        //
        // Why this is necessary:
        // The condition we care about is the minimum gap between consecutive chosen times.
        // That naturally becomes much easier to reason about when the times are in sorted order.
        // Once sorted, if we greedily choose the earliest possible valid next time, we can test
        // whether a candidate minimum gap is achievable.
        //
        // Data structure choice:
        // We use the input array itself and sort it in place to avoid extra memory usage.
        Array.Sort(times);

        // Step 2:
        // Define the binary search range for the answer.
        //
        // The minimum possible gap is 0.
        // The maximum possible gap is the difference between the largest and smallest time.
        //
        // We will binary search on "d" = candidate minimum gap.
        // For each d, we ask:
        // "Can we choose at least k times so that each chosen time is at least d away
        //  from the previously chosen one?"
        int left = 0;
        int right = times[^1] - times[0];

        // This variable will store the best valid answer found so far.
        int answer = 0;

        // Step 3:
        // Standard binary search on the answer space.
        //
        // Why binary search works:
        // If a gap d is feasible, then any smaller gap is also feasible.
        // If a gap d is not feasible, then any larger gap is also not feasible.
        //
        // This "feasible / not feasible" pattern is monotonic, which is exactly what
        // binary search needs.
        while (left <= right)
        {
            // Compute the middle candidate gap carefully.
            // This avoids overflow and is the standard safe binary search pattern.
            int mid = left + (right - left) / 2;

            // Step 4:
            // Check whether it is possible to place k exam sessions with at least "mid"
            // minutes between consecutive chosen times.
            if (CanPlaceWithGap(times, k, mid))
            {
                // If "mid" is feasible, it is a valid answer.
                // But we want the largest possible valid gap, so we try bigger values.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // If "mid" is not feasible, then any larger gap will also fail.
                // So we must search the smaller half.
                right = mid - 1;
            }
        }

        // After binary search finishes, "answer" holds the largest feasible minimum gap.
        return answer;
    }

    private bool CanPlaceWithGap(int[] times, int k, int gap)
    {
        // This helper method answers:
        // "Can we choose at least k times from the sorted array such that the difference
        //  between consecutive chosen times is at least 'gap'?"
        //
        // Greedy strategy:
        // - Always choose the earliest available time first.
        // - Then keep choosing the earliest next time that is at least "gap" away from
        //   the last chosen time.
        //
        // Why greedy is correct here:
        // Choosing an earlier valid time leaves as much room as possible for future choices.
        // If we skipped an earlier valid time and chose a later one instead, we would only
        // make future placement harder, never easier.
        //
        // Therefore, if the greedy strategy cannot place k sessions, no other strategy can.

        // Step 1:
        // We always choose the first time in the sorted array as the first exam session.
        //
        // Why:
        // It is the earliest possible choice and gives maximum remaining space for the rest.
        int placedCount = 1;
        int lastPlacedTime = times[0];

        // Step 2:
        // Scan from left to right through the remaining times.
        for (int i = 1; i < times.Length; i++)
        {
            // Check whether the current time is far enough from the last chosen time.
            //
            // If times[i] - lastPlacedTime >= gap, then this time can be chosen
            // without violating the minimum gap requirement.
            if (times[i] - lastPlacedTime >= gap)
            {
                // Place one more exam session here.
                placedCount++;
                lastPlacedTime = times[i];

                // As soon as we have placed k sessions, we can stop early.
                // This improves performance in many cases.
                if (placedCount >= k)
                {
                    return true;
                }
            }
        }

        // If we finish scanning and still have fewer than k placements,
        // then this candidate gap is not feasible.
        return false;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1:
// times = [30, 0, 90, 60, 120], k = 3
// Sorted: [0, 30, 60, 90, 120]
// Best choice for maximum minimum gap is 0, 60, 120 -> minimum gap = 60
int[] times1 = { 30, 0, 90, 60, 120 };
int k1 = 3;
int result1 = solution.MaximizeMinimumGap(times1, k1);
Console.WriteLine(result1); // Expected: 60

// Example 2:
// times = [5, 18, 1, 9, 14], k = 4
// Sorted: [1, 5, 9, 14, 18]
// One valid choice: 1, 5, 9, 14 -> minimum gap = 4
int[] times2 = { 5, 18, 1, 9, 14 };
int k2 = 4;
int result2 = solution.MaximizeMinimumGap(times2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional demo:
int[] times3 = { 2, 10, 6, 14, 18, 22 };
int k3 = 3;
int result3 = solution.MaximizeMinimumGap(times3, k3);
Console.WriteLine(result3);