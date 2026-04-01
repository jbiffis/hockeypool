package com.playoffpool.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NhlApiClientTest {

    private MockRestServiceServer mockServer;
    private NhlApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(NhlApiClient.BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        client = new NhlApiClient(builder.build());
    }

    // --- Playoffs ---

    @Test
    void getPlayoffBracket() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/playoff-bracket/2025"))
                .andRespond(withSuccess("{\"series\":[{\"seriesLetter\":\"A\"}]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getPlayoffBracket(2025);

        mockServer.verify();
        assertTrue(result.has("series"));
        assertEquals("A", result.get("series").get(0).get("seriesLetter").asText());
    }

    @Test
    void getPlayoffSeriesSchedule() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/schedule/playoff-series/20242025/i"))
                .andRespond(withSuccess("{\"round\":2,\"seriesLetter\":\"I\",\"games\":[]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getPlayoffSeriesSchedule(20242025, "i");

        mockServer.verify();
        assertEquals(2, result.get("round").asInt());
        assertEquals("I", result.get("seriesLetter").asText());
    }

    // --- Schedule ---

    @Test
    void getLeagueScheduleNow() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/schedule/now"))
                .andRespond(withSuccess("{\"gameWeek\":[]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getLeagueScheduleNow();

        mockServer.verify();
        assertTrue(result.has("gameWeek"));
    }

    @Test
    void getLeagueScheduleByDate() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/schedule/2024-04-20"))
                .andRespond(withSuccess("{\"gameWeek\":[{\"date\":\"2024-04-20\"}]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getLeagueSchedule("2024-04-20");

        mockServer.verify();
        assertEquals("2024-04-20", result.get("gameWeek").get(0).get("date").asText());
    }

    @Test
    void getTeamSchedule() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/club-schedule/TOR/20242025"))
                .andRespond(withSuccess("{\"games\":[]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getTeamSchedule("TOR", 20242025);

        mockServer.verify();
        assertTrue(result.has("games"));
    }

    // --- Scores & Boxscores ---

    @Test
    void getScoresNow() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/score/now"))
                .andRespond(withSuccess("{\"games\":[]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getScoresNow();

        mockServer.verify();
        assertTrue(result.has("games"));
    }

    @Test
    void getScoresByDate() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/score/2024-05-10"))
                .andRespond(withSuccess("{\"games\":[{\"id\":2024030211}]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getScores("2024-05-10");

        mockServer.verify();
        assertEquals(2024030211L, result.get("games").get(0).get("id").asLong());
    }

    @Test
    void getBoxscore() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/gamecenter/2024030211/boxscore"))
                .andRespond(withSuccess("{\"id\":2024030211,\"homeTeam\":{\"abbrev\":\"TOR\"},\"awayTeam\":{\"abbrev\":\"FLA\"}}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getBoxscore("2024030211");

        mockServer.verify();
        assertEquals("TOR", result.get("homeTeam").get("abbrev").asText());
        assertEquals("FLA", result.get("awayTeam").get("abbrev").asText());
    }

    // --- Skater Stats Leaders ---

    @Test
    void getSkaterStatsLeaders() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/skater-stats-leaders/20242025/3"))
                .andRespond(withSuccess("{\"goals\":[],\"assists\":[]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getSkaterStatsLeaders(20242025, 3);

        mockServer.verify();
        assertTrue(result.has("goals"));
        assertTrue(result.has("assists"));
    }

    @Test
    void getSkaterStatsLeadersWithCategoryAndLimit() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/skater-stats-leaders/20242025/2?categories=goals&limit=5"))
                .andRespond(withSuccess("{\"goals\":[{\"player\":\"test\"}]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getSkaterStatsLeaders(20242025, 2, "goals", 5);

        mockServer.verify();
        assertEquals("test", result.get("goals").get(0).get("player").asText());
    }

    // --- Goalie Stats Leaders ---

    @Test
    void getGoalieStatsLeaders() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/goalie-stats-leaders/20242025/3"))
                .andRespond(withSuccess("{\"wins\":[],\"savePctg\":[]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getGoalieStatsLeaders(20242025, 3);

        mockServer.verify();
        assertTrue(result.has("wins"));
        assertTrue(result.has("savePctg"));
    }

    @Test
    void getGoalieStatsLeadersWithCategoryAndLimit() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/goalie-stats-leaders/20242025/2?categories=wins&limit=3"))
                .andRespond(withSuccess("{\"wins\":[{\"player\":\"test\"}]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getGoalieStatsLeaders(20242025, 2, "wins", 3);

        mockServer.verify();
        assertEquals("test", result.get("wins").get(0).get("player").asText());
    }

    // --- Standings ---

    @Test
    void getStandingsNow() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/standings/now"))
                .andRespond(withSuccess("{\"standings\":[]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getStandingsNow();

        mockServer.verify();
        assertTrue(result.has("standings"));
    }

    @Test
    void getStandingsByDate() {
        mockServer.expect(requestTo(NhlApiClient.BASE_URL + "/standings/2024-11-10"))
                .andRespond(withSuccess("{\"standings\":[{\"teamAbbrev\":\"TOR\"}]}", MediaType.APPLICATION_JSON));

        JsonNode result = client.getStandings("2024-11-10");

        mockServer.verify();
        assertEquals("TOR", result.get("standings").get(0).get("teamAbbrev").asText());
    }
}
