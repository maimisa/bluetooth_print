package com.example.bluetooth_print;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

public class FilterManager {
    public Bitmap printImage(Bitmap bitmap, final AttributesImage attributesImage) {
        final int dots_per_line = 576;
        final int n = dots_per_line * attributesImage.getScale() / 16;

        if (bitmap != null) {
            final Bitmap.Config config = bitmap.getConfig();
            try {
                if (attributesImage.isRotateImage()) {
                    bitmap = GraphLibrary.rotateBitmap(bitmap);
                }
                boolean doScale = attributesImage.isDoScale();
                if (!attributesImage.isDoScale()) {
                    doScale = doScale;
                    if (bitmap.getWidth() > n) {
                        doScale = true;
                    }
                }
                Bitmap resizeImage = bitmap;
                if (doScale) {
                    resizeImage = GraphLibrary.resizeImage(bitmap, n, bitmap.getHeight() * n / bitmap.getWidth());
                }
                final int height = resizeImage.getHeight();
                Label_0270: {
                    if (attributesImage.getScale() >= 16) {
                        bitmap = resizeImage;
                        if (attributesImage.isDoScale()) {
                            break Label_0270;
                        }
                    }
                    final int width = resizeImage.getWidth();
                    final int[] array = new int[width * height];
                    resizeImage.getPixels(array, 0, resizeImage.getWidth(), 0, 0, resizeImage.getWidth(), resizeImage.getHeight());
                    bitmap = Bitmap.createBitmap(dots_per_line, height, config);
                    int n2;
                    if (attributesImage.isInverseColor()) {
                        n2 = -16777216;
                    }
                    else {
                        n2 = -1;
                    }
                    bitmap.eraseColor(n2);
                    int n3;
                    if ("right".equals(attributesImage.getAlignment())) {
                        n3 = dots_per_line - width;
                    }
                    else if ("center".equals(attributesImage.getAlignment())) {
                        n3 = (dots_per_line - width) / 2;
                    }
                    else {
                        n3 = 0;
                    }
                    bitmap.setPixels(array, 0, width, n3, 0, width, height);
                }
                final int[] array2 = new int[(height + 1) * dots_per_line + 1];
                Bitmap grayscale = null;
                Label_0308: {
                    if (attributesImage.getGraphicFilter() != 0) {
                        grayscale = bitmap;
                        if (attributesImage.getGraphicFilter() != 8) {
                            break Label_0308;
                        }
                    }
                    grayscale = GraphLibrary.toGrayscale(bitmap);
                }
                grayscale.getPixels(array2, 0, grayscale.getWidth(), 0, 0, grayscale.getWidth(), grayscale.getHeight());
                final byte[] dithering = GraphLibrary.dithering(array2, height, dots_per_line, attributesImage.getGraphicFilter());
                //grayscale.recycle();
                if (attributesImage.isInverseColor()) {
                    for (int i = 0; i < dithering.length; ++i) {
                        if (dithering[i] == 1) {
                            dithering[i] = 0;
                        }
                        else {
                            dithering[i] = 1;
                        }
                    }
                }

                //for (int i = 0; i < dithering.length; ++i) {
                //    if (dithering[i] != 1 && dithering[i] != 0) {
                //        Log.e("iii", "1");
                //        break;
                //    }
                //}

                //bitmap.setPixels(dithering, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                //bitmap = BitmapFactory.decodeByteArray(dithering, 0, dithering.length);

                //bitmap.setPixel(0,0, Color.rgb(0,0,0));

                //this.graphics(dithering, dots_per_line, height);
                for (int row=0; row < height; row++) {
                    for (int col=0; col < dots_per_line; col++) {
                        int color = dithering[dots_per_line*row + col] == 1 ? Color.rgb(0,0,0) : Color.rgb(255,255,255);
                        bitmap.setPixel(col,row, color);
                    }
                }
                return bitmap;
            }
            catch (Exception ex) {
                ex.printStackTrace();
                //final TaskForTransport jobTask = this.jobTask;
                //if (jobTask != null) {
                //    final StringBuilder sb = new StringBuilder();
                //    sb.append("error : ");
                //    sb.append(ex.getLocalizedMessage());
                //    jobTask.deviceProgress(sb.toString());
                //}
                return null;
            }
            catch (OutOfMemoryError outOfMemoryError) {
                //final TaskForTransport jobTask2 = this.jobTask;
                //if (jobTask2 != null) {
                //    final StringBuilder sb2 = new StringBuilder();
                //    sb2.append("error : ");
                //    sb2.append(outOfMemoryError.getLocalizedMessage());
                //    jobTask2.deviceError(sb2.toString());
                //}
                outOfMemoryError.printStackTrace();
                return null;
            }
        }

        return null;
    }

    public void graphics(final byte[] array, final int n, final int n2) {
        //this.drv.bytes(new byte[] { 27, 51, 16 });
        try {
            final byte[] array2 = new byte[n * 24];
            for (int i = 0; i < n2; i += 24) {
                for (int j = 0; j < 24; ++j) {
                    for (int k = 0; k < n; ++k) {
                        if (i + j < n2) {
                            final int n3 = j * n;
                            array2[n3 + k] = array[i * n + n3 + k];
                        }
                        else {
                            array2[j * n + k] = 0;
                        }
                    }
                }
                byte[] graphFormat = GraphLibrary.graphicsFormat(array2, n, 24, 3);
                //this.sendRow(GraphLibrary.graphicsFormat(array2, n, 24, 3), n);
                Log.e("iii", "graphFormat");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        //this.drv.bytes(new byte[] { 27, 50 });
    }
}
