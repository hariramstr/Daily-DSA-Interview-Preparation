/*
Title: Minimum Server Version to Pass All Client Requirements

Problem Description:
A company is rolling out a backend API in numbered versions from 1 to m. There are n client applications,
and each client i can successfully connect only if the server version is at least requirements[i].
You are also given an integer k, the minimum number of clients that must be supported immediately after launch.

Your task is to find the smallest server version v such that at least k clients can connect when version v is deployed.

Formally, return the minimum integer v where the count of values in requirements that are less than or equal to v
is at least k. If it is impossible because k > n, return -1.

The input array is not guaranteed to be sorted. A straightforward solution would sort the requirements first,
then use binary search on the answer or directly on the sorted array. The goal is to design an efficient solution
that works within typical interview constraints.

Constraints:
- 1 <= n <= 2 * 10^5
- 1 <= requirements[i] <= 10^9
- 1 <= m <= 10^9
- 1 <= k <= 2 * 10^5
- Server versions are positive integers from 1 to m

If the minimum required version exceeds m, then no valid deployment inside the allowed range exists, and you should return -1.

Example 1:
Input: requirements = [5, 2, 8, 4, 4], m = 10, k = 3
Output: 4
Explanation: At version 4, the supported clients are those requiring 2, 4, and 4, so 3 clients can connect.
No smaller version supports at least 3 clients.

Example 2:
Input: requirements = [7, 9, 12], m = 10, k = 2
Output: 9
Explanation: At version 9, clients requiring 7 and 9 can connect, so 2 clients are supported.
Version 8 supports only 1 client, so 9 is the minimum valid answer.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting the requirements array takes O(n log n)
    - After sorting, the answer is simply the k-th smallest requirement, which is O(1) to access
    - Total: O(n log n)

    Space Complexity:
    - If the input array can be modified, Array.Sort works in-place for practical interview purposes
    - Extra auxiliary space is considered O(1) beyond the input storage
    */
    public int MinimumServerVersion(int[] requirements, int m, int k)
    {
        // Step 1:
        // First, we handle impossible cases as early as possible.
        //
        // Why is this necessary?
        // We are asked to support at least k clients.
        // If there are fewer than k total clients in the array, then no server version can ever support k clients,
        // because even the maximum possible version cannot create more clients than actually exist.
        if (requirements == null || k > requirements.Length)
        {
            return -1;
        }

        // Step 2:
        // Sort the requirements array in non-decreasing order.
        //
        // Why sort?
        // After sorting:
        // - The smallest requirement is at index 0
        // - The second smallest is at index 1
        // - ...
        // - The k-th smallest requirement is at index k - 1
        //
        // This is the key observation:
        // The minimum server version that supports at least k clients is exactly the k-th smallest requirement.
        //
        // Why?
        // Because:
        // - If the server version is smaller than the k-th smallest requirement,
        //   then fewer than k clients can be supported.
        // - If the server version is equal to the k-th smallest requirement,
        //   then at least the first k clients in sorted order are supported.
        //
        // So sorting transforms the problem into a direct lookup.
        Array.Sort(requirements);

        // Step 3:
        // The answer candidate is the k-th smallest requirement.
        //
        // Since arrays are zero-indexed in C#:
        // - 1st smallest is requirements[0]
        // - 2nd smallest is requirements[1]
        // - k-th smallest is requirements[k - 1]
        int answer = requirements[k - 1];

        // Step 4:
        // We must ensure the chosen version is actually deployable.
        //
        // The problem states that valid server versions are only in the range [1, m].
        // So even if the mathematical answer is some value > m,
        // that version cannot be deployed, and we must return -1.
        if (answer > m)
        {
            return -1;
        }

        // Step 5:
        // If we reach here, answer is the smallest valid server version that supports at least k clients.
        return answer;
    }

    /*
    Optional educational alternative:
    This method demonstrates the "sort + binary search on answer" approach mentioned in the prompt.

    Time Complexity:
    - Sorting: O(n log n)
    - Binary search over version range [1, m]: O(log m)
    - Each binary search step needs an upper-bound search in the sorted array: O(log n)
    - Total: O(n log n + log m * log n)

    Space Complexity:
    - O(1) extra beyond the input array (practical interview assumption)
    */
    public int MinimumServerVersionBinarySearch(int[] requirements, int m, int k)
    {
        // Just like the main method, we first reject impossible input.
        if (requirements == null || k > requirements.Length)
        {
            return -1;
        }

        // Sort so we can quickly count how many requirements are <= a chosen version.
        Array.Sort(requirements);

        // Before binary searching, check whether even the maximum allowed version m
        // can support at least k clients.
        //
        // To do that, count how many requirements are <= m.
        int supportedAtMaxVersion = CountLessThanOrEqual(requirements, m);

        // If even version m supports fewer than k clients, then no valid answer exists.
        if (supportedAtMaxVersion < k)
        {
            return -1;
        }

        // Binary search on the answer space:
        // We search for the smallest version v in [1, m]
        // such that count(requirements <= v) >= k.
        int left = 1;
        int right = m;
        int best = -1;

        while (left <= right)
        {
            // Standard overflow-safe midpoint calculation.
            int mid = left + (right - left) / 2;

            // Count how many clients can connect if we deploy version mid.
            int supported = CountLessThanOrEqual(requirements, mid);

            // If mid supports at least k clients, it is a valid candidate.
            // But we still want the minimum such version, so we continue searching left.
            if (supported >= k)
            {
                best = mid;
                right = mid - 1;
            }
            else
            {
                // Otherwise, mid is too small, so we must search larger versions.
                left = mid + 1;
            }
        }

        return best;
    }

    private int CountLessThanOrEqual(int[] sortedRequirements, int target)
    {
        // This helper returns how many values in the sorted array are <= target.
        //
        // We use a classic upper-bound style binary search:
        // find the first index where value > target.
        // That index is also the count of values <= target.
        int left = 0;
        int right = sortedRequirements.Length;

        while (left < right)
        {
            int mid = left + (right - left) / 2;

            if (sortedRequirements[mid] <= target)
            {
                left = mid + 1;
            }
            else
            {
                right = mid;
            }
        }

        return left;
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// requirements = [5, 2, 8, 4, 4], m = 10, k = 3
// Sorted: [2, 4, 4, 5, 8]
// The 3rd smallest requirement is 4, so the minimum version is 4.
int[] requirements1 = { 5, 2, 8, 4, 4 };
int m1 = 10;
int k1 = 3;
int result1 = solution.MinimumServerVersion(requirements1, m1, k1);
Console.WriteLine(result1); // Expected: 4

// Example 2:
// requirements = [7, 9, 12], m = 10, k = 2
// Sorted: [7, 9, 12]
// The 2nd smallest requirement is 9, and 9 <= 10, so answer is 9.
int[] requirements2 = { 7, 9, 12 };
int m2 = 10;
int k2 = 2;
int result2 = solution.MinimumServerVersion(requirements2, m2, k2);
Console.WriteLine(result2); // Expected: 9

// Additional demo:
// Impossible because the minimum version needed for the k-th client exceeds m.
// requirements = [7, 9, 12], m = 8, k = 2
// Sorted: [7, 9, 12]
// The 2nd smallest requirement is 9, but 9 > 8, so answer is -1.
int[] requirements3 = { 7, 9, 12 };
int m3 = 8;
int k3 = 2;
int result3 = solution.MinimumServerVersion(requirements3, m3, k3);
Console.WriteLine(result3); // Expected: -1

// Additional demo:
// Impossible because k > n.
int[] requirements4 = { 1, 2 };
int m4 = 10;
int k4 = 3;
int result4 = solution.MinimumServerVersion(requirements4, m4, k4);
Console.WriteLine(result4); // Expected: -1