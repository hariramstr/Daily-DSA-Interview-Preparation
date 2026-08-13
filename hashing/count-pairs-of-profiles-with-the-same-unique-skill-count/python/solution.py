"""
Title: Count Pairs of Profiles with the Same Unique Skill Count

Problem Description:
You are given a list of employee profiles. Each profile is represented by a list of
skill names, where the same skill may appear multiple times in the same profile because
of imported data errors. For each profile, define its unique skill count as the number
of distinct skill names that appear in that profile.

Your task is to return the number of unordered pairs of profiles that have the same
unique skill count.

Two profiles form a valid pair if, after removing duplicates within each individual
profile, both profiles contain the same number of distinct skills. The actual skill
names do not need to match—only the count of distinct skills matters.

For example, the profiles ["java", "sql", "java"] and ["go", "python"] both have a
unique skill count of 2, so they form a valid pair.

Return the total number of such pairs across all profiles.

Constraints:
- 1 <= profiles.length <= 100000
- 1 <= profiles[i].length <= 100000
- The sum of profiles[i].length over all profiles does not exceed 200000
- Each skill name consists of lowercase English letters and has length between 1 and 20

Examples:
1)
Input:
profiles = [
    ["java", "sql", "java"],
    ["go", "python"],
    ["aws", "aws", "linux"],
    ["c++"],
    ["html", "css", "js"]
]
Unique skill counts: [2, 2, 2, 1, 3]
Profiles with count 2: 3 profiles -> 3 choose 2 = 3 pairs
Answer: 3

2)
Input:
profiles = [
    ["ml", "ml", "ml"],
    ["sql"],
    ["go", "rust"],
    ["a", "b", "c"],
    ["x", "y"],
    ["k"]
]
Unique skill counts: [1, 1, 2, 3, 2, 1]
Count 1 appears 3 times -> 3 choose 2 = 3 pairs
Count 2 appears 2 times -> 2 choose 2 = 1 pair
Total answer: 4
"""

from typing import Dict, List


class Solution:
    def count_pairs_same_unique_skill_count(self, profiles: List[List[str]]) -> int:
        """
        Count unordered pairs of profiles that have the same number of distinct skills.

        The method first computes the number of unique skills in each profile by using
        a set, then groups profiles by that unique-count value using a dictionary.
        Finally, for each group of size k, it adds k * (k - 1) // 2 to the answer.

        Args:
            profiles: A list where each element is a profile represented by a list of skill names.

        Returns:
            The total number of unordered profile pairs with the same unique skill count.

        Time complexity:
            O(S), where S is the total number of skill entries across all profiles.
            Creating a set for each profile processes each skill once overall.

        Space complexity:
            O(P + U), where P is the number of profiles for the frequency map keys/values,
            and U is the maximum number of distinct skills in a single profile set at one time.
        """
        # This dictionary will map:
        #   unique_skill_count -> number_of_profiles_with_that_count
        #
        # Example:
        # If we process profiles and find unique counts [2, 2, 1, 3, 2],
        # then this dictionary becomes:
        #   {
        #       2: 3,
        #       1: 1,
        #       3: 1
        #   }
        #
        # We use a dictionary because:
        # - We need fast updates while scanning profiles
        # - We only care how many profiles share the same distinct-count value
        count_frequency: Dict[int, int] = {}

        # Process each profile one by one.
        for profile in profiles:
            # Convert the profile list into a set.
            #
            # Why?
            # A set automatically removes duplicates.
            #
            # Example:
            # ["java", "sql", "java"] -> {"java", "sql"}
            #
            # Then len(...) gives the number of distinct skills in that profile.
            unique_skill_count: int = len(set(profile))

            # Record how many profiles have this exact unique skill count.
            #
            # dict.get(key, 0) means:
            # - if key exists, return its current value
            # - otherwise, start from 0
            count_frequency[unique_skill_count] = count_frequency.get(unique_skill_count, 0) + 1

        # Now compute the number of valid unordered pairs.
        #
        # If a certain unique skill count appears k times, then the number of ways
        # to choose 2 profiles from those k profiles is:
        #
        #   k * (k - 1) // 2
        #
        # This is the standard combination formula "k choose 2".
        total_pairs: int = 0

        for frequency in count_frequency.values():
            total_pairs += frequency * (frequency - 1) // 2

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    profiles1: List[List[str]] = [
        ["java", "sql", "java"],
        ["go", "python"],
        ["aws", "aws", "linux"],
        ["c++"],
        ["html", "css", "js"],
    ]
    result1: int = solution.count_pairs_same_unique_skill_count(profiles1)
    print("Example 1 Output:", result1)  # Expected: 3

    # Example 2 from the prompt.
    profiles2: List[List[str]] = [
        ["ml", "ml", "ml"],
        ["sql"],
        ["go", "rust"],
        ["a", "b", "c"],
        ["x", "y"],
        ["k"],
    ]
    result2: int = solution.count_pairs_same_unique_skill_count(profiles2)
    print("Example 2 Output:", result2)  # Expected: 4

    # Additional small sanity check.
    profiles3: List[List[str]] = [
        ["a", "a"],      # unique count = 1
        ["b"],           # unique count = 1
        ["c", "d"],      # unique count = 2
        ["e", "f"],      # unique count = 2
        ["g", "h", "i"], # unique count = 3
    ]
    result3: int = solution.count_pairs_same_unique_skill_count(profiles3)
    print("Additional Test Output:", result3)  # Expected: 2