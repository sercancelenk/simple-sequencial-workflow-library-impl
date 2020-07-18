package byzas.example.simpleworkflow.core.workflow;

import byzas.example.simpleworkflow.core.context.AbstractContext;
import byzas.example.simpleworkflow.core.exception.WorkflowException;
import byzas.example.simpleworkflow.core.workflowstep.WorkflowStep;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * @author ext0280263 on 18.07.2020
 * @project IntelliJ IDEA
 */

@Log4j2
@Getter
abstract class WorkflowImpl implements IWorkflow {
    protected final List<WorkflowStep> steps;
    protected final String workFlowName;

    public WorkflowImpl(String workFlowName, List<WorkflowStep> steps) {
        this.steps = steps;
        this.workFlowName = workFlowName;
    }

    @Override
    public CompletableFuture<Boolean> processWorkflowFuture(AbstractContext context) {
        List<WorkflowStep> steps = getWorkflowSteps();
        log.info("[WORKFLOW] [{}] Started.", getWorkFlowName());
        return steps.stream()
                .reduce(CompletableFuture.completedFuture(true),
                        (f, method) -> f.thenApplyAsync(result -> {
                            try {
                                return result && method.doActionFuture(context)
                                        .handle((r, t) -> {
                                            if (Optional.ofNullable(t).isPresent()) {
                                                log.info("[WORKFLOW-STEP] [{}] completed exceptionally.", method.getName());
                                                CompletableFuture<Boolean> rr = CompletableFuture.completedFuture(false);
                                                rr.completeExceptionally(new RuntimeException(t));
                                                return rr;
                                            }
                                            log.info("[WORKFLOW-STEP] [{}] completed successfully.", method.getName());
                                            return CompletableFuture.completedFuture(r);
                                        })
                                        .thenComposeAsync(Function.identity())
                                        .join();
                            } catch (Exception e) {
                                log.error("[WORKFLOW-ERROR] [{}] - [{}]", this.getWorkFlowName(), method.getName(), e);
                                throw new WorkflowException(e);
                            }
                        }),
                        (f1, f2) -> f1.thenCombine(f2, (result1, result2) -> result1 && result2))
                .handle((r, ex) -> {
                    if (ex instanceof CompletionException) {
                        log.info("[WORKFLOW] [{}] completed exceptionally.", getWorkFlowName());
                    } else {
                        log.info("[WORKFLOW] [{}] completed successfully.", getWorkFlowName());
                    }

                    return r;
                });
    }

    @Override
    public Mono<Boolean> processWorkflow(AbstractContext context) {
        List<WorkflowStep> steps = getWorkflowSteps();
        log.info("[WORKFLOW] [{}] Started.", getWorkFlowName());
        log.info("[{}] Steps", getWorkFlowName());
        steps.stream().forEach(action -> log.info("- {}", action.getName()));
        log.info("----------------------------------------------------");
        return steps.stream()
                .reduce(Mono.just(true),
                        (f, method) -> f.flatMap(result -> {
                            if (result) {
                                log.info("[WORKFLOW-STEP] [{}] started.", method.getName());
                                return method.doAction(context)
                                        .onErrorResume(t -> {
                                            log.info("[WORKFLOW-STEP] [{}] completed exceptionally.", method.getName());
                                            return Mono.error(new RuntimeException(t));
                                        })
                                        .doOnSuccess(d -> {
                                            log.info("[WORKFLOW-STEP] [{}] completed successfully.", method.getName());
                                        });

                            } else {
                                return Mono.just(false);
                            }
                        }),
                        (f1, f2) -> f1.zipWith(f2, (result1, result2) -> result1 && result2))
                .doOnError(t -> {
                    log.error("[WORKFLOW] [{}] completed exceptionally.", getWorkFlowName(), t.getCause());
                })
                .doOnSuccess(d -> {
                    log.info("[WORKFLOW] [{}] completed successfully.", getWorkFlowName());
                });
    }

    private List<WorkflowStep> getWorkflowSteps() {
        List<WorkflowStep> steps = getSteps();
        if (CollectionUtils.isEmpty(steps)) {
            log.error("There is no defined step for " + getWorkFlowName());
            throw new IllegalArgumentException("There is no defined step for " + getWorkFlowName());
        }
        return steps;
    }


}