package events;

public class ErrorEvents {

    public static void onErrorEvent(Exception e) {
        e.printStackTrace();
    }
}
