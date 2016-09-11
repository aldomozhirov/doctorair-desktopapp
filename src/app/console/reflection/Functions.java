package app.console.reflection;

/**
 * Created by Alexey on 17.08.2016.
 */
public class Functions {

    public static String getModeName(int mode) {
        switch(mode) {
            case -1:
                return "In service";
            case 0:
                return "Switched off";
            case 1:
                return "Always on";
            case 2:
                return "";
            case 3:
                return "";
            case 4:
                return "";
            case 5:
                return "";
            case 6:
                return "";
            case 7:
                return "";
            case 8:
                return "";
            default:
                return "";
        }
    }

}
