"""
Title: Detect Earliest Reused API Payload Shape

Problem Description:
You are given a stream of API requests. Each request is represented as a list of
key-value pairs describing a JSON-like payload. The order of pairs inside a payload
is arbitrary, keys are unique within a single payload, and values are strings.

Two payloads are considered to have the same shape if:
1. They contain exactly the same set of keys.
2. For every key, the corresponding value has the same length in both payloads.

The actual value contents do not need to match.

Example:
[("user", "amy"), ("region", "us")] and [("region", "eu"), ("user", "bob")]
have the same shape because both contain keys {"user", "region"} and the value
length mapping is {"user": 3, "region": 2}.

Return the smallest index j such that there exists an earlier index i < j where
request i and request j have the same payload shape. If no such pair exists,
return -1.

Constraints:
- 1 <= n <= 200000
- 0 <= m_i <= 100
- Sum of all m_i over all requests <= 400000
- Keys and values contain lowercase English letters
- 1 <= key.length <= 20
- 0 <= value.length <= 100
- Keys are unique within a single request
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_signature(self, request: List[Tuple[str, str]]) -> Tuple[Tuple[str, int], ...]:
        """
        Build a canonical, hashable signature for one request.

        The signature must ignore pair order inside the payload, while still
        preserving exactly which keys exist and the length of each value.
        To achieve that, we convert each pair into (key, len(value)) and then
        sort the resulting list by key so that any two equivalent payload shapes
        produce the exact same tuple.

        Args:
            request: A single API request represented as a list of (key, value) pairs.

        Returns:
            A tuple of (key, value_length) pairs sorted by key. This tuple is
            hashable and can be used as a dictionary key.

        Time complexity:
            O(m log m), where m is the number of pairs in the request, due to sorting.

        Space complexity:
            O(m), for storing the normalized pairs.
        """
        # We create a normalized representation of the payload.
        #
        # Why normalization is necessary:
        # - The input order of pairs is arbitrary.
        # - Two payloads with the same shape may list keys in different orders.
        # - Therefore, we need a canonical form so equal shapes become identical
        #   Python objects when compared.
        #
        # Example:
        # [("user", "amy"), ("region", "us")]
        # becomes [("user", 3), ("region", 2)]
        #
        # [("region", "eu"), ("user", "bob")]
        # becomes [("region", 2), ("user", 3)]
        #
        # After sorting both by key, both become:
        # (("region", 2), ("user", 3))
        normalized_pairs: List[Tuple[str, int]] = []

        # Process each key-value pair in the request.
        for key, value in request:
            # We do not care about the actual value contents.
            # We only care about the length of the value because the problem
            # defines shape using key set + value lengths.
            normalized_pairs.append((key, len(value)))

        # Sort by key to remove any effect of original input order.
        normalized_pairs.sort()

        # Convert to tuple so it becomes immutable and hashable.
        # This allows us to store it in a dictionary/set efficiently.
        return tuple(normalized_pairs)

    def earliest_reused_payload_shape(
        self, requests: List[List[Tuple[str, str]]]
    ) -> int:
        """
        Return the smallest index whose payload shape has appeared earlier.

        The algorithm scans requests from left to right. For each request, it builds
        a canonical signature that uniquely represents the payload shape. A hash map
        stores signatures that have already been seen. The first time we encounter
        a signature that is already in the map, we immediately return the current
        index because we are scanning in increasing order, so this is the smallest
        possible repeated index.

        Args:
            requests: A list of requests, where each request is a list of
                (key, value) pairs.

        Returns:
            The smallest index j such that there exists i < j with the same
            payload shape, or -1 if no repeated shape exists.

        Time complexity:
            O(sum(m_i log m_i)) over all requests, because each request of size m_i
            is normalized by sorting its pairs.

        Space complexity:
            O(k), where k is the number of distinct signatures seen so far.
            In the worst case, this can be O(n + total_pairs_stored_in_signatures)).
        """
        # This dictionary records the first index where each signature appeared.
        #
        # Why a dictionary:
        # - We need fast average O(1) lookup to check whether a signature
        #   has been seen before.
        # - We also want to preserve earliest occurrence information.
        #
        # Mapping:
        # signature -> first index where it appeared
        seen_signatures: Dict[Tuple[Tuple[str, int], ...], int] = {}

        # Iterate through the requests in order.
        #
        # This left-to-right scan is crucial:
        # - The problem asks for the smallest index j that repeats an earlier shape.
        # - The first repeated signature we encounter while scanning is exactly
        #   that smallest j.
        for index, request in enumerate(requests):
            # Build the canonical signature for the current request.
            signature = self._build_signature(request)

            # If this signature has already been seen, then the current request
            # reuses an earlier payload shape.
            #
            # Because we are scanning from smallest index to largest index,
            # this current index is the earliest repeated index, so we can
            # return immediately.
            if signature in seen_signatures:
                return index

            # Otherwise, remember where this signature first appeared.
            #
            # We store only the first occurrence because the problem only needs
            # to know whether there exists an earlier matching request.
            seen_signatures[signature] = index

        # If we finish scanning all requests without finding a repeated signature,
        # then no payload shape was reused.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the problem statement.
    #
    # Request 0:
    # [("user","amy"),("region","us")]
    # Signature -> (("region", 2), ("user", 3))
    #
    # Request 1:
    # [("device","ios")]
    # Signature -> (("device", 3),)
    #
    # Request 2:
    # [("region","eu"),("user","bob")]
    # Signature -> (("region", 2), ("user", 3))
    #
    # This matches request 0, so the answer should be 2.
    requests1: List[List[Tuple[str, str]]] = [
        [("user", "amy"), ("region", "us")],
        [("device", "ios")],
        [("region", "eu"), ("user", "bob")],
        [("user", "anna"), ("region", "uk")],
    ]
    print(solution.earliest_reused_payload_shape(requests1))  # Expected: 2

    # Example 2 from the problem statement.
    #
    # Signatures:
    # 0 -> (("id", 1),)
    # 1 -> (("id", 2),)
    # 2 -> (("id", 3),)
    # 3 -> (("id", 2),)
    #
    # Request 3 repeats request 1's shape, so the answer should be 3.
    requests2: List[List[Tuple[str, str]]] = [
        [("id", "7")],
        [("id", "88")],
        [("id", "999")],
        [("id", "44")],
    ]
    print(solution.earliest_reused_payload_shape(requests2))  # Expected: 3

    # Additional sanity check: no repeated shapes.
    requests3: List[List[Tuple[str, str]]] = [
        [("a", "x")],
        [("a", "yy")],
        [("b", "z")],
        [("a", "yyy"), ("b", "q")],
    ]
    print(solution.earliest_reused_payload_shape(requests3))  # Expected: -1

    # Additional sanity check: empty payloads.
    #
    # Two empty payloads have the same shape because both contain no keys.
    requests4: List[List[Tuple[str, str]]] = [
        [],
        [("x", "a")],
        [],
    ]
    print(solution.earliest_reused_payload_shape(requests4))  # Expected: 2