/*
Title: Validate Service Desk Callbacks
Difficulty: Easy
Topic: Stacks and Queues

Problem Description:
A customer support system receives calls identified by unique integer IDs. During the day, every incoming call is appended to a waiting line in the order it arrives. At certain times, the system records a callback log showing the order in which calls were actually handled. Because the support desk always serves the oldest waiting call first, except that some calls may still remain unhandled by the end of the day, the callback log should be a valid prefix of the queue's natural processing order.

You are given two integer arrays: arrivals and handled. The array arrivals lists call IDs in the exact order they entered the waiting line. The array handled lists call IDs in the exact order the support desk claims to have handled them. Determine whether handled could be a valid callback log.

A callback log is valid if each handled call appears at the front of the queue at the moment it is processed, and every handled call must have appeared earlier in arrivals. Calls are unique, and a handled call cannot appear more than once.

Return true if handled is valid, otherwise return false.

Constraints:
- 1 <= arrivals.length <= 100000
- 0 <= handled.length <= arrivals.length
- 1 <= arrivals[i], handled[i] <= 1000000000
- All values in arrivals are distinct
- All values in handled are distinct

Example 1:
Input: arrivals = [10, 20, 30, 40], handled = [10, 20]
Output: true
Explanation: The desk handles the first two calls in FIFO order, and the remaining calls are still waiting.

Example 2:
Input: arrivals = [10, 20, 30, 40], handled = [10, 30]
Output: false
Explanation: After handling call 10, call 20 is still at the front of the queue, so call 30 cannot be handled next.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(m), where m = handled.Length
    Space Complexity: O(1)

    Why this is efficient:
    - In a FIFO queue, the handled sequence must exactly match the beginning (prefix) of arrivals.
    - That means handled[0] must equal arrivals[0], handled[1] must equal arrivals[1], and so on.
    - We only need to compare corresponding elements one by one.
    */
    public bool IsValidCallbackLog(int[] arrivals, int[] handled)
    {
        // Step 1:
        // If the handled log contains more calls than the total number of arrivals,
        // it is automatically impossible.
        //
        // Why this check is necessary:
        // You cannot handle more unique calls than the number of calls that ever arrived.
        if (handled.Length > arrivals.Length)
        {
            return false;
        }

        // Step 2:
        // Walk through every handled call and compare it with the call that should be
        // at the front of the queue at that moment.
        //
        // Important queue idea:
        // Since the support desk always serves the oldest waiting call first,
        // the valid handling order must be exactly the same as the arrival order,
        // at least for however many calls were actually handled.
        //
        // So:
        // - The 1st handled call must be arrivals[0]
        // - The 2nd handled call must be arrivals[1]
        // - The 3rd handled call must be arrivals[2]
        // and so on.
        for (int i = 0; i < handled.Length; i++)
        {
            // Step 3:
            // Compare the current handled call with the expected front-of-queue call.
            //
            // Why this works:
            // At the moment we are checking position i:
            // - The first i calls from arrivals would already have been removed if valid.
            // - Therefore, the next call to be handled must be arrivals[i].
            //
            // If handled[i] is different, then the log is claiming that some later call
            // was handled before an older waiting call, which breaks FIFO order.
            if (handled[i] != arrivals[i])
            {
                return false;
            }
        }

        // Step 4:
        // If every handled call matched the corresponding arrival call,
        // then handled is a valid prefix of the natural queue processing order.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] arrivals1 = { 10, 20, 30, 40 };
int[] handled1 = { 10, 20 };
bool result1 = solution.IsValidCallbackLog(arrivals1, handled1);
Console.WriteLine($"Example 1: {result1}"); // Expected: True

// Example 2
int[] arrivals2 = { 10, 20, 30, 40 };
int[] handled2 = { 10, 30 };
bool result2 = solution.IsValidCallbackLog(arrivals2, handled2);
Console.WriteLine($"Example 2: {result2}"); // Expected: False

// Additional demo: empty handled log is always valid
int[] arrivals3 = { 1, 2, 3 };
int[] handled3 = Array.Empty<int>();
bool result3 = solution.IsValidCallbackLog(arrivals3, handled3);
Console.WriteLine($"Example 3: {result3}"); // Expected: True

// Additional demo: full valid processing order
int[] arrivals4 = { 5, 6, 7 };
int[] handled4 = { 5, 6, 7 };
bool result4 = solution.IsValidCallbackLog(arrivals4, handled4);
Console.WriteLine($"Example 4: {result4}"); // Expected: True