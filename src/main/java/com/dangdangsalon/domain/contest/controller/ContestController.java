package com.dangdangsalon.domain.contest.controller;

import com.dangdangsalon.domain.auth.dto.CustomOAuth2User;
import com.dangdangsalon.domain.contest.dto.ContestDetailDto;
import com.dangdangsalon.domain.contest.dto.ContestInfoDto;
import com.dangdangsalon.domain.contest.dto.ContestPaymentDto;
import com.dangdangsalon.domain.contest.dto.ContestPaymentRequestDto;
import com.dangdangsalon.domain.contest.dto.LastContestWinnerDto;
import com.dangdangsalon.domain.contest.dto.PostInfoDto;
import com.dangdangsalon.domain.contest.dto.WinnerRankDto;
import com.dangdangsalon.domain.contest.service.ContestPostService;
import com.dangdangsalon.domain.contest.service.ContestService;
import com.dangdangsalon.domain.payment.service.PaymentGetService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;
    private final ContestPostService contestPostService;
    private final PaymentGetService paymentGetService;

    @GetMapping
    public ApiSuccess<?> getContest() {
        ContestInfoDto contest = contestService.getLatestContest();
        return ApiUtil.success(contest);
    }

    @GetMapping("/{contestId}")
    public ApiSuccess<?> getContestDetails(@PathVariable Long contestId) {
        ContestDetailDto details = contestService.getContestDetails(contestId);

        return ApiUtil.success(details);
    }

    @GetMapping("/{contestId}/posts")
    public ApiSuccess<?> getContestPosts(@AuthenticationPrincipal CustomOAuth2User user, @PathVariable Long contestId,
                                         @PageableDefault(size = 3) Pageable pageable) {
        Long userId = user.getUserId();

        Page<PostInfoDto> contestPosts = contestPostService.getContestPosts(contestId, userId, pageable);

        return ApiUtil.success(contestPosts);
    }

    @GetMapping("/{contestId}/check")
    public ApiSuccess<?> checkAlreadyJoin(@PathVariable Long contestId,
                                          @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();
        boolean alreadyParticipated = contestService.checkUserParticipated(contestId, userId);

        return ApiUtil.success(Map.of("already_participated", alreadyParticipated));
    }

    @GetMapping("/winner/last")
    public ApiSuccess<?> getLastContestWinner() {
        LastContestWinnerDto winner = contestService.getLastContestWinner();

        return ApiUtil.success(winner);
    }

    @GetMapping("/winner/rank")
    public ApiSuccess<?> getWinnerAndRankPost() {
        WinnerRankDto rankDto = contestService.getWinnerAndRankPost();

        return ApiUtil.success(rankDto);
    }

    @PostMapping("/payment")
    public ApiSuccess<?> getPaymentBetweenContest(@RequestBody ContestPaymentRequestDto contestPaymentRequestDto,
                                                  @AuthenticationPrincipal CustomOAuth2User user) {
        Long userId = user.getUserId();

        List<ContestPaymentDto> contestPayments = paymentGetService.getContestPayments(contestPaymentRequestDto, userId);
        return ApiUtil.success(contestPayments);
    }
}
