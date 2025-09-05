package bankapp.auth.infrastructure.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Aspect
@Component
@RequiredArgsConstructor
public class UseCaseTransactionalAspect {

    private final TransactionTemplate transactionTemplate;

    @Around("@annotation(bankapp.auth.domain.model.annotations.TransactionalUseCase)")
    public Object executeInTransaction(ProceedingJoinPoint joinPoint) {

        return transactionTemplate.execute(_ -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (e instanceof RuntimeException exception) {
                    throw exception;
                }
                throw new RuntimeException("Transaction failed. Rolling back", e);
            }
        });
    }
}
