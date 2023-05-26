package EasyCursor;

import android.database.AbstractCursor;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class EasyObjectCursor<T> extends AbstractCursor implements EasyCursor {
    private static final String _ID = "_id";
    private static final String IS = "is";
    private static final String GET = "get";
    public static final String DEFAULT_STRING = null;
    public static final boolean DEFAULT_BOOLEAN = false;
    public static final double DEFAULT_DOUBLE = 0.0D;
    public static final float DEFAULT_FLOAT = 0.0F;
    public static final int DEFAULT_INT = 0;
    public static final long DEFAULT_LONG = 0L;
    public static final short DEFAULT_SHORT = 0;
    private final EasyQueryModel mQueryModel;
    private final List<Method> mMethodList;
    private final List<String> mFieldNameList;
    private final List<T> mObjectList;
    private final Map<String, Integer> mFieldToIndexMap;
    private final Map<String, Method> mFieldToMethodMap;
    private final String TAG;
    private final String m_IdAlias;
    private boolean mDebugEnabled;

    public EasyObjectCursor(Class<T> clazz, List<T> objectList, String _idAlias) {
        this(clazz, (List)objectList, _idAlias, (EasyQueryModel)null);
    }

    public EasyObjectCursor(Class<T> clazz, List<T> objectList, String _idAlias, EasyQueryModel model) {
        this.TAG = this.getClass().getName();
        this.mQueryModel = model;
        this.mFieldToIndexMap = new HashMap();
        this.mFieldToMethodMap = Collections.synchronizedMap(new HashMap());
        this.mMethodList = new ArrayList();
        this.mFieldNameList = new ArrayList();
        this.mObjectList = objectList;
        this.m_IdAlias = _idAlias;
        this.populateMethodList(clazz);
    }

    public EasyObjectCursor(Class<T> clazz, T[] objectArray, String _idAlias) {
        this(clazz, (List)(new ArrayList(Arrays.asList(objectArray))), _idAlias, (EasyQueryModel)null);
    }

    public EasyObjectCursor(Class<T> clazz, T[] objectArray, String _idAlias, EasyQueryModel model) {
        this(clazz, (List)(new ArrayList(Arrays.asList(objectArray))), _idAlias, model);
    }

    private String applyAlias(String columnName) {
        return "_id".equals(columnName) && this.m_IdAlias != null ? this.m_IdAlias : columnName;
    }

    public byte[] getBlob(String fieldName) {
        Method method = this.getGetterForFieldOrThrow(this.applyAlias(fieldName));
        return ObjectConverters.toByteArray(this.runGetter(method, this.getItem(this.getPosition())));
    }

    public boolean getBoolean(String fieldName) {
        Method method = this.getGetterForFieldOrThrow(this.applyAlias(fieldName));
        return ObjectConverters.toBoolean(this.runGetter(method, this.getItem(this.getPosition())));
    }

    public int getColumnIndex(String columnName) {
        String column = this.applyAlias(columnName);
        return this.mFieldToIndexMap.containsKey(column) ? (Integer)this.mFieldToIndexMap.get(column) : -1;
    }

    public int getColumnIndexOrThrow(String columnName) {
        String column = this.applyAlias(columnName);
        if (this.mFieldToIndexMap.containsKey(column)) {
            return (Integer)this.mFieldToIndexMap.get(column);
        } else {
            throw new IllegalArgumentException("There is no column named '" + column + "'");
        }
    }

    public String getColumnName(int columnIndex) {
        return (String)this.mFieldNameList.get(columnIndex);
    }

    public String[] getColumnNames() {
        return (String[])this.mFieldNameList.toArray(new String[this.mFieldNameList.size()]);
    }

    public int getCount() {
        return this.mObjectList.size();
    }

    public double getDouble(int column) {
        return this.getDouble((Method)this.mMethodList.get(column));
    }

    private double getDouble(Method method) {
        return ObjectConverters.toDouble(this.runGetter(method, this.getItem(this.getPosition())));
    }

    public double getDouble(String fieldName) {
        return this.getDouble(this.getGetterForFieldOrThrow(this.applyAlias(fieldName)));
    }

    public float getFloat(int column) {
        return this.getFloat((Method)this.mMethodList.get(column));
    }

    private float getFloat(Method method) {
        return ObjectConverters.toFloat(this.runGetter(method, this.getItem(this.getPosition())));
    }

    public float getFloat(String fieldName) {
        return this.getFloat(this.getGetterForFieldOrThrow(this.applyAlias(fieldName)));
    }

    private synchronized Method getGetterForField(String field) {
        if (this.mFieldToMethodMap.containsKey(field)) {
            return (Method)this.mFieldToMethodMap.get(field);
        } else {
            String booleanField = "is" + field.toLowerCase(Locale.US);
            String otherField = "get" + field.toLowerCase(Locale.US);
            Method methodResult = null;
            Iterator var6 = this.mMethodList.iterator();

            while(var6.hasNext()) {
                Method method = (Method)var6.next();
                if (method.getName().toLowerCase(Locale.US).equals(booleanField)) {
                    methodResult = method;
                    break;
                }

                if (method.getName().toLowerCase(Locale.US).equals(otherField)) {
                    methodResult = method;
                    break;
                }
            }

            this.mFieldToMethodMap.put(field, methodResult);
            return methodResult;
        }
    }

    private Method getGetterForFieldOrThrow(String fieldName) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            throw new IllegalArgumentException("Could not find getter for field '" + this.applyAlias(fieldName) + "'");
        } else {
            return method;
        }
    }

    public int getInt(int column) {
        return this.getInt((Method)this.mMethodList.get(column));
    }

    private int getInt(Method method) {
        return ObjectConverters.toInt(this.runGetter(method, this.getItem(this.getPosition())));
    }

    public int getInt(String fieldName) {
        return this.getInt(this.getGetterForFieldOrThrow(this.applyAlias(fieldName)));
    }

    public T getItem(int position) {
        return this.mObjectList.get(position);
    }

    public long getLong(int column) {
        return this.getLong((Method)this.mMethodList.get(column));
    }

    private long getLong(Method method) {
        return ObjectConverters.toLong(this.runGetter(method, this.getItem(this.getPosition())));
    }

    public long getLong(String fieldName) {
        return this.getLong(this.getGetterForFieldOrThrow(this.applyAlias(fieldName)));
    }

    public List<Method> getMethods() {
        return this.mMethodList;
    }

    public Object getObject(int column) {
        return this.getObject((Method)this.mMethodList.get(column));
    }

    private Object getObject(Method method) {
        return this.runGetter(method, this.getItem(this.getPosition()));
    }

    public Object getObject(String fieldName) {
        return this.getObject(this.getGetterForFieldOrThrow(this.applyAlias(fieldName)));
    }

    public EasyQueryModel getQueryModel() {
        return this.mQueryModel;
    }

    public short getShort(int column) {
        return this.getShort((Method)this.mMethodList.get(column));
    }

    public short getShort(Method method) {
        return ObjectConverters.toShort(this.runGetter(method, this.getItem(this.getPosition())));
    }

    public short getShort(String fieldName) {
        return this.getShort(this.getGetterForFieldOrThrow(this.applyAlias(fieldName)));
    }

    public String getString(int column) {
        return this.getString((Method)this.mMethodList.get(column));
    }

    private String getString(Method method) {
        return ObjectConverters.toString(this.runGetter(method, this.getItem(this.getPosition())));
    }

    public String getString(String fieldName) {
        return this.getString(this.getGetterForFieldOrThrow(this.applyAlias(fieldName)));
    }

    public boolean isDebugEnabled() {
        return this.mDebugEnabled;
    }

    public boolean isNull(int column) {
        return this.isNull((Method)this.mMethodList.get(column));
    }

    private boolean isNull(Method method) {
        return this.runGetter(method, this.getItem(this.getPosition())) == null;
    }

    public boolean isNull(String fieldName) {
        return this.isNull(this.getGetterForFieldOrThrow(this.applyAlias(fieldName)));
    }

    public boolean optBoolean(String fieldName) {
        return this.optBoolean(fieldName, false);
    }

    public boolean optBoolean(String fieldName, boolean fallback) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            return fallback;
        } else {
            try {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optBoolean('" + fieldName + "') Getter was null.");
                }

                return ObjectConverters.toBoolean(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var5) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optBoolean('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var5);
                    var5.printStackTrace();
                }

                return fallback;
            }
        }
    }

    public Boolean optBooleanAsWrapperType(String fieldName) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optBooleanAsWrapperType('" + fieldName + "') Getter was null.");
            }

            return null;
        } else {
            try {
                return ObjectConverters.toBoolean(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var4) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optBooleanAsWrapperType('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var4);
                    var4.printStackTrace();
                }

                return null;
            }
        }
    }

    public double optDouble(String fieldName) {
        return this.optDouble(fieldName, 0.0D);
    }

    public double optDouble(String fieldName, double fallback) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optDouble('" + fieldName + "') Getter was null.");
            }

            return fallback;
        } else {
            try {
                return ObjectConverters.toDouble(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var6) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optDouble('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var6);
                    var6.printStackTrace();
                }

                return fallback;
            }
        }
    }

    public Double optDoubleAsWrapperType(String fieldName) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optDoubleAsWrapperType('" + fieldName + "') Getter was null.");
            }

            return null;
        } else {
            try {
                return ObjectConverters.toDouble(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var4) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optDoubleAsWrapperType('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var4);
                    var4.printStackTrace();
                }

                return null;
            }
        }
    }

    public float optFloat(String fieldName) {
        return this.optFloat(fieldName, 0.0F);
    }

    public float optFloat(String fieldName, float fallback) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optFloat('" + fieldName + "') Getter was null.");
            }

            return fallback;
        } else {
            try {
                return ObjectConverters.toFloat(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var5) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optFloat('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var5);
                    var5.printStackTrace();
                }

                return fallback;
            }
        }
    }

    public Float optFloatAsWrapperType(String fieldName) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optFloatAsWrapperType('" + fieldName + "') Getter was null.");
            }

            return null;
        } else {
            try {
                return ObjectConverters.toFloat(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var4) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optFloatAsWrapperType('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var4);
                    var4.printStackTrace();
                }

                return null;
            }
        }
    }

    public int optInt(String fieldName) {
        return this.optInt(fieldName, 0);
    }

    public int optInt(String fieldName, int fallback) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optInt('" + fieldName + "') Getter was null.");
            }

            return fallback;
        } else {
            try {
                return ObjectConverters.toInt(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var5) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optInt('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var5);
                    var5.printStackTrace();
                }

                return fallback;
            }
        }
    }

    public Integer optIntAsWrapperType(String fieldName) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optIntAsWrapperType('" + fieldName + "') Getter was null.");
            }

            return null;
        } else {
            try {
                return ObjectConverters.toInt(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var4) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optIntAsWrapperType('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var4);
                    var4.printStackTrace();
                }

                return null;
            }
        }
    }

    public long optLong(String fieldName) {
        return this.optLong(fieldName, 0L);
    }

    public long optLong(String fieldName, long fallback) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optLong('" + fieldName + "') Getter was null.");
            }

            return fallback;
        } else {
            try {
                return ObjectConverters.toLong(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var6) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optLong('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var6);
                    var6.printStackTrace();
                }

                return fallback;
            }
        }
    }

    public Long optLongAsWrapperType(String fieldName) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optLongAsWrapperType('" + fieldName + "') Getter was null.");
            }

            return null;
        } else {
            try {
                return ObjectConverters.toLong(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var4) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optLongAsWrapperType('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var4);
                    var4.printStackTrace();
                }

                return null;
            }
        }
    }

    public String optString(String fieldName) {
        return this.optString(fieldName, DEFAULT_STRING);
    }

    public String optString(String fieldName, String fallback) {
        Method method = this.getGetterForField(this.applyAlias(fieldName));
        if (method == null) {
            if (this.mDebugEnabled) {
                Log.w(this.TAG, "optString('" + fieldName + "') Getter was null.");
            }

            return fallback;
        } else {
            try {
                return ObjectConverters.toString(this.runGetter(method, this.getItem(this.getPosition())));
            } catch (Exception var5) {
                if (this.mDebugEnabled) {
                    Log.w(this.TAG, "optString('" + fieldName + "') Caught Exception  at " + this.getPosition() + "/" + this.getCount(), var5);
                    var5.printStackTrace();
                }

                return fallback;
            }
        }
    }

    private void populateMethodList(Class<T> clazz) {
        int count = 0;
        Method[] var8;
        int var7 = (var8 = clazz.getMethods()).length;

        for(int var6 = 0; var6 < var7; ++var6) {
            Method method = var8[var6];
            Method candidate = null;
            String canditateCleanName = null;
            if (Modifier.isPublic(method.getModifiers()) && method.getName().length() > 3 && method.getParameterTypes().length == 0 && !method.getReturnType().equals(Void.TYPE)) {
                if (method.getName().startsWith("get")) {
                    candidate = method;
                    canditateCleanName = method.getName().substring("get".length()).toLowerCase(Locale.US);
                } else if (method.getName().startsWith("is")) {
                    candidate = method;
                    canditateCleanName = method.getName().substring("is".length()).toLowerCase(Locale.US);
                }

                if (candidate != null) {
                    this.mMethodList.add(candidate);
                    this.mFieldToIndexMap.put(canditateCleanName, count);
                    this.mFieldNameList.add(canditateCleanName);
                    ++count;
                }
            }
        }

    }

    private Object runGetter(Method method, T object) {
        if (method != null) {
            try {
                return method.invoke(object);
            } catch (IllegalAccessException var4) {
                Log.w(this.TAG, "Could not determine method: " + method.getName());
            } catch (InvocationTargetException var5) {
                Log.w(this.TAG, "Could not determine method: " + method.getName());
            }
        }

        return null;
    }

    public void setDebugEnabled(boolean enabled) {
        this.mDebugEnabled = enabled;
    }
}
