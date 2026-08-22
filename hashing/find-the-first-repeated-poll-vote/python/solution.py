"""
Title: Find the First Repeated Poll Vote

Problem Description:
You are given an array `votes` where each element is a string representing the option
selected by a user in the order the votes were received. Your task is to return the
first vote value that appears more than once in the stream.

A vote is considered the first repeated vote if its second occurrence appears earlier
than the second occurrence of any other repeated vote. In other words, scan the array
from left to right and return the first value you have already seen before.

If no vote is repeated, return an empty string `""`.

This problem models a real-time polling system where duplicate selections may indicate
repeated submissions, and the system wants to detect the earliest duplicated option as
quickly as possible.

You should aim for a solution that processes the votes in one pass. A hash set is a
natural fit because it allows you to check whether a vote has already appeared in
average O(1) time.

Constraints:
- 1 <= votes.length <= 100000
- 1 <= votes[i].length <= 30
- votes[i] consists of lowercase English letters, digits, or underscores

Example 1:
Input: votes = ["red", "blue", "green", "blue", "red"]
Output: "blue"

Example 2:
Input: votes = ["north", "south", "east", "west"]
Output: ""
"""

from typing import List, Set


class Solution:
    def first_repeated_vote(self, votes: List[str]) -> str:
        """
        Find the first vote whose second occurrence appears earliest while scanning
        from left to right.

        Args:
            votes: A list of vote strings in the order they were received.

        Returns:
            The first repeated vote value encountered during a left-to-right scan.
            Returns an empty string if no vote is repeated.

        Time Complexity:
            O(n), where n is the number of votes, because we scan the list once and
            each set lookup/insertion is average O(1).

        Space Complexity:
            O(n) in the worst case, if all votes are unique and must be stored in the set.
        """
        # We use a set named "seen" to store every vote we have already encountered.
        #
        # Why a set?
        # - A set is designed for fast membership checks.
        # - We need to repeatedly answer the question:
        #   "Have we seen this vote before?"
        # - In Python, checking membership in a set is average O(1), which makes it
        #   ideal for a one-pass solution.
        seen: Set[str] = set()

        # We now scan through the votes from left to right exactly once.
        #
        # This order is extremely important:
        # - The problem defines the answer based on the earliest second occurrence.
        # - By scanning in arrival order, the first time we encounter a vote that is
        #   already in "seen", we know that this is the earliest repeated vote.
        for vote in votes:
            # Step 1: Check whether the current vote has already appeared before.
            #
            # If it has, then this current position is the second (or later) occurrence
            # of that vote. Because we are scanning from left to right, this is the
            # earliest repeated vote encountered so far, and therefore the correct answer.
            if vote in seen:
                return vote

            # Step 2: If the vote has not been seen before, add it to the set.
            #
            # This records the vote so that if it appears again later, we can detect
            # the repetition immediately.
            seen.add(vote)

        # If we finish the entire loop without returning, then no vote was repeated.
        # According to the problem statement, in that case we return an empty string.
        return ""

    def solve(self, votes: List[str]) -> str:
        """
        Wrapper method that calls the main algorithm.

        Args:
            votes: A list of vote strings.

        Returns:
            The first repeated vote, or an empty string if none exists.

        Time Complexity:
            O(n), where n is the number of votes.

        Space Complexity:
            O(n) in the worst case.
        """
        return self.first_repeated_vote(votes)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem description:
    # Scan order:
    # - "red"   -> not seen before, add it
    # - "blue"  -> not seen before, add it
    # - "green" -> not seen before, add it
    # - "blue"  -> already seen, so answer is "blue"
    votes1 = ["red", "blue", "green", "blue", "red"]
    result1 = solution.solve(votes1)
    print(result1)  # Expected: "blue"

    # Example 2 from the problem description:
    # All votes are unique, so there is no repeated vote.
    votes2 = ["north", "south", "east", "west"]
    result2 = solution.solve(votes2)
    print(result2)  # Expected: ""

    # Additional sample:
    # - "a" -> add
    # - "b" -> add
    # - "c" -> add
    # - "a" -> repeated first, so answer is "a"
    votes3 = ["a", "b", "c", "a", "b"]
    result3 = solution.solve(votes3)
    print(result3)  # Expected: "a"