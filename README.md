
- 쿼리/DB연동: MyBatis (MyBatisPagingItemReader / MyBatisBatchItemWriter)
- 입력: DB2 테이블
- 출력: DB2 타겟 테이블 (SELECT -> INSERT)
- Job 2개
  - Job#1: Tasklet 집계(전날 기준) -> AGG_DAILY MERGE
  - Job#2: 대용량 가정(10,000건) -> chunk(100) + 멀티스레드 Step 예시
- 메타데이터 저장소: DB2 (Spring Batch schema-db2.sql)
- 스케줄: 매일 05:00 (Asia/Seoul)
- 개발: IntelliJ IDEA 2025.3.1.1, JDK 25
- Spring Boot: 4.0.1




