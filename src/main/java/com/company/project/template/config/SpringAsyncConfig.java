package com.company.project.template.config;

import io.micrometer.context.ContextSnapshot;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Slf4j
public class SpringAsyncConfig {

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("MsTemplateProjectAsyncThread-");
        executor.setTaskDecorator(new ContextPropagationTaskDecorator());
        executor.initialize();
        log.debug("Created Async Task Executor");

        return executor;
    }

    static class ContextPropagationTaskDecorator implements TaskDecorator {

        @NotNull
        @Override
        public Runnable decorate(@NotNull Runnable runnable) {
            var mdcContextMap = MDC.getCopyOfContextMap();
            var micrometerContextSnapshot = ContextSnapshot.captureAll();
            return micrometerContextSnapshot.wrap(() -> {
                try {
                    if (mdcContextMap != null) {
                        MDC.setContextMap(mdcContextMap);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            });
        }
    }

}
