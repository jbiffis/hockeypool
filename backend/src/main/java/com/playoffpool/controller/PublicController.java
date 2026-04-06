package com.playoffpool.controller;

import com.playoffpool.dto.DivisionDto;
import com.playoffpool.dto.LeaderboardDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class PublicController {

    private final SeasonRepository seasonRepository;
    private final LeaderboardService leaderboardService;
    private final AdminParticipantService adminParticipantService;
    private final AdminDivisionService adminDivisionService;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ResponseAnswerRepository responseAnswerRepository;
    private final ParticipantRepository participantRepository;

    public PublicController(SeasonRepository seasonRepository,
                            LeaderboardService leaderboardService,
                            AdminParticipantService adminParticipantService,
                            AdminDivisionService adminDivisionService,
                            QuestionRepository questionRepository,
                            QuestionOptionRepository questionOptionRepository,
                            ResponseAnswerRepository responseAnswerRepository,
                            ParticipantRepository participantRepository) {
        this.seasonRepository = seasonRepository;
        this.leaderboardService = leaderboardService;
        this.adminParticipantService = adminParticipantService;
        this.adminDivisionService = adminDivisionService;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.responseAnswerRepository = responseAnswerRepository;
        this.participantRepository = participantRepository;
    }

    @GetMapping("/seasons")
    public List<Season> getSeasons() {
        return seasonRepository.findAllByOrderByYearDesc();
    }

    @GetMapping("/seasons/{seasonId}/signup")
    public ResponseEntity<?> getSignupPage(@PathVariable Integer seasonId) {
        return seasonRepository.findById(seasonId)
                .<ResponseEntity<?>>map(s -> ResponseEntity.ok(SeasonDto.fromEntity(s)))
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
        for (ParticipantResponseDto r : responses) {
            r.setEmail(null);
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
}
