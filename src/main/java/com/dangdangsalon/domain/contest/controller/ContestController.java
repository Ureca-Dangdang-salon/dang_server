package com.dangdangsalon.domain.contest.controller;

import com.dangdangsalon.domain.contest.dto.ContestDetailDto;
import com.dangdangsalon.domain.contest.dto.ContestInfoDto;
import com.dangdangsalon.domain.contest.dto.PostInfoDto;
import com.dangdangsalon.domain.contest.service.ContestService;
import com.dangdangsalon.util.ApiUtil;
import com.dangdangsalon.util.ApiUtil.ApiSuccess;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

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
    public ApiSuccess<?> getContestPosts(@PathVariable Long contestId, @PageableDefault(size = 3) Pageable pageable) {
        Page<PostInfoDto> contestPosts = contestService.getContestPosts(contestId, pageable);

        return ApiUtil.success(contestPosts);
    }
}
