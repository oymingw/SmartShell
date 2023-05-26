package EasyCursor;

import org.json.JSONException;

public interface EasyQueryModel {
    String getModelComment();

    String getModelTag();

    int getModelVersion();

    void setModelComment(String var1);

    void setModelTag(String var1);

    void setModelVersion(int var1);

    String toJson() throws JSONException;
}
