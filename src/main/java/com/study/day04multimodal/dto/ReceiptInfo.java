package com.study.day04multimodal.dto;

import java.util.List;

public record ReceiptInfo(
        String vendor,
        String totalAmount,
        String date,
        List<String> items
) {
}
