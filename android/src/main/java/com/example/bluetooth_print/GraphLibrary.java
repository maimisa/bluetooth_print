package com.example.bluetooth_print;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.nio.Buffer;
import java.nio.IntBuffer;

public class GraphLibrary {
    private static final int[][] m_16;
    private static final int[] p0;
    private static final int[] p1;
    private static final int[] p2;
    private static final int[] p3;
    private static final int[] p4;
    private static final int[] p5;
    private static final int[] p6;

    static {
        p0 = new int[] { 0, 128 };
        p1 = new int[] { 0, 64 };
        p2 = new int[] { 0, 32 };
        p3 = new int[] { 0, 16 };
        p4 = new int[] { 0, 8 };
        p5 = new int[] { 0, 4 };
        p6 = new int[] { 0, 2 };
        m_16 = new int[][] { { 0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42, 170 }, { 192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 234, 106 }, { 48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 26, 154 }, { 240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 122, 218, 90 }, { 12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 166 }, { 204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 230, 102 }, { 60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 22, 150 }, { 252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246, 118, 214, 86 }, { 3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 169 }, { 195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 233, 105 }, { 51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 25, 153 }, { 243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249, 121, 217, 89 }, { 15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 165 }, { 207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 229, 101 }, { 63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 21, 149 }, { 254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245, 117, 213, 85 } };
    }

    private static byte[] bayerMatrix(final int[] array, final int n, final int n2) {
        toGray(array, n, n2);
        final byte[] array2 = new byte[n * n2];
        int i = 0;
        int n3 = 0;
        while (i < n) {
            for (int j = 0; j < n2; ++j) {
                if ((array[n3] & 0xFF) > GraphLibrary.m_16[j & 0xF][i & 0xF]) {
                    array2[n3] = 0;
                }
                else {
                    array2[n3] = 1;
                }
                ++n3;
            }
            ++i;
        }
        return array2;
    }

    private static Bitmap colorDodgeBlend(Bitmap copy, final Bitmap bitmap) {
        copy = copy.copy(Bitmap.Config.ARGB_8888, true);
        final Bitmap copy2 = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        final IntBuffer allocate = IntBuffer.allocate(copy.getWidth() * copy.getHeight());
        copy.copyPixelsToBuffer((Buffer)allocate);
        allocate.rewind();
        final IntBuffer allocate2 = IntBuffer.allocate(copy2.getWidth() * copy2.getHeight());
        copy2.copyPixelsToBuffer((Buffer)allocate2);
        allocate2.rewind();
        final IntBuffer allocate3 = IntBuffer.allocate(copy.getWidth() * copy.getHeight());
        allocate3.rewind();
        while (allocate3.position() < allocate3.limit()) {
            final int value = allocate2.get();
            final int value2 = allocate.get();
            allocate3.put(Color.argb(255, colordodge(Color.red(value), Color.red(value2)), colordodge(Color.green(value), Color.green(value2)), colordodge(Color.blue(value), Color.blue(value2))));
        }
        allocate3.rewind();
        copy.copyPixelsFromBuffer((Buffer)allocate3);
        copy2.recycle();
        return copy;
    }

    private static int colordodge(final int n, final int n2) {
        float min = (float)n2;
        final float n3 = (float)n;
        if (min != 255.0f) {
            min = Math.min(255.0f, ((long)n3 << 8) / (255.0f - min));
        }
        return (int)min;
    }

