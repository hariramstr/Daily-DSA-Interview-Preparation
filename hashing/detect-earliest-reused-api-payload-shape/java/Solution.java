import java.util.*;

/*
 * Title: Detect Earliest Reused API Payload Shape
 * Difficulty: Hard
 * Topic: Hashing
 *
 * Problem Description:
 * You are given a stream of API requests. Each request is represented as a list of key-value pairs
 * describing a JSON-like payload. The order of pairs inside a payload is arbitrary, keys are unique
 * within a single payload, and values are strings. Two payloads are considered to have the same shape
 * if they contain exactly the same set of keys, and for every key, the corresponding value has the same
 * length as in the other payload. The actual value contents do not need to match.
 *
 * For example, [("user","amy"),("region","us")] and [("region","eu"),("user","bob")] have the same shape
 * because both contain keys {user, region} and the value lengths are {user: 3, region: 2}. However,
 * [("user","amy")] and [("user","anna")] do not have the same shape because the value lengths differ.
 *
 * Return the smallest index j such that there exists an earlier index i < j where request i and request j
 * have the same payload shape. If no such pair exists, return -1. Indices are 0-based.
 *
 * A correct solution must handle large inputs efficiently. A naive pairwise comparison of all payloads
 * will time out. The intended approach is to build a canonical signature for each payload and use hashing
 * to detect the first repeated signature while preserving earliest occurrence information.
 *
 * Constraints:
 * - 1 <= n <= 200000, where n is the number of requests
 * - 0 <= m_i <= 100, where m_i is the number of key-value pairs in request i
 * - The sum of all m_i over all requests does not exceed 400000
 * - Each key and value consists of lowercase English letters
 * - 1 <= key.length <= 20
 * - 0 <= value.length <= 100
 * - Keys are unique within a single request
 *
 * Example 1:
 * Input: requests = [[("user","amy"),("region","us")],[("device","ios")],[("region","eu"),("user","bob")],[("user","anna"),("region","uk")]]
 * Output: 2
 * Explanation: Request 0 has signature {(region,2),(user,3)}. Request 2 has the same signature, so the earliest
 * repeated payload shape appears at index 2.
 *
 * Example 2:
 * Input: requests = [[("id","7")],[("id","88")],[("id","999")],[("id","44")]]
 * Output: 3
 * Explanation: Signatures by value length are (id,1), (id,2), (id,3), (id,2). The first index that repeats any
 * earlier signature is 3, matching request 1.
 */

public class Solution {

    /**
     * Simple data holder representing one key-value pair inside a request payload.
     */
    public static class Pair {
        String key;
        String value;

        /**
         * Constructs a key-value pair.
         *
         * @param key the payload field name
         * @param value the payload field value
         */
        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    /**
     * Finds the smallest index j such that there exists an earlier index i < j where
     * request i and request j have the same payload shape.
     *
     * The key idea is:
     * 1. Convert each request into a canonical signature that depends only on:
     *    - which keys are present
     *    - the length of each corresponding value
     * 2. Because pair order inside a request is arbitrary, we sort the request's pairs by key
     *    before building the signature.
     * 3. We store each signature in a hash map the first time we see it.
     * 4. As soon as we encounter a signature already present in the map, we have found the
     *    earliest repeated index j, because we scan requests from left to right.
     *
     * Correctness on Example 1:
     * - Request 0 => sorted pairs: (region,2),(user,3)
     * - Request 1 => (device,3)
     * - Request 2 => sorted pairs: (region,2),(user,3) => repeats request 0 => answer 2
     *
     * Correctness on Example 2:
     * - Request 0 => (id,1)
     * - Request 1 => (id,2)
     * - Request 2 => (id,3)
     * - Request 3 => (id,2) => repeats request 1 => answer 3
     *
     * @param requests a list of requests; each request is a list of key-value pairs
     * @return the smallest index j whose payload shape appeared earlier, or -1 if none exists
     * Time complexity: O(totalPairs * log M + totalStringChars), where M is the maximum number of pairs
     * in a single request (at most 100). Since M is small, this is efficient for the constraints.
     * Space complexity: O(number of distinct signatures + size of generated signatures)
     */
    public int earliestReusedPayloadShape(List<List<Pair>> requests) {
        // This map stores the first index where each canonical signature appeared.
        // Key   = canonical signature string
        // Value = earliest index where that signature was seen
        Map<String, Integer> firstSeenIndex = new HashMap<>();

        // We process requests from left to right.
        // The first time we find a repeated signature, that current index is guaranteed
        // to be the smallest possible answer because we are scanning in increasing order.
        for (int i = 0; i < requests.size(); i++) {
            // Build a canonical representation of the current request.
            String signature = buildSignature(requests.get(i));

            // If this exact signature has been seen before, then the current request
            // matches an earlier request in payload shape.
            if (firstSeenIndex.containsKey(signature)) {
                return i;
            }

            // Otherwise, remember the first occurrence of this signature.
            firstSeenIndex.put(signature, i);
        }

        // If we finish scanning all requests without finding any repeated signature,
        // then no payload shape was reused.
        return -1;
    }

