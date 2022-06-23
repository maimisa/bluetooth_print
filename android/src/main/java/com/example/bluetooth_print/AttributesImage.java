package com.example.bluetooth_print;

import android.os.*;

public class AttributesImage implements Parcelable
{
    public static final Parcelable.Creator<AttributesImage> CREATOR;
    private String alignment;
    private boolean doScale;
    int graphicFilter;
    boolean inverseColor;
    boolean rotateImage;
    private int scale;
    
    static {
        CREATOR = (Parcelable.Creator)new Parcelable.Creator<AttributesImage>() {
            public AttributesImage createFromParcel(final Parcel parcel) {
                return new AttributesImage(parcel);
            }
            
            public AttributesImage[] newArray(final int n) {
                return new AttributesImage[n];
            }
        };
    }
    
    public AttributesImage() {
        this.graphicFilter = 0;
        this.rotateImage = false;
        this.inverseColor = false;
        this.scale = 16;
        this.doScale = true;
        this.alignment = "left";
    }
    
    public AttributesImage(final int graphicFilter) {
        this.graphicFilter = 0;
        this.rotateImage = false;
        this.inverseColor = false;
        this.scale = 16;
        this.doScale = true;
        this.alignment = "left";
        this.graphicFilter = graphicFilter;
    }
    
    protected AttributesImage(final Parcel parcel) {
        final boolean b = false;
        this.graphicFilter = 0;
        this.rotateImage = false;
        this.inverseColor = false;
        this.scale = 16;
        this.doScale = true;
        this.alignment = "left";
        this.graphicFilter = parcel.readInt();
        this.rotateImage = (parcel.readByte() != 0);
        this.inverseColor = (parcel.readByte() != 0);
        this.scale = parcel.readInt();
        this.alignment = parcel.readString();
        boolean doScale = b;
        if (parcel.readByte() != 0) {
            doScale = true;
        }
        this.doScale = doScale;
    }
    
    public int describeContents() {
        return 0;
    }
    
    public String getAlignment() {
        return this.alignment;
    }
    
    public int getGraphicFilter() {
        return this.graphicFilter;
    }
    
    public int getScale() {
        return this.scale;
    }
    
    public boolean isDoScale() {
        return this.doScale;
    }
    
    public boolean isInverseColor() {
        return this.inverseColor;
    }
    
    public boolean isRotateImage() {
        return this.rotateImage;
    }
    
    public AttributesImage setAlignment(final String alignment) {
        this.alignment = alignment;
        return this;
    }
    
    public AttributesImage setDoScale(final boolean doScale) {
        this.doScale = doScale;
        return this;
    }
    
    public AttributesImage setGraphicFilter(final int graphicFilter) {
        this.graphicFilter = graphicFilter;
        return this;
    }
    
    public AttributesImage setInverseColor(final boolean inverseColor) {
        this.inverseColor = inverseColor;
        return this;
    }
    
    public AttributesImage setRotateImage(final boolean rotateImage) {
        this.rotateImage = rotateImage;
        return this;
    }
    
    public AttributesImage setScale(final int scale) {
        this.scale = scale;
        return this;
    }
    
    public void writeToParcel(final Parcel parcel, final int n) {
        parcel.writeInt(this.graphicFilter);
        parcel.writeByte((byte)(byte)(this.rotateImage ? 1 : 0));
        parcel.writeByte((byte)(byte)(this.inverseColor ? 1 : 0));
        parcel.writeInt(this.scale);
        parcel.writeString(this.alignment);
        parcel.writeByte((byte)(byte)(this.doScale ? 1 : 0));
    }
}
