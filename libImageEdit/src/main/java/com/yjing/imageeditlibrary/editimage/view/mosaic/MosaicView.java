package com.yjing.imageeditlibrary.editimage.view.mosaic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.yjing.imageeditlibrary.editimage.inter.EditFunctionOperationInterface;
import com.yjing.imageeditlibrary.editimage.inter.OnViewTouthListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MosaicView extends View implements EditFunctionOperationInterface {

    public static final String TAG = "MosaicView";

    // default image inner padding, in dip pixels
    private static final int INNER_PADDING = 0;

    /**
     * 马赛克粗细默认值
     **/
    private static final int PATH_WIDTH = 30;

    /**
     * 绘画板宽度和高度，数据来自要处理的图片
     */
    private int mImageWidth, mImageHeight;

    /**
     * 最终唯一保存操作状态的Bitmap
     */
    private Bitmap bmMosaicLayer;

    /**
     * 画笔
     */
    private int mBrushWidth;

    private Rect mImageRect;

    private int mPadding;

    /**
     * 触摸路径数据
     */
    private List<MosaicPath> touchPaths;

    private Context mContext;
    private boolean isOperation = false;
    private HashMap<MosaicUtil.Effect, Bitmap> mosaicResMap;//所有的马赛克资源

    /**
     * 当前所选马赛克样式,默认为MOSAIC
     */
    private MosaicUtil.Effect mosaicEffect = MosaicUtil.Effect.MOSAIC;
    private MosaicPath touchPath;
    private Matrix mMatrix;
    private float[] floats = new float[]{1,0,0,0,1,0,0,0,1};

    private OnViewTouthListener onViewTouthListener;
    private RectF mainRectf;

    public MosaicView(Context context) {
        this(context,null);
    }

    public MosaicView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MosaicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initDrawView();
    }

    public void setOnViewTouthListener(OnViewTouthListener onViewTouthListener) {
        this.onViewTouthListener = onViewTouthListener;
    }

    /**
     * 初始化绘画板 默认的情况下是马赛克模式
     */
    private void initDrawView() {
        touchPaths = new ArrayList<>();

        mPadding = dp2px(INNER_PADDING);
        mBrushWidth = dp2px(PATH_WIDTH);

        mImageRect = new Rect();
        setWillNotDraw(false);
    }

    /**
     * 设置画刷的宽度
     *
     * @param brushWidth 画刷宽度大小
     */
    public void setMosaicBrushWidth(int brushWidth) {
        this.mBrushWidth = dp2px(brushWidth);
    }

    /**
     * 设置所要打码的资源图片
     *
     * @param bitmap 资源图片路径
     */
    public void setMosaicBackgroundResource(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e("jarlen", "setMosaicBackgroundResource : bitmap == null");
            return;
        }
        int zoomNum = MosaicUtil.newInstance().getZoomNum(bitmap.getWidth(),bitmap.getHeight());
        mImageWidth = bitmap.getWidth() / zoomNum;
        mImageHeight = bitmap.getHeight() / zoomNum;

        requestLayout();
        invalidate();
    }

    /**
     * 设置马赛克样式资源
     *
     * @param mosaicResMap 样式图片资源
     */
    public void setMosaicResource(HashMap<MosaicUtil.Effect, Bitmap> mosaicResMap) {
        this.mosaicResMap = mosaicResMap;
    }


    /**
     * 重置绘画版
     *
     * @return
     */
    public boolean reset() {
        this.mImageWidth = 0;
        this.mImageHeight = 0;

        if (bmMosaicLayer != null) {
            bmMosaicLayer.recycle();
            bmMosaicLayer = null;
        }

        touchPaths.clear();

        invalidate();

        return true;
    }
    private float moveX = 0;
    private float moveY = 0;
    private static final int MOVE_DES_SUITABLE = 10;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        //外部通过isOperation变量可以控制是否操作本图层
        if (!isOperation || mosaicResMap == null) {
            return isOperation;
        }
        super.dispatchTouchEvent(event);

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return true;
        }

        if (mainRectf != null && !mainRectf.contains(x,y)) {
            if (onViewTouthListener != null) {
                onViewTouthListener.onTouchUp();
            }
            return true;
        }

        float ratio = (mImageRect.right - mImageRect.left) / (float) mImageWidth;

        x = (int) ((Math.abs(floats[2]) + x) / floats[0]);
        y = (int) ((Math.abs(floats[5]) + y) / floats[4]);

        x = (int) ((x - mImageRect.left) / ratio);
        y = (int) ((y - mImageRect.top) / ratio);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (onViewTouthListener != null) {
                    onViewTouthListener.onTouchDown();
                }
                moveX = event.getX();
                moveY = event.getY();
                touchPath = new MosaicPath();
                touchPath.drawPath = new Path();
                touchPath.drawPath.moveTo(x, y);
                touchPath.effect = mosaicEffect;
                touchPath.paintWidth = mBrushWidth;

                touchPaths.add(touchPath);
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - moveX) > MOVE_DES_SUITABLE || Math.abs(event.getY() - moveY) > MOVE_DES_SUITABLE) {
                    if (onViewTouthListener != null) {
                        onViewTouthListener.onTouchMove();
                    }
                }
                if (touchPath == null) {
                    touchPath = new MosaicPath();
                    touchPath.drawPath = new Path();
                    touchPath.drawPath.moveTo(x, y);
                    touchPath.effect = mosaicEffect;
                    touchPath.paintWidth = mBrushWidth;

                    touchPaths.add(touchPath);
                }else {
                    touchPath.drawPath.lineTo(x, y);
                }
                updatePathMosaic();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (onViewTouthListener != null) {
                    onViewTouthListener.onTouchUp();
                }
            case MotionEvent.ACTION_CANCEL:

                break;

        }
        return true;
    }

    public void setMainLevelMatrix(Matrix matrix, RectF rectF){
        mainRectf = rectF;
        mMatrix = matrix;
        matrix.getValues(floats);
        postInvalidate();
    }

    /**
     * 刷新绘画板
     */
    private void updatePathMosaic() {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        if (bmMosaicLayer != null) {
            bmMosaicLayer.recycle();
            bmMosaicLayer = null;
        }
        //创建马赛克图层
        bmMosaicLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Bitmap.Config.ARGB_4444);

        //向画布上画马赛克，正常画路径
        for (MosaicPath path : touchPaths) {
            drawPath(path);
        }
    }

    private void drawPath(MosaicPath path) {

        //临时的bitmap
        Bitmap bmTouchLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Bitmap.Config.ARGB_4444);

        //创建paint并设置
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setPathEffect(new CornerPathEffect(10));//将路径的转角变得圆滑
        paint.setStrokeWidth(mBrushWidth);
        paint.setColor(Color.BLUE);

        //临时的canvas
        Canvas canvas = new Canvas(bmTouchLayer);

        //向画布上画马赛克，正常画路径

        Path pathTemp = path.drawPath;
        int drawWidth = path.paintWidth;
        paint.setStrokeWidth(drawWidth);
        canvas.drawPath(pathTemp, paint);


        //创建马赛克图层
        Bitmap layer = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Bitmap.Config.ARGB_4444);
        canvas.setBitmap(layer);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawBitmap(mosaicResMap.get(path.effect), 0, 0, null);//马赛克样式的图层

        paint.reset();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));//显示手指触摸的地方
        canvas.drawBitmap(bmTouchLayer, 0, 0, paint);//手指操作的图层
        paint.setXfermode(null);
        canvas.save();


        canvas.setBitmap(bmMosaicLayer);
        canvas.drawBitmap(layer, 0, 0, null);

        layer.recycle();
        bmTouchLayer.recycle();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bmMosaicLayer != null) {
            this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            canvas.concat(mMatrix);
            this.setLayerType(View.LAYER_TYPE_NONE, null);
            canvas.drawBitmap(bmMosaicLayer, null, mImageRect, null);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {

        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        int contentWidth = right - left;
        int contentHeight = bottom - top;
        int viewWidth = contentWidth - mPadding * 2;
        int viewHeight = contentHeight - mPadding * 2;
        float widthRatio = viewWidth / ((float) mImageWidth);
        float heightRatio = viewHeight / ((float) mImageHeight);
        float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        int realWidth = (int) (mImageWidth * ratio);
        int realHeight = (int) (mImageHeight * ratio);

        int imageLeft = (contentWidth - realWidth) / 2;
        int imageTop = (contentHeight - realHeight) / 2;
        int imageRight = imageLeft + realWidth;
        int imageBottom = imageTop + realHeight;
        mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
    }

    /**
     * 返回马赛克最终结果
     *
     * @return 马赛克最终结果
     */
    public Bitmap getMosaicBitmap() {
        if (bmMosaicLayer == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bmMosaicLayer, 0, 0, null);
        canvas.save();
        return bitmap;
    }

    public Rect getImageRect(){
        return mImageRect;
    }


    public Bitmap getMosaicBit() {
        return bmMosaicLayer;
    }

    private int dp2px(int dip) {
        Context context = this.getContext();
        Resources resources = context.getResources();
        int px = Math
                .round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        dip, resources.getDisplayMetrics()));
        return px;
    }

    @Override
    public void setIsOperation(boolean isOperation) {
        this.isOperation = isOperation;
    }

    @Override
    public Boolean getIsOperation() {
        return null;
    }

    public void undo() {
        if (touchPaths.size() > 0) {
            MosaicPath undoable = touchPaths.remove(touchPaths.size() - 1);
            Log.i("wangyanjing", "撤销了一个mPath" + undoable.hashCode() + "====" + touchPaths.size());
            updatePathMosaic();
            invalidate();
        }
    }

    public void setMosaicEffect(MosaicUtil.Effect mosaicEffect) {
        this.mosaicEffect = mosaicEffect;
//        invalidate();
    }

    public MosaicUtil.Effect getMosaicEffect() {
        return mosaicEffect;
    }
}//end class
