package daniels.reactive.blog;


import daniels.reactive.blog.ib.Instruments;
import daniels.reactive.blog.ib.LiveBarEvent;
import daniels.reactive.blog.ib.LivePriceEvent;
import daniels.reactive.blog.ib.PriceEvent;
import daniels.reactive.blog.rx.MarketDataObservable;
import lombok.val;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import static daniels.reactive.blog.ib.Instruments.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by daniel on 7/17/15.
 */
public class MarketDataObservableTest {

    MarketDataObservable observable;

    @Before public void setUp(){
        observable = new MarketDataObservable();

    }
    @Test
    public void twoPricesWithinTheSameSecondProducesOneMinuteBar() throws InterruptedException {

        val priceAtMinute1Sec0A =  event(APPL,time(minute(1), sec(0)), price(10.0));
        val priceAtMinute1Sec30A =  event(APPL,time(minute(1), sec(30)), price(20.0));
        val priceAMinute2Sec1A =    event(APPL,time(minute(2), sec(1)), price(20.0));

        val priceAtMinute1Sec0G =  event(GOOG,time(minute(1), sec(0)), price(10.0));
        val priceAtMinute1Sec30G =  event(GOOG,time(minute(1), sec(30)), price(20.0));
        val priceAMinute2Sec1G =    event(GOOG,time(minute(2), sec(1)), price(20.0));

        observable.aggregateLiveMinuteBar();

        assertThatWhenPush(l(priceAtMinute1Sec0A,priceAtMinute1Sec0G, priceAtMinute1Sec30A,priceAtMinute1Sec30G,priceAMinute2Sec1A,priceAMinute2Sec1G),
                produces(
                        minuteBar(GOOG,time(minute(1), sec(30)), price(20.0)),
                        minuteBar(APPL, time(minute(1), sec(30)), price(20.0))));

    }



    private <T> List<T> produces(T... val) {
        return l(val);
    }
    private <T> List<T> l(T ... val){
        return Arrays.asList(val);
    }

    private void assertThatWhenPush(List<PriceEvent> push ,List<LiveBarEvent> expected){

        CountDownLatch latch = new CountDownLatch(expected.size());
        Collections.reverse(expected);

        observable.observable().ofType(LiveBarEvent.class).subscribe((b) -> {

                    assertThat(b.getCreateTimestamp(), is(expected.get((int) latch.getCount() - 1).getCreateTimestamp()));
                    assertThat(b.getPrice().stripTrailingZeros(), is(expected.get((int) latch.getCount() - 1).getPrice()));
                    latch.countDown();
                }
        );

        push.forEach(observable::push);

        try {
            boolean succeed = latch.await(3000, TimeUnit.MILLISECONDS );
            if(!succeed) fail("no element produced");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    LivePriceEvent event(Instruments ins,long time , BigDecimal p){
        return new LivePriceEvent(time, ins.val(),p);
    }

    LiveBarEvent minuteBar(Instruments ins,long time , BigDecimal p){
        return new LiveBarEvent(TimeUnit.MINUTES,time, ins.val(),p);
    }


    BigDecimal price(double price){

        return new BigDecimal(String.valueOf(price)).stripTrailingZeros();

    }


    long time(int minute , int sec ){

        return new DateTime(2013, 03, 10, 12, minute, sec, 0).getMillis();

    }

    int minute(int minute ){

        return minute;

    }

    int sec(int second ){

        return second;

    }


}
