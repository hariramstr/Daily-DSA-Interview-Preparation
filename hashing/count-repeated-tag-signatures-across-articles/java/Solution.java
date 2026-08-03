import java.util.*;

/*
 * Title: Count Repeated Tag Signatures Across Articles
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a list of articles, where each article is represented by a list of string tags.
 * Two articles are said to have the same tag signature if they contain exactly the same set of
 * distinct tags, regardless of order and regardless of duplicate occurrences inside the same article.
 * For example, ["ai", "cloud", "ai"] and ["cloud", "ai"] have the same signature because both reduce
 * to the set {"ai", "cloud"}.
 *
 * Your task is to return the number of unordered pairs of articles that share the same tag signature.
 *
 * In other words, normalize each article by removing duplicate tags within that article and ignoring order,
 * then count how many pairs of articles become identical after normalization.
 *
 * This problem is intended to test careful use of hashing for canonical representations. A brute-force
 * comparison of every pair of articles will be too slow for large inputs. Instead, you should build a
 * compact signature for each article and use a hash map to count how many times each signature appears.
 *
 * Constraints:
 * - 1 <= articles.length <= 100000
 * - 1 <= articles[i].length <= 20
 * - 1 <= tags[i][j].length <= 20
 * - Each tag consists of lowercase English letters only
 * - The answer may be large, so use 64-bit integer arithmetic
 *
 * Example 1:
 * Input: articles = [["ai","cloud","ai"],["cloud","ai"],["ml"],["ml","ml"],["cloud"]]
 * Output: 2
 * Explanation:
 * The first two articles share the signature {"ai","cloud"}.
 * The third and fourth articles share the signature {"ml"}.
 * So there are 2 valid unordered pairs.
 *
 * Example 2:
 * Input: articles = [["news","sports"],["sports","news","sports"],["finance"],["news"],["finance","finance"],["sports","news"]]
 * Output: 4
 * Explanation:
 * Three articles reduce to the signature {"news","sports"}, contributing 3 unordered pairs.
 * Two articles reduce to {"finance"}, contributing 1 unordered pair.
 * Total = 4.
 */

public class Solution {

    /**
     * Counts the number of unordered pairs of articles that share the same normalized tag signature.
     *
     * The key idea is:
     * 1. For each article, remove duplicate tags inside that article.
     * 2. Sort the remaining distinct tags so that order does not matter.
     * 3. Convert that normalized set into a canonical string signature.
     * 4. Use a hash map to count how many times each signature has appeared.
     * 5. When we see a signature again, every previous article with the same signature forms a new pair
     *    with the current article.
     *
     * Example:
     * If a signature has already appeared 3 times, then the 4th occurrence creates 3 new pairs.
     *
     * @param articles a list of articles, where each article is a list of string tags
     * @return the number of unordered pairs of articles with identical tag signatures
     * Time complexity: O(n * k log k), where n is the number of articles and k is the number of tags
     * per article (k <= 20), due to sorting distinct tags for each article
     * Space complexity: O(n * k) in the worst case for storing signatures in the hash map
     */
    public long countRepeatedTagSignatures(List<List<String>> articles) {
        // This map stores:
        // key   = canonical signature of an article
        // value = how many previous articles had exactly this signature
        Map<String, Long> signatureCount = new HashMap<>();

        // We use long because the number of pairs can be large.
        long pairs = 0L;

        // Process each article one by one.
        for (List<String> article : articles) {
            // Convert the article into a canonical signature that ignores:
            // - duplicate tags inside the article
            // - order of tags
            String signature = buildSignature(article);

            // Find how many times we have already seen this exact signature.
            long seenBefore = signatureCount.getOrDefault(signature, 0L);

            // If we have seen it 'seenBefore' times, then the current article forms
            // exactly 'seenBefore' new unordered pairs with those previous articles.
            pairs += seenBefore;

            // Record that we have now seen one more article with this signature.
            signatureCount.put(signature, seenBefore + 1);
        }

        return pairs;
    }

