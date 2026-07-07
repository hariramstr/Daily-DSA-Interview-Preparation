import java.util.*;

/*
 * Title: Count Accounts Sharing an Email Domain Set
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of user accounts, where each account is represented by a user name
 * and a list of email addresses associated with that user. Two accounts are considered
 * equivalent if the set of email domains used by their addresses is exactly the same,
 * regardless of how many emails they have from each domain, the order of the emails,
 * or repeated addresses.
 *
 * For example, an account with emails ["a@x.com", "b@y.com", "c@x.com"] has the domain set
 * {"x.com", "y.com"}. Another account with emails ["m@y.com", "n@x.com"] is equivalent to it
 * because the unique domain set matches.
 *
 * Your task is to return the number of unordered pairs of accounts that are equivalent in this sense.
 *
 * An email domain is the substring after the '@' character. You may assume every email contains
 * exactly one '@' and has a non-empty local part and domain part. Account names are not relevant
 * for matching and should be ignored.
 *
 * Because the input can be large, an efficient solution is required. A common approach is to
 * normalize each account into a canonical representation of its unique domain set, then use hashing
 * to count how many times each normalized set appears.
 *
 * Constraints:
 * - 1 <= accounts.length <= 100000
 * - 1 <= emails[i].length <= 100000 across all accounts combined
 * - Each account contains at least 1 email
 * - Each email string length is between 3 and 100
 * - Domains consist of lowercase English letters, digits, dots, and hyphens
 * - The total number of emails across all accounts does not exceed 200000
 *
 * Example 1:
 * Input:
 * accounts = [
 *   ["Alice", ["a@x.com", "b@y.com", "c@x.com"]],
 *   ["Bob",   ["u@y.com", "v@x.com"]],
 *   ["Cara",  ["k@z.com"]],
 *   ["Dan",   ["p@z.com", "q@z.com"]]
 * ]
 * Output: 2
 *
 * Explanation:
 * Alice and Bob share the same domain set {x.com, y.com}.
 * Cara and Dan share the same domain set {z.com}.
 * So there are 2 unordered equivalent pairs.
 *
 * Example 2:
 * Input:
 * accounts = [
 *   ["Rita", ["r@a.io", "s@b.io"]],
 *   ["Sam",  ["t@b.io", "u@a.io", "v@c.io"]],
 *   ["Tina", ["w@a.io", "x@b.io"]],
 *   ["Uma",  ["y@c.io"]]
 * ]
 * Output: 1
 *
 * Explanation:
 * Rita and Tina both normalize to the domain set {a.io, b.io}.
 * Sam has {a.io, b.io, c.io}, and Uma has {c.io}, so no other pairs match.
 */

public class Solution {

    /**
     * Counts the number of unordered pairs of accounts that have exactly the same set of email domains.
     *
     * The algorithm works as follows:
     * 1. For each account, ignore the account name.
     * 2. Extract the domain from every email.
     * 3. Put the domains into a set so duplicates within the same account are removed.
     * 4. Convert that set into a canonical normalized form by sorting the unique domains and joining them.
     * 5. Use a hash map to count how many accounts share the same normalized domain-set key.
     * 6. For each key that appears k times, it contributes k * (k - 1) / 2 unordered pairs.
     *
     * @param accounts the list of accounts, where each account is represented as a list:
     *                 index 0 is the user name, and indices 1..n are email addresses
     * @return the number of unordered equivalent account pairs
     * Time complexity: O(E * DlogD) in the worst case, where E is the total number of emails
     * and D is the number of unique domains in an account due to sorting per account.
     * More precisely, total work is proportional to extracting all domains plus sorting each account's unique domains.
     * Space complexity: O(U), where U is the total number of distinct normalized domain-set keys stored in the map,
     * plus temporary storage for per-account unique domains.
     */
    public long countEquivalentAccounts(List<List<String>> accounts) {
        // This map stores:
        // key   -> canonical representation of a unique domain set
        // value -> how many accounts have exactly that domain set
        Map<String, Integer> frequencyByDomainSet = new HashMap<>();

        // Process every account one by one.
        for (List<String> account : accounts) {
            // Convert the current account into a canonical key that represents
            // its unique set of domains, ignoring:
            // - the account name
            // - duplicate emails
            // - duplicate domains
            // - email order
            String normalizedKey = buildNormalizedDomainSetKey(account);

            // Increase the count for this normalized domain set.
            frequencyByDomainSet.put(normalizedKey, frequencyByDomainSet.getOrDefault(normalizedKey, 0) + 1);
        }

        long pairs = 0L;

        // For every group of equivalent accounts:
        // if a normalized domain set appears k times,
        // then the number of unordered pairs inside that group is:
        // k choose 2 = k * (k - 1) / 2
        for (int count : frequencyByDomainSet.values()) {
            pairs += (long) count * (count - 1) / 2;
        }

        return pairs;
    }

