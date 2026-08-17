"""
Title: Count Pairs of Sessions With the Same Unique Error Codes

Problem Description:
A monitoring system records application sessions, where each session contains a list
of error codes that occurred during that session. The same error code may appear
multiple times inside one session if the issue was triggered repeatedly.

Two sessions are considered equivalent if the set of distinct error codes seen in
the two sessions is exactly the same, regardless of the order of codes and regardless
of how many times each code repeats.

Given an array sessions, where sessions[i] is a non-empty array of integers
representing the error codes seen in the i-th session, return the number of pairs
of indices (i, j) such that i < j and sessions[i] and sessions[j] are equivalent.

Example:
- [4, 7, 4, 9] and [9, 7, 4] are equivalent because both contain the unique set
  {4, 7, 9}.
- [4, 7] and [4, 7, 8] are not equivalent.

Constraints:
- 1 <= sessions.length <= 100000
- 1 <= sessions[i].length <= 100
- 0 <= sessions[i][j] <= 1000000000
- The total number of error codes across all sessions does not exceed 300000
"""

from typing import Dict, List, Tuple


class Solution:
    def _canonical_signature(self, session: List[int]) -> Tuple[int, ...]:
        """
        Build a canonical representation for one session.

        The canonical representation must ignore:
        1. Repeated values inside the same session
        2. Original ordering of values

        We achieve this by:
        - Converting the session to a set to keep only distinct error codes
        - Sorting the distinct values so equivalent sessions always produce
          exactly the same ordered result
        - Converting the sorted values to a tuple so it can be used as a
          dictionary key

        Args:
            session: A list of integers representing error codes in one session.

        Returns:
            A tuple containing the sorted distinct error codes of the session.

        Time complexity:
            O(k log k), where k is the number of elements in the session.
            The set creation is O(k), and sorting the distinct values dominates.

        Space complexity:
            O(k), for the set of distinct values and the resulting tuple.
        """
        # Step 1: Remove duplicates inside this session.
        # Example:
        #   [4, 7, 4, 9] -> {4, 7, 9}
        #
        # We use a set because sets automatically keep only unique values.
        unique_codes = set(session)

        # Step 2: Sort the unique values.
        # This is essential because sets are unordered, and we need a stable,
        # repeatable representation.
        #
        # Example:
        #   {4, 7, 9} -> [4, 7, 9]
        #   {9, 4, 7} -> [4, 7, 9]
        #
        # Now equivalent sessions will produce the exact same sequence.
        sorted_unique_codes = sorted(unique_codes)

        # Step 3: Convert the sorted list into a tuple.
        # Tuples are immutable and hashable, so they can be used as keys in
        # a dictionary. Lists cannot be used as dictionary keys.
        signature = tuple(sorted_unique_codes)

        return signature

    def count_equivalent_sessions(self, sessions: List[List[int]]) -> int:
        """
        Count how many pairs of sessions have exactly the same set of distinct
        error codes.

        The main idea:
        - For each session, compute a canonical signature that represents its
          distinct error-code set.
        - Use a dictionary to count how many times each signature has already
          appeared.
        - When we see a signature again, every previous session with that same
          signature forms a new valid pair with the current session.

        Example:
            sessions = [[4,7,4,9], [9,4,7], [1,2,2], [2,1], [5]]

            Signatures in order:
            - (4,7,9): seen 0 times before -> add 0 pairs
            - (4,7,9): seen 1 time before  -> add 1 pair
            - (1,2):   seen 0 times before -> add 0 pairs
            - (1,2):   seen 1 time before  -> add 1 pair
            - (5,):    seen 0 times before -> add 0 pairs

            Total = 2

        Args:
            sessions: A list where each element is a non-empty list of integers
                representing the error codes seen in one session.

        Returns:
            The number of pairs (i, j) such that i < j and the two sessions are
            equivalent by distinct error-code set.

        Time complexity:
            Let T be the total number of error codes across all sessions.
            Since each session has length at most 100, sorting each session's
            distinct values is efficient. Overall complexity is
            O(sum(k_i log k_i)), which is efficient under the given constraints.

        Space complexity:
            O(m * u) in the worst case for storing signatures in the dictionary,
            where m is the number of sessions and u is the average number of
            distinct values per session. More simply, the extra space is
            proportional to the number of unique signatures stored.
        """
        # This dictionary maps:
        #   canonical session signature -> how many previous sessions had it
        #
        # Example:
        #   {
        #       (4, 7, 9): 2,
        #       (1, 2): 1
        #   }
        #
        # We use a dictionary because it gives average O(1) lookup and update,
        # which is ideal for counting repeated patterns efficiently.
        signature_count: Dict[Tuple[int, ...], int] = {}

        # This will store the final number of valid pairs.
        total_pairs = 0

        # Process each session one by one.
        for session in sessions:
            # Convert the current session into its canonical form.
            # Equivalent sessions will always produce the same signature.
            signature = self._canonical_signature(session)

            # Find how many times we have already seen this exact signature.
            #
            # Why does this directly tell us how many new pairs are formed?
            # Because if the same signature was seen x times before, then the
            # current session can pair with each of those x previous sessions.
            #
            # Example:
            #   If signature (1, 2) has appeared 3 times already, then the
            #   current session creates 3 new pairs.
            previous_occurrences = signature_count.get(signature, 0)

            # Add those newly formed pairs to the answer.
            total_pairs += previous_occurrences

            # Now record that we have seen this signature one more time.
            signature_count[signature] = previous_occurrences + 1

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement
    sessions1 = [[4, 7, 4, 9], [9, 4, 7], [1, 2, 2], [2, 1], [5]]
    result1 = solution.count_equivalent_sessions(sessions1)
    print("Example 1 Output:", result1)  # Expected: 2

    # Example 2 from the problem statement
    sessions2 = [[8, 8, 8], [8], [1, 3, 1, 3], [3, 1], [2, 2, 4], [4, 2, 4], [2, 4, 5]]
    result2 = solution.count_equivalent_sessions(sessions2)
    print("Example 2 Output:", result2)  # Expected: 3

    # Additional quick checks
    sessions3 = [[1], [1], [1]]
    result3 = solution.count_equivalent_sessions(sessions3)
    print("Additional Check 1 Output:", result3)  # Expected: 3

    sessions4 = [[1, 2], [2, 1, 1], [1, 2, 3], [3, 2, 1], [4]]
    result4 = solution.count_equivalent_sessions(sessions4)
    print("Additional Check 2 Output:", result4)  # Expected: 2