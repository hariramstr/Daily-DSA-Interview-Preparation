/*
Title: Find the First Repeated Poll Vote
Difficulty: Easy
Topic: Hashing

Problem Description:
You are given an array `votes` where each element is a string representing the option selected by a user in the order the votes were received. Your task is to return the first vote value that appears more than once in the stream.

A vote is considered the first repeated vote if its second occurrence appears earlier than the second occurrence of any other repeated vote. In other words, scan the array from left to right and return the first value you have already seen before.

If no vote is repeated, return an empty string `""`.

This problem models a real-time polling system where duplicate selections may indicate repeated submissions, and the system wants to detect the earliest duplicated option as quickly as possible.

You should aim for a solution that processes the votes in one pass. A hash set is a natural fit because it allows you to check whether a vote has already appeared in average O(1) time.

Constraints:
- `1 <= votes.length <= 100000`
- `1 <= votes[i].length <= 30`
- `votes[i]` consists of lowercase English letters, digits, or underscores

Example 1:
Input: votes = ["red", "blue", "green", "blue", "red"]
Output: "blue"
Explanation: `blue` is the first vote whose second appearance is encountered while scanning from left to right.

Example 2:
Input: votes = ["north", "south", "east", "west"]
Output: ""
Explanation: No vote appears more than once, so return an empty string.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We scan through the votes array exactly once.
    - Each HashSet operation (Contains / Add) is O(1) on average.

    Space Complexity: O(n)
    - In the worst case, no vote repeats, so we store all votes in the HashSet.
    */
    public string FirstRepeatedVote(string[] votes)
    {
        // We use a HashSet<string> to store every vote we have already seen.
        // Why a HashSet?
        // - We only care whether a vote has appeared before.
        // - A HashSet gives very fast average O(1) lookup and insertion.
        // - This makes it ideal for one-pass duplicate detection.
        var seenVotes = new HashSet<string>();

        // We now scan the votes from left to right, exactly in the order they arrived.
        // This order matters because the problem asks for the first vote whose
        // SECOND occurrence appears earliest in the stream.
        foreach (var vote in votes)
        {
            // Step 1: Check whether this vote has already been seen before.
            // If it has, that means the current vote is a repeated vote.
            // Because we are scanning from left to right, this is the earliest
            // second occurrence we have encountered so far.
            if (seenVotes.Contains(vote))
            {
                // As soon as we find such a vote, we return it immediately.
                // This is correct because any later repeated vote would have its
                // second occurrence later in the stream, so it cannot be the answer.
                return vote;
            }

            // Step 2: If the vote was not already seen, record it in the HashSet.
            // This ensures that if the same vote appears again later, we can detect
            // that repeat instantly.
            seenVotes.Add(vote);
        }

        // If we finish scanning the entire array and never find a repeated vote,
        // then no vote appeared more than once.
        // The problem tells us to return an empty string in that case.
        return "";
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// votes = ["red", "blue", "green", "blue", "red"]
// Walkthrough:
// - "red"   -> first time seen, add it
// - "blue"  -> first time seen, add it
// - "green" -> first time seen, add it
// - "blue"  -> already seen, so this is the first repeated vote
// Expected output: "blue"
string[] votes1 = ["red", "blue", "green", "blue", "red"];
string result1 = solution.FirstRepeatedVote(votes1);
Console.WriteLine(result1);

// Example 2:
// votes = ["north", "south", "east", "west"]
// Walkthrough:
// - each vote appears only once
// - no repeated vote is found
// Expected output: ""
string[] votes2 = ["north", "south", "east", "west"];
string result2 = solution.FirstRepeatedVote(votes2);
Console.WriteLine(result2);

// Additional demo:
// votes = ["a", "b", "c", "a", "b"]
// The second occurrence of "a" appears before the second occurrence of "b".
// Expected output: "a"
string[] votes3 = ["a", "b", "c", "a", "b"];
string result3 = solution.FirstRepeatedVote(votes3);
Console.WriteLine(result3);

// Additional demo with immediate repetition:
// votes = ["yes", "yes", "no"]
// The second vote is already a repeat.
// Expected output: "yes"
string[] votes4 = ["yes", "yes", "no"];
string result4 = solution.FirstRepeatedVote(votes4);
Console.WriteLine(result4);