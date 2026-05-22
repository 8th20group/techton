package com.techton.activity;

import com.techton.activity.dto.ActivityApprovalResponse;
import com.techton.activity.dto.ActivityEarnResponse;
import com.techton.activity.dto.ActivityRejectRequest;
import com.techton.activity.dto.ActivityRejectResponse;
import com.techton.activity.dto.ActivitySubmitResponse;
import com.techton.activity.dto.BlogActivityRequest;
import com.techton.activity.dto.WeeklyActivitiesResponse;
import com.techton.activity.dto.CrewActivityResponse;
import com.techton.activity.dto.PendingActivityResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;



    @PostMapping("/crews/{crewId}/activities/mission")
    public ActivitySubmitResponse mission(@PathVariable Long crewId,
                                          @RequestParam String activityDate,
                                          @RequestPart MultipartFile image,
                                          @RequestParam(required = false) String memo) {
        return activityService.mission(crewId, activityDate, image, memo);
    }

    @PostMapping("/crews/{crewId}/activities/blog")
    public ActivitySubmitResponse blog(@PathVariable Long crewId, @RequestBody BlogActivityRequest request) {
        return activityService.blog(crewId, request);
    }

    @GetMapping("/crews/{crewId}/weekly-activities")
    public WeeklyActivitiesResponse weeklyActivities(@PathVariable Long crewId) {
        return activityService.weeklyActivities(crewId);
    }

    @GetMapping("/crews/{crewId}/activities")
    public List<CrewActivityResponse> getCrewActivities(@PathVariable Long crewId) {
        return activityService.getCrewActivities(crewId);
    }

    @GetMapping("/admin/activities/pending")
    public List<PendingActivityResponse> pendingActivities() {
        return activityService.pendingActivities();
    }

    @PatchMapping("/admin/activities/{activityId}/approve")
    public ActivityApprovalResponse approve(@PathVariable Long activityId) {
        return activityService.approve(activityId);
    }

    @PatchMapping("/admin/activities/{activityId}/reject")
    public ActivityRejectResponse reject(@PathVariable Long activityId, @RequestBody ActivityRejectRequest request) {
        return activityService.reject(activityId, request);
    }
}
