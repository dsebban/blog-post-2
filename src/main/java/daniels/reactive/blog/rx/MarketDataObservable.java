package daniels.reactive.blog.rx;

import com.ib.client.Types;
import daniels.reactive.blog.ib.LiveBarEvent;
import daniels.reactive.blog.ib.LivePriceEvent;
import daniels.reactive.blog.ib.PriceEvent;
import daniels.reactive.blog.util.Utils;
import lombok.extern.java.Log;
import org.joda.time.DateTime;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by daniel on 7/17/15.
 */
@Log
public class MarketDataObservable {

    private final PublishSubject<PriceEvent> publishSubject;

    public MarketDataObservable() {
        publishSubject = PublishSubject.create();
    }

    public void aggregateLiveMinuteBar() {

        observable().
                ofType(LivePriceEvent.class). //filter on live ticks
                groupBy(LivePriceEvent::getInstrument). // group by instrument i.e AAPL, GOOG
                flatMap(grouped -> grouped.buffer(2, 1)). // take each 2 consecutive events
                //until here same as Part 1


                //1. instead of subscribe we use flatMap , to produce a fresh observable
                flatMap(listOf2 ->

                        //2. ---Generate One Minute Bar Task -----
                        Observable.defer(() -> {
                            //Logic to check if the minute has been crossed

                            //simulate heavy computation
                            sleepALittle();

                            LivePriceEvent lastEvent = listOf2.get(0);
                            int lastMinute = new DateTime(lastEvent.getCreateTimestamp()).minuteOfHour().get();
                            int currentMinute = new DateTime(listOf2.get(1).getCreateTimestamp()).minuteOfHour().get();
                            //3. when minute is crossed , we create a new observable with the minute in it
                            if (lastMinute != currentMinute) {
                                System.out.println("[generating minute bar]" + Thread.currentThread().getName());
                                //4.
                                return Observable.just(new LiveBarEvent(TimeUnit.MINUTES, lastEvent.createTimestamp, lastEvent.getInstrument(), lastEvent.getPrice()));
                            }

                            System.out.println("[ignoring event ]" + Thread.currentThread().getName());
                            //5.
                            return Observable.empty();

                        }).
                                //6. every Task get executed on the computation thread pool
                                        subscribeOn(Schedulers.computation()).
                                doOnEach(Utils::printInfo)


        ).//7. end of flatmap



                //8. push the result back to make it available for subscribers
                subscribe(this::push);


    }

    public Observable<PriceEvent> observable() {
        return publishSubject.asObservable();
    }


    public void push(PriceEvent priceEvent) {

        publishSubject.onNext(priceEvent);
    }


    private void sleepALittle(){
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void error(Exception e) {
        publishSubject.onError(e);
    }
}
