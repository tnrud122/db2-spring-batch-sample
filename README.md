# DB2 + Spring Batch 샘플 (Spring Boot 4.0.1, JDK 25)

요구사항 반영:
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

---

## 1) DB 준비

### (1) Spring Batch 메타데이터 테이블 생성
Spring Batch는 DB별 메타데이터 스키마 스크립트를 제공하며, DB2용은 `schema-db2.sql` 입니다.
이 스크립트는 `spring-batch-core` JAR 안에 포함되어 있습니다.

- 위치 예: `org/springframework/batch/core/schema-db2.sql`

운영에서는 `spring.batch.jdbc.initialize-schema=never` 유지하고,
DBA/배포 파이프라인에서 스키마를 관리하는 것을 권장합니다.

### (2) 샘플 비즈니스 테이블 생성 + 10,000건 적재
`src/main/resources/db/init-business-tables-db2.sql` 실행

---

## 2) 실행

### (1) 애플리케이션 실행
IntelliJ에서 `Db2SpringBatchSampleApplication` 실행

### (2) 크론 스케줄
`application.yml`의 `app.schedule.cron` 기본값은 매일 05:00 입니다.

---

## 3) 멀티스레드(병렬) 처리 포인트

`chunkTransferStep`에 `.taskExecutor(...)`를 붙여 병렬 처리합니다.

멀티스레드 Step에서는 Reader가 thread-safe여야 하므로,
예제에서는 `SynchronizedItemStreamReader`로 감쌌습니다.

---


