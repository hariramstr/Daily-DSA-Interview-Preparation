"""
Title: Count Mirror Username Pairs
Difficulty: Medium
Topic: Hashing

Problem Description:
A social platform stores a list of usernames in the order they were created.
Two usernames form a mirror pair if one of them is exactly the reverse of the other,
and the two usernames appear at different indices.

For example:
- "stressed" and "desserts" form a mirror pair
- A single username does not pair with itself unless the same string appears again
  at another index

Task:
Given an array usernames of length n, return the total number of distinct index pairs
(i, j) with i < j such that usernames[j] is the reverse of usernames[i].

Important details:
- Pairs are counted by indices, not by unique string values
- Duplicate usernames can create multiple valid pairs
- Palindromes such as "level" can form mirror pairs with other equal copies of the
  same palindrome

Constraints:
- 1 <= n <= 200000
- 1 <= usernames[i].length <= 30
- usernames[i] consists only of lowercase English letters

Example 1:
Input: usernames = ["abc", "cba", "xy", "yx", "abc"]
Output: 2
Explanation:
- (0, 1): "abc" reversed is "cba"
- (2, 3): "xy" reversed is "yx"
The last "abc" has no later matching reverse.

Example 2:
Input: usernames = ["aa", "aa", "aa", "ab", "ba"]
Output: 4
Explanation:
- The three "aa" usernames are palindromes, so every pair among them is valid:
  C(3, 2) = 3
- "ab" and "ba" form 1 pair
Total = 4
"""

from typing import Dict, List


class Solution:
    def count_mirror_pairs(self, usernames: List[str]) -> int:
        """
        Count the number of index pairs (i, j) with i < j such that
        usernames[j] is the reverse of usernames[i].

        The method scans from left to right and keeps track of how many times
        each username has already appeared. For the current username, any earlier
        occurrence of its reversed form creates a valid pair.

        Args:
            usernames: A list of lowercase username strings.

        Returns:
            The total number of mirror pairs.

        Time complexity:
            O(n * m), where n is the number of usernames and m is the maximum
            username length, because reversing a string of length m takes O(m).

        Space complexity:
            O(k), where k is the number of distinct usernames stored in the hash map.
        """
        # This dictionary will store how many times each username has appeared
        # so far while scanning from left to right.
        #
        # Why this helps:
        # Suppose we are currently at index j with username = current_name.
        # We want to count all earlier indices i < j such that:
        #     usernames[i] reversed == current_name
        #
        # That is equivalent to:
        #     usernames[i] == reverse(current_name)
        #
        # So if we already know how many times reverse(current_name) has appeared
        # earlier, then we can add that count directly to the answer.
        #
        # This avoids checking every earlier username one by one, which would be
        # too slow for up to 200000 usernames.
        seen_count: Dict[str, int] = {}

        # This will accumulate the total number of valid mirror pairs.
        total_pairs: int = 0

        # Process usernames in creation order.
        # This guarantees that whenever we count matches from seen_count,
        # those matches come from earlier indices only, so the condition i < j
        # is automatically satisfied.
        for username in usernames:
            # Compute the reversed version of the current username.
            #
            # Example:
            # - username = "cba"
            # - reversed_username = "abc"
            #
            # If "abc" has already appeared before, then each such earlier "abc"
            # forms a valid pair with the current "cba".
            reversed_username: str = username[::-1]

            # Add the number of earlier usernames equal to reversed_username.
            #
            # If reversed_username has not appeared before, get(..., 0) returns 0.
            #
            # Examples:
            # 1) usernames = ["abc", "cba"]
            #    - At "abc": reversed is "cba", seen_count["cba"] is 0, add 0
            #    - Record "abc"
            #    - At "cba": reversed is "abc", seen_count["abc"] is 1, add 1
            #
            # 2) usernames = ["aa", "aa", "aa"]
            #    Since "aa" reversed is still "aa":
            #    - first "aa": add 0
            #    - second "aa": add 1
            #    - third "aa": add 2
            #    Total = 3, which is correct.
            total_pairs += seen_count.get(reversed_username, 0)

            # Now record that the current username has been seen one more time.
            #
            # This update happens AFTER counting, not before.
            # That order is important because a username must pair only with
            # earlier indices, not with itself at the same index.
            seen_count[username] = seen_count.get(username, 0) + 1

        # After processing all usernames, total_pairs contains the answer.
        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    sample_usernames_1: List[str] = ["abc", "cba", "xy", "yx", "abc"]
    result_1: int = solution.count_mirror_pairs(sample_usernames_1)
    print("Example 1:")
    print("Input:", sample_usernames_1)
    print("Output:", result_1)
    print("Expected:", 2)
    print()

    sample_usernames_2: List[str] = ["aa", "aa", "aa", "ab", "ba"]
    result_2: int = solution.count_mirror_pairs(sample_usernames_2)
    print("Example 2:")
    print("Input:", sample_usernames_2)
    print("Output:", result_2)
    print("Expected:", 4)
    print()

    additional_usernames_1: List[str] = ["stressed", "desserts"]
    result_3: int = solution.count_mirror_pairs(additional_usernames_1)
    print("Additional Example 1:")
    print("Input:", additional_usernames_1)
    print("Output:", result_3)
    print("Expected:", 1)
    print()

    additional_usernames_2: List[str] = ["abc", "abc", "cba", "cba", "cba"]
    result_4: int = solution.count_mirror_pairs(additional_usernames_2)
    print("Additional Example 2:")
    print("Input:", additional_usernames_2)
    print("Output:", result_4)
    print("Expected:", 6)
    print()

    additional_usernames_3: List[str] = ["level", "level", "level", "test"]
    result_5: int = solution.count_mirror_pairs(additional_usernames_3)
    print("Additional Example 3:")
    print("Input:", additional_usernames_3)
    print("Output:", result_5)
    print("Expected:", 3)