    /**
     * Builds a canonical signature for one request.
     *
     * Two requests must produce exactly the same signature if and only if:
     * - they contain the same set of keys
     * - for each key, the corresponding value lengths are equal
     *
     * Since input pair order is arbitrary, we must normalize the order.
     * We do that by sorting pairs by key, then appending:
     *   key + '#' + valueLength + ';'
     *
     * Example:
     *   [("user","amy"),("region","us")]
     * After sorting by key:
     *   [("region","us"),("user","amy")]
     * Signature:
     *   "region#2;user#3;"
     *
     * Empty request:
     *   []
     * Signature:
     *   ""
     *
     * @param request one request payload represented as a list of key-value pairs
     * @return a canonical string signature for hashing and equality comparison
     * Time complexity: O(m log m + total characters in keys), where m is request size
     * Space complexity: O(m + signature length)
     */
    public String buildSignature(List<Pair> request) {
        // We create a copy so that we do not mutate the caller's input order.
        List<Pair> sorted = new ArrayList<>(request);

        // Sort by key so that requests with the same logical content but different pair order
        // produce the same final signature.
        sorted.sort(Comparator.comparing(p -> p.key));

        // Build the canonical string.
        // We include separators to avoid ambiguity.
        StringBuilder sb = new StringBuilder();

        // Step through the sorted pairs one by one.
        for (Pair pair : sorted) {
            // Append the key.
            sb.append(pair.key);

            // Append a separator between key and value length.
            sb.append('#');

            // Append only the length of the value, not the value itself,
            // because the problem defines shape using value lengths.
            sb.append(pair.value.length());

            // Append a separator between entries.
            sb.append(';');
        }

        return sb.toString();
    }

    /**
     * Convenience helper that accepts a 3D string array and converts it into the required
     * request structure, then computes the answer.
     *
     * Input format:
     * requests[i][k][0] = key
     * requests[i][k][1] = value
     *
     * Example:
     * {
     *   { {"user","amy"}, {"region","us"} },
     *   { {"device","ios"} }
     * }
     *
     * @param rawRequests requests encoded as nested string arrays
     * @return the smallest repeated-shape index, or -1 if none exists
     * Time complexity: O(totalPairs * log M + totalStringChars)
     * Space complexity: O(totalPairs + distinct signatures)
     */
    public int earliestReusedPayloadShape(String[][][] rawRequests) {
        List<List<Pair>> requests = new ArrayList<>();

        // Convert each raw request into a list of Pair objects.
        for (String[][] rawRequest : rawRequests) {
            List<Pair> request = new ArrayList<>();
            for (String[] entry : rawRequest) {
                request.add(new Pair(entry[0], entry[1]));
            }
            requests.add(request);
        }

        return earliestReusedPayloadShape(requests);
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 => 2
     * Example 2 => 3
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(totalPairs * log M) for the demonstrated examples
     * Space complexity: O(totalPairs)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Request 0: [("user","amy"),("region","us")]   => signature "region#2;user#3;"
        // Request 1: [("device","ios")]                 => signature "device#3;"
        // Request 2: [("region","eu"),("user","bob")]   => signature "region#2;user#3;" repeats request 0
        // Request 3: [("user","anna"),("region","uk")]  => signature "region#2;user#4;" not same as request 0
        // Therefore the earliest repeated index is 2.
        String[][][] example1 = {
            { {"user", "amy"}, {"region", "us"} },
            { {"device", "ios"} },
            { {"region", "eu"}, {"user", "bob"} },
            { {"user", "anna"}, {"region", "uk"} }
        };

        // Example 2:
        // Signatures:
        // 0 -> "id#1;"
        // 1 -> "id#2;"
        // 2 -> "id#3;"
        // 3 -> "id#2;" repeats request 1
        // Therefore the earliest repeated index is 3.
        String[][][] example2 = {
            { {"id", "7"} },
            { {"id", "88"} },
            { {"id", "999"} },
            { {"id", "44"} }
        };

        System.out.println(solution.earliestReusedPayloadShape(example1)); // Expected: 2
        System.out.println(solution.earliestReusedPayloadShape(example2)); // Expected: 3
    }
}