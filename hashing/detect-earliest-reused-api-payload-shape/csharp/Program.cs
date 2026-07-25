/*
Title: Detect Earliest Reused API Payload Shape
Difficulty: Hard
Topic: Hashing

Problem Description:
You are given a stream of API requests. Each request is represented as a list of key-value pairs describing a JSON-like payload. The order of pairs inside a payload is arbitrary, keys are unique within a single payload, and values are strings. Two payloads are considered to have the same shape if they contain exactly the same set of keys, and for every key, the corresponding value has the same length as in the other payload. The actual value contents do not need to match.

For example, [("user","amy"),("region","us")] and [("region","eu"),("user","bob")] have the same shape because both contain keys {user, region} and the value lengths are {user: 3, region: 2}. However, [("user","amy")] and [("user","anna")] do not have the same shape because the value lengths differ.

Return the smallest index j such that there exists an earlier index i < j where request i and request j have the same payload shape. If no such pair exists, return -1. Indices are 0-based.

A correct solution must handle large inputs efficiently. A naive pairwise comparison of all payloads will time out. The intended approach is to build a canonical signature for each payload and use hashing to detect the first repeated signature while preserving earliest occurrence information.

Constraints:
- 1 <= n <= 200000, where n is the number of requests
- 0 <= m_i <= 100, where m_i is the number of key-value pairs in request i
- The sum of all m_i over all requests does not exceed 400000
- Each key and value consists of lowercase English letters
- 1 <= key.length <= 20
- 0 <= value.length <= 100
- Keys are unique within a single request
*/

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of requests.
    - Let M be the total number of key-value pairs across all requests.
    - For each request, we sort its pairs by key so that order differences do not matter.
    - If a request has m pairs, sorting costs O(m log m).
    - Therefore total time is O(sum of (m_i log m_i)).
    - Since each m_i <= 100, this is efficient in practice and easily fits the constraints.

    Space Complexity:
    - We store one canonical signature per distinct payload shape seen so far.
    - In the worst case, all requests are unique, so the dictionary may hold O(n) signatures.
    - The extra temporary space for sorting each request is O(m_i).
    */
    public int EarliestReusedPayloadShapeIndex(List<List<KeyValuePair<string, string>>> requests)
    {
        // This dictionary maps:
        //   canonical signature of a payload shape -> earliest index where we first saw it
        //
        // Why do we need this?
        // Because the problem asks for the smallest index j such that some earlier i < j
        // has the same shape. If we process requests from left to right:
        // - the first time we see a signature, we store its index
        // - the next time we see the same signature, we immediately know this current index
        //   is the earliest repeated index for that signature
        // - because we scan in increasing index order, the first repeated index we encounter
        //   overall is automatically the answer
        var firstSeenIndexBySignature = new Dictionary<string, int>();

        // We process requests in their original order because the answer depends on the
        // earliest repeated index j.
        for (int requestIndex = 0; requestIndex < requests.Count; requestIndex++)
        {
            var request = requests[requestIndex];

            // Build a canonical signature for the current request.
            //
            // "Canonical" means:
            // no matter how the pairs are ordered in the input, two payloads with the same
            // shape must produce exactly the same signature string.
            //
            // Since pair order is arbitrary, we must normalize the order.
            // The simplest and safest way is:
            // 1. sort pairs by key
            // 2. for each pair, record (key, value.Length)
            //
            // We do NOT use the actual value text, because the problem says only value length matters.
            string signature = BuildCanonicalSignature(request);

            // If this signature already exists in the dictionary, then we have found an earlier
            // request with the same shape.
            //
            // Because we are scanning from left to right, this current requestIndex is the
            // smallest possible j that repeats any earlier signature.
            if (firstSeenIndexBySignature.ContainsKey(signature))
            {
                return requestIndex;
            }

            // Otherwise, this is the first time we have seen this shape.
            // Store its earliest occurrence.
            firstSeenIndexBySignature[signature] = requestIndex;
        }

        // If we finish scanning all requests without finding any repeated signature,
        // then no payload shape was reused.
        return -1;
    }

    private string BuildCanonicalSignature(List<KeyValuePair<string, string>> request)
    {
        // Special case:
        // An empty payload has a valid shape too.
        // All empty payloads should map to the same signature.
        if (request.Count == 0)
        {
            return "#EMPTY";
        }

        // We sort by key so that:
        // [("user","amy"),("region","us")]
        // and
        // [("region","eu"),("user","bob")]
        // become aligned in the same order before signature creation.
        //
        // Without sorting, two equivalent shapes could produce different signatures simply
        // because their input pair order differs.
        var sortedPairs = request
            .OrderBy(pair => pair.Key, StringComparer.Ordinal)
            .ToList();

        // StringBuilder is used because it is efficient for constructing strings from many parts.
        // We create a compact but unambiguous representation.
        //
        // Example:
        // key="region", value length=2
        // key="user",   value length=3
        //
        // signature could become:
        // "6:region=2|4:user=3|"
        //
        // Including key length and separators makes the representation unambiguous.
        // This avoids accidental collisions like:
        //   key="ab", len=12
        // versus
        //   key="a",  len represented next to "b..."
        var signatureBuilder = new StringBuilder();

        foreach (var pair in sortedPairs)
        {
            // Current step:
            // Append one normalized component for this key-value pair.
            //
            // Why this exact information?
            // - pair.Key identifies which field exists
            // - pair.Value.Length captures the only value property that matters
            // - actual pair.Value text is intentionally ignored
            signatureBuilder.Append(pair.Key.Length);
            signatureBuilder.Append(':');
            signatureBuilder.Append(pair.Key);
            signatureBuilder.Append('=');
            signatureBuilder.Append(pair.Value.Length);
            signatureBuilder.Append('|');
        }

        return signatureBuilder.ToString();
    }
}

