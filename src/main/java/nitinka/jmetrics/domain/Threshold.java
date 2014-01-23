package nitinka.jmetrics.domain;

/**
 * Created with IntelliJ IDEA.
 * User: nitinka
 * Date: 20/1/14
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class Threshold {

    public static enum LEVEL{
        OK, WARNING, CRITICAL
    }

    public static enum CHECK{
        LT, BT, GT
    }

    private LEVEL level;
    private CHECK check;
    private double[] thresholdValues;

    public LEVEL getLevel() {
        return level;
    }

    public void setLevel(LEVEL level) {
        this.level = level;
    }

    public double[] getThresholdValues() {
        return thresholdValues;
    }

    public void setThresholdValues(double[] thresholdValues) {
        this.thresholdValues = thresholdValues;
    }

    public CHECK getCheck() {
        return check;
    }

    public void setCheck(CHECK check) {
        this.check = check;
    }

    public LEVEL doCheck(double value) {
        switch (check) {
            case LT:
                if(value < thresholdValues[0])
                    return level;
                break;

            case BT:
                if(value >= thresholdValues[0] && value <= thresholdValues[1])
                    return level;
                break;

            case GT:
                if(value > thresholdValues[0])
                    return level;
                break;
        }
        return LEVEL.OK;
    }

}
