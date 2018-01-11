package com.yjing.imageeditlibrary.editimage.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.yjing.imageeditlibrary.editimage.inter.EditFunctionOperationInterface;
import com.yjing.imageeditlibrary.editimage.inter.OnViewTouthListener;

import java.util.concurrent.CopyOnWriteArrayList;

public class CustomPaintView extends View implements EditFunctionOperationInterface {
    private Paint mPaint;
    private Bitmap mDrawBit;

    private Canvas mPaintCanvas = null;
    private Matrix mMatrix;

    private float last_x;
    private float last_y;

    private boolean isOperation = false;
    private Path mPath;
    private OnViewTouthListener onViewTouthListener;
    private float[] floats = new float[]{1,0,0,0,1,0,0,0,1};
    /**
     * 模拟栈，保存涂鸦操作，便于撤销
     */
    private CopyOnWriteArrayList<PaintPath> mUndoStack = new CopyOnWriteArrayList<>();
    private RectF mRoundRectf;

    public CustomPaintView(Context context) {
        super(context);
        initPaint();
    }

    public CustomPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public CustomPaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomPaintView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //System.out.println("width = "+getMeasuredWidth()+"     height = "+getMeasuredHeight());
        if (mDrawBit == null) {
            generatorBit();
        }
    }

    /**
     * 创建画布
     */
    private void generatorBit() {
        mDrawBit = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        mPaintCanvas = new Canvas(mDrawBit);
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    /**
     * 复制一支信息相同的画笔
     */
    private Paint copyPaint() {
        Paint paint = new Paint();
        paint.setColor(mPaint.getColor());
        paint.setAntiAlias(mPaint.isAntiAlias());
        paint.setStrokeJoin(mPaint.getStrokeJoin());
        paint.setStrokeCap(mPaint.getStrokeCap());
        paint.setStyle(mPaint.getStyle());
        paint.setStrokeWidth(mPaint.getStrokeWidth());
        return paint;
    }

    public void setColor(int color) {
        this.mPaint.setColor(color);
    }

    public void setWidth(float width) {
        this.mPaint.setStrokeWidth(width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawBit != null) {

            this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            canvas.concat(mMatrix);
            this.setLayerType(View.LAYER_TYPE_NONE, null);

            canvas.drawBitmap(mDrawBit, 0, 0, null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //外部通过isOperation来控制是否要对此涂鸦view进行操作
        if (!isOperation) {
            return isOperation;
        }

        boolean ret = super.onTouchEvent(event);
        float x = event.getX();
        float y = event.getY();
        float descX = (float) ((Math.abs(floats[2]) + x) / floats[0]);
        float descY = (float) ((Math.abs(floats[5]) + y) / floats[4]);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!checkIsOnBounds(descX,descY)) {
                    return false;
                }
                if (onViewTouthListener != null) {
                    onViewTouthListener.onTouchDown();
                }
                // 每次down下去重新new一个Path
                mPath = new Path();
                mPath.moveTo(descX, descY);
                ret = true;
                last_x = x;
                last_y = y;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!checkIsOnBounds(descX,descY)) {
                    if (onViewTouthListener != null) {
                        onViewTouthListener.onTouchUp();
                    }
                    return false;
                }
                if (onViewTouthListener != null) {
                    onViewTouthListener.onTouchMove();
                }
                ret = true;
                // 从x1,y1到x2,y2画一条贝塞尔曲线，更平滑(直接用mPath.lineTo也是可以的)
                if (mPath == null) {
                    mPath = new Path();
                    mPath.moveTo(descX,descY);
                    ret = true;
                    last_x = x;
                    last_y = y;
                }else {
                    mPath.lineTo(descX, descY);
                    mPaintCanvas.drawPath(mPath, mPaint);
                }

//                mPaintCanvas.drawLine(last_x, last_y, x, y, mPaint);
                last_x = x;
                last_y = y;
                this.postInvalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (onViewTouthListener != null) {
                    onViewTouthListener.onTouchUp();
                }
                mUndoStack.add(new PaintPath(mPath, copyPaint()));
                Log.i("wangyanjing", "数组里面加入了一个mPath"+mPath.hashCode()+"===="+mUndoStack.size());
                mPaintCanvas.drawPath(mPath, mPaint);
                ret = false;
                this.postInvalidate();
                break;
        }
        return ret;
    }

    private boolean checkIsOnBounds(float x, float y){
        if (mRoundRectf == null) {
            return true;
        }
        return mRoundRectf.contains(x,y);
    }

    public void setMainLevelMatrix(Matrix matrix,RectF rectF){
        mMatrix = matrix;
        mRoundRectf = rectF;
        mMatrix.getValues(floats);
        postInvalidate();
    }


    public void setOnViewTouthListener(OnViewTouthListener onViewTouthListener) {
        this.onViewTouthListener = onViewTouthListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDrawBit != null && !mDrawBit.isRecycled()) {
            mDrawBit.recycle();
        }
    }

    public Bitmap getPaintBit() {
        return mDrawBit;
    }

    public void reset() {
        resetCanvas();
        //清空保存操作的栈容器
        mUndoStack.clear();
    }

    private void resetCanvas() {
        if (mDrawBit != null && !mDrawBit.isRecycled()) {
            mDrawBit.recycle();
        }
        invalidate();
        generatorBit();
    }

    @Override
    public void setIsOperation(boolean isOperation) {
        this.isOperation = isOperation;
    }

    @Override
    public Boolean getIsOperation() {
        return null;
    }


    /**
     * 撤销
     */
    public void undo() {
        if (mUndoStack.size() > 0) {
            PaintPath undoable = mUndoStack.remove(mUndoStack.size() - 1);
            Log.i("wangyanjing", "撤销了一个mPath"+undoable.hashCode()+"===="+mUndoStack.size());
            resetCanvas();
            draw(mPaintCanvas, mUndoStack);
            invalidate();
        }
    }

    private void draw(Canvas mPaintCanvas, CopyOnWriteArrayList<PaintPath> mUndoStack) {
        for (PaintPath paintPath : mUndoStack) {
            mPaintCanvas.drawPath(paintPath.getPath(), paintPath.getPaint());
        }
    }
}//end class
