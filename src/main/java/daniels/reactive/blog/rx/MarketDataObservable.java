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

                //instead of subscribe we use flatMap , to produce a fresh observable
                flatMap(listOf2 ->


                //each time some one will subscribe look
                Observable.defer(() -> {
                    LivePriceEvent lastEvent = listOf2.get(0);
                    int lastMinute = new DateTime(lastEvent.getCreateTimestamp()).minuteOfHour().get();
                    int currentMinute = new DateTime(listOf2.get(1).getCreateTimestamp()).minuteOfHour().get();
                    //when minute is crossed , we push the result back in the observable to make it available to other subscribers
                    if (lastMinute != currentMinute) {
                        System.out.println("[generating minute bar]" + Thread.currentThread().getName());
                        return Observable.just(new LiveBarEvent(TimeUnit.MINUTES, lastEvent.createTimestamp, lastEvent.getInstrument(), lastEvent.getPrice()));
                    }
                    System.out.println("[ignoring event ]" + Thread.currentThread().getName());
                    return Observable.empty();

                }).
//                        doOnEach(Utils::printInfo).
//                        doOnSubscribe(() -> System.out.println("[subscribing ]" + Thread.currentThread().getName())).
                        subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))).


                subscribe(this::push);


    }

    public Observable<PriceEvent> observable() {
        return publishSubject.asObservable();
    }


    public void push(PriceEvent priceEvent) {

        publishSubject.onNext(priceEvent);
    }

    public void error(Exception e) {
        publishSubject.onError(e);
    }
}
