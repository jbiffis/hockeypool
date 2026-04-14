#!/bin/bash
# Seed 16 NHL playoff team options for question 57
# Run this from a machine that can reach the API.
# Usage: ADMIN_COOKIE="JSESSIONID=your-session-id" ./seed-playoff-teams.sh
#
# First, log in to the admin panel in your browser and copy the JSESSIONID cookie value.

BASE_URL="${API_URL:-https://hockeypool.biffis.com}"
QID=57
COOKIE="${ADMIN_COOKIE:?Set ADMIN_COOKIE to your JSESSIONID value}"

# 2025-26 NHL Playoff Teams (16 teams)
# Eastern Conference
teams=(
  "Washington Capitals|WSH|1"
  "Toronto Maple Leafs|TOR|2"
  "Tampa Bay Lightning|TBL|3"
  "Florida Panthers|FLA|4"
  "Carolina Hurricanes|CAR|5"
  "New Jersey Devils|NJD|6"
  "Ottawa Senators|OTT|7"
  "Montreal Canadiens|MTL|8"
  "Winnipeg Jets|WPG|9"
  "Dallas Stars|DAL|10"
  "Vegas Golden Knights|VGK|11"
  "Minnesota Wild|MIN|12"
  "Colorado Avalanche|COL|13"
  "Edmonton Oilers|EDM|14"
  "Los Angeles Kings|LAK|15"
  "Vancouver Canucks|VAN|16"
)

for entry in "${teams[@]}"; do
  IFS='|' read -r name abbrev order <<< "$entry"
  image_url="https://assets.nhle.com/logos/nhl/svg/${abbrev}_dark.svg"

  echo "Creating: $name ($abbrev)"
  curl -s -X POST "${BASE_URL}/api/admin/questions/${QID}/options" \
    -H "Content-Type: application/json" \
    -H "Cookie: ${COOKIE}" \
    -d "{\"optionText\":\"${name}\",\"displayOrder\":${order},\"imageUrl\":\"${image_url}\",\"points\":null,\"subtext\":null}" \
    -o /dev/null -w "  HTTP %{http_code}\n"
done

echo "Done! Created ${#teams[@]} options."
