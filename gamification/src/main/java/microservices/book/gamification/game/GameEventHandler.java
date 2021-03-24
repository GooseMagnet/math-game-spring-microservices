package microservices.book.gamification.game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microservices.book.gamification.challenge.ChallengeSolvedEvent;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class GameEventHandler {

    private final GameService gameService;

    @RabbitListener(queues = "${amqp.queue.gamification}")
    void handleMultiplicationSolved(final ChallengeSolvedEvent event) {
        log.info("Challenge Solved Event received: {}", event.getAttemptId());
        try {
            gameService.newAttemptForUser(event);
        } catch (final Exception e) {
            log.error("Error when trying to process ChallengeSolvedEvent", e);
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}
