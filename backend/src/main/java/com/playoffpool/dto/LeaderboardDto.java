package com.playoffpool.dto;

import java.util.List;
import java.util.Map;

public class LeaderboardDto {

    private List<RoundInfo> rounds;
    private List<LeaderboardEntry> entries;

    public LeaderboardDto() {}

    public List<RoundInfo> getRounds() { return rounds; }
    public void setRounds(List<RoundInfo> rounds) { this.rounds = rounds; }

    public List<LeaderboardEntry> getEntries() { return entries; }
    public void setEntries(List<LeaderboardEntry> entries) { this.entries = entries; }

    public static class RoundInfo {
        private Integer id;
        private String name;
        private Integer displayOrder;
        private boolean scored;

        public RoundInfo() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

        public boolean isScored() { return scored; }
        public void setScored(boolean scored) { this.scored = scored; }
    }

    public static class LeaderboardEntry {
        private Integer participantId;
        private String name;
        private String teamName;
        private Map<Integer, Integer> roundScores; // roundId -> points
        private int overallTotal;

        public LeaderboardEntry() {}

        public Integer getParticipantId() { return participantId; }
        public void setParticipantId(Integer participantId) { this.participantId = participantId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }

        public Map<Integer, Integer> getRoundScores() { return roundScores; }
        public void setRoundScores(Map<Integer, Integer> roundScores) { this.roundScores = roundScores; }

        public int getOverallTotal() { return overallTotal; }
        public void setOverallTotal(int overallTotal) { this.overallTotal = overallTotal; }
    }
}
