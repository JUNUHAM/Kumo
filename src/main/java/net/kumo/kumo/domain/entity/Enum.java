package net.kumo.kumo.domain.entity;

public class Enum {
	public enum UserRole { SEEKER, RECRUITER, ADMIN }
	public enum Gender { MALE, FEMALE, OTHER }
	public enum RegionType { PREFECTURE, CITY, WARD, TOWN_VILLAGE }
	public enum SalaryType { HOURLY, DAILY, MONTHLY, NEGOTIABLE }
	public enum JobStatus {RECRUITING,CLOSED }
	public enum ApplicationStatus { APPLIED, VIEWED, PASSED, FAILED }
	public enum ReportStatus { PENDING, RESOLVED, REJECTED }
	public enum MessageType { TEXT, IMAGE, SYSTEM }
	
}