    /**
     * Builds a canonical signature for one article.
     *
     * The signature must be identical for all articles that reduce to the same set of distinct tags.
     *
     * Steps:
     * 1. Put all tags into a set to remove duplicates within the article.
     * 2. Move the distinct tags into a list.
     * 3. Sort the list so that order becomes consistent.
     * 4. Join the sorted tags with a delimiter that cannot appear in tags.
     *
     * Since tags contain only lowercase English letters, using a character like '#'
     * as a separator is safe and unambiguous.
     *
     * Example:
     * ["cloud", "ai", "ai"] -> distinct set {"cloud", "ai"} -> sorted ["ai", "cloud"]
     * -> signature "ai#cloud#"
     *
     * @param article the list of tags for one article
     * @return a canonical string signature representing the set of distinct tags in the article
     * Time complexity: O(k log k), where k is the number of tags in the article
     * Space complexity: O(k), for the set, list, and resulting signature
     */
    public String buildSignature(List<String> article) {
        // Step 1: Remove duplicates inside this single article.
        // A HashSet keeps only distinct tags.
        Set<String> distinctTags = new HashSet<>(article);

        // Step 2: Convert the set into a list so we can sort it.
        List<String> sortedTags = new ArrayList<>(distinctTags);

        // Step 3: Sort alphabetically to make the representation independent of original order.
        Collections.sort(sortedTags);

        // Step 4: Build a single canonical string.
        // We append a separator after each tag to avoid ambiguity.
        // For example, ["ab", "c"] becomes "ab#c#" which is different from ["a", "bc"] -> "a#bc#".
        StringBuilder signature = new StringBuilder();
        for (String tag : sortedTags) {
            signature.append(tag).append('#');
        }

        return signature.toString();
    }

    /**
     * Convenience overload for callers using a 2D string array instead of List<List<String>>.
     *
     * @param articles a 2D array where each row represents one article's tags
     * @return the number of unordered pairs of articles with identical tag signatures
     * Time complexity: O(n * k log k), where n is the number of articles and k is the number of tags
     * per article
     * Space complexity: O(n * k), due to stored signatures in the map
     */
    public long countRepeatedTagSignatures(String[][] articles) {
        List<List<String>> articleList = new ArrayList<>(articles.length);

        // Convert each String[] article into a List<String>.
        for (String[] article : articles) {
            articleList.add(Arrays.asList(article));
        }

        return countRepeatedTagSignatures(articleList);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It prints the computed results and also prints the expected values so the output
     * can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo size, or more generally the same as the called methods
     * Space complexity: O(1) for the fixed demo size, excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample Input 1:
        // [["ai","cloud","ai"],["cloud","ai"],["ml"],["ml","ml"],["cloud"]]
        //
        // Normalized signatures:
        // 1. {"ai","cloud"}
        // 2. {"ai","cloud"}  -> matches article 1
        // 3. {"ml"}
        // 4. {"ml"}          -> matches article 3
        // 5. {"cloud"}
        //
        // Total pairs = 2
        String[][] articles1 = {
            {"ai", "cloud", "ai"},
            {"cloud", "ai"},
            {"ml"},
            {"ml", "ml"},
            {"cloud"}
        };

        long result1 = solution.countRepeatedTagSignatures(articles1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Example 1 Expected: 2");

        // Sample Input 2:
        // [["news","sports"],["sports","news","sports"],["finance"],["news"],["finance","finance"],["sports","news"]]
        //
        // Normalized signatures:
        // 1. {"news","sports"}
        // 2. {"news","sports"} -> pair with 1
        // 3. {"finance"}
        // 4. {"news"}
        // 5. {"finance"}       -> pair with 3
        // 6. {"news","sports"} -> pairs with 1 and 2
        //
        // Signature {"news","sports"} appears 3 times => 3 choose 2 = 3 pairs
        // Signature {"finance"} appears 2 times => 2 choose 2 = 1 pair
        // Total = 4
        String[][] articles2 = {
            {"news", "sports"},
            {"sports", "news", "sports"},
            {"finance"},
            {"news"},
            {"finance", "finance"},
            {"sports", "news"}
        };

        long result2 = solution.countRepeatedTagSignatures(articles2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Example 2 Expected: 4");
    }
}