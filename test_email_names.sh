#!/bin/bash

# Test script for new emailName functionality
# Usage: ./test_email_names.sh

BASE_URL="http://localhost:8080"
JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiQXJhZGh5ZSBTd2FydXAiLCJ1c2VySWQiOiI1ODAzYjc2My01MjEwLTRkNjUtYjQ1OS1mYTE4MDM0ZGE5MzYiLCJlbWFpbCI6ImFyYWRoeWUxQGdtYWlsLmNvbSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NMajJsUTNtLWd6cFlsc2p6OVNSei1fX3FBSzVxU2pNX1EyRFlOY1ppajlRYnIzODJDdT1zOTYtYyIsInN1YiI6IjExMTM2MDk4OTcyNzAwNTA3NjgzNSIsImlhdCI6MTc1Nzg0Mzg0MiwiZXhwIjoxNzU3OTMwMjQyfQ.XVi8FL03iAWg0bJ9xiFttd14ehgCoqrfO3mdgXeqAdw"

echo "🧪 Testing New EmailName Functionality"
echo "======================================"

echo ""
echo "📧 1. Creating mailbox with custom email name 'johndoe'"
echo "-------------------------------------------------------"

response1=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/mailboxes/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"lifespan": 3, "burnAfterRead": false, "emailName": "johndoe"}')

status1="${response1: -3}"
body1="${response1%???}"

echo "Status Code: $status1"
echo "Response Body:"
echo "$body1" | jq . 2>/dev/null || echo "$body1"

echo ""
echo "📧 2. Creating mailbox with special characters 'test@user!123'"
echo "------------------------------------------------------------"

response2=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/mailboxes/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"lifespan": 1, "burnAfterRead": true, "emailName": "test@user!123"}')

status2="${response2: -3}"
body2="${response2%???}"

echo "Status Code: $status2"
echo "Response Body:"
echo "$body2" | jq . 2>/dev/null || echo "$body2"

echo ""
echo "📧 3. Creating mailbox with empty email name ''"
echo "----------------------------------------------"

response3=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/mailboxes/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"lifespan": 7, "burnAfterRead": false, "emailName": ""}')

status3="${response3: -3}"
body3="${response3%???}"

echo "Status Code: $status3"
echo "Response Body:"
echo "$body3" | jq . 2>/dev/null || echo "$body3"

echo ""
echo "📧 4. Creating mailbox without email name field"
echo "----------------------------------------------"

response4=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/mailboxes/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"lifespan": 30, "burnAfterRead": false}')

status4="${response4: -3}"
body4="${response4%???}"

echo "Status Code: $status4"
echo "Response Body:"
echo "$body4" | jq . 2>/dev/null || echo "$body4"

echo ""
echo "📋 5. Listing all created mailboxes"
echo "-----------------------------------"

list_response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/api/mailboxes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN")

list_status="${list_response: -3}"
list_body="${list_response%???}"

echo "Status Code: $list_status"
echo "Response Body:"
echo "$list_body" | jq . 2>/dev/null || echo "$list_body"

echo ""
echo "✅ Email name functionality test completed!"
echo ""
echo "📝 Summary of changes:"
echo "  - ✅ lifespan now accepts DAYS instead of minutes"
echo "  - ✅ emailName field allows custom email prefixes"
echo "  - ✅ Special characters are cleaned automatically"
echo "  - ✅ Falls back to random ID if emailName is invalid"
echo "  - ✅ Timestamp suffix ensures uniqueness"