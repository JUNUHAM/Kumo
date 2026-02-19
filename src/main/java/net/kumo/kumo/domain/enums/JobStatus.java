package net.kumo.kumo.domain.enums;

public enum JobStatus {
    RECRUITING, // 모집중 (기본값)
    CLOSED,     // 마감됨
    BLOCKED     // 차단됨 (신고 누적 등)
}