    public static Bitmap resizeImage(final Bitmap bitmap, final int n, final int n2) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final float n3 = n / (float)width;
        final float n4 = n2 / (float)height;
        final Matrix matrix = new Matrix();
        matrix.postScale(n3, n4);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static Bitmap rotateBitmap(final Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap.Config config = bitmap.getConfig();
        final int n = width * height;
        final int[] array = new int[n];
        bitmap.getPixels(array, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        bitmap.recycle();
        final int[] array2 = new int[n];
        int i = 0;
        int n2 = 0;
        while (i < height) {
            int n3 = height - i - 1;
            for (int j = 0; j < width; ++j) {
                array2[n3] = array[n2];
                n3 += height;
                ++n2;
            }
            ++i;
        }
        final Bitmap bitmap2 = Bitmap.createBitmap(height, width, config);
        bitmap2.setPixels(array2, 0, height, 0, 0, height, width);
        return bitmap2;
    }

    private static int[] sketchImage(final int[] array, final int n, final int n2, final Context context) {
        final Bitmap bitmap = Bitmap.createBitmap(n2, n, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(array, 0, n2, 0, 0, n2, n);
        final Bitmap grayscale = toGrayscale(bitmap, 1);
        final Bitmap colorDodgeBlend = colorDodgeBlend(toBlur(toInverted(grayscale, 100), context), grayscale);
        colorDodgeBlend.getPixels(array, 0, colorDodgeBlend.getWidth(), 0, 0, colorDodgeBlend.getWidth(), colorDodgeBlend.getHeight());
        toGray(array, n, n2);
        long n3 = 0L;
        int i = 0;
        int n4 = 0;
        while (i < n) {
            for (int j = 0; j < n2; ++j) {
                n3 += (array[n4] & 0xFF);
                ++n4;
            }
            ++i;
        }
        final int n5 = (int)(n3 / n / n2);
        int k = 0;
        int n6 = 0;
        while (k < n) {
            for (int l = 0; l < n2; ++l) {
                if (array[n6] < n5) {
                    array[n6] = 0;
                }
                else {
                    array[n6] = 255;
                }
                ++n6;
            }
            ++k;
        }
        return array;
    }

    private static Bitmap toBlur(final Bitmap bitmap, final Context context) {
        try {
            final RenderScript create = RenderScript.create(context);
            final Allocation fromBitmap = Allocation.createFromBitmap(create, bitmap);
            final ScriptIntrinsicBlur create2 = ScriptIntrinsicBlur.create(create, Element.U8_4(create));
            create2.setRadius(2.5f);
            create2.setInput(fromBitmap);
            final Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            final Allocation fromBitmap2 = Allocation.createFromBitmap(create, bitmap2);
            create2.forEach(fromBitmap2);
            fromBitmap2.copyTo(bitmap2);
            create.destroy();
            fromBitmap2.destroy();
            fromBitmap.destroy();
            create2.destroy();
            return bitmap2;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return bitmap;
        }
    }

    public static Bitmap toLight(final Bitmap bitmap) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        final Bitmap bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final int n = width * height;
        final int[] array = new int[n];
        bitmap.getPixels(array, 0, width, 0, 0, width, height);
        for (int i = 0; i < n; ++i) {
            final int n2 = array[i];
            int n3 = 255;
            final int n4 = n2 >> 16 & 0xFF;
            int n5 = n2 >> 8 & 0xFF;
            int n6 = n2 & 0xFF;
            if (n4 + n5 + n6 > 720) {
                n6 = 255;
                n5 = 255;
            }
            else {
                n3 = n4;
            }
            array[i] = (n6 | (0xFF000000 | n3 << 16 | n5 << 8));
        }
        bitmap2.setPixels(array, 0, width, 0, 0, width, height);
        return bitmap2;
    }

    public static Bitmap toDark(final Bitmap bitmap, int n) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        final Bitmap bitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final int n2 = width * height;
        final int[] array = new int[n2];
        bitmap.getPixels(array, 0, width, 0, 0, width, height);
        if (n != -1) {
            n = 2;
        }
        else {
            n = 1;
        }
        for (int i = 0; i < n2; ++i) {
            final int n3 = array[i];
            array[i] = ((n3 & 0xFF) >> n | ((n3 >> 16 & 0xFF) >> n << 16 | 0xFF000000 | (n3 >> 8 & 0xFF) >> n << 8));
        }
        bitmap2.setPixels(array, 0, width, 0, 0, width, height);
        return bitmap2;
    }

    private static void toGray(final int[] array, final int n, final int n2) {
        int i = 0;
        int n3 = 0;
        while (i < n) {
            for (int j = 0; j < n2; ++j) {
                final int n4 = array[n3];
                int n5;
                if ((n5 = ((n4 >> 16 & 0xFF) >> 2) + ((n4 >> 8 & 0xFF) >> 1) + ((n4 & 0xFF) >> 2)) > 249) {
                    n5 = 255;
                }
                array[n3] = ((n5 - 255) * (n4 >> 24 & 0xFF) / 255 + 255 & 0xFF);
                ++n3;
            }
            ++i;
        }
    }

