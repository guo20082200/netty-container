import com.github.netty.core.support.LoggerFactoryX;
import com.github.netty.core.support.LoggerX;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一次 qps 统计
 * Created by acer01 on 2018/8/12/012.
 */
public class QpsOnceTest {

    int queryCount = 10000;//===========总调用次数=================
    int waitTime = 10;//===========等待时间(秒)=================
    AtomicInteger successCount = new AtomicInteger();
    AtomicInteger errorCount = new AtomicInteger();
    CountDownLatch latch = new CountDownLatch(queryCount);
    long totalTime;

    //==============Vertx客户端===============
    Vertx vertx = Vertx.vertx();
    WebClient client = WebClient.create(vertx,new WebClientOptions()
            .setTcpKeepAlive(false)
            //是否保持连接
            .setKeepAlive(true));

    public static void main(String[] args) throws InterruptedException {
        QpsOnceTest test = new QpsOnceTest();
        test.doQuery(Constant.PORT,Constant.HOST, Constant.URI);

        int successCount = test.successCount.get();
        int errorCount = test.errorCount.get();
        long totalTime = test.totalTime;

        test.client.close();
        test.vertx.close();

        logger.info("时间 = " + totalTime + "毫秒, " +
                "成功 = " + successCount + ", " +
                "失败 = " + errorCount + ", " +
                "qps = " + new BigDecimal((double) successCount/(double) totalTime * 1000).setScale(2,BigDecimal.ROUND_HALF_DOWN).stripTrailingZeros().toPlainString() +
                "\r\n==============================="
        );

    }

    private void doQuery(int port, String host, String uri) throws InterruptedException {
        long beginTime = System.currentTimeMillis();

        for(int i=0; i< queryCount; i++) {
            client.post(port, host, uri).sendJsonObject(Constant.BODY,asyncResult -> {
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
        totalTime = System.currentTimeMillis() - beginTime;
    }

    private static LoggerX logger = LoggerFactoryX.getLogger(QpsOnceTest.class);

}
