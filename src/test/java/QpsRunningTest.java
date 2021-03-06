import com.github.netty.core.support.LoggerFactoryX;
import com.github.netty.core.support.LoggerX;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * running 测试
 * Created by acer01 on 2018/8/12/012.
 */
public class QpsRunningTest {

    int queryCount = 10000;//===========一次qps任务的调用次数=================
    int waitTime = 10;//===========一次qps任务的等待时间(秒)=================

    static final long reportPrintTime = 5;//===========qps统计间隔时间(秒)=================
    static final long onceSleep = 300;//===========下次调用qps任务的暂停时间(毫秒)=================

    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger errorCount = new AtomicInteger();
    AtomicLong totalSleepTime = new AtomicLong();

    //==============Vertx客户端===============
    Vertx vertx = Vertx.vertx();
    WebClient client = WebClient.create(vertx,new WebClientOptions()
            .setTcpKeepAlive(false)
            //是否保持连接
            .setKeepAlive(true));

    public static void main(String[] args) throws InterruptedException {
        QpsRunningTest test = new QpsRunningTest();
        new PrintThread(test).start();

        try {
            while (true) {
                test.doQuery(Constant.PORT,Constant.HOST, Constant.URI);
            }
        }catch (Throwable t){
            t.printStackTrace();
        }

    }

    private void doQuery(int port, String host, String uri) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(queryCount);
        for(int i=0 ;i< queryCount; i++) {
            client.get(port, host, uri).sendJsonObject(Constant.BODY, asyncResult -> {
                if(asyncResult.succeeded()){
                    successCount.incrementAndGet();
                }else {
                    errorCount.incrementAndGet();
                    System.out.println("error = " + asyncResult.cause());
                }
                latch.countDown();
            });
        }

        latch.await(waitTime, TimeUnit.SECONDS);
        Thread.sleep(onceSleep);
        totalSleepTime.addAndGet(onceSleep);
    }

    static class PrintThread extends Thread{
        private final QpsRunningTest test;
        private AtomicInteger printCount = new AtomicInteger();
        private long beginTime = System.currentTimeMillis();
        private LoggerX logger = LoggerFactoryX.getLogger(getClass());

        public PrintThread(QpsRunningTest test) {
            super("QpsPrintThread");
            this.test = test;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sleep(reportPrintTime * 1000);
//                    synchronized (test) {
                    long totalTime = System.currentTimeMillis() - beginTime - test.totalSleepTime.get();
                    printQps(test.successCount.get(), test.errorCount.get(), totalTime);
//                    }
                }catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }

        private void printQps(int successCount, int errorCount, long totalTime){
            logger.info(
                    "第("+printCount.incrementAndGet()+")次统计, "+
                            "时间 = " + totalTime + "毫秒["+(totalTime/60000)+"分"+((totalTime % 60000 ) / 1000)+"秒], " +
                            "成功 = " + successCount + ", " +
                            "失败 = " + errorCount + ", " +
                            "平均响应 = " + new BigDecimal((double) totalTime/(double) successCount).setScale(2,BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros().toPlainString() + "ms, " +
                            "qps = " + new BigDecimal((double) successCount/(double) totalTime * 1000).setScale(2,BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros().toPlainString()
//                            +
//                            "\r\n==============================="
            );
        }
    }

}
