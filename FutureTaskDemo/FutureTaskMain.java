import java.util.concurrent.Callable;

public class FutureTaskMain {
    public static void main(String[] args) {
        long s = System.currentTimeMillis();
        Callable<Doing> doingThread = () -> {
            Thread.sleep(5000);
            return new Doing();
        };
        try {
            java.util.concurrent.FutureTask<Doing> task = new java.util.concurrent.FutureTask<>(doingThread);
            new Thread(task).start();
            Thread.sleep(2000);
            while (!task.isDone()){}
            Doing doing = task.get();
            long e = System.currentTimeMillis();
            System.out.println("耗费时间为:" + (e - s));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class Doing {}
}
