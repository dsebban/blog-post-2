package daniels.reactive.blog.ib;

import lombok.Value;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Created by daniel on 7/17/15.
 */
@Value
public class LiveBarEvent implements PriceEvent {
    public TimeUnit barDuration;
    public Long createTimestamp ;
    Instrument instrument;
    BigDecimal price;

    @Override
    public String toString() {
        return "LiveBarEvent{" +
                "min:" +  new DateTime(createTimestamp).minuteOfHour().get() +"-sec:"+new DateTime(createTimestamp).secondOfMinute().get()+"-ms:"+new DateTime(createTimestamp).millisOfSecond().get()+
                ", instrument=" + instrument +
                ", price=" + price.toPlainString() +
                '}';
    }
}
