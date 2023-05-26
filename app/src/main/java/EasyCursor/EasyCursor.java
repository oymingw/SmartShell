package EasyCursor;

import android.database.Cursor;

public interface EasyCursor extends Cursor {
    byte[] getBlob(String var1);

    boolean getBoolean(String var1);

    double getDouble(String var1);

    float getFloat(String var1);

    int getInt(String var1);

    long getLong(String var1);

    EasyQueryModel getQueryModel();

    String getString(String var1);

    boolean isNull(String var1);

    boolean optBoolean(String var1);

    boolean optBoolean(String var1, boolean var2);

    Boolean optBooleanAsWrapperType(String var1);

    double optDouble(String var1);

    double optDouble(String var1, double var2);

    Double optDoubleAsWrapperType(String var1);

    float optFloat(String var1);

    float optFloat(String var1, float var2);

    Float optFloatAsWrapperType(String var1);

    int optInt(String var1);

    int optInt(String var1, int var2);

    Integer optIntAsWrapperType(String var1);

    long optLong(String var1);

    long optLong(String var1, long var2);

    Long optLongAsWrapperType(String var1);

    String optString(String var1);

    String optString(String var1, String var2);
}
