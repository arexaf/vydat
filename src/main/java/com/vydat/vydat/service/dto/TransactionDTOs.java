package com.vydat.vydat.service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class TransactionDTOs {

    @Data
    public static class AirtimeRequest {
        private Long userId;
        private String network;   // MTN, GLO, AIRTEL, 9MOBILE
        private String phone;
        private double amount;
    }

    @Data
    public static class DataRequest {
        private Long userId;
        private String network;
        private String phone;
        private String plan;   // e.g., "500MB", "1GB", or plan id
        private double amount;
    }

    @Data
    public static class CableRequest {
        private Long userId;
        private String provider; // GOTV, DSTV, STARTIMES
        private String smartCardNumber;
        private String plan;
        private double amount;
    }

    @Data
    public static class ElectricityRequest {
        private Long userId;
        private String disco; // e.g., "ikeja-electric", "abuja-electric"
        private String meterNumber;
        private String meterType; // prepaid/postpaid
        private double amount;
    }

    @Data
    public static class ExamRequest {
        private Long userId;
        private String examType; // WAEC, NECO, JAMB
        private int quantity;
    }

    @Getter
    @Setter
    public static class ResponseDTO {
        private boolean success;
        private String message;
        private Object data;

        public ResponseDTO() {}
        public ResponseDTO(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
    }
}
