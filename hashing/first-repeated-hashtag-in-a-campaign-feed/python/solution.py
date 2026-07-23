"""
Title: First Repeated Hashtag in a Campaign Feed

Problem Description:
A marketing platform stores the hashtags used in a campaign feed as an array of strings,
in the exact order they were posted. Your task is to find the first hashtag that appears
more than once while scanning the feed from left to right.

Return the first repeated hashtag based on the moment its second occurrence is seen.
If no hashtag is repeated, return an empty string.

For example, if the feed is ["#launch", "#sale", "#launch", "#summer"], the answer is
"#launch" because it is the first hashtag whose second appearance occurs during the scan.
If multiple hashtags appear multiple times, you should not return the one with the
smallest total count or lexicographically smallest value; return the one that becomes
repeated earliest.

This problem is intended to be solved efficiently using a hash-based data structure to
track which hashtags have already been seen.

Constraints:
- 1 <= hashtags.length <= 100000
- 1 <= hashtags[i].length <= 50
- hashtags[i] consists of letters, digits, underscores, and the '#' character
- Comparison is case-sensitive

Example 1:
Input: hashtags = ["#launch", "#sale", "#launch", "#summer"]
Output: "#launch"
Explanation: "#launch" is seen at index 0 and repeats at index 2, which is the earliest
second occurrence.

Example 2:
Input: hashtags = ["#red", "#blue", "#green", "#blue", "#red"]
Output: "#blue"
Explanation: Although both "#blue" and "#red" repeat, "#blue" becomes repeated first
when scanning from left to right.
"""

from typing import List, Set


class Solution:
    def first_repeated_hashtag(self, hashtags: List[str]) -> str:
        """
        Find the first hashtag whose second occurrence appears earliest while scanning
        from left to right.

        Args:
            hashtags: A list of hashtag strings in posting order.

        Returns:
            The first repeated hashtag encountered during the left-to-right scan.
            Returns an empty string if no hashtag repeats.

        Time complexity:
            O(n), where n is the number of hashtags, because each hashtag is processed once
            and set lookups/inserts are O(1) on average.

        Space complexity:
            O(n) in the worst case, if all hashtags are unique and must be stored in the set.
        """
        # We use a set because it is a hash-based data structure that gives us
        # very fast average-case membership checks.
        #
        # Why a set?
        # - We only need to know whether a hashtag has been seen before.
        # - We do NOT need to count all occurrences.
        # - We do NOT need to sort anything.
        # - We do NOT need to store positions for this specific task.
        #
        # A set is therefore the simplest and most efficient tool here.
        seen: Set[str] = set()

        # We scan the feed exactly in the order given, from left to right.
        # This is important because the problem asks for the hashtag whose
        # SECOND occurrence happens first during the scan.
        #
        # That means:
        # - The moment we encounter a hashtag that is already in "seen",
        #   we immediately know it is the first repeated hashtag.
        # - We can return right away, because any later repeated hashtag would
        #   have its second occurrence later in the scan.
        for hashtag in hashtags:
            # Step 1: Check whether this hashtag has already appeared before.
            if hashtag in seen:
                # If yes, then this is the first moment we have found a repeat
                # while scanning from left to right.
                #
                # This directly matches the problem requirement:
                # "Return the first repeated hashtag based on the moment its
                # second occurrence is seen."
                return hashtag

            # Step 2: If it has not been seen before, record it in the set.
            # This ensures that if we encounter the same hashtag again later,
            # we can detect the repetition instantly.
            seen.add(hashtag)

        # If we finish the entire scan without finding any repeated hashtag,
        # then no hashtag appears more than once.
        return ""

    def solve(self, hashtags: List[str]) -> str:
        """
        Wrapper method that calls the main algorithm.

        Args:
            hashtags: A list of hashtag strings.

        Returns:
            The first repeated hashtag, or an empty string if none exists.

        Time complexity:
            O(n), where n is the number of hashtags.

        Space complexity:
            O(n) in the worst case.
        """
        return self.first_repeated_hashtag(hashtags)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement:
    # Scan order:
    # 1. "#launch" -> not seen, add it
    # 2. "#sale"   -> not seen, add it
    # 3. "#launch" -> already seen, so this is the first repeated hashtag
    hashtags1: List[str] = ["#launch", "#sale", "#launch", "#summer"]
    result1: str = solution.solve(hashtags1)
    print(result1)  # Expected: #launch

    # Example 2 from the problem statement:
    # Scan order:
    # 1. "#red"   -> not seen, add it
    # 2. "#blue"  -> not seen, add it
    # 3. "#green" -> not seen, add it
    # 4. "#blue"  -> already seen, first repeated found here
    # 5. "#red"   -> also repeated, but too late to matter
    hashtags2: List[str] = ["#red", "#blue", "#green", "#blue", "#red"]
    result2: str = solution.solve(hashtags2)
    print(result2)  # Expected: #blue

    # Additional sample: no repeated hashtags
    hashtags3: List[str] = ["#one", "#two", "#three"]
    result3: str = solution.solve(hashtags3)
    print(result3)  # Expected: ""