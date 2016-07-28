package com.gotye.live.demo.view;

import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class ViewHelper {

	private static int id = 123;

	/*
	 * 获取控件宽
	 */
	public static int getWidth(View view) {
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		return (view.getMeasuredWidth());
	}

	/*
	 * 获取控件高
	 */
	public static int getHeight(View view) {
		int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		view.measure(w, h);
		return (view.getMeasuredHeight());
	}

	public static void setBackgroundDrawable(Context context, View view, Drawable d) {
		if (android.os.Build.VERSION.SDK_INT < 16) {
			if (d == null) {
				view.setBackgroundDrawable(null);
			} else {
				view.setBackgroundDrawable(d);
			}

		} else {
			if (d == null) {
				view.setBackground(null);
			} else {
				view.setBackground(d);
			}

		}
	}

	public static int getWindowHeight(Context context) {
		if (android.os.Build.VERSION.SDK_INT < 13) {
			return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
		} else {
			Point point = new Point();

			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(point);
			return point.y;
		}
	}

	public static int getWindowWidth(Context context) {
		if (android.os.Build.VERSION.SDK_INT < 13) {
			return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		} else {
			Point point = new Point();
			((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(point);
			return point.x;
		}
	}

	public static int getHeight(Display display) {
		if (android.os.Build.VERSION.SDK_INT < 13) {
			return display.getHeight();
		} else {
			Point point = new Point();
			display.getSize(point);
			return point.y;
		}
	}

	public static int getWidth(Display display) {
		if (android.os.Build.VERSION.SDK_INT < 13) {
			return display.getWidth();
		} else {
			Point point = new Point();
			display.getSize(point);
			return point.x;
		}
	}
public  static DisplayMetrics getscreenDisplayMetrics(Context context)
{
	DisplayMetrics dm = new DisplayMetrics();

	((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);

	return dm;
}

	public static View findSurfaceView(ViewGroup root) {
		if (root == null) {
			return null;
		}
		int count = root.getChildCount();
		for (int i = 0; i < count; i++) {
			View child = root.getChildAt(i);
			if (child instanceof ViewGroup) {
				return findSurfaceView((ViewGroup) child);
			} else {
				if (child instanceof GLSurfaceView || child instanceof SurfaceView) {
					return child;
				} else {
					continue;
				}
			}
		}
		return null;
	}

	public static ViewGroup getRootView(View self) {
		ViewParent father = null;
		father = (ViewParent) self;
		ViewParent tmp = null;
		while ((tmp = father.getParent()) != null) {
			if (!(tmp instanceof ViewGroup)) {
				break;
			}
			father = tmp;
		}
		return (ViewGroup) father;
	}

	public static int generateViewId() {
		return id++;
	}

	public static boolean isBackground(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				/*
				 * BACKGROUND=400 EMPTY=500 FOREGROUND=100 GONE=1000
				 * PERCEPTIBLE=130 SERVICE=300 ISIBLE=200
				 */
				Log.i(context.getPackageName(), "此appimportace =" + appProcess.importance
						+ ",context.getClass().getName()=" + context.getClass().getName());
				if (appProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public static void setCursorDrawableColor(EditText editText, int color) {
		try {
			Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
			fCursorDrawableRes.setAccessible(true);
			int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
			Field fEditor = TextView.class.getDeclaredField("mEditor");
			fEditor.setAccessible(true);
			Object editor = fEditor.get(editText);
			Class<?> clazz = editor.getClass();
			Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
			fCursorDrawable.setAccessible(true);
			Drawable[] drawables = new Drawable[2];
			drawables[0] = editText.getContext().getResources().getDrawable(mCursorDrawableRes);
			drawables[1] = editText.getContext().getResources().getDrawable(mCursorDrawableRes);
			drawables[0].setColorFilter(color, PorterDuff.Mode.SRC_IN);
			drawables[1].setColorFilter(color, PorterDuff.Mode.SRC_IN);
			fCursorDrawable.set(editor, drawables);
		} catch (final Throwable ignored) {
		}
	}
}
