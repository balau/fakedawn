/**
 *   Copyright 2012 Francesco Balducci
 *
 *   This file is part of FakeDawn.
 *
 *   FakeDawn is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   FakeDawn is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with FakeDawn.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.balau.fakedawn;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;

/**
 * @author francesco
 *
 */
public class IntervalSlider extends View {

	private float m_leftCursorPos = 0.1F;
	private float m_rightCursorPos = 0.9F;
	private ScaleGestureDetector m_pinchDetector;
	private GestureDetector m_gestureDetector;

	private int m_touchedPart;
	private IntervalSlider.GestureListener m_gestureListener;

	public static final int TOUCH_ALL = 0;
	public static final int TOUCH_LEFT = 1;
	public static final int TOUCH_RIGHT = 2;

	private String m_textLeft = "";
	private String m_textRight = "";

	private Paint m_rectPaint;
	
	private void ConstructDetectors(Context context)
	{
		m_rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		m_rectPaint.setColor(0xFF0000FF);
		m_rectPaint.setStrokeWidth(0);

		m_gestureListener = new IntervalSlider.GestureListener();
		m_pinchDetector = new ScaleGestureDetector(context, m_gestureListener);
		m_gestureDetector = new GestureDetector(context, m_gestureListener);
		m_gestureDetector.setIsLongpressEnabled(false);
	}

