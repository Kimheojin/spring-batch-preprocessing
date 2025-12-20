package HeoJin.SpringBatch.runner.controller;

import HeoJin.SpringBatch.runner.BatchRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchRunner batchRunner;

    private final Job dummyDataJob;
    private final Job processDataJob;

    @GetMapping("/dummy")
    public ResponseEntity<String> runDummyDataJob() {
        try {
            batchRunner.runJob(dummyDataJob);
            return ResponseEntity.ok("DummyData Job 실행 성공");
        } catch (Exception e) {
            log.error("DummyData Job 실행 실패", e);
            return ResponseEntity.internalServerError()
                    .body("DummyData Job 실행 실패: " + e.getMessage());
        }
    }

    @GetMapping("/process")
    public ResponseEntity<String> runProcessDataJob() {
        try {
            batchRunner.runJob(processDataJob);
            return ResponseEntity.ok("ProcessData Job 실행 성공");
        } catch (Exception e) {
            log.error("ProcessData Job 실행 실패", e);
            return ResponseEntity.internalServerError()
                    .body("ProcessData Job 실행 실패: " + e.getMessage());
        }
    }


    @PostMapping("/dummy/custom")
    public ResponseEntity<String> runDummyDataJobWithParams(
            @RequestBody Map<String, String> params) {
        try {
            // 동적으로 모든 파라미터 추가
            JobParametersBuilder builder = new JobParametersBuilder();

            for (String key : params.keySet()) {
                builder.addString(key, params.get(key));
            }

            JobParameters jobParameters = builder.toJobParameters();
            batchRunner.runJob(dummyDataJob, jobParameters);
            return ResponseEntity.ok("DummyData Job (커스텀 파라미터) 실행 성공");
        } catch (Exception e) {
            log.error("DummyData Job (커스텀 파라미터) 실행 실패", e);
            return ResponseEntity.internalServerError()
                    .body("DummyData Job 실행 실패: " + e.getMessage());
        }
    }
}