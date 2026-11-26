"""
Title: Shortest Comment Burst Covering Required Reactions

Problem Description:
You are given a chronological stream of reactions on a live video. Each reaction is
represented by a string in the array reactions, where reactions[i] is the reaction
type posted at second i. You are also given a list required where each element is a
pair [type, count], meaning the chosen contiguous time window must contain at least
count occurrences of that reaction type. A reaction type may appear many times in the
stream, and reaction types not mentioned in required may also appear inside the window.

Return the length of the shortest contiguous subarray of reactions that satisfies all
required reaction counts. If no such window exists, return -1. If multiple valid
windows have the same minimum length, any one of them would be acceptable conceptually,
but your function should return only the minimum length.

This is a hard sliding window problem because the target is not just to include a set
of labels, but to satisfy per-label multiplicity requirements while processing a long
stream efficiently. A brute-force check over all subarrays will time out.

Constraints:
- 1 <= reactions.length <= 200000
- 1 <= required.length <= 100000
- 1 <= reactions[i].length <= 20
- 1 <= required[j][1] <= 10^9
- Sum of distinct reaction types in required is at most 100000
- Reaction strings consist of lowercase English letters, digits, and underscore
- If a reaction type appears multiple times in required, their counts should be added
  together before evaluating the answer

Examples:
1)
reactions = ["like","wow","sad","like","heart","wow","like","heart"]
required = [["like",2],["heart",1],["wow",1]]
Output: 4

2)
reactions = ["fire","clap","fire","laugh","clap","fire"]
required = [["fire",2],["clap",2],["boo",1]]
Output: -1
"""

from typing import Dict, List


