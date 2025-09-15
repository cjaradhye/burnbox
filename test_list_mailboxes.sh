#!/bin/bash

# Test script for List Mailboxes API
# Usage: ./test_list_mailboxes.sh

BASE_URL="http://localhost:8080"
JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJuYW1lIjoiQXJhZGh5ZSBTd2FydXAiLCJ1c2VySWQiOiI1ODAzYjc2My01MjEwLTRkNjUtYjQ1OS1mYTE4MDM0ZGE5MzYiLCJlbWFpbCI6ImFyYWRoeWUxQGdtYWlsLmNvbSIsInBpY3R1cmUiOiJodHRwczovL2xoMy5nb29nbGV1c2VyY29udGVudC5jb20vYS9BQ2c4b2NMajJsUTNtLWd6cFlsc2p6OVNSei1fX3FBSzVxU2pNX1EyRFlOY1ppajlRYnIzODJDdT1zOTYtYyIsInN1YiI6IjExMTM2MDk4OTcyNzAwNTA3NjgzNSIsImlhdCI6MTc1Nzg0Mzg0MiwiZXhwIjoxNzU3OTMwMjQyfQ.XVi8FL03iAWg0bJ9xiFttd14ehgCoqrfO3mdgXeqAdw"

echo "🧪 Testing List Mailboxes API"
echo "=============================="

echo ""
echo "📋 1. Testing GET /api/mailboxes (List all mailboxes for user)"
echo "--------------------------------------------------------------"

response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/api/mailboxes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN")

status_code="${response: -3}"
body="${response%???}"

echo "Status Code: $status_code"
echo "Response Body:"
echo "$body" | jq . 2>/dev/null || echo "$body"

echo ""
echo "📊 2. Creating a test mailbox with custom email name (if none exist)"
echo "-------------------------------------------------------------------"

create_response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/mailboxes/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"lifespan": 1, "burnAfterRead": false, "emailName": "testuser123"}')

create_status="${create_response: -3}"
create_body="${create_response%???}"

echo "Create Mailbox Status: $create_status"
echo "Create Response:"
echo "$create_body" | jq . 2>/dev/null || echo "$create_body"

echo ""
echo "📊 2b. Creating another mailbox without custom email name"
echo "--------------------------------------------------------"

create_response2=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/api/mailboxes/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d '{"lifespan": 7, "burnAfterRead": true}')

create_status2="${create_response2: -3}"
create_body2="${create_response2%???}"

echo "Create Mailbox Status: $create_status2"
echo "Create Response:"
echo "$create_body2" | jq . 2>/dev/null || echo "$create_body2"

echo ""
echo "📋 3. Testing GET /api/mailboxes again (should show the new mailbox)"
echo "--------------------------------------------------------------------"

response2=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/api/mailboxes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN")

status_code2="${response2: -3}"
body2="${response2%???}"

echo "Status Code: $status_code2"
echo "Response Body:"
echo "$body2" | jq . 2>/dev/null || echo "$body2"

echo ""
echo "🔑 4. Testing without authentication (should return 401)"
echo "--------------------------------------------------------"

unauth_response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/api/mailboxes" \
  -H "Content-Type: application/json")

unauth_status="${unauth_response: -3}"
unauth_body="${unauth_response%???}"

echo "Status Code: $unauth_status"
echo "Response Body:"
echo "$unauth_body"

echo ""
echo "✅ Test completed!"