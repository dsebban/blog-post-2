package daniels.reactive.blog.util;


import rx.Notification;

/**
 * Created by daniel on 7/24/15.
 */
public class Utils {

    public static void printInfo(Notification n){
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        if(!n.isOnNext()) return;
        System.out.printf("%-10s[TID: %3s : %-3s] %s%n", "",
                Thread.currentThread().getId(),
                Thread.currentThread().getName(),n.getValue());
    }

}
