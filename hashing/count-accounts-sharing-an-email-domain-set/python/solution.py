"""
Title: Count Accounts Sharing an Email Domain Set
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given a list of user accounts, where each account is represented by a user name
and a list of email addresses associated with that user. Two accounts are considered
equivalent if the set of email domains used by their addresses is exactly the same,
regardless of how many emails they have from each domain, the order of the emails,
or repeated addresses.

For example, an account with emails ["a@x.com", "b@y.com", "c@x.com"] has the domain
set {"x.com", "y.com"}. Another account with emails ["m@y.com", "n@x.com"] is
equivalent to it because the unique domain set matches.

Your task is to return the number of unordered pairs of accounts that are equivalent
in this sense.

An email domain is the substring after the '@' character. You may assume every email
contains exactly one '@' and has a non-empty local part and domain part. Account names
are not relevant for matching and should be ignored.

Because the input can be large, an efficient solution is required. A common approach
is to normalize each account into a canonical representation of its unique domain set,
then use hashing to count how many times each normalized set appears.

Constraints:
- 1 <= accounts.length <= 100000
- 1 <= emails[i].length <= 100000 across all accounts combined
- Each account contains at least 1 email
- Each email string length is between 3 and 100
- Domains consist of lowercase English letters, digits, dots, and hyphens
- The total number of emails across all accounts does not exceed 200000

Example 1:
Input:
accounts = [
    ["Alice", ["a@x.com", "b@y.com", "c@x.com"]],
    ["Bob", ["u@y.com", "v@x.com"]],
    ["Cara", ["k@z.com"]],
    ["Dan", ["p@z.com", "q@z.com"]]
]
Output: 2

Explanation:
- Alice -> {"x.com", "y.com"}
- Bob   -> {"x.com", "y.com"}
- Cara  -> {"z.com"}
- Dan   -> {"z.com"}

Equivalent unordered pairs:
- (Alice, Bob)
- (Cara, Dan)

So the answer is 2.

Example 2:
Input:
accounts = [
    ["Rita", ["r@a.io", "s@b.io"]],
    ["Sam", ["t@b.io", "u@a.io", "v@c.io"]],
    ["Tina", ["w@a.io", "x@b.io"]],
    ["Uma", ["y@c.io"]]
]
Output: 1

Explanation:
- Rita -> {"a.io", "b.io"}
- Sam  -> {"a.io", "b.io", "c.io"}
- Tina -> {"a.io", "b.io"}
- Uma  -> {"c.io"}

Equivalent unordered pair:
- (Rita, Tina)

So the answer is 1.
"""

from collections import defaultdict
from typing import DefaultDict, FrozenSet, List, Set


class Solution:
    def extract_domain(self, email: str) -> str:
        """
        Extract the domain part from a single email address.

        Args:
            email: A valid email string containing exactly one '@'.

        Returns:
            The substring after the '@' character.

        Time Complexity:
            O(len(email))

        Space Complexity:
            O(len(domain)) for the returned substring
        """
        # We split only once at the '@' character.
        # The problem guarantees exactly one '@', so this is safe.
        # Example:
        # "alice@x.com" -> ["alice", "x.com"] -> return "x.com"
        return email.split("@", 1)[1]

    def normalize_domain_set(self, emails: List[str]) -> FrozenSet[str]:
        """
        Convert a list of emails into a canonical representation of its unique domain set.

        Args:
            emails: List of email addresses for one account.

        Returns:
            A frozenset containing the unique domains used by the account.

        Time Complexity:
            O(total characters in emails for this account)

        Space Complexity:
            O(k), where k is the number of unique domains in this account
        """
        # We use a normal mutable set first because:
        # 1. We need uniqueness of domains.
        # 2. Order does not matter.
        # 3. Repeated emails or repeated domains should count only once.
        unique_domains: Set[str] = set()

        # Process every email in the account.
        for email in emails:
            # Extract the domain and add it to the set.
            # If the domain is already present, the set automatically ignores duplicates.
            domain: str = self.extract_domain(email)
            unique_domains.add(domain)

        # We convert the mutable set into a frozenset because:
        # - A normal set is not hashable, so it cannot be used as a dictionary key.
        # - A frozenset is immutable and hashable.
        # This gives us a canonical hashable representation of the account's domain set.
        return frozenset(unique_domains)

    def count_equivalent_pairs(self, accounts: List[List[object]]) -> int:
        """
        Count unordered pairs of accounts that share exactly the same email domain set.

        Args:
            accounts: A list where each element is:
                      [account_name, list_of_emails]

        Returns:
            The number of unordered equivalent account pairs.

        Time Complexity:
            O(T), where T is the total number of characters across all email strings,
            plus hashing overhead for domain-set keys. This is efficient for the given constraints.

        Space Complexity:
            O(A + D), where:
            - A is the number of accounts stored in the frequency map
            - D is the total number of unique domains across normalized account sets
        """
        # This dictionary counts how many accounts have each exact normalized domain set.
        #
        # Key:
        #   frozenset of domains, such as frozenset({"x.com", "y.com"})
        #
        # Value:
        #   how many accounts normalize to that exact set
        #
        # Example after processing some accounts:
        # {
        #   frozenset({"x.com", "y.com"}): 2,
        #   frozenset({"z.com"}): 2
        # }
        domain_set_count: DefaultDict[FrozenSet[str], int] = defaultdict(int)

        # Step 1: Normalize every account and count frequencies.
        for account in accounts:
            # The account format is [name, emails].
            # The name is irrelevant for matching, so we ignore account[0].
            emails: List[str] = account[1]  # type: ignore[assignment]

            # Convert this account's emails into its unique domain-set signature.
            normalized: FrozenSet[str] = self.normalize_domain_set(emails)

            # Increase the count for this signature.
            domain_set_count[normalized] += 1

        # Step 2: For each group of equivalent accounts, count unordered pairs.
        #
        # If a normalized domain set appears n times, then the number of unordered
        # pairs inside that group is:
        #
        #   n choose 2 = n * (n - 1) // 2
        #
        # Why?
        # Because every pair of distinct accounts in that group is equivalent.
        total_pairs: int = 0

        for count in domain_set_count.values():
            # Add the number of unordered pairs contributed by this group.
            total_pairs += count * (count - 1) // 2

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    accounts1: List[List[object]] = [
        ["Alice", ["a@x.com", "b@y.com", "c@x.com"]],
        ["Bob", ["u@y.com", "v@x.com"]],
        ["Cara", ["k@z.com"]],
        ["Dan", ["p@z.com", "q@z.com"]],
    ]
    result1: int = solution.count_equivalent_pairs(accounts1)
    print("Example 1 Output:", result1)  # Expected: 2

    # Example 2
    accounts2: List[List[object]] = [
        ["Rita", ["r@a.io", "s@b.io"]],
        ["Sam", ["t@b.io", "u@a.io", "v@c.io"]],
        ["Tina", ["w@a.io", "x@b.io"]],
        ["Uma", ["y@c.io"]],
    ]
    result2: int = solution.count_equivalent_pairs(accounts2)
    print("Example 2 Output:", result2)  # Expected: 1

    # Additional small sanity check:
    # Three accounts all normalize to {"m.com"}:
    # pairs = 3 choose 2 = 3
    accounts3: List[List[object]] = [
        ["A", ["a@m.com"]],
        ["B", ["b@m.com", "c@m.com"]],
        ["C", ["d@m.com"]],
    ]
    result3: int = solution.count_equivalent_pairs(accounts3)
    print("Additional Check Output:", result3)  # Expected: 3