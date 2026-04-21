package com.playoffpool.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.playoffpool.dto.AnswerDto;
import com.playoffpool.dto.DivisionDto;
import com.playoffpool.dto.LeaderboardDto;
import com.playoffpool.dto.LiveDto;
import com.playoffpool.dto.ParticipantResponseDto;
import com.playoffpool.dto.QuestionDetailDto;
import com.playoffpool.dto.QuestionDetailDto.OptionDetail;
import com.playoffpool.dto.QuestionDetailDto.PickerInfo;
import com.playoffpool.dto.SeasonDto;
import com.playoffpool.model.*;
import com.playoffpool.repository.*;
import com.playoffpool.service.AdminDivisionService;
import com.playoffpool.service.AdminParticipantService;
import com.playoffpool.service.LeaderboardService;
import com.playoffpool.service.NhlApiClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PublicController {

    private static final Map<String, String> ABBREV_TO_NAME;
    static {
        ABBREV_TO_NAME = new java.util.HashMap<>();
        ABBREV_TO_NAME.put("BUF", "Buffalo Sabres");
        ABBREV_TO_NAME.put("BOS", "Boston Bruins");
        ABBREV_TO_NAME.put("TBL", "Tampa Bay Lightning");
        ABBREV_TO_NAME.put("MTL", "Montreal Canadiens");
        ABBREV_TO_NAME.put("CAR", "Carolina Hurricanes");
        ABBREV_TO_NAME.put("OTT", "Ottawa Senators");
        ABBREV_TO_NAME.put("PHI", "Philadelphia Flyers");
        ABBREV_TO_NAME.put("PIT", "Pittsburgh Penguins");
        ABBREV_TO_NAME.put("COL", "Colorado Avalanche");
        ABBREV_TO_NAME.put("LAK", "Los Angeles Kings");
        ABBREV_TO_NAME.put("DAL", "Dallas Stars");
        ABBREV_TO_NAME.put("MIN", "Minnesota Wild");
        ABBREV_TO_NAME.put("VGK", "Vegas Golden Knights");
        ABBREV_TO_NAME.put("UTA", "Utah Mammoth");
        ABBREV_TO_NAME.put("EDM", "Edmonton Oilers");
        ABBREV_TO_NAME.put("ANA", "Anaheim Ducks");
        // pool-option abbreviation aliases
        ABBREV_TO_NAME.put("TB", "Tampa Bay Lightning");
        ABBREV_TO_NAME.put("UTH", "Utah Mammoth");
        ABBREV_TO_NAME.put("ANH", "Anaheim Ducks");
    }

    // Normalize pool option abbreviations to NHL-canonical ones for logo URLs
    private static final Map<String, String> LOGO_ABBREV = java.util.Map.of(
        "TB", "TBL", "UTH", "UTA", "ANH", "ANA"
    );

    private final SeasonRepository seasonRepository;
    private final LeaderboardService leaderboardService;
    private final AdminParticipantService adminParticipantService;
    private final AdminDivisionService adminDivisionService;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ResponseAnswerRepository responseAnswerRepository;
    private final ParticipantRepository participantRepository;
    private final NhlApiClient nhlApiClient;

    public PublicController(SeasonRepository seasonRepository,
                            LeaderboardService leaderboardService,
                            AdminParticipantService adminParticipantService,
                            AdminDivisionService adminDivisionService,
                            QuestionRepository questionRepository,
                            QuestionOptionRepository questionOptionRepository,
                            ResponseAnswerRepository responseAnswerRepository,
                            ParticipantRepository participantRepository,
                            NhlApiClient nhlApiClient) {
        this.seasonRepository = seasonRepository;
        this.leaderboardService = leaderboardService;
        this.adminParticipantService = adminParticipantService;
        this.adminDivisionService = adminDivisionService;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.responseAnswerRepository = responseAnswerRepository;
        this.participantRepository = participantRepository;
        this.nhlApiClient = nhlApiClient;
    }

    @GetMapping("/seasons")
    public List<Season> getSeasons() {
        return seasonRepository.findAllByOrderByYearDesc();
    }

    @GetMapping("/seasons/{seasonId}/signup")
    public ResponseEntity<?> getSignupPage(@PathVariable Integer seasonId) {
        return seasonRepository.findById(seasonId)
                .<ResponseEntity<?>>map(s -> {
                    SeasonDto dto = SeasonDto.fromEntity(s);
                    dto.setParticipantCount(participantRepository.countBySeasonId(seasonId));
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/seasons/{seasonId}/signup")
    public ResponseEntity<?> submitSignup(@PathVariable Integer seasonId,
                                          @RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "Email is required"));
        }
        Season season = seasonRepository.findById(seasonId).orElse(null);
        if (season == null) {
            return ResponseEntity.notFound().build();
        }
        if (participantRepository.findByEmailAndSeasonId(email.trim().toLowerCase(), seasonId).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(java.util.Map.of("error", "This email is already registered for this season"));
        }
        Participant p = new Participant();
        p.setEmail(email.trim().toLowerCase());
        p.setSeason(season);
        p.setCreatedAt(java.time.LocalDateTime.now());
        participantRepository.save(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("message", "Signed up successfully"));
    }

    @GetMapping("/divisions")
    public List<DivisionDto> getDivisions(@RequestParam Integer seasonId) {
        return adminDivisionService.getDivisionsForSeason(seasonId).stream()
            .map(d -> { DivisionDto dto = new DivisionDto(); dto.setId(d.getId()); dto.setName(d.getName()); dto.setSeasonId(d.getSeason().getId()); return dto; })
            .collect(java.util.stream.Collectors.toList());
    }

    @GetMapping("/leaderboard/{seasonId}")
    public LeaderboardDto getLeaderboard(@PathVariable Integer seasonId,
                                         @RequestParam(required = false) Integer divisionId) {
        return leaderboardService.getLeaderboard(seasonId, divisionId);
    }

    @GetMapping("/participants/{participantId}/responses")
    public List<ParticipantResponseDto> getParticipantResponses(@PathVariable Integer participantId) {
        List<ParticipantResponseDto> responses = adminParticipantService.getResponsesByParticipant(participantId);
        boolean unpaid = !responses.isEmpty() && !Boolean.TRUE.equals(responses.get(0).getPaid());
        for (ParticipantResponseDto r : responses) {
            r.setEmail(null);
            if (unpaid) {
                r.setRoundPointsTotal(null);
                if (r.getAnswers() != null) {
                    for (AnswerDto a : r.getAnswers()) {
                        a.setPointsEarned(null);
                        a.setOptionPointValue(null);
                    }
                }
            }
        }
        return responses;
    }

    @GetMapping("/questions/{questionId}")
    public QuestionDetailDto getQuestionDetail(@PathVariable Integer questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        QuestionDetailDto dto = new QuestionDetailDto();
        dto.setQuestionId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setDescription(question.getDescription());
        dto.setImageUrl(question.getImageUrl());
        dto.setQuestionType(question.getQuestionType());
        dto.setMaxWager(question.getMaxWager());
        dto.setMaxSelections(question.getMaxSelections());
        dto.setCorrectAnswerText(question.getCorrectAnswerText());
        dto.setRoundId(question.getRound().getId());
        dto.setRoundName(question.getRound().getName());

        // Get options
        List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByDisplayOrder(question.getId());

        // Get all response answers for this question, grouped by selected_option_id
        List<ResponseAnswer> answers = responseAnswerRepository.findByQuestionId(questionId);
        Map<Integer, List<ResponseAnswer>> answersByOption = answers.stream()
                .filter(a -> a.getSelectedOption() != null)
                .collect(Collectors.groupingBy(a -> a.getSelectedOption().getId()));

        // Build participant lookup
        Set<Integer> participantIds = answers.stream()
                .map(a -> a.getResponse().getParticipant().getId())
                .collect(Collectors.toSet());
        Map<Integer, Participant> participantMap = new HashMap<>();
        for (Integer pid : participantIds) {
            participantRepository.findById(pid).ifPresent(p -> participantMap.put(p.getId(), p));
        }

        List<OptionDetail> optionDetails = new ArrayList<>();
        for (QuestionOption opt : options) {
            OptionDetail od = new OptionDetail();
            od.setOptionId(opt.getId());
            od.setOptionText(opt.getOptionText());
            od.setSubtext(opt.getSubtext());
            od.setImageUrl(opt.getImageUrl());
            od.setPoints(opt.getPoints());
            od.setCorrect(question.getCorrectAnswerText() != null
                    && question.getCorrectAnswerText().equals(opt.getOptionText()));

            List<ResponseAnswer> optAnswers = answersByOption.getOrDefault(opt.getId(), Collections.emptyList());
            List<PickerInfo> pickers = new ArrayList<>();
            for (ResponseAnswer ra : optAnswers) {
                Participant p = participantMap.get(ra.getResponse().getParticipant().getId());
                if (p != null) {
                    PickerInfo pi = new PickerInfo();
                    pi.setParticipantId(p.getId());
                    pi.setName(p.getName());
                    pi.setTeamName(p.getTeamName());
                    pickers.add(pi);
                }
            }
            pickers.sort(Comparator.comparing(PickerInfo::getTeamName, String.CASE_INSENSITIVE_ORDER));
            od.setPickers(pickers);
            optionDetails.add(od);
        }

        dto.setOptions(optionDetails);
        return dto;
    }

    @GetMapping("/live")
    public LiveDto getLive(@RequestParam(defaultValue = "2") Integer seasonId) {
        // All participants for this season
        List<Participant> allParticipants = participantRepository.findBySeasonId(seasonId);

        // Build team name → pickers map from R1 multi_select questions (round 7)
        Map<String, List<LiveDto.PickerInfo>> teamPicksMap = new java.util.HashMap<>();
        List<Question> r1Questions = questionRepository.findByRoundIdOrderByDisplayOrder(7)
                .stream().filter(q -> "multi_select".equals(q.getQuestionType())).collect(Collectors.toList());
        for (Question q : r1Questions) {
            List<ResponseAnswer> answers = responseAnswerRepository.findByQuestionId(q.getId());
            Map<Integer, List<ResponseAnswer>> byOption = answers.stream()
                    .filter(ra -> ra.getSelectedOption() != null)
                    .collect(Collectors.groupingBy(ra -> ra.getSelectedOption().getId()));
            List<QuestionOption> opts = questionOptionRepository.findByQuestionIdOrderByDisplayOrder(q.getId());
            for (QuestionOption opt : opts) {
                List<LiveDto.PickerInfo> pickers = byOption.getOrDefault(opt.getId(), Collections.emptyList())
                        .stream().map(ra -> {
                            Participant p = ra.getResponse().getParticipant();
                            return new LiveDto.PickerInfo(p.getId(), p.getName(), p.getTeamName());
                        }).collect(Collectors.toList());
                teamPicksMap.put(opt.getOptionText(), pickers);
            }
        }

        // Fetch today's NHL games (ET date to handle late-night games)
        List<LiveDto.GameInfo> games = new ArrayList<>();
        try {
            String today = java.time.LocalDate.now(java.time.ZoneId.of("America/New_York")).toString();
            JsonNode scoresJson = nhlApiClient.getScores(today);
            if (scoresJson != null && scoresJson.has("games")) {
                for (JsonNode game : scoresJson.get("games")) {
                    if (game.path("gameType").asInt(0) != 3) continue;
                    String state = game.path("gameState").asText();
                    if ("OFF".equals(state)) continue;

                    String awayAbbrev = game.path("awayTeam").path("abbrev").asText();
                    String homeAbbrev = game.path("homeTeam").path("abbrev").asText();
                    String awayName = ABBREV_TO_NAME.getOrDefault(awayAbbrev, awayAbbrev);
                    String homeName = ABBREV_TO_NAME.getOrDefault(homeAbbrev, homeAbbrev);

                    LiveDto.TeamInfo away = new LiveDto.TeamInfo();
                    away.setAbbrev(awayAbbrev);
                    away.setName(awayName);
                    away.setLogo(game.path("awayTeam").path("logo").asText(
                            "https://assets.nhle.com/logos/nhl/svg/" + awayAbbrev + "_light.svg"));
                    away.setScore(game.path("awayTeam").has("score") ? game.path("awayTeam").path("score").asInt() : null);

                    LiveDto.TeamInfo home = new LiveDto.TeamInfo();
                    home.setAbbrev(homeAbbrev);
                    home.setName(homeName);
                    home.setLogo(game.path("homeTeam").path("logo").asText(
                            "https://assets.nhle.com/logos/nhl/svg/" + homeAbbrev + "_light.svg"));
                    home.setScore(game.path("homeTeam").has("score") ? game.path("homeTeam").path("score").asInt() : null);

                    LiveDto.GameInfo gi = new LiveDto.GameInfo();
                    gi.setGameId(game.path("id").asText());
                    gi.setGameState(state);
                    gi.setStartTimeUTC(game.path("startTimeUTC").asText());
                    gi.setAwayTeam(away);
                    gi.setHomeTeam(home);

                    if ("LIVE".equals(state) || "CRIT".equals(state)) {
                        int periodNum = game.path("periodDescriptor").path("number").asInt(0);
                        String periodType = game.path("periodDescriptor").path("periodType").asText("REG");
                        gi.setPeriod(formatPeriod(periodNum, periodType));
                        gi.setClock(game.path("clock").path("timeRemaining").asText(""));
                        gi.setInIntermission(game.path("clock").path("inIntermission").asBoolean(false));
                    }

                    gi.setAwayPicks(teamPicksMap.getOrDefault(awayName, Collections.emptyList()));
                    gi.setHomePicks(teamPicksMap.getOrDefault(homeName, Collections.emptyList()));
                    games.add(gi);
                }
            }
        } catch (Exception ignored) {}

        LiveDto dto = new LiveDto();
        dto.setParticipants(allParticipants.stream()
                .filter(p -> p.getName() != null && p.getTeamName() != null)
                .map(p -> new LiveDto.PickerInfo(p.getId(), p.getName(), p.getTeamName()))
                .sorted(Comparator.comparing(LiveDto.PickerInfo::getTeamName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList()));
        dto.setGames(games);
        dto.setHotHand(buildLivePlayerPicks(74));
        dto.setBlockade(buildLivePlayerPicks(75));
        return dto;
    }

    private List<LiveDto.PlayerPicksInfo> buildLivePlayerPicks(Integer questionId) {
        List<QuestionOption> opts = questionOptionRepository.findByQuestionIdOrderByDisplayOrder(questionId);
        List<ResponseAnswer> answers = responseAnswerRepository.findByQuestionId(questionId);
        Map<Integer, List<ResponseAnswer>> byOption = answers.stream()
                .filter(ra -> ra.getSelectedOption() != null)
                .collect(Collectors.groupingBy(ra -> ra.getSelectedOption().getId()));

        List<LiveDto.PlayerPicksInfo> result = new ArrayList<>();
        for (QuestionOption opt : opts) {
            String text = opt.getOptionText();
            String playerName = text.replaceAll("\\s*\\([^)]*\\)\\s*$", "").trim();
            String abbrev = text.matches(".*\\(([A-Z]+)\\)$") ? text.replaceAll(".*\\(([A-Z]+)\\)$", "$1") : "";

            List<LiveDto.PickerInfo> pickers = byOption.getOrDefault(opt.getId(), Collections.emptyList())
                    .stream().map(ra -> {
                        Participant p = ra.getResponse().getParticipant();
                        return new LiveDto.PickerInfo(p.getId(), p.getName(), p.getTeamName());
                    }).sorted(Comparator.comparing(LiveDto.PickerInfo::getTeamName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .collect(Collectors.toList());

            LiveDto.PlayerPicksInfo info = new LiveDto.PlayerPicksInfo();
            info.setOptionId(opt.getId());
            info.setPlayerName(playerName);
            String logoAbbrev = LOGO_ABBREV.getOrDefault(abbrev, abbrev);
            info.setTeamAbbrev(logoAbbrev);
            info.setTeamName(ABBREV_TO_NAME.getOrDefault(logoAbbrev, ABBREV_TO_NAME.getOrDefault(abbrev, abbrev)));
            info.setPoints(opt.getPoints());
            info.setPickers(pickers);
            result.add(info);
        }
        return result;
    }

    private String formatPeriod(int period, String periodType) {
        return switch (periodType) {
            case "OT" -> period == 4 ? "OT" : (period - 3) + "OT";
            case "SO" -> "SO";
            default -> switch (period) {
                case 1 -> "1st";
                case 2 -> "2nd";
                case 3 -> "3rd";
                default -> period + "";
            };
        };
    }
}
