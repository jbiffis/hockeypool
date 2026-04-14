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
# Eastern Conference - Atlantic: BUF (y), TBL (x), MTL (x), BOS (x), OTT (x)
# Eastern Conference - Metropolitan: CAR (z), PIT (x), PHI (x)
# Western Conference - Central: COL (*), DAL (x), MIN (x), UTA (x)
# Western Conference - Pacific: VGK (x), EDM (x), ANA (x), LAK (x)
teams=(
  "Buffalo Sabres|BUF|1"
  "Tampa Bay Lightning|TBL|2"
  "Montreal Canadiens|MTL|3"
  "Boston Bruins|BOS|4"
  "Ottawa Senators|OTT|5"
  "Carolina Hurricanes|CAR|6"
  "Pittsburgh Penguins|PIT|7"
  "Philadelphia Flyers|PHI|8"
  "Colorado Avalanche|COL|9"
  "Dallas Stars|DAL|10"
  "Minnesota Wild|MIN|11"
  "Utah Hockey Club|UTA|12"
  "Vegas Golden Knights|VGK|13"
  "Edmonton Oilers|EDM|14"
  "Anaheim Ducks|ANA|15"
  "Los Angeles Kings|LAK|16"
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
