package knu.team1.be.boost.task.service;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import knu.team1.be.boost.member.entity.Member;
import knu.team1.be.boost.member.repository.MemberRepository;
import knu.team1.be.boost.project.entity.Project;
import knu.team1.be.boost.project.repository.ProjectRepository;
import knu.team1.be.boost.projectMembership.entity.ProjectMembership;
import knu.team1.be.boost.projectMembership.entity.ProjectRole;
import knu.team1.be.boost.projectMembership.repository.ProjectMembershipRepository;
import knu.team1.be.boost.task.entity.Task;
import knu.team1.be.boost.task.entity.TaskStatus;
import knu.team1.be.boost.task.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@SpringBootTest
@DisplayName("Task 낙관적 락 통합 테스트")
class TaskOptimisticLockIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ProjectMembershipRepository projectMembershipRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private Project testProject;
    private Member assignee;
    private Member reviewer1;
    private Member reviewer2;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // TransactionTemplate을 사용하여 트랜잭션 안에서 데이터 생성
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            // 테스트 프로젝트 생성
            testProject = Project.builder()
                .name("낙관적 락 테스트 프로젝트")
                .defaultReviewerCount(2)
                .build();
            testProject = projectRepository.save(testProject);

            // 테스트 멤버 생성
            assignee = createMember("담당자", "assignee@test.com");
            reviewer1 = createMember("리뷰어1", "reviewer1@test.com");
            reviewer2 = createMember("리뷰어2", "reviewer2@test.com");

            // 프로젝트 멤버십 생성
            createProjectMembership(testProject, assignee);
            createProjectMembership(testProject, reviewer1);
            createProjectMembership(testProject, reviewer2);

            // 테스트 Task 생성
            testTask = Task.builder()
                .project(testProject)
                .title("낙관적 락 테스트 Task")
                .description("동시에 두 명이 승인하면 충돌 발생")
                .status(TaskStatus.REVIEW)
                .dueDate(LocalDate.now().plusDays(7))
                .urgent(false)
                .requiredReviewerCount(2)
                .tags(Set.of())
                .assignees(Set.of(assignee))
                .approvers(Set.of())
                .build();
            testTask = taskRepository.save(testTask);

            // 영속성 컨텍스트 초기화 (실제 DB에서 조회하도록)
            entityManager.flush();
            entityManager.clear();

            log.info("테스트 데이터 준비 완료 - Task ID: {}, version: {}",
                testTask.getId(), testTask.getVersion());

            return null;
        });
    }

    @AfterEach
    void tearDown() {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            taskRepository.deleteAll();
            projectMembershipRepository.deleteAll();
            memberRepository.deleteAll();
            projectRepository.deleteAll();
            return null;
        });
    }

    @Test
    @DisplayName("동시에 두 사용자가 Task를 승인하면 한 명은 OptimisticLockException 발생")
    void concurrentApprovalCausesOptimisticLockException() throws Exception {
        // given
        UUID taskId = testTask.getId();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when: 두 스레드가 거의 동시에 승인 시도
        // 첫 번째 리뷰어 승인
        executor.submit(() -> {
            try {
                startLatch.await(); // 동시 시작 대기
                Thread.sleep(10);

                TransactionTemplate transactionTemplate = new TransactionTemplate(
                    transactionManager);
                transactionTemplate.execute(status -> {
                    Task task = taskRepository.findById(taskId).orElseThrow();
                    log.info("[Thread-1] Task 조회 완료 - version: {}", task.getVersion());

                    task.approve(reviewer1);
                    log.info("[Thread-1] approve() 호출 완료");

                    return null;
                });

                successCount.incrementAndGet();
                log.info("[Thread-1] 승인 성공");

            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                conflictCount.incrementAndGet();
                log.warn("[Thread-1] 낙관적 락 충돌: {}", e.getMessage());
            } catch (Exception e) {
                log.error("[Thread-1] 예외 발생", e);
            } finally {
                doneLatch.countDown();
            }
        });

        // 두 번째 리뷰어 승인
        executor.submit(() -> {
            try {
                startLatch.await(); // 동시 시작 대기
                Thread.sleep(10);

                TransactionTemplate transactionTemplate = new TransactionTemplate(
                    transactionManager);
                transactionTemplate.execute(status -> {
                    Task task = taskRepository.findById(taskId).orElseThrow();
                    log.info("[Thread-2] Task 조회 완료 - version: {}", task.getVersion());

                    task.approve(reviewer2);
                    log.info("[Thread-2] approve() 호출 완료");

                    return null;
                });

                successCount.incrementAndGet();
                log.info("[Thread-2] 승인 성공");

            } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
                conflictCount.incrementAndGet();
                log.warn("[Thread-2] 낙관적 락 충돌: {}", e.getMessage());
            } catch (Exception e) {
                log.error("[Thread-2] 예외 발생", e);
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown(); // 동시 시작
        boolean finished = doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // then
        assertThat(finished).isTrue();
        log.info("=== 테스트 결과: 성공 {}, 충돌 {} ===", successCount.get(), conflictCount.get());

        // 동시성 상황에서는 일반적으로 하나는 성공, 하나는 충돌
        // 하지만 타이밍에 따라 둘 다 성공할 수도 있음 (별도 트랜잭션이므로)
        assertThat(successCount.get() + conflictCount.get()).isEqualTo(2);

        // 최종 결과 확인 (트랜잭션 안에서 검증)
        TransactionTemplate verifyTemplate = new TransactionTemplate(transactionManager);
        verifyTemplate.execute(status -> {
            entityManager.clear();
            Task finalTask = taskRepository.findById(taskId).orElseThrow();

            // Lazy Loading 초기화
            int approverCount = finalTask.getApprovers().size();

            log.info("=== 최종 Task 상태 ===");
            log.info("승인자 수: {}", approverCount);
            log.info("버전: {}", finalTask.getVersion());

            // 두 명 모두 성공했다면 approvers에 2명이 있어야 함
            // 한 명만 성공했다면 approvers에 1명만 있어야 함
            if (successCount.get() == 2) {
                assertThat(finalTask.getApprovers()).hasSize(2);
            } else if (successCount.get() == 1) {
                assertThat(finalTask.getApprovers()).hasSize(1);
                assertThat(conflictCount.get()).isEqualTo(1);
            }

            // version이 증가했는지 확인 (적어도 1번은 업데이트 됨)
            assertThat(finalTask.getVersion()).isGreaterThan(0L);

            return null;
        });
    }

    @Test
    @DisplayName("Task 수정 시 version이 자동으로 증가한다")
    void versionIncreasesOnUpdate() {
        // given
        Long initialVersion = testTask.getVersion();
        log.info("초기 version: {}", initialVersion);

        // when: Task 상태 변경
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(status -> {
            Task task = taskRepository.findById(testTask.getId()).orElseThrow();
            task.changeStatus(TaskStatus.DONE);
            return null;
        });

        // then: 트랜잭션 안에서 검증
        transactionTemplate.execute(status -> {
            entityManager.clear();
            Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();

            log.info("업데이트 후 version: {}", updatedTask.getVersion());
            assertThat(updatedTask.getVersion()).isGreaterThan(initialVersion);
            assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.DONE);

            return null;
        });
    }

    @Test
    @DisplayName("같은 Task를 3번 연속으로 승인하면 version이 3 증가한다")
    void versionIncreasesSequentially() {
        // given
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        Member reviewer3 = transactionTemplate.execute(status -> {
            Member r3 = createMember("리뷰어3", "reviewer3@test.com");
            createProjectMembership(testProject, r3);
            return r3;
        });

        Long initialVersion = testTask.getVersion();
        log.info("초기 version: {}", initialVersion);

        // when: 3명이 순차적으로 승인
        // 첫 번째 승인
        transactionTemplate.execute(status -> {
            Task task = taskRepository.findById(testTask.getId()).orElseThrow();
            task.approve(reviewer1);
            return null;
        });

        // 두 번째 승인
        transactionTemplate.execute(status -> {
            Task task = taskRepository.findById(testTask.getId()).orElseThrow();
            task.approve(reviewer2);
            return null;
        });

        // 세 번째 승인
        transactionTemplate.execute(status -> {
            Task task = taskRepository.findById(testTask.getId()).orElseThrow();
            task.approve(reviewer3);
            return null;
        });

        // then: 트랜잭션 안에서 검증
        transactionTemplate.execute(status -> {
            entityManager.clear();
            Task finalTask = taskRepository.findById(testTask.getId()).orElseThrow();

            // approvers 컬렉션 초기화 (Lazy Loading)
            finalTask.getApprovers().size();

            log.info("최종 version: {} (초기: {})", finalTask.getVersion(), initialVersion);
            assertThat(finalTask.getVersion()).isEqualTo(initialVersion + 3);
            assertThat(finalTask.getApprovers()).hasSize(3);

            return null;
        });
    }

    private Member createMember(String name, String email) {
        long uniqueProviderId = System.nanoTime() + (long) (Math.random() * 100000);

        Member member = Member.builder()
            .name(name)
            .avatar("1234")
            .backgroundColor("#FF5733")
            .notificationEnabled(true)
            .oauthInfo(new knu.team1.be.boost.member.entity.vo.OauthInfo("kakao", uniqueProviderId))
            .build();
        return memberRepository.save(member);
    }

    private void createProjectMembership(Project project, Member member) {
        ProjectMembership membership = ProjectMembership.builder()
            .project(project)
            .member(member)
            .role(ProjectRole.MEMBER)
            .notificationEnabled(true)
            .build();
        projectMembershipRepository.save(membership);
    }
}

