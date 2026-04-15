package com.oism.capitaltech.dto;

import com.oism.capitaltech.entity.Plan;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record PlanResponse(
        String id,
        String name,
        BigDecimal amount,
        String description
) {
    public static PlanResponse fromPlan(Plan plan) {
        return new PlanResponse(
                plan.name(),
                plan.getDisplayName(),
                plan.getAmount(),
                plan.getDescription()
        );
    }

    public static List<PlanResponse> allPlans() {
        return Arrays.stream(Plan.values())
                .map(PlanResponse::fromPlan)
                .toList();
    }
}