    public static Bitmap toGrayscale(final Bitmap bitmap) {
        final Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap2);
        final Paint paint = new Paint();
        paint.setColor(-1);
        canvas.drawRect(0.0f, 0.0f, (float)bitmap2.getWidth(), (float)bitmap2.getHeight(), paint);
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0.0f);
        paint.setColorFilter((ColorFilter)new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmap2;
    }

    private static Bitmap toGrayscale(final Bitmap bitmap, final float n) {
        final Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap2.setHasAlpha(false);
        bitmap2.eraseColor(-1);
        final Canvas canvas = new Canvas(bitmap2);
        final Paint paint = new Paint();
        final ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(n / 100.0f);
        paint.setColorFilter((ColorFilter)new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmap2;
    }

    private static Bitmap toInverted(final Bitmap bitmap, final float n) {
        final ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(new ColorMatrix(new float[] { -1.0f, 0.0f, 0.0f, 0.0f, 255.0f, 0.0f, -1.0f, 0.0f, 0.0f, 255.0f, 0.0f, 0.0f, -1.0f, 0.0f, 255.0f, 0.0f, 0.0f, 0.0f, n / 100.0f, 0.0f }));
        final Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap2);
        final Paint paint = new Paint();
        paint.setColorFilter((ColorFilter)colorFilter);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        return bitmap2;
    }

    private static int[] bestContrast(final int[] array, final int n, final int n2) {
        toGray(array, n, n2);
        final long[] array2 = new long[n * n2];
        for (int i = 0; i < n2; ++i) {
            long n3 = 0L;
            for (int j = 0; j < n; ++j) {
                final int n4 = j * n2 + i;
                n3 += array[n4];
                if (i == 0) {
                    array2[n4] = n3;
                }
                else {
                    array2[n4] = array2[n4 - 1] + n3;
                }
            }
        }
        final int n5 = n2 / 16;
        for (int k = 0; k < n2; ++k) {
            for (int l = 0; l < n; ++l) {
                final int n6 = l * n2 + k;
                final int n7 = k - n5;
                final int n8 = k + n5;
                final int n9 = l - n5;
                final int n10 = l + n5;
                int n11;
                if ((n11 = n7) < 0) {
                    n11 = 0;
                }
                int n12;
                if ((n12 = n8) >= n2) {
                    n12 = n2 - 1;
                }
                int n13;
                if ((n13 = n9) < 0) {
                    n13 = 0;
                }
                int n14;
                if ((n14 = n10) >= n) {
                    n14 = n - 1;
                }
                final int n15 = n14 * n2;
                final long n16 = array2[n15 + n12];
                final int n17 = n13 * n2;
                if (array[n6] * ((n12 - n11) * (n14 - n13)) < (long)((n16 - array2[n12 + n17] - array2[n15 + n11] + array2[n17 + n11]) * 0.85f)) {
                    array[n6] = 0;
                }
                else {
                    array[n6] = 255;
                }
            }
        }
        return array;
    }

    public static byte[] dithering(int[] array, final int n, final int n2, int i) {
        if (i == 0) {
            return format_K_threshold(array, n2, n);
        }
        if (i != 9) {
            switch (i) {
                default: {
                    array = ditheringSF_real(array, n, n2);
                    break;
                }
                case 7: {
                    return bayerMatrix(array, n, n2);
                }
                case 6: {
                    array = bestContrast(array, n, n2);
                    break;
                }
                case 5: {
                    //array = sketchImage(array, n, n2, context);
                    break;
                }
                case 4: {
                    array = ditheringSiera16(array, n, n2);
                    break;
                }
                case 3: {
                    array = ditheringBurkes(array, n, n2);
                    break;
                }
                case 2: {
                    array = ditheringAtkinson(array, n, n2);
                    break;
                }
            }
            final byte[] array2 = new byte[n2 * n];
            i = 0;
            int n3 = 0;
            while (i < n) {
                for (int j = 0; j < n2; ++j) {
                    if (array[n3] > 127) {
                        array2[n3] = 0;
                    }
                    else {
                        array2[n3] = 1;
                    }
                    ++n3;
                }
                ++i;
            }
            return array2;
        }
        return format_K_threshold127(array, n2, n);
    }

    private static int[] ditheringAtkinson(final int[] array, final int n, final int n2) {
        int i = 0;
        int n3 = 0;
        while (i < n) {
            for (int j = 0; j < n2; ++j) {
                final int n4 = array[n3];
                array[n3] = (((n4 >> 16 & 0xFF) >> 2) + ((n4 >> 8 & 0xFF) >> 1) + ((n4 & 0xFF) >> 2) - 255) * (n4 >> 24 & 0xFF) / 255 + 255;
                ++n3;
            }
            ++i;
        }
        int k = 0;
        int n5 = 0;
        while (k < n - 1) {
            int n8;
            for (int l = 1; l < n2 - 1; ++l, n5 = n8) {
                final int n6 = array[n5];
                int n7;
                if (n6 > 127) {
                    n7 = 255;
                }
                else {
                    n7 = 0;
                }
                array[n5] = n7;
                n8 = n5 + 1;
                final int n9 = array[n8];
                final int n10 = (n6 - n7) / 8;
                array[n8] = n9 + n10;
                final int n11 = n5 + 2;
                array[n11] += n10;
                final int n12 = n5 + n2;
                final int n13 = n12 - 1;
                array[n13] += n10;
                array[n12] += n10;
                final int n14 = n12 + 1;
                array[n14] += n10;
                if (k < n - 2) {
                    final int n15 = n12 + n2;
                    array[n15] += n10;
                }
            }
            ++k;
        }
        return array;
    }

    private static int[] ditheringBurkes(final int[] array, final int n, final int n2) {
        int i = 0;
        int n3 = 0;
        while (i < n) {
            for (int j = 0; j < n2; ++j) {
                final int n4 = array[n3];
                array[n3] = (((n4 >> 16 & 0xFF) >> 2) + ((n4 >> 8 & 0xFF) >> 1) + ((n4 & 0xFF) >> 2) - 255) * (n4 >> 24 & 0xFF) / 255 + 255;
                ++n3;
            }
            ++i;
        }
        int k = 0;
        int n5 = 0;
        while (k < n - 2) {
            for (int l = 0; l < n2; ++l) {
                final int n6 = array[n5];
                int n7;
                if (n6 > 127) {
                    n7 = 255;
                }
                else {
                    n7 = 0;
                }
                array[n5] = n7;
                if (l > 1 && l < n2 - 1) {
                    final int n8 = n6 - n7;
                    final int n9 = n5 + 1;
                    final int n10 = array[n9];
                    final int n11 = n8 * 8 / 32;
                    array[n9] = n10 + n11;
                    final int n12 = n5 + 2;
                    final int n13 = array[n12];
                    final int n14 = n8 * 4 / 32;
                    array[n12] = n13 + n14;
                    final int n15 = n5 + n2;
                    final int n16 = n15 - 2;
                    array[n16] += n8 * 2 / 32;
                    final int n17 = n15 - 1;
                    array[n17] += n14;
                    array[n15] += n11;
                    final int n18 = n15 + 1;
                    array[n18] += n14;
                    final int n19 = n15 + 2;
                    array[n19] += n8 * 3 / 32;
                }
                ++n5;
            }
            ++k;
        }
        return array;
    }

    private static int[] ditheringSF_real(final int[] array, final int n, final int n2) {
        toGray(array, n, n2);
        int i = 0;
        int n3 = 0;
        while (i < n - 1) {
            for (int j = 0; j < n2 - 1; ++j) {
                final int n4 = array[n3];
                int n5;
                if (n4 > 127) {
                    n5 = 255;
                }
                else {
                    n5 = 0;
                }
                array[n3] = n5;
                if (j > 0) {
                    final int n6 = n4 - n5;
                    final int n7 = n3 + 1;
                    array[n7] += n6 * 7 / 16;
                    int n8 = n3 + n2;
                    final int n9 = n8 - 1;
                    array[n9] += n6 * 3 / 16;
                    array[n8] += n6 * 5 / 16;
                    ++n8;
                    array[n8] += n6 / 16;
                }
                ++n3;
            }
            ++i;
        }
        return array;
    }

    private static int[] ditheringSiera16(final int[] array, final int n, final int n2) {
        int i = 0;
        int n3 = 0;
        while (i < n) {
            for (int j = 0; j < n2; ++j) {
                final int n4 = array[n3];
                array[n3] = (((n4 >> 16 & 0xFF) >> 2) + ((n4 >> 8 & 0xFF) >> 1) + ((n4 & 0xFF) >> 2) - 255) * (n4 >> 24 & 0xFF) / 255 + 255;
                ++n3;
            }
            ++i;
        }
        int k = 0;
        int n5 = 0;
        while (k < n - 2) {
            for (int l = 0; l < n2; ++l) {
                final int n6 = array[n5];
                int n7;
                if (n6 > 127) {
                    n7 = 255;
                }
                else {
                    n7 = 0;
                }
                array[n5] = n7;
                if (l > 1 && l < n2 - 1) {
                    final int n8 = n6 - n7;
                    final int n9 = n5 + 1;
                    array[n9] += n8 * 4 / 16;
                    final int n10 = n5 + 2;
                    final int n11 = array[n10];
                    final int n12 = n8 * 2 / 16;
                    array[n10] = n11 + n12;
                    final int n13 = n5 + n2;
                    final int n14 = n13 - 2;
                    final int n15 = array[n14];
                    final int n16 = n8 / 16;
                    array[n14] = n15 + n16;
                    final int n17 = n13 - 1;
                    array[n17] += n12;
                    array[n13] += n8 * 3 / 16;
                    final int n18 = n13 + 1;
                    array[n18] += n12;
                    final int n19 = n13 + 2;
                    array[n19] += n16;
                }
                ++n5;
            }
            ++k;
        }
        return array;
    }

    public static byte[] eachLinePixForStar(final byte[] array, int i) {
        final int n = array.length / i;
        final int n2 = i / 8;
        final int n3 = n2 + 9;
        final byte[] array2 = new byte[n3 * n];
        i = 0;
        int n4 = 0;
        while (i < n) {
            final int n5 = i * n3;
            array2[n5] = 27;
            array2[n5 + 1] = 29;
            array2[n5 + 2] = 83;
            array2[n5 + 3] = 1;
            array2[n5 + 4] = (byte)(n2 % 256);
            array2[n5 + 5] = (byte)(n2 / 256);
            array2[n5 + 6] = 1;
            array2[n5 + 8] = (array2[n5 + 7] = 0);
            for (int j = 0; j < n2; ++j) {
                array2[n5 + 9 + j] = (byte)(GraphLibrary.p0[array[n4]] + GraphLibrary.p1[array[n4 + 1]] + GraphLibrary.p2[array[n4 + 2]] + GraphLibrary.p3[array[n4 + 3]] + GraphLibrary.p4[array[n4 + 4]] + GraphLibrary.p5[array[n4 + 5]] + GraphLibrary.p6[array[n4 + 6]] + array[n4 + 7]);
                n4 += 8;
            }
            ++i;
        }
        return array2;
    }

    public static byte[] format_K_threshold(final int[] array, final int n, final int n2) {
        final byte[] array2 = new byte[n * n2];
        int i = 0;
        int n3 = 0;
        int n4 = 0;
        while (i < n2) {
            for (int j = 0; j < n; ++j) {
                n3 += (array[n4] & 0xFF);
                ++n4;
            }
            ++i;
        }
        int n5;
        if ((n5 = n3 / n2 / n) > 254) {
            n5 = 254;
        }
        int k = 0;
        int n6 = 0;
        while (k < n2) {
            for (int l = 0; l < n; ++l) {
                if ((array[n6] & 0xFF) > n5) {
                    array2[n6] = 0;
                }
                else {
                    array2[n6] = 1;
                }
                ++n6;
            }
            ++k;
        }
        return array2;
    }

    public static byte[] format_K_threshold127(final int[] array, final int n, final int n2) {
        final byte[] array2 = new byte[n * n2];
        int i = 0;
        int n3 = 0;
        while (i < n2) {
            for (int j = 0; j < n; ++j) {
                if ((array[n3] & 0xFF) > 127) {
                    array2[n3] = 0;
                }
                else {
                    array2[n3] = 1;
                }
                ++n3;
            }
            ++i;
        }
        return array2;
    }

    public static byte[] graphicsFormat(final byte[] array, final int n, final int n2, int n3) throws Exception {
        if (n2 / 8 * 8 != n2) {
            throw new Exception("height not multiple 8");
        }
        if (n2 / n3 * n3 == n2) {
            final byte[] array2 = new byte[n2 * n / 8];
            int i = 0;
            int n4 = 0;
            n3 = 0;
            int n5 = 0;
            while (i < n) {
                int n7;
                for (int j = 0; j < n2; ++j, n5 = n7) {
                    final int n6 = n3 * 2 + array[j * n + i];
                    ++n4;
                    n3 = n6;
                    n7 = n5;
                    if (n4 > 7) {
                        array2[n5] = (byte)n6;
                        n7 = n5 + 1;
                        n4 = 0;
                        n3 = 0;
                    }
                }
                ++i;
            }
            return array2;
        }
        throw new Exception("height not multiple bundleHeightInBytes");
    }
}
