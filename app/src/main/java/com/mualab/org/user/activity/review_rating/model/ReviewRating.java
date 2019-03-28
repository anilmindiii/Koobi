package com.mualab.org.user.activity.review_rating.model;

import java.util.List;

/**
 * Created by mindiii on 7/2/19.
 */

public final class ReviewRating {

    /**
     * status : success
     * message : ok
     * data : [{"_id":84,"userDetail":[{"_id":3,"userName":"anil","profileImage":"http://koobi.co.uk:3000/uploads/profile/1549265675351.jpg"}],"review":"thanks for the update","rating":4,"crd":"2019-02-06"}]
     */

    private String status;
    private String message;
    private List<DataBean> data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * _id : 84
         * userDetail : [{"_id":3,"userName":"anil","profileImage":"http://koobi.co.uk:3000/uploads/profile/1549265675351.jpg"}]
         * review : thanks for the update
         * rating : 4
         * crd : 2019-02-06
         */

        private int _id;
        private String review;
        private float rating;
        private String crd;
        private List<UserDetailBean> userDetail;

        public int get_id() {
            return _id;
        }

        public void set_id(int _id) {
            this._id = _id;
        }

        public String getReview() {
            return review;
        }

        public void setReview(String review) {
            this.review = review;
        }

        public float getRating() {
            return rating;
        }

        public void setRating(float rating) {
            this.rating = rating;
        }

        public String getCrd() {
            return crd;
        }

        public void setCrd(String crd) {
            this.crd = crd;
        }

        public List<UserDetailBean> getUserDetail() {
            return userDetail;
        }

        public void setUserDetail(List<UserDetailBean> userDetail) {
            this.userDetail = userDetail;
        }

        public static class UserDetailBean {
            /**
             * _id : 3
             * userName : anil
             * profileImage : http://koobi.co.uk:3000/uploads/profile/1549265675351.jpg
             */

            private int _id;
            private String userName;
            private String profileImage;

            public int get_id() {
                return _id;
            }

            public void set_id(int _id) {
                this._id = _id;
            }

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }

            public String getProfileImage() {
                return profileImage;
            }

            public void setProfileImage(String profileImage) {
                this.profileImage = profileImage;
            }
        }
    }
}
