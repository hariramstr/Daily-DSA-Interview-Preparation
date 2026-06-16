/*
Title: Minimum Cooldown to Launch K Rockets

Problem Description:
A spaceport has n launch pads arranged along a straight line. The position of the i-th pad is given by pads[i],
and the array is sorted in non-decreasing order. You want to schedule exactly k rocket launches, choosing k
distinct pads. For safety reasons, any two chosen pads must be at least d units apart, where d is a global
cooldown distance applied to the entire schedule.

Your task is to compute the largest possible value of d such that it is still possible to choose k pads
satisfying the distance requirement. In other words, maximize the minimum pairwise distance between consecutive
selected launch pads.

Return the maximum feasible cooldown distance.

Constraints:
- 2 <= n <= 200000
- 2 <= k <= n
- 0 <= pads[i] <= 10^18
- pads is sorted in non-decreasing order
- Multiple pads may share the same position, but only one rocket can be launched from each pad index

Examples:
1) pads = [1, 2, 8, 12, 17], k = 3
   Output: 7
   Explanation: Choose pads at positions 1, 8, and 17. The minimum consecutive gap is 7.

2) pads = [0, 0, 4, 9, 13, 18], k = 4
   Output: 4
   Explanation: One optimal choice is 0, 4, 9, 13. The minimum consecutive gap is 4.

Core idea:
- If a distance d is feasible, then every smaller distance is also feasible.
- If a distance d is not feasible, then every larger distance is also not feasible.
- This monotonic behavior makes binary search the correct tool.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Feasibility check for one candidate distance: O(n)
    - Binary search over the answer range [0, pads[n - 1] - pads[0]]: O(log(maxCoordinateRange))
    - Total: O(n * log(maxCoordinateRange))

    Space Complexity:
    - O(1) extra space
    */
    public long MaximumCooldown(long[] pads, int k)
    {
        // The smallest possible answer is 0.
        // This is important because duplicate pad positions are allowed,
        // so sometimes the best possible minimum distance can truly be 0.
        long left = 0;

        // The largest possible answer cannot exceed the distance between
        // the leftmost and rightmost pad.
        long right = pads[pads.Length - 1] - pads[0];

        // We will store the best feasible answer found so far.
        long answer = 0;

        // Standard binary search on the answer space.
        // We are searching for the maximum feasible distance.
        while (left <= right)
        {
            // Compute the middle carefully.
            // Using this form avoids overflow in general binary search patterns.
            long mid = left + (right - left) / 2;

            // Check whether it is possible to choose at least k pads
            // such that every chosen pad is at least 'mid' away from the previous chosen pad.
            if (CanPlaceKLaunches(pads, k, mid))
            {
                // If 'mid' is feasible, it means this distance works.
                // Since we want the largest possible feasible distance,
                // we record it and try searching to the right for a bigger answer.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // If 'mid' is not feasible, then any larger distance will also not be feasible.
                // So we must search the smaller half.
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool CanPlaceKLaunches(long[] pads, int k, long minDistance)
    {
        // Greedy strategy:
        // Always place the first rocket at the earliest available pad,
        // then place each next rocket at the earliest pad that is far enough away.
        //
        // Why greedy works:
        // Choosing an earlier valid pad leaves as much room as possible for future choices.
        // If we can place k rockets this way, then the distance is feasible.
        // If even this best-room-preserving strategy cannot place k rockets,
        // then no other strategy can.

        // We always choose the first pad.
        int placed = 1;

        // Track the position of the most recently chosen pad.
        long lastChosenPosition = pads[0];

        // Scan from left to right through the sorted pads.
        for (int i = 1; i < pads.Length; i++)
        {
            // Check whether the current pad is far enough from the last chosen pad.
            // If yes, we can safely choose it.
            if (pads[i] - lastChosenPosition >= minDistance)
            {
                placed++;
                lastChosenPosition = pads[i];

                // As soon as we have placed k rockets, we know this candidate distance works.
                if (placed >= k)
                {
                    return true;
                }
            }
        }

        // If we finish scanning and still placed fewer than k rockets,
        // then this candidate distance is not feasible.
        return false;
    }
}

// Demo code

var solution = new Solution();

// Example 1
long[] pads1 = { 1, 2, 8, 12, 17 };
int k1 = 3;
long result1 = solution.MaximumCooldown(pads1, k1);
Console.WriteLine(result1); // Expected: 7

// Example 2
long[] pads2 = { 0, 0, 4, 9, 13, 18 };
int k2 = 4;
long result2 = solution.MaximumCooldown(pads2, k2);
Console.WriteLine(result2); // Expected: 4

// Additional quick demo
long[] pads3 = { 5, 5, 5, 5 };
int k3 = 2;
long result3 = solution.MaximumCooldown(pads3, k3);
Console.WriteLine(result3); // Expected: 0