    /**
     * Builds a canonical string key for one account's unique domain set.
     *
     * Important details:
     * - The account name at index 0 is ignored.
     * - Only the domain part after '@' is used.
     * - Duplicate domains inside the same account are removed.
     * - The remaining unique domains are sorted so that different input orders
     *   produce the exact same key.
     *
     * Example:
     * Account = ["Alice", "a@x.com", "b@y.com", "c@x.com"]
     * Unique domains = {"x.com", "y.com"}
     * Sorted domains = ["x.com", "y.com"]
     * Key = "x.com|y.com"
     *
     * @param account one account represented as a list where index 0 is the name
     *                and indices 1..n are email addresses
     * @return a canonical normalized string representing the account's unique domain set
     * Time complexity: O(m + dlogd), where m is the number of emails in the account
     * and d is the number of unique domains in the account
     * Space complexity: O(d) for storing unique domains and the sorted list
     */
    public String buildNormalizedDomainSetKey(List<String> account) {
        // Use a set to keep only unique domains for this account.
        Set<String> uniqueDomains = new HashSet<>();

        // Start from index 1 because index 0 is the account name and must be ignored.
        for (int i = 1; i < account.size(); i++) {
            String email = account.get(i);

            // Extract the domain part after '@'.
            String domain = extractDomain(email);

            // Add to the set. If the same domain appears multiple times in this account,
            // the set automatically keeps only one copy.
            uniqueDomains.add(domain);
        }

        // Convert the set to a list so we can sort it.
        List<String> sortedDomains = new ArrayList<>(uniqueDomains);

        // Sorting is the key step that makes the representation canonical.
        // Without sorting, the same set in different orders would produce different keys.
        Collections.sort(sortedDomains);

        // Join the sorted domains into one string using a separator.
        // The separator "|" is safe here because valid domains contain lowercase letters,
        // digits, dots, and hyphens, so "|" will not appear inside a domain.
        return String.join("|", sortedDomains);
    }

    /**
     * Extracts the domain part from an email address.
     *
     * Example:
     * "alice@x.com" -> "x.com"
     *
     * The problem guarantees that every email contains exactly one '@'
     * and both local part and domain part are non-empty.
     *
     * @param email the full email address
     * @return the substring after the '@' character
     * Time complexity: O(L), where L is the length of the email string
     * Space complexity: O(L) in Java due to substring object creation
     */
    public String extractDomain(String email) {
        int atIndex = email.indexOf('@');
        return email.substring(atIndex + 1);
    }

    /**
     * Builds sample input for Example 1 from the problem statement.
     *
     * @return a list of accounts formatted for this solution
     * Time complexity: O(1)
     * Space complexity: O(1) excluding the returned data structure itself
     */
    public static List<List<String>> buildExample1() {
        List<List<String>> accounts = new ArrayList<>();

        accounts.add(Arrays.asList("Alice", "a@x.com", "b@y.com", "c@x.com"));
        accounts.add(Arrays.asList("Bob", "u@y.com", "v@x.com"));
        accounts.add(Arrays.asList("Cara", "k@z.com"));
        accounts.add(Arrays.asList("Dan", "p@z.com", "q@z.com"));

        return accounts;
    }

    /**
     * Builds sample input for Example 2 from the problem statement.
     *
     * @return a list of accounts formatted for this solution
     * Time complexity: O(1)
     * Space complexity: O(1) excluding the returned data structure itself
     */
    public static List<List<String>> buildExample2() {
        List<List<String>> accounts = new ArrayList<>();

        accounts.add(Arrays.asList("Rita", "r@a.io", "s@b.io"));
        accounts.add(Arrays.asList("Sam", "t@b.io", "u@a.io", "v@c.io"));
        accounts.add(Arrays.asList("Tina", "w@a.io", "x@b.io"));
        accounts.add(Arrays.asList("Uma", "y@c.io"));

        return accounts;
    }

    /**
     * Demonstrates the solution on the sample inputs and prints the results.
     *
     * Expected output:
     * Example 1 result: 2
     * Example 2 result: 1
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: Depends on the sample sizes passed to the counting method
     * Space complexity: Depends on the sample sizes passed to the counting method
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        List<List<String>> example1 = buildExample1();
        long result1 = solution.countEquivalentAccounts(example1);
        System.out.println("Example 1 result: " + result1);

        List<List<String>> example2 = buildExample2();
        long result2 = solution.countEquivalentAccounts(example2);
        System.out.println("Example 2 result: " + result2);

        // Additional small sanity check:
        // Three accounts all normalize to {a.com, b.com}, so the number of unordered pairs is 3.
        // Pairs are: (1,2), (1,3), (2,3)
        List<List<String>> extra = new ArrayList<>();
        extra.add(Arrays.asList("User1", "x@a.com", "y@b.com", "z@a.com"));
        extra.add(Arrays.asList("User2", "m@b.com", "n@a.com"));
        extra.add(Arrays.asList("User3", "p@a.com", "q@b.com"));
        extra.add(Arrays.asList("User4", "r@c.com"));

        long extraResult = solution.countEquivalentAccounts(extra);
        System.out.println("Extra example result: " + extraResult);
    }
}