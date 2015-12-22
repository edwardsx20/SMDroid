package cl.edicsm.control;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by eaguad on 12/21/2015.
 */
public class ViewDetail implements Parcelable {
    private String value;
    private String cantidad;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public ViewDetail(String value, String cantidad) {
        this.value = value;
        this.cantidad = cantidad;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.value);
        dest.writeValue(this.cantidad);
    }

    protected ViewDetail(Parcel in) {
        this.value = in.readString();
        this.cantidad = in.readString();
    }

    public static final Parcelable.Creator<ViewDetail> CREATOR = new Parcelable.Creator<ViewDetail>() {
        public ViewDetail createFromParcel(Parcel source) {
            return new ViewDetail(source);
        }

        public ViewDetail[] newArray(int size) {
            return new ViewDetail[size];
        }
    };
}
