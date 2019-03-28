package com.mualab.org.user.activity.payment.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class StripeSaveCardResponce {

    /**
     * count : null
     * data : [{"address_city":null,"address_country":null,"address_line1":null,"address_line1_check":null,"address_line2":null,"address_state":null,"address_zip":null,"address_zip_check":null,"available_payout_methods":null,"brand":"Visa","country":"US","currency":null,"cvc_check":"pass","default_for_currency":null,"deleted":null,"description":null,"dynamic_last4":null,"exp_month":10,"exp_year":2023,"fingerprint":"xQUClG00Wnpubm6b","funding":"credit","iin":null,"issuer":null,"last4":"4242","name":null,"recipient":null,"status":null,"three_d_secure":null,"tokenization_method":null,"type":null,"account":null,"customer":"cus_E2nQFXkVY4FrpL","id":"card_1DaixWAx9wBOzcgNgKKABdUR","metadata":{},"object":"card"},{"address_city":null,"address_country":null,"address_line1":null,"address_line1_check":null,"address_line2":null,"address_state":null,"address_zip":null,"address_zip_check":null,"available_payout_methods":null,"brand":"Visa","country":"US","currency":null,"cvc_check":"pass","default_for_currency":null,"deleted":null,"description":null,"dynamic_last4":null,"exp_month":10,"exp_year":2026,"fingerprint":"xQUClG00Wnpubm6b","funding":"credit","iin":null,"issuer":null,"last4":"4242","name":null,"recipient":null,"status":null,"three_d_secure":null,"tokenization_method":null,"type":null,"account":null,"customer":"cus_E2nQFXkVY4FrpL","id":"card_1DajcuAx9wBOzcgNIfq6g90l","metadata":{},"object":"card"},{"address_city":null,"address_country":null,"address_line1":null,"address_line1_check":null,"address_line2":null,"address_state":null,"address_zip":null,"address_zip_check":null,"available_payout_methods":null,"brand":"Visa","country":"US","currency":null,"cvc_check":"pass","default_for_currency":null,"deleted":null,"description":null,"dynamic_last4":null,"exp_month":10,"exp_year":2025,"fingerprint":"xQUClG00Wnpubm6b","funding":"credit","iin":null,"issuer":null,"last4":"4242","name":null,"recipient":null,"status":null,"three_d_secure":null,"tokenization_method":null,"type":null,"account":null,"customer":"cus_E2nQFXkVY4FrpL","id":"card_1DajVOAx9wBOzcgNSuFmmbyb","metadata":{},"object":"card"}]
     * has_more : true
     * object : list
     * request_options : null
     * request_params : {"object":"card","limit":3}
     * total_count : null
     * url : /v1/customers/cus_E2nQFXkVY4FrpL/sources
     */

    private Object count;
    private boolean has_more;
    private String object;
    private Object request_options;
    private RequestParamsBean request_params;
    private Object total_count;
    private String url;
    private List<DataBean> data;

    public Object getCount() {
        return count;
    }

    public void setCount(Object count) {
        this.count = count;
    }

    public boolean isHas_more() {
        return has_more;
    }

    public void setHas_more(boolean has_more) {
        this.has_more = has_more;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Object getRequest_options() {
        return request_options;
    }

    public void setRequest_options(Object request_options) {
        this.request_options = request_options;
    }

    public RequestParamsBean getRequest_params() {
        return request_params;
    }

    public void setRequest_params(RequestParamsBean request_params) {
        this.request_params = request_params;
    }

    public Object getTotal_count() {
        return total_count;
    }

    public void setTotal_count(Object total_count) {
        this.total_count = total_count;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class RequestParamsBean {
        /**
         * object : card
         * limit : 3
         */

        private String object;
        private int limit;

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }
    }

    public static class DataBean implements Parcelable {
        private Object address_city;
        private Object address_country;
        private Object address_line1;
        private Object address_line1_check;
        private Object address_line2;
        private Object address_state;
        private Object address_zip;
        private Object address_zip_check;
        private Object available_payout_methods;
        private String brand;
        private String country;
        private Object currency;
        private String cvc_check;
        private Object default_for_currency;
        private Object deleted;
        private Object description;
        private Object dynamic_last4;
        private int exp_month;
        private int exp_year;
        private String fingerprint;
        private String funding;
        private Object iin;
        private Object issuer;
        private String last4;
        private Object name;
        private Object recipient;
        private Object status;
        private Object three_d_secure;
        private Object tokenization_method;
        private Object type;
        private Object account;
        private String customer;
        private String id;
        public boolean isSelected;
        private MetadataBean metadata;
        private String object;


        protected DataBean(Parcel in) {
            brand = in.readString();
            country = in.readString();
            cvc_check = in.readString();
            exp_month = in.readInt();
            exp_year = in.readInt();
            fingerprint = in.readString();
            funding = in.readString();
            last4 = in.readString();
            customer = in.readString();
            id = in.readString();
            isSelected = in.readByte() != 0;
            object = in.readString();
        }

        public static final Creator<DataBean> CREATOR = new Creator<DataBean>() {
            @Override
            public DataBean createFromParcel(Parcel in) {
                return new DataBean(in);
            }

            @Override
            public DataBean[] newArray(int size) {
                return new DataBean[size];
            }
        };

        public Object getAddress_city() {
            return address_city;
        }

        public void setAddress_city(Object address_city) {
            this.address_city = address_city;
        }

        public Object getAddress_country() {
            return address_country;
        }

        public void setAddress_country(Object address_country) {
            this.address_country = address_country;
        }

        public Object getAddress_line1() {
            return address_line1;
        }

        public void setAddress_line1(Object address_line1) {
            this.address_line1 = address_line1;
        }

        public Object getAddress_line1_check() {
            return address_line1_check;
        }

        public void setAddress_line1_check(Object address_line1_check) {
            this.address_line1_check = address_line1_check;
        }

        public Object getAddress_line2() {
            return address_line2;
        }

        public void setAddress_line2(Object address_line2) {
            this.address_line2 = address_line2;
        }

        public Object getAddress_state() {
            return address_state;
        }

        public void setAddress_state(Object address_state) {
            this.address_state = address_state;
        }

        public Object getAddress_zip() {
            return address_zip;
        }

        public void setAddress_zip(Object address_zip) {
            this.address_zip = address_zip;
        }

        public Object getAddress_zip_check() {
            return address_zip_check;
        }

        public void setAddress_zip_check(Object address_zip_check) {
            this.address_zip_check = address_zip_check;
        }

        public Object getAvailable_payout_methods() {
            return available_payout_methods;
        }

        public void setAvailable_payout_methods(Object available_payout_methods) {
            this.available_payout_methods = available_payout_methods;
        }

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public Object getCurrency() {
            return currency;
        }

        public void setCurrency(Object currency) {
            this.currency = currency;
        }

        public String getCvc_check() {
            return cvc_check;
        }

        public void setCvc_check(String cvc_check) {
            this.cvc_check = cvc_check;
        }

        public Object getDefault_for_currency() {
            return default_for_currency;
        }

        public void setDefault_for_currency(Object default_for_currency) {
            this.default_for_currency = default_for_currency;
        }

        public Object getDeleted() {
            return deleted;
        }

        public void setDeleted(Object deleted) {
            this.deleted = deleted;
        }

        public Object getDescription() {
            return description;
        }

        public void setDescription(Object description) {
            this.description = description;
        }

        public Object getDynamic_last4() {
            return dynamic_last4;
        }

        public void setDynamic_last4(Object dynamic_last4) {
            this.dynamic_last4 = dynamic_last4;
        }

        public int getExp_month() {
            return exp_month;
        }

        public void setExp_month(int exp_month) {
            this.exp_month = exp_month;
        }

        public int getExp_year() {
            return exp_year;
        }

        public void setExp_year(int exp_year) {
            this.exp_year = exp_year;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }

        public String getFunding() {
            return funding;
        }

        public void setFunding(String funding) {
            this.funding = funding;
        }

        public Object getIin() {
            return iin;
        }

        public void setIin(Object iin) {
            this.iin = iin;
        }

        public Object getIssuer() {
            return issuer;
        }

        public void setIssuer(Object issuer) {
            this.issuer = issuer;
        }

        public String getLast4() {
            return last4;
        }

        public void setLast4(String last4) {
            this.last4 = last4;
        }

        public Object getName() {
            return name;
        }

        public void setName(Object name) {
            this.name = name;
        }

        public Object getRecipient() {
            return recipient;
        }

        public void setRecipient(Object recipient) {
            this.recipient = recipient;
        }

        public Object getStatus() {
            return status;
        }

        public void setStatus(Object status) {
            this.status = status;
        }

        public Object getThree_d_secure() {
            return three_d_secure;
        }

        public void setThree_d_secure(Object three_d_secure) {
            this.three_d_secure = three_d_secure;
        }

        public Object getTokenization_method() {
            return tokenization_method;
        }

        public void setTokenization_method(Object tokenization_method) {
            this.tokenization_method = tokenization_method;
        }

        public Object getType() {
            return type;
        }

        public void setType(Object type) {
            this.type = type;
        }

        public Object getAccount() {
            return account;
        }

        public void setAccount(Object account) {
            this.account = account;
        }

        public String getCustomer() {
            return customer;
        }

        public void setCustomer(String customer) {
            this.customer = customer;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public MetadataBean getMetadata() {
            return metadata;
        }

        public void setMetadata(MetadataBean metadata) {
            this.metadata = metadata;
        }

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(brand);
            dest.writeString(country);
            dest.writeString(cvc_check);
            dest.writeInt(exp_month);
            dest.writeInt(exp_year);
            dest.writeString(fingerprint);
            dest.writeString(funding);
            dest.writeString(last4);
            dest.writeString(customer);
            dest.writeString(id);
            dest.writeByte((byte) (isSelected ? 1 : 0));
            dest.writeString(object);
        }


        public static class MetadataBean {
        }
    }}