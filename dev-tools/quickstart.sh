#!/bin/bash
echo "=== Content Curator Dev Quickstart ==="

RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"sk@test.com","password":"password123"}')

echo "Login response: $RESPONSE"

TOKEN=$(echo $RESPONSE | python3 -c "
import sys, json
try:
    d = json.load(sys.stdin)
    print(d['token'])
except:
    print('')
" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  echo "ERROR: Could not get token. Is user-profile running on 8081?"
else
  echo "export TOKEN='$TOKEN'" > ~/projects/content-curator/dev-tools/.token
  export TOKEN
  echo "Token saved (${#TOKEN} chars)"
fi
