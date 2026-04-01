package com.playoffpool.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class NhlApiClient {

    static final String BASE_URL = "https://api-web.nhle.com/v1";

    private final RestClient restClient;

    public NhlApiClient() {
        this(RestClient.builder().baseUrl(BASE_URL).build());
    }

    NhlApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    // --- Playoffs ---

    /**
     * Playoff bracket overview for a year (e.g. 2025).
     * Returns all series with teams, win counts, winningTeamId, and seriesLetter.
     */
    public JsonNode getPlayoffBracket(int year) {
        return get("/playoff-bracket/{year}", year);
    }

    /**
     * Game-by-game schedule for a specific playoff series.
     * @param season      e.g. 20242025
     * @param seriesLetter lowercase letter from the bracket (e.g. "i" for R2 TOR vs FLA)
     */
    public JsonNode getPlayoffSeriesSchedule(int season, String seriesLetter) {
        return get("/schedule/playoff-series/{season}/{letter}", season, seriesLetter);
    }

    // --- Schedule ---

    /** League-wide schedule for today. */
    public JsonNode getLeagueScheduleNow() {
        return get("/schedule/now");
    }

    /** League-wide schedule for a specific date (yyyy-MM-dd). */
    public JsonNode getLeagueSchedule(String date) {
        return get("/schedule/{date}", date);
    }

    /** A single team's full-season schedule (e.g. team="TOR", season=20242025). */
    public JsonNode getTeamSchedule(String team, int season) {
        return get("/club-schedule/{team}/{season}", team, season);
    }

    // --- Scores & Boxscores ---

    /** Daily scores for today. */
    public JsonNode getScoresNow() {
        return get("/score/now");
    }

    /** Daily scores for a specific date (yyyy-MM-dd). */
    public JsonNode getScores(String date) {
        return get("/score/{date}", date);
    }

    /** Full boxscore for a game. */
    public JsonNode getBoxscore(String gameId) {
        return get("/gamecenter/{gameId}/boxscore", gameId);
    }

    // --- Skater Stats Leaders ---

    /**
     * Skater stats leaders for a specific season and game type.
     * @param season  e.g. 20242025
     * @param gameType  2 = regular season, 3 = playoffs
     */
    public JsonNode getSkaterStatsLeaders(int season, int gameType) {
        return get("/skater-stats-leaders/{season}/{gameType}", season, gameType);
    }

    /**
     * Skater stats leaders filtered by category.
     * @param season   e.g. 20242025
     * @param gameType 2 = regular season, 3 = playoffs
     * @param category e.g. "goals", "assists", "points"
     * @param limit    number of results, -1 for all
     */
    public JsonNode getSkaterStatsLeaders(int season, int gameType, String category, int limit) {
        return restClient.get()
                .uri("/skater-stats-leaders/{season}/{gameType}?categories={cat}&limit={lim}",
                        season, gameType, category, limit)
                .retrieve()
                .body(JsonNode.class);
    }

    // --- Goalie Stats Leaders ---

    /**
     * Goalie stats leaders for a specific season and game type.
     * @param season   e.g. 20242025
     * @param gameType 2 = regular season, 3 = playoffs
     */
    public JsonNode getGoalieStatsLeaders(int season, int gameType) {
        return get("/goalie-stats-leaders/{season}/{gameType}", season, gameType);
    }

    /**
     * Goalie stats leaders filtered by category.
     * @param season   e.g. 20242025
     * @param gameType 2 = regular season, 3 = playoffs
     * @param category e.g. "wins", "savePctg", "goalsAgainstAvg"
     * @param limit    number of results, -1 for all
     */
    public JsonNode getGoalieStatsLeaders(int season, int gameType, String category, int limit) {
        return restClient.get()
                .uri("/goalie-stats-leaders/{season}/{gameType}?categories={cat}&limit={lim}",
                        season, gameType, category, limit)
                .retrieve()
                .body(JsonNode.class);
    }

    // --- Standings ---

    /** Current standings. */
    public JsonNode getStandingsNow() {
        return get("/standings/now");
    }

    /** Standings as of a specific date (yyyy-MM-dd). */
    public JsonNode getStandings(String date) {
        return get("/standings/{date}", date);
    }

    // --- helpers ---

    private JsonNode get(String uri, Object... uriVars) {
        return restClient.get()
                .uri(uri, uriVars)
                .retrieve()
                .body(JsonNode.class);
    }
}
