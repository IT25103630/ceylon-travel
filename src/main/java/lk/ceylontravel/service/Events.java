package lk.ceylontravel.service;

import java.util.concurrent.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Real-time event broadcasting and in-app notifications
 */
@Service
public class Events {
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> clients = new ConcurrentHashMap<>();
    private final JdbcTemplate jdbc;

    public Events(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public SseEmitter subscribe(long id) {
        var emitter = new SseEmitter(300000L);
        clients.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable remove = () -> clients.getOrDefault(id, new CopyOnWriteArrayList<>()).remove(emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());

        try {
            emitter.send(SseEmitter.event().name("ready").data("connected"));
        } catch (Exception e) {
            remove.run();
        }
        return emitter;
    }

    public void changed(long id, String kind) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { send(id, kind); }
            });
        } else {
            send(id, kind);
        }
    }

    private void send(long id, String kind) {
        var list = clients.get(id);
        if (list == null) return;
        for (var emitter : list) {
            try {
                emitter.send(SseEmitter.event().name("changed").data(kind));
            } catch (Exception e) {
                list.remove(emitter);
            }
        }
    }

    public void notify(long id, String text) {
        jdbc.update("INSERT INTO notifications(user_id, text) VALUES(?, ?)", id, text);
        changed(id, "notifications");
    }
}