// Demo code

var solution = new Solution();

// Example 1:
// requests = [
//   [("user","amy"),("region","us")],
//   [("device","ios")],
//   [("region","eu"),("user","bob")],
//   [("user","anna"),("region","uk")]
// ]
//
// Signatures:
// 0 -> {(region,2),(user,3)}
// 1 -> {(device,3)}
// 2 -> {(region,2),(user,3)}  <-- repeats request 0, so answer is 2
// 3 -> {(region,2),(user,4)}  <-- different from request 0 because user length is 4
var requests1 = new List<List<KeyValuePair<string, string>>>
{
    new()
    {
        new("user", "amy"),
        new("region", "us")
    },
    new()
    {
        new("device", "ios")
    },
    new()
    {
        new("region", "eu"),
        new("user", "bob")
    },
    new()
    {
        new("user", "anna"),
        new("region", "uk")
    }
};

int result1 = solution.EarliestReusedPayloadShapeIndex(requests1);
Console.WriteLine(result1); // Expected: 2

// Example 2:
// requests = [
//   [("id","7")],
//   [("id","88")],
//   [("id","999")],
//   [("id","44")]
// ]
//
// Signatures by value length:
// 0 -> (id,1)
// 1 -> (id,2)
// 2 -> (id,3)
// 3 -> (id,2)  <-- repeats request 1, so answer is 3
var requests2 = new List<List<KeyValuePair<string, string>>>
{
    new()
    {
        new("id", "7")
    },
    new()
    {
        new("id", "88")
    },
    new()
    {
        new("id", "999")
    },
    new()
    {
        new("id", "44")
    }
};

int result2 = solution.EarliestReusedPayloadShapeIndex(requests2);
Console.WriteLine(result2); // Expected: 3

// Additional quick demo: no repeated shape
var requests3 = new List<List<KeyValuePair<string, string>>>
{
    new()
    {
        new("a", "x")
    },
    new()
    {
        new("a", "yy")
    },
    new()
    {
        new("b", "z")
    }
};

int result3 = solution.EarliestReusedPayloadShapeIndex(requests3);
Console.WriteLine(result3); // Expected: -1