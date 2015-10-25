package com.aleongson.cloudcoffeeadmin;

/**
 * 25/10/2015.
 */
public class CoffeeMakerStatus {
    public static final int MAX_COFFEE_TSP = 500;
    public static final int MAX_SUGAR_TSP = 500;
    public static final int MAX_CREAMER_TSP = 500;
    public static final int MAX_WATER_CUP = 300;

    private int coffeeTsp;
    private int sugarTsp;
    private int creamerTsp;
    private int waterCup;

    private ErrorCode errorCode;

    private TrayStatus trayStatus[];
    private String trayOwner[];

    public CoffeeMakerStatus() {
        trayStatus = new TrayStatus[3];
        trayOwner = new String[3];

        coffeeTsp = 0;
        sugarTsp = 0;
        creamerTsp = 0;
        waterCup = 0;

        errorCode = ErrorCode.None;

        for(int i = 0; i < 3; i++) {
            trayStatus[i] = TrayStatus.Vacant;
            trayOwner[i] = "^";
        }
    }

    public int getCoffeeTsp() { return coffeeTsp; }
    public int getSugarTsp() { return sugarTsp; }
    public int getCreamerTsp() { return creamerTsp; }
    public int getWaterCup() { return waterCup; }
    public ErrorCode getErrorCode() { return errorCode; }
    public TrayStatus getTrayStatus(int index) { return trayStatus[index]; }
    public String getTrayOwner(int index) { return trayOwner[index]; }

    public void setCoffeeTsp(int coffeeTsp) { this.coffeeTsp = coffeeTsp; }
    public void setSugarTsp(int sugarTsp) { this.sugarTsp = sugarTsp; }
    public void setCreamerTsp(int creamerTsp) { this.creamerTsp = creamerTsp; }
    public void setWaterCup(int waterCup) { this.waterCup = waterCup; }

    public void setErrorCode(ErrorCode errorCode) { this.errorCode = errorCode; }
    public void setErrorCode(int errorCodeRaw) { this.errorCode = ErrorCode.fromInt(errorCodeRaw); }

    public void setTrayStatus(int index, TrayStatus trayStatus) { this.trayStatus[index] = trayStatus; }
    public void setTrayStatus(int index, int trayStatus) { this.trayStatus[index] = TrayStatus.fromInt(trayStatus); }

    public void setTrayOwner(int index, String trayOwner) { this.trayOwner[index] = trayOwner; }

    public enum ErrorCode {
        None(0), TrayUnaligned(1), IngredientShortSupply(2), TrayFull(3);

        int value;

        ErrorCode(int x) {
            value = x;
        }

        public static ErrorCode fromInt(int x) {
            switch(x) {
                case 0:
                    return None;
                case 1:
                    return TrayUnaligned;
                case 2:
                    return IngredientShortSupply;
                case 3:
                    return TrayFull;
            }
            return null;
        }
    }

    public enum TrayStatus {
        Vacant(0), Pending(1), Occupied(2);

        int value;

        TrayStatus(int x) {
            value = x;
        }

        public static TrayStatus fromInt(int x) {
            switch(x) {
                case 0:
                    return Vacant;
                case 1:
                    return Pending;
                case 2:
                    return Occupied;
            }
            return null;
        }

        public String toString() {
            switch(value) {
                case 0:
                    return "Vacant";
                case 1:
                    return "Pending";
                case 2:
                    return "Occupied";
            }
            return "";
        }
    }
}
