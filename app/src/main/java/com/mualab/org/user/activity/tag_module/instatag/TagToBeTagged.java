/*
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mualab.org.user.activity.tag_module.instatag;

import java.io.Serializable;

public class TagToBeTagged implements Serializable {
    private String unique_tag_id, tagId, tabType, title;
    //private HashMap<String, TagDetail> tagDetails;
    TagDetail tagDetails;

    //private TagDetail mTag;

    // private Double x_co_ord;
    // private Double y_co_ord;
    private float x_axis;
    private float y_axis;

    public TagToBeTagged() {

    }

    /*public TagToBeTagged(String unique_tag_id, float x_co_ord, float y_co_ord, HashMap<String, TagDetail> tagDetails) {
        this.unique_tag_id = unique_tag_id;
        this.x_axis = x_co_ord;
        this.y_axis = y_co_ord;
        this.tagDetails = tagDetails;
    }*/

    public TagToBeTagged(String unique_tag_id, float x_co_ord, float y_co_ord, TagDetail tagDetails) {
        this.unique_tag_id = unique_tag_id;
        this.x_axis = x_co_ord;
        this.y_axis = y_co_ord;
        this.tagDetails = tagDetails;
    }

    /*public HashMap<String, TagDetail> getTagDetails() {
        return tagDetails;
    }

    public void setTagDetails(HashMap<String, TagDetail> tagDetails) {
        this.tagDetails = tagDetails;
    }*/

    public TagDetail getTagDetails() {
        return tagDetails;
    }

    public void setTagDetails(TagDetail tagDetails) {
        this.tagDetails = tagDetails;
    }

    public String getTabType() {
        return tabType;
    }

    public void setTabType(String tabType) {
        this.tabType = tabType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getUnique_tag_id() {
        return unique_tag_id;
    }


    public void setUnique_tag_id(String unique_tag_id) {
        this.unique_tag_id = unique_tag_id;
    }

    public float getX_co_ord() {
        return x_axis;
    }

    public void setX_co_ord(float x_co_ord) {
        this.x_axis = x_co_ord;
    }

    public float getY_co_ord() {
        return y_axis;
    }

    public void setY_co_ord(float y_co_ord) {
        this.y_axis = y_co_ord;
    }

/*
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.unique_tag_id);
        dest.writeString(this.tagId);
        dest.writeString(this.title);
        dest.writeString(this.tabType);
        dest.writeValue(this.x_axis);
        dest.writeValue(this.y_axis);
        dest.writeValue(this.tagDetails);
    }

    private TagToBeTagged(Parcel in) {
        this.unique_tag_id = in.readString();
        this.tagId = in.readString();
        this.title = in.readString();
        this.tabType = in.readString();
        this.tagDetails  = in.readHashMap(String.class.getClassLoader());
        this.x_axis = (Double) in.readValue(Double.class.getClassLoader());
        this.y_axis = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Creator<TagToBeTagged> CREATOR =
            new Creator<TagToBeTagged>() {
                @Override
                public TagToBeTagged createFromParcel(Parcel source) {
                    return new TagToBeTagged(source);
                }

                @Override
                public TagToBeTagged[] newArray(int size) {
                    return new TagToBeTagged[size];
                }
            };*/
}
