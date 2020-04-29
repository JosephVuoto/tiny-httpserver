import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool extends ThreadPoolExecutor {

    private static ThreadPool instance;

    private ThreadPool() {
        super(5, 10, 1000,
                TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public synchronized static ThreadPool getInstance() {
        if (instance == null) {
            synchronized (ThreadPool.class) {
                if (instance == null) {
                    instance = new ThreadPool();
                }
            }
        }
        return instance;
    }

}
