/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 Roman.Golovanov@gmail.com and other
 * respective project committers (see project home page)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.ametro.libs;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {

    private static String[] splitCommaSeparaterString(String value) {
        value = value.replaceAll("/\\(.*\\)/", "");
        return value.split(",");
    }

    private static final Pattern csvPattern = Pattern.compile("(?:^|,)(\"(?:[^\"]|\"\")*\"|[^,]*)");


    public static String[] splitCSV(String line) {
        ArrayList<String> elements = new ArrayList<String>();
        Matcher m = csvPattern.matcher(line);
        while (m.find()) {
            elements.add(m.group()
                    .replaceAll("^,", "") // remove first comma if any
                            //.replaceAll( "^?\"(.*)\"$", "$1" ) // remove outer quotations if any
                    .replaceAll("^\"(.*)\"$", "$1") // remove outer quotations if any
                    .replaceAll("\"\"", "\"")); // replace double inner quotations if any
        }
        return elements.toArray(new String[elements.size()]);
    }

    public static String[] parseStringArray(String value) {
        return splitCommaSeparaterString(value);
        //return splitCSV(value);
    }

    public static Integer[] parseIntegerArray(String value) {
        String[] parts = splitCommaSeparaterString(value);
        ArrayList<Integer> vals = new ArrayList<Integer>();
        for (int i = 0; i < parts.length; i++) {
            try {
                Integer val = Integer.parseInt(parts[i].trim());
                vals.add(val);
            } catch (Exception ex) {
                vals.add(null);
            }
        }
        return vals.toArray(new Integer[vals.size()]);
    }

    public static Double[] parseDoubleArray(String value) {
        String[] parts = splitCommaSeparaterString(value);
        ArrayList<Double> vals = new ArrayList<Double>();
        for (int i = 0; i < parts.length; i++) {
            try {
                Double val = Double.parseDouble(parts[i].trim());
                vals.add(val);
            } catch (Exception ex) {
                vals.add(null);
            }
        }
        return vals.toArray(new Double[vals.size()]);
    }

    public static Point[] parsePointArray(String value) {
        String[] parts = splitCommaSeparaterString(value);
        ArrayList<Point> points = new ArrayList<Point>();
        for (int i = 0; i < parts.length / 2; i++) {
            Point point = new Point();
            point.x = Integer.parseInt(parts[i * 2].trim());
            point.y = Integer.parseInt(parts[i * 2 + 1].trim());
            points.add(point);
        }
        return points.toArray(new Point[points.size()]);
    }

    public static Rect[] parseRectangleArray(String value) {
        String[] parts = splitCommaSeparaterString(value);
        ArrayList<Rect> rectangles = new ArrayList<Rect>();
        for (int i = 0; i < parts.length / 4; i++) {
            int x1 = Integer.parseInt(parts[i * 4].trim());
            int y1 = Integer.parseInt(parts[i * 4 + 1].trim());
            int x2 = x1 + Integer.parseInt(parts[i * 4 + 2].trim());
            int y2 = y1 + Integer.parseInt(parts[i * 4 + 3].trim());
            rectangles.add(new Rect(x1, y1, x2, y2));
        }
        return rectangles.toArray(new Rect[rectangles.size()]);
    }

    public static Rect parseRectangle(String value) {
        String[] parts = splitCommaSeparaterString(value);
        int x1 = Integer.parseInt(parts[0].trim());
        int y1 = Integer.parseInt(parts[1].trim());
        int x2 = x1 + Integer.parseInt(parts[2].trim());
        int y2 = y1 + Integer.parseInt(parts[3].trim());
        return new Rect(x1, y1, x2, y2);
    }

    public static Point parsePoint(String value) {
        String[] parts = splitCommaSeparaterString(value);
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        return new Point(x, y);
    }

    public static PointF parsePointF(String value) {
        String[] parts = splitCommaSeparaterString(value);
        float x = Float.parseFloat(parts[0].trim());
        float y = Float.parseFloat(parts[1].trim());
        return new PointF(x, y);
    }
    
    public static String convertCommas(String str) {
        StringBuilder sb = new StringBuilder(str);
        boolean f = false;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch == '(') {
                f = true;
            } else if (ch == ')') {
                f = false;
            } else if (f && ch == ',') {
                sb.setCharAt(i, ';');
            }
        }
        return sb.toString();
    }

	public static Integer parseNullableInteger(String text) {
        if (text != null && !text.equals("")) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
	}
	
    public static Double parseNullableDouble(String text) {
        if (text != null && !text.equals("")) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }


    public static void serializePoint(ObjectOutputStream out, Point point) throws IOException {
        out.writeBoolean(point != null);
        if (point != null) {
            out.writeInt(point.x);
            out.writeInt(point.y);
        }
    }

    public static Point deserializePoint(ObjectInputStream in) throws IOException {
        boolean isNotNull = in.readBoolean();
        if (isNotNull) return new Point(in.readInt(), in.readInt());
        return null;
    }

    public static void serializePointArray(ObjectOutputStream out, Point[] points) throws IOException {
        out.writeBoolean(points != null);
        if (points != null) {
            int len = points.length;
            out.writeInt(len);
            for (int i = 0; i < len; i++) {
                out.writeInt(points[i].x);
                out.writeInt(points[i].y);
            }
        }
    }

    public static Point[] deserializePointArray(ObjectInputStream in) throws IOException {
        boolean isNotNull = in.readBoolean();
        if (isNotNull) {
            int len = in.readInt();
            Point[] points = new Point[len];
            for (int i = 0; i < len; i++) {
                points[i] = new Point(in.readInt(), in.readInt());
            }
            return points;
        }
        return null;

    }

    public static void serializeRect(ObjectOutputStream out, Rect rect) throws IOException {
        out.writeBoolean(rect != null);
        if (rect != null) {
            out.writeInt(rect.left);
            out.writeInt(rect.top);
            out.writeInt(rect.right);
            out.writeInt(rect.bottom);
        }
    }

    public static Rect deserializeRect(ObjectInputStream in) throws IOException {
        boolean isNotNull = in.readBoolean();
        if (isNotNull) return new Rect(in.readInt(), in.readInt(), in.readInt(), in.readInt());
        return null;
    }

    public static void serializeRectArray(ObjectOutputStream out, Rect[] rects) throws IOException {
        out.writeBoolean(rects != null);
        if (rects != null) {
            int len = rects.length;
            out.writeInt(len);
            for (int i = 0; i < len; i++) {
                out.writeInt(rects[i].left);
                out.writeInt(rects[i].top);
                out.writeInt(rects[i].right);
                out.writeInt(rects[i].bottom);
            }
        }
    }

    public static Rect[] deserializeRectArray(ObjectInputStream in) throws IOException {
        boolean isNotNull = in.readBoolean();
        if (isNotNull) {
            int len = in.readInt();
            Rect[] rects = new Rect[len];
            for (int i = 0; i < len; i++) {
                rects[i] = new Rect(in.readInt(), in.readInt(), in.readInt(), in.readInt());
            }
            return rects;
        }
        return null;
    }


}