class Solution:
    def _build_required_counts(self, required: List[List[object]]) -> Dict[str, int]:
        """
        Build a merged requirement map from the raw required list.

        If the same reaction type appears multiple times in the required input,
        their counts must be added together before solving the problem.

        Args:
            required: A list of [reaction_type, count] pairs.

        Returns:
            A dictionary mapping each required reaction type to its total required count.

        Time complexity:
            O(m), where m is the number of pairs in required.

        Space complexity:
            O(k), where k is the number of distinct required reaction types.
        """
        merged_required: Dict[str, int] = {}

        # We combine duplicate requirement entries into one total count.
        # Example:
        # required = [["like", 1], ["wow", 2], ["like", 3]]
        # becomes {"like": 4, "wow": 2}
        for reaction_type, count in required:
            merged_required[reaction_type] = merged_required.get(reaction_type, 0) + int(count)

        return merged_required

    def shortest_comment_burst(
        self, reactions: List[str], required: List[List[object]]
    ) -> int:
        """
        Return the length of the shortest contiguous subarray that satisfies all
        required reaction counts.

        This uses the classic sliding window technique with frequency counting:
        - Expand the right pointer to include more reactions.
        - Track how many required reaction types are currently satisfied.
        - Once all requirements are satisfied, shrink from the left to find the
          smallest valid window ending at the current right pointer.

        Args:
            reactions: Chronological list of reaction strings.
            required: List of [reaction_type, count] pairs.

        Returns:
            The minimum valid window length, or -1 if no valid window exists.

        Time complexity:
            O(n + m), where:
            - n is the length of reactions
            - m is the length of required
            Each reaction enters and leaves the sliding window at most once.

        Space complexity:
            O(k), where k is the number of distinct required reaction types.
        """
        # Step 1: Merge duplicate requirements into a single dictionary.
        # This is necessary because the problem explicitly says repeated reaction
        # types in "required" should have their counts added together.
        required_counts: Dict[str, int] = self._build_required_counts(required)

        # If there are no requirements, conceptually the shortest window would be 0.
        # However, based on the problem constraints required.length >= 1, so this is
        # just a safe guard for robustness.
        if not required_counts:
            return 0

        # Step 2: Quick feasibility check.
        # Before running the sliding window, we can count occurrences of only the
        # required reaction types in the full stream. If the entire stream does not
        # contain enough of any required type, then no window can ever satisfy the
        # requirement, so we return -1 immediately.
        total_available: Dict[str, int] = {}
        for reaction in reactions:
            if reaction in required_counts:
                total_available[reaction] = total_available.get(reaction, 0) + 1

        for reaction_type, needed_count in required_counts.items():
            if total_available.get(reaction_type, 0) < needed_count:
                return -1

        # Step 3: Prepare sliding window state.
        # window_counts stores how many times each required reaction type appears
        # in the current window [left, right].
        window_counts: Dict[str, int] = {}

        # need_types = number of distinct reaction types that must be satisfied.
        # Example: {"like": 2, "heart": 1, "wow": 1} => need_types = 3
        need_types: int = len(required_counts)

        # formed_types = how many distinct required reaction types are currently
        # satisfied in the window.
        #
        # A type is considered "formed" only when:
        # window_counts[type] >= required_counts[type]
        #
        # We increment formed_types exactly when a type crosses from "not enough"
        # to "enough". We decrement it exactly when shrinking the window causes
        # a type to fall below its required count.
        formed_types: int = 0

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far. Start with infinity so any real valid window
        # will be smaller.
        min_length: int = float("inf")

        # Step 4: Expand the window by moving the right pointer.
        for right, reaction in enumerate(reactions):
            # We only care about counting reaction types that are actually required.
            # Non-required reactions may still be inside the window, but they do not
            # affect whether the window is valid.
            if reaction in required_counts:
                window_counts[reaction] = window_counts.get(reaction, 0) + 1

                # If this addition makes the count exactly reach the required amount,
                # then one more required type is now satisfied.
                if window_counts[reaction] == required_counts[reaction]:
                    formed_types += 1

            # Step 5: If the current window satisfies all required types,
            # try to shrink it from the left to make it as short as possible.
            while formed_types == need_types and left <= right:
                # The current window [left, right] is valid.
                current_length: int = right - left + 1
                if current_length < min_length:
                    min_length = current_length

                # We now attempt to remove reactions[left] from the window and see
                # whether the window remains valid.
                left_reaction: str = reactions[left]

                if left_reaction in required_counts:
                    # If removing this reaction would make its count drop below the
                    # required threshold, then the window will stop being valid after
                    # the removal. So we must reduce formed_types.
                    if window_counts[left_reaction] == required_counts[left_reaction]:
                        formed_types -= 1

                    window_counts[left_reaction] -= 1

                # Move the left boundary rightward to shrink the window.
                left += 1

        # Step 6: If min_length was never updated, no valid window exists.
        # In practice, our feasibility check already catches impossible cases,
        # but this final check keeps the method correct and self-contained.
        return -1 if min_length == float("inf") else min_length


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    reactions_1: List[str] = [
        "like",
        "wow",
        "sad",
        "like",
        "heart",
        "wow",
        "like",
        "heart",
    ]
    required_1: List[List[object]] = [["like", 2], ["heart", 1], ["wow", 1]]
    result_1: int = solution.shortest_comment_burst(reactions_1, required_1)
    print(result_1)  # Expected: 4

    # Example 2
    reactions_2: List[str] = ["fire", "clap", "fire", "laugh", "clap", "fire"]
    required_2: List[List[object]] = [["fire", 2], ["clap", 2], ["boo", 1]]
    result_2: int = solution.shortest_comment_burst(reactions_2, required_2)
    print(result_2)  # Expected: -1

    # Additional small sanity check with duplicate required entries.
    reactions_3: List[str] = ["a", "b", "a", "c", "a", "b"]
    required_3: List[List[object]] = [["a", 1], ["b", 1], ["a", 1]]
    result_3: int = solution.shortest_comment_burst(reactions_3, required_3)
    print(result_3)  # Expected: 3, because total requirement is {"a": 2, "b": 1}