package com.playoffpool.dto;

import java.util.List;

public class LiveDto {

    private List<PickerInfo> participants;
    private List<GameInfo> games;
    private List<PlayerPicksInfo> hotHand;
    private List<PlayerPicksInfo> blockade;

    public LiveDto() {}

    public List<PickerInfo> getParticipants() { return participants; }
    public void setParticipants(List<PickerInfo> participants) { this.participants = participants; }

    public List<GameInfo> getGames() { return games; }
    public void setGames(List<GameInfo> games) { this.games = games; }

    public List<PlayerPicksInfo> getHotHand() { return hotHand; }
    public void setHotHand(List<PlayerPicksInfo> hotHand) { this.hotHand = hotHand; }

    public List<PlayerPicksInfo> getBlockade() { return blockade; }
    public void setBlockade(List<PlayerPicksInfo> blockade) { this.blockade = blockade; }

    public static class PickerInfo {
        private Integer participantId;
        private String name;
        private String teamName;

        public PickerInfo() {}
        public PickerInfo(Integer participantId, String name, String teamName) {
            this.participantId = participantId;
            this.name = name;
            this.teamName = teamName;
        }

        public Integer getParticipantId() { return participantId; }
        public void setParticipantId(Integer participantId) { this.participantId = participantId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
    }

    public static class TeamInfo {
        private String abbrev;
        private String name;
        private String logo;
        private Integer score;

        public String getAbbrev() { return abbrev; }
        public void setAbbrev(String abbrev) { this.abbrev = abbrev; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getLogo() { return logo; }
        public void setLogo(String logo) { this.logo = logo; }
        public Integer getScore() { return score; }
        public void setScore(Integer score) { this.score = score; }
    }

    public static class GameInfo {
        private String gameId;
        private String gameState;
        private String startTimeUTC;
        private TeamInfo awayTeam;
        private TeamInfo homeTeam;
        private String period;
        private String clock;
        private boolean inIntermission;
        private List<PickerInfo> awayPicks;
        private List<PickerInfo> homePicks;

        public String getGameId() { return gameId; }
        public void setGameId(String gameId) { this.gameId = gameId; }
        public String getGameState() { return gameState; }
        public void setGameState(String gameState) { this.gameState = gameState; }
        public String getStartTimeUTC() { return startTimeUTC; }
        public void setStartTimeUTC(String startTimeUTC) { this.startTimeUTC = startTimeUTC; }
        public TeamInfo getAwayTeam() { return awayTeam; }
        public void setAwayTeam(TeamInfo awayTeam) { this.awayTeam = awayTeam; }
        public TeamInfo getHomeTeam() { return homeTeam; }
        public void setHomeTeam(TeamInfo homeTeam) { this.homeTeam = homeTeam; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public String getClock() { return clock; }
        public void setClock(String clock) { this.clock = clock; }
        public boolean isInIntermission() { return inIntermission; }
        public void setInIntermission(boolean inIntermission) { this.inIntermission = inIntermission; }
        public List<PickerInfo> getAwayPicks() { return awayPicks; }
        public void setAwayPicks(List<PickerInfo> awayPicks) { this.awayPicks = awayPicks; }
        public List<PickerInfo> getHomePicks() { return homePicks; }
        public void setHomePicks(List<PickerInfo> homePicks) { this.homePicks = homePicks; }
    }

    public static class PlayerPicksInfo {
        private Integer optionId;
        private String playerName;
        private String teamAbbrev;
        private String teamName;
        private Integer points;
        private List<PickerInfo> pickers;

        public Integer getOptionId() { return optionId; }
        public void setOptionId(Integer optionId) { this.optionId = optionId; }
        public String getPlayerName() { return playerName; }
        public void setPlayerName(String playerName) { this.playerName = playerName; }
        public String getTeamAbbrev() { return teamAbbrev; }
        public void setTeamAbbrev(String teamAbbrev) { this.teamAbbrev = teamAbbrev; }
        public String getTeamName() { return teamName; }
        public void setTeamName(String teamName) { this.teamName = teamName; }
        public Integer getPoints() { return points; }
        public void setPoints(Integer points) { this.points = points; }
        public List<PickerInfo> getPickers() { return pickers; }
        public void setPickers(List<PickerInfo> pickers) { this.pickers = pickers; }
    }
}
