"""
Title: Count Repeated Tag Signatures Across Articles

Problem Description:
You are given a list of articles, where each article is represented by a list of string tags.
Two articles are said to have the same tag signature if they contain exactly the same set of
distinct tags, regardless of order and regardless of duplicate occurrences inside the same article.

For example:
["ai", "cloud", "ai"] and ["cloud", "ai"] have the same signature because both reduce to
the set {"ai", "cloud"}.

Your task is to return the number of unordered pairs of articles that share the same tag signature.

In other words:
1. Normalize each article by removing duplicate tags within that article.
2. Ignore the order of tags.
3. Count how many pairs of articles become identical after normalization.

This problem is intended to test careful use of hashing for canonical representations.
A brute-force comparison of every pair of articles would be too slow for large inputs.
Instead, we build a compact canonical signature for each article and use a hash map to count
how many times each signature appears.

Constraints:
- 1 <= articles.length <= 100000
- 1 <= articles[i].length <= 20
- 1 <= tags[i][j].length <= 20
- Each tag consists of lowercase English letters only
- The answer may be large, so use 64-bit integer arithmetic

Example 1:
Input: articles = [["ai","cloud","ai"],["cloud","ai"],["ml"],["ml","ml"],["cloud"]]
Output: 2

Explanation:
- ["ai","cloud","ai"] -> {"ai","cloud"}
- ["cloud","ai"] -> {"ai","cloud"}
- ["ml"] -> {"ml"}
- ["ml","ml"] -> {"ml"}
- ["cloud"] -> {"cloud"}

Matching groups:
- {"ai","cloud"} appears 2 times -> contributes 1 pair
- {"ml"} appears 2 times -> contributes 1 pair
- {"cloud"} appears 1 time -> contributes 0 pairs

Total = 2

Example 2:
Input: articles = [["news","sports"],["sports","news","sports"],["finance"],["news"],["finance","finance"],["sports","news"]]
Output: 4

Explanation:
- ["news","sports"] -> {"news","sports"}
- ["sports","news","sports"] -> {"news","sports"}
- ["finance"] -> {"finance"}
- ["news"] -> {"news"}
- ["finance","finance"] -> {"finance"}
- ["sports","news"] -> {"news","sports"}

Matching groups:
- {"news","sports"} appears 3 times -> contributes 3 pairs
- {"finance"} appears 2 times -> contributes 1 pair
- {"news"} appears 1 time -> contributes 0 pairs

Total = 4
"""

from typing import Dict, List, Tuple


class Solution:
    def _build_signature(self, article: List[str]) -> Tuple[str, ...]:
        """
        Build a canonical hashable signature for one article.

        The signature must ignore:
        - duplicate tags inside the same article
        - the original order of tags

        We achieve this by:
        1. Converting the article list into a set to remove duplicates.
        2. Sorting the distinct tags so that all equivalent articles produce the same order.
        3. Converting the sorted result into a tuple so it can be used as a dictionary key.

        Args:
            article: A list of tags for a single article.

        Returns:
            A tuple containing the sorted distinct tags of the article.

        Time complexity:
            O(k log k), where k is the number of tags in the article.
            The set creation is O(k), and sorting the distinct tags is O(k log k).

        Space complexity:
            O(k) for the set and tuple of distinct tags.
        """
        # Step 1: Remove duplicates inside this article.
        # Example:
        # ["ai", "cloud", "ai"] becomes {"ai", "cloud"}
        distinct_tags = set(article)

        # Step 2: Sort the distinct tags.
        # Why sort?
        # Because sets do not preserve order, and we need a stable canonical representation.
        # For example:
        # {"ai", "cloud"} and {"cloud", "ai"} should both become ("ai", "cloud")
        sorted_tags = sorted(distinct_tags)

        # Step 3: Convert to tuple so it becomes immutable and hashable.
        # Lists cannot be dictionary keys, but tuples can.
        signature = tuple(sorted_tags)

        return signature

    def count_repeated_tag_signatures(self, articles: List[List[str]]) -> int:
        """
        Count the number of unordered pairs of articles that share the same normalized tag signature.

        The algorithm processes each article once, builds a canonical signature, and uses a hash map
        to count how many times each signature has already appeared.

        Key idea:
        If a signature has already appeared `c` times, then the next article with the same signature
        forms exactly `c` new unordered pairs with those previous articles.

        Example:
        Suppose signature X appears in this order:
        - 1st time: adds 0 pairs
        - 2nd time: adds 1 pair
        - 3rd time: adds 2 pairs
        Total = 0 + 1 + 2 = 3 pairs, which matches C(3, 2)

        Args:
            articles: A list where each element is a list of string tags for one article.

        Returns:
            The total number of unordered pairs of articles with the same tag signature.

        Time complexity:
            O(n * k log k), where:
            - n is the number of articles
            - k is the maximum number of tags in an article
            Each article is normalized by deduplicating and sorting its tags.

        Space complexity:
            O(n * k) in the worst case for storing all distinct signatures in the hash map.
        """
        # This dictionary maps:
        # signature -> how many articles with this signature we have seen so far
        #
        # Example:
        # {
        #   ("ai", "cloud"): 2,
        #   ("ml",): 1
        # }
        signature_count: Dict[Tuple[str, ...], int] = {}

        # This will store the final answer.
        # Python integers automatically handle large values, which is perfect for the
        # "use 64-bit integer arithmetic" requirement.
        total_pairs = 0

        # Process each article one by one.
        for article in articles:
            # Build the canonical representation of the article.
            signature = self._build_signature(article)

            # Look up how many times we have already seen this exact signature.
            # If we have seen it `seen_before` times, then this current article forms
            # `seen_before` new unordered pairs with those previous matching articles.
            seen_before = signature_count.get(signature, 0)

            # Add the number of new pairs contributed by this article.
            total_pairs += seen_before

            # Record that we have now seen one more article with this signature.
            signature_count[signature] = seen_before + 1

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    articles1 = [
        ["ai", "cloud", "ai"],
        ["cloud", "ai"],
        ["ml"],
        ["ml", "ml"],
        ["cloud"],
    ]
    result1 = solution.count_repeated_tag_signatures(articles1)
    print(result1)  # Expected: 2

    # Example 2
    articles2 = [
        ["news", "sports"],
        ["sports", "news", "sports"],
        ["finance"],
        ["news"],
        ["finance", "finance"],
        ["sports", "news"],
    ]
    result2 = solution.count_repeated_tag_signatures(articles2)
    print(result2)  # Expected: 4