	/**
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public IntervalSlider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ConstructDetectors(context);
	}

	/**
	 * @param context
	 * @param attrs
	 */
	public IntervalSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		ConstructDetectors(context);
	}

	public IntervalSlider(Context context) {
		super(context);
		ConstructDetectors(context);
	}

	/* (non-Javadoc)
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setRightPos(float p)
	{
		p = Math.max(m_leftCursorPos, Math.min(1.0F, p));
		if(p != m_rightCursorPos)
		{
			m_rightCursorPos = p;
			this.invalidate();
		}
	}

	public float getRightPos()
	{
		return m_rightCursorPos;
	}

	public float moveRightPos(float d)
	{
		float p = getRightPos();
		setRightPos(p + d);
		return getRightPos() - p;
	}

	public void setLeftPos(float p)
	{
		p = Math.max(0.0F, Math.min(getRightPos(), p));
		if(p != m_leftCursorPos)
		{
			m_leftCursorPos = p;
			this.invalidate();
		}
	}

	public float getLeftPos()
	{
		return m_leftCursorPos;
	}

	public float moveLeftPos(float d)
	{
		float p = getLeftPos();
		setLeftPos(p + d);
		return getLeftPos() - p;
	}

	public void setRightText(String t)
	{
		if(!m_textRight.equals(t))
		{
			m_textRight = t;
			this.invalidate();
		}
	}

	public void setLeftText(String t)
	{
		if(!m_textLeft.equals(t))
		{
			m_textLeft = t;
			this.invalidate();
		}
	}

	public void setRectPaint(Paint p)
	{
		m_rectPaint = p;
		this.invalidate();
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		int w, h;
		w = getMeasuredWidth();
		h = getMeasuredHeight();

		Rect r = new Rect(0, 0, w, (3*h)/4);
		canvas.drawRect(r, m_rectPaint);

		Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		cursorPaint.setStrokeWidth(5);
		cursorPaint.setColor(0xFFFFFFFF);
		int leftCursorEnd = (13*h)/16;
		int rightCursorEnd = (15*h)/16;
		canvas.drawLine(w*m_leftCursorPos, 0, w*m_leftCursorPos, leftCursorEnd, cursorPaint);
		canvas.drawCircle(w*m_leftCursorPos, leftCursorEnd, h/16, cursorPaint);
		canvas.drawLine(w*m_rightCursorPos, 0, w*m_rightCursorPos, rightCursorEnd, cursorPaint);
		canvas.drawCircle(w*m_rightCursorPos, rightCursorEnd, h/16, cursorPaint);

		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(0xFFFFFFFF);
		textPaint.setTextSize(h/10);
		Rect bounds = new Rect();

		textPaint.getTextBounds(m_textLeft, 0, m_textLeft.length(), bounds);
		if(w*m_leftCursorPos-h/16-bounds.width() > 0)
		{
			canvas.drawText(m_textLeft, 
					w*m_leftCursorPos-h/16-bounds.right,
					leftCursorEnd-bounds.exactCenterY(),
					textPaint);
		}
		else
		{
			canvas.drawText(m_textLeft, 
					w*m_leftCursorPos+h/16-bounds.left,
					leftCursorEnd-bounds.exactCenterY(),
					textPaint);
		}

		textPaint.getTextBounds(m_textRight, 0, m_textRight.length(), bounds);
		if(w*m_rightCursorPos+h/16+bounds.width() > canvas.getWidth())
		{
			canvas.drawText(m_textRight, 
					w*m_rightCursorPos-h/16-bounds.right,
					rightCursorEnd-bounds.exactCenterY(),
					textPaint);
		}
		else
		{
			canvas.drawText(m_textRight, 
					w*m_rightCursorPos+h/16-bounds.left,
					rightCursorEnd-bounds.exactCenterY(),
					textPaint);
		}

		super.onDraw(canvas);
	}

	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean managed = false;
		managed = managed || m_gestureDetector.onTouchEvent(event);
		managed = managed || m_pinchDetector.onTouchEvent(event);
		return managed;
	}

	
	private OnClickListener m_clickListener = null;
	
	/* (non-Javadoc)
	 * @see android.view.View#setOnClickListener(android.view.View.OnClickListener)
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		m_clickListener = l;
	}

	private void viewClicked()
	{
		if(m_clickListener != null)
		{
			m_clickListener.onClick(this);
		}
	}

	public interface OnCursorsMovedListener {
		void onCursorsMoved(IntervalSlider i, float leftMovement, float rightMovement);
	}
	
	private OnCursorsMovedListener m_cursorsMovedListener = null;
	
	public void setOnCursorsMovedListener(OnCursorsMovedListener l)
	{
		m_cursorsMovedListener = l;
	}
	
	private void cursorsMoved(float left, float right)
	{
		if(m_cursorsMovedListener != null)
		{
			m_cursorsMovedListener.onCursorsMoved(this, left, right);
		}
	}
	
	public int getLastTouched()
	{
		return m_touchedPart;
	}
	
	private class GestureListener implements OnScaleGestureListener, OnGestureListener
	{
		private float m_rightCursorPosBeforeMotionEvent;
		private float m_leftCursorPosBeforeMotionEvent;
		private boolean m_scaleInProgress;

		public GestureListener()
		{
			m_scaleInProgress = false;
		}

		public boolean onScale(ScaleGestureDetector detector) {
			int w = getMeasuredWidth();
			float center = (m_rightCursorPosBeforeMotionEvent + m_leftCursorPosBeforeMotionEvent)/2.0F;
			float distance = (detector.getCurrentSpan() - detector.getPreviousSpan())/2.0F/(float)w;
			float left = Math.min(center, getLeftPos() - distance);
			float right = Math.max(center, getRightPos() + distance);
			setLeftPos(left);
			setRightPos(right);
			return true;
		}

		public boolean onScaleBegin(ScaleGestureDetector detector) {
			m_rightCursorPosBeforeMotionEvent = getRightPos();
			m_leftCursorPosBeforeMotionEvent = getLeftPos();
			m_scaleInProgress = true;
			return true;
		}

		public void onScaleEnd(ScaleGestureDetector detector) {
			cursorsMoved(getLeftPos() - m_leftCursorPosBeforeMotionEvent,
					getRightPos() - m_rightCursorPosBeforeMotionEvent);
		}

		private float getDistance(float x1, float y1, float x2, float y2)
		{
			return (float) Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
		}

		private int getTouchedPart(float x, float y)
		{
			int w = getMeasuredWidth();
			int h = getMeasuredHeight();

			float distanceLeft = getDistance(x,y,w*m_leftCursorPos,(13*h)/16);
			float distanceRight = getDistance(x,y,w*m_rightCursorPos,(15*h)/16);
			if(distanceLeft < h/8 && distanceLeft <= distanceRight)
			{
				return TOUCH_LEFT;			
			}
			else if(distanceRight < h/8 && distanceRight <= distanceLeft)
			{
				return TOUCH_RIGHT;
			}
			else
			{
				return TOUCH_ALL;
			}
		}

		public boolean onDown(MotionEvent e) {
			m_scaleInProgress = false;
			m_touchedPart = getTouchedPart(e.getX(), e.getY());
			return true;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		public void onLongPress(MotionEvent e) {
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
				float distanceY) {
			float xMovement = -distanceX/getMeasuredWidth();
			float moved;
			switch(m_touchedPart)
			{
			case TOUCH_ALL:
				if(xMovement > 0)
				{
					moved = moveRightPos(xMovement);
					moveLeftPos(moved);
				}
				else
				{
					moved = moveLeftPos(xMovement);
					moveRightPos(moved);
				}
				cursorsMoved(moved, moved);
				break;
			case TOUCH_LEFT:
				moved = moveLeftPos(xMovement);
				cursorsMoved(moved, 0.0F);
				break;
			case TOUCH_RIGHT:
				moved = moveRightPos(xMovement);
				cursorsMoved(0.0F, moved);
				break;
			}
			return true;
		}

		public void onShowPress(MotionEvent e) {		
		}

		public boolean onSingleTapUp(MotionEvent e) {
			if(!m_scaleInProgress)
				viewClicked();
			m_scaleInProgress = false;
			return false;
		}		
	}
}
