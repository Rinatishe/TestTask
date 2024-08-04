import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Logger {
    private final Map<String, Long> messageMap;
    private final TreeMap<Long, String> timestampMap;
    private final long EXPIRATION_TIME = 10_000; // 10 секунд в миллисекундах
    private final int MAX_CAPACITY = 100;

    public Logger() {
        messageMap = new HashMap<>();
        timestampMap = new TreeMap<>();
    }

    public synchronized boolean shouldPrintMessage(int timestamp, String message) {
        clean(timestamp);

        if (messageMap.size() >= MAX_CAPACITY) {
            return false; // Система заполнена, не можем добавить новое сообщение
        }

        if (!messageMap.containsKey(message)) {
            messageMap.put(message, (long) timestamp);
            timestampMap.put((long) timestamp, message);
            return true;
        } else {
            long existingTimestamp = messageMap.get(message);
            if (timestamp >= existingTimestamp + EXPIRATION_TIME) {
                messageMap.put(message, (long) timestamp);
                timestampMap.put((long) timestamp, message);
                return true;
            } else {
                return false;
            }
        }
    }

    public synchronized boolean clean(int timestamp) {
        if (timestampMap.isEmpty() || timestampMap.firstKey() > timestamp) {
            return false; // Нельзя очистить, если нет сообщений или текущее время меньше самого раннего сообщения
        }

        while (!timestampMap.isEmpty() && timestampMap.firstKey() < timestamp - EXPIRATION_TIME) {
            long timestampToRemove = timestampMap.firstKey();
            String messageToRemove = timestampMap.remove(timestampToRemove);
            messageMap.remove(messageToRemove);
        }

        return true;
    }

    public synchronized int loggerSize() {
        return messageMap.size();
    }

    public static void main(String[] args) {
        Logger logger = new Logger();

        // Примеры использования
        System.out.println(logger.shouldPrintMessage(1, "foo")); // true
        System.out.println(logger.shouldPrintMessage(2, "bar")); // true
        System.out.println(logger.shouldPrintMessage(3, "foo")); // false
        System.out.println(logger.shouldPrintMessage(11, "foo")); // true, т.к. прошло больше 10 секунд
        System.out.println(logger.clean(12)); // true
        System.out.println(logger.loggerSize()); // возвращает размер логгера
    }
}
